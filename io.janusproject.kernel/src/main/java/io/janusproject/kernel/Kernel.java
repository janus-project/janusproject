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

import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.EventSpace;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arakhne.afc.vmutil.locale.Locale;

import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * This class represents the Kernel of the Janus platform.
 * <p>
 * <strong>The Kernel is a singleton.</strong>
 * <p>
 * The Kernel is assimilated to an agent that is omniscient and distributed other the network. It is containing all the other agents.
 * <p>
 * To create a Kernel, you should use the functions in the class {@link Janus}.
 * 
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
public class Kernel {

	private AgentContext janusContext;

	private ServiceManager serviceManager = null;

	private SpawnService spawnService;

	@Inject
	private Network network;

	/**
	 * Logger of the kernel.
	 */
	@Inject
	Logger log;

	@Inject
	private ExecutorService executorService;

	/**
	 * Constructs a Janus kernel.
	 * 
	 * @param serviceManager is the instance of the service manager that must be used by the kernel.
	 */
	@Inject
	Kernel(ServiceManager serviceManager) {
		// Register a default exception handler that
		// is logging on the kernel's log.
		UncaughtExceptionHandler h = Thread.getDefaultUncaughtExceptionHandler();
		if (h == null) {
			UncaughtExceptionHandler handler = new UncaughtExceptionHandler() {
				@Override
				public void uncaughtException(Thread t, Throwable e) {
					Kernel.this.log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				}
			};
			Thread.setDefaultUncaughtExceptionHandler(handler);
		}

		this.serviceManager = serviceManager;
		this.serviceManager.startAsync().awaitHealthy();
	}

	/**
	 * Replies the default space of the Janus agent.
	 * 
	 * @return the default space in the Janus agent.
	 */
	public EventSpace getDefaultSpace() {
		return this.janusContext.getDefaultSpace();
	}

	/**
	 * Connect this kernel to the given peers.
	 * 
	 * @param peers - list of the peers to be connected to.
	 * @throws Exception
	 */
	public void connectPeers(String... peers) throws Exception {
		for (String string : peers) {
			this.network.connectPeer(string);
		}
	}

	/**
	 * Spawn an agent of the given type, and pass the parameters to its initialization function.
	 * 
	 * @param agent - the type of the agent to spawn.
	 * @param params - the list of the parameters to pass to the agent initialization function.
	 * @return the identifier of the agent.
	 */
	public UUID spawn(Class<? extends Agent> agent, Object... params) {
		return this.spawnService.spawn(this.janusContext.getID(), agent, params);
	}

	/**
	 * Stop the Janus kernel.
	 */
	void stop() {
		this.log.info(Locale.getString("STOP_KERNEL_SERVICES")); //$NON-NLS-1$

		try {
			this.serviceManager.stopAsync().awaitStopped();
		} catch (Exception e) {
			this.log.log(Level.SEVERE, "Error duing service manager stopping", e);
		}

		this.executorService.shutdown();
		this.executorService.shutdownNow();
		this.log.info(Locale.getString("KERNEL_SERVICES_STOPPED")); //$NON-NLS-1$
	}

	/**
	 * Change the Janus context of the kernel.
	 * 
	 * @param janusContext - the new janus kernel. It must be never <code>null</code>.
	 */
	@Inject
	void setJanusContext(@io.janusproject.kernel.annotations.Kernel AgentContext janusContext) {
		assert (janusContext != null);
		this.janusContext = janusContext;
	}

	/**
	 * Change the spawning service.
	 * 
	 * @param spawnService - the new spawning service. It must be never <code>null</code> .
	 */
	@Inject
	void setSpawnSkill(SpawnService spawnService) {
		assert (spawnService != null);
		this.spawnService = spawnService;
	}

}
