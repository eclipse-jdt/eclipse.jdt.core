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
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import junit.framework.TestCase;

/**
 * 
 * @since 3.3
 */
public class ModelTests extends TestCase {
	
	// See corresponding usages in the ElementProc class
	private static final String ELEMENTPROCNAME = "org.eclipse.jdt.compiler.apt.tests.processors.elements.ElementProc";
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	/**
	 * Validate the testElement test against the javac compiler.
	 * @throws IOException 
	 */
	public void testElementWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestElement(compiler);
	}

	/**
	 * Attempt to read various elements of the Element hierarchy.
	 * @throws IOException 
	 */
	public void testElementWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTestElement(compiler);
	}

	/**
	 * Attempt to read various elements of the Element hierarchy.
	 * @throws IOException
	 */
	private void internalTestElement(JavaCompiler compiler) throws IOException {
		System.clearProperty(ELEMENTPROCNAME);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "model");
		BatchTestUtils.copyResources("targets/model", targetFolder);

		List<String> options = new ArrayList<String>();
		options.add("-A" + ELEMENTPROCNAME);
		BatchTestUtils.compileTree(compiler, options, targetFolder);

		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		assertEquals("succeeded", System.getProperty(ELEMENTPROCNAME));
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(ELEMENTPROCNAME);
		super.tearDown();
	}
	


}
