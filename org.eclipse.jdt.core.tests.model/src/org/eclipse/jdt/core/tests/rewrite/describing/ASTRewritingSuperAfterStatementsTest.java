/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
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

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

import junit.framework.Test;

public class ASTRewritingSuperAfterStatementsTest extends ASTRewritingTest{
	static {
//		TESTS_NAMES = new String[] {"test005"};
	}

	public ASTRewritingSuperAfterStatementsTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingSuperAfterStatementsTest.class, 22);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (this.apiLevel == AST.JLS22 ) {
			this.project1.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_22);
			this.project1.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_22);
			this.project1.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_22);
			this.project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		}
	}

	public void test001() throws Exception {
		AST ast = AST.newAST(AST.JLS22, true);
		// Create CompilationUnit
        CompilationUnit compilationUnit = ast.newCompilationUnit();
        PackageDeclaration packageDeclaration = ast.newPackageDeclaration();
        packageDeclaration.setName(ast.newName("test1"));
        compilationUnit.setPackage(packageDeclaration);

        // Create ClassDeclaration
        TypeDeclaration classDeclaration = ast.newTypeDeclaration();
        classDeclaration.setName(ast.newSimpleName("X"));
        classDeclaration.setInterface(false);

        // Create Constructor
        MethodDeclaration constructor = ast.newMethodDeclaration();
        constructor.setName(ast.newSimpleName("X"));
        constructor.setConstructor(true);

        // Create Constructor parameter
        SingleVariableDeclaration parameter = ast.newSingleVariableDeclaration();
        parameter.setType(ast.newPrimitiveType(PrimitiveType.INT));
        parameter.setName(ast.newSimpleName("i"));
        constructor.parameters().add(parameter);

        // Create Constructor body
        Block constructorBody = ast.newBlock();
        IfStatement ifStatement = ast.newIfStatement();
        InfixExpression condition = ast.newInfixExpression();
        condition.setLeftOperand(ast.newSimpleName("i"));
        condition.setRightOperand(ast.newNumberLiteral("0"));
        condition.setOperator(InfixExpression.Operator.LESS);
        ifStatement.setExpression(condition);

     // Create a new block for the then statement
        Block thenBlock = ast.newBlock();
        PostfixExpression postfixExpression = ast.newPostfixExpression();
        postfixExpression.setOperand(ast.newSimpleName("i"));
        postfixExpression.setOperator(PostfixExpression.Operator.INCREMENT);
        thenBlock.statements().add(ast.newExpressionStatement(postfixExpression));

        // Set then block as the then statement of the if statement
        ifStatement.setThenStatement(thenBlock);

        constructorBody.statements().add(ifStatement);
        SuperConstructorInvocation superInvocation = ast.newSuperConstructorInvocation();
        constructorBody.statements().add(superInvocation);
        constructor.setBody(constructorBody);

        // Add Constructor to Class
        classDeclaration.bodyDeclarations().add(constructor);

        // Create main method
        MethodDeclaration mainMethod = ast.newMethodDeclaration();
        mainMethod.setName(ast.newSimpleName("main"));
        mainMethod.modifiers().add(ast.newModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD));
        mainMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));

        SingleVariableDeclaration argvParameter = ast.newSingleVariableDeclaration();
        argvParameter.setType(ast.newArrayType(ast.newSimpleType(ast.newSimpleName("String"))));
        argvParameter.setName(ast.newSimpleName("argv"));
        mainMethod.parameters().add(argvParameter);

        Block mainMethodBody = ast.newBlock();
        ClassInstanceCreation instanceCreation = ast.newClassInstanceCreation();
        instanceCreation.setType(ast.newSimpleType(ast.newSimpleName("X")));
        instanceCreation.arguments().add(ast.newNumberLiteral("0"));

        mainMethodBody.statements().add(ast.newExpressionStatement(instanceCreation));
        mainMethod.setBody(mainMethodBody);

        // Add main method to class
        classDeclaration.bodyDeclarations().add(mainMethod);

        // Add class to compilation unit
        compilationUnit.types().add(classDeclaration);

		StringBuilder buf = new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class X {\n");
		buf.append("  X(  int i){\n");
		buf.append("    if (i < 0) {\n");
		buf.append("      i++;\n");
		buf.append("    }\n");
		buf.append("    super();\n");
		buf.append("  }\n");
		buf.append("  public void main(  String[] argv){\n");
		buf.append("    new X(0);\n");
		buf.append("  }\n");
		buf.append("}\n");

		assertEqualString(compilationUnit.toString(), buf.toString());
	}
	//modify one statement with super
	public void test002() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf = new StringBuilder();
		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class X {\n");
		buf.append("  void hello() {\n");
		buf.append("    System.out.println(\"Hello\");\n");
		buf.append("  }\n");
		buf.append("  void hi() {\n");
		buf.append("    System.out.println(\"Hi\");\n");
		buf.append("  }\n");
		buf.append("  class Inner {\n");
		buf.append("    Inner() {\n");
		buf.append("      hello();\n");
		buf.append("      hi();\n");
		buf.append("    }\n");
		buf.append("  }\n");
		buf.append("  public static void main(String[] args) {\n");
		buf.append("    new X().new Inner();\n");
		buf.append("  }\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
        CompilationUnit astRoot= createAST(this.apiLevel, cu);
        ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
        AST ast= astRoot.getAST();

        assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		TypeDeclaration[] typeClassDecleration = type.getTypes();
		MethodDeclaration[] InerMethodDecleration = typeClassDecleration[0].getMethods();
		List<Statement> blockStatements = InerMethodDecleration[0].getBody().statements();
		assertEquals("Incorrect number of statements", 2, blockStatements.size());
		{
			//replace hi() with super()
			ExpressionStatement	hiStatement = (ExpressionStatement) blockStatements.get(1);
			SuperConstructorInvocation superInvocation = ast.newSuperConstructorInvocation();

			rewrite.replace(hiStatement, superInvocation, null);
		}
		String preview = evaluateRewrite(cu, rewrite);
		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class X {\n");
		buf.append("  void hello() {\n");
		buf.append("    System.out.println(\"Hello\");\n");
		buf.append("  }\n");
		buf.append("  void hi() {\n");
		buf.append("    System.out.println(\"Hi\");\n");
		buf.append("  }\n");
		buf.append("  class Inner {\n");
		buf.append("    Inner() {\n");
		buf.append("      hello();\n");
		buf.append("      super();\n");
		buf.append("    }\n");
		buf.append("  }\n");
		buf.append("  public static void main(String[] args) {\n");
		buf.append("    new X().new Inner();\n");
		buf.append("  }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}
	//replace super() with another statement
	public void test003() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf = new StringBuilder();
		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class X {\n");
		buf.append("  void hello() {\n");
		buf.append("    System.out.println(\"Hello\");\n");
		buf.append("  }\n");
		buf.append("  void hi() {\n");
		buf.append("    System.out.println(\"Hi\");\n");
		buf.append("  }\n");
		buf.append("  class Inner {\n");
		buf.append("    Inner() {\n");
		buf.append("      hello();\n");
		buf.append("      super();\n");
		buf.append("    }\n");
		buf.append("  }\n");
		buf.append("  public static void main(String[] args) {\n");
		buf.append("    new X().new Inner();\n");
		buf.append("  }\n");
		buf.append("}\n");

		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);
        CompilationUnit astRoot= createAST(this.apiLevel, cu);
        ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
        AST ast= astRoot.getAST();

        assertTrue("Parse errors", (astRoot.getFlags() & ASTNode.MALFORMED) == 0);
		TypeDeclaration type= findTypeDeclaration(astRoot, "X");
		TypeDeclaration[] typeClassDecleration = type.getTypes();
		MethodDeclaration[] InerMethodDecleration = typeClassDecleration[0].getMethods();
		List<Statement> blockStatements = InerMethodDecleration[0].getBody().statements();
		assertEquals("Incorrect number of statements", 2, blockStatements.size());
		{
			//replace super() with hi()
			SuperConstructorInvocation superStatement = (SuperConstructorInvocation) blockStatements.get(1);

			MethodInvocation hiMethodInvocation = ast.newMethodInvocation();
			hiMethodInvocation.setName(ast.newSimpleName("hi"));
	        ExpressionStatement hiExpressionStatement = ast.newExpressionStatement(hiMethodInvocation);

			rewrite.replace(superStatement, hiExpressionStatement, null);
		}
		String preview = evaluateRewrite(cu, rewrite);
		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("class X {\n");
		buf.append("  void hello() {\n");
		buf.append("    System.out.println(\"Hello\");\n");
		buf.append("  }\n");
		buf.append("  void hi() {\n");
		buf.append("    System.out.println(\"Hi\");\n");
		buf.append("  }\n");
		buf.append("  class Inner {\n");
		buf.append("    Inner() {\n");
		buf.append("      hello();\n");
		buf.append("      hi();\n");
		buf.append("    }\n");
		buf.append("  }\n");
		buf.append("  public static void main(String[] args) {\n");
		buf.append("    new X().new Inner();\n");
		buf.append("  }\n");
		buf.append("}\n");

		assertEqualString(preview, buf.toString());

	}
}
