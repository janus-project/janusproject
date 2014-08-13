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
import io.janusproject.services.network.event.PlainTextEventEncrypter;

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
public class PlainTextEventEncrypterTest extends Assert {

	private byte[] contextId;
	private byte[] spaceId;
	private byte[] event;
	private byte[] scope;
	private byte[] headers;
	private EventEnvelope envelope;
	private PlainTextEventEncrypter encrypter;
	
	@Before
	public void setUp() {
		this.contextId = new byte[] { 1, 2, 3, 4, 5 };
		this.spaceId = new byte[] { 6, 7, 8, 9, 10 };
		this.event = new byte[] { 11, 12, 13, 14 };
		this.scope = new byte[] { 15, 16, 17 };
		this.headers= new byte[] { 18, 19, 20, 21 };
		this.envelope = new EventEnvelope(this.contextId, this.spaceId, this.scope, this.headers, this.event);
		this.encrypter = new PlainTextEventEncrypter();
	}
	
	@After
	public void tearDown() {
		this.encrypter = null;
		this.envelope = null;
		this.scope = null;
		this.event = null;
		this.spaceId = null;
		this.contextId = null;
		this.headers = null;
	}

	@Test
	public void encrypt() {
		this.encrypter.encrypt(this.envelope);
		assertSame(this.contextId, this.envelope.getContextId());
		assertSame(this.spaceId, this.envelope.getSpaceId());
		assertSame(this.scope, this.envelope.getScope());
		assertSame(this.headers, this.envelope.getCustomHeaders());
		assertSame(this.event, this.envelope.getBody());
	}

	@Test
	public void decrypt() {
		this.encrypter.decrypt(this.envelope);
		assertSame(this.contextId, this.envelope.getContextId());
		assertSame(this.spaceId, this.envelope.getSpaceId());
		assertSame(this.scope, this.envelope.getScope());
		assertSame(this.headers, this.envelope.getCustomHeaders());
		assertSame(this.event, this.envelope.getBody());
	}

}
