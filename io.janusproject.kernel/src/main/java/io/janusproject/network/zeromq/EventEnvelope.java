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

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

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

/**
 * nvelope
 * 
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */

public class EventEnvelope {
	private byte[] contextId;
	private byte[] spaceId;
	private byte[] scope;
	private byte[] customHeaders;
	private byte[] body;

	EventEnvelope(byte[] contextId, byte[] spaceID, byte[] scope,
			byte[] headers, byte[] body) {
		this.contextId = contextId;
		this.spaceId = spaceID;
		this.scope = scope;
		this.customHeaders = headers;
		this.body = body;
	}

	public byte[] getCustomHeaders() {
		return this.customHeaders;
	}

	public byte[] getBody() {
		return this.body;
	}

	public byte[] getContextId() {
		return this.contextId;
	}

	/**
	 * @return the spaceId
	 */
	public byte[] getSpaceId() {
		return this.spaceId;
	}

	/**
	 * @return the scope
	 */
	public byte[] getScope() {
		return this.scope;
	}

	public void send(Socket publisher) {
		publisher.send(this.contextId, ZMQ.SNDMORE);
		publisher.send(this.spaceId, ZMQ.SNDMORE);
		publisher.send(this.scope, ZMQ.SNDMORE);
		publisher.send(this.customHeaders, ZMQ.SNDMORE);
		publisher.send(this.body, 0);
	}

	public static EventEnvelope recv(Socket updates) {
		byte[] data = updates.recv(ZMQ.DONTWAIT);
		if (data == null || !updates.hasReceiveMore())
			return null;
		byte[] contextId = data.clone();

		data = updates.recv(ZMQ.DONTWAIT);
		if (data == null || !updates.hasReceiveMore())
			return null;
		byte[] spaceId = data.clone();

		data = updates.recv(ZMQ.DONTWAIT);
		if (data == null || !updates.hasReceiveMore())
			return null;
		byte[] scope = data.clone();

		data = updates.recv(ZMQ.DONTWAIT);
		if (data == null || !updates.hasReceiveMore())
			return null;
		byte[] headers = data.clone();

		byte[] body = updates.recv(ZMQ.DONTWAIT);
		if (body == null || updates.hasReceiveMore())
			return null;
		EventEnvelope env = new EventEnvelope(contextId, spaceId, scope,
				headers, body);
		return env;
	}

	public static EventEnvelope build(byte[] contextId, byte[] spaceId,
			byte[] scope, byte[] headers, byte[] body) {
		return new EventEnvelope(contextId, spaceId, scope, headers, body);
	}

}
