/* 
 * $Id$
 * 
 * Copyright (C) 2010-2012 Janus Core Developers
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.janusproject.util;

import io.janusproject.JanusConfig;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper for creating a logger.
 * 
 * @author $Author: galland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class LoggerCreator {

	private static Level levelFromProperties = null;
	
	/** Create a logger with the given name.
	 * <p>
	 * The level of logging is influence by {@link JanusConfig#VERBOSE_LEVEL_NAME}.
	 * 
	 * @param name
	 * @return the logger.
	 */
	public static Logger createLogger(String name) {
		Logger logger = Logger.getLogger(name);
		logger.setLevel(getLoggingLevelFromProperties());
		return logger;
	}
	
	/** Extract the logging level from the system properties.
	 * 
	 * @return the logging level.
	 */
	public static Level getLoggingLevelFromProperties() {
		if (levelFromProperties==null) {
			String verboseLevel = JanusConfig.getSystemProperty(JanusConfig.VERBOSE_LEVEL_NAME, JanusConfig.VERBOSE_LEVEL_VALUE);
			levelFromProperties = parseLoggingLevel(verboseLevel);
		}
		return levelFromProperties;
	}

	/** Extract the logging level from the given string.
	 * 
	 * @param level
	 * @return the logging level.
	 */
	public static Level parseLoggingLevel(String level) {
		if (level==null) return Level.INFO;
		switch(level.toLowerCase()) {
		case "none": //$NON-NLS-1$
		case "false": //$NON-NLS-1$
		case "0": //$NON-NLS-1$
			return Level.OFF;
		case "severe": //$NON-NLS-1$
		case "error": //$NON-NLS-1$
		case "1": //$NON-NLS-1$
			return Level.SEVERE;
		case "warn": //$NON-NLS-1$
		case "warning": //$NON-NLS-1$
		case "2": //$NON-NLS-1$
			return Level.WARNING;
		case "info": //$NON-NLS-1$
		case "true": //$NON-NLS-1$
		case "3": //$NON-NLS-1$
			return Level.INFO;
		case "fine": //$NON-NLS-1$
		case "config": //$NON-NLS-1$
		case "4": //$NON-NLS-1$
			return Level.FINE;
		case "finer": //$NON-NLS-1$
		case "5": //$NON-NLS-1$
			return Level.FINER;
		case "finest": //$NON-NLS-1$
		case "debug": //$NON-NLS-1$
		case "6": //$NON-NLS-1$
			return Level.FINEST;
		case "all": //$NON-NLS-1$
		case "7": //$NON-NLS-1$
			return Level.ALL;
		default:
			try {
				return fromInt(Integer.parseInt(level));
			}
			catch(Throwable _) {
				//
			}
			return Level.INFO;
		}
	}

	/** Convert a numerical representation of logging level to the logging level.
	 * 
	 * @param num - the numerical index that corresponds to the given level.
	 * @return the logging level.
	 */
	public static Level fromInt(int num) {
		switch(num) {
		case 0:
			return Level.OFF;
		case 1:
			return Level.SEVERE;
		case 2:
			return Level.WARNING;
		case 3:
			return Level.INFO;
		case 4:
			return Level.FINE;
		case 5:
			return Level.FINER;
		case 6:
			return Level.FINEST;
		case 7:
			return Level.ALL;
		default:
			if (num<0) return Level.OFF;
			return Level.ALL;
		}
	}

	/** Convert a logging level to its numerical equivalent.
	 * 
	 * @param level
	 * @return the numerical index that corresponds to the given level.
	 */
	public static int toInt(Level level) {
		if (level==Level.OFF) return 0;
		if (level==Level.SEVERE) return 1;
		if (level==Level.WARNING) return 2;
		if (level==Level.INFO) return 3;
		if (level==Level.CONFIG) return 4;
		if (level==Level.FINE) return 4;
		if (level==Level.FINER) return 5;
		if (level==Level.FINEST) return 6;
		if (level==Level.ALL) return 7;
		return 3;
	}

	/** Convert a string representing a logging level into its
	 * numerical representation.
	 * <p>
	 * This is a convinient function that calls
	 * {@link #parseLoggingLevel(String)} and
	 * {@link #toInt(Level)}.
	 * 
	 * @param level
	 * @return the numerical index that corresponds to the given level.
	 */
	public static int toInt(String level) {
		return toInt(parseLoggingLevel(level));
	}
	
	/** Replies the string representations for the logging levels.
	 * 
	 * @return the string representations, indexed by the numerical index of the level.
	 */
	public static String[] getLevelStrings() {
		return new String[] {
				"none", //$NON-NLS-1$
				"error", //$NON-NLS-1$
				"warning", //$NON-NLS-1$
				"info", //$NON-NLS-1$
				"fine", //$NON-NLS-1$
				"finer", //$NON-NLS-1$
				"finest", //$NON-NLS-1$
				"all" //$NON-NLS-1$
		};
	}

}