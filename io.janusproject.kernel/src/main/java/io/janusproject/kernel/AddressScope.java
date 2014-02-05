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

import io.sarl.lang.core.Address;
import io.sarl.lang.core.Scope;

import java.util.Collection;

import com.google.common.collect.Sets;


/**
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class AddressScope implements Scope<Address> {

	private final String SCOPE_ID = "aid://";

	private Collection<Address> addresses = null;

	AddressScope(Address... addrs) {
		this.addresses = Sets.newHashSet(addrs) ;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getRepresentation() {
		return this.SCOPE_ID + this.addresses.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean matches(Address address) {
		return this.addresses.contains(address);
	}
	
	public final static AddressScope getScope(Address... addresses){
		return new AddressScope(addresses);
	}

}
