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
package io.janusproject.repository;

import io.sarl.lang.core.EventListener;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import com.google.inject.Inject;

/** Repository that maps participants to multiple addresses.
 * <p>
 * This repository links the id of an entity to its various addresses in the
 * related space.
 * <p>
 * The repository must be distributed and synchronized all over the network by
 * using data-structures that are provided by an injected
 * {@link DistributedDataStructureFactory}.
 * 
 * @param <ADDRESS> - the generic type representing the address of a participant
 * in the related space. This type must remains small, less than M in memory and 
 * must be {@link java.io.Serializable}
 * @author $Author: ngaud$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public final class MultipleAddressParticipantRepository<ADDRESS extends Serializable> extends ParticipantRepository<ADDRESS> {

	/**
	 * Map linking the id of an entity to its various addresses in the related space
	 * This map must be distributed and synchronized all over the network
	 */
	private MultiMap<UUID, ADDRESS> participants;

	private String distributedParticipantMapName;

	/** Constructs a <code>MultipleAddressParticipantRepository</code>.
	 * 
	 * @param distributedParticipantMapName - name of the multimap over the network. 
	 */
	public MultipleAddressParticipantRepository(String distributedParticipantMapName) {
		super();
		this.distributedParticipantMapName = distributedParticipantMapName;
	}

	/** Change the repository factory associated to this repository.
	 * 
	 * @param repositoryImplFactory
	 */
	@Inject
	void setFactory(DistributedDataStructureFactory repositoryImplFactory){
		this.participants = repositoryImplFactory.getMultiMap(this.distributedParticipantMapName);
	}

	/** Add a participant in this repository.
	 * 
	 * @param a - address of a participant to insert in this repository.
	 * @param entity - participant to map to the given address.
	 * @return <var>a</var>.
	 */
	public ADDRESS registerParticipant(ADDRESS a, EventListener entity) {				
		this.addListener(a, entity);
		this.participants.put(entity.getID(), a);
		return a;
	}

	/** Remove a participant from this repository.
	 * 
	 * @param a - address of a participant to remove from this repository.
	 * @param entity - participant to unmap to the given address.
	 * @return <var>a</var>.
	 */
	public ADDRESS unregisterParticipant(ADDRESS a, EventListener entity) {
		this.removeListener(a);
		this.participants.remove(entity.getID(),a);		
		return a;
	}

	/** Replies all the addresses of the participant with the given identifier.
	 * 
	 * @param participant
	 * @return the collection of addresses. It may be <code>null</code> if
	 * the participant is unknown.
	 */
	public Collection<ADDRESS> getAddresses(UUID participant) {
		return this.participants.get(participant);
	}


	/** Replies all the addresses in this repository.
	 * 
	 * @return the collection of addresses.
	 */
	public Collection<ADDRESS> getParticipantAddresses() {
		return this.participants.values();
	}

	/** Replies all the participants in this repository.
	 * 
	 * @return the collection of identifiers.
	 */
	public Set<UUID> getParticipantIDs() {
		return this.participants.keySet();
	}

}
