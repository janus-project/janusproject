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

import io.sarl.core.AgentKilled;
import io.sarl.core.AgentSpawned;
import io.sarl.core.Behaviors;
import io.sarl.core.Destroy;
import io.sarl.core.ExternalContextAccess;
import io.sarl.core.Lifecycle;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.Skill;

import java.util.UUID;

/** Skill that permits to manage the life cycle of the agents.
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class LifecycleSkill extends Skill implements Lifecycle {

	
	private final SpawnService spawnService;
	
	
	/** Constructs the skill.
	 * 
	 * @param agent - owner of the skill.
	 * @param service - reference to the spawning service to use.
	 */
	public LifecycleSkill(Agent agent, SpawnService service) {
		super(agent);
		this.spawnService = service;
	}
	
	@Override
	protected void install() {
		super.install();
	}
	
	@Override
	protected void uninstall() {	
		super.uninstall();
	}

	@Override
	public UUID spawnInContext(Class<? extends Agent> aAgent, AgentContext context,Object[] params) {
		UUID id = this.spawnService.spawn(context.getID(), aAgent, params);
		fireAgentSpawned(aAgent,context);
		return id;
	}
	
	/**
	 * Fire an {@link AgentSpawned} event to inform other context members of the creation of a new agent in the specified context.
	 * @param parentContext - the context in which the owner agent has been spanwed
	 */
	protected void fireAgentSpawned(Class<? extends Agent> aAgent, AgentContext parentContext) {
		EventSpace defSpace = parentContext.getDefaultSpace();
		AgentSpawned event = new AgentSpawned();
		event.setAgentID(this.getOwner().getID());
		event.setAgentType(aAgent);
		event.setSource(defSpace.getAddress(getOwner().getID()));
		defSpace.emit(event);
	}

	@Override
	public void killMe() {
		fireDestroy();
		fireAgentKilledInAllCurrentContexts();
		this.spawnService.killAgent(this.getOwner().getID());
	}
	
	/**
	 * Fire an {@link Destroy} event to inform owner agent's behaviors that this agent will die immediately
	 * Normally it remains any members in this agents at this step
	 */
	protected void fireDestroy() {
		Destroy event = new Destroy();				
		getSkill(Behaviors.class).wake(event);
	}
	
	/**
	 * Fire an {@link AgentKilled} event in every single contexts to which the owner  agent belongs to
	 */
	protected void fireAgentKilledInAllCurrentContexts() {
		for(AgentContext context : getSkill(ExternalContextAccess.class).getAllContexts()) {
			fireAgentLeft(context);
		}
	}
	
	/**
	 * Fire an {@link AgentKilled} event to inform other context members of the death of the owner agent in the specified context.
	 * @param parentContext - the default context of the owner agent
	 */
	protected void fireAgentLeft(AgentContext leftContext) {
		EventSpace defSpace = leftContext.getDefaultSpace();
		AgentKilled event = new AgentKilled();
		event.setAgentID(this.getOwner().getID());
		event.setSource(defSpace.getAddress(getOwner().getID()));
		defSpace.emit(event);
	}


}
