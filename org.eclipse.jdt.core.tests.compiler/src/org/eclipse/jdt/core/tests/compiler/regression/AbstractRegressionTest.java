/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.core.tests.junit.extension.StopableTestCase;
import org.eclipse.jdt.core.tests.util.*;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.batch.*;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;

import java.io.*;
import java.util.*;
import junit.framework.*;

public abstract class AbstractRegressionTest extends StopableTestCase {
	public static String OUTPUT_DIR = Util.getOutputDirectory() + File.separator + "regression";
	public static String JAVA_CLASS_LIB_PATH = Util.getJavaClassLib();

	protected INameEnvironment javaClassLib;
	protected String[] classpaths;
	protected TestVerifier verifier;
	protected boolean createdVerifier;
public AbstractRegressionTest(String name) {
	super(name);
}
protected CompilationUnit[] compilationUnits(String[] testFiles) {
	int length = (int)(testFiles.length / 2);
	CompilationUnit[] result = new CompilationUnit[length];
	int index = 0;
	for (int i = 0; i < length; i++) {
		result[i] = new CompilationUnit(testFiles[index + 1].toCharArray(), testFiles[index], null);
		index += 2;
	}
	return result;
}
protected INameEnvironment[] getClassLibs(String[] classpaths) {
	String encoding = (String) getCompilerOptions().get(CompilerOptions.OPTION_Encoding);
	if ("".equals(encoding)) encoding = null;
	
	int length = classpaths.length;
	INameEnvironment[] classLibs = new INameEnvironment[length];
	for (int i = 0; i < length; i++) {
		String classpath = classpaths[i];
		if (classpath.equals(JAVA_CLASS_LIB_PATH)) {
			if (this.javaClassLib == null) {
				this.javaClassLib =
					new FileSystem(
						new String[] {classpath}, 
						new String[] {}, // ignore initial file names
						encoding // default encoding
				);
			}
			classLibs[i] = this.javaClassLib;
		} else {
			classLibs[i] =
				new FileSystem(
					new String[] {classpath}, 
					new String[] {}, // ignore initial file names
					encoding // default encoding			
				);
		}
	}
	return classLibs;
}
protected Map getCompilerOptions() {
	Hashtable options = new Hashtable();
	options.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.GENERATE);
	options.put(CompilerOptions.OPTION_LineNumberAttribute, CompilerOptions.GENERATE);
	options.put(CompilerOptions.OPTION_SourceFileAttribute, CompilerOptions.GENERATE);
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_1);
	options.put(CompilerOptions.OPTION_ReportUnreachableCode, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportInvalidImport, CompilerOptions.ERROR);
	options.put(CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportMethodWithConstructorName, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportHiddenCatchBlock, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportAssertIdentifier, CompilerOptions.IGNORE);
	options.put(CompilerOptions.OPTION_ReportStaticAccessReceiver, CompilerOptions.WARNING);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_3);
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_3);
	options.put(CompilerOptions.OPTION_Encoding, "");	
	return options;
}
protected String[] getDefaultClassPaths() {
	return new String[] {JAVA_CLASS_LIB_PATH, OUTPUT_DIR}; 
}
protected IErrorHandlingPolicy getErrorHandlingPolicy() {
	return new IErrorHandlingPolicy() {
		public boolean stopOnFirstError() {
			return false;
		}
		public boolean proceedOnErrors() {
			return true;
		}
	};
}
/*
 * Will consider first the source units passed as arguments, then
 * investigate the classpath: jdklib + output dir 
 */
protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths) {
	this.classpaths = 
		classPaths == null ? 
			this.getDefaultClassPaths() :
			classPaths;
	return 
		new InMemoryNameEnvironment(
			testFiles, 
			this.getClassLibs(this.classpaths)
		);
}
protected IProblemFactory getProblemFactory() {
	return new DefaultProblemFactory(Locale.getDefault());
}
protected Requestor getRequestor(IProblemFactory problemFactory) {
	return new Requestor(problemFactory, OUTPUT_DIR.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR + File.separator, false);
}

protected Requestor getRequestor(IProblemFactory problemFactory, boolean generateOutPut) {
	return new Requestor(problemFactory, OUTPUT_DIR.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR + File.separator, true);
}

protected void runConformTest(String[] testFiles) {
	this.runConformTest(testFiles, null, null, true, null);
}

