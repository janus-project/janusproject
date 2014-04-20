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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import io.janusproject2.services.SpawnService;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;

import java.util.Collections;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.AbstractService;
import com.google.common.util.concurrent.ServiceManager;

/**
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public class KernelTest {
	private Kernel kernel;

	private SpawnService spawnSkill;
	
	private AgentContext jc;
	
	private UUID janusID = UUID.randomUUID();
	
	

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		ServiceManager manager = new ServiceManager(Sets.newHashSet(new AbstractService() {
			
			@Override
			protected void doStop() {
				notifyStopped();
			}
			
			@Override
			protected void doStart() {
				notifyStarted();
				
			}
		}));
		this.kernel = new Kernel(manager);
		this.jc = mock(AgentContext.class);
		when(this.jc.getID()).thenReturn(this.janusID);
				
		this.kernel.setJanusContext(this.jc);
		this.spawnSkill = mock(SpawnService.class);
	}

	@Test
	public void testSpawnWithoutArguements() {
		verify(this.spawnSkill).spawn(this.jc,Agent.class, Collections.EMPTY_LIST.toArray());

	}

	@Test
	public void testSpawnWitArguements() {
		verify(this.spawnSkill).spawn(this.jc,Agent.class, new String[] {"hello", "world"});
	}
	

}
