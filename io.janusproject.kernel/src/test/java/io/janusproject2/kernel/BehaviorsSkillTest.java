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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.janusproject2.services.LogService;
import io.sarl.lang.core.Agent;
import io.sarl.util.OpenEventSpace;

import java.util.UUID;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.AsyncSyncEventBus;

/**
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class BehaviorsSkillTest {
	
	private Agent agentMock;
	
	/** 
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.agentMock = mock(Agent.class);
		when(this.agentMock.getID()).thenReturn(UUID.randomUUID());
		ContextFactory factory = mock(ContextFactory.class);
		Context c = mock(Context.class);
		OpenEventSpace space = mock(OpenEventSpace.class);
		
		when(c.getDefaultSpace()).thenReturn(space);
		when(factory.create(any(UUID.class), any(UUID.class))).thenReturn(c);
	}


	@Test
	public void test() {
		AsyncSyncEventBus bus = mock(AsyncSyncEventBus.class);
		BehaviorsAndInnerContextSkill_ behavior = new BehaviorsAndInnerContextSkill_(this.agentMock);
		behavior.setLogService(mock(LogService.class));
		behavior.setInternalEventBus(bus);
		
	}

}
