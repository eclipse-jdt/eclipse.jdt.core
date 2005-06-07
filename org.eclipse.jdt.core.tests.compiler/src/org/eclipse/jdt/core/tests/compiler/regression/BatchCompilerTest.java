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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.Main;

public class BatchCompilerTest extends AbstractRegressionTest {
	public static final String OUTPUT_DIR_PLACEHOLDER = "---OUTPUT_DIR_PLACEHOLDER---";
	static final String JRE_HOME_DIR = Util.getJREDirectory();
	
public BatchCompilerTest(String name) {
	super(name);
}
public static Test suite() {
	if (false) {
		TestSuite suite = new TestSuite();
		suite.addTest(new BatchCompilerTest("test032"));
		return suite;
	}
	if (false) {
		TestSuite suite = new TestSuite();
		for (int i = 23; i < 27; i++) 
		  suite.addTest(new BatchCompilerTest("test0" + String.valueOf(i)));
		return suite;
	}
	return setupSuite(testClass());
	// TODO find a way to reduce the number of command line tests to one per 
	//      test run (aka do not add 1.3, 1.4, 1.5 supplementary level)
}

	/**
	 * Run a compilation test that is expected to complete successfully and
	 * compare the outputs to expected ones.
	 * 
	 * @param testFiles
	 *            the source files, given as a suite of file name, file content;
	 *            file names are relative to the output directory
	 * @param commandLine
	 *            the command line to pass to
	 *            {@link Main#compile(String) Main#compile}
	 * @param expectedSuccessOutOutputString
	 *            the expected contents of the standard output stream; pass null
	 *            to bypass the comparison
	 * @param expectedSuccessErrOutputString
	 *            the expected contents of the standard error output stream;
	 *            pass null to bypass the comparison
	 * @param shouldFlushOutputDirectory
	 *            pass true to get the output directory flushed before the test
	 *            runs
	 */
	protected void runConformTest(String[] testFiles, String commandLine,
			String expectedSuccessOutOutputString,
			String expectedSuccessErrOutputString,
			boolean shouldFlushOutputDirectory) {
		runTest(true, testFiles, commandLine, expectedSuccessOutOutputString,
				expectedSuccessErrOutputString, shouldFlushOutputDirectory);
	}

	/**
	 * Run a compilation test that is expected to fail and compare the outputs
	 * to expected ones.
	 * 
	 * @param testFiles
	 *            the source files, given as a suite of file name, file content;
	 *            file names are relative to the output directory
	 * @param commandLine
	 *            the command line to pass to
	 *            {@link Main#compile(String) Main#compile}
	 * @param expectedFailureOutOutputString
	 *            the expected contents of the standard output stream; pass null
	 *            to bypass the comparison
	 * @param expectedFailureErrOutputString
	 *            the expected contents of the standard error output stream;
	 *            pass null to bypass the comparison
	 * @param shouldFlushOutputDirectory
	 *            pass true to get the output directory flushed before the test
	 *            runs
	 */
	protected void runNegativeTest(String[] testFiles, String commandLine,
			String expectedFailureOutOutputString,
			String expectedFailureErrOutputString,
			boolean shouldFlushOutputDirectory) {
		runTest(false, testFiles, commandLine, expectedFailureOutOutputString,
				expectedFailureErrOutputString, shouldFlushOutputDirectory);
	}

