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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.MessageFormat;

import junit.framework.Test;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.Main;

public class BatchCompilerTest extends AbstractRegressionTest {
	public static final String OUTPUT_DIR_PLACEHOLDER = "---OUTPUT_DIR_PLACEHOLDER---";
	static final String JRE_HOME_DIR = Util.getJREDirectory();
	private static final Main MAIN = new Main(null, null, false);
	
	static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 59 };
//	TESTS_RANGE = new int[] { 1, -1 };
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
	return buildUniqueComplianceTestSuite(testClass(), COMPLIANCE_1_5);
}

private String getLibraryClasses() {
	String[] paths = Util.getJavaClassLibs();
	StringBuffer buffer = new StringBuffer();
	for (int i = 0, max = paths.length; i < max; i++) {
		if (i != 0) {
			buffer.append(File.pathSeparatorChar);
		}
		buffer.append(paths[i]);
	}
	return String.valueOf(buffer);
}

	
	private String getJCEJar() {
		if (Util.isMacOS()) {
			return JRE_HOME_DIR + "/../Classes/jce.jar"; 
		}
		return JRE_HOME_DIR + "/lib/jce.jar";
	}
	
	private String getExtDirectory() {
		return JRE_HOME_DIR + "/lib/ext";
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
		PrintWriter out = null; 
		PrintWriter err = null; 
		boolean compileOK;
		try {
			try {
				out = new PrintWriter(new FileOutputStream(outFileName));
				err = new PrintWriter(new FileOutputStream(errFileName));
				batchCompiler = new Main(out, err, false);
			} catch (FileNotFoundException e) {
				System.out.println(getClass().getName() + '#' + getName());
				e.printStackTrace();
				throw new RuntimeException(e);
			}
			try {
				final String[] tokenizeCommandLine = Main.tokenize(commandLine);
				compileOK = batchCompiler.compile(tokenizeCommandLine);
			} catch (RuntimeException e) {
				compileOK = false;
				System.out.println(getClass().getName() + '#' + getName());
				e.printStackTrace();
				throw e;
			}
		} finally {
			if (out != null)
				out.close();
			if (err != null)
				err.close();
		}
		String outOutputString = Util.fileContent(outFileName), 
		       errOutputString = Util.fileContent(errFileName);
		boolean compareOK = false, outCompareOK = false, errCompareOK = false;
		if (compileOK == shouldCompileOK) {
			compareOK =
				(outCompareOK = semiNormalizedComparison(expectedOutOutputString,
					outOutputString, outputDirNormalizer))
				&& (errCompareOK = semiNormalizedComparison(expectedErrOutputString,
						errOutputString, outputDirNormalizer));
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
		if (!outCompareOK) {
			// calling assertEquals to benefit from the comparison UI
			// (need appropriate exception)
			assertEquals(
					"Unexpected standard output for invocation with arguments ["
						+ commandLine + "]",
					expectedOutOutputString,
					outOutputString);
		}
		if (!errCompareOK) {
			assertEquals(
					"Unexpected error output for invocation with arguments ["
						+ commandLine + "]",
					expectedErrOutputString,
					errOutputString);
		}
	}
	
/**
 * Check that no line of message extends beyond width columns. Tabs count for
 * 4 characters.
 * @param message the message to check
 * @param width the maximum number of columns for the message
 */
private void checkWidth(String message, int width) {
	BufferedReader reader = new BufferedReader(
			new StringReader(message.replaceAll("\t", "    ")));
	String line;
	try {
		while ((line = reader.readLine()) != null) {
			assertTrue("line exceeds " + width + "characters: " + line,
				line.length() <= width);
		}
	} catch (IOException e) {
		// should never happen on a StringReader
	}
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
	 * Normalizer instance that replaces occurrences of OUTPUT_DIR with 
	 * OUTPUT_DIR_PLACEHOLDER and changes file separator to / if the 
	 * platform file separator is different from /.
	 */
	private static Normalizer outputDirNormalizer;
	static {
		if (File.separatorChar == '/') {
			outputDirNormalizer = new StringNormalizer(
					null, OUTPUT_DIR, OUTPUT_DIR_PLACEHOLDER);
		}
		else {
			outputDirNormalizer = new StringNormalizer(
					new StringNormalizer(
							null, File.separator, "/"),
					OUTPUT_DIR, OUTPUT_DIR_PLACEHOLDER);
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
	int length = aBuffer.length(), bLength;
	boolean result = true;
	if (length != (bLength = bBuffer.length())) {
		System.err.println("a and b lengths differ");
		if (length > bLength) {
			length = bLength;
		}
		result = false;
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
					+ "\na: ..." + aBuffer.substring(beforeStart, beforeEnd) 
						+ "<" + aBuffer.charAt(i) + ">"
						+ aBuffer.substring(afterStart, afterEnd) + "..." 
					+ "\nb: ..." + bBuffer.substring(beforeStart, beforeEnd) 
						+ "<" + bBuffer.charAt(i) + ">"
						+ bBuffer.substring(afterStart, afterEnd) + "..."); 
			return false;
		}
	return result; // may be false if one of the strings equals the beginning
	               // of the other one, which is longer anyway
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
public void test007(){
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
        + " -bootclasspath " + getLibraryClasses()
        + " -cp " + getJCEJar()
        + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -verbose -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "[parsing    ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" + 
		"[reading    java/lang/Object.class]\n" + 
		"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" + 
		"[reading    java/util/List.class]\n" + 
		"[reading    java/lang/SuppressWarnings.class]\n" + 
		"[reading    java/lang/String.class]\n" + 
		"[writing    X.class - #1]\n" + 
		"[completed  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" + 
		"[1 unit compiled]\n" + 
		"[1 .class file generated]\n", 
        "", // changed with bug 123522: now the SuppressWarning upon the first type 
        	// influences warnings on unused imports
        true);
}
// test the tester - runNegativeTest
public void test008(){
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
        + " -bootclasspath " + getLibraryClasses()
        + " -cp " + getJCEJar()
        + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "", 
        "----------\n" + 
        "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 11)\n" + 
        "	Zork z;\n" + 
        "	^^^^\n" + 
        "Zork cannot be resolved to a type\n" + 
        "----------\n" + 
        "1 problem (1 error)", 
        true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=92398 -- a case that works, another that does not
// revisit this test case depending on https://bugs.eclipse.org/bugs/show_bug.cgi?id=95349
public void test009(){
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
        + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "", 
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
		"	Warn warn;\n" + 
		"	^^^^\n" + 
		"Discouraged access: The type Warn is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" + 
		"	KO ko;\n" + 
		"	^^\n" + 
		"Access restriction: The type KO is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" + 
		"----------\n" + 
		"3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)\n" + 
		"	Zork z;\n" + 
		"	^^^^\n" + 
		"Zork cannot be resolved to a type\n" + 
		"----------\n" + 
		"3 problems (1 error, 2 warnings)",
        true);
}
// command line - no user classpath nor bootclasspath
public void test010(){
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
        "[parsing    ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" + 
		"[reading    java/lang/Object.class]\n" + 
		"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" + 
		"[reading    java/util/List.class]\n" + 
		"[reading    java/lang/SuppressWarnings.class]\n" + 
		"[reading    java/lang/String.class]\n" + 
		"[writing    X.class - #1]\n" + 
		"[completed  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" + 
		"[1 unit compiled]\n" + 
		"[1 .class file generated]\n", 
        "", 
        true);
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
        "    -cp -classpath <directories and zip/jar files separated by " + File.pathSeparator + ">\n" + 
        "                       specify location for application classes and sources.\n" + 
        "                       Each directory or file can specify access rules for\n" + 
        "                       types between ''['' and '']'' (e.g. [-X] to forbid\n" + 
        "                       access to type X, [~X] to discourage access to type X,\n" + 
        "                       [+p/X:-p/*] to forbid access to all types in package p\n" + 
        "                       but allow access to p/X)\n" + 
        "    -bootclasspath <directories and zip/jar files separated by " + File.pathSeparator + ">\n" + 
        "                       specify location for system classes. Each directory or\n" + 
        "                       file can specify access rules for types between ''[''\n" + 
        "                       and '']''\n" + 
        "    -sourcepath <directories and zip/jar files separated by " + File.pathSeparator + ">\n" + 
        "                       specify location for application sources. Each directory\n" + 
        "                       or file can specify access rules for types between ''[''\n" + 
        "                       and '']''. Each directory can further specify a specific\n" + 
        "                       destination directory using a ''-d'' option between ''[''\n" +
        "                       and '']''; this overrides the general ''-d'' option.\n" +
        "                       .class files created from source files contained in a\n" + 
        "                       jar file are put in the user.dir folder in case no\n" + 
        "                       general ''-d'' option is specified. zip/jar files cannot\n" +
        "                       override the general ''-d'' option\n" + 
        "    -extdirs <directories separated by " + File.pathSeparator + ">\n" + 
        "                       specify location for extension zip/jar files\n" + 
        "    -endorseddirs <directories separated by " + File.pathSeparator + ">\n" + 
        "                       specify location for endorsed zip/jar files\n" + 
        "    -d <dir>           destination directory (if omitted, no directory is\n" + 
        "                       created); this option can be overridden per source\n" + 
		"                       directory\n" +
		"    -d none            generate no .class files\n" + 
        "    -encoding <enc>    specify custom encoding for all sources. Each\n" + 
        "                       file/directory can override it when suffixed with\n" + 
        "                       ''[''<enc>'']'' (e.g. X.java[utf8])\n" + 
        " \n" + 
        " Compliance options:\n" + 
        "    -1.3               use 1.3 compliance (-source 1.3 -target 1.1)\n" + 
        "    -1.4             + use 1.4 compliance (-source 1.3 -target 1.2)\n" + 
        "    -1.5 -5 -5.0       use 1.5 compliance (-source 1.5 -target 1.5)\n" + 
        "    -1.6 -6 -6.0       use 1.6 compliance (-source 1.6 -target 1.6)\n" + 
        "    -1.7 -7 -7.0       use 1.7 compliance (-source 1.7 -target 1.7)\n" + 
        "    -source <version>  set source level: 1.3 to 1.7 (or 5, 5.0, etc)\n" + 
        "    -target <version>  set classfile target: 1.1 to 1.7 (or 5, 5.0, etc)\n" + 
        " \n" + 
        " Warning options:\n" + 
        "    -deprecation     + deprecation outside deprecated code\n" + 
        "    -nowarn -warn:none disable all warnings\n" + 
        "    -?:warn -help:warn display advanced warning options\n" + 
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
        "    -X<option>         specify non-standard option (ignored\n" + 
        "                       expect for -Xemacs)\n" + 
        "    -X                 print non-standard options and exit (ignored)\n" + 
        "    -O                 optimize for execution time (ignored)\n" + 
        " \n" + 
        " Advanced options:\n" + 
        "    @<file>            read command line arguments from file\n" + 
        "    -maxProblems <n>   max number of problems per compilation unit (100 by\n" + 
        "                       default)\n" + 
        "    -log <file>        log to a file. If the file extension is ''.xml'', then\n" + 
        "                       the log will be a xml file.\n" + 
        "    -proceedOnError    do not stop at first error, dumping class files with\n" + 
        "                       problem methods\n" + 
        "    -verbose           enable verbose output\n" + 
        "    -referenceInfo     compute reference info\n" + 
        "    -progress          show progress (only in -log mode)\n" + 
        "    -time              display speed information \n" + 
        "    -noExit            do not call System.exit(n) at end of compilation (n==0\n" + 
        "                       if no error)\n" + 
        "    -repeat <n>        repeat compilation process <n> times for perf analysis\n" + 
        "    -inlineJSR         inline JSR bytecode (implicit if target >= 1.5)\n" + 
        "    -enableJavadoc     consider references in javadoc\n" + 
        "    -Xemacs            used to enable emacs-style output in the console.\n" + 
        "                       It doesnt affect the xml log output\n" + 
        " \n" + 
        "    -? -help           print this help message\n" + 
        "    -v -version        print compiler version\n" + 
        "    -showversion       print compiler version and continue\n" + 
        "\n";
	String expandedExpectedOutput = 
		MessageFormat.format(expectedOutput, new String[] {
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
        "    -deprecation     + deprecation outside deprecated code\n" + 
        "    -nowarn -warn:none disable all warnings\n" + 
        "    -warn:<warnings separated by ,>    enable exactly the listed warnings\n" + 
        "    -warn:+<warnings separated by ,>   enable additional warnings\n" + 
        "    -warn:-<warnings separated by ,>   disable specific warnings\n" + 
        "      allDeprecation       deprecation including inside deprecated code\n" + 
        "      allJavadoc           invalid or missing javadoc\n" + 
        "      assertIdentifier   + ''assert'' used as identifier\n" + 
        "      boxing               autoboxing conversion\n" + 
        "      charConcat         + char[] in String concat\n" + 
        "      conditionAssign      possible accidental boolean assignment\n" + 
        "      constructorName    + method with constructor name\n" + 
        "      dep-ann              missing @Deprecated annotation\n" + 
        "      deprecation        + deprecation outside deprecated code\n" + 
        "      discouraged        + use of types matching a discouraged access rule\n" + 
        "      emptyBlock           undocumented empty block\n" + 
        "      enumSwitch           incomplete enum switch\n" + 
        "      fallthrough          possible fall-through case\n" + 
        "      fieldHiding          field hiding another variable\n" + 
        "      finalBound           type parameter with final bound\n" + 
        "      finally            + finally block not completing normally\n" + 
        "      forbidden          + use of types matching a forbidden access rule\n" + 
        "      hiding               macro for fieldHiding, localHiding, typeHiding and\n" + 
        "                           maskedCatchBlock\n" + 
        "      incomplete-switch    same as enumSwitch\n" + 
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
        "      paramAssign          assignment to a parameter\n" + 
        "      pkgDefaultMethod   + attempt to override package-default method\n" + 
        "      raw                + usage of raw type\n" + 
        "      semicolon            unnecessary semicolon, empty statement\n" + 
        "      serial             + missing serialVersionUID\n" + 
        "      specialParamHiding   constructor or setter parameter hiding another field\n" + 
        "      static-access        macro for indirectStatic and staticReceiver\n" + 
        "      staticReceiver     + non-static reference to static member\n" + 
        "      super                overriding a method without making a super invocation\n" +
        "      suppress           + enable @SuppressWarnings\n" + 
        "      synthetic-access     same as syntheticAccess\n" + 
        "      syntheticAccess      synthetic access for innerclass\n" + 
        "      tasks(<tags separated by |>) tasks identified by tags inside comments\n" + 
        "      typeHiding         + type parameter hiding another type\n" + 
        "      unchecked          + unchecked type operation\n" + 
        "      unnecessaryElse      unnecessary else clause\n" + 
        "      unqualified-field-access same as unqualifiedField\n" + 
        "      unqualifiedField     unqualified reference to field\n" + 
        "      unused               macro for unusedArgument, unusedImport, unusedLabel,\n" + 
        "                               unusedLocal, unusedPrivate and unusedThrown\n" + 
        "      unusedArgument       unread method parameter\n" + 
        "      unusedImport       + unused import declaration\n" + 
        "      unusedLabel        + unused label\n" + 
        "      unusedLocal        + unread local variable\n" + 
        "      unusedPrivate      + unused private member declaration\n" + 
        "      unusedThrown         unused declared thrown exception\n" + 
        "      uselessTypeCheck     unnecessary cast/instanceof operation\n" + 
        "      varargsCast        + varargs argument need explicit cast\n" + 
        "      warningToken       + unhandled warning token in @SuppressWarnings\n" + 
        " \n" + 
        " Advanced options:\n" + 
        "    -? -help           print the help message\n" + 
        "\n";
	String expandedExpectedOutput = 
		MessageFormat.format(expectedOutput, new String[] {
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
				"/** */\n" + 
				"public class X {\n" + 
				"	Zork z;\n" + 
				"}", },
				"\"" + OUTPUT_DIR + File.separator + "X.java\""
				+ " -1.5 -proceedOnError"
				+ " -log \"" + logFileName + "\" -d \"" + OUTPUT_DIR + "\"",
				"", 
				"----------\n" + 
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"1 problem (1 error)", 
				true);
		String logContents = Util.fileContent(logFileName);
		String expectedLogContents = 
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
			"<!DOCTYPE compiler PUBLIC \"-//Eclipse.org//DTD Eclipse JDT 3.2.002 Compiler//EN\" \"http://www.eclipse.org/jdt/core/compiler_32_002.dtd\">\n" + 
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
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.fallthroughCase\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.fatalOptionalError\" value=\"enabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.fieldHiding\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.finalParameterBound\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.finallyBlockNotCompletingNormally\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.forbiddenReference\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.hiddenCatchBlock\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.incompatibleNonInheritedInterfaceMethod\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.incompleteEnumSwitch\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.indirectStaticAccess\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadoc\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadocTags\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsDeprecatedRef\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsNotVisibleRef\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsVisibility\" value=\"public\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.localVariableHiding\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.methodWithConstructorName\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingDeprecatedAnnotation\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocComments\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocCommentsOverriding\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocCommentsVisibility\" value=\"public\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocTags\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocTagsOverriding\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingJavadocTagsVisibility\" value=\"public\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingOverrideAnnotation\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.missingSerialVersion\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.noEffectAssignment\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.noImplicitStringConversion\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.nullReference\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.overridingMethodWithoutSuperInvocation\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.overridingPackageDefaultMethod\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.parameterAssignment\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.possibleAccidentalBooleanAssignment\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.rawTypeReference\" value=\"warning\"/>\n" + 
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
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedLabel\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedLocal\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedParameter\" value=\"ignore\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedParameterWhenImplementingAbstract\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedParameterWhenOverridingConcrete\" value=\"disabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.unusedPrivateMember\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.problem.varargsArgumentNeedCast\" value=\"warning\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.source\" value=\"1.5\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.taskCaseSensitive\" value=\"enabled\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.taskPriorities\" value=\"\"/>\n" + 
			"		<option key=\"org.eclipse.jdt.core.compiler.taskTags\" value=\"\"/>\n" + 
			"	</options>\n" + 
			"	<classpaths>NORMALIZED SECTION</classpaths>\n" + 
			"	<sources>\n" + 
			"		<source output=\"---OUTPUT_DIR_PLACEHOLDER---\" path=\"---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java\">\n" + 
			"			<problems errors=\"1\" problems=\"1\" warnings=\"0\">\n" + 
			"				<problem charEnd=\"28\" charStart=\"25\" id=\"UndefinedType\" line=\"3\" severity=\"ERROR\">\n" + 
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
			"		<problem_summary errors=\"1\" problems=\"1\" tasks=\"0\" warnings=\"0\"/>\n" + 
			"	</stats>\n" + 
			"</compiler>\n";
		String normalizedExpectedLogContents =
				MessageFormat.format(
						expectedLogContents,
						new String[] {
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
				"/** */\n" + 
				"public class X {\n" + 
				"	Zork z;\n" + 
				"}", },
				"\"" + OUTPUT_DIR + File.separator + "X.java\"" 
				+ " -1.5 -proceedOnError"
				+ " -log \"" + logFileName + "\" -d \"" + OUTPUT_DIR + "\"",
				"", 
				"----------\n" + 
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"1 problem (1 error)", 
				false);
		String logContents = Util.fileContent(logFileName);
		String expectedLogContents = 
			"----------\n" + 
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java (at line 3)\n" + 
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
				"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
				"	Zork z;\n" + 
				"	^^^^\n" + 
				"Zork cannot be resolved to a type\n" + 
				"----------\n" + 
				"1 problem (1 error)", 
				false);
		String logContents = Util.fileContent(logFileName);
		String expectedLogContents = 
			"----------\n" + 
			"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---" + File.separator + "X.java (at line 3)\n" + 
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
public void test016(){
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
			"[parsing    ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" + 
			"[reading    java/lang/Object.class]\n" + 
			"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" + 
			"[parsing    ---OUTPUT_DIR_PLACEHOLDER---/OK1.java - #2/2]\n" + 
			"[writing    X.class - #1]\n" + 
			"[completed  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/2]\n" + 
			"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/OK1.java - #2/2]\n" + 
			"[writing    OK1.class - #2]\n" + 
			"[completed  ---OUTPUT_DIR_PLACEHOLDER---/OK1.java - #2/2]\n" + 
			"[2 units compiled]\n" + 
			"[2 .class files generated]\n",
	        "",
	        true);
	}
public void test017(){
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
	        + " -proceedOnError -referenceInfo" 
	        + " -d \"" + OUTPUT_DIR + "\"",
	        "",
	        "incorrect classpath: dummmy_dir\n",
	        true);
	}
// we tolerate inexisting jars on the classpath
// TODO (maxime) check and document
public void _test017b(){
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
        + " -cp dummy.jar" + File.pathSeparator + File.pathSeparator + "\"" + OUTPUT_DIR + "\"" 
        + " -verbose -proceedOnError -referenceInfo" 
        + " -d \"" + OUTPUT_DIR + "\"",
        "[2 .class files generated]\n",
        "incorrect classpath: dummmy.jar\n",
        true);
}
// we tolerate empty classpath entries
// TODO (maxime) check and document
public void _test017c(){
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
        + " -cp " + File.pathSeparator + File.pathSeparator + "\"" + OUTPUT_DIR + "\"" 
        + " -verbose -proceedOnError -referenceInfo" 
        + " -d \"" + OUTPUT_DIR + "\"",
        "[2 .class files generated]\n",
        "incorrect classpath\n",
        true);
}
// command line - unusual classpath (empty, but using current directory, still OK provided 
//	that we execute from the appropriate directory); since there is no notion
// of current directory for this tests suite, the test is not executed
// TODO (maxime) enforce working directory
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
public void test019(){
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
	        + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal" 
	        + " -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\"", 
	        "", 
			"----------\n" + 
			"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
			"	Warn warn;\n" + 
			"	^^^^\n" + 
			"Discouraged access: The type Warn is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" + 
			"----------\n" + 
			"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" + 
			"	KO ko;\n" + 
			"	^^\n" + 
			"Access restriction: The type KO is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" + 
			"----------\n" + 
			"3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)\n" + 
			"	Zork z;\n" + 
			"	^^^^\n" + 
			"Zork cannot be resolved to a type\n" + 
			"----------\n" + 
			"3 problems (1 error, 2 warnings)",
	        true);
	}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - skip options -O -Jxxx and -Xxxx, multiple times if needed
	public void test020(){
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
			"[parsing    ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" + 
			"[reading    java/lang/Object.class]\n" + 
			"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" + 
			"[writing    X.class - #1]\n" + 
			"[completed  ---OUTPUT_DIR_PLACEHOLDER---/X.java - #1/1]\n" + 
			"[1 unit compiled]\n" + 
			"[1 .class file generated]\n",
	        "",
	        true);
	}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - -sourcepath finds additional source files
	public void test021(){
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
			"[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/1]\n" + 
			"[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]\n" + 
			"[reading    java/lang/Object.class]\n" + 
			"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/2]\n" + 
			"[writing    Y.class - #1]\n" + 
			"[completed  ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/2]\n" + 
			"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]\n" + 
			"[writing    X.class - #2]\n" + 
			"[completed  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]\n" + 
			"[2 units compiled]\n" + 
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
	        "duplicate sourcepath specification: -sourcepath ---OUTPUT_DIR_PLACEHOLDER---/src2\n",
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
	        "duplicate extdirs specification: -extdirs ---OUTPUT_DIR_PLACEHOLDER---/src2\n",
	        true);
	}
