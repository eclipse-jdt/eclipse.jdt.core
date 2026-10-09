/*******************************************************************************
 * Copyright (c) 2026 Hélios GILLES and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Hélios GILLES - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import junit.framework.Test;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.search.SearchDocument;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.index.EntryResult;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.index.IndexLocation;
import org.eclipse.jdt.internal.core.search.JavaSearchParticipant;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.indexing.ReadWriteMonitor;
import org.eclipse.jdt.internal.core.search.indexing.SourceIndexerEnvironment;
import org.eclipse.jdt.internal.core.search.processing.IJob;

/**
 * Tests the index entries that the source indexer adds for the documents it has to resolve: those with lambda
 * expressions and method references.
 */
public class SourceIndexerResolveTests extends ModifyingResourceTests {

	private static final char[][] CATEGORIES = { IIndexConstants.TYPE_DECL, IIndexConstants.METHOD_DECL,
			IIndexConstants.METHOD_DECL_PLUS, IIndexConstants.CONSTRUCTOR_DECL, IIndexConstants.FIELD_DECL,
			IIndexConstants.REF, IIndexConstants.METHOD_REF, IIndexConstants.CONSTRUCTOR_REF,
			IIndexConstants.SUPER_REF, IIndexConstants.ANNOTATION_REF, IIndexConstants.MODULE_DECL,
			IIndexConstants.MODULE_REF };

	private IndexManager indexManager;
	private final int maxUnits = SourceIndexerEnvironment.MAX_UNITS;

	public static Test suite() {
		return buildModelTestSuite(SourceIndexerResolveTests.class, BYTECODE_DECLARATION_ORDER);
	}

