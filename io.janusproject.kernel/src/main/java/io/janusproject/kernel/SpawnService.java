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
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

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
			throw new RuntimeException("Cannot instanciate Agent of type " + agentClazz.getName(), e);
		}
	}

	public void killAgent(UUID agentID) {
		this.agents.remove(agentID);
		notifyListenersAgentDestruction(agentID);
		if (this.agents.isEmpty()) {
			this.log.severe("Requesting Kernel Stop");
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
			this.log.finer("Notifing of agent creation to" + l.toString());
			l.agentSpawned(params);
		}
	}
	
	private void notifyListenersAgentDestruction(UUID agentID) {
		for (AgentLifecycleListener l : this.lifecycleListeners.get(agentID)) {
			this.log.finer("Notifing of agent destruction to" + l.toString());
			l.agentDestroy();
		}
	}

}
