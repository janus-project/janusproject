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

import io.janusproject.kernel.JanusSpawnService.AgentFactory;
import io.janusproject.services.KernelAgentSpawnListener;
import io.janusproject.services.SpawnService.AgentKillException;
import io.janusproject.services.SpawnServiceListener;
import io.sarl.core.AgentKilled;
import io.sarl.core.AgentSpawned;
import io.sarl.core.ExternalContextAccess;
import io.sarl.core.InnerContextAccess;
import io.sarl.core.SynchronizedSet;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Capacity;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventSpace;
import io.sarl.util.Collections3;
import io.sarl.util.OpenEventSpace;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;

import com.google.inject.Injector;


/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc","unchecked","synthetic-access"})
public class JanusSpawnServiceTest extends Assert {

	private UUID agentId;
	
	@Mock
	private ExternalContextAccess contextAccess;
	
	@Mock
	private InnerContextAccess innerAccess;

	@Mock
	private AgentContext innerContext;

	@Mock
	private AgentContext agentContext;
	
	@Mock
	private EventSpace defaultSpace;

	@Mock
	private KernelAgentSpawnListener kernelListener;
	
	@Mock
	private SpawnServiceListener serviceListener;
	
	private Agent agent;
	
	private OpenEventSpace innerSpace;

	@Mock
	private AgentFactory agentFactory;

	@Mock
	private Injector injector;
	
	@InjectMocks
	private JanusSpawnService spawnService;
	
	@Before
	public void setUp() throws Exception {
		this.agentId = UUID.randomUUID();
		this.agent = Mockito.spy(new Agent(UUID.randomUUID()) {
			@Override
			protected <S extends Capacity> S getSkill(Class<S> capacity) {
				if (ExternalContextAccess.class.equals(capacity))
					return capacity.cast(JanusSpawnServiceTest.this.contextAccess);
				return capacity.cast(JanusSpawnServiceTest.this.innerAccess);
			}
		});
		MockitoAnnotations.initMocks(this);
		Mockito.when(this.contextAccess.getAllContexts()).thenReturn(Collections3.synchronizedCollection(Collections.singleton(this.agentContext), this));
		Mockito.when(this.innerAccess.getInnerContext()).thenReturn(this.innerContext);
		this.innerSpace = Mockito.mock(OpenEventSpace.class);
		Mockito.when(this.innerSpace.getParticipants()).thenReturn(Collections.singleton(this.agentId));
		Mockito.when(this.innerContext.getDefaultSpace()).thenReturn(this.innerSpace);
		Mockito.when(this.agentContext.getDefaultSpace()).thenReturn(this.defaultSpace);
		Mockito.when(this.defaultSpace.getAddress(Matchers.any(UUID.class))).thenReturn(Mockito.mock(Address.class));
		Mockito.when(this.agentFactory.newInstance(Matchers.any(Class.class), Matchers.any(UUID.class), Matchers.any(Injector.class))).thenReturn(this.agent);
		Mockito.when(this.agent.getID()).thenReturn(this.agentId);
		this.spawnService.addKernelAgentSpawnListener(this.kernelListener);
		this.spawnService.addSpawnServiceListener(this.serviceListener);
		this.spawnService.setAgentFactory(this.agentFactory);
	}
	
	@After
	public void tearDown() {
		this.agentId = null;
		this.injector = null;
		this.spawnService = null;
		this.kernelListener = null;
		this.agent = null;
		this.agentContext = null;
		this.agentFactory = null;
		this.defaultSpace = null;
		this.serviceListener = null;
		this.contextAccess = null;
		this.innerAccess = null;
		this.innerSpace = null;
	}

	@Test
	public void getAgents() {
		SynchronizedSet<UUID> agents = this.spawnService.getAgents();
		assertNotNull(agents);
		assertTrue(agents.isEmpty());
	}

