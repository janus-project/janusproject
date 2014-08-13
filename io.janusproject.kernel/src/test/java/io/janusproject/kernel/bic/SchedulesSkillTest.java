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
package io.janusproject.kernel.bic;

import io.janusproject.services.executor.ExecutorService;
import io.janusproject.services.logging.LogService;
import io.sarl.core.AgentTask;
import io.sarl.lang.core.Agent;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.xtext.xbase.lib.Procedures.Procedure1;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc","unchecked","rawtypes"})
public class SchedulesSkillTest extends Assert {

	private UUID agentId;
	
	@Mock
	private ExecutorService executorService;
	
	@Mock
	private Agent agent;

	@Mock
	private LogService logger;

	@InjectMocks
	private SchedulesSkill skill;
		
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		this.agentId = UUID.randomUUID();
		Mockito.when(this.agent.getID()).thenReturn(this.agentId);
		Mockito.when(this.executorService.schedule(
				Matchers.any(Runnable.class),
				Matchers.any(long.class),
				Matchers.any(TimeUnit.class))).thenAnswer(new Answer<ScheduledFuture>() {
					@Override
					public ScheduledFuture answer(InvocationOnMock invocation)
							throws Throwable {
						ScheduledFuture f = Mockito.mock(ScheduledFuture.class);
						Mockito.when(f.isDone()).thenReturn(false);
						Mockito.when(f.isCancelled()).thenReturn(false);
						Mockito.when(f.cancel(Matchers.anyBoolean())).thenReturn(true);
						return f;
					}
				});
		Mockito.when(this.executorService.scheduleAtFixedRate(
				Matchers.any(Runnable.class),
				Matchers.any(long.class),
				Matchers.any(long.class),
				Matchers.any(TimeUnit.class))).thenAnswer(new Answer<ScheduledFuture>() {
					@Override
					public ScheduledFuture answer(InvocationOnMock invocation)
							throws Throwable {
						ScheduledFuture f = Mockito.mock(ScheduledFuture.class);
						Mockito.when(f.isDone()).thenReturn(false);
						Mockito.when(f.isCancelled()).thenReturn(false);
						Mockito.when(f.cancel(Matchers.anyBoolean())).thenReturn(true);
						return f;
					}
				});
	}

	@After
	public void tearDown() throws Exception {
		this.logger = null;
		this.skill = null;
		this.agent = null;
		this.agentId = null;
	}
	
	@Test
	public void task() {
		AgentTask task = this.skill.task("thename"); //$NON-NLS-1$
		assertNotNull(task);
		assertNotNull(task.getGuard());
		assertNull(task.getProcedure());
		assertEquals("thename", task.getName()); //$NON-NLS-1$
		//
		AgentTask task2 = this.skill.task("thename"); //$NON-NLS-1$
		assertSame(task, task2);
		//
		AgentTask task3 = this.skill.task("thename2"); //$NON-NLS-1$
		assertNotSame(task, task3);
	}

	@Test
	public void inLongProcedure1() {
		Procedure1 procedure = Mockito.mock(Procedure1.class);
		AgentTask task = this.skill.in(5, procedure);
		assertNotNull(task);
		assertSame(procedure, task.getProcedure());
		ArgumentCaptor<Runnable> argument1 = ArgumentCaptor.forClass(Runnable.class);
		ArgumentCaptor<Long> argument2 = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<TimeUnit> argument3 = ArgumentCaptor.forClass(TimeUnit.class);
		Mockito.verify(this.executorService, new Times(1)).schedule(argument1.capture(), argument2.capture(), argument3.capture());
		assertNotNull(argument1.getValue());
		assertEquals(new Long(5), argument2.getValue());
		assertSame(TimeUnit.MILLISECONDS, argument3.getValue());
	}

	@Test
	public void inAgentTaskLongProcedure1() {
		AgentTask task = Mockito.mock(AgentTask.class);
		Mockito.when(task.getName()).thenReturn("thetask"); //$NON-NLS-1$
		Procedure1 procedure = Mockito.mock(Procedure1.class);
		AgentTask t = this.skill.in(task, 5, procedure);
		assertSame(task, t);
		ArgumentCaptor<Procedure1> argument0 = ArgumentCaptor.forClass(Procedure1.class);
		Mockito.verify(task, new Times(1)).setProcedure(argument0.capture());
		assertSame(procedure, argument0.getValue());
		ArgumentCaptor<Runnable> argument1 = ArgumentCaptor.forClass(Runnable.class);
		ArgumentCaptor<Long> argument2 = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<TimeUnit> argument3 = ArgumentCaptor.forClass(TimeUnit.class);
		Mockito.verify(this.executorService, new Times(1)).schedule(argument1.capture(), argument2.capture(), argument3.capture());
		assertNotNull(argument1.getValue());
		assertEquals(new Long(5), argument2.getValue());
		assertSame(TimeUnit.MILLISECONDS, argument3.getValue());
	}

	@Test
	public void everyLongProcedure1() {
		Procedure1 procedure = Mockito.mock(Procedure1.class);
		AgentTask task = this.skill.every(5, procedure);
		assertNotNull(task);
		assertSame(procedure, task.getProcedure());
		ArgumentCaptor<Runnable> argument1 = ArgumentCaptor.forClass(Runnable.class);
		ArgumentCaptor<Long> argument2 = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<Long> argument3 = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<TimeUnit> argument4 = ArgumentCaptor.forClass(TimeUnit.class);
		Mockito.verify(this.executorService, new Times(1)).scheduleAtFixedRate(argument1.capture(), argument2.capture(), argument3.capture(), argument4.capture());
		assertNotNull(argument1.getValue());
		assertEquals(new Long(0), argument2.getValue());
		assertEquals(new Long(5), argument3.getValue());
		assertSame(TimeUnit.MILLISECONDS, argument4.getValue());
	}

	@Test
	public void everyAgentTaskLongProcedure1() {
		AgentTask task = Mockito.mock(AgentTask.class);
		Mockito.when(task.getName()).thenReturn("thetask"); //$NON-NLS-1$
		Procedure1 procedure = Mockito.mock(Procedure1.class);
		AgentTask t = this.skill.every(task, 5, procedure);
		assertSame(task, t);
		ArgumentCaptor<Procedure1> argument0 = ArgumentCaptor.forClass(Procedure1.class);
		Mockito.verify(task, new Times(1)).setProcedure(argument0.capture());
		assertSame(procedure, argument0.getValue());
		ArgumentCaptor<Runnable> argument1 = ArgumentCaptor.forClass(Runnable.class);
		ArgumentCaptor<Long> argument2 = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<Long> argument3 = ArgumentCaptor.forClass(Long.class);
		ArgumentCaptor<TimeUnit> argument4 = ArgumentCaptor.forClass(TimeUnit.class);
		Mockito.verify(this.executorService, new Times(1)).scheduleAtFixedRate(argument1.capture(), argument2.capture(), argument3.capture(), argument4.capture());
		assertNotNull(argument1.getValue());
		assertEquals(new Long(0), argument2.getValue());
		assertEquals(new Long(5), argument3.getValue());
		assertSame(TimeUnit.MILLISECONDS, argument4.getValue());
	}

	@Test
	public void uninstall() {
		Procedure1 procedure1 = Mockito.mock(Procedure1.class);
		this.skill.every(5, procedure1);
		Procedure1 procedure2 = Mockito.mock(Procedure1.class);
		this.skill.in(5, procedure2);
		Collection<ScheduledFuture<?>> futures = this.skill.getActiveFutures();
		assertEquals(2, futures.size());
		//
		this.skill.uninstall();
		//
		Collection<String> activeTasks = this.skill.getActiveTasks();
		assertTrue(activeTasks.isEmpty());
		for(ScheduledFuture<?> f : futures) {
			Mockito.verify(f, new Times(1)).cancel(Matchers.anyBoolean());
		}
	}

	@Test
	public void cancelAgentTask() {
		Procedure1 procedure1 = Mockito.mock(Procedure1.class);
		AgentTask t1 = this.skill.every(5, procedure1);
		Procedure1 procedure2 = Mockito.mock(Procedure1.class);
		AgentTask t2 = this.skill.in(5, procedure2);
		Collection<ScheduledFuture<?>> futures = this.skill.getActiveFutures();
		assertEquals(2, futures.size());
		//
		this.skill.cancel(t2);
		this.skill.cancel(t1);
		//
		Collection<String> activeTasks = this.skill.getActiveTasks();
		assertTrue(activeTasks.isEmpty());
		for(ScheduledFuture<?> f : futures) {
			Mockito.verify(f, new Times(1)).cancel(Matchers.anyBoolean());
		}
	}

	@Test
	public void cancelAgentTaskBoolean_true() {
		Procedure1 procedure1 = Mockito.mock(Procedure1.class);
		AgentTask t1 = this.skill.every(5, procedure1);
		Procedure1 procedure2 = Mockito.mock(Procedure1.class);
		AgentTask t2 = this.skill.in(5, procedure2);
		Collection<ScheduledFuture<?>> futures = this.skill.getActiveFutures();
		assertEquals(2, futures.size());
		//
		this.skill.cancel(t2,true);
		this.skill.cancel(t1,true);
		//
		Collection<String> activeTasks = this.skill.getActiveTasks();
		assertTrue(activeTasks.isEmpty());
		for(ScheduledFuture<?> f : futures) {
			Mockito.verify(f, new Times(1)).cancel(Matchers.anyBoolean());
		}
	}

	@Test
	public void cancelAgentTaskBoolean_false() {
		Procedure1 procedure1 = Mockito.mock(Procedure1.class);
		AgentTask t1 = this.skill.every(5, procedure1);
		Procedure1 procedure2 = Mockito.mock(Procedure1.class);
		AgentTask t2 = this.skill.in(5, procedure2);
		Collection<ScheduledFuture<?>> futures = this.skill.getActiveFutures();
		assertEquals(2, futures.size());
		//
		this.skill.cancel(t2,false);
		this.skill.cancel(t1,false);
		//
		Collection<String> activeTasks = this.skill.getActiveTasks();
		assertTrue(activeTasks.isEmpty());
		for(ScheduledFuture<?> f : futures) {
			Mockito.verify(f, new Times(1)).cancel(Matchers.anyBoolean());
		}
	}

}
