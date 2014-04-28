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
package io.janusproject.kernel.bic;

import io.janusproject.services.ContextSpaceService;
import io.sarl.core.InnerContextAccess;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Skill;
import io.sarl.util.OpenEventSpace;

import java.util.UUID;

import com.google.inject.Inject;

/** Janus implementation of SARL's {@link InnerContextSkill} built-in capacity.
 * 
 * @author $Author: srodriguez$
 * @author $Author: ngaud$
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class InnerContextSkill extends Skill implements InnerContextAccess {
	
	/**
	 * Context inside the agent. 
	 */
	private AgentContext innerContext = null;

	@Inject
	private ContextSpaceService contextService;

	/**
	 * @param agent
	 */
	public InnerContextSkill(Agent agent) {
		super(agent);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void uninstall() {
		AgentContext context = this.innerContext;
		this.innerContext = null;
		if (context!=null) {
			this.contextService.removeContext(context);
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public synchronized AgentContext getInnerContext() {
		if (this.innerContext==null) {			
			this.innerContext = this.contextService.createContext(getOwner().getID(), UUID.randomUUID());
			((OpenEventSpace)this.innerContext.getDefaultSpace()).register(getSkill(EventBusCapacity.class).asEventListener());
		}
		return this.innerContext;
	}

}
