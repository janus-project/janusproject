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
package io.janusproject2.kernel.services;

import java.net.URI;

import io.sarl.lang.core.Event;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.SpaceID;

import com.google.common.util.concurrent.Service;

/** This class enables the Janus kernel to send messages other
 * the network.
 * 
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public interface NetworkService extends Service {

	/** Publish a data over the network.
	 * 
	 * @param id - identifier of the space in which the data was published.
	 * @param scope - scope of the published data.
	 * @param data - data to propage over the network.
	 * @throws Exception
	 */
	void publish(SpaceID id, Scope<?> scope, Event data) throws Exception;
	
	/** Connect this instance of kernel to the given peer over the network
	 * and for the given space.
	 * <p>
	 * If the network service is not yet ready for connecting the given
	 * URI, this URI <strong>MUST</strong> be bufferized until the
	 * network service has been fully started. 
	 * 
	 * @param peerUri
	 * @param space
	 * @param listener - listener on the receiving of the events.
	 * @throws Exception
	 */
	public void connectPeer(URI peerUri, SpaceID space, NetworkEventReceivingListener listener) throws Exception;
	
	/** Disconnect this peer from the given peer for the given space.
	 * 
	 * @param peer
	 * @param space
	 * @throws Exception
	 */
	public void disconnectPeer(URI peer, SpaceID space) throws Exception;

	/** Disconnect this peer from the given peer for all the spaces.
	 * 
	 * @param peer
	 * @throws Exception
	 */
	public void disconnectPeer(URI peer) throws Exception;
	
	/** Replies the public URI used by this network interface.
	 * 
	 * @return the public URI, or <code>null</code> if the
	 * network is not started.
	 */
	public URI getURI();

	/** Add a listener on the events in this service and related
	 * to the network. 
	 * 
	 * @param listener
	 */
	public void addNetworkServiceListener(NetworkServiceListener listener);

	/** Remove a listener on the events in this service and related
	 * to the network. 
	 * 
	 * @param listener
	 */
	public void removeNetworkServiceListener(NetworkServiceListener listener);

}
