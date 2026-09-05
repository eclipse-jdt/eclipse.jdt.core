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

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.Hashtable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.core.JavaModelManager;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/** Tests the real options cache without creating projects, editors or builders. */
public class OptionCacheTests extends TestCase {
	private static final long TIMEOUT_SECONDS = 30;
	private static final String KEY = DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST;
	private Hashtable<String, String> savedOptions;

	public OptionCacheTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(OptionCacheTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		assertTrue("These tests require an Eclipse runtime", Platform.isRunning());
		this.savedOptions = JavaCore.getOptions();
	}

	@Override
	protected void tearDown() throws Exception {
		try {
			if (this.savedOptions != null) {
				JavaCore.setOptions(this.savedOptions);
			}
		} finally {
			super.tearDown();
		}
	}

	public void testCompletedSetOptionsCannotBeOverwritten() throws Exception {
		assertLaterReadIsCurrent(Update.SET_OPTIONS);
	}

	public void testPreferenceInvalidationCannotBeUndone() throws Exception {
		assertLaterReadIsCurrent(Update.PREFERENCE);
	}

	public void testRepeatedInvalidationCannotBeUndone() throws Exception {
		assertLaterReadIsCurrent(Update.REPEATED_INVALIDATION);
	}

	public void testCompletedResetCannotBeOverwritten() throws Exception {
		assertLaterReadIsCurrent(Update.RESET);
	}

	public void testReturnedOptionsAreIndependent() {
		Hashtable<String, String> first = JavaCore.getOptions();
		String expected = first.get(KEY);
		first.put(KEY, "not a formatter value");
		assertEquals(expected, JavaCore.getOptions().get(KEY));
	}

	public void testSequentialUpdates() {
		setOption(JavaCore.DO_NOT_INSERT);
		assertEquals(JavaCore.DO_NOT_INSERT, JavaCore.getOption(KEY));
		assertEquals(JavaCore.DO_NOT_INSERT, JavaCore.getOptions().get(KEY));
		setOption(JavaCore.INSERT);
		assertEquals(JavaCore.INSERT, JavaCore.getOption(KEY));
		assertEquals(JavaCore.INSERT, JavaCore.getOptions().get(KEY));
	}

	public void testCacheIsReused() throws Exception {
		AtomicInteger reads = new AtomicInteger();
		Thread testThread = Thread.currentThread();
		try (PreferenceReads ignored = new PreferenceReads((key, value) -> {
			if (Thread.currentThread() == testThread) {
				reads.incrementAndGet();
			}
		})) {
			invalidateWithValue(JavaCore.INSERT);
			Hashtable<String, String> first = JavaCore.getOptions();
			int coldReads = reads.get();
			assertTrue("The cold read must access the real preferences", coldReads > 0);
			for (int i = 0; i < 20; i++) {
				Hashtable<String, String> next = JavaCore.getOptions();
				assertNotSame(first, next);
				assertEquals(first, next);
			}
			assertEquals("Do not fix the race by disabling the cache", coldReads, reads.get());
		}
	}

