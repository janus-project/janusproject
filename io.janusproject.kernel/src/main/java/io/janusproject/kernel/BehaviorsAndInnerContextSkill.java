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

import io.janusproject.services.ContextSpaceService;
import io.janusproject.services.LogService;
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

import com.google.common.collect.Queues;
import com.google.common.eventbus.AsyncSyncEventBus;
import com.google.inject.Inject;

/** Janus implementation of SARL's {@link Behaviors} built-in capacity.
 * <p>
 * This implementation uses a holonic vision where behaviors interact via an
 * {@link EventSpaceImpl} allowing them to be agent's as well.
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class BehaviorsAndInnerContextSkill extends Skill implements Behaviors, InnerContextAccess {

	/** Reference to the event bus.
	 */
	private AsyncSyncEventBus eventBus;
	
	private AgentContext innerContext;

	private AtomicBoolean running = new AtomicBoolean(false);
	private AtomicBoolean stopping = new AtomicBoolean(false);
	private final AgentEventListener agentAsEventListener;

	private LogService logger;
	private ContextSpaceService contextService;

	/**
	 * @param agent
	 */
	public BehaviorsAndInnerContextSkill(Agent agent) {
		super(agent);
		this.agentAsEventListener = new AgentEventListener(this);
	}

	/**
	 * @param service - reference to the factory of context provided by the platform.
	 */
	@Inject
	private void setContextService(ContextSpaceService service) {
		this.contextService = service;
	}

	/** Change the event bus inside the agent.
	 * 
	 * @param bus
	 */
	@Inject
	private void setInternalEventBus(AsyncSyncEventBus bus) {
		this.eventBus = bus;
		this.eventBus.register(this.getOwner());
	}

	/** Change the logger that is accessible to the inner behaviors or agents.
	 * 
	 * @param log
	 */
	@Inject
	private void setLogService(LogService log) {
		this.logger = log;
	}
	
	private void ensureInnerContext() {
		if (this.innerContext==null) {
			this.innerContext = this.contextService.createContext(getOwner().getID(), UUID.randomUUID());
			((EventSpaceImpl) this.innerContext.getDefaultSpace()).register(this.agentAsEventListener);
		}
	}

	/** {@inheritDoc}
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
		// TODO: dispose eventBus
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized AgentContext getInnerContext() {
		ensureInnerContext();
		return this.innerContext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Behavior registerBehavior(Behavior attitude) {
		this.eventBus.register(attitude);
		return attitude;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized Behavior unregisterBehavior(Behavior attitude) {
		this.eventBus.unregister(attitude);
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
	 * for platform level event dispatching (i.e. {@link Initialize} and
	 * {@link Destroy})
	 * 
	 * @param event
	 */
	synchronized void selfEvent(Event event) {
		EventSpace defSpace = getSkill(InnerContextAccess.class).getInnerContext().getDefaultSpace();
		event.setSource(defSpace.getAddress(getOwner().getID()));				
		if (event instanceof Initialize) {
			this.eventBus.fire(event);//Immediate synchronous dispatching of Initialize event
			this.running.set(true);
			this.agentAsEventListener.agentInitialized();
		} else if (event instanceof Destroy) {
			this.eventBus.fire(event);//Immediate synchronous dispatching of Destroy event
			this.running.set(false);
			this.stopping.set(true);
		} else {
			internalReceiveEvent(event);//Asynchronous parallel dispatching of this event
		}
		this.logger.debug("SELF_EVENT", event); //$NON-NLS-1$
	}

	@Override
	public EventListener asEventListener() {
		return this.agentAsEventListener;
	}

	
	/** Register the agent from the given default space.
	 * 
	 * @param space
	 */
	void registerOnDefaultSpace(EventSpaceImpl space) {
		space.register(asEventListener());
	}

	/** Unregister the agent from the given default space.
	 * 
	 * @param space
	 */
	void unregisterFromDefaultSpace(EventSpaceImpl space) {
		space.unregister(asEventListener());
	}


	/**
	 * @author $Author: srodriguez$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class AgentEventListener implements EventListener {

		private final Queue<Event> buffer = Queues.newConcurrentLinkedQueue();

		private final WeakReference<BehaviorsAndInnerContextSkill> skill;

		private final WeakReference<UUID> aid;

		@SuppressWarnings("synthetic-access")
		public AgentEventListener(BehaviorsAndInnerContextSkill skill) {
			this.skill = new WeakReference<>(skill);
			this.aid = new WeakReference<>(skill.getOwner().getID());
		}

		@Override
		public UUID getID() {
			return this.aid.get();
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void receiveEvent(Event event) {
			BehaviorsAndInnerContextSkill s = this.skill.get();
			synchronized(s) {
				if (s.running.get() && !s.stopping.get()) {
					s.internalReceiveEvent(event);
				} else if (!s.running.get() && s.stopping.get()) {
					// Dropping messages since agent is dying
					s.logger.warning(BehaviorsAndInnerContextSkill.class,
									"EVENT_DROP_WARNING", event); //$NON-NLS-1$
				} else {
					this.buffer.add(event);
				}
			}
		}

		@SuppressWarnings("synthetic-access")
		void agentInitialized() {
			BehaviorsAndInnerContextSkill s = this.skill.get();
			synchronized(s) {
				for (Event evt : this.buffer) {
					s.internalReceiveEvent(evt);
				}
			}
		}

	}

}
