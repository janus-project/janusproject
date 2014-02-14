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

import io.sarl.lang.core.Event;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.core.SpaceSpecification;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.arakhne.afc.vmutil.locale.Locale;

/**  Serialize the {@link EventDispatch} content using the
 * Java serialization mechanism to generate the corresponding
 * {@link EventEnvelope}.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class JavaBinaryEventSerializer implements EventSerializer {

	@Inject
	private EventEncrypter encrypter;
	
	@Override
	public EventEnvelope serialize(EventDispatch dispatch) throws Exception {
		Event event = dispatch.getEvent();
		Scope<?> scope = dispatch.getScope();
		SpaceID spaceID = dispatch.getSpaceID();
		
		dispatch.getHeaders().put("x-java-spacespec-class", //$NON-NLS-1$
				spaceID.getSpaceSpecification().getName());

		EventPack pack = new EventPack();
		pack.setEvent(toBytes(event));
		pack.setScope(toBytes(scope));
		pack.setHeaders(toBytes(dispatch.getHeaders()));
		pack.setSpaceId(spaceID.getID().toString().getBytes());
		pack.setContextId(spaceID.getContextID().toString().getBytes());

		return this.encrypter.encrypt(pack);

	}
	
	private static byte[] toBytes(Object object) throws IOException {
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(object);
			return baos.toByteArray();
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public EventDispatch deserialize(EventEnvelope envelope) throws Exception {
		EventPack pack = this.encrypter.decrypt(envelope);
		
		UUID contextId = UUID.fromString(new String(pack.getContextId()));
		UUID spaceId = UUID.fromString(new String(pack.getSpaceId()));

		Map<String, String> headers = fromBytes(pack.getHeaders(), Map.class);

		Class<?> spaceSpec = Class.forName(headers
				.get("x-java-spacespec-class")); //$NON-NLS-1$

		if (spaceSpec==null || !SpaceSpecification.class.isAssignableFrom(spaceSpec)) {
			throw new ClassCastException(Locale.getString("INVALID_TYPE", spaceSpec)); //$NON-NLS-1$
		}

		SpaceID spaceID = new SpaceID(contextId, spaceId,
				spaceSpec.asSubclass(SpaceSpecification.class));
		
		Event event = fromBytes(pack.getEvent(), Event.class);
		Scope<?> scope = fromBytes(pack.getScope(), Scope.class);
		return new EventDispatch(spaceID,event, scope,headers);

	}

	private static <T> T fromBytes(byte[] data, Class<T> type) throws IOException, ClassNotFoundException {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
			ObjectInputStream oos = new ObjectInputStream(bais);
			Object object = oos.readObject();
			if (object!=null && type.isInstance(object))
				return type.cast(object);
			throw new ClassCastException(Locale.getString("INVALID_TYPE", //$NON-NLS-1$
					type.getName()));
		}
	}

	@Override
	public byte[] serializeContextID(UUID id) throws Exception {
		return this.encrypter.encrytContextID(id);
	}

}
