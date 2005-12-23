/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;
import org.eclipse.jdt.core.tests.util.Util;

public class AnnotationValueConversionTests extends APTTestBase 
{
	private int counter = 0;
	private String projectName = null;
	public AnnotationValueConversionTests(final String name) {
		super( name );
	}

	public static Test suite() {
		return new TestSuite( AnnotationValueConversionTests.class );
	}
	
	public String getUniqueProjectName(){
		projectName = AnnotationValueConversionTests.class.getName() + "Project" + counter; //$NON-NLS-1$
		counter ++;
		return projectName;
	}
	

	public IPath getSourcePath() {
		IProject project = env.getProject( getProjectName() );
		IFolder srcFolder = project.getFolder( "src" ); //$NON-NLS-1$
		IPath srcRoot = srcFolder.getFullPath();
		return srcRoot;
	}
	
	public IPath getBinaryPath(){
		IProject project = env.getProject( getProjectName() );
		IFolder srcFolder = project.getFolder( "binary" ); //$NON-NLS-1$
		IPath lib = srcFolder.getFullPath();
		return lib;
	}
	
	public IPath getOutputPath(){
		IProject project = env.getProject( getProjectName() );
		IFolder binFolder = project.getFolder( "bin" ); //$NON-NLS-1$
		IPath bin = binFolder.getFullPath();
		return bin;
	}
	
	private IProject setupTest() throws Exception
	{				
		ProcessorTestStatus.reset();
		// project will be deleted by super-class's tearDown() method
		IPath projectPath = env.addProject( getUniqueProjectName(), "1.5" ); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$ 
		env.addExternalJars( projectPath, Util.getJavaClassLibs() );
		fullBuild( projectPath );

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot( projectPath, "" ); //$NON-NLS-1$

		env.addPackageFragmentRoot( projectPath, "src" ); //$NON-NLS-1$
		return env.getProject(getProjectName());
	}
	
	/**
	 * Test conversion on byte. 
	 * @throws Exception
	 */
	@SuppressWarnings("nls")
	public void testByteConversion() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		String content =  
			"package sample; \n\n" + 
			"@Test.Annotation(b=(byte)0,\n" +
			"				  s=(byte)0,\n" + 
			"				  i=(byte)0,\n" +
			"                 l=(byte)0,\n" +
			"	 	 	      f=(byte)0,\n" +
			"			      d=(byte)0,\n" +
			"				  c=(byte)0)\n\n" + 
			"public class Test {\n" +
			"	public @interface Annotation\n" +
			"	{\n" +
			"		char c();  \n" +
			"       byte b();  \n" +
			"		short s(); \n" +
			"		int i();   \n" +
			"       long l();  \n" +
			"		float f(); \n" +
			"		double d();\n" +
			"	}\n" +	
			"}\n";
		
		env.addClass( 
				srcRoot, 
				"sample", 
				"Test",
				content );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	
	/**
	 * Test conversion on short. 
	 * @throws Exception
	 */
	@SuppressWarnings("nls")
	public void testShortConversion() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"@Test.Annotation(b=(short)0,\n" +
			"				  s=(short)0,\n" + 
			"				  i=(short)0,\n" +
			"                 l=(short)0,\n" +
			"	 	 	      f=(short)0,\n" +
			"			      d=(short)0,\n" +
			"				  c=(short)0)\n\n" + 
			"public class Test {\n" +
			"	public @interface Annotation\n" +
			"	{\n" +
			"		char c();  \n" +
			"       byte b();  \n" +
			"		short s(); \n" +
			"		int i();   \n" +
			"       long l();  \n" +
			"		float f(); \n" +
			"		double d();\n" +
			"	}\n" +	
			"}\n";
		
		env.addClass( 
				srcRoot, 
				"sample", 
				"Test",
				content );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	
	/**
	 * Test conversion on char. 
	 * @throws Exception
	 */
	@SuppressWarnings("nls")
	public void testCharConversion() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"@Test.Annotation(b=(char)0,\n" +
			"				  s=(char)0,\n" + 
			"				  i=(char)0,\n" +
			"                 l=(char)0,\n" +
			"	 	 	      f=(char)0,\n" +
			"			      d=(char)0,\n" +
			"				  c=(char)0)\n\n" + 
			"public class Test {\n" +
			"	public @interface Annotation\n" +
			"	{\n" +
			"		char c();  \n" +
			"       byte b();  \n" +
			"		short s(); \n" +
			"		int i();   \n" +
			"       long l();  \n" +
			"		float f(); \n" +
			"		double d();\n" +
			"	}\n" +
			"}\n";
		
