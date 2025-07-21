/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.tests.dom;

import java.util.List;
import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class ASTConverterSuperAfterStatements extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getASTLatest(), true);
		this.currentProject = getJavaProject("Converter_23");
		if (this.ast.apiLevel() == AST.JLS23) {
			this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_23);
			this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_23);
			this.currentProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_23);
			this.currentProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			this.currentProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
		}
	}

	public ASTConverterSuperAfterStatements(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(ASTConverterSuperAfterStatements.class);
	}

	static int getASTLatest() {
		return AST.getJLSLatest();
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	private void printJREError() {
		System.err.println("Test "+getName()+" requires a JRE 23");
	}

	public void test001() throws JavaModelException {
		if (!isJRE23) {
			printJREError();
			return;
		}

		String contents = """
				public class X{
				    X(int i) {
				    	if (i < 0)
				    		i++;
				        super();
				    }
				    public static void main(String[] argv) {
				    	new X(0);
				    }
				}
				""";

		this.workingCopy = getWorkingCopy("/Converter_23/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Wrong type of statement", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;

		BodyDeclaration bodyDeclaration = (BodyDeclaration) getASTNode(compilationUnit, 0, 0);
		MethodDeclaration methodDeclaration = (MethodDeclaration) bodyDeclaration;
		Block block = methodDeclaration.getBody();
		assertEquals("statements size", block.statements().size(), 2);

		assertProblemsSize(compilationUnit, 0);
		node = getASTNode(compilationUnit, 0, 0, 0);
		assertEquals("IF statement", node.getNodeType(), ASTNode.IF_STATEMENT);

		node = getASTNode(compilationUnit, 0, 0, 1);
		assertEquals("super Constructor", node.getNodeType(), ASTNode.SUPER_CONSTRUCTOR_INVOCATION);
	}

	public void test002() throws JavaModelException {
		if (!isJRE23) {
			printJREError();
			return;
		}
		String contents = """
				class X {
					    void hello() {
					        System.out.println("Hello");
					    }
					    class Inner {
					        Inner() {
					            hello();
					            super();
					        }
					    }
					    public static void main(String[] args) {
							new X().new Inner();
						}
					}
				""";

		this.workingCopy = getWorkingCopy("/Converter_23/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Wrong type of statement", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;

		TypeDeclaration bodyDeclaration = (TypeDeclaration) getASTNode(compilationUnit, 0, 1);
		List<MethodDeclaration> methodDeclarations = bodyDeclaration.bodyDeclarations();
		Block firstMethodDeclaration = methodDeclarations.get(0).getBody();
		List<ASTNode> statements = firstMethodDeclaration.statements();
		assertEquals("statements size", statements.size(), 2);

		ASTNode expressionStatement = statements.get(0);
		ASTNode superConstructorInvocation = statements.get(1);
		assertEquals("expression statement", expressionStatement.getNodeType(), ASTNode.EXPRESSION_STATEMENT);
		assertEquals("Super constructor invocation", superConstructorInvocation.getNodeType(), ASTNode.SUPER_CONSTRUCTOR_INVOCATION);
	}

	public void test003() throws JavaModelException {
		if (!isJRE23) {
			printJREError();
			return;
		}
		String contents = """
				class Y {
					public int[] vArr;
					private F f1;
					private F f2;
					Y(F f1, F f2) {
						this.f1 = f1;
						this.f2 = f2;
					}
				}
				class F {}
				public class X extends Y {
					public int i;
					public X(int i) {
				        var f = new F();
				        super(f, f);
				        this.i = i;
				    }
				    public static void main(String[] args) {
						X x = new X(100);
						System.out.println(x.i);
						X x2 = new X(1);
						System.out.println(x2.i);
					}
				}
				""";
		this.workingCopy = getWorkingCopy("/Converter_23/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Wrong type of statement", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;

		assertProblemsSize(compilationUnit, 0);
		assertEquals("No.of classes", compilationUnit.types().size(), 3);
		node = ((TypeDeclaration)compilationUnit.types().get(2));
		List<MethodDeclaration> bodyDeclarations = ((TypeDeclaration) node).bodyDeclarations();

		assertEquals("Method Decleration", ASTNode.METHOD_DECLARATION, bodyDeclarations.get(1).getNodeType());
		assertEquals("Method Decleration", ASTNode.METHOD_DECLARATION, bodyDeclarations.get(2).getNodeType());

		Block firstMethodDeclaration =  bodyDeclarations.get(1).getBody();
		List<ASTNode> statements = firstMethodDeclaration.statements();

		assertEquals("Variable Decleration", ASTNode.VARIABLE_DECLARATION_STATEMENT, statements.get(0).getNodeType());
		assertEquals("Super Constructor Invocation", ASTNode.SUPER_CONSTRUCTOR_INVOCATION, statements.get(1).getNodeType());
		assertEquals("Super Constructor Invocation", ASTNode.EXPRESSION_STATEMENT, statements.get(2).getNodeType());

	}

	public void test004() throws JavaModelException {
		if (!isJRE23) {
			printJREError();
			return;
		}
		String contents = """
				class X {
					X() {
				        S tmp = new S(){};    // OK
				        super();
				    }
				    public static void main(String[] args) {
						System.out.println("hello");
					}
				}
				class S {}
				""";

		this.workingCopy = getWorkingCopy("/Converter_23/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(contents, this.workingCopy);
		assertEquals("Wrong type of statement", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit compilationUnit = (CompilationUnit) node;

		assertProblemsSize(compilationUnit, 0);
		assertEquals("No.of classes", compilationUnit.types().size(), 2);
		node = ((TypeDeclaration)compilationUnit.types().get(0));
		List<MethodDeclaration> bodyDeclarations = ((TypeDeclaration) node).bodyDeclarations();

		assertEquals("Method Decleration", ASTNode.METHOD_DECLARATION, bodyDeclarations.get(0).getNodeType());
		assertEquals("Method Decleration", ASTNode.METHOD_DECLARATION, bodyDeclarations.get(1).getNodeType());

		Block firstMethodDeclaration =  bodyDeclarations.get(0).getBody();
		List<ASTNode> statements = firstMethodDeclaration.statements();

		assertEquals("Variable Decleration", ASTNode.VARIABLE_DECLARATION_STATEMENT, statements.get(0).getNodeType());
		assertEquals("Super Constructor Invocation", ASTNode.SUPER_CONSTRUCTOR_INVOCATION, statements.get(1).getNodeType());
	}

}
