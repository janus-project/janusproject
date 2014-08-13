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
package io.janusproject.kernel;

import io.janusproject.services.api.IServiceManager;
import io.janusproject.services.impl.Services;
import io.janusproject.services.spawn.KernelAgentSpawnListener;
import io.janusproject.services.spawn.SpawnService;
import io.janusproject.util.LoggerCreator;
import io.janusproject.util.TwoStepConstruction;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.arakhne.afc.vmutil.locale.Locale;

import com.google.common.util.concurrent.Service;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Singleton;

/**
 * This class represents the Kernel of the Janus platform.
 * <p>
 * <strong>The Kernel is a singleton.</strong>
 * <p>
 * The Kernel is assimilated to an agent that is omniscient and
 * distributed other the network. It is containing all the other agents.
 * <p>
 * To create a Kernel, you should use the function {@link #create(Module...)}.
 *
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
@TwoStepConstruction(names = { "setJanusContext" })
public class Kernel {

	private AgentContext janusContext;

	private final IServiceManager serviceManager;

	private final SpawnService spawnService;

	/**
	 * Constructs a Janus kernel.
	 *
	 * @param serviceManager is the instance of the service manager that must be used by the kernel.
	 * @param spawnService is the instance of the spawn service.
	 * @param exceptionHandler is the handler of the uncaught exceptions.
	 */
	@Inject
	Kernel(IServiceManager serviceManager,
			SpawnService spawnService,
			UncaughtExceptionHandler exceptionHandler) {
		// Initialize the fields
		this.serviceManager = serviceManager;
		this.spawnService = spawnService;

		// Ensure that all the threads has a default hander.
		Thread.setDefaultUncaughtExceptionHandler(exceptionHandler);

		// Listen on the kernel's events
		this.spawnService.addKernelAgentSpawnListener(new KernelStoppingListener());

		// Start the services NOW to ensure that the default context and space
		// of the Janus agent are catched by the modules;
		Services.startServices(this.serviceManager);
	}

	/** Create an instance of {@link Kernel}.
	 *
	 * @param modules - modules to link to the new kernel.
	 * @return the new kernel.
	 */
	public static final Kernel create(Module... modules) {
		Injector injector = Guice.createInjector(modules);
		Kernel k = injector.getInstance(Kernel.class);
		return k;
	}

	/**
	 * Spawn an agent of the given type, and pass the parameters to its initialization function.
	 *
	 * @param agent - the type of the agent to spawn.
	 * @param params - the list of the parameters to pass to the agent initialization function.
	 * @return the identifier of the agent, never <code>null</code>.
	 */
	public UUID spawn(Class<? extends Agent> agent, Object... params) {
		return this.spawnService.spawn(this.janusContext, agent, params);
	}

	/** Replies a kernel service that is alive.
	 *
	 * @param <S> - type of the type to reply.
	 * @param type - type of the type to reply.
	 * @return the service, or <code>null</code>.
	 */
	public <S extends Service> S getService(Class<S> type) {
		for (Service serv : this.serviceManager.servicesByState().values()) {
			if (serv.isRunning() && type.isInstance(serv)) {
				return type.cast(serv);
			}
		}
		return null;
	}

	/**
	 * Change the Janus context of the kernel.
	 *
	 * @param janusContext - the new janus kernel. It must be never <code>null</code>.
	 */
	@Inject
	void setJanusContext(@io.janusproject.kernel.annotations.Kernel AgentContext janusContext) {
		assert (janusContext != null);
		this.janusContext = janusContext;
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class KernelStoppingListener implements KernelAgentSpawnListener {

		/**
		 */
		public KernelStoppingListener() {
			//
		}

		/** {@inheritDoc}
		 */
		@Override
		public void kernelAgentSpawn() {
			//
		}

		/** {@inheritDoc}
		 */
		@Override
		public void kernelAgentDestroy() {
			// CAUTION: EXECUTE THE STOP FUNCTION IN A THREAD THAT
			// IS INDEPENDENT TO THE ONES FROM THE EXECUTORS
			// CREATED BY THE EXECUTORSERVICE.
			// THIS AVOID THE STOP FUNCTION TO BE INTERRUPTED
			// BECAUSE THE EXECUTORSERVICE WAS SHUTTED DOWN.
			StopTheKernel t = new StopTheKernel();
			t.start();
		}
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class StopTheKernel implements ThreadFactory, Runnable, UncaughtExceptionHandler {

		/** Logger for the shuting down stage.
		 */
		private final Logger rawLogger = LoggerCreator.createLogger(Kernel.class.getName());

		/**
		 */
		public StopTheKernel() {
			//
		}

		/** Start the thread.
		 */
		public void start() {
			Thread t = newThread(this);
			t.start();
		}

		/** {@inheritDoc}
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void run() {
			this.rawLogger.info(Locale.getString(Kernel.class, "STOP_KERNEL_SERVICES")); //$NON-NLS-1$
			Services.stopServices(Kernel.this.serviceManager);
			this.rawLogger.info(Locale.getString(Kernel.class, "KERNEL_SERVICES_STOPPED")); //$NON-NLS-1$
		}

		/** {@inheritDoc}
		 */
		@Override
		public Thread newThread(Runnable r) {
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setName("Janus shutdown"); //$NON-NLS-1$
			t.setDaemon(false);
			t.setUncaughtExceptionHandler(this);
			return t;
		}

		/** {@inheritDoc}
		 */
		@Override
		public void uncaughtException(Thread t, Throwable e) {
			assert (t != null);
			assert (e != null);
			LogRecord record = new LogRecord(Level.SEVERE, e.getLocalizedMessage());
			record.setThrown(e);
			StackTraceElement elt = e.getStackTrace()[0];
			assert (elt != null);
			record.setSourceClassName(elt.getClassName());
			record.setSourceMethodName(elt.getMethodName());
			this.rawLogger.log(record);
		}

	}

}

