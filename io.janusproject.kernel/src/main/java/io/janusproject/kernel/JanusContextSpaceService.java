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
import io.janusproject.services.ContextRepositoryListener;
import io.janusproject.services.ContextSpaceService;
import io.janusproject.services.KernelDiscoveryService;
import io.janusproject.services.LogService;
import io.janusproject.services.NetworkService;
import io.janusproject.services.SpaceRepositoryListener;
import io.janusproject.services.impl.AbstractDependentService;
import io.janusproject.util.ListenerCollection;
import io.janusproject.util.TwoStepConstruction;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.util.Collections3;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.util.concurrent.Service;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * A repository of Agent's context and spaces.
 * 
 * @author $Author: ngaud$
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
@TwoStepConstruction
class JanusContextSpaceService extends AbstractDependentService implements ContextSpaceService {

	private final ListenerCollection<?> listeners = new ListenerCollection<>();

	/** Factory of contexts.
	 */
	private ContextFactory contextFactory;
	
	/** Factory of space repositories.
	 */
	private SpaceRepositoryFactory spaceRepositoryFactory;

	/**
	 * Map linking a context id to its associated default space id.
	 * This map must be distributed and synchronized all over the network
	 */
	private IMap<UUID, SpaceID> defaultSpaces;

	/**
	 * Map linking a context id to its related Context object This is local
	 * non-distributed map
	 */
	private Map<UUID,AgentContext> contexts = new TreeMap<>();
	
	private String hazelcastListener = null;
	
	private LogService logger;
	
	/** Constructs <code>ContextRepository</code>.
	 */
	public JanusContextSpaceService() {
		//
	}
	
	@Override
	public final Class<? extends Service> getServiceType() {
		return ContextSpaceService.class;
	}

	@Override
	public Collection<java.lang.Class<? extends Service>> getStartingDependencies() {
		return Arrays.<Class<? extends Service>>asList(NetworkService.class, KernelDiscoveryService.class);
	}
	
	@Override
	public Collection<java.lang.Class<? extends Service>> getStoppingDependencies() {
		return Arrays.<Class<? extends Service>>asList(NetworkService.class, KernelDiscoveryService.class);
	}

	/** Replies the factory used to create contexts.
	 * 
	 * @return the context factory.
	 */
	ContextFactory getContextFactory() {
		return this.contextFactory;
	}

	/** Change the factory used to create contexts.
	 * 
	 * @param factory - the context factory.
	 */
	void setContextFactory(ContextFactory factory) {
		if (factory!=null) this.contextFactory = factory;
	}

	/** Replies the factory used to create space repositories.
	 * 
	 * @return the context factory.
	 */
	SpaceRepositoryFactory getSpaceRepositoryFactory() {
		return this.spaceRepositoryFactory;
	}

	/** Change the factory used to create space repositories.
	 * 
	 * @param factory - the space repository factory.
	 */
	void setSpaceRepositoryFactory(SpaceRepositoryFactory factory) {
		if (factory!=null) this.spaceRepositoryFactory = factory;
	}

	/** {@inheritDoc}
	 */
	@Override
	public final Object mutex() {
		return this;
	}

