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
import io.janusproject.services.executor.jdk.JdkUncaughtExceptionHandler;
import io.janusproject.services.logging.LogService;
import io.sarl.lang.core.Event;

import java.util.concurrent.CancellationException;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.SubscriberExceptionContext;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc"})
public class JdkUncaughtExceptionHandlerTest extends Assert {

	private LogService logger;
	private JdkUncaughtExceptionHandler handler;
	private SubscriberExceptionContext exceptionContext;
	
	@Before
	public void setUp() {
		this.logger = Mockito.mock(LogService.class);
		Mockito.when(this.logger.isLoggeable(Matchers.any(Level.class))).thenReturn(true);
		
		EventBus eventBus = Mockito.mock(EventBus.class);
		Mockito.when(eventBus.toString()).thenReturn("TESTING_EVENT_BUS"); //$NON-NLS-1$
		Event event = Mockito.mock(Event.class);
		Mockito.when(event.toString()).thenReturn("TESTING_EVENT"); //$NON-NLS-1$
		this.exceptionContext = Mockito.mock(SubscriberExceptionContext.class);
		Mockito.when(this.exceptionContext.getEventBus()).thenReturn(eventBus);
		Mockito.when(this.exceptionContext.getEvent()).thenReturn(event);
				
		this.handler = new JdkUncaughtExceptionHandler(this.logger);
	}
	
	@After
	public void tearDown() {
		this.logger = null;
		this.handler = null;
		this.exceptionContext = null;
	}
	
	@Test
	public void uncaughtException_Exception() {
		Exception e = new Exception();
		this.handler.uncaughtException(Thread.currentThread(), e);
		
		ArgumentCaptor<LogRecord> argument = ArgumentCaptor.forClass(LogRecord.class);
		Mockito.verify(this.logger).log(argument.capture());
		assertSame(Level.SEVERE, argument.getValue().getLevel());
		assertSame(e, argument.getValue().getThrown());
		assertEquals(JdkUncaughtExceptionHandlerTest.class.getName(), argument.getValue().getSourceClassName());
		assertEquals("uncaughtException_Exception", argument.getValue().getSourceMethodName()); //$NON-NLS-1$
	}

	@Test
	public void uncaughtException_ChuckNorris() {
		Exception e = new ChuckNorrisException();
		this.handler.uncaughtException(Thread.currentThread(), e);		
		Mockito.verifyZeroInteractions(this.logger);
	}

	@Test
	public void uncaughtException_Cancellation() {
		Exception e = new CancellationException();
		this.handler.uncaughtException(Thread.currentThread(), e);
		
		ArgumentCaptor<LogRecord> argument = ArgumentCaptor.forClass(LogRecord.class);
		Mockito.verify(this.logger).log(argument.capture());
		assertSame(Level.FINEST, argument.getValue().getLevel());
		assertSame(e, argument.getValue().getThrown());
		assertEquals(JdkUncaughtExceptionHandlerTest.class.getName(), argument.getValue().getSourceClassName());
		assertEquals("uncaughtException_Cancellation", argument.getValue().getSourceMethodName()); //$NON-NLS-1$
	}

	@Test
	public void uncaughtException_Interrupt() {
		Exception e = new InterruptedException();
		this.handler.uncaughtException(Thread.currentThread(), e);
		
		ArgumentCaptor<LogRecord> argument = ArgumentCaptor.forClass(LogRecord.class);
		Mockito.verify(this.logger).log(argument.capture());
		assertSame(Level.FINEST, argument.getValue().getLevel());
		assertSame(e, argument.getValue().getThrown());
		assertEquals(JdkUncaughtExceptionHandlerTest.class.getName(), argument.getValue().getSourceClassName());
		assertEquals("uncaughtException_Interrupt", argument.getValue().getSourceMethodName()); //$NON-NLS-1$
	}

	@Test
	public void handleException_Exception() {
		Exception e = new Exception();
		this.handler.handleException(e, this.exceptionContext);
		
		ArgumentCaptor<LogRecord> argument = ArgumentCaptor.forClass(LogRecord.class);
		Mockito.verify(this.logger).log(argument.capture());
		assertSame(Level.SEVERE, argument.getValue().getLevel());
		assertSame(e, argument.getValue().getThrown());
		assertEquals(JdkUncaughtExceptionHandlerTest.class.getName(), argument.getValue().getSourceClassName());
		assertEquals("handleException_Exception", argument.getValue().getSourceMethodName()); //$NON-NLS-1$
	}

	@Test
	public void handleException_ChuckNorris() {
		Exception e = new ChuckNorrisException();
		this.handler.handleException(e, this.exceptionContext);		
		Mockito.verifyZeroInteractions(this.logger);
	}

	@Test
	public void handleException_Cancellation() {
		Exception e = new CancellationException();
		this.handler.handleException(e, this.exceptionContext);

		ArgumentCaptor<LogRecord> argument = ArgumentCaptor.forClass(LogRecord.class);
		Mockito.verify(this.logger).log(argument.capture());
		assertSame(Level.FINEST, argument.getValue().getLevel());
		assertSame(e, argument.getValue().getThrown());
		assertEquals(JdkUncaughtExceptionHandlerTest.class.getName(), argument.getValue().getSourceClassName());
		assertEquals("handleException_Cancellation", argument.getValue().getSourceMethodName()); //$NON-NLS-1$
	}

	@Test
	public void handleException_Interrupt() {
		Exception e = new InterruptedException();
		this.handler.handleException(e, this.exceptionContext);
		
		ArgumentCaptor<LogRecord> argument = ArgumentCaptor.forClass(LogRecord.class);
		Mockito.verify(this.logger).log(argument.capture());
		assertSame(Level.FINEST, argument.getValue().getLevel());
		assertSame(e, argument.getValue().getThrown());
		assertEquals(JdkUncaughtExceptionHandlerTest.class.getName(), argument.getValue().getSourceClassName());
		assertEquals("handleException_Interrupt", argument.getValue().getSourceMethodName()); //$NON-NLS-1$
	}

}
