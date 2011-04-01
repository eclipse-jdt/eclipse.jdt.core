/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.dom;

import java.util.List;

import junit.framework.Test;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class ASTConverter17Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(AST.JLS4);
	}

	public ASTConverter17Test(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 7 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] {"test0001"};
	}
	public static Test suite() {
		return buildModelTestSuite(ASTConverter17Test.class);
	}

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
			"public class X {\n" +
			"	public static final int VAR = 0b001;\n" +
			"}";
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
			"public class X {\n" +
			"	public static final int VAR = 0b0_0__1;\n" +
			"}";
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
			"public class X {\n" +
			"	public static final int VAR = 1_2_3_4;\n" +
			"}";
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
			"public class X {\n" +
			"	public void foo(String s) {\n" +
			"		switch(s) {\n" +
			"			case \"Hello\" :\n" +
			"				System.out.println(s);\n" +
			"				break;\n" +
			"		}\n" +
			"	}\n" +
			"}";
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
	 * Disjunctive types (update for bug 340608)
	 */
	public void test0005() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public void foo(String s) {\n" +
			"		try {\n" +
			"			System.out.println(s);\n" +
			"			Integer.parseInt(s);\n" +
			"		} catch(NumberFormatException | Exception e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(AST.JLS4, this.workingCopy, true);
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
		assertEquals("Not a disjunctive type", ASTNode.DISJUNCTIVE_TYPE, type.getNodeType());
		checkSourceRange(type, "NumberFormatException | Exception", contents);
		ITypeBinding typeBinding = type.resolveBinding();
		assertNotNull("No binding", typeBinding);
	}
	/*
	 * Check that catch type with disjunctive type as a simple type is converted to a simple type
	 */
	public void test0006() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public void foo(String s) {\n" +
			"		try {\n" +
			"			System.out.println(s);\n" +
			"			Integer.parseInt(s);\n" +
			"		} catch(NumberFormatException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}";
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
	 * Check that catch type with disjunctive type as a simple type is converted to a simple type
	 */
	public void test0007() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public void foo(String s) {\n" +
			"		try (Reader r = new FileReader(s)) {\n" +
			"			System.out.println(s);\n" +
			"			Integer.parseInt(s);\n" +
			"		} catch(NumberFormatException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", false/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(AST.JLS4, this.workingCopy, false);
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
	 * Check that catch type with disjunctive type as a simple type is converted to a simple type
	 */
	public void test0008() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public void foo(String s) {\n" +
			"		try (Reader r = new FileReader(s);) {\n" +
			"			System.out.println(s);\n" +
			"			Integer.parseInt(s);\n" +
			"		} catch(NumberFormatException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", false/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(AST.JLS4, this.workingCopy, false);
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
		checkSourceRange((ASTNode) resources.get(0), "Reader r = new FileReader(s);", contents);
	}
	/*
	 * Check that catch type with disjunctive type as a simple type is converted to a simple type
	 */
	public void test0009() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public void foo(String s) {\n" +
			"		try (Reader r = new FileReader(s);Reader r2 = new FileReader(s);) {\n" +
			"			System.out.println(s);\n" +
			"			Integer.parseInt(s);\n" +
			"		} catch(NumberFormatException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", false/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(AST.JLS4, this.workingCopy, false);
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
		checkSourceRange((ASTNode) resources.get(0), "Reader r = new FileReader(s);", contents);
		checkSourceRange((ASTNode) resources.get(1), "Reader r2 = new FileReader(s);", contents);
	}
	/*
	 * Check that catch type with disjunctive type as a simple type is converted to a simple type
	 */
	public void test0010() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	public void foo(String s) {\n" +
			"		try (Reader r = new FileReader(s);Reader r2 = new FileReader(s)) {\n" +
			"			System.out.println(s);\n" +
			"			Integer.parseInt(s);\n" +
			"		} catch(NumberFormatException e) {\n" +
			"			e.printStackTrace();\n" +
			"		}\n" +
			"	}\n" +
			"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", false/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(AST.JLS4, this.workingCopy, false);
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
		checkSourceRange((ASTNode) resources.get(0), "Reader r = new FileReader(s);", contents);
		checkSourceRange((ASTNode) resources.get(1), "Reader r2 = new FileReader(s)", contents);
	}
	/*
	 * Disjunctive types (update for bug 340608)
	 */
	public void test0011() throws JavaModelException {
		String contents =
			"public class X {\n" + 
			"    public static void main(String[] args) {\n" + 
			"        try {\n" + 
			"            int option= 1;\n" + 
			"            throw option == 1 ? new ExceptionA() : new ExceptionB();\n" + 
			"        } catch (/*final*/ ExceptionA | ExceptionB ex) {\n" + 
			"            System.out.println(\"type of ex: \" + ex.getClass());\n" + 
			"            // next 2 methods on 'ex' use different parts of lub:\n" + 
			"            ex.myMethod();\n" + 
			"            throw ex;\n" + 
			"        }\n" + 
			"    }\n" + 
			"}\n" + 
			"interface Mix {\n" + 
			"    public void myMethod();\n" + 
			"}\n" + 
			"class ExceptionA extends RuntimeException implements Mix {\n" + 
			"    public void myMethod() {\n" + 
			"        System.out.println(\"ExceptionA.myMethod()\");\n" + 
			"    }\n" + 
			"    public void onlyA() {\n" + 
			"        System.out.println(\"ExceptionA.onlyA()\");\n" + 
			"    }\n" + 
			"}\n" + 
			"class ExceptionB extends RuntimeException implements Mix {\n" + 
			"    public void myMethod() {\n" + 
			"        System.out.println(\"ExceptionB.myMethod()\");\n" + 
			"    }\n" + 
			"    public void onlyB() {\n" + 
			"        System.out.println(\"ExceptionA.onlyB()\");\n" + 
			"    }\n" + 
			"}";
		this.workingCopy = getWorkingCopy("/Converter17/src/X.java", true/*resolve*/);
		this.workingCopy.getBuffer().setContents(contents);
		ASTNode node = runConversion(AST.JLS4, this.workingCopy, true);
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
		assertEquals("Not a disjunctive type", ASTNode.DISJUNCTIVE_TYPE, type.getNodeType());
		checkSourceRange(type, "ExceptionA | ExceptionB", contents);
		ITypeBinding typeBinding = type.resolveBinding();
		assertNotNull("No binding", typeBinding);
	}
}
