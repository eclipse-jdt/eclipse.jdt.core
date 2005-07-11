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
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.internal.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.JarFactoryContainer;
import org.eclipse.jdt.apt.core.util.AptConfig;
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
}
	
	public static String getProjectName() {
		return FactoryLoaderTests.class.getName() + "Project";
	}

	public IPath getSourcePath() {
		IProject project = env.getProject( getProjectName() );
		IFolder srcFolder = project.getFolder( "src" );
		IPath srcRoot = srcFolder.getFullPath();
		return srcRoot;
	}
	
	public void testExternalJarLoader() throws Exception {
		LoaderTestAnnotationProcessor.clearLoaded();
		IProject project = env.getProject( getProjectName() );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertFalse(LoaderTestAnnotationProcessor.isLoaded());
		
		// add _extJar to the factory list and rebuild.
		IJavaProject jproj = env.getJavaProject( getProjectName() );
		FactoryContainer jarContainer = new JarFactoryContainer(_extJar);
		Map<FactoryContainer, Boolean> containers = new LinkedHashMap<FactoryContainer, Boolean>(1);
		containers.put(jarContainer, true);
		AptConfig.addContainers(jproj, containers);
		
		// rebuild and verify that the processor was loaded
		LoaderTestAnnotationProcessor.clearLoaded();
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertTrue(LoaderTestAnnotationProcessor.isLoaded());
		
		// remove _extJar from the factory list.
		AptConfig.removeContainer(jproj, jarContainer);
		
		// rebuild and verify that the processor was not loaded.
		LoaderTestAnnotationProcessor.clearLoaded();
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertFalse(LoaderTestAnnotationProcessor.isLoaded());
	}
	

}
