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

import java.util.UUID;

import io.sarl.core.DefaultContextInteractions;
import io.sarl.core.Lifecycle;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.Skill;

/**
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class DefaultContextInteractionsImpl extends Skill implements
		DefaultContextInteractions {

	private AgentContext parentContext;
	private EventSpace defaultSpace;
	private Address agentAddress;


	/**
	 * @param agent
	 */
	public DefaultContextInteractionsImpl(Agent agent, AgentContext parentContext) {
		super(agent);
		this.parentContext = parentContext;
		
	}

	
	
	/** {@inheritDoc}
	 */
	@Override
	protected void install() {
		this.defaultSpace = this.parentContext.getDefaultSpace();
		this.agentAddress = this.defaultSpace.getAddress(getOwner().getID());
		
	}
	
	/** {@inheritDoc}
	 */
	@Override
	protected void uninstall() {	
		super.uninstall();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void emit(Event event) {
		event.setSource(this.agentAddress);
		this.defaultSpace.emit(event);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void emit(Event event, Scope<Address> scope) {
		event.setSource(this.agentAddress);
		this.defaultSpace.emit(event, scope);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Address getDefaultAddress() {
		return this.agentAddress;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AgentContext getDefaultContext() {
		return this.parentContext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EventSpace getDefaultSpace() {
		return this.defaultSpace;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void receive(UUID receiverID, Event event) {
		Address recAddr = this.defaultSpace.getAddress(receiverID);
		this.emit(event, AddressScope.getScope(recAddr));
	}


	/** {@inheritDoc}
	 */
	@Override
	public UUID spawn(Class<? extends Agent> aAgent, Object[] params) {
		return getSkill(Lifecycle.class).spawnInContext(aAgent, this.parentContext, params);
		
	}

	
}
