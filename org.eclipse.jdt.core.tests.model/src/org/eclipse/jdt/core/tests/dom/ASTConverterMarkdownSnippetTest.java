/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.internal.compiler.parser.JavadocTagConstants;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;

/**
 * Class to test DOM/AST nodes built for markdown snippet tags.
 *
 * Most of tests are 'automatic'. It means that to add a new tests, you only need to
 * create one or several CUs and put them in org.eclipse.jdt.core.model.tests/workspace/Converter/src/markdown/testXXX
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
public class ASTConverterMarkdownSnippetTest extends ConverterTestSetup {

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

		List comments = new ArrayList();
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
		/** @deprecated using deprecated code */
		protected int astLevel = AST.JLS23;
		protected int savedLevel;
		// Debug
		protected String prefix = "";
		protected boolean debug = false;
		protected StringBuilder problems;
		protected String compilerOption = JavaCore.IGNORE;
		protected List<String> failures;
		Map savedOptions = null;

		private static String SNIPPET_TAG = '@' + new String(JavadocTagConstants.TAG_SNIPPET);

		public ASTConverterMarkdownSnippetTest(String name, String support, String unix) {
			super(name);
			this.docCommentSupport = support;
			this.unix = "true".equals(unix);
		}
		public ASTConverterMarkdownSnippetTest(String name) {
			this(preHyphen(name), nameToSupport(name),
					name.indexOf(" - Unix") != -1 ? "true" : "false");
		}

		private static String preHyphen(String name) {
			int hyphenInd = name.indexOf(" - ");
			String r = hyphenInd == -1 ? name : name.substring(0, hyphenInd);
			return r;
		}
		private static String nameToSupport(String name) {
			int ind1 = name.indexOf(" - Doc ");
			int ind2 = name.lastIndexOf("abled");
			if( ind1 == -1 || ind2 == -1 )
				return name;
			String s = name.substring(name.indexOf(" - Doc ") + 7, name.lastIndexOf("abled") + 5);
			return s;
		}

		/* (non-Javadoc)
		 * @see junit.framework.TestCase#getName()
		 */
		public String getName() {
			String strUnix = this.unix ? " - Unix" : "";
			return super.getName()+" - Doc "+this.docCommentSupport+strUnix;
		}

		public static Test suite() {
			TestSuite suite = new Suite(ASTConverterMarkdownSnippetTest.class.getName());
			if (DOC_COMMENT_SUPPORT == null) {
				buildSuite(suite, JavaCore.ENABLED);
				buildSuite(suite, JavaCore.DISABLED);
			} else {
				String support = DOC_COMMENT_SUPPORT==null ? JavaCore.DISABLED : (DOC_COMMENT_SUPPORT.equals(JavaCore.DISABLED)?JavaCore.DISABLED:JavaCore.ENABLED);
				buildSuite(suite, support);
			}
			return suite;
		}

		public static void buildSuite(TestSuite suite, String support) {
			Class c = ASTConverterMarkdownSnippetTest.class;
			Method[] methods = c.getMethods();
			for (int i = 0, max = methods.length; i < max; i++) {
				if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
					suite.addTest(new ASTConverterMarkdownSnippetTest(methods[i].getName(), support, UNIX_SUPPORT));
				}
			}
			// when unix support not specified, also run using unix format
			if (UNIX_SUPPORT == null && JavaCore.ENABLED.equals(support)) {
				for (int i = 0, max = methods.length; i < max; i++) {
					if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
						suite.addTest(new ASTConverterMarkdownSnippetTest(methods[i].getName(), support, "true"));
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
			setCompliancesLevel();
			this.failures = new ArrayList();
			this.problems = new StringBuilder();
			this.workingCopies = null;
			this.savedLevel = this.astLevel;
		}
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
			assertTrue(title, size==0 || this.problems.length() > 0);
			super.tearDown();

			// Restore saved ast level
			this.astLevel = this.savedLevel;
		}

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
		public ASTNode runConversion(ICompilationUnit unit, boolean resolveBindings) {
			return runConversion(AST.JLS23, unit, resolveBindings);
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
	/*
	 * Convert Javadoc source to match markdown.toString().
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
	 * Convert Javadoc source to match markdown.toString().
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
	private void addFailure(String msg) {
		this.failures.add(msg);
	}
	protected void assumeTrue(String msg, boolean cond) {
		if (!cond) {
			addFailure(msg);
		}
	}

	protected void assumeNull(String msg, Object obj) {
		if (obj != null) {
			addFailure(msg);
		}
	}

	protected void assumeNotNull(String msg, Object obj) {
		if (obj == null) {
			addFailure(msg);
		}
	}

	protected void assumeEquals(String msg, int expected, int actual) {
		if (expected != actual) {
			addFailure(msg+", expected="+expected+" actual="+actual);
		}
	}

	protected void assumeEquals(String msg, Object expected, Object actual) {
		if (expected == null && actual == null)
			return;
		if (expected != null && expected.equals(actual))
			return;
		addFailure(msg+", expected:<"+expected+"> actual:<"+actual+'>');
	}


	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void verifyComments(String test) throws JavaModelException {
		ICompilationUnit[] units = getCompilationUnits("Converter_26" , "src", "markdown."+test); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		String sourceStr = this.sourceUnit.getSource();
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

	private void setCompliancesLevel() {
		this.currentProject = getJavaProject("Converter_26");
		// set up java project options
		this.currentProject.setOption(JavaCore.COMPILER_PB_INVALID_JAVADOC, this.compilerOption);
		this.currentProject.setOption(JavaCore.COMPILER_PB_MISSING_JAVADOC_TAGS, this.compilerOption);
		this.currentProject.setOption(JavaCore.COMPILER_PB_MISSING_JAVADOC_COMMENTS, this.compilerOption);
		this.currentProject.setOption(JavaCore.COMPILER_PB_METHOD_WITH_CONSTRUCTOR_NAME, JavaCore.IGNORE);
		this.currentProject.setOption(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, this.docCommentSupport);
		this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_26);
		this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_26);
		this.astLevel = AST.JLS23;
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

		// Create DOM AST nodes hierarchy
		List unitComments = null;
		setCompliancesLevel();
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

	// empty body
	public void testSnippetTagforMarkdown_01() throws JavaModelException {
		String source = """
					/// {@snippet :
					/// }
					public class Markdown {}
				""";
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter_26/src/markdown/Markdown.java", source, null);
		CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			List unitComments = compilUnit.getCommentList();
			int size = unitComments.size();
			assertEquals("Wrong number of comments", 1, size);
			Javadoc javadoc = (Javadoc) unitComments.get(0);
			TagElement snippetTag = getSnippetTag(javadoc);
			List<?> frags = snippetTag.fragments();
			assertEquals("Fragments should be empty", 0, frags.size());
		}
	}

	// Single element
	public void testSnippetMarkdownTest_02() throws JavaModelException {
		String source = """
				/// {@snippet:
				/// 	int x = 2;
				/// }
				public class Markdown {}
			""";
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter_26/src/markdown/Markdown.java", source, null);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
			List unitComments = compilUnit.getCommentList();
			int size = unitComments.size();
			assertEquals("Wrong number of comments", 1, size);
			Javadoc javadoc = (Javadoc) unitComments.get(0);
			TagElement snippetTag = getSnippetTag(javadoc);
			List<TextElement> frags = snippetTag.fragments();
			assertEquals("Fragments should be 1", 1, frags.size());
			assertEquals("Incorrect text content", " 	int x = 2;", frags.get(0).getText());
		}
	}

	// Blank line inside the body
	public void testSnippetMarkdownTest_03() throws JavaModelException {
		String source = """
		/// {@snippet:
		///
		/// 	int a = 1;
		///
		///		int b = 2;
		/// }
		public class Markdown {}
		""";
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter_26/src/markdown/Markdown.java", source, null);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
			List unitComments = compilUnit.getCommentList();
			int size = unitComments.size();
			assertEquals("Wrong number of comments", 1, size);
			Javadoc javadoc = (Javadoc) unitComments.get(0);
			TagElement snippetTag = getSnippetTag(javadoc);
			List<TextElement> frags = snippetTag.fragments();
			assertEquals("Fragments should be 4", 4, frags.size());
			assertEquals("Incorrect text content", "", frags.get(0).getText());
			assertEquals("Incorrect text content", " 	int a = 1;", frags.get(1).getText());
			assertEquals("Incorrect text content", "", frags.get(2).getText());
			assertEquals("Incorrect text content", "		int b = 2;", frags.get(3).getText());
		}
	}

	// {@code} tag inside the snippet
	public void testSnippetMarkdownTest_04() throws JavaModelException {
		String source = """
					/// {@snippet :
					///   {@code int a = 0;}
					/// }
				""";
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Converter_26/src/markdown/Markdown.java", source, null);
		if (this.docCommentSupport.equals(JavaCore.ENABLED)) {
			CompilationUnit compilUnit = verifyComments(this.workingCopies[0]);
			List unitComments = compilUnit.getCommentList();
			int size = unitComments.size();
			assertEquals("Wrong number of comments", 1, size);
			Javadoc javadoc = (Javadoc) unitComments.get(0);
			TagElement snippetTag = getSnippetTag(javadoc);
			List<TextElement> frags = snippetTag.fragments();
			assertEquals("Fragments should be 1", 1, frags.size());
			assertEquals("Incorrect text content", "   {@code int a = 0;}", frags.get(0).getText());
		}
	}
}
