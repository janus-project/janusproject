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
package io.janusproject2.kernel.services;

import io.sarl.lang.core.Agent;

import java.util.UUID;

/** This service provides the tools to manage
 * the life-cycle of the agents.
 * 
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public interface SpawnService extends JanusService {

	/** Spawn an agent of the given type, and pass the parameters to
	 * its initialization function.
	 * 
	 * @param parentID - the identifier of the parent entity that is creating the agent.
	 * @param agentClazz - the type of the agent to spawn.
	 * @param params - the list of the parameters to pass to the agent initialization function.
	 * @return the identifier of the agent.
	 */
	public UUID spawn(UUID parentID, Class<? extends Agent> agentClazz, Object... params);

	/** Kill the agent with the given identifier.
	 * 
	 * @param agentID - the identifier of the agent to kill.
	 */
	public void killAgent(UUID agentID);

	/** Add a listener on the changes in the current state of an agent.
	 * 
	 * @param id - identifier of the agent.
	 * @param agentLifecycleListener
	 */
	public void addAgentLifecycleListener(UUID id, SpawnServiceListener agentLifecycleListener);

	/** Remove a listener on the changes in the current state of an agent.
	 * 
	 * @param id - identifier of the agent.
	 * @param agentLifecycleListener
	 */
	public void removeAgentLifecycleListener(UUID id, SpawnServiceListener agentLifecycleListener);

}
