/*******************************************************************************
 * Copyright (c) 2016, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IModuleAttribute;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.AttributeNamesConstants;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

public class ModuleCompilationTests extends AbstractBatchCompilerTest {

	static {
//		 TESTS_NAMES = new String[] { "testRelease565930" };
		// TESTS_NUMBERS = new int[] { 1 };
		// TESTS_RANGE = new int[] { 298, -1 };
	}

	// use -source rather than --release but suppress: warning: [options] bootstrap class path not set in conjunction with -source 9:
	private static final String JAVAC_SOURCE_9_OPTIONS = "-source 9 -Xlint:-options";

	public ModuleCompilationTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
	}

	public static Class<?> testClass() {
		return ModuleCompilationTests.class;
	}

	protected void writeFileCollecting(List<String> collectedFiles, String directoryName, String fileName, String source) {
		writeFile(directoryName, fileName, source);
		collectedFiles.add(directoryName+File.separator+fileName);
	}

	protected void writeFile(String directoryName, String fileName, String source) {
		File directory = new File(directoryName);
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				System.out.println("Could not create " + directoryName);
				return;
			}
		}
		String filePath = directory.getAbsolutePath() + File.separator + fileName;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
			writer.write(source);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	}

	class Runner extends AbstractRegressionTest.Runner {
		StringBuilder commandLine = new StringBuilder();
		String outputDir = OUTPUT_DIR + File.separator + "javac";
		List<String> fileNames = new ArrayList<>();
		/** will replace any -8, -9 ... option for javac */
		String javacVersionOptions;

		Runner() {
			this.javacTestOptions = JavacTestOptions.DEFAULT;
			this.expectedOutputString = "";
			this.expectedErrorString = "";
		}
		/** Create a source file and add the filename to the compiler command line. */
		void createFile(String directoryName, String fileName, String source) {
			writeFileCollecting(this.fileNames, directoryName, fileName, source);
		}
		Set<String> runConformModuleTest() {
			if (!this.fileNames.isEmpty()) {
				this.shouldFlushOutputDirectory = false;
				if (this.testFiles == null)
					this.testFiles = new String[0];
				for (String fileName : this.fileNames) {
					this.commandLine.append(" \"").append(fileName).append("\"");
				}
			}
			String commandLineString = this.commandLine.toString();
			String javacCommandLine = adjustForJavac(commandLineString, this.javacVersionOptions);
			return ModuleCompilationTests.this.runConformModuleTest(this.testFiles, commandLineString,
					this.expectedOutputString, this.expectedErrorString,
					this.shouldFlushOutputDirectory, this.outputDir,
					this.javacTestOptions, javacCommandLine);
		}
	}

	void runConformModuleTest(List<String> testFileNames, StringBuilder commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory)
	{
		runConformModuleTest(testFileNames, commandLine,
				expectedFailureOutOutputString, expectedFailureErrOutputString, shouldFlushOutputDirectory, OUTPUT_DIR + File.separator + "javac");
	}

	void runConformModuleTest(List<String> testFileNames, StringBuilder commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String output)
	{
		for (String file : testFileNames)
			commandLine.append(" \"").append(file).append("\"");
		runConformModuleTest(new String[0], commandLine.toString(),
				expectedFailureOutOutputString, expectedFailureErrOutputString, shouldFlushOutputDirectory,
				output, JavacTestOptions.DEFAULT, null);
	}

	Set<String> runConformModuleTest(String[] testFiles, String commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory)
	{
		return runConformModuleTest(testFiles, commandLine, expectedFailureErrOutputString, expectedFailureErrOutputString,
				shouldFlushOutputDirectory, OUTPUT_DIR, JavacTestOptions.DEFAULT, null);
	}

	Set<String> runConformModuleTest(String[] testFiles, String commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String output, JavacTestOptions options, String javacCommandLine)
	{
		this.runConformTest(testFiles, commandLine, expectedFailureOutOutputString, expectedFailureErrOutputString, shouldFlushOutputDirectory);
		if (RUN_JAVAC) {
			File outputDir = new File(output);
			final Set<String> outFiles = new HashSet<>();
			walkOutFiles(output, outFiles, true);
			String[] testFileNames = new String[testFiles.length/2];
			for (int i = 0; i < testFileNames.length; i++) {
				testFileNames[i] = testFiles[i*2];
			}
			if (javacCommandLine == null) {
				javacCommandLine = adjustForJavac(commandLine, null);
			}
			for (JavacCompiler javacCompiler : javacCompilers) {
				if (javacCompiler.compliance < ClassFileConstants.JDK9)
					continue;
				if (options.skip(javacCompiler)) {
					System.err.println("Skip testing javac in "+testName());
					continue;
				}
				StringBuilder log = new StringBuilder();
				try {
					long compileResult = javacCompiler.compile(
											outputDir, /* directory */
											javacCommandLine /* options */,
											testFileNames /* source file names */,
											log,
											false); // don't repeat filenames on the command line
					if (compileResult != 0) {
						System.err.println("Previous error was from "+testName());
						fail("Unexpected error from javac");
					}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
					throw new AssertionFailedError(e.getMessage());
				}
				final Set<String> expectedFiles = new HashSet<>(outFiles);
				walkOutFiles(output, expectedFiles, false);
				for (String missingFile : expectedFiles)
					System.err.println("Missing output file from javac:    "+missingFile);
			}
			return outFiles;
		}
		return null;
	}

	void runNegativeModuleTest(List<String> testFileNames, StringBuilder commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String javacErrorMatch) {
		runNegativeModuleTest(testFileNames, commandLine, expectedFailureOutOutputString,
				expectedFailureErrOutputString, shouldFlushOutputDirectory, javacErrorMatch, OUTPUT_DIR + File.separator + "javac");
	}

	void runNegativeModuleTest(List<String> testFileNames, StringBuilder commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String javacErrorMatch, String output)
	{
		runNegativeModuleTest(testFileNames, commandLine, expectedFailureOutOutputString, expectedFailureErrOutputString,
				shouldFlushOutputDirectory, javacErrorMatch, output, JavacTestOptions.DEFAULT);
	}
	void runNegativeModuleTest(List<String> testFileNames, StringBuilder commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String javacErrorMatch, String output, JavacTestOptions options)
	{
		for (String file : testFileNames)
			commandLine.append(" \"").append(file).append("\"");
		runNegativeModuleTest(new String[0], commandLine.toString(),
				expectedFailureOutOutputString, expectedFailureErrOutputString, shouldFlushOutputDirectory, javacErrorMatch, output,
				options);
	}
	void runNegativeModuleTest(String[] testFiles, String commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String javacErrorMatch) {
		runNegativeModuleTest(testFiles, commandLine, expectedFailureOutOutputString, expectedFailureErrOutputString,
				shouldFlushOutputDirectory, javacErrorMatch, OUTPUT_DIR, JavacTestOptions.DEFAULT);
	}

	void runNegativeModuleTest(String[] testFiles, String commandLine,
			String expectedFailureOutOutputString, String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory, String javacErrorMatch, String output, JavacTestOptions options)
	{
		this.runNegativeTest(testFiles, commandLine, expectedFailureOutOutputString, expectedFailureErrOutputString, shouldFlushOutputDirectory);
		if (RUN_JAVAC) {
			String[] testFileNames = new String[testFiles.length/2];
			for (int i = 0; i < testFileNames.length; i++) {
				testFileNames[i] = testFiles[i*2];
			}
			File outputDir = new File(OUTPUT_DIR);
			final Set<String> outFiles = new HashSet<>();
			walkOutFiles(output, outFiles, true);
			for (JavacCompiler javacCompiler : javacCompilers) {
				if (javacCompiler.compliance < ClassFileConstants.JDK9)
					continue;
				JavacTestOptions.Excuse excuse = options.excuseFor(javacCompiler);

				commandLine = adjustForJavac(commandLine, null);
				StringBuilder log = new StringBuilder();
				int mismatch = 0;
				try {
					long compileResult = javacCompiler.compile(
											outputDir, /* directory */
											commandLine /* options */,
											testFileNames /* source file names */,
											log);
					if (compileResult == 0) {
						mismatch = JavacTestOptions.MismatchType.EclipseErrorsJavacNone;
						javacErrorMatch = expectedFailureErrOutputString;
						System.err.println("Previous error was from "+testName());
					} else if (!log.toString().contains(javacErrorMatch)) {
						mismatch = JavacTestOptions.MismatchType.CompileErrorMismatch;
						System.err.println(testName()+": Error match " + javacErrorMatch + " not found in \n"+log.toString());
					}
				} catch (IOException | InterruptedException e) {
					e.printStackTrace();
					throw new AssertionFailedError(e.getMessage());
				}
				handleMismatch(javacCompiler, testName(), testFiles, javacErrorMatch,
						"", "", log, "", "",
						excuse, mismatch);
				final Set<String> expectedFiles = new HashSet<>(outFiles);
				walkOutFiles(output, expectedFiles, false);
				for (String missingFile : expectedFiles)
					System.err.println("Missing output file from javac:    "+missingFile);
			}
		}
	}

	/**
	 * @param commandLine command line arguments as used for ecj
	 * @param versionOptions if non-null use this to replace any ecj-specific -8, -9 etc. arg.
	 * 		If ecj-specific arg is not found, append anyway
	 * @return commandLine adjusted for javac
	 */
	String adjustForJavac(String commandLine, String versionOptions) {
		String[] tokens = commandLine.split(" ");
		StringBuilder buf = new StringBuilder();
		boolean skipNext = false;
		for (int i = 0; i < tokens.length; i++) {
			if (skipNext) {
				skipNext = false;
				continue;
			}
			if (tokens[i].trim().equals("-9")) {
				if (versionOptions == null)
					buf.append(' ').append(" --release 9 ");
				continue;
			}
			if (tokens[i].trim().equals("-8")) {
				if (versionOptions == null)
					buf.append(' ').append(" --release 8 ");
				continue;
			}
			if (tokens[i].startsWith("-warn") || tokens[i].startsWith("-err") || tokens[i].startsWith("-info")) {
				if (tokens[i].contains("exports") && !tokens[i].contains("-exports"))
					buf.append(" -Xlint:exports ");
				continue;
			}
			if (tokens[i].trim().equals("-classNames")) {
				skipNext = true;
				continue;
			}
			buf.append(tokens[i]).append(' ');
		}
		if (versionOptions != null) {
			buf.append(versionOptions);
		}
		return buf.toString();
	}

	private void walkOutFiles(final String outputLocation, final Set<String> fileNames, boolean add) {
		if (!(new File(outputLocation)).exists())
			return;
		try {
			Files.walkFileTree(Path.of(outputLocation), new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					if (file.toString().endsWith(".class")) {
						if (add) {
							fileNames.add(file.toString());
						} else {
							if (!fileNames.remove(file.toString()))
								System.err.println("Unexpected output file from javac: "+file.toString());
						}
						Files.delete(file);
					}
					return FileVisitResult.CONTINUE;
				}
				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					if (!dir.toString().equals(outputLocation)) {
						try {
							Files.delete(dir);
						} catch (DirectoryNotEmptyException ex) {
							// expected
						}
					}
			        return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
			throw new AssertionFailedError(e.getMessage());
		}
	}

	private void assertClassFile(String msg, String fileName, Set<String> classFiles) {
		if (classFiles != null) {
			assertTrue(msg, classFiles.contains(fileName));
		} else {
			assertTrue(msg, (new File(fileName).exists()));
		}
	}

	public void test001() {
		runNegativeModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					public class X {
						public static void main(String[] args) {
					     java.sql.Connection con = null;
						}
					}""",
				"module-info.java",
				"""
					module mod.one {\s
						requires java.base;
					}"""
	        },
			" -9 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        """
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 4)
					java.sql.Connection con = null;
					^^^^^^^^^^^^^^^^^^^
				The type java.sql.Connection is not accessible
				----------
				1 problem (1 error)
				""",
	        true,
	        "package java.sql" /* match for javac error */);
	}
	public void test002() {
		runConformModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					public class X {
						public static void main(String[] args) {
					     java.sql.Connection con = null;
					     System.out.println(con);
						}
					}""",
				"module-info.java",
				"""
					module mod.one {\s
						requires java.base;
						requires java.sql;
					}"""
	        },
			" -9 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
	}
	public void test003() {
		runConformModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					public class X {
						public static void main(String[] args) {
					     java.sql.Connection con = null;
					     System.out.println(con);
						}
					}""",
	        },
	        "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
	}
	public void test004() {
		Set<String> classFiles = runConformModuleTest(
			new String[] {
				"module-info.java",
				"""
					module mod.one {\s
						requires java.base;
						requires java.sql;
					}"""
	        },
			" -9 \"" + OUTPUT_DIR +  File.separator + "module-info.java\"",
	        "",
	        "",
	        true);
		String fileName = OUTPUT_DIR + File.separator + "module-info.class";
		assertClassFile("Missing modul-info.class: " + fileName, fileName, classFiles);
	}
	public void test005() {
		Set<String> classFiles = runConformModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					public class X {
						java.sql.Connection con;
					}""",
				"module-info.java",
				"""
					module mod.one {\s
						requires java.base;
						requires java.sql;
						requires java.desktop;
					}""",
				"q/Y.java",
				"""
					package q;
					public class Y {
					   java.awt.Image image;
					}"""
	        },
			" -9 \"" + OUTPUT_DIR + File.separator + "module-info.java\" "
			+ "\"" + OUTPUT_DIR + File.separator + "q/Y.java\" "
	        + "\"" + OUTPUT_DIR + File.separator + "p/X.java\" "
	        + "-d " + OUTPUT_DIR ,
	        "",
	        "",
	        true);
		String fileName = OUTPUT_DIR  + File.separator + "module-info.class";
		assertClassFile("Missing modul-info.class: " + fileName, fileName, classFiles);
	}
	public void test006() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires java.sql;
								requires java.desktop;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								java.sql.Connection con;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"""
							package q;
							public class Y {
							   java.awt.Image image;
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runConformModuleTest(files, buffer, "", "", false);
	}
	public void test007() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires transitive java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								public static java.sql.Connection getConnection() {
									return null;
								}
							}""");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								requires java.base;
								requires mod.one;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"""
							package q;
							public class Y {
							   java.sql.Connection con = p.X.getConnection();
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 3)
						java.sql.Connection con = p.X.getConnection();
						                          ^^^
					The type p.X is not accessible
					----------
					1 problem (1 error)
					""",
				false,
				"p.X");
	}
	public void test008() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports p;
								requires mod.two;
								requires transitive java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							import q.Y;
							public class X {
								public static java.sql.Connection getConnection() {
									return Y.con;
								}
							}""");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								exports q;
								requires java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"""
							package q;
							public class Y {
							   public static java.sql.Connection con = null;
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-exports") // Y.con unreliably refers to Connection (missing requires transitive)
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}
	public void test008a() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports p.q;
								requires java.base;
								requires transitive java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "X.java",
						"""
							package p.q;
							public class X {
								public static java.sql.Connection getConnection() {
									return null;
								}
							}""");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								requires java.base;
								requires mod.one;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "q" + File.separator + "r", "Y.java",
						"""
							package q.r;
							public class Y {
							   java.sql.Connection con = p.q.X.getConnection();
							}""");

		String systemDirectory = OUTPUT_DIR+File.separator+"system";
		writeFile(systemDirectory, "readme.txt", "Not a valid system");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append("--system ").append(systemDirectory)
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"invalid location for system libraries: ---OUTPUT_DIR_PLACEHOLDER---/system\n",
				false,
				"system");
	}
	public void test009() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports p;
								requires java.base;
								requires transitive java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								public static java.sql.Connection getConnection() {
									return null;
								}
							}""");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								requires java.base;
								requires mod.one;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"""
							package q;
							public class Y {
							   java.sql.Connection con = p.X.getConnection();
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}
	private void createUnnamedLibrary(String unnamedLoc, String unnamedBin) {
		writeFile(unnamedLoc + File.separator + "s" + File.separator + "t", "Tester.java",
				"""
					package s.t;
					public class Tester {
					}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + unnamedBin)
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\"")
			.append(" -sourcepath \"" + unnamedLoc + "\" ")
			.append(unnamedLoc + File.separator + "s" + File.separator + "t" + File.separator + "Tester.java");

		runConformTest(new String[]{},
				buffer.toString(),
				"",
				"",
				false);
	}
	private void createReusableModules(String srcDir, String outDir, File modDir) {
		String moduleLoc = srcDir + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports p;
								requires java.base;
								requires transitive java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								public static java.sql.Connection getConnection() {
									return null;
								}
							}""");
		// This one is not exported (i.e. internal to this module)
		writeFileCollecting(files, moduleLoc + File.separator + "p1", "X1.java",
				"""
					package p1;
					public class X1 {
						public static java.sql.Connection getConnection() {
							return null;
						}
					}""");

		moduleLoc = srcDir + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								exports q;
								requires java.base;
								requires mod.one;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"""
							package q;
							public class Y {
							   java.sql.Connection con = p.X.getConnection();
							}""");

		StringBuilder buffer = new StringBuilder();
			buffer.append("-d " + outDir )
			.append(" -9 ")
			.append(" --module-path \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + srcDir + "\"");
		for (String fileName : files)
			buffer.append(" \"").append(fileName).append("\"");

		runConformTest(new String[]{},
				buffer.toString(),
				"",
				"",
				false);

		String fileName = modDir + File.separator + "mod.one.jar";
		try {
			Util.zip(new File(outDir + File.separator + "mod.one"),
								fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!modDir.exists()) {
			if (!modDir.mkdirs()) {
				fail("Coult not create folder " + modDir);
			}
		}
		File mod2 = new File(modDir, "mod.two");
		if (!mod2.mkdir()) {
			fail("Coult not create folder " + mod2);
		}
		Util.copy(outDir + File.separator + "mod.two", mod2.getAbsolutePath());

		Util.flushDirectoryContent(new File(outDir));
		Util.flushDirectoryContent(new File(srcDir));
	}
	public void test010() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.three {\s
								requires mod.one;
								requires mod.two;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "r", "Z.java",
						"""
							package r;
							public class Z extends Object {
								p.X x = null;
								q.Y y = null;
							}""");

		StringBuilder buffer = new StringBuilder();
			buffer.append("-d " + outDir )
			.append(" -9 ")
			.append(" -p \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir.getAbsolutePath())
			.append("\" ")
			.append(" --module-source-path " + "\"" + srcDir + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false, outDir);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=487421
	public void test011() {
		runConformModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					public class X {
					   java.lang.SecurityException ex = null;
					}""",
				"module-info.java",
				"""
					module mod.one {\s
						requires java.base;
					}"""
	        },
			" -9 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
	}
	// Modules used as regular -classpath (as opposed to --module-path) and module-info referencing
	// those modules are reported as missing.
	public void test012() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.three {\s
								requires mod.one;
								requires mod.two;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "r", "Z.java",
						"""
							package r;
							public class Z extends Object {
							}""");

		StringBuilder buffer = new StringBuilder();
			buffer.append("-d " + outDir )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir + File.separator + "mod.one.jar").append(File.pathSeparator)
			.append(modDir + File.separator + "mod.two").append(File.pathSeparator)
			.append("\" ")
			.append(" --module-source-path " + "\"" + srcDir + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 2)
						requires mod.one;
						         ^^^^^^^
					mod.one cannot be resolved to a module
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 3)
						requires mod.two;
						         ^^^^^^^
					mod.two cannot be resolved to a module
					----------
					2 problems (2 errors)
					""",
				false,
				"module");
	}
	// Modules used as regular -classpath as opposed to --module-path. The files being compiled
	// aren't part of any modules (i.e. module-info is missing). The files should be able to
	// reference the types from referenced classpath.
	public void test013() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc + File.separator + "p", "Z.java",
						"""
							package r;
							public class Z extends Object {
								p.X x = null;
								q.Y y = null;
							}""");

		StringBuilder buffer = new StringBuilder();
			buffer.append("-d " + outDir )
			.append(" -9")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir + File.separator + "mod.one.jar").append(File.pathSeparator)
			.append(modDir + File.separator + "mod.two").append(File.pathSeparator)
			.append("\" ");
		runConformModuleTest(files,
				buffer,
				"",
				"",
				false,
				outDir);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=495500
	//-source 9
	public void testBug495500a() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					/** */
					public class X {
					}""",
			},
	  "\"" + OUTPUT_DIR +  File.separator + "X.java\""
	  + " -9 -d \"" + OUTPUT_DIR + "\"",
	  "",
	  "",
	  true);
		String expectedOutput = "// Compiled from X.java (version 9 : 53.0, super bit)";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	//-source 8 -target 9
	public void testBug495500b() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					/** */
					public class X {
					}""",
			},
			"\"" + OUTPUT_DIR +  File.separator + "X.java\""
			+ " -9 -source 8 -target 9 -d \"" + OUTPUT_DIR + "\"",
			"",
			"",
			true);
		String expectedOutput = "// Compiled from X.java (version 9 : 53.0, super bit)";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	// compliance 9 -source 9 -target 9
	public void testBug495500c() throws Exception {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					/** */
					public class X {
					}""",
			},
			"\"" + OUTPUT_DIR +  File.separator + "X.java\""
			+ " -9 -source 9 -target 9 -d \"" + OUTPUT_DIR + "\"",
			"",
			"",
			true);
		String expectedOutput = "// Compiled from X.java (version 9 : 53.0, super bit)";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	/*
	 * Test add-exports grants visibility to another module
	 */
	public void test014() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires transitive java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								public static java.sql.Connection getConnection() {
									return null;
								}
							}""");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								requires java.base;
								requires mod.one;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"""
							package q;
							public class Y {
							   java.sql.Connection con = p.X.getConnection();
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}
	public void test015() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires transitive java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								public static java.sql.Connection getConnection() {
									return null;
								}
							}""");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								requires java.base;
								requires java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"""
							package q;
							public class Y {
							   java.sql.Connection con = p.X.getConnection();
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two");
		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 3)
						java.sql.Connection con = p.X.getConnection();
						                          ^^^
					The type p.X is not accessible
					----------
					1 problem (1 error)
					""",
				false,
				"cannot be resolved",
				OUTPUT_DIR + File.separator + out,
				JavacTestOptions.JavacHasABug.JavacBug8207032);
	}
	public void test016() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires transitive java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								public static java.sql.Connection getConnection() {
									return null;
								}
							}""");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								requires java.base;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"""
							package q;
							public class Y {
							   java.sql.Connection con = p.X.getConnection();
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two")
			.append(" --add-reads mod.two=mod.one");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}
	public void test017() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires transitive java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								public static java.sql.Connection getConnection() {
									return null;
								}
							}""");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								requires java.base;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"""
							package q;
							public class Y {
							   java.sql.Connection con = p.X.getConnection();
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.three")
			.append(" --add-reads mod.two=mod.one");

		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 3)
						java.sql.Connection con = p.X.getConnection();
						                          ^^^
					The type p.X is not accessible
					----------
					1 problem (1 error)
					""",
				false,
				"visible",
				OUTPUT_DIR + File.separator + out);
	}
	public void test018() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires transitive java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								public static java.sql.Connection getConnection() {
									return null;
								}
							}""");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								requires java.base;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"""
							package q;
							public class Y {
							   java.sql.Connection con = p.X.getConnection();
							}""");
		moduleLoc = directory + File.separator + "mod.three";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.three {\s
								requires java.base;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "r", "Z.java",
						"""
							package r;
							public class Z {
							   java.sql.Connection con = p.X.getConnection();
							}""");


		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two,mod.three")
			.append(" --add-reads mod.two=mod.one")
			.append(" --add-reads mod.three=mod.one");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}
	/*
	 * Unnamed module tries to access a type from an unexported package successfully due to --add-exports
	 */
	public void test019() {
		Runner runner = new Runner();
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		runner.createFile(moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public abstract class X extends com.sun.security.ntlm.Server {
								//public X() {}
								public X(String arg0, String arg1) throws com.sun.security.ntlm.NTLMException {
									super(arg0, arg1);
								}
							}""");

		runner.commandLine.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -sourcepath " + "\"" + moduleLoc + "\" ")
			.append(" --add-exports java.base/com.sun.security.ntlm=ALL-UNNAMED ");

		runner.javacVersionOptions = JAVAC_SOURCE_9_OPTIONS; // otherwise javac: error: exporting a package from system module java.base is not allowed with --release
		runner.runConformModuleTest();
	}
	/*
	 * Named module tries to access a type from an unnamed module successfully due to --add-reads
	 */
	public void test019b() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String unnamedLoc = directory + File.separator + "nomodule";
		String unnamedBin = OUTPUT_DIR + File.separator + "un_bin";
		String moduleLoc = directory + File.separator + "mod" + File.separator + "mod.one";

		createUnnamedLibrary(unnamedLoc, unnamedBin);

		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {
								exports p.q;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "X.java",
						"""
							package p.q;
							public abstract class X {
								s.t.Tester t;
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(unnamedBin + File.pathSeparator)
			.append("\"")
			.append(" --module-source-path \"" + directory + File.separator + "mod" + "\" ")
			.append(" --add-reads mod.one=ALL-UNNAMED ");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false,
				OUTPUT_DIR + File.separator + out);
	}

	/*
	 * Can only import from a package that contains compilation units (from the unnamed module)
	 */
	public void test019c() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String unnamedLoc = directory + File.separator + "nomodule";
		String unnamedBin = OUTPUT_DIR + File.separator + "un_bin";

		createUnnamedLibrary(unnamedLoc, unnamedBin);

		List<String> files = new ArrayList<>();
		String moduleLoc = directory + File.separator + "mod.one";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {
								exports p.q;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "X.java",
						"""
							package p.q;
							import s.*;
							import s.t.*;
							public abstract class X {
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(unnamedBin + File.pathSeparator)
			.append("\"")
			.append(" --module-source-path \"" + directory + "\" ")
			.append(" --add-reads mod.one=ALL-UNNAMED ");

		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/q/X.java (at line 2)
						import s.*;
						       ^
					The package s is not accessible
					----------
					1 problem (1 error)
					""",
				false,
				"package s",
				 OUTPUT_DIR + File.separator + out,
				 JavacTestOptions.JavacHasABug.JavacBug8204534);
	}
	/*
	 * Unnamed module tries to access a type from an unexported package, fail
	 */
	public void test019fail() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public abstract class X extends com.sun.security.ntlm.Server {
								//public X() {}
								public X(String arg0, String arg1) throws com.sun.security.ntlm.NTLMException {
									super(arg0, arg1);
								}
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -sourcepath " + "\"" + moduleLoc + "\" ");

		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 2)
						public abstract class X extends com.sun.security.ntlm.Server {
						                                ^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					The type com.sun.security.ntlm.Server is not accessible
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 4)
						public X(String arg0, String arg1) throws com.sun.security.ntlm.NTLMException {
						                                          ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					The type com.sun.security.ntlm.NTLMException is not accessible
					----------
					2 problems (2 errors)
					""",
				false,
				"package com.sun.security.ntlm");
				/* javac9:
				 * src/mod.one/p/X.java:2: error: package com.sun.security.ntlm is not visible
				 * public abstract class X extends com.sun.security.ntlm.Server {
				 *                                                 ^
				 *   (package com.sun.security.ntlm is declared in module java.base, which does not export it to the unnamed module)
				 * src/mod.one/p/X.java:4: error: package com.sun.security.ntlm is not visible
				 * public X(String arg0, String arg1) throws com.sun.security.ntlm.NTLMException {
				 *                                                           ^
				 *   (package com.sun.security.ntlm is declared in module java.base, which does not export it to the unnamed module)
				 */
				/* javac10:
				 * src/mod.one/p/X.java:2: error: package com.sun.security.ntlm does not exist
				 * public abstract class X extends com.sun.security.ntlm.Server {
				 *                                                      ^
				 * src/mod.one/p/X.java:4: error: package com.sun.security.ntlm does not exist
				 * public X(String arg0, String arg1) throws com.sun.security.ntlm.NTLMException {
				 *                                                                ^
				 */
	}
	public void test020() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires transitive java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								public static java.sql.Connection getConnection() {
									return null;
								}
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one=mod.two,mod.three");

		runNegativeModuleTest(files,
				buffer,
				"",
				"incorrectly formatted option: --add-exports mod.one=mod.two,mod.three\n",
				false,
				"option");
	}
	public void test021() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires transitive java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								public static java.sql.Connection getConnection() {
									return null;
								}
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-reads mod.one/mod.two");

		runNegativeModuleTest(files,
				buffer,
				"",
				"incorrectly formatted option: --add-reads mod.one/mod.two\n",
				false,
				"option");
	}
	public void test022() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires transitive java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								public static java.sql.Connection getConnection() {
									return null;
								}
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.three")
			.append(" --add-exports mod.one/p=mod.three");

		runNegativeModuleTest(files,
				buffer,
				"",
				"can specify a package in a module only once with --add-export\n",
				false,
				"export");
	}
	public void test023() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires transitive java.sql;
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append("\"" + moduleLoc +  File.separator + "module-info.java\" ")
			.append(" -extdirs " + OUTPUT_DIR + File.separator + "src");

		runNegativeModuleTest(files,
				buffer,
				"",
				"option -extdirs not supported at compliance level 9 and above\n",
				false,
				"extdirs");
	}
	public void test024() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires transitive java.sql;
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" \"" + moduleLoc +  File.separator + "module-info.java\" ")
			.append(" -bootclasspath " + OUTPUT_DIR + File.separator + "src");

		runNegativeModuleTest(files,
				buffer,
				"",
				"option -bootclasspath not supported at compliance level 9 and above\n",
				false,
				"not allowed"); // when specifying -bootclasspath javac answers: "option --boot-class-path not allowed with target 1.9" (two bugs)
	}
	public void test025() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires transitive java.sql;
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append("\"" + moduleLoc +  File.separator + "module-info.java\" ")
			.append(" -endorseddirs " + OUTPUT_DIR + File.separator + "src");

		runNegativeModuleTest(files,
				buffer,
				"",
				"option -endorseddirs not supported at compliance level 9 and above\n",
				false,
				"endorseddirs");
	}
	public void test026() {
		Runner runner = new Runner();
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		runner.createFile(
						moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires transitive java.sql;
							}""");
		String javaHome = System.getProperty("java.home");
		runner.commandLine.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" --system \"").append(javaHome).append("\"");

		runner.javacVersionOptions = JAVAC_SOURCE_9_OPTIONS;
		runner.runConformModuleTest();
	}
	/**
	 * Mixed case of exported and non exported packages being referred to in another module
	 */
	public void test028() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.three {\s
								requires mod.one;
								requires mod.two;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "r", "Z.java",
						"""
							package r;
							public class Z extends Object {
								p.X x = null;
								p1.X1 x1 = null;
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append(" --module-source-path " + "\"" + srcDir + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/r/Z.java (at line 4)
						p1.X1 x1 = null;
						^^^^^
					The type p1.X1 is not accessible
					----------
					1 problem (1 error)
					""",
				false,
				"visible",
				outDir);
	}
	public void test029() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								public static java.sql.Connection getConnection() {
									return null;
								}
							}""");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								requires java.base;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"""
							package q;
							public class Y {
							   java.sql.Connection con = p.X.getConnection();
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two,mod.three")
			.append(" --add-reads mod.two=mod.one");

		runNegativeModuleTest(files,
			buffer,
			"",
			"""
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 3)
					public static java.sql.Connection getConnection() {
					              ^^^^^^^^^^^^^^^^^^^
				The type Connection from module java.sql may not be accessible to clients due to missing \'requires transitive\'
				----------
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 3)
					java.sql.Connection con = p.X.getConnection();
					^^^^^^^^^^^^^^^^^^^
				The type java.sql.Connection is not accessible
				----------
				2 problems (1 error, 1 warning)
				""",
			false,
			"visible");
	}
	public void test030() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								public static java.sql.Connection getConnection() {
									return null;
								}
							}""");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								requires java.base;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"""
							package q;
							import java.sql.*;
							public class Y {
							   Connection con = null;
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-exports") // getConnection() leaks non-transitively required type
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two,mod.three")
			.append(" --add-reads mod.two=mod.one");

		runNegativeModuleTest(files,
			buffer,
			"",
			"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 2)
					import java.sql.*;
					       ^^^^^^^^
				The package java.sql is not accessible
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 4)
					Connection con = null;
					^^^^^^^^^^
				Connection cannot be resolved to a type
				----------
				2 problems (2 errors)
				""",
			false,
			"visible",
			OUTPUT_DIR + File.separator + out);
	}
	public void test031() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X {
								static java.sql.Connection getConnection() {
									return null;
								}
							}""");
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								requires java.base;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "Y.java",
						"""
							package q;
							import java.sql.Connection;
							public class Y {
							   Connection con = null;
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"")
			.append(" --add-exports mod.one/p=mod.two,mod.three")
			.append(" --add-reads mod.two=mod.one");

		runNegativeModuleTest(files,
			buffer,
			"",
			"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 2)
					import java.sql.Connection;
					       ^^^^^^^^^^^^^^^^^^^
				The type java.sql.Connection is not accessible
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/Y.java (at line 4)
					Connection con = null;
					^^^^^^^^^^
				Connection cannot be resolved to a type
				----------
				2 problems (2 errors)
				""",
			false,
			"visible",
			OUTPUT_DIR + File.separator + out);
	}
	public void test032() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
							}""");
		writeFileCollecting(files, moduleLoc+"/p", "X.java",
						"""
							package p;
							public class X {
								public static class Inner {
								}
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
			buffer,
			"",
			"",
			false,
			OUTPUT_DIR + File.separator + out);
	}
	/**
	 * Test that a module can't access types/packages in a plain Jar put in classpath
	 */
	public void test033() {
		File libDir = new File(LIB_DIR);
		Util.delete(libDir); // make sure we recycle the libs
 		libDir.mkdirs();
		try {
			Util.createJar(
				new String[] {
					"a/A.java",
					"""
						package a;
						public class A {
						}"""
				},
				LIB_DIR + "/lib1.jar",
				JavaCore.VERSION_9);
		} catch (IOException e) {
			// ignore
		}
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X extends a.A {
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(LIB_DIR).append(File.separator).append("lib1.jar").append(File.pathSeparator).append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 2)
						public class X extends a.A {
						                       ^^^
					The type a.A is not accessible
					----------
					1 problem (1 error)
					""",
				false,
				"package a does not exist");
	}
	/**
	 * Test that a module can't access types/packages in a plain Jar put in modulepath
	 * but not explicitly added to the "requires" clause
	 */
	public void test034() {
		File libDir = new File(LIB_DIR);
		Util.delete(libDir); // make sure we recycle the libs
 		libDir.mkdirs();
		try {
			Util.createJar(
				new String[] {
					"a/A.java",
					"""
						package a;
						public class A {
						}"""
				},
				LIB_DIR + "/lib1.jar",
				JavaCore.VERSION_9);
		} catch (IOException e) {
			// ignore
		}
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X extends a.A {
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString()).append("\" ")
			.append("-p \"")
			.append(LIB_DIR).append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/X.java (at line 2)
						public class X extends a.A {
						                       ^^^
					The type a.A is not accessible
					----------
					1 problem (1 error)
					""",
				false,
				"does not read");
	}
	/**
	 * Test that a module can access types/packages in a plain Jar put in modulepath
	 * and explicitly added to the "requires" clause
	 */
	public void test035() {
		File libDir = new File(LIB_DIR);
		Util.delete(libDir); // make sure we recycle the libs
 		libDir.mkdirs();
		try {
			Util.createJar(
				new String[] {
					"a/A.java",
					"""
						package a;
						public class A {
						}"""
				},
				LIB_DIR + "/lib1.jar",
				JavaCore.VERSION_9);
		} catch (IOException e) {
			// ignore
		}
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
								requires java.sql;
								requires lib1;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
						"""
							package p;
							public class X extends a.A {
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString()).append("\" ")
			.append("-p \"")
			.append(LIB_DIR).append("\" ")
			.append(" -warn:-module ")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}
	public void testBug515985() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports pm;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "impl", "Other.java",
						"""
							package impl;
							public class Other {
							    public void privateMethod() {}\
							}
							""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"""
							package pm;
							import impl.Other;
							public class C1 extends Other {
							}
							""");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								requires mod.one;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "po", "Client.java",
						"""
							package po;
							import pm.C1;
							public class Client {
							    void test1(C1 one) {
							        one.privateMethod(); // ecj: The method privateMethod() is undefined for the type C1
							    }
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}

	public void testApiLeak1() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports pm;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "impl", "Other.java",
						"""
							package impl;
							public class Other {
							}
							""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"""
							package pm;
							import impl.Other;
							public class C1 extends Other {
								public void m1(Other o) {}
							}
							""");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								requires mod.one;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "impl", "Other.java",
						"""
							package impl;
							public class Other {
							}
							""");
		writeFileCollecting(files, moduleLoc + File.separator + "po", "Client.java",
						"""
							package po;
							import pm.C1;
							public class Client {
							    void test1(C1 one) {
							        one.m1(one);
							    }
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/pm/C1.java (at line 4)
						public void m1(Other o) {}
						               ^^^^^
					The type Other is not exported from this module
					----------
					1 problem (1 warning)
					""",
				false);
	}

	/**
	 * Same-named classes should not conflict, since one is not accessible.
	 * Still a sub class of the inaccessible class can be accessed and used for a method argument.
	 */
	public void testApiLeak2() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports pm;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "impl", "SomeImpl.java",
						"""
							package impl;
							public class SomeImpl {
							}
							""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"""
							package pm;
							import impl.SomeImpl;
							public class C1 {
								public void m1(SomeImpl o) {}
							}
							""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "Other.java",
						"""
							package pm;
							import impl.SomeImpl;
							public class Other extends SomeImpl {
							}
							""");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								requires mod.one;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "impl", "SomeImpl.java",
						"""
							package impl;
							public class SomeImpl {
							}
							""");
		writeFileCollecting(files, moduleLoc + File.separator + "po", "Client.java",
						"""
							package po;
							import pm.C1;
							import pm.Other;
							import impl.SomeImpl;
							public class Client {
							    void test1(C1 one) {
									 SomeImpl impl = new SomeImpl();
							        one.m1(impl);
									 one.m1(new Other());
							    }
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -info:+exports")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. INFO in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/pm/C1.java (at line 4)
						public void m1(SomeImpl o) {}
						               ^^^^^^^^
					The type SomeImpl is not exported from this module
					----------
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/po/Client.java (at line 8)
						one.m1(impl);
						    ^^
					The method m1(impl.SomeImpl) in the type C1 is not applicable for the arguments (impl.SomeImpl)
					----------
					2 problems (1 error, 0 warnings, 1 info)
					""",
				false,
				"incompatible",
				OUTPUT_DIR + File.separator + out);
	}

	// conflict even without any reference to the conflicting package
	// - three-way conflict between two direct and one indirect dependency
	public void testPackageConflict0() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.x {\s
								exports pm;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1x.java",
						"""
							package pm;
							public class C1x {
							}
							""");

		moduleLoc = directory + File.separator + "mod.y";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.y {\s
								requires transitive mod.x;
							}""");

		moduleLoc = directory + File.separator + "mod.one";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports pm;
								exports p2;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"""
							package pm;
							public class C1 {
							}
							""");
		writeFileCollecting(files, moduleLoc + File.separator + "p2", "C2.java",
						"""
							package p2;
							public class C2 {
							}
							""");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								exports pm;
								exports p2.sub;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C3.java",
						"""
							package pm;
							public class C3 {
							}
							""");
		writeFileCollecting(files, moduleLoc + File.separator + "p2" + File.separator + "sub", "C4.java",
						"""
							package p2.sub;
							public class C4 {
							}
							""");

		moduleLoc = directory + File.separator + "mod.three";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.three {\s
								requires mod.one;
								requires mod.two;
								requires transitive mod.y;
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 2)
						requires mod.one;
						^^^^^^^^^^^^^^^^
					The package pm is accessible from more than one module: mod.one, mod.two, mod.x
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 3)
						requires mod.two;
						^^^^^^^^^^^^^^^^
					The package pm is accessible from more than one module: mod.one, mod.two, mod.x
					----------
					3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 4)
						requires transitive mod.y;
						^^^^^^^^^^^^^^^^^^^^^^^^^
					The package pm is accessible from more than one module: mod.one, mod.two, mod.x
					----------
					3 problems (3 errors)
					""",
				false,
				"reads package pm");
	}

	public void testPackageConflict1() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports pm;
								exports p2;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"""
							package pm;
							public class C1 {
							}
							""");
		writeFileCollecting(files, moduleLoc + File.separator + "p2", "C2.java",
						"""
							package p2;
							public class C2 {
							}
							""");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								exports pm;
								exports p2.sub;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C3.java",
						"""
							package pm;
							public class C3 {
							}
							""");
		writeFileCollecting(files, moduleLoc + File.separator + "p2" + File.separator + "sub", "C4.java",
						"""
							package p2.sub;
							public class C4 {
							}
							""");

		moduleLoc = directory + File.separator + "mod.three";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.three {\s
								requires mod.one;
								requires mod.two;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "po", "Client.java",
						"""
							package po;
							import pm.*;
							import pm.C3;
							import p2.C2;
							public class Client {
							    void test1(C1 one) {
							    }
								 pm.C1 f1;
								 p2.sub.C4 f4;
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 2)
						requires mod.one;
						^^^^^^^^^^^^^^^^
					The package pm is accessible from more than one module: mod.one, mod.two
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 3)
						requires mod.two;
						^^^^^^^^^^^^^^^^
					The package pm is accessible from more than one module: mod.one, mod.two
					----------
					----------
					3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/po/Client.java (at line 2)
						import pm.*;
						       ^^
					The package pm is accessible from more than one module: mod.one, mod.two
					----------
					4. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/po/Client.java (at line 3)
						import pm.C3;
						       ^^
					The package pm is accessible from more than one module: mod.one, mod.two
					----------
					5. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/po/Client.java (at line 8)
						pm.C1 f1;
						^^
					The package pm is accessible from more than one module: mod.one, mod.two
					----------
					5 problems (5 errors)
					""",
				false,
				"reads package pm");
	}
	// conflict foreign<->local package
	public void testPackageConflict3() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		List<String> files = new ArrayList<>();
		String moduleLoc = directory + File.separator + "mod.one";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports pm;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"""
							package pm;
							public class C1 {
							}
							""");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								requires mod.one;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C3.java",
						"""
							package pm;
							public class C3 {
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/pm/C3.java (at line 1)
						package pm;
						        ^^
					The package pm conflicts with a package accessible from another module: mod.one
					----------
					1 problem (1 error)
					""",
				false,
				"",
				OUTPUT_DIR + File.separator + out);
	}
	public void testPackageConflict4() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		File srcDir = new File(directory);

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.x {\s
								exports pm;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"""
							package pm;
							public class C1 {
							}
							""");

		moduleLoc = directory + File.separator + "mod.y";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.y {\s
								exports pm;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"""
							package pm;
							public class C1 {
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
		Util.flushDirectoryContent(srcDir);
		files.clear();
		writeFileCollecting(files, directory + File.separator + "p", "X.java",
						"public class X extends pm.C1 { \n" +
						"}");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\"");
		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 1)
						public class X extends pm.C1 {\s
						                       ^^
					The package pm is accessible from more than one module: mod.x, mod.y
					----------
					1 problem (1 error)
					""",
				false,
				"package conflict");
	}
	/**
	 * currently disabled because ECJ allows unnamed modules to read from other modules from
	 * module-path even if they are not part of root modules.
	 */
	public void _testPackageConflict4a() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		File srcDir = new File(directory);

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.x {\s
								exports pm;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"""
							package pm;
							public class C1 {
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
		Util.flushDirectoryContent(srcDir);
		files.clear();
		writeFileCollecting(files, directory + File.separator + "p", "X.java",
						"public class X extends pm.C1 { \n" +
						"}");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\"");
		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 1)
						public class X extends pm.C1 {\s
						                       ^^
					pm cannot be resolved to a type
					----------
					1 problem (1 error)
					""",
				false,
				"package conflict");
	}
	public void testPackageConflict5() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		File srcDir = new File(directory);

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.x {\s
								exports pm;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"""
							package pm;
							public class C1 {
							}
							""");

		moduleLoc = directory + File.separator + "mod.y";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.y {\s
								exports pm;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"""
							package pm;
							public class C1 {
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
		Util.flushDirectoryContent(srcDir);
		files.clear();
		writeFileCollecting(files, directory + File.separator + "p", "X.java",
						"public class X extends pm.C1 { \n" +
						"}");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\"")
			.append(" --add-modules mod.x,mod.y");
		runNegativeModuleTest(files,
				buffer,
				"",
				"The package pm is accessible from more than one module: mod.y, mod.x\n",
				false,
				"reads package pm from both");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\"")
			.append(" --add-modules mod.x,mod.z");
		runNegativeModuleTest(files,
				buffer,
				"",
				"invalid module name: mod.z\n",
				false,
				"module not found");
	}
	public void testPackageConflict6() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		File srcDir = new File(directory);

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.x {\s
								exports pm;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"""
							package pm;
							public class C1 {
							}
							""");

		moduleLoc = directory + File.separator + "mod.y";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.y {\s
								exports pm;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"""
							package pm;
							public class C1 {
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
		Util.flushDirectoryContent(srcDir);
		files.clear();
		writeFileCollecting(files, directory + File.separator + "p", "X.java",
						"public class X extends pm.C1 { \n" +
						"}");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\"")
			.append(" --add-modules mod.x,");
		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 1)
						public class X extends pm.C1 {\s
						                       ^^
					The package pm is accessible from more than one module: mod.x, mod.y
					----------
					1 problem (1 error)
					""",
				false,
				"package conflict");
	}
	public void testPackageConflict7() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		File srcDir = new File(directory);

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.x {\s
								exports pm;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"""
							package pm;
							public class C1 {
							}
							""");

		moduleLoc = directory + File.separator + "mod.y";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.y {\s
								exports pm;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"""
							package pm;
							public class C1 {
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
		Util.flushDirectoryContent(srcDir);
		files.clear();
		writeFileCollecting(files, directory + File.separator + "p", "X.java",
						"public class X { \n" +
						"}");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\"")
			.append(" --add-modules mod.x,mod.y");
		runNegativeModuleTest(files,
				buffer,
				"",
				"The package pm is accessible from more than one module: mod.y, mod.x\n",
				false,
				"reads package pm from both");
	}
	public void testPackageTypeConflict1() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"module mod.one { \n" +
						"}");
		writeFileCollecting(files, moduleLoc + File.separator + "p1" + File.separator + "p2", "t3.java",
						"""
							package p1.p2;
							public class t3 {
							}
							""");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								exports p1.p2.t3;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p1" + File.separator + "p2" + File.separator + "t3", "t4.java",
						"""
							package p1.p2.t3;
							public class t4 {
							}
							""");

		moduleLoc = directory + File.separator + "mod.three";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.three {\s
								requires mod.one;
								requires mod.two;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "po", "Client.java",
						"""
							package po;
							public class Client {
								 p1.p2.t3.t4 f;
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}

	public void testBug519922() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		List<String> files = new ArrayList<>();
		writeFileCollecting(files, directory + File.separator + "test", "Test.java",
						"""
							package test;
							
							public class Test implements org.eclipse.SomeInterface {
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ");

		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/test/Test.java (at line 3)
						public class Test implements org.eclipse.SomeInterface {
						                             ^^^^^^^^^^^
					org.eclipse cannot be resolved to a type
					----------
					1 problem (1 error)
					""",
				false,
				"does not exist");
	}
	public void testMixedSourcepath() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod" + File.separator + "mod.one";

		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {
								exports p.q;
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\"")
			.append(" -sourcepath \"" + directory + "\" ")
			.append(" --module-source-path \"" + directory + File.separator + "mod" + "\" ")
			.append(" --add-reads mod.one=ALL-UNNAMED ");

		runNegativeModuleTest(files,
				buffer,
				"",
				"cannot specify both -source-path and --module-source-path\n",
				false,
				"cannot specify both",
				OUTPUT_DIR + File.separator + out);
	}

	// causes: non-public type (C0), non-exported package (p.priv)
	// locations: field, method parameter, method return
	public void testAPILeakDetection1() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports p.exp;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp", "C1.java",
						"""
							package p.exp;
							import p.priv.*;
							class C0 {
								public void test(C0 c) {}
							}
							public class C1 {
								public C2 f;
								public void test1(C0 c) {}
								public void test2(C2 c) {}
								protected void test3(C0 c) {}
								protected void test4(C2 c) {}
								public p.priv.C2 test5() { return null; }
							}
							""");

		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "priv", "C2.java",
						"""
							package p.priv;
							public class C2 {
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:exports")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files, buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 7)
						public C2 f;
						       ^^
					The type C2 is not exported from this module
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 8)
						public void test1(C0 c) {}
						                  ^^
					The type C0 is not accessible to clients that require this module
					----------
					3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 9)
						public void test2(C2 c) {}
						                  ^^
					The type C2 is not exported from this module
					----------
					4. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 12)
						public p.priv.C2 test5() { return null; }
						       ^^^^^^^^^
					The type C2 is not exported from this module
					----------
					4 problems (4 errors)
					""",
				false,
				"is not exported");
	}

	// details: in array, parameterized type
	public void testAPILeakDetection2() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports p.exp;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp", "C1.java",
						"""
							package p.exp;
							import java.util.*;
							class C0 {
								public void test(C0 c) {}
							}
							public class C1 {
								public List<C0> f1;
								public C0[] f2;
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:+exports")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files, buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 7)
						public List<C0> f1;
						            ^^
					The type C0 is not accessible to clients that require this module
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 8)
						public C0[] f2;
						       ^^
					The type C0 is not accessible to clients that require this module
					----------
					2 problems (2 errors)
					""",
				false,
				"not accessible to clients");
	}

	// suppress
	public void testAPILeakDetection3() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports p.exp;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp", "C1.java",
						"""
							package p.exp;
							import java.util.*;
							class C0 {
								public void test(C0 c) {}
							}
							public class C1 {
								@SuppressWarnings("exports")
								public List<C0> f1;
								@SuppressWarnings("exports")
								public C0[] f2;
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:+exports,+suppress")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
	}

	// details: nested types
	public void testAPILeakDetection4() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports p.exp;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp", "C1.java",
						"""
							package p.exp;
							public class C1 {
								static class C3 {
									public static class C4 {}
								}
								public C3 f1;
								public C3.C4 f2;
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:+exports")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files, buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 6)
						public C3 f1;
						       ^^
					The type C1.C3 is not accessible to clients that require this module
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 7)
						public C3.C4 f2;
						       ^^^^^
					The type C1.C3.C4 is not accessible to clients that require this module
					----------
					2 problems (2 errors)
					""",
				false,
				"one is not accessible to clients");
	}

	// type from non-transitive required module
	public void testAPILeakDetection5() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports p.exp1;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp1", "C1.java",
						"""
							package p.exp1;
							public class C1 {
							}
							""");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								exports p.exp2;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp2", "C2.java",
						"""
							package p.exp2;
							public class C2 {
							}
							""");

		moduleLoc = directory + File.separator + "mod.three";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.three {\s
								requires mod.one; // missing transitive
								requires transitive mod.two;
								exports p.exp3;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp3", "C3.java",
						"""
							package p.exp3;
							public class C3 {
								public void m1(p.exp1.C1 arg) {}
								public void m2(p.exp2.C2 arg) {}
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:+exports")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files, buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/p/exp3/C3.java (at line 3)
						public void m1(p.exp1.C1 arg) {}
						               ^^^^^^^^^
					The type C1 from module mod.one may not be accessible to clients due to missing \'requires transitive\'
					----------
					1 problem (1 error)
					""",
				false,
				"is not indirectly exported");
	}

	// annotated types in API
	public void testAPILeakDetection6() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports p.exp;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp", "C1.java",
						"""
							package p.exp;
							import java.lang.annotation.*;
							@Target(ElementType.TYPE_USE)
							@interface ANN {}
							class C0 {}
							public class C1 {
								public @ANN String f1;
								public @ANN C0 f3;
								public @ANN String test(@ANN String arg, @ANN C0 c) { return ""; }
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:exports")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files, buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 8)
						public @ANN C0 f3;
						            ^^
					The type C0 is not accessible to clients that require this module
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/exp/C1.java (at line 9)
						public @ANN String test(@ANN String arg, @ANN C0 c) { return ""; }
						                                              ^^
					The type C0 is not accessible to clients that require this module
					----------
					2 problems (2 errors)
					""",
				false,
				"is not accessible to clients");
	}

	// enum API
	public void testAPILeakDetection7() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports p.exp;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "exp", "C1.java",
						"""
							package p.exp;
							public enum C1 {
								X, Y, Z;
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:exports")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runConformModuleTest(files, buffer,
				"",
				"",
				false);
	}

	public void testBug486013_comment27() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String projLoc = directory + File.separator + "Proj";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, projLoc + File.separator + "p" + File.separator + "exp", "C1.java",
						"""
							package p.exp;
							import java.util.*;
							public class C1 {
								List<?> l;
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -err:exports");

		runConformModuleTest(files, buffer,
				"",
				"",
				false);
	}
	public void testBug518295a() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.three {\s
								requires mod.one;
								requires mod.two;
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append("-classNames mod.one/p.XYZ")
		.append(" --module-source-path " + "\"" + srcDir + "\"");

		files.forEach(name -> buffer.append(" \"" + name + "\""));
		runNegativeTest(new String[0],
				buffer.toString(),
				"",
				"invalid class name: mod.one/p.XYZ\n",
				false);
	}
	public void testBug518295b() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.three {\s
								requires mod.one;
								requires mod.two;
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append("-classNames mod.xyz/p.X")
		.append(" --module-source-path " + "\"" + srcDir + "\"");

		files.forEach(name -> buffer.append(" \"" + name + "\""));
		runNegativeTest(new String[0],
				buffer.toString(),
				"",
				"invalid module name: mod.xyz\n",
				false);
	}
	public void testBug518295c() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.three {\s
								requires mod.one;
								requires mod.two;
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append("-classNames mod.one/p.X")
		.append(" --module-source-path " + "\"" + srcDir + "\"");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false,
				outDir);
	}
	public void testUnnamedPackage_Bug520839() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.base;
							}""");
		writeFileCollecting(files, moduleLoc, "X.java",
						"public class X {\n" +
						"}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
			buffer,
			"",
			"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/X.java (at line 1)
					public class X {
					^
				Must declare a named package because this compilation unit is associated to the named module \'mod.one\'
				----------
				1 problem (1 error)
				""",
			false,
			"unnamed package is not allowed in named modules",
			OUTPUT_DIR + File.separator + out);
	}
	public void testAutoModule1() throws Exception {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);

		String[] sources = {
			"p/a/X.java",
			"package p.a;\n" +
			"public class X {}\n;"
		};
		String jarPath = OUTPUT_DIR + File.separator + "lib-x.jar";
		Util.createJar(sources, jarPath, "1.8");

		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires lib.x;
							}""");
		writeFileCollecting(files, moduleLoc+File.separator+"q", "X.java",
						"""
							package q;
							public class X {
								p.a.X f;
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -info:+module ")
			.append(" --module-path " + "\"" + jarPath + "\"");

		runConformModuleTest(files,
			buffer,
			"",
			"""
				----------
				1. INFO in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)
					requires lib.x;
					         ^^^^^
				Name of automatic module \'lib.x\' is unstable, it is derived from the module\'s file name.
				----------
				1 problem (1 info)
				""",
			false,
			OUTPUT_DIR + File.separator + out);
	}
	public void testBug521458a() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod._3_ {\s
								requires mod.one;
								requires mod.two;
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append("-classNames mod.one/p.X")
		.append(" --module-source-path " + "\"" + srcDir + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"module name mod._3_ does not match expected name mod.three\n",
				false,
				"does not match expected name");
	}
	/*
	 * Disabled because the parser seem to take the module path as mod and not mod.e
	 */
	public void _testBug521458b() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.3 {\s
								requires mod.one;
								requires mod.two;
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" -p \"")
		.append(Util.getJavaClassLibsAsString())
		.append(modDir.getAbsolutePath())
		.append("\" ")
		.append("-classNames mod.one/p.X")
		.append(" --module-source-path " + "\"" + srcDir + "\"");

		runNegativeModuleTest(files,
				buffer,
				"",
				"module name mod.3 does not match expected name mod.three\r\n",
				false,
				outDir);
	}
