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

import io.sarl.lang.core.Event;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.SpaceID;

import com.google.common.util.concurrent.Service;

/** This class enables the Janus kernel to be distributed
 * other a network.
 * 
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public interface Network extends Service{

	/** Publish a data over the network.
	 * 
	 * @param id - identifier of the space in which the data was published.
	 * @param scope - scope of the published data.
	 * @param data - data to propage over the network.
	 * @throws Exception
	 */
	void publish(SpaceID id, Scope<?> scope, Event data) throws Exception;
	
	/** Register the given distributed space in the network layer. 
	 * 
	 * @param space - the space distributed over the network by this peer.
	 * @throws Exception
	 */
	void register(DistributedSpace space) throws Exception;
	
	/** Connect this instance of kernel to the given peer over the network.
	 * 
	 * @param peerUri
	 * @throws Exception
	 */
	public void connectPeer(String peerUri) throws Exception;
	
	/** Disconnect this peer from the given peer.
	 * 
	 * @param peer
	 * @throws Exception
	 */
	public void disconnectPeer(String peer) throws Exception;
	
	/** Replies the public URI used by this network interface.
	 * 
	 * @return the public URI, or <code>null</code> if the
	 * network is not started.
	 */
	public String getURI();

}
