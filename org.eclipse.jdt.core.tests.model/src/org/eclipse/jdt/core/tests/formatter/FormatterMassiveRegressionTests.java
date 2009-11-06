/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.formatter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.AssertionFailedError;
import junit.framework.ComparisonFailure;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.tests.model.ModelTestsUtil;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.core.util.CodeSnippetParsingUtil;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.text.edits.TextEdit;
import com.ibm.icu.util.StringTokenizer;

/**
 * Comment formatter test suite for massive tests at a given location.
 * <p>
 * This test suite has only one generic test. The tests are dynamically defined by
 * getting all compilation units located at the running workspace or at the
 * directory specified using the "dir" system property and create
 * one test per unit.
 * </p><p>
 * The test consists in first format the compilation unit using the new comments
 * formatter (i.e. since bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=102780
 * has been fixed) and second eventually compare it with the output that the
 * previous comments formatter would have done.
 * </p><p>
 * So, if no comparison is done, the test only insure that the new formatter does
 * not blow up while formatting the files found at the given location and that the
 * second formatting gives the same output than the first one.
 * </p><p>
 * TODO See how fix the remaining failing tests when comparing the
 * formatting of JUnit 3.8.2 files:
 * <ul>
 * 	<li>0 error</li>
 * 	<li>0 failure</li>
 * 	<li>0 file has different line leading spaces than old formatter</li>
 * 	<li>23 files have spaces differences with old formatter</li>
 *		</ul>
 * </p><p>
 * TODO Fix failures while running on workspaces without comparing.
 *
 * It is not possible to continue to compare the entire files after 2 formatting
 * as the code formatter cannot handle properly following snippet:
 * <pre>
 * public class X02 {
 * 	int field; // This is a long comment that should be split in multiple line comments in case the line comment formatting is enabled
 * }
 * </pre>
 * Which is formatted as:
 * <pre>
 * public class X02 {
 * 	int field; // This is a long comment that should be split in multiple line
 * 				// comments in case the line comment formatting is enabled
 * }
 * </pre>
 * But got a different output if formatted again:
 * <pre>
 * public class X02 {
 * 	int field; // This is a long comment that should be split in multiple line
 * 	// comments in case the line comment formatting is enabled
 * }
 * </pre>
 *
 * So, we're now obliged to ignore some whitespaces using the system property
 *  <code>ignoreWhitespaces</code> while running a launch config on this
 * test suite on big workspaces as full source perfs 3.0 or ganymede.
 *
 * Here are the results when setting the system property to
 * <code>linesLeading</code> (e.g. ignore white spaces at the beginning of the
 * lines, including the star inside javadoc or block comments):
 * <ul>
 * 	<li>JUnit 3.8.2 workspace (71 units):
 * 		<ul>
 * 		<li>0 error</li>
 * 		<li>0 failures</li>
 * 		<li>0 failures due to old formatter</li>
 * 		<li>8 files have different lines leading spaces</li>
 * 		<li>0 files have different spaces</li>
 *		</ul>
 *	</li>
 * 	<li>Eclipse 3.0 performance workspace (9951 units):
 * 		<ul>
 * 		<li>1 file has no output while formatting!</li>
 * 		<li>8 files have different output while reformatting twice but was expected!</li>
 * 		<li>714 files have different output while reformatting twice but only by leading whitespaces!</li>
 * 		<li>7 files have different output while reformatting twice but only by whitespaces!</li>
 *		</ul>
 *	</li>
 *	<li>Galileo workspace (41881 units):
 *		<ul>
 * 		<li>3 files have no output while formatting!</li>
 * 		<li>47 files have different output while reformatting twice!</li>
 * 		<li>2 files have different output while reformatting twice but was expected!</li>
 * 		<li>2384 files have different output while reformatting twice but only by leading whitespaces!</li>
 * 		<li>14 files have different output while reformatting twice but only by whitespaces!</li>
 *		</ul>
 *	</li>
 *	<li>JDKs workspace (29069 units):
 *		<ul>
 * 		<li>4 files have unexpected failure while formatting!</li>
 *		<li>1 file has no output while formatting!</li>
 * 		<li>115 files have different output while reformatting twice!</li>
 * 		<li>1148 files have different output while reformatting twice but only by leading whitespaces!</li>
 * 		<li>43 files have different output while reformatting twice but only by whitespaces!</li>
 *		</ul>
 *	</li>
 * </ul>
 */
public class FormatterMassiveRegressionTests extends FormatterRegressionTests {

	final File file;
	final IPath path;
	private DefaultCodeFormatterOptions preferences;

	// Directories
	private final static File INPUT_DIR = new File(System.getProperty("inputDir"));
	private static File OUTPUT_DIR; // use static to minimize data consumption
	private static File WRITE_DIR;
	
	// Files
	final static String FILES_FILTER = System.getProperty("filesFilter");
	final static int FILES_FILTER_KIND;
	static {
		int kind = 0; // No filter
		if (FILES_FILTER != null) {
			int length = FILES_FILTER.length();
			int idxQM = FILES_FILTER.indexOf('?');
			int idxS = FILES_FILTER.indexOf('*');
			if (idxQM >= 0 && idxS >= 0) {
				kind = 4; // Pure pattern match
			} else if (idxQM >= 0) {
				while (idxQM < length && FILES_FILTER.charAt(idxQM) == '?') {
					idxQM++;
				}
				if (idxQM == length) {
					kind = 3; // Starts with + same length
				} else {
					kind = 4; // Pure pattern match
				}
			} else if (idxS >= 0) {
				while (idxS < length && FILES_FILTER.charAt(idxQM) == '*') {
					idxS++;
				}
				if (idxS == length) {
					kind = 2; // Starts with
				} else {
					kind = 4; // Pure pattern match
				}
			} else {
				kind = 1; // Equals
			}
		}
		FILES_FILTER_KIND = kind;
	}

	// Log
	private static File LOG_FILE;
	private static PrintStream LOG_STREAM;

	// Comparison
	private static boolean CLEAN = false;
	private static boolean CAN_COMPARE = true;
	private final boolean canCompare;
	private final int testIndex;

	// Cleaning
	private final File[] inputFiles;
	private static int MAX_FILES, MAX_DIGITS;