		env.addClass( 
				srcRoot, 
				"sample", 
				"Test",
				content );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	
	/**
	 * Test conversion on int. 
	 * @throws Exception
	 */
	@SuppressWarnings("nls")
	public void testIntConversion() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"@Test.Annotation(b=(int)0,\n" +
			"				  s=(int)0,\n" + 
			"				  i=(int)0,\n" +
			"                 l=(int)0,\n" +
			"	 	 	      f=(int)0,\n" +
			"			      d=(int)0,\n" +
			"				  c=(int)0)\n\n" + 
			"public class Test {\n" +
			"	public @interface Annotation\n" +
			"	{\n" +
			"		char c();  \n" +
			"       byte b();  \n" +
			"		short s(); \n" +
			"		int i();   \n" +
			"       long l();  \n" +
			"		float f(); \n" +
			"		double d();\n" +
			"	}\n" +
			"}\n";
		
		env.addClass( 
				srcRoot, 
				"sample", 
				"Test",
				content );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	
	/**
	 * Test conversion on long. 
	 * @throws Exception
	 */
	@SuppressWarnings("nls")
	public void testLongConversion() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"@Test.Annotation(l=(long)0,\n" +			
			"	 	 	      f=(long)0,\n" +
			"			      d=(long)0)\n" +
			"public class Test {\n" +
			"	public @interface Annotation\n" +
			"	{\n" +		
			"       long l();  \n" +
			"		float f(); \n" +
			"		double d();\n" +
			"	}\n" +
			"}\n";
		
		env.addClass( 
				srcRoot, 
				"sample", 
				"Test",
				content );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	
	/**
	 * Test conversion on Float. 
	 * @throws Exception
	 */
	@SuppressWarnings("nls")
	public void testFloatConversion() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"@Test.Annotation(f=(float)0,\n" +
			"			      d=(float)0)\n" +
			"public class Test {\n" +
			"	public @interface Annotation\n" +
			"	{\n" +		
			"		float f(); \n" +
			"		double d();\n" +
			"	}\n" +
			"}\n";
		
		env.addClass( 
				srcRoot, 
				"sample", 
				"Test",
				content );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	
	/**
	 * Test conversion on double. No arrayification.
	 * @throws Exception
	 */
	@SuppressWarnings("nls")
	public void testDoubleConversion() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"@Test.Annotation(d=(double)0)\n" +			 
			"public class Test {\n" +
			"	public @interface Annotation\n" +
			"	{\n" +		
			"		double d();\n" +
			"	}\n" +
			"}\n";
		
		env.addClass( 
				srcRoot, 
				"sample", 
				"Test",
				content );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}	
	
	/**
	 * Convert "singleton" instance to array of the correct type
	 * @throws Exception
	 */
	@SuppressWarnings("nls")
	public void testArrayification() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"@Test.Annotation(bytes=(byte)0,\n" +
			"				  shorts=(byte)0,\n" + 
			"				  ints=(byte)0,\n" +
			"                 longs=(byte)0,\n" +
			"	 	 	      floats=(byte)0,\n" +
			"			      doubles=(byte)0,\n" +
			"				  chars=(byte)0)\n\n" + 
			"public class Test {\n" +
			"	public @interface Annotation\n" +
			"	{\n" +
			"		char[]   chars(); \n" +
			"       byte[]   bytes(); \n" +
			"		short[]  shorts();\n" +
			"		int[]    ints();  \n" +
			"       long[]   longs(); \n" +
			"		float[]  floats();\n" +
			"		double[] doubles();\n" +
			"	}\n" +
			"}\n";
		
		env.addClass( 
				srcRoot, 
				"sample", 
				"Test",
				content );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}	
	
	/**
	 * Test conversion on double.
	 * @throws Exception
	 */
	@SuppressWarnings("nls")
	public void testArrayElementConversion() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"@Test.Annotation(bytes=  {(byte)0,(byte)1}, \n" +
			"				  shorts= {(byte)0,(byte)1},\n" + 
			"				  ints=   {(byte)0,(byte)1},\n" +
			"                 longs=  {(byte)0,(byte)1},\n" +
			"	 	 	      floats= {(byte)0,(byte)1},\n" +
			"			      doubles={(byte)0,(byte)1},\n" +
			"				  chars=  {(byte)0,(byte)1})\n\n" + 
			"public class Test {\n" +
			"	public @interface Annotation\n" +
			"	{\n" +
			"		char[]   chars(); \n" +
			"       byte[]   bytes(); \n" +
			"		short[]  shorts();\n" +
			"		int[]    ints();  \n" +
			"       long[]   longs(); \n" +
			"		float[]  floats();\n" +
			"		double[] doubles();\n" +
			"	}\n" +
			"}\n";
		
		env.addClass( 
				srcRoot, 
				"sample", 
				"Test",
				content );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}	
}
