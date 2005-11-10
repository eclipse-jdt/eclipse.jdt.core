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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
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
		String fname = TEST_FOLDER + "/A.java";
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
				"1. ERROR in /org.eclipse.jdt.apt.tests.AptReconcileTestsProject/src/test/A.java (at line 8)\n" + 
				"	generatedfilepackage.GeneratedFileTest.helloWorld();\n" + 
				"	^^^^^^^^^^^^^^^^^^^^\n" + 
				"generatedfilepackage cannot be resolved\n" + 
				"----------\n" + 
				"----------\n" + 
				"2. ERROR in /org.eclipse.jdt.apt.tests.AptReconcileTestsProject/src/test/A.java (at line 8)\n" + 
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
	 * @throws Throwable
	 */
	// This stopped working when reconcile changed to be in-memory only.  We should investigate why this stopped 
	// (I think it is consistent with the reconcile/build model, just need to convince myself it is true.) 
	@SuppressWarnings("nls")
	public void disabled_testNestedGeneratedFile() throws Throwable
	{
		String fname = TEST_FOLDER + "/A.java";
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
				"1. ERROR in /org.eclipse.jdt.apt.tests.AptReconcileTestsProject/src/test/A.java (at line 8)\n" + 
				"	generatedfilepackage.GeneratedFileTest.helloWorld();\n" + 
				"	^^^^^^^^^^^^^^^^^^^^\n" + 
				"generatedfilepackage cannot be resolved\n" + 
				"----------\n" + 
				"----------\n" + 
				"2. ERROR in /org.eclipse.jdt.apt.tests.AptReconcileTestsProject/src/test/A.java (at line 8)\n" + 
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
		String fname = TEST_FOLDER + "/A.java";
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
				"1. ERROR in /org.eclipse.jdt.apt.tests.AptReconcileTestsProject/src/test/A.java (at line 8)\n" + 
				"	generatedfilepackage.GeneratedFileTest.helloWorld();\n" + 
				"	^^^^^^^^^^^^^^^^^^^^\n" + 
				"generatedfilepackage cannot be resolved\n" + 
				"----------\n" + 
				"----------\n" + 
				"2. ERROR in /org.eclipse.jdt.apt.tests.AptReconcileTestsProject/src/test/A.java (at line 8)\n" + 
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
				
			// error will be different from first one because the package will
			// exist since we only removed the file. 
			String expectedProblems2 = 	"----------\n" + 
				"1. ERROR in /org.eclipse.jdt.apt.tests.AptReconcileTestsProject/src/test/A.java (at line 8)\n" + 
				"	generatedfilepackage.GeneratedFileTest.helloWorld();\n" + 
				"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
				"generatedfilepackage.GeneratedFileTest cannot be resolved to a type\n" + 
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
		String fname = TEST_FOLDER + "/A.java";
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
			
			IProject p = _workingCopy.getJavaProject().getProject();
			GeneratedFileManager gfm = GeneratedFileManager.getGeneratedFileManager( p );
			
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

	@SuppressWarnings("nls")
	public void setUp() throws Exception 
	{
		try 
		{
			super.setUp();

			this._problemRequestor = new ProblemRequestor();

			final IJavaProject project = createJavaProject( TEST_PROJECT,
					new String[] { "src" }, new String[] { "JCL15_LIB" },
					"bin", "1.5" );
			project.getProject().refreshLocal( IResource.DEPTH_INFINITE, null );
			
			// make sure generated source folder exists in the project.  This is necessary
			// for reconcile-time type-generation to work
			GeneratedFileManager.getGeneratedFileManager( project.getProject() );
			
			_classesJarFile = TestUtil.createAndAddAnnotationJar( project );

			IFolder testFolder = createFolder( TEST_FOLDER );
			if ( !testFolder.exists() )
				testFolder.create( true, false, null );
			
			// disable auto-build.  We don't want build-time type-generation interfering with
			// our reconcile tests.
			String key = ResourcesPlugin.PREF_AUTO_BUILDING;
			boolean value = false;
			ResourcesPlugin.getPlugin().getPluginPreferences().setValue(key, value);
		} 
		catch ( Exception t ) 
		{
			t.printStackTrace();
			throw t;
		} 
		catch ( Throwable t ) 
		{
			t.printStackTrace();
			throw new RuntimeException( t ); 
		}
	}

	public void tearDown() throws Exception
	{
		if( this._workingCopy != null )
			this._workingCopy.discardWorkingCopy();

		deleteFile( _classesJarFile );
		deleteFolder( TEST_FOLDER );
		deleteProject( TEST_PROJECT );
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

	public static final String	TEST_PROJECT	= AptReconcileTests.class.getName() + "Project"; //$NON-NLS-1$
	
	public static final String	TEST_FOLDER		= "/" + TEST_PROJECT //$NON-NLS-1$
													+ "/src/test"; //$NON-NLS-1$
	
	private File				_classesJarFile;

}
