/*******************************************************************************
 * Copyright (c) 2022, 2023 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.List;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;

public class ASTConverter_GuardedPattern_Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getAST21(), true);
		this.currentProject = getJavaProject("Converter_19");
		if (this.ast.apiLevel() == AST.JLS21 ) {
			this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_21);
			this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_21);
			this.currentProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_21);
		}
	}

	public ASTConverter_GuardedPattern_Test(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(ASTConverter_GuardedPattern_Test.class);
	}

	static int getAST21() {
		return AST.JLS21;
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	public void testGuardedPattern001() throws CoreException {
		if (!isJRE21) {
			System.err.println("Test "+getName()+" requires a JRE 21");
			return;
		}
		String contents = "" +
						"public class X {\n" +
						"	interface Shape {\n" +
						"		public double calculateArea();\n" +
						"	}\n" +
						"	record Triangle(double base, double height) implements Shape {\n" +
						"		public double calculateArea() {\n" +
						"			return (0.5 * base * height);\n" +
						"		}\n" +
						"	}\n" +
						"	public static void main(String[] args) {\n" +
						"		Shape s= new Triangle(10, 10);\n" +
						"		testTriangle(s);\n" +
						"		s= new Triangle(10, 100);\n" +
						"		testTriangle(s);\n" +
						"	}\n" +
						"	static void testTriangle(Shape s) {\n" +
						"	    switch (s) {\n" +
						"	        case Triangle t \n" +
						"	        when t.calculateArea() > 100 ->\n" +
						"	            System.out.println(\"...Large triangle...\");\n" +
						"	        default ->\n" +
						"	            System.out.println(\"...A shape, possibly a small triangle...\");\n" +
						"	    }\n" +
						"	}\n" +
						"}";
		this.workingCopy = getWorkingCopy("/Converter_19/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;
		assertProblemsSize(compilationUnit, 0);
		node = ((AbstractTypeDeclaration)compilationUnit.types().get(0));
		assertEquals("Not a Type Declaration", ASTNode.TYPE_DECLARATION, node.getNodeType());
		TypeDeclaration type = (TypeDeclaration)node;
		MethodDeclaration[] methods = type.getMethods();
		MethodDeclaration testTriangleMethod = null;
		for (MethodDeclaration  method : methods) {
			SimpleName sName= method.getName();
			if ("testTriangle".equals(sName.getIdentifier())) {
				testTriangleMethod = method;
			}
		}
		assertNotNull("expected Method not found", testTriangleMethod);
		List<ASTNode> statements = testTriangleMethod.getBody().statements();
		ASTNode stmt = statements.get(0);
		assertEquals("Not a Switch Statament", ASTNode.SWITCH_STATEMENT, stmt.getNodeType());
		SwitchStatement swStmt = (SwitchStatement) stmt;
		statements = swStmt.statements();
		ASTNode cCase = statements.get(0);
		assertEquals("Not a Switch Case", ASTNode.SWITCH_CASE, cCase.getNodeType());
		SwitchCase swCase = (SwitchCase) cCase;
		List<ASTNode> expressions = swCase.expressions();
		ASTNode exp = expressions.get(0);
		assertEquals("Not a Guarded Pattern", ASTNode.GUARDED_PATTERN, exp.getNodeType());
		GuardedPattern gPattern = (GuardedPattern) exp;
		int restrictedWhenStartPos = gPattern.getRestrictedIdentifierStartPosition();
		assertEquals("Not correct restricted start position", 444, restrictedWhenStartPos);
	}
}
