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
package io.janusproject.network.zeromq;

import static io.janusproject.JanusConfig.PUB_URI;
import io.janusproject.JanusConfig;
import io.janusproject.kernel.ContextRepository;
import io.janusproject.kernel.DistributedSpace;
import io.janusproject.kernel.Network;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.SpaceID;
import io.sarl.util.Scopes;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arakhne.afc.vmutil.locale.Locale;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

/**
 * Service that is providing the ZeroMQ network.
 * 
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @author $Author: ngaud$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@Singleton
class ZeroMQNetwork extends AbstractExecutionThreadService implements Network {

	private ZContext context;
	private Socket publisher;

	private static final String CMD_DISCOVER = "DICOVER_SPACE"; //$NON-NLS-1$

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

	private String uriCandidate;
	private volatile String validatedURI = null;

	/**
	 * Construct a <code>ZeroMQNetwork</code>.
	 * 
	 * @param uri - injected URI of the PUB socket.
	 */
	@Inject
	ZeroMQNetwork(@Named(PUB_URI) String uri) {
		assert (uri != null && !uri.isEmpty()) : "Injected URI must be not null nor empty"; //$NON-NLS-1$
		this.uriCandidate = uri;
	}

	@Override
	public void connectPeer(String peerURI) throws Exception {

		this.log.info(Locale.getString("PEER_CONNECTION", peerURI)); //$NON-NLS-1$
		// Socket subscriber = this.context.socket(ZMQ.SUB);
		@SuppressWarnings("resource")
		Socket subscriber = this.context.createSocket(ZMQ.SUB);

		this.subcribers.put(peerURI, subscriber);
		for (SpaceID sid : this.spaces.keySet()) {
			subscriber.subscribe(
					EventEnvelope.buildFilterableHeader(
							this.serializer.serializeContextID(sid.getContextID())));
		}
		subscriber.connect(peerURI);
		this.poller.register(subscriber, Poller.POLLIN);

		this.log.info(Locale.getString("PEER_CONNECTED", peerURI)); //$NON-NLS-1$

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getURI() {
		return this.validatedURI;
	}

	@Override
	public void disconnectPeer(String peerURI) throws Exception {
		this.log.info(Locale.getString("PEER_DISCONNECTION", peerURI)); //$NON-NLS-1$
		@SuppressWarnings("resource")
		Socket s = this.subcribers.get(peerURI);
		this.poller.unregister(s);
		// FIXME s.close();
		// this.context.destroySocket(s);
		this.log.info(Locale.getString("PEER_DISCONNECTED", peerURI)); //$NON-NLS-1$

	}

	@Override
	public void register(DistributedSpace space) throws Exception {

		this.log.info(Locale.getString("REGISTERING_DISTRIBUTED_SPACE", space.getID())); //$NON-NLS-1$
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
		EventDispatch d = new EventDispatch(space.getID(), NoEvent.INSTANCE, Scopes.allParticipants());
		d.getHeaders().put("x-netcmd", CMD_DISCOVER); //$NON-NLS-1$

		EventEnvelope command = processOutgoing(d);
		command.send(this.publisher);

	}

	@Override
	public void publish(SpaceID spaceID, Scope<?> scope, Event e) throws Exception {
		EventEnvelope env = processOutgoing(spaceID, e, scope);
		env.send(this.publisher);
		this.log.info(Locale.getString("PUBLISH_EVENT", spaceID, e)); //$NON-NLS-1$

	}

	/**
	 * @param env
	 * @throws Exception
	 */
	void receive(EventEnvelope env) throws Exception {
		this.log.info(Locale.getString("ENVELOPE_RECEIVED", this.validatedURI, env)); //$NON-NLS-1$
		final EventDispatch dispatch = processIncomming(env);

		DistributedSpace space = this.spaces.get(dispatch.getSpaceID());

		if (space == null) {
			SpaceID spaceID = dispatch.getSpaceID();
			this.contextRepository.getContext(spaceID.getContextID()).createSpace(spaceID.getSpaceSpecification(), spaceID.getID());
			// XXX: use assert? I'm not sure: is it for testing synchronization
			// state at runtime?
			Preconditions.checkNotNull(this.spaces.get(spaceID), "Space ( %s ) was not created on time", spaceID); //$NON-NLS-1$
			// FIXME: Improve before release
			space = this.spaces.get(spaceID);
		}
		String cmd = dispatch.getHeaders().get("x-netcmd"); //$NON-NLS-1$
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
		super.startUp();
		// this.context = ZMQ.context(1);

		this.context = new ZContext();
		// this.publisher = this.context.socket(ZMQ.PUB);
		this.publisher = this.context.createSocket(ZMQ.PUB);
		int port = this.publisher.bind(this.uriCandidate);
		if (port >= 0 && this.uriCandidate.endsWith(":*")) { //$NON-NLS-1$
			String prefix = this.uriCandidate.substring(0, this.uriCandidate.lastIndexOf(':') + 1);
			this.validatedURI = prefix + port;
		} else {
			this.validatedURI = this.uriCandidate;
		}
		System.setProperty(JanusConfig.PUB_URI, this.validatedURI);
		this.log.info(Locale.getString("ZEROMQ_BINDED", this.validatedURI)); //$NON-NLS-1$
		this.uriCandidate = null;
		this.poller = new Poller(0);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void shutDown() throws Exception {
		// TODO this.poller.stop();
		// stopPoller();

		// this.publisher.close();

		this.context.destroy();
		this.log.info(Locale.getString("ZEROMQ_SHUTDOWN")); //$NON-NLS-1$
	}

	// private void stopPoller() {
	//		this.log.info(Locale.getString("STOPPING_POLLER")); //$NON-NLS-1$
	// for (int i = 0; i < this.poller.getSize(); i++) {
	// @SuppressWarnings("resource")
	// Socket socket = this.poller.getSocket(i);
	// this.poller.unregister(socket);
	// socket.close();
	// }
	// }

	private EventEnvelope processOutgoing(EventDispatch dispatch) throws Exception {
		return this.serializer.serialize(dispatch);
	}

	private EventEnvelope processOutgoing(SpaceID spaceID, Event event, Scope<?> scope) throws Exception {
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
			try {
				if (this.poller.getSize() > 0) {
					int signaled = this.poller.poll(1000);
					if (signaled > 0) {
						for (int i = 0; i < this.poller.getSize(); i++) {
							if (this.poller.pollin(i)) {
								this.log.info(Locale.getString("POLLING", i)); //$NON-NLS-1$
								EventEnvelope ev = EventEnvelope.recv(this.poller.getSocket(i));
								assert (ev != null);

								try {
									this.receive(ev);
								} catch (Throwable e) {
									catchExceptionWithoutStopping(e, "CANNOT_RECEIVE_EVENT"); //$NON-NLS-1$
								}
							} else if (this.poller.pollerr(i)) {
								this.log.warning(Locale.getString("POLLING_ERROR", this.poller.getSocket(i))); //$NON-NLS-1$
							}
						}
					}
				}
			} catch (Throwable e) {
				catchExceptionWithoutStopping(e, "UNEXPECTED_EXCEPTION"); //$NON-NLS-1$
			}
		}
		// FIXME: May the poller be stopped?
		// stopPoller();
	}

	private void catchExceptionWithoutStopping(Throwable e, String errorMessageKey) {
		// Catch the deserialization or unencrypting exceptions
		// Notify the default listener if one, but do not
		// stop the thread.
		UncaughtExceptionHandler h = Thread.getDefaultUncaughtExceptionHandler();
		if (h != null) {
			try {
				h.uncaughtException(Thread.currentThread(), e);
			} catch (Throwable _) {
				// Ignore the exception according to the
				// document of the function
				// UncaughtExceptionHandler.uncaughtException()
			}
		} else {
			this.log.log(Level.WARNING, Locale.getString(errorMessageKey, e.getLocalizedMessage()), e);
		}
	}
}
