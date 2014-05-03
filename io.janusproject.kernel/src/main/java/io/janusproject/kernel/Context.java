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
package io.janusproject.kernel;

import io.janusproject.kernel.SpaceRepository.SpaceRepositoryListener;
import io.janusproject.services.LogService;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.core.SpaceSpecification;
import io.sarl.util.OpenEventSpace;
import io.sarl.util.OpenEventSpaceSpecification;

import java.util.Collection;
import java.util.UUID;

import com.google.inject.Injector;
import com.hazelcast.core.HazelcastInstance;

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
	
	private final UUID defaultSpaceID;
	private OpenEventSpace defaultSpace;
	
	
	/** Constructs a <code>Context</code>.
	 * <p>
	 * CAUTION: Do not miss to call {@link #postConstruction()}.
	 * 
	 * @param injector - injector used to create the instances.
	 * @param id - identifier of the context.
	 * @param defaultSpaceID - identifier of the default space in the context.
	 * @param logger - instance of the logging service.
	 * @param hzInstance - object that is creating distributed structures.
	 * @param startUpListener - repository listener which is added just after the creation of the repository, but before the creation of the default space.
	 */
	protected Context(Injector injector, UUID id, UUID defaultSpaceID, LogService logger, HazelcastInstance hzInstance, SpaceRepositoryListener startUpListener) {
		this.id = id;
		this.defaultSpaceID = defaultSpaceID;
		this.spaceRepository = new SpaceRepository(
				id.toString()+"-spaces", //$NON-NLS-1$
				hzInstance,
				injector,
				logger,
				new SpaceListener(logger, startUpListener));
	}
	
	@Override
	public String toString() {
		return this.id.toString();
	}
	
	/** Create the default space in this context.
	 * 
	 * @return the created space.
	 */
	EventSpace postConstruction() {
		this.spaceRepository.postConstruction();
		this.defaultSpace = createSpace(OpenEventSpaceSpecification.class, this.defaultSpaceID);
		return this.defaultSpace;
	}
	
	/** Destroy any associated resources.
	 */
	public void destroy() {
		this.spaceRepository.destroy();
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
	public <S extends io.sarl.lang.core.Space> S createSpace(Class<? extends SpaceSpecification<S>> spec,
			UUID spaceUUID, Object... creationParams) {
		return this.spaceRepository.createSpace(new SpaceID(this.id, spaceUUID, spec), spec, creationParams);
	}


	@Override
	public <S extends io.sarl.lang.core.Space> S getOrCreateSpace(
			Class<? extends SpaceSpecification<S>> spec, UUID spaceUUID,
			Object... creationParams) {
		return this.spaceRepository.getOrCreateSpace(spec, new SpaceID(this.id, spaceUUID, spec), creationParams);
	}

	/** {@inheritDoc}
	 */
	@Override
	public <S extends Space> Collection<S> getSpaces(Class<? extends SpaceSpecification<S>> spec) {
		//Type safety: assume that any ClassCastException will be thrown in the caller context.
		return this.spaceRepository.getSpaces(spec);
	}

	@SuppressWarnings("unchecked")
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
		
		private final SpaceRepositoryListener relay;
		private final LogService logger;
		
		/**
		 * @param logger
		 * @param relay
		 */
		public SpaceListener(LogService logger, SpaceRepositoryListener relay) {
			assert(logger!=null);
			assert(relay!=null);
			this.logger = logger;
			this.relay = relay;
		}

		/** {@inheritDoc}
		 */
		@Override
		public void spaceCreated(Space space) {
			this.logger.info(Context.class, "SPACE_CREATED", space.getID()); //$NON-NLS-1$
			
			// Notify the relays (other services)
			this.relay.spaceCreated(space);
			// Put an event in the default space
			EventSpace defSpace = getDefaultSpace();
			// Default space may be null if the default space was not
			// yet created by the space repository has already received new spaces.
			if (defSpace!=null) {
				//FIXME: Caution -> the event should not be fired in remote kernels.
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
		public void spaceDestroyed(Space space) {
			this.logger.info(Context.class, "SPACE_DESTROYED", space.getID()); //$NON-NLS-1$

			// Notify the relays (other services)
			this.relay.spaceDestroyed(space);
			// Put an event in the default space
			EventSpace defSpace = getDefaultSpace();
			// Default space may be null if the default space was not
			// yet created by the space repository has already received new spaces.
			if (defSpace!=null) {
				//FIXME: Caution -> the event should not be fired in remote kernels.
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
