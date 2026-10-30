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

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import junit.framework.Test;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.BufferChangedEvent;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IBufferChangedListener;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.IElementInfo;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.core.Buffer;
import org.eclipse.jdt.internal.core.BufferCache;
import org.eclipse.jdt.internal.core.BufferManager;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.indexing.SourceIndexerEnvironment;
import org.eclipse.jdt.internal.core.search.matching.JavaSearchNameEnvironment;

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
 * or accessing its contents, registering a listener, or retrying forever.
 */
public void testClosedProviderBufferIsRejected() throws Exception {
	String path = "P/x/y/ClosedProvider.java";
	String source = "package x.y; public class ClosedProvider {}";
	createFile(path, source);
	waitUntilIndexesReady();
	class ClosedProviderBuffer extends Buffer {
		int readsAfterClose;
		int charArrayWritesAfterClose;
		int stringWritesAfterClose;
		int listenerAddsAfterClose;
		ClosedProviderBuffer(ICompilationUnit owner) {
			super((IFile) owner.getResource(), owner, false);
		}
		@Override
		public char[] getCharacters() {
			if (isClosed()) this.readsAfterClose++;
			return super.getCharacters();
		}
		@Override
		public void setContents(char[] contents) {
			if (isClosed()) this.charArrayWritesAfterClose++;
			super.setContents(contents);
		}
		@Override
		public void setContents(String contents) {
			if (isClosed()) this.stringWritesAfterClose++;
			super.setContents(contents);
		}
		@Override
		public synchronized void addBufferChangedListener(IBufferChangedListener listener) {
			if (isClosed()) this.listenerAddsAfterClose++;
			super.addBufferChangedListener(listener);
		}
	}
	AtomicReference<ICompilationUnit> attemptedWorkingCopy = new AtomicReference<>();
	AtomicReference<ClosedProviderBuffer> providerBuffer = new AtomicReference<>();
	WorkingCopyOwner owner = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			// Allow the bounded worker to terminate even if opening keeps retrying.
			if (Thread.currentThread().isInterrupted()) {
				throw new OperationCanceledException();
			}
			attemptedWorkingCopy.set(workingCopy);
			ClosedProviderBuffer buffer = providerBuffer.get();
			if (buffer == null) {
				buffer = new ClosedProviderBuffer(workingCopy);
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
		assertEquals("A closed provider buffer must not be read", 0, providerBuffer.get().readsAfterClose);
		assertEquals("A closed provider buffer must not be initialized with characters", 0, providerBuffer.get().charArrayWritesAfterClose);
		assertEquals("A closed provider buffer must not be initialized with a string", 0, providerBuffer.get().stringWritesAfterClose);
		assertEquals("A closed provider buffer must not receive a listener", 0, providerBuffer.get().listenerAddsAfterClose);
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

public void testDelayedNullPrimaryOpenRetriesAsOrdinaryFile() throws Exception {
	String path = "P/x/y/NullPrimary.java";
	String fileSource = "package x.y; public class NullPrimary { int file; }";
	String providerSource = "package x.y; public class NullPrimary { int provider; }";
	createFile(path, fileSource);
	waitUntilIndexesReady();
	WorkingCopyOwner previousProvider = DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider;
	CountDownLatch providerEntered = new CountDownLatch(1);
	CountDownLatch providerMayReturn = new CountDownLatch(1);
	AtomicReference<Thread> delayedThread = new AtomicReference<>();
	WorkingCopyOwner provider = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			if (Thread.currentThread() == delayedThread.get()) {
				providerEntered.countDown();
				awaitLatch(providerMayReturn, "The primary provider was not released");
				return null;
			}
			IBuffer buffer = BufferManager.createBuffer(workingCopy);
			buffer.setContents(providerSource);
			return buffer;
		}
	};
	ICompilationUnit unit = getCompilationUnit(path);
	FutureTask<IBuffer> opening = new FutureTask<>(unit::getBuffer);
	Thread opener = new Thread(opening, "Retrying a primary null provider after discard");
	opener.setDaemon(true);
	try {
		WorkingCopyOwner.setPrimaryBufferProvider(provider);
		unit.becomeWorkingCopy(null);
		unit.getBuffer().close();
		delayedThread.set(opener);
		opener.start();
		awaitLatch(providerEntered, "The primary provider was not entered");
		unit.discardWorkingCopy();
		assertFalse("Final discard must leave an ordinary primary unit", unit.isWorkingCopy());
		providerMayReturn.countDown();
		IBuffer result = opening.get(10, TimeUnit.SECONDS);
		assertNotNull("The stale null result must retry using the ordinary file", result);
		assertEquals("The retry must read the file, not the retired provider", fileSource, result.getContents());
		assertSame("The retry must publish the ordinary file buffer", result, BufferManager.getDefaultBufferManager().getBuffer(unit));
	} finally {
		providerMayReturn.countDown();
		opening.cancel(true);
		opener.join(10_000);
		try {
			assertFalse("The primary opener did not terminate", opener.isAlive());
			unit.discardWorkingCopy();
			unit.close();
		} finally {
			WorkingCopyOwner.setPrimaryBufferProvider(previousProvider);
			deleteFile(path);
		}
	}
}

public void testReentrantDiscardFromClosedCheckDoesNotPublishStaleBuffer() throws Exception {
	assertReentrantProviderQueryDoesNotPublishStaleBuffer(false);
}

public void testReentrantDiscardFromOwnerQueryDoesNotPublishStaleBuffer() throws Exception {
	assertReentrantProviderQueryDoesNotPublishStaleBuffer(true);
}

public void testOwnerQueryClosingCandidateDoesNotPublishClosedBuffer() throws Exception {
	String path = "P/x/y/OwnerCloses.java";
	createFile(path, "package x.y; public class OwnerCloses {}");
	waitUntilIndexesReady();
	AtomicReference<ICompilationUnit> handle = new AtomicReference<>();
	AtomicBoolean closed = new AtomicBoolean();
	WorkingCopyOwner owner = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			handle.set(workingCopy);
			return new Buffer((IFile) workingCopy.getResource(), workingCopy, false) {
				private boolean listenerRegistered;
				@Override
				public synchronized void addBufferChangedListener(IBufferChangedListener listener) {
					super.addBufferChangedListener(listener);
					this.listenerRegistered = true;
				}
				@Override
				public IOpenable getOwner() {
					if (this.listenerRegistered && closed.compareAndSet(false, true)) {
						close();
					}
					return super.getOwner();
				}
			};
		}
	};
	try {
		JavaModelException failure = null;
		try {
			getCompilationUnit(path).getWorkingCopy(owner, null);
		} catch (JavaModelException e) {
			failure = e;
		}
		assertTrue("The owner query must close the candidate", closed.get());
		assertNull("A candidate closed by its owner query must not be cached", BufferManager.getDefaultBufferManager().getBuffer(handle.get()));
		assertNotNull("Opening a closed candidate must fail", failure);
	} finally {
		try {
			if (handle.get() != null) {
				handle.get().discardWorkingCopy();
				handle.get().close();
			}
		} finally {
			deleteFile(path);
		}
	}
}

public void testReentrantDiscardDuringCandidateReleaseDoesNotRepublishModel() throws Exception {
	String path = "P/x/y/ReleaseDiscard.java";
	createFile(path, "package x.y; public class ReleaseDiscard {}");
	waitUntilIndexesReady();
	AtomicBoolean retired = new AtomicBoolean();
	AtomicReference<CompilationUnit> handle = new AtomicReference<>();
	AtomicReference<IBuffer> candidate = new AtomicReference<>();
	WorkingCopyOwner owner = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			handle.set((CompilationUnit) workingCopy);
			IBuffer buffer = new Buffer((IFile) workingCopy.getResource(), workingCopy, false) {
				@Override
				public IOpenable getOwner() {
					if (BufferManager.getDefaultBufferManager().getBuffer(workingCopy) == this && retired.compareAndSet(false, true)) {
						try {
							workingCopy.discardWorkingCopy();
						} catch (JavaModelException e) {
							throw new AssertionError(e);
						}
					}
					return super.getOwner();
				}
			};
			candidate.set(buffer);
			return buffer;
		}
	};
	try {
		JavaModelException failure = null;
		try {
			getCompilationUnit(path).getWorkingCopy(owner, null);
		} catch (JavaModelException e) {
			failure = e;
		}
		assertTrue("Candidate release must exercise the reentrant discard", retired.get());
		assertNull("Cleanup must not republish the discarded model", JavaModelManager.getJavaModelManager().getInfo(handle.get()));
		assertNull("Cleanup must leave no retired buffer", BufferManager.getDefaultBufferManager().getBuffer(handle.get()));
		assertNotNull("The discarded nonprimary working copy must fail to open", failure);
		assertTrue("The discarded working copy must be reported as nonexistent", failure.isDoesNotExist());
		assertTrue("The retired candidate must be closed", candidate.get().isClosed());
	} finally {
		try {
			if (handle.get() != null) {
				handle.get().discardWorkingCopy();
				handle.get().close();
			}
		} finally {
			deleteFile(path);
		}
	}
}

public void testReentrantDiscardDuringEvictionDoesNotPublishStaleBuffer() throws Exception {
	assertEvictionCallbackDoesNotPublishInvalidBuffer(false, false);
}

