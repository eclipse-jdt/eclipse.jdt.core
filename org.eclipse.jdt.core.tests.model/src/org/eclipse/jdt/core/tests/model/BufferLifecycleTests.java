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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.IdentityHashMap;
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
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.TypeDeclaration;
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
		public void addBufferChangedListener(IBufferChangedListener listener) {
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
			(proxy, method, arguments) -> {
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
		public void addBufferChangedListener(IBufferChangedListener listener) {
			this.listeners.put(listener, Boolean.TRUE);
			this.registeredListener = listener;
		}
		@Override
		public void removeBufferChangedListener(IBufferChangedListener listener) {
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

private static void awaitLatch(CountDownLatch latch, String message) {
	try {
		assertTrue(message, latch.await(10, TimeUnit.SECONDS));
	} catch (InterruptedException e) {
		Thread.currentThread().interrupt();
		throw new AssertionError(message, e);
	}
}
}
