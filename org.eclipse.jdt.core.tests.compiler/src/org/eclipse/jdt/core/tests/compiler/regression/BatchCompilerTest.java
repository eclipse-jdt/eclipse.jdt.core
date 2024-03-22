/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
 *     Benjamin Muskalla - Contribution for bug 239066
 *     Stephan Herrmann <stephan@cs.tu-berlin.de> - Contributions for
 *     							bug 236385 - [compiler] Warn for potential programming problem if an object is created but not used
 *     							bug 295551 - Add option to automatically promote all warnings to errors
 *     							bug 185682 - Increment/decrement operators mark local variables as read
 *     							bug 349326 - [1.7] new warning for missing try-with-resources
 *     							bug 359721 - [options] add command line option for new warning token "resource"
 *     							bug 186342 - [compiler][null] Using annotations for null checking
 *								bug 365208 - [compiler][batch] command line options for annotation based null analysis
 *								bug 370639 - [compiler][resource] restore the default for resource leak warnings
 *								bug 365859 - [compiler][null] distinguish warnings based on flow analysis vs. null annotations
 *								bug 374605 - Unreasonable warning for enum-based switch statements
 *								bug 375366 - ECJ ignores unusedParameterIncludeDocCommentReference unless enableJavadoc option is set
 *								bug 388281 - [compiler][null] inheritance of null annotations as an option
 *								bug 381443 - [compiler][null] Allow parameter widening from @NonNull to unannotated
 *								bug 383368 - [compiler][null] syntactic null analysis for field references
 *								Bug 392099 - [1.8][compiler][null] Apply null annotation on types for null analysis
 *								Bug 440477 - [null] Infrastructure for feeding external annotations into compilation
 *								Bug 440687 - [compiler][batch][null] improve command line option for external annotations
 *								Bug 408815 - [batch][null] Add CLI option for COMPILER_PB_SYNTACTIC_NULL_ANALYSIS_FOR_FIELDS
 *     Jesper Steen Moller - Contributions for
 *								bug 404146 - [1.7][compiler] nested try-catch-finally-blocks leads to unrunnable Java byte code
 *								bug 407297 - [1.8][compiler] Control generation of parameter names by option
 *                              bug 413873 - Warning "Method can be static" on method referencing a non-static inner class
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;

import javax.lang.model.SourceVersion;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.ClasspathDirectory;
import org.eclipse.jdt.internal.compiler.batch.ClasspathJar;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.util.ManifestAnalyzer;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class BatchCompilerTest extends AbstractBatchCompilerTest {

	static {
//		TESTS_NAMES = new String[] { "test440477" };
//		TESTS_NUMBERS = new int[] { 306 };
//		TESTS_RANGE = new int[] { 298, -1 };
	}
	public BatchCompilerTest(String name) {
		super(name);
	}
	/**
	 * This test suite only needs to be run on one compliance.
	 * As it includes some specific 1.5 tests, it must be used with a least a 1.5 VM
	 * and not be duplicated in general test suite.
	 * @see TestAll
	 */
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_5);
	}
	public static Class testClass() {
		return BatchCompilerTest.class;
	}
	static class StringMatcher extends Matcher {
		private final String expected;
		private final Normalizer normalizer;
		StringMatcher(String expected, Normalizer normalizer) {
			this.expected = expected;
			this.normalizer = normalizer;
		}
		@Override
		boolean match(String effective) {
			if (this.expected == null) {
				return effective == null;
			}
			if (this.normalizer == null) {
				return this.expected.equals(effective);
			}
			return this.expected.equals(this.normalizer.normalized(effective));
		}
		@Override
		String expected() {
			return this.expected;
		}
	}
	static class SubstringMatcher extends Matcher {
		private final String substring;
		SubstringMatcher(String substring) {
			this.substring = substring;
		}
		@Override
		boolean match(String effective) {
			effective = outputDirNormalizer.normalized(effective);
			return effective.indexOf(this.substring) != -1;
		}
		@Override
		String expected() {
			return "*" + this.substring + "*";
		}
	}
	static final Matcher EMPTY_STRING_MATCHER = new Matcher() {
		@Override
		String expected() {
			return org.eclipse.jdt.internal.compiler.util.Util.EMPTY_STRING;
		}
		@Override
		boolean match(String effective) {
			return effective != null && effective.length() == 0;
		}
	};
	static final Matcher ONE_FILE_GENERATED_MATCHER = new SubstringMatcher("[1 .class file generated]");
	static final Matcher TWO_FILES_GENERATED_MATCHER = new SubstringMatcher("[2 .class files generated]");

	/**
	 * This normalizer replaces the whole classpaths section of a log file with
	 * a normalized placeholder.
	 */
	private static class XMLClasspathsSectionNormalizer extends Normalizer {
		XMLClasspathsSectionNormalizer() {
			super(null);
		}
		XMLClasspathsSectionNormalizer(Normalizer nextInChain) {
			super(nextInChain);
		}
		@Override
		String normalized(String originalValue) {
			String result;
			StringBuilder normalizedValueBuffer = new StringBuilder(originalValue);
			int classpathsStartTagStart = normalizedValueBuffer
					.indexOf("<classpaths>"), classpathsEndTagStart = normalizedValueBuffer
					.indexOf("</classpaths>");
			if (classpathsStartTagStart != -1 && classpathsEndTagStart != -1
					&& classpathsStartTagStart < classpathsEndTagStart)
				normalizedValueBuffer.replace(classpathsStartTagStart + 12,
						classpathsEndTagStart, "NORMALIZED SECTION");
			result = super.normalized(normalizedValueBuffer.toString());
			return result;
		}
	}

	/**
	 * This normalizer removes a selected range of lines from a log file.
	 */
	private static class LinesRangeNormalizer extends Normalizer {
		private final int first;
		private int number;

		LinesRangeNormalizer() {
			super(null);
			this.first = this.number = 0;
		}

		LinesRangeNormalizer(Normalizer nextInChain) {
			super(nextInChain);
			this.first = this.number = 0;
		}

		/**
		 * Make a new normalizer able to suppress a range of lines delimited by
		 * "\n" sequences from a log file (or another string).
		 *
		 * @param nextInChain
		 *            the next normalizer in the chain of responsibility; pass
		 *            null if none is needed
		 * @param firstLineToRemove
		 *            the index of the first line to remove, starting at 0
		 * @param linesNumber
		 *            the number or lines to remove; if 0, no other
		 *            transformation occurs than those operated by nextInChain
		 *            (if any)
		 */
		LinesRangeNormalizer(Normalizer nextInChain, int firstLineToRemove,
				int linesNumber) {
			super(nextInChain);
			this.first = firstLineToRemove;
			this.number = linesNumber >= 0 ? linesNumber : 0;
		}

		@Override
		String normalized(String originalValue) {
			String result;
			if (this.number == 0 || originalValue.length() == 0)
				result = super.normalized(originalValue);
			else {
				final int START = 0, KEEPING = 1, KEEPING_R = 2, SKIPING = 3, SKIPING_R = 4, END = 5, ERROR = 6;
				int state = START, currentLineIndex = 0, currentCharIndex = 0, sourceLength;
				char currentChar = '\0';
				if (this.first <= 0)
					state = SKIPING;
				else
					state = KEEPING;
				StringBuilder normalizedValueBuffer = new StringBuilder(), source = new StringBuilder(
						originalValue);
				sourceLength = source.length();
				while (state != END && state != ERROR) {
					if (currentCharIndex < sourceLength) {
						currentChar = source.charAt(currentCharIndex++);
						switch (currentChar) {
						case '\r':
							switch (state) {
							case KEEPING:
								normalizedValueBuffer.append(currentChar);
								state = KEEPING_R;
								break;
							case SKIPING:
								state = SKIPING_R;
								break;
							default:
								state = ERROR;
							}
							break;
						case '\n':
							currentLineIndex++;
							switch (state) {
							case KEEPING: // tolerate Linux line delimiters
							case KEEPING_R:
								normalizedValueBuffer.append(currentChar);
								if (currentLineIndex == this.first) {
									state = SKIPING;
								}
								break;
							case SKIPING: // tolerate Linux line delimiters
							case SKIPING_R:
								// in effect, we tolerate too big first and number
								// values
								if (currentLineIndex >= this.first + this.number) {
									if (currentCharIndex < sourceLength)
										normalizedValueBuffer.append(source
												.substring(currentCharIndex));
									state = END;
								} else {
									state = SKIPING;
								}
								break;
							default:
								state = ERROR;
							}
							break;
						default:
							switch (state) {
							case KEEPING:
								normalizedValueBuffer.append(currentChar);
								break;
							case SKIPING:
								break;
							default:
								state = ERROR;
							}

						}
					}
					else if (currentChar == '\n')
						state = END;
					else
						state = ERROR;
				}
				if (state == ERROR)
					normalizedValueBuffer
							.append("UNEXPECTED ERROR in LinesRangeNormalizer");
				result = super.normalized(normalizedValueBuffer.toString());
			}
			return result;
		}
	}

	/**
	 * Normalizer instance for non XML log files.
	 */
	private static Normalizer textLogsNormalizer = new StringNormalizer(
			new XMLClasspathsSectionNormalizer(new LinesRangeNormalizer(null,
					0, 2)), OUTPUT_DIR, OUTPUT_DIR_PLACEHOLDER);

	/**
	 * Normalizer instance for XML log files.
	 */
	private static Normalizer xmlLogsNormalizer = new StringNormalizer(
			new XMLClasspathsSectionNormalizer(new LinesRangeNormalizer(null,
					1, 1)), OUTPUT_DIR, OUTPUT_DIR_PLACEHOLDER);