public void testReentrantDiscardDuringEvictedBufferCloseDoesNotRepublishModel() throws Exception {
	assertEvictionCallbackDoesNotPublishInvalidBuffer(true, false);
}

public void testEvictionQueryClosingCandidateDoesNotPublishClosedBuffer() throws Exception {
	assertEvictionCallbackDoesNotPublishInvalidBuffer(false, true);
}

private void assertEvictionCallbackDoesNotPublishInvalidBuffer(boolean duringClose, boolean closeCandidate) throws Exception {
	class LocalBufferManager extends BufferManager {
		void cache(IBuffer buffer) {
			addBuffer(buffer);
		}
	}
	String path = "P/x/y/EvictionDiscard.java";
	createFile(path, "package x.y; public class EvictionDiscard {}");
	waitUntilIndexesReady();
	LocalBufferManager buffers = new LocalBufferManager();
	Field cache = BufferManager.class.getDeclaredField("openBuffers");
	cache.setAccessible(true);
	((BufferCache<?>) cache.get(buffers)).setSpaceLimit(1);
	AtomicBoolean retired = new AtomicBoolean();
	AtomicReference<IBuffer> candidate = new AtomicReference<>();
	WorkingCopyOwner owner = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			IBuffer buffer = BufferManager.createBuffer(workingCopy);
			candidate.set(buffer);
			return buffer;
		}
	};
	ICompilationUnit original = getCompilationUnit(path);
	CompilationUnit unit = new CompilationUnit((PackageFragment) original.getParent(), original.getElementName(), owner) {
		@Override
		protected BufferManager getBufferManager() {
			return buffers;
		}
	};
	ICompilationUnit evictedOwner = getCompilationUnit("P/x/y/Evicted.java");
	IBuffer evicted = new Buffer(null, evictedOwner, false) {
		private void retire(boolean selectedCallback) {
			if (selectedCallback && retired.compareAndSet(false, true)) {
				try {
					if (closeCandidate) candidate.get().close();
					else unit.discardWorkingCopy();
				} catch (JavaModelException e) {
					throw new AssertionError(e);
				}
			}
		}
		@Override
		public boolean hasUnsavedChanges() {
			retire(!duringClose);
			return super.hasUnsavedChanges();
		}
		@Override
		public void close() {
			retire(duringClose);
			super.close();
		}
	};
	evicted.setContents("package x.y; class Evicted {}");
	buffers.cache(evicted);
	try {
		JavaModelException failure = null;
		try {
			unit.becomeWorkingCopy(null);
		} catch (JavaModelException e) {
			failure = e;
		}
		assertTrue("Eviction must exercise the provider callback", retired.get());
		assertNull("Eviction must not publish a retired or closed buffer", buffers.getBuffer(unit));
		assertNull("Eviction must not republish an invalid model", JavaModelManager.getJavaModelManager().getInfo(unit));
		assertNotNull("An invalidated nonprimary working copy must fail to open", failure);
		if (!closeCandidate) {
			assertTrue("The discarded working copy must be reported as nonexistent", failure.isDoesNotExist());
		}
		assertTrue("The retired candidate must be closed", candidate.get().isClosed());
		assertTrue("The evicted buffer must finish closing", evicted.isClosed());
	} finally {
		try {
			unit.discardWorkingCopy();
			unit.close();
			evicted.close();
		} finally {
			deleteFile(path);
		}
	}
}

public void testReentrantOwnerQueryDuringClosePreservesReplacement() throws Exception {
	assertReentrantOwnerQueryPreservesReplacement(false);
}

public void testReentrantOwnerQueryDuringRemovalPreservesReplacement() throws Exception {
	assertReentrantOwnerQueryPreservesReplacement(true);
}

private void assertReentrantOwnerQueryPreservesReplacement(boolean removeDirectly) throws Exception {
	class LocalBufferManager extends BufferManager {
		void cache(IBuffer buffer) {
			addBuffer(buffer);
		}
		void remove(IBuffer buffer) {
			removeBuffer(buffer);
		}
	}
	LocalBufferManager buffers = new LocalBufferManager();
	CompilationUnit unit = new CompilationUnit((PackageFragment) getPackageFragment("P", "", "x.y"), "OwnerCallback.java", DefaultWorkingCopyOwner.PRIMARY) {
		@Override
		protected BufferManager getBufferManager() {
			return buffers;
		}
	};
	IBuffer replacement = BufferManager.createBuffer(unit);
	replacement.setContents("package x.y; class OwnerCallback {}");
	AtomicBoolean armed = new AtomicBoolean();
	AtomicBoolean replaced = new AtomicBoolean();
	IBuffer old = new Buffer(null, unit, false) {
		private int queries;
		@Override
		public IOpenable getOwner() {
			if (armed.get() && ++this.queries >= 2 && replaced.compareAndSet(false, true)) {
				buffers.cache(replacement);
				replacement.addBufferChangedListener(unit);
				replacement.append(" // replacement edit");
			}
			return super.getOwner();
		}
	};
	old.close();
	buffers.cache(old);
	try {
		armed.set(true);
		if (removeDirectly) {
			buffers.remove(old);
		} else {
			unit.bufferChanged(new BufferChangedEvent(old, 0, 0, null));
		}
		// If removal only queried once, publish now, after it completed instead.
		old.getOwner();
		assertTrue("The provider query must publish the replacement", replaced.get());
		assertSame("An old buffer's removal must preserve a reentrant replacement", replacement, buffers.getBuffer(unit));
		assertFalse("The replacement's edit must remain inconsistent", unit.isConsistent());
	} finally {
		armed.set(false);
		replacement.close();
		buffers.remove(replacement);
		unit.close();
	}
}

private void assertReentrantProviderQueryDoesNotPublishStaleBuffer(boolean ownerQuery) throws Exception {
	String path = "P/x/y/ReentrantQuery.java";
	createFile(path, "package x.y; public class ReentrantQuery {}");
	waitUntilIndexesReady();
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	AtomicBoolean retired = new AtomicBoolean();
	AtomicReference<IBuffer> candidate = new AtomicReference<>();
	AtomicReference<CompilationUnit> handle = new AtomicReference<>();
	WorkingCopyOwner owner = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			handle.set((CompilationUnit) workingCopy);
			IBuffer buffer = new Buffer((IFile) workingCopy.getResource(), workingCopy, false) {
				private boolean listenerRegistered;
				@Override
				public synchronized void addBufferChangedListener(IBufferChangedListener listener) {
					super.addBufferChangedListener(listener);
					this.listenerRegistered = true;
				}
				private void retireIfPublishing(boolean selectedQuery) {
					if (selectedQuery && this.listenerRegistered && retired.compareAndSet(false, true)) {
						try {
							workingCopy.discardWorkingCopy();
						} catch (JavaModelException e) {
							throw new AssertionError(e);
						}
					}
				}
				@Override
				public boolean isClosed() {
					retireIfPublishing(!ownerQuery);
					return super.isClosed();
				}
				@Override
				public IOpenable getOwner() {
					retireIfPublishing(ownerQuery);
					return super.getOwner();
				}
			};
			candidate.set(buffer);
			return buffer;
		}
	};
	try {
		JavaModelException failure = null;
		try {
			getCompilationUnit(path).getWorkingCopy(owner, null);
		} catch (JavaModelException e) {
			failure = e;
		}
		assertTrue("The provider query must trigger final retirement", retired.get());
		assertNull("A reentrant retirement must not leave a stale buffer cached", BufferManager.getDefaultBufferManager().getBuffer(handle.get()));
		assertNull("A reentrant retirement must not republish model info", manager.getInfo(handle.get()));
		assertNotNull("A retired nonprimary opening must fail", failure);
		assertTrue("The retired working copy must be reported as nonexistent", failure.isDoesNotExist());
		assertTrue("An unpublished candidate must be closed", candidate.get().isClosed());
	} finally {
		try {
			if (handle.get() != null) {
				handle.get().discardWorkingCopy();
				handle.get().close();
			}
		} finally {
			deleteFile(path);
		}
	}
}

public void testDelayedNullInitialOpenMustNotRepublishDiscardedModel() throws Exception {
	assertDelayedNullProviderRejected(false);
}

public void testDelayedNullReopenMustRejectDiscardedGeneration() throws Exception {
	assertDelayedNullProviderRejected(true);
}

