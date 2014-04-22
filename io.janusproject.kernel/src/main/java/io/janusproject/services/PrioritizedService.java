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

import com.google.common.util.concurrent.Service;

/**
 * This service has a priority to be launch/stop.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public interface PrioritizedService extends Service {

	/** Replies the launching priority.
	 * <p>
	 * Lower is the priority, sooner the service is started.
	 * 
	 * @return the launching priority. 
	 */
	public int getStartPriority();

	/** Replies the stopping priority.
	 * <p>
	 * Lower is the priority, sooner the service is stopped.
	 * 
	 * @return the stopping priority. 
	 */
	public int getStopPriority();

	/** Change the launching priority.
	 * <p>
	 * Lower is the priority, sooner the service is started.
	 * 
	 * @param priority 
	 */
	public void setStartPriority(int priority);

	/** Change the stopping priority.
	 * <p>
	 * Lower is the priority, sooner the service is stopped.
	 * 
	 * @param priority 
	 */
	public void setStopPriority(int priority);

}
