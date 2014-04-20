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
package io.janusproject2.kernel;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.google.inject.Injector;

/**
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
@RunWith(PowerMockRunner.class)
@PrepareForTest(ContextFactory.class)
public class ContextFactoryTest {

	private UUID contextID = UUID.randomUUID();
	private UUID defaultSpaceId = UUID.randomUUID();
	private ContextFactory factory;
	@Mock
	private ContextRepository contextRepository;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		// this.contextRepository = mock(ContextRepository.class);
		this.factory = new ContextFactory();
		this.factory.setContextRepository(this.contextRepository);

	}

	@Test
	public void addsCreatedContextToRepository() throws Exception {
		Context ctx = mock(Context.class);
		// when(a.check()).thenReturn("test");
		PowerMockito
				.whenNew(Context.class)
				.withArguments(any(Injector.class), eq(this.contextID),
						eq(this.defaultSpaceId)).thenReturn(ctx);
		Context retCtx = this.factory.create(this.contextID, this.defaultSpaceId);
		assertEquals(ctx, retCtx);
		// ArgumentCaptor<AgentContext> argument = ArgumentCaptor
		// .forClass(AgentContext.class);
		// verify(this.contextRepository).addContext(argument.capture());
		// assertEquals(contextID, argument.getValue().getID());
		// assertEquals(defaultSpaceId, argument.getValue().getDefaultSpace()
		// .getId());

		verify(this.contextRepository).addContext(ctx);
	}

//	@Test(expected = IllegalArgumentException.class)
//	public void disallowsDuplicateContextIDs() {
//		Context retCtx = factory.create(contextID, defaultSpaceId);
//		assertNotNull(retCtx);
//		factory.create(contextID, defaultSpaceId); // throws exception
//	}

}
