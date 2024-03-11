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
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

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
}
