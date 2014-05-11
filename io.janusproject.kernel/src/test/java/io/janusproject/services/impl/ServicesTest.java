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
package io.janusproject.services.impl;

import io.janusproject.services.PrioritizedService;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.State;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc"})
public class ServicesTest extends Assert {

	private Multimap<State,Service> services;
	private IServiceManager serviceManager;
	private List<Service> encounteredServices;
	
	@Before
	public void setUp() {
		Random r = new Random();
		this.encounteredServices = new LinkedList<>();
		this.services = LinkedListMultimap.create();
		for(int i=0; i<10; ++i) {
			PrioritizedService serv = Mockito.mock(PrioritizedService.class);
			Mockito.when(serv.startAsync()).thenAnswer(new EncounteredServiceAnswer(serv));
			Mockito.when(serv.stopAsync()).thenAnswer(new EncounteredServiceAnswer(serv));
			Mockito.when(serv.getStartPriority()).thenReturn(r.nextInt());
			Mockito.when(serv.getStopPriority()).thenReturn(r.nextInt());
			this.services.put(State.NEW, serv);
		}
		for(int i=0; i<10; ++i) {
			Service serv = Mockito.mock(Service.class);
			Mockito.when(serv.startAsync()).thenAnswer(new EncounteredServiceAnswer(serv));
			Mockito.when(serv.stopAsync()).thenAnswer(new EncounteredServiceAnswer(serv));
			this.services.put(State.NEW, serv);
		}
		this.serviceManager = Mockito.mock(IServiceManager.class);
		Mockito.when(this.serviceManager.servicesByState()).thenReturn(this.services);
	}
	
	@After
	public void tearDown() {
		this.serviceManager = null;
		this.services = null;
		this.encounteredServices = null;
	}

	@Test
	public void startServices() {
		Services.startServices(this.serviceManager);
		assertStartPriorities();
	}

	@Test
	public void stopServices() {
		Services.stopServices(this.serviceManager);
		assertStopPriorities();
	}
	
	private void assertStartPriorities() {
		assertEquals(this.services.size(), this.encounteredServices.size());

		boolean isPrior = true;
		int previous = Integer.MIN_VALUE;
		while (!this.encounteredServices.isEmpty()) {
			Service service = this.encounteredServices.remove(0);
			if (isPrior) {
				if (service instanceof PrioritizedService) {
					PrioritizedService serv = (PrioritizedService)service;
					int p = serv.getStartPriority();
					if (previous>=p) {
						fail("Invalid order for the prioritized services. Previous priority: "+previous+"; current priority: "+p); //$NON-NLS-1$ //$NON-NLS-2$
					}
					previous = p ;
				}
				else {
					isPrior = false;
					this.encounteredServices.add(0, service);
				}
			}
			else if (service instanceof PrioritizedService) {
				fail("Invalid service launching order"); //$NON-NLS-1$
			}
		}
	}

	private void assertStopPriorities() {
		assertEquals(this.services.size(), this.encounteredServices.size());

		boolean isPrior = false;
		int previous = Integer.MIN_VALUE;
		while (!this.encounteredServices.isEmpty()) {
			Service service = this.encounteredServices.remove(0);
			if (!isPrior) {
				if (service instanceof PrioritizedService) {
					isPrior = true;
					this.encounteredServices.add(0, service);
				}
			}
			else if (!(service instanceof PrioritizedService)) {
				fail("Invalid service launching order"); //$NON-NLS-1$
			}
			else {
				PrioritizedService serv = (PrioritizedService)service;
				int p = serv.getStopPriority();
				if (previous>=p) {
					fail("Invalid order for the prioritized services. Previous priority: "+previous+"; current priority: "+p); //$NON-NLS-1$ //$NON-NLS-2$
				}
				previous = p ;
			}
		}
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class EncounteredServiceAnswer implements Answer<Service> {

		private final Service service;
		
		/**
		 * @param service
		 */
		public EncounteredServiceAnswer(Service service) {
			this.service = service;
		}

		/** {@inheritDoc}
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public Service answer(InvocationOnMock invocation) throws Throwable {
			ServicesTest.this.encounteredServices.add(this.service);
			return this.service;
		}
		
	}
	
}