	// Formatting behavior
	final static int FORMAT_REPEAT  = Integer.parseInt(System.getProperty("repeat", "2"));
	private final static boolean NO_COMMENTS = System.getProperty("no_comments", "false").equals("true");
	private static String JOIN_LINES = System.getProperty("join_lines", null);
	private static String BRACES = System.getProperty("braces", null);
	
	// Time measuring
	static class TimeMeasuring {
		long[] formatting = new long[FORMAT_REPEAT];
		int [] occurences = new int[FORMAT_REPEAT];
		int [] null_output = new int[FORMAT_REPEAT];
	}
	private static TimeMeasuring TIME_MEASURES = new TimeMeasuring();
	private static final int ONE_MINUTE = 60000;
	private static final long ONE_HOUR = 3600000L;

	// Failures management
	int failureIndex;
	final static int UNEXPECTED_FAILURE = 0;
	final static int NO_OUTPUT_FAILURE = 1;
	final static int COMPILATION_ERRORS_FAILURE = 2;
	final static int COMPARISON_FAILURE = 3;
	final static int REFORMATTING_FAILURE = 4;
	final static int REFORMATTING_EXPECTED_FAILURE = 5;
	final static int REFORMATTING_LEADING_FAILURE = 6;
	final static int REFORMATTING_WHITESPACES_FAILURE = 7;
	static class FormattingFailure {
		String msg;
		int kind;
		List failures = new ArrayList();
		public FormattingFailure(int kind) {
			this.kind = kind;
        }
		public FormattingFailure(int kind, String msg) {
			this(kind);
	        this.msg = msg;
        }
		int size() {
			return this.failures.size();
		}
		public String toString() {
			switch (this.kind) {
				case  UNEXPECTED_FAILURE:
					return "unexpected failure while formatting";
				case  NO_OUTPUT_FAILURE:
					return "no output while formatting";
				case  COMPILATION_ERRORS_FAILURE:
					return "compilation errors which prevent the formatter to proceed";
				case  COMPARISON_FAILURE:
					return "different output while comparing with previous version";
				default:
			        return "different output while "+this.msg;
			}
        }

	}
	static FormattingFailure[] FAILURES;
	private static final int MAX_FAILURES = Integer.parseInt(System.getProperty("maxFailures", "100")); // Max failures using string comparison
	private static boolean ASSERT_EQUALS_STRINGS = MAX_FAILURES > 0;
	private static String ECLIPSE_VERSION;
	private static String ECLIPSE_MILESTONE;
	private static String JDT_CORE_VERSION;
	private static String PATCH_BUG, PATCH_VERSION;
	private static boolean JDT_CORE_HEAD;
	private final static IPath[] EXPECTED_FAILURES = INPUT_DIR.getPath().indexOf("v34") < 0
		? new IPath[] {
			new Path("org/eclipse/jdt/internal/compiler/ast/QualifiedNameReference.java"),
			new Path("org/eclipse/jdt/internal/eval/CodeSnippetSingleNameReference.java"),
			new Path("org/eclipse/jdt/internal/core/DeltaProcessor.java"),
			new Path("org/eclipse/jdt/internal/core/JavaProject.java"),
			new Path("org/eclipse/jdt/internal/core/search/indexing/IndexManager.java"),
			new Path("org/eclipse/team/internal/ccvs/ui/AnnotateView.java"),
			new Path("org/eclipse/team/internal/ccvs/ui/HistoryView.java"),
			new Path("org/eclipse/team/internal/ccvs/ui/wizards/UpdateWizard.java"),
		}
		:	new IPath[] {
			// Eclipse
			new Path("org/eclipse/equinox/internal/p2/director/NewDependencyExpander.java"),
			new Path("org/eclipse/jdt/core/JavaCore.java"),
			new Path("org/eclipse/jdt/internal/codeassist/CompletionEngine.java"),
			new Path("org/eclipse/jdt/internal/codeassist/SelectionEngine.java"),
			new Path("org/eclipse/jdt/internal/compiler/ast/Expression.java"),
			new Path("org/eclipse/jdt/internal/compiler/ast/QualifiedNameReference.java"),
			new Path("org/eclipse/jdt/internal/compiler/ast/SingleNameReference.java"),
			new Path("org/eclipse/jdt/internal/eval/CodeSnippetSingleNameReference.java"),
			new Path("org/eclipse/jdt/internal/compiler/lookup/WildcardBinding.java"),
			new Path("org/eclipse/jdt/internal/compiler/batch/Main.java"),
			new Path("org/eclipse/jdt/internal/compiler/lookup/ParameterizedMethodBinding.java"),
			new Path("org/eclipse/jdt/internal/core/CompilationUnit.java"),
			new Path("org/eclipse/jdt/internal/core/ExternalJavaProject.java"),
			new Path("org/eclipse/jdt/internal/core/hierarchy/HierarchyResolver.java"),
			new Path("org/eclipse/jdt/internal/core/hierarchy/TypeHierarchy.java"),
			new Path("org/eclipse/jdt/internal/core/search/indexing/IndexAllProject.java"),
			new Path("org/eclipse/jdt/internal/core/search/JavaSearchScope.java"),
			new Path("org/eclipse/jdt/internal/eval/EvaluationContext.java"),
			new Path("org/eclipse/jdt/internal/ui/text/javadoc/JavadocContentAccess2.java"),
			new Path("org/eclipse/jdt/internal/apt/pluggable/core/filer/IdeJavaSourceOutputStream.java"),
			new Path("org/eclipse/team/internal/ccvs/ui/mappings/WorkspaceSubscriberContext.java"),
			// Ganymede
			new Path("com/ibm/icu/text/Collator.java"),
			new Path("org/apache/lucene/analysis/ISOLatin1AccentFilter.java"),
	};

public static Test suite() {

	TestSuite suite = new Suite(FormatterMassiveRegressionTests.class.getName());
	try {
		// Init version
		StringBuffer buffer = new StringBuffer();
		initVersion(buffer);
		
		// Init profiles
		initProfiles(buffer);

		// Log date of test
		long start = System.currentTimeMillis();
		SimpleDateFormat format = new SimpleDateFormat();
		Date now = new Date(start);
		buffer.append("Test date : ");
		buffer.append(format.format(now));
		buffer.append(LINE_SEPARATOR);

		// Get input dir
		buffer.append("Input dir : ");
		buffer.append(INPUT_DIR);

		// Output to console to show startup
		String firstBuffer = buffer.toString();
		System.out.println(firstBuffer);
		buffer.setLength(0);

		// Get files from input dir
		FileFilter filter = new FileFilter() {
			public boolean accept(File pathname) {
				String path = pathname.getPath();
				if (pathname.isDirectory()) {
					String dirName = path.substring(path.lastIndexOf(File.separatorChar)+1);
					return !dirName.equals("bin");
				}
				if (path.endsWith(".java")) {
					if (FILES_FILTER_KIND > 0) {
						String fileName = path.substring(path.lastIndexOf(File.separatorChar)+1);
						switch (FILES_FILTER_KIND) {
							case 1: // Equals
								return fileName.equals(FILES_FILTER);
							case 2: // Starts with
								return fileName.startsWith(FILES_FILTER);
							case 3: // Starts with + same length
								return fileName.startsWith(FILES_FILTER) && fileName.length() == FILES_FILTER.length();
							case 4: // Pattern
								return fileName.matches(FILES_FILTER);
						}
					} else {
						return true;
					}
				}
				return false;
            }
		};
		File[] allFiles = ModelTestsUtil.getAllFiles(INPUT_DIR, filter);
		MAX_FILES = allFiles.length;
		MAX_DIGITS = (int) (Math.log(MAX_FILES)/Math.log(10));
		buffer.append("            ");
		buffer.append(MAX_FILES);
		buffer.append(" java files found");
		buffer.append(LINE_SEPARATOR);

		// Init directories
		initDirectories(buffer);
		buffer.append("Compare vs: ");
		if (CAN_COMPARE) {
			if (CLEAN) {
				buffer.append(JDT_CORE_VERSION);
			} else {
				File versionFile = new File(OUTPUT_DIR, "version.txt");
				if (versionFile.exists()) {
					buffer.append(Util.fileContent(versionFile.getAbsolutePath()));
				} else {
					buffer.append("???");
				}
			}
		} else {
			buffer.append("none");
		}

		// Write logs
		System.out.println(buffer.toString());
		if (LOG_STREAM != null) {
			LOG_STREAM.println(firstBuffer);
			LOG_STREAM.println(buffer.toString());
			LOG_STREAM.flush();
		}
		
		// Add tests to clean the output directory and rebuild the references
		if (CLEAN) {
			suite.addTest(new FormatterMassiveRegressionTests());
			suite.addTest(new FormatterMassiveRegressionTests(allFiles));
		}
		
		// Add one test per found file
		for (int i=0; i<MAX_FILES; i++) {
			suite.addTest(new FormatterMassiveRegressionTests(allFiles[i], i, CAN_COMPARE));
		}
    } catch (Exception e) {
    	// skip
    }
	return suite;
}

private static void initProfiles(StringBuffer buffer) {
	boolean profile = NO_COMMENTS;
	if (JOIN_LINES != null) {
	 	if (!JOIN_LINES.equals("never") &&
	 		!JOIN_LINES.equals("only_comments") &&
	 		!JOIN_LINES.equals("only_code")) {
	 		JOIN_LINES = null;
	 	} else {
	 		profile = true;
	 	}
	}
	if (BRACES != null) {
	 	if (!BRACES.equals(DefaultCodeFormatterConstants.NEXT_LINE) &&
	 		!BRACES.equals(DefaultCodeFormatterConstants.NEXT_LINE_ON_WRAP) &&
	 		!BRACES.equals(DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED)) {
	 		BRACES = null;
	 	} else {
	 		profile = true;
	 	}
	}
	if (profile) {
		buffer.append("Profiles  : ");
		String separator = "";
		if (JOIN_LINES != null) {
			buffer.append("join_lines="+JOIN_LINES);
			separator = ", ";
		}
		if (NO_COMMENTS) {
			buffer.append(separator+"no_comments");
			separator = ", ";
		}
		if (BRACES != null) {
			buffer.append(separator+"braces="+BRACES);
		}
		buffer.append(LINE_SEPARATOR);
	}
}

private static void initDirectories(StringBuffer buffer) {

	// Verify input directory
	if (!INPUT_DIR.exists() && !INPUT_DIR.isDirectory()) {
		throw new RuntimeException(INPUT_DIR+" does not exist or is not a directory!");
	}

	// Get output dir and clean it if specified
	String dir = System.getProperty("outputDir"); //$NON-NLS-1$
	if (dir != null) {
		int idx = dir.indexOf(',');
		if (idx < 0) {
			setOutputDir(dir, buffer);
		} else {
			setOutputDir(dir.substring(0, idx), buffer);
			if (dir.substring(idx+1).equals("clean")) {
				CLEAN = true;
			}
		}
		if (CLEAN) {
			if (PATCH_BUG != null || JDT_CORE_HEAD) {
				throw new RuntimeException("Reference can only be updated using a version (i.e. with a closed buildnotes_jdt-core.html)!");
			}
		} else if (!OUTPUT_DIR.exists()) {
			System.err.println("            WARNING: The output directory "+OUTPUT_DIR+" does not exist...");
			System.err.println("            => NO comparison could be done!");
			CAN_COMPARE = false;
		}
		try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // skip
        }
	}

