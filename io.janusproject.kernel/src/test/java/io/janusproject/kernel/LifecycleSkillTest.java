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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.UUID;

import io.sarl.core.Lifecycle;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
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
	}

	@Test
	public void killMe() {
		this.skill.killMe();
		verify(this.spawnService).killAgent(this.agentID);
	}
	
	@Test
	public void spawn(){
		this.skill.spawnInContext(Agent.class, this.parentContext,Collections.EMPTY_LIST.toArray());
		verify(this.spawnService).spawn(this.parentContextID, Agent.class, Collections.EMPTY_LIST.toArray());
	}

}
