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
	private ConfigurableOption[] options;
	private static final String[] problemOption ={
		CompilerOptions.OPTION_ReportMethodWithConstructorName,
		CompilerOptions.OPTION_ReportHiddenCatchBlock,
		CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod,
		CompilerOptions.OPTION_ReportDeprecation,
		CompilerOptions.OPTION_ReportUnusedLocal,
		CompilerOptions.OPTION_ReportUnusedParameter,
		CompilerOptions.OPTION_ReportSyntheticAccessEmulation,
		CompilerOptions.OPTION_ReportNonExternalizedStringLiteral,
		CompilerOptions.OPTION_ReportInvalidImport,
		CompilerOptions.OPTION_ReportUnreachableCode,
		CompilerOptions.OPTION_ReportAssertIdentifier,
	};
	private boolean noWarn = false;
	
	PrintWriter out;
	boolean systemExitWhenFinished = true;
	boolean proceedOnError = false;
			
	boolean verbose = false;
	boolean produceRefInfo = false;
	boolean timer = false;
	boolean showProgress = false;
	public long time = 0;
	long lineCount;

	String[] filenames;
	String[] classpaths;
	String destinationPath;
	String log;
	int repetitions;
	int globalProblemsCount;
	int globalErrorsCount;
	int globalWarningsCount;

	String versionID = "0.125.12 (jck1.3a)"; //$NON-NLS-1$
	private static final char[] CLASS_FILE_EXTENSION = ".class".toCharArray(); //$NON-NLS-1$

	int exportedClassFilesCounter;

	/**
	 * Are we running JDK 1.1?
	 */
	private static boolean JDK1_1 = false;

	/* Bundle containing messages */
	protected static ResourceBundle bundle;
	private final static String bundleName = "org.eclipse.jdt.internal.compiler.batch.Messages"; //$NON-NLS-1$
	static {
		String ver = System.getProperty("java.version"); //$NON-NLS-1$
		JDK1_1 = ((ver != null) && ver.startsWith("1.1")); //$NON-NLS-1$
		relocalize();
	}
	
	private boolean proceed = true;
	
protected Main(PrintWriter writer, boolean systemExitWhenFinished) {
	this.out = writer;
	this.systemExitWhenFinished = systemExitWhenFinished;
	exportedClassFilesCounter = 0;
	options = Compiler.getDefaultOptions(Locale.getDefault());
}
/*
 *  Low-level API performing the actual compilation
 */
