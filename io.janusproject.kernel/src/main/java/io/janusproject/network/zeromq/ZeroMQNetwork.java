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

import static io.janusproject.network.zeromq.ZeroMQConfig.PUB_URI;
import io.janusproject.kernel.DistributedSpace;
import io.janusproject.kernel.Network;
import io.janusproject.kernel.Scopes;
import io.janusproject.repository.ContextRepository;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.SpaceID;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
class ZeroMQNetwork extends AbstractExecutionThreadService implements Network {
	private ZContext context;
	private Socket publisher;

	private static final String CMD_DISCOVER = "DICOVER_SPACE";

	private Map<SpaceID, DistributedSpace> spaces = new ConcurrentHashMap<>();
	private Map<String, Socket> subcribers = new ConcurrentHashMap<>();

	@Inject
	private Logger log;

	// TODO Change poller that can be stopped properly.
	private Poller poller;

	@Inject
	private ContextRepository contextRepository;

	@Inject
	private EventSerializer serializer;

	@Inject
	private ExecutorService executorService;

	private final String uri;

	/**
	 * 
	 */
	@Inject
	ZeroMQNetwork(@Named(PUB_URI) String uri) {
		this.uri = uri;
	}

	public void connectPeer(String peerURI) throws Exception {

		this.log.severe("Connecting Peer " + peerURI);
//		Socket subscriber = this.context.socket(ZMQ.SUB);
		Socket subscriber = this.context.createSocket(ZMQ.SUB);

		this.subcribers.put(peerURI, subscriber);
		for (SpaceID sid : this.spaces.keySet()) {
			subscriber.subscribe(this.serializer.serializeContextID(sid.getContextID()));
		}
		subscriber.connect(peerURI);
		this.poller.register(subscriber, Poller.POLLIN);
		this.log.severe("Connected Peer " + peerURI);
		
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void disconnectPeer(String peerURI) throws Exception {
		this.log.severe("Disconnecting Peer " + peerURI);
		Socket s = this.subcribers.get(peerURI);
		this.poller.unregister(s);

		//FIXME s.close();
		//this.context.destroySocket(s);
		this.log.severe("Disconnected Peer " + peerURI);
	}

	public void register(DistributedSpace space) throws Exception {

		this.log.fine("Registering distributed Space: " + space.getID());
		byte[] topic = this.serializer.serializeContextID(space.getID().getContextID());
		this.spaces.put(space.getID(), space);
		int next = this.poller.getNext();

		for (int i = 0; i < next; i++) {
			this.poller.getSocket(i).subscribe(topic);
		}
		notifyOfSpace(space);

	}

	/**
	 * @param space
	 * @throws Exception
	 */
	private void notifyOfSpace(DistributedSpace space) throws Exception {
		EventDispatch d = new EventDispatch(space.getID(), NoEvent.INSTANCE, Scopes.nullScope());
		d.getHeaders().put("x-netcmd", CMD_DISCOVER);

		EventEnvelope command = processOutgoing(d);
		command.send(this.publisher);

	}

	public void publish(SpaceID spaceID, Scope<?> scope, Event e) throws Exception {

		EventEnvelope env = processOutgoing(e.getSource().getSpaceId(), e, scope);
		env.send(this.publisher);
		this.log.fine("Publishing Event - spaceID : " + spaceID.toString() + " Event:" + e);

	}

	/**
	 * @param env
	 * @throws Exception
	 */
	void receive(EventEnvelope env) throws Exception {
		this.log.fine("Network on " + this.uri + " received " + env);
		final EventDispatch dispatch = processIncomming(env);

		DistributedSpace space = this.spaces.get(dispatch.getSpaceID());

		if (space == null) {
			SpaceID spaceID = dispatch.getSpaceID();
			this.contextRepository.getContext(spaceID.getContextID()).createSpace(spaceID.getSpaceSpecification(),
					spaceID.getID());
			Preconditions.checkNotNull(this.spaces.get(spaceID), "Space ( %s ) was not created on time", spaceID);
			// FIXME: Improve before release
			space = this.spaces.get(spaceID);
		}
		String cmd = dispatch.getHeaders().get("x-netcmd");
		if (cmd != null && CMD_DISCOVER.equals(cmd)) {
			return;
		}

		final DistributedSpace realSpace = space;

		this.executorService.execute(new Runnable() {

			@Override
			public void run() {
				realSpace.recv(dispatch.getScope(), dispatch.getEvent());

			}
		});

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void startUp() throws Exception {
		//this.context = ZMQ.context(1);
		this.context = new ZContext();
		//this.publisher = this.context.socket(ZMQ.PUB);
		this.publisher = this.context.createSocket(ZMQ.PUB);
		this.publisher.bind(this.uri);
		this.poller = new Poller(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void shutDown() throws Exception {
		// TODO this.poller.stop();
		//stopPoller();
		
		//this.publisher.close();
		
		this.context.destroy();
		this.log.info("ZeroMQNetwork Shutdown");
	}

	private void stopPoller() {
		this.log.info("Stopping Poller");
		for (int i = 0; i < this.poller.getSize(); i++) {
			Socket socket = this.poller.getSocket(i);
			this.poller.unregister(socket);
			socket.close();
		}
	}

	private EventEnvelope processOutgoing(EventDispatch dispatch) throws Exception {
		return this.serializer.serialize(dispatch);
	}

	private EventEnvelope processOutgoing(SpaceID spaceID, Event event, Scope scope) throws Exception {
		return processOutgoing(new EventDispatch(spaceID, event, scope));
	}

	private EventDispatch processIncomming(EventEnvelope envelope) throws Exception {
		return this.serializer.deserialize(envelope);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void run() throws Exception {
		while (isRunning()) {
			if (this.poller.getSize() > 0) {
				int signaled = this.poller.poll(1000);
				if (signaled > 0) {
					for (int i = 0; i < this.poller.getSize(); i++) {
						if (this.poller.pollin(i)) {
							this.log.fine("Polling in from " + i);
							EventEnvelope ev = EventEnvelope.recv(this.poller.getSocket(i));
							this.receive(ev);
						} else if (this.poller.pollerr(i)) {
							this.log.warning("Error in pollerr for " + this.poller.getSocket(i));
						}
					}
				}
			}
		}
		

	}

}
