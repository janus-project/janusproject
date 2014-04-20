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
package io.janusproject2.services;

import java.util.logging.Logger;

import com.google.common.util.concurrent.Service;

/** This class enables to log information by ensuring
 * that the values of the parameters are not evaluated
 * until the information should be really log, according
 * to the log level.
 * <p>
 * The LogService considers the parameters of the functions as:<ul>
 * <li>the <var>messageKey</var> is the name of the message in the property file;</li>
 * <li>the <var>message</var> parameters are the values that will replace the
 * strings {0}, {1}, {2}... in the text extracted from the ressource property;</li>
 * <li>the parameter <var>propertyType</var> is the class from which the filename of
 * the property file will be built.</li>
 * </ul>
 * <p>
 * If a <code>Throwable</code> is passed as parameter, the text of the
 * exception is retreived.
 * <p>
 * If a <code>LogParam</code> is passed as parameter, the <code>toString</code>
 * function will be invoked.
 * <p>
 * For all the other objects, the {@link #toString()} function is invoked.
 * 
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public interface LogService extends Service {

	/** Log an information message.
	 * 
	 * @param messageKey - key of the message in the properties.
	 * @param message
	 */
	public void info(String messageKey, Object... message);

	/** Log an information message.
	 * 
	 * @param propertyType - type that is used to retreive the property file.
	 * @param messageKey - key of the message in the properties.
	 * @param message
	 */
	public void info(Class<?> propertyType, String messageKey, Object... message);

	/** Log a debug message.
	 * 
	 * @param messageKey - key of the message in the properties.
	 * @param message
	 */
	public void debug(String messageKey, Object... message);

	/** Log a debug message.
	 * 
	 * @param propertyType - type that is used to retreive the property file.
	 * @param messageKey - key of the message in the properties.
	 * @param message
	 */
	public void debug(Class<?> propertyType, String messageKey, Object... message);

	/** Log a warning message.
	 * 
	 * @param propertyType - type that is used to retreive the property file.
	 * @param messageKey - key of the message in the properties.
	 * @param message
	 */
	public void warning(Class<?> propertyType, String messageKey, Object... message);

	/** Log a warning message.
	 * 
	 * @param messageKey - key of the message in the properties.
	 * @param message
	 */
	public void warning(String messageKey, Object... message);

	/** Log an error message.
	 * 
	 * @param messageKey - key of the message in the properties.
	 * @param message
	 */
	public void error(String messageKey, Object... message);

	/** Log an error message.
	 * 
	 * @param propertyType - type that is used to retreive the property file.
	 * @param messageKey - key of the message in the properties.
	 * @param message
	 */
	public void error(Class<?> propertyType, String messageKey, Object... message);

	/** Replies the logger.
	 * 
	 * @return the logger.
	 */
	public Logger getLogger();
	
	/** Change the logger.
	 * 
	 * @param logger
	 */
	public void setLogger(Logger logger);

	/** Utility to put objec that us asynchronously evaluated by
	 * the {@link LogService}.
	 * 
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 * @see LogService
	 */
	public interface LogParam {
		
		@Override
		public abstract String toString();
		
	}

}
