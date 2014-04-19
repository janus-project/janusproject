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
package io.janusproject2.kernel.services;

import io.sarl.lang.core.Space;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;


/** This service enables to store the spaces in the janus platform.
 * 
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public interface SpaceService extends JanusService {

	/**
	 * Does this repository contain some space.
	 * 
	 * @return <code>true</code> if this repository contains no context,
	 * <code>false</code> otherwise
	 */
	public boolean isEmptySpaceRepository();

	/**
	 * Returns the number of spaces registered in this repository
	 * 
	 * @return the number of spaces registered in this repository
	 */
	public int getNumberOfSpaces();

	/**
	 * Check if this repository contains a space with the specified id
	 * 
	 * @param spaceID
	 *            - the id to test
	 * @return <code>true</code> if this repository contains a context with the specified id,
	 *         false otherwise
	 */
	public boolean containsSpace(UUID spaceID);

	/**
	 * Add a new space to this repository.
	 * 
	 * @param space
	 *            - the space to add
	 */
	public void addSpace(Space space);

	/**
	 * Remove the specified space from this repository
	 * 
	 * @param space
	 *            - the space to remove
	 */
	public void removeSpace(Space space);

	/**
	 * Remove the space with the specified id from this repository
	 * 
	 * @param spaceID
	 *            - the id of the spaceto remove
	 */
	public void removeSpace(UUID spaceID);

	/**
	 * Clear the space of this repository
	 */
	public void removeAllSpaces();

	/**
	 * Returns the collection of all spaces stored in this repository
	 * 
	 * @return the collection of all sp	aces stored in this repository
	 */
	public Collection<Space> getSpaces();

	/**
	 * Returns the set of all space IDs stored in this repository
	 * 
	 * @return the set of all space IDs stored in this repository
	 */
	public Set<UUID> getSpaceIDs();

	/**
	 * Returns the {@link Space} with the given ID
	 * 
	 * @param spaceID
	 * @return the {@link Space} with the given ID
	 */
	public Space getSpace(UUID spaceID);
	
	/**
	 * Returns the collection of {@link Space} with the given IDs
	 * @param spaceIDs
	 * @return the collection of {@link Space} with the given IDs
	 */
	public Collection<Space> getSpaces(Collection<UUID> spaceIDs);
	
	/** Add a listener on the space service events.
	 * 
	 * @param listener
	 */
	public void addSpaceServiceListener(SpaceServiceListener listener);

	/** Remove a listener on the space service events.
	 * 
	 * @param listener
	 */
	public void removeSpaceServiceListener(SpaceServiceListener listener);

}