public void test001() {

		String commandLine = "-classpath \"D:/a folder\";d:/jdk1.4/jre/lib/rt.jar -1.4 -preserveAllLocals -g -verbose d:/eclipse/workspaces/development2.0/plugins/Bar/src2/ -d d:/test";
		String expected = " <-classpath> <D:/a folder;d:/jdk1.4/jre/lib/rt.jar> <-1.4> <-preserveAllLocals> <-g> <-verbose> <d:/eclipse/workspaces/development2.0/plugins/Bar/src2/> <-d> <d:/test>";

		String[] args = Main.tokenize(commandLine);
		StringBuilder  buffer = new StringBuilder(30);
		for (int i = 0; i < args.length; i++){
			buffer.append(" <"+args[i]+">");
		}
		String result = buffer.toString();
		//System.out.println(Util.displayString(result, 2));
		assertEquals("incorrect tokenized command line",
			expected,
			result);
}
public void test002() {

		String commandLine = "-classpath \"a folder\";\"b folder\"";
		String expected = " <-classpath> <a folder;b folder>";

		String[] args = Main.tokenize(commandLine);
		StringBuilder  buffer = new StringBuilder(30);
		for (int i = 0; i < args.length; i++){
			buffer.append(" <"+args[i]+">");
		}
		String result = buffer.toString();
		//System.out.println(Util.displayString(result, 2));
		assertEquals("incorrect tokenized command line",
			expected,
			result);
}
public void test003() {

		String commandLine = "-classpath \"a folder;b folder\"";
		String expected = " <-classpath> <a folder;b folder>";

		String[] args = Main.tokenize(commandLine);
		StringBuilder  buffer = new StringBuilder(30);
		for (int i = 0; i < args.length; i++){
			buffer.append(" <"+args[i]+">");
		}
		String result = buffer.toString();
		//System.out.println(Util.displayString(result, 2));
		assertEquals("incorrect tokenized command line",
			expected,
			result);
}
public void test004() {

		String commandLine = "\"d:/tmp A/\"A.java  -classpath \"d:/tmp A\";d:/jars/rt.jar -nowarn -time -g -d d:/tmp";
		String expected = " <d:/tmp A/A.java> <-classpath> <d:/tmp A;d:/jars/rt.jar> <-nowarn> <-time> <-g> <-d> <d:/tmp>";

		String[] args = Main.tokenize(commandLine);
		StringBuilder  buffer = new StringBuilder(30);
		for (int i = 0; i < args.length; i++){
			buffer.append(" <"+args[i]+">");
		}
		String result = buffer.toString();
		//System.out.println(Util.displayString(result, 2));
		assertEquals("incorrect tokenized command line",
			expected,
			result);
}
public void test005() {

		String commandLine = "\"d:/tmp A/\"A.java  -classpath d:/jars/rt.jar;\"d:/tmp A\";\"toto\" -nowarn -time -g -d d:/tmp";
		String expected = " <d:/tmp A/A.java> <-classpath> <d:/jars/rt.jar;d:/tmp A;toto> <-nowarn> <-time> <-g> <-d> <d:/tmp>";

		String[] args = Main.tokenize(commandLine);
		StringBuilder  buffer = new StringBuilder(30);
		for (int i = 0; i < args.length; i++){
			buffer.append(" <"+args[i]+">");
		}
		String result = buffer.toString();
		//System.out.println(Util.displayString(result, 2));
		assertEquals("incorrect tokenized command line",
			expected,
			result);
}
public void test006() {

		String commandLine = "\"d:/tmp A/A.java\"  -classpath d:/jars/rt.jar;\"d:/tmp A\";d:/tmpB/ -nowarn -time -g -d d:/tmp";
		String expected = " <d:/tmp A/A.java> <-classpath> <d:/jars/rt.jar;d:/tmp A;d:/tmpB/> <-nowarn> <-time> <-g> <-d> <d:/tmp>";

		String[] args = Main.tokenize(commandLine);
		StringBuilder  buffer = new StringBuilder(30);
		for (int i = 0; i < args.length; i++){
			buffer.append(" <"+args[i]+">");
		}
		String result = buffer.toString();
		//System.out.println(Util.displayString(result, 2));
		assertEquals("incorrect tokenized command line",
			expected,
			result);
}
// test the tester - runConformTest
public void test007(){
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				
				@SuppressWarnings("all"//$NON-NLS-1$
				)
				public class X {
					public static void main(String[] args) {
						if (false) {
							;
						} else {
						}
						// Zork z;
					}
				}"""
        },
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -bootclasspath " + getLibraryClassesAsQuotedString()
        + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -verbose -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        """
			[parsing    ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]
			[reading    java/lang/Object.class]
			[reading    java/lang/SuppressWarnings.class]
			[reading    java/lang/String.class]
			[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]
			[reading    java/util/List.class]
			[writing    X.class - #1]
			[completed  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]
			[1 unit compiled]
			[1 .class file generated]
			""",
        "", // changed with bug 123522: now the SuppressWarning upon the first type
        	// influences warnings on unused imports
        true);
}
// test the tester - runNegativeTest
public void test008(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				
				@SuppressWarnings("all"//$NON-NLS-1$
				)
				public class X {
					public static void main(String[] args) {
						if (false) {
							;
						} else {
						}
						Zork z;
					}
				}"""
        },
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -bootclasspath " + getLibraryClassesAsQuotedString()
        + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        """
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 11)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			1 problem (1 error)
			""",
        true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=92398 -- a case that works, another that does not
// revisit this test case depending on https://bugs.eclipse.org/bugs/show_bug.cgi?id=95349
public void test009(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
					OK1 ok1;
					OK2 ok2;
					Warn warn;
					KO ko;
					Zork z;
				}""",
			"OK1.java",
			"""
				/** */
				public class OK1 {
					// empty
				}""",
			"OK2.java",
			"""
				/** */
				public class OK2 {
					// empty
				}""",
			"Warn.java",
			"""
				/** */
				public class Warn {
					// empty
				}""",
			"KO.java",
			"""
				/** */
				public class KO {
					// empty
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[+OK2" + File.pathSeparator + "~Warn"
        	+ File.pathSeparator + "-KO]\""
        + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        """
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				Warn warn;
				^^^^
			Discouraged access: The type \'Warn\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---\')
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
				KO ko;
				^^
			Access restriction: The type \'KO\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---\')
			----------
			3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			3 problems (1 error, 2 warnings)
			""",
        true);
}
// command line - no user classpath nor bootclasspath
public void test010(){
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.List;
				
				@SuppressWarnings("all"//$NON-NLS-1$
				)
				public class X {
					public static void main(String[] args) {
						if (false) {
							;
						} else {
						}
						// Zork z;
					}
				}"""
        },
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -verbose -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        """
			[parsing    ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]
			[reading    java/lang/Object.class]
			[reading    java/lang/SuppressWarnings.class]
			[reading    java/lang/String.class]
			[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]
			[reading    java/util/List.class]
			[writing    X.class - #1]
			[completed  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]
			[1 unit compiled]
			[1 .class file generated]
			""",
        "",
        true);
}
// command line - unusual classpath (ends with ';', still OK)
public void test011_classpath(){
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[+**/OK2;~**/Warn;-KO]"
        + "\"" + File.pathSeparator
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
}
private String getVersionOptions() {
	StringBuilder builder = new StringBuilder();
	String template = "    -15 -15.0          use 15  compliance (-source 15  -target 15)\n";
	for(int i = ClassFileConstants.MAJOR_VERSION_15; i <= ClassFileConstants.MAJOR_LATEST_VERSION; i++) {
		builder.append(template.replace("15", "" + (i - ClassFileConstants.MAJOR_VERSION_0)));
	}
	return builder.toString();
}
// command line - help
// amended for https://bugs.eclipse.org/bugs/show_bug.cgi?id=141512 (checking
// width)
public void test012(){
	final String expectedOutput =
        "{0} {1}\n" +
        "{2}\n" +
        " \n" +
        " Usage: <options> <source files | directories>\n" +
        " If directories are specified, then their source contents are compiled.\n" +
        " Possible options are listed below. Options enabled by default are prefixed\n" +
        " with ''+''.\n" +
        " \n" +
        " Classpath options:\n" +
        "    -cp -classpath <directories and ZIP archives separated by " + File.pathSeparator + ">\n" +
        "                       specify location for application classes and sources.\n" +
        "                       Each directory or file can specify access rules for\n" +
        "                       types between ''['' and '']'' (e.g. [-X] to forbid\n" +
        "                       access to type X, [~X] to discourage access to type X,\n" +
        "                       [+p/X" + File.pathSeparator + "-p/*] to forbid access to all types in package p\n" +
        "                       but allow access to p/X)\n" +
        "    -bootclasspath <directories and ZIP archives separated by " + File.pathSeparator + ">\n" +
        "                       specify location for system classes. Each directory or\n" +
        "                       file can specify access rules for types between ''[''\n" +
        "                       and '']''\n" +
        "    -sourcepath <directories and ZIP archives separated by " + File.pathSeparator + ">\n" +
        "                       specify location for application sources. Each directory\n" +
        "                       or file can specify access rules for types between ''[''\n" +
        "                       and '']''. Each directory can further specify a specific\n" +
        "                       destination directory using a ''-d'' option between ''[''\n" +
        "                       and '']''; this overrides the general ''-d'' option.\n" +
        "                       .class files created from source files contained in a\n" +
        "                       jar file are put in the user.dir folder in case no\n" +
        "                       general ''-d'' option is specified. ZIP archives cannot\n" +
        "                       override the general ''-d'' option\n" +
        "    -extdirs <directories separated by " + File.pathSeparator + ">\n" +
        "                       specify location for extension ZIP archives\n" +
        "    -endorseddirs <directories separated by " + File.pathSeparator + ">\n" +
        "                       specify location for endorsed ZIP archives\n" +
        "    -d <dir>           destination directory (if omitted, no directory is\n" +
        "                       created); this option can be overridden per source\n" +
        "                       directory\n" +
        "    -d none            generate no .class files\n" +
        "    -encoding <enc>    specify default encoding for all source files. Each\n" +
        "                       file/directory can override it when suffixed with\n" +
        "                       ''[''<enc>'']'' (e.g. X.java[utf8]).\n" +
        "                       If multiple default encodings are specified, the last\n" +
        "                       one will be used.\n" +
        " \n" +
        " Module compilation options:\n" +
        "   These options are meaningful only in Java 9 environment or later.\n" +
        "    --module-source-path <directories separated by " + File.pathSeparator + ">\n" +
        "                       specify where to find source files for multiple modules\n" +
        "    -p --module-path <directories separated by " + File.pathSeparator + ">\n" +
        "                       specify where to find application modules\n" +
        "    --processor-module-path <directories separated by " + File.pathSeparator + ">\n" +
        "                       specify module path where annotation processors\n" +
        "                       can be found\n" +
        "    --system <jdk>     Override location of system modules\n" +
        "    --add-exports <module>/<package>=<other-module>(,<other-module>)*\n" +
        "                       specify additional package exports clauses to the\n" +
        "                       given modules\n" +
        "    --add-reads <module>=<other-module>(,<other-module>)*\n" +
        "                       specify additional modules to be considered as required\n" +
        "                       by given modules\n" +
        "    --add-modules  <module>(,<module>)*\n" +
        "                       specify the additional module names that should be\n" +
        "                       resolved to be root modules\n" +
        "    --limit-modules <module>(,<module>)*\n" +
        "                       specify the observable module names\n" +
        "    --release <release> compile for a specific VM version\n" +
        " \n" +
        " Compliance options:\n" +
        "    -1.3               use 1.3 compliance (-source 1.3 -target 1.1)\n" +
        "    -1.4             + use 1.4 compliance (-source 1.3 -target 1.2)\n" +
        "    -1.5 -5 -5.0       use 1.5 compliance (-source 1.5 -target 1.5)\n" +
        "    -1.6 -6 -6.0       use 1.6 compliance (-source 1.6 -target 1.6)\n" +
        "    -1.7 -7 -7.0       use 1.7 compliance (-source 1.7 -target 1.7)\n" +
        "    -1.8 -8 -8.0       use 1.8 compliance (-source 1.8 -target 1.8)\n" +
        "    -1.9 -9 -9.0       use 1.9 compliance (-source 1.9 -target 1.9)\n" +
        "    -10 -10.0          use 10  compliance (-source 10  -target 10)\n" +
        "    -11 -11.0          use 11  compliance (-source 11  -target 11)\n" +
        "    -12 -12.0          use 12  compliance (-source 12  -target 12)\n" +
        "    -13 -13.0          use 13  compliance (-source 13  -target 13)\n" +
        "    -14 -14.0          use 14  compliance (-source 14  -target 14)\n" +
        getVersionOptions() +
        "    -source <version>  set source level: 1.3 to 1.9, 10 to "+ CompilerOptions.getLatestVersion() +"\n" +
        "                       (or 6, 6.0, etc)\n" +
        "    -target <version>  set classfile target: 1.3 to 1.9, 10 to "+ CompilerOptions.getLatestVersion() +"\n" +
        "                       (or 6, 6.0, etc)\n" +
        "                       cldc1.1 can also be used to generate the StackMap\n" +
        "                       attribute\n" +
        "    --enable-preview   enable support for preview features of the\n" +
        "                       latest Java release\n" +
        " \n" +
        " Warning options:\n" +
        "    -deprecation     + deprecation outside deprecated code (equivalent to\n" +
        "                       -warn:+deprecation)\n" +
        "    -nowarn -warn:none disable all warnings\n" +
        "    -nowarn:[<directories separated by " + File.pathSeparator+ ">]\n" +
        "                       specify directories from which optional problems should\n" +
        "                       be ignored\n" +
        "    -?:warn -help:warn display advanced warning options\n" +
        " \n" +
        " Error options:\n" +
        "    -err:<warnings separated by ,>    convert exactly the listed warnings\n" +
        "                                      to be reported as errors\n" +
        "    -err:+<warnings separated by ,>   enable additional warnings to be\n" +
        "                                      reported as errors\n" +
        "    -err:-<warnings separated by ,>   disable specific warnings to be\n" +
        "                                      reported as errors\n" +
        " \n" +
        " Info options:\n" +
        "    -info:<warnings separated by ,>   convert exactly the listed warnings\n" +
        "                                      to be reported as infos\n" +
        "    -info:+<warnings separated by ,>  enable additional warnings to be\n" +
        "                                      reported as infos\n" +
        "    -info:-<warnings separated by ,>  disable specific warnings to be\n" +
        "                                      reported as infos\n" +
        " \n" +
        " Setting warning, error or info options using properties file:\n" +
        "    -properties <file>   set warnings/errors/info option based on the properties\n" +
        "                         file contents. This option can be used with -nowarn,\n" +
        "                         -err:.., -info: or -warn:.. options, but the last one\n" +
        "                         on the command line sets the options to be used.\n" +
        " \n" +
        " Debug options:\n" +
        "    -g[:lines,vars,source] custom debug info\n" +
        "    -g:lines,source  + both lines table and source debug info\n" +
        "    -g                 all debug info\n" +
        "    -g:none            no debug info\n" +
        "    -preserveAllLocals preserve unused local vars for debug purpose\n" +
        " \n" +
        " Annotation processing options:\n" +
        "   These options are meaningful only in a 1.6 environment.\n" +
        "    -Akey[=value]        options that are passed to annotation processors\n" +
        "    -processorpath <directories and ZIP archives separated by " + File.pathSeparator + ">\n" +
        "                         specify locations where to find annotation processors.\n" +
        "                         If this option is not used, the classpath will be\n" +
        "                         searched for processors\n" +
        "    -processor <class1[,class2,...]>\n" +
        "                         qualified names of the annotation processors to run.\n" +
        "                         This bypasses the default annotation discovery process\n" +
        "    -proc:only           run annotation processors, but do not compile\n" +
        "    -proc:none           perform compilation but do not run annotation\n" +
        "                         processors\n" +
        "    -s <dir>             destination directory for generated source files\n" +
        "    -XprintProcessorInfo print information about which annotations and elements\n" +
        "                         a processor is asked to process\n" +
        "    -XprintRounds        print information about annotation processing rounds\n" +
        "    -classNames <className1[,className2,...]>\n" +
        "                         qualified names of binary classes to process\n" +
        " \n" +
        " Advanced options:\n" +
        "    @<file>            read command line arguments from file\n" +
        "    -maxProblems <n>   max number of problems per compilation unit (100 by\n" +
        "                       default)\n" +
        "    -log <file>        log to a file. If the file extension is ''.xml'', then\n" +
        "                       the log will be a xml file.\n" +
        "    -proceedOnError[:Fatal]\n" +
        "                       do not stop at first error, dumping class files with\n" +
        "                       problem methods\n" +
        "                       With \":Fatal\", all optional errors are treated as fatal\n" +
        "    -failOnWarning     fail compilation if there are warnings\n" +
        "    -verbose           enable verbose output\n" +
        "    -referenceInfo     compute reference info\n" +
        "    -progress          show progress (only in -log mode)\n" +
        "    -time              display speed information \n" +
        "    -noExit            do not call System.exit(n) at end of compilation (n==0\n" +
        "                       if no error)\n" +
        "    -repeat <n>        repeat compilation process <n> times for perf analysis\n" +
        "    -inlineJSR         inline JSR bytecode (implicit if target >= 1.5)\n" +
        "    -enableJavadoc     consider references in javadoc\n" +
        "    -parameters        generate method parameters attribute (for target >= 1.8)\n" +
        "    -genericsignature  generate generic signature for lambda expressions\n" +
        "    -Xemacs            used to enable emacs-style output in the console.\n" +
        "                       It does not affect the xml log output\n" +
        "    -missingNullDefault  report missing default nullness annotation\n" +
        "    -annotationpath <directories and ZIP archives separated by " + File.pathSeparator + ">\n" +
        "                       specify locations where to find external annotations\n" +
        "                       to support annotation-based null analysis.\n" +
        "                       The special name CLASSPATH will cause lookup of\n" +
        "                       external annotations from the classpath and sourcepath.\n" +
        " \n" +
        "    -? -help           print this help message\n" +
        "    -v -version        print compiler version\n" +
        "    -showversion       print compiler version and continue\n" +
        " \n" +
        " Ignored options:\n" +
        "    -J<option>         pass option to virtual machine (ignored)\n" +
        "    -X<option>         specify non-standard option (ignored\n" +
        "                       except for listed -X options)\n" +
        "    -X                 print non-standard options and exit (ignored)\n" +
        "    -O                 optimize for execution time (ignored)\n" +
        "\n";
	String expandedExpectedOutput =
		MessageFormat.format(expectedOutput, new Object[] {
				MAIN.bind("compiler.name"),
				MAIN.bind("compiler.version"),
				MAIN.bind("compiler.copyright")
		// because misc.version is mono-line - reconsider if this changes
//		MessageFormat.format(expectedOutput, new String[] {
//				Main.bind("misc.version", new String[] {
//					Main.bind("compiler.name"),
//					Main.bind("compiler.version"),
//					Main.bind("compiler.copyright")
//				}),
				// File.pathSeparator
			});
		this.runConformTest(
		new String[0],
        " -help -referenceInfo",
        expandedExpectedOutput,
        "", true);
	checkWidth(expandedExpectedOutput, 80);
}
//command line - help
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=144248
// Progressive help text modifies the help options and messages.
// amended for https://bugs.eclipse.org/bugs/show_bug.cgi?id=141512 (checking
// width)
public void test012b(){
	final String expectedOutput =
        "{0} {1}\n" +
        "{2}\n" +
        " \n" +
        " Warning options:\n" +
        "    -deprecation         + deprecation outside deprecated code\n" +
        "    -nowarn -warn:none disable all warnings and infos\n" +
        "    -nowarn:[<directories separated by " + File.pathSeparator+ ">]\n" +
        "                           specify directories from which optional problems\n" +
        "                           should be ignored\n" +
        "    -warn:<warnings separated by ,>   enable exactly the listed warnings\n" +
        "    -warn:+<warnings separated by ,>  enable additional warnings\n" +
        "    -warn:-<warnings separated by ,>  disable specific warnings\n" +
        "      all                  enable all warnings\n" +
        "      allDeadCode          dead code including trivial if(DEBUG) check\n" +
        "      allDeprecation       deprecation including inside deprecated code\n" +
        "      allJavadoc           invalid or missing javadoc\n" +
        "      allOver-ann          all missing @Override annotations\n" +
        "      all-static-method    all method can be declared as static warnings\n" +
        "      assertIdentifier   + ''assert'' used as identifier\n" +
        "      boxing               autoboxing conversion\n" +
        "      charConcat         + char[] in String concat\n" +
        "      compareIdentical   + comparing identical expressions\n" +
        "      conditionAssign      possible accidental boolean assignment\n" +
        "      constructorName    + method with constructor name\n" +
        "      deadCode           + dead code excluding trivial if (DEBUG) check\n" +
        "      dep-ann              missing @Deprecated annotation\n" +
        "      deprecation        + deprecation outside deprecated code\n" +
        "      discouraged        + use of types matching a discouraged access rule\n" +
        "      emptyBlock           undocumented empty block\n" +
        "      enumIdentifier       ''enum'' used as identifier\n" +
        "      enumSwitch           incomplete enum switch\n" +
        "      enumSwitchPedantic + report missing enum switch cases even\n" +
        "                           in the presence of a default case\n" +
        "      fallthrough          possible fall-through case\n" +
        "      fieldHiding          field hiding another variable\n" +
        "      finalBound           type parameter with final bound\n" +
        "      finally            + finally block not completing normally\n" +
        "      forbidden          + use of types matching a forbidden access rule\n" +
        "      hashCode             missing hashCode() method when overriding equals()\n" +
        "      hiding               macro for fieldHiding, localHiding, typeHiding and\n" +
        "                           maskedCatchBlock\n" +
        "      includeAssertNull    raise null warnings for variables\n" +
        "                           that got tainted in an assert expression\n" +
        "      indirectStatic       indirect reference to static member\n" +
        "      inheritNullAnnot     inherit null annotations\n" +
        "      intfAnnotation     + annotation type used as super interface\n" +
        "      intfNonInherited   + interface non-inherited method compatibility\n" +
        "      intfRedundant        find redundant superinterfaces\n" +
        "      invalidJavadoc       all warnings for malformed javadoc tags\n" +
        "      invalidJavadocTag    validate javadoc tag arguments\n" +
        "      invalidJavadocTagDep validate deprecated references in javadoc tag args\n" +
        "      invalidJavadocTagNotVisible\n" +
        "                           validate non-visible references in javadoc tag args\n" +
        "      invalidJavadocVisibility(<visibility>)\n" +
        "                           specify visibility modifier for malformed javadoc\n" +
        "                           tag warnings\n" +
        "      javadoc              invalid javadoc\n" +
        "      localHiding          local variable hiding another variable\n" +
        "      maskedCatchBlock   + hidden catch block\n" +
        "      missingJavadocTags   missing Javadoc tags\n" +
        "      missingJavadocTagsOverriding missing Javadoc tags in overriding methods\n" +
        "      missingJavadocTagsMethod missing Javadoc tags for method type parameter\n" +
        "      missingJavadocTagsVisibility(<visibility>)\n" +
        "                           specify visibility modifier for missing javadoc\n" +
        "                           tags warnings\n" +
        "      missingJavadocComments  missing Javadoc comments\n" +
        "      missingJavadocCommentsOverriding\n" +
        "                           missing Javadoc tags in overriding methods\n" +
        "      missingJavadocCommentsVisibility(<visibility>)  specify visibility\n" +
        "                           modifier for missing javadoc comments warnings\n" +
        "      module             + module related problems.\n" +
        "      nls                  string literal lacking non-nls tag //$NON-NLS-<n>$\n" +
        "      noEffectAssign     + assignment without effect\n" +
        "      null                 potential missing or redundant null check\n" +
        "      nullAnnot(<annot. names separated by |>)\n" +
        "                           annotation based null analysis,\n" +
        "                           nullable|nonnull|nonnullbydefault annotation types\n" +
        "                           optionally specified using fully qualified names.\n" +
        "                           Enabling this option enables all null-annotation\n" +
        "                           related sub-options. These can be individually\n" +
        "                           controlled using options listed below.\n" +
        "      nullAnnotConflict    conflict between null annotation specified\n" +
        "                           and nullness inferred. Is effective only with\n" +
        "                           nullAnnot option enabled.\n" +
        "      nullAnnotRedundant   redundant specification of null annotation. Is\n" +
        "                           effective only with nullAnnot option enabled.\n" +
        "      nullDereference    + missing null check\n" +
        "      nullUncheckedConversion  unchecked conversion from non-annotated type\n" +
        "                           to @NonNull type. Is effective only with\n" +
        "                           nullAnnot option enabled.\n" +
        "      over-ann             missing @Override annotation (superclass)\n" +
        "      paramAssign          assignment to a parameter\n" +
        "      pkgDefaultMethod   + attempt to override package-default method\n" +
        "      raw                + usage of raw type\n" +
        "      removal            + deprecation marked for removal\n" +
        "      resource           + (pot.) unsafe usage of resource of type Closeable\n" +
        "      semicolon            unnecessary semicolon, empty statement\n" +
        "      serial             + missing serialVersionUID\n" +
        "      specialParamHiding   constructor or setter parameter hiding a field\n" +
        "      static-method        method can be declared as static\n" +
        "      static-access        macro for indirectStatic and staticReceiver\n" +
        "      staticReceiver     + non-static reference to static member\n" +
        "      super                overriding a method without making a super invocation\n" +
        "      suppress           + enable @SuppressWarnings\n" +
        "                           When used with -err:, it can also silence optional\n" +
        "                           errors and warnings\n" +
        "      switchDefault        switch statement lacking a default case\n" +
        "      syncOverride         missing synchronized in synchr. method override\n" +
        "      syntacticAnalysis    perform syntax-based null analysis for fields\n" +
        "      syntheticAccess      synthetic access for innerclass\n" +
        "      tasks(<tags separated by |>)  tasks identified by tags inside comments\n" +
        "      typeHiding         + type parameter hiding another type\n" +
        "      unavoidableGenericProblems  + ignore unavoidable type safety problems\n" +
        "                           due to raw APIs\n" +
        "      unchecked          + unchecked type operation\n" +
        "      unlikelyCollectionMethodArgumentType\n" +
        "                         + unlikely argument type for collection method\n" +
        "                           declaring an Object parameter\n" +
        "      unlikelyEqualsArgumentType  unlikely argument type for method equals()\n" +
        "      unnecessaryElse      unnecessary else clause\n" +
        "      unqualifiedField     unqualified reference to field\n" +
        "      unused               macro for unusedAllocation, unusedArgument,\n" +
        "                           unusedImport, unusedLabel, unusedLocal,\n" +
        "                           unusedPrivate, unusedThrown, and unusedTypeArgs,\n" +
        "                           unusedExceptionParam\n" +
        "      unusedAllocation     allocating an object that is not used\n" +
        "      unusedArgument       unread method parameter\n" +
        "      unusedExceptionParam unread exception parameter\n" +
        "      unusedImport       + unused import declaration\n" +
        "      unusedLabel        + unused label\n" +
        "      unusedLocal        + unread local variable\n" +
        "      unusedParam          unused parameter\n" +
        "      unusedParamOverriding  unused parameter for overriding method\n" +
        "      unusedParamImplementing  unused parameter for implementing method\n" +
        "      unusedParamIncludeDoc  unused parameter documented in comment tag\n" +
        "      unusedPrivate      + unused private member declaration\n" +
        "      unusedThrown         unused declared thrown exception\n" +
        "      unusedThrownWhenOverriding  unused declared thrown exception in \n" +
        "                           overriding method\n" +
        "      unusedThrownIncludeDocComment  unused declared thrown exception,\n" +
        "                           documented in a comment tag\n" +
        "      unusedThrownExemptExceptionThrowable  unused declared thrown exception,\n" +
        "                           exempt Exception and Throwable\n" +
        "      unusedTypeArgs     + unused type arguments for method and constructor\n" +
        "      uselessTypeCheck     unnecessary cast/instanceof operation\n" +
        "      varargsCast        + varargs argument need explicit cast\n" +
        "      warningToken       + unsupported or unnecessary @SuppressWarnings\n" +
        "\n";
	String expandedExpectedOutput =
		MessageFormat.format(expectedOutput, new Object[] {
				MAIN.bind("compiler.name"),
				MAIN.bind("compiler.version"),
				MAIN.bind("compiler.copyright")
		// because misc.version is mono-line - reconsider if this changes
//		MessageFormat.format(expectedOutput, new String[] {
//				Main.bind("misc.version", new String[] {
//					Main.bind("compiler.name"),
//					Main.bind("compiler.version"),
//					Main.bind("compiler.copyright")
//				}),
				// File.pathSeparator
			});
	this.runConformTest(
		new String[0],
        " -help:warn -referenceInfo",
        expandedExpectedOutput,
        "", true);
	checkWidth(expandedExpectedOutput, 80);
}

	// command line - xml log contents https://bugs.eclipse.org/bugs/show_bug.cgi?id=93904
	public void test013() {
		String logFileName = OUTPUT_DIR + File.separator + "log.xml";
		this.runNegativeTest(new String[] {
				"X.java",
				"""
					/** */
					public class X {
						Zork z;
					}""", },
				"\"" + OUTPUT_DIR + File.separator + "X.java\""
				+ " -1.5 -proceedOnError"
				+ " -log \"" + logFileName + "\" -d \"" + OUTPUT_DIR + "\"",
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					1 problem (1 error)""",
				true);
		String logContents = Util.fileContent(logFileName);
		String expectedLogContents =
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
			"<!DOCTYPE compiler PUBLIC \"-//Eclipse.org//DTD Eclipse JDT 3.2.006 Compiler//EN\" \"https://www.eclipse.org/jdt/core/compiler_32_006.dtd\">\n" +
			"<compiler copyright=\"{2}\" name=\"{1}\" version=\"{3}\">\n" +
			"	<command_line>\n" +
			"		<argument value=\"---OUTPUT_DIR_PLACEHOLDER---{0}X.java\"/>\n" +
			"		<argument value=\"-1.5\"/>\n" +
			"		<argument value=\"-proceedOnError\"/>\n" +
			"		<argument value=\"-log\"/>\n" +
			"		<argument value=\"---OUTPUT_DIR_PLACEHOLDER---{0}log.xml\"/>\n" +
			"		<argument value=\"-d\"/>\n" +
			"		<argument value=\"---OUTPUT_DIR_PLACEHOLDER---\"/>\n" +
			"	</command_line>\n" +
			"	<options>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.annotation.inheritNullAnnotations\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.annotation.missingNonNullByDefaultAnnotation\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.annotation.nonnull\" value=\"org.eclipse.jdt.annotation.NonNull\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.annotation.nonnull.secondary\" value=\"\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.annotation.nonnullbydefault\" value=\"org.eclipse.jdt.annotation.NonNullByDefault\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.annotation.nonnullbydefault.secondary\" value=\"\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.annotation.notowning\" value=\"org.eclipse.jdt.annotation.NotOwning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.annotation.nullable\" value=\"org.eclipse.jdt.annotation.Nullable\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.annotation.nullable.secondary\" value=\"\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.annotation.nullanalysis\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.annotation.owning\" value=\"org.eclipse.jdt.annotation.Owning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.annotation.resourceanalysis\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.codegen.lambda.genericSignature\" value=\"do not generate\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.codegen.methodParameters\" value=\"do not generate\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.codegen.shareCommonFinallyBlocks\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.codegen.targetPlatform\" value=\"1.5\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.codegen.unusedLocal\" value=\"optimize out\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.codegen.useStringConcatFactory\" value=\"enabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.compliance\" value=\"1.5\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.debug.lineNumber\" value=\"generate\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.debug.localVariable\" value=\"do not generate\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.debug.sourceFile\" value=\"generate\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.doc.comment.support\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.emulateJavacBug8031744\" value=\"enabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.generateClassFiles\" value=\"enabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.ignoreUnnamedModuleForSplitPackage\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.maxProblemPerUnit\" value=\"100\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.APILeak\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.annotatedTypeArgumentToUnannotated\" value=\"info\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.annotationSuperInterface\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.assertIdentifier\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.autoboxing\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.comparingIdentical\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.deadCode\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.deadCodeInTrivialIfStatement\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.deprecation\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.deprecationInDeprecatedCode\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.deprecationWhenOverridingDeprecatedMethod\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.discouragedReference\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.emptyStatement\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.enablePreviewFeatures\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.enumIdentifier\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.explicitlyClosedAutoCloseable\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.fallthroughCase\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.fatalOptionalError\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.fieldHiding\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.finalParameterBound\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.finallyBlockNotCompletingNormally\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.forbiddenReference\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.hiddenCatchBlock\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.includeNullInfoFromAsserts\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.incompatibleNonInheritedInterfaceMethod\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.incompatibleOwningContract\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.incompleteEnumSwitch\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.indirectStaticAccess\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.insufficientResourceAnalysis\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadoc\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadocTags\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsDeprecatedRef\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsNotVisibleRef\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsVisibility\" value=\"public\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.localVariableHiding\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.methodWithConstructorName\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingDefaultCase\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingDeprecatedAnnotation\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingEnumCaseDespiteDefault\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingHashCodeMethod\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocComments\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocCommentsOverriding\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocCommentsVisibility\" value=\"public\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocTagDescription\" value=\"return_tag\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocTags\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocTagsMethodTypeParameters\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocTagsOverriding\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocTagsVisibility\" value=\"public\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingOverrideAnnotation\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingOverrideAnnotationForInterfaceMethodImplementation\" value=\"enabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingSerialVersion\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingSynchronizedOnInheritedMethod\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.noEffectAssignment\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.noImplicitStringConversion\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.nonnullParameterAnnotationDropped\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.nonnullTypeVariableFromLegacyInvocation\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.nullAnnotationInferenceConflict\" value=\"error\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.nullReference\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.nullSpecViolation\" value=\"error\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.nullUncheckedConversion\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.overridingMethodWithoutSuperInvocation\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.overridingPackageDefaultMethod\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.parameterAssignment\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.pessimisticNullAnalysisForFreeTypeVariables\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.possibleAccidentalBooleanAssignment\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.potentialNullReference\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.potentiallyUnclosedCloseable\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.rawTypeReference\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.redundantNullAnnotation\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.redundantNullCheck\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.redundantSpecificationOfTypeArguments\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.redundantSuperinterface\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.reportMethodCanBePotentiallyStatic\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.reportMethodCanBeStatic\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.reportPreviewFeatures\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.specialParameterHidingField\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.staticAccessReceiver\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.suppressOptionalErrors\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.suppressWarnings\" value=\"enabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.suppressWarningsNotFullyAnalysed\" value=\"info\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.syntacticNullAnalysisForFields\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.syntheticAccessEmulation\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.tasks\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.terminalDeprecation\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.typeParameterHiding\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unavoidableGenericTypeProblems\" value=\"enabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.uncheckedTypeOperation\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unclosedCloseable\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.undocumentedEmptyBlock\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unhandledWarningToken\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.uninternedIdentityComparison\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unlikelyCollectionMethodArgumentType\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unlikelyCollectionMethodArgumentTypeStrict\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unlikelyEqualsArgumentType\" value=\"info\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unnecessaryElse\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unnecessaryTypeCheck\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unqualifiedFieldAccess\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unstableAutoModuleName\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownException\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionExemptExceptionAndThrowable\" value=\"enabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionIncludeDocCommentReference\" value=\"enabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionWhenOverriding\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedExceptionParameter\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedImport\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedLabel\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedLocal\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedObjectAllocation\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedParameter\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedParameterIncludeDocCommentReference\" value=\"enabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedParameterWhenImplementingAbstract\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedParameterWhenOverridingConcrete\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedPrivateMember\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedTypeArgumentsForMethodInvocation\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedTypeParameter\" value=\"ignore\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedWarningToken\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.varargsArgumentNeedCast\" value=\"warning\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.processAnnotations\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.release\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.source\" value=\"1.5\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.storeAnnotations\" value=\"disabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.taskCaseSensitive\" value=\"enabled\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.taskPriorities\" value=\"\"/>\n" +
			"		<option key=\"org.eclipse.jdt.core.compiler.taskTags\" value=\"\"/>\n" +
			"	</options>\n" +
			"	<classpaths>NORMALIZED SECTION</classpaths>\n" +
			"	<sources>\n" +
			"		<source output=\"---OUTPUT_DIR_PLACEHOLDER---\" path=\"---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\">\n" +
			"			<problems errors=\"1\" infos=\"0\" problems=\"1\" warnings=\"0\">\n" +
			"				<problem categoryID=\"40\" charEnd=\"28\" charStart=\"25\" id=\"UndefinedType\" line=\"3\" problemID=\"16777218\" severity=\"ERROR\">\n" +
			"					<message value=\"Zork cannot be resolved to a type\"/>\n" +
			"					<source_context sourceEnd=\"3\" sourceStart=\"0\" value=\"Zork z;\"/>\n" +
			"					<arguments>\n" +
			"						<argument value=\"Zork\"/>\n" +
			"					</arguments>\n" +
			"				</problem>\n" +
			"			</problems>\n" +
			"			<classfile path=\"---OUTPUT_DIR_PLACEHOLDER---{0}X.class\"/>\n" +
			"		</source>\n" +
			"	</sources>\n" +
			"	<stats>\n" +
			"		<problem_summary errors=\"1\" infos=\"0\" problems=\"1\" tasks=\"0\" warnings=\"0\"/>\n" +
			"	</stats>\n" +
			"</compiler>\n";
		String normalizedExpectedLogContents =
				MessageFormat.format(
						expectedLogContents,
						new Object[] {
								File.separator,
								MAIN.bind("compiler.name"),
								MAIN.bind("compiler.copyright"),
								MAIN.bind("compiler.version")
						});
		String normalizedLogContents =
				xmlLogsNormalizer.normalized(logContents);
		boolean compareOK = normalizedExpectedLogContents.equals(
				normalizedLogContents);
		if (!compareOK) {
			System.out.println(getClass().getName() + '#' + getName());
			System.out.println(
					  "------------ [START LOG] ------------\n"
					+ "------------- Expected: -------------\n"
					+ expectedLogContents
				  + "\n------------- but was:  -------------\n"
					+ xmlLogsNormalizer.normalized(logContents)
				  + "\n--------- (cut and paste:) ----------\n"
					+ Util.displayString(xmlLogsNormalizer.normalized(logContents))
				  + "\n------------- [END LOG] -------------\n");
			assertEquals("Unexpected log contents",
					normalizedExpectedLogContents, normalizedLogContents);
		}
	}

	// command line - txt log contents https://bugs.eclipse.org/bugs/show_bug.cgi?id=93904
	public void test014() {
		String logFileName = OUTPUT_DIR + File.separator + "log.txt";
		this.runNegativeTest(new String[] {
				"X.java",
				"""
					/** */
					public class X {
						Zork z;
					}""", },
				"\"" + OUTPUT_DIR + File.separator + "X.java\""
				+ " -1.5 -proceedOnError"
				+ " -log \"" + logFileName + "\" -d \"" + OUTPUT_DIR + "\"",
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					1 problem (1 error)
					""",
				false);
		String logContents = Util.fileContent(logFileName);
		String expectedLogContents =
			"----------\n" +
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java (at line 3)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"1 problem (1 error)\n";
		boolean compareOK = semiNormalizedComparison(expectedLogContents,
				logContents, textLogsNormalizer);
		if (!compareOK) {
			System.out.println(getClass().getName() + '#' + getName());
			System.out.println(
							  "------------ [START LOG] ------------\n"
							+ "------------- Expected: -------------\n"
							+ expectedLogContents
						  + "\n------------- but was:  -------------\n"
							+ outputDirNormalizer.normalized(logContents)
						  + "\n--------- (cut and paste:) ----------\n"
							+ Util.displayString(outputDirNormalizer.normalized(logContents))
						  + "\n------------- [END LOG] -------------\n");
		}
		assertTrue("unexpected log contents", compareOK);
	}

	// command line - no extension log contents https://bugs.eclipse.org/bugs/show_bug.cgi?id=93904
	public void test015() {
		String logFileName = OUTPUT_DIR + File.separator + "log";
		this.runNegativeTest(new String[] {
				"X.java",
				"""
					/** */
					public class X {
						Zork z;
					}""", },
				"\"" + OUTPUT_DIR + File.separator + "X.java\""
				+ " -1.5 -proceedOnError"
				+ " -log \"" + logFileName + "\" -d \"" + OUTPUT_DIR + "\"",
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
						Zork z;
						^^^^
					Zork cannot be resolved to a type
					----------
					1 problem (1 error)
					""",
				false);
		String logContents = Util.fileContent(logFileName);
		String expectedLogContents =
			"----------\n" +
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java (at line 3)\n" +
			"	Zork z;\n" +
			"	^^^^\n" +
			"Zork cannot be resolved to a type\n" +
			"----------\n" +
			"1 problem (1 error)\n";
		boolean compareOK = semiNormalizedComparison(expectedLogContents,
				logContents, textLogsNormalizer);
		if (!compareOK) {
			System.out.println(getClass().getName() + '#' + getName());
			System.out.println(
					  "------------ [START LOG] ------------\n"
					+ "------------- Expected: -------------\n"
					+ expectedLogContents
				  + "\n------------- but was:  -------------\n"
					+ outputDirNormalizer.normalized(logContents)
				  + "\n--------- (cut and paste:) ----------\n"
					+ Util.displayString(outputDirNormalizer.normalized(logContents))
				  + "\n------------- [END LOG] -------------\n");
		}
		assertTrue("unexpected log contents", compareOK);
	}
// command line - several path separators within the classpath
public void test016(){
	String setting = System.getProperty("jdt.compiler.useSingleThread");
	try {
		System.setProperty("jdt.compiler.useSingleThread", "true");
		this.runConformTest(
			new String[] {
					"X.java",
					"""
						/** */
						public class X {
							OK1 ok1;
						}""",
					"OK1.java",
					"""
						/** */
						public class OK1 {
							// empty
						}"""
			},
	        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -cp ." + File.pathSeparator + File.pathSeparator + File.pathSeparator + "\"" + OUTPUT_DIR + "\""
	        + " -verbose -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\"",
			"""
				[parsing    ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]
				[reading    java/lang/Object.class]
				[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]
				[parsing    ---OUTPUT_DIR_PLACEHOLDER---/OK1.java - #2/2]
				[writing    X.class - #1]
				[completed  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/2]
				[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/OK1.java - #2/2]
				[writing    OK1.class - #2]
				[completed  ---OUTPUT_DIR_PLACEHOLDER---/OK1.java - #2/2]
				[2 units compiled]
				[2 .class files generated]
				""",
	        "",
	        true);
	} finally {
		System.setProperty("jdt.compiler.useSingleThread", setting == null ? "false" : setting);
	}
}
public void test017(){
		this.runConformTest(
			new String[] {
					"X.java",
					"""
						/** */
						public class X {
							OK1 ok1;
						}""",
					"OK1.java",
					"""
						/** */
						public class OK1 {
							// empty
						}"""
			},
	        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -cp dummmy_dir" + File.pathSeparator + "dummy.jar" + File.pathSeparator + File.pathSeparator + "\"" + OUTPUT_DIR + "\""
	        + " -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\"",
	        "",
	        "incorrect classpath: dummmy_dir\n",
	        true);
	}
// we tolerate inexisting jars on the classpath, and we don't even warn about
// them (javac does the same as us)
public void test017b(){
	this.runTest(
		true,
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
					OK1 ok1;
				}""",
			"OK1.java",
			"""
				/** */
				public class OK1 {
					// empty
				}"""
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp dummy.jar" + File.pathSeparator + File.pathSeparator + "\"" + OUTPUT_DIR + "\""
        + " -verbose -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "\"",
        TWO_FILES_GENERATED_MATCHER,
        EMPTY_STRING_MATCHER,
        true);
}
// we tolerate empty classpath entries, and we don't even warn about
// them (javac does the same as us)
public void test017c(){
	this.runTest(
		true,
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
					OK1 ok1;
				}""",
			"OK1.java",
			"""
				/** */
				public class OK1 {
					// empty
				}"""
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp " + File.pathSeparator + File.pathSeparator + "\"" + OUTPUT_DIR + "\""
        + " -verbose -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "\"",
        TWO_FILES_GENERATED_MATCHER,
        EMPTY_STRING_MATCHER,
        true);
}
// command line - unusual classpath (empty)
// ok provided we explicit the sourcepath
public void test018a(){
	String currentWorkingDirectoryPath = System.getProperty("user.dir");
	if (currentWorkingDirectoryPath == null) {
		System.err.println("BatchCompilerTest#18a could not access the current working directory " + currentWorkingDirectoryPath);
	} else if (!new File(currentWorkingDirectoryPath).isDirectory()) {
		System.err.println("BatchCompilerTest#18a current working directory is not a directory " + currentWorkingDirectoryPath);
	} else {
		String xPath = currentWorkingDirectoryPath + File.separator + "X.java";
		String ok1Path = currentWorkingDirectoryPath + File.separator + "OK1.java";
		PrintWriter sourceFileWriter;
		try {
			File file = new File(xPath);
			sourceFileWriter = new PrintWriter(new FileOutputStream(file));
			try {
				sourceFileWriter.write(
					"""
						/** */
						public class X {
							OK1 ok1;
						}""");
			} finally {
				sourceFileWriter.close();
			}
			file = new File(ok1Path);
			sourceFileWriter = new PrintWriter(new FileOutputStream(file));
			try {
				sourceFileWriter.write(
					"""
						/** */
						public class OK1 {
							// empty
						}""");
			} finally {
				sourceFileWriter.close();
			}
			this.runTest(
				true,
				new String[] {
					"dummy.java", // enforce output directory creation
					""
				},
		        "X.java"
		        + " -1.5 -g -preserveAllLocals"
		        + " -verbose -proceedOnError"
		        + " -sourcepath ."
		        + " -d \"" + OUTPUT_DIR + "\"",
		        TWO_FILES_GENERATED_MATCHER,
		        EMPTY_STRING_MATCHER,
		        false);
		} catch (FileNotFoundException e) {
			System.err.println("BatchCompilerTest#18a could not write to current working directory " + currentWorkingDirectoryPath);
		} finally {
			new File(xPath).delete();
			new File(ok1Path).delete();
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=214725
// empty sourcepath works with javac but not with ecj
public void _test018b(){
	String currentWorkingDirectoryPath = System.getProperty("user.dir");
	if (currentWorkingDirectoryPath == null) {
		System.err.println("BatchCompilerTest#18b could not access the current working directory " + currentWorkingDirectoryPath);
	} else if (!new File(currentWorkingDirectoryPath).isDirectory()) {
		System.err.println("BatchCompilerTest#18b current working directory is not a directory " + currentWorkingDirectoryPath);
	} else {
		String xPath = currentWorkingDirectoryPath + File.separator + "X.java";
		String ok1Path = currentWorkingDirectoryPath + File.separator + "OK1.java";
		PrintWriter sourceFileWriter;
		try {
			File file = new File(xPath);
			sourceFileWriter = new PrintWriter(new FileOutputStream(file));
			sourceFileWriter.write(
				"""
					/** */
					public class X {
						OK1 ok1;
					}""");
			sourceFileWriter.close();
			file = new File(ok1Path);
			sourceFileWriter = new PrintWriter(new FileOutputStream(file));
			sourceFileWriter.write(
				"""
					/** */
					public class OK1 {
						// empty
					}""");
			sourceFileWriter.close();
			this.runTest(
				true,
				new String[] {
					"dummy.java", // enforce output directory creation
					""
				},
		        "X.java"
		        + " -1.5 -g -preserveAllLocals"
		        + " -verbose -proceedOnError"
		        + " -d \"" + OUTPUT_DIR + "\"",
		        TWO_FILES_GENERATED_MATCHER,
		        EMPTY_STRING_MATCHER,
		        false);
		} catch (FileNotFoundException e) {
			System.err.println("BatchCompilerTest#18b could not write to current working directory " + currentWorkingDirectoryPath);
		} finally {
			new File(xPath).delete();
			new File(ok1Path).delete();
		}
	}
}
public void test019(){
		this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					/** */
					public class X {
						OK1 ok1;
						OK2 ok2;
						Warn warn;
						KO ko;
						Zork z;
					}""",
				"OK1.java",
				"""
					/** */
					public class OK1 {
						// empty
					}""",
				"OK2.java",
				"""
					/** */
					public class OK2 {
						// empty
					}""",
				"Warn.java",
				"""
					/** */
					public class Warn {
						// empty
					}""",
				"KO.java",
				"""
					/** */
					public class KO {
						// empty
					}""",
			},
	        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -cp \"" + OUTPUT_DIR + "[+OK2" + File.pathSeparator + "~Warn" + File.pathSeparator + "-KO]\""
	        + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
	        + " -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\"",
	        "",
			"""
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
					Warn warn;
					^^^^
				Discouraged access: The type \'Warn\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---\')
				----------
				2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
					KO ko;
					^^
				Access restriction: The type \'KO\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---\')
				----------
				3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				3 problems (1 error, 2 warnings)
				""",
	        true);
	}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - skip options -O -Jxxx and -Xxxx, multiple times if needed
	public void test020(){
		this.runConformTest(
			new String[] {
					"X.java",
					"""
						/** */
						public class X {
						}""",
			},
	        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -verbose -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\" -O -Xxxx -O -Jxyz -Xtyu -Jyu",
			"""
				[parsing    ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]
				[reading    java/lang/Object.class]
				[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]
				[writing    X.class - #1]
				[completed  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]
				[1 unit compiled]
				[1 .class file generated]
				""",
	        "",
	        true);
	}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - -sourcepath finds additional source files
	public void test021(){
		String setting= System.getProperty("jdt.compiler.useSingleThread");
		try {
			System.setProperty("jdt.compiler.useSingleThread", "true");
			this.runConformTest(
				new String[] {
						"src1/X.java",
						"""
							/** */
							public class X {
							}""",
						"src2/Y.java",
						"""
							/** */
							public class Y extends X {
							}""",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src1\""
				  + File.pathSeparator + "\"" + OUTPUT_DIR +  File.separator + "src2\""
		        + " -1.5 -g -preserveAllLocals"
		        + " -verbose -proceedOnError -referenceInfo"
		        + " -d \"" + OUTPUT_DIR + "\" ",
				"""
					[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/1]
					[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]
					[reading    java/lang/Object.class]
					[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/2]
					[writing    Y.class - #1]
					[completed  ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/2]
					[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]
					[writing    X.class - #2]
					[completed  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]
					[2 units compiled]
					[2 .class files generated]
					""",
		        "",
		        true);
		} finally {
			System.setProperty("jdt.compiler.useSingleThread", setting == null ? "false" : setting);
		}
	}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - repeated -sourcepath fails - even if the error is more
// explicit here than what javac does
	public void test022_repeated_sourcepath(){
		this.runNegativeTest(
			new String[] {
					"src1/X.java",
					"""
						/** */
						public class X {
						}""",
					"src2/Y.java",
					"""
						/** */
						public class Y extends X {
						}""",
			},
			" -sourcepath \"" + OUTPUT_DIR +  File.separator + "src1\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src2\""
	        + " \"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -verbose -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\" ",
	        "",
	        "duplicate sourcepath specification: -sourcepath ---OUTPUT_DIR_PLACEHOLDER---/src2\n",
	        true);
	}
//	 https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - repeated -extdirs fails
	public void test023(){
		this.runNegativeTest(
			new String[] {
					"src1/X.java",
					"""
						/** */
						public class X {
						}""",
					"src2/Y.java",
					"""
						/** */
						public class Y extends X {
						}""",
			},
			" -extdirs \"" + OUTPUT_DIR +  File.separator + "src1\""
			+ " -extdirs \"" + OUTPUT_DIR +  File.separator + "src2\""
	        + " \"" + OUTPUT_DIR +  File.separator + "src1" + File.separator + "X.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -verbose -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\" ",
	        "",
	        "duplicate extdirs specification: -extdirs ---OUTPUT_DIR_PLACEHOLDER---/src2\n",
	        true);
	}
//	 https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - explicit empty -extdirs removes extensions
	public void test024(){
		if (!System.getProperty("java.vm.vendor").equals("Sun Microsystems Inc.")) return;
		/* this tests is using Sun vm layout. The type sun.net.spi.nameservice.dns.DNSNameService
		 * is located in the ext dir.
		 */
		this.runNegativeTest(
				new String[] {
						"X.java",
						"""
							/** */
							public class X {
							  sun.net.spi.nameservice.dns.DNSNameService dummy;
							}""",
				},
				"\"" + OUTPUT_DIR +  File.separator + "X.java\""
				+ " -extdirs \"\""
				+ " -1.5 -g -preserveAllLocals"
				+ " -proceedOnError -referenceInfo"
				+ " -d \"" + OUTPUT_DIR + "\" ",
				"",
				"""
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
						sun.net.spi.nameservice.dns.DNSNameService dummy;
						^^^^^^^^^^^^^^^^^^^^^^^^^^^
					sun.net.spi.nameservice.dns cannot be resolved to a type
					----------
					1 problem (1 error)
					""",
				true);
	}
//	 https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - cumulative -extdirs extends the classpath
	public void test025() throws Exception {
		String path = LIB_DIR;
		String libPath = null;
		if (path.endsWith(File.separator)) {
			libPath = path + "lib.jar";
		} else {
			libPath = path + File.separator + "lib.jar";
		}
		String setting= System.getProperty("jdt.compiler.useSingleThread");
		try {
			Util.createJar(new String[] {
					"my/pkg/Zork.java",
					"""
						package my.pkg;
						public class Zork {
						}""",
				},
				libPath,
				JavaCore.VERSION_1_4);
			System.setProperty("jdt.compiler.useSingleThread", "true");
			this.runConformTest(
				new String[] {
						"src1/X.java",
						"""
							/** */
							public class X {
							  my.pkg.Zork dummy;
							}""",
						"src2/Y.java",
						"""
							/** */
							public class Y extends X {
							}""",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
				+ " -extdirs \"" + path + File.pathSeparator + OUTPUT_DIR +  File.separator + "src1\""
				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src1\""
		        + " -1.5 -g -preserveAllLocals"
		        + " -verbose -proceedOnError -referenceInfo"
		        + " -d \"" + OUTPUT_DIR + "\" ",
		        """
					[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/1]
					[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]
					[reading    java/lang/Object.class]
					[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/2]
					[writing    Y.class - #1]
					[completed  ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/2]
					[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]
					[reading    my/pkg/Zork.class]
					[writing    X.class - #2]
					[completed  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]
					[2 units compiled]
					[2 .class files generated]
					""",
		        "",
		        true);
		} finally {
			System.setProperty("jdt.compiler.useSingleThread", setting == null ? "false" : setting);
			Util.delete(libPath);
		}
	}
//	 https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - -extdirs extends the classpath before -classpath
	public void test026(){
		String setting= System.getProperty("jdt.compiler.useSingleThread");
		try {
			System.setProperty("jdt.compiler.useSingleThread", "true");
			this.runConformTest(
				new String[] {
						"src1/X.java",
						"""
							/** */
							public class X {
							}""",
						"src2/Y.java",
						"""
							/** */
							public class Y extends X {
							}""",
						"src3/X.java",
						"""
							/** */
							public class X {
							  Zork error;
							}""",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
				+ " -classpath \"" + OUTPUT_DIR +  File.separator + "src3\""
				+ " -extdirs \"" + getExtDirectory() + File.pathSeparator + OUTPUT_DIR +  File.separator + "src1\""
				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src2" + File.pathSeparator + OUTPUT_DIR +  File.separator + "src1\""
		        + " -1.5 -g -preserveAllLocals"
		        + " -verbose -proceedOnError -referenceInfo"
		        + " -d \"" + OUTPUT_DIR + "\" ",
				"""
					[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/1]
					[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]
					[reading    java/lang/Object.class]
					[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/2]
					[writing    Y.class - #1]
					[completed  ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/2]
					[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]
					[writing    X.class - #2]
					[completed  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]
					[2 units compiled]
					[2 .class files generated]
					""",
				"",
		        true);
		} finally {
			System.setProperty("jdt.compiler.useSingleThread", setting == null ? "false" : setting);
		}
	}

public void test027(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
					OK1 ok1;
					OK2 ok2;
					Warn warn;
					KO ko;
					Zork z;
				}""",
			"OK1.java",
			"""
				/** */
				public class OK1 {
					// empty
				}""",
			"OK2.java",
			"""
				/** */
				public class OK2 {
					// empty
				}""",
			"p1/Warn.java",
			"""
				/** */
				public class Warn {
					// empty
				}""",
			"KO.java",
			"""
				/** */
				public class KO {
					// empty
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[+OK2" + File.pathSeparator + "-KO]" + File.pathSeparator
        + OUTPUT_DIR + File.separator + "p1[~Warn]\""
        + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        // TODO (maxime) reintroduce the -verbose option to check the number of files
        //               generated, once able to avoid console echoing
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        """
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				Warn warn;
				^^^^
			Discouraged access: The type \'Warn\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---/p1\')
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
				KO ko;
				^^
			Access restriction: The type \'KO\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---\')
			----------
			3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			3 problems (1 error, 2 warnings)
			""",
        true);
}
public void test028(){
			this.runConformTest(
				new String[] {
					"src1/X.java",
					"""
						/** */
						public class X {
						}""",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "src1/X.java\""
		        + " -1.5 -g -preserveAllLocals"
		        + " -proceedOnError -referenceInfo"
		        + " -d \"" + OUTPUT_DIR + File.separator + "bin/\"",
		        "",
		        "",
		        true);
			this.runConformTest(
				new String[] {
					"src2/Y.java",
					"""
						/** */
						public class Y extends X {
						}""",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
		        + " -1.5 -g -preserveAllLocals"
		        + " -cp dummy" + File.pathSeparator + "\"" + OUTPUT_DIR + File.separator + "bin\"" + File.pathSeparator + "dummy"
		        + " -proceedOnError -referenceInfo"
		        + " -d \"" + OUTPUT_DIR + File.separator + "bin/\"",
		        "",
		        "incorrect classpath: dummy\n" +
		        "incorrect classpath: dummy\n",
		        false);
		}
//Extraneous auto-build error message - https://bugs.eclipse.org/bugs/show_bug.cgi?id=93377
public void test030(){
	// first series shows that a clean build is OK
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public interface X<T extends X<T, K, S>,\s
				                   K extends X.K<T, S>,\s
				                   S extends X.S> {
					public interface K<KT extends X<KT, ?, KS>,\s
					                   KS extends X.S> {
					}
					public interface S {
					}
				}
				""",
			"Y.java",
			"""
				public class Y<T extends X<T, K, S>,\s
				               K extends X.K<T, S>,\s
				               S extends X.S> {\s
				}
				""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + File.separator + "\""
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	// second series shows that a staged build - that simulates the auto build context - is OK as well
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public interface X<T extends X<T, K, S>,\s
				                   K extends X.K<T, S>,\s
				                   S extends X.S> {
					public interface K<KT extends X<KT, ?, KS>,\s
					                   KS extends X.S> {
					}
					public interface S {
					}
				}
				""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				public class Y<T extends X<T, K, S>,\s
				               K extends X.K<T, S>,\s
				               S extends X.S> {\s
				}
				""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + File.separator + "\""
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        false);
}
// Extraneous auto-build error message - https://bugs.eclipse.org/bugs/show_bug.cgi?id=93377
// More complex test case than test30
public void test032(){
	// first series shows that a clean build is OK (warning messages only)
	this.runConformTest(
			new String[] {
				"p/X.java",
				"""
					package p;
					import java.io.Serializable;
					public interface X<T extends X<T, U, V>,\s
									   U extends X.XX<T, V>,\s
									   V extends X.XY> {
						public interface XX<TT extends X<TT, ?, UU>,\s
						                    UU extends X.XY>\s
								extends	Serializable {
						}
						public interface XY extends Serializable {
						}
					}
					""",
				"p/Y.java",
				"""
					package p;
					import java.util.*;
					import p.X.*;
					public class Y<T extends X<T, U, V>,\s
					               U extends X.XX<T, V>,\s
					               V extends X.XY> {
						private final Map<U, V> m1 = new HashMap<U, V>();
						private final Map<U, T> m2 = new HashMap<U, T>();
						private final Z m3;
					
						public Y(final Z p1) {
							this.m3 = p1;
						}
					
						public void foo1(final U p1, final V p2, final T p3) {
							m1.put(p1, p2);
							m2.put(p1, p3);
							m3.foo2(p1, p2);
						}
					
						public void foo3(final U p1) {
							assert m1.containsKey(p1);
							m1.remove(p1);
							m2.remove(p1);
							m3.foo2(p1, null);
						}
					
						public Collection<T> foo4() {
							return Collections.unmodifiableCollection(m2.values());
						}
					
						public void foo5(final Map<XX<?, ?>, XY> p1) {
							p1.putAll(m1);
						}
						public void foo6(final Map<XX<?, ?>, XY> p1) {
							m1.keySet().retainAll(p1.keySet());
							m2.keySet().retainAll(p1.keySet());
						}
					}
					""",
				"p/Z.java",
				"""
					package p;
					
					import java.util.*;
					
					import p.X.*;
					
					public class Z {
						private final Map<Class<? extends X>,\s
							              Y<?, ? extends XX<?, ?>, ? extends XY>>\s
							m1 = new HashMap<Class<? extends X>,\s
							                 Y<?, ? extends XX<?, ?>, ? extends XY>>();
					
						private Map<X.XX<?, XY>,\s
						            X.XY>\s
							m2 = new HashMap<X.XX<?, XY>,\s
							                 X.XY>();
					
						public <T extends X<T, U, V>,\s
						        U extends X.XX<T, V>,\s
						        V extends X.XY>\s
						Y<T, U, V> foo1(final Class<T> p1) {
							Y l1 = m1.get(p1);
							if (l1 == null) {
								l1 = new Y<T, U, V>(this);
								m1.put(p1, l1);
							}
							return l1;
						}
					
						public <TT extends X.XX<?, UU>,\s
						        UU extends X.XY>\s
						void foo2(final TT p1, final UU p2) {
							m2.put((XX<?, XY>) p1, p2);
						}
					
						public Map<XX<?, ?>, XY> foo3() {
							final Map<XX<?, ?>,\s
							          XY> l1 = new HashMap<XX<?, ?>,\s
							                               XY>();
							for (final Y<?,\s
									     ? extends XX<?, ?>,\s
									     ? extends XY>\s
									i : m1.values()) {
								i.foo5(l1);
							}
							return l1;
						}
					
						public void foo4(final Object p1, final Map<XX<?, ?>,\s
								                                    XY> p2) {
							for (final Y<?,\s
									     ? extends XX<?, ?>,\s
									     ? extends XY> i : m1.values()) {
								i.foo6(p2);
							}
							for (final Map.Entry<XX<?, ?>,\s
									             XY> i : p2.entrySet()) {
								final XX<?, XY> l1 = (XX<?, XY>) i.getKey();
							}
						}
					}
					"""
			},
	        "\"" + OUTPUT_DIR +  File.separator + "p/X.java\""
	        + " \"" + OUTPUT_DIR +  File.separator + "p/Y.java\""
	        + " \"" + OUTPUT_DIR +  File.separator + "p/Z.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -cp \"" + OUTPUT_DIR + File.separator + "\""
	        + " -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\"",
	        "",
	        """
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 8)
					private final Map<Class<? extends X>,\s
					                                  ^
				X is a raw type. References to generic type X<T,U,V> should be parameterized
				----------
				2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 10)
					m1 = new HashMap<Class<? extends X>,\s
					                                 ^
				X is a raw type. References to generic type X<T,U,V> should be parameterized
				----------
				3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 22)
					Y l1 = m1.get(p1);
					^
				Y is a raw type. References to generic type Y<T,U,V> should be parameterized
				----------
				4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 25)
					m1.put(p1, l1);
					           ^^
				Type safety: The expression of type Y needs unchecked conversion to conform to Y<?,? extends X.XX<?,?>,? extends X.XY>
				----------
				5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 27)
					return l1;
					       ^^
				Type safety: The expression of type Y needs unchecked conversion to conform to Y<T,U,V>
				----------
				6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 33)
					m2.put((XX<?, XY>) p1, p2);
					       ^^^^^^^^^^^^^^
				Type safety: Unchecked cast from TT to X.XX<?,X.XY>
				----------
				7. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 58)
					final XX<?, XY> l1 = (XX<?, XY>) i.getKey();
					                ^^
				The value of the local variable l1 is not used
				----------
				8. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 58)
					final XX<?, XY> l1 = (XX<?, XY>) i.getKey();
					                     ^^^^^^^^^^^^^^^^^^^^^^
				Type safety: Unchecked cast from X.XX<capture#22-of ?,capture#23-of ?> to X.XX<?,X.XY>
				----------
				8 problems (8 warnings)
				""",
	        true);
	// second series shows that a staged build - that simulates the auto build context - is OK as well
	this.runConformTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				import java.io.Serializable;
				public interface X<T extends X<T, U, V>,\s
								   U extends X.XX<T, V>,\s
								   V extends X.XY> {
					public interface XX<TT extends X<TT, ?, UU>,\s
					                    UU extends X.XY>\s
							extends	Serializable {
					}
					public interface XY extends Serializable {
					}
				}
				""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "p/X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"p/Y.java",
			"""
				package p;
				import java.util.*;
				import p.X.*;
				public class Y<T extends X<T, U, V>,\s
				               U extends X.XX<T, V>,\s
				               V extends X.XY> {
					private final Map<U, V> m1 = new HashMap<U, V>();
					private final Map<U, T> m2 = new HashMap<U, T>();
					private final Z m3;
				
					public Y(final Z p1) {
						this.m3 = p1;
					}
				
					public void foo1(final U p1, final V p2, final T p3) {
						m1.put(p1, p2);
						m2.put(p1, p3);
						m3.foo2(p1, p2);
					}
				
					public void foo3(final U p1) {
						assert m1.containsKey(p1);
						m1.remove(p1);
						m2.remove(p1);
						m3.foo2(p1, null);
					}
				
					public Collection<T> foo4() {
						return Collections.unmodifiableCollection(m2.values());
					}
				
					public void foo5(final Map<XX<?, ?>, XY> p1) {
						p1.putAll(m1);
					}
					public void foo6(final Map<XX<?, ?>, XY> p1) {
						m1.keySet().retainAll(p1.keySet());
						m2.keySet().retainAll(p1.keySet());
					}
				}
				""",
			"p/Z.java",
			"""
				package p;
				
				import java.util.*;
				
				import p.X.*;
				
				public class Z {
					private final Map<Class<? extends X>,\s
						              Y<?, ? extends XX<?, ?>, ? extends XY>>\s
						m1 = new HashMap<Class<? extends X>,\s
						                 Y<?, ? extends XX<?, ?>, ? extends XY>>();
				
					private Map<X.XX<?, XY>,\s
					            X.XY>\s
						m2 = new HashMap<X.XX<?, XY>,\s
						                 X.XY>();
				
					public <T extends X<T, U, V>,\s
					        U extends X.XX<T, V>,\s
					        V extends X.XY>\s
					Y<T, U, V> foo1(final Class<T> p1) {
						Y l1 = m1.get(p1);
						if (l1 == null) {
							l1 = new Y<T, U, V>(this);
							m1.put(p1, l1);
						}
						return l1;
					}
				
					public <TT extends X.XX<?, UU>,\s
					        UU extends X.XY>\s
					void foo2(final TT p1, final UU p2) {
						m2.put((XX<?, XY>) p1, p2);
					}
				
					public Map<XX<?, ?>, XY> foo3() {
						final Map<XX<?, ?>,\s
						          XY> l1 = new HashMap<XX<?, ?>,\s
						                               XY>();
						for (final Y<?,\s
								     ? extends XX<?, ?>,\s
								     ? extends XY>\s
								i : m1.values()) {
							i.foo5(l1);
						}
						return l1;
					}
				
					public void foo4(final Object p1, final Map<XX<?, ?>,\s
							                                    XY> p2) {
						for (final Y<?,\s
								     ? extends XX<?, ?>,\s
								     ? extends XY> i : m1.values()) {
							i.foo6(p2);
						}
						for (final Map.Entry<XX<?, ?>,\s
								             XY> i : p2.entrySet()) {
							final XX<?, XY> l1 = (XX<?, XY>) i.getKey();
						}
					}
				}
				"""
		},
        "\"" + OUTPUT_DIR +  File.separator + "p/Y.java\""
        + " \"" + OUTPUT_DIR +  File.separator + "p/Z.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + File.separator + "\""
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "\"",
        "",
        """
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 8)
				private final Map<Class<? extends X>,\s
				                                  ^
			X is a raw type. References to generic type X<T,U,V> should be parameterized
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 10)
				m1 = new HashMap<Class<? extends X>,\s
				                                 ^
			X is a raw type. References to generic type X<T,U,V> should be parameterized
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 22)
				Y l1 = m1.get(p1);
				^
			Y is a raw type. References to generic type Y<T,U,V> should be parameterized
			----------
			4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 25)
				m1.put(p1, l1);
				           ^^
			Type safety: The expression of type Y needs unchecked conversion to conform to Y<?,? extends X.XX<?,?>,? extends X.XY>
			----------
			5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 27)
				return l1;
				       ^^
			Type safety: The expression of type Y needs unchecked conversion to conform to Y<T,U,V>
			----------
			6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 33)
				m2.put((XX<?, XY>) p1, p2);
				       ^^^^^^^^^^^^^^
			Type safety: Unchecked cast from TT to X.XX<?,X.XY>
			----------
			7. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 58)
				final XX<?, XY> l1 = (XX<?, XY>) i.getKey();
				                ^^
			The value of the local variable l1 is not used
			----------
			8. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 58)
				final XX<?, XY> l1 = (XX<?, XY>) i.getKey();
				                     ^^^^^^^^^^^^^^^^^^^^^^
			Type safety: Unchecked cast from X.XX<capture#22-of ?,capture#23-of ?> to X.XX<?,X.XY>
			----------
			8 problems (8 warnings)
			""",
        false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=104664
public void test033(){
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR
        + "\"" + File.pathSeparator
        + " -repeat 2 -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "[repetition 1/2]\n" +
        "[repetition 2/2]\n",
        "",
        true);
}
public void test034(){
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp " + File.pathSeparator + "\"" + OUTPUT_DIR
        + "\"" + File.pathSeparator
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
}
// check classpath value
public void test035(){
	final String javaClassspath = System.getProperty("java.class.path");
	final String javaUserDir = System.getProperty("user.dir");
	try {
		System.setProperty("user.dir", OUTPUT_DIR);
		this.runConformTest(
			new String[] {
				"p/Y.java",
				"package p;\n" +
				"public class Y { public static final String S = \"\"; }",
			},
	        "\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "Y.java\""
	        + " -1.5 -g -preserveAllLocals -proceedOnError -referenceInfo ",
	        "",
	        "",
	        true);
		System.setProperty("java.class.path", "");
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						import p.Y;
						public class X {
							public static void main(String[] args) {
								System.out.print(Y.S);
							}
						}""",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		        + " -1.5 -g -preserveAllLocals -proceedOnError -referenceInfo ",
		        "",// this is not the runtime output
		        "no classpath defined, using default directory instead\n",
		        false);
		final String userDir = System.getProperty("user.dir");
		File f = new File(userDir, "X.java");
		if (!Util.delete(f)) {
			System.out.println("Could not delete X");
		}
		f = new File(userDir, "p" + File.separator + "Y.java");
		if (!Util.delete(f)) {
			System.out.println("Could not delete Y");
		}

	} finally {
		System.setProperty("java.class.path", javaClassspath);
		System.setProperty("user.dir", javaUserDir);
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=119108
// \ in call to AccessRulesSet.getViolatedRestriction
public void test036(){
	this.runConformTest(
		new String[] {
			"src1/p/X.java",
			"""
				package p;
				/** */
				public class X {
				}""",
		},
        "\"" + OUTPUT_DIR + "/src1/p/X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "/bin1/\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"src2/Y.java",
			"""
				/** */
				public class Y extends p.X {
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + File.separator + "bin1[~**/X]\""
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + File.separator + "bin2/\"",
        "",
        """
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 2)
				public class Y extends p.X {
				                       ^^^
			Discouraged access: The type \'X\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---/bin1\')
			----------
			1 problem (1 warning)
			""",
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=53773
// complain on assignment to parameters
public void test037() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo(int i, final int j) {
				    i =  0; // warning
				    j =  0; // error
				  }
				}
				"""},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.5 "
		+ " -cp \"" + OUTPUT_DIR + "\""
		+ " -warn:+paramAssign"
		+ " -proceedOnError"
		+ " -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				i =  0; // warning
				^
			The parameter i should not be assigned
			----------
			2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				j =  0; // error
				^
			The final local variable j cannot be assigned. It must be blank and not using a compound assignment
			----------
			2 problems (1 error, 1 warning)
			""",
		true);
}

// Missing access restriction violation error on generic type.
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=122995
// Binary case.
public void test039(){
	this.runConformTest(
		new String[] {
			"src1/p/X.java",
			"""
				package p;
				public class X<T> {
					T m;
				}""",
		},
        "\"" + OUTPUT_DIR + "/src1/p/X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "/bin1/\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"src2/Y.java",
			"""
				package p;
				public class Y {
					X x1;
					X<String> x2 = new X<String>();
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + File.separator + "bin1[~**/X]\""
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + File.separator + "bin2/\"",
        "",
        """
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 3)
				X x1;
				^
			Discouraged access: The type \'X<T>\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---/bin1\')
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 3)
				X x1;
				^
			X is a raw type. References to generic type X<T> should be parameterized
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 4)
				X<String> x2 = new X<String>();
				^
			Discouraged access: The type \'X<String>\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---/bin1\')
			----------
			4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 4)
				X<String> x2 = new X<String>();
				                   ^
			Discouraged access: The type \'X<String>\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---/bin1\')
			----------
			5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 4)
				X<String> x2 = new X<String>();
				                   ^
			Discouraged access: The constructor \'X<String>()\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---/bin1\')
			----------
			5 problems (5 warnings)
			""",
        false);
}

// check we get appropriate combination of access rules
public void test040(){
	this.runConformTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				/** */
				public class X {
				}""",
			"p/Z.java",
			"""
				package p;
				/** */
				public class Z {
				}"""
		},
        "\"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "X.java\""
        + " \"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "Z.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -nowarn"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				/** */
				public class Y {
				  p.X x;
				  p.Z z;
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[+p/X" + File.pathSeparator + "-p/*]\""
        + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        """
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Y.java (at line 4)
				p.Z z;
				^^^
			Access restriction: The type \'Z\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---\')
			----------
			1 problem (1 warning)
			""",
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=124533
// turn off discouraged references warnings
public void test041(){
	this.runConformTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				/** */
				public class X {
				}""",
			"p/Z.java",
			"""
				package p;
				/** */
				public class Z {
				}"""
		},
        "\"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "X.java\""
        + " \"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "Z.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -nowarn"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				/** */
				public class Y {
				  p.X x;
				  p.Z z;
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[~p/X" + File.pathSeparator + "-p/*]\""
        + " -warn:-discouraged -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        """
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Y.java (at line 4)
				p.Z z;
				^^^
			Access restriction: The type \'Z\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---\')
			----------
			1 problem (1 warning)
			""",
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=124533
// turn off forbidden references warnings
public void test042(){
	this.runConformTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				/** */
				public class X {
				}""",
			"p/Z.java",
			"""
				package p;
				/** */
				public class Z {
				}"""
		},
        "\"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "X.java\""
        + " \"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "Z.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -nowarn"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				/** */
				public class Y {
				  p.X x;
				  p.Z z;
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[~p/X" + File.pathSeparator + "-p/*]\""
        + " -warn:-forbidden -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        """
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Y.java (at line 3)
				p.X x;
				^^^
			Discouraged access: The type \'X\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---\')
			----------
			1 problem (1 warning)
			""",
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=124533
// turn off discouraged and forbidden references warnings
public void test043(){
	this.runConformTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				/** */
				public class X {
				}""",
			"p/Z.java",
			"""
				package p;
				/** */
				public class Z {
				}"""
		},
        "\"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "X.java\""
        + " \"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "Z.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -nowarn"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				/** */
				public class Y {
				  p.X x;
				  p.Z z;
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[~p/X" + File.pathSeparator + "-p/*]\""
        + " -warn:-discouraged,forbidden -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
		"",
        false);
}

// null ref option
public void test044(){
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = null;
				    o.toString();
				  }
				}"""},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -bootclasspath " + getLibraryClassesAsQuotedString()
        + " -warn:+null"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        """
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				o.toString();
				^
			Null pointer access: The variable o can only be null at this location
			----------
			1 problem (1 warning)
			""",
        true);
}

// null ref option
public void test045(){
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = null;
				    o.toString();
				  }
				}"""},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -bootclasspath " + getLibraryClassesAsQuotedString()
        + " -warn:-null" // contrast with test036
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "", true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=114456
// turn off discouraged and forbidden references warnings using SuppressWarnings all
public void test046(){
	this.runConformTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				/** */
				public class X {
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -nowarn"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				/** */
				@SuppressWarnings("all")
				public class Y {
				  p.X x;
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[~p/X" + File.pathSeparator + "-p/*]\""
        + " -warn:+discouraged,forbidden,deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
		"",
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=114456
// turn off discouraged and forbidden references warnings using SuppressWarnings restriction
public void test047(){
	this.runConformTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				/** */
				public class X {
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -nowarn"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				/** */
				@SuppressWarnings("restriction")
				public class Y {
				  p.X x;
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[~p/X" + File.pathSeparator + "-p/*]\""
        + " -warn:+discouraged,forbidden,deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
		"",
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=114456
// turn off discouraged and forbidden references warnings using SuppressWarnings
public void test048(){
	this.runConformTest(
		new String[] {
			"p/X.java",
			"""
				package p;
				/** */
				public class X {
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "p"  +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -nowarn"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
	this.runConformTest(
		new String[] {
			"Y.java",
			"""
				/** */
				@SuppressWarnings("deprecation")
				public class Y {
				  p.X x;
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[~p/X" + File.pathSeparator + "-p/*]\""
        + " -warn:+discouraged,forbidden,deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        """
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Y.java (at line 2)
				@SuppressWarnings("deprecation")
				                  ^^^^^^^^^^^^^
			Unnecessary @SuppressWarnings("deprecation")
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Y.java (at line 4)
				p.X x;
				^^^
			Discouraged access: The type \'X\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---\')
			----------
			2 problems (2 warnings)
			""",
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// disable warning on command line (implicit)
public void test049(){
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				    public void test(int p) {
				        switch (p) {
				        case 0:
				            System.out.println(0);
				        case 1:
				            System.out.println(1); // possible fall-through
				        }
				    }
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -nowarn"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// disable warning on command line (explicit)
public void test050(){
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				    public void test(int p) {
				        switch (p) {
				        case 0:
				            System.out.println(0);
				        case 1:
				            System.out.println(1); // possible fall-through
				        }
				    }
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -warn:-fallthrough"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// enable warning on command line
public void test051(){
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				    public void test(int p) {
				        switch (p) {
				        case 0:
				            System.out.println(0);
				        case 1:
				            System.out.println(1); // complain: possible fall-through
				        }
				    }
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -warn:+fallthrough"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)
				case 1:
				^^^^^^
			Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above
			----------
			1 problem (1 warning)
			""",
        true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=123476
public void test052(){
	try {
		new File(OUTPUT_DIR).mkdirs();
		File barFile = new File(OUTPUT_DIR +  File.separator + "Bar.java");
		try (FileOutputStream barOutput = new FileOutputStream(barFile)) {
			String barContents =
				"""
				public class Bar\t
				{\t
				  Bar(int class)\t
				  {\t
				  }\t
				}
				""";
			barOutput.write(barContents.getBytes());
		}
	} catch(IOException e) {
		// do nothing, will fail below
	}

	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X
				{
				  static Object x()
				  {
				    return new Bar(5);
				  }
				}
				""",
		},
     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
     + " -cp \"" + OUTPUT_DIR + File.pathSeparator + "\""
     + " -d \"" + OUTPUT_DIR + "\"",
     "",
     """
		----------
		1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
			return new Bar(5);
			       ^^^^^^^^^^
		The constructor Bar(int) is undefined
		----------
		----------
		2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/Bar.java (at line 2)
			{\t
			^
		Syntax error, insert "}" to complete ClassBody
		----------
		3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/Bar.java (at line 3)
			Bar(int class)\t
			        ^^^^^
		Syntax error on token "class", invalid VariableDeclaratorId
		----------
		4. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/Bar.java (at line 3)
			Bar(int class)\t
		  {\t
		  }\t
			        ^^^^^^^^^^^^^^^^
		Syntax error on tokens, delete these tokens
		----------
		5. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/Bar.java (at line 6)
			}
			^
		Syntax error on token "}", delete this token
		----------
		5 problems (5 errors)
		""",
     false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=137053
public void test053(){
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {}"
        },
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -d \"" + OUTPUT_DIR + File.separator + "X.java\"",
		"",
		"""
			No .class file created for file X.class in ---OUTPUT_DIR_PLACEHOLDER\
			---/X.java because of an IOException: Regular file \
			---OUTPUT_DIR_PLACEHOLDER---/X.java cannot be used \
			as output directory
			""",
		true);
}
// suggested by https://bugs.eclipse.org/bugs/show_bug.cgi?id=141522
// only checking messages (the bug itself involves concurrent access to
// the file system and a true test case would call for instrumented
// code)
public void test054(){
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {}",
			"f", // create simple file f
			""
        },
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -d \"" + OUTPUT_DIR + "/f/out\"",
		"",
		"""
			No .class file created for file X.class in ---OUTPUT_DIR_PLACEHOLDER\
			---/f/out because of an IOException: \
			Could not create output directory ---OUTPUT_DIR_PLACEHOLDER---/f/out
			""",
		true);
}
// suggested by https://bugs.eclipse.org/bugs/show_bug.cgi?id=141522
// only checking messages (the bug itself involves concurrent access to
// the file system and a true test case would call for instrumented
// code)
// this test only works on appropriate file systems
public void test055(){
	if (File.separatorChar == '/') {
	  	String tentativeOutputDirNameTail =
	      	File.separator + "out";
	  	File outputDirectory = new File(OUTPUT_DIR + tentativeOutputDirNameTail);
	  	outputDirectory.mkdirs();
	  	outputDirectory.setReadOnly();
	  	// read-only directories do not prevent file creation
	  	// on under-gifted file systems
		this.runConformTest(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {}",
	        },
	        "\"" + OUTPUT_DIR +  File.separator + "p/X.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -d \"" + OUTPUT_DIR + "/out\"",
			"",
			"""
				No .class file created for file p/X.class in \
				---OUTPUT_DIR_PLACEHOLDER---/out because of \
				an IOException: Could not create subdirectory p into output directory \
				---OUTPUT_DIR_PLACEHOLDER---/out
				""",
			false /* do not flush output directory */);
	}
}
// suggested by https://bugs.eclipse.org/bugs/show_bug.cgi?id=141522
// only checking messages (the bug itself involves concurrent access to
// the file system and a true test case would call for instrumented
// code)
public void test056(){
  	String tentativeOutputDirNameTail =
      	File.separator + "out";
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;\n" +
			"public class X {}",
			"out/p", // create simple file out/p
			""
        },
        "\"" + OUTPUT_DIR +  File.separator + "p/X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -d \"" + OUTPUT_DIR + tentativeOutputDirNameTail + "\"",
		"",
		"""
			No .class file created for file p/X.class in \
			---OUTPUT_DIR_PLACEHOLDER---/out\
			 because of an IOException: Regular file ---OUTPUT_DIR_PLACEHOLDER---\
			/out/p cannot be used as output directory
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147461
// the compilation is successful because we do not check the classpath entries
// given in the rules; accordingly OK<sep>-KO is seen as a directory that is
// added to positive rules, and the compilation completes normally
public void test057_access_restrictions_separator(){
	String oppositeSeparator = File.pathSeparatorChar == ':' ?
			";" : ":";
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
					OK1 ok1;
					OK2 ok2;
					KO ko;
				}""",
			"OK1.java",
			"""
				/** */
				public class OK1 {
					// empty
				}""",
			"OK2.java",
			"""
				/** */
				public class OK2 {
					// empty
				}""",
			"KO.java",
			"""
				/** */
				public class KO {
					// empty
				}""",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[+OK2" + oppositeSeparator + "-KO]\""
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "",
        true);
}

// .java ending directory name
// as a sibling of the compiled file
public void test058(){
  	File outputDirectory = new File(OUTPUT_DIR + File.separator + "foo.java");
  	outputDirectory.mkdirs();
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {}",
        },
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -d \"" + OUTPUT_DIR + "/out\"",
		"",
		"",
		false /* do not flush output directory */);
}
// .java ending directory name
// subdirectory of a compiled directory, unreferenced
public void test060(){
	File outputDirectory = new File(OUTPUT_DIR + File.separator + "foo.java");
	outputDirectory.mkdirs();
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {}",
        },
	    "\"" + OUTPUT_DIR + "\""
	    + " -1.5 -g -preserveAllLocals"
	    + " -d \"" + OUTPUT_DIR + "/out\"",
		"",
		"",
		false /* do not flush output directory */);
}

// .java ending directory name
// subdirectory of a compiled directory, referenced
public void test061(){
	File outputDirectory = new File(OUTPUT_DIR + File.separator + "foo.java");
	outputDirectory.mkdirs();
	this.runNegativeTest(
		new String[] {
				"X.java",
				"""
					public class X {
					  foo m;
					}""",
	        },
	    "\"" + OUTPUT_DIR + "\""
	    + " -1.5 -g -preserveAllLocals"
	    + " -d \"" + OUTPUT_DIR + "/out\"",
		"",
		"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)
				foo m;
				^^^
			foo cannot be resolved to a type
			----------
			1 problem (1 error)
			""",
		false /* do not flush output directory */);
}

// self-referential jar file
// variant using a relative path to the jar file in the -cp option
// this only tests that the fact the jar file references itself in a Class-Path
// clause does not break anything; the said clause is not needed for the
// compilation to succeed, and is merely irrelevant, but other compilers have
// shown a bug in that area
// TODO (maxime) improve management of current working directory
// we have a problem here in that the working directory is set once and for all
// from above, and we cannot change it afterwards (more on that when jdk7 gets
// ready); moreover, the default working directory may not the best choice we
// could expect (it is within the workspace, endangering the test project
// layout); this would need to be reconsidered from scratch, keeping in mind
// that some disk regions are not available for writing when running the releng
// tests; the pity is that this is precisely the case that fails with other
// compilers, whereas test063 passes;
// this problem affects other batch compiler tests as well, since we must be
// able to simulate command lines that would include relative paths; the
// solution probably encompasses putting the effective working directory under
// control;
public void _test062(){
	String outputDirName = OUTPUT_DIR + File.separator + "d",
	  metaInfDirName = outputDirName + File.separator + "META-INF",
	  jarFileName = outputDirName + File.separator + "L.jar";
//	  currentWorkingDirectory = System.getProperty("user.dir");
	this.runConformTest(
		new String[] {
			"d/Y.java",
			"public class Y {\n" +
			"}"},
	    "\"" + outputDirName + "\""
	    + " -1.5 -g -preserveAllLocals"
	    + " -d \"" + outputDirName + "\"",
		"",
		"",
		true /* flush output directory */);
	File outputDirectory = new File(outputDirName);
	File metaInfDirectory = new File(metaInfDirName);
	metaInfDirectory.mkdirs();
	try {
		Util.createFile(metaInfDirName + File.separator + "MANIFEST.MF",
			"Manifest-Version: 1.0\n" +
			"Class-Path: ../d/L.jar\n");
	} catch (IOException e) {
		fail("could not create manifest file");
	}
	try {
		Util.zip(outputDirectory, jarFileName);
	} catch (IOException e) {
		fail("could not create jar file");
	}
	Util.delete(outputDirName + File.separator + "Y.class");
	Util.delete(outputDirName + File.separator + "Y.java");
	this.runConformTest(
		new String[] {
			"d/X.java",
			"""
				public class X {
				  Y m;
				}"""},
	    "\"" + outputDirName + "\""
	    + " -1.5 -g -preserveAllLocals"
	    + " -cp L.jar"
	    + " -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false /* do not flush output directory */);
}

// self-referential jar file
// variant using an absolute path to the jar file in the -cp option
public void test063(){
	String outputDirName = OUTPUT_DIR + File.separator + "d",
	  metaInfDirName = outputDirName + File.separator + "META-INF",
	  jarFileName = outputDirName + File.separator + "L.jar";
	this.runConformTest(
		new String[] {
			"d/Y.java",
			"public class Y {\n" +
			"}"},
	    "\"" + outputDirName + "\""
	    + " -1.5 -g -preserveAllLocals"
	    + " -d \"" + outputDirName + "\"",
		"",
		"",
		true /* flush output directory */);
	File outputDirectory = new File(outputDirName);
	File metaInfDirectory = new File(metaInfDirName);
	metaInfDirectory.mkdirs();
	try {
		Util.createFile(metaInfDirName + File.separator + "MANIFEST.MF",
			"Manifest-Version: 1.0\n" +
			"Class-Path: ../d/L.jar\n");
	} catch (IOException e) {
		fail("could not create manifest file");
	}
	try {
		Util.zip(outputDirectory, jarFileName);
	} catch (IOException e) {
		fail("could not create jar file");
	}
	Util.delete(outputDirName + File.separator + "Y.class");
	Util.delete(outputDirName + File.separator + "Y.java");
	this.runConformTest(
		new String[] {
			"d/X.java",
			"""
				public class X {
				  Y m;
				}"""},
	    "\"" + outputDirName + "\""
	    + " -1.5 -g -preserveAllLocals"
	    + " -cp \"" + jarFileName + "\""
	    + " -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false /* do not flush output directory */);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=155809
// per sourcepath directory default encoding
public void _test064_per_sourcepath_directory_default_encoding(){
	String source1 = "src1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1
        + "[UTF-8]\"",
		"",
		"",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// pre-existing case 1: using a single, definite output directory
public void test065_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + " -d \"" + OUTPUT_DIR + File.separator + output1 + "\"",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// pre-existing case 2: using no definite output directory
public void test066_per_source_output_directory(){
	String source1 = "src1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1 + "\"",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// pre-existing case 3: -d none absorbs output
public void test067_per_source_output_directory(){
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -d none",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 1: overriding the default output directory for one of the sources
// -sourcepath series
public void test068_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1", output2 = "bin2";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d \"" + OUTPUT_DIR + File.separator + output2 + "\"",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 2: specifying an output directory for a given source directory only
// -sourcepath series
public void test069_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 3: [-d none] selectively absorbs output
// -sourcepath series
public void test070_per_source_output_directory(){
	String source1 = "src1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d none]",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 4: overriding -d none for one of the sources
// -sourcepath series
public void test071_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d none",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// [-d dir][rule] is forbidden
// -sourcepath series
public void test072_per_source_output_directory(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + "\"" + "[-d dir][~**/internal/*]",
		"",
		"access rules cannot follow destination path entries: ---OUTPUT_DIR_PLACEHOLDER---[-d dir][~**/internal/*]\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// [rule][-d dir] is ok
// -sourcepath series
public void test073_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1 + "\"" +
        	"[-**/*][-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Z.java (at line 2)
				X f;
				^
			Access restriction: The type \'X\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---/src1\')
			----------
			1 problem (1 warning)
			""",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 1: overriding the default output directory for one of the sources
// -classpath series
public void test074_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1", output2 = "bin2";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -classpath \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d \"" + OUTPUT_DIR + File.separator + output2 + "\"",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 2: specifying an output directory for a given source directory only
// -classpath series
public void test075_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -cp \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 3: [-d none] selectively absorbs output
// -classpath series
public void test076_per_source_output_directory(){
	String source1 = "src1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -classpath \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d none]",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 4: overriding -d none for one of the sources
// -classpath series
public void test077_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -classpath \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d none",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// [-d dir][rule] is forbidden
// -classpath series
public void test078_per_source_output_directory(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -classpath \"" + OUTPUT_DIR + "\"" + "[-d dir][~**/internal/*]",
		"",
		"access rules cannot follow destination path entries: ---OUTPUT_DIR_PLACEHOLDER---[-d dir][~**/internal/*]\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// [rule][-d dir] is ok
// -classpath series
public void test079_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -classpath \"" + OUTPUT_DIR + File.separator + source1 + "\"" +
        	"[-**/*][-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Z.java (at line 2)
				X f;
				^
			Access restriction: The type \'X\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---/src1\')
			----------
			1 problem (1 warning)
			""",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 1: overriding the default output directory for one of the sources
// -bootclasspath series
public void test080_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1", output2 = "bin2";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -bootclasspath " + getLibraryClassesAsQuotedString() + File.pathSeparator + "\"" +
          OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d \"" + OUTPUT_DIR + File.separator + output2 + "\"",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 2: specifying an output directory for a given source directory only
// -bootclasspath series
public void test081_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -bootclasspath " + getLibraryClassesAsQuotedString() + File.pathSeparator + "\"" +
          OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 3: [-d none] selectively absorbs output
// -bootclasspath series
public void test082_per_source_output_directory(){
	String source1 = "src1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -bootclasspath " + getLibraryClassesAsQuotedString() + File.pathSeparator + "\"" +
          OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d none]",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 4: overriding -d none for one of the sources
// -bootclasspath series
public void test083_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -bootclasspath " + getLibraryClassesAsQuotedString() + File.pathSeparator + "\"" +
          OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d none",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + File.separator + "Z.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// [-d dir][rule] is forbidden
// -bootclasspath series
public void test084_per_source_output_directory(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -bootclasspath \"" + OUTPUT_DIR + "\"" + "[-d dir][~**/internal/*]",
		"",
		"access rules cannot follow destination path entries: ---OUTPUT_DIR_PLACEHOLDER---[-d dir][~**/internal/*]\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// [rule][-d dir] is ok
// -bootclasspath series
public void test085_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -bootclasspath " + getLibraryClassesAsQuotedString() + File.pathSeparator +
        	"\"" + OUTPUT_DIR + File.separator + source1 + "\"" +
        	"[-**/*][-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Z.java (at line 2)
				X f;
				^
			Access restriction: The type \'X\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---/src1\')
			----------
			1 problem (1 warning)
			""",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// jar / zip files in sourcepath
public void test086_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1", output2 = "bin2";
	File outputDir = new File(OUTPUT_DIR),
		sourceDir = new File(OUTPUT_DIR + File.separator + source1);
	try {
		if (outputDir.exists()) {
			Util.flushDirectoryContent(outputDir);
		} else {
			outputDir.mkdirs();
		}
		sourceDir.mkdir();
		Util.createFile(OUTPUT_DIR + File.separator +
			source1 + File.separator + "X.java",
			"public class X {}");
		Util.zip(sourceDir,	OUTPUT_DIR + File.separator + "X.jar");
	} catch (IOException e) {
		fail("could not create jar file");
	}
	this.runConformTest(
		new String[] {
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + "X.jar\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d \"" + OUTPUT_DIR + File.separator + output2 + "\"",
		"",
		"",
		false); // keep jar
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// jar / zip files in classpath are binaries only: no -d argument
public void test087_per_source_output_directory(){
	String output1 = "bin1", output2 = "bin2";
	this.runNegativeTest(
		new String[] {
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -classpath \"" + OUTPUT_DIR + File.separator + "X.jar\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d \"" + OUTPUT_DIR + File.separator + output2 + "\"",
		"",
		"unexpected destination path entry for file: ---OUTPUT_DIR_PLACEHOLDER---/X.jar\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// jar / zip files in bootclasspath are binaries only: no -d argument
public void test088_per_source_output_directory(){
	String output1 = "bin1", output2 = "bin2";
	this.runNegativeTest(
		new String[] {
			"Z.java",
			"public class Z {\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -bootclasspath \"" + OUTPUT_DIR + File.separator + "X.jar\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d \"" + OUTPUT_DIR + File.separator + output2 + "\"",
		"",
		"unexpected destination path entry for file: ---OUTPUT_DIR_PLACEHOLDER---/X.jar\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 2: specifying an output directory for a given source directory only
// jar / zip files in sourcepath
public void test089_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	File outputDir = new File(OUTPUT_DIR),
		sourceDir = new File(OUTPUT_DIR + File.separator + source1),
		standardXOutputFile = new File(System.getProperty("user.dir") +
			File.separator + "X.class");
	try {
		if (outputDir.exists()) {
			Util.flushDirectoryContent(outputDir);
		} else {
			outputDir.mkdirs();
		}
		sourceDir.mkdir();
		Util.createFile(OUTPUT_DIR + File.separator +
			source1 + File.separator + "X.java",
			"public class X {}");
		Util.zip(sourceDir,	OUTPUT_DIR + File.separator + "X.jar");
		if (standardXOutputFile.exists()) {
			Util.delete(standardXOutputFile);
		}
	} catch (IOException e) {
		fail("could not create jar file");
	}
	this.runConformTest(
		new String[] {
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + "X.jar\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        ,
		"",
		"",
		false);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	assertFalse("extraneous file: " + standardXOutputFile.getPath(),
		standardXOutputFile.exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 3: [-d none] selectively absorbs output
// jar / zip files
public void test090_per_source_output_directory(){
	String source1 = "src1";
	File outputDir = new File(OUTPUT_DIR),
		sourceDir = new File(OUTPUT_DIR + File.separator + source1),
		standardXOutputFile = new File(System.getProperty("user.dir") +
			File.separator + "X.class");
	try {
		if (outputDir.exists()) {
			Util.flushDirectoryContent(outputDir);
		} else {
			outputDir.mkdirs();
		}
		sourceDir.mkdir();
		Util.createFile(OUTPUT_DIR + File.separator +
			source1 + File.separator + "X.java",
			"public class X {}");
		Util.zip(sourceDir,	OUTPUT_DIR + File.separator + "X.jar");
		if (standardXOutputFile.exists()) {
			Util.delete(standardXOutputFile);
		}
	} catch (IOException e) {
		fail("could not create jar file");
	}
	this.runConformTest(
		new String[] {
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + "X.jar\""
        + "[-d none]",
		"",
		"",
		false);
	String fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	assertFalse("extraneous file: " + standardXOutputFile.getPath(),
		standardXOutputFile.exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 4: overriding -d none for one of the sources
// jar / zip files
public void test091_per_source_output_directory(){
	String source1 = "src1", output1 = "bin1";
	File outputDir = new File(OUTPUT_DIR),
		sourceDir = new File(OUTPUT_DIR + File.separator + source1),
		standardXOutputFile = new File(System.getProperty("user.dir") +
			File.separator + "X.class");
	try {
		if (outputDir.exists()) {
			Util.flushDirectoryContent(outputDir);
		} else {
			outputDir.mkdirs();
		}
		sourceDir.mkdir();
		Util.createFile(OUTPUT_DIR + File.separator +
			source1 + File.separator + "X.java",
			"public class X {}");
		Util.zip(sourceDir,	OUTPUT_DIR + File.separator + "X.jar");
		if (standardXOutputFile.exists()) {
			Util.delete(standardXOutputFile);
		}
	} catch (IOException e) {
		fail("could not create jar file");
	}
	this.runConformTest(
		new String[] {
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -d none",
		"",
		"",
		false);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 1: overriding the default output directory for one of the sources
// source directories series
public void test092_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1", output2 = "bin2";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -1.5"
        + " -d \"" + OUTPUT_DIR + File.separator + output2 + "\"",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 2: specifying an output directory for a given source directory only
// source directories series
public void test093_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -1.5",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 3: [-d none] selectively absorbs output
// source directories series
public void test094_per_source_output_directory(){
	String source1 = "src1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  // X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d none]"
        + " -1.5",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 3: [-d none] selectively absorbs output
// source directories series
// variant: swap entries
public void test095_per_source_output_directory(){
	String source1 = "src1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  // X f;
				}""",
        },
        " \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d none] "
        + "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " -1.5",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 4: overriding -d none for one of the sources
// source directories series
public void test096_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			"Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"Z.java\""
        + " \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -1.5"
        + " -d none",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + "Z.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// new case 4: overriding -d none for one of the sources
// source directories series
// variant: two source folders
public void test097_per_source_output_directory(){
	String source1 = "src1", source2 = "src2",
		output1 = "bin1", output2 = "bin2";
	this.runConformTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
			source2 + File.separator + "Z.java",
			"""
				public class Z {
				  X f;
				}""",
        },
        " \"" + OUTPUT_DIR + File.separator + source2 + "\""
        + " \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -1.5"
        + " -d none",
		"",
		"",
		true);
	String fileName = OUTPUT_DIR + File.separator + output1 +
			File.separator + "X.class";
	assertTrue("missing file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source1 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + output2 +
			File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
	fileName = OUTPUT_DIR + File.separator + source2 +
			File.separator + "Z.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// [rule] is forbidden for source directories
public void test098_per_source_output_directory(){
	String source1 = "src1";
	this.runNegativeTest(
		new String[] {
			source1 + File.separator + "X.java",
			"""
				public class X {
				  Zork z;
				}"""},
        "\"" + OUTPUT_DIR +  File.separator + source1 + "\""
        + "[~**/internal/*]"
        + " -1.5",
		"",
		"unsupported encoding format: ~**/internal/*\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// changing the coding of -d none option
public void test099_per_source_output_directory() {
	File none = new File(Main.NONE);
	if (none.exists()) {
		fail("unexpected file: " + none.getAbsolutePath() +
				"; please cleanup the test environment");
		// by design, we do not want to agressively destroy a directory that
		// could well exist outside of our dedicated output area
		// TODO (maxime) one more case that calls for a better management of the
		//               current working directory in our batch compiler tests
	}
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {}",
        },
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -d none",
		"",
		"",
		true);
	String fileName = Main.NONE + File.separator + "X.class";
	assertFalse("extraneous file: " + fileName, (new File(fileName)).exists());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// -extdirs cannot receive a -d option
public void test100_per_source_output_directory(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -extdirs \"" + OUTPUT_DIR + "\"" + "[-d dir]",
		"",
		"unexpected destination path entry in -extdir option\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// -endorseddirs cannot receive a -d option
public void test101_per_source_output_directory(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -endorseddirs \"" + OUTPUT_DIR + "\"" + "[-d dir]",
		"",
		"unexpected destination path entry in -endorseddirs option\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// bad syntax
public void test102_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runNegativeTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
        },
        " \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + "[-d \"" + OUTPUT_DIR + File.separator + output1 + "\""
        + " -1.5"
        + " -d none",
		"",
		"incorrect destination path entry: [-d ---OUTPUT_DIR_PLACEHOLDER---/bin1\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// bad syntax
public void test103_per_source_output_directory(){
	String source1 = "src1",
		output1 = "bin1";
	this.runNegativeTest(
		new String[] {
			source1 + File.separator + "/X.java",
			"public class X {}",
        },
        " \"" + OUTPUT_DIR + File.separator + source1 + "\""
        + " [-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]"
        + " -1.5",
		"",
		"unexpected bracket: [-d\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// bad syntax
public void test104_per_source_output_directory(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + "\"" + "[[-d dir]",
		"",
		"unexpected bracket: ---OUTPUT_DIR_PLACEHOLDER---[[-d\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// bad syntax
public void test105_per_source_output_directory(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -cp \"" + OUTPUT_DIR + "\"" + "[-d dir]]",
		"",
		"unexpected bracket: dir]]\n",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=146554
// per source directory output directory
// bad syntax
public void test106_per_source_output_directory(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}"},
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\""
        + " -1.5"
        + " -cp \"" + OUTPUT_DIR + "\"" + "[-d dir1" + File.pathSeparator +
        	"dir2]",
		"",
		"incorrect destination path entry: ---OUTPUT_DIR_PLACEHOLDER---" +
			"[-d dir1" + File.pathSeparator + "dir2]\n",
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
// source 1.3 compliance 1.3
public void test107() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
     + " -1.3 -source 1.3 -d \"" + OUTPUT_DIR + "\"",
     "",
     "",
     true);
	String expectedOutput = "// Compiled from X.java (version 1.1 : 45.3, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.4 source 1.3
public void test108() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.4 -source 1.3 -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.2 : 46.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.4 source 1.4
public void test109() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.4 -source 1.4 -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.4 : 48.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.5 source 1.3
public void test110() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.5 -source 1.3 -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.4 : 48.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.5 source 1.4
public void test111() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.5 -source 1.4 -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.4 : 48.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.5 source 1.5
public void test112() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.5 -source 1.5 -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.5 : 49.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.6 source 1.3
public void test113() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.6 -source 1.3 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.4 : 48.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.6 source 1.4
public void test114() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.6 -source 1.4 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.4 : 48.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.6 source 1.5
public void test115() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.6 -source 1.5 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.6 : 50.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.6 source 1.6
public void test116() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.6 -source 1.6 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.6 : 50.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.7 source 1.3
public void test117() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.7 -source 1.3 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.4 : 48.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.7 source 1.4
public void test118() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.7 -source 1.4 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.4 : 48.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.7 source 1.5
public void test119() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.7 -source 1.5 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.6 : 50.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.7 source 1.6
public void test120() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.7 -source 1.6 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.6 : 50.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=141830
//compliance 1.7 source 1.7
// TODO part of the changes for 206483
public void test121() throws Exception {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.7 -source 1.7 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	String expectedOutput = "// Compiled from X.java (version 1.7 : 51.0, super bit)";
	checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
}
// command line - unusual classpath (ends with ';;;', still OK)
public void test122_classpath(){
	runClasspathTest(
		OUTPUT_DIR + "[+**/OK2]" + File.pathSeparator + File.pathSeparator +
				File.pathSeparator,
		new String[] {
			OUTPUT_DIR,	"{pattern=**/OK2 (ACCESSIBLE)}", null,
		},
		null);
}
// command line - unusual classpath (rules with multiple path separators KO, but
// without any error message though)
public void test123_classpath(){
	String cp = OUTPUT_DIR + "[+OK2" + File.pathSeparator + File.pathSeparator +
			File.pathSeparator + "~Warn" + File.pathSeparator + "-KO]";
	runClasspathTest(
		cp,
		null,
		null);
}
// command line - unusual classpath (rules with embedded -d OK)
public void test124_classpath (){
	runClasspathTest(
		OUTPUT_DIR + "[+OK2" + File.pathSeparator +	"-d ~Warn" +
				File.pathSeparator + "-KO]",
		new String[] {
			OUTPUT_DIR,
				"{pattern=OK2 (ACCESSIBLE), pattern=d ~Warn (NON ACCESSIBLE), pattern=KO (NON ACCESSIBLE)}",
				null,
		},
		null);
}
// command line - unusual classpath (rules starting with -d KO)
public void test125_classpath() {
	String cp = OUTPUT_DIR + "[-d +OK2" + File.pathSeparator + "~Warn" +
			File.pathSeparator + "-KO]";
	runClasspathTest(
		cp,
		null,
		"incorrect destination path entry: " + cp);
}
// command line - unusual classpath (rules starting with -d KO)
public void test126_classpath() {
	String cp = OUTPUT_DIR + "[-d +OK2" + File.pathSeparator + "~Warn" +
			File.pathSeparator + "-KO][-d dummy]";
	runClasspathTest(
		cp,
		null,
		"incorrect destination path entry: " + cp);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=161996
public void test127_classpath() {
	String jarFile = OUTPUT_DIR + File.separator + "[squarebracket].jar";
	runClasspathTest(
		jarFile,
		new String[] {
			jarFile, null, null,
		},
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=161996
public void test128_classpath() {
	String jarFile = OUTPUT_DIR + File.separator + "[square][bracket].jar";
	runClasspathTest(
		jarFile,
		new String[] {
			jarFile, null, null,
		},
		null);
}
// command line - classpath order
public void test129_classpath() {
	runClasspathTest(
		"file.jar[+A]" + File.pathSeparator + OUTPUT_DIR,
		new String[] {
			"file.jar",	"{pattern=A (ACCESSIBLE)}", null,
			OUTPUT_DIR, null, null,
		},
		null);
}
// command line - output directories
// see also test072
public void test130_classpath() {
	String cp = OUTPUT_DIR + "[-d dir][~**/internal/*]";
	runClasspathTest(
		cp,
		null,
		"access rules cannot follow destination path entries: " + cp);
}
// command line - output directories
public void test131_classpath() {
	String cp = OUTPUT_DIR + "[~**/internal/*][-d dir]";
	runClasspathTest(
		cp,
		new String[] {
			OUTPUT_DIR,	"{pattern=**/internal/* (DISCOURAGED)}", "dir",
		},
		null);
}
// command line - brackets in classpath
// unbalanced brackets fail (without any message though)
public void test132_classpath() {
	String cp = OUTPUT_DIR + "[~**/internal/*[-d dir]";
	runClasspathTest(
		cp,
		null,
		null);
}
// command line - brackets in classpath
// unbalanced brackets fail (without any message though)
public void test133_classpath() {
	String cp = OUTPUT_DIR + "[~**/internal/*]-d dir]";
	runClasspathTest(
		cp,
		null,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=161996
public void test134_classpath() {
	String jarFile = OUTPUT_DIR + File.separator + "[squarebracket].jar";
	runClasspathTest(
		jarFile + "[~**/internal/*][-d " + OUTPUT_DIR + "]",
		new String[] {
			jarFile, "{pattern=**/internal/* (DISCOURAGED)}", OUTPUT_DIR,
		},
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=161996
public void test135_classpath() {
	String jarFile = OUTPUT_DIR + File.separator + "[square][bracket].jar";
	runClasspathTest(
		jarFile + "[~**/internal/*][-d dir]",
		new String[] {
			jarFile, "{pattern=**/internal/* (DISCOURAGED)}", "dir",
		},
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=161996
public void test136_classpath() {
	String target = OUTPUT_DIR + File.separator + "[a]";
	(new File(target)).mkdirs();
	runClasspathTest(
		target + File.separator,
		new String[] {
			target, null, null,
		},
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=161996
public void test137_classpath() {
	String target = OUTPUT_DIR + File.separator + "[a]";
	(new File(target)).mkdirs();
	runClasspathTest(
		target + File.separator + "[~**/internal/*][-d dir]",
		new String[] {
			target, "{pattern=**/internal/* (DISCOURAGED)}", "dir",
		},
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=161996
// too many brackets series KO (no error though)
public void test138_classpath() {
	runClasspathTest(
		OUTPUT_DIR + File.separator + "[a][~**/internal/*][-d dir]",
		null,
		null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=173416
// start with a bracket
public void test139_classpath() {
    String cp = "[a].jar";
    runClasspathTest(
        cp,
        new String [] {
            cp, null, null,
        },
        null);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=173416
// start with a bracket
public void test140_classpath() {
    String cp = "[a].jar";
    runClasspathTest(
        cp + "[~**/internal/*][-d dir]",
        new String [] {
            cp, "{pattern=**/internal/* (DISCOURAGED)}", "dir",
        },
        null);
}
// null ref option
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
public void test141_null_ref_option(){
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = null;
				    o.toString();
				  }
				}"""},
     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
     + " -1.5 -g -preserveAllLocals"
     + " -bootclasspath " + getLibraryClassesAsQuotedString()
     + " -warn:+nullDereference"
     + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
     "",
     """
		----------
		1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
			o.toString();
			^
		Null pointer access: The variable o can only be null at this location
		----------
		1 problem (1 warning)
		""",
     true);
}
// null ref option
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
public void test142_null_ref_option(){
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = null;
				    if (o == null) {}
				  }
				}"""},
  "\"" + OUTPUT_DIR +  File.separator + "X.java\""
  + " -1.5 -g -preserveAllLocals"
  + " -bootclasspath " + getLibraryClassesAsQuotedString()
  + " -warn:+null"
  + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
  "",
  """
	----------
	1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
		if (o == null) {}
		    ^
	Redundant null check: The variable o can only be null at this location
	----------
	1 problem (1 warning)
	""",
  true);
}
// null ref option
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=170704
// adding distinct options to control null checks in more detail
public void test143_null_ref_option(){
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  void foo() {
				    Object o = null;
				    if (o == null) {}
				  }
				}"""},
"\"" + OUTPUT_DIR +  File.separator + "X.java\""
+ " -1.5 -g -preserveAllLocals"
+ " -bootclasspath " + getLibraryClassesAsQuotedString()
+ " -warn:+nullDereference"
+ " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
"",
"",
true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=190493
public void test144() throws Exception {
	String version = System.getProperty("java.class.version");
	if ("49.0".equals(version)) {
		this.runConformTest(
			new String[] {
				"X.java",
				"""
					/** */
					public class X {
					}""",
			},
			"\"" + OUTPUT_DIR +  File.separator + "X.java\""
			+ " -1.6 -source 1.6 -d \"" + OUTPUT_DIR + "\"",
			"",
			"Annotation processing got disabled, since it requires a 1.6 compliant JVM\n",
			true);
		String expectedOutput = "// Compiled from X.java (version 1.6 : 50.0, super bit)";
		checkDisassembledClassFile(OUTPUT_DIR + File.separator + "X.class", "X", expectedOutput);
	}
}
// reporting unnecessary declaration of thrown checked exceptions
// default is off
public void test145_declared_thrown_checked_exceptions(){
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public void foo() throws IOException {
				  }
				}
				"""},
  "\"" + OUTPUT_DIR +  File.separator + "X.java\""
  + " -1.5 -g -preserveAllLocals"
  + " -bootclasspath " + getLibraryClassesAsQuotedString()
  + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
  "",
  "",
  true);
}
// reporting unnecessary declaration of thrown checked exceptions
public void test146_declared_thrown_checked_exceptions(){
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				  public void foo() throws IOException {
				  }
				}
				"""},
  "\"" + OUTPUT_DIR +  File.separator + "X.java\""
  + " -1.5 -g -preserveAllLocals"
  + " -bootclasspath " + getLibraryClassesAsQuotedString()
  + " -warn:+unusedThrown"
  + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
  "",
  """
	----------
	1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
		public void foo() throws IOException {
		                         ^^^^^^^^^^^
	The declared exception IOException is not actually thrown by the method foo() from type X
	----------
	1 problem (1 warning)
	""",
  true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=122885
//coverage test
public void test148_access_restrictions(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
					KO ko;
				   void foo() {
				     ko = new KO();
				     ko.bar();
				     if (ko.m) {}
				   }
				   Zork z;
				}""",
			"KO.java",
			"""
				/** */
				public class KO {
				  void bar() {};
				  boolean m;
				}""",
		},
  "\"" + OUTPUT_DIR +  File.separator + "X.java\""
  + " -1.5 -g -preserveAllLocals"
  + " -cp \"" + OUTPUT_DIR + "[-KO]\""
  + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
  + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
  "",
  """
	----------
	1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
		KO ko;
		^^
	Access restriction: The type \'KO\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---\')
	----------
	2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
		ko = new KO();
		         ^^
	Access restriction: The type \'KO\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---\')
	----------
	3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
		ko = new KO();
		         ^^
	Access restriction: The constructor \'KO()\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---\')
	----------
	4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
		ko.bar();
		   ^^^
	Access restriction: The method \'KO.bar()\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---\')
	----------
	5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)
		if (ko.m) {}
		       ^
	Access restriction: The field \'KO.m\' is not API (restriction on classpath entry \'---OUTPUT_DIR_PLACEHOLDER---\')
	----------
	6. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 9)
		Zork z;
		^^^^
	Zork cannot be resolved to a type
	----------
	6 problems (1 error, 5 warnings)
	""",
  true);
}
//http://bugs.eclipse.org/bugs/show_bug.cgi?id=168230
public void test149() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo() {}
					public static void bar() {
						X.<String>foo();
					}
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.7 -warn:-unused -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// http://bugs.eclipse.org/bugs/show_bug.cgi?id=192875
// default in now on for nullDereference
public void test150_null_ref_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo() {
				     String s = null;
				     s.toString();
				   }
					// Zork z;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				s.toString();
				^
			Null pointer access: The variable s can only be null at this location
			----------
			1 problem (1 warning)
			""",
		true);
}
// http://bugs.eclipse.org/bugs/show_bug.cgi?id=192875
// default in now on for nullDereference
public void test151_null_ref_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo() {
				     String s = null;
				     s.toString();
				   }
					// Zork z;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:-nullDereference -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=138018
public void test152() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo() {
				     String s = null;
				     s.toString();
				   }
					// Zork z;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:-nullDereferences -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			invalid warning token: 'nullDereferences'. Ignoring warning and compiling
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				s.toString();
				^
			Null pointer access: The variable s can only be null at this location
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test153_warn_options() {
	// check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo() {
				     String s = null;
				     s.toString();
				     String u;
				   }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -proc:none -d \"" + OUTPUT_DIR + "\"",
//		+ " -warn:none -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				s.toString();
				^
			Null pointer access: The variable s can only be null at this location
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				String u;
				       ^
			The value of the local variable u is not used
			----------
			2 problems (2 warnings)
			""",
		true);
	// observe -warn options variations
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:none -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
}
// -warn option - regression tests
public void test154_warn_options() {
	// same source as 153, skip default checks
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo() {
				     String s = null;
				     s.toString();
				     String u;
				   }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:null -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				s.toString();
				^
			Null pointer access: The variable s can only be null at this location
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test155_warn_options() {
	// same source as 153, skip default checks
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo() {
				     String s = null;
				     s.toString();
				     String u;
				   }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:none -warn:null -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				s.toString();
				^
			Null pointer access: The variable s can only be null at this location
			----------
			1 problem (1 warning)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210518
// bad behavior for -warn:null -warn:unused
public void test156_warn_options() {
	// same source as 153, skip default checks
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo() {
				     String s = null;
				     s.toString();
				     String u;
				   }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:null -warn:unused -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				String u;
				       ^
			The value of the local variable u is not used
			----------
			1 problem (1 warning)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210518
// variant
public void test157_warn_options() {
	// same source as 153, skip default checks
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo() {
				     String s = null;
				     s.toString();
				     String u;
				   }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:null -warn:+unused -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				s.toString();
				^
			Null pointer access: The variable s can only be null at this location
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				String u;
				       ^
			The value of the local variable u is not used
			----------
			2 problems (2 warnings)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210518
// variant
public void test158_warn_options() {
	// same source as 153, skip default checks
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo() {
				     String s = null;
				     s.toString();
				     String u;
				   }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:null -warn:+unused -warn:-null -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				String u;
				       ^
			The value of the local variable u is not used
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test159_warn_options() {
	// same source as 153, skip default checks
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo() {
				     String s = null;
				     s.toString();
				     String u;
				   }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				String u;
				       ^
			The value of the local variable u is not used
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test160_warn_options() {
	// same source as 153, skip default checks
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo() {
				     String s = null;
				     s.toString();
				     String u;
				   }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:none -warn:+unused,null -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				s.toString();
				^
			Null pointer access: The variable s can only be null at this location
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				String u;
				       ^
			The value of the local variable u is not used
			----------
			2 problems (2 warnings)
			""",
		true);
}
// -warn option - regression tests
// this one is undocumented but makes some sense
public void test161_warn_options() {
	// same source as 153, skip default checks
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo() {
				     String s = null;
				     s.toString();
				     String u;
				   }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:-null,unused,+unused,null -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				s.toString();
				^
			Null pointer access: The variable s can only be null at this location
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				String u;
				       ^
			The value of the local variable u is not used
			----------
			2 problems (2 warnings)
			""",
		true);
}
// -warn option - regression tests
// this one is undocumented but makes some sense
public void test162_warn_options() {
	// same source as 153, skip default checks
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static void foo() {
				     String s = null;
				     s.toString();
				     String u;
				   }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -nowarn -warn:+null,unused,-unused,null -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// -warn option - regression tests
public void test163_warn_options() {
	// check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Y f;
				}""",
			"Y.java",
			"""
				/** @deprecated */
				public class Y {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)
				Y f;
				^
			The type Y is deprecated
			----------
			1 problem (1 warning)
			""",
		true);
	// observe -warn options variations
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:none -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
}
// -warn option - regression tests
public void test164_warn_options() {
	// same source as 163, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Y f;
				}""",
			"Y.java",
			"""
				/** @deprecated */
				public class Y {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -nowarn -deprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)
				Y f;
				^
			The type Y is deprecated
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test165_warn_options() {
	// same source as 163, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Y f;
				}""",
			"Y.java",
			"""
				/** @deprecated */
				public class Y {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -nowarn -deprecation -warn:-deprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// -warn option - regression tests
public void test166_warn_options() {
	// same source as 163, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Y f;
				}""",
			"Y.java",
			"""
				/** @deprecated */
				public class Y {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -nowarn -deprecation -warn:-allDeprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// -warn option - regression tests
public void test167_warn_options() {
	// same source as 163, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Y f;
				}""",
			"Y.java",
			"""
				/** @deprecated */
				public class Y {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:allDeprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)
				Y f;
				^
			The type Y is deprecated
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test168_warn_options() {
	// check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				    @param
				  */
				  public void foo() {
				  }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	// observe -warn options variations
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:javadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				@param
				 ^^^^^
			Javadoc: Missing parameter name
			----------
			1 problem (1 warning)
			""",
		false);
}
// -warn option - regression tests
public void test169_warn_options() {
	// same source as 168, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				    @param
				  */
				  public void foo() {
				  }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:allJavadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
				public class X {
				             ^
			Javadoc: Missing comment for public declaration
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				@param
				 ^^^^^
			Javadoc: Missing parameter name
			----------
			2 problems (2 warnings)
			""",
		true);
}
// -warn option - regression tests
public void test170_warn_options() {
	// same source as 168, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				    @param
				  */
				  public void foo() {
				  }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:javadoc -warn:-allJavadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210521
// -warn option - regression tests
public void test171_warn_options() {
	// same source as 168, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				    @param
				  */
				  public void foo() {
				  }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:allJavadoc -warn:-javadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
				public class X {
				             ^
			Javadoc: Missing comment for public declaration
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test172_warn_options() {
	// check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Y f;
				  /** @deprecated */
				  void foo(Y p) {
				  }
				}""",
			"Y.java",
			"""
				/** @deprecated */
				public class Y {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)
				Y f;
				^
			The type Y is deprecated
			----------
			1 problem (1 warning)
			""",
		true);
	// observe -warn options variations
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:allDeprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)
				Y f;
				^
			The type Y is deprecated
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				void foo(Y p) {
				         ^
			The type Y is deprecated
			----------
			2 problems (2 warnings)
			""",
		false);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210524
// -warn option - regression tests
public void _test173_warn_options() {
	// same source as 172, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Y f;
				  /** @deprecated */
				  void foo(Y p) {
				  }
				}""",
			"Y.java",
			"""
				/** @deprecated */
				public class Y {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:allDeprecation -warn:-deprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				void foo(Y p) {
				         ^
			The type Y is deprecated
			----------
			1 problem (1 warning)""",
		true);
}
// -warn option - regression tests
public void test174_warn_options() {
	// check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  int i;
				  class XX {
				    int i;
				  }
				  void foo(int i) {
				    class XX {
				    }
				    if (i > 0) {
				      try {
				        bar();
				      } catch (E2 e2) {
				      } catch (E1 e1) {
				      }
				    }
				  }
				  void bar() throws E2 {
				    throw new E2();
				  }
				}
				class E1 extends Exception {
				  private static final long serialVersionUID = 1L;
				}
				class E2 extends E1 {
				  private static final long serialVersionUID = 1L;
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)
				class XX {
				      ^^
			The type XX is hiding the type X.XX
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)
				class XX {
				      ^^
			The type XX is never used locally
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 13)
				} catch (E1 e1) {
				         ^^
			Unreachable catch block for E1. Only more specific exceptions are thrown and they are handled by previous catch block(s).
			----------
			3 problems (3 warnings)
			""",
		true);
	// observe -warn options variations
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -nowarn -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
}
// -warn option - regression tests
public void test175_warn_options() {
	// same source as 174, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  int i;
				  class XX {
				    int i;
				  }
				  void foo(int i) {
				    class XX {
				    }
				    if (i > 0) {
				      try {
				        bar();
				      } catch (E2 e2) {
				      } catch (E1 e1) {
				      }
				    }
				  }
				  void bar() throws E2 {
				    throw new E2();
				  }
				}
				class E1 extends Exception {
				  private static final long serialVersionUID = 1L;
				}
				class E2 extends E1 {
				  private static final long serialVersionUID = 1L;
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:hiding -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				int i;
				    ^
			The field X.XX.i is hiding a field from type X
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
				void foo(int i) {
				             ^
			The parameter i is hiding a field from type X
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)
				class XX {
				      ^^
			The type XX is hiding the type X.XX
			----------
			4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 13)
				} catch (E1 e1) {
				         ^^
			Unreachable catch block for E1. Only more specific exceptions are thrown and they are handled by previous catch block(s).
			----------
			4 problems (4 warnings)
			""",
		true);
}
// -warn option - regression tests
public void test176_warn_options() {
	// same source as 174, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  int i;
				  class XX {
				    int i;
				  }
				  void foo(int i) {
				    class XX {
				    }
				    if (i > 0) {
				      try {
				        bar();
				      } catch (E2 e2) {
				      } catch (E1 e1) {
				      }
				    }
				  }
				  void bar() throws E2 {
				    throw new E2();
				  }
				}
				class E1 extends Exception {
				  private static final long serialVersionUID = 1L;
				}
				class E2 extends E1 {
				  private static final long serialVersionUID = 1L;
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:fieldHiding -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				int i;
				    ^
			The field X.XX.i is hiding a field from type X
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test177_warn_options() {
	// same source as 174, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  int i;
				  class XX {
				    int i;
				  }
				  void foo(int i) {
				    class XX {
				    }
				    if (i > 0) {
				      try {
				        bar();
				      } catch (E2 e2) {
				      } catch (E1 e1) {
				      }
				    }
				  }
				  void bar() throws E2 {
				    throw new E2();
				  }
				}
				class E1 extends Exception {
				  private static final long serialVersionUID = 1L;
				}
				class E2 extends E1 {
				  private static final long serialVersionUID = 1L;
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:localHiding -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
				void foo(int i) {
				             ^
			The parameter i is hiding a field from type X
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test178_warn_options() {
	// same source as 174, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  int i;
				  class XX {
				    int i;
				  }
				  void foo(int i) {
				    class XX {
				    }
				    if (i > 0) {
				      try {
				        bar();
				      } catch (E2 e2) {
				      } catch (E1 e1) {
				      }
				    }
				  }
				  void bar() throws E2 {
				    throw new E2();
				  }
				}
				class E1 extends Exception {
				  private static final long serialVersionUID = 1L;
				}
				class E2 extends E1 {
				  private static final long serialVersionUID = 1L;
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:maskedCatchBlock -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 13)
				} catch (E1 e1) {
				         ^^
			Unreachable catch block for E1. Only more specific exceptions are thrown and they are handled by previous catch block(s).
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test179_warn_options() {
	// same source as 174, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  int i;
				  class XX {
				    int i;
				  }
				  void foo(int i) {
				    class XX {
				    }
				    if (i > 0) {
				      try {
				        bar();
				      } catch (E2 e2) {
				      } catch (E1 e1) {
				      }
				    }
				  }
				  void bar() throws E2 {
				    throw new E2();
				  }
				}
				class E1 extends Exception {
				  private static final long serialVersionUID = 1L;
				}
				class E2 extends E1 {
				  private static final long serialVersionUID = 1L;
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:typeHiding -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)
				class XX {
				      ^^
			The type XX is hiding the type X.XX
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test180_warn_options() {
	// same source as 174, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  int i;
				  class XX {
				    int i;
				  }
				  void foo(int i) {
				    class XX {
				    }
				    if (i > 0) {
				      try {
				        bar();
				      } catch (E2 e2) {
				      } catch (E1 e1) {
				      }
				    }
				  }
				  void bar() throws E2 {
				    throw new E2();
				  }
				}
				class E1 extends Exception {
				  private static final long serialVersionUID = 1L;
				}
				class E2 extends E1 {
				  private static final long serialVersionUID = 1L;
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:hiding -warn:-fieldHiding -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
				void foo(int i) {
				             ^
			The parameter i is hiding a field from type X
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)
				class XX {
				      ^^
			The type XX is hiding the type X.XX
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 13)
				} catch (E1 e1) {
				         ^^
			Unreachable catch block for E1. Only more specific exceptions are thrown and they are handled by previous catch block(s).
			----------
			3 problems (3 warnings)
			""",
		true);
}
// -warn option - regression tests
public void test181_warn_options() {
	// same source as 174, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  int i;
				  class XX {
				    int i;
				  }
				  void foo(int i) {
				    class XX {
				    }
				    if (i > 0) {
				      try {
				        bar();
				      } catch (E2 e2) {
				      } catch (E1 e1) {
				      }
				    }
				  }
				  void bar() throws E2 {
				    throw new E2();
				  }
				}
				class E1 extends Exception {
				  private static final long serialVersionUID = 1L;
				}
				class E2 extends E1 {
				  private static final long serialVersionUID = 1L;
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:hiding -warn:-localHiding -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				int i;
				    ^
			The field X.XX.i is hiding a field from type X
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)
				class XX {
				      ^^
			The type XX is hiding the type X.XX
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 13)
				} catch (E1 e1) {
				         ^^
			Unreachable catch block for E1. Only more specific exceptions are thrown and they are handled by previous catch block(s).
			----------
			3 problems (3 warnings)
			""",
		true);
}
// -warn option - regression tests
public void test182_warn_options() {
	// same source as 174, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  int i;
				  class XX {
				    int i;
				  }
				  void foo(int i) {
				    class XX {
				    }
				    if (i > 0) {
				      try {
				        bar();
				      } catch (E2 e2) {
				      } catch (E1 e1) {
				      }
				    }
				  }
				  void bar() throws E2 {
				    throw new E2();
				  }
				}
				class E1 extends Exception {
				  private static final long serialVersionUID = 1L;
				}
				class E2 extends E1 {
				  private static final long serialVersionUID = 1L;
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:hiding -warn:-maskedCatchBlock -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				int i;
				    ^
			The field X.XX.i is hiding a field from type X
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
				void foo(int i) {
				             ^
			The parameter i is hiding a field from type X
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)
				class XX {
				      ^^
			The type XX is hiding the type X.XX
			----------
			3 problems (3 warnings)
			""",
		true);
}
// -warn option - regression tests
public void test183_warn_options() {
	// same source as 174, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  int i;
				  class XX {
				    int i;
				  }
				  void foo(int i) {
				    class XX {
				    }
				    if (i > 0) {
				      try {
				        bar();
				      } catch (E2 e2) {
				      } catch (E1 e1) {
				      }
				    }
				  }
				  void bar() throws E2 {
				    throw new E2();
				  }
				}
				class E1 extends Exception {
				  private static final long serialVersionUID = 1L;
				}
				class E2 extends E1 {
				  private static final long serialVersionUID = 1L;
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:hiding -warn:-typeHiding -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				int i;
				    ^
			The field X.XX.i is hiding a field from type X
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
				void foo(int i) {
				             ^
			The parameter i is hiding a field from type X
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 13)
				} catch (E1 e1) {
				         ^^
			Unreachable catch block for E1. Only more specific exceptions are thrown and they are handled by previous catch block(s).
			----------
			3 problems (3 warnings)
			""",
		true);
}
// -warn option - regression tests
public void test184_warn_options() {
	// check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X extends Y {
				  public static int i;
				  void foo() {
				    if (this.i > X.j) {
				    }
				  }
				}
				class Y {
				  static int j;
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				if (this.i > X.j) {
				         ^
			The static field X.i should be accessed in a static way
			----------
			1 problem (1 warning)
			""",
		true);
	// observe -warn options variations
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -nowarn -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
}
// -warn option - regression tests
public void test185_warn_options() {
	// same source as 184, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X extends Y {
				  public static int i;
				  void foo() {
				    if (this.i > X.j) {
				    }
				  }
				}
				class Y {
				  static int j;
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:staticReceiver -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				if (this.i > X.j) {
				         ^
			The static field X.i should be accessed in a static way
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test186_warn_options() {
	// same source as 184, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X extends Y {
				  public static int i;
				  void foo() {
				    if (this.i > X.j) {
				    }
				  }
				}
				class Y {
				  static int j;
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:indirectStatic -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				if (this.i > X.j) {
				               ^
			The static field Y.j should be accessed directly
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test187_warn_options() {
	// same source as 184, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X extends Y {
				  public static int i;
				  void foo() {
				    if (this.i > X.j) {
				    }
				  }
				}
				class Y {
				  static int j;
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:static-access -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				if (this.i > X.j) {
				         ^
			The static field X.i should be accessed in a static way
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				if (this.i > X.j) {
				               ^
			The static field Y.j should be accessed directly
			----------
			2 problems (2 warnings)
			""",
		true);
}
// -warn option - regression tests
public void test188_warn_options() {
	// same source as 184, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X extends Y {
				  public static int i;
				  void foo() {
				    if (this.i > X.j) {
				    }
				  }
				}
				class Y {
				  static int j;
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:static-access -warn:-staticReceiver -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				if (this.i > X.j) {
				               ^
			The static field Y.j should be accessed directly
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test189_warn_options() {
	// same source as 184, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X extends Y {
				  public static int i;
				  void foo() {
				    if (this.i > X.j) {
				    }
				  }
				}
				class Y {
				  static int j;
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:static-access -warn:-indirectStatic -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				if (this.i > X.j) {
				         ^
			The static field X.i should be accessed in a static way
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test190_warn_options() {
	// check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X extends Y {
				  private void foo(int i) throws java.io.IOException {
				    int j;
				    this.<String>bar();
				    next: for (;;) {
				      return;
				    }
				  }
				  void bar() {
				  }
				}
				class Y {
				  <T> void bar() {
				  }
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
				import java.util.ArrayList;
				       ^^^^^^^^^^^^^^^^^^^
			The import java.util.ArrayList is never used
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The method foo(int) from the type X is never used locally
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				int j;
				    ^
			The value of the local variable j is not used
			----------
			4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				this.<String>bar();
				      ^^^^^^
			Unused type arguments for the non generic method bar() of type X; it should not be parameterized with arguments <String>
			----------
			5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
				next: for (;;) {
				^^^^
			The label next is never explicitly referenced
			----------
			5 problems (5 warnings)
			""",
		true);
	// observe -warn options variations
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -nowarn -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
}
// -warn option - regression tests
public void test191_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X extends Y {
				  private void foo(int i) throws java.io.IOException {
				    int j;
				    this.<String>bar();
				    next: for (;;) {
				      return;
				    }
				  }
				  void bar() {
				  }
				}
				class Y {
				  <T> void bar() {
				  }
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
				import java.util.ArrayList;
				       ^^^^^^^^^^^^^^^^^^^
			The import java.util.ArrayList is never used
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The method foo(int) from the type X is never used locally
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				                     ^
			The value of the parameter i is not used
			----------
			4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				                               ^^^^^^^^^^^^^^^^^^^
			The declared exception IOException is not actually thrown by the method foo(int) from type X
			----------
			5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				int j;
				    ^
			The value of the local variable j is not used
			----------
			6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				this.<String>bar();
				      ^^^^^^
			Unused type arguments for the non generic method bar() of type X; it should not be parameterized with arguments <String>
			----------
			7. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
				next: for (;;) {
				^^^^
			The label next is never explicitly referenced
			----------
			8. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 14)
				<T> void bar() {
				 ^
			Unused type parameter T
			----------
			8 problems (8 warnings)
			""",
		true);
}
// -warn option - regression tests
public void test192_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X extends Y {
				  private void foo(int i) throws java.io.IOException {
				    int j;
				    this.<String>bar();
				    next: for (;;) {
				      return;
				    }
				  }
				  void bar() {
				  }
				}
				class Y {
				  <T> void bar() {
				  }
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedArgument -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				                     ^
			The value of the parameter i is not used
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test193_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X extends Y {
				  private void foo(int i) throws java.io.IOException {
				    int j;
				    this.<String>bar();
				    next: for (;;) {
				      return;
				    }
				  }
				  void bar() {
				  }
				}
				class Y {
				  <T> void bar() {
				  }
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedImport -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
				import java.util.ArrayList;
				       ^^^^^^^^^^^^^^^^^^^
			The import java.util.ArrayList is never used
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test194_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X extends Y {
				  private void foo(int i) throws java.io.IOException {
				    int j;
				    this.<String>bar();
				    next: for (;;) {
				      return;
				    }
				  }
				  void bar() {
				  }
				}
				class Y {
				  <T> void bar() {
				  }
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedLabel -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
				next: for (;;) {
				^^^^
			The label next is never explicitly referenced
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test195_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X extends Y {
				  private void foo(int i) throws java.io.IOException {
				    int j;
				    this.<String>bar();
				    next: for (;;) {
				      return;
				    }
				  }
				  void bar() {
				  }
				}
				class Y {
				  <T> void bar() {
				  }
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedLocal -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				int j;
				    ^
			The value of the local variable j is not used
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test196_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X extends Y {
				  private void foo(int i) throws java.io.IOException {
				    int j;
				    this.<String>bar();
				    next: for (;;) {
				      return;
				    }
				  }
				  void bar() {
				  }
				}
				class Y {
				  <T> void bar() {
				  }
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedPrivate -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The method foo(int) from the type X is never used locally
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test197_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X extends Y {
				  private void foo(int i) throws java.io.IOException {
				    int j;
				    this.<String>bar();
				    next: for (;;) {
				      return;
				    }
				  }
				  void bar() {
				  }
				}
				class Y {
				  <T> void bar() {
				  }
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedTypeArgs -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				this.<String>bar();
				      ^^^^^^
			Unused type arguments for the non generic method bar() of type X; it should not be parameterized with arguments <String>
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test198_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X extends Y {
				  private void foo(int i) throws java.io.IOException {
				    int j;
				    this.<String>bar();
				    next: for (;;) {
				      return;
				    }
				  }
				  void bar() {
				  }
				}
				class Y {
				  <T> void bar() {
				  }
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedThrown -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				                               ^^^^^^^^^^^^^^^^^^^
			The declared exception IOException is not actually thrown by the method foo(int) from type X
			----------
			1 problem (1 warning)
			""",
		true);
}
// -warn option - regression tests
public void test199_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X extends Y {
				  private void foo(int i) throws java.io.IOException {
				    int j;
				    this.<String>bar();
				    next: for (;;) {
				      return;
				    }
				  }
				  void bar() {
				  }
				}
				class Y {
				  <T> void bar() {
				  }
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -warn:-unusedArgument -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
				import java.util.ArrayList;
				       ^^^^^^^^^^^^^^^^^^^
			The import java.util.ArrayList is never used
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The method foo(int) from the type X is never used locally
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				                               ^^^^^^^^^^^^^^^^^^^
			The declared exception IOException is not actually thrown by the method foo(int) from type X
			----------
			4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				int j;
				    ^
			The value of the local variable j is not used
			----------
			5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				this.<String>bar();
				      ^^^^^^
			Unused type arguments for the non generic method bar() of type X; it should not be parameterized with arguments <String>
			----------
			6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
				next: for (;;) {
				^^^^
			The label next is never explicitly referenced
			----------
			7. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 14)
				<T> void bar() {
				 ^
			Unused type parameter T
			----------
			7 problems (7 warnings)
			""",
		true);
}
// -warn option - regression tests
public void test200_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X extends Y {
				  private void foo(int i) throws java.io.IOException {
				    int j;
				    this.<String>bar();
				    next: for (;;) {
				      return;
				    }
				  }
				  void bar() {
				  }
				}
				class Y {
				  <T> void bar() {
				  }
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -warn:-unusedImport -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The method foo(int) from the type X is never used locally
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				                     ^
			The value of the parameter i is not used
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				                               ^^^^^^^^^^^^^^^^^^^
			The declared exception IOException is not actually thrown by the method foo(int) from type X
			----------
			4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				int j;
				    ^
			The value of the local variable j is not used
			----------
			5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				this.<String>bar();
				      ^^^^^^
			Unused type arguments for the non generic method bar() of type X; it should not be parameterized with arguments <String>
			----------
			6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
				next: for (;;) {
				^^^^
			The label next is never explicitly referenced
			----------
			7. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 14)
				<T> void bar() {
				 ^
			Unused type parameter T
			----------
			7 problems (7 warnings)
			""",
		true);
}
// -warn option - regression tests
public void test201_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X extends Y {
				  private void foo(int i) throws java.io.IOException {
				    int j;
				    this.<String>bar();
				    next: for (;;) {
				      return;
				    }
				  }
				  void bar() {
				  }
				}
				class Y {
				  <T> void bar() {
				  }
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -warn:-unusedLabel -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
				import java.util.ArrayList;
				       ^^^^^^^^^^^^^^^^^^^
			The import java.util.ArrayList is never used
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The method foo(int) from the type X is never used locally
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				                     ^
			The value of the parameter i is not used
			----------
			4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				                               ^^^^^^^^^^^^^^^^^^^
			The declared exception IOException is not actually thrown by the method foo(int) from type X
			----------
			5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				int j;
				    ^
			The value of the local variable j is not used
			----------
			6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				this.<String>bar();
				      ^^^^^^
			Unused type arguments for the non generic method bar() of type X; it should not be parameterized with arguments <String>
			----------
			7. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 14)
				<T> void bar() {
				 ^
			Unused type parameter T
			----------
			7 problems (7 warnings)
			""",
		true);
}
// -warn option - regression tests
public void test202_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X extends Y {
				  private void foo(int i) throws java.io.IOException {
				    int j;
				    this.<String>bar();
				    next: for (;;) {
				      return;
				    }
				  }
				  void bar() {
				  }
				}
				class Y {
				  <T> void bar() {
				  }
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -warn:-unusedLocal -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
				import java.util.ArrayList;
				       ^^^^^^^^^^^^^^^^^^^
			The import java.util.ArrayList is never used
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The method foo(int) from the type X is never used locally
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				                     ^
			The value of the parameter i is not used
			----------
			4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				                               ^^^^^^^^^^^^^^^^^^^
			The declared exception IOException is not actually thrown by the method foo(int) from type X
			----------
			5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				this.<String>bar();
				      ^^^^^^
			Unused type arguments for the non generic method bar() of type X; it should not be parameterized with arguments <String>
			----------
			6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
				next: for (;;) {
				^^^^
			The label next is never explicitly referenced
			----------
			7. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 14)
				<T> void bar() {
				 ^
			Unused type parameter T
			----------
			7 problems (7 warnings)
			""",
		true);
}
// -warn option - regression tests
public void test203_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X extends Y {
				  private void foo(int i) throws java.io.IOException {
				    int j;
				    this.<String>bar();
				    next: for (;;) {
				      return;
				    }
				  }
				  void bar() {
				  }
				}
				class Y {
				  <T> void bar() {
				  }
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -warn:-unusedPrivate -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
				import java.util.ArrayList;
				       ^^^^^^^^^^^^^^^^^^^
			The import java.util.ArrayList is never used
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				                     ^
			The value of the parameter i is not used
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				                               ^^^^^^^^^^^^^^^^^^^
			The declared exception IOException is not actually thrown by the method foo(int) from type X
			----------
			4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				int j;
				    ^
			The value of the local variable j is not used
			----------
			5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				this.<String>bar();
				      ^^^^^^
			Unused type arguments for the non generic method bar() of type X; it should not be parameterized with arguments <String>
			----------
			6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
				next: for (;;) {
				^^^^
			The label next is never explicitly referenced
			----------
			7. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 14)
				<T> void bar() {
				 ^
			Unused type parameter T
			----------
			7 problems (7 warnings)
			""",
		true);
}
// -warn option - regression tests
public void test204_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X extends Y {
				  private void foo(int i) throws java.io.IOException {
				    int j;
				    this.<String>bar();
				    next: for (;;) {
				      return;
				    }
				  }
				  void bar() {
				  }
				}
				class Y {
				  <T> void bar() {
				  }
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -warn:-unusedThrown -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
				import java.util.ArrayList;
				       ^^^^^^^^^^^^^^^^^^^
			The import java.util.ArrayList is never used
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The method foo(int) from the type X is never used locally
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				                     ^
			The value of the parameter i is not used
			----------
			4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				int j;
				    ^
			The value of the local variable j is not used
			----------
			5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				this.<String>bar();
				      ^^^^^^
			Unused type arguments for the non generic method bar() of type X; it should not be parameterized with arguments <String>
			----------
			6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
				next: for (;;) {
				^^^^
			The label next is never explicitly referenced
			----------
			7. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 14)
				<T> void bar() {
				 ^
			Unused type parameter T
			----------
			7 problems (7 warnings)
			""",
		true);
}
// -warn option - regression tests
public void test205_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X extends Y {
				  private void foo(int i) throws java.io.IOException {
				    int j;
				    this.<String>bar();
				    next: for (;;) {
				      return;
				    }
				  }
				  void bar() {
				  }
				}
				class Y {
				  <T> void bar() {
				  }
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -warn:-unusedTypeArgs -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
				import java.util.ArrayList;
				       ^^^^^^^^^^^^^^^^^^^
			The import java.util.ArrayList is never used
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				             ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			The method foo(int) from the type X is never used locally
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				                     ^
			The value of the parameter i is not used
			----------
			4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private void foo(int i) throws java.io.IOException {
				                               ^^^^^^^^^^^^^^^^^^^
			The declared exception IOException is not actually thrown by the method foo(int) from type X
			----------
			5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				int j;
				    ^
			The value of the local variable j is not used
			----------
			6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
				next: for (;;) {
				^^^^
			The label next is never explicitly referenced
			----------
			7. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 14)
				<T> void bar() {
				 ^
			Unused type parameter T
			----------
			7 problems (7 warnings)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// -warn option - regression tests
public void test206_warn_options() {
	// same source as 168, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				    @param
				  */
				  public void foo() {
				  }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:allJavadoc -enableJavadoc -warn:-allJavadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// -warn option - regression tests
public void test207_warn_options() {
	// same source as 168, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				    @param
				  */
				  public void foo() {
				  }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:javadoc -enableJavadoc -warn:-javadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// variant
public void test208_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				    @param i explained
				  */
				  public void foo(int i) {
				  }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				public void foo(int i) {
				                    ^
			The value of the parameter i is not used
			----------
			1 problem (1 warning)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// variant
public void test209_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				    @param i explained
				  */
				  public void foo(int i) {
				  }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -enableJavadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// variant
public void test210_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				    @param i explained
				  */
				  public void foo(int i) {
				  }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -warn:-allJavadoc -enableJavadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// variant
public void test211_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				    @param i explained
				  */
				  public void foo(int i) {
				  }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -enableJavadoc -warn:-allJavadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// variant - javadoc and allJavadoc mistakenly imply enableJavadoc
public void test212_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				    @param i explained
				  */
				  public void foo(int i) {
				  }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused,allJavadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
				public class X {
				             ^
			Javadoc: Missing comment for public declaration
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				public void foo(int i) {
				                    ^
			The value of the parameter i is not used
			----------
			2 problems (2 warnings)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// variant - javadoc and allJavadoc mistakenly imply enableJavadoc
public void test213_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				    @param i explained
				  */
				  public void foo(int i) {
				  }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused,javadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				public void foo(int i) {
				                    ^
			The value of the parameter i is not used
			----------
			1 problem (1 warning)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// variant - check impact of javadoc upon other warnings
public void _test216a_warn_options() {
	// check what if allJavadoc on
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */\
				public class X {
				  /**
				    {@link Y}
				  */
				  public void foo() {
				  }
				}
				""",
			"Y.java",
			"""
				/** @deprecated */\
				public class Y {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:allJavadoc -proc:none -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
	// same sources, what if we add -warn:+javadoc
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:allJavadoc -warn:+javadoc -proc:none -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
	// same sources, what if we only have -warn:javadoc
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:javadoc -proc:none -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// variant - check impact of javadoc upon other warnings
public void test216b_warn_options() {
	// check what if allJavadoc on
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */\
				public class X {
				  /**
				    {@link Y}
				  */
				  public void foo() {
				  }
				}
				""",
			"Y.java",
			"""
				/** @deprecated */\
				public class Y {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:+allJavadoc -proc:none -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				{@link Y}
				       ^
			Javadoc: The type Y is deprecated
			----------
			1 problem (1 warning)
			""",
		true);
	// same sources, what if we add -warn:+javadoc
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:+allJavadoc -warn:+javadoc -proc:none -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				{@link Y}
				       ^
			Javadoc: The type Y is deprecated
			----------
			1 problem (1 warning)
			""",
		false);
	// same sources, what if we only have -warn:javadoc
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:+javadoc -proc:none -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				{@link Y}
				       ^
			Javadoc: The type Y is deprecated
			----------
			1 problem (1 warning)
			""",
		false);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// variant - check impact of javadoc upon other warnings
public void test217_warn_options() {
	// check what if allJavadoc on
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */\
				public class X {
				  /**
				    @see #bar()
				  */
				  public void foo() {
				    bar();
				  }
				  private void bar() {
				  }
				}
				"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:allJavadoc -proc:none -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				@see #bar()
				     ^^^^^^
			Javadoc: \'public\' visibility for malformed doc comments hides this \'private\' reference
			----------
			1 problem (1 warning)
			""",
		true);
	// same sources, what if we add -warn:+javadoc
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:allJavadoc -warn:+javadoc -proc:none -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				@see #bar()
				     ^^^^^^
			Javadoc: \'public\' visibility for malformed doc comments hides this \'private\' reference
			----------
			1 problem (1 warning)
			""",
		false);
	// same sources, what if we only have -warn:javadoc
	this.runConformTest(
		new String[] { },
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:javadoc -proc:none -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				@see #bar()
				     ^^^^^^
			Javadoc: \'public\' visibility for malformed doc comments hides this \'private\' reference
			----------
			1 problem (1 warning)
			""",
		false);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=214731
// white-box test for internal API
public void test218_batch_classpath_apis() {
	assertFalse("path should be absolute",
		new ClasspathJar(new File("relative.jar"), true, null, null).
		getPath().indexOf(File.separator) == -1);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=214731
// white-box test for internal API
public void test219_batch_classpath_apis() {
	assertFalse("path should be absolute",
		CharOperation.indexOf('/',
			new ClasspathJar(new File("relative.jar"), true, null, null).
			normalizedPath()) == -1);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210524
// -warn option - regression tests
// variant
public void _test220_warn_options() {
	// same source as 172, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Y f;
				  /** @deprecated */
				  void foo(Y p) {
				  }
				}""",
			"Y.java",
			"""
				/** @deprecated */
				public class Y {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:allDeprecation -warn:+deprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)
				Y f;
				^
			The type Y is deprecated
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				void foo(Y p) {
				         ^
			The type Y is deprecated
			----------
			2 problems (2 warnings)""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210524
// -warn option - regression tests
// variant
public void test221_warn_options() {
	// same source as 172, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Y f;
				  /** @deprecated */
				  void foo(Y p) {
				  }
				}""",
			"Y.java",
			"""
				/** @deprecated */
				public class Y {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:allDeprecation -warn:deprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)
				Y f;
				^
			The type Y is deprecated
			----------
			1 problem (1 warning)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210524
// -warn option - regression tests
// variant
public void test222_warn_options() {
	// same source as 172, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Y f;
				  /** @deprecated */
				  void foo(Y p) {
				  }
				}""",
			"Y.java",
			"""
				/** @deprecated */
				public class Y {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:allDeprecation -deprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		// according to the documentation, equivalent to -warn:allDeprecation -warn:+deprecation
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)
				Y f;
				^
			The type Y is deprecated
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				void foo(Y p) {
				         ^
			The type Y is deprecated
			----------
			2 problems (2 warnings)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210524
// -warn option - regression tests
// variant
public void test223_warn_options() {
	// same source as 172, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  Y f;
				  /** @deprecated */
				  void foo(Y p) {
				  }
				}""",
			"Y.java",
			"""
				/** @deprecated */
				public class Y {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -deprecation -warn:-allDeprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210524
// -warn option - regression tests
// variant
public void _test224_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  int i;
				  X(int i) {
				  }
				}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:+localHiding,specialParamHiding -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				X(int i) {
				      ^
			The parameter i is hiding a field from type X
			----------
			1 problem (1 warning)""",
		true);
	// deprecation should erase whatever warnings have been set previously
	this.runConformTest(
		new String[] {},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:+localHiding,specialParamHiding -warn:deprecation -warn:+localHiding -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210524
// -warn option - regression tests
// variant
public void test225_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				@SuppressWarnings("deprecation")
				public class X {
				  Y f;
				  /** @deprecated */
				  void foo(Y p) {
				  }
				}""",
			"Y.java",
			"""
				/** @deprecated */
				public class Y {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.5 -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -deprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210524
// -warn option - regression tests
// variant
public void _test226_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				@SuppressWarnings("deprecation")
				public class X {
				  Y f;
				  /** @deprecated */
				  void foo(Y p) {
				  }
				}""",
			"Y.java",
			"""
				/** @deprecated */
				public class Y {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.5 -sourcepath \"" + OUTPUT_DIR + "\""
		// default -warn:+suppress gets overriden
		+ " -warn:deprecation -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				Y f;
				^
			The type Y is deprecated
			----------
			1 problem (1 warning)""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// -warn option - regression tests
// variant detected while exploring:
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=210524
public void test227_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
				  /**
				    @param i explained
				  */
				  public void foo(int i) {
				  }
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unused -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				public void foo(int i) {
				                    ^
			The value of the parameter i is not used
			----------
			1 problem (1 warning)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// -warn option - regression tests
// variant
public void test228_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				/** @throws IOException mute warning **/
				  public void foo() throws IOException {
				  }
				}
				""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedThrown -enableJavadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=211588
// -warn option - regression tests
// variant
public void test229_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.IOException;
				public class X {
				/** @throws IOException mute warning **/
				  public void foo() throws IOException {
				  }
				}
				""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedThrown -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				public void foo() throws IOException {
				                         ^^^^^^^^^^^
			The declared exception IOException is not actually thrown by the method foo() from type X
			----------
			1 problem (1 warning)
			""",
		true);
}
//-warn option - regression tests
public void test230_warn_options() {
	// same source as 190, skip check defaults
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.util.ArrayList;
				public class X<T>{
				  public X() {
				  }
				  public X(T t){}
				  void foo() {
				      X<String> x = new X<String>();
					   X<Number> x1 = new X<Number>(1);
				  }
				}
				"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedTypeArgs -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)
				X<String> x = new X<String>();
				                  ^
			Redundant specification of type arguments <String>
			----------
			1 problem (1 warning)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// .java/.class files precedence depending on sourcepath and other conditions
// ecj always selects source files from the sourcepath over class files
// javac selects the class file over the source file when the class file is
// newer than the source file, unless option -Xprefer:source is used (available
// since 1.6)
public void test230_sourcepath_vs_classpath() throws IOException, InterruptedException {
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"src1/X.java",
			"""
				public class X {
				  public static final int CONST = 1;
				}
				""",
			"src2/X.java",
			"""
				public class X {
				  public static final int CONST = 2;
				}
				""",
		},
		"\"" + OUTPUT_DIR + File.separator + "src1" + File.separator + "X.java\"" /* commandLine */
		+ " -verbose -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
		"""
			[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #1/1]
			[reading    java/lang/Object.class]
			[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #1/1]
			[writing    X.class - #1]
			[completed  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #1/1]
			[1 unit compiled]
			[1 .class file generated]
			""",
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* progress */);
	// ensure that class file is newer than source file (some file systems
	// store the modification time at a second precision)
	File sourceFile = new File(OUTPUT_DIR + File.separator + "src1" + File.separator + "X.java"),
	  classFile = new File(OUTPUT_DIR + File.separator + "bin1" + File.separator + "X.class");
	while (classFile.lastModified() <= sourceFile.lastModified()) {
		runConformTest(
			null,
			"\"" + OUTPUT_DIR +  File.separator + "src1" + File.separator + "X.java\""
			+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
			"",
			"",
			false);
	}
	// compile with classpath only: X.class gets selected
	runConformTest(
		new String[] {
			"Y.java",
			"""
				public class Y {
				  public static void main (String[] args) {
				    System.out.println(X.CONST);
				  }
				}
				""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "Y.java\""
		+ " -classpath \"" + OUTPUT_DIR + File.separator + "bin1" + "\""
		+ " -verbose -proc:none -d \"" + OUTPUT_DIR + "\"",
		"""
			[parsing    ---OUTPUT_DIR_PLACEHOLDER---/Y.java - #1/1]
			[reading    java/lang/Object.class]
			[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/Y.java - #1/1]
			[reading    java/lang/String.class]
			[reading    java/lang/System.class]
			[reading    java/io/PrintStream.class]
			[reading    X.class]
			[writing    Y.class - #1]
			[completed  ---OUTPUT_DIR_PLACEHOLDER---/Y.java - #1/1]
			[1 unit compiled]
			[1 .class file generated]
			""",
		"",
		false);
	// compile with sourcepath and classpath: src2/X.java is preferred
	// this matches -Xprefer:source option of javac - except that
	// javac then does it for classpath too; by default, javac would select
	// bin1/X.class (as shown below)
	String sourceFilePath = "\"" + OUTPUT_DIR +  File.separator + "Y.java\"";
	String commonOptions =
		" -classpath \"" + OUTPUT_DIR + File.separator + "bin1" + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + File.separator + "src2" + "\""
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin2" + "\"";
	String setting= System.getProperty("jdt.compiler.useSingleThread");
	try {
		System.setProperty("jdt.compiler.useSingleThread", "true");
		runConformTest(
			null,
			sourceFilePath + commonOptions + " -verbose -proc:none",
			"""
				[parsing    ---OUTPUT_DIR_PLACEHOLDER---/Y.java - #1/1]
				[reading    java/lang/Object.class]
				[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/Y.java - #1/1]
				[reading    java/lang/String.class]
				[reading    java/lang/System.class]
				[reading    java/io/PrintStream.class]
				[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src2/X.java - #2/2]
				[writing    Y.class - #1]
				[completed  ---OUTPUT_DIR_PLACEHOLDER---/Y.java - #1/2]
				[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src2/X.java - #2/2]
				[writing    X.class - #2]
				[completed  ---OUTPUT_DIR_PLACEHOLDER---/src2/X.java - #2/2]
				[2 units compiled]
				[2 .class files generated]
				""",
			"",
			false);
	} finally {
		System.setProperty("jdt.compiler.useSingleThread", setting == null ? "false" : setting);
	}
	if (RUN_JAVAC) {
		// run ecj result
		this.verifier.execute("Y", new String[] {OUTPUT_DIR + File.separator + "bin2"});
		assertTrue(this.verifier.getExecutionOutput().startsWith("2")); // skip trailing newline
		// 2 means we selected src2
		// recompile and run result using various levels of javac
		Iterator javacCompilersIterator = javacCompilers.iterator();
		String specialOptions = commonOptions + " -Xprefer:source ";
		String sourceFileNames[] = new String[] {sourceFilePath};
		File outputDir = new File(OUTPUT_DIR);
		while (javacCompilersIterator.hasNext()) {
			JavacCompiler javacCompiler = (JavacCompiler) javacCompilersIterator.next();
			assertTrue(javacCompiler.compile(
					outputDir, /* directory */
					commonOptions /* options */,
					sourceFileNames /* source file names */,
					null /* log */) == 0);
			this.verifier.execute("Y", new String[] {OUTPUT_DIR + File.separator + "bin2"});
			assertEquals('1', this.verifier.getExecutionOutput().charAt(0)); // skip trailing newline
			// 1 means javac selected bin1 by default
			if (javacCompiler.version.compareTo(JavaCore.VERSION_1_6) >= 0) {
				assertTrue(javacCompiler.compile(
						outputDir, /* directory */
						specialOptions /* options */,
						sourceFileNames /* source file names */,
						null /* log */) == 0);
				this.verifier.execute("Y", new String[] {OUTPUT_DIR + File.separator + "bin2"});
				assertEquals('2', this.verifier.getExecutionOutput().charAt(0)); // skip trailing newline
				// 2 means javac selected src2
			}
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// .java/.class files precedence depending on sourcepath
// ecj always selects sourcepath over classpath
// javac takes the source file if it is more recent than the class file
public void test231_sourcepath_vs_classpath() throws IOException, InterruptedException {
	// compile into bin1
	runConformTest(
		new String[] {
			"src1/X.java",
			"""
				public class X {
				  public static final int CONST = 1;
				}
				""",
		},
		"\"" + OUTPUT_DIR + File.separator + "src1" + File.separator + "X.java\""
		+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
		"",
		"",
		true);
	// ensure that source file is newer than class file (some file systems
	// store the modification time at a second precision)
	File sourceFile = new File(OUTPUT_DIR + File.separator + "src2" + File.separator + "X.java"),
	  classFile = new File(OUTPUT_DIR + File.separator + "bin1" + File.separator + "X.class");
	new File(OUTPUT_DIR + File.separator + "src2").mkdirs();
	do {
		Util.writeToFile(
			"public class X {\n" +
			"}\n",
			sourceFile.getPath());
	} while (classFile.lastModified() >= sourceFile.lastModified());
	// compile with sourcepath and classpath: src2/X.java is preferred
	String sourceFilePath = "\"" + OUTPUT_DIR +  File.separator + "Y.java\"";
	String commonOptions =
		" -classpath \"" + OUTPUT_DIR + File.separator + "bin1" + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + File.separator + "src2" + "\""
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin2" + "\"";
	// sourcepath preferred over classpath
	runTest(
		false /* shouldCompileOK */,
		new String[] { /* testFiles */
			"Y.java",
			"""
				public class Y {
				  public static void main (String[] args) {
				    System.out.println(X.CONST);
				  }
				}
				""",
		},
		sourceFilePath + commonOptions + " -proc:none " /* commandLine */,
		"" /* expectedOutOutputString */,
		"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/Y.java (at line 3)
				System.out.println(X.CONST);
				                     ^^^^^
			CONST cannot be resolved or is not a field
			----------
			1 problem (1 error)
			""",
		false /* shouldFlushOutputDirectory */,
		null /* progress */);
	if (RUN_JAVAC) {
		Iterator javacCompilersIterator = javacCompilers.iterator();
		String sourceFileNames[] = new String[] {sourceFilePath};
		File outputDir = new File(OUTPUT_DIR);
		while (javacCompilersIterator.hasNext()) {
			JavacCompiler javacCompiler = (JavacCompiler) javacCompilersIterator.next();
			assertFalse(javacCompiler.compile(
					outputDir /* directory */,
					commonOptions /* options */,
					sourceFileNames /* source file names */,
					null /* log */) == 0);
			// compile fails as well
		}
		assertFalse(runJavac(commonOptions, new String[] {sourceFilePath}, OUTPUT_DIR));
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// ecj different from javac: repeated -classpath concatenates entries, while javac
// only keeps the last one (and swallows the others silently)
public void test232_repeated_classpath() throws IOException, InterruptedException {
	String commonOptions = " -d \"" + OUTPUT_DIR + File.separator + "bin"
		+ "\" -classpath \"" + OUTPUT_DIR + File.separator + "src1";
	String combinedClasspathOptions = commonOptions + File.pathSeparator
		+ OUTPUT_DIR + File.separator + "src2\" ";
	String splitClasspathOptions = commonOptions
		+ "\" -classpath \"" + OUTPUT_DIR + File.separator + "src2\" ";
	String sourceFilePath = "\"" + OUTPUT_DIR + File.separator + "src3" + File.separator + "Z.java\"";
	// ecj considers repeated classpath entries as if they were concatenated
	// into one
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"src1/X.java",
			"public class X {\n" +
			"}\n",
			"src2/Y.java",
			"public class Y {\n" +
			"}\n",
			"src3/Z.java",
			"""
				public class Z {
				  X x;
				  Y y;
				}
				""",
		},
		sourceFilePath + " -proc:none " + combinedClasspathOptions /* commandLine */,
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* progress */);
	runTest(
		true /* shouldCompileOK*/,
		null /* testFiles */,
		sourceFilePath + " -proc:none " + splitClasspathOptions /* commandLine */,
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		false /* shouldFlushOutputDirectory */,
		null /* progress */);
	if (RUN_JAVAC) {
		// javac skips all but the last classpath entry (which results into an
		// error in the split case here)
		Iterator javacCompilersIterator = javacCompilers.iterator();
		String sourceFileNames[] = new String[] {sourceFilePath};
		File outputDir = new File(OUTPUT_DIR);
		while (javacCompilersIterator.hasNext()) {
			JavacCompiler javacCompiler = (JavacCompiler) javacCompilersIterator.next();
			assertTrue(javacCompiler.compile(
					outputDir /* directory */,
					combinedClasspathOptions /* options */,
					sourceFileNames,
					null /* log */) == 0);
			assertFalse(javacCompiler.compile(
					outputDir /* directory */,
					splitClasspathOptions /* options */,
					sourceFileNames,
					null /* log */) == 0);
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// ecj different from javac: repeated -sourcepath yields an error, while javac
// only keeps the last one (and swallows the others silently)
public void test233_repeated_sourcepath() throws IOException, InterruptedException {
	String commonOptions = " -d \"" + OUTPUT_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR + File.separator + "src1\""
		+ " -sourcepath \"" + OUTPUT_DIR + File.separator + "src2\"";
	String sourceFilePathZ = "\"" + OUTPUT_DIR + File.separator + "src3" + File.separator + "Z.java\"";
	String sourceFilePathW = "\"" + OUTPUT_DIR + File.separator + "src3" + File.separator + "W.java\"";
	runTest(
		false /* shouldCompileOK */,
		new String[] { /* testFiles */
			"src1/X.java",
			"public class X {\n" +
			"}\n",
			"src2/Y.java",
			"public class Y {\n" +
			"}\n",
			"src3/Z.java",
			"""
				public class Z {
				  Y y;
				}
				""",
			"src3/W.java",
			"""
				public class W {
				  X x;
				  Y y;
				}
				""",
		},
		sourceFilePathZ + " -proc:none " + commonOptions /* commandLine */,
		"" /* expectedOutOutputString */,
		"duplicate sourcepath specification: -sourcepath ---OUTPUT_DIR_PLACEHOLDER---/src2\n" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* progress */);
	if (RUN_JAVAC) {
		Iterator javacCompilersIterator = javacCompilers.iterator();
		String sourceFileNamesZ[] = new String[] {sourceFilePathZ};
		String sourceFileNamesW[] = new String[] {sourceFilePathW};
		File outputDir = new File(OUTPUT_DIR);
		while (javacCompilersIterator.hasNext()) {
			JavacCompiler javacCompiler = (JavacCompiler) javacCompilersIterator.next();
			// succeeds because it picks src2 up
			assertTrue(javacCompiler.compile(
					outputDir /* directory */,
					commonOptions /* options */,
					sourceFileNamesZ /* source file names */,
					null /* log */) == 0);
			// fails because it misses src1
			assertFalse(javacCompiler.compile(
					outputDir /* directory */,
					commonOptions /* options */,
					sourceFileNamesW /* source file names */,
					null /* log */) == 0);
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// different from javac: javac sourcepath inhibits compile in classpath, while
// ecj goes on finding source files there
public void test234_sourcepath_vs_classpath() throws IOException, InterruptedException {
	String commonOptions = " -d \"" + OUTPUT_DIR + File.separator + "bin\""
		+ " -sourcepath \"" + OUTPUT_DIR + File.separator + "src1\""
		+ " -classpath \"" + OUTPUT_DIR + File.separator + "src2\" ";
	String sourceFilePath = "\"" + OUTPUT_DIR + File.separator + "src3" + File.separator + "Z.java\"";
	// ecj compiles src1 and src2 source files as needed, regardless of their
	// being on the sourcepath or the classpath
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"src1/X.java",
			"public class X {\n" +
			"}\n",
			"src2/Y.java",
			"public class Y {\n" +
			"}\n",
			"src3/Z.java",
			"""
				public class Z {
				  X x;
				  Y y;
				}
				""",
		},
		sourceFilePath + " -proc:none " + commonOptions /* commandLine */,
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* progress */);
	if (RUN_JAVAC) {
		// in contrast with test#232 when src1 is on the classpath, javac fails
		// to find src1/X.java; this is because -sourcepath inhibits source files
		// search in classpath directories
		Iterator javacCompilersIterator = javacCompilers.iterator();
		String sourceFileNames[] = new String[] {sourceFilePath};
		File outputDir = new File(OUTPUT_DIR);
		while (javacCompilersIterator.hasNext()) {
			JavacCompiler javacCompiler = (JavacCompiler) javacCompilersIterator.next();
			assertFalse(javacCompiler.compile(
					outputDir, /* directory */
					commonOptions /* options */,
					sourceFileNames /* source file names */,
					null /* log */) == 0);
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// different from javac: with javac, newer class file down the classpath wins
// over source file upstream, while ecj selects the first source or binary found
// in classpath order (no sourcepath involved here)
public void test235_classpath() throws IOException, InterruptedException {
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"src1/X.java",
			"""
				public class X {
				  public static final int CONST = 1;
				}
				""",
			"src2/X.java",
			"public class X {\n" +
			"}\n",
		},
		"\"" + OUTPUT_DIR + File.separator + "src1" + File.separator + "X.java\"" /* commandLine */
		+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* progress */);
	// ensure that class file is newer than source file (some file systems
	// store the modification time at a second precision)
	File sourceFile = new File(OUTPUT_DIR + File.separator + "src2" + File.separator + "X.java"),
	  classFile = new File(OUTPUT_DIR + File.separator + "bin1" + File.separator + "X.class");
	while (classFile.lastModified() <= sourceFile.lastModified()) {
		runTest(
			true /* shouldCompileOK*/,
			null /* testFiles */,
			"\"" + OUTPUT_DIR +  File.separator + "src1" + File.separator + "X.java\"" /* commandLine */
			+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
			"" /* expectedOutOutputString */,
			"" /* expectedErrOutputString */,
			false /* shouldFlushOutputDirectory */,
			null /* progress */);
	}
	// compile with (buggy) src2 before (correct) bin1 in the classpath
	String sourceFilePath =
		"\"" + OUTPUT_DIR + File.separator + "Y.java\"";
	String commonOptions =
		" -classpath \"" + OUTPUT_DIR + File.separator + "src2"
		+ File.pathSeparator + OUTPUT_DIR + File.separator + "bin1\""
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin2" + "\"";
	runTest(
		false /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"Y.java",
			"""
				public class Y {
				  public static void main (String[] args) {
				    System.out.println(X.CONST);
				  }
				}
				""",
		},
		sourceFilePath /* commandLine */
		+ " -proc:none " + commonOptions,
		"" /* expectedOutOutputString */,
		"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/Y.java (at line 3)
				System.out.println(X.CONST);
				                     ^^^^^
			CONST cannot be resolved or is not a field
			----------
			1 problem (1 error)
			""",
		false /* shouldFlushOutputDirectory */,
		null /* progress */);
	// javac passes, using the most recent file amongst source and class files
	// present on the classpath
	if (RUN_JAVAC) {
		Iterator javacCompilersIterator = javacCompilers.iterator();
		String sourceFileNames[] = new String[] {sourceFilePath};
		File outputDir = new File(OUTPUT_DIR);
		while (javacCompilersIterator.hasNext()) {
			JavacCompiler javacCompiler = (JavacCompiler) javacCompilersIterator.next();
			assertTrue(javacCompiler.compile(
					outputDir /* directory */,
					commonOptions /* options */,
					sourceFileNames /* source file names */,
					null /* log */) == 0);
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// when class files are ready in all classpath entries, ecj and javac pick
// the first available class file up, regardless of which is newer  (no sourcepath here)
public void test236_classpath() throws IOException, InterruptedException {
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"src1/X.java",
			"""
				public class X {
				  public static final int CONST = 1;
				}
				""",
		},
		"\"" + OUTPUT_DIR + File.separator + "src1" + File.separator + "X.java\"" /* commandLine */
		+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* progress */);
	File bin1File = new File(OUTPUT_DIR + File.separator + "bin1" + File.separator + "X.class"),
	  bin2File = new File(OUTPUT_DIR + File.separator + "bin2" + File.separator + "X.class");
	do {
		runTest(
			true /* shouldCompileOK*/,
			new String[] { /* testFiles */
				"src2/X.java",
				"""
					public class X {
					  public static final int CONST = 2;
					}
					""",
			},
			"\"" + OUTPUT_DIR + File.separator + "src2" + File.separator + "X.java\"" /* commandLine */
			+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin2" + "\"",
			"" /* expectedOutOutputString */,
			"" /* expectedErrOutputString */,
			false /* shouldFlushOutputDirectory */,
			null /* progress */);
	} while (bin2File.lastModified() <= bin1File.lastModified());
	String sourceFilePath = "\"" + OUTPUT_DIR +  File.separator + "Y.java\"";
	String commonOptions =
		" -classpath \"" + OUTPUT_DIR + File.separator + "bin1" + File.pathSeparator
		+ OUTPUT_DIR + File.separator + "bin2" + "\""
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin" + "\"";
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"Y.java",
			"""
				public class Y {
				  public static void main (String[] args) {
				    System.out.println(X.CONST);
				  }
				}
				""",
		},
		sourceFilePath + commonOptions	+ " -proc:none " /* commandLine */,
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		false /* shouldFlushOutputDirectory */,
		null /* progress */);
	this.verifier.execute("Y", new String[] {OUTPUT_DIR + File.separator + "bin"});
	assertTrue(this.verifier.getExecutionOutput().startsWith("1")); // skip trailing newline
	if (RUN_JAVAC) {
		Iterator javacCompilersIterator = javacCompilers.iterator();
		String sourceFileNames[] = new String[] {sourceFilePath};
		File outputDir = new File(OUTPUT_DIR);
		while (javacCompilersIterator.hasNext()) {
			JavacCompiler javacCompiler = (JavacCompiler) javacCompilersIterator.next();
			assertTrue(javacCompiler.compile(
					outputDir /* directory */,
					commonOptions /* options */,
					sourceFileNames,
					null /* log */) == 0);
			this.verifier.execute("Y", new String[] {OUTPUT_DIR + File.separator + "bin"});
			assertEquals('1', this.verifier.getExecutionOutput().charAt(0)); // skip trailing newline
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// when a source file is more recent than a class file in a former
// classpath entry, ecj picks the class file up, while javac choses the
// source file (no sourcepath here)
public void test237_classpath() throws IOException, InterruptedException {
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"src1/X.java",
			"""
				public class X {
				  public static final int CONST = 1;
				}
				""",
		},
		"\"" + OUTPUT_DIR + File.separator + "src1" + File.separator + "X.java\"" /* commandLine */
		+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* progress */);
	File sourceFile = new File(OUTPUT_DIR + File.separator + "src2" + File.separator + "X.java"),
	  classFile = new File(OUTPUT_DIR + File.separator + "bin1" + File.separator + "X.class");
	new File(OUTPUT_DIR + File.separator + "src2").mkdirs();
	do {
		Util.writeToFile(
			"""
				public class X {
				  public static final int CONST = 2;
				}
				""",
			sourceFile.getPath());
	} while (classFile.lastModified() >= sourceFile.lastModified());
	String sourceFilePath = "\"" + OUTPUT_DIR +  File.separator + "Y.java\"";
	String commonOptions =
		" -classpath \"" + OUTPUT_DIR + File.separator + "bin1" + File.pathSeparator
		+ OUTPUT_DIR + File.separator + "src2" + "\""
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin" + "\"";
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"Y.java",
			"""
				public class Y {
				  public static void main (String[] args) {
				    System.out.println(X.CONST);
				  }
				}
				""",
		},
		sourceFilePath + commonOptions	+ " -proc:none " /* commandLine */,
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		false /* shouldFlushOutputDirectory */,
		null /* progress */);
	this.verifier.execute("Y", new String[] {OUTPUT_DIR + File.separator + "bin"});
	assertTrue(this.verifier.getExecutionOutput().startsWith("1")); // skip trailing newline
	if (RUN_JAVAC) {
		Iterator javacCompilersIterator = javacCompilers.iterator();
		String sourceFileNames[] = new String[] {sourceFilePath};
		File outputDir = new File(OUTPUT_DIR);
		while (javacCompilersIterator.hasNext()) {
			JavacCompiler javacCompiler = (JavacCompiler) javacCompilersIterator.next();
			assertTrue(javacCompiler.compile(
					outputDir /* directory */,
					commonOptions /* options */,
					sourceFileNames /* source file names */,
					null /* log */) == 0);
			this.verifier.execute("Y", new String[] {OUTPUT_DIR + File.separator + "bin"});
			assertEquals('2', this.verifier.getExecutionOutput().charAt(0)); // skip trailing newline
			// 2 means javac selected src2 (because the source file was more recent than bin1/X.class)
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// when a source file is more recent than another source file in a former
// classpath entry, ecj and javac pick the latter file up (in other words, if
// only source files are involved, the classpath entries order prevails - no sourcepath here)
public void test238_classpath() throws IOException, InterruptedException {
	new File(OUTPUT_DIR + File.separator + "src1").mkdirs();
	File sourceFile1 = new File(OUTPUT_DIR + File.separator + "src1" + File.separator + "X.java");
	File sourceFile2 = new File(OUTPUT_DIR + File.separator + "src2" + File.separator + "X.java");
	Util.writeToFile(
		"""
			public class X {
			  public static final int CONST = 1;
			}
			""",
		sourceFile1.getPath());
	new File(OUTPUT_DIR + File.separator + "src2").mkdirs();
	do {
		Util.writeToFile(
			"""
				public class X {
				  public static final int CONST = 2;
				}
				""",
			sourceFile2.getPath());
	} while (sourceFile1.lastModified() >= sourceFile2.lastModified());
	String sourceFilePath = "\"" + OUTPUT_DIR +  File.separator + "Y.java\"";
	String commonOptions =
		" -classpath \"" + OUTPUT_DIR + File.separator + "src1" + File.pathSeparator
		+ OUTPUT_DIR + File.separator + "src2" + "\""
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin" + "\"";
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"Y.java",
			"""
				public class Y {
				  public static void main (String[] args) {
				    System.out.println(X.CONST);
				  }
				}
				""",
		},
		sourceFilePath + commonOptions	+ " -proc:none " /* commandLine */,
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		false /* shouldFlushOutputDirectory */,
		null /* progress */);
	this.verifier.execute("Y", new String[] {OUTPUT_DIR + File.separator + "bin"});
	assertTrue(this.verifier.getExecutionOutput().startsWith("1")); // skip trailing newline
	if (RUN_JAVAC) {
		Iterator javacCompilersIterator = javacCompilers.iterator();
		String sourceFileNames[] = new String[] {sourceFilePath};
		File outputDir = new File(OUTPUT_DIR);
		while (javacCompilersIterator.hasNext()) {
			JavacCompiler javacCompiler = (JavacCompiler) javacCompilersIterator.next();
			assertTrue(javacCompiler.compile(
					outputDir /* directory */,
					commonOptions /* options */,
					sourceFileNames /* source file names */,
					null /* log */) == 0);
			this.verifier.execute("Y", new String[] {OUTPUT_DIR + File.separator + "bin"});
			assertEquals('1', this.verifier.getExecutionOutput().charAt(0)); // skip trailing newline
			// 1 means javac selected src1 (because src1/X.java comes ahead of src2/X.java on the classpath)
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// basic link: a jar only referenced in the manifest of the first one is found
public void test239_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				  B b;
				}""",
		},
     "\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib1.jar\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
     + " -1.5 -g -preserveAllLocals"
     + " -proceedOnError -referenceInfo"
     + " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
     "",
     "",
     true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// links are followed recursively, eliminating dupes
public void test240_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				  B b;
				  C c;
				  D d;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib3.jar\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// at first level, this is depth first, masking tailing libs
public void test241_jar_ref_in_jar(){
	createCascadedJars();
	this.runNegativeTest(
			new String[] {
					"src/p/X.java",
					"""
						package p;
						/** */
						public class X {
						  int i = R.R2;
						  int j = R.R3;
						}""",
			},
			"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib1.jar\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib3.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
			+ " -1.5 -g -preserveAllLocals"
			+ " -proceedOnError -referenceInfo"
			+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
			"",
			"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 5)
					int j = R.R3;
					          ^^
				R3 cannot be resolved or is not a field
				----------
				1 problem (1 error)
				""",
			true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// using only links, we adopt a depth first algorithm
public void test242_jar_ref_in_jar(){
	createCascadedJars();
	this.runNegativeTest(
			new String[] {
					"src/p/X.java",
					"""
						package p;
						/** */
						public class X {
						  int i = R.R2;
						  int j = R.R3;
						}""",
			},
			"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib4.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
			+ " -1.5 -g -preserveAllLocals"
			+ " -proceedOnError -referenceInfo"
			+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
			"",
			"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 5)
					int j = R.R3;
					          ^^
				R3 cannot be resolved or is not a field
				----------
				1 problem (1 error)
				""",
			true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// managing subdirectories and .. properly
public void test243_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				  B b;
				  C c;
				  D d;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib5.jar\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// variant: the second jar on a line is found as well
public void test244_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  C c;
				}""",
		},
  "\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib4.jar\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
  + " -1.5 -g -preserveAllLocals"
  + " -proceedOnError -referenceInfo"
  + " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
  "",
  "",
  true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// we eat up absolute links silently
public void test245_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  F f;
				}""",
		},
	"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib8.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
	+ " -1.5 -g -preserveAllLocals"
	+ " -proceedOnError -referenceInfo"
	+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
	"",
	"",
	true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// absolute links do not mask following relative links
public void test246_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				  F f;
				}""",
		},
	"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib8.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
	+ " -1.5 -g -preserveAllLocals"
	+ " -proceedOnError -referenceInfo"
	+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
	"",
	"",
	true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// absolute links are not followed
public void test247_jar_ref_in_jar(){
	createCascadedJars();
	this.runNegativeTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  C c;
				}""",
		},
	"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib8.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
	+ " -1.5 -g -preserveAllLocals"
	+ " -proceedOnError -referenceInfo"
	+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
	"",
	"""
		----------
		1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)
			C c;
			^
		C cannot be resolved to a type
		----------
		1 problem (1 error)
		""",
	true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// we accept duplicate classpath lines in manifest and we follow the jars of the
// second and following lines as well as the first line (emit a warning as javac does)
public void test248_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				  G g;
				}""",
		},
	"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib9.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
	+ " -1.5 -g -preserveAllLocals"
	+ " -proceedOnError -referenceInfo"
	+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
	"",
	"multiple Class-Path headers in manifest of jar file: ---LIB_DIR_PLACEHOLDER---/lib9.jar\n",
	true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// we accept duplicate classpath lines in manifest and we follow the jars of the
// second and following lines as well as the first line (emit a warning as javac does)
public void test249_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  C c;
				  G g;
				}""",
		},
	"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
			+ " -cp \"" + LIB_DIR + File.separator + "lib9.jar\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
	+ " -1.5 -g -preserveAllLocals"
	+ " -proceedOnError -referenceInfo"
	+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
	"",
	"multiple Class-Path headers in manifest of jar file: ---LIB_DIR_PLACEHOLDER---/lib9.jar\n",
	true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// bootclasspath does not get expanded with linked files
public void test250_jar_ref_in_jar(){
	createCascadedJars();
	this.runNegativeTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				  B b;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
	  	+ " -bootclasspath " + getLibraryClassesAsQuotedString()
	  	+ File.pathSeparator + "\"" + LIB_DIR + File.separator + "lib1.jar\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 5)
				B b;
				^
			B cannot be resolved to a type
			----------
			1 problem (1 error)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// jar files reached indirectly bear the access rules of the entry that
// references them
public void test251_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib3.jar[~p/A]\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)
				A a;
				^
			Discouraged access: The type \'A\' is not API (restriction on classpath entry \'---LIB_DIR_PLACEHOLDER---/lib3.jar\')
			----------
			1 problem (1 warning)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=217233
// compiler progress test (1 unit)
public void test252_progress() {
	runProgressTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}\n",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -d \"" + OUTPUT_DIR + "\"",
		""/*out output*/,
		""/*err output*/,
		"""
			----------
			[worked: 0 - remaining: 1]
			Beginning to compile
			Processing ---OUTPUT_DIR_PLACEHOLDER---/X.java
			[worked: 1 - remaining: 0]
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=217233
// compiler progress test (2 units)
public void test253_progress() {
	runProgressTest(
		new String[] {
			"Y.java",
			"public class Y {\n" +
			"}\n",
			"X.java",
			"public class X extends Y {\n" +
			"}\n",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -cp " + File.pathSeparator + File.pathSeparator + "\"" + OUTPUT_DIR + "\""
		+ " -d \"" + OUTPUT_DIR + "\"",
		""/*out output*/,
		""/*err output*/,
		"""
			----------
			[worked: 0 - remaining: 1]
			Beginning to compile
			Processing ---OUTPUT_DIR_PLACEHOLDER---/X.java
			[worked: 1 - remaining: 1]
			Processing ---OUTPUT_DIR_PLACEHOLDER---/Y.java
			[worked: 2 - remaining: 0]
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=217233
// compiler progress test (multiple iterations)
public void test254_progress() {
	runProgressTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"}\n",
			"Y.java",
			"public class Y {\n" +
			"}\n",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " \"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -cp " + File.pathSeparator + File.pathSeparator + "\"" + OUTPUT_DIR + "\""
		+ " -d \"" + OUTPUT_DIR + "\""
		+ " -repeat 3",
		"""
			[repetition 1/3]
			[repetition 2/3]
			[repetition 3/3]
			""",
		""/*err output*/,
		"""
			----------
			[worked: 0 - remaining: 6]
			Beginning to compile
			Processing ---OUTPUT_DIR_PLACEHOLDER---/X.java
			[worked: 1 - remaining: 5]
			Processing ---OUTPUT_DIR_PLACEHOLDER---/Y.java
			[worked: 2 - remaining: 4]
			Beginning to compile
			Processing ---OUTPUT_DIR_PLACEHOLDER---/X.java
			[worked: 3 - remaining: 3]
			Processing ---OUTPUT_DIR_PLACEHOLDER---/Y.java
			[worked: 4 - remaining: 2]
			Beginning to compile
			Processing ---OUTPUT_DIR_PLACEHOLDER---/X.java
			[worked: 5 - remaining: 1]
			Processing ---OUTPUT_DIR_PLACEHOLDER---/Y.java
			[worked: 6 - remaining: 0]
			----------
			"""
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=217233
// compiler progress test (cancellation)
public void test255_progress() {
	TestCompilationProgress progress = new TestCompilationProgress() {
		@Override
		public void worked(int workIncrement, int remainingWork) {
			if (remainingWork == 1)
				this.isCanceled = true;
			super.worked(workIncrement, remainingWork);
		}
	};
	String setting= System.getProperty("jdt.compiler.useSingleThread");
	try {
		System.setProperty("jdt.compiler.useSingleThread", "true");
		runProgressTest(
			false/*shouldCompileOK*/,
			new String[] {
				"Y.java",
				"public class Y {\n" +
				"}\n",
				"X.java",
				"public class X extends Y {\n" +
				"}\n",
			},
			"\"" + OUTPUT_DIR +  File.separator + "X.java\""
	        + " -cp " + File.pathSeparator + File.pathSeparator + "\"" + OUTPUT_DIR + "\""
			+ " -d \"" + OUTPUT_DIR + "\"",
			""/*out output*/,
			""/*err output*/,
			progress,
			"""
				----------
				[worked: 0 - remaining: 1]
				Beginning to compile
				Processing ---OUTPUT_DIR_PLACEHOLDER---/X.java
				[worked: 1 - remaining: 1]
				----------
				"""
		);
	} finally {
		System.setProperty("jdt.compiler.useSingleThread", setting == null ? "false" : setting);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// jar files reached indirectly bear the access rules of the entry that
// references them - this hides the access rules of further instances of the
// same jar on the classpath
public void test256_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib3.jar[~p/A]\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib1.jar[-p/A]\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)
				A a;
				^
			Discouraged access: The type \'A\' is not API (restriction on classpath entry \'---LIB_DIR_PLACEHOLDER---/lib3.jar\')
			----------
			1 problem (1 warning)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// jar files reached indirectly bear the access rules of the entry that
// references them - this hides the access rules of further instances of the
// same jar on the classpath
public void test257_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib3.jar[-DUMMY]\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib1.jar[-p/A]\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// jar files reached indirectly bear the access rules of the entry that
// references them - this hides the access rules of further instances of the
// same jar on the classpath, to the point of absorbing it if none is specified
public void test258_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib3.jar\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib1.jar[-p/A]\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// -sourcepath is OK at first level
public void test259_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  S1 s;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -sourcepath \"" + LIB_DIR + File.separator + "lib1.jar\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// -sourcepath is KO at second level (that is, it does not leverage the links
// at all)
public void test260_jar_ref_in_jar(){
	createCascadedJars();
	this.runNegativeTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  S2 s;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -sourcepath \"" + LIB_DIR + File.separator + "lib1.jar\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)
				S2 s;
				^^
			S2 cannot be resolved to a type
			----------
			1 problem (1 error)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// error case: the MANIFEST.MF is a directory; should fail gracefully
public void test261_jar_ref_in_jar(){
	createCascadedJars();
	this.runNegativeTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				  B b;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
				+ " -cp \"" + LIB_DIR + File.separator + "lib10.jar\""
				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 5)
				B b;
				^
			B cannot be resolved to a type
			----------
			1 problem (1 error)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// using relative paths for libs
public void test262_jar_ref_in_jar(){
	String currentWorkingDirectoryPath = System.getProperty("user.dir");
	if (currentWorkingDirectoryPath == null) {
		System.err.println("BatchCompilerTest#235 could not access the current working directory " + currentWorkingDirectoryPath);
	} else if (!new File(currentWorkingDirectoryPath).isDirectory()) {
		System.err.println("BatchCompilerTest#235 current working directory is not a directory " + currentWorkingDirectoryPath);
	} else {
		String lib1Path = currentWorkingDirectoryPath + File.separator + "lib1.jar";
		String lib2Path = currentWorkingDirectoryPath + File.separator + "lib2.jar";
		try {
			Util.createJar(
				null,
				new String[] {
					"META-INF/MANIFEST.MF",
					"""
						Manifest-Version: 1.0
						Created-By: Eclipse JDT Test Harness
						Class-Path: lib2.jar
						""",
				},
				lib1Path,
				JavaCore.VERSION_1_4);
			Util.createJar(
				new String[] {
					"p/A.java",
					"""
						package p;
						public class A {
						}""",
				},
				null,
				lib2Path,
				JavaCore.VERSION_1_4);
			this.runConformTest(
				new String[] {
					"src/p/X.java",
					"""
						package p;
						/** */
						public class X {
						  A a;
						}""",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
				+ " -cp lib1.jar" // relative
				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		        + " -1.5 -g -preserveAllLocals"
		        + " -proceedOnError -referenceInfo"
		        + " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		        "",
		        "",
		        true);
		} catch (IOException e) {
			System.err.println("BatchCompilerTest#235 could not write to current working directory " + currentWorkingDirectoryPath);
		} finally {
			new File(lib1Path).delete();
			new File(lib2Path).delete();
		}
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// empty Class-Path header
// javac 1.4.2 passes, later versions fail in error
// java accepts the same jar (which makes the compiler responsible for the
// error detection)
// design: will issue a warning
public void test263_jar_ref_in_jar(){
	createCascadedJars();
	this.runTest(
		true,
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -classpath \"" + LIB_DIR + File.separator + "lib11.jar\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -verbose -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		ONE_FILE_GENERATED_MATCHER,
		new StringMatcher(
			"invalid Class-Path header in manifest of jar file: ---LIB_DIR_PLACEHOLDER---/lib11.jar\n",
			outputDirNormalizer),
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// missing space after ClassPath:
public void test264_jar_ref_in_jar(){
	createCascadedJars();
	this.runTest(
		false,
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -classpath \"" + LIB_DIR + File.separator + "lib12.jar\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"""
			invalid Class-Path header in manifest of jar file: ---LIB_DIR_PLACEHOLDER---/lib12.jar
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)
				A a;
				^
			A cannot be resolved to a type
			----------
			1 problem (1 error)
			""",
		true,
		null /* progress */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// missing space after ClassPath
// javac reports an error (including an explicit manifest header error since
// version 1.5); moreover, it stops interpreting the said header
// design: we report a warning and eat up the remainding of the line
public void test265_jar_ref_in_jar(){
	createCascadedJars();
	this.runTest(
		false,
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -classpath \"" + LIB_DIR + File.separator + "lib13.jar\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"""
			invalid Class-Path header in manifest of jar file: ---LIB_DIR_PLACEHOLDER---/lib13.jar
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)
				A a;
				^
			A cannot be resolved to a type
			----------
			1 problem (1 error)
			""",
		true,
		null /* progress */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// extra space before Class-Path header
// the net result is that the line is part of the value of the previous header
// we then simply don't see the remainding of the line as jars
public void test266_jar_ref_in_jar(){
	createCascadedJars();
	this.runTest(
		false,
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -classpath \"" + LIB_DIR + File.separator + "lib14.jar\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)
				A a;
				^
			A cannot be resolved to a type
			----------
			1 problem (1 error)
			""",
		true,
		null /* progress */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// missing newline at the end of the line
// javac eats the line silently, which results into not finding A
// design: we report a warning and eat up the remainding of the line
public void test267_jar_ref_in_jar(){
	createCascadedJars();
	this.runTest(
		false,
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -classpath \"" + LIB_DIR + File.separator + "lib15.jar\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"""
			invalid Class-Path header in manifest of jar file: ---LIB_DIR_PLACEHOLDER---/lib15.jar
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)
				A a;
				^
			A cannot be resolved to a type
			----------
			1 problem (1 error)
			""",
		true,
		null /* progress */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// white-box test for duplicate classpath lines variant (empty line between the
// entries)
public void test268_jar_ref_in_jar(){
	try {
		ManifestAnalyzer analyzer = new ManifestAnalyzer();
		assertTrue(analyzeManifestContents(
			analyzer,
			"""
				Manifest-Version: 1.0
				Created-By: Eclipse JDT Test Harness
				Class-Path: lib1.jar
				
				Class-Path: lib3.jar
				"""));
		assertEquals(2, analyzer.getClasspathSectionsCount());
		assertEquals(2, analyzer.getCalledFileNames().size());
	} catch (IOException e) {
		e.printStackTrace();
		fail();
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// white-box test for duplicate classpath lines variant (other header between the
// entries - note that since we are not doing a full-fledged manifest analysis,
// a dummy header passes)
public void test269_jar_ref_in_jar(){
	try {
		ManifestAnalyzer analyzer = new ManifestAnalyzer();
		assertTrue(analyzeManifestContents(
			analyzer,
			"""
				Manifest-Version: 1.0
				Created-By: Eclipse JDT Test Harness
				Class-Path: lib1.jar
				Dummy:
				Class-Path: lib3.jar
				"""));
		assertEquals(2, analyzer.getClasspathSectionsCount());
		assertEquals(2, analyzer.getCalledFileNames().size());
	} catch (IOException e) {
		e.printStackTrace();
		fail();
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// white-box test: tabs are not seen as URI separator, but as parts of URI instead
// will trigger downstream errors if the jars are really needed
public void test270_jar_ref_in_jar(){
	try {
		ManifestAnalyzer analyzer = new ManifestAnalyzer();
		assertTrue(analyzeManifestContents(
			analyzer,
			"""
				Manifest-Version: 1.0
				Created-By: Eclipse JDT Test Harness
				Class-Path: lib1.jar	lib2.jar
				"""));
		assertEquals(1, analyzer.getClasspathSectionsCount());
		assertEquals(1, analyzer.getCalledFileNames().size());
	} catch (IOException e) {
		e.printStackTrace();
		fail();
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// managing continuations properly
public void test271_jar_ref_in_jar(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				  B b;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
				+ " -cp \"" + LIB_DIR + File.separator + "lib16.jar\""
				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// white-box test: variants on continuations
public void test272_jar_ref_in_jar(){
	try {
		ManifestAnalyzer analyzer = new ManifestAnalyzer();
		assertTrue(analyzeManifestContents(
			analyzer,
			"""
				Manifest-Version: 1.0
				Created-By: Eclipse JDT Test Harness
				Class-Path:\s
				            lib1.jar      \s
				
				"""));
		assertEquals(1, analyzer.getClasspathSectionsCount());
		assertEquals(1, analyzer.getCalledFileNames().size());
	} catch (IOException e) {
		e.printStackTrace();
		fail();
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// white-box test: variants on continuations
public void test273_jar_ref_in_jar(){
	try {
		ManifestAnalyzer analyzer = new ManifestAnalyzer();
		assertTrue(analyzeManifestContents(
			analyzer,
			"""
				Manifest-Version: 1.0
				Created-By: Eclipse JDT Test Harness
				Class-Path:\s
				\s
				            lib1.jar      \s
				\s
				            lib1.jar      \s
				
				"""));
		assertEquals(1, analyzer.getClasspathSectionsCount());
		assertEquals(2, analyzer.getCalledFileNames().size());
	} catch (IOException e) {
		e.printStackTrace();
		fail();
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// white-box test: variants on continuations
public void test274_jar_ref_in_jar(){
	try {
		ManifestAnalyzer analyzer = new ManifestAnalyzer();
		assertFalse(analyzeManifestContents(
			analyzer,
			"""
				Manifest-Version: 1.0
				Created-By: Eclipse JDT Test Harness
				Class-Path:\s
				            lib1.jar"""));
	} catch (IOException e) {
		e.printStackTrace();
		fail();
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// white-box test: variants on continuations
public void test275_jar_ref_in_jar(){
	try {
		assertFalse(analyzeManifestContents(
			new ManifestAnalyzer(),
			"""
				Manifest-Version: 1.0
				Created-By: Eclipse JDT Test Harness
				Class-Path:\s
				\s
				            lib1.jar"""));
	} catch (IOException e) {
		e.printStackTrace();
		fail();
	}
}
private boolean analyzeManifestContents(ManifestAnalyzer manifestAnalyzer,
		String string) throws IOException {
	try (InputStream stream = new ByteArrayInputStream(string.getBytes())) {
		return manifestAnalyzer.analyzeManifestContents(stream);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// white-box test: variants on continuations
public void test276_jar_ref_in_jar(){
	try {
		assertFalse(analyzeManifestContents(
			new ManifestAnalyzer(),
			"""
				Manifest-Version: 1.0
				Created-By: Eclipse JDT Test Harness
				Class-Path:     \s
				lib1.jar"""));
	} catch (IOException e) {
		e.printStackTrace();
		fail();
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// extdirs jars do not follow links
public void test277_jar_ref_in_jar(){
	createCascadedJars();
	this.runNegativeTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				  B b;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
	  	+ " -extdirs \"" + LIB_DIR + File.separator + "dir\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 5)
				B b;
				^
			B cannot be resolved to a type
			----------
			1 problem (1 error)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97332 - jars pointed by jars
// endorseddirs does not get expanded with linked files
public void test278_jar_ref_in_jar(){
	createCascadedJars();
	this.runNegativeTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				  B b;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
	  	+ " -endorseddirs \"" + LIB_DIR + File.separator + "dir\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 5)
				B b;
				^
			B cannot be resolved to a type
			----------
			1 problem (1 error)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// looking at access rules: ignore if better makes the class file selected if
// it is newer, but see test#280 for what happens when it is not
public void test279_sourcepath_vs_classpath() throws IOException, InterruptedException {
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"src1/X.java",
			"""
				public class X {
				  public static final int CONST = 1;
				}
				""",
			"src2/X.java",
			"""
				public class X {
				  public static final int CONST = 2;
				}
				""",
		},
		"\"" + OUTPUT_DIR + File.separator + "src1" + File.separator + "X.java\"" /* commandLine */
		+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* progress */);
	// ensure that bin1/X.class file is newer than src2/X.java (some file systems
	// store the modification time at a second precision)
	File sourceFile = new File(OUTPUT_DIR + File.separator + "src2" + File.separator + "X.java"),
	  classFile = new File(OUTPUT_DIR + File.separator + "bin1" + File.separator + "X.class");
	while (classFile.lastModified() <= sourceFile.lastModified()) {
		runConformTest(
			null,
			"\"" + OUTPUT_DIR +  File.separator + "src1" + File.separator + "X.java\""
			+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
			"",
			"",
			false);
	}
	// the ignore if better rule upon src2 leads to bin1 being selected
	runConformTest(
		new String[] {
			"Y.java",
			"""
				public class Y {
				  public static void main (String[] args) {
				    System.out.println(X.CONST);
				  }
				}
				""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "Y.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + File.separator + "src2[?**/*]" + "\""
		+ " -classpath \"" + OUTPUT_DIR + File.separator + "bin1" + "\""
		+ " -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
	this.verifier.execute("Y", new String[] {OUTPUT_DIR });
	assertTrue(this.verifier.getExecutionOutput().startsWith("1")); // skip trailing newline
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=216684
// looking at access rules: ignore if better makes the class file selected even
// if it is older (in test#279 it was newer); access rules are thus no work
// around since they ignore modification dates
public void test280_sourcepath_vs_classpath() throws IOException, InterruptedException {
	runTest(
		true /* shouldCompileOK*/,
		new String[] { /* testFiles */
			"src1/X.java",
			"""
				public class X {
				  public static final int CONST = 1;
				}
				""",
		},
		"\"" + OUTPUT_DIR + File.separator + "src1" + File.separator + "X.java\"" /* commandLine */
		+ " -proc:none -d \"" + OUTPUT_DIR + File.separator + "bin1" + "\"",
		"" /* expectedOutOutputString */,
		"" /* expectedErrOutputString */,
		true /* shouldFlushOutputDirectory */,
		null /* progress */);
	// ensure that bin1/X.class file is older than src2/X.java (some file systems
	// store the modification time at a second precision)
	File sourceFile = new File(OUTPUT_DIR + File.separator + "src2" + File.separator + "X.java"),
	  classFile = new File(OUTPUT_DIR + File.separator + "bin1" + File.separator + "X.class");
	new File(OUTPUT_DIR + File.separator + "src2").mkdirs();
	do {
		Util.writeToFile(
			"""
				public class X {
				  public static final int CONST = 2;
				}
				""",
			sourceFile.getPath());
	} while (classFile.lastModified() >= sourceFile.lastModified());
	// the ignore if better rule upon src2 leads to bin1 being selected even if
	// src2/X.java is newer
	runConformTest(
		new String[] {
			"Y.java",
			"""
				public class Y {
				  public static void main (String[] args) {
				    System.out.println(X.CONST);
				  }
				}
				""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "Y.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + File.separator + "src2[?**/*]" + "\""
		+ " -classpath \"" + OUTPUT_DIR + File.separator + "bin1" + "\""
		+ " -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false);
	this.verifier.execute("Y", new String[] {OUTPUT_DIR });
	assertTrue(this.verifier.getExecutionOutput().startsWith("1")); // skip trailing newline
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=182360
public void test281_classpath() {
	runConformTest(
		new String[] {
			"src1/X.java",
			"public class X {\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "src1/X.java\" -cp Y.java",
        "" /* expectedOutOutputString */,
        "incorrect classpath: Y.java\n",
        false/*shouldFlushOutput*/);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=182360
public void test282_classpath() {
	runConformTest(
		new String[] {
			"src1/X.java",
			"public class X {\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "src1/X.java\" -cp p/Y.java",
        "" /* expectedOutOutputString */,
        "incorrect classpath: p/Y.java\n",
        false/*shouldFlushOutput*/);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=182360
public void test283_classpath() {
	runConformTest(
		new String[] {
			"src1/X.java",
			"public class X {\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "src1/X.java\" -cp Y.class",
        "" /* expectedOutOutputString */,
        "incorrect classpath: Y.class\n",
        false/*shouldFlushOutput*/);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=182360
public void test284_classpath() {
	runConformTest(
		new String[] {
			"src1/X.java",
			"public class X {\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "src1/X.java\" -cp p/Y.class",
        "" /* expectedOutOutputString */,
        "incorrect classpath: p/Y.class\n",
        false/*shouldFlushOutput*/);
}

// command-line expansion
public void test285_option_files() {
	runConformTest(
		new String[] {
			"X.java",
			"public @interface X {\n" +
			"}",
			"options.txt",
			"-source 1.5"
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\" " +
        "\"@" + OUTPUT_DIR +  File.separator + "options.txt\"",
        "" /* expectedOutOutputString */,
        "" /* stderr */,
        true /*shouldFlushOutput*/);
}

// command-line expansion
public void test286_option_files() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public @interface X {\n" +
			"}",
			"options.txt",
			"-source 1.4"
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\" " +
        "\"@" + OUTPUT_DIR +  File.separator + "options.txt\"",
        "" /* expectedOutOutputString */,
        """
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
				public @interface X {
				                  ^
			Syntax error, annotation declarations are only available if source level is 1.5 or greater
			----------
			1 problem (1 error)
			""",
        true /*shouldFlushOutput*/);
}
// command-line expansion
// shows that we don't recurse
public void test287_option_files() {
	runNegativeTest(
		new String[] {
			"X.java",
			"public @interface X {\n" +
			"}",
			"options1.txt",
			"@options2.txt",
			"options2.txt",
			"@options1.txt"
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\" " +
        "\"@" + OUTPUT_DIR +  File.separator + "options1.txt\"",
        "" /* expectedOutOutputString */,
        "Unrecognized option : @options2.txt\n" /* stderr */,
        true /*shouldFlushOutput*/);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=246066
public void test288_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface IX {}
				class BaseX implements IX {}
				public class X extends BaseX implements IX {
				}
				""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:+intfRedundant -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				public class X extends BaseX implements IX {
				                                        ^^
			Redundant superinterface IX for the type X, already defined by BaseX
			----------
			1 problem (1 warning)
			""",
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=246066 - variation
public void test289_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface IX {}
				class BaseX implements IX {}
				public class X extends BaseX implements IX {
				}
				""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:+redundantSuperinterface -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				public class X extends BaseX implements IX {
				                                        ^^
			Redundant superinterface IX for the type X, already defined by BaseX
			----------
			1 problem (1 warning)
			""",
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=246066 - variation
public void test290_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface IX {}
				class BaseX implements IX {}
				public class X extends BaseX implements IX {
				}
				""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:+intfRedundant -warn:-intfRedundant -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=251079
public void test291_jar_ref_in_jar() throws Exception {
	ManifestAnalyzer analyzer = new ManifestAnalyzer();
	assertTrue(analyzeManifestContents(
		analyzer,
		"""
			Manifest-Version: 1.0\r
			Created-By: Eclipse JDT Test Harness\r
			Class-Path: \r
			\r
			"""
		));
	List calledFileNames = analyzer.getCalledFileNames();
	String actual = calledFileNames == null ? "<null>" : Util.toString((String[]) calledFileNames.toArray(new String[calledFileNames.size()]), false/*don't add extra new lines*/);
	assertStringEquals(
		"<null>",
		actual,
		true/*show line serators*/);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=163194
// -warn option - regression tests to check option allOver-ann
public void test292_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				interface A {
				  void m();
				}\
				interface B extends A{
				  void m();
				}\
				public class X implements A{
				  public void m(){}
				  public String toString(){return "HelloWorld";}
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:allOver-ann -1.6 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				void m();
				     ^^^
			The method m() of type B should be tagged with @Override since it actually overrides a superinterface method
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
				public void m(){}
				            ^^^
			The method m() of type X should be tagged with @Override since it actually overrides a superinterface method
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)
				public String toString(){return "HelloWorld";}
				              ^^^^^^^^^^
			The method toString() of type X should be tagged with @Override since it actually overrides a superclass method
			----------
			3 problems (3 warnings)
			""",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325342
// -warn option - regression tests to check option includeAssertNull
// Null problems arising from asserts should be reported here
// since includeAssertNull is enabled
public void test293_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo(Object a, Object b, Object c) {
						assert a == null;
				 \
						if (a!=null) {
							System.out.println("a is not null");
						 } else{
							System.out.println("a is null");
						 }
						a = null;
						if (a== null) {}
						assert b != null;
				 \
						if (b!=null) {
							System.out.println("b is not null");
						 } else{
							System.out.println("b is null");
						 }
						assert c == null;
						if (c.equals(a)) {
							System.out.println("");
						 } else{
							System.out.println("");
						 }
					}
					public static void main(String[] args){
						X test = new X();
						test.foo(null,null, null);
					}
				}
				""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:null,includeAssertNull -1.5 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				if (a!=null) {
				    ^
			Null comparison always yields false: The variable a can only be null at this location
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 9)
				a = null;
				^
			Redundant assignment: The variable a can only be null at this location
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 10)
				if (a== null) {}
				    ^
			Redundant null check: The variable a can only be null at this location
			----------
			4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 12)
				if (b!=null) {
				    ^
			Redundant null check: The variable b cannot be null at this location
			----------
			5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 18)
				if (c.equals(a)) {
				    ^
			Null pointer access: The variable c can only be null at this location
			----------
			5 problems (5 warnings)
			""",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// -warn option - regression test to check option static-method
// Method can be static warning should be given
public void test294_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static int field1;
					public static int field2;
					public void bar(int i) {
						System.out.println(foo());
						foo();\
						System.out.println(X.field1);
						System.out.println(field2);
						field2 = 1;
					}
					private static String foo() {
						return null;
					}
					private void foo1() {
						System.out.println();
					}
					public final void foo2() {
						System.out.println();
					}
				}
				final class A {\
					public void foo() {
						System.out.println();
					}
				}
				""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:static-method -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 13)
				private void foo1() {
				             ^^^^^^
			The method foo1() from the type X can be declared as static
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 16)
				public final void foo2() {
				                  ^^^^^^
			The method foo2() from the type X can be declared as static
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 20)
				final class A {	public void foo() {
				               	            ^^^^^
			The method foo() from the type A can be declared as static
			----------
			3 problems (3 warnings)
			""",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=318682
// -warn option - regression test to check option all-static-method
// Method can be static warning should be given
public void test295_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					public static int field1;
					public static int field2;
					public void bar(int i) {
						System.out.println(foo());
						foo();\
						System.out.println(X.field1);
						System.out.println(field2);
						field2 = 1;
					}
					private static String foo() {
						return null;
					}
					private void foo1() {
						System.out.println();
					}
					public final void foo2() {
						System.out.println();
					}
				}
				final class A {\
					public void foo() {
						System.out.println();
					}
				}
				""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:all-static-method -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				public void bar(int i) {
				            ^^^^^^^^^^
			The method bar(int) from the type X can potentially be declared as static
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 13)
				private void foo1() {
				             ^^^^^^
			The method foo1() from the type X can be declared as static
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 16)
				public final void foo2() {
				                  ^^^^^^
			The method foo2() from the type X can be declared as static
			----------
			4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 20)
				final class A {	public void foo() {
				               	            ^^^^^
			The method foo() from the type A can be declared as static
			----------
			4 problems (4 warnings)
			""",
		true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=280784
public void test293(){
	createCascadedJars();
	this.runNegativeTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib3.jar[~p/A]\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo -err:+discouraged"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)
				A a;
				^
			Discouraged access: The type \'A\' is not API (restriction on classpath entry \'---LIB_DIR_PLACEHOLDER---/lib3.jar\')
			----------
			1 problem (1 error)
			""",
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=280784
public void test294(){
	this.runConformTest(
		new String[] {
			"src/X.java",
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/X.java\""
		+ " -cp \"" + LIB_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo -err:+discouraged2"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"invalid error token: \'discouraged2\'. Ignoring this error token and compiling\n",
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=280784
public void test296(){
	this.runNegativeTest(
		new String[] {
			"src/X.java",
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/X.java\""
		+ " -cp \"" + LIB_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo -err:"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"invalid error configuration: \'-err:\'\n",
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=280784
public void test297(){
	this.runNegativeTest(
		new String[] {
			"src/X.java",
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/X.java\""
		+ " -cp \"" + LIB_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo -err"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"invalid error configuration: \'-err\'\n",
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=295551
public void test298(){
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					@SuppressWarnings("unused")
					private int i;
				}""",
		},
		"\"" + OUTPUT_DIR + File.separator + "X.java\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -err:+unused,suppress -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=295551
public void test299(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					@SuppressWarnings("unused")
					private int i;
				}""",
		},
		"\"" + OUTPUT_DIR + File.separator + "X.java\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -err:+unused -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)
				@SuppressWarnings("unused")
				                  ^^^^^^^^
			Unnecessary @SuppressWarnings("unused")
			----------
			2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private int i;
				            ^
			The value of the field X.i is not used
			----------
			2 problems (1 error, 1 warning)
			""",
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=295551
public void test300(){
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X {
					@SuppressWarnings("unused")
					private int i;
				}""",
		},
		"\"" + OUTPUT_DIR + File.separator + "X.java\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -warn:-suppress -err:+suppress,unused -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=295551
public void test301(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					@SuppressWarnings("unused")
					private int i;
				}""",
		},
		"\"" + OUTPUT_DIR + File.separator + "X.java\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -warn:-suppress -err:+unused -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private int i;
				            ^
			The value of the field X.i is not used
			----------
			1 problem (1 error)
			""",
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=295551
public void test302(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					@SuppressWarnings("unused")
					private int i;
				}""",
		},
		"\"" + OUTPUT_DIR + File.separator + "X.java\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -warn:-suppress -err:+suppress,unused -warn:-suppress -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private int i;
				            ^
			The value of the field X.i is not used
			----------
			1 problem (1 error)
			""",
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=295551
public void test303(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					@SuppressWarnings("unused")
					private int i;
				}""",
		},
		"\"" + OUTPUT_DIR + File.separator + "X.java\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -warn:-suppress -err:+suppress,unused -warn:+suppress -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)
				@SuppressWarnings("unused")
				                  ^^^^^^^^
			Unnecessary @SuppressWarnings("unused")
			----------
			2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private int i;
				            ^
			The value of the field X.i is not used
			----------
			2 problems (1 error, 1 warning)
			""",
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=295551
public void test304(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					@SuppressWarnings("unused")
					private int i;
				}""",
		},
		"\"" + OUTPUT_DIR + File.separator + "X.java\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -err:+suppress,unused -warn:-suppress -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				private int i;
				            ^
			The value of the field X.i is not used
			----------
			1 problem (1 error)
			""",
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=310330
public void test305(){
	this.runConformTest(
		new String[] {
			"src/X.java",
			"public class X {}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/X.java\""
		+ " -encoding UTF-8 -1.5 -g -encoding ISO-8859-1",
		"Found encoding ISO-8859-1. A different encoding was specified: UTF-8\n" +
		"Multiple encoding specified: ISO-8859-1, UTF-8. The default encoding has been set to ISO-8859-1\n",
		"",
		true);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=310330
public void test306(){
	this.runConformTest(
		new String[] {
			"src/X.java",
			"public class X {}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/X.java\""
		+ " -encoding UTF-8 -1.5 -encoding Cp1252 -g -encoding ISO-8859-1",
		"""
			Found encoding Cp1252. A different encoding was specified: UTF-8
			Found encoding ISO-8859-1. Different encodings were specified: Cp1252, UTF-8
			Multiple encoding specified: Cp1252, ISO-8859-1, UTF-8. The default encoding has been set to ISO-8859-1
			""",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321115
public void test0307(){
	String os= System.getProperty("os.name");
    if (!os.startsWith("Windows")) // https://bugs.eclipse.org/bugs/show_bug.cgi?id=323558
    	return;

	final String javaClassspath = System.getProperty("java.class.path");
	final String javaUserDir = System.getProperty("user.dir");
	try {
		System.setProperty("user.dir", OUTPUT_DIR);
		this.runConformTest(
			new String[] {
				"p/Y.java",
				"package p;\n" +
				"public class Y { public class I {}; }",
			},
	        "\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "Y.java\""
	        + " -1.5 -g -preserveAllLocals -proceedOnError -referenceInfo ",
	        "",
	        "",
	        true);
		System.setProperty("java.class.path", "");
		this.runConformTest(
				new String[] {
					"X.java",
					"""
						import p.Y.I;
						public class X {
						   I i;
							public static void main(String[] args) {
								System.out.print("");
							}
						}""",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		        + " -1.5 -g -preserveAllLocals -proceedOnError -referenceInfo ",
		        "",// this is not the runtime output
		        "no classpath defined, using default directory instead\n",
		        false);
		final String userDir = System.getProperty("user.dir");
		File f = new File(userDir, "X.java");
		if (!Util.delete(f)) {
			System.out.println("Could not delete X");
		}
		f = new File(userDir, "p" + File.separator + "Y.java");
		if (!Util.delete(f)) {
			System.out.println("Could not delete Y");
		}

	} finally {
		System.setProperty("java.class.path", javaClassspath);
		System.setProperty("user.dir", javaUserDir);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321115
public void test0307a(){
	String os= System.getProperty("os.name");
    if (!os.startsWith("Windows")) // https://bugs.eclipse.org/bugs/show_bug.cgi?id=323558
    	return;

	final String javaClassspath = System.getProperty("java.class.path");
	final String javaUserDir = System.getProperty("user.dir");
	try {
		System.setProperty("user.dir", OUTPUT_DIR);
		this.runConformTest(
			new String[] {
				"P/Y.java",
				"package P;\n" +
				"public class Y { public class I {}; }",
			},
	        "\"" + OUTPUT_DIR +  File.separator + "P" + File.separator + "Y.java\""
	        + " -1.5 -g -preserveAllLocals -proceedOnError -referenceInfo ",
	        "",
	        "",
	        true);
		System.setProperty("java.class.path", "");
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import p.Y.I;
						public class X {
						   I i;
							public static void main(String[] args) {
								System.out.print("");
							}
						}""",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		        + " -1.5 -g -preserveAllLocals -proceedOnError -referenceInfo ",
		        "",// this is not the runtime output
		        """
					no classpath defined, using default directory instead
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
						import p.Y.I;
						       ^^^
					The import p.Y cannot be resolved
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
						I i;
						^
					I cannot be resolved to a type
					----------
					2 problems (2 errors)
					""",
		        false);
		final String userDir = System.getProperty("user.dir");
		File f = new File(userDir, "X.java");
		if (!Util.delete(f)) {
			System.out.println("Could not delete X");
		}
		f = new File(userDir, "p" + File.separator + "Y.java");
		if (!Util.delete(f)) {
			System.out.println("Could not delete Y");
		}

	} finally {
		System.setProperty("java.class.path", javaClassspath);
		System.setProperty("user.dir", javaUserDir);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321115
public void test0307b(){
	String os= System.getProperty("os.name");
    if (!os.startsWith("Windows")) // https://bugs.eclipse.org/bugs/show_bug.cgi?id=323558
    	return;

	final String javaClassspath = System.getProperty("java.class.path");
	final String javaUserDir = System.getProperty("user.dir");
	try {
		System.setProperty("user.dir", OUTPUT_DIR);
		this.runConformTest(
			new String[] {
				"p/y.java",
				"package p;\n" +
				"public class y { public class I {}; }",
			},
	        "\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "y.java\""
	        + " -1.5 -g -preserveAllLocals -proceedOnError -referenceInfo ",
	        "",
	        "",
	        true);
		System.setProperty("java.class.path", "");
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import p.Y.I;
						public class X {
						   I i;
							public static void main(String[] args) {
								System.out.print("");
							}
						}""",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		        + " -1.5 -g -preserveAllLocals -proceedOnError -referenceInfo ",
		        "",// this is not the runtime output
		        """
					no classpath defined, using default directory instead
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
						import p.Y.I;
						       ^^^
					The import p.Y cannot be resolved
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
						I i;
						^
					I cannot be resolved to a type
					----------
					2 problems (2 errors)
					""",
		        false);
		final String userDir = System.getProperty("user.dir");
		File f = new File(userDir, "X.java");
		if (!Util.delete(f)) {
			System.out.println("Could not delete X");
		}
		f = new File(userDir, "p" + File.separator + "Y.java");
		if (!Util.delete(f)) {
			System.out.println("Could not delete Y");
		}

	} finally {
		System.setProperty("java.class.path", javaClassspath);
		System.setProperty("user.dir", javaUserDir);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321115
public void test0307c(){
	String os= System.getProperty("os.name");
    if (!os.startsWith("Windows")) // https://bugs.eclipse.org/bugs/show_bug.cgi?id=323558
    	return;

	final String javaClassspath = System.getProperty("java.class.path");
	final String javaUserDir = System.getProperty("user.dir");
	try {
		System.setProperty("user.dir", OUTPUT_DIR);
		this.runConformTest(
			new String[] {
				"p/Y.java",
				"package p;\n" +
				"public class Y { public class i {}; }",
			},
	        "\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "Y.java\""
	        + " -1.5 -g -preserveAllLocals -proceedOnError -referenceInfo ",
	        "",
	        "",
	        true);
		System.setProperty("java.class.path", "");
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import p.Y.I;
						public class X {
						   I i;
							public static void main(String[] args) {
								System.out.print("");
							}
						}""",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		        + " -1.5 -g -preserveAllLocals -proceedOnError -referenceInfo ",
		        "",// this is not the runtime output
		        """
					no classpath defined, using default directory instead
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
						import p.Y.I;
						       ^^^^^
					The import p.Y.I cannot be resolved
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
						I i;
						^
					I cannot be resolved to a type
					----------
					2 problems (2 errors)
					""",
		        false);
		final String userDir = System.getProperty("user.dir");
		File f = new File(userDir, "X.java");
		if (!Util.delete(f)) {
			System.out.println("Could not delete X");
		}
		f = new File(userDir, "p" + File.separator + "Y.java");
		if (!Util.delete(f)) {
			System.out.println("Could not delete Y");
		}

	} finally {
		System.setProperty("java.class.path", javaClassspath);
		System.setProperty("user.dir", javaUserDir);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321115
public void test0307d(){
	String os= System.getProperty("os.name");
    if (!os.startsWith("Windows")) // https://bugs.eclipse.org/bugs/show_bug.cgi?id=323558
    	return;

	final String javaClassspath = System.getProperty("java.class.path");
	final String javaUserDir = System.getProperty("user.dir");
	try {
		System.setProperty("user.dir", OUTPUT_DIR);
		this.runConformTest(
			new String[] {
				"p/Y.java",
				"package P;\n" +
				"public class Y { public class I {}; }",
			},
	        "\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "Y.java\""
	        + " -1.5 -g -preserveAllLocals -proceedOnError -referenceInfo ",
	        "",
	        "",
	        true);
		System.setProperty("java.class.path", "");
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import p.Y.I;
						public class X {
						   I i;
							public static void main(String[] args) {
								System.out.print("");
							}
						}""",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		        + " -1.5 -g -preserveAllLocals -proceedOnError -referenceInfo ",
		        "",// this is not the runtime output
		        """
					no classpath defined, using default directory instead
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
						import p.Y.I;
						       ^^^
					The import p.Y cannot be resolved
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
						I i;
						^
					I cannot be resolved to a type
					----------
					2 problems (2 errors)
					""",
		        false);
		final String userDir = System.getProperty("user.dir");
		File f = new File(userDir, "X.java");
		if (!Util.delete(f)) {
			System.out.println("Could not delete X");
		}
		f = new File(userDir, "p" + File.separator + "Y.java");
		if (!Util.delete(f)) {
			System.out.println("Could not delete Y");
		}

	} finally {
		System.setProperty("java.class.path", javaClassspath);
		System.setProperty("user.dir", javaUserDir);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=321115
public void test0307e(){
	String os= System.getProperty("os.name");
    if (!os.startsWith("Windows")) // https://bugs.eclipse.org/bugs/show_bug.cgi?id=323558
    	return;

	final String javaClassspath = System.getProperty("java.class.path");
	final String javaUserDir = System.getProperty("user.dir");
	try {
		System.setProperty("user.dir", OUTPUT_DIR);
		this.runConformTest(
			new String[] {
				"p/Y.java",
				"package P;\n" +
				"public class Y { public class I {}; }",
			},
	        "\"" + OUTPUT_DIR +  File.separator + "P" + File.separator + "Y.java\""
	        + " -1.5 -g -preserveAllLocals -proceedOnError -referenceInfo ",
	        "",
	        "",
	        true);
		System.setProperty("java.class.path", "");
		this.runNegativeTest(
				new String[] {
					"X.java",
					"""
						import p.Y.I;
						public class X {
						   I i;
							public static void main(String[] args) {
								System.out.print("");
							}
						}""",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		        + " -1.5 -g -preserveAllLocals -proceedOnError -referenceInfo ",
		        "",// this is not the runtime output
		        """
					no classpath defined, using default directory instead
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
						import p.Y.I;
						       ^^^
					The import p.Y cannot be resolved
					----------
					2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
						I i;
						^
					I cannot be resolved to a type
					----------
					2 problems (2 errors)
					""",
		        false);
		final String userDir = System.getProperty("user.dir");
		File f = new File(userDir, "X.java");
		if (!Util.delete(f)) {
			System.out.println("Could not delete X");
		}
		f = new File(userDir, "p" + File.separator + "Y.java");
		if (!Util.delete(f)) {
			System.out.println("Could not delete Y");
		}

	} finally {
		System.setProperty("java.class.path", javaClassspath);
		System.setProperty("user.dir", javaUserDir);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328775 - Compiler fails to warn about invalid cast in 1.4 mode.
public void testInferenceIn14Project(){
	String currentWorkingDirectoryPath = System.getProperty("user.dir");
	if (currentWorkingDirectoryPath == null) {
		fail("BatchCompilerTest#testInference14 could not access the current working directory " + currentWorkingDirectoryPath);
	} else if (!new File(currentWorkingDirectoryPath).isDirectory()) {
		fail("BatchCompilerTest#testInference14 current working directory is not a directory " + currentWorkingDirectoryPath);
	}
	String lib1Path = currentWorkingDirectoryPath + File.separator + "lib1.jar";
	try {
		Util.createJar(
				new String[] {
						"Bundle.java",
						"""
							public class Bundle {
							    static <A> A adapt(Class<A> type) {
							        return null;
							    }
							}"""
				},
				null,
				lib1Path,
				JavaCore.VERSION_1_5);
		this.runNegativeTest(
				new String[] {
						"src/X.java",
						"""
							public class X {
							    Bundle b = Bundle.adapt(BundleWiring.class);
							}
							class BundleWiring {}
							""",
				},
				"\"" + OUTPUT_DIR +  File.separator + "src/X.java\""
				+ " -cp lib1.jar" // relative
				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
				+ " -1.4 -g -preserveAllLocals"
				+ " -proceedOnError -referenceInfo"
				+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		        "",
		        """
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/X.java (at line 2)
						Bundle b = Bundle.adapt(BundleWiring.class);
						           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Type mismatch: cannot convert from Object to Bundle
					----------
					1 problem (1 error)
					""",
		        true);
	} catch (IOException e) {
		System.err.println("BatchCompilerTest#testInference14 could not write to current working directory " + currentWorkingDirectoryPath);
	} finally {
		new File(lib1Path).delete();
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328775 - Compiler fails to warn about invalid cast in 1.4 mode.
public void testInferenceIn15Project(){  // ensure 1.5 complains too
	String currentWorkingDirectoryPath = System.getProperty("user.dir");
	if (currentWorkingDirectoryPath == null) {
		fail("BatchCompilerTest#testInference14 could not access the current working directory " + currentWorkingDirectoryPath);
	} else if (!new File(currentWorkingDirectoryPath).isDirectory()) {
		fail("BatchCompilerTest#testInference14 current working directory is not a directory " + currentWorkingDirectoryPath);
	}
	String lib1Path = currentWorkingDirectoryPath + File.separator + "lib1.jar";
	try {
		Util.createJar(
				new String[] {
						"Bundle.java",
						"""
							public class Bundle {
							    static <A> A adapt(Class<A> type) {
							        return null;
							    }
							}"""
				},
				null,
				lib1Path,
				JavaCore.VERSION_1_5);
		this.runNegativeTest(
				new String[] {
						"src/X.java",
						"""
							public class X {
							    Bundle b = Bundle.adapt(BundleWiring.class);
							}
							class BundleWiring {}
							""",
				},
				"\"" + OUTPUT_DIR +  File.separator + "src/X.java\""
				+ " -cp lib1.jar" // relative
				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
				+ " -1.5 -g -preserveAllLocals"
				+ " -proceedOnError -referenceInfo"
				+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		        "",
		        """
					----------
					1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/X.java (at line 2)
						Bundle b = Bundle.adapt(BundleWiring.class);
						           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					Type mismatch: cannot convert from BundleWiring to Bundle
					----------
					1 problem (1 error)
					""",
		        true);
	} catch (IOException e) {
		System.err.println("BatchCompilerTest#testInference14 could not write to current working directory " + currentWorkingDirectoryPath);
	} finally {
		new File(lib1Path).delete();
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=186565 Test interaction between 1.4 and 1.5 class files
public void test186565(){
	String outputDirName = OUTPUT_DIR + File.separator + "d",
	  metaInfDirName = outputDirName + File.separator + "META-INF",
	  jarFileName = outputDirName + File.separator + "classB15.jar";
	this.runConformTest(
		new String[] {
			"d/B.java",
			"public class B<T> extends A<T> {\n" +
			"}",
			"d/A.java",
			"public class A<T> {\n" +
			"}",
			},
	    "\"" + outputDirName + "\""
	    + " -1.5 -g -preserveAllLocals"
	    + " -d \"" + outputDirName + "\"",
		"",
		"",
		true /* flush output directory */);
	File outputDirectory = new File(outputDirName);
	File metaInfDirectory = new File(metaInfDirName);
	metaInfDirectory.mkdirs();
	try {
		Util.createFile(metaInfDirName + File.separator + "MANIFEST.MF",
			"Manifest-Version: 1.0\n" +
			"Class-Path: ../d/classB15.jar\n");
	} catch (IOException e) {
		fail("could not create manifest file");
	}
	Util.delete(outputDirName + File.separator + "A.class");
	Util.delete(outputDirName + File.separator + "A.java");
	try {
		Util.zip(outputDirectory, jarFileName);
	} catch (IOException e) {
		fail("could not create jar file");
	}
	Util.delete(outputDirName + File.separator + "B.class");
	Util.delete(outputDirName + File.separator + "B.java");
	this.runConformTest(
		new String[] {
			"d/A.java",
			"public class A {\n" +
			"}",
			"d/C.java",
			"public class C extends B<String> {\n" +
			"}",
			},
	    "\"" + outputDirName + "\""
	    + " -1.5 -g -preserveAllLocals"
	    + " -cp \"" + jarFileName + "\""
	    + " -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false /* do not flush output directory */);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=330347 - Test retention of bridge methods.
public void testBridgeMethodRetention(){
	String currentWorkingDirectoryPath = System.getProperty("user.dir");
	if (currentWorkingDirectoryPath == null) {
		fail("BatchCompilerTest#testBridgeMethodRetention could not access the current working directory " + currentWorkingDirectoryPath);
	} else if (!new File(currentWorkingDirectoryPath).isDirectory()) {
		fail("BatchCompilerTest#testBridgeMethodRetention current working directory is not a directory " + currentWorkingDirectoryPath);
	}
	String lib1Path = currentWorkingDirectoryPath + File.separator + "lib1.jar";
	try {
		Util.createJar(
				new String[] {
						"Comparable.java",
						"""
							public interface Comparable<T> {
							    public int compareTo(T o);
							}
							""",
						"Character.java",
						"""
							public class Character implements Comparable<Character> {
								public int compareTo(Character obj) {
									return 0;
								}
							}
							"""
				},
				null,
				lib1Path,
				JavaCore.VERSION_1_5);
		this.runConformTest(
				new String[] {
						"src/X.java",
						"""
							public class X {
							    Object fValue;
							    public int compareTo(Object obj) {
							            return ((Character)fValue).compareTo(obj);
							    }
							}
							""",
				},
				"\"" + OUTPUT_DIR +  File.separator + "src/X.java\""
				+ " -cp lib1.jar" // relative
				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
				+ " -1.4 -g -preserveAllLocals"
				+ " -proceedOnError -referenceInfo"
				+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		        "",
		        "",
		        true);
	} catch (IOException e) {
		System.err.println("BatchCompilerTest#testBridgeMethodRetention could not write to current working directory " + currentWorkingDirectoryPath);
	} finally {
		new File(lib1Path).delete();
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817 -- with new option kicking in
public void testReportingUnavoidableGenericProblems() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface Adaptable {
				    public Object getAdapter(Class clazz);   \s
				}
				public class X implements Adaptable {
				    public Object getAdapter(Class clazz) {
				        return null;
				    }
				    Zork z;
				}
				"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.5 -warn:-unavoidableGenericProblems -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)
				public Object getAdapter(Class clazz);   \s
				                         ^^^^^
			Class is a raw type. References to generic type Class<T> should be parameterized
			----------
			2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 8)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			2 problems (1 error, 1 warning)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=322817  -- without new option kicking in
public void testReportingUnavoidableGenericProblems2() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				interface Adaptable {
				    public Object getAdapter(Class clazz);   \s
				}
				public class X implements Adaptable {
				    public Object getAdapter(Class clazz) {
				        return null;
				    }
				    Zork z;
				}
				"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.5 -warn:+unavoidableGenericProblems -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)
				public Object getAdapter(Class clazz);   \s
				                         ^^^^^
			Class is a raw type. References to generic type Class<T> should be parameterized
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)
				public Object getAdapter(Class clazz) {
				                         ^^^^^
			Class is a raw type. References to generic type Class<T> should be parameterized
			----------
			3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 8)
				Zork z;
				^^^^
			Zork cannot be resolved to a type
			----------
			3 problems (1 error, 2 warnings)
			""",
		true);
}
//-warn option - regression tests
public void test0308_warn_options() {
	// check the option introduced in bug 359721
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileReader;
				public class X {
				  void foo() throws java.io.IOException {
				      FileReader r = new FileReader("f1");
				      char[] cs = new char[1024];
					   r.read(cs);
				  }
				}
				"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:-resource -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
//-warn option - regression tests
public void test0309_warn_options() {
	// check the option introduced in bug 359721
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileReader;
				public class X {
				  void foo(boolean b) throws java.io.IOException {
				      FileReader r = new FileReader("f1");
				      char[] cs = new char[1024];
					   r.read(cs);
				      if (b) r.close();
				  }
				}
				"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:+resource -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
				FileReader r = new FileReader("f1");
				           ^
			Potential resource leak: \'r\' may not be closed
			----------
			1 problem (1 warning)
			""",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=366829
// -warn option - regression test to check option syncOverride
// Warning when when a class overrides a synchronized method without synchronizing it
public void test310_warn_options() {
	this.runConformTest(
		new String[] {
			"X.java",
			"class X { synchronized void foo() {} }\n" +
			"class Y extends X { @Override void foo() { } }"
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:syncOverride -1.5 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)
				class Y extends X { @Override void foo() { } }
				                                   ^^^^^
			The method Y.foo() is overriding a synchronized method without being synchronized
			----------
			1 problem (1 warning)
			""",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=366829
// -warn option - regression test to check option syncOverride
// Warning when when a class overrides a synchronized method without synchronizing it
public void test310b_warn_options() {
	this.runConformTest(
		new String[] {
				"X.java",
				"""
					public class X {
					  void bar() { new X() { @Override void foo() {} }; }
					  synchronized void foo() { }
					}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -warn:syncOverride -1.5 -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)
				void bar() { new X() { @Override void foo() {} }; }
				                                      ^^^^^
			The method new X(){}.foo() is overriding a synchronized method without being synchronized
			----------
			1 problem (1 warning)
			""",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325342
// -warn option - regression tests to check option nullAnnot (with args)
// Null warnings because of annotations - custom annotation types used - challenging various kinds of diagnostics
public void test312_warn_options() {
	this.runConformTest(
		new String[] {
				"p/X.java",
				"""
					package p;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.*;
					@SuppressWarnings("unused")
					public class X {
						public void test() { Object o = null; o.toString();}
					  @NonNull Object foo(@Nullable Object o, @NonNull Object o2) {
					    if (o.toString() == ""){ return null;}
					    if (o2 == null) {}
					    goo(null).toString();
						 Object local = null;
						 o.toString();
						 return null;
					  }
					  @Nullable Object goo(@NonNull Object o2) {
					    return new Object();
					  }
					  @NonNullByDefault Object hoo(Object o2) {
					    if (o2 == null){}
					    if (o2 == null){
						    return null;
						 }
						 return new Object();
					  }
					}
					@Documented
					@Retention(RetentionPolicy.CLASS)
					@Target({ METHOD, PARAMETER })
					@interface NonNull{
					}
					@Documented
					@Retention(RetentionPolicy.CLASS)
					@Target({ METHOD, PARAMETER })
					@interface Nullable{
					}
					@Documented
					@Retention(RetentionPolicy.CLASS)
					@Target({ PACKAGE, TYPE, METHOD, CONSTRUCTOR })
					@interface NonNullByDefault{
					}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
//		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -1.5"
		+ " -warn:+nullAnnot(p.Nullable|p.NonNull|p.NonNullByDefault) -warn:+null -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 6)
				public void test() { Object o = null; o.toString();}
				                                      ^
			Null pointer access: The variable o can only be null at this location
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 8)
				if (o.toString() == ""){ return null;}
				    ^
			Potential null pointer access: The variable o may be null at this location
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 8)
				if (o.toString() == ""){ return null;}
				                                ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 9)
				if (o2 == null) {}
				    ^^
			Null comparison always yields false: The variable o2 is specified as @NonNull
			----------
			5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 10)
				goo(null).toString();
				^^^^^^^^^
			Potential null pointer access: The method goo(Object) may return null
			----------
			6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 10)
				goo(null).toString();
				    ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			7. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 13)
				return null;
				       ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			8. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 19)
				if (o2 == null){}
				    ^^
			Null comparison always yields false: The variable o2 is specified as @NonNull
			----------
			9. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 20)
				if (o2 == null){
				    ^^
			Null comparison always yields false: The variable o2 is specified as @NonNull
			----------
			9 problems (9 warnings)
			""",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
// -warn/-error option : enumSwitchPedantic
public void test317_warn_options() {
	this.runConformTest(
		new String[] {
				"p/X.java",
				"""
					package p;
					enum Color { RED, GREEN };
					public class X {
					     int getVal(Color c) {
					         switch (c) {
					             case RED: return 1;
					             default : return 0;
					         }
					     }
					}
					"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -1.5"
		+ " -warn:+enumSwitchPedantic -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 5)
				switch (c) {
				        ^
			The enum constant GREEN should have a corresponding case label in this enum switch on Color. To suppress this problem, add a comment //$CASES-OMITTED$ on the line above the 'default:'
			----------
			1 problem (1 warning)
			""",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
// -warn/-error option : enumSwitchPedantic: increase severity to ERROR
public void test318_warn_options() {
	this.runNegativeTest(
			new String[] {
					"p/X.java",
					"""
						package p;
						enum Color { RED, GREEN };
						public class X {
						     int getVal(Color c) {
						         switch (c) {
						             case RED: return 1;
						             default : return 0;
						         }
						     }
						}
						"""
			},
			"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
			+ " -sourcepath \"" + OUTPUT_DIR + "\""
			+ " -1.5"
			+ " -err:+enumSwitchPedantic -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 5)
					switch (c) {
					        ^
				The enum constant GREEN should have a corresponding case label in this enum switch on Color. To suppress this problem, add a comment //$CASES-OMITTED$ on the line above the 'default:'
				----------
				1 problem (1 error)
				""",
			true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=374605
// -warn/-error option : switchDefault
public void test319_warn_options() {
	this.runConformTest(
		new String[] {
				"p/X.java",
				"""
					package p;
					enum Color { RED, GREEN };
					public class X {
					     int getVal(Color c) {
					         switch (c) {
					             case RED: return 1;
					             case GREEN : return 2;
					         }
					         return 0;
					     }
					}
					"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -1.5"
		+ " -warn:+switchDefault -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 5)
				switch (c) {
				        ^
			The switch over the enum type Color should have a default case
			----------
			1 problem (1 warning)
			""",
		true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220928
//-nowarn option - regression tests
//default
public void test317_nowarn_options() {
	this.runConformTest(
		new String[] {
				"src/X.java",
				"""
					public class X {
					  /**
					    @param
					  */
					  public void foo() {
					  }
					}""",
			},
			"\"" + OUTPUT_DIR + File.separator + "src/X.java\""
			+ " -warn:javadoc -nowarn:[\"" +
			OUTPUT_DIR + File.separator + "src" +
			"\"] -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"",
			true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220928
//-nowarn option - regression tests
//two different source folders ignore only from one
public void test318_nowarn_options() {
	this.runConformTest(
		new String[] {
				"src/X.java",
				"""
					public class X {
					  /**
					    @param
					  */
					  public void foo() {
					  }
					}""",
				"src2/Y.java",
				"""
					public class Y {
					  /**
					    @param
					  */
					  public void foo() {
					  }
					}"""
			},
			"\"" + OUTPUT_DIR + File.separator + "src/X.java\"" +
			" \"" + OUTPUT_DIR + File.separator + "src2/Y.java\"" +
			" -warn:javadoc -nowarn:[" +
			"\"" + OUTPUT_DIR + File.separator + "src"
			+ "\"] -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"""
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 3)
					@param
					 ^^^^^
				Javadoc: Missing parameter name
				----------
				1 problem (1 warning)
				""",
			true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220928
//-nowarn option - regression tests
//two different source folders ignore from both
public void test319_nowarn_options() {
	this.runConformTest(
		new String[] {
				"src1/X.java",
				"""
					public class X {
					  /**
					    @param
					  */
					  public void foo() {
					  }
					}""",
				"src2/Y.java",
				"""
					public class Y {
					  /**
					    @param
					  */
					  public void foo() {
					  }
					}"""
			},
			"\"" + OUTPUT_DIR + File.separator + "src1/X.java\"" +
			" \"" + OUTPUT_DIR + File.separator + "src2/Y.java\"" +
			" -warn:javadoc -nowarn:[" +
			"\"" + OUTPUT_DIR + File.separator + "src1\"" + File.pathSeparator +
			"\"" + OUTPUT_DIR + File.separator +
			"src2\"] -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"",
			true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220928
//-nowarn option - regression tests
//two different source folders ignore from both using multiple -nowarn
public void test320_nowarn_options() {
	this.runConformTest(
		new String[] {
				"src1/X.java",
				"""
					public class X {
					  /**
					    @param
					  */
					  public void foo() {
					  }
					}""",
				"src2/Y.java",
				"""
					public class Y {
					  /**
					    @param
					  */
					  public void foo() {
					  }
					}"""
			},
			"\"" + OUTPUT_DIR + File.separator + "src1/X.java\"" +
			" \"" + OUTPUT_DIR + File.separator + "src2/Y.java\"" +
			" -warn:javadoc -nowarn:[" +
			"\"" + OUTPUT_DIR + File.separator + "src1\"] -nowarn:[" +
			"\"" + OUTPUT_DIR + File.separator + "src2\"] " +
			"-proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"",
			true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220928
//-nowarn option - regression tests
//option syntax error -nowarn:
public void test321_nowarn_options() {
	this.runNegativeTest(
		new String[] {
				"src/X.java",
				"""
					public class X {
					  /**
					    @param
					  */
					  public void foo() {
					  }
					}""",
			},
			"\"" + OUTPUT_DIR + File.separator + "src/X.java\""
			+ " -warn:javadoc -nowarn: -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"invalid syntax for nowarn option: -nowarn:\n",
			true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220928
//-nowarn option - regression tests
//option syntax error -nowarn:[
public void test322_nowarn_options() {
	this.runNegativeTest(
		new String[] {
				"src/X.java",
				"""
					public class X {
					  /**
					    @param
					  */
					  public void foo() {
					  }
					}""",
			},
			"\"" + OUTPUT_DIR + File.separator + "src/X.java\""
			+ " -warn:javadoc -nowarn:[ -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"invalid syntax for nowarn option: -nowarn:[\n",
			true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220928
//-nowarn option - regression tests
//option syntax error -nowarn:[src
public void test323_nowarn_options() {
	this.runNegativeTest(
		new String[] {
				"src/X.java",
				"""
					public class X {
					  /**
					    @param
					  */
					  public void foo() {
					  }
					}""",
			},
			"\"" + OUTPUT_DIR + File.separator + "src/X.java\""
			+ " -warn:javadoc -nowarn:[src -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"invalid syntax for nowarn option: -nowarn:[src\n",
			true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220928
//-nowarn option - regression tests
//option syntax error -nowarn:src]
public void test324_nowarn_options() {
	this.runNegativeTest(
		new String[] {
				"src/X.java",
				"""
					public class X {
					  /**
					    @param
					  */
					  public void foo() {
					  }
					}""",
			},
			"\"" + OUTPUT_DIR + File.separator + "src/X.java\""
			+ " -warn:javadoc -nowarn:src] -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"invalid syntax for nowarn option: -nowarn:src]\n",
			true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220928
//-nowarn option - regression tests
//option syntax error -nowarn[src]
public void test325_nowarn_options() {
	this.runNegativeTest(
		new String[] {
				"src/X.java",
				"""
					public class X {
					  /**
					    @param
					  */
					  public void foo() {
					  }
					}""",
			},
			"\"" + OUTPUT_DIR + File.separator + "src/X.java\""
			+ " -warn:javadoc -nowarn[src] -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"invalid syntax for nowarn option: -nowarn[src]\n",
			true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220928
//-nowarn option - regression tests
//option syntax error -nowarn:[src1]src2
public void test326_nowarn_options() {
	this.runNegativeTest(
		new String[] {
				"src/X.java",
				"""
					public class X {
					  /**
					    @param
					  */
					  public void foo() {
					  }
					}""",
			},
			"\"" + OUTPUT_DIR + File.separator + "src/X.java\""
			+ " -warn:javadoc -nowarn:[src1]src2 -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"invalid syntax for nowarn option: -nowarn:[src1]src2\n",
			true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220928
//-nowarn option - regression tests
//option syntax error -nowarn:[]
public void test327_nowarn_options() {
	this.runNegativeTest(
		new String[] {
				"src/X.java",
				"""
					public class X {
					  /**
					    @param
					  */
					  public void foo() {
					  }
					}""",
			},
			"\"" + OUTPUT_DIR + File.separator + "src/X.java\""
			+ " -warn:javadoc -nowarn:[] -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"invalid syntax for nowarn option: -nowarn:[]\n",
			true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220928
//-nowarn option - regression tests
//non-optional errors cannot be ignored
public void test328_nowarn_options() {
	this.runNegativeTest(
		new String[] {
				"src/X.java",
				"""
					public class X {
					  /**
					    @param
					  */
					  public void foo() {
					    a++;
					  }
					}""",
			},
			"\"" + OUTPUT_DIR + File.separator + "src/X.java\""
			+ " -warn:javadoc -nowarn:[" +
			"\"" + OUTPUT_DIR + File.separator + "src]\" -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"""
				----------
				1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/src/X.java (at line 6)
					a++;
					^
				a cannot be resolved to a variable
				----------
				1 problem (1 error)
				""",
			true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=220928
//-nowarn option - regression tests
//task tags cannot be ignored
public void test329_nowarn_options() {
	this.runConformTest(
		new String[] {
				"src/X.java",
				"""
					public class X {
					  /**
					    @param
					  */
					  public void foo() {
					    // TODO nothing
					  }
					}""",
			},
			"\"" + OUTPUT_DIR + File.separator + "src/X.java\""
			+ " -warn:javadoc,tasks(TODO) -nowarn:[" +
			"\"" + OUTPUT_DIR + File.separator + "src]\" -proc:none -d \"" + OUTPUT_DIR + "\"",
			"",
			"""
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src/X.java (at line 6)
					// TODO nothing
					   ^^^^^^^^^^^^
				TODO nothing
				----------
				1 problem (1 warning)
				""",
			true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=408815
// -warn option - regression tests to check option unlikelyCollectionMethodArgumentType
public void test330_warn_options() {
	this.runConformTest(
		new String[] {
				"p/X.java",
				"""
					package p;
					import java.util.Map;
					public class X {
					  Integer foo(Map<String,Integer> map) {
						 return map.get(3);
					  }
					}
					""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -1.5"
		+ " -warn:-unlikelyCollectionMethodArgumentType -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}//https://bugs.eclipse.org/bugs/show_bug.cgi?id=408815
//-warn option - regression tests to check option unlikelyEqualsArgumentType
public void test331_warn_options() {
	this.runConformTest(
		new String[] {
				"p/X.java",
				"""
					package p;
					public class X {
					  boolean foo() {
						 return "three".equals(3);
					  }
					}
					""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -1.5"
		+ " -info:-unlikelyEqualsArgumentType -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375409
public void testBug375409a() {
	this.runConformTest(
		new String[] {
				"p/X.java",
				"""
					package p;
					public class X {
					/**\s
					* Description {@see String}, {@category cat}
					* @param a
					*/
					public void foo(int i) {}}
					"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -1.5"
		+ " -warn:invalidJavadoc -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 4)
				* Description {@see String}, {@category cat}
				                ^^^
			Javadoc: Unexpected tag
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 4)
				* Description {@see String}, {@category cat}
				                               ^^^^^^^^
			Javadoc: Unexpected tag
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 5)
				* @param a
				         ^
			Javadoc: Parameter a is not declared
			----------
			3 problems (3 warnings)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375409
public void testBug375409b() {
	this.runConformTest(
		new String[] {
				"p/X.java",
				"""
					package p;
					public class X {
					/**\s
					* Description {@see String}, {@category cat}
					* @param a
					*/
					public void foo(int i) {}}
					"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -1.5"
		+ " -warn:missingJavadocTags,missingJavadocTagsVisibility(public) -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 7)
				public void foo(int i) {}}
				                    ^
			Javadoc: Missing tag for parameter i
			----------
			1 problem (1 warning)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375409
public void testBug375409c() {
	this.runConformTest(
		new String[] {
				"p/X.java",
				"""
					package p;
					public class X {
					/**\s
					* Description {@see String}, {@category cat}
					* @param a
					*/
					public void foo(int i) {}}
					"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -1.5"
		+ " -warn:missingJavadocComments -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 2)
				public class X {
				             ^
			Javadoc: Missing comment for public declaration
			----------
			1 problem (1 warning)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375409
public void testBug375409d() {
	this.runConformTest(
		new String[] {
				"p/X.java",
				"""
					package p;
					public class X {
					/**\s
					* Description {@see String}, {@category cat}
					* @param a
					*/
					void foo(int i) {}
					/**\s
					* Description {@see String}, {@category cat}
					* @param a
					*/
					public void foo2(int i2) {}}
					"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
		+ " -sourcepath \"" + OUTPUT_DIR + "\""
		+ " -1.5"
		+ " -warn:missingJavadocTags,missingJavadocTagsVisibility(public) -proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 12)
				public void foo2(int i2) {}}
				                     ^^
			Javadoc: Missing tag for parameter i2
			----------
			1 problem (1 warning)
			""",
		true);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375409
// -warn option - regression tests to check option nullAnnotConflict
public void testBug375409e() {
	this.runConformTest(
		new String[] {
				"p/X.java",
				"""
					package p;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.*;
					public class X {
					  @NonNull Object foo(@Nullable Object o, @NonNull Object o2) {
						 Object o3 = new X().bar();
						 return o3;
					  }
					  @Nullable Object bar() {
						 return null;
					  }
					}
					@Documented
					@Retention(RetentionPolicy.CLASS)
					@Target({ METHOD, PARAMETER })
					@interface NonNull{
					}
					@Documented
					@Retention(RetentionPolicy.CLASS)
					@Target({ METHOD, PARAMETER })
					@interface Nullable{
					}
					@Documented
					@Retention(RetentionPolicy.CLASS)
					@Target({ PACKAGE, TYPE, METHOD, CONSTRUCTOR })
					@interface NonNullByDefault{
					}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
		+ " -1.5"
		+ " -warn:+nullAnnot(p.Nullable|p.NonNull|p.NonNullByDefault),+null,-nullAnnotConflict "
		+ "-proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375409
// -warn option - regression tests to check option nullAnnotRedundant
public void testBug375409f() {
	this.runConformTest(
		new String[] {
				"p/X.java",
				"""
					package p;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.*;
					@NonNullByDefault public class X {
					  @NonNull Object foo() {
						 return null;
					  }
					}
					@Documented
					@Retention(RetentionPolicy.CLASS)
					@Target({ METHOD, PARAMETER })
					@interface NonNull{
					}
					@Documented
					@Retention(RetentionPolicy.CLASS)
					@Target({ METHOD, PARAMETER })
					@interface Nullable{
					}
					@Documented
					@Retention(RetentionPolicy.CLASS)
					@Target({ PACKAGE, TYPE, METHOD, CONSTRUCTOR })
					@interface NonNullByDefault{
					}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
		+ " -1.5"
		+ " -warn:+nullAnnot(p.Nullable|p.NonNull|p.NonNullByDefault),+null,-nullAnnotRedundant "
		+ "-proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/X.java (at line 6)
				return null;
				       ^^^^
			Null type mismatch: required \'@NonNull Object\' but the provided value is null
			----------
			1 problem (1 warning)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=375409
// -warn option - regression tests to check option nullUncheckedConversion
public void testBug375409g() {
	this.runConformTest(
		new String[] {
				"p/X.java",
				"""
					package p;
					import static java.lang.annotation.ElementType.*;
					import java.lang.annotation.*;
					public class X {
					  @NonNull Object foo(@Nullable Object o, @NonNull Object o2) {
						 return new X().bar();
					  }
					  Object bar() {
						 return null;
					  }
					}
					@Documented
					@Retention(RetentionPolicy.CLASS)
					@Target({ METHOD, PARAMETER })
					@interface NonNull{
					}
					@Documented
					@Retention(RetentionPolicy.CLASS)
					@Target({ METHOD, PARAMETER })
					@interface Nullable{
					}
					@Documented
					@Retention(RetentionPolicy.CLASS)
					@Target({ PACKAGE, TYPE, METHOD, CONSTRUCTOR })
					@interface NonNullByDefault{
					}"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "p" + File.separator + "X.java\""
		+ " -1.5"
		+ " -warn:+nullAnnot(p.Nullable|p.NonNull|p.NonNullByDefault) -warn:+null -warn:-nullUncheckedConversion "
		+ "-proc:none -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}


// Bug 375366 - ECJ ignores unusedParameterIncludeDocCommentReference unless enableJavadoc option is set
// when -properties is used process javadoc by default
public void testBug375366a() throws IOException {
	createOutputTestDirectory("regression/.settings");
	Util.createFile(OUTPUT_DIR+"/.settings/org.eclipse.jdt.core.prefs",
			"eclipse.preferences.version=1\n" +
			"org.eclipse.jdt.core.compiler.problem.unusedParameter=warning\n");
	this.runConformTest(
		new String[] {
			"bugs/warning/ShowBug.java",
			"""
				package bugs.warning;
				
				public class ShowBug {
					/**
					 *\s
					 * @param unusedParam
					 */
					public void foo(Object unusedParam) {
					\t
					}
				}
				"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "bugs" + File.separator + "warning" + File.separator + "ShowBug.java\""
		+ " -1.5"
		+ " -properties " + OUTPUT_DIR + File.separator +".settings" + File.separator + "org.eclipse.jdt.core.prefs "
		+ " -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		false /*don't flush output dir*/);
}

// Bug 375366 - ECJ ignores unusedParameterIncludeDocCommentReference unless enableJavadoc option is set
// property file explicitly disables javadoc processing
public void testBug375366b() throws IOException {
	createOutputTestDirectory("regression/.settings");
	Util.createFile(OUTPUT_DIR+"/.settings/org.eclipse.jdt.core.prefs",
			"""
				eclipse.preferences.version=1
				org.eclipse.jdt.core.compiler.problem.unusedParameter=warning
				org.eclipse.jdt.core.compiler.doc.comment.support=disabled
				""");
	this.runTest(
		true, // compile OK, expecting only warning
		new String[] {
			"bugs/warning/ShowBug.java",
			"""
				package bugs.warning;
				
				public class ShowBug {
					/**
					 *\s
					 * @param unusedParam
					 */
					public void foo(Object unusedParam) {
					\t
					}
				}
				"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "bugs" + File.separator + "warning" + File.separator + "ShowBug.java\""
		+ " -1.5"
		+ " -properties " + OUTPUT_DIR + File.separator +".settings" + File.separator + "org.eclipse.jdt.core.prefs "
		+ " -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/bugs/warning/ShowBug.java (at line 8)
				public void foo(Object unusedParam) {
				                       ^^^^^^^^^^^
			The value of the parameter unusedParam is not used
			----------
			1 problem (1 warning)
			""",
		false /*don't flush output dir*/,
		null /* progress */);
}
// see also:
// org.eclipse.jdt.core.tests.compiler.regression.NullAnnotationBatchCompilerTest.testBug375366c()
// org.eclipse.jdt.core.tests.compiler.regression.NullAnnotationBatchCompilerTest.testBug375366d()

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=385780
public void test385780_warn_option() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				public class X<T> {
				public <S> X() {
				}
				public void ph(int t) {
				}
				}
				interface doNothingInterface<T> {
				}
				class doNothing {
				public <T> void doNothingMethod() {\
				}
				}
				class noerror {
				public <T> void doNothing(T t) {\
				}\
				}
				"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:unusedTypeParameter -proc:none -1.7 -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 1)
				public class X<T> {
				               ^
			Unused type parameter T
			----------
			2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)
				public <S> X() {
				        ^
			Unused type parameter S
			----------
			3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)
				interface doNothingInterface<T> {
				                             ^
			Unused type parameter T
			----------
			4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 10)
				public <T> void doNothingMethod() {}
				        ^
			Unused type parameter T
			----------
			4 problems (4 warnings)
			""",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=405225
public void test405225_extdirs() {
	if (AbstractCompilerTest.isJRE9Plus)
		return;
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				import java.io.FileReader;
				public class X {
				  void foo() throws java.io.IOException {
				      FileReader r = new FileReader("f1");
				      char[] cs = new char[1024];
					   r.read(cs);
				  }
				}
				"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -warn:-resource -1.7 -extdirs \"" + LIB_DIR + "\" -d \"" + OUTPUT_DIR + "\"",
		"",
		"",
		true);
}
//Bug 408038 - Classes which implement Externalizable should not have an unused constructor warning
public void test408038a() {
	this.runConformTest(
		new String[] {
			"externalizable/warning/X.java",
			"""
				package externalizable.warning;
				
				public class X {
					private class Y {
						static final int i = 10;
						public Y() {}
						public Y(int x) {System.out.println(x);}
					}
				
					public void zoo() {
						System.out.println(Y.i);
						Y y = new Y(5);
						System.out.println(y);
					}
				}""",
			},
			"\"" + OUTPUT_DIR +  File.separator + "externalizable" + File.separator + "warning" + File.separator + "X.java\""
			+ " -1.6 -d none",
			"",
			"""
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/externalizable/warning/X.java (at line 6)
					public Y() {}
					       ^^^
				The constructor X.Y() is never used locally
				----------
				1 problem (1 warning)
				""",
			true);
}
//Bug 408038 - Classes which implement Externalizable should not have an unused constructor warning
public void test408038b() {
	this.runConformTest(
		new String[] {
			"externalizable/warning/X.java",
			"""
				package externalizable.warning;
				
				public class X {
					private static class Y {
						static final int i = 10;
						public Y() {}
						public Y(int x) {System.out.println(x);}
					}
				
					public void zoo() {
						System.out.println(Y.i);
						Y y = new Y(5);
						System.out.println(y);
					}
				}""",
			},
			"\"" + OUTPUT_DIR +  File.separator + "externalizable" + File.separator + "warning" + File.separator + "X.java\""
			+ " -1.6 -d none",
			"",
			"""
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/externalizable/warning/X.java (at line 6)
					public Y() {}
					       ^^^
				The constructor X.Y() is never used locally
				----------
				1 problem (1 warning)
				""",
			true);
}
//Bug 408038 - Classes which implement Externalizable should not have an unused constructor warning
public void test408038c() {
	this.runConformTest(
		new String[] {
			"externalizable/warning/X.java",
			"""
				package externalizable.warning;
				import java.io.Externalizable;
				import java.io.IOException;
				import java.io.ObjectInput;
				import java.io.ObjectOutput;
				
				public class X {
					private static class Y implements Externalizable {
						static final int i = 10;
						public Y() {}
						public Y(int x) {System.out.println(x);}
				
						@Override
						public void writeExternal(ObjectOutput out) throws IOException {
						}
				
						@Override
						public void readExternal(ObjectInput in) throws IOException,
						ClassNotFoundException {
						}
					}
				
					public void zoo() {
						System.out.println(Y.i);
						Y y = new Y(5);
						System.out.println(y);
					}
				}""",
			},
			"\"" + OUTPUT_DIR +  File.separator + "externalizable" + File.separator + "warning" + File.separator + "X.java\""
			+ " -1.6 -d none",
			"",
			"",
			true);
}
//Bug 408038 - Classes which implement Externalizable should not have an unused constructor warning
public void test408038d() {
	this.runConformTest(
		new String[] {
			"externalizable/warning/X.java",
			"""
				package externalizable.warning;
				import java.io.Externalizable;
				import java.io.IOException;
				import java.io.ObjectInput;
				import java.io.ObjectOutput;
				
				public class X {
					private class Y implements Externalizable {
						static final int i = 10;
						public Y() {}
						public Y(int x) {System.out.println(x);}
				
						@Override
						public void writeExternal(ObjectOutput out) throws IOException {
						}
				
						@Override
						public void readExternal(ObjectInput in) throws IOException,
						ClassNotFoundException {
						}
					}
				
					public void zoo() {
						System.out.println(Y.i);
						Y y = new Y(5);
						System.out.println(y);
					}
				}""",
			},
			"\"" + OUTPUT_DIR +  File.separator + "externalizable" + File.separator + "warning" + File.separator + "X.java\""
			+ " -1.6 -d none",
			"",
			"""
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/externalizable/warning/X.java (at line 10)
					public Y() {}
					       ^^^
				The constructor X.Y() is never used locally
				----------
				1 problem (1 warning)
				""",
			true);
}
// Bug 408038 - Classes which implement Externalizable should not have an unused constructor warning
// The test case is not directly related to the bug. It was discovered as a result
// of the bug. Please see comment 16 bullet 4 in bugzilla.
public void test408038e() {
	this.runConformTest(
		new String[] {
			"externalizable/warning/X.java",
			"""
				package externalizable.warning;
				class X {
					int i;
					private X(int x) {i = x;}
					X() {}
					public int foo() {
						X x = new X();
						return x.i;
					}
				}
				"""
			},
			"\"" + OUTPUT_DIR +  File.separator + "externalizable" + File.separator + "warning" + File.separator + "X.java\""
			+ " -1.6 -d none",
			"",
			"""
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/externalizable/warning/X.java (at line 4)
					private X(int x) {i = x;}
					        ^^^^^^^^
				The constructor X(int) is never used locally
				----------
				1 problem (1 warning)
				""",
			true);
}
public void testBug574425() {
	String path = LIB_DIR;
	String libPath = null;
	if (path.endsWith(File.separator)) {
		libPath = path + "lib.jar";
	} else {
		libPath = path + File.separator + "lib.jar";
	}
	try {
		Util.createJar(
				new String[] {
						"org/apache/accumulo/core/conf/Property.java",
						"""
							package org.apache.accumulo.core.conf;
							import java.lang.annotation.Inherited;
							import java.lang.annotation.Retention;
							import java.lang.annotation.RetentionPolicy;
							public enum Property {
								@Deprecated(since = "2.1.0")
								TSERV_WAL_SORT_MAX_CONCURRENT("tserver.wal.sort.concurrent.max", "2", "2",
										"The maximum number of threads to use to sort logs during recovery", "2.1.0"),
								@Deprecated(since = "2.1.0")
								@ReplacedBy(property = Property.TSERV_WAL_SORT_MAX_CONCURRENT)\s
								TSERV_RECOVERY_MAX_CONCURRENT("tserver.recovery.concurrent.max", "2", "2",
										"The maximum number of threads to use to sort logs during recovery", "1.5.0"),
								RPC_SSL_KEYSTORE_PASSWORD("rpc.javax.net.ssl.keyStorePassword", "", "2",
										"Password used to encrypt the SSL private keystore. " + "Leave blank to use the Accumulo instance secret",
										"1.6.0");\s
							  Property(String name, String defaultValue, String type, String description,
							      String availableSince) {
							  }
							}
							@Inherited
							@Retention(RetentionPolicy.RUNTIME)
							@interface ReplacedBy {\s
							  Property property();
							}
							"""
				},
				null,
				libPath,
				JavaCore.VERSION_1_8);
		this.runConformTest(
				new String[] {
						"src/org/apache/accumulo/test/fate/zookeeper/X.java",
						"""
							package org.apache.accumulo.test.fate.zookeeper;
							import org.apache.accumulo.core.conf.Property;
							public class X {
							}
							""",
				},
				"\"" + OUTPUT_DIR +  File.separator + "src/org/apache/accumulo/test/fate/zookeeper/X.java\""
				+ " -classpath \"" + libPath + "\""
				+ " -1.8 -nowarn"
				+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
				"",
				"",
		        true);
	} catch (IOException e) {
	} finally {
		Util.delete(libPath);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=419351
public void testBug419351() {
	String backup = System.getProperty("java.endorsed.dirs");
	if (backup == null)
		return; // Don't bother running if it is JRE9, where there's no endorsed.dir
	String endorsedPath = LIB_DIR + File.separator + "endorsed";
	new File(endorsedPath).mkdir();
	String lib1Path = endorsedPath + File.separator + "lib1.jar";
	try {
		System.setProperty("java.endorsed.dirs", endorsedPath);
		Util.createJar(
				new String[] {
						"java/lang/String.java",
						"""
							package java.lang;
							public class String {
							    public String(java.lang.Object obj) {}
							}
							"""
				},
				null,
				lib1Path,
				JavaCore.VERSION_1_5);
		this.runConformTest(
				new String[] {
						"src/X.java",
						"""
							public class X {
							    public void foo(Object obj) {
							        java.lang.String str = new java.lang.String(obj);
								}
							}
							""",
				},
				"\"" + OUTPUT_DIR +  File.separator + "src/X.java\""
				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
				+ " -1.4 -nowarn"
				+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
				"",
				"",
		        true);
	} catch (IOException e) {
	} finally {
		System.setProperty("java.endorsed.dirs", backup);
		new File(endorsedPath).delete();
		new File(lib1Path).delete();
	}
}

public void test501457() throws IOException {
	this.runConformTest(new String[] {
			"FailingClass.java",
			"""
				import java.lang.invoke.MethodHandle;
				import java.util.ArrayList;
				public class FailingClass {
				  protected void test(MethodHandle handle) throws Throwable {
				        handle.invoke(null, new ArrayList<>());
				    }
				}
				"""
		},
		" -1.8 " +
		" -sourcepath \"" + OUTPUT_DIR + "\" " +
		"\"" + OUTPUT_DIR +  File.separator + "FailingClass.java",
		"",
		"",
		true
	);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=439750
public void test439750() {
	this.runConformTest(
		new String[] {
			"externalizable/warning/X.java",
			"""
				import java.io.FileInputStream;
				import java.io.IOException;
				class X {
					public static void main(String[] args) {
						FileInputStream fis = null;
						try {
							fis = new FileInputStream("xyz");
							System.out.println("fis");
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							try {
								if (fis != null) fis.close();
							} catch (Exception e) {}
						}
					}
				}
				"""
			},
			"\"" + OUTPUT_DIR +  File.separator + "externalizable" + File.separator + "warning" + File.separator + "X.java\""
			+ " -1.6 -warn:unused -warn:unusedExceptionParam -d none",
			"",
			"""
				----------
				1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/externalizable/warning/X.java (at line 14)
					} catch (Exception e) {}
					                   ^
				The value of the exception parameter e is not used
				----------
				1 problem (1 warning)
				""",
			true);
}

/**
 * A fast exit/result is expected when secondary types are searched with the reserved class name "package-info",
 * because there can not exist a secondary type with the name "package-info", because it is a reserved class name.
 * This fast exit improves the performance, because the search for secondary types is very expensive regarding performance
 * (all classes of a package have to get loaded, parsed and analyzed).
 */
public void testFileSystem_findSecondaryInClass() {
	final String testScratchArea = "fileSystemTestScratchArea";

	File testScratchAreaFile = new File(Util.getOutputDirectory(), testScratchArea);
	try {
		if(!testScratchAreaFile.exists()) {
			testScratchAreaFile.mkdirs();
		}

		assertTrue(testScratchAreaFile.exists());

		Classpath classpath = FileSystem.getClasspath(testScratchAreaFile.getPath(), null, null);
		assertNotNull(classpath);
		assertTrue(classpath instanceof ClasspathDirectory);

		ClasspathDirectory classpathDirectory = (ClasspathDirectory)classpath;
		NameEnvironmentAnswer answer = classpathDirectory.findSecondaryInClass(TypeConstants.PACKAGE_INFO_NAME, null, null);
		assertNull(answer); //No answer is expected, because "package-info" isn't a secondary type.

		try {
			//When there is a call with another name like "package-info", an exception is expected, because the search can not get executed successfully
			// when no value for qualifiedPackageName is provided.
			classpathDirectory.findSecondaryInClass("X".toCharArray(), null, null);
			fail("An exception is expected, because the parameter qualifiedPackageName can not be NULL!");
		} catch(Exception e) {}
	} finally {
		if(testScratchAreaFile.exists()) {
			Util.delete(testScratchAreaFile);
		}
	}
}
//same as test293, but for -info: instead of -err:
public void test496137a(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib3.jar[~p/A]\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo -info:+discouraged"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"""
			----------
			1. INFO in ---OUTPUT_DIR_PLACEHOLDER---/src/p/X.java (at line 4)
				A a;
				^
			Discouraged access: The type \'A\' is not API (restriction on classpath entry \'---LIB_DIR_PLACEHOLDER---/lib3.jar\')
			----------
			1 problem (1 info)
			""",
		true);
}
//same as test294, but for -info: instead of -err:
public void test496137b(){
	this.runConformTest(
		new String[] {
			"src/X.java",
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/X.java\""
		+ " -cp \"" + LIB_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo -info:+discouraged2"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"invalid info token: \'discouraged2\'. Ignoring this info token and compiling\n",
		true);
}
//same as test296, but for -info: instead of -err:
public void test496137c(){
	this.runNegativeTest(
		new String[] {
			"src/X.java",
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/X.java\""
		+ " -cp \"" + LIB_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo -info:"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"invalid info configuration: \'-info:\'\n",
		true);
}
//same as test297, but for -info: instead of -err:
public void test496137d(){
	this.runNegativeTest(
		new String[] {
			"src/X.java",
			"public class X {\n" +
			"}",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/X.java\""
		+ " -cp \"" + LIB_DIR + "\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo -info"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"invalid info configuration: \'-info\'\n",
		true);
}
//same as testBug375366b, but for =info: instead of =warning:
public void test496137e() throws IOException {
	createOutputTestDirectory("regression/.settings");
	Util.createFile(OUTPUT_DIR+"/.settings/org.eclipse.jdt.core.prefs",
			"""
				eclipse.preferences.version=1
				org.eclipse.jdt.core.compiler.problem.unusedParameter=info
				org.eclipse.jdt.core.compiler.doc.comment.support=disabled
				""");
	this.runTest(
		true, // compile OK, expecting only warning
		new String[] {
			"bugs/warning/ShowBug.java",
			"""
				package bugs.warning;
				
				public class ShowBug {
					/**
					 *\s
					 * @param unusedParam
					 */
					public void foo(Object unusedParam) {
					\t
					}
				}
				"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "bugs" + File.separator + "warning" + File.separator + "ShowBug.java\""
		+ " -1.5"
		+ " -properties " + OUTPUT_DIR + File.separator +".settings" + File.separator + "org.eclipse.jdt.core.prefs "
		+ " -d \"" + OUTPUT_DIR + "\"",
		"",
		"""
			----------
			1. INFO in ---OUTPUT_DIR_PLACEHOLDER---/bugs/warning/ShowBug.java (at line 8)
				public void foo(Object unusedParam) {
				                       ^^^^^^^^^^^
			The value of the parameter unusedParam is not used
			----------
			1 problem (1 info)
			""",
		false /*don't flush output dir*/,
		null /* progress */);
}
// variation of test496137a to test that -warn:none turns off all info, too
public void test496137f(){
	createCascadedJars();
	this.runConformTest(
		new String[] {
			"src/p/X.java",
			"""
				package p;
				/** */
				public class X {
				  A a;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p/X.java\""
		+ " -cp \"" + LIB_DIR + File.separator + "lib3.jar[~p/A]\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.5 -g -preserveAllLocals"
		+ " -proceedOnError -referenceInfo -info:+discouraged -warn:none"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"",
		true);
}
public void testReleaseOption() throws Exception {
	try {
		SourceVersion valueOf = SourceVersion.valueOf("RELEASE_9");
		if (valueOf != null)
			return;
	} catch(IllegalArgumentException iae) {
		// Ignore
	}
	this.runNegativeTest(
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
	     "option --release is supported only when run with JDK 9 or above\n",
	     true);
}

public void testBug531579() throws Exception {
	if (!isJRE9Plus) return;
	// these types replace inaccessible types from JRE/javax.xml.bind:
	runConformTest(new String[] {
			"src/javax/xml/bind/JAXBContext.java",
			"""
				package javax.xml.bind;
				public abstract class JAXBContext {
					public static JAXBContext newInstance( String contextPath )
						throws JAXBException {
						return null;
					}
				}
				""",
			"src/javax/xml/bind/JAXBException.java",
			"package javax.xml.bind;\n" +
			"public class JAXBException extends Exception {}\n"
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/javax/xml/bind/JAXBContext.java\""
		+ " \"" + OUTPUT_DIR +  File.separator + "src/javax/xml/bind/JAXBException.java\""
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.8"
		+ " -warn:none"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"",
		true);

	runConformTest(new String[] {
			"src/p1/ImportJAXBType.java",
			"""
				package p1;
				
				import javax.xml.bind.JAXBContext;
				
				public class ImportJAXBType {
				
					public static void main(String[] args) throws Exception {
						JAXBContext context = JAXBContext.newInstance("");
					}
				
				}
				"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "src/p1/ImportJAXBType.java\""
		+ " -cp \"" + OUTPUT_DIR + File.separator + "bin\" "
		+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src\""
		+ " -1.9"
		+ " -warn:none"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin\" ",
		"",
		"",
		false);
}

public void testFailOnWarnings_NoWarning() {
	this.runConformTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -failOnWarning"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin/\"",
		"",
		"",
		true);

}

public void testFailOnWarnings_WithWarning() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				/** */
				public class X {
				private int a;
				}""",
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -failOnWarning"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin/\"",
		"",
		"----------\n" +
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" +
		"	private int a;\n" +
		"	            ^\n" +
		"The value of the field X.a is not used\n" +
		"----------\n" +
		"1 problem (1 warning)\n" +
		"error: warnings found and -failOnWarning specified\n" +
		"",
		true);
}
public void testUnusedObjectAllocation() {
	runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
					void foo() {
						new X();
					}
				}
				"""
		},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -err:+unused"
		+ " -d \"" + OUTPUT_DIR + File.separator + "bin/\"",
		"",
		"""
			----------
			1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
				new X();
				^^^^^^^
			The allocated object is never used
			----------
			1 problem (1 error)
			""",
		true);

}
public void testBug573153() {
	String output = MAIN.bind("configure.source", "10");
	String template = "source level should be in '1.1'...'1.8','9'...'15' (or '5.0'..'15.0'): 10";
	template = template.replace("15", CompilerOptions.getLatestVersion());
	assertEquals("configure.source is not updated", template, output);

	output = MAIN.bind("configure.targetJDK", "10");
	template = "target level should be in '1.1'...'1.8','9'...'15' (or '5.0'..'15.0') or cldc1.1: 10";
	template = template.replace("15", CompilerOptions.getLatestVersion());
	assertEquals("configure.source is not updated", template, output);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=413873
public void test413873() {
	this.runConformTest(
		new String[] {
			"OuterClass.java",
			"""
				public class OuterClass<T> {
					private final class InnerClass {
					}
				
					private InnerClass foo(final InnerClass object) {
						return object;
					}
				\t
					public void doStuff() {
						foo(new InnerClass());
					}
				}"""
			},
			"\"" + OUTPUT_DIR +  File.separator + "OuterClass.java\""
			+ " -1.6 -warn:all-static-method -proc:none -d none",
			"",
			"",
			true);
}
//https://github.com/eclipse-jdt/eclipse.jdt.core/issues/89
public void testIssue89_1() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Set;
					public class X<T> {
						public boolean method1(final Set<Integer> a) {
							return a.isEmpty();
						}
						public String method2(final X a) {
							return a.toString();
						}
					}"""
				},
				"\"" + OUTPUT_DIR +  File.separator + "X.java\" "
				+ " -failOnWarning"
				+ " -1.6 -warn:all-static-method -proc:none -d none",
				"",
				"""
					----------
					1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
						public boolean method1(final Set<Integer> a) {
						               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					The method method1(Set<Integer>) from the type X<T> can potentially be declared as static
					----------
					2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)
						public String method2(final X a) {
						              ^^^^^^^^^^^^^^^^^^
					The method method2(X) from the type X<T> can potentially be declared as static
					----------
					2 problems (2 warnings)
					error: warnings found and -failOnWarning specified
					""",
				true);
}
public void testIssue89_2() {
	this.runConformTest(
			new String[] {
				"X.java",
				"""
					import java.util.Set;
					public class X<T> {
					 @SuppressWarnings("static-method")\
						public boolean method1(final Set<Integer> a) {
							return a.isEmpty();
						}
					 @SuppressWarnings("static-method")\
						public String method2(final X a) {
							return a.toString();
						}
					}"""
				},
				"\"" + OUTPUT_DIR +  File.separator + "X.java\" "
				+ " -failOnWarning"
				+ " -1.6 -warn:all-static-method -proc:none -d none",
				"",
				"",
				true);
}
public void testIssue89_3() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Set;
					public class X<T> {
					 private final class InnerClass{}
						public boolean method1(final Set<Integer> a) {
							return a.isEmpty();
						}
						public String method2(final InnerClass i) {
							return i.toString();
						}
					}"""
				},
				"\"" + OUTPUT_DIR +  File.separator + "X.java\" "
				+ " -failOnWarning"
				+ " -1.6 -warn:all-static-method -proc:none -d none",
				"",
				"""
					----------
					1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
						public boolean method1(final Set<Integer> a) {
						               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					The method method1(Set<Integer>) from the type X<T> can potentially be declared as static
					----------
					1 problem (1 warning)
					error: warnings found and -failOnWarning specified
					""",
				true);
}
public void testIssue89_4() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Set;
					public class X<T> {
					 private final class InnerClass{}
						public boolean method1(final Set<Integer> a) {
							return a.isEmpty();
						}
						public InnerClass method2() {
							return null;
						}
					}"""
				},
				"\"" + OUTPUT_DIR +  File.separator + "X.java\" "
				+ " -failOnWarning"
				+ " -1.6 -warn:all-static-method -proc:none -d none",
				"",
				"""
					----------
					1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)
						public boolean method1(final Set<Integer> a) {
						               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					The method method1(Set<Integer>) from the type X<T> can potentially be declared as static
					----------
					1 problem (1 warning)
					error: warnings found and -failOnWarning specified
					""",
				true);
}
public void testIssue89_5() {
	this.runNegativeTest(
			new String[] {
				"X.java",
				"""
					import java.util.Set;
					public class X<T> {
					 private final class InnerClass{}\
						public boolean method1(final Set<InnerClass> a) {
							return a.isEmpty();
						}
					}"""
				},
				"\"" + OUTPUT_DIR +  File.separator + "X.java\" "
				+ " -failOnWarning"
				+ " -1.6 -warn:all-static-method -proc:none -d none",
				"",
				"""
					----------
					1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)
						private final class InnerClass{}	public boolean method1(final Set<InnerClass> a) {
						                                	               ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					The method method1(Set<X<T>.InnerClass>) from the type X<T> can potentially be declared as static
					----------
					1 problem (1 warning)
					error: warnings found and -failOnWarning specified
					""",
				true);
}

public void testGitHub316(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				void m() { (1+2) = 3; }\
				}"""},
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\"",
		"",
		"----------\n"
		+ "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)\n"
		+ "	void m() { (1+2) = 3; }}\n"
		+ "	           ^^^^^\n"
		+ "The left-hand side of an assignment must be a variable\n"
		+ "----------\n"
		+ "1 problem (1 error)\n"
		+ "",
		true);
}
public void testGitHub1122(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"""
				public class X {
				void m() {\
				.a() >0);\
				 }\
				}"""},
        "\"" + OUTPUT_DIR +  File.separator +
        	"X.java\"",
		"",
		"----------\n"
		+ "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)\n"
		+ "	void m() {.a() >0); }}\n"
		+ "	          ^\n"
		+ "Syntax error on token \".\", invalid (\n"
		+ "----------\n"
		+ "2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)\n"
		+ "	void m() {.a() >0); }}\n"
		+ "	                 ^\n"
		// XXX ASTParser instead reports "The left-hand side of an assignment must be a variable":
		+ "Syntax error, insert \"AssignmentOperator Expression\" to complete Expression\n"
		+ "----------\n"
		+ "2 problems (2 errors)\n"
		+ "",
		true);
}
}