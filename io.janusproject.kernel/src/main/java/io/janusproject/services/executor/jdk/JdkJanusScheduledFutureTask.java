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

import io.janusproject.services.executor.ChuckNorrisException;
import io.janusproject.services.executor.JanusScheduledFutureTask;

import java.lang.ref.WeakReference;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link ScheduledFuture} that is {@link Runnable}. Successful
 * execution of the <tt>run</tt> method causes completion of the
 * <tt>Future</tt> and allows access to its results.
 *
 * @param <V> - type of the values supported by the task.
 * @see FutureTask
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class JdkJanusScheduledFutureTask<V> implements JanusScheduledFutureTask<V> {

	private final AtomicBoolean treated = new AtomicBoolean(false);
	private final RunnableScheduledFuture<V> task;
	private WeakReference<Thread> thread;

	/**
	 * @param task - the JRE task that must be wrapped into the particular Janus implementation.
	 */
	JdkJanusScheduledFutureTask(RunnableScheduledFuture<V> task) {
		this.task = task;
	}

	@Override
	public String toString() {
		return "[ " + this.task + " ] ON [ " //$NON-NLS-1$ //$NON-NLS-2$
				+ getThread() + " ]"; //$NON-NLS-1$
	}

	/** Set the running thread.
	 *
	 * @param thread - thread which is running this task.
	 */
	void setThread(Thread thread) {
		this.thread = new WeakReference<>(thread);
	}

	/** Report the exception if one.
	 *
	 * @param thread - thread for which an exception must be reported.
	 */
	void reportException(Thread thread) {
		try {
			this.task.get();
		} catch (ExecutionException e) {
			Throwable ex = e;
			while (ex instanceof ExecutionException) {
				ex = e.getCause();
			}
			if (!(ex instanceof ChuckNorrisException)
				&& !this.treated.getAndSet(true)) {
				JdkExecutorUtil.log(thread, ex);
			}
		} catch (InterruptedException | CancellationException e) {
			if (!this.treated.getAndSet(true)) {
				JdkExecutorUtil.log(thread, e);
			}
		}
	}

	@Override
	public Thread getThread() {
		return this.thread.get();
	}

	@Override
	public boolean isCurrentThread() {
		return Thread.currentThread() == this.thread.get();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void run() {
		this.task.run();
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
		try {
			return this.task.get();
		} catch (ExecutionException e) {
			Throwable ex = e;
			while (ex instanceof ExecutionException) {
				ex = e.getCause();
			}
			if (ex instanceof ChuckNorrisException) {
				return null;
			}
			throw e;
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException,
			ExecutionException, TimeoutException {
		try {
			return this.task.get(timeout, unit);
		} catch (ExecutionException e) {
			Throwable ex = e;
			while (ex instanceof ExecutionException) {
				ex = e.getCause();
			}
			if (ex instanceof ChuckNorrisException) {
				return null;
			}
			throw e;
		}
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

}