//	 https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - explicit empty -extdirs removes extensions
	public void test024(){
		this.runNegativeTest(
			new String[] {
					"X.java",
					"/** */\n" + 
					"public class X {\n" + 
					"  my.pkg.Zork dummy;\n" + 
					"}",
			},
	        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
			+ " -extdirs \"\"" 
	        + " -1.5 -g -preserveAllLocals"
	        + " -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\" ",
	        "",
	        "----------\n" + 
	        "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
	        "	my.pkg.Zork dummy;\n" + 
	        "	^^\n" + 
	        "my cannot be resolved to a type\n" + 
	        "----------\n" + 
	        "1 problem (1 error)",
	        true);
	}
//	 https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - cumulative -extdirs extends the classpath
	public void test025(){
		String path = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
		String libPath = null;
		if (path.endsWith(File.separator)) {
			libPath = path + "lib.jar";
		} else {
			libPath = path + File.separator + "lib.jar";
		}
		boolean jarCreated = false;
		try {
			Util.createJar(new String[] {
					"my/pkg/Zork.java",
					"package my.pkg;\n" + 
					"public class Zork {\n" + 
					"}",
				},
				libPath,
				JavaCore.VERSION_1_4);
			jarCreated = true;
		} catch (IOException e) {
			// ignore
		}
		this.runConformTest(
			new String[] {
					"src1/X.java",
					"/** */\n" + 
					"public class X {\n" + 
					"  my.pkg.Zork dummy;\n" + 
					"}",
					"src2/Y.java",
					"/** */\n" + 
					"public class Y extends X {\n" + 
					"}",
			},
	        "\"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
			+ " -extdirs \"" + path + File.pathSeparator + OUTPUT_DIR +  File.separator + "src1\"" 
			+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src1\"" 
	        + " -1.5 -g -preserveAllLocals"
	        + " -verbose -proceedOnError -referenceInfo"
	        + " -d \"" + OUTPUT_DIR + "\" ",
	        "[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/1]\n" + 
	        "[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]\n" + 
	        "[reading    java/lang/Object.class]\n" + 
	        "[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/2]\n" + 
	        "[writing    Y.class - #1]\n" + 
	        "[completed  ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/2]\n" + 
	        "[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]\n" + 
	        "[reading    my/pkg/Zork.class]\n" + 
	        "[writing    X.class - #2]\n" + 
	        "[completed  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]\n" + 
	        "[2 units compiled]\n" + 
	        "[2 .class files generated]\n",
	        "",
	        true);
		if (jarCreated) {
			new File(libPath).delete();
		}
	}
