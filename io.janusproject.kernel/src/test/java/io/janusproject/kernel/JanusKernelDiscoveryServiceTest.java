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

import io.janusproject.services.ExecutorService;
import io.janusproject.services.LogService;
import io.janusproject.services.NetworkService;
import io.janusproject.util.TwoStepConstruction;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collection;
import java.util.HashSet;
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

import com.google.common.util.concurrent.Service.Listener;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.ISet;
import com.hazelcast.core.ItemListener;


/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc","static-method"})
public class JanusKernelDiscoveryServiceTest extends Assert {

	private URI kernelURI;
	private UUID contextId;
	private ISet<Object> kernels;
	private JanusKernelDiscoveryService service;
	private HazelcastInstance hzInstance;
	private NetworkService networkService;
	private ExecutorService executorService;
	private LogService logger;

	@Before
	public void setUp() throws Exception {
		this.kernelURI = new URI("tcp://123.124.125.126:34567"); //$NON-NLS-1$
		this.contextId = UUID.randomUUID();
		this.kernels = new SetWrapper();
		this.hzInstance = Mockito.mock(HazelcastInstance.class);
		Mockito.when(this.hzInstance.getSet(Matchers.anyString())).thenReturn(this.kernels);
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
	}
	
	@Test
	public void postConstruction() {
		ArgumentCaptor<Listener> argument1 = ArgumentCaptor.forClass(Listener.class);
		ArgumentCaptor<java.util.concurrent.ExecutorService> argument2 = ArgumentCaptor.forClass(java.util.concurrent.ExecutorService.class);
		Mockito.verify(this.networkService, new Times(1)).addListener(argument1.capture(), argument2.capture());
	}

	@Test
	public void mutex() {
		assertSame(this.service, this.service.mutex());
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
	private static class SetWrapper extends HashSet<Object> implements ISet<Object> {

		private static final long serialVersionUID = -1238373881805960849L;

		/**
		 */
		public SetWrapper() {
			//
		}

		@Override
		public String getName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String addItemListener(ItemListener<Object> listener, boolean includeValue) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean removeItemListener(String registrationId) {
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
		
	}

}