private void assertDelayedNullProviderRejected(boolean reopening) throws Exception {
	String name = reopening ? "Reopen" : "Initial";
	String path = "P/x/y/" + name + ".java";
	createFile(path, "package x.y; public class " + name + " {}");
	waitUntilIndexesReady();
	CountDownLatch providerEntered = new CountDownLatch(1);
	CountDownLatch providerMayReturn = new CountDownLatch(1);
	AtomicBoolean delay = new AtomicBoolean(!reopening);
	WorkingCopyOwner owner = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			if (!delay.get()) {
				return BufferManager.createBuffer(workingCopy);
			}
			providerEntered.countDown();
			try {
				assertTrue("Timed out waiting for the final discard", providerMayReturn.await(10, TimeUnit.SECONDS));
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new AssertionError(e);
			}
			return null;
		}
	};
	ICompilationUnit original = getCompilationUnit(path);
	CompilationUnit unit = new CompilationUnit((PackageFragment) original.getParent(), original.getElementName(), owner);
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	FutureTask<Object> opening = new FutureTask<>(() -> {
		if (reopening) {
			return unit.getBuffer();
		}
		unit.becomeWorkingCopy(null);
		return unit;
	});
	Thread opener = new Thread(opening, "Delayed null provider " + name);
	opener.setDaemon(true);
	try {
		if (reopening) {
			unit.becomeWorkingCopy(null);
			unit.getBuffer().close();
			assertTrue("Setup must leave the existing model open", unit.isOpen());
			delay.set(true);
		}
		opener.start();
		assertTrue("The opener must have entered the provider", providerEntered.await(10, TimeUnit.SECONDS));
		assertNotNull("The opening attempt must have a working-copy registration", unit.getPerWorkingCopyInfo());
		unit.discardWorkingCopy();
		assertNull("Final discard must remove the working-copy registration", unit.getPerWorkingCopyInfo());
		assertNull("Final discard must leave no model info", manager.getInfo(unit));
		providerMayReturn.countDown();
		Throwable failure = null;
		try {
			opening.get(10, TimeUnit.SECONDS);
		} catch (ExecutionException e) {
			failure = e.getCause();
		}
		assertNull("A delayed null provider must not republish a discarded model", manager.getInfo(unit));
		assertTrue("A stale null-returning provider must fail with JavaModelException", failure instanceof JavaModelException);
		assertTrue("The discarded working copy must be reported as nonexistent", ((JavaModelException) failure).isDoesNotExist());
	} finally {
		providerMayReturn.countDown();
		opening.cancel(true);
		opener.join(10_000);
		try {
			assertFalse("The diagnostic worker must terminate", opener.isAlive());
		} finally {
			try {
				unit.discardWorkingCopy();
				unit.close();
			} finally {
				deleteFile(path);
			}
		}
	}
}

public void testNullProviderWithoutConcurrentDiscardRemainsSupported() throws Exception {
	String path = "P/x/y/NullControl.java";
	createFile(path, "package x.y; public class NullControl {}");
	waitUntilIndexesReady();
	WorkingCopyOwner owner = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			return null;
		}
	};
	ICompilationUnit original = getCompilationUnit(path);
	CompilationUnit unit = new CompilationUnit((PackageFragment) original.getParent(), original.getElementName(), owner);
	try {
		unit.becomeWorkingCopy(null);
		assertTrue("A normal null provider must still allow model construction", unit.isOpen());
		assertNotNull("A normal null provider must retain the working-copy registration", unit.getPerWorkingCopyInfo());
		assertNull("A normal null provider must not manufacture a physical buffer", unit.getBuffer());
	} finally {
		try {
			unit.discardWorkingCopy();
			unit.close();
		} finally {
			deleteFile(path);
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

/**
 * A failed opener must not close a shared candidate while another opener is
 * still preparing it and neither has published a buffer yet.
 */
public void testFailedOpenerPreservesSharedUnpublishedBuffer() throws Exception {
	String path = "P/x/y/SharedCandidate.java";
	String source = "package x.y; public class SharedCandidate {}";
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
				deleteFile(path);
			}
		}
	}
}

/**
 * An ordinary open must notice a complete working-copy generation even when
 * the unit is ordinary again before its old source read finishes.
 */
public void testOrdinaryBufferReadIsRetriedAfterWorkingCopyRoundTrip() throws Exception {
	String path = "P/x/y/GenerationRoundTrip.java";
	String source = "package x.y; public class GenerationRoundTrip {}";
	String changedSource = "package x.y; public class GenerationRoundTrip { int changed; }";
	createFile(path, source);
	waitUntilIndexesReady();
	ICompilationUnit original = getCompilationUnit(path);
	original.close();
	IFile file = (IFile) original.getResource();
	CountDownLatch sourceRead = new CountDownLatch(1);
	CountDownLatch finishReading = new CountDownLatch(1);
	AtomicBoolean delayReading = new AtomicBoolean(true);
	IFile delayedFile = (IFile) Proxy.newProxyInstance(IFile.class.getClassLoader(), new Class<?>[] { IFile.class },
			(_, method, arguments) -> {
				Object result;
				try {
					result = method.invoke(file, arguments);
				} catch (InvocationTargetException e) {
					throw e.getCause();
				}
				if ("readAllChars".equals(method.getName()) && delayReading.compareAndSet(true, false)) {
					// The real file has already been read; hold that old result until after the transition.
					sourceRead.countDown();
					awaitLatch(finishReading, "Timed out waiting to finish the ordinary buffer's source read");
				}
				return result;
			});
	class DelayedReadUnit extends CompilationUnit {
		DelayedReadUnit() {
			super((PackageFragment) original.getParent(), original.getElementName(), DefaultWorkingCopyOwner.PRIMARY);
		}
		@Override
		public IResource getResource() {
			return delayedFile;
		}
		IBuffer openBufferForTest() throws JavaModelException {
			return super.openBuffer(null, null);
		}
	}
	DelayedReadUnit unit = new DelayedReadUnit();
	FutureTask<IBuffer> opening = new FutureTask<>(unit::openBufferForTest);
	Thread opener = new Thread(opening, "Reading across a working-copy generation");
	opener.setDaemon(true);
	WorkingCopyOwner previousProvider = DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider;
	WorkingCopyOwner.setPrimaryBufferProvider(new WorkingCopyOwner() {});
	try {
		opener.start();
		if (!sourceRead.await(10, TimeUnit.SECONDS)) {
			if (opening.isDone()) {
				opening.get(10, TimeUnit.SECONDS); // Surface an early opener failure instead of hiding it behind the latch timeout.
			}
			fail("The ordinary opener did not read the original source");
		}
		unit.becomeWorkingCopy(null);
		unit.discardWorkingCopy();
		assertFalse("The unit must be ordinary again", unit.isWorkingCopy());
		editFile(path, changedSource);
		assertNull("The old source read must not have a cached winner", BufferManager.getDefaultBufferManager().getBuffer(unit));
		finishReading.countDown();
		IBuffer buffer = opening.get(10, TimeUnit.SECONDS);
		assertEquals("The old opener must reread source after the working-copy generation", changedSource, buffer.getContents());
		assertSame("The fresh ordinary buffer must be cached", buffer, BufferManager.getDefaultBufferManager().getBuffer(unit));
	} finally {
		finishReading.countDown();
		opening.cancel(true);
		try {
			opener.join(10_000);
			assertFalse("The ordinary source reader did not finish", opener.isAlive());
		} finally {
			try {
				unit.discardWorkingCopy();
				unit.close();
				IBuffer buffer = BufferManager.getDefaultBufferManager().getBuffer(unit);
				if (buffer != null) {
					buffer.close();
				}
			} finally {
				WorkingCopyOwner.setPrimaryBufferProvider(previousProvider);
				deleteFile(path);
			}
		}
	}
}

/**
 * A provider may compare listeners by identity. Discard through an equivalent
 * handle must remove the exact listener previously registered on its buffer.
 */
public void testDiscardThroughEqualHandleRemovesRegisteredListener() throws Exception {
	String path = "P/x/y/ListenerIdentity.java";
	createFile(path, "package x.y; public class ListenerIdentity {}");
	waitUntilIndexesReady();
	class IdentityListenerBuffer extends Buffer {
		final IdentityHashMap<IBufferChangedListener, Boolean> listeners = new IdentityHashMap<>();
		IBufferChangedListener registeredListener;
		IBufferChangedListener removedListener;
		int listenersAtClose = -1;
		IdentityListenerBuffer(ICompilationUnit owner) {
			super((IFile) owner.getResource(), owner, false);
		}
		@Override
		public synchronized void addBufferChangedListener(IBufferChangedListener listener) {
			this.listeners.put(listener, Boolean.TRUE);
			this.registeredListener = listener;
		}
		@Override
		public synchronized void removeBufferChangedListener(IBufferChangedListener listener) {
			this.listeners.remove(listener);
			this.removedListener = listener;
		}
		@Override
		protected void notifyChanged(BufferChangedEvent event) {
			for (IBufferChangedListener listener : new ArrayList<>(this.listeners.keySet())) {
				listener.bufferChanged(event);
			}
		}
		@Override
		public void close() {
			if (isClosed()) return;
			this.listenersAtClose = this.listeners.size();
			super.close();
			this.listeners.clear();
		}
	}
	ICompilationUnit original = getCompilationUnit(path);
	WorkingCopyOwner owner = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			CompilationUnit bufferOwner = new CompilationUnit((PackageFragment) workingCopy.getParent(),
					workingCopy.getElementName(), workingCopy.getOwner());
			return new IdentityListenerBuffer(bufferOwner);
		}
	};
	CompilationUnit opened = new CompilationUnit((PackageFragment) original.getParent(), original.getElementName(), owner);
	CompilationUnit discarded = new CompilationUnit((PackageFragment) original.getParent(), original.getElementName(), owner);
	assertNotSame("The handles must differ by identity", opened, discarded);
	assertEquals("The handles must represent the same working copy", opened, discarded);
	try {
		opened.becomeWorkingCopy(null);
		IdentityListenerBuffer buffer = (IdentityListenerBuffer) opened.getBuffer();
		assertNotNull("Opening must register a buffer listener", buffer.registeredListener);
		discarded.discardWorkingCopy();
		assertSame("Discard must remove the exact listener that was registered", buffer.registeredListener, buffer.removedListener);
		assertEquals("The model listener must be detached before the provider closes", 0, buffer.listenersAtClose);
		assertTrue("Final discard must close the provider buffer", buffer.isClosed());
	} finally {
		try {
			opened.discardWorkingCopy();
		} finally {
			deleteFile(path);
		}
	}
}

