/*******************************************************************************
 * Copyright (c) 2014 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    het@google.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;
import org.eclipse.jdt.apt.tests.annotations.annotationvalue.CodeExample;
import org.eclipse.jdt.apt.tests.plugin.AptTestsPlugin;
import org.eclipse.jdt.core.IJavaProject;

public class AnnotationValueTests extends APTTestBase {
	public AnnotationValueTests(final String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(AnnotationValueTests.class);
	}

	private IProject setupTest() throws Exception {
		// This should not be necessary, but see
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=99638
		IJavaProject jproj = getCurrentJavaProject();
		jproj.setOption("org.eclipse.jdt.core.compiler.problem.deprecation", "ignore");
		return jproj.getProject();
	}

	private void addTriggerSource() {
		IPath srcRoot = getSourcePath();
		// MyTrigger.java
		env.addClass(srcRoot, CodeExample.PACKAGE_TRIGGER,
				CodeExample.MYTRIGGER_CLASS,
				CodeExample.MYTRIGGER_CODE);

		// Trigger.java
		env.addClass(srcRoot, CodeExample.PACKAGE_TRIGGER,
				CodeExample.TRIGGER_CLASS, CodeExample.TRIGGER_CODE);
	}

	/**
	 * Runs the AnnotationValueProcessor, which contains
	 * the actual tests
	 */
	public void testAnnotationValueProcessor() throws Exception {
		IProject project = setupTest();
		final File jar =
			TestUtil.getFileInPlugin(AptTestsPlugin.getDefault(),
									 new Path("/resources/question.jar")); //$NON-NLS-1$
		final String path = jar.getAbsolutePath();
		env.addExternalJar(project.getFullPath(), path);

		addTriggerSource();

		fullBuild(project.getFullPath());
		expectingNoProblems();

		assertTrue(ProcessorTestStatus.processorRan());
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
}
