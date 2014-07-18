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
package io.janusproject.testutils;

import io.janusproject.services.distributeddata.DMapListener;
import io.janusproject.services.distributeddata.DMultiMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

/** A mock of {@link DMultiMap}.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @param <K>
 * @param <V>
 */
public class MultiMapMock<K,V> implements DMultiMap<K, V> {

	private final Multimap<K,V> m = LinkedListMultimap.create();

	/**
	 */
	public MultiMapMock() {
		//
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean put(K key, V value) {
		return this.m.put(key, value);
	}

	/** {@inheritDoc}
	 */
	@Override
	public Collection<V> get(K key) {
		return this.m.get(key);
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean remove(Object key, Object value) {
		return this.m.remove(key, value);
	}

	/** {@inheritDoc}
	 */
	@Override
	public Collection<V> remove(Object key) {
		return this.m.removeAll(key);
	}

	/** {@inheritDoc}
	 */
	@Override
	public Set<K> keySet() {
		return this.m.keySet();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Collection<V> values() {
		return this.m.values();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Set<Entry<K, V>> entrySet() {
		return new HashSet<>(this.m.entries());
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean containsKey(K key) {
		return this.m.containsKey(key);
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean containsValue(Object value) {
		return this.m.containsValue(value);
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean containsEntry(K key, V value) {
		return this.m.containsEntry(key, value);
	}

	/** {@inheritDoc}
	 */
	@Override
	public int size() {
		return this.m.size();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void clear() {
		this.m.clear();
	}

	/** {@inheritDoc}
	 */
	@Override
	public int valueCount(K key) {
		Collection<V> c = this.m.asMap().get(key);
		if (c!=null) {
			return c.size();
		}
		return 0;
	}

	/** {@inheritDoc}
	 */
	@Override
	public void addDMapListener(DMapListener<K, V> listener) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void removeDMapListener(DMapListener<K, V> listener) {
		throw new UnsupportedOperationException();
	}

}
