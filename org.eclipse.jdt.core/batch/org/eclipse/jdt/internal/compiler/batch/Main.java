package org.eclipse.jdt.internal.compiler.batch;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.Compiler;

import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;

import org.eclipse.jdt.internal.compiler.codegen.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;
import org.eclipse.jdt.internal.compiler.impl.*;

import java.io.*;
import java.util.*;

public class Main implements ConfigurableProblems, ProblemSeverities {
	PrintWriter out;
	boolean systemExitWhenFinished = true;
	boolean proceedOnError = false;
	int warningMask =
		ParsingOptionalError |
		MethodWithConstructorName | OverriddenPackageDefaultMethod |
		UsingDeprecatedAPI | MaskedCatchBlock |
		UnusedLocalVariable | UnusedArgument |
		TemporaryWarning | OverriddenPackageDefaultMethod |
		AccessEmulation;
			
	int debugMask = CompilerOptions.Lines | CompilerOptions.Source;
	int targetJDK = CompilerOptions.JDK1_1;
	boolean verbose = false;
	boolean produceRefInfo = false;
	boolean importProblemIsError = true;
	boolean timer = false;
	boolean showProgress = false;
	public long time = 0;
	long lineCount;
	boolean preserveAllLocalVariables = false; // The unused and final local variables will be optimized

	String[] filenames;
	String[] classpaths;
	String destinationPath;
	String log;
	int repetitions;
	int globalProblemsCount;
	int globalErrorsCount;
	int globalWarningsCount;

	String versionID = "0.125.12 (jck1.3a)"/*nonNLS*/;
	private static final char[] CLASS_FILE_EXTENSION = ".class"/*nonNLS*/.toCharArray();

	int exportedClassFilesCounter;

	/**
	 * Are we running JDK 1.1?
	 */
	private static boolean JDK1_1 = false;

