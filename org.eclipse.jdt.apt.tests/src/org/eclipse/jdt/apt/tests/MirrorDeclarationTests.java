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

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.apt.tests.annotations.mirrortest.MirrorDeclarationTestAnnotationProcessor;
import org.eclipse.jdt.apt.tests.annotations.mirrortest.MirrorDeclarationCodeExample;
import org.eclipse.jdt.core.tests.builder.Tests;
import org.eclipse.jdt.core.tests.util.Util;

public class MirrorDeclarationTests extends Tests {

	public MirrorDeclarationTests(final String name)
	{
		super(name);
	}

	public static Test suite()
	{
		return new TestSuite(MirrorDeclarationTests.class);
	}
	
	public void setUp() throws Exception
	{
		super.setUp();

		env.resetWorkspace();

		// project will be deleted by super-class's tearDown() method
		IPath projectPath = env.addProject( getProjectName(), "1.5" ); //$NON-NLS-1$
		env.addExternalJars( projectPath, Util.getJavaClassLibs() );
		fullBuild( projectPath );

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot( projectPath, "" ); //$NON-NLS-1$
		env.addPackageFragmentRoot( projectPath, "src" ); //$NON-NLS-1$
		env.setOutputFolder( projectPath, "bin" ); //$NON-NLS-1$

		TestUtil.createAndAddAnnotationJar( env.getJavaProject( projectPath ) );
		
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		String code = MirrorDeclarationCodeExample.CODE;
		env.addClass(srcRoot, MirrorDeclarationCodeExample.CODE_PACKAGE, MirrorDeclarationCodeExample.CODE_CLASS_NAME, code);
		fullBuild( project.getFullPath() );
		expectingNoProblems();
	}
	
	public static String getProjectName()
	{
		return MirrorDeclarationTests.class.getName() + "Project"; //$NON-NLS-1$
	}

	public IPath getSourcePath()
	{
		IProject project = env.getProject( getProjectName() );
		IFolder srcFolder = project.getFolder( "src" ); //$NON-NLS-1$
		IPath srcRoot = srcFolder.getFullPath();
		return srcRoot;
	}
	
	public void testMirrorDeclaration() throws Exception
	{
		//tests are embedded in the AnnotationProcessor
		assertEquals(MirrorDeclarationTestAnnotationProcessor.NO_ERRORS, MirrorDeclarationTestAnnotationProcessor.ERROR);
	}
}
