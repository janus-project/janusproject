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

import io.janusproject.kernel.Kernel;
import io.janusproject.network.NetworkConfig;
import io.sarl.lang.core.Agent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.arakhne.afc.vmutil.locale.Locale;

/** This is the class that permits to boot the Janus platform.
 * <p>
 * This class provides the "main" function for the platform.
 * The list of the parameters is composed of a list of options,
 * the classname of an agent to launch, and the parameters to pass
 * to the launched agent.
 * <p>
 * The supported options may be obtain by passing no parameter, or
 * the option <code>-h</code>.  
 * <p>
 * Example of Janus launching with Maven:
 * <pre><code>mvn exec:java
 *     -Dexec.mainClass="io.janusproject.Boot"
 *     -Dexec.args="my.Agent"</code></pre>
 * <p>
 * Example of Janus launching from the CLI (only with the Jar file that is containing
 * all the jar dependencies):
 * <pre><code>java -jar janus-with-dependencies.jar my.Agent</code></pre>  
 * 
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class Boot {

	private static String[] parseCommandLine(String[] args, List<URL> propertyFiles) {
		CommandLineParser parser = new GnuParser();

		try {
			CommandLine cmd = parser.parse(getOptions(), args);
			if (cmd.hasOption('h') || cmd.getArgs().length == 0) {
				showHelp();
			}

			if (cmd.hasOption('o')) {
				System.setProperty(JanusConfig.OFFLINE, Boolean.TRUE.toString());
			}

			if (cmd.hasOption('R')) {
				System.setProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID, Boolean.FALSE.toString());
				System.setProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID, Boolean.TRUE.toString());
			}
			else if (cmd.hasOption('B')) {
				System.setProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID, Boolean.TRUE.toString());
				System.setProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID, Boolean.FALSE.toString());
			}
			else if (cmd.hasOption('W')) {
				System.setProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID, Boolean.FALSE.toString());
				System.setProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID, Boolean.FALSE.toString());
			}

			// Define the system properties, if not already done by the JRE.
			Properties props = cmd.getOptionProperties("D"); //$NON-NLS-1$
			if (props!=null) {
				for(Entry<Object,Object> entry : props.entrySet()) {
					System.setProperty(
							entry.getKey().toString(),
							entry.getValue().toString());
				}
			}

			// Define the verbosity.
			// The order of the options is important.
			if (cmd.hasOption('v') || cmd.hasOption('q')) {
				@SuppressWarnings("unchecked")
				Iterator<Option> optIterator = cmd.iterator();
				int verbose = 0;
				while (optIterator.hasNext()) {
					Option opt = optIterator.next();
					switch(opt.getOpt()) {
					case "q": //$NON-NLS-1$
						verbose = 0;
						break;
					case "v": //$NON-NLS-1$
						verbose++;
						break;
					default:
					}
				}
				System.setProperty(JanusConfig.VERBOSE_LEVEL,
						Integer.toString(Math.min(0, Math.max(6, verbose))));
			}

			// Retreive the list of the property files given on CLI
			if (cmd.hasOption('f')) {
				for(String rawFilename : cmd.getOptionValues('f')) {
					if (rawFilename==null || "".equals(rawFilename)) { //$NON-NLS-1$
						showHelp();
					}
					File file = new File(rawFilename);
					if (!file.canRead()) {
						System.err.println(Locale.getString("INVALID_PROPERTY_FILENAME", rawFilename)); //$NON-NLS-1$
						System.exit(255);
					}
					propertyFiles.add(file.toURI().toURL());
				}
			}

			return cmd.getArgs();
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			showHelp();
			throw new Error(); // Only to avoid compilation errors
		}
	}
	
	/** Main function that is parsing the command line and launching
	 * the first agent.
	 * 
	 * @param args
	 * @see #startJanus(Class, Object...)
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		try {
			List<URL> propertyFiles = new ArrayList<>();
			
			String[] freeArgs = parseCommandLine(args, propertyFiles);
			String agentToLaunch = freeArgs[0];
			
			showJanusLogo();
			
			Class<?> agent = Class.forName(agentToLaunch);

			// The following test is needed because the
			// cast to Class<? extends Agent> is not checking
			// the Agent type (it is a generic type, not
			// tested at runtime).
			if (Agent.class.isAssignableFrom(agent)) {

				// Load property files
				Properties systemProperties = System.getProperties();
				for(URL url : propertyFiles) {
					try(InputStream stream = url.openStream()) {
						systemProperties.load(stream);
					}
				}

				// Set the boot agent classname
				System.setProperty(JanusConfig.BOOT_AGENT, agent.getCanonicalName());

				System.out.println(Locale.getString("LAUNCHING_AGENT", agentToLaunch)); //$NON-NLS-1$
				startJanus(
						(Class<? extends Agent>)agent,
						Arrays.copyOfRange(
								freeArgs,
								1, freeArgs.length,
								Object[].class));
			}
			else {
				throw new ClassCastException(
						Locale.getString("INVALID_AGENT_TYPE", agentToLaunch)); //$NON-NLS-1$
			}
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			showHelp();
		}

	}

	/** Replies the command line options supported by this boot class.
	 * 
	 * @return the command line options.
	 */
	public static Options getOptions() {
		Option opt;
		Options options = new Options();

		options.addOption("h", "help", false, Locale.getString("CLI_HELP_H"));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		options.addOption("f", "file", true, Locale.getString("CLI_HELP_F"));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		options.addOption("o", "offline", false, Locale.getString("CLI_HELP_O", JanusConfig.OFFLINE));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		options.addOption("B", "bootid", false, Locale.getString("CLI_HELP_B", JanusConfig.BOOT_DEFAULT_CONTEXT_ID, JanusConfig.RANDOM_DEFAULT_CONTEXT_ID));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		options.addOption("R", "randomid", false, Locale.getString("CLI_HELP_R", JanusConfig.BOOT_DEFAULT_CONTEXT_ID, JanusConfig.RANDOM_DEFAULT_CONTEXT_ID));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		options.addOption("W", "worldid", false, Locale.getString("CLI_HELP_W", JanusConfig.BOOT_DEFAULT_CONTEXT_ID, JanusConfig.RANDOM_DEFAULT_CONTEXT_ID));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		options.addOption("q", "quiet", false, Locale.getString("CLI_HELP_Q"));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
		options.addOption("v", "verbose", false, Locale.getString("CLI_HELP_V"));  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$

		opt = new Option("D", true, Locale.getString("CLI_HELP_D"));  //$NON-NLS-1$//$NON-NLS-2$
		opt.setArgs(2);
		opt.setValueSeparator('=');
		opt.setArgName(Locale.getString("CLI_HELP_D_ARGNAME")); //$NON-NLS-1$
		options.addOption(opt);

		return options;
	}


	/** Show the help message on the standard console.
	 * This function never returns.
	 */
	public static void showHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(Boot.class.getName()+" [OPTIONS] <agent_classname>", getOptions()); //$NON-NLS-1$

		Map<String,Object> defaultValues = new TreeMap<>();
		JanusConfig.getDefaultValues(defaultValues);
		NetworkConfig.getDefaultValues(defaultValues);
		String none = Locale.getString("NONE"); //$NON-NLS-1$
		System.out.println();
		System.out.println(Locale.getString("DEFAULT_PROPERTIES")); //$NON-NLS-1$
		for(Entry<String,Object> entry : defaultValues.entrySet()) {
			Object o = entry.getValue();
			if (o!=null) {
				o = "'"+o.toString()+"'"; //$NON-NLS-1$//$NON-NLS-2$
			}
			else {
				o = none;
			}
			System.out.println(Locale.getString("DEFAULT_PROPERTY", entry.getKey(), o)); //$NON-NLS-1$
		}

		System.exit(255);
	}

	/** Show the heading logo of the Janus platform.
	 */
	public static void showJanusLogo() {
		System.out.println(Locale.getString("JANUS_TEXT_LOGO")); //$NON-NLS-1$
	}

	/** Launch the first agent of the Janus kernel.
	 * <p>
	 * Thus function does not parse the command line.
	 * See {@link #main(String[])} for the command line management.
	 * When this function is called, it is assumed that all the
	 * system's properties are correctly set.
	 * 
	 * @param agentCls - type of the first agent to launch.
	 * @param params - parameters to pass to the agent as its initliazation parameters.
	 * @see #main(String[])
	 */
	public static void startJanus(Class<? extends Agent> agentCls, Object... params) {
		Kernel k = Kernel.create(new JanusDefaultModule());
		k.spawn(agentCls, params);
	}

}
