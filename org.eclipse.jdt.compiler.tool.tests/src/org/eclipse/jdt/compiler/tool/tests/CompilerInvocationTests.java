/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.compiler.tool.tests;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.LogRecord;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import junit.framework.Test;

import org.eclipse.jdt.core.tests.compiler.regression.BatchCompilerTest;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

public class CompilerInvocationTests extends BatchCompilerTest {
	static {
//		TESTS_NAMES = new String[] { "test000" };
//		TESTS_NUMBERS = new int[] { 5 };
//		TESTS_RANGE = new int[] { 1, -1 };
	}
public CompilerInvocationTests(String name) {
	super(name);
}
public static Test suite() {
	return buildUniqueComplianceTestSuite(CompilerInvocationTests.class, ClassFileConstants.JDK1_6);
}
public static Class<CompilerInvocationTests> testClass() {
	return CompilerInvocationTests.class;
}
static class CompilerInvocationTestsArguments {
	StandardJavaFileManager standardJavaFileManager;
	List<String> options;
	String[] fileNames;
	CompilerInvocationTestsArguments(
			StandardJavaFileManager standardJavaFileManager, 
			List<String> options,
			String[] fileNames) {
		this.standardJavaFileManager = standardJavaFileManager;
		this.options = options;
		this.fileNames = fileNames;
	}
	@Override
	public String toString() {
		StringBuffer result = new StringBuffer();
		for (String option: this.options) {
			result.append(option);
			result.append(' ');
		}
		return result.toString();
	}
}
private static EclipseCompiler COMPILER = new EclipseCompiler();
private static JavaCompiler JAVAC_COMPILER = ToolProvider.getSystemJavaCompiler();

protected void checkClassFiles(String[] fileNames) {
	for (int i = 0, l = fileNames.length; i < l; i++) {
		ClassFileReader reader = null;
		try {
			reader = ClassFileReader.read(new File(OUTPUT_DIR, fileNames[i]), true);
		} catch (ClassFormatException e) {
			fail("Class format exception for file " + fileNames[i]);
		} catch (IOException e) {
			fail("IO exception for file " + fileNames[i]);
		}
		assertNotNull("Could not read " + fileNames[i], reader);
		assertEquals("Wrong Java version for " + fileNames[i], ClassFileConstants.JDK1_6, reader.getVersion());
	}
}
@Override
protected boolean invokeCompiler(
		PrintWriter out, 
		PrintWriter err,
		Object extraArguments,
		TestCompilationProgress compilationProgress) {
	CompilerInvocationTestsArguments arguments = (CompilerInvocationTestsArguments) extraArguments;
	StandardJavaFileManager manager = arguments.standardJavaFileManager;
	if (manager == null) {
		manager = JAVAC_COMPILER.getStandardFileManager(null, null, null); // will pick defaults up
	}
	List<File> files = new ArrayList<File>();
	String[] fileNames = arguments.fileNames;
	for (int i = 0, l = fileNames.length; i < l; i++) {
		files.add(new File(OUTPUT_DIR + File.separator + fileNames[i]));
	}
	CompilationTask task = COMPILER.getTask(out, manager, null, arguments.options, null, manager.getJavaFileObjectsFromFiles(files));
	assertTrue("Has no location CLASS_OUPUT", manager.hasLocation(StandardLocation.CLASS_OUTPUT));
	return task.call();
}
void runTest(
		boolean shouldCompileOK, 
		String[] sourceFiles,
		StandardJavaFileManager standardJavaFileManager,
		List<String> options,
		String[] compileFileNames,
		String expectedOutOutputString,
		String expectedErrOutputString, 
		boolean shouldFlushOutputDirectory,
		String[] classFileNames) {
	super.runTest(
		shouldCompileOK,
		sourceFiles, 
		new CompilerInvocationTestsArguments(standardJavaFileManager, options, compileFileNames),
		expectedOutOutputString,
		expectedErrOutputString,
		shouldFlushOutputDirectory,
		null /* progress */);
	checkClassFiles(classFileNames);
}
class GetLocationDetector extends ForwardingStandardJavaFileManager<StandardJavaFileManager>  {
	private Location match;
	private boolean matchFound;
	GetLocationDetector(StandardJavaFileManager javaFileManager, Location location) {
		super(javaFileManager);
		this.match = location;
	}
	@Override
	public Iterable<? extends File> getLocation(Location location) {
		if (location == this.match) {
			this.matchFound = true;
		}
		return super.getLocation(location);
	}
	boolean matchFound() {
		return this.matchFound;
	}
}
class SetLocationDetector extends ForwardingStandardJavaFileManager<StandardJavaFileManager>  {
	private Location match;
	private boolean matchFound;
	SetLocationDetector(StandardJavaFileManager javaFileManager, Location location) {
		super(javaFileManager);
		this.match = location;
	}
	@Override
	public void setLocation(Location location, Iterable<? extends File> path)
			throws IOException {
		if (location == this.match) {
			this.matchFound = true;
		}
		super.setLocation(location, path);
	}
	boolean matchFound() {
		return this.matchFound;
	}
}
class SubstringDetector extends java.util.logging.Logger {
	private String match;
	private boolean matchFound;
	SubstringDetector(String match) {
		super("SubstringDetector", null);
		this.match = match;
	}
	@Override
	public void log(LogRecord record) {
		if (!this.matchFound && record.getMessage().indexOf(this.match) != -1) {
			this.matchFound = true;
		}
	}
	boolean matchFound() {
		return this.matchFound;
	}
}
// most possibly basic test
public void test001_basic() {
	runTest(
		true /* shouldCompileOK */,
		new String [] { /* sourceFiles */
			"X.java",
			"public class X {}",
		}, 
		null /* standardJavaFileManager */,
		Arrays.asList("-d", OUTPUT_DIR) /* options */, 
		new String[] { /* compileFileNames */
			"X.java"
		}, 
		"" /* expectedOutOutputString */, 
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		new String[] { /* classFileNames */
			"X.class"
		});
}
// exploring -d / FileManager interaction
// -d changes CLASS_OUTPUT location
public void test002_dash_d_option() {
	StandardJavaFileManager javacStandardJavaFileManager =  JAVAC_COMPILER.getStandardFileManager(null, null, null); // will pick defaults up
	runTest(
		true /* shouldCompileOK */,
		new String [] { /* sourceFiles */
			"X.java",
			"public class X {}",
		}, 
		javacStandardJavaFileManager /* standardJavaFileManager */,
		Arrays.asList("-d", OUTPUT_DIR) /* options */, 
		new String[] { /* compileFileNames */
			"X.java"
		}, 
		"" /* expectedOutOutputString */, 
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		new String[] { /* classFileNames */
			"X.class"
		});
	assertEquals(OUTPUT_DIR, javacStandardJavaFileManager.getLocation(StandardLocation.CLASS_OUTPUT).toString());
}
// exploring -d / FileManager interaction
// -d changes CLASS_OUTPUT location (OUTPUT_DIR subdirectory)
public void test003_dash_d_option() {
	StandardJavaFileManager javacStandardJavaFileManager =  JAVAC_COMPILER.getStandardFileManager(null, null, null); // will pick defaults up
	String outputDir = OUTPUT_DIR + File.separator + "bin";
	runTest(
		true /* shouldCompileOK */,
		new String [] { /* sourceFiles */
			"src/X.java",
			"public class X {}",
		}, 
		javacStandardJavaFileManager /* standardJavaFileManager */,
		Arrays.asList("-d", outputDir) /* options */, 
		new String[] { /* compileFileNames */
			"src/X.java"
		}, 
		"" /* expectedOutOutputString */, 
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		new String[] { /* classFileNames */
			"bin/X.class"
		});
	assertEquals(outputDir, javacStandardJavaFileManager.getLocation(StandardLocation.CLASS_OUTPUT).toString());
}
// exploring -d / FileManager interaction
// ecj uses the output location from the javac standard Java file manager if it
// is set
public void test004_no_dash_d_option() throws IOException {
	File binDirectory = new File(OUTPUT_DIR + File.separator + "bin");
	binDirectory.mkdir();
	StandardJavaFileManager javacStandardJavaFileManager =  JAVAC_COMPILER.getStandardFileManager(null, null, null); // will pick defaults up
	javacStandardJavaFileManager.setLocation(
			StandardLocation.CLASS_OUTPUT, 
			Arrays.asList(binDirectory));
	runTest(
		true /* shouldCompileOK */,
		new String [] { /* sourceFiles */
			"src/X.java",
			"public class X {}",
		}, 
		javacStandardJavaFileManager /* standardJavaFileManager */,
		null /* options */, 
		new String[] { /* compileFileNames */
			"src/X.java"
		}, 
		"" /* expectedOutOutputString */, 
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		new String[] { /* classFileNames */
			"bin/X.class"
		});
}
// exploring -d / FileManager interaction
// ecj does not call setLocation on standard Java file managers; it uses 
// handleOption instead; javac does the same
public void test005_dash_d_option_custom_file_manager() {
	StandardJavaFileManager javacJavaFileManager = JAVAC_COMPILER.getStandardFileManager(null, null, null);
	SetLocationDetector customJavaFileManager =
		new SetLocationDetector(
				javacJavaFileManager, 
				StandardLocation.CLASS_OUTPUT);
	runTest(
		true /* shouldCompileOK */,
		new String [] { /* sourceFiles */
			"X.java",
			"public class X {}",
		}, 
		customJavaFileManager /* standardJavaFileManager */,
		Arrays.asList("-d", OUTPUT_DIR) /* options */, 
		new String[] { /* compileFileNames */
			"X.java"
		}, 
		"" /* expectedOutOutputString */, 
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		new String[] { /* classFileNames */
			"X.class"
		});
	assertEquals(OUTPUT_DIR, customJavaFileManager.getLocation(StandardLocation.CLASS_OUTPUT).toString());
	assertFalse(customJavaFileManager.matchFound());
	if (RUN_JAVAC) {
		customJavaFileManager =	new SetLocationDetector(javacJavaFileManager, 
					StandardLocation.CLASS_OUTPUT);
		assertTrue(JAVAC_COMPILER.getTask(null, customJavaFileManager, null, 
				Arrays.asList("-d", OUTPUT_DIR), null, 
				customJavaFileManager.getJavaFileObjectsFromFiles(
						Arrays.asList(new File(OUTPUT_DIR + File.separator + "X.java")))).call());
		assertFalse(customJavaFileManager.matchFound());
	}
}
// exploring -d / FileManager interaction
// ecj calls getLocation on a non-javac standard Java file manager
public void test006_no_dash_d_option_custom_file_manager() throws IOException {
	File binDirectory = new File(OUTPUT_DIR + File.separator + "bin");
	binDirectory.mkdirs();
	GetLocationDetector customJavaFileManager =
		new GetLocationDetector(
				JAVAC_COMPILER.getStandardFileManager(null, null, null),
				StandardLocation.CLASS_OUTPUT);
	customJavaFileManager.setLocation(
			StandardLocation.CLASS_OUTPUT, 
			Arrays.asList(binDirectory));
	runTest(
		true /* shouldCompileOK */,
		new String [] { /* sourceFiles */
			"src/X.java",
			"public class X {}",
		}, 
		customJavaFileManager /* standardJavaFileManager */,
		null /* options */, 
		new String[] { /* compileFileNames */
			"src/X.java"
		}, 
		"" /* expectedOutOutputString */, 
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		new String[] { /* classFileNames */
			"bin/X.class"
		});
	assertTrue(customJavaFileManager.matchFound()); // failure here means that getLocation was not called for the class output location
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=226918
// options consumption - compare with javac and ensure the consumption mechanism
// behaves the same on an option that is supported by both compilers
public void test007_options_consumption() throws IOException {
	List<String> remainingAsList = Arrays.asList("output", "remainder");
	StandardJavaFileManager ecjStandardJavaFileManager = 
		COMPILER.getStandardFileManager(null /* diagnosticListener */, null /* locale */, null /* charset */);
	Iterator<String> remaining = remainingAsList.iterator();
	assertTrue("does not support -d option", ecjStandardJavaFileManager.handleOption("-d", remaining));
	assertEquals("unexpected consumption rate", "remainder", remaining.next());
	if (RUN_JAVAC) {
		StandardJavaFileManager javacStandardJavaFileManager =  
			ToolProvider.getSystemJavaCompiler().getStandardFileManager(null, null, null); // will pick defaults up
		remaining = remainingAsList.iterator();
		assertTrue("does not support -d option", javacStandardJavaFileManager.handleOption("-d", remaining));
		assertEquals("unexpected consumption rate", "remainder", remaining.next());
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=226918
// options consumption - check consumption rate on supported zero-args options
public void test008_options_consumption() throws IOException {
	final String REMAINDER = "remainder";
	List<String> remainingAsList = Arrays.asList("output", REMAINDER);
	StandardJavaFileManager ecjStandardJavaFileManager = 
		COMPILER.getStandardFileManager(null /* diagnosticListener */, null /* locale */, null /* charset */);
	for (String option: CompilerToolTests.ZERO_ARG_OPTIONS) {
		if (ecjStandardJavaFileManager.isSupportedOption(option) != -1) { // some options that the compiler support could well not be supported by the file manager
			Iterator<String> remaining = remainingAsList.iterator();
			assertTrue("does not support " + option + " option", ecjStandardJavaFileManager.handleOption(option, remaining));
			assertEquals("unexpected consumption rate", REMAINDER, remaining.next());
		}
	}
	for (String option: CompilerToolTests.FAKE_ZERO_ARG_OPTIONS) {
		if (ecjStandardJavaFileManager.isSupportedOption(option) != -1) {
			Iterator<String> remaining = remainingAsList.iterator();
			assertTrue("does not support " + option + " option", ecjStandardJavaFileManager.handleOption(option, remaining));
			assertEquals("unexpected consumption rate", REMAINDER, remaining.next());
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=226918
// options consumption - check consumption rate on supported one-arg options
public void test009_options_consumption() throws IOException {
	final String REMAINDER = "remainder";
	List<String> remainingAsList = Arrays.asList("utf-8", REMAINDER);
	StandardJavaFileManager ecjStandardJavaFileManager = 
		COMPILER.getStandardFileManager(null /* diagnosticListener */, null /* locale */, null /* charset */);
	for (String option: CompilerToolTests.ONE_ARG_OPTIONS) {
		if (ecjStandardJavaFileManager.isSupportedOption(option) != -1) { // some options that the compiler support could well not be supported by the file manager
			Iterator<String> remaining = remainingAsList.iterator();
			assertTrue("does not support " + option + " option", ecjStandardJavaFileManager.handleOption(option, remaining));
			assertEquals("unexpected consumption rate", REMAINDER, remaining.next());
		}
	}
}
// tests #10-11 show that ecj throws a RuntimeException when encountering a wrong
// encoding in its parameters, while the default compiler swallows it silently
// based upon the behavior of the command-line javac for the same level, we
// would expect an error to be raised in some fashion here, hence we make the
// tests fail when RUN_JAVAC is on
public void test010_inappropriate_encoding_diagnosis() throws IOException {
	List<String> buggyEncoding = Arrays.asList("dummy");
	boolean passed = true;
	try {
		passed = COMPILER.getStandardFileManager(null /* diagnosticListener */, null /* locale */, null /* charset */).
		handleOption("-encoding", buggyEncoding.iterator());
	} catch (RuntimeException e) {
		passed = false;
	}
	assertFalse("does not catch inappropriate -encoding option", passed);
	if (RUN_JAVAC) {
		passed = true;
		try {
			passed = JAVAC_COMPILER.getStandardFileManager(null, null, null).
				handleOption("-encoding", buggyEncoding.iterator());
		} catch (Throwable t) {
			passed = false;
		}
		assertFalse("does not catch inappropriate -encoding option", passed);
	}
}
public void test011_inappropriate_encoding_diagnosis() {
	List<String> options = Arrays.asList("-d", OUTPUT_DIR, "-encoding", "dummy");
	boolean passed = true;
	try {
	runTest(
		false /* shouldCompileOK */,
		new String [] { /* sourceFiles */
			"X.java",
			"public class X {}",
		}, 
		null /* standardJavaFileManager */,
		options /* options */, 
		new String[] { /* compileFileNames */
			"X.java"
		}, 
		"" /* expectedOutOutputString */, 
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* classFileNames */);
	} catch (RuntimeException e) {
		passed = false;
	}
	assertFalse("does not catch inappropriate -encoding option", passed);
	if (RUN_JAVAC) {
		passed = true;
		try {
			passed = JAVAC_COMPILER.getTask(null, null, null, options, null, 
					JAVAC_COMPILER.getStandardFileManager(null, null, null).getJavaFileObjectsFromFiles(
						Arrays.asList(new File(OUTPUT_DIR + File.separator + "X.java")))).call();
		} catch (Throwable t) {
			passed = false;
		}
		assertFalse("does not catch inappropriate -encoding option", passed);		
	}
}
}
