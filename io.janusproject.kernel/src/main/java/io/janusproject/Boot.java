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

import java.util.Arrays;

import io.janusproject.kernel.Janus;
import io.janusproject.kernel.JanusDefaultConfigModule;
import io.janusproject.kernel.Kernel;
import io.sarl.lang.core.Agent;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * 
 * mvn exec:java -Dexec.mainClass="io.janusproject.Boot"
 * [-Dexec.args="my.Agent"]
 * 
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class Boot {

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		CommandLineParser parser = new BasicParser();

		try {
			CommandLine cmd = parser.parse(getOptions(), args);
			if (cmd.hasOption('h')) {
				showHelp();

			}

			if (cmd.getArgs().length == 0) {
				showHelp();
			}

			String agentToLaunch = cmd.getArgs()[0];
			showHeader();
			System.out.println("Launching agent: " + agentToLaunch);
			Class<?> agent = Class.forName(agentToLaunch);			
			startJanus((Class<? extends Agent>) agent, Arrays.copyOfRange(cmd.getArgs(), 1, cmd.getArgs().length));
		} catch (ParseException | ClassNotFoundException e) {
			e.printStackTrace();
			showHelp();
			System.exit(-1);
		}

	}

	static Options getOptions() {
		Options options = new Options();

		options.addOption("h", "help", false, "Display help");
		return options;
	}

	static void showHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("io.janusproject.Boot [OPTIONS] AGENT_FQN", getOptions());
		System.exit(0);
	}

	static void showHeader() {
		System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
		System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
		System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
		System.out.println("MMMMMM$77777777777777777777777777NMMMMMM");
		System.out.println("MMMM$77777$ZO8OO7777$77777777777777OMMMM");
		System.out.println("MM77MMMMMMMMMMMM7777MMMZ777$MMMMN7777MMM");
		System.out.println("MMMMMMMMMMMMMMMM777NMMN77778MMMMMM777ZMM");
		System.out.println("MMMMMMMMMMMMMMMO77OMMM$7777MMMMMMMD777MM");
		System.out.println("MMMMMMMMMMMMMMN777MMMM7777NMMMMMMMN777MM");
		System.out.println("MMMMMMMMMMMMMMO777MMMM7777MMMMMMMMO777MM");
		System.out.println("MMMMMMMMMMMMMM$77$MMMD7777MMMMMMMM7777MM");
		System.out.println("MMMMMMMMMMMMMM777DMMM$777$MMMMMMZ7777MMM");
		System.out.println("MMMMMMMMMMNDO$777ZZZZ7777$$777777777MMMM");
		System.out.println("MMMMMD777777777777777777777777777$MMMMMM");
		System.out.println("MMMM77777777777777777777777777$8MMMMMMMM");
		System.out.println("MM77777$NMMMM777NMMMO7778MMMMMMMMMMMMMMM");
		System.out.println("M$77ZMMMMMMMM777MMMMZ777MMMMMMMMMMMMMMMM");
		System.out.println("M77ZMMMMMMMM8777MMMM777ZMMMMMMMMMMMMMMMM");
		System.out.println("877NMMMMMMMM7778MMMM77DMMMMMMMMMMMMMMMMM");
		System.out.println("877DMMMMMMMN77ZMMMMO7$MMMMMMMMMMMMMMMMMM");
		System.out.println("M777NMMMMN7777MMMMZ7DMMMM777MMMMMMMMMMMM");
		System.out.println("MM777777777$MMMMMZMMMMMM$777MMMMMMMMMMMM");
		System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
		System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
		System.out.println("MMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMM");
		  
	}

	static void startJanus(Class<? extends Agent> agentCls, String... params) {
		Kernel k = Janus.create(new JanusDefaultConfigModule());
		
		k.spawn(agentCls, params);
	}

}
