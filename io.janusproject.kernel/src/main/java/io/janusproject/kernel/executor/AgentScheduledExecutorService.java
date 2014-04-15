/*
 * Copyright 2014 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.janusproject.kernel.executor;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.arakhne.afc.vmutil.locale.Locale;

/**
 * A {@link ScheduledExecutorService} implementation that re-throws Errors and Exceptions encountered on the task execution.
 * <p>
 * Original Code: {@link "http://code.nomad-labs.com/2011/12/09/mother-fk-the-scheduledexecutorservice/"}.
 * 
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class AgentScheduledExecutorService extends ScheduledThreadPoolExecutor {

	/**
	 * @param corePoolSize
	 */
	public AgentScheduledExecutorService(int corePoolSize) {
		super(corePoolSize);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return super.scheduleAtFixedRate(wrapRunnable(command), initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		return super.scheduleWithFixedDelay(wrapRunnable(command), initialDelay, delay, unit);
	}

	private static Runnable wrapRunnable(Runnable command) {
		return new LogOnExceptionRunnable(command);
	}

	private static class LogOnExceptionRunnable implements Runnable {
		private Runnable theRunnable;

		public LogOnExceptionRunnable(Runnable theRunnable) {
			super();
			this.theRunnable = theRunnable;
		}

		@Override
		public void run() {
			try {
				this.theRunnable.run();
			} catch (Throwable t) {
				String message = Locale.getString("SCHEDULED_TASK_ERROR", this.theRunnable); //$NON-NLS-1$
				// LOG IT HERE!!!
				System.err.println(message);
				t.printStackTrace();
				// and re throw it so that the Executor also gets this error so that it can do what it would
				// usually do
				throw new RuntimeException(message, t);
			}
		}
	}

}
