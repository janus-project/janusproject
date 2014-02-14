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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An abstract repository providing the basic support of storage a 
 * collection a participant's address and its related listener.
 *  
 * @param <ADDRESS> - the generic type representing the address of a
 * participant in the related space. This type must remains small, less than M
 * in memory and must be {@link java.io.Serializable}
 * @author $Author: ngaud$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public abstract class ParticipantRepository<ADDRESS extends Serializable> {

	/**
	 * Map linking the unique address of an entity in the related space to the entity itself
	 * This is local non-distributed map
	 */
	private final Map<ADDRESS, EventListener> listeners;
	
	/** Construct a <code>ParticipantRepository</code>.
	 */
	protected ParticipantRepository() {
		this.listeners = new ConcurrentHashMap<>();
	}

	/** Replies the numbers of participants registered in this repository.
	 * 
	 * @return the number of listeners.
	 */
	public int listenerCount() {
		return this.listeners.size();
	}

	/** Replies if there is no participant registered in this repository.
	 * 
	 * @return <code>true</code> if the repository is empty, <code>false</code>
	 * if there is a least one participant.
	 */
	protected boolean isListenerEmpty() {
		return this.listeners.isEmpty();
	}

	/** Replies if the given address is present inside this repository.
	 * 
	 * @param key - key to search in the repository.
	 * @return <code>true</code> if the given key is found in the repository,
	 * <code>false</code> if the key is not present or <code>null</code>.
	 */
	protected boolean containsAddress(ADDRESS key) {
		return this.listeners.containsKey(key);
	}

	/** Replies if the given participant is present inside this repository.
	 * 
	 * @param value - participant to search in the repository.
	 * @return <code>true</code> if the given participant is found in the repository,
	 * <code>false</code> if the participant is not present or <code>null</code>.
	 */
	protected boolean containsListener(EventListener value) {
		return this.listeners.containsValue(value);
	}

	/** Replies the participant with the given address.
	 * 
	 * @param key
	 * @return the participant with the given address, or <code>null</code>
	 * if there is no participant with the given address.
	 */
	protected EventListener getListener(ADDRESS key) {
		return this.listeners.get(key);
	}

	/** Add a participant with the given address in this repository.
	 * 
	 * @param key - address of the participant.
	 * @param value - is the participant to map to the address.
	 * @return the participant that was previously associated to the given address.
	 */
	protected EventListener addListener(ADDRESS key, EventListener value) {
		return this.listeners.put(key, value);
	}

	/** Remove the mapping from the given address to the associated participant.
	 * 
	 * @param key - address of the participant to remove.
	 * @return the participant for which the address was removed, <code>null</code>
	 * if the given address was not found.
	 */
	protected EventListener removeListener(ADDRESS key) {
		return this.listeners.remove(key);
	}

	/** Remove all the participants in this repository.
	 */
	protected void clearListeners() {
		this.listeners.clear();
	}

	/** Replies all the addresses from the inside of this repository.
	 * 
	 * @return the addresses in this repository.
	 */
	protected Set<ADDRESS> getAdresses() {
		return this.listeners.keySet();
	}

	/** Replies all the participants from the inside of this repository.
	 * 
	 * @return the participants.
	 */
	public Collection<EventListener> getListeners() {
		return this.listeners.values();
	}

	/** Replies the pairs of addresses and participants in this repository.
	 * 
	 * @return the pairs of addresses and participants
	 */
	protected Set<Entry<ADDRESS, EventListener>> listenersEntrySet() {
		return this.listeners.entrySet();
	}
	
}
