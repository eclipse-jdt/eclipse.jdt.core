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
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.BufferChangedEvent;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IBufferChangedListener;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.compiler.env.IElementInfo;
import org.eclipse.jdt.internal.core.Buffer;
import org.eclipse.jdt.internal.core.BufferCache;
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

/**
 * A delayed provider return from a discarded registration must not close the
 * same physical buffer after a replacement registration has published it.
 */
public void testStaleRegistrationDoesNotCloseSharedReplacementBuffer() throws Exception {
	String path = "P/x/y/SharedRegistration.java";
	String source = "package x.y; public class SharedRegistration {}";
	String editedSource = "package x.y; public class SharedRegistration { int changed; }";
	createFile(path, source);
	waitUntilIndexesReady();
	ICompilationUnit unit = getCompilationUnit(path);
	unit.close();
	IBuffer sharedBuffer = BufferManager.createBuffer(unit);
	sharedBuffer.setContents(source);
	CountDownLatch firstProviderEntered = new CountDownLatch(1);
	CountDownLatch firstProviderMayReturn = new CountDownLatch(1);
	FutureTask<Void> firstOpening = new FutureTask<>(() -> {
		unit.becomeWorkingCopy(null);
		return null;
	});
	Thread firstOpener = new Thread(firstOpening, "Opening discarded working-copy registration");
	firstOpener.setDaemon(true);
	WorkingCopyOwner previousProvider = DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider;
	WorkingCopyOwner.setPrimaryBufferProvider(new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			if (Thread.currentThread() == firstOpener) {
				firstProviderEntered.countDown();
				try {
					assertTrue("Timed out waiting to return the old registration's buffer", firstProviderMayReturn.await(10, TimeUnit.SECONDS));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new AssertionError(e);
				}
			}
			return sharedBuffer;
		}
	});
	try {
		firstOpener.start();
		assertTrue("The first registration did not enter its provider", firstProviderEntered.await(10, TimeUnit.SECONDS));
		assertTrue("The first working-copy registration must already exist", unit.isWorkingCopy());
		assertNull("The delayed candidate must not be cached yet", BufferManager.getDefaultBufferManager().getBuffer(unit));
		unit.discardWorkingCopy();
		assertFalse("The first registration must be discarded", unit.isWorkingCopy());
		assertFalse("Discard must not close an unpublished candidate", sharedBuffer.isClosed());
		unit.becomeWorkingCopy(null);
		assertSame("The replacement registration must publish the shared buffer", sharedBuffer, BufferManager.getDefaultBufferManager().getBuffer(unit));
		sharedBuffer.setContents(editedSource);
		firstProviderMayReturn.countDown();
		Throwable firstFailure = null;
		try {
			firstOpening.get(10, TimeUnit.SECONDS);
		} catch (ExecutionException e) {
			firstFailure = e.getCause();
		}
		// Inspect the original object and cache before getBuffer can reopen anything.
		assertFalse("A stale registration must not close the replacement's shared buffer", sharedBuffer.isClosed());
		assertSame("The replacement's shared buffer must remain cached", sharedBuffer, BufferManager.getDefaultBufferManager().getBuffer(unit));
		assertEquals("The replacement's unsaved edits must survive stale cleanup", editedSource, sharedBuffer.getContents());
		if (firstFailure != null) {
			throw new AssertionError("The delayed working-copy opener failed", firstFailure);
		}
	} finally {
		firstProviderMayReturn.countDown();
		try {
			firstOpener.join(10_000);
			assertFalse("The delayed working-copy opener did not finish", firstOpener.isAlive());
		} finally {
			WorkingCopyOwner.setPrimaryBufferProvider(previousProvider);
			try {
				unit.discardWorkingCopy();
				unit.close();
			} finally {
				sharedBuffer.close();
				deleteFile(path);
			}
		}
	}
}

/**
 * Stale cleanup must preserve a shared candidate already being prepared by the
 * replacement registration, even before that candidate has been cached.
 */
