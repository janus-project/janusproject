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

import io.janusproject.kernel.annotations.Kernel;
import io.janusproject.kernel.executor.AgentScheduledExecutorService;
import io.janusproject.repository.AddressSerializer;
import io.janusproject.repository.ContextRepository;
import io.janusproject.repository.JanusConfig;
import io.janusproject.repository.KernelRepositoryService;
import io.janusproject.repository.SpaceIDSerializer;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.BuiltinCapacitiesProvider;
import io.sarl.lang.core.EventSpaceSpecification;
import io.sarl.lang.core.Percept;
import io.sarl.lang.core.SpaceID;
import io.sarl.util.OpenEventSpaceSpecification;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.google.common.eventbus.AnnotationModule;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.hazelcast.config.Config;
import com.hazelcast.config.SerializerConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

/**
 * The Core Janus Module configures the minimum requirements for Janus to run
 * properly. If you need a standard configuration use
 * {@link JanusDefaultConfigModule}
 * 
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class CoreModule extends AbstractModule {

	private Context janusContext = null;
	private ExecutorService executorService;
	private ExecutorService kernelExecutorService;
	private ScheduledExecutorService scheduledExecutorService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configure() {

		UUID JanusContextID = UUID.fromString("2c38fb7f-f363-4f6e-877b-110b1f07cc77");

		UUID defaultJanusSpaceId = UUID.fromString("7ba8885d-545b-445a-a0e9-b655bc15ebe0");

		bind(UUID.class).annotatedWith(Names.named(JanusConfig.JANUS_CONTEXT_ID)).toInstance(JanusContextID);

		bind(UUID.class).annotatedWith(Names.named(JanusConfig.JANUS_DEF_SPACE_ID)).toInstance(defaultJanusSpaceId);

		// Hazelcast config
		Config hazelcastConfig = new Config();
		SerializerConfig sc = new SerializerConfig();
		sc.setTypeClass(SpaceID.class);
		sc.setImplementation(new SpaceIDSerializer());

		SerializerConfig sc2 = new SerializerConfig();
		sc2.setTypeClass(Address.class);
		sc2.setImplementation(new AddressSerializer());

		hazelcastConfig.getSerializationConfig().addSerializerConfig(sc);
		hazelcastConfig.getSerializationConfig().addSerializerConfig(sc2);

		bind(Config.class).toInstance(hazelcastConfig);

		bind(BuiltinCapacitiesProvider.class).to(BuiltinCapacitiesProviderImpl.class).in(Singleton.class);
		bind(SpawnService.class).in(Singleton.class);

		bind(ContextRepository.class).in(Singleton.class);

		Multibinder<Service> uriBinder = Multibinder.newSetBinder(binder(), Service.class);
		uriBinder.addBinding().to(JanusExecutorsService.class);
		uriBinder.addBinding().to(KernelRepositoryService.class);

		bind(EventSpaceSpecification.class).to(EventSpaceSpecificationImpl.class).in(Singleton.class);
		bind(OpenEventSpaceSpecification.class).to(EventSpaceSpecificationImpl.class).in(Singleton.class);

		// Janus Guava bindings
		// Bus exception
		bind(SubscriberExceptionHandler.class).to(AgentEventBusSubscriberExceptionHandler.class);
		install(new AnnotationModule(Percept.class));

	}

	@Provides
	@Kernel
	@Singleton
	private AgentContext getKernel(ContextFactory factory, @Named(JanusConfig.JANUS_CONTEXT_ID) UUID janusContextID,
			@Named(JanusConfig.JANUS_DEF_SPACE_ID) UUID defaultJanusSpaceId) {

		if (this.janusContext == null) {
			this.janusContext = factory.create(janusContextID, defaultJanusSpaceId);
		}
		return this.janusContext;
	}

	@Provides
	private static AsyncEventBus createAgentInternalBus(Injector injector, ExecutorService service,
			SubscriberExceptionHandler exceptionHandler) {
		AsyncEventBus aeb = new AsyncEventBus(service, exceptionHandler);
		// to be able to inject the SubscriberFindingStrategy
		injector.injectMembers(aeb);
		return aeb;
	}

	@Provides
	private ExecutorService createThreadPoolService() {
		if (this.executorService == null) {
			this.executorService = Executors.newCachedThreadPool();
		}
		return this.executorService;
	}

	@Provides
	@Kernel
	private ExecutorService createKernelDeamonThreadPoolService() {
		if (this.kernelExecutorService == null) {
			this.kernelExecutorService = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).build());
		}
		return this.kernelExecutorService;
	}

	@Provides
	private ScheduledExecutorService createScheduledExecutorService() {
		if (this.scheduledExecutorService == null) {
			// TODO find a better way to control agent threads
			this.scheduledExecutorService = new AgentScheduledExecutorService(10);
		}
		return this.scheduledExecutorService;
	}

	@Provides
	@Singleton
	private static ServiceManager createServiceManager(Set<Service> services) {
		return new ServiceManager(services);
	}

	@Provides
	@Singleton
	private static HazelcastInstance createHazelcastInstance(Config config) {
		return Hazelcast.newHazelcastInstance(config);
	}

}
