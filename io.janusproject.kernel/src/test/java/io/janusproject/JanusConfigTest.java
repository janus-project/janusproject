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
package io.janusproject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import java.util.UUID;

import org.junit.Test;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc","static-method"})
public class JanusConfigTest {

	private static final String DEFAULT_VALUE = UUID.randomUUID().toString();
	
	@Test
	public void testGetSystemPropertyString_fromProperties() throws Exception {
		for(Object k : System.getProperties().keySet()) {
			String sv = System.getProperty(k.toString());
			String v = JanusConfig.getSystemProperty(k.toString());
			assertEquals(sv, v);
			v = JanusConfig.getSystemProperty(k+UUID.randomUUID().toString());
			assertNull(v);
		}
	}

	@Test
	public void testGetSystemPropertyStringString_fromProperties() throws Exception {
		for(Object k : System.getProperties().keySet()) {
			String sv = System.getProperty(k.toString());
			String v = JanusConfig.getSystemProperty(k.toString(), DEFAULT_VALUE);
			assertEquals(sv, v);
			v = JanusConfig.getSystemProperty(k+UUID.randomUUID().toString(), DEFAULT_VALUE);
			assertSame(DEFAULT_VALUE, v);
		}
	}

	@Test
	public void testGetSystemPropertyString_fromEnv() throws Exception {
		for(Object k : System.getenv().keySet()) {
			String sv = System.getenv(k.toString());
			String v = JanusConfig.getSystemProperty(k.toString());
			assertEquals(sv, v);
			v = JanusConfig.getSystemProperty(k+UUID.randomUUID().toString());
			assertNull(v);
		}
	}

	@Test
	public void testGetSystemPropertyStringString_fromEnv() throws Exception {
		for(Object k : System.getenv().keySet()) {
			String sv = System.getenv(k.toString());
			String v = JanusConfig.getSystemProperty(k.toString(), DEFAULT_VALUE);
			assertEquals(sv, v);
			v = JanusConfig.getSystemProperty(k+UUID.randomUUID().toString(), DEFAULT_VALUE);
			assertSame(DEFAULT_VALUE, v);
		}
	}

	@Test
	public void testGetSystemPropertyAsInteger() throws Exception {
		System.setProperty("janus.unit.test.enum", Integer.toString(234)); //$NON-NLS-1$
		for(Object k : System.getProperties().keySet()) {
			String sv = JanusConfig.getSystemProperty(k.toString(), ""); //$NON-NLS-1$
			int v = JanusConfig.getSystemPropertyAsInteger(k.toString(), 567);
			if (k.equals("janus.unit.test.enum")) { //$NON-NLS-1$
				assertEquals(234, v);
			}
			else {
				int rv;
				try {
					rv = Integer.parseInt(sv);
				}
				catch(Throwable _) {
					rv = 567;
				}
				assertEquals(rv, v);
			}
		}
		int v = JanusConfig.getSystemPropertyAsInteger(
				"janus.unit.test.enum2", 789); //$NON-NLS-1$
		assertEquals(789, v);
	}

	@Test
	public void testGetSystemPropertyAsBoolean() throws Exception {
		System.setProperty("janus.unit.test.enum", Boolean.TRUE.toString()); //$NON-NLS-1$
		for(Object k : System.getProperties().keySet()) {
			String sv = JanusConfig.getSystemProperty(k.toString(), ""); //$NON-NLS-1$
			boolean v = JanusConfig.getSystemPropertyAsBoolean(k.toString(), true);
			if (k.equals("janus.unit.test.enum")) { //$NON-NLS-1$
				assertEquals(true, v);
			}
			else {
				assertSame(Boolean.parseBoolean(sv), v);
			}
		}
		boolean v = JanusConfig.getSystemPropertyAsBoolean(
				"janus.unit.test.enum2", true); //$NON-NLS-1$
		assertEquals(true, v);
	}

	@Test
	public void testGetSystemPropertyAsEnum() throws Exception {
		System.setProperty("janus.unit.test.enum", UnitTestEnum.UNIT_TEST_ENUM_DEF.name()); //$NON-NLS-1$
		for(Object k : System.getProperties().keySet()) {
			UnitTestEnum v = JanusConfig.getSystemPropertyAsEnum(
					UnitTestEnum.class, k.toString(), UnitTestEnum.UNIT_TEST_ENUM_ABC);
			if (k.equals("janus.unit.test.enum")) { //$NON-NLS-1$
				assertSame(UnitTestEnum.UNIT_TEST_ENUM_DEF, v);
			}
			else {
				assertSame(UnitTestEnum.UNIT_TEST_ENUM_ABC, v);
			}
		}
		UnitTestEnum v = JanusConfig.getSystemPropertyAsEnum(
				UnitTestEnum.class, "janus.unit.test.enum2", UnitTestEnum.UNIT_TEST_ENUM_ABC); //$NON-NLS-1$
		assertSame(UnitTestEnum.UNIT_TEST_ENUM_ABC, v);
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static enum UnitTestEnum {
		UNIT_TEST_ENUM_ABC, UNIT_TEST_ENUM_DEF;
	}

}
