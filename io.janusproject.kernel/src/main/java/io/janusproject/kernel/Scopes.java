/*
 * $Id$
 * 
 * Janus platform is an open-source multiagent platform.
 * More details on &lt;http://www.janus-project.org&gt;
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