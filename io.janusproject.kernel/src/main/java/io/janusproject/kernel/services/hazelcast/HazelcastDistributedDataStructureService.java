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
package io.janusproject.kernel.services.hazelcast;

import io.janusproject.services.AbstractDependentService;
import io.janusproject.services.distributeddata.DMap;
import io.janusproject.services.distributeddata.DMapListener;
import io.janusproject.services.distributeddata.DMultiMap;
import io.janusproject.services.distributeddata.DistributedDataStructureService;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.util.concurrent.Service;
import com.google.inject.Inject;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.MultiMap;

/**
 * Service based on Hazelcast that permits to manage data structures
 * that are shared over a network.
 *
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class HazelcastDistributedDataStructureService extends AbstractDependentService
implements DistributedDataStructureService {

	@Inject
	private HazelcastInstance hazelcastInstance;

	@Override
	public final Class<? extends Service> getServiceType() {
		return DistributedDataStructureService.class;
	}

	/** Change the hazelcast instance used by this factory.
	 *
	 * @param hazelcastInstance - reference to the Hazelcast engine.
	 */
	void setHazelcastInstance(HazelcastInstance hazelcastInstance) {
		this.hazelcastInstance = hazelcastInstance;
	}

	@Override
	protected void doStart() {
		notifyStarted();
	}

	@Override
	protected void doStop() {
		notifyStopped();
	}

	@Override
	public <K, V> DMap<K, V> getMap(String name) {
		IMap<K, V> m = this.hazelcastInstance.getMap(name);
		if (m != null) {
			return new MapWrapper<>(m);
		}
		return null;
	}

	@Override
	public <K, V> DMultiMap<K, V> getMultiMap(String name) {
		MultiMap<K, V> m = this.hazelcastInstance.getMultiMap(name);
		if (m != null) {
			return new MultiMapWrapper<>(m);
		}
		return null;
	}

	/**
	 * @param <K> - type of the keys.
	 * @param <V> - type of the values.
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static final class MapWrapper<K, V> implements DMap<K, V> {

		private final IMap<K, V> map;

		public MapWrapper(IMap<K, V> map) {
			assert (map != null);
			this.map = map;
		}

		@Override
		public void clear() {
			this.map.clear();
		}

		@Override
		public boolean containsKey(Object arg0) {
			return this.map.containsKey(arg0);
		}

		@Override
		public boolean containsValue(Object arg0) {
			return this.map.containsValue(arg0);
		}

		@Override
		public Set<java.util.Map.Entry<K, V>> entrySet() {
			return this.map.entrySet();
		}

		@Override
		public V get(Object arg0) {
			return this.map.get(arg0);
		}

		@Override
		public boolean isEmpty() {
			return this.map.isEmpty();
		}

		@Override
		public Set<K> keySet() {
			return this.map.keySet();
		}

		@Override
		public V put(K arg0, V arg1) {
			return this.map.put(arg0, arg1);
		}

		@Override
		public void putAll(Map<? extends K, ? extends V> arg0) {
			this.map.putAll(arg0);
		}

		@Override
		public V remove(Object arg0) {
			return this.map.remove(arg0);
		}

		@Override
		public int size() {
			return this.map.size();
		}

		@Override
		public Collection<V> values() {
			return this.map.values();
		}

		@Override
		public V putIfAbsent(K key, V value) {
			return this.map.putIfAbsent(key, value);
		}

		@Override
		public void addDMapListener(DMapListener<K, V> listener) {
			EntryListenerWrapper<K, V> w = new EntryListenerWrapper<>(listener);
			String k = this.map.addEntryListener(w, true);
			w.setHazelcastListener(k);
		}

		/** {@inheritDoc}
		 */
		@Override
		public void removeDMapListener(DMapListener<K, V> listener) {
			if (listener instanceof EntryListenerWrapper) {
				String k = ((EntryListenerWrapper<?, ?>) listener).getHazelcastListener();
				if (k != null) {
					this.map.removeEntryListener(k);
				}
			}
		}

	}

	/**
	 * @param <K> - type of the keys.
	 * @param <V> - type of the values.
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static final class MultiMapWrapper<K, V> implements DMultiMap<K, V> {

		private final MultiMap<K, V> map;

		public MultiMapWrapper(MultiMap<K, V> map) {
			assert (map != null);
			this.map = map;
		}

		@Override
		public boolean put(K key, V value) {
			return this.map.put(key, value);
		}

		@Override
		public Collection<V> get(K key) {
			return this.map.get(key);
		}

		@Override
		public boolean remove(Object key, Object value) {
			return this.map.remove(key, value);
		}

		@Override
		public Collection<V> remove(Object key) {
			return this.map.remove(key);
		}

		@Override
		public Set<K> keySet() {
			return this.map.keySet();
		}

		@Override
		public Collection<V> values() {
			return this.map.values();
		}

		@Override
		public Set<Entry<K, V>> entrySet() {
			return this.map.entrySet();
		}

		@Override
		public boolean containsKey(K key) {
			return this.map.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return this.map.containsValue(value);
		}

		@Override
		public boolean containsEntry(K key, V value) {
			return this.map.containsEntry(key, value);
		}

		@Override
		public int size() {
			return this.map.size();
		}

		@Override
		public void clear() {
			this.map.clear();
		}

		@Override
		public int valueCount(K key) {
			return this.map.valueCount(key);
		}

		@Override
		public void addDMapListener(DMapListener<K, V> listener) {
			EntryListenerWrapper<K, V> w = new EntryListenerWrapper<>(listener);
			String k = this.map.addEntryListener(w, true);
			w.setHazelcastListener(k);
		}

		@Override
		public void removeDMapListener(DMapListener<K, V> listener) {
			if (listener instanceof EntryListenerWrapper) {
				String k = ((EntryListenerWrapper<?, ?>) listener).getHazelcastListener();
				if (k != null) {
					this.map.removeEntryListener(k);
				}
			}
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 * @param <K>
	 * @param <V>
	 */
	private static class EntryListenerWrapper<K, V> implements EntryListener<K, V> {

		private final DMapListener<K, V> dmapListener;
		private String key;

		public EntryListenerWrapper(DMapListener<K, V> listener) {
			this.dmapListener = listener;
		}

		/** Replies the Hazelcast listener associated to this object.
		 *
		 * @return the hazelcast listener.
		 */
		public String getHazelcastListener() {
			return this.key;
		}

		/** Replies the Hazelcast listener associated to this object.
		 *
		 * @param hazelcastListener - the hazelcast listener.
		 */
		public void setHazelcastListener(String hazelcastListener) {
			this.key = hazelcastListener;
		}

		@Override
		public void entryAdded(EntryEvent<K, V> event) {
			this.dmapListener.entryAdded(event.getKey(), event.getValue());
		}

		@Override
		public void entryEvicted(EntryEvent<K, V> event) {
			this.dmapListener.entryRemoved(event.getKey(), event.getValue());
		}

		@Override
		public void entryRemoved(EntryEvent<K, V> event) {
			this.dmapListener.entryRemoved(event.getKey(), event.getValue());
		}

		@Override
		public void entryUpdated(EntryEvent<K, V> event) {
			this.dmapListener.entryUpdated(event.getKey(), event.getValue());
		}

	}

}
