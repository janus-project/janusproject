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
package io.janusproject2.network.event;

import io.sarl.lang.core.Event;

/** Utility class that is representing the "no-event" event.
 * 
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public final class NoEvent extends Event {
	
	private static final long serialVersionUID = 6442889151184546270L;
	
	/** Singleton instance.
	 */
	final static Event INSTANCE = new NoEvent();
	
	private NoEvent() {
		//
	}
	
	@Override
	protected String attributesToString() {
		return "NO_EVENT"; //$NON-NLS-1$
	}

}
