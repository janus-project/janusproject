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
package io.janusproject.network.event;

import io.sarl.lang.core.Event;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.core.SpaceSpecification;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc","unchecked"})
public class GsonEventSerializerTest extends Assert {

	private EventDispatch dispatch;
	private EventEnvelope envelope;
	private GsonEventSerializer serializer;
	
	@Before
	public void setUp() throws Exception {
		Gson gson = new GsonBuilder().registerTypeAdapter(Class.class, new GsonEventSerializer.ClassTypeAdapter()).setPrettyPrinting().create();

		SpaceID spaceId = new SpaceID(
				UUID.fromString("005dd043-8553-40d2-8094-ad159bfabf86"), //$NON-NLS-1$
				UUID.fromString("76595ddf-bc40-479d-b92a-7c1785642f9c"), //$NON-NLS-1$
				(Class<? extends SpaceSpecification<?>>)SpaceSpecification.class);
		Scope<?> scope = new ScopeMock();
		Map<String,String> headers = new HashMap<>();
		headers.put("a", "b");  //$NON-NLS-1$//$NON-NLS-2$
		Event event = new EventMock();
		this.dispatch = new EventDispatch(spaceId, event, scope, headers);
				
		this.envelope = new EventEnvelope(
				"005dd043-8553-40d2-8094-ad159bfabf86".getBytes(Charset.forName("UTF-8")), //$NON-NLS-1$ //$NON-NLS-2$
				"76595ddf-bc40-479d-b92a-7c1785642f9c".getBytes(Charset.forName("UTF-8")), //$NON-NLS-1$ //$NON-NLS-2$
				new byte[] {123, 125},
				new byte[] {123, 10, 32, 32, 34, 120, 45, 106, 97, 118, 97, 45, 101, 118, 101, 110, 116, 45, 99, 108, 97, 115, 115, 34, 58, 32, 34, 105, 111, 46, 106, 97, 110, 117, 115, 112, 114, 111, 106, 101, 99, 116, 46, 110, 101, 116, 119, 111, 114, 107, 46, 101, 118, 101, 110, 116, 46, 71, 115, 111, 110, 69, 118, 101, 110, 116, 83, 101, 114, 105, 97, 108, 105, 122, 101, 114, 84, 101, 115, 116, 36, 69, 118, 101, 110, 116, 77, 111, 99, 107, 34, 44, 10, 32, 32, 34, 97, 34, 58, 32, 34, 98, 34, 44, 10, 32, 32, 34, 120, 45, 106, 97, 118, 97, 45, 115, 112, 97, 99, 101, 115, 112, 101, 99, 45, 99, 108, 97, 115, 115, 34, 58, 32, 34, 105, 111, 46, 115, 97, 114, 108, 46, 108, 97, 110, 103, 46, 99, 111, 114, 101, 46, 83, 112, 97, 99, 101, 83, 112, 101, 99, 105, 102, 105, 99, 97, 116, 105, 111, 110, 34, 44, 10, 32, 32, 34, 120, 45, 106, 97, 118, 97, 45, 115, 99, 111, 112, 101, 45, 99, 108, 97, 115, 115, 34, 58, 32, 34, 105, 111, 46, 106, 97, 110, 117, 115, 112, 114, 111, 106, 101, 99, 116, 46, 110, 101, 116, 119, 111, 114, 107, 46, 101, 118, 101, 110, 116, 46, 71, 115, 111, 110, 69, 118, 101, 110, 116, 83, 101, 114, 105, 97, 108, 105, 122, 101, 114, 84, 101, 115, 116, 36, 83, 99, 111, 112, 101, 77, 111, 99, 107, 34, 10, 125},
				new byte[] {123, 125});
		
		this.serializer = new GsonEventSerializer(gson, new PlainTextEventEncrypter());
	}
	
	@After
	public void tearDown() {
		this.serializer = null;
		this.dispatch = null;
		this.envelope = null;
	}

	@Test
	public void serialize() throws Exception {
		EventEnvelope e = this.serializer.serialize(this.dispatch);
		assertNotNull(e);
		
		assertArrayEquals(
				this.envelope.getContextId(),
				e.getContextId());

		assertArrayEquals(
				this.envelope.getSpaceId(),
				e.getSpaceId());

		assertArrayEquals(
				this.envelope.getScope(),
				e.getScope());

		assertArrayEquals(
				this.envelope.getCustomHeaders(),
				e.getCustomHeaders());

		assertArrayEquals(
				this.envelope.getBody(),
				e.getBody());
	}

	@Test
	public void deserialize() throws Exception {
		EventDispatch d = this.serializer.deserialize(this.envelope);
		assertNotNull(d);
		
		assertEquals(
				this.dispatch.getSpaceID(),
				d.getSpaceID());

		assertEquals(
				this.dispatch.getScope(),
				d.getScope());

		assertNotEquals(
				this.dispatch.getCustomHeaders(),
				d.getCustomHeaders());

		Map<String,String> realHeaders = new HashMap<>();
		realHeaders.put("a", "b");  //$NON-NLS-1$//$NON-NLS-2$
		realHeaders.put("x-java-spacespec-class", "io.sarl.lang.core.SpaceSpecification");  //$NON-NLS-1$//$NON-NLS-2$
		realHeaders.put("x-java-event-class", "io.janusproject.network.event.GsonEventSerializerTest$EventMock");  //$NON-NLS-1$//$NON-NLS-2$
		realHeaders.put("x-java-scope-class", "io.janusproject.network.event.GsonEventSerializerTest$ScopeMock");  //$NON-NLS-1$//$NON-NLS-2$

		assertEquals(
				realHeaders,
				d.getCustomHeaders());

		assertEquals(
				this.dispatch.getEvent(),
				d.getEvent());
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class EventMock extends Event {
		
		private static final long serialVersionUID = 8517823813578172006L;

		/**
		 */
		public EventMock() {
			//
		}
		
		/** {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			return (obj instanceof EventMock);
		}
		
		/** {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return 1234567890;
		}
		
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class ScopeMock implements Scope<String> {
		
		private static final long serialVersionUID = -3244607127069483542L;

		/**
		 */
		public ScopeMock() {
			//
		}
		
		/** {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			return (obj instanceof ScopeMock);
		}
		
		/** {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return 987654321;
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean matches(String element) {
			throw new UnsupportedOperationException();
		}
		
	}

}
