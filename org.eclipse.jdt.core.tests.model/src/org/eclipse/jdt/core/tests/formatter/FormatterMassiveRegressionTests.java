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
package org.eclipse.jdt.core.tests.formatter;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
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
import java.util.StringTokenizer;

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

/**
 * Comment formatter test suite for massive tests at a given location.
 * <p>
 * This test suite has only one generic test. When running this test suite, one test
 * is created per compilation unit found while traversing the directory specified
 * using the <code>inputDir</code> system property<br>(e.g.
 * <code>-DinputDir=D:\eclipse\workspaces\formatter\inputs\full-src-30</code>).
 * </p><p>
 * Each test formats twice the compilation unit and compare the result to a
 * previous formatting already stored at the same location from a root directory
 * specified using the <code>outputDir</code> system property.
 * </p><p>
 * For example, if <code>outputDir</code> is set to the following value:<br>
 * <code>-DoutputDir=D:\eclipse\workspaces\formatter\outputs</code><br>
 * then a compilation unit found in <code>...\inputs\full-src-30\test\A.java</code>
 * will be compared with the previously stored output in:
 * <code>...\outputs\full-src-30\test\A.java</code>
 * </p><p>
 * To store the outputs on a specific input directory using a specific version,
 * then load a JDT/Core version in the workspace and run this test suite using
 * the following VM arguments:
 * <pre>
 * -DinputDir=D:\eclipse\workspaces\formatter\inputs\full-src-30
 * -DoutputDir=D:\eclipse\workspaces\formatter\outputs,clean
 * </pre>
 * Note the <code>clean</code> arguments added at the end of the outputDir
 * system property to signify that the formatter outputs must be cleaned and
 * stored.
 * </p><p>
 * The <code>logDir</code> system property can be set to tell the suite to
 * write the console output in a file located in the specified directory. That makes
 * the comparison between version and patches easier to do using the eclipse
 * file comparison...
 * </p><p><br>
 * <b>***************************************<br>
 * * Process to run massive tests against a patch *<br>
 * ***************************************</b>
 * <p>
 * Here is the full description of the process to run massive tests against a patch
 * using this test suite and all the compilation units of the JDT/Core performance
 * <b>full-source-R3_0.zip</b> file...
 * </p>
 * <h3>Set-up input directory</h3>
 * <p>
 * The test suite needs to know where are the sources which will be used
 * to massively test the formatter. To make it easy to set-up, only a root directory
 * is necessary. From there, all compilation units found while traversing the tree of
 * this directory will be used for the massive tests.
 * </p><p>
 * In our example, we will extract the content of the <b>full-source-R3_0.zip</b>
 * file located in the <b>org.eclipse.jdt.core.tests.performance</b> plugin
 * somewhere on our local disk... let's say in the
 * <b>D:\tmp\formatter\inputs\full-src-30</b> directory.
 * </p>
 * <h3>Create the output reference</h3>
 * <p>
 * The reference from which the patch output will be compared to while running
 * the massive test needs also to be created. To do this, a launch config
 * for the <code>FormatterMassiveRegressionTests</code> test suite is necessary.
 * </p><p>
 *  For example, create a launch config named
 * <b>FormatterMassiveRegressionTests (Eclipse 3.0 - clean)</b> with the
 * following VM arguments:
 * <pre>
 * -Xmx256M
 * -DinputDir=D:\tmp\formatter\inputs\full-src-30
 * -DoutputDir=D:\tmp\formatter\outputs,clean
 * -DlogDir=D:\tmp\formatter\log
 * </pre>
 * </p><p>
 * Load the last version of JDT/Core plugins (e.g. <code>v_B11</code>) and
 * launch this config...
 * </p><p>
 * When done, the console should have the following content:
 * <pre>
 * Get all files from D:\tmp\formatter\inputs\full-src-30...done
 * Deleting all files from D:\tmp\formatter\outputs\v37\full-src-30...done
 * Version   : v_B11
 * Profiles  : none!
 * Test date : 9/12/10 1:47 PM
 * Input dir : D:\tmp\formatter\inputs\full-src-30
 *             9950 java files to format...
 * Output dir: D:\tmp\formatter\outputs\v37\full-src-30
 *             CLEANED
 * </pre>
 * Looking at the output directory, it should contain the same folders tree than
 * the input one...
 * </p>
 * <h3>Create the log reference</h3>
 * <p>
 * The test suite log several problems which may occur while formatting a unit:
 * 	<ul>
 * 	<li>the file may have compilation errors preventing the formatter to proceed</li>
 * 	<li>there's no output while formatting</li>
 * 	<li>the output may be different while formatting twice</li>
 * 	<li>the output may be different while formatting twice but only by leading whitespaces</li>
 * 	<li>the output may be different while formatting twice but only by whitespaces</li>
 *	</ul>
 * </p><p>
 * Even with last version of the formatter, such problems may happen on one or
 * several tested compilation unit. So, it's important to know which are the existing
 * issues of the used formatter version (e.g. <code>v_B11</code> in our example...).
 * </p><p>
 * To do this, another launch config is necessary to run the massive tests of the
 * loaded JDT/Core version.
 * </p><p>
 *  For example, copy the previous launch config and rename it
 * <b>FormatterMassiveRegressionTests (Eclipse 3.0)</b>. Change the VM
 * arguments as follows (<i>note that the <code>clean</code> has been removed
 * from the <code>outputDir</code> system property</i>):
 * <pre>
 * -Xmx256M
 * -DinputDir=D:\tmp\formatter\inputs\full-src-30
 * -DoutputDir=D:\tmp\formatter\outputs
 * -DlogDir=D:\tmp\formatter\log
 * </pre>
 * </p><p>
 * Launch the config...
 * </p><p>
 * The log file contains the console output but also the complete list of the units
 * on which problems were observed. As this run was done with the JDT/Core
 * version it can be considered as the reference for this version...
 * </p><p>
 * Note that for our example, the observed problems for <code>v_B11</code>
 * version while running massive tests on a Eclipse 3.0 performance workspace
 * (9951 units) are:
 * 	<ul>
 * 	<li>1 file has compilation errors which prevent the formatter to proceed!</li>
 * 	<li>4 files have different output while reformatting twice!</li>
 * 	<li>10 files have different output while reformatting twice but only by leading whitespaces!</li>
 * 	<li>4 files have different output while reformatting twice but only by whitespaces!</li>
 *	</ul>
 * </p>
 * <h3>Run the massive tests on the patch</h3>
 * <p>
 * As the setup has been done for the massive tests, it's now possible to test a
 * patch applied on the reference version (<code>v_B11</code>). For this, the
 * patch needs of course to be applied first and also the <b>buildnotes_jdt-core.html</b>
 * modified.
 * </p><p>
 * If the patch vXX of bug XXXXXX is about to be tested, then the line
 * <code>Patch vXX for bug XXXXXX</code> needs to be added at the
 * beginning of the first <b>What's new in this drop</b> section of the
 * <b>buildnotes_jdt-core.html</b> file, e.g.:
 * <pre>
 * &lt;h2&gt;What's new in this drop&lt;/h2&gt;
 * Patch v05 for bug 303519
 * &lt;ul&gt;
 * ...
 * </pre>
 * </p><p>
 * Launch the <b>FormatterMassiveRegressionTests (Eclipse 3.0)</b> config...
 * </p><p>
 * Like the previous run, the written log file contains the complete list of the units
 * on which problems were observed. Comparing this log file with the reference one
 * will show whether the patch implies behavior changes for the formatter or not.
 * </p>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class FormatterMassiveRegressionTests extends FormatterRegressionTests {

	final File file;
	final IPath path;
	private DefaultCodeFormatterOptions preferences;

	// Directories
//	private final static File INPUT_DIR = new File(System.getProperty("inputDir"));
	private final File inputDir;
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

	// Maintenance
	private static boolean MAINTENANCE = false;

	// Comparison
	private static boolean CLEAN = false;
	private static boolean CAN_COMPARE = true;
	private final boolean canCompare;
	private final int testIndex;

	// Cleaning
	private static boolean LIST = false;
	private final static Map MAX_FILES = new HashMap();

	// Formatting behavior
	final static int FORMAT_REPEAT  = Integer.parseInt(System.getProperty("repeat", "2"));
	private final static boolean NO_COMMENTS = System.getProperty("no_comments", "false").equals("true");
	private final static String JOIN_LINES = System.getProperty("join_lines", null);
	private final static String BRACES = System.getProperty("braces", null);
	private final static int PRESERVED_LINES;
	static {
		String str = System.getProperty("preserved_lines", null);
		int value = -1;
		if (str != null) {
			try {
				value = Integer.parseInt(str);
			}
			catch (NumberFormatException nfe) {
				// skip
			}
		}
		PRESERVED_LINES = value;
	}
	private final int profiles;
	private final static int PROFILE_NEVER_JOIN_LINES = 1;
	private final static int PROFILE_JOIN_LINES_ONLY_COMMENTS = 2;
	private final static int PROFILE_JOIN_LINES_ONLY_CODE = 3;
	private final static int PROFILE_JOIN_LINES_MASK = 0x0003;
	private final static int PROFILE_NO_COMMENTS = 1 << 2;
	private final static int PROFILE_BRACES_NEXT_LINE = 1 << 3;
	private final static int PROFILE_BRACES_NEXT_LINE_ON_WRAP = 2 << 3;
	private final static int PROFILE_BRACES_NEXT_LINE_SHIFTED = 3 << 3;
	private final static int PROFILE_BRACES_MASK = 0x0018;
	private final static int PROFILE_PRESERVED_LINES_MASK = 0x00E0;

	// Time measuring
	static class TimeMeasuring {
		long[] formatting = new long[FORMAT_REPEAT];
		int [] occurences = new int[FORMAT_REPEAT];
		int [] null_output = new int[FORMAT_REPEAT];
	}
	private static TimeMeasuring TIME_MEASURES;
	private static final int ONE_MINUTE = 60000;
	private static final long ONE_HOUR = 3600000L;

	// Failures management
	int failureIndex;
	final static int UNEXPECTED_FAILURE = 0;
	final static int NO_OUTPUT_FAILURE = 1;
	final static int COMPILATION_ERRORS_FAILURE = 2;
	final static int FILE_NOT_FOUND_FAILURE = 3;
	final static int COMPARISON_FAILURE = 4;
	final static int REFORMATTING_FAILURE = 5;
	final static int REFORMATTING_EXPECTED_FAILURE = 6;
	final static int REFORMATTING_LEADING_FAILURE = 7;
	final static int REFORMATTING_WHITESPACES_FAILURE = 8;
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
		@Override
		public String toString() {
			switch (this.kind) {
				case  UNEXPECTED_FAILURE:
					return "unexpected failure while formatting";
				case  NO_OUTPUT_FAILURE:
					return "no output while formatting";
				case  COMPILATION_ERRORS_FAILURE:
					return "compilation errors which prevent the formatter to proceed";
				case  FILE_NOT_FOUND_FAILURE:
					return "no formatted output to compare with";
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
	private static String TEMP_OUTPUT;
	private static boolean JDT_CORE_HEAD;
	/*
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
	*/

