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

import io.janusproject.JanusConfig;
import io.janusproject.kernel.DistributedSpace;
import io.janusproject.network.event.EventDispatch;
import io.janusproject.network.event.EventEnvelope;
import io.janusproject.network.event.EventSerializer;
import io.janusproject.services.ExecutorService;
import io.janusproject.services.KernelDiscoveryService;
import io.janusproject.services.KernelDiscoveryServiceListener;
import io.janusproject.services.LogService;
import io.janusproject.services.LogService.LogParam;
import io.janusproject.services.NetworkService;
import io.janusproject.services.NetworkServiceListener;
import io.janusproject.services.SpaceService;
import io.janusproject.services.SpaceServiceListener;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.Space;
import io.sarl.lang.core.SpaceID;

import java.io.EOFException;
import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import com.google.common.primitives.Ints;
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
class ZeroMQNetwork extends AbstractExecutionThreadService implements NetworkService {

	private final Listener serviceListener = new Listener();
	
	@Inject
	private LogService logger;

	@Inject
	private KernelDiscoveryService kernelService;

	@Inject
	private SpaceService spaceService;

	@Inject
	private ExecutorService executorService;

	@Inject
	private EventSerializer serializer;

	private ZContext context;
	private Socket publisher;
	private Map<URI, Socket> subcribers = new ConcurrentHashMap<>();
	
	private final Map<URI,NetworkEventReceivingListener> spaces = new TreeMap<>();

	// TODO Change poller that can be stopped properly.
	private Poller poller;

	private URI uriCandidate;
	private URI validatedURI = null;

	private Collection<Triplet> bufferedConnections = new ArrayList<>();

	private final List<NetworkServiceListener> listeners = new ArrayList<>();
	
	/**
	 * Construct a <code>ZeroMQNetwork</code>.
	 * 
	 * @param uri - injected URI of the PUB socket.
	 */
	@Inject
	ZeroMQNetwork(@Named(JanusConfig.PUB_URI) URI uri) {
		assert (uri != null) : "Injected URI must be not null nor empty"; //$NON-NLS-1$
		this.uriCandidate = uri;
	}

