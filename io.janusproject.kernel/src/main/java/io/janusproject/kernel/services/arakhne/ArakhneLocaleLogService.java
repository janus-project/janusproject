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
package io.janusproject.kernel.services.arakhne;

import io.janusproject.services.AbstractDependentService;
import io.janusproject.services.logging.LogService;

import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.arakhne.afc.vmutil.locale.Locale;

import com.google.common.util.concurrent.Service;
import com.google.inject.Inject;

/** This class enables to log information by ensuring
 * that the values of the parameters are not evaluated
 * until the information should be really log, according
 * to the log level.
 * This implementation is based on {@link Locale}, and the logger is injected.
 * <p>
 * The LogService considers the parameters of the functions as:<ul>
 * <li>the messageKey is the name of the message in the property file;</li>
 * <li>the message parameters are the values that will replace the
 * strings {0}, {1}, {2}... in the text extracted from the ressource property;</li>
 * <li>the parameter propertyType is the class from which the filename of
 * the property file will be built.</li>
 * </ul>
 * <p>
 * If a <code>Throwable</code> is passed as parameter, the text of the
 * exception is retreived.
 * <p>
 * If a <code>Callable</code> is passed as parameter, the object is automatically
 * called.
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
public class ArakhneLocaleLogService extends AbstractDependentService implements LogService {

	private Logger logger;

	/**
	 */
	public ArakhneLocaleLogService() {
		//
	}

	@Override
	public final Class<? extends Service> getServiceType() {
		return LogService.class;
	}

	private static StackTraceElement getCaller() {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		Class<?> type;
		// Start at 1 because the top of the stack corresponds to getStackTrace.
		for (int i = 1; i < stackTrace.length; ++i) {
			try {
				type = Class.forName(stackTrace[i].getClassName());
				if (type != null && !LogService.class.isAssignableFrom(type)) {
					return stackTrace[i];
				}
			} catch (ClassNotFoundException e) {
				//
			}
		}
		return null;
	}

	/** Replies if this service permits to log the messages.
	 *
	 * @return <code>true</code> if the messages are loggeable,
	 * <code>false</code> otherwise.
	 */
	protected boolean isLogEnabled() {
		return state().ordinal() <= State.RUNNING.ordinal();
	}

	private synchronized void log(Level level, boolean exception, String messageKey, Object... message) {
		if (isLogEnabled() && this.logger.isLoggable(level)) {
			StackTraceElement elt = getCaller();
			assert (elt != null);
			Class<?> callerType;
			try {
				callerType = Class.forName(elt.getClassName());
			} catch (ClassNotFoundException e1) {
				throw new Error(e1);
			}
			log(level, exception, callerType, messageKey, message);
		}
	}

	private synchronized void log(
			Level level,
			boolean exception,
			Class<?> propertyType,
			String messageKey,
			Object... message) {
		if (isLogEnabled() && this.logger.isLoggable(level)) {
			StackTraceElement elt = getCaller();
			assert (elt != null);
			String text = Locale.getString(propertyType, messageKey, message);
			Throwable e = null;
			if (exception) {
				for (Object m : message) {
					if (m instanceof Throwable) {
						e = (Throwable) m;
						break;
					}
				}
			}
			LogRecord record = new LogRecord(level, text);
			if (e != null) {
				record.setThrown(e);
			}
			record.setSourceClassName(elt.getClassName());
			record.setSourceMethodName(elt.getMethodName());
			this.logger.log(record);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized void log(LogRecord record) {
		if (isLogEnabled()) {
			this.logger.log(record);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public void log(Level level, Class<?> propertyType, String messageKey,
			Object... message) {
		log(level, true, propertyType, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void log(Level level, String messageKey, Object... message) {
		log(level, true, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void info(String messageKey, Object... message) {
		log(Level.INFO, false, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void info(Class<?> propertyType, String messageKey,
			Object... message) {
		log(Level.INFO, false, propertyType, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void fineInfo(String messageKey, Object... message) {
		log(Level.FINE, false, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void fineInfo(Class<?> propertyType, String messageKey,
			Object... message) {
		log(Level.FINE, false, propertyType, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void finerInfo(String messageKey, Object... message) {
		log(Level.FINER, false, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void finerInfo(Class<?> propertyType, String messageKey,
			Object... message) {
		log(Level.FINER, false, propertyType, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void debug(String messageKey, Object... message) {
		log(Level.FINEST, true, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void debug(Class<?> propertyType, String messageKey,
			Object... message) {
		log(Level.FINEST, true, propertyType, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void warning(Class<?> propertyType, String messageKey,
			Object... message) {
		log(Level.WARNING, true, propertyType, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void warning(String messageKey, Object... message) {
		log(Level.WARNING, true, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void error(String messageKey, Object... message) {
		log(Level.SEVERE, true, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void error(Class<?> propertyType, String messageKey,
			Object... message) {
		log(Level.SEVERE, true, propertyType, messageKey, message);
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized Logger getLogger() {
		return this.logger;
	}

	/** {@inheritDoc}
	 */
	@Inject
	@Override
	public synchronized void setLogger(Logger logger) {
		if (logger != null) {
			this.logger = logger;
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized void setFilter(Filter filter) {
		this.logger.setFilter(filter);
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized Filter getFilter() {
		return this.logger.getFilter();
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean isLoggeable(Level level) {
		return isLogEnabled() && this.logger.isLoggable(level);
	}

	/** {@inheritDoc}
	 */
	@Override
	public Level getLevel() {
		return this.logger.getLevel();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void setLevel(Level level) {
		this.logger.setLevel(level);
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void doStart() {
		notifyStarted();
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void doStop() {
		notifyStopped();
	}

}
