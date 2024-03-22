/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.rewrite.modifying;

import java.util.List;

import junit.framework.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.tests.util.Util;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ASTRewritingModifyingCopyTest extends ASTRewritingModifyingTest {
	private static final Class THIS = ASTRewritingModifyingCopyTest.class;

	public ASTRewritingModifyingCopyTest(String name) {
		super(name);
	}

	public static Test allTests() {
		return new Suite(THIS);
	}

	public static Test suite() {
		return allTests();
	}

	public void test0001() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0001", false, null);
		String str = """
			package test0001;
			
			public class X {
			
			}
			class Y /**/ {
			
			}
			class Z {
			
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(1);
		TypeDeclaration typeDeclaration2 = (TypeDeclaration)ASTNode.copySubtree(a, typeDeclaration);
		types.add(typeDeclaration2);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0001;
			
			public class X {
			
			}
			class Y /**/ {
			
			}
			class Z {
			
			}
			class Y /**/ {
			
			}
			""";
		assertEqualString(preview, str1);
	}

	/*
	 * https://bugs.eclipse.org/405699 : modify a copied node.
	 */
	public void test0002() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0002", false, null);
		String str = """
			package test0002;
			
			public class X {
			
			}
			class Y /**/ {
			    //pre
			
			    int i; //post
			}
			class Z {
			
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(1);
		TypeDeclaration typeDeclaration2 = (TypeDeclaration)ASTNode.copySubtree(a, typeDeclaration);
		typeDeclaration2.setName(a.newSimpleName("A"));
		types.add(typeDeclaration2);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0002;
			
			public class X {
			
			}
			class Y /**/ {
			    //pre
			
			    int i; //post
			}
			class Z {
			
			}
			class A {
			    int i; //post
			}
			""";
		assertEqualString(preview, str1);
	}

	/** @deprecated using deprecated code */
	public void test0003() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0003", false, null);
		String str = """
			package test0003;
			
			public class X extends Z1
			                        .Z2
			                            .Z3 {
			
			}
			class Y {
			
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration1 = (TypeDeclaration)types.get(0);
		TypeDeclaration typeDeclaration2 = (TypeDeclaration)types.get(1);
		Name name = typeDeclaration1.getSuperclass();
		Name name2 = (Name)ASTNode.copySubtree(a, name);
		typeDeclaration2.setSuperclass(name2);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0003;
			
			public class X extends Z1
			                        .Z2
			                            .Z3 {
			
			}
			class Y extends Z1
			                        .Z2
			                            .Z3 {
			
			}
			""";
		assertEqualString(preview, str1);
	}

	/** @deprecated using deprecated code */
	public void test0004() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0004", false, null);
		String str = """
			package test0004;
			
			public class X extends Z1
			                        .Z2
			                            .Z3 {
			
			}
			class Y {
			
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration1 = (TypeDeclaration)types.get(0);
		TypeDeclaration typeDeclaration2 = (TypeDeclaration)types.get(1);
		Name name = typeDeclaration1.getSuperclass();
		QualifiedName name2 = (QualifiedName)ASTNode.copySubtree(a, name);
		Name name3 = name2.getQualifier();
		name2.setQualifier(a.newSimpleName("A"));
		typeDeclaration2.setSuperclass(name3);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0004;
			
			public class X extends Z1
			                        .Z2
			                            .Z3 {
			
			}
			class Y extends Z1
			                        .Z2 {
			
			}
			""";
		assertEqualString(Util.convertToIndependantLineDelimiter(preview), Util.convertToIndependantLineDelimiter(str1));
	}

	/** @deprecated using deprecated code */
	public void test0005() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0005", false, null);
		String str = """
			package test0005;
			
			public class X extends Z1
			                        .Z2
			                            .Z3 {
			
			}
			class Y {
			
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration1 = (TypeDeclaration)types.get(0);
		TypeDeclaration typeDeclaration2 = (TypeDeclaration)types.get(1);
		Name name = typeDeclaration1.getSuperclass();
		QualifiedName name2 = (QualifiedName)ASTNode.copySubtree(a, name);
		QualifiedName name3 = (QualifiedName)name2.getQualifier();
		name2.setQualifier(a.newSimpleName("A"));
		name3.getName().setIdentifier("B");
		typeDeclaration2.setSuperclass(name3);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0005;
			
			public class X extends Z1
			                        .Z2
			                            .Z3 {
			
			}
			class Y extends Z1
			                        .Z2 {
			
			}
			""";
		assertEqualString(Util.convertToIndependantLineDelimiter(preview), Util.convertToIndependantLineDelimiter(str1));
	}

	public void test0006() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0006", false, null);
		String str = """
			package test0006;
			
			public class X {
			    void foo() {
			        bar1();
			       \s
			        //comment1
			        bar2();//comment2
			        //comment3
			        bar3();
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		AST a = astRoot.getAST();

		List types = astRoot.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDeclaration = typeDeclaration.getMethods()[0];
		Block body = methodDeclaration.getBody();
		List statements = body.statements();
		Statement statement1 = (Statement)statements.get(1);
		Statement statement2 = (Statement)ASTNode.copySubtree(a, statement1);
		statements.add(statement2);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test0006;
			
			public class X {
			    void foo() {
			        bar1();
			       \s
			        //comment1
			        bar2();//comment2
			        //comment3
			        bar3();
			        //comment1
			        bar2();//comment2
			    }
			}
			""";
		assertEqualString(Util.convertToIndependantLineDelimiter(preview), Util.convertToIndependantLineDelimiter(str1));
	}

	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=93208
	/** @deprecated using deprecated code */
	public void test0007() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test", false, null);
		String str = """
			package test; public class Test { }""";
		ICompilationUnit cu= pack1.createCompilationUnit("Test.java", str, false, null);

		CompilationUnit astRoot= createCU(cu, false);

		astRoot.recordModifications();

		TypeDeclaration type = (TypeDeclaration) astRoot.types().get(0);
		AST ast = type.getAST();

		MethodDeclaration m = ast.newMethodDeclaration();
		type.bodyDeclarations().add(m);

		Block block = ast.newBlock();
		m.setName(ast.newSimpleName("foo"));
		m.setReturnType(ast.newPrimitiveType(PrimitiveType.VOID));
		m.setBody(block);

		FieldAccess fa = ast.newFieldAccess();
		fa.setExpression(ast.newThisExpression());
		fa.setName(ast.newSimpleName("x"));
		MethodInvocation mi = ast.newMethodInvocation();
		mi.setExpression(fa);
		mi.setName(ast.newSimpleName("llall"));

		ExpressionStatement exp = ast.newExpressionStatement(mi);
		block.statements().add(exp);

		StructuralPropertyDescriptor loc = mi.getLocationInParent();
		//This will cause the bug
		ASTNode node = ASTNode.copySubtree(ast, fa);
		exp.setStructuralProperty(loc, node);

		String preview = evaluateRewrite(cu, astRoot);

		String str1 = """
			package test; public class Test {
			
			    void foo(){this.x;} }""";
		assertEqualString(Util.convertToIndependantLineDelimiter(preview), Util.convertToIndependantLineDelimiter(str1));
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=304656
	public void test0008() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0008", false, null);
		String str = """
			package test0008;
			
			public class TestClass {
			    Thread t = new Thread(new Runnable(){
			
			        @Override
			        public void run() {
			            try {
			                Thread.currentThread().sleep(1000);
			            } catch (InterruptedException e) {
			                e.printStackTrace();
			            }
			        }
			       \s
			    });
			
			    public void testMethod(){
			        t.start();
			    }
			
			    public static Thread staticTestMethod(Thread thread){
			        return thread;
			    }
			}""";
		ICompilationUnit cu = pack1.createCompilationUnit("Test.java", str, false, null);

		ASTParser astParser = ASTParser.newParser(getJLS3());
		astParser.setSource(cu);
		ASTNode root = astParser.createAST(new NullProgressMonitor());
		AST ast = root.getAST();

		CompilationUnit compilationUnit = (CompilationUnit) root;
		compilationUnit.recordModifications();
		List types = compilationUnit.types();
		for (int i = 0, max = types.size(); i < max; i++) {
			TypeDeclaration td = (TypeDeclaration) types.get(i);
			MethodDeclaration[] methods = td.getMethods();
			for (int j = 0, max2 = methods.length; j < max2; j++) {
				MethodDeclaration md = methods[j];
				if (md.getName().getFullyQualifiedName().equals("testMethod")) {
					List statements = md.getBody().statements();
					ExpressionStatement es = (ExpressionStatement) statements.get(0);
					MethodInvocation mi = (MethodInvocation) es.getExpression();
					Expression existingExpression = mi.getExpression();
					MethodInvocation threadNameInvocation = ast.newMethodInvocation();
					threadNameInvocation.setExpression(null);
					threadNameInvocation.setName(ast.newSimpleName("staticTestMethod"));
					existingExpression.delete();
					threadNameInvocation.arguments().add(existingExpression);
					mi.setExpression(threadNameInvocation);


					Block code = md.getBody();
					TryStatement tryStatement = ast.newTryStatement();
					List catches = tryStatement.catchClauses();
					// create catch clause
					CatchClause catchClause = ast.newCatchClause();
					// exception to catch
					SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
					svd.setType(ast.newSimpleType(ast.newName("Throwable")));
					svd.setName(ast.newSimpleName("e"));
					catchClause.setException(svd);
					// code to run on catch
					Block catchBody = ast.newBlock();
					List catchStatements = catchBody.statements();
					// throw statement
					ThrowStatement throwStatement = ast.newThrowStatement();
					Expression throwExpression = ast.newName("e");
					throwStatement.setExpression(throwExpression);
					catchStatements.add(throwStatement);
					catches.add(catchClause);
					// create finally statement
					Block finallyBlock = ast.newBlock();
					statements = code.statements();
					tryStatement.setFinally(finallyBlock);
					code.delete();
					tryStatement.setBody(code);
					Block tryBlock = ast.newBlock();
					tryBlock.statements().add(tryStatement);
					md.setBody(tryBlock);
				}
			}

		}
		String preview = evaluateRewrite(cu, compilationUnit);

		String str1 = """
			package test0008;
			
			public class TestClass {
			    Thread t = new Thread(new Runnable(){
			
			        @Override
			        public void run() {
			            try {
			                Thread.currentThread().sleep(1000);
			            } catch (InterruptedException e) {
			                e.printStackTrace();
			            }
			        }
			       \s
			    });
			
			    public void testMethod(){
			        try {
			            staticTestMethod(t).start();
			        } catch (Throwable e) {
			        } finally {
			        }
			    }
			
			    public static Thread staticTestMethod(Thread thread){
			        return thread;
			    }
			}""";
		assertEqualString(Util.convertToIndependantLineDelimiter(preview), Util.convertToIndependantLineDelimiter(str1));
	}

	public void test0009() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test0007", false, null);
		String str = """
			package test0007;
			
			public class X {
			    /**
			     * NOTHING
			     */
			    void foo() {
			   \s
			    }
			    void bar() {
			   \s
			    }
			}
			""";
		ICompilationUnit cu= pack1.createCompilationUnit("X.java", str, false, null);

		ASTParser astParser = ASTParser.newParser(getJLS3());
		astParser.setSource(cu);
		CompilationUnit compilationUnit = (CompilationUnit) astParser.createAST(new NullProgressMonitor());
		AST ast = compilationUnit.getAST();

		compilationUnit.recordModifications();

		List types = compilationUnit.types();
		TypeDeclaration typeDeclaration = (TypeDeclaration)types.get(0);
		MethodDeclaration methodDeclaration1 = typeDeclaration.getMethods()[0];
		MethodDeclaration methodDeclaration2 = typeDeclaration.getMethods()[1];
		Javadoc javadoc1 = methodDeclaration1.getJavadoc();
		Javadoc javadoc2 = (Javadoc)ASTNode.copySubtree(ast, javadoc1);
		methodDeclaration2.setJavadoc(javadoc2);

		String preview = evaluateRewrite(cu, compilationUnit);

		String str1 = """
			package test0007;
			
			public class X {
			    /**
			     * NOTHING
			     */
			    void foo() {
			   \s
			    }
			    /**
			     * NOTHING
			     */
			    void bar() {
			   \s
			    }
			}
			""";
		assertEqualString(Util.convertToIndependantLineDelimiter(preview), Util.convertToIndependantLineDelimiter(str1));
	}
}