public void testStaleRegistrationPreservesSharedCandidateDuringPreparation() throws Exception {
	String path = "P/x/y/SharedPreparation.java";
	String source = "package x.y; public class SharedPreparation {}";
	createFile(path, source);
	waitUntilIndexesReady();
	ICompilationUnit unit = getCompilationUnit(path);
	unit.close();
	CountDownLatch firstProviderEntered = new CountDownLatch(1);
	CountDownLatch firstProviderMayReturn = new CountDownLatch(1);
	CountDownLatch replacementPreparing = new CountDownLatch(1);
	CountDownLatch finishPreparation = new CountDownLatch(1);
	CountDownLatch firstRetryEntered = new CountDownLatch(1);
	CountDownLatch finishFirstRetry = new CountDownLatch(1);
	class SharedProviderBuffer extends Buffer {
		Thread replacementOpener;
		final AtomicBoolean pausePreparation = new AtomicBoolean(true);
		SharedProviderBuffer() {
			super((IFile) unit.getResource(), unit, false);
		}
		@Override
		public char[] getCharacters() {
			if (Thread.currentThread() == this.replacementOpener && this.pausePreparation.compareAndSet(true, false)) {
				replacementPreparing.countDown();
				awaitLatch(finishPreparation, "Timed out waiting to prepare the replacement's shared candidate");
			}
			return super.getCharacters();
		}
	}
	SharedProviderBuffer sharedBuffer = new SharedProviderBuffer();
	sharedBuffer.setContents(source);
	FutureTask<Void> firstOpening = new FutureTask<>(() -> {
		unit.becomeWorkingCopy(null);
		return null;
	});
	FutureTask<Void> replacementOpening = new FutureTask<>(() -> {
		unit.becomeWorkingCopy(null);
		return null;
	});
	Thread firstOpener = new Thread(firstOpening, "Returning a stale shared candidate");
	Thread replacementOpener = new Thread(replacementOpening, "Preparing the replacement's shared candidate");
	sharedBuffer.replacementOpener = replacementOpener;
	firstOpener.setDaemon(true);
	replacementOpener.setDaemon(true);
	WorkingCopyOwner previousProvider = DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider;
	WorkingCopyOwner.setPrimaryBufferProvider(new WorkingCopyOwner() {
		private boolean firstReturn = true;
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			if (Thread.currentThread() == firstOpener) {
				if (this.firstReturn) {
					this.firstReturn = false;
					firstProviderEntered.countDown();
					awaitLatch(firstProviderMayReturn, "Timed out waiting to return the stale candidate");
				} else {
					// Inspect cleanup before this retry can publish or repair the candidate.
					firstRetryEntered.countDown();
					awaitLatch(finishFirstRetry, "Timed out waiting to resume the stale opener's retry");
				}
			}
			return sharedBuffer;
		}
	});
	try {
		firstOpener.start();
		awaitLatch(firstProviderEntered, "The first registration did not enter its provider");
		unit.discardWorkingCopy();
		replacementOpener.start();
		awaitLatch(replacementPreparing, "The replacement did not start preparing the shared candidate");
		firstProviderMayReturn.countDown();
		awaitLatch(firstRetryEntered, "The stale opener did not finish candidate cleanup and retry");
		assertFalse("Stale cleanup must preserve the replacement's unpublished candidate", sharedBuffer.isClosed());
		assertNull("The replacement must still be paused before publication", BufferManager.getDefaultBufferManager().getBuffer(unit));
		assertEquals("Stale cleanup must preserve the candidate's contents", source, sharedBuffer.getContents());
		finishPreparation.countDown();
		replacementOpening.get(10, TimeUnit.SECONDS);
		assertSame("The replacement must publish its shared candidate", sharedBuffer, BufferManager.getDefaultBufferManager().getBuffer(unit));
		finishFirstRetry.countDown();
		firstOpening.get(10, TimeUnit.SECONDS);
		assertFalse("The resumed retry must leave the shared candidate open", sharedBuffer.isClosed());
	} finally {
		firstProviderMayReturn.countDown();
		finishPreparation.countDown();
		finishFirstRetry.countDown();
		firstOpening.cancel(true);
		replacementOpening.cancel(true);
		try {
			firstOpener.join(10_000);
			replacementOpener.join(10_000);
			assertFalse("The stale opener did not finish", firstOpener.isAlive());
			assertFalse("The replacement opener did not finish", replacementOpener.isAlive());
		} finally {
			WorkingCopyOwner.setPrimaryBufferProvider(previousProvider);
			try {
				unit.discardWorkingCopy();
				unit.close();
			} finally {
				sharedBuffer.close();
				deleteFile(path);
			}
		}
	}
}

