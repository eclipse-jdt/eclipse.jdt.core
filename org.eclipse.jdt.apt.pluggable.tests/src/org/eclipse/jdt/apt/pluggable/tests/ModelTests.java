/*******************************************************************************
 * Copyright (c) 2008 Walter Harley and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    eclipse@cafewalter.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.pluggable.tests.processors.modeltester.ModelTesterProc;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Basic tests for the typesystem model interfaces in the IDE.
 * Note that most coverage of these interfaces is provided by
 * org.eclipse.jdt.compiler.apt.tests.
 */
public class ModelTests extends TestBase
{

	public ModelTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(ModelTests.class);
	}
	
	/**
	 * Call ModelTesterProc.testFieldType(), which checks the type of a field
	 */
	public void testFieldType() throws Throwable {
		ProcessorTestStatus.reset();
		IJavaProject jproj = createJavaProject(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();
		
		env.addClass(projPath.append("src"), 
				ModelTesterProc.TEST_FIELD_TYPE_PKG,
				ModelTesterProc.TEST_FIELD_TYPE_CLASS,
				ModelTesterProc.TEST_FIELD_TYPE_SOURCE);
		
		AptConfig.setEnabled(jproj, true);
		
		fullBuild();
		expectingNoProblems();
		assertTrue("Processor did not run", ProcessorTestStatus.processorRan());
		assertEquals("Processor reported errors", ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Call ModelTesterProc.testMethodType(), which checks the type of a method
	 */
	public void testMethodType() throws Throwable {
		ProcessorTestStatus.reset();
		IJavaProject jproj = createJavaProject(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();
		
		env.addClass(projPath.append("src"), 
				ModelTesterProc.TEST_METHOD_TYPE_PKG,
				ModelTesterProc.TEST_METHOD_TYPE_CLASS,
				ModelTesterProc.TEST_METHOD_TYPE_SOURCE);
		
		AptConfig.setEnabled(jproj, true);
		
		fullBuild();
		expectingNoProblems();
		assertTrue("Processor did not run", ProcessorTestStatus.processorRan());
		assertEquals("Processor reported errors", ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}	
}
