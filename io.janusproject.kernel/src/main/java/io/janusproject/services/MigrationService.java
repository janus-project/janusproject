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

import io.sarl.lang.core.Agent;

import java.net.URI;
import java.util.Collection;

import com.google.common.base.Predicate;

/** This class enables the Janus kernel to support migration of
 * agents other the network.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public interface MigrationService extends DependentService {

	/** Tries to build a list of kernels that are matching the
	 * given migration predicate.
	 * 
	 * @param condition - the condition to apply to the remote kernels.
	 * @return the list of kernels that may apply the given condition.
	 */
	public Collection<URI> findKernels(Predicate<?> condition);
	
	/** Migrate the agent.
	 * 
	 * @param agent - the agent that should migrate.
	 * @param kernel - the address of the kernel that should receive the agent.
	 */
	public void migrateTo(Agent agent, URI kernel);

	/** Add a listener on the migration of an agent.
	 * 
	 * @param listener
	 */
	public void addSpawnServiceListener(MigrationServiceListener listener);

	/** Remove a listener on the migration of an agent.
	 * 
	 * @param listener
	 */
	public void removeMigrationServiceListener(MigrationServiceListener listener);
	
}
