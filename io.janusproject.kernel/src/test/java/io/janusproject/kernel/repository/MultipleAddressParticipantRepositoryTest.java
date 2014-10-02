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
package io.janusproject.kernel.repository;

import io.janusproject.kernel.repository.MultipleAddressParticipantRepository;
import io.janusproject.services.distributeddata.DistributedDataStructureService;
import io.janusproject.testutils.MultiMapMock;
import io.sarl.lang.core.EventListener;

import java.util.Collection;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc"})
public class MultipleAddressParticipantRepositoryTest extends Assert {
	
	private String distributedName;
	private MultipleAddressParticipantRepository<String> repository;
	private DistributedDataStructureService service;
	private UUID id1;
	private UUID id2;
	private EventListener listener1;
	private EventListener listener2;

	@Before
	public void setUp() {
		this.distributedName = getClass().getName()+UUID.randomUUID().toString();
		//
		this.service = Mockito.mock(DistributedDataStructureService.class);
		Mockito.when(this.service.getMultiMap(this.distributedName)).thenReturn(new MultiMapMock<>());
		//
		this.repository = new MultipleAddressParticipantRepository<>(this.distributedName, this.service);
		//
		this.id1 = UUID.randomUUID();
		this.id2 = UUID.randomUUID();
		//
		this.listener1 = Mockito.mock(EventListener.class);
		Mockito.when(this.listener1.getID()).thenReturn(this.id1);
		this.listener2 = Mockito.mock(EventListener.class);
		Mockito.when(this.listener2.getID()).thenReturn(this.id2);
	}

	@After
	public void tearDown() {
		this.repository = null;
		this.distributedName = null;
		this.service = null;
		this.id1 = this.id2 = null;
		this.listener1 = this.listener2 = null;
	}

	@Test
	public void registerParticipant() {
		Collection<String> col;
		col = this.repository.getAddresses(this.id1);
		assertTrue(col.isEmpty());
		col = this.repository.getAddresses(this.id2);
		assertTrue(col.isEmpty());
		this.repository.registerParticipant("a", this.listener1); //$NON-NLS-1$
		this.repository.registerParticipant("b", this.listener2); //$NON-NLS-1$
		this.repository.registerParticipant("c", this.listener1); //$NON-NLS-1$
		col = this.repository.getAddresses(this.id1);
		assertFalse(col.isEmpty());
		assertEquals(2, col.size());
		assertTrue(col.contains("a")); //$NON-NLS-1$
		assertTrue(col.contains("c")); //$NON-NLS-1$
		col = this.repository.getAddresses(this.id2);
		assertFalse(col.isEmpty());
		assertEquals(1, col.size());
		assertTrue(col.contains("b")); //$NON-NLS-1$
	}

	@Test
	public void unregisterParticipant() {
		Collection<String> col;
		this.repository.registerParticipant("a", this.listener1); //$NON-NLS-1$
		this.repository.registerParticipant("b", this.listener2); //$NON-NLS-1$
		this.repository.registerParticipant("c", this.listener1); //$NON-NLS-1$
		//
		this.repository.unregisterParticipant("c", this.listener1); //$NON-NLS-1$
		col = this.repository.getAddresses(this.id1);
		assertFalse(col.isEmpty());
		assertEquals(1, col.size());
		assertTrue(col.contains("a")); //$NON-NLS-1$
		col = this.repository.getAddresses(this.id2);
		assertFalse(col.isEmpty());
		assertEquals(1, col.size());
		assertTrue(col.contains("b")); //$NON-NLS-1$
		//
		this.repository.unregisterParticipant("b", this.listener1); //$NON-NLS-1$
		col = this.repository.getAddresses(this.id1);
		assertFalse(col.isEmpty());
		assertEquals(1, col.size());
		assertTrue(col.contains("a")); //$NON-NLS-1$
		col = this.repository.getAddresses(this.id2);
		assertFalse(col.isEmpty());
		assertEquals(1, col.size());
		assertTrue(col.contains("b")); //$NON-NLS-1$
		//
		this.repository.unregisterParticipant("b", this.listener2); //$NON-NLS-1$
		col = this.repository.getAddresses(this.id1);
		assertFalse(col.isEmpty());
		assertEquals(1, col.size());
		assertTrue(col.contains("a")); //$NON-NLS-1$
		col = this.repository.getAddresses(this.id2);
		assertTrue(col.isEmpty());
		//
		this.repository.unregisterParticipant("a", this.listener1); //$NON-NLS-1$
		col = this.repository.getAddresses(this.id1);
		assertTrue(col.isEmpty());
		col = this.repository.getAddresses(this.id2);
		assertTrue(col.isEmpty());
	}

	@Test
	public void getParticipantAddresses() {
		Collection<String> col;
		col = this.repository.getParticipantAddresses();
		assertTrue(col.isEmpty());
		this.repository.registerParticipant("a", this.listener1); //$NON-NLS-1$
		this.repository.registerParticipant("b", this.listener2); //$NON-NLS-1$
		this.repository.registerParticipant("c", this.listener1); //$NON-NLS-1$
		col = this.repository.getParticipantAddresses();
		assertFalse(col.isEmpty());
		assertEquals(3, col.size());
		assertTrue(col.contains("a")); //$NON-NLS-1$
		assertTrue(col.contains("b")); //$NON-NLS-1$
		assertTrue(col.contains("c")); //$NON-NLS-1$
	}

	@Test
	public void getParticipantIDs() {
		Collection<UUID> col;
		col = this.repository.getParticipantIDs();
		assertTrue(col.isEmpty());
		this.repository.registerParticipant("a", this.listener1); //$NON-NLS-1$
		this.repository.registerParticipant("b", this.listener2); //$NON-NLS-1$
		this.repository.registerParticipant("c", this.listener1); //$NON-NLS-1$
		col = this.repository.getParticipantIDs();
		assertFalse(col.isEmpty());
		assertEquals(2, col.size());
		assertTrue(col.contains(this.id1));
		assertTrue(col.contains(this.id2));
	}

}
