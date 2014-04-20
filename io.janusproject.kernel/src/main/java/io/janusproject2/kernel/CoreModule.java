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
package io.janusproject2.kernel;

import java.net.InetAddress;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import io.janusproject2.JanusConfig;
import io.janusproject2.JanusDefaultModule;
import io.janusproject2.kernel.executor.AgentScheduledExecutorService;
import io.janusproject2.kernel.hazelcast.AddressSerializer;
import io.janusproject2.kernel.hazelcast.SpaceIDSerializer;
import io.janusproject2.network.NetworkUtil;
import io.janusproject2.services.ArakhneLocaleLogService;
import io.janusproject2.services.SpaceService;
import io.janusproject2.services.SpawnService;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.BuiltinCapacitiesProvider;
import io.sarl.lang.core.EventSpaceSpecification;
import io.sarl.lang.core.Percept;
import io.sarl.lang.core.SpaceID;
import io.sarl.util.OpenEventSpaceSpecification;

import com.google.common.eventbus.AnnotationModule;
import com.google.common.eventbus.AsyncSyncEventBus;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provider;
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
 * The Core Janus Module configures the minimum requirements for Janus to run properly. If you need a standard configuration use {@link JanusDefaultModule}
 * 
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class CoreModule extends AbstractModule {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configure() {

		// Provider for the public URI
		// FIXME test if this property has been already binded using Guice, but we don't know the way to do that
		//Even with a method provider we have a problem since this variable may have already been binded by the JanusDefaultConfigModule in case of this value has been specified as a command line parameter.
		if (JanusConfig.getSystemProperty(JanusConfig.PUB_URI) == null) {
			bind(Key.get(String.class, Names.named(JanusConfig.PUB_URI))).toProvider(PublicURIProvider.class);
		}

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

		bind(BuiltinCapacitiesProvider.class).to(JanusBuiltinCapacitiesProvider.class).in(Singleton.class);
		bind(EventSpaceSpecification.class).to(EventSpaceSpecificationImpl.class).in(Singleton.class);
		bind(OpenEventSpaceSpecification.class).to(EventSpaceSpecificationImpl.class).in(Singleton.class);

		Multibinder<Service> uriBinder = Multibinder.newSetBinder(binder(), Service.class);
		uriBinder.addBinding().to(ArakhneLocaleLogService.class).in(Singleton.class);
		uriBinder.addBinding().to(JanusExecutorService.class).in(Singleton.class);
		uriBinder.addBinding().to(ContextRepository.class).in(Singleton.class);
		uriBinder.addBinding().to(SpawnService.class).in(Singleton.class);
		uriBinder.addBinding().to(SpaceService.class).in(Singleton.class);
		uriBinder.addBinding().to(JanusKernelDiscoveryService.class);

		// Janus Guava bindings
		// Bus exception
		bind(SubscriberExceptionHandler.class).to(AgentEventBusSubscriberExceptionHandler.class);
		install(new AnnotationModule(Percept.class));

	}

	@Provides
	@io.janusproject2.kernel.annotations.Kernel
	@Singleton
	private static AgentContext getKernel(ContextFactory factory, @Named(JanusConfig.DEFAULT_CONTEXT_ID) UUID janusContextID, @Named(JanusConfig.DEFAULT_SPACE_ID) UUID defaultJanusSpaceId) {
		return factory.create(janusContextID, defaultJanusSpaceId);
	}

	@Provides
	private static AsyncSyncEventBus createAgentInternalBus(Injector injector, ExecutorService service, SubscriberExceptionHandler exceptionHandler) {
		AsyncSyncEventBus aeb = new AsyncSyncEventBus(service, exceptionHandler);
		// to be able to inject the SubscriberFindingStrategy
		injector.injectMembers(aeb);
		return aeb;
	}

	@Provides
	private static ExecutorService createThreadPoolService() {
		return Executors.newCachedThreadPool();
	}

	@Provides
	private static ScheduledExecutorService createScheduledExecutorService() {
		// TODO find a better way to control agent threads
		return new AgentScheduledExecutorService(JanusConfig.VALUE_NUMBER_OF_THREADS_IN_EXECUTOR);
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

	/**
	 * @return the contextID
	 */
	@Provides
	@Named(JanusConfig.DEFAULT_CONTEXT_ID)
	public static UUID getContextID() {
		String defaultContextID = JanusConfig.getSystemProperty(JanusConfig.DEFAULT_CONTEXT_ID);
		if (defaultContextID == null || "".equals(defaultContextID)) { //$NON-NLS-1$
			Boolean v;

			// From boot agent type
			defaultContextID = JanusConfig.getSystemProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID);
			if (defaultContextID == null) {
				v = Boolean.valueOf(JanusConfig.VALUE_BOOT_DEFAULT_CONTEXT_ID);
			} else {
				v = Boolean.valueOf(Boolean.parseBoolean(defaultContextID));
			}
			if (v.booleanValue()) {
				String bootClassname = JanusConfig.getSystemProperty(JanusConfig.BOOT_AGENT);
				assert (bootClassname != null);
				Class<?> bootClass;
				try {
					bootClass = Class.forName(bootClassname);
				} catch (ClassNotFoundException e) {
					throw new Error(e);
				}
				defaultContextID = UUID.nameUUIDFromBytes(bootClass.getCanonicalName().getBytes()).toString();
			} else {
				// Random
				defaultContextID = JanusConfig.getSystemProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID);
				if (defaultContextID == null) {
					v = Boolean.valueOf(JanusConfig.VALUE_RANDOM_DEFAULT_CONTEXT_ID);
				} else {
					v = Boolean.valueOf(Boolean.parseBoolean(defaultContextID));
				}
				if (v.booleanValue()) {
					defaultContextID = UUID.randomUUID().toString();
				} else {
					defaultContextID = JanusConfig.VALUE_DEFAULT_CONTEXT_ID;
				}
			}

			// Force the global value of the property to prevent to re-generate the UUID at the next call.
			System.setProperty(JanusConfig.DEFAULT_CONTEXT_ID, defaultContextID);
		}

		assert (defaultContextID != null && !defaultContextID.isEmpty());
		return UUID.fromString(defaultContextID);
	}

	/**
	 * @return the spaceID
	 */
	@Provides
	@Named(JanusConfig.DEFAULT_SPACE_ID)
	public static UUID getSpaceID() {
		String v = JanusConfig.getSystemProperty(JanusConfig.DEFAULT_SPACE_ID, JanusConfig.VALUE_DEFAULT_SPACE_ID);
		return UUID.fromString(v);
	}
	
	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class PublicURIProvider implements Provider<String> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String get() {
			String pubUri = JanusConfig.getSystemProperty(JanusConfig.PUB_URI);
			if (pubUri == null || pubUri.isEmpty()) {
				InetAddress a = NetworkUtil.getPrimaryAddress(true);
				if (a != null) {
					pubUri = "tcp://" + a.getHostAddress() + ":*"; //$NON-NLS-1$//$NON-NLS-2$
					System.setProperty(JanusConfig.PUB_URI, pubUri);
				}
			}
			return pubUri;
		}

	}

}
