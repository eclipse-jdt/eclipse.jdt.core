package org.eclipse.jdt.code.tests.formatter;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Hashtable;

import junit.framework.Test;

import org.eclipse.core.resources.IWorkspaceDescription;
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
	
	public static Test suite() {
		junit.framework.TestSuite suite = new Suite(
				FormatterRegressionTests.class.getName());

		if (true) {
			Class c = FormatterRegressionTests.class;
			Method[] methods = c.getMethods();
			for (int i = 0, max = methods.length; i < max; i++) {
				if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
					suite.addTest(
						new FormatterRegressionTests(methods[i].getName()));
				}
			}
		} else {
			suite.addTest(new FormatterRegressionTests("test159"));  //$NON-NLS-1$
		}
		return suite;
	}

	public FormatterRegressionTests(String name) {
		super(name);
	}
	
	/**
	 * Returns the OS path to the directory that contains this plugin.
	 */
	protected String getPluginDirectoryPath() {
		CodeSource javaCoreCodeSource = JavaCore.class.getProtectionDomain().getCodeSource();
		if (javaCoreCodeSource != null) {
			URL javaCoreUrl = javaCoreCodeSource.getLocation();
			String javaCorePath = javaCoreUrl.getFile();
			int index = javaCorePath.indexOf(JavaCore.PLUGIN_ID);
			if (index != -1) {
				String pluginsPath = javaCorePath.substring(0, index);
				File pluginsFile = new File(pluginsPath);
				String[] list = pluginsFile.list(new FilenameFilter() {
					public boolean accept(File dir, String name) {
						return name.startsWith( "org.eclipse.jdt.core.tests.model");
					}
				});
				if (list != null && list.length > 0) {
					return pluginsPath + list[0];
				}
			}
		}
		return null;
	}

	public String getSourceWorkspacePath() {
		return getPluginDirectoryPath() +  java.io.File.separator + "workspace";
	}
	
	private String runFormatter(DefaultCodeFormatter codeFormatter, String source, int kind, int indentationLevel, int offset, int length) {
		long time = System.currentTimeMillis();
		TextEdit edit = codeFormatter.format(kind, source, offset, length, indentationLevel, null);//$NON-NLS-1$
		if (edit == null) return null;
		String result = org.eclipse.jdt.internal.core.Util.editedString(source, edit);

		if (length == source.length()) {
			time = System.currentTimeMillis();
			edit = codeFormatter.format(kind, result, 0, result.length(), indentationLevel, null);//$NON-NLS-1$
			if (edit == null) return null;
			assertEquals("Shoult not have edits", 0, edit.getChildren().length);
			final String result2 = org.eclipse.jdt.internal.core.Util.editedString(result, edit);
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
			assertTrue(checkNull);
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
				System.out.println(Util.displayString(expectedContents, 2));
				assertTrue(false);
			}
		}
	}

	private String[] createArrayOfString(String s) {
		String delim = null;
		int indexOfBackslashN = s.indexOf('\n');
		int indexOfBackslashR = s.indexOf('\r');
		if (indexOfBackslashN != -1) {
			if (indexOfBackslashR != -1) {
				delim = "\r\n";
			} else {
				delim = "\n";
			}
		} else if (indexOfBackslashR != -1) {
			delim = "\r";
		} else {
			return new String[] {s};
		}
		int start = 0;
		ArrayList arrayList = new ArrayList();
		int index = s.indexOf(delim, start);

		while (index != -1) {
			arrayList.add(s.substring(start, index));
			start = index + delim.length();
			index = s.indexOf(delim, start);
		}
		if (s.endsWith(delim)) {
			arrayList.add("");
		}
		return (String[]) arrayList.toArray(new String[arrayList.size()]);
	}

	private void runTest(String packageName, String compilationUnitName) {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, packageName, compilationUnitName, CodeFormatter.K_COMPILATION_UNIT, 0);
	}
	
	private void runTest(DefaultCodeFormatter codeFormatter, String packageName, String compilationUnitName) {
		runTest(codeFormatter, packageName, compilationUnitName, CodeFormatter.K_COMPILATION_UNIT, 0);
	}

	private void runTest(String packageName, String compilationUnitName, int kind) {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
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
		DefaultCodeFormatterOptions formatPrefs = DefaultCodeFormatterOptions.getDefault();
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
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
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
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.keep_simple_if_on_one_line = true;
		preferences.keep_then_statement_on_same_line = true;
		preferences.format_guardian_clause_on_one_line = true;
		preferences.use_tab = true;
		preferences.compact_else_if = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter,"test029", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test030() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
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
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.insert_new_line_in_empty_type_declaration = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test041", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test042() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.insert_new_line_in_empty_type_declaration = false;
		preferences.insert_space_before_block_open_brace = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test042", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test043() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
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
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.insert_space_after_assignment_operators = false;
		preferences.insert_space_before_assignment_operators = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test046", "A.java", CodeFormatter.K_EXPRESSION);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test047() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.insert_space_after_assignment_operators = true;
		preferences.insert_space_before_assignment_operators = true;
		preferences.preserve_user_linebreaks = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test047", "A.java", CodeFormatter.K_STATEMENTS, 2);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test048() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.insert_space_after_assignment_operators = true;
		preferences.insert_space_before_assignment_operators = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test048", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test049() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.insert_space_after_assignment_operators = true;
		preferences.insert_space_before_assignment_operators = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test049", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test050() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.insert_space_after_binary_operator = false;
		preferences.insert_space_before_unary_operator = false;
		preferences.insert_space_after_unary_operator = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test050", "A.java", CodeFormatter.K_EXPRESSION);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test051() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
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
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.keep_simple_if_on_one_line = true;
		preferences.keep_then_statement_on_same_line = true;
		preferences.keep_else_statement_on_same_line = true;
		preferences.format_guardian_clause_on_one_line = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test056", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test057() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.type_member_alignment = Alignment.M_MULTICOLUMN;
		preferences.array_initializer_expressions_alignment = Alignment.M_COMPACT_SPLIT;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test057", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test058() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.type_member_alignment = Alignment.M_MULTICOLUMN;
		preferences.array_initializer_expressions_alignment = Alignment.M_COMPACT_SPLIT;
		preferences.use_tab = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test058", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test059() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.type_member_alignment = Alignment.M_NO_ALIGNMENT;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test059", "Parser.java");//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test060() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.type_member_alignment = Alignment.M_NO_ALIGNMENT;
		preferences.use_tab = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		long time = System.currentTimeMillis();
		runTest(codeFormatter, "test060", "Parser.java");//$NON-NLS-1$ //$NON-NLS-2$
	}		

	public void test061() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.type_member_alignment = Alignment.M_NO_ALIGNMENT;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test061", "Parser.java");//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test062() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
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
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.allocation_expression_arguments_alignment = Alignment.M_NEXT_PER_LINE_SPLIT;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test068", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 3327
	public void test069() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.blank_lines_before_method = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test069", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// 5691
	public void test070() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test070", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test071() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.indent_body_declarations_compare_to_type_header = false;
		preferences.type_declaration_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		preferences.type_member_alignment = Alignment.M_MULTICOLUMN;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test071", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// bug 7224
	public void test072() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.number_of_empty_lines_to_preserve = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test072", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 7439
	public void test073() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.keep_simple_if_on_one_line = true;
		preferences.keep_then_statement_on_same_line = true;
		preferences.format_guardian_clause_on_one_line = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test073", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 12321
	public void test074() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
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
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.method_declaration_arguments_alignment = Alignment.M_INDENT_ON_COLUMN | Alignment.M_NEXT_PER_LINE_SPLIT;
		preferences.page_width = 57;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test075", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 16231
	public void test076() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.array_initializer_expressions_alignment = Alignment.M_ONE_PER_LINE_SPLIT;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test076", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 16233
	public void test077() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.method_declaration_arguments_alignment = Alignment.M_NEXT_PER_LINE_SPLIT;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test077", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 17349
	public void test078() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.type_declaration_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		preferences.block_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test078", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 19811
	public void test079() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test079", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 19811
	public void test080() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.method_declaration_arguments_alignment = Alignment.M_COMPACT_SPLIT;
		preferences.continuation_indentation = 2;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test080", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 19811
	public void test081() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.array_initializer_expressions_alignment = Alignment.M_NEXT_PER_LINE_SPLIT;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test081", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 19999
	public void test082() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.number_of_empty_lines_to_preserve = 2;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test082", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// bug 20721
	public void test083() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test083", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 21943
	public void test084() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
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
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
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
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
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
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.keep_simple_if_on_one_line = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test087", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 23144
	public void test088() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.keep_simple_if_on_one_line = false;
		preferences.format_guardian_clause_on_one_line = false;
		preferences.keep_then_statement_on_same_line = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test088", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// bug 24200
	public void test089() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.insert_space_after_open_paren_in_parenthesized_expression = true;
		preferences.insert_space_before_closing_paren_in_parenthesized_expression = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test089", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 24200
	public void test090() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.insert_space_between_brackets_in_array_reference = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test090", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 25559
	public void test091() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.insert_space_after_assignment_operators = false;
		preferences.insert_space_before_assignment_operators = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test091", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 25559
	public void test092() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.insert_space_after_binary_operator = false;
		preferences.insert_space_before_binary_operator = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test092", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 25559
	public void test093() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.insert_space_after_closing_paren_in_cast = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test093", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 25559
	public void test094() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.insert_space_after_assignment_operators = false;
		preferences.insert_space_before_assignment_operators = false;
		preferences.insert_space_after_comma_in_messagesend_arguments = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test094", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// bug 27196
	public void test095() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.block_brace_position = DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED;
		preferences.indent_block_statements = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test095", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 28098
	public void test096() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test096", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 34897
	public void test097() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.insert_space_within_message_send = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test097", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 35173
	public void test098() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.anonymous_type_declaration_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		preferences.method_declaration_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test098", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 35433
	public void test099() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.insert_space_before_method_declaration_open_paren = true;
		preferences.insert_space_before_for_paren = true;
		preferences.insert_space_after_semicolon_in_for = false;
		preferences.put_empty_statement_on_new_line = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test099", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test100() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
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
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test101", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 37057
	public void test102() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.number_of_empty_lines_to_preserve = 1;
		preferences.line_delimiter = "\n";//$NON-NLS-1$
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test102", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 37106
	public void test103() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test103", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 37657
	public void test104() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.insert_space_in_if_condition = true;
		preferences.block_brace_position = DefaultCodeFormatterConstants.NEXT_LINE_SHIFTED;
		preferences.insert_new_line_in_control_statements = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test104", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 38151
	public void test105() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.method_declaration_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		preferences.type_declaration_brace_position = DefaultCodeFormatterConstants.NEXT_LINE;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test105", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 39603
	public void test106() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test106", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// bug 39607
	public void test107() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.keep_then_statement_on_same_line = false;
		preferences.keep_simple_if_on_one_line = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test107", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// bug 40777
	public void test108() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test108", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test109() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.blank_lines_before_package = 2;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test109", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test110() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.blank_lines_before_package = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test110", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test111() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.blank_lines_after_package = 1;
		preferences.blank_lines_before_new_chunk = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test111", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test112() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.blank_lines_after_package = 1;
		preferences.blank_lines_before_new_chunk = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test112", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test113() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.message_send_arguments_alignment = Alignment.M_INDENT_ON_COLUMN | Alignment.M_NEXT_PER_LINE_SPLIT;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test113", "A.java");//$NON-NLS-1$ //$NON-NLS-2
	}

	// bug 14659
	public void test114() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = false;
		preferences.method_declaration_arguments_alignment = Alignment.M_INDENT_ON_COLUMN | Alignment.M_NEXT_PER_LINE_SPLIT;
		preferences.page_width = 57;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test114", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 14659
	public void test115() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.method_declaration_arguments_alignment = Alignment.M_INDENT_ON_COLUMN | Alignment.M_NEXT_PER_LINE_SPLIT;
		preferences.page_width = 57;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test115", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// bug 14659
	public void test116() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.method_declaration_arguments_alignment = Alignment.M_INDENT_ON_COLUMN | Alignment.M_NEXT_PER_LINE_SPLIT;
		preferences.page_width = 57;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test116", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// JDT/UI tests
	public void test117() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test117", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// JDT/UI tests
	public void test118() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test118", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// JDT/UI tests
	public void test119() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test119", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// JDT/UI tests
	public void test120() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test120", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}

	// JDT/UI tests
	public void test121() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test121", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// probing statements
	public void test122() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test122", "A.java", CodeFormatter.K_UNKNOWN);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// probing compilation unit
	public void test123() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test123", "A.java", CodeFormatter.K_UNKNOWN);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	// probing class body declarations
	public void test124() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.method_declaration_arguments_alignment = Alignment.M_INDENT_ON_COLUMN | Alignment.M_NEXT_PER_LINE_SPLIT;
		preferences.page_width = 57;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test124", "A.java", CodeFormatter.K_UNKNOWN);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// probing expression
	public void test125() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.insert_space_after_binary_operator = false;
		preferences.insert_space_before_unary_operator = false;
		preferences.insert_space_after_unary_operator = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test125", "A.java", CodeFormatter.K_UNKNOWN);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// probing unrecognized source
	public void test126() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test126", "A.java", CodeFormatter.K_UNKNOWN, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	// probing unrecognized source
	public void test127() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test127", "A.java", CodeFormatter.K_UNKNOWN);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	// probing unrecognized source
	public void test128() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test128", "A.java", CodeFormatter.K_UNKNOWN, true);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test129() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test129", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test130() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test130", "A.java", CodeFormatter.K_COMPILATION_UNIT, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test131() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test131", "A.java", CodeFormatter.K_COMPILATION_UNIT, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test132() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test132", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test133() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test133", "A.java", CodeFormatter.K_COMPILATION_UNIT, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test134() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test134", "A.java", CodeFormatter.K_COMPILATION_UNIT, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test135() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.INSERT);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test135", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test136() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test136", "A.java", CodeFormatter.K_COMPILATION_UNIT, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test137() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test137", "A.java", CodeFormatter.K_COMPILATION_UNIT, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test138() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test138", "A.java", CodeFormatter.K_STATEMENTS, 2, true, 8, 37);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test139() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test139", "A.java", CodeFormatter.K_STATEMENTS, 0, true, 0, 5);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test140() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test140", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test141() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = false;
		preferences.indent_switchstatements_compare_to_cases = false;
		preferences.indent_switchstatements_compare_to_switch = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test141", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test142() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test142", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, 1);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test143() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test143", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, 1);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test144() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test144", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test145() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test145", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test146() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test146", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test147() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.insert_space_before_assignment_operators = false;
		preferences.number_of_empty_lines_to_preserve = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test147", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test148() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test148", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test149() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test149", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test150() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test150", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test151() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test151", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test152() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test152", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test153() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.type_member_alignment = Alignment.M_MULTICOLUMN;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test153", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test154() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test154", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}	
	
	public void test155() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getSunSetttings();
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test155", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=44036
	public void test156() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getSunSetttings();
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test156", "A.java", CodeFormatter.K_STATEMENTS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test157() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test157", "A.java", CodeFormatter.K_STATEMENTS, 0, true, 11, 7);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test158() {
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
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
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		preferences.insert_new_line_in_control_statements = false;
		preferences.compact_else_if = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test160", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44481
	 */
	public void test161() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		preferences.insert_new_line_in_control_statements = false;
		preferences.compact_else_if = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test161", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44481
	 */
	public void test162() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		preferences.insert_new_line_in_control_statements = true;
		preferences.compact_else_if = false;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test162", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44481
	 */
	public void test163() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		preferences.insert_new_line_in_control_statements = true;
		preferences.compact_else_if = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test163", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS);//$NON-NLS-1$ //$NON-NLS-2$
	}	

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44493
	 * TODO fix me
	 */
	public void test164() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test164", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test165() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test165", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44546
	 */
	public void test166() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test166", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44503
	 */
	public void test167() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test167", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44503
	 */
	public void test169() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test169", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44503
	 */
	public void test170() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test170", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44576
	 */
	public void test171() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
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
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
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
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
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
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
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
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
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
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
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
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
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
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
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
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
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
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
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
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test181", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44651
	 */
	public void test182() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		preferences.use_tab = true;
		preferences.number_of_empty_lines_to_preserve = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test182", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44653
	 */
	public void _test183() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		preferences.use_tab = true;
		preferences.number_of_empty_lines_to_preserve = 1;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test183", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44653
	 */
	public void _test184() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test184", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44653
	 */
	public void _test185() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test185", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 */
	public void _test186() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test186", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44839
	 */
	public void test187() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test187", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44839
	 */
	public void test188() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test188", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44839
	 */
	public void test189() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions();
		preferences.use_tab = true;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test189", "A.java", CodeFormatter.K_COMPILATION_UNIT);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 */
	public void test190() {
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(JavaCore.getOptions());
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
		DefaultCodeFormatterOptions preferences = DefaultCodeFormatterOptions.getDefault();
		preferences.use_tab = true;
		preferences.type_member_alignment = Alignment.M_MULTICOLUMN;
		preferences.array_initializer_expressions_alignment = Alignment.M_COMPACT_SPLIT;
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test192", "A.java");//$NON-NLS-1$ //$NON-NLS-2$
	}	

	public void test193() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.DO_NOT_INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.DO_NOT_INSERT);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test193", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test194() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.INSERT);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test194", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test195() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.DO_NOT_INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.DO_NOT_INSERT);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test195", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test196() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test196", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test197() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.DO_NOT_INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.DO_NOT_INSERT);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test197", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test198() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.DO_NOT_INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.DO_NOT_INSERT);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test198", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test199() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test199", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test201() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.DO_NOT_INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.DO_NOT_INSERT);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test201", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * TODO Fix multi local declaration alignment
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44909
	 */
	public void _test202() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.DO_NOT_INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.DO_NOT_INSERT);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test202", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * TODO Fix multi local declaration alignment
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=44909
	 */
	public void _test203() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test203", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test204() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test204", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test205() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(JavaCore.FORMATTER_CLEAR_BLANK_LINES, JavaCore.PRESERVE_ONE);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test205", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test206() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test206", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test207() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.DO_NOT_INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.DO_NOT_INSERT);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test207", "A.java", CodeFormatter.K_STATEMENTS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test208() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test208", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test209() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.DO_NOT_INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.DO_NOT_INSERT);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(JavaCore.FORMATTER_CLEAR_BLANK_LINES, JavaCore.PRESERVE_ONE);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test209", "A.java", CodeFormatter.K_CLASS_BODY_DECLARATIONS, true);//$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void test210() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(JavaCore.FORMATTER_CLEAR_BLANK_LINES, JavaCore.PRESERVE_ONE);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test210", "A.java", CodeFormatter.K_COMPILATION_UNIT, true);//$NON-NLS-1$ //$NON-NLS-2$
	}

	public void test211() {
		Hashtable options = new Hashtable();
		options.put(JavaCore.FORMATTER_NEWLINE_CONTROL, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_NEWLINE_OPENING_BRACE, JavaCore.INSERT);
		options.put(JavaCore.FORMATTER_TAB_CHAR, JavaCore.TAB);
		options.put(JavaCore.FORMATTER_CLEAR_BLANK_LINES, JavaCore.PRESERVE_ONE);
		DefaultCodeFormatterOptions preferences = new DefaultCodeFormatterOptions(options);
		DefaultCodeFormatter codeFormatter = new DefaultCodeFormatter(preferences);
		runTest(codeFormatter, "test211", "A.java", CodeFormatter.K_COMPILATION_UNIT, 1);//$NON-NLS-1$ //$NON-NLS-2$
	}	
	
}