	// Get log dir
	try {
		setLogDir(buffer);
	} catch (CoreException e) {
		e.printStackTrace();
	}

	// Get write dir
	String wdir = System.getProperty("writeDir"); //$NON-NLS-1$
	if (wdir != null) {
		WRITE_DIR = new File(wdir);
		if (WRITE_DIR.exists()) {
			Util.delete(WRITE_DIR);
		}
		WRITE_DIR.mkdirs();
		buffer.append("Write dir : ");
		buffer.append(WRITE_DIR);
		buffer.append(LINE_SEPARATOR);
	}
}

private static void setLogDir(StringBuffer buffer) throws CoreException {

	// Compute log dir
//	IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
//	IProject project = root.getProject("Bugs");
//	IFolder rootFolder = project.getFolder("Formatter");
	File logDir = new File(System.getProperty("logDir"));
	if (!logDir.exists()) {
		return;
	}

	// Compute log sub-directories depending on profiles
	logDir = new File(logDir, ECLIPSE_VERSION);
//	IFolder folder = rootFolder.getFolder(ECLIPSE_VERSION);
//	if (!folder.exists()) folder.create(true, true, null);
	if (PATCH_BUG != null) {
		logDir = new File(logDir, "tests");
		logDir = new File(logDir, PATCH_BUG);
		logDir = new File(logDir, PATCH_VERSION);
//		folder = folder.getFolder("tests");
//		if (!folder.exists()) folder.create(true, true, null);
//		folder = folder.getFolder(PATCH_BUG);
//		if (!folder.exists()) folder.create(true, true, null);
//		folder = folder.getFolder(PATCH_VERSION);
//		if (!folder.exists()) folder.create(true, true, null);
	} else if (JDT_CORE_HEAD) {
		logDir = new File(logDir, "HEAD");
//		folder = folder.getFolder("HEAD");
//		if (!folder.exists()) folder.create(true, true, null);
	} else {
		logDir = new File(logDir, ECLIPSE_MILESTONE);
		logDir = new File(logDir, JDT_CORE_VERSION);
//		folder = folder.getFolder(ECLIPSE_MILESTONE);
//		if (!folder.exists()) folder.create(true, true, null);
//		folder = folder.getFolder(JDT_CORE_VERSION);
//		if (!folder.exists()) folder.create(true, true, null);
	}
	if (NO_COMMENTS || BRACES != null || JOIN_LINES != null) {
		logDir = new File(logDir, "profiles");
//		folder = folder.getFolder("profiles");
//		if (!folder.exists()) folder.create(true, true, null);
		if (JOIN_LINES != null) {
			logDir = new File(new File(logDir, "join_lines"), JOIN_LINES);
//			folder = folder.getFolder("join_lines");
//			if (!folder.exists()) folder.create(true, true, null);
//			folder = folder.getFolder(JOIN_LINES);
//			if (!folder.exists()) folder.create(true, true, null);
		}
		if (NO_COMMENTS) {
			logDir = new File(logDir, "no_comments");
//			folder = folder.getFolder("no_comments");
//			if (!folder.exists()) folder.create(true, true, null);
		}
		if (BRACES != null) {
			logDir = new File(new File(logDir, "braces"), BRACES);
//			folder = folder.getFolder("braces");
//			if (!folder.exists()) folder.create(true, true, null);
//			folder = folder.getFolder(BRACES);
//			if (!folder.exists()) folder.create(true, true, null);
		}
	}
	
	if (FILES_FILTER_KIND > 0) {
		logDir = new File(new File(logDir, "filter"), FILES_FILTER.replace('?', '_').replace('*', '%'));
	}

	// Create log stream
	logDir.mkdirs();
	String filePrefix = INPUT_DIR.getName().replaceAll("\\.", "");
	String logFileName = filePrefix+".txt";
	LOG_FILE = new File(logDir, logFileName);
	if (LOG_FILE.exists()) {
		File saveDir = new File(logDir, "save");
		saveDir.mkdir();
		int i=0;
		while (true) {
			String newFileName = filePrefix+"_";
			if (i<10) newFileName += "0";
			newFileName += i+".txt";
			File renamedFile = new File(saveDir, newFileName);
			if (LOG_FILE.renameTo(renamedFile)) break;
			i++;
		}
	}
//	LOG_RESOURCE = folder.getFile(logFileName);
	buffer.append("Log file  : ");
	buffer.append(LOG_FILE);
//	buffer.append(LOG_RESOURCE.getFullPath());
	buffer.append(LINE_SEPARATOR);
	try {
		LOG_STREAM = new PrintStream(new BufferedOutputStream(new FileOutputStream(LOG_FILE)));
		LOG_STREAM.flush();
	}
	catch (FileNotFoundException fnfe) {
		System.err.println("Can't create log file"+LOG_FILE); //$NON-NLS-1$
	}
//	if (LOG_RESOURCE.exists()) {
//		Util.delete(LOG_RESOURCE);
//	}
//	LOG_BUFFER = new StringBuffer();
}
private static void setOutputDir(String dir, StringBuffer buffer) {

	// Find the root of the output directory
	OUTPUT_DIR = new File(dir);
	if (OUTPUT_DIR.getName().equals(INPUT_DIR.getName())) {
		OUTPUT_DIR = OUTPUT_DIR.getParentFile();
	}
	if (OUTPUT_DIR.getName().equals(ECLIPSE_VERSION)) {
		OUTPUT_DIR = OUTPUT_DIR.getParentFile();
	}

	// Compute output sub-directories depending on profiles
	if (NO_COMMENTS || BRACES != null || JOIN_LINES != null) {
		OUTPUT_DIR = new File(OUTPUT_DIR, "profiles");
		if (JOIN_LINES != null) {
			OUTPUT_DIR = new File(new File(OUTPUT_DIR, "join_lines"), JOIN_LINES);
		}
		if (NO_COMMENTS) {
			OUTPUT_DIR = new File(OUTPUT_DIR, "no_comments");
		}
		if (BRACES != null) {
			OUTPUT_DIR = new File(new File(OUTPUT_DIR, "braces"), BRACES);
		}
	}

	// Compute the final output dir
	OUTPUT_DIR = new File(new File(OUTPUT_DIR, ECLIPSE_VERSION), INPUT_DIR.getName());
	buffer.append("Output dir: ");
	buffer.append(OUTPUT_DIR);
	buffer.append(LINE_SEPARATOR);
}