	/* Bundle containing messages */
	protected static ResourceBundle bundle;
	private final static String bundleName = "org.eclipse.jdt.internal.compiler.batch.Messages"/*nonNLS*/;
	static {
		String ver = System.getProperty("java.version"/*nonNLS*/);
		JDK1_1 = ((ver != null) && ver.startsWith("1.1"/*nonNLS*/));
		relocalize();
	}
protected Main(PrintWriter writer, boolean systemExitWhenFinished) {
	this.out = writer;
	this.systemExitWhenFinished = systemExitWhenFinished;
	exportedClassFilesCounter = 0;
}
/*
 *  Low-level API performing the actual compilation
 */
protected void compile(String[] argv) {
	// decode command line arguments
	try {
		configure(argv);
		if (showProgress) System.out.print(Main.bind("progress.compiling"/*nonNLS*/));
		for (int i = 0; i < repetitions; i++){
			globalProblemsCount = 0;
			globalErrorsCount = 0;
			globalWarningsCount = 0;		
			lineCount = 0;
			if (repetitions > 1){
				out.flush();
				out.println(Main.bind("compile.repetition"/*nonNLS*/,String.valueOf(i+1),String.valueOf(repetitions)));
			}
			long startTime = System.currentTimeMillis();

			// request compilation
			performCompilation();
			if (timer) {
				time = System.currentTimeMillis() - startTime;
				if (lineCount != 0){
					out.println(Main.bind("compile.instantTime"/*nonNLS*/,new String[]{String.valueOf(lineCount),String.valueOf(time),String.valueOf((((int)((lineCount*10000.0)/time))/10.0))}));
				} else {
					out.println(Main.bind("compile.totalTime"/*nonNLS*/,String.valueOf(time)));
					
				}
			}
			if (globalProblemsCount > 0) {
				if (globalProblemsCount == 1) {
					out.print(Main.bind("compile.oneProblem"/*nonNLS*/));
				} else {
					out.print(Main.bind("compile.severalProblems"/*nonNLS*/,String.valueOf(globalProblemsCount)));
				}
				out.print(" ("/*nonNLS*/);
				if (globalErrorsCount > 0) {
					if (globalErrorsCount == 1) {
						out.print(Main.bind("compile.oneError"/*nonNLS*/));
					} else {
						out.print(Main.bind("compile.severalErrors"/*nonNLS*/,String.valueOf(globalErrorsCount)));
					}
				}
				if (globalWarningsCount > 0) {
					if (globalErrorsCount > 0) {
						out.print(", "/*nonNLS*/);
					}
					if (globalWarningsCount == 1) {
						out.print(Main.bind("compile.oneWarning"/*nonNLS*/));
					} else {
						out.print(Main.bind("compile.severalWarnings"/*nonNLS*/,String.valueOf(globalWarningsCount)));
					}
				}
				out.println(")"/*nonNLS*/);
			}
			if (exportedClassFilesCounter != 0 && (this.showProgress || this.timer || this.verbose)) {
				if (exportedClassFilesCounter == 1) {
					out.print(Main.bind("compile.oneClassFileGenerated"/*nonNLS*/));
				} else {
					out.print(Main.bind("compile.severalClassFilesGenerated"/*nonNLS*/,String.valueOf(exportedClassFilesCounter)));
				}
			}
		}
		if (showProgress) System.out.println();
		if (systemExitWhenFinished){
			out.flush();
			System.exit(globalErrorsCount > 0 ? -1 : 0);
		}
	} catch (InvalidInputException e) {
		out.println(e.getMessage());
		out.println("------------------------"/*nonNLS*/);
		printUsage();
		if (systemExitWhenFinished){
			System.exit(-1);			
		}
	} catch (ThreadDeath e) { // do not stop this one
		throw e;
	} catch (Throwable e) { // internal compiler error
		if (systemExitWhenFinished) {
			out.flush();
			System.exit(-1);
		}
	} finally {
		out.flush();
	}
}
/*
 * Internal IDE API
 */
public static void compile(String commandLine) {
	compile(commandLine, new PrintWriter(System.out));
}
/*
 * Internal IDE API for test harness purpose
 */
public static void compile(String commandLine, PrintWriter writer) {
	int count = 0;
	String[] argv = new String[10];
	int startIndex = 0;
	int lastIndex = commandLine.indexOf('"');
	boolean insideQuotes = false;
	boolean insideClasspath = false;
	StringTokenizer tokenizer;
	while (lastIndex != -1) {
		if (insideQuotes) {
			if (count == argv.length) {
				System.arraycopy(argv, 0, (argv = new String[count * 2]), 0, count);
			}
			if (insideClasspath) {
				argv[count-1] += commandLine.substring(startIndex, lastIndex);
				insideClasspath = false;
			} else {
				argv[count++] = commandLine.substring(startIndex, lastIndex);
			}
		} else {
			String subCommandLine = commandLine.substring(startIndex, lastIndex);
			if (subCommandLine.equals(File.pathSeparator)) {
				argv[count-1] += File.pathSeparator;
				insideClasspath = true;
			} else {
				tokenizer = new StringTokenizer(subCommandLine, File.pathSeparator + " "/*nonNLS*/);
				while (tokenizer.hasMoreTokens()) {
					if (count == argv.length) {
						System.arraycopy(argv, 0, (argv = new String[count * 2]), 0, count);
					}
					argv[count++] = tokenizer.nextToken();
				}
			}
		}
		startIndex = lastIndex + 1;
		lastIndex = commandLine.indexOf('"', startIndex);
		insideQuotes = !insideQuotes;
	}
	if (startIndex == 0) {
		tokenizer = new StringTokenizer(commandLine);
		while (tokenizer.hasMoreTokens()) {
			if (count == argv.length) {
				System.arraycopy(argv, 0, (argv = new String[count * 2]), 0, count);
			}
			argv[count++] = tokenizer.nextToken();
		}
	} else {
		if (startIndex + 1 <= commandLine.length()) {
			if (insideQuotes) {
				if (count == argv.length) {
					System.arraycopy(argv, 0, (argv = new String[count * 2]), 0, count);
				}
				argv[count++] = commandLine.substring(startIndex, commandLine.length());
			} else {
				tokenizer = new StringTokenizer(commandLine.substring(startIndex, commandLine.length()), File.pathSeparator + " "/*nonNLS*/);
				while (tokenizer.hasMoreTokens()) {
					if (count == argv.length) {
						System.arraycopy(argv, 0, (argv = new String[count * 2]), 0, count);
					}
					argv[count++] = tokenizer.nextToken();
				}
			}
		}
	}
	System.arraycopy(argv, 0, argv = new String[count], 0, count);
	new Main(writer, false).compile(argv);
}
/*
Decode the command line arguments 
 */
private void configure(String[] argv) throws InvalidInputException {
	if ((argv == null) || (argv.length == 0))
		throw new InvalidInputException(Main.bind("configure.noSourceFile"/*nonNLS*/));
	final int InsideClasspath = 1;
	final int InsideDestinationPath = 2;
	final int TargetSetting = 4;
	final int InsideLog = 8;
	final int InsideRepetition = 16;
	final int Default = 0;
	int DEFAULT_SIZE_CLASSPATH = 4;
	boolean noWarnOptionInUsed = false;
	boolean warnOptionInUsed = false;
	int pathCount = 0;
	int index = -1, filesCount = 0, argCount = argv.length;
	int mode = Default;
	repetitions = 0;
	boolean versionIDRequired = false;
	boolean printUsageRequired = false;
	
	while (++index < argCount) {
		String currentArg = argv[index].trim();
		if (currentArg.endsWith(".java"/*nonNLS*/)) {
			if (filenames == null) {
				filenames = new String[argCount - index];
			} else if (filesCount == filenames.length) {
				int length = filenames.length;
				System.arraycopy(filenames, 0, (filenames = new String[length + argCount - index]), 0, length);
			}
			filenames[filesCount++] = currentArg;
			mode = Default;
			continue;
		}
		if (currentArg.equals("-log"/*nonNLS*/)) {
			if (log != null)
				throw new InvalidInputException(Main.bind("configure.duplicateLog"/*nonNLS*/,currentArg));
			mode = InsideLog;
			continue;
		}
		if (currentArg.equals("-repeat"/*nonNLS*/)) {
			if (repetitions > 0)
				throw new InvalidInputException(Main.bind("configure.duplicateRepeat"/*nonNLS*/,currentArg));
			mode = InsideRepetition;
			continue;
		}
		if (currentArg.equals("-d"/*nonNLS*/)) {
			if (destinationPath != null)
				throw new InvalidInputException(Main.bind("configure.duplicateOutputPath"/*nonNLS*/,currentArg));
			mode = InsideDestinationPath;
			continue;
		}
		if (currentArg.equals("-classpath"/*nonNLS*/)) {
			if (pathCount > 0)
				throw new InvalidInputException(Main.bind("configure.duplicateClasspath"/*nonNLS*/,currentArg));
			classpaths = new String[DEFAULT_SIZE_CLASSPATH];
			mode = InsideClasspath;
			continue;
		}
		if (currentArg.equals("-progress"/*nonNLS*/)) {
			mode = Default;
			showProgress = true;
			continue;
		}
		if (currentArg.equals("-proceedOnError"/*nonNLS*/)) {
			mode = Default;
			proceedOnError = true;
			continue;
		}
		if (currentArg.equals("-time"/*nonNLS*/)) {
			mode = Default;
			timer = true;
			continue;
		}
		if (currentArg.equals("-version"/*nonNLS*/) || currentArg.equals("-v"/*nonNLS*/)) {
			versionIDRequired = true;
			continue;
		}
		if (currentArg.equals("-help"/*nonNLS*/)) {
			printUsageRequired = true;
			continue;
		}		
		if (currentArg.equals("-noImportError"/*nonNLS*/)) {
			mode = Default;
			importProblemIsError = false;
			continue;
		}
		if (currentArg.equals("-noExit"/*nonNLS*/)) {
			mode = Default;
			systemExitWhenFinished = false;
			continue;
		}		
		if (currentArg.equals("-verbose"/*nonNLS*/)) {
			mode = Default;
			verbose = true;
			continue;
		}
		if (currentArg.equals("-referenceInfo"/*nonNLS*/)) {
			mode = Default;
			produceRefInfo = true;
			continue;
		}
		if (currentArg.startsWith("-g"/*nonNLS*/)) {
			mode = Default;
			debugMask = 0; // reinitialize the default value
			String debugOption = currentArg;
			int length = currentArg.length();
			if (length == 2) {
				debugMask = CompilerOptions.Lines | CompilerOptions.Vars | CompilerOptions.Source;
				continue;
			}
			if (length > 3) {
				if (length == 7 && debugOption.equals("-g:none"/*nonNLS*/))
					continue;
				StringTokenizer tokenizer = new StringTokenizer(debugOption.substring(3, debugOption.length()), ","/*nonNLS*/);
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					if (token.equals("vars"/*nonNLS*/)) {
						debugMask |= CompilerOptions.Vars;
					} else if (token.equals("lines"/*nonNLS*/)) {
						debugMask |= CompilerOptions.Lines;
					} else if (token.equals("source"/*nonNLS*/)) {
						debugMask |= CompilerOptions.Source;
					} else {
						throw new InvalidInputException(Main.bind("configure.invalidDebugOption"/*nonNLS*/,debugOption));
					}
				}
				continue;
			}
			throw new InvalidInputException(Main.bind("configure.invalidDebugOption"/*nonNLS*/,debugOption));
		}
		if (currentArg.startsWith("-nowarn"/*nonNLS*/)) {
			noWarnOptionInUsed = true;
			if (warnOptionInUsed)
				throw new InvalidInputException(Main.bind("configure.duplicateWarningConfiguration"/*nonNLS*/));
			mode = Default;
			warningMask = TemporaryWarning; // reinitialize the default value (still see TemporaryWarning)		
			continue;
		}
		if (currentArg.startsWith("-warn"/*nonNLS*/)) {
			warnOptionInUsed = true;
			if (noWarnOptionInUsed)
				throw new InvalidInputException(Main.bind("configure.duplicateWarningConfiguration"/*nonNLS*/));
			mode = Default;
			String warningOption = currentArg;
			int length = currentArg.length();
			if (length == 10 && warningOption.equals("-warn:none"/*nonNLS*/)) {
				warningMask = TemporaryWarning; // reinitialize the default value (still see TemporaryWarning)
				continue;
			}
			if (length < 6)
				throw new InvalidInputException(Main.bind("configure.invalidWarningConfiguration"/*nonNLS*/,warningOption));
			StringTokenizer tokenizer = new StringTokenizer(warningOption.substring(6, warningOption.length()), ","/*nonNLS*/);
			int tokenCounter = 0;
			warningMask = 0; // reinitialize the default value				
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				tokenCounter++;
				if (token.equals("constructorName"/*nonNLS*/)) {
					warningMask |= CompilerOptions.MethodWithConstructorName;
				} else if (token.equals("packageDefaultMethod"/*nonNLS*/)) {
					warningMask |= CompilerOptions.OverriddenPackageDefaultMethod;
				} else if (token.equals("maskedCatchBlocks"/*nonNLS*/)) {
					warningMask |= CompilerOptions.MaskedCatchBlock;
				} else if (token.equals("deprecation"/*nonNLS*/)) {
					warningMask |= CompilerOptions.UsingDeprecatedAPI;
				} else if (token.equals("unusedLocals"/*nonNLS*/)) {
					warningMask |= CompilerOptions.UnusedLocalVariable;
				} else if (token.equals("unusedArguments"/*nonNLS*/)) {
					warningMask |= CompilerOptions.UnusedArgument;
				} else if (token.equals("syntheticAccess"/*nonNLS*/)){
					warningMask |= CompilerOptions.AccessEmulation;
				} else if (token.equals("nls"/*nonNLS*/)){
					warningMask |= CompilerOptions.NonExternalizedString;
				} else {
					throw new InvalidInputException(Main.bind("configure.invalidWarning"/*nonNLS*/,token));
				}
			}
			if (tokenCounter == 0)
				throw new InvalidInputException(Main.bind("configure.invalidWarningOption"/*nonNLS*/,currentArg));
			continue;
		}
		if (currentArg.equals("-target"/*nonNLS*/)) {
			mode = TargetSetting;
			continue;
		}
		if (currentArg.equals("-preserveAllLocals"/*nonNLS*/)) {
			preserveAllLocalVariables = true;
			continue;
		}
		if (mode == TargetSetting) {
			if (currentArg.equals("1.1"/*nonNLS*/)) {
				targetJDK = CompilerOptions.JDK1_1;
			} else if (currentArg.equals("1.2"/*nonNLS*/)) {
				targetJDK = CompilerOptions.JDK1_2;
			} else {
				throw new InvalidInputException(Main.bind("configure.targetJDK"/*nonNLS*/,currentArg));
			}
			mode = Default;
			continue;
		}
		if (mode == InsideLog){
			log = currentArg;
			mode = Default;
			continue;
		}
		if (mode == InsideRepetition){
			try {
				repetitions = Integer.parseInt(currentArg);
				if (repetitions <= 0){
					throw new InvalidInputException(Main.bind("configure.repetition"/*nonNLS*/,currentArg));
				}
			} catch(NumberFormatException e){
				throw new InvalidInputException(Main.bind("configure.repetition"/*nonNLS*/,currentArg));
			}
			mode = Default;
			continue;
		}
		if (mode == InsideDestinationPath) {
			destinationPath = currentArg;
			mode = Default;
			continue;
		}
		if (mode == InsideClasspath) {
			StringTokenizer tokenizer = new StringTokenizer(currentArg, File.pathSeparator);
			while (tokenizer.hasMoreTokens()) {
				int length;
				if ((length = classpaths.length) <= pathCount) {
					System.arraycopy(classpaths, 0, (classpaths = new String[length * 2]), 0, length);
				}
				classpaths[pathCount++] = tokenizer.nextToken();
			}
			mode = Default;
			continue;
		}
		//default is input directory
		currentArg = currentArg.replace('/', File.separatorChar);
		if (currentArg.endsWith(File.separator))
			currentArg = currentArg.substring(0, currentArg.length() - File.separator.length());
		File dir = new File(currentArg);
		if (!dir.isDirectory())
			throw new InvalidInputException(Main.bind("configure.directoryNotExist"/*nonNLS*/,currentArg));
		FileFinder finder = new FileFinder();
		try{
			finder.find(dir, ".JAVA"/*nonNLS*/, verbose);
		} catch(Exception e){
			throw new InvalidInputException(Main.bind("configure.IOError"/*nonNLS*/,currentArg));		
		}
		if (filenames != null) {
			// some source files were specified explicitly
			String results[] = finder.resultFiles;
			int length = results.length;
			System.arraycopy(filenames, 0, (filenames = new String[length + filesCount]), 0, filesCount);
			System.arraycopy(results, 0, filenames, filesCount, length);
			filesCount += length;
		} else {
			filenames = finder.resultFiles;
			filesCount = filenames.length;
		}
		mode = Default;
		continue;
	}

	/*
	 * Standalone options
	 */
	if (versionIDRequired) {
		out.println(Main.bind("configure.version"/*nonNLS*/,this.versionID));
		out.println();
		return;
	}
		
	if (printUsageRequired) {
		printUsage();
		return;
	}	
	
	if (filesCount != 0)
		System.arraycopy(filenames, 0, (filenames = new String[filesCount]), 0, filesCount);
	if (pathCount == 0) {
		String classProp = System.getProperty("LFclasspath"/*nonNLS*/);
		if ((classProp == null) || (classProp.length() == 0)) {
			out.println(Main.bind("configure.noClasspath"/*nonNLS*/));
			classProp = "."/*nonNLS*/;
		}
		StringTokenizer tokenizer = new StringTokenizer(classProp, File.pathSeparator);
		classpaths = new String[tokenizer.countTokens()];
		while (tokenizer.hasMoreTokens()) {
			classpaths[pathCount++] = tokenizer.nextToken();
		}
	}

	if (classpaths == null)
		classpaths = new String[0];
	System.arraycopy(classpaths, 0, (classpaths = new String[pathCount]), 0, pathCount);
	for (int i = 0, max = classpaths.length; i < max; i++) {
		File file = new File(classpaths[i]);
		if (!file.exists())
			throw new InvalidInputException(Main.bind("configure.incorrectClasspath"/*nonNLS*/,classpaths[i]));
	}
	if (destinationPath == null) {
		destinationPath = System.getProperty("user.dir"/*nonNLS*/);
	} else if ("none"/*nonNLS*/.equals(destinationPath)) {
		destinationPath = null;
	}
		
	if (filenames == null)
		throw new InvalidInputException(Main.bind("configure.noSource"/*nonNLS*/));

	if (log != null){
		try {
			out = new PrintWriter(new FileOutputStream(log, false));
		} catch(IOException e){
			throw new InvalidInputException(Main.bind("configure.cannotOpenLog"/*nonNLS*/));
		}
	} else {
		showProgress = false;
	}
	if (repetitions == 0) {
		repetitions = 1;
	}
}
/*
 * Answer the component to which will be handed back compilation results from the compiler
 */
