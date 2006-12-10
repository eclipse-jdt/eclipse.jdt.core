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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.tests.model.ModifyingResourceTests;

public class AptReconcileTests extends ModifyingResourceTests
{
	public AptReconcileTests(String name)
	{
		super( name );
	}
	
	public static Test suite() {
		return new TestSuite(AptReconcileTests.class);
	}
	
	@SuppressWarnings("nls")
	public void testGeneratedFile() throws Throwable
	{
		String fname = _testFolder + "/A.java";
		try
		{
			
			//
			//  first make sure errors are present when the annotation
			// is commented out
			//
			String codeWithErrors = "package test;" + "\n" +
				"//import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;" + "\n" + 
				"public class A " +  "\n" +
				"{" +  "\n" +
				"    //@HelloWorldAnnotation" + "\n" + 
				"    public static void main( String[] argv )" + "\n" +
				"    {" + "\n" +
				"        generatedfilepackage.GeneratedFileTest.helloWorld();" + "\n" +
				"    }" + "\n" +
				"}";

			createFile( fname, codeWithErrors );
			this._problemRequestor = new ProblemRequestor();
			
			setUpWorkingCopy( fname, codeWithErrors );
			this._workingCopy.reconcile( ICompilationUnit.NO_AST, true, null,
				null );
			
			String expectedProblems = "----------\n" + 
				"1. ERROR in /" + _testProject + "/src/test/A.java (at line 8)\n" + 
				"	generatedfilepackage.GeneratedFileTest.helloWorld();\n" + 
				"	^^^^^^^^^^^^^^^^^^^^\n" + 
				"generatedfilepackage cannot be resolved\n" + 
				"----------\n" + 
				"----------\n" + 
				"2. ERROR in /" + _testProject + "/src/test/A.java (at line 8)\n" + 
				"	generatedfilepackage.GeneratedFileTest.helloWorld();\n" + 
				"	^^^^^^^^^^^^^^^^^^^^\n" + 
				"generatedfilepackage cannot be resolved\n" + 
				"----------\n";
			
			assertProblems( "Unexpected problems", expectedProblems );
			
			
			//
			// now make sure errors go away when annotations are present
			//
			String codeWithOutErrors = "package test;" + "\n" +
			    "import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;" + "\n" + 
			    "public class A " +  "\n" +
			    "{" +  "\n" +
			    "    @HelloWorldAnnotation" + "\n" + 
				"    public static void main( String[] argv )" + "\n" +
				"    {" + "\n" +
				"        generatedfilepackage.GeneratedFileTest.helloWorld();" + "\n" +
				"    }" + "\n" +
				"}";

			setWorkingCopyContents( codeWithOutErrors );
			this._workingCopy.reconcile( ICompilationUnit.NO_AST, true, null,
					null );
			
			assertProblems( "UnexpectedProblems", "----------\n----------\n" );
			
		}
		catch( Throwable e )
		{
			e.printStackTrace();
			throw e;
		}
		finally
		{
			deleteFile( fname );
		}
	}

	
	/**
	 *   This tests an annotation that generates a file with an annotation that
	 *   generates a file that should fix an error in the parent file.
	 *   TODO: re-enable this test - it seems sporadically flaky, need to find out why.
	 * @throws Throwable
	 */
	@SuppressWarnings("nls")
	public void testNestedGeneratedFile() throws Throwable
	{
		String fname = _testFolder + "/A.java";
		try
		{
			
			//
			//  first make sure errors are present when the annotation
			// is commented out
			//
			String codeWithErrors = "package test;" + "\n" +
				"//import org.eclipse.jdt.apt.tests.annotations.nestedhelloworld.NestedHelloWorldAnnotation;" + "\n" + 
				"public class A " +  "\n" +
				"{" +  "\n" +
				"    //@NestedHelloWorldAnnotation" + "\n" + 
				"    public static void main( String[] argv )" + "\n" +
				"    {" + "\n" +
				"        generatedfilepackage.GeneratedFileTest.helloWorld();" + "\n" +
				"    }" + "\n" +
				"}";

			createFile( fname, codeWithErrors );
			this._problemRequestor = new ProblemRequestor();
			
			setUpWorkingCopy( fname, codeWithErrors );
			this._workingCopy.reconcile( ICompilationUnit.NO_AST, true, null,
				null );
			
			String expectedProblems = "----------\n" + 
				"1. ERROR in /" + _testProject + "/src/test/A.java (at line 8)\n" + 
				"	generatedfilepackage.GeneratedFileTest.helloWorld();\n" + 
				"	^^^^^^^^^^^^^^^^^^^^\n" + 
				"generatedfilepackage cannot be resolved\n" + 
				"----------\n" + 
				"----------\n" + 
				"2. ERROR in /" + _testProject + "/src/test/A.java (at line 8)\n" + 
				"	generatedfilepackage.GeneratedFileTest.helloWorld();\n" + 
				"	^^^^^^^^^^^^^^^^^^^^\n" + 
				"generatedfilepackage cannot be resolved\n" + 
				"----------\n";
			
			assertProblems( "Unexpected problems", expectedProblems );
			
			
			//
			// now make sure errors go away when annotations are present
			//
			String codeWithOutErrors = "package test;" + "\n" +
			    "import org.eclipse.jdt.apt.tests.annotations.nestedhelloworld.NestedHelloWorldAnnotation;" + "\n" + 
			    "public class A " +  "\n" +
			    "{" +  "\n" +
			    "    @NestedHelloWorldAnnotation" + "\n" + 
				"    public static void main( String[] argv )" + "\n" +
				"    {" + "\n" +
				"        generatedfilepackage.GeneratedFileTest.helloWorld();" + "\n" +
				"    }" + "\n" +
				"}";

			setWorkingCopyContents( codeWithOutErrors );
			this._workingCopy.reconcile( ICompilationUnit.NO_AST, true, null,
					null );
			
			assertProblems( "UnexpectedProblems", "----------\n----------\n" );
			
		}
		catch( Throwable e )
		{
			e.printStackTrace();
			throw e;
		}
		finally
		{
			deleteFile( fname );
		}
	}


