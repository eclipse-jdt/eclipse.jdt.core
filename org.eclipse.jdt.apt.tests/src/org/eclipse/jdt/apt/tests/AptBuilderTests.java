/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.core.tests.builder.Tests;
import org.eclipse.jdt.core.tests.util.Util;

public class AptBuilderTests extends Tests
{

	public AptBuilderTests(String name)
	{
		super( name );
	}

	public static Test suite()
	{
		return new TestSuite( AptBuilderTests.class );
	}

	public void setUp() throws Exception
	{
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
	
	public static String getProjectName()
	{
		return AptBuilderTests.class.getName() + "Project";
	}

	public IPath getSourcePath()
	{
		IProject project = env.getProject( getProjectName() );
		IFolder srcFolder = project.getFolder( "src" );
		IPath srcRoot = srcFolder.getFullPath();
		return srcRoot;
	}
	
	public void testGeneratedFileInBuilder() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		
		String code = "package p1;\n"
			+ "//import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;"
			+ "\n" + "public class A " + "\n" + "{"
			+ "    //@HelloWorldAnnotation" + "\n"
			+ "    public static void main( String[] argv )" + "\n" + "    {"
			+ "\n"
			+ "        generatedfilepackage.GeneratedFileTest.helloWorld();"
			+ "\n" + "    }" + "\n" + "}" + "\n";

		
		IPath p1aPath = env.addClass( srcRoot, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			code );

		fullBuild( project.getFullPath() );

		expectingOnlyProblemsFor( p1aPath );
		expectingOnlySpecificProblemFor( p1aPath, new Problem(
			"A", "generatedfilepackage cannot be resolved", p1aPath ) ); //$NON-NLS-1$ //$NON-NLS-2$	

		code = "package p1;\n"
			+ "import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;"
			+ "\n" + "public class A " + "\n" + "{"
			+ "    @HelloWorldAnnotation" + "\n"
			+ "    public static void main( String[] argv )" + "\n" + "    {"
			+ "\n"
			+ "        generatedfilepackage.GeneratedFileTest.helloWorld();"
			+ "\n" + "    }" + "\n" + "}" + "\n";

		env.addClass( srcRoot, "p1", "A", code );
		fullBuild( project.getFullPath() );

		expectingOnlyProblemsFor( new IPath[0] );
	}
	
	/**
	 *  This test makes sure we run apt on generated files during build
	 */
	public void testNestedGeneratedFileInBuilder() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		
		String code = "package p1;\n"
			+ "//import org.eclipse.jdt.apt.tests.annotations.nestedhelloworld.NestedHelloWorldAnnotation;"
			+ "\n" + "public class A " + "\n" + "{"
			+ "    //@NestedHelloWorldAnnotation" + "\n"
			+ "    public static void main( String[] argv )" + "\n" + "    {"
			+ "\n"
			+ "        generatedfilepackage.GeneratedFileTest.helloWorld();"
			+ "\n" + "    }" + "\n" + "}" + "\n";

		
		IPath p1aPath = env.addClass( srcRoot, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			code );

		fullBuild( project.getFullPath() );

		expectingOnlyProblemsFor( p1aPath );
		expectingOnlySpecificProblemFor( p1aPath, new Problem(
			"A", "generatedfilepackage cannot be resolved", p1aPath ) ); //$NON-NLS-1$ //$NON-NLS-2$	

		code = "package p1;\n"
			+ "import org.eclipse.jdt.apt.tests.annotations.nestedhelloworld.NestedHelloWorldAnnotation;"
			+ "\n" + "public class A " + "\n" + "{"
			+ "    @NestedHelloWorldAnnotation" + "\n"
			+ "    public static void main( String[] argv )" + "\n" + "    {"
			+ "\n"
			+ "        generatedfilepackage.GeneratedFileTest.helloWorld();"
			+ "\n" + "    }" + "\n" + "}" + "\n";

		env.addClass( srcRoot, "p1", "A", code );
		fullBuild( project.getFullPath() );

