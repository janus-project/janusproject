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
package io.janusproject.testutils;

import static org.junit.Assert.assertNull;
import io.janusproject.Boot;
import io.janusproject.Boot.Exiter;
import io.janusproject.kernel.Kernel;
import io.janusproject.modules.StandardJanusPlatformModule;
import io.janusproject.services.executor.ChuckNorrisException;
import io.sarl.core.Initialize;
import io.sarl.core.Lifecycle;
import io.sarl.core.Schedules;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Percept;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.google.inject.util.Modules;

/** Abstract class for creating unit tests that needs
 * to launch a Janus instance.
 *
 * @param <S> - the type of the service.
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public abstract class AbstractJanusRunTest extends AbstractJanusTest {

	/** Reference to the instance of the Janus kernel.
	 */
	protected Kernel janusKernel;
	
	@Nullable
	private List<Object> results;

	@Rule
	public TestWatcher janusRunWatcher = new TestWatcher() {
		@Override
		protected void starting(Description description) {
			JanusRun skipRun = description.getAnnotation(JanusRun.class);
			if (skipRun != null) {
				try {
					runJanus(skipRun.value());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}

		@Override
		protected void finished(Description description) {
			if (AbstractJanusRunTest.this.janusKernel != null) {
				AbstractJanusRunTest.this.janusKernel = null;
			}
		}
	};
	
	/** Replies result at the given indx of the run of the agent.
	 * 
	 * @param type - the type of the result.
	 * @param index - the index of the result.
	 * @return the value; or <code>null</code> if no result.
	 */
	protected <T> T getResult(Class<T> type, int index) {
		if (this.results != null) {
			try {
				return type.cast(this.results.get(index));
			} catch (Throwable _) {
				//
			}
		}
		return null;
	}

	/** Start the Janus platform.
T	 * 
	 * @param type - the type of the agent to launch at start-up.
	 * @throws Exception - if the kernel cannot be launched.
	 */
	protected void runJanus(Class<? extends TestingAgent> type) throws Exception {
		assertNull("Janus already launched.", this.janusKernel);
		Boot.setConsoleLogger(new PrintStream(new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				//
			}
		}));
		this.results = new ArrayList<>();
		this.janusKernel = Boot.startJanus(
				Modules.override(new StandardJanusPlatformModule()).with(new TestingModule()),
				type, results);
		while (this.janusKernel.isRunning()) {
			Thread.yield();
		}
		Boot.setConsoleLogger(null);
	}

	/** Interface that permits to mark a method that is manually launching the Janus.
	 *
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.TYPE)
	protected @interface JanusRun {

		/** The type of the agent to launch.
		 *
		 * @return the type of the agent to launch.
		 */
		Class<? extends TestingAgent> value();

	}

	/** Abstract implementation of an agent that is used for testing Janus
	 * 
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	protected static abstract class TestingAgent extends Agent {

		private List<Object> results;

		/**
		 * @param parentID - the identifier of the parent's agent.
		 */
		public TestingAgent(UUID parentID) {
			super(parentID);
		}

		/**
		 * @param parentID - the identifier of the parent's agent.
		 * @param agentID - the identifier of the agent.
		 */
		public TestingAgent(UUID parentID, UUID agentID) {
			super(parentID, agentID);
		}
		
		/** Add a result.
		 * 
		 * @param result - the result.
		 */
		protected void addResult(Object result) {
			this.results.add(result);
		}

		/** Invoked at the start of the agent.
		 * 
		 * @param event - the initialization event.
		 */
		@Percept
		protected void _handler_Initialize_0(Initialize event) {
			this.results = (List<Object>) event.parameters[0];
			if (runAgentTest()) {
				getSkill(Schedules.class).in(1000, new Procedure1<Agent>() {
					@Override
					public void apply(Agent it) {
						getSkill(Lifecycle.class).killMe();
					}
				});
			}
		}

		/** Invoked to run the unit test.
		 *
		 * @return <code>true</code> for killing the agent.
		 */
		protected abstract boolean runAgentTest();

	}

}
