/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Contributors to the Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.BufferChangedEvent;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.compiler.env.IElementInfo;
import org.eclipse.jdt.internal.core.Buffer;
import org.eclipse.jdt.internal.core.BufferManager;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.PackageFragment;

public class BufferLifecycleTests extends ModifyingResourceTests {

public BufferLifecycleTests(String name) {
	super(name);
}

public static Test suite() {
	return buildModelTestSuite(BufferLifecycleTests.class);
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
 * A close event from a replaced buffer must not hide the replacement's edits
 * from the compilation unit's consistency check.
 */
public void testStaleCloseEventPreservesReplacementInconsistency() throws Exception {
	class LocalBufferManager extends BufferManager {
		void cache(IBuffer buffer) {
			addBuffer(buffer);
		}
	}
	LocalBufferManager bufferManager = new LocalBufferManager();
	CountDownLatch oldCloseReceived = new CountDownLatch(1);
	CountDownLatch finishOldClose = new CountDownLatch(1);
	AtomicReference<IBuffer> delayedBuffer = new AtomicReference<>();
	AtomicReference<Throwable> callbackFailure = new AtomicReference<>();
	CompilationUnit unit = new CompilationUnit((PackageFragment) getPackageFragment("P", "", "x.y"), "StaleClose.java", DefaultWorkingCopyOwner.PRIMARY) {
		@Override
		protected BufferManager getBufferManager() {
			return bufferManager;
		}
		@Override
		public void bufferChanged(BufferChangedEvent event) {
			try {
				if (event.getBuffer() == delayedBuffer.get() && event.getBuffer().isClosed()) {
					oldCloseReceived.countDown();
					assertTrue("Timed out waiting to deliver the old close event", finishOldClose.await(10, TimeUnit.SECONDS));
				}
				super.bufferChanged(event);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				callbackFailure.compareAndSet(null, e);
			} catch (Throwable e) {
				// Buffer dispatches through SafeRunner, so surface callback failures in the test thread.
				callbackFailure.compareAndSet(null, e);
			}
		}
	};
	IBuffer oldBuffer = BufferManager.createBuffer(unit);
	IBuffer replacement = BufferManager.createBuffer(unit);
	String source = "package x.y; class StaleClose {}";
	oldBuffer.setContents(source);
	replacement.setContents(source);
	delayedBuffer.set(oldBuffer);
	oldBuffer.addBufferChangedListener(unit);
	replacement.addBufferChangedListener(unit);
	bufferManager.cache(oldBuffer);
	FutureTask<Void> closing = new FutureTask<>(oldBuffer::close, null);
	Thread closer = new Thread(closing, "Delivering stale compilation-unit buffer close");
	closer.setDaemon(true);
	try {
		assertTrue("The compilation unit must initially be consistent", unit.isConsistent());
		closer.start();
		assertTrue("The old buffer did not deliver its close event", oldCloseReceived.await(10, TimeUnit.SECONDS));
		assertTrue("The old buffer must be closed before its event is delivered", oldBuffer.isClosed());
		bufferManager.cache(replacement);
		replacement.setContents("package x.y; class StaleClose { int changed; }");
		assertFalse("Editing the replacement must make the compilation unit inconsistent", unit.isConsistent());

		finishOldClose.countDown();
		closing.get(10, TimeUnit.SECONDS);
		if (callbackFailure.get() != null) {
			throw new AssertionError("The old buffer close callback failed", callbackFailure.get());
		}
		assertSame("A stale close event must leave the replacement cached", replacement, bufferManager.getBuffer(unit));
		assertFalse("A stale close event must leave the replacement open", replacement.isClosed());
		assertFalse("A stale close event must not mark the replacement consistent", unit.isConsistent());

		replacement.close();
		if (callbackFailure.get() != null) {
			throw new AssertionError("The replacement buffer close callback failed", callbackFailure.get());
		}
		assertNull("Closing the current buffer must remove it from the cache", bufferManager.getBuffer(unit));
		assertTrue("Closing the current buffer must clear its inconsistency", unit.isConsistent());
	} finally {
		finishOldClose.countDown();
		try {
			closer.join(10_000);
			assertFalse("The old buffer closer did not finish", closer.isAlive());
		} finally {
			oldBuffer.close();
			replacement.close();
			oldBuffer.removeBufferChangedListener(unit);
			replacement.removeBufferChangedListener(unit);
		}
	}
}

/**
 * A provider may start a new working-copy generation from its buffer's close
 * callback. Finishing the old discard must leave that generation usable.
 */
public void testFinalDiscardPreservesReentrantWorkingCopy() throws Exception {
	String path = "P/x/y/ReentrantDiscard.java";
	String source = "package x.y; public class ReentrantDiscard {}";
	String editedSource = "package x.y; public class ReentrantDiscard { int changed; }";
	createFile(path, source);
	waitUntilIndexesReady();
	class ReopeningBuffer extends Buffer {
		boolean reopenOnClose;
		IBuffer replacement;
		ReopeningBuffer(ICompilationUnit owner) {
			super((IFile) owner.getResource(), owner, false);
		}
		@Override
		public void close() {
			boolean reopen = this.reopenOnClose;
			this.reopenOnClose = false;
			super.close();
			if (reopen) {
				ICompilationUnit unit = (ICompilationUnit) getOwner();
				try {
					unit.becomeWorkingCopy(null);
					this.replacement = unit.getBuffer();
					this.replacement.setContents(editedSource);
				} catch (JavaModelException e) {
					throw new AssertionError("Could not reopen the working copy from the provider's close callback", e);
				}
			}
		}
	}
	WorkingCopyOwner owner = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			return new ReopeningBuffer(workingCopy);
		}
	};
	ICompilationUnit unit = null;
	ReopeningBuffer oldBuffer = null;
	try {
		unit = getCompilationUnit(path).getWorkingCopy(owner, null);
		oldBuffer = (ReopeningBuffer) unit.getBuffer();
		assertEquals("Unexpected initial working-copy source", source, oldBuffer.getContents());
		oldBuffer.reopenOnClose = true;

		unit.discardWorkingCopy();

		IBuffer replacement = oldBuffer.replacement;
		assertTrue("The discarded generation's buffer must be closed", oldBuffer.isClosed());
		assertNotNull("The provider's close callback must open a replacement", replacement);
		assertNotSame("The new generation must have a new buffer", oldBuffer, replacement);
		assertFalse("Discarding the previous generation must not close the replacement", replacement.isClosed());
		assertSame("Discarding the previous generation must leave the replacement cached", replacement,
				BufferManager.getDefaultBufferManager().getBuffer(unit));
		assertTrue("Discarding the previous generation must leave the new model open", unit.isOpen());
		assertSame("Readers must receive the replacement buffer", replacement, unit.getBuffer());
		assertEquals("The replacement's edits must survive the old discard", editedSource, replacement.getContents());
		assertFalse("The replacement's edits must still require reconciliation", unit.isConsistent());
		unit.makeConsistent(null);
		assertTrue("The replacement's edited field must enter the model", unit.getType("ReentrantDiscard").getField("changed").exists());

		unit.discardWorkingCopy();
		assertTrue("Discarding the new generation must close its buffer", replacement.isClosed());
		assertNull("Discarding the new generation must remove its cached buffer", BufferManager.getDefaultBufferManager().getBuffer(unit));
	} finally {
		if (oldBuffer != null) {
			oldBuffer.reopenOnClose = false;
		}
		try {
			if (unit != null) {
				unit.discardWorkingCopy();
			}
		} finally {
			deleteFile(path);
		}
	}
}

