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
package io.janusproject.modules.hazelcast;

import io.janusproject.services.logging.LogService;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.hazelcast.logging.AbstractLogger;
import com.hazelcast.logging.ILogger;
import com.hazelcast.logging.LogEvent;
import com.hazelcast.logging.LoggerFactorySupport;

/** Factory of logger for the Hazelcast API.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class HazelcastKernelLoggerFactory extends LoggerFactorySupport {

	private static LogService logger;

	/**
	 */
	public HazelcastKernelLoggerFactory() {
		//
	}

	/** Replies the log service.
	 *
	 * @return the log service.
	 */
	static LogService getLogService() {
		synchronized (HazelcastKernelLoggerFactory.class) {
			return logger;
		}
	}

	/** Set the log service.
	 *
	 * @param service - the log service.
	 */
	static void setLogService(LogService service) {
		synchronized (HazelcastKernelLoggerFactory.class) {
			logger = service;
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	protected ILogger createLogger(String name) {
		return new HzKernelLogger();
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static final class HzKernelLogger extends AbstractLogger {

        /**
         */
        public HzKernelLogger() {
        	//
        }

		/** {@inheritDoc}
		 */
		@Override
		public void log(Level level, String message) {
			LogService serv = getLogService();
			if (serv != null) {
				serv.log(new LogRecord(level, message));
			}
		}

		/** {@inheritDoc}
		 */
		@Override
		public void log(Level level, String message, Throwable thrown) {
			LogService serv = getLogService();
			if (serv != null && serv.isLoggeable(level)) {
				StackTraceElement elt = getCaller();
				assert (elt != null);
				LogRecord record = new LogRecord(level, message);
				if (thrown != null) {
					record.setThrown(thrown);
				}
				record.setSourceClassName(elt.getClassName());
				record.setSourceMethodName(elt.getMethodName());
				serv.log(record);
			}
		}

		/** {@inheritDoc}
		 */
		@Override
		public void log(LogEvent logEvent) {
			LogService serv = getLogService();
			if (serv != null) {
				serv.log(logEvent.getLogRecord());
			}
		}

		/** {@inheritDoc}
		 */
		@Override
		public Level getLevel() {
			LogService serv = getLogService();
			if (serv != null) {
				return serv.getLevel();
			}
			return Level.OFF;
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean isLoggable(Level level) {
			LogService serv = getLogService();
			if (serv != null) {
				return serv.isLoggeable(level);
			}
			return false;
		}

		private static StackTraceElement getCaller() {
			StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
			Class<?> type;
			// Start at 1 because the top of the stack corresponds to getStackTrace.
			for (int i = 1; i < stackTrace.length; ++i) {
				try {
					type = Class.forName(stackTrace[i].getClassName());
					if (type != null && !ILogger.class.isAssignableFrom(type)) {
						return stackTrace[i];
					}
				} catch (ClassNotFoundException e) {
					//
				}
			}
			return null;
		}

    }

}
