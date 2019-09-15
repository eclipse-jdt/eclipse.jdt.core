/*******************************************************************************
 * Copyright (c) 2009 Walter Harley and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    eclipse@cafewalter.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests;

import java.io.File;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Performance tests for use when profiling APT
 */
public class PerformanceTests extends TestBase
{
	private final static boolean VERBOSE = true;
	private final static int PAUSE_EVERY = 200; // wait for indexer to catch up after creating this many files
	private final static int PAUSE_TIME = 2000; // milliseconds to wait for indexer
	private final static boolean INCLUDE_APT_DISABLED = true;

	private final static String CMD_PROFILER_PREFIX = "java -jar c:/opt/yourkit-8.0.13/lib/yjp-controller-api-redist.jar localhost 10001";
	private final static String CMD_START_CPU_PROFILING = CMD_PROFILER_PREFIX + " start-cpu-sampling noj2ee";
	private final static String CMD_STOP_CPU_PROFILING =  CMD_PROFILER_PREFIX + " stop-cpu-profiling";
	private final static String CMD_PERF_SNAPSHOT =       CMD_PROFILER_PREFIX + " capture-performance-snapshot";
	private final static String CMD_HEAP_SNAPSHOT =       CMD_PROFILER_PREFIX + " capture-memory-snapshot";

	public PerformanceTests(String name) {
		super(name);
	}

	public static Test suite()
	{
		return new TestSuite( PerformanceTests.class );
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
	}

	/**
	 * Create files that contain annotations that won't be processed.
	 */
	private void createBoringFiles(int numFiles, IJavaProject jproj)
	{
		String srcTemplate =
			"package p;\n" +
			"import java.util.List;\n" +
			"@SuppressWarnings(\"unchecked\")\n" +
			"public class TestB%05d {\n" +
			"  private List l = null;\n" +
			"  public List getL() { return l; }\n" +
			"}";
		String nameTemplate = "TestB%05d";
		createFiles(numFiles, nameTemplate, srcTemplate, jproj);
	}

	/**
	 * Create files that contain annotations that will be processed with a Java 6 processor.
	 */
	private void createInterestingFilesWithJ6(int numFiles, IJavaProject jproj)
	{
		String srcTemplate =
			"package p;\n" +
			"import org.eclipse.jdt.apt.pluggable.tests.annotations.ModelTestTrigger;\n" +
			"import org.eclipse.jdt.apt.pluggable.tests.annotations.LookAt;\n" +
			"import java.util.List;\n" +
			"@ModelTestTrigger(test = \"testFieldType\")" +
			"@SuppressWarnings(\"unused\")\n" +
			"public class TestI6%05d {\n" +
			"    @LookAt\n" +
			"    private int _fInt = 0;\n" +
			"    @LookAt\n" +
			"    private String _fString = \"\";\n" +
			"    @LookAt\n" +
			"    private List<String> _fFoo = null;\n" +
			"}";
		String nameTemplate = "TestI6%05d";
		createFiles(numFiles, nameTemplate, srcTemplate, jproj);
	}

	/**
	 * Create files that have annotations that cause other files to be generated.
	 */
	private void createGeneratingFiles(int numFiles, IJavaProject jproj)
	{
		String srcTemplate =
			"package p;\n" +
			"import org.eclipse.jdt.apt.pluggable.tests.annotations.GenClass6;\n" +
			"@GenClass6(pkg=\"g\", name=\"Generated%05d\")\n" +
			"public class TestG%05d {}";

		String nameTemplate = "TestG%05d";
		createFiles(numFiles, nameTemplate, srcTemplate, jproj);
	}

