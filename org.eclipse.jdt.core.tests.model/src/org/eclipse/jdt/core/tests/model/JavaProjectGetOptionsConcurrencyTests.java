/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import junit.framework.Test;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;

/**
 * Regression test for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/5520 :
 * {@link JavaProject#getOptions(boolean)} iterates the live, shared
 * {@link JavaModelManager.PerProjectInfo#options} table directly. A concurrent
 * mutation of that same table by another thread -- e.g. another getOptions()
 * rebuild for the same project, or a preference-change-driven update -- can throw
 * a ConcurrentModificationException from within getOptions() itself.
 */
public class JavaProjectGetOptionsConcurrencyTests extends AbstractJavaModelTests {

	private static final long TIMEOUT_SECONDS = 30;
	private static final int ITERATIONS = 5000;
	private static final int SEED_SIZE = 10000;

	public JavaProjectGetOptionsConcurrencyTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(JavaProjectGetOptionsConcurrencyTests.class);
	}

	public void testGetOptionsDoesNotThrowUnderConcurrentCacheMutation() throws Exception {
		IJavaProject javaProject = createJavaProject("P_5520");
		try {
			JavaProject project = (JavaProject) javaProject;
			JavaModelManager.PerProjectInfo info = project.getPerProjectInfo();

			// Seed the cache with a sizable table so a single iteration over it takes
			// measurable time, widening the window for a concurrent mutation to land
			// mid-iteration.
			Hashtable<String, String> seed = new Hashtable<>();
			for (int i = 0; i < SEED_SIZE; i++) {
				seed.put("org.eclipse.jdt.core.test.option" + i, "value" + i);
			}
			info.options = seed;

			CountDownLatch start = new CountDownLatch(1);
			AtomicReference<Throwable> failure = new AtomicReference<>();
			ExecutorService executor = Executors.newFixedThreadPool(2);
			try {
				Future<?> mutator = executor.submit(() -> {
					try {
						start.await();
						for (int i = 0; i < ITERATIONS && failure.get() == null; i++) {
							// Add a brand new key each time -- this is a structural modification
							// (bumps the Hashtable's modCount), exactly like a concurrent
							// getOptions() rebuild or preference-driven update would perform on
							// the shared cache. Re-putting the SAME key every time does NOT bump
							// modCount once the key already exists (Hashtable only increments
							// modCount when an entry is actually added/removed), so it would
							// never trigger the race -- hence starting past the seeded keys.
							info.options.put("org.eclipse.jdt.core.test.option" + (SEED_SIZE + i), String.valueOf(i));
						}
					} catch (Throwable t) {
						failure.compareAndSet(null, t);
					}
				});
				Future<?> reader = executor.submit(() -> {
					try {
						start.await();
						for (int i = 0; i < ITERATIONS && failure.get() == null; i++) {
							project.getOptions(true);
						}
					} catch (Throwable t) {
						failure.compareAndSet(null, t);
					}
				});
				start.countDown();
				mutator.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
				reader.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			} finally {
				executor.shutdownNow();
			}

			Throwable t = failure.get();
			if (t != null) {
				throw new AssertionError("getOptions() must not throw under concurrent cache mutation", t);
			}
		} finally {
			deleteProject(javaProject);
		}
	}
}
