/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
 * Test generic type search.
 */
public class JavaSearchGenericTypeTests extends JavaSearchTests {
		
	public JavaSearchGenericTypeTests(String name) {
		super(name);
	}
	public static Test suite() {
		return buildTestSuite(JavaSearchGenericTypeTests.class);
	}
	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
		// Prefix for names of tests to run
//		TESTS_PREFIX =  "testStringPatternNestedParam";
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		TESTS_NAMES = new String[] { "testGenericFieldReferenceAC04" };
		// Numbers of tests to run: "test<number>" will be run for each number of this array
//		TESTS_NUMBERS = new int[] { 8 };
		// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		TESTS_RANGE = new int[] { -1, -1 };
	}
	
	protected void setUp () throws Exception {
		super.setUp();
		this.resultCollector.showAccuracy = true;
	}

	/**
	 * Type reference for 1.5.
	 * Bug 73336: [1.5][search] Search Engine does not find type references of actual generic type parameters
	 * (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=73336)
	 */
	public void testTypeReferenceBug73336() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/bug73336/A.java").getType("A");
		
		search(type,
			REFERENCES,
			getJavaSearchScope15("bug73336", false),
			resultCollector);
		assertSearchResults(
			"src/bug73336/AA.java bug73336.AA [A] EXACT_MATCH\n" + 
			"src/bug73336/B.java bug73336.B [A] EXACT_MATCH\n" + 
			"src/bug73336/B.java bug73336.B [A] EXACT_MATCH\n" + 
			"src/bug73336/C.java bug73336.C [A] EXACT_MATCH\n" + 
			"src/bug73336/C.java void bug73336.C.foo() [A] EXACT_MATCH\n" + 
			"src/bug73336/C.java void bug73336.C.foo() [A] EXACT_MATCH",
			resultCollector);
	}
	public void testTypeReferenceBug73336b() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/bug73336b/A.java").getType("A");
		
		search(type,
			REFERENCES,
			getJavaSearchScope15("bug73336b", false), 
			resultCollector);
		assertSearchResults(
			"src/bug73336b/B.java bug73336b.B [A] EXACT_MATCH\n" + 
			"src/bug73336b/B.java bug73336b.B [A] EXACT_MATCH\n" + 
			"src/bug73336b/C.java bug73336b.C [A] EXACT_MATCH\n" + 
			"src/bug73336b/C.java bug73336b.C [A] EXACT_MATCH\n" + 
			"src/bug73336b/C.java bug73336b.C [A] EXACT_MATCH\n" + 
			"src/bug73336b/C.java bug73336b.C() [A] EXACT_MATCH\n" + 
			"src/bug73336b/C.java bug73336b.C() [A] EXACT_MATCH",
			resultCollector);
	}
	// Verify that no NPE was raised on following case
	public void testTypeReferenceBug73336c() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/bug73336c/A.java").getType("A");
		
		search(type,
			REFERENCES,
			getJavaSearchScope15("bug73336c", false), 
			resultCollector);
		assertSearchResults(
				"src/bug73336c/B.java bug73336c.B [A] EXACT_MATCH\n" + 
				"src/bug73336c/B.java bug73336c.B [A] EXACT_MATCH\n" + 
				"src/bug73336c/C.java bug73336c.C [A] EXACT_MATCH\n" + 
				"src/bug73336c/C.java bug73336c.C [A] EXACT_MATCH\n" + 
				"src/bug73336c/C.java bug73336c.C [A] EXACT_MATCH",
			resultCollector);
	}

	/**
	 * Bug 75641: [1.5][Search] Types search does not work with generics
	 * (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=75641)
	 */
	/*
	 * Following functionalities are tested:
	 * 	A) Search using an IJavaElement
	 * 		a) single parameter generic types
	 * 		b) multiple parameters generic types
	 * 	B) Search using a not parameterized string pattern
	 * 		a) simple name
	 * 		b) any string characters
	 * 	C) Search using a single parameterized string pattern
	 * 		a) no wildcard
	 * 		b) wildcard extends
	 * 		c) wildcard super
	 * 		d) wildcard unbound
	 * 	D) Search using a multiple parameterized string pattern
	 * 		a) no wildcard
	 * 			GenericTypeReferenceDA* tests
	 * 		b) wildcard extends
	 * 			GenericTypeReferenceDB* tests
	 * 		c) wildcard super
	 * 			GenericTypeReferenceDC* tests
	 * 		d) wildcard unbound
	 * 			GenericTypeReferenceDD* tests
	 */
	// Search reference to a generic type
	public void testElementPatternSingleParam01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R1.java [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [g1.t.s.def.Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type declared in a generic type
	public void testElementPatternSingleParam02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getType("Member");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [g1.t.s.def.Generic<Object>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [g1.t.s.def.Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [g1.t.s.def.Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [g1.t.s.def.Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [g1.t.s.def.Generic<? super RuntimeException>.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type declared in a generic type
	public void testElementPatternSingleParam03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getType("MemberGeneric");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic<Object>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [g1.t.s.def.Generic<Object>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [g1.t.s.def.Generic<Exception>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [g1.t.s.def.Generic<?>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [g1.t.s.def.Generic<? extends Throwable>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [g1.t.s.def.Generic<? super RuntimeException>.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type declared in a non-generic type
	public void testElementPatternSingleParam04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/NonGeneric.java").getType("NonGeneric").getType("GenericMember");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH",
			resultCollector);
	}

	// Search reference to a generic type
	public void testElementPatternMultipleParam01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_obj [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [g1.t.m.def.Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type declared in a generic type
	public void testElementPatternMultipleParam02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getType("Member");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [g1.t.m.def.Generic<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [g1.t.m.def.Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type declared in a generic type
	public void testElementPatternMultipleParam03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getType("MemberGeneric");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic<Object, Exception, RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [g1.t.m.def.Generic<Object, Exception, RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [g1.t.m.def.Generic<?, ?, ?>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type declared in a non-generic type
	public void testElementPatternMultipleParam04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/NonGeneric.java").getType("NonGeneric").getType("GenericMember");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH",
			resultCollector);
	}

	// Search reference with nested parameterized types
	public void testElementPatternNestedParam01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GS.java").getType("GS");
		ICompilationUnit ref = getCompilationUnit("JavaSearch15/src/g3/t/ref/R1.java");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new ICompilationUnit[] {ref});
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R1.java [g3.t.def.GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_wld [GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_obj [GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_wld [GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_wld [GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_wld [GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_wld [g3.t.def.GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [g3.t.def.GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_obj [g3.t.def.GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_wld [g3.t.def.GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_wld [g3.t.def.GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_wld [g3.t.def.GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [g3.t.def.GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [g3.t.def.GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [g3.t.def.GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [g3.t.def.GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [g3.t.def.GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [g3.t.def.GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [g3.t.def.GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [g3.t.def.GS] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [g3.t.def.GS] EXACT_MATCH",
			resultCollector);
	}
	public void testElementPatternNestedParam02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GS.java").getType("GS").getType("Member");
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_wld [GS<GM<?, ?, ?>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GS<GM<GM<?, ?, ?>.Member,GM<?, ?, ?>.Member,GM<?, ?, ?>.Member>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_obj [GS<GM<Object, Exception, RuntimeException>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<GS<?>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<GS<GS<?>.Member>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<GS<?>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_obj [GS<Object>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_obj [GS<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_obj [GS<? super RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_wld [g3.t.def.GS<g3.t.def.GM<?, ?, ?>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GS<g3.t.def.GM<g3.t.def.GM<?, ?, ?>.Member, g3.t.def.GM<?, ?, ?>.Member, g3.t.def.GM<?, ?, ?>.Member>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_obj [g3.t.def.GS<g3.t.def.GM<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [g3.t.def.GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [g3.t.def.GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [g3.t.def.GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GS<g3.t.def.GS<?>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>.Member>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GS<g3.t.def.GS<?>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [g3.t.def.GS<java.lang.Object>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [g3.t.def.GS<? extends java.lang.Throwable>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [g3.t.def.GS<? super java.lang.RuntimeException>.Member] EXACT_MATCH",
			resultCollector);
	}
	public void testElementPatternNestedParam03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GS.java").getType("GS").getType("Generic");
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_wld [GS<GM<?, ?, ?>.Generic<?, ?, ?>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GS<GM<GM<?, ?, ?>.Generic<?,?,?>,GM<?, ?, ?>.Generic<?,?,?>,GM<?, ?, ?>.Generic<?,?,?>>.Generic<?,?,?>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_obj [GS<GM<Object, Exception, RuntimeException>.Generic<Object, Exception, RuntimeException>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [GS<?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [GS<?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [GS<?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<GS<?>.Generic<?>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<GS<GS<?>.Generic<?>>.Generic<?>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<GS<?>.Generic<?>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [GS<Object>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [GS<? extends Throwable>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [GS<? super RuntimeException>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_wld [g3.t.def.GS<g3.t.def.GM<?, ?, ?>.Generic<?, ?, ?>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [g3.t.def.GS<g3.t.def.GM<g3.t.def.GM<?, ?, ?>.Generic<?,?,?>,g3.t.def.GM<?, ?, ?>.Generic<?,?,?>,g3.t.def.GM<?, ?, ?>.Generic<?,?,?>>.Generic<?,?,?>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_obj [g3.t.def.GS<g3.t.def.GM<Object, Exception, RuntimeException>.Generic<Object, Exception, RuntimeException>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [g3.t.def.GS<?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [g3.t.def.GS<?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [g3.t.def.GS<?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [g3.t.def.GS<?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [g3.t.def.GS<g3.t.def.GS<?>.Generic<?>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [g3.t.def.GS<?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>.Generic<?>>.Generic<?>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [g3.t.def.GS<g3.t.def.GS<?>.Generic<?>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [g3.t.def.GS<?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [g3.t.def.GS<Object>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [g3.t.def.GS<? extends Throwable>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [g3.t.def.GS<? super RuntimeException>.Generic] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testElementPatternNestedParam04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GS.java").getType("NGS").getType("Generic");
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_wld [NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_obj [NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_obj [NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_obj [NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_obj [NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_wld [g3.t.def.NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [g3.t.def.NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_obj [g3.t.def.NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [g3.t.def.NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [g3.t.def.NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [g3.t.def.NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [g3.t.def.NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [g3.t.def.NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [g3.t.def.NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [g3.t.def.NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [g3.t.def.NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [g3.t.def.NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [g3.t.def.NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [g3.t.def.NGS.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [g3.t.def.NGS.Generic] EXACT_MATCH",
			resultCollector);
	}
	public void testElementPatternNestedParam05() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GM.java").getType("GM");
		ICompilationUnit ref = getCompilationUnit("JavaSearch15/src/g3/t/ref/R1.java");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new ICompilationUnit[] {ref});
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R1.java [g3.t.def.GM] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_wld [GM] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [GM] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [GM] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [GM] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [GM] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_obj [GM] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_wld [GM] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GM] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [GM] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_wld [g3.t.def.GM] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [g3.t.def.GM] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [g3.t.def.GM] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [g3.t.def.GM] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [g3.t.def.GM] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_obj [g3.t.def.GM] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_wld [g3.t.def.GM] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [g3.t.def.GM] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [g3.t.def.GM] EXACT_MATCH",
			resultCollector);
	}
	public void testElementPatternNestedParam06() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GM.java").getType("GM").getType("Member");
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_wld [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<GM<?, ?, ?>.Member,GM<?, ?, ?>.Member,GM<?, ?, ?>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_obj [GM<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GM<GS<?>.Member, GS<?>.Member, GS<?>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GM<GS<?>.Member, GS<GS<?>.Member>.Member, GS<GS<GS<?>.Member>.Member>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_obj [GM<GS<Object>.Member, GS<? extends Throwable>.Member, GS<? super RuntimeException>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_wld [g3.t.def.GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<g3.t.def.GM<?, ?, ?>.Member, g3.t.def.GM<?, ?, ?>.Member, g3.t.def.GM<?, ?, ?>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_obj [g3.t.def.GM<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [g3.t.def.GM<g3.t.def.GS<?>.Member, g3.t.def.GS<?>.Member, g3.t.def.GS<?>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GM<g3.t.def.GS<?>.Member, g3.t.def.GS<g3.t.def.GS<?>.Member>.Member, g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>.Member>.Member>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [g3.t.def.GM<g3.t.def.GS<java.lang.Object>.Member, g3.t.def.GS<? extends java.lang.Throwable>.Member, g3.t.def.GS<? super java.lang.RuntimeException>.Member>.Member] EXACT_MATCH",
			resultCollector);
	}
	public void testElementPatternNestedParam07() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GM.java").getType("GM").getType("Generic");
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_wld [GM<?, ?, ?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GM<GM<?, ?, ?>.Generic<?,?,?>,GM<?, ?, ?>.Generic<?,?,?>,GM<?, ?, ?>.Generic<?,?,?>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GM<?, ?, ?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GM<?, ?, ?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GM<?, ?, ?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_obj [GM<Object, Exception, RuntimeException>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [GM<GS<?>.Generic<?>, GS<?>.Generic<?>, GS<?>.Generic<?>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GM<GS<?>.Generic<?>, GS<GS<?>.Generic<?>>.Generic<?>, GS<GS<GS<?>.Generic<?>>.Generic<?>>.Generic<?>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [GM<GS<Object>.Generic<?>, GS<? extends Throwable>.Generic<?>, GS<? super RuntimeException>.Generic<?>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_wld [g3.t.def.GM<?, ?, ?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [g3.t.def.GM<g3.t.def.GM<?, ?, ?>.Generic<?,?,?>,g3.t.def.GM<?, ?, ?>.Generic<?,?,?>,g3.t.def.GM<?, ?, ?>.Generic<?,?,?>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [g3.t.def.GM<?, ?, ?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [g3.t.def.GM<?, ?, ?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [g3.t.def.GM<?, ?, ?>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_obj [g3.t.def.GM<Object, Exception, RuntimeException>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [g3.t.def.GM<g3.t.def.GS<?>.Generic<?>, g3.t.def.GS<?>.Generic<?>, g3.t.def.GS<?>.Generic<?>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [g3.t.def.GM<g3.t.def.GS<?>.Generic<?>, g3.t.def.GS<g3.t.def.GS<?>.Generic<?>>.Generic<?>, g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>.Generic<?>>.Generic<?>>.Generic<?>>.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [g3.t.def.GM<g3.t.def.GS<Object>.Generic<?>, g3.t.def.GS<? extends Throwable>.Generic<?>, g3.t.def.GS<? super RuntimeException>.Generic<?>>.Generic] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testElementPatternNestedParam08() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GM.java").getType("NGM").getType("Generic");
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_wld [NGM.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGM.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGM.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGM.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGM.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_obj [NGM.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [NGM.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGM.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_obj [NGM.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_wld [g3.t.def.NGM.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [g3.t.def.NGM.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [g3.t.def.NGM.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [g3.t.def.NGM.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [g3.t.def.NGM.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_obj [g3.t.def.NGM.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [g3.t.def.NGM.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [g3.t.def.NGM.Generic] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [g3.t.def.NGM.Generic] EXACT_MATCH",
			resultCollector);
	}

	// Search reference to a generic type
	public void testStringPatternSimpleName01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.s.ref", false /* only this package */);
		search("Generic", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R1.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic] EXACT_MATCH",
			resultCollector);
	}
	public void testStringPatternSimpleName02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.m.ref", false /* only this package */);
		search("Generic", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type declared in a generic type
	public void testStringPatternSimpleName03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type declared in a generic type
	public void testStringPatternSimpleName04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type declared in a non-generic type
	public void testStringPatternSimpleName05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [GenericMember] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testStringPatternAnyStrings01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.s.ref", false /* only this package */);
		search("*Generic", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R1.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic] EXACT_MATCH",
			resultCollector);
	}
	public void testStringPatternAnyStrings02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.m.ref", false /* only this package */);
		search("*Generic", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [NonGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type declared in a generic type
	public void testStringPatternAnyStrings03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.s.ref", false /* only this package */);
		search("*Member*", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Member] EXACT_MATCH",
			resultCollector);
	}
	public void testStringPatternAnyStrings04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.m.ref", false /* only this package */);
		search("*Member*", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Member] EXACT_MATCH",
			resultCollector);
	}

	// Search reference to a generic type
	public void testSingleParameterizedStringPattern01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testSingleParameterizedStringPattern02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<Exception>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [g1.t.s.def.Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [g1.t.s.def.Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [g1.t.s.def.Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [g1.t.s.def.Generic<? super RuntimeException>.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testSingleParameterizedStringPattern03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [GenericMember<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [GenericMember<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [GenericMember<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [GenericMember<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	public void testSingleParameterizedStringPattern04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testSingleParameterizedStringPattern05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<Exception>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testSingleParameterizedStringPattern06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) Current limitation of SearchPattern
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testSingleParameterizedStringPattern07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<Exception>.MemberGeneric<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH",
			resultCollector);
	}

	// Search reference to a generic type
	public void testSingleWildcardExtendsStringPattern01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testSingleWildcardExtendsStringPattern02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [g1.t.s.def.Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [g1.t.s.def.Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [g1.t.s.def.Generic<? super RuntimeException>.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testSingleWildcardExtendsStringPattern03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [GenericMember<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [GenericMember<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	public void testSingleWildcardExtendsStringPattern04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testSingleWildcardExtendsStringPattern05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (sezrch-frederic) try to have a better match selection
	public void testSingleWildcardExtendsStringPattern06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) Current limitation of SearchPattern
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testSingleWildcardExtendsStringPattern07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception>.MemberGeneric<? extends Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>.MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [g1.t.s.def.Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [g1.t.s.def.Generic<?>.MemberGeneric<?>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testSingleWildcardSuperStringPattern01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testSingleWildcardSuperStringPattern02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [g1.t.s.def.Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [g1.t.s.def.Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [g1.t.s.def.Generic<? extends Throwable>.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testSingleWildcardSuperStringPattern03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [GenericMember<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [GenericMember<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	public void testSingleWildcardSuperStringPattern04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testSingleWildcardSuperStringPattern05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testSingleWildcardSuperStringPattern06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) Current limitation of SearchPattern
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testSingleWildcardSuperStringPattern07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception>.MemberGeneric<? super Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>.MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [g1.t.s.def.Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [g1.t.s.def.Generic<?>.MemberGeneric<?>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testSingleWildcardUnboundStringPattern01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testSingleWildcardUnboundStringPattern02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<?>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [g1.t.s.def.Generic<Object>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [g1.t.s.def.Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [g1.t.s.def.Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [g1.t.s.def.Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [g1.t.s.def.Generic<? super RuntimeException>.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testSingleWildcardUnboundStringPattern03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [GenericMember<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [GenericMember<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [GenericMember<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [GenericMember<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [GenericMember<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [GenericMember<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	public void testSingleWildcardUnboundStringPattern04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testSingleWildcardUnboundStringPattern05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<?>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testSingleWildcardUnboundStringPattern06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) Current limitation of SearchPattern
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testSingleWildcardUnboundStringPattern07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<?>.MemberGeneric<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH",
			resultCollector);
	}

	// Search reference to a generic type
	public void testMultipleParameterizedStringPattern01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testMultipleParameterizedStringPattern02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<Exception, Exception, RuntimeException>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [g1.t.m.def.Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testMultipleParameterizedStringPattern03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH",
			resultCollector);
	}
	public void testMultipleParameterizedStringPattern04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testMultipleParameterizedStringPattern05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<Exception, Exception, RuntimeException>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testMultipleParameterizedStringPattern06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) Current limitation of SearchPattern
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testMultipleParameterizedStringPattern07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testMultipleWildcardExtendsStringPattern01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testMultipleWildcardExtendsStringPattern02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception, ? extends Exception, ? extends RuntimeException>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [g1.t.m.def.Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testMultipleWildcardExtendsStringPattern03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH",
			resultCollector);
	}
	public void testMultipleWildcardExtendsStringPattern04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testMultipleWildcardExtendsStringPattern05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception, ? extends Exception, ? extends RuntimeException>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testMultipleWildcardExtendsStringPattern06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) Current limitation of SearchPattern
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testMultipleWildcardExtendsStringPattern07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? extends Exception, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? extends Exception, ? extends Exception, ? extends RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testMultipleWildcardSuperStringPattern01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception, ? super Exception, ? super RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testMultipleWildcardSuperStringPattern02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception, ? super Exception, ? super RuntimeException>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [g1.t.m.def.Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testMultipleWildcardSuperStringPattern03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember<? super Exception, ? super Exception, ? super RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH",
			resultCollector);
	}
	public void testMultipleWildcardSuperStringPattern04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric<? super Exception, ? super Exception, ? super RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testMultipleWildcardSuperStringPattern05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception, ? super Exception, ? super RuntimeException>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testMultipleWildcardSuperStringPattern06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<? super Exception, ? super Exception, ? super RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) Current limitation of SearchPattern
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testMultipleWildcardSuperStringPattern07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<? super Exception, ? super Exception, ? super RuntimeException>.MemberGeneric<? super Exception, ? super Exception, ? super RuntimeException>.", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic type
	public void testMultipleWildcardUnboundStringPattern01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<?, ?, ? >", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type
	public void testMultipleWildcardUnboundStringPattern02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<?, ?, ?>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [g1.t.m.def.Generic<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [g1.t.m.def.Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a generic member type
	public void testMultipleWildcardUnboundStringPattern03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember<?, ?, ?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [GenericMember<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [GenericMember<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH",
			resultCollector);
	}
	public void testMultipleWildcardUnboundStringPattern04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric<?, ?, ?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testMultipleWildcardUnboundStringPattern05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<?, ?, ?>.MemberGeneric", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testMultipleWildcardUnboundStringPattern06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric<?, ?, ?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) Current limitation of SearchPattern
	// We cannot find these kind of references as we compute 2 type arguments
	// although there's only one per class in the member type hierarchy...
	public void _testMultipleWildcardUnboundStringPattern07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<?, ?, ?>.MemberGeneric<?, ?, ?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH",
			resultCollector);
	}

	// Search reference with nested parameterized types
	public void testStringPatternNestedParam01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("GS<Exception>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_wld [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_wld [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_wld [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [GS<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [GS<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_wld [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_wld [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_wld [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [GS<? extends java.lang.Throwable>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [GS<? super java.lang.RuntimeException>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_obj [GS<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_obj [GS<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [GS<? extends java.lang.Throwable>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [GS<? super java.lang.RuntimeException>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [GS<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [GS<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [GS<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [GS<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [GS<? super RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	public void testStringPatternNestedParam02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("GS<? extends Exception>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_obj [GS<? super RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [g3.t.def.GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [g3.t.def.GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [g3.t.def.GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [g3.t.def.GS<? super java.lang.RuntimeException>.Member] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) Match count is OK but selection is not correct in this peculiar case...
	public void testStringPatternNestedParam03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("GS.Generic<? super RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [Generic<?,?,?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [Generic<?,?,?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [Generic<?>] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testStringPatternNestedParam04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("NGS.Generic<?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_wld [Generic<NGM.Generic<?, ?, ?>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [Generic<NGM.Generic<NGM.Generic<?, ?, ?>,NGM.Generic<?, ?, ?>,NGM.Generic<?, ?, ?>>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_obj [Generic<NGM.Generic<Object, Exception, RuntimeException>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [Generic<NGS.Generic<?>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [Generic<NGS.Generic<NGS.Generic<?>>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [Generic<NGS.Generic<?>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_obj [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_obj [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_wld [Generic<g3.t.def.NGM.Generic<?, ?, ?>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [Generic<g3.t.def.NGM.Generic<g3.t.def.NGM.Generic<?, ?, ?>, g3.t.def.NGM.Generic<?, ?, ?>, g3.t.def.NGM.Generic<?, ?, ?>>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_obj [Generic<g3.t.def.NGM.Generic<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [Generic<g3.t.def.NGS.Generic<?>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [Generic<g3.t.def.NGS.Generic<g3.t.def.NGS.Generic<?>>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [Generic<g3.t.def.NGS.Generic<?>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [Generic<java.lang.Object>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [Generic<? extends java.lang.Throwable>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [Generic<? super java.lang.RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	public void testStringPatternNestedParam05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("GM<Object, Exception, RuntimeException>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_wld [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_obj [GM<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_wld [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_obj [GM<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_wld [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_obj [GM<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_wld [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_obj [GM<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_wld [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_obj [GM<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_wld [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [GM<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_obj [GM<Object, Exception, RuntimeException>] EXACT_MATCH",
			resultCollector);
	}
	public void testStringPatternNestedParam06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("GM<java.lang.Object, ? extends java.lang.Exception, ? super java.lang.RuntimeException>.Member", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_wld [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_obj [GM<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_wld [g3.t.def.GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_obj [g3.t.def.GM<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>.Member] EXACT_MATCH",
			resultCollector);
	}
	public void testStringPatternNestedParam07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("GM.Member<?, ?, ?>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_wld [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<GM<?, ?, ?>.Member,GM<?, ?, ?>.Member,GM<?, ?, ?>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_obj [GM<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GM<GS<?>.Member, GS<?>.Member, GS<?>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GM<GS<?>.Member, GS<GS<?>.Member>.Member, GS<GS<GS<?>.Member>.Member>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_obj [GM<GS<Object>.Member, GS<? extends Throwable>.Member, GS<? super RuntimeException>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_wld [g3.t.def.GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<g3.t.def.GM<?, ?, ?>.Member, g3.t.def.GM<?, ?, ?>.Member, g3.t.def.GM<?, ?, ?>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_obj [g3.t.def.GM<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [g3.t.def.GM<g3.t.def.GS<?>.Member, g3.t.def.GS<?>.Member, g3.t.def.GS<?>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GM<g3.t.def.GS<?>.Member, g3.t.def.GS<g3.t.def.GS<?>.Member>.Member, g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>.Member>.Member>.Member>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [g3.t.def.GM<g3.t.def.GS<java.lang.Object>.Member, g3.t.def.GS<? extends java.lang.Throwable>.Member, g3.t.def.GS<? super java.lang.RuntimeException>.Member>.Member] EXACT_MATCH",
			resultCollector);
	}
	// TODO (search-frederic) try to have a better match selection
	public void testStringPatternNestedParam08() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("NGM.Generic<? extends java.lang.Object, ? extends java.lang.Object, ? extends java.lang.Object>", TYPE, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [Generic<NGM.Generic<?, ?, ?>,NGM.Generic<?, ?, ?>,NGM.Generic<?, ?, ?>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [Generic<NGS.Generic<?>, NGS.Generic<?>, NGS.Generic<?>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [Generic<NGS.Generic<?>, NGS.Generic<NGS.Generic<?>>, NGS.Generic<NGS.Generic<NGS.Generic<?>>>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_obj [Generic<NGS.Generic<Object>, NGS.Generic<? extends Throwable>, NGS.Generic<? super RuntimeException>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [Generic<g3.t.def.NGM.Generic<?, ?, ?>, g3.t.def.NGM.Generic<?, ?, ?>, g3.t.def.NGM.Generic<?, ?, ?>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_obj [Generic<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [Generic<g3.t.def.NGS.Generic<?>, g3.t.def.NGS.Generic<?>, g3.t.def.NGS.Generic<?>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [Generic<g3.t.def.NGS.Generic<?>, g3.t.def.NGS.Generic<g3.t.def.NGS.Generic<?>>, g3.t.def.NGS.Generic<g3.t.def.NGS.Generic<g3.t.def.NGS.Generic<?>>>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [Generic<g3.t.def.NGS.Generic<java.lang.Object>, g3.t.def.NGS.Generic<? extends java.lang.Throwable>, g3.t.def.NGS.Generic<? super java.lang.RuntimeException>>] EXACT_MATCH",
			resultCollector);
	}
}
