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

import io.janusproject.kernel.SpaceRepository.SpaceRepositoryListener;
import io.janusproject.services.LogService;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.EventSpaceSpecification;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;
import io.sarl.util.OpenEventSpace;
import io.sarl.util.OpenEventSpaceSpecification;
import io.sarl.util.RestrictedAccessEventSpace;
import io.sarl.util.RestrictedAccessEventSpaceSpecification;

import java.util.Collection;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;

import com.google.inject.Injector;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;


/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc","unchecked"})
public class SpaceRepositoryTest extends Assert {

	private IMap<SpaceID,Object[]> spaceIDs;
	private HazelcastInstance hzInstance;
	private Injector injector;
	private LogService logService;
	private SpaceRepositoryListener listener;
	private SpaceRepository repository;
	private SpaceID spaceID;
	private Object[] params;
	private OpenEventSpaceSpecification spaceSpecification;
	private OpenEventSpace space;
	
	@Before
	public void setUp() {
		this.spaceIDs = Mockito.mock(IMap.class);
		this.hzInstance = Mockito.mock(HazelcastInstance.class);
		this.injector = Mockito.mock(Injector.class);
		this.logService = Mockito.mock(LogService.class);
		this.listener = Mockito.mock(SpaceRepositoryListener.class);
		this.spaceID = new SpaceID(UUID.randomUUID(), UUID.randomUUID(), OpenEventSpaceSpecification.class);
		this.params = new Object[]{"PARAM"}; //$NON-NLS-1$
		//
		Mockito.when(this.hzInstance.<SpaceID,Object[]>getMap(Matchers.anyString())).thenReturn(this.spaceIDs);
		//
		this.repository = new SpaceRepository(
				"thename", //$NON-NLS-1$
				this.hzInstance,
				this.injector,
				this.logService,
				this.listener);		
	}
	
	@After
	public void tearDown() {
		this.space = null;
		this.spaceSpecification = null;
		this.params = null;
		this.spaceID = null;
		this.listener = null;
		this.logService = null;
		this.injector = null;
		this.hzInstance = null;
		this.repository = null;
		this.spaceIDs = null;
	}
	
	private void initMocks() {
		Mockito.when(this.spaceIDs.containsKey(this.spaceID)).thenReturn(true);
		this.spaceSpecification = Mockito.mock(OpenEventSpaceSpecification.class);
		Mockito.when(this.injector.getInstance(OpenEventSpaceSpecification.class)).thenReturn(this.spaceSpecification);
		this.space = Mockito.mock(OpenEventSpace.class);
		Mockito.when(this.spaceSpecification.create(this.spaceID, this.params)).thenReturn(this.space);
		Mockito.when(this.space.getID()).thenReturn(this.spaceID);
	}
	
	private void baseInit() {
		this.spaceSpecification = Mockito.mock(OpenEventSpaceSpecification.class);
		Mockito.when(this.injector.getInstance(OpenEventSpaceSpecification.class)).thenReturn(this.spaceSpecification);
		this.space = Mockito.mock(OpenEventSpace.class);
		Mockito.when(this.spaceSpecification.create(this.spaceID, this.params)).thenReturn(this.space);
		Mockito.when(this.space.getID()).thenReturn(this.spaceID);
	}

	private void initRepository() {
		initMocks();
		this.repository.ensureSpaceDefinition(this.spaceID, this.params);
	}

	@Test(expected=AssertionError.class)
	public void ensureSpaceDefinition_nospaceid() {
		this.repository.ensureSpaceDefinition(this.spaceID, this.params);
	}

	@Test
	public void ensureSpaceDefinition_spaceid() {
		initMocks();
		//
		this.repository.ensureSpaceDefinition(this.spaceID, this.params);
		//
		ArgumentCaptor<SpaceID> argument1 = ArgumentCaptor.forClass(SpaceID.class);
		ArgumentCaptor<Object[]> argument2 = ArgumentCaptor.forClass(Object[].class);
		Mockito.verify(this.spaceSpecification, new Times(1)).create(argument1.capture(), argument2.capture());
		assertSame(this.spaceID, argument1.getValue());
		assertEquals("PARAM", argument2.getValue()); //$NON-NLS-1$
		assertSame(this.space, this.repository.getSpace(this.spaceID));
		//
		ArgumentCaptor<Space> argument3 = ArgumentCaptor.forClass(Space.class);
		Mockito.verify(this.listener, new Times(1)).spaceCreated(argument3.capture());
		assertSame(this.space, argument3.getValue());
	}

