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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.IFactoryPath;
import org.eclipse.jdt.apt.tests.external.annotations.classloader.ColorAnnotationProcessor;
import org.eclipse.jdt.apt.tests.external.annotations.classloader.ColorTestCodeExample;
import org.eclipse.jdt.apt.tests.external.annotations.loadertest.LoaderTestAnnotationProcessor;
import org.eclipse.jdt.apt.tests.external.annotations.loadertest.LoaderTestCodeExample;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

/**
 * 
 */
public class FactoryLoaderTests extends APTTestBase {
	
	private File _extJar; // external annotation jar
	private IPath _extVarJar; // external annotation jar, as a classpath-var-relative path
	
	private final static String TEMPJARDIR_CPVAR = "FACTORYLOADERTEST_TEMP"; //$NON-NLS-1$
	
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
		IPath projectPath = env.getProject( getProjectName() ).getFullPath(); //$NON-NLS-1$
		
		_extJar = TestUtil.createAndAddExternalAnnotationJar(
				env.getJavaProject( projectPath ));
		
		// Create a classpath variable for the same jar file, so we can
		// refer to it that way.
		File canonicalJar = _extJar.getCanonicalFile();
		IPath jarDir = new Path( canonicalJar.getParent() );
		String extJarName = canonicalJar.getName();
		IPath varPath = new Path( TEMPJARDIR_CPVAR );
		_extVarJar = varPath.append( extJarName );
		JavaCore.setClasspathVariable( TEMPJARDIR_CPVAR, jarDir, null );

		IPath srcRoot = getSourcePath();
		String code = LoaderTestCodeExample.CODE;
		env.addClass(srcRoot, LoaderTestCodeExample.CODE_PACKAGE, LoaderTestCodeExample.CODE_CLASS_NAME, code);
		
		code = ColorTestCodeExample.CODE;
		env.addClass(srcRoot, ColorTestCodeExample.CODE_PACKAGE, ColorTestCodeExample.CODE_CLASS_NAME, code);
	}

	public void testExternalJarLoader() throws Exception {
		LoaderTestAnnotationProcessor.clearLoaded();
		IProject project = env.getProject( getProjectName() );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertFalse(LoaderTestAnnotationProcessor.isLoaded());
		
		IJavaProject jproj = env.getJavaProject( getProjectName() );
		IFactoryPath ifp = AptConfig.getFactoryPath(jproj);
		
		// add _extJar to the factory list as an external jar, and rebuild.
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
		
		// add _extJar to the factory list as a class-path-relative jar, and rebuild.
		ifp.addVarJar(_extVarJar);
		AptConfig.setFactoryPath(jproj, ifp);
		
		// rebuild and verify that the processor was loaded
		LoaderTestAnnotationProcessor.clearLoaded();
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertTrue(LoaderTestAnnotationProcessor.isLoaded());
		
		// restore to the original
		ifp.removeVarJar(_extVarJar);
		AptConfig.setFactoryPath(jproj, ifp);
		
		// rebuild and verify that the processor was not loaded.
		LoaderTestAnnotationProcessor.clearLoaded();
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertFalse(LoaderTestAnnotationProcessor.isLoaded());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.builder.Tests#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		JavaCore.removeClasspathVariable( TEMPJARDIR_CPVAR, null );
		_extJar = null;
		_extVarJar = null;
		super.tearDown();
	}
	

}