protected void compile(String[] argv) {
	// decode command line arguments
	try {
		configure(argv);
		if(proceed){
			if (showProgress) out.print(Main.bind("progress.compiling")); //$NON-NLS-1$
			for (int i = 0; i < repetitions; i++){
				globalProblemsCount = 0;
				globalErrorsCount = 0;
				globalWarningsCount = 0;		
				lineCount = 0;
	
				if (repetitions > 1){
					out.flush();
					out.println(Main.bind("compile.repetition",String.valueOf(i+1),String.valueOf(repetitions))); //$NON-NLS-1$
				}
				long startTime = System.currentTimeMillis();
				// request compilation
				performCompilation();
				if (timer) {
	
					time = System.currentTimeMillis() - startTime;
					if (lineCount != 0){
						out.println(Main.bind("compile.instantTime",new String[]{String.valueOf(lineCount),String.valueOf(time),String.valueOf((((int)((lineCount*10000.0)/time))/10.0))})); //$NON-NLS-1$
					} else {
						out.println(Main.bind("compile.totalTime",String.valueOf(time)));				 //$NON-NLS-1$
					}
				}
				if (globalProblemsCount > 0) {
					if (globalProblemsCount == 1) {
						out.print(Main.bind("compile.oneProblem")); //$NON-NLS-1$
					} else {
						out.print(Main.bind("compile.severalProblems",String.valueOf(globalProblemsCount))); //$NON-NLS-1$
					}
					out.print(" ("); //$NON-NLS-1$
					if (globalErrorsCount > 0) {
						if (globalErrorsCount == 1) {
							out.print(Main.bind("compile.oneError")); //$NON-NLS-1$
						} else {
							out.print(Main.bind("compile.severalErrors",String.valueOf(globalErrorsCount))); //$NON-NLS-1$
						}
					}
					if (globalWarningsCount > 0) {
						if (globalErrorsCount > 0) {
							out.print(", "); //$NON-NLS-1$
						}
						if (globalWarningsCount == 1) {
							out.print(Main.bind("compile.oneWarning")); //$NON-NLS-1$
						} else {
							out.print(Main.bind("compile.severalWarnings",String.valueOf(globalWarningsCount))); //$NON-NLS-1$
						}
					}
					out.println(")"); //$NON-NLS-1$
				}
				if (exportedClassFilesCounter != 0 && (this.showProgress || this.timer || this.verbose)) {
					if (exportedClassFilesCounter == 1) {
						out.print(Main.bind("compile.oneClassFileGenerated")); //$NON-NLS-1$
					} else {
						out.print(Main.bind("compile.severalClassFilesGenerated",String.valueOf(exportedClassFilesCounter))); //$NON-NLS-1$
					}
				}
			}
			if (showProgress) System.out.println();
		}
		if (systemExitWhenFinished){
			out.flush();
			System.exit(globalErrorsCount > 0 ? -1 : 0);
		}
	} catch (InvalidInputException e) {
		out.println(e.getMessage());
		out.println("------------------------"); //$NON-NLS-1$
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
				tokenizer = new StringTokenizer(subCommandLine, File.pathSeparator + " "); //$NON-NLS-1$
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
				tokenizer = new StringTokenizer(commandLine.substring(startIndex, commandLine.length()), File.pathSeparator + " "); //$NON-NLS-1$
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
private void setOptionValueIndex(String id,int valueIndex){
	for(int i = 0 ; i < options.length ; i++){
		if(options[i].getID().equals(id)){
			options[i].setValueIndex(valueIndex);
			return;
		}
	}
}

/*
Decode the command line arguments 
 */
private void configure(String[] argv) throws InvalidInputException {
	if ((argv == null) || (argv.length == 0))
		throw new InvalidInputException(Main.bind("configure.noSourceFile")); //$NON-NLS-1$
	final int InsideClasspath = 1;
	final int InsideDestinationPath = 2;
	final int TargetSetting = 4;
	final int InsideLog = 8;
	final int InsideRepetition = 16;
	final int InsideSource = 32;
	final int Default = 0;
	int DEFAULT_SIZE_CLASSPATH = 4;
	boolean warnOptionInUsed = false;
	boolean noWarnOptionInUsed = false;
	int pathCount = 0;
	int index = -1, filesCount = 0, argCount = argv.length;
	int mode = Default;
	repetitions = 0;
	boolean versionIDRequired = false;
	boolean printUsageRequired = false;
	
	while (++index < argCount) {
		String currentArg = argv[index].trim();
		if (currentArg.endsWith(".java")) { //$NON-NLS-1$
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
		if (currentArg.equals("-log")) { //$NON-NLS-1$
			if (log != null)
				throw new InvalidInputException(Main.bind("configure.duplicateLog",currentArg)); //$NON-NLS-1$
			mode = InsideLog;
			continue;
		}
		if (currentArg.equals("-repeat")) { //$NON-NLS-1$
			if (repetitions > 0)
				throw new InvalidInputException(Main.bind("configure.duplicateRepeat",currentArg)); //$NON-NLS-1$
			mode = InsideRepetition;
			continue;
		}
		if (currentArg.equals("-source")) { //$NON-NLS-1$
			mode = InsideSource;
			continue;
		}
		if (currentArg.equals("-d")) { //$NON-NLS-1$
			if (destinationPath != null)
				throw new InvalidInputException(Main.bind("configure.duplicateOutputPath",currentArg)); //$NON-NLS-1$
			mode = InsideDestinationPath;
			continue;
		}
		if (currentArg.equals("-classpath")) { //$NON-NLS-1$
			if (pathCount > 0)
				throw new InvalidInputException(Main.bind("configure.duplicateClasspath",currentArg)); //$NON-NLS-1$
			classpaths = new String[DEFAULT_SIZE_CLASSPATH];
			mode = InsideClasspath;
			continue;
		}
		if (currentArg.equals("-progress")) { //$NON-NLS-1$
			mode = Default;
			showProgress = true;
			continue;
		}
		if (currentArg.equals("-proceedOnError")) { //$NON-NLS-1$
			mode = Default;
			proceedOnError = true;
			continue;
		}
		if (currentArg.equals("-time")) { //$NON-NLS-1$
			mode = Default;
			timer = true;
			continue;
		}
		if (currentArg.equals("-version") || currentArg.equals("-v")) { //$NON-NLS-1$ //$NON-NLS-2$
			versionIDRequired = true;
			continue;
		}
		if (currentArg.equals("-help")) { //$NON-NLS-1$
			printUsageRequired = true;
			continue;
		}		
		if (currentArg.equals("-noImportError")) { //$NON-NLS-1$
			mode = Default;
			setOptionValueIndex("org.eclipse.jdt.internal.compiler.Compiler.problemInvalidImport",2); //$NON-NLS-1$
			continue;
		}
		if (currentArg.equals("-noExit")) { //$NON-NLS-1$
			mode = Default;
			systemExitWhenFinished = false;
			continue;
		}		
		if (currentArg.equals("-verbose")) { //$NON-NLS-1$
			mode = Default;
			verbose = true;
			continue;
		}
		if (currentArg.equals("-referenceInfo")) { //$NON-NLS-1$
			mode = Default;
			produceRefInfo = true;
			continue;
		}
		if (currentArg.startsWith("-g")) { //$NON-NLS-1$
			mode = Default;
			String debugOption = currentArg;
			int length = currentArg.length();
			if (length == 2) {
				setOptionValueIndex(CompilerOptions.OPTION_LocalVariableAttribute, 0);
				setOptionValueIndex(CompilerOptions.OPTION_LineNumberAttribute, 0);
				setOptionValueIndex(CompilerOptions.OPTION_SourceFileAttribute, 0);
				continue;
			}
			if (length > 3) {
				setOptionValueIndex(CompilerOptions.OPTION_LocalVariableAttribute, 1);
				setOptionValueIndex(CompilerOptions.OPTION_LineNumberAttribute, 1);
				setOptionValueIndex(CompilerOptions.OPTION_SourceFileAttribute, 1);				
				if (length == 7 && debugOption.equals("-g:none")) //$NON-NLS-1$
					continue;
				StringTokenizer tokenizer = new StringTokenizer(debugOption.substring(3, debugOption.length()), ","); //$NON-NLS-1$
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					if (token.equals("vars")) { //$NON-NLS-1$
						setOptionValueIndex(CompilerOptions.OPTION_LocalVariableAttribute, 0);
					} else if (token.equals("lines")) { //$NON-NLS-1$
						setOptionValueIndex(CompilerOptions.OPTION_LineNumberAttribute, 0);
					} else if (token.equals("source")) { //$NON-NLS-1$
						setOptionValueIndex(CompilerOptions.OPTION_SourceFileAttribute, 0);
					} else {
						throw new InvalidInputException(Main.bind("configure.invalidDebugOption",debugOption)); //$NON-NLS-1$
					}
				}
				continue;
			}
			throw new InvalidInputException(Main.bind("configure.invalidDebugOption",debugOption)); //$NON-NLS-1$
		}
		if (currentArg.startsWith("-nowarn")) { //$NON-NLS-1$
			noWarnOptionInUsed = true;
			noWarn = true;
			if (warnOptionInUsed)
				throw new InvalidInputException(Main.bind("configure.duplicateWarningConfiguration")); //$NON-NLS-1$
			mode = Default;		
			continue;
		}
		if (currentArg.startsWith("-warn")) { //$NON-NLS-1$
			warnOptionInUsed = true;
			if (noWarnOptionInUsed)
				throw new InvalidInputException(Main.bind("configure.duplicateWarningConfiguration")); //$NON-NLS-1$
			mode = Default;
			String warningOption = currentArg;
			int length = currentArg.length();
			if (length == 10 && warningOption.equals("-warn:none")) { //$NON-NLS-1$
				noWarn = true;
				continue;
			}
			if (length < 6)
				throw new InvalidInputException(Main.bind("configure.invalidWarningConfiguration",warningOption)); //$NON-NLS-1$
			StringTokenizer tokenizer = new StringTokenizer(warningOption.substring(6, warningOption.length()), ","); //$NON-NLS-1$
			int tokenCounter = 0;

			setOptionValueIndex(CompilerOptions.OPTION_ReportMethodWithConstructorName, 2);
			setOptionValueIndex(CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod, 2);
			setOptionValueIndex(CompilerOptions.OPTION_ReportHiddenCatchBlock, 2);
			setOptionValueIndex(CompilerOptions.OPTION_ReportDeprecation, 2);
			setOptionValueIndex(CompilerOptions.OPTION_ReportUnusedLocal, 2);
			setOptionValueIndex(CompilerOptions.OPTION_ReportUnusedParameter, 2);
			setOptionValueIndex(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, 2);
			setOptionValueIndex(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, 2);
			setOptionValueIndex(CompilerOptions.OPTION_ReportAssertIdentifier, 2);
			
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				tokenCounter++;
				if (token.equals("constructorName")) { //$NON-NLS-1$
					setOptionValueIndex(CompilerOptions.OPTION_ReportMethodWithConstructorName, 1);
				} else if (token.equals("packageDefaultMethod")) { //$NON-NLS-1$
					setOptionValueIndex(CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod, 1);
				} else if (token.equals("maskedCatchBlocks")) { //$NON-NLS-1$
					setOptionValueIndex(CompilerOptions.OPTION_ReportHiddenCatchBlock, 1);
				} else if (token.equals("deprecation")) { //$NON-NLS-1$
					setOptionValueIndex(CompilerOptions.OPTION_ReportDeprecation, 1);
				} else if (token.equals("unusedLocals")) { //$NON-NLS-1$
					setOptionValueIndex(CompilerOptions.OPTION_ReportUnusedLocal, 1);
				} else if (token.equals("unusedArguments")) { //$NON-NLS-1$
					setOptionValueIndex(CompilerOptions.OPTION_ReportUnusedParameter, 1);
				} else if (token.equals("syntheticAccess")){ //$NON-NLS-1$
					setOptionValueIndex(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, 1);
				} else if (token.equals("nls")){ //$NON-NLS-1$
					setOptionValueIndex(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, 1);
				} else if (token.equals("assertIdentifier")){ //$NON-NLS-1$
					setOptionValueIndex(CompilerOptions.OPTION_ReportAssertIdentifier, 1);
				} else {
					throw new InvalidInputException(Main.bind("configure.invalidWarning",token)); //$NON-NLS-1$
				}
			}
			if (tokenCounter == 0)
				throw new InvalidInputException(Main.bind("configure.invalidWarningOption",currentArg)); //$NON-NLS-1$
			continue;
		}
		if (currentArg.equals("-target")) { //$NON-NLS-1$
			mode = TargetSetting;
			continue;
		}
		if (currentArg.equals("-preserveAllLocals")) { //$NON-NLS-1$
			setOptionValueIndex(CompilerOptions.OPTION_PreserveUnusedLocal, 0);
			continue;
		}
		if (mode == TargetSetting) {
			if (currentArg.equals("1.1")) { //$NON-NLS-1$
				setOptionValueIndex(CompilerOptions.OPTION_TargetPlatform, 0);
			} else if (currentArg.equals("1.2")) { //$NON-NLS-1$
				setOptionValueIndex(CompilerOptions.OPTION_TargetPlatform, 1);
			} else {
				throw new InvalidInputException(Main.bind("configure.targetJDK",currentArg)); //$NON-NLS-1$
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
					throw new InvalidInputException(Main.bind("configure.repetition",currentArg)); //$NON-NLS-1$
				}
			} catch(NumberFormatException e){
				throw new InvalidInputException(Main.bind("configure.repetition",currentArg)); //$NON-NLS-1$
			}
			mode = Default;
			continue;
		}
		if (mode == InsideSource){
			if (currentArg.equals("1.3")) { //$NON-NLS-1$
				setOptionValueIndex(CompilerOptions.OPTION_Source, 0);
			} else if (currentArg.equals("1.4")) { //$NON-NLS-1$
				setOptionValueIndex(CompilerOptions.OPTION_Source, 1);
			} else {
				throw new InvalidInputException(Main.bind("configure.source",currentArg)); //$NON-NLS-1$
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
			throw new InvalidInputException(Main.bind("configure.directoryNotExist",currentArg)); //$NON-NLS-1$
		FileFinder finder = new FileFinder();
		try{
			finder.find(dir, ".JAVA", verbose); //$NON-NLS-1$
		} catch(Exception e){
			throw new InvalidInputException(Main.bind("configure.IOError",currentArg));		 //$NON-NLS-1$
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

	if(noWarn){
		for(int i = 0; i < problemOption.length ; i++){
			for(int j = 0 ; j < options.length ; j++){
				if(options[j].getID().equals(problemOption[i]) && options[j].getValueIndex() == 1){
					options[j].setValueIndex(2);
				}
			}
		}
	}
	/*
	 * Standalone options
	 */
	if (versionIDRequired) {
		out.println(Main.bind("configure.version",this.versionID)); //$NON-NLS-1$
		out.println();
		proceed = false;
		return;
	}
		
	if (printUsageRequired) {
		printUsage();
		proceed = false;
		return;
	}	
	
	if (filesCount != 0)
		System.arraycopy(filenames, 0, (filenames = new String[filesCount]), 0, filesCount);
	if (pathCount == 0) {
		String classProp = System.getProperty("LFclasspath"); //$NON-NLS-1$
		if ((classProp == null) || (classProp.length() == 0)) {
			out.println(Main.bind("configure.noClasspath")); //$NON-NLS-1$
			classProp = "."; //$NON-NLS-1$
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
			throw new InvalidInputException(Main.bind("configure.incorrectClasspath",classpaths[i])); //$NON-NLS-1$
	}
	if (destinationPath == null) {
		destinationPath = System.getProperty("user.dir"); //$NON-NLS-1$
	} else if ("none".equals(destinationPath)) { //$NON-NLS-1$
		destinationPath = null;
	}
		
	if (filenames == null)
		throw new InvalidInputException(Main.bind("configure.noSource")); //$NON-NLS-1$

	if (log != null){
		try {
			out = new PrintWriter(new FileOutputStream(log, false));
		} catch(IOException e){
			throw new InvalidInputException(Main.bind("configure.cannotOpenLog")); //$NON-NLS-1$
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
							out.println("----------"); //$NON-NLS-1$
						out.print(globalProblemsCount + ". " + (problems[i].isError() ? Main.bind("requestor.error") : Main.bind("requestor.warning"))); //$NON-NLS-3$ //$NON-NLS-2$ //$NON-NLS-1$
						if (problems[i].isError()) {
							globalErrorsCount++;
						} else {
							globalWarningsCount++;
						}
						out.print(" "); //$NON-NLS-1$
						out.print(Main.bind("requestor.in",new String(problems[i].getOriginatingFileName()))); //$NON-NLS-1$
						try {
							out.println(((DefaultProblem)problems[i]).errorReportSource(compilationResult.compilationUnit));
							out.println(problems[i].getMessage());
						} catch (Exception e) {
							out.println(Main.bind("requestor.notRetrieveErrorMessage",problems[i].toString())); //$NON-NLS-1$
						}
						out.println("----------"); //$NON-NLS-1$
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
			throw new InvalidInputException(Main.bind("unit.more",filenames[i]));			 //$NON-NLS-1$
		} else {
			knownFileNames.put(charName, charName);
		}
		File file = new File(filenames[i]);
		if (!file.exists())
			throw new InvalidInputException(Main.bind("unit.missing",filenames[i])); //$NON-NLS-1$
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
	return options;
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
					System.out.println(Main.bind("output.noClassFileCreated",fileName)); //$NON-NLS-1$
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
	out.println(Main.bind("misc.usage",this.versionID)); //$NON-NLS-1$
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
		return "No message available"; //$NON-NLS-1$
	String message = null;
	try {
		message = bundle.getString(id);
	} catch (MissingResourceException e) {
		// If we got an exception looking for the message, fail gracefully by just returning
		// the id we were looking for.  In most cases this is semi-informative so is not too bad.
		return "Missing message: "+id+" in: "+bundleName; //$NON-NLS-2$ //$NON-NLS-1$
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
					output.append("{missing " + Integer.toString(index) + "}"); //$NON-NLS-2$ //$NON-NLS-1$
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
