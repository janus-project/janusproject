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
package io.janusproject.kernel.services.zeromq;

import io.janusproject.kernel.services.AbstractServiceImplementationTest;
import io.janusproject.services.network.NetworkService;
import io.janusproject.services.network.NetworkUtil;

import java.net.InetAddress;

import org.junit.Before;

/** 
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class ZeroMQNetworkServiceTest
extends AbstractServiceImplementationTest<NetworkService> {

	private ZeroMQNetworkService service;

	/**
	 */
	public ZeroMQNetworkServiceTest() {
		super(NetworkService.class);
	}

	@Override
	protected final NetworkService getTestedService() {
		return this.service;
	}

	/**
	 */
	@Before
	public void setUp() {
		InetAddress adr = NetworkUtil.getLoopbackAddress();
		this.service = new ZeroMQNetworkService(
				NetworkUtil.toURI(adr, -1));
	}
	
}
