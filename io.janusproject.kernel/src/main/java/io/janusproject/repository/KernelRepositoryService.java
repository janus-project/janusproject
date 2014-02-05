/*
 * Copyright 2014 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.janusproject.repository;

import io.janusproject.kernel.Network;
import io.janusproject.kernel.annotations.Kernel;
import io.janusproject.network.zeromq.ZeroMQConfig;
import io.janusproject.repository.impl.RepositoryImplFactory;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;

/**
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class KernelRepositoryService extends AbstractService {
	private ISet<String> kernels;
	private String distributedParticipantMapName;
	private String localURI;

	@Inject
	private HazelcastInstance instance;

	@Inject
	private Logger log;

	private Network network;

	@Inject
	@Kernel
	private ExecutorService executorService;

	@Inject
	void KernelRepository(@Named(JanusConfig.JANUS_CONTEXT_ID) UUID janusID,
			RepositoryImplFactory repositoryImplFactory, @Named(ZeroMQConfig.PUB_URI) String myuri) {
		this.kernels = repositoryImplFactory.getSet(janusID.toString() + "-kernels");

		this.localURI = myuri;

	}

	@Inject
	void setNetwork(Network network) {
		this.network = network;
		this.network.addListener(new NetworkListener(this), this.executorService);
	}

	void connectExiting() {

		for (String peerURI : this.kernels) {
			this.log.finer("Connecting to existing Peer " + peerURI);
			if (!this.localURI.equals(peerURI)) {
				connect(peerURI);
			}
		}
	}

	void connect(String peer) {
		try {
			this.network.connectPeer(peer);
		} catch (Exception e) {
			throw new RuntimeException("Error while connecting to peer " + peer + " to Network");
		}
	}

	void disconnect(String peer) {
		try {
			this.network.disconnectPeer(peer);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error while disconnecting to peer " + peer + " to Network");
		}
	}

	private static class HazelcastListener implements ItemListener<String> {
		private final KernelRepositoryService kernelRepositoryService;

		/**
		 * 
		 */
		public HazelcastListener(KernelRepositoryService kernelRepositoryService) {
			this.kernelRepositoryService = kernelRepositoryService;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void itemAdded(ItemEvent<String> item) {
			if (!this.kernelRepositoryService.localURI.equals(item.getItem())) {
				this.kernelRepositoryService.connect(item.getItem());
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void itemRemoved(ItemEvent<String> item) {
			if (!this.kernelRepositoryService.localURI.equals(item.getItem())) {
				this.kernelRepositoryService.disconnect(item.getItem());
			}
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doStart() {
		this.kernels.add(this.localURI);

		notifyStarted();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void doStop() {
		this.kernels.remove(this.localURI);

		notifyStopped();
		this.log.info("KernelRespositoryService Shutdown");
	}

	private static class NetworkListener extends Listener {

		private final KernelRepositoryService kernelRepositoryService;

		/**
		 * 
		 */
		public NetworkListener(KernelRepositoryService kernelRepositoryService) {
			this.kernelRepositoryService = kernelRepositoryService;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void starting() {
			this.kernelRepositoryService.log.info("Hazelcast starting");

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void running() {
			this.kernelRepositoryService.log.info("Hazelcast init start");
			this.kernelRepositoryService.kernels.addItemListener(new HazelcastListener(this.kernelRepositoryService),
					true);
			this.kernelRepositoryService.connectExiting();
			this.kernelRepositoryService.log.info("Hazelcast init end");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void terminated(State from) {			
			this.kernelRepositoryService.log.info("Hazelcast Shutdown");
			this.kernelRepositoryService.instance.shutdown();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void stopping(State from) {			
			this.kernelRepositoryService.log.info("Hazelcast stopping");
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void failed(State from, Throwable failure) {
			System.out.println("Syso Hazelcast Failure");

			super.failed(from, failure);
			this.kernelRepositoryService.log.log(Level.SEVERE, "Failure on Network Service ", failure);
		}
	}
}
