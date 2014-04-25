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
package io.janusproject.services;

import io.sarl.lang.core.Event;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.SpaceID;

import java.net.URI;
import java.util.EventListener;

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
	public void connectToRemoteSpaces(URI peerUri, SpaceID space, NetworkEventReceivingListener listener) throws Exception;
	
	/** Disconnect this peer from the given peer for the given space.
	 * 
	 * @param peer
	 * @param space
	 * @throws Exception
	 */
	public void disconnectFromRemoteSpace(URI peer, SpaceID space) throws Exception;

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

	/** Listener on events that are received from the network.
	 *  
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	public interface NetworkEventReceivingListener extends EventListener {

		/** Invoked when a data is received from a distant peer.
		 * 
		 * @param space - the id of the space.
		 * @param scope - the scope of the received data.
		 * @param event - the event with the data inside.
		 */
		public void eventReceived(SpaceID space, Scope<?> scope, Event event);
		
	}
	
}
