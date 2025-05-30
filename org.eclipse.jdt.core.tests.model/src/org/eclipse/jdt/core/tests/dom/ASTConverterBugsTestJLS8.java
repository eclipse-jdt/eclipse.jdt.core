/*******************************************************************************
 * Copyright (c) 2011, 2024 IBM Corporation and others.
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
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.internal.core.dom.rewrite.ASTRewriteFlattener;
import org.eclipse.jdt.internal.core.dom.rewrite.RewriteEventStore;

/**
 * Test suite to verify that DOM/AST bugs are fixed.
 *
 * Note that only specific JLS8 tests are defined in this test suite, but when
 * running it, all superclass {@link ASTConverterBugsTest} tests will be run
 * as well.
 */
@SuppressWarnings("rawtypes")
public class ASTConverterBugsTestJLS8 extends ASTConverterBugsTest {

/**
 * @deprecated
 */
public ASTConverterBugsTestJLS8(String name) {
    super(name);
    this.testLevel = AST.JLS8;
}

public static Test suite() {
	TestSuite suite = new Suite(ASTConverterBugsTestJLS8.class.getName());
	List tests = buildTestsList(ASTConverterBugsTestJLS8.class, 1, 0/* do not sort*/);
	for (int index=0, size=tests.size(); index<size; index++) {
		suite.addTest((Test)tests.get(index));
	}
	return suite;
}

/**
 * bug 130778: Invalid annotation elements cause no annotation to be in the AST
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
public void _testBug130778q() throws JavaModelException {
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
public void testBug527351() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/Class1.java",
			"package a;\n" +
			"public class Class1 {\n" +
			"  String value;\n" +
			"}\n",
			true/*resolve*/);
	CompilationUnit cu = (CompilationUnit) buildAST(this.workingCopies[0]);
	String flattened = ASTRewriteFlattener.asString(cu, new RewriteEventStore());
	assertEquals("Flattened AST",
			"package a;" +
			"public class Class1 {" +
			"String value;" +
			"}",
			flattened);
}
public void testGH1376() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P", new String[] { "" }, new String[] { "CONVERTER_JCL18_LIB" }, "", "1.8", true);
		createFolder("P/p");
		createFile("P/p/A.java",
			"package p;\n" +
			"public class A<T extends java.util.List> {\n" +
			"}"
		);
		createFile("P/p/B.java",
			"package p;\n" +
			"public class B extends A<C> {\n" + // for C we branch into binaries, which get resolved lazily
			"}"
		);
		createFile("P/p/D.java",
			"package p;\n" +
			"public interface D {\n" +
			"}"
		);
		addLibrary(project, "lib.jar", null,
				new String[] {
					"p/D.java",
					"package p;\n" +
					"public interface D {\n" + // duplicate just so we can create the jar
					"}",
					"p/C.java",
					"package p;\n" +
					"public interface C extends java.util.List<D> {\n" + // for D we will branch back to source
					"}"
				},
				"1.8");
		ICompilationUnit cuA = getCompilationUnit("P/p/A.java"); // compiles cleanly
		ICompilationUnit cuB = getCompilationUnit("P/p/B.java"); // during checkParameterizedTypes() we re-enter LE.completeTypeBinding(..)
		// just ensure that bound check during completion doesn't trigger NPE:
		resolveASTs(new ICompilationUnit[] {cuA, cuB}, new String[0], new BindingRequestor(), project, this.wcOwner);
	} finally {
		deleteProject("P");
	}
}
public void testGH2275() throws CoreException {
	try {
		createJavaProject("P", new String[] { "" }, new String[] { "CONVERTER_JCL18_LIB" }, "", "1.8", true);
		createFolder("P/p");
		createFile("P/p/A.java",
			"""
				package p;
				import java.util.Collections;
				import java.util.Map;

				class A {
					public A(Map<String, Integer> map) {
						Map<String, Integer> emptyMap= Collections.emptyMap();	// return type inferred from the target type
						Map<String, Integer> emptyMap2= foo(A.class);			// inference used, but not influencing the return type
					}
					<T> Map<String, Integer> foo(Class<T> clazz) {
						return Collections.emptyMap();
					}
				}
				"""
		);
		ICompilationUnit cuA = getCompilationUnit("P/p/A.java");
		ASTParser parser = createASTParser();
		parser.setResolveBindings(true);
		parser.setSource(cuA);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);

		TypeDeclaration classA = (TypeDeclaration) cu.types().get(0);
		MethodDeclaration constructor = classA.getMethods()[0];
		assertTrue(constructor.isConstructor());
		List statements = constructor.getBody().statements();

		VariableDeclarationStatement local = (VariableDeclarationStatement) statements.get(0);
		MethodInvocation invocation = (MethodInvocation) ((VariableDeclarationFragment) local.fragments().get(0)).getInitializer();
		assertEquals("emptyMap", invocation.getName().getIdentifier());
		assertTrue(invocation.isResolvedTypeInferredFromExpectedType());

		VariableDeclarationStatement local2 = (VariableDeclarationStatement) statements.get(1);
		MethodInvocation invocation2 = (MethodInvocation) ((VariableDeclarationFragment) local2.fragments().get(0)).getInitializer();
		assertEquals("foo", invocation2.getName().getIdentifier());
		assertFalse(invocation2.isResolvedTypeInferredFromExpectedType());
	} finally {
		deleteProject("P");
	}
}

public void testGH3064() throws CoreException {
	try {
		createJavaProject("P", new String[] { "" }, new String[] { "CONVERTER_JCL_LIB" }, "", "1.8", true);
		createFolder("P/src/test");
		createFile("/P/src/test/Action.java", """
				package test;
				public class Action<Request extends BroadcastRequest<Request>, ShardRequest> {
				    private final Request request;

				    protected ShardRequest newShardRequest(Request request) {return null;} // can also be omitted

				    protected void performOperation(final int shardIndex) {
				        ShardRequest shardRequest = newShardRequest(request);
				        shardRequest.setParentTask(foobar);
				    }
				}
				""");
		ICompilationUnit cuAction = getCompilationUnit("P/src/test/Action.java");
		ASTParser parser = createASTParser();
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setSource(cuAction);
		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		TypeDeclaration type = (TypeDeclaration) cu.types().get(0);
		Object decl2 = type.bodyDeclarations().get(2);
		assertEquals(MethodDeclaration.class, decl2.getClass());
		Object stat0 = ((MethodDeclaration) decl2).getBody().statements().get(0);
		Object stat1 = ((MethodDeclaration) decl2).getBody().statements().get(1);
		assertEquals(ExpressionStatement.class, stat1.getClass());
		Expression expr = ((ExpressionStatement) stat1).getExpression();
		assertEquals(MethodInvocation.class, expr.getClass());
		Expression receiver = ((MethodInvocation) expr).getExpression();
		IBinding binding1 = ((SimpleName) receiver).resolveBinding();
		assertNotNull(binding1);
		assertEquals(VariableDeclarationStatement.class, stat0.getClass());
		VariableDeclarationFragment frag = (VariableDeclarationFragment) ((VariableDeclarationStatement) stat0)
				.fragments().get(0);
		IBinding binding0 = frag.getName().resolveBinding();
		assertEquals(binding0, binding1);
	} finally {
		deleteProject("P");
	}
}
}
