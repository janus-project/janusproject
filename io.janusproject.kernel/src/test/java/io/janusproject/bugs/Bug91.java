/*
 * $Id$
 *
 * Janus platform is an open-source multiagent platform.
 * More details on http://www.janusproject.io
 *
 * Copyright (C) 2014-2015 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND.
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

package io.janusproject.bugs;

import static org.junit.Assert.assertNotNull;

import java.util.UUID;

import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.janusproject.Boot;
import io.janusproject.testutils.AbstractJanusRunTest;
import org.junit.Before;
import org.junit.Test;

import io.sarl.lang.core.Agent;
import io.sarl.lang.core.BuiltinCapacitiesProvider;

/** Unit test for the issue #91: Stop agent on initialization failure?
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @see https://github.com/janus-project/janusproject/issues/91
 */
@SuppressWarnings("all")
public class Bug91 extends AbstractJanusRunTest {

	@Inject
	private SubscriberExceptionHandler uncaughtEventBusExceptionHandler;
	
	@Before
	public void setUp() {
		Boot.setOffline(true);
	}
	
	@Test
	public void ExceptionInInit() throws Exception {
		runJanus(ExceptionInInitAgent.class);
		assertNumberOfResults(1);
		Exception ex = getResult(Exception.class, 0);
		assertNotNull(ex);
		assertCause(TestException.class, ex);
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public static class ExceptionInInitAgent extends TestingAgent {

		@Inject
		public ExceptionInInitAgent(
				BuiltinCapacitiesProvider provider,
				@Named(Agent.PARENT_ID_KEY_NAME) UUID parentID,
				@Named(Agent.AGENT_ID_KEY_NAME) UUID agentID) {
			super(provider, parentID, agentID);
		}

		@Override
		protected boolean runAgentTest() {
			throw new TestException();
		}

	}
	
	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class TestException extends RuntimeException {
		
		public TestException() {
			super("Error in the agent");
		}
		
	}

}