/**
 * Detaching the model listener before close must still invalidate the opening
 * handle's cached AST, including when another equivalent handle discards it.
 */
public void testFinalDiscardInvalidatesOpeningHandlesAST() throws Exception {
	String path = "P/x/y/CachedAST.java";
	String source = "package x.y; public class CachedAST { void before() {} }";
	String changedSource = "package x.y; public class CachedAST { void after() {} }";
	createFile(path, source);
	waitUntilIndexesReady();
	ICompilationUnit original = getCompilationUnit(path);
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	CompilationUnit opened = new CompilationUnit((PackageFragment) original.getParent(), original.getElementName(), owner);
	CompilationUnit discarded = new CompilationUnit((PackageFragment) original.getParent(), original.getElementName(), owner);
	try {
		opened.becomeWorkingCopy(null);
		var oldAST = opened.getOrBuildAST(owner, -1);
		assertNotNull("The initial working copy must produce an AST", oldAST);
		assertEquals("before", ((TypeDeclaration) oldAST.types().get(0)).getMethods()[0].getName().getIdentifier());

		discarded.discardWorkingCopy();
		editFile(path, changedSource);
		opened.becomeWorkingCopy(null);
		assertEquals("The reopened working copy must read the changed source", changedSource, opened.getBuffer().getContents());
		var newAST = opened.getOrBuildAST(owner, -1);
		assertNotNull("The reopened working copy must produce an AST", newAST);
		assertNotSame("Final discard must invalidate the opening handle's AST", oldAST, newAST);
		assertEquals("after", ((TypeDeclaration) newAST.types().get(0)).getMethods()[0].getName().getIdentifier());
	} finally {
		try {
			opened.discardWorkingCopy();
		} finally {
			deleteFile(path);
		}
	}
}

/**
 * Competing equal handles can register the same physical buffer before either
 * publishes it. The first actual listener still owns an AST to invalidate.
 */
public void testConcurrentSharedBufferDiscardInvalidatesActualListenersAST() throws Exception {
	assertConcurrentSharedBufferLifecycle(false, false);
}

/** Identity-based providers must not retain a losing opener's model listener. */
public void testConcurrentSharedBufferDiscardRemovesIdentityListeners() throws Exception {
	assertConcurrentSharedBufferLifecycle(true, false);
}

/** Both equal handles must read edited ASTs even after one reconciles the shared model. */
public void testConcurrentSharedBufferEditInvalidatesBothHandlesASTs() throws Exception {
	assertConcurrentSharedBufferLifecycle(false, true);
}

/** An identity-based provider must preserve edited AST visibility for both opening handles. */
public void testConcurrentSharedIdentityBufferEditInvalidatesBothHandlesASTs() throws Exception {
	assertConcurrentSharedBufferLifecycle(true, true);
}

private void assertConcurrentSharedBufferLifecycle(boolean identityListeners, boolean editSharedBuffer) throws Exception {
	String path = "P/x/y/ConcurrentSharedAST.java";
	String source = "package x.y; public class ConcurrentSharedAST { void before() {} }";
	String changedSource = "package x.y; public class ConcurrentSharedAST { void after() {} }";
	createFile(path, source);
	waitUntilIndexesReady();
	ICompilationUnit original = getCompilationUnit(path);
	CountDownLatch firstListenerRegistered = new CountDownLatch(1);
	CountDownLatch letFirstOpenerPublish = new CountDownLatch(1);
	AtomicReference<Thread> firstOpenerThread = new AtomicReference<>();
	AtomicReference<IBuffer> provided = new AtomicReference<>();
	WorkingCopyOwner owner = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			IBuffer shared = provided.get();
			return shared == null ? BufferManager.createBuffer(workingCopy) : shared;
		}
	};
	CompilationUnit first = new CompilationUnit((PackageFragment) original.getParent(), original.getElementName(), owner);
	CompilationUnit winner = new CompilationUnit((PackageFragment) original.getParent(), original.getElementName(), owner);
	assertNotSame("The concurrent handles must be distinct objects", first, winner);
	assertEquals("The concurrent handles must represent one working copy", first, winner);
	class SharedBuffer extends Buffer {
		final IdentityHashMap<IBufferChangedListener, Boolean> listeners = new IdentityHashMap<>();
		int listenersAtClose = -1;
		SharedBuffer() {
			super((IFile) first.getResource(), first, false);
		}
		@Override
		// The competing opener must register while this opener is paused.
		@SuppressWarnings("sync-override")
		public void addBufferChangedListener(IBufferChangedListener listener) {
			if (identityListeners) {
				synchronized (this.listeners) {
					this.listeners.put(listener, Boolean.TRUE);
				}
			} else {
				// Exercise the real Core Buffer's equality-based listener list.
				super.addBufferChangedListener(listener);
			}
			if (Thread.currentThread() == firstOpenerThread.get()) {
				firstListenerRegistered.countDown();
				// Wait after registration without holding a provider monitor.
				awaitLatch(letFirstOpenerPublish, "The first opener was not released");
			}
		}
		@Override
		// Listener access is guarded by its own monitor or the superclass.
		@SuppressWarnings("sync-override")
		public void removeBufferChangedListener(IBufferChangedListener listener) {
			if (identityListeners) {
				synchronized (this.listeners) {
					this.listeners.remove(listener);
				}
			} else {
				super.removeBufferChangedListener(listener);
			}
		}
		@Override
		protected void notifyChanged(BufferChangedEvent event) {
			if (!identityListeners) {
				super.notifyChanged(event);
				return;
			}
			ArrayList<IBufferChangedListener> snapshot;
			synchronized (this.listeners) {
				snapshot = new ArrayList<>(this.listeners.keySet());
			}
			for (IBufferChangedListener listener : snapshot) {
				listener.bufferChanged(event);
			}
		}
		@Override
		public void close() {
			if (isClosed()) return;
			synchronized (this.listeners) {
				this.listenersAtClose = this.listeners.size();
			}
			super.close();
			synchronized (this.listeners) {
				this.listeners.clear();
			}
		}
	}
	SharedBuffer shared = new SharedBuffer();
	FutureTask<IBuffer> firstOpening = new FutureTask<>(first::getBuffer);
	FutureTask<IBuffer> winningOpening = new FutureTask<>(winner::getBuffer);
	Thread firstThread = new Thread(firstOpening, "First shared-buffer listener");
	Thread winnerThread = new Thread(winningOpening, "Shared-buffer publication winner");
	firstThread.setDaemon(true);
	winnerThread.setDaemon(true);
	firstOpenerThread.set(firstThread);
	try {
		first.becomeWorkingCopy(null);
		first.getBuffer().close();
		assertTrue("Closing the initial buffer must leave the model open", first.isOpen());
		assertNull("The initial buffer must be removed from the cache", BufferManager.getDefaultBufferManager().getBuffer(first));
		provided.set(shared);

		firstThread.start();
		assertTrue("The first opener did not register its listener", firstListenerRegistered.await(10, TimeUnit.SECONDS));
		assertNull("The first opener must not have published yet", BufferManager.getDefaultBufferManager().getBuffer(first));
		winnerThread.start();
		assertSame("The second opener must publish the shared buffer first", shared, winningOpening.get(10, TimeUnit.SECONDS));
		assertSame("The second opener must have cached the shared buffer", shared, BufferManager.getDefaultBufferManager().getBuffer(winner));
		letFirstOpenerPublish.countDown();
		assertSame("The first opener must receive the same published buffer", shared, firstOpening.get(10, TimeUnit.SECONDS));

		if (editSharedBuffer) {
			assertSharedBufferEditInvalidatesBothHandlesASTs(first, winner, owner, shared, changedSource);
			return;
		}
		var oldAST = first.getOrBuildAST(owner, -1);
		assertNotNull("The first handle must cache an AST", oldAST);
		assertEquals("before", ((TypeDeclaration) oldAST.types().get(0)).getMethods()[0].getName().getIdentifier());

		winner.discardWorkingCopy();
		assertTrue("Final discard must close the shared buffer", shared.isClosed());
		if (identityListeners) {
			assertEquals("Final discard must detach every registered model listener before close", 0, shared.listenersAtClose);
		}
		assertFalse("Final discard must close the working-copy model", winner.isOpen());
		editFile(path, changedSource);
		provided.set(null);
		winner.becomeWorkingCopy(null);
		assertEquals("The first handle must see the replacement buffer's changed source", changedSource, first.getBuffer().getContents());
		var newAST = first.getOrBuildAST(owner, -1);
		assertNotNull("The first handle must produce an AST after reopening", newAST);
		String method = ((TypeDeclaration) newAST.types().get(0)).getMethods()[0].getName().getIdentifier();
		assertNotSame("Final discard after concurrent shared-buffer publication must invalidate the actual listener handle's AST", oldAST, newAST);
		assertEquals("The AST must reflect the replacement buffer", "after", method);
	} finally {
		letFirstOpenerPublish.countDown();
		firstOpening.cancel(true);
		winningOpening.cancel(true);
		try {
			firstThread.join(10_000);
			winnerThread.join(10_000);
			assertFalse("The first opener did not terminate", firstThread.isAlive());
			assertFalse("The second opener did not terminate", winnerThread.isAlive());
		} finally {
			try {
				winner.discardWorkingCopy();
				first.discardWorkingCopy();
			} finally {
				deleteFile(path);
			}
		}
	}
}

