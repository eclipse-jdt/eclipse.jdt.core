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
 * Tests of the type system implementation
 * @since 3.3
 */
public class ModelTests extends TestCase {
	
	// Processor class names; see corresponding usage in the processor classes.
	private static final String ELEMENTPROC = "org.eclipse.jdt.compiler.apt.tests.processors.elements.ElementProc";
	private static final String GENERICSPROC = "org.eclipse.jdt.compiler.apt.tests.processors.generics.GenericsProc";
	private static final String TYPEMIRRORPROC = "org.eclipse.jdt.compiler.apt.tests.processors.typemirror.TypeMirrorProc";
	private static final String VISITORPROC = "org.eclipse.jdt.compiler.apt.tests.processors.visitors.VisitorProc";

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
		internalTest(compiler, ELEMENTPROC);
	}

	/**
	 * Attempt to read various elements of the Element hierarchy.
	 * @throws IOException 
	 */
	public void testElementWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, ELEMENTPROC);
	}

	/**
	 * Validate the testTypeMirror test against the javac compiler.
	 * @throws IOException 
	 */
	public void testTypeMirrorWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPEMIRRORPROC);
	}

	/**
	 * Attempt to read various elements of the TypeMirror hierarchy.
	 * @throws IOException 
	 */
	public void testTypeMirrorWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPEMIRRORPROC);
	}

	/**
	 * Validate the generics test against the javac compiler.
	 * @throws IOException 
	 */
	public void testGenericsWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, GENERICSPROC);
	}

	/**
	 * Test handling of generic types.
	 * @throws IOException 
	 */
	public void testGenericsWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, GENERICSPROC);
	}

	/**
	 * Validate the visitors test against the javac compiler.
	 * @throws IOException 
	 */
	public void testVisitorsWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, VISITORPROC);
	}

	/**
	 * Test the Visitor method implementations.
	 * @throws IOException 
	 */
	public void testVisitorsWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, VISITORPROC);
	}

	/**
	 * Test functionality by running a particular processor against the types in
	 * resources/targets.  The processor must support "*" (the set of all annotations) 
	 * and must report its errors or success via the methods in BaseProcessor.
	 * @throws IOException
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

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
