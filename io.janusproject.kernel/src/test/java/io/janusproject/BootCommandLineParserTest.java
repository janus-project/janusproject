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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.junit.Before;
import org.junit.Test;

/**
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"nls","javadoc"})
public class BootCommandLineParserTest {

	private Options janusOptions;
	private CommandLineParser parser;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.janusOptions = Boot.getOptions();
		this.parser = new BasicParser();
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

	private static String[] args(String... strings) {
		return strings;
	}
}
