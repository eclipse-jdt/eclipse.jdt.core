/*******************************************************************************
 * Copyright (c) 2007, 2009 BEA Systems, Inc. and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

import junit.framework.TestCase;

/**
 * Tests for the implementation of javax.annotation.processing.Messager
 * @since 3.3
 */
public class MessagerTests extends TestCase {
	private static final String COMPARE_OK = "OK";
	// See corresponding usages in the MessagerProc class
	private static final String MESSAGERPROCNAME = "org.eclipse.jdt.compiler.apt.tests.processors.messager.MessagerProc";
	
	// Expected output for the javac compiler.
	// Note that this is actually a series of regular expressions, which will be matched line by line!
	// This is required in order to deal with things like hard-coded paths.
	private static final String[] EXPECTED_JAVAC_MESSAGES = {
		"Note\\: Informational message not associated with an element",
		".*D\\.java\\:15\\: Error on element D",
		"public class D \\{",
		"       \\^",
		".*D\\.java\\:12\\: Error on element D",
		"\\@AnnoZ\\(",
		"\\^",
		".*D\\.java\\:12\\: Error on element D",
		"\\@AnnoZ\\(",
		"\\^",
		"error\\: Error on element java\\.lang\\.String",
		".*E\\.java\\:12\\: warning\\: Warning on method foo",
		"	public void foo\\(int i\\) \\{\\}",
		"	            \\^",
		".*E\\.java\\:14\\: Note\\: Note for field j",
		"	public static int j;",
		"	                  \\^",
		".*D\\.java\\:19\\: warning\\: Error on parameter of D\\.methodDvoid",
		"	public void methodDvoid\\(DEnum dEnum1\\) \\{",
		"	                              \\^",
		"4 errors"
	};
	
	// Expected output for Eclipse compiler.
	// Note that this is actually a series of regular expressions, which will be matched line by line!
	// This is required in order to deal with things like hard-coded paths.
	private static final String[] EXPECTED_ECLIPSE_MESSAGES = {
		"----------", 
		"1\\. WARNING in \\(original file name is not available\\)", 
		"Informational message not associated with an element", 
		"----------", 
		"2\\. ERROR in .*D\\.java \\(at line 15\\)", 
		"	public class D \\{", 
		"	             \\^", 
		"Error on element D", 
		"----------", 
		"3\\. ERROR in .*D\\.java \\(at line 12\\)", 
		"	@AnnoZ\\(",
		"	\\^\\^\\^\\^\\^\\^",
		"Error on element D", 
		"----------", 
		"4\\. ERROR in .*D\\.java \\(at line 13\\)", 
		"	annoZString = \"annoZOnD\"\\)", 
		"	\\^\\^\\^\\^\\^\\^\\^\\^\\^\\^\\^", 
		"Error on element D", 
		"----------", 
		"5\\. ERROR in \\(original file name is not available\\)", 
		"Error on element java\\.lang\\.String", 
		"----------", 
		"6\\. WARNING in .*E\\.java \\(at line 12\\)", 
		"	public void foo\\(int i\\) \\{\\}", 
		"	            \\^\\^\\^\\^\\^\\^\\^\\^\\^\\^", 
		"Warning on method foo", 
		"----------", 
		"7\\. WARNING in .*E\\.java \\(at line 14\\)", 
		"	public static int j;", 
		"	                  \\^", 
		"Note for field j", 
		"----------", 
		"8\\. WARNING in .*D\\.java \\(at line 19\\)", 
		"	public void methodDvoid\\(DEnum dEnum1\\) \\{", 
		"	                              \\^\\^\\^\\^\\^\\^", 
		"Error on parameter of D\\.methodDvoid", 
		"----------", 
		"8 problems \\(4 errors, 4 warnings\\)" 
	};
	
	/**
	 * Compare an actual multi-line string against an array of regular expressions
	 * representing an expected string. Each regular expression will be matched against
	 * one line of the actual string.
	 * @return the string "OK" if every line in the actual was matched by the corresponding regex
	 * in the expected, or an error string if not.
	 */
	private static String compareRegexLines(String actual, String[] expected) {
		String[] actualLines = actual.split("\n");
		if (actualLines.length != expected.length) {
			return "ERROR: expected " + expected.length + " lines but found " + actualLines.length;
		}
		int i = 0;
		for (String pattern : expected) {
			int iCR = actualLines[i].indexOf('\r');
			actualLines[i] = iCR > 0 ? actualLines[i].substring(0, iCR) : actualLines[i];
			int iNL = actualLines[i].indexOf('\n');
			actualLines[i] = iNL > 0 ? actualLines[i].substring(0, iNL) : actualLines[i];
			if (!Pattern.matches(pattern, actualLines[i++])) {
				--i;
				return "ERROR: mismatch at line " + i + ": actual line was [" + actualLines[i] + "]";
			}
		}
		return COMPARE_OK;
	}
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
	}

	/**
	 * Validate the testMessager test against the javac compiler.
	 * @throws IOException 
	 */
	public void testMessagerWithSystemCompiler() throws IOException {
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		String actualErrors = internalTestMessager(compiler);
		assertEquals(COMPARE_OK, compareRegexLines(actualErrors, EXPECTED_JAVAC_MESSAGES));
	}

	/**
	 * Attempt to report errors on various elements, using the Eclipse compiler.
	 * @throws IOException 
	 */
	public void testMessagerWithEclipseCompiler() throws IOException {
		JavaCompiler compiler = BatchTestUtils.getEclipseCompiler();
		String actualErrors = internalTestMessager(compiler);
		assertEquals(COMPARE_OK, compareRegexLines(actualErrors, EXPECTED_ECLIPSE_MESSAGES));
	}

	/**
	 * Attempt to report errors on various elements.
	 * @throws IOException
	 * @return the outputted errors, if the test succeeded enough to generate them
	 */
	private String internalTestMessager(JavaCompiler compiler) throws IOException {
		System.clearProperty(MESSAGERPROCNAME);
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "errors");
		BatchTestUtils.copyResources("targets/errors", targetFolder);

		// Turn on the MessagerProc - without this, it will just return without doing anything
		List<String> options = new ArrayList<String>();
		options.add("-A" + MESSAGERPROCNAME);

		// Invoke processing by compiling the targets.model resources
		StringWriter errors = new StringWriter();
		boolean success = BatchTestUtils.compileTreeWithErrors(compiler, options, targetFolder, errors);
		
		assertTrue("errors should not be empty", errors.getBuffer().length() != 0);
		assertTrue("Compilation should have failed due to expected errors, but it didn't", !success);

		// If it succeeded, the processor will have set this property to "succeeded";
		// if not, it will set it to an error value.
		String property = System.getProperty(MESSAGERPROCNAME);
		assertNotNull("No property", property);
		assertEquals("succeeded", property);
		
		//System.out.println(errors.getBuffer().toString());
		
		return errors.getBuffer().toString();
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		System.clearProperty(MESSAGERPROCNAME);
		super.tearDown();
	}
	
}
