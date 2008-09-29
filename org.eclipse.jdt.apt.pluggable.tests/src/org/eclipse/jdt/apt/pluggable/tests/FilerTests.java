/*******************************************************************************
 * Copyright (c) 2007, 2008 BEA Systems, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.pluggable.tests.processors.filertester.FilerTesterProc;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.builder.Problem;

/**
 * Basic tests for the Filer interface in the IDE.
 * @see javax.annotation.processing.Filer
 */
public class FilerTests extends TestBase
{

	public FilerTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(FilerTests.class);
	}
	
	/**
	 * Test generation of a source file, using the GenClass6 annotation
	 * @see javax.annotation.processing.Filer#createClassFile(CharSequence, javax.lang.model.element.Element...)
	 */
	public void testCreateSourceFile() throws Throwable
	{
		// Temporary workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=201931
		// Bail out on Linux
		String osName = System.getProperty("os.name");
		if (null == osName || !osName.contains("Windows")) {
			return;
		}

		IJavaProject jproj = createJavaProject(_projectName);
		IProject proj = jproj.getProject();
		IdeTestUtils.copyResources(proj, "targets/filer01a", "src/targets/filer");
		AptConfig.setEnabled(jproj, true);
		fullBuild();
		expectingNoProblems();
		
		// Check whether generated sources were generated and compiled
		expectingFile(proj, ".apt_generated/gen6/Generated01.java");
		final String[] expectedClasses = { "targets.filer.Parent01", "gen6.Generated01" };
		expectingUniqueCompiledClasses(expectedClasses);
		
		// Check whether non-source resource was generated in final round
		expectingFile( proj, ".apt_generated/summary.txt" );
		
		//TODO: if a parent file is modified,
		// the generated file should be regenerated and recompiled.
		// The generated resource file should be regenerated (but not compiled).
	
		// Modify target file to remove annotation and incrementally rebuild; 
		// generated file should be deleted.
		IdeTestUtils.copyResources(proj, "targets/filer01b", "src/targets/filer");
		incrementalBuild();
		expectingNoProblems();
		
		final String[] expectedClasses2 = { "targets.filer.Parent01" };
		expectingUniqueCompiledClasses(expectedClasses2);

		expectingNoFile(proj, ".apt_generated/gen6/Generated01.java");
		expectingNoFile( proj, ".apt_generated/summary.txt" );
	}

	/**
	 * Test generation of a source file that is referenced by the parent, using the GenClass6 annotation
	 * @see javax.annotation.processing.Filer#createSourceFile(CharSequence, javax.lang.model.element.Element...)
	 */
	public void testCreateSourceFileWithGenReference() throws Throwable
	{
		// Temporary workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=201931
		// Bail out on Linux
		String osName = System.getProperty("os.name");
		if (null == osName || !osName.contains("Windows")) {
			return;
		}

		IJavaProject jproj = createJavaProject(_projectName);
		IProject proj = jproj.getProject();
		IdeTestUtils.copyResources(proj, "targets/filer02a", "src/targets/filer");
		
		disableJava5Factories(jproj);
		AptConfig.setEnabled(jproj, true);
		fullBuild();
		expectingNoProblems();
		
		// Check whether generated sources were generated and compiled
		expectingFile(proj, ".apt_generated/gen6/Generated02.java");
		final String[] expectedClasses = { "targets.filer.Parent02", "gen6.Generated02" };
		expectingUniqueCompiledClasses(expectedClasses);
		
		// Modify target file to change name of generated file and incrementally rebuild; 
		// generated file should be deleted.
		IdeTestUtils.copyResources(proj, "targets/filer02b", "src/targets/filer");
		incrementalBuild();
		
		IPath parentPath = proj.getFullPath().append("src/targets/filer/Parent02.java");
		expectingOnlySpecificProblemFor(parentPath, new Problem("Parent02", "gen6.Generated02 cannot be resolved to a type", parentPath, 842, 858, CategorizedProblem.CAT_TYPE, IMarker.SEVERITY_ERROR));
		
		// This test only works if there are no Java 5 processors (e.g., apt.tests plugin) in the factory path.
		// If Java 5 processors are present, then gen6.Generated02 will also be recompiled, before it's deleted.
		final String[] expectedClasses2 = { "gen6.XxxGenerated02", "targets.filer.Parent02" };
		expectingUniqueCompiledClasses(expectedClasses2);

		expectingNoFile(proj, ".apt_generated/gen6/Generated02.java");
	}
	
	/**
	 * Call FilerTesterProc.testGetResource01(), which checks getResource() in SOURCE_OUTPUT location
	 */
	public void testGetResource01() throws Throwable {
		ProcessorTestStatus.reset();
		IJavaProject jproj = createJavaProject(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();
		
		env.addClass(projPath.append("src"), "p", "Trigger",
				"package p;\n" +
				"import org.eclipse.jdt.apt.pluggable.tests.annotations.FilerTestTrigger;\n" +
				"@FilerTestTrigger(test = \"testGetResource01\", arg0 = \"g\", arg1 = \"Test.java\")" +
				"public class Trigger {\n" +
				"}"
			); 
		
		AptConfig.setEnabled(jproj, true);
		
		// FilerTesterProc looks for the existence and contents of this class:
		env.addClass(projPath.append(".apt_generated"), "g", "Test",
				FilerTesterProc.resource01FileContents);

		fullBuild();
		expectingNoProblems();
		assertTrue("Processor did not run", ProcessorTestStatus.processorRan());
		assertEquals("Processor reported errors", ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Call FilerTesterProc.testGetResource02(), which checks getResource() in CLASS_OUTPUT location
	 */
	public void testGetResource02() throws Throwable {
		ProcessorTestStatus.reset();
		IJavaProject jproj = createJavaProject(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();
		
		env.addClass(projPath.append("src"), "p", "Trigger",
				"package p;\n" +
				"import org.eclipse.jdt.apt.pluggable.tests.annotations.FilerTestTrigger;\n" +
				"@FilerTestTrigger(test = \"testGetResource02\", arg0 = \"t\", arg1 = \"Test.txt\")" +
				"public class Trigger {\n" +
				"}"
			); 
		
		AptConfig.setEnabled(jproj, true);
		
		// FilerTesterProc looks for the existence and contents of this file after it is copied to the output folder:
		IFolder textFileFolder = proj.getFolder("src/t");
		textFileFolder.create(false, true, null);
		IFile textFile = proj.getFile(textFileFolder.getProjectRelativePath().append("Test.txt"));
		InputStream textSource = new ByteArrayInputStream(FilerTesterProc.resource02FileContents.getBytes());
		textFile.create(textSource, false, null);

		fullBuild();
		expectingNoProblems();
		assertTrue("Processor did not run", ProcessorTestStatus.processorRan());
		assertEquals("Processor reported errors", ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Call FilerTesterProc.testCreateNonSourceFile(), which creates a non-source output file
	 */
	public void testCreateNonSourceFile() throws Throwable {
		ProcessorTestStatus.reset();
		IJavaProject jproj = createJavaProject(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();
		
		env.addClass(projPath.append("src"), "p", "Trigger",
				"package p;\n" +
				"import org.eclipse.jdt.apt.pluggable.tests.annotations.FilerTestTrigger;\n" +
				"@FilerTestTrigger(test = \"testCreateNonSourceFile\", arg0 = \"t\", arg1 = \"Test.txt\")" +
				"public class Trigger {\n" +
				"}"
			); 
		
		AptConfig.setEnabled(jproj, true);
		
		fullBuild();
		expectingNoProblems();
		assertTrue("Processor did not run", ProcessorTestStatus.processorRan());
		assertEquals("Processor reported errors", ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	
	/**
	 * Call FilerTesterProc.testURI, which tests the FileObject.toUri() method on
	 * various different sorts of files
	 */
	public void testURI() throws Throwable {
		ProcessorTestStatus.reset();
		IJavaProject jproj = createJavaProject(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();
		
		env.addClass(projPath.append("src"), "p", "Trigger",
				"package p;\n" +
				"import org.eclipse.jdt.apt.pluggable.tests.annotations.FilerTestTrigger;\n" +
				"@FilerTestTrigger(test = \"testURI\", arg0 = \"t\", arg1 = \"Test.txt\")" +
				"public class Trigger {\n" +
				"}"
			); 
		
		IFolder textFileFolder = proj.getFolder("src/t");
		textFileFolder.create(false, true, null);
		IFile textFile = proj.getFile(textFileFolder.getProjectRelativePath().append("Test.txt"));
		InputStream textSource = new ByteArrayInputStream(FilerTesterProc.helloStr.getBytes());
		textFile.create(textSource, false, null);

		AptConfig.setEnabled(jproj, true);
		
		fullBuild();
		expectingNoProblems();
		assertTrue("Processor did not run", ProcessorTestStatus.processorRan());
		assertEquals("Processor reported errors", ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Call FilerTesterProc.testGetCharContentLarge(), which checks FileObject.getCharContent() 
	 * for a large (multiple buffers long) file
	 */
	public void testGetCharContentLarge() throws Throwable {
		ProcessorTestStatus.reset();
		IJavaProject jproj = createJavaProject(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();
		
		env.addClass(projPath.append("src"), "p", "Trigger",
				"package p;\n" +
				"import org.eclipse.jdt.apt.pluggable.tests.annotations.FilerTestTrigger;\n" +
				"@FilerTestTrigger(test = \"testGetCharContentLarge\", arg0 = \"g\", arg1 = \"Test.java\")" +
				"public class Trigger {\n" +
				"}"
			); 
		
		AptConfig.setEnabled(jproj, true);
		
		// FilerTesterProc looks for the existence and contents of this class:
		env.addClass(projPath.append(".apt_generated"), "g", "Test",
				FilerTesterProc.largeJavaClass());

		fullBuild();
		expectingNoProblems();
		assertTrue("Processor did not run", ProcessorTestStatus.processorRan());
		assertEquals("Processor reported errors", ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

}