	@SuppressWarnings("nls")
	public void testStopGeneratingFileInReconciler() throws Exception
	{
		String fname = _testFolder + "/A.java";
		try
		{
			
			//
			//  first make sure errors are present when the annotation
			// is commented out
			//
			String codeWithErrors = "package test;" + "\n" +
				"//import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;" + "\n" + 
				"public class A " +  "\n" +
				"{" +  "\n" +
				"    //@HelloWorldAnnotation" + "\n" + 
				"    public static void main( String[] argv )" + "\n" +
				"    {" + "\n" +
				"        generatedfilepackage.GeneratedFileTest.helloWorld();" + "\n" +
				"    }" + "\n" +
				"}";

			createFile( fname, codeWithErrors );
			this._problemRequestor = new ProblemRequestor();
				
			setUpWorkingCopy( fname, codeWithErrors );
			this._workingCopy.reconcile( ICompilationUnit.NO_AST, true, null,
				null );
				
			String expectedProblems = "----------\n" + 
				"1. ERROR in /" + _testProject + "/src/test/A.java (at line 8)\n" + 
				"	generatedfilepackage.GeneratedFileTest.helloWorld();\n" + 
				"	^^^^^^^^^^^^^^^^^^^^\n" + 
				"generatedfilepackage cannot be resolved\n" + 
				"----------\n" + 
				"----------\n" + 
				"2. ERROR in /" + _testProject + "/src/test/A.java (at line 8)\n" + 
				"	generatedfilepackage.GeneratedFileTest.helloWorld();\n" + 
				"	^^^^^^^^^^^^^^^^^^^^\n" + 
				"generatedfilepackage cannot be resolved\n" + 
				"----------\n";
				
			assertProblems( "Unexpected problems", expectedProblems );
					
			//
			// now make sure errors go away when annotations are present
			//
			String codeWithOutErrors = "package test;" + "\n" +
			    "import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;" + "\n" + 
			    "public class A " +  "\n" +
			    "{" +  "\n" +
			    "    @HelloWorldAnnotation" + "\n" + 
				"    public static void main( String[] argv )" + "\n" +
				"    {" + "\n" +
				"        generatedfilepackage.GeneratedFileTest.helloWorld();" + "\n" +
				"    }" + "\n" +
				"}";

			setWorkingCopyContents( codeWithOutErrors );
			this._workingCopy.reconcile( ICompilationUnit.NO_AST, true, null,
					null );
				
			assertProblems( "UnexpectedProblems", "----------\n----------\n" );
			
			//
			// now make sure errors come back when annotations are taken away
			//
			setWorkingCopyContents( codeWithErrors );
			this._workingCopy.reconcile( ICompilationUnit.NO_AST, true, null,
					null );
				
			String expectedProblems2 = 	"----------\n" + 
				"1. ERROR in /" + _testProject + "/src/test/A.java (at line 8)\n" + 
				"	generatedfilepackage.GeneratedFileTest.helloWorld();\n" + 
				"	^^^^^^^^^^^^^^^^^^^^\n" + 
				"generatedfilepackage cannot be resolved\n" + 
				"----------\n";
			assertProblems( "Unexpected problems", expectedProblems2 );
		}
		catch (Exception e )
		{
			e.printStackTrace();
			throw e;
		}
	}