protected ICompilerRequestor getBatchRequestor() {
	return new ICompilerRequestor() {
		int lineDelta = 0;
		public void acceptResult(CompilationResult compilationResult) {
			if (compilationResult.lineSeparatorPositions != null){
				int unitLineCount = compilationResult.lineSeparatorPositions.length;
				lineCount += unitLineCount;
				lineDelta += unitLineCount;
				if (showProgress && lineDelta > 2000){ // in -log mode, dump a dot every 2000 lines compiled
					System.out.print('.');
					lineDelta = 0;
				}
			}
			if (compilationResult.hasProblems()) {
				IProblem[] problems = compilationResult.getProblems();
				int count = problems.length;
				int localErrorCount = 0;
				for (int i = 0; i < count; i++) { 
					if (problems[i] != null) {
						globalProblemsCount++;
						if (localErrorCount == 0)
							out.println("----------"/*nonNLS*/);
						out.print(globalProblemsCount + ". "/*nonNLS*/ + (problems[i].isError() ? Main.bind("requestor.error"/*nonNLS*/) : Main.bind("requestor.warning"/*nonNLS*/)));
						if (problems[i].isError()) {
							globalErrorsCount++;
						} else {
							globalWarningsCount++;
						}
						out.print(" "/*nonNLS*/);
						out.print(Main.bind("requestor.in"/*nonNLS*/,new String(problems[i].getOriginatingFileName())));
						try {
							out.println(((DefaultProblem)problems[i]).errorReportSource(compilationResult.compilationUnit));
							out.println(problems[i].getMessage());
						} catch (Exception e) {
							out.println(Main.bind("requestor.notRetrieveErrorMessage"/*nonNLS*/,problems[i].toString()));
						}
						out.println("----------"/*nonNLS*/);
						if (problems[i].isError())
							localErrorCount++;
					}
				};
				// exit?
				if (systemExitWhenFinished && !proceedOnError && (localErrorCount > 0)) {
					out.flush();
					System.exit(-1);
				}
			}
			outputClassFiles(compilationResult);
		}
	};
}
/*
 *  Build the set of compilation source units
 */
