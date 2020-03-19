/*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    sbandow@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import java.io.File;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.internal.AptProject;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;
import org.eclipse.jdt.apt.tests.annotations.filegen.TextGenAnnotationProcessor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class FileGenerationTests extends APTTestBase {

	public FileGenerationTests(final String name)
	{
		super(name);
	}

	public static Test suite()
	{
		return new TestSuite(FileGenerationTests.class);
	}

	public void testSourceGenPackages() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		String code =
				"package test;" + "\n" +
				"import org.eclipse.jdt.apt.tests.annotations.filegen.FileGenLocationAnnotation;" + "\n" +
				"@FileGenLocationAnnotation" + "\n" +
				"public class Test" + "\n" +
				"{" + "\n" +
				"}";

		env.addClass(srcRoot, "test", "Test", code);

		fullBuild( project.getFullPath() );
		expectingNoProblems();

		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	public void testSourceGenOverwrite() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		String code =
			"package test;" + "\n" +
			"import org.eclipse.jdt.apt.tests.annotations.filegen.FirstGenAnnotation;" + "\n" +
			"@FirstGenAnnotation" + "\n" +
			"public class Test" + "\n" +
			"{" + "\n" +
			"}";

		env.addClass(srcRoot, "test", "Test", code);

		fullBuild( project.getFullPath() );
		expectingNoProblems();

		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	public void testSourceGenAfterDirChange() throws Exception
	{
		IJavaProject jproj = env.getJavaProject( getProjectName() );
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		String code =
			"package test;" + "\n" +
			"import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;" + "\n" +
			"@HelloWorldAnnotation" + "\n" +
			"public class Test" + "\n" +
			"{" + "\n" +
			"	generatedfilepackage.GeneratedFileTest gft;" + "\n" +
			"}";

		AptConfig.setGenSrcDir(jproj, "__foo_src");

		env.addClass(srcRoot, "test", "Test", code);

		fullBuild( project.getFullPath() );
		expectingNoProblems();

		Map<String,String> options = AptConfig.getProcessorOptions(jproj, false);
		String sourcepath = options.get("-sourcepath");

		assertTrue(sourcepath.contains("__foo_src"));
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	public void testSourceGenSubDir() throws Exception
	{
		IJavaProject jproj = env.getJavaProject( getProjectName() );
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		String code =
			"package test;" + "\n" +
			"import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;" + "\n" +
			"@HelloWorldAnnotation" + "\n" +
			"public class Test" + "\n" +
			"{" + "\n" +
			"	generatedfilepackage.GeneratedFileTest gft;" + "\n" +
			"}";

		AptConfig.setGenSrcDir(jproj, "gen/foo");
		env.addClass(srcRoot, "test", "Test", code);

		fullBuild( project.getFullPath() );
		expectingNoProblems();

		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	public void testTextFileGen() throws Exception {
		// enable the test only on windows for now
		if (System.getProperty("os.name").indexOf("Windows") == -1) return;
		final String TEXT_FILE_NAME = "TextFile.txt";

		clearProcessorResult(TextGenAnnotationProcessor.class);
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		String code =
				"package test;" + "\n" +
				"import org.eclipse.jdt.apt.tests.annotations.filegen.TextGenAnnotation;" + "\n" +
				"@TextGenAnnotation(\"" + TEXT_FILE_NAME + "\")" + "\n" +
				"public class Test" + "\n" +
				"{" + "\n" +
				"}";

		env.addClass(srcRoot, "test", "Test", code);

		fullBuild( project.getFullPath() );
		expectingNoProblems();

		// Look for the file
		Map<String,String> options = AptConfig.getProcessorOptions(JavaCore.create(project), false);
		// We'll find it in the binary output directory
		String outputRootPath = options.get("-d");
		File theFile = new File(new File(outputRootPath), TEXT_FILE_NAME);

		assertTrue("File was not found: " + theFile.getAbsolutePath(), theFile.exists());

		incrementalBuild( project.getFullPath() );
		expectingNoProblems();
		checkProcessorResult(TextGenAnnotationProcessor.class);
		assertTrue("File was not found: " + theFile.getAbsolutePath(), theFile.exists());

		// Change the annotation to specify an illegal filename, and an exception should be thrown
		code =
			"package test;" + "\n" +
			"import org.eclipse.jdt.apt.tests.annotations.filegen.TextGenAnnotation;" + "\n" +
			"@TextGenAnnotation(\">.txt\")" + "\n" +
			"public class Test" + "\n" +
			"{" + "\n" +
			"}";
		env.addClass(srcRoot, "test", "Test", code);

		incrementalBuild( project.getFullPath() );
		expectingNoProblems();
		assertEquals("Could not generate text file due to IOException", getProcessorResult(TextGenAnnotationProcessor.class));
		assertTrue("File was found, but should be deleted: " + theFile.getAbsolutePath(), !theFile.exists());

		// remove the annotation, and the file should be deleted and processor should not run
		code =
			"package test;" + "\n" +
			"public class Test" + "\n" +
			"{" + "\n" +
			"}";
		env.addClass(srcRoot, "test", "Test", code);

		incrementalBuild( project.getFullPath() );
		expectingNoProblems();
		assertEquals(null, getProcessorResult(TextGenAnnotationProcessor.class));
		assertTrue("File was found, but should be deleted: " + theFile.getAbsolutePath(), !theFile.exists());
	}

	public void testIsGeneratedOrParentFile() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		String code =
				"package test;" + "\n" +
				"import org.eclipse.jdt.apt.tests.annotations.filegen.FileGenLocationAnnotation;" + "\n" +
				"@FileGenLocationAnnotation" + "\n" +
				"public class Test" + "\n" +
				"{" + "\n" +
				"}";

		env.addClass(srcRoot, "test", "Test", code);

		fullBuild( project.getFullPath() );
		expectingNoProblems();

		AptProject aptProj = new AptProject(env.getJavaProject(getProjectName()));
		GeneratedFileManager gfm = aptProj.getGeneratedFileManager(false);
		String genSrcDir = AptConfig.getGenSrcDir(env.getJavaProject(getProjectName()));
		String P = File.separator;

		IFile parentFile = project.getFile("src" + P + "test" + P + "Test.java");
		IFile generatedFile = project.getFile(genSrcDir + P + "test" + P + "A.java");

		assertTrue("expected src/test/Test.java to be designated as parent file", gfm.isParentFile(parentFile));
		assertTrue("expected .apt_generated/test/A.java to be designated as generated file", gfm.isGeneratedFile(generatedFile));
	}

	public void testTestSourceGen() throws Exception
	{
		IJavaProject jproj = env.getJavaProject( getProjectName() );
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();

		String mainCode =
				"package test;" + "\n" +
				"import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;" + "\n" +
				"@HelloWorldAnnotation(\"GeneratedFileMain\")" + "\n" +
				"public class Main" + "\n" +
				"{" + "\n" +
				"	generatedfilepackage.GeneratedFileMain gfm;" + "\n" +
				"}";
		String testCode =
				"package test;" + "\n" +
				"import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;" + "\n" +
				"@HelloWorldAnnotation" + "\n" +
				"public class Test" + "\n" +
				"{" + "\n" +
				"	generatedfilepackage.GeneratedFileTest gft;" + "\n" +
				"	generatedfilepackage.GeneratedFileMain gfm;" + "\n" +
				"}";

		AptConfig.setGenSrcDir(jproj, "__main_foo_src");
		AptConfig.setGenTestSrcDir(jproj, "__test_foo_src");

		IPath testSrcRoot = addTestSourceFolder();

		env.addClass(srcRoot, "test", "Main", mainCode);
		env.addClass(testSrcRoot, "test", "Test", testCode);

		fullBuild( project.getFullPath() );
		expectingNoProblems();

		assertTrue(AptConfig.getProcessorOptions(jproj, false).get("-sourcepath").contains("__main_foo_src"));
		assertTrue(AptConfig.getProcessorOptions(jproj, true).get("-sourcepath").contains("__test_foo_src"));
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());

		String P = File.separator;

		AptProject aptProj = new AptProject(env.getJavaProject(getProjectName()));
		GeneratedFileManager maingfm = aptProj.getGeneratedFileManager(false);
		GeneratedFileManager testgfm = aptProj.getGeneratedFileManager(true);

		IFile mainParentFile = project.getFile("src" + P + "test" + P + "Main.java");
		IFile mainGeneratedFile = project.getFile("__main_foo_src" + P + "generatedfilepackage" + P + "GeneratedFileMain.java");

		IFile testParentFile = project.getFile("src-tests" + P + "test" + P + "Test.java");
		IFile testGeneratedFile = project.getFile("__test_foo_src" + P + "generatedfilepackage" + P + "GeneratedFileTest.java");

		assertTrue("expected "
				+ mainParentFile.getProjectRelativePath()
				+ " to be designated as main parent file", maingfm.isParentFile(mainParentFile));
		assertTrue("expected " + mainGeneratedFile.getProjectRelativePath()
				+ " to be designated as main generated file", maingfm.isGeneratedFile(mainGeneratedFile));
		assertFalse("expected "
				+ testParentFile.getProjectRelativePath()
				+ " not to be designated as main parent file", maingfm.isParentFile(testParentFile));
		assertFalse("expected " + testGeneratedFile.getProjectRelativePath()
				+ " not to be designated as main generated file", maingfm.isGeneratedFile(testGeneratedFile));

		assertTrue("expected "
				+ testParentFile.getProjectRelativePath()
				+ " to be designated as test parent file", testgfm.isParentFile(testParentFile));
		assertTrue("expected " + testGeneratedFile.getProjectRelativePath()
				+ " to be designated as test generated file", testgfm.isGeneratedFile(testGeneratedFile));
		assertFalse("expected "
				+ mainParentFile.getProjectRelativePath()
				+ " not to be designated as test parent file", testgfm.isParentFile(mainParentFile));
		assertFalse("expected " + mainGeneratedFile.getProjectRelativePath()
				+ " not to be designated as test generated file", testgfm.isGeneratedFile(mainGeneratedFile));

	}

}
