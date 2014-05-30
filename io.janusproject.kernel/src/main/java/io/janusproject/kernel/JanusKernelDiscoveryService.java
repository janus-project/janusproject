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
import io.janusproject.services.impl.AbstractPrioritizedService;
import io.janusproject.services.impl.Services;
import io.janusproject.util.ListenerCollection;
import io.janusproject.util.TwoStepConstruction;
import io.sarl.util.Collections3;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

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
@TwoStepConstruction
class JanusKernelDiscoveryService extends AbstractPrioritizedService implements io.janusproject.services.KernelDiscoveryService {

	private final UUID janusID;
	private URI currentURI;

	private ISet<URI> kernels;

	private String hzRegId = null;

	private NetworkService network;

	private LogService logger;

	private ExecutorService executorService;

	private final ListenerCollection<KernelDiscoveryServiceListener> listeners = new ListenerCollection<>();

	/** Constructs a <code>KernelRepositoryService</code>.
	 * 
	 * @param janusID - injected identifier of the Janus context.
	 */
	@Inject
	public JanusKernelDiscoveryService(@Named(JanusConfig.DEFAULT_CONTEXT_ID_NAME) UUID janusID) {
		this.janusID = janusID;
		setStartPriority(Services.START_KERNEL_DISCOVERY_SERVICE);
		setStopPriority(Services.STOP_KERNEL_DISCOVERY_SERVICE);
	}
	

	/** Do the post initialization.
	 * 
	 * @param instance
	 * @param networkService
	 * @param executorService
	 * @param logger
	 */
	@Inject
	void postConstruction(HazelcastInstance instance, NetworkService networkService, ExecutorService executorService, LogService logger) {
		this.executorService = executorService;
		this.logger = logger;
		this.network = networkService;
		this.kernels = instance.getSet(this.janusID.toString() + "-kernels"); //$NON-NLS-1$
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
		return Collections3.synchronizedSet(Collections.unmodifiableSet(this.kernels),mutex());
	}

	/** {@inheritDoc}
	 */
	@Override
	public void addKernelDiscoveryServiceListener(KernelDiscoveryServiceListener listener) {
		this.listeners.add(KernelDiscoveryServiceListener.class, listener);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void removeKernelDiscoveryServiceListener(KernelDiscoveryServiceListener listener) {
		this.listeners.remove(KernelDiscoveryServiceListener.class, listener);
	}

	/** Notifies the listeners about the discovering of a kernel.
	 * 
	 * @param uri
	 */
	protected void fireKernelDiscovered(URI uri) {
		this.logger.info(
				JanusKernelDiscoveryService.class,
				"KERNEL_DISCOVERY", uri, getCurrentKernel()); //$NON-NLS-1$
		for(KernelDiscoveryServiceListener listener : this.listeners.getListeners(KernelDiscoveryServiceListener.class)) {
			listener.kernelDiscovered(uri);
		}
	}

	/** Notifies the listeners about the killing of a kernel.
	 * 
	 * @param uri
	 */
	protected void fireKernelDisconnected(URI uri) {
		this.logger.info(
				JanusKernelDiscoveryService.class,
				"KERNEL_DISCONNECTION", uri, getCurrentKernel()); //$NON-NLS-1$
		for(KernelDiscoveryServiceListener listener : this.listeners.getListeners(KernelDiscoveryServiceListener.class)) {
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
		// Remove the current kernel from the kernel's list
		if (this.currentURI!=null) {
			this.kernels.remove(this.currentURI);
		}
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
		@Override
		public void itemAdded(ItemEvent<URI> item) {
			fireKernelDiscovered(item.getItem());
		}

		/** {@inheritDoc}
		 */
		@Override
		public void itemRemoved(ItemEvent<URI> item) {
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