//	 https://bugs.eclipse.org/bugs/show_bug.cgi?id=88364 - -extdirs extends the classpath before -classpath
		public void test026(){
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
				+ " -extdirs \"" + getExtDirectory() + File.pathSeparator + OUTPUT_DIR +  File.separator + "src1\"" 
				+ " -sourcepath \"" + OUTPUT_DIR +  File.separator + "src2" + File.pathSeparator + OUTPUT_DIR +  File.separator + "src1\"" 
		        + " -1.5 -g -preserveAllLocals"
		        + " -verbose -proceedOnError -referenceInfo"
		        + " -d \"" + OUTPUT_DIR + "\" ",
				"[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/1]\n" + 
				"[parsing    ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]\n" + 
				"[reading    java/lang/Object.class]\n" + 
				"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/2]\n" + 
				"[writing    Y.class - #1]\n" + 
				"[completed  ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java - #1/2]\n" + 
				"[analyzing  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]\n" + 
				"[writing    X.class - #2]\n" + 
				"[completed  ---OUTPUT_DIR_PLACEHOLDER---/src1/X.java - #2/2]\n" + 
				"[2 units compiled]\n" + 
				"[2 .class files generated]\n",
				"",
		        true);
		}
		
public void test027(){
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
        + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        // TODO (maxime) reintroduce the -verbose option to check the number of files
        //               generated, once able to avoid console echoing
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "", 
        "----------\n" + 
        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
        "	Warn warn;\n" + 
        "	^^^^\n" + 
		"Discouraged access: The type Warn is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---/p1\n" + 
		"----------\n" + 
		"2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 6)\n" + 
		"	KO ko;\n" + 
		"	^^\n" + 
		"Access restriction: The type KO is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" + 
        "----------\n" + 
        "3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)\n" + 
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
	        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 8)\n" + 
	        "	private final Map<Class<? extends X>, \n" + 
	        "	                                  ^\n" + 
	        "X is a raw type. References to generic type X<T,U,V> should be parameterized\n" + 
	        "----------\n" + 
	        "2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 10)\n" + 
	        "	m1 = new HashMap<Class<? extends X>, \n" + 
	        "	                                 ^\n" + 
	        "X is a raw type. References to generic type X<T,U,V> should be parameterized\n" + 
	        "----------\n" + 
	        "3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 22)\n" + 
	        "	Y l1 = m1.get(p1);\n" + 
	        "	^\n" + 
	        "Y is a raw type. References to generic type Y<T,U,V> should be parameterized\n" + 
	        "----------\n" + 
	        "4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 25)\n" + 
	        "	m1.put(p1, l1);\n" + 
	        "	           ^^\n" + 
	        "Type safety: The expression of type Y needs unchecked conversion to conform to Y<?,? extends X.XX<?,?>,? extends X.XY>\n" + 
	        "----------\n" + 
	        "5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 27)\n" + 
	        "	return l1;\n" + 
	        "	       ^^\n" + 
	        "Type safety: The expression of type Y needs unchecked conversion to conform to Y<T,U,V>\n" + 
	        "----------\n" + 
	        "6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 33)\n" + 
	        "	m2.put((XX<?, XY>) p1, p2);\n" + 
	        "	       ^^^^^^^^^^^^^^\n" + 
	        "Type safety: Unchecked cast from TT to X.XX<?,X.XY>\n" + 
	        "----------\n" + 
	        "7. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 58)\n" + 
	        "	final XX<?, XY> l1 = (XX<?, XY>) i.getKey();\n" + 
	        "	                ^^\n" + 
	        "The local variable l1 is never read\n" + 
	        "----------\n" + 
	        "8. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 58)\n" + 
	        "	final XX<?, XY> l1 = (XX<?, XY>) i.getKey();\n" + 
	        "	                     ^^^^^^^^^^^^^^^^^^^^^^\n" + 
	        "Type safety: Unchecked cast from X.XX<capture#22-of ?,capture#23-of ?> to X.XX<?,X.XY>\n" + 
	        "----------\n" + 
	        "8 problems (8 warnings)",
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
        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 8)\n" + 
        "	private final Map<Class<? extends X>, \n" + 
        "	                                  ^\n" + 
        "X is a raw type. References to generic type X<T,U,V> should be parameterized\n" + 
        "----------\n" + 
        "2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 10)\n" + 
        "	m1 = new HashMap<Class<? extends X>, \n" + 
        "	                                 ^\n" + 
        "X is a raw type. References to generic type X<T,U,V> should be parameterized\n" + 
        "----------\n" + 
        "3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 22)\n" + 
        "	Y l1 = m1.get(p1);\n" + 
        "	^\n" + 
        "Y is a raw type. References to generic type Y<T,U,V> should be parameterized\n" + 
        "----------\n" + 
        "4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 25)\n" + 
        "	m1.put(p1, l1);\n" + 
        "	           ^^\n" + 
        "Type safety: The expression of type Y needs unchecked conversion to conform to Y<?,? extends X.XX<?,?>,? extends X.XY>\n" + 
        "----------\n" + 
        "5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 27)\n" + 
        "	return l1;\n" + 
        "	       ^^\n" + 
        "Type safety: The expression of type Y needs unchecked conversion to conform to Y<T,U,V>\n" + 
        "----------\n" + 
        "6. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 33)\n" + 
        "	m2.put((XX<?, XY>) p1, p2);\n" + 
        "	       ^^^^^^^^^^^^^^\n" + 
        "Type safety: Unchecked cast from TT to X.XX<?,X.XY>\n" + 
        "----------\n" + 
        "7. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 58)\n" + 
        "	final XX<?, XY> l1 = (XX<?, XY>) i.getKey();\n" + 
        "	                ^^\n" + 
        "The local variable l1 is never read\n" + 
        "----------\n" + 
        "8. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/p/Z.java (at line 58)\n" + 
        "	final XX<?, XY> l1 = (XX<?, XY>) i.getKey();\n" + 
        "	                     ^^^^^^^^^^^^^^^^^^^^^^\n" + 
        "Type safety: Unchecked cast from X.XX<capture#22-of ?,capture#23-of ?> to X.XX<?,X.XY>\n" + 
        "----------\n" + 
        "8 problems (8 warnings)",
        false);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=104664
