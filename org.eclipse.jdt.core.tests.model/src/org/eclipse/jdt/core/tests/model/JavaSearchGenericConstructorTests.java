/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;

/**
 * Test for generic constructor search using R_ERASURE_MATCH rule.
 */
public class JavaSearchGenericConstructorTests extends AbstractJavaSearchGenericTests {

	public JavaSearchGenericConstructorTests(String name) {
		super(name, ERASURE_RULE);
	}
	// defined for sub-classes
	JavaSearchGenericConstructorTests(String name, int matchRule) {
		super(name, matchRule);
	}
	public static Test suite() {
		return buildModelTestSuite(JavaSearchGenericConstructorTests.class);
	}
	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_PREFIX = "testConstructorDeclarations";
//		TESTS_NAMES = new String[] { "testConstructorStringPatternSingleParamArguments06" };
//		TESTS_NUMBERS = new int[] { 8 };
//		TESTS_RANGE = new int[] { -1, -1 };
	}

	/*
	 * Remove last type arguments from a line of an expected result.
	 * This line contains one search match print out.
	 */
	long removeFirstTypeArgument(char[] line) {
		int idx=0;
		int length = line.length;
		while (line[idx++] != '[') {
			if (idx == length) return -1;
		}
		if (line[idx++] != '<') return -1;
		int start = idx;
		int n = 1;
		while(idx < length && n!= 0) {
			switch (line[idx++]) {
				case '<': n++; break;
				case '>': n--; break;
			}
		}
		if (n!= 0) {
			// something wrong here...
			return -1;
		}
		return ((long)start<<32) + idx;
	}

	void addResultLine(StringBuffer buffer, char[] line) {
		long positions = removeFirstTypeArgument(line);
		if (buffer.length() > 0) buffer.append('\n');
		if (positions != -1) {
			int stop = (int) (positions >>> 32) - 1;
			int restart = (int) positions;
			buffer.append(line, 0, stop);
			buffer.append(line, restart, line.length-restart);
		} else {
			buffer.append(line);
		}
	}

	/**
	 * Bug 75642: [1.5][search] Methods and constructor search does not work with generics
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=75642"
	 */
	/*
	 * REFERENCES
	 */
	// Search references to constructors defined in a single type parameter class
	public void testConstructorReferencesElementPatternSingleTypeParameter01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g5/c/def/Single.java").getType("Single");
		// search reference to a standard constructor
		IMethod method = type.getMethod("Single", new String[] { "QT;" });
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object())] ERASURE_RAW_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new Single<Object>(new Object())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new Single<Exception>(new Exception())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new Single<RuntimeException>(new RuntimeException())] ERASURE_MATCH"
		);
	}
	public void testConstructorReferencesElementPatternSingleTypeParameter02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g5/c/def/Single.java").getType("Single");
		// search reference to a generic constructor
		IMethod method = type.getMethod("Single", new String[] { "QT;", "QU;" });
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object(), new Throwable())] ERASURE_RAW_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new <Throwable>Single<Object>(new Object(), new Throwable())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new <Exception>Single<Exception>(new Exception(), new Exception())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new <RuntimeException>Single<RuntimeException>(new RuntimeException(), new RuntimeException())] ERASURE_MATCH"
		);
	}
	public void testConstructorReferencesElementPatternSingleTypeParameter03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g5/c/def/Single.java").getType("Single");
		// search reference to a method with parameterized type arguments
		IMethod method = type.getMethod("Single", new String[] { "QSingle<QT;>;" });
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(gs)] ERASURE_RAW_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new Single<Object>(gs)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new Single<Exception>(gs)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new Single<RuntimeException>(gs)] ERASURE_MATCH"
		);
	}
	public void testConstructorReferencesElementPatternSingleTypeParameter04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g5/c/def/Single.java").getType("Single");
		// search reference to a generic method returning a param type with param type parameters (=complete)
		IMethod method = type.getMethod("Single", new String[] { "QU;", "QSingle<QT;>;" });
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object(), gs)] ERASURE_RAW_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new <Exception>Single<Object>(new Exception(), gs)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new <Exception>Single<Exception>(new Exception(), gs)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new <RuntimeException>Single<RuntimeException>(new RuntimeException(), gs)] ERASURE_MATCH"
		);
	}

	// Search references to contructors defined in a multiple type parameters class
	public void testConstructorReferencesElementPatternMultipleTypeParameter01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g5/c/def/Multiple.java").getType("Multiple");
		IMethod method = type.getMethod("Multiple", new String[] { "QT1;","QT2;","QT3;" });
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new Multiple<Object, Object, Object>(new Object(), new Object(), new Object())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(new Exception(), new Exception(), new Exception())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(), new RuntimeException(), new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(new Object(), new Object(), new Object())] ERASURE_RAW_MATCH"
		);
	}
	public void testConstructorReferencesElementPatternMultipleTypeParameter02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g5/c/def/Multiple.java").getType("Multiple");
		IMethod method = type.getMethod("Multiple", new String[] { "QMultiple<QT1;QT2;QT3;>;", "QU1;","QU2;","QU3;" });
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new <Object, Throwable, Exception>Multiple<Object, Object, Object>(gm, new Object(), new Throwable(), new Exception())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new <Exception, Exception, Exception>Multiple<Exception, Exception, Exception>(gm, new Exception(),new Exception(),new Exception())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(gm, new RuntimeException(),new RuntimeException(),new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(gm, new Object(), new Throwable(), new Exception())] ERASURE_RAW_MATCH"
		);
	}
	public void testConstructorReferencesElementPatternMultipleTypeParameter03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g5/c/def/Multiple.java").getType("Multiple");
		IMethod method = type.getMethod("Multiple", new String[] { "QMultiple<QT1;QT2;QT3;>;" });
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new Multiple<Object, Object, Object>(gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new Multiple<RuntimeException, RuntimeException, RuntimeException>(gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(gm)] ERASURE_RAW_MATCH"
		);
	}
	public void testConstructorReferencesElementPatternMultipleTypeParameter04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g5/c/def/Multiple.java").getType("Multiple");
		IMethod method = type.getMethod("Multiple", new String[] { "QU1;","QU2;","QU3;", "QMultiple<QT1;QT2;QT3;>;" });
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new <Object, Throwable, Exception>Multiple<Object, Object, Object>(new Object(), new Throwable(), new Exception(), gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new <Exception, Exception, Exception>Multiple<Exception, Exception, Exception>(new Exception(),new Exception(),new Exception(), gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(),new RuntimeException(),new RuntimeException(), gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(new Object(), new Throwable(), new Exception(), gm)] ERASURE_RAW_MATCH"
		);
	}

	// Search references to single parameterized contructors
	public void testConstructorReferencesElementPatternSingleParamArguments01() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefSingle.java");
		IMethod method = selectMethod(unit, "Single", 9);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new Single<Object>(new Object())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new Single<Exception>(new Exception())] EXACT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new Single<RuntimeException>(new RuntimeException())] ERASURE_MATCH"
		);
	}
	public void testConstructorReferencesElementPatternSingleParamArguments02() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefSingle.java");
		IMethod method = selectMethod(unit, "Single", 10);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object(), new Throwable())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new <Throwable>Single<Object>(new Object(), new Throwable())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new <Exception>Single<Exception>(new Exception(), new Exception())] EXACT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new <RuntimeException>Single<RuntimeException>(new RuntimeException(), new RuntimeException())] ERASURE_MATCH"
		);
	}
	public void testConstructorReferencesElementPatternSingleParamArguments03() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefSingle.java");
		IMethod method = selectMethod(unit, "Single", 11);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(gs)] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new Single<Object>(gs)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new Single<Exception>(gs)] EXACT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new Single<RuntimeException>(gs)] ERASURE_MATCH"
		);
	}
	public void testConstructorReferencesElementPatternSingleParamArguments04() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefSingle.java");
		IMethod method = selectMethod(unit, "Single", 12);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object(), gs)] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new <Exception>Single<Object>(new Exception(), gs)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new <Exception>Single<Exception>(new Exception(), gs)] EXACT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new <RuntimeException>Single<RuntimeException>(new RuntimeException(), gs)] ERASURE_MATCH"
		);
	}
	public void testConstructorReferencesElementPatternSingleParamArguments05() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefRaw.java");
		IMethod method = selectMethod(unit, "Single", 5);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object(), new Throwable())] EXACT_RAW_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new <Throwable>Single<Object>(new Object(), new Throwable())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new <Exception>Single<Exception>(new Exception(), new Exception())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new <RuntimeException>Single<RuntimeException>(new RuntimeException(), new RuntimeException())] EQUIVALENT_MATCH"
		);
	}
	public void testConstructorReferencesElementPatternSingleParamArguments06() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefRaw.java");
		IMethod method = selectMethod(unit, "Single", 7);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object(), gs)] EXACT_RAW_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new <Exception>Single<Object>(new Exception(), gs)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new <Exception>Single<Exception>(new Exception(), gs)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new <RuntimeException>Single<RuntimeException>(new RuntimeException(), gs)] EQUIVALENT_MATCH"
		);
	}

	// Search references to multiple parameterized contructors
	public void testConstructorReferencesElementPatternMultipleParamArguments01() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefMultiple.java");
		IMethod method = selectMethod(unit, "Multiple", 8);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new Multiple<Object, Object, Object>(new Object(), new Object(), new Object())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(new Exception(), new Exception(), new Exception())] EXACT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(), new RuntimeException(), new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(new Object(), new Object(), new Object())] EQUIVALENT_RAW_MATCH"
		);
	}
	public void testConstructorReferencesElementPatternMultipleParamArguments02() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefMultiple.java");
		IMethod method = selectMethod(unit, "Multiple", 9);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new <Object, Throwable, Exception>Multiple<Object, Object, Object>(gm, new Object(), new Throwable(), new Exception())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new <Exception, Exception, Exception>Multiple<Exception, Exception, Exception>(gm, new Exception(),new Exception(),new Exception())] EXACT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(gm, new RuntimeException(),new RuntimeException(),new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(gm, new Object(), new Throwable(), new Exception())] EQUIVALENT_RAW_MATCH"
		);
	}
	public void testConstructorReferencesElementPatternMultipleParamArguments03() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefMultiple.java");
		IMethod method = selectMethod(unit, "Multiple", 10);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new Multiple<Object, Object, Object>(gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(gm)] EXACT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new Multiple<RuntimeException, RuntimeException, RuntimeException>(gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(gm)] EQUIVALENT_RAW_MATCH"
		);
	}
	public void testConstructorReferencesElementPatternMultipleParamArguments04() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefMultiple.java");
		IMethod method = selectMethod(unit, "Multiple", 11);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new <Object, Throwable, Exception>Multiple<Object, Object, Object>(new Object(), new Throwable(), new Exception(), gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new <Exception, Exception, Exception>Multiple<Exception, Exception, Exception>(new Exception(),new Exception(),new Exception(), gm)] EXACT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(),new RuntimeException(),new RuntimeException(), gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(new Object(), new Throwable(), new Exception(), gm)] EQUIVALENT_RAW_MATCH"
		);
	}
	public void testConstructorReferencesElementPatternMultipleParamArguments05() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefRaw.java");
		IMethod method = selectMethod(unit, "Multiple", 5);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new <Object, Throwable, Exception>Multiple<Object, Object, Object>(gm, new Object(), new Throwable(), new Exception())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new <Exception, Exception, Exception>Multiple<Exception, Exception, Exception>(gm, new Exception(),new Exception(),new Exception())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(gm, new RuntimeException(),new RuntimeException(),new RuntimeException())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(gm, new Object(), new Throwable(), new Exception())] EXACT_RAW_MATCH"
		);
	}
	public void testConstructorReferencesElementPatternMultipleParamArguments06() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefRaw.java");
		IMethod method = selectMethod(unit, "Multiple", 7);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new <Object, Throwable, Exception>Multiple<Object, Object, Object>(new Object(), new Throwable(), new Exception(), gm)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new <Exception, Exception, Exception>Multiple<Exception, Exception, Exception>(new Exception(),new Exception(),new Exception(), gm)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(),new RuntimeException(),new RuntimeException(), gm)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(new Object(), new Throwable(), new Exception(), gm)] EXACT_RAW_MATCH"
		);
	}

	// Search string pattern references to single parameterized contructors
	public void testConstructorReferencesStringPatternSingleParamArguments01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.c.ref", false);
		search("<Exception>Single", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object(), new Throwable())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(gs)] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object(), gs)] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new Single<Object>(new Object())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new <Throwable>Single<Object>(new Object(), new Throwable())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new Single<Object>(gs)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new <Exception>Single<Object>(new Exception(), gs)] EXACT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new Single<Exception>(new Exception())] EXACT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new <Exception>Single<Exception>(new Exception(), new Exception())] EXACT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new Single<Exception>(gs)] EXACT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new <Exception>Single<Exception>(new Exception(), gs)] EXACT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new Single<RuntimeException>(new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new <RuntimeException>Single<RuntimeException>(new RuntimeException(), new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new Single<RuntimeException>(gs)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new <RuntimeException>Single<RuntimeException>(new RuntimeException(), gs)] ERASURE_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternSingleParamArguments02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.c.ref", false);
		search("<? extends Exception> Single", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object(), new Throwable())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(gs)] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object(), gs)] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new Single<Object>(new Object())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new <Throwable>Single<Object>(new Object(), new Throwable())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new Single<Object>(gs)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new <Exception>Single<Object>(new Exception(), gs)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new Single<Exception>(new Exception())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new <Exception>Single<Exception>(new Exception(), new Exception())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new Single<Exception>(gs)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new <Exception>Single<Exception>(new Exception(), gs)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new Single<RuntimeException>(new RuntimeException())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new <RuntimeException>Single<RuntimeException>(new RuntimeException(), new RuntimeException())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new Single<RuntimeException>(gs)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new <RuntimeException>Single<RuntimeException>(new RuntimeException(), gs)] EQUIVALENT_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternSingleParamArguments03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.c.ref", false);
		search("<? super Exception>S?ng*", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object(), new Throwable())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(gs)] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object(), gs)] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new Single<Object>(new Object())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new <Throwable>Single<Object>(new Object(), new Throwable())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new Single<Object>(gs)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new <Exception>Single<Object>(new Exception(), gs)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new Single<Exception>(new Exception())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new <Exception>Single<Exception>(new Exception(), new Exception())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new Single<Exception>(gs)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new <Exception>Single<Exception>(new Exception(), gs)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new Single<RuntimeException>(new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new <RuntimeException>Single<RuntimeException>(new RuntimeException(), new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new Single<RuntimeException>(gs)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new <RuntimeException>Single<RuntimeException>(new RuntimeException(), gs)] ERASURE_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternSingleParamArguments04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.c.ref", false);
		search("Single", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object())] EXACT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object(), new Throwable())] EXACT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(gs)] EXACT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object(), gs)] EXACT_RAW_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new Single<Object>(new Object())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new <Throwable>Single<Object>(new Object(), new Throwable())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new Single<Object>(gs)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new <Exception>Single<Object>(new Exception(), gs)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new Single<Exception>(new Exception())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new <Exception>Single<Exception>(new Exception(), new Exception())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new Single<Exception>(gs)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new <Exception>Single<Exception>(new Exception(), gs)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new Single<RuntimeException>(new RuntimeException())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new <RuntimeException>Single<RuntimeException>(new RuntimeException(), new RuntimeException())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new Single<RuntimeException>(gs)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new <RuntimeException>Single<RuntimeException>(new RuntimeException(), gs)] EQUIVALENT_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternSingleParamArguments05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.c.ref", false);
		search("Single(Object)", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object())] EXACT_RAW_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new Single<Object>(new Object())] EQUIVALENT_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternSingleParamArguments06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.c.ref", false);
		search("Single(Exception, Single<Exception>)", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new <Exception>Single<Object>(new Exception(), gs)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new <Exception>Single<Exception>(new Exception(), gs)] EQUIVALENT_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternSingleParamArguments07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.c.ref", false);
		search("Single<Exception>", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object(), new Throwable())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(gs)] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testSingle() [new Single(new Object(), gs)] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new Single<Object>(new Object())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new <Throwable>Single<Object>(new Object(), new Throwable())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new Single<Object>(gs)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new <Exception>Single<Object>(new Exception(), gs)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new Single<Exception>(new Exception())] EXACT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new <Exception>Single<Exception>(new Exception(), new Exception())] EXACT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new Single<Exception>(gs)] EXACT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new <Exception>Single<Exception>(new Exception(), gs)] EXACT_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new Single<RuntimeException>(new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new <RuntimeException>Single<RuntimeException>(new RuntimeException(), new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new Single<RuntimeException>(gs)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testRuntimeException() [new <RuntimeException>Single<RuntimeException>(new RuntimeException(), gs)] ERASURE_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternSingleParamArguments08() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.c.ref", false);
		search("Single<Exception>(Exception)", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new Single<Exception>(new Exception())] EXACT_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternSingleParamArguments09() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.c.ref", false);
		search("Single<? extends Exception>(Exception, Single<? super Exception>)", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testObject() [new <Exception>Single<Object>(new Exception(), gs)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new <Exception>Single<Exception>(new Exception(), gs)] EQUIVALENT_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternSingleParamArguments10() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<Exception>Single(Exception)", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new Single<Exception>(new Exception())] EXACT_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternSingleParamArguments11() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("g5.c.def.Single<Exception>(Exception)", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new Single<Exception>(new Exception())] EXACT_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternSingleParamArguments12() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("g5.c.def.<? extends Exception>Single<? extends Exception>(Exception)", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefSingle.java void g5.c.ref.RefSingle.testException() [new Single<Exception>(new Exception())] EQUIVALENT_MATCH"
		);
	}

	// Search string pattern references to multiple parameterized contructors
	public void testConstructorReferencesStringPatternMultipleParamArguments01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.c.ref", false);
		search("<?, ? extends Exception, ? super RuntimeException>Multiple", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new Multiple<Object, Object, Object>(new Object(), new Object(), new Object())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new <Object, Throwable, Exception>Multiple<Object, Object, Object>(gm, new Object(), new Throwable(), new Exception())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new Multiple<Object, Object, Object>(gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new <Object, Throwable, Exception>Multiple<Object, Object, Object>(new Object(), new Throwable(), new Exception(), gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(new Exception(), new Exception(), new Exception())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new <Exception, Exception, Exception>Multiple<Exception, Exception, Exception>(gm, new Exception(),new Exception(),new Exception())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(gm)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new <Exception, Exception, Exception>Multiple<Exception, Exception, Exception>(new Exception(),new Exception(),new Exception(), gm)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(), new RuntimeException(), new RuntimeException())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(gm, new RuntimeException(),new RuntimeException(),new RuntimeException())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new Multiple<RuntimeException, RuntimeException, RuntimeException>(gm)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(),new RuntimeException(),new RuntimeException(), gm)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(new Object(), new Object(), new Object())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(gm, new Object(), new Throwable(), new Exception())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(gm)] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(new Object(), new Throwable(), new Exception(), gm)] EQUIVALENT_RAW_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternMultipleParamArguments02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.c.ref", false);
		search("<Object, Exception, RuntimeException>Multiple", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new Multiple<Object, Object, Object>(new Object(), new Object(), new Object())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new <Object, Throwable, Exception>Multiple<Object, Object, Object>(gm, new Object(), new Throwable(), new Exception())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new Multiple<Object, Object, Object>(gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new <Object, Throwable, Exception>Multiple<Object, Object, Object>(new Object(), new Throwable(), new Exception(), gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(new Exception(), new Exception(), new Exception())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new <Exception, Exception, Exception>Multiple<Exception, Exception, Exception>(gm, new Exception(),new Exception(),new Exception())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new <Exception, Exception, Exception>Multiple<Exception, Exception, Exception>(new Exception(),new Exception(),new Exception(), gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(), new RuntimeException(), new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(gm, new RuntimeException(),new RuntimeException(),new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new Multiple<RuntimeException, RuntimeException, RuntimeException>(gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(),new RuntimeException(),new RuntimeException(), gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(new Object(), new Object(), new Object())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(gm, new Object(), new Throwable(), new Exception())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(gm)] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(new Object(), new Throwable(), new Exception(), gm)] EQUIVALENT_RAW_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternMultipleParamArguments03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.c.ref", false);
		search("Multiple", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new Multiple<Object, Object, Object>(new Object(), new Object(), new Object())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new <Object, Throwable, Exception>Multiple<Object, Object, Object>(gm, new Object(), new Throwable(), new Exception())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new Multiple<Object, Object, Object>(gm)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new <Object, Throwable, Exception>Multiple<Object, Object, Object>(new Object(), new Throwable(), new Exception(), gm)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(new Exception(), new Exception(), new Exception())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new <Exception, Exception, Exception>Multiple<Exception, Exception, Exception>(gm, new Exception(),new Exception(),new Exception())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(gm)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new <Exception, Exception, Exception>Multiple<Exception, Exception, Exception>(new Exception(),new Exception(),new Exception(), gm)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(), new RuntimeException(), new RuntimeException())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(gm, new RuntimeException(),new RuntimeException(),new RuntimeException())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new Multiple<RuntimeException, RuntimeException, RuntimeException>(gm)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(),new RuntimeException(),new RuntimeException(), gm)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(new Object(), new Object(), new Object())] EXACT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(gm, new Object(), new Throwable(), new Exception())] EXACT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(gm)] EXACT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(new Object(), new Throwable(), new Exception(), gm)] EXACT_RAW_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternMultipleParamArguments04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.c.ref", false);
		search("Multiple(Exception,Exception,Exception)", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(new Exception(), new Exception(), new Exception())] EQUIVALENT_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternMultipleParamArguments05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.c.ref", false);
		search("Multiple(RuntimeException,RuntimeException,RuntimeException,Multiple<RuntimeException, RuntimeException, RuntimeException>)", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(),new RuntimeException(),new RuntimeException(), gm)] EQUIVALENT_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternMultipleParamArguments06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.c.ref", false);
		search("Multiple<RuntimeException, RuntimeException, RuntimeException>", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new Multiple<Object, Object, Object>(new Object(), new Object(), new Object())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new <Object, Throwable, Exception>Multiple<Object, Object, Object>(gm, new Object(), new Throwable(), new Exception())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new Multiple<Object, Object, Object>(gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new <Object, Throwable, Exception>Multiple<Object, Object, Object>(new Object(), new Throwable(), new Exception(), gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(new Exception(), new Exception(), new Exception())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new <Exception, Exception, Exception>Multiple<Exception, Exception, Exception>(gm, new Exception(),new Exception(),new Exception())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new <Exception, Exception, Exception>Multiple<Exception, Exception, Exception>(new Exception(),new Exception(),new Exception(), gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(), new RuntimeException(), new RuntimeException())] EXACT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(gm, new RuntimeException(),new RuntimeException(),new RuntimeException())] EXACT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new Multiple<RuntimeException, RuntimeException, RuntimeException>(gm)] EXACT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(),new RuntimeException(),new RuntimeException(), gm)] EXACT_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(new Object(), new Object(), new Object())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(gm, new Object(), new Throwable(), new Exception())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(gm)] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(new Object(), new Throwable(), new Exception(), gm)] EQUIVALENT_RAW_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternMultipleParamArguments07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.c.ref", false);
		search("Multiple<?,? extends Throwable,? extends Exception>", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new Multiple<Object, Object, Object>(new Object(), new Object(), new Object())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new <Object, Throwable, Exception>Multiple<Object, Object, Object>(gm, new Object(), new Throwable(), new Exception())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new Multiple<Object, Object, Object>(gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new <Object, Throwable, Exception>Multiple<Object, Object, Object>(new Object(), new Throwable(), new Exception(), gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(new Exception(), new Exception(), new Exception())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new <Exception, Exception, Exception>Multiple<Exception, Exception, Exception>(gm, new Exception(),new Exception(),new Exception())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(gm)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new <Exception, Exception, Exception>Multiple<Exception, Exception, Exception>(new Exception(),new Exception(),new Exception(), gm)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(), new RuntimeException(), new RuntimeException())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(gm, new RuntimeException(),new RuntimeException(),new RuntimeException())] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new Multiple<RuntimeException, RuntimeException, RuntimeException>(gm)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new <RuntimeException, RuntimeException, RuntimeException>Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(),new RuntimeException(),new RuntimeException(), gm)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(new Object(), new Object(), new Object())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(gm, new Object(), new Throwable(), new Exception())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(gm)] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(new Object(), new Throwable(), new Exception(), gm)] EQUIVALENT_RAW_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternMultipleParamArguments08() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.c.ref", false);
		search("<RuntimeException, RuntimeException, RuntimeException>Multiple(*,*,*)", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new Multiple<Object, Object, Object>(new Object(), new Object(), new Object())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(new Exception(), new Exception(), new Exception())] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new Multiple<RuntimeException, RuntimeException, RuntimeException>(new RuntimeException(), new RuntimeException(), new RuntimeException())] EXACT_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(new Object(), new Object(), new Object())] EQUIVALENT_RAW_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternMultipleParamArguments09() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.c.ref", false);
		search("<?,? extends Throwable,? extends RuntimeException>Multiple(*)", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testObject() [new Multiple<Object, Object, Object>(gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(gm)] ERASURE_MATCH\n" +
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testRuntimeException() [new Multiple<RuntimeException, RuntimeException, RuntimeException>(gm)] EQUIVALENT_MATCH\n" +
			"src/g5/c/ref/RefRaw.java void g5.c.ref.RefRaw.testMultiple() [new Multiple(gm)] EQUIVALENT_RAW_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternMultipleParamArguments10() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<Object,Exception,Exception>Multiple(Exception,Exception,Exception)", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(new Exception(), new Exception(), new Exception())] ERASURE_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternMultipleParamArguments11() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("g5.c.def.Multiple<Exception,Exception,Exception>(Exception,Exception,Exception)", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(new Exception(), new Exception(), new Exception())] EXACT_MATCH"
		);
	}
	public void testConstructorReferencesStringPatternMultipleParamArguments12() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("g5.c.def.<?,? extends Throwable,? extends Throwable>Multiple<?,? extends Throwable,? extends Exception>(Exception,Exception,Exception)", CONSTRUCTOR, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/ref/RefMultiple.java void g5.c.ref.RefMultiple.testException() [new Multiple<Exception, Exception, Exception>(new Exception(), new Exception(), new Exception())] EQUIVALENT_MATCH"
		);
	}

	/*
	 * DECLARATIONS
	 */
	// Search references to single parameterized contructors
	public void testConstructorDeclarationsElementPatternSingleParamArguments01() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefSingle.java");
		IMethod method = selectMethod(unit, "Single", 9);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Single.java g5.c.def.Single(T) [Single] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsElementPatternSingleParamArguments02() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefSingle.java");
		IMethod method = selectMethod(unit, "Single", 10);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Single.java g5.c.def.Single(T, U) [Single] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsElementPatternSingleParamArguments03() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefSingle.java");
		IMethod method = selectMethod(unit, "Single", 11);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Single.java g5.c.def.Single(Single<T>) [Single] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsElementPatternSingleParamArguments04() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefSingle.java");
		IMethod method = selectMethod(unit, "Single", 12);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Single.java g5.c.def.Single(U, Single<T>) [Single] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationssElementPatternSingleParamArguments05() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefRaw.java");
		IMethod method = selectMethod(unit, "Single", 5);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Single.java g5.c.def.Single(T, U) [Single] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationssElementPatternSingleParamArguments06() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefRaw.java");
		IMethod method = selectMethod(unit, "Single", 7);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Single.java g5.c.def.Single(U, Single<T>) [Single] EXACT_MATCH"
		);
	}

	// Search references to multiple parameterized contructors
	public void testConstructorDeclarationsElementPatternMultipleParamArguments01() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefMultiple.java");
		IMethod method = selectMethod(unit, "Multiple", 8);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(T1, T2, T3) [Multiple] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsElementPatternMultipleParamArguments02() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefMultiple.java");
		IMethod method = selectMethod(unit, "Multiple", 9);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(Multiple<T1,T2,T3>, U1, U2, U3) [Multiple] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsElementPatternMultipleParamArguments03() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefMultiple.java");
		IMethod method = selectMethod(unit, "Multiple", 10);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(Multiple<T1,T2,T3>) [Multiple] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsElementPatternMultipleParamArguments04() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefMultiple.java");
		IMethod method = selectMethod(unit, "Multiple", 11);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(U1, U2, U3, Multiple<T1,T2,T3>) [Multiple] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationssElementPatternMultipleParamArguments05() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefRaw.java");
		IMethod method = selectMethod(unit, "Multiple", 5);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(Multiple<T1,T2,T3>, U1, U2, U3) [Multiple] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationssElementPatternMultipleParamArguments06() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/c/ref/RefRaw.java");
		IMethod method = selectMethod(unit, "Multiple", 7);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(U1, U2, U3, Multiple<T1,T2,T3>) [Multiple] EXACT_MATCH"
		);
	}

	// Search string pattern references to single parameterized contructors
	public void testConstructorDeclarationsStringPatternSingleParamArguments01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<Exception>Single", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Single.java g5.c.def.Single(T, U) [Single] EXACT_MATCH\n" +
			"src/g5/c/def/Single.java g5.c.def.Single(U, Single<T>) [Single] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsStringPatternSingleParamArguments02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<T>Single", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Single.java g5.c.def.Single(T, U) [Single] EXACT_MATCH\n" +
			"src/g5/c/def/Single.java g5.c.def.Single(U, Single<T>) [Single] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsStringPatternSingleParamArguments03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<U>S?ng*", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Single.java g5.c.def.Single(T, U) [Single] EXACT_MATCH\n" +
			"src/g5/c/def/Single.java g5.c.def.Single(U, Single<T>) [Single] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsStringPatternSingleParamArguments04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("Single", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Single.java g5.c.def.Single(T) [Single] EXACT_MATCH\n" +
			"src/g5/c/def/Single.java g5.c.def.Single(T, U) [Single] EXACT_MATCH\n" +
			"src/g5/c/def/Single.java g5.c.def.Single(Single<T>) [Single] EXACT_MATCH\n" +
			"src/g5/c/def/Single.java g5.c.def.Single(U, Single<T>) [Single] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsStringPatternSingleParamArguments05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("Single(*)", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Single.java g5.c.def.Single(T) [Single] EXACT_MATCH\n" +
			"src/g5/c/def/Single.java g5.c.def.Single(Single<T>) [Single] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsStringPatternSingleParamArguments06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("Single(*, *)", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Single.java g5.c.def.Single(T, U) [Single] EXACT_MATCH\n" +
			"src/g5/c/def/Single.java g5.c.def.Single(U, Single<T>) [Single] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsStringPatternSingleParamArguments07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("Single<Exception>", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Single.java g5.c.def.Single(T, U) [Single] EXACT_MATCH\n" +
			"src/g5/c/def/Single.java g5.c.def.Single(U, Single<T>) [Single] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsStringPatternSingleParamArguments08() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("Single<Exception>(*)", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			""
		);
	}
	public void testConstructorDeclarationsStringPatternSingleParamArguments09() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("Single<? extends Exception>(*, *)", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Single.java g5.c.def.Single(T, U) [Single] EXACT_MATCH\n" +
			"src/g5/c/def/Single.java g5.c.def.Single(U, Single<T>) [Single] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationssStringPatternSingleParamArguments10() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<Exception>Single", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Single.java g5.c.def.Single(T, U) [Single] EXACT_MATCH\n" +
			"src/g5/c/def/Single.java g5.c.def.Single(U, Single<T>) [Single] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationssStringPatternSingleParamArguments11() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("g5.c.def.Single<Exception>", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Single.java g5.c.def.Single(T, U) [Single] EXACT_MATCH\n" +
			"src/g5/c/def/Single.java g5.c.def.Single(U, Single<T>) [Single] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationssStringPatternSingleParamArguments12() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("g5.c.def.<? extends Exception>Single<? extends Exception>", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Single.java g5.c.def.Single(T, U) [Single] EXACT_MATCH\n" +
			"src/g5/c/def/Single.java g5.c.def.Single(U, Single<T>) [Single] EXACT_MATCH"
		);
	}

	// Search string pattern references to multiple parameterized contructors
	public void testConstructorDeclarationsStringPatternMultipleParamArguments01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<?, ? extends Exception, ? super RuntimeException>Multiple", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(Multiple<T1,T2,T3>, U1, U2, U3) [Multiple] EXACT_MATCH\n" +
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(U1, U2, U3, Multiple<T1,T2,T3>) [Multiple] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsStringPatternMultipleParamArguments02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<Object, Exception, RuntimeException>Multiple", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(Multiple<T1,T2,T3>, U1, U2, U3) [Multiple] EXACT_MATCH\n" +
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(U1, U2, U3, Multiple<T1,T2,T3>) [Multiple] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsStringPatternMultipleParamArguments03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("Multiple", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(T1, T2, T3) [Multiple] EXACT_MATCH\n" +
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(Multiple<T1,T2,T3>, U1, U2, U3) [Multiple] EXACT_MATCH\n" +
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(Multiple<T1,T2,T3>) [Multiple] EXACT_MATCH\n" +
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(U1, U2, U3, Multiple<T1,T2,T3>) [Multiple] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsStringPatternMultipleParamArguments04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("Multiple(*,*,*)", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(T1, T2, T3) [Multiple] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsStringPatternMultipleParamArguments05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("Multiple(*,*,*,*)", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(Multiple<T1,T2,T3>, U1, U2, U3) [Multiple] EXACT_MATCH\n" +
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(U1, U2, U3, Multiple<T1,T2,T3>) [Multiple] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsStringPatternMultipleParamArguments06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("Multiple<RuntimeException, RuntimeException, RuntimeException>", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(Multiple<T1,T2,T3>, U1, U2, U3) [Multiple] EXACT_MATCH\n" +
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(U1, U2, U3, Multiple<T1,T2,T3>) [Multiple] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsStringPatternMultipleParamArguments07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("Multiple<?,? extends Throwable,? extends Exception>", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(Multiple<T1,T2,T3>, U1, U2, U3) [Multiple] EXACT_MATCH\n" +
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(U1, U2, U3, Multiple<T1,T2,T3>) [Multiple] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationsStringPatternMultipleParamArguments08() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<RuntimeException, RuntimeException, RuntimeException>Multiple(*,*,*)", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			""
		);
	}
	public void testConstructorDeclarationsStringPatternMultipleParamArguments09() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<?,? extends Throwable,? extends RuntimeException>Multiple", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(Multiple<T1,T2,T3>, U1, U2, U3) [Multiple] EXACT_MATCH\n" +
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(U1, U2, U3, Multiple<T1,T2,T3>) [Multiple] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationssStringPatternMultipleParamArguments10() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<Object,Exception,Exception>Multiple(*,*,*,*)", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(Multiple<T1,T2,T3>, U1, U2, U3) [Multiple] EXACT_MATCH\n" +
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(U1, U2, U3, Multiple<T1,T2,T3>) [Multiple] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationssStringPatternMultipleParamArguments11() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("g5.c.def.Multiple<Object,Exception,Exception>(*,*,*,*)", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(Multiple<T1,T2,T3>, U1, U2, U3) [Multiple] EXACT_MATCH\n" +
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(U1, U2, U3, Multiple<T1,T2,T3>) [Multiple] EXACT_MATCH"
		);
	}
	public void testConstructorDeclarationssStringPatternMultipleParamArguments12() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("g5.c.def.<?,? extends Throwable,? extends Throwable>Multiple<?,? extends Throwable,? extends Exception>(*,*,*,*)", CONSTRUCTOR, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(Multiple<T1,T2,T3>, U1, U2, U3) [Multiple] EXACT_MATCH\n" +
			"src/g5/c/def/Multiple.java g5.c.def.Multiple(U1, U2, U3, Multiple<T1,T2,T3>) [Multiple] EXACT_MATCH"
		);
	}
}
