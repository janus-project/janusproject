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
package io.janusproject.network.zeromq;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.arakhne.afc.vmutil.locale.Locale;

import com.google.common.util.concurrent.Service;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.Multibinder;

import io.janusproject.kernel.Network;
import io.janusproject.util.AbstractSystemPropertyProvider;

/**
 * Module that provides the network layer based on the ZeroMQ library.
 * 
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class ZeroMQNetworkModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Network.class).to(ZeroMQNetwork.class).in(Singleton.class);
		Multibinder<Service> uriBinder = Multibinder.newSetBinder(binder(), Service.class);
		uriBinder.addBinding().to(ZeroMQNetwork.class);
	}

	@Provides
	private static Gson createGson() {
		return new GsonBuilder().registerTypeAdapter(Class.class, new ClassTypeAdapter()).setPrettyPrinting().create();
	}

	@Provides
	private static EventSerializer createEventSerializer(Injector injector) {
		Class<? extends EventSerializer> serializerType = GsonEventSerializer.class;
		String serializerClassname = AbstractSystemPropertyProvider.getSystemProperty(ZeroMQConfig.SERIALIZER_CLASSNAME);
		try {
			if (serializerClassname != null && !serializerClassname.isEmpty()) {
				Class<?> type = Class.forName(serializerClassname);
				if (type != null && EventSerializer.class.isAssignableFrom(type)) {
					serializerType = type.asSubclass(EventSerializer.class);
				}
				assert(injector!=null);
				return injector.getInstance(serializerType);
			}
			return injector.getInstance(serializerType);
		} catch (Throwable e) {
			Logger.getAnonymousLogger().log(Level.SEVERE, Locale.getString(ZeroMQNetworkModule.class, "CANNOT_CREATE_SERIALIZER", serializerClassname, ZeroMQConfig.SERIALIZER_CLASSNAME), e); //$NON-NLS-1$
			return null;
		}
	}

	@Provides
	private static EventEncrypter getEncrypter(Injector injector) {
		Class<? extends EventEncrypter> encrypterType = null;
		String encrypterClassname = AbstractSystemPropertyProvider.getSystemProperty(ZeroMQConfig.ENCRYPTER_CLASSNAME);
		try {
			if (encrypterClassname != null && !encrypterClassname.isEmpty()) {
				try {
					Class<?> type = Class.forName(encrypterClassname);
					if (type != null && EventEncrypter.class.isAssignableFrom(type)) {
						encrypterType = type.asSubclass(EventEncrypter.class);
					}
				} catch (Throwable e) {
					Logger.getAnonymousLogger().log(Level.SEVERE, Locale.getString("CANNOT_CREATE_ENCRYPTER", encrypterClassname, ZeroMQConfig.ENCRYPTER_CLASSNAME), e); //$NON-NLS-1$
				}
			}
			if (encrypterType == null) {
				String aesKey = AbstractSystemPropertyProvider.getSystemProperty(ZeroMQConfig.AES_KEY);
				if (aesKey != null && !aesKey.isEmpty()) {
					encrypterType = AESEventEncrypter.class;
				} else {
					encrypterType = PlainTextEncrypter.class;
				}
			}
			assert(injector!=null);
			return injector.getInstance(encrypterType);
		} catch (Throwable e) {
			Logger.getAnonymousLogger().log(Level.SEVERE, Locale.getString("CANNOT_CREATE_ENCRYPTER", encrypterClassname, ZeroMQConfig.ENCRYPTER_CLASSNAME), e); //$NON-NLS-1$
			return null;
		}
	}

}
