/*
 * $Id$
 *
 * Janus platform is an open-source multiagent platform.
 * More details on http://www.janusproject.io
 *
 * Copyright (C) 2014-2015 the original authors or authors.
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

package io.janusproject.kernel.bic.internaleventdispatching;

import static com.google.common.base.Preconditions.checkNotNull;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

import org.eclipse.xtext.xbase.lib.Pair;

import io.sarl.lang.core.DeadEvent;
import io.sarl.lang.core.Event;

/**
 * The class in charge of dispatching every single events coming from the outside of this agent (i.e. from a space) or from an
 * agent's behavior.
 *
 * @author $Author: ngaud$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 *
 */
public class AgentInternalEventsDispatcher {

	/**
	 * The registry of all {@code BehaviorGuardEvaluator} classes containing a method to evaluate the guard of a given behavior
	 * (on clause in SARL behavior). This class has been inspired by the com.google.common.eventbus.SuscriberRegistry class of
	 * Google Guava library.
	 */
	private final BehaviorGuardEvaluatorRegistry behaviorGuardEvaluatorRegistry;

	/**
	 * The executor used to execute behavior methods in dedicated thread.
	 */
	private final Executor executor;

	/**
	 * Per-thread queue of events to dispatch.
	 */
	private final ThreadLocal<Queue<Pair<Object, Collection<Method>>>> queue =
	  new ThreadLocal<Queue<Pair<Object, Collection<Method>>>>() {
			@Override
			protected Queue<Pair<Object, Collection<Method>>> initialValue() {
				return Queues.newArrayDeque();
			}
	};

	/**
	 * Per-thread dispatch state, used to avoid reentrant event dispatching.
	 */
	private final ThreadLocal<Boolean> dispatching = new ThreadLocal<Boolean>() {
		@Override
		protected Boolean initialValue() {
			return Boolean.FALSE;
		}
	};

	/**
	 * Instantiates a dispatcher.
	 *
	 * @param executor - the executor used to execute behavior methods in dedicated thread.
	 * @param perceptGuardEvaluatorAnnotation - The annotation used to identify methods considered as the evaluator of the guard
	 *        of a given behavior (on clause in SARL behavior) If class has a such method, it is considered as a
	 *        {@code BehaviorGuardEvaluator}.
	 */
	public AgentInternalEventsDispatcher(Executor executor, Class<? extends Annotation> perceptGuardEvaluatorAnnotation) {
		this.executor = checkNotNull(executor);
		this.behaviorGuardEvaluatorRegistry = new BehaviorGuardEvaluatorRegistry(perceptGuardEvaluatorAnnotation);
	}

	/**
	 * Registers all {@code PerceptGuardEvaluator} methods on {@code object} to receive events.
	 *
	 * @param object object whose {@code PerceptGuardEvaluator} methods should be registered.
	 */
	public void register(Object object) {
		this.behaviorGuardEvaluatorRegistry.register(object);
	}

	/**
	 * Unregisters all {@code PerceptGuardEvaluator} methods on a registered {@code object}.
	 *
	 * @param object object whose {@code PerceptGuardEvaluator} methods should be unregistered.
	 * @throws IllegalArgumentException if the object was not previously registered.
	 */
	public void unregister(Object object) {
		this.behaviorGuardEvaluatorRegistry.unregister(object);
	}

