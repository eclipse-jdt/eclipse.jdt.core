/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.io.*;
import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
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
 * Since 3.4, the failing test also provides the comparision between the source of the comment
 * and the string get from the built DOM/AST nodes in the comment (see {@link ASTConverterJavadocFlattener})
 * but this may be not enough to see precisely the origin of the problem.
 */
public class ASTConverterJavadocTest extends ConverterTestSetup {

	// Flag to know whether Converter directory should be copied from org.eclipse.jdt.core.tests.model project
	static protected boolean COPY_DIR = true;

	// Test counters
	protected static int[] TEST_COUNTERS = { 0, 0, 0, 0 };
	// Unicode tests
	protected static boolean UNICODE = false;
	// Unix tests
	final boolean unix;
	static final String UNIX_SUPPORT = System.getProperty("unix");
	// Doc Comment support
	static final String DOC_COMMENT_SUPPORT = System.getProperty("doc.support");
	final String docCommentSupport;

	// List of comments read from source of test
	private static final int LINE_COMMENT = 100;
	private static final int BLOCK_COMMENT =200;
	private static final int DOC_COMMENT = 300;
	List comments = new ArrayList();
	private String chars;
	// List of tags contained in each comment read from test source.
	List allTags = new ArrayList();
	// Current compilation unit
	protected ICompilationUnit sourceUnit;
	// Test package binding
	protected boolean resolveBinding = true;
	protected boolean packageBinding = true;
	// AST Level
	/** @deprecated using deprecated code */
	protected int astLevel = AST.JLS2;
	protected int savedLevel;
	// Debug
	protected String prefix = "";
	protected boolean debug = false;
	protected StringBuffer problems;
	protected String compilerOption = JavaCore.IGNORE;
	protected List failures;
	protected boolean stopOnFailure = true;
	// Project
	protected IJavaProject currentProject;
	Map savedOptions = null;

	/**
	 * @param name
	 * @param support
	 */
	public ASTConverterJavadocTest(String name, String support, String unix) {
		super(name);
		this.docCommentSupport = support;
		this.unix = "true".equals(unix);
	}
	/**
	 * @param name
	 */
	public ASTConverterJavadocTest(String name) {
		this(name, JavaCore.ENABLED, UNIX_SUPPORT);
	}

	public static Test suite() {
		TestSuite suite = new Suite(ASTConverterJavadocTest.class.getName());
//		String param = System.getProperty("unicode");
//		if ("true".equals(param)) {
//			unicode = true;
//		}
//		String param = System.getProperty("unix");
//		if ("true".equals(param)) {
//			unix = true;
//		}
		if (DOC_COMMENT_SUPPORT == null) {
			buildSuite(suite, JavaCore.ENABLED);
			buildSuite(suite, JavaCore.DISABLED);
		} else {
			String support = DOC_COMMENT_SUPPORT==null ? JavaCore.DISABLED : (DOC_COMMENT_SUPPORT.equals(JavaCore.DISABLED)?JavaCore.DISABLED:JavaCore.ENABLED);
			buildSuite(suite, support);
		}
		return suite;

//		Run test cases subset
//		COPY_DIR = false;
//		System.err.println("WARNING: only subset of tests will be executed!!!");
//		suite.addTest(new ASTConverterJavadocTest("testBug165525"));
//		return suite;
	}

