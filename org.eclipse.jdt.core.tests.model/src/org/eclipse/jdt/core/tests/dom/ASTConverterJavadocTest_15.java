/*******************************************************************************
 * Copyright (c) 2020, 2021 IBM Corporation and others.
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
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.ModuleQualifiedName;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.IModuleBinding;
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
public class ASTConverterJavadocTest_15 extends ConverterTestSetup {

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



	public ASTConverterJavadocTest_15(String name, String support, String unix) {
		super(name);
		this.docCommentSupport = support;
		this.unix = "true".equals(unix);
	}
	public ASTConverterJavadocTest_15(String name) {
		this(name.substring(0, name.indexOf(" - ")),
				name.substring(name.indexOf(" - Doc ") + 7, name.lastIndexOf("abled") + 5),
				name.indexOf(" - Unix") != -1 ? "true" : "false");
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#getName()
	 */
	public String getName() {
		String strUnix = this.unix ? " - Unix" : "";
		return super.getName()+" - Doc "+this.docCommentSupport+strUnix;
	}

	public static Test suite() {
		TestSuite suite = new Suite(ASTConverterJavadocTest_15.class.getName());
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
		Class c = ASTConverterJavadocTest_15.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
				suite.addTest(new ASTConverterJavadocTest_15(methods[i].getName(), support, UNIX_SUPPORT));
			}
		}
		// when unix support not specified, also run using unix format
		if (UNIX_SUPPORT == null && JavaCore.ENABLED.equals(support)) {
			for (int i = 0, max = methods.length; i < max; i++) {
				if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
					suite.addTest(new ASTConverterJavadocTest_15(methods[i].getName(), support, "true"));
				}
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
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=206345
		if (!(TAG_CODE.equalsIgnoreCase(tagName) || !TAG_LITERAL.equalsIgnoreCase(tagName)) && tagElement.isNested()) {
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
				} else if (fragment.getNodeType() == ASTNode.MODULE_QUALIFIED_NAME) {
					ModuleQualifiedName name = (ModuleQualifiedName) fragment;
					Name typeName = name.getName();
					if (typeName != null) {
						if (typeName.getNodeType() == ASTNode.SIMPLE_NAME) {
							previousBinding = ((Name)fragment).resolveBinding();
						} else if (typeName.getNodeType() == ASTNode.QUALIFIED_NAME) {
							QualifiedName qname = (QualifiedName) typeName;
							previousBinding = qname.resolveBinding();
							verifyNameBindings(qname);
						}
					}
					Name mName = name.getModuleQualifier();
					if (mName.getNodeType() == ASTNode.SIMPLE_NAME) {
						previousBinding = ((Name)fragment).resolveBinding();
					} else if (mName.getNodeType() == ASTNode.QUALIFIED_NAME) {
						QualifiedName qname = (QualifiedName) mName;
						previousBinding = qname.resolveBinding();
					}
					assumeNotNull(this.prefix+""+name+" binding was not foundfound in "+fragment, previousBinding);
					assumeTrue(this.prefix+""+name+" binding is not module binding "+fragment, previousBinding instanceof IModuleBinding);
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
			complianceLevel = this.currentProject.getOption(JavaCore.COMPILER_COMPLIANCE, true);
			sourceLevel = this.currentProject.getOption(JavaCore.COMPILER_SOURCE, true);
			this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_15);
			this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_15);
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

	public void testJavadoc1() throws JavaModelException {
		this.moduleUnit = getWorkingCopy("/Converter_15_1/src/module-info.java",
				"module test1.one.two {\r\n" +
				"}");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter_15_1/src/javadoc/X.java",
			"package javadoc;\n" +
			"public class X {\n" +
			"	/** \n" +
			"	 * @see test1.one.two/ \n" +
			"	 * {@link test1.one.two/} \n" +
			"	 * {@linkplain test1.one.two/} \n" +
			"	 */ \n" +
			"	public static void foo(Object object) {\n" +
			"	}\n" +
			"}\n"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Get comments
			List unitComments = compilUnit.getCommentList();
			int size = unitComments.size();
			assertEquals("Wrong number of comments", 1, size);
			Javadoc[] javadocs = new Javadoc[size];
			Iterator iterator = unitComments.iterator();
			for (int i=0; i<size; i++) {
				Comment comment = (Comment) iterator.next();
				assertEquals("Expect javadoc for comment: "+comment, ASTNode.JAVADOC, comment.getNodeType());
				javadocs[i] = (Javadoc) comment;
			}

			// Verify member type declaration start
			ASTNode node = getASTNode(compilUnit, 0, 0);
			assertEquals("Expected method declaration for node: "+node, ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			int javadocStart = javadocs[0].getStartPosition();
			assertEquals("Invalid start position for MethodDeclaration: "+methodDeclaration, methodDeclaration.getStartPosition(), javadocStart);
		}
	}

	public void testJavadoc3() throws JavaModelException {
		this.moduleUnit = getWorkingCopy("/Converter_15_1/src/module-info.java",
				"module test1.one.two {\r\n" +
				"}");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter_15_1/src/javadoc/X.java",
			"package javadoc;\n" +
			"public class X {\n" +
			"	/** \n" +
			"	 * @see test1.one.two/javadoc.X#foo \n" +
			"	 */ \n" +
			"	public static void foo(Object object) {\n" +
			"	}\n" +
			"}\n"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Get comments
			List unitComments = compilUnit.getCommentList();
			int size = unitComments.size();
			assertEquals("Wrong number of comments", 1, size);
			Javadoc[] javadocs = new Javadoc[size];
			Iterator iterator = unitComments.iterator();
			for (int i=0; i<size; i++) {
				Comment comment = (Comment) iterator.next();
				assertEquals("Expect javadoc for comment: "+comment, ASTNode.JAVADOC, comment.getNodeType());
				javadocs[i] = (Javadoc) comment;
			}

			// Verify member type declaration start
			ASTNode node = getASTNode(compilUnit, 0, 0);
			assertEquals("Expected method declaration for node: "+node, ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			int javadocStart = javadocs[0].getStartPosition();
			assertEquals("Invalid start position for MethodDeclaration: "+methodDeclaration, methodDeclaration.getStartPosition(), javadocStart);
		}
	}

	public void testJavadoc2() throws JavaModelException {
		this.moduleUnit = getWorkingCopy("/Converter_15_1/src/module-info.java",
				"module test1.one.two {\r\n" +
				"}");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter_15_1/src/javadoc/X.java",
			"package javadoc;\n" +
			"public class X {\n" +
			"	/** \n" +
			"	 * @see test1.one.two/javadoc.X \n" +
			"	 */ \n" +
			"	public static void foo(Object object) {\n" +
			"	}\n" +
			"}\n"
		);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			// Get comments
			List unitComments = compilUnit.getCommentList();
			int size = unitComments.size();
			assertEquals("Wrong number of comments", 1, size);
			Javadoc[] javadocs = new Javadoc[size];
			Iterator iterator = unitComments.iterator();
			for (int i=0; i<size; i++) {
				Comment comment = (Comment) iterator.next();
				assertEquals("Expect javadoc for comment: "+comment, ASTNode.JAVADOC, comment.getNodeType());
				javadocs[i] = (Javadoc) comment;
			}

			// Verify member type declaration start
			ASTNode node = getASTNode(compilUnit, 0, 0);
			assertEquals("Expected method declaration for node: "+node, ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			int javadocStart = javadocs[0].getStartPosition();
			assertEquals("Invalid start position for MethodDeclaration: "+methodDeclaration, methodDeclaration.getStartPosition(), javadocStart);
		}
	}
}
