/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.core.ResolvedSourceType;


/**
 * Test for generic types search using R_ERASURE_MATCH rule.
 */
public class JavaSearchGenericTypeTests extends AbstractJavaSearchGenericTests {

	public JavaSearchGenericTypeTests(String name) {
		super(name, ERASURE_RULE);
	}
	// defined for sub-classes
	JavaSearchGenericTypeTests(String name, int matchRule) {
		super(name, matchRule);
	}
	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
//		org.eclipse.jdt.internal.core.search.matching.MatchLocator.PRINT_BUFFER = false;
//		TESTS_PREFIX =  "testArray";
//		TESTS_NAMES = new String[] { "testParameterizedTypeMultipleArguments01" };
//		TESTS_NUMBERS = new int[] { 8 };
//		TESTS_RANGE = new int[] { 6, -1 };
	}
	public static Test suite() {
		return buildModelTestSuite(JavaSearchGenericTypeTests.class);
	}

	@Override
	protected void setUp () throws Exception {
		super.setUp();
		this.resultCollector.showAccuracy(true);
		this.resultCollector.showRule();
	}

	/*
	 * Remove last type arguments from a line of an expected result.
	 * This line contains one search match print out.
	 */
	int[] removeLastTypeArgument(char[] line) {
		int idx=line.length-1;
		while (line[idx] != ']') {
			idx--;
			if (idx == 0) return null;
		}
		if (line[--idx] != '>') return null;
		int n = 1;
		int end = idx+1;
		while(idx>=0 && n!= 0) {
			switch (line[--idx]) {
				case '<': n--; break;
				case '>': n++; break;
			}
		}
		if (n!= 0) {
			// something wrong here...
			return null;
		}
		int start = idx;
		while (idx>=0 && line[idx] != '[') idx--;
		if (idx > 0)
			return new int[] { start, end };
		// We should have opened a bracket!
		return null;
	}

	@Override
	void addResultLine(StringBuffer buffer, char[] line) {
		int[] positions = removeLastTypeArgument(line);
		if (buffer.length() > 0) buffer.append('\n');
		if (positions != null) {
			int stop = positions[0];
			int restart = positions[1];
			buffer.append(line, 0, stop);
			buffer.append(line, restart, line.length-restart);
		} else {
			buffer.append(line);
		}
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
	// Source type pattern on single type argument
	public void testTypeSingleArgument01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R1.java [g1.t.s.def.Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] ERASURE_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [g1.t.s.def.Generic] ERASURE_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [g1.t.s.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [g1.t.s.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [g1.t.s.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [g1.t.s.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [g1.t.s.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java [g1.t.s.def.Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] ERASURE_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic] ERASURE_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [g1.t.s.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [g1.t.s.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [g1.t.s.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [g1.t.s.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [g1.t.s.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java [g1.t.s.def.Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] ERASURE_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic] ERASURE_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [g1.t.s.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [g1.t.s.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [g1.t.s.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [g1.t.s.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [g1.t.s.def.Generic] ERASURE_MATCH",
			this.resultCollector);
	}
	public void testTypeSingleArgument02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getType("Member");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] ERASURE_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>.Member] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>.Member] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] ERASURE_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [g1.t.s.def.Generic<Object>.Member] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [g1.t.s.def.Generic<Exception>.Member] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [g1.t.s.def.Generic<?>.Member] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [g1.t.s.def.Generic<? extends Throwable>.Member] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [g1.t.s.def.Generic<? super RuntimeException>.Member] ERASURE_MATCH",
			this.resultCollector);
	}
	public void testTypeSingleArgument03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getType("MemberGeneric");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] ERASURE_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic<Object>.MemberGeneric] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>.MemberGeneric] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>.MemberGeneric] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>.MemberGeneric] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>.MemberGeneric] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] ERASURE_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [g1.t.s.def.Generic<Object>.MemberGeneric] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [g1.t.s.def.Generic<Exception>.MemberGeneric] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [g1.t.s.def.Generic<?>.MemberGeneric] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [g1.t.s.def.Generic<? extends Throwable>.MemberGeneric] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [g1.t.s.def.Generic<? super RuntimeException>.MemberGeneric] ERASURE_MATCH",
			this.resultCollector);
	}
	public void testTypeSingleArgument04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/s/def/NonGeneric.java").getType("NonGeneric").getType("GenericMember");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [NonGeneric.GenericMember] ERASURE_RAW_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [NonGeneric.GenericMember] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [NonGeneric.GenericMember] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [NonGeneric.GenericMember] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [NonGeneric.GenericMember] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [NonGeneric.GenericMember] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [g1.t.s.def.NonGeneric.GenericMember] ERASURE_RAW_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [g1.t.s.def.NonGeneric.GenericMember] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [g1.t.s.def.NonGeneric.GenericMember] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [g1.t.s.def.NonGeneric.GenericMember] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [g1.t.s.def.NonGeneric.GenericMember] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [g1.t.s.def.NonGeneric.GenericMember] ERASURE_MATCH",
			this.resultCollector);
	}

	// Source type pattern on multiple type arguments
	public void testTypeMultipleArguments01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java [g1.t.m.def.Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] ERASURE_RAW_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_obj [Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [g1.t.m.def.Generic] ERASURE_RAW_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_obj [g1.t.m.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [g1.t.m.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [g1.t.m.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [g1.t.m.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [g1.t.m.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java [g1.t.m.def.Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] ERASURE_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic] ERASURE_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [g1.t.m.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [g1.t.m.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [g1.t.m.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [g1.t.m.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [g1.t.m.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java [g1.t.m.def.Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] ERASURE_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic] ERASURE_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [g1.t.m.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [g1.t.m.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [g1.t.m.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [g1.t.m.def.Generic] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [g1.t.m.def.Generic] ERASURE_MATCH",
			this.resultCollector);
	}
	public void testTypeMultipleArguments02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getType("Member");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] ERASURE_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic<Object, Exception, RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] ERASURE_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [g1.t.m.def.Generic<Object, Exception, RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [g1.t.m.def.Generic<?, ?, ?>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] ERASURE_MATCH",
			this.resultCollector);
	}
	public void testTypeMultipleArguments03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getType("MemberGeneric");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] ERASURE_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic<Object, Exception, RuntimeException>.MemberGeneric] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>.MemberGeneric] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] ERASURE_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [g1.t.m.def.Generic<Object, Exception, RuntimeException>.MemberGeneric] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.MemberGeneric] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [g1.t.m.def.Generic<?, ?, ?>.MemberGeneric] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric] ERASURE_MATCH",
			this.resultCollector);
	}
	public void testTypeMultipleArguments04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g1/t/m/def/NonGeneric.java").getType("NonGeneric").getType("GenericMember");
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [NonGeneric.GenericMember] ERASURE_RAW_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [NonGeneric.GenericMember] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [NonGeneric.GenericMember] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [NonGeneric.GenericMember] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [NonGeneric.GenericMember] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [NonGeneric.GenericMember] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [g1.t.m.def.NonGeneric.GenericMember] ERASURE_RAW_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [g1.t.m.def.NonGeneric.GenericMember] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [g1.t.m.def.NonGeneric.GenericMember] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [g1.t.m.def.NonGeneric.GenericMember] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [g1.t.m.def.NonGeneric.GenericMember] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [g1.t.m.def.NonGeneric.GenericMember] ERASURE_MATCH",
			this.resultCollector);
	}

	// Source type pattern on nested single type argument
	public void testTypeNestedSingleArgument01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GS.java").getType("GS");
		ICompilationUnit ref = getCompilationUnit("JavaSearch15/src/g3/t/ref/R1.java");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new ICompilationUnit[] {ref});
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R1.java [g3.t.def.GS] EQUIVALENT_RAW_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_wld [GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_obj [GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_wld [GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_wld [GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_wld [GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_wld [g3.t.def.GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [g3.t.def.GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_obj [g3.t.def.GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_wld [g3.t.def.GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_wld [g3.t.def.GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_wld [g3.t.def.GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [g3.t.def.GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [g3.t.def.GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [g3.t.def.GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [g3.t.def.GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [g3.t.def.GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [g3.t.def.GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [g3.t.def.GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [g3.t.def.GS] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [g3.t.def.GS] ERASURE_MATCH",
			this.resultCollector);
	}
	public void testTypeNestedSingleArgument02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GS.java").getType("GS").getType("Member");
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_wld [GS<GM<?, ?, ?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GS<GM<GM<?, ?, ?>.Member,GM<?, ?, ?>.Member,GM<?, ?, ?>.Member>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_obj [GS<GM<Object, Exception, RuntimeException>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GS<?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GS<?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GS<?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<GS<?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<GS<GS<?>.Member>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<GS<?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_obj [GS<Object>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_obj [GS<? extends Throwable>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_obj [GS<? super RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_wld [g3.t.def.GS<g3.t.def.GM<?, ?, ?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GS<g3.t.def.GM<g3.t.def.GM<?, ?, ?>.Member, g3.t.def.GM<?, ?, ?>.Member, g3.t.def.GM<?, ?, ?>.Member>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_obj [g3.t.def.GS<g3.t.def.GM<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [g3.t.def.GS<?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [g3.t.def.GS<?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [g3.t.def.GS<?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GS<?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GS<g3.t.def.GS<?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GS<?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>.Member>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GS<g3.t.def.GS<?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GS<?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [g3.t.def.GS<java.lang.Object>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [g3.t.def.GS<? extends java.lang.Throwable>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [g3.t.def.GS<? super java.lang.RuntimeException>.Member] ERASURE_MATCH",
			this.resultCollector);
	}
	public void testTypeNestedSingleArgument03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GS.java").getType("GS").getType("Generic");
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_wld [GS<GM<?, ?, ?>.Generic<?, ?, ?>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GS<GM<GM<?, ?, ?>.Generic<?,?,?>,GM<?, ?, ?>.Generic<?,?,?>,GM<?, ?, ?>.Generic<?,?,?>>.Generic<?,?,?>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_obj [GS<GM<Object, Exception, RuntimeException>.Generic<Object, Exception, RuntimeException>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [GS<?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [GS<?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [GS<?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<GS<?>.Generic<?>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<GS<GS<?>.Generic<?>>.Generic<?>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<GS<?>.Generic<?>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [GS<Object>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [GS<? extends Throwable>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [GS<? super RuntimeException>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_wld [g3.t.def.GS<g3.t.def.GM<?, ?, ?>.Generic<?, ?, ?>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [g3.t.def.GS<g3.t.def.GM<g3.t.def.GM<?, ?, ?>.Generic<?,?,?>,g3.t.def.GM<?, ?, ?>.Generic<?,?,?>,g3.t.def.GM<?, ?, ?>.Generic<?,?,?>>.Generic<?,?,?>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_obj [g3.t.def.GS<g3.t.def.GM<Object, Exception, RuntimeException>.Generic<Object, Exception, RuntimeException>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [g3.t.def.GS<?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [g3.t.def.GS<?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [g3.t.def.GS<?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [g3.t.def.GS<?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [g3.t.def.GS<g3.t.def.GS<?>.Generic<?>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [g3.t.def.GS<?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>.Generic<?>>.Generic<?>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [g3.t.def.GS<g3.t.def.GS<?>.Generic<?>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [g3.t.def.GS<?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [g3.t.def.GS<Object>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [g3.t.def.GS<? extends Throwable>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [g3.t.def.GS<? super RuntimeException>.Generic] ERASURE_MATCH",
			this.resultCollector);
	}
	public void testTypeNestedSingleArgument04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/NGS.java").getType("NGS").getType("Generic");
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_wld [NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_obj [NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_obj [NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_obj [NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_obj [NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_wld [g3.t.def.NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [g3.t.def.NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_obj [g3.t.def.NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [g3.t.def.NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [g3.t.def.NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [g3.t.def.NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [g3.t.def.NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [g3.t.def.NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [g3.t.def.NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [g3.t.def.NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [g3.t.def.NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [g3.t.def.NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [g3.t.def.NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [g3.t.def.NGS.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [g3.t.def.NGS.Generic] ERASURE_MATCH",
			this.resultCollector);
	}

	// Source type pattern on nested multiple type arguments
	public void testTypeNestedMultipleArguments01() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GM.java").getType("GM");
		ICompilationUnit ref = getCompilationUnit("JavaSearch15/src/g3/t/ref/R1.java");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new ICompilationUnit[] {ref});
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R1.java [g3.t.def.GM] EQUIVALENT_RAW_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_wld [GM] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [GM] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [GM] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [GM] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [GM] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_obj [GM] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_wld [GM] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GM] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [GM] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_wld [g3.t.def.GM] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [g3.t.def.GM] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [g3.t.def.GM] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [g3.t.def.GM] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [g3.t.def.GM] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_obj [g3.t.def.GM] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_wld [g3.t.def.GM] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [g3.t.def.GM] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [g3.t.def.GM] ERASURE_MATCH",
			this.resultCollector);
	}
	public void testTypeNestedMultipleArguments02() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GM.java").getType("GM").getType("Member");
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_wld [GM<?, ?, ?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<GM<?, ?, ?>.Member,GM<?, ?, ?>.Member,GM<?, ?, ?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_obj [GM<Object, Exception, RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GM<GS<?>.Member, GS<?>.Member, GS<?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GM<GS<?>.Member, GS<GS<?>.Member>.Member, GS<GS<GS<?>.Member>.Member>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_obj [GM<GS<Object>.Member, GS<? extends Throwable>.Member, GS<? super RuntimeException>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_wld [g3.t.def.GM<?, ?, ?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<g3.t.def.GM<?, ?, ?>.Member, g3.t.def.GM<?, ?, ?>.Member, g3.t.def.GM<?, ?, ?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<?, ?, ?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<?, ?, ?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<?, ?, ?>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_obj [g3.t.def.GM<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [g3.t.def.GM<g3.t.def.GS<?>.Member, g3.t.def.GS<?>.Member, g3.t.def.GS<?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GM<g3.t.def.GS<?>.Member, g3.t.def.GS<g3.t.def.GS<?>.Member>.Member, g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>.Member>.Member>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [g3.t.def.GM<g3.t.def.GS<java.lang.Object>.Member, g3.t.def.GS<? extends java.lang.Throwable>.Member, g3.t.def.GS<? super java.lang.RuntimeException>.Member>.Member] ERASURE_MATCH",
			this.resultCollector);
	}
	public void testTypeNestedMultipleArguments03() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/GM.java").getType("GM").getType("Generic");
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_wld [GM<?, ?, ?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GM<GM<?, ?, ?>.Generic<?,?,?>,GM<?, ?, ?>.Generic<?,?,?>,GM<?, ?, ?>.Generic<?,?,?>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GM<?, ?, ?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GM<?, ?, ?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GM<?, ?, ?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_obj [GM<Object, Exception, RuntimeException>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [GM<GS<?>.Generic<?>, GS<?>.Generic<?>, GS<?>.Generic<?>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GM<GS<?>.Generic<?>, GS<GS<?>.Generic<?>>.Generic<?>, GS<GS<GS<?>.Generic<?>>.Generic<?>>.Generic<?>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [GM<GS<Object>.Generic<?>, GS<? extends Throwable>.Generic<?>, GS<? super RuntimeException>.Generic<?>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_wld [g3.t.def.GM<?, ?, ?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [g3.t.def.GM<g3.t.def.GM<?, ?, ?>.Generic<?,?,?>,g3.t.def.GM<?, ?, ?>.Generic<?,?,?>,g3.t.def.GM<?, ?, ?>.Generic<?,?,?>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [g3.t.def.GM<?, ?, ?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [g3.t.def.GM<?, ?, ?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [g3.t.def.GM<?, ?, ?>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_obj [g3.t.def.GM<Object, Exception, RuntimeException>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [g3.t.def.GM<g3.t.def.GS<?>.Generic<?>, g3.t.def.GS<?>.Generic<?>, g3.t.def.GS<?>.Generic<?>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [g3.t.def.GM<g3.t.def.GS<?>.Generic<?>, g3.t.def.GS<g3.t.def.GS<?>.Generic<?>>.Generic<?>, g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>.Generic<?>>.Generic<?>>.Generic<?>>.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [g3.t.def.GM<g3.t.def.GS<Object>.Generic<?>, g3.t.def.GS<? extends Throwable>.Generic<?>, g3.t.def.GS<? super RuntimeException>.Generic<?>>.Generic] ERASURE_MATCH",
			this.resultCollector);
	}
	public void testTypeNestedMultipleArguments04() throws CoreException {
		IType type = getCompilationUnit("JavaSearch15/src/g3/t/def/NGM.java").getType("NGM").getType("Generic");
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_wld [NGM.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGM.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGM.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGM.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGM.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_obj [NGM.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [NGM.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGM.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_obj [NGM.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_wld [g3.t.def.NGM.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [g3.t.def.NGM.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [g3.t.def.NGM.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [g3.t.def.NGM.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [g3.t.def.NGM.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_obj [g3.t.def.NGM.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [g3.t.def.NGM.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [g3.t.def.NGM.Generic] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [g3.t.def.NGM.Generic] ERASURE_MATCH",
			this.resultCollector);
	}

	// String pattern with no type argument
	public void testStringNoArgument01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.s.ref", false /* only this package */);
		search("Generic", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R1.java [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testStringNoArgument02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.m.ref", false /* only this package */);
		search("Generic", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testStringNoArgument03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Member", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Member] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Member] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Member] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Member] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Member] EXACT_MATCH",
			this.resultCollector);
	}
	public void testStringNoArgument04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testStringNoArgument05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [GenericMember] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testStringNoArgument06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic.Member", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic<Object, Exception, RuntimeException>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic.Member] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Generic<Object, Exception, RuntimeException>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic<Exception, Exception, RuntimeException>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic<?, ?, ?>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic.Member] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic<Object>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic<? extends Throwable>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic<? super RuntimeException>.Member] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testStringNoArgument07() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic.MemberGeneric", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic<Object, Exception, RuntimeException>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic.MemberGeneric] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [Generic<Object, Exception, RuntimeException>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic<?, ?, ?>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic<Object>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic.MemberGeneric] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [Generic<Object>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [Generic<? extends Throwable>.MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [Generic<? super RuntimeException>.MemberGeneric] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testStringNoArgument08() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("NonGeneric.GenericMember", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [NonGeneric.GenericMember] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [NonGeneric.GenericMember] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [NonGeneric.GenericMember] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [NonGeneric.GenericMember] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [NonGeneric.GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [NonGeneric.GenericMember] EQUIVALENT_MATCH",
			this.resultCollector);
	}

	// Parameterized Source type pattern on single type argument
	public void testParameterizedTypeSingleArgument01() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java");
		ResolvedSourceType type = selectParameterizedType(unit, "Generic<Exception>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R1.java [g1.t.s.def.Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [Generic<Object>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic<Exception>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [g1.t.s.def.Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [g1.t.s.def.Generic<Object>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [g1.t.s.def.Generic<Exception>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [g1.t.s.def.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [g1.t.s.def.Generic<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [g1.t.s.def.Generic<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java [g1.t.s.def.Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic<Object>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [g1.t.s.def.Generic<Object>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [g1.t.s.def.Generic<Exception>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [g1.t.s.def.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [g1.t.s.def.Generic<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [g1.t.s.def.Generic<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java [g1.t.s.def.Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [g1.t.s.def.Generic<Object>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [g1.t.s.def.Generic<Exception>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [g1.t.s.def.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [g1.t.s.def.Generic<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [g1.t.s.def.Generic<? super RuntimeException>] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testParameterizedTypeSingleArgument02() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java");
		ResolvedSourceType type = selectParameterizedType(unit, "g1.t.s.def.Generic<Exception>.Member"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>.Member] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [g1.t.s.def.Generic.Member] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [g1.t.s.def.Generic<Object>.Member] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [g1.t.s.def.Generic<Exception>.Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [g1.t.s.def.Generic<?>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [g1.t.s.def.Generic<? extends Throwable>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [g1.t.s.def.Generic<? super RuntimeException>.Member] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testParameterizedTypeSingleArgument03() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java");
		ResolvedSourceType type = selectParameterizedType(unit, "NonGeneric.GenericMember<Exception>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [NonGeneric.GenericMember] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [NonGeneric.GenericMember<Object>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [NonGeneric.GenericMember<Exception>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [NonGeneric.GenericMember<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [NonGeneric.GenericMember<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [NonGeneric.GenericMember<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [g1.t.s.def.NonGeneric.GenericMember] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [g1.t.s.def.NonGeneric.GenericMember<Object>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [g1.t.s.def.NonGeneric.GenericMember<Exception>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [g1.t.s.def.NonGeneric.GenericMember<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [g1.t.s.def.NonGeneric.GenericMember<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [g1.t.s.def.NonGeneric.GenericMember<? super RuntimeException>] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testParameterizedTypeSingleArgument04() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java");
		ResolvedSourceType type = selectParameterizedType(unit,  "g1.t.s.def.Generic<Exception>.MemberGeneric<Exception>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic<Object>.MemberGeneric<Object>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>.MemberGeneric<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>.MemberGeneric<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>.MemberGeneric<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [g1.t.s.def.Generic.MemberGeneric] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [g1.t.s.def.Generic<Object>.MemberGeneric<Object>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [g1.t.s.def.Generic<Exception>.MemberGeneric<Exception>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [g1.t.s.def.Generic<?>.MemberGeneric<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [g1.t.s.def.Generic<? extends Throwable>.MemberGeneric<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [g1.t.s.def.Generic<? super RuntimeException>.MemberGeneric<? extends Throwable>] EQUIVALENT_MATCH",
			this.resultCollector);
	}

	// Parameterized Source type pattern on multiple type arguments
	public void testParameterizedTypeMultipleArguments01() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java");
		ResolvedSourceType type = selectParameterizedType(unit, "g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java [g1.t.m.def.Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_obj [Generic<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic<Exception, Exception, RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [g1.t.m.def.Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_obj [g1.t.m.def.Generic<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [g1.t.m.def.Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java [g1.t.m.def.Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [g1.t.m.def.Generic<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [g1.t.m.def.Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java [g1.t.m.def.Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [g1.t.m.def.Generic<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [g1.t.m.def.Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH",
			this.resultCollector);
	}
	public void testParameterizedTypeMultipleArguments02() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java");
		ResolvedSourceType type = selectParameterizedType(unit, "Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic<Object, Exception, RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [g1.t.m.def.Generic.Member] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [g1.t.m.def.Generic<Object, Exception, RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [g1.t.m.def.Generic<?, ?, ?>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] ERASURE_MATCH",
			this.resultCollector);
	}
	public void testParameterizedTypeMultipleArguments03() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java");
		ResolvedSourceType type = selectParameterizedType(unit, "NonGeneric.GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [NonGeneric.GenericMember] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [NonGeneric.GenericMember<Object, Exception, RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [NonGeneric.GenericMember<Exception, Exception, RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [NonGeneric.GenericMember<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [NonGeneric.GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [NonGeneric.GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [g1.t.m.def.NonGeneric.GenericMember] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [g1.t.m.def.NonGeneric.GenericMember<Object, Exception, RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [g1.t.m.def.NonGeneric.GenericMember<Exception, Exception, RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [g1.t.m.def.NonGeneric.GenericMember<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [g1.t.m.def.NonGeneric.GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [g1.t.m.def.NonGeneric.GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] EXACT_MATCH",
			this.resultCollector);
	}
	public void testParameterizedTypeMultipleArguments04() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java");
		ResolvedSourceType type = selectParameterizedType(unit,  "g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic<Object, Exception, RuntimeException>.MemberGeneric<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>.MemberGeneric<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [g1.t.m.def.Generic.MemberGeneric] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [g1.t.m.def.Generic<Object, Exception, RuntimeException>.MemberGeneric<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [g1.t.m.def.Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [g1.t.m.def.Generic<?, ?, ?>.MemberGeneric<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [g1.t.m.def.Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [g1.t.m.def.Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] EXACT_MATCH",
			this.resultCollector);
	}

	// String pattern with Single type argument
	public void testStringParameterizedSingleArgument01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<?>", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [Generic<Object>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic<Exception>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic<?>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [Generic<Object>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic<Exception>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic<?>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [Generic<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [Generic<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic<Object>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [Generic<Object>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [Generic<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [Generic<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic<Object>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic<? super RuntimeException>] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testStringParameterizedSingleArgument02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<?>.Member", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic.Member] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>.Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic.Member] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic<Object>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>.Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic<? extends Throwable>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic<? super RuntimeException>.Member] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testStringParameterizedSingleArgument03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember<?>", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [GenericMember<Object>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [GenericMember<Exception>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember<?>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [GenericMember<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [GenericMember<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [GenericMember<Object>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [GenericMember<Exception>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember<?>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [GenericMember<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [GenericMember<? super RuntimeException>] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testStringParameterizedSingleArgument04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric<?>", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric<Object>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric<Exception>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric<?>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric<Object>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric<Exception>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric<?>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric<? extends Throwable>] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testStringParameterizedSingleArgument05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<?>.MemberGeneric<?>", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic.MemberGeneric] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic<Object>.MemberGeneric<Object>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>.MemberGeneric<Exception>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>.MemberGeneric<?>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>.MemberGeneric<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>.MemberGeneric<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic.MemberGeneric] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [Generic<Object>.MemberGeneric<Object>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>.MemberGeneric<Exception>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>.MemberGeneric<?>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [Generic<? extends Throwable>.MemberGeneric<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [Generic<? super RuntimeException>.MemberGeneric<? extends Throwable>] EQUIVALENT_MATCH",
			this.resultCollector);
	}

	// Multiple type arguments in string pattern
	public void testStringParameterizedMultipleArguments01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<Unresolved1, Unresolved2, Unresolved3>", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_obj [Generic<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic<Exception, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_obj [Generic<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [Generic<Exception, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [Generic<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic<Exception, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Generic<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic<Exception, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EQUIVALENT_RAW_MATCH",
			this.resultCollector);
	}
	public void testStringParameterizedMultipleArguments02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<Unresolved1, Unresolved2, Unresolved3>.Member", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic.Member] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic<Object, Exception, RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic<Exception, Exception, RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic<?, ?, ?>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic.Member] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Generic<Object, Exception, RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic<Exception, Exception, RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic<?, ?, ?>.Member] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.Member] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic.Member] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic.Member] EQUIVALENT_RAW_MATCH",
			this.resultCollector);
	}
	public void testStringParameterizedMultipleArguments03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("GenericMember<Unresolved1, Unresolved2, Unresolved3>", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [GenericMember] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [GenericMember<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [GenericMember<Exception, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [GenericMember<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen [GenericMember] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [GenericMember<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [GenericMember<Exception, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [GenericMember<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [GenericMember<? extends Throwable, ? extends Exception, ? extends RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [GenericMember<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EQUIVALENT_RAW_MATCH",
			this.resultCollector);
	}
	public void testStringParameterizedMultipleArguments04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("MemberGeneric<Unresolved1, Unresolved2, Unresolved3>", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [MemberGeneric<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric<Exception, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [MemberGeneric<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric<Exception, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EQUIVALENT_RAW_MATCH",
			this.resultCollector);
	}
	public void testStringParameterizedMultipleArguments05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t", true /* add all subpackages */);
		search("Generic<Unresolved1, Unresolved2, Unresolved3>.MemberGeneric<Unresolved1, Unresolved2, Unresolved3>", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic.MemberGeneric] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic<Object, Exception, RuntimeException>.MemberGeneric<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic<?, ?, ?>.MemberGeneric<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic.MemberGeneric] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [Generic<Object, Exception, RuntimeException>.MemberGeneric<Object, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic<Exception, Exception, RuntimeException>.MemberGeneric<Exception, Exception, RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic<?, ?, ?>.MemberGeneric<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [Generic<? extends Throwable, ? extends Exception, ? extends RuntimeException>.MemberGeneric<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>] ERASURE_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [Generic<? super RuntimeException, ? super IllegalMonitorStateException, ? super IllegalMonitorStateException>.MemberGeneric<? extends Throwable, ? extends Exception, ? extends RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic.MemberGeneric] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic.MemberGeneric] EQUIVALENT_RAW_MATCH",
			this.resultCollector);
	}

	// String pattern with 	any strings characters ('*' or '?')
	public void testStringAnyStrings01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.s.ref", false /* only this package */);
		search("*Generic", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R1.java [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java [NonGeneric] EXACT_RAW_MATCH\n" +
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
			"src/g1/t/s/ref/R3.java [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testStringAnyStrings02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.m.ref", false /* only this package */);
		search("G?ner?c", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Generic] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Generic] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Generic] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testStringAnyStrings03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.s.ref", false /* only this package */);
		search("*Member*", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [GenericMember] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [MemberGeneric] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [MemberGeneric] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Member] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Member] EXACT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Member] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Member] EXACT_MATCH",
			this.resultCollector);
	}
	public void testStringAnyStrings04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.m.ref", false /* only this package */);
		search("Member*", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [MemberGeneric] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen [MemberGeneric] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [MemberGeneric] EQUIVALENT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [Member] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen [Member] EXACT_RAW_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [Member] EXACT_MATCH\n" +
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [Member] EXACT_MATCH",
			this.resultCollector);
	}
	public void testStringAnyStrings05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.s.ref", true);
		search("Generic*<Object>", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R1.java [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [Generic<Object>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic<Exception>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic<? extends Throwable>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [Generic<Object>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic<Exception>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [Generic<? extends Throwable>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [Generic<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [GenericMember] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [GenericMember<Object>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [GenericMember<Exception>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [GenericMember<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [GenericMember<? extends Throwable>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [GenericMember<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen [GenericMember] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [GenericMember<Object>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [GenericMember<Exception>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [GenericMember<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [GenericMember<? extends Throwable>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [GenericMember<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic<Object>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [Generic<Object>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [Generic<? extends Throwable>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [Generic<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic<Object>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic<? extends Throwable>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic<? super RuntimeException>] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testStringAnyStrings06() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g1.t.s.ref", true);
		search("Generic<Obj*>", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g1/t/s/ref/R1.java [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [Generic<Object>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [Generic<Exception>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [Generic<? extends Throwable>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [Generic<? super RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [Generic<Object>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [Generic<Exception>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [Generic<? extends Throwable>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [Generic<? super RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [Generic<Object>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [Generic<Exception>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [Generic<? extends Throwable>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [Generic<? super RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [Generic<Object>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [Generic<Exception>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [Generic<? extends Throwable>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [Generic<? super RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [Generic<Object>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [Generic<Exception>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [Generic<? extends Throwable>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [Generic<? super RuntimeException>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen [Generic] EQUIVALENT_RAW_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [Generic<Object>] EXACT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [Generic<Exception>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [Generic<? extends Throwable>] ERASURE_MATCH\n" +
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [Generic<? super RuntimeException>] ERASURE_MATCH",
			this.resultCollector);
	}

	// String pattern with nested single type argument
	public void testStringNestedSingleArgument01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchCUScope("JavaSearch15", "g3/t/ref", "R1.java");
		search("GS<Exception>", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R1.java [GS] EQUIVALENT_RAW_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_wld [GS<GM<?, ?, ?>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [GS<GM<GM<?, ?, ?>,GM<?, ?, ?>,GM<?, ?, ?>>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_obj [GS<GM<Object, Exception, RuntimeException>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_wld [GS<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_wld [GS<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_wld [GS<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS<GS<?>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS<GS<GS<?>>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS<GS<?>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GS<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [GS<Object>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [GS<? extends Throwable>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [GS<? super RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_wld [GS<g3.t.def.GM<?, ?, ?>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [GS<g3.t.def.GM<g3.t.def.GM<?, ?, ?>, g3.t.def.GM<?, ?, ?>, g3.t.def.GM<?, ?, ?>>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_obj [GS<g3.t.def.GM<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_wld [GS<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_wld [GS<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_wld [GS<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [GS<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [GS<g3.t.def.GS<?>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [GS<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [GS<g3.t.def.GS<g3.t.def.GS<?>>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [GS<g3.t.def.GS<?>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [GS<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [GS<java.lang.Object>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [GS<? extends java.lang.Throwable>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [GS<? super java.lang.RuntimeException>] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testStringNestedSingleArgument02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("GS<? extends Exception>.Member", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_wld [GS<GM<?, ?, ?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GS<GM<GM<?, ?, ?>.Member,GM<?, ?, ?>.Member,GM<?, ?, ?>.Member>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_obj [GS<GM<Object, Exception, RuntimeException>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GS<?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GS<?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GS<?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<GS<?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<GS<GS<?>.Member>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<GS<?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GS<?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_obj [GS<Object>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_obj [GS<? extends Throwable>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_obj [GS<? super RuntimeException>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_wld [GS<g3.t.def.GM<?, ?, ?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [GS<g3.t.def.GM<g3.t.def.GM<?, ?, ?>.Member, g3.t.def.GM<?, ?, ?>.Member, g3.t.def.GM<?, ?, ?>.Member>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_obj [GS<g3.t.def.GM<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [GS<?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [GS<?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [GS<?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [GS<?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [GS<g3.t.def.GS<?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [GS<?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [GS<g3.t.def.GS<g3.t.def.GS<?>.Member>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [GS<g3.t.def.GS<?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [GS<?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [GS<java.lang.Object>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [GS<? extends java.lang.Throwable>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [GS<? super java.lang.RuntimeException>.Member] ERASURE_MATCH",
			this.resultCollector);
	}
	public void testStringNestedSingleArgument03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("GS<?>.Generic<? super RuntimeException>", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_wld [GS<GM<?, ?, ?>.Generic<?, ?, ?>>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GS<GM<GM<?, ?, ?>.Generic<?,?,?>,GM<?, ?, ?>.Generic<?,?,?>,GM<?, ?, ?>.Generic<?,?,?>>.Generic<?,?,?>>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_obj [GS<GM<Object, Exception, RuntimeException>.Generic<Object, Exception, RuntimeException>>.Generic<Exception>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [GS<?>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [GS<?>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [GS<?>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<?>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<GS<?>.Generic<?>>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<?>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<GS<GS<?>.Generic<?>>.Generic<?>>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<GS<?>.Generic<?>>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GS<?>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [GS<Object>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [GS<? extends Throwable>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [GS<? super RuntimeException>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_wld [GS<g3.t.def.GM<?, ?, ?>.Generic<?, ?, ?>>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [GS<g3.t.def.GM<g3.t.def.GM<?, ?, ?>.Generic<?,?,?>,g3.t.def.GM<?, ?, ?>.Generic<?,?,?>,g3.t.def.GM<?, ?, ?>.Generic<?,?,?>>.Generic<?,?,?>>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_obj [GS<g3.t.def.GM<Object, Exception, RuntimeException>.Generic<Object, Exception, RuntimeException>>.Generic<Exception>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [GS<?>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [GS<?>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [GS<?>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [GS<?>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [GS<g3.t.def.GS<?>.Generic<?>>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [GS<?>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [GS<g3.t.def.GS<g3.t.def.GS<?>.Generic<?>>.Generic<?>>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [GS<g3.t.def.GS<?>.Generic<?>>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [GS<?>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [GS<Object>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [GS<? extends Throwable>.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [GS<? super RuntimeException>.Generic<?>] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testStringNestedSingleArgument04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("NGS.Generic<? extends Throwable>", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_wld [NGS.Generic<NGM.Generic<?, ?, ?>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGS.Generic<NGM.Generic<NGM.Generic<?, ?, ?>,NGM.Generic<?, ?, ?>,NGM.Generic<?, ?, ?>>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_obj [NGS.Generic<NGM.Generic<Object, Exception, RuntimeException>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [NGS.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [NGS.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [NGS.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic<NGS.Generic<?>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic<NGS.Generic<NGS.Generic<?>>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic<NGS.Generic<?>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGS.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_obj [NGS.Generic<Object>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_obj [NGS.Generic<? extends Throwable>] EXACT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_obj [NGS.Generic<? super RuntimeException>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_wld [NGS.Generic<g3.t.def.NGM.Generic<?, ?, ?>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [NGS.Generic<g3.t.def.NGM.Generic<g3.t.def.NGM.Generic<?, ?, ?>, g3.t.def.NGM.Generic<?, ?, ?>, g3.t.def.NGM.Generic<?, ?, ?>>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_obj [NGS.Generic<g3.t.def.NGM.Generic<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [NGS.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [NGS.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [NGS.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [NGS.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [NGS.Generic<g3.t.def.NGS.Generic<?>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [NGS.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [NGS.Generic<g3.t.def.NGS.Generic<g3.t.def.NGS.Generic<?>>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [NGS.Generic<g3.t.def.NGS.Generic<?>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [NGS.Generic<?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [NGS.Generic<java.lang.Object>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [NGS.Generic<? extends java.lang.Throwable>] EXACT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [NGS.Generic<? super java.lang.RuntimeException>] ERASURE_MATCH",
			this.resultCollector);
	}

	// String pattern with nested multiple type arguments
	public void testStringNestedMultipleArguments01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchCUScope("JavaSearch15", "g3/t/ref", "R1.java");
		search("GM<Object, Exception, RuntimeException>", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R1.java [GM] EQUIVALENT_RAW_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_wld [GM<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [GM<GM<?, ?, ?>,GM<?, ?, ?>,GM<?, ?, ?>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [GM<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [GM<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_www [GM<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgsm_obj [GM<Object, Exception, RuntimeException>] EXACT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_wld [GM<GS<?>, GS<?>, GS<?>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_www [GM<GS<?>, GS<GS<?>>, GS<GS<GS<?>>>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.sgms_obj [GM<GS<Object>, GS<? extends Throwable>, GS<? super RuntimeException>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_wld [GM<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [GM<g3.t.def.GM<?, ?, ?>, g3.t.def.GM<?, ?, ?>, g3.t.def.GM<?, ?, ?>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [GM<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [GM<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_www [GM<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgsm_obj [GM<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>] EXACT_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_wld [GM<g3.t.def.GS<?>, g3.t.def.GS<?>, g3.t.def.GS<?>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_www [GM<g3.t.def.GS<?>, g3.t.def.GS<g3.t.def.GS<?>>, g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>>>>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R1.java g3.t.ref.R1.qgms_obj [GM<g3.t.def.GS<java.lang.Object>, g3.t.def.GS<? extends java.lang.Throwable>, g3.t.def.GS<? super java.lang.RuntimeException>>] ERASURE_MATCH",
			this.resultCollector);
	}
	public void testStringNestedMultipleArguments02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("GM<java.lang.Object, ? extends java.lang.Exception, ? super java.lang.RuntimeException>.Member", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_wld [GM<?, ?, ?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<GM<?, ?, ?>.Member,GM<?, ?, ?>.Member,GM<?, ?, ?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_obj [GM<Object, Exception, RuntimeException>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GM<GS<?>.Member, GS<?>.Member, GS<?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GM<GS<?>.Member, GS<GS<?>.Member>.Member, GS<GS<GS<?>.Member>.Member>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_obj [GM<GS<Object>.Member, GS<? extends Throwable>.Member, GS<? super RuntimeException>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_wld [GM<?, ?, ?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [GM<g3.t.def.GM<?, ?, ?>.Member, g3.t.def.GM<?, ?, ?>.Member, g3.t.def.GM<?, ?, ?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [GM<?, ?, ?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [GM<?, ?, ?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [GM<?, ?, ?>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_obj [GM<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [GM<g3.t.def.GS<?>.Member, g3.t.def.GS<?>.Member, g3.t.def.GS<?>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [GM<g3.t.def.GS<?>.Member, g3.t.def.GS<g3.t.def.GS<?>.Member>.Member, g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>.Member>.Member>.Member>.Member] ERASURE_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [GM<g3.t.def.GS<java.lang.Object>.Member, g3.t.def.GS<? extends java.lang.Throwable>.Member, g3.t.def.GS<? super java.lang.RuntimeException>.Member>.Member] ERASURE_MATCH",
			this.resultCollector);
	}
	public void testStringNestedMultipleArguments03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("GM<Object, Exception, RuntimeException>.Generic<?, ?, ?>", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_wld [GM<?, ?, ?>.Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GM<GM<?, ?, ?>.Generic<?,?,?>,GM<?, ?, ?>.Generic<?,?,?>,GM<?, ?, ?>.Generic<?,?,?>>.Generic<?,?,?>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GM<?, ?, ?>.Generic<?,?,?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GM<?, ?, ?>.Generic<?,?,?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_www [GM<?, ?, ?>.Generic<?,?,?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgsm_obj [GM<Object, Exception, RuntimeException>.Generic<Object, Exception, RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_wld [GM<GS<?>.Generic<?>, GS<?>.Generic<?>, GS<?>.Generic<?>>.Generic<?,?,?>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_www [GM<GS<?>.Generic<?>, GS<GS<?>.Generic<?>>.Generic<?>, GS<GS<GS<?>.Generic<?>>.Generic<?>>.Generic<?>>.Generic<?,?,?>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.sgms_obj [GM<GS<Object>.Generic<?>, GS<? extends Throwable>.Generic<?>, GS<? super RuntimeException>.Generic<?>>.Generic<?,?,?>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_wld [GM<?, ?, ?>.Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [GM<g3.t.def.GM<?, ?, ?>.Generic<?,?,?>,g3.t.def.GM<?, ?, ?>.Generic<?,?,?>,g3.t.def.GM<?, ?, ?>.Generic<?,?,?>>.Generic<?,?,?>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [GM<?, ?, ?>.Generic<?,?,?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [GM<?, ?, ?>.Generic<?,?,?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_www [GM<?, ?, ?>.Generic<?,?,?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgsm_obj [GM<Object, Exception, RuntimeException>.Generic<Object, Exception, RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_wld [GM<g3.t.def.GS<?>.Generic<?>, g3.t.def.GS<?>.Generic<?>, g3.t.def.GS<?>.Generic<?>>.Generic<?,?,?>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_www [GM<g3.t.def.GS<?>.Generic<?>, g3.t.def.GS<g3.t.def.GS<?>.Generic<?>>.Generic<?>, g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>.Generic<?>>.Generic<?>>.Generic<?>>.Generic<?,?,?>] ERASURE_MATCH\n" +
			"src/g3/t/ref/R3.java g3.t.ref.R3.qgms_obj [GM<g3.t.def.GS<Object>.Generic<?>, g3.t.def.GS<? extends Throwable>.Generic<?>, g3.t.def.GS<? super RuntimeException>.Generic<?>>.Generic<?,?,?>] ERASURE_MATCH",
			this.resultCollector);
	}
	public void testStringNestedMultipleArguments04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("g3.t.def.GM<?, ?, ?>.Member", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_wld [GM<?, ?, ?>.Member] EXACT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<GM<?, ?, ?>.Member,GM<?, ?, ?>.Member,GM<?, ?, ?>.Member>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] EXACT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] EXACT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_www [GM<?, ?, ?>.Member] EXACT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgsm_obj [GM<Object, Exception, RuntimeException>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_wld [GM<GS<?>.Member, GS<?>.Member, GS<?>.Member>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_www [GM<GS<?>.Member, GS<GS<?>.Member>.Member, GS<GS<GS<?>.Member>.Member>.Member>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.sgms_obj [GM<GS<Object>.Member, GS<? extends Throwable>.Member, GS<? super RuntimeException>.Member>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_wld [g3.t.def.GM<?, ?, ?>.Member] EXACT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<g3.t.def.GM<?, ?, ?>.Member, g3.t.def.GM<?, ?, ?>.Member, g3.t.def.GM<?, ?, ?>.Member>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<?, ?, ?>.Member] EXACT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<?, ?, ?>.Member] EXACT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_www [g3.t.def.GM<?, ?, ?>.Member] EXACT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgsm_obj [g3.t.def.GM<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_wld [g3.t.def.GM<g3.t.def.GS<?>.Member, g3.t.def.GS<?>.Member, g3.t.def.GS<?>.Member>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_www [g3.t.def.GM<g3.t.def.GS<?>.Member, g3.t.def.GS<g3.t.def.GS<?>.Member>.Member, g3.t.def.GS<g3.t.def.GS<g3.t.def.GS<?>.Member>.Member>.Member>.Member] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R2.java g3.t.ref.R2.qgms_obj [g3.t.def.GM<g3.t.def.GS<java.lang.Object>.Member, g3.t.def.GS<? extends java.lang.Throwable>.Member, g3.t.def.GS<? super java.lang.RuntimeException>.Member>.Member] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testStringNestedMultipleArguments05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15("g3.t", true /* add all subpackages */);
		search("NGM.Generic<? extends java.lang.Object, ? extends java.lang.Object, ? extends java.lang.Object>", TYPE, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_wld [NGM.Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGM.Generic<NGM.Generic<?, ?, ?>,NGM.Generic<?, ?, ?>,NGM.Generic<?, ?, ?>>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGM.Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGM.Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_www [NGM.Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgsm_obj [NGM.Generic<Object, Exception, RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_wld [NGM.Generic<NGS.Generic<?>, NGS.Generic<?>, NGS.Generic<?>>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_www [NGM.Generic<NGS.Generic<?>, NGS.Generic<NGS.Generic<?>>, NGS.Generic<NGS.Generic<NGS.Generic<?>>>>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.sgms_obj [NGM.Generic<NGS.Generic<Object>, NGS.Generic<? extends Throwable>, NGS.Generic<? super RuntimeException>>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_wld [NGM.Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [NGM.Generic<g3.t.def.NGM.Generic<?, ?, ?>, g3.t.def.NGM.Generic<?, ?, ?>, g3.t.def.NGM.Generic<?, ?, ?>>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [NGM.Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [NGM.Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_www [NGM.Generic<?, ?, ?>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgsm_obj [NGM.Generic<java.lang.Object, java.lang.Exception, java.lang.RuntimeException>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_wld [NGM.Generic<g3.t.def.NGS.Generic<?>, g3.t.def.NGS.Generic<?>, g3.t.def.NGS.Generic<?>>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_www [NGM.Generic<g3.t.def.NGS.Generic<?>, g3.t.def.NGS.Generic<g3.t.def.NGS.Generic<?>>, g3.t.def.NGS.Generic<g3.t.def.NGS.Generic<g3.t.def.NGS.Generic<?>>>>] EQUIVALENT_MATCH\n" +
			"src/g3/t/ref/R4.java g3.t.ref.R4.qgms_obj [NGM.Generic<g3.t.def.NGS.Generic<java.lang.Object>, g3.t.def.NGS.Generic<? extends java.lang.Throwable>, g3.t.def.NGS.Generic<? super java.lang.RuntimeException>>] EQUIVALENT_MATCH",
			this.resultCollector);
	}

	// Parameterized array type with single type argument
	public void testParameterizedArrayTypeSingleArgument01() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g6/t/ref/Single.java");
		IType type = selectType(unit,  "List", 2 /* 2nd occurence*/); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g6", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionList [g6.t.def.List] EQUIVALENT_MATCH\n" +
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArray [g6.t.def.List] EQUIVALENT_MATCH\n" +
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArrayList [g6.t.def.List] EQUIVALENT_MATCH\n" +
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArrayList [g6.t.def.List] EQUIVALENT_MATCH\n" +
			"src/g6/t/ref/Single.java [g6.t.def.List] EXACT_RAW_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.list [List] EXACT_RAW_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionList [List] EQUIVALENT_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArray [List] EQUIVALENT_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArrayList [List] EQUIVALENT_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArrayList [List] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testParameterizedArrayTypeSingleArgument02() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g6/t/ref/Single.java");
		IType type = selectType(unit,  "List<Exception>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g6", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionList [g6.t.def.List<Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArray [g6.t.def.List<Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArrayList [g6.t.def.List<g6.t.def.List<Exception>[]>] ERASURE_MATCH\n" +
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArrayList [g6.t.def.List<Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/Single.java [g6.t.def.List] EQUIVALENT_RAW_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.list [List] EQUIVALENT_RAW_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionList [List<Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArray [List<Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArrayList [List<List<Exception>[]>] ERASURE_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArrayList [List<Exception>] EXACT_MATCH",

			this.resultCollector);
	}
	public void testParameterizedArrayTypeSingleArgument03() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g6/t/ref/QualifSingle.java");
		IType type = selectType(unit,  "g6.t.def.List<Exception>", 2 /* 2nd occurence*/); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g6", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionList [g6.t.def.List<Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArray [g6.t.def.List<Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArrayList [g6.t.def.List<g6.t.def.List<Exception>[]>] ERASURE_MATCH\n" +
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArrayList [g6.t.def.List<Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/Single.java [g6.t.def.List] EQUIVALENT_RAW_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.list [List] EQUIVALENT_RAW_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionList [List<Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArray [List<Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArrayList [List<List<Exception>[]>] ERASURE_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArrayList [List<Exception>] EXACT_MATCH",
			this.resultCollector);
	}
	public void testParameterizedArrayTypeSingleArgument04() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g6/t/ref/QualifSingle.java");
		IType type = selectType(unit,  "g6.t.def.List<g6.t.def.List<Exception>[]>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g6", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionList [g6.t.def.List<Exception>] ERASURE_MATCH\n" +
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArray [g6.t.def.List<Exception>] ERASURE_MATCH\n" +
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArrayList [g6.t.def.List<g6.t.def.List<Exception>[]>] EXACT_MATCH\n" +
			"src/g6/t/ref/QualifSingle.java g6.t.ref.QualifSingle.exceptionListArrayList [g6.t.def.List<Exception>] ERASURE_MATCH\n" +
			"src/g6/t/ref/Single.java [g6.t.def.List] EQUIVALENT_RAW_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.list [List] EQUIVALENT_RAW_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionList [List<Exception>] ERASURE_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArray [List<Exception>] ERASURE_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArrayList [List<List<Exception>[]>] EXACT_MATCH\n" +
			"src/g6/t/ref/Single.java g6.t.ref.Single.exceptionListArrayList [List<Exception>] ERASURE_MATCH",
			this.resultCollector);
	}

	// Parameterized array type with multiple type arguments
	public void testParameterizedArrayTypeMultipleArguments01() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g6/t/ref/Multiple.java");
		IType type = selectType(unit,  "Table.Entry"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g6", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entry [Table.Entry] EXACT_RAW_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entryException [Table<String, Exception>.Entry] EQUIVALENT_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entryExceptionArray [Table<String, Exception>.Entry] EQUIVALENT_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Table<String, Exception>.Entry<String, Exception>[]>.Entry] EQUIVALENT_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Exception>.Entry] EQUIVALENT_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Exception>.Entry] EQUIVALENT_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryException [g6.t.def.Table<String, Exception>.Entry] EQUIVALENT_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryExceptionArray [g6.t.def.Table<String, Exception>.Entry] EQUIVALENT_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]>.Entry] EQUIVALENT_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, Exception>.Entry] EQUIVALENT_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, Exception>.Entry] EQUIVALENT_MATCH",
			this.resultCollector);
	}
	public void testParameterizedArrayTypeMultipleArguments02() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g6/t/ref/QualifMultiple.java");
		IType type = selectType(unit,  "Table<String, Exception>.Entry<String, Exception>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g6", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entry [Table.Entry] EQUIVALENT_RAW_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entryException [Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entryExceptionArray [Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Table<String, Exception>.Entry<String, Exception>[]>.Entry<String, Table<String, Exception>.Entry<String, Exception>[]>] ERASURE_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryException [g6.t.def.Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryExceptionArray [g6.t.def.Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]>.Entry<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]>] ERASURE_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH",
			this.resultCollector);
	}
	public void testParameterizedArrayTypeMultipleArguments03() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g6/t/ref/Multiple.java");
		IType type = selectType(unit,  "Table<String, Exception>.Entry<String, Exception>", 2); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g6", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entry [Table.Entry] EQUIVALENT_RAW_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entryException [Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entryExceptionArray [Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Table<String, Exception>.Entry<String, Exception>[]>.Entry<String, Table<String, Exception>.Entry<String, Exception>[]>] ERASURE_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryException [g6.t.def.Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryExceptionArray [g6.t.def.Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]>.Entry<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]>] ERASURE_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, Exception>.Entry<String, Exception>] EXACT_MATCH",
			this.resultCollector);
	}
	public void testParameterizedArrayTypeMultipleArguments04() throws CoreException {
		ICompilationUnit unit = getCompilationUnit("JavaSearch15/src/g6/t/ref/Multiple.java");
		IType type = selectType(unit,  "Table<String, Table<String, Exception>.Entry<String, Exception>[]>.Entry<String, Table<String, Exception>.Entry<String, Exception>[]>"); //$NON-NLS-1$
		IJavaSearchScope scope = getJavaSearchScope15("g6", true /* add all subpackages */);
		search(type, REFERENCES, scope, this.resultCollector);
		assertSearchResults(
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entry [Table.Entry] EQUIVALENT_RAW_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entryException [Table<String, Exception>.Entry<String, Exception>] ERASURE_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.entryExceptionArray [Table<String, Exception>.Entry<String, Exception>] ERASURE_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Table<String, Exception>.Entry<String, Exception>[]>.Entry<String, Table<String, Exception>.Entry<String, Exception>[]>] EXACT_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Exception>.Entry<String, Exception>] ERASURE_MATCH\n" +
			"src/g6/t/ref/Multiple.java g6.t.ref.Multiple.tableOfEntryExceptionArray [Table<String, Exception>.Entry<String, Exception>] ERASURE_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryException [g6.t.def.Table<String, Exception>.Entry<String, Exception>] ERASURE_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.entryExceptionArray [g6.t.def.Table<String, Exception>.Entry<String, Exception>] ERASURE_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]>.Entry<String, g6.t.def.Table<String, Exception>.Entry<String, Exception>[]>] EXACT_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, Exception>.Entry<String, Exception>] ERASURE_MATCH\n" +
			"src/g6/t/ref/QualifMultiple.java g6.t.ref.QualifMultiple.tableOfEntryExceptionArray [g6.t.def.Table<String, Exception>.Entry<String, Exception>] ERASURE_MATCH",
			this.resultCollector);
	}

	/**
	 * Bug 83713: [search] IAE while searching reference to parameterized type
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83713"
	 */
	public void testParameterizedType_Bug83713() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearch15/src/p/X.java",
			"package p;\n" +
			"public class X<T> {}\n" +
			"class Y<E> extends X<E> {\n" +
			"	X<E> y1;\n" +
			"	X<E> y2;\n" +
			"}\n"
			);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
		IType type = selectParameterizedType(this.workingCopies[0], "X<E>", 2);
		search(type, REFERENCES, scope);
		assertSearchResults(
			"src/p/X.java p.Y [X<E>] EXACT_MATCH\n" +
			"src/p/X.java p.Y.y1 [X<E>] EXACT_MATCH\n" +
			"src/p/X.java p.Y.y2 [X<E>] EXACT_MATCH"
		);
	}
}