private static void initFailures() {
	FAILURES = new FormattingFailure[REFORMATTING_WHITESPACES_FAILURE+1];
	for (int i=UNEXPECTED_FAILURE; i<=COMPARISON_FAILURE; i++) {
		FAILURES[i] = new FormattingFailure(i);
	}
	FAILURES[REFORMATTING_FAILURE] = new FormattingFailure(REFORMATTING_FAILURE, "reformatting twice");
	FAILURES[REFORMATTING_LEADING_FAILURE] = new FormattingFailure(REFORMATTING_LEADING_FAILURE, "reformatting twice but only by leading whitespaces");
	FAILURES[REFORMATTING_WHITESPACES_FAILURE] = new FormattingFailure(REFORMATTING_WHITESPACES_FAILURE, "reformatting twice but only by whitespaces");
	FAILURES[REFORMATTING_EXPECTED_FAILURE] = new FormattingFailure(REFORMATTING_EXPECTED_FAILURE, "reformatting twice but was expected");
}

/*
 * Read JDT/Core build notes file to see what version is currently running.
 */
private static void initVersion(StringBuffer buffer) {
	BufferedReader buildnotesReader;
    try {
		URL platformURL = Platform.getBundle("org.eclipse.jdt.core").getEntry("/");
		String path = new File(FileLocator.toFileURL(platformURL).getFile(), "buildnotes_jdt-core.html").getAbsolutePath();
	    buildnotesReader = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
    } catch (IOException ioe) {
	    ioe.printStackTrace();
	    return;
    }
	String line;
	String patch_line = null;
	JDT_CORE_HEAD = true;
	try {
		while ((line = buildnotesReader.readLine()) != null) {
			if (line.startsWith("<a name=\"")) {
				boolean first = JDT_CORE_VERSION == null;
				JDT_CORE_VERSION = line.substring(line.indexOf('"')+1, line.lastIndexOf('"'));
				if (!first) break;
			} else if (line.startsWith("Eclipse SDK ")) {
				StringTokenizer tokenizer = new StringTokenizer(line);
				tokenizer.nextToken(); // 'Eclipse'
				tokenizer.nextToken(); // 'SDK'
				String milestone = tokenizer.nextToken();
				ECLIPSE_VERSION = "v"+milestone.charAt(0)+milestone.charAt(2);
				ECLIPSE_MILESTONE = milestone.substring(3);
				tokenizer.nextToken(); // '-'
				JDT_CORE_HEAD = tokenizer.nextToken().equals("%date%");
			} else if (line.startsWith("<h2>What's new")) {
				line = buildnotesReader.readLine();
				if (line.startsWith("Patch")) {
					patch_line = line;
					StringTokenizer tokenizer = new StringTokenizer(line);
					tokenizer.nextToken(); // 'Patch'
					PATCH_VERSION = tokenizer.nextToken();
					while (tokenizer.hasMoreTokens()) {
						PATCH_BUG = tokenizer.nextToken();
					}
					try {
						Integer.parseInt(PATCH_BUG);
					}
					catch (NumberFormatException nfe) {
						System.err.println("Invalid patch bug number noticed in JDT/Core buildnotes: "+PATCH_BUG);
					}
				}
				if (!JDT_CORE_HEAD) break;
			}
		}
	} catch (Exception e) {
		try {
	        buildnotesReader.close();
        } catch (IOException ioe) {
	        ioe.printStackTrace();
        }
	}

	// Log version info
	buffer.append("Version   : ");
	if (PATCH_BUG != null) {
		buffer.append('\'');
		buffer.append(patch_line);
		buffer.append("' applied on ");
	}
	if (JDT_CORE_HEAD) {
		buffer.append("HEAD on top of ");
	}
	buffer.append(JDT_CORE_VERSION);
	buffer.append(LINE_SEPARATOR);
}

