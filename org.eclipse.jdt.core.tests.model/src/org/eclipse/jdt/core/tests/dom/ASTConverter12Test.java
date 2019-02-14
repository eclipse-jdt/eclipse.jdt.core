/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import junit.framework.Test;

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
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
}
