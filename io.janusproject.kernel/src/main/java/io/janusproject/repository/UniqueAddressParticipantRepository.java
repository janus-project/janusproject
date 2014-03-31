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


import io.janusproject.repository.impl.DistributedDataStructureFactory;
import io.sarl.lang.core.EventListener;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.inject.Inject;

/**
 * A repository of participants specific to a given space.
 * <p>
 * This repository links the id of an entity to its various addresses in the
 * related space.
 * <p>
 * The repository must be distributed and synchronized all over the network.
 * 
 * @param <ADDRESS> - the generic type representing the address of a participant
 * in the related space. This type must remains small, less than M in memory and
 * must be {@link java.io.Serializable}.
 * @author $Author: ngaud$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public final class UniqueAddressParticipantRepository<ADDRESS extends Serializable> extends ParticipantRepository<ADDRESS> {

	/**
	 * Map linking the id of an entity to its unique address in the related space
	 * This map must be distributed and synchronized all over the network
	 */
	private Map<UUID, ADDRESS> participants;
	private String distributedParticipantMapName;	

	
	/** Constructs a <code>UniqueAddressParticipantRepository</code>.
	 * 
	 * @param distributedParticipantMapName - name of the multimap over the network. 
	 */
	public UniqueAddressParticipantRepository(String distributedParticipantMapName) {
		super();
		this.distributedParticipantMapName = distributedParticipantMapName;
	}
	
	/** Change the repository factory associated to this repository.
	 * 
	 * @param repositoryImplFactory
	 */
	@Inject
	void setFactory(DistributedDataStructureFactory repositoryImplFactory){
		this.participants = repositoryImplFactory.getMap(this.distributedParticipantMapName);
	}
	
	/**
	 * Registers a new participant in this repository
	 * @param a - the address of the participant
	 * @param entity - the entity associated to the specified address
	 * @return the address of the participant
	 */
	public ADDRESS registerParticipant(ADDRESS a, EventListener entity) {	
		this.addListener(a, entity);
		this.participants.put(entity.getID(), a);
		return a;
	}

	/** Remove a participant from this repository.
	 * 
	 * @param entity - participant to remove from this repository.
	 * @return the address that was mapped to the given participant.
	 */
	public ADDRESS unregisterParticipant(EventListener entity) {
		return this.unregisterParticipant(entity.getID());
	}
	
	/** Remove a participant with the given ID from this repository.
	 * 
	 * @param entityID - identifier of the participant to remove from this repository.
	 * @return the address that was mapped to the given participant.
	 */
	public ADDRESS unregisterParticipant(UUID entityID) {
		this.removeListener(this.participants.get(entityID));
		return this.participants.remove(entityID);
	}
	
	/** Replies the address associated to the given participant.
	 * 
	 * @param entity - instance of a participant.
	 * @return the address of the participant with the given id.
	 */
	public ADDRESS getAddress(EventListener entity) {
		return getAddress(entity.getID());
	}

	/** Replies the address associated to the participant with the given identifier.
	 * 
	 * @param id - identifier of the participant to retreive.
	 * @return the address of the participant with the given id.
	 */
	public ADDRESS getAddress(UUID id) {
		return this.participants.get(id);
	}

	/** Replies all the addresses of the participants that ar einside this repository.
	 * 
	 * @return all the addresses.
	 */
	public Collection<ADDRESS> getParticipantAddresses() {
		return this.participants.values();
	}
	
	/** Replies the identifiers of all the participants in this repository.
	 * 
	 * @return all the identifiers.
	 */
	public Set<UUID> getParticipantIDs() {
		return this.participants.keySet();
	}
	
}
