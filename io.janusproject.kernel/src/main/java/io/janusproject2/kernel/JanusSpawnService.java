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

import io.janusproject2.services.LogService;
import io.janusproject2.services.SpawnService;
import io.janusproject2.services.SpawnServiceListener;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.arakhne.afc.vmutil.locale.Locale;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.google.inject.Injector;

/** Implementation of a spawning service.
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class JanusSpawnService extends AbstractService implements SpawnService {

	private SpawnServiceListener kernelListener = null;
	private final Multimap<UUID, SpawnServiceListener> lifecycleListeners = ArrayListMultimap.create();
	private final Map<UUID, Agent> agents = new ConcurrentHashMap<>();
	private final AtomicBoolean isAcceptSpawns = new AtomicBoolean(false);

	@Inject
	private LogService logger;

	@Inject
	private Injector injector;

	/** {@inheritDoc}
	 */
	@Override
	public UUID spawn(AgentContext parent, Class<? extends Agent> agentClazz,
			Object... params) {
		if (this.isAcceptSpawns.get()) {
			try {
				Agent agent = agentClazz.getConstructor(UUID.class).newInstance(parent.getID());
				assert(agent!=null);
				this.agents.put(agent.getID(), agent);
				this.injector.injectMembers(agent);
				fireAgentSpawned(parent, agent, params);
				return agent.getID();
			} catch (Throwable e) {
				throw new CannotSpawnException(agentClazz, e);
			}
		}
		throw new SpawnDisabledException(parent.getID(), agentClazz);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void killAgent(UUID agentID) {
		Agent agent = this.agents.remove(agentID);
		fireAgentDestroyed(agent);
		if (this.isAcceptSpawns.getAndSet(this.agents.isEmpty())) {
			this.logger.debug("KERNEL_AGENT_STOPPED"); //$NON-NLS-1$
			this.injector.getInstance(Kernel.class).stop();
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public void addSpawnServiceListener(UUID id,
			SpawnServiceListener agentLifecycleListener) {
		synchronized(this.lifecycleListeners) {
			if (id==null) {
				if (this.kernelListener!=null)
					throw new IllegalArgumentException("id"); //$NON-NLS-1$
				this.kernelListener = agentLifecycleListener;
			}
			this.lifecycleListeners.put(id, agentLifecycleListener);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public void removeSpawnServiceListener(UUID id,
			SpawnServiceListener agentLifecycleListener) {
		synchronized(this.lifecycleListeners) {
			this.lifecycleListeners.remove(id, agentLifecycleListener);
		}
	}

	/** Notifies the listeners about the agent creation.
	 * 
	 * @param context - context in whic hthe agent is spawn.
	 * @param agent - the spawn agent.
	 * @param initializationParameters
	 */
	protected void fireAgentSpawned(AgentContext context, Agent agent, Object[] initializationParameters) {
		synchronized (this.lifecycleListeners) {
			if (this.kernelListener!=null)
				this.kernelListener.agentSpawned(context, agent, initializationParameters);
			for (SpawnServiceListener l : this.lifecycleListeners.get(agent.getID())) {
				l.agentSpawned(context, agent, initializationParameters);
			}
		}
	}

	/** Notifies the listeners about the agent destruction.
	 * 
	 * @param agent
	 */
	protected void fireAgentDestroyed(Agent agent) {
		synchronized (this.lifecycleListeners) {
			if (this.kernelListener!=null)
				this.kernelListener.agentDestroy(agent);
			for (SpawnServiceListener l : this.lifecycleListeners.get(agent.getID())) {
				l.agentDestroy(agent);
			}
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void doStart() {
		notifyStarted();
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void doStop() {
		this.isAcceptSpawns.set(true);
		this.lifecycleListeners.clear();
		this.kernelListener = null;
		notifyStopped();
	}

	/** This exception is thrown when the spawning service of agents is disabled.
	 * 
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class SpawnDisabledException extends RuntimeException {

		private static final long serialVersionUID = -380402400888610762L;

		/**
		 * @param parentID - the identifier of the parent entity that is creating the agent.
		 * @param agentClazz - the type of the agent to spawn.
		 */
		public SpawnDisabledException(UUID parentID, Class<? extends Agent> agentClazz) {
			super(Locale.getString(JanusSpawnService.class, "SPAWN_DISABLED", parentID, agentClazz)); //$NON-NLS-1$
		}

	}

	/** This exception is thrown when an agent cannot be spawned.
	 * 
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class CannotSpawnException extends RuntimeException {

		private static final long serialVersionUID = -380402400888610762L;

		/**
		 * @param agentClazz - the type of the agent to spawn.
		 * @param cause - the cause of the exception.
		 */
		public CannotSpawnException(Class<? extends Agent> agentClazz, Throwable cause) {
			super(Locale.getString(JanusSpawnService.class, "CANNOT_INSTANCIATE_AGENT", agentClazz), cause); //$NON-NLS-1$
		}

	}

}
