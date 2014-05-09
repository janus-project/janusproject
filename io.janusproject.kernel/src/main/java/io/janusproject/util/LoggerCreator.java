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

	/** Create a logger with the given name.
	 * <p>
	 * The level of logging is influence by {@link JanusConfig#VERBOSE_LEVEL}.
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
		int verboseLevel = JanusConfig.getSystemPropertyAsInteger(JanusConfig.VERBOSE_LEVEL, JanusConfig.VALUE_VERBOSE_LEVEL);
		switch(verboseLevel) {
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
		default:
			return Level.ALL;
		}
	}

}