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
package io.janusproject;

import io.janusproject.network.NetworkUtil;

import java.io.IOError;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;
import java.util.Map.Entry;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

/**
 * The module configures the minimum requirements for 
 * the system variables.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class BootModule extends AbstractModule {

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void configure() {
		boolean foundPubUri = false;
		String name;
		for(Entry<Object,Object> entry : System.getProperties().entrySet()) {
			name = entry.getKey().toString();
			bind(Key.get(String.class, Names.named(name))).toInstance(entry.getValue().toString());
			if (JanusConfig.PUB_URI.equals(name)) {
				foundPubUri = true;
			}
		}

		if (!foundPubUri) {
			bind(Key.get(String.class, Names.named(JanusConfig.PUB_URI))).toProvider(PublicURIProvider.class);
		}
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
	 * @return the spaceID
	 */
	@Provides
	@Named(JanusConfig.PUB_URI)
	public static URI getPubURI() {
		String v = getDefaultPubUri();
		try {
			return new URI(v);
		}
		catch (URISyntaxException e) {
			throw new IOError(e);
		}
	}

	private static String getDefaultPubUri() {
		String pubUri = JanusConfig.getSystemProperty(JanusConfig.PUB_URI);
		if (pubUri == null || pubUri.isEmpty()) {
			InetAddress a = NetworkUtil.getPrimaryAddress(true);
			if (a != null) {
				pubUri = NetworkUtil.toURI(a, -1).toString();
				System.setProperty(JanusConfig.PUB_URI, pubUri);
			}
		}
		return pubUri;
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
		@SuppressWarnings("synthetic-access")
		@Override
		public String get() {
			return getDefaultPubUri();
		}

	}

}
