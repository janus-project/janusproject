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

import io.sarl.core.Behaviors;
import io.sarl.core.DefaultContextInteractions;
import io.sarl.core.Destroy;
import io.sarl.core.Initialize;
import io.sarl.core.InnerContextAccess;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Behavior;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.Skill;

import java.lang.ref.WeakReference;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.collect.Queues;
import com.google.common.eventbus.AsyncEventBus;
import com.google.inject.Inject;

/**
 * Janus implementation of SARL's {@link Behaviors} built-in capacity
 * 
 * This implementation uses a holonic vision where behaviors interact via an
 * {@link EventSpaceImpl} allowing them to be agent's as well.
 * 
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class BehaviorsAndInnerContextSkill extends Skill implements Behaviors, InnerContextAccess {

	protected AsyncEventBus eventBus;
	private AgentContext innerContext;

	private AtomicBoolean running = new AtomicBoolean(false);
	private AtomicBoolean stoping = new AtomicBoolean(false);
	private final AgentEventListener agentAsEventListener;

	private Logger log;

	/**
	 * @param agent
	 */
	public BehaviorsAndInnerContextSkill(Agent agent) {
		super(agent);
		this.agentAsEventListener = new AgentEventListener(this);
	}

	@Inject
	void createInternalContext(ContextFactory factory) {
		this.innerContext = factory.create(getOwner().getID(), UUID.randomUUID());
		((EventSpaceImpl) this.innerContext.getDefaultSpace()).register(this.agentAsEventListener);
	}

	@Inject
	void setInternalEventBus(AsyncEventBus bus) {
		this.eventBus = bus;
		this.eventBus.register(this.getOwner());
		if (this.log != null)
			this.log.severe("[" + this.getOwner().getID() + "] Agent registered in internal Event Bus");
	}

	@Inject
	void setLogger(Logger log) {
		this.log = log;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void install() {
		super.install();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void uninstall() {
		super.uninstall();
		// TODO dispose eventBus
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public AgentContext getInnerContext() {
		return this.innerContext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Behavior registerBehavior(Behavior attitude) {
		this.eventBus.register(attitude);
		return attitude;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Behavior unregisterBehavior(Behavior attitude) {
		this.eventBus.unregister(attitude);
		return attitude;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void wake(Event evt) {
		// Use the inner space so all behaviors (even agents inside the holon
		// running in distant kernels) are notified. The event will return into
		// the agent via the inner default space add call internalReceiveEvent
		// for
		// real posting
		EventSpace defSpace = getSkill(InnerContextAccess.class).getInnerContext().getDefaultSpace();
		evt.setSource(defSpace.getAddress(getOwner().getID()));
		defSpace.emit(evt);
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
	private void internalReceiveEvent(Event event) {
		this.eventBus.post(event);
	}

	/**
	 * Sends an event to itself using its defaultInnerAddress as source. Used
	 * for platform levet event dispatching (i.e. {@link Initialize} and
	 * {@link Destroy})
	 * 
	 * @param event
	 */
	void selfEvent(Event event) {
		EventSpace defSpace = getSkill(InnerContextAccess.class).getInnerContext().getDefaultSpace();
		event.setSource(defSpace.getAddress(getOwner().getID()));
		internalReceiveEvent(event);
		this.log.finer("selfEvent: " + event);
		if (event instanceof Initialize) {
			this.running.set(true);
			this.agentAsEventListener.agentInitialized();
		} else if (event instanceof Destroy) {
			this.running.set(false);
			this.stoping.set(true);
		}
	}

	private EventListener agentAsEventListener() {
		return this.agentAsEventListener;
	}

	void registerOnDefaultSpace(EventSpaceImpl space) {
		space.register(agentAsEventListener());
	}

	/**
	 * @param defaultSpace
	 */
	void unregisterFromDefaultSpace(EventSpaceImpl space) {
		space.unregister(agentAsEventListener());
	}



	private static class AgentEventListener implements EventListener {

		private Queue<Event> buffer = Queues.newConcurrentLinkedQueue();

		private WeakReference<BehaviorsAndInnerContextSkill> skill;

		private WeakReference<UUID> aid;

		public AgentEventListener(BehaviorsAndInnerContextSkill skill) {
			this.skill = new WeakReference<BehaviorsAndInnerContextSkill>(skill);
			this.aid = new WeakReference<UUID>(skill.getOwner().getID());
		}

		@Override
		public UUID getID() {
			return this.aid.get();
		}

		@Override
		public void receiveEvent(Event event) {
			if (this.skill.get().running.get() && !this.skill.get().stoping.get()) {

				this.skill.get().internalReceiveEvent(event);
			} else if (!this.skill.get().running.get() && this.skill.get().stoping.get()) {
				// Dropping messages since agent is dying
				this.skill.get().log.log(Level.WARNING, "Dropping event since agent is dying: " + event.toString());
			} else {
				this.buffer.add(event);
			}
		}

		private void agentInitialized() {
			for (Event evt : this.buffer) {
				this.skill.get().internalReceiveEvent(evt);
			}
		}

	}

}
