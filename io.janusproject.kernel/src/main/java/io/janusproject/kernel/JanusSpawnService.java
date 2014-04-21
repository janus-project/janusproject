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
package io.janusproject.kernel;

import io.janusproject.services.KernelAgentSpawnListener;
import io.janusproject.services.SpawnService;
import io.janusproject.services.SpawnServiceListener;
import io.sarl.core.AgentKilled;
import io.sarl.core.AgentSpawned;
import io.sarl.core.ExternalContextAccess;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.EventSpace;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

import org.arakhne.afc.vmutil.locale.Locale;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

/** Implementation of a spawning service.
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
class JanusSpawnService extends AbstractService implements SpawnService {

	private final SpawnServiceListener loopListener = new SpawningEventEmitter();
	private final Multimap<UUID, SpawnServiceListener> lifecycleListeners = ArrayListMultimap.create();
	private final List<KernelAgentSpawnListener> kernelAgentListeners = new ArrayList<>();
	private final Map<UUID, Agent> agents = new TreeMap<>();

	@Inject
	private Injector injector;

	/** {@inheritDoc}
	 */
	@Override
	public synchronized UUID spawn(AgentContext parent, Class<? extends Agent> agentClazz,
			Object... params) {
		if (isRunning()) {
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
	public synchronized void killAgent(UUID agentID) {
		boolean error = !isRunning();
		Agent agent = this.agents.remove(agentID);
		fireAgentDestroyed(agent);
		if (this.agents.isEmpty()) {
			fireKernelAgentDestroy();
		}
		if (error) {
			throw new SpawnServiceStopException(agentID);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public void addKernelAgentSpawnListener(KernelAgentSpawnListener listener) {
		synchronized(this.kernelAgentListeners) {
			this.kernelAgentListeners.add(listener);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public void removeKernelAgentSpawnListener(KernelAgentSpawnListener listener) {
		synchronized(this.kernelAgentListeners) {
			this.kernelAgentListeners.remove(listener);
		}
	}

	/** Notifies the listeners about the kernel agent creation.
	 */
	protected void fireKernelAgentSpawn() {
		KernelAgentSpawnListener[] listeners;
		synchronized (this.kernelAgentListeners) {
			listeners = new KernelAgentSpawnListener[this.kernelAgentListeners.size()];
			this.kernelAgentListeners.toArray(listeners);
		}
		for (KernelAgentSpawnListener l : listeners) {
			l.kernelAgentSpawn();
		}
	}

	/** Notifies the listeners about the kernel agent destruction.
	 */
	protected void fireKernelAgentDestroy() {
		KernelAgentSpawnListener[] listeners;
		synchronized (this.kernelAgentListeners) {
			listeners = new KernelAgentSpawnListener[this.kernelAgentListeners.size()];
			this.kernelAgentListeners.toArray(listeners);
		}
		for (KernelAgentSpawnListener l : listeners) {
			l.kernelAgentDestroy();
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public void addSpawnServiceListener(UUID id,
			SpawnServiceListener agentLifecycleListener) {
		synchronized(this.lifecycleListeners) {
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
		SpawnServiceListener[] listeners;
		synchronized (this.lifecycleListeners) {
			Collection<SpawnServiceListener> list = this.lifecycleListeners.get(agent.getID());
			listeners = new SpawnServiceListener[list.size()];
			list.toArray(listeners);
		}
		for (SpawnServiceListener l : listeners) {
			l.agentSpawned(context, agent, initializationParameters);
		}
		this.loopListener.agentSpawned(context, agent, initializationParameters);
	}

	/** Notifies the listeners about the agent destruction.
	 * 
	 * @param agent
	 */
	protected void fireAgentDestroyed(Agent agent) {
		SpawnServiceListener[] listeners;
		synchronized (this.lifecycleListeners) {
			Collection<SpawnServiceListener> list = this.lifecycleListeners.get(agent.getID());
			listeners = new SpawnServiceListener[list.size()];
			list.toArray(listeners);
		}
		this.loopListener.agentDestroy(agent);
		for (SpawnServiceListener l : listeners) {
			l.agentDestroy(agent);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	protected synchronized void doStart() {
		// Assume that when the service is starting, the kernel agent is up.
		fireKernelAgentSpawn();
		notifyStarted();
	}

	/** {@inheritDoc}
	 */
	@Override
	protected synchronized void doStop() {
		synchronized(this.lifecycleListeners) {
			this.lifecycleListeners.clear();
		}
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

	/** This exception is thrown when the spawning service is not running when
	 * the killing function on an agent is called.
	 * 
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class SpawnServiceStopException extends RuntimeException {

		private static final long serialVersionUID = 8104012713598435249L;

		/**
		 * @param agentID - the identifier of the agent.
		 */
		public SpawnServiceStopException(UUID agentID) {
			super(Locale.getString(JanusSpawnService.class, "KILL_DISABLED", agentID)); //$NON-NLS-1$
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

	/** This class permits to catch any spawning of an agent and emit
	 * an event in the defualt space of the corresponding context.
	 * 
	 * @author $Author: ngaud$
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class SpawningEventEmitter implements SpawnServiceListener {

		private volatile Method skillMethod = null;
		
		/**
		 */
		public SpawningEventEmitter() {
			//
		}

		/** {@inheritDoc}
		 */
		@Override
		public void agentSpawned(AgentContext parentContext, Agent agent, Object[] initializationParameters) {
			EventSpace defSpace = parentContext.getDefaultSpace();
			AgentSpawned event = new AgentSpawned();
			event.setAgentID(agent.getID());
			event.setAgentType(agent.getClass());
			event.setSource(defSpace.getAddress(agent.getID()));
			defSpace.emit(event);
		}

		/** {@inheritDoc}
		 */
		@Override
		public void agentDestroy(final Agent agent) {
			try {
				Method method = this.skillMethod;
				if (method==null) {
					method = Agent.class.getDeclaredMethod("getSkill", Class.class); //$NON-NLS-1$
					method.setAccessible(true);
					this.skillMethod = method;
				}
				ExternalContextAccess skill = (ExternalContextAccess)method.invoke(agent, ExternalContextAccess.class);
				for (AgentContext context : skill.getAllContexts()) {
					EventSpace defSpace = context.getDefaultSpace();
					AgentKilled event = new AgentKilled();
					event.setAgentID(agent.getID());
					event.setSource(defSpace.getAddress(agent.getID()));
					defSpace.emit(event);
				}
			}
			catch(Exception e) {
				throw new Error(e);
			}
		}

	}

}