/**
 * A candidate selected for closing must not be published by another registration
 * during the interval before its physical close completes.
 */
public void testSharedCandidateCannotBePublishedDuringRetirement() throws Exception {
	String path = "P/x/y/SharedRetirement.java";
	String source = "package x.y; public class SharedRetirement {}";
	createFile(path, source);
	waitUntilIndexesReady();
	ICompilationUnit unit = getCompilationUnit(path);
	unit.close();
	CountDownLatch firstProviderEntered = new CountDownLatch(1);
	CountDownLatch firstProviderMayReturn = new CountDownLatch(1);
	CountDownLatch closeEntered = new CountDownLatch(1);
	CountDownLatch finishClose = new CountDownLatch(1);
	AtomicBoolean closeHeldManagerMonitor = new AtomicBoolean();
	class RetiringProviderBuffer extends Buffer {
		Thread retiringOpener;
		final AtomicBoolean pauseClose = new AtomicBoolean(true);
		RetiringProviderBuffer() {
			super((IFile) unit.getResource(), unit, false);
		}
		@Override
		public void close() {
			if (Thread.currentThread() == this.retiringOpener && this.pauseClose.compareAndSet(true, false)) {
				closeHeldManagerMonitor.set(Thread.holdsLock(BufferManager.getDefaultBufferManager()));
				closeEntered.countDown();
				awaitLatch(finishClose, "Timed out waiting to finish retiring the shared candidate");
			}
			super.close();
		}
	}
	RetiringProviderBuffer sharedBuffer = new RetiringProviderBuffer();
	sharedBuffer.setContents(source);
	FutureTask<Void> firstOpening = new FutureTask<>(() -> {
		unit.becomeWorkingCopy(null);
		return null;
	});
	FutureTask<Void> replacementOpening = new FutureTask<>(() -> {
		unit.becomeWorkingCopy(null);
		return null;
	});
	Thread firstOpener = new Thread(firstOpening, "Retiring a stale shared candidate");
	Thread replacementOpener = new Thread(replacementOpening, "Rejecting a retiring shared candidate");
	sharedBuffer.retiringOpener = firstOpener;
	firstOpener.setDaemon(true);
	replacementOpener.setDaemon(true);
	WorkingCopyOwner previousProvider = DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider;
	WorkingCopyOwner.setPrimaryBufferProvider(new WorkingCopyOwner() {
		private boolean firstReturn = true;
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			if (Thread.currentThread() == firstOpener) {
				if (!this.firstReturn) return BufferManager.createBuffer(workingCopy);
				this.firstReturn = false;
				firstProviderEntered.countDown();
				awaitLatch(firstProviderMayReturn, "Timed out waiting to return the retiring candidate");
			}
			return sharedBuffer;
		}
	});
	try {
		firstOpener.start();
		awaitLatch(firstProviderEntered, "The first registration did not enter its provider");
		unit.discardWorkingCopy();
		firstProviderMayReturn.countDown();
		awaitLatch(closeEntered, "Stale cleanup did not start closing its candidate");
		assertFalse("Physical close must run outside the buffer manager monitor", closeHeldManagerMonitor.get());
		assertFalse("The physical close must still be paused", sharedBuffer.isClosed());
		replacementOpener.start();
		try {
			replacementOpening.get(10, TimeUnit.SECONDS);
			fail("The replacement must reject a candidate whose retirement has begun");
		} catch (ExecutionException e) {
			assertTrue("The retiring candidate must be rejected with JavaModelException", e.getCause() instanceof JavaModelException);
			assertEquals("The rejection must report invalid buffer contents", IJavaModelStatusConstants.INVALID_CONTENTS,
					((JavaModelException) e.getCause()).getJavaModelStatus().getCode());
		}
		assertNull("A retiring candidate must not enter the cache", BufferManager.getDefaultBufferManager().getBuffer(unit));
		assertFalse("Rejecting the candidate must not finish another opener's close", sharedBuffer.isClosed());
		finishClose.countDown();
		firstOpening.get(10, TimeUnit.SECONDS);
		assertTrue("The stale candidate must eventually be physically closed", sharedBuffer.isClosed());
		assertNotSame("A retry must not publish the retired candidate", sharedBuffer, BufferManager.getDefaultBufferManager().getBuffer(unit));
	} finally {
		firstProviderMayReturn.countDown();
		finishClose.countDown();
		firstOpening.cancel(true);
		replacementOpening.cancel(true);
		try {
			firstOpener.join(10_000);
			replacementOpener.join(10_000);
			assertFalse("The retiring opener did not finish", firstOpener.isAlive());
			assertFalse("The replacement opener did not finish", replacementOpener.isAlive());
		} finally {
			WorkingCopyOwner.setPrimaryBufferProvider(previousProvider);
			try {
				unit.discardWorkingCopy();
				unit.close();
			} finally {
				sharedBuffer.close();
				deleteFile(path);
			}
		}
	}
}

