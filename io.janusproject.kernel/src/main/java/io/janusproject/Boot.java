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
import org.arakhne.afc.vmutil.locale.Locale;

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

		options.addOption("h", "help", false, Locale.getString("CLI_HELP_H"));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		return options;
	}

	static void showHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("io.janusproject.Boot [OPTIONS] AGENT_FQN", getOptions()); //$NON-NLS-1$
		System.exit(0);
	}

	
	/** Show the heading logo of the Janus platform.
	 */
	public static void showHeader() {
		System.out.println(Locale.getString("JANUS_TEXT_LOGO")); //$NON-NLS-1$
	}

	static void startJanus(Class<? extends Agent> agentCls, String... params) {
		Kernel k = Janus.create(new JanusDefaultConfigModule());
		
		k.spawn(agentCls, params);
	}

}
