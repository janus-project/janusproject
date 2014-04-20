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
package io.janusproject2.kernel;

import io.janusproject2.services.ContextService;
import io.janusproject2.services.ExecutorService;
import io.janusproject2.services.KernelDiscoveryService;
import io.janusproject2.services.LogService;
import io.janusproject2.services.NetworkService;
import io.janusproject2.services.SpaceService;
import io.janusproject2.services.SpawnService;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.EventSpace;

import java.util.UUID;

import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;

/**
 * This class represents the Kernel of the Janus platform.
 * <p>
 * <strong>The Kernel is a singleton.</strong>
 * <p>
 * The Kernel is assimilated to an agent that is omniscient and 
 * distributed other the network. It is containing all the other agents.
 * <p>
 * To create a Kernel, you should use the function {@link #create(Module...)}.
 * 
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @author $Author: ngaud$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
public class Kernel {

	/** Create an instance of {@link Kernel}.
	 * 
	 * @param modules - modules to link to the new kernel.
	 * @return the new kernel.
	 */
	public static final Kernel create(Module... modules){
		Injector injector = Guice.createInjector(modules);
		Kernel k = injector.getInstance(Kernel.class);
		return k;
	}

	private AgentContext janusContext;

	private final ServiceManager serviceManager;

	@Inject
	private LogService logger;

	@Inject
	private ExecutorService executorService;

	@Inject
	private SpawnService spawnService;

	@Inject
	private NetworkService networkService;

	@Inject
	private ContextService contextService;

	@Inject
	private KernelDiscoveryService kernelDiscoveryService;
	
	@Inject
	private SpaceService spaceService;

	/**
	 * Constructs a Janus kernel.
	 * 
	 * @param serviceManager is the instance of the service manager that must be used by the kernel.
	 */
	@Inject
	Kernel(ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
		this.serviceManager.startAsync().awaitHealthy();
	}

	/**
	 * Replies the default space of the Janus agent.
	 * 
	 * @return the default space in the Janus agent.
	 */
	EventSpace getDefaultSpace() {
		return this.janusContext.getDefaultSpace();
	}

	/**
	 * Spawn an agent of the given type, and pass the parameters to its initialization function.
	 * 
	 * @param agent - the type of the agent to spawn.
	 * @param params - the list of the parameters to pass to the agent initialization function.
	 * @return the identifier of the agent, never <code>null</code>.
	 */
	public UUID spawn(Class<? extends Agent> agent, Object... params) {
		return this.spawnService.spawn(this.janusContext, agent, params);
	}

	/**
	 * Stop the Janus kernel.
	 */
	void stop() {
		this.logger.info("STOP_KERNEL_SERVICES"); //$NON-NLS-1$

		try {
			this.serviceManager.stopAsync().awaitStopped();
		} catch (Exception e) {
			this.logger.error("KERNEL_STOP_ERROR", e); //$NON-NLS-1$
		}

		this.logger.info("KERNEL_SERVICES_STOPPED"); //$NON-NLS-1$
	}

	/**
	 * Change the Janus context of the kernel.
	 * 
	 * @param janusContext - the new janus kernel. It must be never <code>null</code>.
	 */
	@Inject
	void setJanusContext(@io.janusproject2.kernel.annotations.Kernel AgentContext janusContext) {
		assert (janusContext != null);
		this.janusContext = janusContext;
	}

}
