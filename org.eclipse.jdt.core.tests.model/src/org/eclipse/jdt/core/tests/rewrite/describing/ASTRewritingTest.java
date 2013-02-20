/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.core.tests.model.AbstractJavaModelTests;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;

/**
  */
public class ASTRewritingTest extends AbstractJavaModelTests {


	/** @deprecated using deprecated code */
	private final static int JLS2_INTERNAL = AST.JLS2;

	/**
	 * Internal synonynm for deprecated constant AST.JSL3
	 * to alleviate deprecation warnings.
	 * @deprecated
	 */
	private static final int JLS3_INTERNAL = AST.JLS3;

	/** @deprecated using deprecated code */
	private final static int JLS4_INTERNAL = AST.JLS4;


	private static int LEVEL_2 = 0x1;
	private static int LEVEL_3 = 0x2;
	private static int LEVEL_4 = 0x4;
	private static int LEVEL_8 = 0x8;

	private static int SINCE_8 = LEVEL_8;
	private static int SINCE_4 = LEVEL_4 | SINCE_8;
	private static int SINCE_3 = LEVEL_3 | SINCE_4;
	private static int SINCE_2 = LEVEL_2 | SINCE_3;
	
	private static final String ONLY_AST_STRING = "_only";
	private static final String SINCE_AST_STRING = "_since";
	private static final String STRING_2 = "_2";
	private static final String STRING_3 = "_3";
	private static final String STRING_4 = "_4";
	private static final String STRING_8 = "_8";

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
		suite.addTest(addMethodsToSuite(ASTRewritingExpressionsTest.class));
		suite.addTest(addMethodsToSuite(ASTRewritingInsertBoundTest.class));
		suite.addTest(addMethodsToSuite(ASTRewritingMethodDeclTest.class));
		suite.addTest(addMethodsToSuite(ASTRewritingMoveCodeTest.class));
		suite.addTest(addMethodsToSuite(ASTRewritingStatementsTest.class));
		suite.addTest(addMethodsToSuite(ASTRewritingTrackingTest.class));
		suite.addTest(addMethodsToSuite(ASTRewritingJavadocTest.class));
		suite.addTest(addMethodsToSuite(ASTRewritingTypeDeclTest.class));
		suite.addTest(addMethodsToSuite(ASTRewritingGroupNodeTest.class));
		suite.addTest(addMethodsToSuite(ASTRewritingRevertTest.class));
		suite.addTest(addMethodsToSuite(SourceModifierTest.class));
		suite.addTest(ImportRewriteTest.suite());
		suite.addTest(addMethodsToSuite(LineCommentOffsetsTest.class));
		suite.addTest(addMethodsToSuite(ASTRewritingWithStatementsRecoveryTest.class));
		suite.addTest(addMethodsToSuite(ASTRewritePropertyTest.class));
		suite.addTest(addMethodsToSuite(ASTRewritingPackageDeclTest.class));
		return suite;
	}

	protected static TestSuite addMethodsToSuite(Class testClass) {
		TestSuite suite = new TestSuite(testClass.getName());
		try {
			Method[] methods = testClass.getMethods();
			Constructor cons = testClass.getConstructor(new Class[]{String.class, int.class});
			for (int i = 0, max = methods.length; i < max; i++) {
				String name = methods[i].getName();
				if (name.startsWith("test")) { //$NON-NLS-1$
					int levels = getASTLevelsForTest(name);
					if ( (levels & LEVEL_2) != 0 )
						suite.addTest((Test) cons.newInstance(new Object[]{name,  new Integer(JLS2_INTERNAL)}));
					if ( (levels & LEVEL_3) != 0 )
						suite.addTest((Test) cons.newInstance(new Object[]{name,  new Integer(JLS3_INTERNAL)}));
					if ( (levels & LEVEL_4) != 0 )
						suite.addTest((Test) cons.newInstance(new Object[]{name,  new Integer(JLS4_INTERNAL)}));
					if ( (levels & LEVEL_8) != 0 )
						suite.addTest((Test) cons.newInstance(new Object[]{name,  new Integer(AST.JLS8)}));
					// In future when adding support for new levels, make sure you update the getASTLevelsForTest() method too
					// and create a new SINCE_* constant and update the value of SINCE_8
				}
			}
		} catch (Exception e) {
			e.printStackTrace(); // In the unlikely case, can't do much
		}
		return suite;
	}

	private static int getASTLevelsForTest(String name) {
		int index = name.indexOf(ONLY_AST_STRING);
		int levels = 0;
		if (index == -1) {
			index = name.indexOf(SINCE_AST_STRING);
			if (index != -1) {
				String subString = name.substring(index, name.length());
				if (subString.indexOf(STRING_8) != -1) {
					return LEVEL_8;
				} else if (subString.indexOf(STRING_4) != -1) {
					return SINCE_4;
				} else if (subString.indexOf(STRING_3) != -1) {
					return SINCE_3;
				} else {
					return SINCE_2;
				}
			}
		} else {
			String subString = name.substring(index, name.length());
			if (subString.indexOf(STRING_2) != -1) {
				levels |= LEVEL_2;
			}
			if (subString.indexOf(STRING_3) != -1) {
				levels |= LEVEL_3;
			}
			if (subString.indexOf(STRING_4) != -1) {
				levels |= LEVEL_4;
			}
			if (subString.indexOf(STRING_8) != -1) {
				levels |= LEVEL_8;
			}
			return levels;
		}
		return SINCE_2;
	}

	protected void setUp() throws Exception {
		super.setUp();

		IJavaProject proj= createProject("P", JavaCore.VERSION_1_5);

		this.project1 = proj;
		this.sourceFolder = getPackageFragmentRoot("P", "src");
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
	protected void tearDown() throws Exception {
		deleteProject("P");
		super.tearDown();
	}

	protected CompilationUnit createAST(ICompilationUnit cu) {
		return createAST(this.apiLevel, cu, false);
	}
	protected CompilationUnit createAST(ICompilationUnit cu, boolean statementsRecovery) {
		return createAST(this.apiLevel, cu, false, statementsRecovery);
	}
	protected CompilationUnit createAST(int JLSLevel, ICompilationUnit cu, boolean statementsRecovery) {
		return createAST(JLSLevel, cu, false, statementsRecovery);
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
