/*
 * $Id$
 * 
 * Janus platform is an open-source multiagent platform.
 * More details on &lt;http://www.janusproject.io&gt;
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
import io.sarl.core.Behaviors;
import io.sarl.core.DefaultContextInteractions;
import io.sarl.core.Destroy;
import io.sarl.core.ExternalContextAccess;
import io.sarl.core.Initialize;
import io.sarl.core.InnerContextAccess;
import io.sarl.core.Lifecycle;
import io.sarl.core.Schedules;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.BuiltinCapacitiesProvider;
import io.sarl.lang.core.Capacity;
import io.sarl.lang.core.Skill;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class BuiltinCapacitiesProviderImpl implements BuiltinCapacitiesProvider {

	private Injector injector;

	private SpawnService spawnService;

	
	private ContextRepository contextRepository;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Class<? extends Capacity>, Skill> getBuiltinCapacities(Agent agent) {
		Map<Class<? extends Capacity>, Skill> result = new ConcurrentHashMap<>();
		final BehaviorsAndInnerContextSkill behaviorsSkill = createBehaviorsSkill(agent);
		final ExternalContextAccessSkill externalContextSkill = createExternalContextAccessSkill(agent);
		final LifecycleSkill lifecyleSkill = new LifecycleSkill(agent, this.spawnService);
		final DefaultContextInteractionsImpl defaultContextInteractionsSkill = new DefaultContextInteractionsImpl(agent,
				this.contextRepository.getContext(agent.getParentID()));

		
		final SchedulesSkill schedulesSkill = createSchedulesSkill(agent);
		result.put(InnerContextAccess.class, behaviorsSkill);
		result.put(Behaviors.class, behaviorsSkill);

		result.put(ExternalContextAccess.class, externalContextSkill);
		result.put(Lifecycle.class, lifecyleSkill);
		result.put(DefaultContextInteractions.class, defaultContextInteractionsSkill);
		result.put(Schedules.class,schedulesSkill);

		this.spawnService.addAgentLifecycleListener(agent.getID(), new AgentLifecycleListener() {

			@Override
			public void agentDestroy() {
				Destroy destroy = new Destroy();
				behaviorsSkill.selfEvent(destroy);
				
				
				defaultContextInteractionsSkill.uninstall();
				lifecyleSkill.uninstall();
				schedulesSkill.uninstall();
				behaviorsSkill.uninstall();
				externalContextSkill.uninstall();
				
				
			}


			@Override
			public void agentSpawned(Object[] initializationParameters) {
				externalContextSkill.install();
				behaviorsSkill.install();
				schedulesSkill.install();
				lifecyleSkill.install();
				defaultContextInteractionsSkill.install();
				
				Initialize init = new Initialize();
				init.setParameters(initializationParameters);
				behaviorsSkill.selfEvent(init);
			}
		});

		return result;
	}

	// --- Injections
	@Inject
	void setSpawnService(SpawnService spawnService) {
		this.spawnService = spawnService;
	}
	
	@Inject
	void setContextRepository(ContextRepository contextRepository) {
		this.contextRepository = contextRepository;
	}


	@Inject
	void setInjector(Injector injector) {
		this.injector = injector;
	}

	private ExternalContextAccessSkill createExternalContextAccessSkill(Agent agent) {
		return new ExternalContextAccessSkill(agent, this.contextRepository);
	}

	private BehaviorsAndInnerContextSkill createBehaviorsSkill(Agent agent) {
		final BehaviorsAndInnerContextSkill s = new BehaviorsAndInnerContextSkill(agent);
		this.injector.injectMembers(s);

		return s;
	}

	private SchedulesSkill createSchedulesSkill(Agent agent) {
		final SchedulesSkill s = new SchedulesSkill(agent);
		this.injector.injectMembers(s);
		return s;
	}

}
