/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import junit.framework.AssertionFailedError;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.search.SearchDocument;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.tests.junit.extension.StopableTestCase;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.core.tests.util.TestVerifier;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.core.search.JavaSearchParticipant;
import org.eclipse.jdt.internal.core.search.indexing.BinaryIndexer;

public abstract class AbstractRegressionTest extends AbstractCompilerTest implements StopableTestCase {
	public final static String PACKAGE_INFO_NAME = new String(TypeConstants.PACKAGE_INFO_NAME);
	public static final String OUTPUT_DIR = Util.getOutputDirectory() + File.separator + "regression";
	protected static final String EVAL_DIRECTORY = Util.getOutputDirectory()  + File.separator + "eval";
	protected static final String SOURCE_DIRECTORY = Util.getOutputDirectory()  + File.separator + "source";
	public static int INDENT = 2;
	public static boolean SHIFT = false;

	protected INameEnvironment javaClassLib;
	protected String[] classpaths;
	protected TestVerifier verifier;
	protected boolean createdVerifier;
	public AbstractRegressionTest(String name) {
		super(name);
	}
		
	protected void checkClassFile(String directoryName, String className, String source, String expectedOutput, int mode) throws ClassFormatException, IOException {
		compileAndDeploy(source, directoryName, className);
		try {
			File directory = new File(EVAL_DIRECTORY, directoryName);
			if (!directory.exists()) {
				assertTrue(".class file not generated properly in " + directory, false);
			}
			File f = new File(directory, className + ".class");
			byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(f);
			ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
			String result = disassembler.disassemble(classFileBytes, "\n", mode);
			int index = result.indexOf(expectedOutput);
			if (index == -1 || expectedOutput.length() == 0) {
				System.out.println(Util.displayString(result, 3));
			}
			if (index == -1) {
				assertEquals("Wrong contents", expectedOutput, result);
			}
			
			try {
				FileInputStream stream = new FileInputStream(f);
				ClassFileReader.read(stream, className + ".class", true);
				stream.close();
			} catch (org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException e) {
				e.printStackTrace();
				assertTrue("ClassFormatException", false);
			} catch (IOException e) {
				e.printStackTrace();
				assertTrue("IOException", false);
			}
		} finally {
			removeTempClass(className);
		}
	}

	protected void checkClassFile(String className, String source, String expectedOutput, int mode) throws ClassFormatException, IOException {
		this.checkClassFile("", className, source, expectedOutput, mode);
	}

	protected void checkClassFile(String className, String source, String expectedOutput) throws ClassFormatException, IOException {
		this.checkClassFile("", className, source, expectedOutput, ClassFileBytesDisassembler.SYSTEM);
	}

