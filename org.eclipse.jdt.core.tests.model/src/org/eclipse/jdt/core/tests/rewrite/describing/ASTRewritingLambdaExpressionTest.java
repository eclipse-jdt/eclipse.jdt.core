/*******************************************************************************
 * Copyright (c) 2013, 2015 IBM Corporation and others.
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

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

@SuppressWarnings("rawtypes")
public class ASTRewritingLambdaExpressionTest extends ASTRewritingTest {

	public ASTRewritingLambdaExpressionTest(String name) {
		super(name);
	}

	public ASTRewritingLambdaExpressionTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritingLambdaExpressionTest.class);
	}

	public void testLambdaExpressions_since_8() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("	int foo(int x);\n");
		buf.append("}\n");
		buf.append("interface J {\n");
		buf.append("	int foo();\n");
		buf.append("}\n");
		buf.append("public class X {\n");
		buf.append(" I i =  vlambda -> {return 200;};\n");
		buf.append(" J j =  () -> 1729;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		TypeDeclaration typedeclaration= findTypeDeclaration(astRoot, "I");

		{ // change return type
			MethodDeclaration methodDecl= findMethodDeclaration(typedeclaration, "foo");
			assertTrue("null return type: foo", methodDecl.getReturnType2() != null);

			Type returnType= methodDecl.getReturnType2();
			Type newReturnType= astRoot.getAST().newPrimitiveType(PrimitiveType.FLOAT);
			rewrite.replace(returnType, newReturnType, null);
		}
		{ // add a parameter to the singleton function in the interface.
			MethodDeclaration methodDecl= findMethodDeclaration(typedeclaration, "foo");
			List parameters= methodDecl.parameters();
			assertTrue("must be 1 parameter", parameters.size() == 1);

			SingleVariableDeclaration decl= ast.newSingleVariableDeclaration();
			decl.setType(ast.newPrimitiveType(PrimitiveType.INT));
			decl.setName(ast.newSimpleName("y"));
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertLast(decl, null);
		}

		{ // insert a parameter
			typedeclaration= findTypeDeclaration(astRoot, "J");
			MethodDeclaration methodDecl = findMethodDeclaration(typedeclaration, "foo");
			List parameters = methodDecl.parameters();
			assertTrue("must be 0 parameters", parameters.size() == 0);

			SingleVariableDeclaration newParam= ast.newSingleVariableDeclaration();
			newParam.setName(ast.newSimpleName("x"));
			newParam.setType(ast.newPrimitiveType(PrimitiveType.INT));
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).insertFirst(newParam, null);
		}

		typedeclaration= findTypeDeclaration(astRoot, "X");
		FieldDeclaration fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
		Expression expression = fragment.getInitializer();
		assertTrue(expression instanceof LambdaExpression);
		LambdaExpression lambdaExpression = (LambdaExpression)expression;

		{// add a parameter to the lambda expression
			List parameters= lambdaExpression.parameters();
			assertTrue("must be 1 parameter", parameters.size() == 1);
			ASTNode firstParam= (ASTNode) parameters.get(0);
			VariableDeclarationFragment newParam= ast.newVariableDeclarationFragment();
			newParam.setName(ast.newSimpleName("wlambda"));
			rewrite.getListRewrite(lambdaExpression, LambdaExpression.PARAMETERS_PROPERTY).insertAfter(newParam, firstParam, null);
		}

		{// replace the block body with a float literal expression body.
			ASTNode body = lambdaExpression.getBody();
			ASTNode newBody = ast.newNumberLiteral("3.14");
			rewrite.replace(body, newBody, null);
		}

		fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(1);
		fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
		expression = fragment.getInitializer();
		assertTrue(expression instanceof LambdaExpression);
		lambdaExpression = (LambdaExpression)expression;

		{// add a parameter to the lambda expression - border case - empty list initially
			List parameters= lambdaExpression.parameters();
			assertTrue("must be 0 parameter", parameters.size() == 0);
			VariableDeclarationFragment newParam= ast.newVariableDeclarationFragment();
			newParam.setName(ast.newSimpleName("vlambda"));
			rewrite.getListRewrite(lambdaExpression, LambdaExpression.PARAMETERS_PROPERTY).insertFirst(newParam, null);
		}
		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("	float foo(int x, int y);\n");
		buf.append("}\n");
		buf.append("interface J {\n");
		buf.append("	int foo(int x);\n");
		buf.append("}\n");
		buf.append("public class X {\n");
		buf.append(" I i =  (vlambda, wlambda) -> 3.14;\n");
		buf.append(" J j =  (vlambda) -> 1729;\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}

	public void testLambdaExpressions_Parentheses_since_8() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("	int foo(int x);\n");
		buf.append("}\n");
		buf.append("interface J {\n");
		buf.append("	int foo();\n");
		buf.append("}\n");
		buf.append("interface K {\n");
		buf.append("	int foo(int x, int y);\n");
		buf.append("}\n");
		buf.append("interface L {\n");
		buf.append("	int foo(int x);\n");
		buf.append("}\n");
		buf.append("public class X {\n");
		buf.append(" I i =  vlambda -> 22121887;\n");
		buf.append(" I idash =  (vlambda) -> 1729;\n");
		buf.append(" J j =  () -> 26041920;\n");
		buf.append(" K k =  (x, y) -> 1729;\n");
		buf.append(" K kdash =  (int x, int y) -> 1729;\n");
		buf.append(" L l =  vlambda -> 1729;\n");
		buf.append("}\n");
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		AST ast= astRoot.getAST();

		{ // remove a parameter
			TypeDeclaration typedeclaration= findTypeDeclaration(astRoot, "L");
			MethodDeclaration methodDecl = findMethodDeclaration(typedeclaration, "foo");
			List parameters = methodDecl.parameters();
			assertTrue("must be 1 parameter", parameters.size() == 1);
			ASTNode param = (ASTNode) parameters.get(0);
			rewrite.getListRewrite(methodDecl, MethodDeclaration.PARAMETERS_PROPERTY).remove(param, null);
		}

		int fCount = 0;

		TypeDeclaration typedeclaration= findTypeDeclaration(astRoot, "X");
		FieldDeclaration fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(fCount++);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
		LambdaExpression lambdaExpression = (LambdaExpression)fragment.getInitializer();

		{// set parentheses
			assertTrue("lambda expression has parantheses", lambdaExpression.hasParentheses() == false);
			rewrite.set(lambdaExpression, LambdaExpression.PARENTHESES_PROPERTY, Boolean.TRUE, null);
		}

		fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(fCount++);
		fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
		lambdaExpression = (LambdaExpression)fragment.getInitializer();

		{// reset parentheses - a legal operation here.
			assertTrue("lambda expression has parantheses", lambdaExpression.hasParentheses() == true);
			rewrite.set(lambdaExpression, LambdaExpression.PARENTHESES_PROPERTY, Boolean.FALSE, null);
		}

		fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(fCount++);
		fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
		lambdaExpression = (LambdaExpression)fragment.getInitializer();

		{// reset parentheses - an illegal operation here.
			assertTrue("lambda expression does not have parantheses", lambdaExpression.hasParentheses() == true);
			rewrite.set(lambdaExpression, LambdaExpression.PARENTHESES_PROPERTY, Boolean.FALSE, null);
		}

		fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(fCount++);
		fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
		lambdaExpression = (LambdaExpression)fragment.getInitializer();

		{// reset parentheses - an illegal operation here.
			assertTrue("lambda expression does not have parantheses", lambdaExpression.hasParentheses() == true);
			rewrite.set(lambdaExpression, LambdaExpression.PARENTHESES_PROPERTY, Boolean.FALSE, null);
		}

		{// change all the parameter to SVD ie add type info to the parameters.
			List parameters = lambdaExpression.parameters();
			VariableDeclaration param = (VariableDeclaration)parameters.get(0);
			SingleVariableDeclaration newParam= ast.newSingleVariableDeclaration();
			newParam.setName(ast.newSimpleName(new String(param.toString())));
			newParam.setType(ast.newPrimitiveType(PrimitiveType.INT));
			rewrite.getListRewrite(lambdaExpression, LambdaExpression.PARAMETERS_PROPERTY).replace(param, newParam, null);

			param = (VariableDeclaration)parameters.get(1);
			newParam= ast.newSingleVariableDeclaration();
			newParam.setName(ast.newSimpleName(new String(param.toString())));
			newParam.setType(ast.newPrimitiveType(PrimitiveType.INT));
			rewrite.getListRewrite(lambdaExpression, LambdaExpression.PARAMETERS_PROPERTY).replace(param, newParam, null);
		}

		{// Remove type info from the parameters.

			fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(fCount++);
			fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
			lambdaExpression = (LambdaExpression)fragment.getInitializer();
			List parameters = lambdaExpression.parameters();

			for (int i = 0; i < parameters.size(); ++i) {
				SingleVariableDeclaration param = (SingleVariableDeclaration)parameters.get(i);
				VariableDeclarationFragment newParam= ast.newVariableDeclarationFragment();
				newParam.setName(ast.newSimpleName(new String(param.getName().toString())));
				rewrite.getListRewrite(lambdaExpression, LambdaExpression.PARAMETERS_PROPERTY).replace(param, newParam, null);
			}
		}

		fieldDeclaration = (FieldDeclaration) typedeclaration.bodyDeclarations().get(fCount++);
		fragment = (VariableDeclarationFragment)fieldDeclaration.fragments().get(0);
		lambdaExpression = (LambdaExpression)fragment.getInitializer();

		{// remove the only parameter and the rewriter should automatically add the parentheses
			List parameters = lambdaExpression.parameters();
			ASTNode param = (ASTNode)parameters.get(0);
			rewrite.getListRewrite(lambdaExpression, LambdaExpression.PARAMETERS_PROPERTY).remove(param,  null);
		}

		String preview= evaluateRewrite(cu, rewrite);

		buf= new StringBuilder();
		buf.append("package test1;\n");
		buf.append("interface I {\n");
		buf.append("	int foo(int x);\n");
		buf.append("}\n");
		buf.append("interface J {\n");
		buf.append("	int foo();\n");
		buf.append("}\n");
		buf.append("interface K {\n");
		buf.append("	int foo(int x, int y);\n");
		buf.append("}\n");
		buf.append("interface L {\n");
		buf.append("	int foo();\n");
		buf.append("}\n");
		buf.append("public class X {\n");
		buf.append(" I i =  (vlambda) -> 22121887;\n");
		buf.append(" I idash =  vlambda -> 1729;\n");
		buf.append(" J j =  () -> 26041920;\n");
		buf.append(" K k =  (int x, int y) -> 1729;\n");
		buf.append(" K kdash =  (x, y) -> 1729;\n");
		buf.append(" L l =  () -> 1729;\n");
		buf.append("}\n");
		assertEqualString(preview, buf.toString());
	}
}
