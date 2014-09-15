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

import io.janusproject.kernel.services.jdk.executors.JdkTaskListener;
import io.janusproject.kernel.services.jdk.executors.JdkThreadFactory;
import io.janusproject.kernel.services.jdk.executors.JdkThreadPoolExecutor;
import io.janusproject.services.executor.ChuckNorrisException;
import io.janusproject.testutils.FutureExceptionMatcher;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.exceptions.base.MockitoException;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc"})
public class JdkThreadPoolExecutorTest extends Assert {

	static final Object VALUE = new Object();

	private TerminationListener termListener;
	private JdkThreadPoolExecutor executor;
	private UncaughtExceptionHandler handler;

	@Before
	public void setUp() {
		this.termListener = new TerminationListener();
		this.handler = Mockito.mock(UncaughtExceptionHandler.class);
		this.executor = new JdkThreadPoolExecutor(new JdkThreadFactory(this.handler));
		this.executor.addTaskListener(this.termListener);
	}

	@After
	public void tearDown() {
		this.executor.shutdownNow();
		this.executor.removeTaskListener(this.termListener);
		this.handler = null;
		this.executor = null;
		this.termListener = null;
	}

	@Test
	public void submitRunnable_succeed() throws Exception {
		Runnable mock = new RunnableMock(0);
		Future<?> f = this.executor.submit(mock);
		waitTaskTermination();
		assertNotNull(f);
		assertNull(f.get());
		Mockito.verifyZeroInteractions(this.handler);
	}

	@Test
	public void submitRunnableObject_succeed() throws Exception {
		Runnable mock = new RunnableMock(0);
		Future<?> f = this.executor.submit(mock, VALUE);
		waitTaskTermination();
		assertNotNull(f);
		assertSame(VALUE, f.get());
		Mockito.verifyZeroInteractions(this.handler);
	}

	@Test
	public void submitCallable_succeed() throws Exception {
		Callable<?> mock = new RunnableMock(0);
		Future<?> f = this.executor.submit(mock);
		waitTaskTermination();
		assertNotNull(f);
		assertSame(VALUE, f.get());
		Mockito.verifyZeroInteractions(this.handler);
	}

	@Test
	public void executeRunnable_succeed() throws Exception {
		Runnable mock = new RunnableMock(0);
		this.executor.execute(mock);
		waitTaskTermination();
		Mockito.verifyZeroInteractions(this.handler);
	}

	@Test
	public void submitRunnable_chucknorris() throws Exception {
		Runnable mock = new RunnableMock(2);
		Future<?> f = this.executor.submit(mock);
		waitTaskTermination();
		assertNotNull(f);
		assertNull(f.get());
		Mockito.verifyZeroInteractions(this.handler);
	}

	@Test
	public void submitCallable_chucknorris() throws Exception {
		Callable<?> mock = new RunnableMock(2);
		Future<?> f = this.executor.submit(mock);
		waitTaskTermination();
		assertNotNull(f);
		assertNull(f.get());
		Mockito.verifyZeroInteractions(this.handler);
	}

	@Test
	public void executeRunnable_chucknorris() throws Exception {
		Runnable mock = new RunnableMock(2);
		this.executor.execute(mock);
		waitTaskTermination();

		ArgumentCaptor<Thread> argument1 = ArgumentCaptor.forClass(Thread.class);
		ArgumentCaptor<Throwable> argument2 = ArgumentCaptor.forClass(Throwable.class);
		Mockito.verify(this.handler).uncaughtException(argument1.capture(), argument2.capture());
		assertTrue(argument2.getValue() instanceof ChuckNorrisException);
	}

	@Test
	public void submitRunnable_exception() throws Exception {
		Runnable mock = new RunnableMock(1);
		Future<?> f = this.executor.submit(mock);
		waitTaskTermination();

		assertNotNull(f);
		assertThat(f, new FutureExceptionMatcher(MockitoException.class));

		ArgumentCaptor<Thread> argument1 = ArgumentCaptor.forClass(Thread.class);
		ArgumentCaptor<Throwable> argument2 = ArgumentCaptor.forClass(Throwable.class);
		Mockito.verify(this.handler).uncaughtException(argument1.capture(), argument2.capture());
		assertTrue(argument2.getValue() instanceof MockitoException);
	}

	@Test
	public void submitCallable_exception() throws Exception {
		Callable<?> mock = new RunnableMock(1);
		Future<?> f = this.executor.submit(mock);
		waitTaskTermination();

		assertNotNull(f);
		assertThat(f, new FutureExceptionMatcher(MockitoException.class));

		ArgumentCaptor<Thread> argument1 = ArgumentCaptor.forClass(Thread.class);
		ArgumentCaptor<Throwable> argument2 = ArgumentCaptor.forClass(Throwable.class);
		Mockito.verify(this.handler).uncaughtException(argument1.capture(), argument2.capture());
		assertTrue(argument2.getValue() instanceof MockitoException);
	}

	@Test
	public void executeRunnable_exception() throws Exception {
		RunnableMock mock = new RunnableMock(1);
		this.executor.execute(mock);
		waitTaskTermination();

		ArgumentCaptor<Thread> argument1 = ArgumentCaptor.forClass(Thread.class);
		ArgumentCaptor<Throwable> argument2 = ArgumentCaptor.forClass(Throwable.class);
		Mockito.verify(this.handler).uncaughtException(argument1.capture(), argument2.capture());
		assertTrue(argument2.getValue() instanceof MockitoException);
	}

	private void waitTaskTermination() throws Exception {
		this.termListener.waitForTermination();
		this.executor.shutdown();
		this.executor.awaitTermination(30, TimeUnit.SECONDS);
		assertEquals(0, this.executor.getActiveCount());
		Thread.sleep(250);
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class RunnableMock implements Runnable, Callable<Object> {

		private final int state;

		/**
		 * @param state
		 */
		public RunnableMock(int state) {
			this.state = state;
		}

		/** {@inheritDoc}
		 */
		@Override
		public void run() {
			switch(this.state) {
			case 1:
				throw new MockitoException(""); //$NON-NLS-1$
			case 2:
				throw new ChuckNorrisException();
			default:
			}
		}

		/** {@inheritDoc}
		 */
		@Override
		public Object call() {
			switch(this.state) {
			case 1:
				throw new MockitoException(""); //$NON-NLS-1$
			case 2:
				throw new ChuckNorrisException();
			default:
			}
			return VALUE;
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class TerminationListener implements JdkTaskListener {

		private final AtomicBoolean finished = new AtomicBoolean(false);

		/**
		 */
		public TerminationListener() {
			//
		}

		/** {@inheritDoc}
		 */
		@Override
		public void taskFinished(Thread thread, Runnable task) {
			this.finished.set(true);
		}

		public void waitForTermination() {
			while (!this.finished.get()) {
				Thread.yield();
			}
		}

	}

}
