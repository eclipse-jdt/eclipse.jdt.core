/*******************************************************************************
 * Copyright (c) 2000, 2019 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.dom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import junit.framework.Test;

import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ASTConverter17Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getJLS4(), false);
	}

	public ASTConverter17Test(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 19 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] {"test0001"};
	}
	public static Test suite() {
		return buildModelTestSuite(ASTConverter17Test.class);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}

	/*
	 * Binary literals
	 */
	public void test0001() throws JavaModelException {
		String contents =
			"""
			public class X {
				public static final int VAR = 0b001;
			}""";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		final List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		final Expression initializer = fragment.getInitializer();
		assertEquals("Not a number literal", ASTNode.NUMBER_LITERAL, initializer.getNodeType());
		checkSourceRange(initializer, "0b001", contents);
		assertEquals("Wrong token", "0b001", ((NumberLiteral) initializer).getToken());
	}
	/*
	 * Binary literals with underscores
	 */
	public void test0002() throws JavaModelException {
		String contents =
			"""
			public class X {
				public static final int VAR = 0b0_0__1;
			}""";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		final List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		final Expression initializer = fragment.getInitializer();
		assertEquals("Not a number literal", ASTNode.NUMBER_LITERAL, initializer.getNodeType());
		checkSourceRange(initializer, "0b0_0__1", contents);
		assertEquals("Wrong token", "0b0_0__1", ((NumberLiteral) initializer).getToken());
	}

	/*
	 * Integer literals with underscores
	 */
	public void test0003() throws JavaModelException {
		String contents =
			"""
			public class X {
				public static final int VAR = 1_2_3_4;
			}""";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0);
		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
		final List fragments = fieldDeclaration.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
		final Expression initializer = fragment.getInitializer();
		assertEquals("Not a number literal", ASTNode.NUMBER_LITERAL, initializer.getNodeType());
		checkSourceRange(initializer, "1_2_3_4", contents);
		assertEquals("Wrong token", "1_2_3_4", ((NumberLiteral) initializer).getToken());
		IVariableBinding variableBinding = fragment.resolveBinding();
		Integer constantValue = (Integer) variableBinding.getConstantValue();
		assertEquals("Wrong value", 1234, constantValue.intValue());
	}
	/*
	 * Switch on strings
	 */
	public void test0004() throws JavaModelException {
		String contents =
			"""
			public class X {
				public void foo(String s) {
					switch(s) {
						case "Hello" :
							System.out.println(s);
							break;
					}
				}
			}""";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		ASTNode node = buildAST(
			contents,
			this.workingCopy);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a switch statement", ASTNode.SWITCH_STATEMENT, node.getNodeType());
		SwitchStatement switchStatement = (SwitchStatement) node;
		Expression expression = switchStatement.getExpression();
		ITypeBinding typeBinding = expression.resolveTypeBinding();
		assertEquals("Wrong type", "java.lang.String", typeBinding.getQualifiedName());
	}
	/*
	 * Union types (update for bug 340608)
	 */
	public void test0005() throws JavaModelException {
		String contents =
			"""
			public class X {
				public void foo(String s) {
					try {
						System.out.println(s);
						Integer.parseInt(s);
					} catch(NumberFormatException | ArithmeticException e) {
						e.printStackTrace();
					}
				}
			}""";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(getJLS4(), this.workingCopy, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a try statement", ASTNode.TRY_STATEMENT, node.getNodeType());
		TryStatement tryStatement = (TryStatement) node;
		List catchClauses = tryStatement.catchClauses();
		CatchClause clause = (CatchClause) catchClauses.get(0);
		SingleVariableDeclaration exception = clause.getException();
		Type type = exception.getType();
		assertEquals("Not an union type", ASTNode.UNION_TYPE, type.getNodeType());
		checkSourceRange(type, "NumberFormatException | ArithmeticException", contents);
		ITypeBinding typeBinding = type.resolveBinding();
		assertNotNull("No binding", typeBinding);
	}
	/*
	 * Check that catch type with union type as a simple type is converted to a simple type
	 */
	public void test0006() throws JavaModelException {
		String contents =
			"""
			public class X {
				public void foo(String s) {
					try {
						System.out.println(s);
						Integer.parseInt(s);
					} catch(NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}""";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", false/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a try statement", ASTNode.TRY_STATEMENT, node.getNodeType());
		TryStatement tryStatement = (TryStatement) node;
		List catchClauses = tryStatement.catchClauses();
		CatchClause clause = (CatchClause) catchClauses.get(0);
		SingleVariableDeclaration exception = clause.getException();
		Type type = exception.getType();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, type.getNodeType());
		checkSourceRange(type, "NumberFormatException", contents);
	}
	/*
	 * Check that catch type with union type as a simple type is converted to a simple type
	 */
	public void test0007() throws JavaModelException {
		String contents =
			"""
			public class X {
				public void foo(String s) {
					try (Reader r = new FileReader(s)) {
						System.out.println(s);
						Integer.parseInt(s);
					} catch(NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}""";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", false/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(getJLS4(), this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a try statement", ASTNode.TRY_STATEMENT, node.getNodeType());
		TryStatement tryStatement = (TryStatement) node;
		List catchClauses = tryStatement.catchClauses();
		CatchClause clause = (CatchClause) catchClauses.get(0);
		SingleVariableDeclaration exception = clause.getException();
		Type type = exception.getType();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, type.getNodeType());
		checkSourceRange(type, "NumberFormatException", contents);
		List resources = tryStatement.resources();
		checkSourceRange((ASTNode) resources.get(0), "Reader r = new FileReader(s)", contents);
	}
	/*
	 * Check that catch type with union type as a simple type is converted to a simple type
	 */
	public void test0008() throws JavaModelException {
		String contents =
			"""
			public class X {
				public void foo(String s) {
					try (Reader r = new FileReader(s);) {
						System.out.println(s);
						Integer.parseInt(s);
					} catch(NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}""";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", false/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(getJLS4(), this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a try statement", ASTNode.TRY_STATEMENT, node.getNodeType());
		TryStatement tryStatement = (TryStatement) node;
		List catchClauses = tryStatement.catchClauses();
		CatchClause clause = (CatchClause) catchClauses.get(0);
		SingleVariableDeclaration exception = clause.getException();
		Type type = exception.getType();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, type.getNodeType());
		checkSourceRange(type, "NumberFormatException", contents);
		List resources = tryStatement.resources();
		checkSourceRange((ASTNode) resources.get(0), "Reader r = new FileReader(s)", contents);
	}
	/*
	 * Check that catch type with union type as a simple type is converted to a simple type
	 */
	public void test0009() throws JavaModelException {
		String contents =
			"""
			public class X {
				public void foo(String s) {
					try (Reader r = new FileReader(s);Reader r2 = new FileReader(s);) {
						System.out.println(s);
						Integer.parseInt(s);
					} catch(NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}""";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", false/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(getJLS4(), this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a try statement", ASTNode.TRY_STATEMENT, node.getNodeType());
		TryStatement TryStatement = (TryStatement) node;
		List catchClauses = TryStatement.catchClauses();
		CatchClause clause = (CatchClause) catchClauses.get(0);
		SingleVariableDeclaration exception = clause.getException();
		Type type = exception.getType();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, type.getNodeType());
		checkSourceRange(type, "NumberFormatException", contents);
		List resources = TryStatement.resources();
		checkSourceRange((ASTNode) resources.get(0), "Reader r = new FileReader(s)", contents);
		checkSourceRange((ASTNode) resources.get(1), "Reader r2 = new FileReader(s)", contents);
	}
	/*
	 * Check that catch type with union type as a simple type is converted to a simple type
	 */
	public void test0010() throws JavaModelException {
		String contents =
			"""
			public class X {
				public void foo(String s) {
					try (Reader r = new FileReader(s);Reader r2 = new FileReader(s)) {
						System.out.println(s);
						Integer.parseInt(s);
					} catch(NumberFormatException e) {
						e.printStackTrace();
					}
				}
			}""";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", false/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(getJLS4(), this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a try statement", ASTNode.TRY_STATEMENT, node.getNodeType());
		TryStatement tryStatement = (TryStatement) node;
		List catchClauses = tryStatement.catchClauses();
		CatchClause clause = (CatchClause) catchClauses.get(0);
		SingleVariableDeclaration exception = clause.getException();
		Type type = exception.getType();
		assertEquals("Not a simple type", ASTNode.SIMPLE_TYPE, type.getNodeType());
		checkSourceRange(type, "NumberFormatException", contents);
		List resources = tryStatement.resources();
		checkSourceRange((ASTNode) resources.get(0), "Reader r = new FileReader(s)", contents);
		checkSourceRange((ASTNode) resources.get(1), "Reader r2 = new FileReader(s)", contents);
	}
	/*
	 * Union types (update for bug 340608)
	 */
	public void test0011() throws JavaModelException {
		String contents =
			"""
			public class X {
			    public static void main(String[] args) {
			        try {
			            int option= 1;
			            throw option == 1 ? new ExceptionA() : new ExceptionB();
			        } catch (/*final*/ ExceptionA | ExceptionB ex) {
			            System.out.println("type of ex: " + ex.getClass());
			            // next 2 methods on 'ex' use different parts of lub:
			            ex.myMethod();
			            throw ex;
			        }
			    }
			}
			interface Mix {
			    public void myMethod();
			}
			class ExceptionA extends RuntimeException implements Mix {
			    private static final long serialVersionUID = 1L;
			    public void myMethod() {
			        System.out.println("ExceptionA.myMethod()");
			    }
			    public void onlyA() {
			        System.out.println("ExceptionA.onlyA()");
			    }
			}
			class ExceptionB extends RuntimeException implements Mix {
			    private static final long serialVersionUID = 1L;
			    public void myMethod() {
			        System.out.println("ExceptionB.myMethod()");
			    }
			    public void onlyB() {
			        System.out.println("ExceptionA.onlyB()");
			    }
			}""";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(getJLS4(), this.workingCopy, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a try statement", ASTNode.TRY_STATEMENT, node.getNodeType());
		TryStatement tryStatement = (TryStatement) node;
		List catchClauses = tryStatement.catchClauses();
		CatchClause clause = (CatchClause) catchClauses.get(0);
		SingleVariableDeclaration exception = clause.getException();
		Type type = exception.getType();
		assertEquals("Not an union type", ASTNode.UNION_TYPE, type.getNodeType());
		checkSourceRange(type, "ExceptionA | ExceptionB", contents);
		ITypeBinding typeBinding = type.resolveBinding();
		assertNotNull("No binding", typeBinding);
		typeBinding = typeBinding.getGenericTypeOfWildcardType();
		assertNull("This should be null for intersection type", typeBinding);
	}
	/*
	 * Binary literals with underscores
	 */
	public void test0012() throws JavaModelException {
		AST localAst= AST.newAST(getJLS4(), false);
		NumberLiteral literal= localAst.newNumberLiteral();
		try {
			literal.setToken("0b1010");
			literal.setToken("0xCAFE_BABE");
			literal.setToken("01_234");
			literal.setToken("1_234");
			literal.setToken("0b1_01_0");
		} catch(IllegalArgumentException e) {
			assertTrue("Should not happen", false);
		}
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=344522
	 */
	public void test0013() throws JavaModelException {
		String contents =
				"""
			import java.util.*;
			public class X {
				public static Object foo() {
					List<String> l = new ArrayList<>();
					return l;
				}
			}""";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(getJLS4(), this.workingCopy, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not a try statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) ((VariableDeclarationFragment) statement.fragments().get(0)).getInitializer();
		Type type = classInstanceCreation.getType();
		assertTrue("Should be Parameterized type", type.isParameterizedType());
		checkSourceRange(type, "ArrayList<>", contents);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=349862
	 */
	public void test0014() throws JavaModelException {
		String contents =
				"""
			public class X {
				void foo() {
					try (Object | Integer res= null) {
					} catch (Exception e) {
					}
				}
			}""";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(getJLS4(), this.workingCopy, true, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0);
		assertTrue("The method declaration is not malformed", isMalformed(node));
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=344522
	 */
	public void test0015() throws JavaModelException {
		String contents =
				"""
			import java.lang.invoke.MethodHandle;
			import java.lang.invoke.MethodHandles;
			import java.lang.invoke.MethodType;
			
			public class X {
				public static void main(String[] args) throws Throwable {
					Object x;
					String s;
					int i;
					MethodType mt;
					MethodHandle mh;
					MethodHandles.Lookup lookup = MethodHandles.lookup();
					// mt is (char,char)String
					mt = MethodType.methodType(String.class, char.class, char.class);
					mh = lookup.findVirtual(String.class, "replace", mt);
					s = (String) mh.invokeExact("daddy", 'd', 'n');
					// invokeExact(Ljava/lang/String;CC)Ljava/lang/String;
					assert s.equals("nanny");
					// weakly typed invocation (using MHs.invoke)
					s = (String) mh.invokeWithArguments("sappy", 'p', 'v');
					assert s.equals("nanny");
					// mt is (Object[])List
					mt = MethodType.methodType(java.util.List.class, Object[].class);
					mh = lookup.findStatic(java.util.Arrays.class, "asList", mt);
					assert (mh.isVarargsCollector());
					x = mh.invoke("one", "two");
					// invoke(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/Object;
					System.out.println(x);
					// mt is (Object,Object,Object)Object
					mt = MethodType.genericMethodType(3);
					mh = mh.asType(mt);
					x = mh.invokeExact((Object) 1, (Object) 2, (Object) 3);
					// invokeExact(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
					System.out.println(x);
					// mt is ()int
					mt = MethodType.methodType(int.class);
					mh = lookup.findVirtual(java.util.List.class, "size", mt);
					i = (int) mh.invokeExact(java.util.Arrays.asList(1, 2, 3));
					// invokeExact(Ljava/util/List;)I
					assert (i == 3);
					mt = MethodType.methodType(void.class, String.class);
					mh = lookup.findVirtual(java.io.PrintStream.class, "println", mt);
					mh.invokeExact(System.out, "Hello, world.");
					// invokeExact(Ljava/io/PrintStream;Ljava/lang/String;)V
				}
			}""";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(getJLS4(), this.workingCopy, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		IProblem[] problems = unit.getProblems();
		for (int i = 0; i < problems.length; i++) {
			IProblem iProblem = problems[i];
			System.err.println(iProblem);
		}
		final List invokeExactMethods = new ArrayList();
		unit.accept(new ASTVisitor() {
			public boolean visit(MethodInvocation methodInvocation) {
				IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
				if (methodBinding != null) {
					IJavaElement javaElement = methodBinding.getJavaElement();
					assertNotNull("No java element for : " + methodBinding, javaElement);
					String elementName = javaElement.getElementName();
					if ("invokeExact".equals(elementName)) {
						invokeExactMethods.add(methodBinding);
					}
				}
				return true;
			}
		});
		assertEquals("Wrong size", 4, invokeExactMethods.size());
		IMethodBinding first = (IMethodBinding) invokeExactMethods.get(0);
		IMethodBinding second = (IMethodBinding) invokeExactMethods.get(1);
		assertEquals(first.getMethodDeclaration(), second.getMethodDeclaration());
		String firstKey = first.getKey();
		String secondKey = second.getKey();
		assertEquals("Wrong key", "Ljava/lang/invoke/MethodHandle;.invokeExact(Ljava/lang/String;CC)Ljava/lang/String;|Ljava/lang/Throwable;", firstKey);
		assertEquals("Wrong key", "Ljava/lang/invoke/MethodHandle;.invokeExact(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;|Ljava/lang/Throwable;", secondKey);
		assertFalse("Not the same key", firstKey.equals(secondKey));
		// check that all are resolved
		for (Iterator iterator = invokeExactMethods.iterator(); iterator.hasNext();) {
			IMethodBinding methodBinding = (IMethodBinding) iterator.next();
			assertTrue("Not resolved", ((IMethod) methodBinding.getJavaElement()).isResolved());
			assertTrue("Not a varargs method", methodBinding.getMethodDeclaration().isVarargs());
			assertFalse("Is a varargs method", methodBinding.isVarargs());
		}
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=350039
	 */
	public void test0016() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		String contents =
				"""
			import java.lang.invoke.MethodHandle;
			import java.lang.invoke.MethodHandles;
			import java.lang.invoke.MethodType;
			
			public class X {
				void bar() throws Throwable {
					MethodType mt;
					MethodHandle mh;
					MethodHandles.Lookup lookup = MethodHandles.lookup();
					mt = MethodType.methodType(String.class, char.class, char.class);
					mh = lookup.findVirtual(String.class, "replace", mt);
					String s = (String) mh.invokeExact("daddy",'d','n');
				}
			}""";
		this.workingCopy.getBuffer().setContents(contents);
		this.workingCopy.save(null, true);
		final ASTNode[] asts = new ASTNode[1];
		final IBinding[] bindings = new IBinding[1];
		final String key = "Ljava/lang/invoke/MethodHandle;.invokeExact(Ljava/lang/String;CC)Ljava/lang/String;|Ljava/lang/Throwable;";
		resolveASTs(
			new ICompilationUnit[] {
				this.workingCopy
			},
			new String[] {
				key
			},
			new ASTRequestor() {
				public void acceptAST(ICompilationUnit source, CompilationUnit localAst) {
					asts[0] = localAst;
				}
				public void acceptBinding(String bindingKey, IBinding binding) {
					if (key.equals(bindingKey)) {
						bindings[0] = binding;
					}
				}
			},
			getJavaProject("Converter17"),
			null);
		ASTNode node = asts[0];
		assertNotNull("Should not be null", node);
		assertNotNull("Should not be null", bindings[0]);
		assertEquals("Wrong kind", IBinding.METHOD, bindings[0].getKind());
		ITypeBinding[] parameterTypes = ((IMethodBinding) bindings[0]).getParameterTypes();
		assertEquals("Wrong size", 3, parameterTypes.length);
		assertEquals("Wrong key", key, bindings[0].getKey());

		VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) getASTNode((CompilationUnit) node, 0, 0, 5);
		Expression initializer = ((VariableDeclarationFragment) variableDeclarationStatement.fragments().get(0)).getInitializer();
		MethodInvocation invocation = (MethodInvocation) ((CastExpression) initializer).getExpression();
		IMethodBinding invokeExactBinding = invocation.resolveMethodBinding();
		IAnnotationBinding[] annotations = invokeExactBinding.getAnnotations();
		assertEquals("Wrong size", 1, annotations.length);
		assertEquals("Wrong annotation", "java.lang.invoke.MethodHandle.PolymorphicSignature", annotations[0].getAnnotationType().getQualifiedName());
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=350496
	 */
	public void test0017() throws JavaModelException {
		String contents =
			"package test0017;\n" +
			"public class Y {}";
		this.workingCopy = getWorkingCopy(
				"/Converter17/src/test0017/Y.java",
				contents,
				true/*resolve*/
			);
		IType type = this.workingCopy.getJavaProject().findType("java.lang.invoke.MethodHandle");
		IMethod[] methods = type.getMethods();
		boolean found = false;
		for (int i = 0; i < methods.length; i++) {
			IMethod iMethod = methods[i];
			if ("invokeExact".equals(iMethod.getElementName())) {
				IAnnotation[] annotations = iMethod.getAnnotations();
				assertEquals("Wrong size", 1, annotations.length);
				assertAnnotationsEqual("@java.lang.invoke.MethodHandle$PolymorphicSignature\n", annotations);
				found = true;
			}
		}
		assertTrue("No method found", found);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=350897
	 */
	public void test0018() throws JavaModelException {
		String contents =
			"""
			public class X<T> {
				T field1;
				public X(T param){
					field1 = param;
				}
			
				public static void main(String[] args) {
					X<Object> a = new X<Object>("hello");
					X.testFunction(a.getField()); //prints 2
					X<String> b = new X<>("hello");
					X.testFunction(b.getField()); // prints 1
			
					X<Object> c = new X<>(null);
					X.testFunction(c.getField()); // prints 2
					X<String> d = new X<>(null);
					X.testFunction(d.getField()); // prints 1
				}
				public static void testFunction(String param){
					System.out.println(1 + ", String param: " + param);
				}
				public static void testFunction(Object param){
					System.out.println(2);
				}
				public T getField(){
					return field1;
				}
			}""";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(getJLS4(), this.workingCopy, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 0);
		class InferredTypeFromExpectedVisitor extends ASTVisitor {
			StringBuilder buf = new StringBuilder();
			public boolean visit(ClassInstanceCreation creation) {
				this.buf.append(creation.isResolvedTypeInferredFromExpectedType());
				return false;
			}
			public String toString() {
				return String.valueOf(this.buf);
			}
		}
		InferredTypeFromExpectedVisitor visitor = new InferredTypeFromExpectedVisitor();
		unit.accept(visitor);
		assertEquals("Wrong contents", "falsefalsetruetrue", String.valueOf(visitor));
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=353093
	 */
	public void test0019() throws JavaModelException {
		String contents =
			"package test0019;\n" +
			"public class Y {}";
		this.workingCopy = getWorkingCopy(
				"/Converter17/src/test0017/Y.java",
				contents,
				true/*resolve*/
			);
		IType type = this.workingCopy.getJavaProject().findType("java.util.Arrays");
		IMethod[] methods = type.getMethods();
		boolean found = false;
		for (int i = 0; i < methods.length; i++) {
			IMethod iMethod = methods[i];
			if ("asList".equals(iMethod.getElementName())) {
				IAnnotation[] annotations = iMethod.getAnnotations();
				assertEquals("Wrong size", 1, annotations.length);
				assertAnnotationsEqual("@java.lang.SafeVarargs\n", annotations);
				found = true;
			}
		}
		assertTrue("No method found", found);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=353640
	 */
	public void test0020() throws JavaModelException {
		String contents =
			"""
			public class DiamondTest<T> {
				public <U> DiamondTest(T t) {}
				public static void main ( String[] args ) {
					DiamondTest<String> d = new <Integer> DiamondTest<>();
				}
			}""";
		this.workingCopy = getWorkingCopy("/Converter17/src/DiamondTest.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(this.workingCopy, true);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		assertProblemsSize(unit, 1, "Explicit type arguments cannot be used with \'<>\' in an allocation expression");
		VariableDeclarationStatement statement = (VariableDeclarationStatement) getASTNode(unit, 0, 1, 0);
		ClassInstanceCreation creation = (ClassInstanceCreation) ((VariableDeclarationFragment) statement.fragments().get(0)).getInitializer();
		ITypeBinding binding = creation.resolveTypeBinding();
		IMethodBinding[] methods = binding.getDeclaredMethods();
		assertEquals("Wrong size", 2, methods.length);
	}
	/**
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=402673
     */
    public void test402673a() throws JavaModelException {
            String contents = """
				package test402673;\
				public class X {
				    Runnable r = () -> System.out.println("hi");
				}
				""";
        	this.workingCopy = getWorkingCopy("/Converter/src/test402673/X.java", true/* resolve */);
        	this.workingCopy.getBuffer().setContents(contents);
        	ASTNode node = runConversion(this.workingCopy, true);
        	assertTrue(node != null);
    		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    		CompilationUnit unit = (CompilationUnit) node;
    		assertProblemsSize(unit, 1, "Lambda expressions are allowed only at source level 1.8 or above");
    		TypeDeclaration type = (TypeDeclaration) getASTNode(unit, 0);
    		assertTrue((type.getFlags() & ASTNode.MALFORMED) != 0);
    		node = getASTNode(unit, 0, 0);
    		assertEquals("Not a field declaration", ASTNode.FIELD_DECLARATION, node.getNodeType());
    		FieldDeclaration fieldDeclaration = (FieldDeclaration) node;
    		final List fragments = fieldDeclaration.fragments();
    		assertEquals("Wrong size", 1, fragments.size());
    		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
    		final Expression initializer = fragment.getInitializer();
    		assertEquals("Not a null literal", ASTNode.NULL_LITERAL, initializer.getNodeType());
    		NullLiteral nullLiteral = (NullLiteral) initializer;
    		assertTrue((nullLiteral.getFlags() & ASTNode.MALFORMED) != 0);
    }
    /**
     * https://bugs.eclipse.org/bugs/show_bug.cgi?id=402673
     */
    public void test402673b() throws JavaModelException {
            String contents = """
				package test402673;\
				public class X {
				    public void foo() {
				        Runnable r = () -> System.out.println("hi");
				    }
				}
				""";
        	this.workingCopy = getWorkingCopy("/Converter/src/test402673/X.java", true/* resolve */);
        	this.workingCopy.getBuffer().setContents(contents);
        	ASTNode node = runConversion(this.workingCopy, true);
        	assertTrue(node != null);
    		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
    		CompilationUnit unit = (CompilationUnit) node;
    		assertProblemsSize(unit, 1, "Lambda expressions are allowed only at source level 1.8 or above");
    		node = getASTNode(unit, 0, 0);
    		assertEquals("Not a method declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
    		MethodDeclaration methodDecl = (MethodDeclaration) node;
    		assertTrue((methodDecl.getFlags() & ASTNode.MALFORMED) == 1);
    		node = getASTNode(unit, 0, 0, 0);
    		assertEquals("Not a variable declaration statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
    		VariableDeclarationStatement variableDecl = (VariableDeclarationStatement) node;
    		final List fragments = variableDecl.fragments();
    		assertEquals("Wrong size", 1, fragments.size());
    		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fragments.get(0);
    		final Expression initializer = fragment.getInitializer();
    		assertEquals("Not a null literal", ASTNode.NULL_LITERAL, initializer.getNodeType());
    		NullLiteral nullLiteral = (NullLiteral) initializer;
    		assertTrue((nullLiteral.getFlags() & ASTNode.MALFORMED) != 0);
    }
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=402674
	 */
	public void test403444() throws JavaModelException {
		this.workingCopy = getWorkingCopy("/Converter18/src/test403444/X.java",
				true/* resolve */);
		String contents = """
			package test403444;\
			public class X {
			  public static interface StringToInt {
			   	int stoi(String s);
			  }
			  public static interface ReduceInt {
			      int reduce(int a, int b);
			  }
			  void foo(StringToInt s) { }
			  void bar(ReduceInt r) { }
			  void bar() {
			      foo(s -> s.length());
			      foo((s) -> s.length());
			      foo((String s) -> s.length()); //SingleVariableDeclaration is OK
			      bar((x, y) -> x+y);
			      bar((int x, int y) -> x+y); //SingleVariableDeclarations are OK
			  }
			}
			""";


    	this.workingCopy = getWorkingCopy("/Converter/src/test403444/X.java", true/* resolve */);
    	this.workingCopy.getBuffer().setContents(contents);
    	ASTNode node = runConversion(this.workingCopy, true);
    	assertTrue(node != null);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;

		String error = """
			Lambda expressions are allowed only at source level 1.8 or above
			Lambda expressions are allowed only at source level 1.8 or above
			Lambda expressions are allowed only at source level 1.8 or above
			Lambda expressions are allowed only at source level 1.8 or above
			Lambda expressions are allowed only at source level 1.8 or above""";
		assertProblemsSize(unit, 5, error);

		TypeDeclaration typedeclaration = (TypeDeclaration) getASTNode(unit, 0);
		MethodDeclaration methoddecl = (MethodDeclaration)typedeclaration.bodyDeclarations().get(4);
		List statements = methoddecl.getBody().statements();
		int sCount = 0;

		ExpressionStatement statement = (ExpressionStatement)statements.get(sCount++);
		MethodInvocation methodInvocation = (MethodInvocation)statement.getExpression();
		Expression expression = (Expression) methodInvocation.arguments().get(0);
		assertTrue(expression instanceof NullLiteral);
		ITypeBinding binding = expression.resolveTypeBinding();
		assertNull(binding);

		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		expression = (Expression) methodInvocation.arguments().get(0);
		assertTrue(expression instanceof NullLiteral);
		binding = expression.resolveTypeBinding();
		assertNull(binding);

		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		expression = (Expression) methodInvocation.arguments().get(0);
		assertTrue(expression instanceof NullLiteral);
		binding = expression.resolveTypeBinding();
		assertNull(binding);

		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		expression = (Expression) methodInvocation.arguments().get(0);
		assertTrue(expression instanceof NullLiteral);
		binding = expression.resolveTypeBinding();
		assertNull(binding);

		statement = (ExpressionStatement)statements.get(sCount++);
		methodInvocation = (MethodInvocation)statement.getExpression();
		expression = (Expression) methodInvocation.arguments().get(0);
		assertTrue(expression instanceof NullLiteral);
		binding = expression.resolveTypeBinding();
		assertNull(binding);
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=399791
	 */
	public void test0021() throws JavaModelException {
		String contents =
				"""
			public interface X {
				static void foo(){}
			   default void foo(int i){}
			}
			""";
			this.workingCopy = getWorkingCopy("/Converter17/src/X.java", false);
			ASTNode node = buildAST(contents, this.workingCopy, false);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit unit = (CompilationUnit) node;
			TypeDeclaration type =  (TypeDeclaration) unit.types().get(0);
			node = (ASTNode) type.bodyDeclarations().get(0);
			assertEquals("Not a method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
			MethodDeclaration method = (MethodDeclaration) node;
			assertEquals("Method should be malformed", ASTNode.MALFORMED, (method.getFlags() & ASTNode.MALFORMED));

			method = (MethodDeclaration) type.bodyDeclarations().get(1);
			assertEquals("Method should be malformed", ASTNode.MALFORMED, (method.getFlags() & ASTNode.MALFORMED));
	}
	/**
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=405934
	 *
	 * @deprecated as it uses deprecated methods
	 */
	public void test0022() throws JavaModelException {
		String contents =
				"""
			public class X {
				void foo() throws  @NonNull EOFException, java.io.@NonNull FileNotFoundException {}
			}
			""";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", false);
		ASTNode node = buildAST(contents, this.workingCopy, false);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit unit = (CompilationUnit) node;
		TypeDeclaration type =  (TypeDeclaration) unit.types().get(0);
		node = (ASTNode) type.bodyDeclarations().get(0);
		assertEquals("Not a method Declaration", ASTNode.METHOD_DECLARATION, node.getNodeType());
		MethodDeclaration method = (MethodDeclaration) node;
		SimpleName exception1 = (SimpleName) method.thrownExceptions().get(0);
		assertEquals("QualifiedName should be malformed", ASTNode.MALFORMED, (exception1.getFlags() & ASTNode.MALFORMED));
		QualifiedName exception2 = (QualifiedName) method.thrownExceptions().get(1);
		assertEquals("QualifiedName should be malformed", ASTNode.MALFORMED, (exception2.getFlags() & ASTNode.MALFORMED));
	}
}
