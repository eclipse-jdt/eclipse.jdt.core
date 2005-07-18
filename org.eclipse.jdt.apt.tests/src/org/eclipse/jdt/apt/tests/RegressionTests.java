/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.tests.builder.Tests;
import org.eclipse.jdt.core.tests.util.Util;

/**
 * 
 */
public class RegressionTests extends Tests {

	public RegressionTests(String name) {
		super(name);
	}

	public static Test suite()
	{
		return new TestSuite( RegressionTests.class );
	}

	public static String getProjectName()
	{
		return RegressionTests.class.getName() + "Project";
	}

	public void setUp() throws Exception
	{
		super.setUp();
	}
	
	/**
	 * Bugzilla 104032: NPE when deleting project that has APT settings.
	 */
	public void testBugzilla104032() throws Exception
	{
		// set up project with unique name
		final String projName = RegressionTests.class.getName() + "104032.Project";
		IPath projectPath = env.addProject( projName, "1.5" ); //$NON-NLS-1$
		env.addExternalJars( projectPath, Util.getJavaClassLibs() );

		env.removePackageFragmentRoot( projectPath, "" ); //$NON-NLS-1$
		env.addPackageFragmentRoot( projectPath, "src" ); //$NON-NLS-1$
		env.setOutputFolder( projectPath, "bin" ); //$NON-NLS-1$

		TestUtil.createAndAddAnnotationJar( env
			.getJavaProject( projectPath ) );
		IProject project = env.getProject( projName );
		IFolder srcFolder = project.getFolder( "src" );
		IPath srcRoot = srcFolder.getFullPath();

		String a1Code = "package p1; " + "\n"
			+ "import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;" + "\n"
			+ "@HelloWorldAnnotation" + "\n"
			+ "public class A1 {}";
		String a2Code = "package p1; " + "\n"
			+ "import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;" + "\n"
			+ "@HelloWorldAnnotation" + "\n"
			+ "public class A2 {}";
		String bCode = "package p1; " + "\n"
			+ "public class B { generatedfilepackage.GeneratedFileTest gft; }";
		env.addClass( srcRoot, "p1", "A1", a1Code ); //$NON-NLS-1$ //$NON-NLS-2$
		env.addClass( srcRoot, "p1", "A2", a2Code ); //$NON-NLS-1$ //$NON-NLS-2$
		env.addClass( srcRoot, "p1", "B", bCode ); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Set some per-project preferences
		IJavaProject jproj = env.getJavaProject( projName );
		AptConfig.addProcessorOption(jproj, "test.104032.a", "foo");
		
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		// Now delete the project!
		ResourcesPlugin.getWorkspace().delete(new IResource[] { project }, true, null);

	}
	
}