	private void createFiles(int numFiles, String nameTemplate, String srcTemplate, IJavaProject jproj)
	{
		IProject project = jproj.getProject();
		IFolder srcFolder = project.getFolder( "src" );
		IPath srcRoot = srcFolder.getFullPath();

		for (int i = 1; i <= numFiles; ++i) {
			String name = String.format(nameTemplate, i);
			String contents = String.format(srcTemplate, i, i);
			env.addClass( srcRoot, "p", name, contents ); //$NON-NLS-1$ //$NON-NLS-2$

			// pause to let indexer catch up
			if (i % PAUSE_EVERY == 0) {
				if (VERBOSE)
					System.out.println("Created " + i + " files; pausing for indexer");
				try {
					Thread.sleep(PAUSE_TIME);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
			}
		}

		if (VERBOSE)
			System.out.println("Done creating source files");
	}

	/**
	 * Performance with files that contain annotations that won't be processed.
	 */
	public void testBoringFiles() throws Exception
	{
		final int FILES_TO_GENERATE = 2000; // total number of files to create
		// set up project with unique name
		IJavaProject jproj = createJavaProject(_projectName);
		IProject project = jproj.getProject();

		createBoringFiles(FILES_TO_GENERATE, jproj);

		Runtime run = Runtime.getRuntime();

		long start;

		if (INCLUDE_APT_DISABLED) {
			AptConfig.setEnabled(jproj, false);
			start = System.currentTimeMillis();
			run.exec(CMD_START_CPU_PROFILING).waitFor();
			fullBuild( project.getFullPath() );
			if (VERBOSE)
				System.out.println("APT disabled: full build took " + ((System.currentTimeMillis() - start)/1000L) + " sec");
			run.exec(CMD_PERF_SNAPSHOT).waitFor();
			run.exec(CMD_STOP_CPU_PROFILING).waitFor();
			expectingNoProblems();

//			System.gc();
//			Thread.sleep(1000);
//
//			AptConfig.setEnabled(jproj, false);
//			start = System.currentTimeMillis();
//			fullBuild( project.getFullPath() );
//			if (VERBOSE) {
//				System.out.println("full build took " + ((System.currentTimeMillis() - start)/1000L) + " sec");
//				System.out.println("Taking heap snapshot");
//			}
//			run.exec(CMD_HEAP_SNAPSHOT).waitFor();
//			expectingNoProblems();
		}

		System.gc();
		Thread.sleep(1000);

		AptConfig.setEnabled(jproj, true);
		start = System.currentTimeMillis();
		if (VERBOSE)
			System.out.println("APT enabled: starting full build");
		fullBuild( project.getFullPath() );
		if (VERBOSE) {
			System.out.println("full build took " + ((System.currentTimeMillis() - start)/1000L) + " sec");
			System.out.println("Taking heap snapshot");
		}
		run.exec(CMD_HEAP_SNAPSHOT).waitFor();
		expectingNoProblems();

		System.gc();
		Thread.sleep(1000);

		AptConfig.setEnabled(jproj, true);
		start = System.currentTimeMillis();
		if (VERBOSE)
			System.out.println("APT enabled: starting full build");
		run.exec(CMD_START_CPU_PROFILING).waitFor();
		fullBuild( project.getFullPath() );
		if (VERBOSE)
			System.out.println("full build took " + ((System.currentTimeMillis() - start)/1000L) + " sec");
		run.exec(CMD_PERF_SNAPSHOT).waitFor();
		run.exec(CMD_STOP_CPU_PROFILING).waitFor();
		expectingNoProblems();

		System.gc();
		Thread.sleep(1000);

		AptConfig.setEnabled(jproj, true);
		start = System.currentTimeMillis();
		if (VERBOSE)
			System.out.println("APT enabled: starting full build");
		run.exec(CMD_START_CPU_PROFILING).waitFor();
		fullBuild( project.getFullPath() );
		if (VERBOSE)
			System.out.println("full build took " + ((System.currentTimeMillis() - start)/1000L) + " sec");
		run.exec(CMD_PERF_SNAPSHOT).waitFor();
		run.exec(CMD_STOP_CPU_PROFILING).waitFor();
		expectingNoProblems();

		// Now delete the project!
		if (VERBOSE)
			System.out.println("Deleting workspace");
		ResourcesPlugin.getWorkspace().delete(new IResource[] { project }, true, null);

	}

	/**
	 * Performance with files that contain annotations that will be processed
	 * with a Java 6 processor, but no file generation.
	 */
	public void _testInterestingFilesWithJ6() throws Exception
	{
		final int FILES_TO_GENERATE = 2000; // total number of files to create
		// set up project with unique name
		IJavaProject jproj = createJavaProject(_projectName);
		IProject project = jproj.getProject();

		createInterestingFilesWithJ6(FILES_TO_GENERATE, jproj);

		Runtime run = Runtime.getRuntime();

		long start;

		if (INCLUDE_APT_DISABLED) {
			AptConfig.setEnabled(jproj, false);
			start = System.currentTimeMillis();
			run.exec(CMD_START_CPU_PROFILING).waitFor();
			fullBuild( project.getFullPath() );
			if (VERBOSE)
				System.out.println("APT disabled: full build took " + ((System.currentTimeMillis() - start)/1000L) + " sec");
			run.exec(CMD_PERF_SNAPSHOT).waitFor();
			run.exec(CMD_STOP_CPU_PROFILING).waitFor();
			expectingNoProblems();

//			System.gc();
//			Thread.sleep(1000);
//
//			AptConfig.setEnabled(jproj, false);
//			start = System.currentTimeMillis();
//			run.exec(CMD_START_CPU_PROFILING).waitFor();
//			fullBuild( project.getFullPath() );
//			if (VERBOSE)
//				System.out.println("APT disabled: full build took " + ((System.currentTimeMillis() - start)/1000L) + " sec");
//			run.exec(CMD_PERF_SNAPSHOT).waitFor();
//			run.exec(CMD_STOP_CPU_PROFILING).waitFor();
//			expectingNoProblems();
		}

		System.gc();
		Thread.sleep(1000);

		AptConfig.setEnabled(jproj, true);
		start = System.currentTimeMillis();
		if (VERBOSE)
			System.out.println("APT enabled: starting full build");
		run.exec(CMD_START_CPU_PROFILING).waitFor();
		fullBuild( project.getFullPath() );
		if (VERBOSE)
			System.out.println("full build took " + ((System.currentTimeMillis() - start)/1000L) + " sec");
		run.exec(CMD_PERF_SNAPSHOT).waitFor();
		run.exec(CMD_STOP_CPU_PROFILING).waitFor();
		expectingNoProblems();

//		System.gc();
//		Thread.sleep(1000);
//
//		AptConfig.setEnabled(jproj, true);
//		start = System.currentTimeMillis();
//		if (VERBOSE)
//			System.out.println("APT enabled: starting full build");
//		run.exec(CMD_START_CPU_PROFILING).waitFor();
//		fullBuild( project.getFullPath() );
//		if (VERBOSE)
//			System.out.println("full build took " + ((System.currentTimeMillis() - start)/1000L) + " sec");
//		run.exec(CMD_PERF_SNAPSHOT).waitFor();
//		run.exec(CMD_STOP_CPU_PROFILING).waitFor();
//		expectingNoProblems();

		// Now delete the project!
		if (VERBOSE)
			System.out.println("Deleting workspace");
		ResourcesPlugin.getWorkspace().delete(new IResource[] { project }, true, null);

		if (VERBOSE)
			System.out.println("Test complete");
	}

	/**
	 * Test with files that have meaningful processing (generation of additional types).
	 * Currently disabled.
	 */
	public void _testGeneratingLotsOfFiles() throws Exception
	{
		final int FILES_TO_GENERATE = 4000; // total number of files to create
		// set up project with unique name
		IJavaProject jproj = createJavaProject(_projectName);
		IProject project = jproj.getProject();

		createGeneratingFiles(FILES_TO_GENERATE, jproj);

		// Set some per-project preferences
		AptConfig.setEnabled(jproj, true);

		long start = System.currentTimeMillis();
		fullBuild( project.getFullPath() );
		if (VERBOSE)
			System.out.println("Done with build after " + ((System.currentTimeMillis() - start)/1000L) + " sec");

		expectingNoProblems();

		IPath projPath = jproj.getProject().getLocation();
		for (int i = 1; i <= FILES_TO_GENERATE; ++i) {
			// check that file was generated
			String genFileName = String.format(".apt_generated/g/Generated%05d.java", i);
			File genFile = new File(projPath.append(genFileName).toOSString());
			assertTrue("Expected generated source file " + genFileName + " was not found", genFile != null && genFile.exists());
			// check that generated file was compiled
			String genClassName = String.format("bin/g/Generated%05d.class", i);
			File genClass = new File(projPath.append(genClassName).toOSString());
			assertTrue("Compiled file " + genClassName + " was not found", genClass != null && genClass.exists());
		}

		if (VERBOSE)
			System.out.println("Done checking output");

		// Now delete the project!
		ResourcesPlugin.getWorkspace().delete(new IResource[] { project }, true, null);

	}

}
