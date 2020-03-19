/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation.
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

import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

import junit.framework.TestCase;

public class Java8FilerTests extends TestCase {
	private static final String FILER_PROC = "org.eclipse.jdt.compiler.apt.tests.processors.filer.Java8FilerProcessor";

	/**
	 * Validate the testElement test against the javac compiler.
	 * @throws IOException
	 */
	public void testFilerWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest(compiler, true);
	}

	/**
	 * Attempt to read various elements of the Element hierarchy.
	 * @throws IOException
	 */
	public void testFilerWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, false);
	}

	private void internalTest(JavaCompiler compiler, boolean isSystemCommpiler) throws IOException {
		if (!canRunJava8()) {
			return;
		}
		System.clearProperty(FILER_PROC);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "filer8");
		BatchTestUtils.copyResources("targets/filer8", targetFolder);

		List<String> options = new ArrayList<String>();
		options.add("-A" + FILER_PROC);
		// Javac 1.8 doesn't (yet?) support the -1.8 option
		if (compiler instanceof EclipseCompiler) {
			options.add("-1.8");
		}
		BatchTestUtils.compileTree(compiler, options, targetFolder);

		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		assertEquals("succeeded", System.getProperty(FILER_PROC));
	}
	public boolean canRunJava8() {
		try {
			SourceVersion.valueOf("RELEASE_8");
		} catch(IllegalArgumentException iae) {
			return false;
		}
		return true;
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