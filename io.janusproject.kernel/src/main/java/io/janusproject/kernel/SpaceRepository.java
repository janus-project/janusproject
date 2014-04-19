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

import io.janusproject.repository.impl.DistributedDataStructureFactory;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.core.SpaceSpecification;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.arakhne.afc.vmutil.ClassComparator;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;
import com.google.inject.Inject;
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

	/**
	 * The set of the id of all spaces stored in this repository This set must be distributed and synchronized all over the network
	 */
	private ISet<SpaceID> spaceIDs;

	/**
	 * Map linking a space id to its related Space object This is local non-distributed map
	 */
	private final Map<SpaceID, Space> spaces;

	/**
	 * Map linking a a class of Space specification to its related implementations' ids Use the map <code>spaces</code> to get the Space object associated to a given id This is local non-distributed map
	 */
	private final Multimap<Class<? extends SpaceSpecification>, SpaceID> spacesBySpec;

	private final String distributedSpaceSetName;

	/**
	 * The listener on the modifications occurring in the {@link spaceIDs} distributed map
	 */
	private ItemListener<SpaceID> spaceIDsEntryListener;

	private final WeakReference<AgentContext> agentContext;
	
	/**
	 * 
	 * @param distributedSpaceSetName - the name used to identify distributed map over network
	 * @param agentContext - is the owner of this space repository.
	 */
	public SpaceRepository(String distributedSpaceSetName, AgentContext agentContext) {
		this.distributedSpaceSetName = distributedSpaceSetName;
		this.agentContext = new WeakReference<>(agentContext);
		this.spaces = new ConcurrentHashMap<>();
		Multimap<Class<? extends SpaceSpecification>, SpaceID> tmp = TreeMultimap.create(ClassComparator.SINGLETON, new ObjectReferenceComparator<SpaceID>());
		this.spacesBySpec = Multimaps.synchronizedMultimap(tmp);
	}
	
	/** Replies the context associated to this space repository.
	 * 
	 * @return the agent context.
	 */
	protected AgentContext getContext() {
		return this.agentContext.get();
	}

	/**
	 * Change the repository factory used by this space repository.
	 * 
	 * @param repositoryImplFactory
	 */
	@Inject
	synchronized void setRespositoryImplFactory(DistributedDataStructureFactory repositoryImplFactory) {
		this.spaceIDs = repositoryImplFactory.getSet(this.distributedSpaceSetName);
		for (SpaceID id : this.spaceIDs) {
			addExistingSpace(id);
		}
		this.spaceIDsEntryListener = new ItemListener<SpaceID>() {

			@Override
			public void itemAdded(ItemEvent<SpaceID> item) {
				addExistingSpace(item.getItem());
			}

			@Override
			public void itemRemoved(ItemEvent<SpaceID> item) {
				// TODO Auto-generated method stub

			}

		};
		this.spaceIDs.addItemListener(this.spaceIDsEntryListener, true);
	}

	/** Add the existing, but not yet known, spaces into this repository. 
	 * 
	 * @param id - identifier of the space
	 */
	protected void addExistingSpace(SpaceID id) {
		if (!this.spaces.containsKey(id)) {
			//FIXME manage the propagation of the creationParams inside the ID of the space
			Space ispace = getContext().createSpace(id.getSpaceSpecification(), id.getID());
			assert (ispace != null);
			this.spaces.put(id, ispace);
		}
		if (!this.spacesBySpec.containsKey(id.getSpaceSpecification())) {
			this.spacesBySpec.put(id.getSpaceSpecification(), id);
		}
	}

	/**
	 * Add a new space to this repository
	 * 
	 * @param space - the space to add
	 */
	synchronized void addSpace(Space space) {
		SpaceID id = space.getID();
		this.spaceIDs.add(id);
		this.spaces.put(id, space);
		this.spacesBySpec.put(id.getSpaceSpecification(), id);
	}

	/**
	 * Remove the specified space from this repository
	 * 
	 * @param space - the space to remove
	 */
	synchronized void removeSpace(Space space) {
		SpaceID id = space.getID();
		this.spaces.remove(id);
		this.spacesBySpec.remove(id.getSpaceSpecification(), id);
		this.spaceIDs.remove(space);
	}

	/**
	 * Remove the specified space from this repository
	 * 
	 * @param spaceID - the space of the space to remove
	 */
	public synchronized void removeSpace(SpaceID spaceID) {
		this.spaceIDs.remove(this.spaces.get(spaceID));
		this.spaces.remove(spaceID);
		this.spacesBySpec.remove(spaceID.getSpaceSpecification(), spaceID);
	}

	/**
	 * Clear the context of this repository
	 */
	public synchronized void clearRepository() {
		this.spaceIDs.clear();
		this.spaces.clear();
		this.spacesBySpec.clear();
	}

	/**
	 * Returns the number of space registered in this repository
	 * 
	 * @return the number of space registered in this repository
	 */
	public synchronized int numberOfRegisteredSpaces() {
		return this.spaceIDs.size();
	}

	/**
	 * Does this repository contain some space
	 * 
	 * @return true if this repository contains no space, false otherwise
	 */
	public synchronized boolean isEmpty() {
		return this.spaceIDs.isEmpty();
	}

	/**
	 * Checks if this repository contains a space with the specified ID
	 * 
	 * @param spaceid - the space's ID to test
	 * @return true if this repository contains a space with the specified ID, false otherwise
	 */
	public synchronized boolean containsSpace(SpaceID spaceid) {
		return this.spaceIDs.contains(spaceid);
	}

	/**
	 * Returns the set of all space's IDs stored in this repository
	 * 
	 * @return the set of all space's IDs stored in this repository
	 */
	public Set<SpaceID> getSpaceIDs() {
		return Collections.synchronizedSet(this.spaces.keySet());
	}

	/**
	 * Returns the collection of all spaces stored in this repository
	 * 
	 * @return the collection of all spaces stored in this repository
	 */
	public Collection<Space> getSpaces() {
		return Collections.synchronizedCollection(this.spaces.values());
	}

	/**
	 * Returns the collection of all spaces with the specified {@link SpaceSpecification} stored in this repository
	 * 
	 * @param spec - the specification used to filter the set of stored spaces
	 * @return the collection of all spaces with the specified {@link SpaceSpecification} stored in this repository
	 */
	public Collection<Space> getSpacesFromSpec(final Class<? extends SpaceSpecification> spec) {
		return Collections2.filter(
				Collections.synchronizedCollection(this.spaces.values()), new Predicate<Space>() {
			@Override
			public boolean apply(Space input) {
				return input.getID().getSpaceSpecification().equals(spec);
			}
		});

	}

	/**
	 * Returns the first instance of a space with the specified {@link SpaceSpecification} stored in this repository.
	 * 
	 * @param spec - the specification used to filter the set of stored spaces
	 * @return the space instance of <code>null</code> if none.
	 */
	public synchronized Space getFirstSpaceFromSpec(Class<? extends SpaceSpecification> spec) {
		Collection<SpaceID> spaces = this.spacesBySpec.get(spec);
		if (spaces == null || spaces.isEmpty())
			return null;
		return this.spaces.get(spaces.iterator().next());
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
	 * Provides support for object's comparisons using hashCode.
	 * 
	 * @param <T> - the type of object to compare
	 * @author $Author: ngaud$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class ObjectReferenceComparator<T> implements Comparator<T> {

		/**
		 */
		public ObjectReferenceComparator() {
			//
		}

		@Override
		public int compare(T o1, T o2) {
			return System.identityHashCode(o2) - System.identityHashCode(o1);
		}

	}

}
