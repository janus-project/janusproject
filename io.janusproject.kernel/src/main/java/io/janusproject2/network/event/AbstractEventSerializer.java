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
package io.janusproject2.network.event;

import java.util.UUID;

import com.google.inject.Inject;

/**
 * Abstract implementation of an event serializer.
 * 
 * @author $Author: sgalland$
 * @author $Author: ngaud$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public abstract class AbstractEventSerializer implements EventSerializer {

	@Inject
	private EventEncrypter encrypter;

	/**
	 * {@inheritDoc}
	 * 
	 * @throws Exception
	 */
	@Override
	public byte[] serializeContextID(UUID id) throws Exception {
		assert(this.encrypter!=null) : "Error in the injection of the encrypter"; //$NON-NLS-1$
		return this.encrypter.encryptUUID(id);
	}
	
}
