/*******************************************************************************
 * Copyright (c) 2018, 2023 IBM Corporation.
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.lang.model.SourceVersion;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import junit.framework.TestCase;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

public class Java11ElementsTests extends TestCase {
	private static final String MODULE_PROC = "org.eclipse.jdt.compiler.apt.tests.processors.elements.Java11ElementProcessor";

	public void testFiler1() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testFiler1", null);
	}
	public void testFiler1Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest2(compiler, MODULE_PROC, "testFiler1", null);
	}
	public void testFiler2() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testFiler2", null);
	}
	public void testFiler2Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest2(compiler, MODULE_PROC, "testFiler2", null);
	}
	public void testFiler3() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest2(compiler, MODULE_PROC, "testFiler3", null);
	}
	public void testFiler3Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest2(compiler, MODULE_PROC, "testFiler3", null);
	}
	protected void internalTestWithBinary(JavaCompiler compiler, String processor, String compliance, String testMethod, String testClass, String resourceArea) throws IOException {
		if (!canRunJava11()) {
			return;
		}
		System.clearProperty(processor);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", resourceArea);
		if (testClass == null || testClass.equals("")) {
			BatchTestUtils.copyResources("targets/" + resourceArea, targetFolder);
		} else {
			BatchTestUtils.copyResource("targets/" + resourceArea + "/" + testClass, targetFolder);
		}


		List<String> options = new ArrayList<>();
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
	/*
	 * Tests are run in multi-module mode
	 */
	private void internalTest2(JavaCompiler compiler, String processor, String testMethod, String testClass) throws IOException {
		if (!canRunJava11()) {
			return;
		}
		System.clearProperty(MODULE_PROC);
		File srcRoot = TestUtils.concatPath(BatchTestUtils.getSrcFolderName());
		BatchTestUtils.copyResources("mod_locations/modules", srcRoot);

		List<String> options = new ArrayList<>();
		options.add("-processor");
		options.add(MODULE_PROC);
		options.add("-A" + MODULE_PROC);
		options.add("-A" + testMethod);
		if (compiler instanceof EclipseCompiler) {
			options.add("-17");
		}
		BatchTestUtils.compileInModuleMode(compiler, options, MODULE_PROC, srcRoot, null, true, false);
		assertEquals("succeeded", System.getProperty(MODULE_PROC));
	}
	public boolean canRunJava11() {
		try {
			SourceVersion.valueOf("RELEASE_11");
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