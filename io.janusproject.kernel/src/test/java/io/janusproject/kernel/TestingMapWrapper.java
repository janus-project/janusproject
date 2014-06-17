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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import com.hazelcast.core.EntryListener;
import com.hazelcast.core.EntryView;
import com.hazelcast.core.ExecutionCallback;
import com.hazelcast.core.IMap;
import com.hazelcast.map.EntryProcessor;
import com.hazelcast.map.MapInterceptor;
import com.hazelcast.mapreduce.JobTracker;
import com.hazelcast.mapreduce.aggregation.Aggregation;
import com.hazelcast.mapreduce.aggregation.Supplier;
import com.hazelcast.monitor.LocalMapStats;
import com.hazelcast.query.Predicate;


/**
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("rawtypes")
class TestingMapWrapper extends HashMap<Object,Object> implements IMap<Object,Object> {

	private static final long serialVersionUID = -1238373881805960849L;

	/**
	 */
	public TestingMapWrapper() {
		//
	}

	/** {@inheritDoc}
	 */
	@Override
	public Object getId() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public String getPartitionKey() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public String getServiceName() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void destroy() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean remove(Object key, Object value) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void delete(Object key) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void flush() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Map<Object, Object> getAll(Set<Object> keys) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Future<Object> getAsync(Object key) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Future<Object> putAsync(Object key, Object value) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Future<Object> putAsync(Object key, Object value, long ttl,
			TimeUnit timeunit) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Future<Object> removeAsync(Object key) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean tryRemove(Object key, long timeout, TimeUnit timeunit) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean tryPut(Object key, Object value, long timeout,
			TimeUnit timeunit) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Object put(Object key, Object value, long ttl, TimeUnit timeunit) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void putTransient(Object key, Object value, long ttl,
			TimeUnit timeunit) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Object putIfAbsent(Object key, Object value) {
		if (containsKey(key)) 
			return get(key);
		return put(key,value);
	}

	/** {@inheritDoc}
	 */
	@Override
	public Object putIfAbsent(Object key, Object value, long ttl,
			TimeUnit timeunit) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean replace(Object key, Object oldValue, Object newValue) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Object replace(Object key, Object value) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void set(Object key, Object value) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void set(Object key, Object value, long ttl, TimeUnit timeunit) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void lock(Object key) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void lock(Object key, long leaseTime, TimeUnit timeUnit) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean isLocked(Object key) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean tryLock(Object key) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean tryLock(Object key, long time, TimeUnit timeunit)
			throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void unlock(Object key) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void forceUnlock(Object key) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public String addLocalEntryListener(
			EntryListener<Object, Object> listener) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public String addInterceptor(MapInterceptor interceptor) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void removeInterceptor(String id) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public String addEntryListener(EntryListener<Object, Object> listener,
			boolean includeValue) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean removeEntryListener(String id) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public String addEntryListener(EntryListener<Object, Object> listener,
			Object key, boolean includeValue) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public String addEntryListener(EntryListener<Object, Object> listener,
			Predicate<Object, Object> predicate, boolean includeValue) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public String addEntryListener(EntryListener<Object, Object> listener,
			Predicate<Object, Object> predicate, Object key,
			boolean includeValue) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public EntryView<Object, Object> getEntryView(Object key) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public boolean evict(Object key) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Set<Object> keySet(Predicate predicate) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Set<java.util.Map.Entry<Object, Object>> entrySet(
			Predicate predicate) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Collection<Object> values(Predicate predicate) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Set<Object> localKeySet() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Set<Object> localKeySet(Predicate predicate) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void addIndex(String attribute, boolean ordered) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public LocalMapStats getLocalMapStats() {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Object executeOnKey(Object key, EntryProcessor entryProcessor) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Map<Object, Object> executeOnEntries(
			EntryProcessor entryProcessor) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public String addLocalEntryListener(EntryListener<Object, Object> arg0,
			Predicate<Object, Object> arg1, boolean arg2) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public String addLocalEntryListener(EntryListener<Object, Object> arg0,
			Predicate<Object, Object> arg1, Object arg2, boolean arg3) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Map<Object, Object> executeOnEntries(EntryProcessor arg0,
			Predicate arg1) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Map<Object, Object> executeOnKeys(Set<Object> arg0,
			EntryProcessor arg1) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public Future submitToKey(Object arg0, EntryProcessor arg1) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public void submitToKey(Object arg0, EntryProcessor arg1,
			ExecutionCallback arg2) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public <SuppliedValue, Result> Result aggregate(
			Supplier<Object, Object, SuppliedValue> arg0,
			Aggregation<Object, SuppliedValue, Result> arg1) {
		throw new UnsupportedOperationException();
	}

	/** {@inheritDoc}
	 */
	@Override
	public <SuppliedValue, Result> Result aggregate(
			Supplier<Object, Object, SuppliedValue> arg0,
			Aggregation<Object, SuppliedValue, Result> arg1, JobTracker arg2) {
		throw new UnsupportedOperationException();
	}

}