	public void testReentrantPreferenceListener() throws Exception {
		setOption(JavaCore.DO_NOT_INSERT);
		AtomicInteger callbacks = new AtomicInteger();
		AtomicReference<Throwable> callbackFailure = new AtomicReference<>();
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);
		IEclipsePreferences.IPreferenceChangeListener listener = event -> {
			if (KEY.equals(event.getKey())) {
				try {
					JavaCore.getOptions();
					callbacks.incrementAndGet();
				} catch (Throwable failure) {
					callbackFailure.set(failure);
				}
			}
		};
		ExecutorService executor = newExecutor();
		node.addPreferenceChangeListener(listener);
		try {
			Hashtable<String, String> next = new Hashtable<>(this.savedOptions);
			next.put(KEY, JavaCore.INSERT);
			executor.submit(() -> JavaCore.setOptions(next)).get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			assertNull("Preference callback failed", callbackFailure.get());
			assertTrue("The preference callback must have reentered getOptions", callbacks.get() > 0);
			assertEquals(JavaCore.INSERT, JavaCore.getOption(KEY));
			assertEquals(JavaCore.INSERT, JavaCore.getOptions().get(KEY));
		} finally {
			node.removePreferenceChangeListener(listener);
			stop(executor);
		}
	}

	private enum Update {
		SET_OPTIONS, PREFERENCE, REPEATED_INVALIDATION, RESET
	}

	private void assertLaterReadIsCurrent(Update update) throws Exception {
		String expected = JavaCore.getDefaultOptions().get(KEY);
		assertNotNull(expected);
		String oldValue = opposite(expected);
		setOption(oldValue);
		CountDownLatch capturedOldValue = new CountDownLatch(1);
		CountDownLatch resumeReader = new CountDownLatch(1);
		AtomicBoolean intercepted = new AtomicBoolean();
		AtomicReference<Thread> readerThread = new AtomicReference<>();
		ExecutorService executor = newExecutor();
		Future<Hashtable<String, String>> reader = null;
		try (PreferenceReads ignored = new PreferenceReads((key, value) -> {
			if (Thread.currentThread() == readerThread.get() && KEY.equals(key)
					&& oldValue.equals(value) && intercepted.compareAndSet(false, true)) {
				capturedOldValue.countDown();
				assertTrue("Writer did not release the options reader",
						resumeReader.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
			}
		})) {
			try {
				// Invalidate through real preference events, not by modifying cache internals.
				invalidateWithValue(oldValue);
				reader = executor.submit(() -> {
					readerThread.set(Thread.currentThread());
					return JavaCore.getOptions();
				});
				assertTrue("Reader did not capture the old preference",
						capturedOldValue.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
				IEclipsePreferences node = InstanceScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);
				switch (update) {
					case SET_OPTIONS -> setOption(expected);
					case PREFERENCE -> node.put(KEY, expected);
					case REPEATED_INVALIDATION -> {
						// null -> null invalidations must also invalidate an in-flight computation.
						node.put(KEY, expected);
						node.put(KEY, oldValue);
						node.put(KEY, expected);
					}
					case RESET -> JavaCore.setOptions(null);
				}
				assertEquals("The update must be complete before resuming the reader", expected, JavaCore.getOption(KEY));
				resumeReader.countDown();
				reader.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
				// The overlapping read may observe the old value. Only this later read is asserted.
				assertEquals("A completed " + update + " must not be undone by an older reader",
						expected, JavaCore.getOptions().get(KEY));
			} finally {
				resumeReader.countDown();
				try {
					if (reader != null) {
						reader.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
					}
				} finally {
					stop(executor);
				}
			}
		}
	}

	private void setOption(String value) {
		Hashtable<String, String> options = new Hashtable<>(this.savedOptions);
		options.put(KEY, value);
		JavaCore.setOptions(options);
	}

	private static void invalidateWithValue(String value) {
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(JavaCore.PLUGIN_ID);
		node.put(KEY, opposite(value));
		node.put(KEY, value);
	}

	private static String opposite(String value) {
		return JavaCore.INSERT.equals(value) ? JavaCore.DO_NOT_INSERT : JavaCore.INSERT;
	}

	private static ExecutorService newExecutor() {
		return Executors.newSingleThreadExecutor(runnable -> {
			Thread thread = new Thread(runnable, "JDT options cache regression");
			thread.setDaemon(true);
			return thread;
		});
	}

	private static void stop(ExecutorService executor) throws InterruptedException {
		executor.shutdownNow();
		assertTrue("Options worker did not terminate", executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS));
	}

	@FunctionalInterface
	private interface AfterRead {
		void accept(String key, Object value) throws Exception;
	}

	/**
	 * Delays a real preference read after it has returned, without holding the
	 * preference implementation's locks. No production manager or preference
	 * values are mocked. Reflection is confined to installing this read barrier.
	 */
	private static final class PreferenceReads implements AutoCloseable {
		private final IEclipsePreferences[] lookup;
		private final IEclipsePreferences[] original;

		PreferenceReads(AfterRead afterRead) throws ReflectiveOperationException {
			Field field = JavaModelManager.class.getDeclaredField("preferencesLookup");
			field.setAccessible(true);
			this.lookup = (IEclipsePreferences[]) field.get(JavaModelManager.getJavaModelManager());
			this.original = this.lookup.clone();
			try {
				for (int i = 0; i < this.lookup.length; i++) {
					IEclipsePreferences delegate = this.original[i];
					if (delegate != null) {
						this.lookup[i] = (IEclipsePreferences) Proxy.newProxyInstance(
								IEclipsePreferences.class.getClassLoader(), new Class<?>[] { IEclipsePreferences.class },
								(proxy, method, arguments) -> {
							Object result;
							try {
								result = method.invoke(delegate, arguments);
							} catch (InvocationTargetException failure) {
								throw failure.getCause();
							}
							if (method.getName().equals("get") && arguments != null && arguments.length == 2) {
								afterRead.accept((String) arguments[0], result);
							}
							return result;
						});
					}
				}
			} catch (RuntimeException | Error failure) {
				close();
				throw failure;
			}
		}

		@Override
		public void close() {
			System.arraycopy(this.original, 0, this.lookup, 0, this.lookup.length);
		}
	}
}
