/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   wharley - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import java.io.File;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.IFactoryPath;
import org.eclipse.jdt.apt.tests.external.annotations.classloader.ColorAnnotationProcessor;
import org.eclipse.jdt.apt.tests.external.annotations.classloader.ColorTestCodeExample;
import org.eclipse.jdt.apt.tests.external.annotations.loadertest.LoaderTestAnnotationProcessor;
import org.eclipse.jdt.apt.tests.external.annotations.loadertest.LoaderTestCodeExample;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class FactoryLoaderTests extends APTTestBase {

	private File _extJar; // external annotation jar
	private IPath _extVarJar; // external annotation jar, as a classpath-var-relative path
	private IPath _projectPath; // initialized in setUp(), cleared in tearDown()

	private final static String TEMPJARDIR_CPVAR = "FACTORYLOADERTEST_TEMP"; //$NON-NLS-1$

	public FactoryLoaderTests(String name)
	{
		super( name );
	}

	public static Test suite() {
		return new TestSuite( FactoryLoaderTests.class );
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();

		_projectPath = env.getProject( getProjectName() ).getFullPath();
		_extJar = TestUtil.createAndAddExternalAnnotationJar(
				env.getJavaProject( _projectPath ));

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

	// Test what happens when the factory path contains a jar file that can't be found.
	public void testNonexistentEntry() throws Exception {
		LoaderTestAnnotationProcessor.clearLoaded();
		IProject project = env.getProject( getProjectName() );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertFalse(LoaderTestAnnotationProcessor.isLoaded());

		IJavaProject jproj = env.getJavaProject( getProjectName() );
		IFactoryPath ifp = AptConfig.getFactoryPath(jproj);

		// add bogus entry to factory list, and rebuild.
		File bogusJar = new File("bogusJar.jar"); // assumed to not exist
		ifp.addExternalJar(bogusJar);

		// verify that a problem marker was added.
		AptConfig.setFactoryPath(jproj, ifp);
		fullBuild( project.getFullPath() );
		IMarker[] markers = getAllAPTMarkers(_projectPath);
		assertEquals(1, markers.length);
		assertEquals(AptPlugin.APT_LOADER_PROBLEM_MARKER, markers[0].getType());
		String message = markers[0].getAttribute(IMarker.MESSAGE, "");
		assertTrue(message.contains("bogusJar.jar"));

		// remove bogus entry, add _extJar to the factory list as an external jar, and rebuild.
		ifp.removeExternalJar(bogusJar);
		ifp.addExternalJar(_extJar);
		AptConfig.setFactoryPath(jproj, ifp);

		// rebuild and verify that the processor was loaded and the problems were removed.
		LoaderTestAnnotationProcessor.clearLoaded();
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertTrue(LoaderTestAnnotationProcessor.isLoaded());

		// Verify that we were able to run the ColorAnnotationProcessor successfully
		assertTrue(ColorAnnotationProcessor.wasSuccessful());

		// restore to the original
		AptConfig.setFactoryPath(jproj, ifp);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.builder.Tests#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		JavaCore.removeClasspathVariable( TEMPJARDIR_CPVAR, null );
		_extJar = null;
		_extVarJar = null;
		_projectPath = null;
		super.tearDown();
	}


}
