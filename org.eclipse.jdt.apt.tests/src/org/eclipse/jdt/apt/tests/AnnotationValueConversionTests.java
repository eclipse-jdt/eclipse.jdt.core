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
import org.eclipse.jdt.core.tests.builder.Problem;
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
	
	@SuppressWarnings("nls")
	public void testByteConversion() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		String content =  
			"package sample; \n\n" + 
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.Annotation;\n" +
			"@Annotation(z=(byte)49,\n" +
			"				  b=(byte)49,\n" +
			"				  s=(byte)49,\n" + 
			"				  i=(byte)49,\n" +
			"                 l=(byte)49,\n" +
			"	 	 	      f=(byte)49,\n" +
			"			      d=(byte)49,\n" +
			"				  c=(byte)49)\n\n" + 
			"public class Test {}\n";
		
		IPath testPath = env.addClass( srcRoot, "sample", "Test", content );
		fullBuild( project.getFullPath() );
		expectingSpecificProblemsFor(testPath, new Problem[]{
				new Problem("", "Type mismatch: cannot convert from byte to boolean", testPath),
				new Problem("", "type mismatch for memeber z expected java.lang.Boolean but got java.lang.Byte", testPath)
		});
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	
	@SuppressWarnings("nls")
	public void testByteConversion_Reflection() throws Exception {
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		String content =  
			"package sample; \n\n" + 
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.RefAnnotation;\n" +
			"@RefAnnotation(z=(byte)49,\n" +
			"				  b=(byte)49,\n" +
			"				  s=(byte)49,\n" + 
			"				  i=(byte)49,\n" +
			"                 l=(byte)49,\n" +
			"	 	 	      f=(byte)49,\n" +
			"			      d=(byte)49,\n" +
			"				  c=(byte)49)\n\n" + 
			"public class Test {}\n";
		
		IPath testPath = env.addClass( srcRoot, "sample", "Test", content );
		fullBuild( project.getFullPath() );
		expectingSpecificProblemsFor(testPath, new Problem[]{
				new Problem("", "Type mismatch: cannot convert from byte to boolean", testPath),
				new Problem("", "value mismatch for memeber z expected true but got false", testPath)
		});
		
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
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.Annotation;\n" +
			"@Annotation(z=(short)49,\n" +
			"				  b=(short)49,\n" +
			"				  s=(short)49,\n" + 
			"				  i=(short)49,\n" +
			"                 l=(short)49,\n" +
			"	 	 	      f=(short)49,\n" +
			"			      d=(short)49,\n" +
			"				  c=(short)49)\n\n" + 
			"public class Test {}\n";
		
		IPath testPath = env.addClass( srcRoot, "sample", "Test", content );
		fullBuild( project.getFullPath() );
		expectingSpecificProblemsFor(testPath, new Problem[]{
				new Problem("", "Type mismatch: cannot convert from short to boolean", testPath),
				new Problem("", "type mismatch for memeber z expected java.lang.Boolean but got java.lang.Short", testPath)
		});
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	
	@SuppressWarnings("nls")
	public void testShortConversion_Reflection() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.RefAnnotation;\n" +
			"@RefAnnotation(z=(short)49,\n"+
			"				  b=(short)49,\n" +
			"				  s=(short)49,\n" + 
			"				  i=(short)49,\n" +
			"                 l=(short)49,\n" +
			"	 	 	      f=(short)49,\n" +
			"			      d=(short)49,\n" +
			"				  c=(short)49)\n\n" + 
			"public class Test {}\n";
		
		IPath testPath = env.addClass( srcRoot, "sample", "Test", content );
		fullBuild( project.getFullPath() );
		expectingSpecificProblemsFor(testPath, new Problem[]{
				new Problem("", "Type mismatch: cannot convert from short to boolean", testPath),
				new Problem("", "value mismatch for memeber z expected true but got false", testPath)
		});
		
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
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.Annotation;\n" +
			"@Annotation(z='1',\n" +
			"                 b='1',\n" +
			"				  s='1',\n" + 
			"				  i='1',\n" +
			"                 l='1',\n" +
			"	 	 	      f='1',\n" +
			"			      d='1',\n" +
			"				  c='1')\n\n" + 
			"public class Test {}\n";
		
		IPath testPath = env.addClass( srcRoot, "sample", "Test", content );
		fullBuild( project.getFullPath() );
		expectingSpecificProblemsFor(testPath, new Problem[]{
				new Problem("", "Type mismatch: cannot convert from char to boolean", testPath),
				new Problem("", "type mismatch for memeber z expected java.lang.Boolean but got java.lang.Character", testPath)
		});
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	
	/**
	 * Test conversion on char through reflection
	 * @throws Exception
	 */
	@SuppressWarnings("nls")
	public void testCharConversion_Reflection() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.RefAnnotation;\n" +
			"@RefAnnotation(z='1',\n" +
			"				  b='1',\n" +
			"				  s='1',\n" + 
			"				  i='1',\n" +
			"                 l='1',\n" +
			"	 	 	      f='1',\n" +
			"			      d='1',\n" +
			"				  c='1')\n\n" + 
			"public class Test {}\n";
		
		IPath testPath = env.addClass( srcRoot, "sample", "Test", content );
		fullBuild( project.getFullPath() );
		expectingSpecificProblemsFor(testPath, new Problem[]{
				new Problem("", "Type mismatch: cannot convert from char to boolean", testPath),
				new Problem("", "value mismatch for memeber z expected true but got false", testPath)
		});
		
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
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.Annotation;\n" +
			"@Annotation(z=49,\n" +
			"				  b=49,\n" +
			"				  s=49,\n" + 
			"				  i=49,\n" +
			"                 l=49,\n" +
			"	 	 	      f=49,\n" +
			"			      d=49,\n" +
			"				  c=49)\n\n" + 
			"public class Test {}\n";
		
		IPath testPath = env.addClass( srcRoot, "sample", "Test", content );
		fullBuild( project.getFullPath() );
		expectingSpecificProblemsFor(testPath, new Problem[]{
				new Problem("", "Type mismatch: cannot convert from int to boolean", testPath),
				new Problem("", "type mismatch for memeber z expected java.lang.Boolean but got java.lang.Integer", testPath)
		});
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	
	/**
	 * Test conversion on int through reflection
	 * @throws Exception
	 */
	@SuppressWarnings("nls")
	public void testIntConversion_Reflection() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.RefAnnotation;\n" +
			"@RefAnnotation(z=49,\n" +
			"				  b=49,\n" +
			"				  s=49,\n" + 
			"				  i=49,\n" +
			"                 l=49,\n" +
			"	 	 	      f=49,\n" +
			"			      d=49,\n" +
			"				  c=49)\n\n" + 
			"public class Test {}\n";
		
		IPath testPath = env.addClass( srcRoot, "sample", "Test", content );
		fullBuild( project.getFullPath() );
		expectingSpecificProblemsFor(testPath, new Problem[]{
				new Problem("", "Type mismatch: cannot convert from int to boolean", testPath),
				new Problem("", "value mismatch for memeber z expected true but got false", testPath)
		});
		
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
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.Annotation;\n" +
			"@Annotation(z=49l,\n" +
			"				  c=49l,\n" +
			"				  b=49l,\n" +
			"				  s=49l,\n" + 
			"				  i=49l,\n" +
			"				  l=49l,\n" +			
			"	 	 	      f=49l,\n" +
			"			      d=49l)\n" +
			"public class Test {}\n";
		
		IPath testPath = env.addClass( srcRoot, "sample", "Test", content );
		fullBuild( project.getFullPath() );
		expectingSpecificProblemsFor(testPath, new Problem[]{
				new Problem("", "Type mismatch: cannot convert from long to boolean", testPath),
				new Problem("", "Type mismatch: cannot convert from long to char", testPath),
				new Problem("", "Type mismatch: cannot convert from long to byte", testPath),
				new Problem("", "Type mismatch: cannot convert from long to short", testPath),
				new Problem("", "Type mismatch: cannot convert from long to int", testPath),
				new Problem("", "type mismatch for memeber z expected java.lang.Boolean but got java.lang.Long", testPath),
				new Problem("", "type mismatch for memeber s expected java.lang.Short but got java.lang.Long", testPath),
				new Problem("", "type mismatch for memeber i expected java.lang.Integer but got java.lang.Long", testPath),
				new Problem("", "type mismatch for memeber c expected java.lang.Character but got java.lang.Long", testPath),
				new Problem("", "type mismatch for memeber b expected java.lang.Byte but got java.lang.Long", testPath),
		});
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	
	/**
	 * Test conversion on long. 
	 * @throws Exception
	 */
	@SuppressWarnings("nls")
	public void testLongConversion_Reflection() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.RefAnnotation;\n" +
			"@RefAnnotation(z=49l,\n" +
			"				  c=49l,\n" +
			"				  b=49l,\n" +
			"				  s=49l,\n" + 
			"				  i=49l,\n" +
			"				  l=49l,\n" +			
			"	 	 	      f=49l,\n" +
			"			      d=49l)\n" +
			"public class Test {}\n";
		
		IPath testPath = env.addClass( srcRoot, "sample", "Test", content );
		fullBuild( project.getFullPath() );
		expectingSpecificProblemsFor(testPath, new Problem[]{
				new Problem("", "Type mismatch: cannot convert from long to boolean", testPath),
				new Problem("", "Type mismatch: cannot convert from long to char", testPath),
				new Problem("", "Type mismatch: cannot convert from long to byte", testPath),
				new Problem("", "Type mismatch: cannot convert from long to short", testPath),
				new Problem("", "Type mismatch: cannot convert from long to int", testPath),
				new Problem("", "value mismatch for memeber z expected true but got false", testPath),
				new Problem("", "value mismatch for memeber s expected 49 but got 0", testPath),
				new Problem("", "value mismatch for memeber i expected 49 but got 0", testPath),
				new Problem("", "value mismatch for memeber c expected 1 but got 0", testPath),
				new Problem("", "value mismatch for memeber b expected 49 but got 0", testPath),
		});
		
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
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.Annotation;\n" +
			"@Annotation(z=49f,\n" +
			"				  c=49f,\n" +
			"				  b=49f,\n" +
			"				  s=49f,\n" + 
			"				  i=49f,\n" +
			"				  l=49f,\n" +	
			"				  f=49f,\n" +
			"			      d=49f)\n" +
			"public class Test {}\n";
		
		IPath testPath = env.addClass( 
				srcRoot, 
				"sample", 
				"Test",
				content );
		fullBuild( project.getFullPath() );
		expectingSpecificProblemsFor(testPath, new Problem[]{
				new Problem("", "Type mismatch: cannot convert from float to boolean", testPath),
				new Problem("", "Type mismatch: cannot convert from float to int", testPath),
				new Problem("", "Type mismatch: cannot convert from float to long", testPath),
				new Problem("", "Type mismatch: cannot convert from float to char", testPath),
				new Problem("", "Type mismatch: cannot convert from float to byte", testPath),
				new Problem("", "Type mismatch: cannot convert from float to short", testPath),
				new Problem("", "type mismatch for memeber z expected java.lang.Boolean but got java.lang.Float", testPath),
				new Problem("", "type mismatch for memeber s expected java.lang.Short but got java.lang.Float", testPath),
				new Problem("", "type mismatch for memeber i expected java.lang.Integer but got java.lang.Float", testPath),
				new Problem("", "type mismatch for memeber c expected java.lang.Character but got java.lang.Float", testPath),
				new Problem("", "type mismatch for memeber b expected java.lang.Byte but got java.lang.Float", testPath),
				new Problem("", "type mismatch for memeber l expected java.lang.Long but got java.lang.Float", testPath),
		});		
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	
	/**
	 * Test conversion on Float. 
	 * @throws Exception
	 */
	@SuppressWarnings("nls")
	public void testFloatConversion_Reflection() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.RefAnnotation;\n" +
			"@RefAnnotation(z=49f,\n" +
			"				  c=49f,\n" +
			"				  b=49f,\n" +
			"				  s=49f,\n" + 
			"				  i=49f,\n" +
			"				  l=49f,\n" +	
			"				  f=49f,\n" +
			"			      d=49f)\n" +
			"public class Test {}\n";
		
		IPath testPath = env.addClass( 
				srcRoot, 
				"sample", 
				"Test",
				content );
		fullBuild( project.getFullPath() );
		expectingSpecificProblemsFor(testPath, new Problem[]{
				new Problem("", "Type mismatch: cannot convert from float to boolean", testPath),
				new Problem("", "Type mismatch: cannot convert from float to int", testPath),
				new Problem("", "Type mismatch: cannot convert from float to long", testPath),
				new Problem("", "Type mismatch: cannot convert from float to char", testPath),
				new Problem("", "Type mismatch: cannot convert from float to byte", testPath),
				new Problem("", "Type mismatch: cannot convert from float to short", testPath),
				new Problem("", "value mismatch for memeber z expected true but got false", testPath),
				new Problem("", "value mismatch for memeber s expected 49 but got 0", testPath),
				new Problem("", "value mismatch for memeber i expected 49 but got 0", testPath),
				new Problem("", "value mismatch for memeber c expected 1 but got 0", testPath),
				new Problem("", "value mismatch for memeber b expected 49 but got 0", testPath),
				new Problem("", "value mismatch for memeber l expected 49 but got 0", testPath),
		});		
		
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
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.Annotation;\n" +
			"@Annotation(z=49d," +
			"				  c=49d,\n" +
			"				  b=49d,\n" +
			"				  s=49d,\n" + 
			"				  i=49d,\n" +
			"				  l=49d,\n" +	
			"				  f=49d,\n" +
			"			      d=49d)\n" +
			"public class Test {}\n";
		
		IPath testPath = env.addClass(srcRoot, "sample", "Test", content );
		fullBuild( project.getFullPath() );
		expectingSpecificProblemsFor(testPath, new Problem[]{
				new Problem("", "Type mismatch: cannot convert from double to boolean", testPath),
				new Problem("", "Type mismatch: cannot convert from double to int", testPath),
				new Problem("", "Type mismatch: cannot convert from double to long", testPath),
				new Problem("", "Type mismatch: cannot convert from double to char", testPath),
				new Problem("", "Type mismatch: cannot convert from double to byte", testPath),
				new Problem("", "Type mismatch: cannot convert from double to short", testPath),
				new Problem("", "Type mismatch: cannot convert from double to float", testPath),
				new Problem("", "type mismatch for memeber z expected java.lang.Boolean but got java.lang.Double", testPath),
				new Problem("", "type mismatch for memeber s expected java.lang.Short but got java.lang.Double", testPath),
				new Problem("", "type mismatch for memeber i expected java.lang.Integer but got java.lang.Double", testPath),
				new Problem("", "type mismatch for memeber c expected java.lang.Character but got java.lang.Double", testPath),
				new Problem("", "type mismatch for memeber b expected java.lang.Byte but got java.lang.Double", testPath),
				new Problem("", "type mismatch for memeber l expected java.lang.Long but got java.lang.Double", testPath),
				new Problem("", "type mismatch for memeber f expected java.lang.Float but got java.lang.Double", testPath)
		});
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}	
	
	/**
	 * Test conversion on double. No arrayification.
	 * @throws Exception
	 */
	@SuppressWarnings("nls")
	public void testDoubleConversion_Reflection() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.RefAnnotation;\n" +
			"@RefAnnotation(z=49d,\n" +
			"				  c=49d,\n" +
			"				  b=49d,\n" +
			"				  s=49d,\n" + 
			"				  i=49d,\n" +
			"				  l=49d,\n" +	
			"				  f=49d,\n" +
			"			      d=49d)\n" +
			"public class Test {}\n";
		
		IPath testPath = env.addClass(srcRoot, "sample", "Test", content );
		fullBuild( project.getFullPath() );
		expectingSpecificProblemsFor(testPath, new Problem[]{
				new Problem("", "Type mismatch: cannot convert from double to boolean", testPath),
				new Problem("", "Type mismatch: cannot convert from double to int", testPath),
				new Problem("", "Type mismatch: cannot convert from double to long", testPath),
				new Problem("", "Type mismatch: cannot convert from double to char", testPath),
				new Problem("", "Type mismatch: cannot convert from double to byte", testPath),
				new Problem("", "Type mismatch: cannot convert from double to short", testPath),
				new Problem("", "Type mismatch: cannot convert from double to float", testPath),
				new Problem("", "value mismatch for memeber z expected true but got false", testPath),
				new Problem("", "value mismatch for memeber s expected 49 but got 0", testPath),
				new Problem("", "value mismatch for memeber i expected 49 but got 0", testPath),
				new Problem("", "value mismatch for memeber c expected 1 but got 0", testPath),
				new Problem("", "value mismatch for memeber b expected 49 but got 0", testPath),
				new Problem("", "value mismatch for memeber l expected 49 but got 0", testPath),
				new Problem("", "value mismatch for memeber f expected 49.0 but got 0.0", testPath)
		});
		
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
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.AnnotationWithArray;\n" +
			"@AnnotationWithArray(booleans=true,\n" +
			"				  bytes=(byte)49,\n" +
			"				  shorts=(short)49,\n" + 
			"				  ints=49,\n" +
			"                 longs=49,\n" +
			"	 	 	      floats=49,\n" +
			"			      doubles=49,\n" +
			"				  chars='1')\n\n" + 
			"public class Test {}\n" ;
		
		env.addClass( srcRoot, "sample", "Test", content );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}	
	
	/**
	 * Convert "singleton" instance to array of the correct type
	 * @throws Exception
	 */
	@SuppressWarnings("nls")
	public void testArrayification_Reflection() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.RefAnnotationWithArray;\n" +
			"@RefAnnotationWithArray(booleans=true,\n" +
			"				  bytes=(byte)49,\n" +
			"				  shorts=(short)49,\n" + 
			"				  ints=49,\n" +
			"                 longs=49,\n" +
			"	 	 	      floats=49,\n" +
			"			      doubles=49,\n" +
			"				  chars='1')\n\n" + 
			"public class Test {}\n" ;
		
		env.addClass( srcRoot, "sample", "Test", content );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}	
	
	/**
	 * Test conversion on array elements.
	 * @throws Exception
	 */
	@SuppressWarnings("nls")
	public void testArrayElementConversion() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.AnnotationWithArray;\n" +
			"@AnnotationWithArray(booleans={true, true },\n" +
			"				  bytes=  {(byte)49, 50}, \n" +
			"				  shorts= {(byte)49, 50},\n" + 
			"				  ints=   {(byte)49, 50},\n" +
			"                 longs=  {(byte)49, 50},\n" +
			"	 	 	      floats= {(byte)49, 50},\n" +
			"			      doubles={(byte)49, 50},\n" +
			"				  chars=  {'1','2'})\n\n" + 
			"public class Test {}\n";
		
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
	 * Test conversion on array elements.
	 * @throws Exception
	 */
	@SuppressWarnings("nls")
	public void testArrayElementConversion_Reflection() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.RefAnnotationWithArray;\n" +
			"@RefAnnotationWithArray(booleans={true, true },\n" +
			"				  bytes=  {(byte)49, 50}, \n" +
			"				  shorts= {(byte)49, 50},\n" + 
			"				  ints=   {(byte)49, 50},\n" +
			"                 longs=  {(byte)49, 50},\n" +
			"	 	 	      floats= {(byte)49, 50},\n" +
			"			      doubles={(byte)49, 50},\n" +
			"				  chars=  {'1','2'})\n\n" + 
			"public class Test {}\n" ;
		
		env.addClass( 
				srcRoot, 
				"sample", 
				"Test",
				content );
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	
	public void testErrorStringValue() throws Exception {

		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.AnnotationWithArray;\n" +
			"@AnnotationWithArray(booleans={true, true },\n" +
			"				  bytes=  {(byte)49, 50}, \n" +
			"				  shorts= {(byte)49, 50},\n" + 
			"				  ints=   {(byte)49, 50},\n" +
			"                 longs=  {(byte)49, 50},\n" +
			"	 	 	      floats= {(byte)49, 50},\n" +
			"			      doubles={(byte)49, 50},\n" +
			"				  chars=  {'1','2'},\n" +
			"                 str=true)\n\n" + 
			"public class Test {}\n";
		
		env.addClass( 
				srcRoot, 
				"sample", 
				"Test",
				content );
		IPath testPath = env.addClass( srcRoot, "sample", "Test", content );
		fullBuild( project.getFullPath() );
		
		expectingSpecificProblemsFor(testPath, new Problem[]{
				new Problem("", "Type mismatch: cannot convert from boolean to String", testPath),
				new Problem("", "type mismatch for memeber str expected java.lang.String but got java.lang.Boolean", testPath)
		});
	}
	
	public void testErrorStringValue_Reflection() throws Exception {			
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.RefAnnotationWithArray;\n" +
			"@RefAnnotationWithArray(booleans={true, true },\n" +
			"				  bytes=  {(byte)49, 50}, \n" +
			"				  shorts= {(byte)49, 50},\n" + 
			"				  ints=   {(byte)49, 50},\n" +
			"                 longs=  {(byte)49, 50},\n" +
			"	 	 	      floats= {(byte)49, 50},\n" +
			"			      doubles={(byte)49, 50},\n" +
			"				  chars=  {'1','2'},\n" +
			"                 str=true)\n\n" + 
			"public class Test {}\n";
		
		IPath testPath = env.addClass( srcRoot, "sample", "Test", content );
		fullBuild( project.getFullPath() );
		
		expectingSpecificProblemsFor(testPath, new Problem[]{
				new Problem("", "Type mismatch: cannot convert from boolean to String", testPath),
				new Problem("", "value mismatch for memeber str expected string but got null", testPath)
		});	
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	
	@SuppressWarnings("nls")
	public void testMissingPrimitiveTypeValues_Reflection() throws Exception {
		// reset the error reset the error;
		IProject project = setupTest();	
		IPath srcRoot = getSourcePath();		
		
		String content =  
			"package sample; \n\n" + 
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.RefAnnotation;\n" +
			"@RefAnnotation()\n"+
			"public class Test {}\n";
		
		IPath testPath = env.addClass( srcRoot, "sample", "Test", content );
		fullBuild( project.getFullPath() );
		
		expectingSpecificProblemsFor(testPath, new Problem[]{
				new Problem("", "The annotation @RefAnnotation must define the attribute z", testPath),
				new Problem("", "The annotation @RefAnnotation must define the attribute c", testPath),
				new Problem("", "The annotation @RefAnnotation must define the attribute b", testPath),
				new Problem("", "The annotation @RefAnnotation must define the attribute s", testPath),
				new Problem("", "The annotation @RefAnnotation must define the attribute i", testPath),
				new Problem("", "The annotation @RefAnnotation must define the attribute l", testPath),
				new Problem("", "The annotation @RefAnnotation must define the attribute f", testPath),
				new Problem("", "The annotation @RefAnnotation must define the attribute d", testPath),
				new Problem("", "value mismatch for memeber z expected true but got false", testPath),
				new Problem("", "value mismatch for memeber s expected 49 but got 0", testPath),
				new Problem("", "value mismatch for memeber i expected 49 but got 0", testPath),
				new Problem("", "value mismatch for memeber c expected 1 but got 0", testPath),
				new Problem("", "value mismatch for memeber b expected 49 but got 0", testPath),
				new Problem("", "value mismatch for memeber l expected 49 but got 0", testPath),
				new Problem("", "value mismatch for memeber f expected 49.0 but got 0.0", testPath),
				new Problem("", "value mismatch for memeber d expected 49.0 but got 0.0", testPath)
		});	
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
}
