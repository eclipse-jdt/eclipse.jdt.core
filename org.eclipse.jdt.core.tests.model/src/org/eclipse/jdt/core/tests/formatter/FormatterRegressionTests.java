package org.eclipse.jdt.core.tests.formatter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatter;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.jdt.internal.formatter.align.Alignment;
import org.eclipse.text.edits.TextEdit;

public class FormatterRegressionTests extends AbstractJavaModelTests {
		
	public static final int UNKNOWN_KIND = 0;
	public static final String IN = "_in";
	public static final String OUT = "_out";
	public static final boolean DEBUG = false;
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	public static Test suite() {
		if (true) {
			return new Suite(FormatterRegressionTests.class);
		} else {
			junit.framework.TestSuite suite = new Suite(FormatterRegressionTests.class.getName());
			suite.addTest(new FormatterRegressionTests("test407"));  //$NON-NLS-1$
			return suite;
		}
	}

	public FormatterRegressionTests(String name) {
		super(name);
	}
	
	/**
	 * Returns the OS path to the directory that contains this plugin.
	 */
	protected String getPluginDirectoryPath() {
		try {
			return new File(Platform.resolve(Platform.getPlugin("org.eclipse.jdt.core.tests.model").getDescriptor().getInstallURL()).getFile()).getAbsolutePath();
		} catch (IOException e) {
			//Error
		}
		return null;
	}

	public String getSourceWorkspacePath() {
		return getPluginDirectoryPath() +  java.io.File.separator + "workspace";
	}
	
	private String runFormatter(DefaultCodeFormatter codeFormatter, String source, int kind, int indentationLevel, int offset, int length) {
		//long time = System.currentTimeMillis();
		TextEdit edit = codeFormatter.format(kind, source, offset, length, indentationLevel, null);//$NON-NLS-1$
		if (edit == null) return null;
		String result = org.eclipse.jdt.internal.core.util.Util.editedString(source, edit);

		if (length == source.length()) {
			//time = System.currentTimeMillis();
			edit = codeFormatter.format(kind, result, 0, result.length(), indentationLevel, null);//$NON-NLS-1$
			if (edit == null) return null;
//			assertEquals("Shoult not have edits", 0, edit.getChildren().length);
			final String result2 = org.eclipse.jdt.internal.core.util.Util.editedString(result, edit);
			if (!result.equals(result2)) {
				assertEquals("Different reformatting", result2, result);
			}
		}
		if (DefaultCodeFormatter.DEBUG){
			System.out.println(codeFormatter.getDebugOutput());
			
		}
		return result;
	}

	/**
	 * Create project and set the jar placeholder.
	 */
	public void setUpSuite() throws Exception {
		// ensure autobuilding is turned off
		IWorkspaceDescription description = getWorkspace().getDescription();
		if (description.isAutoBuilding()) {
			description.setAutoBuilding(false);
			getWorkspace().setDescription(description);
		}
		setUpJavaProject("Formatter"); //$NON-NLS-1$
	}	

	/**
	 * Reset the jar placeholder and delete project.
	 */
	public void tearDownSuite() throws Exception {
		this.deleteProject("Formatter"); //$NON-NLS-1$
		
		super.tearDown();
	}	

	private String getIn(String compilationUnitName) {
		assertNotNull(compilationUnitName);
		int dotIndex = compilationUnitName.indexOf('.');
		assertTrue(dotIndex != -1);
		return compilationUnitName.substring(0, dotIndex) + IN + compilationUnitName.substring(dotIndex);
	}
	
	private String getOut(String compilationUnitName) {
		assertNotNull(compilationUnitName);
		int dotIndex = compilationUnitName.indexOf('.');
		assertTrue(dotIndex != -1);
		return compilationUnitName.substring(0, dotIndex) + OUT + compilationUnitName.substring(dotIndex);
	}

	private void assertLineEquals(String actualContents, String originalSource, String expectedContents, boolean checkNull) {
		if (actualContents == null) {
			assertTrue("actualContents is null", checkNull);
			assertEquals(originalSource, expectedContents);
			return;
		}
		String[] actualContentsArray = createArrayOfString(actualContents);
		String[] expectedContentsArray = createArrayOfString(expectedContents);
		if (actualContentsArray.length != expectedContentsArray.length) {
			System.out.println(Util.displayString(actualContents, 2));
		}
		assertEquals("Different size", expectedContentsArray.length, actualContentsArray.length); //$NON-NLS-1$
		for (int i = 0, max = expectedContentsArray.length; i < max; i++) {
			if (!expectedContentsArray[i].equals(actualContentsArray[i])){
				System.out.println(Util.displayString(actualContentsArray[i], 2));
			}
			if (!expectedContentsArray[i].equals(actualContentsArray[i])) {
				System.out.println("line " + i + " is different");
				System.out.println(Util.displayString(actualContents, 2));
				assertTrue("Different line", false);
			}
		}
	}

	private String[] createArrayOfString(String s) {
		ArrayList arrayList = new ArrayList();
		int start = 0;
		char[] source = s.toCharArray();
		for (int i = 0, max = source.length; i < max; i++) {
			switch(source[i]) {
				case '\r':
					arrayList.add(s.substring(start, i));
					if ((i + 1) < max) {
						if (source[i + 1] == '\n') {
							i++;
						}
					}
					start = i + 1;
					break;
				case '\n' :
					arrayList.add(s.substring(start, i));
					start = i + 1;
			}
		}
		final int size = arrayList.size();
		if (size == 0) {
			return new String[] { s };
		} else {
			return (String[]) arrayList.toArray(new String[size]);
		}
	}

