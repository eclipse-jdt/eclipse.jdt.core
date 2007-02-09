/*******************************************************************************
 * Copyright (c) 2006, 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.compiler.apt.tests;

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
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject.Kind;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

public class CompilerAptTests extends TestCase {
	
	private static boolean _verbose = false; 
	
	// relative to plugin directory
	protected static final String _processorJarName = "export\\apttestprocessors.jar";
	
	// class that generates another class, using processor for @Echo
	protected static final String _echoSource = 
			"package p;\n" +
			"import org.eclipse.jdt.compiler.apt.tests.annotations.GenClass;\n" + 
			"import gen.XGen;\n" +
			"@GenClass(clazz=\"gen.XGen\", method=\"foo\")\n" +
			"public class X {\n" +
			"\tXGen _xgen;\n" +
			"}\n" ;
	
	// class that causes processor args to be echoed to stdout, using processor for @EchoArgs
	protected static final String _echoArgsSource = 
			"package p;\n" +
			"import org.eclipse.jdt.compiler.apt.tests.annotations.EchoArgs;\n" + 
			"@EchoArgs\n" +
			"public class TestEchoArgs {\n" +
			"}\n" ;
	
	private static final String _twoAnnotationsSource = 
		"package p;\n" +
		"import org.eclipse.jdt.compiler.apt.tests.annotations.EchoArgs;\n" + 
		"import org.eclipse.jdt.compiler.apt.tests.annotations.GenClass;\n" + 
		"import gen.XGen;\n" +
		"@GenClass(clazz=\"gen.XGen\", method=\"foo\")\n" +
		"@EchoArgs\n" +
		"public class X {\n" +
		"\tXGen _xgen;\n" +
		"}\n" ;
	
	// locations to generate files
	protected static final String _tmpFolder = System.getProperty("java.io.tmpdir") + "eclipse-temp";
	
	private static final String[] ONE_ARG_OPTIONS = {
		"-s",
		"-processor",
		"-processorpath"
	};
	private static final String[] ZERO_ARG_OPTIONS = {
		"-proc:none",
		"-proc:only",
		"-XprintProcessorInfo",
		"-XprintRounds"
	};


	protected JavaCompiler _eclipseCompiler;
	
	protected String _tmpSrcFolderName;
	protected File _tmpSrcDir;
	protected String _tmpBinFolderName;
	protected File _tmpBinDir;
	protected String _tmpGenFolderName;
	protected File _tmpGenDir;
	
	public static TestSuite suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(CompilerAptTests.class);
		return suite;
	}

	public CompilerAptTests(String name) {
		super(name);
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		_tmpBinFolderName = _tmpFolder + File.separator + "bin";
		_tmpBinDir = new File(_tmpBinFolderName);
		deleteTree(_tmpBinDir); // remove existing contents
		_tmpBinDir.mkdirs();
		assert _tmpBinDir.exists() : "couldn't mkdirs " + _tmpBinFolderName;
		
		_tmpGenFolderName = _tmpFolder + File.separator + "gen-src";
		_tmpGenDir = new File(_tmpGenFolderName);
		deleteTree(_tmpGenDir); // remove existing contents
		_tmpGenDir.mkdirs();
		assert _tmpGenDir.exists() : "couldn't mkdirs " + _tmpGenFolderName;
		
		_tmpSrcFolderName = _tmpFolder + File.separator + "src";
		_tmpSrcDir = new File(_tmpSrcFolderName);
		deleteTree(_tmpSrcDir); // remove existing contents
		_tmpSrcDir.mkdirs();
		assert _tmpSrcDir.exists() : "couldn't mkdirs " + _tmpSrcFolderName;
		
		File processorJar = new File(_processorJarName);
		assertTrue("Couldn't find processor jar at " + processorJar.getAbsolutePath(), processorJar.exists());
		
		ServiceLoader<JavaCompiler> javaCompilerLoader = ServiceLoader.load(JavaCompiler.class);//, EclipseCompiler.class.getClassLoader());
		int compilerCounter = 0;
		for (JavaCompiler javaCompiler : javaCompilerLoader) {
			compilerCounter++;
			if (javaCompiler instanceof EclipseCompiler) {
				_eclipseCompiler = javaCompiler;
			}
	     }
		assertEquals("Only one compiler available", 1, compilerCounter);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	/**
	 * Verify that Eclipse compiler properly supports apt-related command line options
	 */
	public void testCheckOptions() {
		assertNotNull("No compiler found", _eclipseCompiler);
		for (String option : ONE_ARG_OPTIONS) {
			assertEquals(option + " requires 1 argument", 1, _eclipseCompiler.isSupportedOption(option));
		}
		for (String option : ZERO_ARG_OPTIONS) {
			assertEquals(option + " requires no argument", 0, _eclipseCompiler.isSupportedOption(option));
		}
	}
	
	public void testProcessorArgumentsWithSystemCompiler() {
		// System compiler
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestProcessorArguments(compiler);
	}
	
	public void testProcessorArgumentsWithEclipseCompiler() {
		JavaCompiler compiler = _eclipseCompiler;
		internalTestProcessorArguments(compiler);
	}

	/**
	 * Read annotation values and generate a class using system compiler (javac)
	 * This is a sanity check to verify that the processors, sample code, and 
	 * compiler options are correct.
	 */
	public void testCompilerOneClassWithSystemCompiler() {
		// System compiler
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		internalTestGenerateClass(compiler);
	}
	
	/**
	 * Read annotation values and generate a class using Eclipse compiler
	 */
	public void testCompilerOneClassWithEclipseCompiler() {
		// Eclipse compiler
		JavaCompiler compiler = _eclipseCompiler;
		internalTestGenerateClass(compiler);
	}
	
	public void testTwoAnnotations() {
		File inputFile = writeSourceFile(_tmpSrcFolderName, "X.java", _twoAnnotationsSource);
		
		List<String> options = new ArrayList<String>();
		// See corresponding list in ArgsTestProc processor.
		// Processor will throw IllegalStateException if it detects a mismatch.
		options.add("-Afoo=bar");
		options.add("-Anovalue");
		compileOneClass(_eclipseCompiler, inputFile, options);
		
		// check that the src and class files were generated
 		File genSrcFile = new File(_tmpGenFolderName + File.separator + "gen" + File.separator + "XGen.java");
 		assertTrue("generated src file does not exist", genSrcFile.exists());
 		File classFile = new File(_tmpBinFolderName + File.separator + "p" + File.separator + "X.class");
 		assertTrue("ordinary src file was not compiled", classFile.exists());
 		File genClassFile = new File(_tmpBinFolderName + File.separator + "gen" + File.separator + "XGen.class");
 		assertTrue("generated src file was not compiled", genClassFile.exists());
	}
	
	/*
	 * Clean up the compiler
	 */
	public void testCleanUp() {
		_eclipseCompiler = null;
	}
	
	private void internalTestGenerateClass(JavaCompiler compiler) {
		File inputFile = writeSourceFile(_tmpSrcFolderName, "X.java", _echoSource);
		
		List<String> options = new ArrayList<String>();
		compileOneClass(compiler, inputFile, options);
		
		// check that the src and class files were generated
 		File genSrcFile = new File(_tmpGenFolderName + File.separator + "gen" + File.separator + "XGen.java");
 		assertTrue("generated src file does not exist", genSrcFile.exists());
 		File classFile = new File(_tmpBinFolderName + File.separator + "p" + File.separator + "X.class");
 		assertTrue("ordinary src file was not compiled", classFile.exists());
 		File genClassFile = new File(_tmpBinFolderName + File.separator + "gen" + File.separator + "XGen.class");
 		assertTrue("generated src file was not compiled", genClassFile.exists());
		
	}
	
	private void internalTestProcessorArguments(JavaCompiler compiler) {
		File inputFile = writeSourceFile(_tmpSrcFolderName, "TestEchoArgs.java", _echoArgsSource);
		
		List<String> options = new ArrayList<String>();
		// See corresponding list in ArgsTestProc processor.
		// Processor will throw IllegalStateException if it detects a mismatch.
		options.add("-Afoo=bar");
		options.add("-Anovalue");
		compileOneClass(compiler, inputFile, options);
	}

	/**
	 * Recursively delete the contents of a directory, including any subdirectories.
	 * This is not optimized to handle very large or deep directory trees efficiently.
	 * @param f is either a normal file (which will be deleted) or a directory
	 * (which will be emptied and then deleted).
	 */
	protected static void deleteTree(File f)
	{
		if (null == f) {
			return;
		}
		File[] children = f.listFiles();
		if (null != children) {
			// if f has any children, (recursively) delete them
			for (File child : children) {
				deleteTree(child);
			}
		}
		// At this point f is either a normal file or an empty directory
		f.delete();
	}

	/**
	 * Get a JavaFileManager that can spit out diagnostic info.
	 */
	private ForwardingJavaFileManager<StandardJavaFileManager> getForwardingFileManager(
			StandardJavaFileManager manager) {
		ForwardingJavaFileManager<StandardJavaFileManager> forwardingJavaFileManager = new ForwardingJavaFileManager<StandardJavaFileManager>(manager) {
			@Override
			public FileObject getFileForInput(Location location, String packageName, String relativeName)
					throws IOException {
				if (_verbose)
					System.out.println("Create file for input : " + packageName + " " + relativeName + " in location " + location);
				return super.getFileForInput(location, packageName, relativeName);
			}
			@Override
			public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind)
					throws IOException {
				if (_verbose)
					System.out.println("Create java file for input : " + className + " in location " + location);
				return super.getJavaFileForInput(location, className, kind);
			}
			@Override
			public JavaFileObject getJavaFileForOutput(Location location,
					String className,
					Kind kind,
					FileObject sibling) throws IOException {
				
				if (_verbose) {
					if (null != sibling) {
						System.out.println("Create .class file for " + className + " in location " + location + " with sibling " + sibling.toUri());
					}
					else {
						System.out.println("Create .class file for " + className + " in location " + location);
					}
				}
				JavaFileObject javaFileForOutput = super.getJavaFileForOutput(location, className, kind, sibling);
				if (_verbose)
					System.out.println(javaFileForOutput.toUri());
				return javaFileForOutput;
			}
		};
		return forwardingJavaFileManager;
	}

	/**
	 * Create a class that contains an annotation that generates another class, 
	 * and compile it.  Verify that generation and compilation succeeded.
	 */
	private void compileOneClass(JavaCompiler compiler, File inputFile, List<String> options) {
		StandardJavaFileManager manager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.defaultCharset());
		ForwardingJavaFileManager<StandardJavaFileManager> forwardingJavaFileManager = getForwardingFileManager(manager);
	
		
		// create new list containing inputfile
		List<File> files = new ArrayList<File>();
		files.add(inputFile);
		Iterable<? extends JavaFileObject> units = manager.getJavaFileObjectsFromFiles(files);
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		
		if (compiler == _eclipseCompiler) {
			options.add("-6.0"); // not the default for Eclipse compiler
		}
		options.add("-d");
		options.add(_tmpBinFolderName);
		options.add("-s");
		options.add(_tmpGenFolderName);
		options.add("-cp");
		options.add(_tmpSrcFolderName + File.pathSeparator + _tmpGenFolderName + File.pathSeparator + _processorJarName);
		options.add("-XprintRounds");
		CompilationTask task = compiler.getTask(printWriter, forwardingJavaFileManager, null, options, null, units);
		Boolean result = task.call();
		
		if (!result.booleanValue()) {
			System.err.println("Compilation failed: " + stringWriter.getBuffer().toString());
	 		assertTrue("Compilation failed ", false);
		}
	}

	/**
	 * Write a Java source file with the specified name and content.
	 */
	private File writeSourceFile(String tmpFolder, String name, String content) {
		File inputFile = new File(tmpFolder, name);
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(inputFile));
			writer.write(content);
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
		return inputFile;
	}
	
}