	public SourceIndexerResolveTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		this.indexDisabledForTest = false;
		super.setUp();
		this.indexManager = JavaModelManager.getIndexManager();
	}

	@Override
	protected void tearDown() throws Exception {
		SourceIndexerEnvironment.MAX_UNITS = this.maxUnits;
		SourceIndexerEnvironment.KEEP = true;
		deleteProject("P");
		super.tearDown();
	}

	/**
	 * The documents of a project are resolved in one environment, which gives the same index as resolving each of
	 * them in its own environment.
	 */
	public void testSameIndexAsWithSeparateEnvironments() throws Exception {
		IJavaProject project = createIndexedProject("src");
		int types = 40;
		for (int i = 0; i < types; i++) {
			int next = (i + 1) % types;
			int other = (i + 7) % types;
			createSource("/P/src/p" + (i % 4) + "/C" + i + ".java",
					"package p" + (i % 4) + ";\n" +
					"import java.util.ArrayList;\n" +
					"import java.util.List;\n" +
					"import java.util.function.Function;\n" +
					"import java.util.function.Supplier;\n" +
					"import p" + (next % 4) + ".C" + next + ";\n" +
					"import p" + (other % 4) + ".C" + other + ";\n" +
					"public class C" + i + " implements Runnable {\n" +
					"	public interface Visitor" + i + " {\n" +
					"		boolean visit" + i + "(C" + next + " c, int depth);\n" +
					"	}\n" +
					"	public C" + next + " next;\n" +
					"	Function<C" + other + ", C" + next + "> field = c -> c.next" + (other % 3) + "(null).next;\n" +
					"	public C" + next + " next" + (i % 3) + "(List<C" + next + "> list) {\n" +
					"		return list == null || list.isEmpty() ? this.next : list.get(0);\n" +
					"	}\n" +
					"	public int value() {\n" +
					"		return " + i + ";\n" +
					"	}\n" +
					"	public boolean accept(C" + other + ".Visitor" + other + " visitor) {\n" +
					"		return visitor.visit" + other + "(null, value());\n" +
					"	}\n" +
					"	public void run() {\n" +
					"		Supplier<C" + next + "> supplier = C" + next + "::new;\n" +
					"		Function<C" + other + ", Integer> function = C" + other + "::value;\n" +
					"		List<String> list = new ArrayList<>();\n" +
					"		list.forEach(s -> System.out.println(s + function.apply(new C" + other + "()) + supplier.get()));\n" +
					"		new C" + next + "().accept((c, depth) -> depth > " + i + ");\n" +
					"		Runnable local = new Runnable() {\n" +
					"			public void run() {\n" +
					"				new C" + other + "().accept((c, depth) -> c == null);\n" +
					"			}\n" +
					"		};\n" +
					"	}\n" +
					"}\n");
		}
		createSource("/P/src/Default.java",
				"public class Default {\n" +
				"	java.util.function.ToIntFunction<p0.C0> f = p0.C0::value;\n" +
				"	p1.C1.Visitor1 visitor = (c, depth) -> true;\n" +
				"}\n");
		createSource("/P/src/p0/Unresolved.java",
				"package p0;\n" +
				"public class Unresolved {\n" +
				"	Missing missing = () -> {};\n" +
				"	Runnable runnable = Missing::missing;\n" +
				"	C0.Visitor0 visitor = c -> true;\n" +
				"}\n");

		Map<String, String> index = assertSameIndexAsWithSeparateEnvironments(project);

		// the declaration of Visitor1, and the lambda expressions of this type
		assertEquals("src/Default.java,src/p1/C1.java,src/p1/C33.java,src/p3/C27.java", index.get("methodDecl/visit1/2"));
		// C0::new, and new C0()
		assertEquals("src/p1/C33.java,src/p3/C39.java", index.get("constructorRef/C0/0"));
	}

	/**
	 * A secondary type is only known once its unit is: whether another unit resolves it must not depend on the
	 * units that were resolved before in the same environment.
	 */
	public void testSecondaryTypes() throws Exception {
		IJavaProject project = createIndexedProject("src");
		createSource("/P/src/p/Main.java",
				"package p;\n" +
				"public class Main {\n" +
				"	Runnable runnable = () -> {};\n" +
				"	Secondary secondary = i -> {};\n" +
				"}\n" +
				"interface Secondary {\n" +
				"	void call(int i);\n" +
				"}\n");
		createSource("/P/src/p/Other.java",
				"package p;\n" +
				"public class Other {\n" +
				"}\n" +
				"interface OtherSecondary {\n" +
				"	void other(String s);\n" +
				"}\n");
		for (int i = 0; i < 6; i++) {
			// refers to the secondary types only
			createSource("/P/src/p/A" + i + ".java",
					"package p;\n" +
					"public class A" + i + " {\n" +
					"	Secondary secondary = i -> {};\n" +
					"	OtherSecondary other = s -> {};\n" +
					"}\n");
			// refers to their units first
			createSource("/P/src/p/B" + i + ".java",
					"package p;\n" +
					"public class B" + i + " {\n" +
					"	Main main = new Main();\n" +
					"	Other other = new Other();\n" +
					"	Secondary secondary = i -> {};\n" +
					"	OtherSecondary otherSecondary = s -> {};\n" +
					"}\n");
			// refers to nothing of them
			createSource("/P/src/p/C" + i + ".java",
					"package p;\n" +
					"public class C" + i + " {\n" +
					"	Runnable runnable = () -> {};\n" +
					"}\n");
		}

		assertSameIndexAsWithSeparateEnvironments(project);
	}

	/**
	 * A type of a jar refers to a type that is missing from the classpath: where the resolution of a unit fails
	 * must not depend on the units that were resolved before in the same environment.
	 */
	public void testMissingType() throws Exception {
		IJavaProject project = createIndexedProject("src");
		String jar = project.getProject().getLocation().append("lib.jar").toOSString();
		createJar(new String[] {
				"lib/Missing.java",
				"package lib;\n" +
				"public class Missing {\n" +
				"}\n",
				"lib/Lib.java",
				"package lib;\n" +
				"public class Lib extends Missing {\n" +
				"	public Missing missing;\n" +
				"	public void use(Missing m) {}\n" +
				"	public int value() { return 0; }\n" +
				"}\n",
				"lib/Holder.java",
				"package lib;\n" +
				"public class Holder {\n" +
				"	public Lib lib;\n" +
				"	public java.util.List<? extends Missing> list;\n" +
				"}\n" },
				jar);
		removeFromJar(jar, "lib/Missing.class");
		project.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
		addLibraryEntry(project, project.getPath().append("lib.jar"), false);
		String[] bodies = {
				"	Runnable runnable = () -> {};\n" +
				"	java.util.function.Function<lib.Holder, Object> function = h -> h.lib;\n",
				"	java.util.function.Supplier<String> supplier = () -> \"\";\n" +
				"	lib.Holder holder;\n" +
				"	java.util.function.Consumer<lib.Holder> consumer = h -> h.list.size();\n",
				"	Runnable runnable = () -> {};\n" +
				"	void test(lib.Lib lib) {\n" +
				"		java.util.function.IntSupplier value = lib::value;\n" +
				"		Runnable use = () -> lib.use(null);\n" +
				"	}\n",
				"	Runnable runnable = () -> {};\n" +
				"	lib.Missing missing;\n" +
				"	java.util.function.Supplier<Object> supplier = () -> new lib.Holder().lib.missing;\n",
				"	java.util.function.Function<String, Integer> length = String::length;\n" };
		for (int i = 0; i < 20; i++) {
			createSource("/P/src/p/A" + i + ".java",
					"package p;\n" +
					"public class A" + i + " {\n" +
					bodies[i % bodies.length] +
					(i > 0 ? "	A" + (i - 1) + " previous;\n" : "") +
					"}\n");
		}

		assertSameIndexAsWithSeparateEnvironments(project);
	}

	private static void removeFromJar(String jar, String entryName) throws IOException {
		java.nio.file.Path path = java.nio.file.Path.of(jar);
		java.nio.file.Path copy = java.nio.file.Path.of(jar + ".tmp");
		try (ZipInputStream in = new ZipInputStream(Files.newInputStream(path));
				ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(copy))) {
			for (ZipEntry entry = in.getNextEntry(); entry != null; entry = in.getNextEntry()) {
				if (!entry.getName().equals(entryName)) {
					out.putNextEntry(new ZipEntry(entry.getName()));
					in.transferTo(out);
					out.closeEntry();
				}
			}
		}
		Files.move(copy, path, StandardCopyOption.REPLACE_EXISTING);
	}

	/**
	 * The same type is declared by two units.
	 */
	public void testDuplicateTypes() throws Exception {
		IJavaProject project = createIndexedProject("src", "src2");
		createSource("/P/src/p/I.java", "package p;\npublic interface I {\n	void foo();\n}\n");
		createSource("/P/src/p/Duplicate.java",
				"package p;\n" +
				"public class Duplicate {\n" +
				"	void test() {\n" +
				"		I i = () -> {};\n" +
				"	}\n" +
				"}\n" +
				"class Secondary {\n" +
				"	Runnable runnable = () -> {};\n" +
				"}\n");
		createSource("/P/src2/p/Duplicate.java",
				"package p;\n" +
				"public class Duplicate {\n" +
				"	void test() {\n" +
				"		java.util.function.Supplier<User> supplier = User::new;\n" +
				"	}\n" +
				"}\n");
		createSource("/P/src/p/Secondary.java",
				"package p;\n" +
				"public class Secondary {\n" +
				"	java.util.function.Function<Duplicate, String> function = d -> d.toString();\n" +
				"}\n");
		createSource("/P/src/p/User.java",
				"package p;\n" +
				"public class User {\n" +
				"	void test(Duplicate duplicate, Secondary secondary) {\n" +
				"		I i = duplicate::test;\n" +
				"	}\n" +
				"}\n");

		Map<String, String> index = assertSameIndexAsWithSeparateEnvironments(project);

		assertEquals("src/p/Duplicate.java,src/p/I.java", index.get("methodDecl/foo/0"));
		assertEquals("src/p/User.java", index.get("methodRef/test/0"));
		assertEquals("src2/p/Duplicate.java", index.get("constructorRef/User/0"));
	}

	/**
	 * The units of a module refer to each other and to the modules it requires.
	 */
	public void testModule() throws Exception {
		IJavaProject project = createIndexedProject("src");
		createSource("/P/src/module-info.java",
				"module m {\n" +
				"	requires java.logging;\n" +
				"	exports p;\n" +
				"}\n");
		createSource("/P/src/p/I.java", "package p;\npublic interface I {\n	void foo(java.util.logging.Logger logger);\n}\n");
		createSource("/P/src/q/J.java", "package q;\npublic interface J {\n	p.I bar();\n}\n");
		createSource("/P/src/p/A.java",
				"package p;\n" +
				"import java.util.logging.Logger;\n" +
				"public class A {\n" +
				"	I i = logger -> logger.info(\"A\");\n" +
				"	java.util.function.Supplier<Logger> supplier = Logger::getGlobal;\n" +
				"	java.sql.Wrapper notRequired = c -> false;\n" +
				"	void test(q.J j) {\n" +
				"		j.bar().foo(supplier.get());\n" +
				"	}\n" +
				"}\n");
		createSource("/P/src/q/B.java",
				"package q;\n" +
				"public class B {\n" +
				"	J j = () -> logger -> new p.A().test(null);\n" +
				"	java.util.function.BiConsumer<p.A, J> consumer = p.A::test;\n" +
				"}\n");

		Map<String, String> index = assertSameIndexAsWithSeparateEnvironments(project);

		assertEquals("src/p/A.java,src/p/I.java,src/q/B.java", index.get("methodDecl/foo/1"));
		assertEquals("src/q/B.java,src/q/J.java", index.get("methodDecl/bar/0"));
		assertEquals("src/q/B.java", index.get("methodRef/test/1"));
		// java.sql is not required
		assertEquals(null, index.get("methodDecl/isWrapperFor/1"));
	}

	/**
	 * A unit is resolved with the contents of the working copies, and indexed with its saved contents.
	 */
	public void testWorkingCopy() throws Exception {
		IJavaProject project = createIndexedProject("src");
		createSource("/P/src/p/I.java", "package p;\npublic interface I {\n	void foo();\n}\n");
		createSource("/P/src/p/A.java",
				"package p;\n" +
				"public class A {\n" +
				"	void test() {\n" +
				"		I i = x -> {};\n" +
				"	}\n" +
				"}\n");
		createSource("/P/src/p/B.java",
				"package p;\n" +
				"public class B {\n" +
				"	I i = x -> {};\n" +
				"}\n");
		Map<String, String> index = assertSameIndexAsWithSeparateEnvironments(project);

		assertEquals("src/p/I.java", index.get("methodDecl/foo/0"));
		assertEquals(null, index.get("methodDecl/bar/1"));

		ICompilationUnit workingCopy = getCompilationUnit("/P/src/p/I.java");
		workingCopy.becomeWorkingCopy(null);
		try {
			workingCopy.getBuffer().setContents("package p;\npublic interface I {\n	void bar(int x);\n}\n");
			index = assertSameIndexAsWithSeparateEnvironments(project);

			assertEquals("src/p/I.java", index.get("methodDecl/foo/0"));
			assertEquals("src/p/A.java,src/p/B.java", index.get("methodDecl/bar/1"));
		} finally {
			workingCopy.discardWorkingCopy();
		}
		index = assertSameIndexAsWithSeparateEnvironments(project);

		assertEquals("src/p/I.java", index.get("methodDecl/foo/0"));
		assertEquals(null, index.get("methodDecl/bar/1"));
	}

	/**
	 * A unit changes while others are waiting to be indexed: they are not resolved with what was known of it.
	 */
	public void testChangeWhileIndexing() throws Exception {
		IJavaProject project = createIndexedProject("src");
		createFolder("/P/src/p");
		waitUntilIndexesReady();
		CountDownLatch allRequested = new CountDownLatch(1);
		try {
			// hold the indexing, so that all the documents are indexed in a row
			this.indexManager.request(new TestJob(() -> {
				allRequested.await();
			}));
			createFileWithoutWaiting("/P/src/p/I.java", "package p;\npublic interface I {\n	void foo();\n}\n");
			createFileWithoutWaiting("/P/src/p/A.java",
					"package p;\n" +
					"public class A {\n" +
					"	void test() {\n" +
					"		I i = () -> {};\n" +
					"	}\n" +
					"}\n");
			createFileWithoutWaiting("/P/src/p/B.java",
					"package p;\n" +
					"public class B {\n" +
					"	void test() {\n" +
					"		I i = () -> {};\n" +
					"	}\n" +
					"}\n");
			// once A and B are indexed, and before C and D are
			this.indexManager.request(new TestJob(() -> {
				getFile("/P/src/p/I.java").setContents(
						"package p;\npublic interface I {\n	void bar(int x);\n}\n".getBytes(StandardCharsets.UTF_8), IResource.FORCE, null);
			}));
			createFileWithoutWaiting("/P/src/p/C.java",
					"package p;\n" +
					"public class C {\n" +
					"	void test() {\n" +
					"		I i = x -> {};\n" +
					"	}\n" +
					"}\n");
			createFileWithoutWaiting("/P/src/p/D.java",
					"package p;\n" +
					"public class D {\n" +
					"	void test() {\n" +
					"		I i = x -> {};\n" +
					"	}\n" +
					"}\n");
		} finally {
			allRequested.countDown();
		}
		waitUntilIndexesReady();

		Map<String, String> index = dumpIndex(project.getProject());
		assertEquals("src/p/A.java,src/p/B.java", index.get("methodDecl/foo/0"));
		assertEquals("src/p/C.java,src/p/D.java,src/p/I.java", index.get("methodDecl/bar/1"));
	}

	/**
	 * Unlike {@link #createFile(String, String)}, does not wait for the file to be indexed.
	 */
	private void createFileWithoutWaiting(String path, String contents) throws CoreException {
		getFile(path).create(contents.getBytes(StandardCharsets.UTF_8), IResource.FORCE, null);
	}

	/**
	 * The contents of a working copy change while others are waiting to be indexed: they are resolved with its
	 * new contents.
	 */
	public void testWorkingCopyChangeWhileIndexing() throws Exception {
		IJavaProject project = createIndexedProject("src");
		createSource("/P/src/p/I.java", "package p;\npublic interface I {\n	void foo();\n}\n");
		waitUntilIndexesReady();
		ICompilationUnit workingCopy = getCompilationUnit("/P/src/p/I.java");
		workingCopy.becomeWorkingCopy(null);
		CountDownLatch allRequested = new CountDownLatch(1);
		try {
			// hold the indexing, so that all the documents are indexed in a row
			this.indexManager.request(new TestJob(() -> {
				allRequested.await();
			}));
			createFileWithoutWaiting("/P/src/p/A.java",
					"package p;\n" +
					"public class A {\n" +
					"	I i = () -> {};\n" +
					"}\n");
			createFileWithoutWaiting("/P/src/p/B.java",
					"package p;\n" +
					"public class B {\n" +
					"	I i = () -> {};\n" +
					"}\n");
			// once A and B are indexed, and before C and D are
			this.indexManager.request(new TestJob(() -> {
				workingCopy.getBuffer().setContents("package p;\npublic interface I {\n	void bar(int x);\n}\n");
			}));
			createFileWithoutWaiting("/P/src/p/C.java",
					"package p;\n" +
					"public class C {\n" +
					"	I i = x -> {};\n" +
					"}\n");
			createFileWithoutWaiting("/P/src/p/D.java",
					"package p;\n" +
					"public class D {\n" +
					"	I i = x -> {};\n" +
					"}\n");
		} finally {
			allRequested.countDown();
		}
		try {
			waitUntilIndexesReady();

			Map<String, String> index = dumpIndex(project.getProject());
			assertEquals("src/p/A.java,src/p/B.java,src/p/I.java", index.get("methodDecl/foo/0"));
			assertEquals("src/p/C.java,src/p/D.java", index.get("methodDecl/bar/1"));
		} finally {
			workingCopy.discardWorkingCopy();
		}
	}

	/**
	 * A unit that has a working copy is resolved with its saved contents when it is indexed: the units that are
	 * indexed after it are still resolved with the contents of the working copy.
	 */
	public void testWorkingCopyIndexedFirst() throws Exception {
		IJavaProject project = createIndexedProject("src");
		createFolder("/P/src/p");
		waitUntilIndexesReady();
		String saved =
				"package p;\n" +
				"public interface I {\n" +
				"	Runnable RUNNABLE = () -> {};\n" +
				"	void foo();\n" +
				"}\n";
		CountDownLatch allRequested = new CountDownLatch(1);
		ICompilationUnit workingCopy = null;
		try {
			// hold the indexing, so that all the documents are indexed in a row
			this.indexManager.request(new TestJob(() -> {
				allRequested.await();
			}));
			createFileWithoutWaiting("/P/src/p/I.java", saved);
			workingCopy = getCompilationUnit("/P/src/p/I.java");
			workingCopy.becomeWorkingCopy(null);
			workingCopy.getBuffer().setContents(saved.replace("void foo();", "void bar(int x);"));
			createFileWithoutWaiting("/P/src/p/A.java",
					"package p;\n" +
					"public class A {\n" +
					"	I i = x -> {};\n" +
					"}\n");
			createFileWithoutWaiting("/P/src/p/B.java",
					"package p;\n" +
					"public class B {\n" +
					"	I i = x -> {};\n" +
					"}\n");
		} finally {
			allRequested.countDown();
		}
		try {
			waitUntilIndexesReady();

			Map<String, String> index = dumpIndex(project.getProject());
			assertEquals("src/p/I.java", index.get("methodDecl/foo/0"));
			assertEquals("src/p/A.java,src/p/B.java", index.get("methodDecl/bar/1"));
		} finally {
			if (workingCopy != null) {
				workingCopy.discardWorkingCopy();
			}
		}
	}

	/**
	 * The index is deleted while a document is resolved: the document is not indexed, and the environment that
	 * resolved it is released all the same.
	 */
	public void testIndexDeletedWhileResolving() throws Exception {
		IJavaProject project = createIndexedProject("src");
		createSource("/P/src/p/A.java",
				"package p;\n" +
				"public class A {\n" +
				"	Runnable runnable = () -> {};\n" +
				"}\n");
		waitUntilIndexesReady();
		CountDownLatch done = new CountDownLatch(1);
		try {
			// hold the indexing: when idle, the index manager does not keep an environment
			this.indexManager.request(new TestJob(() -> {
				done.await();
			}));
			JavaSearchParticipant participant = new JavaSearchParticipant();
			SearchDocument document = participant.getDocument("/P/src/p/A.java");
			IndexLocation indexLocation = this.indexManager.computeIndexLocation(project.getPath());
			Index index = this.indexManager.getIndex(project.getPath(), indexLocation, true, true);
			ReadWriteMonitor monitor = index.monitor;
			monitor.enterWrite();
			try {
				this.indexManager.indexDocument(document, participant, index, indexLocation.getIndexPath());
			} finally {
				monitor.exitWrite();
			}
			assertTrue("The document should be resolved", document.shouldIndexResolvedDocument());
			assertFalse("No environment should be kept yet", SourceIndexerEnvironment.isKept(this.indexManager));

			index.monitor = null; // as when the index is deleted
			try {
				this.indexManager.indexResolvedDocument(document, participant, index, indexLocation.getIndexPath());
			} finally {
				index.monitor = monitor;
			}
			assertTrue("The environment should be released", SourceIndexerEnvironment.isKept(this.indexManager));
		} finally {
			done.countDown();
		}
	}

	/**
	 * The entries of the classpath are reordered while units are waiting to be indexed, which changes the unit that
	 * declares a type found in two source folders: they are resolved with the new order.
	 */
	public void testClasspathOrderChangeWhileIndexing() throws Exception {
		IJavaProject project = createIndexedProject("src", "src2");
		createSource("/P/src/p/I.java", "package p;\npublic interface I {\n	void foo();\n}\n");
		createSource("/P/src2/p/I.java", "package p;\npublic interface I {\n	void bar(int x);\n}\n");
		createFolder("/P/src/q");
		waitUntilIndexesReady();
		CountDownLatch allRequested = new CountDownLatch(1);
		CountDownLatch firstIndexed = new CountDownLatch(1);
		CountDownLatch classpathChanged = new CountDownLatch(1);
		try {
			// hold the indexing, so that all the documents are indexed in a row
			this.indexManager.request(new TestJob(() -> {
				allRequested.await();
			}));
			createFileWithoutWaiting("/P/src/q/A.java", "package q;\npublic class A {\n	p.I i = () -> {};\n}\n");
			createFileWithoutWaiting("/P/src/q/B.java", "package q;\npublic class B {\n	p.I i = () -> {};\n}\n");
			// once A and B are indexed, and before C and D are
			this.indexManager.request(new TestJob(() -> {
				firstIndexed.countDown();
				classpathChanged.await(60, TimeUnit.SECONDS);
			}));
			createFileWithoutWaiting("/P/src/q/C.java", "package q;\npublic class C {\n	p.I i = x -> {};\n}\n");
			createFileWithoutWaiting("/P/src/q/D.java", "package q;\npublic class D {\n	p.I i = x -> {};\n}\n");
			allRequested.countDown();
			assertTrue("A and B should be indexed", firstIndexed.await(60, TimeUnit.SECONDS));
			// src2 first
			IClasspathEntry[] classpath = project.getRawClasspath();
			List<IClasspathEntry> reordered = new ArrayList<>(Arrays.asList(classpath));
			IClasspathEntry src = null;
			for (IClasspathEntry entry : classpath) {
				if (entry.getPath().lastSegment().equals("src")) {
					src = entry;
				}
			}
			assertNotNull("No src entry", src);
			reordered.remove(src);
			reordered.add(src);
			project.setRawClasspath(reordered.toArray(new IClasspathEntry[0]), null);
		} finally {
			allRequested.countDown();
			classpathChanged.countDown();
		}
		waitUntilIndexesReady();

		Map<String, String> index = dumpIndex(project.getProject());
		assertEquals("src/p/I.java,src/q/A.java,src/q/B.java", index.get("methodDecl/foo/0"));
		assertEquals("src/q/C.java,src/q/D.java,src2/p/I.java", index.get("methodDecl/bar/1"));
	}

	private static class TestJob implements IJob {

		interface Body {
			void run() throws Exception;
		}

		private final Body body;

		TestJob(Body body) {
			this.body = body;
		}

		@Override
		public boolean execute(IProgressMonitor progress) {
			try {
				this.body.run();
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
			return true;
		}

		@Override
		public boolean belongsTo(String jobFamily) {
			return false;
		}

		@Override
		public void cancel() {
			// nothing to do
		}

		@Override
		public void ensureReadyToRun() {
			// nothing to do
		}

		@Override
		public String getJobFamily() {
			return ""; //$NON-NLS-1$
		}
	}

	private void createSource(String path, String contents) throws CoreException {
		createFolder(new Path(path).removeLastSegments(1));
		createFile(path, contents);
	}

	/**
	 * Creates the project P, with the JRE as a module.
	 */
	private IJavaProject createIndexedProject(String... sourceFolders) throws Exception {
		return createJava9ProjectWithJREAttributes("P", sourceFolders, null, CompilerOptions.VERSION_17);
	}

	/**
	 * Indexes the given project again, first with a new environment for each document, which is what the indexer
	 * did before it kept one, then with one name environment for all its documents and: one lookup environment, a
	 * few lookup environments, a new lookup environment for each document.
	 *
	 * @return the entries of the index
	 */
	private Map<String, String> assertSameIndexAsWithSeparateEnvironments(IJavaProject project) throws Exception {
		SourceIndexerEnvironment.KEEP = false;
		Map<String, String> expected = indexAll(project);
		assertFalse("Nothing was indexed", expected.isEmpty());
		SourceIndexerEnvironment.KEEP = true;
		assertEquals("Index with one environment", toString(expected), toString(indexAll(project)));
		SourceIndexerEnvironment.MAX_UNITS = 5;
		assertEquals("Index with a few lookup environments", toString(expected), toString(indexAll(project)));
		SourceIndexerEnvironment.MAX_UNITS = 1;
		assertEquals("Index with a lookup environment for each document", toString(expected), toString(indexAll(project)));
		SourceIndexerEnvironment.MAX_UNITS = this.maxUnits;
		return expected;
	}

	private Map<String, String> indexAll(IJavaProject project) throws Exception {
		waitUntilIndexesReady();
		this.indexManager.removeIndex(project.getPath());
		this.indexManager.indexAll(project.getProject());
		waitUntilIndexesReady();
		return dumpIndex(project.getProject());
	}

	private static String toString(Map<String, String> index) {
		StringBuilder builder = new StringBuilder();
		index.forEach((key, documents) -> builder.append(key).append(" -> ").append(documents).append('\n'));
		return builder.toString();
	}

	/**
	 * @return all the entries of the index of the given project: "category/key" to the sorted names of the
	 *         documents
	 */
	private Map<String, String> dumpIndex(IProject project) throws IOException {
		Map<String, String> entries = new TreeMap<>();
		Index index = this.indexManager.getIndex(project.getFullPath(), true, false);
		assertNotNull("No index for " + project, index);
		index.monitor.enterRead();
		try {
			for (char[] category : CATEGORIES) {
				EntryResult[] results = index.query(new char[][] { category }, null, SearchPattern.R_PREFIX_MATCH);
				if (results != null) {
					for (EntryResult result : results) {
						String[] documents = result.getDocumentNames(index);
						Arrays.sort(documents);
						entries.put(new String(category) + '/' + new String(result.getWord()), String.join(",", documents));
					}
				}
			}
		} finally {
			index.monitor.exitRead();
		}
		return entries;
	}
}
