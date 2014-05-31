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

import io.janusproject.services.AsyncStateService;
import io.janusproject.services.DependentService;
import io.janusproject.services.IServiceManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.arakhne.afc.vmutil.ClassComparator;

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
	 * This starting function supports the {@link DependentService prioritized services}.
	 * 
	 * @param manager
	 */
	public static void startServices(ServiceManager manager) {
		startServices(new GoogleServiceManager(manager));
	}

	/** Start the services associated to the given service manager.
	 * <p>
	 * This starting function supports the {@link DependentService prioritized services}.
	 * 
	 * @param manager
	 */
	public static void startServices(IServiceManager manager) {
		List<Service> otherServices = new ArrayList<>();
		LinkedList<DependencyNode> serviceQueue = new LinkedList<>();
		Accessors accessors = new StartingPhaseAccessors();
		
		// Build the dependency graph
		buildDependencyGraph(manager, serviceQueue, otherServices, accessors);

		// Launch the services
		runDependencyGraph(serviceQueue, otherServices, accessors);

		manager.awaitHealthy();
	}

	/** Stop the services associated to the given service manager.
	 * <p>
	 * This stopping function supports the {@link DependentService prioritized services}.
	 * 
	 * @param manager
	 */
	public static void stopServices(ServiceManager manager) {
		stopServices(new GoogleServiceManager(manager));
	}

	/** Stop the services associated to the given service manager.
	 * <p>
	 * This stopping function supports the {@link DependentService prioritized services}.
	 * 
	 * @param manager
	 */
	public static void stopServices(IServiceManager manager) {
		List<Service> otherServices = new ArrayList<>();
		LinkedList<DependencyNode> serviceQueue = new LinkedList<>();
		Accessors accessors = new StoppingPhaseAccessors();
		
		// Build the dependency graph
		buildInvertedDependencyGraph(manager, serviceQueue, otherServices, accessors);

		// Launch the services
		runDependencyGraph(serviceQueue, otherServices, accessors);

		manager.awaitStopped();
	}

	/** Build the dependency graph for the services.
	 * 
	 * @param manager - lsit of the services.
	 * @param roots - filled with the services that have no dependency.
	 * @param freeServices - filled with the services that are executed before/after all the dependent services.
	 * @param accessors - permits to retreive information on the services.
	 */
	private static void buildDependencyGraph(
			IServiceManager manager,
			List<DependencyNode> roots, List<Service> freeServices,
			Accessors accessors) {
		Map<Class<? extends Service>, DependencyNode> dependentServices = new TreeMap<>(ClassComparator.SINGLETON);

		Service service;
		for(Entry<State,Service> entry : manager.servicesByState().entries()) {
			if (accessors.matches(entry.getKey())) {
				service = entry.getValue();
				if (service instanceof DependentService) {
					DependentService depServ = (DependentService)service;
					Class<? extends Service> type = depServ.getServiceType();
					DependencyNode node = dependentServices.get(type);
					if (node==null) {
						node = new DependencyNode(depServ, type);
						dependentServices.put(type, node);
					}
					else {
						assert(node.service==null);
						node.service = depServ;
					}

					boolean isRoot = true;
					Collection<Class<? extends Service>> deps = depServ.getServiceDependencies();
					for(Class<? extends Service> dep : deps) {
						isRoot = false;
						DependencyNode depNode = dependentServices.get(dep);
						if (depNode==null) {
							depNode = new DependencyNode(dep);
							dependentServices.put(dep, depNode);
						}
						depNode.nextServices.add(node);
					}

					deps = depServ.getServiceWeakDependencies();
					for(Class<? extends Service> dep : deps) {
						isRoot = false;
						DependencyNode depNode = dependentServices.get(dep);
						if (depNode==null) {
							depNode = new DependencyNode(dep);
							dependentServices.put(dep, depNode);
						}
						depNode.nextWeakServices.add(node);
					}

					if (isRoot) {
						roots.add(node);
					}
				}
				else {
					freeServices.add(service);
				}
			}
		}
		
		if (accessors.isAsyncStateWaitingEnabled()) {
			for(DependencyNode node : dependentServices.values()) {
				assert(node.service!=null);
				if (node.service instanceof AsyncStateService) {
					for(DependencyNode next : node.nextServices) {
						next.asyncStateServices.add(new WeakReference<>(node));
					}
				}
			}
		}
	}

	/** Build the dependency graph for the services.
	 * 
	 * @param manager - lsit of the services.
	 * @param roots - filled with the services that have no dependency.
	 * @param freeServices - filled with the services that are executed before/after all the dependent services.
	 * @param accessors - permits to retreive information on the services.
	 */
	private static void buildInvertedDependencyGraph(
			IServiceManager manager,
			List<DependencyNode> roots, List<Service> freeServices,
			Accessors accessors) {
		Map<Class<? extends Service>,DependencyNode> dependentServices = new TreeMap<>(ClassComparator.SINGLETON);
		Map<Class<? extends Service>,DependencyNode> rootServices = new TreeMap<>(ClassComparator.SINGLETON);

		Service service;
		for(Entry<State,Service> entry : manager.servicesByState().entries()) {
			if (accessors.matches(entry.getKey())) {
				service = entry.getValue();
				if (service instanceof DependentService) {
					DependentService depServ = (DependentService)service;
					Class<? extends Service> type = depServ.getServiceType();
					DependencyNode node = dependentServices.get(type);
					boolean isRoot = true;
					if (node==null) {
						node = new DependencyNode(depServ, type);
						dependentServices.put(type, node);
					}
					else {
						assert(node.service==null);
						node.service = depServ;
						isRoot = false;
					}

					Collection<Class<? extends Service>> deps = depServ.getServiceDependencies();
					for(Class<? extends Service> dep : deps) {
						DependencyNode depNode = dependentServices.get(dep);
						if (depNode==null) {
							depNode = new DependencyNode(dep);
							dependentServices.put(dep, depNode);
						}
						node.nextServices.add(depNode);
						rootServices.remove(depNode.type);
					}

					deps = depServ.getServiceWeakDependencies();
					for(Class<? extends Service> dep : deps) {
						DependencyNode depNode = dependentServices.get(dep);
						if (depNode==null) {
							depNode = new DependencyNode(dep);
							dependentServices.put(dep, depNode);
						}
						node.nextWeakServices.add(depNode);
						rootServices.remove(depNode.type);
					}
					
					if (isRoot) rootServices.put(type, node);
				}
				else {
					freeServices.add(service);
				}
			}
		}
		
		roots.addAll(rootServices.values());
	}

	/** Run the dependency graph for the services.
	 * 
	 * @param roots - filled with the services that have no dependency.
	 * @param freeServices - filled with the services that are executed before/after all the dependent services.
	 * @param accessors - permits to retreive information on the services.
	 */
	private static void runDependencyGraph(
			LinkedList<DependencyNode> roots, List<Service> freeServices,
			Accessors accessors) {
		final boolean async = accessors.isAsyncStateWaitingEnabled();
		Set<Class<? extends Service>> executed = new TreeSet<>(ClassComparator.SINGLETON);
		accessors.runBefore(freeServices);
		while (!roots.isEmpty()) {
			DependencyNode node = roots.removeFirst();
			if (node!=null) {
				assert(node.type!=null);
				if (!executed.contains(node.type)) {
					executed.add(node.type);
					roots.addAll(node.nextServices);
					roots.addAll(node.nextWeakServices);
					if (node.service!=null) {
						if (async) {
							for(WeakReference<DependencyNode> asyncService : node.asyncStateServices) {
								AsyncStateService as = (AsyncStateService)(asyncService.get().service);
								if (as!=null) {
									while (!as.isReadyForOtherServices()) {
										Thread.yield();
									}
								}
							}
						}
						accessors.run(node.service);
					}
				}
			}
		}
		accessors.runAfter(freeServices);
	}
	
	/**
	 */
	private static class DependencyNode {

		public Service service;
		public final Class<? extends Service> type;
		public final Collection<DependencyNode> nextServices = new ArrayList<>();
		public final Collection<DependencyNode> nextWeakServices = new ArrayList<>();
		public final Collection<WeakReference<DependencyNode>> asyncStateServices = new ArrayList<>();

		public DependencyNode(DependentService service, Class<? extends Service> type) {
			this.service = service;
			this.type = type;
		}

		public DependencyNode(Class<? extends Service> type) {
			this.service = null;
			this.type = type;
		}

		/** {@inheritDoc}
		 */
		@Override
		public String toString() {
			return this.service==null ? null : this.service.toString();
		}
		
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static interface Accessors {

		public boolean matches(State element);

		public void runBefore(List<Service> freeServices);
		
		public boolean isAsyncStateWaitingEnabled();
		
		public void run(Service service);

		public void runAfter(List<Service> freeServices);

	}
	
	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class StartingPhaseAccessors implements Accessors {
		
		/**
		 */
		public StartingPhaseAccessors() {
			//
		}
		
		@Override
		public boolean matches(State element) {
			return element==State.NEW;
		}
		@Override
		public void runBefore(List<Service> freeServices) {
			//
		}
		@Override
		public boolean isAsyncStateWaitingEnabled() {
			return true;
		}
		@Override
		public void run(Service service) {
			service.startAsync().awaitRunning();
		}
		@Override
		public void runAfter(List<Service> freeServices) {
			for(Service serv : freeServices) {
				serv.startAsync();
			}
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class StoppingPhaseAccessors implements Accessors {
		
		/**
		 */
		public StoppingPhaseAccessors() {
			//
		}
		
		@Override
		public boolean matches(State element) {
			return element!=State.TERMINATED && element!=State.STOPPING;
		}
		@Override
		public void runBefore(List<Service> freeServices) {
			for(Service serv : freeServices) {
				serv.stopAsync();
			}
		}
		@Override
		public boolean isAsyncStateWaitingEnabled() {
			return false;
		}
		@Override
		public void run(Service service) {
			service.stopAsync().awaitTerminated();
		}
		@Override
		public void runAfter(List<Service> freeServices) {
			//
		}

	}

}