	/**
	 * Initialize this service with injected objects
	 * 
	 * @param janusID - injected identifier.
	 * @param hzInstance - Hazelcast instance.
	 * @param logService - service of logging.
	 * @param injector - the injector to use.
	 */
	@Inject
	synchronized void postConstruction( 
			@Named(JanusConfig.DEFAULT_CONTEXT_ID_NAME) UUID janusID,
			HazelcastInstance hzInstance,
			LogService logService,
			Injector injector) {
		this.logger = logService;
		this.contextFactory = new DefaultContextFactory();
		this.spaceRepositoryFactory = new Context.DefaultSpaceRepositoryFactory(injector, hzInstance, logService);
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
	public synchronized AgentContext createContext(UUID contextID, UUID defaultSpaceUUID) {
		AgentContext context = this.contexts.get(contextID); 
		if (context==null) {
			Context ctx = this.contextFactory.newInstance(
					contextID,
					defaultSpaceUUID,
					this.spaceRepositoryFactory,
					new SpaceEventProxy());
			this.contexts.put(contextID, ctx);
			fireContextCreated(ctx);
			Space defaultSpace = ctx.postConstruction();
			this.defaultSpaces.putIfAbsent(ctx.getID(), defaultSpace.getID());
			return ctx;
		}
		return context;
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
	public synchronized Collection<AgentContext> getContexts() {
		return Collections.unmodifiableCollection(Collections3.synchronizedCollection(this.contexts.values(), mutex()));
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized Set<UUID> getContextIDs() {
		return Collections.unmodifiableSet(Collections3.synchronizedSet(this.contexts.keySet(), mutex()));
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
	public void addContextRepositoryListener(ContextRepositoryListener listener) {
		this.listeners.add(ContextRepositoryListener.class, listener);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void removeContextRepositoryListener(ContextRepositoryListener listener) {
		this.listeners.remove(ContextRepositoryListener.class, listener);
	}

	/** Notifies the listeners about a context creation.
	 * 
	 * @param context
	 */
	protected void fireContextCreated(AgentContext context) {
		ContextRepositoryListener[] ilisteners = this.listeners.getListeners(ContextRepositoryListener.class);
		this.logger.info(JanusContextSpaceService.class, "CONTEXT_CREATED", context.getID()); //$NON-NLS-1$
		for(ContextRepositoryListener listener : ilisteners) {
			listener.contextCreated(context);
		}
	}

	/** Notifies the listeners about a context destruction.
	 * 
	 * @param context
	 */
	protected void fireContextDestroyed(AgentContext context) {
		ContextRepositoryListener[] ilisteners = this.listeners.getListeners(ContextRepositoryListener.class);
		this.logger.info(JanusContextSpaceService.class, "CONTEXT_DESTROYED", context.getID()); //$NON-NLS-1$
		for(ContextRepositoryListener listener : ilisteners) {
			listener.contextDestroyed(context);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public void addSpaceRepositoryListener(SpaceRepositoryListener listener) {
		this.listeners.add(SpaceRepositoryListener.class, listener);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void removeSpaceRepositoryListener(SpaceRepositoryListener listener) {
		this.listeners.remove(SpaceRepositoryListener.class, listener);
	}
	
	/** Notifies the listeners on the space creation.
	 * 
	 * @param space
	 * @param isLocalCreation
	 */
	protected void fireSpaceCreated(Space space, boolean isLocalCreation) {
		for(SpaceRepositoryListener listener : this.listeners.getListeners(SpaceRepositoryListener.class)) {
			listener.spaceCreated(space, isLocalCreation);
		}
	}

	/** Notifies the listeners on the space destruction.
	 * 
	 * @param space
	 * @param isLocalDestruction
	 */
	protected void fireSpaceDestroyed(Space space, boolean isLocalDestruction) {
		for(SpaceRepositoryListener listener : this.listeners.getListeners(SpaceRepositoryListener.class)) {
			listener.spaceDestroyed(space, isLocalDestruction);
		}
	}
	
	/** Update the internal data structure when a default
	 * space was discovered. 
	 * @param spaceID
	 */
	protected synchronized void ensureDefaultSpaceDefinition(SpaceID spaceID) {
		UUID contextID = spaceID.getContextID();
		createContext(contextID, spaceID.getID());
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
			JanusContextSpaceService.this.logger.warning(JanusContextSpaceService.class, "UNSUPPORTED_HAZELCAST_EVENT", event); //$NON-NLS-1$
		}

		/** {@inheritDoc}
		 */
		@Override
		public void entryEvicted(EntryEvent<UUID, SpaceID> event) {
			removeDefaultSpaceDefinition(event.getValue());
		}
		
	}
	
	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class SpaceEventProxy implements SpaceRepositoryListener {
		
		/**
		 */
		public SpaceEventProxy() {
			//
		}

		/** {@inheritDoc}
		 */
		@Override
		public void spaceCreated(Space space, boolean isLocalCreation) {
			fireSpaceCreated(space, isLocalCreation);
		}

		/** {@inheritDoc}
		 */
		@Override
		public void spaceDestroyed(Space space, boolean isLocalDestruction) {
			fireSpaceDestroyed(space, isLocalDestruction);
		}
		
	}
	
	/** Factory of contexts.
	 * 
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class DefaultContextFactory implements ContextFactory {
		
		/** Create an instance of context.
		 */
		public DefaultContextFactory() {
			//
		}

		@Override
		public Context newInstance(
				UUID contextId,
				UUID defaultSpaceId,
				SpaceRepositoryFactory factory,
				SpaceRepositoryListener listener) {
			return new Context(
					contextId,
					defaultSpaceId,
					factory,
					listener);
		}
		
	}

}
