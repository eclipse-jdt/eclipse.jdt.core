/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import junit.framework.Test;

@SuppressWarnings("rawtypes")
public class ASTConverter12Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getAST12());
	}

	public ASTConverter12Test(String name) {
		super(name);
	}

	static {
//		TESTS_NUMBERS = new int[] { 19 };
//		TESTS_RANGE = new int[] { 1, -1 };
//		TESTS_NAMES = new String[] {"test0001"};
	}
	public static Test suite() {
		return buildModelTestSuite(ASTConverter12Test.class);
	}
	
	static int getAST12() {
		return AST.JLS12;
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}
	/*
	 * Test that a simple switch expression's return type holds the correct type
	 */
	public void test0001() throws JavaModelException {
		String contents =
			"	public class X {\n" +
			"   enum Day\n" +
			"   {\n" + 
			"   	SUNDAY, MONDAY, TUESDAY, WEDNESDAY,\n" + 
			"   	THURSDAY, FRIDAY, SATURDAY;\n" + 
			"	}\n" +
			"	public static void main(String[] args) {\n" + 
			"		Day day = Day.SUNDAY;\n" +
			"		int k = switch (day) {\n" + 
			"    	case MONDAY  -> throw new NullPointerException();\n" + 
			"    	case TUESDAY -> 1;\n" + 
			"\n" +     
			"	 	case WEDNESDAY -> {break 10;}\n" + 
			"    	default      -> {\n" +
			"        	int g = day.toString().length();\n" +
			"        	break g;\n" +
			"   	}};\n" +
			"   	System.out.println(k);\n" +
			"	}\n" +
			"}" ;
		this.workingCopy = getWorkingCopy("/Converter12/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = getASTNode(compilationUnit, 0, 1);
			MethodDeclaration methodDeclaration = (MethodDeclaration) node;
			VariableDeclarationStatement vStmt1 = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(1);
			Type type = vStmt1.getType();
			IBinding binding = type.resolveBinding();
			assertTrue("null binding", binding != null);
			assertTrue("binding incorrect", binding.getName().equals("int"));
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	/*
	 * Test that a case statement with multiple cases is resolved correctly
	 * and has the correct source range
	 */
	public void test0002() throws JavaModelException {
		String contents =
			"public class X {\n" + 
			"	static enum Day {MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY, SATURDAY,SUNDAY}\n" + 
			"	String aa(Day day) throws Exception {\n" + 
			"		var today = \"\";\n" + 
			"		switch (day) {\n" + 
			"			case SATURDAY,SUNDAY ->\n" + 
			"				today=\"Weekend\";\n" + 
			"			case MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY ->\n" + 
			"				today=\"Working\";\n" + 
			"			default ->\n" + 
			"				throw new Exception(\"Invalid day: \" + day.name());\n" + 
			"		}\n" + 
			"		return today;\n" + 
			"	}\n" + 
			"	\n" + 
			"	String bb(Day day) throws Exception {\n" + 
			"		var today = \"\";\n" + 
			"		switch (day) {\n" + 
			"			case SATURDAY,SUNDAY:\n" + 
			"				today = \"Weekend day\";\n" + 
			"				break;\n" + 
			"			case MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY:\n" + 
			"				today = \"Working day\";\n" + 
			"				break;\n" + 
			"			default:\n" + 
			"				throw new Exception(\"Invalid day: \" + day.name());\n" + 
			"		}\n" + 
			"		return today;\n" + 
			"	}\n" + 
			"}" ;
		this.workingCopy = getWorkingCopy("/Converter12/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
				contents,
				this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = getASTNode(compilationUnit, 0, 1, 1);
			assertEquals("Switch statement", node.getNodeType(), ASTNode.SWITCH_STATEMENT);
			SwitchStatement switchStatement = (SwitchStatement) node;
			checkSourceRange((Statement) switchStatement.statements().get(0), "case SATURDAY,SUNDAY ->", contents);
			checkSourceRange((Statement) switchStatement.statements().get(2), "case MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY ->", contents);
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	
	/* test implicit break statement */

	public void test0003() throws JavaModelException {
		String contents =
			"public class X {\n" +
			"	static enum Day {MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY, SATURDAY,SUNDAY}\n" +
			"	String aa(Day day) throws Exception {\n" +
			"		var today = \"\";\n" +
			"		switch (day) {\n" +
			"			case SATURDAY,SUNDAY ->\n" +
			"				today=\"Weekend\";\n" +
			"			case MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY ->\n" +
			"				today=\"Working\";\n" +
			"			default ->\n" +
			"				throw new Exception(\"Invalid day: \" + day.name());\n" +
			"		}\n" +
			"		return today;\n" +
			"	}\n" +
			"	\n" +
			"	String bb(Day day) throws Exception {\n" +
			"		String today = \"\";\n" +
			"		today = switch (day) {\n" +
			"			case SATURDAY,SUNDAY:\n" +
			"				break \"Weekend day\";\n" +
			"			case MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY:\n" +
			"				break \"Week day\";\n" +
			"			default:\n" +
			"				break \"Any day\";\n" +
			"		};\n" +
			"		return today;\n" +
			"	}\n" +
			"}" ;
		this.workingCopy = getWorkingCopy("/Converter12/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
					contents,
					this.workingCopy);
				assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
				CompilationUnit compilationUnit = (CompilationUnit) node;
				assertProblemsSize(compilationUnit, 0);
				node = getASTNode(compilationUnit, 0, 1, 1);
				assertEquals("Switch statement", node.getNodeType(), ASTNode.SWITCH_STATEMENT);
				SwitchStatement switchStatement = (SwitchStatement) node;
				checkSourceRange((Statement) switchStatement.statements().get(0), "case SATURDAY,SUNDAY ->", contents);
			
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	public void test0004() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	static enum Day {MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY, SATURDAY,SUNDAY}\n" +
				"	String bb(Day day) throws Exception {\n" +
				"		String today = switch (day) {\n" +
				"			case SATURDAY,SUNDAY:\n" +
				"				break \"Weekend day\";\n" +
				"			case MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY:\n" +
				"				break \"Week day\";\n" +
				"			default:\n" +
				"				break \"Any day\";\n" +
				"		};\n" +
				"		return today;\n" +
				"	}\n" +
				"}" ;
		this.workingCopy = getWorkingCopy("/Converter12/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
					contents,
					this.workingCopy);
				assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
				CompilationUnit compilationUnit = (CompilationUnit) node;
				assertProblemsSize(compilationUnit, 0);
				node = getASTNode(compilationUnit, 0, 1, 0);
				assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
				List fragments = ((VariableDeclarationStatement) node).fragments();
				assertEquals("Incorrect no of fragments", 1, fragments.size());
				node = (ASTNode) fragments.get(0);
				assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
				VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
				Expression initializer = fragment.getInitializer();
				assertEquals("incorrect type", ASTNode.SWITCH_EXPRESSION, initializer.getNodeType());
				Expression expression = ((SwitchExpression) initializer).getExpression();
				assertEquals("incorrect type", ASTNode.SIMPLE_NAME, expression.getNodeType());
				assertEquals("incorrect name", "day", ((SimpleName) expression).getFullyQualifiedName());
				List statements = ((SwitchExpression) initializer).statements();
				assertEquals("incorrect no of statements", 6, statements.size());
				BreakStatement brStmt = (BreakStatement) statements.get(1);
				Expression expression2 = brStmt.getExpression();
				assertNotNull("should not null", expression2);
				assertEquals("incorrect node type", ASTNode.STRING_LITERAL, expression2.getNodeType());
				
				//default case:
				SwitchCase caseStmt = (SwitchCase) statements.get(4);
				assertTrue("not default", caseStmt.isDefault());
				brStmt = (BreakStatement) statements.get(5);
				expression2 = brStmt.getExpression();
				assertNotNull("should not null", expression2);
				assertEquals("incorrect node type", ASTNode.STRING_LITERAL, expression2.getNodeType());
			
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	public void test0005() throws JavaModelException {
		String contents =
				"public class X {\n" +
				"	public String test001() {\n" + 
				"		int i = 0;\n" + 
				"		String ret = switch(i%2) {\n" + 
				"		case 0 -> \"odd\";\n" + 
				"		case 1 -> \"even\";\n" + 
				"		default -> \"\";\n" + 
				"		};\n" + 
				"		return ret;\n" + 
				"	}" +
				"}" ;
		this.workingCopy = getWorkingCopy("/Converter12/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
					contents,
					this.workingCopy);
				assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
				CompilationUnit compilationUnit = (CompilationUnit) node;
				assertProblemsSize(compilationUnit, 0);
				node = getASTNode(compilationUnit, 0, 0, 1);
				assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
				List fragments = ((VariableDeclarationStatement) node).fragments();
				assertEquals("Incorrect no of fragments", 1, fragments.size());
				node = (ASTNode) fragments.get(0);
				assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
				VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
				Expression initializer = fragment.getInitializer();
				assertEquals("incorrect type", ASTNode.SWITCH_EXPRESSION, initializer.getNodeType());
				Expression expression = ((SwitchExpression) initializer).getExpression();
				assertEquals("incorrect type", ASTNode.INFIX_EXPRESSION, expression.getNodeType());
				List statements = ((SwitchExpression) initializer).statements();
				assertEquals("incorrect no of statements", 6, statements.size());
				BreakStatement brStmt = (BreakStatement) statements.get(1);
				Expression expression2 = brStmt.getExpression();
				assertNotNull("should not null", expression2);
				assertEquals("incorrect node type", ASTNode.STRING_LITERAL, expression2.getNodeType());

				//default case:
				SwitchCase caseStmt = (SwitchCase) statements.get(4);
				assertTrue("not default", caseStmt.isDefault());
				brStmt = (BreakStatement) statements.get(5);
				expression2 = brStmt.getExpression();
				assertNotNull("should not null", expression2);
				assertEquals("incorrect node type", ASTNode.STRING_LITERAL, expression2.getNodeType());
			
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	public void test0006() throws JavaModelException {
		String contents =
				"public class X {\n" +
						"	public String test001() {\n" + 
						"		int i = 0;\n" + 
						"		String ret = switch(i%2) {\n" + 
						"		case 0 -> {return \"odd\"; }\n" + 
						"		case 1 -> \"even\";\n" + 
						"		default -> \"\";\n" + 
						"		};\n" + 
						"		return ret;\n" + 
						"	}" +
						"}" ;
		this.workingCopy = getWorkingCopy("/Converter12/src/X.java", true/*resolve*/);
		IJavaProject javaProject = this.workingCopy.getJavaProject();
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			javaProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
			ASTNode node = buildAST(
					contents,
					this.workingCopy);
			assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
			CompilationUnit compilationUnit = (CompilationUnit) node;
			assertProblemsSize(compilationUnit, 0);
			node = getASTNode(compilationUnit, 0, 0, 1);
			assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_STATEMENT);
			List fragments = ((VariableDeclarationStatement) node).fragments();
			assertEquals("Incorrect no of fragments", 1, fragments.size());
			node = (ASTNode) fragments.get(0);
			assertEquals("Switch statement", node.getNodeType(), ASTNode.VARIABLE_DECLARATION_FRAGMENT);
			VariableDeclarationFragment fragment = (VariableDeclarationFragment) node;
			Expression initializer = fragment.getInitializer();
			List statements = ((SwitchExpression) initializer).statements();
			assertEquals("incorrect no of statements", 6, statements.size());
			Block block = (Block) statements.get(1);
			statements = block.statements();
			assertEquals("incorrect no of statements", 1, statements.size());
			Statement stmt = (Statement) statements.get(0);
			assertEquals("incorrect node type", ASTNode.RETURN_STATEMENT, stmt.getNodeType());

		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
}
