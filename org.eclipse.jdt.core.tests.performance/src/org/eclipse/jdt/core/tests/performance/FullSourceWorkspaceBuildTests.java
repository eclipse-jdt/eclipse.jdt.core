/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.performance;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.SourceElementRequestorAdapter;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 * Class to test compiler performance.
 * This includes tests on build, batch compiler, Scanner and Parser.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class FullSourceWorkspaceBuildTests extends FullSourceWorkspaceTests {

	// Tests counters
	private static int TESTS_COUNT = 0;
	private final static int ITERATIONS_COUNT = 10;
	private final static int WARMUP_COUNT = 3;
	private final static int SCAN_REPEAT = 800; // 800 is default

	// Tests thresholds
	private final static int TIME_THRESHOLD = 150;

	// Log files
	private static PrintStream[] LOG_STREAMS = new PrintStream[DIM_NAMES.length];

	// Options
	private static final String ALL_OPTIONS = "-warn:" +
			"allDeprecation," +
			"allJavadoc," +
			"assertIdentifier," +
			"charConcat," +
			"conditionAssign," +
			"constructorName," +
			"deprecation," +
			"emptyBlock," +
			"fieldHiding," +
			"finally," +
			"indirectStatic," +
			"intfNonInherited," +
			"localHiding," +
			"maskedCatchBlock," +
			"nls," +
			"noEffectAssign," +
			"pkgDefaultMethod," +
			"semicolon," +
			"unqualifiedField," +
			"unusedArgument," +
			"unusedImport," +
			"unusedLocal," +
			"unusedPrivate," +
			"unusedThrown," +
			"unnecessaryElse," +
			"uselessTypeCheck," +
			"specialParamHiding," +
			"staticReceiver," +
			"syntheticAccess," +
			"tasks(TODO|FIX|XXX)";

	// Source paths
	final static String[] JDT_CORE_SRC_PATHS = {
		"batch",
		"codeassist",
		"compiler",
		"dom",
		"eval",
		"formatter",
		"model",
		"search"
	};

	public FullSourceWorkspaceBuildTests(String name) {
		super(name);
	}

	static {
//		TESTS_PREFIX = "testCompile";
//		TESTS_NAMES = new String[] { "testFullBuildProjectNoWarning" };
	}

	public static Test suite() {
		Test suite = buildSuite(testClass());
		TESTS_COUNT = suite.countTestCases();
		createPrintStream(testClass(), LOG_STREAMS, TESTS_COUNT, null);
		return suite;
	}

	private static Class testClass() {
		return FullSourceWorkspaceBuildTests.class;
	}

	/**
	 * Start a build on given project or workspace using given options.
	 *
	 * @param javaProject Project which must be (full) build or null if all workspace has to be built.
	 * @param options Options used while building
	 */
	void build(final IJavaProject javaProject, Hashtable options, boolean noWarning) throws IOException, CoreException {
		if (DEBUG) System.out.print("\tstart build...");
		JavaCore.setOptions(options);
		if (PRINT) System.out.println("JavaCore options: "+options);

		// Build workspace if no project
		if (javaProject == null) {
			// single measure
			runGc();
			startMeasuring();
			ENV.fullBuild();
			stopMeasuring();
		} else {
			if (PRINT) System.out.println("Project options: "+javaProject.getOptions(false));
			// warm-up
			for (int i=0; i<WARMUP_COUNT; i++) {
				ENV.fullBuild(javaProject.getProject().getName());
			}

			// measures
			int max = MEASURES_COUNT;
			for (int i=0; i<max; i++) {
				runGc();
				startMeasuring();
				IWorkspaceRunnable compilation = new IWorkspaceRunnable() {
					public void run(IProgressMonitor monitor) throws CoreException {
						ENV.fullBuild(javaProject.getPath());
					}
				};
				IWorkspace workspace = ResourcesPlugin.getWorkspace();
				workspace.run(
					compilation,
					null/*don't take any lock*/,
					IWorkspace.AVOID_UPDATE,
					null/*no progress available here*/);
				stopMeasuring();
			}
		}

		// Verify markers
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IMarker[] markers = workspaceRoot.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		List resources = new ArrayList();
		List messages = new ArrayList();
		int warnings = 0;
		for (int i = 0, length = markers.length; i < length; i++) {
			IMarker marker = markers[i];
			switch (((Integer) marker.getAttribute(IMarker.SEVERITY)).intValue()) {
				case IMarker.SEVERITY_ERROR:
					resources.add(marker.getResource().getName());
					messages.add(marker.getAttribute(IMarker.MESSAGE));
					break;
				case IMarker.SEVERITY_WARNING:
					warnings++;
					if (noWarning) {
						resources.add(marker.getResource().getName());
						messages.add(marker.getAttribute(IMarker.MESSAGE));
					}
					break;
			}
		}
		workspaceRoot.deleteMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);

		// Assert result
		int size = messages.size();
		if (size > 0) {
			StringBuffer debugBuffer = new StringBuffer();
			for (int i=0; i<size; i++) {
				debugBuffer.append(resources.get(i));
				debugBuffer.append(":\n\t");
				debugBuffer.append(messages.get(i));
				debugBuffer.append('\n');
			}
			System.out.println(this.scenarioShortName+": Unexpected ERROR marker(s):\n" + debugBuffer.toString());
			System.out.println("--------------------");
			String target = javaProject == null ? "workspace" : javaProject.getElementName();
			assertEquals("Found "+size+" unexpected errors while building "+target, 0, size);
		}
		if (DEBUG) System.out.println("done");

		// Commit measure
		commitMeasurements();
		assertPerformance();

		// Store warning
		if (warnings>0) {
			System.out.println("\t- "+warnings+" warnings found while performing build.");
		}
		if (this.scenarioComment == null) {
			this.scenarioComment = new StringBuffer("["+TEST_POSITION+"]");
		} else {
			this.scenarioComment.append(' ');
		}
		this.scenarioComment.append("warn=");
		this.scenarioComment.append(warnings);
	}

	/*
	 * Compile given paths in a plugin using batch compiler
	 */
	void compile(String pluginID, String options, boolean log, String[] srcPaths) throws IOException, CoreException {
		compile(pluginID, options, null, log, srcPaths);
	}

	/*
	 * Compile given paths in a plugin using batch compiler
	 */
	void compile(String pluginID, String options, String compliance, boolean log, String[] srcPaths) throws IOException, CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final String targetWorkspacePath = workspaceRoot.getProject(pluginID).getLocation().toFile().getCanonicalPath();
		String logFileName = targetWorkspacePath + File.separator + getName()+".log";
		String workspacePath = workspaceRoot.getLocation().toFile().getCanonicalPath()+File.separator;
		String binPath = File.separator+"bin"+File.pathSeparator;
		String classpath = " -cp " +
			workspacePath+"org.eclipse.osgi" + binPath +
			workspacePath+"org.eclipse.jface" + binPath +
			workspacePath+"org.eclipse.core.runtime" + binPath +
			workspacePath+"org.eclipse.core.resources"+binPath +
			workspacePath+"org.eclipse.text"+binPath;
		String sources = srcPaths == null ? " "+targetWorkspacePath : "";
		if (srcPaths != null) {
			for (int i=0, l=srcPaths.length; i<l; i++) {
				String path = workspacePath + pluginID + File.separator + srcPaths[i];
				if (path.indexOf(" ") > 0) {
					path = "\"" + path + "\"";
				}
				sources += " " + path;
			}
		}

		compile(sources, options, classpath, null, log, logFileName);
	}

	// compile the file from org.eclipse.jdt.core.tests.binaries bundle using batch compiler
	void compile (String srcPath, long fileSize, String options, String compliance, boolean log) throws IOException {
		final String targetWorkspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile().getCanonicalPath();
		String logFileName = targetWorkspacePath + File.separator + getName()+".log";

		File file = fetchFromBinariesProject(srcPath, fileSize);
		String path = file.getAbsolutePath();
		if (path.indexOf(" ") > 0) {
			path = "\"" + path + "\"";
		}
		String sources = " " + path;
		compile(sources, options, "", compliance, log, logFileName);
	}

	// compile the given sources using batch compiler
	private void compile(String sources, String options, String classpath, String compliance, boolean log, String logFileName) {
		// Warm up
		if (compliance == null)
			compliance = " -" + (COMPLIANCE==null ? "1.4" : COMPLIANCE);
		else
			compliance = " -" + compliance;
		final String cmdLine = classpath + compliance + " -g -preserveAllLocals "+(options==null?"":options)+" -d " + COMPILER_OUTPUT_DIR + (log?" -log "+logFileName:"") + sources;
		if (PRINT) System.out.println("	Compiler command line = "+cmdLine);
		int warnings = 0;
		StringWriter errStrWriter = new StringWriter();
		PrintWriter err = new PrintWriter(errStrWriter);
		PrintWriter out = new PrintWriter(new StringWriter());
		Main warmup = new Main(out, err, false/*systemExit*/, null/*options*/, null/*progress*/);
		for (int i=1; i<WARMUP_COUNT; i++) {
			warmup.compile(Main.tokenize(cmdLine));
		}
		if (warmup.globalErrorsCount > 0) {
			System.out.println(this.scenarioShortName+": "+warmup.globalErrorsCount+" Unexpected compile ERROR!");
			if (DEBUG) {
				System.out.println(errStrWriter.toString());
				System.out.println("--------------------");
			}
		}
		if (!"none".equals(COMPILER_OUTPUT_DIR)) {
			org.eclipse.jdt.core.tests.util.Util.delete(COMPILER_OUTPUT_DIR);
		}
		warnings = warmup.globalWarningsCount;
		if (!log) org.eclipse.jdt.core.tests.util.Util.writeToFile(errStrWriter.toString(), logFileName);

		// Clean writer
		err = null;
		out = null;
		errStrWriter = null;

		// Measures
		for (int i = 0; i < MEASURES_COUNT; i++) {
			runGc();
			NullPrintWriter nullPrint= new NullPrintWriter();
			Main main = new Main(nullPrint, nullPrint, false/*systemExit*/, null/*options*/, null/*progress*/);
			startMeasuring();
			main.compile(Main.tokenize(cmdLine));
			stopMeasuring();
			if (!"none".equals(COMPILER_OUTPUT_DIR)) {
				org.eclipse.jdt.core.tests.util.Util.delete(COMPILER_OUTPUT_DIR);
			}
		}

		// Commit measures
		commitMeasurements();
		assertPerformance();

		// Store warning
		if (warnings>0) {
			System.out.println("\t- "+warnings+" warnings found while performing batch compilation.");
		}
		if (this.scenarioComment == null) {
			this.scenarioComment = new StringBuffer("["+TEST_POSITION+"]");
		} else {
			this.scenarioComment.append(' ');
		}
		this.scenarioComment.append("warn=");
		this.scenarioComment.append(warnings);
	}

	/*
	 * Parse several times a file giving its name.
	 */
	long[] parseFile(String fileName, int kind, int iterations) throws InvalidInputException, IOException {

		// Test for parser
		File file = new File(fileName);
		char[] content = Util.getFileCharContent(file, null);
		CompilerOptions options = new CompilerOptions();
		options.sourceLevel = ClassFileConstants.JDK1_4;
		options.targetJDK = ClassFileConstants.JDK1_4;
		ProblemReporter problemReporter =
				new ProblemReporter(
					DefaultErrorHandlingPolicies.exitAfterAllProblems(),
					options,
					new DefaultProblemFactory());

		// Create parser
        Parser parser = null;
		switch (kind) {
		case 1: // SourceElementParser
				parser = new SourceElementParser(new SourceElementRequestorAdapter(), problemReporter.problemFactory, options, true, true);
				break;
			default:
				parser = new Parser(problemReporter, true);
				break;
		}

		// Warm up
		for (int i = 0; i < 2; i++) {
			ICompilationUnit unit = new CompilationUnit(content, file.getName(), null);
			CompilationResult unitResult = new CompilationResult(unit, 0, 1, options.maxProblemsPerUnit);
			CompilationUnitDeclaration unitDeclaration = parser.dietParse(unit, unitResult);
			parser.getMethodBodies(unitDeclaration);
		}

		// Clean memory
		runGc();

		// Measures
		long parsedLines = 0;
		long parsedCharacters = 0;
		long start = 0;
		if (DEBUG) {
			start = System.currentTimeMillis();
		}
		startMeasuring();
		for (int i = 0; i < iterations; i++) {
			ICompilationUnit unit = new CompilationUnit(content, file.getName(), null);
			CompilationResult unitResult = new CompilationResult(unit, 0, 1, options.maxProblemsPerUnit);
			CompilationUnitDeclaration unitDeclaration = parser.dietParse(unit, unitResult);
			parser.getMethodBodies(unitDeclaration);
			parsedCharacters += content.length;
			parsedLines += unitResult.getLineSeparatorPositions().length;
		}
		stopMeasuring();

		// Warn if measure time is not enough while debugging
		if (DEBUG) {
			long time = System.currentTimeMillis() - start;
			if (time < TIME_THRESHOLD) {
	            System.err.println(parsedLines + " lines/"+ parsedCharacters + " characters parsed");
			} else {
	            System.out.println(parsedLines + " lines/"+ parsedCharacters + " characters parsed");
			}
		}

		// Return stats
		return new long[] { parsedCharacters, parsedLines };
	}

	/*
	 * Test performance for a parser on one file.
	 * Parse is executed many times ({@link #ITERATIONS_COUNT}) to have significant time for execution.
	 * This test is repeated several times ({@link #MEASURES_COUNT}) to average time measuring.
	 */
	void parseParserFile(final int kind) throws InvalidInputException, IOException, CoreException {

		// Get workspace path
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final String workspacePath = workspaceRoot.getLocation().toFile().getCanonicalPath();

		// Run test
		for (int i=0; i<MEASURES_COUNT; i++) {
			IWorkspaceRunnable compilation = new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					try {
						parseFile(workspacePath+PARSER_WORKING_COPY.getPath(), kind, ITERATIONS_COUNT*6);
					} catch (InvalidInputException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			if (workspace.isTreeLocked()) {
				compilation.run(null/*no progress available*/);
			} else {
				workspace.run(
					compilation,
					null/*don't take any lock*/,
					IWorkspace.AVOID_UPDATE,
					null/*no progress available here*/);
			}
		}

		// dump measure
		commitMeasurements();
		assertPerformance();
	}

	/*
	 * Scan a file giving its name.
	 * Two kind of scan is currently possible:
	 * 	- 0: only scan all tokens
	 * 	- 1: scan all tokens and get each identifier
	 */
	void scanFile(String fileName, int kind) throws InvalidInputException, IOException {

		// Test for scanner
		long tokenCount = 0;
		char[] content = Util.getFileCharContent(new File(fileName),
			null);
		Scanner scanner = new Scanner();
		scanner.setSource(content);

		// Warm up
		for (int i = 0; i < 2; i++) {
			scanner.resetTo(0, content.length);
			tokenize: while (true) {
				int token = scanner.getNextToken();
				switch (kind) {
					case 0: // first case: only read tokens
						switch (token) {
							case TerminalTokens.TokenNameEOF:
								break tokenize;
						}
						break;
					case 1: // second case: read tokens + get ids
						switch (token) {
							case TerminalTokens.TokenNameEOF:
								break tokenize;
							case TerminalTokens.TokenNameIdentifier:
								scanner.getCurrentIdentifierSource();
								break;
						}
						break;
				}
			}
		}

		// Measures
		long size = 0;
		for (int i = 0; i < MEASURES_COUNT; i++) {
			runGc();
			startMeasuring();
			for (int j = 0; j < SCAN_REPEAT; j++) {
				scanner.resetTo(0, content.length);
				tokenize: while (true) {
					int token = scanner.getNextToken();
					switch (kind) {
						case 0: // first case: only read tokens
							switch (token) {
								case TerminalTokens.TokenNameEOF:
									break tokenize;
							}
							break;
						case 1: // second case: read tokens + get ids
							switch (token) {
								case TerminalTokens.TokenNameEOF:
									break tokenize;
								case TerminalTokens.TokenNameIdentifier:
									char[] c = scanner.getCurrentIdentifierSource();
									size += c.length;
									break;
							}
							break;
					}
					tokenCount++;
				}
			}
			stopMeasuring();
		}

		// Commit
		commitMeasurements();
		assertPerformance();

		// Debug
		if (DEBUG) {
			switch (kind) {
				case 0:
					System.out.println(tokenCount + " tokens read.");
					break;
				case 1:
					System.out.print(tokenCount + " tokens were read ("+size+" characters)");
					break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {

		// End of execution => one test less
		TESTS_COUNT--;

		// Log perf result
		if (LOG_DIR != null) {
			logPerfResult(LOG_STREAMS, TESTS_COUNT);
		}

		// Call super at the end as it close print streams
		super.tearDown();
	}

	/**
	 * Test performance for Scanner on one file.
	 * Scan is executed many times ({@link #SCAN_REPEAT}) to have significant time for execution.
	 * This test is repeated several times ({@link #ITERATIONS_COUNT}) to average time measuring.
	 */
	public void testScanner() throws InvalidInputException, IOException, CoreException {

		// Get workspace path
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final String workspacePath = workspaceRoot.getLocation().toFile().getCanonicalPath();

		// Run test
		IWorkspaceRunnable compilation = new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					scanFile(workspacePath+PARSER_WORKING_COPY.getPath(), 1/*scan tokens+get identifiers*/);
				} catch (InvalidInputException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		if (workspace.isTreeLocked()) {
			compilation.run(null/*no progress available*/);
		} else {
			workspace.run(
				compilation,
				null/*don't take any lock*/,
				IWorkspace.AVOID_UPDATE,
				null/*no progress available here*/);
		}
	}

	/**
	 * Test performance for Parser on one file.
	 * Parse is executed many times ({@link #ITERATIONS_COUNT}) to have significant time for execution.
	 * This test is repeated several times ({@link #MEASURES_COUNT}) to average time measuring.
	 */
	public void testParser() throws InvalidInputException, IOException, CoreException {
		parseParserFile(0); // Parser kind
	}

	/**
	 * Test performance for SourceElementParser on one file.
	 * Parse is executed many times ({@link #ITERATIONS_COUNT}) to have significant time for execution.
	 * This test is repeated several times ({@link #MEASURES_COUNT}) to average time measuring.
	 *
	 * Note: This test has been temporarily removed as there's unexplicable difference between
	 * HEAD and 3.0 versions for CPU Time results (10% faster) and Elapsed process (25% slower)...
	 * TODO (frederic) Put back when platform-releng will have stabilized performance results process.
	 */
	public void _testSourceParser() throws InvalidInputException, IOException, CoreException {
		parseParserFile(1); // SourceElementParser kind
	}

	/**
	 * Full build with JavaCore default options.
	 *
	 * WARNING:
	 * 	This test must be and _ever_ stay at first position as it build the entire workspace.
	 * 	It also cannot be removed as it's a Global fingerprint!
	 * 	Move it would have great consequence on all other tests results...
	 */
	public void testFullBuildDefault() throws CoreException, IOException {
		tagAsSummary("Build entire workspace", false); // do NOT put in fingerprint
		build(null, warningOptions(0/*default warnings*/), false);
	}

	/**
	 * JDT/Core project full build with no warning.
	 *
	 * @since 3.2 M6
	 */
	public void testFullBuildProjectNoWarning() throws CoreException, IOException {
		tagAsSummary("Build JDT/Core project (no warning)", false); // do NOT put in fingerprint
		build(JDT_CORE_PROJECT, warningOptions(-1/*no warning*/), true);
	}

	/**
	 * JDT/Core project full build with JavaCore default options.
	 *
	 * @since 3.2 M6
	 */
	public void testFullBuildProjectDefault() throws CoreException, IOException {
		tagAsGlobalSummary("Build JDT/Core project", true); // put in global fingerprint
		build(JDT_CORE_PROJECT, warningOptions(0/*default warnings*/), false);
	}

	/**
	 * JDT/Core project full build with all warnings.
	 *
	 * @since 3.2 M6
	 */
	public void testFullBuildProjectAllWarnings() throws CoreException, IOException {
		tagAsSummary("Build JDT/Core project (all warnings)", false); // do NOT put in fingerprint
		build(JDT_CORE_PROJECT, warningOptions(1/*all warnings*/), false);
	}

	/**
	 * Batch compiler build with no warning
	 *
	 * Not calling tagAsSummary means that this test is currently evaluated
	 * before put it in builds performance results.
	 */
	public void testBatchCompilerNoWarning() throws IOException, CoreException {
		tagAsSummary("Compile folders using cmd line (no warn)", false); // do NOT put in fingerprint
		compile(JavaCore.PLUGIN_ID, "-nowarn", null, true/*log errors*/, null);
	}

	/**
	 * Compile JDT/Core project with default warnings
	 */
	public void testCompileJDTCoreProjectNoWarning() throws IOException, CoreException {
		tagAsSummary("Compile JDT/Core with cmd line (no warn)", false); // do NOT put in fingerprint
		compile(JavaCore.PLUGIN_ID, "-nowarn", null, false/*no log*/, JDT_CORE_SRC_PATHS);
	}

	/**
	 * Compile JDT/Core project with default warnings
	 */
	public void testCompileJDTCoreProjectDefault() throws IOException, CoreException {
		tagAsSummary("Compile JDT/Core with command line", true); // put in fingerprint
		compile(JavaCore.PLUGIN_ID, "", null, false/*no log*/, JDT_CORE_SRC_PATHS);
	}

	/**
	 * Compile JDT/Core project with default javadoc warnings
	 */
	public void testCompileJDTCoreProjectJavadoc() throws IOException, CoreException {
		tagAsSummary("Compile JDT/Core with cmd line (javadoc)", false); // do NOT put in fingerprint
		compile(JavaCore.PLUGIN_ID, "-warn:javadoc", null, false/*no log*/, JDT_CORE_SRC_PATHS);
	}

	/**
	 * Compile JDT/Core project with all warnings
	 *
	 * @since 3.2 M6
	 */
	public void testCompileJDTCoreProjectAllWarnings() throws IOException, CoreException {
		tagAsSummary("Compile JDT/Core with cmd line (all)", false); // do NOT put in fingerprint
		compile(JavaCore.PLUGIN_ID, ALL_OPTIONS, null, false/*no log*/, JDT_CORE_SRC_PATHS);
	}

	/**
	 * Compile JDT/Core project with default warnings
	 *
	 * @since 3.2 M6
	 */
	public void testCompileSWTProjectDefault() throws IOException, CoreException {
		tagAsSummary("Compile SWT with command line", false); // do NOT put in fingerprint
		String[] sourcePaths = {
				"Eclipse SWT/win32",
				"Eclipse SWT/common",
				"Eclipse SWT/common_j2se",
				"Eclipse SWT PI/win32",
				"Eclipse SWT PI/common_j2se",
				"Eclipse SWT OLE Win32/win32",
				"Eclipse SWT Accessibility/win32",
				"Eclipse SWT Accessibility/common",
				"Eclipse SWT AWT/win32",
				"Eclipse SWT AWT/common",
				"Eclipse SWT Drag and Drop/win32",
				"Eclipse SWT Drag and Drop/common",
				"Eclipse SWT Printing/win32",
				"Eclipse SWT Printing/common",
				"Eclipse SWT Program/win32",
				"Eclipse SWT Program/common",
				"Eclipse SWT Custom Widgets/common",
				"Eclipse SWT Browser/common",
				"Eclipse SWT Browser/win32",
		};
		compile("org.eclipse.swt", "", null, false/*no log*/, sourcePaths);
	}

	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=315978
	 */
	public void testBuildGenericType() throws IOException, CoreException {
		tagAsSummary("Build Generic Type ", false); // do NOT put in fingerprint
		compile("EclipseVisitorBug.java", 37_884, "", "1.6", false /*no log*/ );
	}

	/**
	 * Test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=434326 [compile][generics] Slow compilation of test cases with a significant amount of generics
	 */
	public void testBug434326() throws IOException, CoreException {
		tagAsSummary("Build with Generic Types ", false); // do NOT put in fingerprint
		compile("GenericsTest.java", 12_629_541, "", "1.8", false /*no log*/ );
	}
}
