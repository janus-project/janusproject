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
package io.janusproject.kernel.executor;

import io.janusproject.services.LogService;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.arakhne.afc.vmutil.locale.Locale;

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
public class JanusUncaughtExceptionHandler implements UncaughtExceptionHandler {

	private final LogService logger;

	/**
	 * @param logger
	 */
	@Inject
	public JanusUncaughtExceptionHandler(LogService logger) {
		this.logger = logger;
	}

	/** {@inheritDoc}
	 */
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		assert(t!=null);
		assert(e!=null);
		LogRecord record = new LogRecord(Level.SEVERE,
				Locale.getString("UNCAUGHT_EXCEPTION", e.getLocalizedMessage(), t.getId(), t.getName())); //$NON-NLS-1$		
		record.setThrown(e);
		StackTraceElement elt = e.getStackTrace()[0];
		assert(elt!=null);
		record.setSourceClassName(elt.getClassName());
		record.setSourceMethodName(elt.getMethodName());
		if (this.logger!=null) {
			this.logger.log(record);
		}
		else {
			Logger.getAnonymousLogger().log(record);
		}
	}

}

