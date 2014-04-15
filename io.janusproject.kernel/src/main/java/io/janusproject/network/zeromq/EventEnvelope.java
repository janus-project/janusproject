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

/**
 * Envelope of a message that is exchanged by ZeroMQ peers.
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @see EventPack
 */
public class EventEnvelope {
	private byte[] contextId;
	private byte[] spaceId;
	private byte[] scope;
	private byte[] customHeaders;
	private byte[] body;

	/** Construct an envelope.
	 * 
	 * @param contextId - identifier of the context in which the event occurs.
	 * @param spaceID - identifier of the space in which the event occurs.
	 * @param scope - scope for the event.
	 * @param headers - custom header associated to the event.
	 * @param body - body of the event.
	 */
	EventEnvelope(byte[] contextId, byte[] spaceID, byte[] scope,
			byte[] headers, byte[] body) {
		assert(contextId!=null && contextId.length>0) : "Parameter 'contextId' must not be null or zero-length"; //$NON-NLS-1$
		assert(spaceID!=null && spaceID.length>0) : "Parameter 'spaceID' must not be null or zero-length"; //$NON-NLS-1$
		assert(scope!=null && scope.length>0) : "Parameter 'contextId' must not be null or zero-length"; //$NON-NLS-1$
		assert(headers!=null && headers.length>0) : "Parameter 'contextId' must not be null or zero-length"; //$NON-NLS-1$
		assert(body!=null && body.length>0) : "Parameter 'contextId' must not be null or zero-length"; //$NON-NLS-1$
		this.contextId = contextId;
		this.spaceId = spaceID;
		this.scope = scope;
		this.customHeaders = headers;
		this.body = body;
	}

	/** Replies the custom header.
	 * 
	 * @return the custom header.
	 */
	public byte[] getCustomHeaders() {
		return this.customHeaders;
	}

	/** Replies the body of the event.
	 * 
	 * @return the body.
	 */
	public byte[] getBody() {
		return this.body;
	}

	/** Replies the identifier of the context in
	 * which the event occurs.
	 * 
	 * @return the content identifier.
	 */
	public byte[] getContextId() {
		return this.contextId;
	}

	/** Replies the identifier of the space in
	 * which the event occurs.
	 * 
	 * @return the space identifier.
	 */
	public byte[] getSpaceId() {
		return this.spaceId;
	}

	/** Replies the scope of the event.
	 * 
	 * @return the scope.
	 */
	public byte[] getScope() {
		return this.scope;
	}

	/** Send this envelope over the network.
	 * 
	 * @param publisher - network publisher.
	 */
	public void send(Socket publisher) {
		publisher.send(this.contextId, ZMQ.SNDMORE);
		publisher.send(this.spaceId, ZMQ.SNDMORE);
		publisher.send(this.scope, ZMQ.SNDMORE);
		publisher.send(this.customHeaders, ZMQ.SNDMORE);
		publisher.send(this.body, 0);
	}

	/** Receive data from the network.
	 * 
	 * @param updates - network reader.
	 * @return the envelope received over the network.
	 */
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

	/** Create an envelope for an event.
	 * 
	 * @param contextId - identifier of the context in which the event occurs.
	 * @param spaceId - identifier of the space in which the event occurs.
	 * @param scope - scope for the event.
	 * @param headers - custom header associated to the event.
	 * @param body - body of the event.
	 * @return the new envelope.
	 */
	public static EventEnvelope build(byte[] contextId, byte[] spaceId,
			byte[] scope, byte[] headers, byte[] body) {
		return new EventEnvelope(contextId, spaceId, scope, headers, body);
	}

}
