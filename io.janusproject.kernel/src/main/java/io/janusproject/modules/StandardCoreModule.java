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
package io.janusproject.modules;

import io.janusproject.modules.executors.JdkExecutorModule;
import io.janusproject.modules.hazelcast.HazelcastModule;
import io.janusproject.modules.kernel.MandatoryKernelModule;
import io.janusproject.services.contextspace.ContextSpaceService;
import io.janusproject.services.contextspace.base.BaseContextSpaceService;
import io.janusproject.services.distributeddata.DistributedDataStructureService;
import io.janusproject.services.executor.ExecutorService;
import io.janusproject.services.kerneldiscovery.KernelDiscoveryService;
import io.janusproject.services.logging.LogService;
import io.janusproject.services.logging.jdk.ArakhneLocaleLogService;
import io.janusproject.services.network.NetworkService;
import io.janusproject.services.spawn.SpawnService;
import io.janusproject.services.spawn.base.BaseSpawnService;

import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

/**
 * The Core Janus Module configures the minimum requirements
 * for Janus to run properly. If you need a standard configuration
 * use <code>JanusDefaultModule</code>
 *
 *
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class StandardCoreModule extends AbstractModule {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configure() {
		// Install the elements for the Janus kernel
		install(new MandatoryKernelModule());

		install(new HazelcastModule());
		install(new JdkExecutorModule());

		bind(LogService.class).to(ArakhneLocaleLogService.class).in(Singleton.class);
		bind(ContextSpaceService.class).to(BaseContextSpaceService.class).in(Singleton.class);
		bind(SpawnService.class).to(BaseSpawnService.class).in(Singleton.class);

		// Check if all the services are binded
		requireBinding(DistributedDataStructureService.class);
		requireBinding(KernelDiscoveryService.class);
		requireBinding(ExecutorService.class);
		requireBinding(ContextSpaceService.class);
		requireBinding(LogService.class);
		requireBinding(NetworkService.class);
		requireBinding(SpawnService.class);

		// Create a binder for: Set<Service>
		// (This set is given to the service manager to launch the services).
		Multibinder<Service> serviceSetBinder = Multibinder.newSetBinder(binder(), Service.class);
		serviceSetBinder.addBinding().to(LogService.class);
		serviceSetBinder.addBinding().to(ExecutorService.class);
		serviceSetBinder.addBinding().to(ContextSpaceService.class);
		serviceSetBinder.addBinding().to(KernelDiscoveryService.class);
		serviceSetBinder.addBinding().to(SpawnService.class);
		serviceSetBinder.addBinding().to(DistributedDataStructureService.class);
	}

}
