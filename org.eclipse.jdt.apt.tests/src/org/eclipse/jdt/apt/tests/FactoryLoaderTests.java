/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   wharley - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.IFactoryPath;
import org.eclipse.jdt.apt.tests.external.annotations.classloader.ColorAnnotationProcessor;
import org.eclipse.jdt.apt.tests.external.annotations.classloader.ColorTestCodeExample;
import org.eclipse.jdt.apt.tests.external.annotations.loadertest.LoaderTestAnnotationProcessor;
import org.eclipse.jdt.apt.tests.external.annotations.loadertest.LoaderTestCodeExample;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.tests.builder.Tests;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * 
 */
public class FactoryLoaderTests extends Tests {
	
	private File _extJar = null; // external annotation jar
	
	public FactoryLoaderTests(String name)
	{
		super( name );
	}

	public static Test suite() {
		return new TestSuite( FactoryLoaderTests.class );
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
		
		_extJar = TestUtil.createAndAddExternalAnnotationJar(
				env.getJavaProject( projectPath ));

		IPath srcRoot = getSourcePath();
		String code = LoaderTestCodeExample.CODE;
		env.addClass(srcRoot, LoaderTestCodeExample.CODE_PACKAGE, LoaderTestCodeExample.CODE_CLASS_NAME, code);
		
		code = ColorTestCodeExample.CODE;
		env.addClass(srcRoot, ColorTestCodeExample.CODE_PACKAGE, ColorTestCodeExample.CODE_CLASS_NAME, code);
}
	
	public static String getProjectName() {
		return FactoryLoaderTests.class.getName() + "Project"; //$NON-NLS-1$
	}

	public IPath getSourcePath() {
		IProject project = env.getProject( getProjectName() );
		IFolder srcFolder = project.getFolder( "src" ); //$NON-NLS-1$
		IPath srcRoot = srcFolder.getFullPath();
		return srcRoot;
	}
	
	public void testExternalJarLoader() throws Exception {
		LoaderTestAnnotationProcessor.clearLoaded();
		IProject project = env.getProject( getProjectName() );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertFalse(LoaderTestAnnotationProcessor.isLoaded());
		
		IJavaProject jproj = env.getJavaProject( getProjectName() );
		IFactoryPath ifp = AptConfig.getFactoryPath(jproj);
		
		// add _extJar to the factory list and rebuild.
		ifp.addExternalJar(_extJar);
		AptConfig.setFactoryPath(jproj, ifp);
		
		// rebuild and verify that the processor was loaded
		LoaderTestAnnotationProcessor.clearLoaded();
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertTrue(LoaderTestAnnotationProcessor.isLoaded());
		
		// Verify that we were able to run the ColorAnnotationProcessor successfully
		assertTrue(ColorAnnotationProcessor.wasSuccessful());
		
		// restore to the original
		ifp.removeExternalJar(_extJar);
		AptConfig.setFactoryPath(jproj, ifp);
		
		// rebuild and verify that the processor was not loaded.
		LoaderTestAnnotationProcessor.clearLoaded();
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertFalse(LoaderTestAnnotationProcessor.isLoaded());
		
		// This file will be locked until GC takes care of unloading
		// the annotation processor classes.
		_extJar.deleteOnExit();
		_extJar = null;
	}
	

}
