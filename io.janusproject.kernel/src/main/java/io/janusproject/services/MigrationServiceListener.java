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
import io.sarl.lang.core.AgentContext;

import java.util.EventListener;

/** Listener on events related to the migration of an agent.
 *  
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public interface MigrationServiceListener extends EventListener {

	/** Invoked on the target kernel when the agent has migrated.
	 *
	 * @param parent - the context in which the agent was created.
	 * @param agentID - the agent.
	 * @param initializationParameters - list of parameters that were passed to the agent.
	 */
	public void agentArrival(AgentContext parent, Agent agentID, Object[] initializationParameters);
	
	/** Invoked on the source kernel when the agent has migrated.
	 * @param agent - the agent.
	 */
	public void agentDeparture(Agent agent);
	
}