public static Test suite() {
	return suite(new File(System.getProperty("inputDir")), buildProfileString(), new HashMap());
}

protected static Test suite(File inputDir, String profile, Map directories) {

	String name = "FormatterMassiveRegressionTests on "+inputDir.getName();
	if (profile != null && profile.length() > 0) {
		name += " " + profile;
	}
	TestSuite suite = new Suite(name);
	try {
		// Init version
		initVersion();

		// Init profiles
		int profiles = initProfiles(profile);

		// Init directories
		initDirectories(inputDir, profiles, true);

		// Get files from input dir
		FileFilter filter = new FileFilter() {
			@Override
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
		File[] allFiles = (File[]) directories.get(inputDir);
		File listFile = new File(inputDir.getParentFile(), inputDir.getName()+".lst");
		BufferedWriter listFileWriter = null;
		if (allFiles == null) {
			System.out.print("Get all files from ");
			if (LIST || !listFile.exists()) {
				// Get the files list
				System.out.print(inputDir+"...");
				allFiles = ModelTestsUtil.getAllFiles(inputDir, filter);
				// Delete the files list
				if (listFile.exists()) {
					listFile.delete();
				}
				// Initialize the files list writer
				listFileWriter = new BufferedWriter(new FileWriter(listFile));
				listFileWriter.write(Integer.toString(allFiles.length));
				listFileWriter.newLine();
			} else {
				System.out.print("stored list in "+listFile.getPath()+"...");
				BufferedReader listFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(listFile.getAbsolutePath())));
				try {
					// First line is the number of files
					String line = listFileReader.readLine();
					int maxFiles = Integer.parseInt(line);
					// Following lines are the files path
					allFiles = new File[maxFiles];
					for (int i=0; i<maxFiles; i++) {
						allFiles[i] = new File(inputDir, listFileReader.readLine());
						if (!allFiles[i].exists()) {
							throw new IOException("Cannot find file "+allFiles[i]);
						}
					}
				}
				catch (NumberFormatException nfe) {
					nfe.printStackTrace();
					return null;
				}
				catch (IOException ioe) {
					ioe.printStackTrace();
					return null;
				}
				finally {
					listFileReader.close();
				}
			}
			directories.put(inputDir, allFiles);
			System.out.println("done");
		}
		int[] maxFiles = new int[2];
		maxFiles[0] = allFiles.length;
		maxFiles[1] = (int) (Math.log(maxFiles[0])/Math.log(10));
		MAX_FILES.put(inputDir, maxFiles);

		// Add tests to clean the output directory and rebuild the references
//		if (CLEAN) {
//			suite.addTest(new FormatterMassiveRegressionTests(profiles));
//		}

		// Add one test per found file
		try {
			final int inputDirPathLength = inputDir.getPath().length()+1;
			for (int i=0; i<maxFiles[0]; i++) {
				if (CLEAN) {
					suite.addTest(new FormatterMassiveRegressionTests(inputDir, allFiles[i], i, profiles, false/*do not compare while cleaning*/));
				} else {
					suite.addTest(new FormatterMassiveRegressionTests(inputDir, allFiles[i], i, profiles, CAN_COMPARE));
				}
				if (listFileWriter != null) {
					listFileWriter.write(allFiles[i].getPath().substring(inputDirPathLength));
					listFileWriter.newLine();
				}
			}
		}
		finally {
			if (listFileWriter != null) {
				listFileWriter.close();
			}
		}
    } catch (Exception e) {
    	e.printStackTrace();
    }
	return suite;
}

