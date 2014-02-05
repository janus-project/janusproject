package io.janusproject.repository;

import io.janusproject.repository.impl.RepositoryImplFactory;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.core.SpaceSpecification;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;
import com.google.inject.Inject;



/**
 * A repository of spaces specific to a given context
 * 
 * @author $Author: ngaud$
 *
 */
public class SpaceRepository {
	
	/**
	 * The set of the id of all spaces stored in this repository
	 * This set must be distributed and synchronized all over the network
	 */
	private Set<SpaceID> spaceIDs;
	
	/**
	 * Map linking a space id to its related Space object
	 * This is local non-distributed map
	 */
	private final Map<SpaceID, Space> spaces;
	
	/**
	 * Map linking a a class of Space specification to its related implementations' ids
	 * Use the map <code>spaces</code> to get the Space object associated to a given id
	 * This is local non-distributed map
	 */
	private final Multimap<Class<? extends SpaceSpecification>, SpaceID> spacesBySpec;
	
	
	private RepositoryImplFactory repositoryImplFactory;
	
	private final String distributedSpaceSetName;
	
	/**
	 * 
	 * @param distributedSpaceSetName - the name used to identify distributed map over network
	 */
	public SpaceRepository(String distributedSpaceSetName) {		
		this.distributedSpaceSetName = distributedSpaceSetName;
		this.spaces = new ConcurrentHashMap<SpaceID, Space>();	
		Multimap<Class<? extends SpaceSpecification>, SpaceID> tmp = TreeMultimap.create(new ClassComparator(), new ObjectReferenceComparator<SpaceID>());
		this.spacesBySpec = Multimaps.synchronizedMultimap(tmp) ;		
	}
	
	@Inject
	void setRespositoryImplFactory(RepositoryImplFactory repositoryImplFactory){
		this.spaceIDs = repositoryImplFactory.getSet(this.distributedSpaceSetName);
	}
	/**
	 * Add a new space to this repository
	 * @param space - the space to add
	 */
	public void addSpace(Space space) {
		SpaceID id = space.getID();
		this.spaceIDs.add(id);
		this.spaces.put(id, space);
		this.spacesBySpec.put(id.getSpaceSpecification(), id);		
	}	
	
	/**
	 * Remove the specified space from this repository
	 * @param space - the space to remove
	 */
	public void removeSpace(Space space) {
		SpaceID id = space.getID();
		this.spaces.remove(id);
		this.spacesBySpec.remove(id.getSpaceSpecification(), id);
		this.spaceIDs.remove(space);
	}
	
	/**
	 * Remove the specified space from this repository
	 * @param spaceID - the space of the space to remove
	 */
	public void removeSpace(SpaceID spaceID) {	
		this.spaceIDs.remove(this.spaces.get(spaceID));
		this.spaces.remove(spaceID);
		this.spacesBySpec.remove(spaceID.getSpaceSpecification(), spaceID);		
	}
	
	/**
	 * Clear the context of this repository
	 */
	public void clearRepository() {
		this.spaceIDs.clear();
		this.spaces.clear();
		this.spacesBySpec.clear();
	}
	
	/**
	 * Returns the number of space registered in this repository
	 * @return the number of space registered in this repository
	 */
	public int numberOfRegisteredSpaces() {
		return this.spaceIDs.size();
	}

	/**
	 * Does this repository contain some space
	 * @return true if this repository contains no space, false otherwise
	 */
	public boolean isEmpty() {
		return this.spaceIDs.isEmpty();
	}

	/**
	 * Checks if this repository contains a space with the specified ID
	 * @param spaceid - the space's ID to test
	 * @return true if this repository contains a space with the specified ID, false otherwise
	 */
	public boolean containsSpace(SpaceID spaceid) {
		return this.spaceIDs.contains(spaceid);
	}

	/**
	 * Returns an iterator over the various space's IDs stored in this repository
	 * @return an iterator over the various space's IDs stored in this repository
	 */
	public Iterator<SpaceID> getSpaceIDIterator() {
		return this.spaceIDs.iterator();
	}
	
	/**
	 * Returns an iterator over the various space's  stored in this repository
	 * @return an iterator over the various space's stored in this repository
	 */
	public Iterator<Space> getSpaceIterator() {
		return this.spaces.values().iterator();
	}
	
	/**
	 * Returns the set of all space's IDs stored in this repository
	 * @return the set of all space's IDs stored in this repository
	 */
	public Set<SpaceID> getSpaceIDs() {
		return this.spaces.keySet();
	}

	/**
	 * Returns the collection of all spaces stored in this repository
	 * @return the collection of all spaces stored in this repository
	 */
	public Collection<Space> getSpaces() {
		return this.spaces.values();
	}

	/**
	 * Returns the collection of all spaces IDs with the specified {@link SpaceSpecification} stored in this repository
	 * @param spec - the specification used to filter the set of stored spaces
	 * @return the collection of all spaces IDs with the specified {@link SpaceSpecification} stored in this repository
	 */
	public Collection<SpaceID> getSpaceIDsFromSpec(Class<? extends SpaceSpecification> spec) {
		return this.spacesBySpec.get(spec);
	}

	/**
	 * Returns the collection of all spaces with the specified {@link SpaceSpecification} stored in this repository
	 * @param spec - the specification used to filter the set of stored spaces
	 * @return the collection of all spaces with the specified {@link SpaceSpecification} stored in this repository
	 */
	public Collection<Space> getSpacesFromSpec(final Class<? extends SpaceSpecification> spec) {
		return Collections2.filter(this.spaces.values(), new Predicate<Space>() {
			@Override
			public boolean apply(Space input) {	
				return input.getID().getSpaceSpecification().equals(spec);
			}
		});
		
	}
	/**
	 * Returns the collection of all spaces with the specified {@link SpaceSpecification} stored in this repository
	 * @param spec - the specification used to filter the set of stored spaces
	 * @return the collection of all spaces with the specified {@link SpaceSpecification} stored in this repository
	 */
	public Space getFirstSpaceFromSpec(Class<? extends SpaceSpecification> spec) {
		return this.getSpaceIterator().next();
	}

		
	
	/**
	 * Provides support for class comparisons based on class's canonical name
	 * @author $Author: ngaud$
	 *
	 */
	private static class ClassComparator implements Comparator<Class<?>> {

		/**
		 */
		public ClassComparator() {
			//
		}

		/**
		 * {@inheritDoc}
		 */
		public int compare(Class<?> o1, Class<?> o2) {
			if (o1==o2) return 0;
			if (o1==null) return Integer.MIN_VALUE;
			if (o2==null) return Integer.MAX_VALUE;
			return o1.getCanonicalName().compareTo(o2.getCanonicalName());
		}
		
	}
	
	/**
	 * Provides support for object's comparisons using hashCode
	 * $Author: ngaud$
	 *
	 * @param <T> - the type of object to compare
	 */
	private static class ObjectReferenceComparator<T> implements Comparator<T> {

		/**
		 */
		public ObjectReferenceComparator() {
			//
		}

		/**
		 * {@inheritDoc}
		 */
		public int compare(T o1, T o2) {
			return System.identityHashCode(o2) - System.identityHashCode(o1);
		}
		
	}
	
	
}
