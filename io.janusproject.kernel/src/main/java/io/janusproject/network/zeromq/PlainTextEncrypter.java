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
package io.janusproject.network.zeromq;

import java.util.UUID;

/**
 * A utility implementation of the {@link EventEncrypter} that creates the
 * {@link EventEnvelope} fields using {@link String#getBytes()}
 * 
 * The main use of the class should be development to be able to easy see what's
 * being transfered on the wire.
 * 
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class PlainTextEncrypter implements EventEncrypter {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EventEnvelope encrypt(EventPack pack) {
		return EventEnvelope.build(pack.getContextId().getBytes(), pack
				.getSpaceId().getBytes(), pack.getScope().getBytes(), pack
				.getHeaders().getBytes(), pack.getEvent().getBytes());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public EventPack decrypt(EventEnvelope envelope) {
		EventPack pack = new EventPack();
		pack.setContextId(new String(envelope.getContextId()));
		pack.setSpaceId(new String(envelope.getSpaceId()));
		pack.setScope(new String(envelope.getScope()));
		pack.setEvent(new String(envelope.getBody()));
		pack.setHeaders(new String(envelope.getCustomHeaders()));
		return pack;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] encrytContextID(UUID id) throws Exception {
		return id.toString().getBytes();
	}

}
