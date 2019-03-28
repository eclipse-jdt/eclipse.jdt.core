/*******************************************************************************
 * Copyright (c) 2017, 2018 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.SourceVersion;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.eclipse.jdt.compiler.apt.tests.NegativeTests.TestDiagnosticListener;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

import junit.framework.TestCase;

public class Java9ElementsTests extends TestCase {
	private static final String MODULE_PROC = "org.eclipse.jdt.compiler.apt.tests.processors.elements.Java9ElementProcessor";

	public void _testRootElements1Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testRootElements1", null);
	}
	public void _testRootElements1() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testRootElements1", null);
	}

	public void _testRootElements2Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testRootElements2", null);
	}
	public void _testRootElements2() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testRootElements2", null);
	}

	public void testAnnotations1Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleAnnotation1", null);
	}
	public void testAnnotations1() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleAnnotation1", null);
	}

	public void testModuleElement1Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleElement1", null);
	}
	public void testModuleElement1() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleElement1", null);
	}

	public void testModuleElement2Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleElement2", null);
	}
	public void testModuleElement2() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleElement2", null);
	}

	public void testModuleElement3Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleElement3", null);
	}
	public void testModuleElement3() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleElement3", null);
	}
	public void testModuleElement4Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleElement4", null);
	}
	public void testModuleElement4() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleElement4", null);
	}

	public void testModuleElement5Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleElement5", null);
	}
	public void testModuleElement5() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleElement5", null);
	}

	public void testModuleElement6Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleElement6", null);
	}
	public void testModuleElement6() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleElement6", null);
	}

	public void _testModuleElement7Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleElement7", null);
	}
	public void testModuleElement7() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleElement7", null);
	}

	public void testModuleJavaBase1Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase1", null);
	}
	public void testModuleJavaBase1() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase1", null);
	}

	
	public void testModuleJavaBase2Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase2", null);
	}
	public void testModuleJavaBase2() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase2", null);
	}

	
	public void testModuleJavaBase3Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase3", null);
	}
	public void testModuleJavaBase3() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase3", null);
	}

	public void testModuleJavaBase4Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase4", null);
	}
	public void testModuleJavaBase4() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase4", null);
	}

	public void testModuleJavaBase5Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase5", null);
	}
	public void testModuleJavaBase5() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase5", null);
	}

	public void testModuleTypeMirror1Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleTypeMirror1", null);
	}
	public void testModuleTypeMirror1() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleTypeMirror1", null);
	}

	public void testModuleTypeMirror2Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleTypeMirror2", null);
	}
	public void testModuleTypeMirror2() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleTypeMirror2", null);
	}

	public void testModuleJavaSql1Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleJavaSql1", null);
	}
	public void testModuleJavaSql1() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleJavaSql1", null);
	}

	public void testSourceModule1Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testSourceModule1", null);
	}
	public void testSourceModule1() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testSourceModule1", null);
	}

	public void testSourceModule2Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testSourceModule2", null);
	}
	public void testSourceModule2() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testSourceModule2", null);
	}
	public void testUnnamedModule1Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest(compiler, MODULE_PROC, "testUnnamedModule1", null, "model9");
	}
	public void testUnnamedModule1() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "testUnnamedModule1", null, "model9");
	}
	public void testUnnamedModule2Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest(compiler, MODULE_PROC, "testUnnamedModule2", null, "model9");
	}
	public void testUnnamedModule2() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "testUnnamedModule2", null, "model9");
	}
	public void testUnnamedModule3Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest(compiler, MODULE_PROC, "testUnnamedModule3", null, "model9a");
	}
	public void testUnnamedModule3() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "testUnnamedModule3", null, "model9a");
	}
	public void testUnnamedModule4Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest(compiler, MODULE_PROC, "testUnnamedModule4", null, "model9a");
	}
	public void testUnnamedModule4() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "testUnnamedModule4", null, "model9a");
	}
	public void testUnnamedModule5Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest(compiler, MODULE_PROC, "testUnnamedModule5", null, new String[] {
				"targets/model9x/X.java",
				"package targets.model9x;\n" +
				"public class X {\n" +
				"    X(final int j) {\n" +
				"        j = 4;\n" +
				"    }\n" +
				"}\n"
		});
	}
	public void testUnnamedModule5() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "testUnnamedModule5", null, new String[] {
				"targets/model9x/X.java",
				"package targets.model9x;\n" +
				"public class X {\n" +
				"    X(final int j) {\n" +
				"        j = 4;\n" +
				"    }\n" +
				"}\n"
		});
	}
	public void testBug521723() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithBinary(compiler, MODULE_PROC, "9", "testBug521723", null, "bug521723");
	}
	public void testBug521723Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithBinary(compiler, MODULE_PROC, "9", "testBug521723", null, "bug521723");
	}
	public void testDirectiveVisitorJavac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest2(compiler, MODULE_PROC, "testDirectiveVisitor", null);
	}
	public void testTypesImpl() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testTypesImpl", null);
	}
	public void testBug498022a() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "testBug498022a", null, "model9");
	}
	public void testBug498022aJavac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, MODULE_PROC, "testBug498022a", null, "model9");
	}
	public void testBug498022b() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "testBug498022b", null, "model9");
	}
	public void testBug498022bJavac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, MODULE_PROC, "testBug498022b", null, "model9");
	}
	public void testBug535819() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, MODULE_PROC, "testBug535819", null, "bug535819", true);
	}
	protected void internalTestWithBinary(JavaCompiler compiler, String processor, String compliance, String testMethod, String testClass, String resourceArea) throws IOException {
		if (!canRunJava9()) {
			return;
		}
		System.clearProperty(processor);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", resourceArea);
		if (testClass == null || testClass.equals("")) {
			BatchTestUtils.copyResources("targets/" + resourceArea, targetFolder);
		} else {
			BatchTestUtils.copyResource("targets/" + resourceArea + "/" + testClass, targetFolder);
		}
		

		List<String> options = new ArrayList<String>();
		options.add("-A" + processor);
		options.add("-A" + testMethod);
		options.add("-processor");
		options.add(processor);
		// Javac 1.8 doesn't (yet?) support the -1.8 option
		if (compiler instanceof EclipseCompiler) {
			options.add("-" + compliance);
		} else {
			options.add("-source");
			options.add(compliance);
		}
		BatchTestUtils.compileTreeAndProcessBinaries(compiler, options, processor, targetFolder, null);

		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		assertEquals("succeeded", System.getProperty(processor));
	}
	private void internalTest(JavaCompiler compiler, String processor, String testMethod, String testClass, String[] source) throws IOException {
		if (!canRunJava9()) {
			return;
		}
		if ((source.length % 2) != 0) return;

		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName());
		for(int i = 0; i < source.length;) {
			File targetFile = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), source[i++]);
			BatchTestUtils.writeFile(targetFile, source[i++].getBytes());
		}

		List<String> options = new ArrayList<String>();
		options.add("-processor");
		options.add(MODULE_PROC);
		options.add("-A" + processor);
		options.add("-A" + testMethod);
		if (compiler instanceof EclipseCompiler) {
			options.add("-9");
		}
		BatchTestUtils.compileTreeWithErrors(compiler, options, targetFolder, null, true, true);

		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		assertEquals("succeeded", System.getProperty(processor));
	}
	private void internalTest(JavaCompiler compiler, String processor, String testMethod, String testClass, String resourceArea) throws IOException {
		internalTest(compiler, processor, testMethod, testClass, resourceArea, false);
	}
	private void internalTest(JavaCompiler compiler, String processor, String testMethod, String testClass, String resourceArea, boolean continueWithErrors) throws IOException {
		if (!canRunJava9()) {
			return;
		}
		System.clearProperty(processor);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", resourceArea);
		if (testClass == null || testClass.equals("")) {
			BatchTestUtils.copyResources("targets/" + resourceArea, targetFolder);
		} else {
			BatchTestUtils.copyResource("targets/" + resourceArea + "/" + testClass, targetFolder);
		}
		

		List<String> options = new ArrayList<String>();
		options.add("-A" + processor);
		options.add("-A" + testMethod);
		if (compiler instanceof EclipseCompiler) {
			options.add("-9");
		}
		ByteArrayOutputStream errBuffer = new ByteArrayOutputStream();
		PrintWriter printWriter = new PrintWriter(errBuffer);
		TestDiagnosticListener diagnosticListener = new TestDiagnosticListener(printWriter);
		if (continueWithErrors) {
			BatchTestUtils.compileTreeWithErrors(compiler, options, targetFolder, diagnosticListener, true, true);
		} else {
			BatchTestUtils.compileTree(compiler, options, targetFolder, true);
		}

		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		assertEquals("succeeded", System.getProperty(processor));
	}
	/*
	 * Tests are run in multi-module mode
	 */
	private void internalTest2(JavaCompiler compiler, String processor, String testMethod, String testClass) throws IOException {
		if (!canRunJava9()) {
			return;
		}
		System.clearProperty(MODULE_PROC);
		File srcRoot = TestUtils.concatPath(BatchTestUtils.getSrcFolderName());
		BatchTestUtils.copyResources("mod_locations/modules", srcRoot);

		List<String> options = new ArrayList<String>();
		options.add("-processor");
		options.add(MODULE_PROC);
		options.add("-A" + MODULE_PROC);
		options.add("-A" + testMethod);
		if (compiler instanceof EclipseCompiler) {
			options.add("-9");
		}
		BatchTestUtils.compileInModuleMode(compiler, options, MODULE_PROC, srcRoot, null, true);
		assertEquals("succeeded", System.getProperty(MODULE_PROC));
	}
	public boolean canRunJava9() {
		try {
			SourceVersion.valueOf("RELEASE_9");
		} catch(IllegalArgumentException iae) {
			return false;
		}
		return true;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}