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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.arakhne.afc.vmutil.locale.Locale;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * @author $Author: srodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class SpawnService {

	private Multimap<UUID, AgentLifecycleListener> lifecycleListeners = ArrayListMultimap.create();
	private Map<UUID, Agent> agents = new ConcurrentHashMap<>();



	@Inject
	private Logger log;

	@Inject
	private Injector injector;

	public UUID spawn(UUID parentID, Class<? extends Agent> agentClazz, Object... params) {

		try {
			Agent agent = agentClazz.getConstructor(UUID.class).newInstance(parentID);
			this.agents.put(agent.getID(), agent);
			this.injector.injectMembers(agent);
			
			notifyListenersAgentCreation(agent.getID(), params);
			return agent.getID();
		} catch (Exception e) {
			throw new RuntimeException(
					Locale.getString("CANNOT_INSTANCIATE_AGENT", agentClazz.getName()), e); //$NON-NLS-1$
		}
	}

	public void killAgent(UUID agentID) {
		this.agents.remove(agentID);
		notifyListenersAgentDestruction(agentID);
		if (this.agents.isEmpty()) {
			this.log.severe(Locale.getString("KERNEL_AGENT_STOPPED")); //$NON-NLS-1$
			this.injector.getInstance(Kernel.class).stop();
		}
	}



	/**
	 * @param id
	 * @param agentLifecycleListener
	 */
	public void addAgentLifecycleListener(UUID id, AgentLifecycleListener agentLifecycleListener) {
		this.lifecycleListeners.put(id, agentLifecycleListener);
	}

	private void notifyListenersAgentCreation(UUID agentID, Object[] params) {
		for (AgentLifecycleListener l : this.lifecycleListeners.get(agentID)) {
			this.log.finer(Locale.getString("NOTIFY_AGENT_CREATION_TO", l)); //$NON-NLS-1$
			l.agentSpawned(params);
		}
	}
	
	private void notifyListenersAgentDestruction(UUID agentID) {
		for (AgentLifecycleListener l : this.lifecycleListeners.get(agentID)) {
			this.log.finer(Locale.getString("NOTIFY_AGENT_DESTRUCTION_TO", l)); //$NON-NLS-1$
			l.agentDestroy();
		}
	}

}
