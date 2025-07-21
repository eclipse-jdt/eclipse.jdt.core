/*******************************************************************************
 * Copyright (c) 2005, 2008 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

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


	@Override
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
		IPath projectPath = env.addProject( getUniqueProjectName(), CompilerOptions.getFirstSupportedJavaVersion() ); //$NON-NLS-1$
		env.setOutputFolder(projectPath, "bin"); //$NON-NLS-1$
		env.addExternalJars( projectPath, Util.getJavaClassLibs() );
		fullBuild( projectPath );

		// remove old package fragment root so that names don't collide
		env.removePackageFragmentRoot( projectPath, "" ); //$NON-NLS-1$

		env.addPackageFragmentRoot( projectPath, "src" ); //$NON-NLS-1$
		return env.getProject(getProjectName());
	}

	public void testByteConversion() throws Exception {
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
		expectingSpecificProblemsFor(testPath, new ExpectedProblem[]{
				new ExpectedProblem("", "Type mismatch: cannot convert from byte to boolean", testPath),
				new ExpectedProblem("", "type mismatch for member z expected java.lang.Boolean but got java.lang.Byte", testPath)
		});

		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

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
		expectingSpecificProblemsFor(testPath, new ExpectedProblem[]{
				new ExpectedProblem("", "Type mismatch: cannot convert from byte to boolean", testPath),
				new ExpectedProblem("", "value mismatch for member z expected true but got false", testPath)
		});

		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Test conversion on short.
	 */
	public void testShortConversion() throws Exception {
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
		expectingSpecificProblemsFor(testPath, new ExpectedProblem[]{
				new ExpectedProblem("", "Type mismatch: cannot convert from short to boolean", testPath),
				new ExpectedProblem("", "type mismatch for member z expected java.lang.Boolean but got java.lang.Short", testPath)
		});

		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	public void testShortConversion_Reflection() throws Exception {
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
		expectingSpecificProblemsFor(testPath, new ExpectedProblem[]{
				new ExpectedProblem("", "Type mismatch: cannot convert from short to boolean", testPath),
				new ExpectedProblem("", "value mismatch for member z expected true but got false", testPath)
		});

		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Test conversion on char.
	 */
	public void testCharConversion() throws Exception {
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
		expectingSpecificProblemsFor(testPath, new ExpectedProblem[]{
				new ExpectedProblem("", "Type mismatch: cannot convert from char to boolean", testPath),
				new ExpectedProblem("", "type mismatch for member z expected java.lang.Boolean but got java.lang.Character", testPath)
		});

		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Test conversion on char through reflection
	 */
	public void testCharConversion_Reflection() throws Exception {
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
		expectingSpecificProblemsFor(testPath, new ExpectedProblem[]{
				new ExpectedProblem("", "Type mismatch: cannot convert from char to boolean", testPath),
				new ExpectedProblem("", "value mismatch for member z expected true but got false", testPath)
		});

		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Test conversion on int.
	 */
	public void testIntConversion() throws Exception {
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
		expectingSpecificProblemsFor(testPath, new ExpectedProblem[]{
				new ExpectedProblem("", "Type mismatch: cannot convert from int to boolean", testPath),
				new ExpectedProblem("", "type mismatch for member z expected java.lang.Boolean but got java.lang.Integer", testPath)
		});

		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Test conversion on int through reflection
	 */
	public void testIntConversion_Reflection() throws Exception {
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
		expectingSpecificProblemsFor(testPath, new ExpectedProblem[]{
				new ExpectedProblem("", "Type mismatch: cannot convert from int to boolean", testPath),
				new ExpectedProblem("", "value mismatch for member z expected true but got false", testPath)
		});

		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Test conversion on long.
	 */
	public void testLongConversion() throws Exception {
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
		expectingSpecificProblemsFor(testPath, new ExpectedProblem[]{
				new ExpectedProblem("", "Type mismatch: cannot convert from long to boolean", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from long to char", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from long to byte", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from long to short", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from long to int", testPath),
				new ExpectedProblem("", "type mismatch for member z expected java.lang.Boolean but got java.lang.Long", testPath),
				new ExpectedProblem("", "type mismatch for member s expected java.lang.Short but got java.lang.Long", testPath),
				new ExpectedProblem("", "type mismatch for member i expected java.lang.Integer but got java.lang.Long", testPath),
				new ExpectedProblem("", "type mismatch for member c expected java.lang.Character but got java.lang.Long", testPath),
				new ExpectedProblem("", "type mismatch for member b expected java.lang.Byte but got java.lang.Long", testPath),
		});

		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Test conversion on long.
	 */
	public void testLongConversion_Reflection() throws Exception {
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
		expectingSpecificProblemsFor(testPath, new ExpectedProblem[]{
				new ExpectedProblem("", "Type mismatch: cannot convert from long to boolean", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from long to char", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from long to byte", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from long to short", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from long to int", testPath),
				new ExpectedProblem("", "value mismatch for member z expected true but got false", testPath),
				new ExpectedProblem("", "value mismatch for member s expected 49 but got 0", testPath),
				new ExpectedProblem("", "value mismatch for member i expected 49 but got 0", testPath),
				new ExpectedProblem("", "value mismatch for member c expected 1 but got 0", testPath),
				new ExpectedProblem("", "value mismatch for member b expected 49 but got 0", testPath),
		});

		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Test conversion on Float.
	 */
	public void testFloatConversion() throws Exception {
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
		expectingSpecificProblemsFor(testPath, new ExpectedProblem[]{
				new ExpectedProblem("", "Type mismatch: cannot convert from float to boolean", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from float to int", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from float to long", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from float to char", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from float to byte", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from float to short", testPath),
				new ExpectedProblem("", "type mismatch for member z expected java.lang.Boolean but got java.lang.Float", testPath),
				new ExpectedProblem("", "type mismatch for member s expected java.lang.Short but got java.lang.Float", testPath),
				new ExpectedProblem("", "type mismatch for member i expected java.lang.Integer but got java.lang.Float", testPath),
				new ExpectedProblem("", "type mismatch for member c expected java.lang.Character but got java.lang.Float", testPath),
				new ExpectedProblem("", "type mismatch for member b expected java.lang.Byte but got java.lang.Float", testPath),
				new ExpectedProblem("", "type mismatch for member l expected java.lang.Long but got java.lang.Float", testPath),
		});

		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Test conversion on Float.
	 */
	public void testFloatConversion_Reflection() throws Exception {
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
		expectingSpecificProblemsFor(testPath, new ExpectedProblem[]{
				new ExpectedProblem("", "Type mismatch: cannot convert from float to boolean", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from float to int", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from float to long", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from float to char", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from float to byte", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from float to short", testPath),
				new ExpectedProblem("", "value mismatch for member z expected true but got false", testPath),
				new ExpectedProblem("", "value mismatch for member s expected 49 but got 0", testPath),
				new ExpectedProblem("", "value mismatch for member i expected 49 but got 0", testPath),
				new ExpectedProblem("", "value mismatch for member c expected 1 but got 0", testPath),
				new ExpectedProblem("", "value mismatch for member b expected 49 but got 0", testPath),
				new ExpectedProblem("", "value mismatch for member l expected 49 but got 0", testPath),
		});

		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Test conversion on double. No arrayification.
	 */
	public void testDoubleConversion() throws Exception {
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
		expectingSpecificProblemsFor(testPath, new ExpectedProblem[]{
				new ExpectedProblem("", "Type mismatch: cannot convert from double to boolean", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from double to int", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from double to long", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from double to char", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from double to byte", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from double to short", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from double to float", testPath),
				new ExpectedProblem("", "type mismatch for member z expected java.lang.Boolean but got java.lang.Double", testPath),
				new ExpectedProblem("", "type mismatch for member s expected java.lang.Short but got java.lang.Double", testPath),
				new ExpectedProblem("", "type mismatch for member i expected java.lang.Integer but got java.lang.Double", testPath),
				new ExpectedProblem("", "type mismatch for member c expected java.lang.Character but got java.lang.Double", testPath),
				new ExpectedProblem("", "type mismatch for member b expected java.lang.Byte but got java.lang.Double", testPath),
				new ExpectedProblem("", "type mismatch for member l expected java.lang.Long but got java.lang.Double", testPath),
				new ExpectedProblem("", "type mismatch for member f expected java.lang.Float but got java.lang.Double", testPath)
		});

		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Test conversion on double. No arrayification.
	 */
	public void testDoubleConversion_Reflection() throws Exception {
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
		expectingSpecificProblemsFor(testPath, new ExpectedProblem[]{
				new ExpectedProblem("", "Type mismatch: cannot convert from double to boolean", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from double to int", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from double to long", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from double to char", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from double to byte", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from double to short", testPath),
				new ExpectedProblem("", "Type mismatch: cannot convert from double to float", testPath),
				new ExpectedProblem("", "value mismatch for member z expected true but got false", testPath),
				new ExpectedProblem("", "value mismatch for member s expected 49 but got 0", testPath),
				new ExpectedProblem("", "value mismatch for member i expected 49 but got 0", testPath),
				new ExpectedProblem("", "value mismatch for member c expected 1 but got 0", testPath),
				new ExpectedProblem("", "value mismatch for member b expected 49 but got 0", testPath),
				new ExpectedProblem("", "value mismatch for member l expected 49 but got 0", testPath),
				new ExpectedProblem("", "value mismatch for member f expected 49.0 but got 0.0", testPath)
		});

		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Convert "singleton" instance to array of the correct type
	 */
	public void testArrayification() throws Exception {
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
	 */
	public void testArrayification_Reflection() throws Exception {
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
	 */
	public void testArrayElementConversion() throws Exception {
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
	 */
	public void testArrayElementConversion_Reflection() throws Exception {
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

		expectingSpecificProblemsFor(testPath, new ExpectedProblem[]{
				new ExpectedProblem("", "Type mismatch: cannot convert from boolean to String", testPath),
				new ExpectedProblem("", "type mismatch for member str expected java.lang.String but got java.lang.Boolean", testPath)
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

		expectingSpecificProblemsFor(testPath, new ExpectedProblem[]{
				new ExpectedProblem("", "Type mismatch: cannot convert from boolean to String", testPath),
				new ExpectedProblem("", "value mismatch for member str expected string but got null", testPath)
		});
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	public void testMissingPrimitiveTypeValues_Reflection() throws Exception {
		IProject project = setupTest();
		IPath srcRoot = getSourcePath();

		String content =
			"package sample; \n\n" +
			"import org.eclipse.jdt.apt.tests.annotations.valueconversion.RefAnnotation;\n" +
			"@RefAnnotation()\n"+
			"public class Test {}\n";

		IPath testPath = env.addClass( srcRoot, "sample", "Test", content );
		fullBuild( project.getFullPath() );

		expectingSpecificProblemsFor(testPath, new ExpectedProblem[]{
				new ExpectedProblem("", "The annotation @RefAnnotation must define the attribute z", testPath),
				new ExpectedProblem("", "The annotation @RefAnnotation must define the attribute c", testPath),
				new ExpectedProblem("", "The annotation @RefAnnotation must define the attribute b", testPath),
				new ExpectedProblem("", "The annotation @RefAnnotation must define the attribute s", testPath),
				new ExpectedProblem("", "The annotation @RefAnnotation must define the attribute i", testPath),
				new ExpectedProblem("", "The annotation @RefAnnotation must define the attribute l", testPath),
				new ExpectedProblem("", "The annotation @RefAnnotation must define the attribute f", testPath),
				new ExpectedProblem("", "The annotation @RefAnnotation must define the attribute d", testPath),
				new ExpectedProblem("", "value mismatch for member z expected true but got false", testPath),
				new ExpectedProblem("", "value mismatch for member s expected 49 but got 0", testPath),
				new ExpectedProblem("", "value mismatch for member i expected 49 but got 0", testPath),
				new ExpectedProblem("", "value mismatch for member c expected 1 but got 0", testPath),
				new ExpectedProblem("", "value mismatch for member b expected 49 but got 0", testPath),
				new ExpectedProblem("", "value mismatch for member l expected 49 but got 0", testPath),
				new ExpectedProblem("", "value mismatch for member f expected 49.0 but got 0.0", testPath),
				new ExpectedProblem("", "value mismatch for member d expected 49.0 but got 0.0", testPath)
		});
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
}
