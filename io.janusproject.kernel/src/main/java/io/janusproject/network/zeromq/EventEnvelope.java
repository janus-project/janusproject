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

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

import com.google.common.primitives.Ints;

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

	/** Build the byte array that may be used for the ZeroMQ filtering
	 * associated with {@link Socket#subscribe(byte[])}.
	 * For a given contextID (translated into a byte array with an
	 * {@link EventSerializer}), this function must always reply the
	 * same sequence of bytes.
	 * 
	 * @param contextID
	 * @return the header of the ZeroMQ message that may be used for
	 * filtering.
	 */
	public static byte[] buildFilterableHeader(byte[] contextID) {
		byte[] header = new byte[Ints.BYTES+contextID.length];
		byte[] length = Ints.toByteArray(contextID.length);
		System.arraycopy(length, 0, header, 0, length.length);
		System.arraycopy(contextID, 0, header, length.length, contextID.length);
		return header;
	}
	
	/**
	 * Send this envelope over the network.
	 * 
	 * @param publisher - network publisher.
	 */
	public void send(Socket publisher) {
		publisher.sendMore(buildFilterableHeader(this.contextId));
		publisher.sendMore(Ints.toByteArray(this.spaceId.length));
		publisher.sendMore(this.spaceId);
		publisher.sendMore(Ints.toByteArray(this.scope.length));
		publisher.sendMore(this.scope);
		publisher.sendMore(Ints.toByteArray(this.customHeaders.length));
		publisher.sendMore(this.customHeaders);
		publisher.sendMore(Ints.toByteArray(this.body.length));
		publisher.send(this.body);
	}

	private static byte[] readBuffer(ByteBuffer buffer, int size) throws IOException {
		if (buffer.remaining()>=size) {
			byte[] result = new byte[size];
			buffer.get(result);
			return result;
		}
		throw new EOFException();
	}
	
	private static byte[] readBlock(ByteBuffer buffer) throws IOException {
		int length = Ints.fromByteArray(readBuffer(buffer, Ints.BYTES));
		return readBuffer(buffer, length);
	}

	/**
	 * Receive data from the network.
	 * 
	 * @param socket - network reader.
	 * @return the envelope received over the network.
	 * @throws IOException if the envelope cannot be read from the network.
	 */
	public static EventEnvelope recv(Socket socket) throws IOException {
		//TODO: Read the ZeroMQ socket via a NIO wrapper to support large data: indeed the arrays has a maximal size bounded by a native int value, and the real data could be larger than this limit.

		byte[] data;
		{ 
			data = socket.recv(ZMQ.DONTWAIT);
			byte[] cdata;
			int oldSize = 0;
			while (socket.hasReceiveMore()) {
				cdata = socket.recv(ZMQ.DONTWAIT);
				oldSize = data.length;
				data = Arrays.copyOf(data, data.length + cdata.length);
				System.arraycopy(cdata, 0, data, oldSize, cdata.length);
			}
		}
		
		ByteBuffer buffer = ByteBuffer.wrap(data);
		
		byte[] contextId = readBlock(buffer);
		assert(contextId!=null && contextId.length>0);
		
		byte[] spaceId = readBlock(buffer);
		assert(spaceId!=null && spaceId.length>0);
		
		byte[] scope = readBlock(buffer);
		assert(scope!=null && scope.length>0);

		byte[] headers = readBlock(buffer);
		assert(headers!=null && headers.length>0);

		byte[] body = readBlock(buffer);
		assert(body!=null && body.length>0);

		return new EventEnvelope(contextId, spaceId, scope, headers, body);
	}

}