	/**
	 * Worker method for runConformTest and runNegativeTest.
	 * 
	 * @param shouldCompileOK
	 *            set to true if the compiler should compile the given sources
	 *            without errors
	 * @param testFiles
	 *            the source files, given as a suite of file name, file content;
	 *            file names are relative to the output directory
	 * @param commandLine
	 *            the command line to pass to
	 *            {@link Main#compile(String) Main#compile}
	 * @param expectedOutOutputString
	 *            the expected contents of the standard output stream; pass null
	 *            to bypass the comparison
	 * @param expectedErrOutputString
	 *            the expected contents of the standard error output stream;
	 *            pass null to bypass the comparison
	 * @param shouldFlushOutputDirectory
	 *            pass true to get the output directory flushed before the test
	 *            runs
	 */
	private void runTest(boolean shouldCompileOK, String[] testFiles, String commandLine,
			String expectedOutOutputString,
			String expectedErrOutputString,
			boolean shouldFlushOutputDirectory) {
		File outputDirectory = new File(OUTPUT_DIR);
		if (shouldFlushOutputDirectory)
			Util.flushDirectoryContent(outputDirectory);
		try {
			if (!outputDirectory.isDirectory()) {
				outputDirectory.mkdirs();
			}
			PrintWriter sourceFileWriter;
			for (int i = 0; i < testFiles.length; i += 2) {
				String fileName = OUTPUT_DIR + File.separator + testFiles[i];
				File file = new File(fileName), innerOutputDirectory = file
						.getParentFile();
				if (!innerOutputDirectory.isDirectory()) {
					innerOutputDirectory.mkdirs();
				}
				sourceFileWriter = new PrintWriter(new FileOutputStream(file));
				sourceFileWriter.write(testFiles[i + 1]);
				sourceFileWriter.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		String printerWritersNameRoot = OUTPUT_DIR + File.separator + testName();
		String outFileName = printerWritersNameRoot + "out.txt", 
			   errFileName = printerWritersNameRoot + "err.txt";
		Main batchCompiler;
		try {
			batchCompiler = new Main(new PrintWriter(new FileOutputStream(
					outFileName)), new PrintWriter(new FileOutputStream(
					errFileName)), false);
		} catch (FileNotFoundException e) {
			System.out.println(getClass().getName() + '#' + getName());
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		boolean compileOK;
		try {
			final String[] tokenizeCommandLine = Main.tokenize(commandLine);
			compileOK = batchCompiler.compile(tokenizeCommandLine);
		} catch (RuntimeException e) {
			compileOK = false;
			System.out.println(getClass().getName() + '#' + getName());
			e.printStackTrace();
			throw e;
		}
		String outOutputString = Util.fileContent(outFileName), 
		       errOutputString = Util.fileContent(errFileName);
		boolean compareOK = false;
		if (compileOK == shouldCompileOK) {
			compareOK = semiNormalizedComparison(expectedOutOutputString,
					outOutputString, outputDirNormalizer)
					&& semiNormalizedComparison(expectedErrOutputString,
							errOutputString, outputDirNormalizer);
		}
		if (compileOK != shouldCompileOK || !compareOK) {
			System.out.println(getClass().getName() + '#' + getName());
			for (int i = 0; i < testFiles.length; i += 2) {
				System.out.print(testFiles[i]);
				System.out.println(" [");
				System.out.println(testFiles[i + 1]);
				System.out.println("]");
			}
		}
		if (compileOK != shouldCompileOK)
			System.out.println(errOutputString);
		if (compileOK == shouldCompileOK && !compareOK) {
			System.out.println(
					    "------------ [START OUT] ------------\n"
					+   "------------- Expected: -------------\n"
					+ expectedOutOutputString
					+ "\n------------- but was:  -------------\n"
					+ outOutputString
					+ "\n--------- (cut and paste:) ----------\n"
					+ Util.displayString(outputDirNormalizer
							.normalized(outOutputString))
					+ "\n------------- [END OUT] -------------\n"
					+   "------------ [START ERR] ------------\n"
					+   "------------- Expected: -------------\n"
					+ expectedErrOutputString
					+ "\n------------- but was:  -------------\n"
					+ errOutputString
					+ "\n--------- (cut and paste:) ----------\n"
					+ Util.displayString(outputDirNormalizer
							.normalized(errOutputString))
					+ "\n------------- [END ERR] -------------\n");
		}
		if (shouldCompileOK)
			assertTrue("Unexpected problems: " + errOutputString, compileOK);
		else
			assertTrue("Unexpected success: " + errOutputString, !compileOK);
		assertTrue("Unexpected output for invocation with arguments ["
				+ commandLine + "]:\n--[START]--\n" + outOutputString + "\n"
				+ errOutputString + "\n---[END]---\n", compareOK);
	}
	
	/**
	 * Abstract normalizer for output comparison. This class merely embodies a
	 * chain of responsibility, plus the signature of the method of interest
	 * here, that is {@link #normalized(String) normalized}.
	 */
	private static abstract class Normalizer {
		private Normalizer nextInChain;
		Normalizer(Normalizer nextInChain) {
			this.nextInChain = nextInChain;
		}
		String normalized(String originalValue) {
			String result;
			if (nextInChain == null)
				result = Util.convertToIndependantLineDelimiter(originalValue);
			else
				result = nextInChain.normalized(originalValue);
			return result;
		}
	}

	/**
	 * This normalizer replaces occurrences of a given string with a given
	 * placeholder.
	 */
	private static class StringNormalizer extends Normalizer {
		private String match;
		private int matchLength;
		private String placeholder;
		StringNormalizer(Normalizer nextInChain, String match, String placeholder) {
			super(nextInChain);
			this.match = match;
			this.matchLength = match.length();
			this.placeholder = placeholder;
		}
		String normalized(String originalValue) {
			String result;
			StringBuffer normalizedValueBuffer = new StringBuffer(originalValue);
			int nextOccurrenceIndex;
			while ((nextOccurrenceIndex = normalizedValueBuffer.indexOf(match)) != -1)
				normalizedValueBuffer.replace(nextOccurrenceIndex,
						nextOccurrenceIndex + matchLength, placeholder);
			result = super.normalized(normalizedValueBuffer.toString());
			return result;
		}
	}
	
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
		String normalized(String originalValue) {
			String result;
			StringBuffer normalizedValueBuffer = new StringBuffer(originalValue);
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
		private int first, number;

		LinesRangeNormalizer() {
			super(null);
			first = number = 0;
		}

		LinesRangeNormalizer(Normalizer nextInChain) {
			super(nextInChain);
			first = number = 0;
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
			first = firstLineToRemove;
			number = linesNumber >= 0 ? linesNumber : 0;
		}

		String normalized(String originalValue) {
			String result;
			if (number == 0 || originalValue.length() == 0)
				result = super.normalized(originalValue);
			else {
				final int START = 0, KEEPING = 1, KEEPING_R = 2, SKIPING = 3, SKIPING_R = 4, END = 5, ERROR = 6;
				int state = START, currentLineIndex = 0, currentCharIndex = 0, sourceLength;
				char currentChar = '\0';
				if (first <= 0)
					state = SKIPING;
				else
					state = KEEPING;
				StringBuffer normalizedValueBuffer = new StringBuffer(), source = new StringBuffer(
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
								if (currentLineIndex == first) {
									state = SKIPING;
								}
								break;
							case SKIPING: // tolerate Linux line delimiters
							case SKIPING_R:
								// in effect, we tolerate too big first and number
								// values
								if (currentLineIndex >= first + number) {
									if (currentCharIndex < sourceLength)
										normalizedValueBuffer.append(source
												.substring(currentCharIndex));
									state = END;
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
	 * Normalizer instance that replaces occurrences of OUTPUT_DIR with 
	 * OUTPUT_DIR_PLACEHOLDER.
	 */
	private static Normalizer outputDirNormalizer = new StringNormalizer(null,
			OUTPUT_DIR, OUTPUT_DIR_PLACEHOLDER);

	/**
	 * Normalizer instance for non XML log files. 
	 */
	private static Normalizer textLogsNormalizer = new StringNormalizer(
			new XMLClasspathsSectionNormalizer(new LinesRangeNormalizer(null,
					0, 1)), OUTPUT_DIR, OUTPUT_DIR_PLACEHOLDER);

	/**
	 * Normalizer instance for XML log files.
	 */
	private static Normalizer xmlLogsNormalizer = new StringNormalizer(
			new XMLClasspathsSectionNormalizer(new LinesRangeNormalizer(null,
					1, 1)), OUTPUT_DIR, OUTPUT_DIR_PLACEHOLDER);
	

	/**
	 * Return true if and only if the two strings passed as parameters compare
	 * equal, modulo the transformation of the second string by a normalizer
	 * passed in parameter. This is meant to erase the variations of subparts of
	 * the compared strings in function of the test machine, the user account,
	 * etc.
	 * 
	 * @param keep
	 *            the first string to compare, gets compared as it is
	 * @param normalize
	 *            the second string to compare, passed through the normalizer
	 *            before comparison
	 * @param normalizer
	 *            the transformation applied to normalize
	 * @return true if keep and normalize compare equal after normalize has been
	 *         normalized
	 */
	private boolean semiNormalizedComparison(String keep, String normalize,
			Normalizer normalizer) {
		if (keep == null)
			return normalize == null;
		if (normalize == null)
			return false;
		// return keep.equals(normalizer.normalized(normalize));
		return equals(keep, normalizer.normalized(normalize));
	}
private static boolean equals(String a, String b) {
	StringBuffer aBuffer = new StringBuffer(a), bBuffer = new StringBuffer(b);
	int length = aBuffer.length();
	if (length != bBuffer.length()) {
		System.err.println("a and b lengths differ");
		return false;
	}
	for (int i = 0; i < length; i++)
		if (aBuffer.charAt(i) != bBuffer.charAt(i)) {
			int beforeStart = i - 5, beforeEnd = i - 1, afterStart = i + 1, afterEnd = i + 5;
			if (beforeStart < 0) {
				beforeStart = 0;
				if (beforeEnd < 0)
					beforeEnd = 0;
			}
			if (afterEnd >= length) {
				afterEnd = length - 1;
				if (afterStart >= length) 
					afterStart = length - 1;
			}
			System.err.println("a and b differ at rank: " + i 
					+ " a: ..." + aBuffer.substring(beforeStart, beforeEnd) 
						+ "<" + aBuffer.charAt(i) + ">"
						+ aBuffer.substring(afterStart, afterEnd) + "..." 
					+ " b: ..." + bBuffer.substring(beforeStart, beforeEnd) 
						+ "<" + bBuffer.charAt(i) + ">"
						+ bBuffer.substring(afterStart, afterEnd) + "..."); 
			return false;
		}
	return true;
}

public void test001() {
	
		String commandLine = "-classpath \"D:/a folder\";d:/jdk1.4/jre/lib/rt.jar -1.4 -preserveAllLocals -g -verbose d:/eclipse/workspaces/development2.0/plugins/Bar/src2/ -d d:/test";
		String expected = " <-classpath> <D:/a folder;d:/jdk1.4/jre/lib/rt.jar> <-1.4> <-preserveAllLocals> <-g> <-verbose> <d:/eclipse/workspaces/development2.0/plugins/Bar/src2/> <-d> <d:/test>";
		
		String[] args = Main.tokenize(commandLine);
		StringBuffer  buffer = new StringBuffer(30);
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
		StringBuffer  buffer = new StringBuffer(30);
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
		StringBuffer  buffer = new StringBuffer(30);
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
		StringBuffer  buffer = new StringBuffer(30);
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
		StringBuffer  buffer = new StringBuffer(30);
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
		StringBuffer  buffer = new StringBuffer(30);
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
public void _test007(){
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" + 
			"\n" + 
			"@SuppressWarnings(\"all\"//$NON-NLS-1$\n" + 
			")\n" + 
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		if (false) {\n" + 
			"			;\n" + 
			"		} else {\n" + 
			"		}\n" + 
			"		// Zork z;\n" + 
			"	}\n" + 
			"}"
        },
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -bootclasspath " + JRE_HOME_DIR + "/lib/rt.jar"
        + " -cp " + JRE_HOME_DIR + "/lib/jce.jar"
        + " -verbose -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "[1 .class file generated]\n", 
        "----------\n" + 
        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\n" + 
        " (at line 1)\n" + 
        "	import java.util.List;\n" + 
        "	       ^^^^^^^^^^^^^^\n" + 
        "The import java.util.List is never used\n" + 
        "----------\n" + 
        "1 problem (1 warning)", true);
}
// test the tester - runNegativeTest
public void _test008(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" + 
			"\n" + 
			"@SuppressWarnings(\"all\"//$NON-NLS-1$\n" + 
			")\n" + 
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		if (false) {\n" + 
			"			;\n" + 
			"		} else {\n" + 
			"		}\n" + 
			"		Zork z;\n" + 
			"	}\n" + 
			"}"
        },
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -bootclasspath " + JRE_HOME_DIR + "/lib/rt.jar"
        + " -cp " + JRE_HOME_DIR + "/lib/jce.jar"
        + " -verbose -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "[1 .class file generated]\n", 
        "----------\n" + 
        "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\n" + 
        " (at line 11)\n" + 
        "	Zork z;\n" + 
        "	^^^^\n" + 
        "Zork cannot be resolved to a type\n" + 
        "----------\n" + 
        "1 problem (1 error)", true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=92398 -- a case that works, another that does not
// revisit this test case depending on https://bugs.eclipse.org/bugs/show_bug.cgi?id=95349
public void _test009(){
	this.runNegativeTest(
		new String[] {
			"X.java",
			"/** */\n" + 
			"public class X {\n" + 
			"	OK1 ok1;\n" + 
			"	OK2 ok2;\n" + 
			"	Warn warn;\n" + 
			"	KO ko;\n" + 
	        "	Zork z;\n" + 
			"}",
			"OK1.java",
			"/** */\n" + 
			"public class OK1 {\n" + 
			"	// empty\n" + 
			"}",
			"OK2.java",
			"/** */\n" + 
			"public class OK2 {\n" + 
			"	// empty\n" + 
			"}",
			"Warn.java",
			"/** */\n" + 
			"public class Warn {\n" + 
			"	// empty\n" + 
			"}",
			"KO.java",
			"/** */\n" + 
			"public class KO {\n" + 
			"	// empty\n" + 
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[+OK2" + File.pathSeparator + "~Warn" 
        	+ File.pathSeparator + "-KO]\""
        + " -verbose -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "[5 .class files generated]\n", 
        "----------\n" + 
        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\n" + 
        " (at line 5)\n" + 
        "	Warn warn;\n" + 
        "	^^^^\n" + 
        "Discouraged access: Warn\n" + 
        "----------\n" + 
        "----------\n" + 
        "2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\n" + 
        " (at line 6)\n" + 
        "	KO ko;\n" + 
        "	^^\n" + 
        "Access restriction: KO\n" + 
        "----------\n" + 
        "----------\n" + 
        "3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\n" + 
        " (at line 7)\n" + 
        "	Zork z;\n" + 
        "	^^^^\n" + 
        "Zork cannot be resolved to a type\n" + 
        "----------\n" + 
        "3 problems (1 error, 2 warnings)",
        true);
}
// command line - no user classpath nor bootclasspath
public void _test010(){
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.util.List;\n" + 
			"\n" + 
			"@SuppressWarnings(\"all\"//$NON-NLS-1$\n" + 
			")\n" + 
			"public class X {\n" + 
			"	public static void main(String[] args) {\n" + 
			"		if (false) {\n" + 
			"			;\n" + 
			"		} else {\n" + 
			"		}\n" + 
			"		// Zork z;\n" + 
			"	}\n" + 
			"}"
        },
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -verbose -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "[1 .class file generated]\n", 
        "----------\n" + 
        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\n" + 
        " (at line 1)\n" + 
        "	import java.util.List;\n" + 
        "	       ^^^^^^^^^^^^^^\n" + 
        "The import java.util.List is never used\n" + 
        "----------\n" + 
        "1 problem (1 warning)", true);
}
// command line - unusual classpath (ends with ';', still OK)
public void test011(){
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" + 
			"public class X {\n" + 
			"}",
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
// command line - help
public void _test012(){
	final String expectedOutput =
        "{0}\n" + 
        " \n" + 
        " Usage: <options> <source files | directories>\n" + 
        " If directories are specified, then their source contents are compiled.\n" + 
        " Possible options are listed below. Options enabled by default are prefixed with \'\'+\'\'\n" + 
        " \n" + 
        " Classpath options:\n" + 
        "    -cp -classpath <directories and zip/jar files separated by {1}>\n" + 
        "                       specify location for application classes and sources. Each\n" + 
        "                       directory or file can specify access rules for types between\n" + 
        "                       \'\'[\'\' and \'\']\'\' (e.g. [-X.java] to deny access to type X)\n" + 
        "    -bootclasspath <directories and zip/jar files separated by {1}>\n" + 
        "                       specify location for system classes. Each directory or file can\n" + 
        "                       specify access rules for types between \'\'[\'\' and \'\']\'\' (e.g. [-X.java]\n" + 
        "                       to deny access to type X)\n" + 
        "    -sourcepath <directories and zip/jar files separated by {1}>\n" + 
        "                       specify location for application sources. Each directory or file can\n" + 
        "                       specify access rules for types between \'\'[\'\' and \'\']\'\' (e.g. [-X.java]\n" + 
        "                       to deny access to type X)\n" + 
        "    -extdirs <directories separated by {1}>\n" + 
        "                       specify location for extension zip/jar files\n" + 
        "    -d <dir>           destination directory (if omitted, no directory is created)\n" + 
        "    -d none            generate no .class files\n" + 
        "    -encoding <enc>    specify custom encoding for all sources. Each file/directory can override it\n" + 
        "                       when suffixed with \'\'[\'\'<enc>\'\']\'\' (e.g. X.java[utf8])\n" + 
        " \n" + 
        " Compliance options:\n" + 
        "    -1.3               use 1.3 compliance level (implicit -source 1.3 -target 1.1)\n" + 
        "    -1.4             + use 1.4 compliance level (implicit -source 1.3 -target 1.2)\n" + 
        "    -1.5               use 1.5 compliance level (implicit -source 1.5 -target 1.5)\n" + 
        "    -source <version>  set source level: 1.3 to 1.5 (or 5 or 5.0)\n" + 
        "    -target <version>  set classfile target level: 1.1 to 1.5 (or 5 or 5.0)\n" + 
        " \n" + 
        " Warning options:\n" + 
        "    -deprecation     + deprecation outside deprecated code\n" + 
        "    -nowarn            disable all warnings\n" + 
        "    -warn:none         disable all warnings\n" + 
        "    -warn:<warnings separated by ,>    enable exactly the listed warnings\n" + 
        "    -warn:+<warnings separated by ,>   enable additional warnings\n" + 
        "    -warn:-<warnings separated by ,>   disable specific warnings\n" + 
        "      allDeprecation       deprecation including inside deprecated code\n" + 
        "      allJavadoc           invalid or missing javadoc\n" + 
        "      assertIdentifier   + \'\'assert\'\' used as identifier\n" + 
        "      boxing               autoboxing conversion\n" + 
        "      charConcat         + char[] in String concat\n" + 
        "      conditionAssign      possible accidental boolean assignment\n" + 
        "      constructorName    + method with constructor name\n" + 
        "      dep-ann              missing @Deprecated annotation\n" + 
        "      deprecation        + deprecation outside deprecated code\n" + 
        "      emptyBlock           undocumented empty block\n" + 
        "      enumSwitch           incomplete enum switch\n" + 
        "      fieldHiding          field hiding another variable\n" + 
        "      finalBound           type parameter with final bound\n" + 
        "      finally            + finally block not completing normally\n" + 
        "      indirectStatic       indirect reference to static member\n" + 
        "      intfAnnotation     + annotation type used as super interface\n" + 
        "      intfNonInherited   + interface non-inherited method compatibility\n" + 
        "      javadoc              invalid javadoc\n" + 
        "      localHiding          local variable hiding another variable\n" + 
        "      maskedCatchBlock   + hidden catch block\n" + 
        "      nls                  string literal lacking non-nls tag //$NON-NLS-<n>$\n" + 
        "      noEffectAssign     + assignment without effect\n" + 
        "      null                 missing or redundant null check\n" + 
        "      over-ann             missing @Override annotation\n" + 
        "      pkgDefaultMethod   + attempt to override package-default method\n" + 
        "      semicolon            unnecessary semicolon, empty statement\n" + 
        "      serial             + missing serialVersionUID\n" + 
        "      suppress           + enable @SuppressWarnings\n" + 
        "      unqualifiedField     unqualified reference to field\n" + 
        "      unchecked          + unchecked type operation\n" + 
        "      unusedArgument       unread method parameter\n" + 
        "      unusedImport       + unused import declaration\n" + 
        "      unusedLocal          unread local variable\n" + 
        "      unusedPrivate        unused private member declaration\n" + 
        "      unusedThrown         unused declared thrown exception\n" + 
        "      unnecessaryElse      unnecessary else clause\n" + 
        "      uselessTypeCheck     unnecessary cast/instanceof operation\n" + 
        "      specialParamHiding   constructor or setter parameter hiding another field\n" + 
        "      staticReceiver     + non-static reference to static member\n" + 
        "      syntheticAccess      synthetic access for innerclass\n" + 
        "      tasks(<tags separated by |>) tasks identified by tags inside comments\n" + 
        "      typeHiding         + type parameter hiding another type\n" + 
        "      varargsCast        + varargs argument need explicit cast\n" + 
        "      warningToken       + unhandled warning token in @SuppressWarnings\n" + 
        " \n" + 
        " Debug options:\n" + 
        "    -g[:lines,vars,source] custom debug info\n" + 
        "    -g:lines,source  + both lines table and source debug info\n" + 
        "    -g                 all debug info\n" + 
        "    -g:none            no debug info\n" + 
        "    -preserveAllLocals preserve unused local vars for debug purpose\n" + 
        " \n" + 
        " Ignored options:\n" + 
        "    -J<option>         pass option to virtual machine (ignored)\n" + 
        "    -X<option>         specify non-standard option (ignored)\n" + 
        "    -X                 print non-standard options and exit (ignored)\n" + 
        "    -O                 optimize for execution time (ignored)\n" + 
        " \n" + 
        " Advanced options:\n" + 
        "    @<file>            read command line arguments from file\n" + 
        "    -maxProblems <n>   max number of problems per compilation unit (100 by default)\n" + 
        "    -log <file>        log to a file\n" + 
        "    -proceedOnError    do not stop at first error, dumping class files with problem methods\n" + 
        "    -verbose           enable verbose output\n" + 
        "    -referenceInfo     compute reference info\n" + 
        "    -progress          show progress (only in -log mode)\n" + 
        "    -time              display speed information \n" + 
        "    -noExit            do not call System.exit(n) at end of compilation (n==0 if no error)\n" + 
        "    -repeat <n>        repeat compilation process <n> times for perf analysis\n" + 
        "    -inlineJSR         inline JSR bytecode (implicit if target >= 1.5)\n" + 
        "    -enableJavadoc     consider references in javadoc\n" + 
        " \n" + 
        "    -? -help           print this help message\n" + 
        "    -v -version        print compiler version\n" + 
        "    -showversion       print compiler version and continue\n" + 
        "\n";

	this.runConformTest(
		new String[0],
        " -help -showversion -referenceInfo",
		MessageFormat.format(expectedOutput, new String[] {
				Main.bind("misc.version", new String[] {
					Main.bind("compiler.name"),
					Main.bind("compiler.version"),
					Main.bind("compiler.copyright")
				}),
				File.pathSeparator
		}),
        "", true);
}

	// command line - xml log contents https://bugs.eclipse.org/bugs/show_bug.cgi?id=93904
	public void _test013() {
		String logFileName = OUTPUT_DIR + File.separator + "log.xml";
		this.runNegativeTest(new String[] { 
				"X.java",
				"/** */\n" + 
				"public class X {\n" + 
				"	Zork z;\n" + 
				"}", },
				"\"" + OUTPUT_DIR + File.separator + "X.java\""
				+ " -1.5 -proceedOnError"
				+ " -log \"" + logFileName + "\" -d \"" + OUTPUT_DIR + "\"",
				"", 
				"----------\n" + 
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\n" + 
				" (at line 3)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"1 problem (1 error)", 
				true);
		String logContents = Util.fileContent(logFileName);
		String expectedLogContents = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
			"<!DOCTYPE compiler SYSTEM \"compiler.dtd\">\n" + 
			"<compiler name=\"Eclipse Java Compiler\" copyright=\"Copyright IBM Corp 2000, 2005. All rights reserved.\" version=\"{1}\">\n" + 
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
			"		<option key=\"org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.codegen.targetPlatform\" value=\"1.5\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.codegen.unusedLocal\" value=\"optimize out\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.compliance\" value=\"1.5\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.debug.lineNumber\" value=\"generate\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.debug.localVariable\" value=\"do not generate\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.debug.sourceFile\" value=\"generate\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.doc.comment.support\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.maxProblemPerUnit\" value=\"100\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.annotationSuperInterface\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.assertIdentifier\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.autoboxing\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.deprecation\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.deprecationInDeprecatedCode\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.deprecationWhenOverridingDeprecatedMethod\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.discouragedReference\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.emptyStatement\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.enumIdentifier\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.fieldHiding\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.finalParameterBound\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.finallyBlockNotCompletingNormally\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.forbiddenReference\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.hiddenCatchBlock\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.incompatibleNonInheritedInterfaceMethod\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.incompleteEnumSwitch\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.indirectStaticAccess\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadoc\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadocTags\" value=\"enabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsDeprecatedRef\" value=\"enabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsNotVisibleRef\" value=\"enabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsVisibility\" value=\"private\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.localVariableHiding\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.methodWithConstructorName\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingDeprecatedAnnotation\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocComments\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocCommentsOverriding\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocCommentsVisibility\" value=\"public\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocTags\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocTagsOverriding\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocTagsVisibility\" value=\"private\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingOverrideAnnotation\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingSerialVersion\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.noEffectAssignment\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.noImplicitStringConversion\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.nullReference\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.overridingPackageDefaultMethod\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.possibleAccidentalBooleanAssignment\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.specialParameterHidingField\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.staticAccessReceiver\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.suppressWarnings\" value=\"enabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.syntheticAccessEmulation\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.typeParameterHiding\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.uncheckedTypeOperation\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.undocumentedEmptyBlock\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unhandledWarningToken\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unnecessaryElse\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unnecessaryTypeCheck\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unqualifiedFieldAccess\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownException\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionWhenOverriding\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedImport\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedLocal\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedParameter\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedParameterWhenImplementingAbstract\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedParameterWhenOverridingConcrete\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedPrivateMember\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.varargsArgumentNeedCast\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.source\" value=\"1.5\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.taskCaseSensitive\" value=\"enabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.taskPriorities\" value=\"\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.taskTags\" value=\"\"/>\n" + 
			"	</options>\n" + 
			"	<classpaths>NORMALIZED SECTION</classpaths>\n" + 
			"	<sources>\n" + 
			"		<source path=\"---OUTPUT_DIR_PLACEHOLDER---{0}X.java\">\n" + 
			"			<problems problems=\"1\" errors=\"1\" warnings=\"0\">\n" + 
			"				<problem charEnd=\"28\" charStart=\"25\" severity=\"ERROR\" line=\"3\" id=\"UndefinedType\">\n" + 
			"					<message value=\"Zork cannot be resolved to a type\"/>\n" + 
			"					<source_context value=\"Zork z;\" sourceStart=\"0\" sourceEnd=\"3\"/>\n" + 
			"					<arguments>\n" + 
			"						<argument value=\"Zork\"/>\n" + 
			"					</arguments>\n" + 
			"				</problem>\n" + 
			"			</problems>\n" + 
			"			<classfile path=\"---OUTPUT_DIR_PLACEHOLDER---{0}X.class\"/>\n" + 
			"		</source>\n" + 
			"	</sources>\n" + 
			"	<stats>\n" + 
			"		<problem_summary problems=\"1\" errors=\"1\" warnings=\"0\" tasks=\"0\"/>\n" + 
			"	</stats>\n" + 
			"</compiler>\n";
		boolean compareOK = semiNormalizedComparison(
				MessageFormat.format(
						expectedLogContents,
						new String[] {
								File.separator,
								Main.bind("compiler.version")
						}),
				logContents, xmlLogsNormalizer);
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
		}
		assertTrue("unexpected log contents", compareOK);
	}

	// command line - txt log contents https://bugs.eclipse.org/bugs/show_bug.cgi?id=93904
	public void test014() {
		String logFileName = OUTPUT_DIR + File.separator + "log.txt";
		this.runNegativeTest(new String[] { 
				"X.java",
				"/** */\n" + 
				"public class X {\n" + 
				"	Zork z;\n" + 
				"}", },
				"\"" + OUTPUT_DIR + File.separator + "X.java\"" 
				+ " -1.5 -proceedOnError"
				+ " -log \"" + logFileName + "\" -d \"" + OUTPUT_DIR + "\"",
				"", 
				"----------\n" + 
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\n" + 
				" (at line 3)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"1 problem (1 error)", 
				false);
		String logContents = Util.fileContent(logFileName);
		String expectedLogContents = 
			"----------\n" + 
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\n" + 
			" (at line 3)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"1 problem (1 error)";
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
				"/** */\n" + 
				"public class X {\n" + 
				"	Zork z;\n" + 
				"}", },
				"\"" + OUTPUT_DIR + File.separator + "X.java\""
				+ " -1.5 -proceedOnError"
				+ " -log \"" + logFileName + "\" -d \"" + OUTPUT_DIR + "\"",
				"", 
				"----------\n" + 
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\n" + 
				" (at line 3)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"1 problem (1 error)", 
				false);
		String logContents = Util.fileContent(logFileName);
		String expectedLogContents = 
			"----------\n" + 
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\n" + 
			" (at line 3)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"1 problem (1 error)";
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
public void _test016(){
		this.runConformTest(
			new String[] {
					"X.java",
					"/** */\n" + 
					"public class X {\n" + 
					"	OK1 ok1;\n" + 
					"}",
					"OK1.java",
					"/** */\n" + 
					"public class OK1 {\n" + 
					"	// empty\n" + 
					"}"
			},
	        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -cp ." + File.pathSeparator + File.pathSeparator + File.pathSeparator + "\"" + OUTPUT_DIR + "\""
	        + " -verbose -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\"",
	        "[2 .class files generated]\n",
	        "",
	        true);
	}
public void _test017(){
		this.runConformTest(
			new String[] {
					"X.java",
					"/** */\n" + 
					"public class X {\n" + 
					"	OK1 ok1;\n" + 
					"}",
					"OK1.java",
					"/** */\n" + 
					"public class OK1 {\n" + 
					"	// empty\n" + 
					"}"
			},
	        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -cp dummmy_dir" + File.pathSeparator + "dummy.jar" + File.pathSeparator + File.pathSeparator + "\"" + OUTPUT_DIR + "\"" 
	        + " -verbose -proceedOnError -referenceInfo" 
	        + " -d \"" + OUTPUT_DIR + "\"",
	        "[2 .class files generated]\n",
	        "incorrect classpath: dummmy_dir\n" + 
	        "incorrect classpath: dummy.jar\n" + 
	        "incorrect classpath: dummy.jar\n",
	        true);
	}
// command line - unusual classpath (empty, but using current directory, still OK provided 
//	that we execute from the appropriate directory)
	public void _test018(){
		this.runConformTest(
			new String[] {
					"X.java",
					"/** */\n" + 
					"public class X {\n" + 
					"	OK1 ok1;\n" + 
					"}",
					"OK1.java",
					"/** */\n" + 
					"public class OK1 {\n" + 
					"	// empty\n" + 
					"}"
			},
	        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -verbose -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\"",
	        "[2 .class files generated]\n",
	        "",
	        true);
	}
public void _test019(){
		this.runNegativeTest(
			new String[] {
				"X.java",
				"/** */\n" + 
				"public class X {\n" + 
				"	OK1 ok1;\n" + 
				"	OK2 ok2;\n" + 
				"	Warn warn;\n" + 
				"	KO ko;\n" + 
		        "	Zork z;\n" + 
				"}",
				"OK1.java",
				"/** */\n" + 
				"public class OK1 {\n" + 
				"	// empty\n" + 
				"}",
				"OK2.java",
				"/** */\n" + 
				"public class OK2 {\n" + 
				"	// empty\n" + 
				"}",
				"Warn.java",
				"/** */\n" + 
				"public class Warn {\n" + 
				"	// empty\n" + 
				"}",
				"KO.java",
				"/** */\n" + 
				"public class KO {\n" + 
				"	// empty\n" + 
				"}",
			},
	        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
	        + " -1.5 -g -preserveAllLocals" 
	        + " -cp \"" + OUTPUT_DIR + "[+OK2" + File.pathSeparator + "~Warn" + File.pathSeparator + "-KO]\""
	        + " -verbose -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal" 
	        + " -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\"", 
	        "[5 .class files generated]\n", 
	        "----------\n" + 
	        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\n" + 
	        " (at line 5)\n" + 
	        "	Warn warn;\n" + 
	        "	^^^^\n" + 
	        "Discouraged access: Warn\n" + 
	        "----------\n" + 
	        "----------\n" + 
	        "2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\n" + 
	        " (at line 6)\n" + 
	        "	KO ko;\n" + 
	        "	^^\n" + 
	        "Access restriction: KO\n" + 
	        "----------\n" + 
	        "----------\n" + 
	        "3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\n" + 
	        " (at line 7)\n" + 
	        "	Zork z;\n" + 
	        "	^^^^\n" + 
	        "Zork cannot be resolved to a type\n" + 
	        "----------\n" + 
	        "3 problems (1 error, 2 warnings)",
	        true);
	}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - skip options -O -Jxxx and -Xxxx, multiple times if needed
	public void _test020(){
		this.runConformTest(
			new String[] {
					"X.java",
					"/** */\n" + 
					"public class X {\n" + 
					"}",
			},
	        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -verbose -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\" -O -Xxxx -O -Jxyz -Xtyu -Jyu",
	        "[1 .class file generated]\n",
	        "",
	        true);
	}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - -sourcepath finds additional source files
	public void _test021(){
		this.runConformTest(
			new String[] {
					"src1/X.java",
					"/** */\n" + 
					"public class X {\n" + 
					"}",
					"src2/Y.java",
					"/** */\n" + 
					"public class Y extends X {\n" + 
					"}",
			},
	        "\"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src1\"" 
			  + File.pathSeparator + "\"" + OUTPUT_DIR +  File.separator + "src2\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -verbose -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\" ",
	        "[2 .class files generated]\n",
	        "",
	        true);
	}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - repeated -sourcepath fails - even if the error is more
// explicit here than what javac does
	public void test022(){
		this.runNegativeTest(
			new String[] {
					"src1/X.java",
					"/** */\n" + 
					"public class X {\n" + 
					"}",
					"src2/Y.java",
					"/** */\n" + 
					"public class Y extends X {\n" + 
					"}",
			},
			" -sourcepath \"" + OUTPUT_DIR +  File.separator + "src1\"" 
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src2\"" 
	        + " \"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -verbose -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\" ",
	        "",
	        "duplicate sourcepath specification: -sourcepath\n",
	        true);
	}
//	 https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - repeated -extdirs fails
		public void test023(){
			this.runNegativeTest(
				new String[] {
						"src1/X.java",
						"/** */\n" + 
						"public class X {\n" + 
						"}",
						"src2/Y.java",
						"/** */\n" + 
						"public class Y extends X {\n" + 
						"}",
				},
				" -extdirs \"" + OUTPUT_DIR +  File.separator + "src1\"" 
				+ " -extdirs \"" + OUTPUT_DIR +  File.separator + "src2\"" 
		        + " \"" + OUTPUT_DIR +  File.separator + "src1" + File.separator + "X.java\""
		        + " -1.5 -g -preserveAllLocals"
		        + " -verbose -proceedOnError -referenceInfo"
		        + " -d \"" + OUTPUT_DIR + "\" ",
		        "",
		        "duplicate extdirs specification: -extdirs\n",
		        true);
		}
//	 https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - explicit empty -extdirs removes extensions
		public void _test024(){
			this.runNegativeTest(
				new String[] {
						"X.java",
						"/** */\n" + 
						"public class X {\n" + 
						"  sun.net.spi.nameservice.dns.DNSNameService dummy;\n" + 
						"}",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
				+ " -extdirs \"\"" 
		        + " -1.5 -g -preserveAllLocals"
		        + " -verbose -proceedOnError -referenceInfo"
		        + " -d \"" + OUTPUT_DIR + "\" ",
		        "[1 .class file generated]\n",
		        "----------\n" + 
		        "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\n" + 
		        " (at line 3)\n" + 
		        "	sun.net.spi.nameservice.dns.DNSNameService dummy;\n" + 
		        "	^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" + 
		        "sun.net.spi.nameservice.dns cannot be resolved to a type\n" + 
		        "----------\n" + 
		        "1 problem (1 error)",
		        true);
		}
//	 https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - cumulative -extdirs extends the classpath
		public void _test025(){
			this.runConformTest(
				new String[] {
						"src1/X.java",
						"/** */\n" + 
						"public class X {\n" + 
						"  sun.net.spi.nameservice.dns.DNSNameService dummy;\n" + 
						"}",
						"src2/Y.java",
						"/** */\n" + 
						"public class Y extends X {\n" + 
						"}",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
				+ " -extdirs \"" + JRE_HOME_DIR + "/lib/ext" + File.pathSeparator + OUTPUT_DIR +  File.separator + "src1\"" 
				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src1\"" 
		        + " -1.5 -g -preserveAllLocals"
		        + " -verbose -proceedOnError -referenceInfo"
		        + " -d \"" + OUTPUT_DIR + "\" ",
		        "[2 .class files generated]\n",
		        "",
		        true);
		}
//	 https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - -extdirs extends the classpath before -classpath
		public void _test026(){
			this.runConformTest(
				new String[] {
						"src1/X.java",
						"/** */\n" + 
						"public class X {\n" + 
						"}",
						"src2/Y.java",
						"/** */\n" + 
						"public class Y extends X {\n" + 
						"}",
						"src3/X.java",
						"/** */\n" + 
						"public class X {\n" + 
						"  Zork error;\n" + 
						"}",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
				+ " -classpath \"" + OUTPUT_DIR +  File.separator + "src3\"" 
				+ " -extdirs \"" + JRE_HOME_DIR + "/lib/ext" + File.pathSeparator + OUTPUT_DIR +  File.separator + "src1\"" 
				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src2" + File.pathSeparator + OUTPUT_DIR +  File.separator + "src1\"" 
		        + " -1.5 -g -preserveAllLocals"
		        + " -verbose -proceedOnError -referenceInfo"
		        + " -d \"" + OUTPUT_DIR + "\" ",
		        "[2 .class files generated]\n",
				"",
		        true);
		}
		
public void _test027(){
			this.runNegativeTest(
				new String[] {
					"X.java",
					"/** */\n" + 
					"public class X {\n" + 
					"	OK1 ok1;\n" + 
					"	OK2 ok2;\n" + 
					"	Warn warn;\n" + 
					"	KO ko;\n" + 
			        "	Zork z;\n" + 
					"}",
					"OK1.java",
					"/** */\n" + 
					"public class OK1 {\n" + 
					"	// empty\n" + 
					"}",
					"OK2.java",
					"/** */\n" + 
					"public class OK2 {\n" + 
					"	// empty\n" + 
					"}",
					"p1/Warn.java",
					"/** */\n" + 
					"public class Warn {\n" + 
					"	// empty\n" + 
					"}",
					"KO.java",
					"/** */\n" + 
					"public class KO {\n" + 
					"	// empty\n" + 
					"}",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		        + " -1.5 -g -preserveAllLocals"
		        + " -cp \"" + OUTPUT_DIR + "[+OK2" + File.pathSeparator + "-KO]" + File.pathSeparator
		        + OUTPUT_DIR + File.separator + "p1[~Warn]\""
		        + " -verbose -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
		        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
		        "[5 .class files generated]\n", 
		        "----------\n" + 
		        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\n" + 
		        " (at line 5)\n" + 
		        "	Warn warn;\n" + 
		        "	^^^^\n" + 
		        "Discouraged access: Warn\n" + 
		        "----------\n" + 
		        "----------\n" + 
		        "2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\n" + 
		        " (at line 6)\n" + 
		        "	KO ko;\n" + 
		        "	^^\n" + 
		        "Access restriction: KO\n" + 
		        "----------\n" + 
		        "----------\n" + 
		        "3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\n" + 
		        " (at line 7)\n" + 
		        "	Zork z;\n" + 
		        "	^^^^\n" + 
		        "Zork cannot be resolved to a type\n" + 
		        "----------\n" + 
		        "3 problems (1 error, 2 warnings)",
		        true);
		}
public void test028(){
			this.runConformTest(
				new String[] {
					"src1/X.java",
					"/** */\n" + 
					"public class X {\n" + 
					"}",
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
					"/** */\n" + 
					"public class Y extends X {\n" + 
					"}",
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
			"public interface X<T extends X<T, K, S>, \n" + 
			"                   K extends X.K<T, S>, \n" + 
			"                   S extends X.S> {\n" + 
			"	public interface K<KT extends X<KT, ?, KS>, \n" + 
			"	                   KS extends X.S> {\n" + 
			"	}\n" + 
			"	public interface S {\n" + 
			"	}\n" + 
			"}\n",
			"Y.java",
			"public class Y<T extends X<T, K, S>, \n" + 
			"               K extends X.K<T, S>, \n" + 
			"               S extends X.S> { \n" + 
			"}\n",
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
			"public interface X<T extends X<T, K, S>, \n" + 
			"                   K extends X.K<T, S>, \n" + 
			"                   S extends X.S> {\n" + 
			"	public interface K<KT extends X<KT, ?, KS>, \n" + 
			"	                   KS extends X.S> {\n" + 
			"	}\n" + 
			"	public interface S {\n" + 
			"	}\n" + 
			"}\n",
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
			"public class Y<T extends X<T, K, S>, \n" + 
			"               K extends X.K<T, S>, \n" + 
			"               S extends X.S> { \n" + 
			"}\n",
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
				"package p;\n" + 
				"import java.io.Serializable;\n" + 
				"public interface X<T extends X<T, U, V>, \n" + 
				"				   U extends X.XX<T, V>, \n" + 
				"				   V extends X.XY> {\n" + 
				"	public interface XX<TT extends X<TT, ?, UU>, \n" + 
				"	                    UU extends X.XY> \n" + 
				"			extends	Serializable {\n" + 
				"	}\n" + 
				"	public interface XY extends Serializable {\n" + 
				"	}\n" + 
				"}\n",
				"p/Y.java",
				"package p;\n" + 
				"import java.util.*;\n" + 
				"import p.X.*;\n" + 
				"public class Y<T extends X<T, U, V>, \n" + 
				"               U extends X.XX<T, V>, \n" + 
				"               V extends X.XY> {\n" + 
				"	private final Map<U, V> m1 = new HashMap<U, V>();\n" + 
				"	private final Map<U, T> m2 = new HashMap<U, T>();\n" + 
				"	private final Z m3;\n" + 
				"\n" + 
				"	public Y(final Z p1) {\n" + 
				"		this.m3 = p1;\n" + 
				"	}\n" + 
				"\n" + 
				"	public void foo1(final U p1, final V p2, final T p3) {\n" + 
				"		m1.put(p1, p2);\n" + 
				"		m2.put(p1, p3);\n" + 
				"		m3.foo2(p1, p2);\n" + 
				"	}\n" + 
				"\n" + 
				"	public void foo3(final U p1) {\n" + 
				"		assert m1.containsKey(p1);\n" + 
				"		m1.remove(p1);\n" + 
				"		m2.remove(p1);\n" + 
				"		m3.foo2(p1, null);\n" + 
				"	}\n" + 
				"\n" + 
				"	public Collection<T> foo4() {\n" + 
				"		return Collections.unmodifiableCollection(m2.values());\n" + 
				"	}\n" + 
				"\n" + 
				"	public void foo5(final Map<XX<?, ?>, XY> p1) {\n" + 
				"		p1.putAll(m1);\n" + 
				"	}\n" + 
				"\n" + 
				"	public void foo6(final Map<XX<?, ?>, XY> p1) {\n" + 
				"		m1.keySet().retainAll(p1.keySet());\n" + 
				"		m2.keySet().retainAll(p1.keySet());\n" + 
				"	}\n" + 
				"}\n",
				"p/Z.java",
				"package p;\n" + 
				"\n" + 
				"import java.util.*;\n" + 
				"\n" + 
				"import p.X.*;\n" + 
				"\n" + 
				"public class Z {\n" + 
				"	private final Map<Class<? extends X>, \n" + 
				"		              Y<?, ? extends XX<?, ?>, ? extends XY>> \n" + 
				"		m1 = new HashMap<Class<? extends X>, \n" + 
				"		                 Y<?, ? extends XX<?, ?>, ? extends XY>>();\n" + 
				"\n" + 
				"	private Map<X.XX<?, XY>, \n" + 
				"	            X.XY> \n" + 
				"		m2 = new HashMap<X.XX<?, XY>, \n" + 
				"		                 X.XY>();\n" + 
				"\n" + 
				"	public <T extends X<T, U, V>, \n" + 
				"	        U extends X.XX<T, V>, \n" + 
				"	        V extends X.XY> \n" + 
				"	Y<T, U, V> foo1(final Class<T> p1) {\n" + 
				"		Y l1 = m1.get(p1);\n" + 
				"		if (l1 == null) {\n" + 
				"			l1 = new Y<T, U, V>(this);\n" + 
				"			m1.put(p1, l1);\n" + 
				"		}\n" + 
				"		return l1;\n" + 
				"	}\n" + 
				"\n" + 
				"	public <TT extends X.XX<?, UU>, \n" + 
				"	        UU extends X.XY> \n" + 
				"	void foo2(final TT p1, final UU p2) {\n" + 
				"		m2.put((XX<?, XY>) p1, p2);\n" + 
				"	}\n" + 
				"\n" + 
				"	public Map<XX<?, ?>, XY> foo3() {\n" + 
				"		final Map<XX<?, ?>, \n" + 
				"		          XY> l1 = new HashMap<XX<?, ?>, \n" + 
				"		                               XY>();\n" + 
				"		for (final Y<?, \n" + 
				"				     ? extends XX<?, ?>, \n" + 
				"				     ? extends XY> \n" + 
				"				i : m1.values()) {\n" + 
				"			i.foo5(l1);\n" + 
				"		}\n" + 
				"		return l1;\n" + 
				"	}\n" + 
				"\n" + 
				"	public void foo4(final Object p1, final Map<XX<?, ?>, \n" + 
				"			                                    XY> p2) {\n" + 
				"		for (final Y<?, \n" + 
				"				     ? extends XX<?, ?>, \n" + 
				"				     ? extends XY> i : m1.values()) {\n" + 
				"			i.foo6(p2);\n" + 
				"		}\n" + 
				"		for (final Map.Entry<XX<?, ?>, \n" + 
				"				             XY> i : p2.entrySet()) {\n" + 
				"			final XX<?, XY> l1 = (XX<?, XY>) i.getKey();\n" + 
				"		}\n" + 
				"	}\n" + 
				"}\n"
			},
	        "\"" + OUTPUT_DIR +  File.separator + "p/X.java\""
	        + " \"" + OUTPUT_DIR +  File.separator + "p/Y.java\""
	        + " \"" + OUTPUT_DIR +  File.separator + "p/Z.java\""
	        + " -1.5 -g -preserveAllLocals"
	        + " -cp \"" + OUTPUT_DIR + File.separator + "\""
	        + " -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\"",
	        "",
	        "----------\n" + 
	        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "p" + File.separator + "Z.java\n" + 
	        " (at line 25)\n" + 
	        "	m1.put(p1, l1);\n" + 
	        "	           ^^\n" + 
	        "Type safety: The expression of type Y needs unchecked conversion to conform to Y<?,? extends X.XX<?,?>,? extends X.XY>\n" + 
	        "----------\n" + 
	        "----------\n" + 
	        "2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "p" + File.separator + "Z.java\n" + 
	        " (at line 27)\n" + 
	        "	return l1;\n" + 
	        "	       ^^\n" + 
	        "Type safety: The expression of type Y needs unchecked conversion to conform to Y<T,U,V>\n" + 
	        "----------\n" + 
	        "----------\n" + 
	        "3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "p" + File.separator + "Z.java\n" + 
	        " (at line 33)\n" + 
	        "	m2.put((XX<?, XY>) p1, p2);\n" + 
	        "	       ^^^^^^^^^^^^^^\n" + 
	        "Type safety: The cast from TT to X.XX<?,X.XY> is actually checking against the erased type X<T,U,V>.XX\n" + 
	        "----------\n" + 
	        "----------\n" + 
	        "4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "p" + File.separator + "Z.java\n" + 
	        " (at line 58)\n" + 
	        "	final XX<?, XY> l1 = (XX<?, XY>) i.getKey();\n" + 
	        "	                ^^\n" + 
	        "The local variable l1 is never read\n" + 
	        "----------\n" + 
	        "----------\n" + 
	        "5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "p" + File.separator + "Z.java\n" + 
	        " (at line 58)\n" + 
	        "	final XX<?, XY> l1 = (XX<?, XY>) i.getKey();\n" + 
	        "	                     ^^^^^^^^^^^^^^^^^^^^^^\n" + 
	        "Type safety: The cast from X.XX<capture-of ?,capture-of ?> to X.XX<?,X.XY> is actually checking against the erased type X<T,U,V>.XX\n" + 
	        "----------\n" + 
	        "5 problems (5 warnings)",
	        true);
	// second series shows that a staged build - that simulates the auto build context - is OK as well
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;\n" + 
			"import java.io.Serializable;\n" + 
			"public interface X<T extends X<T, U, V>, \n" + 
			"				   U extends X.XX<T, V>, \n" + 
			"				   V extends X.XY> {\n" + 
			"	public interface XX<TT extends X<TT, ?, UU>, \n" + 
			"	                    UU extends X.XY> \n" + 
			"			extends	Serializable {\n" + 
			"	}\n" + 
			"	public interface XY extends Serializable {\n" + 
			"	}\n" + 
			"}\n",
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
			"package p;\n" + 
			"import java.util.*;\n" + 
			"import p.X.*;\n" + 
			"public class Y<T extends X<T, U, V>, \n" + 
			"               U extends X.XX<T, V>, \n" + 
			"               V extends X.XY> {\n" + 
			"	private final Map<U, V> m1 = new HashMap<U, V>();\n" + 
			"	private final Map<U, T> m2 = new HashMap<U, T>();\n" + 
			"	private final Z m3;\n" + 
			"\n" + 
			"	public Y(final Z p1) {\n" + 
			"		this.m3 = p1;\n" + 
			"	}\n" + 
			"\n" + 
			"	public void foo1(final U p1, final V p2, final T p3) {\n" + 
			"		m1.put(p1, p2);\n" + 
			"		m2.put(p1, p3);\n" + 
			"		m3.foo2(p1, p2);\n" + 
			"	}\n" + 
			"\n" + 
			"	public void foo3(final U p1) {\n" + 
			"		assert m1.containsKey(p1);\n" + 
			"		m1.remove(p1);\n" + 
			"		m2.remove(p1);\n" + 
			"		m3.foo2(p1, null);\n" + 
			"	}\n" + 
			"\n" + 
			"	public Collection<T> foo4() {\n" + 
			"		return Collections.unmodifiableCollection(m2.values());\n" + 
			"	}\n" + 
			"\n" + 
			"	public void foo5(final Map<XX<?, ?>, XY> p1) {\n" + 
			"		p1.putAll(m1);\n" + 
			"	}\n" + 
			"\n" + 
			"	public void foo6(final Map<XX<?, ?>, XY> p1) {\n" + 
			"		m1.keySet().retainAll(p1.keySet());\n" + 
			"		m2.keySet().retainAll(p1.keySet());\n" + 
			"	}\n" + 
			"}\n",
			"p/Z.java",
			"package p;\n" + 
			"\n" + 
			"import java.util.*;\n" + 
			"\n" + 
			"import p.X.*;\n" + 
			"\n" + 
			"public class Z {\n" + 
			"	private final Map<Class<? extends X>, \n" + 
			"		              Y<?, ? extends XX<?, ?>, ? extends XY>> \n" + 
			"		m1 = new HashMap<Class<? extends X>, \n" + 
			"		                 Y<?, ? extends XX<?, ?>, ? extends XY>>();\n" + 
			"\n" + 
			"	private Map<X.XX<?, XY>, \n" + 
			"	            X.XY> \n" + 
			"		m2 = new HashMap<X.XX<?, XY>, \n" + 
			"		                 X.XY>();\n" + 
			"\n" + 
			"	public <T extends X<T, U, V>, \n" + 
			"	        U extends X.XX<T, V>, \n" + 
			"	        V extends X.XY> \n" + 
			"	Y<T, U, V> foo1(final Class<T> p1) {\n" + 
			"		Y l1 = m1.get(p1);\n" + 
			"		if (l1 == null) {\n" + 
			"			l1 = new Y<T, U, V>(this);\n" + 
			"			m1.put(p1, l1);\n" + 
			"		}\n" + 
			"		return l1;\n" + 
			"	}\n" + 
			"\n" + 
			"	public <TT extends X.XX<?, UU>, \n" + 
			"	        UU extends X.XY> \n" + 
			"	void foo2(final TT p1, final UU p2) {\n" + 
			"		m2.put((XX<?, XY>) p1, p2);\n" + 
			"	}\n" + 
			"\n" + 
			"	public Map<XX<?, ?>, XY> foo3() {\n" + 
			"		final Map<XX<?, ?>, \n" + 
			"		          XY> l1 = new HashMap<XX<?, ?>, \n" + 
			"		                               XY>();\n" + 
			"		for (final Y<?, \n" + 
			"				     ? extends XX<?, ?>, \n" + 
			"				     ? extends XY> \n" + 
			"				i : m1.values()) {\n" + 
			"			i.foo5(l1);\n" + 
			"		}\n" + 
			"		return l1;\n" + 
			"	}\n" + 
			"\n" + 
			"	public void foo4(final Object p1, final Map<XX<?, ?>, \n" + 
			"			                                    XY> p2) {\n" + 
			"		for (final Y<?, \n" + 
			"				     ? extends XX<?, ?>, \n" + 
			"				     ? extends XY> i : m1.values()) {\n" + 
			"			i.foo6(p2);\n" + 
			"		}\n" + 
			"		for (final Map.Entry<XX<?, ?>, \n" + 
			"				             XY> i : p2.entrySet()) {\n" + 
			"			final XX<?, XY> l1 = (XX<?, XY>) i.getKey();\n" + 
			"		}\n" + 
			"	}\n" + 
			"}\n"
		},
        "\"" + OUTPUT_DIR +  File.separator + "p/Y.java\""
        + " \"" + OUTPUT_DIR +  File.separator + "p/Z.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + File.separator + "\""
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + "\"",
        "",
        "----------\n" + 
        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "p" + File.separator + "Z.java\n" + 
        " (at line 25)\n" + 
        "	m1.put(p1, l1);\n" + 
        "	           ^^\n" + 
        "Type safety: The expression of type Y needs unchecked conversion to conform to Y<?,? extends X.XX<?,?>,? extends X.XY>\n" + 
        "----------\n" + 
        "----------\n" + 
        "2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "p" + File.separator + "Z.java\n" + 
        " (at line 27)\n" + 
        "	return l1;\n" + 
        "	       ^^\n" + 
        "Type safety: The expression of type Y needs unchecked conversion to conform to Y<T,U,V>\n" + 
        "----------\n" + 
        "----------\n" + 
        "3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "p" + File.separator + "Z.java\n" + 
        " (at line 33)\n" + 
        "	m2.put((XX<?, XY>) p1, p2);\n" + 
        "	       ^^^^^^^^^^^^^^\n" + 
        "Type safety: The cast from TT to X.XX<?,X.XY> is actually checking against the erased type X<T,U,V>.XX\n" + 
        "----------\n" + 
        "----------\n" + 
        "4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "p" + File.separator + "Z.java\n" + 
        " (at line 58)\n" + 
        "	final XX<?, XY> l1 = (XX<?, XY>) i.getKey();\n" + 
        "	                ^^\n" + 
        "The local variable l1 is never read\n" + 
        "----------\n" + 
        "----------\n" + 
        "5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "p" + File.separator + "Z.java\n" + 
        " (at line 58)\n" + 
        "	final XX<?, XY> l1 = (XX<?, XY>) i.getKey();\n" + 
        "	                     ^^^^^^^^^^^^^^^^^^^^^^\n" + 
        "Type safety: The cast from X.XX<capture-of ?,capture-of ?> to X.XX<?,X.XY> is actually checking against the erased type X<T,U,V>.XX\n" + 
        "----------\n" + 
        "5 problems (5 warnings)",
        false);
}
public static Class testClass() {
	return BatchCompilerTest.class;
}
}
