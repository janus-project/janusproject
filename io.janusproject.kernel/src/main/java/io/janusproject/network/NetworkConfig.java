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
package io.janusproject.network;

import java.nio.charset.Charset;

import com.google.common.base.Charsets;
import com.google.inject.name.Named;

/**
 * Public configuration properties for the network modules.
 * Define the properties as required in your application module as a
 * {@link Named} annotation.
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class NetworkConfig {
		
	/** Name of the property for the AES key.
	 */
	public static final String AES_KEY = "network.encrypter.aes.key"; //$NON-NLS-1$

	/** Name of the property for the classname of the serializer to use.
	 */
	public static final String SERIALIZER_CLASSNAME = "network.serializer.class"; //$NON-NLS-1$

	/** Name of the property for the classname of the encrypter to use.
	 */
	public static final String ENCRYPTER_CLASSNAME = "network.encrypter.class"; //$NON-NLS-1$

	/** Charset that should be used for converting String to byte array or
	 * byte array to String.
	 * <p>
	 * This constant was introduced to enforce the values on different platforms.
	 */
	public static final Charset BYTE_ARRAY_STRING_CHARSET = Charsets.UTF_8;

}
