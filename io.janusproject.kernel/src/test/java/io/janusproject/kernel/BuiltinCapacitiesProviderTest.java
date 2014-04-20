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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.sarl.core.Behaviors;
import io.sarl.core.DefaultContextInteractions;
import io.sarl.core.ExternalContextAccess;
import io.sarl.core.InnerContextAccess;
import io.sarl.core.Lifecycle;
import io.sarl.core.Schedules;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Capacity;
import io.sarl.lang.core.Skill;

import java.util.Map;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.inject.Injector;

/**
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class BuiltinCapacitiesProviderTest {
	private BuiltinCapacitiesProviderImpl builtinProvider;
	private Map<Class<? extends Capacity>, Skill> builtins;

	@Mock
	private SpawnService spawnService;

	@Mock
	private ContextRepository_ contextRepository;
	
	@Mock
	private Agent agent;

	@Mock
	private Injector injector;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.builtinProvider = new BuiltinCapacitiesProviderImpl();
		this.builtinProvider.setSpawnService(this.spawnService);
		this.builtinProvider.setInjector(this.injector);
		this.builtinProvider.setContextRepository(this.contextRepository);
		UUID id = UUID.randomUUID();
		
		when(this.contextRepository.getContext(id)).thenReturn(mock(AgentContext.class));
		
		
		when(this.agent.getID()).thenReturn(id);
		AgentContext ac = mock(AgentContext.class);
		when(ac.getDefaultSpace()).thenReturn(mock(EventSpaceImpl.class));
		this.builtins = this.builtinProvider.getBuiltinCapacities(this.agent);

	}

	@Test
	public void listsBuiltinCapacities() {
		assertTrue(this.builtins.keySet().contains(InnerContextAccess.class));
		assertTrue(this.builtins.keySet().contains(ExternalContextAccess.class));

	}

	@Test
	public void lifecycle() {
		assertTrue(this.builtins.keySet().contains(Lifecycle.class));
		assertNotNull(this.builtins.get(Lifecycle.class));

	}

	@Test
	public void behaviors() {
		assertTrue(this.builtins.keySet().contains(Behaviors.class));
		assertNotNull(this.builtins.get(Behaviors.class));
	}

	@Test
	public void schedules() {
		assertTrue(this.builtins.keySet().contains(Schedules.class));
		assertNotNull(this.builtins.get(Schedules.class));
	}

	@Test
	public void defaultContextInterations() {
		assertTrue(this.builtins.keySet().contains(DefaultContextInteractions.class));
		assertNotNull(this.builtins.get(DefaultContextInteractions.class));
	}

}
