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

import org.eclipse.core.runtime.CoreException;


/**
 * Test generic type search.
 */
public class NewJavaSearchGenericTypeTests extends AbstractJavaSearchGenericTypeTests {
	
	public NewJavaSearchGenericTypeTests(String name) {
		super(name);
		this.tabs = 3;
		this.displayName = true;
	}
	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_PREFIX =  "testArray";
//		TESTS_NAMES = new String[] { "testElementPatternSingle", "Bug73336" };
//		TESTS_NUMBERS = new int[] { 8 };
//		TESTS_RANGE = new int[] { 6, -1 };
	}
	public static Test suite() {
		return buildTestSuite(NewJavaSearchGenericTypeTests.class);
	}

	/**
	 * Type reference for 1.5.
	 * Bug 73336: [1.5][search] Search Engine does not find type references of actual generic type parameters
	 * (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=73336)
	 */
	public void testTypeReferenceBug73336() throws CoreException {
		this.expectedResult =
			"src/bug73336/AA.java bug73336.AA [A] EXACT_MATCH\n" + 
			"src/bug73336/B.java bug73336.B [A] EXACT_MATCH\n" + 
			"src/bug73336/B.java bug73336.B [A] EXACT_MATCH\n" + 
			"src/bug73336/C.java bug73336.C [A] EXACT_MATCH\n" + 
			"src/bug73336/C.java void bug73336.C.foo() [A] EXACT_MATCH\n" + 
			"src/bug73336/C.java void bug73336.C.foo() [A] EXACT_MATCH";
		super.testTypeReferenceBug73336();
	}
	public void testTypeReferenceBug73336b() throws CoreException {
		this.expectedResult =
			"src/bug73336b/B.java bug73336b.B [A] EXACT_MATCH\n" + 
			"src/bug73336b/B.java bug73336b.B [A] EXACT_MATCH\n" + 
			"src/bug73336b/C.java bug73336b.C [A] EXACT_MATCH\n" + 
			"src/bug73336b/C.java bug73336b.C [A] EXACT_MATCH\n" + 
			"src/bug73336b/C.java bug73336b.C [A] EXACT_MATCH\n" + 
			"src/bug73336b/C.java bug73336b.C() [A] EXACT_MATCH\n" + 
			"src/bug73336b/C.java bug73336b.C() [A] EXACT_MATCH";
		super.testTypeReferenceBug73336b();
	}
	// Verify that no NPE was raised on following case
	public void testTypeReferenceBug73336c() throws CoreException {
		this.expectedResult =
				"src/bug73336c/B.java bug73336c.B [A] EXACT_MATCH\n" + 
				"src/bug73336c/B.java bug73336c.B [A] EXACT_MATCH\n" + 
				"src/bug73336c/C.java bug73336c.C [A] EXACT_MATCH\n" + 
				"src/bug73336c/C.java bug73336c.C [A] EXACT_MATCH\n" + 
				"src/bug73336c/C.java bug73336c.C [A] EXACT_MATCH";
		super.testTypeReferenceBug73336c();
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
	 * 		b) wildcard extends
	 * 		c) wildcard super
	 * 		d) wildcard unbound
	 */
	// Search reference to a generic type
	public void testElementPatternSingleParam01() throws CoreException {
		this.expectedResult =
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
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [g1.t.s.def.Generic] EXACT_MATCH";
		super.testElementPatternSingleParam01();
	}
	// Search reference to a member type declared in a generic type
	public void testElementPatternSingleParam02() throws CoreException {
		this.expectedResult =
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
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [g1.t.s.def.Generic<? super RuntimeException>.Member] EXACT_MATCH";
		super.testElementPatternSingleParam02();
	}
	// Search reference to a generic member type declared in a generic type
	public void testElementPatternSingleParam03() throws CoreException {
		this.expectedResult =
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
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [g1.t.s.def.Generic<? super RuntimeException>.MemberGeneric] EXACT_MATCH";
		super.testElementPatternSingleParam03();
	}
	// Search reference to a generic member type declared in a non-generic type
	public void testElementPatternSingleParam04() throws CoreException {
		this.expectedResult =
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
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH";
		super.testElementPatternSingleParam04();
	}

	public void testElementPatternMultipleParam01() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [g1.t.m.def.Generic] EXACT_MATCH";
		super.testElementPatternMultipleParam01();
	}

	public void testElementPatternMultipleParam02() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH";
		super.testElementPatternMultipleParam02();
	}

	public void testElementPatternMultipleParam03() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric] EXACT_MATCH";
		super.testElementPatternMultipleParam03();
	}

	public void testElementPatternMultipleParam04() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH";
		super.testElementPatternMultipleParam04();
	}

	public void testElementPatternSingleNestedParam01() throws CoreException {
		this.expectedResult = 
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
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [g3.t.def.GS] EXACT_MATCH";
		super.testElementPatternSingleNestedParam01();
	}

	public void testElementPatternSingleNestedParam02() throws CoreException {
		this.expectedResult = 
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
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [g3.t.def.GS<? super java.lang.RuntimeException>.Member] EXACT_MATCH";
		super.testElementPatternSingleNestedParam02();
	}

	public void testElementPatternSingleNestedParam03() throws CoreException {
		this.expectedResult = 
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
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [g3.t.def.GS<? super RuntimeException>.Generic] EXACT_MATCH";
		super.testElementPatternSingleNestedParam03();
	}

	public void testElementPatternSingleNestedParam04() throws CoreException {
		this.expectedResult = 
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
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [g3.t.def.NGS.Generic] EXACT_MATCH";
		super.testElementPatternSingleNestedParam04();
	}

	public void testElementPatternMultipleNestedParam01() throws CoreException {
		this.expectedResult = 
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
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [g3.t.def.GM] EXACT_MATCH";
		super.testElementPatternMultipleNestedParam01();
	}

	public void testElementPatternMultipleNestedParam02() throws CoreException {
		this.expectedResult = 
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
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [g3.t.def.GM<g3.t.def.GS<java.lang.Object>.Member, g3.t.def.GS<? extends java.lang.Throwable>.Member, g3.t.def.GS<? super java.lang.RuntimeException>.Member>.Member] EXACT_MATCH";
		super.testElementPatternMultipleNestedParam02();
	}

	public void testElementPatternMultipleNestedParam03() throws CoreException {
		this.expectedResult = 
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
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [g3.t.def.GM<g3.t.def.GS<Object>.Generic<?>, g3.t.def.GS<? extends Throwable>.Generic<?>, g3.t.def.GS<? super RuntimeException>.Generic<?>>.Generic] EXACT_MATCH";
		super.testElementPatternMultipleNestedParam03();
	}

	public void testElementPatternMultipleNestedParam04() throws CoreException {
		this.expectedResult = 
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
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [g3.t.def.NGM.Generic] EXACT_MATCH";
		super.testElementPatternMultipleNestedParam04();
	}

	public void testStringPatternSimpleName01() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic] EXACT_MATCH";
		super.testStringPatternSimpleName01();
	}

	public void testStringPatternSimpleName02() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic] EXACT_MATCH";
		super.testStringPatternSimpleName02();
	}

	public void testStringPatternSimpleName03() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Member] EXACT_MATCH";
		super.testStringPatternSimpleName03();
	}

	public void testStringPatternSimpleName04() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric] EXACT_MATCH";
		super.testStringPatternSimpleName04();
	}

	public void testStringPatternSimpleName05() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [GenericMember] EXACT_MATCH";
		super.testStringPatternSimpleName05();
	}

	public void testSinglePatternSimpleName06() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Generic<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic<Object>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic<? super RuntimeException>.Member] EXACT_MATCH";
		super.testSinglePatternSimpleName06();
	}

	public void testSinglePatternSimpleName07() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic<Object, Exception, RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [Generic<Object, Exception, RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic<?, ?, ?>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic<Object>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [Generic<Object>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [Generic<? extends Throwable>.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [Generic<? super RuntimeException>.MemberGeneric] EXACT_MATCH";
		super.testSinglePatternSimpleName07();
	}

	public void testSinglePatternSimpleName08() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [NonGeneric.GenericMember] EXACT_MATCH";
		super.testSinglePatternSimpleName08();
	}

	public void testStringPatternAnyStrings01() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic] EXACT_MATCH";
		super.testStringPatternAnyStrings01();
	}

	public void testStringPatternAnyStrings02() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic] EXACT_MATCH";
		super.testStringPatternAnyStrings02();
	}

	public void testStringPatternAnyStrings03() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Member] EXACT_MATCH";
		super.testStringPatternAnyStrings03();
	}

	public void testStringPatternAnyStrings04() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Member] EXACT_MATCH";
		super.testStringPatternAnyStrings04();
	}

	public void testStringPatternAnyStrings05() throws CoreException {
		this.expectedResult = 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [GenericMember<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [GenericMember<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [GenericMember<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [GenericMember<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH";
		super.testStringPatternAnyStrings05();
	}

	public void testStringPatternAnyStrings06() throws CoreException {
		this.expectedResult = 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic<? extends Throwable>] POTENTIAL_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [Generic<? extends Throwable>] POTENTIAL_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>] POTENTIAL_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [Generic<? extends Throwable>] POTENTIAL_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>] POTENTIAL_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic<? extends Throwable>] POTENTIAL_MATCH";
		super.testStringPatternAnyStrings06();
	}

	public void testSingleParameterizedElementPattern01() throws CoreException {
		this.expectedResult = 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [g1.t.s.def.Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [g1.t.s.def.Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [g1.t.s.def.Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [g1.t.s.def.Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [g1.t.s.def.Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [g1.t.s.def.Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [g1.t.s.def.Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [g1.t.s.def.Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [g1.t.s.def.Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [g1.t.s.def.Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [g1.t.s.def.Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [g1.t.s.def.Generic<? super RuntimeException>] EXACT_MATCH";
		super.testSingleParameterizedElementPattern01();
	}

	public void testSingleParameterizedElementPattern02() throws CoreException {
		this.expectedResult = 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [g1.t.s.def.Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [g1.t.s.def.Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [g1.t.s.def.Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [g1.t.s.def.Generic<? super RuntimeException>.Member] EXACT_MATCH";
		super.testSingleParameterizedElementPattern02();
	}

	public void testSingleParameterizedElementPattern03() throws CoreException {
		this.expectedResult = 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [NonGeneric.GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [NonGeneric.GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [NonGeneric.GenericMember<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [NonGeneric.GenericMember<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [g1.t.s.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [g1.t.s.def.NonGeneric.GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [g1.t.s.def.NonGeneric.GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [g1.t.s.def.NonGeneric.GenericMember<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [g1.t.s.def.NonGeneric.GenericMember<? super RuntimeException>] EXACT_MATCH";
		super.testSingleParameterizedElementPattern03();
	}

	public void testSingleParameterizedElementPattern04() throws CoreException {
		this.expectedResult = 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>.MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>.MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>.MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [g1.t.s.def.Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [g1.t.s.def.Generic<?>.MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [g1.t.s.def.Generic<? extends Throwable>.MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [g1.t.s.def.Generic<? super RuntimeException>.MemberGeneric<? extends Throwable>] EXACT_MATCH";
		super.testSingleParameterizedElementPattern04();
	}

	public void testMultipleParameterizedElementPattern01() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [g1.t.m.def.Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [g1.t.m.def.Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [g1.t.m.def.Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH";
		super.testMultipleParameterizedElementPattern01();
	}

	public void testMultipleParameterizedElementPattern02() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [g1.t.m.def.Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH";
		super.testMultipleParameterizedElementPattern02();
	}

	public void testMultipleParameterizedElementPattern03() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [NonGeneric.GenericMember<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [NonGeneric.GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [NonGeneric.GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [NonGeneric.GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [g1.t.m.def.NonGeneric.GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [g1.t.m.def.NonGeneric.GenericMember<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [g1.t.m.def.NonGeneric.GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [g1.t.m.def.NonGeneric.GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [g1.t.m.def.NonGeneric.GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH";
		super.testMultipleParameterizedElementPattern03();
	}

	public void testMultipleParameterizedElementPattern04() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>.MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [g1.t.m.def.Generic<?, ?, ?>.MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH";
		super.testMultipleParameterizedElementPattern04();
	}

	public void testSingleParameterizedStringPattern01() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH";
		super.testSingleParameterizedStringPattern01();
	}

	public void testSingleParameterizedStringPattern02() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic<? super RuntimeException>.Member] EXACT_MATCH";
		super.testSingleParameterizedStringPattern02();
	}

	public void testSingleParameterizedStringPattern03() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [GenericMember<? super RuntimeException>] EXACT_MATCH";
		super.testSingleParameterizedStringPattern03();
	}

	public void testSingleParameterizedStringPattern04() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH";
		super.testSingleParameterizedStringPattern04();
	}

	public void testSingleParameterizedStringPattern05() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>.MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>.MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>.MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>.MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [Generic<? extends Throwable>.MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [Generic<? super RuntimeException>.MemberGeneric<? extends Throwable>] EXACT_MATCH";
		super.testSingleParameterizedStringPattern05();
	}

	public void testSingleWildcardExtendsStringPattern01() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>] EXACT_MATCH";
		super.testSingleWildcardExtendsStringPattern01();
	}

	public void testSingleWildcardExtendsStringPattern02() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>.Member] EXACT_MATCH";
		super.testSingleWildcardExtendsStringPattern02();
	}

	public void testSingleWildcardExtendsStringPattern03() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember<?>] EXACT_MATCH";
		super.testSingleWildcardExtendsStringPattern03();
	}

	public void testSingleWildcardExtendsStringPattern04() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH";
		super.testSingleWildcardExtendsStringPattern04();
	}

	public void testSingleWildcardExtendsStringPattern05() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>.MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>.MemberGeneric<?>] EXACT_MATCH";
		super.testSingleWildcardExtendsStringPattern05();
	}

	public void testSingleWildcardSuperStringPattern01() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>] EXACT_MATCH";
		super.testSingleWildcardSuperStringPattern01();
	}

	public void testSingleWildcardSuperStringPattern02() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic<Object>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>.Member] EXACT_MATCH";
		super.testSingleWildcardSuperStringPattern02();
	}

	public void testSingleWildcardSuperStringPattern03() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [GenericMember<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [GenericMember<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [GenericMember<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember<?>] EXACT_MATCH";
		super.testSingleWildcardSuperStringPattern03();
	}

	public void testSingleWildcardSuperStringPattern04() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH";
		super.testSingleWildcardSuperStringPattern04();
	}

	public void testSingleWildcardSuperStringPattern05() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic<Object>.MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>.MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [Generic<Object>.MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>.MemberGeneric<?>] EXACT_MATCH";
		super.testSingleWildcardSuperStringPattern05();
	}

	public void testSingleWildcardUnboundStringPattern01() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic<? super RuntimeException>] EXACT_MATCH";
		super.testSingleWildcardUnboundStringPattern01();
	}

	public void testSingleWildcardUnboundStringPattern02() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic<Object>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic<? extends Throwable>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic<? super RuntimeException>.Member] EXACT_MATCH";
		super.testSingleWildcardUnboundStringPattern02();
	}

	public void testSingleWildcardUnboundStringPattern03() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [GenericMember<? super RuntimeException>] EXACT_MATCH";
		super.testSingleWildcardUnboundStringPattern03();
	}

	public void testSingleWildcardUnboundStringPattern04() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EXACT_MATCH";
		super.testSingleWildcardUnboundStringPattern04();
	}

	public void testSingleWildcardUnboundStringPattern05() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic<Object>.MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>.MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>.MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>.MemberGeneric<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [Generic<Object>.MemberGeneric<Object>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>.MemberGeneric<?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [Generic<? extends Throwable>.MemberGeneric<? super RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [Generic<? super RuntimeException>.MemberGeneric<? extends Throwable>] EXACT_MATCH";
		super.testSingleWildcardUnboundStringPattern05();
	}

	public void testMultipleParameterizedStringPattern01() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH";
		super.testMultipleParameterizedStringPattern01();
	}

	public void testMultipleParameterizedStringPattern02() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic.Member] EXACT_MATCH";
		super.testMultipleParameterizedStringPattern02();
	}

	public void testMultipleParameterizedStringPattern03() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH";
		super.testMultipleParameterizedStringPattern03();
	}

	public void testMultipleParameterizedStringPattern04() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH";
		super.testMultipleParameterizedStringPattern04();
	}

	public void testMultipleParameterizedStringPattern05() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>.MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic<?, ?, ?>.MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic.MemberGeneric] EXACT_MATCH";
		super.testMultipleParameterizedStringPattern05();
	}

	public void testMultipleWildcardExtendsStringPattern01() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH";
		super.testMultipleWildcardExtendsStringPattern01();
	}

	public void testMultipleWildcardExtendsStringPattern02() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic.Member] EXACT_MATCH";
		super.testMultipleWildcardExtendsStringPattern02();
	}

	public void testMultipleWildcardExtendsStringPattern03() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH";
		super.testMultipleWildcardExtendsStringPattern03();
	}

	public void testMultipleWildcardExtendsStringPattern04() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH";
		super.testMultipleWildcardExtendsStringPattern04();
	}

	public void testMultipleWildcardExtendsStringPattern05() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>.MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic<?, ?, ?>.MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic.MemberGeneric] EXACT_MATCH";
		super.testMultipleWildcardExtendsStringPattern05();
	}

	public void testMultipleWildcardSuperStringPattern01() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH";
		super.testMultipleWildcardSuperStringPattern01();
	}

	public void testMultipleWildcardSuperStringPattern02() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Generic<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic.Member] EXACT_MATCH";
		super.testMultipleWildcardSuperStringPattern02();
	}

	public void testMultipleWildcardSuperStringPattern03() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [GenericMember<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [GenericMember<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [GenericMember<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [GenericMember<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH";
		super.testMultipleWildcardSuperStringPattern03();
	}

	public void testMultipleWildcardSuperStringPattern04() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH";
		super.testMultipleWildcardSuperStringPattern04();
	}

	public void testMultipleWildcardSuperStringPattern05() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic<Object, Exception, RuntimeException>.MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>.MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [Generic<Object, Exception, RuntimeException>.MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic<?, ?, ?>.MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic.MemberGeneric] EXACT_MATCH";
		super.testMultipleWildcardSuperStringPattern05();
	}

	public void testMultipleWildcardUnboundStringPattern01() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_MATCH";
		super.testMultipleWildcardUnboundStringPattern01();
	}

	public void testMultipleWildcardUnboundStringPattern02() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Generic<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic<Exception, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic.Member] EXACT_MATCH";
		super.testMultipleWildcardUnboundStringPattern02();
	}

	public void testMultipleWildcardUnboundStringPattern03() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_MATCH";
		super.testMultipleWildcardUnboundStringPattern03();
	}

	public void testMultipleWildcardUnboundStringPattern04() throws CoreException {
		this.expectedResult = 
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
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_MATCH";
		super.testMultipleWildcardUnboundStringPattern04();
	}

	public void testMultipleWildcardUnboundStringPattern05() throws CoreException {
		this.expectedResult = 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic<Object, Exception, RuntimeException>.MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>.MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [Generic<Object, Exception, RuntimeException>.MemberGeneric<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic<?, ?, ?>.MemberGeneric<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic.MemberGeneric] EXACT_MATCH";
		super.testMultipleWildcardUnboundStringPattern05();
	}

	public void testStringPatternSingleNestedParam01() throws CoreException {
		this.expectedResult = 
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
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [GS<? super RuntimeException>] EXACT_MATCH";
		super.testStringPatternSingleNestedParam01();
	}

	public void testStringPatternSingleNestedParam02() throws CoreException {
		this.expectedResult = 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [GS<?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [GS<?>.Member] EXACT_MATCH";
		super.testStringPatternSingleNestedParam02();
	}

	public void testStringPatternSingleNestedParam03() throws CoreException {
		this.expectedResult = 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_wld [GS<GM<?, ?, ?>.Generic<?, ?, ?>>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GS<GM<GM<?, ?, ?>.Generic<?,?,?>,GM<?, ?, ?>.Generic<?,?,?>,GM<?, ?, ?>.Generic<?,?,?>>.Generic<?,?,?>>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_obj [GS<GM<Object, Exception, RuntimeException>.Generic<Object, Exception, RuntimeException>>.Generic<Exception>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [GS<?>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [GS<?>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [GS<?>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<?>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<GS<?>.Generic<?>>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<?>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<GS<GS<?>.Generic<?>>.Generic<?>>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<GS<?>.Generic<?>>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<?>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [GS<Object>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [GS<? extends Throwable>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [GS<? super RuntimeException>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_wld [GS<g3.t.def.GM<?, ?, ?>.Generic<?, ?, ?>>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [GS<g3.t.def.GM<g3.t.def.GM<?, ?, ?>.Generic<?,?,?>,g3.t.def.GM<?, ?, ?>.Generic<?,?,?>,g3.t.def.GM<?, ?, ?>.Generic<?,?,?>>.Generic<?,?,?>>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_obj [GS<g3.t.def.GM<Object, Exception, RuntimeException>.Generic<Object, Exception, RuntimeException>>.Generic<Exception>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [GS<?>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [GS<?>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [GS<?>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [GS<?>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [GS<g3.t.def.GS<?>.Generic<?>>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [GS<?>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [GS<g3.t.def.GS<g3.t.def.GS<?>.Generic<?>>.Generic<?>>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [GS<g3.t.def.GS<?>.Generic<?>>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [GS<?>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [GS<Object>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [GS<? extends Throwable>.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [GS<? super RuntimeException>.Generic<?>] EXACT_MATCH";
		super.testStringPatternSingleNestedParam03();
	}

	public void testStringPatternSingleNestedParam04() throws CoreException {
		this.expectedResult = 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [NGS.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [NGS.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [NGS.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_obj [NGS.Generic<? extends Throwable>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [NGS.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [NGS.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [NGS.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [NGS.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [NGS.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [NGS.Generic<?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [NGS.Generic<? extends java.lang.Throwable>] EXACT_MATCH";
		super.testStringPatternSingleNestedParam04();
	}

	public void testStringPatternMultipleNestedParam01() throws CoreException {
		this.expectedResult = 
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
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_obj [GM<Object, Exception, RuntimeException>] EXACT_MATCH";
		super.testStringPatternMultipleNestedParam01();
	}

	public void testStringPatternMultipleNestedParam02() throws CoreException {
		this.expectedResult = 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_wld [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_obj [GM<Object, Exception, RuntimeException>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_wld [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [GM<?, ?, ?>.Member] EXACT_MATCH\n" + 
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_obj [GM<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>.Member] EXACT_MATCH";
		super.testStringPatternMultipleNestedParam02();
	}

	public void testStringPatternMultipleNestedParam03() throws CoreException {
		this.expectedResult = 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_wld [GM<?, ?, ?>.Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GM<?, ?, ?>.Generic<?,?,?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GM<?, ?, ?>.Generic<?,?,?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GM<?, ?, ?>.Generic<?,?,?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_obj [GM<Object, Exception, RuntimeException>.Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_wld [GM<?, ?, ?>.Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [GM<?, ?, ?>.Generic<?,?,?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [GM<?, ?, ?>.Generic<?,?,?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [GM<?, ?, ?>.Generic<?,?,?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_obj [GM<Object, Exception, RuntimeException>.Generic<Object, Exception, RuntimeException>] EXACT_MATCH";
		super.testStringPatternMultipleNestedParam03();
	}

	public void testStringPatternMultipleNestedParam04() throws CoreException {
		this.expectedResult = 
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
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [g3.t.def.GM<g3.t.def.GS<java.lang.Object>.Member, g3.t.def.GS<? extends java.lang.Throwable>.Member, g3.t.def.GS<? super java.lang.RuntimeException>.Member>.Member] EXACT_MATCH";
		super.testStringPatternMultipleNestedParam04();
	}

	public void testStringPatternMultipleNestedParam05() throws CoreException {
		this.expectedResult = 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_wld [NGM.Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGM.Generic<NGM.Generic<?, ?, ?>,NGM.Generic<?, ?, ?>,NGM.Generic<?, ?, ?>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGM.Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGM.Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGM.Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_obj [NGM.Generic<Object, Exception, RuntimeException>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [NGM.Generic<NGS.Generic<?>, NGS.Generic<?>, NGS.Generic<?>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGM.Generic<NGS.Generic<?>, NGS.Generic<NGS.Generic<?>>, NGS.Generic<NGS.Generic<NGS.Generic<?>>>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_obj [NGM.Generic<NGS.Generic<Object>, NGS.Generic<? extends Throwable>, NGS.Generic<? super RuntimeException>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_wld [NGM.Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [NGM.Generic<g3.t.def.NGM.Generic<?, ?, ?>, g3.t.def.NGM.Generic<?, ?, ?>, g3.t.def.NGM.Generic<?, ?, ?>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [NGM.Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [NGM.Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [NGM.Generic<?, ?, ?>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_obj [NGM.Generic<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [NGM.Generic<g3.t.def.NGS.Generic<?>, g3.t.def.NGS.Generic<?>, g3.t.def.NGS.Generic<?>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [NGM.Generic<g3.t.def.NGS.Generic<?>, g3.t.def.NGS.Generic<g3.t.def.NGS.Generic<?>>, g3.t.def.NGS.Generic<g3.t.def.NGS.Generic<g3.t.def.NGS.Generic<?>>>>] EXACT_MATCH\n" + 
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [NGM.Generic<g3.t.def.NGS.Generic<java.lang.Object>, g3.t.def.NGS.Generic<? extends java.lang.Throwable>, g3.t.def.NGS.Generic<? super java.lang.RuntimeException>>] EXACT_MATCH";
		super.testStringPatternMultipleNestedParam05();
	}

	public void testArraySingle01() throws CoreException {
		this.expectedResult = 
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionList [g6.t.def.List] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArray [g6.t.def.List] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArrayList [g6.t.def.List] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArrayList [g6.t.def.List] EXACT_MATCH\n" + 
			"src/g6/t/ref/Single.java [g6.t.def.List] EXACT_MATCH\n" + 
			"src/g6/t/ref/Single.java g6.t.ref.Single.list [List] EXACT_MATCH\n" + 
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionList [List] EXACT_MATCH\n" + 
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArray [List] EXACT_MATCH\n" + 
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArrayList [List] EXACT_MATCH\n" + 
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArrayList [List] EXACT_MATCH";
		super.testArraySingle01();
	}

	public void testArraySingle02() throws CoreException {
		this.expectedResult = 
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionList [g6.t.def.List<Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArray [g6.t.def.List<Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArrayList [g6.t.def.List<Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/Single.java g6.t.ref.Single.list [List] EXACT_MATCH\n" + 
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionList [List<Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArray [List<Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArrayList [List<Exception>] EXACT_MATCH";
		super.testArraySingle02();
	}

	public void testArraySingle03() throws CoreException {
		this.expectedResult = 
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionList [g6.t.def.List<Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArray [g6.t.def.List<Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArrayList [g6.t.def.List<Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/Single.java g6.t.ref.Single.list [List] EXACT_MATCH\n" + 
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionList [List<Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArray [List<Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArrayList [List<Exception>] EXACT_MATCH";
		super.testArraySingle03();
	}

	public void testArraySingle04() throws CoreException {
		this.expectedResult = 
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArrayList [g6.t.def.List<g6.t.def.List<Exception>[]>] EXACT_MATCH\n" + 
			"src/g6/t/ref/Single.java g6.t.ref.Single.list [List] EXACT_MATCH\n" + 
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArrayList [List<List<Exception>[]>] EXACT_MATCH";
		super.testArraySingle04();
	}

	public void testArrayMultiple01() throws CoreException {
		this.expectedResult = 
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entry [Table.Entry] EXACT_MATCH\n" + 
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entryException [Table<String, Exception>.Entry] EXACT_MATCH\n" + 
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entryExceptionArray [Table<String, Exception>.Entry] EXACT_MATCH\n" + 
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Table<String, Exception>.Entry<String, Exception>[]>.Entry] EXACT_MATCH\n" + 
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Exception>.Entry] EXACT_MATCH\n" + 
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Exception>.Entry] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryException [g6.t.def.Table<String, Exception>.Entry] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryExceptionArray [g6.t.def.Table<String, Exception>.Entry] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]>.Entry] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, Exception>.Entry] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, Exception>.Entry] EXACT_MATCH";
		super.testArrayMultiple01();
	}

	public void testArrayMultiple02() throws CoreException {
		this.expectedResult = 
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entry [Table.Entry] EXACT_MATCH\n" + 
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entryException [Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entryExceptionArray [Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryException [g6.t.def.Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryExceptionArray [g6.t.def.Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH";
		super.testArrayMultiple02();
	}

	public void testArrayMultiple03() throws CoreException {
		this.expectedResult = 
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entry [Table.Entry] EXACT_MATCH\n" + 
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entryException [Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entryExceptionArray [Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryException [g6.t.def.Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryExceptionArray [g6.t.def.Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH";
		super.testArrayMultiple03();
	}

	public void testArrayMultiple04() throws CoreException {
		this.expectedResult = 
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entry [Table.Entry] EXACT_MATCH\n" + 
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Table<String, Exception>.Entry<String, Exception>[]>.Entry<String, Table<String, Exception>.Entry<String, Exception>[]>] EXACT_MATCH\n" + 
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]>.Entry<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]>] EXACT_MATCH";
		super.testArrayMultiple04();
	}
}
