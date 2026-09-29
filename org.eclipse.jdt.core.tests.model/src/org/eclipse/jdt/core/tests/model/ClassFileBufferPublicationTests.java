/*******************************************************************************
 * Copyright (c) 2026 contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import junit.framework.Test;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.BufferManager;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.ModularClassFile;
import org.eclipse.jdt.internal.core.NullBuffer;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.SourceMapper;

/** Tests publication using real class-file/source-mapper/buffer/cache implementations. */
public class ClassFileBufferPublicationTests extends ModifyingResourceTests {

	private static final int TIMEOUT_SECONDS = 30;
	private static final String CLASS_SOURCE = "package p;\npublic class X { public static class Inner {} }\n";
	private static final String MODULE_SOURCE = "module buffer.publication { exports p; }\n";

	private enum FileKind { ORDINARY, MODULAR }
	private enum SourceAttachment { PRESENT, MISSING_ENTRY }

	public ClassFileBufferPublicationTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(ClassFileBufferPublicationTests.class);
	}

	public void testClassSourceIsInitializedBeforePublication() throws Exception {
		assertSourcePublished(FileKind.ORDINARY);
	}

	public void testModuleSourceIsInitializedBeforePublication() throws Exception {
		assertSourcePublished(FileKind.MODULAR);
	}

	public void testClassBufferListenerIsInstalledBeforePublication() throws Exception {
		assertCloseRemovesBuffer(FileKind.ORDINARY, SourceAttachment.PRESENT);
	}

	public void testModuleBufferListenerIsInstalledBeforePublication() throws Exception {
		assertCloseRemovesBuffer(FileKind.MODULAR, SourceAttachment.PRESENT);
	}

	public void testClassNullBufferListenerIsInstalledBeforePublication() throws Exception {
		assertCloseRemovesBuffer(FileKind.ORDINARY, SourceAttachment.MISSING_ENTRY);
	}

	public void testModuleNullBufferListenerIsInstalledBeforePublication() throws Exception {
		assertCloseRemovesBuffer(FileKind.MODULAR, SourceAttachment.MISSING_ENTRY);
	}

	public void testConcurrentClassOpensReuseBuffer() throws Exception {
		assertConcurrentOpens(FileKind.ORDINARY, SourceAttachment.PRESENT, false);
	}

	public void testConcurrentModuleOpensReuseBuffer() throws Exception {
		assertConcurrentOpens(FileKind.MODULAR, SourceAttachment.PRESENT, false);
	}

	public void testConcurrentClassOpensReuseNullBuffer() throws Exception {
		assertConcurrentOpens(FileKind.ORDINARY, SourceAttachment.MISSING_ENTRY, false);
	}

	public void testConcurrentModuleOpensReuseNullBuffer() throws Exception {
		assertConcurrentOpens(FileKind.MODULAR, SourceAttachment.MISSING_ENTRY, false);
	}

	public void testConcurrentOuterAndInnerOpensReuseBuffer() throws Exception {
		assertConcurrentOpens(FileKind.ORDINARY, SourceAttachment.PRESENT, true);
	}

	public void testConcurrentOuterAndInnerOpensReuseNullBuffer() throws Exception {
		assertConcurrentOpens(FileKind.ORDINARY, SourceAttachment.MISSING_ENTRY, true);
	}

	private void assertConcurrentOpens(FileKind kind, SourceAttachment attachment, boolean useInner) throws Exception {
		ConcurrentBufferManager manager = new ConcurrentBufferManager();
		// These tests pause cache misses, not publication.
		manager.resume();
		try (PublicationFixture fixture = new PublicationFixture(kind, attachment, manager)) {
			IClassFile other = useInner ? fixture.createClassFile("X$Inner")
					: kind == FileKind.ORDINARY ? fixture.createClassFile("X") : fixture.createModularClassFile();
			assertNotSame("Exercise distinct handles sharing a cache key", fixture.classFile, other);
			assertNotNull(((JavaElement) fixture.classFile).getSourceMapper());
			other.open(null);
			Future<IBuffer> first = fixture.workers.submit(() -> manager.open(fixture.classFile));
			Future<IBuffer> second = fixture.workers.submit(() -> manager.open(other));
			IBuffer firstBuffer = first.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			IBuffer secondBuffer = second.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			assertEquals("Concurrent cache misses must publish one buffer", 1, manager.additions.get());
			IBuffer cached = manager.getBuffer(fixture.classFile);
			assertNotNull("Discarding a competing buffer must retain the cached buffer", cached);
			assertFalse("Discarding a competing buffer must not close the cached buffer", cached.isClosed());
			assertEquals("Inner classes must use the outer class as buffer owner", fixture.classFile, cached.getOwner());
			if (attachment == SourceAttachment.PRESENT) {
				assertSame("Both callers must receive the cached buffer", cached, firstBuffer);
				assertSame("Both callers must receive the cached buffer", cached, secondBuffer);
			} else {
				assertTrue("Missing source must remain a cached NullBuffer", cached instanceof NullBuffer);
				assertNull(firstBuffer);
				assertNull(secondBuffer);
			}
			assertEquals(fixture.source, fixture.classFile.getSource());
			assertEquals(fixture.source, other.getSource());
			assertSame("Subsequent access must retain the shared buffer", cached, manager.getBuffer(fixture.classFile));
			cached.close();
			assertNull("The winning buffer must have its close listener installed", manager.getBuffer(fixture.classFile));
			assertEquals("Closing must allow source lookup to reopen the buffer", fixture.source, other.getSource());
			IBuffer reopened = manager.getBuffer(fixture.classFile);
			assertNotNull(reopened);
			assertNotSame(cached, reopened);
			assertFalse(reopened.isClosed());
		}
	}

	private void assertSourcePublished(FileKind kind) throws Exception {
		try (PublicationFixture fixture = new PublicationFixture(kind, SourceAttachment.PRESENT)) {
			Future<String> writer = fixture.startWriter();
			IBuffer published = fixture.awaitPublication();
			assertFalse("A newly published source buffer must be open", published.isClosed());
			assertFalse("The fixture must have an attached source entry", published instanceof NullBuffer);
			Future<String> reader = fixture.workers.submit(fixture.classFile::getSource);
			assertEquals("Published source must already be initialized", fixture.source,
					reader.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
			fixture.manager.resume();
			assertEquals(fixture.source, writer.get(TIMEOUT_SECONDS, TimeUnit.SECONDS));
			assertEquals(fixture.source, fixture.classFile.getSource());
		}
	}

	private void assertCloseRemovesBuffer(FileKind kind, SourceAttachment attachment) throws Exception {
		try (PublicationFixture fixture = new PublicationFixture(kind, attachment)) {
			Future<String> writer = fixture.startWriter();
			IBuffer published = fixture.awaitPublication();
			assertEquals("Check the requested source/no-source path",
					attachment == SourceAttachment.MISSING_ENTRY, published instanceof NullBuffer);
			published.close();
			assertTrue(published.isClosed());
			assertNull("Closing a published buffer must remove it from the cache",
					fixture.manager.getBuffer(fixture.classFile));
			fixture.manager.resume();
			// The overlapping read may return null after close; it must finish without an error.
			writer.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
			assertEquals("A later access must recreate the appropriate buffer",
					fixture.source, fixture.classFile.getSource());
		}
	}

	/**
	 * Each handle uses a private, real BufferManager. There is no replacement of the
	 * global manager, no mocked source lookup, and no production test hook.
	 */
	private final class PublicationFixture implements AutoCloseable {
		final PausingBufferManager manager;
		final ExecutorService workers = Executors.newFixedThreadPool(2, runnable -> {
			Thread thread = new Thread(runnable, "classfile-buffer-publication");
			thread.setDaemon(true);
			return thread;
		});
		final IJavaProject project;
		final IPackageFragmentRoot root;
		final IClassFile classFile;
		final String source;

		PublicationFixture(FileKind kind, SourceAttachment attachment) throws Exception {
			this(kind, attachment, new PausingBufferManager());
		}

		PublicationFixture(FileKind kind, SourceAttachment attachment, PausingBufferManager manager) throws Exception {
			this.manager = manager;
			this.project = createJava9Project("BufferPublication");
			boolean initialized = false;
			try {
				addModularLibrary(this.project, "lib.jar", "libsrc.zip", new String[] {
						"module-info.java", MODULE_SOURCE, "p/X.java", CLASS_SOURCE
				}, JavaCore.VERSION_9);
				if (attachment == SourceAttachment.MISSING_ENTRY) {
					// Retain an attached source archive (and SourceMapper), but not the requested entry.
					ByteArrayOutputStream bytes = new ByteArrayOutputStream();
					try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
						zip.putNextEntry(new ZipEntry("other/Unrelated.java"));
						zip.write("package other; class Unrelated {}".getBytes(StandardCharsets.UTF_8));
						zip.closeEntry();
					}
					this.project.getProject().getFile("libsrc.zip").setContents(
							new ByteArrayInputStream(bytes.toByteArray()), true, false, null);
				}
				this.root = this.project.getPackageFragmentRoot(this.project.getProject().getFile("lib.jar"));
				this.classFile = switch (kind) {
					case ORDINARY -> createClassFile("X");
					case MODULAR -> createModularClassFile();
				};
				this.source = attachment == SourceAttachment.MISSING_ENTRY ? null
						: kind == FileKind.ORDINARY ? CLASS_SOURCE : MODULE_SOURCE;
				// Warm the Java-model metadata, without opening a source buffer.
				this.classFile.open(null);
				assertNull(this.manager.getBuffer(this.classFile));
				initialized = true;
			} finally {
				if (!initialized) {
					this.workers.shutdownNow();
					deleteProject(this.project);
				}
			}
		}

		ClassFile createClassFile(String name) {
			return new ClassFile((PackageFragment) this.root.getPackageFragment("p"), name) {
				@Override
				protected BufferManager getBufferManager() {
					return PublicationFixture.this.manager;
				}

				@Override
				public SourceMapper getSourceMapper() {
					SourceMapper mapper = super.getSourceMapper();
					PublicationFixture.this.manager.beforeSourceLookup();
					return mapper;
				}
			};
		}

		ModularClassFile createModularClassFile() {
			return new ModularClassFile((PackageFragment) this.root.getPackageFragment("")) {
				@Override
				protected BufferManager getBufferManager() {
					return PublicationFixture.this.manager;
				}

				@Override
				public SourceMapper getSourceMapper() {
					SourceMapper mapper = super.getSourceMapper();
					PublicationFixture.this.manager.beforeSourceLookup();
					return mapper;
				}
			};
		}

		Future<String> startWriter() {
			return this.workers.submit(this.classFile::getSource);
		}

		IBuffer awaitPublication() throws InterruptedException {
			assertTrue("Writer did not publish a buffer", this.manager.published.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
			IBuffer buffer = this.manager.getBuffer(this.classFile);
			assertNotNull("Published buffer must be retrievable by its class-file handle", buffer);
			return buffer;
		}

		@Override
		public void close() throws Exception {
			this.manager.resume();
			this.workers.shutdownNow();
			try {
				assertTrue("Buffer reader threads did not terminate",
						this.workers.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS));
			} finally {
				try {
					this.classFile.close();
				} finally {
					deleteProject(this.project);
				}
			}
		}
	}

	private static class PausingBufferManager extends BufferManager {
		final CountDownLatch published = new CountDownLatch(1);
		private final CountDownLatch released = new CountDownLatch(1);
		private final AtomicBoolean pauseOnce = new AtomicBoolean(true);

		@Override
		protected void addBuffer(IBuffer buffer) {
			super.addBuffer(buffer);
			if (this.pauseOnce.compareAndSet(true, false)) {
				// Pause outside the real cache monitor, immediately after publication.
				this.published.countDown();
				try {
					if (!this.released.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
						throw new AssertionError("Timed out waiting to resume buffer publication");
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new AssertionError("Interrupted while publishing buffer", e);
				}
			}
		}

		void beforeSourceLookup() {
			// Only concurrent-creation tests pause here.
		}

		void resume() {
			this.released.countDown();
		}
	}

	/** Pause after the real cache misses, before either caller can create a buffer. */
	private static final class ConcurrentBufferManager extends PausingBufferManager {
		final AtomicInteger additions = new AtomicInteger();
		private final CyclicBarrier lookups = new CyclicBarrier(2);
		private final ThreadLocal<Boolean> awaitingLookup = new ThreadLocal<>();

		IBuffer open(IClassFile classFile) throws Exception {
			this.awaitingLookup.set(Boolean.TRUE);
			try {
				IBuffer buffer = classFile.getBuffer();
				assertEquals("Both callers must enter source lookup", Boolean.FALSE, this.awaitingLookup.get());
				return buffer;
			} finally {
				this.awaitingLookup.remove();
			}
		}

		@Override
		void beforeSourceLookup() {
			if (Boolean.TRUE.equals(this.awaitingLookup.get())) {
				this.awaitingLookup.set(Boolean.FALSE);
				try {
					this.lookups.await(TIMEOUT_SECONDS, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new AssertionError("Interrupted while coordinating source lookup", e);
				} catch (BrokenBarrierException | TimeoutException e) {
					throw new AssertionError("Both readers must reach source lookup", e);
				}
			}
		}

		@Override
		protected void addBuffer(IBuffer buffer) {
			this.additions.incrementAndGet();
			super.addBuffer(buffer);
		}
	}
}
