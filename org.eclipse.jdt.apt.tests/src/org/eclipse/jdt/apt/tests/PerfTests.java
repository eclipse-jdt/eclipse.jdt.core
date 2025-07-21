/*******************************************************************************
 * Copyright (c) 2005, 2011 BEA Systems, Inc.
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
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipInputStream;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.builder.BuilderTests;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class PerfTests extends BuilderTests
{

	private static final String GITHUB_TESTS_BINARIES = "https://github.com/eclipse-jdt/eclipse.jdt.core.binaries/raw/master/org.eclipse.jdt.core.tests.binaries/";
	private IPath projectPath;

	public PerfTests(String name)
	{
		super( name );
	}

	public static Test suite()
	{
		return new TestSuite( PerfTests.class );
	}

	public void setUp() throws Exception
	{
		super.setUp();

		IWorkspace ws = env.getWorkspace();
		IWorkspaceRoot root = ws.getRoot();
		IPath path = root.getLocation();
		File destRoot = path.toFile();

		File tempPath = fetchFromBinariesProject("perf-test-project.zip", 3_307_492);
		
		InputStream in = new FileInputStream(tempPath);
		
		try (ZipInputStream zipIn = new ZipInputStream(in)) {
			TestUtil.unzip(zipIn, destRoot);
		}

		// project will be deleted by super-class's tearDown() method
		projectPath = env.addProject( "org.eclipse.jdt.core", CompilerOptions.getFirstSupportedJavaVersion() ); //$NON-NLS-1$ //$NON-NLS-2$

		System.out.println("Performing full build..."); //$NON-NLS-1$
		fullBuild( projectPath );
		System.out.println("Completed build."); //$NON-NLS-1$

		assertNoUnexpectedProblems();

	}

	private static File fetchFromBinariesProject(String nameInProject, long size) throws IOException, MalformedURLException {
		String tmpRoot = System.getProperty("java.io.tmpdir");
		File tempFile = new File(tmpRoot, nameInProject);
		if (!tempFile.isFile() || tempFile.length() != size) {			
			String githubUrl = GITHUB_TESTS_BINARIES + nameInProject;
			try(BufferedInputStream bin = new BufferedInputStream(new URL(githubUrl).openStream())){
				Files.copy(bin, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			}
		}
		return tempFile;
	}

	/**
	 * JDT Core has one warning on the use of IWorkingCopy, and a number
	 * of TODOs, XXXs and FIXMEs.
	 */
	private void assertNoUnexpectedProblems() {
		Problem[] problems = env.getProblems();
		for (Problem problem : problems) {
			if (problem.getMessage().startsWith("TODO") ||
				problem.getMessage().startsWith("XXX") ||
				problem.getMessage().startsWith("FIXME")) {
				continue;
			}
			else {
				if (problem.getMessage().equals("The type IWorkingCopy is deprecated"))
					continue;
			}
			fail("Found unexpected problem: " + problem);
		}
	}

	public static String getProjectName()
	{
		return PerfTests.class.getName() + "Project"; //$NON-NLS-1$
	}

	public IPath getSourcePath()
	{
		IProject project = env.getProject( getProjectName() );
		IFolder srcFolder = project.getFolder( "src" ); //$NON-NLS-1$
		IPath srcRoot = srcFolder.getFullPath();
		return srcRoot;
	}

	public void testBuilding() throws Throwable {
		IProject proj = env.getProject(projectPath);
		IJavaProject jproj = JavaCore.create(proj); // doesn't actually create anything

		assertNoUnexpectedProblems();

		// Start with APT turned off
		AptConfig.setEnabled(jproj, false);
		proj.build(IncrementalProjectBuilder.CLEAN_BUILD, null);

		assertNoUnexpectedProblems();

		System.out.println("Performing full build without apt...");
		long start = System.currentTimeMillis();
		proj.build(IncrementalProjectBuilder.FULL_BUILD, null);
		long totalWithoutAPT = System.currentTimeMillis() - start;
		System.out.println("Completed full build without APT in " + totalWithoutAPT + "ms.");

		assertNoUnexpectedProblems();

		// Now turn on APT
		AptConfig.setEnabled(jproj, true);
		proj.build(IncrementalProjectBuilder.CLEAN_BUILD, null);

		assertNoUnexpectedProblems();

		System.out.println("Performing full build with apt...");
		start = System.currentTimeMillis();
		proj.build(IncrementalProjectBuilder.FULL_BUILD, null);
		long totalWithAPT = System.currentTimeMillis() - start;
		System.out.println("Completed full build with APT in " + totalWithAPT + "ms.");

		assertNoUnexpectedProblems();

		if (totalWithAPT > totalWithoutAPT * 1.15) {
			fail("APT performance degradation greater than 15%");
		}
	}

}
