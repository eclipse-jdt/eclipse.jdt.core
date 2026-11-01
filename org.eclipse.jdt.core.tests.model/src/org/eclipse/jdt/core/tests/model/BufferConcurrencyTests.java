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

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import junit.framework.Test;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.BufferChangedEvent;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.compiler.env.IElementInfo;
import org.eclipse.jdt.internal.core.BufferManager;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.OpenableElementInfo;
import org.eclipse.jdt.internal.core.PackageFragment;

public class BufferConcurrencyTests extends ModifyingResourceTests {
public BufferConcurrencyTests(String name) {
	super(name);
}

public static Test suite() {
	return buildModelTestSuite(BufferConcurrencyTests.class);
}

@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();
	createJavaProject("P", new String[] {""}, "");
	createFolder("P/x/y");
}

@Override
public void tearDownSuite() throws Exception {
	try {
		deleteProject("P");
	} finally {
		super.tearDownSuite();
	}
}

/**
 * A primary working copy must use its provider's buffer even when another
 * thread has opened an ordinary buffer but has not yet published the CU info.
 */
public void testBecomeWorkingCopyWhileOpeningBuffer() throws Exception {
	String path = "P/x/y/Concurrent.java";
	String source = "package x.y; public class Concurrent {}";
	createFile(path, source);
	waitUntilIndexesReady();
	ICompilationUnit original = getCompilationUnit(path);
	original.close();
	CountDownLatch bufferOpened = new CountDownLatch(1);
	CountDownLatch finishOpening = new CountDownLatch(1);
	CompilationUnit unit = new CompilationUnit((PackageFragment) original.getParent(), original.getElementName(), DefaultWorkingCopyOwner.PRIMARY) {
		@Override
		protected boolean buildStructure(OpenableElementInfo info, IProgressMonitor monitor,
				Map<IJavaElement, IElementInfo> newElements, IResource resource) throws JavaModelException {
			boolean result = super.buildStructure(info, monitor, newElements, resource);
			if (!isWorkingCopy()) {
				bufferOpened.countDown();
				try {
					assertTrue("Timed out waiting to finish opening the ordinary CU", finishOpening.await(10, TimeUnit.SECONDS));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new AssertionError(e);
				}
			}
			return result;
		}
	};
	WorkingCopyOwner previousProvider = DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider;
	AtomicReference<IBuffer> providerBuffer = new AtomicReference<>();
	WorkingCopyOwner.setPrimaryBufferProvider(new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			IBuffer buffer = new TestBuffer(workingCopy);
			providerBuffer.set(buffer);
			return buffer;
		}
	});
	FutureTask<IBuffer> opening = new FutureTask<>(unit::getBuffer);
	// Dedicated workers keep these blocking tests independent of a shared pool's availability.
	Thread reader = new Thread(opening, "Opening ordinary compilation unit");
	// Do not keep the test JVM alive if a regression leaves a worker stuck after the timed join.
	reader.setDaemon(true);
	try {
		reader.start();
		assertTrue("The ordinary CU did not open its buffer", bufferOpened.await(10, TimeUnit.SECONDS));
		unit.becomeWorkingCopy(null);
		finishOpening.countDown();
		// FutureTask.get also reports assertions and exceptions from the reader on the test thread.
		IBuffer buffer = opening.get(10, TimeUnit.SECONDS);
		assertNotNull("The primary working-copy buffer provider was not used", providerBuffer.get());
		assertSame("The ordinary CU buffer must be replaced", providerBuffer.get(), unit.getBuffer());
		assertSame("Concurrent readers must see the working-copy buffer", unit.getBuffer(), buffer);
		assertEquals("Unexpected initial source", source, buffer.getContents());
		String editedSource = "package x.y; public class Concurrent { int changed; }";
		unit.getBuffer().setContents(editedSource);
		assertEquals("Edits must reach the provider's buffer", editedSource, providerBuffer.get().getContents());
	} finally {
		finishOpening.countDown();
		try {
			reader.join(10_000);
			assertFalse("The ordinary CU reader did not finish", reader.isAlive());
		} finally {
			try {
				unit.discardWorkingCopy();
			} finally {
				WorkingCopyOwner.setPrimaryBufferProvider(previousProvider);
				deleteFile(path);
			}
		}
	}
}

