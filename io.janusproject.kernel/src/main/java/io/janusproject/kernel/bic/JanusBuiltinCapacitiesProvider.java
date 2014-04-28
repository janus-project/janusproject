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
package io.janusproject.kernel.bic;

import io.janusproject.services.ContextSpaceService;
import io.janusproject.services.SpawnService;
import io.janusproject.services.SpawnServiceListener;
import io.sarl.core.Behaviors;
import io.sarl.core.DefaultContextInteractions;
import io.sarl.core.Destroy;
import io.sarl.core.ExternalContextAccess;
import io.sarl.core.Initialize;
import io.sarl.core.InnerContextAccess;
import io.sarl.core.Lifecycle;
import io.sarl.core.Schedules;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.BuiltinCapacitiesProvider;
import io.sarl.lang.core.Capacity;
import io.sarl.lang.core.Skill;
import io.sarl.lang.core.SpaceID;
import io.sarl.util.OpenEventSpaceSpecification;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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

	@Inject
	private Injector injector;

	@Inject
	private SpawnService spawnService;

	@Inject
	private ContextSpaceService contextRepository;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<Class<? extends Capacity>, Skill> getBuiltinCapacities(Agent agent) {
		Map<Class<? extends Capacity>, Skill> result = new HashMap<>(); // no need to be synchronized

		UUID innerContextID = agent.getID();
		SpaceID innerSpaceID = new SpaceID(
				innerContextID,
				UUID.randomUUID(),
				OpenEventSpaceSpecification.class);
		Address agentAddress = new Address(innerSpaceID, agent.getID());
		
		
		EventBusSkill eventBusSkill = new EventBusSkill(agent, agentAddress);		
		InnerContextSkill innerContextSkill = new InnerContextSkill(agent, agentAddress);
		BehaviorsSkill behaviorSkill = new BehaviorsSkill(agent);
		LifecycleSkill lifecycleSkill = new LifecycleSkill(agent);
		ExternalContextAccessSkill externalContextSkill = new ExternalContextAccessSkill(agent);
		DefaultContextInteractionsSkill interactionSkill = new DefaultContextInteractionsSkill(agent, this.contextRepository.getContext(agent.getParentID()));
		SchedulesSkill scheduleSkill = new SchedulesSkill(agent);

		this.injector.injectMembers(eventBusSkill);
		this.injector.injectMembers(innerContextSkill);
		this.injector.injectMembers(behaviorSkill);
		this.injector.injectMembers(lifecycleSkill);
		this.injector.injectMembers(externalContextSkill);
		this.injector.injectMembers(interactionSkill);
		this.injector.injectMembers(scheduleSkill);
		
		result.put(EventBusCapacity.class, eventBusSkill);
		result.put(InnerContextAccess.class, innerContextSkill);
		result.put(Behaviors.class, behaviorSkill);
		result.put(Lifecycle.class, lifecycleSkill);
		result.put(ExternalContextAccess.class, externalContextSkill);
		result.put(DefaultContextInteractions.class, interactionSkill);
		result.put(Schedules.class, scheduleSkill);
		
		this.spawnService.addSpawnServiceListener(agent.getID(),
				new SkillInstaller(
						agent.getID(),
						this.spawnService,
						eventBusSkill,
						innerContextSkill,
						behaviorSkill,
						lifecycleSkill,
						externalContextSkill,
						interactionSkill,
						scheduleSkill));

		// Test if all the BICs are installed.
		assert(result.get(Behaviors.class)!=null);
		assert(result.get(DefaultContextInteractions.class)!=null);
		assert(result.get(EventBusCapacity.class)!=null);
		assert(result.get(ExternalContextAccess.class)!=null);
		assert(result.get(InnerContextAccess.class)!=null);
		assert(result.get(Lifecycle.class)!=null);
		assert(result.get(Schedules.class)!=null);

		return result;
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class SkillInstaller implements SpawnServiceListener {

		private final UUID agentID;
		private final WeakReference<SpawnService> spawnService;
		private final EventBusCapacity eventBusCapacity;
		private final Skill[] skills;
		
		/**
		 * @param agentId
		 * @param spawnService
		 * @param eventBusCapacity
		 * @param skills
		 */
		public SkillInstaller(UUID agentId, SpawnService spawnService, EventBusCapacity eventBusCapacity, Skill... skills) {
			this.agentID = agentId;
			this.spawnService = new WeakReference<>(spawnService);
			this.eventBusCapacity = eventBusCapacity;
			this.skills = skills;
		}

		@Override
		public void agentSpawned(AgentContext parent, Agent agentID,
				Object[] initializationParameters) {
			try {
				Method m = Skill.class.getDeclaredMethod("install"); //$NON-NLS-1$
				boolean isAccessible = m.isAccessible();
				try {
					m.setAccessible(true);
					m.invoke(this.eventBusCapacity);
					for(Skill s : this.skills) {
						m.invoke(s);
					}
				}
				finally {
					m.setAccessible(isAccessible);
				}
			}
			catch(RuntimeException e) {
				throw e;
			}
			catch(Exception e) {
				throw new RuntimeException(e);
			}
			
			Initialize init = new Initialize();
			init.setParameters(initializationParameters);
			this.eventBusCapacity.selfEvent(init);
		}

		@Override
		public void agentDestroy(Agent agent) {
			SpawnService service = this.spawnService.get();
			assert(service!=null);
			service.removeSpawnServiceListener(this.agentID, this);
			
			Destroy destroy = new Destroy();
			this.eventBusCapacity.selfEvent(destroy);

			try {
				Method m = Skill.class.getDeclaredMethod("uninstall"); //$NON-NLS-1$
				boolean isAccessible = m.isAccessible();
				try {
					m.setAccessible(true);
					for(int i=this.skills.length-1; i>=0; i--) {
						m.invoke(this.skills[i]);
					}
					m.invoke(this.eventBusCapacity);
				}
				finally {
					m.setAccessible(isAccessible);
				}
			}
			catch(RuntimeException e) {
				throw e;
			}
			catch(Exception e) {
				throw new RuntimeException(e);
			}
		}
		
	}

}
