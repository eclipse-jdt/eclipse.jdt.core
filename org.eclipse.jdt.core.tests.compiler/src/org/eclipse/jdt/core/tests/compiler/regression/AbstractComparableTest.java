/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.*;
import java.util.*;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class AbstractComparableTest extends AbstractRegressionTest {

	class Logger extends Thread {
		StringBuffer buffer;
		InputStream inputStream;
		String type;
		Logger(InputStream inputStream, String type) {
			this.inputStream = inputStream;
			this.type = type;
			this.buffer = new StringBuffer();
		}

		public void run() {
			try {
				BufferedReader reader = new BufferedReader(new InputStreamReader(this.inputStream));
				String line = null;
				while ((line = reader.readLine()) != null) {
					this.buffer./*append(this.type).append("->").*/append(line).append("\n");
				}
				reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// to enable set VM args to -Dcompliance=1.5 -Drun.javac=enabled
	public static final String RUN_SUN_JAVAC = System.getProperty("run.javac");
	public static boolean RUN_JAVAC = CompilerOptions.ENABLED.equals(RUN_SUN_JAVAC);
	public static String JAVAC_OUTPUT_DIR = Util.getOutputDirectory() + File.separator + "javac";
	static int[] DIFF_COUNTERS = new int[3];
	public IPath dirPath;
	
	// Summary display
	static String CURRENT_CLASS_NAME;
	static Map TESTS_COUNTERS = new HashMap();

	public static Test buildTestSuite(Class evaluationTestClass) {
		Test suite = buildTestSuiteUniqueCompliance(evaluationTestClass, COMPLIANCE_1_5);
		TESTS_COUNTERS.put(evaluationTestClass.getName(), new Integer(suite.countTestCases()));
		return suite;
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		if (RUN_JAVAC) {
			if (!getClass().getName().equals(CURRENT_CLASS_NAME)) {
				CURRENT_CLASS_NAME = getClass().getName();
				System.out.println("***************************************************************************");
				System.out.print("* Comparison with Sun Javac compiler for class ");
				System.out.print(CURRENT_CLASS_NAME.substring(CURRENT_CLASS_NAME.lastIndexOf('.')+1));
				System.out.println(" ("+TESTS_COUNTERS.get(CURRENT_CLASS_NAME)+" tests)");
				System.out.println("***************************************************************************");
				DIFF_COUNTERS[0] = 0;
				DIFF_COUNTERS[1] = 0;
				DIFF_COUNTERS[2] = 0;
			}
		}
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		super.tearDown();
		if (RUN_JAVAC) {
			Integer count = (Integer)TESTS_COUNTERS.get(CURRENT_CLASS_NAME);
			if (count != null) {
				int newCount = count.intValue()-1;
				TESTS_COUNTERS.put(CURRENT_CLASS_NAME, new Integer(newCount));
				if (newCount == 0) {
					if (DIFF_COUNTERS[0]!=0 || DIFF_COUNTERS[1]!=0 || DIFF_COUNTERS[2]!=0) {
						System.out.println("===========================================================================");
						System.out.println("Results summary:");
					}
					if (DIFF_COUNTERS[0]!=0)
						System.out.println("	- "+DIFF_COUNTERS[0]+" test(s) where Javac found errors/warnings but Eclipse did not");
					if (DIFF_COUNTERS[1]!=0)
						System.out.println("	- "+DIFF_COUNTERS[1]+" test(s) where Eclipse found errors/warnings but Javac did not");
					if (DIFF_COUNTERS[2]!=0)
						System.out.println("	- "+DIFF_COUNTERS[2]+" test(s) where Eclipse and Javac did not have same output");
					System.out.println("\n");
				}
			}
		}
	}

	public AbstractComparableTest(String name) {
		super(name);
	}

	/*
	 * Toggle compiler in mode -1.5
	 */
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);	
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_ReportFinalParameterBound, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportMissingOverrideAnnotation, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportMissingDeprecatedAnnotation, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportIncompleteEnumSwitch, CompilerOptions.WARNING);
		options.put(CompilerOptions.OPTION_ReportRawTypeReference, CompilerOptions.WARNING);
		return options;
	}

	/*######################################
	 * Specific method to let tests Sun javac compilation available...
	 #######################################*/
	/*
	 * Cleans up the given directory by removing all the files it contains as well
	 * but leaving the directory.
	 * @throws TargetException if the target path could not be cleaned up
	 */
	protected void cleanupDirectory(File directory) {
		if (!directory.exists()) {
			return;
		}
		String[] fileNames = directory.list();
		for (int i = 0; i < fileNames.length; i++) {
			File file = new File(directory, fileNames[i]);
			if (file.isDirectory()) {
				cleanupDirectory(file);
			} else {
				if (!file.delete())
					System.out.println("Could not delete file " + file.getPath());
			}
		}
		if (!directory.delete())
			System.out.println("Could not delete directory " + directory.getPath());
	}

	/*
	 * Write given source test files in current output sub-directory.
	 * Use test name for this sub-directory name (ie. test001, test002, etc...)
	 */
	protected void printFiles(String[] testFiles) {
		for (int i=0, length=testFiles.length; i<length; i++) {
			System.out.println(testFiles[i++]);
			System.out.println(testFiles[i]);
		}
		System.out.println("");
	}

	/*#########################################
	 * Override basic runConform and run Negative methods to compile test files
	 * with Sun compiler (if specified) and compare its results with ours.
	 ##########################################*/
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest#runConformTest(java.lang.String[], java.lang.String)
	 */
	protected void runConformTest(String[] testFiles,
			String expectedSuccessOutputString, String[] classLib,
			boolean shouldFlushOutputDirectory, String[] vmArguments,
			Map customOptions, ICompilerRequestor clientRequestor) {
		try {
			super.runConformTest(testFiles, expectedSuccessOutputString,
					classLib, shouldFlushOutputDirectory, vmArguments,
					customOptions, clientRequestor);
		} catch (AssertionFailedError e) {
			throw e;
		} finally {
			if (RUN_JAVAC)
				runJavac(testFiles, null, expectedSuccessOutputString, shouldFlushOutputDirectory);
		}
	}

	/*
	 * Run Sun compilation using javac.
	 * Use JRE directory to retrieve javac bin directory and current classpath for
	 * compilation.
	 * Launch compilation in a thread and verify that it does not take more than 5s
	 * to perform it. Otherwise abort the process and log in console.
	 */
	protected void runJavac(String[] testFiles, final String expectedProblemLog, final String expectedSuccessOutputString, boolean shouldFlushOutputDirectory) {
		try {
			if (shouldFlushOutputDirectory)
				cleanupDirectory(new File(JAVAC_OUTPUT_DIR));

			// Write files in dir
			IPath dirFilePath = writeFiles(testFiles);

			String testName = testName();
			Process compileProcess = null;
			Process execProcess = null;
			try {
				// Compute classpath
				String[] classpath = Util.concatWithClassLibs(JAVAC_OUTPUT_DIR, false);
				StringBuffer cp = new StringBuffer();
				cp.append(" -classpath .;"); // start with the current directory which contains the source files
				int length = classpath.length;
				for (int i = 0; i < length; i++) {
					if (classpath[i].indexOf(" ") != -1) {
						cp.append("\"" + classpath[i] + "\"");
					} else {
						cp.append(classpath[i]);
					}
					if (i<(length-1)) cp.append(";");
				}
				// Compute command line
				IPath jdkDir = (new Path(Util.getJREDirectory())).removeLastSegments(1);
				IPath javacPath = jdkDir.append("bin").append("javac.exe");
				StringBuffer cmdLine = new StringBuffer(javacPath.toString());
				cmdLine.append(cp);
				cmdLine.append(" -d ");
				cmdLine.append(JAVAC_OUTPUT_DIR.indexOf(" ") != -1 ? "\"" + JAVAC_OUTPUT_DIR + "\"" : JAVAC_OUTPUT_DIR);
				cmdLine.append(" -source 1.5 -deprecation -Xlint:unchecked "); // enable recommended warnings
				if (this.dirPath.equals(dirFilePath)) {
					cmdLine.append("*.java");
				} else {
					IPath subDirPath = dirFilePath.append("*.java").removeFirstSegments(this.dirPath.segmentCount());
					String subDirName = subDirPath.toString().substring(subDirPath.getDevice().length());
					cmdLine.append(subDirName);
				}
//				System.out.println(testName+": "+cmdLine.toString());
//				System.out.println(GenericTypeTest.this.dirPath.toFile().getAbsolutePath());
				// Launch process
				compileProcess = Runtime.getRuntime().exec(cmdLine.toString(), null, this.dirPath.toFile());
	            // Log errors
	            Logger errorLogger = new Logger(compileProcess.getErrorStream(), "ERROR");            

	            // Log output
	            Logger outputLogger = new Logger(compileProcess.getInputStream(), "OUTPUT");

	            // start the threads to run outputs (standard/error)
	            errorLogger.start();
	            outputLogger.start();

	            // Wait for end of process
				int exitValue = compileProcess.waitFor();

				// Compare compilation results
				if (expectedProblemLog == null) {
					if (exitValue != 0) {
						System.out.println("----------------------------------------");
						System.out.println(testName+" - Javac has found error(s) but Eclipse expects conform result:\n");
						System.out.println(errorLogger.buffer.toString());
						printFiles(testFiles);
						DIFF_COUNTERS[0]++;
					} else if (errorLogger.buffer.length() > 0) {
						System.out.println("----------------------------------------");
						System.out.println(testName+" - Javac has found warning(s) but Eclipse expects conform result:\n");
						System.out.println(errorLogger.buffer.toString());
						printFiles(testFiles);
						DIFF_COUNTERS[0]++;
					} else if (expectedSuccessOutputString != null) {
						// Compute command line
						IPath javaPath = jdkDir.append("bin").append("java.exe");
						StringBuffer javaCmdLine = new StringBuffer(javaPath.toString());
						javaCmdLine.append(cp);
						// assume executable class is name of first test file
						javaCmdLine.append(' ').append(testFiles[0].substring(0, testFiles[0].indexOf('.')));
						execProcess = Runtime.getRuntime().exec(javaCmdLine.toString(), null, this.dirPath.toFile());
						Logger logger = new Logger(execProcess.getInputStream(), "OUTPUT");
						logger.start();

						exitValue = execProcess.waitFor();
						String javaOutput = logger.buffer.toString().trim();
						if (!expectedSuccessOutputString.equals(javaOutput)) {
							System.out.println("----------------------------------------");
							System.out.println(testName+" - Javac and Eclipse runtime output is not the same:");
							System.out.println(expectedSuccessOutputString);
							System.out.println(javaOutput);
							System.out.println("\n");
							printFiles(testFiles);
							DIFF_COUNTERS[2]++;
						}
					}
				} else if (exitValue == 0) {
					if (errorLogger.buffer.length() == 0 && expectedProblemLog.length() > 0) {
						System.out.println("----------------------------------------");
						System.out.println(testName+" - Eclipse has found error(s)/warning(s) but Javac did not find any:");
						System.out.println(expectedProblemLog);
						printFiles(testFiles);
						DIFF_COUNTERS[1]++;
					} else if (expectedProblemLog.indexOf("ERROR") >0 ){
						System.out.println("----------------------------------------");
						System.out.println(testName+" - Eclipse has found error(s) but Javac only found warning(s):");
						System.out.println("javac:");
						System.out.println(errorLogger.buffer.toString());
						System.out.println("eclipse:");
						System.out.println(expectedProblemLog);
						printFiles(testFiles);
						DIFF_COUNTERS[1]++;
					} else {
						// TODO (frederic) compare warnings in each result and verify they are similar...
//						System.out.println(testName+": javac has found warnings :");
//						System.out.print(errorLogger.buffer.toString());
//						System.out.println(testName+": we're expecting warning results:");
//						System.out.println(expectedProblemLog);
					}
				} else if (errorLogger.buffer.length() == 0) {
					System.out.println("----------------------------------------");
					System.out.println(testName+" - Eclipse has found error(s)/warning(s) but Javac did not find any:");
					System.out.println(expectedProblemLog);
					printFiles(testFiles);
					DIFF_COUNTERS[1]++;
				}
			} catch (IOException ioe) {
				System.out.println(testName+": Not possible to launch Sun javac compilation!");
			} catch (InterruptedException e1) {
				if (compileProcess != null) compileProcess.destroy();
				if (execProcess != null) execProcess.destroy();
				System.out.println(testName+": Sun javac compilation was aborted!");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// Clean up written file(s)
			IPath testDir =  new Path(Util.getOutputDirectory()).append(testName());
			cleanupDirectory(testDir.toFile());
		}
	}

	/* (non-Javadoc)
	 * Override to compile test files with Sun compiler if specified and compare its results with ours.
	 * @see org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest#runNegativeTest(java.lang.String[], java.lang.String)
	 */
	protected void runNegativeTest(String[] testFiles,
			String expectedProblemLog, String[] classLib,
			boolean shouldFlushOutputDirectory, Map customOptions,
			boolean generateOutput, boolean showCategory, boolean showWarningToken) {
		try {
			super.runNegativeTest(testFiles, expectedProblemLog, classLib,
					shouldFlushOutputDirectory, customOptions, generateOutput,
					showCategory, showWarningToken);
		} catch (AssertionFailedError e) {
			throw e;
		} finally {
			if (RUN_JAVAC)
				runJavac(testFiles, expectedProblemLog, null, shouldFlushOutputDirectory);
		}
	}

	/*
	 * Write given source test files in current output sub-directory.
	 * Use test name for this sub-directory name (ie. test001, test002, etc...)
	 */
	protected IPath writeFiles(String[] testFiles) {
		// Compute and create specific dir
		IPath outDir = new Path(Util.getOutputDirectory());
		this.dirPath =  outDir.append(testName());
		File dir = this.dirPath.toFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}

		// For each given test files
		IPath dirFilePath = null;
		for (int i=0, length=testFiles.length; i<length; i++) {
			String contents = testFiles[i+1];
			String fileName = testFiles[i++];
			IPath filePath = this.dirPath.append(fileName);
			if (fileName.lastIndexOf('/') >= 0) {
				dir = filePath.removeLastSegments(1).toFile();
				if (!dir.exists()) {
					dir.mkdirs();
				}
			}
			if (dirFilePath == null|| (filePath.segmentCount()-1) < dirFilePath.segmentCount()) {
				dirFilePath = filePath.removeLastSegments(1);
			}
			Util.writeToFile(contents, filePath.toString());
		}
		return dirFilePath;
	}
}
