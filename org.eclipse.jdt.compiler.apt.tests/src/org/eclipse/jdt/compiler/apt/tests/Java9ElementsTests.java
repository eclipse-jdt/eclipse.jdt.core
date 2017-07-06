/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.SourceVersion;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

import junit.framework.TestCase;

public class Java9ElementsTests extends TestCase {
	private static final String MODULE_PROC = "org.eclipse.jdt.compiler.apt.tests.processors.elements.Java9ElementProcessor";

	public void testRootElementsJavac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testRootElements", null);
	}

	public void testRootElements() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testRootElements", null);
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

	public void testModuleElement7Javac() throws IOException {
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

	public void testModuleJavaBase1() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase1", null);
	}
	public void testModuleJavaBase1Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase1", null);
	}
	
	public void testModuleJavaBase2() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase2", null);
	}
	public void testModuleJavaBase2Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase2", null);
	}
	
	public void testModuleJavaBase3() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase3", null);
	}
	public void testModuleJavaBase3Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase3", null);
	}
	
	public void testModuleJavaBase4() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase4", null);
	}
	public void testModuleJavaBase4Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase4", null);
	}
	public void testModuleJavaBase5() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase5", null);
	}
	public void testModuleJavaBase5Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleJavaBase5", null);
	}
	public void testModuleJavaSql1() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testModuleJavaSql1", null);
	}
	public void testModuleJavaSql1Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testModuleJavaSql1", null);
	}
	public void testSourceModule1() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testSourceModule1", null);
	}
	public void testSourceModule1Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testSourceModule1", null);
	}
	public void testSourceModule2() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testSourceModule2", null);
	}
	public void testSourceModule2Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest2(compiler, MODULE_PROC, "testSourceModule2", null);
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
		BatchTestUtils.copyResources("modules", srcRoot);

		List<String> options = new ArrayList<String>();
		options.add("-processor");
		options.add(MODULE_PROC);
		options.add("-A" + MODULE_PROC);
		options.add("-A" + testMethod);
		if (compiler instanceof EclipseCompiler) {
			options.add("-9");
		}
		BatchTestUtils.compileInModuleMode(compiler, options, srcRoot, null, true);
		assertEquals("succeeded", System.getProperty(MODULE_PROC));
	}
//	private void internalTest(JavaCompiler compiler, String processor, String testMethod, String testClass) throws IOException {
//		if (!canRunJava9()) {
//			return;
//		}
//		System.clearProperty(MODULE_PROC);
////		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "mod.one");
//		File srcRoot = TestUtils.concatPath(BatchTestUtils.getSrcFolderName());
//		BatchTestUtils.copyResources("modules", srcRoot);
//		//		BatchTestUtils.copyResource("module-info.java", srcRoot);
////		BatchTestUtils.copyResources("targets/model9", targetFolder);
//
//		List<String> options = new ArrayList<String>();
//		options.add("-processor");
//		options.add(MODULE_PROC);
//		options.add("-A" + MODULE_PROC);
//		options.add("-A" + testMethod);
//		if (compiler instanceof EclipseCompiler) {
//			options.add("-9");
//		}
//		BatchTestUtils.compileInModuleMode(compiler, options, srcRoot, null, true);
//		assertEquals("succeeded", System.getProperty(MODULE_PROC));
//	}
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