private void assertSharedBufferEditInvalidatesBothHandlesASTs(CompilationUnit first, CompilationUnit second,
		WorkingCopyOwner owner, IBuffer buffer, String changedSource) throws Exception {
	var oldFirstAST = first.getOrBuildAST(owner, -1);
	var oldSecondAST = second.getOrBuildAST(owner, -1);
	assertNotNull("The first handle must produce an AST", oldFirstAST);
	assertNotNull("The second handle must produce an AST", oldSecondAST);
	assertEquals("before", ((TypeDeclaration) oldFirstAST.types().get(0)).getMethods()[0].getName().getIdentifier());
	assertEquals("before", ((TypeDeclaration) oldSecondAST.types().get(0)).getMethods()[0].getName().getIdentifier());
	assertSame("The unchanged registered handle must reuse its AST", oldFirstAST, first.getOrBuildAST(owner, -1));

	buffer.setContents(changedSource);
	assertEquals("Both handles must expose the edited shared buffer", changedSource, second.getBuffer().getContents());
	first.makeConsistent(null);
	assertTrue("Reconciliation through the first handle must make the shared model consistent", first.isConsistent());
	assertTrue("The second handle must already see a consistent model", second.isConsistent());
	var newFirstAST = first.getOrBuildAST(owner, -1);
	var newSecondAST = second.getOrBuildAST(owner, -1);
	assertEquals("The first handle's AST must reflect the shared-buffer edit", "after",
			((TypeDeclaration) newFirstAST.types().get(0)).getMethods()[0].getName().getIdentifier());
	assertEquals("The second handle's AST must reflect the edit even after another handle reconciles", "after",
			((TypeDeclaration) newSecondAST.types().get(0)).getMethods()[0].getName().getIdentifier());
}

/** Opening unchanged file-backed units must preserve the retained indexer environment. */
public void testOrdinaryBufferPublicationPreservesIndexingEnvironment() throws Exception {
	String firstPath = "P/x/y/IndexOrdinaryFirst.java";
	String secondPath = "P/x/y/IndexOrdinarySecond.java";
	createFile(firstPath, "package x.y; public class IndexOrdinaryFirst {}");
	createFile(secondPath, "package x.y; public class IndexOrdinarySecond {}");
	ICompilationUnit first = getCompilationUnit(firstPath);
	ICompilationUnit second = getCompilationUnit(secondPath);
	IndexManager indexManager = JavaModelManager.getIndexManager();
	boolean previousKeep = SourceIndexerEnvironment.KEEP;
	try {
		waitUntilIndexesReady();
		first.close();
		second.close();
		SourceIndexerEnvironment.KEEP = true;
		// Resolve the classpath and settle indexing before measuring buffer publication.
		retainedIndexingEnvironment(first);
		waitUntilIndexesReady();
		SourceIndexerEnvironment before = retainedIndexingEnvironment(first);
		assertFalse("The first unit must be an ordinary compilation unit", first.isWorkingCopy());
		assertFalse("The second unit must be an ordinary compilation unit", second.isWorkingCopy());
		assertNull("The first file buffer must genuinely be uncached", BufferManager.getDefaultBufferManager().getBuffer(first));
		assertNull("The second file buffer must genuinely be uncached", BufferManager.getDefaultBufferManager().getBuffer(second));
		int countBefore = indexManager.getSourceEnvironmentChangeCount();

		assertNotNull("The first file must publish a buffer", first.getBuffer());
		assertNotNull("The second file must publish a buffer", second.getBuffer());

		assertEquals("Opening unchanged ordinary file buffers must not invalidate source resolution",
				countBefore, indexManager.getSourceEnvironmentChangeCount());
		assertSame("Ordinary file buffer publication must preserve the exact retained indexing environment",
				before, retainedIndexingEnvironment(first));
	} finally {
		SourceIndexerEnvironment.KEEP = previousKeep;
		try {
			first.close();
			second.close();
		} finally {
			deleteFile(firstPath);
			deleteFile(secondPath);
		}
	}
}

/**
 * A clean replacement in the same primary working-copy generation needs its own
 * notification; the old buffer's earlier close cannot invalidate a later snapshot.
 */
public void testCleanWorkingCopyBufferRepublicationInvalidatesIndexingEnvironment() throws Exception {
	String path = "P/x/y/IndexRepublication.java";
	String beforeSource = "package x.y; public class IndexRepublication { void before() {} }";
	String afterSource = "package x.y; public class IndexRepublication { void after() {} }";
	createFile(path, beforeSource);
	waitUntilIndexesReady();
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	IndexManager indexManager = JavaModelManager.getIndexManager();
	WorkingCopyOwner previousProvider = DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider;
	boolean previousKeep = SourceIndexerEnvironment.KEEP;
	AtomicReference<String> providerContents = new AtomicReference<>(beforeSource);
	WorkingCopyOwner provider = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			IBuffer buffer = BufferManager.createBuffer(workingCopy);
			buffer.setContents(providerContents.get());
			return buffer;
		}
	};
	ICompilationUnit unit = getCompilationUnit(path);
	try {
		WorkingCopyOwner.setPrimaryBufferProvider(provider);
		SourceIndexerEnvironment.KEEP = true;
		unit.becomeWorkingCopy(null);
		IBuffer oldBuffer = unit.getBuffer();
		Object generation = ((CompilationUnit) unit).getPerWorkingCopyInfo();
		retainedIndexingEnvironment(unit);
		// The readiness helper searches primary copies and may call getBuffer(),
		// so finish it before closing the buffer whose replacement is under test.
		waitUntilIndexesReady();
		oldBuffer.close();
		assertTrue("The old buffer's close event must be complete", oldBuffer.isClosed());
		assertNull("The old buffer must no longer be cached", BufferManager.getDefaultBufferManager().getBuffer(unit));
		assertTrue("Closing the old buffer must preserve the working-copy model", unit.isOpen());
		// Capture only after the old close, so it cannot mask a missing publication notification.
		SourceIndexerEnvironment before = retainedIndexingEnvironment(unit);
		ICompilationUnit[] copiesBefore = manager.getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, true);
		assertNull("Capturing the baseline must leave the replacement unpublished", BufferManager.getDefaultBufferManager().getBuffer(unit));
		int countBefore = indexManager.getSourceEnvironmentChangeCount();
		providerContents.set(afterSource);

		IBuffer replacement = unit.getBuffer();

		assertNotSame("Republication must create a new physical buffer", oldBuffer, replacement);
		assertEquals("The clean provider source must survive publication", afterSource, replacement.getContents());
		assertFalse("The provider must not need an edit event to expose its source", replacement.hasUnsavedChanges());
		assertSame("Republication must stay in the same working-copy generation", generation, ((CompilationUnit) unit).getPerWorkingCopyInfo());
		assertTrue("Republication must preserve the working-copy handle list",
				Arrays.equals(copiesBefore, manager.getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, true)));
		assertTrue("A clean working-copy publication must independently advance the source environment",
				indexManager.getSourceEnvironmentChangeCount() > countBefore);
		assertNotSame("The snapshot taken after the old close must be invalidated by the replacement publication",
				before, retainedIndexingEnvironment(unit));
	} finally {
		try {
			unit.discardWorkingCopy();
		} finally {
			SourceIndexerEnvironment.KEEP = previousKeep;
			WorkingCopyOwner.setPrimaryBufferProvider(previousProvider);
			deleteFile(path);
		}
	}
}

/** A working-copy generation can retire with model info but no physical buffer. */
public void testBufferlessWorkingCopyRetirementInvalidatesIndexingEnvironment() throws Exception {
	String path = "P/x/y/IndexBufferless.java";
	String source = "package x.y; public class IndexBufferless {}";
	createFile(path, source);
	waitUntilIndexesReady();
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	IndexManager indexManager = JavaModelManager.getIndexManager();
	WorkingCopyOwner previousProvider = DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider;
	boolean previousKeep = SourceIndexerEnvironment.KEEP;
	WorkingCopyOwner provider = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			return null;
		}
	};
	ICompilationUnit unit = getCompilationUnit(path);
	try {
		WorkingCopyOwner.setPrimaryBufferProvider(provider);
		SourceIndexerEnvironment.KEEP = true;
		unit.becomeWorkingCopy(null);
		assertTrue("A null provider buffer must still allow an open working-copy model", unit.isOpen());
		assertNotNull("The working-copy model info must be retained", manager.getInfo(unit));
		assertNull("The provider must not leave a physical buffer", BufferManager.getDefaultBufferManager().getBuffer(unit));
		assertEquals("The bufferless compilation unit must read its file source", source, new String(((CompilationUnit) unit).getContents()));
		waitUntilIndexesReady();
		SourceIndexerEnvironment before = retainedIndexingEnvironment(unit);
		int countBefore = indexManager.getSourceEnvironmentChangeCount();

		unit.becomeWorkingCopy(null);
		assertEquals("Retaining a generation must not invalidate the source environment", countBefore, indexManager.getSourceEnvironmentChangeCount());
		unit.discardWorkingCopy();
		assertTrue("A nonfinal discard must retain the working copy", unit.isWorkingCopy());
		assertEquals("A nonfinal discard must not invalidate the source environment", countBefore, indexManager.getSourceEnvironmentChangeCount());
		assertSame("Retain and nonfinal discard must preserve the retained environment", before, retainedIndexingEnvironment(unit));

		unit.discardWorkingCopy();

		assertFalse("Final discard must retire the working-copy generation", unit.isWorkingCopy());
		assertNull("Final discard must remove the model info", manager.getInfo(unit));
		assertNull("Final discard must leave no physical buffer", BufferManager.getDefaultBufferManager().getBuffer(unit));
		assertTrue("A generation's final retirement must invalidate even without a cached buffer",
				indexManager.getSourceEnvironmentChangeCount() > countBefore);
		assertNotSame("The retired bufferless generation must not retain its indexing environment", before, retainedIndexingEnvironment(unit));
	} finally {
		try {
			unit.discardWorkingCopy();
			if (unit.isWorkingCopy()) {
				// Balance the extra retain if an assertion failed before the nonfinal discard.
				unit.discardWorkingCopy();
			}
		} finally {
			SourceIndexerEnvironment.KEEP = previousKeep;
			WorkingCopyOwner.setPrimaryBufferProvider(previousProvider);
			deleteFile(path);
		}
	}
}

