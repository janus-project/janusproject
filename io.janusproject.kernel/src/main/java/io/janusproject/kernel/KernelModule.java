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

import io.janusproject.JanusConfig;
import io.janusproject.JanusDefaultModule;
import io.janusproject.kernel.executor.JanusScheduledThreadPoolExecutor;
import io.janusproject.kernel.executor.JanusThreadFactory;
import io.janusproject.kernel.executor.JanusThreadPoolExecutor;
import io.janusproject.kernel.executor.JanusUncaughtExceptionHandler;
import io.janusproject.services.agentplatform.ContextSpaceService;
import io.janusproject.services.agentplatform.ExecutorService;
import io.janusproject.services.agentplatform.KernelDiscoveryService;
import io.janusproject.services.agentplatform.LogService;
import io.janusproject.services.agentplatform.NetworkService;
import io.janusproject.services.agentplatform.SpawnService;
import io.janusproject.services.api.IServiceManager;
import io.janusproject.services.impl.ArakhneLocaleLogService;
import io.janusproject.services.impl.GoogleServiceManager;
import io.sarl.lang.core.AgentContext;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Logger;

import com.google.common.eventbus.AsyncSyncEventBus;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.common.util.concurrent.Service;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/** The Core Janus Module configures the minimum requirements for Janus to
 * run properly. If you need a standard configuration use
 * {@link JanusDefaultModule}.
 *
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class KernelModule extends AbstractModule {
	/** {@inheritDoc}
	 */
	@Override
	protected void configure() {
		requireBinding(Key.get(UUID.class, Names.named(JanusConfig.DEFAULT_CONTEXT_ID_NAME)));
		requireBinding(Key.get(UUID.class, Names.named(JanusConfig.DEFAULT_SPACE_ID_NAME)));
		requireBinding(NetworkService.class);
		requireBinding(Logger.class);

		bind(UncaughtExceptionHandler.class).to(JanusUncaughtExceptionHandler.class).in(Singleton.class);
		bind(ThreadFactory.class).to(JanusThreadFactory.class).in(Singleton.class);
		bind(java.util.concurrent.ExecutorService.class).to(JanusThreadPoolExecutor.class).in(Singleton.class);
		bind(ScheduledExecutorService.class).to(JanusScheduledThreadPoolExecutor.class).in(Singleton.class);

		// Bind the services, indiviually
		bind(LogService.class).to(ArakhneLocaleLogService.class).in(Singleton.class);
		bind(ContextSpaceService.class).to(JanusContextSpaceService.class).in(Singleton.class);
		bind(ExecutorService.class).to(JanusExecutorService.class).in(Singleton.class);
		bind(KernelDiscoveryService.class).to(JanusKernelDiscoveryService.class).in(Singleton.class);
		bind(SpawnService.class).to(JanusSpawnService.class).in(Singleton.class);

		// Create a binder for: Set<Service>
		// (This set is given to the service manager to launch the services).
		Multibinder<Service> serviceSetBinder = Multibinder.newSetBinder(binder(), Service.class);
		serviceSetBinder.addBinding().to(LogService.class);
		serviceSetBinder.addBinding().to(ExecutorService.class);
		serviceSetBinder.addBinding().to(ContextSpaceService.class);
		serviceSetBinder.addBinding().to(KernelDiscoveryService.class);
		serviceSetBinder.addBinding().to(SpawnService.class);
	}

	@Provides
	@io.janusproject.kernel.annotations.Kernel
	@Singleton
	private static AgentContext getKernel(
			ContextSpaceService contextService,
			@Named(JanusConfig.DEFAULT_CONTEXT_ID_NAME) UUID janusContextID,
			@Named(JanusConfig.DEFAULT_SPACE_ID_NAME) UUID defaultJanusSpaceId) {
		return contextService.createContext(janusContextID, defaultJanusSpaceId);
	}

	@Provides
	private static AsyncSyncEventBus createAgentInternalBus(
			Injector injector,
			java.util.concurrent.ExecutorService service,
			SubscriberExceptionHandler exceptionHandler) {
		AsyncSyncEventBus aeb = new AsyncSyncEventBus(service, exceptionHandler);
		// to be able to inject the SubscriberFindingStrategy
		injector.injectMembers(aeb);
		return aeb;
	}

	@Provides
	@Singleton
	private static IServiceManager createServiceManager(Set<Service> services) {
		return new GoogleServiceManager(services);
	}

}
