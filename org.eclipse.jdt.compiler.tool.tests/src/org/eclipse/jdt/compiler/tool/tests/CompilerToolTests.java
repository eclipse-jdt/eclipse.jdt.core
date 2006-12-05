/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.compiler.tool.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ServiceLoader;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
//import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject.Kind;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jdt.compiler.tool.EclipseCompiler;

public class CompilerToolTests extends TestCase {
	public CompilerToolTests(String name) {
		super(name);
	}
	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		suite.addTest(new CompilerToolTests("testInitializeJavaCompiler"));
		suite.addTest(new CompilerToolTests("testCheckOptions"));
		suite.addTest(new CompilerToolTests("testCompilerOneClassWithSystemCompiler"));
//		suite.addTest(new CompilerToolTests("testCompilerOneClassWithSystemCompiler2"));
		suite.addTest(new CompilerToolTests("testCompilerOneClassWithEclipseCompiler"));
		suite.addTest(new CompilerToolTests("testCompilerOneClassWithEclipseCompiler2"));
		suite.addTest(new CompilerToolTests("testCompilerOneClassWithEclipseCompiler3"));
		suite.addTest(new CompilerToolTests("testCleanUp"));
		return suite;
	}

	private static JavaCompiler Compiler;
	private static String[] ONE_ARG_OPTIONS = {
		"-cp",
		"-classpath",
		"-bootclasspath",
		"-sourcepath",
		"-extdirs",
		"-endorseddirs",
		"-d",
		"-encoding",
		"-source",
		"-target",
		"-maxProblems",
		"-log",
		"-repeat"
	};
	private static String[] ZERO_ARG_OPTIONS = {
		"-1.3",
		"-1.4",
		"-1.5",
		"-1.6",
		"-6",
		"-6.0",
		"-5",
		"-5.0",
		"-deprecation",
		"-nowarn",
		"-warn:none",
		"-?:warn",
		"-g",
		"-g:lines",
		"-g:source",
		"-g:vars",
		"-g:lines,vars",
		"-g:none",
		"-preserveAllLocals",
		"-X",
		"-O",
		"-proceedOnError",
		"-verbose",
		"-referenceInfo",
		"-progress",
		"-time",
		"-noExit",
		"-inlineJSR",
		"-enableJavadoc",
		"-Xemacs",
		"-?",
		"-help",
		"-v",
		"-version",
		"-showversion"
	};

	private void displayLocation(StandardJavaFileManager manager, StandardLocation standardLocation) {
		System.out.println(standardLocation.getName());
		Iterable<? extends File> location = manager.getLocation(standardLocation);
		if (location == null) return;
		for (File f : location) {
			System.out.println(f.getAbsolutePath());
		}
	}

	/*
	 * Initialize the compiler for all the tests
	 */
	public void testInitializeJavaCompiler() {
		ServiceLoader<JavaCompiler> javaCompilerLoader = ServiceLoader.load(JavaCompiler.class);//, EclipseCompiler.class.getClassLoader());
		int compilerCounter = 0;
		for (JavaCompiler javaCompiler : javaCompilerLoader) {
			compilerCounter++;
			if (javaCompiler instanceof EclipseCompiler) {
				Compiler = javaCompiler;
			}
	     }
		assertEquals("Only one compiler available", 1, compilerCounter);
	}

	public void testCheckOptions() {
		assertNotNull("No compiler found", Compiler);
		for (String option : ONE_ARG_OPTIONS) {
			assertEquals(option + " requires 1 argument", 1, Compiler.isSupportedOption(option));
		}
		for (String option : ZERO_ARG_OPTIONS) {
			assertEquals(option + " requires no argument", 0, Compiler.isSupportedOption(option));
		}
		assertEquals("-Jignore requires no argument", 0, Compiler.isSupportedOption("-Jignore"));
		assertEquals("-Xignore requires no argument", 0, Compiler.isSupportedOption("-Xignore"));
	}

	public void testCompilerOneClassWithSystemCompiler() {
		String tmpFolder = System.getProperty("java.io.tmpdir");
		File inputFile = new File(tmpFolder, "X.java");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(inputFile));
			writer.write(
				"package p;\n" +
				"public class X {}");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// ignore
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		// System compiler
		JavaCompiler systemCompiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager manager = systemCompiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());

		ForwardingJavaFileManager<StandardJavaFileManager> forwardingJavaFileManager = new ForwardingJavaFileManager<StandardJavaFileManager>(manager) {
			@Override
			public FileObject getFileForInput(Location location, String packageName, String relativeName)
					throws IOException {
				System.out.println("Create file for input : " + packageName + " " + relativeName + " in location " + location);
				return super.getFileForInput(location, packageName, relativeName);
			}
			@Override
			public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind)
					throws IOException {
				System.out.println("Create java file for input : " + className + " in location " + location);
				return super.getJavaFileForInput(location, className, kind);
			}
			@Override
			public JavaFileObject getJavaFileForOutput(Location location,
					String className,
					Kind kind,
					FileObject sibling) throws IOException {
				
				System.out.println("Create .class file for " + className + " in location " + location + " with sibling " + sibling.toUri());
				JavaFileObject javaFileForOutput = super.getJavaFileForOutput(location, className, kind, sibling);
				System.out.println(javaFileForOutput.toUri());
				return javaFileForOutput;
			}
		};
		// create new list containing inputfile
		List<File> files = new ArrayList<File>();
		files.add(inputFile);
		Iterable<? extends JavaFileObject> units = manager.getJavaFileObjectsFromFiles(files);
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		
		List<String> options = new ArrayList<String>();
		options.add("-d");
		options.add(tmpFolder);
 		CompilationTask task = systemCompiler.getTask(printWriter, forwardingJavaFileManager, null, options, null, units);
 		assertTrue("Has location CLASS_OUPUT ", forwardingJavaFileManager.hasLocation(StandardLocation.CLASS_OUTPUT));
		Boolean result = task.call();
		
 		if (!result.booleanValue()) {
 			System.err.println("Compilation failed: " + stringWriter.getBuffer().toString());
 	 		assertTrue("Compilation failed ", false);
 		}
		// check that the .class file exist for X
		assertTrue("delete failed", inputFile.delete());
	}
	
	/*
	 * Run the system compiler using the Eclipse java file manager
	 * TODO need to investigate why rt.jar gets removed from the PLATFORM_CLASSPATH location 
	 */ 
	public void testCompilerOneClassWithSystemCompiler2() {
		String tmpFolder = System.getProperty("java.io.tmpdir");
		File inputFile = new File(tmpFolder, "X.java");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(inputFile));
			writer.write(
				"package p;\n" +
				"public class X {}");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// ignore
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		// System compiler
		JavaCompiler systemCompiler = ToolProvider.getSystemJavaCompiler();
		StandardJavaFileManager manager = Compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());

		ForwardingJavaFileManager<StandardJavaFileManager> forwardingJavaFileManager = new ForwardingJavaFileManager<StandardJavaFileManager>(manager) {
			@Override
			public FileObject getFileForInput(Location location, String packageName, String relativeName)
					throws IOException {
				System.out.println("Create file for input : " + packageName + " " + relativeName + " in location " + location);
				return super.getFileForInput(location, packageName, relativeName);
			}
			@Override
			public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind)
					throws IOException {
				System.out.println("Create java file for input : " + className + " in location " + location);
				return super.getJavaFileForInput(location, className, kind);
			}
			@Override
			public JavaFileObject getJavaFileForOutput(Location location,
					String className,
					Kind kind,
					FileObject sibling) throws IOException {
				
				System.out.println("Create .class file for " + className + " in location " + location + " with sibling " + sibling.toUri());
				JavaFileObject javaFileForOutput = super.getJavaFileForOutput(location, className, kind, sibling);
				System.out.println(javaFileForOutput.toUri());
				return javaFileForOutput;
			}
		};
		// create new list containing inputfile
		List<File> files = new ArrayList<File>();
		files.add(inputFile);
		Iterable<? extends JavaFileObject> units = manager.getJavaFileObjectsFromFiles(files);
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		
		List<String> options = new ArrayList<String>();
		options.add("-d");
		options.add(tmpFolder);
 		CompilationTask task = systemCompiler.getTask(printWriter, manager, null, options, null, units);

 		System.out.println("Has location CLASS_OUPUT : " + forwardingJavaFileManager.hasLocation(StandardLocation.CLASS_OUTPUT));
 		
		Boolean result = task.call();
 		displayLocation(manager, StandardLocation.CLASS_PATH);
 		displayLocation(manager, StandardLocation.PLATFORM_CLASS_PATH);

 		if (!result.booleanValue()) {
 			System.err.println("Compilation failed: " + stringWriter.getBuffer().toString());
 	 		assertTrue("Compilation failed ", false);
 		}
		// check that the .class file exist for X
		assertTrue("delete failed", inputFile.delete());
	}

	public void testCompilerOneClassWithEclipseCompiler() {
		String tmpFolder = System.getProperty("java.io.tmpdir");
		File inputFile = new File(tmpFolder, "X.java");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(inputFile));
			writer.write(
				"package p;\n" +
				"public class X {}");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// ignore
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		// System compiler
		StandardJavaFileManager manager = Compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());

		ForwardingJavaFileManager<StandardJavaFileManager> forwardingJavaFileManager = new ForwardingJavaFileManager<StandardJavaFileManager>(manager) {
			@Override
			public JavaFileObject getJavaFileForOutput(Location location,
					String className,
					Kind kind,
					FileObject sibling) throws IOException {
				
				System.out.println("EC: Create .class file for " + className + " in location " + location + " with sibling " + sibling.toUri());
				JavaFileObject javaFileForOutput = super.getJavaFileForOutput(location, className, kind, sibling);
				System.out.println(javaFileForOutput.toUri());
				return javaFileForOutput;
			}
		};
		// create new list containing inputfile
		List<File> files = new ArrayList<File>();
		files.add(inputFile);
		Iterable<? extends JavaFileObject> units = manager.getJavaFileObjectsFromFiles(files);
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		
		List<String> options = new ArrayList<String>();
		options.add("-d");
		options.add(tmpFolder);
 		CompilationTask task = Compiler.getTask(printWriter, forwardingJavaFileManager, null, options, null, units);
 		// check the classpath location
 		assertTrue("Has no location CLASS_OUPUT", forwardingJavaFileManager.hasLocation(StandardLocation.CLASS_OUTPUT));
		Boolean result = task.call();
		printWriter.flush();
		printWriter.close();
 		if (!result.booleanValue()) {
 			System.err.println("Compilation failed: " + stringWriter.getBuffer().toString());
 	 		assertTrue("Compilation failed ", false);
 		}
		// check that the .class file exist for X
		assertTrue("delete failed", inputFile.delete());
	}
	
	public void testCompilerOneClassWithEclipseCompiler2() {
		String tmpFolder = System.getProperty("java.io.tmpdir");
		File inputFile = new File(tmpFolder, "X.java");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(inputFile));
			writer.write(
				"package p;\n" +
				"public class X {}");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// ignore
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		// System compiler
		StandardJavaFileManager manager = Compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());

		ForwardingJavaFileManager<StandardJavaFileManager> forwardingJavaFileManager = new ForwardingJavaFileManager<StandardJavaFileManager>(manager) {
			@Override
			public JavaFileObject getJavaFileForOutput(Location location,
					String className,
					Kind kind,
					FileObject sibling) throws IOException {
				
				System.out.println("EC: Create .class file for " + className + " in location " + location + " with sibling " + sibling.toUri());
				JavaFileObject javaFileForOutput = super.getJavaFileForOutput(location, className, kind, sibling);
				System.out.println(javaFileForOutput.toUri());
				return javaFileForOutput;
			}
		};
		// create new list containing inputfile
		List<File> files = new ArrayList<File>();
		files.add(inputFile);
		Iterable<? extends JavaFileObject> units = manager.getJavaFileObjectsFromFiles(files);
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		
		List<String> options = new ArrayList<String>();
		options.add("-d");
		options.add(tmpFolder);
 		CompilationTask task = Compiler.getTask(printWriter, forwardingJavaFileManager, null, options, null, units);
 		// check the classpath location
 		assertTrue("Has no location CLASS_OUPUT", forwardingJavaFileManager.hasLocation(StandardLocation.CLASS_OUTPUT));
		Boolean result = task.call();
		printWriter.flush();
		printWriter.close();
 		if (!result.booleanValue()) {
 			System.err.println("Compilation failed: " + stringWriter.getBuffer().toString());
 	 		assertTrue("Compilation failed ", false);
 		}
		
		stringWriter = new StringWriter();
		printWriter = new PrintWriter(stringWriter);
		task = Compiler.getTask(printWriter, forwardingJavaFileManager, null, options, null, units);
 		// check the classpath location
 		assertTrue("Has no location CLASS_OUPUT", forwardingJavaFileManager.hasLocation(StandardLocation.CLASS_OUTPUT));
		result = task.call();		
		printWriter.flush();
		printWriter.close();
 		if (!result.booleanValue()) {
 			System.err.println("Compilation failed: " + stringWriter.getBuffer().toString());
 	 		assertTrue("Compilation failed ", false);
 		}
		// check that the .class file exist for X
		assertTrue("delete failed", inputFile.delete());		
	}
	
	public void testCompilerOneClassWithEclipseCompiler3() {
		String tmpFolder = System.getProperty("java.io.tmpdir");
		File inputFile = new File(tmpFolder, "X.java");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(inputFile));
			writer.write(
				"package p;\n" +
				"public class X {}");
			writer.flush();
			writer.close();
		} catch (IOException e) {
			// ignore
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					// ignore
				}
			}
		}
		// System compiler
		StandardJavaFileManager manager = Compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());

		ForwardingJavaFileManager<StandardJavaFileManager> forwardingJavaFileManager = new ForwardingJavaFileManager<StandardJavaFileManager>(manager) {
			@Override
			public JavaFileObject getJavaFileForOutput(Location location,
					String className,
					Kind kind,
					FileObject sibling) throws IOException {
				
				System.out.println("Create .class file for " + className + " in location " + location + " with sibling " + sibling.toUri());
				JavaFileObject javaFileForOutput = super.getJavaFileForOutput(location, className, kind, sibling);
				System.out.println(javaFileForOutput.toUri());
				return javaFileForOutput;
			}
		};
		// create new list containing inputfile
		List<File> files = new ArrayList<File>();
		files.add(inputFile);
		Iterable<? extends JavaFileObject> units = manager.getJavaFileObjectsFromFiles(files);
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		
		List<String> options = new ArrayList<String>();
		options.add("-d");
		options.add(tmpFolder);
 		CompilationTask task = Compiler.getTask(printWriter, manager, null, options, null, units);
 		// check the classpath location
 		assertTrue("Has no location CLASS_OUPUT", forwardingJavaFileManager.hasLocation(StandardLocation.CLASS_OUTPUT));
		Boolean result = task.call();
		printWriter.flush();
		printWriter.close();
 		if (!result.booleanValue()) {
 			System.err.println("Compilation failed: " + stringWriter.getBuffer().toString());
 	 		assertTrue("Compilation failed ", false);
 		}
		
		try {
			task.call();
			assertTrue("Should not get there", false);
		} catch (IllegalStateException e) {
			// ignore: expected
		}

		// check that the .class file exist for X
		assertTrue("delete failed", inputFile.delete());		
	}

	/*
	 * Clean up the compiler
	 */
	public void testCleanUp() {
		Compiler = null;
	}
}