protected CompilationUnit[] getCompilationUnits() throws InvalidInputException {
	int fileCount = filenames.length;
	CompilationUnit[] units = new CompilationUnit[fileCount];

	HashtableOfObject knownFileNames = new HashtableOfObject(fileCount);
	
	for (int i = 0; i < fileCount; i++) {
		char[] charName = filenames[i].toCharArray();
		if (knownFileNames.get(charName) != null){
			throw new InvalidInputException(Main.bind("unit.more"/*nonNLS*/,filenames[i]));			
		} else {
			knownFileNames.put(charName, charName);
		}
		File file = new File(filenames[i]);
		if (!file.exists())
			throw new InvalidInputException(Main.bind("unit.missing"/*nonNLS*/,filenames[i]));
		units[i] = new CompilationUnit(null, filenames[i]);
	}
	return units;
}
/*
 *  Low-level API performing the actual compilation
 */
protected IErrorHandlingPolicy getHandlingPolicy() {

	// passes the initial set of files to the batch oracle (to avoid finding more than once the same units when case insensitive match)	
	return new IErrorHandlingPolicy() {
		public boolean stopOnFirstError() {
			return false;
		}
		public boolean proceedOnErrors() {
			return proceedOnError; // stop if there are some errors 
		}
	};
}
/*
 *  Low-level API performing the actual compilation
 */
