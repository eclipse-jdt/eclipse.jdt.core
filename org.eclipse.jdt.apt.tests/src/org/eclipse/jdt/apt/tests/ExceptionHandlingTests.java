/*******************************************************************************
 * Copyright (c) 2006, 2016 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    sbandow@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;

public class ExceptionHandlingTests extends APTTestBase {

	public ExceptionHandlingTests(final String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(ExceptionHandlingTests.class);
	}

	/**
	 * Annotation that expects a primitive but gets its wrapper class should not throw a ClassCastException
	 */
	public void testWrapperClassForPrimitiveValue() throws Exception {
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		IPath testPath = env.addClass(srcRoot, "test", "Test", getCodeForTest("booleanValue = Boolean.valueOf(true)"));

		fullBuild( project.getFullPath() );
		expectingOnlySpecificProblemFor(testPath, new ExpectedProblem("Test", "Type mismatch: cannot convert from Boolean to boolean", testPath));
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Annotation that expects one primitive but gets another should not throw a ClassCastException
	 */
	public void testOtherPrimitiveForBooleanValue() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		IPath testPath = env.addClass(srcRoot, "test", "Test", getCodeForTest("booleanValue = 2"));

		fullBuild( project.getFullPath() );
		expectingOnlySpecificProblemFor(testPath, new ExpectedProblem("Test", "Type mismatch: cannot convert from int to boolean", testPath));
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Annotation that expects a primitive but gets a String should not throw a ClassCastException
	 */
	public void testStringForBooleanValue() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		IPath testPath = env.addClass(srcRoot, "test", "Test", getCodeForTest("booleanValue = \"not a boolean\""));

		fullBuild( project.getFullPath() );
		expectingOnlySpecificProblemFor(testPath, new ExpectedProblem("Test", "Type mismatch: cannot convert from String to boolean", testPath));
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Annotation that expects a primitive but gets an array should not throw a ClassCastException
	 */
	public void testArrayForBooleanValue() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		IPath testPath = env.addClass(srcRoot, "test", "Test", getCodeForTest("booleanValue = {}"));

		fullBuild( project.getFullPath() );
		ExpectedProblem ep = new ExpectedProblem("Test", "Type mismatch: cannot convert from Object[] to boolean", testPath);
		expectingOnlySpecificProblemFor(testPath, ep);
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Annotation that expects a String but gets a primitive should not throw a ClassCastException
	 */
	public void testPrimitiveForStringValue() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		IPath testPath = env.addClass(srcRoot, "test", "Test", getCodeForTest("strValue = true"));

		fullBuild( project.getFullPath() );
		expectingOnlySpecificProblemFor(testPath, new ExpectedProblem("Test", "Type mismatch: cannot convert from boolean to String", testPath));
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Annotation that expects a String but gets another class should not throw a ClassCastException
	 */
	public void testOtherClassForStringValue() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		IPath testPath = env.addClass(srcRoot, "test", "Test", getCodeForTest("strValue = new Object()"));

		fullBuild( project.getFullPath() );
		expectingOnlySpecificProblemFor(testPath, new ExpectedProblem("Test", "Type mismatch: cannot convert from Object to String", testPath));
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Annotation that expects a String but gets an array should not throw a ClassCastException
	 */
	public void testArrayForStringValue() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		IPath testPath = env.addClass(srcRoot, "test", "Test", getCodeForTest("strValue = {}"));

		fullBuild( project.getFullPath() );
		ExpectedProblem ep = new ExpectedProblem("Test", "Type mismatch: cannot convert from Object[] to String", testPath);
		expectingOnlySpecificProblemFor(testPath, ep);
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Annotation that expects an array but gets a primitive should not throw a ClassCastException
	 */
	public void testPrimitiveForArrayValue() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		IPath testPath = env.addClass(srcRoot, "test", "Test", getCodeForTest("arrValue = 'c'"));

		fullBuild( project.getFullPath() );
		expectingOnlySpecificProblemFor(testPath, new ExpectedProblem("Test", "Type mismatch: cannot convert from char to String[]", testPath));
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Annotation that expects an array but gets an object should not throw a ClassCastException
	 */
	public void testNonArrayForArrayValue() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		IPath testPath = env.addClass(srcRoot, "test", "Test", getCodeForTest("arrValue = new Object()"));

		fullBuild( project.getFullPath() );
		expectingOnlySpecificProblemFor(testPath, new ExpectedProblem("Test", "Type mismatch: cannot convert from Object to String[]", testPath));
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Annotation that expects an array of enums but gets an annotation should not throw a ClassCastException
	 */
	public void testAnnotationForEnumArrayValue() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		IPath testPath = env.addClass(srcRoot, "test", "Test", getCodeForTest("enumsValue = @ExceptionHandlingAnnotation()"));

		fullBuild( project.getFullPath() );
		expectingOnlySpecificProblemFor(testPath, new ExpectedProblem("Test", "Type mismatch: cannot convert from ExceptionHandlingAnnotation to ExceptionHandlingAnnotation.EHAEnum[]", testPath));
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Set up the test code for APT exception handling tests
	 * @param annoValue attribute values to pass to ExceptionHandlingAnnotation
	 * @return complete test code
	 */
	private static String getCodeForTest(String annoValue) {
		return "package test;" + "\n" +
			"import org.eclipse.jdt.apt.tests.annotations.exceptionhandling.ExceptionHandlingAnnotation;" + "\n" +
			"@ExceptionHandlingAnnotation(" +
			annoValue +
			")" + "\n" +
			"public class Test" + "\n" +
			"{" + "\n" +
			"}";
	}
}
