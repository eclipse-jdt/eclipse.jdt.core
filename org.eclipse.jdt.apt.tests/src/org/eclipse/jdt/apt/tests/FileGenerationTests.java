/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
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

		Map<String,String> options = AptConfig.getProcessorOptions(jproj);
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
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		
		String code = 
				"package test;" + "\n" +
				"import org.eclipse.jdt.apt.tests.annotations.filegen.TextGenAnnotation;" + "\n" +
				"@TextGenAnnotation" + "\n" +
				"public class Test" + "\n" +
				"{" + "\n" +
				"}";

		env.addClass(srcRoot, "test", "Test", code);

		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		// Look for the file
		Map<String,String> options = AptConfig.getProcessorOptions(JavaCore.create(project));
		// We'll find it in the binary output directory
		String outputRootPath = options.get("-d");
		File theFile = new File(new File(outputRootPath), TextGenAnnotationProcessor.FILE_NAME);
		
		assertTrue("File was not found: " + theFile.getAbsolutePath(), theFile.exists());
		
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		// Look for the file again
		assertTrue("File was not found: " + theFile.getAbsolutePath(), theFile.exists());
		
		// remove the annotation, and the file should be deleted
		code = 
			"package test;" + "\n" +
			"public class Test" + "\n" +
			"{" + "\n" +
			"}";
		env.addClass(srcRoot, "test", "Test", code);
		
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		
		// Look for the file -- it should be gone
		assertTrue("File was found, but should be deleted: " + theFile.getAbsolutePath(), !theFile.exists());
	}
}
