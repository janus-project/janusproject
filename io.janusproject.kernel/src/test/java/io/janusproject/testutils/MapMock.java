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

import io.janusproject.services.distributeddata.DMap;
import io.janusproject.services.distributeddata.DMapListener;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/** A mock of {@link DMap}.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @param <K>
 * @param <V>
 */
public class MapMock<K,V> implements DMap<K, V> {

	private final Map<K,V> m = new HashMap<>();

	/**
	 */
	public MapMock() {
		//
	}

	@Override
	public void addDMapListener(DMapListener<K, V> listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeDMapListener(DMapListener<K, V> listener) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		this.m.clear();
	}

	@Override
	public boolean containsKey(Object arg0) {
		return this.m.containsKey(arg0);
	}

	@Override
	public boolean containsValue(Object arg0) {
		return this.m.containsValue(arg0);
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return this.m.entrySet();
	}

	@Override
	public V get(Object arg0) {
		return this.m.get(arg0);
	}

	@Override
	public boolean isEmpty() {
		return this.m.isEmpty();
	}

	@Override
	public Set<K> keySet() {
		return this.m.keySet();
	}

	@Override
	public V put(K arg0, V arg1) {
		return this.m.put(arg0, arg1);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> arg0) {
		this.m.putAll(arg0);		
	}

	@Override
	public V remove(Object arg0) {
		return this.m.remove(arg0);
	}

	@Override
	public int size() {
		return this.m.size();
	}

	@Override
	public Collection<V> values() {
		return this.m.values();
	}

	@Override
	public V putIfAbsent(K key, V value) {
		return this.m.put(key, value);
	}

}
