package io.janusproject.repository;

import io.janusproject.repository.impl.RepositoryImplFactory;
import io.sarl.lang.core.AgentContext;
import io.sarl.lang.core.SpaceID;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * A repository of Agent's Context
 * 
 * @author $Author: ngaud$
 * @author $Author: srodriguez$
 * 
 */
public class ContextRepository {

	/**
	 * Map linking a context id to its related Context object This is local
	 * non-distributed map
	 */
	private final Map<UUID, AgentContext> contexts;

	/**
	 * Map linking a context id to its associated default space id This map must
	 * be distributed and synchronized all over the network
	 */
	private Map<UUID, SpaceID> spaces;



	public ContextRepository() {
		this.contexts = new ConcurrentHashMap<UUID, AgentContext>();
		
	}
	
	@Inject
	void setJanusID(@Named(JanusConfig.JANUS_CONTEXT_ID) UUID janusID, RepositoryImplFactory repositoryImplFactory){
		this.spaces = repositoryImplFactory.getMap(janusID.toString());
	}

	/**
	 * Does this repository contain some context
	 * 
	 * @return true if this repository contains no context, false otherwise
	 */
	public boolean isEmpty() {
		return this.contexts.isEmpty();
	}

	/**
	 * Returns the number of context registered in this repository
	 * 
	 * @return the number of context registered in this repository
	 */
	public int numberOfRegisteredContexts() {
		return this.contexts.size();
	}

	/**
	 * Check if this repository contains a context with the specified id
	 * 
	 * @param contextID
	 *            - the id to test
	 * @return true if this repository contains a context with the specified id,
	 *         false otherwise
	 */
	public boolean containsContext(UUID contextID) {
		return this.contexts.containsKey(contextID);
	}

	/**
	 * Add a new context to this repository as well as its default context to
	 * the list of related spaces
	 * 
	 * @param context
	 *            - the context to add
	 */
	public void addContext(AgentContext context) {
		this.spaces.put(context.getID(), context.getDefaultSpace().getID());
		this.contexts.put(context.getID(), context);
	}

	/**
	 * Remove the specified context from this repository
	 * 
	 * @param context
	 *            - the context to remove
	 */
	public void removeContext(AgentContext context) {
		this.removeContext(context.getID());
	}

	/**
	 * Remove the context with the specified id from this repository
	 * 
	 * @param contextID
	 *            - the id of the context to remove
	 */
	public void removeContext(UUID contextID) {
		this.spaces.remove(contextID);
		this.contexts.remove(contextID);
	}

	/**
	 * Clear the context of this repository
	 */
	public void clearRepository() {
		this.spaces.clear();
		this.contexts.clear();
	}

	/**
	 * Returns the collection of all agent's contexts stored in this repository
	 * 
	 * @return the collection of all agent's contexts stored in this repository
	 */
	public Collection<AgentContext> getContexts() {
		return this.contexts.values();
	}

	/**
	 * Returns the set of all agent context IDs stored in this repository
	 * 
	 * @return the set of all agent context IDs stored in this repository
	 */
	public Set<UUID> getContextIDs() {
		return this.contexts.keySet();
	}

	/**
	 * Returns the {@link AgentContext} with the given ID
	 * 
	 * @param contextID
	 * @return the {@link AgentContext} with the given ID
	 */
	public AgentContext getContext(UUID contextID) {
		return this.contexts.get(contextID);
	}
	
	/**
	 * Returns the collection of {@link AgentContext} with the given IDs
	 * @param contextIDs
	 * @return the collection of {@link AgentContext} with the given IDs
	 */
	public Collection<AgentContext> getContexts(final Collection<UUID> contextIDs){
		return Collections2.filter(this.contexts.values(),new Predicate<AgentContext>() {
			@Override
			public boolean apply(AgentContext input) {	
				return contextIDs.contains(input.getID());
			}
		} );
		
	}

}