/** A real indexer package lookup must wait for bufferless model cleanup. */
public void testParallelIndexerSeesCompletedBufferlessRetirement() throws Exception {
	String path = "P/x/y/IndexParallel.java";
	String fileSource = "package x.y; public class IndexParallel { int file; }";
	String providerSource = "package previous; public class IndexParallel { int provider; }";
	createFile(path, fileSource);
	waitUntilIndexesReady();
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	IndexManager indexManager = JavaModelManager.getIndexManager();
	WorkingCopyOwner previousProvider = DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider;
	boolean previousKeep = SourceIndexerEnvironment.KEEP;
	AtomicBoolean bufferless = new AtomicBoolean();
	AtomicBoolean pauseCleanup = new AtomicBoolean();
	AtomicReference<Thread> indexingThread = new AtomicReference<>();
	AtomicReference<String> observedPackage = new AtomicReference<>();
	CountDownLatch packageRequested = new CountDownLatch(1);
	CountDownLatch continueLookup = new CountDownLatch(1);
	CountDownLatch cleanupEntered = new CountDownLatch(1);
	CountDownLatch finishCleanup = new CountDownLatch(1);
	WorkingCopyOwner provider = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			if (bufferless.get()) return null;
			IBuffer buffer = BufferManager.createBuffer(workingCopy);
			buffer.setContents(providerSource);
			return buffer;
		}
	};
	ICompilationUnit original = getCompilationUnit(path);
	CompilationUnit unit = new CompilationUnit((PackageFragment) original.getParent(), original.getElementName(), DefaultWorkingCopyOwner.PRIMARY) {
		@Override
		public IPackageDeclaration[] getPackageDeclarations() throws JavaModelException {
			boolean indexing = Thread.currentThread() == indexingThread.get();
			if (indexing) {
				packageRequested.countDown();
				awaitLatch(continueLookup, "The package lookup was not released");
			}
			IPackageDeclaration[] packages = super.getPackageDeclarations();
			if (indexing) {
				observedPackage.set(packages.length == 0 ? "" : packages[0].getElementName());
			}
			return packages;
		}
		@Override
		protected void closing(Object info) {
			if (pauseCleanup.compareAndSet(true, false)) {
				assertTrue("Model cleanup must hold the manager monitor", Thread.holdsLock(manager));
				cleanupEntered.countDown();
				awaitLatch(finishCleanup, "Model cleanup was not released");
			}
			super.closing(info);
		}
	};
	Method acquire = IndexManager.class.getDeclaredMethod("acquireSourceIndexerEnvironment", JavaProject.class);
	Method release = IndexManager.class.getDeclaredMethod("releaseSourceIndexerEnvironment", SourceIndexerEnvironment.class);
	acquire.setAccessible(true);
	release.setAccessible(true);
	FutureTask<SourceIndexerEnvironment> acquiring = new FutureTask<>(() ->
			(SourceIndexerEnvironment) invoke(acquire, indexManager, unit.getJavaProject()));
	Thread indexer = new Thread(acquiring, "Reading packages during bufferless retirement");
	FutureTask<Void> discarding = new FutureTask<>(() -> { unit.discardWorkingCopy(); return null; });
	Thread discarder = new Thread(discarding, "Retiring a bufferless primary working copy");
	indexer.setDaemon(true);
	discarder.setDaemon(true);
	SourceIndexerEnvironment held = null;
	SourceIndexerEnvironment acquired = null;
	SourceIndexerEnvironment after = null;
	try {
		WorkingCopyOwner.setPrimaryBufferProvider(provider);
		SourceIndexerEnvironment.KEEP = true;
		unit.becomeWorkingCopy(null);
		assertEquals("The old model must describe the provider's package", "previous", unit.getPackageDeclarations()[0].getElementName());
		waitUntilIndexesReady();
		// Keep any retained environment checked out so the worker must build a new one.
		held = (SourceIndexerEnvironment) invoke(acquire, indexManager, unit.getJavaProject());
		bufferless.set(true);
		unit.getBuffer().close();
		assertNotNull("The bufferless generation must retain its old model", manager.getInfo(unit));
		assertNull("Retirement must start without a cached buffer", BufferManager.getDefaultBufferManager().getBuffer(unit));
		indexingThread.set(indexer);
		indexer.start();
		awaitLatch(packageRequested, "The indexer did not reach the working-copy package lookup");
		pauseCleanup.set(true);
		discarder.start();
		awaitLatch(cleanupEntered, "Retirement did not enter model cleanup");
		continueLookup.countDown();
		assertBlockedOnManager(indexer, discarder, manager);
		finishCleanup.countDown();
		discarding.get(10, TimeUnit.SECONDS);
		acquired = acquiring.get(10, TimeUnit.SECONDS);
		assertEquals("The waiting indexer must read the completed file model", "x.y", observedPackage.get());
		assertIndexingSource(acquired, fileSource);
		invoke(release, indexManager, acquired);
		after = (SourceIndexerEnvironment) invoke(acquire, indexManager, unit.getJavaProject());
		assertNotSame("An environment captured before retirement must not be reused", acquired, after);
		assertIndexingSource(after, fileSource);
	} finally {
		continueLookup.countDown();
		finishCleanup.countDown();
		pauseCleanup.set(false);
		acquiring.cancel(true);
		discarding.cancel(true);
		indexer.join(10_000);
		discarder.join(10_000);
		try {
			assertFalse("The indexer worker did not terminate", indexer.isAlive());
			assertFalse("The discard worker did not terminate", discarder.isAlive());
			if (held != null) invoke(release, indexManager, held);
			if (after != null) invoke(release, indexManager, after);
			else if (acquired != null) invoke(release, indexManager, acquired);
			unit.discardWorkingCopy();
			unit.close();
		} finally {
			WorkingCopyOwner.setPrimaryBufferProvider(previousProvider);
			SourceIndexerEnvironment.KEEP = previousKeep;
			deleteFile(path);
		}
	}
}

private static void assertBlockedOnManager(Thread reader, Thread owner, JavaModelManager manager) throws Exception {
	long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
	while (System.nanoTime() < deadline) {
		ThreadInfo info = ManagementFactory.getThreadMXBean().getThreadInfo(reader.threadId());
		if (info != null && info.getThreadState() == Thread.State.BLOCKED
				&& info.getLockOwnerId() == owner.threadId() && info.getLockInfo() != null
				&& info.getLockInfo().getIdentityHashCode() == System.identityHashCode(manager)) {
			return;
		}
		Thread.sleep(1);
	}
	fail("The indexer did not block on the manager monitor held by model cleanup");
}

private static void assertIndexingSource(SourceIndexerEnvironment environment, String source) throws Exception {
	Field field = SourceIndexerEnvironment.class.getDeclaredField("nameEnvironment");
	field.setAccessible(true);
	JavaSearchNameEnvironment names = (JavaSearchNameEnvironment) field.get(environment);
	NameEnvironmentAnswer answer = names.findType(new char[][] { "x".toCharArray(), "y".toCharArray(), "IndexParallel".toCharArray() }, ModuleBinding.ANY);
	assertNotNull("The completed package must resolve its type", answer);
	assertEquals("The resolved type must use the file contents", source, new String(answer.getCompilationUnit().getContents()));
	assertNull("The retired provider package must no longer resolve", names.findType(
			new char[][] { "previous".toCharArray(), "IndexParallel".toCharArray() }, ModuleBinding.ANY));
}

/**
 * The notification contract requires the model transition to finish before its
 * epoch advances. This observes that contract, not a concurrent package lookup.
 */
public void testRetirementNotifiesAfterModelCleanup() throws Exception {
	assertWorkingCopyTransitionNotifiesAfterModelCleanup(false);
}

public void testBecomeWorkingCopyNotifiesAfterModelCleanup() throws Exception {
	assertWorkingCopyTransitionNotifiesAfterModelCleanup(true);
}

