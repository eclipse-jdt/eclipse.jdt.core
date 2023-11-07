/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.dom;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.JavaDocRegion;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TagProperty;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.internal.compiler.parser.JavadocTagConstants;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;

/**
 * Class to test DOM/AST nodes built for Javadoc comments.
 *
 * Most of tests are 'automatic'. It means that to add a new tests, you only need to
 * create one or several CUs and put them in org.eclipse.jdt.core.model.tests/workspace/Converter/src/javadoc/testXXX
 * folder and add the corresponding test in this class:
 * <pre>
 * public void testXXX() throws JavaModelException {
 * 	verifyComments("testXXX");
 * }
 * </pre>
 *
 * Note that when a test fails, the easiest way to debug it is to open
 * a runtime workbench, create a project 'Converter', delete the default 'src' source folder
 * and replace it by a linked source to the 'src' folder of org.eclipse.jdt.core.model.tests/workspace/Converter/src
 * in your workspace.
 *
 * Then open the CU on which the test fails in a ASTView and verify the offset/length
 * of the offending node located at the positions displayed in the console when the test failed...
 *
 * Since 3.4, the failing test also provides the comparison between the source of the comment
 * and the string get from the built DOM/AST nodes in the comment (see {@link ASTConverterJavadocFlattener})
 * but this may be not enough to see precisely the origin of the problem.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ASTConverterJavadocTest_18 extends ConverterTestSetup {

	// Flag to know whether Converter directory should be copied from org.eclipse.jdt.core.tests.model project
	static protected boolean COPY_DIR = true;

	// Test counters
	protected static int[] TEST_COUNTERS = { 0, 0, 0, 0 };
	// Unicode tests
	protected static boolean UNICODE = false;
	// Doc Comment support
	static final String DOC_COMMENT_SUPPORT = System.getProperty("doc.support");
	// List of comments read from source of test
	private static final int LINE_COMMENT = 100;
	private static final int BLOCK_COMMENT =200;
	private static final int DOC_COMMENT = 300;
	List comments = new ArrayList();
	private String chars;
	// List of tags contained in each comment read from test source.
	List allTags = new ArrayList();
	// tags inhibiting inline tags
	static final String TAG_CODE = "code";
	static final String TAG_LITERAL = "literal";
	// Current compilation unit
	protected ICompilationUnit sourceUnit;
	// Test package binding
	protected boolean resolveBinding = true;
	protected boolean packageBinding = true;
	// AST Level
	protected int astLevel = AST.getJLSLatest();
	protected int savedLevel;
	// Debug
	protected String prefix = "";
	protected boolean debug = false;
	protected StringBuffer problems;
	protected String compilerOption = JavaCore.IGNORE;
	protected List failures;
	protected boolean stopOnFailure = true;
	Map savedOptions = null;
	protected ICompilationUnit moduleUnit;

	private static String SNIPPET_TAG = '@' + new String(JavadocTagConstants.TAG_SNIPPET);

	public ASTConverterJavadocTest_18(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new Suite(ASTConverterJavadocTest_18.class.getName());
		buildSuite(suite);
		return suite;
	}

	public static void buildSuite(TestSuite suite) {
		Class c = ASTConverterJavadocTest_18.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
				suite.addTest(new ASTConverterJavadocTest_18(methods[i].getName()));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#copyDirectory(java.io.File, java.io.File)
	 */
	@Override
	protected void copyDirectory(File sourceDir, File targetDir) throws IOException {
		if (COPY_DIR) {
			super.copyDirectory(sourceDir, targetDir);
		} else {
			targetDir.mkdirs();
			File sourceFile = new File(sourceDir, ".project");
			File targetFile = new File(targetDir, ".project");
			targetFile.createNewFile();
			copy(sourceFile, targetFile);
			sourceFile = new File(sourceDir, ".classpath");
			targetFile = new File(targetDir, ".classpath");
			targetFile.createNewFile();
			copy(sourceFile, targetFile);
		}
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TEST_COUNTERS[0]++;
		this.failures = new ArrayList();
		this.problems = new StringBuffer();
		this.workingCopies = null;
		this.savedLevel = this.astLevel;
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		int size = this.failures.size();
		String title = size+" positions/bindings were incorrect in "+getName();
		if (size == 0) {
			TEST_COUNTERS[1]++;
		} else if (this.problems.length() > 0) {
			if (this.debug) {
				System.out.println("Compilation warnings/errors occured:");
				System.out.println(this.problems.toString());
			}
			TEST_COUNTERS[2]++;
		} else {
			TEST_COUNTERS[3]++;
			System.out.println(title+":");
			for (int i=0; i<size; i++) {
				System.out.println("	- "+this.failures.get(i));
			}
		}
//		if (!stopOnFailure) {
			assertTrue(title, size==0 || this.problems.length() > 0);
//		}
		super.tearDown();

		// Restore saved ast level
		this.astLevel = this.savedLevel;
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	public void tearDownSuite() throws Exception {
		// put default options on project
		if (this.currentProject != null && this.savedOptions != null) {
			this.currentProject.setOptions(this.savedOptions);
		}
		super.tearDownSuite();
		if (TEST_COUNTERS[0] != TEST_COUNTERS[1]) {
			NumberFormat intFormat = NumberFormat.getInstance();
			intFormat.setMinimumIntegerDigits(3);
			intFormat.setMaximumIntegerDigits(3);
			System.out.println("=====================================");
			System.out.println(intFormat.format(TEST_COUNTERS[0])+" tests have been executed:");
			System.out.println("  - "+intFormat.format(TEST_COUNTERS[1])+" tests have been actually executed.");
			System.out.println("  - "+intFormat.format(TEST_COUNTERS[2])+" tests were skipped due to compilation errors.");
			System.out.println("  - "+intFormat.format(TEST_COUNTERS[3])+" tests failed.");
		}
	}

	@Override
	public ASTNode runConversion(char[] source, String unitName, IJavaProject project) {
		ASTParser parser = ASTParser.newParser(this.astLevel);
		parser.setSource(source);
		parser.setUnitName(unitName);
		parser.setProject(project);
		parser.setResolveBindings(this.resolveBinding);
		return parser.createAST(null);
	}

	@Override
	public ASTNode runConversion(char[] source, String unitName, IJavaProject project, Map options) {
		if (project == null) {
			ASTParser parser = ASTParser.newParser(this.astLevel);
			parser.setSource(source);
			parser.setUnitName(unitName);
			parser.setCompilerOptions(options);
			parser.setResolveBindings(this.resolveBinding);
			return parser.createAST(null);
		}
		return runConversion(source, unitName, project);
	}

	private char getNextChar(char[] source, int idx) {
			// get next char
			char ch = source[idx];
			int charLength = 1;
			int pos = idx;
			this.chars = null;
			if (ch == '\\' && source[idx+1] == 'u') {
				//-------------unicode traitement ------------
				int c1, c2, c3, c4;
				charLength++;
				while (source[idx+charLength] == 'u') charLength++;
				if (((c1 = ScannerHelper.getHexadecimalValue(source[idx+charLength++])) > 15 || c1 < 0)
					|| ((c2 = ScannerHelper.getHexadecimalValue(source[idx+charLength++])) > 15 || c2 < 0)
					|| ((c3 = ScannerHelper.getHexadecimalValue(source[idx+charLength++])) > 15 || c3 < 0)
					|| ((c4 = ScannerHelper.getHexadecimalValue(source[idx+charLength++])) > 15 || c4 < 0)) {
					return ch;
				}
				ch = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
				this.chars = new String(source, pos, charLength);
			}
			return ch;
	}
	/*
	 * Convert Javadoc source to match Javadoc.toString().
	 * Store converted comments and their corresponding tags respectively
	 * in comments and allTags fields
	 */
	protected void setSourceComment(char[] source) throws ArrayIndexOutOfBoundsException {
		this.comments = new ArrayList();
		this.allTags = new ArrayList();
		StringBuilder buffer = null;
		int comment = 0;
		boolean end = false, lineStarted = false;
		String tag = null;
		List tags = new ArrayList();
		int length = source.length;
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206345
		// when parsing tags such as @code and @literal,
		// any tag should be discarded and considered as plain text until
		// properly closed with closing brace
		boolean considerTagAsPlainText = false;
		int openingBraces = 0;
		char previousChar=0, currentChar=0;
		for (int i=0; i<length;) {
			previousChar = currentChar;
			// get next char
			currentChar = getNextChar(source, i);
			i += (this.chars==null) ? 1 : this.chars.length();

			switch (comment) {
				case 0:
					switch (currentChar) {
						case '/':
							comment = 1; // first char for comments...
							buffer = new StringBuilder();
							if (this.chars == null) buffer.append(currentChar);
							else buffer.append(this.chars);
							break;
						case '\'':
							while (i<length) {
								// get next char
								currentChar = getNextChar(source, i);
								i += (this.chars==null) ? 1 : this.chars.length();
								if (currentChar == '\\') {
									// get next char
									currentChar = getNextChar(source, i);
									i += (this.chars==null) ? 1 : this.chars.length();
								} else {
									if (currentChar == '\'') {
										break;
									}
								}
							}
							break;
						case '"':
							while (i<length) {
								// get next char
								currentChar = getNextChar(source, i);
								i += (this.chars==null) ? 1 : this.chars.length();
								if (currentChar == '\\') {
									// get next char
									currentChar = getNextChar(source, i);
									i += (this.chars==null) ? 1 : this.chars.length();
								} else {
									if (currentChar == '"') {
										// get next char
										currentChar = getNextChar(source, i);
										if (currentChar == '"') {
											i += (this.chars==null) ? 1 : this.chars.length();
										} else {
											break;
										}
									}
								}
							}
							break;
					}
					break;
				case 1: // first '/' has been found...
					switch (currentChar) {
						case '/':
							if (this.chars == null) buffer.append(currentChar);
							else buffer.append(this.chars);
							comment = LINE_COMMENT;
							break;
						case '*':
							if (this.chars == null) buffer.append(currentChar);
							else buffer.append(this.chars);
							comment = 2; // next step
							break;
						default:
							comment = 0;
							break;
					}
					break;
				case 2: // '/*' has been found...
					if (currentChar == '*') {
						comment = 3; // next step...
					} else {
						comment = BLOCK_COMMENT;
					}
					if (this.chars == null) buffer.append(currentChar);
					else buffer.append(this.chars);
					break;
				case 3: // '/**' has bee found, verify that's not an empty block comment
					if (currentChar == '/') { // empty block comment
						if (this.chars == null) buffer.append(currentChar);
						else buffer.append(this.chars);
						this.comments.add(buffer.toString());
						this.allTags.add(new ArrayList());
						comment = 0;
						break;
					}
					comment = DOC_COMMENT;
					// $FALL-THROUGH$ - do not break, directly go to next case...
				case DOC_COMMENT:
					if (tag != null) { // a tag name is currently scanned
						if (currentChar >= 'a' && currentChar <= 'z') {
							tag += currentChar;
						} else {
							if (tag.equalsIgnoreCase(TAG_LITERAL) || tag.equalsIgnoreCase(TAG_CODE)) considerTagAsPlainText = true;
							tags.add(tag);
							tag = null;
						}
					}
					// Some characters are special in javadoc comments
					switch (currentChar) {
						case '@':
							if (!lineStarted) {
								tag = "";
								lineStarted = true;
							} else if (previousChar == '{') {
								// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206345
								if (considerTagAsPlainText) {
									openingBraces++;
								} else {
									tag = "";
									lineStarted = true;
								}
							}
							break;
						case '\r':
						case '\n':
							lineStarted = false;
							break;
						case '*':
							break;
						case '}':
							// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206345
							if (considerTagAsPlainText) {
								if (openingBraces > 0) {
									openingBraces--;
								} else {
									considerTagAsPlainText = false;
								}
							}
							break;
						default:
							if (!Character.isWhitespace(currentChar)) {
								lineStarted = true;
							}
					}
					// $FALL-THROUGH$ - common treatment for block and javadoc comments
				case BLOCK_COMMENT:
					if (this.chars == null) buffer.append(currentChar);
					else buffer.append(this.chars);
					if (end && currentChar == '/') {
						comment = 0;
						lineStarted = false;
						this.comments.add(buffer.toString());
						this.allTags.add(tags);
						tags = new ArrayList();
					}
					end = currentChar == '*';
					break;
				case LINE_COMMENT:
					if (currentChar == '\r' || currentChar == '\n') {
						/*
						if (currentChar == '\r' && source[i+1] == '\n') {
							buffer.append(source[++i]);
						}
						*/
						comment = 0;
						this.comments.add(buffer.toString());
						this.allTags.add(tags);
					} else {
						if (this.chars == null) buffer.append(currentChar);
						else buffer.append(this.chars);
					}
					break;
				default:
					// do nothing
					break;
			}
		}
	}

	/*
	 * Convert Javadoc source to match Javadoc.toString().
	 * Store converted comments and their corresponding tags respectively
	 * in comments and allTags fields
	 */
	char[] getUnicodeSource(char[] source) {
		int length = source.length;
		int unicodeLength = length*6;
		char[] unicodeSource = new char[unicodeLength];
		int u=0;
		for (int i=0; i<length; i++) {
			// get next char
			if (source[i] == '\\' && source[i+1] == 'u') {
				//-------------unicode traitement ------------
				int c1, c2, c3, c4;
				unicodeSource[u++] = source[i];
				unicodeSource[u++] = source[++i];
				if (((c1 = ScannerHelper.getHexadecimalValue(source[i+1])) > 15 || c1 < 0)
					|| ((c2 = ScannerHelper.getHexadecimalValue(source[i+2])) > 15 || c2 < 0)
					|| ((c3 = ScannerHelper.getHexadecimalValue(source[i+3])) > 15 || c3 < 0)
					|| ((c4 = ScannerHelper.getHexadecimalValue(source[i+4])) > 15 || c4 < 0)) {
					throw new RuntimeException("Invalid unicode in source at "+i);
				}
				for (int j=0; j<4; j++) unicodeSource[u++] = source[++i];
			} else {
				unicodeSource[u++] = '\\';
				unicodeSource[u++] = 'u';
				unicodeSource[u++] = '0';
				unicodeSource[u++] = '0';
				int val = source[i]/16;
				unicodeSource[u++] = (char) (val<10 ? val+ 0x30 : val-10+0x61);
				val = source[i]%16;
				unicodeSource[u++] = (char) (val<10 ? val+ 0x30 : val-10+0x61);
			}
		}
		// Return one well sized array
		if (u != unicodeLength) {
			char[] result = new char[u];
			System.arraycopy(unicodeSource, 0, result, 0, u);
			return result;
		}
		return unicodeSource;
	}

	/*
	 * Convert Javadoc source to match Javadoc.toString().
	 * Store converted comments and their corresponding tags respectively
	 * in comments and allTags fields
	 */
	char[] getUnixSource(char[] source) {
		int length = source.length;
		int unixLength = length;
		char[] unixSource = new char[unixLength];
		int u=0;
		for (int i=0; i<length; i++) {
			// get next char
			if (source[i] == '\r' && source[i+1] == '\n') {
				i++;
			}
			unixSource[u++] = source[i];
		}
		// Return one well sized array
		if (u != unixLength) {
			char[] result = new char[u];
			System.arraycopy(unixSource, 0, result, 0, u);
			return result;
		}
		return unixSource;
	}

	/*
	 * Return all tags number for a given Javadoc
	 */
	int allTags(Javadoc docComment) {
		int all = 0;
		// Count main tags
		Iterator tags = docComment.tags().listIterator();
		while (tags.hasNext()) {
			TagElement tagElement = (TagElement) tags.next();
			if (tagElement.getTagName() != null) {
				all++;
			}
			Iterator fragments = tagElement.fragments().listIterator();
			while (fragments.hasNext()) {
				ASTNode node = (ASTNode) fragments.next();
				if (node.getNodeType() == ASTNode.TAG_ELEMENT) {
					all++;
				}
			}
		}
		return all;
	}

	/*
	 * Add a failure to the list. Use only one method as it easier to put breakpoint to
	 * debug failure when it occurs...
	 */
	private void addFailure(String msg) {
		this.failures.add(msg);
	}

	/*
	 * Put the failure message in list instead of throwing exception immediately.
	 * This allow to store several failures per test...
	 * @see tearDown method which finally throws the execption to signal that test fails.
	 */
	protected void assumeTrue(String msg, boolean cond) {
		if (!cond) {
			addFailure(msg);
			if (this.stopOnFailure) assertTrue(msg, cond);
		}
	}

	/*
	 * Put the failure message in list instead of throwing exception immediately.
	 * This allow to store several failures per test...
	 * @see tearDown method which finally throws the execption to signal that test fails.
	 */
	protected void assumeNull(String msg, Object obj) {
		if (obj != null) {
			addFailure(msg);
			if (this.stopOnFailure) assertNull(msg, obj);
		}
	}

	/*
	 * Put the failure message in list instead of throwing exception immediately.
	 * This allow to store several failures per test...
	 * @see tearDown method which finally throws the execption to signal that test fails.
	 */
	protected void assumeNotNull(String msg, Object obj) {
		if (obj == null) {
			addFailure(msg);
			if (this.stopOnFailure) assertNotNull(msg, obj);
		}
	}

	/*
	 * Put the failure message in list instead of throwing exception immediately.
	 * This allow to store several failures per test...
	 * @see tearDown method which finally throws the execption to signal that test fails.
	 */
	protected void assumeEquals(String msg, int expected, int actual) {
		if (expected != actual) {
			addFailure(msg+", expected="+expected+" actual="+actual);
			if (this.stopOnFailure) assertEquals(msg, expected, actual);
		}
	}

	/*
	 * Put the failure message in list instead of throwing exception immediately.
	 * This allow to store several failures per test...
	 * @see tearDown method which finally throws the execption to signal that test fails.
	 */
	protected void assumeEquals(String msg, Object expected, Object actual) {
		if (expected == null && actual == null)
			return;
		if (expected != null && expected.equals(actual))
			return;
		addFailure(msg+", expected:<"+expected+"> actual:<"+actual+'>');
		if (this.stopOnFailure) assertEquals(msg, expected, actual);
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void verifyComments(String test) throws JavaModelException {
		ICompilationUnit[] units = getCompilationUnits("Converter" , "src", "javadoc."+test); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (int i=0; i<units.length; i++) {
			verifyComments(units[i]);
		}
	}

	/*
	 * Verify the comments of a compilation unit.
	 */
	protected void verifyWorkingCopiesComments() throws JavaModelException {
		assumeNotNull("No working copies to verify!", this.workingCopies);
		int length = this.workingCopies.length;
		assumeTrue("We need to have at least one working copy to verify!", length>0);
		for (int i=0; i<length; i++) {
			verifyComments(this.workingCopies[i]);
		}
	}

	/*
	 * Verify the comments of a compilation unit.
	 */
	protected CompilationUnit verifyComments(ICompilationUnit unit) throws JavaModelException {
		// Get test file
		this.sourceUnit = unit;
		this.prefix = unit.getElementName()+": ";

		// Get current project
		String sourceStr = this.sourceUnit.getSource();
		if (this.savedOptions != null && !this.sourceUnit.getJavaProject().getElementName().equals(this.currentProject.getElementName())) {
			this.currentProject.setOptions(this.savedOptions);
			this.savedOptions = null;
		}
		this.currentProject = this.sourceUnit.getJavaProject();
		if (this.savedOptions == null) this.savedOptions = this.currentProject.getOptions(false);

		// set up java project options
		this.currentProject.setOption(JavaCore.COMPILER_PB_INVALID_JAVADOC, this.compilerOption);
		this.currentProject.setOption(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS, this.compilerOption);
		this.currentProject.setOption(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS, this.compilerOption);
		this.currentProject.setOption(JavaCore.COMPILER_PB_METHOD_WITH_CONSTRUCTOR_NAME, JavaCore.IGNORE);
		this.currentProject.setOption(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);

		// Verify source regardings converted comments
		char[] source = sourceStr.toCharArray();
		String fileName = unit.getPath().toString();
		try {
			return verifyComments(fileName, source);
		}
		catch (RuntimeException ex) {
			TEST_COUNTERS[3]++;
			throw ex;
		}
	}

	protected CompilationUnit verifyComments(String fileName, char[] source) {
		return verifyComments(fileName, source, null);
	}

	protected CompilationUnit verifyComments(String fileName, char[] source, Map options) {

		// Verify comments either in unicode or not
		char[] testedSource = source;
		if (UNICODE) {
			testedSource = getUnicodeSource(source);
		}

		// Get comments infos from test file
		setSourceComment(testedSource);

		// Create DOM AST nodes hierarchy
		List unitComments = null;
		String sourceLevel = null;
		String complianceLevel = null;
		if (this.currentProject != null) {
			complianceLevel = this.currentProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
			sourceLevel = this.currentProject.getOption(JavaCore.COMPILER_SOURCE, true);
			this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_18);
			this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_18);
		}
		CompilationUnit compilUnit = (CompilationUnit) runConversion(testedSource, fileName, this.currentProject, options);
		if (this.compilerOption.equals(JavaCore.ERROR)) {
			assumeEquals(this.prefix+"Unexpected problems", 0, compilUnit.getProblems().length); //$NON-NLS-1$
		} else if (this.compilerOption.equals(JavaCore.WARNING)) {
			IProblem[] problemsList = compilUnit.getProblems();
			int length = problemsList.length;
			if (length > 0) {
				this.problems.append("  - "+this.prefix+length+" problems:"); //$NON-NLS-1$
				for (int i = 0; i < problemsList.length; i++) {
					this.problems.append("	+ ");
					this.problems.append(problemsList[i]);
					this.problems.append("\n");
				}
			}
		}
		unitComments = compilUnit.getCommentList();
		assumeNotNull(this.prefix+"Unexpected problems", unitComments);

		// Basic comments verification
		int size = unitComments.size();
		assumeEquals(this.prefix+"Wrong number of comments!", this.comments.size(), size);

		// Verify comments positions and bindings
		for (int i=0; i<size; i++) {
			Comment comment = (Comment) unitComments.get(i);
			List tags = (List) this.allTags.get(i);
			// Verify flattened content
			String stringComment = (String) this.comments.get(i);
//			ASTConverterJavadocFlattener printer = new ASTConverterJavadocFlattener(stringComment);
//			comment.accept(printer);
			String text = new String(testedSource, comment.getStartPosition(), comment.getLength());
			assumeEquals(this.prefix+"Flattened comment does NOT match source!", stringComment, text);
			// Verify javdoc tags positions and bindings
			if (comment.isDocComment()) {
				Javadoc docComment = (Javadoc)comment;
				assumeEquals(this.prefix+"Invalid tags number in javadoc:\n"+docComment+"\n", tags.size(), allTags(docComment));
			}
		}

		/* Verify each javadoc: not implemented yet
		Iterator types = compilUnit.types().listIterator();
		while (types.hasNext()) {
			TypeDeclaration typeDeclaration = (TypeDeclaration) types.next();
			verifyJavadoc(typeDeclaration.getJavadoc());
		}
		*/

		if (sourceLevel != null) {
			this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, complianceLevel);
			this.currentProject.setOption(JavaCore.COMPILER_SOURCE, sourceLevel);
		}
		// Return compilation unit for possible further verifications
		return compilUnit;
	}

	private TagElement getSnippetTag(Javadoc docComment) {
		TagElement snippet = null;
		if (docComment != null) {
			for (Object tag : docComment.tags()) {
				if (tag instanceof TagElement) {
					TagElement tagElement = (TagElement) tag;
					if (SNIPPET_TAG.equals(tagElement.getTagName())) {
						return tagElement;
					}
					List fragments = tagElement.fragments();
					for (Object fragment : fragments) {
						if (fragment instanceof TagElement) {
							TagElement tagElem = (TagElement) fragment;
							if (SNIPPET_TAG.equals(tagElem.getTagName())) {
								return tagElem;
							}
						}
					}

				}
			}
		}
		return snippet;
	}

	public void testSnippetStartJavadoc1() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter_15_1/src/javadoc/X.java",
			"package javadoc;\n" +
			"public class X {\n" +
			"    /**\n" +
			"     * Below is an example snippet\n" +
			"     * {@snippet :\n" +
			"     *  System.out.println(); \n" +
			"     * }\n" +
			"     */\n" +
			"    public static void foo(Object object) {\n" +
			"    }\n" +
			"}\n"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		// Get comments
		List unitComments = compilUnit.getCommentList();
		int size = unitComments.size();
		assertEquals("Wrong number of comments", 1, size);
		Javadoc javadoc = (Javadoc) unitComments.get(0);
		TagElement snippetTag = getSnippetTag(javadoc);
		assertNotNull("Snippet Tag is not present", snippetTag);
		Object validPorperty = snippetTag.getProperty(TagProperty.TAG_PROPERTY_SNIPPET_IS_VALID);
		assertNotNull("Snippet Tag valid property not present", validPorperty);
		assertEquals("Snippet should be valid", true, (validPorperty instanceof Boolean) ? ((Boolean)validPorperty).booleanValue() : false);
	}

	public void testSnippetStartJavadoc2() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter_15_1/src/javadoc/X.java",
			"package javadoc;\n" +
			"public class X {\n" +
			"    /**\n" +
			"     * Below is an example snippet\n" +
			"     * {@snippet \n" +
			"	  * \n" +
			"	  *		\n" +
			"	  *			\n" +
			"	  *\n" +
			"	  *	:\n" +
			"     *  System.out.println(); \n" +
			"     * }\n" +
			"     */\n" +
			"    public static void foo(Object object) {\n" +
			"    }\n" +
			"}\n"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		// Get comments
		List unitComments = compilUnit.getCommentList();
		int size = unitComments.size();
		assertEquals("Wrong number of comments", 1, size);
		Javadoc javadoc = (Javadoc) unitComments.get(0);
		TagElement snippetTag = getSnippetTag(javadoc);
		assertNotNull("Snippet Tag is not present", snippetTag);
		Object validPorperty = snippetTag.getProperty(TagProperty.TAG_PROPERTY_SNIPPET_IS_VALID);
		assertNotNull("Snippet Tag valid property not present", validPorperty);
		assertEquals("Snippet should be valid", true, (validPorperty instanceof Boolean) ? ((Boolean)validPorperty).booleanValue() : false);
	}

	public void testSnippetStartJavadoc3() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter_15_1/src/javadoc/X.java",
			"package javadoc;\n" +
			"public class X {\n" +
			"    /**\n" +
			"     * Below is an example snippet\n" +
			"     * {@snippet \n" +
			"	  * \n" +
			"	  *		\n" +
			"	  			\n" +
			"	  *\n" +
			"	  *	:\n" +
			"     *  System.out.println(); \n" +
			"     * }\n" +
			"     */\n" +
			"    public static void foo(Object object) {\n" +
			"    }\n" +
			"}\n"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		// Get comments
		List unitComments = compilUnit.getCommentList();
		int size = unitComments.size();
		assertEquals("Wrong number of comments", 1, size);
		Javadoc javadoc = (Javadoc) unitComments.get(0);
		TagElement snippetTag = getSnippetTag(javadoc);
		assertNotNull("Snippet Tag is not present", snippetTag);
		Object validPorperty = snippetTag.getProperty(TagProperty.TAG_PROPERTY_SNIPPET_IS_VALID);
		assertNotNull("Snippet Tag valid property not present", validPorperty);
		assertEquals("Snippet should be valid", true, (validPorperty instanceof Boolean) ? ((Boolean)validPorperty).booleanValue() : false);
	}

	public void testSnippetStartJavadoc4() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter_15_1/src/javadoc/X.java",
			"package javadoc;\n" +
			"public class X {\n" +
			"    /**\n" +
			"     * Below is an example snippet\n" +
			"     * {@snippet \n" +
			"	  * \n" +
			"	  *		\n" +
			"	  			\n" +
			"	  *\n" +
			"	  *	: a\n" +
			"     *  System.out.println(); \n" +
			"     * }\n" +
			"     */\n" +
			"    public static void foo(Object object) {\n" +
			"    }\n" +
			"}\n"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		// Get comments
		List unitComments = compilUnit.getCommentList();
		int size = unitComments.size();
		assertEquals("Wrong number of comments", 1, size);
		Javadoc javadoc = (Javadoc) unitComments.get(0);
		TagElement snippetTag = getSnippetTag(javadoc);
		assertNotNull("Snippet Tag is not present", snippetTag);
		Object validPorperty = snippetTag.getProperty(TagProperty.TAG_PROPERTY_SNIPPET_IS_VALID);
		assertNotNull("Snippet Tag valid property not present", validPorperty);
		assertEquals("Snippet should not be valid", false, (validPorperty instanceof Boolean) ? ((Boolean)validPorperty).booleanValue() : false);
	}

	public void testSnippetMultiLineTagsJavadoc1() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter_15_1/src/javadoc/X.java",
			"package javadoc;\n" +
			"public class X {\n" +
			"    /**\n" +
			"     * Below is an example snippet\n" +
			"     * {@snippet :\n" +
			"     *  //Starting Code // @highlight substring=\"out\" :\n" +
			"     *  System.out.println(); // @highlight substring=\"print\"\n" +
			"     * }\n" +
			"     */\n" +
			"    public static void foo(Object object) {\n" +
			"    }\n" +
			"}\n"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		// Get comments
		List unitComments = compilUnit.getCommentList();
		int size = unitComments.size();
		assertEquals("Wrong number of comments", 1, size);
		Javadoc javadoc = (Javadoc) unitComments.get(0);
		TagElement snippetTag = getSnippetTag(javadoc);
		assertNotNull("Snippet Tag is not present", snippetTag);
		Object validPorperty = snippetTag.getProperty(TagProperty.TAG_PROPERTY_SNIPPET_IS_VALID);
		assertNotNull("Snippet Tag valid property not present", validPorperty);
		assertEquals("Snippet should be valid", true, (validPorperty instanceof Boolean) ? ((Boolean)validPorperty).booleanValue() : false);
		List fragments = snippetTag.fragments();
		assertEquals("Three fragments should be created", 3, fragments.size());
		assertEquals("First Tag should be TextElement", true, fragments.get(0) instanceof TextElement);
		assertEquals("Second Tag should be JavaDocRegion", true, fragments.get(1) instanceof JavaDocRegion);
		JavaDocRegion region = (JavaDocRegion) fragments.get(1);
		assertEquals("JavaDocRegion should be dummy", true, region.isDummyRegion());
		assertEquals("third Tag should be TextElement", true, fragments.get(2) instanceof TextElement);
		assertEquals("JavaDocRegion should have 2 tags", 2, region.tags().size());
		assertEquals("JavaDocRegion should have 1 text fragmwent", 1, region.fragments().size());
	}

	public void testSnippetMultiLineTagsJavadoc2() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter_15_1/src/javadoc/X.java",
			"package javadoc;\n" +
			"public class X {\n" +
			"    /**\n" +
			"     * Below is an example snippet\n" +
			"     * {@snippet :\n" +
			"     *  //Starting Code // @highlight substring=\"out\" @highlight substring=\"Sys\" :\n" +
			"     *  System.out.println(); // @highlight substring=\"print\"\n" +
			"     * }\n" +
			"     */\n" +
			"    public static void foo(Object object) {\n" +
			"    }\n" +
			"}\n"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		// Get comments
		List unitComments = compilUnit.getCommentList();
		int size = unitComments.size();
		assertEquals("Wrong number of comments", 1, size);
		Javadoc javadoc = (Javadoc) unitComments.get(0);
		TagElement snippetTag = getSnippetTag(javadoc);
		assertNotNull("Snippet Tag is not present", snippetTag);
		Object validPorperty = snippetTag.getProperty(TagProperty.TAG_PROPERTY_SNIPPET_IS_VALID);
		assertNotNull("Snippet Tag valid property not present", validPorperty);
		assertEquals("Snippet should be valid", true, (validPorperty instanceof Boolean) ? ((Boolean)validPorperty).booleanValue() : false);
		List fragments = snippetTag.fragments();
		assertEquals("Three fragments should be created", 3, fragments.size());
		assertEquals("First Tag should be TextElement", true, fragments.get(0) instanceof TextElement);
		assertEquals("Second Tag should be JavaDocRegion", true, fragments.get(1) instanceof JavaDocRegion);
		JavaDocRegion region = (JavaDocRegion) fragments.get(1);
		assertEquals("JavaDocRegion should be dummy", true, region.isDummyRegion());
		assertEquals("third Tag should be TextElement", true, fragments.get(2) instanceof TextElement);
		assertEquals("JavaDocRegion should have 3 tags", 3, region.tags().size());
		assertEquals("JavaDocRegion should have 1 text fragmwent", 1, region.fragments().size());
	}

	public void testSnippetMultiLineTagsJavadoc3() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter_15_1/src/javadoc/X.java",
			"package javadoc;\n" +
			"public class X {\n" +
			"    /**\n" +
			"     * Below is an example snippet\n" +
			"     * {@snippet :\n" +
			"     *  //Starting Code // @highlight region substring=\"out\" :\n" +
			"     *  System.out.println(); // @highlight substring=\"print\" @end\n" +
			"     * }\n" +
			"     */\n" +
			"    public static void foo(Object object) {\n" +
			"    }\n" +
			"}\n"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		// Get comments
		List unitComments = compilUnit.getCommentList();
		int size = unitComments.size();
		assertEquals("Wrong number of comments", 1, size);
		Javadoc javadoc = (Javadoc) unitComments.get(0);
		TagElement snippetTag = getSnippetTag(javadoc);
		assertNotNull("Snippet Tag is not present", snippetTag);
		Object validPorperty = snippetTag.getProperty(TagProperty.TAG_PROPERTY_SNIPPET_IS_VALID);
		assertNotNull("Snippet Tag valid property not present", validPorperty);
		assertEquals("Snippet should be valid", true, (validPorperty instanceof Boolean) ? ((Boolean)validPorperty).booleanValue() : false);
		List fragments = snippetTag.fragments();
		assertEquals("Four fragments should be created", 4, fragments.size());
		assertEquals("First Tag should be TextElement", true, fragments.get(0) instanceof TextElement);
		assertEquals("Second Tag should be JavaDocRegion", true, fragments.get(1) instanceof JavaDocRegion);
		JavaDocRegion region = (JavaDocRegion) fragments.get(1);
		assertEquals("JavaDocRegion should be dummy", false, region.isDummyRegion());
		assertEquals("third Tag should be TextElement", true, fragments.get(2) instanceof TagElement);
		TagElement tagElem = (TagElement) fragments.get(2);
		assertEquals("third Tag should be TextElement", true, fragments.get(3) instanceof TextElement);
		assertEquals("TagElement should have 1 fragment", 1, tagElem.fragments().size());
		assertEquals("Tag element fragment should be TextElement", true, tagElem.fragments().get(0) instanceof TextElement);
		TextElement textElem =  (TextElement) tagElem.fragments().get(0);
		List<JavaDocRegion> regions = snippetTag.tagRegionsContainingTextElement(textElem);
		assertEquals("regions count should be 1", 1, regions.size());
		assertEquals("original JavaDocRegion should be present here", true, regions.contains(region));
	}

	public void testSnippetMultiLineTagsJavadoc4() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter_15_1/src/javadoc/X.java",
			"package javadoc;\n" +
			"public class X {\n" +
			"    /**\n" +
			"     * Below is an example snippet\n" +
			"     * {@snippet :\n" +
			"     *  //Starting Code // @highlight substring=\"out\" :\n" +
			"     *  System.out.println(); // @highlight substring=\"print\" :\n" +
			"     *  System.out.println();\n" +
			"     * }\n" +
			"     */\n" +
			"    public static void foo(Object object) {\n" +
			"    }\n" +
			"}\n"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		// Get comments
		List unitComments = compilUnit.getCommentList();
		int size = unitComments.size();
		assertEquals("Wrong number of comments", 1, size);
		Javadoc javadoc = (Javadoc) unitComments.get(0);
		TagElement snippetTag = getSnippetTag(javadoc);
		assertNotNull("Snippet Tag is not present", snippetTag);
		Object validPorperty = snippetTag.getProperty(TagProperty.TAG_PROPERTY_SNIPPET_IS_VALID);
		assertNotNull("Snippet Tag valid property not present", validPorperty);
		assertEquals("Snippet should be valid", true, (validPorperty instanceof Boolean) ? ((Boolean)validPorperty).booleanValue() : false);
		List fragments = snippetTag.fragments();
		assertEquals("Four fragments should be created", 4, fragments.size());
		assertEquals("First fragment should be TextElement", true, fragments.get(0) instanceof TextElement);
		assertEquals("Second fragment should be TagElement", true, fragments.get(1) instanceof TagElement);
		TagElement tagElem = (TagElement) fragments.get(1);
		assertEquals("TagElement should have 1 fragment", 1, tagElem.fragments().size());
		assertEquals("Tag element fragment should be TextElement", true, tagElem.fragments().get(0) instanceof TextElement);
		assertEquals("Third fragment should be TagElement", true, fragments.get(1) instanceof TagElement);
		tagElem = (TagElement) fragments.get(2);
		assertEquals("TagElement should have 1 fragment", 1, tagElem.fragments().size());
		assertEquals("Tag element fragment should be TextElement", true, tagElem.fragments().get(0) instanceof TextElement);
		assertEquals("Fourth fragment should be TextElement", true, fragments.get(3) instanceof TextElement);
	}
}
