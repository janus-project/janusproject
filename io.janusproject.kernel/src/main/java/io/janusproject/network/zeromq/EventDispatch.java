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

import io.sarl.lang.core.Event;
import io.sarl.lang.core.Scope;
import io.sarl.lang.core.SpaceID;

import java.util.HashMap;
import java.util.Map;

/**
 * @author $Author: Sebastian Rodriguez$
 * @version $Name$ $Revision$ $Date$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public class EventDispatch {
	private final Scope<?> scope;
	private final Event event;
	private final SpaceID spaceID;
	private Map<String, String> headers;
	
	public EventDispatch(SpaceID sid, Event event, Scope<?> scope, Map<String, String> headers){
		this.spaceID = sid;
		this.event = event;
		this.scope = scope;
		this.headers = headers;
		
	}
	public EventDispatch(SpaceID sid, Event event, Scope<?> scope){
		this(sid,event,scope,new HashMap<String, String>());
	}
	
	

	/**
	 * @return
	 */
	public Event getEvent() {
		return this.event;
	}
	
	public Scope<?> getScope(){
		return this.scope;
	}
	
	public Map<String, String> getHeaders(){
		return this.headers;
	}
	
	public SpaceID getSpaceID(){
		return this.spaceID;
	}
}