public void test033(){
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" + 
			"public class X {\n" + 
			"}",
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
			"/** */\n" + 
			"public class X {\n" + 
			"}",
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
					"import p.Y;\n" +
					"public class X {\n" +
					"	public static void main(String[] args) {\n" +
					"		System.out.print(Y.S);\n" +
					"	}\n" +
					"}",
				},
		        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
		        + " -1.5 -g -preserveAllLocals -proceedOnError -referenceInfo ",
		        "",// this is not the runtime output
		        "no classpath defined, using default directory instead\n",
		        false);
		final String userDir = System.getProperty("user.dir");
		File f = new File(userDir, "X.java");
		if (!f.delete()) {
			System.out.println("Could not delete X");
		}
		f = new File(userDir, "p" + File.separator + "Y.java");
		if (!f.delete()) {
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
			"package p;\n" + 
			"/** */\n" + 
			"public class X {\n" + 
			"}",
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
			"/** */\n" + 
			"public class Y extends p.X {\n" + 
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + File.separator + "bin1[~**/X]\""
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + File.separator + "bin2/\"",
        "",
        "----------\n" + 
        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 2)\n" + 
        "	public class Y extends p.X {\n" + 
        "	                       ^^^\n" + 
        "Discouraged access: The type X is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---/bin1\n" + 
        "----------\n" + 
        "1 problem (1 warning)",
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=53773
// complain on assignment to parameters
public void test037() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X {\n" +
			"  void foo(int i, final int j) {\n" +
			"    i =  0; // warning\n" +
			"    j =  0; // error\n" +
			"  }\n" +
			"}\n"},
		"\"" + OUTPUT_DIR +  File.separator + "X.java\""
		+ " -1.5 "
		+ " -cp \"" + OUTPUT_DIR + "\""
		+ " -warn:+paramAssign"
		+ " -proceedOnError"
		+ " -d \"" + OUTPUT_DIR + "\"",
		"",
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 3)\n" + 
		"	i =  0; // warning\n" + 
		"	^\n" + 
		"The parameter i should not be assigned\n" + 
		"----------\n" + 
		"2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
		"	j =  0; // error\n" + 
		"	^\n" + 
		"The final local variable j cannot be assigned. It must be blank and not using a compound assignment\n" + 
		"----------\n" + 
		"2 problems (1 error, 1 warning)",
		true);
}

