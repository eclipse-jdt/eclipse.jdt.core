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
package org.eclipse.jdt.core.tests.dom;

import java.io.IOException;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JavaElement;

/**
 * Test suite to verify that DOM/AST bugs are fixed.
 *
 * Note that only specific JLS3 tests are defined in this test suite, but when
 * running it, all superclass {@link ASTConverterBugsTest} tests will be run
 * as well.
 */
@SuppressWarnings("rawtypes")
public class ASTConverterBugsTestJLS3 extends ASTConverterBugsTest {

public ASTConverterBugsTestJLS3(String name) {
    super(name);
    this.testLevel = getJLS3();
}

public static Test suite() {
	TestSuite suite = new Suite(ASTConverterBugsTestJLS3.class.getName());
	List tests = buildTestsList(ASTConverterBugsTestJLS3.class, 1, 0/* do not sort*/);
	for (int index=0, size=tests.size(); index<size; index++) {
		suite.addTest((Test)tests.get(index));
	}
	return suite;
}

/**
 * @bug 130778: Invalid annotation elements cause no annotation to be in the AST
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=130778"
 */
public void testBug130778a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"package a;\n" +
			"public @interface AnAnnotation2 {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*]public[*1*]\n" +
			"[*2*]@AnAnnotation(\"a\")[*2*]\n" +
			"[*3*]final[*3*]\n" +
			"[*4*]@AnAnnotation2(\"b\")[*4*]\n" +
			"class X {\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*]public[*1*] [*2*]@AnAnnotation(\"a\")[*2*] [*3*]final[*3*] [*4*]@AnAnnotation2(\"b\")[*4*] class X {\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:MODIFIER,[11,6],,,[N/A]\n" +
			"2:SINGLE_MEMBER_ANNOTATION,[18,18],,,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"3:MODIFIER,[37,5],,,[N/A]\n" +
			"4:SINGLE_MEMBER_ANNOTATION,[43,19],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]\n" +
			"===== Problems =====\n" +
			"No problem",
			result);
}
public void testBug130778b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"package a;\n" +
			"public @interface AnAnnotation2 {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"#\n" +
			"[*1*]public[*1*]\n" +
			"[*2*]@AnAnnotation(\"a\")[*2*]\n" +
			"[*3*]final[*3*]\n" +
			"[*4*]@AnAnnotation2(\"b\")[*4*]\n" +
			"class X {\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*]public[*1*] [*2*]@AnAnnotation(\"a\")[*2*] [*3*]final[*3*] [*4*]@AnAnnotation2(\"b\")[*4*] class X {\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:MODIFIER,[13,6],,,[N/A]\n" +
			"2:SINGLE_MEMBER_ANNOTATION,[20,18],,,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"3:MODIFIER,[39,5],,,[N/A]\n" +
			"4:SINGLE_MEMBER_ANNOTATION,[45,19],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 2)\n" +
			"	#\n" +
			"	^\n" +
			"Syntax error on token \"Invalid Character\", delete this token\n",
			result);
}
public void testBug130778c() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"package a;\n" +
			"public @interface AnAnnotation2 {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*]public[*1*]\n" +
			"#\n" +
			"[*2*]@AnAnnotation(\"a\")[*2*]\n" +
			"[*3*]final[*3*]\n" +
			"[*4*]@AnAnnotation2(\"b\")[*4*]\n" +
			"class X {\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*]public[*1*] [*2*]@AnAnnotation(\"a\")[*2*] [*3*]final[*3*] [*4*]@AnAnnotation2(\"b\")[*4*] class X {\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:MODIFIER,[11,6],,,[N/A]\n" +
			"2:SINGLE_MEMBER_ANNOTATION,[20,18],,,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"3:MODIFIER,[39,5],,,[N/A]\n" +
			"4:SINGLE_MEMBER_ANNOTATION,[45,19],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 3)\n" +
			"	#\n" +
			"	^\n" +
			"Syntax error on token \"Invalid Character\", delete this token\n",
			result);
}
public void testBug130778d() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"package a;\n" +
			"public @interface AnAnnotation2 {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*]public[*1*]\n" +
			"[*2*]@AnAnnotation(\"a\")[*2*]\n" +
			"#\n" +
			"[*3*]final[*3*]\n" +
			"[*4*]@AnAnnotation2(\"b\")[*4*]\n" +
			"class X {\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*]public[*1*] [*2*]@AnAnnotation(\"a\")[*2*] [*3*]final[*3*] [*4*]@AnAnnotation2(\"b\")[*4*] class X {\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:MODIFIER,[11,6],,,[N/A]\n" +
			"2:SINGLE_MEMBER_ANNOTATION,[18,18],,,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"3:MODIFIER,[39,5],,,[N/A]\n" +
			"4:SINGLE_MEMBER_ANNOTATION,[45,19],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 4)\n" +
			"	#\n" +
			"	^\n" +
			"Syntax error on token \"Invalid Character\", delete this token\n",
			result);
}
public void testBug130778e() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"package a;\n" +
			"public @interface AnAnnotation2 {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*]public[*1*]\n" +
			"[*2*]@AnAnnotation(\"a\")[*2*]\n" +
			"[*3*]final[*3*]\n" +
			"#\n" +
			"[*4*]@AnAnnotation2(\"b\")[*4*]\n" +
			"class X {\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*]public[*1*] [*2*]@AnAnnotation(\"a\")[*2*] [*3*]final[*3*] [*4*]@AnAnnotation2(\"b\")[*4*] class X {\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:MODIFIER,[11,6],,,[N/A]\n" +
			"2:SINGLE_MEMBER_ANNOTATION,[18,18],,,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"3:MODIFIER,[37,5],,,[N/A]\n" +
			"4:SINGLE_MEMBER_ANNOTATION,[45,19],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 5)\n" +
			"	#\n" +
			"	^\n" +
			"Syntax error on token \"Invalid Character\", delete this token\n",
			result);
}
public void testBug130778f() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"package a;\n" +
			"public @interface AnAnnotation2 {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*]public[*1*]\n" +
			"[*2*]@AnAnnotation(\"a\")[*2*]\n" +
			"[*3*]final[*3*]\n" +
			"[*4*]@AnAnnotation2(\"b\")[*4*]\n" +
			"#\n" +
			"class X {\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*]public[*1*] [*2*]@AnAnnotation(\"a\")[*2*] [*3*]final[*3*] [*4*]@AnAnnotation2(\"b\")[*4*] class X {\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:MODIFIER,[11,6],,,[N/A]\n" +
			"2:SINGLE_MEMBER_ANNOTATION,[18,18],,,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"3:MODIFIER,[37,5],,,[N/A]\n" +
			"4:SINGLE_MEMBER_ANNOTATION,[43,19],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 6)\n" +
			"	#\n" +
			"	^\n" +
			"Syntax error on token \"Invalid Character\", delete this token\n",
			result);
}
public void testBug130778g() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"package a;\n" +
			"public @interface AnAnnotation2 {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*]public[*1*]\n" +
			"[*2*]@AnAnnotation(\"a\")[*2*]\n" +
			"[*3*]final[*3*]\n" +
			"[*4*]@AnAnnotation2(\"b\")[*4*]\n" +
			"class X {\n" +
			"  #\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*]public[*1*] [*2*]@AnAnnotation(\"a\")[*2*] [*3*]final[*3*] [*4*]@AnAnnotation2(\"b\")[*4*] class X {\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:MODIFIER,[11,6],,,[N/A]\n" +
			"2:SINGLE_MEMBER_ANNOTATION,[18,18],,,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"3:MODIFIER,[37,5],,,[N/A]\n" +
			"4:SINGLE_MEMBER_ANNOTATION,[43,19],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 7)\n" +
			"	#\n" +
			"	^\n" +
			"Syntax error on token \"Invalid Character\", delete this token\n",
			result);
}
public void testBug130778h() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"package a;\n" +
			"public @interface AnAnnotation2 {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*]@AnAnnotation(value=\"a\")[*1*]\n" +
			"[*2*]@AnAnnotation2(value=\"b\")[*2*]\n" +
			"[*3*]public[*3*] class X {\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*]@AnAnnotation(value=\"a\")[*1*] [*2*]@AnAnnotation2(value=\"b\")[*2*] [*3*]public[*3*] class X {\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:NORMAL_ANNOTATION,[11,24],,,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"2:NORMAL_ANNOTATION,[36,25],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]\n" +
			"3:MODIFIER,[62,6],,,[N/A]\n" +
			"===== Problems =====\n" +
			"No problem",
			result);
}
public void testBug130778i() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"package a;\n" +
			"public @interface AnAnnotation2 {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*]@AnAnnotation(value=[*1*])\n" +
			"[*2*]@AnAnnotation2(value=\"b\")[*2*]\n" +
			"[*3*]public[*3*] class X {\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*]@AnAnnotation(value=$missing$)[*1*] [*2*]@AnAnnotation2(value=\"b\")[*2*] [*3*]public[*3*] class X {\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:NORMAL_ANNOTATION,[11,20],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"2:NORMAL_ANNOTATION,[33,25],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]\n" +
			"3:MODIFIER,[59,6],,,[N/A]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 2)\n" +
			"	@AnAnnotation(value=)\n" +
			"	                   ^\n" +
			"Syntax error on token \"=\", MemberValue expected after this token\n",
			result);
}
public void testBug130778j() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"package a;\n" +
			"public @interface AnAnnotation2 {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*]@AnAnnotation(value=\"a\")[*1*]\n" +
			"[*2*]@AnAnnotation2(value=[*2*])\n" +
			"[*3*]public[*3*] class X {\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*]@AnAnnotation(value=\"a\")[*1*] [*2*]@AnAnnotation2(value=$missing$)[*2*] [*3*]public[*3*] class X {\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:NORMAL_ANNOTATION,[11,24],,,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"2:NORMAL_ANNOTATION,[36,21],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation2;,]\n" +
			"3:MODIFIER,[59,6],,,[N/A]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 3)\n" +
			"	@AnAnnotation2(value=)\n" +
			"	                    ^\n" +
			"Syntax error on token \"=\", MemberValue expected after this token\n",
			result);
}
public void testBug130778k() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"package a;\n" +
			"public @interface AnAnnotation2 {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*]@AnAnnotation(value=[*1*])\n" +
			"[*2*]@AnAnnotation2(value=[*2*])\n" +
			"[*3*]public[*3*] class X {\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*]@AnAnnotation(value=$missing$)[*1*] [*2*]@AnAnnotation2(value=$missing$)[*2*] [*3*]public[*3*] class X {\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:NORMAL_ANNOTATION,[11,20],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"2:NORMAL_ANNOTATION,[33,21],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation2;,]\n" +
			"3:MODIFIER,[56,6],,,[N/A]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 2)\n" +
			"	@AnAnnotation(value=)\n" +
			"	                    ^\n" +
			"Syntax error on token \")\", delete this token\n" +
			"2. ERROR in /Converter15/src/a/X.java (at line 3)\n" +
			"	@AnAnnotation2(value=)\n" +
			"	                    ^\n" +
			"Syntax error on token \"=\", ) expected\n",
			result);
}
public void testBug130778l() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"package a;\n" +
			"public @interface AnAnnotation2 {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"public class X {\n" +
			"  public void foo(){\n" +
			"    [*1*]@AnAnnotation(value=[*1*])\n" +
			"    [*2*]@AnAnnotation2(value=\"b\")[*2*]\n" +
			"    class Y {}\n" +
			"  }\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"public class X {\n" +
			"  public void foo(){\n" +
			"[*1*]@AnAnnotation(value=$missing$)[*1*] [*2*]@AnAnnotation2(value=\"b\")[*2*] class Y {\n" +
			"    }\n" +
			"  }\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:NORMAL_ANNOTATION,[53,20],,RECOVERED,[ANNOTATION,La/X$115$Y;@La/AnAnnotation;,]\n" +
			"2:NORMAL_ANNOTATION,[79,25],,,[ANNOTATION,La/X$115$Y;@La/AnAnnotation2;,]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 4)\n" +
			"	@AnAnnotation(value=)\n" +
			"	                   ^\n" +
			"Syntax error on token \"=\", MemberValue expected after this token\n",
			result);
}
public void testBug130778m() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"package a;\n" +
			"public @interface AnAnnotation2 {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"public class X {\n" +
			"  public void foo(){\n" +
			"    [*1*]@AnAnnotation(value=)[*1*]\n" +
			"    [*2*]@AnAnnotation2(value=\"b\")[*2*]\n" +
			"    int i;\n" +
			"  }\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"public class X {\n" +
			"  public void foo(){\n" +
			"    [*1*]@AnAnnotation(value=$missing$)[*1*] [*2*]@AnAnnotation2(value=\"b\")[*2*] int i;\n" +
			"  }\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:NORMAL_ANNOTATION,[53,21],,,[ANNOTATION,La/X;.foo()V#i@La/AnAnnotation;,]\n" +
			"2:NORMAL_ANNOTATION,[79,25],,,[ANNOTATION,La/X;.foo()V#i@La/AnAnnotation2;,]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 4)\n" +
			"	@AnAnnotation(value=)\n" +
			"	                   ^\n" +
			"Syntax error on token \"=\", MemberValue expected after this token\n",
			result);
}
public void testBug130778n() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String name1();\n" +
			"  String name2();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*][*2*]@AnAnnotation([*3*]name1=\"a\"[*3*][*2*], name2)\n" +
			"public class X {\n" +
			"}[*1*]\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*][*2*]@AnAnnotation([*3*]name1=\"a\"[*3*])[*2*] public class X {\n" +
			"}[*1*]\n" +
			"\n" +
			"===== Details =====\n" +
			"1:TYPE_DECLARATION,[11,50],,MALFORMED|RECOVERED,[TYPE,La/X;,]\n" +
			"2:NORMAL_ANNOTATION,[11,23],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"3:MEMBER_VALUE_PAIR,[25,9],,,[N/A]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 2)\n" +
			"	@AnAnnotation(name1=\"a\", name2)\n" +
			"	                       ^\n" +
			"Syntax error on token \",\", . expected\n",
			result);
}
public void testBug130778o() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String name1();\n" +
			"  String name2();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*][*2*]@AnAnnotation([*3*]name1=[*3*][*2*])\n" +
			"public class X {\n" +
			"}[*1*]\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*][*2*]@AnAnnotation([*3*]name1=$missing$[*3*])[*2*] public class X {\n" +
			"}[*1*]\n" +
			"\n" +
			"===== Details =====\n" +
			"1:TYPE_DECLARATION,[11,40],,MALFORMED,[TYPE,La/X;,]\n" +
			"2:NORMAL_ANNOTATION,[11,20],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"3:MEMBER_VALUE_PAIR,[25,6],,RECOVERED,[N/A]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 2)\n" +
			"	@AnAnnotation(name1=)\n" +
			"	                   ^\n" +
			"Syntax error on token \"=\", MemberValue expected after this token\n",
			result);
}
public void testBug130778p() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  AnAnnotation2 value();\n" +
			"}\n",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"package a;\n" +
			"public @interface AnAnnotation2 {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*]@AnAnnotation([*2*]value=[*3*]@AnAnnotation2(value=\"a\")[*3*][*2*])[*1*]\n" +
			"public class X {\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*]@AnAnnotation([*2*]value=[*3*]@AnAnnotation2(value=\"a\")[*3*][*2*])[*1*] public class X {\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:NORMAL_ANNOTATION,[11,46],,,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"2:MEMBER_VALUE_PAIR,[25,31],,,[N/A]\n" +
			"3:NORMAL_ANNOTATION,[31,25],,,[ANNOTATION,@La/AnAnnotation2;,]\n" +
			"===== Problems =====\n" +
			"No problem",
			result);
}
public void testBug130778q() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  AnAnnotation2 value();\n" +
			"}\n",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"package a;\n" +
			"public @interface AnAnnotation2 {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*]@AnAnnotation([*2*]value=[*3*]@AnAnnotation2(value=\"a\")[*3*][*2*][*1*]\n" +
			"public class X {\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*]@AnAnnotation([*2*]value=[*3*]@AnAnnotation2(value=\"a\")[*3*][*2*])[*1*] public class X {\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:NORMAL_ANNOTATION,[11,45],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"2:MEMBER_VALUE_PAIR,[25,31],,,[N/A]\n" +
			"3:NORMAL_ANNOTATION,[31,25],,RECOVERED,[ANNOTATION,@La/AnAnnotation2;,]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 2)\n" +
			"	@AnAnnotation(value=@AnAnnotation2(value=\"a\")\n" +
			"	                                            ^\n" +
			"Syntax error, insert \")\" to complete Modifiers\n",
			result);
}
public void testBug130778r() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  AnAnnotation2 value();\n" +
			"}\n",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"package a;\n" +
			"public @interface AnAnnotation2 {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*][*2*]@AnAnnotation(value=[*2*][*3*]@AnAnnotation2(value=[*3*]))\n" +
			"public class X {\n" +
			"}[*1*]\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*][*2*]@AnAnnotation(value=$missing$)[*2*] public class X {\n" +
			"}[*1*]\n" +
			"\n" +
			"===== Details =====\n" +
			"1:TYPE_DECLARATION,[11,62],,MALFORMED|RECOVERED,[TYPE,La/X;,]\n" +
			"2:NORMAL_ANNOTATION,[11,20],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"3:No corresponding node\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 2)\n" +
			"	@AnAnnotation(value=@AnAnnotation2(value=))\n" +
			"	                                        ^\n" +
			"Syntax error on token \"=\", MemberValue expected after this token\n",
			result);
}
public void testBug130778s() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String value1();\n" +
			"  AnAnnotation2 value2();\n" +
			"}\n",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"package a;\n" +
			"public @interface AnAnnotation2 {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*][*2*]@AnAnnotation([*3*]value1=[*3*][*2*],[*4*]value=[*5*]@AnAnnotation2(value=\"b\")[*5*][*4*])\n" +
			"public class X {\n" +
			"}[*1*]\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*][*2*]@AnAnnotation([*3*]value1=$missing$[*3*])[*2*] [*5*]@AnAnnotation2(value=\"b\")[*5*] public class X {\n" +
			"}[*1*]\n" +
			"\n" +
			"===== Details =====\n" +
			"1:TYPE_DECLARATION,[11,73],,MALFORMED,[TYPE,La/X;,]\n" +
			"2:NORMAL_ANNOTATION,[11,21],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"3:MEMBER_VALUE_PAIR,[25,7],,RECOVERED,[N/A]\n" +
			"5:NORMAL_ANNOTATION,[39,25],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]\n" +
			"4:No corresponding node\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 2)\n" +
			"	@AnAnnotation(value1=,value=@AnAnnotation2(value=\"b\"))\n" +
			"	                    ^\n" +
			"Syntax error on token \"=\", MemberValue expected after this token\n",
			result);
}
public void testBug130778t() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*]@AnAnnotation(\"b\")[*1*]\n" +
			"public class X {\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*]@AnAnnotation(\"b\")[*1*] public class X {\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:SINGLE_MEMBER_ANNOTATION,[11,18],,,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"===== Problems =====\n" +
			"No problem",
			result);
}
public void testBug130778u() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*]@AnAnnotation([*2*]\"b\"[*2*][*1*]\n" +
			"public class X {\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*]@AnAnnotation([*2*]\"b\"[*2*])[*1*] public class X {\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:SINGLE_MEMBER_ANNOTATION,[11,17],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"2:STRING_LITERAL,[25,3],,,[N/A]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 2)\n" +
			"	@AnAnnotation(\"b\"\n" +
			"	              ^^^\n" +
			"Syntax error, insert \")\" to complete Modifiers\n",
			result);
}
public void testBug130778v() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  AnAnnotation2 value();\n" +
			"}\n",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"package a;\n" +
			"public @interface AnAnnotation2 {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*]@AnAnnotation[*1*](@AnAnnotation2(\"b\"\n" +
			"public class X {\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*]@AnAnnotation[*1*] public class X {\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:MARKER_ANNOTATION,[11,13],,,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 2)\n" +
			"	@AnAnnotation(@AnAnnotation2(\"b\"\n" +
			"	                             ^^^\n" +
			"Syntax error, insert \")\" to complete SingleMemberAnnotation\n" +
			"2. ERROR in /Converter15/src/a/X.java (at line 2)\n" +
			"	@AnAnnotation(@AnAnnotation2(\"b\"\n" +
			"	                             ^^^\n" +
			"Syntax error, insert \")\" to complete Modifiers\n",
			result);
}
public void testBug130778x() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"package a;\n" +
			"public @interface AnAnnotation {\n" +
			"  String value();\n" +
			"}\n",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"[*1*]@AnAnnotation([*2*]\"a\"[*2*][*1*], [*3*]\"b\"[*3*])\n" +
			"public class X {\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"[*1*]@AnAnnotation([*2*]\"a\"[*2*])[*1*] public class X {\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:SINGLE_MEMBER_ANNOTATION,[11,17],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation;,]\n" +
			"2:STRING_LITERAL,[25,3],,,[N/A]\n" +
			"3:No corresponding node\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 2)\n" +
			"	@AnAnnotation(\"a\", \"b\")\n" +
			"	                 ^\n" +
			"Syntax error on token \",\", < expected\n",
			result);

}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=388137
public void testbug388137() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	IJavaProject project = createJavaProject("P1", new String[] {""}, new String[] {"CONVERTER_JCL15_LIB"}, "", "1.5");
	try {
		String contents = "package p;\n" +
							"import java.util.List;\n" +
							"public class X {\n" +
							"	public X(List list) {}\n" +
							"	public static class ListHandler implements Handler {\n" +
							"		List list = null;\n" +
							"		public ListHandler(List list) {\n" +
							" 	 		this.list = list;\n" +
							"		}\n" +
							"	}\n" +
							"}\n" +
							"interface Handler {}\n";
		addLibrary(project, "lib.jar", "src.zip", new String[] {"/P1/p/X.java", contents}, "1.5");

		this.workingCopies[0] = getWorkingCopy("/P1/q/Y.java", true);
		contents =
				"package q;\n" +
				"import p.X.ListHandler;\n" +
				"public class Y {\n" +
				"	public Object foo() {\n" +
				"		ListHandler sortHandler = new ListHandler(null);\n" +
				"		return sortHandler;" +
				"	}\n" +
				"}\n";
		ASTNode node = buildAST(contents, this.workingCopies[0], true);

		assertTrue("Should be a compilation unit", node instanceof CompilationUnit);
		CompilationUnit unit = (CompilationUnit) node;
		node = getASTNode(unit, 0, 0, 0);
		assertEquals("Not an expression statement", ASTNode.VARIABLE_DECLARATION_STATEMENT, node.getNodeType());
		VariableDeclarationStatement statement = (VariableDeclarationStatement) node;
		List fragments = statement.fragments();
		assertEquals("Wrong size", 1, fragments.size());
		VariableDeclarationFragment fragment= (VariableDeclarationFragment) fragments.get(0);
		Expression expression = fragment.getInitializer();
		assertEquals("Not a constructor invocation", ASTNode.CLASS_INSTANCE_CREATION, expression.getNodeType());
		ClassInstanceCreation classInstanceCreation = (ClassInstanceCreation) expression;
		IMethodBinding binding = classInstanceCreation.resolveConstructorBinding();
		JavaElement element = (JavaElement)binding.getJavaElement();
		assertNotNull("Null Element info", element.getElementInfo());
	} finally {
		deleteProject(project);
	}
}

public void testBug405908() throws CoreException, IOException {
	try {
		createJavaProject("P", new String[] { "" }, new String[0], "", CompilerOptions.VERSION_1_5);
			createFile("P/A.java",
					"@interface Generated {\n" +
					"    String comment() default \"\";\n" +
					"    String[] value();\n" +
					"}\n" +
					"@Generated()\n" +
					"class A {\n" +
					"}"
			);
		ICompilationUnit cuA = getCompilationUnit("P/A.java");
		CompilationUnit unitA = (CompilationUnit) runConversion(cuA, true, false, true);
		AbstractTypeDeclaration typeA = (AbstractTypeDeclaration) unitA.types().get(1);
		IAnnotationBinding[] annotations = typeA.resolveBinding().getAnnotations();
		IAnnotationBinding generated = annotations[0];
		IMemberValuePairBinding[] mvps = generated.getAllMemberValuePairs();
		IMemberValuePairBinding valueBinding = mvps[1];
		assertEquals("value", valueBinding.getName());
		Object value = valueBinding.getValue();
		assertEquals(0, ((Object[]) value).length);
	} finally {
		deleteProject("P");
	}
}
}
