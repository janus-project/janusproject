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
import io.janusproject.services.network.NetworkConfig;
import io.janusproject.util.LoggerCreator;
import io.sarl.lang.core.Agent;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.arakhne.afc.vmutil.locale.Locale;

import com.google.inject.Module;
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
public final class Boot {
	private static final int ERROR_EXIT_CODE = 255;
	private Boot() {
		//
	}
	private static void parseCommandForInfoOptions(CommandLine cmd) {
		if (cmd.hasOption('h') || cmd.getArgs().length == 0) {
			showHelp();
		}
		if (cmd.hasOption('s')) {
			showDefaults();
		}
	}
	private static void parseCommandLineForSystemProperties(CommandLine cmd) {
		if (cmd.hasOption('o')) {
			System.setProperty(JanusConfig.OFFLINE, Boolean.TRUE.toString());
		}
		if (cmd.hasOption('R')) {
			System.setProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME, Boolean.FALSE.toString());
			System.setProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME, Boolean.TRUE.toString());
		} else if (cmd.hasOption('B')) {
			System.setProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME, Boolean.TRUE.toString());
			System.setProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME, Boolean.FALSE.toString());
		} else if (cmd.hasOption('W')) {
			System.setProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME, Boolean.FALSE.toString());
			System.setProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME, Boolean.FALSE.toString());
		}
		// Define the system properties, if not already done by the JRE.
		Properties props = cmd.getOptionProperties("D"); //$NON-NLS-1$
		if (props != null) {
			for (Entry<Object, Object> entry : props.entrySet()) {
				System.setProperty(
						entry.getKey().toString(),
						entry.getValue().toString());
			}
		}
	}
	private static void parseCommandLineForVerbosity(CommandLine cmd) {
		// The order of the options is important.
		int verbose = LoggerCreator.toInt(JanusConfig.VERBOSE_LEVEL_VALUE);
		if (cmd.hasOption('v') || cmd.hasOption('q') || cmd.hasOption('l')) {
			@SuppressWarnings("unchecked")
			Iterator<Option> optIterator = cmd.iterator();
			while (optIterator.hasNext()) {
				Option opt = optIterator.next();
				switch(opt.getOpt()) {
				case "l": //$NON-NLS-1$
					verbose = LoggerCreator.toInt(opt.getValue());
					break;
				case "q": //$NON-NLS-1$
					--verbose;
					break;
				case "v": //$NON-NLS-1$
					++verbose;
					break;
				default:
				}
			}
			System.setProperty(
					JanusConfig.VERBOSE_LEVEL_NAME,
					Integer.toString(verbose));
		}

		// Show the Janus logo?
		if (cmd.hasOption("nologo") || verbose == 0) { //$NON-NLS-1$
			System.setProperty(
					JanusConfig.JANUS_LOGO_SHOW_NAME,
					Boolean.FALSE.toString());
		}
	}
	/** Parse the command line.
	 *
	 * @param args - the CLI arguments given to the program.
	 * @param propertyFiles - files that may be filled with the filenames given on the CLI.
	 * @return the arguments that are not recognized as CLI options.
	 */
	public static String[] parseCommandLine(String[] args, List<URL> propertyFiles) {
		CommandLineParser parser = new GnuParser();
		try {
			CommandLine cmd = parser.parse(getOptions(), args);
			parseCommandForInfoOptions(cmd);
			parseCommandLineForSystemProperties(cmd);
			parseCommandLineForVerbosity(cmd);
			// Retreive the list of the property files given on CLI
			if (cmd.hasOption('f')) {
				for (String rawFilename : cmd.getOptionValues('f')) {
					if (rawFilename == null || "".equals(rawFilename)) { //$NON-NLS-1$
						showHelp();
					}
					File file = new File(rawFilename);
					if (!file.canRead()) {
						//CHECKSTYLE:OFF
						System.err.println(Locale.getString(
								"INVALID_PROPERTY_FILENAME", //$NON-NLS-1$
								rawFilename));
						//CHECKSTYLE:ON
						System.exit(ERROR_EXIT_CODE);
					}
					propertyFiles.add(file.toURI().toURL());
				}
			}
			return cmd.getArgs();
		} catch (IOException | ParseException e) {
			//CHECKSTYLE:OFF
			e.printStackTrace();
			//CHECKSTYLE:ON
			showHelp();
			// Only to avoid compilation errors
			throw new Error();
		}
	}

	/** Show an error message, and exit.
	 *
	 * @param message - the description of the error.
	 * @param e - the cause of the error.
	 */
	protected static void showError(String message, Throwable e) {
		if (message != null && !message.isEmpty()) {
			//CHECKSTYLE:OFF
			System.err.println(message);
			//CHECKSTYLE:ON
		}
		if (e != null) {
			//CHECKSTYLE:OFF
			e.printStackTrace();
			//CHECKSTYLE:ON
		}
		showHelp();
	}

	private static Class<? extends Agent> loadAgentClass(String fullyQualifiedName) {
		Class<?> type;
		try {
			type = Class.forName(fullyQualifiedName);
		} catch (Exception e) {
			showError(
					Locale.getString(
							"INVALID_AGENT_QUALIFIED_NAME", //$NON-NLS-1$
							fullyQualifiedName,
							System.getProperty("java.class.path")), //$NON-NLS-1$
					null);
			return null;
		}
		// The following test is needed because the
		// cast to Class<? extends Agent> is not checking
		// the Agent type (it is a generic type, not
		// tested at runtime).
		if (Agent.class.isAssignableFrom(type)) {
			return type.asSubclass(Agent.class);
		}

		showError(
			Locale.getString("INVALID_AGENT_TYPE", //$NON-NLS-1$
				 fullyQualifiedName),
			null);
		return null;
	}

	/** Main function that is parsing the command line and launching
	 * the first agent.
	 *
	 * @param args - command line arguments
	 * @see #startJanus(Class, Class, Object...)
	 */
	public static void main(String[] args) {
		try {
			List<URL> propertyFiles = new ArrayList<>();
			Object[] freeArgs = parseCommandLine(args, propertyFiles);
			if (JanusConfig.getSystemPropertyAsBoolean(
					JanusConfig.JANUS_LOGO_SHOW_NAME,
					JanusConfig.JANUS_LOGO_SHOW)) {
				showJanusLogo();
			}
			if (freeArgs.length == 0) {
				showError(
						Locale.getString("NO_AGENT_QUALIFIED_NAME"), //$NON-NLS-1$
						null);
			}
			String agentToLaunch = freeArgs[0].toString();
			freeArgs = Arrays.copyOfRange(
					freeArgs,
					1, freeArgs.length,
					String[].class);
			// Load the agent class
			Class<? extends Agent> agent = loadAgentClass(agentToLaunch);
			assert (agent != null);
			// Load property files
			Properties systemProperties = System.getProperties();
			for (URL url : propertyFiles) {
				try (InputStream stream = url.openStream()) {
					systemProperties.load(stream);
				}
			}
			// Set the boot agent classname
			System.setProperty(JanusConfig.BOOT_AGENT, agent.getCanonicalName());

			// Get the start-up injection module
			Class<? extends Module> startupModule = JanusConfig.getSystemPropertyAsClass(
					Module.class, JanusConfig.INJECTION_MODULE_NAME,
					JanusConfig.INJECTION_MODULE_NAME_VALUE);

			startJanus(
					startupModule,
					(Class<? extends Agent>) agent,
					freeArgs);
		} catch (Exception e) {
			showError(
					Locale.getString(
							"LAUNCHING_ERROR", //$NON-NLS-1$
							e.getLocalizedMessage()),
					e);
			return;
		}
	}
	/** Replies the command line options supported by this boot class.
	 *
	 * @return the command line options.
	 */
	public static Options getOptions() {
		Option opt;
		Options options = new Options();

		options.addOption("B", "bootid", false, //$NON-NLS-1$//$NON-NLS-2$
				Locale.getString("CLI_HELP_B",  //$NON-NLS-1$
						JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME,
						JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME));

		options.addOption("f", "file", true,  //$NON-NLS-1$//$NON-NLS-2$
				Locale.getString("CLI_HELP_F"));  //$NON-NLS-1$

		options.addOption("h", "help", false, //$NON-NLS-1$//$NON-NLS-2$
				Locale.getString("CLI_HELP_H"));  //$NON-NLS-1$

		options.addOption("nologo", false,  //$NON-NLS-1$
				Locale.getString("CLI_HELP_NOLOGO"));  //$NON-NLS-1$

		options.addOption("o", "offline", false,  //$NON-NLS-1$//$NON-NLS-2$
				Locale.getString("CLI_HELP_O", JanusConfig.OFFLINE));  //$NON-NLS-1$

		options.addOption("q", "quiet", false, //$NON-NLS-1$//$NON-NLS-2$
				Locale.getString("CLI_HELP_Q"));  //$NON-NLS-1$

		options.addOption("R", "randomid", false, //$NON-NLS-1$//$NON-NLS-2$
				Locale.getString("CLI_HELP_R",  //$NON-NLS-1$
						JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME,
						JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME));

		options.addOption("s", "showdefaults", false, //$NON-NLS-1$//$NON-NLS-2$
				Locale.getString("CLI_HELP_S"));  //$NON-NLS-1$

		options.addOption("v", "verbose", false, //$NON-NLS-1$//$NON-NLS-2$
				Locale.getString("CLI_HELP_V")); //$NON-NLS-1$

		options.addOption("W", "worldid", false, //$NON-NLS-1$//$NON-NLS-2$
				Locale.getString("CLI_HELP_W", //$NON-NLS-1$
						JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME,
						JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME));
		StringBuilder b = new StringBuilder();
		int l = 0;
		for (String logLevel : LoggerCreator.getLevelStrings()) {
			if (b.length() > 0) {
				b.append(", "); //$NON-NLS-1$
			}
			b.append(logLevel);
			b.append(" ("); //$NON-NLS-1$
			b.append(l);
			b.append(")"); //$NON-NLS-1$
			++l;
		}
		opt = new Option("l", "log", true, Locale.getString("CLI_HELP_L",  //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
				JanusConfig.VERBOSE_LEVEL_VALUE, b));
		opt.setArgs(1);
		options.addOption(opt);
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
		formatter.printHelp(
				Boot.class.getName()
				+ " " //$NON-NLS-1$
				+ Locale.getString(Boot.class, "CLI_PARAM_SYNOPTIC"), //$NON-NLS-1$
				getOptions());
		System.exit(ERROR_EXIT_CODE);
	}

	/** Show the default values of the system properties.
	 * This function never returns.
	 */
	public static void showDefaults() {
		Properties defaultValues = new Properties();
		JanusConfig.getDefaultValues(defaultValues);
		NetworkConfig.getDefaultValues(defaultValues);
		try {
			defaultValues.storeToXML(System.out, null);
		} catch (IOException e) {
			//CHECKSTYLE:OFF
			e.printStackTrace();
			//CHECKSTYLE:ON
		}
		System.exit(ERROR_EXIT_CODE);
	}

	/** Show the heading logo of the Janus platform.
	 */
	public static void showJanusLogo() {
		//CHECKSTYLE:OFF
		System.out.println(Locale.getString("JANUS_TEXT_LOGO")); //$NON-NLS-1$
		//CHECKSTYLE:ON
	}

	/** Launch the first agent of the Janus kernel.
	 * <p>
	 * Thus function does not parse the command line.
	 * See {@link #main(String[])} for the command line management.
	 * When this function is called, it is assumed that all the
	 * system's properties are correctly set.
	 *
	 * @param platformModule - type of the injection module to use for initializing the platform.
	 * @param agentCls - type of the first agent to launch.
	 * @param params - parameters to pass to the agent as its initliazation parameters.
	 * @throws Exception - if it is impossible to start the platform.
	 * @see #main(String[])
	 */
	public static void startJanus(
			Class<? extends Module> platformModule,
			Class<? extends Agent> agentCls,
			Object... params) throws Exception {
		assert (platformModule != null) : "No platform injection module"; //$NON-NLS-1$
		Kernel k = Kernel.create(platformModule.newInstance());
		if (LoggerCreator.getLoggingLevelFromProperties().intValue() > 0) {
			//CHECKSTYLE:OFF
			System.out.println(Locale.getString("LAUNCHING_AGENT", agentCls.getName())); //$NON-NLS-1$
			//CHECKSTYLE:ON
		}
		k.spawn(agentCls, params);
	}

}
