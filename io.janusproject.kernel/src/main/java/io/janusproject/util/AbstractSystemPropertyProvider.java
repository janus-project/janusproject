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
package io.janusproject.util;

import com.google.inject.Provider;

/** Abstract implementation of a injection provider that is based on
 * the retreival of system properties.
 * 
 * @param <T> - type of the value to provide.
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public abstract class AbstractSystemPropertyProvider<T> implements Provider<T> {

	/**
	 */
	public AbstractSystemPropertyProvider() {
		//
	}

	/** Replies the value of the system property.
	 * 
	 * @param name - name of the property.
	 * @return the value, or <code>null</code> if no property found.
	 */
	public static String getSystemProperty(String name) {
		return getSystemProperty(name, null);
	}

	/** Replies the value of the system property.
	 * 
	 * @param name - name of the property.
	 * @param defaultValue - value to reply if the these is no property found
	 * @return the value, or <var>defaultValue</var>.
	 */
	public static String getSystemProperty(String name, String defaultValue) {
		String value;
		value = System.getProperty(name, null);
		if (value!=null) return value;
		value = System.getenv(name);
		if (value!=null) return value;
		return defaultValue;
	}

}	