private void assertWorkingCopyTransitionNotifiesAfterModelCleanup(boolean becomingWorkingCopy) throws Exception {
	String path = "P/x/y/IndexRetirementOrder.java";
	createFile(path, "package x.y; public class IndexRetirementOrder {}");
	waitUntilIndexesReady();
	ICompilationUnit original = getCompilationUnit(path);
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	IndexManager indexManager = JavaModelManager.getIndexManager();
	AtomicBoolean captureClosing = new AtomicBoolean();
	AtomicReference<Integer> countDuringClosing = new AtomicReference<>();
	AtomicBoolean infoPresentDuringClosing = new AtomicBoolean();
	WorkingCopyOwner owner = becomingWorkingCopy ? DefaultWorkingCopyOwner.PRIMARY : new WorkingCopyOwner() {};
	CompilationUnit unit = new CompilationUnit((PackageFragment) original.getParent(), original.getElementName(), owner) {
		@Override
		protected void closing(Object info) {
			if (captureClosing.get()) {
				countDuringClosing.set(indexManager.getSourceEnvironmentChangeCount());
				infoPresentDuringClosing.set(manager.getInfo(this) == info);
			}
			super.closing(info);
		}
	};
	try {
		if (becomingWorkingCopy) {
			unit.open(null);
		} else {
			unit.becomeWorkingCopy(null);
		}
		assertNotNull("The generation must have a buffer to detach", BufferManager.getDefaultBufferManager().getBuffer(unit));
		Object oldInfo = manager.getInfo(unit);
		assertNotNull("The generation must have model info to retire", oldInfo);
		waitUntilIndexesReady();
		int countBefore = indexManager.getSourceEnvironmentChangeCount();
		captureClosing.set(true);

		if (becomingWorkingCopy) {
			unit.becomeWorkingCopy(null);
		} else {
			unit.discardWorkingCopy();
		}

		assertNotNull("The transition must invoke the old model's closing callback", countDuringClosing.get());
		assertTrue("The closing callback must observe model cleanup still in progress", infoPresentDuringClosing.get());
		assertEquals("The source environment must not advance before the old model info is removed",
				countBefore, countDuringClosing.get().intValue());
		assertNotSame("The transition must finish removing the old model info", oldInfo, manager.getInfo(unit));
		if (becomingWorkingCopy) {
			assertTrue("The transition must enter working-copy mode", unit.isWorkingCopy());
		} else {
			assertNull("Final discard must leave no model info", manager.getInfo(unit));
		}
		assertTrue("Completed model retirement must advance the source environment",
				indexManager.getSourceEnvironmentChangeCount() > countBefore);
	} finally {
		captureClosing.set(false);
		try {
			unit.discardWorkingCopy();
			unit.close();
		} finally {
			deleteFile(path);
		}
	}
}

private SourceIndexerEnvironment retainedIndexingEnvironment(ICompilationUnit unit) throws Exception {
	IndexManager indexManager = JavaModelManager.getIndexManager();
	Method acquire = IndexManager.class.getDeclaredMethod("acquireSourceIndexerEnvironment", JavaProject.class);
	Method release = IndexManager.class.getDeclaredMethod("releaseSourceIndexerEnvironment", SourceIndexerEnvironment.class);
	acquire.setAccessible(true);
	release.setAccessible(true);
	SourceIndexerEnvironment environment = (SourceIndexerEnvironment) invoke(acquire, indexManager, unit.getJavaProject());
	invoke(release, indexManager, environment);
	assertTrue("The indexing environment must be available for reuse", indexManager.isSourceIndexerEnvironmentKept());
	return environment;
}

/**
 * A clean provider buffer can change across primary working-copy generations
 * without a file edit or buffer change event to invalidate the indexing cache.
 */
public void testPrimaryWorkingCopyRoundTripInvalidatesIndexingEnvironment() throws Exception {
	String path = "P/x/y/IndexGeneration.java";
	String beforeSource = "package x.y; public class IndexGeneration { void before() {} }";
	String afterSource = "package x.y; public class IndexGeneration { void after() {} }";
	createFile(path, beforeSource);
	waitUntilIndexesReady();

	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	IndexManager indexManager = JavaModelManager.getIndexManager();
	WorkingCopyOwner previousProvider = DefaultWorkingCopyOwner.PRIMARY.primaryBufferProvider;
	boolean previousKeep = SourceIndexerEnvironment.KEEP;
	AtomicReference<String> providerContents = new AtomicReference<>(beforeSource);
	ICompilationUnit unit = getCompilationUnit(path);
	WorkingCopyOwner provider = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			IBuffer buffer = BufferManager.createBuffer(workingCopy);
			// A real Core Buffer's first initialization is clean and silent.
			buffer.setContents(providerContents.get());
			return buffer;
		}
	};
	Method acquire = IndexManager.class.getDeclaredMethod("acquireSourceIndexerEnvironment", JavaProject.class);
	Method release = IndexManager.class.getDeclaredMethod("releaseSourceIndexerEnvironment", SourceIndexerEnvironment.class);
	acquire.setAccessible(true);
	release.setAccessible(true);
	SourceIndexerEnvironment acquiredAfter = null;
	try {
		WorkingCopyOwner.setPrimaryBufferProvider(provider);
		SourceIndexerEnvironment.KEEP = true;
		unit.becomeWorkingCopy(null);
		IBuffer oldBuffer = unit.getBuffer();
		assertEquals(beforeSource, oldBuffer.getContents());
		assertFalse("The provider's first buffer must be clean", oldBuffer.hasUnsavedChanges());
		assertTrue("The first working-copy model must be consistent", unit.isConsistent());
		waitUntilIndexesReady();

		// Acquisition also warms the resolved classpath before the counter is captured.
		SourceIndexerEnvironment acquiredBefore = (SourceIndexerEnvironment) invoke(acquire, indexManager, unit.getJavaProject());
		invoke(release, indexManager, acquiredBefore);
		assertTrue("The first environment must be available for reuse", indexManager.isSourceIndexerEnvironmentKept());
		ICompilationUnit[] copiesBefore = manager.getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, true);
		int countBefore = indexManager.getSourceEnvironmentChangeCount();
		for (int i = 0; i < 3; i++) {
			assertSame("A cached read must retain the current provider buffer", oldBuffer, unit.getBuffer());
		}
		assertEquals("Cached reads must not invalidate the indexing environment", countBefore, indexManager.getSourceEnvironmentChangeCount());
		SourceIndexerEnvironment reused = (SourceIndexerEnvironment) invoke(acquire, indexManager, unit.getJavaProject());
		invoke(release, indexManager, reused);
		assertSame("An unchanged working copy must reuse its indexing environment", acquiredBefore, reused);
		assertEquals("Acquiring an unchanged indexing environment must not invalidate it", countBefore, indexManager.getSourceEnvironmentChangeCount());

		// No file writes, reconciliation, AST construction, or indexing requests
		// occur between these captures. Only the working-copy generation changes.
		unit.discardWorkingCopy();
		int countAfterDiscard = indexManager.getSourceEnvironmentChangeCount();
		assertTrue("Final discard must invalidate the source indexing environment: before=" + countBefore + ", after=" + countAfterDiscard,
				countAfterDiscard > countBefore);
		providerContents.set(afterSource);
		unit.becomeWorkingCopy(null);
		IBuffer newBuffer = unit.getBuffer();
		assertNotSame("The second generation must have a new physical buffer", oldBuffer, newBuffer);
		assertTrue("The first buffer must have been retired", oldBuffer.isClosed());
		assertEquals("The new generation must expose its provider's contents", afterSource, newBuffer.getContents());
		assertFalse("The second provider buffer must also be clean", newBuffer.hasUnsavedChanges());
		assertTrue("The working-copy handle list must be equal after the round trip",
				Arrays.equals(copiesBefore, manager.getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, true)));
		int countAfterReopen = indexManager.getSourceEnvironmentChangeCount();
		acquiredAfter = (SourceIndexerEnvironment) invoke(acquire, indexManager, unit.getJavaProject());

		String evidence = "count before=" + countBefore + ", after discard=" + countAfterDiscard
				+ ", after reopen=" + countAfterReopen + ", environment reused=" + (acquiredBefore == acquiredAfter);
		assertTrue("A changed provider source behind equal working-copy handles must advance the source environment: " + evidence,
				countAfterReopen > countBefore);
		assertNotSame("A new generation with different contents must invalidate the retained indexing environment: " + evidence,
				acquiredBefore, acquiredAfter);
	} finally {
		try {
			if (acquiredAfter != null) {
				invoke(release, indexManager, acquiredAfter);
			}
			unit.discardWorkingCopy();
		} finally {
			SourceIndexerEnvironment.KEEP = previousKeep;
			WorkingCopyOwner.setPrimaryBufferProvider(previousProvider);
			deleteFile(path);
		}
	}
}


/**
 * An edit delivered after detachment but before listener removal must not leave
 * the discarded handle retained as inconsistent after provider cleanup ends.
 */
