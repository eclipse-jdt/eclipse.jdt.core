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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.tests.runtime.TargetException;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class GenericTypeTest extends AbstractRegressionTest {
	
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
	
	static final String RUN_SUN_JAVAC = System.getProperty("run.javac");
	static boolean runJavac = CompilerOptions.ENABLED.equals(RUN_SUN_JAVAC);
	IPath dirPath;
	
	public GenericTypeTest(String name) {
		super(name);
	}

	public static Class testClass() {
		return GenericTypeTest.class;
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
		return options;
	}
	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		testsNames = new String[] { "Bug51529a", "Bug51529b" };
		// Numbers of tests to run: "test<number>" will be run for each number of this array
//		testsNumbers = new int[] { 3, 7, 10, 21 };
		// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		testsRange = new int[] { 21, 50 };
//		testsRange = new int[] { -1, 50 }; // run all tests with a number less or equals to 50
//		testsRange = new int[] { 10, -1 }; // run all tests with a number greater or equals to 10
	}
	public static Test suite() {
		if (testsNames != null || testsNumbers!=null || testsRange!=null) {
			return new RegressionTestSetup(suite(testClass(), testClass().getName()), highestComplianceLevels());
		} else {
			// To run a specific test, just uncomment line with testNumbers in static initializer above
			// and put numbers of tests you want to perform
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
			IPath dirFilePath = writeFiles(testFiles);
			
			String testName = shortTestName();
			Process process = null;
			try {
				// Compute classpath
				String[] classpath = getDefaultClassPaths();
				StringBuffer cp = new StringBuffer();
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
				cmdLine.append(" -classpath ");
				cmdLine.append(cp);
				cmdLine.append(" -source 1.5 -deprecation -Xlint:unchecked "); // enable recommended warnings
				if (GenericTypeTest.this.dirPath.equals(dirFilePath)) {
					cmdLine.append("*.java");
				} else {
					IPath subDirPath = dirFilePath.append("*.java").removeFirstSegments(GenericTypeTest.this.dirPath.segmentCount());
					String subDirName = subDirPath.toString().substring(subDirPath.getDevice().length());
					cmdLine.append(subDirName);
				}
//				System.out.println(testName+": "+cmdLine.toString());
//				System.out.println(GenericTypeTest.this.dirPath.toFile().getAbsolutePath());
				// Launch process
				process = Runtime.getRuntime().exec(cmdLine.toString(), null, GenericTypeTest.this.dirPath.toFile());
	            // Log errors
	            Logger errorLogger = new Logger(process.getErrorStream(), "ERROR");            
	            
	            // Log output
	            Logger outputLogger = new Logger(process.getInputStream(), "OUTPUT");
	                
	            // start the threads to run outputs (standard/error)
	            errorLogger.start();
	            outputLogger.start();

	            // Wait for end of process
				int exitValue = process.waitFor();

				// Compare compilation results
				if (expectedProblemLog == null) {
					if (exitValue != 0) {
						System.out.println(testName+": javac has found error(s) although we're expecting conform result:\n");
						System.out.println(errorLogger.buffer.toString());
					}
					if (errorLogger.buffer.length() > 0) {
						System.out.println(testName+": javac displays warning(s) although we're expecting conform result:\n");
						System.out.println(errorLogger.buffer.toString());
					}
				}
				else if (expectedProblemLog != null) {
					if (exitValue == 0) {
						if (errorLogger.buffer.length() == 0) {
							System.out.println(testName+": javac has found no error/warning although we're expecting negative result:");
							System.out.println(expectedProblemLog);
						} else if (expectedProblemLog.indexOf("ERROR") >0 ){
							System.out.println(testName+": javac has found warning(s) although we're expecting error(s):");
							System.out.print("javac:\n"+errorLogger.buffer.toString());
							System.out.println("eclipse:");
							System.out.println(expectedProblemLog);
						} else {
							// TODO (frederic) compare warnings in each result and verify they are similar...
//							System.out.println(testName+": javac has found warnings :");
//							System.out.print(errorLogger.buffer.toString());
//							System.out.println(testName+": we're expecting warning results:");
//							System.out.println(expectedProblemLog);
						}
					} else if (errorLogger.buffer.length() == 0) {
						System.out.println(testName+": javac displays no output although we're expecting negative result:\n");
						System.out.println(expectedProblemLog);
					}
				}
			} catch (IOException ioe) {
				System.out.println(testName+": Not possible to launch Sun javac compilation!");
			} catch (InterruptedException e1) {
				if (process != null) process.destroy();
				System.out.println(testName+": Sun javac compilation was aborted!");
			}

			// Clean up written file(s)
			IPath testDir =  new Path(Util.getOutputDirectory()).append(shortTestName());
			cleanupDirectory(testDir.toFile());
		} catch (Exception e) {
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
				"public class X<Tx1 extends String, Tx2 extends Comparable>  extends XS<Tx2> {\n" + 
				"\n" + 
				"    public static void main(String[] args) {\n" + 
				"        Integer w = new X<String,Integer>().get(new Integer(12));\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n" + 
				"class XS <Txs> {\n" + 
				"    Txs get(Txs t) {\n" + 
				"        return t;\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}
	
	public void test002() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<Xp1 extends String, Xp2 extends Comparable>  extends XS<Xp2> {\n" + 
				"\n" + 
				"    public static void main(String[] args) {\n" + 
				"        Integer w = new X<String,Integer>().get(new Integer(12));\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"    Xp2 get(Xp2 t){\n" + 
				"        System.out.print(\"{X::get}\");\n" + 
				"        return super.get(t);\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class XS <XSp1> {\n" + 
				"    XSp1 get(XSp1 t) {\n" + 
				"        System.out.print(\"{XS::get}\");\n" + 
				"        return t;\n" + 
				"    }\n" + 
				"}\n"
			},
			"{X::get}{XS::get}SUCCESS");
	}
	
	// check cannot bind superclass to type variable
	public void test003() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <X> extends X {\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X <X> extends X {\n" + 
			"	                           ^\n" + 
			"Cannot refer to the type parameter X as a supertype\n" + 
			"----------\n");
	}
	
	// check cannot bind superinterface to type variable
	public void test004() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <X> implements X {\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X <X> implements X {\n" + 
			"	                              ^\n" + 
			"Cannot refer to the type parameter X as a supertype\n" + 
			"----------\n");
	}
	
	// check cannot bind type variable in static context
	public void test005() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    \n" + 
				"    T t;\n" + 
				"    static {\n" + 
				"        T s;\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	T s;\n" + 
			"	^\n" + 
			"Cannot make a static reference to the type parameter T\n" + 
			"----------\n");
	}
				
	// check static references to type variables
	public void test006() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    \n" + 
				"    T ok1;\n" + 
				"    static {\n" + 
				"        T wrong1;\n" + 
				"    }\n" + 
				"    static void foo(T wrong2) {\n" + 
				"		T wrong3;\n" + 
				"    }\n" + 
				"    class MX extends T {\n" + 
				"        T ok2;\n" + 
				"    }\n" + 
				"    static class SMX extends T {\n" + 
				"        T wrong4;\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	T wrong1;\n" + 
			"	^\n" + 
			"Cannot make a static reference to the type parameter T\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	static void foo(T wrong2) {\n" + 
			"	                ^\n" + 
			"Cannot make a static reference to the type parameter T\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	T wrong3;\n" + 
			"	^\n" + 
			"Cannot make a static reference to the type parameter T\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 10)\n" + 
			"	class MX extends T {\n" + 
			"	                 ^\n" + 
			"Cannot refer to the type parameter T as a supertype\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 13)\n" + 
			"	static class SMX extends T {\n" + 
			"	                         ^\n" + 
			"Cannot refer to the type parameter T as a supertype\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 14)\n" + 
			"	T wrong4;\n" + 
			"	^\n" + 
			"Cannot make a static reference to the type parameter T\n" + 
			"----------\n");
	}
	
	// check static references to type variables
	public void test007() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    \n" + 
				"    T ok1;\n" + 
				"    static class SMX {\n" + 
				"        T wrong4;\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 5)\n" + 
			"	T wrong4;\n" + 
			"	^\n" + 
			"Cannot make a static reference to the type parameter T\n" + 
			"----------\n");
	}
	
	// check static references to type variables
	public void test008() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    \n" + 
				"     T ok;\n" + 
				"    static T wrong;\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 4)\n" + 
			"	static T wrong;\n" + 
			"	       ^\n" + 
			"Cannot make a static reference to the type parameter T\n" + 
			"----------\n");
	}
	
	// Object cannot be generic
	public void test009() {
		this.runNegativeTest(
			new String[] {
				"Object.java",
				"package java.lang;\n" +
				"public class Object <T> {\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in Object.java (at line 2)\n" + 
			"	public class Object <T> {\n" + 
			"	                     ^\n" + 
			"The type java.lang.Object cannot be declared as a generic\n" + 
			"----------\n");
	}
	
	public void test010() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class Foo {} \n" + 
				"public class X<T extends Object & Comparable<? super T>> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<Foo>();\n" + 
				"    }\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 4)\n" + 
		"	new X<Foo>();\n" + 
		"	      ^^^\n" + 
		"Type mismatch: Cannot convert from Foo to the bounded parameter <T extends Object & Comparable<? super T>> of the type X<T>\n" + 
		"----------\n");
	}
	
	public void test011() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T extends Object & Comparable<? super T>> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<Foo>();\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 3)\n" + 
			"	new X<Foo>();\n" + 
			"	      ^^^\n" + 
			"Foo cannot be resolved to a type\n" + 
			"----------\n");
	}
	
	public void test012() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends String> {\n" + 
				"    T foo(T t) {\n" + 
				"        return t;\n" + 
				"    }\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        String s = new X<String>().foo(\"SUCCESS\");\n" + 
				"        System.out.println(s);\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	
	public void test013() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends String> {\n" + 
				"    T foo(T t) {\n" + 
				"        return t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>().baz(\"SUCCESS\");\n" + 
				"    }\n" + 
				"    void baz(final T t) {\n" + 
				"        new Object() {\n" + 
				"            void print() {\n" + 
				"                System.out.println(foo(t));\n" + 
				"            }\n" + 
				"        }.print();\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	
	public void test014() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends Exception> {\n" + 
				"    T foo(T t) throws T {\n" + 
				"        return t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<EX>().baz(new EX());\n" + 
				"    }\n" + 
				"    void baz(final T t) {\n" + 
				"        new Object() {\n" + 
				"            void print() {\n" + 
				"                System.out.println(foo(t));\n" + 
				"            }\n" + 
				"        }.print();\n" + 
				"    }\n" + 
				"}\n" + 
				"class EX extends Exception {\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 11)\n" + 
			"	System.out.println(foo(t));\n" + 
			"	                   ^^^^^^\n" + 
			"Unhandled exception type T\n" + 
			"----------\n");
	}
	
	public void test015() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends Exception> {\n" + 
				"    String foo() throws T {\n" + 
				"        return \"SUCCESS\";\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<EX>().baz(new EX());\n" + 
				"    }\n" + 
				"    void baz(final T t) {\n" + 
				"        new Object() {\n" + 
				"            void print() {\n" + 
				"                try {\n" + 
				"	                System.out.println(foo());\n" + 
				"                } catch (T t) {\n" + 
				"                }\n" + 
				"            }\n" + 
				"        }.print();\n" + 
				"    }\n" + 
				"}\n" + 
				"class EX extends Exception {\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	
	public void test016() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <E extends Exception> {\n" + 
				"    void foo(E e) throws E {\n" + 
				"        throw e;\n" + 
				"    }\n" + 
				"    void bar(E e) {\n" + 
				"        try {\n" + 
				"            foo(e);\n" + 
				"        } catch(E ex) {\n" + 
				"	        System.out.println(\"SUCCESS\");\n" + 
				"        }\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<Exception>().bar(new Exception());\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	public void test017() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" + 
				"public class X <E extends Exception> {\n" + 
				"    void foo(E e) throws E {\n" + 
				"        throw e;\n" + 
				"    }\n" + 
				"    void bar(E e) {\n" + 
				"        try {\n" + 
				"            foo(e);\n" + 
				"        } catch(E ex) {\n" + 
				"	        System.out.println(\"SUCCESS\");\n" + 
				"        }\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<IOException>().bar(new Exception());\n" + 
				"    }\n" + 
				"}\n" ,
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 14)\n" + 
			"	new X<IOException>().bar(new Exception());\n" + 
			"	                     ^^^\n" + 
			"The method bar(IOException) in the type X<IOException> is not applicable for the arguments (Exception)\n" + 
			"----------\n");
	}
	public void test018() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    T foo(T t) {\n" + 
				"        System.out.println(t);\n" + 
				"        return t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<XY>() {\n" + 
				"            void run() {\n" + 
				"                foo(new XY());\n" + 
				"            }\n" + 
				"        }.run();\n" + 
				"    }\n" + 
				"}\n" + 
				"class XY {\n" + 
				"    public String toString() {\n" + 
				"        return \"SUCCESS\";\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	public void test019() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"     private T foo(T t) {\n" + 
				"        System.out.println(t);\n" + 
				"        return t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<XY>() {\n" + 
				"            void run() {\n" + 
				"                foo(new XY());\n" + 
				"            }\n" + 
				"        }.run();\n" + 
				"    }\n" + 
				"}\n" + 
				"class XY {\n" + 
				"    public String toString() {\n" + 
				"        return \"SUCCESS\";\n" + 
				"    }\n" + 
				"}",
			},
			// TODO (philippe) should eliminate 1st diagnosis, as foo is still used even if incorrectly
			"----------\n" + 
			"1. WARNING in X.java (at line 2)\n" + 
			"	private T foo(T t) {\n" + 
			"	          ^^^^^^^^\n" + 
			"The private method foo(T) from the type X<T> is never used locally\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 9)\n" + 
			"	foo(new XY());\n" + 
			"	^^^\n" + 
			"The method foo(T) in the type X<T> is not applicable for the arguments (XY)\n" + 
			"----------\n");
	}
	public void test020() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"     void foo(Y<T> y) {\n" + 
				"		System.out.print(\"SUCC\");\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>().bar();\n" + 
				"    }\n" + 
				"    void bar() {\n" + 
				"        new Y<T>() {\n" + 
				"            public void pre() {\n" + 
				"                foo(this);\n" + 
				"            }\n" + 
				"        }.print(\"ESS\");\n" + 
				"    }\n" + 
				"}\n" + 
				"class Y <P> {\n" + 
				"	public void print(P p) {\n" + 
				"		pre();\n" + 
				"		System.out.println(p);\n" + 
				"	}\n" + 
				"	public void pre() {\n" + 
				"	}\n" + 
				"}",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 13)\n" + 
			"	}.print(\"ESS\");\n" + 
			"	  ^^^^^\n" + 
			"The method print(T) in the type Y<T> is not applicable for the arguments (String)\n" + 
			"----------\n");
	}
	public void test021() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends String> {\n" + 
				"    void foo(T t) {\n" + 
				"    }\n" + 
				"    void bar(String x) {\n" + 
				"        foo(x);\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>().foo(new Object());\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 1)\n" + 
			"	public class X <T extends String> {\n" + 
			"	                          ^^^^^^\n" + 
			"The type parameter T should not be bounded by the final type String. Final types cannot be further extended\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 5)\n" + 
			"	foo(x);\n" + 
			"	^^^\n" + 
			"The method foo(T) in the type X<T> is not applicable for the arguments (String)\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	new X<String>().foo(new Object());\n" + 
			"	                ^^^\n" + 
			"The method foo(String) in the type X<String> is not applicable for the arguments (Object)\n" + 
			"----------\n");
	}
	
	public void test022() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends String> {\n" + 
				"    X(T t) {\n" + 
				"        System.out.println(t);\n" + 
				"    }\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"       new X<String>(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	
	public void test023() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends String> {\n" + 
				"    X(final T t) {\n" + 
				"        new Object() {\n" + 
				"            void print() {\n" + 
				"                System.out.println(t);\n" + 
				"            }\n" + 
				"        }.print();\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	
	public void test024() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends Exception> {\n" + 
				"    X(final T t) throws T {\n" + 
				"        new Object() {\n" + 
				"            void print() {\n" + 
				"                System.out.println(t);\n" + 
				"            }\n" + 
				"        }.print();\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<EX>(new EX());\n" + 
				"    }\n" + 
				"}\n" + 
				"class EX extends Exception {\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	new X<EX>(new EX());\n" + 
			"	^^^^^^^^^^^^^^^^^^^\n" + 
			"Unhandled exception type EX\n" + 
			"----------\n");
	}
	
	public void test025() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends Exception> {\n" + 
				"    String foo() throws T {\n" + 
				"        return \"SUCCESS\";\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<EX>(new EX());\n" + 
				"    }\n" + 
				"    X(final T t) {\n" + 
				"        new Object() {\n" + 
				"            void print() {\n" + 
				"                try {\n" + 
				"	                System.out.println(foo());\n" + 
				"                } catch (T t) {\n" + 
				"                }\n" + 
				"            }\n" + 
				"        }.print();\n" + 
				"    }\n" + 
				"}\n" + 
				"class EX extends Exception {\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	
	public void test026() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <E extends Exception> {\n" + 
				"    void foo(E e) throws E {\n" + 
				"        throw e;\n" + 
				"    }\n" + 
				"    X(E e) {\n" + 
				"        try {\n" + 
				"            foo(e);\n" + 
				"        } catch(E ex) {\n" + 
				"	        System.out.println(\"SUCCESS\");\n" + 
				"        }\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<Exception>(new Exception());\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	public void test027() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" + 
				"public class X <E extends Exception> {\n" + 
				"    void foo(E e) throws E {\n" + 
				"        throw e;\n" + 
				"    }\n" + 
				"    X(E e) {\n" + 
				"        try {\n" + 
				"            foo(e);\n" + 
				"        } catch(E ex) {\n" + 
				"	        System.out.println(\"SUCCESS\");\n" + 
				"        }\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<IOException>(new Exception());\n" + 
				"    }\n" + 
				"}\n" ,
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 14)\n" + 
			"	new X<IOException>(new Exception());\n" + 
			"	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
			"The constructor X<IOException>(Exception) is undefined\n" + 
			"----------\n");
	}
	
	public void test028() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        String s = new X<String>(\"SU\").t;\n" + 
				"        System.out.print(s);\n" + 
				"        s = new X<String>(\"failed\").t = \"CC\";\n" + 
				"        System.out.print(s);\n" + 
				"        s = new X<String>(\"\").t += \"ESS\";\n" + 
				"        System.out.println(s);\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	
	public void test029() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    T t;\n" + 
				"    X() {\n" + 
				"    }\n" + 
				"    T foo(T a, T b) {\n" + 
				"        T s;\n" + 
				"        s = t = a;\n" + 
				"		s = t += b;\n" + 
				"		return t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        System.out.println(new X<String>().foo(\"SUC\", \"CESS\"));\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	s = t += b;\n" + 
			"	    ^^^^^^\n" + 
			"The operator += is undefined for the argument type(s) T, T\n" + 
			"----------\n");
	}
	
	public void test030() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    T t;\n" + 
				"    X() {\n" + 
				"    }\n" + 
				"    T foo(T a) {\n" + 
				"        T s;\n" + 
				"        s = t = a;\n" + 
				"		return t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        System.out.println(new X<String>().foo(\"SUCCESS\"));\n" + 
				"    }\n" + 
				"}\n" ,
			},
			"SUCCESS");
	}
	
	public void test031() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>(\"OUTER\").bar();\n" + 
				"    }\n" + 
				"    void bar() {\n" + 
				"        new X<String>(\"INNER\") {\n" + 
				"            void run() {\n" + 
				"                \n" + 
				"                new Object() {\n" + 
				"                    void run() {\n" + 
				"		                String s = t = \"SUC\";\n" + 
				"		                s = t+= \"CESS\";\n" + 
				"				        System.out.println(t);\n" + 
				"                    }\n" + 
				"                }.run();\n" + 
				"            }\n" + 
				"        }.run();\n" + 
				"    }\n" + 
				"}\n" ,
			},
			"SUCCESS");
	}
	
	public void test032() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>(\"OUTER\").bar();\n" + 
				"    }\n" + 
				"    void bar() {\n" + 
				"        new X<String>(\"INNER\") {\n" + 
				"            void run() {\n" + 
				"                String s = t = \"SUC\";\n" + 
				"                s = t+= \"CESS\";\n" + 
				"		        System.out.println(t);\n" + 
				"            }\n" + 
				"        }.run();\n" + 
				"    }\n" + 
				"}\n" ,
			},
			"SUCCESS");
	}
	
	public void test033() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <E, T> {\n" + 
				"	void foo(E e){}\n" + 
				"	void foo(T t){}\n" + 
				"}\n" ,
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void foo(E e){}\n" + 
			"	     ^^^^^^^^\n" + 
			"Method foo(E) has the same erasure foo(Object) as another method in type X<E,T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	void foo(T t){}\n" + 
			"	     ^^^^^^^^\n" + 
			"Method foo(T) has the same erasure foo(Object) as another method in type X<E,T>\n" + 
			"----------\n");
	}		
	
	public void test034() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <E extends Exception, T extends Exception> {\n" + 
				"	void foo(E e){}\n" + 
				"	void foo(T t){}\n" + 
				"}\n" ,
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void foo(E e){}\n" + 
			"	     ^^^^^^^^\n" + 
			"Method foo(E) has the same erasure foo(Exception) as another method in type X<E,T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	void foo(T t){}\n" + 
			"	     ^^^^^^^^\n" + 
			"Method foo(T) has the same erasure foo(Exception) as another method in type X<E,T>\n" + 
			"----------\n");
	}	
	
	public void test035() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <E extends Exception, T extends Thread> {\n" + 
				"	void foo(E e, Thread t){}\n" + 
				"	void foo(Exception e, T t){}\n" + 
				"}\n" ,
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void foo(E e, Thread t){}\n" + 
			"	     ^^^^^^^^^^^^^^^^^^\n" + 
			"Method foo(E, Thread) has the same erasure foo(Exception, Thread) as another method in type X<E,T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	void foo(Exception e, T t){}\n" + 
			"	     ^^^^^^^^^^^^^^^^^^^^^\n" + 
			"Method foo(Exception, T) has the same erasure foo(Exception, Thread) as another method in type X<E,T>\n" + 
			"----------\n");
	}	
	
	public void test036() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <E extends Exception, T extends Thread> {\n" + 
				"	void foo(E e){}\n" + 
				"	void foo(T t){}\n" + 
				"    public static void main(String[] args) {\n" + 
				"		 System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n" ,
			},
			"SUCCESS");
	}			
				
	public void test037() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <E extends Cloneable, T extends Thread & Cloneable> {\n" + 
				"	void foo(E e){}\n" + 
				"	void foo(T t){}\n" + 
				"    public static void main(String[] args) {\n" + 
				"		 System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n" ,
			},
			"SUCCESS");
	}	
	
	public void test038() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <E extends Cloneable, T extends Thread & Cloneable> {\n" + 
				"	void foo(E e){}\n" + 
				"	void foo(T t){}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		X<XY,XY> x = new X<XY, XY>();\n" + 
				"		x.foo(new XY());\n" + 
				"	}\n" + 
				"}\n" + 
				"class XY extends Thread implements Cloneable {\n" + 
				"}\n" ,
			},		"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	x.foo(new XY());\n" + 
			"	  ^^^\n" + 
			"The method foo(XY) is ambiguous for the type X<XY,XY>\n" + 
			"----------\n");
	}

	public void test039() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <E extends Cloneable, T extends Thread> {\n" + 
				"	void foo(L<E> l1){}\n" + 
				"	void foo(L<T> l2){}\n" + 
				"	void foo(L l){}\n" + 
				"}\n" + 
				"\n" + 
				"class L<E> {\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	void foo(L<E> l1){}\n" + 
			"	     ^^^^^^^^^^^^\n" + 
			"Method foo(L<E>) has the same erasure foo(L<E>) as another method in type X<E,T>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 3)\n" + 
			"	void foo(L<T> l2){}\n" + 
			"	     ^^^^^^^^^^^^\n" + 
			"Method foo(L<T>) has the same erasure foo(L<E>) as another method in type X<E,T>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 4)\n" + 
			"	void foo(L l){}\n" + 
			"	     ^^^^^^^^\n" + 
			"Duplicate method foo(L) in type X<E,T>\n" + 
			"----------\n");
	}
	
	public void test040() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends X> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }    \n" + 
				"}\n",
			},
			"SUCCESS");
	}	
	
	public void test041() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T, U extends T> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }    \n" + 
				"}\n",
			},
			"SUCCESS");
	}	
	
	public void test042() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends U, U> {\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X <T extends U, U> {\n" + 
			"	                ^\n" + 
			"Illegal forward reference to type parameter U\n" + 
			"----------\n");
	}	
	
	public void test043() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends L<T> , U extends T> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n" +
				"class L<E>{}\n",
			},
			"SUCCESS");
	}	
	
	public void test044() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X extends L<X> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }    \n" + 
				"}\n" + 
				"class L<E> {}\n",
			},
			"SUCCESS");
	}	
	
	public void test045() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    public Z<T> var;\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	public Z<T> var;\n" + 
			"	       ^\n" + 
			"Z cannot be resolved to a type\n" + 
			"----------\n");
	}
	public void test046() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    public Object<T> var;\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	public Object<T> var;\n" + 
			"	              ^\n" + 
			"T cannot be resolved to a type\n" + 
			"----------\n");
	}
	public void test047() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    private T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>(\"OUTER\").bar();\n" + 
				"    }\n" + 
				"    void bar() {\n" + 
				"        new MX<String>(\"INNER\") {\n" + 
				"            void run() {\n" + 
				"                \n" + 
				"                new Object() {\n" + 
				"                    void run() {\n" + 
				"		                String s = t = \"SUC\";\n" + 
				"		                s = t+= \"CESS\";\n" + 
				"				        System.out.println(t);\n" + 
				"                    }\n" + 
				"                }.run();\n" + 
				"            }\n" + 
				"        }.run();\n" + 
				"    }\n" + 
				"}\n" + 
				"class MX<U> {\n" + 
				"    MX(U u){}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 15)\n" + 
			"	String s = t = \"SUC\";\n" + 
			"	       ^\n" + 
			"Type mismatch: cannot convert from T to String\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 15)\n" + 
			"	String s = t = \"SUC\";\n" + 
			"	               ^^^^^\n" + 
			"Type mismatch: cannot convert from String to T\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 16)\n" + 
			"	s = t+= \"CESS\";\n" + 
			"	    ^^^^^^^^^^\n" + 
			"The operator += is undefined for the argument type(s) T, String\n" + 
			"----------\n");
	}
	// Access to enclosing 't' of type 'T' (not substituted from X<X> as private thus non inherited)
	// javac finds no error/warning on this test but it should
	public void test048() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    private T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>(\"OUTER\").bar();\n" + 
				"    }\n" + 
				"    void bar() {\n" + 
				"        new X<X>(this) {\n" + 
				"            void run() {\n" + 
				"                new Object() {\n" + 
				"                    void run() {\n" + 
				"                        X x = t;\n" + 
				"				        System.out.println(x);\n" + 
				"                    }\n" + 
				"                }.run();\n" + 
				"            }\n" + 
				"        }.run();\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 14)\n" + 
			"	X x = t;\n" + 
			"	  ^\n" + 
			"Type mismatch: cannot convert from T to X\n" + 
			"----------\n");
	}
	public void test049() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    public T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>(\"OUTER\").bar();\n" + 
				"    }\n" + 
				"    void bar() {\n" + 
				"        new X<X>(this) {\n" + 
				"            void run() {\n" + 
				"                new Object() {\n" + 
				"                    void run() {\n" + 
				"                        X x = t;\n" + 
				"				        System.out.println(\"SUCCESS:\"+x);\n" + 
				"                    }\n" + 
				"                }.run();\n" + 
				"            }\n" + 
				"        }.run();\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}
	public void test050() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"class Super {class M {}}\n" + 
				"public class X <T extends M> extends Super {\n" +
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	public class X <T extends M> extends Super {\n" + 
			"	                          ^\n" + 
			"M cannot be resolved to a type\n" + 
			"----------\n");
	}
	public void test051() {
		this.runConformTest(
			new String[] {
				"X.java",
				"class Super {class M {}}\n" + 
				"public class X extends Super {\n" + 
				"	class N <T extends M> {}\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(\"SUCCESS:\");\n" + 
				"	}\n" + 
				"}\n", 
			},
			"SUCCESS");
	}
	public void test052() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> extends p.A<T> {\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(\"SUCCESS:\");\n" + 
				"	}\n" + 
				"}\n", 
				"p/A.java",
				"package p; \n" +
				"public class A<P> {\n" + 
				"}\n", 
			},
			"SUCCESS");
	}
	public void test053() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> extends p.A<T> {\n" + 
				"    protected T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>(\"OUTER\").bar();\n" + 
				"    }\n" + 
				"    void bar() {\n" + 
				"        new X<X>(this) {\n" + 
				"            void run() {\n" + 
				"                new Object() {\n" + 
				"                    void run() {\n" + 
				"                        print(t);\n" + 
				"                    }\n" + 
				"                }.run();\n" + 
				"            }\n" + 
				"        }.run();\n" + 
				"    }\n" + 
				"}\n", 
				"p/A.java",
				"package p; \n" +
				"public class A<P> {\n" + 
				"    protected void print(P p) {\n" + 
				"        System.out.println(\"SUCCESS\"+p);\n" + 
				"    }\n" + 
				"}\n", 
			},
			"SUCCESS");
	}
	public void test054() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> extends p.A<T> {\n" + 
				"    protected T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        new X<String>(\"OUTER\").bar();\n" + 
				"    }\n" + 
				"    void bar() {\n" + 
				"        new X<X>(this) {\n" + 
				"            void run() {\n" + 
				"                new Object() {\n" + 
				"                    void run() {\n" + 
				"                        print(X.this.t);\n" + 
				"                    }\n" + 
				"                }.run();\n" + 
				"            }\n" + 
				"        }.run();\n" + 
				"    }\n" + 
				"}\n", 
				"p/A.java",
				"package p; \n" +
				"public class A<P> {\n" + 
				"    protected void print(P p) {\n" + 
				"        System.out.println(\"SUCCESS\"+p);\n" + 
				"    }\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 14)\n" + 
			"	print(X.this.t);\n" + 
			"	^^^^^\n" + 
			"The method print(X) in the type A<X> is not applicable for the arguments (T)\n" + 
			"----------\n");
	}

	public void test055() {
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
				"	  X<String> xs = new X<String>(\"SUCCESS\");\n" + 
				"	  System.out.println(xs.t);\n" + 
				"    }\n" + 
				"}\n", 
				"p/A.java",
				"package p; \n" +
				"public class A<P> {\n" + 
				"	 protected P p;\n" +
				"    protected A(P p) {\n" +
				"       this.p = p; \n" +
				"    } \n" +
				"    protected void print(P p) {\n" + 
				"        System.out.println(\"SUCCESS\"+p);\n" + 
				"    }\n" + 
				"}\n", 
			},
			"SUCCESS");
	}
	
	public void test056() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> extends p.A<T> {\n" + 
				"    protected T t;\n" + 
				"    X(T t) {\n" + 
				"        super(t);\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"	  X<String> xs = new X<String>(\"SUCCESS\");\n" + 
				"	  System.out.println((X)xs.t);\n" + 
				"    }\n" + 
				"}\n", 
				"p/A.java",
				"package p; \n" +
				"public class A<P> {\n" + 
				"	 protected P p;\n" +
				"    protected A(P p) {\n" +
				"       this.p = p; \n" +
				"    } \n" +
				"    protected void print(P p) {\n" + 
				"        System.out.println(\"SUCCESS\"+p);\n" + 
				"    }\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	System.out.println((X)xs.t);\n" + 
			"	                   ^^^^^^^\n" + 
			"Cannot cast from String to X\n" + 
			"----------\n" + 
			"----------\n" + 
			"1. WARNING in p\\A.java (at line 7)\n" + 
			"	protected void print(P p) {\n" + 
			"	                       ^\n" + 
			"The parameter p is hiding a field from type A<P>\n" + 
			"----------\n");
	}
	
	public void test057() {
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
				"	  X<X<String>> xs = new X<X<String>>(new X<String>(\"SUCCESS\"));\n" + 
				"	  System.out.println(xs.t.t);\n" + 
				"    }\n" + 
				"}\n", 
				"p/A.java",
				"package p; \n" +
				"public class A<P> {\n" + 
				"	 protected P p;\n" +
				"    protected A(P p) {\n" +
				"       this.p = p; \n" +
				"    } \n" +
				"    protected void print(P p) {\n" + 
				"        System.out.println(\"SUCCESS\"+p);\n" + 
				"    }\n" + 
				"}\n", 
			},
			"SUCCESS");
	}

	// JSR14-v10[§2.1,§2.2]: Valid multiple parameter types
	public void test058() {
		this.runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"// Valid Parameterized Type Declaration\n" + 
					"public class X<A1, A2, A3> {\n" + 
					"}\n" + 
					"// Valid Type Syntax\n" + 
					"class Y {\n" + 
					"	X<String, Number, Integer> x;\n" + 
					"}\n"
			}
		);
	}
	// JSR14-v10[§2.1,§2.2]: Invalid multiple parameter types: more declared than referenced
	public void test059() {
		this.runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"// Valid Parameterized Type Declaration\n" + 
					"public class X<A1, A2, A3, A4> {\n" + 
					"}\n" + 
					"// Invalid Valid Type Syntax (not enough parameters)\n" + 
					"class Y {\n" + 
					"	X<String, Number, Integer> x;\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in test\\X.java (at line 7)\n" + 
				"	X<String, Number, Integer> x;\n" + 
				"	^\n" + 
				"Incorrect number of arguments for type X<A1,A2,A3,A4>; it cannot be parameterized with arguments <String, Number, Integer>\n" + 
				"----------\n"
		);
	}
	// JSR14-v10[§2.1,§2.2]: Invalid multiple parameter types: more referenced than declared
	public void test060() {
		this.runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"// Valid Parameterized Type Declaration\n" + 
					"public class X<A1, A2> {\n" + 
					"}\n" + 
					"// Invalid Valid Type Syntax (too many parameters)\n" + 
					"class Y {\n" + 
					"	X<String, Number, Integer> x;\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in test\\X.java (at line 7)\n" + 
				"	X<String, Number, Integer> x;\n" + 
				"	^\n" + 
				"Incorrect number of arguments for type X<A1,A2>; it cannot be parameterized with arguments <String, Number, Integer>\n" + 
				"----------\n"
		);
	}
	// JSR14-v10[§2.1,§2.2]: Invalid multiple parameter types: primitive types
	public void test061() {
		this.runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"// Valid Parameterized Type Declaration\n" + 
					"public class X<A1, A2, A3, A4, A5, A6, A7> {\n" + 
					"}\n" + 
					"// Invalid Valid Type Syntax (primitive cannot be parameters)\n" + 
					"class Y {\n" + 
					"	X<int, short, long, float, double, boolean, char> x;\n" + 
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in test\\X.java (at line 7)\n" + 
				"	X<int, short, long, float, double, boolean, char> x;\n" + 
				"	  ^^^\n" + 
				"Syntax error on token \"int\", Dimensions expected after this token\n" + 
				"----------\n" + 
				"2. ERROR in test\\X.java (at line 7)\n" + 
				"	X<int, short, long, float, double, boolean, char> x;\n" + 
				"	       ^^^^^\n" + 
				"Syntax error on token \"short\", Dimensions expected after this token\n" + 
				"----------\n" + 
				"3. ERROR in test\\X.java (at line 7)\n" + 
				"	X<int, short, long, float, double, boolean, char> x;\n" + 
				"	              ^^^^\n" + 
				"Syntax error on token \"long\", Dimensions expected after this token\n" + 
				"----------\n" + 
				"4. ERROR in test\\X.java (at line 7)\n" + 
				"	X<int, short, long, float, double, boolean, char> x;\n" + 
				"	                    ^^^^^\n" + 
				"Syntax error on token \"float\", Dimensions expected after this token\n" + 
				"----------\n" + 
				"5. ERROR in test\\X.java (at line 7)\n" + 
				"	X<int, short, long, float, double, boolean, char> x;\n" + 
				"	                           ^^^^^^\n" + 
				"Syntax error on token \"double\", Dimensions expected after this token\n" + 
				"----------\n" + 
				"6. ERROR in test\\X.java (at line 7)\n" + 
				"	X<int, short, long, float, double, boolean, char> x;\n" + 
				"	                                   ^^^^^^^\n" + 
				"Syntax error on token \"boolean\", Dimensions expected after this token\n" + 
				"----------\n" + 
				"7. ERROR in test\\X.java (at line 7)\n" + 
				"	X<int, short, long, float, double, boolean, char> x;\n" + 
				"	                                            ^^^^\n" + 
				"Syntax error on token \"char\", Dimensions expected after this token\n" + 
				"----------\n"
		);
	}
	// JSR14-v10[§2.1,§2.2]: Valid multiple parameter types: primitive type arrays
	public void test062() {
		this.runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"// Valid Parameterized Type Declaration\n" + 
					"public class X<A1, A2, A3, A4, A5, A6, A7, A8> {\n" + 
					"}\n" + 
					"// Valid Type Syntax\n" + 
					"class Y {\n" + 
					"	X<int[], short[][], long[][][], float[][][][], double[][][][][], boolean[][][][][][], char[][][][][][][], Object[][][][][][][][][]> x;\n" + 
					"}\n"
			},
			""
		);
	}
	public void test063() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> extends p.A<T> {\n" + 
				"    \n" + 
				"    X(T t) {\n" + 
				"        super(t);\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        X x = new X(args);\n" + 
				"        X<String> xs = new X<String>(args);\n" + 
				"	}\n" + 
				"}\n", 
				"p/A.java",
				"package p; \n" +
				"public class A<P> {\n" + 
				"	 protected P p;\n" +
				"    protected A(P p) {\n" +
				"       this.p = p; \n" +
				"    } \n" +
				"    protected void print(P p) {\n" + 
				"        System.out.println(\"SUCCESS\"+p);\n" + 
				"    }\n" + 
				"}\n", 
			},
		"----------\n" + 
		"1. WARNING in X.java (at line 7)\n" + 
		"	X x = new X(args);\n" + 
		"	      ^^^^^^^^^^^\n" + 
		"Unsafe type operation: Should not invoke the constructor X(T) of raw type X. References to generic type X<T> should be parameterized\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 8)\n" + 
		"	X<String> xs = new X<String>(args);\n" + 
		"	               ^^^^^^^^^^^^^^^^^^^\n" + 
		"The constructor X<String>(String[]) is undefined\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. WARNING in p\\A.java (at line 7)\n" + 
		"	protected void print(P p) {\n" + 
		"	                       ^\n" + 
		"The parameter p is hiding a field from type A<P>\n" + 
		"----------\n");
	}
	// raw type: variable map to its strict erasure 
	public void test064() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T extends Exception & IX> {\n" + 
				"    T t;\n" + 
				"    void bar(T t) {\n" + 
				"        t.getMessage();\n" + 
				"        t.foo();\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X x = new X();\n" + // raw type
				"		x.t.getMessage();\n" + // T is strictly exception !
				"		x.t.foo();\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"interface IX {\n" + 
				"    void foo();\n" + 
				"}\n", 
			},
		"----------\n" + 
		"1. WARNING in X.java (at line 3)\n" + 
		"	void bar(T t) {\n" + 
		"	           ^\n" + 
		"The parameter t is hiding a field from type X<T>\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 10)\n" + 
		"	x.t.foo();\n" + 
		"	    ^^^\n" + 
		"The method foo() is undefined for the type Exception\n" + 
		"----------\n");
	}
	// raw type: assignments 
	public void test065() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportUnsafeTypeOperation, CompilerOptions.ERROR);		
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.io.IOException;\n" + 
				"\n" + 
				"public class X<T extends Exception> {\n" + 
				"\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X x = new X();\n" + 
				"		X<IOException> xioe = new X<IOException>(); // ok\n" + 
				"		\n" + 
				"		X x2 = xioe;\n" + 
				"		X<IOException> xioe2 = x; // unsafe\n" + 
				"	}\n" + 
				"}\n", 
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 10)\n" + 
			"	X<IOException> xioe2 = x; // unsafe\n" + 
			"	                       ^\n" + 
			"Unsafe type operation: Should not assign expression of raw type X to type X<IOException>. References to generic type X<T> should be parameterized\n" + 
			"----------\n",
			null,
			true,
			customOptions);
	}

	// JSR14-v10[§2.1,§2.2]: Invalid PT declaration (mix with reference)
	public void test066() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
				"// Valid Consecutive Parameterized Type Declaration\n" + 
				"public class X1<A1 extends X2<A2>> {\n" + 
				"	A1 a1;\n" +
				"}\n" + 
				"// Valid Parameterized Type Declaration\n" + 
				"class X2<A2>{\n" + 
				"	A2 a2;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1<A1 extends X2<A2>> {\n" + 
			"	                              ^^\n" + 
			"A2 cannot be resolved to a type\n" + 
			"----------\n"
		);
	}

	// JSR14-v10[§2.1,§2.2]: Invalid PT declaration (mix with reference)
	public void test067() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
				"// Valid Consecutive Parameterized Type Declaration\n" + 
				"public class X1< A1 extends X2	<	A2	>     			> {\n" + 
				"	A1 a1;\n" +
				"}\n" + 
				"// Valid Parameterized Type Declaration\n" + 
				"class X2<A2>{\n" + 
				"	A2 a2;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1< A1 extends X2	<	A2	>     			> {\n" + 
			"	                              	 	^^\n" + 
			"A2 cannot be resolved to a type\n" + 
			"----------\n"
		);
	}

	// JSR14-V10[§2.4]: Not terminated consecutive declaration
	// TODO (david) diagnosis message on error 3 sounds strange, doesn't it?
	public void test068() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
				"// Invalid Consecutive Parameterized Type Declaration\n" + 
				"public class X1<A1 extends X2<A2> {\n" + 
				"	A1 a1;\n" +
				"}\n" + 
				"// Invalid Parameterized Type Declaration\n" + 
				"class X2<A2 {\n" + 
				"	A2 a2;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1<A1 extends X2<A2> {\n" + 
			"	                              ^^\n" + 
			"A2 cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1<A1 extends X2<A2> {\n" + 
			"	                                ^\n" + 
			"Syntax error, insert \">\" to complete ReferenceType1\n" + 
			"----------\n" + 
			"3. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1<A1 extends X2<A2> {\n" + 
			"	                                ^\n" + 
			"Syntax error, insert \"Dimensions\" to complete ArrayType\n" + 
			"----------\n" + 
			"4. ERROR in test\\X1.java (at line 7)\n" + 
			"	class X2<A2 {\n" + 
			"	         ^^\n" + 
			"Syntax error on token \"A2\", > expected after this token\n" + 
			"----------\n"
		);
	}

	// JSR14-V10[§2.4]: Not terminated consecutive declaration
	public void test069() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
				"// Invalid Consecutive Parameterized Type Declaration\n" + 
				"public class X1<A1 extends X2<A2 {\n" + 
				"	A1 a1;\n" +
				"}\n" + 
				"// Invalid Parameterized Type Declaration\n" + 
				"class X2<A2> {\n" + 
				"	A2 a2;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1<A1 extends X2<A2 {\n" + 
			"	                              ^^\n" + 
			"Syntax error, insert \">>\" to complete ReferenceType2\n" + 
			"----------\n"
		);
	}

	// JSR14-v10[§2.4]: Unexpected consecutive PT declaration (right-shift symbol)
	// TODO (david) surround expected token with (double-)quotes
	public void test070() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
				"// Invalid Consecutive Parameterized Type Declaration\n" + 
				"public class X1<A1>> {\n" + 
				"	A1 a1;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1<A1>> {\n" + 
			"	                  ^^\n" + 
			"Syntax error on token \">>\", > expected\n" + 
			"----------\n"
		);
	}

	// JSR14-v10[§2.1,§2.2]: Unexpected consecutive PT declaration (with spaces)
	public void test071() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
				"// Invalid Consecutive Parameterized Type Declaration\n" + 
				"public class X1 < A1 > > {\n" + 
				"	A1 a1;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1 < A1 > > {\n" + 
			"	                       ^\n" + 
			"Syntax error on token \">\", delete this token\n" + 
			"----------\n"
		);
	}

	// JSR14-v10[§2.4]: Unexpected consecutive PT declaration (unary right-shift symbol)
	// TODO (david) surround expected token with (double-)quotes
	public void test072() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
				"// Invalid Consecutive Parameterized Type Declaration\n" + 
				"public class X1<A1>>> {\n" + 
				"	A1 a1;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1<A1>>> {\n" + 
			"	                  ^^^\n" + 
			"Syntax error on token \">>>\", > expected\n" + 
			"----------\n"
		);
	}

	// JSR14-v10[§2.4]: Unexpected consecutive PT declaration (right-shift symbol)
	// TODO (david) surround expected token with (double-)quotes
	public void test073() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
				"// Invalid Consecutive Parameterized Type Declaration\n" + 
				"public class X1<A1 extends X2<A1>>> {\n" + 
				"	A1 a1;\n" +
				"}\n" + 
				"// Valid Parameterized Type Declaration\n" + 
				"class X2<A2> {\n" + 
				"	A2 a2;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1<A1 extends X2<A1>>> {\n" + 
			"	                                ^^^\n" + 
			"Syntax error on token \">>>\", >> expected\n" + 
			"----------\n"
		);
	}

	// JSR14-v10[§2.1,§2.2]: Unexpected consecutive PT declaration (with spaces)
	public void test074() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
				"// Invalid Consecutive Parameterized Type Declaration\n" + 
				"public class X1 < A1 > > > {\n" + 
				"	A1 a1;\n" +
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X1.java (at line 3)\n" + 
			"	public class X1 < A1 > > > {\n" + 
			"	                       ^^^\n" + 
			"Syntax error on tokens, delete these tokens\n" + 
			"----------\n"
		);
	}
	
	// A is not an interface
	public void test075() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends Object & p.A<? super T>> extends p.A<T> {\n" + 
				"    protected T t;\n" + 
				"    X(T t) {\n" + 
				"        super(t);\n" + 
				"        this.t = t;\n" + 
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
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X <T extends Object & p.A<? super T>> extends p.A<T> {\n" + 
		"	                                   ^^^\n" + 
		"The type A<? super T> is not an interface; it cannot be specified as a bounded parameter\n" + 
		"----------\n"
		);
	}

	// A is not an interface
	public void test076() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends Object & p.A> extends p.A<T> {\n" + 
				"    protected T t;\n" + 
				"    X(T t) {\n" + 
				"        super(t);\n" + 
				"        this.t = t;\n" + 
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
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X <T extends Object & p.A> extends p.A<T> {\n" + 
			"	                                   ^^^\n" + 
			"The type A is not an interface; it cannot be specified as a bounded parameter\n" + 
			"----------\n"
		);
	}
	// unsafe type operation: only for constructors with signature change
	public void test077() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> extends p.A<T> {\n" + 
				"	 X() {\n" +
				"		super(null);\n" +
				"	}\n"+
				"    X(T t) {\n" + 
				"        super(t);\n" + 
				"    }\n" + 
				"    X(X<T> xt) {\n" + 
				"        super(xt.t);\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        X x = new X();\n" + 
				"        X x1 = new X(args);\n" + 
				"        X x2 = new X(x);\n" + 
				"        X<String> xs = new X<String>(args);\n" + 
				"	}\n" + 
				"}\n",
				"p/A.java",
				"package p;\n" + 
				"public class A<P> {\n" + 
				"    protected P p;\n" + 
				"    protected A(P p) {\n" + 
				"        this.p = p;\n" + 
				"    }\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 9)\n" + 
			"	super(xt.t);\n" + 
			"	      ^^^^\n" + 
			"xt.t cannot be resolved or is not a field\n" + 
			"----------\n" + 
			"2. WARNING in X.java (at line 13)\n" + 
			"	X x1 = new X(args);\n" + 
			"	       ^^^^^^^^^^^\n" + 
			"Unsafe type operation: Should not invoke the constructor X(T) of raw type X. References to generic type X<T> should be parameterized\n" + 
			"----------\n" + 
			"3. WARNING in X.java (at line 14)\n" + 
			"	X x2 = new X(x);\n" + 
			"	       ^^^^^^^^\n" + 
			"Unsafe type operation: Should not invoke the constructor X(X<T>) of raw type X. References to generic type X<T> should be parameterized\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 15)\n" + 
			"	X<String> xs = new X<String>(args);\n" + 
			"	               ^^^^^^^^^^^^^^^^^^^\n" + 
			"The constructor X<String>(String[]) is undefined\n" + 
			"----------\n");
	}	
	public void test078() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import p.A;\n" + 
				"public class X {\n" + 
				"    X(A<String> a, A<String> b) {\n" + 
				"    }\n" + 
				"    void foo(A<String> a) {\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        X x = new X((A)null, (A)null);\n" + 
				"        A a = new A((A)null);\n" + 
				"		x.foo(a);\n" + 
				"		a.print(x);\n" + 
				"		A<String> as = new A<String>(null);\n" + 
				"		as.print(\"hello\");\n" + 
				"	}\n" + 
				"}\n",
				"p/A.java",
				"package p;\n" +
				"public class A<P> {\n" + 
				"    protected P p;\n" + 
				"    protected A(P p) {\n" + 
				"        this.p = p;\n" + 
				"    }\n" + 
				"    protected void print(P p) {\n" + 
				"        System.out.println(\"SUCCESS\"+p);\n" + 
				"    }\n" + 
				"    protected void print(A<P> a) {\n" + 
				"        print(a.p);\n" + 
				"    }\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. WARNING in X.java (at line 8)\n" + 
		"	X x = new X((A)null, (A)null);\n" + 
		"	      ^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unsafe type operation: The constructor X(A<String>, A<String>) should not be applied for the arguments (A, A). References to generic types should be parameterized\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 9)\n" + 
		"	A a = new A((A)null);\n" + 
		"	      ^^^^^^^^^^^^^^\n" + 
		"The constructor A(P) is not visible\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 10)\n" + 
		"	x.foo(a);\n" + 
		"	^^^^^^^^\n" + 
		"Unsafe type operation: The method foo(A<String>) in the type X should not be applied for the arguments (A). References to generic types should be parameterized\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 11)\n" + 
		"	a.print(x);\n" + 
		"	  ^^^^^\n" + 
		"The method print(P) from the type A is not visible\n" + 
		"----------\n" + 
		"5. ERROR in X.java (at line 12)\n" + 
		"	A<String> as = new A<String>(null);\n" + 
		"	               ^^^^^^^^^^^^^^^^^^^\n" + 
		"The constructor A<String>(P) is not visible\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 13)\n" + 
		"	as.print(\"hello\");\n" + 
		"	   ^^^^^\n" + 
		"The method print(P) from the type A<String> is not visible\n" + 
		"----------\n" + 
		"----------\n" + 
		"1. WARNING in p\\A.java (at line 7)\n" + 
		"	protected void print(P p) {\n" + 
		"	                       ^\n" + 
		"The parameter p is hiding a field from type A<P>\n" + 
		"----------\n");
	}	

	// JSR14-v10[§2.4]: Valid consecutive Type Parameters Brackets
	public void test079() {
		this.runConformTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"public class X<A extends X1<X2<X3<String>>>> {\n" + 
					"	A a;\n" +
					"	public static void main(String[] args) {\n" + 
					"		X<X1<X2<X3<String>>>> x = new X<X1<X2<X3<String>>>>();\n" + 
					"		x.a = new X1<X2<X3<String>>>();\n" + 
					"		x.a.a1 = new X2<X3<String>>();\n" + 
					"		x.a.a1.a2 = new X3<String>();\n" + 
					"		x.a.a1.a2.a3 = \"SUCCESS\";\n" + 
					"		System.out.println(x.a.a1.a2.a3);\n" + 
					"	}\n" + 
					"}\n" + 
					"class X1<A extends X2<X3<String>>> {\n" + 
					"	A a1;\n" +
					"}\n" + 
					"class X2<A extends X3<String>> {\n" + 
					"	A a2;\n" +
					"}\n" + 
					"class X3<A> {\n" + 
					"	A a3;\n" +
					"}\n"
			},
			"SUCCESS" 
		);
	}
	// TODO (david) remove errors: insert dimension to complete array type
	public void test080() {
		this.runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"public class X<A extends X1<X2<X3<String>>> {}\n" + 
					"class X1<A extends X2<X3<String>> {}\n" + 
					"class X2<A extends X3<String> {}\n" + 
					"class X3<A {}\n"
			},
			"----------\n" + 
				"1. ERROR in test\\X.java (at line 2)\n" + 
				"	public class X<A extends X1<X2<X3<String>>> {}\n" + 
				"	                                        ^^^\n" + 
				"Syntax error, insert \">\" to complete ReferenceType1\n" + 
				"----------\n" + 
				"2. ERROR in test\\X.java (at line 2)\n" + 
				"	public class X<A extends X1<X2<X3<String>>> {}\n" + 
				"	                                        ^^^\n" + 
				"Syntax error, insert \"Dimensions\" to complete ArrayType\n" + 
				"----------\n" + 
				"3. ERROR in test\\X.java (at line 3)\n" + 
				"	class X1<A extends X2<X3<String>> {}\n" + 
				"	                               ^^\n" + 
				"Syntax error, insert \">\" to complete ReferenceType1\n" + 
				"----------\n" + 
				"4. ERROR in test\\X.java (at line 3)\n" + 
				"	class X1<A extends X2<X3<String>> {}\n" + 
				"	                               ^^\n" + 
				"Syntax error, insert \"Dimensions\" to complete ArrayType\n" + 
				"----------\n" + 
				"5. ERROR in test\\X.java (at line 4)\n" + 
				"	class X2<A extends X3<String> {}\n" + 
				"	                            ^\n" + 
				"Syntax error, insert \">\" to complete ReferenceType1\n" + 
				"----------\n" + 
				"6. ERROR in test\\X.java (at line 4)\n" + 
				"	class X2<A extends X3<String> {}\n" + 
				"	                            ^\n" + 
				"Syntax error, insert \"Dimensions\" to complete ArrayType\n" + 
				"----------\n" + 
				"7. ERROR in test\\X.java (at line 5)\n" + 
				"	class X3<A {}\n" + 
				"	         ^\n" + 
				"Syntax error on token \"A\", > expected after this token\n" + 
				"----------\n"
		);
	}
	// TODO (david) remove errors: insert dimension to complete array type
	public void test081() {
		this.runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"public class X<A extends X1<X2<X3<String>> {}\n" + 
					"class X1<A extends X2<X3<String> {}\n" + 
					"class X2<A extends X3<String {}\n" + 
					"class X3<A> {}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X.java (at line 2)\n" + 
			"	public class X<A extends X1<X2<X3<String>> {}\n" + 
			"	                                        ^^\n" + 
			"Syntax error, insert \">>\" to complete ReferenceType2\n" + 
			"----------\n" + 
			"2. ERROR in test\\X.java (at line 2)\n" + 
			"	public class X<A extends X1<X2<X3<String>> {}\n" + 
			"	                                        ^^\n" + 
			"Syntax error, insert \"Dimensions\" to complete ArrayType\n" + 
			"----------\n" + 
			"3. ERROR in test\\X.java (at line 3)\n" + 
			"	class X1<A extends X2<X3<String> {}\n" + 
			"	                               ^\n" + 
			"Syntax error, insert \">>\" to complete ReferenceType2\n" + 
			"----------\n" + 
			"4. ERROR in test\\X.java (at line 3)\n" + 
			"	class X1<A extends X2<X3<String> {}\n" + 
			"	                               ^\n" + 
			"Syntax error, insert \"Dimensions\" to complete ArrayType\n" + 
			"----------\n" + 
			"5. ERROR in test\\X.java (at line 4)\n" + 
			"	class X2<A extends X3<String {}\n" + 
			"	                      ^^^^^^\n" + 
			"Syntax error, insert \">>\" to complete ReferenceType2\n" + 
			"----------\n"
		);
	}
	// TODO (david) remove error: insert dimension to complete array type
	public void test082() {
		this.runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"public class X<A extends X1<X2<X3<String> {}\n" + 
					"class X1<A extends X2<X3<String {}\n" + 
					"class X2<A extends X3<String>> {}\n" + 
					"class X3<A> {}\n"
			},
			"----------\n" + 
				"1. ERROR in test\\X.java (at line 2)\n" + 
				"	public class X<A extends X1<X2<X3<String> {}\n" + 
				"	                                        ^\n" + 
				"Syntax error, insert \">>>\" to complete ReferenceType3\n" + 
				"----------\n" + 
				"2. ERROR in test\\X.java (at line 2)\n" + 
				"	public class X<A extends X1<X2<X3<String> {}\n" + 
				"	                                        ^\n" + 
				"Syntax error, insert \"Dimensions\" to complete ArrayType\n" + 
				"----------\n" + 
				"3. ERROR in test\\X.java (at line 3)\n" + 
				"	class X1<A extends X2<X3<String {}\n" + 
				"	                         ^^^^^^\n" + 
				"Syntax error, insert \">>>\" to complete ReferenceType3\n" + 
				"----------\n"
		);
	}
	// TODO (david) remove error: insert dimension to complete array type
	public void test083() {
		this.runNegativeTest(
			new String[] {
				"test/X.java",
				"package test;\n" +
					"public class X<A extends X1<X2<X3<String {}\n" + 
					"class X1<A extends X2<X3<String>>> {}\n" + 
					"class X2<A extends X3<String>> {}\n" + 
					"class X3<A> {}\n"
			},
			"----------\n" + 
			"1. ERROR in test\\X.java (at line 2)\n" + 
			"	public class X<A extends X1<X2<X3<String {}\n" + 
			"	                                  ^^^^^^\n" + 
			"Syntax error, insert \">\" to complete ReferenceType1\n" + 
			"----------\n" + 
			"2. ERROR in test\\X.java (at line 2)\n" + 
			"	public class X<A extends X1<X2<X3<String {}\n" + 
			"	                                  ^^^^^^\n" + 
			"Syntax error, insert \"Dimensions\" to complete ArrayType\n" + 
			"----------\n" + 
			"3. ERROR in test\\X.java (at line 2)\n" + 
			"	public class X<A extends X1<X2<X3<String {}\n" + 
			"	                                  ^^^^^^\n" + 
			"Syntax error, insert \">>>\" to complete ReferenceType3\n" + 
			"----------\n"
		);
	}
	public void test084() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    X(AX<String> a, AX<String> b) {\n" + 
				"    }\n" + 
				"    void foo(AX<String> a) {\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        X x = new X((AX)null, (AX)null);\n" + 
				"        AX a = new AX((AX)null);\n" + 
				"        AX a2 = new AX(null);\n" + 
				"		x.foo(a);\n" + 
				"		a.foo(a);\n" + 
				"		a.bar(a);\n" + 
				"		AX<String> as = new AX<String>(null);\n" + 
				"		as.print(a);\n" + 
				"		as.bar(a);\n" + 
				"	}\n" + 
				"}\n" + 
				"class AX <P> {\n" + 
				"    AX(AX<P> ax){}\n" + 
				"    AX(P p){}\n" + 
				"    void print(P p){}\n" + 
				"    void foo(AX rawAx){}\n" + 
				"    void bar(AX<P> ax){}\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. WARNING in X.java (at line 7)\n" + 
		"	X x = new X((AX)null, (AX)null);\n" + 
		"	      ^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		"Unsafe type operation: The constructor X(AX<String>, AX<String>) should not be applied for the arguments (AX, AX). References to generic types should be parameterized\n" + 
		"----------\n" + 
		"2. WARNING in X.java (at line 8)\n" + 
		"	AX a = new AX((AX)null);\n" + 
		"	       ^^^^^^^^^^^^^^^^\n" + 
		"Unsafe type operation: Should not invoke the constructor AX(AX<P>) of raw type AX. References to generic type AX<P> should be parameterized\n" + 
		"----------\n" + 
		"3. WARNING in X.java (at line 9)\n" + 
		"	AX a2 = new AX(null);\n" + 
		"	        ^^^^^^^^^^^^\n" + 
		"Unsafe type operation: Should not invoke the constructor AX(AX<P>) of raw type AX. References to generic type AX<P> should be parameterized\n" + 
		"----------\n" + 
		"4. WARNING in X.java (at line 10)\n" + 
		"	x.foo(a);\n" + 
		"	^^^^^^^^\n" + 
		"Unsafe type operation: The method foo(AX<String>) in the type X should not be applied for the arguments (AX). References to generic types should be parameterized\n" + 
		"----------\n" + 
		"5. WARNING in X.java (at line 12)\n" + 
		"	a.bar(a);\n" + 
		"	^^^^^^^^\n" + 
		"Unsafe type operation: Should not invoke the method bar(AX<P>) of raw type AX. References to generic type AX<P> should be parameterized\n" + 
		"----------\n" + 
		"6. ERROR in X.java (at line 13)\n" + 
		"	AX<String> as = new AX<String>(null);\n" + 
		"	                ^^^^^^^^^^^^^^^^^^^^\n" + 
		"The constructor AX<String>(AX<String>) is ambiguous\n" + 
		"----------\n" + 
		"7. ERROR in X.java (at line 14)\n" + 
		"	as.print(a);\n" + 
		"	   ^^^^^\n" + 
		"The method print(String) in the type AX<String> is not applicable for the arguments (AX)\n" + 
		"----------\n" + 
		"8. WARNING in X.java (at line 15)\n" + 
		"	as.bar(a);\n" + 
		"	^^^^^^^^^\n" + 
		"Unsafe type operation: The method bar(AX<P>) in the type AX<String> should not be applied for the arguments (AX). References to generic types should be parameterized\n" + 
		"----------\n");
	}		

	public void test085() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX ax = new AX();\n" + 
				"        X x = (X)ax.p;\n" + 
				"        System.out.println(x);\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class AX <P> {\n" + 
				"    \n" + 
				"    P p;\n" + 
				"}\n",
			},
		"null");
	}		

	public void test086() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportUnsafeTypeOperation, CompilerOptions.ERROR);			    
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX ax = new AX();\n" + 
				"        AX ax2 = ax.p;\n" + 
				"        ax.p = new AX<String>();\n" + 
				"        System.out.println(ax2);\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class AX <P> {\n" + 
				"    AX<P> p;\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	ax.p = new AX<String>();\n" + 
		"	^^^^\n" + 
		"Unsafe type operation: Should not assign expression of type AX<String> to the field p of raw type AX. References to generic type AX<P> should be parameterized\n" + 
		"----------\n",
		null,
		true,
		customOptions);		
	}		
	
	public void test087() {
		Map customOptions = getCompilerOptions();
		// check no unsafe type operation problem is issued
		customOptions.put(CompilerOptions.OPTION_ReportUnsafeTypeOperation, CompilerOptions.ERROR);			    
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX ax = new AX();\n" + 
				"        AX ax2 = ax.p;\n" + 
				"        AX ax3 = new AX<String>();\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class AX <P> {\n" + 
				"    AX<P> p;\n" + 
				"}\n",
			},
		"SUCCESS",
		null,
		true,
		null,
		customOptions);		
	}			
	
	public void test088() {
		Map customOptions = getCompilerOptions();
		// check no unsafe type operation problem is issued
		customOptions.put(CompilerOptions.OPTION_ReportUnsafeTypeOperation, CompilerOptions.ERROR);			    
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"     AX ax = new AX();\n" + 
				"     AX ax2 = ax.p;\n" + 
				"     AX ax3 = new AX<String>();\n" + 
				"}\n" + 
				"\n" + 
				"class AX <P> {\n" + 
				"    AX<P> p;\n" + 
				"}\n",
			},
		"",
		null,
		true,
		null,
		customOptions);		
	}				

	public void test089() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    T q;\n" + 
				"     public static void main(String[] args) {\n" + 
				"         X<String[]> xss = new X<String[]>();\n" + 
				"         X<X<String[]>> xxs = new X<X<String[]>>();\n" + 
				"         xxs.q = xss;\n" + 
				"         System.out.println(\"SUCCESS\");\n" + 
				"     }\n" + 
				"}\n",
			},
		"SUCCESS");
	}				

	public void test090() {
		Map customOptions = getCompilerOptions();
		// check no unsafe type operation problem is issued
		customOptions.put(CompilerOptions.OPTION_ReportUnsafeTypeOperation, CompilerOptions.ERROR);			    
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    T q;\n" + 
				"    \n" + 
				"     public static void main(String[] args) {\n" + 
				"         X<String[]> xss = new X<String[]>();\n" + 
				"         X<X<String[]>> xxs = new X<X<String[]>>();\n" + 
				"         xxs.q = xss;\n" + 
				"         System.out.println(\"SUCCESS\");\n" + 
				"     }\n" + 
				"      void foo(X[] xs) {\n" + 
				"          xs[0] = new X<String>();\n" + 
				"     }\n" +
				"}\n",
			},
		"SUCCESS",
		null,
		true,
		null,
		customOptions);		
	}				

	public void test091() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"      void foo(X<String>[] xs) {\n" + 
				"     }\n" +
				"}\n",
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 2)\n" + 
		"	void foo(X<String>[] xs) {\n" + 
		"	         ^\n" + 
		"An array of parameterized type X<String> is an invalid type\n" + 
		"----------\n");		
	}				

	public void test092() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    T t;\n" + 
				"    X(T t) {\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"     void foo() {\n" + 
				"         X<String> xs = new X<String>(\"\");\n" + 
				"         X<String> xs2 = (X<String>) xs;\n" + 
				"         \n" + 
				"         ((X)xs).t = this;\n" + 
				"         \n" + 
				"         System.out.prinln((T) this.t);\n" + 
				"     }\n" + 
				"     public static void main(String[] args) {\n" + 
				"		new X<String>(\"SUCCESS\").foo();\n" + 
				"	}\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. WARNING in X.java (at line 10)\n" + 
		"	((X)xs).t = this;\n" + 
		"	        ^\n" + 
		"Unsafe type operation: Should not assign expression of type X<T> to the field t of raw type X. References to generic type X<T> should be parameterized\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 12)\n" + 
		"	System.out.prinln((T) this.t);\n" + 
		"	           ^^^^^^\n" + 
		"The method prinln(T) is undefined for the type PrintStream\n" + 
		"----------\n");		
	}		

	public void test093() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX ax = new AX();\n" + 
				"        AX ax2 = new AX();\n" + 
				"        ax.p = ax2.p;\n" + // javac reports unchecked warning, which seems a bug as no difference in between lhs and rhs types
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n" + 
				"class AX <P> {\n" + 
				"    AX<P> p;\n" + 
				"}\n",
			},
		"SUCCESS");		
	}		
	
	// same as test001, but every type is now a SourceTypeBinding
	public void test094() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<Tx1 extends S, Tx2 extends C>  extends XS<Tx2> {\n" + 
				"\n" + 
				"    public static void main(String[] args) {\n" + 
				"        I w = new X<S,I>().get(new I());\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n" + 
				"class S {}\n" +
				"class I implements C<I> {}\n" + 
				"interface C<Tc> {}\n" +
				"class XS <Txs> {\n" + 
				"    Txs get(Txs t) {\n" + 
				"        return t;\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}
	public void test095() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<Tx1 extends S, Tx2 extends C>  extends XS<Tx2> {\n" + 
				"\n" + 
				"    public static void main(String[] args) {\n" + 
				"        I w = new X<S,I>().get(new I());\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n" + 
				"class S {}\n" +
				"class I implements C {}\n" + 
				"interface C<Tc> {}\n" +
				"class XS <Txs> {\n" + 
				"    Txs get(Txs t) {\n" + 
				"        return t;\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}	
	public void test096() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> extends X {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X<T> extends X {}\n" + 
			"	                          ^\n" + 
			"Cycle detected: the type X cannot extend/implement itself or one of its own member types\n" + 
			"----------\n");
	}
	public void test097() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> extends X<String> {}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 1)\n" + 
			"	public class X<T> extends X<String> {}\n" + 
			"	                          ^\n" + 
			"Cycle detected: the type X cannot extend/implement itself or one of its own member types\n" + 
			"----------\n");
	}	
	public void test098() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportUnsafeTypeOperation, CompilerOptions.ERROR);			    
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        AX ax = new AX();\n" + 
				"        AX ax2 = ax.p;\n" + 
				"        ax.p = new AX<String>();\n" + 
				"        ax.q = new AX<String>();\n" + 
				"        ax.r = new AX<Object>();\n" + 
				"        ax.s = new AX<String>();\n" + 
				"        System.out.println(ax2);\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class AX <P> {\n" + 
				"    AX<P> p;\n" + 
				"    AX<Object> q;\n" + 
				"    AX<String> r;\n" + 
				"    BX<String> s;\n" + 
				"}\n" + 
				"\n" + 
				"class BX<Q> {\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 6)\n" + 
		"	ax.p = new AX<String>();\n" + 
		"	^^^^\n" + 
		"Unsafe type operation: Should not assign expression of type AX<String> to the field p of raw type AX. References to generic type AX<P> should be parameterized\n" + 
		"----------\n" + 
		"2. ERROR in X.java (at line 7)\n" + 
		"	ax.q = new AX<String>();\n" + 
		"	^^^^\n" + 
		"Unsafe type operation: Should not assign expression of type AX<String> to the field q of raw type AX. References to generic type AX<P> should be parameterized\n" + 
		"----------\n" + 
		"3. ERROR in X.java (at line 8)\n" + 
		"	ax.r = new AX<Object>();\n" + 
		"	^^^^\n" + 
		"Unsafe type operation: Should not assign expression of type AX<Object> to the field r of raw type AX. References to generic type AX<P> should be parameterized\n" + 
		"----------\n" + 
		"4. ERROR in X.java (at line 9)\n" + 
		"	ax.s = new AX<String>();\n" + 
		"	       ^^^^^^^^^^^^^^^^\n" + 
		"Type mismatch: cannot convert from AX<String> to BX\n" + 
		"----------\n",
		null,
		true,
		customOptions);		
	}				
	// wildcard bound cannot be base type
	// TODO (david) only syntax error should be related to wilcard bound being a base type. Ripple effect is severe here.
	public void test099() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X  <T extends AX<? super int>> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"		AX<String> ax;\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"	void foo(X<?> x) {\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<P> {\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X  <T extends AX<? super int>> {\n" + 
		"	                                      ^^^\n" + 
		"Syntax error on token \"int\", Dimensions expected after this token\n" + 
		"----------\n");		
	}		

	// type parameterized with wildcard cannot appear in allocation
	public void test100() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    T t;\n" + 
				"    X(T t){\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X<? extends AX> x = new X<? extends AX>(new AX<String>());\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<P> {\n" + 
				"    P foo() { return null; }\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	X<? extends AX> x = new X<? extends AX>(new AX<String>());\n" + 
		"	                        ^\n" + 
		"Cannot instantiate the generic type X<T> using wildcard arguments (? extends AX)\n" + 
		"----------\n");		
	}		


	// wilcard may not pass parameter bound check
	public void test101() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends String> {\n" + 
				"    T t;\n" + 
				"    X(T t){\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X<? extends AX> x = new X<AX<String>>(new AX<String>());\n" + 
				"		x.t.foo(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<P> {\n" + 
				"   void foo(P p) { \n" + 
				"		System.out.println(p);\n" + 
				"   }\n" + 
				"}\n" + 
				"\n",
			},
			"----------\n" + 
			"1. WARNING in X.java (at line 1)\n" + 
			"	public class X <T extends String> {\n" + 
			"	                          ^^^^^^\n" + 
			"The type parameter T should not be bounded by the final type String. Final types cannot be further extended\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	X<? extends AX> x = new X<AX<String>>(new AX<String>());\n" + 
			"	  ^^^^^^^^^^^^\n" + 
			"Type mismatch: Cannot convert from ? extends AX to the bounded parameter <T extends String> of the type X<T>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 7)\n" + 
			"	X<? extends AX> x = new X<AX<String>>(new AX<String>());\n" + 
			"	                          ^^\n" + 
			"Type mismatch: Cannot convert from AX<String> to the bounded parameter <T extends String> of the type X<T>\n" + 
			"----------\n");		
	}		
	// unbound wildcard implicitly bound by matching parameter bounds
	public void test102() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends AX> {\n" + 
				"    T t;\n" + 
				"    X(T t){\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X<?> x = new X<BX<String>>(new BX<String>());\n" + 
				"		x.t.foo(\"SUCC\");\n" + 
				"		x.t.bar(\"ESS\");\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<P> {\n" + 
				"   void foo(P p) { \n" + 
				"		System.out.print(p);\n" + 
				"   }\n" + 
				"}\n" + 
				"\n" + 
				"class BX<Q> extends AX<Q> {\n" + 
				"   void bar(Q q) { \n" + 
				"		System.out.println(q);\n" + 
				"   }\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 9)\n" + 
		"	x.t.bar(\"ESS\");\n" + 
		"	    ^^^\n" + 
		"The method bar(String) is undefined for the type ?\n" + 
		"----------\n");		
	}		
	
	public void test103() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends AX> {\n" + 
				"    T t;\n" + 
				"    X(T t){\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X<? extends BX> x = new X<BX<String>>(new BX<String>());\n" + 
				"		x.t.foo(\"SUCC\");\n" + 
				"		x.t.bar(\"ESS\");\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<P> {\n" + 
				"   void foo(P p) { \n" + 
				"		System.out.print(p);\n" + 
				"   }\n" + 
				"}\n" + 
				"\n" + 
				"class BX<Q> extends AX<Q> {\n" + 
				"   void bar(Q q) { \n" + 
				"		System.out.println(q);\n" + 
				"   }\n" + 
				"}\n",
			},
			"SUCCESS");		
	}			

	// wildcard bound check
	public void test104() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends AX> {\n" + 
				"    T t;\n" + 
				"    X(T t){\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X<? extends BX> x = new X<AX<String>>(new AX<String>());\n" + 
				"		x.t.foo(\"SUCC\");\n" + 
				"		x.t.bar(\"ESS\");\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<P> {\n" + 
				"   void foo(P p) { \n" + 
				"		System.out.print(p);\n" + 
				"   }\n" + 
				"}\n" + 
				"\n" + 
				"class BX<Q> extends AX<Q> {\n" + 
				"   void bar(Q q) { \n" + 
				"		System.out.println(q);\n" + 
				"   }\n" + 
				"}\n",
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	X<? extends BX> x = new X<AX<String>>(new AX<String>());\n" + 
		"	                ^\n" + 
		"Type mismatch: cannot convert from X<AX<String>> to X<? extends BX>\n" + 
		"----------\n");		
	}			
	public void test105() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends AX> {\n" + 
				"    T t;\n" + 
				"    X(T t){\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X<? extends AX> x = new X<AX<String>>(new AX<String>());\n" + 
				"		x.t.foo(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<P> {\n" + 
				"   void foo(P p) { \n" + 
				"		System.out.println(p);\n" + 
				"   }\n" + 
				"}\n",
			},
			"SUCCESS");		
	}			
	public void test106() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends AX> {\n" + 
				"    T t;\n" + 
				"    X(T t){\n" + 
				"        this.t = t;\n" + 
				"    }\n" + 
				"    public static void main(String[] args) {\n" + 
				"        X<BX<String>> x = new X<BX<String>>(new BX<String>());\n" + 
				"		x.t.foo(\"SUCC\");\n" + 
				"		x.t.bar(\"ESS\");\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<P> {\n" + 
				"   void foo(P p) { \n" + 
				"		System.out.print(p);\n" + 
				"   }\n" + 
				"}\n" + 
				"\n" + 
				"class BX<Q> extends AX<Q> {\n" + 
				"   void bar(Q q) { \n" + 
				"		System.out.println(q);\n" + 
				"   }\n" + 
				"}\n",
			},
			"SUCCESS");		
	}			
	// unsafe assignment thru binaries
	public void test107() {
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportUnsafeTypeOperation, CompilerOptions.ERROR);			    
		this.runNegativeTest(
			new String[] {
				"X.java",
				"import java.util.ArrayList;\n" + 
				"\n" + 
				"public class X  {\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"        \n" + 
				"        Iterable<String> is = new ArrayList();\n" + 
				"		is.iterator();\n" + 
				"    }\n" + 
				"}\n" + 
				"\n",
			},
		"----------\n" + 
		"1. ERROR in X.java (at line 7)\n" + 
		"	Iterable<String> is = new ArrayList();\n" + 
		"	                      ^^^^^^^^^^^^^^^\n" + 
		"Unsafe type operation: Should not assign expression of raw type ArrayList to type Iterable<String>. References to generic type Iterable<T> should be parameterized\n" + 
		"----------\n",
		null,
		true,
		customOptions);		
	}			
	// class literal: Integer.class of type Class<Integer>
	public void test108() {
	    // also ensure no unsafe type operation problem is issued (assignment to variable of type raw)
		Map customOptions = getCompilerOptions();
		customOptions.put(CompilerOptions.OPTION_ReportUnsafeTypeOperation, CompilerOptions.ERROR);			    
		this.runConformTest(
			new String[] {
				"X.java",
			"public class X {\n" + 
			"    Class k;\n" + 
			"    public static void main(String args[]) {\n" + 
			"        new X().foo();\n" + 
			"    }\n" + 
			"    void foo() {\n" + 
			"        Class c = this.getClass();\n" + 
			"        this.k = this.getClass();\n" + 
			"        this.k = Integer.class;\n" + 
			"        try {\n" + 
			"            Integer i = Integer.class.newInstance();\n" + 
			"        } catch (Exception e) {\n" + 
			"        }\n" + 
			"        System.out.println(\"SUCCESS\");\n" + 
			"    }\n" + 
			"}\n",
			},
		"SUCCESS",
		null,
		true,
		null,
		customOptions);		
	}	
	// parameterized interface cannot be implemented simultaneously with distinct arguments
	public void test109() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X implements AX<String> {}\n" + 
				"class Y extends X implements AX<Thread> {}\n" + 
				"interface AX<P> {}\n" + 
				"\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class Y extends X implements AX<Thread> {}\n" + 
			"	      ^\n" + 
			"The interface AX cannot be implemented simultaneously with different arguments: AX<Thread> and AX<String>\n" + 
			"----------\n");
	}		
	public void test110() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X implements AX {}\n" + 
				"class Y extends X implements AX<Thread> {}\n" + 
				"interface AX<P> {}\n" + 
				"\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class Y extends X implements AX<Thread> {}\n" + 
			"	      ^\n" + 
			"The interface AX cannot be implemented simultaneously with different arguments: AX<Thread> and AX\n" + 
			"----------\n");		
	}		
	public void test111() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X implements AX<Object> {}\n" + 
				"class Y extends X implements AX {}\n" + 
				"interface AX<P> {}\n" + 
				"\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	class Y extends X implements AX {}\n" + 
			"	      ^\n" + 
			"The interface AX cannot be implemented simultaneously with different arguments: AX and AX<Object>\n" + 
			"----------\n");		
	}		
	// test member types
	public void test112() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T extends X.MX<Runnable>.MMX<Iterable<String>>>{\n" + 
				"    public static void main(String [] args) {\n" + 
				"        \n" + 
				"        new X<X.MX<Runnable>.MMX<Iterable<String>>>().new MX<Exception>();\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"    void foo(X<Thread>.MX.MMX<X> mx) {\n" + 
				"    }\n" + 
				"    \n" + 
				"    class MX <MT> {\n" + 
				"        class MMX <MMT> {\n" + 
				"        }\n" + 
				"    }\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\r\n" + 
			"	void foo(X<Thread>.MX.MMX<X> mx) {\r\n" + 
			"	           ^^^^^^\n" + 
			"Type mismatch: Cannot convert from Thread to the bounded parameter <T extends X.MX<Runnable>.MMX<Iterable<String>>> of the type X<T>\n" + 
			"----------\n");		
	}			
	public void test113() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"  class MX<U> {\n" + 
				"  }\n" + 
				"\n" + 
				"  public static void main(String[] args) {\n" + 
				"    new X<Thread>().foo();\n" + 
				"	System.out.println(\"SUCCESS\");\n" + 
				"  }\n" + 
				"  void foo() {\n" + 
				"		new X<String>().new MX<T>();\n" + 
				"  }\n" + 
				"}\n",
			},
			"SUCCESS");		
	}		
	public void test114() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"  class MX<U> {\n" + 
				"  }\n" + 
				"\n" + 
				"  public static void main(String[] args) {\n" + 
				"    new X<Thread>().foo(new X<String>().new MX<Thread>());\n" + 
				"  }\n" + 
				"  void foo(X.MX<Thread> mx) {\n" + 
				"	System.out.println(\"SUCCESS\");\n" + 
				"  }\n" + 
				"}\n",
			},
			"SUCCESS");		
	}			
	public void test115() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"  class MX<U> {\n" + 
				"  }\n" + 
				"\n" + 
				"  public static void main(String[] args) {\n" + 
				"    new X<Thread>().foo(new X<String>().new MX<Thread>());\n" + 
				"  }\n" + 
				"  void foo(X<String>.MX mx) {\n" + 
				"	System.out.println(\"SUCCESS\");\n" + 
				"  }\n" + 
				"}\n",
			},
			"SUCCESS");		
	}		
	public void test116() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"  class MX<U> {\n" + 
				"  }\n" + 
				"\n" + 
				"  public static void main(String[] args) {\n" + 
				"    new X<Thread>().foo(new X<String>().new MX<Thread>());\n" + 
				"  }\n" + 
				"  void foo(X.MX mx) {\n" + 
				"	System.out.println(\"SUCCESS\");\n" + 
				"  }\n" + 
				"}\n",
			},
			"SUCCESS");		
	}			
	// test member types
	public void test117() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends X.MX<Runnable>.MMX<Iterable<String>>>{\n" + 
				"    public static void main(String [] args) {\n" + 
				"        \n" + 
				"        new X<X.MX<Runnable>.MMX<Iterable<String>>>().new MX<Exception>();\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"    void foo(X<X.MX.MMX>.MX.MMX<X> mx) {\n" + 
				"    }\n" + 
				"    \n" + 
				"    class MX <MT> {\n" + 
				"        class MMX <MMT> {\n" + 
				"        }\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");		
	}				
	// test generic method with recursive parameter bound <T extends Comparable<? super T>>
	public void test118() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        java.util.Collections.sort(new java.util.LinkedList<String>());\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}
	// test binary member types
	public void test119() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T extends X.MX<Runnable>.MMX<Iterable<String>>>{\n" + 
				"    public static void main(String [] args) {\n" + 
				"        \n" + 
				"        new X<X.MX<Runnable>.MMX<Iterable<String>>>().new MX<Exception>();\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"    void foo(X<X.MX.MMX>.MX.MMX<X> mx) {\n" + 
				"    }\n" + 
				"    void foo2(X.MX.MMX<X> mx) {\n" + 
				"    }\n" + 
				"    void foo3(X<X.MX<Runnable>.MMX<Iterable<String>>> mx) {\n" + 
				"    }\n" + 
				"    \n" + 
				"    class MX <MT> {\n" + 
				"        class MMX <MMT> {\n" + 
				"        }\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");

		this.runConformTest(
			new String[] {
				"Y.java",
				"public class Y extends X {\n" + 
				"    public static void main(String [] args) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS",
			null,
			false, // do not flush output
			null);		
	}			
	// test generic method
	public void test120() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T>{\n" + 
				"    public static void main(String[] args) {\n" + 
				"        \n" + 
				"        String s = new X<String>().foo(\"SUCCESS\");\n" + 
				"	}\n" + 
				"    <U extends String> T foo (U u) {\n" + 
				"        System.out.println(u);\n" + 
				"        return null;\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}			
	// substitute array types
	public void test121() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"		new X<String>().foo(args);\n" + 
				"	}\n" + 
				"    \n" + 
				"    void foo(T[] ts) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}			
	// generic method with most specific common supertype: U --> String
	public void test122() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"		new X<String>().foo(args, new X<X<String>>());\n" + 
				"	}\n" + 
				"    <U> void foo(U[] us, X<X<U>> xxu) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n",
			},
			"SUCCESS");
	}			
	// invalid parameterized type
	public void test123() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X <T> {\n" + 
				"    T<String> ts;\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	T<String> ts;\n" + 
			"	^\n" + 
			"The type T is not generic; it cannot be parameterized with arguments <String>\n" + 
			"----------\n");
	}		
	// generic method with indirect type inference: BX<String, Thread> --> AX<W>
	public void test124() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    <W> void foo(AX<W> aw) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"     }\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"		new X().foo(new BX<String,Thread>());\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<T> {\n" + 
				"}\n" + 
				"class BX<U, V> extends AX<V> {\n" + 
				"}\n",
			},
			"SUCCESS");
	}		
	// generic method with indirect type inference: CX  --> AX<W>
	public void test125() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    <W> void foo(AX<W> aw) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"     }\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"		new X().foo(new CX());\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<T> {\n" + 
				"}\n" + 
				"class BX<U, V> extends AX<V> {\n" + 
				"}\n" + 
				"class CX extends BX<String, Thread> {\n" + 
				"}\n",
			},
			"SUCCESS");
	}			
	// variation on test125 with typo: CX extends B instead of BX.
	public void test126() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    \n" + 
				"    <W> void foo(AX<W> aw) {\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"     }\n" + 
				"    \n" + 
				"    public static void main(String[] args) {\n" + 
				"		new X().foo(new CX());\n" + 
				"	}\n" + 
				"}\n" + 
				"\n" + 
				"class AX<T> {\n" + 
				"}\n" + 
				"class BX<U, V> extends AX<V> {\n" + 
				"}\n" + 
				"class CX extends B<String, Thread> {\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 8)\n" + 
			"	new X().foo(new CX());\n" + 
			"	        ^^^\n" + 
			"The method foo(AX<W>) in the type X is not applicable for the arguments (CX)\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 16)\n" + 
			"	class CX extends B<String, Thread> {\n" + 
			"	                 ^\n" + 
			"B cannot be resolved to a type\n" + 
			"----------\n");
	}			
	// 57784: test generic method
	public void test127() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        java.util.Arrays.asList(new Object[] {\"1\"});\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}	
	// 58666: special treatment for Object#getClass declared of type: Class<? extends Object>
	// but implicitly converted to Class<? extends X> for free.
	public void test128() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { \n" + 
				"    public static void main(String[] args) {\n" + 
				"		X x = new X();\n" + 
				"		Class c1 = x.getClass();\n" + 
				"		Class<? extends X> c2 = x.getClass();\n" + 
				"		String s = \"hello\";\n" + 
				"		Class<? extends X> c3 = s.getClass();\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 7)\n" + 
			"	Class<? extends X> c3 = s.getClass();\n" + 
			"	                   ^^\n" + 
			"Type mismatch: cannot convert from Class<? extends String> to Class<? extends X>\n" + 
			"----------\n");
	}		
	// variation on test128
	public void test129() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X { \n" + 
				"\n" + 
				"    public static void main(String[] args) {\n" + 
				"		XY xy = new XY();\n" + 
				"		Class c1 = xy.getClass();\n" + 
				"		Class<? extends XY> c2 = xy.getClass();\n" + 
				"		String s = \"hello\";\n" + 
				"		Class<? extends XY> c3 = s.getClass();\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class XY extends X {\n" + 
				"    public Class <? extends Object> getClass() {\n" + 
				"        return super.getClass();\n" + 
				"    }\n" + 
				"}\n"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 6)\n" + 
			"	Class<? extends XY> c2 = xy.getClass();\n" + 
			"	                    ^^\n" + 
			"Type mismatch: cannot convert from Class<? extends Object> to Class<? extends XY>\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
			"	Class<? extends XY> c3 = s.getClass();\n" + 
			"	                    ^^\n" + 
			"Type mismatch: cannot convert from Class<? extends String> to Class<? extends XY>\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 14)\n" + 
			"	public Class <? extends Object> getClass() {\n" + 
			"	                                ^^^^^^^^^^\n" + 
			"Cannot override the final method from Object\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 15)\n" + 
			"	return super.getClass();\n" + 
			"	       ^^^^^^^^^^^^^^^^\n" + 
			"Type mismatch: cannot convert from Class<? extends X> to Class<? extends Object>\n" + 
			"----------\n");
	}			
	// getClass on array type
	public void test130() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X { \n" + 
				"\n" + 
				"    public static void main(String[] args) {\n" + 
				"		X[] x = new X[0];\n" + 
				"		Class<? extends X[]> c = x.getClass();\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}			
	// 58979
	public void test131() {
		this.runNegativeTest(
			new String[] {
				"ArrayList.java",
				" interface List<T> {\n" + 
				"	 List<T> foo();\n" + 
				"}\n" + 
				"\n" + 
				" class ArrayList<T> implements List<T> {\n" + 
				"	public List<T> foo() {\n" + 
				"		List<T> lt = this;\n" + 
				"		lt.bar();\n" + 
				"		return this;\n" + 
				"	}\n" + 
				"}\n",
			},
			"----------\n" + 
			"1. ERROR in ArrayList.java (at line 8)\n" + 
			"	lt.bar();\n" + 
			"	   ^^^\n" + 
			"The method bar() is undefined for the type List<T>\n" + 
			"----------\n");
	}
	public void test132() {
		this.runNegativeTest(
			new String[] {
				"X.java",
				"public class X {\n" +
				"  <T extends X<W>.Z> foo() {}\n" +
				"}"
			},
			"----------\n" + 
			"1. ERROR in X.java (at line 2)\n" + 
			"	<T extends X<W>.Z> foo() {}\n" + 
			"	             ^\n" + 
			"W cannot be resolved to a type\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 2)\n" + 
			"	<T extends X<W>.Z> foo() {}\n" + 
			"	                   ^^^^^\n" + 
			"Return type for the method is missing\n" + 
			"----------\n");
	}
	// TODO (philippe) enable once bridge methods are gen'ed
	public void _test133() {
		this.runConformTest(
			new String[] {
				"X.java",
				"public class X<T> {\n" + 
				"    public static void main(String[] args) {\n" + 
				"        X x = new X();\n" + 
				"        System.out.println(x.foo());\n" + 
				"    }\n" + 
				"   T foo() {return null;}\n" + 
				"   void foo(T t) {}\n" + 
				"}\n" + 
				"class Y extends X<Object> {\n" + 
				"    String foo() {return \"SUCCESS\";}\n" + 
				"    void foo(String s) {}\n" + 
				"}\n"
			},
			"SUCCESS");
	}
	// TODO (philippe) reenable when supported
	public void _test134() {
		this.runConformTest(
			new String[] {
				"Z.java",
				"import java.util.ArrayList;\n" + 
				"import java.util.List;\n" + 
				"\n" + 
				"public class Z <T extends List> { \n" + 
				"    T t;\n" + 
				"    public static void main(String[] args) {\n" + 
				"        foo(new Z<ArrayList>().set(new ArrayList<String>()));\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"    Z<T> set(T t) {\n" + 
				"        this.t = t;\n" + 
				"        return this;\n" + 
				"    }\n" + 
				"    T get() { \n" + 
				"        return this.t; \n" + 
				"    }\n" + 
				"    \n" + 
				"    static void foo(Z<? super ArrayList> za) {\n" + 
				"        za.get().isEmpty();\n" + 
				"    }\n" + 
				"}\n"
			},
			"SUCCESS");
	}	
	public void test135() {
		this.runNegativeTest(
			new String[] {
				"Z.java",
				"public class Z <T extends ZA> { \n" + 
				"    public static void main(String[] args) {\n" + 
				"        foo(new Z<ZA>());\n" + 
				"        System.out.println(\"SUCCESS\");\n" + 
				"    }\n" + 
				"    static void foo(Z<? super String> zs) {\n" + 
				"    }\n" + 
				"}\n" + 
				"\n" + 
				"class ZA {\n" + 
				"    void foo() {}\n" + 
				"}\n" + 
				"\n" + 
				"class ZB extends ZA {\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in Z.java (at line 3)\n" + 
			"	foo(new Z<ZA>());\n" + 
			"	^^^\n" + 
			"The method foo(Z<ZA>) is undefined for the type Z<T>\n" + 
			"----------\n" + 
			"2. ERROR in Z.java (at line 6)\n" + 
			"	static void foo(Z<? super String> zs) {\n" + 
			"	                  ^^^^^^^^^^^^^^\n" + 
			"Type mismatch: Cannot convert from ? super String to the bounded parameter <T extends ZA> of the type Z<T>\n" + 
			"----------\n");
	}
	public void test136() {
		this.runNegativeTest(
			new String[] {
				"Z.java",
				"public class Z <T extends ZB> { \n" + 
				"    public static void main(String[] args) {\n" + 
				"        foo(new Z<ZB>());\n" + 
				"    }\n" + 
				"    static void foo(Z<? super ZA> zs) {\n" + 
				"        zs.foo();\n" + 
				"    }\n" + 
				"}\n" + 
				"class ZA {\n" + 
				"}\n" + 
				"class ZB extends ZA {\n" + 
				"    void foo() {}\n" + 
				"}"
			},
			"----------\n" + 
			"1. ERROR in Z.java (at line 3)\n" + 
			"	foo(new Z<ZB>());\n" + 
			"	^^^\n" + 
			"The method foo(Z<ZB>) is undefined for the type Z<T>\n" + 
			"----------\n" + 
			"2. ERROR in Z.java (at line 5)\n" + 
			"	static void foo(Z<? super ZA> zs) {\n" + 
			"	                  ^^^^^^^^^^\n" + 
			"Type mismatch: Cannot convert from ? super ZA to the bounded parameter <T extends ZB> of the type Z<T>\n" + 
			"----------\n");
	}
}
