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
package io.janusproject.services.executor.jdk;

import io.janusproject.JanusConfig;
import io.janusproject.services.executor.ChuckNorrisException;
import io.janusproject.util.ListenerCollection;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
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
public class JdkScheduledThreadPoolExecutor extends ScheduledThreadPoolExecutor {

	private ListenerCollection<JdkTaskListener> listeners;

	/**
	 * @param factory - the thread factory to use for creating new threads.
	 */
	@Inject
	public JdkScheduledThreadPoolExecutor(ThreadFactory factory) {
		super(JanusConfig.getSystemPropertyAsInteger(
				JanusConfig.MAX_NUMBER_OF_THREADS_IN_EXECUTOR_NAME,
				JanusConfig.MAX_NUMBER_OF_THREADS_IN_EXECUTOR_VALUE),
				factory);
	}

	/** Add a listener on tasks.
	 *
	 * @param listener - the listener on task events
	 */
	public synchronized void addTaskListener(JdkTaskListener listener) {
		if (this.listeners == null) {
			this.listeners = new ListenerCollection<>();
		}
		this.listeners.add(JdkTaskListener.class, listener);
	}

	/** Remove a listener on tasks.
	 *
	 * @param listener - the listener on task events
	 */
	public synchronized void removeTaskListener(JdkTaskListener listener) {
		if (this.listeners != null) {
			this.listeners.remove(JdkTaskListener.class, listener);
			if (this.listeners.isEmpty()) {
				this.listeners = null;
			}
		}
	}

	/** Notify the listeners about a task termination.
	 *
	 * @param thread - the thread that was run the finished task.
	 * @param task - the finished task.
	 */
	protected void fireTaskFinished(Thread thread, Runnable task) {
		JdkTaskListener[] listeners;
		synchronized (this) {
			if (this.listeners == null) {
				return;
			}
			listeners = this.listeners.getListeners(JdkTaskListener.class);
		}
		for (JdkTaskListener listener : listeners) {
			listener.taskFinished(thread, task);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable,
			RunnableScheduledFuture<V> task) {
		return new JdkJanusScheduledFutureTask<>(task);
	}

	/** {@inheritDoc}
	 */
	@Override
	protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable,
			RunnableScheduledFuture<V> task) {
		return new JdkJanusScheduledFutureTask<>(task);
	}

	/** {@inheritDoc}
	 */
	@Override
	public <T> Future<T> submit(Runnable task, T result) {
        return schedule(
        		new ResultRunnable<>(task, result),
                0, TimeUnit.NANOSECONDS);
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		// Was the task submitted (if future task) or executed?
		if (r instanceof JdkJanusScheduledFutureTask<?>) {
			((JdkJanusScheduledFutureTask<?>) r).setThread(t);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		assert (t == null);
		assert (r instanceof JdkJanusScheduledFutureTask<?>);
		JdkJanusScheduledFutureTask<?> task = (JdkJanusScheduledFutureTask<?>) r;
		assert (task.isDone() || task.isCancelled() || task.isPeriodic());
		if (task.isDone() || task.isCancelled()) {
			task.reportException(task.getThread());
			fireTaskFinished(task.getThread(), task);
		}
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 * @param <V>
	 */
	private static class ResultRunnable<V> implements Callable<V> {

		private final Runnable runnable;
		private final V result;

		/**
		 * @param runnable
		 * @param result
		 */
		public ResultRunnable(Runnable runnable, V result) {
			this.runnable = runnable;
			this.result = result;
		}

		/** {@inheritDoc}
		 */
		@Override
		public V call() throws Exception {
			try {
				this.runnable.run();
			} catch (ChuckNorrisException _) {
				//
			}
			return this.result;
		}

	}

}
