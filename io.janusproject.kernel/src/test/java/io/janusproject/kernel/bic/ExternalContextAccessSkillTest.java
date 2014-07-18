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

import io.janusproject.services.contextspace.ContextSpaceService;
import io.sarl.core.Behaviors;
import io.sarl.core.ContextJoined;
import io.sarl.core.ContextLeft;
import io.sarl.core.MemberJoined;
import io.sarl.core.MemberLeft;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Capacity;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.EventSpaceSpecification;
import io.sarl.lang.core.SpaceID;
import io.sarl.util.OpenEventSpace;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc","unchecked","rawtypes","synthetic-access"})
public class ExternalContextAccessSkillTest extends Assert {

	private static final Object MUTEX = new Object();

	private List<AgentContext> contexts;
	
	@Mock
	private ContextSpaceService contextRepository;
	
	@InjectMocks
	private ExternalContextAccessSkill skill;

	private InternalEventBusCapacity busCapacity;
	private Behaviors behaviorCapacity;
	private OpenEventSpace defaultSpace;
	private Agent agent;
	private EventListener eventListener;

	@Before
	public void setUp() throws Exception {
		UUID parentId = UUID.randomUUID();
		
		this.eventListener = Mockito.mock(EventListener.class);
		this.behaviorCapacity = Mockito.mock(Behaviors.class);

		this.busCapacity = Mockito.mock(InternalEventBusCapacity.class);
		Mockito.when(this.busCapacity.asEventListener()).thenReturn(this.eventListener);

		this.contexts = new ArrayList<>();
		for(int i=0; i<10; ++i) {
			UUID contextId = i==0 ? parentId : UUID.randomUUID();
			OpenEventSpace defaultSpace = Mockito.mock(OpenEventSpace.class);
			if (i==0) {
				this.defaultSpace = defaultSpace;
			}
			Mockito.when(defaultSpace.getID()).thenReturn(
					new SpaceID(contextId, UUID.randomUUID(), EventSpaceSpecification.class));
			AgentContext c = Mockito.mock(AgentContext.class);
			Mockito.when(c.getID()).thenReturn(contextId);
			Mockito.when(c.getDefaultSpace()).thenReturn(defaultSpace);
			this.contexts.add(c);
		}
		this.agent = new Agent(UUID.randomUUID()) {
			@Override
			protected <S extends Capacity> S getSkill(Class<S> capacity) {
				if (Behaviors.class.equals(capacity))
					return capacity.cast(ExternalContextAccessSkillTest.this.behaviorCapacity);
				return capacity.cast(ExternalContextAccessSkillTest.this.busCapacity);
			}
		};
		this.agent = Mockito.spy(this.agent);
		Mockito.when(this.agent.getParentID()).thenReturn(parentId);

		this.skill = new ExternalContextAccessSkill(this.agent);
		
		MockitoAnnotations.initMocks(this);
		
		Mockito.when(this.contextRepository.mutex()).thenReturn(MUTEX);
		Mockito.when(this.contextRepository.getContexts()).thenReturn(this.contexts);
		Mockito.when(this.contextRepository.getContexts(Matchers.anyCollection())).then(new Answer<Collection>() {
			@Override
			public Collection answer(InvocationOnMock invocation)
					throws Throwable {
				Collection<UUID> ids = (Collection<UUID>)invocation.getArguments()[0];
				List<AgentContext> l = new ArrayList<>();
				for(AgentContext ctx: ExternalContextAccessSkillTest.this.contexts) {
					if (ids.contains(ctx.getID())) {
						l.add(ctx);
					}
				}
				return l;
			}
		});
		Mockito.when(this.contextRepository.getContext(Matchers.any(UUID.class))).then(new Answer<AgentContext>() {
			@Override
			public AgentContext answer(InvocationOnMock invocation)
					throws Throwable {
				UUID id = (UUID)invocation.getArguments()[0];
				for(AgentContext ctx: ExternalContextAccessSkillTest.this.contexts) {
					if (id.equals(ctx.getID())) {
						return ctx;
					}
				}
				return null;
			}
		});
	}

	@After
	public void tearDown() throws Exception {
		this.skill = null;
		this.contexts = null;
		this.contextRepository = null;
		this.busCapacity = null;
		this.behaviorCapacity = null;
		this.defaultSpace = null;
		this.agent = null;
		this.eventListener = null;
	}
		
