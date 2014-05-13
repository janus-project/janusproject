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
import io.janusproject.util.ListenerCollection;

import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;
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

	private ListenerCollection<TaskListener> listeners = null;

	/**
	 * @param factory
	 */
	@Inject
	public JanusThreadPoolExecutor(ThreadFactory factory) {
        this(	JanusConfig.getSystemPropertyAsInteger(
					JanusConfig.MAX_NUMBER_OF_THREADS_IN_EXECUTOR_NAME,
					JanusConfig.MAX_NUMBER_OF_THREADS_IN_EXECUTOR_VALUE),
				factory);
	}
	
	/**
	 * @param poolSize - maximal number of threads in the pool.
	 * @param factory - thread factory.
	 */
	@Inject
	public JanusThreadPoolExecutor(int poolSize, ThreadFactory factory) {
        super(	poolSize,
				Integer.MAX_VALUE,
        		60L, TimeUnit.SECONDS,
        		new SynchronousQueue<Runnable>(),
        		factory);
	}

	/** Add a listener on tasks.
	 * 
	 * @param listener
	 */
	public synchronized void addTaskListener(TaskListener listener) {
		if (this.listeners==null) {
			this.listeners = new ListenerCollection<>();
		}
		this.listeners.add(TaskListener.class, listener);
	}
	
	/** Remove a listener on tasks.
	 * 
	 * @param listener
	 */
	public synchronized void removeTaskListener(TaskListener listener) {
		if (this.listeners!=null) {
			this.listeners.remove(TaskListener.class, listener);
			if (this.listeners.isEmpty()) {
				this.listeners = null;
			}
		}
	}
	
	/** Notify the listeners about a task termination.
	 * 
	 * @param thread
	 * @param task
	 */
	protected void fireTaskFinished(Thread thread, Runnable task) {
		TaskListener[] listeners;
		synchronized(this) {
			if (this.listeners==null) return;
			listeners = this.listeners.getListeners(TaskListener.class);
		}
		for(TaskListener listener : listeners) {
			listener.taskFinished(thread, task);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
		// This function is invoked when the task was submited
		return new JanusFutureTask<>(callable);
	}
	
	/** {@inheritDoc}
	 */
	@Override
	protected <T> RunnableFuture<T> newTaskFor(Runnable runnable, T value) {
		// This function is invoked when the task was submited
		return new JanusFutureTask<>(runnable, value);
	}
	
	/** {@inheritDoc}
	 */
	@Override
	protected void beforeExecute(Thread t, Runnable r) {
		// Was the task submitted (if future task) or executed?
		if (r instanceof JanusFutureTask<?>) {
			((JanusFutureTask<?>)r).setThread(t);
		}
	}
	
	/** {@inheritDoc}
	 */
	@Override
	protected void afterExecute(Runnable r, Throwable t) {
		Thread th;
		JanusFutureTask<?> task;
		if (r instanceof JanusFutureTask<?>) {
			task = (JanusFutureTask<?>)r;
			th = task.getThread(); 
		}
		else {
			task = null;
			th = Thread.currentThread();
		}
		if (t!=null && task!=null) {
			// Was the task submitted (if future task) or executed?
			ExecutorUtil.log(th, t);
		}
		fireTaskFinished(th, r);
	}

}