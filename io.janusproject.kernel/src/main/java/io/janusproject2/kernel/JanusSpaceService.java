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
package io.janusproject2.kernel;

import io.janusproject2.kernel.SpaceRepository.SpaceRepositoryListener;
import io.janusproject2.services.SpaceService;
import io.janusproject2.services.SpaceServiceListener;
import io.sarl.lang.core.Space;

import java.util.ArrayList;
import java.util.List;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Singleton;

/** Platform service that supports the space listening.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
class JanusSpaceService extends AbstractService implements SpaceService, SpaceRepositoryListener {

	private final List<SpaceServiceListener> listeners = new ArrayList<>();
	
	/** {@inheritDoc}
	 */
	@Override
	public void addSpaceServiceListener(SpaceServiceListener listener) {
		synchronized(this.listeners) {
			this.listeners.add(listener);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public void removeSpaceServiceListener(SpaceServiceListener listener) {
		synchronized(this.listeners) {
			this.listeners.add(listener);
		}
	}
	
	/** Notifies the listeners on the space creation.
	 * 
	 * @param id
	 */
	protected void fireSpaceCreated(Space id) {
		SpaceServiceListener[] listeners;
		synchronized(this.listeners) {
			listeners = new SpaceServiceListener[this.listeners.size()];
			this.listeners.toArray(listeners);
		}
		for(SpaceServiceListener listener : listeners) {
			listener.spaceCreated(id);
		}
	}

	/** Notifies the listeners on the space destruction.
	 * 
	 * @param id
	 */
	protected void fireSpaceDestroyed(Space id) {
		SpaceServiceListener[] listeners;
		synchronized(this.listeners) {
			listeners = new SpaceServiceListener[this.listeners.size()];
			this.listeners.toArray(listeners);
		}
		for(SpaceServiceListener listener : listeners) {
			listener.spaceDestroyed(id);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void doStart() {
		notifyStarted();
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void doStop() {
		notifyStopped();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void spaceCreated(Space space) {
		if (isRunning()) fireSpaceCreated(space);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void spaceDestroyed(Space space) {
		if (isRunning()) fireSpaceDestroyed(space);
	}

}