	private void runTest(String packageName, String compilationUnitName) {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, packageName, compilationUnitName, CodeFormatter.K_COMPILATION_UNIT, 0);
	}
	
	private void runTest(DefaultCodeFormatter codeFormatter, String packageName, String compilationUnitName) {
		runTest(codeFormatter, packageName, compilationUnitName, CodeFormatter.K_COMPILATION_UNIT, 0);
	}

	private void runTest(String packageName, String compilationUnitName, int kind) {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, packageName, compilationUnitName, kind, 0);
	}

	private void runTest(DefaultCodeFormatter codeFormatter, String packageName, String compilationUnitName, int kind) {
		runTest(codeFormatter, packageName, compilationUnitName, kind, 0, false, 0, -1);
	}

	private void runTest(DefaultCodeFormatter codeFormatter, String packageName, String compilationUnitName, int kind, boolean checkNull) {
		runTest(codeFormatter, packageName, compilationUnitName, kind, 0, checkNull, 0, -1);
	}

	private void runTest(DefaultCodeFormatter codeFormatter, String packageName, String compilationUnitName, int kind, int indentationLevel) {
		runTest(codeFormatter, packageName, compilationUnitName, kind, indentationLevel, false, 0, -1);
	}

	private void runTest(DefaultCodeFormatter codeFormatter, String packageName, String compilationUnitName, int kind, int indentationLevel, boolean checkNull, int offset, int length) {
		try {
				ICompilationUnit sourceUnit = getCompilationUnit("Formatter" , "", packageName, getIn(compilationUnitName)); //$NON-NLS-1$ //$NON-NLS-2$
				String s = sourceUnit.getSource();
				assertNotNull(s);
				ICompilationUnit outputUnit = getCompilationUnit("Formatter" , "", packageName, getOut(compilationUnitName)); //$NON-NLS-1$ //$NON-NLS-2$
				assertNotNull(outputUnit);
				String result;
				if (length == -1) {
					result = runFormatter(codeFormatter, s, kind, indentationLevel, offset, s.length());
				} else {
					result = runFormatter(codeFormatter, s, kind, indentationLevel, offset, length);
				}
				assertLineEquals(result, s, outputUnit.getSource(), checkNull);
		} catch (JavaModelException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	private void runTest(String source, String expectedResult, DefaultCodeFormatter codeFormatter, int kind, int indentationLevel, boolean checkNull, int offset, int length) {
		String result;
		if (length == -1) {
			result = runFormatter(codeFormatter, source, kind, indentationLevel, offset, source.length());
		} else {
			result = runFormatter(codeFormatter, source, kind, indentationLevel, offset, length);
		}
		assertLineEquals(result, source, expectedResult, checkNull);
	}
	
	public void _test() {
		try {
			char[] contents = org.eclipse.jdt.internal.compiler.util.Util.getFileCharContent(new File("D:/workspaces/eclipse/plugins/TestingOlivier/src/FormatterRegressionTests.java"), null);
			CompilationUnit compilationUnit = AST.parseCompilationUnit(contents);
			List types = compilationUnit.types();
			TypeDeclaration typeDeclaration = (TypeDeclaration) types.get(0);
			MethodDeclaration[] methodDeclarations = typeDeclaration.getMethods();
			int testCaseCounter = 229;
			for (int i = 0, max = methodDeclarations.length; i < max; i++) {
				MethodDeclaration methodDeclaration = methodDeclarations[i];
				final SimpleName methodName = methodDeclaration.getName();
				if (methodName.getIdentifier().startsWith("test")) {
					Block block = methodDeclaration.getBody();
					List statements = block.statements();
					Statement statement = (Statement) statements.get(0);
					if (statement.getNodeType() == ASTNode.VARIABLE_DECLARATION_STATEMENT) {
						VariableDeclarationStatement localDeclaration = (VariableDeclarationStatement) statement;
						List fragments = localDeclaration.fragments();
						VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
						if (fragment.getName().getIdentifier().equals("source")) {
							Expression expression = fragment.getInitializer();
							StringBuffer buffer = new StringBuffer();
							switch(expression.getNodeType()) {
								case ASTNode.INFIX_EXPRESSION :
									InfixExpression expression2 = (InfixExpression) expression;
									List extendedOperands = expression2.extendedOperands();
									buffer.append(getSource(expression2.getLeftOperand(), contents));
									buffer.append(getSource(expression2.getRightOperand(), contents));
									for (int j = 0, max2 = extendedOperands.size(); j < max2; j++) {
										buffer.append(getSource((Expression) extendedOperands.get(j), contents));
									}
									break;
								case ASTNode.STRING_LITERAL :
									StringLiteral literal = (StringLiteral) expression;
									buffer.append(getSource(literal, contents));
									break;
							}
							createTestCase(buffer.toString(), testCaseCounter++);
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	void createTestCase(String contents, int counter) {
		System.out.println("Create test " + counter);
		try {
			File testDir = new File("D:/workspaces/eclipse/plugins/org.eclipse.jdt.core.tests.model/workspace/Formatter", "test" + counter);
			testDir.mkdirs();
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File(testDir, "A_in.java")));
			writer.write(contents);
			writer.flush();
			writer.close();
			writer = new BufferedWriter(new FileWriter(new File(testDir, "A_out.java")));
			writer.write(contents);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Done test " + counter);
	}
	String getSource(ASTNode astNode, char[] source) {
		String result = new String(CharOperation.subarray(source, astNode.getStartPosition() + 1, astNode.getStartPosition() + astNode.getLength() - 1));
		if (result.endsWith("\\n")) {
			return result.substring(0, result.length() - 2) + LINE_SEPARATOR;
		} else {
			return result;
		}
	}

	public void test001() {
		runTest("test001", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}	
	public void test002() {
		runTest("test002", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test003() {
		runTest("test003", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test004() {
		runTest("test004", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	public void test005() {
		runTest("test005", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test006() {
		runTest("test006", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test007() {
		runTest("test007", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test008() {
		runTest("test008", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test009() {
		runTest("test009", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test010() {
		runTest("test010", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test011() {
		runTest("test011", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test012() {
		runTest("test012", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test013() {
		runTest("test013", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test014() {
		runTest("test014", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test015() {
		runTest("test015", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test016() {
		runTest("test016", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test017() {
		runTest("test017", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test018() {
		runTest("test018", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test019() {
		DefaultCodeFormatterOptions formatPrefs = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		formatPrefs.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(formatPrefs);
		runTest(codeFormatter, "test019", "A_1.java");//$NON-NLS-1$ //$NON-NLS-2$

		formatPrefs.use_tab = false;
		codeFormatter = new DefaultCodeFormatter(formatPrefs);
		runTest(codeFormatter, "test019", "A_2.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test020() {
		runTest("test020", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test021() {
		runTest("test021", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test022() {
		runTest("test022", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test023() {
		runTest("test023", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test024() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.keep_simple_if_on_one_line = true;
		preferences.keep_then_statement_on_same_line = true;
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test024", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test025() {
		runTest("test025", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test026() {
		runTest("test026", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test027() {
		runTest("test027", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test028() {
		runTest("test028", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test029() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.keep_simple_if_on_one_line = true;
		preferences.keep_then_statement_on_same_line = true;
		preferences.format_guardian_clause_on_one_line = true;
		preferences.use_tab = true;
		preferences.compact_else_if = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter,"test029", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test030() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.keep_simple_if_on_one_line = true;
		preferences.keep_then_statement_on_same_line = true;
		preferences.format_guardian_clause_on_one_line = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test030", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test031() {
		runTest("test031", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test032() {
		runTest("test032", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test033() {
		runTest("test033", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test034() {
		runTest("test034", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test035() {
		runTest("test035", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test036() {
		runTest("test036", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test037() {
		runTest("test037", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test038() {
		runTest("test038", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test039() {
		runTest("test039", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test040() {
		runTest("test040", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test041() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_new_line_in_empty_type_declaration = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test041", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test042() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_new_line_in_empty_type_declaration = false;
		preferences.insert_space_before_block_open_brace = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test042", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test043() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_new_line_in_empty_type_declaration = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test043", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test044() {
		runTest("test044", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test045() {
		runTest("test045", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test046() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_assignment_operators = false;
		preferences.insert_space_before_assignment_operators = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test046", "A.java", CodeFormatter.K_EXPRESSION);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test047() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_assignment_operators = true;
		preferences.insert_space_before_assignment_operators = true;
		preferences.preserve_user_linebreaks = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test047", "A.java", CodeFormatter.K_STATEMENTS, 2);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test048() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_assignment_operators = true;
		preferences.insert_space_before_assignment_operators = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test048", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test049() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_assignment_operators = true;
		preferences.insert_space_before_assignment_operators = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test049", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test050() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_binary_operator = false;
		preferences.insert_space_before_unary_operator = false;
		preferences.insert_space_after_unary_operator = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test050", "A.java", CodeFormatter.K_EXPRESSION);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test051() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_assignment_operators = true;
		preferences.insert_space_before_assignment_operators = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test051", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test052() {
		runTest("test052", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test053() {
		runTest("test053", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test054() {
		runTest("test054", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test055() {
		runTest("test055", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test056() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.keep_simple_if_on_one_line = true;
		preferences.keep_then_statement_on_same_line = true;
		preferences.keep_else_statement_on_same_line = true;
		preferences.format_guardian_clause_on_one_line = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test056", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test057() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.type_member_alignment = Alignment.M_MULTICOLUMN;
		preferences.array_initializer_expressions_alignment = Alignment.M_COMPACT_SPLIT;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test057", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test058() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.type_member_alignment = Alignment.M_MULTICOLUMN;
		preferences.array_initializer_expressions_alignment = Alignment.M_COMPACT_SPLIT;
		preferences.use_tab = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test058", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test059() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.type_member_alignment = Alignment.M_NO_ALIGNMENT;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test059", "Parser.java");//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test060() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.type_member_alignment = Alignment.M_NO_ALIGNMENT;
		preferences.use_tab = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		//long time = System.currentTimeMillis();
		runTest(codeFormatter, "test060", "Parser.java");//$NON-NLS-1$ //$NON-NLS-2$
	}		

	public void test061() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.type_member_alignment = Alignment.M_NO_ALIGNMENT;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test061", "Parser.java");//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test062() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_assignment_operators = true;
		preferences.insert_space_before_assignment_operators = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test062", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test063() {
		runTest("test063", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test064() {
		runTest("test064", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// Line break inside an array initializer (line comment)	
	public void test065() {
		runTest("test065", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test066() {
		runTest("test066", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test067() {
		runTest("test067", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}	

	// bug 3181
	public void test068() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.allocation_expression_arguments_alignment = Alignment.M_ONE_PER_LINE_SPLIT;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test068", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 3327
	public void test069() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.blank_lines_before_first_class_body_declaration = 1;
		preferences.blank_lines_before_method = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test069", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// 5691
	public void test070() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test070", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test071() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.indent_body_declarations_compare_to_type_header = false;
		preferences.type_declaration_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		preferences.type_member_alignment = Alignment.M_MULTICOLUMN;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test071", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// bug 7224
	public void test072() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.number_of_empty_lines_to_preserve = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test072", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 7439
	public void test073() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.keep_simple_if_on_one_line = true;
		preferences.keep_then_statement_on_same_line = true;
		preferences.format_guardian_clause_on_one_line = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test073", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 12321
	public void test074() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_new_line_in_control_statements = true;
		preferences.keep_simple_if_on_one_line = false;
		preferences.keep_then_statement_on_same_line = false;
		preferences.keep_else_statement_on_same_line = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test074", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 14659
	public void test075() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.method_declaration_arguments_alignment = Alignment.M_INDENT_ON_COLUMN | Alignment.M_NEXT_PER_LINE_SPLIT;
		preferences.page_width = 57;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test075", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 16231
	public void test076() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.array_initializer_expressions_alignment = Alignment.M_ONE_PER_LINE_SPLIT;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test076", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 16233
	public void test077() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.method_declaration_arguments_alignment = Alignment.M_NEXT_PER_LINE_SPLIT;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test077", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 17349
	public void test078() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.type_declaration_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		preferences.block_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test078", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 19811
	public void test079() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test079", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 19811
	public void test080() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.method_declaration_arguments_alignment = Alignment.M_COMPACT_SPLIT;
		preferences.continuation_indentation = 2;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test080", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 19811
	public void test081() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.array_initializer_expressions_alignment = Alignment.M_ONE_PER_LINE_SPLIT;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test081", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 19999
	public void test082() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.number_of_empty_lines_to_preserve = 2;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test082", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// bug 20721
	public void test083() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test083", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 21943
	public void test084() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_before_if_condition = false;
		preferences.insert_space_before_for_paren = false;
		preferences.insert_space_before_while_condition = false;
		preferences.keep_simple_if_on_one_line = true;
		preferences.keep_then_statement_on_same_line = true;
		preferences.format_guardian_clause_on_one_line = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test084", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 21943
	public void test085() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_before_if_condition = true;
		preferences.insert_space_before_for_paren = true;
		preferences.insert_space_before_while_condition = true;
		preferences.keep_simple_if_on_one_line = true;
		preferences.keep_then_statement_on_same_line = true;
		preferences.format_guardian_clause_on_one_line = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test085", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 22313
	public void test086() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_new_line_in_control_statements = true;
		preferences.block_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		preferences.type_declaration_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		preferences.method_declaration_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		preferences.insert_space_before_binary_operator = false;
		preferences.insert_space_after_binary_operator = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test086", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 23144
	public void test087() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.keep_simple_if_on_one_line = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test087", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 23144
	public void test088() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.keep_simple_if_on_one_line = false;
		preferences.format_guardian_clause_on_one_line = false;
		preferences.keep_then_statement_on_same_line = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test088", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// bug 24200
	public void test089() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_open_paren_in_parenthesized_expression = true;
		preferences.insert_space_before_closing_paren_in_parenthesized_expression = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test089", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 24200
	public void test090() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_between_brackets_in_array_reference = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test090", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 25559
	public void test091() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_assignment_operators = false;
		preferences.insert_space_before_assignment_operators = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test091", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 25559
	public void test092() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_binary_operator = false;
		preferences.insert_space_before_binary_operator = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test092", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 25559
	public void test093() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_closing_paren_in_cast = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test093", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 25559
	public void test094() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_assignment_operators = false;
		preferences.insert_space_before_assignment_operators = false;
		preferences.insert_space_after_comma_in_messagesend_arguments = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test094", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// bug 27196
	public void test095() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.block_brace_position = DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED;
		preferences.indent_block_statements = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test095", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 28098
	public void test096() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test096", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 34897
	public void test097() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_within_message_send = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test097", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 35173
	public void test098() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.anonymous_type_declaration_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		preferences.method_declaration_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test098", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 35433
	public void test099() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_before_method_declaration_open_paren = true;
		preferences.insert_space_before_for_paren = true;
		preferences.insert_space_after_semicolon_in_for = false;
		preferences.put_empty_statement_on_new_line = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test099", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test100() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_before_opening_brace_in_array_initializer = true;
		preferences.insert_space_before_first_initializer = true;
		preferences.insert_space_before_closing_brace_in_array_initializer = true;
		preferences.number_of_empty_lines_to_preserve = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test100", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// bug 36832
	public void test101() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test101", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 37057
	public void test102() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.number_of_empty_lines_to_preserve = 1;
		preferences.line_separator = "\n";//$NON-NLS-1$
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test102", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 37106
	public void test103() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test103", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 37657
	public void test104() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_in_if_condition = true;
		preferences.block_brace_position = DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED;
		preferences.insert_new_line_in_control_statements = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test104", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 38151
	public void test105() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.method_declaration_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		preferences.type_declaration_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test105", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 39603
	public void test106() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test106", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// bug 39607
	public void test107() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.keep_then_statement_on_same_line = false;
		preferences.keep_simple_if_on_one_line = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test107", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// bug 40777
	public void test108() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test108", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test109() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.blank_lines_before_package = 2;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test109", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test110() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.blank_lines_before_package = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test110", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test111() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.blank_lines_after_package = 1;
		preferences.blank_lines_before_first_class_body_declaration = 1;
		preferences.blank_lines_before_new_chunk = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test111", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test112() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.blank_lines_after_package = 1;
		preferences.blank_lines_before_first_class_body_declaration = 1;
		preferences.blank_lines_before_new_chunk = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test112", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test113() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.message_send_arguments_alignment = Alignment.M_INDENT_ON_COLUMN | Alignment.M_NEXT_PER_LINE_SPLIT;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test113", "A.java");//$NON-NLS-1$ //$NON-NLS-2
	}

	// bug 14659
	public void test114() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = false;
		preferences.method_declaration_arguments_alignment = Alignment.M_INDENT_ON_COLUMN | Alignment.M_NEXT_PER_LINE_SPLIT;
		preferences.page_width = 57;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test114", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 14659
	public void test115() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.method_declaration_arguments_alignment = Alignment.M_INDENT_ON_COLUMN | Alignment.M_NEXT_PER_LINE_SPLIT;
		preferences.page_width = 57;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test115", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 14659
	public void test116() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.method_declaration_arguments_alignment = Alignment.M_INDENT_ON_COLUMN | Alignment.M_NEXT_PER_LINE_SPLIT;
		preferences.page_width = 57;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test116", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// JDT/UI tests
	public void test117() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test117", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// JDT/UI tests
	public void test118() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test118", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// JDT/UI tests
	public void test119() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test119", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// JDT/UI tests
	public void test120() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test120", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// JDT/UI tests
	public void test121() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test121", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// probing statements
	public void test122() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test122", "A.java", CodeFormatter.K_UNKNOWN);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// probing compilation unit
	public void test123() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test123", "A.java", CodeFormatter.K_UNKNOWN);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	// probing class body declarations
	public void test124() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.method_declaration_arguments_alignment = Alignment.M_INDENT_ON_COLUMN | Alignment.M_NEXT_PER_LINE_SPLIT;
		preferences.page_width = 57;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test124", "A.java", CodeFormatter.K_UNKNOWN);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// probing expression
	public void test125() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_binary_operator = false;
		preferences.insert_space_before_unary_operator = false;
		preferences.insert_space_after_unary_operator = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test125", "A.java", CodeFormatter.K_UNKNOWN);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// probing unrecognized source
	public void test126() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test126", "A.java", CodeFormatter.K_UNKNOWN, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// probing unrecognized source
	public void test127() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test127", "A.java", CodeFormatter.K_UNKNOWN);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// probing unrecognized source
	public void test128() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test128", "A.java", CodeFormatter.K_UNKNOWN, true);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test129() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test129", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test130() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test130", "A.java", CodeFormatter.K_COMPILATION_UNIT, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test131() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test131", "A.java", CodeFormatter.K_COMPILATION_UNIT, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test132() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test132", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test133() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test133", "A.java", CodeFormatter.K_COMPILATION_UNIT, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test134() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test134", "A.java", CodeFormatter.K_COMPILATION_UNIT, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test135() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test135", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test136() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test136", "A.java", CodeFormatter.K_COMPILATION_UNIT, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test137() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test137", "A.java", CodeFormatter.K_COMPILATION_UNIT, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test138() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test138", "A.java", CodeFormatter.K_STATEMENTS, 2, true, 8, 37);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test139() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test139", "A.java", CodeFormatter.K_STATEMENTS, 0, true, 0, 5);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test140() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test140", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test141() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = false;
		preferences.indent_switchstatements_compare_to_cases = false;
		preferences.indent_switchstatements_compare_to_switch = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test141", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test142() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test142", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, 1);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test143() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test143", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, 1);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test144() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test144", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test145() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test145", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test146() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test146", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test147() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_before_assignment_operators = false;
		preferences.number_of_empty_lines_to_preserve = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test147", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test148() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test148", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test149() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test149", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test150() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test150", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test151() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test151", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test152() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test152", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test153() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.type_member_alignment = Alignment.M_MULTICOLUMN;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test153", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test154() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test154", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}	
	
	public void test155() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getJavaConventionsSettings());
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test155", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44036
	public void test156() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getJavaConventionsSettings());
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test156", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test157() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test157", "A.java", CodeFormatter.K_STATEMENTS, 0, true, 11, 7);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test158() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test158", "A.java", CodeFormatter.K_STATEMENTS, 0, true, 11, 8);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test159() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(JavaCore.getOptions());
		preferences.number_of_empty_lines_to_preserve = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test159", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44481
	 */
	public void test160() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.insert_new_line_in_control_statements = false;
		preferences.compact_else_if = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test160", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44481
	 */
	public void test161() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.insert_new_line_in_control_statements = false;
		preferences.compact_else_if = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test161", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44481
	 */
	public void test162() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.insert_new_line_in_control_statements = true;
		preferences.compact_else_if = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test162", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44481
	 */
	public void test163() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.insert_new_line_in_control_statements = true;
		preferences.compact_else_if = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test163", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44493
	 */
	public void test164() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test164", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test165() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test165", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44546
	 */
	public void test166() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test166", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44503
	 */
	public void test167() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test167", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44503
	 */
	public void test169() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test169", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44503
	 */
	public void test170() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test170", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44576
	 */
	public void test171() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.anonymous_type_declaration_brace_position = DefaultCodeFormatterConstants.END_OF_LINE;
		preferences.type_declaration_brace_position = DefaultCodeFormatterConstants.END_OF_LINE;
		preferences.method_declaration_brace_position = DefaultCodeFormatterConstants.END_OF_LINE;
		preferences.block_brace_position = DefaultCodeFormatterConstants.END_OF_LINE;
		preferences.switch_brace_position = DefaultCodeFormatterConstants.END_OF_LINE;
		preferences.compact_else_if = false;
		preferences.insert_new_line_in_control_statements = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test171", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44576
	 */
	public void test172() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.anonymous_type_declaration_brace_position = DefaultCodeFormatterConstants.END_OF_LINE;
		preferences.type_declaration_brace_position = DefaultCodeFormatterConstants.END_OF_LINE;
		preferences.method_declaration_brace_position = DefaultCodeFormatterConstants.END_OF_LINE;
		preferences.block_brace_position = DefaultCodeFormatterConstants.END_OF_LINE;
		preferences.switch_brace_position = DefaultCodeFormatterConstants.END_OF_LINE;
		preferences.compact_else_if = false;
		preferences.insert_new_line_in_control_statements = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test172", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44570
	 */
	public void test173() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_new_line_in_empty_anonymous_type_declaration = false;
		preferences.insert_new_line_in_empty_type_declaration = false;
		preferences.insert_new_line_in_empty_method_body = false;
		preferences.insert_new_line_in_empty_block = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test173", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44570
	 */
	public void test174() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_new_line_in_empty_anonymous_type_declaration = false;
		preferences.insert_new_line_in_empty_type_declaration = false;
		preferences.insert_new_line_in_empty_method_body = false;
		preferences.insert_new_line_in_empty_block = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test174", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44570
	 */
	public void test175() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_new_line_in_empty_anonymous_type_declaration = false;
		preferences.insert_new_line_in_empty_type_declaration = false;
		preferences.insert_new_line_in_empty_method_body = false;
		preferences.insert_new_line_in_empty_block = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test175", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44570
	 */
	public void test176() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_new_line_in_empty_anonymous_type_declaration = false;
		preferences.insert_new_line_in_empty_type_declaration = false;
		preferences.insert_new_line_in_empty_method_body = true;
		preferences.insert_new_line_in_empty_block = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test176", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44570
	 */
	public void test177() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_new_line_in_empty_anonymous_type_declaration = false;
		preferences.insert_new_line_in_empty_type_declaration = false;
		preferences.insert_new_line_in_empty_method_body = false;
		preferences.insert_new_line_in_empty_block = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test177", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44570
	 */
	public void test178() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_new_line_in_empty_anonymous_type_declaration = false;
		preferences.insert_new_line_in_empty_type_declaration = true;
		preferences.insert_new_line_in_empty_method_body = false;
		preferences.insert_new_line_in_empty_block = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test178", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44570
	 */
	public void test179() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_new_line_in_empty_anonymous_type_declaration = true;
		preferences.insert_new_line_in_empty_type_declaration = true;
		preferences.insert_new_line_in_empty_method_body = false;
		preferences.insert_new_line_in_empty_block = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test179", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44570
	 */
	public void test180() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_new_line_in_empty_anonymous_type_declaration = false;
		preferences.insert_new_line_in_empty_type_declaration = false;
		preferences.insert_new_line_in_empty_method_body = false;
		preferences.insert_new_line_in_empty_block = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test180", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44651
	 */
	public void test181() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test181", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44651
	 */
	public void test182() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.number_of_empty_lines_to_preserve = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test182", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44653
	 */
	public void _test183() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.number_of_empty_lines_to_preserve = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test183", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44653
	 */
	public void _test184() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test184", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44653
	 */
	public void _test185() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test185", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 */
	public void _test186() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test186", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44839
	 */
	public void test187() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test187", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44839
	 */
	public void test188() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test188", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44839
	 */
	public void test189() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test189", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 */
	public void test190() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "1");
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test190", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 */
	public void test191() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(JavaCore.getOptions());
		preferences.number_of_empty_lines_to_preserve = 0;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test191", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test192() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.type_member_alignment = Alignment.M_MULTICOLUMN;
		preferences.array_initializer_expressions_alignment = Alignment.M_COMPACT_SPLIT;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test192", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test193() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test193", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test194() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test194", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test195() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test195", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test196() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test196", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test197() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test197", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test198() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test198", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test199() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test199", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test201() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test201", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * TODO Fix multi local declaration alignment
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44909
	 */
	public void _test202() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test202", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * TODO Fix multi local declaration alignment
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44909
	 */
	public void _test203() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test203", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test204() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test204", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test205() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "1");//$NON-NLS-1$
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test205", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test206() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test206", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test207() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test207", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test208() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test208", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test209() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "1");//$NON-NLS-1$
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test209", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test210() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "1");//$NON-NLS-1$
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test210", "A.java", CodeFormatter.K_COMPILATION_UNIT, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test211() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "1");//$NON-NLS-1$
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test211", "A.java", CodeFormatter.K_COMPILATION_UNIT, 1);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test212() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test212", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test213() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test213", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test214() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test214", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test215() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test215", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test216() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test216", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test217() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test217", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test218() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test218", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, 1);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test219() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test219", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, 1);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test220() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test220", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, 1);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test221() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test221", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test222() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test222", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	public void test223() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test223", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}	
	
	public void test224() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test224", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test225() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test225", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}	
	
	public void test226() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test226", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test227() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test227", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}	
	
	public void test228() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test228", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test229() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test229", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}	
	
	public void test230() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test230", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test231() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test231", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test232() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test232", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test233() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test233", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, 1);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test234() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test234", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test235() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test235", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test236() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test236", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test237() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test237", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test238() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test238", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test239() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test239", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test240() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test240", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test241() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test241", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test242() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test242", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test243() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test243", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test244() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test244", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test245() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test245", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test246() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test246", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test247() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test247", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test248() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test248", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test249() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test249", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test250() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test250", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test251() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test251", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test252() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test252", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test253() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test253", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test254() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test254", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test255() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test255", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test256() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test256", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test257() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test257", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test258() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test258", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test259() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test259", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test260() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test260", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test261() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test261", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test262() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test262", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test263() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test263", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test264() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test264", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test265() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test265", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test266() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test266", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test267() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test267", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test268() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test268", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test269() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test269", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test270() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test270", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test271() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test271", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test272() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test272", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test273() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test273", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test274() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test274", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test275() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test275", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test276() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test276", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test277() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test277", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test278() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test278", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test279() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test279", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test280() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test280", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test281() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test281", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test282() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test282", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test283() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test283", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test284() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test284", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test285() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test285", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test286() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test286", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test287() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test287", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test288() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test288", "A.java", CodeFormatter.K_STATEMENTS, 1);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test289() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test289", "A.java", CodeFormatter.K_STATEMENTS, 1);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test290() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test290", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test291() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test291", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test292() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test292", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test293() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test293", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test294() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test294", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test295() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test295", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test296() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test296", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test297() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(JavaCore.FORMATTER_TAB_SIZE, "4");
		options.put(JavaCore.FORMATTER_LINE_SPLIT, "100");
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test297", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test298() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(JavaCore.FORMATTER_LINE_SPLIT, "80");
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test298", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test299() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(JavaCore.FORMATTER_LINE_SPLIT, "80");
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test299", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test300() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test300", "A.java", CodeFormatter.K_EXPRESSION, 2);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test301() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_CLOSING_PAREN_IN_CAST, JavaCore.DO_NOT_INSERT);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test301", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test302() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test302", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test303() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.indent_switchstatements_compare_to_cases = true;
		preferences.indent_switchstatements_compare_to_switch = true;
		preferences.indent_breaks_compare_to_cases = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test303", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test304() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.indent_switchstatements_compare_to_cases = true;
		preferences.indent_switchstatements_compare_to_switch = true;
		preferences.indent_breaks_compare_to_cases = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test304", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test305() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.indent_switchstatements_compare_to_cases = false;
		preferences.indent_switchstatements_compare_to_switch = true;
		preferences.indent_breaks_compare_to_cases = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test305", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test306() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.indent_switchstatements_compare_to_cases = true;
		preferences.indent_switchstatements_compare_to_switch = true;
		preferences.indent_breaks_compare_to_cases = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test306", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test307() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.indent_switchstatements_compare_to_cases = true;
		preferences.indent_switchstatements_compare_to_switch = true;
		preferences.indent_breaks_compare_to_cases = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test307", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test308() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.indent_switchstatements_compare_to_cases = false;
		preferences.indent_switchstatements_compare_to_switch = false;
		preferences.indent_breaks_compare_to_cases = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test308", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test309() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.indent_switchstatements_compare_to_cases = false;
		preferences.indent_switchstatements_compare_to_switch = false;
		preferences.indent_breaks_compare_to_cases = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test309", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test310() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test310", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test311() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test311", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test312() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test312", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test313() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test313", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test314() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test314", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test315() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		String source = "public final void addDefinitelyAssignedVariables(Scope scope, int initStateIndex) {\n" + 
				"/*\n" + 
				"	\n" + 
				"*/\n" + 
				"}";
		String expectedResult = "public final void addDefinitelyAssignedVariables(Scope scope, int initStateIndex) {\r\n" + 
				"	/*\r\n" + 
				"	 \r\n" + 
				"	 */\r\n" + 
				"}";
		runTest(source, expectedResult, codeFormatter, CodeFormatter.K_CLASS_BODY_DECLARATIONS, 0, false, 0, -1);
	}

	public void test316() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		String source = "public final void addDefinitelyAssignedVariables(Scope scope, int initStateIndex) {\r" + 
				"/*\r" + 
				"	\r" + 
				"*/\r" + 
				"}";
		String expectedResult = "public final void addDefinitelyAssignedVariables(Scope scope, int initStateIndex) {\r\n" + 
				"	/*\r\n" + 
				"	 \r\n" + 
				"	 */\r\n" + 
				"}";
		runTest(source, expectedResult, codeFormatter, CodeFormatter.K_CLASS_BODY_DECLARATIONS, 0, false, 0, -1);
	}
	
	public void test317() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.line_separator = "\n";
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		String source = "public final void addDefinitelyAssignedVariables(Scope scope, int initStateIndex) {\r\n" + 
				"/*\r\n" + 
				"	\r\n" + 
				"*/\r\n" + 
				"}";
		String expectedResult = "public final void addDefinitelyAssignedVariables(Scope scope, int initStateIndex) {\n" + 
				"	/*\n" + 
				"	 \n" + 
				"	 */\n" + 
				"}";
		runTest(source, expectedResult, codeFormatter, CodeFormatter.K_CLASS_BODY_DECLARATIONS, 0, false, 0, -1);
	}
	
	public void test318() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.line_separator = "\r";
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		String source = "public final void addDefinitelyAssignedVariables(Scope scope, int initStateIndex) {\r" + 
				"/*\r" + 
				"	\r" + 
				"*/\r" + 
				"}";
		String expectedResult = "public final void addDefinitelyAssignedVariables(Scope scope, int initStateIndex) {\r" + 
				"	/*\r" + 
				"	 \r" + 
				"	 */\r" + 
				"}";
		runTest(source, expectedResult, codeFormatter, CodeFormatter.K_CLASS_BODY_DECLARATIONS, 0, false, 0, -1);
	}

	public void test319() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_new_line_in_empty_anonymous_type_declaration = false;
		preferences.insert_new_line_in_empty_type_declaration = false;
		preferences.insert_new_line_in_empty_method_body = false;
		preferences.insert_new_line_in_empty_block = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test319", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test320() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_new_line_in_empty_anonymous_type_declaration = false;
		preferences.insert_new_line_in_empty_type_declaration = false;
		preferences.insert_new_line_in_empty_method_body = false;
		preferences.insert_new_line_in_empty_block = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test320", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test321() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_new_line_in_empty_anonymous_type_declaration = false;
		preferences.insert_new_line_in_empty_type_declaration = false;
		preferences.insert_new_line_in_empty_method_body = false;
		preferences.insert_new_line_in_empty_block = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test321", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test322() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_new_line_in_empty_anonymous_type_declaration = false;
		preferences.insert_new_line_in_empty_type_declaration = false;
		preferences.insert_new_line_in_empty_method_body = false;
		preferences.insert_new_line_in_empty_block = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test322", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test323() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_new_line_in_empty_anonymous_type_declaration = false;
		preferences.insert_new_line_in_empty_type_declaration = false;
		preferences.insert_new_line_in_empty_method_body = false;
		preferences.insert_new_line_in_empty_block = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test323", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=45141
	 */
	public void test324() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test324", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=45220
	 */
	public void test325() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test325", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=45465
	 */
	public void test326() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test326", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=45508
	 */
	public void test327() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test327", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=22073
	 */
	public void test328() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test328", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=29473
	 */
	public void test329() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test329", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=27249
	 */
	public void test330() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test330", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=23709
	 */
	public void test331() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test331", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=23709
	 */
	public void test332() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test332", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=45968
	 */
	public void test333() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.number_of_empty_lines_to_preserve = 5;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test333", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46058
	 */
	public void test334() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.put_empty_statement_on_new_line = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test334", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46033
	 */
	public void test335() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test335", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46023
	 */
	public void test336() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test336", "A.java", CodeFormatter.K_STATEMENTS, 8);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46150
	 */
	public void test337() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test337", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46686
	 */
	public void test338() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test338", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46686
	 */
	public void test339() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test339", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46686
	 */
	public void test340() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test340", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46689
	 */
	public void test341() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_before_unary_operator = false;
		preferences.insert_space_after_assignment_operators = false;
		preferences.insert_space_after_binary_operator = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test341", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46690
	 */
	public void test342() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_comma_in_multiple_local_declarations = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test342", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46690
	 */
	public void test343() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_comma_in_multiple_field_declarations = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test343", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46690
	 */
	public void test344() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_comma_in_multiple_field_declarations = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test344", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=46690
	 */
	public void test345() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_comma_in_multiple_local_declarations = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test345", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44493
	 */
	public void test347() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = false;
		preferences.blank_lines_before_method = 1;
		preferences.blank_lines_before_first_class_body_declaration = 1;
		preferences.method_throws_clause_alignment = Alignment.M_INDENT_ON_COLUMN | Alignment.M_NEXT_PER_LINE_SPLIT;
		preferences.insert_new_line_in_empty_method_body = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test347", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44493
	 */
	public void test348() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = false;
		preferences.blank_lines_before_method = 1;
		preferences.blank_lines_before_first_class_body_declaration = 1;
		preferences.method_throws_clause_alignment = Alignment.M_COMPACT_SPLIT;
		preferences.insert_new_line_in_empty_method_body = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test348", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44493
	 */
	public void test349() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getJavaConventionsSettings());
		preferences.blank_lines_before_first_class_body_declaration = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test349", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44653
	 */
	public void test350() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test350", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44765
	 */
	public void test351() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test351", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44653
	 */
	public void test352() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getJavaConventionsSettings());
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test352", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44642
	 */
	public void test353() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test353", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47799
	 */
	public void test354() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, DefaultCodeFormatterConstants.FALSE);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test354", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47799
	 */
	public void test355() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_PUT_EMPTY_STATEMENT_ON_NEW_LINE, DefaultCodeFormatterConstants.TRUE);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test355", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47800
	 */
	public void test356() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BINARY_EXPRESSION_ALIGNMENT, DefaultCodeFormatterConstants.FORMATTER_COMPACT_SPLIT);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test356", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47801
	 */
	public void test357() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_PREFIX_OPERATOR, JavaCore.INSERT);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test357", "A.java", CodeFormatter.K_EXPRESSION);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47801
	 */
	public void test358() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_PREFIX_OPERATOR, JavaCore.DO_NOT_INSERT);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test358", "A.java", CodeFormatter.K_EXPRESSION);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47811
	 */
	public void test359() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test359", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47811
	 */
	public void test360() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "2");
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test360", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47811
	 */
	public void test361() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "1");
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test361", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47802
	 */
	public void test362() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.method_declaration_arguments_alignment = Alignment.M_NEXT_PER_LINE_SPLIT;
		preferences.page_width = 57;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test362", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47800
	 */
	public void test363() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test363", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47986
	 */
	public void test364() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_comma_in_for_inits = false;
		preferences.insert_space_after_comma_in_for_increments = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test364", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47986
	 */
	public void test365() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_comma_in_for_inits = false;
		preferences.insert_space_after_comma_in_for_increments = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test365", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47986
	 */
	public void test366() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_comma_in_for_inits = true;
		preferences.insert_space_before_comma_in_for_inits = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test366", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47986
	 */
	public void test367() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(DefaultCodeFormatterConstants.getDefaultSettings());
		preferences.use_tab = true;
		preferences.insert_space_after_comma_in_for_inits = true;
		preferences.insert_space_before_comma_in_for_inits = true;
		preferences.insert_space_after_comma_in_multiple_local_declarations = false;
		preferences.insert_space_before_comma_in_multiple_local_declarations = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test367", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47918
	 */
	public void test368() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "1");
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_METHOD, "0");
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(new DefaultCodeFormatterOptions(options));
		runTest(codeFormatter, "test368", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47918
	 */
	public void test369() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "0");
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_METHOD, "1");
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_FIRST_CLASS_BODY_DECLARATION, "1");
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(new DefaultCodeFormatterOptions(options));
		runTest(codeFormatter, "test369", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47918
	 */
	public void test370() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "1");
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_METHOD, "1");
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_FIRST_CLASS_BODY_DECLARATION, "1");
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(new DefaultCodeFormatterOptions(options));
		runTest(codeFormatter, "test370", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47918
	 */
	public void test371() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "1");
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_METHOD, "0");
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(new DefaultCodeFormatterOptions(options));
		runTest(codeFormatter, "test371", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47918
	 */
	public void test372() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "1");
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_METHOD, "0");
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(new DefaultCodeFormatterOptions(options));
		runTest(codeFormatter, "test372", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47918
	 */
	public void test373() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "0");
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_METHOD, "1");
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_FIRST_CLASS_BODY_DECLARATION, "1");
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(new DefaultCodeFormatterOptions(options));
		runTest(codeFormatter, "test373", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44813
	 */
	public void test374() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_CONTINUATION_INDENTATION, "1");
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER, JavaCore.INSERT);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(new DefaultCodeFormatterOptions(options));
		runTest(codeFormatter, "test374", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44813
	 */
	public void test375() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_CONTINUATION_INDENTATION, "1");
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER, JavaCore.INSERT);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(new DefaultCodeFormatterOptions(options));
		runTest(codeFormatter, "test375", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44813
	 */
	public void test376() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_CONTINUATION_INDENTATION, "1");
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER, JavaCore.DO_NOT_INSERT);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(new DefaultCodeFormatterOptions(options));
		runTest(codeFormatter, "test376", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44813
	 */
	public void test377() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_CONTINUATION_INDENTATION, "1");
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER, JavaCore.DO_NOT_INSERT);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(new DefaultCodeFormatterOptions(options));
		runTest(codeFormatter, "test377", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44813
	 */
	public void test378() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_BRACE_POSITION, DefaultCodeFormatterConstants.END_OF_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_CONTINUATION_INDENTATION, "1");
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER, JavaCore.INSERT);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(new DefaultCodeFormatterOptions(options));
		runTest(codeFormatter, "test378", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47997
	 */
	public void test379() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ARRAY_INITIALIZER_CONTINUATION_INDENTATION, "1");
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_AFTER_COMMA_IN_ARRAY_INITIALIZER, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_AFTER_OPENING_BRACE_IN_ARRAY_INITIALIZER, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_BEFORE_CLOSING_BRACE_IN_ARRAY_INITIALIZER, JavaCore.INSERT);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(new DefaultCodeFormatterOptions(options));
		runTest(codeFormatter, "test379", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47997
	 */
	public void test380() {
		Hashtable options = new Hashtable();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_NEW_LINE_IN_CONTROL_STATEMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_ANONYMOUS_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_TYPE_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_METHOD_DECLARATION_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLOCK_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_SWITCH_BRACE_POSITION, DefaultCodeFormatterConstants.NEXT_LINE);
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_FIRST_CLASS_BODY_DECLARATION, "1");
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		options.put(JavaCore.FORMATTER_TAB_SIZE, "4");
		options.put(JavaCore.FORMATTER_LINE_SPLIT, "100");
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test380", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=47997
	 */
	public void test381() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_NUMBER_OF_EMPTY_LINES_TO_PRESERVE, "0");
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_METHOD, "1");
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_FIRST_CLASS_BODY_DECLARATION, "0");
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(new DefaultCodeFormatterOptions(options));
		runTest(codeFormatter, "test381", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48131
	 */
	public void test382() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(new DefaultCodeFormatterOptions(options));
		runTest(codeFormatter, "test382", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48141
	 */
	public void test383() {
		Map options = DefaultCodeFormatterConstants.getJavaConventionsSettings();
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(new DefaultCodeFormatterOptions(options));
		runTest(codeFormatter, "test383", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48143
	 */
	public void _test384() {
		Map options = DefaultCodeFormatterConstants.getJavaConventionsSettings();
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		preferences.conditional_expression_alignment = Alignment.M_NEXT_PER_LINE_SPLIT | Alignment.M_FORCE | Alignment.M_INDENT_ON_COLUMN;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test384", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
		
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48143
	 */
	public void test385() {
		Map options = DefaultCodeFormatterConstants.getJavaConventionsSettings();
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		preferences.conditional_expression_alignment = Alignment.M_NEXT_SHIFTED_SPLIT | Alignment.M_FORCE;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test385", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48143
	 */
	public void test386() {
		Map options = DefaultCodeFormatterConstants.getJavaConventionsSettings();
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		preferences.conditional_expression_alignment = Alignment.M_ONE_PER_LINE_SPLIT | Alignment.M_FORCE;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test386", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48143
	 */
	public void _test387() {
		Map options = DefaultCodeFormatterConstants.getJavaConventionsSettings();
//		options.put(DefaultCodeFormatterConstants.FORMATTER_CONDITIONAL_EXPRESSION_ALIGNMENT, "18");
//		options.put(DefaultCodeFormatterConstants.FORMATTER_LINE_SPLIT, "40");
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		preferences.conditional_expression_alignment = Alignment.M_COMPACT_SPLIT | Alignment.M_INDENT_ON_COLUMN;
		preferences.page_width = 40;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test387", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48167
	 */
	public void test388() {
		Map options = DefaultCodeFormatterConstants.getJavaConventionsSettings();
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		preferences.use_tab = true;
		preferences.array_initializer_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		preferences.array_initializer_expressions_alignment = Alignment.M_NEXT_PER_LINE_SPLIT | Alignment.M_INDENT_ON_COLUMN;
		preferences.page_width = 40;
		preferences.insert_new_line_after_opening_brace_in_array_initializer = true;
		preferences.insert_new_line_before_closing_brace_in_array_initializer = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test388", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48167
	 */
	public void test389() {
		Map options = DefaultCodeFormatterConstants.getJavaConventionsSettings();
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		preferences.use_tab = true;
		preferences.array_initializer_continuation_indentation = 1;
		preferences.array_initializer_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		preferences.array_initializer_expressions_alignment = Alignment.M_NEXT_PER_LINE_SPLIT | Alignment.M_INDENT_ON_COLUMN | Alignment.M_FORCE;
		preferences.page_width = 40;
		preferences.insert_new_line_after_opening_brace_in_array_initializer = true;
		preferences.insert_new_line_before_closing_brace_in_array_initializer = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test389", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48167
	 */
	public void test390() {
		Map options = DefaultCodeFormatterConstants.getJavaConventionsSettings();
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		preferences.use_tab = true;
		preferences.array_initializer_continuation_indentation = 1;
		preferences.array_initializer_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		preferences.array_initializer_expressions_alignment = Alignment.M_COMPACT_FIRST_BREAK_SPLIT;
		preferences.page_width = 40;
		preferences.insert_new_line_after_opening_brace_in_array_initializer = true;
		preferences.insert_new_line_before_closing_brace_in_array_initializer = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test390", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48167
	 */
	public void test391() {
		Map options = DefaultCodeFormatterConstants.getJavaConventionsSettings();
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		preferences.use_tab = true;
		preferences.array_initializer_continuation_indentation = 3;
		preferences.array_initializer_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		preferences.array_initializer_expressions_alignment = Alignment.M_COMPACT_SPLIT | Alignment.M_INDENT_BY_ONE;
		preferences.page_width = 40;
		preferences.insert_new_line_after_opening_brace_in_array_initializer = true;
		preferences.insert_new_line_before_closing_brace_in_array_initializer = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test391", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48167
	 */
	public void test392() {
		Map options = DefaultCodeFormatterConstants.getJavaConventionsSettings();
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		preferences.use_tab = true;
		preferences.array_initializer_continuation_indentation = 3;
		preferences.array_initializer_brace_position = DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED;
		preferences.array_initializer_expressions_alignment = Alignment.M_COMPACT_SPLIT | Alignment.M_INDENT_BY_ONE;
		preferences.page_width = 40;
		preferences.insert_new_line_after_opening_brace_in_array_initializer = true;
		preferences.insert_new_line_before_closing_brace_in_array_initializer = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test392", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48167
	 */
	public void test393() {
		Map options = DefaultCodeFormatterConstants.getJavaConventionsSettings();
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		preferences.use_tab = true;
		preferences.array_initializer_continuation_indentation = 1;
		preferences.array_initializer_brace_position = DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED;
		preferences.array_initializer_expressions_alignment = Alignment.M_COMPACT_SPLIT;
		preferences.page_width = 40;
		preferences.insert_new_line_after_opening_brace_in_array_initializer = true;
		preferences.insert_new_line_before_closing_brace_in_array_initializer = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test393", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48404
	 */
	public void test394() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test394", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49318
	 */
	public void test395() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		preferences.insert_space_before_method_declaration_open_paren = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test395", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49243
	 */
	public void test396() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		preferences.insert_space_before_semicolon_in_for = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test396", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49187
	 */
	public void test397() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		preferences.blank_lines_before_package = 2;
		preferences.blank_lines_after_package = 0;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test397", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49187
	 */
	public void test398() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		preferences.blank_lines_before_package = 0;
		preferences.blank_lines_after_package = 0;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test398", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49187
	 */
	public void test399() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		preferences.blank_lines_before_package = 1;
		preferences.blank_lines_after_package = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test399", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49187
	 */
	public void test400() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_PACKAGE, "2");
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_AFTER_PACKAGE, "2");
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test400", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49361
	 */
	public void test401() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_ARRAY_INITIALIZER, JavaCore.INSERT);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test401", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}	
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49361
	 */
	public void test402() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_ARRAY_INITIALIZER, JavaCore.DO_NOT_INSERT);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test402", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49298
	 */
	public void test403() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_WITHIN_MESSAGE_SEND, JavaCore.INSERT);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test403", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49298
	 */
	public void test404() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_WITHIN_MESSAGE_SEND, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_MESSAGESEND_ARGUMENTS, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_ARGUMENTS, JavaCore.DO_NOT_INSERT);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test404", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49187
	 */
	public void test405() {
		Map options = DefaultCodeFormatterConstants.getJavaConventionsSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_BLANK_LINES_BEFORE_PACKAGE, "10");
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test405", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49298
	 */
	public void test406() {
		Map options = DefaultCodeFormatterConstants.getDefaultSettings();
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_WITHIN_MESSAGE_SEND, JavaCore.INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_MESSAGESEND_ARGUMENTS, JavaCore.DO_NOT_INSERT);
		options.put(DefaultCodeFormatterConstants.FORMATTER_INSERT_SPACE_BETWEEN_EMPTY_ARGUMENTS, JavaCore.INSERT);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test406", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49481
	 */
	public void test407() {
		Map options = DefaultCodeFormatterConstants.getJavaConventionsSettings();
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test407", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=49481
	 */
	public void test408() {
		Map options = DefaultCodeFormatterConstants.getJavaConventionsSettings();
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test408", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}		
}