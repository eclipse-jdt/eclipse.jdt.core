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
import org.eclipse.jdt.core.tests.runtime.TargetException;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class GenericTypeTest extends AbstractRegressionTest {
	static final String RUN_SUN_JAVAC = System.getProperty("run.javac");
	static boolean runJavac;
	{
		runJavac = RUN_SUN_JAVAC.equals(CompilerOptions.ENABLED);
	}
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
		return options;
	}
	static {
		// Use this static to specify a subset of tests using testsNames, testNumbers or testsRange arrays
//		testsRange = new int[] { 66, -1 };
//		testsNumbers = new int[] { 1006 };
	}
	public static Test suite() {
		if (testsNames != null || testsNumbers!=null || testsRange!=null) {
			return new RegressionTestSetup(suite(testClass()), highestComplianceLevels());
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
						cmdLine.append(" -source 1.5 -deprecation ");
						if (GenericTypeTest.this.dirPath.equals(dirFilePath)) {
							cmdLine.append("*.java");
						} else {
							IPath subDirPath = dirFilePath.append("*.java").removeFirstSegments(GenericTypeTest.this.dirPath.segmentCount());
							String subDirName = subDirPath.toString().substring(subDirPath.getDevice().length());
							cmdLine.append(subDirName);
						}
//						System.out.println(testName+": "+cmdLine.toString());
						// Launch process
						process = Runtime.getRuntime().exec(cmdLine.toString(), null, GenericTypeTest.this.dirPath.toFile());
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
			"Cannot refer to the type variable X as a supertype\n" + 
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
			"Cannot refer to the type variable X as a supertype\n" + 
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
			"Cannot make a static reference to the type variable T\n" + 
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
			"Cannot make a static reference to the type variable T\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 7)\n" + 
			"	static void foo(T wrong2) {\n" + 
			"	                ^\n" + 
			"Cannot make a static reference to the type variable T\n" + 
			"----------\n" + 
			"3. ERROR in X.java (at line 8)\n" + 
			"	T wrong3;\n" + 
			"	^\n" + 
			"Cannot make a static reference to the type variable T\n" + 
			"----------\n" + 
			"4. ERROR in X.java (at line 10)\n" + 
			"	class MX extends T {\n" + 
			"	                 ^\n" + 
			"Cannot refer to the type variable T as a supertype\n" + 
			"----------\n" + 
			"5. ERROR in X.java (at line 13)\n" + 
			"	static class SMX extends T {\n" + 
			"	                         ^\n" + 
			"Cannot refer to the type variable T as a supertype\n" + 
			"----------\n" + 
			"6. ERROR in X.java (at line 14)\n" + 
			"	T wrong4;\n" + 
			"	^\n" + 
			"Cannot make a static reference to the type variable T\n" + 
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
			"Cannot make a static reference to the type variable T\n" + 
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
			"Cannot make a static reference to the type variable T\n" + 
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
			"1. ERROR in Object.java (at line 1)\n" + 
			"	package java.lang;\n" + 
			"	^\n" + 
			"The type java.lang.Object cannot be declared as a generic\n" + 
			"----------\n");
	}
	
	// TODO reenable once wildcards are supported
	public void _test010() {
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
			"Type mismatch: Cannot convert from Foo to the bounded parameter <T extends Object & Comparable> of the type X\n" + 
			"----------\n");
	}
	
	// TODO reenable once wildcards are supported
	public void _test011() {
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
			"1. ERROR in X.java (at line 5)\n" + 
			"	foo(x);\n" + 
			"	^^^\n" + 
			"The method foo(T) in the type X<T> is not applicable for the arguments (String)\n" + 
			"----------\n" + 
			"2. ERROR in X.java (at line 8)\n" + 
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
			"The operator += is undefined for the argument type(s) , \n" + 
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
	
	// TODO (kent) reenable once ambiguity is detected
	public void _test038() {
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
			},
			"foo invocation is ambiguous");
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
			"	^\n" + 
			"Illegal forward reference to type variable U\n" + 
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
	// TODO (philippe) reenable once array type arguments are supported
	public void _test062() {
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
		// TODO (philippe) should also issue unchecked warning
		"----------\n" + 
		"1. ERROR in X.java (at line 8)\n" + 
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
	    // TODO raise severity of unchecked warning to ERROR
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
			"		X<IOException> xioe2 = x; // unchecked\n" + 
			"	}\n" + 
			"}\n", 
			},
		"unchecked warning assigning raw type X to X<IOException>");
	}

	// JSR14-v10[§2.4]: Valid right-shift symbol
	// TODO (philippe) seems to be a valid Parameterized type declaration, doesn't it?
	public void _test066() {
		this.runConformTest(
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
					"}\n" +
					"// Valid Type Syntax\n" + 
					"class Y {\n" + 
					"	X1<X2<String>> x = new X1<X2<String>>();\n" +
					"	{\n" +
					"		x.a1.a2 = \"SUCCESS\";\n" +
					"		System.out.println(x.a1.a2);\n" +
					"	}\n" +
					"}\n"
			},
			"SUCCESS"
		);
	}

	// JSR14-v10[§2.1,§2.2]: Valid consecutive spaced declaration
	// TODO (philippe) same as previous test
	public void _test067() {
		this.runConformTest(
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
					"}\n" +
					"// Valid Type Syntax\n" + 
					"class Y {\n" + 
					"	X1<X2<String>> x = new X1<X2<String>>();\n" +
					"	{\n" +
					"		x.a1.a2 = \"SUCCESS\";\n" +
					"		System.out.println(x.a1.a2);\n" +
					"	}\n" +
					"}\n"
			},
			"SUCCESS"
		);
	}

	// JSR14-V10[§2.4]: Not terminated consecutive declaration
	// TODO (david) diagnosis message on error 2 sounds strange, doesn't it?
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
				"	                                ^\n" + 
				"Syntax error, insert \">\" to complete ReferenceType1\n" + 
				"----------\n" + 
				"2. ERROR in test\\X1.java (at line 3)\n" + 
				"	public class X1<A1 extends X2<A2> {\n" + 
				"	                                ^\n" + 
				"Syntax error, insert \"Dimensions\" to complete ArrayType\n" + 
				"----------\n" + 
				"3. ERROR in test\\X1.java (at line 7)\n" + 
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

	// JSR14-V10[§2.4]: Consecutive declaration using unary right-shift symbol
	// TODO (philippe) seems to be a valid Parameterized type declaration, doesn't it?
	public void _test072() {
		this.runConformTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
					"// Valid Consecutive Parameterized Type Declaration (2 levels)\n" + 
					"public class X1<A1 extends X2<A2 extends X3<A3>>> {\n" + 
					"	A1 a1;\n" +
					"}\n" + 
					"// Valid Consecutive Parameterized Type Declaration\n" + 
					"class X2<A2 extends X3<A3>> {\n" + 
					"	A2 a2;\n" +
					"}\n" + 
					"// Valid Parameterized Type Declaration\n" + 
					"class X3<A3>{\n" + 
					"	A3 a3;\n" +
					"}\n" +
					"// Valid Type Syntax\n" + 
					"class Y {\n" + 
					"	X1<X2<X3<String>>> x = new X1<X2<X3<String>>>();\n" +
					"	{\n" +
					"		x.a1.a2.a3 = \"SUCCESS\";\n" +
					"		System.out.println(x.a1.a2.a3);\n" +
					"	}\n" +
					"}\n"
			},
			"SUCCESS"
		);
	}

	// JSR14-V10[§2.1,§2.2]: Consecutive declaration without unary right-shift symbol
	// TODO (philippe) same as previous test
	public void _test073() {
		this.runConformTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
					"// Valid Consecutive Parameterized Type Declaration (2 levels)\n" + 
					"public class X1 < A1 extends X2 < A2 extends X3 < A3 > > >  {\n" + 
					"	A1 a1;\n" +
					"}\n" + 
					"// Valid Consecutive Parameterized Type Declaration\n" + 
					"class X2<A2 extends X3<A3>> {\n" + 
					"	A2 a2;\n" +
					"}\n" + 
					"// Valid Parameterized Type Declaration\n" + 
					"class X3<A3>{\n" + 
					"	A3 a3;\n" +
					"}\n" +
					"// Valid Type Syntax\n" + 
					"class Y {\n" + 
					"	X1<X2<X3<String>>> x = new X1<X2<X3<String>>>();\n" +
					"	{\n" +
					"		x.a1.a2.a3 = \"SUCCESS\";\n" +
					"		System.out.println(x.a1.a2.a3);\n" +
					"	}\n" +
					"}\n"
			},
			"SUCCESS"
		);
	}

	// JSR14-V10[§2.4]: Not terminated consecutive declaration
	// TODO (david) , should be surrounded by double-quotes
	// TODO (philippe) same as test066
	public void test074() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
					"// Valid Consecutive Parameterized Type Declaration (2 levels)\n" + 
					"public class X1<A1 extends X2<A2 extends X3<A3>> {\n" + 
					"	A1 a1;\n" +
					"}\n" + 
					"// Valid Consecutive Parameterized Type Declaration\n" + 
					"class X2<A2 extends X3<A3>> {\n" + 
					"	A2 a2;\n" +
					"}\n" + 
					"// Valid Parameterized Type Declaration\n" + 
					"class X3<A3> {\n" + 
					"	A3 a3;\n" +
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in test\\X1.java (at line 3)\n" + 
				"	public class X1<A1 extends X2<A2 extends X3<A3>> {\n" + 
				"	                             ^\n" + 
				"Syntax error on token \"<\", , expected\n" + 
				"----------\n" + 
				"2. ERROR in test\\X1.java (at line 7)\n" + 
				"	class X2<A2 extends X3<A3>> {\n" + 
				"	                       ^^\n" + 
				"A3 cannot be resolved to a type\n" + 
				"----------\n"
		);
	}

	// JSR14-V10[§2.4]: Not terminated consecutive declaration
	// TODO (philippe) same as test066
	public void test075() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
					"// Valid Consecutive Parameterized Type Declaration (2 levels)\n" + 
					"public class X1<A1 extends X2<A2 extends X3<A3> {\n" + 
					"	A1 a1;\n" +
					"}\n" + 
					"// Valid Consecutive Parameterized Type Declaration\n" + 
					"class X2<A2 extends X3<A3>> {\n" + 
					"	A2 a2;\n" +
					"}\n" + 
					"// Valid Parameterized Type Declaration\n" + 
					"class X3<A3> {\n" + 
					"	A3 a3;\n" +
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in test\\X1.java (at line 3)\n" + 
				"	public class X1<A1 extends X2<A2 extends X3<A3> {\n" + 
				"	                              ^^\n" + 
				"Syntax error, insert \">>\" to complete ReferenceType2\n" + 
				"----------\n" + 
				"2. ERROR in test\\X1.java (at line 7)\n" + 
				"	class X2<A2 extends X3<A3>> {\n" + 
				"	                       ^^\n" + 
				"A3 cannot be resolved to a type\n" + 
				"----------\n"
		);
	}

	// JSR14-V10[§2.4]: Not terminated consecutive declaration
	// TODO (philippe) same as test066
	public void test076() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
					"// Valid Consecutive Parameterized Type Declaration (2 levels)\n" + 
					"public class X1<A1 extends X2<A2 extends X3<A3 {\n" + 
					"	A1 a1;\n" +
					"}\n" + 
					"// Valid Consecutive Parameterized Type Declaration\n" + 
					"class X2<A2 extends X3<A3>> {\n" + 
					"	A2 a2;\n" +
					"}\n" + 
					"// Valid Parameterized Type Declaration\n" + 
					"class X3<A3> {\n" + 
					"	A3 a3;\n" +
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in test\\X1.java (at line 3)\n" + 
				"	public class X1<A1 extends X2<A2 extends X3<A3 {\n" + 
				"	                              ^^\n" + 
				"Syntax error, insert \">>\" to complete ReferenceType2\n" + 
				"----------\n" + 
				"2. ERROR in test\\X1.java (at line 3)\n" + 
				"	public class X1<A1 extends X2<A2 extends X3<A3 {\n" + 
				"	                                            ^^\n" + 
				"Syntax error, insert \">\" to complete ReferenceType1\n" + 
				"----------\n" + 
				"3. ERROR in test\\X1.java (at line 7)\n" + 
				"	class X2<A2 extends X3<A3>> {\n" + 
				"	                       ^^\n" + 
				"A3 cannot be resolved to a type\n" + 
				"----------\n"
		);
	}

	// JSR14-v10[§2.4]: Unexpected consecutive PT declaration (unary right-shift symbol)
	// TODO (david) surround expected token with (double-)quotes
	public void test077() {
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
	public void test078() {
		this.runNegativeTest(
			new String[] {
				"test/X1.java",
				"package test;\n" +
					"// Invalid Consecutive Parameterized Type Declaration\n" + 
					"public class X1<A1 extends X2<A2>>> {\n" + 
					"	A1 a1;\n" +
					"}\n"
			},
			"----------\n" + 
				"1. ERROR in test\\X1.java (at line 3)\n" + 
				"	public class X1<A1 extends X2<A2>>> {\n" + 
				"	                                ^^^\n" + 
				"Syntax error on token \">>>\", >> expected\n" + 
				"----------\n"
		);
	}

	// JSR14-v10[§2.1,§2.2]: Unexpected consecutive PT declaration (with spaces)
	public void test079() {
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
			"	                     ^^^\n" + 
			"Syntax error on tokens, delete these tokens\n" + 
			"----------\n"
		);
	}
}
