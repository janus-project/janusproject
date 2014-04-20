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

import io.janusproject2.kernel.hazelcast.HazelcastDistributedDataStructureFactory;

import java.util.UUID;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * A factory to create agent contexts.
 * 
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class ContextFactory {

	@Inject
	private Injector injector;
	
	@Inject
	private HazelcastDistributedDataStructureFactory repositoryImplFactory;

	private ContextRepository_ contextRepository;

	/**
	 * Create a context.
	 * 
	 * @param contextID - identifier of the context.
	 * @param defaultSpaceId - identifier of the default space in the context.
	 * @return the context.
	 */
	public Context create(UUID contextID, UUID defaultSpaceId) {
		Context ctx = new Context(this.injector, contextID, defaultSpaceId, this.repositoryImplFactory);
		this.contextRepository.addContext(ctx);
		return ctx;
	}

	/**
	 * Set the context repository of the platform.
	 * 
	 * @param contextRepo - reference to the context repository to use to store the contexts.
	 */
	@Inject
	void setContextRepository(ContextRepository_ contextRepo) {
		this.contextRepository = contextRepo;
	}

}
