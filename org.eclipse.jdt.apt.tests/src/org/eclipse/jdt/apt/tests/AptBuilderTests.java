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

import java.io.File;

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

		IPath root = env.addPackageFragmentRoot( projectPath, "src" ); //$NON-NLS-1$
		env.setOutputFolder( projectPath, "bin" ); //$NON-NLS-1$

		File jarFile = TestUtil.createAndAddAnnotationJar( env
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
	 *   This test makes sure that our extra-dependency stuff is hooked up in the build.  
	 *   Specifically, we test to make sure that Extra dependencies only appear when 
	 *   an annotation processor looks up a type by name.  We also test that expected
	 *   build output is there because of the dependency.
	 */
	public void testExtraDependencies()
	{
		String codeA = "package p1;\n"
			+  "public class A { B b; D d; }";
		
		String codeB1 = "package p1;\n"
			+  "public class B { }";
		
		String codeB2 = "package p1;\n"
			+  "public class B { public static void main( String[] argv ) {} }";
		
		String codeC = "package p1;\n"
			+  "public class C { }";
		
		String codeD = "package p1;\n"
			+  "public class D { }";
		 
		String codeE = "package p1;\n"
			+  "public class E { }";
		
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		
		IPath p1aPath = env.addClass( srcRoot, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			codeA );
		
		IPath p1bPath = env.addClass( srcRoot, "p1", "B", //$NON-NLS-1$ //$NON-NLS-2$
			codeB1 );
		
		IPath p1cPath = env.addClass( srcRoot, "p1", "C", //$NON-NLS-1$ //$NON-NLS-2$
			codeC );
		
		IPath p1dPath = env.addClass( srcRoot, "p1", "D", //$NON-NLS-1$ //$NON-NLS-2$
			codeD );
		
		IPath p1ePath = env.addClass( srcRoot, "p1", "E", //$NON-NLS-1$ //$NON-NLS-2$
			codeE );
		
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		// touch B - make sure its public shape changes.
		env.addClass( srcRoot, "p1", "B", //$NON-NLS-1$ //$NON-NLS-2$
			codeB2 );
		
		incrementalBuild( project.getFullPath() );
		expectingNoProblems();
		expectingCompiledClasses(new String[]{"p1.B", "p1.A"}); //$NON-NLS-1$ //$NON-NLS-2$
		expectingCompilingOrder(new String[]{"p1.B", "p1.A"}); //$NON-NLS-1$ //$NON-NLS-2$

		//
		//  Now have p1.A w/ an anontation whose processor looks up p1.C by name 
		//
		
		// new code for A with an annotation processor that should introduce a dep on C
		codeA = "package p1;\n"
			+  "import org.eclipse.jdt.apt.tests.annotations.extradependency.ExtraDependencyAnnotation;" + "\n" 
			+  "@ExtraDependencyAnnotation" + "\n" 
			+  "public class A { B b; D d; }";
		
		p1aPath = env.addClass( srcRoot, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			codeA );
		p1bPath = env.addClass( srcRoot, "p1", "B", //$NON-NLS-1$ //$NON-NLS-2$
			codeB1 );
		
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		// touch B
		p1bPath = env.addClass( srcRoot, "p1", "B", //$NON-NLS-1$ //$NON-NLS-2$
			codeB2 );
		
		incrementalBuild( project.getFullPath() );
		expectingNoProblems();
		
		//
		// Note that p1.A is showing up twice because it has annotations, and we need to 
		// parse the source, parsing runs through the compiler, and this registers the 
		// file a second time with the Compiler#DebugRequestor 
		//
		expectingCompiledClasses(new String[]{"p1.B", "p1.A", "p1.A", "p1.C"}); //$NON-NLS-1$ //$NON-NLS-2$
		expectingCompilingOrder(new String[]{"p1.B", "p1.A", "p1.C", "p1.A"}); //$NON-NLS-1$ //$NON-NLS-2$
		
		//
		// now make sure that p1.C is not compiled when A uses NoOp Annotation
		//
		
		// new code for A with an annotation processor that should introduce a dep on C
		codeA = "package p1;\n"
			+  "import org.eclipse.jdt.apt.tests.annotations.noop.NoOpAnnotation;" + "\n" 
			+  "@NoOpAnnotation" + "\n" 
			+  "public class A { B b; D d; }";
		
		p1aPath = env.addClass( srcRoot, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			codeA );
		p1bPath = env.addClass( srcRoot, "p1", "B", //$NON-NLS-1$ //$NON-NLS-2$
			codeB1 );
		
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		// touch B
		p1bPath = env.addClass( srcRoot, "p1", "B", //$NON-NLS-1$ //$NON-NLS-2$
			codeB2 );
		
		incrementalBuild( project.getFullPath() );
		expectingNoProblems();
		
		//
		// Note that p1.A is showing up twice because it has annotations, and we need to 
		// parse the source, parsing runs through the compiler, and this registers the 
		// file a second time with the Compiler#DebugRequestor 
		//
		expectingCompiledClasses(new String[]{"p1.B", "p1.A", "p1.A" }); //$NON-NLS-1$ //$NON-NLS-2$
		expectingCompilingOrder(new String[]{"p1.B", "p1.A", "p1.A" }); //$NON-NLS-1$ //$NON-NLS-2$
		
		
		
	}
}
