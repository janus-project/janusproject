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
package io.janusproject2.kernel;

import io.sarl.lang.core.Address;
import io.sarl.lang.core.Scope;

import java.util.Collection;

import com.google.common.collect.Sets;


/**
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 * @deprecated see {@link io.sarl.util.AddressScope}
 */
@Deprecated
public class AddressScope implements Scope<Address> {

	private static final long serialVersionUID = 6544932857914994641L;

	//TODO: URI?
	private final String SCOPE_ID = "aid://"; //$NON-NLS-1$

	private final Collection<Address> addresses;

	/** Construct a scope.
	 * 
	 * @param addrs - set of the addresses that is describing the scope.
	 */
	AddressScope(Address... addrs) {
		this.addresses = Sets.newHashSet(addrs) ;
	}

	@Override
	public String toString() {
		return this.SCOPE_ID + this.addresses.toString();
	}

	@Override
	public boolean matches(Address address) {
		return this.addresses.contains(address);
	}

	/** Create a scope that is restricted to the given list of addresses.
	 * 
	 * @param addresses -addresses that are allowed in the scope.
	 * @return the scope, restricted to the given set of addresses.
	 */
	public final static AddressScope getScope(Address... addresses) {
		return new AddressScope(addresses);
	}

}
