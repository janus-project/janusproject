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
package io.janusproject.kernel.space;

import io.sarl.lang.core.Event;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.SpaceID;
import io.sarl.lang.util.SynchronizedSet;
import io.sarl.util.OpenEventSpaceSpecification;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;


/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings({"javadoc","static-method"})
public class SpaceBaseTest extends Assert {

	@Test
	public void getID() {
		SpaceID spaceId = new SpaceID(
				UUID.randomUUID(),
				UUID.randomUUID(),
				OpenEventSpaceSpecification.class);
		SpaceBase base = new SpaceBase(spaceId) {
			@Override
			public void eventReceived(SpaceID space, Scope<?> scope, Event event) {
				throw new UnsupportedOperationException();
			}
			
			@Override
			public SynchronizedSet<UUID> getParticipants() {
				throw new UnsupportedOperationException();
			}
		};
		assertEquals(spaceId, base.getID());
	}

}
