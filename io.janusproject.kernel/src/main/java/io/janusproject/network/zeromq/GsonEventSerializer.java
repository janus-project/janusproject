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

import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;

import org.arakhne.afc.vmutil.locale.Locale;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**  Serialize the {@link EventDispatch} content using GSON to
 * generate the corresponding {@link EventEnvelope}.
 * 
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class GsonEventSerializer implements EventSerializer {

	@Inject
	private Gson gson;
	
	@Inject
	private EventEncrypter encrypter;
	
	@Override
	public EventEnvelope serialize(EventDispatch dispatch) throws Exception {
		Event event = dispatch.getEvent();
		Scope<?> scope = dispatch.getScope();
		SpaceID spaceID = dispatch.getSpaceID();

		dispatch.getHeaders().put("x-java-event-class", //$NON-NLS-1$
				event.getClass().getName());
		dispatch.getHeaders().put("x-java-scope-class", //$NON-NLS-1$
				scope.getClass().getName());
		dispatch.getHeaders().put("x-java-spacespec-class", //$NON-NLS-1$
				spaceID.getSpaceSpecification().getName());
		
		EventPack pack = new EventPack();
		pack.setEvent(this.gson.toJson(event));
		pack.setScope(this.gson.toJson(scope));
		pack.setHeaders(this.gson.toJson(dispatch.getHeaders()));
		pack.setSpaceId(dispatch.getSpaceID().getID().toString());
		pack.setContextId(dispatch.getSpaceID().getContextID().toString());

		return this.encrypter.encrypt(pack);

	}

	private static Map<String, String> getHeadersFromString(String src) {
		Gson gson = new Gson();
		Type headersType = new TypeToken<Map<String, String>>() {
		}.getType();
		return gson.fromJson(src, headersType);

	}

	@Override
	public EventDispatch deserialize(EventEnvelope envelope) throws Exception {
		EventPack pack = this.encrypter.decrypt(envelope);
		
		UUID contextId = UUID.fromString(pack.getContextId());
		UUID spaceId = UUID.fromString(pack.getSpaceId());

		Map<String, String> headers = getHeadersFromString(pack.getHeaders());

		Class<?> spaceSpec = Class.forName(headers
				.get("x-java-spacespec-class")); //$NON-NLS-1$
		Class<?> eventClazz = Class.forName(headers.get("x-java-event-class")); //$NON-NLS-1$
		Class<?> scopeClazz = Class.forName(headers.get("x-java-scope-class")); //$NON-NLS-1$

		SpaceID spaceID = new SpaceID(contextId, spaceId,
				(Class<? extends SpaceSpecification>) spaceSpec);
		
		Event event = (Event) this.gson.fromJson(pack.getEvent(), eventClazz);
		Scope<?> scope = (Scope<?>) this.gson.fromJson(pack.getScope(), scopeClazz);
		return new EventDispatch(spaceID,event, scope,headers);

	}


	/** {@inheritDoc}
	 * @throws Exception 
	 */
	@Override
	public byte[] serializeContextID(UUID id) throws Exception {
		return this.encrypter.encrytContextID(id);
	}

}
