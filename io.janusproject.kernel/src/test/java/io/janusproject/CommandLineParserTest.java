/*
 * $Id$
 * 
 * Janus platform is an open-source multiagent platform.
 * More details on &lt;http://www.janusproject.io&gt;
 * Copyright (C) 2013 Janus Core Developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
 */
package io.janusproject;

import static org.junit.Assert.*;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.junit.Before;
import org.junit.Test;

/**
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class CommandLineParserTest {

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