/**
 * An ordinary buffer created before the working-copy transition must not be
 * published while the primary buffer provider is still creating its buffer.
 */
public void testBecomeWorkingCopyBeforeBufferPublication() throws Exception {
	assertBecomeWorkingCopyBeforeBufferPublication(false);
}

/**
 * A provider may return the same buffer to competing callers. The caller that
 * finds it already cached must not close that shared working-copy buffer.
 */
public void testBecomeWorkingCopyBeforeBufferPublicationWithSharedBuffer() throws Exception {
	assertBecomeWorkingCopyBeforeBufferPublication(true);
}

private void assertBecomeWorkingCopyBeforeBufferPublication(boolean shareBuffer) throws Exception {
	String path = "P/x/y/LateBuffer.java";
	String source = "package x.y; public class LateBuffer {}";
	createFile(path, source);
	waitUntilIndexesReady();
	ICompilationUnit original = getCompilationUnit(path);
	original.close();
	Thread testThread = Thread.currentThread();
	CountDownLatch creatingOrdinaryBuffer = new CountDownLatch(1);
	CountDownLatch finishCreating = new CountDownLatch(1);
	CompilationUnit unit = new CompilationUnit((PackageFragment) original.getParent(), original.getElementName(), DefaultWorkingCopyOwner.PRIMARY) {
		private final ThreadLocal<Boolean> openingBuffer = new ThreadLocal<>();
		@Override
		protected IBuffer openBuffer(IProgressMonitor monitor, IElementInfo info) throws JavaModelException {
			this.openingBuffer.set(Boolean.TRUE);
			try {
				return super.openBuffer(monitor, info);
			} finally {
				this.openingBuffer.remove();
			}
		}
		@Override
		public IResource resource() {
			if (Thread.currentThread() != testThread && Boolean.TRUE.equals(this.openingBuffer.get()) && !isWorkingCopy()) {
				// BufferManager.createBuffer has already selected an ordinary buffer.
				creatingOrdinaryBuffer.countDown();
				try {
					assertTrue("Timed out waiting to create the ordinary buffer", finishCreating.await(10, TimeUnit.SECONDS));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new AssertionError(e);
				}
			}
			return super.resource();
		}
	};
	FutureTask<IBuffer> opening = new FutureTask<>(unit::getBuffer);
	Thread reader = new Thread(opening, "Creating ordinary compilation-unit buffer");
	// Do not keep the test JVM alive if a regression leaves a worker stuck after the timed join.
	reader.setDaemon(true);
	WorkingCopyOwner previousProvider = DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider;
	AtomicReference<IBuffer> providerBuffer = new AtomicReference<>();
	IBuffer sharedBuffer = shareBuffer ? new TestBuffer(unit) : null;
	WorkingCopyOwner.setPrimaryBufferProvider(new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			IBuffer buffer = shareBuffer ? sharedBuffer : new TestBuffer(workingCopy);
			providerBuffer.set(buffer);
			if (Thread.currentThread() == testThread) {
				// Let the old reader finish before this candidate can be published.
				finishCreating.countDown();
				try {
					opening.get(10, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new AssertionError(e);
				} catch (ExecutionException | TimeoutException e) {
					throw new AssertionError(e);
				}
			}
			return buffer;
		}
	});
	try {
		reader.start();
		assertTrue("The ordinary CU did not start creating its buffer", creatingOrdinaryBuffer.await(10, TimeUnit.SECONDS));
		unit.becomeWorkingCopy(null);
		// FutureTask.get also reports assertions and exceptions from the reader on the test thread.
		IBuffer buffer = opening.get(10, TimeUnit.SECONDS);
		if (shareBuffer) {
			assertFalse("The shared provider buffer must remain open", sharedBuffer.isClosed());
			assertSame("The shared provider buffer must remain cached", sharedBuffer, BufferManager.getDefaultBufferManager().getBuffer(unit));
		}
		assertSame("A late ordinary buffer must not replace the provider's buffer", providerBuffer.get(), unit.getBuffer());
		assertSame("Concurrent readers must see the working-copy buffer", unit.getBuffer(), buffer);
		assertEquals("Unexpected initial source", source, buffer.getContents());
		String editedSource = "package x.y; public class LateBuffer { int changed; }";
		unit.getBuffer().setContents(editedSource);
		assertEquals("Edits must reach the provider's buffer", editedSource, providerBuffer.get().getContents());
	} finally {
		finishCreating.countDown();
		try {
			reader.join(10_000);
			assertFalse("The ordinary CU reader did not finish", reader.isAlive());
		} finally {
			try {
				unit.discardWorkingCopy();
			} finally {
				WorkingCopyOwner.setPrimaryBufferProvider(previousProvider);
				deleteFile(path);
			}
		}
	}
}