private static String buildProfileString() {
	boolean hasProfile = NO_COMMENTS || PRESERVED_LINES != -1;
	if (JOIN_LINES != null) {
	 	if (JOIN_LINES.equals("never") ||
	 		JOIN_LINES.equals("only_comments") ||
	 		JOIN_LINES.equals("only_code")) {
	 		hasProfile = true;
	 	}
	}
	if (BRACES != null) {
	 	if (BRACES.equals(DefaultCodeFormatterConstants.NEXT_LINE) ||
	 		BRACES.equals(DefaultCodeFormatterConstants.NEXT_LINE_ON_WRAP) ||
	 		BRACES.equals(DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED)) {
	 		hasProfile = true;
	 	}
	}
	String builtProfile = null;
	if (hasProfile) {
		StringBuilder buffer = new StringBuilder();
		String separator = "";
		if (JOIN_LINES != null) {
			buffer.append("join_lines="+JOIN_LINES);
			separator = ",";
		}
		if (NO_COMMENTS) {
			buffer.append(separator+"no_comments=true");
			separator = ",";
		}
		if (BRACES != null) {
			buffer.append(separator+"braces="+BRACES);
			separator = ",";
		}
		if (PRESERVED_LINES != -1) {
			buffer.append(separator+"preserved_lines="+PRESERVED_LINES);
			separator = ",";
		}
		builtProfile = buffer.toString();
	}

	// Return built profile string
	return builtProfile;
}

