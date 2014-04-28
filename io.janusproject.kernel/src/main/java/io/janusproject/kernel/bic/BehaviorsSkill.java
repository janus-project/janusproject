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
package io.janusproject.kernel.bic;

import io.sarl.core.Behaviors;
import io.sarl.core.InnerContextAccess;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Behavior;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.Skill;

/** Janus implementation of SARL's {@link Behaviors} built-in capacity.
 * 
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class BehaviorsSkill extends Skill implements Behaviors {
	
	/**
	 * @param agent
	 */
	public BehaviorsSkill(Agent agent) {
		super(agent);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Behavior registerBehavior(Behavior attitude) {
		getSkill(EventBusCapacity.class).registerEventListener(attitude);
		return attitude;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Behavior unregisterBehavior(Behavior attitude) {
		getSkill(EventBusCapacity.class).unregisterEventListener(attitude);
		return attitude;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized void wake(Event evt) {
		// Use the inner space so all behaviors (even agents inside the holon
		// running in distant kernels) are notified. The event will return into
		// the agent via the inner default space add call internalReceiveEvent
		// for real posting
		EventSpace defSpace = getSkill(InnerContextAccess.class).getInnerContext().getDefaultSpace();
		evt.setSource(defSpace.getAddress(getOwner().getID()));
		defSpace.emit(evt);
	}

	/** {@inheritDoc}
	 */
	@Override
	public EventListener asEventListener() {
		return getSkill(EventBusCapacity.class).asEventListener();
	}

}
