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

import io.janusproject.network.NetworkUtil;
import io.janusproject.services.ExecutorService;
import io.janusproject.services.LogService;
import io.janusproject.services.NetworkService;
import io.janusproject.util.TwoStepConstruction;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javassist.Modifier;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;

import com.google.common.util.concurrent.Service.Listener;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.EntryListener;
import com.hazelcast.core.EntryView;
import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.Member;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.map.MapInterceptor;
import com.hazelcast.monitor.LocalMapStats;
import com.hazelcast.query.Predicate;


/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc","static-method"})
public class JanusKernelDiscoveryServiceTest extends Assert {

	private URI kernelURI;
	private URI hazelcastURI;
	private UUID contextId;
	private IMap<Object,Object> kernels;
	private JanusKernelDiscoveryService service;
	private HazelcastInstance hzInstance;
	private NetworkService networkService;
	private ExecutorService executorService;
	private LogService logger;

	@Before
	public void setUp() throws Exception {
		this.hazelcastURI = NetworkUtil.toURI("tcp://123.124.125.126:5023"); //$NON-NLS-1$
		this.kernelURI = NetworkUtil.toURI("tcp://123.124.125.126:34567"); //$NON-NLS-1$
		this.contextId = UUID.randomUUID();
		this.kernels = new MapWrapper();
		this.hzInstance = Mockito.mock(HazelcastInstance.class);
		{
			Mockito.when(this.hzInstance.getMap(Matchers.anyString())).thenReturn(this.kernels);
			Cluster clusterMock = Mockito.mock(Cluster.class);
			Mockito.when(this.hzInstance.getCluster()).thenReturn(clusterMock);
			Member memberMock = Mockito.mock(Member.class);
			Mockito.when(clusterMock.getLocalMember()).thenReturn(memberMock);
			InetSocketAddress adr = NetworkUtil.toInetSocketAddress(this.hazelcastURI);
			Mockito.when(memberMock.getSocketAddress()).thenReturn(adr);
		}
		this.networkService = Mockito.mock(NetworkService.class);
		Mockito.when(this.networkService.getURI()).thenReturn(this.kernelURI);
		this.executorService = Mockito.mock(ExecutorService.class);
		this.logger = Mockito.mock(LogService.class);
		this.service = new JanusKernelDiscoveryService(this.contextId);
		this.service.postConstruction(this.hzInstance, this.networkService,
				this.executorService, this.logger);
	}
	
	@After
	public void tearDown() {
		this.contextId = null;
		this.kernels = null;
		this.service = null;
		this.hzInstance = null;
		this.networkService = null;
		this.executorService = null;
		this.logger = null;
		this.kernelURI = null;
		this.hazelcastURI = null;
	}
	
	@Test
	public void postConstruction() {
		ArgumentCaptor<Listener> argument1 = ArgumentCaptor.forClass(Listener.class);
		ArgumentCaptor<java.util.concurrent.ExecutorService> argument2 = ArgumentCaptor.forClass(java.util.concurrent.ExecutorService.class);
		Mockito.verify(this.networkService, new Times(1)).addListener(argument1.capture(), argument2.capture());
	}

	@Test
	public void getCurrentKernel_nonetworknotification() {
		assertNull(this.service.getCurrentKernel());
	}

	@Test
	public void getKernels_nonetworknotification() {
		assertTrue(this.service.getKernels().isEmpty());
	}
	
	@Test
	public void getCurrentKernel_networknotification() {
		ArgumentCaptor<Listener> argument1 = ArgumentCaptor.forClass(Listener.class);
		ArgumentCaptor<java.util.concurrent.ExecutorService> argument2 = ArgumentCaptor.forClass(java.util.concurrent.ExecutorService.class);
		Mockito.verify(this.networkService, new Times(1)).addListener(argument1.capture(), argument2.capture());
		Listener listener = argument1.getValue();
		listener.running();
		//
		assertEquals(this.kernelURI, this.service.getCurrentKernel());
	}

	@Test
	public void getKernels_networknotification() {
		ArgumentCaptor<Listener> argument1 = ArgumentCaptor.forClass(Listener.class);
		ArgumentCaptor<java.util.concurrent.ExecutorService> argument2 = ArgumentCaptor.forClass(java.util.concurrent.ExecutorService.class);
		Mockito.verify(this.networkService, new Times(1)).addListener(argument1.capture(), argument2.capture());
		Listener listener = argument1.getValue();
		listener.running();
		//
		Collection<URI> c = this.service.getKernels();
		assertNotNull(c);
		assertEquals(1, c.size());
		assertTrue(c.contains(this.kernelURI));
	}