	/**
	 * Tests that when a working copy is discarded, we clean up any cached data in the
	 * GeneratedFileManager.
	 */
	@SuppressWarnings("nls")	
	public void testDiscardParentWorkingCopy()
	 	throws Throwable
	{
		String fname = _testFolder + "/A.java";
		try
		{
			String codeWithOutErrors = "package test;" + "\n" +
			    "import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;" + "\n" + 
			    "public class A " +  "\n" +
			    "{" +  "\n" +
			    "    @HelloWorldAnnotation" + "\n" + 
				"    public static void main( String[] argv )" + "\n" +
				"    {" + "\n" +
				"        generatedfilepackage.GeneratedFileTest.helloWorld();" + "\n" +
				"    }" + "\n" +
				"}";

			createFile( fname, codeWithOutErrors );
			this._problemRequestor = new ProblemRequestor();
			setUpWorkingCopy( fname, codeWithOutErrors );
			
			// use new problem requestor to remove any errors that occurred in setUpWorkingCopy()
			this._problemRequestor = new ProblemRequestor();
			this._workingCopy.reconcile( ICompilationUnit.NO_AST, true, null,
				null );
			
			assertProblems( "UnexpectedProblems", "" );
			
			IJavaProject jp = _workingCopy.getJavaProject();
			GeneratedFileManager gfm = AptPlugin.getAptProject(jp).getGeneratedFileManager();
			
			if ( !gfm.containsWorkingCopyMapEntriesForParent((IFile)_workingCopy.getResource()))
				fail( "Expected to find map entries in GeneratedFileManager");
			
			_workingCopy.discardWorkingCopy();

			if ( gfm.containsWorkingCopyMapEntriesForParent( (IFile)_workingCopy.getResource() ) )
				fail( "Unexpected map entries in GeneratedFileManager!");
		}
		finally
		{
			deleteFile( fname );
		}	
	}
	
	public void testBasicReconcile() throws Exception {
		String fname = _testFolder + "/X.java";
		try
		{
			
			String code = "package test;" + "\n" +
			    "@org.eclipse.jdt.apt.tests.annotations.apitest.Common\n" + 
				"public class X " +  "\n" +
				"{" +  "\n" +
				"    public static void main( String[] argv )" + "\n" +
				"    {" + "\n" +
				"    }" + "\n" +
				"}";

			createFile( fname, code );
			this._problemRequestor = new ProblemRequestor();
			
			setUpWorkingCopy( fname, code );
			this._workingCopy.reconcile( ICompilationUnit.NO_AST, true, null,
				null );
			
			assertProblems( "UnexpectedProblems", "----------\n----------\n----------\n----------\n" );
			
		}
		finally
		{
			deleteFile( fname );
		}
		
	}

	@SuppressWarnings("nls")
	public void setUp() throws Exception 
	{
		// This increments the project name for each test, which helps get past
		// sporadic threading problems.  It has not been necessary lately - WHarley 12/06
		//++_testProjectNum;
		_testProject = TEST_PROJECT + _testProjectNum;
		_testFolder = "/" + _testProject + "/src/test";
		AptPlugin.trace("Setting up " + _testProject );
		
		super.setUp();			
		// disable auto-build.  We don't want build-time type-generation interfering with
		// our reconcile tests.
		String key = ResourcesPlugin.PREF_AUTO_BUILDING;
		boolean value = false;
		ResourcesPlugin.getPlugin().getPluginPreferences().setValue(key, value);

		this._problemRequestor = new ProblemRequestor();

		final IJavaProject project = createJavaProject( _testProject,
				new String[] { "src" }, new String[] { "JCL15_LIB" },
				"bin", "1.5" );
		TestUtil.createAndAddAnnotationJar(project);
		AptConfig.setEnabled(project, true);
		
		createFolder( _testFolder );
		
	}
	public void tearDown() throws Exception
	{
		AptPlugin.trace("Tearing down " + _testProject );

		deleteProject( _testProject );
		super.tearDown();
	}
	
	/***************************************************************************
	 * 
	 * copied from ReconcilerTests...
	 * 
	 */

	private void setWorkingCopyContents(String contents)
		throws JavaModelException
	{
		this._workingCopy.getBuffer().setContents( contents );
		this._problemRequestor.initialize( contents.toCharArray() );
	}

	private void setUpWorkingCopy(String path, String contents)
		throws JavaModelException
	{
		if( this._workingCopy != null )
			this._workingCopy.discardWorkingCopy();
		this._workingCopy = getCompilationUnit( path ).getWorkingCopy(
			new WorkingCopyOwner()
			{}, this._problemRequestor, null );
		setWorkingCopyContents( contents );
		this._workingCopy.makeConsistent( null );
	}

	protected void assertProblems(String message, String expected)
	{
		assertProblems( message, expected, this._problemRequestor );
	}

	/** *********************************************************************** */


	protected ICompilationUnit	_workingCopy;

	protected ProblemRequestor	_problemRequestor;
	
	private static String _testProject;
	
	private static String _testFolder;
	
	private static int _testProjectNum = 0;

	private static final String	TEST_PROJECT	= AptReconcileTests.class.getName() + "Project"; //$NON-NLS-1$
	

}
