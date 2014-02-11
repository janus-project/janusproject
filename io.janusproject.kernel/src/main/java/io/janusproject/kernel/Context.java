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

import io.janusproject.repository.SpaceRepository;
import io.sarl.lang.core.AgentContext;
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
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class Context implements AgentContext{

	private final UUID id;

	
	private SpaceRepository spaceRepository;
	
	private final EventSpaceImpl defaultSpace;

	private Injector injector;

	/** Constructs a <code>Context</code>.
	 * 
	 * @param injector - reference to the injector to be used.
	 * @param id - identifier of the context.
	 * @param defaultSpaceID - identifier of the default space in the context.
	 */
	protected Context(Injector injector, UUID id, UUID defaultSpaceID) {
		this.id = id;
		this.injector = injector;
		this.spaceRepository = new SpaceRepository(id.toString()+"-spaces");
		this.injector.injectMembers(this.spaceRepository);
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
		S space = this.injector.getInstance(spec).create(
				new SpaceID(this.id, spaceUUID,spec), creationParams);
		this.spaceRepository.addSpace(space);
		return space;
	}


	@Override
	public <S extends io.sarl.lang.core.Space> S getOrCreateSpace(
			Class<? extends SpaceSpecification> spec, UUID spaceUUID,
			Object... creationParams) {
		Space s = this.spaceRepository.getFirstSpaceFromSpec(spec);
		if (s != null) {
			return (S) s;
		}
		return createSpace(spec, spaceUUID, creationParams);
	}

	/** {@inheritDoc}
	 */
	@Override
	public <S extends Space> Collection<S> getSpaces(Class<? extends SpaceSpecification> spec) {
		return (Collection<S>) this.spaceRepository.getSpacesFromSpec(spec);
	}

}
