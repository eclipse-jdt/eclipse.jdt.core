/*******************************************************************************
 * Copyright (c) 2000, 2022 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Salesforce - copied and adapted from org.eclipse.jdt.core.tests.compiler.regression.BatchCompilerTest
 *******************************************************************************/
package org.eclipse.jdt.apt.tests;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.eclipse.jdt.core.tests.compiler.regression.AbstractBatchCompilerTest;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

import junit.framework.Test;
import junit.framework.TestSuite;

@SuppressWarnings("restriction")
public class AnnotationProcessingCompilerToolTest extends AbstractBatchCompilerTest {

	public static record JavacArguments(List<Path> classPath, Path classOutput, Path nativeHeaderOutput, List<Path> sourcePath, List<Path> sourceFiles, Path system, List<Path> bootClassPath, List<Path> processorPath, Path sourceOutput) {};

	public static Test suite() {
		return new TestSuite(AnnotationProcessingCompilerToolTest.class);
	}

	private File _extJar; // external annotation jar

	public AnnotationProcessingCompilerToolTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_extJar = TestUtil.createAndAddExternalAnnotationJar(null /* no project */);
	}

	public void test_github844() throws IOException {
		// @formatter:off
		runTest(
			true /* shouldCompileOK */,
			new String [] { /* sourceFiles */
				"X.java",
				"package p1;\n"
				+ "\n import org.eclipse.jdt.apt.tests.external.annotations.batch.*;"
				+ "\n import p1.gen.*;"
				+ "\n@BatchGen\n"
				+ "public class X {"
				+ "   Class0 clazz0;\n"
				+ "   Class1 clazz1;\n"
				+ "}\n",
			},
			null /* standardJavaFileManager */,
			Arrays.asList(
					"-d", OUTPUT_DIR,
			        "-source", "1.6",
			        "-g", "-preserveAllLocals",
			        "-cp",  OUTPUT_DIR  + File.pathSeparator + _extJar.getAbsolutePath() ,
			        "-s", OUTPUT_DIR  +  File.separator + "src-gen",
			        "-processorpath", _extJar.getAbsolutePath(),
			        "-XprintProcessorInfo", "-XprintRounds",
			        "-proceedOnError"
					) /* options */,
			new String[] { /* compileFileNames */
				"X.java"
			},
			"Round 1:\n"
			+ "	input files: {p1.X}\n"
			+ "	annotations: [org.eclipse.jdt.apt.tests.external.annotations.batch.BatchGen]\n"
			+ "	last round: false\n"
			+ "Discovered processor service org.eclipse.jdt.apt.tests.external.annotations.batch.BatchGenProcessor\n"
			+ "  supporting [org.eclipse.jdt.apt.tests.external.annotations.batch.BatchGen]\n"
			+ "  in jar:"  + Path.of(_extJar.getCanonicalPath()).toUri().toURL().toString() +"!/\n"
			+ "Processor org.eclipse.jdt.apt.tests.external.annotations.batch.BatchGenProcessor matches [org.eclipse.jdt.apt.tests.external.annotations.batch.BatchGen] and returns true\n"
			+ "Round 2:\n"
			+ "	input files: {p1.gen.Class1,p1.gen.Class2,p1.Class0}\n"
			+ "	annotations: []\n"
			+ "	last round: false\n"
			+ "Processor org.eclipse.jdt.apt.tests.external.annotations.batch.BatchGenProcessor matches [] and returns false\n"
			+ "Round 3:\n"
			+ "	input files: {}\n"
			+ "	annotations: []\n"
			+ "	last round: true\n" /* expectedOutOutputString */,
			"" /* expectedErrOutputString */,
			true /* shouldFlushOutputDirectory */ );
		// @formatter:on
	}

	protected static class CompilerInvocationTestsArguments {
		StandardJavaFileManager standardJavaFileManager;
		List<String> options;
		String[] fileNames;
		public CompilerInvocationTestsArguments(
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
	static class CompilerInvocationDiagnosticListener implements DiagnosticListener<JavaFileObject> {
		public static final int NONE = 0;
		public static final int ERROR = 1;
		public static final int INFO = 2;
		public static final int WARNING = 4;

		private PrintWriter err;
		public int kind;

		public CompilerInvocationDiagnosticListener(PrintWriter err) {
			this.err = err;
			this.kind = NONE;
		}
		@Override
		public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
			err.println(diagnostic.getMessage(Locale.getDefault()));
			if (this.kind == NONE) {
				switch(diagnostic.getKind()) {
					case ERROR :
						this.kind = ERROR;
						break;
					case WARNING :
					case MANDATORY_WARNING :
						this.kind = WARNING;
						break;
					case NOTE :
					case OTHER :
						this.kind = INFO;
				}
			}
		}
	}
	static EclipseCompiler COMPILER = new EclipseCompiler();
	static JavaCompiler JAVAC_COMPILER = ToolProvider.getSystemJavaCompiler();
	@Override
	protected boolean invokeCompiler(
			PrintWriter out,
			PrintWriter err,
			Object extraArguments,
			TestCompilationProgress compilationProgress) {
		CompilerInvocationTestsArguments arguments = (CompilerInvocationTestsArguments) extraArguments;
		StandardJavaFileManager manager = arguments.standardJavaFileManager;
		boolean ownsManager = false;
		if (manager == null) {
			manager = COMPILER.getStandardFileManager(null, null, null); // will pick defaults up
			ownsManager = true;
		}
		try {
			List<File> files = new ArrayList<File>();
			String[] fileNames = arguments.fileNames;
			for (int i = 0, l = fileNames.length; i < l; i++) {
				if (fileNames[i].startsWith(OUTPUT_DIR)) {
					files.add(new File(fileNames[i]));
				} else {
					files.add(new File(OUTPUT_DIR + File.separator + fileNames[i]));
				}
			}
			CompilationTask task = COMPILER.getTask(out, arguments.standardJavaFileManager /* carry the null over */, new CompilerInvocationDiagnosticListener(err), arguments.options, null, manager.getJavaFileObjectsFromFiles(files));
			return task.call();
		} finally {
			try {
				if (ownsManager)
					manager.close();
			} catch (IOException e) {
				// nop
			}
		}
	}

	protected void runTest(
			boolean shouldCompileOK,
			String[] sourceFiles,
			StandardJavaFileManager standardJavaFileManager,
			List<String> options,
			String[] compileFileNames,
			String expectedOutOutputString,
			String expectedErrOutputString,
			boolean shouldFlushOutputDirectory) {
		super.runTest(
			shouldCompileOK,
			sourceFiles,
			new CompilerInvocationTestsArguments(standardJavaFileManager, options, compileFileNames),
			expectedOutOutputString,
			expectedErrOutputString,
			shouldFlushOutputDirectory,
			null /* progress */);
	}

}
