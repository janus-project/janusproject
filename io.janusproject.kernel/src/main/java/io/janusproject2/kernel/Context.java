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
import io.janusproject2.kernel.hazelcast.HazelcastDistributedDataStructureFactory;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.EventSpaceSpecification;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.core.SpaceSpecification;
import io.sarl.util.OpenEventSpace;

import java.util.Collection;
import java.util.UUID;

import com.google.inject.Injector;

/** Implementation of an agent context in the Janus platform.
 * 
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class Context implements AgentContext{

	private final UUID id;

	private final SpaceRepository spaceRepository;
	
	private final EventSpaceImpl defaultSpace;
	
	/** Constructs a <code>Context</code>.
	 * 
	 * @param injector - injector used to create the instances.
	 * @param id - identifier of the context.
	 * @param defaultSpaceID - identifier of the default space in the context.
	 * @param repositoryImplFactory - object that is creating distributed structures.
	 */
	protected Context(Injector injector, UUID id, UUID defaultSpaceID, HazelcastDistributedDataStructureFactory repositoryImplFactory) {
		this.id = id;
		this.spaceRepository = new SpaceRepository(
				id.toString()+"-spaces", //$NON-NLS-1$
				repositoryImplFactory,
				injector,
				new SpaceListener());
		this.defaultSpace = createSpace(EventSpaceSpecification.class, defaultSpaceID);
	}
	
	@Override
	public UUID getID() {
		return this.id;
	}

	@Override
	public OpenEventSpace getDefaultSpace() {
		return this.defaultSpace;
	}

	@Override
	public Collection<io.sarl.lang.core.Space> getSpaces() {
		return this.spaceRepository.getSpaces();
	}

	@Override
	public <S extends io.sarl.lang.core.Space> S createSpace(Class<? extends SpaceSpecification> spec,
			UUID spaceUUID, Object... creationParams) {
		return this.spaceRepository.createSpace(new SpaceID(this.id, spaceUUID, spec), spec, creationParams);
	}


	@Override
	public <S extends io.sarl.lang.core.Space> S getOrCreateSpace(
			Class<? extends SpaceSpecification> spec, UUID spaceUUID,
			Object... creationParams) {
		return this.spaceRepository.getOrCreateSpace(spec, new SpaceID(this.id, spaceUUID, spec), creationParams);
	}

	/** {@inheritDoc}
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <S extends Space> Collection<S> getSpaces(Class<? extends SpaceSpecification> spec) {
		//Type safety: assume that any ClassCastException will be thrown in the caller context.
		return (Collection<S>) this.spaceRepository.getSpaces(spec);
	}

	@Override
	public <S extends io.sarl.lang.core.Space> S getSpace(UUID spaceUUID) {
		//Type safety: assume that any ClassCastException will be thrown in the caller context.
		return (S) this.spaceRepository.getSpace(
				// The space specification parameter
				// could be null because it will
				// not be used during the search.
				new SpaceID(this.id, spaceUUID, null));
	}
	
	/** Listener on the events in the space repository.
	 * 
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class SpaceListener implements SpaceRepositoryListener {
		
		/**
		 */
		public SpaceListener() {
			//
		}

		/** {@inheritDoc}
		 */
		@Override
		public void spaceCreated(Space space) {
			EventSpace defSpace = getDefaultSpace();
			// Default space may be null if the default space was not
			// yet created by the space repository has already received new spaces.
			if (defSpace!=null) {
				/*
				SpaceCreated event = new SpaceCreated();
				event.setSpaceID(space.getID());
				event.setSpaceSpecification(space.getID().getSpaceSpecification());
				event.setSource(defSpace.getAddress(agent.getID()));
				defSpace.emit(event);*/
			}
		}

		/** {@inheritDoc}
		 */
		@Override
		public void spaceDetroyed(Space space) {
			EventSpace defSpace = getDefaultSpace();
			// Default space may be null if the default space was not
			// yet created by the space repository has already received new spaces.
			if (defSpace!=null) {
				/*
				SpaceDestroyed event = new SpaceDestroyed();
				event.setSpaceID(space.getID());
				event.setSpaceSpecification(space.getID().getSpaceSpecification());
				event.setSource(defSpace.getAddress(agent.getID()));
				defSpace.emit(event);*/
			}
		}
		
	}

}