private static int initProfiles(String profile) {
	if (profile == null || profile.length() == 0) return 0;
	StringTokenizer tokenizer = new StringTokenizer(profile, ",");
	int profiles = 0;
	while (tokenizer.hasMoreTokens()) {
		String token = tokenizer.nextToken();
		int idx = token.indexOf('=');
		if (idx <= 0) {
			System.err.println("'"+profile+"' is not a valid profile!!!");
			return 0;
		}
		String profileName = token.substring(0, idx);
		if (profileName.equals("join_lines")) {
			String joinLines = token.substring(idx+1);
		 	if (joinLines.equals("never")) {
		 		profiles += PROFILE_NEVER_JOIN_LINES;
		 	} else if (joinLines.equals("only_comments")) {
		 		profiles += PROFILE_JOIN_LINES_ONLY_COMMENTS;
		 	} else if (joinLines.equals("only_code")) {
		 		profiles += PROFILE_JOIN_LINES_ONLY_CODE;
			}
		} else if (profileName.equals("no_comments")) {
			String noComments = token.substring(idx+1);
		 	if (noComments.equals(DefaultCodeFormatterConstants.TRUE)) {
	 			profiles |= PROFILE_NO_COMMENTS;
		 	}
		} else if (profileName.equals("braces")) {
			String braces = token.substring(idx+1);
		 	if (braces.equals(DefaultCodeFormatterConstants.NEXT_LINE)) {
		 		profiles += PROFILE_BRACES_NEXT_LINE;
		 	} else if (braces.equals(DefaultCodeFormatterConstants.NEXT_LINE_ON_WRAP)) {
		 		profiles += PROFILE_BRACES_NEXT_LINE_ON_WRAP;
		 	} else if (braces.equals(DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED)) {
		 		profiles += PROFILE_BRACES_NEXT_LINE_SHIFTED;
		 	}
		} else if (profileName.equals("preserved_lines")) {
			try {
				String lines = token.substring(idx+1);
	 			int value = Integer.parseInt(lines);
	 			if (value >= 0 && value < 8) {
		 			profiles += value << 5;
	 			}
		 	}
			catch (NumberFormatException nfe) {
				// skip
			}
		}
	}
	return profiles;
}

private static void initDirectories(File inputDir, int profiles, boolean verify) {

	// Verify input directory
	if (!inputDir.exists() && !inputDir.isDirectory()) {
		System.err.println(inputDir+" does not exist or is not a directory!");
		System.exit(1);
	}

	// Get output dir and clean it if specified
	String dir = System.getProperty("outputDir"); //$NON-NLS-1$
	if (dir != null) {
		StringTokenizer tokenizer = new StringTokenizer(dir, ",");
		String outputDir = tokenizer.nextToken();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.equals("clean")) {
				CLEAN = true;
			} else if (token.equals("maintenance")) {
				MAINTENANCE = true;
			} else if (token.equals("list")) {
				LIST = true;
			} else {
				TEMP_OUTPUT = token;
			}
		}
		setOutputDir(inputDir, outputDir, profiles);
		if (CLEAN) {
			if ((PATCH_BUG != null || JDT_CORE_HEAD) && TEMP_OUTPUT == null) {
				System.err.println("Reference can only be updated using a version (i.e. with a closed buildnotes_jdt-core.html)!");
				System.exit(1);
			}
			return;
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
		setLogDir(inputDir, profiles, verify);
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
	}
}

