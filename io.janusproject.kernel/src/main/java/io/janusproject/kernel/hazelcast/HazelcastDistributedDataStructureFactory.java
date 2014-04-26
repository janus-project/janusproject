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
package io.janusproject.kernel.hazelcast;

import io.janusproject.repository.DistributedDataStructureFactory;

import java.util.Collection;
import java.util.Map.Entry;
import java.util.Set;

import com.google.inject.Inject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.core.ISet;
import com.hazelcast.core.MultiMap;

/**
 * Factory that permits to manage data structures that are shared over a network.
 * 
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class HazelcastDistributedDataStructureFactory implements DistributedDataStructureFactory {

	@Inject
	private HazelcastInstance hazelcastInstance;

	@Override
	public <K, V> IMap<K, V> getMap(String name) {
		return this.hazelcastInstance.getMap(name);
	}

	@Override
	public <K, V> io.janusproject.repository.MultiMap<K, V> getMultiMap(String name) {
		MultiMap<K,V> m = this.hazelcastInstance.getMultiMap(name);
		if (m==null) return new MultiMapWrapper<>(m);
		return null;
	}

	@Override
	public <E> IQueue<E> getQueue(String name) {
		return this.hazelcastInstance.getQueue(name);
	}

	@Override
	public <E> ISet<E> getSet(String name) {
		return this.hazelcastInstance.getSet(name);
	}

	@Override
	public <E> IList<E> getList(String name) {
		return this.hazelcastInstance.getList(name);
	}

	@Override
	public ISemaphore getSemaphore(String name) {
		return this.hazelcastInstance.getSemaphore(name);
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 * @param <K>
	 * @param <V>
	 */
	private static final class MultiMapWrapper<K,V> implements io.janusproject.repository.MultiMap<K,V> {
		
		private final MultiMap<K,V> map;
		
		public MultiMapWrapper(MultiMap<K,V> map) {
			assert(map!=null);
			this.map = map;
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean put(K key, V value) {
			return this.map.put(key, value);
		}

		/** {@inheritDoc}
		 */
		@Override
		public Collection<V> get(K key) {
			return this.map.get(key);
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean remove(Object key, Object value) {
			return this.map.remove(key, value);
		}

		/** {@inheritDoc}
		 */
		@Override
		public Collection<V> remove(Object key) {
			return this.map.remove(key);
		}

		/** {@inheritDoc}
		 */
		@Override
		public Set<K> keySet() {
			return this.map.keySet();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Collection<V> values() {
			return this.map.values();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Set<Entry<K, V>> entrySet() {
			return this.map.entrySet();
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean containsKey(K key) {
			return this.map.containsKey(key);
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean containsValue(Object value) {
			return this.map.containsValue(value);
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean containsEntry(K key, V value) {
			return this.map.containsEntry(key, value);
		}

		/** {@inheritDoc}
		 */
		@Override
		public int size() {
			return this.map.size();
		}

		/** {@inheritDoc}
		 */
		@Override
		public void clear() {
			this.map.clear();
		}

		/** {@inheritDoc}
		 */
		@Override
		public int valueCount(K key) {
			return this.map.valueCount(key);
		}
		
	}
	
}
