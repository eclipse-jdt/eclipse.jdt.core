/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
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

public class Java8ElementsTests extends TestCase {
	
	private static final String TYPE_ANNOTATION_PROC = "org.eclipse.jdt.compiler.apt.tests.processors.elements.Java8ElementProcessor";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	public void testSE8Specifics() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testSE8Specifics");
	}
	public void testSE8SpecificsWithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testSE8Specifics");
	}
	public void testLambdaSpecifics() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testLambdaSpecifics");
	}
	public void testLambdaSpecificsWithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testLambdaSpecifics");
	}
	public void testTypeAnnotations() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations");
	}
	public void testTypeAnnotationsWithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations");
	}
	public void testTypeAnnotations1() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations1");
	}
	public void testTypeAnnotations1WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations1");
	}
	public void testTypeAnnotations2() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations2");
	}
	public void testTypeAnnotations2WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations2");
	}
	public void testTypeAnnotations3() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations3");
	}
	public void testTypeAnnotations3WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations3");
	}
	public void testTypeAnnotations4() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations4");
	}
	public void testTypeAnnotations4WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations4");
	}
	public void testTypeAnnotations5() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations5");
	}
	public void testTypeAnnotations5WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations5");
	}
	public void testTypeAnnotations6() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations6");
	}
	public void _testTypeAnnotations6WithJavac() throws Exception {   // Disabled for now. Javac 8b108 drops annotations arrays preceding varargs.
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations6");
	}
	public void testTypeAnnotations7() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations7");
	}
	public void _testTypeAnnotations7WithJavac() throws Exception {  // Disabled for now. Javac 8b108 misattributes annotations on type parameters 
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations7");
	}
	public void testTypeAnnotations8() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations8");
	}
	public void _testTypeAnnotations8WithJavac() throws Exception {     // Disabled for now. Javac 8b108 misattributes annotations on type parameters
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations8");
	}
	public void testTypeAnnotations9() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations9");
	}
	public void testTypeAnnotations9WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations9");
	}
	public void testTypeAnnotations10() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations10");
	}
	public void testTypeAnnotations10WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations10");
	}
	public void testTypeAnnotations11() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations11");
	}
	public void testTypeAnnotations11WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations11");
	}
	public void testTypeAnnotations12() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations12");
	}
	public void testTypeAnnotations12WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations12");
	}
	public void testTypeAnnotations13() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations13");
	}
	public void testTypeAnnotations13WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations13");
	}
	public void testTypeAnnotations14() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations14");
	}
	public void _testTypeAnnotations14WithJavac() throws Exception { // Disabled for now. Javac returns null as receiver type where it should be type 'None'
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations14");
	}
	public void testTypeAnnotations15() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations15", "Z1.java");
	}
	public void testTypeAnnotations15WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations15", "Z1.java");
	}
	public void testTypeAnnotations16() throws Exception {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations16", "Z2.java");
	}
	public void testTypeAnnotations16WithJavac() throws Exception {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTest(compiler, TYPE_ANNOTATION_PROC, "testTypeAnnotations16", "Z2.java");
	}

	private void internalTest(JavaCompiler compiler, String processor, String testMethod) throws IOException {
		internalTest(compiler, processor, testMethod, null);
	}
	private void internalTest(JavaCompiler compiler, String processor, String testMethod, String testClass) throws IOException {
		if (!canRunJava8()) {
			return;
		}
		System.clearProperty(processor);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "model8");
		if (testClass == null || testClass.equals("")) {
			BatchTestUtils.copyResources("targets/model8", targetFolder);
		} else {
			BatchTestUtils.copyResource("targets/model8/" + testClass, targetFolder);
		}
		

		List<String> options = new ArrayList<String>();
		options.add("-A" + processor);
		options.add("-A" + testMethod);
		// Javac 1.8 doesn't (yet?) support the -1.8 option
		if (compiler instanceof EclipseCompiler) {
			options.add("-1.8");
		}
		BatchTestUtils.compileTree(compiler, options, targetFolder);

		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		assertEquals("succeeded", System.getProperty(processor));
	}
	public boolean canRunJava8() {
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
