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
import io.janusproject.kernel.annotations.Kernel;
import io.janusproject.kernel.executor.AgentScheduledExecutorService;
import io.janusproject.network.NetworkUtil;
import io.janusproject.repository.AddressSerializer;
import io.janusproject.repository.SpaceIDSerializer;
import io.janusproject.util.AbstractSystemPropertyProvider;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.BuiltinCapacitiesProvider;
import io.sarl.lang.core.EventSpaceSpecification;
import io.sarl.lang.core.Percept;
import io.sarl.lang.core.SpaceID;
import io.sarl.util.OpenEventSpaceSpecification;

import java.net.InetAddress;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.google.common.eventbus.AnnotationModule;
import com.google.common.eventbus.AsyncSyncEventBus;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Key;
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
 * The Core Janus Module configures the minimum requirements for Janus to run properly. If you need a standard configuration use {@link JanusDefaultConfigModule}
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

		// Provider for the public URI
		// FIXME test if this property has been already binded using Guice, but we don't know the way to do that
		//Even with a method provider we have a problem since this variable may have already been binded by the JanusDefaultConfigModule in case of this value has been specified as a command line parameter.
		if (AbstractSystemPropertyProvider.getSystemProperty(JanusConfig.PUB_URI) == null) {
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

		bind(BuiltinCapacitiesProvider.class).to(BuiltinCapacitiesProviderImpl.class).in(Singleton.class);
		bind(SpawnService.class).in(Singleton.class);

		bind(ContextRepository.class).in(Singleton.class);

		Multibinder<Service> uriBinder = Multibinder.newSetBinder(binder(), Service.class);
		uriBinder.addBinding().to(JanusExecutorsService.class);
		uriBinder.addBinding().to(KernelDiscoveryService.class);

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
	private AgentContext getKernel(ContextFactory factory, @Named(JanusConfig.DEFAULT_CONTEXT_ID) UUID janusContextID, @Named(JanusConfig.DEFAULT_SPACE_ID) UUID defaultJanusSpaceId) {

		if (this.janusContext == null) {
			this.janusContext = factory.create(janusContextID, defaultJanusSpaceId);
		}
		return this.janusContext;
	}

	@Provides
	private static AsyncSyncEventBus createAgentInternalBus(Injector injector, ExecutorService service, SubscriberExceptionHandler exceptionHandler) {
		AsyncSyncEventBus aeb = new AsyncSyncEventBus(service, exceptionHandler);
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

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class PublicURIProvider extends AbstractSystemPropertyProvider<String> {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String get() {
			String pubUri = getSystemProperty(JanusConfig.PUB_URI);
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

	@Provides
	@Named(JanusConfig.DEFAULT_CONTEXT_ID)
	public static UUID getContextID() {
		String defaultContextID = AbstractSystemPropertyProvider.getSystemProperty(JanusConfig.DEFAULT_CONTEXT_ID);
		if (defaultContextID == null || "".equals(defaultContextID)) { //$NON-NLS-1$
			Boolean v;

			// From boot agent type
			defaultContextID = AbstractSystemPropertyProvider.getSystemProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID);
			if (defaultContextID == null) {
				v = Boolean.valueOf(JanusConfig.VALUE_BOOT_DEFAULT_CONTEXT_ID);
			} else {
				v = Boolean.valueOf(Boolean.parseBoolean(defaultContextID));
			}
			if (v.booleanValue()) {
				String bootClassname = AbstractSystemPropertyProvider.getSystemProperty(JanusConfig.BOOT_AGENT);
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
				defaultContextID = AbstractSystemPropertyProvider.getSystemProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID);
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

	@Provides
	@Named(JanusConfig.DEFAULT_SPACE_ID)
	public static UUID getSpaceID() {
		String v = AbstractSystemPropertyProvider.getSystemProperty(JanusConfig.DEFAULT_SPACE_ID);
		if (v == null)
			v = JanusConfig.VALUE_DEFAULT_SPACE_ID;
		return UUID.fromString(v);
	}

}
