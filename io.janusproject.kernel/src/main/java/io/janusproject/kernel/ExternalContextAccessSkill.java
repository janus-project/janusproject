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

import io.janusproject.repository.ContextRepository;
import io.sarl.core.Behaviors;
import io.sarl.core.ExternalContextAccess;
import io.sarl.lang.core.Agent;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.Skill;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import org.arakhne.afc.vmutil.locale.Locale;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

/**
 * @author $Author: srodriguez$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
class ExternalContextAccessSkill extends Skill implements ExternalContextAccess{

	private Set<UUID> contexts = Sets.newConcurrentHashSet();
	
	
	private ContextRepository contextRepository;
	/**
	 * @param agent
	 */
	public ExternalContextAccessSkill(Agent agent, ContextRepository contextRepository) {
		super(agent);
		this.contextRepository = contextRepository;
	}
	
	/** {@inheritDoc}
	 */
	@Override
	protected void install() {
		super.install();
		AgentContext ac = this.contextRepository.getContext(getOwner().getParentID());
		this.join(ac.getID(), ac.getDefaultSpace().getID().getID());	
	}
	
	/** {@inheritDoc}
	 */
	@Override
	protected void uninstall() {
		//Leave all contexts including the default one.
		for (UUID contextID : this.contexts) {
			this.leave(contextID);
		}
		super.uninstall();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Collection<AgentContext> getAllContexts() {
		return this.contextRepository.getContexts(this.contexts);
	}

	/** {@inheritDoc}
	 */
	@Override
	public AgentContext getContext(UUID contextID) {
		Preconditions.checkNotNull(contextID);
		if(!this.contexts.contains(contextID)){
			throw new IllegalArgumentException(Locale.getString("UNKNOWN_CONTEXT_ID", contextID)); //$NON-NLS-1$
		}
		return this.contextRepository.getContext(contextID);
	}

	/** {@inheritDoc}
	 */
	@Override
	public void join(UUID futureContext, UUID futureContextDefaultSpaceID) {
		Preconditions.checkNotNull(futureContext);
		Preconditions.checkNotNull(futureContextDefaultSpaceID);
		
		AgentContext ac = this.contextRepository.getContext(futureContext);
		
		Preconditions.checkNotNull(ac, "Unknown Context"); //$NON-NLS-1$
		
		if(this.contexts.contains(futureContext)){			
			return;
		}
		
		
		if(ac.getDefaultSpace().getID().getID() != futureContextDefaultSpaceID){
			throw new IllegalArgumentException(Locale.getString("INVALID_DEFAULT_SPACE_MATCHING", futureContextDefaultSpaceID)); //$NON-NLS-1$
		}
		
		this.contexts.add(futureContext);
		BehaviorsAndInnerContextSkill imp = (BehaviorsAndInnerContextSkill) getSkill(Behaviors.class);
		imp.registerOnDefaultSpace((EventSpaceImpl) ac.getDefaultSpace());
	}

	/** {@inheritDoc}
	 */
	@Override
	public void leave(UUID contextID) {
		Preconditions.checkNotNull(contextID);
		AgentContext ac = this.contextRepository.getContext(contextID);
		Preconditions.checkNotNull(ac, "Unknown Context"); //$NON-NLS-1$
		if(!this.contexts.contains(contextID)){
			return;
		}
		BehaviorsAndInnerContextSkill imp = (BehaviorsAndInnerContextSkill) getSkill(Behaviors.class);
		imp.unregisterFromDefaultSpace((EventSpaceImpl) ac.getDefaultSpace());
		this.contexts.remove(contextID);
		
	}

	
}
