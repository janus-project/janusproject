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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import io.sarl.lang.core.Agent;
import io.sarl.util.OpenEventSpace;

import java.util.UUID;
import java.util.logging.Logger;

import org.junit.Before;
import org.junit.Test;

import com.google.common.eventbus.AsyncEventBus;

/**
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
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
		AsyncEventBus bus = mock(AsyncEventBus.class);
		BehaviorsAndInnerContextSkill behavior = new BehaviorsAndInnerContextSkill(this.agentMock);
		behavior.setLogger(mock(Logger.class));
		behavior.setInternalEventBus(bus);
		
	}

}
