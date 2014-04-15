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

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

/**
 * Envelope of a message that is exchanged by ZeroMQ peers.
 * 
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @see EventPack
 */
public class EventEnvelope {
	private final byte[] contextId;
	private final byte[] spaceId;
	private final byte[] scope;
	private final byte[] customHeaders;
	private final byte[] body;

	/**
	 * Construct an envelope.
	 * 
	 * @param contextId - identifier of the context in which the event occurs.
	 * @param spaceID - identifier of the space in which the event occurs.
	 * @param scope - scope for the event.
	 * @param headers - custom header associated to the event.
	 * @param body - body of the event.
	 */
	EventEnvelope(byte[] contextId, byte[] spaceID, byte[] scope, byte[] headers, byte[] body) {

		assert (contextId != null && contextId.length > 0) : "Parameter 'contextId' must not be null or zero-length"; //$NON-NLS-1$
		assert (spaceID != null && spaceID.length > 0) : "Parameter 'spaceID' must not be null or zero-length"; //$NON-NLS-1$
		assert (scope != null && scope.length > 0) : "Parameter 'contextId' must not be null or zero-length"; //$NON-NLS-1$
		assert (headers != null && headers.length > 0) : "Parameter 'contextId' must not be null or zero-length"; //$NON-NLS-1$
		assert (body != null && body.length > 0) : "Parameter 'contextId' must not be null or zero-length"; //$NON-NLS-1$

		this.contextId = contextId;
		this.spaceId = spaceID;
		this.scope = scope;
		this.customHeaders = headers;
		this.body = body;
	}

	/**
	 * Replies the custom header.
	 * 
	 * @return the custom header.
	 */
	public byte[] getCustomHeaders() {
		return this.customHeaders;
	}

	/**
	 * Replies the body of the event.
	 * 
	 * @return the body.
	 */
	public byte[] getBody() {
		return this.body;
	}

	/**
	 * Replies the identifier of the context in which the event occurs.
	 * 
	 * @return the content identifier.
	 */
	public byte[] getContextId() {
		return this.contextId;
	}

	/**
	 * Replies the identifier of the space in which the event occurs.
	 * 
	 * @return the space identifier.
	 */
	public byte[] getSpaceId() {
		return this.spaceId;
	}

	/**
	 * Replies the scope of the event.
	 * 
	 * @return the scope.
	 */
	public byte[] getScope() {
		return this.scope;
	}

	private static final int SIZE_OF_INTEGER = 4;

	private static byte[] intToBytes(final int value) {
		return new byte[] { (byte) (value >> 24), (byte) (value >> 16), (byte) (value >> 8), (byte) value };
	}

	private static int bytesToInt(byte[] bytes) {
		return bytes[0] << 24 | (bytes[1] & 0xFF) << 16 | (bytes[2] & 0xFF) << 8 | (bytes[3] & 0xFF);
	}

	/**
	 * Send this envelope over the network.
	 * 
	 * @param publisher - network publisher.
	 */
	public void send(Socket publisher) {
		int contextSize = this.contextId.length;
		int spaceIdSize = this.spaceId.length;
		int scopeSize = this.scope.length;
		int customHeadersSize = this.customHeaders.length;
		int bodySize = this.body.length;

		/*
		 * byte[] data = new byte[5*SIZE_OF_INTEGER+contextSize+spaceIdSize+scopeSize+customHeadersSize+bodySize]; ByteBuffer.wrap(data, 0, SIZE_OF_INTEGER).put(intToBytes(contextSize), 0, SIZE_OF_INTEGER); ByteBuffer.wrap(data, SIZE_OF_INTEGER,contextSize).put(this.contextId, 0, contextSize);
		 * 
		 * ByteBuffer.wrap(data, SIZE_OF_INTEGER+contextSize, SIZE_OF_INTEGER).put(intToBytes(spaceIdSize), 0, SIZE_OF_INTEGER); ByteBuffer.wrap(data, 2*SIZE_OF_INTEGER+contextSize,spaceIdSize).put(this.spaceId, 0, spaceIdSize);
		 * 
		 * 
		 * ByteBuffer.wrap(data, 2*SIZE_OF_INTEGER+contextSize+spaceIdSize, SIZE_OF_INTEGER).put(intToBytes(scopeSize), 0, SIZE_OF_INTEGER); ByteBuffer.wrap(data, 3*SIZE_OF_INTEGER+contextSize+spaceIdSize,scopeSize).put(this.scope, 0, scopeSize);
		 * 
		 * ByteBuffer.wrap(data, 3*SIZE_OF_INTEGER+contextSize+spaceIdSize+scopeSize, SIZE_OF_INTEGER).put(intToBytes(customHeadersSize), 0, SIZE_OF_INTEGER); ByteBuffer.wrap(data, 4*SIZE_OF_INTEGER+contextSize+spaceIdSize+scopeSize,customHeadersSize).put(this.customHeaders, 0, customHeadersSize);
		 * 
		 * ByteBuffer.wrap(data, 4*SIZE_OF_INTEGER+contextSize+spaceIdSize+scopeSize+customHeadersSize, SIZE_OF_INTEGER).put(intToBytes(bodySize), 0, SIZE_OF_INTEGER); ByteBuffer.wrap(data, 5*SIZE_OF_INTEGER+contextSize+spaceIdSize+scopeSize+customHeadersSize,bodySize).put(this.body, 0, bodySize);
		 * 
		 * 
		 * int maxMsgSize = (new Long(publisher.getMaxMsgSize())).intValue(); if (maxMsgSize <= 0) { maxMsgSize = 1024; } if (data.length > maxMsgSize) { byte[] msgData; int offset = 0; while ((offset < data.length) && ((offset+maxMsgSize)< data.length)) { msgData= new byte[maxMsgSize]; ByteBuffer.wrap(data, offset, maxMsgSize).get(msgData); publisher.sendMore(msgData); offset += maxMsgSize; } if ((offset+maxMsgSize)>= data.length) { msgData= new byte[data.length-offset]; ByteBuffer.wrap(data, offset, data.length-offset).get(msgData); publisher.send(msgData); } } else { publisher.send(data); }
		 */

		publisher.sendMore(intToBytes(contextSize));
		publisher.sendMore(this.contextId);
		publisher.sendMore(intToBytes(spaceIdSize));
		publisher.sendMore(this.spaceId);
		publisher.sendMore(intToBytes(scopeSize));
		publisher.sendMore(this.scope);
		publisher.sendMore(intToBytes(customHeadersSize));
		publisher.sendMore(this.customHeaders);
		publisher.sendMore(intToBytes(bodySize));
		publisher.send(this.body);
	}

