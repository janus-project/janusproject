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
import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

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
		return new FutureWrapper<>(task);
	}
	
	/** {@inheritDoc}
	 */
	@Override
	protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable,
			RunnableScheduledFuture<V> task) {
		return new FutureWrapper<>(task);
	}
	
	/** {@inheritDoc}
	 */
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		((FutureWrapper<?>)r).setThread(t);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		assert(t==null);
		FutureWrapper<?> future = (FutureWrapper<?>)r;
		assert(future.isDone() || future.isCancelled() || future.isPeriodic());
		if (future.isDone() || future.isCancelled()) {
			extractException(future);
		}
	}
	
	private static void extractException(FutureWrapper<?> future) {
		if (!future.consume()) {
			// Test the throw of an exception
			try {
				// This function should not timeout because the task should be terminated.
				future.get(10, TimeUnit.SECONDS);
			}
			catch(Throwable e) {
				// Get the cause of the exception
				while (e instanceof ExecutionException) {
					e = ((ExecutionException)e).getCause();
				}
				if (!(e instanceof ChuckNorrisException)) {
					// Call the exception catcher
					Thread th = future.getThread();
					UncaughtExceptionHandler h = th.getUncaughtExceptionHandler();
					if (h==null) h = Thread.getDefaultUncaughtExceptionHandler();
					if (h!=null) h.uncaughtException(th, e);
					else {
						System.err.println(e.toString());
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	/**
	 * @param <V>
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class FutureWrapper<V> implements JanusScheduledFuture<V> {

		private final AtomicBoolean treated = new AtomicBoolean(false);
		private final RunnableScheduledFuture<V> task;
		private WeakReference<Thread> thread = null;

		/**
		 * @param task
		 */
		public FutureWrapper(RunnableScheduledFuture<V> task) {
			assert(task!=this);
			this.task = task;
		}
		
		/** Replies if this future was already treated before.
		 * 
		 * @return <code>true</code> or <code>false</code>.
		 */
		public boolean consume() {
			return this.treated.getAndSet(true);
		}

		/** Set the running thread.
		 * 
		 * @param thread
		 */
		public void setThread(Thread thread) {
			this.thread = new WeakReference<>(thread);
		}

		@Override
		public Thread getThread() {
			return this.thread.get();
		}
		
		@Override
		public boolean isCurrentThread() {
			return Thread.currentThread()==this.thread.get();
		}

		@Override
		public void run() {
			this.task.run();
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return this.task.cancel(mayInterruptIfRunning);
		}

		@Override
		public boolean isCancelled() {
			return this.task.isCancelled();
		}

		@Override
		public boolean isDone() {
			return this.task.isDone();
		}

		@Override
		public V get() throws InterruptedException, ExecutionException {
			return this.task.get();
		}

		@Override
		public V get(long timeout, TimeUnit unit) throws InterruptedException,
		ExecutionException, TimeoutException {
			return this.task.get(timeout, unit);
		}

		@Override
		public long getDelay(TimeUnit unit) {
			return this.task.getDelay(unit);
		}

		@Override
		public int compareTo(Delayed o) {
			return this.task.compareTo(o);
		}

		@Override
		public boolean isPeriodic() {
			return this.task.isPeriodic();
		}

		@Override
		public boolean equals(Object obj) {
			return this.task.equals(obj);
		}

		@Override
		public int hashCode() {
			return this.task.hashCode();
		}

	}

}