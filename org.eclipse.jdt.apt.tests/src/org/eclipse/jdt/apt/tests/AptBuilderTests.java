/*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    IBM Corporation - updated test to reflect changes for https://bugs.eclipse.org/bugs/show_bug.cgi?id=185601
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedSourceFolderManager;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotationProcessor;
import org.eclipse.jdt.apt.tests.annotations.messager.MessagerAnnotationProcessor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.tests.util.Util;

public class AptBuilderTests extends APTTestBase
{

	public AptBuilderTests(String name)
	{
		super( name );
	}

	public static Test suite()
	{
		return new TestSuite( AptBuilderTests.class );
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();

		//
		// project will be deleted by super-class's tearDown() method
		// create a project with a src directory as the project root directory
		//
		IPath projectPath = env.addProject( getProjectName_ProjectRootAsSrcDir(), "1.5" );
		env.addExternalJars( projectPath, Util.getJavaClassLibs() );
		fullBuild( projectPath );

		// remove old package fragment root so that names don't collide
		env.setOutputFolder( projectPath, "bin" ); //$NON-NLS-1$

		IJavaProject jproj = env.getJavaProject( projectPath );
		AptConfig.setEnabled( jproj, true );
		TestUtil.createAndAddAnnotationJar( jproj );

	}

	public static String getProjectName_ProjectRootAsSrcDir()
	{
		return AptBuilderTests.class.getName() + "NoSrcProject"; //$NON-NLS-1$
	}

	public IPath getSourcePath( String projectName )
	{
		if ( getProjectName_ProjectRootAsSrcDir().equals( projectName) )
			return new Path( "/" + getProjectName_ProjectRootAsSrcDir() );
		else
		{
			IProject project = env.getProject( projectName );
			IFolder srcFolder = project.getFolder( "src" ); //$NON-NLS-1$
			IPath srcRoot = srcFolder.getFullPath();
			return srcRoot;
		}
	}
	public void testGeneratedFileInBuilder() throws Exception
	{
		_testGeneratedFileInBuilder0( getProjectName() );
	}

	/**
	 *  Regresses Buzilla 103745 and 95661
	 */

	public void testGeneratedFileInBuilder_ProjectRootAsSourceDir() throws Exception
	{
		_testGeneratedFileInBuilder0( getProjectName_ProjectRootAsSrcDir() );
	}


	public void testGeneratedFileInBuilder1() throws Exception{
		_testGeneratedFileInBuilder1( getProjectName() );
	}

	private void _testGeneratedFileInBuilder0(String projectName){
		IProject project = env.getProject( projectName );
		IPath srcRoot = getSourcePath( projectName );

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
		expectingOnlySpecificProblemFor( p1aPath, new ExpectedProblem(
			"A", "generatedfilepackage cannot be resolved", p1aPath ) ); //$NON-NLS-1$ //$NON-NLS-2$

		code = "package p1;\n"
			+ "import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;"
			+ "\n" + "public class A " + "\n" + "{"
			+ "    @HelloWorldAnnotation" + "\n"
			+ "    public static void main( String[] argv )" + "\n" + "    {"
			+ "\n"
			+ "         generatedfilepackage.GeneratedFileTest.helloWorld();"
			+ "\n" + "    }" + "\n" + "}" + "\n";

		env.addClass( srcRoot, "p1", "A", code );
		fullBuild( project.getFullPath() );

		expectingOnlyProblemsFor( new IPath[0] );
	}

	/**
	 *  slight variation to _testGeneratedFileInBuilder0.
	 *  Difference:
	 *   The method invocation is not fully qualified and an import is added.
	 */
	private void _testGeneratedFileInBuilder1( String projectName )
	{
		IProject project = env.getProject( projectName );
		IPath srcRoot = getSourcePath( projectName );

		String code = "package p1;\n"
			+ "import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;"
			+ "import generatedfilepackage.GeneratedFileTest;"
			+ "\n" + "public class A " + "\n" + "{"
			+ "    @HelloWorldAnnotation" + "\n"
			+ "    public static void main( String[] argv )" + "\n" + "    {"
			+ "\n"
			+ "        GeneratedFileTest.helloWorld();"
			+ "\n" + "    }" + "\n" + "}" + "\n";

		env.addClass( srcRoot, "p1", "A", code );
		fullBuild( project.getFullPath() );

		expectingOnlyProblemsFor( new IPath[0] );
	}

	/**
	 *  Try generating a bogus type name; expect exception
	 */
	public void testGeneratingIllegalTypeName()
	{
		String projectName = getProjectName();
		clearProcessorResult(HelloWorldAnnotationProcessor.class);
		IProject project = env.getProject( projectName );
		IPath srcRoot = getSourcePath( projectName );

		String code = "package p1;\n"
			+ "import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;"
			+ "import generatedfilepackage.GeneratedFileTest;"
			+ "\n" + "public class A " + "\n" + "{"
			+ "    @HelloWorldAnnotation(\"Bad-Type-Name\")" + "\n"
			+ "    public static void main( String[] argv )" + "\n" + "    {"
			+ "\n"
			+ "        GeneratedFileTest.helloWorld();"
			+ "\n" + "    }" + "\n" + "}" + "\n";

		env.addClass( srcRoot, "p1", "A", code );
		fullBuild( project.getFullPath() );
		assertEquals("Could not generate text file due to IOException", getProcessorResult(HelloWorldAnnotationProcessor.class));

		// Type "lowercase" would cause a warning in the new type wizard, because it doesn't start with caps.
		// Test that we do not issue a warning or error in this case.
		String code2 = "package p1;\n"
			+ "import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;"
			+ "import generatedfilepackage.lowercase;"
			+ "\n" + "public class A " + "\n" + "{"
			+ "    @HelloWorldAnnotation(\"lowercase\")" + "\n"
			+ "    public static void main( String[] argv )" + "\n" + "    {"
			+ "\n"
			+ "        lowercase.helloWorld();"
			+ "\n" + "    }" + "\n" + "}" + "\n";

		env.addClass( srcRoot, "p1", "A", code2 );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
	}

	/**
	 *  Try running two processors on the same file, and look for interactions.
	 *  Regression for https://bugs.eclipse.org/bugs/show_bug.cgi?id=175794
	 */
	public void testTwoAnnotations()
	{
		String projectName = getProjectName();
		clearProcessorResult(HelloWorldAnnotationProcessor.class);
		clearProcessorResult(MessagerAnnotationProcessor.class);
		IProject project = env.getProject( projectName );
		IPath srcRoot = getSourcePath( projectName );

		String codeMessageFirst = "package p1;\n"
			+ "import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;\n"
			+ "import org.eclipse.jdt.apt.tests.annotations.messager.MessagerAnnotation;\n"
			+ "import generatedfilepackage.GeneratedFileTest;\n"
			+ "@MessagerAnnotation(severity=MessagerAnnotation.Severity.ERROR)\n"
			+ "@HelloWorldAnnotation\n"
			+ "public class A {\n"
			+ "    public static void main( String[] argv ) {\n"
			+ "        GeneratedFileTest.helloWorld();\n"
			+ "    }\n"
			+ "}\n";

		IPath p1aPath = env.addClass( srcRoot, "p1", "A", codeMessageFirst );
		fullBuild( project.getFullPath() );
		checkProcessorResult(HelloWorldAnnotationProcessor.class);
		checkProcessorResult(MessagerAnnotationProcessor.class);
		expectingOnlySpecificProblemFor( p1aPath, new ExpectedProblem(
			"A", MessagerAnnotationProcessor.PROBLEM_TEXT_ERROR, p1aPath ) ); //$NON-NLS-1$ //$NON-NLS-2$
	}



	/**
	 *  This test makes sure we run apt on generated files during build
	 */

	public void testNestedGeneratedFileInBuilder() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath( getProjectName() );

		String code = "package p1;\n"
			+ "//import org.eclipse.jdt.apt.tests.annotations.nestedhelloworld.NestedHelloWorldAnnotation;"
			+ "import generatedfilepackage.GeneratedFileTest;"
			+ "\n" + "public class A " + "\n" + "{"
			+ "    //@NestedHelloWorldAnnotation" + "\n"
			+ "    public static void main( String[] argv )" + "\n" + "    {"
			+ "\n"
			+ "        GeneratedFileTest.helloWorld();"
			+ "\n" + "    }" + "\n" + "}" + "\n";


		IPath p1aPath = env.addClass( srcRoot, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			code );

		fullBuild( project.getFullPath() );

		expectingOnlyProblemsFor( p1aPath );
		expectingOnlySpecificProblemFor( p1aPath, new ExpectedProblem(
			"A", "GeneratedFileTest cannot be resolved", p1aPath ) ); //$NON-NLS-1$ //$NON-NLS-2$

		code = "package p1;\n"
			+ "import org.eclipse.jdt.apt.tests.annotations.nestedhelloworld.NestedHelloWorldAnnotation;\n"
			+ "import generatedfilepackage.GeneratedFileTest;"
			+ "\n" + "public class A " + "\n" + "{"
			+ "    @NestedHelloWorldAnnotation" + "\n"
			+ "    public static void main( String[] argv )" + "\n" + "    {"
			+ "\n"
			+ "        GeneratedFileTest.helloWorld();"
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
			+  "public class A { B b; }";

		String codeB1 = "package p1.p2.p3.p4;\n"
			+  "public class B { }";

		String codeB2 = "package p1.p2.p3.p4;\n"
			+  "public class B { public static void main( String[] argv ) {} }";

		String codeC = "package p1.p2.p3.p4;\n"
			+  "public class C { }";

		String codeC2 = "package p1.p2.p3.p4;\n"
			+  "public class C { public int foo; }";

		String codeD = "package p1.p2.p3.p4;\n"
			+  "public class D { }";

		String codeE = "package p1.p2.p3.p4;\n"
			+  "public class E { }";

		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath( getProjectName() );

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
		expectingCompilingOrder(
				new String[] { "/org.eclipse.jdt.apt.tests.AptBuilderTestsProject/src/p1/p2/p3/p4/B.java", //$NON-NLS-1$
						"/org.eclipse.jdt.apt.tests.AptBuilderTestsProject/src/p1/p2/p3/p4/A.java" }); //$NON-NLS-1$

		//
		//  Now have p1.p2.p3.p4.A w/ an anontation whose processor looks up p1.p2.p3.p4.C by name
		//

		// new code for A with an annotation processor that should introduce a dep on C
		codeA = "package p1.p2.p3.p4;\n"
			+  "import org.eclipse.jdt.apt.tests.annotations.extradependency.ExtraDependencyAnnotation;" + "\n"
			+  "@ExtraDependencyAnnotation" + "\n"
			+  "public class A {  }";

		env.addClass( srcRoot, "p1.p2.p3.p4", "A", //$NON-NLS-1$ //$NON-NLS-2$
			codeA );

		fullBuild( project.getFullPath() );
		expectingNoProblems();

		// touch C
		env.addClass( srcRoot, "p1.p2.p3.p4", "C", //$NON-NLS-1$ //$NON-NLS-2$
			codeC2 );

		incrementalBuild( project.getFullPath() );
		expectingNoProblems();

		//
		// Note that p1.p2.p3.p4.A is showing up twice because it has annotations, and we need to
		// parse the source, parsing runs through the compiler, and this registers the
		// file a second time with the Compiler#DebugRequestor
		//
		expectingCompiledClasses(new String[]{"p1.p2.p3.p4.C", "p1.p2.p3.p4.A"}); //$NON-NLS-1$ //$NON-NLS-2$
		expectingCompilingOrder(
				new String[] { "/org.eclipse.jdt.apt.tests.AptBuilderTestsProject/src/p1/p2/p3/p4/C.java",
						"/org.eclipse.jdt.apt.tests.AptBuilderTestsProject/src/p1/p2/p3/p4/A.java",
						"/org.eclipse.jdt.apt.tests.AptBuilderTestsProject/src/p1/p2/p3/p4/A.java" });

		//
		// now make sure that p1.p2.p3.p4.C is not compiled when A uses NoOp Annotation
		//

		// new code for A with an annotation processor that should remove a dep on C
		codeA = "package p1.p2.p3.p4;\n"
			+  "import org.eclipse.jdt.apt.tests.annotations.noop.NoOpAnnotation;" + "\n"
			+  "@NoOpAnnotation" + "\n"
			+  "public class A { B b; D d; }";

		env.addClass( srcRoot, "p1.p2.p3.p4", "A", //$NON-NLS-1$ //$NON-NLS-2$
			codeA );

		fullBuild( project.getFullPath() );
		expectingNoProblems();

		// touch C
		env.addClass( srcRoot, "p1.p2.p3.p4", "C", //$NON-NLS-1$ //$NON-NLS-2$
			codeC2 );

		incrementalBuild( project.getFullPath() );
		expectingNoProblems();

		//
		// Note that p1.p2.p3.p4.A is showing up twice because it has annotations, and we need to
		// parse the source, parsing runs through the compiler, and this registers the
		// file a second time with the Compiler#DebugRequestor
		//
		expectingCompiledClasses(new String[]{"p1.p2.p3.p4.C" }); //$NON-NLS-1$ //$NON-NLS-2$
		expectingCompilingOrder(
				new String[] { "/org.eclipse.jdt.apt.tests.AptBuilderTestsProject/src/p1/p2/p3/p4/C.java" });
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
			+ "        "
			+ "\n"
			+ "    }"
			+ "\n"
			+ "    public static void otherMethod()" + "\n" + "    {"
			+ "        System.out.println();\n"
			+ "    }"
			+ "}"
			+ "\n";

		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath( getProjectName() );

		env.addClass( srcRoot, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			code );

		fullBuild( project.getFullPath() );
		expectingNoProblems();
		expectingCompiledClasses(new String[] {"p1.A", "generatedfilepackage.GeneratedFileTest"}); //$NON-NLS-1 //$NON_NLS-2$

		// touch A - make sure its public shape changes.
		env.addClass( srcRoot, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			modifiedCode );
		incrementalBuild( project.getFullPath() );
		expectingNoProblems();
		expectingCompiledClasses(new String[]{"p1.A"}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/**
	 * This test makes sure that we delete generated files when the parent file
	 * is deleted.  We also make sure that multi-parent support is working.
	 */

	public void testDeletedParentFile() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath( getProjectName() );

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
		TestUtil.deleteFile(p1a1Path);

		// sleep to let the resource-change event fire
		sleep( 1000 );

		incrementalBuild( project.getFullPath() );

		expectingNoProblems();

		// now delete file A2 and make sure we have a problem on B
		TestUtil.deleteFile( p1a2Path );

		// sleep to let the resource-change event fire
		// TODO: Is there a more reliable, consistent, and efficient way to wait?
		sleep( 1000 );

		incrementalBuild( project.getFullPath() );
		expectingOnlyProblemsFor( p1bPath );
		expectingOnlySpecificProblemFor( p1bPath, new ExpectedProblem(
			"B", "generatedfilepackage cannot be resolved to a type", p1bPath ) ); //$NON-NLS-1$ //$NON-NLS-2$
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
		IPath srcRoot = getSourcePath( getProjectName() );

		String code = "package p1;\n"
			+ "//import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;\n"
			+ "import generatedfilepackage.GeneratedFileTest;"
			+ "\n" + "public class A " + "\n" + "{"
			+ "    //@HelloWorldAnnotation" + "\n"
			+ "    public static void main( String[] argv )" + "\n" + "    {"
			+ "\n"
			+ "        GeneratedFileTest.helloWorld();"
			+ "\n" + "    }" + "\n" + "}" + "\n";


		IPath p1aPath = env.addClass( srcRoot, "p1", "A", //$NON-NLS-1$ //$NON-NLS-2$
			code );

		if ( fullBuild )
			fullBuild( project.getFullPath() );
		else
			incrementalBuild( project.getFullPath() );


		expectingOnlyProblemsFor( p1aPath );
		expectingOnlySpecificProblemsFor( p1aPath, new ExpectedProblem[]{
				new ExpectedProblem( "A", "The import generatedfilepackage cannot be resolved", p1aPath ),
				new ExpectedProblem( "A", "GeneratedFileTest cannot be resolved", p1aPath ) }
				); //$NON-NLS-1$ //$NON-NLS-2$

		code = "package p1;\n"
			+ "import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;"
			+ "import generatedfilepackage.GeneratedFileTest;"
			+ "\n" + "public class A " + "\n" + "{"
			+ "    @HelloWorldAnnotation" + "\n"
			+ "    public static void main( String[] argv )" + "\n" + "    {"
			+ "\n"
			+ "        GeneratedFileTest.helloWorld();"
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
			+ "import generatedfilepackage.GeneratedFileTest;"
			+ "\n" + "public class A " + "\n" + "{"
			+ "    //@HelloWorldAnnotation" + "\n"
			+ "    public static void main( String[] argv )" + "\n" + "    {"
			+ "\n"
			+ "        GeneratedFileTest.helloWorld();"
			+ "\n" + "    }" + "\n" + "}" + "\n";

		env.addClass( srcRoot, "p1", "A", code );

		if ( fullBuild )
			fullBuild( project.getFullPath() );
		else
			incrementalBuild( project.getFullPath() );

		expectingOnlyProblemsFor( p1aPath );

		expectingOnlySpecificProblemFor( p1aPath,
					new ExpectedProblem( "A", "GeneratedFileTest cannot be resolved", p1aPath ) ); //$NON-NLS-1$
	}

	public void testAPTRounding()
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath( getProjectName()  );

		String codeX = "package p1;\n"
			+ "\n import org.eclipse.jdt.apt.tests.annotations.aptrounding.*;"
			+ "\n@GenBean\n"
			+ "public class X {}\n";

		env.addClass( srcRoot, "p1", "X", codeX );

		String codeY = "package p1;\n"
			+ "\n import org.eclipse.jdt.apt.tests.annotations.aptrounding.*;"
			+ "public class Y { @GenBean2 test.Bean _bean = null; }\n";

		env.addClass( srcRoot, "p1", "Y", codeY );

		fullBuild( project.getFullPath() );

		expectingNoProblems();
	}

	// After fix for https://bugs.eclipse.org/bugs/show_bug.cgi?id=185601,
	// the config marker 'Generated source folder * is not in classpath'
	// is not easy to simulate. So, this test doesn't really do anything
	public void testConfigMarker() throws Exception{
		final String projectName = "ConfigMarkerTestProject";
		final IJavaProject javaProj = createJavaProject( projectName );
		// apt is currently disabled save off the cp before configuration
		final IClasspathEntry[] cp = javaProj.getRawClasspath();
		IProject project = env.getProject( projectName );
		IPath srcRoot = getSourcePath( projectName );
		// this will cause a type generation.
		String code = "package pkg;\n"
			+ "import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;"
			+ "\npublic class Foo{\n"
			+ "    @HelloWorldAnnotation\n"
			+ "    public static void main( String[] argv ){}"
			+ "\n}";

		env.addClass( srcRoot, "pkg", "Foo", code );

		AptConfig.setEnabled(javaProj, true);
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		expectingNoMarkers();

		// wipe out the source folder from the classpath.
		javaProj.setRawClasspath(cp, null);
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		// classpath should be updated with an entry for the source folder
		// make sure we do not post the marker about the incorrect classpath
		expectingNoMarkers();

		// take out the annotation and no type generation will occur.
		code = "package pkg;\n"
			+ "\npublic class Foo{\n"
			+ "    public static void main( String[] argv ){}"
			+ "\n}";

		env.addClass( srcRoot, "pkg", "Foo", code );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		// Make sure we cleaned out config marker from previous build
		// We don't need to generate types, hence we should not register the config marker
		expectingNoMarkers();
	}

	public void testDeletedGeneratedSourceFolder()
		throws Exception
	{
		final String projectName = "DeleteGenSourceFolderTestProject";
		final IJavaProject javaProj = createJavaProject( projectName );
		IProject project = env.getProject( projectName );
		IPath srcRoot = getSourcePath( projectName );
		// this will cause a type generation.
		String code = "package pkg;\n"
			+ "import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;"
			+ "\npublic class Foo{\n"
			+ "    @HelloWorldAnnotation\n"
			+ "    public static void main( String[] argv ){}"
			+ "\n}";

		env.addClass( srcRoot, "pkg", "Foo", code );
		AptConfig.setEnabled(javaProj, true);
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		expectingNoMarkers();

		GeneratedSourceFolderManager mgr = AptPlugin.getAptProject(javaProj).getGeneratedSourceFolderManager(false);
		IFolder srcFolder = mgr.getFolder();
		assertEquals(true, srcFolder.exists());
		// delete the gen source folder
		Util.delete(srcFolder);
		assertEquals(false, srcFolder.exists());

		// we would have re-created the folder on the next build
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		expectingNoMarkers();
	}

}
