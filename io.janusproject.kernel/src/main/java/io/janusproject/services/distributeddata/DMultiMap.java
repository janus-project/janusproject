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
package io.janusproject.services.distributeddata;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/** Interface that represents a distributed multi-map.
 * <p>
 * A multi-map is a map that is associating a key to multiple values.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @param <K>
 * @param <V>
 */
public interface DMultiMap<K, V> {

	/**
	 * Stores a key-value pair in the multimap.
	 * <p/>
	 * <p><b>Warning:</b></p>
	 * <p>
	 * This method uses <tt>hashCode</tt> and <tt>equals</tt> of binary form of
	 * the <tt>key</tt>, not the actual implementations of <tt>hashCode</tt> and <tt>equals</tt>
	 * defined in <tt>key</tt>'s class.
	 * </p>
	 *
	 * @param key   the key to be stored
	 * @param value the value to be stored
	 * @return true if size of the multimap is increased, false if the multimap
	 *         already contains the key-value pair.
	 */
	boolean put(K key, V value);

	/**
	 * Returns the collection of values associated with the key.
	 * <p/>
	 * <p><b>Warning:</b></p>
	 * <p>
	 * This method uses <tt>hashCode</tt> and <tt>equals</tt> of binary form of
	 * the <tt>key</tt>, not the actual implementations of <tt>hashCode</tt> and <tt>equals</tt>
	 * defined in <tt>key</tt>'s class.
	 * </p>
	 * <p/>
	 * <p><b>Warning-2:</b></p>
	 * The collection is <b>NOT</b> backed by the map,
	 * so changes to the map are <b>NOT</b> reflected in the collection, and vice-versa.
	 *
	 * @param key the key whose associated values are to be returned
	 * @return the collection of the values associated with the key.
	 */
	Collection<V> get(K key);

	/**
	 * Removes the given key value pair from the multimap.
	 * <p/>
	 * <p><b>Warning:</b></p>
	 * This method uses <tt>hashCode</tt> and <tt>equals</tt> of binary form of
	 * the <tt>key</tt>, not the actual implementations of <tt>hashCode</tt> and <tt>equals</tt>
	 * defined in <tt>key</tt>'s class.
	 *
	 * @param key   the key of the entry to remove
	 * @param value the value of the entry to remove
	 * @return true if the size of the multimap changed after the remove operation, false otherwise.
	 */
	boolean remove(Object key, Object value);

	/**
	 * Removes all the entries with the given key.
	 * <p/>
	 * <p><b>Warning:</b></p>
	 * <p>
	 * This method uses <tt>hashCode</tt> and <tt>equals</tt> of binary form of
	 * the <tt>key</tt>, not the actual implementations of <tt>hashCode</tt> and <tt>equals</tt>
	 * defined in <tt>key</tt>'s class.
	 * </p>
	 * <p/>
	 * <p><b>Warning-2:</b></p>
	 * The collection is <b>NOT</b> backed by the map,
	 * so changes to the map are <b>NOT</b> reflected in the collection, and vice-versa.
	 *
	 * @param key the key of the entries to remove
	 * @return the collection of removed values associated with the given key. Returned collection
	 *         might be modifiable but it has no effect on the multimap
	 */
	Collection<V> remove(Object key);

	/**
	 * Returns the set of keys in the multimap.
	 * <p/>
	 * <p><b>Warning:</b></p>
	 * The set is <b>NOT</b> backed by the map,
	 * so changes to the map are <b>NOT</b> reflected in the set, and vice-versa.
	 *
	 * @return the set of keys in the multimap. Returned set might be modifiable
	 *         but it has no effect on the multimap
	 */
	Set<K> keySet();

	/**
	 * Returns the collection of values in the multimap.
	 * <p/>
	 * <p><b>Warning:</b></p>
	 * The collection is <b>NOT</b> backed by the map,
	 * so changes to the map are <b>NOT</b> reflected in the collection, and vice-versa.
	 *
	 * @return the collection of values in the multimap. Returned collection might be modifiable
	 *         but it has no effect on the multimap
	 */
	Collection<V> values();

	/**
	 * Returns the set of key-value pairs in the multimap.
	 * <p/>
	 * <p><b>Warning:</b></p>
	 * The set is <b>NOT</b> backed by the map,
	 * so changes to the map are <b>NOT</b> reflected in the set, and vice-versa.
	 *
	 * @return the set of key-value pairs in the multimap. Returned set might be modifiable
	 *         but it has no effect on the multimap
	 */
	Set<Map.Entry<K, V>> entrySet();

	/**
	 * Returns whether the multimap contains an entry with the key.
	 * <p/>
	 * <p><b>Warning:</b></p>
	 * <p>
	 * This method uses <tt>hashCode</tt> and <tt>equals</tt> of binary form of
	 * the <tt>key</tt>, not the actual implementations of <tt>hashCode</tt> and <tt>equals</tt>
	 * defined in <tt>key</tt>'s class.
	 * </p>
	 *
	 * @param key the key whose existence is checked.
	 * @return true if the multimap contains an entry with the key, false otherwise.
	 */
	boolean containsKey(K key);

	/**
	 * Returns whether the multimap contains an entry with the value.
	 * <p/>
	 *
	 * @param value the value whose existence is checked.
	 * @return true if the multimap contains an entry with the value, false otherwise.
	 */
	boolean containsValue(Object value);

	/**
	 * Returns whether the multimap contains the given key-value pair.
	 * <p/>
	 * This method uses <tt>hashCode</tt> and <tt>equals</tt> of binary form of
	 * the <tt>key</tt>, not the actual implementations of <tt>hashCode</tt> and <tt>equals</tt>
	 * defined in <tt>key</tt>'s class.
	 *
	 * @param key   the key whose existence is checked.
	 * @param value the value whose existence is checked.
	 * @return true if the multimap contains the key-value pair, false otherwise.
	 */
	boolean containsEntry(K key, V value);

	/**
	 * Returns the number of key-value pairs in the multimap.
	 *
	 * @return the number of key-value pairs in the multimap.
	 */
	int size();

	/**
	 * Clears the multimap. Removes all key-value pairs.
	 */
	void clear();

	/**
	 * Returns number of values matching to given key in the multimap.
	 * <p/>
	 * <p><b>Warning:</b></p>
	 * <p>
	 * This method uses <tt>hashCode</tt> and <tt>equals</tt> of binary form of
	 * the <tt>key</tt>, not the actual implementations of <tt>hashCode</tt> and <tt>equals</tt>
	 * defined in <tt>key</tt>'s class.
	 * </p>
	 *
	 * @param key the key whose values count are to be returned
	 * @return number of values matching to given key in the multimap.
	 */
	int valueCount(K key);

	/** Add listener on events on the DMultiMap.
	 *
	 * @param listener - the listener
	 */
	void addDMapListener(DMapListener<K, V> listener);

	/** Remove listener on events on the DMultiMap.
	 *
	 * @param listener - the listener
	 */
	void removeDMapListener(DMapListener<K, V> listener);

}