private static void setLogDir(File inputDir, int profiles, boolean verify) throws CoreException {

	// Compute log dir
	File rootLogDir = new File(System.getProperty("logDir"));
	if (!rootLogDir.exists()) {
		if (!rootLogDir.mkdirs()) {
			System.err.println("Cannot create specified log directory: "+rootLogDir+"!!!");
			return;
		}
	}

	// Compute log sub-directories depending on version
	rootLogDir = new File(rootLogDir, ECLIPSE_VERSION);
	String subRootDir;
	if (PATCH_BUG != null) {
		rootLogDir = new File(rootLogDir, "tests");
		rootLogDir = new File(rootLogDir, PATCH_BUG);
		subRootDir = PATCH_VERSION;
	} else if (JDT_CORE_HEAD) {
		subRootDir = "HEAD";
	} else {
		rootLogDir = new File(rootLogDir, ECLIPSE_MILESTONE);
		subRootDir = JDT_CORE_VERSION;
	}

	// Compute log sub-directories depending on profiles
	List subDirs = new ArrayList();
	if (profiles > 0) {
		subDirs.add("profiles");
		setProfilesDir(profiles, subDirs);
	}

	if (FILES_FILTER_KIND > 0) {
		subDirs.add("filter");
		subDirs.add(FILES_FILTER.replace('?', '_').replace('*', '%'));
	}

	// Create log stream
	File logDir = createDir(new File (rootLogDir, subRootDir), subDirs);
	String filePrefix = inputDir.getName().replaceAll("\\.", "");
	String logFileName = filePrefix+".txt";
	LOG_FILE = new File(logDir, logFileName);
	if (verify && LOG_FILE.exists()) {
		File saveDir = new File(new File(rootLogDir, "save"), subRootDir);
		saveDir.mkdirs();
		int i=0;
		while (true) {
			String dirN = Integer.toString(i);
			if (i<10) dirN = "0" + dirN;
			saveDir = new File(saveDir, dirN);
			logDir = createDir(saveDir, subDirs);
			File renamedFile = new File(logDir, logFileName);
			if (LOG_FILE.renameTo(renamedFile)) break;
			i++;
		}
	}
//	LOG_RESOURCE = folder.getFile(logFileName);
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

private static File createDir(File rootDir, List subDirs) {
	File dir = rootDir;
	for (int i=0, s=subDirs.size(); i<s; i++) {
		dir = new File (dir, (String) subDirs.get(i));
	}
	dir.mkdirs();
	return dir;
}

private static File setProfilesDir(int profiles, File dir) {
	List subDirs = new ArrayList();
	setProfilesDir(profiles, subDirs);
	return createDir(dir, subDirs);
}

private static void setProfilesDir(int profiles, List subDirs) {
	String joinLines = null;
	switch (profiles & PROFILE_JOIN_LINES_MASK) {
		case PROFILE_NEVER_JOIN_LINES:
			joinLines = "never";
			break;
		case PROFILE_JOIN_LINES_ONLY_COMMENTS:
			joinLines = "only_comments";
			break;
		case PROFILE_JOIN_LINES_ONLY_CODE:
			joinLines = "only_code";
			break;
	}
	if (joinLines != null) {
		subDirs.add("join_lines");
		subDirs.add(joinLines);
	}
	if ((profiles & PROFILE_NO_COMMENTS) != 0) {
		subDirs.add("no_comments");
	}
	String braces = null;
	switch (profiles & PROFILE_BRACES_MASK) {
		case PROFILE_BRACES_NEXT_LINE:
			braces = DefaultCodeFormatterConstants.NEXT_LINE;
			break;
		case PROFILE_BRACES_NEXT_LINE_ON_WRAP:
			braces = DefaultCodeFormatterConstants.NEXT_LINE_ON_WRAP;
			break;
		case PROFILE_BRACES_NEXT_LINE_SHIFTED:
			braces = DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED;
			break;
	}
	if (braces != null) {
		subDirs.add("braces");
		subDirs.add(braces);
	}
	if ((profiles & PROFILE_PRESERVED_LINES_MASK) != 0) {
		int lines = (profiles & PROFILE_PRESERVED_LINES_MASK) >> 5;
		subDirs.add("preserved_lines");
		subDirs.add(Integer.toString(lines));
	}
}

private static void appendProfiles(int profiles, StringBuffer buffer) {
	String joinLines = null;
	boolean first = true;
	switch (profiles & PROFILE_JOIN_LINES_MASK) {
		case PROFILE_NEVER_JOIN_LINES:
			joinLines = "never";
			break;
		case PROFILE_JOIN_LINES_ONLY_COMMENTS:
			joinLines = "only_comments";
			break;
		case PROFILE_JOIN_LINES_ONLY_CODE:
			joinLines = "only_code";
			break;
	}
	if (joinLines != null) {
		buffer.append("join_lines=");
		buffer.append(joinLines);
		first = false;
	}
	if ((profiles & PROFILE_NO_COMMENTS) != 0) {
		if (!first) buffer.append(',');
		buffer.append("no_comments");
		first = false;
	}
	String braces = null;
	switch (profiles & PROFILE_BRACES_MASK) {
		case PROFILE_BRACES_NEXT_LINE:
			braces = DefaultCodeFormatterConstants.NEXT_LINE;
			break;
		case PROFILE_BRACES_NEXT_LINE_ON_WRAP:
			braces = DefaultCodeFormatterConstants.NEXT_LINE_ON_WRAP;
			break;
		case PROFILE_BRACES_NEXT_LINE_SHIFTED:
			braces = DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED;
			break;
	}
	if (braces != null) {
		if (!first) buffer.append(',');
		buffer.append("braces=");
		buffer.append(braces);
		first = false;
	}
	if ((profiles & PROFILE_PRESERVED_LINES_MASK) != 0) {
		int lines = (profiles & PROFILE_PRESERVED_LINES_MASK) >> 5;
		if (!first) buffer.append(',');
		buffer.append("preserved_lines=");
		buffer.append(lines);
		first = false;
	}
	if (first) {
		buffer.append("none!");
	}
}

private static void setOutputDir(File inputDir, String dir, int profiles) {

	// Find the root of the output directory
	OUTPUT_DIR = new File(dir);
	if (OUTPUT_DIR.getName().equals(inputDir.getName())) {
		OUTPUT_DIR = OUTPUT_DIR.getParentFile();
	}
	if (OUTPUT_DIR.getName().equals(ECLIPSE_VERSION)) {
		OUTPUT_DIR = OUTPUT_DIR.getParentFile();
	}

	// Add the temporary output if any
	if (TEMP_OUTPUT != null) {
		StringTokenizer tokenizer = new StringTokenizer(TEMP_OUTPUT, "/");
		while (tokenizer.hasMoreTokens()) {
			OUTPUT_DIR = new File(OUTPUT_DIR, tokenizer.nextToken());
		}
	}

	// Compute output sub-directories depending on profiles
	if (profiles > 0) {
		OUTPUT_DIR = new File(OUTPUT_DIR, "profiles");
		OUTPUT_DIR = setProfilesDir(profiles, OUTPUT_DIR);
	}

	// Compute the final output dir
	File parent = new File(OUTPUT_DIR, ECLIPSE_VERSION);
	if (MAINTENANCE) {
		try {
			int version = Integer.parseInt(ECLIPSE_VERSION.substring(1));
			File maintenance = new File(OUTPUT_DIR, "v"+(version-1));
			if (maintenance.exists()) {
				parent = maintenance;
			}
		}
		catch (NumberFormatException nfe) {
			// skip
		}
	}
	OUTPUT_DIR = new File(parent, inputDir.getName());
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
private static void initVersion() {
	if (JDT_CORE_VERSION == null) {
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
							// try to split
							StringTokenizer bugTokenizer = new StringTokenizer(PATCH_BUG, "+");
							try {
								while (bugTokenizer.hasMoreTokens()) {
									Integer.parseInt(bugTokenizer.nextToken());
								}
							}
							catch (NumberFormatException nfe2) {
								System.err.println("Invalid patch bug number noticed in JDT/Core buildnotes: "+PATCH_BUG);
							}
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
	}
}

/*
 * Constructor used to clean the output directory.
 *
public FormatterMassiveRegressionTests(int profiles) {
	super("testDeleteOutputDir");
	this.canCompare = false;
	this.file = null;
	this.inputDir = OUTPUT_DIR;
	this.testIndex = -1;
	this.profiles = profiles;
	this.path = new Path(OUTPUT_DIR.getPath());
}

/*
 * Constructor used to dump references in the output directory.
 *
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
public FormatterMassiveRegressionTests(File inputDir, File file, int index, int profiles, boolean compare) {
	super(CLEAN ? "testReference" : "testCompare");
	this.canCompare = compare;
	this.file = file;
	this.inputDir = inputDir;
	this.testIndex = index;
	this.profiles = profiles;
	this.path = new Path(file.getPath().substring(inputDir.getPath().length()+1));
}

/* (non-Javadoc)
 * @see junit.framework.TestCase#getName()
 */
public String getName() {
	StringBuilder name = new StringBuilder(super.getName());
	if (this.testIndex >= 0) {
		int n = this.testIndex == 0 ? 0 : (int) (Math.log(this.testIndex)/Math.log(10));
		int max = ((int[])MAX_FILES.get(this.inputDir))[1];
		for (int i=n; i<max; i++) {
			name.append('0');
		}
		name.append(this.testIndex);
	}
	if (this.profiles > 0) {
		name.append('_');
		name.append(this.profiles);
	}
	name.append(" - ");
	name.append(this.path);
	return name.toString();
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests#setUpSuite()
 */
@Override
public void setUp() throws Exception {
	super.setUp();

	// Setup preferences
	this.preferences = DefaultCodeFormatterOptions.getEclipseDefaultSettings();

	// Setup no comments profile
	if ((this.profiles & PROFILE_NO_COMMENTS) != 0) {
		this.preferences.comment_format_javadoc_comment = false;
		this.preferences.comment_format_block_comment = false;
		this.preferences.comment_format_line_comment = false;
	}

	// Setup join lines profile
	String joinLines = null;
	switch (this.profiles & PROFILE_JOIN_LINES_MASK) {
		case PROFILE_NEVER_JOIN_LINES:
			joinLines = "never";
			break;
		case PROFILE_JOIN_LINES_ONLY_COMMENTS:
			joinLines = "only_comments";
			break;
		case PROFILE_JOIN_LINES_ONLY_CODE:
			joinLines = "only_code";
			break;
	}
	if (joinLines != null) {
		if (!joinLines.equals("only_comments")) {
			this.preferences.join_lines_in_comments = false;
		}
		if (!joinLines.equals("only_code")) {
			this.preferences.join_wrapped_lines = false;
		}
	}

	// Setup braces profile
	String braces = null;
	switch (this.profiles & PROFILE_BRACES_MASK) {
		case PROFILE_BRACES_NEXT_LINE:
			braces = DefaultCodeFormatterConstants.NEXT_LINE;
			break;
		case PROFILE_BRACES_NEXT_LINE_ON_WRAP:
			braces = DefaultCodeFormatterConstants.NEXT_LINE_ON_WRAP;
			break;
		case PROFILE_BRACES_NEXT_LINE_SHIFTED:
			braces = DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED;
			break;
	}
	if (braces != null) {
		this.preferences.brace_position_for_annotation_type_declaration = braces;
		this.preferences.brace_position_for_anonymous_type_declaration = braces;
		this.preferences.brace_position_for_array_initializer = braces;
		this.preferences.brace_position_for_block = braces;
		this.preferences.brace_position_for_block_in_case = braces;
		this.preferences.brace_position_for_constructor_declaration = braces;
		this.preferences.brace_position_for_enum_constant = braces;
		this.preferences.brace_position_for_enum_declaration = braces;
		this.preferences.brace_position_for_method_declaration = braces;
		this.preferences.brace_position_for_switch = braces;
		this.preferences.brace_position_for_type_declaration = braces;
	}

	// Setup preserved lines profile
	if ((this.profiles & PROFILE_PRESERVED_LINES_MASK) != 0) {
		int lines = (this.profiles & PROFILE_PRESERVED_LINES_MASK) >> 5;
		this.preferences.number_of_empty_lines_to_preserve = lines;
	}
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests#setUpSuite()
 */
@Override
public void setUpSuite() throws Exception {

	// Init directories
	initDirectories(this.inputDir, this.profiles, false);

	// Delete output dir before compute reference
	if (CLEAN) {
		System.out.print("Deleting all files from "+OUTPUT_DIR+"...");
		Util.delete(OUTPUT_DIR);
		System.out.println("done");
	}
	// Init failure
	else if (this.canCompare) {
		initFailures();
	}

	// Dump the version
	if (CLEAN) {
		File versionFile = new Path(OUTPUT_DIR.getPath()).append("version.txt").toFile();
		OUTPUT_DIR.mkdirs();
		String version = JDT_CORE_VERSION;
		if (TEMP_OUTPUT != null) {
				version += " + " + TEMP_OUTPUT;
			if (PATCH_BUG != null) {
				version += " " + PATCH_VERSION + " of " + PATCH_BUG;
			}
		}
		Util.writeToFile(version, versionFile.getAbsolutePath());
	}

	// Init time measuring
	TIME_MEASURES = new TimeMeasuring();

	// Print
	print();
}

private void print() {

	StringBuffer buffer = new StringBuffer();

	// Log version info
	buffer.append("Version   : ");
	if (PATCH_BUG != null) {
		buffer.append("'Patch ");
		buffer.append(PATCH_VERSION);
		buffer.append(" for bug ");
		buffer.append(PATCH_BUG);
		buffer.append("' applied on ");
	}
	if (JDT_CORE_HEAD) {
		buffer.append("HEAD on top of ");
	}
	buffer.append(JDT_CORE_VERSION);
	buffer.append(LINE_SEPARATOR);

	// Profiles
	buffer.append("Profiles  : ");
	appendProfiles(this.profiles, buffer);
	buffer.append(LINE_SEPARATOR);

	// Log date of test
	long start = System.currentTimeMillis();
	SimpleDateFormat format = new SimpleDateFormat();
	Date now = new Date(start);
	buffer.append("Test date : ");
	buffer.append(format.format(now));
	buffer.append(LINE_SEPARATOR);

	// Input dir
	buffer.append("Input dir : ");
	buffer.append(this.inputDir);
	buffer.append(LINE_SEPARATOR);

	// Files
	buffer.append("            ");
	int[] maxFiles = (int[]) MAX_FILES.get(this.inputDir);
	buffer.append(maxFiles[0]);
	buffer.append(" java files to format...");

	// Flush to console to show startup
	String firstBuffer = buffer.toString();
	System.out.println(firstBuffer);

	// Output dir
	buffer.setLength(0);
	buffer.append("Output dir: ");
	buffer.append(OUTPUT_DIR);
	buffer.append(LINE_SEPARATOR);
	if (CLEAN) {
		buffer.append("            CLEANED");
		buffer.append(LINE_SEPARATOR);
	}

	// Log dir
	if (LOG_FILE != null) {
		buffer.append("Log file  : ");
		buffer.append(LOG_FILE);
		buffer.append(LINE_SEPARATOR);
	}

	// Write dir
	if (WRITE_DIR != null) {
		buffer.append("Write dir : ");
		buffer.append(WRITE_DIR);
		buffer.append(LINE_SEPARATOR);
	}

	// Comparison
	if (CAN_COMPARE) {
		if (!CLEAN) {
			buffer.append("Compare vs: ");
			File versionFile = new File(OUTPUT_DIR, "version.txt");
			if (versionFile.exists()) {
				String fileContent = Util.fileContent(versionFile.getAbsolutePath());
				if (TEMP_OUTPUT != null) {
					buffer.append(TEMP_OUTPUT);
					buffer.append(" on top of ");
				}
				buffer.append(fileContent);
			} else {
				buffer.append("???");
			}
		}
	} else {
		buffer.append("Compare vs: none");
	}
	buffer.append(LINE_SEPARATOR);

	// Write logs
	System.out.println(buffer.toString());
	if (LOG_STREAM != null) {
		LOG_STREAM.println(firstBuffer);
		LOG_STREAM.println(buffer.toString());
		LOG_STREAM.flush();
	}
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests#tearDown()
 */
@Override
public void tearDown() throws Exception {
	// verify whether the max failures has been reached or not
	if (ASSERT_EQUALS_STRINGS && FAILURES != null) {
		ASSERT_EQUALS_STRINGS = FAILURES[COMPARISON_FAILURE].size() < MAX_FAILURES;
	}
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.formatter.FormatterRegressionTests#tearDownSuite()
 */
@Override
public void tearDownSuite() throws Exception {

	// Display time measures
	StringBuilder buffer1 = new StringBuilder();
	if (CLEAN) {
//		buffer1.append(" cannot be done as the directory was cleaned!");
//		buffer1.append(LINE_SEPARATOR);
		return;
	} else {
		buffer1.append("Time measures:");
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
	StringBuilder buffer2 = new StringBuilder(LINE_SEPARATOR);
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
@Override
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

@Override
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
			try {
				String expectedResult = new String(org.eclipse.jdt.internal.compiler.util.Util.getFileCharContent(outputFile, null));
				assertSourceEquals("Unexpected format output!", expectedResult, actualResult);
			}
			catch (FileNotFoundException fnfe) {
				this.failureIndex = FILE_NOT_FOUND_FAILURE;
				FAILURES[FILE_NOT_FOUND_FAILURE].failures.add(this.path);
				return;
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
	StringBuilder buffer = new StringBuilder();
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

/*
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
*/

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

	@Override
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
//		if (!isExpectedFailure()) {
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
//		}
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
	StringBuilder buffer = new StringBuilder();
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
 *
public void testDeleteOutputDir() throws IOException, Exception {
	Util.delete(this.inputDir);
}

/*
 * Test to fill the output directory with reference.
 */
public void testReference() throws IOException, Exception {

	// Dump the version
//	if (this.testIndex == 0) {
//		File versionFile = new Path(OUTPUT_DIR.getPath()).append("version.txt").toFile();
//		OUTPUT_DIR.mkdirs();
//		Util.writeToFile(JDT_CORE_VERSION, versionFile.getAbsolutePath());
//	}

	// Get the source from file
	String source = new String(org.eclipse.jdt.internal.compiler.util.Util.getFileCharContent(this.file, null));
	try {
		// Format the source
		TextEdit edit = codeFormatter().format(CodeFormatter.K_COMPILATION_UNIT | CodeFormatter.F_INCLUDE_COMMENTS, source, 0, source.length(), 0, null);

		// Write the result
		if (edit != null) {
			String formatResult = org.eclipse.jdt.internal.core.util.Util.editedString(source, edit);
			String inputPath = this.file.getPath().substring(this.inputDir.getPath().length()+1);
			File writtenFile = new Path(OUTPUT_DIR.getPath()).append(inputPath).toFile();
			writtenFile.getParentFile().mkdirs();
			Util.writeToFile(formatResult, writtenFile.getAbsolutePath());
		}
	}
	catch (Exception ex) {
		// skip silently
	}
}

/*
 * Test to compare the formatter output with an existing file.
 */
public void testCompare() throws IOException, Exception {
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