protected void runConformTest(String[] testFiles, String[] vmArguments) {
	this.runConformTest(testFiles, null, null, true, vmArguments);
}
protected void runConformTest(String[] testFiles, String expectedSuccessOutputString, String[] vmArguments) {
	this.runConformTest(testFiles, expectedSuccessOutputString, null, true, vmArguments);
}

protected void runConformTest(String[] testFiles, String expectedSuccessOutputString) {
	this.runConformTest(testFiles, expectedSuccessOutputString, null, true, null);
}
protected void runConformTest(String[] testFiles, String expectedSuccessOutputString, String[] classLib, boolean shouldFlushOutputDirectory, String[] vmArguments) {
	this.runConformTest( testFiles,  expectedSuccessOutputString, classLib,  shouldFlushOutputDirectory, vmArguments, null);
}

protected void runConformTest(String[] testFiles, String expectedSuccessOutputString, String[] classLib, boolean shouldFlushOutputDirectory, String[] vmArguments, Map customOptions) {

	if (shouldFlushOutputDirectory) Util.flushDirectoryContent(new File(OUTPUT_DIR));
	
	IProblemFactory problemFactory = getProblemFactory();
	LogRequestor requestor = new LogRequestor(problemFactory, OUTPUT_DIR.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR + File.separator);
	
	Map options = getCompilerOptions();
	if (customOptions != null){
		options.putAll(customOptions);
	}
	Compiler batchCompiler =
		new Compiler(
			getNameEnvironment(new String[] {}, classLib),
			getErrorHandlingPolicy(),
			options,
			requestor,
			problemFactory);
	batchCompiler.compile(compilationUnits(testFiles)); // compile all files together
	if (!requestor.hasErrors) {
		String sourceFile = testFiles[0];
		
		// Compute class name by removing ".java" and replacing slashes with dots
		String className = sourceFile.substring(0, sourceFile.length() - 5).replace('/', '.').replace('\\', '.');

		if (vmArguments != null) {
			if (this.verifier != null) {
				this.verifier.shutDown();
			}
			this.verifier = new TestVerifier(false);
			this.createdVerifier = true;
		}		
		boolean passed = this.verifier.verifyClassFiles(sourceFile, className, expectedSuccessOutputString, this.classpaths, null, vmArguments);
		assertTrue(
			this.verifier.failureReason, // computed by verifyClassFiles(...) action
			passed
		);
		if (vmArguments != null) {
			if (this.verifier != null) {
				this.verifier.shutDown();
			}
			this.verifier = new TestVerifier(false);
			this.createdVerifier = true;
		}		
	} else {
		assertTrue("Unexpected problems: " + requestor.problemLog, false);
	}
}

protected void runConformTestThrowingError(String[] testFiles, String expectedSuccessOutputString, String[] classLib, boolean shouldFlushOutputDirectory, String[] vmArguments) {

	if (shouldFlushOutputDirectory) Util.flushDirectoryContent(new File(OUTPUT_DIR));
	
	IProblemFactory problemFactory = getProblemFactory();
	LogRequestor requestor = new LogRequestor(problemFactory, OUTPUT_DIR.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR + File.separator);
	Compiler batchCompiler =
		new Compiler(
			getNameEnvironment(new String[] {}, classLib),
			getErrorHandlingPolicy(),
			getCompilerOptions(),
			requestor,
			problemFactory);
	batchCompiler.compile(compilationUnits(testFiles)); // compile all files together
	if (!requestor.hasErrors) {
		String sourceFile = testFiles[0];
		
		// Compute class name by removing ".java" and replacing slashes with dots
		String className = sourceFile.substring(0, sourceFile.length() - 5).replace('/', '.').replace('\\', '.');
		
		boolean passed = this.verifier.verifyClassFilesThrowingError(sourceFile, className, expectedSuccessOutputString, this.classpaths, null, vmArguments);
		assertTrue(
			this.verifier.failureReason, // computed by verifyClassFiles(...) action
			passed
		);
	} else {
		assertTrue("Unexpected problems: " + requestor.problemLog, false);
	}
}

