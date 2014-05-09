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

import java.util.Map;



/** Constants for the Janus configuration.
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class JanusConfig {
	
	/** Name of the property that contains the logger factory for hazelcast.
	 */
	public static final String HAZELCAST_LOGGER_FACTORY = "hazelcast.logging.class"; //$NON-NLS-1$

	/** Name of the property that contains the verbosity level of Janus.
	 */
	public static final String VERBOSE_LEVEL = "janus.verbose.level"; //$NON-NLS-1$

	/** Name of the property that contains the verbosity level of Janus.
	 */
	public static final String LOGGING_PROPERTY_FILE = "java.util.logging.config.file"; //$NON-NLS-1$

	/** Name of the property that contains the boolean value for offline/online.
	 */
	public static final String OFFLINE = "janus.network.offline"; //$NON-NLS-1$

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

	/** Name of the property that contains the classname of the boot agent.
	 */
	public static final String BOOT_AGENT = "janus.boot.agent"; //$NON-NLS-1$

	/** Name of the property that contains the identifier for the default
	 * space of the Janus context.
	 * @see #VALUE_DEFAULT_SPACE_ID
	 */
	public static final String DEFAULT_SPACE_ID = "janus.context.space.id"; //$NON-NLS-1$
	
	/** Name of the property that contains the public network URI.
	 */
	public static final String PUB_URI = "network.pub.uri"; //$NON-NLS-1$

	/** The default name of the hazelcast logger factory of Janus.
	 * @see #HAZELCAST_LOGGER_FACTORY
	 */
	public static final String VALUE_HAZELCAST_LOGGER_FACTORY = "io.janusproject.kernel.hazelcast.HzKernelLoggerFactory"; //$NON-NLS-1$

	/** The default name of the logging property file of Janus.
	 * @see #LOGGING_PROPERTY_FILE
	 */
	public static final String VALUE_LOGGING_PROPERTY_FILE = "resource:io/janusproject/logging.properties"; //$NON-NLS-1$

	/** The default verbosity level of Janus.
	 * @see #VERBOSE_LEVEL
	 */
	public static final int VALUE_VERBOSE_LEVEL = 3;

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
	public static final Boolean VALUE_RANDOM_DEFAULT_CONTEXT_ID = Boolean.FALSE;

	/** Indicates if the default context id has a value computed
	 * from the boot agent type.
	 * @see #BOOT_DEFAULT_CONTEXT_ID
	 */
	public static final Boolean VALUE_BOOT_DEFAULT_CONTEXT_ID = Boolean.FALSE;

	/** Indicates the maximal number of threads in a thread pool.
	 */
	public static final int VALUE_NUMBER_OF_THREADS_IN_EXECUTOR = 20;
	
	/** Indicates the numbers of seconds that the kernel is waiting
	 * for thread terminations before timeout.
	 */
	public static final int VALUE_KERNEL_THREAD_TIMEOUT = 30;
	
	
	/** Replies the default values for the properties supported by Janus config.
	 * 
	 * @param defaultValues - filled with the default values supported by the Janus platform.
	 */
	public static void getDefaultValues(Map<String,Object> defaultValues) {
		defaultValues.put(BOOT_AGENT, null);
		defaultValues.put(BOOT_DEFAULT_CONTEXT_ID, VALUE_BOOT_DEFAULT_CONTEXT_ID);
		defaultValues.put(DEFAULT_CONTEXT_ID, VALUE_DEFAULT_CONTEXT_ID);
		defaultValues.put(DEFAULT_SPACE_ID, VALUE_DEFAULT_SPACE_ID);
		defaultValues.put(OFFLINE, Boolean.FALSE.toString());
		defaultValues.put(PUB_URI, null);
		defaultValues.put(RANDOM_DEFAULT_CONTEXT_ID, VALUE_RANDOM_DEFAULT_CONTEXT_ID);
		defaultValues.put(VERBOSE_LEVEL, VALUE_VERBOSE_LEVEL);
		defaultValues.put(LOGGING_PROPERTY_FILE, VALUE_LOGGING_PROPERTY_FILE);
		defaultValues.put(HAZELCAST_LOGGER_FACTORY, VALUE_HAZELCAST_LOGGER_FACTORY);
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
	
	/** Replies the value of the boolean system property.
	 * 
	 * @param name - name of the property.
	 * @return the value, or <code>false</code> if no property found.
	 */
	public static boolean getSystemPropertyAsBoolean(String name) {
		return getSystemPropertyAsBoolean(name, false);
	}

	/** Replies the value of the boolean system property.
	 * 
	 * @param name - name of the property.
	 * @param defaultValue - value to reply if the these is no property found
	 * @return the value, or <var>defaultValue</var>.
	 */
	public static boolean getSystemPropertyAsBoolean(String name, boolean defaultValue) {
		String value = getSystemProperty(name, null);
		if (value!=null) {
			try {
				return Boolean.parseBoolean(value);
			}
			catch(Throwable _) {
				//
			}
		}
		return defaultValue;
	}

	/** Replies the value of the integer system property.
	 * 
	 * @param name - name of the property.
	 * @return the value, or <code>0</code> if no property found.
	 */
	public static int getSystemPropertyAsInteger(String name) {
		return getSystemPropertyAsInteger(name, 0);
	}

	/** Replies the value of the integer system property.
	 * 
	 * @param name - name of the property.
	 * @param defaultValue - value to reply if the these is no property found
	 * @return the value, or <var>defaultValue</var>.
	 */
	public static int getSystemPropertyAsInteger(String name, int defaultValue) {
		String value = getSystemProperty(name, null);
		if (value!=null) {
			try {
				return Integer.parseInt(value);
			}
			catch(Throwable _) {
				//
			}
		}
		return defaultValue;
	}

}
