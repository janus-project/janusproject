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
package io.janusproject.network.nonetwork;

import io.janusproject.network.NetworkUtil;
import io.janusproject.services.NetworkService;
import io.janusproject.services.NetworkServiceListener;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.SpaceID;

import java.io.IOError;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.util.concurrent.AbstractService;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Service that is providing the network service but does not
 * send othet the network.
 * 
 * @author $Author: sgalland$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
class NoNetwork extends AbstractService implements NetworkService {

	private final List<NetworkServiceListener> listeners = new ArrayList<>();
	
	private URI localHost = null;
	
	/**
	 */
	@Inject
	NoNetwork() {
		//
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized URI getURI() {
		return this.localHost;
	}


	/** {@inheritDoc}
	 */
	@Override
	public void addNetworkServiceListener(NetworkServiceListener listener) {
		synchronized (this.listeners) {
			this.listeners.add(listener);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public void removeNetworkServiceListener(NetworkServiceListener listener) {
		synchronized (this.listeners) {
			this.listeners.remove(listener);
		}
	}

	/** {@inheritDoc}
	 */
	@Override
	public void publish(SpaceID id, Scope<?> scope, Event data)
			throws Exception {
		//
	}

	/** {@inheritDoc}
	 */
	@Override
	public void connectToRemoteSpaces(URI peerUri, SpaceID space,
			NetworkEventReceivingListener listener) throws Exception {
		//
	}

	/** {@inheritDoc}
	 */
	@Override
	public void disconnectFromRemoteSpace(URI peer, SpaceID space) throws Exception {
		//
	}

	/** {@inheritDoc}
	 */
	@Override
	public void disconnectPeer(URI peer) throws Exception {
		//
	}

	/** {@inheritDoc}
	 */
	@Override
	protected synchronized void doStart() {
		InetAddress adr;
		try {
			adr = InetAddress.getLocalHost();
		}
		catch (UnknownHostException e) {
			throw new IOError(e);
		}
		assert(adr!=null);
		this.localHost = NetworkUtil.toURI(adr);
		notifyStarted();
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void doStop() {
		notifyStopped();
	}

}
