/*******************************************************************************
 * Copyright (c) 2007, 2011 BEA Systems, Inc.
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
 *    philippe.marschall@netcetera.ch - Regression test for 338370
 *    IBM Corporation - fix for 342936
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import junit.framework.TestCase;

/**
 * Test the implementation of the Filer interface,
 * in more detail than BatchDispatchTests does.
 * @since 3.4
 */
public class FilerTests extends TestCase {
	private static final String TYPEUTILSPROC = "org.eclipse.jdt.compiler.apt.tests.processors.filer.FilerProc";

	/**
	 * Validate the testElement test against the javac compiler.
	 */
	public void testElementWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTestCreateResource(compiler, true);
	}

	/**
	 * Attempt to read various elements of the Element hierarchy.
	 */
	public void _testElementWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestCreateResource(compiler, false);
	}

	/**
	 * Test the createResource() by processing resources/targets/filer/FilerTarget1.java
	 * and verifying the existence and content of the resulting files.
	 */
	private void internalTestCreateResource(JavaCompiler compiler, boolean isSystemCommpiler) throws IOException {
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "filer");
		File inputFile = BatchTestUtils.copyResource("targets/filer/FilerTarget1.java", targetFolder);
		assertNotNull("No input file", inputFile);

		List<String> options = new ArrayList<String>();
		BatchTestUtils.compileOneClass(compiler, options, inputFile);

		// check that the resource and class files were generated
 		File genTextFile = TestUtils.concatPath(BatchTestUtils.getGenFolderName(), "resources", "txt", "text.txt");
 		assertTrue("generated text file does not exist", genTextFile.exists());

 		File genBinaryFile = TestUtils.concatPath(BatchTestUtils.getGenFolderName(), "resources", "dat", "binary.dat");
 		assertTrue("generated binary file does not exist", genBinaryFile.exists());

 		File classFile = TestUtils.concatPath(BatchTestUtils.getBinFolderName(), "targets", "filer", "FilerTarget1.class");
 		assertTrue("ordinary src file was not compiled", classFile.exists());

 		// See corresponding test data in FilerTarget1.java
 		assertTrue(BatchTestUtils.fileContentsEqualText(genTextFile, "A generated string"));
 		assertTrue(BatchTestUtils.fileContentsEqualText(genBinaryFile, new String(new byte[] {102, 110, 111, 114, 100})));

 		if (!isSystemCommpiler) {
 			assertEquals("succeeded", System.getProperty(TYPEUTILSPROC));
 		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
