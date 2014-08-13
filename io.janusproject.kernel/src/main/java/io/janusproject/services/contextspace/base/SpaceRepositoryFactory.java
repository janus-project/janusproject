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
package io.janusproject.services.contextspace.base;

import io.janusproject.services.contextspace.SpaceRepositoryListener;

/** Factory for the space repository in a context.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
interface SpaceRepositoryFactory {

	/** Create an instance of the space repository.
	 *
	 * @param context - the owner of the space repository.
	 * @param distributedSpaceSetName - name of the space repository data-structure that is
	 *                                  shared among the computer network.
	 * @param listener - listener on space repository events that must be registered
	 *                   at the creation time.
	 * @return the new repository
	 */
	SpaceRepository newInstance(Context context, String distributedSpaceSetName, SpaceRepositoryListener listener);

}
