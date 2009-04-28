/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

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
	private static final int AST_INTERNAL_JLS2 = AST.JLS2;

	protected IJavaProject project1;
	protected IPackageFragmentRoot sourceFolder;

	public static Test suite() {
		TestSuite suite= new TestSuite(ASTRewritingTest.class.getName());
		suite.addTest(ASTRewritingExpressionsTest.allTests());
		suite.addTest(ASTRewritingInsertBoundTest.allTests());
		suite.addTest(ASTRewritingMethodDeclTest.allTests());
		suite.addTest(ASTRewritingMoveCodeTest.allTests());
		suite.addTest(ASTRewritingStatementsTest.allTests());
		suite.addTest(ASTRewritingTrackingTest.allTests());
		suite.addTest(ASTRewritingJavadocTest.allTests());
		suite.addTest(ASTRewritingTypeDeclTest.allTests());
		suite.addTest(ASTRewritingGroupNodeTest.allTests());
		suite.addTest(ASTRewritingRevertTest.allTests());
		suite.addTest(SourceModifierTest.allTests());
		suite.addTest(ImportRewriteTest.allTests());
		suite.addTest(LineCommentOffsetsTest.allTests());
		suite.addTest(ASTRewritingWithStatementsRecoveryTest.allTests());
		return suite;
	}


	public ASTRewritingTest(String name) {
		super(name);
	}

	public void setUpSuite() throws Exception {
		super.setUpSuite();
	}

	public void tearDownSuite() throws Exception {
		super.tearDownSuite();
	}

	protected void setUp() throws Exception {
		super.setUp();

		IJavaProject proj= createJavaProject("P", new String[] {"src"}, "bin");
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, JavaCore.SPACE);
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, "4");
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_CASES, DefaultCodeFormatterConstants.TRUE);
		proj.setOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_SWITCHSTATEMENTS_COMPARE_TO_SWITCH, DefaultCodeFormatterConstants.TRUE);
		proj.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		proj.setOption(JavaCore.COMPILER_PB_ASSERT_IDENTIFIER, JavaCore.ERROR);
		proj.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		proj.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);

		this.project1 = proj;
		this.sourceFolder = getPackageFragmentRoot("P", "src");

		waitUntilIndexesReady();
	}

	protected void tearDown() throws Exception {
		deleteProject("P");
		super.tearDown();
	}

	protected CompilationUnit createAST(ICompilationUnit cu) {
		ASTParser parser= ASTParser.newParser(AST_INTERNAL_JLS2);
		parser.setSource(cu);
		parser.setResolveBindings(false);
		return (CompilationUnit) parser.createAST(null);
	}

	protected CompilationUnit createAST3(ICompilationUnit cu) {
		return createAST3(cu, false);
	}
	
	protected CompilationUnit createAST3(ICompilationUnit cu, boolean statementsRecovery) {
		ASTParser parser= ASTParser.newParser(AST.JLS3);
		parser.setSource(cu);
		parser.setResolveBindings(false);
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

	public static SingleVariableDeclaration createNewParam(AST ast, String name) {
		SingleVariableDeclaration newParam= ast.newSingleVariableDeclaration();
		newParam.setType(ast.newPrimitiveType(PrimitiveType.FLOAT));
		newParam.setName(ast.newSimpleName(name));
		return newParam;
	}

	/** @deprecated using deprecated code */
	private void setModifiers(BodyDeclaration bodyDeclaration, int modifiers) {
		bodyDeclaration.setModifiers(modifiers);
	}

	/** @deprecated using deprecated code */
	private void setReturnType(MethodDeclaration methodDeclaration, Type type) {
		methodDeclaration.setReturnType(type);
	}

	protected FieldDeclaration createNewField(AST ast, String name) {
		VariableDeclarationFragment frag= ast.newVariableDeclarationFragment();
		frag.setName(ast.newSimpleName(name));
		FieldDeclaration newFieldDecl= ast.newFieldDeclaration(frag);
		if (ast.apiLevel() == AST_INTERNAL_JLS2) {
			setModifiers(newFieldDecl, Modifier.PRIVATE);
		} else {
			newFieldDecl.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD));
		}
		newFieldDecl.setType(ast.newPrimitiveType(PrimitiveType.DOUBLE));
		return newFieldDecl;
	}

	protected MethodDeclaration createNewMethod(AST ast, String name, boolean isAbstract) {
		MethodDeclaration decl= ast.newMethodDeclaration();
		decl.setName(ast.newSimpleName(name));
		if (ast.apiLevel() == AST_INTERNAL_JLS2) {
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