	public static void buildSuite(TestSuite suite, String support) {
		Class c = ASTConverterJavadocTest.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
				suite.addTest(new ASTConverterJavadocTest(methods[i].getName(), support, UNIX_SUPPORT));
			}
		}
		// when unix support not specified, also run using unix format
		if (UNIX_SUPPORT == null && JavaCore.ENABLED.equals(support)) {
			for (int i = 0, max = methods.length; i < max; i++) {
				if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
					suite.addTest(new ASTConverterJavadocTest(methods[i].getName(), support, "true"));
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#copyDirectory(java.io.File, java.io.File)
	 */
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
	 * @see junit.framework.TestCase#getName()
	 */
	public String getName() {
		String strUnix = this.unix ? " - Unix" : "";
		return "Doc "+this.docCommentSupport+strUnix+" - "+super.getName();
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
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

	public ASTNode runConversion(char[] source, String unitName, IJavaProject project) {
		ASTParser parser = ASTParser.newParser(this.astLevel);
		parser.setSource(source);
		parser.setUnitName(unitName);
		parser.setProject(project);
		parser.setResolveBindings(this.resolveBinding);
		return parser.createAST(null);
	}

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
				if (((c1 = ScannerHelper.getNumericValue(source[idx+charLength++])) > 15
					|| c1 < 0)
					|| ((c2 = ScannerHelper.getNumericValue(source[idx+charLength++])) > 15 || c2 < 0)
					|| ((c3 = ScannerHelper.getNumericValue(source[idx+charLength++])) > 15 || c3 < 0)
					|| ((c4 = ScannerHelper.getNumericValue(source[idx+charLength++])) > 15 || c4 < 0)) {
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
		StringBuffer buffer = null;
		int comment = 0;
		boolean end = false, lineStarted = false;
		String tag = null;
		List tags = new ArrayList();
		int length = source.length;
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
							buffer = new StringBuffer();
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
							tags.add(tag);
							tag = null;
						}
					}
					// Some characters are special in javadoc comments
					switch (currentChar) {
						case '@':
							if (!lineStarted || previousChar == '{') {
								tag = "";
								lineStarted = true;
							}
							break;
						case '\r':
						case '\n':
							lineStarted = false;
							break;
						case '*':
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
				if (((c1 = ScannerHelper.getNumericValue(source[i+1])) > 15
					|| c1 < 0)
					|| ((c2 = ScannerHelper.getNumericValue(source[i+2])) > 15 || c2 < 0)
					|| ((c3 = ScannerHelper.getNumericValue(source[i+3])) > 15 || c3 < 0)
					|| ((c4 = ScannerHelper.getNumericValue(source[i+4])) > 15 || c4 < 0)) {
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

	/*
	 * Verify positions of tags in source
	 */
	private void verifyPositions(Javadoc docComment, char[] source) {
		boolean stop = this.stopOnFailure;
		this.stopOnFailure = false;
		// Verify javadoc start and end position
		int start = docComment.getStartPosition();
		int end = start+docComment.getLength()-1;
		assumeTrue(this.prefix+"Misplaced javadoc start at <"+start+">: "+docComment, source[start++] == '/' && source[start++] == '*' && source[start++] == '*');
		// Get first meaningful character
		int tagStart = start;
		// Verify tags
		Iterator tags = docComment.tags().listIterator();
		while (tags.hasNext()) {
			while (source[tagStart] == '*' || Character.isWhitespace(source[tagStart])) {
				tagStart++; // purge non-stored characters
			}
			TagElement tagElement = (TagElement) tags.next();
			int teStart = tagElement.getStartPosition();
			assumeEquals(this.prefix+"Wrong start position <"+teStart+"> for tag element: "+tagElement, tagStart, teStart);
			verifyPositions(tagElement, source);
			tagStart += tagElement.getLength();
		}
		while (source[tagStart] == '*' || Character.isWhitespace(source[tagStart])) {
			tagStart++; // purge non-stored characters
		}
		assumeTrue(this.prefix+"Misplaced javadoc end at <"+tagStart+'>', source[tagStart-1] == '*' && source[tagStart] == '/');
		assumeEquals(this.prefix+"Wrong javadoc length at <"+end+">: ", tagStart, end);
		this.stopOnFailure = stop;
		if (stop && this.failures.size() > 0) {
			String expected = new String(source, docComment.getStartPosition(), docComment.getLength());
			ASTConverterJavadocFlattener flattener = new ASTConverterJavadocFlattener(expected);
			docComment.accept(flattener);
			assertEquals("Unexpected errors while verifying javadoc comment positions!", expected, flattener.getResult());
		}
	}

	/**
	 * Verify positions of fragments in source
	 * @deprecated using deprecated code
	 */
	private void verifyPositions(TagElement tagElement, char[] source) {
		String text = null;
		// Verify tag name
		String tagName = tagElement.getTagName();
		int tagStart = tagElement.getStartPosition();
		if (tagElement.isNested()) {
			assumeEquals(this.prefix+"Wrong start position <"+tagStart+"> for "+tagElement, '{', source[tagStart++]);
		}
		if (tagName != null) {
			text= new String(source, tagStart, tagName.length());
			assumeEquals(this.prefix+"Misplaced tag name at <"+tagStart+">: ", tagName, text);
			tagStart += tagName.length();
		}
		// Verify each fragment
		ASTNode previousFragment = null;
		Iterator elements = tagElement.fragments().listIterator();
		while (elements.hasNext()) {
			ASTNode fragment = (ASTNode) elements.next();
			if (fragment.getNodeType() == ASTNode.TEXT_ELEMENT) {
				if (previousFragment == null && TagElement.TAG_PARAM.equals(tagName) && ((TextElement)fragment).getText().equals("<")) { // special case here for @param <E> syntax
					int start = tagStart;
					// verify '<'
					while (source[start] == ' ' || Character.isWhitespace(source[start])) {
						start++; // purge white characters
					}
					text = new String(source, start, fragment.getLength());
					assumeEquals(this.prefix+"Misplaced text element at <"+fragment.getStartPosition()+">: ", text, ((TextElement) fragment).getText());
					start += fragment.getLength();
					// verify simple name
					assumeTrue(this.prefix+"Unexpected fragment end for "+tagElement, elements.hasNext());
					fragment = (ASTNode) elements.next();
					while (source[start] == ' ' || Character.isWhitespace(source[start])) {
						start++; // purge white characters
					}
					assumeEquals(this.prefix+"Unexpected node type for tag element "+tagElement, ASTNode.SIMPLE_NAME, fragment.getNodeType());
					Name name = (Name) fragment;
					verifyNamePositions(start, name, source);
					start += fragment.getLength();
					// verify simple name
					assumeTrue(this.prefix+"Unexpected fragment end for "+tagElement, elements.hasNext());
					fragment = (ASTNode) elements.next();
					while (source[start] == ' ' || Character.isWhitespace(source[start])) {
						start++; // purge white characters
					}
					text = new String(source, start, fragment.getLength());
					assumeEquals(this.prefix+"Misplaced text element at <"+fragment.getStartPosition()+">: ", text, ((TextElement) fragment).getText());
					start += fragment.getLength();
					// reset fragment as simple name to avoid issue with next text element
					fragment = name;
					tagStart += (start- tagStart) - name.getLength();
				} else {
					if (previousFragment == null) {
						if (tagName != null && (source[tagStart] == '\r' || source[tagStart] == '\n')) {
							while (source[tagStart] == '*' || Character.isWhitespace(source[tagStart])) {
								tagStart++; // purge non-stored characters
							}
						}
					} else {
						if (previousFragment.getNodeType() == ASTNode.TEXT_ELEMENT) {
							assumeTrue(this.prefix+"Wrong length at <"+previousFragment.getStartPosition()+"> for text element "+previousFragment, (source[tagStart] == '\r' /* && source[tagStart+1] == '\n' */ || source[tagStart] == '\n'));
							while (source[tagStart] == '*' || Character.isWhitespace(source[tagStart])) {
								tagStart++; // purge non-stored characters
							}
						} else if (TagElement.TAG_PARAM.equals(tagName) && previousFragment.getNodeType() == ASTNode.SIMPLE_NAME && ((TextElement)fragment).getText().equals(">")) {
							while (source[tagStart] == ' ' || Character.isWhitespace(source[tagStart])) {
								tagStart++; // purge white characters
							}
						} else {
							int start = tagStart;
							boolean newLine = false;
							while (source[start] == '*' || Character.isWhitespace(source[start])) {
								start++; // purge non-stored characters
								if (source[tagStart] == '\r' || source[tagStart] == '\n') {
									newLine = true;
								}
							}
							if (newLine) tagStart = start;
						}
					}
					text = new String(source, tagStart, fragment.getLength());
					assumeEquals(this.prefix+"Misplaced text element at <"+fragment.getStartPosition()+">: ", text, ((TextElement) fragment).getText());
				}
			} else {
				while (source[tagStart] == '*' || Character.isWhitespace(source[tagStart])) {
					tagStart++; // purge non-stored characters
				}
				if (fragment.getNodeType() == ASTNode.SIMPLE_NAME || fragment.getNodeType() == ASTNode.QUALIFIED_NAME) {
					verifyNamePositions(tagStart, (Name) fragment, source);
				} else if (fragment.getNodeType() == ASTNode.TAG_ELEMENT) {
					TagElement inlineTag = (TagElement) fragment;
					assumeEquals(this.prefix+"Tag element <"+inlineTag+"> has wrong start position", tagStart, inlineTag.getStartPosition());
					verifyPositions(inlineTag, source);
				} else if (fragment.getNodeType() == ASTNode.MEMBER_REF) {
					MemberRef memberRef = (MemberRef) fragment;
					// Store start position
					int start = tagStart;
					// Verify qualifier position
					Name qualifier = memberRef.getQualifier();
					if (qualifier != null) {
						verifyNamePositions(start, qualifier, source);
						start += qualifier.getLength();
						while (source[start] == '*' || Character.isWhitespace(source[start])) {
							start++; // purge non-stored characters
						}
					}
					// Verify member separator position
					assumeEquals(this.prefix+"Misplaced # separator at <"+start+"> for member ref "+memberRef, '#', source[start]);
					start++;
					while (source[start] == '*' || Character.isWhitespace(source[start])) {
						start++; // purge non-stored characters
					}
					// Verify member name position
					Name name = memberRef.getName();
					text = new String(source, start, name.getLength());
					assumeEquals(this.prefix+"Misplaced member ref at <"+start+">: ", text, name.toString());
					verifyNamePositions(start, name, source);
				} else if (fragment.getNodeType() == ASTNode.METHOD_REF) {
					MethodRef methodRef = (MethodRef) fragment;
					// Store start position
					int start = tagStart;
					// Verify qualifier position
					Name qualifier = methodRef.getQualifier();
					if (qualifier != null) {
						verifyNamePositions(start, qualifier, source);
						start += qualifier.getLength();
						while (source[start] == '*' || Character.isWhitespace(source[start])) {
							start++; // purge non-stored characters
						}
					}
					// Verify member separator position
					assumeEquals(this.prefix+"Misplaced # separator at <"+start+"> for method ref: "+methodRef, '#', source[start]);
					start++;
					while (source[start] == '*' || Character.isWhitespace(source[start])) {
						start++; // purge non-stored characters
					}
					// Verify member name position
					Name name = methodRef.getName();
					int nameLength = name.getLength();
					text = new String(source, start, nameLength);
					if (!text.equals(name.toString())) { // may have qualified constructor reference for inner classes
						if (methodRef.getQualifier().isQualifiedName()) {
							text = new String(source, start, methodRef.getQualifier().getLength());
							assumeEquals(this.prefix+"Misplaced method ref name at <"+start+">: ", text, methodRef.getQualifier().toString());
							while (source[start] != '.' || Character.isWhitespace(source[start])) {
								start++; // purge non-stored characters
							}
							start++;
						} else {
							while (source[start] != '.' || Character.isWhitespace(source[start])) {
								start++; // purge non-stored characters
							}
							start++;
							text = new String(source, start, nameLength);
							assumeEquals(this.prefix+"Misplaced method ref name at <"+start+">: ", text, name.toString());
						}
					}
					verifyNamePositions(start, name, source);
					start += nameLength;
					// Verify arguments starting open parenthesis
					while (source[start] == '*' || Character.isWhitespace(source[start])) {
						start++; // purge non-stored characters
					}
//					assumeEquals(prefix+"Misplaced ( at <"+start+"> for method ref: "+methodRef, '(', source[start]);
					if (source[start] == '(') { // now method reference may have no parenthesis...
						start++;
						// Verify parameters
						Iterator parameters = methodRef.parameters().listIterator();
						while (parameters.hasNext()) {
							MethodRefParameter param = (MethodRefParameter) parameters.next();
							boolean lastParam = !parameters.hasNext();
							// Verify parameter type positions
							while (source[start] == '*' || Character.isWhitespace(source[start])) {
								 start++; // purge non-stored characters
							}
							Type type = param.getType();
							if (type.isSimpleType()) {
								verifyNamePositions(start, ((SimpleType)type).getName(), source);
							} else if (type.isPrimitiveType()) {
								text = new String(source, start, type.getLength());
								assumeEquals(this.prefix+"Misplaced method ref parameter type at <"+start+"> for method ref: "+methodRef, text, type.toString());
							} else if (type.isArrayType()) {
								Type elementType = ((ArrayType) param.getType()).getElementType();
								if (elementType.isSimpleType()) {
									verifyNamePositions(start, ((SimpleType)elementType).getName(), source);
								} else if (elementType.isPrimitiveType()) {
									text = new String(source, start, elementType.getLength());
									assumeEquals(this.prefix+"Misplaced method ref parameter type at <"+start+"> for method ref: "+methodRef, text, elementType.toString());
								}
							}
							start += type.getLength();
							// if last param then perhaps a varargs
							while (Character.isWhitespace(source[start])) { // do NOT accept '*' in parameter declaration
								 start++; // purge non-stored characters
							}
							if (lastParam && this.astLevel != AST.JLS2 && param.isVarargs()) {
								for (int p=0;p<3;p++) {
									assumeTrue(this.prefix+"Missing ellipsis for vararg method ref parameter at <"+start+"> for method ref: "+methodRef, source[start++]=='.');
								}
							}
							// Verify parameter name positions
							while (Character.isWhitespace(source[start])) { // do NOT accept '*' in parameter declaration
								 start++; // purge non-stored characters
							}
							name = param.getName();
							if (name != null) {
								text = new String(source, start, name.getLength());
								assumeEquals(this.prefix+"Misplaced method ref parameter name at <"+start+"> for method ref: "+methodRef, text, name.toString());
								start += name.getLength();
							}
							// Verify end parameter declaration
							while (source[start] == '*' || Character.isWhitespace(source[start])) {
								start++;
							}
							assumeTrue(this.prefix+"Misplaced parameter end at <"+start+"> for method ref: "+methodRef, source[start] == ',' || source[start] == ')');
							start++;
							if (source[start] == ')') {
								break;
							}
						}
					}
				}
			}
			tagStart += fragment.getLength();
			previousFragment = fragment;
		}
		if (tagElement.isNested()) {
			assumeEquals(this.prefix+"Wrong end character at <"+tagStart+"> for "+tagElement, '}', source[tagStart++]);
		}
	}

	/*
	 * Verify each name component positions.
	 */
	private void verifyNamePositions(int nameStart, Name name, char[] source) {
		if (name.isQualifiedName()) {
			QualifiedName qualified = (QualifiedName) name;
			int start = qualified.getName().getStartPosition();
			String str = new String(source, start, qualified.getName().getLength());
			assumeEquals(this.prefix+"Misplaced or wrong name for qualified name: "+name, str, qualified.getName().toString());
			verifyNamePositions(nameStart, ((QualifiedName) name).getQualifier(), source);
		}
		String str = new String(source, nameStart, name.getLength());
		if (str.indexOf('\n') < 0) { // cannot compare if text contains new line
			assumeEquals(this.prefix+"Misplaced name for qualified name: ", str, name.toString());
		} else if (this.debug) {
			System.out.println(this.prefix+"Name contains new line for qualified name: "+name);
		}
	}

	/*
	 * Verify that bindings of Javadoc comment structure are resolved or not.
	 * For expected unresolved binding, verify that following text starts with 'Unknown'
	 */
	private void verifyBindings(Javadoc docComment) {
		boolean stop = this.stopOnFailure;
//		stopOnFailure = false;
		// Verify tags
		Iterator tags = docComment.tags().listIterator();
		while (tags.hasNext()) {
			verifyBindings((TagElement) tags.next());
		}
		this.stopOnFailure = stop;
		assertTrue(!stop || this.failures.size()==0);
	}

	/*
	 * Verify that bindings of Javadoc tag structure are resolved or not.
	 * For expected unresolved binding, verify that following text starts with 'Unknown'
	 */
	private void verifyBindings(TagElement tagElement) {
		// Verify each fragment
		Iterator elements = tagElement.fragments().listIterator();
		IBinding previousBinding = null;
		ASTNode previousFragment = null;
		boolean resolvedBinding = false;
		while (elements.hasNext()) {
			ASTNode fragment = (ASTNode) elements.next();
			if (fragment.getNodeType() == ASTNode.TEXT_ELEMENT) {
				TextElement text = (TextElement) fragment;
				if (resolvedBinding) {
					if (previousBinding == null) {
						assumeTrue(this.prefix+"Reference '"+previousFragment+"' should be bound!", text.getText().trim().indexOf("Unknown")>=0);
					} else {
						assumeTrue(this.prefix+"Unknown reference '"+previousFragment+"' should NOT be bound!", text.getText().trim().indexOf("Unknown")<0);
					}
				}
				previousBinding = null;
				resolvedBinding = false;
			} else if (fragment.getNodeType() == ASTNode.TAG_ELEMENT) {
				verifyBindings((TagElement) fragment);
				previousBinding = null;
				resolvedBinding = false;
			} else {
				resolvedBinding = true;
				if (fragment.getNodeType() == ASTNode.SIMPLE_NAME) {
					previousBinding = ((Name)fragment).resolveBinding();
				} else if (fragment.getNodeType() == ASTNode.QUALIFIED_NAME) {
					QualifiedName name = (QualifiedName) fragment;
					previousBinding = name.resolveBinding();
					verifyNameBindings(name);
				} else if (fragment.getNodeType() == ASTNode.MEMBER_REF) {
					MemberRef memberRef = (MemberRef) fragment;
					previousBinding = memberRef.resolveBinding();
					if (previousBinding != null) {
						SimpleName name = memberRef.getName();
						assumeNotNull(this.prefix+""+name+" binding was not foundfound in "+fragment, name.resolveBinding());
						verifyNameBindings(memberRef.getQualifier());
					}
				} else if (fragment.getNodeType() == ASTNode.METHOD_REF) {
					MethodRef methodRef = (MethodRef) fragment;
					previousBinding = methodRef.resolveBinding();
					if (previousBinding != null) {
						SimpleName methodName = methodRef.getName();
						IBinding methNameBinding = methodName.resolveBinding();
						Name methodQualifier = methodRef.getQualifier();
						// TODO (frederic) Replace the two following lines by commented block when bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=62650 will be fixed
						assumeNotNull(this.prefix+""+methodName+" binding was not found in "+fragment, methNameBinding);
						verifyNameBindings(methodQualifier);
						/*
						if (methodQualifier == null) {
							if (methNameBinding == null) {
								char firstChar = methodName.getIdentifier().charAt(0);
								if (Character.isUpperCase(firstChar)) {
									// assume that selector starting with uppercase is for constructor => signal that binding is null
									System.out.println(prefix+"Binding for selector of  '"+methodRef+"' is null.");
								}
							} else {
								if (methNameBinding.getName().equals(methodName.getIdentifier())) { // binding is not null only for constructor
									assumeNotNull(prefix+""+methodName+" binding was not found!",methNameBinding);
								} else {
									assumeNull(prefix+""+methodName+" binding should be null!", methNameBinding);
								}
							}
						} else {
							SimpleName methodSimpleType = null;
							if (methodQualifier.isQualifiedName()) {
								methodSimpleType = ((QualifiedName)methodQualifier).getName();
							} else {
								methodSimpleType = (SimpleName) methodQualifier;
							}
							if (methodSimpleType.getIdentifier().equals(methodName.getIdentifier())) { // binding is not null only for constructor
								assumeNotNull(prefix+""+methodName+" binding was not found!",methNameBinding);
							} else {
								assumeNull(prefix+""+methodName+" binding should be null!", methNameBinding);
							}
							verifyNameBindings(methodRef.getQualifier());
						}
						*/
						Iterator parameters = methodRef.parameters().listIterator();
						while (parameters.hasNext()) {
							MethodRefParameter param = (MethodRefParameter) parameters.next();
							Type type = param.getType();
							assumeNotNull(this.prefix+""+type+" binding was not found in "+fragment, type.resolveBinding());
							if (type.isSimpleType()) {
								verifyNameBindings(((SimpleType)type).getName());
							} else if (type.isArrayType()) {
								Type elementType = ((ArrayType) param.getType()).getElementType();
								assumeNotNull(this.prefix+""+elementType+" binding was not found in "+fragment, elementType.resolveBinding());
								if (elementType.isSimpleType()) {
									verifyNameBindings(((SimpleType)elementType).getName());
								}
							}
							//	Do not verify parameter name as no binding is expected for them
						}
					}
				}
			}
			previousFragment = fragment;
		}
		assumeTrue(this.prefix+"Reference '"+(previousFragment==null?tagElement:previousFragment)+"' should be bound!", (!resolvedBinding || previousBinding != null));
	}

	/*
	 * Verify each name component binding.
	 */
	private void verifyNameBindings(Name name) {
		if (name != null) {
			IBinding binding = name.resolveBinding();
			if (name.toString().indexOf("Unknown") > 0) {
				assumeNull(this.prefix+name+" binding should be null!", binding);
			} else {
				assumeNotNull(this.prefix+name+" binding was not found!", binding);
			}
			SimpleName simpleName = null;
			int index = 0;
			while (name.isQualifiedName()) {
				simpleName = ((QualifiedName) name).getName();
				binding = simpleName.resolveBinding();
				if (simpleName.getIdentifier().equalsIgnoreCase("Unknown")) {
					assumeNull(this.prefix+simpleName+" binding should be null!", binding);
				} else {
					assumeNotNull(this.prefix+simpleName+" binding was not found!", binding);
				}
				if (index > 0 && this.packageBinding) {
					assumeEquals(this.prefix+"Wrong binding type!", IBinding.PACKAGE, binding.getKind());
				}
				index++;
				name = ((QualifiedName) name).getQualifier();
				binding = name.resolveBinding();
				if (name.toString().indexOf("Unknown") > 0) {
					assumeNull(this.prefix+name+" binding should be null!", binding);
				} else {
					assumeNotNull(this.prefix+name+" binding was not found!", binding);
				}
				if (this.packageBinding) {
					assumeEquals(this.prefix+"Wrong binding type!", IBinding.PACKAGE, binding.getKind());
				}
			}
		}
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
		this.currentProject.setOption(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, this.docCommentSupport);

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

		// Verify comments either in unicode or not
		else if (this.unix) {
			testedSource = getUnixSource(source);
		}

		// Get comments infos from test file
		setSourceComment(testedSource);

		// Create DOM AST nodes hierarchy
		List unitComments = null;
		String sourceLevel = null;
		String complianceLevel = null;
		if (this.currentProject != null) {
			if (this.astLevel == AST.JLS3) {
				complianceLevel = this.currentProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
				sourceLevel = this.currentProject.getOption(JavaCore.COMPILER_SOURCE, true);
				this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
				this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
			}
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
				if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
					assumeEquals(this.prefix+"Invalid tags number in javadoc:\n"+docComment+"\n", tags.size(), allTags(docComment));
					verifyPositions(docComment, testedSource);
					if (this.resolveBinding) {
						verifyBindings(docComment);
					}
				} else {
					assumeEquals("Javadoc should be flat!", 0, docComment.tags().size());
				}
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

	/*
	 * Verify each javadoc
	 * Not implented yet
	private void verifyJavadoc(Javadoc docComment) {
	}
	*/

	/**
	 * Check javadoc for MethodDeclaration
	 */
	public void test000() throws JavaModelException {
		verifyComments("test000");
	}

	/**
	 * Check javadoc for invalid syntax
	 */
	public void test001() throws JavaModelException {
		verifyComments("test001");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=50781"
	 */
	public void test002() throws JavaModelException {
		verifyComments("test002");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=50784"
	 */
	public void test003() throws JavaModelException {
		verifyComments("test003");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=50785"
	 */
	public void test004() throws JavaModelException {
		verifyComments("test004");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=50838"
	 */
	public void test005() throws JavaModelException {
		verifyComments("test005");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=50877"
	 */
	public void test006() throws JavaModelException {
		verifyComments("test006");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=50877"
	 */
	public void test007() throws JavaModelException {
		verifyComments("test007");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=50877"
	 */
	public void test008() throws JavaModelException {
		verifyComments("test008");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=50877"
	 */
	public void test009() throws JavaModelException {
		verifyComments("test009");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=50880"
	 */
	public void test010() throws JavaModelException {
		verifyComments("test010");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=47396"
	 */
	public void test011() throws JavaModelException {
		this.problems = new StringBuffer();
		this.sourceUnit = getCompilationUnit("Converter" , "src", "javadoc.test011", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(this.sourceUnit, true);
		assumeNotNull("No compilation unit", result);
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=50938"
	 */
	public void test012() throws JavaModelException {
		verifyComments("test012");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=51104"
	 */
	public void test013() throws JavaModelException {
		verifyComments("test013");
	}

	/**
	 * Verify that text on next line following empty tag element
	 * is well positionned.
	 */
	public void test014() throws JavaModelException {
		verifyComments("test014");
	}

	/**
	 * Verify that we do not report failure when types are written on several lines
	 * in Javadoc comments.
	 */
	public void test015() throws JavaModelException {
		verifyComments("test015");
	}

	/**
	 * Verify DefaultCommentMapper heuristic to get leading and trailing comments
	 */
	protected void verifyMapper(String folder, int count, int[] indexes) throws JavaModelException {
		ICompilationUnit[] units = getCompilationUnits("Converter" , "src", "javadoc."+folder); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (int i=0; i<units.length; i++) {
			this.sourceUnit = units[i];
			ASTNode result = runConversion(this.sourceUnit, false);
			final CompilationUnit compilUnit = (CompilationUnit) result;
			assumeEquals(this.prefix+"Wrong number of problems", 0, compilUnit.getProblems().length); //$NON-NLS-1$
			assumeEquals(this.prefix+"Wrong number of comments", count, compilUnit.getCommentList().size());
			// Verify first method existence
			ASTNode node = getASTNode((CompilationUnit) result, 0, 0);
			assumeNotNull("We should get a non-null ast node", node);
			assumeTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
			MethodDeclaration method = (MethodDeclaration) node;
			// Verify first method extended positions
			int commentStart = method.getStartPosition();
			if (indexes[0]>=0) {
				Comment comment = (Comment) compilUnit.getCommentList().get(indexes[0]);
				commentStart = comment.getStartPosition();
			}
			int startPosition = compilUnit.getExtendedStartPosition(method);
			assumeEquals("Method "+node+" does not start at the right position", commentStart, startPosition);
			int methodEnd = startPosition + compilUnit.getExtendedLength(method) - 1;
			int commentEnd = method.getStartPosition() + method.getLength() - 1;
			if (indexes[1]>=0) {
				Comment comment = (Comment) compilUnit.getCommentList().get(indexes[1]);
				commentEnd = comment.getStartPosition() + comment.getLength() - 1;
			}
			assumeEquals("Method "+node+" does not have the correct length", commentEnd, methodEnd);
			// Verify second method existence
			node = getASTNode((CompilationUnit) result, 0, 1);
			assumeNotNull("We should get a non-null ast node", node);
			assumeTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
			method = (MethodDeclaration) node;
			// Verify second method extended positions
			commentStart = method.getStartPosition();
			if (indexes[2]>=0) {
				Comment comment = (Comment) compilUnit.getCommentList().get(indexes[2]);
				commentStart = comment.getStartPosition();
			}
			startPosition = compilUnit.getExtendedStartPosition(method);
			assumeEquals("Method "+node+" does not start at the right position", commentStart, startPosition);
			methodEnd = startPosition + compilUnit.getExtendedLength(method) - 1;
			commentEnd = method.getStartPosition() + method.getLength() - 1;
			if (indexes[3]>=0) {
				Comment comment = (Comment) compilUnit.getCommentList().get(indexes[3]);
				commentEnd = comment.getStartPosition() + comment.getLength() - 1;
			}
			assumeEquals("Method "+node+" does not have the correct length", commentEnd, methodEnd);
		}
	}

	/**
	 * Verify DefaultCommentMapper heuristic to get leading and trailing comments
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=53445"
	 */
	public void test100() throws JavaModelException {
		verifyMapper("test100", 16, new int[] {2,7,8,15});
	}
	public void test101() throws JavaModelException {
		verifyMapper("test101", 8, new int[] {1,3,4,7});
	}
	public void test102() throws JavaModelException {
		verifyMapper("test102", 16, new int[] {4,9,10,13});
	}
	public void test103() throws JavaModelException {
		verifyMapper("test103", 8, new int[] {2,4,5,6});
	}
	public void test104() throws JavaModelException {
		verifyMapper("test104", 16, new int[] {2,7,8,15});
	}
	public void test105() throws JavaModelException {
		verifyMapper("test105", 16, new int[] {-1,11,-1,15});
	}
	public void test106() throws JavaModelException {
		verifyMapper("test106", 8, new int[] {-1,5,-1,7});
	}
	public void test107() throws JavaModelException {
		verifyMapper("test107", 16, new int[] {2,7,8,-1});
	}
	public void test108() throws JavaModelException {
		verifyMapper("test108", 8, new int[] {1,3,4,-1});
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=54776"
	 */
	public void testBug54776() throws JavaModelException {
		this.sourceUnit = getCompilationUnit("Converter" , "src", "javadoc.testBug54776", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(this.sourceUnit, false);
		final CompilationUnit compilUnit = (CompilationUnit) result;
		assumeEquals(this.prefix+"Wrong number of problems", 0, compilUnit.getProblems().length); //$NON-NLS-1$
		assumeEquals(this.prefix+"Wrong number of comments", 2, compilUnit.getCommentList().size());
		// get comments range
		Comment comment = (Comment) compilUnit.getCommentList().get(0);
		int commentStart = comment.getStartPosition();
		int extendedLength = ((Comment) compilUnit.getCommentList().get(1)).getStartPosition()-commentStart+comment.getLength();
		// get method invocation in field initializer
		ASTNode node = getASTNode((CompilationUnit) result, 0);
		assumeNotNull("We should get a non-null ast node", node);
		assumeTrue("Not a type declaration", node.getNodeType() == ASTNode.TYPE_DECLARATION); //$NON-NLS-1$
		TypeDeclaration typeDecl = (TypeDeclaration) node;
		FieldDeclaration[] fields = typeDecl.getFields();
		assumeEquals("We should have a field declaration", 1, fields.length);
		List fragments = fields[0].fragments();
		assumeEquals("We should have a variable fragment", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assumeTrue("We should get an expression", expression instanceof MethodInvocation);
		MethodInvocation methodInvocation = (MethodInvocation) expression;
		// verify  that methodinvocation extended range includes leading and trailing comment
		int methodStart = compilUnit.getExtendedStartPosition(methodInvocation);
		assumeEquals("Method invocation "+methodInvocation+" does not start at the right position", commentStart, methodStart);
		int methodLength = compilUnit.getExtendedLength(methodInvocation);
		assumeEquals("Method invocation "+methodInvocation+" does not have the correct length", extendedLength, methodLength);
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=55221"
	 */
	public void testBug55221a() throws JavaModelException {
		this.sourceUnit = getCompilationUnit("Converter" , "src", "javadoc.testBug55221.a", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(this.sourceUnit, false);
		final CompilationUnit compilUnit = (CompilationUnit) result;
		assumeEquals(this.prefix+"Wrong number of problems", 0, compilUnit.getProblems().length); //$NON-NLS-1$
		assumeEquals(this.prefix+"Wrong number of comments", 1, compilUnit.getCommentList().size());
		// Get comment range
		Comment comment = (Comment) compilUnit.getCommentList().get(0);
		int commentStart = comment.getStartPosition();
		// get first method
		ASTNode node = getASTNode(compilUnit, 0, 0);
		assumeNotNull("We should get a non-null ast node", node);
		assumeTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration method = (MethodDeclaration) node;
		// verify that first method does not include comment
		int methodStart = compilUnit.getExtendedStartPosition(method);
		assumeEquals("Method "+method+" does not start at the right position", method.getStartPosition(), methodStart);
		int methodLength = compilUnit.getExtendedLength(method);
		assumeEquals("Method declaration "+method+" does not end at the right position",method.getLength(), methodLength);
		// get method body
		node = method.getBody();
		assumeNotNull("We should get a non-null ast node", node);
		assumeTrue("Not a block", node.getNodeType() == ASTNode.BLOCK); //$NON-NLS-1$
		Block block = (Block) node;
		// verify that body does not include following comment
		int blockStart = compilUnit.getExtendedStartPosition(block);
		assumeEquals("Body block "+block+" does not start at the right position", block.getStartPosition(), blockStart);
		int blockLength = compilUnit.getExtendedLength(block);
		assumeEquals("Body block "+block+" does not have the correct length", block.getLength(), blockLength);
		// get second method
		node = getASTNode(compilUnit, 0, 1);
		assumeNotNull("We should get a non-null ast node", node);
		assumeTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		method = (MethodDeclaration) node;
		// verify that second method start includes comment
		assumeEquals("Method declaration "+method+" does not start at the right position", commentStart, method.getStartPosition());
	}
	public void testBug55221b() throws JavaModelException {
		this.sourceUnit = getCompilationUnit("Converter" , "src", "javadoc.testBug55221.b", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(this.sourceUnit, false);
		final CompilationUnit compilUnit = (CompilationUnit) result;
		assumeEquals(this.prefix+"Wrong number of problems", 0, compilUnit.getProblems().length); //$NON-NLS-1$
		assumeEquals(this.prefix+"Wrong number of comments", 1, compilUnit.getCommentList().size());
		// Get comment range
		Comment comment = (Comment) compilUnit.getCommentList().get(0);
		int commentStart = comment.getStartPosition();
		// get first method
		ASTNode node = getASTNode(compilUnit, 0, 0);
		assumeNotNull("We should get a non-null ast node", node);
		assumeTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration method = (MethodDeclaration) node;
		// verify that first method does not include comment
		int methodStart = compilUnit.getExtendedStartPosition(method);
		assumeEquals("Method "+method+" does not start at the right position", method.getStartPosition(), methodStart);
		int methodLength = compilUnit.getExtendedLength(method);
		assumeEquals("Method declaration "+method+" does not end at the right position",method.getLength(), methodLength);
		// get method body
		node = method.getBody();
		assumeNotNull("We should get a non-null ast node", node);
		assumeTrue("Not a block", node.getNodeType() == ASTNode.BLOCK); //$NON-NLS-1$
		Block block = (Block) node;
		// verify that body does not include following comment
		int blockStart = compilUnit.getExtendedStartPosition(block);
		assumeEquals("Body block "+block+" does not start at the right position", block.getStartPosition(), blockStart);
		int blockLength = compilUnit.getExtendedLength(block);
		assumeEquals("Body block "+block+" does not have the correct length", block.getLength(), blockLength);
		// get second method
		node = getASTNode(compilUnit, 0, 1);
		assumeNotNull("We should get a non-null ast node", node);
		assumeTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		method = (MethodDeclaration) node;
		// verify that second method start includes comment
		assumeEquals("Method declaration "+method+" does not start at the right position", commentStart, method.getStartPosition());
	}
	public void testBug55221c() throws JavaModelException {
		this.sourceUnit = getCompilationUnit("Converter" , "src", "javadoc.testBug55221.c", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(this.sourceUnit, false);
		final CompilationUnit compilUnit = (CompilationUnit) result;
		assumeEquals(this.prefix+"Wrong number of problems", 0, compilUnit.getProblems().length); //$NON-NLS-1$
		assumeEquals(this.prefix+"Wrong number of comments", 1, compilUnit.getCommentList().size());
		// Get comment range
		Comment comment = (Comment) compilUnit.getCommentList().get(0);
		int commentStart = comment.getStartPosition();
		int commentEnd = commentStart+comment.getLength()-1;
		// get first method
		ASTNode node = getASTNode(compilUnit, 0, 0);
		assumeNotNull("We should get a non-null ast node", node);
		assumeTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration method = (MethodDeclaration) node;
		// verify that first method includes comment
		int methodStart = compilUnit.getExtendedStartPosition(method);
		assumeEquals("Method "+method+" does not start at the right position", method.getStartPosition(), methodStart);
		int methodLength = compilUnit.getExtendedLength(method);
		assumeEquals("Method "+method+" does not end at the right position", commentEnd, methodStart+methodLength-1);
		// get method body
		node = method.getBody();
		assumeNotNull("We should get a non-null ast node", node);
		assumeTrue("Not a block", node.getNodeType() == ASTNode.BLOCK); //$NON-NLS-1$
		Block block = (Block) node;
		// verify that body includes following comment
		int blockStart = compilUnit.getExtendedStartPosition(block);
		assumeEquals("Body block "+block+" does not start at the right position", block.getStartPosition(), blockStart);
		int blockLength = compilUnit.getExtendedLength(block);
		assumeEquals("Body block "+block+" does not end at the right position", commentEnd, blockStart+blockLength-1);
		// get second method
		node = getASTNode(compilUnit, 0, 1);
		assumeNotNull("We should get a non-null ast node", node);
		assumeTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		method = (MethodDeclaration) node;
		// verify that second method does not include comment
		methodStart = compilUnit.getExtendedStartPosition(method);
		assumeEquals("Method "+method+" does not start at the right position", method.getStartPosition(), methodStart);
		methodLength = compilUnit.getExtendedLength(method);
		assumeEquals("Method declaration "+method+" does not end at the right position",method.getLength(), methodLength);
	}
	/** @deprecated using deprecated code */
	public void testBug55221d() throws JavaModelException {
		this.sourceUnit = getCompilationUnit("Converter" , "src", "javadoc.testBug55221.d", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(this.sourceUnit, false);
		final CompilationUnit compilUnit = (CompilationUnit) result;
		assumeEquals(this.prefix+"Wrong number of problems", 0, compilUnit.getProblems().length); //$NON-NLS-1$
		assumeEquals(this.prefix+"Wrong number of comments", 2, compilUnit.getCommentList().size());
		// get first method
		ASTNode node = getASTNode(compilUnit, 0, 0);
		assumeNotNull("We should get a non-null ast node", node);
		assumeTrue("Not a method declaration", node.getNodeType() == ASTNode.METHOD_DECLARATION); //$NON-NLS-1$
		MethodDeclaration method = (MethodDeclaration) node;
		// verify that first method includes comment
		int methodStart = compilUnit.getExtendedStartPosition(method);
		assumeEquals("Method "+method+" does not start at the right position", method.getStartPosition(), methodStart);
		int methodLength = compilUnit.getExtendedLength(method);
		assumeEquals("Method "+method+" does not have the right length", methodLength, method.getLength());
		// get return type
		node = method.getReturnType();
		assumeNotNull("We should get a non-null ast node", node);
		assumeTrue("Not return type", node.getNodeType() == ASTNode.PRIMITIVE_TYPE); //$NON-NLS-1$
		PrimitiveType returnType = (PrimitiveType) node;
		// verify that return type includes following comment
		int returnStart = compilUnit.getExtendedStartPosition(returnType);
		assumeEquals("Return type "+returnType+" does not start at the right position", returnType.getStartPosition(), returnStart);
		int returnLength = compilUnit.getExtendedLength(returnType);
		assumeEquals("Return type "+returnType+" does not have the right length", returnType.getLength(), returnLength);
	}
	public void testBug55223a() throws JavaModelException {
//		stopOnFailure = false;
		this.sourceUnit = getCompilationUnit("Converter" , "src", "javadoc.testBug55223", "TestA.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(this.sourceUnit, false);
		final CompilationUnit compilUnit = (CompilationUnit) result;
		assumeEquals(this.prefix+"Wrong number of problems", 0, compilUnit.getProblems().length); //$NON-NLS-1$
		assumeEquals(this.prefix+"Wrong number of comments", 2, compilUnit.getCommentList().size());
		// get method
		ASTNode node = getASTNode(compilUnit, 0, 0);
		assumeNotNull("We should get a non-null ast node", node);
		assumeEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType()); //$NON-NLS-1$
		MethodDeclaration method = (MethodDeclaration) node;
		// get method body
		node = method.getBody();
		assumeNotNull("We should get a non-null ast node", node);
		assumeEquals("Not a block", ASTNode.BLOCK, node.getNodeType()); //$NON-NLS-1$
		Block block = (Block) node;
		// verify block statements start/end positions
		Iterator statements = block.statements().iterator();
		int idx = 0;
		while (statements.hasNext()) {
			node = (ExpressionStatement) statements.next();
			assumeEquals("Not a block", ASTNode.EXPRESSION_STATEMENT, node.getNodeType()); //$NON-NLS-1$
			ExpressionStatement statement = (ExpressionStatement) node;
			int statementStart = statement.getStartPosition();
			int statementEnd = statementStart + statement.getLength() - 1;
			if (idx < 2) {
				// Get comment range
				Comment comment = (Comment) compilUnit.getCommentList().get(idx);
				int commentStart = comment.getStartPosition();
				statementEnd = commentStart+comment.getLength()-1;
			}
			int extendedStart = compilUnit.getExtendedStartPosition(statement);
			assumeEquals("Statement "+statement+" does not start at the right position", statementStart, extendedStart);
			int extendedEnd = extendedStart + compilUnit.getExtendedLength(statement) - 1;
			assumeEquals("Statement "+statement+" does not end at the right position", statementEnd, extendedEnd);
			idx++;
		}
	}
	/** @deprecated using deprecated code */
	public void testBug55223b() throws JavaModelException {
		this.sourceUnit = getCompilationUnit("Converter" , "src", "javadoc.testBug55223", "TestB.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(this.sourceUnit, false);
		final CompilationUnit compilUnit = (CompilationUnit) result;
		assumeEquals(this.prefix+"Wrong number of problems", 0, compilUnit.getProblems().length); //$NON-NLS-1$
		assumeEquals(this.prefix+"Wrong number of comments", 2, compilUnit.getCommentList().size());
		// Get comment range
		Comment comment = (Comment) compilUnit.getCommentList().get(1);
		int commentStart = comment.getStartPosition();
		// get method
		ASTNode node = getASTNode(compilUnit, 0, 0);
		assumeNotNull("We should get a non-null ast node", node);
		assumeEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType()); //$NON-NLS-1$
		MethodDeclaration method = (MethodDeclaration) node;
		// get return type
		node = method.getReturnType();
		assumeNotNull("We should get a non-null ast node", node);
		assumeTrue("Not return type", node.getNodeType() == ASTNode.SIMPLE_TYPE); //$NON-NLS-1$
		SimpleType returnType = (SimpleType) node;
		// verify that return type includes following comment
		int returnStart = compilUnit.getExtendedStartPosition(returnType);
		assumeEquals("Return type "+returnType+" does not start at the right position", commentStart, returnStart);
		int returnEnd = returnStart + compilUnit.getExtendedLength(returnType) - 1;
		assumeEquals("Return type "+returnType+" does not end at the right length", returnType.getStartPosition()+returnType.getLength()-1, returnEnd);
	}
	/*
	 * End DefaultCommentMapper verifications
	 */

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=48489"
	 */
	public void testBug48489() throws JavaModelException {
		verifyComments("testBug48489");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=50898"
	 */
	public void testBug50898() throws JavaModelException {
		ICompilationUnit unit = getCompilationUnit("Converter" , "src", "javadoc.testBug50898", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		this.packageBinding = false;
		verifyComments(unit);
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=51226"
	 */
	public void testBug51226() throws JavaModelException {
		ICompilationUnit[] units = getCompilationUnits("Converter" , "src", "javadoc.testBug51226"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		for (int i=0; i<units.length; i++) {
			ASTNode result = runConversion(units[i], false);
			final CompilationUnit unit = (CompilationUnit) result;
			assumeEquals(this.prefix+"Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
			assumeEquals(this.prefix+"Wrong number of comments", 1, unit.getCommentList().size());
			Comment comment = (Comment) unit.getCommentList().get(0);
			assumeTrue(this.prefix+"Comment should be a Javadoc one", comment.isDocComment());
			if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
				Javadoc docComment = (Javadoc) comment;
				assumeEquals(this.prefix+"Wrong number of tags", 1, docComment.tags().size());
				TagElement tagElement = (TagElement) docComment.tags().get(0);
				assumeNull(this.prefix+"Wrong type of tag ["+tagElement+"]", tagElement.getTagName());
				assumeEquals(this.prefix+"Wrong number of fragments in tag ["+tagElement+"]", 1, tagElement.fragments().size());
				ASTNode fragment = (ASTNode) tagElement.fragments().get(0);
				assumeEquals(this.prefix+"Invalid type for fragment ["+fragment+"]", ASTNode.TEXT_ELEMENT, fragment.getNodeType());
				TextElement textElement = (TextElement) fragment;
				assumeEquals(this.prefix+"Invalid content for text element ", "Test", textElement.getText());
				if (this.debug) System.out.println(docComment+"\nsuccessfully verified.");
			}
		}
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=51241"
	 */
	public void testBug51241() throws JavaModelException {
		verifyComments("testBug51241");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=51363"
	 */
	public void testBug51363() throws JavaModelException {
		this.sourceUnit = getCompilationUnit("Converter" , "src", "javadoc.testBug51363", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		ASTNode result = runConversion(this.sourceUnit, false);
		final CompilationUnit unit = (CompilationUnit) result;
		assumeEquals(this.prefix+"Wrong number of problems", 0, unit.getProblems().length); //$NON-NLS-1$
		assumeEquals(this.prefix+"Wrong number of comments", 2, unit.getCommentList().size());
		// verify first comment
		Comment comment = (Comment) unit.getCommentList().get(0);
		assumeTrue(this.prefix+"Comment should be a line comment ", comment.isLineComment());
		String sourceStr = this.sourceUnit.getSource();
		int startPos = comment.getStartPosition()+comment.getLength();
		assumeEquals("Wrong length for line comment "+comment, "\\u000D\\u000A", sourceStr.substring(startPos, startPos+12));
		if (this.debug) System.out.println(comment+"\nsuccessfully verified.");
		// verify second comment
		comment = (Comment) unit.getCommentList().get(1);
		assumeTrue(this.prefix+"Comment should be a line comment", comment.isLineComment());
		sourceStr = this.sourceUnit.getSource();
		startPos = comment.getStartPosition()+comment.getLength();
		assumeEquals("Wrong length for line comment "+comment, "\\u000Dvoid", sourceStr.substring(startPos, startPos+10));
		if (this.debug) System.out.println(comment+"\nsuccessfully verified.");
//		verifyComments("testBug51363");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=51476"
	 */
	public void testBug51476() throws JavaModelException {
		verifyComments("testBug51476");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=51478"
	 */
	public void testBug51478() throws JavaModelException {
		verifyComments("testBug51478");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=51508"
	 */
	public void testBug51508() throws JavaModelException {
		verifyComments("testBug51508");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=51650"
	 */
	public void testBug51650() throws JavaModelException {
		verifyComments("testBug51650");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=51770"
	 */
	public void testBug51770() throws JavaModelException {
		verifyComments("testBug51770");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=52908"
	 */
	public void testBug52908() throws JavaModelException {
		verifyComments("testBug52908");
	}
	public void testBug52908a() throws JavaModelException {
		verifyComments("testBug52908a");
	}
	public void testBug52908unicode() throws JavaModelException {
		verifyComments("testBug52908unicode");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=53276"
	 */
	public void testBug53276() throws JavaModelException {
		verifyComments("testBug53276");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=53075"
	 */
	public void testBug53075() throws JavaModelException {
		ICompilationUnit unit = getCompilationUnit("Converter" , "src", "javadoc.testBug53075", "X.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		boolean pb = this.packageBinding;
		this.packageBinding = false;
		CompilationUnit compilUnit = verifyComments(unit);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			Comment comment = (Comment) compilUnit.getCommentList().get(0);
			assumeTrue(this.prefix+"Comment should be a javadoc comment ", comment.isDocComment());
			Javadoc docComment = (Javadoc) comment;
			TagElement tagElement = (TagElement) docComment.tags().get(0);
			assumeEquals("Wrong tag type!", TagElement.TAG_LINK, tagElement.getTagName());
			tagElement = (TagElement) docComment.tags().get(1);
			assumeEquals("Wrong tag type!", TagElement.TAG_LINKPLAIN, tagElement.getTagName());
		}
		this.packageBinding = pb;
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=53757"
	 */
	public void testBug53757() throws JavaModelException {
		verifyComments("testBug53757");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=51600"
	 */
	public void testBug51600() throws JavaModelException {
		verifyComments("testBug51600");
	}
	public void testBug51617() throws JavaModelException {
		this.stopOnFailure = false;
		String [] unbound = { "e" };
		verifyComments("testBug51617");
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			int size = unbound.length;
			for (int i=0, f=0; i<size; i++) {
				assertTrue("Invalid number of failures!", this.failures.size()>f);
				String failure = (String) this.failures.get(f);
				String expected = "Reference '"+unbound[i]+"' should be bound!";
				if (expected.equals(failure.substring(failure.indexOf(' ')+1))) {
					this.failures.remove(f);
				} else {
					f++;	// skip offending failure
					i--;	// stay on expected string
				}
			}
		}
		this.stopOnFailure = true;
	}
	public void testBug54424() throws JavaModelException {
		this.stopOnFailure = false;
		String [] unbound = { "tho",
				"A#getList(int,long,boolean)",
				"#getList(Object,java.util.AbstractList)",
		};
		verifyComments("testBug54424");
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			int size = unbound.length;
			for (int i=0, f=0; i<size; i++) {
				assertTrue("Invalid number of failures!", this.failures.size()>f);
				String failure = (String) this.failures.get(f);
				String expected = "Reference '"+unbound[i]+"' should be bound!";
				if (expected.equals(failure.substring(failure.indexOf(' ')+1))) {
					this.failures.remove(f);
				} else {
					f++;	// skip offending failure
					i--;	// stay on expected string
				}
			}
		}
		this.stopOnFailure = true;
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=63044"
	 */
	public void testBug63044() throws JavaModelException {
		verifyComments("testBug63044");
	}

	/**
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=51660"
	 */
	public void testBug51660() throws JavaModelException {
		this.stopOnFailure = false;
		ICompilationUnit unit = getCompilationUnit("Converter" , "src", "javadoc.testBug51660", "Test.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		CompilationUnit compilUnit = verifyComments(unit);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			String[] tagNames = {
				"@ejb",
				"@ejb",
				"@ejb",
				"@ejb",
				"@ejb",
				"@ejb",
				"@ejb(bean",
				"@ejb)bean",
				"@ejb",
				"@ejb+bean",
				"@ejb,bean",
				"@ejb-bean",
				"@ejb.bean",
				"@ejb/bean",
				"@ejb",
				"@ejb;bean",
				"@ejb",
				"@ejb=bean",
				"@ejb",
				"@ejb?bean",
				"@ejb@bean",
				"@ejb[bean",
				"@ejb\\bean",
				"@ejb]bean",
				"@ejb^bean",
				"@ejb`bean",
				"@ejb{bean",
				"@ejb|bean",
				"@ejb",
				"@ejb~bean",
				"@unknown"
			};
			String[] tagTexts = {
				"!bean test non-java id character '!' (val=33) in tag name",
				"\"bean test non-java id character '\"' (val=34) in tag name",
				"#bean test non-java id character '#' (val=35) in tag name",
				"%bean test non-java id character '%' (val=37) in tag name",
				"&bean test non-java id character '&' (val=38) in tag name",
				"'bean test non-java id character ''' (val=39) in tag name",
				" test non-java id character '(' (val=40) in tag name",
				" test non-java id character ')' (val=41) in tag name",
				"*bean test non-java id character '*' (val=42) in tag name",
				" test non-java id character '+' (val=43) in tag name",
				" test non-java id character ',' (val=44) in tag name",
				" test non-java id character '-' (val=45) in tag name",
				" test non-java id character '.' (val=46) in tag name",
				" test non-java id character '/' (val=47) in tag name",
				":bean test non-java id character ':' (val=58) in tag name",
				" test non-java id character ';' (val=59) in tag name",
				"<bean test non-java id character '<' (val=60) in tag name",
				" test non-java id character '=' (val=61) in tag name",
				">bean test non-java id character '>' (val=62) in tag name",
				" test non-java id character '?' (val=63) in tag name",
				" test non-java id character '@' (val=64) in tag name",
				" test non-java id character '[' (val=91) in tag name",
				" test non-java id character '\\' (val=92) in tag name",
				" test non-java id character ']' (val=93) in tag name",
				" test non-java id character '^' (val=94) in tag name",
				" test non-java id character '`' (val=96) in tag name",
				" test non-java id character '{' (val=123) in tag name",
				" test non-java id character '|' (val=124) in tag name",
				"}bean test non-java id character '}' (val=125) in tag name",
				" test non-java id character '~' (val=126) in tag name",
				" test java id"
			};
			Comment comment = (Comment) compilUnit.getCommentList().get(0);
			assumeTrue(this.prefix+"Comment should be a javadoc comment ", comment.isDocComment());
			Javadoc docComment = (Javadoc) comment;
			int size = docComment.tags().size();
			for (int i=0; i<size; i++) {
				TagElement tagElement = (TagElement) docComment.tags().get(i);
				assumeEquals("Wrong tag name for:"+tagElement, tagNames[i], tagElement.getTagName());
				assumeEquals("Wrong fragments size for :"+tagElement, 1, tagElement.fragments().size());
				ASTNode fragment = (ASTNode) tagElement.fragments().get(0);
				assumeEquals("Wrong fragments type for :"+tagElement, ASTNode.TEXT_ELEMENT, fragment.getNodeType());
				TextElement textElement = (TextElement) fragment;
				assumeEquals("Wrong text for tag!", tagTexts[i], textElement.getText());
			}
		}
		this.stopOnFailure = true;
	}

	/**
	 * Bug 65174: Spurious "Javadoc: Missing reference" error
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=65174"
	 */
	public void testBug65174() throws JavaModelException {
		verifyComments("testBug65174");
	}

	/**
	 * Bug 65253: [Javadoc] @@tag is wrongly parsed as @tag
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=65253"
	 */
	public void testBug65253() throws JavaModelException {
		verifyComments("testBug65253");
	}

	/**
	 * Bug 65288: Javadoc: tag gets mangled when javadoc closing on same line without whitespace
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=65288"
	 */
	public void testBug65288() throws JavaModelException {
		verifyComments("testBug65288");
	}

	/**
	 * Bug 68017: Javadoc processing does not detect missing argument to @return
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=68017"
	 */
	public void testBug68017() throws JavaModelException {
		verifyComments("testBug68017");
	}

	/**
	 * Bug 68025: Javadoc processing does not detect some wrong links
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=68025"
	 */
	public void testBug68025() throws JavaModelException {
		verifyComments("testBug68025");
	}

	/**
	 * Bug 69272: [Javadoc] Invalid malformed reference (missing separator)
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=69272"
	 */
	public void testBug69272() throws JavaModelException {
		verifyComments("testBug69272");
	}

	/**
	 * Bug 69275: [Javadoc] Invalid warning on @see link
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=69275"
	 */
	public void testBug69275() throws JavaModelException {
		verifyComments("testBug69275");
	}

	/**
	 * Bug 69302: [Javadoc] Invalid reference warning inconsistent with javadoc tool
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=69302"
	 */
	public void testBug69302() throws JavaModelException {
		verifyComments("testBug69302");
	}

	/**
	 * Bug 68726: [Javadoc] Target attribute in @see link triggers warning
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=68726"
	 */
	public void testBug68726() throws JavaModelException {
		verifyComments("testBug68726");
	}

	/**
	 * Bug 70892: [1.5][Javadoc] Compiler should parse reference for inline tag @value
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=70892"
	 * @deprecated using deprecated code
	 */
	public void testBug70892_JLS2() throws JavaModelException {
		int level = this.astLevel;
		this.astLevel = AST.JLS2;
		verifyComments("testBug70892");
		this.astLevel = level;
	}
	public void testBug70892_JLS3() throws JavaModelException {
		int level = this.astLevel;
		this.astLevel = AST.JLS3;
		verifyComments("testBug70892");
		this.astLevel = level;
	}

	/**
	 * Bug 51911: [Javadoc] @see method w/out ()
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=51911"
	 */
	public void testBug51911() throws JavaModelException {
		verifyComments("testBug51911");
	}

	/**
	 * Bug 73348: [Javadoc] Missing description for return tag is not always warned
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=73348"
	 */
	public void testBug73348() throws JavaModelException {
		verifyComments("testBug73348");
	}

	/**
	 * Bug 77644: [dom] AST node extended positions may be wrong while moving
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=77644"
	 */
	public void testBug77644() throws JavaModelException {
		verifyComments("testBug77644");
	}

	/**
	 * Bug 79809: [1.5][dom][javadoc] Need better support for type parameter Javadoc tags
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=79809"
	 */
	public void testBug79809() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter/src/javadoc/b79809/Test.java",
			"package javadoc.b79809;\n" +
			"/**\n" +
			" * @param <E>  Class type parameter\n" +
			" * @see Object\n" +
			" */\n" +
			"public class Test<E> {\n" +
			"	/**\n" +
			"	 * @param t\n" +
			"	 * @param <T> Method type parameter\n" +
			"	 */\n" +
			"	<T> void foo(T t) {}\n" +
			"}\n");
		verifyWorkingCopiesComments();
	}
	public void testBug79809b() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter/src/javadoc/b79809/Test.java",
			"package javadoc.b79809;\n" +
			"\n" +
			"/**\n" +
			" * New tags for 5.0\n" +
			" *  - literal: {@literal a<B>c}\n" +
			" *  - code: {@code abc}\n" +
			" *  - value: {@value System#out}\n" +
			" */\n" +
			"public class Test {\n" +
			"\n" +
			"}\n");
		verifyWorkingCopiesComments();
	}

	/**
	 * Bug 79904: [1.5][dom][javadoc] TagElement range not complete for type parameter tags
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=79904"
	 */
	public void testBug79904() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter/src/javadoc/b79904/Test.java",
			"package javadoc.b79904;\n" +
			"/**\n" +
			" * @param <E>\n" +
			" * @see Object\n" +
			" */\n" +
			"public class Test<E> {\n" +
			"	/**\n" +
			"	 * @param t\n" +
			"	 * @param <T>\n" +
			"	 */\n" +
			"	<T> void foo(T t) {}\n" +
			"}\n");
		verifyWorkingCopiesComments();
	}

	/**
	 * Bug 80221: [1.5][dom][javadoc] Need better support for type parameter Javadoc tags
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=80221"
	 */
	public void testBug80221() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter/src/javadoc/b80221/Test.java",
			"package javadoc.b80221;\n" +
			"public class Test {\n" +
			"	/**\n" +
			"	 * @see Object Unknown: ref is not resolved due to compile error...\n" +
			"	 */\n" +
			"	public foo() {\n" +
			"		return 1;\n" +
			"	}\n" +
			"}\n"
		);
		verifyWorkingCopiesComments();
	}

	/**
	 * Bug 80257: [1.5][javadoc][dom] Type references in javadocs should have generic binding, not raw
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=80257"
	 */
	public void testBug80257() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b80257/Test.java",
			"package javadoc.b80257;\n" +
			"import java.util.*;\n" +
			"public class Test {\n" +
			"	/**\n" +
			"	 * @see ArrayList\n" +
			"	 * @return {@link java.util.List}\n" +
			"	 */\n" +
			"	List<String> getList() {\n" +
			"		return new ArrayList<String>();\n" +
			"	}\n" +
			"}\n"
			);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Do not need to verify following statement as we know it's ok as verifyComments did not fail
			Javadoc docComment = (Javadoc) compilUnit.getCommentList().get(0); // get javadoc comment
			TagElement firstTag = (TagElement) docComment.tags().get(0); // get first tag
			TagElement secondTag = (TagElement) docComment.tags().get(1); // get second tag
			TagElement inlineTag = (TagElement) secondTag.fragments().get(1); // get inline tag
			// Get tag simple name reference in first tag
			assertEquals("Invalid number of fragments for tag element: "+firstTag, 1, firstTag.fragments().size());
			ASTNode node = (ASTNode) firstTag.fragments().get(0);
			assertEquals("Invalid kind of name reference for tag element: "+firstTag, ASTNode.SIMPLE_NAME, node.getNodeType());
			SimpleName seeRef = (SimpleName) node;
			// Verify binding for simple name
			IBinding binding = seeRef.resolveBinding();
			assertTrue("Wrong kind of binding", binding instanceof ITypeBinding);
			ITypeBinding typeBinding = (ITypeBinding)binding;
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=209936
			// only have RawTypeBinding in Javadocs
			assertFalse(seeRef.toString()+" should NOT have a generic type binding", typeBinding.isGenericType());
			assertFalse(seeRef.toString()+" should NOT have a parameterized type binding", typeBinding.isParameterizedType());
			assertTrue(seeRef.toString()+" should have a raw type binding", typeBinding.isRawType());
			// Get inline tag simple name reference in second tag
			assertEquals("Invalid number of fragments for inline tag element: "+inlineTag, 1, inlineTag.fragments().size());
			node = (ASTNode) inlineTag.fragments().get(0);
			assertEquals("Invalid kind of name reference for tag element: "+inlineTag, ASTNode.QUALIFIED_NAME, node.getNodeType());
			QualifiedName linkRef = (QualifiedName) node;
			// Verify binding for qualified name
			binding = linkRef.resolveBinding();
			assertTrue("Wrong kind of binding", binding instanceof ITypeBinding);
			typeBinding = (ITypeBinding)binding;
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=209936
			// only have RawTypeBinding in Javadocs
			assertFalse(linkRef.toString()+" should NOT have a generic type binding", typeBinding.isGenericType());
			assertFalse(linkRef.toString()+" should NOT have a parameterized type binding", typeBinding.isParameterizedType());
			assertTrue(linkRef.toString()+" should have a raw type binding", typeBinding.isRawType());
		}
	}

	/**
	 * Bug 83804: [1.5][javadoc] Missing Javadoc node for package declaration
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=83804"
	 */
	public void testBug83804() throws CoreException, JavaModelException {
		this.astLevel = AST.JLS3;
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getCompilationUnit("Converter15", "src", "javadoc.b83804", "package-info.java");
		this.workingCopies[1] = getCompilationUnit("Converter15", "src", "javadoc.b83804", "Test.java");
		verifyWorkingCopiesComments();
	}
	public void testBug83804a() throws CoreException, JavaModelException {
		this.astLevel = AST.JLS3;
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getCompilationUnit("Converter15", "src", "javadoc.b83804a", "package-info.java");
		this.workingCopies[1] = getCompilationUnit("Converter15", "src", "javadoc.b83804a", "Test.java");
		verifyWorkingCopiesComments();
	}

	/**
	 * Bug 84049: [javadoc][dom] Extended ranges wrong for method name without return type
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=84049"
	 */
	public void testBug84049() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b84049/Test.java",
			"package javadoc.b84049;\n" +
			"public class Test {\n" +
			"	/**\n" +
			"	 * @see Object\n" +
			"	 */\n" +
			"	foo() {\n" +
			"	}\n" +
			"}\n"
			);
		CompilationUnit compilUnit = (CompilationUnit) runConversion(this.workingCopies[0], true);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			ASTNode node = getASTNode(compilUnit, 0, 0);
			assertEquals("Invalid type for node: "+node, ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			Javadoc methodJavadoc = methodDeclaration.getJavadoc();
			assertNotNull("MethodDeclaration have a javadoc comment", methodJavadoc);
			int javadocStart = methodJavadoc.getStartPosition();
			assertEquals("Method declaration should include javadoc comment", methodDeclaration.getStartPosition(), javadocStart);
			SimpleName methodName = methodDeclaration.getName();
			int nameStart = methodName.getStartPosition();
			assertTrue("Method simple name should not include javadoc comment", nameStart > javadocStart+methodJavadoc.getLength());
			int extendedStart = compilUnit.getExtendedStartPosition(methodName);
			assertEquals("Method simple name start position should not be extended!", nameStart, extendedStart);
			int extendedLength = compilUnit.getExtendedLength(methodName);
			assertEquals("Method simple name length should not be extended!", methodName.getLength(), extendedLength);
		}
	}

	/**
	 * Bug 87845: [1.5][javadoc][dom] Type references in javadocs should have generic binding, not raw
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=87845"
	 */
	public void testBug87845() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b87845/Test.java",
			"package javadoc.b87845;\n" +
			"public class Test {\n" +
			"	public void foo(int a, int b) {} \n" +
			"	public void foo(int a, int... args) {}\n" +
			"	public void foo(String... args) {}\n" +
			"	public void foo(Exception str, boolean... args) {}\n" +
			"	/**\n" +
			"	* @see Test#foo(int, int)\n" +
			"	* @see Test#foo(int, int[])\n" +
			"	* @see Test#foo(int, int...)\n" +
			"	* @see Test#foo(String[])\n" +
			"	* @see Test#foo(String...)\n" +
			"	* @see Test#foo(Exception, boolean[])\n" +
			"	* @see Test#foo(Exception, boolean...)\n" +
			"	*/\n" +
			"	public void valid() {}\n" +
			"	/**\n" +
			"	* @see Test#foo(int)\n" +
			"	* @see Test#foo(int, int, int)\n" +
			"	* @see Test#foo()\n" +
			"	* @see Test#foo(String)\n" +
			"	* @see Test#foo(String, String)\n" +
			"	* @see Test#foo(Exception)\n" +
			"	* @see Test#foo(Exception, boolean)\n" +
			"	* @see Test#foo(Exception, boolean, boolean)\n" +
			"	*/\n" +
			"	public void invalid() {}\n" +
			"}\n"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Do not need to verify following statement as we know it's ok as verifyComments did not fail
			Javadoc docComment = (Javadoc) compilUnit.getCommentList().get(0); // get first javadoc comment
			// Verify last parameter for all methods reference in javadoc comment
			List tags = docComment.tags();
			int size = tags.size();
			for (int i=0; i<size; i++) {
				TagElement tag = (TagElement) docComment.tags().get(i);
				assertEquals("Invalid number of fragment for see reference: "+tag, 1, tag.fragments().size());
				ASTNode node = (ASTNode) tag.fragments().get(0);
				assertEquals("Invalid kind of name reference for tag element: "+tag, ASTNode.METHOD_REF, node.getNodeType());
				MethodRef methodRef = (MethodRef) node;
				List parameters = methodRef.parameters();
				int paramSize = parameters.size();
				for (int j=0; j<paramSize; j++) {
					node = (ASTNode) parameters.get(j);
					assertEquals("Invalid kind of method parameter: "+node, ASTNode.METHOD_REF_PARAMETER, node.getNodeType());
					MethodRefParameter parameter = (MethodRefParameter) node;
					if (j==(paramSize-1)) {
						switch (i) {
							case 2:
							case 4:
							case 6:
								assertTrue("Method parameter \""+parameter+"\" should be varargs!", parameter.isVarargs());
								break;
							default:
								assertFalse("Method parameter \""+parameter+"\" should not be varargs!", parameter.isVarargs());
								break;
						}
					} else {
						assertFalse("Method parameter \""+parameter+"\" should not be varargs!", parameter.isVarargs());
					}
				}
			}
		}
	}

	/**
	 * Bug 93880: [1.5][javadoc] Source range of PackageDeclaration does not include Javadoc child
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=93880"
	 */
	public void testBug93880_15a() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b93880/Test.java",
			"/**\n" +
			" * Javadoc\n" +
			" */\n" +
			"package javadoc.b93880;\n" +
			"public class Test {\n" +
			"}\n"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Get package declaration declaration and javadoc
			PackageDeclaration packDecl = compilUnit.getPackage();
			Javadoc docComment = (Javadoc) compilUnit.getCommentList().get(0); // Do not need to verify following statement as we know it's ok as verifyComments did not fail

			// Verify package declaration javadoc
			assertTrue("Javadoc should be set on package declaration", docComment == packDecl.getJavadoc());

			// Verify package declaration declaration source start
			assertEquals("Source range of PackageDeclaration should include Javadoc child", docComment.getStartPosition(), packDecl.getStartPosition());
		}
	}
	public void testBug93880_15b() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b93880/package-info.java",
			"/**\n" +
			" * Javadoc for all package\n" +
			" */\n" +
			"package javadoc.b93880;"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Get package declaration declaration and javadoc
			PackageDeclaration packDecl = compilUnit.getPackage();
			Javadoc docComment = (Javadoc) compilUnit.getCommentList().get(0); // Do not need to verify following statement as we know it's ok as verifyComments did not fail

			// Verify package declaration javadoc
			assertTrue("Javadoc should be set on package declaration", docComment == packDecl.getJavadoc());

			// Verify package declaration declaration source start
			assertEquals("Source range of PackageDeclaration should include Javadoc child", docComment.getStartPosition(), packDecl.getStartPosition());
		}
	}
	public void testBug93880_15c() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b93880/package-info.java",
			"/**\n" +
			" * Javadoc for all package\n" +
			" */\n" +
			"private package javadoc.b93880;"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Get package declaration declaration and javadoc
			PackageDeclaration packDecl = compilUnit.getPackage();
			Javadoc docComment = (Javadoc) compilUnit.getCommentList().get(0); // Do not need to verify following statement as we know it's ok as verifyComments did not fail

			// Verify package declaration javadoc
			assertTrue("Javadoc should be set on package declaration", docComment == packDecl.getJavadoc());

			// Verify package declaration declaration source start
			assertEquals("Source range of PackageDeclaration should include Javadoc child", docComment.getStartPosition(), packDecl.getStartPosition());
		}
	}
	public void testBug93880_15d() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b93880/package-info.java",
			"/**\n" +
			" * Javadoc for all package\n" +
			" */\n" +
			"@Deprecated\n" +
			"package javadoc.b93880;"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Get package declaration declaration and javadoc
			PackageDeclaration packDecl = compilUnit.getPackage();
			assertNotNull("Compilation unit should have a package declaration", packDecl);
			Javadoc docComment = (Javadoc) compilUnit.getCommentList().get(0); // Do not need to verify following statement as we know it's ok as verifyComments did not fail

			// Verify package declaration javadoc
			assertTrue("Javadoc should be set on package declaration", docComment == packDecl.getJavadoc());

			// Verify package declaration declaration source start
			assertEquals("Source range of PackageDeclaration should include Javadoc child", docComment.getStartPosition(), packDecl.getStartPosition());
		}
	}
	public void testBug93880_15e() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b93880/package-info.java",
			"/* (non-javadoc)\n" +
			" * No comment\n" +
			" */\n" +
			"package javadoc.b93880;"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Get package declaration declaration and javadoc
			PackageDeclaration packDecl = compilUnit.getPackage();
			List unitComments = compilUnit.getCommentList();
			assertEquals("Wrong number of comments", 1, unitComments.size());
			Comment comment = (Comment) unitComments.get(0);

			// Verify package declaration javadoc
			assertNull("Package declaration should not have any javadoc", packDecl.getJavadoc());

			// Verify package declaration declaration source start
			assertTrue("Source range of PackageDeclaration should NOT include Javadoc child", packDecl.getStartPosition() > comment.getStartPosition()+comment.getLength());
		}
	}
	public void testBug93880_14a() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b93880/Test.java",
			"/**\n" +
			" * Javadoc\n" +
			" */\n" +
			"package javadoc.b93880;\n" +
			"public class Test {\n" +
			"}\n"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Get package declaration declaration and javadoc
			PackageDeclaration packDecl = compilUnit.getPackage();
			Javadoc docComment = (Javadoc) compilUnit.getCommentList().get(0); // Do not need to verify following statement as we know it's ok as verifyComments did not fail

			// Verify package declaration declaration source start
			assertEquals("Source range of PackageDeclaration should include Javadoc child", docComment.getStartPosition(), packDecl.getStartPosition());
		}
	}
	public void testBug93880_14b() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b93880/package-info.java",
			"/**\n" +
			" * Javadoc for all package\n" +
			" */\n" +
			"package javadoc.b93880;"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Get package declaration declaration and javadoc
			PackageDeclaration packDecl = compilUnit.getPackage();
			Javadoc docComment = (Javadoc) compilUnit.getCommentList().get(0); // Do not need to verify following statement as we know it's ok as verifyComments did not fail

			// Verify package declaration declaration source start
			assertEquals("Source range of PackageDeclaration should include Javadoc child", docComment.getStartPosition(), packDecl.getStartPosition());
		}
	}
	public void testBug93880_14c() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b93880/package-info.java",
			"/**\n" +
			" * Javadoc for all package\n" +
			" */\n" +
			"private package javadoc.b93880;"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Get package declaration declaration and javadoc
			PackageDeclaration packDecl = compilUnit.getPackage();
			Javadoc docComment = (Javadoc) compilUnit.getCommentList().get(0); // Do not need to verify following statement as we know it's ok as verifyComments did not fail

			// Verify package declaration declaration source start
			assertEquals("Source range of PackageDeclaration should include Javadoc child", docComment.getStartPosition(), packDecl.getStartPosition());
		}
	}
	public void testBug93880_14d() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b93880/package-info.java",
			"/**\n" +
			" * Javadoc for all package\n" +
			" */\n" +
			"@Deprecated\n" +
			"package javadoc.b93880;"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Get package declaration declaration and javadoc
			PackageDeclaration packDecl = compilUnit.getPackage();
			assertNotNull("Compilation unit should have a package declaration", packDecl);
			Javadoc docComment = (Javadoc) compilUnit.getCommentList().get(0); // Do not need to verify following statement as we know it's ok as verifyComments did not fail

			// Verify package declaration declaration source start
			assertEquals("Source range of PackageDeclaration should include Javadoc child", docComment.getStartPosition(), packDecl.getStartPosition());
		}
	}
	public void testBug93880_14e() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b93880/package-info.java",
			"/* (non-javadoc)\n" +
			" * No comment\n" +
			" */\n" +
			"package javadoc.b93880;"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Get package declaration declaration and javadoc
			PackageDeclaration packDecl = compilUnit.getPackage();
			List unitComments = compilUnit.getCommentList();
			assertEquals("Wrong number of comments", 1, unitComments.size());
			Comment comment = (Comment) unitComments.get(0);

			// Verify package declaration declaration source start
			assertTrue("Source range of PackageDeclaration should NOT not include Javadoc child", packDecl.getStartPosition() > comment.getStartPosition()+comment.getLength());
		}
	}

	/**
	 * Bug 94150: [javadoc][dom] Extended ranges wrong for method name without return type
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=94150"
	 */
	public void testBug94150() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b94150/Category.java",
			"package javadoc.b94150;\n" +
			"public enum Category {\n" +
			"    /**\n" +
			"     * history style\n" +
			"     * @see Object\n" +
			"     */ \n" +
			"     HISTORY,\n" +
			"\n" +
			"    /**\n" +
			"     * war style\n" +
			"     */ \n" +
			"     WAR;\n" +
			"}\n"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Get enum declaration
			ASTNode node = getASTNode(compilUnit, 0);
			assertEquals("Expected enum declaration.", ASTNode.ENUM_DECLARATION, node.getNodeType());
			EnumDeclaration enumDeclaration = (EnumDeclaration) node;

			// Verify each enum constant javadoc
			List constants = enumDeclaration.enumConstants();
			int size = constants.size();
			assertEquals("Wrong number of constants", 2, size);
			for (int i=0; i<size; i++) {
				EnumConstantDeclaration constant  = (EnumConstantDeclaration) constants.get(i);
				Javadoc docComment = (Javadoc) compilUnit.getCommentList().get(i); // Do not need to verify following statement as we know it's ok as verifyComments did not fail
				assertTrue("Javadoc should be set on first enum constant", docComment == constant.getJavadoc());
			}
		}
	}

	/**
	 * Bug 99507: [javadoc] Infinit loop in DocCommentParser
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=99507"
	 */
	public void testBug99507() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b99507/X.java",
			"package javadoc.b99507;\n" +
			"public class X {\n" +
			"}\n" +
			"/** @param test*/"
		);
		verifyComments(this.workingCopies[0]);
	}
	public void testBug99507b() throws JavaModelException {
        String source = "/**\n@param country*/";
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(source.toCharArray());
		parser.createAST(null);
	}

	/**
	 * Bug 100041: [javadoc] Infinit loop in DocCommentParser
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=100041"
	 */
	public void testBug100041() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b100041/X.java",
			"package javadoc.b100041;\n" +
			"class X {\n" +
			"	static Object object;\n" +
			"	static void foo() {\n" +
			"		/**\n" +
			"		 * javadoc comment.\n" +
			"		 */\n" +
			"		if (object instanceof String) {\n" +
			"			final String clr = null;\n" +
			"		}\n" +
			"	}\n" +
			"}"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Get comment
			List unitComments = compilUnit.getCommentList();
			assertEquals("Wrong number of comments", 1, unitComments.size());
			Comment comment = (Comment) unitComments.get(0);
			int commentStart = comment.getStartPosition();
			int commentEnd = commentStart+comment.getLength();

			// Get local variable declaration
			ASTNode node = getASTNode(compilUnit, 0, 1, 0);
			assertEquals("Expected if statement for node: "+node, ASTNode.IF_STATEMENT, node.getNodeType());
			IfStatement ifStatement = (IfStatement) node;
			assertTrue("Invalid start position for IfStatement: "+ifStatement, ifStatement.getStartPosition() > commentEnd);
			Statement statement  = ifStatement.getThenStatement();
			assertEquals("Expected block for node: "+statement, ASTNode.BLOCK, statement.getNodeType());
			Block block = (Block) statement;
			assertTrue("Invalid start position for Block: "+block, block.getStartPosition() > commentEnd);
			List statements = block.statements();
			assertEquals("Invalid number of statements for block: "+block, 1, statements.size());
			statement = (Statement) statements.get(0);
			assertEquals("Expected variable declaration statement for node: "+statement, ASTNode.VARIABLE_DECLARATION_STATEMENT, statement.getNodeType());
			VariableDeclarationStatement varDecl = (VariableDeclarationStatement) statement;
			assertTrue("Invalid start position for : VariableDeclarationStatement"+varDecl, varDecl.getStartPosition() > commentEnd);
		}
	}
	public void testBug100041b() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b100041/X.java",
			"package javadoc.b100041;\n" +
			"class X {\n" +
			"	static Object object;\n" +
			"	static void foo() {\n" +
			"		/**\n" +
			"		 * javadoc comment.\n" +
			"		 */\n" +
			"		if (object instanceof String)\n" +
			"			return;\n" +
			"	}\n" +
			"}"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Get comment
			List unitComments = compilUnit.getCommentList();
			assertEquals("Wrong number of comments", 1, unitComments.size());
			Comment comment = (Comment) unitComments.get(0);
			int commentStart = comment.getStartPosition();
			int commentEnd = commentStart+comment.getLength();

			// Get local variable declaration
			ASTNode node = getASTNode(compilUnit, 0, 1, 0);
			assertEquals("Expected if statement for node: "+node, ASTNode.IF_STATEMENT, node.getNodeType());
			IfStatement ifStatement = (IfStatement) node;
			assertTrue("Invalid start position for IfStatement: "+ifStatement, ifStatement.getStartPosition() > commentEnd);
			Statement statement  = ifStatement.getThenStatement();
			assertEquals("Expected block for node: "+statement, ASTNode.RETURN_STATEMENT, statement.getNodeType());
			ReturnStatement returnStatement = (ReturnStatement) statement;
			assertTrue("Invalid start position for Block: "+returnStatement, returnStatement.getStartPosition() > commentEnd);
		}
	}
	public void testBug100041c() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b100041/Z.java",
			"package javadoc.b100041;\n" +
			"public class Z {\n" +
			"	/** C1 */\n" +
			"	class Z1 {}\n" +
			"	/** C2 */\n" +
			"	Z1 z1;\n" +
			"	/** C3 */\n" +
			"	public static void foo(Object object) {\n" +
			"		/** C4 */\n" +
			"		class ZZ {\n" +
			"			/** C5 */\n" +
			"			ZZ zz;\n" +
			"			/** C6 */\n" +
			"			public void bar() {}\n" +
			"		}\n" +
			"	}\n" +
			"}\n"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Get comments
			List unitComments = compilUnit.getCommentList();
			int size = unitComments.size();
			assertEquals("Wrong number of comments", 6, size);
			Javadoc[] javadocs = new Javadoc[size];
			Iterator iterator = unitComments.iterator();
			for (int i=0; i<size; i++) {
				Comment comment = (Comment) iterator.next();
				assertEquals("Expect javadoc for comment: "+comment, ASTNode.JAVADOC, comment.getNodeType());
				javadocs[i] = (Javadoc) comment;
			}

			// Verify member type declaration start
			ASTNode node = getASTNode(compilUnit, 0, 0);
			assertEquals("Expected type declaration for node: "+node, ASTNode.TYPE_DECLARATION, node.getNodeType());
			TypeDeclaration typeDeclaration = (TypeDeclaration) node;
			int javadocStart = javadocs[0].getStartPosition();
			assertEquals("Invalid start position for TypeDeclaration: "+typeDeclaration, typeDeclaration.getStartPosition(), javadocStart);

			// Verify field declaration start
			node = getASTNode(compilUnit, 0, 1);
			assertEquals("Expected field declaration for node: "+node, ASTNode.FIELD_DECLARATION, node.getNodeType());
			FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
			javadocStart = javadocs[1].getStartPosition();
			assertEquals("Invalid start position for FieldDeclaration: "+fieldDeclaration, fieldDeclaration.getStartPosition(), javadocStart);

			// Verify method declaration start
			node = getASTNode(compilUnit, 0, 2);
			assertEquals("Expected method declaration for node: "+node, ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			javadocStart = javadocs[2].getStartPosition();
			assertEquals("Invalid start position for MethodDeclaration: "+methodDeclaration, methodDeclaration.getStartPosition(), javadocStart);

			// Verify local type declaration start
			node = getASTNode(compilUnit, 0, 2, 0);
			assertEquals("Expected type declaration for node: "+node, ASTNode.TYPE_DECLARATION_STATEMENT, node.getNodeType());
			typeDeclaration = (TypeDeclaration) ((TypeDeclarationStatement) node).getDeclaration();
			javadocStart = javadocs[3].getStartPosition();
			assertEquals("Invalid start position for TypeDeclaration: "+typeDeclaration, typeDeclaration.getStartPosition(), javadocStart);

			// Verify field declaration start
			List bodyDeclarations = typeDeclaration.bodyDeclarations();
			node = (ASTNode) bodyDeclarations.get(0);
			assertEquals("Expected field declaration for node: "+node, ASTNode.FIELD_DECLARATION, node.getNodeType());
			fieldDeclaration = (FieldDeclaration) node;
			javadocStart = javadocs[4].getStartPosition();
			assertEquals("Invalid start position for FieldDeclaration: "+fieldDeclaration, fieldDeclaration.getStartPosition(), javadocStart);

			// Verify method declaration start
			node = (ASTNode) bodyDeclarations.get(1);
			assertEquals("Expected method declaration for node: "+node, ASTNode.METHOD_DECLARATION, node.getNodeType());
			methodDeclaration = (MethodDeclaration) node;
			javadocStart = javadocs[5].getStartPosition();
			assertEquals("Invalid start position for MethodDeclaration: "+methodDeclaration, methodDeclaration.getStartPosition(), javadocStart);
		}
	}

	/**
	 * @bug 103304: [Javadoc] Wrong reference proposal for inner classes.
	 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=103304"
	 */
	public void testBug103304() throws JavaModelException {
		this.packageBinding = false; // do NOT verify that qualification only can be package name
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b103304/Test.java",
			"package javadoc.b103304;\n" +
			"interface IAFAState {\n" +
			"    public class ValidationException extends Exception {\n" +
			"        public ValidationException(String variableName, IAFAState subformula) {\n" +
			"            super(\"Variable \'\"+variableName+\"\' may be unbound in \'\"+subformula+\"\'\");\n" +
			"        }\n" +
			"    }\n" +
			"}\n" +
			"public class Test {\n" +
			"	/**\n" +
			"	 * @see IAFAState.ValidationException#IAFAState.ValidationException(String, IAFAState)\n" +
			"	 */\n" +
			"	IAFAState.ValidationException valid;\n" +
			"}\n"
		);
		CompilationUnit compilUnit = (CompilationUnit) runConversion(this.workingCopies[0], true);
		verifyWorkingCopiesComments();
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Verify comment type
			Iterator unitComments = compilUnit.getCommentList().iterator();
			while (unitComments.hasNext()) {
				Comment comment = (Comment) unitComments.next();
				assertEquals("Comment should be javadoc", comment.getNodeType(), ASTNode.JAVADOC);
				Javadoc javadoc = (Javadoc) comment;

				// Verify that there's always a method reference in tags
				List tags = javadoc.tags();
				int size = tags.size();
				for (int i=0; i<size; i++) {
					TagElement tag = (TagElement) javadoc.tags().get(i);
					assertEquals("Invalid number of fragment for see reference: "+tag, 1, tag.fragments().size());
					ASTNode node = (ASTNode) tag.fragments().get(0);
					assertEquals("Invalid kind of name reference for tag element: "+tag, ASTNode.METHOD_REF, node.getNodeType());
				}
			}
		}
	}

	/**
	 * Bug 106581: [javadoc] null type binding for parameter in javadoc
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=106581"
	 */
	public void testBug106581() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b106581/A.java",
			"package javadoc.b106581;\n" +
			"public class A {\n" +
			"    /**\n" +
			"     * @param x\n" +
			"     */ \n" +
			"     public void foo(int x) {},\n" +
			"}\n"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Get comment
			List unitComments = compilUnit.getCommentList();
			assertEquals("Wrong number of comments", 1, unitComments.size());
			Comment comment = (Comment) unitComments.get(0);
			assertEquals("Comment should be javadoc", comment.getNodeType(), ASTNode.JAVADOC);

			// Get local variable declaration
			Javadoc docComment = (Javadoc) comment;
			TagElement tag = (TagElement) docComment.tags().get(0);
			assertEquals("Invalid number of fragment for tag: "+tag, 1, tag.fragments().size());
			ASTNode node = (ASTNode) tag.fragments().get(0);
			assertEquals("Invalid kind of name reference for tag element: "+tag, ASTNode.SIMPLE_NAME, node.getNodeType());
			SimpleName simpleName = (SimpleName) node;
			assertNotNull("We should have a type binding for simple name: "+simpleName, simpleName.resolveTypeBinding());
		}
	}

	/**
	 * Bug 108622: [javadoc][dom] ASTNode not including javadoc
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=108622"
	 */
	public void testBug108622() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b108622/Test.java",
			"package javadoc.b108622;\n" +
			"/**\n" +
			" * \n" +
			" */\n" +
			"public abstract class Test {\n" +
			"\n" +
			"	/**\n" +
			"	 * \n" +
			"	 */\n" +
			"	public abstract Zork getFoo();\n" +
			"\n" +
			"	/**\n" +
			"	 * \n" +
			"	 */\n" +
			"	public abstract void setFoo(Zork dept);\n" +
			"\n" +
			"}"
			);
		CompilationUnit compilUnit = (CompilationUnit) runConversion(this.workingCopies[0], true);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Verify first method
			ASTNode node = getASTNode(compilUnit, 0, 0);
			assertEquals("Invalid type for node: "+node, ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			assertEquals("Invalid method name", "getFoo", methodDeclaration.getName().toString());
			Javadoc methodJavadoc = methodDeclaration.getJavadoc();
			assertNotNull("MethodDeclaration have a javadoc comment", methodJavadoc);
			int javadocStart = methodJavadoc.getStartPosition();
			assertEquals("Method declaration should include javadoc comment", methodDeclaration.getStartPosition(), javadocStart);
			// Verify second method
			node = getASTNode(compilUnit, 0, 1);
			assertEquals("Invalid type for node: "+node, ASTNode.METHOD_DECLARATION, node.getNodeType());
			methodDeclaration = (MethodDeclaration) node;
			assertEquals("Invalid method name", "setFoo", methodDeclaration.getName().toString());
			methodJavadoc = methodDeclaration.getJavadoc();
			assertNotNull("MethodDeclaration have a javadoc comment", methodJavadoc);
			javadocStart = methodJavadoc.getStartPosition();
			assertEquals("Method declaration should include javadoc comment", methodDeclaration.getStartPosition(), javadocStart);
		}
	}

	/**
	 * Bug 113108: [API][comments] CompilationUnit.getNodeComments(ASTNode)
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=113108"
	 */
	public void testBug113108a() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b113108/Test.java",
			"package javadoc.b113108;\n" +
			"/** C0 */\n" +
			"public class Test {\n" +
			"	/* C1 */\n" +
			"	/** C2 */\n" +
			"	// C3\n" +
			"	public void foo() {\n" +
			"		/* C4 */\n" +
			"	}\n" +
			"	/* C5 */\n" +
			"	/** C6 */\n" +
			"	// C7\n" +
			"}"
			);
		CompilationUnit compilUnit = (CompilationUnit) runConversion(this.workingCopies[0], true);
		verifyWorkingCopiesComments();
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Verify  method javadoc
			ASTNode node = getASTNode(compilUnit, 0, 0);
			assertEquals("Invalid type for node: "+node, ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			assertEquals("Invalid method name", "foo", methodDeclaration.getName().toString());
			Javadoc methodJavadoc = methodDeclaration.getJavadoc();
			assertNotNull("MethodDeclaration have a javadoc comment", methodJavadoc);
			int javadocStart = methodJavadoc.getStartPosition();
			assertEquals("Method declaration should include javadoc comment", methodDeclaration.getStartPosition(), javadocStart);
			// Verify method first leading and last trailing comment
			int index = compilUnit.firstLeadingCommentIndex(methodDeclaration);
			assertEquals("Invalid first leading comment for "+methodDeclaration, 1, index);
			index = compilUnit.lastTrailingCommentIndex(methodDeclaration);
			assertEquals("Invalid last trailing comment for "+methodDeclaration, 7, index);
		}
	}
	public void testBug113108b() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b113108/Test.java",
			"package javadoc.b113108;\n" +
			"/** C0 */\n" +
			"public class Test {\n" +
			"	/** C1 */\n" +
			"	// C2\n" +
			"	/* C3 */\n" +
			"	public void foo() {\n" +
			"		// C4\n" +
			"	}\n" +
			"	/** C5 */\n" +
			"	/// C6\n" +
			"	/* C7 */\n" +
			"}"
			);
		CompilationUnit compilUnit = (CompilationUnit) runConversion(this.workingCopies[0], true);
		verifyWorkingCopiesComments();
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Verify  method javadoc
			ASTNode node = getASTNode(compilUnit, 0, 0);
			assertEquals("Invalid type for node: "+node, ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			assertEquals("Invalid method name", "foo", methodDeclaration.getName().toString());
			Javadoc methodJavadoc = methodDeclaration.getJavadoc();
			assertNotNull("MethodDeclaration have a javadoc comment", methodJavadoc);
			int javadocStart = methodJavadoc.getStartPosition();
			assertEquals("Method declaration should include javadoc comment", methodDeclaration.getStartPosition(), javadocStart);
			// Verify method first leading and last trailing comment
			int index = compilUnit.firstLeadingCommentIndex(methodDeclaration);
			assertEquals("Invalid first leading comment for "+methodDeclaration, 1, index);
			index = compilUnit.lastTrailingCommentIndex(methodDeclaration);
			assertEquals("Invalid last trailing comment for "+methodDeclaration, 7, index);
		}
	}
	public void testBug113108c() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b113108/Test.java",
			"package javadoc.b113108;\n" +
			"/** C0 */\n" +
			"public class Test {\n" +
			"	// C1\n" +
			"	/* C2 */\n" +
			"	/** C3 */\n" +
			"	public void foo() {\n" +
			"		/** C4 */\n" +
			"	}\n" +
			"	// C5\n" +
			"	/* C6 */\n" +
			"	/** C7 */\n" +
			"}"
			);
		CompilationUnit compilUnit = (CompilationUnit) runConversion(this.workingCopies[0], true);
		verifyWorkingCopiesComments();
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Verify  method javadoc
			ASTNode node = getASTNode(compilUnit, 0, 0);
			assertEquals("Invalid type for node: "+node, ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			assertEquals("Invalid method name", "foo", methodDeclaration.getName().toString());
			Javadoc methodJavadoc = methodDeclaration.getJavadoc();
			assertNotNull("MethodDeclaration have a javadoc comment", methodJavadoc);
			int javadocStart = methodJavadoc.getStartPosition();
			assertEquals("Method declaration should include javadoc comment", methodDeclaration.getStartPosition(), javadocStart);
			// Verify method first leading and last trailing comment
			int index = compilUnit.firstLeadingCommentIndex(methodDeclaration);
			assertEquals("Invalid first leading comment for "+methodDeclaration, 1, index);
			index = compilUnit.lastTrailingCommentIndex(methodDeclaration);
			assertEquals("Invalid last trailing comment for "+methodDeclaration, 7, index);
		}
	}

	/**
	 * @bug 125676: [javadoc] @category should not read beyond end of line
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=125676"
	 */
	public void testBug125676() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[3];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b125676/A.java",
			"package javadoc.b125676;\n" +
			"public class A {\n" +
			"        /**\n" +
			"         * @category \n" +
			"         * When searching for field matches, it will exclusively find read accesses, as\n" +
			"         * opposed to write accesses. Note that some expressions are considered both\n" +
			"         * as field read/write accesses: for example, x++; x+= 1;\n" +
			"         * \n" +
			"         * @since 2.0\n" +
			"         */\n" +
			"        int READ_ACCESSES = 4;\n" +
			"}\n"
		);
		this.workingCopies[1] = getWorkingCopy("/Converter15/src/javadoc/b125676/B.java",
			"package javadoc.b125676;\n" +
			"public class B {\n" +
			"        /**\n" +
			"         * @category test\n" +
			"         */\n" +
			"        int field1;\n" +
			"        /**\n" +
			"         * @category     test\n" +
			"         */\n" +
			"        int field2;\n" +
			"        /**\n" +
			"         * @category test    \n" +
			"         */\n" +
			"        int field3;\n" +
			"        /**\n" +
			"         * @category    test    \n" +
			"         */\n" +
			"        int field4;\n" +
			"        /** @category test */\n" +
			"        int field5;\n" +
			"\n" +
			"}\n"
		);
		this.workingCopies[2] = getWorkingCopy("/Converter15/src/javadoc/b125676/C.java",
			"package javadoc.b125676;\n" +
			"public class C { \n" +
			"        /**\n" +
			"         * @category test mutli ids\n" +
			"         */\n" +
			"        int field1;\n" +
			"        /**\n" +
			"         * @category    test    mutli    ids   \n" +
			"         */\n" +
			"        int field2;\n" +
			"        /** @category    test    mutli    ids*/\n" +
			"        int field3;\n" +
			"}\n"
		);
		verifyWorkingCopiesComments();
	}

	/**
	 * @bug 125903: [javadoc] Treat whitespace in javadoc tags as invalid tags
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=125903"
	 */
	public void testBug125903() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.astLevel = AST.JLS3;
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b125903/Test.java",
			"package javadoc.b125903;\n" +
			"/**\n" +
			" * {@ link java.lang.String}\n" +
			" * @ since 2.1\n" +
			" */\n" +
			"public class Test {\n" +
			"\n" +
			"}\n"
		);
		CompilationUnit compilUnit = (CompilationUnit) runConversion(this.workingCopies[0], true);
		verifyWorkingCopiesComments();
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Verify  method javadoc
			ASTNode node = getASTNode(compilUnit, 0);
			assertEquals("Invalid type for node: "+node, ASTNode.TYPE_DECLARATION, node.getNodeType());
			TypeDeclaration typeDeclaration = (TypeDeclaration) node;
			Javadoc javadoc = typeDeclaration.getJavadoc();
			assertNotNull("TypeDeclaration should have a javadoc comment", javadoc);
			List tags = javadoc.tags();
			TagElement tag = (TagElement) tags.get(0);
			tag = (TagElement) tag.fragments().get(0);
			assertEquals("Tag name should be empty", tag.getTagName(), "@");
			tag = (TagElement) tags.get(1);
			assertEquals("Tag name should be empty", tag.getTagName(), "@");
		}
	}

	/**
	 * @bug 130752: [comments] first BlockComment parsed as LineComment
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=130752"
	 */
	public void testBug130752() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b130752/Test.java",
			"/* Ceci n'est pas\n" +
			" * une ligne. */\n" +
			"package javadoc.b130752;\n" +
			"public class Test {\n" +
			"}\n"
		);
		CompilationUnit compilUnit = (CompilationUnit) runConversion(this.workingCopies[0], true);
		verifyWorkingCopiesComments();
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Verify comment type
			List unitComments = compilUnit.getCommentList();
			assertEquals("Wrong number of comments", 1, unitComments.size());
			Comment comment = (Comment) unitComments.get(0);
			assertEquals("Comment should be javadoc", comment.getNodeType(), ASTNode.BLOCK_COMMENT);
		}
	}
	public void testBug130752b() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b130752/Test.java",
			"// Line comment\n" +
			"package javadoc.b130752;\n" +
			"public class Test {\n" +
			"}\n"
		);
		CompilationUnit compilUnit = (CompilationUnit) runConversion(this.workingCopies[0], true);
		verifyWorkingCopiesComments();
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Verify comment type
			List unitComments = compilUnit.getCommentList();
			assertEquals("Wrong number of comments", 1, unitComments.size());
			Comment comment = (Comment) unitComments.get(0);
			assertEquals("Comment should be javadoc", comment.getNodeType(), ASTNode.LINE_COMMENT);
		}
	}
	public void testBug130752c() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b130752/Test.java",
			"/** Javadoc comment */\n" +
			"package javadoc.b130752;\n" +
			"public class Test {\n" +
			"}\n"
		);
		CompilationUnit compilUnit = (CompilationUnit) runConversion(this.workingCopies[0], true);
		verifyWorkingCopiesComments();
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Verify comment type
			List unitComments = compilUnit.getCommentList();
			assertEquals("Wrong number of comments", 1, unitComments.size());
			Comment comment = (Comment) unitComments.get(0);
			assertEquals("Comment should be javadoc", comment.getNodeType(), ASTNode.JAVADOC);
		}
	}

	/**
	 * @bug 165525: [comments] ASTParser excludes trailing line comments from extended range of fields in enums
	 * @test Ensure that extended ranges are correct for enum constants and last comments of enum declaration
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=165525"
	 */
	public void testBug165525() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter15/src/javadoc/b165525/Test.java",
			"package javadoc.b165525;\n" +
			"public enum Test {\n" +
			"	ENUM_CONST_1(\"String constant 1\") //$NON-NLS-1$\n" +
			"	, ENUM_CONST_2(\"String constant 2\") //$NON-NLS-1$\n" +
			"	;\n" +
			"	Test(String x) {\n" +
			"	}\n" +
			"	String a = \"a\"; //$NON-NLS-1$\n" +
			"	String b = \"b\"; //$NON-NLS-1$\n" +
			"}\n"
		);
		CompilationUnit compilUnit = (CompilationUnit) runConversion(AST.JLS3, this.workingCopies[0], true);
		verifyWorkingCopiesComments();
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Verify comment type
			List unitComments = compilUnit.getCommentList();
			assertEquals("Wrong number of comments", 4, unitComments.size());

			// Verify extension of first enum declaration constant
			Comment comment = (Comment) unitComments.get(0);
			EnumDeclaration enumDeclaration = (EnumDeclaration) compilUnit.types().get(0);
			EnumConstantDeclaration constantDeclaration = (EnumConstantDeclaration) enumDeclaration.enumConstants().get(0);
			int declarationEnd = constantDeclaration.getStartPosition() + compilUnit.getExtendedLength(constantDeclaration) - 1;
			int commentEnd = comment.getStartPosition() + comment.getLength() - 1;
			assumeEquals("Enum constant declaration "+constantDeclaration+" does not have the correct length", commentEnd, declarationEnd);

			// Verify extension of second enum declaration constant
			comment = (Comment) unitComments.get(1);
			constantDeclaration = (EnumConstantDeclaration) enumDeclaration.enumConstants().get(1);
			declarationEnd = constantDeclaration.getStartPosition() + compilUnit.getExtendedLength(constantDeclaration) - 1;
			commentEnd = comment.getStartPosition() + comment.getLength() - 1;
			assumeEquals("Enum constant declaration "+constantDeclaration+" does not have the correct length", commentEnd, declarationEnd);

			// Verify extension of first field declaration
			comment = (Comment) unitComments.get(2);
			FieldDeclaration fieldDeclaration = (FieldDeclaration) enumDeclaration.bodyDeclarations().get(1);
			declarationEnd = fieldDeclaration.getStartPosition() + compilUnit.getExtendedLength(fieldDeclaration) - 1;
			commentEnd = comment.getStartPosition() + comment.getLength() - 1;
			assumeEquals("Enum constant declaration "+constantDeclaration+" does not have the correct length", commentEnd, declarationEnd);

			// Verify extension of second field declaration
			comment = (Comment) unitComments.get(3);
			fieldDeclaration = (FieldDeclaration) enumDeclaration.bodyDeclarations().get(2);
			declarationEnd = fieldDeclaration.getStartPosition() + compilUnit.getExtendedLength(fieldDeclaration) - 1;
			commentEnd = comment.getStartPosition() + comment.getLength() - 1;
			assumeEquals("Enum constant declaration "+constantDeclaration+" does not have the correct length", commentEnd, declarationEnd);
		}
	}

	/**
	 * @bug 228648: AST: no binding for Javadoc reference to inner class
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=228648"
	 */
	public void testBug228648() throws JavaModelException {
		ICompilationUnit unit = getCompilationUnit("Converter" , "src", "javadoc.testBug228648", "A.java"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		verifyComments(unit);
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=196714
	public void test109() throws JavaModelException {
		verifyComments("test109");
	}
}
