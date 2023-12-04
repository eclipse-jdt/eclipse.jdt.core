/*******************************************************************************
 * Copyright (c) 2008, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    IBM Corporation - fix for 342936
 *******************************************************************************/
package org.eclipse.jdt.compiler.tool.tests;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.LogRecord;

import javax.lang.model.SourceVersion;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileObject;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;

import junit.framework.Test;

public class CompilerInvocationTests extends AbstractCompilerToolTest {
	static {
//		TESTS_NAMES = new String[] { "test019_sourcepath_without_destination" };
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
private boolean isOnJRE9() {
	try {
		SourceVersion.valueOf("RELEASE_9");
	} catch(IllegalArgumentException iae) {
		return false;
	}
	return true;
}
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
	List<String> opt = options == null ? new ArrayList<>() : new ArrayList<>(options);
	opt.add("-source");
	opt.add("1.6");
	super.runTest(
		shouldCompileOK,
		sourceFiles,
		new CompilerInvocationTestsArguments(standardJavaFileManager, opt, compileFileNames),
		expectedOutOutputString,
		expectedErrOutputString,
		shouldFlushOutputDirectory,
		null /* progress */);
	// TODO maxime introduce stderr comparison based upon specific diagnostic listener
	if (classFileNames != null) {
		checkClassFiles(classFileNames);
	}
}
class GetLocationDetector extends ForwardingStandardJavaFileManager<StandardJavaFileManager>  {
	private final Location match;
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
abstract class GetJavaFileDetector extends ForwardingStandardJavaFileManager<StandardJavaFileManager>  {
	boolean matchFound;
	String discriminatingSuffix;
	GetJavaFileDetector(StandardJavaFileManager javaFileManager) {
		super(javaFileManager);
	}
	GetJavaFileDetector(StandardJavaFileManager javaFileManager,
			String discriminatingSuffix) {
		super(javaFileManager);
		this.discriminatingSuffix = discriminatingSuffix;
	}
	abstract JavaFileObject detector(JavaFileObject original);
	@Override
	public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
		return getJavaFileObjectsFromFiles(Arrays.asList(files));
	}
	@Override
	public Iterable<? extends JavaFileObject> getJavaFileObjects(
			String... names) {
		return getJavaFileObjectsFromStrings(Arrays.asList(names));
	}
	@Override
	public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(
			Iterable<? extends File> files) {
		ArrayList<JavaFileObject> result = new ArrayList<>();
		for (JavaFileObject file: super.getJavaFileObjectsFromFiles(files)) {
			result.add(detector(file));
		}
		return result;
	}
	@Override
	public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(
			Iterable<String> names) {
		ArrayList<JavaFileObject> result = new ArrayList<>();
		for (JavaFileObject file: getJavaFileObjectsFromStrings(names)) {
			result.add(detector(file));
		}
		return result;
	}
	@Override
	public Iterable<JavaFileObject> list(Location location, String packageName,
			Set<Kind> kinds, boolean recurse) throws IOException {
		ArrayList<JavaFileObject> result = new ArrayList<>();
		for (JavaFileObject file: super.list(location, packageName, kinds, recurse)) {
			result.add(detector(file));
		}
		return result;
	}
}
class GetJavaFileForInputDetector extends GetJavaFileDetector  {
	private final Kind discriminatingKind;
	GetJavaFileForInputDetector(StandardJavaFileManager javaFileManager) {
		super(javaFileManager);
		this.discriminatingKind = Kind.SOURCE;
	}
	GetJavaFileForInputDetector(StandardJavaFileManager javaFileManager,
			String discriminatingSuffix,
			Kind discriminatingKind) {
		super(javaFileManager, discriminatingSuffix);
		this.discriminatingKind = discriminatingKind;
	}
	class JavaFileInputDetector extends ForwardingJavaFileObject<JavaFileObject> {
		JavaFileInputDetector(JavaFileObject fileObject) {
			super(fileObject);
		}
		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors)
				throws IOException {
			matchFound = true;
			return super.getCharContent(ignoreEncodingErrors);
		}
		@Override
		public InputStream openInputStream() throws IOException {
			matchFound = true;
			return super.openInputStream();
		}
		@Override
		public Reader openReader(boolean ignoreEncodingErrors)
				throws IOException {
			matchFound = true;
			return super.openReader(ignoreEncodingErrors);
		}
	}
	@Override
	JavaFileObject detector(JavaFileObject original) {
		if (original != null && original.getKind() == this.discriminatingKind
				&& (this.discriminatingSuffix == null || original.getName().endsWith(this.discriminatingSuffix))) {
			return new JavaFileInputDetector(original);
		}
		return original;
	}
	@Override
	public FileObject getFileForInput(Location location, String packageName,
			String relativeName) throws IOException {
		FileObject result =
			super.getFileForInput(location, packageName, relativeName);
		if (result instanceof JavaFileObject) {
			return detector((JavaFileObject) result);
		}
		return result;
	}
	@Override
	public JavaFileObject getJavaFileForInput(Location location,
			String className, Kind kind) throws IOException {
		return detector(super.getJavaFileForInput(location, className, kind));
	}
}
class GetJavaFileForOutputDetector extends GetJavaFileDetector  {
	GetJavaFileForOutputDetector(StandardJavaFileManager javaFileManager) {
		super(javaFileManager);
	}
	GetJavaFileForOutputDetector(StandardJavaFileManager javaFileManager,
			String discriminatingSuffix) {
		super(javaFileManager, discriminatingSuffix);
	}
	class JavaFileOutputDetector extends ForwardingJavaFileObject<JavaFileObject> {
		JavaFileOutputDetector(JavaFileObject fileObject) {
			super(fileObject);
		}
		@Override
		public OutputStream openOutputStream() throws IOException {
			matchFound = true;
			return super.openOutputStream();
		}
		@Override
		public Writer openWriter() throws IOException {
			matchFound = true;
			return super.openWriter();
		}
	}
	@Override
	JavaFileObject detector(JavaFileObject original) {
		if (original != null && original.getKind() == Kind.CLASS
				&& (this.discriminatingSuffix == null || original.getName().endsWith(this.discriminatingSuffix))) {
			return new JavaFileOutputDetector(original);
		}
		return original;
	}
	@Override
	public FileObject getFileForOutput(Location location, String packageName,
			String relativeName, FileObject sibling) throws IOException {
		FileObject result =
			super.getFileForOutput(location, packageName, relativeName, sibling);
		if (result instanceof JavaFileObject) {
			return detector((JavaFileObject) result);
		}
		return result;
	}
	@Override
	public JavaFileObject getJavaFileForOutput(Location location,
			String className, Kind kind, FileObject sibling) throws IOException {
		return detector(super.getJavaFileForOutput(location, className, kind, sibling));
	}
}
class SetLocationDetector extends ForwardingStandardJavaFileManager<StandardJavaFileManager>  {
	private final Location match;
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
	private final String match;
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
protected void compareFileLocations(String[] expected, Iterator<? extends File> actual) {
	int i = 0;
	while(actual.hasNext() && i < expected.length) {
		assertEquals("Path mismatch", expected[i], actual.next().toString());
		i++;
	}
	assertEquals("Incorret no of files in path", i, expected.length);
	assertFalse("Incorrect no of files in path", actual.hasNext());
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
	if (JAVAC_COMPILER == null) {
		System.out.println("No system java compiler available");
		return;
	}
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
	compareFileLocations(new String[]{OUTPUT_DIR}, javacStandardJavaFileManager.getLocation(StandardLocation.CLASS_OUTPUT).iterator());
}
// exploring -d / FileManager interaction
// -d changes CLASS_OUTPUT location (OUTPUT_DIR subdirectory)
public void test003_dash_d_option() {
	if (JAVAC_COMPILER == null) {
		System.out.println("No system java compiler available");
		return;
	}
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
	compareFileLocations(new String[]{outputDir}, javacStandardJavaFileManager.getLocation(StandardLocation.CLASS_OUTPUT).iterator());
}
// exploring -d / FileManager interaction
// ecj uses the output location from the javac standard Java file manager if it
// is set
public void test004_no_dash_d_option() throws IOException {
	if (JAVAC_COMPILER == null) {
		System.out.println("No system java compiler available");
		return;
	}
	File binDirectory = new File(OUTPUT_DIR + File.separator + "bin");
	binDirectory.mkdirs();
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
	if (JAVAC_COMPILER == null) {
		System.out.println("No system java compiler available");
		return;
	}
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
	compareFileLocations(new String[]{OUTPUT_DIR}, customJavaFileManager.getLocation(StandardLocation.CLASS_OUTPUT).iterator());
	assertFalse(customJavaFileManager.matchFound());
	if (RUN_JAVAC && JAVAC_COMPILER != null) {
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
	if (JAVAC_COMPILER == null) {
		System.out.println("No system java compiler available");
		return;
	}
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
	if (RUN_JAVAC && JAVAC_COMPILER != null) {
		StandardJavaFileManager javacStandardJavaFileManager =
			JAVAC_COMPILER.getStandardFileManager(null, null, null); // will pick defaults up
		remaining = remainingAsList.iterator();
		assertTrue("does not support -d option", javacStandardJavaFileManager.handleOption("-d", remaining));
		assertEquals("unexpected consumption rate", "remainder", remaining.next());
		javacStandardJavaFileManager.close();
	}
	ecjStandardJavaFileManager.close();
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
	ecjStandardJavaFileManager.close();
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=226918
// options consumption - check consumption rate on supported one-arg options
public void test009_options_consumption() throws IOException {
	final String REMAINDER = "remainder";
	List<String> remainingAsList = Arrays.asList("utf-8", REMAINDER);
	StandardJavaFileManager ecjStandardJavaFileManager =
		COMPILER.getStandardFileManager(null /* diagnosticListener */, null /* locale */, null /* charset */);
	for (String option: CompilerToolTests.ONE_ARG_OPTIONS) {
		if (isOnJRE9() && (option.equals("-extdirs") || option.equals("-endorseddirs")))
				continue;
		if (ecjStandardJavaFileManager.isSupportedOption(option) != -1) { // some options that the compiler support could well not be supported by the file manager
			Iterator<String> remaining = remainingAsList.iterator();
			assertTrue("does not support " + option + " option", ecjStandardJavaFileManager.handleOption(option, remaining));
			assertEquals("unexpected consumption rate", REMAINDER, remaining.next());
		}
	}
	ecjStandardJavaFileManager.close();
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
	if (RUN_JAVAC && JAVAC_COMPILER != null) {
		// this fails, which may be deemed appropriate or not; but at least
		// test #11 shows that the behavior that can be observed from the
		// outside is inappropriate
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
	if (RUN_JAVAC && JAVAC_COMPILER != null) {
		// compared to what the command-line javac does, this is due to be a
		// bug
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=188796
// files access must happen through the user-specified file manager
// simplest source read case
public void test012_files_access_read() throws IOException {
	if (JAVAC_COMPILER == null) {
		System.out.println("No system java compiler available");
		return;
	}
	GetJavaFileForInputDetector customJavaFileManager =
		new GetJavaFileForInputDetector(
				JAVAC_COMPILER.getStandardFileManager(null /* diagnosticListener */, null /* locale */, null /* charset */));
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
	assertTrue(customJavaFileManager.matchFound);
	if (RUN_JAVAC && JAVAC_COMPILER != null) {
		customJavaFileManager.matchFound = false;
		assertTrue(JAVAC_COMPILER.getTask(null, customJavaFileManager, null,
				Arrays.asList("-d", OUTPUT_DIR), null,
				customJavaFileManager.getJavaFileObjectsFromFiles(
						Arrays.asList(new File(OUTPUT_DIR + File.separator + "X.java")))).call());
		assertTrue(customJavaFileManager.matchFound);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=188796
// files access must happen through the user-specified file manager
// source file accessed through the sourcepath
public void _test013_files_access_read() throws IOException {
	if (JAVAC_COMPILER == null) {
		System.out.println("No system java compiler available");
		return;
	}
	GetJavaFileForInputDetector customJavaFileManager =
		new GetJavaFileForInputDetector(
				COMPILER.getStandardFileManager(null /* diagnosticListener */, null /* locale */, null /* charset */),
				"Y.java", Kind.SOURCE);
	List<String> options = Arrays.asList(
			"-d", OUTPUT_DIR,
			"-sourcepath", OUTPUT_DIR + File.separator + "src2");
	runTest(
		true /* shouldCompileOK */,
		new String [] { /* sourceFiles */
			"src1/X.java",
			"public class X {\n" +
			"  Y y;\n" +
			"}",
			"src2/Y.java",
			"public class Y {}",
		},
		customJavaFileManager /* standardJavaFileManager */,
		options /* options */,
		new String[] { /* compileFileNames */
			"src1/X.java"
		},
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		new String[] { /* classFileNames */
			"X.class"
		});
	assertTrue(customJavaFileManager.matchFound);
	if (RUN_JAVAC && JAVAC_COMPILER != null) {
		customJavaFileManager.matchFound = false;
		assertTrue(JAVAC_COMPILER.getTask(null, customJavaFileManager, null,
				options, null,
				customJavaFileManager.getJavaFileObjectsFromFiles(
						Arrays.asList(new File(OUTPUT_DIR + File.separator + "src1/X.java")))).call());
		assertTrue(customJavaFileManager.matchFound);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=188796
// files access must happen through the user-specified file manager
// class file accessed for read through the classpath
public void _test014_files_access_read() throws IOException {
	GetJavaFileForInputDetector customJavaFileManager =
		new GetJavaFileForInputDetector(
				COMPILER.getStandardFileManager(null /* diagnosticListener */, null /* locale */, null /* charset */),
				"Y.class", Kind.CLASS);
	List<String> options = Arrays.asList(
			"-d", OUTPUT_DIR,
			"-classpath", OUTPUT_DIR);
	runTest(
			true /* shouldCompileOK */,
			new String [] { /* sourceFiles */
				"src2/Y.java",
				"public class Y {}",
			},
			customJavaFileManager /* standardJavaFileManager */,
			options /* options */,
			new String[] { /* compileFileNames */
				"src2/Y.java"
			},
			"" /* expectedOutOutputString */,
			"" /* expectedErrOutputString */,
			true /* shouldFlushOutputDirectory */,
			new String[] { /* classFileNames */
				"Y.class"
			});
	runTest(
		true /* shouldCompileOK */,
		new String [] { /* sourceFiles */
			"src1/X.java",
			"public class X {\n" +
			"  Y y;\n" +
			"}",
		},
		customJavaFileManager /* standardJavaFileManager */,
		options /* options */,
		new String[] { /* compileFileNames */
			"src1/X.java"
		},
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		false /* shouldFlushOutputDirectory */,
		new String[] { /* classFileNames */
			"X.class"
		});
	assertTrue(customJavaFileManager.matchFound);
	if (RUN_JAVAC && JAVAC_COMPILER != null) {
		// javac merely throws an exception, which is due to be a bug on their
		// side
		customJavaFileManager.matchFound = false;
		assertTrue(JAVAC_COMPILER.getTask(null, customJavaFileManager, null,
				options, null,
				customJavaFileManager.getJavaFileObjectsFromFiles(
						Arrays.asList(new File(OUTPUT_DIR + File.separator + "src1/X.java")))).call());
		assertTrue(customJavaFileManager.matchFound);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=188796
// files access must happen through the user-specified file manager
// class file accessed for write
public void test015_files_access_write() throws IOException {
	GetJavaFileForOutputDetector customJavaFileManager =
		new GetJavaFileForOutputDetector(
				COMPILER.getStandardFileManager(null /* diagnosticListener */, null /* locale */, null /* charset */),
				"X.class");
	List<String> options = Arrays.asList("-d", OUTPUT_DIR);
	runTest(
		true /* shouldCompileOK */,
		new String [] { /* sourceFiles */
			"src/X.java",
			"public class X {\n" +
			"}",
		},
		customJavaFileManager /* standardJavaFileManager */,
		options /* options */,
		new String[] { /* compileFileNames */
			"src/X.java"
		},
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		new String[] { /* classFileNames */
			"X.class"
		});
	assertTrue(customJavaFileManager.matchFound);
	if (RUN_JAVAC && JAVAC_COMPILER != null) {
		customJavaFileManager.matchFound = false;
		assertTrue(JAVAC_COMPILER.getTask(null, customJavaFileManager, null,
				options, null,
				customJavaFileManager.getJavaFileObjectsFromFiles(
						Arrays.asList(new File(OUTPUT_DIR + File.separator + "src/X.java")))).call());
		assertTrue(customJavaFileManager.matchFound);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=188796
// files access must happen through the user-specified file manager
// class file accessed for write
public void test016_files_access_write() throws IOException {
	GetJavaFileForOutputDetector customJavaFileManager =
		new GetJavaFileForOutputDetector(
				COMPILER.getStandardFileManager(null /* diagnosticListener */, null /* locale */, null /* charset */),
				"Y.class");
	List<String> options = Arrays.asList(
			"-sourcepath", OUTPUT_DIR + File.separator + "src2");
	runTest(
		true /* shouldCompileOK */,
		new String [] { /* sourceFiles */
			"src/X.java",
			"public class X {\n" +
			"  Y y;\n" +
			"}",
			"src2/Y.java",
			"public class Y {\n" +
			"}",
		},
		customJavaFileManager /* standardJavaFileManager */,
		options /* options */,
		new String[] { /* compileFileNames */
			"src/X.java"
		},
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		new String[] { /* classFileNames */
			"src/X.class"
		});
	assertTrue(customJavaFileManager.matchFound);
	if (RUN_JAVAC && JAVAC_COMPILER != null) {
		customJavaFileManager.matchFound = false;
		assertTrue(JAVAC_COMPILER.getTask(null, customJavaFileManager, null,
				options, null,
				customJavaFileManager.getJavaFileObjectsFromFiles(
						Arrays.asList(new File(OUTPUT_DIR + File.separator + "src/X.java")))).call());
		assertTrue(customJavaFileManager.matchFound);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=227583
public void test017_sourcepath_without_destination() throws IOException {
	runTest(
		true /* shouldCompileOK */,
		new String [] { /* sourceFiles */
			"src1/X.java",
			"public class X {\n" +
			"  Y y;\n" +
			"}",
			"src2/Y.java",
			"public class Y {}",
		},
		null /* standardJavaFileManager */,
		Arrays.asList(
				"-d", OUTPUT_DIR + "/bin1", /* options */
				"-sourcepath", OUTPUT_DIR + "/src2"),
		new String[] { /* compileFileNames */
			"src1/X.java"
		},
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		new String[] { /* classFileNames */
			"bin1/X.class",
			"bin1/Y.class"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=227583
// see BatchCompilerTest#68 and following, that show how the option works
// with jsr199-less ecj
public void _test018_sourcepath_with_destination() throws IOException {
	runTest(
		true /* shouldCompileOK */,
		new String [] { /* sourceFiles */
			"src1/X.java",
			"public class X {\n" +
			"  Y y;\n" +
			"}",
			"src2/Y.java",
			"public class Y {}",
		},
		null /* standardJavaFileManager */,
		Arrays.asList(
				"-d", OUTPUT_DIR + "/bin1", /* options */
				"-sourcepath", "\"" + OUTPUT_DIR + "/src2\"[-d \"" + OUTPUT_DIR + "/bin2\"]"),
		new String[] { /* compileFileNames */
			"src1/X.java"
		},
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		new String[] { /* classFileNames */
			"bin1/X.class",
			"bin2/Y.class"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=227583
public void test019_sourcepath_without_destination() throws IOException {
	String sourceDirectoryName = OUTPUT_DIR + "/src2";
	File sourceFolder = new File(sourceDirectoryName);
	if (!sourceFolder.exists()) {
		if (!sourceFolder.mkdirs()) {
			// source folder could not be built
			return;
		}
	}
	StandardJavaFileManager ecjStandardJavaFileManager =
		COMPILER.getStandardFileManager(null /* diagnosticListener */, null /* locale */, null /* charset */);
	assertTrue(ecjStandardJavaFileManager.handleOption(
			"-sourcepath",
			Arrays.asList(OUTPUT_DIR + "/src2").iterator()));
	runTest(
		true /* shouldCompileOK */,
		new String [] { /* sourceFiles */
			"src1/X.java",
			"public class X {\n" +
			"  Y y;\n" +
			"}",
			"src2/Y.java",
			"public class Y {}",
		},
		ecjStandardJavaFileManager /* standardJavaFileManager */,
		Arrays.asList("-d", OUTPUT_DIR + "/bin1") /* options */,
		new String[] { /* compileFileNames */
			"src1/X.java"
		},
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		new String[] { /* classFileNames */
			"bin1/X.class",
			"bin1/Y.class"
		});
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=227583
public void _test020_sourcepath_with_destination() throws IOException {
	StandardJavaFileManager ecjStandardJavaFileManager =
		COMPILER.getStandardFileManager(null /* diagnosticListener */, null /* locale */, null /* charset */);
	assertTrue(ecjStandardJavaFileManager.handleOption(
			"-sourcepath",
			Arrays.asList("\"" + OUTPUT_DIR + "/src2\"[-d \"" + OUTPUT_DIR + "/bin2\"]").iterator()));
	runTest(
		true /* shouldCompileOK */,
		new String [] { /* sourceFiles */
			"src1/X.java",
			"public class X {\n" +
			"  Y y;\n" +
			"}",
			"src2/Y.java",
			"public class Y {}",
		},
		ecjStandardJavaFileManager /* standardJavaFileManager */,
		Arrays.asList("-d", OUTPUT_DIR + "/bin1") /* options */,
		new String[] { /* compileFileNames */
			"src1/X.java"
		},
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		new String[] { /* classFileNames */
			"bin1/X.class",
			"bin2/Y.class"
		});
}
// most basic output test
public void test021_output_streams() throws IOException {
	ByteArrayOutputStream
			outBuffer = new ByteArrayOutputStream(),
			errBuffer = new ByteArrayOutputStream();
	CompilationTask task = COMPILER.getTask(
		new PrintWriter(outBuffer),
		COMPILER.getStandardFileManager(null /* diagnosticListener */, null /* locale */, null /* charset */),
		new CompilerInvocationDiagnosticListener(new PrintWriter(errBuffer)),
		Arrays.asList("-v"), null, null);
	assertTrue(task.call());
	Properties properties = new Properties();
	try (InputStream resourceAsStream = Main.class.getResourceAsStream("messages.properties")) {
		properties.load(resourceAsStream);
	}
	assertTrue(outBuffer.toString().startsWith(properties.getProperty("compiler.name")));
	assertTrue(errBuffer.toString().isEmpty());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=236814
public void test022_output_streams() throws IOException {
	ByteArrayOutputStream
	outBuffer = new ByteArrayOutputStream(),
	errBuffer = new ByteArrayOutputStream();
	PrintStream
	systemOut = System.out,
	systemErr = System.err;
	System.setOut(new PrintStream(outBuffer));
	System.setErr(new PrintStream(errBuffer));
	CompilationTask task = COMPILER.getTask(
			null,
			COMPILER.getStandardFileManager(null /* diagnosticListener */, null /* locale */, null /* charset */),
			new CompilerInvocationDiagnosticListener(new PrintWriter(errBuffer)),
			Arrays.asList("-v"), null, null);
	try {
		assertTrue(task.call());
		assertTrue(outBuffer.toString().isEmpty());
		assertTrue(errBuffer.toString().startsWith("Eclipse Compiler for Java"));
	} finally {
		System.setOut(systemOut);
		System.setErr(systemErr);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=236817
// according to JavaCompiler#getTask, out should receive supplementary compiler
// output only; errors should be funneled through the diagnostic listener
public void _test023_output_streams() throws IOException {
	runTest(
		false /* shouldCompileOK */,
		new String [] { /* sourceFiles */
			"X.java",
			"public class Y {}",
		},
		null /* standardJavaFileManager */,
		Arrays.asList("-d", OUTPUT_DIR) /* options */,
		new String[] { /* compileFileNames */
			"X.java"
		},
		"" /* expectedOutOutputString */,
		"----------\n" + /* expectedErrOutputString */
		"1. ERROR in X.java (at line 1)\n" +
		"	public class Y {}\n" +
		"	             ^\n" +
		"The public type Y must be defined in its own file\n" +
		"----------\n" +
		"1 problem (1 error)",
		true /* shouldFlushOutputDirectory */,
		null /* classFileNames */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577550
// check that an empty iterable for the 'classes' argument doesn't result in an exception
public void test024_bug577550_test_empty_classes_argument() {
	// create a source file to compile
	StandardJavaFileManager javacJavaFileManager = JAVAC_COMPILER.getStandardFileManager(null, null, null);
	runTest(
			true /* shouldCompileOK */,
			new String [] { /* sourceFiles */
				"X.java",
				"public class X {}",
			},
			javacJavaFileManager /* standardJavaFileManager */,
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
	
	// Bug 577550: supply intentionally empty (and not null) classes iterable
	Iterable<String> classes = new ArrayList<>();
	StandardJavaFileManager ecjStandardJavaFileManager = COMPILER.getStandardFileManager(null /* diagnosticListener */,
			null /* locale */, null /* charset */);
	assertTrue("Expected compile with empty 'classes' argument to succeed", COMPILER.getTask(null,
			ecjStandardJavaFileManager, null, Arrays.asList("-d", OUTPUT_DIR), classes,
			ecjStandardJavaFileManager
					.getJavaFileObjectsFromFiles(Arrays.asList(new File(OUTPUT_DIR + File.separator + "X.java"))))
			.call());
}
}