	/**
	 * Receive data from the network.
	 * 
	 * @param updates - network reader.
	 * @return the envelope received over the network.
	 */
	public static EventEnvelope recv(Socket updates) {

		byte[] data = updates.recv(ZMQ.DONTWAIT);
		byte[] cdata;
		int oldSize = 0;
		while (updates.hasReceiveMore()) {
			cdata = updates.recv(ZMQ.DONTWAIT);
			oldSize = data.length;
			data = Arrays.copyOf(data, data.length + cdata.length);
			System.arraycopy(cdata, 0, data, oldSize, cdata.length);
		}

		int currentIndex = 0;
		int contextSize = bytesToInt(ByteBuffer.wrap(data, 0, SIZE_OF_INTEGER).array());
		currentIndex += SIZE_OF_INTEGER;
		byte[] contextId = new byte[contextSize];
		ByteBuffer.wrap(data, currentIndex, contextSize).get(contextId);
		currentIndex += contextSize;

		int spaceIdSize = bytesToInt(ByteBuffer.wrap(data, currentIndex, SIZE_OF_INTEGER).array());
		currentIndex += SIZE_OF_INTEGER;
		byte[] spaceId = new byte[spaceIdSize];
		ByteBuffer.wrap(data, currentIndex, spaceIdSize).get(spaceId);
		currentIndex += spaceIdSize;

		int scopeSize = bytesToInt(ByteBuffer.wrap(data, currentIndex, SIZE_OF_INTEGER).array());
		currentIndex += SIZE_OF_INTEGER;
		byte[] scope = new byte[scopeSize];
		ByteBuffer.wrap(data, currentIndex, scopeSize).get(scope);
		currentIndex += scopeSize;

		int customHeadersSize = bytesToInt(ByteBuffer.wrap(data, currentIndex, SIZE_OF_INTEGER).array());
		currentIndex += SIZE_OF_INTEGER;
		byte[] headers = new byte[customHeadersSize];
		ByteBuffer.wrap(data, currentIndex, customHeadersSize).get(headers);
		currentIndex += customHeadersSize;

		int bodySize = bytesToInt(ByteBuffer.wrap(data, currentIndex, SIZE_OF_INTEGER).array());
		currentIndex += SIZE_OF_INTEGER;
		byte[] body = new byte[bodySize];
		ByteBuffer.wrap(data, currentIndex, bodySize).get(body);
		currentIndex += bodySize;

		EventEnvelope env = new EventEnvelope(contextId, spaceId, scope, headers, body);
		return env;

		/*
		 * byte[] data = updates.recv(ZMQ.DONTWAIT); assert (!(data == null || !updates.hasReceiveMore())); byte[] contextId = data.clone();
		 * 
		 * data = updates.recv(ZMQ.DONTWAIT); assert (!(data == null || !updates.hasReceiveMore())); byte[] spaceId = data.clone();
		 * 
		 * data = updates.recv(ZMQ.DONTWAIT); assert (!(data == null || !updates.hasReceiveMore())); byte[] scope = data.clone();
		 * 
		 * data = updates.recv(ZMQ.DONTWAIT); assert (!(data == null || !updates.hasReceiveMore())); byte[] headers = data.clone();
		 * 
		 * byte[] body = updates.recv(ZMQ.DONTWAIT); byte[] cbody; int oldSize = 0; while (updates.hasReceiveMore()) { cbody = updates.recv(ZMQ.DONTWAIT); oldSize = body.length; body = Arrays.copyOf(body, body.length + cbody.length); System.arraycopy(cbody, 0, body, oldSize, cbody.length); }
		 * 
		 * assert (!(body == null)); EventEnvelope env = new EventEnvelope(contextId, spaceId, scope, headers, body); return env;
		 */
	}

	/**
	 * Create an envelope for an event.
	 * 
	 * @param contextId - identifier of the context in which the event occurs.
	 * @param spaceId - identifier of the space in which the event occurs.
	 * @param scope - scope for the event.
	 * @param headers - custom header associated to the event.
	 * @param body - body of the event.
	 * @return the new envelope.
	 */
	public static EventEnvelope build(byte[] contextId, byte[] spaceId, byte[] scope, byte[] headers, byte[] body) {
		return new EventEnvelope(contextId, spaceId, scope, headers, body);
	}

}
