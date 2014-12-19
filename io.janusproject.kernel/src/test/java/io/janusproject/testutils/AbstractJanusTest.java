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
package io.janusproject.testutils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.annotation.Nullable;

import org.junit.Rule;
import org.junit.Test;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.Service.Listener;
import com.google.common.util.concurrent.Service.State;

/** Abstract class that clear the attributes.
 *
 * @param <S> - the type of the service.
 * @author $Author: sgalland$
 * @version $FullVersion$
 * @mavengroupid $GroupId$
 * @mavenartifactid $ArtifactId$
 */
@SuppressWarnings("all")
public abstract class AbstractJanusTest {

	/** This rule permits to clean automatically the fields
	 * at the end of the test.
	 */
	@Rule
	public TestWatcher serviceTestWatcher = new TestWatcher() {
		@Override
		protected void finished(Description description) {
			// Clear the references to the mock objects or the injected objects
			Class<?> type = AbstractJanusTest.this.getClass();
			while (type != null && !Object.class.equals(type)) {
				for (Field field : type.getDeclaredFields()) {
					if ((field.getAnnotation(Mock.class) != null
							|| field.getAnnotation(InjectMocks.class) != null
							|| field.getAnnotation(Nullable.class) != null)
							&& (field.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) == 0) {
						boolean isAcc = field.isAccessible();
						try {
							field.setAccessible(true);
							field.set(AbstractJanusTest.this, null);
						} catch (Exception e) {
							throw new Error(e);
						} finally {
							field.setAccessible(isAcc);
						}
					}
				}
				type = type.getSuperclass();
			}
		}
	};

	/** Test if the actual collection/iterable contains all the expected objects.
	 * 
	 * @param actual - the collection to test.
	 * @param expected - the expected objects.
	 */
	public static void assertContains(Iterable<?> actual, Object... expected) {
		assertContainsCollection(actual, Arrays.asList(expected));
	}

	/** Test if the actual collection/iterable contains all the expected objects.
	 * 
	 * @param actual - the collection to test.
	 * @param expected - the expected objects.
	 */
	public static void assertContainsCollection(Iterable<?> actual, Iterable<?> expected) {
		assertNotNull(actual);
		Collection<Object> la = new ArrayList<>();
		Iterables.addAll(la, actual);
		Collection<Object> le = new ArrayList<>();
		Iterables.addAll(le, expected);

		Iterator<?> it1 = la.iterator();
		while (it1.hasNext()) {
			Object ac = it1.next();
			it1.remove();
			if (!le.remove(ac)) {
				fail("Unexpecting element: " + ac);
				return;
			}
		}

		if (!le.isEmpty()) {
			fail("Expecting the following elements:\n" + le.toString() + "\nbut was:\n" +
					Iterables.toString(actual));
		}
	}

}
