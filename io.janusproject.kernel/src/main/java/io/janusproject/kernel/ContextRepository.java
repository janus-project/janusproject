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
import io.janusproject.repository.impl.DistributedDataStructureFactory;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.SpaceID;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.hazelcast.core.EntryListener;
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
public class ContextRepository {

	private Injector injector;
		
	/**
	 * Map linking a context id to its related Context object This is local
	 * non-distributed map
	 */
	private final Map<UUID, AgentContext> contexts;

	/**
	 * Map linking a context id to its associated default space id This map must
	 * be distributed and synchronized all over the network
	 */
	private IMap<UUID, SpaceID> defaultSpaces;

	/**
	 * The listener on the modifications occurring in the {@link spaces} distributed map
	 */
	private EntryListener<UUID, SpaceID> spacesEntryListener;

	/** Constructs <code>ContextRepository</code>.
	 */
	public ContextRepository() {
		this.contexts = new ConcurrentHashMap<>();
		
	}
	
	/**
	 * Change the identifier of the Janus context.
	 * 
	 * @param janusID - injected identifier
	 * @param repositoryImplFactory - factory that permits to create a repository.
	 */
	@Inject
	void setJanusID(Injector injector, @Named(JanusConfig.DEFAULT_CONTEXT_ID) UUID janusID, DistributedDataStructureFactory repositoryImplFactory){
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

	/**
	 * Does this repository contain some context
	 * 
	 * @return true if this repository contains no context, false otherwise
	 */
	public boolean isEmpty() {
		return this.contexts.isEmpty();
	}

	/**
	 * Returns the number of context registered in this repository
	 * 
	 * @return the number of context registered in this repository
	 */
	public int numberOfRegisteredContexts() {
		return this.contexts.size();
	}

	/**
	 * Check if this repository contains a context with the specified id
	 * 
	 * @param contextID
	 *            - the id to test
	 * @return true if this repository contains a context with the specified id,
	 *         false otherwise
	 */
	public boolean containsContext(UUID contextID) {
		return this.contexts.containsKey(contextID);
	}

	/**
	 * Add a new context to this repository as well as its default context to
	 * the list of related spaces
	 * 
	 * @param context
	 *            - the context to add
	 */
	public void addContext(AgentContext context) {
		this.defaultSpaces.put(context.getID(), context.getDefaultSpace().getID());
		this.contexts.put(context.getID(), context);
	}

	/**
	 * Remove the specified context from this repository
	 * 
	 * @param context
	 *            - the context to remove
	 */
	public void removeContext(AgentContext context) {
		this.removeContext(context.getID());
	}

	/**
	 * Remove the context with the specified id from this repository
	 * 
	 * @param contextID
	 *            - the id of the context to remove
	 */
	public void removeContext(UUID contextID) {
		this.defaultSpaces.remove(contextID);
		this.contexts.remove(contextID);
	}

	/**
	 * Clear the context of this repository
	 */
	public void clearRepository() {
		this.defaultSpaces.clear();
		this.contexts.clear();
	}

	/**
	 * Returns the collection of all agent's contexts stored in this repository
	 * 
	 * @return the collection of all agent's contexts stored in this repository
	 */
	public Collection<AgentContext> getContexts() {
		return this.contexts.values();
	}

	/**
	 * Returns the set of all agent context IDs stored in this repository
	 * 
	 * @return the set of all agent context IDs stored in this repository
	 */
	public Set<UUID> getContextIDs() {
		return this.contexts.keySet();
	}

	/**
	 * Returns the {@link AgentContext} with the given ID
	 * 
	 * @param contextID
	 * @return the {@link AgentContext} with the given ID
	 */
	public AgentContext getContext(UUID contextID) {
		return this.contexts.get(contextID);
	}
	
	/**
	 * Returns the collection of {@link AgentContext} with the given IDs
	 * @param contextIDs
	 * @return the collection of {@link AgentContext} with the given IDs
	 */
	public Collection<AgentContext> getContexts(final Collection<UUID> contextIDs){
		return Collections2.filter(this.contexts.values(),new Predicate<AgentContext>() {
			@Override
			public boolean apply(AgentContext input) {	
				return contextIDs.contains(input.getID());
			}
		} );
		
	}

}
