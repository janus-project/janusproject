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

import io.janusproject.kernel.executor.ChuckNorrisException;
import io.janusproject.services.agentplatform.SpawnService;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;

import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;

/**
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc","rawtypes","unchecked"})
public class LifecycleSkillTest extends Assert {

	private UUID agentId;
	
	@Mock
	private SpawnService spawnService;
	
	@Mock
	private Agent agent;

	@InjectMocks
	private LifecycleSkill skill;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.agentId = UUID.randomUUID();
		Mockito.when(this.agent.getID()).thenReturn(this.agentId);
	}

	@After
	public void tearDown() throws Exception {
		this.spawnService = null;
		this.agent = null;
		this.skill = null;
		this.agentId = null;
	}

	@Test
	public void spawnInContext() {
		Class type = Agent.class;
		AgentContext context = Mockito.mock(AgentContext.class);
		Object[] params = new Object[] {1,"String"}; //$NON-NLS-1$
		this.skill.spawnInContext(type, context, params);
		ArgumentCaptor<AgentContext> argument1 = ArgumentCaptor.forClass(AgentContext.class);
		ArgumentCaptor<Class> argument2 = ArgumentCaptor.forClass(Class.class);
		ArgumentCaptor<Integer> argument3 = ArgumentCaptor.forClass(Integer.class);
		ArgumentCaptor<String> argument4 = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.spawnService, new Times(1)).spawn(argument1.capture(), argument2.capture(), argument3.capture(), argument4.capture());
		assertSame(context, argument1.getValue());
		assertEquals(Agent.class, argument2.getValue());
		assertEquals(1, argument3.getValue().intValue());
		assertEquals("String", argument4.getValue()); //$NON-NLS-1$
	}

	@Test
	public void killMe() throws Exception {
		try {
			this.skill.killMe();
			fail("killMe() msut never return!"); //$NON-NLS-1$
		}
		catch(ChuckNorrisException _) {
			// Expected exception
		}
		catch(Exception e) {
			throw e;
		}
		ArgumentCaptor<UUID> argument = ArgumentCaptor.forClass(UUID.class);
		Mockito.verify(this.spawnService, new Times(1)).killAgent(argument.capture());
		assertSame(this.agentId, argument.getValue());
	}

}
