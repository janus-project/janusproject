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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/** Utility functions to set-up the Janus platform.
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class Janus {
	
	/** Create an instance of {@link Kernel}.
	 * 
	 * @param modules - modules to link to the new kernel.
	 * @return the new kernel.
	 */
	public static final Kernel create(Module... modules){
		Injector injector = Guice.createInjector(modules);
		Kernel k = injector.getInstance(Kernel.class);
		return k;
	}
	
	
}