	@Test
	public void ensureSpaceDefinition_recall() {
		initMocks();
		//
		this.repository.ensureSpaceDefinition(this.spaceID, this.params);
		//
		this.repository.ensureSpaceDefinition(this.spaceID, this.params);
		//
		ArgumentCaptor<SpaceID> argument1 = ArgumentCaptor.forClass(SpaceID.class);
		ArgumentCaptor<Object[]> argument2 = ArgumentCaptor.forClass(Object[].class);
		Mockito.verify(this.spaceSpecification, new Times(1)).create(argument1.capture(), argument2.capture());
		assertSame(this.spaceID, argument1.getValue());
		assertEquals("PARAM", argument2.getValue()); //$NON-NLS-1$
		assertSame(this.space, this.repository.getSpace(this.spaceID));
		//
		ArgumentCaptor<Space> argument3 = ArgumentCaptor.forClass(Space.class);
		Mockito.verify(this.listener, new Times(1)).spaceCreated(argument3.capture());
		assertSame(this.space, argument3.getValue());
	}

	@Test(expected=AssertionError.class)
	public void removeSpaceDefinition_spaceid() {
		initMocks();
		//
		this.repository.removeSpaceDefinition(this.spaceID);
	}

	@Test
	public void removeSpaceDefinition_nospaceid() {
		initRepository();
		Mockito.when(this.spaceIDs.containsKey(this.spaceID)).thenReturn(false);
		//
		this.repository.removeSpaceDefinition(this.spaceID);
		//
		assertNull(this.repository.getSpace(this.spaceID));
		//
		ArgumentCaptor<Space> argument3 = ArgumentCaptor.forClass(Space.class);
		Mockito.verify(this.listener, new Times(1)).spaceDestroyed(argument3.capture());
		assertSame(this.space, argument3.getValue());
	}
	
	@Test
	public void getSpaces() {
		initRepository();
		//
		Collection<Space> spaces = this.repository.getSpaces();
		assertNotNull(spaces);
		assertEquals(1, spaces.size());
		assertTrue(spaces.contains(this.space));
	}

	@Test
	public void getSpaceSpaceID() {
		initRepository();
		//
		Space space = this.repository.getSpace(this.spaceID);
		assertSame(this.space, space);
		//
		assertNull(this.repository.getSpace(Mockito.mock(SpaceID.class)));
	}

	@Test
	public void getSpacesClass_EventSpace() {
		initRepository();
		//
		Collection<EventSpace> spaces = this.repository.getSpaces(EventSpaceSpecification.class);
		assertNotNull(spaces);
		assertEquals(0, spaces.size());
		assertFalse(spaces.contains(this.space));
	}

	@Test
	public void getSpacesClass_OpenEventSpace() {
		initRepository();
		//
		Collection<OpenEventSpace> spaces = this.repository.getSpaces(OpenEventSpaceSpecification.class);
		assertNotNull(spaces);
		assertEquals(1, spaces.size());
		assertTrue(spaces.contains(this.space));
	}

	@Test
	public void getSpacesClass_RestrictedAccessEventSpace() {
		initRepository();
		//
		Collection<RestrictedAccessEventSpace> spaces = this.repository.getSpaces(RestrictedAccessEventSpaceSpecification.class);
		assertNotNull(spaces);
		assertEquals(0, spaces.size());
		assertFalse(spaces.contains(this.space));
	}

