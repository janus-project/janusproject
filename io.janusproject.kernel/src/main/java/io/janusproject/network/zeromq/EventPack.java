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

/** Set of data that should be exchanged on the network
 * by the ZeroMQ peers.
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @see EventEnvelope
 */
public class EventPack {
	
	private byte[] contextId;
	private byte[] spaceId;
	private byte[] headers;
	private byte[] scope;
	private byte[] event;
	
	/**
	 * @return the contextId
	 */
	public byte[] getContextId() {
		return this.contextId;
	}
	
	/**
	 * @param contextId the contextId to set
	 */
	public void setContextId(byte[] contextId) {
		assert(contextId!=null && contextId.length>0) : "Parameter 'contextId' must not be null or zero-length"; //$NON-NLS-1$
		this.contextId = contextId;
	}
	
	/**
	 * @return the spaceId
	 */
	public byte[] getSpaceId() {
		return this.spaceId;
	}
	
	/**
	 * @param spaceId the spaceId to set
	 */
	public void setSpaceId(byte[] spaceId) {
		assert(spaceId!=null && spaceId.length>0) : "Parameter 'spaceId' must not be null or zero-length"; //$NON-NLS-1$
		this.spaceId = spaceId;
	}
	
	/**
	 * @return the headers
	 */
	public byte[] getHeaders() {
		return this.headers;
	}
	
	/**
	 * @param headers the headers to set
	 */
	public void setHeaders(byte[] headers) {
		assert(headers!=null && headers.length>0) : "Parameter 'headers' must not be null or zero-length"; //$NON-NLS-1$
		this.headers = headers;
	}
	
	/**
	 * @return the scope
	 */
	public byte[] getScope() {
		return this.scope;
	}
	
	/**
	 * @param scope the scope to set
	 */
	public void setScope(byte[] scope) {
		assert(scope!=null && scope.length>0) : "Parameter 'scope' must not be null or zero-length"; //$NON-NLS-1$
		this.scope = scope;
	}
	
	/**
	 * @return the event
	 */
	public byte[] getEvent() {
		return this.event;
	}
	
	/**
	 * @param event the event to set
	 */
	public void setEvent(byte[] event) {
		assert(event!=null && event.length>0) : "Parameter 'event' must not be null or zero-length"; //$NON-NLS-1$
		this.event = event;
	}

}
