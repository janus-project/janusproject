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

import io.sarl.core.Lifecycle;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
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
		return this.spawnService.spawn(context.getID(), aAgent, params);
	}

	@Override
	public void killMe() {
		this.spawnService.killAgent(this.getOwner().getID());
	}

}
