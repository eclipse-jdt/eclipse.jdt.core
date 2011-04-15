/*******************************************************************************
 * Copyright (c) 2007, 2011 BEA Systems, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import junit.framework.TestCase;

/**
 * Tests for the implementation of javax.annotation.processing.Messager
 * @since 3.3
 */
public class MessagerTests extends TestCase {
	
	public final class DiagnosticReport<S> implements DiagnosticListener<S> {
		public int errors;

		DiagnosticReport() {
			this.errors = 0;
		}
		public void report(Diagnostic<? extends S> diagnostic) {
			if (diagnostic.getKind() ==  Diagnostic.Kind.ERROR) {
				errors++;
			}
		}
	}
	// See corresponding usages in the MessagerProc class
	private static final String MESSAGERPROCNAME = "org.eclipse.jdt.compiler.apt.tests.processors.messager.MessagerProc";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	/**
	 * Validate the testMessager test against the javac compiler.
	 * @throws IOException 
	 */
	public void testMessagerWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		DiagnosticReport<JavaFileObject> diagnosticListener = new DiagnosticReport<JavaFileObject>();
		internalTestMessager(compiler, diagnosticListener);
		// surprisingly enough javac 1.7 only reports 3 errors
		// javac 1.6 reports 4 errors as expected
		assertTrue("Wrong number of reported errors", diagnosticListener.errors >= 3);
	}

	/**
	 * Attempt to report errors on various elements, using the Eclipse compiler.
	 * @throws IOException 
	 */
	public void testMessagerWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		DiagnosticReport<JavaFileObject> diagnosticListener = new DiagnosticReport<JavaFileObject>();
		internalTestMessager(compiler, diagnosticListener);
		assertEquals("Wrong number of reported errors", 4, diagnosticListener.errors);
	}

	/**
	 * Attempt to report errors on various elements.
	 * @throws IOException
	 * @return the outputted errors, if the test succeeded enough to generate them
	 */
	private void internalTestMessager(JavaCompiler compiler, DiagnosticListener<? super JavaFileObject> diagnosticListener) throws IOException {
		System.clearProperty(MESSAGERPROCNAME);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "errors");
		BatchTestUtils.copyResources("targets/errors", targetFolder);

		// Turn on the MessagerProc - without this, it will just return without doing anything
		List<String> options = new ArrayList<String>();
		options.add("-A" + MESSAGERPROCNAME);
		options.add("-nowarn");

		// Invoke processing by compiling the targets.errors resources
		boolean success = BatchTestUtils.compileTreeWithErrors(compiler, options, targetFolder, diagnosticListener);
		
		assertTrue("Compilation should have failed due to expected errors, but it didn't", !success);

		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		String property = System.getProperty(MESSAGERPROCNAME);
		assertNotNull("No property", property);
		assertEquals("succeeded", property);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(MESSAGERPROCNAME);
		super.tearDown();
	}
	
}