/**
 * A provider's explicitly closed buffer must be rejected without publishing it
 * or retrying forever.
 */
public void testClosedProviderBufferIsRejected() throws Exception {
	String path = "P/x/y/ClosedProvider.java";
	String source = "package x.y; public class ClosedProvider {}";
	createFile(path, source);
	waitUntilIndexesReady();
	AtomicReference<ICompilationUnit> attemptedWorkingCopy = new AtomicReference<>();
	AtomicReference<IBuffer> providerBuffer = new AtomicReference<>();
	WorkingCopyOwner owner = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			// Allow the bounded worker to terminate even if opening keeps retrying.
			if (Thread.currentThread().isInterrupted()) {
				throw new OperationCanceledException();
			}
			attemptedWorkingCopy.set(workingCopy);
			IBuffer buffer = providerBuffer.get();
			if (buffer == null) {
				buffer = BufferManager.createBuffer(workingCopy);
				buffer.setContents(source);
				buffer.close();
				providerBuffer.set(buffer);
			}
			return buffer;
		}
	};
	FutureTask<ICompilationUnit> opening = new FutureTask<>(() -> getCompilationUnit(path).getWorkingCopy(owner, null));
	Thread opener = new Thread(opening, "Opening a closed provider buffer");
	opener.setDaemon(true);
	try {
		opener.start();
		try {
			opening.get(10, TimeUnit.SECONDS);
			fail("A closed provider buffer must be rejected");
		} catch (ExecutionException e) {
			assertTrue("A closed provider buffer must fail with JavaModelException", e.getCause() instanceof JavaModelException);
		}
		assertNotNull("The provider must have been asked for a buffer", attemptedWorkingCopy.get());
		assertTrue("The rejected provider buffer must remain closed", providerBuffer.get().isClosed());
		assertNull("A closed provider buffer must not be cached",
				BufferManager.getDefaultBufferManager().getBuffer(attemptedWorkingCopy.get()));
	} finally {
		opening.cancel(true);
		try {
			opener.join(10_000);
			assertFalse("The closed-buffer opener did not finish", opener.isAlive());
		} finally {
			try {
				if (attemptedWorkingCopy.get() != null) {
					attemptedWorkingCopy.get().discardWorkingCopy();
				}
			} finally {
				deleteFile(path);
			}
		}
	}
}

