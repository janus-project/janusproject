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

/** Skill to access to the default interaction context.
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class DefaultContextInteractionsImpl extends Skill implements
		DefaultContextInteractions {

	private AgentContext parentContext;
	private EventSpace defaultSpace;
	private Address agentAddress;


	/** Constructs a <code>DefaultContextInteractionsImpl</code>.
	 * 
	 * @param agent - owner of the skill.
	 * @param parentContext - reference to the parent context.
	 */
	public DefaultContextInteractionsImpl(Agent agent, AgentContext parentContext) {
		super(agent);
		this.parentContext = parentContext;
		
	}

	@Override
	protected void install() {
		this.defaultSpace = this.parentContext.getDefaultSpace();
		this.agentAddress = this.defaultSpace.getAddress(getOwner().getID());
		
	}
	
	@Override
	protected void uninstall() {	
		super.uninstall();
	}

	@Override
	public void emit(Event event) {
		event.setSource(this.agentAddress);
		this.defaultSpace.emit(event);
	}

	@Override
	public void emit(Event event, Scope<Address> scope) {
		event.setSource(this.agentAddress);
		this.defaultSpace.emit(event, scope);
	}

	@Override
	public Address getDefaultAddress() {
		return this.agentAddress;
	}

	@Override
	public AgentContext getDefaultContext() {
		return this.parentContext;
	}

	@Override
	public EventSpace getDefaultSpace() {
		return this.defaultSpace;
	}

	@Override
	public void receive(UUID receiverID, Event event) {
		Address recAddr = this.defaultSpace.getAddress(receiverID);
		this.emit(event, AddressScope.getScope(recAddr));
	}

	@Override
	public UUID spawn(Class<? extends Agent> aAgent, Object[] params) {
		return getSkill(Lifecycle.class).spawnInContext(aAgent, this.parentContext, params);
	}
	
}
