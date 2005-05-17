/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;


import org.eclipse.jdt.core.search.SearchDocument;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.tests.junit.extension.StopableTestCase;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.core.tests.util.TestVerifier;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.core.search.JavaSearchParticipant;
import org.eclipse.jdt.internal.core.search.indexing.BinaryIndexer;

public abstract class AbstractRegressionTest extends AbstractCompilerTest implements StopableTestCase {
	public final static String PACKAGE_INFO_NAME = new String(TypeConstants.PACKAGE_INFO_NAME);
	public static String OUTPUT_DIR = Util.getOutputDirectory() + File.separator + "regression";
	public static int INDENT = 2;
	public static boolean SHIFT = false;

	protected INameEnvironment javaClassLib;
	protected String[] classpaths;
	protected TestVerifier verifier;
	protected boolean createdVerifier;
	public AbstractRegressionTest(String name) {
		super(name);
	}
	
	/*
	 * Returns the references in the given .class file.
	 */
	protected String findReferences(String classFilePath) {
		// check that "new Z().init()" is bound to "AbstractB.init()"
		final StringBuffer references = new StringBuffer(10);
		final SearchParticipant participant = new JavaSearchParticipant() {
			final SearchParticipant searchParticipant = this;
			public SearchDocument getDocument(final String documentPath) {
				return new SearchDocument(documentPath, this.searchParticipant) {
					public byte[] getByteContents() {
						try {
							return  org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(getPath()));
						} catch (IOException e) {
							e.printStackTrace();
							return null;
						}
					}
					public char[] getCharContents() {
						// not used
						return null;
					}
					public String getEncoding() {
						// not used
						return null;
					}
				};
			}
		};
		SearchDocument document = participant.getDocument(new File(classFilePath).getPath());
		BinaryIndexer indexer = new BinaryIndexer(document) {
			protected void addIndexEntry(char[] category, char[] key) {
				references.append(category);
				references.append('/');
				references.append(key);
				references.append('\n');
			}
		};
		indexer.indexDocument();
		String computedReferences = references.toString();
		return computedReferences;
	}
	protected INameEnvironment[] getClassLibs() {
		String encoding = (String)getCompilerOptions().get(CompilerOptions.OPTION_Encoding);
		if ("".equals(encoding))
			encoding = null;

		INameEnvironment[] classLibs = new INameEnvironment[1];
		classLibs[0] = new FileSystem(this.classpaths, new String[]{}, // ignore initial file names
				encoding // default encoding
		);
		return classLibs;
	}
	protected Map getCompilerOptions() {
		Map defaultOptions = super.getCompilerOptions();
		defaultOptions.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.GENERATE);
		defaultOptions.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_ReportPossibleAccidentalBooleanAssignment, CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.WARNING);
		defaultOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
		defaultOptions.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
		defaultOptions.put(CompilerOptions.OPTION_ReportUnnecessaryElse, CompilerOptions.WARNING );
		return defaultOptions;
	}
	protected String[] getDefaultClassPaths() {
		return Util.concatWithClassLibs(OUTPUT_DIR, false);
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
	 * Will consider first the source units passed as arguments, then investigate the classpath: jdklib + output dir
	 */
	protected INameEnvironment getNameEnvironment(final String[] testFiles, String[] classPaths) {
		this.classpaths = classPaths == null ? getDefaultClassPaths() : classPaths;
		return new InMemoryNameEnvironment(testFiles, getClassLibs());
	}
	protected IProblemFactory getProblemFactory() {
		return new DefaultProblemFactory(Locale.getDefault());
	}
	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
		if (setUp instanceof RegressionTestSetup) {
			RegressionTestSetup regressionTestSetUp = (RegressionTestSetup)setUp;
			this.javaClassLib = regressionTestSetUp.javaClassLib;
			this.verifier = regressionTestSetUp.verifier;
		}
	}
	protected void runConformTest(String[] testFiles) {
		runConformTest(testFiles, null, null, true, null);
	}

	protected void runConformTest(String[] testFiles, String[] vmArguments) {
		runConformTest(testFiles, null, null, true, vmArguments);
	}
	protected void runConformTest(
		String[] testFiles, 
		String expectedSuccessOutputString, 
		String[] vmArguments) {
		
		runConformTest(testFiles, expectedSuccessOutputString, null, true, vmArguments);
	}

	protected void runConformTest(String[] testFiles, String expectedSuccessOutputString) {
		runConformTest(testFiles, expectedSuccessOutputString, null, true, null);
	}
	protected void runConformTest(
		String[] testFiles, 
		String expectedSuccessOutputString, 
		String[] classLib,
		boolean shouldFlushOutputDirectory, 
		String[] vmArguments) {
		
		runConformTest(
			testFiles, 
			expectedSuccessOutputString, 
			classLib, 
			shouldFlushOutputDirectory, 
			vmArguments,
			null /*no custom options*/,
			null /*no custom requestor*/);
	}

	protected void runConformTest(
		String[] testFiles, 
		String expectedSuccessOutputString, 
		String[] classLib,
		boolean shouldFlushOutputDirectory, 
		String[] vmArguments, 
		Map customOptions,
		ICompilerRequestor clientRequestor) {

		if (shouldFlushOutputDirectory)
			Util.flushDirectoryContent(new File(OUTPUT_DIR));

		IProblemFactory problemFactory = getProblemFactory();
		Requestor requestor = 
			new Requestor(
				problemFactory, 
				OUTPUT_DIR.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR + File.separator, 
				false,
				clientRequestor);

		Map options = getCompilerOptions();
		if (customOptions != null) {
			options.putAll(customOptions);
		}
		Compiler batchCompiler = 
			new Compiler(
				getNameEnvironment(new String[]{}, classLib), 
				getErrorHandlingPolicy(), 
				options,
				requestor, 
				problemFactory);
		batchCompiler.options.produceReferenceInfo = true;
		try {
			batchCompiler.compile(Util.compilationUnits(testFiles)); // compile all files together
		} catch(RuntimeException e) {
			System.out.println(getClass().getName() + '#' + getName());
			e.printStackTrace();
			for (int i = 0; i < testFiles.length; i += 2) {
				System.out.print(testFiles[i]);
				System.out.println(" ["); //$NON-NLS-1$
				System.out.println(testFiles[i + 1]);
				System.out.println("]"); //$NON-NLS-1$
			}
			throw e;
		}
		if (!requestor.hasErrors) {
			String sourceFile = testFiles[0];

			// Compute class name by removing ".java" and replacing slashes with dots
			String className = sourceFile.substring(0, sourceFile.length() - 5).replace('/', '.').replace('\\', '.');
			if (className.endsWith(PACKAGE_INFO_NAME)) return;

			if (vmArguments != null) {
				if (this.verifier != null) {
					this.verifier.shutDown();
				}
				this.verifier = new TestVerifier(false);
				this.createdVerifier = true;
			}
			boolean passed = 
				this.verifier.verifyClassFiles(
					sourceFile, 
					className, 
					expectedSuccessOutputString,
					this.classpaths, 
					null, 
					vmArguments);
			if (!passed) {
				System.out.println(getClass().getName() + '#' + getName());
				for (int i = 0; i < testFiles.length; i += 2) {
					System.out.print(testFiles[i]);
					System.out.println(" ["); //$NON-NLS-1$
					System.out.println(testFiles[i + 1]);
					System.out.println("]"); //$NON-NLS-1$
				}
			}
			assertTrue(this.verifier.failureReason, // computed by verifyClassFiles(...) action
					passed);
			if (vmArguments != null) {
				if (this.verifier != null) {
					this.verifier.shutDown();
				}
				this.verifier = new TestVerifier(false);
				this.createdVerifier = true;
			}
		} else {
			System.out.println(getClass().getName() + '#' + getName());
			System.out.println(Util.displayString(requestor.problemLog, INDENT, SHIFT));
			for (int i = 0; i < testFiles.length; i += 2) {
				System.out.print(testFiles[i]);
				System.out.println(" ["); //$NON-NLS-1$
				System.out.println(testFiles[i + 1]);
				System.out.println("]"); //$NON-NLS-1$
			}
			assertTrue("Unexpected problems: " + requestor.problemLog, false);
		}
	}

	protected void runConformTestThrowingError(
		String[] testFiles, 
		String expectedSuccessOutputString, 
		String[] classLib,
		boolean shouldFlushOutputDirectory, 
		String[] vmArguments) {

		if (shouldFlushOutputDirectory)
			Util.flushDirectoryContent(new File(OUTPUT_DIR));

		IProblemFactory problemFactory = getProblemFactory();
		Requestor requestor = 
			new Requestor(
				problemFactory, 
				OUTPUT_DIR.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR + File.separator, 
				false,
				null/*no custom requestor*/);
		Compiler batchCompiler = 
			new Compiler(
				getNameEnvironment(new String[]{}, classLib), 
				getErrorHandlingPolicy(),
				getCompilerOptions(), 
				requestor, 
				problemFactory);
		batchCompiler.options.produceReferenceInfo = true;
		batchCompiler.compile(Util.compilationUnits(testFiles)); // compile all files together
		if (!requestor.hasErrors) {
			String sourceFile = testFiles[0];

			// Compute class name by removing ".java" and replacing slashes with dots
			String className = sourceFile.substring(0, sourceFile.length() - 5).replace('/', '.').replace('\\', '.');

			boolean passed = 
				this.verifier.verifyClassFilesThrowingError(
					sourceFile, 
					className, 
					expectedSuccessOutputString,
					this.classpaths, 
					null, 
					vmArguments);
			assertTrue(this.verifier.failureReason, // computed by verifyClassFiles(...) action
					passed);
		} else {
			assertTrue("Unexpected problems: " + requestor.problemLog, false);
		}
	}
	/**
	 * Log contains all problems (warnings+errors)
	 */
	protected void runNegativeTest(String[] testFiles, String expectedProblemLog) {
		runNegativeTest(testFiles, expectedProblemLog, null, true);
	}

	/**
	 * Log contains all problems (warnings+errors)
	 */
	protected void runNegativeTest(
		String[] testFiles, 
		String expectedProblemLog, 
		String[] classLib,
		boolean shouldFlushOutputDirectory) {
		
		runNegativeTest(testFiles, expectedProblemLog, classLib, shouldFlushOutputDirectory, null);
	}
	/**
	 * Log contains all problems (warnings+errors)
	 */
	protected void runNegativeTest(
		String[] testFiles, 
		String expectedProblemLog, 
		String[] classLib,
		boolean shouldFlushOutputDirectory, 
		Map customOptions) {
		
		runNegativeTest(testFiles, expectedProblemLog, classLib, shouldFlushOutputDirectory, customOptions, false);
	}
	/**
	 * Log contains all problems (warnings+errors)
	 */
	protected void runNegativeTest(
		String[] testFiles, 
		String expectedProblemLog, 
		String[] classLib,
		boolean shouldFlushOutputDirectory, 
		Map customOptions, 
		boolean generateOutput) {

		if (shouldFlushOutputDirectory)
			Util.flushDirectoryContent(new File(OUTPUT_DIR));

		IProblemFactory problemFactory = getProblemFactory();
		Requestor requestor = 
			new Requestor(
				problemFactory, 
				OUTPUT_DIR.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR + File.separator, 
				generateOutput,
				null/*no custom requestor*/);
		Map options = getCompilerOptions();
		if (customOptions != null) {
			options.putAll(customOptions);
		}
		Compiler batchCompiler = 
			new Compiler(
				getNameEnvironment(new String[]{}, classLib), 
				getErrorHandlingPolicy(), 
				options,
				requestor, problemFactory);
		batchCompiler.options.produceReferenceInfo = true;
		batchCompiler.compile(Util.compilationUnits(testFiles)); // compile all files together
		String computedProblemLog = Util.convertToIndependantLineDelimiter(requestor.problemLog.toString());
		String platformIndependantExpectedLog = Util.convertToIndependantLineDelimiter(expectedProblemLog);
		if (!platformIndependantExpectedLog.equals(computedProblemLog)) {
			System.out.println(getClass().getName() + '#' + getName());
			System.out.println(Util.displayString(computedProblemLog, INDENT, SHIFT));
			for (int i = 0; i < testFiles.length; i += 2) {
				System.out.print(testFiles[i]);
				System.out.println(" ["); //$NON-NLS-1$
				System.out.println(testFiles[i + 1]);
				System.out.println("]"); //$NON-NLS-1$
			}
		}
		assertEquals("Invalid problem log ", platformIndependantExpectedLog, computedProblemLog);
	}
	protected void setUp() throws Exception {
		super.setUp();
		if (this.verifier == null) {
			this.verifier = new TestVerifier(true);
			this.createdVerifier = true;
		}
	}
	public void stop() {
		this.verifier.shutDown();
	}
	protected void tearDown() throws Exception {
		if (this.createdVerifier) {
			this.stop();
		}
		// clean up output dir
		File outputDir = new File(OUTPUT_DIR);
		if (outputDir.exists()) {
			Util.flushDirectoryContent(outputDir);
			outputDir.delete();
		}
		super.tearDown();
	}
}