	@Test
	public void getAllContexts() {
		Collection<AgentContext> c = this.skill.getAllContexts();
		assertTrue(c.isEmpty());
	}

	@Test(expected=IllegalArgumentException.class)
	public void getContext() {
		for(AgentContext c : this.contexts) {
			this.skill.getContext(c.getID());
		}
	}

	@Test
	public void join() {
		int nb = 0;
		for(AgentContext c : this.contexts) {
			this.skill.join(c.getID(), c.getDefaultSpace().getID().getID());
			//
			AgentContext ctx = this.skill.getContext(c.getID());
			assertSame(c, ctx);
			//
			ArgumentCaptor<Event> argument1 = ArgumentCaptor.forClass(Event.class);
			Mockito.verify(c.getDefaultSpace(), new Times(1)).emit(argument1.capture());
			Event evt = argument1.getValue();
			assertNotNull(evt);
			assertTrue(evt instanceof MemberJoined);
			assertEquals(c.getID(), ((MemberJoined)evt).parentContextID);
			assertEquals(this.agent.getID(), ((MemberJoined)evt).agentID);
			//
			ArgumentCaptor<Event> argument2 = ArgumentCaptor.forClass(Event.class);
			++nb;
			Mockito.verify(this.behaviorCapacity, new Times(nb)).wake(argument2.capture());
			evt = argument2.getValue();
			assertNotNull(evt);
			assertTrue(evt instanceof ContextJoined);
			assertEquals(c.getID(), ((ContextJoined)evt).holonContextID);
			assertEquals(c.getDefaultSpace().getID().getID(), ((ContextJoined)evt).defaultSpaceID);
		}
		Collection<AgentContext> c = this.skill.getAllContexts();
		assertEquals(this.contexts.size(), c.size());
		for(AgentContext ctx : c) {
			assertTrue(this.contexts.contains(ctx));
		}
	}

	@Test
	public void leave() {
		int nb = 0;
		for(AgentContext c : this.contexts) {
			this.skill.join(c.getID(), c.getDefaultSpace().getID().getID());
			++nb;
		}
		//
		List<AgentContext> remaining = new ArrayList<>(this.contexts);
		for(AgentContext c : this.contexts) {
			this.skill.leave(c.getID());
			//
			remaining.remove(c);
			Collection<AgentContext> in = this.skill.getAllContexts();
			assertEquals(remaining.size(), in.size());
			for(AgentContext ctx : in) {
				assertTrue(remaining.contains(ctx));
			}
			//
			ArgumentCaptor<Event> argument1 = ArgumentCaptor.forClass(Event.class);
			// 2 times: 1 for MemberJoined, 1 for MemberLeft
			Mockito.verify(c.getDefaultSpace(), new Times(2)).emit(argument1.capture());
			Event evt = argument1.getValue();
			assertNotNull(evt);
			assertTrue(evt instanceof MemberLeft);
			assertEquals(this.agent.getID(), ((MemberLeft)evt).agentID);
			//
			ArgumentCaptor<Event> argument2 = ArgumentCaptor.forClass(Event.class);
			++nb;
			// Nb times includes the joins and the leaves
			Mockito.verify(this.behaviorCapacity, new Times(nb)).wake(argument2.capture());
			evt = argument2.getValue();
			assertNotNull(evt);
			assertTrue(evt instanceof ContextLeft);
			assertEquals(c.getID(), ((ContextLeft)evt).holonContextID);
		}
		assertTrue(remaining.isEmpty());
	}

	@Test
	public void install() {
		assertNull(this.defaultSpace.getAddress(this.agent.getID()));
		this.skill.install();
		ArgumentCaptor<EventListener> argument = ArgumentCaptor.forClass(EventListener.class);
		Mockito.verify(this.defaultSpace, new Times(1)).register(argument.capture());
		assertSame(this.eventListener, argument.getValue());
	}

	@Test
	public void uninstall() {
		this.skill.install();
		this.skill.uninstall();
		ArgumentCaptor<EventListener> argument = ArgumentCaptor.forClass(EventListener.class);
		Mockito.verify(this.defaultSpace, new Times(1)).unregister(argument.capture());
		assertSame(this.eventListener, argument.getValue());
	}

}
