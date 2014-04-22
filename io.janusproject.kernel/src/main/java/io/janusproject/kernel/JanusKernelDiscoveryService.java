/*
/*
 * $Id$
 * 
 * Janus platform is an open-source multiagent platform.
 * More details on http://www.janusproject.io
 * 
 * Copyright (C) 2014 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.janusproject.kernel;

import io.janusproject.JanusConfig;
import io.janusproject.services.ExecutorService;
import io.janusproject.services.KernelDiscoveryServiceListener;
import io.janusproject.services.LogService;
import io.janusproject.services.NetworkService;
import io.janusproject.util.Collections3;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;

/** Service that is providing the access to the repository of the Janus kernels.
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
class JanusKernelDiscoveryService extends AbstractService implements io.janusproject.services.KernelDiscoveryService {

	private final UUID janusID;
	private URI currentURI;

	private ISet<URI> kernels;

	private String hzRegId = null;

	private NetworkService network;

	@Inject
	private LogService logger;

	@Inject
	private ExecutorService executorService;

	private final List<KernelDiscoveryServiceListener> listeners = new ArrayList<>();

	/** Constructs a <code>KernelRepositoryService</code>.
	 * 
	 * @param janusID - injected identifier of the Janus context.
	 */
	@Inject
	JanusKernelDiscoveryService(@Named(JanusConfig.DEFAULT_CONTEXT_ID) UUID janusID) {
		this.janusID = janusID;
	}

	@Inject
	private void setHazelcastInstance(HazelcastInstance instance) {
		this.kernels = instance.getSet(this.janusID.toString() + "-kernels"); //$NON-NLS-1$
	}

	@Inject
	private void setNetwork(NetworkService service) {
		this.network = service;
		this.network.addListener(new NetworkStartListener(), this.executorService.getExecutorService());
	}

	/** {@inheritDoc}
	 */
	@Override
	public Object mutex() {
		return this;
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized URI getCurrentKernel() {
		return this.currentURI;
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized Collection<URI> getKernels() {
		return Collections.unmodifiableSet(Collections3.synchronizedSet(this.kernels,mutex()));
	}

	/** {@inheritDoc}
	 */
	@Override
	public void addKernelDiscoveryServiceListener(
			KernelDiscoveryServiceListener listener) {
		synchronized(this.listeners) {
			this.listeners.add(listener);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public void removeKernelDiscoveryServiceListener(
			KernelDiscoveryServiceListener listener) {
		synchronized(this.listeners) {
			this.listeners.remove(listener);
		}
	}

	/** Notifies the listeners about the discovering of a kernel.
	 * 
	 * @param uri
	 */
	protected void fireKernelDiscovered(URI uri) {
		KernelDiscoveryServiceListener[] listeners;
		synchronized(this.listeners) {
			listeners = new KernelDiscoveryServiceListener[this.listeners.size()];
			this.listeners.toArray(listeners);
		}
		for(KernelDiscoveryServiceListener listener : listeners) {
			listener.kernelDiscovered(uri);
		}
	}

	/** Notifies the listeners about the killing of a kernel.
	 * 
	 * @param uri
	 */
	protected void fireKernelDisconnected(URI uri) {
		KernelDiscoveryServiceListener[] listeners;
		synchronized(this.listeners) {
			listeners = new KernelDiscoveryServiceListener[this.listeners.size()];
			this.listeners.toArray(listeners);
		}
		for(KernelDiscoveryServiceListener listener : listeners) {
			listener.kernelDisconnected(uri);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	protected synchronized void doStart() {
		this.hzRegId = this.kernels.addItemListener(new HazelcastListener(), true);
		notifyStarted();
	}

	/** {@inheritDoc}
	 */
	@Override
	protected synchronized void doStop() {
		if (this.hzRegId!=null) this.kernels.removeItemListener(this.hzRegId);
		// Unconnect the kernel collection from remote clusters
		// Not needed becasue the Kernel will be stopped: this.kernels.destroy();
		notifyStopped();
	}

	/**
	 * @author $Author: srodriguez$
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class HazelcastListener implements ItemListener<URI> {

		/**
		 */
		public HazelcastListener() {
			//
		}

		/** {@inheritDoc}
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void itemAdded(ItemEvent<URI> item) {
			JanusKernelDiscoveryService.this.logger.debug(
					JanusKernelDiscoveryService.class,
					"KERNEL_DISCOVERY", item.getItem()); //$NON-NLS-1$
			fireKernelDiscovered(item.getItem());
		}

		/** {@inheritDoc}
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void itemRemoved(ItemEvent<URI> item) {
			JanusKernelDiscoveryService.this.logger.debug(
					JanusKernelDiscoveryService.class,
					"KERNEL_DISCONNECTION", item.getItem()); //$NON-NLS-1$
			fireKernelDisconnected(item.getItem());
		}

	}

	/**
	 * @author $Author: srodriguez$
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class NetworkStartListener extends Listener {

		/**
		 */
		public NetworkStartListener() {
			//
		}

		/** {@inheritDoc}
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void running() {
			super.running();
			synchronized(JanusKernelDiscoveryService.this) {
				if (JanusKernelDiscoveryService.this.currentURI==null) {
					URI uri = JanusKernelDiscoveryService.this.network.getURI();
					JanusKernelDiscoveryService.this.currentURI = uri;
					JanusKernelDiscoveryService.this.kernels.add(uri);
				}
			}
		}

	}

}
