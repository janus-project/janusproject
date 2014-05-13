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

import io.janusproject.util.TwoStepConstruction;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.util.SynchronizedCollection;
import io.sarl.util.Collections3;
import io.sarl.util.OpenEventSpace;
import io.sarl.util.OpenEventSpaceSpecification;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

import javassist.Modifier;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc","static-method","unchecked","synthetic-access","rawtypes"})
public class ContextTest extends Assert {

	private UUID contextId;
	private UUID spaceId;
	private Collection<OpenEventSpace> spaces;
	
	private SpaceRepository spaceRepository;
	
	private Context context;
	
	@Before
	public void setUp() {
		this.contextId = UUID.randomUUID();
		this.spaceId = UUID.randomUUID();
		this.spaces = new ArrayList<>();
		this.spaceRepository = Mockito.mock(SpaceRepository.class);
		Mockito.when(this.spaceRepository.createSpace(Matchers.any(SpaceID.class), Matchers.any(Class.class)))
			.thenAnswer(new Answer<Space>() {
				@Override
				public Space answer(InvocationOnMock invocation)
						throws Throwable {
					OpenEventSpace space = Mockito.mock(OpenEventSpace.class);
					Mockito.when(space.getID()).thenReturn((SpaceID)invocation.getArguments()[0]);
					ContextTest.this.spaces.add(space);
					return space;
				}
			});
		Mockito.when(this.spaceRepository.getOrCreateSpace(Matchers.any(Class.class), Matchers.any(SpaceID.class)))
			.thenAnswer(new Answer<Space>() {
				@Override
				public Space answer(InvocationOnMock invocation)
						throws Throwable {
					for(Space s : ContextTest.this.spaces) {
						if (s.getID().equals(invocation.getArguments()[1])) {
							return s;
						}
					}
					OpenEventSpace space = Mockito.mock(OpenEventSpace.class);
					Mockito.when(space.getID()).thenReturn((SpaceID)invocation.getArguments()[1]);
					ContextTest.this.spaces.add(space);
					return space;
				}
			});
		Mockito.when(this.spaceRepository.getSpaces(Matchers.any(Class.class)))
		.thenAnswer(new Answer<SynchronizedCollection<? extends Space>>() {
			@Override
			public SynchronizedCollection<? extends Space> answer(InvocationOnMock invocation)
					throws Throwable {
				Collection<Space> c = new ArrayList<>();
				for(OpenEventSpace space : ContextTest.this.spaces) {
					if (invocation.getArguments()[0].equals(space.getID().getSpaceSpecification())) {
						c.add(space);
					}
				}
				return Collections3.synchronizedCollection(c, c);
			}
		});
		Mockito.when(this.spaceRepository.getSpace(Matchers.any(SpaceID.class)))
		.thenAnswer(new Answer<Space>() {
			@Override
			public Space answer(InvocationOnMock invocation)
					throws Throwable {
				for(OpenEventSpace space : ContextTest.this.spaces) {
					if (invocation.getArguments()[0].equals(space.getID())) {
						return space;
					}
				}
				return null;
			}
		});
		Mockito.when(this.spaceRepository.getSpaces()).thenReturn(
				Collections3.synchronizedCollection((Collection)this.spaces,this.spaces));
		this.context = new Context(this.contextId, this.spaceId, this.spaceRepository);
		this.context.postConstruction();
	}
	
	@After
	public void tearDown() {
		this.contextId = null;
		this.spaceId = null;
		this.context = null;
		this.spaceRepository = null;
		this.spaces = null;
	}

	@Test
	public void twoStepConstruction() throws Exception {
		TwoStepConstruction annotation = Context.class.getAnnotation(TwoStepConstruction.class);
		assertNotNull(annotation);
		for(String name : annotation.names()) {
			for(Method method : Context.class.getMethods()) {
				if (name.equals(method.getName())) {
					assertTrue(Modifier.isPackage(method.getModifiers())
							||Modifier.isPublic(method.getModifiers()));
					break;
				}
			}
		}
	}
	
