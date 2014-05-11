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

import io.janusproject.services.ContextSpaceService;
import io.janusproject.services.ExecutorService;
import io.janusproject.services.SpawnService;
import io.janusproject.services.impl.IServiceManager;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.State;
import com.hazelcast.core.HazelcastInstance;


/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc","unchecked","rawtypes"})
public class KernelTest extends Assert {

	private ImmutableMultimap<State,Service> services;
	private SpawnService spawnService;
	private ExecutorService executorService;
	private ContextSpaceService contextService;
	private HazelcastInstance hzInstance;
	private IServiceManager serviceManager;
	private UncaughtExceptionHandler exceptionHandler;
	private AgentContext agentContext;
	private Kernel kernel;
	private UUID uuid;
	
	@Before
	public void setUp() {
		this.uuid = UUID.randomUUID();
		this.spawnService = Mockito.mock(SpawnService.class);
		this.executorService = Mockito.mock(ExecutorService.class);
		this.contextService = Mockito.mock(ContextSpaceService.class);
		this.services = ImmutableMultimap.of(
				State.RUNNING, this.spawnService,
				State.RUNNING, this.executorService,
				State.RUNNING, this.contextService);
		this.hzInstance = Mockito.mock(HazelcastInstance.class);
		this.agentContext = Mockito.mock(AgentContext.class);
		this.exceptionHandler = Mockito.mock(UncaughtExceptionHandler.class);
		this.serviceManager = Mockito.mock(IServiceManager.class);
		//
		Mockito.when(this.spawnService.isRunning()).thenReturn(true);
		Mockito.when(this.spawnService.state()).thenReturn(State.RUNNING);
		Mockito.when(this.spawnService.spawn(Matchers.any(AgentContext.class), Matchers.any(Class.class), Matchers.anyString(), Matchers.anyString())).thenReturn(this.uuid);
		Mockito.when(this.executorService.isRunning()).thenReturn(true);
		Mockito.when(this.executorService.state()).thenReturn(State.RUNNING);
		Mockito.when(this.contextService.isRunning()).thenReturn(true);
		Mockito.when(this.contextService.state()).thenReturn(State.RUNNING);
		Mockito.when(this.serviceManager.servicesByState()).thenReturn(this.services);
		//
		this.kernel = new Kernel(this.serviceManager, this.spawnService, this.hzInstance, this.exceptionHandler);
		this.kernel = Mockito.spy(this.kernel);
	}
	
	@After
	public void tearDown() {
		this.kernel = null;
		this.agentContext = null;
		this.serviceManager = null;
		this.hzInstance = null;
		this.spawnService = null;
		this.executorService = null;
		this.contextService = null;
		this.exceptionHandler = null;
		this.services = null;
		this.uuid = null;
	}

	@Test
	public void getService() {
		assertSame(this.spawnService, this.kernel.getService(SpawnService.class));
		assertSame(this.executorService, this.kernel.getService(ExecutorService.class));
		assertSame(this.contextService, this.kernel.getService(ContextSpaceService.class));
	}

	@Test
	public void spawn() {
		this.kernel.setJanusContext(this.agentContext);
		//
		UUID id = this.kernel.spawn(Agent.class, "a", "b"); //$NON-NLS-1$//$NON-NLS-2$
		assertSame(this.uuid, id);
		ArgumentCaptor<AgentContext> argument1 = ArgumentCaptor.forClass(AgentContext.class);
		ArgumentCaptor<Class> argument2 = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<String> argument3 = ArgumentCaptor.forClass(String.class);
		ArgumentCaptor<String> argument4 = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.spawnService).spawn(
				argument1.capture(), argument2.capture(),
				argument3.capture(), argument4.capture());
		assertSame(this.agentContext, argument1.getValue());
		assertEquals(Agent.class, argument2.getValue());
		assertEquals("a", argument3.getValue()); //$NON-NLS-1$
		assertEquals("b", argument4.getValue()); //$NON-NLS-1$
	}

}
