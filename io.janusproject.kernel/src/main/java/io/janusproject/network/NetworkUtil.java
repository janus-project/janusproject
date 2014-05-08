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

import java.io.IOError;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.UUID;

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
	public static InetAddress getPrimaryAddress(boolean onlyIPv4) {
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

	/** Replies the byte-array representation of the given id.
	 * 
	 * @param id
	 * @return the byte-array representation.
	 */
	public static byte[] toByteArray(UUID id) {
		return id.toString().getBytes(NetworkConfig.BYTE_ARRAY_STRING_CHARSET);
	}

	/** Replies the id from the given byte-array representation.
	 * 
	 * @param id
	 * @return the UUID.
	 */
	public static UUID fromByteArray(byte[] id) {
		return UUID.fromString(new String(id, NetworkConfig.BYTE_ARRAY_STRING_CHARSET));
	}

	/** Convert an inet address to an URI.
	 * 
	 * @param adr
	 * @return the URI.
	 */
	public static URI toURI(InetAddress adr) {
		try {
			return new URI("tcp://"+adr.getHostAddress()); //$NON-NLS-1$
		}
		catch (URISyntaxException e) {
			throw new IOError(e);
		}
	}

	/** Convert a socket address to an URI.
	 * 
	 * @param adr
	 * @return the URI.
	 */
	public static URI toURI(InetSocketAddress adr) {
		return toURI(adr.getAddress(), adr.getPort());
	}

	/** Convert an inet address to an URI.
	 * 
	 * @param adr - the address.
	 * @param port - port number, if negative or nul, use the "*" notation.
	 * @return the URI.
	 */
	public static URI toURI(InetAddress adr, int port) {
		try {
			String p;
			if (port<=0) p = "*"; //$NON-NLS-1$
			else p = Integer.toString(port);
			return new URI("tcp://"+adr.getHostAddress()+":"+p); //$NON-NLS-1$ //$NON-NLS-2$
		}
		catch (URISyntaxException e) {
			throw new IOError(e);
		}
	}

	/** Extract an Inet address from an URI.
	 * 
	 * @param uri - the address.
	 * @return the address.
	 * @throws IllegalArgumentException if the URI has not the scheme "tcp" nor "udp".
	 * @throws IOError is the host cannot be resolve.
	 */
	public static InetAddress toInetAddress(URI uri) {
		try {
			if ("tcp".equalsIgnoreCase(uri.getScheme())) { //$NON-NLS-1$
				return InetAddress.getByName(uri.getHost());
			}
			else if ("udp".equalsIgnoreCase(uri.getScheme())) { //$NON-NLS-1$
				return InetAddress.getByName(uri.getHost());
			}
			throw new IllegalArgumentException(uri.toString());
		} catch (UnknownHostException e) {
			throw new IOError(e);
		} 
	}

	/** Extract an Inet address from an URI.
	 * 
	 * @param uri - the address.
	 * @return the address.
	 * @throws IllegalArgumentException if the URI has not the scheme "tcp" nor "udp"; or the port is negative or nul.
	 * @throws IOError is the host cannot be resolve.
	 */
	public static InetSocketAddress toInetSocketAddress(URI uri) {
		InetAddress adr = toInetAddress(uri);
		int port = uri.getPort();
		if (port<=0) throw new IllegalArgumentException(uri.toString());
		return new InetSocketAddress(adr, port);
	}

}
