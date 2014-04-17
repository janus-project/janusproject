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
package io.janusproject;

import io.sarl.lang.core.Agent;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.UUID;


/** Constants for the Janus configuration.
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class JanusConfig {
	
	/** Name of the property that contains the identifier of the Janus context.
	 * @see #VALUE_DEFAULT_CONTEXT_ID
	 */
	public static final String DEFAULT_CONTEXT_ID = "janus.context.id"; //$NON-NLS-1$

	/** Name of the property that indicates if the ID of the default context
	 * must be randomly computed at boot time, or not.
	 * @see #DEFAULT_CONTEXT_ID
	 */
	public static final String RANDOM_DEFAULT_CONTEXT_ID = "janus.context.id.random"; //$NON-NLS-1$

	/** Name of the property that indicates if the ID of the default context
	 * must be computed from the boot agent type, or not.
	 * @see #DEFAULT_CONTEXT_ID
	 */
	public static final String BOOT_DEFAULT_CONTEXT_ID = "janus.context.id.boot"; //$NON-NLS-1$

	/** Name of the property that contains the identifier for the default
	 * space of the Janus context.
	 * @see #VALUE_DEFAULT_SPACE_ID
	 */
	public static final String DEFAULT_SPACE_ID = "janus.context.space.id"; //$NON-NLS-1$
	
	/** Name of the property that contains the public network URI.
	 */
	public static final String PUB_URI = "network.pub.uri"; //$NON-NLS-1$

	/** The default value for the Janus context identifier.
	 * @see #DEFAULT_CONTEXT_ID
	 */
	public static final String VALUE_DEFAULT_CONTEXT_ID = "2c38fb7f-f363-4f6e-877b-110b1f07cc77"; //$NON-NLS-1$
	
	/** The default value for the Janus space identifier.
	 * @see #DEFAULT_SPACE_ID
	 */
	public static final String VALUE_DEFAULT_SPACE_ID = "7ba8885d-545b-445a-a0e9-b655bc15ebe0"; //$NON-NLS-1$
	
	/** Indicates if the default context id has a random value or not at each boot time.
	 * @see #RANDOM_DEFAULT_CONTEXT_ID
	 */
	public static final boolean VALUE_RANDOM_DEFAULT_CONTEXT_ID = false;

	/** Indicates if the default context id has a value computed
	 * from the boot agent type.
	 * @see #BOOT_DEFAULT_CONTEXT_ID
	 */
	/** Reference to the type of the boot agent.
	 */
	private static Class<? extends Agent> bootAgent = null;
	
	/** Set the system properties from the given files.
	 * 
	 * @param bootClass - class of the first agent to launch.
	 * @param propertyFiles - list of the property files to read.
	 * @throws IOException when a property file cannot be read.
	 */
	static void init(Class<? extends Agent> bootClass, List<URL> propertyFiles) throws IOException {
		Properties systemProperties = System.getProperties();
		for(URL url : propertyFiles) {
			try(InputStream stream = url.openStream()) {
				systemProperties.load(stream);
			}
		}
		
		// Force the "janus.boot.agent" to have the name of the bootClass
		bootAgent = bootClass;
		
	}
	
	/** Replies the type of the agent that is launched
	 * when Janus was started.
	 * @return the type of the boot agent.
	 */
	public static Class<? extends Agent> getBootAgent() {
		return bootAgent;
	}
	
	/** Replies the value of the given property.
	 * <p>
	 * The value is search in the following sources.
	 * The first value found is replied.
	 * <ol>
	 * <li>{@link System#getProperty(String) system property};</li>
	 * <li>{@link System#getenv(String) environment variable};</li>
	 * <li>The default value defined by the Janus developers.</li>
	 * </ol>
	 * 
	 * @param name - name of the property to search for.
	 * @return the value of the property, or <code>null</code> if not found.
	 * @see #VALUE_DEFAULT_CONTEXT_ID
	 * @see #VALUE_DEFAULT_SPACE_ID
	 * @see #getProperty(String, String)
	 */
	public static String getProperty(String name) {
		return getProperty(name, true, null);
	}
	
	/** Replies the value of the given property.
	 * <p>
	 * The value is search in the following sources.
	 * The first value found is replied.
	 * <ol>
	 * <li>{@link System#getProperty(String) system property};</li>
	 * <li>{@link System#getenv(String) environment variable};</li>
	 * <li>the default value defined by the Janus developers, if known;</li>
	 * <li>the value of <var>defaultValue</var>.</li>
	 * </ol>
	 * 
	 * @param name - name of the property to search for.
	 * @param defaultValue - value to reply if none was found.
	 * @return the value of the property, or <var>defaultValue</var> if not found.
	 * @see #VALUE_DEFAULT_CONTEXT_ID
	 * @see #VALUE_DEFAULT_SPACE_ID
	 * @see #getProperty(String)
	 */
	public static String getProperty(String name, String defaultValue) {
		return getProperty(name, true, defaultValue);
	}
	
	private static String getProperty(String name, boolean useDefaults, String defaultValue) {
		String value;
		
		value = System.getProperty(name, null);
		if (value!=null) return value;
		
		value = System.getenv(name);
		if (value!=null) return value;
		
		if (useDefaults) {
			switch(name) {
			case DEFAULT_CONTEXT_ID:
				return VALUE_DEFAULT_CONTEXT_ID;
			case DEFAULT_SPACE_ID:
				return VALUE_DEFAULT_SPACE_ID;
			default:
			}
		}
		
		return defaultValue;
	}
	public static final boolean VALUE_BOOT_DEFAULT_CONTEXT_ID = false;
	
}
