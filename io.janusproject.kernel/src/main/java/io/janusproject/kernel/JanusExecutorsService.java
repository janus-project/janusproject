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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.arakhne.afc.vmutil.locale.Locale;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;

/**
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class JanusExecutorsService extends AbstractService {

	@Inject
	private ScheduledExecutorService schedules;
	
	@Inject
	private ExecutorService exec;
	/** {@inheritDoc}
	 */
	@Override
	protected void doStart() {
		notifyStarted();
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void doStop() {
		this.exec.shutdown();
		this.schedules.shutdown();
		try {
			this.schedules.awaitTermination(5, TimeUnit.SECONDS);
			this.exec.awaitTermination(5, TimeUnit.SECONDS);
			this.schedules.shutdownNow();
			this.exec.shutdownNow();
		} catch (InterruptedException e) {
			throw new RuntimeException(Locale.getString("STOP_ERROR"),e); //$NON-NLS-1$
		} finally{
			notifyStopped();					
		}
		
	}

}