// Missing access restriction violation error on generic type.
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=122995
// Binary case.
public void test039(){
	this.runConformTest(
		new String[] {
			"src1/p/X.java",
			"package p;\n" +
			"public class X<T> {\n" +
			"	T m;\n" +
			"}",
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
			"package p;\n" +
			"public class Y {\n" +
			"	X x1;\n" +
			"	X<String> x2 = new X<String>();\n" +
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "src2/Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + File.separator + "bin1[~**/X]\""
        + " -proceedOnError -referenceInfo"
        + " -d \"" + OUTPUT_DIR + File.separator + "bin2/\"",
        "",
        "----------\n" + 
        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 3)\n" + 
        "	X x1;\n" + 
        "	^\n" + 
        "Discouraged access: The type X<T> is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---/bin1\n" + 
        "----------\n" + 
        "2. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 3)\n" + 
        "	X x1;\n" + 
        "	^\n" + 
        "X is a raw type. References to generic type X<T> should be parameterized\n" + 
        "----------\n" + 
        "3. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 4)\n" + 
        "	X<String> x2 = new X<String>();\n" + 
        "	^\n" + 
        "Discouraged access: The type X<String> is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---/bin1\n" + 
        "----------\n" + 
        "4. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 4)\n" + 
        "	X<String> x2 = new X<String>();\n" + 
        "	               ^^^^^^^^^^^^^^^\n" + 
        "Discouraged access: The constructor X<String>() is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---/bin1\n" + 
        "----------\n" + 
        "5. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/src2/Y.java (at line 4)\n" + 
        "	X<String> x2 = new X<String>();\n" + 
        "	                   ^\n" + 
        "Discouraged access: The type X<String> is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---/bin1\n" + 
        "----------\n" + 
        "5 problems (5 warnings)",
        false);
}

