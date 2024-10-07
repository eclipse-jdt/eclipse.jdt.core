/*******************************************************************************
 * Copyright (c) 2005, 2008 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests;

import java.io.File;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;
import org.eclipse.jdt.apt.tests.annotations.readannotation.CodeExample;
import org.eclipse.jdt.apt.tests.plugin.AptTestsPlugin;
import org.eclipse.jdt.core.IJavaProject;

public class ReadAnnotationTests extends APTTestBase
{
	public ReadAnnotationTests(final String name) {
		super( name );
	}

	public static Test suite() {
		return new TestSuite( ReadAnnotationTests.class );
	}

	private void addAllSources()
	{
		addQuestionSources();
		addTriggerSource();
		addNoTypeSources();
	}

	private void addQuestionSources()
	{
		IPath srcRoot = getSourcePath();
		// SimpleAnnotation.java
		env.addClass(
				srcRoot,
				CodeExample.PACKAGE_QUESTION,
				CodeExample.SIMPLE_ANNOTATION_CLASS,
				CodeExample.SIMPLE_ANNOTATION_CODE );

		// RTVisibleAnnotation.java
		env.addClass(
				srcRoot,
				CodeExample.PACKAGE_QUESTION,
				CodeExample.RTVISIBLE_CLASS,
				CodeExample.RTVISIBLE_ANNOTATION_CODE);

		// RTInvisibleAnnotation.java
		env.addClass(
				srcRoot,
				CodeExample.PACKAGE_QUESTION,
				CodeExample.RTINVISIBLE_CLASS,
				CodeExample.RTINVISIBLE_ANNOTATION_CODE);

		// package-info.java
		env.addClass(
				srcRoot,
				CodeExample.PACKAGE_QUESTION,
				CodeExample.PACKAGE_INFO_CLASS,
				CodeExample.PACKAGE_INFO_CODE);

		// Color.java
		env.addClass(
				srcRoot,
				CodeExample.PACKAGE_QUESTION,
				CodeExample.COLOR_CLASS,
				CodeExample.COLOR_CODE);

		// AnnotationTest.java
		env.addClass(
				srcRoot,
				CodeExample.PACKAGE_QUESTION,
				CodeExample.ANNOTATION_TEST_CLASS,
				CodeExample.ANNOTATION_TEST_CODE);
	}

	private void addNoTypeSources()
	{
		IPath srcRoot = getSourcePath();
		// package-info.java
		env.addClass(srcRoot,
				CodeExample.PACKAGE_NOTYPES,
				CodeExample.PACKAGE_INFO_NOTYPES_CLASS,
				CodeExample.PACKAGE_INFO_NOTYPES_CODE);
	}

	private void addTriggerSource()
	{
		IPath srcRoot = getSourcePath();
		// MyMarkerAnnotation.java
		env.addClass(srcRoot,
				CodeExample.PACKAGE_TRIGGER,
				CodeExample.MYMARKERANNOTATION_CLASS,
				CodeExample.MYMARKERANNOTATION_CODE);

		// Trigger.java
		env.addClass(srcRoot,
				CodeExample.PACKAGE_TRIGGER,
				CodeExample.TRIGGER_CLASS,
				CodeExample.TRIGGER_CODE);
	}

	private IProject setupTest() throws Exception
	{
		// This should not be necessary, but see https://bugs.eclipse.org/bugs/show_bug.cgi?id=99638
		IJavaProject jproj = getCurrentJavaProject();
		jproj.setOption("org.eclipse.jdt.core.compiler.problem.deprecation", "ignore");
		return jproj.getProject();
	}

	/**
	 * Set up all the source files for testing.
	 * Runs the ReadAnnotationProcessor, which contains
	 * the actual testing.
	 */

	public void test0() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();
		addAllSources();
		fullBuild( project.getFullPath() );
		expectingNoProblems();

		assertTrue(ProcessorTestStatus.processorRan());
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Set up the jar file for testing.
	 * Runs the ReadAnnotationProcessor, which contains
	 * the actual testing.
	 */
	public void test1() throws Exception
	{
		IProject project = setupTest();
		final File jar =
			TestUtil.getFileInPlugin(AptTestsPlugin.getDefault(),
									 new Path("/resources/question.jar")); //$NON-NLS-1$
		final String path = jar.getAbsolutePath();
		env.addExternalJar(project.getFullPath(), path);

		addTriggerSource();

		fullBuild( project.getFullPath() );
		expectingNoProblems();

		assertTrue(ProcessorTestStatus.processorRan());
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
}