/*
 * Constructor used to clean the output directory.
 */
public FormatterMassiveRegressionTests() {
	super("testDeleteOutputDir");
	this.canCompare = false;
	this.file = null;
	this.inputFiles = null;
	this.testIndex = -1;
	this.path = new Path(OUTPUT_DIR.getPath());
}

/*
 * Constructor used to dump references in the output directory.
 */
public FormatterMassiveRegressionTests(File[] files) {
	super("testMakeReferences");
	assertNotNull("This test needs some files to proceed!", files);
	this.canCompare = false;
	this.file = null;
	this.inputFiles = files;
	this.testIndex = -1;
	this.path = new Path(OUTPUT_DIR.getPath());
}

/*
 * Contructor used to compare outputs.
 */
public FormatterMassiveRegressionTests(File file, int index, boolean compare) {
	super("testCompare");
	this.canCompare = compare;
	this.file = file;
	this.inputFiles = null;
	this.testIndex = index;
	this.path = new Path(file.getPath().substring(INPUT_DIR.getPath().length()+1));
}

/* (non-Javadoc)
 * @see junit.framework.TestCase#getName()
 */
public String getName() {
	StringBuffer name = new StringBuffer(super.getName());
	if (this.testIndex >= 0) {
		int n = this.testIndex == 0 ? 0 : (int) (Math.log(this.testIndex)/Math.log(10));
		for (int i=n; i<MAX_DIGITS; i++) {
			name.append('0');
		}
		name.append(this.testIndex);
	}
	name.append(" - ");
	name.append(this.path);
	return name.toString();
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests#setUpSuite()
 */
public void setUp() throws Exception {
	super.setUp();
	this.preferences = DefaultCodeFormatterOptions.getEclipseDefaultSettings();
	if (NO_COMMENTS) {
		this.preferences.comment_format_javadoc_comment = false;
		this.preferences.comment_format_block_comment = false;
		this.preferences.comment_format_line_comment = false;
	}
	if (JOIN_LINES != null) {
		if (!JOIN_LINES.equals("only_comments")) {
			this.preferences.join_lines_in_comments = false;
		}
		if (!JOIN_LINES.equals("only_code")) {
			this.preferences.join_wrapped_lines = false;
		}
	}
	if (BRACES != null) {
		this.preferences.brace_position_for_annotation_type_declaration = BRACES;
		this.preferences.brace_position_for_anonymous_type_declaration = BRACES;
		this.preferences.brace_position_for_array_initializer = BRACES;
		this.preferences.brace_position_for_block = BRACES;
		this.preferences.brace_position_for_block_in_case = BRACES;
		this.preferences.brace_position_for_constructor_declaration = BRACES;
		this.preferences.brace_position_for_enum_constant = BRACES;
		this.preferences.brace_position_for_enum_declaration = BRACES;
		this.preferences.brace_position_for_method_declaration = BRACES;
		this.preferences.brace_position_for_switch = BRACES;
		this.preferences.brace_position_for_type_declaration = BRACES;
	}
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests#setUpSuite()
 */
public void setUpSuite() throws Exception {
	// skip standard model suite set up
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests#tearDown()
 */
public void tearDown() throws Exception {
	// verify whether the max failures has been reached or not
	if (ASSERT_EQUALS_STRINGS && FAILURES != null) {
		ASSERT_EQUALS_STRINGS = FAILURES[COMPARISON_FAILURE].size() < MAX_FAILURES;
	}
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests#tearDownSuite()
 */
public void tearDownSuite() throws Exception {

	// Display time measures
	StringBuffer buffer1 = new StringBuffer();
	buffer1.append("Time measures:");
	if (CLEAN) {
		buffer1.append(" cannot be done as the directory was cleaned!");
		buffer1.append(LINE_SEPARATOR);
	} else {
		buffer1.append(LINE_SEPARATOR);
		for (int i=0; i<FORMAT_REPEAT; i++) {
			buffer1.append("	- "+counterToString(i+1)).append(" format:").append(LINE_SEPARATOR);
			buffer1.append("		+ elapsed = "+timeString(TIME_MEASURES.formatting[i])).append(LINE_SEPARATOR);
			buffer1.append("		+ occurrences = "+TIME_MEASURES.occurences[i]).append(LINE_SEPARATOR);
			buffer1.append("		+ null output = "+TIME_MEASURES.null_output[i]).append(LINE_SEPARATOR);
		}
	}
	buffer1.append(LINE_SEPARATOR);

	// Display stored failures
	int max = FAILURES.length;
	for (int i=0; i<max; i++) {
		List failures = FAILURES[i].failures;
		int size = failures.size();
		if (size > 0) {
			buffer1.append(size);
			buffer1.append(" file");
			if (size == 1) {
				buffer1.append(" has ");
			} else {
				buffer1.append("s have ");
			}
			buffer1.append(FAILURES[i]);
			buffer1.append('!');
			buffer1.append(LINE_SEPARATOR);
		}
	}
	buffer1.append(LINE_SEPARATOR);
	StringBuffer buffer2 = new StringBuffer(LINE_SEPARATOR);
	for (int i=0; i<max; i++) {
		List failures = FAILURES[i].failures;
		int size = failures.size();
		if (size > 0) {
			buffer2.append("List of file(s) with ");
			buffer2.append(FAILURES[i]);
			buffer2.append(':');
			buffer2.append(LINE_SEPARATOR);
			for (int j=0; j<size; j++) {
				buffer2.append("	- ");
				buffer2.append(failures.get(j));
				buffer2.append(LINE_SEPARATOR);
			}
		}
	}

	// Log failures
	System.out.println(buffer1.toString());
	if (LOG_STREAM == null) {
		System.out.println(buffer2.toString());
	} else {
		LOG_STREAM.print(buffer1.toString());
		LOG_STREAM.print(buffer2.toString());
		LOG_STREAM.close();
	}
//	LOG_BUFFER.append(buffer1.toString());
//	LOG_BUFFER.append(buffer2.toString());
//	InputStream stream= new InputStream() {
//		private Reader reader= new StringReader(LOG_BUFFER.toString());
//		public int read() throws IOException {
//			return this.reader.read();
//		}
//	};
//	if (LOG_RESOURCE.exists()) {
//		LOG_RESOURCE.setContents(
//			stream,
//			IResource.FORCE | IResource.KEEP_HISTORY,
//			null);
//	} else {
//		LOG_RESOURCE.create(stream, IResource.FORCE, null);
//	}
}

/*
 * Asserts that the given actual source (usually coming from a file content) is equal to the expected one.
 * Note that 'expected' is assumed to have the '\n' line separator.
 * The line separators in 'actual' are converted to '\n' before the comparison.
 */
protected void assertSourceEquals(String message, String expected, String actual) {
	if (expected == null) {
		assertNull(message, actual);
		return;
	}
	if (actual == null) {
		assertEquals(message, expected, null);
		return;
	}
	expected = Util.convertToIndependantLineDelimiter(expected);
	actual = Util.convertToIndependantLineDelimiter(actual);
	if (ASSERT_EQUALS_STRINGS) {
		assertEquals(message, expected, actual);
	} else {
		assertTrue(message, actual.equals(expected));
	}
}

DefaultCodeFormatter codeFormatter() {
	DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(this.preferences, getDefaultCompilerOptions());
	return codeFormatter;
}

void compareFormattedSource() throws IOException, Exception {
	String source = new String(org.eclipse.jdt.internal.compiler.util.Util.getFileCharContent(this.file, null));
	String actualResult = null;
	try {
		// Format the source
		actualResult = runFormatter(codeFormatter(), source, CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, 0, 0, source.length(), null, true);

		// Look for output to compare with
		File outputFile = new Path(OUTPUT_DIR.getPath()).append(this.path).toFile();
		if (actualResult != null && FAILURES != null && this.canCompare) {
			String expectedResult = new String(org.eclipse.jdt.internal.compiler.util.Util.getFileCharContent(outputFile, null));
			try {
				assertSourceEquals("Unexpected format output!", expectedResult, actualResult);
			}
			catch (ComparisonFailure cf) {
				this.failureIndex = COMPARISON_FAILURE;
				throw cf;
			}
			catch (AssertionFailedError afe) {
				this.failureIndex = COMPARISON_FAILURE;
				throw afe;
			}
		}
	}
	catch (Exception e) {
//		System.err.println(e.getMessage()+" occurred in "+getName());
		throw e;
	}
	finally {
		// Write file
		if (actualResult != null) {
			if (WRITE_DIR != null) {
				File writtenFile = new Path(WRITE_DIR.getPath()).append(this.path).toFile();
				writtenFile.getParentFile().mkdirs();
				Util.writeToFile(actualResult, writtenFile.getAbsolutePath());
			}
		}
	}
}

private String counterToString(int count) {
	int reminder = count%10;
	StringBuffer buffer = new StringBuffer();
	buffer.append(count);
	switch (reminder) {
		case 1:
			buffer.append("st");
			break;
		case 2:
			buffer.append("nd");
			break;
		case 3:
			buffer.append("rd");
			break;
		default:
			buffer.append("th");
			break;
	}
	return buffer.toString();
}

private Map getDefaultCompilerOptions() {
	Map optionsMap = new HashMap(30);
	optionsMap.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.DO_NOT_GENERATE);
	optionsMap.put(CompilerOptions.OPTION_LineNumberAttribute, CompilerOptions.DO_NOT_GENERATE);
	optionsMap.put(CompilerOptions.OPTION_SourceFileAttribute, CompilerOptions.DO_NOT_GENERATE);
	optionsMap.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.PRESERVE);
	optionsMap.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportMethodWithConstructorName, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportOverridingMethodWithoutSuperInvocation, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportDeprecationWhenOverridingDeprecatedMethod, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportHiddenCatchBlock, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportNoEffectAssignment, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportNoImplicitStringConversion, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportIndirectStaticAccess, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportIncompatibleNonInheritedInterfaceMethod, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportLocalVariableHiding, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportPossibleAccidentalBooleanAssignment, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportEmptyStatement, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportAssertIdentifier, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportEnumIdentifier, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportUndocumentedEmptyBlock, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PUBLIC);
	optionsMap.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportMissingJavadocTagDescription, CompilerOptions.RETURN_TAG);
	optionsMap.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsNotVisibleRef, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportMissingJavadocTags, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility, CompilerOptions.PUBLIC);
	optionsMap.put(CompilerOptions.OPTION_ReportMissingJavadocTagsOverriding, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportMissingJavadocComments, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsVisibility, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportMissingJavadocCommentsOverriding, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportFinallyBlockNotCompletingNormally, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownException, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownExceptionWhenOverriding, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportUnqualifiedFieldAccess, CompilerOptions.IGNORE);
	optionsMap.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_6);
	optionsMap.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_6);
	optionsMap.put(CompilerOptions.OPTION_TaskTags, ""); //$NON-NLS-1$
	optionsMap.put(CompilerOptions.OPTION_TaskPriorities, ""); //$NON-NLS-1$
	optionsMap.put(CompilerOptions.OPTION_TaskCaseSensitive, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_ReportSpecialParameterHidingField, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_MaxProblemPerUnit, String.valueOf(100));
	optionsMap.put(CompilerOptions.OPTION_InlineJsr, CompilerOptions.DISABLED);
	optionsMap.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_6);
	return optionsMap;
}