/**
 * A non-primary getBuffer that has already obtained model info must not reopen
 * its discarded working copy when it reaches buffer creation later.
 */
public void testDelayedBufferOpenDoesNotReopenDiscardedWorkingCopy() throws Exception {
	String path = "P/x/y/DiscardedOpen.java";
	createFile(path, "package x.y; public class DiscardedOpen {}");
	waitUntilIndexesReady();
	ICompilationUnit original = getCompilationUnit(path);
	AtomicBoolean delayOpening = new AtomicBoolean();
	CountDownLatch openingBuffer = new CountDownLatch(1);
	CountDownLatch finishOpening = new CountDownLatch(1);
	CompilationUnit unit = new CompilationUnit((PackageFragment) original.getParent(), original.getElementName(), new WorkingCopyOwner() {}) {
		@Override
		protected IBuffer openBuffer(IProgressMonitor monitor, IElementInfo info) throws JavaModelException {
			if (delayOpening.compareAndSet(true, false)) {
				openingBuffer.countDown();
				try {
					assertTrue("Timed out waiting to finish the delayed buffer open", finishOpening.await(10, TimeUnit.SECONDS));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new AssertionError(e);
				}
			}
			return super.openBuffer(monitor, info);
		}
	};
	FutureTask<IBuffer> opening = new FutureTask<>(unit::getBuffer);
	Thread opener = new Thread(opening, "Opening a discarded working-copy buffer");
	opener.setDaemon(true);
	try {
		unit.becomeWorkingCopy(null);
		unit.getBuffer().close();
		assertTrue("Closing the buffer must leave the working-copy model open", unit.isOpen());
		assertNull("The closed buffer must no longer be cached", BufferManager.getDefaultBufferManager().getBuffer(unit));

		delayOpening.set(true);
		opener.start();
		assertTrue("The reader did not reach buffer creation", openingBuffer.await(10, TimeUnit.SECONDS));
		unit.discardWorkingCopy();
		assertFalse("Final discard must close the working-copy model", unit.isOpen());
		finishOpening.countDown();
		try {
			opening.get(10, TimeUnit.SECONDS);
			fail("A delayed buffer open must not reopen a discarded working copy");
		} catch (ExecutionException e) {
			assertTrue("A discarded working copy must fail with JavaModelException", e.getCause() instanceof JavaModelException);
			assertTrue("The discarded working copy must be reported as not present", ((JavaModelException) e.getCause()).isDoesNotExist());
		}
		assertNull("A discarded working copy must not acquire another cached buffer", BufferManager.getDefaultBufferManager().getBuffer(unit));
		assertFalse("A delayed buffer open must leave the discarded model closed", unit.isOpen());
	} finally {
		finishOpening.countDown();
		opening.cancel(true);
		try {
			opener.join(10_000);
			assertFalse("The discarded working-copy opener did not finish", opener.isAlive());
		} finally {
			try {
				unit.discardWorkingCopy();
				IBuffer buffer = BufferManager.getDefaultBufferManager().getBuffer(unit);
				if (buffer != null) {
					buffer.close();
				}
			} finally {
				deleteFile(path);
			}
		}
	}
}
}
