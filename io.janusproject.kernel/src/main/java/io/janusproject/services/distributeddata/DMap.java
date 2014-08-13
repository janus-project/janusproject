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

import java.util.Map;

/** Interface that represents a distributed map.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @param <K>
 * @param <V>
 */
public interface DMap<K, V> extends Map<K, V> {

	/** Put the value if the given key is not inside
	 * the map.
	 *
	 * @param key - the key to insert.
	 * @param value - the value to insert.
	 * @return the previous value.
	 */
	V putIfAbsent(K key, V value);

	/** Add listener on events on the DMap.
	 *
	 * @param listener - the listener
	 */
	void addDMapListener(DMapListener<K, V> listener);

	/** Remove listener on events on the DMap.
	 *
	 * @param listener - the listener
	 */
	void removeDMapListener(DMapListener<K, V> listener);

}
