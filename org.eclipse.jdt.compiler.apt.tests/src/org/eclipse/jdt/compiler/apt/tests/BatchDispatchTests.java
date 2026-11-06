/*******************************************************************************
 * Copyright (c) 2006, 2011 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    IBM Corporation - fix for 342936
 *******************************************************************************/
package org.eclipse.jdt.compiler.apt.tests;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.jdt.compiler.apt.tests.BatchTestUtils.DiagnosticReport;

/**
 * Test the ability to execute annotation processors in batch mode, including
 * tests of file generation, processor environment, etc.
 * <p>
 * This suite is not meant to exhaustively test typesystem functionality.
 * @since 3.3
 */
public class BatchDispatchTests extends TestCase {

	// Processor class names; see corresponding usage in the processor classes.
	private static final String INHERITEDANNOPROC = "org.eclipse.jdt.compiler.apt.tests.processors.inherited.InheritedAnnoProc";

	private static final String[] ONE_ARG_OPTIONS = {
		"-s",
		"-processor",
		"-processorpath"
	};
	private static final String[] ZERO_ARG_OPTIONS = {
		"-proc:none",
		"-proc:only",
		"-XprintProcessorInfo",
		"-XprintRounds"
	};


	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(BatchDispatchTests.class);
		return suite;
	}

	public BatchDispatchTests(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	/**
	 * Verify that Eclipse compiler properly supports apt-related command line options
	 */
	public void testCheckOptions() {
		for (String option : ONE_ARG_OPTIONS) {
			assertEquals(option + " requires 1 argument", 1, BatchTestUtils.getEclipseCompiler().isSupportedOption(option));
		}
		for (String option : ZERO_ARG_OPTIONS) {
			assertEquals(option + " requires no argument", 0, BatchTestUtils.getEclipseCompiler().isSupportedOption(option));
		}
	}

	/**
	 * Veriy that processor sees correct environment options
	 * (sanity check with system compiler)
	 */
	public void testProcessorArgumentsWithSystemCompiler() throws IOException {
		// System compiler
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTestProcessorArguments(compiler);
	}

	/**
	 * Veriy that processor sees correct environment options
	 * when called from Eclipse compiler
	 */
	public void _testProcessorArgumentsWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestProcessorArguments(compiler);
	}

	/**
	 * Read annotation values and generate a class using system compiler (javac)
	 * This is a sanity check to verify that the processors, sample code, and
	 * compiler options are correct.
	 */
	public void testCompilerOneClassWithSystemCompiler() throws IOException {
		// System compiler
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTestGenerateClass(compiler);
	}

	/**
	 * Read annotation values and generate a class using Eclipse compiler
	 */
	public void testGhIssue4687WithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "gh4687");
		File inputFile = BatchTestUtils.copyResource("targets/gh4687/Comp.java", targetFolder);
		assertNotNull("No input file", inputFile);

		List<String> options = new ArrayList<>();
		BatchTestUtils.compileOneClass(compiler, options, inputFile);

		// check that the gen-src and class files were generated
 		File genSrcFile = TestUtils.concatPath(BatchTestUtils.getGenFolderName(), "foobar", "ImmutableComp.java");
 		assertTrue("generated src file does not exist", genSrcFile.exists());

		inputFile = BatchTestUtils.copyResource("targets/gh4687/Container.java", targetFolder);
		assertNotNull("No input file", inputFile);

		options = new ArrayList<>();
		BatchTestUtils.compileOneClass(compiler, options, inputFile);
		// check that the gen-src and class files were generated
 		genSrcFile = TestUtils.concatPath(BatchTestUtils.getGenFolderName(), "foobar", "WorkingRecordBuilder.java");
 		assertTrue("generated src file does not exist", genSrcFile.exists());
	}
	
	/**
	 * Read annotation values and generate a class using Eclipse compiler
	 */
	public void testCompilerOneClassWithEclipseCompiler() throws IOException {
		// Eclipse compiler
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestGenerateClass(compiler);
	}

	public void testWarningsWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWarnings(compiler, 1, "-warn:+unused");
	}

	public void testNoWarningsWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWarnings(compiler, 0, "-warn:+unused", "-nowarn");
	}

	public void testUnusedWarningWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWarnings(compiler, 1, "-nowarn", "-warn:+unused");
	}

	public void testNoWarningsInFolderWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWarnings(compiler, 0, "-warn:+unused", "-nowarn:[" + BatchTestUtils.getGenFolderName() + ']');
	}

	public void testNoWarningsInFolderWithEclipseCompiler2() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWarnings(compiler, 0, "-nowarn:[" + BatchTestUtils.getGenFolderName() + ']', "-warn:+unused");
	}

	private void internalTestWarnings(JavaCompiler compiler, int numberOfWarnings, String... extraOptions) throws IOException {
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "dispatch");
		File inputFile = BatchTestUtils.copyResource("targets/dispatch/WarnGenClass.java", targetFolder);
		assertNotNull("No input file", inputFile);

		List<String> options = new ArrayList<>();
		if (extraOptions != null) {
			options.addAll(Arrays.asList(extraOptions));
		}
		DiagnosticReport<JavaFileObject> diagnostics = BatchTestUtils.compileOneClass(compiler, options, inputFile);

		// check that the gen-src and class files were generated
 		File genSrcFile = TestUtils.concatPath(BatchTestUtils.getGenFolderName(), "gen", "WarnGen.java");
 		assertTrue("generated src file does not exist", genSrcFile.exists());

 		File classFile = TestUtils.concatPath(BatchTestUtils.getBinFolderName(), "targets", "dispatch", "WarnGenClass.class");
 		assertTrue("ordinary src file was not compiled", classFile.exists());

 		File genClassFile = TestUtils.concatPath(BatchTestUtils.getBinFolderName(), "gen", "WarnGen.class");
 		assertTrue("generated src file was not compiled", genClassFile.exists());

 		assertEquals("Wrong number of warnings", numberOfWarnings,
 				diagnostics.get(Diagnostic.Kind.WARNING, Diagnostic.Kind.MANDATORY_WARNING).size());
	}

	/**
	 * Validate the inherited annotations test against the javac compiler.
	 */
	public void testInheritedAnnosWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTestInheritance(compiler, INHERITEDANNOPROC);
	}

	/**
	 * Test dispatch of annotation processor on inherited annotations.
	 */
	public void testInheritedAnnosWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestInheritance(compiler, INHERITEDANNOPROC);
	}

	/**
	 * Verify that if a type has two annotations, both processors are run.
	 */
	public void _testTwoAnnotations() throws IOException {
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "dispatch");
		File inputFile = BatchTestUtils.copyResource("targets/dispatch/TwoAnnotations.java", targetFolder);
		assertNotNull("No input file", inputFile);

		List<String> options = new ArrayList<>();
		// See corresponding list in CheckArgsProc processor.
		// Processor will throw IllegalStateException if it detects a mismatch.
		options.add("-Afoo=bar");
		options.add("-Anovalue");
		options.add("-Abar2=");
		BatchTestUtils.compileOneClass(BatchTestUtils.getEclipseCompiler(), options, inputFile);

		// check that the src and class files were generated
 		File genSrcFile = TestUtils.concatPath(BatchTestUtils.getGenFolderName(), "gen", "TwoAnnotationsGen.java");
 		assertTrue("generated src file does not exist", genSrcFile.exists());

 		File classFile = TestUtils.concatPath(BatchTestUtils.getBinFolderName(), "targets", "dispatch", "TwoAnnotations.class");
 		assertTrue("ordinary src file was not compiled", classFile.exists());

 		File genClassFile = TestUtils.concatPath(BatchTestUtils.getBinFolderName(), "gen", "TwoAnnotationsGen.class");
 		assertTrue("generated src file was not compiled", genClassFile.exists());
	}

	// Called with system compiler and Eclipse compiler
	private void internalTestGenerateClass(JavaCompiler compiler) throws IOException {
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "dispatch");
		File inputFile = BatchTestUtils.copyResource("targets/dispatch/HasGenClass.java", targetFolder);
		assertNotNull("No input file", inputFile);

		List<String> options = new ArrayList<>();
		BatchTestUtils.compileOneClass(compiler, options, inputFile);

		// check that the gen-src and class files were generated
 		File genSrcFile = TestUtils.concatPath(BatchTestUtils.getGenFolderName(), "gen", "HgcGen.java");
 		assertTrue("generated src file does not exist", genSrcFile.exists());

 		File classFile = TestUtils.concatPath(BatchTestUtils.getBinFolderName(), "targets", "dispatch", "HasGenClass.class");
 		assertTrue("ordinary src file was not compiled", classFile.exists());

 		File genClassFile = TestUtils.concatPath(BatchTestUtils.getBinFolderName(), "gen", "HgcGen.class");
 		assertTrue("generated src file was not compiled", genClassFile.exists());
	}

	// Called with system compiler and Eclipse compiler
	private void internalTestProcessorArguments(JavaCompiler compiler) throws IOException {
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "dispatch");
		File inputFile = BatchTestUtils.copyResource("targets/dispatch/HasCheckArgs.java", targetFolder);
		assertNotNull("No input file", inputFile);

		List<String> options = new ArrayList<>();
		// See corresponding list in CheckArgsProc processor.
		// Processor will throw IllegalStateException if it detects a mismatch.
		options.add("-Afoo=bar");
		options.add("-Anovalue");
		options.add("-Abar2=");
		BatchTestUtils.compileOneClass(compiler, options, inputFile);
	}

	/**
	 * Test functionality by running a particular processor against the types in
	 * resources/targets.  The processor must support "*" (the set of all annotations)
	 * and must report its errors or success via the methods in BaseProcessor.
	 */
	private void internalTestInheritance(JavaCompiler compiler, String processorClass) throws IOException {
		System.clearProperty(processorClass);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets/dispatch", "inheritedanno");
		BatchTestUtils.copyResources("targets/dispatch/inheritedanno", targetFolder);

		List<String> options = new ArrayList<>();
		options.add("-A" + processorClass);
		BatchTestUtils.compileTree(compiler, options, targetFolder);

		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		assertEquals("succeeded", System.getProperty(processorClass));
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=209961
	 */
	public void test209961() throws IOException {
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "dispatch");
		File inputFile = BatchTestUtils.copyResource("targets/dispatch/X.java", targetFolder);
		assertNotNull("No input file", inputFile);
		File classpathEntry =TestUtils.concatPath(
			new File(BatchTestUtils.getPluginDirectoryPath(), BatchTestUtils.getResourceFolderName()).getAbsolutePath(),
			"targets",
			"dispatch");

		List<String> options = new ArrayList<>();
		// See corresponding list in CheckArgsProc processor.
		// Processor will throw IllegalStateException if it detects a mismatch.
		options.add("-classpath");
		options.add(classpathEntry.getAbsolutePath());
		options.add("-verbose");

		BatchTestUtils.compileOneClass(BatchTestUtils.getEclipseCompiler(), options, inputFile);
	}

	/**
	 * Records the order of the annotations given to {@code process()}, of {@link RoundEnvironment#getRootElements()}
	 * and of {@link RoundEnvironment#getElementsAnnotatedWith(Class)} in the first round.
	 */
	@SupportedAnnotationTypes("*")
	private static final class ElementOrderProc extends AbstractProcessor {
		boolean firstRound = true;
		final List<String> annotations = new ArrayList<>();
		final List<String> rootElements = new ArrayList<>();
		final List<String> annotatedElements = new ArrayList<>();

		@Override
		public SourceVersion getSupportedSourceVersion() {
			return SourceVersion.latestSupported();
		}

		@Override
		public boolean process(Set<? extends TypeElement> annotationTypes, RoundEnvironment roundEnv) {
			if (this.firstRound) {
				this.firstRound = false;
				for (TypeElement annotation : annotationTypes) {
					this.annotations.add(annotation.getQualifiedName().toString());
				}
				for (Element element : roundEnv.getRootElements()) {
					this.rootElements.add(element.getSimpleName().toString());
				}
				for (Element element : roundEnv.getElementsAnnotatedWith(Deprecated.class)) {
					this.annotatedElements.add(element.getEnclosingElement().getSimpleName() + "." + element.getSimpleName());
				}
			}
			return false;
		}
	}

	private static final String ELEMENT_ORDER_X = """
			package p;
			public class X {
				@Deprecated public void m0() {}
				@Deprecated public void m1() {}
				@Deprecated public void m2() {}
				@Deprecated public void m3() {}
				@Deprecated public void m4() {}
				@Deprecated public void m5() {}
				@Deprecated public void m6() {}
				@Deprecated public void m7() {}
				@Deprecated public void m8() {}
				@Deprecated public void m9() {}
				@Deprecated public static class Inner {
					@Deprecated public void i0() {}
					@Deprecated public void i1() {}
					@Deprecated public void i2() {}
					@Deprecated public void i3() {}
					@Deprecated public void i4() {}
				}
				@Deprecated public void m10() {}
				@Deprecated public void m11() {}
			}
			""";

	private static final String ELEMENT_ORDER_Y = """
			package p;
			@SuppressWarnings("all") @Deprecated class Y {}
			""";

	/**
	 * The elements come in the order in which their annotations are found: member types first, then the other
	 * members (for these sources, javac gives the same order). Processors such as the Spring Boot configuration
	 * metadata processor report spurious errors when the order changes from one compilation to the next.
	 */
	private static final List<String> ELEMENT_ORDER_EXPECTED = List.of(
			"X.Inner", "Inner.i0", "Inner.i1", "Inner.i2", "Inner.i3", "Inner.i4",
			"X.m0", "X.m1", "X.m2", "X.m3", "X.m4", "X.m5", "X.m6", "X.m7", "X.m8", "X.m9", "X.m10", "X.m11",
			"p.Y");

	private void checkElementOrder(JavaCompiler compiler) throws IOException {
		File binFolder = new File(BatchTestUtils.getBinFolderName());
		binFolder.mkdirs();
		File packageFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "elementorder", "p");
		packageFolder.mkdirs();
		File x = new File(packageFolder, "X.java");
		File y = new File(packageFolder, "Y.java");
		Files.writeString(x.toPath(), ELEMENT_ORDER_X);
		Files.writeString(y.toPath(), ELEMENT_ORDER_Y);
		ElementOrderProc processor = new ElementOrderProc();
		DiagnosticReport<JavaFileObject> report = new DiagnosticReport<>();
		StringWriter out = new StringWriter();
		try (StandardJavaFileManager manager = compiler.getStandardFileManager(null, null, null)) {
			CompilationTask task = compiler.getTask(out, manager, report,
					List.of("-proc:only", "-d", binFolder.getAbsolutePath()), null, manager.getJavaFileObjects(x, y));
			task.setProcessors(List.of(processor));
			assertTrue("Compilation failed: " + out + report, task.call());
		}
		assertEquals("Annotations", List.of("java.lang.Deprecated", "java.lang.SuppressWarnings"), processor.annotations);
		assertEquals("Root elements", List.of("X", "Y"), processor.rootElements);
		// the enclosing element of a top-level type is its package: "p.Y"
		assertEquals("Elements annotated with @Deprecated", ELEMENT_ORDER_EXPECTED, processor.annotatedElements);
	}

	public void testElementOrderWithEclipseCompiler() throws IOException {
		checkElementOrder(BatchTestUtils.getEclipseCompiler());
	}

	@Override
	protected void tearDown() throws Exception {
		BatchTestUtils.tearDown();
	}
}