	protected void compileAndDeploy(String source, String directoryName, String className) {
		File directory = new File(SOURCE_DIRECTORY);
		if (!directory.exists()) {
			if (!directory.mkdirs()) {
				System.out.println("Could not create " + SOURCE_DIRECTORY);
				return;
			}
		}
		if (directoryName != null && directoryName.length() != 0) {
			directory = new File(SOURCE_DIRECTORY, directoryName);
			if (!directory.exists()) {
				if (!directory.mkdirs()) {
					System.out.println("Could not create " + directory);
					return;
				}
			}
		}
		String fileName = directory.getAbsolutePath() + File.separator + className + ".java";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
			writer.write(source);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		StringBuffer buffer = new StringBuffer()
			.append("\"")
			.append(fileName)
			.append("\" -d \"")
			.append(EVAL_DIRECTORY);
		if (this.complianceLevel.compareTo(COMPLIANCE_1_5) < 0) {
			buffer.append("\" -1.4 -source 1.3 -target 1.2");
		} else {
			buffer.append("\" -1.5");
		}
		buffer
			.append(" -preserveAllLocals -nowarn -g -classpath \"")
			.append(Util.getJavaClassLibsAsString())
			.append(SOURCE_DIRECTORY)
			.append("\"");
		org.eclipse.jdt.internal.compiler.batch.Main.compile(buffer.toString());
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

	protected void removeTempClass(String className) {
		File dir = new File(SOURCE_DIRECTORY);
		String[] fileNames = dir.list();
		if (fileNames != null) {
			for (int i = 0, max = fileNames.length; i < max; i++) {
				if (fileNames[i].indexOf(className) != -1) {
					new File(SOURCE_DIRECTORY + File.separator + fileNames[i]).delete();
				}
			}
		}
		
		dir = new File(EVAL_DIRECTORY);
		fileNames = dir.list();
		if (fileNames != null) {
			for (int i = 0, max = fileNames.length; i < max; i++) {
				if (fileNames[i].indexOf(className) != -1) {
					new File(EVAL_DIRECTORY + File.separator + fileNames[i]).delete();
				}
			}
		}
	
	}
	protected void runConformTest(String[] testFiles) {
		runConformTest(
			testFiles, 
			null /* no expected output string */, 
			null /* no extra class libraries */, 
			true /* flush output directory */, 
			null /* no vm arguments */,
			null /* no custom options*/,
			null /* no custom requestor*/,
		  	false /* do not skip javac for this peculiar test */);	
	}

	protected void runConformTest(String[] testFiles, String[] vmArguments) {
		runConformTest(
			testFiles, 
			null /* no expected output string */, 
			null /* no extra class libraries */, 
			true /* flush output directory */, 
			vmArguments,
			null /* no custom options*/,
			null /* no custom requestor*/,
		  	false /* do not skip javac for this peculiar test */);	
	}
	protected void runConformTest(
		String[] testFiles, 
		String expectedSuccessOutputString, 
		String[] vmArguments) {
		runConformTest(
			testFiles, 
			expectedSuccessOutputString, 
			null /* no extra class libraries */, 
			true /* flush output directory */, 
			vmArguments,
			null /* no custom options*/,
			null /* no custom requestor*/,
		  	false /* do not skip javac for this peculiar test */);	
		}

	protected void runConformTest(String[] testFiles, String expectedSuccessOutputString) {
		runConformTest(
			testFiles, 
			expectedSuccessOutputString, 
			null /* no extra class libraries */, 
			true /* flush output directory */, 
			null /* no vm arguments */,
			null /* no custom options*/,
			null /* no custom requestor*/,
		  	false /* do not skip javac for this peculiar test */); 
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
			null /* no custom options*/,
			null /* no custom requestor*/,
		  	false /* do not skip javac for this peculiar test */); 
	}

	protected void runConformTest(
		String[] testFiles, 
		String expectedSuccessOutputString, 
		String[] classLib,
		boolean shouldFlushOutputDirectory, 
		String[] vmArguments, 
		Map customOptions,
		ICompilerRequestor clientRequestor) {
		runConformTest(
		  testFiles, 
		  expectedSuccessOutputString, 
		  classLib,
		  shouldFlushOutputDirectory, 
		  vmArguments, 
		  customOptions,
		  clientRequestor,
		  false /* do not skip javac for this peculiar test */); 
	}
	
	protected void runConformTest(
		String[] testFiles, 
		String expectedSuccessOutputString, 
		String[] classLib,
		boolean shouldFlushOutputDirectory, 
		String[] vmArguments, 
		Map customOptions,
		ICompilerRequestor clientRequestor,
		boolean skipJavac) {
		// Non-javac part
		try {
			if (shouldFlushOutputDirectory)
				Util.flushDirectoryContent(new File(OUTPUT_DIR));
	
			IProblemFactory problemFactory = getProblemFactory();
			Requestor requestor = 
				new Requestor(
					problemFactory, 
					OUTPUT_DIR.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR + File.separator, 
					false,
					clientRequestor,
					false, /* show category */
					false /* show warning token*/);
	
			Map options = getCompilerOptions();
			if (customOptions != null) {
				options.putAll(customOptions);
			}
			CompilerOptions compilerOptions = new CompilerOptions(options);
			compilerOptions.performStatementsRecovery = false;
			Compiler batchCompiler = 
				new Compiler(
					getNameEnvironment(new String[]{}, classLib), 
					getErrorHandlingPolicy(), 
					compilerOptions,
					requestor, 
					problemFactory);
			compilerOptions.produceReferenceInfo = true;
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
		// javac part
		} catch (AssertionFailedError e) {
			throw e;
		} finally {
			if (RUN_JAVAC && !skipJavac)
				runJavac(testFiles, null, expectedSuccessOutputString, shouldFlushOutputDirectory);
			  // PREMATURE for now, skipping javac implies skipping the compile
			  //                and execution steps; yet, only cases for which the
			  //                execution step was a problem have been discovered so
			  //                far; may consider skipping the execution step only
		}
	}

