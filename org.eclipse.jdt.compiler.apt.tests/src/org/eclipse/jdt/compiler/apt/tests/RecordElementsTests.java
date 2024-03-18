/*******************************************************************************
 * Copyright (c) 2020, 2024 IBM Corporation.
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

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

import junit.framework.TestCase;

public class RecordElementsTests extends TestCase {
	private static final String MODULE_PROC = "org.eclipse.jdt.compiler.apt.tests.processors.elements.RecordElementProcessor";

	public void _testPreviewFlagTrue() throws IOException {
		if (!isRunning21()) {
			return;
		}
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, CompilerOptions.getLatestVersion(), "testPreviewFlagTrue", null, "records", true);
	}
	public void testRecords1() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords1", null, "records", false);
	}
	public void testRecords1Javac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords1", null, "records", false);
	}
	public void testRecords2() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords2", null, "records", false);
	}
	public void testRecords2Javac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords2", null, "records", false);
	}
	public void testRecords3() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords3", null, "records", false);
	}
	public void testRecords3Javac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords3", null, "records", false);
	}
	public void testRecords3a() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords3a", null, "records", false);
	}
	public void testRecords3aJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords3a", null, "records", false);
	}
	public void testRecords4() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords4", null, "records", false);
	}
	public void testRecords4Javac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords4", null, "records", false);
	}
	public void testRecords4a() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords4a", null, "records", false, true);
	}
	public void _testRecords4aJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords4a", null, "records", false, true);
	}
	public void testRecords5() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords5", null, "records", false);
	}
	public void testRecords5Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords5", null, "records", false);
	}
	public void testRecords5a() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords5a", null, "records", false);
	}
	public void testRecords5aJavac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords5a", null, "records", false);
	}
	public void testRecords6() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords6", null, "records", false);
	}
	public void testRecords6Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords6", null, "records", false);
	}
	public void testRecords7() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords7", null, "records", false);
	}
	public void testRecords7Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords7", null, "records", false);
	}
	public void testRecords8() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords8", null, "records", false);
	}
	public void testRecords8Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords8", null, "records", false);
	}
	public void testRecords9() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords9", null, "records", false);
	}
	public void testRecords9Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords9", null, "records", false);
	}
	public void testRecords10() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords10", null, "records", false);
	}
	public void testRecords10Javac() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "16", "testRecords10", null, "records", false);
	}
	public void testRecordsConstructorsJavac() throws IOException {
		if (!canRunJava21()) {
			return;
		}
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "21", "testRecordsConstructors", null, "records", true, true);
	}
	public void testRecordsConstructors() throws IOException {
		if (!canRunJava21()) {
			return;
		}
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestWithPreview(compiler, MODULE_PROC, "21", "testRecordsConstructors", null, "records", true, true);
	}
	public boolean canRunJava21() {
		try {
			SourceVersion.valueOf("RELEASE_21");
		} catch(IllegalArgumentException iae) {
			return false;
		}
		return true;
	}
	protected void internalTestWithPreview(JavaCompiler compiler, String processor, String compliance,
			String testMethod, String testClass, String resourceArea, boolean preview) throws IOException {
		internalTestWithPreview(compiler, processor, compliance, testMethod, testClass, resourceArea, preview, false);
	}
	protected void internalTestWithPreview(JavaCompiler compiler, String processor, String compliance,
			String testMethod, String testClass, String resourceArea, boolean preview, boolean processBinaries) throws IOException {
		if (!isRunning16()) {
			return;
		}
		System.clearProperty(processor);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "mod_locations", resourceArea);
		if (testClass == null || testClass.equals("")) {
			BatchTestUtils.copyResources("mod_locations/" + resourceArea, targetFolder);
		} else {
			BatchTestUtils.copyResource("mod_locations/" + resourceArea + "/" + testClass, targetFolder);
		}

		List<String> options = new ArrayList<>();
		options.add("-A" + processor);
		options.add("-A" + testMethod);
		options.add("-processor");
		options.add(processor);
		if (compiler instanceof EclipseCompiler) {
			options.add("-" + compliance);
		} else {
			options.add("-source");
			options.add(compliance);
		}
//		if (preview)
//			options.add("--enable-preview");
		BatchTestUtils.compileInModuleMode(compiler, options, processor, targetFolder, null, true, processBinaries);
		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		assertEquals("succeeded", System.getProperty(processor));
	}
	public boolean isRunning21() {
		try {
			SourceVersion.valueOf("RELEASE_21");
		} catch(IllegalArgumentException iae) {
			return false;
		}
		return true;
	}
	public boolean isRunning17() {
		try {
			SourceVersion.valueOf("RELEASE_17");
		} catch(IllegalArgumentException iae) {
			return false;
		}
		return true;
	}
	public boolean isRunning16() {
		try {
			SourceVersion.valueOf("RELEASE_16");
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