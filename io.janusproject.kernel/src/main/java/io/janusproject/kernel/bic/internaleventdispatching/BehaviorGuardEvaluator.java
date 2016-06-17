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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * 
 * 
 * 
 * @author $Author: ngaud$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 *
 */
public class BehaviorGuardEvaluator {

	/**
	 * Creates a {@code Subscriber} for {@code method} on {@code listener}.
	 * 
	 * @param listener
	 * @param method
	 * @return
	 */
	static BehaviorGuardEvaluator create(Object listener, Method method) {
		return new BehaviorGuardEvaluator(listener, method);
	}

	/** The object with the {@code PerceptGuardEvaluator} method. */
	private final Object target;

	/** {@code PerceptGuardEvaluator} method. */
	private final Method method;

	private BehaviorGuardEvaluator(Object target, Method method) {
		this.target = checkNotNull(target);
		this.method = method;
		method.setAccessible(true);
	}

	/**
	 * Evaluate the guard associated to the specified {@code event} and returns the list of behaviors methods that must be
	 * executed
	 * 
	 * @param event - the event triggering behaviors
	 * @param behaviorsMethodsToExecute - the list of behavior methods that will be completed according to the result of the guard
	 *        evaluation, BE CARFEUL: we suppose that these behavior methods are parts of the SAME object where the
	 *        {@code PerceptGuardEvaluator} method is declared
	 * @throws InvocationTargetException
	 */
	final void evaluateGuard(final Object event, Collection<Method> behaviorsMethodsToExecute) throws InvocationTargetException {
		invokeBehaviorGuardEvaluatorMethod(event, behaviorsMethodsToExecute);
	}

	/**
	 * Invokes the subscriber method. This method can be overridden to make the invocation synchronized.
	 * 
	 * @param event
	 * @param behaviorsMethodsToExecute
	 * @throws InvocationTargetException
	 */
	private void invokeBehaviorGuardEvaluatorMethod(Object event, Collection<Method> behaviorsMethodsToExecute)
			throws InvocationTargetException {
		try {
			this.method.invoke(this.target, event, behaviorsMethodsToExecute);
		} catch (IllegalArgumentException e) {
			throw new Error("PerceptGuardEvaluator method rejected target/argument: " + event, e);
		} catch (IllegalAccessException e) {
			throw new Error("PerceptGuardEvaluator method became inaccessible: " + event, e);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof Error) {
				throw (Error) e.getCause();
			}
			throw e;
		}
	}

	/**
	 * Returns he object instance containing the {@code PerceptGuardEvaluator}
	 * 
	 * @return the object instance containing the {@code PerceptGuardEvaluator}
	 */
	public Object getTarget() {
		return this.target;
	}

}
