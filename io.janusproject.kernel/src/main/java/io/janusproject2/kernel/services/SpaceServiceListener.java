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
package io.janusproject2.kernel.services;

import io.sarl.lang.core.Space;

import java.util.EventListener;

/** Listener on events related to the space service.
 *  
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public interface SpaceServiceListener extends EventListener {

	/** Invoked when the space is added.
	 * 
	 * @param space
	 */
	public void sapceCreated(Space space);
	
	/** Invoked when the space is destroyed.
	 * 
	 * @param space
	 */
	public void spaceDetroyed(Space[] space);
	
}
