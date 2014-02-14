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

/**
 * A utility implementation of the {@link EventEncrypter} that creates the
 * {@link EventEnvelope} fields using {@link String#getBytes()}.
 * <p>
 * The main use of the class should be development to be able to easy see what's
 * being transfered on the wire.
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class PlainTextEncrypter implements EventEncrypter {

	@Override
	public EventEnvelope encrypt(EventPack pack) {
		return EventEnvelope.build(pack.getContextId().getBytes(), pack
				.getSpaceId().getBytes(), pack.getScope().getBytes(), pack
				.getHeaders().getBytes(), pack.getEvent().getBytes());
	}

	@Override
	public EventPack decrypt(EventEnvelope envelope) {
		EventPack pack = new EventPack();
		pack.setContextId(new String(envelope.getContextId()));
		pack.setSpaceId(new String(envelope.getSpaceId()));
		pack.setScope(new String(envelope.getScope()));
		pack.setEvent(new String(envelope.getBody()));
		pack.setHeaders(new String(envelope.getCustomHeaders()));
		return pack;
	}

	@Override
	public byte[] encrytContextID(UUID id) throws Exception {
		return id.toString().getBytes();
	}

}
