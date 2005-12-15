/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;
import org.eclipse.jdt.apt.tests.annotations.mirrortest.MirrorUtilTestAnnotationProcessor;
import org.eclipse.jdt.apt.tests.annotations.mirrortest.MirrorUtilTestCodeExample;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.tests.builder.Problem;

public class MirrorUtilTests extends APTTestBase {

	public MirrorUtilTests(final String name)
	{
		super(name);
	}

	public static Test suite()
	{
		return new TestSuite(MirrorUtilTests.class);
	}
	
	public void setUp() throws Exception
	{	
		super.setUp();		
		
		IProject project = env.getProject( getProjectName() );
		IJavaProject jproj = env.getJavaProject(project.getFullPath());
		addEnvOptions(jproj);
		IPath srcRoot = getSourcePath();
		String code = MirrorUtilTestCodeExample.CODE;
		env.addClass(srcRoot, MirrorUtilTestCodeExample.CODE_PACKAGE, MirrorUtilTestCodeExample.CODE_CLASS_NAME, code);
		fullBuild( project.getFullPath() );
		assertNoUnexpectedProblems();
	}
	
	/**
	 * Add options which the AnnotationProcessorEnvironment should see.
	 * The options will be verified within the processor code.
	 */
	private void addEnvOptions(IJavaProject jproj) {
		for (int i = 0; i < MirrorUtilTestAnnotationProcessor.ENV_KEYS.length; ++i) {
			AptConfig.addProcessorOption(jproj, 
					MirrorUtilTestAnnotationProcessor.ENV_KEYS[i], 
					MirrorUtilTestAnnotationProcessor.ENV_VALUES[i]);
		}
	}

	/**
	 * 
	 */
	private void assertNoUnexpectedProblems() {
		Problem[] problems = env.getProblems();
		for (Problem problem : problems) {
			if (problem.getMessage().startsWith("The field DeclarationsTestClass")) { //$NON-NLS-1$
				continue;
			}
			fail("Found unexpected problem: " + problem); //$NON-NLS-1$
		}
	}
	
	public void testMirrorUtils() throws Exception
	{
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
}
