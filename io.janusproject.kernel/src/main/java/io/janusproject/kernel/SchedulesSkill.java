/*
 * $Id$
 * 
 * Janus platform is an open-source multiagent platform.
 * More details on &lt;http://www.janusproject.io&gt;
 * Copyright (C) 2013 Janus Core Developers
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see &lt;http://www.gnu.org/licenses/&gt;.
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
		return in(this.task("task-" + UUID.randomUUID()), delay, procedure);
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
		return every(this.task("task-" + UUID.randomUUID()), period, procedure);
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
				System.out.println("Agent Task is null");
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
			return Objects.toStringHelper(this).add("name", this.agentTaskRef.get().getName())
					.add("agent", this.agentRef.get().getID()).toString();
		}

	}
}
