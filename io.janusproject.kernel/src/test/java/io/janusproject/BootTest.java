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

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;

import io.janusproject.kernel.Kernel;
import io.sarl.lang.core.Agent;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.arakhne.afc.vmutil.FileSystem;
import org.arakhne.afc.vmutil.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provides;

import static org.mockito.Mockito.*;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@RunWith(Suite.class)
@SuiteClasses({
	BootTest.BootCommandLineParserTests.class,
	BootTest.OptionSetterTests.class,
	BootTest.StartTests.class,
	BootTest.Start2Tests.class,
})
@SuppressWarnings("all")
public class BootTest extends Assert {

	static final UUID ID = UUID.fromString("63ee52ee-4739-47b1-9e73-0a7986d17bc5");

	private static String[] args(String... strings) {
		return strings;
	}

	private static void assertTrueStr(String actual) {
		assertEquals(Boolean.TRUE.toString(), actual);
	}

	private static void assertFalseStr(String actual) {
		assertEquals(Boolean.FALSE.toString(), actual);
	}

	private static void assertTrueProperty(String name) {
		assertTrueStr(System.getProperty(name));
	}

	private static void assertFalseProperty(String name) {
		assertFalseStr(System.getProperty(name));
	}
	
	protected static void resetProperties() {
		Properties tmp = new Properties();
		JanusConfig.getDefaultValues(tmp);
		Properties props = System.getProperties();
		for(Object name : tmp.keySet()) {
			props.remove(name);
		}
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class AgentMock extends Agent {
		/**
		 */
		public AgentMock() {
			super(null);
		}
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class TestModule implements Module {

		public TestModule() {
			//
		}

		@Override
		public void configure(Binder binder) {
			//
		}

		@Provides
		public Kernel createKernel() {
			Kernel k = mock(Kernel.class);
			when(k.spawn(any(Class.class), anyVararg())).thenReturn(ID);
			return k;
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class OptionSetterTests {

		@Before
		public void setUp() {
			resetProperties();
		}
		
		@After
		public void tearDown() {
			resetProperties();
		}

		@Test
		public void setOffline_true() {
			Boot.setOffline(true);
			assertTrueProperty(JanusConfig.OFFLINE);
		}

		@Test
		public void setOffline_false() {
			Boot.setOffline(false);
			assertFalseProperty(JanusConfig.OFFLINE);
		}

		@Test
		public void setRandomContextUUID() {
			Boot.setRandomContextUUID();
			assertFalseProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
			assertTrueProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		}

		@Test
		public void setBootAgentTypeContextUUID() {
			Boot.setBootAgentTypeContextUUID();
			assertTrueProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
			assertFalseProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		}

		public void setDefaultContextUUID() {
			Boot.setDefaultContextUUID();
			assertFalseProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME);
			assertFalseProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME);
		}

		@Test
		public void setVerboseLevel() {
			for(int i = -10; i < 10; ++i) {
				Boot.setVerboseLevel(i);
				assertEquals(Integer.toString(i), System.getProperty(JanusConfig.VERBOSE_LEVEL_NAME));
			}
		}

		@Test
		public void setProperty_withValue() {
			String name = UUID.randomUUID().toString();
			String value = UUID.randomUUID().toString();
			Boot.setProperty(name, value);
			assertEquals(value, System.getProperty(name));
		}

		@Test
		public void setPropertiesFromURL() throws IOException {
			URL resource = Resources.getResource(getClass(), "Test1.properties");
			Assume.assumeNotNull(resource);
			Boot.setPropertiesFrom(resource);
			assertEquals("my value 0", System.getProperty("io.janusproject.tests.MY_PROPERTY_0"));
			assertEquals("my value 1", System.getProperty("io.janusproject.tests.MY_PROPERTY_1"));
			assertEquals("my value 2", System.getProperty("io.janusproject.tests.MY_PROPERTY_2"));
		}

		@Test
		public void setPropertiesFromFile() throws IOException {
			URL resource = Resources.getResource(getClass(), "Test2.properties");
			Assume.assumeNotNull(resource);
			File file = FileSystem.convertURLToFile(resource);
			Assume.assumeNotNull(file);
			Boot.setPropertiesFrom(file);
			assertEquals("my value 3", System.getProperty("io.janusproject.tests.MY_PROPERTY_3"));
			assertEquals("my value 4", System.getProperty("io.janusproject.tests.MY_PROPERTY_4"));
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class BootCommandLineParserTests {

		private Options janusOptions;
		private CommandLineParser parser;

		/**
		 * @throws java.lang.Exception
		 */
		@Before
		public void setUp() throws Exception {
			resetProperties();
			this.janusOptions = Boot.getOptions();
			this.parser = new BasicParser();
		}

		@After
		public void tearDown() {
			resetProperties();
		}

		@Test
		public void testHelp() throws Exception {
			CommandLine cmd = this.parser.parse(this.janusOptions, args(""));
			assertFalse(cmd.hasOption('h'));
			cmd = this.parser.parse(this.janusOptions, args("-h"));
			assertTrue(cmd.hasOption('h'));
			cmd = this.parser.parse(this.janusOptions, args("-help"));
			assertTrue(cmd.hasOption('h'));
			cmd = this.parser.parse(this.janusOptions, args("--help"));
			assertTrue(cmd.hasOption('h'));

			cmd = this.parser.parse(this.janusOptions, args("-f", "thefile"));
			assertTrue(cmd.hasOption('f'));
			cmd = this.parser.parse(this.janusOptions, args("-file", "thefile"));
			assertTrue(cmd.hasOption('f'));
			cmd = this.parser.parse(this.janusOptions, args("--file", "thefile"));
			assertTrue(cmd.hasOption('f'));

			cmd = this.parser.parse(this.janusOptions, args("-B", "uid"));
			assertTrue(cmd.hasOption('B'));
			cmd = this.parser.parse(this.janusOptions, args("-bootid", "uid"));
			assertTrue(cmd.hasOption('B'));
			cmd = this.parser.parse(this.janusOptions, args("--bootid", "uid"));
			assertTrue(cmd.hasOption('B'));

			cmd = this.parser.parse(this.janusOptions, args("-R", "uid"));
			assertTrue(cmd.hasOption('R'));
			cmd = this.parser.parse(this.janusOptions, args("-randomid", "uid"));
			assertTrue(cmd.hasOption('R'));
			cmd = this.parser.parse(this.janusOptions, args("--randomid", "uid"));
			assertTrue(cmd.hasOption('R'));

			cmd = this.parser.parse(this.janusOptions, args("-W", "uid"));
			assertTrue(cmd.hasOption('W'));
			cmd = this.parser.parse(this.janusOptions, args("-worldid", "uid"));
			assertTrue(cmd.hasOption('W'));
			cmd = this.parser.parse(this.janusOptions, args("--worldid", "uid"));
			assertTrue(cmd.hasOption('W'));

			cmd = this.parser.parse(this.janusOptions, args("-D", "name=value"));
			assertTrue(cmd.hasOption('D'));
			cmd = this.parser.parse(this.janusOptions, args("-D", "name"));
			assertTrue(cmd.hasOption('D'));
		}

		@Test
		public void testAgentArg() throws Exception {
			CommandLine cmd = this.parser.parse(this.janusOptions, null);

			cmd = this.parser.parse(this.janusOptions, args("-h", "main.Agent"));
			assertEquals(1, cmd.getArgs().length);
			assertEquals("main.Agent", cmd.getArgs()[0]);

			cmd = this.parser.parse(this.janusOptions, args("-h", "main.Agent", "12"));
			assertEquals(2, cmd.getArgs().length);
			assertEquals("main.Agent", cmd.getArgs()[0]);
			assertEquals("12", cmd.getArgs()[1]);

			cmd = this.parser.parse(this.janusOptions, args("-h", "main.Agent", "12", "hola"));
			assertEquals(3, cmd.getArgs().length);
			assertEquals("main.Agent", cmd.getArgs()[0]);
			assertEquals("12", cmd.getArgs()[1]);
			assertEquals("hola", cmd.getArgs()[2]);

		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class StartTests {

		@Before
		public void setUp() {
			resetProperties();
		}

		@After
		public void tearDown() {
			resetProperties();
		}

		@Test
		public void startJanus() throws Exception {
			Kernel kernel = Boot.startJanus(TestModule.class, AgentMock.class, "param1", "param2", "param3");

			assertNotNull(kernel);

			ArgumentCaptor<Class> agentType = ArgumentCaptor.forClass(Class.class);
			ArgumentCaptor<String> parameters = ArgumentCaptor.forClass(String.class);
			verify(kernel).spawn(agentType.capture(), parameters.capture());
			assertEquals(AgentMock.class, agentType.getValue());
			assertArrayEquals(new String[] {
					"param1", "param2", "param3"
			}, parameters.getAllValues().toArray());

			assertEquals(AgentMock.class.getCanonicalName(), System.getProperty(JanusConfig.BOOT_AGENT));
			String sid = System.getProperty(JanusConfig.BOOT_AGENT_ID);
			assertNotNull(sid);
			assertEquals(ID.toString(), sid);
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class Start2Tests {

		@Before
		public void setUp() {
			resetProperties();
		}

		@After
		public void tearDown() {
			resetProperties();
		}

		@Test
		public void getBootAgentIdentifier_notStarted() throws Exception {
			assertNull(Boot.getBootAgentIdentifier());
		}

		@Test
		public void getBootAgentIdentifier_started() throws Exception {
			Boot.startJanus(TestModule.class, AgentMock.class);
			assertEquals(ID, Boot.getBootAgentIdentifier());
		}

		@Test
		public void getBootAgentIdentifier_startedAgain() throws Exception {
			Boot.startJanus(TestModule.class, AgentMock.class);
			assertEquals(ID, Boot.getBootAgentIdentifier());
			assertEquals(ID, Boot.getBootAgentIdentifier());
		}

	}

}
