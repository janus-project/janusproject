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
package io.janusproject2.kernel;

import io.janusproject2.JanusConfig;
import io.janusproject2.repository.DistributedDataStructureFactory;
import io.janusproject2.services.ContextService;
import io.janusproject2.services.ContextServiceListener;
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
import com.google.inject.Injector;
import com.google.inject.name.Named;

/**
 * A repository of Agent's Context.
 * 
 * @author $Author: ngaud$
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class ContextRepository_ extends AbstractService implements ContextService {

	private Injector injector;

	private final Collection<ContextServiceListener> listeners;

	/**
	 * Map linking a context id to its associated default space id.
	 * This map must be distributed and synchronized all over the network
	 */
	private Map<UUID, SpaceID> defaultSpaces;

	/**
	 * Map linking a context id to its related Context object This is local
	 * non-distributed map
	 */
	private final Map<UUID, AgentContext> contexts;

	/** Constructs <code>ContextRepository</code>.
	 */
	public ContextRepository_() {
		this.contexts = new TreeMap<>();
		this.listeners = new ArrayList<>();
	}

	/**
	 * Change the identifier of the Janus context.
	 * 
	 * @param injector - the injector.
	 * @param janusID - injected identifier.
	 * @param repositoryImplFactory - factory that permits to create a repository.
	 */
	@Inject
	synchronized void setJanusID(Injector injector, 
			@Named(JanusConfig.DEFAULT_CONTEXT_ID) UUID janusID,
			DistributedDataStructureFactory repositoryImplFactory) {
		this.injector = injector;
		this.defaultSpaces = repositoryImplFactory.getMap(janusID.toString());
		/*Context ctx;
		for(IMap.Entry<UUID, SpaceID> entry: defaultSpaces.entrySet()) {
			if(!this.contexts.containsKey(entry.getKey())) {
				ctx = new Context(this.injector, entry.getKey(), entry.getValue().getID());
				assert(ctx != null);
				this.contexts.put(entry.getKey(), ctx);
			}
		}
		this.spacesEntryListener = new EntryListener<UUID, SpaceID>() {

			@Override
			public void entryAdded(EntryEvent<UUID, SpaceID> event) {
				Context ctx = new Context(ContextRepository.this.injector,event.getKey(), event.getValue().getID());
				assert(ctx != null);
				ContextRepository.this.contexts.put(event.getKey(), ctx);				
			}

			@Override
			public void entryRemoved(EntryEvent<UUID, SpaceID> event) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void entryUpdated(EntryEvent<UUID, SpaceID> event) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void entryEvicted(EntryEvent<UUID, SpaceID> event) {
				// TODO Auto-generated method stub
				
			}			
		};
		this.defaultSpaces.addEntryListener(this.spacesEntryListener, true);*/
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
		if (context!=null) fireContextDestroyed(context);
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized void removeAllContexts() {
		this.defaultSpaces.clear();
		this.contexts.clear();
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

	/** {@inheritDoc}
	 */
	@Override
	protected void doStart() {
		notifyStarted();
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void doStop() {
		notifyStopped();
	}

}
