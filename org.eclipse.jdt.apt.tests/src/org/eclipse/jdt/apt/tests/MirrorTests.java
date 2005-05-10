/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.tests.annotations.mirrortest.CodeExample;
import org.eclipse.jdt.apt.tests.annotations.mirrortest.MirrorTestAnnotationProcessor;
import org.eclipse.jdt.core.tests.builder.Tests;
import org.eclipse.jdt.core.tests.util.Util;

public class MirrorTests extends Tests {
	
	public MirrorTests(final String name) {
		super( name );
	}

	public static Test suite() {
		return new TestSuite( MirrorTests.class );
	}

	public void setUp() throws Exception {
		super.setUp();
		
		// project will be deleted by super-class's tearDown() method
		IPath projectPath = env.addProject( getProjectName(), "1.5" ); //$NON-NLS-1$
		env.addExternalJars( projectPath, Util.getJavaClassLibs() );
		fullBuild( projectPath );

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot( projectPath, "" ); //$NON-NLS-1$

		env.addPackageFragmentRoot( projectPath, "src" ); //$NON-NLS-1$
		env.setOutputFolder( projectPath, "bin" ); //$NON-NLS-1$

		TestUtil.createAndAddAnnotationJar( env
			.getJavaProject( projectPath ) );
	}
	
	public static String getProjectName() {
		return MirrorTests.class.getName() + "Project";
	}

	public IPath getSourcePath() {
		IProject project = env.getProject( getProjectName() );
		IFolder srcFolder = project.getFolder( "src" );
		IPath srcRoot = srcFolder.getFullPath();
		return srcRoot;
	}
	
	/**
	 * Runs the MirrorTestAnnotationProcessor, which contains
	 * the actual tests
	 */
	public void testMirror() throws Exception {
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		
		String code = CodeExample.CODE;

		env.addClass( 
				srcRoot, 
				CodeExample.CODE_PACKAGE, 
				CodeExample.CODE_CLASS_NAME,
				code );

		fullBuild( project.getFullPath() );

		expectingNoProblems();
		
		assertEquals(MirrorTestAnnotationProcessor.NO_ERRORS, 
					 MirrorTestAnnotationProcessor.ERROR);
	}
	
}