	@Test
	public void twoStepConstruction() throws Exception {
		TwoStepConstruction annotation = JanusKernelDiscoveryService.class.getAnnotation(TwoStepConstruction.class);
		assertNotNull(annotation);
		for(String name : annotation.names()) {
			for(Method method : JanusKernelDiscoveryService.class.getMethods()) {
				if (name.equals(method.getName())) {
					assertTrue(Modifier.isPackage(method.getModifiers())
							||Modifier.isPublic(method.getModifiers()));
					break;
				}
			}
		}
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@SuppressWarnings("rawtypes")
	private static class MapWrapper extends HashMap<Object,Object> implements IMap<Object,Object> {

		private static final long serialVersionUID = -1238373881805960849L;

		/**
		 */
		public MapWrapper() {
			//
		}

		@Override
		public String getName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object getId() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getPartitionKey() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getServiceName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void destroy() {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean remove(Object key, Object value) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public void delete(Object key) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public void flush() {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Map<Object, Object> getAll(Set<Object> keys) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Future<Object> getAsync(Object key) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Future<Object> putAsync(Object key, Object value) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Future<Object> putAsync(Object key, Object value, long ttl,
				TimeUnit timeunit) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Future<Object> removeAsync(Object key) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean tryRemove(Object key, long timeout, TimeUnit timeunit) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean tryPut(Object key, Object value, long timeout,
				TimeUnit timeunit) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Object put(Object key, Object value, long ttl, TimeUnit timeunit) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public void putTransient(Object key, Object value, long ttl,
				TimeUnit timeunit) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Object putIfAbsent(Object key, Object value) {
			if (!containsKey(key)) {
				return put(key, value);
			}
			return null;
		}

		/** {@inheritDoc}
		 */
		@Override
		public Object putIfAbsent(Object key, Object value, long ttl,
				TimeUnit timeunit) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean replace(Object key, Object oldValue, Object newValue) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Object replace(Object key, Object value) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public void set(Object key, Object value) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public void set(Object key, Object value, long ttl, TimeUnit timeunit) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public void lock(Object key) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public void lock(Object key, long leaseTime, TimeUnit timeUnit) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean isLocked(Object key) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean tryLock(Object key) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean tryLock(Object key, long time, TimeUnit timeunit)
				throws InterruptedException {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public void unlock(Object key) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public void forceUnlock(Object key) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public String addLocalEntryListener(
				EntryListener<Object, Object> listener) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public String addLocalEntryListener(
				EntryListener<Object, Object> listener,
				Predicate<Object, Object> predicate, boolean includeValue) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public String addLocalEntryListener(
				EntryListener<Object, Object> listener,
				Predicate<Object, Object> predicate, Object key,
				boolean includeValue) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public String addInterceptor(MapInterceptor interceptor) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public void removeInterceptor(String id) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public String addEntryListener(EntryListener<Object, Object> listener,
				boolean includeValue) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean removeEntryListener(String id) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public String addEntryListener(EntryListener<Object, Object> listener,
				Object key, boolean includeValue) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public String addEntryListener(EntryListener<Object, Object> listener,
				Predicate<Object, Object> predicate, boolean includeValue) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public String addEntryListener(EntryListener<Object, Object> listener,
				Predicate<Object, Object> predicate, Object key,
				boolean includeValue) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public EntryView<Object, Object> getEntryView(Object key) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean evict(Object key) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Set<Object> keySet(Predicate predicate) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Set<java.util.Map.Entry<Object, Object>> entrySet(
				Predicate predicate) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Collection<Object> values(Predicate predicate) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Set<Object> localKeySet() {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Set<Object> localKeySet(Predicate predicate) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public void addIndex(String attribute, boolean ordered) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public LocalMapStats getLocalMapStats() {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Object executeOnKey(Object key, EntryProcessor entryProcessor) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Map<Object, Object> executeOnKeys(Set<Object> keys,
				EntryProcessor entryProcessor) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public void submitToKey(Object key, EntryProcessor entryProcessor,
				ExecutionCallback callback) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Future submitToKey(Object key, EntryProcessor entryProcessor) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Map<Object, Object> executeOnEntries(
				EntryProcessor entryProcessor) {
			throw new UnsupportedOperationException();
		}

		/** {@inheritDoc}
		 */
		@Override
		public Map<Object, Object> executeOnEntries(
				EntryProcessor entryProcessor, Predicate predicate) {
			throw new UnsupportedOperationException();
		}
		
	}

}
