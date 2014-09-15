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

import io.janusproject.JanusConfig;
import io.janusproject.services.AbstractDependentService;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.google.common.util.concurrent.Service;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/** Platform service that supports the execution resources.
 *
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
public class JdkExecutorService extends AbstractDependentService implements io.janusproject.services.executor.ExecutorService {

	@Inject
	private ScheduledExecutorService schedules;

	@Inject
	private ExecutorService exec;

	/**
	 */
	public JdkExecutorService() {
		//
	}

	@Override
	public final Class<? extends Service> getServiceType() {
		return io.janusproject.services.executor.ExecutorService.class;
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void doStart() {
		notifyStarted();
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void doStop() {
		this.exec.shutdown();
		this.schedules.shutdown();
		try {
			int timeout = JanusConfig.getSystemPropertyAsInteger(
					JanusConfig.KERNEL_THREAD_TIMEOUT_NAME,
					JanusConfig.KERNEL_THREAD_TIMEOUT_VALUE);
			this.schedules.awaitTermination(timeout, TimeUnit.SECONDS);
			this.exec.awaitTermination(timeout, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// This error may occur when the thread is killed before this
			// function is waiting for its termination.
		} finally {
			this.schedules.shutdownNow();
			this.exec.shutdownNow();
			notifyStopped();
		}

	}

	/** {@inheritDoc}
	 */
	@Override
	public Future<?> submit(Runnable task) {
		return this.exec.submit(task);
	}

	/** {@inheritDoc}
	 */
	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return this.exec.submit(task, result);
	}

	/** {@inheritDoc}
	 */
	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return this.exec.submit(task);
	}

	/** {@inheritDoc}
	 */
	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay,
			TimeUnit unit) {
		return this.schedules.schedule(command, delay, unit);
	}

	/** {@inheritDoc}
	 */
	@Override
	public <T> ScheduledFuture<T> schedule(Callable<T> command, long delay,
			TimeUnit unit) {
		return this.schedules.schedule(command, delay, unit);
	}

	/** {@inheritDoc}
	 */
	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
			long initialDelay, long period, TimeUnit unit) {
		return this.schedules.scheduleAtFixedRate(command, initialDelay, period, unit);
	}

	/** {@inheritDoc}
	 */
	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
			long initialDelay, long delay, TimeUnit unit) {
		return this.schedules.scheduleWithFixedDelay(command, initialDelay, delay, unit);
	}

	/** {@inheritDoc}
	 */
	@Override
	public ExecutorService getExecutorService() {
		return this.exec;
	}

}
