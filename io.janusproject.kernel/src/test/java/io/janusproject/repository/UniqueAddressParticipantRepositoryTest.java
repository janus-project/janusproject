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
package io.janusproject.repository;

import io.sarl.lang.core.EventListener;

import java.util.Collection;
import java.util.HashMap;
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
public class UniqueAddressParticipantRepositoryTest extends Assert {

	private String distributedName;
	private UniqueAddressParticipantRepository<String> repository;
	private DistributedDataStructureFactory factory;
	private UUID id1;
	private UUID id2;
	private EventListener listener1;
	private EventListener listener2;

	@Before
	public void setUp() {
		this.distributedName = getClass().getName()+UUID.randomUUID().toString();
		this.factory = Mockito.mock(DistributedDataStructureFactory.class);
		Mockito.when(this.factory.getMap(this.distributedName)).thenReturn(new HashMap<>());
		this.repository = new UniqueAddressParticipantRepository<>(this.distributedName);
		this.repository.setFactory(this.factory);
		this.id1 = UUID.randomUUID();
		this.id2 = UUID.randomUUID();
		this.listener1 = Mockito.mock(EventListener.class);
		Mockito.when(this.listener1.getID()).thenReturn(this.id1);
		this.listener2 = Mockito.mock(EventListener.class);
		Mockito.when(this.listener2.getID()).thenReturn(this.id2);
	}

	@After
	public void tearDown() {
		this.repository = null;
		this.distributedName = null;
		this.factory = null;
		this.id1 = this.id2 = null;
		this.listener1 = this.listener2 = null;
	}

	@Test
	public void registerParticipant() {	
		assertNull(this.repository.getAddress(this.listener1));
		assertNull(this.repository.getAddress(this.listener2));
		assertEquals("a", this.repository.registerParticipant("a", this.listener1)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("b", this.repository.registerParticipant("b", this.listener2)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("a", this.repository.getAddress(this.listener1)); //$NON-NLS-1$
		assertEquals("b", this.repository.getAddress(this.listener2)); //$NON-NLS-1$
	}

	@Test
	public void unregisterParticipantEventListener() {	
		assertEquals("a", this.repository.registerParticipant("a", this.listener1)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("b", this.repository.registerParticipant("b", this.listener2)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("a", this.repository.getAddress(this.listener1)); //$NON-NLS-1$
		assertEquals("b", this.repository.getAddress(this.listener2)); //$NON-NLS-1$
		//
		assertEquals("a", this.repository.unregisterParticipant(this.listener1)); //$NON-NLS-1$
		assertEquals("b", this.repository.unregisterParticipant(this.listener2)); //$NON-NLS-1$
	}

	@Test
	public void unregisterParticipantUUID() {	
		assertEquals("a", this.repository.registerParticipant("a", this.listener1)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("b", this.repository.registerParticipant("b", this.listener2)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("a", this.repository.getAddress(this.listener1)); //$NON-NLS-1$
		assertEquals("b", this.repository.getAddress(this.listener2)); //$NON-NLS-1$
		//
		assertEquals("a", this.repository.unregisterParticipant(this.id1)); //$NON-NLS-1$
		assertEquals("b", this.repository.unregisterParticipant(this.id2)); //$NON-NLS-1$
	}

	@Test
	public void getAddress() {	
		assertNull(this.repository.getAddress(this.listener1));
		assertNull(this.repository.getAddress(this.listener2));
		assertEquals("a", this.repository.registerParticipant("a", this.listener1)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("b", this.repository.registerParticipant("b", this.listener2)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("a", this.repository.getAddress(this.listener1)); //$NON-NLS-1$
		assertEquals("b", this.repository.getAddress(this.listener2)); //$NON-NLS-1$
	}

	@Test
	public void getAddressUUID() {
		assertNull(this.repository.getAddress(this.listener1));
		assertNull(this.repository.getAddress(this.listener2));
		assertEquals("a", this.repository.registerParticipant("a", this.listener1)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("b", this.repository.registerParticipant("b", this.listener2)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("a", this.repository.getAddress(this.id1)); //$NON-NLS-1$
		assertEquals("b", this.repository.getAddress(this.id2)); //$NON-NLS-1$
	}

	@Test
	public void getParticipantAddresses() {	
		assertNull(this.repository.getAddress(this.listener1));
		assertNull(this.repository.getAddress(this.listener2));
		assertEquals("a", this.repository.registerParticipant("a", this.listener1)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("b", this.repository.registerParticipant("b", this.listener2)); //$NON-NLS-1$ //$NON-NLS-2$
		Collection<String> adrs = this.repository.getParticipantAddresses();
		assertNotNull(adrs);
		assertEquals(2, adrs.size());
		assertTrue(adrs.contains("a")); //$NON-NLS-1$
		assertTrue(adrs.contains("b")); //$NON-NLS-1$
	}

	@Test
	public void getParticipantIDs() {	
		assertNull(this.repository.getAddress(this.listener1));
		assertNull(this.repository.getAddress(this.listener2));
		assertEquals("a", this.repository.registerParticipant("a", this.listener1)); //$NON-NLS-1$ //$NON-NLS-2$
		assertEquals("b", this.repository.registerParticipant("b", this.listener2)); //$NON-NLS-1$ //$NON-NLS-2$
		Collection<UUID> adrs = this.repository.getParticipantIDs();
		assertNotNull(adrs);
		assertEquals(2, adrs.size());
		assertTrue(adrs.contains(this.id1));
		assertTrue(adrs.contains(this.id2));
	}

}
