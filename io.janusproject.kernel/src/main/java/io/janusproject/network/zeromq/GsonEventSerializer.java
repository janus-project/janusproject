/*
 * $Id$
 * 
 * Janus platform is an open-source multiagent platform.
 * More details on &lt;http://www.janus-project.org&gt;
 * Copyright (C) 2013 Janus Core Developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class GsonEventSerializer implements EventSerializer {

	@Inject
	private Gson gson;
	@Inject
	private EventEncrypter encrypter;
	/**
	 * {@inheritDoc}
	 * @throws Exception 
	 */
	@Override
	public EventEnvelope serialize(EventDispatch dispatch) throws Exception {
		

		Event event = dispatch.getEvent();
		Scope<?> scope = dispatch.getScope();
		SpaceID spaceID = dispatch.getSpaceID();



		dispatch.getHeaders().put("x-java-event-class",
				event.getClass().getName());
		dispatch.getHeaders().put("x-java-scope-class",
				scope.getClass().getName());
		dispatch.getHeaders().put("x-java-spacespec-class",
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

	/**
	 * {@inheritDoc}
	 * 
	 * @throws Exception
	 */
	@Override
	public EventDispatch deserialize(EventEnvelope envelope) throws Exception {
		EventPack pack = this.encrypter.decrypt(envelope);
		
		UUID contextId = UUID.fromString(pack.getContextId());
		UUID spaceId = UUID.fromString(pack.getSpaceId());

		Map<String, String> headers = getHeadersFromString(pack.getHeaders());

		Class<?> spaceSpec = Class.forName(headers
				.get("x-java-spacespec-class"));
		Class<?> eventClazz = Class.forName(headers.get("x-java-event-class"));
		Class<?> scopeClazz = Class.forName(headers.get("x-java-scope-class"));

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
