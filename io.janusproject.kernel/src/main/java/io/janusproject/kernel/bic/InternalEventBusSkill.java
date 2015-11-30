/*
 * $Id$
 *
 * Janus platform is an open-source multiagent platform.
 * More details on http://www.janusproject.io
 *
 * Copyright (C) 2014-2015 the original authors or authors.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.Queues;
import com.google.common.eventbus.AsyncSyncEventBus;
import com.google.inject.Inject;
import io.janusproject.services.logging.LogService;
import io.janusproject.services.spawn.SpawnService;
import io.janusproject.services.spawn.SpawnService.AgentKillException;

import io.sarl.core.AgentSpawned;
import io.sarl.core.Destroy;
import io.sarl.core.Initialize;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.Skill;

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
class InternalEventBusSkill extends Skill implements InternalEventBusCapacity {

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

	@Inject
	private SpawnService spawnService;

	/** Address of the agent in the inner space.
	 */
	private final Address agentAddressInInnerDefaultSpace;

	/** Collection of objects that are listening the event bus,
	 * except the owner of this skill.
	 */
	private List<Object> eventListeners;

	/**
	 * @param agent - reference to the owner of this skill.
	 * @param addressInInnerDefaultSpace - address of the owner of this skill
	 *                                     in its inner default space.
	 */
	InternalEventBusSkill(Agent agent, Address addressInInnerDefaultSpace) {
		super(agent);
		this.agentAsEventListener = new AgentEventListener();
		this.agentAddressInInnerDefaultSpace = addressInInnerDefaultSpace;
	}

	@Override
	protected String attributesToString() {
		return super.attributesToString()
				+ ", state = " + this.state //$NON-NLS-1$
				+ ", addressInDefaultspace = " + this.agentAddressInInnerDefaultSpace; //$NON-NLS-1$
	}

	@Override
	public OwnerState getOwnerState() {
		return this.state.get();
	}

	@Override
	public synchronized Address getInnerDefaultSpaceAddress() {
		return this.agentAddressInInnerDefaultSpace;
	}

	@Override
	protected synchronized void install() {
		this.eventBus.register(getOwner());
	}

	@Override
	protected synchronized void uninstall() {
		this.eventBus.unregister(getOwner());
		// TODO: dispose eventBus => remove any registered objects, but without a list in this skill
		List<Object> list = this.eventListeners;
		this.eventListeners = null;
		if (list != null) {
			for (Object o : list) {
				this.eventBus.unregister(o);
			}
		}
	}

	@Override
	public synchronized void registerEventListener(Object listener) {
		this.eventBus.register(listener);
		if (this.eventListeners == null) {
			this.eventListeners = new ArrayList<>();
		}
		this.eventListeners.add(listener);
	}

	@Override
	public synchronized void unregisterEventListener(Object listener) {
		this.eventBus.unregister(listener);
		if (this.eventListeners != null) {
			this.eventListeners.remove(listener);
			if (this.eventListeners.isEmpty()) {
				this.eventListeners = null;
			}
		}
	}

	@Override
	public synchronized void selfEvent(Event event) {
		// Ensure that the event source is the agent itself!
		event.setSource(getInnerDefaultSpaceAddress());
		// If the event must be fired only by the
		// agent itself, it is treated in this function.
		// Otherwise, it is given to the asynchronous
		// listener.
		if (event instanceof Initialize) {
			//Immediate synchronous dispatching of Initialize event
			this.eventBus.fire(event);
			this.state.set(OwnerState.RUNNING);
		} else if (event instanceof Destroy) {
			//Immediate synchronous dispatching of Destroy event
			this.state.set(OwnerState.DESTROYED);
			this.eventBus.fire(event);
		} else if (event instanceof AsynchronousAgentKillingEvent) {
			//Asynchronous kill of the event.
			this.agentAsEventListener.killOrMarkAsKilled();
		} else {
			//Asynchronous parallel dispatching of this event
			this.agentAsEventListener.receiveEvent(event);
		}
		this.logger.debug("SELF_EVENT", event); //$NON-NLS-1$
	}

	@Override
	public final EventListener asEventListener() {
		return this.agentAsEventListener;
	}

	/** Definition of the listener on events on the agent's bus.
	 *
	 * @author $Author: srodriguez$
	 * @author $Author: ngaud$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class AgentEventListener implements EventListener {

		private Queue<Event> buffer = Queues.newConcurrentLinkedQueue();

		private final UUID aid;

		private boolean isKilled;

		@SuppressWarnings("synthetic-access")
		AgentEventListener() {
			this.aid = InternalEventBusSkill.this.getOwner().getID();
		}

		@Override
		public UUID getID() {
			return this.aid;
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void receiveEvent(Event event) {
			assert ((!(event instanceof Initialize))
					&& (!(event instanceof Destroy))
					&& (!(event instanceof AsynchronousAgentKillingEvent)))
					: "Unsupported type of event: " + event; //$NON-NLS-1$
			synchronized (InternalEventBusSkill.this) {
				if (event instanceof AgentSpawned
						&& this.aid.equals(((AgentSpawned) event).agentID)) {
					// This permits to ensure that the killing event
					// is correctly treated when fired from the initialization
					// handler.
					fireEnqueuedEvents(InternalEventBusSkill.this);
					if (this.isKilled) {
						killOwner(InternalEventBusSkill.this);
						return;
					}
				}
				switch (InternalEventBusSkill.this.state.get()) {
				case NEW:
					this.buffer.add(event);
					break;
				case RUNNING:
					fireEnqueuedEvents(InternalEventBusSkill.this);
					InternalEventBusSkill.this.eventBus.post(event);
					break;
				case DESTROYED:
					// Dropping messages since agent is dying
					InternalEventBusSkill.this.logger.debug(InternalEventBusSkill.class,
							"EVENT_DROP_WARNING", event); //$NON-NLS-1$
					break;
				default:
					throw new IllegalStateException();
				}
			}
		}

		@SuppressWarnings("synthetic-access")
		private void fireEnqueuedEvents(InternalEventBusSkill skill) {
			Queue<Event> queue = this.buffer;
			if (queue != null && !queue.isEmpty()) {
				this.buffer = null;
				for (Event evt : queue) {
					skill.eventBus.post(evt);
				}
			}
		}

		@SuppressWarnings("synthetic-access")
		private void killOwner(InternalEventBusSkill skill) {
			try {
				skill.spawnService.killAgent(this.aid);
			} catch (AgentKillException e) {
				skill.logger.error(InternalEventBusSkill.class,
						"CANNOT_KILL_AGENT", this.aid, e); //$NON-NLS-1$
			}
		}

		@SuppressWarnings("synthetic-access")
		void killOrMarkAsKilled() {
			synchronized (InternalEventBusSkill.this) {
				this.isKilled = true;
				if (InternalEventBusSkill.this.state.get() != OwnerState.NEW) {
					killOwner(InternalEventBusSkill.this);
				}
			}
		}

	}

}
