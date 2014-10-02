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

import io.janusproject.kernel.services.jdk.executors.JdkThreadFactory;

import java.lang.Thread.UncaughtExceptionHandler;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc"})
public class JdkThreadFactoryTest extends Assert {

	private UncaughtExceptionHandler handler;
	private JdkThreadFactory factory;
	
	@Before
	public void setUp() {
		this.handler = Mockito.mock(UncaughtExceptionHandler.class);
		this.factory = new JdkThreadFactory(this.handler);
	}
	
	@After
	public void tearDown() {
		this.factory = null;
		this.handler = null;
	}
	
	@Test
	public void newThread() {
		Thread t = this.factory.newThread(Mockito.mock(Runnable.class));
		assertNotNull(t);
		assertSame(this.handler, t.getUncaughtExceptionHandler());
	}

}
