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
package io.janusproject.services;


/**
 * Utility interface that is providing the best service priorities.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public interface ServicePriorities {

	/** Start priority for the executor service.
	 */
	public static final int START_EXECUTOR_SERVICE = 0;

	/** Start priority for the logging service.
	 */
	public static final int START_LOGGING_SERVICE = 1;

	/** Start priority for the kernel discovery service.
	 */
	public static final int START_KERNEL_DISCOVERY_SERVICE = 2;

	/** Start priority for the network service.
	 */
	public static final int START_NETWORK_SERVICE = 3;

	/** Start priority for the space service.
	 */
	public static final int START_SPACE_SERVICE = 4;

	/** Start priority for the context service.
	 */
	public static final int START_CONTEXT_SERVICE = 5;

	/** Start priority for the agent spawning service.
	 */
	public static final int START_SPAWN_SERVICE = 6;

	/** Start priority for the executor service.
	 */
	public static final int STOP_EXECUTOR_SERVICE = 6;

	/** Start priority for the logging service.
	 */
	public static final int STOP_LOGGING_SERVICE = 5;

	/** Start priority for the kernel discovery service.
	 */
	public static final int STOP_KERNEL_DISCOVERY_SERVICE = 4;

	/** Start priority for the network service.
	 */
	public static final int STOP_NETWORK_SERVICE = 3;

	/** Start priority for the space service.
	 */
	public static final int STOP_SPACE_SERVICE = 2;

	/** Start priority for the context service.
	 */
	public static final int STOP_CONTEXT_SERVICE = 1;

	/** Start priority for the agent spawning service.
	 */
	public static final int STOP_SPAWN_SERVICE = 0;

}

