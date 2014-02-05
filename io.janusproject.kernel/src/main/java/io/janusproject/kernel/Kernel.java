/*
 * $Id$
 * 
 * Janus platform is an open-source multiagent platform.
 * More details on &lt;http://www.janus-project.org&gt;
 * Copyright (C) 2013 Janus Core Developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
 */
package io.janusproject.kernel;

import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.EventSpace;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
public class Kernel {
	
	private static UncaughtExceptionHandler GLOBAL_HANDLER = new UncaughtExceptionHandler() {
		
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			e.printStackTrace();
			
		}
	};
	
	static{
		Thread.setDefaultUncaughtExceptionHandler(GLOBAL_HANDLER);
	}

	private AgentContext janusContext;

	private ServiceManager serviceManager = null;

	private SpawnService spawnService;

	@Inject
	private Network network;

	@Inject
	Logger log;

	@Inject
	private ExecutorService executorService;

	@Inject
	Kernel(ServiceManager serviceManager) {
		this.serviceManager = serviceManager;
		this.serviceManager.startAsync().awaitHealthy();
		
	}

	public EventSpace getDefaultSpace() {
		return this.janusContext.getDefaultSpace();
	}

	public void connectPeers(String... peers) throws Exception {
		for (String string : peers) {
			this.network.connectPeer(string);
		}
	}

	public UUID spawn(Class<? extends Agent> agent, Object... params) {
		return this.spawnService.spawn(this.janusContext.getID(), agent, params);
	}


	void stop() {
		this.log.info("Stopping Kernel Services");
		this.serviceManager.stopAsync().awaitStopped();
		this.executorService.shutdown();
		this.log.info("All Kernel Services stopped");
	}

	// ----- Injections
	@Inject
	void setJanusContext(@io.janusproject.kernel.annotations.Kernel AgentContext janusContext) {
		this.janusContext = janusContext;
	}
	@Inject
	void setSpawnSkill(SpawnService spawnService) {
		this.spawnService = spawnService;
	}
}
