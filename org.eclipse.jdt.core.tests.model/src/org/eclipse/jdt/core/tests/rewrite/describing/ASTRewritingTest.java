/*******************************************************************************
 * Copyright (c) 2000, 2023 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.rewrite.describing;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for ASTRewrite. Subclasses must have 2 constructors that forward to
 * constructors with the same signature as this class's constructors.
 *
 * Test methods can end with:
 * <ul>
 * <li>"_since_<i>n</i>", where <i>n</i> is an AST.JLS* constant value:
 *   test will run for all AST levels >= <i>n</i>
 * </li>
 * <li>"_only_<i>a</i>_<i>b</i>...", where <i>a</i>, <i>b</i>, ... are AST.JLS* constant values:
 *   test will run for all specified AST levels
 * </li>
 * </ul>
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class ASTRewritingTest extends AbstractJavaModelTests {


	/** @deprecated using deprecated code */
	private final static int JLS2_INTERNAL = AST.JLS2;

	/**
	 * Internal synonym for deprecated constant AST.JSL3
	 * to alleviate deprecation warnings.
	 * @deprecated
	 */
	private static final int JLS3_INTERNAL = AST.JLS3;

	/** @deprecated using deprecated code */
	private final static int JLS4_INTERNAL = AST.JLS4;

	/** @deprecated using deprecated code */
	private final static int JLS8_INTERNAL = AST.JLS8;

	/** @deprecated using deprecated code */
	private final static int JLS9_INTERNAL = AST.JLS9;

	/** @deprecated using deprecated code */
	private final static int JLS10_INTERNAL = AST.JLS10;

	/** @deprecated using deprecated code */
	private final static int JLS14_INTERNAL = AST.JLS14;

	/** @deprecated using deprecated code */
	private final static int JLS15_INTERNAL = AST.JLS15;

	/** @deprecated using deprecated code */
	private final static int JLS16_INTERNAL = AST.JLS16;

	/** @deprecated using deprecated code */
	private final static int JLS17_INTERNAL = AST.JLS17;

	/** @deprecated using deprecated code */
	private final static int JLS18_INTERNAL = AST.JLS18;

	/** @deprecated using deprecated code */
	private final static int JLS19_INTERNAL = AST.JLS19;

	private final static int JLS20_INTERNAL = AST.JLS20;

	private final static int[] JLS_LEVELS = { JLS2_INTERNAL, JLS3_INTERNAL, JLS4_INTERNAL, JLS8_INTERNAL, JLS9_INTERNAL, JLS10_INTERNAL, JLS14_INTERNAL, JLS15_INTERNAL, JLS16_INTERNAL, JLS17_INTERNAL, JLS18_INTERNAL, JLS19_INTERNAL, JLS20_INTERNAL};

	private static final String ONLY_AST_STRING = "_only";
	private static final String SINCE_AST_STRING = "_since";
	private static final String STRING_ = "_";

	protected int apiLevel;

	protected IJavaProject project1;
	protected IPackageFragmentRoot sourceFolder;

	/** @deprecated using deprecated code */
	public String getName() {
		String name = super.getName() + " - JLS" + this.apiLevel;
		return name;
	}

	public ASTRewritingTest(String name) {
		super(name.substring(0, name.indexOf(" - JLS")));
		name.indexOf(" - JLS");
		this.apiLevel = Integer.parseInt(name.substring(name.indexOf(" - JLS") + 6));
	}

	/**
	 * Creates an instance of a test at a particular AST level. All sub tests of ASTRewritingTest must have a constructor
	 * with the specified parameters.
	 *
	 * @param name name of the test method
	 * @param apiLevel The JLS level
	 */
	public ASTRewritingTest(String name, int apiLevel) {
		super(name);
		this.apiLevel = apiLevel;
	}

	public static Test suite() {
		TestSuite suite= new TestSuite(ASTRewritingTest.class.getName());


		  suite.addTest(ASTRewritingExpressionsTest.suite());
		  suite.addTest(ASTRewritingInsertBoundTest.suite());
		  suite.addTest(ASTRewritingMethodDeclTest.suite());
		  suite.addTest(ASTRewritingMoveCodeTest.suite());
		  suite.addTest(ASTRewritingStatementsTest.suite());
		  suite.addTest(ASTRewritingSwitchExpressionsTest.suite());
		  suite.addTest(ASTRewritingSwitchPatternTest.suite());

		  suite.addTest(ASTRewritingTrackingTest.suite());
		  suite.addTest(ASTRewritingJavadocTest.suite());
		  suite.addTest(ASTRewritingTypeAnnotationsTest.suite());
		  suite.addTest(ASTRewritingTypeDeclTest.suite());
		  suite.addTest(ASTRewritingGroupNodeTest.suite());
		  suite.addTest(ASTRewritingRevertTest.suite());
		  suite.addTest(LineCommentOffsetsTest.suite());
		  suite.addTest(ASTRewritingWithStatementsRecoveryTest.suite());
		  suite.addTest(ASTRewritePropertyTest.suite());
		  suite.addTest(ASTRewritingPackageDeclTest.suite());
		  suite.addTest(ASTRewritingLambdaExpressionTest.suite());
		  suite.addTest(ASTRewritingReferenceExpressionTest.suite());
		  suite.addTest(ASTRewritingRecordDeclarationTest.suite());
		  suite.addTest(ASTRewritingRecordPatternTest.suite());
		  suite.addTest(ASTRewritingInstanceOfPatternExpressionTest.suite());
		  suite.addTest(SourceModifierTest.suite());
		  suite.addTest(ImportRewriteTest.suite());
		  suite.addTest(ImportRewrite18Test.suite());
		  suite.addTest(ImportRewrite_RecordTest.suite());

		return suite;
	}

	/**
	 * Creates a test suite according to the rules in {@link ASTRewritingTest}.
	 *
	 * @param testClass subclass of ASTRewritingTest
	 * @return test suite that runs all tests with all supported AST levels
	 */
	protected static TestSuite createSuite(Class testClass) {
		return createSuite(testClass, -1);
	}

	/**
	 * Creates a test suite according to the rules in {@link ASTRewritingTest}.
	 *
	 * @param testClass subclass of ASTRewritingTest
	 * @param classSince smallest supported AST level for this test class, or -1 to support all levels
	 * @return test suite that runs all tests with all supported AST levels
	 */
	protected static TestSuite createSuite(Class testClass, int classSince) {
		TestSuite suite = new TestSuite(testClass.getName());
		try {
			Method[] methods = testClass.getMethods();
			Constructor cons = testClass.getConstructor(new Class[]{String.class, int.class});
			for (int i = 0, max = methods.length; i < max; i++) {
				String name = methods[i].getName();
				if (name.startsWith("test")) { //$NON-NLS-1$

					int index = name.indexOf(ONLY_AST_STRING);
					if (index != -1) {
						String suffix = name.substring(index + ONLY_AST_STRING.length() + 1);
						String[] levels = suffix.split(STRING_);
						for (int l= 0; l < levels.length; l++) {
							suite.addTest((Test) cons.newInstance(new Object[]{name,  Integer.valueOf(levels[l])}));
						}

					} else {
						int since = -1;
						index = name.indexOf(SINCE_AST_STRING);
						if (index != -1) {
							String suffix = name.substring(index + SINCE_AST_STRING.length() + 1);
							since = Integer.parseInt(suffix);
						}
						for (int j= 0; j < JLS_LEVELS.length; j++) {
							int level = JLS_LEVELS[j];
							if (level >= since && level >= classSince) {
								suite.addTest((Test) cons.newInstance(new Object[]{name, Integer.valueOf(level)}));
							}
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(); // In the unlikely case, can't do much
		}
		return suite;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		IJavaProject proj= createProject("P", JavaCore.VERSION_1_5);

		this.project1 = proj;
		this.sourceFolder = getPackageFragmentRoot("P", "src");
	}

	@SuppressWarnings("deprecation")
	protected void setUpProjectAbove14() throws Exception {
		if (this.apiLevel == AST_INTERNAL_JLS14 ) {
			this.project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_14);
			this.project1.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_14);
			this.project1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_14);
		}
		setUpProjectAbove15();
	}

	@SuppressWarnings("deprecation")
	protected void setUpProjectAbove15() throws Exception {
		if (this.apiLevel == AST_INTERNAL_JLS15 ) {
			this.project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_15);
			this.project1.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_15);
			this.project1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_15);
		}
		setUpProjectAbove16();
	}

	@SuppressWarnings("deprecation")
	protected void setUpProjectAbove16() throws Exception {
		if (this.apiLevel == AST_INTERNAL_JLS16 ) {
			this.project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_16);
			this.project1.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_16);
			this.project1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_16);
		}
		setUpProjectAbove17();
	}

	@SuppressWarnings("deprecation")
	protected void setUpProjectAbove17() throws Exception {
		if (this.apiLevel == AST_INTERNAL_JLS17 ) {
			this.project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_17);
			this.project1.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_17);
			this.project1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_17);
		}
		setUpProjectAbove18();
	}

	@SuppressWarnings("deprecation")
	protected void setUpProjectAbove18() throws Exception {
		if (this.apiLevel == AST_INTERNAL_JLS18 ) {
			this.project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_18);
			this.project1.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_18);
			this.project1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_18);
		}
		setUpProjectAbove19();
	}
	@SuppressWarnings("deprecation")
	protected void setUpProjectAbove19() throws Exception {
		if (this.apiLevel == AST_INTERNAL_JLS19 ) {
			this.project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_19);
			this.project1.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_19);
			this.project1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_19);
		}
		setUpProjectAbove20();
	}
	protected void setUpProjectAbove20() throws Exception {
		if (this.apiLevel == AST_INTERNAL_JLS20) {
			this.project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_20);
			this.project1.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_20);
			this.project1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_20);
		}
	}

	protected IJavaProject createProject(String projectName, String complianceVersion) throws CoreException {
		IJavaProject proj = createJavaProject(projectName, new String[] {"src"}, "bin");
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES, DefaultCodeFormatterConstants.TRUE);
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH, DefaultCodeFormatterConstants.TRUE);
		proj.setOption(JavaCore.COMPILER_COMPLIANCE, complianceVersion);
		proj.setOption(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
		proj.setOption(JavaCore.COMPILER_SOURCE, complianceVersion);
		proj.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, complianceVersion);
		return proj;
	}
	@Override
	protected void tearDown() throws Exception {
		deleteProject("P");
		super.tearDown();
	}

	protected CompilationUnit createAST(ICompilationUnit cu) {
		return createAST(this.apiLevel, cu, false, false);
	}
	protected CompilationUnit createAST(ICompilationUnit cu, boolean statementsRecovery) {
		return createAST(this.apiLevel, cu, false, statementsRecovery);
	}
	protected CompilationUnit createAST(ICompilationUnit cu, boolean resolveBindings, boolean statementsRecovery) {
		return createAST(this.apiLevel, cu, resolveBindings, statementsRecovery);
	}


	protected CompilationUnit createAST(int JLSLevel, ICompilationUnit cu) {
		ASTParser parser= ASTParser.newParser(JLSLevel);
		parser.setSource(cu, JLSLevel);
		parser.setResolveBindings(false);
		parser.setStatementsRecovery(false);
		return (CompilationUnit) parser.createAST(null);
	}

	protected CompilationUnit createAST(int JLSLevel, ICompilationUnit cu, boolean resolveBindings, boolean statementsRecovery) {
		ASTParser parser= ASTParser.newParser(JLSLevel);
		parser.setSource(cu);
		parser.setResolveBindings(resolveBindings);
		parser.setStatementsRecovery(statementsRecovery);
		return (CompilationUnit) parser.createAST(null);
	}

	protected String evaluateRewrite(ICompilationUnit cu, ASTRewrite rewrite) throws Exception {
		Document document1= new Document(cu.getSource());
		TextEdit res= rewrite.rewriteAST(document1, cu.getJavaProject().getOptions(true));
		res.apply(document1);
		String content1= document1.get();

		Document document2= new Document(cu.getSource());
		TextEdit res2= rewrite.rewriteAST();
		res2.apply(document2);
		String content2= document2.get();

		assertEquals(content1, content2);

		return content1;
	}


	public static void assertEqualString(String actual, String expected) {
		StringAsserts.assertEqualString(actual, expected);
	}

	public static TypeDeclaration findTypeDeclaration(CompilationUnit astRoot, String simpleTypeName) {
		return (TypeDeclaration) findAbstractTypeDeclaration(astRoot, simpleTypeName);
	}

	public static AbstractTypeDeclaration findAbstractTypeDeclaration(CompilationUnit astRoot, String simpleTypeName) {
		List types= astRoot.types();
		for (int i= 0; i < types.size(); i++) {
			AbstractTypeDeclaration elem= (AbstractTypeDeclaration) types.get(i);
			if (simpleTypeName.equals(elem.getName().getIdentifier())) {
				return elem;
			}
		}
		return null;
	}

	public static MethodDeclaration findMethodDeclaration(TypeDeclaration typeDecl, String methodName) {
		MethodDeclaration[] methods= typeDecl.getMethods();
		for (int i= 0; i < methods.length; i++) {
			if (methodName.equals(methods[i].getName().getIdentifier())) {
				return methods[i];
			}
		}
		return null;
	}

	public static MethodDeclaration findMethodDeclaration(RecordDeclaration typeDecl, String methodName) {
		MethodDeclaration[] methods= typeDecl.getMethods();
		for (int i= 0; i < methods.length; i++) {
			if (methodName.equals(methods[i].getName().getIdentifier())) {
				return methods[i];
			}
		}
		return null;
	}

	protected static SingleVariableDeclaration createNewParam(AST ast, String name) {
		SingleVariableDeclaration newParam= ast.newSingleVariableDeclaration();
		newParam.setType(ast.newPrimitiveType(PrimitiveType.FLOAT));
		newParam.setName(ast.newSimpleName(name));
		return newParam;
	}

	/** @deprecated using deprecated code */
	private static void setModifiers(BodyDeclaration bodyDeclaration, int modifiers) {
		bodyDeclaration.setModifiers(modifiers);
	}

	/** @deprecated using deprecated code */
	private static void setReturnType(MethodDeclaration methodDeclaration, Type type) {
		methodDeclaration.setReturnType(type);
	}

	protected static FieldDeclaration createNewField(AST ast, String name) {
		VariableDeclarationFragment frag= ast.newVariableDeclarationFragment();
		frag.setName(ast.newSimpleName(name));
		FieldDeclaration newFieldDecl= ast.newFieldDeclaration(frag);
		if (ast.apiLevel() == JLS2_INTERNAL) {
			setModifiers(newFieldDecl, Modifier.PRIVATE);
		} else {
			newFieldDecl.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
		}
		newFieldDecl.setType(ast.newPrimitiveType(PrimitiveType.DOUBLE));
		return newFieldDecl;
	}

	protected static MethodDeclaration createNewMethod(AST ast, String name, boolean isAbstract) {
		MethodDeclaration decl= ast.newMethodDeclaration();
		decl.setName(ast.newSimpleName(name));
		if (ast.apiLevel() == JLS2_INTERNAL) {
			setModifiers(decl, isAbstract ? (Modifier.ABSTRACT | Modifier.PRIVATE) : Modifier.PRIVATE);
			setReturnType(decl, ast.newPrimitiveType(PrimitiveType.VOID));
		} else {
			decl.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
			if (isAbstract) {
				decl.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD));
			}
			decl.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
		}
		SingleVariableDeclaration param= ast.newSingleVariableDeclaration();
		param.setName(ast.newSimpleName("str"));
		param.setType(ast.newSimpleType(ast.newSimpleName("String")));
		decl.parameters().add(param);
		decl.setBody(isAbstract ? null : ast.newBlock());
		return decl;
	}

}
