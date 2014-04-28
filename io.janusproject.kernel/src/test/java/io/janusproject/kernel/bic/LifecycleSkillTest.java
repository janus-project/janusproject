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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.janusproject.services.SpawnService;
import io.sarl.core.Behaviors;
import io.sarl.core.ExternalContextAccess;
import io.sarl.core.Lifecycle;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.util.OpenEventSpace;

import java.util.Collections;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;

/**
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"nls","javadoc"})
public class LifecycleSkillTest {

	private Lifecycle skill;
	private UUID agentID = UUID.randomUUID();
	private UUID parentContextID = UUID.randomUUID();
	
	@Mock
	private AgentContext parentContext;
	
	@Mock 
	private Agent agent;
	
	@Mock
	private SpawnService spawnService;

	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.skill = new LifecycleSkill(this.agent, this.spawnService);
		when(this.parentContext.getID()).thenReturn(this.parentContextID);
		when(this.agent.getID()).thenReturn(this.agentID);
		
		BehaviorsSkill behaviorsMock = mock(BehaviorsSkill.class);
		ExternalContextAccessSkill extContextMock = mock(ExternalContextAccessSkill.class);
				
		Lifecycle l = PowerMockito.spy(this.skill);
		PowerMockito.when(l,"getSkill",Behaviors.class).thenReturn(behaviorsMock);		
		PowerMockito.when(l,"getSkill",ExternalContextAccess.class).thenReturn(extContextMock);
				
		Agent agentMock = mock(Agent.class);
		PowerMockito.when(l,"getOwner").thenReturn(agentMock);
		
		OpenEventSpace spaceMock = mock(OpenEventSpace.class);
		when(this.parentContext.getDefaultSpace()).thenReturn(spaceMock);
		
	}

	@Test
	public void killMe() {
		this.skill.killMe();
		verify(this.spawnService).killAgent(this.agentID);
	}
	
	@Test
	public void spawn(){
		this.skill.spawnInContext(Agent.class, this.parentContext,Collections.EMPTY_LIST.toArray());
		verify(this.spawnService).spawn(this.parentContext, Agent.class, Collections.EMPTY_LIST.toArray());
	}

}
