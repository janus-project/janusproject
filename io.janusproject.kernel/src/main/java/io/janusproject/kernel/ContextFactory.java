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

import io.janusproject.repository.ContextRepository;

import java.util.UUID;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * A Factory to create Agent Contexts
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class ContextFactory {
	@Inject
	private Injector injector;
	
	
	private ContextRepository contextRepository;
	
	public Context create(UUID contextID, UUID defaultSpaceId){

		Context ctx = new Context(this.injector, contextID, defaultSpaceId);
		this.contextRepository.addContext(ctx);
		return ctx;
	}
	
	@Inject
	void setContextRepository(ContextRepository contextRepo){
		this.contextRepository=contextRepo;
	}
	
}
