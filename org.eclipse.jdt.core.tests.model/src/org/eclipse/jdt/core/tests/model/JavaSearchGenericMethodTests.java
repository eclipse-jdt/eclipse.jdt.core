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
 * Test for generic methods search using R_ERASURE_MATCH rule.
 */
public class JavaSearchGenericMethodTests extends AbstractJavaSearchGenericTests {

	public JavaSearchGenericMethodTests(String name) {
		super(name, ERASURE_RULE);
	}
	// defined for sub-classes
	JavaSearchGenericMethodTests(String name, int matchRule) {
		super(name, matchRule);
	}
	public static Test suite() {
		return buildModelTestSuite(JavaSearchGenericMethodTests.class);
	}
	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_PREFIX = "testMethodReferencesElementPatternSingleParamArguments";
//		TESTS_NAMES = new String[] { "testMethodReferencesElementPatternMultipleParamArguments04" };
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
	// Search references to methods defined in a single type parameter class
	public void testMethodReferencesElementPatternSingleTypeParameter01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g5/m/def/Single.java").getType("Single");
		// search reference to a standard method
		IMethod method = type.getMethod("standard", new String[] { "QT;" });
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [standard(new Exception())] ERASURE_RAW_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [standard(new Object())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [standard(new Exception())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testRuntimeException() [standard(new RuntimeException())] ERASURE_MATCH"
		);
	}
	public void testMethodReferencesElementPatternSingleTypeParameter02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g5/m/def/Single.java").getType("Single");
		// search reference to a generic method
		IMethod method = type.getMethod("generic", new String[] { "QU;" });
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [generic(new Exception())] ERASURE_RAW_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [generic(new Object())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [generic(new Exception())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testRuntimeException() [generic(new RuntimeException())] ERASURE_MATCH"
		);
	}
	public void testMethodReferencesElementPatternSingleTypeParameter03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g5/m/def/Single.java").getType("Single");
		// search reference to a method returning a parameterized type
		IMethod method = type.getMethod("returnParamType", new String[] {});
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Single.java Single<T> g5.m.def.Single.complete(U, Single<T>) [returnParamType()] EXACT_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [returnParamType()] ERASURE_RAW_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [returnParamType()] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [returnParamType()] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testRuntimeException() [returnParamType()] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testUnbound() [returnParamType()] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testExtends() [returnParamType()] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testSuper() [returnParamType()] ERASURE_MATCH"
		);
	}
	public void testMethodReferencesElementPatternSingleTypeParameter04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g5/m/def/Single.java").getType("Single");
		// search reference to a method with parameterized type arguments
		IMethod method = type.getMethod("paramTypesArgs", new String[] { "QSingle<QT;>;" });
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [paramTypesArgs(gs)] ERASURE_RAW_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [paramTypesArgs(gs)] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [paramTypesArgs(gs)] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testRuntimeException() [paramTypesArgs(gs)] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testUnbound() [paramTypesArgs(gs)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testExtends() [paramTypesArgs(gs)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testSuper() [paramTypesArgs(gs)] POTENTIAL_MATCH"
		);
	}
	public void testMethodReferencesElementPatternSingleTypeParameter05() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g5/m/def/Single.java").getType("Single");
		// search reference to a generic method returning a param type with param type parameters (=complete)
		IMethod method = type.getMethod("complete", new String[] { "QU;", "QSingle<QT;>;" });
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [complete(new Exception(), gs)] ERASURE_RAW_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [complete(new Object(), gs)] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [complete(new Exception(), gs)] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testRuntimeException() [complete(new RuntimeException(), gs)] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testUnbound() [complete(new String(), gs)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testExtends() [complete(new Throwable(), gs)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testSuper() [complete(new RuntimeException(), gs)] POTENTIAL_MATCH"
		);
	}

	// Search references to methods defined in a multiple type parameters class
	public void testMethodReferencesElementPatternMultipleTypeParameter01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g5/m/def/Multiple.java").getType("Multiple");
		IMethod method = type.getMethod("standard", new String[] { "QT1;","QT2;","QT3;" });
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [standard(new Object(), new Exception(), new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testMultiple() [standard(new Object(), new Exception(), new RuntimeException())] ERASURE_RAW_MATCH"
		);
	}
	public void testMethodReferencesElementPatternMultipleTypeParameter02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g5/m/def/Multiple.java").getType("Multiple");
		IMethod method = type.getMethod("generic", new String[] { "QU1;","QU2;","QU3;" });
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [generic(new Object(), new Exception(), new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [generic(new Object(), new RuntimeException(), new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [generic(new Object(), new RuntimeException(), new IllegalMonitorStateException())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testMultiple() [generic(new Object(), new Exception(), new RuntimeException())] ERASURE_RAW_MATCH"
		);
	}
	public void testMethodReferencesElementPatternMultipleTypeParameter03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g5/m/def/Multiple.java").getType("Multiple");
		IMethod method = type.getMethod("returnParamType", new String[] {});
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [returnParamType()] EXACT_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [returnParamType()] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testUnbound() [returnParamType()] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [returnParamType()] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [returnParamType()] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testMultiple() [returnParamType()] ERASURE_RAW_MATCH"
		);
	}
	public void testMethodReferencesElementPatternMultipleTypeParameter04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g5/m/def/Multiple.java").getType("Multiple");
		IMethod method = type.getMethod("paramTypesArgs", new String[] { "QSingle<QT1;>;","QSingle<QT2;>;","QSingle<QT3;>;","QMultiple<QT1;QT2;QT3;>;" });
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [paramTypesArgs(new Single<Object>(), new Single<Exception>(), new Single<RuntimeException>(), gm)] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testUnbound() [paramTypesArgs(new Single<Object>(), new Single<Object>(), new Single<Object>(), gm)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [paramTypesArgs(new Single<Object>(), new Single<Throwable>(), new Single<Exception>(), gm)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [paramTypesArgs(new Single<Object>(), new Single<RuntimeException>(), new Single<RuntimeException>(), gm)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testMultiple() [paramTypesArgs(new Single<Object>(), new Single<Exception>(), new Single<RuntimeException>(), gm)] ERASURE_RAW_MATCH"
		);
	}
	public void testMethodReferencesElementPatternMultipleTypeParameter05() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g5/m/def/Multiple.java").getType("Multiple");
		IMethod method = type.getMethod("complete", new String[] { "QU1;","QU2;","QU3;", "QMultiple<QT1;QT2;QT3;>;" });
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [complete(new Object(), new Exception(), new RuntimeException(), gm)] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [complete(new Object(), new RuntimeException(), new RuntimeException(), gm)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [complete(new Object(), new RuntimeException(), new IllegalMonitorStateException(), gm)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testMultiple() [complete(new Object(), new Exception(), new RuntimeException(), gm)] ERASURE_RAW_MATCH"
		);
	}

	// Search references to single parameterized methods
	public void testMethodReferencesElementPatternSingleParamArguments01() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefSingle.java");
		IMethod method = selectMethod(unit, "generic", 3);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [generic(new Exception())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [<Object>generic(new Object())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [generic(new Exception())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testRuntimeException() [generic(new RuntimeException())] ERASURE_MATCH"
		);
	}
	public void testMethodReferencesElementPatternSingleParamArguments02() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefSingle.java");
		IMethod method = selectMethod(unit, "generic", 6);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [generic(new Exception())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [generic(new Object())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [<Exception>generic(new Exception())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testRuntimeException() [generic(new RuntimeException())] ERASURE_MATCH"
		);
	}
	public void testMethodReferencesElementPatternSingleParamArguments03() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefSingle.java");
		IMethod method = selectMethod(unit, "complete");
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [complete(new Exception(), gs)] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [<Object>complete(new Object(), gs)] EXACT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [complete(new Exception(), gs)] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testRuntimeException() [complete(new RuntimeException(), gs)] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testUnbound() [complete(new String(), gs)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testExtends() [complete(new Throwable(), gs)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testSuper() [complete(new RuntimeException(), gs)] POTENTIAL_MATCH"
		);
	}
	public void testMethodReferencesElementPatternSingleParamArguments04() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefSingle.java");
		IMethod method = selectMethod(unit, "complete", 2);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [complete(new Exception(), gs)] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [complete(new Object(), gs)] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [<Exception>complete(new Exception(), gs)] EXACT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testRuntimeException() [complete(new RuntimeException(), gs)] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testUnbound() [complete(new String(), gs)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testExtends() [complete(new Throwable(), gs)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testSuper() [complete(new RuntimeException(), gs)] POTENTIAL_MATCH"
		);
	}
	public void testMethodReferencesElementPatternSingleParamArguments05() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefRaw.java");
		IMethod method = selectMethod(unit, "generic", 2);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [generic(new Exception())] EXACT_RAW_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [generic(new Object())] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [generic(new Exception())] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testRuntimeException() [generic(new RuntimeException())] EQUIVALENT_MATCH"
		);
	}
	public void testMethodReferencesElementPatternSingleParamArguments06() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefRaw.java");
		IMethod method = selectMethod(unit, "complete");
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [complete(new Exception(), gs)] EXACT_RAW_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [complete(new Object(), gs)] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [complete(new Exception(), gs)] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testRuntimeException() [complete(new RuntimeException(), gs)] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testUnbound() [complete(new String(), gs)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testExtends() [complete(new Throwable(), gs)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testSuper() [complete(new RuntimeException(), gs)] POTENTIAL_MATCH"
		);
	}

	// Search references to multiple parameterized methods
	public void testMethodReferencesElementPatternMultipleParamArguments01() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefMultiple.java");
		IMethod method = selectMethod(unit, "generic", 2);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [<Object, Exception, RuntimeException>generic(new Object(), new Exception(), new RuntimeException())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [generic(new Object(), new RuntimeException(), new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [generic(new Object(), new RuntimeException(), new IllegalMonitorStateException())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testMultiple() [generic(new Object(), new Exception(), new RuntimeException())] EQUIVALENT_RAW_MATCH"
		);
	}
	public void testMethodReferencesElementPatternMultipleParamArguments02() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefMultiple.java");
		IMethod method = selectMethod(unit, "generic", 6);
		search(method, REFERENCES);
		assertSearchResults(
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [generic(new Object(), new Exception(), new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [<Object, RuntimeException, RuntimeException>generic(new Object(), new RuntimeException(), new RuntimeException())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [generic(new Object(), new RuntimeException(), new IllegalMonitorStateException())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testMultiple() [generic(new Object(), new Exception(), new RuntimeException())] EQUIVALENT_RAW_MATCH"
		);
	}
	public void testMethodReferencesElementPatternMultipleParamArguments03() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefMultiple.java");
		IMethod method = selectMethod(unit, "complete");
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [<Object, Exception, RuntimeException>complete(new Object(), new Exception(), new RuntimeException(), gm)] EXACT_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [complete(new Object(), new RuntimeException(), new RuntimeException(), gm)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [complete(new Object(), new RuntimeException(), new IllegalMonitorStateException(), gm)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testMultiple() [complete(new Object(), new Exception(), new RuntimeException(), gm)] EQUIVALENT_RAW_MATCH"
		);
	}
	public void testMethodReferencesElementPatternMultipleParamArguments04() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefMultiple.java");
		IMethod method = selectMethod(unit, "complete", 2);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [complete(new Object(), new Exception(), new RuntimeException(), gm)] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [<Object, RuntimeException, RuntimeException>complete(new Object(), new RuntimeException(), new RuntimeException(), gm)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [complete(new Object(), new RuntimeException(), new IllegalMonitorStateException(), gm)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testMultiple() [complete(new Object(), new Exception(), new RuntimeException(), gm)] EQUIVALENT_RAW_MATCH"
		);
	}
	public void testMethodReferencesElementPatternMultipleParamArguments05() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefRaw.java");
		IMethod method = selectMethod(unit, "generic", 4);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [generic(new Object(), new Exception(), new RuntimeException())] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [generic(new Object(), new RuntimeException(), new RuntimeException())] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [generic(new Object(), new RuntimeException(), new IllegalMonitorStateException())] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testMultiple() [generic(new Object(), new Exception(), new RuntimeException())] EXACT_RAW_MATCH"
		);
	}
	public void testMethodReferencesElementPatternMultipleParamArguments06() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefRaw.java");
		IMethod method = selectMethod(unit, "complete", 2);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [complete(new Object(), new Exception(), new RuntimeException(), gm)] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [complete(new Object(), new RuntimeException(), new RuntimeException(), gm)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [complete(new Object(), new RuntimeException(), new IllegalMonitorStateException(), gm)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testMultiple() [complete(new Object(), new Exception(), new RuntimeException(), gm)] EXACT_RAW_MATCH"
		);
	}

	// Search string pattern references to single parameterized methods
	public void testMethodReferencesStringPatternSingleParamArguments01() throws CoreException {
		search("<Exception>generic", METHOD, REFERENCES);
		assertSearchResults(
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [generic(new Exception())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [generic(new Object())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [<Exception>generic(new Exception())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testRuntimeException() [generic(new RuntimeException())] ERASURE_MATCH"
		);
	}
	public void testMethodReferencesStringPatternSingleParamArguments02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<? extends Exception> complete ", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [complete(new Exception(), gs)] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [complete(new Object(), gs)] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [<Exception>complete(new Exception(), gs)] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testRuntimeException() [<RuntimeException>complete(new RuntimeException(), gs)] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testUnbound() [complete(new String(), gs)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testExtends() [complete(new Throwable(), gs)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testSuper() [<RuntimeException>complete(new RuntimeException(), gs)] POTENTIAL_MATCH"
		);
	}
	public void testMethodReferencesStringPatternSingleParamArguments03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<? super Exception>*e?e*", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [generic(new Exception())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [complete(new Exception(), gs)] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [<Object>generic(new Object())] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [<Object>complete(new Object(), gs)] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [<Exception>generic(new Exception())] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [<Exception>complete(new Exception(), gs)] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testRuntimeException() [generic(new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testRuntimeException() [complete(new RuntimeException(), gs)] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testUnbound() [complete(new String(), gs)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testExtends() [<Throwable>complete(new Throwable(), gs)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testSuper() [complete(new RuntimeException(), gs)] POTENTIAL_MATCH"
		);
	}
	public void testMethodReferencesStringPatternSingleParamArguments04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g5.m.ref", false);
		search("generic", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [generic(new Object(), new Exception(), new RuntimeException())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [generic(new Object(), new RuntimeException(), new RuntimeException())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [generic(new Object(), new RuntimeException(), new IllegalMonitorStateException())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [generic(new Exception())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testMultiple() [generic(new Object(), new Exception(), new RuntimeException())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [generic(new Object())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [generic(new Exception())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testRuntimeException() [generic(new RuntimeException())] EXACT_MATCH"
		);
	}
	public void testMethodReferencesStringPatternSingleParamArguments05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("generic(Object)", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			/* Results while resolving string pattern with no qualification (currently disabled as it is not comaptible with previous results):
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [generic(new Exception())] EQUIVALENT_RAW_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [generic(new Object())] EQUIVALENT_RAW_MATCH"
			*/
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [generic(new Exception())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [generic(new Object())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [generic(new Exception())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testRuntimeException() [generic(new RuntimeException())] EXACT_MATCH"
		);
	}
	public void testMethodReferencesStringPatternSingleParamArguments06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("complete(Exception, Single<Exception>)", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			/* Results while resolving string pattern with no qualification (currently disabled as it is not comaptible with previous results):
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [complete(new Exception(), gs)] EQUIVALENT_RAW_MATCH"
			*/
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testSingle() [complete(new Exception(), gs)] EXACT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testObject() [complete(new Object(), gs)] EXACT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [complete(new Exception(), gs)] EXACT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testRuntimeException() [complete(new RuntimeException(), gs)] EXACT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testUnbound() [complete(new String(), gs)] EXACT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testExtends() [complete(new Throwable(), gs)] EXACT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testSuper() [complete(new RuntimeException(), gs)] EXACT_MATCH"
		);
	}
	public void testMethodReferencesStringPatternSingleParamArguments07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<Exception> generic ( Exception ) ", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [<Exception>generic(new Exception())] EXACT_MATCH"
		);
	}
	public void testMethodReferencesStringPatternSingleParamArguments08() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<? extends Exception>complete(Exception, Single<? super Exception>)", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [<Exception>complete(new Exception(), gs)] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testUnbound() [complete(new String(), gs)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testExtends() [complete(new Throwable(), gs)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testSuper() [complete(new RuntimeException(), gs)] POTENTIAL_MATCH"
		);
	}
	public void testMethodReferencesStringPatternSingleParamArguments09() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("Single.generic(Exception)", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [generic(new Exception())] EQUIVALENT_MATCH"
		);
	}
	public void testMethodReferencesStringPatternSingleParamArguments10() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("g5.m.def.Single<Exception>.generic(Exception)", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [generic(new Exception())] EQUIVALENT_MATCH"
		);
	}
	public void testMethodReferencesStringPatternSingleParamArguments11() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("g5.m.def.Single<? extends Exception>.<? extends Exception>generic(Exception)", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefSingle.java void g5.m.ref.RefSingle.testException() [<Exception>generic(new Exception())] EQUIVALENT_MATCH"
		);
	}

	// Search string pattern references to multiple parameterized methods
	public void testMethodReferencesStringPatternMultipleParamArguments01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<?, ? extends Exception, ? super RuntimeException>generic", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [<Object, Exception, RuntimeException>generic(new Object(), new Exception(), new RuntimeException())] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [<Object, RuntimeException, RuntimeException>generic(new Object(), new RuntimeException(), new RuntimeException())] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [generic(new Object(), new RuntimeException(), new IllegalMonitorStateException())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testMultiple() [generic(new Object(), new Exception(), new RuntimeException())] EQUIVALENT_RAW_MATCH"
		);
	}
	public void testMethodReferencesStringPatternMultipleParamArguments02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<Object, Exception, RuntimeException>complete", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [<Object, Exception, RuntimeException>complete(new Object(), new Exception(), new RuntimeException(), gm)] EXACT_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [complete(new Object(), new RuntimeException(), new RuntimeException(), gm)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [complete(new Object(), new RuntimeException(), new IllegalMonitorStateException(), gm)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testMultiple() [complete(new Object(), new Exception(), new RuntimeException(), gm)] EQUIVALENT_RAW_MATCH"
		);
	}
	public void testMethodReferencesStringPatternMultipleParamArguments04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("generic(Object,Exception,RuntimeException)", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			/* Results while resolving string pattern with no qualification (currently disabled as it is not comaptible with previous results):
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [generic(new Object(), new Exception(), new RuntimeException())] EQUIVALENT_RAW_MATCH"
			*/
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [generic(new Object(), new Exception(), new RuntimeException())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [generic(new Object(), new RuntimeException(), new RuntimeException())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [generic(new Object(), new RuntimeException(), new IllegalMonitorStateException())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testMultiple() [generic(new Object(), new Exception(), new RuntimeException())] EXACT_MATCH"
		);
	}
	public void testMethodReferencesStringPatternMultipleParamArguments05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("complete(Object,RuntimeException,RuntimeException,Multiple<Object, RuntimeException, RuntimeException>)", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			/* Results while resolving string pattern with no qualification (currently disabled as it is not comaptible with previous results):
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [complete(new Object(), new RuntimeException(), new RuntimeException(), gm)] EQUIVALENT_RAW_MATCH"
			*/
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [complete(new Object(), new Exception(), new RuntimeException(), gm)] EXACT_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [complete(new Object(), new RuntimeException(), new RuntimeException(), gm)] EXACT_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [complete(new Object(), new RuntimeException(), new IllegalMonitorStateException(), gm)] EXACT_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testMultiple() [complete(new Object(), new Exception(), new RuntimeException(), gm)] EXACT_MATCH"
		);
	}
	public void testMethodReferencesStringPatternMultipleParamArguments06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<Object, RuntimeException, RuntimeException>generic(*,*,*)", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [generic(new Object(), new Exception(), new RuntimeException())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [<Object, RuntimeException, RuntimeException>generic(new Object(), new RuntimeException(), new RuntimeException())] EXACT_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [generic(new Object(), new RuntimeException(), new IllegalMonitorStateException())] ERASURE_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testMultiple() [generic(new Object(), new Exception(), new RuntimeException())] EQUIVALENT_RAW_MATCH"
		);
	}
	public void testMethodReferencesStringPatternMultipleParamArguments07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<?,? extends Throwable,? extends RuntimeException>complete", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [<Object, Exception, RuntimeException>complete(new Object(), new Exception(), new RuntimeException(), gm)] EQUIVALENT_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testExtends() [<Object, RuntimeException, RuntimeException>complete(new Object(), new RuntimeException(), new RuntimeException(), gm)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.testSuper() [<Object, RuntimeException, IllegalMonitorStateException>complete(new Object(), new RuntimeException(), new IllegalMonitorStateException(), gm)] POTENTIAL_MATCH\n" +
			"src/g5/m/ref/RefRaw.java void g5.m.ref.RefRaw.testMultiple() [complete(new Object(), new Exception(), new RuntimeException(), gm)] EQUIVALENT_RAW_MATCH"
		);
	}
	public void testMethodReferencesStringPatternMultipleParamArguments08() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("Multiple.generic(Object,Exception,RuntimeException)", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [generic(new Object(), new Exception(), new RuntimeException())] EQUIVALENT_MATCH"
		);
	}
	public void testMethodReferencesStringPatternMultipleParamArguments09() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("g5.m.def.Multiple<Object, RuntimeException, RuntimeException>.generic(Object,Exception,RuntimeException)", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [generic(new Object(), new Exception(), new RuntimeException())] ERASURE_MATCH"
		);
	}
	public void testMethodReferencesStringPatternMultipleParamArguments10() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("g5.m.def.Multiple<?,? extends Throwable,? extends RuntimeException>.<?,? extends Throwable,? extends RuntimeException>generic(Object,Exception,RuntimeException)", METHOD, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/ref/RefMultiple.java void g5.m.ref.RefMultiple.test() [<Object, Exception, RuntimeException>generic(new Object(), new Exception(), new RuntimeException())] EQUIVALENT_MATCH"
		);
	}

	/*
	 * DECLARATIONS
	 */
	// Search references to single parameterized methods
	public void testMethodDeclarationsElementPatternSingleParamArguments01() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefSingle.java");
		IMethod method = selectMethod(unit, "generic", 3);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Single.java T g5.m.def.Single.generic(U) [generic] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsElementPatternSingleParamArguments02() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefSingle.java");
		IMethod method = selectMethod(unit, "generic", 6);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Single.java T g5.m.def.Single.generic(U) [generic] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsElementPatternSingleParamArguments03() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefRaw.java");
		IMethod method = selectMethod(unit, "generic", 2);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Single.java T g5.m.def.Single.generic(U) [generic] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsElementPatternSingleParamArguments04() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefSingle.java");
		IMethod method = selectMethod(unit, "complete");
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Single.java Single<T> g5.m.def.Single.complete(U, Single<T>) [complete] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsElementPatternSingleParamArguments05() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefSingle.java");
		IMethod method = selectMethod(unit, "complete", 2);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Single.java Single<T> g5.m.def.Single.complete(U, Single<T>) [complete] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsElementPatternSingleParamArguments06() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefRaw.java");
		IMethod method = selectMethod(unit, "complete");
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Single.java Single<T> g5.m.def.Single.complete(U, Single<T>) [complete] EXACT_MATCH"
		);
	}

	// Search references to multiple parameterized methods
	public void testMethodDeclarationsElementPatternMultipleParamArguments01() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefMultiple.java");
		IMethod method = selectMethod(unit, "generic", 2);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Multiple.java T1 g5.m.def.Multiple.generic(U1, U2, U3) [generic] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsElementPatternMultipleParamArguments02() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefMultiple.java");
		IMethod method = selectMethod(unit, "generic", 6);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Multiple.java T1 g5.m.def.Multiple.generic(U1, U2, U3) [generic] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsElementPatternMultipleParamArguments03() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefRaw.java");
		IMethod method = selectMethod(unit, "generic", 4);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Multiple.java T1 g5.m.def.Multiple.generic(U1, U2, U3) [generic] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsElementPatternMultipleParamArguments04() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefMultiple.java");
		IMethod method = selectMethod(unit, "complete");
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [complete] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsElementPatternMultipleParamArguments05() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefMultiple.java");
		IMethod method = selectMethod(unit, "complete", 2);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [complete] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsElementPatternMultipleParamArguments06() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g5/m/ref/RefRaw.java");
		IMethod method = selectMethod(unit, "complete", 2);
		IJavaSearchScope scope = getJavaSearchScope15();
		search(method, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [complete] EXACT_MATCH"
		);
	}

	// Search string pattern references to single parameterized methods
	public void testMethodDeclarationsStringPatternSingleParamArguments01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<Exception>generic", METHOD, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Single.java T g5.m.def.Single.generic(U) [generic] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsStringPatternSingleParamArguments02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<U>complete", METHOD, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Single.java Single<T> g5.m.def.Single.complete(U, Single<T>) [complete] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsStringPatternSingleParamArguments03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<T> *e?e*", METHOD, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Single.java T g5.m.def.Single.generic(U) [generic] EXACT_MATCH\n" +
			"src/g5/m/def/Single.java Single<T> g5.m.def.Single.complete(U, Single<T>) [complete] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsStringPatternSingleParamArguments04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("generic(*)", METHOD, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Single.java T g5.m.def.Single.generic(U) [generic] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsStringPatternSingleParamArguments05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<Exception>generic(*)", METHOD, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Single.java T g5.m.def.Single.generic(U) [generic] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsStringPatternSingleParamArguments06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("Single.<Object>generic", METHOD, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Single.java T g5.m.def.Single.generic(U) [generic] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsStringPatternSingleParamArguments07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("g5.m.def.Single<Object>.<U>generic", METHOD, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Single.java T g5.m.def.Single.generic(U) [generic] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsStringPatternSingleParamArguments08() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("g5.m.def.Single<?>.<?>generic", METHOD, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Single.java T g5.m.def.Single.generic(U) [generic] EXACT_MATCH"
		);
	}

	// Search string pattern references to multiple parameterized methods
	public void testMethodDeclarationsStringPatternMultipleParamArguments01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<?, ? extends Exception, ? super RuntimeException>generic", METHOD, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Multiple.java T1 g5.m.def.Multiple.generic(U1, U2, U3) [generic] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsStringPatternMultipleParamArguments02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<Object, Exception, RuntimeException>complete", METHOD, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [complete] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsStringPatternMultipleParamArguments03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("complete(*,*,*,*)", METHOD, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [complete] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsStringPatternMultipleParamArguments04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("<Object, Exception, RuntimeException>complete(*,*,*,*)", METHOD, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [complete] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsStringPatternMultipleParamArguments05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("Multiple.<Object, Exception, RuntimeException>complete", METHOD, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [complete] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsStringPatternMultipleParamArguments06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("g5.m.def.Multiple<Object, Exception, RuntimeException>.<U1,U2,U3>complete", METHOD, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [complete] EXACT_MATCH"
		);
	}
	public void testMethodDeclarationsStringPatternMultipleParamArguments07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("g5.m.def.Multiple<?,?,?>.<?,?,?>complete", METHOD, DECLARATIONS, scope, this.resultCollector);
		assertSearchResults(
			"src/g5/m/def/Multiple.java Multiple<T1,T2,T3> g5.m.def.Multiple.complete(U1, U2, U3, Multiple<T1,T2,T3>) [complete] EXACT_MATCH"
		);
	}
}
