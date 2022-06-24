/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
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
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import junit.framework.Test;

public class ASTConverter_RecordPattern_Test extends ConverterTestSetup {

	ICompilationUnit workingCopy;

	public void setUpSuite() throws Exception {
		super.setUpSuite();
		this.ast = AST.newAST(getAST19(), true);
		this.currentProject = getJavaProject("Converter_19");
		if (this.ast.apiLevel() == AST.JLS19 ) {
			this.currentProject.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_19);
			this.currentProject.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_19);
			this.currentProject.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_19);
			this.currentProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			this.currentProject.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);
		}
	}

	public ASTConverter_RecordPattern_Test(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(ASTConverter_RecordPattern_Test.class);
	}

	static int getAST19() {
		return AST.JLS19;
	}
	protected void tearDown() throws Exception {
		super.tearDown();
		if (this.workingCopy != null) {
			this.workingCopy.discardWorkingCopy();
			this.workingCopy = null;
		}
	}


	public void _testRecordPattern001() throws CoreException {
		if (!isJRE19) {
			System.err.println("Test "+getName()+" requires a JRE 19");
			return;
		}
		String contents = "" +
						"public class X {\n"
						+ "  static void print(Rectangle r) {\n"
						+ "    if (r instanceof (Rectangle(ColoredPoint(Point(int x, int y), Color c),\n"
						+ "                               ColoredPoint lr) r1)) {\n"
						+ "        System.out.println(\"Upper-left corner: \" + r1);\n"
						+ "    }\n"
						+ "  }\n"
						+ "  public static void main(String[] obj) {\n"
						+ "    print(new Rectangle(new ColoredPoint(new Point(0, 0), Color.BLUE), \n"
						+ "                               new ColoredPoint(new Point(10, 15), Color.RED)));\n"
						+ "  }\n"
						+ "}\n"
						+ "record Point(int x, int y) {}\n"
						+ "enum Color { RED, GREEN, BLUE }\n"
						+ "record ColoredPoint(Point p, Color c) {}\n"
						+ "record Rectangle(ColoredPoint upperLeft, ColoredPoint lowerRight) {}";
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
	}

	public void testRecordPattern002() throws CoreException {
		if (!isJRE19) {
			System.err.println("Test "+getName()+" requires a JRE 19");
			return;
		}
		String contents = ""
				+ "public class X {\n"
				+ "  public static void print(Record r) {\n"
				+ "    int res = switch(r) {\n"
				+ "       case Record(int x) r1 -> x ;\n"
				+ "        default -> 0;\n"
				+ "    }; \n"
				+ "    System.out.println(\"Returns: \" + res);\n"
				+ "  }\n"
				+ "  public static void main(String[] args) {\n"
				+ "    print(new Record(3));\n"
				+ "  }\n"
				+ "}\n"
				+ "record Record(int x) {}";

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
	}


}
