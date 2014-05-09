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
package io.janusproject.services.impl;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc"})
public class AbstractPrioritizedServiceTest extends Assert {

	private AbstractPrioritizedService service;
	
	@Before
	public void setUp() {
		this.service = new AbstractPrioritizedService() {
			@Override
			protected void doStop() {
				//
			}
			
			@Override
			protected void doStart() {
				//
			}
		};
	}
	
	@After
	public void tearDown() {
		this.service = null;
	}

	@Test
	public void setStartPriority() {
		this.service.setStartPriority(-1);
		for(int i=0; i<10; ++i) {
			assertNotEquals(i, this.service.getStartPriority());
			this.service.setStartPriority(i);
			assertEquals(i, this.service.getStartPriority());
		}
	}

	@Test
	public void setStopPriority() {
		this.service.setStopPriority(-1);
		for(int i=0; i<10; ++i) {
			assertNotEquals(i, this.service.getStopPriority());
			this.service.setStopPriority(i);
			assertEquals(i, this.service.getStopPriority());
		}
	}

}
