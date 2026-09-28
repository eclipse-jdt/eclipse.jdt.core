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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
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
import org.eclipse.jdt.internal.core.ModularClassFile;
import org.eclipse.jdt.internal.core.NullBuffer;
import org.eclipse.jdt.internal.core.PackageFragment;

/** Tests publication using real class-file/source-mapper/buffer/cache implementations. */
public class ClassFileBufferPublicationTests extends ModifyingResourceTests {

	private static final int TIMEOUT_SECONDS = 30;
	private static final String CLASS_SOURCE = "package p;\npublic class X {}\n";
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
		final PausingBufferManager manager = new PausingBufferManager();
		final ExecutorService workers = Executors.newFixedThreadPool(2, runnable -> {
			Thread thread = new Thread(runnable, "classfile-buffer-publication");
			thread.setDaemon(true);
			return thread;
		});
		final IJavaProject project;
		final IClassFile classFile;
		final String source;

		PublicationFixture(FileKind kind, SourceAttachment attachment) throws Exception {
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
				IPackageFragmentRoot root = this.project.getPackageFragmentRoot(this.project.getProject().getFile("lib.jar"));
				this.classFile = switch (kind) {
					case ORDINARY -> new ClassFile((PackageFragment) root.getPackageFragment("p"), "X") {
						@Override
						protected BufferManager getBufferManager() {
							return PublicationFixture.this.manager;
						}
					};
					case MODULAR -> new ModularClassFile((PackageFragment) root.getPackageFragment("")) {
						@Override
						protected BufferManager getBufferManager() {
							return PublicationFixture.this.manager;
						}
					};
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

	private static final class PausingBufferManager extends BufferManager {
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

		void resume() {
			this.released.countDown();
		}
	}
}
