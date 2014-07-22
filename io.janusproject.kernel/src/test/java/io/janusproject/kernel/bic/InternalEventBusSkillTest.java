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

import io.janusproject.services.logging.LogService;
import io.sarl.core.Destroy;
import io.sarl.core.Initialize;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.internal.verification.Times;

import com.google.common.eventbus.AsyncSyncEventBus;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc"})
public class InternalEventBusSkillTest extends Assert {

	@Mock
	private AsyncSyncEventBus eventBus;
	
	@Mock
	private LogService logger;

	@Mock
	private Agent agent;

	@Mock
	private Address innerAddress;

	@InjectMocks
	private InternalEventBusSkill skill;
	
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	@After
	public void tearDown() throws Exception {
		this.agent = null;
		this.eventBus = null;
		this.skill = null;
	}
	
	@Test
	public void asEventListener() {
		assertNotNull(this.skill.asEventListener());
	}

	@Test
	public void getInnerDefaultSpaceAddress() {
		assertSame(this.innerAddress, this.skill.getInnerDefaultSpaceAddress());
	}
		
	@Test
	public void registerEventListener() {
		EventListener eventListener = Mockito.mock(EventListener.class);
		this.skill.registerEventListener(eventListener);
		ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(this.eventBus, new Times(1)).register(argument.capture());
		assertSame(eventListener, argument.getValue());
	}

	@Test
	public void unregisterEventListener() {
		EventListener eventListener = Mockito.mock(EventListener.class);
		this.skill.registerEventListener(eventListener);
		//
		this.skill.unregisterEventListener(eventListener);
		ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(this.eventBus, new Times(1)).unregister(argument.capture());
		assertSame(eventListener, argument.getValue());
	}

	@Test
	public void install() {
		this.skill.install();
		ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(this.eventBus, new Times(1)).register(argument.capture());
		assertSame(this.agent, argument.getValue());
	}
	
	@Test
	public void uninstall() {
		this.skill.install();
		EventListener eventListener = Mockito.mock(EventListener.class);
		this.skill.registerEventListener(eventListener);
		//
		this.skill.uninstall();
		ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(this.eventBus, new Times(2)).unregister(argument.capture());
		assertSame(this.agent, argument.getAllValues().get(0));
		assertSame(eventListener, argument.getAllValues().get(1));
	}

	@Test
	public void selfEvent_other_notinitialized() {
		Event event = Mockito.mock(Event.class);
		this.skill.selfEvent(event);
		Mockito.verifyZeroInteractions(this.eventBus);
	}

	@Test
	public void selfEvent_other_initialized() {
		Initialize initEvent = Mockito.mock(Initialize.class);
		this.skill.selfEvent(initEvent);
		//
		Event event = Mockito.mock(Event.class);
		this.skill.selfEvent(event);
		ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(this.eventBus, new Times(1)).post(argument.capture());
		assertSame(event, argument.getValue());
	}

	@Test
	public void selfEvent_initialize() {
		Initialize event = Mockito.mock(Initialize.class);
		this.skill.selfEvent(event);
		ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(this.eventBus, new Times(1)).fire(argument.capture());
		assertSame(event, argument.getValue());
	}

	@Test
	public void selfEvent_destroy() {
		Destroy event = Mockito.mock(Destroy.class);
		this.skill.selfEvent(event);
		ArgumentCaptor<Object> argument = ArgumentCaptor.forClass(Object.class);
		Mockito.verify(this.eventBus, new Times(1)).fire(argument.capture());
		assertSame(event, argument.getValue());
	}

}