	@Test
	public void spawn() {
		this.spawnService.startAsync().awaitRunning();
		//
		UUID agentId = this.spawnService.spawn(this.agentContext, Agent.class, "a", "b");  //$NON-NLS-1$//$NON-NLS-2$
		//
		assertNotNull(agentId);
		Set<UUID> agents = this.spawnService.getAgents();
		assertEquals(1, agents.size());
		assertTrue(agents.contains(agentId));
		assertSame(this.agentId, agentId);
		//
		ArgumentCaptor<AgentContext> argument1 = ArgumentCaptor.forClass(AgentContext.class);
		ArgumentCaptor<Agent> argument2 = ArgumentCaptor.forClass(Agent.class);
		ArgumentCaptor<Object[]> argument3 = ArgumentCaptor.forClass(Object[].class);
		Mockito.verify(this.serviceListener, new Times(1)).agentSpawned(
				argument1.capture(), argument2.capture(), argument3.capture());
		assertSame(this.agentContext, argument1.getValue());
		Agent ag = argument2.getValue();
		assertNotNull(ag);
		assertSame(agentId, ag.getID());
		assertEquals("a", argument3.getValue()[0]); //$NON-NLS-1$
		assertEquals("b", argument3.getValue()[1]); //$NON-NLS-1$
		//
		ArgumentCaptor<Event> argument4 = ArgumentCaptor.forClass(Event.class);
		Mockito.verify(this.defaultSpace, new Times(1)).emit(argument4.capture());
		assertTrue(argument4.getValue() instanceof AgentSpawned);
		assertSame(agentId, ((AgentSpawned)argument4.getValue()).getAgentID());
		assertEquals(ag.getClass().getName(), ((AgentSpawned)argument4.getValue()).getAgentType());
	}

	@Test
	public void canKillAgent_oneagentinsideinnercontext() {
		Set<UUID> agIds = new HashSet<>();
		Mockito.when(this.defaultSpace.getParticipants()).thenReturn(agIds);
		this.spawnService.startAsync().awaitRunning();
		UUID agentId = this.spawnService.spawn(this.agentContext, Agent.class, "a", "b");  //$NON-NLS-1$//$NON-NLS-2$
		agIds.add(agentId);
		Agent ag = this.spawnService.getAgent(agentId);
		assertNotNull(ag);
		assertSame(this.agent, ag);
		//
		assertTrue(this.spawnService.canKillAgent(ag));
	}
	
	@Test
	public void canKillAgent_twoagentsinsideinnercontext() {
		Mockito.when(this.innerSpace.getParticipants()).thenReturn(
				new HashSet<>(Arrays.asList(this.agentId, UUID.randomUUID())));
		Set<UUID> agIds = new HashSet<>();
		Mockito.when(this.defaultSpace.getParticipants()).thenReturn(agIds);
		this.spawnService.startAsync().awaitRunning();
		UUID agentId = this.spawnService.spawn(this.agentContext, Agent.class, "a", "b");  //$NON-NLS-1$//$NON-NLS-2$
		agIds.add(agentId);
		Agent ag = this.spawnService.getAgent(agentId);
		assertNotNull(ag);
		assertSame(this.agent, ag);
		//
		assertFalse(this.spawnService.canKillAgent(ag));
	}

	@Test
	public void killAgent() throws AgentKillException {
		this.spawnService.startAsync().awaitRunning();
		UUID agentId = this.spawnService.spawn(this.agentContext, Agent.class, "a", "b");  //$NON-NLS-1$//$NON-NLS-2$
		Agent ag = this.spawnService.getAgent(agentId);
		assertNotNull(ag);
		//
		this.spawnService.killAgent(agentId);
		//
		Set<UUID> agents = this.spawnService.getAgents();
		assertTrue(agents.isEmpty());
		//
		ArgumentCaptor<Agent> argument4 = ArgumentCaptor.forClass(Agent.class);
		Mockito.verify(this.serviceListener, new Times(1)).agentDestroy(argument4.capture());
		assertSame(ag, argument4.getValue());
		//
		ArgumentCaptor<Event> argument5 = ArgumentCaptor.forClass(Event.class);
		Mockito.verify(this.defaultSpace, new Times(2)).emit(argument5.capture());
		assertTrue(argument5.getValue() instanceof AgentKilled);
		assertEquals(agentId, ((AgentKilled)argument5.getValue()).getAgentID());
		//
		Mockito.verify(this.kernelListener, new Times(1)).kernelAgentDestroy();
	}

	@Test
	public void doStart() {
		try {
			this.spawnService.doStart();
			fail("Expecting IllegalStateException"); //$NON-NLS-1$
		}
		catch(IllegalStateException _) {
			// Expected excpetion fired by notifyStarted()
		}
		Mockito.verify(this.kernelListener, new Times(1)).kernelAgentSpawn();
	}
	
}
