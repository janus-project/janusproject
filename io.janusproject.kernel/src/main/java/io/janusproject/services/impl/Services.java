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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import org.arakhne.afc.vmutil.ObjectReferenceComparator;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.State;
import com.google.common.util.concurrent.ServiceManager;


/** Tools for launching and stopping services.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class Services {

	/** Start the services associated to the given service manager.
	 * <p>
	 * This starting function supports the {@link PrioritizedService prioritized services}.
	 * 
	 * @param manager
	 */
	public static void startServices(ServiceManager manager) {
		startServices(new GServiceManager(manager));
	}
	
	/** Start the services associated to the given service manager.
	 * <p>
	 * This starting function supports the {@link PrioritizedService prioritized services}.
	 * 
	 * @param manager
	 */
	static void startServices(IServiceManager manager) {
		List<Service> otherServices = new ArrayList<>();
		Multimap<Integer,Service> priorServices = TreeMultimap.create(
				new Comparator<Integer>() {
					@Override
					public int compare(Integer o1, Integer o2) {
						return Integer.compare(o1.intValue(), o2.intValue());
					}
				},
				ObjectReferenceComparator.SINGLETON);

		{
			Service service;
			for(Entry<State,Service> entry : manager.servicesByState().entries()) {
				if (entry.getKey()==State.NEW) {
					service = entry.getValue();
					if (service instanceof PrioritizedService) {
						priorServices.put(new Integer(((PrioritizedService)service).getStartPriority()), service);
					}
					else {
						otherServices.add(service);
					}
				}
			}
		}

		for(Service service : priorServices.values()) {
			service.startAsync().awaitRunning();
		}
		
		for(Service service : otherServices) {
			service.startAsync();
		}

		manager.awaitHealthy();
	}

	/** Stop the services associated to the given service manager.
	 * <p>
	 * This stopping function supports the {@link PrioritizedService prioritized services}.
	 * 
	 * @param manager
	 */
	public static void stopServices(ServiceManager manager) {
		stopServices(new GServiceManager(manager));
	}

	/** Stop the services associated to the given service manager.
	 * <p>
	 * This stopping function supports the {@link PrioritizedService prioritized services}.
	 * 
	 * @param manager
	 */
	static void stopServices(IServiceManager manager) {
		List<Service> otherServices = new ArrayList<>();
		Multimap<Integer,Service> priorServices = TreeMultimap.create(
				new Comparator<Integer>() {
					@Override
					public int compare(Integer o1, Integer o2) {
						return Integer.compare(o1.intValue(), o2.intValue());
					}
				},
				ObjectReferenceComparator.SINGLETON);

		{
			Service service;
			for(Entry<State,Service> entry : manager.servicesByState().entries()) {
				if (entry.getKey()!=State.TERMINATED && entry.getKey()!=State.STOPPING) {
					service = entry.getValue();
					if (service instanceof PrioritizedService) {
						priorServices.put(new Integer(((PrioritizedService)service).getStopPriority()), service);
					}
					else {
						otherServices.add(service);
					}
				}
			}
		}

		for(Service service : otherServices) {
			service.stopAsync();
		}

		for(Service service : priorServices.values()) {
			service.stopAsync().awaitTerminated();
		}
		
		manager.awaitStopped();
	}

	/** Manager of services.
	 * 
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	static interface IServiceManager {

		/** Replies the services by state.
		 * 
		 * @return the services.
		 */
		public Multimap<State,Service> servicesByState();
		
		/** Wait for all the services are started.
		 */
		public void awaitHealthy();

		/** Wait for all the services are stopped.
		 */
		public void awaitStopped();

	}
	
	/** Manager of services.
	 * 
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	static class GServiceManager implements IServiceManager {

		private final ServiceManager sm;
		
		/**
		 * @param sm
		 */
		public GServiceManager(ServiceManager sm) {
			this.sm = sm;
		}

		@Override
		public Multimap<State,Service> servicesByState() {
			return this.sm.servicesByState();
		}
		
		@Override
		public void awaitHealthy() {
			this.sm.awaitHealthy();
		}

		@Override
		public void awaitStopped() {
			this.sm.awaitStopped();
		}

	}

}
