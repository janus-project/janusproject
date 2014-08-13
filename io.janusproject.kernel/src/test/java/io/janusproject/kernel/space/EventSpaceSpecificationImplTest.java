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
package io.janusproject.kernel.space;

import io.janusproject.services.distributeddata.DistributedDataStructureService;
import io.janusproject.testutils.MapMock;
import io.sarl.lang.core.EventSpace;
import io.sarl.lang.core.EventSpaceSpecification;
import io.sarl.lang.core.SpaceID;

import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.google.inject.Injector;


/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"all"})
public class EventSpaceSpecificationImplTest extends Assert {

	private SpaceID spaceId;

	@Mock
	private DistributedDataStructureService structureFactory;
	@Mock
	private Injector injector;
	@InjectMocks
	private EventSpaceSpecificationImpl specification;
	
	@Before
	public void setUp() {
		this.spaceId = new SpaceID(
				UUID.randomUUID(),
				UUID.randomUUID(),
				EventSpaceSpecification.class);
		MockitoAnnotations.initMocks(this);
		Mockito.when(this.injector.getInstance(Matchers.any(Class.class))).thenReturn(this.structureFactory);
		Mockito.when(this.structureFactory.getMap(Matchers.anyString())).thenReturn(new MapMock<>());
	}
	
	@After
	public void tearDown() {
		this.specification = null;
		this.spaceId = null;
	}

	@Test
	public void create() {
		EventSpace space = this.specification.create(this.spaceId, "a", "b", "c");  //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
		assertNotNull(space);
		assertSame(this.spaceId, space.getID());
	}

}
