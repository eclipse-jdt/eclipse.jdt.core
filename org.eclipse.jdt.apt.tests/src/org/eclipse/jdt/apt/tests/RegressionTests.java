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
import org.eclipse.jdt.core.tests.util.Util;

/**
 * 
 */
public class RegressionTests extends APTTestBase {

	public RegressionTests(String name) {
		super(name);
	}

	public static Test suite()
	{
		return new TestSuite( RegressionTests.class );
	}

	public void setUp() throws Exception
	{
		super.setUp();
	}
	
	/**
	 * Bugzilla 104032: NPE when deleting project that has APT settings.
	 */
	@SuppressWarnings("nls")
	public void testBugzilla104032() throws Exception
	{
		// set up project with unique name
		final String projName = RegressionTests.class.getName() + "104032.Project"; //$NON-NLS-1$
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
		AptConfig.setEnabled(jproj, true);
		
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		// Now delete the project!
		ResourcesPlugin.getWorkspace().delete(new IResource[] { project }, true, null);

	}
    
	/**
	 * Tests annotation proxies
	 */
    public void testBugzilla106541() throws Exception
    {
        final String projName = RegressionTests.class.getName() + "104032.Project"; //$NON-NLS-1$
        IPath projectPath = env.addProject( projName, "1.5" ); //$NON-NLS-1$
        env.addExternalJars( projectPath, Util.getJavaClassLibs() );

        env.removePackageFragmentRoot( projectPath, "" ); //$NON-NLS-1$
        env.addPackageFragmentRoot( projectPath, "src" ); //$NON-NLS-1$
        env.setOutputFolder( projectPath, "bin" ); //$NON-NLS-1$

        IJavaProject javaProject = env.getJavaProject( projectPath ) ;
		AptConfig.setEnabled(javaProject, true);
        TestUtil.createAndAddAnnotationJar(javaProject);
        IProject project = env.getProject( projName );
        IFolder srcFolder = project.getFolder( "src" );
        IPath srcRoot = srcFolder.getFullPath();

        String code = "package p1; " + "\n"
        + "import org.eclipse.jdt.apt.tests.annotations.readAnnotationType.SimpleAnnotation;" + "\n"
        + "@SimpleAnnotation(SimpleAnnotation.Name.HELLO)" + "\n"
        + "public class MyClass { \n"
        + " public test.HELLOGen _gen;"
        + " }";
        
        env.addClass( srcRoot, "p1", "MyClass", code );
        
        fullBuild( project.getFullPath() );
        expectingNoProblems();
        
        // Now delete the project!
        ResourcesPlugin.getWorkspace().delete(new IResource[] { project }, true, null);

    }
    
    // doesn't work because of a jdt.core type system universe problem.
    public void DISABLED_testBugzilla120255() throws Exception{
    	final String projName = RegressionTests.class.getName() + "120255.Project"; //$NON-NLS-1$
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

		String a1Code = "package pkg; " + "\n"
			+ "import org.eclipse.jdt.apt.tests.annotations.apitest.Common;\n" 
			+ "import java.util.*;\n\n"
			+ "@Commmon\n"
			+ "public class A1<T> {\n "
			+ "    @Common\n" 
			+ "    Collection<String> collectionOfString;\n\n" 
			+ "    @Common\n"
			+ "    Collection<List> collectionOfList;\n"
			+ "    public static class inner{}"
			+ "}";
	
		final IPath a1Path = env.addClass( srcRoot, "pkg", "A1", a1Code ); //$NON-NLS-1$ //$NON-NLS-2$
		
		// Set some per-project preferences
		IJavaProject jproj = env.getJavaProject( projName );
		AptConfig.setEnabled(jproj, true);
		fullBuild( project.getFullPath() );				
		expectingSpecificProblemsFor(a1Path, new ExpectedProblem[]{
				new ExpectedProblem("", "java.util.List is assignable to java.util.Collection", a1Path),
				new ExpectedProblem("", "java.lang.String is not assignable to java.util.Collection", a1Path)
				}
		);
    }
	
}
