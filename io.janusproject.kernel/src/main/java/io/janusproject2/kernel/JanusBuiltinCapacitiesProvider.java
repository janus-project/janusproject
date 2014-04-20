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

import io.janusproject2.services.SpawnService;
import io.janusproject2.services.SpawnServiceListener;
import io.sarl.core.Behaviors;
import io.sarl.core.DefaultContextInteractions;
import io.sarl.core.Destroy;
import io.sarl.core.ExternalContextAccess;
import io.sarl.core.Initialize;
import io.sarl.core.InnerContextAccess;
import io.sarl.core.Lifecycle;
import io.sarl.core.Schedules;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.BuiltinCapacitiesProvider;
import io.sarl.lang.core.Capacity;
import io.sarl.lang.core.Skill;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.google.inject.Inject;
import com.google.inject.Injector;

/** Provider of the built-in capacities of the Janus platform.
 * 
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class JanusBuiltinCapacitiesProvider implements BuiltinCapacitiesProvider {

	private Injector injector;

	private SpawnService spawnService;

	private ContextRepository contextRepository;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Class<? extends Capacity>, Skill> getBuiltinCapacities(Agent agent) {
		Map<Class<? extends Capacity>, Skill> result = new ConcurrentHashMap<>();
		final BehaviorsAndInnerContextSkill_ behaviorsSkill = createBehaviorsSkill(agent);
		final ExternalContextAccessSkill_ externalContextSkill = createExternalContextAccessSkill(agent);
		final LifecycleSkill lifecyleSkill = new LifecycleSkill(agent, this.spawnService);
		final DefaultContextInteractionsSkill_ defaultContextInteractionsSkill = new DefaultContextInteractionsSkill_(agent,
				this.contextRepository.getContext(agent.getParentID()));

		
		final SchedulesSkill schedulesSkill = createSchedulesSkill(agent);
		result.put(InnerContextAccess.class, behaviorsSkill);
		result.put(Behaviors.class, behaviorsSkill);

		result.put(ExternalContextAccess.class, externalContextSkill);
		result.put(Lifecycle.class, lifecyleSkill);
		result.put(DefaultContextInteractions.class, defaultContextInteractionsSkill);
		result.put(Schedules.class,schedulesSkill);

		this.spawnService.addSpawnServiceListener(agent.getID(), new SpawnServiceListener() {
			
			@Override
			public void agentSpawned(AgentContext parent, Agent agentID,
					Object[] initializationParameters) {
				externalContextSkill.install();
				behaviorsSkill.install();
				schedulesSkill.install();
				lifecyleSkill.install();
				defaultContextInteractionsSkill.install();
				
				Initialize init = new Initialize();
				init.setParameters(initializationParameters);
				behaviorsSkill.selfEvent(init);
			}
			
			@Override
			public void agentDestroy(Agent agent) {
				Destroy destroy = new Destroy();
				behaviorsSkill.selfEvent(destroy);
				
				
				defaultContextInteractionsSkill.uninstall();
				lifecyleSkill.uninstall();
				schedulesSkill.uninstall();
				behaviorsSkill.uninstall();
				externalContextSkill.uninstall();
			}
		});

		return result;
	}

	// --- Injections
	
	/** Set the spawning service.
	 * 
	 * @param spawnService
	 */
	@Inject
	void setSpawnService(SpawnService spawnService) {
		this.spawnService = spawnService;
	}
	
	/** Set the context repository.
	 * 
	 * @param contextRepository
	 */
	@Inject
	void setContextRepository(ContextRepository contextRepository) {
		this.contextRepository = contextRepository;
	}

	/** Set the injector.
	 * 
	 * @param injector
	 */
	@Inject
	void setInjector(Injector injector) {
		this.injector = injector;
	}

	private ExternalContextAccessSkill_ createExternalContextAccessSkill(Agent agent) {
		return new ExternalContextAccessSkill_(agent, this.contextRepository);
	}

	private BehaviorsAndInnerContextSkill_ createBehaviorsSkill(Agent agent) {
		final BehaviorsAndInnerContextSkill_ s = new BehaviorsAndInnerContextSkill_(agent);
		this.injector.injectMembers(s);

		return s;
	}

	private SchedulesSkill createSchedulesSkill(Agent agent) {
		final SchedulesSkill s = new SchedulesSkill(agent);
		this.injector.injectMembers(s);
		return s;
	}

}
