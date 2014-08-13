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
package io.janusproject.services.infrastructure;

import com.google.common.util.concurrent.Service;

/** This class supports the management of the infrastructure as
 * a service for the Janus platform.
 * <p>
 * <strong>All the other services must depends on
 * this service</strong>.
 * <p>
 * The tasks that are done by this service are low-level and must
 * not depend on other services.
 * <p>
 * This service is used for released any resource that is shared
 * by several other services. For example, Hazelcast instance
 * may be release by a specific implementation of this service.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public interface InfrastructureService extends Service {
	//
}
