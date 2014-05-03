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

import io.janusproject.services.LogService;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.core.SpaceSpecification;

import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.arakhne.afc.vmutil.ClassComparator;
import org.arakhne.afc.vmutil.ObjectReferenceComparator;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;
import com.google.inject.Injector;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;

/**
 * A repository of spaces specific to a given context.
 * 
 * @author $Author: ngaud$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class SpaceRepository {
	
	private static final Object[] NO_PARAMETERS = new Object[0]; 

	private final String distributedSpaceSetName;
	private final Injector injector;
	private final LogService logService;

	/** Listener on the events in this repository (basically somewhere in the Context).
	 */
	private final SpaceRepositoryListener externalListener;

	/**
	 * The set of the id of all spaces stored in this repository This set must be distributed and synchronized all over the network
	 */
	private final IMap<SpaceID,Object[]> spaceIDs;

	/**
	 * Map linking a space id to its related Space object This is local non-distributed map
	 */
	private final Map<SpaceID, Space> spaces;

	/**
	 * Map linking a a class of Space specification to its related implementations' ids Use the map <code>spaces</code> to get the Space object associated to a given id This is local non-distributed map
	 */
	private final Multimap<Class<? extends SpaceSpecification<?>>, SpaceID> spacesBySpec;
	
	/** Id of the Hazelcast listener.
	 */
	private String spaceIDListener;

	/**
	 * @param distributedSpaceSetName - the name used to identify distributed map over network
	 * @param hzInstance - factory for creating Hazelcast data structures
	 * @param injector - injector to used for creating new spaces.
	 * @param logService - logging service
	 * @param listener - listener on the events in the space repository.
	 */
	public SpaceRepository(String distributedSpaceSetName, HazelcastInstance hzInstance, Injector injector, LogService logService, SpaceRepositoryListener listener) {
		this.distributedSpaceSetName = distributedSpaceSetName;
		this.injector = injector;
		this.logService = logService;
		this.externalListener = listener;
		this.spaces = new ConcurrentHashMap<>();
		Multimap<Class<? extends SpaceSpecification<?>>, SpaceID> tmp = TreeMultimap.create(ClassComparator.SINGLETON, ObjectReferenceComparator.SINGLETON);
		this.spacesBySpec = Multimaps.synchronizedMultimap(tmp);
		this.spaceIDs = hzInstance.getMap(this.distributedSpaceSetName);
	}
	
	/** Finalize the initialization: ensure that the events are fired outside the scope of the SpaceRepository constructor.
	 */
	synchronized void postConstruction() {
		for (Entry<SpaceID,Object[]> e : this.spaceIDs.entrySet()) {
			ensureSpaceDefinition(e.getKey(), e.getValue());
		}
		this.spaceIDListener = this.spaceIDs.addEntryListener(new HazelcastListener(), true);
	}
	
	/** Destroy this repository and releaqse all the resources.
	 */
	public synchronized void destroy() {
		// Unregister from Hazelcast layer.
		if (this.spaceIDListener!=null) {
			this.spaceIDs.removeEntryListener(this.spaceIDListener);
		}
	}
	
	private synchronized <S extends Space> S createSpaceInstance(Class<? extends SpaceSpecification<S>> spec, SpaceID spaceID, boolean updateHazelcast, Object[] creationParams) {
		S space;
		// Split the call to create() to let the JVM to create the "empty" array for creation parameters.
		if (creationParams!=null) {
			space = this.injector.getInstance(spec).create(spaceID, creationParams);
		}
		else {
			space = this.injector.getInstance(spec).create(spaceID);
		}
		assert(space!=null);
		SpaceID id = space.getID();
		assert(id!=null);
		this.spaces.put(id, space);
		this.spacesBySpec.put(id.getSpaceSpecification(), id);
		if (updateHazelcast) {
			this.spaceIDs.putIfAbsent(id,
					(creationParams!=null && creationParams.length>0) ? creationParams : NO_PARAMETERS);
		}
		fireSpaceAdded(space);
		return space;
	}

	/** Add the existing, but not yet known, spaces into this repository. 
	 * 
	 * @param id - identifier of the space
	 * @param initializationParameters - parameters for initialization.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected synchronized void ensureSpaceDefinition(SpaceID id, Object[] initializationParameters) {
		assert(this.spaceIDs.containsKey(id));
		if (!this.spaces.containsKey(id)) {
			createSpaceInstance((Class)id.getSpaceSpecification(), id, false, initializationParameters);
		}
	}

	/** Remove a remote space. 
	 * 
	 * @param id - identifier of the space
	 */
	protected synchronized void removeSpaceDefinition(SpaceID id) {
		assert(!this.spaceIDs.containsKey(id));
		Space space = this.spaces.remove(id);
		if (space!=null) {
			assert(space.getParticipants().isEmpty());
			this.spacesBySpec.remove(id.getSpaceSpecification(), id);
			fireSpaceRemoved(space);
		}
	}
	
	/** Create a space.
	 * 
	 * @param spaceID - ID of the space.
	 * @param spec - specification of the space.
	 * @param creationParams - creation parameters.
	 * @return the new space.
	 */
	@SuppressWarnings("unchecked")
	public synchronized <S extends io.sarl.lang.core.Space> S createSpace(
			SpaceID spaceID, 
			Class<? extends SpaceSpecification<S>> spec,
			Object... creationParams) {
		S space = (S)this.spaces.get(spaceID);
		if (space==null) {
			space = createSpaceInstance(spec, spaceID, true, creationParams);
		}
		return space;
	}

	/** Retrive the first space of the given specification, or create a space if none.
	 * 
	 * @param spec - specification of the space.
	 * @param spaceID - ID of the space (used only when creating a space).
	 * @param creationParams - creation parameters (used only when creating a space).
	 * @return the new space.
	 */
	@SuppressWarnings("unchecked")
	public synchronized <S extends io.sarl.lang.core.Space> S getOrCreateSpace(
			Class<? extends SpaceSpecification<S>> spec,
			SpaceID spaceID, 
			Object... creationParams) {
		Collection<SpaceID> spaces = this.spacesBySpec.get(spec);
		S firstSpace;
		if (spaces == null || spaces.isEmpty()) {
			firstSpace = createSpaceInstance(spec, spaceID, true, creationParams);
		}
		else {
			firstSpace = (S)this.spaces.get(spaces.iterator().next());
		}
		assert(firstSpace!=null);
		return firstSpace;
	}

	/**
	 * Returns the collection of all spaces stored in this repository
	 * 
	 * @return the collection of all spaces stored in this repository
	 */
	public synchronized Collection<Space> getSpaces() {
		return Collections.unmodifiableCollection(this.spaces.values());
	}

	/**
	 * Returns the first instance of a space with the specified SpaceID.
	 * 
	 * @param spaceID - the identifier to retreive.
	 * @return the space instance of <code>null</code> if none.
	 */
	public synchronized Space getSpace(SpaceID spaceID) {
		return this.spaces.get(spaceID);
	}


	/**
	 * Returns the collection of all spaces with the specified {@link SpaceSpecification} stored in this repository
	 * 
	 * @param spec - the specification used to filter the set of stored spaces
	 * @return the collection of all spaces with the specified {@link SpaceSpecification} stored in this repository
	 */
	@SuppressWarnings("unchecked")
	public synchronized <S extends Space> Collection<S> getSpaces(final Class<? extends SpaceSpecification<S>> spec) {
		return (Collection<S>)Collections2.filter(this.spaces.values(), new Predicate<Space>() {
					@Override
					public boolean apply(Space input) {
						return input.getID().getSpaceSpecification().equals(spec);
					}
				});
	}
	
	/** Notifies the listeners on the space creation.
	 * 
	 * @param space
	 */
	protected void fireSpaceAdded(Space space) {
		if (this.externalListener!=null) {
			this.externalListener.spaceCreated(space);
		}
	}

	/** Notifies the listeners on the space destruction.
	 * 
	 * @param space
	 */
	protected void fireSpaceRemoved(Space space) {
		if (this.externalListener!=null) {
			this.externalListener.spaceCreated(space);
		}
	}

	/** Listener on events related to the space service.
	 *  
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class HazelcastListener implements EntryListener<SpaceID,Object[]> {

		/**
		 */
		public HazelcastListener() {
			//
		}

		/** {@inheritDoc}
		 */
		@Override
		public void entryAdded(EntryEvent<SpaceID, Object[]> event) {
			ensureSpaceDefinition(event.getKey(), event.getValue());
		}

		/** {@inheritDoc}
		 */
		@Override
		public void entryRemoved(EntryEvent<SpaceID, Object[]> event) {
			removeSpaceDefinition(event.getKey());
		}

		/** {@inheritDoc}
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void entryUpdated(EntryEvent<SpaceID, Object[]> event) {
			SpaceRepository.this.logService.warning(
					SpaceRepository.class, "UNSUPPORTED_HAZELCAST_EVENT", event); //$NON-NLS-1$
		}

		/** {@inheritDoc}
		 */
		@Override
		public void entryEvicted(EntryEvent<SpaceID, Object[]> event) {
			removeSpaceDefinition(event.getKey());
		}

	}

	/** Listener on events related to the space service.
	 *  
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static interface SpaceRepositoryListener extends EventListener {

		/** Invoked when the space is added.
		 * 
		 * @param space
		 */
		public void spaceCreated(Space space);

		/** Invoked when the space is destroyed.
		 * 
		 * @param space
		 */
		public void spaceDestroyed(Space space);

	}

}
