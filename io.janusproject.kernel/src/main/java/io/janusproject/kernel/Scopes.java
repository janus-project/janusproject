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


/** Accessors and utilities related to the scopes.
 * 
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @deprecated see {@link io.sarl.util.Scopes}
 */
@Deprecated
public class Scopes {

	/** Replies the scope that is representing everything.
	 * This scope represents the entire interaction space, and all
	 * the entities in this space.
	 * 
	 * @return the null scope.
	 */
	@SuppressWarnings("unchecked")
	public final static <T> Scope<T> nullScope() {
		return (Scope<T>) AlwaysTrueScope.INSTANCE;
	}

	/** It is coded with an enumeration to have only one instance
	 * of this scope in the JVM, ie. it is a way to code the singleton
	 * design pattern in a private scope.
	 * 
	 * @author $Author: srodriguez$
	 * @version $Name$ $Revision$ $Date$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static enum AlwaysTrueScope implements Scope<Object> {
		INSTANCE;

		@Override
		public boolean matches(Object o) {
			return true;
		}

		@Override
		public String toString() {
			return "AlwaysTRUE"; //$NON-NLS-1$
		}

	}

}