/**
 * Two callers can both observe an ordinary CU before either transition registers
 * its working-copy info. The later operation must retain the first one's buffer.
 */
public void testConcurrentBecomeWorkingCopyPreservesExistingBuffer() throws Exception {
	String path = "P/x/y/ConcurrentTransition.java";
	String source = "package x.y; public class ConcurrentTransition {}";
	String editedSource = "package x.y; public class ConcurrentTransition { int changed; }";
	createFile(path, source);
	waitUntilIndexesReady();
	ICompilationUnit unit = getCompilationUnit(path);
	unit.close();
	CountDownLatch firstCallerAtOperation = new CountDownLatch(1);
	CountDownLatch finishFirstTransition = new CountDownLatch(1);
	WorkingCopyOwner previousProvider = DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider;
	WorkingCopyOwner.setPrimaryBufferProvider(new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			return new TestBuffer(workingCopy);
		}
	});
	FutureTask<Void> transition = new FutureTask<>(() -> {
		unit.becomeWorkingCopy(new NullProgressMonitor() {
			@Override
			public void beginTask(String name, int totalWork) {
				// JavaModelOperation converts the monitor before executing the operation.
				// This caller has passed the initial retain check but has not registered yet.
				firstCallerAtOperation.countDown();
				try {
					assertTrue("Timed out waiting to finish the first transition", finishFirstTransition.await(10, TimeUnit.SECONDS));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new AssertionError(e);
				}
			}
		});
		return null;
	});
	Thread firstCaller = new Thread(transition, "Becoming a primary working copy");
	firstCaller.setDaemon(true);
	try {
		firstCaller.start();
		assertTrue("The first caller did not reach the pre-registration pause", firstCallerAtOperation.await(10, TimeUnit.SECONDS));
		unit.becomeWorkingCopy(null);
		IBuffer publishedBuffer = unit.getBuffer();
		publishedBuffer.setContents(editedSource);
		finishFirstTransition.countDown();
		transition.get(10, TimeUnit.SECONDS);

		assertFalse("Retaining the working copy must not close its published buffer", publishedBuffer.isClosed());
		assertSame("The second operation must preserve the published buffer", publishedBuffer, BufferManager.getDefaultBufferManager().getBuffer(unit));
		assertEquals("Retaining the working copy must preserve its edits", editedSource, unit.getBuffer().getContents());
		unit.discardWorkingCopy();
		assertTrue("Both successful callers must retain the working copy", unit.isWorkingCopy());
		assertSame("One discard must preserve the remaining caller's buffer", publishedBuffer, unit.getBuffer());
	} finally {
		finishFirstTransition.countDown();
		try {
			firstCaller.join(10_000);
			assertFalse("The first working-copy transition did not finish", firstCaller.isAlive());
		} finally {
			try {
				unit.discardWorkingCopy();
				unit.discardWorkingCopy();
			} finally {
				WorkingCopyOwner.setPrimaryBufferProvider(previousProvider);
				deleteFile(path);
			}
		}
	}
}

/**
 * A delayed close event must not remove a replacement buffer for the same owner.
 * Closing the current buffer must still remove it from the cache.
 */
