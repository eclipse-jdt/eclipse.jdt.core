/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import junit.framework.TestCase;

/**
 * Tests for the implementation of javax.annotation.processing.Messager
 * @since 3.3
 */
public class MessagerTests extends TestCase {
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
		internalTestMessager(compiler);
	}

	/**
	 * Attempt to report errors on various elements, using the Eclipse compiler.
	 * @throws IOException 
	 */
	public void testMessagerWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestMessager(compiler);
	}

	/**
	 * Attempt to read various elements of the Element hierarchy.
	 * @throws IOException
	 */
	private void internalTestMessager(JavaCompiler compiler) throws IOException {
		System.clearProperty(MESSAGERPROCNAME);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "errors");
		BatchTestUtils.copyResources("targets/errors", targetFolder);

		// Turn on the MessagerProc - without this, it will just return without doing anything
		List<String> options = new ArrayList<String>();
		options.add("-A" + MESSAGERPROCNAME);

		// Invoke processing by compiling the targets.model resources
		StringWriter errors = new StringWriter();
		boolean success = BatchTestUtils.compileTreeWithErrors(compiler, options, targetFolder, errors);
		
		assertTrue("errors should not be empty", errors.getBuffer().length() != 0);
		assertTrue("Compilation should have failed due to expected errors, but it didn't", !success);

		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		String property = System.getProperty(MESSAGERPROCNAME);
		assertNotNull("No property", property);
		assertEquals("succeeded", property);
		
		// TODO: check "errors" against expected values to ensure that the problems were correctly reported
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
