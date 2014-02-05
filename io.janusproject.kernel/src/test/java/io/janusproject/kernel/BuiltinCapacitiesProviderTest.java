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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.janusproject.repository.ContextRepository;
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
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class BuiltinCapacitiesProviderTest {
	private BuiltinCapacitiesProviderImpl builtinProvider;
	private Map<Class<? extends Capacity>, Skill> builtins;

	@Mock
	private SpawnService spawnService;

	@Mock
	private ContextRepository contextRepository;
	
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
