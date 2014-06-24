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

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * Factory that permits to manage data structures that are shared over a network.
 *
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public interface DistributedDataStructureFactory {

	/**
	 * Replies the {@link Map} with the given name.
	 *
	 * @param <K> - types of the keys in the map.
	 * @param <V> - types of the values in the map.
	 * @param name - name of the shared map.
	 * @return the map.
	 */
	<K, V> Map<K, V> getMap(String name);

	/**
	 * Replies the {@link MultiMap} with the given name.
	 *
	 * @param <K> - types of the keys in the map.
	 * @param <V> - types of the values in the map.
	 * @param name - name of the shared multi-map.
	 * @return the map.
	 */
	<K, V> MultiMap<K, V> getMultiMap(String name);

	/**
	 * Replies the {@link Queue} with the given name.
	 *
	 * @param <E> - types of the elements in the queue.
	 * @param name - name of the shared queue.
	 * @return the queue.
	 */
	<E> Queue<E> getQueue(String name);

	/**
	 * Replies the {@link Set} with the given name.
	 *
	 * @param <E> - types of the elements in the set.
	 * @param name - name of the shared set.
	 * @return the set.
	 */
	<E> Set<E> getSet(String name);

	/**
	 * Replies the {@link List} with the given name.
	 *
	 * @param <E> - types of the elements in the list.
	 * @param name - name of the shared list.
	 * @return the list.
	 */
	<E> List<E> getList(String name);

	/**
	 * Replies the semaphore with the given name.
	 *
	 * @param name - name of the shared semaphore.
	 * @return the semaphore.
	 */
	Object getSemaphore(String name);

}
