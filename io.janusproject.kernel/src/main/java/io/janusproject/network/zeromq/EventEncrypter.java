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

import java.util.UUID;

/** An encrypter of events to be published over the network.
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public interface EventEncrypter {

	/** Encrypt the given event.
	 * 
	 * @param pack - the event to encrypt.
	 * @return the envelope with the encrypted event inside.
	 * @throws Exception
	 */
	public EventEnvelope encrypt(EventPack pack) throws Exception;

	/** Decrypt the event in the given envelope.
	 * 
	 * @param envelope - the envelope to decrypt.
	 * @return the decrypted event.
	 * @throws Exception
	 */
	public EventPack decrypt(EventEnvelope envelope) throws Exception;
	
	/** Encrypt the identifier of a content.
	 * 
	 * @param id - the identifier to encrypt.
	 * @return the encrypted identifier.
	 * @throws Exception
	 */
	public byte[] encrytContextID(UUID id) throws Exception;
	
}
