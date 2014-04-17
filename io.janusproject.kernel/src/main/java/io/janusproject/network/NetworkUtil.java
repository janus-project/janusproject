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

package io.janusproject.network;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/** Provide utilities related to the network.
 * 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class NetworkUtil {

	/** Replies the first public address.
	 * A public address is an address that is not loopback.
	 * 
	 * @param onlyIPv4 - indicates if only IPv4 address can be replied.
	 * @return the first public address or <code>null</code> if
	 * none.
	 */
	public static InetAddress getFirstPublicAddress(boolean onlyIPv4) {
		try {
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			if (interfaces!=null) {
				NetworkInterface inter;
				InetAddress adr;
				Enumeration<InetAddress> addrs;
				boolean allIPs = !onlyIPv4;
				while (interfaces.hasMoreElements()) {
					inter = interfaces.nextElement();
					addrs = inter.getInetAddresses();
					if (addrs!=null) {
						while (addrs.hasMoreElements()) {
							adr = addrs.nextElement();
							if (adr!=null && !adr.isLoopbackAddress() &&
								(allIPs || (adr instanceof Inet4Address))) {
								return adr;
							}
						}
					}
				}
			}
		} catch (SocketException e) {
			//
		}
		return null;
	}
	
}
