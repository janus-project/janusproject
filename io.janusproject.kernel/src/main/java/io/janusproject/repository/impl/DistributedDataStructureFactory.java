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
package io.janusproject.repository.impl;

import java.util.Map;

import com.google.inject.Inject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.core.ISet;
import com.hazelcast.core.MultiMap;

/** Factory that permits to manage data structures that are shared
 * over a network.
 * 
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class DistributedDataStructureFactory {

	@Inject
	private HazelcastInstance hazelcastInstance;
	
	/** Replies the {@link Map} with the given name.
	 * 
	 * @param name - name of the shared map.
	 * @return the map.
	 */
	public <K,V> Map<K,V> getMap(String name) {
		return this.hazelcastInstance.getMap(name);
	}
	
	/** Replies the {@link IMap} with the given name.
	 * 
	 * @param name - name of the shared map.
	 * @return the map.
	 */
	public <K,V> IMap<K,V> getIMap(String name) {
		return this.hazelcastInstance.getMap(name);
	}
		
	/** Replies the {@link MultiMap} with the given name.
	 * 
	 * @param name - name of the shared multi-map.
	 * @return the map.
	 */
	public <K,V> MultiMap<K,V> getMultiMap(String name) {
		return this.hazelcastInstance.getMultiMap(name);
	}


	/** Replies the {@link IQueue} with the given name.
	 * 
	 * @param name - name of the shared queue.
	 * @return the queue.
	 */
	public <E> IQueue<E> getQueue(String name) {
		return this.hazelcastInstance.getQueue(name);
	}


	/** Replies the {@link ISet} with the given name.
	 * 
	 * @param name - name of the shared set.
	 * @return the set.
	 */
	public <E> ISet<E> getSet(String name) {
		return this.hazelcastInstance.getSet(name);
	}


	/** Replies the {@link IList} with the given name.
	 * 
	 * @param name - name of the shared list.
	 * @return the list.
	 */
	public <E> IList<E> getList(String name) {
		return this.hazelcastInstance.getList(name);
	}


	/** Replies the {@link ISemaphore} with the given name.
	 * 
	 * @param name - name of the shared semaphore.
	 * @return the semaphore.
	 */
	public ISemaphore getSemaphore(String name) {
		return this.hazelcastInstance.getSemaphore(name);
	}
	
}