/**
 * A failed opener must not close a shared candidate while another opener is
 * still preparing it and neither has published a buffer yet.
 */
public void testExceptionalOpenerPreservesSharedCandidateDuringPreparation() throws Exception {
	String path = "P/x/y/ExceptionalSharedCandidate.java";
	String source = "package x.y; public class ExceptionalSharedCandidate {}";
	createFile(path, source);
	waitUntilIndexesReady();
	CountDownLatch registeringListener = new CountDownLatch(1);
	CountDownLatch survivorReading = new CountDownLatch(1);
	CountDownLatch finishReading = new CountDownLatch(1);
	class SharedProviderBuffer extends Buffer {
		Thread failingOpener;
		Thread survivingOpener;
		final AtomicBoolean delayReading = new AtomicBoolean(true);
		SharedProviderBuffer(ICompilationUnit owner) {
			super((IFile) owner.getResource(), owner, false);
		}
		@Override
		public char[] getCharacters() {
			if (Thread.currentThread() == this.survivingOpener && this.delayReading.compareAndSet(true, false)) {
				survivorReading.countDown();
				awaitLatch(finishReading, "Timed out waiting to finish the surviving opener's read");
			}
			return super.getCharacters();
		}
		@Override
		// Wait before registration without holding the provider monitor.
		@SuppressWarnings("sync-override")
		public void addBufferChangedListener(IBufferChangedListener listener) {
			if (Thread.currentThread() == this.failingOpener) {
				registeringListener.countDown();
				awaitLatch(survivorReading, "The second opener did not reach the shared candidate");
				throw new OperationCanceledException();
			}
			super.addBufferChangedListener(listener);
		}
	}
	AtomicReference<IBuffer> providerBuffer = new AtomicReference<>();
	WorkingCopyOwner owner = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			IBuffer buffer = providerBuffer.get();
			return buffer == null ? BufferManager.createBuffer(workingCopy) : buffer;
		}
	};
	ICompilationUnit unit = getCompilationUnit(path).getWorkingCopy(owner, null);
	SharedProviderBuffer sharedBuffer = new SharedProviderBuffer(unit);
	FutureTask<IBuffer> failedOpening = new FutureTask<>(unit::getBuffer);
	FutureTask<IBuffer> survivingOpening = new FutureTask<>(unit::getBuffer);
	sharedBuffer.failingOpener = new Thread(failedOpening, "Failing shared-buffer opener");
	sharedBuffer.survivingOpener = new Thread(survivingOpening, "Surviving shared-buffer opener");
	sharedBuffer.failingOpener.setDaemon(true);
	sharedBuffer.survivingOpener.setDaemon(true);
	try {
		unit.getBuffer().close();
		providerBuffer.set(sharedBuffer);
		sharedBuffer.failingOpener.start();
		awaitLatch(registeringListener, "The first opener did not prepare the shared candidate");
		sharedBuffer.survivingOpener.start();
		awaitLatch(survivorReading, "The surviving opener did not reach the shared candidate");
		try {
			failedOpening.get(10, TimeUnit.SECONDS);
			fail("The first opener must report its cancellation");
		} catch (ExecutionException e) {
			assertTrue("The first opener must fail with OperationCanceledException", e.getCause() instanceof OperationCanceledException);
		}
		assertNull("Neither opener has published the shared candidate", BufferManager.getDefaultBufferManager().getBuffer(unit));
		assertFalse("A failed opener must leave another opener's candidate open", sharedBuffer.isClosed());
		finishReading.countDown();
		assertSame("The surviving opener must publish the shared candidate", sharedBuffer, survivingOpening.get(10, TimeUnit.SECONDS));
		assertSame("The shared candidate must remain cached", sharedBuffer, unit.getBuffer());
		assertEquals("The surviving opener must retain the initialized source", source, sharedBuffer.getContents());
	} finally {
		survivorReading.countDown();
		finishReading.countDown();
		failedOpening.cancel(true);
		survivingOpening.cancel(true);
		try {
			sharedBuffer.failingOpener.join(10_000);
			sharedBuffer.survivingOpener.join(10_000);
			assertFalse("The failed opener did not finish", sharedBuffer.failingOpener.isAlive());
			assertFalse("The surviving opener did not finish", sharedBuffer.survivingOpener.isAlive());
		} finally {
			try {
				unit.discardWorkingCopy();
			} finally {
				sharedBuffer.close();
				deleteFile(path);
			}
		}
	}
}

