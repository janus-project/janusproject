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
package io.janusproject.kernel.hazelcast;

import io.janusproject.repository.MultiMap;

import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.core.ISet;


/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc","static-access","unchecked"})
public class HazelcastDistributedDataStructureFactoryTest extends Assert {

	private HazelcastInstance hz;
	private IList<Object> list;
	private IMap<Object,Object> map;
	private com.hazelcast.core.MultiMap<Object,Object> multimap;
	private IQueue<Object> queue;
	private ISemaphore semaphore;
	private ISet<Object> set;
	private HazelcastDistributedDataStructureFactory factory;
	
	@Before
	public void setUp() {
		this.list = Mockito.mock(IList.class);
		this.map = Mockito.mock(IMap.class);
		this.multimap = Mockito.mock(com.hazelcast.core.MultiMap.class);
		this.queue = Mockito.mock(IQueue.class);
		this.semaphore = Mockito.mock(ISemaphore.class);
		this.set = Mockito.mock(ISet.class);
		this.hz = Mockito.mock(HazelcastInstance.class);
		Mockito.when(this.hz.getMap(Mockito.any(String.class))).thenReturn(this.map);
		Mockito.when(this.hz.getMultiMap(Mockito.any(String.class))).thenReturn(this.multimap);
		Mockito.when(this.hz.getQueue(Mockito.any(String.class))).thenReturn(this.queue);
		Mockito.when(this.hz.getSemaphore(Mockito.any(String.class))).thenReturn(this.semaphore);
		Mockito.when(this.hz.getSet(Mockito.any(String.class))).thenReturn(this.set);
		Mockito.when(this.hz.getList(Mockito.any(String.class))).thenReturn(this.list);
		this.factory = new HazelcastDistributedDataStructureFactory();
		this.factory.setHazelcastInstance(this.hz);
	}
	
	@After
	public void tearDown() {
		this.factory = null;
		this.hz = null;
		this.list = null;
		this.map = null;
		this.multimap = null;
		this.queue = null;
		this.semaphore = null;
		this.set = null;
	}
	
	@Test
	public void getMap() {
		IMap<Object,Object> m = this.factory.getMap(UUID.randomUUID().toString());
		assertSame(this.map, m);
	}

	@Test
	public void getMultiMap() {
		MultiMap<Object,Object> m = this.factory.getMultiMap(UUID.randomUUID().toString());
		
		m.put("a", "b");  //$NON-NLS-1$//$NON-NLS-2$
		
		ArgumentCaptor<Object> argument1 = ArgumentCaptor.forClass(Object.class);
		ArgumentCaptor<Object> argument2 = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(this.multimap).put(argument1.capture(), argument2.capture());
		assertEquals("a", argument1.getValue()); //$NON-NLS-1$
		assertEquals("b", argument2.getValue()); //$NON-NLS-1$
	}

	@Test
	public void getQueue() {
		IQueue<Object> m = this.factory.getQueue(UUID.randomUUID().toString());
		assertSame(this.queue, m);
	}

	@Test
	public void getSet() {
		ISet<Object> m = this.factory.getSet(UUID.randomUUID().toString());
		assertSame(this.set, m);
	}

	@Test
	public void getList() {
		IList<Object> m = this.factory.getList(UUID.randomUUID().toString());
		assertSame(this.list, m);
	}

	@Test
	public void getSemaphore() {
		ISemaphore m = this.factory.getSemaphore(UUID.randomUUID().toString());
		assertSame(this.semaphore, m);
	}

}
