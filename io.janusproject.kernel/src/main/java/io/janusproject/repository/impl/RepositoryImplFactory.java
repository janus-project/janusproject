package io.janusproject.repository.impl;

import java.util.Map;

import com.google.inject.Inject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IList;
import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;
import com.hazelcast.core.ISemaphore;
import com.hazelcast.core.ISet;
import com.hazelcast.core.MultiMap;

public class RepositoryImplFactory {

	@Inject
	private HazelcastInstance hazelcastInstance;
	
	
	public <K,V> Map<K,V> getMap(String mapName) {
		return this.hazelcastInstance.getMap(mapName);
	}
	
	public <K,V> IMap<K,V> getIMap(String mapName) {
		return this.hazelcastInstance.getMap(mapName);
	}
	
	
	public <K,V> MultiMap<K,V> getMultiMap(String mapName) {
		return this.hazelcastInstance.getMultiMap(mapName);
	}


	public <E> IQueue<E> getQueue(String name) {
		return this.hazelcastInstance.getQueue(name);
	}


	public <E> ISet<E> getSet(String name) {
		return this.hazelcastInstance.getSet(name);
	}


	public <E> IList<E> getList(String name) {
		return this.hazelcastInstance.getList(name);
	}


	public ISemaphore getSemaphore(String name) {
		return this.hazelcastInstance.getSemaphore(name);
	}
}
