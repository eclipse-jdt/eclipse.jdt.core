/*******************************************************************************
 * Copyright (c) 2011, 2016 IBM Corporation and others.
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.core.dom.rewrite.ASTRewriteFlattener;
import org.eclipse.jdt.internal.core.dom.rewrite.RewriteEventStore;

import junit.framework.Test;
import junit.framework.TestSuite;

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
			"""
				package a;
				public @interface AnAnnotation {
				  String value();
				}
				""",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"""
				package a;
				public @interface AnAnnotation2 {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*]public[*1*]
				[*2*]@AnAnnotation("a")[*2*]
				[*3*]final[*3*]
				[*4*]@AnAnnotation2("b")[*4*]
				class X {
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*]public[*1*] [*2*]@AnAnnotation("a")[*2*] [*3*]final[*3*] [*4*]@AnAnnotation2("b")[*4*] class X {
				}
				
				===== Details =====
				1:MODIFIER,[11,6],,,[N/A]
				2:SINGLE_MEMBER_ANNOTATION,[18,18],,,[ANNOTATION,La/X;@La/AnAnnotation;,]
				3:MODIFIER,[37,5],,,[N/A]
				4:SINGLE_MEMBER_ANNOTATION,[43,19],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]
				===== Problems =====
				No problem""",
			result);
}
public void testBug130778b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  String value();
				}
				""",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"""
				package a;
				public @interface AnAnnotation2 {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				#
				[*1*]public[*1*]
				[*2*]@AnAnnotation("a")[*2*]
				[*3*]final[*3*]
				[*4*]@AnAnnotation2("b")[*4*]
				class X {
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*]public[*1*] [*2*]@AnAnnotation("a")[*2*] [*3*]final[*3*] [*4*]@AnAnnotation2("b")[*4*] class X {
				}
				
				===== Details =====
				1:MODIFIER,[13,6],,,[N/A]
				2:SINGLE_MEMBER_ANNOTATION,[20,18],,,[ANNOTATION,La/X;@La/AnAnnotation;,]
				3:MODIFIER,[39,5],,,[N/A]
				4:SINGLE_MEMBER_ANNOTATION,[45,19],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 2)
					#
					^
				Syntax error on token "Invalid Character", delete this token
				""",
			result);
}
public void testBug130778c() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  String value();
				}
				""",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"""
				package a;
				public @interface AnAnnotation2 {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*]public[*1*]
				#
				[*2*]@AnAnnotation("a")[*2*]
				[*3*]final[*3*]
				[*4*]@AnAnnotation2("b")[*4*]
				class X {
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*]public[*1*] [*2*]@AnAnnotation("a")[*2*] [*3*]final[*3*] [*4*]@AnAnnotation2("b")[*4*] class X {
				}
				
				===== Details =====
				1:MODIFIER,[11,6],,,[N/A]
				2:SINGLE_MEMBER_ANNOTATION,[20,18],,,[ANNOTATION,La/X;@La/AnAnnotation;,]
				3:MODIFIER,[39,5],,,[N/A]
				4:SINGLE_MEMBER_ANNOTATION,[45,19],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 3)
					#
					^
				Syntax error on token "Invalid Character", delete this token
				""",
			result);
}
public void testBug130778d() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  String value();
				}
				""",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"""
				package a;
				public @interface AnAnnotation2 {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*]public[*1*]
				[*2*]@AnAnnotation("a")[*2*]
				#
				[*3*]final[*3*]
				[*4*]@AnAnnotation2("b")[*4*]
				class X {
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*]public[*1*] [*2*]@AnAnnotation("a")[*2*] [*3*]final[*3*] [*4*]@AnAnnotation2("b")[*4*] class X {
				}
				
				===== Details =====
				1:MODIFIER,[11,6],,,[N/A]
				2:SINGLE_MEMBER_ANNOTATION,[18,18],,,[ANNOTATION,La/X;@La/AnAnnotation;,]
				3:MODIFIER,[39,5],,,[N/A]
				4:SINGLE_MEMBER_ANNOTATION,[45,19],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 4)
					#
					^
				Syntax error on token "Invalid Character", delete this token
				""",
			result);
}
public void testBug130778e() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  String value();
				}
				""",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"""
				package a;
				public @interface AnAnnotation2 {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*]public[*1*]
				[*2*]@AnAnnotation("a")[*2*]
				[*3*]final[*3*]
				#
				[*4*]@AnAnnotation2("b")[*4*]
				class X {
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*]public[*1*] [*2*]@AnAnnotation("a")[*2*] [*3*]final[*3*] [*4*]@AnAnnotation2("b")[*4*] class X {
				}
				
				===== Details =====
				1:MODIFIER,[11,6],,,[N/A]
				2:SINGLE_MEMBER_ANNOTATION,[18,18],,,[ANNOTATION,La/X;@La/AnAnnotation;,]
				3:MODIFIER,[37,5],,,[N/A]
				4:SINGLE_MEMBER_ANNOTATION,[45,19],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 5)
					#
					^
				Syntax error on token "Invalid Character", delete this token
				""",
			result);
}
public void testBug130778f() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  String value();
				}
				""",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"""
				package a;
				public @interface AnAnnotation2 {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*]public[*1*]
				[*2*]@AnAnnotation("a")[*2*]
				[*3*]final[*3*]
				[*4*]@AnAnnotation2("b")[*4*]
				#
				class X {
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*]public[*1*] [*2*]@AnAnnotation("a")[*2*] [*3*]final[*3*] [*4*]@AnAnnotation2("b")[*4*] class X {
				}
				
				===== Details =====
				1:MODIFIER,[11,6],,,[N/A]
				2:SINGLE_MEMBER_ANNOTATION,[18,18],,,[ANNOTATION,La/X;@La/AnAnnotation;,]
				3:MODIFIER,[37,5],,,[N/A]
				4:SINGLE_MEMBER_ANNOTATION,[43,19],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 6)
					#
					^
				Syntax error on token "Invalid Character", delete this token
				""",
			result);
}
public void testBug130778g() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  String value();
				}
				""",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"""
				package a;
				public @interface AnAnnotation2 {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*]public[*1*]
				[*2*]@AnAnnotation("a")[*2*]
				[*3*]final[*3*]
				[*4*]@AnAnnotation2("b")[*4*]
				class X {
				  #
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*]public[*1*] [*2*]@AnAnnotation("a")[*2*] [*3*]final[*3*] [*4*]@AnAnnotation2("b")[*4*] class X {
				}
				
				===== Details =====
				1:MODIFIER,[11,6],,,[N/A]
				2:SINGLE_MEMBER_ANNOTATION,[18,18],,,[ANNOTATION,La/X;@La/AnAnnotation;,]
				3:MODIFIER,[37,5],,,[N/A]
				4:SINGLE_MEMBER_ANNOTATION,[43,19],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 7)
					#
					^
				Syntax error on token "Invalid Character", delete this token
				""",
			result);
}
public void testBug130778h() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  String value();
				}
				""",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"""
				package a;
				public @interface AnAnnotation2 {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*]@AnAnnotation(value="a")[*1*]
				[*2*]@AnAnnotation2(value="b")[*2*]
				[*3*]public[*3*] class X {
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*]@AnAnnotation(value="a")[*1*] [*2*]@AnAnnotation2(value="b")[*2*] [*3*]public[*3*] class X {
				}
				
				===== Details =====
				1:NORMAL_ANNOTATION,[11,24],,,[ANNOTATION,La/X;@La/AnAnnotation;,]
				2:NORMAL_ANNOTATION,[36,25],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]
				3:MODIFIER,[62,6],,,[N/A]
				===== Problems =====
				No problem""",
			result);
}
public void testBug130778i() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  String value();
				}
				""",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"""
				package a;
				public @interface AnAnnotation2 {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*]@AnAnnotation(value=[*1*])
				[*2*]@AnAnnotation2(value="b")[*2*]
				[*3*]public[*3*] class X {
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*]@AnAnnotation(value=$missing$)[*1*] [*2*]@AnAnnotation2(value="b")[*2*] [*3*]public[*3*] class X {
				}
				
				===== Details =====
				1:NORMAL_ANNOTATION,[11,20],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation;,]
				2:NORMAL_ANNOTATION,[33,25],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]
				3:MODIFIER,[59,6],,,[N/A]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 2)
					@AnAnnotation(value=)
					                   ^
				Syntax error on token "=", MemberValue expected after this token
				""",
			result);
}
public void testBug130778j() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  String value();
				}
				""",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"""
				package a;
				public @interface AnAnnotation2 {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*]@AnAnnotation(value="a")[*1*]
				[*2*]@AnAnnotation2(value=[*2*])
				[*3*]public[*3*] class X {
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*]@AnAnnotation(value="a")[*1*] [*2*]@AnAnnotation2(value=$missing$)[*2*] [*3*]public[*3*] class X {
				}
				
				===== Details =====
				1:NORMAL_ANNOTATION,[11,24],,,[ANNOTATION,La/X;@La/AnAnnotation;,]
				2:NORMAL_ANNOTATION,[36,21],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation2;,]
				3:MODIFIER,[59,6],,,[N/A]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 3)
					@AnAnnotation2(value=)
					                    ^
				Syntax error on token "=", MemberValue expected after this token
				""",
			result);
}
public void testBug130778k() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  String value();
				}
				""",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"""
				package a;
				public @interface AnAnnotation2 {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*]@AnAnnotation(value=[*1*])
				[*2*]@AnAnnotation2(value=[*2*])
				[*3*]public[*3*] class X {
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*]@AnAnnotation(value=$missing$)[*1*] [*2*]@AnAnnotation2(value=$missing$)[*2*] [*3*]public[*3*] class X {
				}
				
				===== Details =====
				1:NORMAL_ANNOTATION,[11,20],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation;,]
				2:NORMAL_ANNOTATION,[33,21],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation2;,]
				3:MODIFIER,[56,6],,,[N/A]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 2)
					@AnAnnotation(value=)
					                    ^
				Syntax error on token ")", delete this token
				2. ERROR in /Converter15/src/a/X.java (at line 3)
					@AnAnnotation2(value=)
					                    ^
				Syntax error on token "=", ) expected
				""",
			result);
}
public void testBug130778l() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  String value();
				}
				""",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"""
				package a;
				public @interface AnAnnotation2 {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				public class X {
				  public void foo(){
				    [*1*]@AnAnnotation(value=[*1*])
				    [*2*]@AnAnnotation2(value="b")[*2*]
				    class Y {}
				  }
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				public class X {
				  public void foo(){
				[*1*]@AnAnnotation(value=$missing$)[*1*] [*2*]@AnAnnotation2(value="b")[*2*] class Y {
				    }
				  }
				}
				
				===== Details =====
				1:NORMAL_ANNOTATION,[53,20],,RECOVERED,[ANNOTATION,La/X$115$Y;@La/AnAnnotation;,]
				2:NORMAL_ANNOTATION,[79,25],,,[ANNOTATION,La/X$115$Y;@La/AnAnnotation2;,]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 4)
					@AnAnnotation(value=)
					                   ^
				Syntax error on token "=", MemberValue expected after this token
				""",
			result);
}
public void testBug130778m() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  String value();
				}
				""",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"""
				package a;
				public @interface AnAnnotation2 {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				public class X {
				  public void foo(){
				    [*1*]@AnAnnotation(value=)[*1*]
				    [*2*]@AnAnnotation2(value="b")[*2*]
				    int i;
				  }
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				public class X {
				  public void foo(){
				    [*1*]@AnAnnotation(value=$missing$)[*1*] [*2*]@AnAnnotation2(value="b")[*2*] int i;
				  }
				}
				
				===== Details =====
				1:NORMAL_ANNOTATION,[53,21],,,[ANNOTATION,La/X;.foo()V#i@La/AnAnnotation;,]
				2:NORMAL_ANNOTATION,[79,25],,,[ANNOTATION,La/X;.foo()V#i@La/AnAnnotation2;,]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 4)
					@AnAnnotation(value=)
					                   ^
				Syntax error on token "=", MemberValue expected after this token
				""",
			result);
}
public void testBug130778n() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  String name1();
				  String name2();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*][*2*]@AnAnnotation([*3*]name1="a"[*3*][*2*], name2)
				public class X {
				}[*1*]
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*][*2*]@AnAnnotation([*3*]name1="a"[*3*])[*2*] public class X {
				}[*1*]
				
				===== Details =====
				1:TYPE_DECLARATION,[11,50],,MALFORMED|RECOVERED,[TYPE,La/X;,]
				2:NORMAL_ANNOTATION,[11,23],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation;,]
				3:MEMBER_VALUE_PAIR,[25,9],,,[N/A]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 2)
					@AnAnnotation(name1="a", name2)
					                       ^
				Syntax error on token ",", . expected
				""",
			result);
}
public void testBug130778o() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  String name1();
				  String name2();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*][*2*]@AnAnnotation([*3*]name1=[*3*][*2*])
				public class X {
				}[*1*]
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*][*2*]@AnAnnotation([*3*]name1=$missing$[*3*])[*2*] public class X {
				}[*1*]
				
				===== Details =====
				1:TYPE_DECLARATION,[11,40],,MALFORMED,[TYPE,La/X;,]
				2:NORMAL_ANNOTATION,[11,20],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation;,]
				3:MEMBER_VALUE_PAIR,[25,6],,RECOVERED,[N/A]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 2)
					@AnAnnotation(name1=)
					                   ^
				Syntax error on token "=", MemberValue expected after this token
				""",
			result);
}
public void testBug130778p() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  AnAnnotation2 value();
				}
				""",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"""
				package a;
				public @interface AnAnnotation2 {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*]@AnAnnotation([*2*]value=[*3*]@AnAnnotation2(value="a")[*3*][*2*])[*1*]
				public class X {
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*]@AnAnnotation([*2*]value=[*3*]@AnAnnotation2(value="a")[*3*][*2*])[*1*] public class X {
				}
				
				===== Details =====
				1:NORMAL_ANNOTATION,[11,46],,,[ANNOTATION,La/X;@La/AnAnnotation;,]
				2:MEMBER_VALUE_PAIR,[25,31],,,[N/A]
				3:NORMAL_ANNOTATION,[31,25],,,[ANNOTATION,@La/AnAnnotation2;,]
				===== Problems =====
				No problem""",
			result);
}
public void _testBug130778q() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  AnAnnotation2 value();
				}
				""",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"""
				package a;
				public @interface AnAnnotation2 {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*]@AnAnnotation([*2*]value=[*3*]@AnAnnotation2(value="a")[*3*][*2*][*1*]
				public class X {
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*]@AnAnnotation([*2*]value=[*3*]@AnAnnotation2(value="a")[*3*][*2*])[*1*] public class X {
				}
				
				===== Details =====
				1:NORMAL_ANNOTATION,[11,45],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation;,]
				2:MEMBER_VALUE_PAIR,[25,31],,,[N/A]
				3:NORMAL_ANNOTATION,[31,25],,RECOVERED,[ANNOTATION,@La/AnAnnotation2;,]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 2)
					@AnAnnotation(value=@AnAnnotation2(value="a")
					                                            ^
				Syntax error, insert ")" to complete Modifiers
				""",
			result);
}
public void testBug130778r() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  AnAnnotation2 value();
				}
				""",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"""
				package a;
				public @interface AnAnnotation2 {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*][*2*]@AnAnnotation(value=[*2*][*3*]@AnAnnotation2(value=[*3*]))
				public class X {
				}[*1*]
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*][*2*]@AnAnnotation(value=$missing$)[*2*] public class X {
				}[*1*]
				
				===== Details =====
				1:TYPE_DECLARATION,[11,62],,MALFORMED|RECOVERED,[TYPE,La/X;,]
				2:NORMAL_ANNOTATION,[11,20],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation;,]
				3:No corresponding node
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 2)
					@AnAnnotation(value=@AnAnnotation2(value=))
					                                        ^
				Syntax error on token "=", MemberValue expected after this token
				""",
			result);
}
public void testBug130778s() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  String value1();
				  AnAnnotation2 value2();
				}
				""",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"""
				package a;
				public @interface AnAnnotation2 {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*][*2*]@AnAnnotation([*3*]value1=[*3*][*2*],[*4*]value=[*5*]@AnAnnotation2(value="b")[*5*][*4*])
				public class X {
				}[*1*]
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*][*2*]@AnAnnotation([*3*]value1=$missing$[*3*])[*2*] [*5*]@AnAnnotation2(value="b")[*5*] public class X {
				}[*1*]
				
				===== Details =====
				1:TYPE_DECLARATION,[11,73],,MALFORMED,[TYPE,La/X;,]
				2:NORMAL_ANNOTATION,[11,21],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation;,]
				3:MEMBER_VALUE_PAIR,[25,7],,RECOVERED,[N/A]
				5:NORMAL_ANNOTATION,[39,25],,,[ANNOTATION,La/X;@La/AnAnnotation2;,]
				4:No corresponding node
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 2)
					@AnAnnotation(value1=,value=@AnAnnotation2(value="b"))
					                    ^
				Syntax error on token "=", MemberValue expected after this token
				""",
			result);
}
public void testBug130778t() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*]@AnAnnotation("b")[*1*]
				public class X {
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*]@AnAnnotation("b")[*1*] public class X {
				}
				
				===== Details =====
				1:SINGLE_MEMBER_ANNOTATION,[11,18],,,[ANNOTATION,La/X;@La/AnAnnotation;,]
				===== Problems =====
				No problem""",
			result);
}
public void testBug130778u() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*]@AnAnnotation([*2*]"b"[*2*][*1*]
				public class X {
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*]@AnAnnotation([*2*]"b"[*2*])[*1*] public class X {
				}
				
				===== Details =====
				1:SINGLE_MEMBER_ANNOTATION,[11,17],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation;,]
				2:STRING_LITERAL,[25,3],,,[N/A]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 2)
					@AnAnnotation("b"
					              ^^^
				Syntax error, insert ")" to complete Modifiers
				""",
			result);
}
public void testBug130778v() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  AnAnnotation2 value();
				}
				""",
			true/*resolve*/);

	this.workingCopies[1] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation2.java",
			"""
				package a;
				public @interface AnAnnotation2 {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*]@AnAnnotation[*1*](@AnAnnotation2("b"
				public class X {
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*]@AnAnnotation[*1*] public class X {
				}
				
				===== Details =====
				1:MARKER_ANNOTATION,[11,13],,,[ANNOTATION,La/X;@La/AnAnnotation;,]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 2)
					@AnAnnotation(@AnAnnotation2("b"
					                             ^^^
				Syntax error, insert ")" to complete SingleMemberAnnotation
				2. ERROR in /Converter15/src/a/X.java (at line 2)
					@AnAnnotation(@AnAnnotation2("b"
					                             ^^^
				Syntax error, insert ")" to complete Modifiers
				""",
			result);
}
public void testBug130778x() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/AnAnnotation.java",
			"""
				package a;
				public @interface AnAnnotation {
				  String value();
				}
				""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				[*1*]@AnAnnotation([*2*]"a"[*2*][*1*], [*3*]"b"[*3*])
				public class X {
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				[*1*]@AnAnnotation([*2*]"a"[*2*])[*1*] public class X {
				}
				
				===== Details =====
				1:SINGLE_MEMBER_ANNOTATION,[11,17],,RECOVERED,[ANNOTATION,La/X;@La/AnAnnotation;,]
				2:STRING_LITERAL,[25,3],,,[N/A]
				3:No corresponding node
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 2)
					@AnAnnotation("a", "b")
					                 ^
				Syntax error on token ",", < expected
				""",
			result);
}
public void testBug527351() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter15/src/a/Class1.java",
			"""
				package a;
				public class Class1 {
				  String value;
				}
				""",
			true/*resolve*/);
	CompilationUnit cu = (CompilationUnit) buildAST(this.workingCopies[0]);
	String flattened = ASTRewriteFlattener.asString(cu, new RewriteEventStore());
	assertEquals("Flattened AST",
			"""
				package a;\
				public class Class1 {\
				String value;\
				}""",
			flattened);
}
public void testGH1376() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P", new String[] { "" }, new String[] { "CONVERTER_JCL_LIB" }, "", "1.8", true);
		createFolder("P/p");
		createFile("P/p/A.java",
			"""
				package p;
				public class A<T extends java.util.List> {
				}"""
		);
		createFile("P/p/B.java",
			"package p;\n" +
			"public class B extends A<C> {\n" + // for C we branch into binaries, which get resolved lazily
			"}"
		);
		createFile("P/p/D.java",
			"""
				package p;
				public interface D {
				}"""
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
}