protected void runNegativeTest(String[] testFiles, ExpectedProblem[] expectedProblems) {
	this.runNegativeTest(testFiles, expectedProblems, null, true);
}
protected void runNegativeTest(String[] testFiles, ExpectedProblem[] expectedProblems, String[] classLib, boolean shouldFlushOutputDirectory) {

	if (shouldFlushOutputDirectory) Util.flushDirectoryContent(new File(OUTPUT_DIR));

	IProblemFactory problemFactory = getProblemFactory();
	Requestor requestor = getRequestor(problemFactory);
	requestor.expectedProblems(expectedProblems);
	Compiler batchCompiler =
		new Compiler(
			getNameEnvironment(new String[] {}, classLib),
			getErrorHandlingPolicy(),
			getCompilerOptions(),
			requestor,
			problemFactory);
	batchCompiler.compile(compilationUnits(testFiles)); // compile all files together
	if (!requestor.hasErrors) {
		String sourceFile = testFiles[0];
		
		// Compute class name by removing ".java" and replacing slashes with dots
		String className = sourceFile.substring(0, sourceFile.length() - 5).replace('/', '.').replace('\\', '.');
		
		assertTrue(
			this.verifier.failureReason,
			this.verifier.verifyClassFiles(sourceFile, className, null, this.classpaths)
		);
	}
}
protected void runNegativeTest(String[] testFiles, ExpectedProblem[] expectedProblems, String[] classLib, boolean shouldFlushOutputDirectory, boolean generateOutput) {


	if (shouldFlushOutputDirectory) Util.flushDirectoryContent(new File(OUTPUT_DIR));


	IProblemFactory problemFactory = getProblemFactory();
	Requestor requestor = getRequestor(problemFactory, generateOutput);
	requestor.expectedProblems(expectedProblems);
	Compiler batchCompiler =
		new Compiler(
			getNameEnvironment(new String[] {}, classLib),
			getErrorHandlingPolicy(),
			getCompilerOptions(),
			requestor,
			problemFactory);
	batchCompiler.compile(compilationUnits(testFiles)); // compile all files together
	if (!requestor.hasErrors) {
		String sourceFile = testFiles[0];
		
		// Compute class name by removing ".java" and replacing slashes with dots
		String className = sourceFile.substring(0, sourceFile.length() - 5).replace('/', '.').replace('\\', '.');
		
		assertTrue(
			this.verifier.failureReason,
			this.verifier.verifyClassFiles(sourceFile, className, null, this.classpaths)
		);
	}
}

/**
 * Log contains all problems (warnings+errors)
 */
protected void runNegativeTest(String[] testFiles, String expectedProblemLog) {
	this.runNegativeTest(testFiles, expectedProblemLog, null, true);
}

/**
 * Log contains all problems (warnings+errors)
 */
protected void runNegativeTest(String[] testFiles, String expectedProblemLog, String[] classLib, boolean shouldFlushOutputDirectory) {
	this.runNegativeTest(testFiles, expectedProblemLog, classLib, shouldFlushOutputDirectory, null);
}
/**
 * Log contains all problems (warnings+errors)
 */
protected void runNegativeTest(String[] testFiles, String expectedProblemLog, String[] classLib, boolean shouldFlushOutputDirectory, Map customOptions) {

	if (shouldFlushOutputDirectory) Util.flushDirectoryContent(new File(OUTPUT_DIR));

	IProblemFactory problemFactory = getProblemFactory();
	LogRequestor requestor = new LogRequestor(problemFactory, OUTPUT_DIR.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR + File.separator);
	Map options = getCompilerOptions();
	if (customOptions != null){
		options.putAll(customOptions);
	}
	Compiler batchCompiler =
		new Compiler(
			getNameEnvironment(new String[] {}, classLib),
			getErrorHandlingPolicy(),
			options,
			requestor,
			problemFactory);
	batchCompiler.compile(compilationUnits(testFiles)); // compile all files together

	String computedProblemLog = requestor.problemLog.toString();
	if (!expectedProblemLog.equals(computedProblemLog)){
	 	System.out.println(Util.displayString(computedProblemLog, 2));
	}
	assertEquals(
		"Invalid problem log ",
		expectedProblemLog,
		computedProblemLog);
}
protected void setUp() throws Exception {
	if (this.verifier == null) {
		this.verifier = new TestVerifier(true);
		this.createdVerifier = true;
	}
}
public static Test setupSuite(Class clazz) {
	return new RegressionTestSetup(suite(clazz));
}
public void stop() {
	this.verifier.shutDown();
}
public static Test suite(Class evaluationTestClass) {
	TestSuite suite = new TestSuite(evaluationTestClass);
	return suite;
}
protected void tearDown() throws Exception {
	if (this.createdVerifier) {
		this.stop();
	}
}
}