private boolean isExpectedFailure() {
	int length = EXPECTED_FAILURES.length;
	for (int i=0; i<length; i++) {
		IPath expectedFailure= EXPECTED_FAILURES[i];
		if (this.path.toString().indexOf(expectedFailure.toString()) >= 0) {
			this.failureIndex = REFORMATTING_EXPECTED_FAILURE;
			FAILURES[REFORMATTING_EXPECTED_FAILURE].failures.add(this.path);
			return true;
		}
	}
	return false;
}

/*
private boolean runFormatterWithoutComments(CodeFormatter codeFormatter, String source, int kind, int indentationLevel, int offset, int length, String lineSeparator) {
	DefaultCodeFormatterOptions preferencesWithoutComment = DefaultCodeFormatterOptions.getEclipseDefaultSettings();
	preferencesWithoutComment.comment_format_line_comment = false;
	preferencesWithoutComment.comment_format_block_comment = false;
	preferencesWithoutComment.comment_format_javadoc_comment = false;
	DefaultCodeFormatter codeFormatterWithoutComment = new DefaultCodeFormatter(preferencesWithoutComment);

	TextEdit edit = codeFormatterWithoutComment.format(kind, source, offset, length, indentationLevel, lineSeparator);//$NON-NLS-1$
	if (edit == null) return false;
	String initialResult = org.eclipse.jdt.internal.core.util.Util.editedString(source, edit);

	int count = 1;
	String result = initialResult;
	String previousResult = result;
	while (count++ < FORMAT_REPEAT) {
		edit = codeFormatterWithoutComment.format(kind, result, 0, result.length(), indentationLevel, lineSeparator);//$NON-NLS-1$
		if (edit == null) return false;
		previousResult = result;
		result = org.eclipse.jdt.internal.core.util.Util.editedString(result, edit);
	}
	return previousResult.equals(result);
}
*/

