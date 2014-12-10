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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;

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
			setOffline(true);
		}
		if (cmd.hasOption('R')) {
			setRandomContextUUID();
		} else if (cmd.hasOption('B')) {
			setBootAgentTypeContextUUID();
		} else if (cmd.hasOption('W')) {
			setDefaultContextUUID();
		}
		// Define the system properties, if not already done by the JRE.
		Properties props = cmd.getOptionProperties("D"); //$NON-NLS-1$
		if (props != null) {
			for (Entry<Object, Object> entry : props.entrySet()) {
				setProperty(entry.getKey().toString(), entry.getValue().toString());
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
			setVerboseLevel(verbose);
		}

		// Show the Janus logo?
		if (cmd.hasOption("nologo") || verbose == 0) { //$NON-NLS-1$
			setProperty(
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

			// Load property files
			for (URL url : propertyFiles) {
				setPropertiesFrom(url);
			}

			// Load the agent class
			Class<? extends Agent> agent = loadAgentClass(agentToLaunch);
			assert (agent != null);

			startJanus(
					null,
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

	/** Set offline flag of the Janus platform.
	 *
	 * This function is equivalent to the command line option <code>-o</code>.
	 *
	 * This function must be called before launching the Janus platform.
	 *
	 * @param isOffline - the offline flag.
	 * @since 2.0.2.0
	 * @see JanusConfig#OFFLINE
	 */
	public static void setOffline(boolean isOffline) {
		System.setProperty(JanusConfig.OFFLINE, Boolean.toString(isOffline));
	}

	/** Force the Janus platform to use a random identifier for its default context.
	 *
	 * This function is equivalent to the command line option <code>-R</code>.
	 *
	 * This function must be called before launching the Janus platform.
	 *
	 * @see JanusConfig#BOOT_DEFAULT_CONTEXT_ID_NAME
	 * @see JanusConfig#RANDOM_DEFAULT_CONTEXT_ID_NAME
	 * @since 2.0.2.0
	 */
	public static void setRandomContextUUID() {
		System.setProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME, Boolean.FALSE.toString());
		System.setProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME, Boolean.TRUE.toString());
	}

	/** Force the Janus platform to use a default context identifier that tis build upon the
	 * classname of the boot agent.
	 * It means that the UUID is always the same for a given classname.
	 *
	 * This function is equivalent to the command line option <code>-B</code>.
	 *
	 * @see JanusConfig#BOOT_DEFAULT_CONTEXT_ID_NAME
	 * @see JanusConfig#RANDOM_DEFAULT_CONTEXT_ID_NAME
	 * @since 2.0.2.0
	 */
	public static void setBootAgentTypeContextUUID() {
		System.setProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME, Boolean.TRUE.toString());
		System.setProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME, Boolean.FALSE.toString());
	}

	/** Force the Janus platform to use the identifier hard-coded in the source code for its
	 * default context.
	 *
	 * This function is equivalent to the command line option <code>-W</code>.
	 *
	 * This function must be called before launching the Janus platform.
	 *
	 * @see JanusConfig#BOOT_DEFAULT_CONTEXT_ID_NAME
	 * @see JanusConfig#RANDOM_DEFAULT_CONTEXT_ID_NAME
	 * @since 2.0.2.0
	 */
	public static void setDefaultContextUUID() {
		System.setProperty(JanusConfig.BOOT_DEFAULT_CONTEXT_ID_NAME, Boolean.FALSE.toString());
		System.setProperty(JanusConfig.RANDOM_DEFAULT_CONTEXT_ID_NAME, Boolean.FALSE.toString());
	}

	/** Force the verbosity level.
	 *
	 * This function must be called before launching the Janus platform.
	 *
	 * @param level - the verbosity level.
	 * @see JanusConfig#VERBOSE_LEVEL_NAME
	 * @since 2.0.2.0
	 */
	public static void setVerboseLevel(int level) {
		System.setProperty(JanusConfig.VERBOSE_LEVEL_NAME, Integer.toString(level));
	}

	/** Set the system property.
	 * This function is an helper for setting a system property usually accessible with
	 * {@link System}.
	 *
	 * This function must be called before launching the Janus platform.
	 *
	 * @param name - the name of the property.
	 * @param value - the value of the property.
	 *                If the value is <code>null</code> or empty, the property is removed.
	 * @see System#setProperty(String, String)
	 * @see System#getProperties()
	 * @since 2.0.2.0
	 */
	public static void setProperty(String name, String value) {
		if (name != null && !name.isEmpty()) {
			if (value == null || value.isEmpty()) {
				System.getProperties().remove(name);
			} else {
				System.setProperty(name, value);
			}
		}
	}

	/** Set the system property from the content of the file with the given URL.
	 * This function is an helper for setting the system properties usually accessible with
	 * {@link System}.
	 *
	 * @param propertyFile - the URL from which a stream is opened.
	 * @throws IOException - if the stream cannot be read.
	 * @see System#getProperties()
	 * @see Properties#load(InputStream)
	 * @since 2.0.2.0
	 */
	public static void setPropertiesFrom(URL propertyFile) throws IOException {
		Properties systemProperties = System.getProperties();
		try (InputStream stream = propertyFile.openStream()) {
			systemProperties.load(stream);
		}
	}

	/** Set the system property from the content of the file with the given URL.
	 * This function is an helper for setting the system properties usually accessible with
	 * {@link System}.
	 *
	 * @param propertyFile - the URL from which a stream is opened.
	 * @throws IOException - if the stream cannot be read.
	 * @see System#getProperties()
	 * @see Properties#load(InputStream)
	 * @since 2.0.2.0
	 */
	public static void setPropertiesFrom(File propertyFile) throws IOException {
		Properties systemProperties = System.getProperties();
		try (InputStream stream = new FileInputStream(propertyFile)) {
			systemProperties.load(stream);
		}
	}

	/** Replies the identifier of the boot agent from the system's properties.
	 * The boot agent is launched with {@link #startJanus(Class, Class, Object...)}.
	 *
	 * @return the identifier of the boot agent, or <code>null</code> if it is unknown.
	 * @since 2.0.2.0
	 * @see JanusConfig#BOOT_AGENT_ID
	 * @see #startJanus(Class, Class, Object...)
	 */
	public static UUID getBootAgentIdentifier() {
		String id = JanusConfig.getSystemProperty(JanusConfig.BOOT_AGENT_ID);
		if (id != null && !id.isEmpty()) {
			try {
				return UUID.fromString(id);
			} catch (Throwable _) {
				//
			}
		}
		return null;
	}

	/** Launch the Janus kernel and the first agent in the kernel.
	 *
	 * Thus function does not parse the command line.
	 * See {@link #main(String[])} for the command line management.
	 * When this function is called, it is assumed that all the
	 * system's properties are correctly set.
	 *
	 * The platformModule parameter permits to specify the injection module to use.
	 * The injection module is in change of creating/injecting all the components
	 * of the platform. The default injection module is retreived from the system
	 * property with the name stored in {@link JanusConfig#INJECTION_MODULE_NAME}.
	 * The default type for the injection module is stored in the constant
	 * {@link JanusConfig#INJECTION_MODULE_NAME_VALUE}.
	 *
	 * The function {@link #getBootAgentIdentifier()} permits to retreive the identifier
	 * of the launched agent.
	 *
	 * @param platformModule - type of the injection module to use for initializing the platform,
	 *                         if <code>null</code> the default module will be used.
	 * @param agentCls - type of the first agent to launch.
	 * @param params - parameters to pass to the agent as its initliazation parameters.
	 * @return the kernel that was launched.
	 * @throws Exception - if it is impossible to start the platform.
	 * @see #main(String[])
	 * @see #getBootAgentIdentifier()
	 */
	public static Kernel startJanus(
			Class<? extends Module> platformModule,
			Class<? extends Agent> agentCls,
			Object... params) throws Exception {
		// Set the boot agent classname
		System.setProperty(JanusConfig.BOOT_AGENT, agentCls.getCanonicalName());
		// Get the start-up injection module
		Class<? extends Module> startupModule = platformModule;
		if (startupModule == null) {
			startupModule = JanusConfig.getSystemPropertyAsClass(
					Module.class, JanusConfig.INJECTION_MODULE_NAME,
					JanusConfig.INJECTION_MODULE_NAME_VALUE);
		}
		assert (startupModule != null) : "No platform injection module"; //$NON-NLS-1$
		Kernel k = Kernel.create(startupModule.newInstance());
		if (LoggerCreator.getLoggingLevelFromProperties().intValue() <= Level.INFO.intValue()) {
			//CHECKSTYLE:OFF
			System.out.println(Locale.getString("LAUNCHING_AGENT", agentCls.getName())); //$NON-NLS-1$
			//CHECKSTYLE:ON
		}
		UUID id = k.spawn(agentCls, params);
		if (id != null) {
			System.setProperty(JanusConfig.BOOT_AGENT_ID, id.toString());
		} else {
			System.getProperties().remove(JanusConfig.BOOT_AGENT_ID);
		}
		return k;
	}

}