/**
 * An eviction query can close the candidate after preparation. Publication must
 * reject it and still drain the evicted buffer's deferred close.
 */
public void testEvictionCallbackCannotPublishClosedCandidate() throws Exception {
	class LocalBufferManager extends BufferManager {
		void cache(IBuffer buffer) {
			addBuffer(buffer);
		}
	}
	String path = "P/x/y/EvictionClosesCandidate.java";
	createFile(path, "package x.y; public class EvictionClosesCandidate {}");
	waitUntilIndexesReady();
	LocalBufferManager buffers = new LocalBufferManager();
	Field cache = BufferManager.class.getDeclaredField("openBuffers");
	cache.setAccessible(true);
	((BufferCache<?>) cache.get(buffers)).setSpaceLimit(1);
	AtomicReference<IBuffer> candidate = new AtomicReference<>();
	WorkingCopyOwner owner = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			IBuffer provided = candidate.get();
			return provided == null ? BufferManager.createBuffer(workingCopy) : provided;
		}
	};
	ICompilationUnit original = getCompilationUnit(path);
	CompilationUnit unit = new CompilationUnit((PackageFragment) original.getParent(), original.getElementName(), owner) {
		@Override
		protected BufferManager getBufferManager() {
			return buffers;
		}
	};
	AtomicBoolean evictionQueried = new AtomicBoolean();
	IBuffer evicted = new Buffer(null, getCompilationUnit("P/x/y/EvictedCandidate.java"), false) {
		@Override
		public boolean hasUnsavedChanges() {
			if (evictionQueried.compareAndSet(false, true)) candidate.get().close();
			return super.hasUnsavedChanges();
		}
	};
	evicted.setContents("package x.y; class EvictedCandidate {}");
	try {
		unit.becomeWorkingCopy(null);
		unit.getBuffer().close();
		candidate.set(BufferManager.createBuffer(unit));
		buffers.cache(evicted);
		assertFalse("The new candidate must start open", candidate.get().isClosed());
		try {
			unit.getBuffer();
			fail("An eviction callback's closed candidate must be rejected");
		} catch (JavaModelException e) {
			assertEquals("The closed candidate must report invalid contents", IJavaModelStatusConstants.INVALID_CONTENTS,
					e.getJavaModelStatus().getCode());
		}
		assertTrue("Publication must exercise the eviction query", evictionQueried.get());
		assertTrue("The eviction query must close the candidate", candidate.get().isClosed());
		assertNull("A candidate closed during eviction must not be cached", buffers.getBuffer(unit));
		assertTrue("Rejected publication must still drain the evicted buffer's close", evicted.isClosed());
	} finally {
		try {
			unit.discardWorkingCopy();
			unit.close();
		} finally {
			if (candidate.get() != null) candidate.get().close();
			evicted.close();
			deleteFile(path);
		}
	}
}

