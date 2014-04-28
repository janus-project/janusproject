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

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.inject.Inject;

/**
 * Executor that support uncaucht exceptions and interruptable threads.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class JanusThreadPoolExecutor extends ThreadPoolExecutor {

	/**
	 * @param factory
	 */
	@Inject
	public JanusThreadPoolExecutor(ThreadFactory factory) {
        super(	0, Integer.MAX_VALUE,
        		60L, TimeUnit.SECONDS,
        		new SynchronousQueue<Runnable>(),
        		factory);
	}
	
	/** {@inheritDoc}
	 */
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		if (t!=null && (!(t instanceof ChuckNorrisException))) {
			UncaughtExceptionHandler h = Thread.currentThread().getUncaughtExceptionHandler();
			if (h==null) h = Thread.getDefaultUncaughtExceptionHandler();
			if (h!=null) h.uncaughtException(Thread.currentThread(), t);
			else {
				System.err.println(t.toString());
				t.printStackTrace();
			}
		}
	}

}