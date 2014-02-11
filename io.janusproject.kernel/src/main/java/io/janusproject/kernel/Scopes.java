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

import io.sarl.lang.core.Scope;


/**
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class Scopes {
	@SuppressWarnings("unchecked")
	public final static <T> Scope<T> nullScope() {
		
		return (Scope<T>) AlwaysTrueScope.INSTANCE;
	}
}


enum AlwaysTrueScope implements Scope<Object> {
	INSTANCE;

	public boolean matches(Object o) {
		return true;
	}

	@Override
	public String toString() {
		return "AlwaysTRUE";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRepresentation() {
		return "AlwaysTrueScope";
	}
}