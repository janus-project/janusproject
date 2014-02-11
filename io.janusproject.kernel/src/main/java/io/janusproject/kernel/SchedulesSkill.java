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

import io.sarl.core.AgentTask;
import io.sarl.core.Schedules;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Skill;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.arakhne.afc.vmutil.locale.Locale;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;

import com.google.common.base.Objects;
import com.google.inject.Inject;

/**
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class SchedulesSkill extends Skill implements Schedules {

	@Inject
	private ScheduledExecutorService executor;

	private Map<String, AgentTask> tasks = new ConcurrentHashMap<>();
	private Map<String, ScheduledFuture<AgentRunnableTask>> futures = new ConcurrentHashMap<>();

	/**
	 * @param agent
	 */
	public SchedulesSkill(Agent agent) {
		super(agent);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void install() {
		super.install();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void uninstall() {
		for (ScheduledFuture<AgentRunnableTask> future : this.futures.values()) {
			if(!future.isDone() && !future.isCancelled()){				
				future.cancel(true);
			}
		}
		super.uninstall();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public AgentTask in(long delay, Procedure1<? super Agent> procedure) {
		return in(this.task("task-" + UUID.randomUUID()), delay, procedure); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AgentTask in(AgentTask task, long delay, Procedure1<? super Agent> procedure) {
		task.setProcedure((Procedure1<Agent>) procedure);
		ScheduledFuture<AgentRunnableTask> sf = (ScheduledFuture<AgentRunnableTask>) this.executor.schedule(
				new AgentRunnableTask(task, getOwner()), delay, TimeUnit.MILLISECONDS);
		this.futures.put(task.getName(), sf);
		return task;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AgentTask task(String name) {
		if (this.tasks.containsKey(name)) {
			return this.tasks.get(name);
		}
		final AgentTask t = new AgentTask();
		t.setName(name);
		t.setGuard(new Function1<Agent, Boolean>() {

			@Override
			public Boolean apply(Agent arg0) {
				return true;
			}
		});
		this.tasks.put(name, t);
		return t;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AgentTask every(long period, Procedure1<? super Agent> procedure) {
		return every(this.task("task-" + UUID.randomUUID()), period, procedure); //$NON-NLS-1$
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AgentTask every(AgentTask task, long period, Procedure1<? super Agent> procedure) {
		task.setProcedure((Procedure1<Agent>) procedure);
		ScheduledFuture<AgentRunnableTask> sf = (ScheduledFuture<AgentRunnableTask>) this.executor.scheduleAtFixedRate(
				new AgentRunnableTask(task, getOwner()), 0, period, TimeUnit.MILLISECONDS);
		this.futures.put(task.getName(), sf);
		return task;
	}



	private static class AgentRunnableTask implements Runnable {
		private WeakReference<AgentTask> agentTaskRef;
		private WeakReference<Agent> agentRef;

		public AgentRunnableTask(AgentTask task, Agent agent) {
			this.agentTaskRef = new WeakReference<AgentTask>(task);
			this.agentRef = new WeakReference<Agent>(agent);
		}

		@Override
		public void run() {

			if (this.agentTaskRef.get() == null) {
				System.out.println(Locale.getString("NULL_AGENT_TASK")); //$NON-NLS-1$
			} else {
				AgentTask task = this.agentTaskRef.get();
				if (task.getGuard().apply(this.agentRef.get())) {
					task.getProcedure().apply(this.agentRef.get());
				}
			}

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return Objects.toStringHelper(this).add(
					"name", this.agentTaskRef.get().getName()) //$NON-NLS-1$
					.add("agent", this.agentRef.get().getID()).toString(); //$NON-NLS-1$
		}

	}
}
