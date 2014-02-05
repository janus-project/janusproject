package io.janusproject.repository;


import io.janusproject.repository.impl.RepositoryImplFactory;
import io.sarl.lang.core.EventListener;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.inject.Inject;

/**
 * A repository of participants specific to a given space
 * 
 * @author $Author: ngaud$
 * 
 * @param <ADDRESS> - the generic type representing the address of a participant in the related space. This type must remains small, less thanM in memory and must be {@link java.io.Serializable}
 */
public final class UniqueAddressParticipantRepository<ADDRESS extends Serializable> extends ParticipantRepository<ADDRESS> {

	/**
	 * Map linking the id of an entity to its unique address in the related space
	 * This map must be distributed and synchronized all over the network
	 */
	private Map<UUID, ADDRESS> participants;
	private String distributedParticipantMapName;	

	
	
	public UniqueAddressParticipantRepository(String distributedParticipantMapName) {
		super();
		this.distributedParticipantMapName = distributedParticipantMapName;
	}
	
	@Inject
	void setFactory(RepositoryImplFactory repositoryImplFactory){
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

	public ADDRESS unregisterParticipant(EventListener entity) {
		return this.unregisterParticipant(entity.getID());
	}
	
	public ADDRESS unregisterParticipant(UUID entityID) {
		this.removeListener(this.participants.get(entityID));
		return this.participants.remove(entityID);
	}
	
	public ADDRESS getAddress(EventListener entity) {
		return getAddress(entity.getID());
	}

	public ADDRESS getAddress(UUID id) {
		return this.participants.get(id);
	}
	
	public Collection<ADDRESS> getParticipantAddresses() {
		return this.participants.values();
	}
	
	public Set<UUID> getParticipantIDs() {
		return this.participants.keySet();
	}
	
}
