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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ExecutionException;


/**
 * Utilities for executors.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
final class JdkExecutorUtil {

	private JdkExecutorUtil() {
		//
	}

	/** Log the exception.
	 *
	 * @param thread - the thread in which the exception occurs.
	 * @param exception - the exception to log, or <code>null</code> if none.
	 * @return <code>true</code> if ChuckNorrisException is detected.
	 */
	public static boolean log(Thread thread, Throwable exception) {
		if (exception != null) {
			Throwable e = exception;
			// Get the cause of the exception
			while (e instanceof ExecutionException) {
				e = ((ExecutionException) e).getCause();
			}
			if (!(e instanceof ChuckNorrisException)) {
				// Call the exception catcher
				UncaughtExceptionHandler h = thread.getUncaughtExceptionHandler();
				if (h == null) {
					h = Thread.getDefaultUncaughtExceptionHandler();
				}
				if (h != null) {
					h.uncaughtException(thread, e);
				} else {
					//CHECKSTYLE:OFF
					System.err.println(e.toString());
					e.printStackTrace();
					//CHECKSTYLE:ON
				}
			} else {
				return true;
			}
		}
		return false;
	}

}