public void testFinalDiscardIgnoresEditsWhileRemovingDetachedListener() throws Exception {
	String path = "P/x/y/DetachedEdit.java";
	String source = "package x.y; public class DetachedEdit {}";
	String editedSource = "package x.y; public class DetachedEdit { int beforeDiscard; }";
	String lateSource = "package x.y; public class DetachedEdit { int duringDiscard; }";
	createFile(path, source);
	waitUntilIndexesReady();
	CountDownLatch removingListener = new CountDownLatch(1);
	CountDownLatch finishRemoval = new CountDownLatch(1);
	class DelayedRemovalBuffer extends Buffer {
		final AtomicBoolean delayRemoval = new AtomicBoolean();
		DelayedRemovalBuffer(ICompilationUnit owner) {
			super((IFile) owner.getResource(), owner, false);
		}
		@Override
		// Allow edit notifications while listener removal is paused.
		@SuppressWarnings("sync-override")
		public void removeBufferChangedListener(IBufferChangedListener listener) {
			if (this.delayRemoval.compareAndSet(true, false)) {
				removingListener.countDown();
				awaitLatch(finishRemoval, "The detached listener removal was not released");
			}
			super.removeBufferChangedListener(listener);
		}
	}
	WorkingCopyOwner owner = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			return new DelayedRemovalBuffer(workingCopy);
		}
	};
	ICompilationUnit unit = getCompilationUnit(path).getWorkingCopy(owner, null);
	DelayedRemovalBuffer buffer = (DelayedRemovalBuffer) unit.getBuffer();
	Method outOfSyncMethod = JavaModelManager.class.getDeclaredMethod("getElementsOutOfSynchWithBuffers");
	outOfSyncMethod.setAccessible(true);
	Set<?> outOfSync = (Set<?>) invoke(outOfSyncMethod, JavaModelManager.getJavaModelManager());
	FutureTask<Void> discarding = new FutureTask<>(() -> {
		unit.discardWorkingCopy();
		return null;
	});
	Thread discarder = new Thread(discarding, "Removing a detached working-copy buffer listener");
	discarder.setDaemon(true);
	try {
		buffer.setContents(editedSource);
		assertFalse("The initial edit must make the working copy inconsistent", unit.isConsistent());
		assertTrue("The initial edit must register the handle as out of sync", outOfSync.contains(unit));
		buffer.delayRemoval.set(true);
		discarder.start();
		awaitLatch(removingListener, "Discard did not reach provider listener removal");
		assertNull("The old buffer must already be detached", BufferManager.getDefaultBufferManager().getBuffer(unit));
		assertFalse("The provider must still be open while its listener is attached", buffer.isClosed());
		assertTrue("Detachment must clear the previous inconsistency", unit.isConsistent());

		buffer.setContents(lateSource);
		assertEquals("The late edit must reach the detached provider", lateSource, buffer.getContents());
		finishRemoval.countDown();
		discarding.get(10, TimeUnit.SECONDS);
		assertTrue("Final discard must close the detached provider", buffer.isClosed());
		assertFalse("Final discard must close the working-copy model", unit.isOpen());
		assertNull("Final discard must leave no cached buffer", BufferManager.getDefaultBufferManager().getBuffer(unit));
		assertTrue("Final discard must not retain inconsistency from an edit during listener removal", unit.isConsistent());
		assertFalse("Final discard must not retain the discarded handle in the out-of-sync set", outOfSync.contains(unit));
	} finally {
		finishRemoval.countDown();
		discarding.cancel(true);
		try {
			discarder.join(10_000);
			assertFalse("The discarding thread did not finish", discarder.isAlive());
		} finally {
			try {
				unit.discardWorkingCopy();
				buffer.close();
			} finally {
				// Do not let a failing cleanup regression retain this test's handle.
				outOfSync.remove(unit);
				deleteFile(path);
			}
		}
	}
}

/**
 * An edit between listener registration and publication must remain visible to
 * an already-open model, even when edit events for uncached buffers are ignored.
 */
public void testEditBeforePublicationLeavesOpenModelInconsistent() throws Exception {
	String path = "P/x/y/EditedBeforePublication.java";
	String source = "package x.y; public class EditedBeforePublication {}";
	String changedSource = "package x.y; public class EditedBeforePublication { int changed; }";
	createFile(path, source);
	waitUntilIndexesReady();
	CountDownLatch listenerRegistered = new CountDownLatch(1);
	CountDownLatch publishBuffer = new CountDownLatch(1);
	class DelayedPublicationBuffer extends Buffer {
		DelayedPublicationBuffer(ICompilationUnit owner) {
			super((IFile) owner.getResource(), owner, false);
		}
		@Override
		// Allow edits after registration while publication is paused.
		@SuppressWarnings("sync-override")
		public void addBufferChangedListener(IBufferChangedListener listener) {
			super.addBufferChangedListener(listener);
			listenerRegistered.countDown();
			awaitLatch(publishBuffer, "The edited candidate was not released for publication");
		}
	}
	AtomicReference<IBuffer> provided = new AtomicReference<>();
	WorkingCopyOwner owner = new WorkingCopyOwner() {
		@Override
		public IBuffer createBuffer(ICompilationUnit workingCopy) {
			IBuffer candidate = provided.get();
			return candidate == null ? BufferManager.createBuffer(workingCopy) : candidate;
		}
	};
	ICompilationUnit unit = getCompilationUnit(path).getWorkingCopy(owner, null);
	DelayedPublicationBuffer candidate = new DelayedPublicationBuffer(unit);
	FutureTask<IBuffer> opening = new FutureTask<>(unit::getBuffer);
	Thread opener = new Thread(opening, "Publishing a candidate edited before publication");
	opener.setDaemon(true);
	IndexManager indexManager = JavaModelManager.getIndexManager();
	try {
		assertFalse("The original model must not contain the edited field", unit.getType("EditedBeforePublication").getField("changed").exists());
		unit.getBuffer().close();
		assertTrue("The previous buffer's close must leave the model open", unit.isOpen());
		assertTrue("Closing the previous buffer must leave the model consistent", unit.isConsistent());
		provided.set(candidate);
		int countBeforePublication = indexManager.getSourceEnvironmentChangeCount();
		opener.start();
		awaitLatch(listenerRegistered, "The candidate did not register its listener");
		assertNull("The candidate must not yet be published", BufferManager.getDefaultBufferManager().getBuffer(unit));
		assertEquals("The candidate must start with the original source", source, candidate.getContents());
		candidate.setContents(changedSource);
		assertTrue("The prepublication edit must dirty the actual provider buffer", candidate.hasUnsavedChanges());
		assertEquals("An edit from an unpublished buffer must not invalidate the source environment",
				countBeforePublication, indexManager.getSourceEnvironmentChangeCount());
		publishBuffer.countDown();
		assertSame("The edited candidate must be published", candidate, opening.get(10, TimeUnit.SECONDS));
		assertSame("The edited candidate must be the cached buffer", candidate, BufferManager.getDefaultBufferManager().getBuffer(unit));
		assertEquals("Publication must preserve the edit", changedSource, candidate.getContents());
		assertFalse("Publication must leave the old model inconsistent with the edited candidate", unit.isConsistent());
		assertTrue("Publication must invalidate the source environment even when the earlier edit event was ignored",
				indexManager.getSourceEnvironmentChangeCount() > countBeforePublication);
		unit.makeConsistent(null);
		assertTrue("Reconciliation must read the prepublication edit", unit.getType("EditedBeforePublication").getField("changed").exists());
		assertTrue("The rebuilt model must be consistent", unit.isConsistent());
	} finally {
		publishBuffer.countDown();
		opening.cancel(true);
		try {
			opener.join(10_000);
			assertFalse("The edited candidate opener did not finish", opener.isAlive());
		} finally {
			try {
				unit.discardWorkingCopy();
			} finally {
				deleteFile(path);
			}
		}
	}
}

/** A deferred close must clear inconsistency after its cache entry has been removed. */
public void testCloseAfterCacheRemovalClearsInconsistency() throws Exception {
	class LocalBufferManager extends BufferManager {
		void cache(IBuffer buffer) {
			addBuffer(buffer);
		}
		void remove(IBuffer buffer) {
			removeBuffer(buffer);
		}
	}
	LocalBufferManager bufferManager = new LocalBufferManager();
	CompilationUnit unit = new CompilationUnit((PackageFragment) getPackageFragment("P", "", "x.y"), "RemovedBuffer.java", DefaultWorkingCopyOwner.PRIMARY) {
		@Override
		protected BufferManager getBufferManager() {
			return bufferManager;
		}
	};
	IBuffer buffer = BufferManager.createBuffer(unit);
	buffer.setContents("package x.y; class RemovedBuffer {}");
	buffer.addBufferChangedListener(unit);
	bufferManager.cache(buffer);
	try {
		buffer.setContents("package x.y; class RemovedBuffer { int changed; }");
		assertFalse("The real buffer edit must make the handle inconsistent", unit.isConsistent());
		bufferManager.remove(buffer);
		assertNull("The cache entry must be absent before deferred close", bufferManager.getBuffer(unit));
		assertFalse("Removing the cache entry must not close the buffer", buffer.isClosed());
		assertFalse("Cache removal alone must leave the close event's cleanup pending", unit.isConsistent());
		buffer.close();
		assertTrue("The deferred close must clear the old handle's inconsistency", unit.isConsistent());
		assertNull("The close event must leave the cache empty", bufferManager.getBuffer(unit));
	} finally {
		buffer.close();
		buffer.removeBufferChangedListener(unit);
	}
}

private static Object invoke(Method method, Object receiver, Object... arguments) throws Exception {
	try {
		return method.invoke(receiver, arguments);
	} catch (InvocationTargetException e) {
		if (e.getCause() instanceof Exception exception) {
			throw exception;
		}
		if (e.getCause() instanceof Error error) {
			throw error;
		}
		throw e;
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
}
