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
import java.util.logging.Logger;

import org.arakhne.afc.vmutil.locale.Locale;

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
		this.log.info(Locale.getString("STOP_KERNEL_SERVICES")); //$NON-NLS-1$
		this.serviceManager.stopAsync().awaitStopped();
		this.executorService.shutdown();
		this.log.info(Locale.getString("KERNEL_SERVICES_STOPPED")); //$NON-NLS-1$
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
