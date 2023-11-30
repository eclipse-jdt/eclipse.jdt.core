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
 *    IBM Corporation - fix for 342936
 *    het@google.com  - fix for 441790
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import junit.framework.TestCase;

/**
 * Tests of the type system implementation
 * @since 3.3
 */
public class ModelTests extends TestCase {

	// Processor class names; see corresponding usage in the processor classes.
	private static final String ANNOTATIONMIRRORPROC = "org.eclipse.jdt.compiler.apt.tests.processors.annotationmirror.AnnotationMirrorProc";
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
	 * Attempt to read various elements of the AnnotationMirror hierarchy.
	 */
	public void testAnnotationMirrorWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, ANNOTATIONMIRRORPROC);
	}

	/**
	 * Validate the testElement test against the javac compiler.
	 */
	public void testElementWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest(compiler, ELEMENTPROC);
	}

	/**
	 * Attempt to read various elements of the Element hierarchy.
	 */
	public void testElementWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, ELEMENTPROC);
	}

	/**
	 * Validate the testTypeMirror test against the javac compiler.
	 */
	public void testTypeMirrorWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest(compiler, TYPEMIRRORPROC);
	}

	/**
	 * Attempt to read various elements of the TypeMirror hierarchy.
	 */
	public void testTypeMirrorWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPEMIRRORPROC);
	}

	/**
	 * Validate the generics test against the javac compiler.
	 */
	public void testGenericsWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest(compiler, GENERICSPROC);
	}

	/**
	 * Test handling of generic types.
	 */
	public void testGenericsWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, GENERICSPROC);
	}

	/**
	 * Validate the visitors test against the javac compiler.
	 */
	public void testVisitorsWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler == null) {
			System.out.println("No system java compiler available");
			return;
		}
		internalTest(compiler, VISITORPROC);
	}

	/**
	 * Test the Visitor method implementations.
	 */
	public void testVisitorsWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, VISITORPROC);
	}

	public void testReportedProblemsWithDiagnosticListener() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, ELEMENTPROC,
				"The method staticMethod() from the type targets.jsr199.F is never used locally\n");
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

		List<String> options = new ArrayList<>();
		options.add("-A" + processorClass);
		BatchTestUtils.compileTree(compiler, options, targetFolder);

		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		assertEquals("succeeded", System.getProperty(processorClass));
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void internalTest(JavaCompiler compiler, String processorClass, String errors) throws IOException {
		System.clearProperty(processorClass);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "jsr199");
		BatchTestUtils.copyResources("targets/jsr199", targetFolder);

		List<String> options = new ArrayList<>();
		options.add("-A" + processorClass);
		final StringBuilder reported = new StringBuilder();
		BatchTestUtils.compileTree(compiler, options, targetFolder, new DiagnosticListener () {
			@Override
			public void report(Diagnostic diag) {
				reported.append(diag.getMessage(null)).append("\n");
			}});

		assertEquals(errors, reported.toString());
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

}
