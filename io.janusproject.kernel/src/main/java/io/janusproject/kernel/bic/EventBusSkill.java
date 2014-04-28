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

import io.janusproject.services.LogService;
import io.sarl.core.DefaultContextInteractions;
import io.sarl.core.Destroy;
import io.sarl.core.Initialize;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.Skill;

import java.lang.ref.WeakReference;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Queues;
import com.google.common.eventbus.AsyncSyncEventBus;
import com.google.inject.Inject;

/** Janus implementation of an internal skill that provides
 * an event bus to notify the different components of an agent. 
 * 
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class EventBusSkill extends Skill implements EventBusCapacity {
	
	/** State of the owner.
	 */
	private final AtomicReference<OwnerState> state = new AtomicReference<>(OwnerState.NEW);

	/** Implementation of an EventListener linked to the owner of this skill.
	 */
	private final AgentEventListener agentAsEventListener;
	
	/** Reference to the event bus.
	 * The event bus is the mean of routing of the events inside
	 * the context of the agents. The agent itself and the behaviors
	 * are connected to the event bus.
	 */
	@Inject
	private AsyncSyncEventBus eventBus;

	@Inject
	private LogService logger;

	/** Address of the agent in the inner space.
	 */
	private final Address agentAddressInInnerDefaultSpace;
	
	/**
	 * @param agent
	 * @param addressInInnerDefaultSpace
	 */
	public EventBusSkill(Agent agent, Address addressInInnerDefaultSpace) {
		super(agent);
		this.agentAsEventListener = new AgentEventListener(this);
		this.agentAddressInInnerDefaultSpace = addressInInnerDefaultSpace;
	}
	
	/** {@inheritDoc}
	 */
	@Override
	protected String attributesToString() {
		return super.attributesToString()
				+", state = "+this.state //$NON-NLS-1$
				+", addressInDefaultspace = "+this.agentAddressInInnerDefaultSpace; //$NON-NLS-1$
	}

	@Override
	public synchronized Address getInnerDefaultSpaceAddress() {
		return this.agentAddressInInnerDefaultSpace;
	}
	
	@Override
	protected void install() {
		this.eventBus.register(getOwner());
	}
	
	@Override
	protected void uninstall() {
		this.eventBus.unregister(getOwner());
		// TODO: dispose eventBus => remove any registered objects
	}
	
	@Override
	public void registerEventListener(Object listener) {
		this.eventBus.register(listener);
	}

	@Override
	public void unregisterEventListener(Object listener) {
		this.eventBus.unregister(listener);
	}

	/**
	 * Internal event dispatching. This methods post the event in the internal
	 * eventbus without modifying it (i.e. as is)
	 * 
	 * This method is called when: - A behavior wakes other behaviors and the
	 * event is returned from the inner default Space. - The
	 * {@link DefaultContextInteractions} receives an event.
	 * 
	 * Do not call directly
	 * 
	 * @param event
	 */
	private synchronized void internalReceiveEvent(Event event) {
		this.eventBus.post(event);
	}

	@Override
	public synchronized void selfEvent(Event event) {
		event.setSource(getInnerDefaultSpaceAddress());
		if (event instanceof Initialize) {
			this.eventBus.fire(event);//Immediate synchronous dispatching of Initialize event
			this.state.set(OwnerState.RUNNING);
			this.agentAsEventListener.agentInitialized();
		} else if (event instanceof Destroy) {
			this.eventBus.fire(event);//Immediate synchronous dispatching of Destroy event
			this.state.set(OwnerState.DESTROYED);
		} else {
			internalReceiveEvent(event);//Asynchronous parallel dispatching of this event
		}
		this.logger.debug("SELF_EVENT", event); //$NON-NLS-1$
	}

	@Override
	public EventListener asEventListener() {
		return this.agentAsEventListener;
	}

	/**
	 * @author $Author: srodriguez$
	 * @author $Author: ngaud$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class AgentEventListener implements EventListener {

		private final Queue<Event> buffer = Queues.newConcurrentLinkedQueue();

		private final WeakReference<EventBusSkill> skill;

		private final UUID aid;

		@SuppressWarnings("synthetic-access")
		public AgentEventListener(EventBusSkill skill) {
			this.skill = new WeakReference<>(skill);
			this.aid = skill.getOwner().getID();
		}

		@Override
		public UUID getID() {
			return this.aid;
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void receiveEvent(Event event) {
			EventBusSkill s = this.skill.get();
			synchronized(s) {
				switch(s.state.get()) {
				case NEW:
					this.buffer.add(event);
					break;
				case RUNNING:
					s.internalReceiveEvent(event);
					break;
				case DESTROYED:
					// Dropping messages since agent is dying
					s.logger.warning(EventBusSkill.class,
									"EVENT_DROP_WARNING", event); //$NON-NLS-1$
					break;
				default:
					throw new IllegalStateException();
				}
			}
		}

		@SuppressWarnings("synthetic-access")
		void agentInitialized() {
			EventBusSkill s = this.skill.get();
			synchronized(s) {
				for (Event evt : this.buffer) {
					s.internalReceiveEvent(evt);
				}
			}
		}

	}
	
	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static enum OwnerState {
		NEW, RUNNING, DESTROYED
	}

}
