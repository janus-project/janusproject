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
import io.janusproject.network.NetworkUtil;
import io.janusproject.services.ExecutorService;
import io.janusproject.services.KernelDiscoveryServiceListener;
import io.janusproject.services.LogService;
import io.janusproject.services.NetworkService;
import io.janusproject.services.impl.AbstractPrioritizedService;
import io.janusproject.services.impl.Services;
import io.janusproject.util.ListenerCollection;
import io.janusproject.util.TwoStepConstruction;
import io.sarl.util.Collections3;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MemberAttributeEvent;
import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

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
	private URI currentPubURI;
	private URI currentHzURI;

	private IMap<URI,URI> kernels;

	private String hzRegId1 = null;
	private String hzRegId2 = null;

	private NetworkService network;

	private LogService logger;

	private ExecutorService executorService;

	private final ListenerCollection<KernelDiscoveryServiceListener> listeners = new ListenerCollection<>();
	
	private HazelcastInstance hzInstance;
	
	private final HazelcastListener hzListener = new HazelcastListener(); 

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
		this.hzInstance = instance;
		this.logger = logger;
		this.network = networkService;
		this.kernels = instance.getMap(this.janusID.toString() + "-kernels"); //$NON-NLS-1$
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
		return this.currentPubURI;
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized Collection<URI> getKernels() {
		return Collections3.synchronizedCollection(Collections.unmodifiableMap(this.kernels).values(),mutex());
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
		this.hzRegId1 = this.kernels.addEntryListener(this.hzListener, true);
		this.hzRegId2 = this.hzInstance.getCluster().addMembershipListener(this.hzListener);
		notifyStarted();
	}

	/** {@inheritDoc}
	 */
	@Override
	protected synchronized void doStop() {
		if (this.hzRegId1!=null) this.kernels.removeEntryListener(this.hzRegId1);
		if (this.hzRegId2!=null) this.hzInstance.getClientService().removeClientListener(this.hzRegId2);
		// Remove the current kernel from the kernel's list
		if (this.currentHzURI!=null) {
			this.kernels.remove(this.currentHzURI);
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
	private class HazelcastListener implements EntryListener<URI,URI>, MembershipListener {

		/**
		 */
		public HazelcastListener() {
			//
		}

		/** {@inheritDoc}
		 */
		@Override
		public void memberAdded(MembershipEvent membershipEvent) {
			//
		}

		/** {@inheritDoc}
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void memberRemoved(MembershipEvent membershipEvent) {
			InetSocketAddress s = membershipEvent.getMember().getSocketAddress();
			if (s!=null) {
				URI u = NetworkUtil.toURI(s);
				if (u!=null) {
					synchronized(JanusKernelDiscoveryService.this) {
						JanusKernelDiscoveryService.this.kernels.remove(u);
					}
				}
			}
		}

		/** {@inheritDoc}
		 */
		@Override
		public void memberAttributeChanged(MemberAttributeEvent memberAttributeEvent) {
			//	
		}

		/** {@inheritDoc}
		 */
		@Override
		public void entryAdded(EntryEvent<URI, URI> event) {
			fireKernelDiscovered(event.getValue());
		}

		/** {@inheritDoc}
		 */
		@Override
		public void entryRemoved(EntryEvent<URI, URI> event) {
			fireKernelDisconnected(event.getValue());
		}

		/** {@inheritDoc}
		 */
		@Override
		public void entryUpdated(EntryEvent<URI, URI> event) {
			//
		}

		/** {@inheritDoc}
		 */
		@Override
		public void entryEvicted(EntryEvent<URI, URI> event) {
			fireKernelDisconnected(event.getValue());
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
				if (JanusKernelDiscoveryService.this.currentPubURI==null) {
					URI uri = JanusKernelDiscoveryService.this.network.getURI();
					JanusKernelDiscoveryService.this.currentPubURI = uri;
					JanusKernelDiscoveryService.this.currentHzURI = NetworkUtil.toURI(JanusKernelDiscoveryService.this.hzInstance.getCluster().getLocalMember().getSocketAddress());
					JanusKernelDiscoveryService.this.kernels.put(
							JanusKernelDiscoveryService.this.currentHzURI,
							JanusKernelDiscoveryService.this.currentPubURI);
				}
			}
		}

	}

}
