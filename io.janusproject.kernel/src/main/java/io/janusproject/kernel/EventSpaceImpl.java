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

import io.janusproject.repository.UniqueAddressParticipantRepository;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.SpaceID;
import io.sarl.util.OpenEventSpace;
import io.sarl.util.Scopes;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.arakhne.afc.vmutil.locale.Locale;

import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * Default implementation of an event space.
 * 
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class EventSpaceImpl extends SpaceBase implements OpenEventSpace {

	private UniqueAddressParticipantRepository<Address> participants;

	private Network network;

	@Inject
	private Logger log;

	@Inject
	private ExecutorService executorService;

	/**
	 * Constructs an event space.
	 * 
	 * @param id - identifier of the space.
	 */
	public EventSpaceImpl(SpaceID id) {
		super(id);
	}

	/**
	 * 
	 * @param injector
	 */
	@Inject
	void setInjector(Injector injector) {
		this.participants = new UniqueAddressParticipantRepository<>(getID().getID().toString() + "-participants"); //$NON-NLS-1$
		injector.injectMembers(this.participants);
	}

	/**
	 * Set the network service to be used by this space to be distributed over the network.
	 * 
	 * @param net - instance of the network service.
	 * @throws Exception
	 */
	@Inject
	void setNetwork(Network net) throws Exception {
		this.network = net;
		this.network.register(new DistributedProxy(this));
	}

	/**
	 * Replies the address associated to the given participant.
	 * 
	 * @param entity - instance of a participant.
	 * @return the address of the participant with the given id.
	 */
	public final Address getAddress(EventListener entity) {
		return getAddress(entity.getID());
	}

	@Override
	public Address getAddress(UUID id) {
		return this.participants.getAddress(id);
	}

	@Override
	public Address register(EventListener entity) {
		Address a = new Address(getID(), entity.getID());
		return this.participants.registerParticipant(a, entity);
	}

	@Override
	public Address unregister(EventListener entity) {
		return this.participants.unregisterParticipant(entity);
	}

	@Override
	public void emit(Event event, Scope<Address> scope) {
		assert (event != null);
		assert (event.getSource() != null) : "Every event must have a source"; //$NON-NLS-1$
		assert this.getID().equals(event.getSource().getSpaceId()) : "The source address must belong to this space"; //$NON-NLS-1$

		try {
			this.network.publish(this.getID(), scope, event);
			doEmit(event, scope);
		} catch (Exception e) {
			this.log.log(Level.SEVERE, e.getMessage(), e);
			e.printStackTrace();
		}

	}

	@Override
	public void emit(Event event) {
		this.emit(event, Scopes.<Address> allParticipants());
	}

	private void doEmit(final Event event, final Scope<Address> scope) {

		for (final EventListener agent : this.participants.getListeners()) {
			if (scope.matches(this.getAddress(agent))) {
				// TODO Verify the agent is still alive and running
				this.executorService.execute(new Runnable() {
					@Override
					public void run() {
						agent.receiveEvent(event);
					}
				});

			}
		}
	}

	/**
	 * Invoked when an event was not handled by a listener.
	 * 
	 * @param e - dead event
	 */
	@Subscribe
	public void unhandledEvent(DeadEvent e) {
		this.log.finer(Locale.getString("UNHANDLED_EVENT", //$NON-NLS-1$
				getID(), ((Event) e.getEvent()).getSource(), e.getEvent()));
	}

	private static class DistributedProxy implements DistributedSpace {

		private EventSpaceImpl space;

		DistributedProxy(EventSpaceImpl space) {
			this.space = space;
		}

		@Override
		public SpaceID getID() {
			return this.space.getID();
		}

		@SuppressWarnings("synthetic-access")
		@Override
		public void recv(Scope<?> scope, Event event) {
			this.space.doEmit(event, (Scope<Address>) scope);
		}

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<UUID> getParticipants() {
		return this.participants.getParticipantIDs();
	}

}
