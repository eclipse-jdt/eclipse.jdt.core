/*******************************************************************************
 * Copyright (c) 2005, 2014 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   wharley@bea.com - initial API and implementation
 *   het@google.com - Bug 423254 - There is no way to tell if a project's factory path is different from the workspace default
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.IFactoryPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.tests.util.Util;

public class RegressionTests extends APTTestBase {

	public RegressionTests(String name) {
		super(name);
	}

	public static Test suite()
	{
		return new TestSuite( RegressionTests.class );
	}

	@Override
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

		String a1Code = """
			package p1; \
			
			import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;\
			
			@HelloWorldAnnotation\
			
			public class A1 {}""";
		String a2Code = """
			package p1; \
			
			import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;\
			
			@HelloWorldAnnotation\
			
			public class A2 {}""";
		String bCode = """
			package p1; \
			
			public class B { generatedfilepackage.GeneratedFileTest gft; }""";
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

        String code = """
			package p1; \
			
			import org.eclipse.jdt.apt.tests.annotations.readAnnotationType.SimpleAnnotation;\
			
			@SimpleAnnotation(SimpleAnnotation.Name.HELLO)\
			
			public class MyClass {\s
			 public test.HELLOGen _gen;\
			 }""";

        env.addClass( srcRoot, "p1", "MyClass", code );

        fullBuild( project.getFullPath() );
        expectingNoProblems();

        Util.delete(project);
    }

    // doesn't work because of a jdt.core type system universe problem.
    public void testBugzilla120255() throws Exception{
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

		String a1Code = """
			package pkg; \
			
			import org.eclipse.jdt.apt.tests.annotations.apitest.Common;
			import java.util.*;
			
			@Common
			public class A1<T> {
			 \
			    @Common
			    Collection<String> collectionOfString;
			
			    @Common
			    Collection<List> collectionOfList;
			    public static class inner{}\
			}""";

		final IPath a1Path = env.addClass( srcRoot, "pkg", "A1", a1Code ); //$NON-NLS-1$ //$NON-NLS-2$

		// Set some per-project preferences
		IJavaProject jproj = env.getJavaProject( projName );
		AptConfig.setEnabled(jproj, true);
		fullBuild( project.getFullPath() );
		expectingSpecificProblemsFor(a1Path, new ExpectedProblem[]{
				new ExpectedProblem("", "java.util.List is assignable to java.util.Collection", a1Path),
				new ExpectedProblem("", "java.lang.String is not assignable to java.util.Collection", a1Path),
				new ExpectedProblem("", "Type parameter 'T' belongs to org.eclipse.jdt.apt.core.internal.declaration.ClassDeclarationImpl A1", a1Path)
				}
		);
    }

    /**
     * Test the Types.isSubtype() API, in various inheritance scenarios
     */
    public void testBugzilla206591A() throws Exception {
    	final String projName = RegressionTests.class.getName() + "206591.Project"; //$NON-NLS-1$
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

		String a1Code = """
			package pkg; \
			
			import org.eclipse.jdt.apt.tests.annotations.apitest.SubtypeOf;
			public interface A1 {
			 \
			}
			class A2 implements A1 {
			}
			class A3 extends A2 {
			    @SubtypeOf(A1.class) // yes
			    A2 _foo;
			    @SubtypeOf(A1.class) // yes
			    A3 _bar;
			    @SubtypeOf(A2.class) // yes
			    A3 _baz;
			    @SubtypeOf(A1.class) // yes
			    A1 _quux;
			    @SubtypeOf(A2.class) // no
			    A1 _yuzz;
			    @SubtypeOf(String.class) // no
			    A2 _wum;
			}
			class A4 extends A2 implements A1 {
			    @SubtypeOf(A1.class) // yes
			    A4 _humpf;
			    @SubtypeOf(A2.class) // yes
			    A4 _fuddle;
			    @SubtypeOf(A5.class) // no
			    A4 _snee;
			}
			class A5 {
			}
			""";

		final IPath a1Path = env.addClass( srcRoot, "pkg", "A1", a1Code ); //$NON-NLS-1$ //$NON-NLS-2$

		// Set some per-project preferences
		IJavaProject jproj = env.getJavaProject( projName );
		AptConfig.setEnabled(jproj, true);
		fullBuild( project.getFullPath() );
		expectingSpecificProblemsFor(a1Path, new ExpectedProblem[]{
				new ExpectedProblem("", "pkg.A2 is a subtype of pkg.A1", a1Path),
				new ExpectedProblem("", "pkg.A3 is a subtype of pkg.A1", a1Path),
				new ExpectedProblem("", "pkg.A3 is a subtype of pkg.A2", a1Path),
				new ExpectedProblem("", "pkg.A1 is a subtype of pkg.A1", a1Path),
				new ExpectedProblem("", "pkg.A1 is not a subtype of pkg.A2", a1Path),
				new ExpectedProblem("", "pkg.A2 is not a subtype of java.lang.String", a1Path),
				new ExpectedProblem("", "pkg.A4 is a subtype of pkg.A1", a1Path),
				new ExpectedProblem("", "pkg.A4 is a subtype of pkg.A2", a1Path),
				new ExpectedProblem("", "pkg.A4 is not a subtype of pkg.A5", a1Path),
				}
		);
    }

    /**
     * Test the Types.isAssignable() API, in various inheritance scenarios
     */
    public void testBugzilla206591B() throws Exception {
    	final String projName = RegressionTests.class.getName() + "206591.Project"; //$NON-NLS-1$
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

		String a1Code = """
			package pkg; \
			
			import org.eclipse.jdt.apt.tests.annotations.apitest.AssignableTo;
			public interface A1 {
			 \
			}
			class A2 implements A1 {
			}
			class A3 extends A2 {
			    @AssignableTo(A1.class) // yes
			    A2 _foo;
			    @AssignableTo(int.class) // yes
			    byte _bar;
			    @AssignableTo(A1.class) // yes
			    A3 _baz;
			    @AssignableTo(A2.class) // no
			    A1 _quux;
			}""";

		final IPath a1Path = env.addClass( srcRoot, "pkg", "A1", a1Code ); //$NON-NLS-1$ //$NON-NLS-2$

		// Set some per-project preferences
		IJavaProject jproj = env.getJavaProject( projName );
		AptConfig.setEnabled(jproj, true);
		fullBuild( project.getFullPath() );
		expectingSpecificProblemsFor(a1Path, new ExpectedProblem[]{
				new ExpectedProblem("", "pkg.A2 is assignable to pkg.A1", a1Path),
				new ExpectedProblem("", "byte is assignable to int", a1Path),
				new ExpectedProblem("", "pkg.A3 is assignable to pkg.A1", a1Path),
				new ExpectedProblem("", "pkg.A1 is not assignable to pkg.A2", a1Path),
				}
		);
    }

	/**
	 * Tests that a
	 * {@link AptConfig#hasProjectSpecificFactoryPath(IJavaProject)} checks if
	 * the project's factory path is equivalent to the default factory path, not
	 * just that it has created a factory path.
	 */
	public void testBugzilla423254() throws Exception {
		final String projName = RegressionTests.class.getName()
				+ "423254.Project"; //$NON-NLS-1$
		IPath projectPath = env.addProject(projName, "1.5"); //$NON-NLS-1$
		env.addExternalJars(projectPath, Util.getJavaClassLibs());

		env.removePackageFragmentRoot(projectPath, ""); //$NON-NLS-1$
		env.addPackageFragmentRoot(projectPath, "src"); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$

		IJavaProject jproj = env.getJavaProject(projName);
		assertFalse(AptConfig.hasProjectSpecificFactoryPath(jproj));

		IFactoryPath fp = AptConfig.getFactoryPath(jproj);
		fp.addVarJar(Path.fromOSString("/some_phony.jar"));
		AptConfig.setFactoryPath(jproj, fp);
		assertTrue(AptConfig.hasProjectSpecificFactoryPath(jproj));

		fp = AptConfig.getFactoryPath(jproj);
		fp.removeVarJar(Path.fromOSString("/some_phony.jar"));
		AptConfig.setFactoryPath(jproj, fp);
		assertFalse(AptConfig.hasProjectSpecificFactoryPath(jproj));
	}
}
