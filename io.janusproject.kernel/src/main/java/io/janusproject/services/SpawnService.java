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

import java.util.UUID;

import com.google.common.util.concurrent.Service;

/** This service provides the tools to manage
 * the life-cycle of the agents.
 * 
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public interface SpawnService extends Service {

	/** Spawn an agent of the given type, and pass the parameters to
	 * its initialization function.
	 * 
	 * @param parent - the parent entity that is creating the agent.
	 * @param agentClazz - the type of the agent to spawn.
	 * @param params - the list of the parameters to pass to the agent initialization function.
	 * @return the identifier of the agent, never <code>null</code>.
	 */
	public UUID spawn(AgentContext parent, Class<? extends Agent> agentClazz, Object... params);

	/** Kill the agent with the given identifier.
	 * 
	 * @param agentID - the identifier of the agent to kill.
	 */
	public void killAgent(UUID agentID);

	/** Add a listener on the changes in the current state of an agent.
	 * 
	 * @param id - identifier of the context.
	 * @param agentLifecycleListener
	 */
	public void addSpawnServiceListener(UUID id, SpawnServiceListener agentLifecycleListener);

	/** Remove a listener on the changes in the current state of an agent.
	 * 
	 * @param id - identifier of the context.
	 * @param agentLifecycleListener
	 */
	public void removeSpawnServiceListener(UUID id, SpawnServiceListener agentLifecycleListener);
	
	/** Add a listener on the changes related to the kernel agent.
	 * 
	 * @param listener
	 */
	public void addKernelAgentSpawnListener(KernelAgentSpawnListener listener);

	/** Remove a listener on the changes related to the kernel agent.
	 * 
	 * @param listener
	 */
	public void removeKernelAgentSpawnListener(KernelAgentSpawnListener listener);

}