// check we get appropriate combination of access rules
public void test040(){
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;\n" + 
			"/** */\n" + 
			"public class X {\n" + 
			"}",
			"p/Z.java",
			"package p;\n" + 
			"/** */\n" + 
			"public class Z {\n" + 
			"}"			
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
			"/** */\n" + 
			"public class Y {\n" +
			"  p.X x;\n" + 
			"  p.Z z;\n" + 
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[+p/X" + File.pathSeparator + "-p/*]\""
        + " -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "", 
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Y.java (at line 4)\n" + 
		"	p.Z z;\n" + 
		"	^^^\n" + 
		"Access restriction: The type Z is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=124533
// turn off discouraged references warnings
public void test041(){
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;\n" + 
			"/** */\n" + 
			"public class X {\n" + 
			"}",
			"p/Z.java",
			"package p;\n" + 
			"/** */\n" + 
			"public class Z {\n" + 
			"}"			
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
			"/** */\n" + 
			"public class Y {\n" +
			"  p.X x;\n" + 
			"  p.Z z;\n" + 
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[~p/X" + File.pathSeparator + "-p/*]\""
        + " -warn:-discouraged -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "", 
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Y.java (at line 4)\n" + 
		"	p.Z z;\n" + 
		"	^^^\n" + 
		"Access restriction: The type Z is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" + 		
		"----------\n" + 
		"1 problem (1 warning)",
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=124533
// turn off forbidden references warnings
public void test042(){
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;\n" + 
			"/** */\n" + 
			"public class X {\n" + 
			"}",
			"p/Z.java",
			"package p;\n" + 
			"/** */\n" + 
			"public class Z {\n" + 
			"}"			
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
			"/** */\n" + 
			"public class Y {\n" +
			"  p.X x;\n" + 
			"  p.Z z;\n" + 
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[~p/X" + File.pathSeparator + "-p/*]\""
        + " -warn:-forbidden -warn:+deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "", 
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Y.java (at line 3)\n" + 
		"	p.X x;\n" + 
		"	^^^\n" + 
		"Discouraged access: The type X is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=124533
// turn off discouraged and forbidden references warnings
public void test043(){
	this.runConformTest(
		new String[] {
			"p/X.java",
			"package p;\n" + 
			"/** */\n" + 
			"public class X {\n" + 
			"}",
			"p/Z.java",
			"package p;\n" + 
			"/** */\n" + 
			"public class Z {\n" + 
			"}"			
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
			"/** */\n" + 
			"public class Y {\n" +
			"  p.X x;\n" + 
			"  p.Z z;\n" + 
			"}",
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
			"public class X {\n" + 
			"  void foo() {\n" + 
			"    Object o = null;\n" + 
			"    o.toString();\n" + 
			"  }\n" + 
			"}"},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -bootclasspath " + getLibraryClasses()
        + " -cp " + getJCEJar()
        + " -warn:+null"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "", 
        "----------\n" + 
        "1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 4)\n" + 
        "	o.toString();\n" + 
        "	^\n" + 
        "The variable o can only be null; it was either set to null or checked for null when last used\n" + 
        "----------\n" + 
        "1 problem (1 warning)",
        true);
}

// null ref option
public void test045(){
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"  void foo() {\n" + 
			"    Object o = null;\n" + 
			"    o.toString();\n" + 
			"  }\n" + 
			"}"},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -bootclasspath " + getLibraryClasses()
        + " -cp " + getJCEJar()
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
			"package p;\n" + 
			"/** */\n" + 
			"public class X {\n" + 
			"}",
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
			"/** */\n" + 
			"@SuppressWarnings(\"all\")\n" +
			"public class Y {\n" +
			"  p.X x;\n" + 
			"}",
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
			"package p;\n" + 
			"/** */\n" + 
			"public class X {\n" + 
			"}",
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
			"/** */\n" + 
			"@SuppressWarnings(\"restriction\")\n" +
			"public class Y {\n" +
			"  p.X x;\n" + 
			"}",
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
			"package p;\n" + 
			"/** */\n" + 
			"public class X {\n" + 
			"}",
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
			"/** */\n" + 
			"@SuppressWarnings(\"deprecation\")\n" +
			"public class Y {\n" +
			"  p.X x;\n" + 
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "Y.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[~p/X" + File.pathSeparator + "-p/*]\""
        + " -warn:+discouraged,forbidden,deprecation,syntheticAccess,uselessTypeCheck,unsafe,finalBound,unusedLocal"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "",
        "----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Y.java (at line 4)\n" + 
		"	p.X x;\n" + 
		"	^^^\n" + 
		"Discouraged access: The type X is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---\n" + 
		"----------\n" + 
		"1 problem (1 warning)", 
        false);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=67836