	@Test
	public void createSpace_singlecreation() {
		baseInit();
		//
		OpenEventSpace space = this.repository.createSpace(this.spaceID, OpenEventSpaceSpecification.class, this.params);
		//
		assertSame(this.space, space);
		ArgumentCaptor<SpaceID> argument1 = ArgumentCaptor.forClass(SpaceID.class);
		ArgumentCaptor<Object[]> argument2 = ArgumentCaptor.forClass(Object[].class);
		Mockito.verify(this.spaceSpecification, new Times(1)).create(argument1.capture(), argument2.capture());
		assertSame(this.spaceID, argument1.getValue());
		assertEquals("PARAM", argument2.getValue()); //$NON-NLS-1$
		assertSame(this.space, this.repository.getSpace(this.spaceID));
		//
		ArgumentCaptor<Space> argument3 = ArgumentCaptor.forClass(Space.class);
		Mockito.verify(this.listener, new Times(1)).spaceCreated(argument3.capture());
		assertSame(this.space, argument3.getValue());
	}

	@Test
	public void createSpace_doublecreation() {
		baseInit();
		//
		OpenEventSpace space1 = this.repository.createSpace(this.spaceID, OpenEventSpaceSpecification.class, this.params);
		OpenEventSpace space2 = this.repository.createSpace(this.spaceID, OpenEventSpaceSpecification.class, this.params);
		//
		assertSame(this.space, space1);
		assertSame(this.space, space2);
		ArgumentCaptor<SpaceID> argument1 = ArgumentCaptor.forClass(SpaceID.class);
		ArgumentCaptor<Object[]> argument2 = ArgumentCaptor.forClass(Object[].class);
		Mockito.verify(this.spaceSpecification, new Times(1)).create(argument1.capture(), argument2.capture());
		assertSame(this.spaceID, argument1.getValue());
		assertEquals("PARAM", argument2.getValue()); //$NON-NLS-1$
		assertSame(this.space, this.repository.getSpace(this.spaceID));
		//
		ArgumentCaptor<Space> argument3 = ArgumentCaptor.forClass(Space.class);
		Mockito.verify(this.listener, new Times(1)).spaceCreated(argument3.capture());
		assertSame(this.space, argument3.getValue());
	}

	@Test
	public void getOrCreateSpace_singlecreation() {
		baseInit();
		//
		OpenEventSpace space = this.repository.getOrCreateSpace(
				OpenEventSpaceSpecification.class, this.spaceID, this.params);
		//
		assertSame(this.space, space);
		ArgumentCaptor<SpaceID> argument1 = ArgumentCaptor.forClass(SpaceID.class);
		ArgumentCaptor<Object[]> argument2 = ArgumentCaptor.forClass(Object[].class);
		Mockito.verify(this.spaceSpecification, new Times(1)).create(argument1.capture(), argument2.capture());
		assertSame(this.spaceID, argument1.getValue());
		assertEquals("PARAM", argument2.getValue()); //$NON-NLS-1$
		assertSame(this.space, this.repository.getSpace(this.spaceID));
		//
		ArgumentCaptor<Space> argument3 = ArgumentCaptor.forClass(Space.class);
		Mockito.verify(this.listener, new Times(1)).spaceCreated(argument3.capture());
		assertSame(this.space, argument3.getValue());
	}

	@Test
	public void getOrCreateSpace_doublecreation() {
		baseInit();
		//
		OpenEventSpace space1 = this.repository.getOrCreateSpace(
				OpenEventSpaceSpecification.class, this.spaceID, this.params);
		OpenEventSpace space2 = this.repository.getOrCreateSpace(
				OpenEventSpaceSpecification.class, this.spaceID, this.params);
		//
		assertSame(this.space, space1);
		assertSame(this.space, space2);
		ArgumentCaptor<SpaceID> argument1 = ArgumentCaptor.forClass(SpaceID.class);
		ArgumentCaptor<Object[]> argument2 = ArgumentCaptor.forClass(Object[].class);
		Mockito.verify(this.spaceSpecification, new Times(1)).create(argument1.capture(), argument2.capture());
		assertSame(this.spaceID, argument1.getValue());
		assertEquals("PARAM", argument2.getValue()); //$NON-NLS-1$
		assertSame(this.space, this.repository.getSpace(this.spaceID));
		//
		ArgumentCaptor<Space> argument3 = ArgumentCaptor.forClass(Space.class);
		Mockito.verify(this.listener, new Times(1)).spaceCreated(argument3.capture());
		assertSame(this.space, argument3.getValue());
	}

}
