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

import javax.annotation.processing.Processor;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import org.eclipse.jdt.compiler.apt.tests.processors.elements.ElementProc;
import org.eclipse.jdt.compiler.apt.tests.processors.generics.GenericsProc;
import org.eclipse.jdt.compiler.apt.tests.processors.visitors.VisitorProc;

import junit.framework.TestCase;

/**
 * Tests of the type system implementation
 * @since 3.3
 */
public class ModelTests extends TestCase {
	
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
		internalTest(compiler, ElementProc.class);
	}

	/**
	 * Attempt to read various elements of the Element hierarchy.
	 * @throws IOException 
	 */
	public void testElementWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, ElementProc.class);
	}

	/**
	 * Validate the generics test against the javac compiler.
	 * @throws IOException 
	 */
	public void testGenericsWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, GenericsProc.class);
	}

	/**
	 * Test handling of generic types.
	 * @throws IOException 
	 */
	public void testGenericsWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, GenericsProc.class);
	}

	/**
	 * Validate the visitors test against the javac compiler.
	 * @throws IOException 
	 */
	public void testVisitorsWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, VisitorProc.class);
	}

	/**
	 * Test the Visitor method implementations.
	 * @throws IOException 
	 */
	public void testVisitorsWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, VisitorProc.class);
	}

	/**
	 * Test functionality by running a particular processor against the types in
	 * resources/targets.  The processor must support "*" (the set of all annotations) 
	 * and must report its errors or success via the methods in BaseProcessor.
	 * @throws IOException
	 */
	private void internalTest(JavaCompiler compiler, Class<? extends Processor> processor) throws IOException {
		System.clearProperty(processor.getName());
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "model");
		BatchTestUtils.copyResources("targets/model", targetFolder);

		List<String> options = new ArrayList<String>();
		options.add("-A" + processor.getName());
		BatchTestUtils.compileTree(compiler, options, targetFolder);

		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		assertEquals("succeeded", System.getProperty(processor.getName()));
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