/**
 * Different working-copy owners may deliberately share a physical buffer. Each
 * unequal working copy must receive the shared buffer's change notifications.
 */
public void testSharedCandidateNotifiesDifferentWorkingCopyOwners() throws Exception {
	String path = "P/x/y/DifferentSharedOwners.java";
	String source = "package x.y; public class DifferentSharedOwners {}";
	String editedSource = "package x.y; public class DifferentSharedOwners { int changed; }";
	createFile(path, source);
	waitUntilIndexesReady();
	ICompilationUnit original = getCompilationUnit(path);
	CountDownLatch firstPreparing = new CountDownLatch(1);
	CountDownLatch finishFirstPreparation = new CountDownLatch(1);
	AtomicReference<ICompilationUnit> firstHandle = new AtomicReference<>();
	AtomicReference<IBuffer> shared = new AtomicReference<>();
	class SharedProviderBuffer extends Buffer {
		final Thread firstOpener = Thread.currentThread();
		final AtomicBoolean pausePreparation = new AtomicBoolean(true);
		SharedProviderBuffer(ICompilationUnit owner) {
			super((IFile) owner.getResource(), owner, false);
		}
		@Override
		public char[] getCharacters() {
			if (Thread.currentThread() == this.firstOpener && this.pausePreparation.compareAndSet(true, false)) {
				firstPreparing.countDown();
				awaitLatch(finishFirstPreparation, "Timed out waiting to prepare the first owner's shared buffer");
			}
			return super.getCharacters();
		}
	}
	WorkingCopyOwner firstOwner = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			firstHandle.set(workingCopy);
			IBuffer buffer = shared.get();
			if (buffer == null) {
				buffer = new SharedProviderBuffer(workingCopy);
				shared.set(buffer);
			}
			return buffer;
		}
	};
	WorkingCopyOwner secondOwner = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			return shared.get();
		}
	};
	FutureTask<ICompilationUnit> firstOpening = new FutureTask<>(() -> original.getWorkingCopy(firstOwner, null));
	Thread firstOpener = new Thread(firstOpening, "Preparing a buffer shared by different owners");
	firstOpener.setDaemon(true);
	ICompilationUnit second = null;
	try {
		firstOpener.start();
		awaitLatch(firstPreparing, "The first owner did not claim and prepare its shared candidate");
		second = original.getWorkingCopy(secondOwner, null);
		assertSame("The second working copy must use the shared buffer", shared.get(), second.getBuffer());
		// Finish the second model while the first candidate claim is still active.
		// A later getBuffer() could register a fresh listener and hide the lost notification.
		second.makeConsistent(null);
		finishFirstPreparation.countDown();
		ICompilationUnit first = firstOpening.get(10, TimeUnit.SECONDS);
		assertFalse("The owners must identify different working copies", first.equals(second));
		assertSame("The first working copy must use the shared buffer", shared.get(), first.getBuffer());
		first.makeConsistent(null);
		assertTrue("The first model must start consistent", first.isConsistent());
		assertTrue("The second model must start consistent", second.isConsistent());
		shared.get().setContents(editedSource);
		assertFalse("An edit must invalidate the first owner's model", first.isConsistent());
		assertFalse("An edit must invalidate the second owner's model", second.isConsistent());
	} finally {
		finishFirstPreparation.countDown();
		firstOpening.cancel(true);
		try {
			firstOpener.join(10_000);
			assertFalse("The first owner's opener did not finish", firstOpener.isAlive());
		} finally {
			try {
				if (firstHandle.get() != null) firstHandle.get().discardWorkingCopy();
			} finally {
				try {
					if (second != null) second.discardWorkingCopy();
				} finally {
					if (shared.get() != null) shared.get().close();
					deleteFile(path);
				}
			}
		}
	}
}

private static void awaitLatch(CountDownLatch latch, String message) {
	try {
		assertTrue(message, latch.await(10, TimeUnit.SECONDS));
	} catch (InterruptedException e) {
		Thread.currentThread().interrupt();
		throw new AssertionError(message, e);
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