	@Test
	public void postConstruction() {
		ArgumentCaptor<SpaceID> argument1 = ArgumentCaptor.forClass(SpaceID.class);
		ArgumentCaptor<Class> argument2 = ArgumentCaptor.forClass(Class.class);
		Mockito.verify(this.spaceRepository, new Times(1)).createSpace(argument1.capture(), argument2.capture());
		assertEquals(this.contextId, argument1.getValue().getContextID());
		assertEquals(this.spaceId, argument1.getValue().getID());
	}

	@Test
	public void getID() {
		assertSame(this.contextId, this.context.getID());
	}

	@Test
	public void getDefaultSpace() {
		OpenEventSpace space = this.context.getDefaultSpace();
		assertNotNull(space);
		assertEquals(this.contextId, space.getID().getContextID());
		assertEquals(this.spaceId, space.getID().getID());
	}

	@Test
	public void createSpace() {
		Collection<? extends Space> c;
		UUID id = UUID.randomUUID();
		OpenEventSpace space = this.context.createSpace(OpenEventSpaceSpecification.class, id);
		//
		assertNotNull(space);
		assertEquals(id, space.getID().getID());
		c = this.context.getSpaces();
		assertNotNull(c);
		assertEquals(2, c.size());
		Collection<UUID> ids = new ArrayList<>();
		ids.add(this.spaceId);
		ids.add(id);
		for(Space sp: c) {
			ids.remove(sp.getID().getID());
		}
		assertTrue(ids.isEmpty());
	}

	@Test
	public void getOrCreateSpace() {
		Collection<? extends Space> c;
		UUID id = UUID.randomUUID();
		OpenEventSpace space = this.context.getOrCreateSpace(OpenEventSpaceSpecification.class, id);
		//
		assertNotNull(space);
		assertEquals(id, space.getID().getID());
		c = this.context.getSpaces();
		assertNotNull(c);
		assertEquals(2, c.size());
		Collection<UUID> ids = new ArrayList<>();
		ids.add(this.spaceId);
		ids.add(id);
		for(Space sp: c) {
			ids.remove(sp.getID().getID());
		}
		assertTrue(ids.isEmpty());
		//
		OpenEventSpace space2 = this.context.getOrCreateSpace(OpenEventSpaceSpecification.class, id);
		assertSame(space, space2);
		}

	@Test
	public void getSpaces() {
		Collection<? extends Space> c;
		c = this.context.getSpaces();
		assertNotNull(c);
		assertEquals(1, c.size());
		assertEquals(this.spaceId, c.iterator().next().getID().getID());
		//
		UUID id = UUID.randomUUID();
		this.context.createSpace(OpenEventSpaceSpecification.class, id);
		//
		c = this.context.getSpaces();
		assertNotNull(c);
		assertEquals(2, c.size());
		Collection<UUID> ids = new ArrayList<>();
		ids.add(this.spaceId);
		ids.add(id);
		for(Space space : c) {
			ids.remove(space.getID().getID());
		}
		assertTrue(ids.isEmpty());
	}

	@Test
	public void getSpacesClass() {
		Collection<OpenEventSpace> c;
		c = this.context.getSpaces(OpenEventSpaceSpecification.class);
		assertNotNull(c);
		assertEquals(1, c.size());
		assertEquals(this.spaceId, c.iterator().next().getID().getID());
		//
		UUID id = UUID.randomUUID();
		this.context.createSpace(OpenEventSpaceSpecification.class, id);
		//
		c = this.context.getSpaces(OpenEventSpaceSpecification.class);
		assertNotNull(c);
		assertEquals(2, c.size());
		Collection<UUID> ids = new ArrayList<>();
		ids.add(this.spaceId);
		ids.add(id);
		for(Space space : c) {
			ids.remove(space.getID().getID());
		}
		assertTrue(ids.isEmpty());
	}

	@Test
	public void getSpaceUUID() {
		assertNotNull(this.context.getSpace(this.spaceId));
		assertNull(this.context.getSpace(UUID.randomUUID()));
	}

	@Test
	public void destroy() {
		this.context.destroy();
		Mockito.verify(this.spaceRepository, new Times(1)).destroy();
	}
	
}
