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

import io.janusproject.JanusConfig;
import io.janusproject.services.ContextService;
import io.janusproject.services.ContextServiceListener;
import io.janusproject.services.LogService;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.SpaceID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * A repository of Agent's Context.
 * 
 * @author $Author: ngaud$
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
class JanusContextService extends AbstractService implements ContextService {

	private final Collection<ContextServiceListener> listeners;

	/**
	 * Map linking a context id to its associated default space id.
	 * This map must be distributed and synchronized all over the network
	 */
	private IMap<UUID, SpaceID> defaultSpaces;

	/**
	 * Map linking a context id to its related Context object This is local
	 * non-distributed map
	 */
	private Map<UUID,AgentContext> contexts;
	
	private String hazelcastListener = null;
	
	@Inject 
	private LogService logger;
	
	@Inject
	private ContextFactory contextFactory;

	
	/** Constructs <code>ContextRepository</code>.
	 */
	public JanusContextService() {
		this.contexts = new TreeMap<>();
		this.listeners = new ArrayList<>();
	}

	/**
	 * Initialize this service with injected objects
	 * 
	 * @param janusID - injected identifier.
	 * @param hzInstance - Hazelcast instance.
	 */
	@Inject
	private synchronized void initiliaze( 
			@Named(JanusConfig.DEFAULT_CONTEXT_ID) UUID janusID,
			HazelcastInstance hzInstance) {
		this.defaultSpaces = hzInstance.getMap(janusID.toString());
	}


	/** {@inheritDoc}
	 */
	@Override
	public synchronized boolean isEmptyContextRepository() {
		return this.contexts.isEmpty();
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized int getNumberOfContexts() {
		return this.contexts.size();
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized boolean containsContext(UUID contextID) {
		return this.contexts.containsKey(contextID);
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized void addContext(AgentContext context) {
		this.defaultSpaces.put(context.getID(), context.getDefaultSpace().getID());
		this.contexts.put(context.getID(), context);
		fireContextCreated(context);
	}

	/** {@inheritDoc}
	 */
	@Override
	public final void removeContext(AgentContext context) {
		removeContext(context.getID());
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized void removeContext(UUID contextID) {
		this.defaultSpaces.remove(contextID);
		AgentContext context = this.contexts.remove(contextID);
		if (context!=null) {
			((Context)context).destroy();
			fireContextDestroyed(context);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized void removeAllContexts() {
		Map<UUID,AgentContext> old = this.contexts;
		this.contexts = new TreeMap<>();
		this.defaultSpaces.clear();
		for(AgentContext context : old.values()) {
			((Context)context).destroy();
			fireContextDestroyed(context);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized Collection<AgentContext> getContexts() {
		return Collections.synchronizedCollection(this.contexts.values());
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized Set<UUID> getContextIDs() {
		return Collections.synchronizedSet(this.contexts.keySet());
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized AgentContext getContext(UUID contextID) {
		return this.contexts.get(contextID);
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized Collection<AgentContext> getContexts(final Collection<UUID> contextIDs) {
		return Collections2.filter(this.contexts.values(), new Predicate<AgentContext>() {
			@Override
			public boolean apply(AgentContext input) {	
				return contextIDs.contains(input.getID());
			}
		} );
	}

	/** {@inheritDoc}
	 */
	@Override
	public void addContextServiceListener(ContextServiceListener listener) {
		synchronized(this.listeners) {
			this.listeners.add(listener);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public void removeContextServiceListener(ContextServiceListener listener) {
		synchronized(this.listeners) {
			this.listeners.remove(listener);
		}
	}

	/** Notifies the listeners about a context creation.
	 * 
	 * @param context
	 */
	protected void fireContextCreated(AgentContext context) {
		ContextServiceListener[] listeners;
		synchronized(this.listeners) {
			listeners = new ContextServiceListener[this.listeners.size()];
			this.listeners.toArray(listeners);
		}
		for(ContextServiceListener listener : listeners) {
			listener.contextCreated(context);
		}
	}

	/** Notifies the listeners about a context destruction.
	 * 
	 * @param context
	 */
	protected void fireContextDestroyed(AgentContext context) {
		ContextServiceListener[] listeners;
		synchronized(this.listeners) {
			listeners = new ContextServiceListener[this.listeners.size()];
			this.listeners.toArray(listeners);
		}
		for(ContextServiceListener listener : listeners) {
			listener.contextDestroyed(context);
		}
	}
	
	/** Update the internal data structure when a default
	 * space was discovered. 
	 * @param spaceID
	 */
	protected synchronized void ensureDefaultSpaceDefinition(SpaceID spaceID) {
		UUID contextID = spaceID.getContextID();
		if (!this.contexts.containsKey(contextID)) {
			Context context = this.contextFactory.create(contextID, spaceID.getID());
			this.contexts.put(contextID, context);
			fireContextCreated(context);
		}
	}

	/** Update the internal data structure when a default
	 * space was removed. 
	 * @param spaceID
	 */
	protected synchronized void removeDefaultSpaceDefinition(SpaceID spaceID) {
		AgentContext context = this.contexts.remove(spaceID.getContextID());
		if (context!=null) fireContextDestroyed(context);
	}

	/** {@inheritDoc}
	 */
	@Override
	protected synchronized void doStart() {
		for(SpaceID space : this.defaultSpaces.values()) {
			ensureDefaultSpaceDefinition(space);
		}
		this.hazelcastListener = this.defaultSpaces.addEntryListener(new HazelcastListener(), true);
		notifyStarted();
	}

	/** {@inheritDoc}
	 */
	@Override
	protected synchronized void doStop() {
		if (this.hazelcastListener!=null) {
			this.defaultSpaces.removeEntryListener(this.hazelcastListener);
		}
		// Unconnect the default space collection from remote clusters
		// Not needed becasue the Kernel will be stopped: this.defaultSpaces.destroy();
		// Delete the contexts from this repository
		Map<UUID,AgentContext> old = this.contexts;
		this.contexts = new TreeMap<>();
		for(AgentContext context : old.values()) {
			((Context)context).destroy();
			fireContextDestroyed(context);
		}
		notifyStopped();
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class HazelcastListener implements EntryListener<UUID,SpaceID> {

		/**
		 */
		public HazelcastListener() {
			//
		}

		/** {@inheritDoc}
		 */
		@Override
		public void entryAdded(EntryEvent<UUID, SpaceID> event) {
			ensureDefaultSpaceDefinition(event.getValue());
		}

		/** {@inheritDoc}
		 */
		@Override
		public void entryRemoved(EntryEvent<UUID, SpaceID> event) {
			removeDefaultSpaceDefinition(event.getValue());
		}

		/** {@inheritDoc}
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void entryUpdated(EntryEvent<UUID, SpaceID> event) {
			JanusContextService.this.logger.warning(JanusContextService.class, "UNSUPPORTED_HAZELCAST_EVENT", event); //$NON-NLS-1$
		}

		/** {@inheritDoc}
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void entryEvicted(EntryEvent<UUID, SpaceID> event) {
			JanusContextService.this.logger.warning(JanusContextService.class, "UNSUPPORTED_HAZELCAST_EVENT", event); //$NON-NLS-1$
		}
		
	}

}
