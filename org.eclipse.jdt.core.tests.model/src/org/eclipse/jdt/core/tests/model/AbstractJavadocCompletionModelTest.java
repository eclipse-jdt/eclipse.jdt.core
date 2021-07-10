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
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Test;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.parser.JavadocTagConstants;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractJavadocCompletionModelTest extends AbstractJavaModelCompletionTests implements JavadocTagConstants {

	// Basic relevance values
	/** R_DEFAULT+R_INTERESTING+R_NON_RESTRICTED<br>= 8 */
	protected final static int JAVADOC_RELEVANCE = R_DEFAULT+ R_INTERESTING+ R_NON_RESTRICTED;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING<br>+R_UNQUALIFIED+R_NON_RESTRICTED<br>= 9 */
	protected final static int R_DRINR= R_DEFAULT+R_RESOLVED+R_INTERESTING+R_NON_RESTRICTED;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING<br>+R_UNQUALIFIED+R_NON_RESTRICTED<br>= 12 */
	protected final static int R_DRIUNR= R_DEFAULT+R_RESOLVED+R_INTERESTING+R_UNQUALIFIED+R_NON_RESTRICTED;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE<br>+R_NON_RESTRICTED<br>= 19 */
	protected static final int R_DRICNR = R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_NON_RESTRICTED;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE<br>+R_QUALIFIED+R_NON_RESTRICTED<br>= 21 */
	protected static final int R_DRICQNR = R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_QUALIFIED+R_NON_RESTRICTED;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE<br>+R_UNQUALIFIED+R_NON_RESTRICTED<br>= 22 */
	protected static final int R_DRICUNR = R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_UNQUALIFIED+R_NON_RESTRICTED;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE+R_EXACT_NAME<br>+R_NON_RESTRICTED<br>= 23 */
	protected static final int R_DRICENNR = R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_EXACT_NAME+R_NON_RESTRICTED;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE+R_EXACT_NAME<br>+R_QUALIFIED+R_NON_RESTRICTED<br>= 25 */
	protected static final int R_DRICENQNR = R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_EXACT_NAME+R_QUALIFIED+R_NON_RESTRICTED;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE+R_EXACT_NAME<br>+R_UNQUALIFIED+R_NON_RESTRICTED<br>= 26 */
	protected static final int R_DRICENUNR = R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_EXACT_NAME+R_UNQUALIFIED+R_NON_RESTRICTED;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE<br>+R_NON_RESTRICTED+R_NON_STATIC<br>= 30 */
	protected static final int R_DRICNRNS = R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_NON_RESTRICTED+R_NON_STATIC;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE+R_EXACT_NAME<br>+R_NON_RESTRICTED+R_NON_STATIC<br>= 34 */
	protected static final int R_DRICENNRNS = R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_EXACT_NAME+R_NON_RESTRICTED+R_NON_STATIC;

	// Exception relevance values
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE<br>+R_NON_RESTRICTED+R_EXCEPTION<br>= 39 */
	protected static final int R_DRICNRE = R_DRICNR+R_EXCEPTION;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE<br>+R_UNQUALIFIED+R_NON_RESTRICTED+R_EXCEPTION<br>= 42 */
	protected static final int R_DRICUNRE = R_DRICUNR+R_EXCEPTION;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE+R_EXACT_NAME<br>+R_NON_RESTRICTED+R_EXCEPTION<br>= 43 */
	protected static final int R_DRICENNRE = R_DRICENNR+R_EXCEPTION;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE+R_EXACT_NAME<br>+R_UNQUALIFIED+R_NON_RESTRICTED+R_EXCEPTION<br>= 46 */
	protected static final int R_DRICENUNRE = R_DRICENUNR+R_EXCEPTION;

	// Exact Expected relevance values
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE<br>+R_NON_RESTRICTED+R_EXACT_EXPECTED_TYPE<br>= 49 */
	protected static final int R_DRICNREET = R_DRICNR+R_EXACT_EXPECTED_TYPE;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE+R_UNQUALIFIED<br>+R_NON_RESTRICTED+R_EXACT_EXPECTED_TYPE<br>= 52 */
	protected static final int R_DRICUNREET = R_DRICUNR+R_EXACT_EXPECTED_TYPE;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE+R_UNQUALIFIED<br>+R_NON_RESTRICTED+R_EXACT_EXPECTED_TYPE+R_EXCEPTION<br>= 72 */
	protected static final int R_DRICUNREETE = R_DRICUNR+R_EXACT_EXPECTED_TYPE+R_EXCEPTION;

	// Exact Expected Exception relevance values
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE+R_NON_RESTRICTED<br>+R_EXCEPTION+R_EXACT_EXPECTED_TYPE<br>= 69 */
	protected static final int R_DRICNREEET = R_DRICNRE+R_EXACT_EXPECTED_TYPE;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE+R_UNQUALIFIED<br>+R_NON_RESTRICTED+R_EXCEPTION<br>
	 * +R_EXACT_EXPECTED_TYPE<br>= 72 */
	protected static final int R_DRICUNREEET = R_DRICUNRE+R_EXACT_EXPECTED_TYPE;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE+R_EXACT_NAME<br>+R_NON_RESTRICTED+R_EXCEPTION<br>
	 * +R_EXACT_EXPECTED_TYPE<br>= 73 */
	protected static final int R_DRICENNREEET = R_DRICENNRE+R_EXACT_EXPECTED_TYPE;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE+R_EXACT_NAME<br>+R_UNQUALIFIED+R_NON_RESTRICTED+R_EXCEPTION<br>
	 * +R_EXACT_EXPECTED_TYPE<br>= 76 */
	protected static final int R_DRICENUNREEET = R_DRICENUNRE+R_EXACT_EXPECTED_TYPE;

	// Inline tag relevance values
	/** R_DEFAULT+R_INTERESTING+R_NON_RESTRICTED<br>+R_INLINE_TAG<br>= 39 */
	protected static final int JAVADOC_RELEVANCE_IT = JAVADOC_RELEVANCE+R_INLINE_TAG;
	/** R_DEFAULT+R_RESOLVED<br+R_INTERESTING+R_NON_RESTRICTED<br>+R_INLINE_TAG<br>= 40 */
	protected static final int R_DRINRIT = R_DRINR+R_INLINE_TAG;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE<br>+R_NON_RESTRICTED+R_INLINE_TAG<br>= 50 */
	protected static final int R_DRICNRIT = R_DRICNR+R_INLINE_TAG;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE+R_UNQUALIFIED<br>+R_NON_RESTRICTED+R_INLINE_TAG<br>= 53 */
	protected static final int R_DRICUNRIT = R_DRICUNR+R_INLINE_TAG;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE+R_EXACT_NAME<br>+R_NON_RESTRICTED+R_INLINE_TAG<br>= 54 */
	protected static final int R_DRICENNRIT = R_DRICENNR+R_INLINE_TAG;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE+R_EXACT_NAME<br>+R_UNQUALIFIED+R_NON_RESTRICTED+R_INLINE_TAG<br>= 57 */
	protected static final int R_DRICENUNRIT = R_DRICENUNR+R_INLINE_TAG;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE<br>+R_NON_RESTRICTED+R_NON_STATIC+R_INLINE_TAG<br>= 61 */
	protected static final int R_DRICNRNSIT = R_DRICNRNS+R_INLINE_TAG;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE+R_NON_RESTRICTED<br>+R_EXACT_EXPECTED_TYPE+R_INLINE_TAG<br>= 80 */
	protected static final int R_DRICNREETIT = R_DRICNREET+R_INLINE_TAG;
	/** R_DEFAULT+R_RESOLVED<br>+R_INTERESTING+R_CASE+R_UNQUALIFIED<br>+R_NON_RESTRICTED+R_EXACT_EXPECTED_TYPE<br>
	 * +R_INLINE_TAG<br>= 83 */
	protected static final int R_DRICUNREETIT = R_DRICUNREET+R_INLINE_TAG;

	// Store all relevance values in array
	private static final int[] RELEVANCES = {
		JAVADOC_RELEVANCE,
		R_DRIUNR,
		R_DRICNR,
		R_DRICQNR,
		R_DRICUNR,
		R_DRICENNR,
		R_DRICENQNR,
		R_DRICENUNR,
		R_DRICNRNS,
		R_DRICENNRNS,
		R_DRICNRE,
		R_DRICUNRE,
		R_DRICENNRE,
		R_DRICENUNRE,
		R_DRICNREET,
		R_DRICUNREET,
		R_DRICNREEET,
		R_DRICUNREEET,
		R_DRICENNREEET,
		R_DRICENUNREEET,
		JAVADOC_RELEVANCE_IT,
		R_DRICNRIT,
		R_DRICUNRIT,
		R_DRICENNRIT,
		R_DRICENUNRIT,
		R_DRICNRNSIT,
		R_DRICNREETIT,
		R_DRICUNREETIT,
	};
	private static final String[] RELEVANCES_NAMES = {
		"JAVADOC_RELEVANCE",
		"R_DRIUNR",
		"R_DRICNR",
		"R_DRICQNR",
		"R_DRICUNR",
		"R_DRICENNR",
		"R_DRICENQNR",
		"R_DRICENUNR",
		"R_DRICNRNS",
		"R_DRICENNRNS",
		"R_DRICNRE",
		"R_DRICUNRE",
		"R_DRICENNRE",
		"R_DRICENUNRE",
		"R_DRICNREET",
		"R_DRICUNREET",
		"R_DRICNREEET",
		"R_DRICUNREEET",
		"R_DRICENNREEET",
		"R_DRICENUNREEET",
		"JAVADOC_RELEVANCE_IT",
		"R_DRICNRIT",
		"R_DRICUNRIT",
		"R_DRICENNRIT",
		"R_DRICENUNRIT",
		"R_DRICNRNSIT",
		"R_DRICNREETIT",
		"R_DRICUNREETIT",
	};

	// Write file contents
	protected static final String WRITE_DIR = System.getProperty("writeDir");
	protected static final File WRITE_DIR_FILE;
	protected static final Set PACKAGE_FILES = new HashSet();
	static {
		File writeDir = null;
		if (WRITE_DIR != null) {
			// Create write directory if necessay
			writeDir = new File(WRITE_DIR);
			if (writeDir.exists()) {
				// perhaps delete all files...
			} else if (!writeDir.mkdirs()) {
				System.err.println(WRITE_DIR+" does NOT exist and cannot be created!!!");
				writeDir = null;
			}

		}
		WRITE_DIR_FILE = writeDir;
	}

	CompletionTestsRequestor2 requestor;
	protected int cursorLocation;
	protected int completionStart;
	protected String replacedText;
	protected String positions;

	public AbstractJavadocCompletionModelTest(String name) {
		super(name);
		this.tabs = 2;
		this.displayName = true;
		this.endChar = "";
	}

	public static Test suite() {
		return buildModelTestSuite(AbstractJavadocCompletionModelTest.class);
	}
	protected void assertResults(String expected) throws JavaModelException {
		int length = this.workingCopies.length;
		String[] sources = new String[length*2];
		for (int i=0; i<length; i++) {
			sources[i*2] = this.workingCopies[i].getPath().toString();
			sources[i*2+1] = this.workingCopies[i].getSource();
		}
		assertResults(sources, expected, this.requestor.getResultsWithoutSorting());
	}
	protected void assertSortedResults(String expected) throws JavaModelException {
		int length = this.workingCopies.length;
		String[] sources = new String[length*2];
		for (int i=0; i<length; i++) {
			sources[i*2] = this.workingCopies[i].getPath().toString();
			sources[i*2+1] = this.workingCopies[i].getSource();
		}
		assertResults(sources, expected, this.requestor.getReversedResults());
	}
	private void assertResults(String[] sources, String expected, String actual) {
		int count = this.requestor.proposalsPtr+1;
		if (!expected.equals(actual)) {
			System.out.println("********************************************************************************");
			if (this.displayName) {
				System.out.print(getName());
				System.out.print(" got ");
				if (count==0)
					System.out.println("no result!");
				else {
					System.out.print(count);
					System.out.print(" result");
					if (count==1)
						System.out.println(":");
					else
						System.out.println("s:");
				}
			}
			if (!this.displayName || count>0) {
				System.out.println(displayString(actual, this.tabs));
				System.out.println(this.endChar);
			}
			System.out.println("--------------------------------------------------------------------------------");
			for (int i=0, length = sources.length; i<length; i+=2) {
				System.out.println(sources[i]);
				System.out.println(sources[i+1]);
			}
		}
		assertEquals(
			"Completion proposals are not correct!",
			expected,
			actual
		);
	}
	protected void assertNoProblem(String path) {
		String problem = this.requestor.getProblem();
		if (problem.length() > 0) {
			System.out.println("********************************************************************************");
			if (this.displayName) {
				System.out.print(getName());
				System.out.println(" contains an error although it should NOT:");
			}
			System.out.println(displayString(problem, this.tabs));
			System.out.println("--------------------------------------------------------------------------------");
			System.out.println(this.workingCopies[0].getPath().toString()+'\n');
			try {
				System.out.println(this.workingCopies[0].getSource());
			} catch (JavaModelException e) {
				// forget it
			}
			assertEquals(
				path+" should have NO problem!",
				"",
				problem
			);
		}
	}

	protected void completeInJavadoc(String path, String source, boolean showPositions, String completeBehind) throws JavaModelException {
		completeInJavadoc(path, source, showPositions, completeBehind, 1 /* first index */);
	}

	protected void completeInJavadoc(String path, String source, boolean showPositions, String completeBehind, boolean last) throws JavaModelException {
		completeInJavadoc(path, source, showPositions, completeBehind, last ? -1 : 1);
	}

	protected void completeInJavadoc(String path, String source, boolean showPositions, String completeBehind, int occurencePosition) throws JavaModelException {
		completeInJavadoc(new String[] { path, source }, showPositions, completeBehind, occurencePosition, null);
	}

	protected void completeInJavadoc(String path, String source, boolean showPositions, String completeBehind, int occurencePosition, int[] ignoreList) throws JavaModelException {
		completeInJavadoc(new String[] { path, source }, showPositions, completeBehind, occurencePosition, ignoreList);
	}
	protected void completeInJavadoc(String[] sources, boolean showPositions, String completeBehind) throws JavaModelException {
		completeInJavadoc(sources, showPositions, completeBehind, 1, null);
	}

	protected void completeInJavadoc(String[] sources, boolean showPositions, String completeBehind, int occurencePosition) throws JavaModelException {
		completeInJavadoc(sources, showPositions, completeBehind, occurencePosition, null);
	}

	protected void completeInJavadoc(String[] sources, boolean showPositions, String completeBehind, int occurencePosition, int[] ignoreList) throws JavaModelException {
		assertNotNull("We should have sources!!!", sources);
		assertTrue("Invalid number of sources!!!",  sources.length%2==0);

		// Build working copy(ies)
		int length = sources.length / 2;
		this.workingCopies = new ICompilationUnit[length];
		for (int i=0; i<length; i++) {
			this.workingCopies[i] = getWorkingCopy(sources[i*2], sources[i*2+1]);
			if (WRITE_DIR != null) 	writeFiles(sources);
		}

		// Wait for indexes
		waitUntilIndexesReady();

		// Complete
		this.requestor = new CompletionTestsRequestor2(true, false, showPositions);
		if (ignoreList != null) {
			for (int i = 0; i < ignoreList.length; i++) {
				this.requestor.setIgnored(ignoreList[i], true);
			}
		}
		String source = this.workingCopies[0].getSource();
		this.replacedText = completeBehind;
		this.completionStart = -1;
		int cursorPos = this.replacedText.length();
		if (occurencePosition < -10) { // case where we want to specify directly the cursor location relatively to completion start
			this.completionStart = source.indexOf(this.replacedText);
			cursorPos = -occurencePosition - 10;
		} else if (occurencePosition < 0) {
			this.completionStart = source.lastIndexOf(this.replacedText);
			int max = -occurencePosition;
			for (int i=1; i<max; i++) {
				this.completionStart = source.lastIndexOf(this.replacedText, this.completionStart);
			}
		} else {
			this.completionStart = source.indexOf(this.replacedText);
			int shift = this.replacedText.length();
			for (int i=1; i<occurencePosition && this.completionStart>0; i++) {
				this.completionStart = source.indexOf(this.replacedText, this.completionStart+shift);
			}
		}
		assertTrue("We should have found "+occurencePosition+" occurence(s) of '"+this.replacedText+"' in:\n"+source, this.completionStart>0);
		this.cursorLocation =  this.completionStart + cursorPos;
		this.workingCopies[0].codeComplete(this.cursorLocation, this.requestor, this.wcOwner);
		assertNoProblem(sources[0]);

		// Store replacement info
		if (occurencePosition == 0) { // special case for completion on empty token...
			this.completionStart = this.cursorLocation;
		}
		int endPosition = this.cursorLocation;
		char ch = source.charAt(endPosition);
		if (Character.isJavaIdentifierPart(ch) || ch == '>' || ch == '}' || ch == '(' || ch == ')') {
			do {
				ch = source.charAt(++endPosition);
			} while (Character.isJavaIdentifierPart(ch) || ch == '>' || ch == '}' || ch == '(' || ch == ')');
		}
		this.positions = "["+this.completionStart+", "+endPosition+"], ";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#displayString(java.lang.String, int)
	 */
	@Override
	protected String displayString(String toPrint, int indent) {
		String toDisplay = super.displayString(toPrint, indent);
		int openBracket = toDisplay.indexOf(", [");
		if (openBracket > 0) {
			StringBuilder buffer = new StringBuilder();
			int closeBracket = 0;
			while (openBracket > 0) {
				buffer.append(toDisplay.substring(closeBracket, openBracket+2));
				closeBracket = toDisplay.indexOf("], ", openBracket+3);
				if (closeBracket < 0) break; // invalid
				closeBracket += 3;
				buffer.append("\"+this.positions+");
				int i=0;
				while (toDisplay.charAt(closeBracket+i) != '}') i++;
				try {
					int relevance = Integer.parseInt(toDisplay.substring(closeBracket, closeBracket+i));
					int length = RELEVANCES.length;
					boolean found = false;
					for (int r=0; !found && r<length; r++) {
						if (RELEVANCES[r] == relevance) {
							buffer.append(RELEVANCES_NAMES[r]);
							buffer.append("+\"");
							found = true;
						}
					}
					if (!found) {
						buffer.append('"');
						buffer.append(relevance);
					}
					closeBracket += i;
				}
				catch (NumberFormatException nfe) {
					System.err.println(nfe.getMessage()+" should not occur!");
				}
				openBracket = toDisplay.indexOf(", [", closeBracket);
			}
			if (closeBracket > 0) {
				buffer.append(toDisplay.substring(closeBracket, toDisplay.length()));
				toDisplay = buffer.toString();
			}
		}
		return toDisplay.replaceAll(", 8}", ", \"+JAVADOC_RELEVANCE+\"}");
	}

	protected void setUpProjectOptions(String compliance) throws JavaModelException {
		try {
			setUpProjectCompliance(COMPLETION_PROJECT, compliance);
		} catch (IOException e) {
			assertTrue("Unexpected IOException: "+e.getMessage(), false);
		}
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		if (COMPLETION_PROJECT == null)  {
			COMPLETION_PROJECT = setUpJavaProject("Completion");
			createFolder(new Path("/Completion/src/javadoc/tags"));
		} else {
			setUpProjectCompliance(COMPLETION_PROJECT, "1.4");
			this.currentProject = COMPLETION_PROJECT;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		this.requestor = null;
		super.tearDown();
	}

	@Override
	public void tearDownSuite() throws Exception {
		deleteFolder(new Path("/Completion/src/javadoc/tags"));
		super.tearDownSuite();
		if (COMPLETION_SUITES == null) {
			COMPLETION_PROJECT = null;
		}
	}

	/*
	 * Write files for self-hosting debug.
	 */
	protected void writeFiles(String[] sources) {

		// Get write directory path
		if (WRITE_DIR_FILE == null) return;

		// Get test name
		String testName = getName();
		int idx = testName.indexOf(" - ");
		if (idx > 0) {
			testName = testName.substring(idx+3);
		}
//		testName = "Test"+testName.substring(4);

		// Write sources to dir
		int length = sources.length / 2;
		String[][] names = new String[length][3];
		for (int i=0; i<length; i++) {

			// Get pathes
			IPath filePath = new Path(sources[2*i]).removeFirstSegments(2); // remove project and source folder
			IPath dirPath = filePath.removeLastSegments(1);
			String fileDir = dirPath.toString();
			String typeName = filePath.removeFileExtension().lastSegment();

			// Create package dir or delete files if already exist
			File packageDir = new File(WRITE_DIR_FILE, fileDir);
			if (!PACKAGE_FILES.contains(packageDir)) {
				if (packageDir.exists()) {
					PACKAGE_FILES.add(packageDir);
					Util.delete(packageDir);
				} else if (packageDir.mkdirs()) {
					PACKAGE_FILES.add(packageDir);
				} else {
					System.err.println(packageDir+" does not exist and CANNOT be created!!!");
					continue;
				}
			}

			// Store names info
			names[i][0] = typeName;
			String fileName = (typeName.length() <= 3) ? typeName : typeName.substring(0, typeName.length()-3);
			fileName = fileName + testName.substring(4);
			names[i][1] = fileName;
			names[i][2] = packageDir.getAbsolutePath()+"\\"+fileName+".java";
		}

		// Write modified contents
		for (int i=0; i<length; i++) {
			String contents = sources[2*i+1];
			for (int j=0; j<length; j++) {
				contents = contents.replaceAll(names[j][0], names[j][1]);
			}
			String fullPathName = names[i][2];
			System.out.println("Write file "+fullPathName);
			Util.writeToFile(contents, fullPathName);
		}
	}
}