private boolean sourceHasCompilationErrors(String source) {
	CodeSnippetParsingUtil codeSnippetParsingUtil = new CodeSnippetParsingUtil();
	codeSnippetParsingUtil.parseCompilationUnit(source.toCharArray(), getDefaultCompilerOptions(), true);
	if (codeSnippetParsingUtil.recordedParsingInformation != null) {
		CategorizedProblem[] problems = codeSnippetParsingUtil.recordedParsingInformation.problems;
		int length = problems == null ? 0 : problems.length;
		for (int i=0; i<length; i++) {
			if (((DefaultProblem)problems[i]).isError()) {
				return true;
			}
		}
	}
	return false;
}

String runFormatter(CodeFormatter codeFormatter, String source, int kind, int indentationLevel, int offset, int length, String lineSeparator, boolean repeat) {
	long timeStart = System.currentTimeMillis();
	TextEdit edit = codeFormatter.format(kind, source, offset, length, indentationLevel, lineSeparator);
	if (FAILURES != null) { // Comparison has started
		TIME_MEASURES.formatting[0] += System.currentTimeMillis() - timeStart;
		TIME_MEASURES.occurences[0]++;
		if (edit == null) TIME_MEASURES.null_output[0]++;
	}
	if (edit == null) {
		if (sourceHasCompilationErrors(source)) {
			this.failureIndex = COMPILATION_ERRORS_FAILURE;
			FAILURES[COMPILATION_ERRORS_FAILURE].failures.add(this.path);
			return null;
		}
		this.failureIndex = NO_OUTPUT_FAILURE;
		throw new AssertionFailedError("Formatted source should not be null!");
	}
	String initialResult = org.eclipse.jdt.internal.core.util.Util.editedString(source, edit);

	int count = 0;
	String result = initialResult;
	String previousResult = result;
	while (++count < FORMAT_REPEAT) {
		timeStart = System.currentTimeMillis();
		edit = codeFormatter.format(kind, result, 0, result.length(), indentationLevel, lineSeparator);
		if (FAILURES != null) { // Comparison has started
			TIME_MEASURES.formatting[count] += System.currentTimeMillis() - timeStart;
			TIME_MEASURES.occurences[count]++;
			if (edit == null) TIME_MEASURES.null_output[count]++;
		}
		if (edit == null) return null;
		previousResult = result;
		result = org.eclipse.jdt.internal.core.util.Util.editedString(result, edit);
	}
	if (!previousResult.equals(result)) {

		if (FAILURES != null) {
			// Try to compare without leading spaces
			String trimmedExpected = ModelTestsUtil.trimLinesLeadingWhitespaces(previousResult);
			String trimmedActual= ModelTestsUtil.trimLinesLeadingWhitespaces(result);
			if (trimmedExpected.equals(trimmedActual)) {
				this.failureIndex = REFORMATTING_LEADING_FAILURE;
				FAILURES[REFORMATTING_LEADING_FAILURE].failures.add(this.path);
				return initialResult;
			}

			// Try to compare without spaces at all
			if (ModelTestsUtil.removeWhiteSpace(previousResult).equals(ModelTestsUtil.removeWhiteSpace(result))) {
				this.failureIndex = REFORMATTING_WHITESPACES_FAILURE;
				FAILURES[REFORMATTING_WHITESPACES_FAILURE].failures.add(this.path);
				return initialResult;
			}
		}

		/*
		// Try to see if the formatting also fails without comments
		if (!runFormatterWithoutComments(null, source, kind, indentationLevel, offset, length, lineSeparator)) {
			return initialResult;
		}

		// format without comments is OK => there's a problem with comment formatting
		String counterString = counterToString(count-1);
		assertSourceEquals(counterString+" formatting is different from first one!", previousResult, result);
		*/
		if (!isExpectedFailure()) {
			String counterString = counterToString(count);
			try {
				assertSourceEquals(counterString+" formatting is different from first one!", previousResult, result);
			}
			catch (ComparisonFailure cf) {
				this.failureIndex = REFORMATTING_FAILURE;
				throw cf;
			}
			catch (AssertionFailedError afe) {
				this.failureIndex = REFORMATTING_FAILURE;
				throw afe;
			}
		}
	}
	return initialResult;
}