	/** {@inheritDoc}
	 */
	@Override
	public synchronized URI getURI() {
		return this.validatedURI;
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

	/** Notifies that a peer space was connected.
	 * 
	 * @param peerURI
	 * @param space
	 */
	protected void firePeerConnected(URI peerURI, SpaceID space) {
		NetworkServiceListener[] listeners;
		synchronized (this.listeners) {
			listeners = new NetworkServiceListener[this.listeners.size()];
			this.listeners.toArray(listeners);
		}
		for(NetworkServiceListener listener : listeners) {
			listener.peerConnected(peerURI, space);
		}
	}

	/** Notifies that a peer space was disconnected.
	 * 
	 * @param peerURI
	 * @param space
	 */
	protected void firePeerDisconnected(URI peerURI, SpaceID space) {
		NetworkServiceListener[] listeners;
		synchronized (this.listeners) {
			listeners = new NetworkServiceListener[this.listeners.size()];
			this.listeners.toArray(listeners);
		}
		for(NetworkServiceListener listener : listeners) {
			listener.peerDisconnected(peerURI, space);
		}
	}

	/** Notifies that a peer was discovered.
	 * 
	 * @param peerURI
	 */
	protected void firePeerDiscovered(URI peerURI) {
		NetworkServiceListener[] listeners;
		synchronized (this.listeners) {
			listeners = new NetworkServiceListener[this.listeners.size()];
			this.listeners.toArray(listeners);
		}
		for(NetworkServiceListener listener : listeners) {
			listener.peerDiscovered(peerURI);
		}
	}

	/** Notifies that a peer was disconnected.
	 * 
	 * @param peerURI
	 */
	protected void firePeerDisconnected(URI peerURI) {
		NetworkServiceListener[] listeners;
		synchronized (this.listeners) {
			listeners = new NetworkServiceListener[this.listeners.size()];
			this.listeners.toArray(listeners);
		}
		for(NetworkServiceListener listener : listeners) {
			listener.peerDisconnected(peerURI);
		}
	}

	private EventEnvelope processOutgoing(EventDispatch dispatch) throws Exception {
		return this.serializer.serialize(dispatch);
	}

	private EventEnvelope processOutgoing(SpaceID spaceID, Event event, Scope<?> scope) throws Exception {
		return processOutgoing(new EventDispatch(spaceID, event, scope));
	}

	private EventDispatch processIncomming(EventEnvelope envelope) throws Exception {
		return this.serializer.deserialize(envelope);
	}

	private void send(EventEnvelope e) {
		this.publisher.sendMore(buildFilterableHeader(e.getContextId()));
		this.publisher.sendMore(Ints.toByteArray(e.getSpaceId().length));
		this.publisher.sendMore(e.getSpaceId());
		this.publisher.sendMore(Ints.toByteArray(e.getScope().length));
		this.publisher.sendMore(e.getScope());
		this.publisher.sendMore(Ints.toByteArray(e.getCustomHeaders().length));
		this.publisher.sendMore(e.getCustomHeaders());
		this.publisher.sendMore(Ints.toByteArray(e.getBody().length));
		this.publisher.send(e.getBody());
	}

	/** Build the byte array that may be used for the ZeroMQ filtering
	 * associated with {@link Socket#subscribe(byte[])}.
	 * For a given contextID (translated into a byte array with an
	 * {@link EventSerializer}), this function must always reply the
	 * same sequence of bytes.
	 * 
	 * @param contextID
	 * @return the header of the ZeroMQ message that may be used for
	 * filtering.
	 */
	public static byte[] buildFilterableHeader(byte[] contextID) {
		byte[] header = new byte[Ints.BYTES+contextID.length];
		byte[] length = Ints.toByteArray(contextID.length);
		System.arraycopy(length, 0, header, 0, length.length);
		System.arraycopy(contextID, 0, header, length.length, contextID.length);
		return header;
	}

	/** {@inheritDoc}
	 */
	@Override
	public void publish(SpaceID id, Scope<?> scope, Event data)
			throws Exception {
		EventEnvelope env = processOutgoing(id, data, scope);
		send(env);
		this.logger.info("PUBLISH_EVENT", id, data); //$NON-NLS-1$
	}

	private static byte[] readBuffer(ByteBuffer buffer, int size) throws IOException {
		if (buffer.remaining()>=size) {
			byte[] result = new byte[size];
			buffer.get(result);
			return result;
		}
		throw new EOFException();
	}

	private static byte[] readBlock(ByteBuffer buffer) throws IOException {
		int length = Ints.fromByteArray(readBuffer(buffer, Ints.BYTES));
		return readBuffer(buffer, length);
	}

	/**
	 * Receive data from the network.
	 * 
	 * @param socket - network reader.
	 * @return the envelope received over the network.
	 * @throws IOException if the envelope cannot be read from the network.
	 */
	private static EventEnvelope extractEnvelope(Socket socket) throws IOException {
		//TODO: Read the ZeroMQ socket via a NIO wrapper to support large data: indeed the arrays has a maximal size bounded by a native int value, and the real data could be larger than this limit.

		byte[] data;
		{ 
			data = socket.recv(ZMQ.DONTWAIT);
			byte[] cdata;
			int oldSize = 0;
			while (socket.hasReceiveMore()) {
				cdata = socket.recv(ZMQ.DONTWAIT);
				oldSize = data.length;
				data = Arrays.copyOf(data, data.length + cdata.length);
				System.arraycopy(cdata, 0, data, oldSize, cdata.length);
			}
		}

		ByteBuffer buffer = ByteBuffer.wrap(data);

		byte[] contextId = readBlock(buffer);
		assert(contextId!=null && contextId.length>0);

		byte[] spaceId = readBlock(buffer);
		assert(spaceId!=null && spaceId.length>0);

		byte[] scope = readBlock(buffer);
		assert(scope!=null && scope.length>0);

		byte[] headers = readBlock(buffer);
		assert(headers!=null && headers.length>0);

		byte[] body = readBlock(buffer);
		assert(body!=null && body.length>0);

		return new EventEnvelope(contextId, spaceId, scope, headers, body);
	}

	/** {@inheritDoc}PEER_CONNECTION = Connection à {0}
	 */
	@SuppressWarnings("resource")
	@Override
	public synchronized void connectPeer(URI peerUri, SpaceID space,
			NetworkEventReceivingListener listener) throws Exception {
		if (this.validatedURI==null) {
			// Bufferizing the peerURI.
			assert(this.bufferedConnections!=null);
			this.bufferedConnections.add(new Triplet(peerUri, space, listener));
		}
		else {
			Socket subscriber = this.subcribers.get(peerUri);
			if (subscriber==null) {
				this.logger.info("PEER_CONNECTION", peerUri, space); //$NON-NLS-1$
				// Socket subscriber = this.context.socket(ZMQ.SUB);
				subscriber = this.context.createSocket(ZMQ.SUB);

				this.subcribers.put(peerUri, subscriber);
				subscriber.connect(peerUri.toString());
				this.poller.register(subscriber, Poller.POLLIN);

				this.logger.info("PEER_CONNECTED", peerUri); //$NON-NLS-1$
			}
			assert(subscriber!=null);
			byte[] header = buildFilterableHeader(
					this.serializer.serializeContextID(space.getContextID()));
			this.logger.info("PEER_SUBSCRIPTION", peerUri, space); //$NON-NLS-1$
			subscriber.subscribe(header);
		}
	}

	/** {@inheritDoc}
	 */
	@SuppressWarnings("resource")
	@Override
	public synchronized void disconnectPeer(URI peer, SpaceID space) throws Exception {
		Socket s = this.subcribers.get(peer);
		if (s!=null) {
			this.logger.info("PEER_UNSUBSCRIPTION ", peer, space); //$NON-NLS-1$
			byte[] header = buildFilterableHeader(
					this.serializer.serializeContextID(space.getContextID()));
			s.unsubscribe(header);
		}
	}

	/** {@inheritDoc}
	 */
	@SuppressWarnings("resource")
	@Override
	public synchronized void disconnectPeer(URI peer) throws Exception {
		Socket s = this.subcribers.remove(peer);
		if (s!=null) {
			this.logger.info("PEER_DISCONNECTION", peer); //$NON-NLS-1$
			this.poller.unregister(s);
			//FIXME: are the two following lines needed?
			s.close();
			this.context.destroySocket(s);
			this.logger.info("PEER_DISCONNECTED", peer); //$NON-NLS-1$
		}
	}

	/**
	 * @param env
	 * @throws Exception
	 */
	protected synchronized void receive(EventEnvelope env) throws Exception {
		this.logger.info("ENVELOPE_RECEIVED", this.validatedURI, env); //$NON-NLS-1$
		EventDispatch dispatch = processIncomming(env);
		this.logger.info("DISPATCH_RECEIVED", dispatch); //$NON-NLS-1$

		SpaceID spaceID = dispatch.getSpaceID();
		NetworkEventReceivingListener space = this.spaces.get(spaceID.getID());
		if (space != null) {
			this.executorService.submit(new AsyncRunner(
					space, spaceID,
					dispatch.getScope(), dispatch.getEvent()));
		}
		else {
			this.logger.debug("UNKNOWN_SPACE", spaceID, dispatch.getEvent()); //$NON-NLS-1$
		}
	}

	/** {@inheritDoc}
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
								this.logger.debug("POLLING", i); //$NON-NLS-1$
								EventEnvelope ev = extractEnvelope(this.poller.getSocket(i));
								assert (ev != null);

								try {
									receive(ev);
								} catch (Throwable e) {
									catchExceptionWithoutStopping(e, "CANNOT_RECEIVE_EVENT"); //$NON-NLS-1$
								}
							} else if (this.poller.pollerr(i)) {
								final int poolerIdx = i;
								this.logger.warning("POLLING_ERROR", new LogParam() { //$NON-NLS-1$
									@SuppressWarnings("synthetic-access")
									@Override
									public String toString() {
										return ZeroMQNetwork.this.poller.getSocket(poolerIdx).toString();
									}
								});
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
			this.logger.warning(errorMessageKey, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void startUp() throws Exception {
		Collection<Triplet> connections;
		synchronized(this) {
			super.startUp();
			// this.context = ZMQ.context(1);

			this.context = new ZContext();
			// this.publisher = this.context.socket(ZMQ.PUB);
			this.publisher = this.context.createSocket(ZMQ.PUB);
			String strUri = this.uriCandidate.toString();
			int port = this.publisher.bind(strUri);
			if (port >= 0 && strUri.endsWith(":*")) { //$NON-NLS-1$
				String prefix = strUri.substring(0, strUri.lastIndexOf(':') + 1);
				this.validatedURI = new URI(prefix + port);
			} else {
				this.validatedURI = this.uriCandidate;
			}
			System.setProperty(JanusConfig.PUB_URI, this.validatedURI.toString());
			this.logger.info("ZEROMQ_BINDED", this.validatedURI); //$NON-NLS-1$
			this.uriCandidate = null;
			connections = this.bufferedConnections;
			this.bufferedConnections = null;
			this.poller = new Poller(0);
			
			this.kernelService.addKernelDiscoveryServiceListener(this.serviceListener);
			this.spaceService.addSpaceServiceListener(this.serviceListener);
		}
		for(Triplet t : connections) {
			connectPeer(t.peerURI, t.spaceID, t.listener);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected synchronized void shutDown() throws Exception {
		this.kernelService.removeKernelDiscoveryServiceListener(this.serviceListener);
		this.spaceService.removeSpaceServiceListener(this.serviceListener);

		// TODO this.poller.stop();
		// stopPoller();

		// this.publisher.close();

		this.context.destroy();
		this.logger.info("ZEROMQ_SHUTDOWN"); //$NON-NLS-1$
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private static class Triplet {

		/** URI of the peer.
		 */
		public final URI peerURI;

		/** ID of the space.
		 */
		public final SpaceID spaceID;

		/** Reception listener.
		 */
		public final NetworkEventReceivingListener listener;

		/**
		 * @param peerURI
		 * @param spaceID
		 * @param listener
		 */
		public Triplet(URI peerURI, SpaceID spaceID, NetworkEventReceivingListener listener) {
			this.peerURI = peerURI;
			this.spaceID = spaceID;
			this.listener = listener;
		}

	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class AsyncRunner implements Runnable {
		
		private final NetworkEventReceivingListener space;
		private final SpaceID spaceID;
		private final Scope<?> scope;
		private final Event event;
		
		public AsyncRunner(NetworkEventReceivingListener space, SpaceID spaceID, Scope<?> scope, Event event) {
			this.space = space;
			this.spaceID = spaceID;
			this.scope = scope;
			this.event = event;
		}
		
		@Override
		public void run() {
			this.space.eventReceived(this.spaceID, this.scope, this.event);
		}
	}

	/**
	 * @author $Author: sgalland$
	 * @version $FullVersion$
	 * @mavengroupid $GroupId$
	 * @mavenartifactid $ArtifactId$
	 */
	private class Listener implements SpaceServiceListener, KernelDiscoveryServiceListener {

		/**
		 */
		public Listener() {
			//
		}

		/** {@inheritDoc}
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void spaceCreated(Space space) {
			try {
				for(URI peer : ZeroMQNetwork.this.kernelService.getKernels()) {
					if (!peer.equals(ZeroMQNetwork.this.getURI())) {
						if (space instanceof DistributedSpace) {
							connectPeer(peer, space.getID(), ((DistributedSpace)space).getNetworkProxy());
						}
						else {
							ZeroMQNetwork.this.logger.error(ZeroMQNetwork.class, "NOT_DISTRIBUTABLE_SPACE", space); //$NON-NLS-1$
						}
					}
				}
			}
			catch (Exception e) {
				ZeroMQNetwork.this.logger.error(ZeroMQNetwork.class, "UNEXPECTED_EXCEPTION", e); //$NON-NLS-1$
			}
		}

		/** {@inheritDoc}
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void spaceDestroyed(Space space) {
			try {
				for(URI peer : ZeroMQNetwork.this.kernelService.getKernels()) {
					if (!peer.equals(ZeroMQNetwork.this.getURI())) {
						disconnectPeer(peer, space.getID());
					}
				}
			}
			catch (Exception e) {
				ZeroMQNetwork.this.logger.error(ZeroMQNetwork.class, "UNEXPECTED_EXCEPTION", e); //$NON-NLS-1$
			}
		}

		/** {@inheritDoc}
		 */
		@Override
		public void kernelDiscovered(URI peerURI) {
			//
		}

		/** {@inheritDoc}
		 */
		@SuppressWarnings("synthetic-access")
		@Override
		public void kernelDisconnected(URI peerURI) {
			try {
				disconnectPeer(peerURI);
			}
			catch (Exception e) {
				ZeroMQNetwork.this.logger.error(ZeroMQNetwork.class, "UNEXPECTED_EXCEPTION", e); //$NON-NLS-1$
			}
		}

	}

}