// [compiler] warning on fall through
// disable warning on command line (implicit)
public void test049(){
	this.runConformTest(
		new String[] {
			"X.java",
			"/** */\n" + 
			"public class X {\n" + 
			"    public void test(int p) {\n" + 
			"        switch (p) {\n" + 
			"        case 0:\n" + 
			"            System.out.println(0);\n" + 
			"        case 1:\n" + 
			"            System.out.println(1); // possible fall-through\n" + 
			"        }\n" + 
			"    }\n" + 
			"}",
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
			"/** */\n" + 
			"public class X {\n" + 
			"    public void test(int p) {\n" + 
			"        switch (p) {\n" + 
			"        case 0:\n" + 
			"            System.out.println(0);\n" + 
			"        case 1:\n" + 
			"            System.out.println(1); // possible fall-through\n" + 
			"        }\n" + 
			"    }\n" + 
			"}",
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
			"/** */\n" + 
			"public class X {\n" + 
			"    public void test(int p) {\n" + 
			"        switch (p) {\n" + 
			"        case 0:\n" + 
			"            System.out.println(0);\n" + 
			"        case 1:\n" + 
			"            System.out.println(1); // complain: possible fall-through\n" + 
			"        }\n" + 
			"    }\n" + 
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -warn:+fallthrough"
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "", 
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 7)\n" + 
		"	case 1:\n" + 
		"	^^^^^^\n" + 
		"Switch case may be entered by falling through previous case\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
        true);
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=123476
public void test052(){
	try {
		new File(OUTPUT_DIR).mkdirs();
		File barFile = new File(OUTPUT_DIR +  File.separator + "Bar.java");
		FileOutputStream barOutput = new FileOutputStream(barFile);
		try {
			String barContents = 
				"public class Bar	\n" +
				"{	\n" +
				"  Bar(int class)	\n" +
				"  {	\n" +
				"  }	\n" +
				"}\n";
			barOutput.write(barContents.getBytes());
		} finally {
			barOutput.close();
		}
	} catch(IOException e) {
		// do nothing, will fail below
	}
	
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X\n" + 
			"{\n" + 
			"  static Object x()\n" + 
			"  {\n" + 
			"    return new Bar(5);\n" + 
			"  }\n" + 
			"}\n",
		},
     "\"" + OUTPUT_DIR +  File.separator + "X.java\""
     + " -cp \"" + OUTPUT_DIR + File.pathSeparator + "\""
     + " -d \"" + OUTPUT_DIR + "\"",
     "", 
     "----------\n" + 
     "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 5)\n" + 
     "	return new Bar(5);\n" + 
     "	       ^^^^^^^^^^\n" + 
     "The constructor Bar(int) is undefined\n" + 
     "----------\n" + 
     "----------\n" + 
     "2. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/Bar.java (at line 2)\n" + 
     "	{	\n" + 
     "	^\n" + 
     "Syntax error, insert \"}\" to complete ClassBody\n" + 
     "----------\n" + 
     "3. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/Bar.java (at line 3)\n" + 
     "	Bar(int class)	\n" + 
     "	        ^^^^^\n" + 
     "Syntax error on token \"class\", invalid VariableDeclaratorId\n" + 
     "----------\n" + 
     "4. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/Bar.java (at line 3)\n" + 
     "	Bar(int class)	\n" + 
     "  {	\n" + 
     "  }	\n" + 
     "	        ^^^^^^^^^^^^^^^^\n" + 
     "Syntax error on tokens, delete these tokens\n" + 
     "----------\n" + 
     "5. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/Bar.java (at line 6)\n" + 
     "	}\n" + 
     "	^\n" + 
     "Syntax error on token \"}\", delete this token\n" + 
     "----------\n" + 
     "5 problems (5 errors)",
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
		"No .class file created for file X.class in ---OUTPUT_DIR_PLACEHOLDER" +
			"---/X.java because of an IOException: Regular file " +
			"---OUTPUT_DIR_PLACEHOLDER---/X.java cannot be used " +
			"as output directory\n",
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
		"No .class file created for file X.class in ---OUTPUT_DIR_PLACEHOLDER" +
			"---/f/out because of an IOException: " +
			"Could not create output directory ---OUTPUT_DIR_PLACEHOLDER---/f/out\n",
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
			"No .class file created for file p/X.class in " +
				"---OUTPUT_DIR_PLACEHOLDER---/out because of " +
				"an IOException: Could not create subdirectory p into output directory " +
				"---OUTPUT_DIR_PLACEHOLDER---/out\n",
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
		"No .class file created for file p/X.class in " +
			"---OUTPUT_DIR_PLACEHOLDER---/out" + 
			" because of an IOException: Regular file ---OUTPUT_DIR_PLACEHOLDER---" + 
			"/out/p cannot be used as output directory\n",
		true);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=147461