	// PREMATURE consider whether conform tests throwing errors should
	//                implement javac comparison or not
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
				null/*no custom requestor*/,
				false, /* show category */
				false /* show warning token*/);
		
		CompilerOptions compilerOptions = new CompilerOptions(getCompilerOptions());
		compilerOptions.performStatementsRecovery = false;
		Compiler batchCompiler = 
			new Compiler(
				getNameEnvironment(new String[]{}, classLib), 
				getErrorHandlingPolicy(),
				compilerOptions, 
				requestor, 
				problemFactory);
		batchCompiler.options.produceReferenceInfo = true;
		Throwable exception = null;
		try {
			batchCompiler.compile(Util.compilationUnits(testFiles)); // compile all files together
		} catch(RuntimeException e){
			exception = e;
			throw e;
		} catch(Error e) {
			exception = e;
			throw e;
		} finally {

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
				if (exception == null)
					assertTrue(this.verifier.failureReason, // computed by verifyClassFiles(...) action
						passed);
			} else {
				if (exception == null)
					assertTrue("Unexpected problems: " + requestor.problemLog, false);
			}
		}
	}
	/**
	 * Log contains all problems (warnings+errors)
	 */
	protected void runNegativeTest(String[] testFiles, String expectedProblemLog) {
		runNegativeTest(testFiles, expectedProblemLog, null, true);
		runNegativeTest(
			testFiles, 
			expectedProblemLog, 
			null /* no extra class libraries */, 
			true /* flush output directory */, 
			null /* no custom options */,
			false /* do not generate output */,
			false /* do not show category */, 
			false /* do not show warning token */, 
			false  /* do not skip javac for this peculiar test */);
	}

	/**
	 * Log contains all problems (warnings+errors)
	 */
	protected void runNegativeTest(
		String[] testFiles, 
		String expectedProblemLog, 
		String[] classLib,
		boolean shouldFlushOutputDirectory) {
		runNegativeTest(
			testFiles, 
			expectedProblemLog, 
			classLib, 
			shouldFlushOutputDirectory, 
			null /* no custom options */,
			false /* do not generate output */,
			false /* do not show category */, 
			false /* do not show warning token */, 
			false  /* do not skip javac for this peculiar test */);
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
		runNegativeTest(
			testFiles, 
			expectedProblemLog, 
			classLib, 
			shouldFlushOutputDirectory, 
			customOptions, 
			false /* do not generate output */,
			false /* do not show category */, 
			false /* do not show warning token */, 
			false  /* do not skip javac for this peculiar test */);
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
		boolean generateOutput,
		boolean showCategory,
		boolean showWarningToken) {
    runNegativeTest(
		  testFiles, 
		  expectedProblemLog, 
		  classLib,
		  shouldFlushOutputDirectory, 
		  customOptions, 
		  generateOutput,
		  showCategory,
		  showWarningToken,
		  false  /* do not skip javac for this peculiar test */);
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
		boolean generateOutput,
		boolean showCategory,
		boolean showWarningToken,
		boolean skipJavac) {
		// Non-javac part
		try {
			if (shouldFlushOutputDirectory)
				Util.flushDirectoryContent(new File(OUTPUT_DIR));
	
			IProblemFactory problemFactory = getProblemFactory();
			Requestor requestor = 
				new Requestor(
					problemFactory, 
					OUTPUT_DIR.endsWith(File.separator) ? OUTPUT_DIR : OUTPUT_DIR + File.separator, 
					generateOutput,
					null/*no custom requestor*/,
					showCategory,
					showWarningToken);
			Map options = getCompilerOptions();
			if (customOptions != null) {
				options.putAll(customOptions);
			}
			CompilerOptions compilerOptions = new CompilerOptions(options);
			compilerOptions.performStatementsRecovery = false;
			Compiler batchCompiler = 
				new Compiler(
					getNameEnvironment(new String[]{}, classLib), 
					getErrorHandlingPolicy(), 
					compilerOptions,
					requestor, 
					problemFactory);
			batchCompiler.options.produceReferenceInfo = true;
			Throwable exception = null;
			try {
				batchCompiler.compile(Util.compilationUnits(testFiles)); // compile all files together
			} catch(RuntimeException e){
				exception = e;
				throw e;
			} catch(Error e) {
				exception = e;
				throw e;
			} finally {
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
				if (exception == null)
					assertEquals("Invalid problem log ", platformIndependantExpectedLog, computedProblemLog);
			}
		// javac part
		} catch (AssertionFailedError e) {
			throw e;
		} finally {
			if (RUN_JAVAC && !skipJavac)
				runJavac(testFiles, expectedProblemLog, null, shouldFlushOutputDirectory);
		}
	}
	
	protected void runNegativeTestWithExecution(
			String[] testFiles, 
			String expectedProblemLog, 
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
				true,
				clientRequestor,
				false /*show category*/,
				false /*show warning token*/);

		Map options = getCompilerOptions();
		if (customOptions != null) {
			options.putAll(customOptions);
		}
		CompilerOptions compilerOptions = new CompilerOptions(options);
		compilerOptions.performStatementsRecovery = false;
		Compiler batchCompiler = 
			new Compiler(
				getNameEnvironment(new String[]{}, classLib), 
				getErrorHandlingPolicy(), 
				compilerOptions,
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
		assertTrue("Must have errors", requestor.hasErrors);
		
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
			assertEquals("Invalid problem log ", platformIndependantExpectedLog, computedProblemLog);
		}
		
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
			String platformIndependantExpectedSuccessOutputString = Util.convertToIndependantLineDelimiter(expectedSuccessOutputString);
			String platformIndependantFailureReason = Util.convertToIndependantLineDelimiter(this.verifier.failureReason);
			if (platformIndependantFailureReason.indexOf(platformIndependantExpectedSuccessOutputString) == -1) {
				System.out.println(getClass().getName() + '#' + getName());
				System.out.println(Util.displayString(platformIndependantFailureReason, INDENT, SHIFT));
				assertEquals("Invalid runtime log ", platformIndependantExpectedSuccessOutputString, platformIndependantFailureReason);
				System.out.println(getClass().getName() + '#' + getName());
				for (int i = 0; i < testFiles.length; i += 2) {
					System.out.print(testFiles[i]);
					System.out.println(" ["); //$NON-NLS-1$
					System.out.println(testFiles[i + 1]);
					System.out.println("]"); //$NON-NLS-1$
				}
			}
		} else if (vmArguments != null) {
			if (this.verifier != null) {
				this.verifier.shutDown();
			}
			this.verifier = new TestVerifier(false);
			this.createdVerifier = true;
		}
	}

	protected void setUp() throws Exception {
		super.setUp();
		if (this.verifier == null) {
			this.verifier = new TestVerifier(true);
			this.createdVerifier = true;
		}
		if (RUN_JAVAC) {
			if (!getClass().getName().equals(CURRENT_CLASS_NAME)) {
				if (javacFullLog == null) {
					// One time initialization of javac related concerns
					// compute command lines and extract javac version
					String jdkRootDirectory = System.getProperty("jdk.root");
					if (jdkRootDirectory == null)
					  jdkRootDirPath = (new Path(Util.getJREDirectory())).removeLastSegments(1);
					else 
						jdkRootDirPath = new Path(jdkRootDirectory);
		
					StringBuffer cmdLineHeader = new StringBuffer(jdkRootDirPath.
							append("bin").append(JAVA_NAME).toString()); // PREMATURE replace JAVA_NAME and JAVAC_NAME with locals? depends on potential reuse
					javaCommandLineHeader = cmdLineHeader.toString();
					cmdLineHeader = new StringBuffer(jdkRootDirPath.
							append("bin").append(JAVAC_NAME).toString());
					cmdLineHeader.append(" -classpath . ");
					  // start with the current directory which contains the source files
					Process compileProcess = Runtime.getRuntime().exec(
						cmdLineHeader.toString() + " -version", null, null);
	        Logger versionLogger = new Logger(compileProcess.getErrorStream(), ""); // WORK            
	        versionLogger.start();
	        compileProcess.waitFor();
					versionLogger.join(); // make sure we get the whole output
					String version = versionLogger.buffer.toString();
					int eol = version.indexOf('\n');
					version = version.substring(0, eol);
					cmdLineHeader.append(" -d ");
					cmdLineHeader.append(JAVAC_OUTPUT_DIR.indexOf(" ") != -1 ? "\"" + JAVAC_OUTPUT_DIR + "\"" : JAVAC_OUTPUT_DIR);
					cmdLineHeader.append(" -source 1.5 -deprecation -Xlint:unchecked "); // enable recommended warnings
					// REVIEW consider enabling all warnings instead? Philippe does not see
					//        this as ez to use (too many changes in logs)
					javacCommandLineHeader = cmdLineHeader.toString();

					javacFullLogFileName = Util.getOutputDirectory() +	File.separatorChar + 
                    							version.replace(' ', '_') + "_" + 
                    					    (new SimpleDateFormat("yyyyMMdd_HHmmss")).format(new Date()) +
                    					    ".txt";
					javacFullLog = 
					  	new PrintWriter(new FileOutputStream(javacFullLogFileName));
					javacFullLog.println(version); // so that the contents is self sufficient
					System.out.println("***************************************************************************");
					System.out.println("* Sun Javac compiler output archived into file:");
					System.out.println("* " + javacFullLogFileName);
					System.out.println("***************************************************************************");
				}
				// per class initialization
				CURRENT_CLASS_NAME = getClass().getName();
				dualPrintln("***************************************************************************");
				System.out.print("* Comparison with Sun Javac compiler for class ");
				dualPrintln(CURRENT_CLASS_NAME.substring(CURRENT_CLASS_NAME.lastIndexOf('.')+1) + 
						" (" + TESTS_COUNTERS.get(CURRENT_CLASS_NAME) + " tests)");
				System.out.println("***************************************************************************");
				DIFF_COUNTERS[0] = 0;
				DIFF_COUNTERS[1] = 0;
				DIFF_COUNTERS[2] = 0;
			}
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
		if (RUN_JAVAC) {
			printJavacResultsSummary();
		}
	}
	
	protected void executeClass(
			String sourceFile, 
			String expectedSuccessOutputString, 
			String[] classLib,
			boolean shouldFlushOutputDirectory, 
			String[] vmArguments, 
			Map customOptions,
			ICompilerRequestor clientRequestor) {

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
		assertTrue(this.verifier.failureReason, // computed by verifyClassFiles(...) action
				passed);
		if (vmArguments != null) {
			if (this.verifier != null) {
				this.verifier.shutDown();
			}
			this.verifier = new TestVerifier(false);
			this.createdVerifier = true;
		}
	}
	
	// javac comparison related types, fields and methods - see runJavac for
	// details
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
		
	protected static IPath jdkRootDirPath;
	protected static final String JAVA_NAME = 
		File.pathSeparatorChar == ':' ? "java" : "java.exe";
	protected static final String JAVAC_NAME = 
		File.pathSeparatorChar == ':' ? "javac" : "javac.exe";
	protected static String JAVAC_OUTPUT_DIR = 
		Util.getOutputDirectory() + File.separator + "javac";
	protected static int[] DIFF_COUNTERS = new int[3];
	protected static PrintWriter javacFullLog;
	protected static String javacCommandLineHeader;
	protected static String javaCommandLineHeader;
	protected static String javacTestName; 
	  // needed for multiple test calls within a single test method
	protected static boolean javacTestErrorFlag;
	  private static String javacFullLogFileName;
	  // flags errors so that any error in a test case prevents
	  // java execution
	 
	/*######################################
	 * Specific method to let tests Sun javac compilation available...
	 #######################################*/
	/*
	 * Cleans up the given directory by removing all the files it contains as well
	 * but leaving the directory.
	 * @throws TargetException if the target path could not be cleaned up
	 */
	protected void cleanupDirectory(File directory) {
		if (!directory.exists()) {
			return;
		}
		String[] fileNames = directory.list();
		for (int i = 0; i < fileNames.length; i++) {
			File file = new File(directory, fileNames[i]);
			if (file.isDirectory()) {
				cleanupDirectory(file);
			} else {
				if (!file.delete())
					System.out.println("Could not delete file " + file.getPath());
			}
		}
		if (!directory.delete())
			System.out.println("Could not delete directory " + directory.getPath());
	}

	/*
	 * Write given source test files in current output sub-directory.
	 * Use test name for this sub-directory name (ie. test001, test002, etc...)
	 */
	protected void printFiles(String[] testFiles) {
		for (int i=0, length=testFiles.length; i<length; i++) {
			System.out.println(testFiles[i++]);
			System.out.println(testFiles[i]);
		}
		System.out.println("");
	}

	/*
	 * Run Sun compilation using javac.
	 * Launch compilation in a thread and verify that it does not take more than 5s
	 * to perform it. Otherwise abort the process and log in console.
	 * WORK not sure we really do that 5s cap any more.
	 * A semi verbose output is sent to the console that analyzes differences
	 * of behaviors between javac and Eclipse on a per test basis. A more 
	 * verbose output is produced into a file which name is printed on the
	 * console. Such files can be compared between various javac releases
	 * to check potential changes. 
	 * To enable such tests, specify the following VM properies in the launch
	 * configuration:
	 * -Drun.javac=enabled
	 *     mandatory - tells the test suite to run javac tests
	 * -Djdk.root=<the root directory of the tested javac>
	 *     optional - enables to find the javac that will be run by the tests
	 *     suite; the root directory must be specified as an absolute path and
	 *     should point to the JDK root, aka /opt/jdk1.5.0_05 for Linux or
	 *     c:/JDK_50 for Windows; in case this property is not specified, the
	 *     tests suite will use the runtime JRE of the launching configuration.
	 * Note that enabling javac tests implies running into 1.5 compliance level
	 * (without having to specify it into the VM properties.)
	 * TODO (maxime) consider impacts of Java 6
	 */
	protected void runJavac(
			String[] testFiles, 
			final String expectedProblemLog, 
			final String expectedSuccessOutputString, 
			boolean shouldFlushOutputDirectory) {
		String testName = null;
		Process compileProcess = null;
		Process execProcess = null;
		try {
			// Init test name
			testName = testName();

			// Cleanup javac output dir if needed
			File javacOutputDirectory = new File(JAVAC_OUTPUT_DIR);
			if (shouldFlushOutputDirectory) {
				cleanupDirectory(javacOutputDirectory);
			}
			
			// Write files in dir
			writeFiles(testFiles);

			// Prepare command line
			StringBuffer cmdLine = new StringBuffer(javacCommandLineHeader);
			// compute extra classpath
			String[] classpath = Util.concatWithClassLibs(JAVAC_OUTPUT_DIR, false);
			StringBuffer cp = new StringBuffer(" -classpath ");
			int length = classpath.length;
			for (int i = 0; i < length; i++) {
				if (i > 0)
				  cp.append(File.pathSeparatorChar);
				if (classpath[i].indexOf(" ") != -1) {
					cp.append("\"" + classpath[i] + "\"");
				} else {
					cp.append(classpath[i]);
				}
			} 
			cmdLine.append(cp);
			// add source files
			for (int i = 0; i < testFiles.length; i += 2) {
				// *.java is not enough (p1/X.java, p2/Y.java)
				cmdLine.append(' ');
				cmdLine.append(testFiles[i]);
			}

			// Launch process
			compileProcess = Runtime.getRuntime().exec(
				cmdLine.toString(), null, this.outputTestDirectoryPath.toFile());

			// Log errors
      Logger errorLogger = new Logger(compileProcess.getErrorStream(), "ERROR");            

      // Log output
      Logger outputLogger = new Logger(compileProcess.getInputStream(), "OUTPUT");

      // start the threads to run outputs (standard/error)
      errorLogger.start();
      outputLogger.start();

      // Wait for end of process
			int exitValue = compileProcess.waitFor();
			errorLogger.join(); // make sure we get the whole output
			outputLogger.join();

			// Report raw javac results
			if (! testName.equals(javacTestName)) {
				javacTestName = testName;
				javacTestErrorFlag = false;
				javacFullLog.println("-----------------------------------------------------------------");
				javacFullLog.println(CURRENT_CLASS_NAME + " " + testName);
			}
			if (exitValue != 0) {
				javacTestErrorFlag = true;
			}
			if (errorLogger.buffer.length() > 0) {
				javacFullLog.println("--- javac err: ---");
				javacFullLog.println(errorLogger.buffer.toString());
			}
			if (outputLogger.buffer.length() > 0) {
				javacFullLog.println("--- javac out: ---");
				javacFullLog.println(outputLogger.buffer.toString());
			}

			// Compare compilation results
			if (expectedProblemLog == null || expectedProblemLog.length() == 0) {
				// Eclipse found no error and no warning
				if (exitValue != 0) {
					// Javac found errors
					System.out.println("----------------------------------------");
					System.out.println(testName + " - Javac has found error(s) but Eclipse expects conform result:\n");
					javacFullLog.println("JAVAC_MISMATCH: Javac has found error(s) but Eclipse expects conform result");
					System.out.println(errorLogger.buffer.toString());
					printFiles(testFiles);
					DIFF_COUNTERS[0]++;
				} 
				else {
					// Javac found no error - may have found warnings
					if (errorLogger.buffer.length() > 0) {
						System.out.println("----------------------------------------");
						System.out.println(testName + " - Javac has found warning(s) but Eclipse expects conform result:\n");
						javacFullLog.println("JAVAC_MISMATCH: Javac has found warning(s) but Eclipse expects conform result");
						System.out.println(errorLogger.buffer.toString());
						printFiles(testFiles);
						DIFF_COUNTERS[0]++;
					} 
					if (expectedSuccessOutputString != null && !javacTestErrorFlag) {
						// Neither Eclipse nor Javac found errors, and we have a runtime
						// bench value
						StringBuffer javaCmdLine = new StringBuffer(javaCommandLineHeader);
						javaCmdLine.append(cp);
						javaCmdLine.append(' ').append(testFiles[0].substring(0, testFiles[0].indexOf('.')));
							// assume executable class is name of first test file - PREMATURE check if this is also the case in other test fwk classes
						execProcess = Runtime.getRuntime().exec(javaCmdLine.toString(), null, this.outputTestDirectoryPath.toFile());
						Logger logger = new Logger(execProcess.getInputStream(), ""); // WORK
						logger.start();
						exitValue = execProcess.waitFor();
						logger.join(); // make sure we get the whole output
						String javaOutput = logger.buffer.toString().trim();
						if (!expectedSuccessOutputString.equals(javaOutput)) {
							System.out.println("----------------------------------------");
							System.out.println(testName + " - Javac and Eclipse runtime output is not the same:");
							javacFullLog.println("JAVAC_MISMATCH: Javac and Eclipse runtime output is not the same");
							dualPrintln("eclipse:");
							dualPrintln(expectedSuccessOutputString);
							dualPrintln("javac:");
							dualPrintln(javaOutput);
							System.out.println("\n");
							printFiles(testFiles); // PREMATURE consider printing files to the log as well
							DIFF_COUNTERS[2]++;
						}
					}
				}
			} 
			else {
				// Eclipse found errors or warnings
				if (errorLogger.buffer.length() == 0) {
					System.out.println("----------------------------------------");
					System.out.println(testName + " - Eclipse has found error(s)/warning(s) but Javac did not find any:");
					javacFullLog.println("JAVAC_MISMATCH: Eclipse has found error(s)/warning(s) but Javac did not find any");
					dualPrintln("eclipse:");
					dualPrintln(expectedProblemLog);
					printFiles(testFiles);
					DIFF_COUNTERS[1]++;
				} else if (expectedProblemLog.indexOf("ERROR") > 0 && exitValue == 0){
					System.out.println("----------------------------------------");
					System.out.println(testName + " - Eclipse has found error(s) but Javac only found warning(s):");
					javacFullLog.println("JAVAC_MISMATCH: Eclipse has found error(s) but Javac only found warning(s)");
					dualPrintln("eclipse:");
					dualPrintln(expectedProblemLog);
					System.out.println("javac:");
					System.out.println(errorLogger.buffer.toString());
					printFiles(testFiles);
					DIFF_COUNTERS[1]++;
				} else {
					// PREMATURE refine comparison
					// TODO (frederic) compare warnings in each result and verify they are similar...
//						System.out.println(testName+": javac has found warnings :");
//						System.out.print(errorLogger.buffer.toString());
//						System.out.println(testName+": we're expecting warning results:");
//						System.out.println(expectedProblemLog);
				}
			}
		} 
		catch (InterruptedException e1) {
			if (compileProcess != null) compileProcess.destroy();
			if (execProcess != null) execProcess.destroy();
			System.out.println(testName+": Sun javac compilation was aborted!");
			javacFullLog.println("JAVAC_WARNING: Sun javac compilation was aborted!");
			e1.printStackTrace(javacFullLog);
		}
		catch (Throwable e) {
			System.out.println(testName+": could not launch Sun javac compilation!");
			e.printStackTrace();
			javacFullLog.println("JAVAC_ERROR: could not launch Sun javac compilation!");
			e.printStackTrace(javacFullLog);
			// PREMATURE failing the javac pass or comparison could also fail
			//           the test itself
		} 
		finally {
			// Clean up written file(s)
			cleanupDirectory(outputTestDirectoryPath.toFile());
		}
	}

	protected void	printJavacResultsSummary() {
		if (RUN_JAVAC) {
			Integer count = (Integer)TESTS_COUNTERS.get(CURRENT_CLASS_NAME);
			if (count != null) {
				int newCount = count.intValue()-1;
				TESTS_COUNTERS.put(CURRENT_CLASS_NAME, new Integer(newCount));
				if (newCount == 0) {
					if (DIFF_COUNTERS[0]!=0 || DIFF_COUNTERS[1]!=0 || DIFF_COUNTERS[2]!=0) {
						dualPrintln("===========================================================================");
						dualPrintln("Results summary:");
					}
					if (DIFF_COUNTERS[0]!=0)
						dualPrintln("	- "+DIFF_COUNTERS[0]+" test(s) where Javac found errors/warnings but Eclipse did not");
					if (DIFF_COUNTERS[1]!=0)
						dualPrintln("	- "+DIFF_COUNTERS[1]+" test(s) where Eclipse found errors/warnings but Javac did not");
					if (DIFF_COUNTERS[2]!=0)
						dualPrintln("	- "+DIFF_COUNTERS[2]+" test(s) where Eclipse and Javac did not have same output");
					System.out.println("\n");
				}
			}
			dualPrintln("\n\nFull results sent to " + javacFullLogFileName);
			javacFullLog.flush();
		}
	}
	
	protected void dualPrintln(String message) {
		System.out.println(message);
		javacFullLog.println(message);
	}
}