protected FileSystem getLibraryAccess() {
	return new FileSystem(classpaths, filenames);
}
/*
 *  Low-level API performing the actual compilation
 */
protected ConfigurableOption[] getOptions() {
	CompilerOptions options = new CompilerOptions();
	options.produceDebugAttributes(debugMask);
	options.preserveAllLocalVariables(preserveAllLocalVariables);
	options.handleImportProblemAsError(importProblemIsError);
	options.setWarningThreshold(warningMask);
	options.setTargetJDK(targetJDK);
	return options.getConfigurableOptions(Locale.getDefault());
}
protected IProblemFactory getProblemFactory() {
	return new DefaultProblemFactory(Locale.getDefault());
}
/*
 * External API
 */

public static void main(String[] argv) {
	new Main(new PrintWriter(System.out), true).compile(argv);
}
// Dump classfiles onto disk for all compilation units that where successfull.

protected void outputClassFiles(CompilationResult unitResult) {

	if (!((unitResult == null) || (unitResult.hasErrors() && !proceedOnError))) {
		Enumeration classFiles = unitResult.compiledTypes.elements();
		if (destinationPath != null) {
			while (classFiles.hasMoreElements()) {
				// retrieve the key and the corresponding classfile
				ClassFile classFile = (ClassFile) classFiles.nextElement();
				char[] filename = classFile.fileName();
				int length = filename.length;
				char[] relativeName = new char[length + 6];
				System.arraycopy(filename, 0, relativeName, 0, length);
				System.arraycopy(CLASS_FILE_EXTENSION, 0, relativeName, length, 6);
				CharOperation.replace(relativeName, '/', File.separatorChar);
				try {
					ClassFile.writeToDisk(
						destinationPath,
						new String(relativeName),
						classFile.getBytes());
				} catch (IOException e) {
					String fileName = destinationPath + new String(relativeName);
					e.printStackTrace();
					System.out.println(Main.bind("output.noClassFileCreated"/*nonNLS*/,fileName));
				}
				exportedClassFilesCounter++;
			}
		}
	}
}
/*
 *  Low-level API performing the actual compilation
 */