public void testBug521362_emptyFile() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports p1;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p1", "X.java",
						"");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
			buffer,
			"",
			"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)
					exports p1;
					        ^^
				The package p1 does not exist or is empty
				----------
				1 problem (1 error)
				""",
			false,
			"empty",
			OUTPUT_DIR + File.separator + out);
	}
	public void testBug521362_mismatchingdeclaration() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports p1;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p1", "X.java",
						"package q;\n");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
			buffer,
			"",
			"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)
					exports p1;
					        ^^
				The package p1 does not exist or is empty
				----------
				1 problem (1 error)
				""",
			false,
			"package is empty",
			OUTPUT_DIR + File.separator + out);
	}
	public void testBug521362_multiplePackages() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports p1;
								exports p2;
								exports p3;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p1", "X.java",
						"package q;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p2", "X.java",
				"package q2;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p3", "X.java",
				"package p3;\n");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
			buffer,
			"",
			"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)
					exports p1;
					        ^^
				The package p1 does not exist or is empty
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 3)
					exports p2;
					        ^^
				The package p2 does not exist or is empty
				----------
				2 problems (2 errors)
				""",
			false,
			"package is empty",
			OUTPUT_DIR + File.separator + out);
	}
	public void testBug521362_multiplePackages2() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports p1;
								exports p2;
								exports p3.p4.p5;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p1", "X.java",
						"package q;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p2", "X.java",
				"package q2;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "p3" + File.separator + "p4" + File.separator + "p5", "X.java",
				"package p3.p4.p5;\n");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(files,
			buffer,
			"",
			"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)
					exports p1;
					        ^^
				The package p1 does not exist or is empty
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 3)
					exports p2;
					        ^^
				The package p2 does not exist or is empty
				----------
				2 problems (2 errors)
				""",
			false,
			"package is empty",
			OUTPUT_DIR + File.separator + out);
	}
	/*
	 * Test that when module-info is the only file being compiled, the class is still
	 * generated inside the module's sub folder.
	 */
	public void testBug500170a() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.sql;
							}""");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java");

		Set<String> classFiles = runConformModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"",
				false);
		String fileName = OUTPUT_DIR + File.separator + out + File.separator + "mod.one" + File.separator + "module-info.class";
		assertClassFile("Missing modul-info.class: " + fileName, fileName, classFiles);
	}
	/*
	 * Test that no NPE is thrown when the module-info is compiled at a level below 9
	 */
	public void testBug500170b() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.sql;
							}""");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -8")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ");

		runNegativeModuleTest(files,
				buffer,
				"",
				"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 1)
					module mod.one {\s
					^^^^^^
				Syntax error on token(s), misplaced construct(s)
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 1)
					module mod.one {\s
					^^^^^^^^^^^^^^
				Syntax error on token(s), misplaced construct(s)
				----------
				3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 2)
					requires java.sql;
					             ^
				Syntax error on token ".", , expected
				----------
				4. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/module-info.java (at line 3)
					}
					^
				Syntax error on token "}", delete this token
				----------
				4 problems (4 errors)
				""",
				false,
				"modules are not supported");
	}
	public void testBug522472c() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		File srcDir = new File(directory);
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"""
					module mod.one {\s
						exports x.y.z;
						exports a.b.c;
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z",
				"X.java",
				"package x.y.z;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "a" + File.separator + "b" + File.separator + "c",
				"A.java",
				"package a.b.c;\n" +
				"public class A {}");

		moduleLoc = directory + File.separator + "mod.one.a";
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"""
					module mod.one.a {\s
						exports x.y.z;
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z",
				"X.java",
				"package x.y.z;\n" +
				"public class X {}\n");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);

		Util.flushDirectoryContent(srcDir);
		files.clear();
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"""
					module mod.two {\s
						requires mod.one;
						requires mod.one.a;
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "r",
				"Main.java",
				"""
					package p.q.r;
					import a.b.c.*;
					import x.y.z.*;
					@SuppressWarnings("unused")
					public class Main {\
					}""");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ");
		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 2)
						requires mod.one;
						^^^^^^^^^^^^^^^^
					The package x.y.z is accessible from more than one module: mod.one, mod.one.a
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 3)
						requires mod.one.a;
						^^^^^^^^^^^^^^^^^^
					The package x.y.z is accessible from more than one module: mod.one, mod.one.a
					----------
					2 problems (2 errors)
					""",
				false,
				"module mod.two reads package x.y.z from both mod.one and mod.one.a");
	}
	public void testReleaseOption1() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						/** */
						public class X {
						}""",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 8 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "",
		     true);
		String expectedOutput = "// Compiled from X.java (version 1.8 : 52.0, super bit)";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	public void testReleaseOption2() throws Exception {
		if (!isJRE17Plus) return;
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						/** */
						public class X {
						}""",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 10 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "",
		     true);
		String expectedOutput = "// Compiled from X.java (version 10 : 54.0, super bit)";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	public void testReleaseOption3() throws Exception {
		if (!isJRE17Plus) return;
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						/** */
						public class X {
						}""",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 10 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "",
		     true);
		String expectedOutput = "// Compiled from X.java (version 10 : 54.0, super bit)";
			checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
	public void testReleaseOption4() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						/** */
						public class X {
						}""",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 6 -source 1.6 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "option -source is not supported when --release is used\n",
		     true);
	}
	public void testReleaseOption5() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						/** */
						public class X {
						}""",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 8 -target 1.8 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "option -target is not supported when --release is used\n",
		     true);
	}
	public void testReleaseOption6() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						/** */
						public class X {
						}""",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 5 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "release 5 is not found in the system\n",
		     true);
	}
	public void testReleaseOption7() throws Exception {
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						import java.util.stream.*;
						/** */
						public class X {
							public Stream<String> emptyStream() {
								Stream<String> st = Stream.empty();
								return st;
							}
						}""",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 8 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "",
		     true);
	}
	public void testReleaseOption8() throws Exception {
		if (isJRE20Plus) return;
		String output =
				isJRE12Plus ?
						"""
								public java.util.stream.Stream<String> emptyStream() {
								       ^^^^^^^^^^^^^^^^
							java.util.stream cannot be resolved to a type
							""" :
							"""
									public java.util.stream.Stream<String> emptyStream() {
									       ^^^^^^^^^^^^^^^^^^^^^^^
								java.util.stream.Stream cannot be resolved to a type
								""";

		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						/** */
						public class X {
							public java.util.stream.Stream<String> emptyStream() {
								return null;
							}
						}""",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 7 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "----------\n" +
    		 "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" +
    		 output +
    		 "----------\n" +
    		 "1 problem (1 error)\n",
		     true);
	}
	public void testReleaseOption9() throws Exception {
		if (isJRE20Plus) return;
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						interface I {
						  int add(int x, int y);
						}
						public class X {
						  public static void main(String[] args) {
						    I i = (x, y) -> {
						      return x + y;
						    };
						  }
						}
						""",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 7 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     """
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
					I i = (x, y) -> {
					      ^^^^^^^^^
				Lambda expressions are allowed only at source level 1.8 or above
				----------
				1 problem (1 error)
				""",
		     true);
	}
	public void testReleaseOption10() throws Exception {
		if (isJRE12Plus) return;
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.io.*;
						
						public class X {
							public static void main(String[] args) {
								try {
									System.out.println();
									Reader r = new FileReader(args[0]);
									r.read();
								} catch(IOException | FileNotFoundException e) {
									e.printStackTrace();
								}
							}
						}""",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 6 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     """
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 9)
					} catch(IOException | FileNotFoundException e) {
					        ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				Multi-catch parameters are not allowed for source level below 1.7
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 9)
					} catch(IOException | FileNotFoundException e) {
					                      ^^^^^^^^^^^^^^^^^^^^^
				The exception FileNotFoundException is already caught by the alternative IOException
				----------
				2 problems (2 errors)
				""",
		     true);
	}
	public void testReleaseOption11() throws Exception {
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public static void main(String[] args) {
							}
						}""",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\"" +
		     " -bootclasspath " + OUTPUT_DIR + File.separator + "src " +
		     " --release 9 -d \"" + OUTPUT_DIR + "\"",
		     "",
    		 "option -bootclasspath not supported at compliance level 9 and above\n",
		     true);
	}
	public void _testReleaseOption12() throws Exception {
		String javaHome = System.getProperty("java.home");
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public static void main(String[] args) {
							}
						}""",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\"" +
		      " --system \"" + javaHome + "\"" +
		     " --release 6 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "----------\n" +
    		 "option --system not supported below compliance level 9",
		     true);
	}
	public void testReleaseOption13() {
		runConformModuleTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					public class X {
						public static void main(String[] args) {
						}
					}""",
				"module-info.java",
				"""
					module mod.one {\s
						requires java.base;
					}"""
	        },
			" --release 9 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "",
	        true);
	}
	public void testReleaseOption13a() {
		Runner runner = new Runner();
		runner.createFile(
				OUTPUT_DIR +File.separator+"p", "X.java",
				"""
					package p;
					public class X {
						public static void main(String[] args) {
						}
					}""");
		runner.createFile(
				OUTPUT_DIR, "module-info.java",
				"""
					module mod.one {\s
						requires java.base;
					}""");
		runner.commandLine.append(" --release 10");
		runner.javacTestOptions = new JavacTestOptions(ClassFileConstants.JDK10);
		runner.runConformModuleTest();
	}
	public void testReleaseOption14() {
		runNegativeModuleTest(
			new String[] {
				"module-info.java",
				"module mod.one { \n" +
				"}"
			},
			" --release 8 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" ",
			"",
			"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/module-info.java (at line 1)
				module mod.one {\s
				^^^^^^
			Syntax error on token(s), misplaced construct(s)
			----------
			2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/module-info.java (at line 1)
				module mod.one {\s
				           ^^^
			Syntax error, insert "Identifier (" to complete MethodHeaderName
			----------
			3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/module-info.java (at line 1)
				module mod.one {\s
				           ^^^
			Syntax error, insert ")" to complete MethodDeclaration
			----------
			3 problems (3 errors)
			""",
			true,
			/*not tested with javac*/"");
	}
	// Test from https://bugs.eclipse.org/bugs/show_bug.cgi?id=526997
	public void testReleaseOption15() {
		Runner runner = new Runner();
		String fooDir = OUTPUT_DIR + File.separator + "foo";
		runner.createFile(
				fooDir, "Module.java",
				"package foo;\n" +
				"public class Module {}\n");
		runner.createFile(
				fooDir, "X.java",
				"""
					package foo;
					public class X {\s
						public Module getModule(String name) {
							return null;
						}
					}""");
		runner.commandLine.append(" --release 8 ");
	    runner.runConformModuleTest();
	}
	// Test from https://bugs.eclipse.org/bugs/show_bug.cgi?id=526997
	public void testReleaseOption16() {
		runNegativeModuleTest(
			new String[] {
				"foo/Module.java",
				"package foo;\n" +
				"public class Module {}\n",
				"bar/X.java",
				"""
					package bar;
					import foo.*;
					public class X {\s
						public Module getModule(String name) {
							return null;
						}
					}"""
	        },
			" -source 9 \"" + OUTPUT_DIR +  File.separator + "foo" + File.separator + "Module.java\" " +
			"\"" +  OUTPUT_DIR +  File.separator + "bar" + File.separator + "X.java\" ",
	        "",
	        """
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/bar/X.java (at line 4)
					public Module getModule(String name) {
					       ^^^^^^
				The type Module is ambiguous
				----------
				1 problem (1 error)
				""",
	        true,
	        /*not tested with javac*/"");
	}
	public void testReleaseOption17() {
		runNegativeModuleTest(
			new String[] {
				"foo/Module.java",
				"package foo;\n" +
				"public class Module {}\n",
				"foo/X.java",
				"""
					package foo;
					public class X {\s
						public Module getModule(String name) {
							return null;
						}
					}"""
	        },
			" --release 60 \"" + OUTPUT_DIR +  File.separator + "foo" + File.separator + "Module.java\" " +
			"\"" +  OUTPUT_DIR +  File.separator + "foo" + File.separator + "X.java\" ",
	        "",
	        "release version 60 is not supported\n",
	        true,
	        /*not tested with javac*/"");
	}
	public void testReleaseOption18() {
		runNegativeModuleTest(
			new String[] {
				"X.java",
				"""
					/** */
					public class X {
					}""",
				},
			" --release 6 -1.8 \"" + OUTPUT_DIR +  File.separator + "foo" + File.separator + "Module.java\" " +
			"\"" +  OUTPUT_DIR +  File.separator + "foo" + File.separator + "X.java\" ",
	        "",
    		"option 1.8 is not supported when --release is used\n",
	        true,
	        /*not tested with javac*/"");
	}
	public void testReleaseOption19() {
		runNegativeModuleTest(
			new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
			},
			" -9 --release 9 \"" + OUTPUT_DIR +  File.separator + "foo" + File.separator + "Module.java\" " +
			"\"" +  OUTPUT_DIR +  File.separator + "foo" + File.separator + "X.java\" ",
	        "",
    		"option 9 is not supported when --release is used\n",
	        true,
	        /*not tested with javac*/"");
	}
	public void testReleaseOption20() throws Exception {
		if (!isJRE12Plus || isJRE20Plus) return;
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import java.io.*;
						
						public class X {
							public static void main(String[] args) {
								String str = Integer.toUnsignedString(1, 1);
							}
						}""",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 7 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     """
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
					String str = Integer.toUnsignedString(1, 1);
					                     ^^^^^^^^^^^^^^^^
				The method toUnsignedString(int, int) is undefined for the type Integer
				----------
				1 problem (1 error)
				""",
		     true);
	}
	public void testReleaseOption21() throws Exception {
		if (!isJRE12Plus) return;
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						public class X {
							public static void main(String[] args) {
								Integer.toUnsignedString(1, 1);
							}
						}""",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		     + " --release 8 -d \"" + OUTPUT_DIR + "\"",
		     "",
    		 "",
		     true);
	}
	public void testReleaseOption22() {
		if (isJRE11Plus || isJRE12Plus) return;
		runConformTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					public class X {
						public static void main(String[] args) {
						}
					}""",
				"module-info.java",
				"""
					module mod.one {\s
						requires java.base;
						requires java.xml.ws.annotation;
					}"""
	        },
			" --limit-modules java.base,java.xml.ws.annotation " +
			" --release 10 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        """
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/module-info.java (at line 3)
					requires java.xml.ws.annotation;
					         ^^^^^^^^^^^^^^^^^^^^^^
				The module java.xml.ws.annotation has been deprecated since version 9 and marked for removal
				----------
				1 problem (1 warning)
				""",
	        true);
	}
	public void testReleaseOption23() {
		if (!isJRE11Plus) return;
		runNegativeTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					public class X {
						public static void main(String[] args) {
						}
					}""",
				"module-info.java",
				"""
					module mod.one {\s
						requires java.xml.ws.annotation;
					}"""
	        },
			" --limit-modules java.base,java.xml.ws.annotation " +
			" --release 11 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "invalid module name: java.xml.ws.annotation\n",
	        true);
	}
	public void testReleaseOption24() {
		if (!isJRE11Plus) return;
		runNegativeTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					public class X {
						public static void main(String[] args) {
						}
					}""",
				"module-info.java",
				"""
					module mod.one {\s
						requires java.xml.ws.annotation;
					}"""
	        },
			" --limit-modules java.base,java.xml.ws.annotation " +
			" --release 12 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
	        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
	        "",
	        "invalid module name: java.xml.ws.annotation\n",
	        true);
	}
	public void testLimitModules1() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.x {\s
								exports pm;
								requires java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"""
							package pm;
							public class C1 {
							}
							""");


		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --limit-modules java.base")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runNegativeModuleTest(files, buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.x/module-info.java (at line 3)
						requires java.sql;
						         ^^^^^^^^
					java.sql cannot be resolved to a module
					----------
					1 problem (1 error)
					""",
				false,
				"module not found");
	}
	public void testLimitModules2() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.x {\s
								exports pm;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"""
							package pm;
							import java.sql.Connection;
							public class C1 {
							}
							""");


		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --limit-modules java.base")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runNegativeModuleTest(files, buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.x/pm/C1.java (at line 2)
						import java.sql.Connection;
						       ^^^^^^^^
					The import java.sql cannot be resolved
					----------
					1 problem (1 error)
					""",
				false,
				"is not visible");
	}
	public void testLimitModules3() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.x {\s
								exports pm;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pm", "C1.java",
						"""
							package pm;
							public class C1 {
							}
							""");


		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --limit-modules java.sql")
			.append(" --module-source-path " + "\"" + directory + "\"");
		runConformModuleTest(files, buffer,
				"",
				"",
				false);
	}
	public void testLimitModules4() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.three {\s
								requires mod.one;
								requires mod.two;
							}""");


		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
			.append(" -9 ")
			.append(" --module-path \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir.getAbsolutePath())
			.append("\" ")
			.append(" --limit-modules mod.one,mod.two ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ");
		runConformModuleTest(files, buffer,
				"",
				"",
				false);
	}
	public void testLimitModules5() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.three {\s
								requires mod.one;
								requires mod.two;
							}""");


		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
			.append(" -9 ")
			.append(" --module-path \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir.getAbsolutePath())
			.append("\" ")
			.append(" --limit-modules mod.one ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ");
		runNegativeModuleTest(files, buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 3)
						requires mod.two;
						         ^^^^^^^
					mod.two cannot be resolved to a module
					----------
					1 problem (1 error)
					""",
				false,
				"");
	}
	public void testBug519600() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = srcDir + File.separator + "test";
		Runner runner = new Runner();
		runner.createFile(moduleLoc, "module-info.java",
						"module test {}");
		runner.createFile(moduleLoc + File.separator + "test", "Thing.java",
				"""
					package test;
					import java.util.Comparator;
					import java.util.Iterator;
					public abstract class Thing implements Iterator<Object> {
					    void breaking() {
					        remove(); // allowed (good)
					        Iterator.super.remove(); // not 1.7-compliant (must be an error)
					        Comparator.naturalOrder(); // not 1.7-compliant (bad error message)
					    }
					}
					""");

		runner.commandLine.append("-d " + outDir )
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ");
		runner.javacVersionOptions = "-Xlint:-options"; // -source 9 already provided
		runner.runConformModuleTest();
	}
	public void testBug508889_001() throws Exception {
		this.runConformTest(
			new String[] {
				"module-info.java",
				"module mymodule {\n" +
				"}",
			},
			"\"" + OUTPUT_DIR +  File.separator + "module-info.java\""
			+ " -9 -source 9 -target 9 -d \"" + OUTPUT_DIR + "\"",
			"",
			"",
			true);
		String expectedOutput = """
			// Compiled from module-info.java (version 9 : 53.0, no super bit)
			 module mymodule  {
			  // Version:\s
			
			  requires java.base;
			
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "module-info.class", "module-info", expectedOutput);
	}
	public void testBug508889_002() throws Exception {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
							    exports pack1;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pack1", "X11.java",
						"""
							package pack1;
							public class X11 {
							}""");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);
		String expectedOutput = """
			// Compiled from module-info.java (version 9 : 53.0, no super bit)
			 module mod.one  {
			  // Version:\s
			
			  requires java.base;
			
			  exports pack1;
			
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + out + File.separator + "module-info.class", "module-info", expectedOutput);
	}
	public void testBug508889_003() throws Exception {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
							    exports pack1;
							    exports pack2 to second;
							    opens pack3;
							    opens pack4 to third;
							    uses pack5.X51;
							    provides pack1.I11 with pack1.X11;
							    requires transitive java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pack1", "I11.java",
				"""
					package pack1;
					public interface I11 {
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pack1", "X11.java",
						"""
							package pack1;
							public class X11 implements I11{
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pack2", "X21.java",
				"""
					package pack2;
					public class X21 {
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pack3", "X31.java",
				"""
					package pack3;
					public class X31 {
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pack4", "X41.java",
				"""
					package pack4;
					public class X41 {
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "pack5", "X51.java",
				"""
					package pack5;
					public class X51 {
					}""");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ");
		files.forEach(name -> buffer.append(" \"" + name + "\""));
		runConformTest(new String[0],
				buffer.toString(),
				"",
				"",
				false);
		String expectedOutput = """
			// Compiled from module-info.java (version 9 : 53.0, no super bit)
			 module mod.one  {
			  // Version:\s
			
			  requires transitive java.sql;
			  requires java.base;
			
			  exports pack1;
			  exports pack2 to second;
			
			  opens pack3;
			  opens pack4 to third;
			
			  uses pack5.X51
			
			  provides pack1.I11 with pack1.X11;
			
			}""";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + out + File.separator + "module-info.class", "module-info", expectedOutput);
	}
	public void testBug520858() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = srcDir + File.separator + "test";
		Runner runner = new Runner();
		runner.createFile(moduleLoc, "module-info.java",
						"""
							module test {
								requires org.astro;
							}""");
		runner.createFile(moduleLoc + File.separator + "p", "Test.java",
			"""
				package p;
				import org.astro.World;
				public class Test {
					World w = null;
				}""");
		moduleLoc = srcDir + File.separator + "org.astro";
		runner.createFile(moduleLoc, "module-info.java",
				"""
					module org.astro {
						exports org.astro;
					}""");
		runner.createFile(moduleLoc + File.separator + "org" + File.separator + "astro", "World.java",
			"""
				package org.astro;
				public interface World {
					public static String name() {
						return "";
					}
				}""");
		runner.commandLine.append("-d " + outDir )
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ");
		runner.javacVersionOptions = " -Xlint:-options";
		runner.runConformModuleTest();
	}
	public void testBug520858a() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = srcDir + File.separator + "test";
		Runner runner = new Runner();
		// not adding some files to the command line
		writeFile(moduleLoc, "module-info.java",
						"""
							module test {
								requires org.astro;
							}""");
		// the only file added:
		runner.createFile(moduleLoc + File.separator + "p", "Test.java",
			"""
				package p;
				import org.astro.World;
				public class Test {
					World w = null;
				}""");
		moduleLoc = srcDir + File.separator + "org.astro";
		writeFile(moduleLoc, "module-info.java",
				"""
					module org.astro {
						exports org.astro;
					}""");
		writeFile(moduleLoc + File.separator + "org" + File.separator + "astro", "World.java",
			"""
				package org.astro;
				public interface World {
					public static String name() {
						return "";
					}
				}""");
		runner.commandLine.append("-d " + outDir )
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ");
		runner.javacVersionOptions = " -Xlint:-options";
		runner.runConformModuleTest();
	}
	public void testBug520858b() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = srcDir + File.separator + "test";
		Runner runner = new Runner();
		runner.createFile(moduleLoc, "module-info.java",
						"""
							module test {
								requires org.astro;
							}""");
		runner.createFile(moduleLoc + File.separator + "p", "Test.java",
			"""
				package p;
				import org.astro.World;
				public class Test {
					World w = null;
				}""");
		moduleLoc = srcDir + File.separator + "org.astro";
		// not adding this file to the command line (intentional?):
		writeFile(moduleLoc, "module-info.java",
				"""
					module org.astro {
						exports org.astro;
					}""");
		runner.createFile(moduleLoc + File.separator + "org" + File.separator + "astro", "World.java",
			"""
				package org.astro;
				public interface World {
					public static String name() {
						return "";
					}
				}""");
		runner.commandLine.append("-d " + outDir )
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ");
		runner.javacVersionOptions = " -Xlint:-options";
		runner.runConformModuleTest();
	}
	public void testBug520858c() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = srcDir + File.separator + "test";
		List<String> files = new ArrayList<>();

		writeFileCollecting(files, moduleLoc + File.separator + "p", "Test.java",
			"""
				package p;
				import org.astro.World;
				public class Test {
					World w = null;
				}""");
		moduleLoc = srcDir + File.separator + "org.astro";
		writeFileCollecting(files, moduleLoc, "module-info.java",
				"""
					module org.astro {
						exports org.astro;
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "org" + File.separator + "astro", "World.java",
			"""
				package org.astro;
				public interface World {
					public static String name() {
						return "";
					}
				}""");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ");
		runNegativeModuleTest(files, buffer,
				"",
				"\'---OUTPUT_DIR_PLACEHOLDER---/src/test/p/Test.java\' does not belong to a module on the module source path\n",
				false,
				"not in a module on the module source path");
	}
	public void testBug520858d() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = srcDir + File.separator + "test";
		List<String> files = new ArrayList<>();

		writeFileCollecting(files, moduleLoc + File.separator + "p", "Test.java",
			"""
				package p;
				import org.astro.World;
				public class Test {
					World w = null;
				}""");
		moduleLoc = srcDir + File.separator + "org.astro";
		writeFileCollecting(files, moduleLoc, "module-info.java",
				"""
					module org.astro {
						exports org.astro;
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "org" + File.separator + "astro", "World.java",
			"""
				package org.astro;
				public interface World {
					public static String name() {
						return "";
					}
				}""");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ")
			.append(srcDir + File.separator + "org.astro" + File.separator + "org" + File.separator + "astro" + File.separator + "World.java ")
			.append(srcDir + File.separator + "test" + File.separator + "p" + File.separator + "Test.java");
		runNegativeModuleTest(Collections.emptyList(), buffer,
			"",
			"\'---OUTPUT_DIR_PLACEHOLDER---/src/test/p/Test.java\' does not belong to a module on the module source path\n",
			false,
			"not in a module on the module source path");
	}
	public void testBug520858e() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = srcDir + File.separator + "test";
		List<String> files = new ArrayList<>();

		writeFileCollecting(files, moduleLoc + File.separator + "p", "Test.java",
			"""
				package p;
				import org.astro.World;
				public class Test {
					World w = null;
				}""");
		moduleLoc = srcDir + File.separator + "org.astro";
		writeFileCollecting(files, moduleLoc, "module-info.java",
				"""
					module org.astro {
						exports org.astro;
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "org" + File.separator + "astro", "World.java",
			"""
				package org.astro;
				public interface World {
					public static String name() {
						return "";
					}
				}""");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
			.append(" -source 9 ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ")
			.append(srcDir + File.separator + "org.astro" + File.separator + "org" + File.separator + "astro" + File.separator + "World.java ")
			.append(srcDir + File.separator + "org.astro" + File.separator + "module-info.java ")
			.append(srcDir + File.separator + "test" + File.separator + "p" + File.separator + "Test.java");
		runNegativeModuleTest(Collections.emptyList(), buffer,
			"",
			"\'---OUTPUT_DIR_PLACEHOLDER---/src/test/p/Test.java\' does not belong to a module on the module source path\n",
			false,
			"not in a module on the module source path");
	}
	public void testBug530575() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		File srcDir = new File(directory);

		String moduleLoc = directory + File.separator + "mod.x";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.x {\s
								exports px;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "px", "C1.java",
						"""
							package px;
							public class C1 {
							}
							""");

		moduleLoc = directory + File.separator + "mod.y";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.y {\s
								exports py;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "py", "C1.java",
						"""
							package py;
							public class C1 {
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");
		for (String fileName : files)
			buffer.append(" \"").append(fileName).append("\"");
		runConformTest(new String[0],
				buffer.toString(),
				"",
				"",
				false);
		Util.flushDirectoryContent(srcDir);
		files.clear();
		writeFileCollecting(files, directory, "module-info.java",
				"""
					module test {\s
						requires mod.x;
						requires mod.y;
					}""");
		writeFileCollecting(files, directory + File.separator + "p", "X.java",
						"""
							package p;
							public class X extends px.C1 {\s
								py.C1 c = null;
							}""");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + File.separator + "mod.x" + File.pathSeparator + OUTPUT_DIR + File.separator + out + File.separator + "mod.y" + "\"");
		runConformModuleTest(files,
				buffer,
				"",
				"",
				false,
				OUTPUT_DIR + "javac");
	}
	/*
	 * Test that when module-info is not included in the command line, the class is still
	 * generated inside the module's sub folder.
	 */
	public void testBug533411() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								requires java.sql;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "Test.java",
				"""
					package p;
					public class Test {
						java.sql.Connection conn = null;
					}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "p" + File.separator + "Test.java");

		Set<String> classFiles = runConformModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"",
				false);
		String fileName = OUTPUT_DIR + File.separator + out + File.separator + "mod.one" + File.separator + "module-info.class";
		assertClassFile("Missing modul-info.class: " + fileName, fileName, classFiles);
	}
	public void test_npe_bug535107() {
		runConformModuleTest(
				new String[] {
					"p/X.java",
					"""
						package p;
						import java.lang.annotation.*;
						@Target(ElementType.MODULE)
						public @interface X {
							ElementType value();
						}""",
					"module-info.java",
			  		"""
						import java.lang.annotation.*;
						@p.X(ElementType.MODULE)
						module mod.one {\s
						}"""
		        },
				" -9 \"" + OUTPUT_DIR +  File.separator + "module-info.java\" "
		        + "\"" + OUTPUT_DIR +  File.separator + "p/X.java\"",
		        "",
		        "",
		        true);
	}
	public void testBug540067a() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
							 exports p;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
				"""
					package p;
					public class X {
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "Test.java",
				"/*nothing in it */");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "X.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "Test.java");

		runConformModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"",
				false);
	}
	public void testBug540067b() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
							 exports p;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
				"""
					package p;
					public class X {
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "Test.java",
				"package p.q;");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "X.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "Test.java");

		runConformModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"",
				false);
	}
	public void testBug540067c() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
							 exports p;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
				"""
					package p;
					public class X {
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "Test.java",
				"package p.q;\n"
				+ "class Test {}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "X.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "Test.java");

		runConformModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"",
				false);
	}
	public void testBug540067d() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
							 exports p;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
				"""
					package p;
					public class X {
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "Test.java",
				"class Test {}");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "X.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "Test.java");

		runNegativeModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/q/Test.java (at line 1)
						class Test {}
						^
					Must declare a named package because this compilation unit is associated to the named module \'mod.one\'
					----------
					1 problem (1 error)
					""",
				false,
				"unnamed package is not allowed in named modules");
	}
	public void testBug540067e() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
							 exports p;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
				"""
					package p;
					public class X {
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q", "Test.java",
				"import java.lang.*;");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-unused")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(moduleLoc + File.separator + "module-info.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "X.java ")
			.append(moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "Test.java");

		runNegativeModuleTest(
				new String[0],
				buffer.toString(),
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.one/p/q/Test.java (at line 1)
						import java.lang.*;
						^
					Must declare a named package because this compilation unit is associated to the named module \'mod.one\'
					----------
					1 problem (1 error)
					""",
				false,
				"unnamed package is not allowed in named modules");
	}
	public void testBug548195() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
							 exports p;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
				"""
					package p;
					public class X {
					}""");

		StringBuilder buffer = new StringBuilder();
		String binDir = OUTPUT_DIR + File.separator + out;
		buffer.append("-d " + binDir )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-unused")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(" --module-version 47.11 ");
		String outText = isJRE9Plus ? "" : "Could not invoke method java.lang.module.ModuleDescriptor.Version.parse(), cannot validate module version.\n";
		runConformModuleTest(files, buffer, outText, "", false);

		IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(binDir + File.separator + "mod.one" + File.separator + "module-info.class", IClassFileReader.CLASSFILE_ATTRIBUTES);
		assertNotNull("Error reading module-info.class", cfr);
		IClassFileAttribute[] attrs = cfr.getAttributes();
		for (IClassFileAttribute attr : attrs) {
			char[] name = attr.getAttributeName();
			if (CharOperation.equals(name, AttributeNamesConstants.ModuleName)) {
				IModuleAttribute modAttr = (IModuleAttribute) attr;
				String expectedVersion = isJRE9Plus ? "47.11" : "";
				assertEquals("version in attribute", expectedVersion, new String(modAttr.getModuleVersionValue()));
				return;
			}
		}
		fail("module attribute not found");
	}
	public void testBug548195fail() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
							 exports p;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "X.java",
				"""
					package p;
					public class X {
					}""");

		StringBuilder buffer = new StringBuilder();
		String binDir = OUTPUT_DIR + File.separator + out;
		buffer.append("-d " + binDir )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" -warn:-unused")
			.append(" --module-source-path " + "\"" + directory + "\" ")
			.append(" --module-version fourtyseven.11 ");
		if (isJRE9Plus) {
			runNegativeModuleTest(files, buffer, "", "fourtyseven.11: Version string does not start with a number\n", false, "bad value");
		} else {
			runConformModuleTest(files, buffer, "Could not invoke method java.lang.module.ModuleDescriptor.Version.parse(), cannot validate module version.\n", "", false);
		}
	}
	public void testPackageTypeConflict2() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";

		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports p1.p2;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p1" + File.separator + "p2", "t3.java",
						"""
							package p1.p2;
							public class t3 {
							}
							""");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								exports p1.p2.t3;
								requires mod.one;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p1" + File.separator + "p2" + File.separator + "t3", "t4.java",
						"""
							package p1.p2.t3;
							public class t4 {
							}
							""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\"");

		runNegativeModuleTest(
				files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p1/p2/t3/t4.java (at line 1)
						package p1.p2.t3;
						        ^^^^^^^^
					The package p1.p2.t3 collides with a type
					----------
					1 problem (1 error)
					""",
				false,
				"package p1.p2.t3 clashes with class of same name");
	}
	public void testBug550178() throws Exception {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		createReusableModules(srcDir, outDir, modDir);
		String moduleLoc = srcDir + File.separator + "mod.three";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.three {\s
								exports pkg.invalid;
							}""");


		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
			.append(" -9 ")
			.append(" --module-path \"")
			.append(Util.getJavaClassLibsAsString())
			.append(modDir.getAbsolutePath())
			.append("\" ")
			.append(" --module-source-path " + "\"" + srcDir + "\" ");
		runNegativeModuleTest(files, buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.three/module-info.java (at line 2)
						exports pkg.invalid;
						        ^^^^^^^^^^^
					The package pkg.invalid does not exist or is empty
					----------
					1 problem (1 error)
					""",
				false,
				"");
	}
	public void testRelease565930_1() throws Exception {
		this.runConformTest(
				new String[] {
					"Dummy.java",
					"""
						public class Dummy {
							boolean b = new String("a").contains("b");
						}""",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "Dummy.java\""
		     + " --release 9 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     "",
		     true);
	}
	public void testRelease565930_2() throws Exception {
		this.runConformTest(
				new String[] {
					"Main.java",
					"""
						public final class Main<T extends Object> {
						    public void test() {
						    	final ClassLoader classLoader = this.getClass().getClassLoader();
						    }\s
						}""",
				},
		     "\"" + OUTPUT_DIR +  File.separator + "Main.java\""
		     + " --release 9 -d \"" + OUTPUT_DIR + "\"",
		     "",
		     """
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Main.java (at line 3)
					final ClassLoader classLoader = this.getClass().getClassLoader();
					                  ^^^^^^^^^^^
				The value of the local variable classLoader is not used
				----------
				1 problem (1 warning)
				""",
		     true);
	}
	public void testBug571363() throws Exception {
		if (!isJRE12Plus) return;
		this.runConformTest(
			new String[] {
				"A.java",
				"""
					public final class A {
					    org.w3c.dom.Element list;
					}""",
			},
	     "\"" + OUTPUT_DIR +  File.separator + "A.java\""
	     + " -classpath " + "\"" + this.getCompilerTestsPluginDirectoryPath() + File.separator + "workspace" + File.separator + "Test571363.jar\""
	     + " --release 11 -d \"" + OUTPUT_DIR + "\"",
	     "",
	     "",
	     true);
	}
	public void testBug574097() {
		Util.flushDirectoryContent(new File(OUTPUT_DIR));
		String outDir = OUTPUT_DIR + File.separator + "bin";
		String srcDir = OUTPUT_DIR + File.separator + "src";
		File modDir = new File(OUTPUT_DIR + File.separator + "mod");
		String moduleLoc = srcDir + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.one {\s
								exports p;
								requires transitive java.compiler;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p", "TestProcessor.java",
						"""
							package p;
							import java.util.Set;
							import javax.annotation.processing.AbstractProcessor;
							import javax.annotation.processing.RoundEnvironment;
							import javax.lang.model.element.TypeElement;
							public class TestProcessor extends AbstractProcessor {
								@Override
								public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
									return false;
								}
							}""");

		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" --module-path \"")
		.append(Util.getJavaClassLibsAsString())
		.append("\" ")
		.append(" --module-source-path " + "\"" + srcDir + "\"");
		for (String fileName : files)
			buffer.append(" \"").append(fileName).append("\"");

		runConformTest(new String[]{},
			buffer.toString(),
			"",
			"",
			false);
		String jarName = modDir + File.separator + "mod.one.jar";
		try {
			Util.zip(new File(outDir + File.separator + "mod.one"),
								jarName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (!modDir.exists()) {
			if (!modDir.mkdirs()) {
				fail("Coult not create folder " + modDir);
			}
		}
		Util.flushDirectoryContent(new File(srcDir));
		files = new ArrayList<>();
		moduleLoc = srcDir + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc, "module-info.java",
						"""
							module mod.two {\s
								exports q;
								requires java.base;
								requires mod.one;
							}""");
		writeFileCollecting(files, moduleLoc + File.separator + "q", "A.java",
						"""
							package q;
							public class A {
							   p.TestProcessor prc = null;
							}""");
		buffer = new StringBuilder();
		buffer.append("-d " + outDir )
		.append(" -9 ")
		.append(" --module-path \"")
		.append(Util.getJavaClassLibsAsString())
		.append("\" ")
		.append(" --module-source-path " + "\"" + srcDir + "\"")
		.append(" --processor-module-path " + "\"" + jarName + "\"");
		for (String name : files)
			buffer.append(" \"").append(name).append("\"");

		runNegativeTest(new String[]{},
			buffer.toString(),
			"",
			"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 4)
					requires mod.one;
					         ^^^^^^^
				mod.one cannot be resolved to a module
				----------
				----------
				2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/q/A.java (at line 3)
					p.TestProcessor prc = null;
					^
				p cannot be resolved to a type
				----------
				2 problems (2 errors)
				""",
			false);
	}
	/*
	 * Test that reference to a binary package that is exported in a module
	 * but doesn't have a corresponding resource or .class files is reported.
	 */
	public void testBug522472a() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		File srcDir = new File(directory);
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"""
					module mod.one {\s
						exports x.y.z;
						exports a.b.c;
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z",
				"X.java",
				"package x.y.z;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "a" + File.separator + "b" + File.separator + "c",
				"A.java",
				"package a.b.c;\n" +
				"public class A {}");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);

		Util.flushDirectoryContent(srcDir);
		files.clear();
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"""
					module mod.two {\s
						requires mod.one;
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "r",
				"Main.java",
				"""
					package p.q.r;
					import a.b.c.*;
					import x.y.z.*;
					public class Main {\
					}""");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ");
		runConformModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p/q/r/Main.java (at line 2)
						import a.b.c.*;
						       ^^^^^
					The import a.b.c is never used
					----------
					2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p/q/r/Main.java (at line 3)
						import x.y.z.*;
						       ^^^^^
					The import x.y.z is never used
					----------
					2 problems (2 warnings)
					""",
				false,
				"package conflict");
	}
	/*
	 * Same as above test case, but two binary modules export the package, without any .class files
	 */
	public void testBug522472b() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		File srcDir = new File(directory);
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"""
					module mod.one {\s
						exports x.y.z;
						exports a.b.c;
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z",
				"X.java",
				"package x.y.z;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "a" + File.separator + "b" + File.separator + "c",
				"A.java",
				"package a.b.c;\n" +
				"public class A {}");

		moduleLoc = directory + File.separator + "mod.one.a";
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"""
					module mod.one.a {\s
						exports x.y.z;
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z",
				"X.java",
				"package x.y.z;\n");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ");

		runConformModuleTest(files,
				buffer,
				"",
				"",
				false);

		Util.flushDirectoryContent(srcDir);
		files.clear();
		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"""
					module mod.two {\s
						requires mod.one;
						requires mod.one.a;
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "r",
				"Main.java",
				"""
					package p.q.r;
					import a.b.c.*;
					import x.y.z.*;
					public class Main {\
					}""");
		buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-path " + "\"" + OUTPUT_DIR + File.separator + out + "\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ");
		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 2)
						requires mod.one;
						^^^^^^^^^^^^^^^^
					The package x.y.z is accessible from more than one module: mod.one, mod.one.a
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 3)
						requires mod.one.a;
						^^^^^^^^^^^^^^^^^^
					The package x.y.z is accessible from more than one module: mod.one, mod.one.a
					----------
					----------
					3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p/q/r/Main.java (at line 3)
						import x.y.z.*;
						       ^^^^^
					The import x.y.z cannot be resolved
					----------
					3 problems (3 errors)
					""",
				false,
				"package conflict");
	}
	public void testBug522472d() {
		File outputDirectory = new File(OUTPUT_DIR);
		Util.flushDirectoryContent(outputDirectory);
		String out = "bin";
		String directory = OUTPUT_DIR + File.separator + "src";
		String moduleLoc = directory + File.separator + "mod.one";
		List<String> files = new ArrayList<>();
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"""
					module mod.one {\s
						exports x.y.z;
						exports a.b.c;
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z",
				"X.java",
				"package x.y.z;\n");
		writeFileCollecting(files, moduleLoc + File.separator + "a" + File.separator + "b" + File.separator + "c",
				"A.java",
				"package a.b.c;\n" +
				"public class A {}");

		moduleLoc = directory + File.separator + "mod.one.a";
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"""
					module mod.one.a {\s
						exports x.y.z;
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "x" + File.separator + "y" + File.separator + "z",
				"X.java",
				"package x.y.z;\n");
		StringBuilder buffer = new StringBuilder();
		buffer.append("-d " + OUTPUT_DIR + File.separator + out )
			.append(" -9 ")
			.append(" -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append("\" ")
			.append(" --module-source-path " + "\"" + directory + "\" ");

		moduleLoc = directory + File.separator + "mod.two";
		writeFileCollecting(files, moduleLoc,
				"module-info.java",
				"""
					module mod.two {\s
						requires mod.one;
						requires mod.one.a;
					}""");
		writeFileCollecting(files, moduleLoc + File.separator + "p" + File.separator + "q" + File.separator + "r",
				"Main.java",
				"""
					package p.q.r;
					import a.b.c.*;
					import x.y.z.*;
					public class Main {\
					}""");

		runNegativeModuleTest(files,
				buffer,
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 2)
						requires mod.one;
						^^^^^^^^^^^^^^^^
					The package x.y.z is accessible from more than one module: mod.one, mod.one.a
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/module-info.java (at line 3)
						requires mod.one.a;
						^^^^^^^^^^^^^^^^^^
					The package x.y.z is accessible from more than one module: mod.one, mod.one.a
					----------
					----------
					3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/mod.two/p/q/r/Main.java (at line 3)
						import x.y.z.*;
						       ^^^^^
					The package x.y.z is accessible from more than one module: mod.one, mod.one.a
					----------
					3 problems (3 errors)
					""",
				false,
				"conflict");
	}
}