		expectingOnlyProblemsFor( new IPath[0] );
	}
	
	
	
	/**
	 *   This test makes sure that our extra-dependency stuff is hooked up in the build.  
	 *   Specifically, we test to make sure that Extra dependencies only appear when 
	 *   an annotation processor looks up a type by name.  We also test that expected
	 *   build output is there because of the dependency.
	 */
	public void testExtraDependencies()
	{
		String codeA = "package p1.p2.p3.p4;\n"
			+  "public class A { B b; D d; }";
		
		String codeB1 = "package p1.p2.p3.p4;\n"
			+  "public class B { }";
		
		String codeB2 = "package p1.p2.p3.p4;\n"
			+  "public class B { public static void main( String[] argv ) {} }";
		
		String codeC = "package p1.p2.p3.p4;\n"
			+  "public class C { }";
		
		String codeD = "package p1.p2.p3.p4;\n"
			+  "public class D { }";
		 
		String codeE = "package p1.p2.p3.p4;\n"
			+  "public class E { }";
		
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		
		env.addClass( srcRoot, "p1.p2.p3.p4", "A", //$NON-NLS-1$ //$NON-NLS-2$
			codeA );
		
		env.addClass( srcRoot, "p1.p2.p3.p4", "B", //$NON-NLS-1$ //$NON-NLS-2$
			codeB1 );
		
		env.addClass( srcRoot, "p1.p2.p3.p4", "C", //$NON-NLS-1$ //$NON-NLS-2$
			codeC );
		
		env.addClass( srcRoot, "p1.p2.p3.p4", "D", //$NON-NLS-1$ //$NON-NLS-2$
			codeD );
		
		env.addClass( srcRoot, "p1.p2.p3.p4", "E", //$NON-NLS-1$ //$NON-NLS-2$
			codeE );
		
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		// touch B - make sure its public shape changes.
		env.addClass( srcRoot, "p1.p2.p3.p4", "B", //$NON-NLS-1$ //$NON-NLS-2$
			codeB2 );
		
		incrementalBuild( project.getFullPath() );
		expectingNoProblems();
		expectingCompiledClasses(new String[]{"p1.p2.p3.p4.B", "p1.p2.p3.p4.A"}); //$NON-NLS-1$ //$NON-NLS-2$
		expectingCompilingOrder(new String[]{"p1.p2.p3.p4.B", "p1.p2.p3.p4.A"}); //$NON-NLS-1$ //$NON-NLS-2$

		//
		//  Now have p1.p2.p3.p4.A w/ an anontation whose processor looks up p1.p2.p3.p4.C by name 
		//
		
		// new code for A with an annotation processor that should introduce a dep on C
		codeA = "package p1.p2.p3.p4;\n"
			+  "import org.eclipse.jdt.apt.tests.annotations.extradependency.ExtraDependencyAnnotation;" + "\n" 
			+  "@ExtraDependencyAnnotation" + "\n" 
			+  "public class A { B b; D d; }";
		
		env.addClass( srcRoot, "p1.p2.p3.p4", "A", //$NON-NLS-1$ //$NON-NLS-2$
			codeA );
		env.addClass( srcRoot, "p1.p2.p3.p4", "B", //$NON-NLS-1$ //$NON-NLS-2$
			codeB1 );
		
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		// touch B
		env.addClass( srcRoot, "p1.p2.p3.p4", "B", //$NON-NLS-1$ //$NON-NLS-2$
			codeB2 );
		
		incrementalBuild( project.getFullPath() );
		expectingNoProblems();
		
		//
		// Note that p1.p2.p3.p4.A is showing up twice because it has annotations, and we need to 
		// parse the source, parsing runs through the compiler, and this registers the 
		// file a second time with the Compiler#DebugRequestor 
		//
		expectingCompiledClasses(new String[]{"p1.p2.p3.p4.B", "p1.p2.p3.p4.A", "p1.p2.p3.p4.A", "p1.p2.p3.p4.C"}); //$NON-NLS-1$ //$NON-NLS-2$
		expectingCompilingOrder(new String[]{"p1.p2.p3.p4.B", "p1.p2.p3.p4.A", "p1.p2.p3.p4.C", "p1.p2.p3.p4.A"}); //$NON-NLS-1$ //$NON-NLS-2$
		
		//
		// now make sure that p1.p2.p3.p4.C is not compiled when A uses NoOp Annotation
		//
		
		// new code for A with an annotation processor that should introduce a dep on C
		codeA = "package p1.p2.p3.p4;\n"
			+  "import org.eclipse.jdt.apt.tests.annotations.noop.NoOpAnnotation;" + "\n" 
			+  "@NoOpAnnotation" + "\n" 
			+  "public class A { B b; D d; }";
		
		env.addClass( srcRoot, "p1.p2.p3.p4", "A", //$NON-NLS-1$ //$NON-NLS-2$
			codeA );
		env.addClass( srcRoot, "p1.p2.p3.p4", "B", //$NON-NLS-1$ //$NON-NLS-2$
			codeB1 );
		
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		// touch B
		env.addClass( srcRoot, "p1.p2.p3.p4", "B", //$NON-NLS-1$ //$NON-NLS-2$
			codeB2 );
		
		incrementalBuild( project.getFullPath() );
		expectingNoProblems();
		
		//
		// Note that p1.p2.p3.p4.A is showing up twice because it has annotations, and we need to 
		// parse the source, parsing runs through the compiler, and this registers the 
		// file a second time with the Compiler#DebugRequestor 
		//
		expectingCompiledClasses(new String[]{"p1.p2.p3.p4.B", "p1.p2.p3.p4.A", "p1.p2.p3.p4.A" }); //$NON-NLS-1$ //$NON-NLS-2$
		expectingCompilingOrder(new String[]{"p1.p2.p3.p4.B", "p1.p2.p3.p4.A", "p1.p2.p3.p4.A" }); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 *   Test that we do not recompile generated files that are
	 *   not changed even as their parent is modified.
	 */
	public void testCaching()
	{
		String code = "package p1;\n"
			+ "import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;"
			+ "\n" + "public class A " + "\n" + "{"
			+ "    @HelloWorldAnnotation" + "\n"
			+ "    public static void main( String[] argv )" + "\n" + "    {"
			+ "\n"
			+ "        generatedfilepackage.GeneratedFileTest.helloWorld();"
			+ "\n" 
			+ "    }" 
			+ "\n" 
			+ "}" 
			+ "\n";
		
		String modifiedCode = "package p1;\n"
			+ "import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;"
			+ "\n" + "public class A " + "\n" + "{"
			+ "    @HelloWorldAnnotation" + "\n"
			+ "    public static void main( String[] argv )" + "\n" + "    {"
			+ "\n"
			+ "        generatedfilepackage.GeneratedFileTest.helloWorld();"
			+ "\n" 
			+ "    }" 
			+ "\n" 
			+ "    public static void otherMethod()" + "\n" + "    {"
			+ "        System.out.println();\n"
			+ "    }"
			+ "}" 
			+ "\n";
		
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		
		env.addClass( srcRoot, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			code );
		
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		expectingCompiledClasses(new String[] {"p1.A", "p1.A", "generatedfilepackage.GeneratedFileTest"}); //$NON-NLS-1 //$NON_NLS-2$

		// build this again.  The first build would have caused a classpath change by adding in the 
		// generated-src dir, and a classpath change causes a full build 
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		expectingCompiledClasses(new String[] {"p1.A", "p1.A", "generatedfilepackage.GeneratedFileTest"}); //$NON-NLS-1 //$NON_NLS-2$

		
		// touch A - make sure its public shape changes.
		env.addClass( srcRoot, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			modifiedCode );
		
		incrementalBuild( project.getFullPath() );
		expectingNoProblems();
		expectingCompiledClasses(new String[]{"p1.A", "p1.A"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * This test makes sure that we delete generated files when the parent file 
	 * is deleted.  We also make sure that multi-parent support is working.
	 */
	public void testDeletedParentFile() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

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

		IPath p1a1Path = env.addClass( srcRoot, "p1", "A1", //$NON-NLS-1$ //$NON-NLS-2$
			a1Code );
		IPath p1a2Path = env.addClass( srcRoot, "p1", "A2", //$NON-NLS-1$ //$NON-NLS-2$
			a2Code );
		IPath p1bPath = env.addClass( srcRoot, "p1", "B", //$NON-NLS-1$ //$NON-NLS-2$
			bCode );
		
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		// now delete file A1 and make sure we still have no problems
		env.removeFile( p1a1Path );
		
		// sleep to let the resource-change event fire
		sleep( 1000 );
		
		incrementalBuild( project.getFullPath() );
		
		expectingNoProblems();
		
		// now delete file A2 and make sure we have a problem on B
		env.removeFile( p1a2Path );

		// sleep to let the resource-change event fire
		sleep( 1000 );

		incrementalBuild( project.getFullPath() );
		expectingOnlyProblemsFor( p1bPath );
		expectingOnlySpecificProblemFor( p1bPath, new Problem(
			"B", "generatedfilepackage.GeneratedFileTest cannot be resolved to a type", p1bPath ) ); //$NON-NLS-1$ //$NON-NLS-2$	
	}
	
	public void testStopGeneratingFileInBuilder_FullBuild()
	{
		internalTestStopGeneratingFileInBuilder( true );
	}

	public void testStopGeneratingFileInBuilder_IncrementalBuild()
	{
		internalTestStopGeneratingFileInBuilder( false );
	}
	
	private void internalTestStopGeneratingFileInBuilder( boolean fullBuild )
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		
		String code = "package p1;\n"
			+ "//import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;"
			+ "\n" + "public class A " + "\n" + "{"
			+ "    //@HelloWorldAnnotation" + "\n"
			+ "    public static void main( String[] argv )" + "\n" + "    {"
			+ "\n"
			+ "        generatedfilepackage.GeneratedFileTest.helloWorld();"
			+ "\n" + "    }" + "\n" + "}" + "\n";

		
		IPath p1aPath = env.addClass( srcRoot, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			code );

		if ( fullBuild )
			fullBuild( project.getFullPath() );
		else
			incrementalBuild( project.getFullPath() );
		

		expectingOnlyProblemsFor( p1aPath );
		expectingOnlySpecificProblemFor( p1aPath, new Problem(
			"A", "generatedfilepackage cannot be resolved", p1aPath ) ); //$NON-NLS-1$ //$NON-NLS-2$	

		code = "package p1;\n"
			+ "import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;"
			+ "\n" + "public class A " + "\n" + "{"
			+ "    @HelloWorldAnnotation" + "\n"
			+ "    public static void main( String[] argv )" + "\n" + "    {"
			+ "\n"
			+ "        generatedfilepackage.GeneratedFileTest.helloWorld();"
			+ "\n" + "    }" + "\n" + "}" + "\n";

		env.addClass( srcRoot, "p1", "A", code );
		if ( fullBuild )
			fullBuild( project.getFullPath() );
		else
			incrementalBuild( project.getFullPath() );
		
		expectingOnlyProblemsFor( new IPath[0] );

		// do a full build again.  This is necessary because generating the file
		// caused a classpath change, so the next inremental build will end up being
		// a full build because of the classpath change
		if ( ! fullBuild )
			fullBuild( project.getFullPath() );
		
		// now remove the annotation.  The generated file should go away
		// and we should see errors again
		code = "package p1;\n"
			+ "//import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;"
			+ "\n" + "public class A " + "\n" + "{"
			+ "    //@HelloWorldAnnotation" + "\n"
			+ "    public static void main( String[] argv )" + "\n" + "    {"
			+ "\n"
			+ "        generatedfilepackage.GeneratedFileTest.helloWorld();"
			+ "\n" + "    }" + "\n" + "}" + "\n";

		env.addClass( srcRoot, "p1", "A", code );
		
		if ( fullBuild )
			fullBuild( project.getFullPath() );
		else
			incrementalBuild( project.getFullPath() );
		
		expectingOnlyProblemsFor( p1aPath );
		String expectedError;
		if ( fullBuild )
			expectedError = "generatedfilepackage cannot be resolved";
		else
			expectedError = "generatedfilepackage.GeneratedFileTest cannot be resolved to a type";

		expectingOnlySpecificProblemFor( p1aPath, 
					new Problem( "A", expectedError, p1aPath ) ); //$NON-NLS-1$ 
	}
	
	private static void sleep( long millis )
	{	
		long end = System.currentTimeMillis() + millis;
		while ( millis > 0 )
		{
			try
			{
				Thread.sleep( millis );
			}
			catch ( InterruptedException ie )
			{}
			millis = end - System.currentTimeMillis();
		}
	}
	
}
