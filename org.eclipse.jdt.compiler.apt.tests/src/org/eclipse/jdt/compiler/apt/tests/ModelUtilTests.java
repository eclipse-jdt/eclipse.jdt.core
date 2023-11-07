/*******************************************************************************
 * Copyright (c) 2007, 2023 BEA Systems, Inc.
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
import java.util.ArrayList;
import java.util.List;

import javax.lang.model.SourceVersion;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import junit.framework.TestCase;

/**
 * Tests of the language model utility APIs, e.g., javax.lang.model.util.*
 */
public class ModelUtilTests extends TestCase
{
	// Processor class names; see corresponding usage in the processor classes.
	private static final String ELEMENTUTILSPROC = "org.eclipse.jdt.compiler.apt.tests.processors.elementutils.ElementUtilsProc";
	private static final String TYPEUTILSPROC = "org.eclipse.jdt.compiler.apt.tests.processors.typeutils.TypeUtilsProc";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	/**
	 * Validate the testElements test against the javac compiler.
	 */
	public void _testElementsWithSystemCompiler() throws IOException {
		// Commented out - to be fixed by https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1306
		if (!canRunJava8()) {
			return;
		}
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest(compiler, ELEMENTUTILSPROC);
	}

	/**
	 * Test the Elements utility implementation.
	 */
	public void testElementsWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, ELEMENTUTILSPROC);
	}

	/**
	 * Validate the testTypes test against the javac compiler.
	 */
	public void testTypesWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest(compiler, TYPEUTILSPROC);
	}

	/**
	 * Test the Types utility implementation.
	 */
	public void testTypesWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPEUTILSPROC);
	}

	/**
	 * Test functionality by running a particular processor against the types in
	 * resources/targets.  The processor must support "*" (the set of all annotations)
	 * and must report its errors or success via the methods in BaseProcessor.
	 */
	private void internalTest(JavaCompiler compiler, String processorClass) throws IOException {
		System.clearProperty(processorClass);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "model");
		BatchTestUtils.copyResources("targets/model", targetFolder);

		List<String> options = new ArrayList<String>();
		options.add("-A" + processorClass);
		BatchTestUtils.compileTree(compiler, options, targetFolder);

		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		assertEquals("succeeded", System.getProperty(processorClass));
	}
	private boolean canRunJava8() {
		try {
			SourceVersion.valueOf("RELEASE_8");
		} catch(IllegalArgumentException iae) {
			return false;
		}
		return true;
	}
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