protected void performCompilation() throws InvalidInputException {
	Compiler batchCompiler =
			new Compiler(
				getLibraryAccess(),
				getHandlingPolicy(),
				getOptions(),
		 		getBatchRequestor(),
				getProblemFactory());

	CompilerOptions options = batchCompiler.options;
	// set the non-externally configurable options.
	options.setVerboseMode(verbose);
	options.produceReferenceInfo(produceRefInfo);
	
	batchCompiler.compile(getCompilationUnits());
}
private void printUsage() {
	out.println(Main.bind("misc.usage"/*nonNLS*/,this.versionID));
	/*out.println(
		"Eclipse Java Compiler "+ this.versionID + ", Copyright IBM Corp 2000\n\n" +
		"Usage: <options> <source files | directories>\n\n" +
					"where options include:\n" +
					"-version or -v\tdisplays the version number (standalone option)\n" +
					"-help\tdisplay this help message (standalone option)\n" +
					"-noExit\tPrevent the compiler to call System.exit at the end of the compilation process\n" +					
					"-classpath <dir 1>;<dir 2>;...;<dir P>\n" +
					"-d <dir>\tdestination directory\n\t\t, specified '-d none' if you don't want to dump files\n" +
					"-verbose\tprint accessed/processed compilation units \n" +
					"-time\t\tdisplay total compilation time" +
							"\n\t\tand speed if line attributes are enabled\n" +
					"-log <filename>\tspecify a log file for recording problems\n" +
					"-progress\t\tshow progress (only in -log mode)\n" +
					"-g[:<level>]\tspecify the level of details for debug attributes" +
							"\n\t\t-g\tgenerate all debug info"+
							"\n\t\t-g:none\tno debug info"+
							"\n\t\t-g:{lines,vars,source}\tonly some debug info\n" +
					"-nowarn\t\tdo not report warnings \n" +
					"-warn:<mask>\tspecify the level of details for warnings\n" +
							"\t\t-warn:none no warning\n"+
							"\t\t-warn:{constructorName, packageDefaultMethod, deprecation,\n" +
							"\t\t\tmaskedCatchBlocks, unusedLocals, unusedArguments, \n" +
							"\t\t\tsyntheticAccess}\n" +					
					"-noImportError\tdo not report errors on incorrect imports\n" +
					"-proceedOnError\tkeep compiling when error, \n\t\tdumping class files with problem methods\n" +
					"-referenceInfo\tcompute reference info\n" +
					"-preserveAllLocals\trequest code gen preserve all local variables\n" +
					"-repeat <n>\trepeat compilation process for performance analysis\n");*/
	out.flush();
}

