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

import io.janusproject.JanusConfig;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.inject.Inject;

/**
 * Executor that support uncaucht exceptions and interruptable threads.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class JanusScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

	/**
	 * @param factory
	 */
	@Inject
	public JanusScheduledThreadPoolExecutor(ThreadFactory factory) {
        super(JanusConfig.VALUE_NUMBER_OF_THREADS_IN_EXECUTOR, factory);
	}

	/** {@inheritDoc}
	 */
	@Override
	protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable,
			RunnableScheduledFuture<V> task) {
		return new FutureWrapper<>(super.decorateTask(callable, task), callable);
	}
	
	/** {@inheritDoc}
	 */
	@Override
	protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable,
			RunnableScheduledFuture<V> task) {
		return new FutureWrapper<>(super.decorateTask(runnable, task), runnable);
	}
	
	/**
	 * @param <V>
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class FutureWrapper<V> implements RunnableScheduledFuture<V> {
		
		private final RunnableScheduledFuture<V> task;
		private final Object nameSource;
		
		/**
		 * @param task
		 * @param nameSource
		 */
		public FutureWrapper(RunnableScheduledFuture<V> task, Object nameSource) {
			assert(task!=this);
			this.task = task;
			this.nameSource = nameSource;
		}
		
		/** {@inheritDoc}
		 */
		@Override
		public void run() {
			// Run the task.
			try {
				this.task.run();
			}
			finally {
				// Test the throw of an exception
				try {
					this.task.get();
				}
				catch(Throwable e) {
					// Get the cause of the exception
					while (e instanceof ExecutionException) {
						e = ((ExecutionException)e).getCause();
					}
						Thread t = Thread.currentThread();
						// Change the thread's name
						String threadName = t.getName();
						t.setName(toString());
						// Call the exception catcher
						UncaughtExceptionHandler h = t.getUncaughtExceptionHandler();
						if (h==null) h = Thread.getDefaultUncaughtExceptionHandler();
						if (h!=null) h.uncaughtException(t, e);
						else {
							System.err.println(e.toString());
							e.printStackTrace();
						}
						// Restore the thread's name
						t.setName(threadName);
					}
				}
			}
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return this.task.cancel(mayInterruptIfRunning);
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean isCancelled() {
			return this.task.isCancelled();
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean isDone() {
			return this.task.isDone();
		}

		/** {@inheritDoc}
		 */
		@Override
		public V get() throws InterruptedException, ExecutionException {
			return this.task.get();
		}

		/** {@inheritDoc}
		 */
		@Override
		public V get(long timeout, TimeUnit unit) throws InterruptedException,
				ExecutionException, TimeoutException {
			return this.task.get(timeout, unit);
		}

		/** {@inheritDoc}
		 */
		@Override
		public long getDelay(TimeUnit unit) {
			return this.task.getDelay(unit);
		}

		/** {@inheritDoc}
		 */
		@Override
		public int compareTo(Delayed o) {
			return this.task.compareTo(o);
		}

		/** {@inheritDoc}
		 */
		@Override
		public boolean isPeriodic() {
			return this.task.isPeriodic();
		}
		
		/** {@inheritDoc}
		 */
		@Override
		public String toString() {
			return this.nameSource != null ? this.nameSource.toString() : super.toString();
		}
		
		/** {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			return this.task.equals(obj);
		}
		
		/** {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return this.task.hashCode();
		}
		
	}

}