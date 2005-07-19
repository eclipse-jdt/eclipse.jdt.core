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
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipInputStream;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.core.tests.builder.Tests;

public class PerfTests extends Tests
{
	
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
		
		URL platformURL = Platform.getBundle("org.eclipse.jdt.apt.tests").getEntry("/");  //$NON-NLS-1$//$NON-NLS-2$
		File f = new File(Platform.asLocalURL(platformURL).getFile());
		f = new File(f, "perf-test-project.zip"); //$NON-NLS-1$

		
		//InputStream in = PerfTests.class.getClassLoader().getResourceAsStream("perf-test-project.zip");
		InputStream in = new FileInputStream(f);
		ZipInputStream zipIn = new ZipInputStream(in);
		try {
			TestUtil.unzip(zipIn, destRoot);
		}
		finally {
			zipIn.close();
		}
		
		// project will be deleted by super-class's tearDown() method
		projectPath = env.addProject( "org.eclipse.jdt.core", "1.4" ); //$NON-NLS-1$ //$NON-NLS-2$
		
		System.out.println("Performing full build..."); //$NON-NLS-1$
		fullBuild( projectPath );
		System.out.println("Completed build."); //$NON-NLS-1$
		
		assertNoUnexpectedProblems();
		
	}
	
	/**
	 * JDT Core has one warning on the use of IWorkingCopy, and a number
	 * of TODOs, XXXs and FIXMEs.
	 */
	@SuppressWarnings("nls")
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
	
	@SuppressWarnings("nls")
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
