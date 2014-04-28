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

import io.sarl.core.Destroy;
import io.sarl.core.Initialize;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Capacity;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;

/** Capacity that provides an event bus to notify the different components of an agent. 
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
interface EventBusCapacity extends Capacity {
	
	/** Register the given object on the event bus for receiving
	 * any event.
	 * 
	 * @param listener
	 */
	public void registerEventListener(Object listener);
	
	/** Unregister the given object on the event bus for receiving
	 * any event.
	 * 
	 * @param listener
	 */
	public void unregisterEventListener(Object listener);

	/**
	 * Sends an event to itself using its defaultInnerAddress as source. Used
	 * for platform level event dispatching (i.e. {@link Initialize} and
	 * {@link Destroy})
	 * 
	 * @param event
	 */
	public void selfEvent(Event event);

	/**
	 * Replies the event listener linked to the owner of this capacity.
	 * 
	 * @return the event listener of the owner of this skill.
	 */
	public EventListener asEventListener();
	
	/** Replies the address of the agent in its inner default space.
	 * 
	 * @return the address of the agent in its inner default space.
	 */
	public Address getInnerDefaultSpaceAddress();

}
