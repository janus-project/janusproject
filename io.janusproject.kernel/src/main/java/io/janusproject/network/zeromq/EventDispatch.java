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