/**
 * Returns a string to display the given time as a duration
 * formatted as:
 *	<ul>
 *	<li>"XXXms" if the duration is less than 0.1s (e.g. "543ms")</li>
 *	<li>"X.YYs" if the duration is less than 1s (e.g. "5.43s")</li>
 *	<li>"XX.Ys" if the duration is less than 1mn (e.g. "54.3s")</li>
 *	<li>"XXmn XXs" if the duration is less than 1h (e.g. "54mn 3s")</li>
 *	<li>"XXh XXmn XXs" if the duration is over than 1h (e.g. "5h 4mn 3s")</li>
 *	</ul>
 *
 * @param time The time to format as a long.
 * @return The formatted string.
 */
public String timeString(long time) {
	NumberFormat format = NumberFormat.getInstance();
	format.setMaximumFractionDigits(3);
	StringBuffer buffer = new StringBuffer();
	if (time == 0) {
		// print nothing
	} else {
		long h = time / ONE_HOUR;
		if (h > 0) buffer.append(h).append("h "); //$NON-NLS-1$
		long remaining = time % ONE_HOUR;
		long m = remaining / ONE_MINUTE;
		if (h > 0 || m > 0) buffer.append(m).append("mn "); //$NON-NLS-1$
		remaining = remaining % ONE_MINUTE;
		if ((remaining % 1000) == 0) {
			buffer.append(remaining/1000);
		} else {
			buffer.append(format.format(remaining/1000.0));
		}
		buffer.append("s"); //$NON-NLS-1$
	}
	return buffer.toString();
}

/*
 * Test to delete the output directory.
 */
public void testDeleteOutputDir() throws IOException, Exception {
	Util.delete(OUTPUT_DIR);
}

/*
 * Test to fill the output directory with reference.
 */
public void testMakeReferences() throws IOException, Exception {

	// Dump the version
	File versionFile = new Path(OUTPUT_DIR.getPath()).append("version.txt").toFile();
	OUTPUT_DIR.mkdirs();
	Util.writeToFile(JDT_CORE_VERSION, versionFile.getAbsolutePath());

	// Format each file of the input dir and write the result to the output directory
	assertNotNull("We should have got input files from "+INPUT_DIR, this.inputFiles);
	DefaultCodeFormatter codeFormatter = codeFormatter();
	int length = this.inputFiles.length;
	for (int i=0; i<length; i++) {

		// Get the source from file
		String source = new String(org.eclipse.jdt.internal.compiler.util.Util.getFileCharContent(this.inputFiles[i], null));

		try {
			// Format the source
			TextEdit edit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, source, 0, source.length(), 0, null);

			// Write the result
			if (edit != null) {
				String formatResult = org.eclipse.jdt.internal.core.util.Util.editedString(source, edit);
				String inputPath = this.inputFiles[i].getPath().substring(INPUT_DIR.getPath().length()+1);
				File writtenFile = new Path(OUTPUT_DIR.getPath()).append(inputPath).toFile();
				writtenFile.getParentFile().mkdirs();
				Util.writeToFile(formatResult, writtenFile.getAbsolutePath());
			}
		}
		catch (Exception ex) {
			// skip silently
		}
	}
}

/*
 * Test to compare the formatter output with an existing file.
 */
public void testCompare() throws IOException, Exception {
	if (FAILURES == null && this.canCompare) {
		initFailures();
	}
	try {
		compareFormattedSource();
	}
	catch (ComparisonFailure cf) {
		if (this.failureIndex == -1) {
			FAILURES[UNEXPECTED_FAILURE].failures.add(this.path);
		} else {
			FAILURES[this.failureIndex].failures.add(this.path);
		}
		throw cf;
	}
	catch (AssertionFailedError afe) {
		if (this.failureIndex == -1) {
			FAILURES[UNEXPECTED_FAILURE].failures.add(this.path);
		} else {
			FAILURES[this.failureIndex].failures.add(this.path);
		}
		throw afe;
	}
	catch (Exception ex) {
		FAILURES[UNEXPECTED_FAILURE].failures.add(this.path);
		throw ex;
	}
}
}
