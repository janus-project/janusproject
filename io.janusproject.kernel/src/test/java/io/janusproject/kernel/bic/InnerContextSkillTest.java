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

import io.janusproject.services.ContextSpaceService;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Capacity;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.EventSpaceSpecification;
import io.sarl.lang.core.SpaceID;
import io.sarl.util.OpenEventSpace;

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

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc","synthetic-access"})
public class InnerContextSkillTest extends Assert {

	@Mock
	private EventListener eventListener;
	
	@Mock
	private AgentContext innerContext;

	@Mock(name="innerSpace")
	private OpenEventSpace innerSpace;
	
	@Mock
	private ContextSpaceService contextService;
	
	@Mock
	private InternalEventBusCapacity busCapacity;

	@InjectMocks
	private InnerContextSkill skill;
	
	@Before
	public void setUp() throws Exception {
		UUID agentId = UUID.randomUUID();
		Address address = new Address(
				new SpaceID(
						UUID.randomUUID(),
						UUID.randomUUID(),
						EventSpaceSpecification.class),
				agentId);
		Agent agent = new Agent(agentId) {
			@Override
			protected <S extends Capacity> S getSkill(Class<S> capacity) {
				return capacity.cast(InnerContextSkillTest.this.busCapacity);
			}
		};
		agent = Mockito.spy(agent);
		address = Mockito.spy(address);
		this.skill = new InnerContextSkill(agent, address);
		MockitoAnnotations.initMocks(this);
		Mockito.when(this.contextService.createContext(Matchers.any(UUID.class), 
				Matchers.any(UUID.class))).thenReturn(this.innerContext);
		Mockito.when(this.innerContext.getDefaultSpace()).thenReturn(this.innerSpace);
		Mockito.when(this.busCapacity.asEventListener()).thenReturn(this.eventListener);
	}

	@After
	public void tearDown() throws Exception {
		this.skill = null;
		this.contextService = null;
		this.innerContext = null;
		this.innerSpace = null;
		this.eventListener = null;
		this.busCapacity = null;
	}
	
	@Test
	public void getInnerContext() {
		// Things are already injected
		this.skill.resetInnerContext();
		assertFalse(this.skill.hasInnerContext());
		//
		AgentContext ctx = this.skill.getInnerContext();
		assertSame(this.innerContext, ctx);
		assertTrue(this.skill.hasInnerContext());
		ArgumentCaptor<EventListener> argument = ArgumentCaptor.forClass(EventListener.class);
		Mockito.verify(this.innerSpace, new Times(1)).register(argument.capture());
		assertSame(this.eventListener, argument.getValue());
	}

	@Test
	public void uninstall() {
		// Things are already injected
		this.skill.resetInnerContext();
		assertFalse(this.skill.hasInnerContext());
		this.skill.getInnerContext();
		assertTrue(this.skill.hasInnerContext());
		//
		this.skill.uninstall();
		assertFalse(this.skill.hasInnerContext());
		ArgumentCaptor<AgentContext> argument = ArgumentCaptor.forClass(AgentContext.class);
		Mockito.verify(this.contextService, new Times(1)).removeContext(argument.capture());
		assertSame(this.innerContext, argument.getValue());
	}

}
