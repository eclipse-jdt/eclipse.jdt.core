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
 * Test search for generic fields.
 */
public class JavaSearchGenericFieldTests extends JavaSearchTests {

	public JavaSearchGenericFieldTests(String name) {
		super(name);
	}
	public static Test suite() {
		return buildTestSuite(JavaSearchGenericFieldTests.class);
	}
	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
		// Prefix for names of tests to run
//		TESTS_PREFIX =  "testGenericFieldReference";
		// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//		TESTS_NAMES = new String[] { "testGenericFieldReferenceAC04" };
		// Numbers of tests to run: "test<number>" will be run for each number of this array
//		TESTS_NUMBERS = new int[] { 8 };
		// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//		TESTS_SEARCH = new int[] { -1, -1 };
	}

	protected void setUp () throws Exception {
		super.setUp();
		this.resultCollector.showAccuracy = true;
	}

	/**
	 * Bug 73277: [1.5][Search] Fields search does not work with generics
	 * (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=73277)
	 */
	/*
	 * Following functionalities are tested:
	 * 	A) Search using an IJavaElement
	 * 		a) single parameter generic type field
	 * 		b) multiple parameters generic type field
	 * 		c) single parameterized type field
	 * 		d) mutliple parameterized type field
	 * 	B) Search using a string pattern
	 * 		a) single name
	 * 			GenericFieldReferenceBA* tests
	 * 		b) any char characters
	 * 			GenericFieldReferenceBB* tests
	 * 		b) any string characters
	 * 			GenericFieldReferenceBB* tests
	 */
	// Search reference to a field of generic type
	public void testElementPatternSingleTypeArgument01() throws CoreException {
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getField("t");
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a field of field of member type declared in a generic type
	public void testElementPatternSingleTypeArgument02() throws CoreException {
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getType("Member").getField("m");
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a field of field of generic member type declared in a generic type
	public void testElementPatternSingleTypeArgument03() throws CoreException {
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getType("MemberGeneric").getField("v");
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R3.java g2.f.s.def.R3.{} [v] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a field of generic member type declared in a non-generic type
	public void testElementPatternSingleTypeArgument04() throws CoreException {
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/def/NonGeneric.java").getType("NonGeneric").getType("GenericMember").getField("t");
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" + 
			"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a field of generic type
	public void testElementPatternMultipleTypeArgument01() throws CoreException {
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getField("t1");
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a field of member type declared in a generic type
	public void testElementPatternMultipleTypeArgument02() throws CoreException {
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getType("Member").getField("m");
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a field of generic member type declared in a generic type
	public void testElementPatternMultipleTypeArgument03() throws CoreException {
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getType("MemberGeneric").getField("u2");
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R3.java g2.f.m.def.R3.{} [u2] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a field of generic member type declared in a non-generic type
	public void testElementPatternMultipleTypeArgument04() throws CoreException {
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/def/NonGeneric.java").getType("NonGeneric").getType("GenericMember").getField("t3");
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" + 
			"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a single parameterized field
	public void testElementPatternSingleParameterizedType01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("gen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("gen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("gen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("gen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("gen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("gen_run");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("qgen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("qgen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("qgen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("qgen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("qgen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("qgen_run");
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_obj] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_exc] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_wld] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_thr] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_run] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_obj] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_exc] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_wld] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_thr] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_run] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type field of single parameterized type
	public void testElementPatternSingleParameterizedType02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("gen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("gen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("gen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("gen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("gen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("gen_run");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("qgen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("qgen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("qgen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("qgen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("qgen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("qgen_run");
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_obj] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_exc] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_wld] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_thr] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_run] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_obj] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_exc] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_wld] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_thr] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_run] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a single parameterized member type field of a single parameterized type
	public void testElementPatternSingleParameterizedType03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("gen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("gen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("gen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("gen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("gen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("gen_run");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("qgen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("qgen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("qgen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("qgen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("qgen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("qgen_run");
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_obj] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_exc] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_wld] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_thr] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_run] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_obj] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_exc] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_wld] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_thr] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_run] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a single parameterized member type field
	public void testElementPatternSingleParameterizedType04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("gen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("gen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("gen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("gen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("gen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("gen_run");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("qgen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("qgen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("qgen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("qgen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("qgen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("qgen_run");
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_obj] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_exc] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_wld] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_thr] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_run] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_obj] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_exc] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_wld] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_thr] EXACT_MATCH\n" +
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_run] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a multiple parameterized field
	public void testElementPatternMultipleParameterizedType01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("gen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("gen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("gen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("gen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("gen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("gen_run");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("qgen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("qgen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("qgen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("qgen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("qgen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("qgen_run");
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_obj] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_exc] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_wld] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_thr] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_run] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_obj] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_exc] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_wld] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_thr] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_run] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a member type field of multiple parameterized type
	public void testElementPatternMultipleParameterizedType02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("gen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("gen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("gen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("gen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("gen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("gen_run");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("qgen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("qgen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("qgen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("qgen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("qgen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("qgen_run");
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_obj] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_exc] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_wld] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_thr] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_run] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_obj] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_exc] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_wld] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_thr] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_run] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a multiple parameterized member type field of a multiple parameterized type
	public void testElementPatternMultipleParameterizedType03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("gen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("gen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("gen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("gen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("gen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("gen_run");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("qgen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("qgen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("qgen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("qgen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("qgen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("qgen_run");
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_obj] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_exc] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_wld] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_thr] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_run] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_obj] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_exc] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_wld] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_thr] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_run] EXACT_MATCH",
			resultCollector);
	}
	// Search reference to a multiple parameterized member type field
	public void testElementPatternMultipleParameterizedType04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchPackageScope("g2.f", true /* add all subpackages */);
		IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("gen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("gen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("gen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("gen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("gen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("gen_run");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("qgen");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("qgen_obj");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("qgen_exc");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("qgen_wld");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("qgen_thr");
		search(field, REFERENCES, scope, resultCollector);
		field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("qgen_run");
		search(field, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_obj] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_exc] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_wld] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_thr] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_run] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_obj] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_exc] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_wld] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_thr] EXACT_MATCH\n" +
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_run] EXACT_MATCH",
			resultCollector);
	}

	// Search reference to a string pattern
	public void testStringPattern01() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("gen", FIELD, ALL_OCCURRENCES, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen [gen] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen [gen] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen [gen] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen [gen] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen [gen] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen [gen] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen [gen] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen [gen] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen] EXACT_MATCH",
			resultCollector);
	}
	public void testStringPattern02() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("gen_???", FIELD, DECLARATIONS, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_obj [gen_obj] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_exc [gen_exc] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_wld [gen_wld] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_thr [gen_thr] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.gen_run [gen_run] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_obj [gen_obj] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_exc [gen_exc] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_wld [gen_wld] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_thr [gen_thr] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.gen_run [gen_run] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_obj [gen_obj] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_exc [gen_exc] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_wld [gen_wld] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_thr [gen_thr] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.gen_run [gen_run] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_obj [gen_obj] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_exc [gen_exc] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_wld [gen_wld] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_thr [gen_thr] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.gen_run [gen_run] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_obj [gen_obj] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_exc [gen_exc] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_wld [gen_wld] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_thr [gen_thr] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.gen_run [gen_run] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_obj [gen_obj] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_exc [gen_exc] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_wld [gen_wld] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_thr [gen_thr] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.gen_run [gen_run] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_obj [gen_obj] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_exc [gen_exc] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_wld [gen_wld] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_thr [gen_thr] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.gen_run [gen_run] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_obj [gen_obj] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_exc [gen_exc] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_wld [gen_wld] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_thr [gen_thr] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.gen_run [gen_run] EXACT_MATCH",
			resultCollector);
	}
	public void testStringPattern03() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("gen_*", FIELD, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_obj] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_exc] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_wld] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_thr] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [gen_run] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_obj] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_exc] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_wld] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_thr] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [gen_run] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_obj] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_exc] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_wld] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_thr] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [gen_run] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_obj] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_exc] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_wld] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_thr] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [gen_run] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_obj] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_exc] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_wld] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_thr] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [gen_run] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_obj] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_exc] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_wld] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_thr] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [gen_run] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_obj] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_exc] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_wld] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_thr] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [gen_run] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_obj] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_exc] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_wld] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_thr] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [gen_run] EXACT_MATCH",
			resultCollector);
	}
	public void testStringPattern04() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("?gen_*", FIELD, DECLARATIONS, scope, resultCollector);
		assertSearchResults(
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_obj [qgen_obj] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_exc [qgen_exc] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_wld [qgen_wld] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_thr [qgen_thr] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R1.java g1.t.m.ref.R1.qgen_run [qgen_run] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_obj [qgen_obj] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_exc [qgen_exc] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_wld [qgen_wld] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_thr [qgen_thr] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R2.java g1.t.m.ref.R2.qgen_run [qgen_run] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_obj [qgen_obj] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_exc [qgen_exc] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_wld [qgen_wld] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_thr [qgen_thr] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R3.java g1.t.m.ref.R3.qgen_run [qgen_run] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_obj [qgen_obj] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_exc [qgen_exc] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_wld [qgen_wld] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_thr [qgen_thr] EXACT_MATCH\n" + 
			"src/g1/t/m/ref/R4.java g1.t.m.ref.R4.qgen_run [qgen_run] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_obj [qgen_obj] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_exc [qgen_exc] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_wld [qgen_wld] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_thr [qgen_thr] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R1.java g1.t.s.ref.R1.qgen_run [qgen_run] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_obj [qgen_obj] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_exc [qgen_exc] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_wld [qgen_wld] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_thr [qgen_thr] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R2.java g1.t.s.ref.R2.qgen_run [qgen_run] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_obj [qgen_obj] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_exc [qgen_exc] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_wld [qgen_wld] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_thr [qgen_thr] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R3.java g1.t.s.ref.R3.qgen_run [qgen_run] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_obj [qgen_obj] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_exc [qgen_exc] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_wld [qgen_wld] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_thr [qgen_thr] EXACT_MATCH\n" + 
			"src/g1/t/s/ref/R4.java g1.t.s.ref.R4.qgen_run [qgen_run] EXACT_MATCH",
			resultCollector);
	}
	public void testStringPattern05() throws CoreException {
		IJavaSearchScope scope = getJavaSearchScope15();
		search("qgen_*", FIELD, REFERENCES, scope, resultCollector);
		assertSearchResults(
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_obj] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_exc] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_wld] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_thr] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR1.java g2.f.m.ref.RR1.{} [qgen_run] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_obj] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_exc] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_wld] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_thr] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR2.java g2.f.m.ref.RR2.{} [qgen_run] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_obj] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_exc] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_wld] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_thr] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR3.java g2.f.m.ref.RR3.{} [qgen_run] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_obj] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_exc] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_wld] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_thr] EXACT_MATCH\n" + 
			"src/g2/f/m/ref/RR4.java g2.f.m.ref.RR4.{} [qgen_run] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_obj] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_exc] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_wld] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_thr] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR1.java g2.f.s.ref.RR1.{} [qgen_run] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_obj] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_exc] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_wld] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_thr] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR2.java g2.f.s.ref.RR2.{} [qgen_run] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_obj] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_exc] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_wld] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_thr] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR3.java g2.f.s.ref.RR3.{} [qgen_run] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_obj] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_exc] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_wld] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_thr] EXACT_MATCH\n" + 
			"src/g2/f/s/ref/RR4.java g2.f.s.ref.RR4.{} [qgen_run] EXACT_MATCH",
			resultCollector);
	}
}
