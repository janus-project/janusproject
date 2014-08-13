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
package io.janusproject.services.network.event;

import io.janusproject.services.network.event.EventEnvelope;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc"})
public class EventEnvelopeTest extends Assert {

	private byte[] contextId;
	private byte[] spaceId;
	private byte[] event;
	private byte[] scope;
	private byte[] headers;
	private EventEnvelope envelope;
	
	@Before
	public void setUp() {
		this.contextId = new byte[] { 32 };
		this.spaceId = new byte[] { 32 };
		this.event = new byte[] { 32 };
		this.scope = new byte[] { 32 };
		this.headers= new byte[] { 32 };
		this.envelope = new EventEnvelope(this.contextId, this.spaceId, this.scope, this.headers, this.event);
	}
	
	@After
	public void tearDown() {
		this.envelope = null;
		this.scope = null;
		this.event = null;
		this.spaceId = null;
		this.contextId = null;
		this.headers = null;
	}

	@Test
	public void getCustomHeaders() {
		assertSame(this.headers, this.envelope.getCustomHeaders());
	}

	@Test
	public void getBody() {
		assertSame(this.event, this.envelope.getBody());
	}

	@Test
	public void getContextId() {
		assertSame(this.contextId, this.envelope.getContextId());
	}

	@Test
	public void getSpaceId() {
		assertSame(this.spaceId, this.envelope.getSpaceId());
	}

	@Test
	public void getScope() {
		assertSame(this.scope, this.envelope.getScope());
	}

	@Test
	public void setCustomHeaders() {
		byte[] newMock = new byte[] { 32 };
		this.envelope.setCustomHeaders(newMock);
		assertSame(newMock, this.envelope.getCustomHeaders());
	}

	@Test
	public void setBody() {
		byte[] newMock = new byte[] { 32 };
		this.envelope.setBody(newMock);
		assertSame(newMock, this.envelope.getBody());
	}

	@Test
	public void setContextId() {
		byte[] newMock = new byte[] { 32 };
		this.envelope.setContextId(newMock);
		assertSame(newMock, this.envelope.getContextId());
	}

	@Test
	public void setSpaceId() {
		byte[] newMock = new byte[] { 32 };
		this.envelope.setSpaceId(newMock);
		assertSame(newMock, this.envelope.getSpaceId());
	}

	@Test
	public void setScope() {
		byte[] newMock = new byte[] { 32 };
		this.envelope.setScope(newMock);
		assertSame(newMock, this.envelope.getScope());
	}

}
