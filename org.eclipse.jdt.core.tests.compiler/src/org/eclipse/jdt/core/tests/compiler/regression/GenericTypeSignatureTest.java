/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.runtime.TargetException;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class GenericTypeSignatureTest extends AbstractRegressionTest {
	static final String RUN_SUN_JAVAC = System.getProperty("run.javac");
	static boolean runJavac = CompilerOptions.ENABLED.equals(RUN_SUN_JAVAC);
	IPath dirPath;
	
	public GenericTypeSignatureTest(String name) {
		super(name);
	}

	public static Class testClass() {
		return GenericTypeSignatureTest.class;
	}

	/*
	 * Toggle compiler in mode -1.5
	 */
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
		options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);	
		options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);	
		return options;
	}
	static {
		// Use this static to specify a subset of tests using testsNames, testNumbers or testsRange arrays
//		testsRange = new int[] { 66, -1 };
//		testsNumbers = new int[] { 65 };
	}
	public static Test suite() {
		if (testsNames != null || testsNumbers!=null || testsRange!=null) {
			return new RegressionTestSetup(suite(testClass(), testClass().getName()), highestComplianceLevels());
		} else {
			return setupSuite(testClass());
		}
	}

	/*
	 * Get short test name (without compliance info)
	 */
	String shortTestName() {
		String fname = getName();
		int idx = fname.indexOf(" - ");
		if (idx < 0) {
			return fname;
		} else {
			return fname.substring(idx+3);
		}
	}

	/*######################################
	 * Specific method to let tests Sun javac compilation available...
	 #######################################*/
	/*
	 * Cleans up the given directory by removing all the files it contains as well
	 * but leaving the directory.
	 * @throws TargetException if the target path could not be cleaned up
	 */
	private void cleanupDirectory(File directory) throws TargetException {
		if (!directory.exists()) {
			return;
		}
		String[] fileNames = directory.list();
		for (int i = 0; i < fileNames.length; i++) {
			File file = new File(directory, fileNames[i]);
			if (file.isDirectory()) {
				cleanupDirectory(file);
			} else {
				if (!file.delete()) {
					throw new TargetException("Could not delete file " + file.getPath());
				}
			}
		}
		if (!directory.delete()) {
			throw new TargetException("Could not delete directory " + directory.getPath());
		}
	}
	/*
	 * Write given source test files in current output sub-directory.
	 * Use test name for this sub-directory name (ie. test001, test002, etc...)
	 */
	private IPath writeFiles(String[] testFiles) {
		// Compute and create specific dir
		IPath outDir = new Path(Util.getOutputDirectory());
		this.dirPath =  outDir.append(shortTestName());
		IPath dirFilePath = this.dirPath;
		File dir = dirFilePath.toFile();
		if (!dir.exists()) {
			dir.mkdirs();
		}

		// For each given test files
		for (int i=0, length=testFiles.length; i<length; i++) {
			String contents = testFiles[i+1];
			String fileName = testFiles[i++];
			IPath filePath = dirFilePath.append(fileName);
			if (fileName.lastIndexOf('/') >= 0) {
				dirFilePath = filePath.removeLastSegments(1);
				dir = dirFilePath.toFile();
				if (!dir.exists()) {
					dir.mkdirs();
				}
			}
			Util.writeToFile(contents, filePath.toString());
		}
		return dirFilePath;
	}

	/*
	 * Run Sun compilation using javac.
	 * Use JRE directory to retrieve javac bin directory and current classpath for
	 * compilation.
	 * Launch compilation in a thread and verify that it does not take more than 5s
	 * to perform it. Otherwise abort the process and log in console.
	 */
	protected void runJavac(String[] testFiles, final String expectedProblemLog) {
		try {
			// Write files in dir
			final IPath dirFilePath = writeFiles(testFiles);
			
			// Create thread to run process
			Thread waitThread = new Thread() {
				public void run() {
					String testName = shortTestName();
					Process process = null;
					try {
						// Compute classpath
						String[] classpath = getDefaultClassPaths();
						StringBuffer cp = new StringBuffer();
						int length = classpath.length;
						for (int i = 0; i < length; i++) {
							cp.append(classpath[i]);
							if (i<(length-1)) cp.append(";");
						}
						// Compute command line
						IPath jdkDir = (new Path(Util.getJREDirectory())).removeLastSegments(1);
						IPath javacPath = jdkDir.append("bin").append("javac.exe");
						StringBuffer cmdLine = new StringBuffer(javacPath.toString());
						cmdLine.append(" -classpath ");
						cmdLine.append(cp);
						cmdLine.append(" -source 1.5 -deprecation -Xlint "); // enable recommended warnings
						if (GenericTypeSignatureTest.this.dirPath.equals(dirFilePath)) {
							cmdLine.append("*.java");
						} else {
							IPath subDirPath = dirFilePath.append("*.java").removeFirstSegments(GenericTypeSignatureTest.this.dirPath.segmentCount());
							String subDirName = subDirPath.toString().substring(subDirPath.getDevice().length());
							cmdLine.append(subDirName);
						}
//						System.out.println(testName+": "+cmdLine.toString());
						// Launch process
						process = Runtime.getRuntime().exec(cmdLine.toString(), null, GenericTypeSignatureTest.this.dirPath.toFile());
						process.waitFor();
						// Compare compilation results
						int exitValue = process.exitValue();
						if (expectedProblemLog == null && exitValue != 0) {
							System.out.println(testName+": javac has found error(s) although we're expecting conform result!");
						}
						else if (expectedProblemLog != null && exitValue == 0) {
							System.out.println(testName+": javac has found no error although we're expecting negative result:");
							System.out.println(expectedProblemLog);
						}
					} catch (IOException ioe) {
						System.out.println(testName+": Not possible to launch Sun javac compilation!");
					} catch (InterruptedException e1) {
						if (process != null) process.destroy();
						System.out.println(testName+": Sun javac compilation was aborted!");
					}
				}
			};
			
			// Run thread and wait 5 seconds for end of compilation
			waitThread.start();
			try {
				waitThread.join(2000);

			} catch (InterruptedException e1) {
				// do nothing
			}
			if (waitThread.isAlive()) {
				waitThread.interrupt();
			}

			// Clean up written file(s)
			IPath testDir =  new Path(Util.getOutputDirectory()).append(shortTestName());
			cleanupDirectory(testDir.toFile());
		} catch (Exception e) {
			// fails silently...
			e.printStackTrace();
		}
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
			Map customOptions) {
		try {
			super.runConformTest(testFiles, expectedSuccessOutputString,
					classLib, shouldFlushOutputDirectory, vmArguments,
					customOptions);
		} catch (AssertionFailedError e) {
			throw e;
		} finally {
			if (runJavac)
				runJavac(testFiles, null);
		}
	}
	/* (non-Javadoc)
	 * Override to compile test files with Sun compiler if specified and compare its results with ours.
	 * @see org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest#runNegativeTest(java.lang.String[], java.lang.String)
	 */
	protected void runNegativeTest(String[] testFiles,
			String expectedProblemLog, String[] classLib,
			boolean shouldFlushOutputDirectory, Map customOptions,
			boolean generateOutput) {
		try {
			super.runNegativeTest(testFiles, expectedProblemLog, classLib,
					shouldFlushOutputDirectory, customOptions, generateOutput);
		} catch (AssertionFailedError e) {
			throw e;
		} finally {
			if (runJavac)
				runJavac(testFiles, expectedProblemLog);
		}
	}

	public void test001() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> extends p.A<T> {\n" + 
				"    protected T t;\n" + 
				"    X(T t) {\n" + 
				"        super(t);\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"    	X<X<String>> xs = new X<X<String>>(new X<String>(\"SUCCESS\"));\n" + 
				"        System.out.print(xs.t.t);\n" + 
				"    }\n" + 
				"}",
				"p/A.java",
				"package p;\n" + 
				"public class A<P> {\n" + 
				"    protected P p;\n" + 
				"    protected A(P p) {\n" + 
				"        this.p = p;\n" + 
				"    }\n" + 
				"}"
			},
			"SUCCESS");

		String expectedOutput =
			"// Compiled from X.java (version 1.5 : 49.0, super bit)\n" + 
			"// Signature: <T:Ljava/lang/Object;>Lp/A<TT;>;\n" + 
			"public class X extends p.A {\n" + 
			"  \n" + 
			"  // Field descriptor #6 Ljava/lang/Object;\n" + 
			"  // Signature: TT;\n" + 
			"  protected java.lang.Object t;\n" + 
			"  \n" + 
			"  // Method descriptor  #10 (Ljava/lang/Object;)V\n" + 
			"  // Signature: (TT;)V\n" + 
			"  // Stack: 2, Locals: 2\n" + 
			"  X(java.lang.Object t);\n" + 
			"     0  aload_0\n" + 
			"     1  aload_1\n" + 
			"     2  invokespecial #15 <Constructor p.A(java.lang.Object arg)>\n" + 
			"     5  aload_0\n" + 
			"     6  aload_1\n" + 
			"     7  putfield #17 <Field X#t java.lang.Object>\n" + 
			"    10  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 4]\n" + 
			"        [pc: 5, line: 5]\n" + 
			"        [pc: 10, line: 6]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 11] local: this index: 0 type: X\n" + 
			"        [pc: 0, pc: 11] local: t index: 1 type: java.lang.Object\n" + 
			"      Local variable type table:\n" + 
			"        [pc: 0, pc: 11] local: this index: 0 type: LX<TT;>;\n" + 
			"        [pc: 0, pc: 11] local: t index: 1 type: TT;\n" + 
			"  \n" + 
			"  // Method descriptor  #25 ([Ljava/lang/String;)V\n" + 
			"  // Stack: 5, Locals: 2\n" + 
			"  public static void main(String[] args);\n" + 
			"     0  new #2 X\n" + 
			"     3  dup\n" + 
			"     4  new #2 X\n" + 
			"     7  dup\n" + 
			"     8  ldc #27 <String \"SUCCESS\">\n" + 
			"    10  invokespecial #28 <Constructor X(java.lang.Object arg)>\n" + 
			"    13  invokespecial #28 <Constructor X(java.lang.Object arg)>\n" + 
			"    16  astore_1\n" + 
			"    17  getstatic #34 <Field java.lang.System#out java.io.PrintStream>\n" + 
			"    20  aload_1\n" + 
			"    21  getfield #17 <Field X#t java.lang.Object>\n" + 
			"    24  checkcast #35 X\n" + 
			"    27  getfield #17 <Field X#t java.lang.Object>\n" + 
			"    30  checkcast #37 java.lang.String\n" + 
			"    33  invokevirtual #43 <Method java.io.PrintStream#print(java.lang.String arg) void>\n" + 
			"    36  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 8]\n" + 
			"        [pc: 17, line: 9]\n" + 
			"        [pc: 36, line: 10]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 37] local: args index: 0 type: java.lang.String[]\n" + 
			"        [pc: 17, pc: 37] local: xs index: 1 type: X\n" + 
			"      Local variable type table:\n" + 
			"        [pc: 17, pc: 37] local: xs index: 1 type: LX<LX<Ljava/lang/String;>;>;\n" +
			"}";
		
		try {
			File f = new File(OUTPUT_DIR + File.separator + "X.class");
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
			int index = result.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(result, 3));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, result);
			}
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}		
	}
	
	public void test002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"class X extends p.A<String> {\n" + 
				"    X() {\n" + 
				"        super(null);\n" + 
				"    }\n" + 
				"}",
				"p/A.java",
				"package p;\n" + 
				"public class A<P> {\n" + 
				"    protected A(P p) {\n" + 
				"    }\n" + 
				"}"
			});

		String expectedOutputForX =
			"// Compiled from X.java (version 1.5 : 49.0, super bit)\n" + 
			"// Signature: Lp/A<Ljava/lang/String;>;\n" + 
			"class X extends p.A {\n" + 
			"  \n" + 
			"  // Method descriptor  #6 ()V\n" + 
			"  // Stack: 2, Locals: 1\n" + 
			"  X();\n" + 
			"    0  aload_0\n" + 
			"    1  aconst_null\n" + 
			"    2  invokespecial #11 <Constructor p.A(java.lang.Object arg)>\n" + 
			"    5  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 5, line: 4]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 6] local: this index: 0 type: X\n" + 
			"}";
		
		try {
			File f = new File(OUTPUT_DIR + File.separator + "X.class");
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
			int index = result.indexOf(expectedOutputForX);
			if (index == -1 || expectedOutputForX.length() == 0) {
				System.out.println(Util.displayString(result, 3));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutputForX, result);
			}
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
		
		String expectedOutputForA =
			"// Compiled from A.java (version 1.5 : 49.0, super bit)\n" + 
			"// Signature: <P:Ljava/lang/Object;>Ljava/lang/Object;\n" + 
			"public class p.A extends java.lang.Object {\n" + 
			"  \n" + 
			"  // Method descriptor  #6 (Ljava/lang/Object;)V\n" + 
			"  // Signature: (TP;)V\n" + 
			"  // Stack: 1, Locals: 2\n" + 
			"  protected p.A(java.lang.Object p);\n" + 
			"    0  aload_0\n" + 
			"    1  invokespecial #12 <Constructor java.lang.Object()>\n" + 
			"    4  return\n" + 
			"      Line numbers:\n" + 
			"        [pc: 0, line: 3]\n" + 
			"        [pc: 4, line: 4]\n" + 
			"      Local variable table:\n" + 
			"        [pc: 0, pc: 5] local: this index: 0 type: p.A\n" + 
			"        [pc: 0, pc: 5] local: p index: 1 type: java.lang.Object\n" + 
			"      Local variable type table:\n" + 
			"        [pc: 0, pc: 5] local: this index: 0 type: Lp/A<TP;>;\n" + 
			"        [pc: 0, pc: 5] local: p index: 1 type: TP;\n" + 
			"}";
		
		try {
			File f = new File(OUTPUT_DIR + File.separator + "p/A.class");
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			String result = disassembler.disassemble(classFileBytes, "\n", ClassFileBytesDisassembler.DETAILED);
			int index = result.indexOf(expectedOutputForA);
			if (index == -1 || expectedOutputForA.length() == 0) {
				System.out.println(Util.displayString(result, 3));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutputForA, result);
			}
		} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
			assertTrue(false);
		} catch (IOException e) {
			assertTrue(false);
		}
	}	
}
