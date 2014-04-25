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

import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.core.SpaceSpecification;

import java.util.Collection;
import java.util.Collections;
import java.util.EventListener;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.arakhne.afc.vmutil.ClassComparator;
import org.arakhne.afc.vmutil.ObjectReferenceComparator;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;
import com.google.inject.Injector;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;

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

	private final String distributedSpaceSetName;
	private final Injector injector;

	/** Listener on the events in this repository (basically somewhere in the Context).
	 */
	private final SpaceRepositoryListener externalListener;

	/**
	 * The set of the id of all spaces stored in this repository This set must be distributed and synchronized all over the network
	 */
	private final ISet<SpaceID> spaceIDs;

	/**
	 * Map linking a space id to its related Space object This is local non-distributed map
	 */
	private final Map<SpaceID, Space> spaces;

	/**
	 * Map linking a a class of Space specification to its related implementations' ids Use the map <code>spaces</code> to get the Space object associated to a given id This is local non-distributed map
	 */
	private final Multimap<Class<? extends SpaceSpecification<?>>, SpaceID> spacesBySpec;

	/**
	 * @param distributedSpaceSetName - the name used to identify distributed map over network
	 * @param hzInstance - factory for creating Hazelcast data structures
	 * @param injector - injector to used for creating new spaces.
	 * @param listener - listener on the events in the space repository.
	 */
	public SpaceRepository(String distributedSpaceSetName, HazelcastInstance hzInstance, Injector injector, SpaceRepositoryListener listener) {
		this.distributedSpaceSetName = distributedSpaceSetName;
		this.injector = injector;
		this.externalListener = listener;
		this.spaces = new ConcurrentHashMap<>();
		Multimap<Class<? extends SpaceSpecification<?>>, SpaceID> tmp = TreeMultimap.create(ClassComparator.SINGLETON, ObjectReferenceComparator.SINGLETON);
		this.spacesBySpec = Multimaps.synchronizedMultimap(tmp);
		this.spaceIDs = hzInstance.getSet(this.distributedSpaceSetName);
	}
	
	/** Finalize the initialization: ensure that the events are fired outside the scope of the SpaceRepository constructor.
	 */
	void postConstruction() {
		for (SpaceID id : this.spaceIDs) {
			ensureSpaceDefinition(id);
		}
		this.spaceIDs.addItemListener(new HazelcastListener(), true);
	}
	
	/** Destroy this repository and releaqse all the resources.
	 */
	public synchronized void destroy() {
		// Unconnect the space collection from remote clusters
		//this.spaceIDs.destroy();
	}
	
	private <S extends Space> S createSpaceInstance(Class<? extends SpaceSpecification<S>> spec, SpaceID spaceID, boolean updateSpaceIDs, Object[] creationParams) {
		S space = this.injector.getInstance(spec).create(spaceID, creationParams);
		SpaceID id = space.getID();
		this.spaces.put(id, space);
		this.spacesBySpec.put(id.getSpaceSpecification(), id);
		if (updateSpaceIDs) this.spaceIDs.add(id);
		fireSpaceAdded(space);
		return space;
	}

	/** Add the existing, but not yet known, spaces into this repository. 
	 * 
	 * @param id - identifier of the space
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected synchronized void ensureSpaceDefinition(SpaceID id) {
		assert(this.spaceIDs.contains(id));
		if (!this.spaces.containsKey(id)) {
			//FIXME manage the propagation of the creationParams inside the ID of the space
			createSpaceInstance((Class)id.getSpaceSpecification(), id, false, new Object[0]);
		}
	}

	/** Remove a remote space. 
	 * 
	 * @param id - identifier of the space
	 */
	protected synchronized void removeSpaceDefinition(SpaceID id) {
		assert(!this.spaceIDs.contains(id));
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
	private class HazelcastListener implements ItemListener<SpaceID> {

		/**
		 */
		public HazelcastListener() {
			
		}

		/** {@inheritDoc}
		 */
		@Override
		public void itemAdded(ItemEvent<SpaceID> item) {
			ensureSpaceDefinition(item.getItem());
		}

		/** {@inheritDoc}
		 */
		@Override
		public void itemRemoved(ItemEvent<SpaceID> item) {
			removeSpaceDefinition(item.getItem());
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
