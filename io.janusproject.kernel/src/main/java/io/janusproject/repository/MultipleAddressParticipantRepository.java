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

import io.janusproject.repository.impl.RepositoryImplFactory;
import io.sarl.lang.core.EventListener;

import java.io.Serializable;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import com.google.inject.Inject;
import com.hazelcast.core.MultiMap;

/**
 * 
 * @author $Author: ngaud$
 *
 * @param <ADDRESS> - the generic type representing the address of a participant in the related space. This type must remains small, less thanM in memory and must be {@link java.io.Serializable}
 */
public final class MultipleAddressParticipantRepository<ADDRESS extends Serializable> extends ParticipantRepository<ADDRESS> {

	/**
	 * Map linking the id of an entity to its various addresses in the related space
	 * This map must be distributed and synchronized all over the network
	 */
	private MultiMap<UUID, ADDRESS> participants;
	
	
	private String distributedParticipantMapName;
	
	public MultipleAddressParticipantRepository(String distributedParticipantMapName) {
		super();
		this.distributedParticipantMapName = distributedParticipantMapName;
	}
	

	

	@Inject
	void setFactory(RepositoryImplFactory repositoryImplFactory){
		this.participants = repositoryImplFactory.getMultiMap(this.distributedParticipantMapName);
	}
	
	public ADDRESS registerParticipant(ADDRESS a, EventListener entity) {				
		this.addListener(a, entity);
		this.participants.put(entity.getID(), a);
		
		return a;
	}

	public ADDRESS unregisterParticipant(ADDRESS a, EventListener entity) {
		this.removeListener(a);
		this.participants.remove(entity.getID(),a);		
		return a;
	}

	public EventListener getListener(ADDRESS a) {
		return this.getListener(a);
	}
	
	public Collection<ADDRESS> getAddresses(UUID participant) {
		return this.participants.get(participant);
	}
	
	
	public Collection<ADDRESS> getParticipantAddresses() {
		return this.participants.values();
	}
	
	public Set<UUID> getParticipantIDs() {
		return this.participants.keySet();
	}
	

	
}