public void testStaleCloseEventDoesNotRemoveReplacementBuffer() throws Exception {
	class LocalBufferManager extends BufferManager {
		void cache(IBuffer buffer) {
			addBuffer(buffer);
		}
	}
	LocalBufferManager bufferManager = new LocalBufferManager();
	CountDownLatch oldCloseCallbackEntered = new CountDownLatch(1);
	CountDownLatch processOldCloseEvent = new CountDownLatch(1);
	AtomicReference<IBuffer> delayedBuffer = new AtomicReference<>();
	Queue<Throwable> callbackFailures = new ConcurrentLinkedQueue<>();
	CompilationUnit unit = new CompilationUnit((PackageFragment) getPackageFragment("P", "", "x.y"), "StaleClose.java", DefaultWorkingCopyOwner.PRIMARY) {
		@Override
		protected BufferManager getBufferManager() {
			return bufferManager;
		}
		@Override
		public void bufferChanged(BufferChangedEvent event) {
			try {
				if (event.getBuffer() == delayedBuffer.get() && event.getBuffer().isClosed()) {
					// Receipt by this override precedes processing by Openable.bufferChanged below.
					oldCloseCallbackEntered.countDown();
					assertTrue("Timed out waiting to process the old close event in Openable.bufferChanged", processOldCloseEvent.await(10, TimeUnit.SECONDS));
				}
				super.bufferChanged(event);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				callbackFailures.add(e);
			} catch (Throwable e) {
				// Buffer dispatches through SafeRunner, so surface callback failures in the test thread.
				callbackFailures.add(e);
			}
		}
	};
	// These real Core buffers need only a CU handle; no file or working-copy transition is needed.
	IBuffer oldBuffer = BufferManager.createBuffer(unit);
	IBuffer replacement = BufferManager.createBuffer(unit);
	oldBuffer.setContents("package x.y; class StaleClose {}");
	replacement.setContents("package x.y; class StaleClose { int changed; }");
	delayedBuffer.set(oldBuffer);
	oldBuffer.addBufferChangedListener(unit);
	replacement.addBufferChangedListener(unit);
	bufferManager.cache(oldBuffer);
	FutureTask<Void> closing = new FutureTask<>(oldBuffer::close, null);
	Thread closer = new Thread(closing, "Closing replaced compilation-unit buffer");
	// Do not keep the test JVM alive if a regression leaves a worker stuck after the timed join.
	closer.setDaemon(true);
	try {
		assertSame("The old buffer must initially be cached", oldBuffer, bufferManager.getBuffer(unit));
		closer.start();
		assertTrue("The old close event did not reach the pausing callback", oldCloseCallbackEntered.await(10, TimeUnit.SECONDS));
		assertTrue("The old buffer must be closed when the callback pauses before Openable.bufferChanged", oldBuffer.isClosed());
		bufferManager.cache(replacement);
		assertSame("The replacement must be cached before Openable.bufferChanged processes the old event", replacement, bufferManager.getBuffer(unit));
		processOldCloseEvent.countDown();
		closing.get(10, TimeUnit.SECONDS);
		assertNoCallbackFailures(callbackFailures);
		assertSame("A stale close event must not remove the replacement buffer", replacement, bufferManager.getBuffer(unit));
		assertFalse("A stale close event must not close the replacement buffer", replacement.isClosed());

		replacement.close();
		assertNoCallbackFailures(callbackFailures);
		assertTrue("The current buffer must be closed", replacement.isClosed());
		assertNull("Closing the current buffer must remove it from the cache", bufferManager.getBuffer(unit));
	} finally {
		processOldCloseEvent.countDown();
		try {
			closer.join(10_000);
			assertFalse("The old buffer closer did not finish", closer.isAlive());
		} finally {
			oldBuffer.removeBufferChangedListener(unit);
			replacement.removeBufferChangedListener(unit);
			oldBuffer.close();
			replacement.close();
		}
	}
}

private static void assertNoCallbackFailures(Queue<Throwable> callbackFailures) {
	if (!callbackFailures.isEmpty()) {
		AssertionError failure = new AssertionError("Buffer change callbacks failed");
		for (Throwable callbackFailure : callbackFailures) {
			failure.addSuppressed(callbackFailure);
		}
		throw failure;
	}
}
}