public void _test057_access_restrictions_separator(){
	String oppositeSeparator = File.pathSeparatorChar == ':' ?
			";" : ":";
	this.runNegativeTest(
		new String[] {
			"X.java",
			"/** */\n" + 
			"public class X {\n" + 
			"	OK1 ok1;\n" + 
			"	OK2 ok2;\n" + 
			"	KO ko;\n" + 
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
			"KO.java",
			"/** */\n" + 
			"public class KO {\n" + 
			"	// empty\n" + 
			"}",
		},
        "\"" + OUTPUT_DIR +  File.separator + "X.java\""
        + " -1.5 -g -preserveAllLocals"
        + " -cp \"" + OUTPUT_DIR + "[+OK2" + oppositeSeparator + "-KO]" 
        + " -proceedOnError -referenceInfo -d \"" + OUTPUT_DIR + "\"",
        "", 
        "ERR: invalid spec",
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

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=149118
// .java ending directory name
// instead of a java source file
public void test059(){
	File outputDirectory = new File(OUTPUT_DIR + File.separator + "foo.java");
	outputDirectory.mkdirs();
	this.runNegativeTest(
	    new String[] {},
	    "\"" + OUTPUT_DIR +  File.separator + "foo.java\""
	    + " -1.5 -g -preserveAllLocals "
	    + " -d \"" + OUTPUT_DIR + "/out\"",
	    "",
	    "----------\n" + 
	    "1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/foo.java\n" + 
	    "Cannot read the source from ---OUTPUT_DIR_PLACEHOLDER---/foo.java due to internal exception java.io.FileNotFoundException:---OUTPUT_DIR_PLACEHOLDER---/foo.java (Access is denied)\n" + 
	    "----------\n" + 
	    "1 problem (1 error)",
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
				"public class X {\n" +
				"  foo m;\n" +
				"}",
	        },
	    "\"" + OUTPUT_DIR + "\""
	    + " -1.5 -g -preserveAllLocals"
	    + " -d \"" + OUTPUT_DIR + "/out\"",
		"", 
		"----------\n" + 
		"1. ERROR in ---OUTPUT_DIR_PLACEHOLDER---/X.java (at line 2)\n" + 
		"	foo m;\n" + 
		"	^^^\n" + 
		"foo cannot be resolved to a type\n" + 
		"----------\n" + 
		"1 problem (1 error)",
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
	new File(outputDirName + File.separator + "Y.class").delete();
	new File(outputDirName + File.separator + "Y.java").delete();
	this.runConformTest(
		new String[] {
			"d/X.java",
			"public class X {\n" +
			"  Y m;\n" +
			"}"},
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
	new File(outputDirName + File.separator + "Y.class").delete();
	new File(outputDirName + File.separator + "Y.java").delete();
	this.runConformTest(
		new String[] {
			"d/X.java",
			"public class X {\n" +
			"  Y m;\n" +
			"}"},
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator + 
        	"Z.java\""
        + " -1.5"
        + " -sourcepath \"" + OUTPUT_DIR + File.separator + source1 + "\"" +
        	"[-**/*][-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]",
		"", 
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Z.java (at line 2)\n" + 
		"	X f;\n" + 
		"	^\n" + 
		"Access restriction: The type X is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---/src1\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator + 
        	"Z.java\""
        + " -1.5"
        + " -classpath \"" + OUTPUT_DIR + File.separator + source1 + "\"" +
        	"[-**/*][-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]",
		"", 
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Z.java (at line 2)\n" + 
		"	X f;\n" + 
		"	^\n" + 
		"Access restriction: The type X is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---/src1\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator + 
        	"Z.java\""
        + " -1.5"
        + " -bootclasspath " + getLibraryClasses() + File.pathSeparator + "\"" + 
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator + 
        	"Z.java\""
        + " -1.5"
        + " -bootclasspath " + getLibraryClasses() + File.pathSeparator + "\"" + 
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator + 
        	"Z.java\""
        + " -1.5"
        + " -bootclasspath " + getLibraryClasses() + File.pathSeparator + "\"" + 
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator + 
        	"Z.java\""
        + " -1.5"
        + " -bootclasspath " + getLibraryClasses() + File.pathSeparator + "\"" + 
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
        },
        "\"" + OUTPUT_DIR +  File.separator + 
        	"Z.java\""
        + " -1.5"
        + " -bootclasspath " + getLibraryClasses() + File.pathSeparator + 
        	"\"" + OUTPUT_DIR + File.separator + source1 + "\"" +
        	"[-**/*][-d \"" + OUTPUT_DIR + File.separator + output1 + "\"]",
		"", 
		"----------\n" + 
		"1. WARNING in ---OUTPUT_DIR_PLACEHOLDER---/Z.java (at line 2)\n" + 
		"	X f;\n" + 
		"	^\n" + 
		"Access restriction: The type X is not accessible due to restriction on classpath entry ---OUTPUT_DIR_PLACEHOLDER---/src1\n" + 
		"----------\n" + 
		"1 problem (1 warning)",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			standardXOutputFile.delete();
		}
	} catch (IOException e) {
		fail("could not create jar file");
	}
	this.runConformTest(
		new String[] {
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			standardXOutputFile.delete();
		}
	} catch (IOException e) {
		fail("could not create jar file");
	}
	this.runConformTest(
		new String[] {
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			standardXOutputFile.delete();
		}
	} catch (IOException e) {
		fail("could not create jar file");
	}
	this.runConformTest(
		new String[] {
			"Z.java",
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			"public class Z {\n" +
			"  // X f;\n" +
			"}",
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
			"public class Z {\n" +
			"  // X f;\n" +
			"}",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			"public class Z {\n" +
			"  X f;\n" +
			"}",
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
			"public class X {\n" +
			"  Zork z;\n" +
			"}"},
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

public static Class testClass() {
	return BatchCompilerTest.class;
}
}