	/**
	 * Posts an event to all registered {@code BehaviorGuardEvaluator}, the dispatch of this event will be done synchronously.
	 * This method will return successfully after the event has been posted to all {@code BehaviorGuardEvaluator}, and regardless
	 * of any exceptions thrown by {@code BehaviorGuardEvaluator}.
	 *
	 * <p>
	 * If no {@code BehaviorGuardEvaluator} have been subscribed for {@code event}'s class, and {@code event} is not already a
	 * {@link DeadEvent}, it will be wrapped in a DeadEvent and reposted.
	 * </p>
	 *
	 * @param event - an event to dispatch synchronously.
	 */
	public void immediateDispatch(Event event) {
		checkNotNull(event);

		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				Iterator<BehaviorGuardEvaluator> behaviorGuardEvaluators = AgentInternalEventsDispatcher.this.behaviorGuardEvaluatorRegistry
				  .getBehaviorGuardEvaluators(event);
				if (behaviorGuardEvaluators.hasNext()) {
					Collection<Pair<Object, Collection<Method>>> behaviorsMethodsToExecute;
					try {
						behaviorsMethodsToExecute = evaluateGuards(event, behaviorGuardEvaluators);
						executeImmmediatlyBehaviorMethods(event, behaviorsMethodsToExecute);
					} catch (InvocationTargetException e) {
						throw new RuntimeException(e);
					}

				} else if (!(event instanceof DeadEvent)) {
					// the event had no subscribers and was not itself a DeadEvent
					immediateDispatch(new DeadEvent(event));
				}
			}
		});
	}

	/**
	 * Posts an event to all registered {@code BehaviorGuardEvaluator}, the dispatch of this event will be done asynchronously.
	 * This method will return successfully after the event has been posted to all {@code BehaviorGuardEvaluator}, and regardless
	 * of any exceptions thrown by {@code BehaviorGuardEvaluator}.
	 *
	 * <p>
	 * If no {@code BehaviorGuardEvaluator} have been subscribed for {@code event}'s class, and {@code event} is not already a
	 * {@link DeadEvent}, it will be wrapped in a DeadEvent and reposted.
	 * </p>
	 * 
	 * @param event - an event to dispatch asynchronously.
	 */
	public void asyncDispatch(Event event) {
		checkNotNull(event);
		this.executor.execute(new Runnable() {
			@Override
			public void run() {
				Iterator<BehaviorGuardEvaluator> behaviorGuardEvaluators = AgentInternalEventsDispatcher.this.behaviorGuardEvaluatorRegistry
				  .getBehaviorGuardEvaluators(event);
				if (behaviorGuardEvaluators.hasNext()) {

					Collection<Pair<Object, Collection<Method>>> behaviorsMethodsToExecute;
					try {
						behaviorsMethodsToExecute = evaluateGuards(event, behaviorGuardEvaluators);
					} catch (InvocationTargetException e) {
						throw new RuntimeException(e);
					}
					executeAsynchronouslyBehaviorMethods(event, behaviorsMethodsToExecute);

				} else if (!(event instanceof DeadEvent)) {
					// the event had no subscribers and was not itself a DeadEvent
					asyncDispatch(new DeadEvent(event));
				}
			}
		});
	}

	/**
	 * Evaluate the guard associated to the specified {@code event} and returns the list of behaviors methods that must be
	 * executed.
	 *
	 * @param event - the event triggering behaviors
	 * @param behaviorGuardEvaluators - the list of class containing a {@code PerceptGuardEvaluator} method
	 * @return the collection of couple associating a object and its collection of behavior methods that must be executed
	 * @throws InvocationTargetException - exception when you try to execute a method by reflection and this method doesn't exist.
	 */
	private static Collection<Pair<Object, Collection<Method>>> evaluateGuards(final Object event,
			final Iterator<BehaviorGuardEvaluator> behaviorGuardEvaluators) throws InvocationTargetException {

		Collection<Pair<Object, Collection<Method>>> behaviorsMethodsToExecute = new LinkedList<>();

		Collection<Method> behaviorsMethodsToExecutePerTarget = null;
		BehaviorGuardEvaluator evaluator = null;
		while (behaviorGuardEvaluators.hasNext()) {
			// TODO Maybe we can parallelize this loop, could be interesting when the number of guardEvlauators increase
			evaluator = behaviorGuardEvaluators.next();
			behaviorsMethodsToExecutePerTarget = Lists.newLinkedList();
			evaluator.evaluateGuard(event, behaviorsMethodsToExecutePerTarget);
			behaviorsMethodsToExecute.add(new Pair<>(evaluator.getTarget(), behaviorsMethodsToExecutePerTarget));
		}

		return behaviorsMethodsToExecute;
	}

	/**
	 * Execute the specified Behavior method of the specified target with the specified event occurrence as parameter.
	 *
	 * @param target - the object containing the method to execute.
	 * @param method - the method to execute.
	 * @param event - the event that must process by the specified method.
	 * @throws InvocationTargetException - exception when you try to execute a method by reflection and this method doesn't exist.
	 */
	private static void invokeBehaviorMethod(Object target, Method method, Object event) throws InvocationTargetException {
		try {
			method.invoke(target, event);
		} catch (IllegalArgumentException e) {
			throw new Error("Behavior method (on clause) rejected target/argument: " + event, e);
		} catch (IllegalAccessException e) {
			throw new Error("Behavior method (on clause) became inaccessible: " + event, e);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof Error) {
				throw (Error) e.getCause();
			}
			throw e;
		}
	}

	/**
	 * 
	 * @param event
	 * @param behaviorsMethodsToExecute
	 * @throws InvocationTargetException
	 */
	private static void executeImmmediatlyBehaviorMethods(Object event,
			Collection<Pair<Object, Collection<Method>>> behaviorsMethodsToExecute) throws InvocationTargetException {

		Object target = null;
		for (Pair<Object, Collection<Method>> pair : behaviorsMethodsToExecute) {
			target = pair.getKey();
			for (Method method : pair.getValue()) {
				invokeBehaviorMethod(target, method, event);
			}
		}
	}

	private void executeAsynchronouslyBehaviorMethods(Object event,
			Collection<Pair<Object, Collection<Method>>> behaviorsMethodsToExecute) {

		for (Pair<Object, Collection<Method>> pair : behaviorsMethodsToExecute) {
			final Object target = pair.getKey();

			Queue<Pair<Object, Collection<Method>>> queueForThread = this.queue.get();
			queueForThread.offer(new Pair<>(event, pair.getValue()));

			if (!this.dispatching.get().booleanValue()) {
				this.dispatching.set(Boolean.TRUE);
				try {
					while (true) {
						Pair<Object, Collection<Method>> nextEvent = queueForThread.poll();
						if (nextEvent == null) {
							break;
						}

						for (Method m : nextEvent.getValue()) {
							this.executor.execute(new Runnable() {
								@Override
								public void run() {
									try {
										invokeBehaviorMethod(target, m, nextEvent.getKey());

									} catch (InvocationTargetException e) {
										throw new RuntimeException(e);
									}
								}
							});
						}
					}

				} finally {
					this.dispatching.remove();
					this.queue.remove();
				}
			}

		}
	}

}
