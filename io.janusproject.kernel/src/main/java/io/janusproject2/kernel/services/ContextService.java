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
package io.janusproject2.kernel.services;

import io.sarl.lang.core.AgentContext;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/** This service enables to store the contexts in the janus platform.
 * 
 * @author $Author: srodriguez$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
public interface ContextService extends JanusService {

	/**
	 * Does this repository contain some context
	 * 
	 * @return <code>true</code> if this repository contains no context,
	 * <code>false</code> otherwise
	 */
	public boolean isEmptyContextRepository();

	/**
	 * Returns the number of context registered in this repository
	 * 
	 * @return the number of context registered in this repository
	 */
	public int getNumberOfContexts();

	/**
	 * Check if this repository contains a context with the specified id
	 * 
	 * @param contextID
	 *            - the id to test
	 * @return <code>true</code> if this repository contains a context with
	 * the specified id, <code>false</code> otherwise
	 */
	public boolean containsContext(UUID contextID);

	/**
	 * Add a new context to this repository
	 * 
	 * @param context
	 *            - the context to add
	 */
	public void addContext(AgentContext context);

	/**
	 * Remove the specified context from this repository
	 * 
	 * @param context
	 *            - the context to remove
	 */
	public void removeContext(AgentContext context);

	/**
	 * Remove the context with the specified id from this repository
	 * 
	 * @param contextID
	 *            - the id of the context to remove
	 */
	public void removeContext(UUID contextID);

	/**
	 * Clear the context of this repository
	 */
	public void removeAllContexts();

	/**
	 * Returns the collection of all agent's contexts stored in this repository
	 * 
	 * @return the collection of all agent's contexts stored in this repository
	 */
	public Collection<AgentContext> getContexts();

	/**
	 * Returns the set of all agent context IDs stored in this repository
	 * 
	 * @return the set of all agent context IDs stored in this repository
	 */
	public Set<UUID> getContextIDs();

	/**
	 * Returns the {@link AgentContext} with the given ID
	 * 
	 * @param contextID
	 * @return the {@link AgentContext} with the given ID
	 */
	public AgentContext getContext(UUID contextID);
	
	/**
	 * Returns the collection of {@link AgentContext} with the given IDs
	 * @param contextIDs
	 * @return the collection of {@link AgentContext} with the given IDs
	 */
	public Collection<AgentContext> getContexts(Collection<UUID> contextIDs);
	
	/** Add a listener on the context service events.
	 * 
	 * @param listener
	 */
	public void addContextServiceListener(ContextServiceListener listener);

	/** Remove a listener on the context service events.
	 * 
	 * @param listener
	 */
	public void removeContextServiceListener(ContextServiceListener listener);

}
