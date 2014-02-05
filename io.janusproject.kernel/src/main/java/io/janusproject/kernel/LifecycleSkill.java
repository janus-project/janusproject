/*
 * $Id$
 * 
 * Janus platform is an open-source multiagent platform.
 * More details on &lt;http://www.janusproject.io&gt;
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

import io.sarl.core.Lifecycle;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Skill;

import java.util.UUID;

/**
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class LifecycleSkill extends Skill implements Lifecycle {

	
	private SpawnService spawnService;
	
	
	/**
	 * 
	 */
	public LifecycleSkill(Agent agent, SpawnService service) {
		super(agent);
		this.spawnService = service;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void install() {
		super.install();
	}
	
	/** {@inheritDoc}
	 */
	@Override
	protected void uninstall() {	
		super.uninstall();
	}

	/** {@inheritDoc}
	 */
	@Override
	public UUID spawnInContext(Class<? extends Agent> aAgent, AgentContext context,Object[] params) {
		return this.spawnService.spawn(context.getID(), aAgent, params);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void killMe() {
		this.spawnService.killAgent(this.getOwner().getID());
	}



	

}
