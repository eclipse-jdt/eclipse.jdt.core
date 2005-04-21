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
import org.eclipse.core.resources.IResource;
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
	
	public void testGeneratedFile() throws Throwable
	{
		String fname = TEST_FOLDER + "/A.java";
		try
		{
			String code = "package generatedfilepackage;"
				+ "import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;" + "\n" + 
				"public class A " +  "\n" +
				"{" +  "\n" +
				"    @HelloWorldAnnotation" + "\n" + 
				"    public static void main( String[] argv )" + "\n" +
				"    {" + "\n" +
				"        GeneratedFileTest.helloWorld();" + "\n" +
				"    }" + "\n" +
				"}";

			createFile( fname, code );

			setUpWorkingCopy( fname, code );
			this._problemRequestor = new ProblemRequestor();
			
			this._workingCopy.reconcile( ICompilationUnit.NO_AST, true, null,
				null );
			assertProblems( "Unexpected problems", "" );
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
			
			_classesJarFile = TestUtil.createAndAddAnnotationJar( project );

			IFolder testFolder = createFolder( TEST_FOLDER );
			if ( !testFolder.exists() )
				testFolder.create( true, false, null );
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

	public static final String	TEST_PROJECT	= "AptTestProject";

	public static final String	TEST_FOLDER		= "/" + TEST_PROJECT
													+ "/src/test";
	
	private File				_classesJarFile;

}
