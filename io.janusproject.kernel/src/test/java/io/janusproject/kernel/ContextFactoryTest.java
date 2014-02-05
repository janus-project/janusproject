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

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import io.janusproject.repository.ContextRepository;
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
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
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
