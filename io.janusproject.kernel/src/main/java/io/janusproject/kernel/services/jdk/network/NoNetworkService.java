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
package io.janusproject.kernel.services.jdk.network;

import io.janusproject.services.executor.ExecutorService;
import io.janusproject.services.kerneldiscovery.KernelDiscoveryService;
import io.janusproject.services.logging.LogService;
import io.janusproject.services.network.AbstractNetworkingService;
import io.janusproject.services.network.NetworkServiceListener;
import io.janusproject.services.network.NetworkUtil;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.SpaceID;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.google.common.util.concurrent.Service;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Service that is providing the network service but does not
 * send othet the network.
 *
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
public class NoNetworkService extends AbstractNetworkingService {

	private static final Random RANDOM = new Random();

	private static final int DYNFROM = 0xc000;
    private static final int DYNTO = 0xffff;

    private final List<NetworkServiceListener> listeners = new ArrayList<>();

	private URI localHost;

	/**
	 */
	@Inject
	public NoNetworkService() {
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
	public void publish(Scope<?> scope, Event data)
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
		int port = DYNFROM + (RANDOM.nextInt() % (DYNTO - DYNFROM));
		InetAddress adr = NetworkUtil.getLoopbackAddress();
		if (adr == null) {
			try {
				this.localHost = NetworkUtil.toURI("tcp://127.0.0.1:" + port); //$NON-NLS-1$
			} catch (URISyntaxException e) {
				throw new Error(e);
			}
		} else {
			this.localHost = NetworkUtil.toURI(adr, port);
		}
		notifyStarted();
	}

	/** {@inheritDoc}
	 */
	@Override
	protected void doStop() {
		notifyStopped();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Collection<Class<? extends Service>> getServiceDependencies() {
		return Arrays.<Class<? extends Service>>asList(LogService.class, ExecutorService.class);
	}

	/** {@inheritDoc}
	 */
	@Override
	public Collection<Class<? extends Service>> getServiceWeakDependencies() {
		return Arrays.<Class<? extends Service>>asList(KernelDiscoveryService.class);
	}

}
