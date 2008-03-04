/*******************************************************************************
 * Copyright (c) 2007, 2008 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import junit.framework.TestCase;

/**
 * Test cases for annotation processing behavior when code contains semantic errors
 */
public class NegativeTests extends TestCase
{
	// See corresponding usages in the NegativeModelProc class
	private static final String NEGATIVEMODELPROCNAME = "org.eclipse.jdt.compiler.apt.tests.processors.negative.NegativeModelProc";
	private static final String IGNOREJAVACBUGS = "ignoreJavacBugs";
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	/**
	 * Validate the testNegativeModel test against the javac compiler.
	 * All test routines are executed.
	 * @throws IOException 
	 */
	public void testNegativeModelWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		
		internalTestNegativeModel(compiler, 0, Collections.singletonList("-A" + IGNOREJAVACBUGS));
	}

	/**
	 * Inspect model of resources/targets.negative.pa.Negative1, 
	 * using the Eclipse compiler.
	 * @throws IOException 
	 */
	public void testNegativeModel1WithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestNegativeModel(compiler, 1, null);
	}

	/**
	 * Inspect model of resources/targets.negative.pa.Negative2, 
	 * using the Eclipse compiler.
	 * @throws IOException 
	 */
	public void testNegativeModel2WithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestNegativeModel(compiler, 2, null);
	}

	/**
	 * Inspect model of resources/targets.negative.pa.Negative3, 
	 * using the Eclipse compiler.
	 * @throws IOException 
	 */
	public void testNegativeModel3WithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestNegativeModel(compiler, 3, null);
	}

	/**
	 * Inspect model of resources/targets.negative.pa.Negative4, 
	 * using the Eclipse compiler.
	 * @throws IOException 
	 */
	public void testNegativeModel4WithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestNegativeModel(compiler, 4, null);
	}

	/**
	 * Inspect model of resources/targets.negative.pa.Negative5, 
	 * using the Eclipse compiler.
	 * @throws IOException 
	 */
	public void testNegativeModel5WithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestNegativeModel(compiler, 5, null);
	}

	/**
	 * Inspect model of resources/targets.negative.pa.Negative6, 
	 * using the Eclipse compiler.
	 * @throws IOException 
	 */
	public void testNegativeModel6WithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestNegativeModel(compiler, 6, null);
	}

	/**
	 * Inspect model of resources/targets.negative.pa.Negative7, 
	 * using the Eclipse compiler.
	 * @throws IOException 
	 */
	public void testNegativeModel7WithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestNegativeModel(compiler, 7, null);
	}

	/**
	 * Attempt to report errors on various elements.
	 * @throws IOException
	 */
	private void internalTestNegativeModel(JavaCompiler compiler, int test, Collection<String> extraOptions) throws IOException {
		System.clearProperty(NEGATIVEMODELPROCNAME);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "negative");
		BatchTestUtils.copyResources("targets/negative", targetFolder);

		// Turn on the NegativeModelProc - without this, it will just return without doing anything
		List<String> options = new ArrayList<String>();
		options.add("-A" + NEGATIVEMODELPROCNAME + "=" + test);
		if (null != extraOptions)
			options.addAll(extraOptions);

		// Invoke processing by compiling the targets.model resources
		StringWriter errors = new StringWriter();
		boolean success = BatchTestUtils.compileTreeWithErrors(compiler, options, targetFolder, errors);
		
		assertTrue("errors should not be empty", errors.getBuffer().length() != 0);
		assertTrue("Compilation should have failed due to expected errors, but it didn't", !success);

		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		String property = System.getProperty(NEGATIVEMODELPROCNAME);
		assertNotNull("No property - probably processing did not take place", property);
		assertEquals("succeeded", property);
		
		// TODO: check "errors" against expected values to ensure that the problems were correctly reported
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(NEGATIVEMODELPROCNAME);
		super.tearDown();
	}
	

}
