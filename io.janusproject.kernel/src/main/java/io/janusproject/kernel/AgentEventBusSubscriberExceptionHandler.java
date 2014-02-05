/*
 * Copyright 2014 Sebastian RODRIGUEZ, Nicolas GAUD, Stéphane GALLAND
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.janusproject.kernel;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.eventbus.SubscriberExceptionContext;
import com.google.common.eventbus.SubscriberExceptionHandler;
import com.google.inject.Inject;

/**
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class AgentEventBusSubscriberExceptionHandler implements SubscriberExceptionHandler {

	@Inject
	private Logger log;
	
	/** {@inheritDoc}
	 */
	@Override
	public void handleException(Throwable exception, SubscriberExceptionContext context) {
		this.log.log(Level.SEVERE, "SubscriberException "+context.toString(),exception);
		exception.printStackTrace();

	}


}
