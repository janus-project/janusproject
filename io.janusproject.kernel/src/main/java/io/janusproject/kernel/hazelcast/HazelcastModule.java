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
package io.janusproject.kernel.hazelcast;

import io.janusproject.JanusConfig;
import io.janusproject.JanusDefaultModule;
import io.janusproject.network.NetworkUtil;
import io.janusproject.repository.DistributedDataStructureFactory;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.SpaceID;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
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
public class HazelcastModule extends AbstractModule {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configure() {
		Config hazelcastConfig = new Config();
		
		SerializerConfig sc = new SerializerConfig();
		sc.setTypeClass(SpaceID.class);
		sc.setImplementation(new SpaceIDSerializer());

		SerializerConfig sc2 = new SerializerConfig();
		sc2.setTypeClass(Address.class);
		sc2.setImplementation(new AddressSerializer());

		hazelcastConfig.getSerializationConfig().addSerializerConfig(sc);
		hazelcastConfig.getSerializationConfig().addSerializerConfig(sc2);

		if (JanusConfig.getSystemPropertyAsBoolean(JanusConfig.OFFLINE, false)
			|| NetworkUtil.getPrimaryAddress(true)==null) {
			hazelcastConfig.setProperty("hazelcast.local.localAddress", "localhost"); //$NON-NLS-1$ //$NON-NLS-2$
			hazelcastConfig.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
		}
		
		bind(Config.class).toInstance(hazelcastConfig);
		
		bind(DistributedDataStructureFactory.class).to(HazelcastDistributedDataStructureFactory.class).in(Singleton.class);
	}

	@Provides
	@Singleton
	private static HazelcastInstance createHazelcastInstance(Config config) {
		return Hazelcast.newHazelcastInstance(config);
	}
	
}
