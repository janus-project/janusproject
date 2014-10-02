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
package io.janusproject.kernel.services.jdk.executors;

import io.janusproject.services.executor.ChuckNorrisException;
import io.janusproject.services.logging.LogService;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.arakhne.afc.vmutil.locale.Locale;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * A factory of threads for the Janus platform.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
public class JdkUncaughtExceptionHandler implements UncaughtExceptionHandler, SubscriberExceptionHandler {

	private final LogService logger;

	/**
	 * @param logger - the logging service that must be used for output the errors.
	 */
	@Inject
	public JdkUncaughtExceptionHandler(LogService logger) {
		assert (logger != null);
		this.logger = logger;
	}

	private void log(Throwable e, String taskId, String taskName) {
		assert (e != null);
		Throwable cause = e;
		while (cause.getCause() != null && cause.getCause() != cause) {
			cause = cause.getCause();
		}
		LogRecord record;
		if (cause instanceof ChuckNorrisException || e instanceof ChuckNorrisException) {
			// Chuck Norris cannot be catched!
			return;
		}
		if (cause instanceof CancellationException || e instanceof CancellationException) {
			// Avoid too much processing if the error is not loggeable
			if (!this.logger.isLoggeable(Level.FINEST)) {
				return;
			}
			record = new LogRecord(Level.FINEST,
					Locale.getString("CANCEL_TASK", taskId, taskName)); //$NON-NLS-1$
		} else if (cause instanceof InterruptedException || e instanceof InterruptedException) {
			// Avoid too much processing if the error is not loggeable
			if (!this.logger.isLoggeable(Level.FINEST)) {
				return;
			}
			record = new LogRecord(Level.FINEST,
					Locale.getString("INTERRUPT_TASK", taskId, taskName)); //$NON-NLS-1$
		} else {
			// Avoid too much processing if the error is not loggeable
			if (!this.logger.isLoggeable(Level.SEVERE)) {
				return;
			}
			record = new LogRecord(
					Level.SEVERE,
					Locale.getString("UNCAUGHT_EXCEPTION", //$NON-NLS-1$
							cause.getLocalizedMessage(),
							taskId,
							taskName));
		}

		record.setThrown(cause);
		StackTraceElement[] trace = cause.getStackTrace();
		if (trace != null && trace.length > 0) {
			StackTraceElement elt = trace[0];
			assert (elt != null);
			record.setSourceClassName(elt.getClassName());
			record.setSourceMethodName(elt.getMethodName());
		}
		this.logger.log(record);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log(e, Long.toString(t.getId()), t.getName());
	}

	/** {@inheritDoc}
	 */
	@Override
	public void handleException(Throwable exception,
			SubscriberExceptionContext context) {
		log(exception, context.getEventBus().toString(), context.getEvent().toString());
	}

}

