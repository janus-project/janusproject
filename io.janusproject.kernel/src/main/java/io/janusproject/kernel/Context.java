/*
 * $Id$
 * 
 * Janus platform is an open-source multiagent platform.
 * More details on &lt;http://www.janus-project.org&gt;
 * Copyright (C) 2013 Janus Core Developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
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

/**
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class Context implements AgentContext{

	private final UUID id;

	
	private SpaceRepository spaceRepository;
	
	private final EventSpaceImpl defaultSpace;

	private Injector injector;

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