/**
 * Creates a NLS catalog for the given locale.
 */
public static void relocalize() {
	bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault());
}

/**
 * Lookup the message with the given ID in this catalog 
 */
public static String bind(String id) {
	return bind(id, (String[])null);
}

/**
 * Lookup the message with the given ID in this catalog and bind its
 * substitution locations with the given string values.
 */
public static String bind(String id, String[] bindings) {
	if (id == null)
		return "No message available"/*nonNLS*/;
	String message = null;
	try {
		message = bundle.getString(id);
	} catch (MissingResourceException e) {
		// If we got an exception looking for the message, fail gracefully by just returning
		// the id we were looking for.  In most cases this is semi-informative so is not too bad.
		return "Missing message: "/*nonNLS*/+id+" in: "/*nonNLS*/+bundleName;
	}
	if (bindings == null)
		return message;
	int length = message.length();
	int start = -1;
	int end = length;
	StringBuffer output = new StringBuffer(80);
	while (true) {
		if ((end = message.indexOf('{', start)) > -1) {
			output.append(message.substring(start + 1, end));
			if ((start = message.indexOf('}', end)) > -1) {
				int index = -1;
				try {
					index = Integer.parseInt(message.substring(end + 1, start));
					output.append(bindings[index]);
				} catch (NumberFormatException nfe) {
					output.append(message.substring(end + 1, start + 1));
				} catch (ArrayIndexOutOfBoundsException e) {
					output.append("{missing "/*nonNLS*/ + Integer.toString(index) + "}"/*nonNLS*/);
				}
			} else {
				output.append(message.substring(end, length));
				break;
			}
		} else {
			output.append(message.substring(start + 1, length));
			break;
		}
	}
	return output.toString();
}

/**
 * Lookup the message with the given ID in this catalog and bind its
 * substitution locations with the given string.
 */
public static String bind(String id, String binding) {
	return bind(id, new String[] {binding});
}

/**
 * Lookup the message with the given ID in this catalog and bind its
 * substitution locations with the given strings.
 */
public static String bind(String id, String binding1, String binding2) {
	return bind(id, new String[] {binding1, binding2});
}

}
