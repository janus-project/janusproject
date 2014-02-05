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
package io.janusproject.kernel;

import io.janusproject.repository.UniqueAddressParticipantRepository;
import io.sarl.lang.core.Address;
import io.sarl.lang.core.Event;
import io.sarl.lang.core.EventListener;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.SpaceID;
import io.sarl.util.OpenEventSpace;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.DeadEvent;
import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class EventSpaceImpl extends SpaceBase implements OpenEventSpace {

	private UniqueAddressParticipantRepository<Address> participants;

	private Network network;

	@Inject
	private Logger log;

	@Inject
	private ExecutorService executorService;


	public EventSpaceImpl(SpaceID id) {
		super(id);

	}

	@Inject
	void setInjector(Injector injector) {
		this.participants = new UniqueAddressParticipantRepository<>(getID().getID().toString() + "-participants");
		injector.injectMembers(this.participants);
	}

	@Inject
	void setNetwork(Network net) throws Exception {
		this.network = net;
		this.network.register(new DistributedProxy(this));
	}

	public Address getAddress(EventListener entity) {
		return getAddress(entity.getID());
	}

	public Address getAddress(UUID id) {
		return this.participants.getAddress(id);
	}

	/**
	 * {@inheritDoc}
	 * 
	 */
	public Address register(EventListener entity) {
		Address a = new Address(getID(), entity.getID());
		return this.participants.registerParticipant(a, entity);
	}

	/**
	 * {@inheritDoc}
	 */
	public Address unregister(EventListener entity) {
		return this.participants.unregisterParticipant(entity);
	}

	/**
	 * {@inheritDoc}
	 */

	@Override
	public void emit(Event event, final Scope scope) {
		Preconditions.checkNotNull(event);
		Preconditions.checkNotNull(event.getSource(), "Every event must have a source");
		Preconditions.checkArgument(this.getID().equals(event.getSource().getSpaceId()),
				"The source address must belong to this space");

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
		this.emit(event, Scopes.<Address> nullScope());
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

	@Subscribe
	public void unhandledEvent(DeadEvent e) {
		System.out.println("----- DeadEvent : My SpaceID : " + getID());
		System.out.println("----- DeadEvent Source : " + ((Event) e.getEvent()).getSource());
		System.out.println("----- DeadEvent : " + e.getEvent());
	}

	private static class DistributedProxy implements DistributedSpace {

		private EventSpaceImpl space;

		DistributedProxy(EventSpaceImpl space) {
			this.space = space;
		}

		/**
		 * {@inheritDoc}
		 */
		public SpaceID getID() {
			return this.space.getID();
		}

		/**
		 * {@inheritDoc}
		 */
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
