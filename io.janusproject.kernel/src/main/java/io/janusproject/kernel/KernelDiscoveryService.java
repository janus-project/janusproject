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
import io.janusproject.kernel.annotations.Kernel;
import io.janusproject.network.zeromq.ZeroMQConfig;
import io.janusproject.repository.impl.DistributedDataStructureFactory;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arakhne.afc.vmutil.locale.Locale;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
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
public class KernelDiscoveryService extends AbstractService {

	private ISet<String> kernels;
	private String localURI;

	@Inject
	private HazelcastInstance instance;

	@Inject
	private Logger log;

	private Network network;

	@Inject
	@Kernel
	private ExecutorService executorService;

	/** Constructs a <code>KernelRepositoryService</code>.
	 * 
	 * @param janusID - injected identifier of the Janus context.
	 * @param repositoryImplFactory - factory to build a context.
	 * @param myuri - injected URI of the current kernel.
	 */
	@Inject
	void KernelRepository(@Named(JanusConfig.JANUS_CONTEXT_ID) UUID janusID,
			DistributedDataStructureFactory repositoryImplFactory, @Named(ZeroMQConfig.PUB_URI) String myuri) {
		this.kernels = repositoryImplFactory.getSet(janusID.toString() + "-kernels"); //$NON-NLS-1$
		this.localURI = myuri;
	}

	/** Change the network interface.
	 * 
	 * @param network
	 */
	@Inject
	void setNetwork(Network network) {
		this.network = network;
		this.network.addListener(new NetworkListener(), this.executorService);
	}

	/** Connect to the known kernel peers.
	 */
	void connectExiting() {
		for (String peerURI : this.kernels) {
			this.log.finer(Locale.getString("CONNECTING_TO_PEER", peerURI)); //$NON-NLS-1$
			if (!this.localURI.equals(peerURI)) {
				connect(peerURI);
			}
		}
	}

	/** Connect to a peer repository.
	 * 
	 * @param peer
	 */
	void connect(String peer) {
		try {
			this.network.connectPeer(peer);
		} catch (Exception e) {
			throw new RuntimeException(Locale.getString("CONNECTION_ERROR", peer)); //$NON-NLS-1$
		}
	}

	/** Disconnect from a peer repository.
	 * 
	 * @param peer
	 */
	void disconnect(String peer) {
		try {
			this.network.disconnectPeer(peer);
		} catch (Exception e) {
			throw new RuntimeException(Locale.getString("DISCONNECTION_ERROR", peer)); //$NON-NLS-1$
		}
	}

	/**
	 * @author $Author: srodriguez$
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class HazelcastListener implements ItemListener<String> {

		/**
		 */
		public HazelcastListener() {
			//
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void itemAdded(ItemEvent<String> item) {
			if (!KernelDiscoveryService.this.localURI.equals(item.getItem())) {
				KernelDiscoveryService.this.connect(item.getItem());
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void itemRemoved(ItemEvent<String> item) {
			if (!KernelDiscoveryService.this.localURI.equals(item.getItem())) {
				KernelDiscoveryService.this.disconnect(item.getItem());
			}
		}

	}

	@Override
	protected void doStart() {
		this.kernels.add(this.localURI);

		notifyStarted();
	}

	@Override
	protected void doStop() {
		this.kernels.remove(this.localURI);

		notifyStopped();
		this.log.info(Locale.getString("SHUTDOWN")); //$NON-NLS-1$
	}

	/**
	 * @author $Author: srodriguez$
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class NetworkListener extends Listener {

		/**
		 */
		public NetworkListener() {
			//
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void starting() {
			KernelDiscoveryService.this.log.info(
					Locale.getString(KernelDiscoveryService.class, "HAZELCAST_STARTING")); //$NON-NLS-1$
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void running() {
			KernelDiscoveryService.this.log.info(
					Locale.getString(KernelDiscoveryService.class, "HAZELCAST_INIT_STARTING")); //$NON-NLS-1$
			KernelDiscoveryService.this.kernels.addItemListener(new HazelcastListener(),
					true);
			KernelDiscoveryService.this.connectExiting();
			KernelDiscoveryService.this.log.info(
					Locale.getString(KernelDiscoveryService.class, "HAZELCAST_INIT_ENDING")); //$NON-NLS-1$
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void terminated(State from) {			
			KernelDiscoveryService.this.log.info(
					Locale.getString(KernelDiscoveryService.class, "HAZELCAST_ENDING")); //$NON-NLS-1$
			KernelDiscoveryService.this.instance.shutdown();
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void stopping(State from) {			
			KernelDiscoveryService.this.log.info(
					Locale.getString(KernelDiscoveryService.class, "HAZELCAST_ENDING")); //$NON-NLS-1$
		}

		/**
		 * {@inheritDoc}
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void failed(State from, Throwable failure) {
			super.failed(from, failure);
			KernelDiscoveryService.this.log.log(Level.SEVERE, 
					Locale.getString(KernelDiscoveryService.class, "NETWORK_FAILURE"), //$NON-NLS-1$
					failure);
		}
	}
}
