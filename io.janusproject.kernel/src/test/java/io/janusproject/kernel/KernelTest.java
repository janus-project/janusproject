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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class KernelTest {
	private Kernel kernel;

	private SpawnService spawnSkill;
	
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
		AgentContext jc = mock(AgentContext.class);
		when(jc.getID()).thenReturn(this.janusID);
				
		this.kernel.setJanusContext(jc);
		this.spawnSkill = mock(SpawnService.class);
		
		this.kernel.setSpawnSkill(this.spawnSkill);
	}

	@Test
	public void testSpawnWithoutArguements() {

		this.kernel.spawn(Agent.class);
		verify(this.spawnSkill).spawn(this.janusID,Agent.class, Collections.EMPTY_LIST.toArray());

	}

	@Test
	public void testSpawnWitArguements() {
		this.kernel.spawn(Agent.class, "hello", "world");
		verify(this.spawnSkill).spawn(this.janusID,Agent.class, new String[] {"hello", "world"});
	}
	

}
