/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
	return buildModelTestSuite(JavaSearchGenericFieldTests.class);
}

@Override
protected void setUp () throws Exception {
	super.setUp();
	this.resultCollector.showAccuracy(true);
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
 * 	C) Search local variables using an IJavaElement
 * 		a) single parameter generic type field
 * 		b) multiple parameters generic type field
 * 		c) single parameterized type field
 * 		d) multiple parameterized type field
 */
// Search reference to a field of generic type
public void testElementPatternSingleTypeArgument01() throws CoreException {
	IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getField("t");
	IJavaSearchScope scope = getJavaSearchScope15("g2.f", true /* add all subpackages */);
	search(field, REFERENCES, scope, this.resultCollector);
	assertSearchResults(
		"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" +
		"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" +
		"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" +
		"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" +
		"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" +
		"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" +
		"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH\n" +
		"src/g2/f/s/def/R1.java g2.f.s.def.R1.{} [t] EXACT_MATCH",
		this.resultCollector);
}
// Search reference to a field of field of member type declared in a generic type
public void testElementPatternSingleTypeArgument02() throws CoreException {
	IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getType("Member").getField("m");
	IJavaSearchScope scope = getJavaSearchScope15("g2.f", true /* add all subpackages */);
	search(field, REFERENCES, scope, this.resultCollector);
	assertSearchResults(
		"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" +
		"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" +
		"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" +
		"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" +
		"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" +
		"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" +
		"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH\n" +
		"src/g2/f/s/def/R2.java g2.f.s.def.R2.{} [m] EXACT_MATCH",
		this.resultCollector);
}
// Search reference to a field of field of generic member type declared in a generic type
public void testElementPatternSingleTypeArgument03() throws CoreException {
	IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/def/Generic.java").getType("Generic").getType("MemberGeneric").getField("v");
	IJavaSearchScope scope = getJavaSearchScope15("g2.f", true /* add all subpackages */);
	search(field, REFERENCES, scope, this.resultCollector);
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
		this.resultCollector);
}
// Search reference to a field of generic member type declared in a non-generic type
public void testElementPatternSingleTypeArgument04() throws CoreException {
	IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/def/NonGeneric.java").getType("NonGeneric").getType("GenericMember").getField("t");
	IJavaSearchScope scope = getJavaSearchScope15("g2.f", true /* add all subpackages */);
	search(field, REFERENCES, scope, this.resultCollector);
	assertSearchResults(
		"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" +
		"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" +
		"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" +
		"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" +
		"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" +
		"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" +
		"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH\n" +
		"src/g2/f/s/def/R4.java g2.f.s.def.R4.{} [t] EXACT_MATCH",
		this.resultCollector);
}
// Search reference to a field of generic type
public void testElementPatternMultipleTypeArgument01() throws CoreException {
	IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getField("t1");
	IJavaSearchScope scope = getJavaSearchScope15("g2.f", true /* add all subpackages */);
	search(field, REFERENCES, scope, this.resultCollector);
	assertSearchResults(
		"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" +
		"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" +
		"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" +
		"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" +
		"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" +
		"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" +
		"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH\n" +
		"src/g2/f/m/def/R1.java g2.f.m.def.R1.{} [t1] EXACT_MATCH",
		this.resultCollector);
}
// Search reference to a field of member type declared in a generic type
public void testElementPatternMultipleTypeArgument02() throws CoreException {
	IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getType("Member").getField("m");
	IJavaSearchScope scope = getJavaSearchScope15("g2.f", true /* add all subpackages */);
	search(field, REFERENCES, scope, this.resultCollector);
	assertSearchResults(
		"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" +
		"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" +
		"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" +
		"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" +
		"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" +
		"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" +
		"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH\n" +
		"src/g2/f/m/def/R2.java g2.f.m.def.R2.{} [m] EXACT_MATCH",
		this.resultCollector);
}
// Search reference to a field of generic member type declared in a generic type
public void testElementPatternMultipleTypeArgument03() throws CoreException {
	IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/def/Generic.java").getType("Generic").getType("MemberGeneric").getField("u2");
	IJavaSearchScope scope = getJavaSearchScope15("g2.f", true /* add all subpackages */);
	search(field, REFERENCES, scope, this.resultCollector);
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
		this.resultCollector);
}
// Search reference to a field of generic member type declared in a non-generic type
public void testElementPatternMultipleTypeArgument04() throws CoreException {
	IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/def/NonGeneric.java").getType("NonGeneric").getType("GenericMember").getField("t3");
	IJavaSearchScope scope = getJavaSearchScope15("g2.f", true /* add all subpackages */);
	search(field, REFERENCES, scope, this.resultCollector);
	assertSearchResults(
		"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" +
		"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" +
		"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" +
		"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" +
		"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" +
		"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" +
		"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH\n" +
		"src/g2/f/m/def/R4.java g2.f.m.def.R4.{} [t3] EXACT_MATCH",
		this.resultCollector);
}
// Search reference to a single parameterized field
public void testElementPatternSingleParameterizedType01() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g2.f", true /* add all subpackages */);
	IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("gen");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("gen_obj");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("gen_exc");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("gen_wld");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("gen_thr");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("gen_run");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("qgen");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("qgen_obj");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("qgen_exc");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("qgen_wld");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("qgen_thr");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R1.java").getType("R1").getField("qgen_run");
	search(field, REFERENCES, scope, this.resultCollector);
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
		this.resultCollector);
}
// Search reference to a member type field of single parameterized type
public void testElementPatternSingleParameterizedType02() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g2.f", true /* add all subpackages */);
	IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("gen");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("gen_obj");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("gen_exc");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("gen_wld");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("gen_thr");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("gen_run");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("qgen");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("qgen_obj");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("qgen_exc");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("qgen_wld");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("qgen_thr");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R2.java").getType("R2").getField("qgen_run");
	search(field, REFERENCES, scope, this.resultCollector);
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
		this.resultCollector);
}
// Search reference to a single parameterized member type field of a single parameterized type
public void testElementPatternSingleParameterizedType03() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g2.f", true /* add all subpackages */);
	IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("gen");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("gen_obj");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("gen_exc");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("gen_wld");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("gen_thr");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("gen_run");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("qgen");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("qgen_obj");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("qgen_exc");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("qgen_wld");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("qgen_thr");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R3.java").getType("R3").getField("qgen_run");
	search(field, REFERENCES, scope, this.resultCollector);
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
		this.resultCollector);
}
// Search reference to a single parameterized member type field
public void testElementPatternSingleParameterizedType04() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g2.f", true /* add all subpackages */);
	IField field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("gen");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("gen_obj");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("gen_exc");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("gen_wld");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("gen_thr");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("gen_run");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("qgen");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("qgen_obj");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("qgen_exc");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("qgen_wld");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("qgen_thr");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/s/ref/R4.java").getType("R4").getField("qgen_run");
	search(field, REFERENCES, scope, this.resultCollector);
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
		this.resultCollector);
}
// Search reference to a multiple parameterized field
public void testElementPatternMultipleParameterizedType01() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g2.f", true /* add all subpackages */);
	IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("gen");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("gen_obj");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("gen_exc");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("gen_wld");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("gen_thr");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("gen_run");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("qgen");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("qgen_obj");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("qgen_exc");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("qgen_wld");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("qgen_thr");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R1.java").getType("R1").getField("qgen_run");
	search(field, REFERENCES, scope, this.resultCollector);
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
		this.resultCollector);
}
// Search reference to a member type field of multiple parameterized type
public void testElementPatternMultipleParameterizedType02() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g2.f", true /* add all subpackages */);
	IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("gen");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("gen_obj");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("gen_exc");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("gen_wld");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("gen_thr");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("gen_run");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("qgen");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("qgen_obj");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("qgen_exc");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("qgen_wld");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("qgen_thr");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R2.java").getType("R2").getField("qgen_run");
	search(field, REFERENCES, scope, this.resultCollector);
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
		this.resultCollector);
}
// Search reference to a multiple parameterized member type field of a multiple parameterized type
public void testElementPatternMultipleParameterizedType03() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g2.f", true /* add all subpackages */);
	IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("gen");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("gen_obj");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("gen_exc");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("gen_wld");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("gen_thr");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("gen_run");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("qgen");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("qgen_obj");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("qgen_exc");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("qgen_wld");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("qgen_thr");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R3.java").getType("R3").getField("qgen_run");
	search(field, REFERENCES, scope, this.resultCollector);
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
		this.resultCollector);
}
// Search reference to a multiple parameterized member type field
public void testElementPatternMultipleParameterizedType04() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g2.f", true /* add all subpackages */);
	IField field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("gen");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("gen_obj");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("gen_exc");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("gen_wld");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("gen_thr");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("gen_run");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("qgen");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("qgen_obj");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("qgen_exc");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("qgen_wld");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("qgen_thr");
	search(field, REFERENCES, scope, this.resultCollector);
	field = getCompilationUnit("JavaSearch15/src/g1/t/m/ref/R4.java").getType("R4").getField("qgen_run");
	search(field, REFERENCES, scope, this.resultCollector);
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
		this.resultCollector);
}

// Search reference to a string pattern
public void testStringPattern01() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15();
	search("gen", FIELD, ALL_OCCURRENCES, scope, this.resultCollector);
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
		this.resultCollector);
}
public void testStringPattern02() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15();
	search("gen_???", FIELD, DECLARATIONS, scope, this.resultCollector);
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
		this.resultCollector);
}
public void testStringPattern03() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15();
	search("gen_*", FIELD, REFERENCES, scope, this.resultCollector);
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
		this.resultCollector);
}
public void testStringPattern04() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15();
	search("?gen_*", FIELD, DECLARATIONS, scope, this.resultCollector);
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
		this.resultCollector);
}
public void testStringPattern05() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15();
	search("qgen_*", FIELD, REFERENCES, scope, this.resultCollector);
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
		this.resultCollector);
}

// Search reference to a local variable
public void testElementPatternLocalVariables01() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g4.v.ref", false);
	ILocalVariable localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R1.java", "gen_obj,", "gen_obj");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R1.java", "gen_exc,", "gen_exc");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R1.java", "gen_thr,", "gen_thr");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R1.java", "gen_run)", "gen_run");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	assertSearchResults(
		"src/g4/v/ref/R1.java void g4.v.ref.R1.simple_name(Generic<Object>, Generic<Exception>, Generic<? extends Throwable>, Generic<? super RuntimeException>).gen_obj [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R1.java void g4.v.ref.R1.simple_name(Generic<Object>, Generic<Exception>, Generic<? extends Throwable>, Generic<? super RuntimeException>) [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R1.java void g4.v.ref.R1.simple_name(Generic<Object>, Generic<Exception>, Generic<? extends Throwable>, Generic<? super RuntimeException>).gen_exc [gen_exc] EXACT_MATCH\n" +
		"src/g4/v/ref/R1.java void g4.v.ref.R1.simple_name(Generic<Object>, Generic<Exception>, Generic<? extends Throwable>, Generic<? super RuntimeException>) [gen_exc] EXACT_MATCH\n" +
		"src/g4/v/ref/R1.java void g4.v.ref.R1.simple_name(Generic<Object>, Generic<Exception>, Generic<? extends Throwable>, Generic<? super RuntimeException>).gen_thr [gen_thr] EXACT_MATCH\n" +
		"src/g4/v/ref/R1.java void g4.v.ref.R1.simple_name(Generic<Object>, Generic<Exception>, Generic<? extends Throwable>, Generic<? super RuntimeException>) [gen_thr] EXACT_MATCH\n" +
		"src/g4/v/ref/R1.java void g4.v.ref.R1.simple_name(Generic<Object>, Generic<Exception>, Generic<? extends Throwable>, Generic<? super RuntimeException>).gen_run [gen_run] EXACT_MATCH\n" +
		"src/g4/v/ref/R1.java void g4.v.ref.R1.simple_name(Generic<Object>, Generic<Exception>, Generic<? extends Throwable>, Generic<? super RuntimeException>) [gen_run] EXACT_MATCH",
		this.resultCollector);
}
public void testElementPatternLocalVariables02() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g4.v.ref", false);
	ILocalVariable localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R1.java", "gen_obj =", "gen_obj");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R1.java", "gen_exc =", "gen_exc");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R1.java", "gen_thr =", "gen_thr");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R1.java", "gen_run =", "gen_run");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	assertSearchResults(
		"src/g4/v/ref/R1.java void g4.v.ref.R1.qualified_name().gen_obj [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R1.java void g4.v.ref.R1.qualified_name() [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R1.java void g4.v.ref.R1.qualified_name().gen_exc [gen_exc] EXACT_MATCH\n" +
		"src/g4/v/ref/R1.java void g4.v.ref.R1.qualified_name() [gen_exc] EXACT_MATCH\n" +
		"src/g4/v/ref/R1.java void g4.v.ref.R1.qualified_name().gen_thr [gen_thr] EXACT_MATCH\n" +
		"src/g4/v/ref/R1.java void g4.v.ref.R1.qualified_name() [gen_thr] EXACT_MATCH\n" +
		"src/g4/v/ref/R1.java void g4.v.ref.R1.qualified_name().gen_run [gen_run] EXACT_MATCH\n" +
		"src/g4/v/ref/R1.java void g4.v.ref.R1.qualified_name() [gen_run] EXACT_MATCH",
		this.resultCollector);
}
public void testElementPatternLocalVariables03() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g4.v.ref", false);
	ILocalVariable localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R2.java", "gen_obj,", "gen_obj");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R2.java", "gen_exc,", "gen_exc");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R2.java", "gen_thr,", "gen_thr");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R2.java", "gen_run)", "gen_run");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	assertSearchResults(
		"src/g4/v/ref/R2.java void g4.v.ref.R2.qualified_name(g1.t.s.def.NonGeneric.GenericMember<Object>, g1.t.s.def.NonGeneric.GenericMember<Exception>, g1.t.s.def.NonGeneric.GenericMember<? extends Throwable>, g1.t.s.def.NonGeneric.GenericMember<? super RuntimeException>).gen_obj [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R2.java void g4.v.ref.R2.qualified_name(g1.t.s.def.NonGeneric.GenericMember<Object>, g1.t.s.def.NonGeneric.GenericMember<Exception>, g1.t.s.def.NonGeneric.GenericMember<? extends Throwable>, g1.t.s.def.NonGeneric.GenericMember<? super RuntimeException>) [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R2.java void g4.v.ref.R2.qualified_name(g1.t.s.def.NonGeneric.GenericMember<Object>, g1.t.s.def.NonGeneric.GenericMember<Exception>, g1.t.s.def.NonGeneric.GenericMember<? extends Throwable>, g1.t.s.def.NonGeneric.GenericMember<? super RuntimeException>).gen_exc [gen_exc] EXACT_MATCH\n" +
		"src/g4/v/ref/R2.java void g4.v.ref.R2.qualified_name(g1.t.s.def.NonGeneric.GenericMember<Object>, g1.t.s.def.NonGeneric.GenericMember<Exception>, g1.t.s.def.NonGeneric.GenericMember<? extends Throwable>, g1.t.s.def.NonGeneric.GenericMember<? super RuntimeException>) [gen_exc] EXACT_MATCH\n" +
		"src/g4/v/ref/R2.java void g4.v.ref.R2.qualified_name(g1.t.s.def.NonGeneric.GenericMember<Object>, g1.t.s.def.NonGeneric.GenericMember<Exception>, g1.t.s.def.NonGeneric.GenericMember<? extends Throwable>, g1.t.s.def.NonGeneric.GenericMember<? super RuntimeException>).gen_thr [gen_thr] EXACT_MATCH\n" +
		"src/g4/v/ref/R2.java void g4.v.ref.R2.qualified_name(g1.t.s.def.NonGeneric.GenericMember<Object>, g1.t.s.def.NonGeneric.GenericMember<Exception>, g1.t.s.def.NonGeneric.GenericMember<? extends Throwable>, g1.t.s.def.NonGeneric.GenericMember<? super RuntimeException>) [gen_thr] EXACT_MATCH\n" +
		"src/g4/v/ref/R2.java void g4.v.ref.R2.qualified_name(g1.t.s.def.NonGeneric.GenericMember<Object>, g1.t.s.def.NonGeneric.GenericMember<Exception>, g1.t.s.def.NonGeneric.GenericMember<? extends Throwable>, g1.t.s.def.NonGeneric.GenericMember<? super RuntimeException>).gen_run [gen_run] EXACT_MATCH\n" +
		"src/g4/v/ref/R2.java void g4.v.ref.R2.qualified_name(g1.t.s.def.NonGeneric.GenericMember<Object>, g1.t.s.def.NonGeneric.GenericMember<Exception>, g1.t.s.def.NonGeneric.GenericMember<? extends Throwable>, g1.t.s.def.NonGeneric.GenericMember<? super RuntimeException>) [gen_run] EXACT_MATCH",
		this.resultCollector);
}
public void testElementPatternLocalVariables04() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g4.v.ref", false);
	ILocalVariable localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R2.java", "gen_obj =", "gen_obj");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R2.java", "gen_exc =", "gen_exc");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R2.java", "gen_thr =", "gen_thr");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R2.java", "gen_run =", "gen_run");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	assertSearchResults(
		"src/g4/v/ref/R2.java void g4.v.ref.R2.simple_name().gen_obj [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R2.java void g4.v.ref.R2.simple_name() [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R2.java void g4.v.ref.R2.simple_name().gen_exc [gen_exc] EXACT_MATCH\n" +
		"src/g4/v/ref/R2.java void g4.v.ref.R2.simple_name() [gen_exc] EXACT_MATCH\n" +
		"src/g4/v/ref/R2.java void g4.v.ref.R2.simple_name().gen_thr [gen_thr] EXACT_MATCH\n" +
		"src/g4/v/ref/R2.java void g4.v.ref.R2.simple_name() [gen_thr] EXACT_MATCH\n" +
		"src/g4/v/ref/R2.java void g4.v.ref.R2.simple_name().gen_run [gen_run] EXACT_MATCH\n" +
		"src/g4/v/ref/R2.java void g4.v.ref.R2.simple_name() [gen_run] EXACT_MATCH",
		this.resultCollector);
}
public void testElementPatternLocalVariables05() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g4.v.ref", false);
	ILocalVariable localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R3.java", "gen_obj,", "gen_obj");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R3.java", "gen_exc,", "gen_exc");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R3.java", "gen_thr,", "gen_thr");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R3.java", "gen_run)", "gen_run");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	assertSearchResults(
		"src/g4/v/ref/R3.java void g4.v.ref.R3.simple_name(Generic<Object>.MemberGeneric<Object>, Generic<Exception>.MemberGeneric<Exception>, Generic<? extends Throwable>.MemberGeneric<? extends Throwable>, Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException>).gen_obj [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R3.java void g4.v.ref.R3.simple_name(Generic<Object>.MemberGeneric<Object>, Generic<Exception>.MemberGeneric<Exception>, Generic<? extends Throwable>.MemberGeneric<? extends Throwable>, Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException>) [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R3.java void g4.v.ref.R3.simple_name(Generic<Object>.MemberGeneric<Object>, Generic<Exception>.MemberGeneric<Exception>, Generic<? extends Throwable>.MemberGeneric<? extends Throwable>, Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException>).gen_exc [gen_exc] EXACT_MATCH\n" +
		"src/g4/v/ref/R3.java void g4.v.ref.R3.simple_name(Generic<Object>.MemberGeneric<Object>, Generic<Exception>.MemberGeneric<Exception>, Generic<? extends Throwable>.MemberGeneric<? extends Throwable>, Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException>) [gen_exc] EXACT_MATCH\n" +
		"src/g4/v/ref/R3.java void g4.v.ref.R3.simple_name(Generic<Object>.MemberGeneric<Object>, Generic<Exception>.MemberGeneric<Exception>, Generic<? extends Throwable>.MemberGeneric<? extends Throwable>, Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException>).gen_thr [gen_thr] EXACT_MATCH\n" +
		"src/g4/v/ref/R3.java void g4.v.ref.R3.simple_name(Generic<Object>.MemberGeneric<Object>, Generic<Exception>.MemberGeneric<Exception>, Generic<? extends Throwable>.MemberGeneric<? extends Throwable>, Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException>) [gen_thr] EXACT_MATCH\n" +
		"src/g4/v/ref/R3.java void g4.v.ref.R3.simple_name(Generic<Object>.MemberGeneric<Object>, Generic<Exception>.MemberGeneric<Exception>, Generic<? extends Throwable>.MemberGeneric<? extends Throwable>, Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException>).gen_run [gen_run] EXACT_MATCH\n" +
		"src/g4/v/ref/R3.java void g4.v.ref.R3.simple_name(Generic<Object>.MemberGeneric<Object>, Generic<Exception>.MemberGeneric<Exception>, Generic<? extends Throwable>.MemberGeneric<? extends Throwable>, Generic<? super RuntimeException>.MemberGeneric<? super RuntimeException>) [gen_run] EXACT_MATCH",
		this.resultCollector);
}
public void testElementPatternLocalVariables06() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g4.v.ref", false);
	ILocalVariable localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R3.java", "gen_obj =", "gen_obj");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R3.java", "gen_exc =", "gen_exc");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R3.java", "gen_thr =", "gen_thr");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R3.java", "gen_run =", "gen_run");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	assertSearchResults(
		"src/g4/v/ref/R3.java void g4.v.ref.R3.qualified_name().gen_obj [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R3.java void g4.v.ref.R3.qualified_name() [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R3.java void g4.v.ref.R3.qualified_name().gen_exc [gen_exc] EXACT_MATCH\n" +
		"src/g4/v/ref/R3.java void g4.v.ref.R3.qualified_name() [gen_exc] EXACT_MATCH\n" +
		"src/g4/v/ref/R3.java void g4.v.ref.R3.qualified_name().gen_thr [gen_thr] EXACT_MATCH\n" +
		"src/g4/v/ref/R3.java void g4.v.ref.R3.qualified_name() [gen_thr] EXACT_MATCH\n" +
		"src/g4/v/ref/R3.java void g4.v.ref.R3.qualified_name().gen_run [gen_run] EXACT_MATCH\n" +
		"src/g4/v/ref/R3.java void g4.v.ref.R3.qualified_name() [gen_run] EXACT_MATCH",
		this.resultCollector);
}
public void testElementPatternLocalVariables07() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g4.v.ref", false);
	ILocalVariable localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R4.java", "gen_obj,", "gen_obj");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R4.java", "gen_exc,", "gen_exc");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R4.java", "gen_thr,", "gen_thr");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R4.java", "gen_run)", "gen_run");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	assertSearchResults(
		"src/g4/v/ref/R4.java void g4.v.ref.R4.qualified_name(g1.t.s.def.Generic<Object>.Member, g1.t.s.def.Generic<Exception>.Member, g1.t.s.def.Generic<? extends Throwable>.Member, g1.t.s.def.Generic<? super RuntimeException>.Member).gen_obj [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R4.java void g4.v.ref.R4.qualified_name(g1.t.s.def.Generic<Object>.Member, g1.t.s.def.Generic<Exception>.Member, g1.t.s.def.Generic<? extends Throwable>.Member, g1.t.s.def.Generic<? super RuntimeException>.Member) [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R4.java void g4.v.ref.R4.qualified_name(g1.t.s.def.Generic<Object>.Member, g1.t.s.def.Generic<Exception>.Member, g1.t.s.def.Generic<? extends Throwable>.Member, g1.t.s.def.Generic<? super RuntimeException>.Member).gen_exc [gen_exc] EXACT_MATCH\n" +
		"src/g4/v/ref/R4.java void g4.v.ref.R4.qualified_name(g1.t.s.def.Generic<Object>.Member, g1.t.s.def.Generic<Exception>.Member, g1.t.s.def.Generic<? extends Throwable>.Member, g1.t.s.def.Generic<? super RuntimeException>.Member) [gen_exc] EXACT_MATCH\n" +
		"src/g4/v/ref/R4.java void g4.v.ref.R4.qualified_name(g1.t.s.def.Generic<Object>.Member, g1.t.s.def.Generic<Exception>.Member, g1.t.s.def.Generic<? extends Throwable>.Member, g1.t.s.def.Generic<? super RuntimeException>.Member).gen_thr [gen_thr] EXACT_MATCH\n" +
		"src/g4/v/ref/R4.java void g4.v.ref.R4.qualified_name(g1.t.s.def.Generic<Object>.Member, g1.t.s.def.Generic<Exception>.Member, g1.t.s.def.Generic<? extends Throwable>.Member, g1.t.s.def.Generic<? super RuntimeException>.Member) [gen_thr] EXACT_MATCH\n" +
		"src/g4/v/ref/R4.java void g4.v.ref.R4.qualified_name(g1.t.s.def.Generic<Object>.Member, g1.t.s.def.Generic<Exception>.Member, g1.t.s.def.Generic<? extends Throwable>.Member, g1.t.s.def.Generic<? super RuntimeException>.Member).gen_run [gen_run] EXACT_MATCH\n" +
		"src/g4/v/ref/R4.java void g4.v.ref.R4.qualified_name(g1.t.s.def.Generic<Object>.Member, g1.t.s.def.Generic<Exception>.Member, g1.t.s.def.Generic<? extends Throwable>.Member, g1.t.s.def.Generic<? super RuntimeException>.Member) [gen_run] EXACT_MATCH",
		this.resultCollector);
}
public void testElementPatternLocalVariables08() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g4.v.ref", false);
	ILocalVariable localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R4.java", "gen_obj =", "gen_obj");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R4.java", "gen_exc =", "gen_exc");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R4.java", "gen_thr =", "gen_thr");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R4.java", "gen_run =", "gen_run");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	assertSearchResults(
		"src/g4/v/ref/R4.java void g4.v.ref.R4.simple_name().gen_obj [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R4.java void g4.v.ref.R4.simple_name() [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R4.java void g4.v.ref.R4.simple_name().gen_exc [gen_exc] EXACT_MATCH\n" +
		"src/g4/v/ref/R4.java void g4.v.ref.R4.simple_name() [gen_exc] EXACT_MATCH\n" +
		"src/g4/v/ref/R4.java void g4.v.ref.R4.simple_name().gen_thr [gen_thr] EXACT_MATCH\n" +
		"src/g4/v/ref/R4.java void g4.v.ref.R4.simple_name() [gen_thr] EXACT_MATCH\n" +
		"src/g4/v/ref/R4.java void g4.v.ref.R4.simple_name().gen_run [gen_run] EXACT_MATCH\n" +
		"src/g4/v/ref/R4.java void g4.v.ref.R4.simple_name() [gen_run] EXACT_MATCH",
		this.resultCollector);
}
public void testElementPatternLocalVariables09() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g4.v.ref", false);
	ILocalVariable localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R5.java", "gen_wld, // simple", "gen_wld");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R5.java", "gen_www, // simple", "gen_www");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R5.java", "gen_obj) // simple", "gen_obj");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	assertSearchResults(
		"src/g4/v/ref/R5.java void g4.v.ref.R5.simple_name(GM<GS<?>,GS<?>,GS<?>>, GM<GS<?>,GS<GS<?>>,GS<GS<GS<?>>>>, GM<GS<Object>,GS<? extends Throwable>,GS<? super RuntimeException>>).gen_wld [gen_wld] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.simple_name(GM<GS<?>,GS<?>,GS<?>>, GM<GS<?>,GS<GS<?>>,GS<GS<GS<?>>>>, GM<GS<Object>,GS<? extends Throwable>,GS<? super RuntimeException>>) [gen_wld] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.simple_name(GM<GS<?>,GS<?>,GS<?>>, GM<GS<?>,GS<GS<?>>,GS<GS<GS<?>>>>, GM<GS<Object>,GS<? extends Throwable>,GS<? super RuntimeException>>).gen_www [gen_www] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.simple_name(GM<GS<?>,GS<?>,GS<?>>, GM<GS<?>,GS<GS<?>>,GS<GS<GS<?>>>>, GM<GS<Object>,GS<? extends Throwable>,GS<? super RuntimeException>>) [gen_www] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.simple_name(GM<GS<?>,GS<?>,GS<?>>, GM<GS<?>,GS<GS<?>>,GS<GS<GS<?>>>>, GM<GS<Object>,GS<? extends Throwable>,GS<? super RuntimeException>>).gen_obj [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.simple_name(GM<GS<?>,GS<?>,GS<?>>, GM<GS<?>,GS<GS<?>>,GS<GS<GS<?>>>>, GM<GS<Object>,GS<? extends Throwable>,GS<? super RuntimeException>>) [gen_obj] EXACT_MATCH",
		this.resultCollector);
}
public void testElementPatternLocalVariables10() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g4.v.ref", false);
	ILocalVariable localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R5.java", "gen_wld = new GS", "gen_wld");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R5.java", "gen_www = new GS", "gen_www");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R5.java", "gen_obj = new GS", "gen_obj");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	assertSearchResults(
		"src/g4/v/ref/R5.java void g4.v.ref.R5.simple_name().gen_wld [gen_wld] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.simple_name() [gen_wld] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.simple_name().gen_www [gen_www] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.simple_name() [gen_www] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.simple_name().gen_obj [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.simple_name() [gen_obj] EXACT_MATCH",
		this.resultCollector);
}
public void testElementPatternLocalVariables11() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g4.v.ref", false);
	ILocalVariable localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R5.java", "gen_wld, // qualified", "gen_wld");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R5.java", "gen_www, // qualified", "gen_www");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R5.java", "gen_obj) // qualified", "gen_obj");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	assertSearchResults(
		"src/g4/v/ref/R5.java void g4.v.ref.R5.qualified_name(g3.t.def.GS<g3.t.def.GM<?,?,?>>, g3.t.def.GS<g3.t.def.GM<g3.t.def.GM<?,?,?>,g3.t.def.GM<?,?,?>,g3.t.def.GM<?,?,?>>>, g3.t.def.GS<g3.t.def.GM<java.lang.Object,java.lang.Exception,java.lang.RuntimeException>>).gen_wld [gen_wld] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.qualified_name(g3.t.def.GS<g3.t.def.GM<?,?,?>>, g3.t.def.GS<g3.t.def.GM<g3.t.def.GM<?,?,?>,g3.t.def.GM<?,?,?>,g3.t.def.GM<?,?,?>>>, g3.t.def.GS<g3.t.def.GM<java.lang.Object,java.lang.Exception,java.lang.RuntimeException>>) [gen_wld] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.qualified_name(g3.t.def.GS<g3.t.def.GM<?,?,?>>, g3.t.def.GS<g3.t.def.GM<g3.t.def.GM<?,?,?>,g3.t.def.GM<?,?,?>,g3.t.def.GM<?,?,?>>>, g3.t.def.GS<g3.t.def.GM<java.lang.Object,java.lang.Exception,java.lang.RuntimeException>>).gen_www [gen_www] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.qualified_name(g3.t.def.GS<g3.t.def.GM<?,?,?>>, g3.t.def.GS<g3.t.def.GM<g3.t.def.GM<?,?,?>,g3.t.def.GM<?,?,?>,g3.t.def.GM<?,?,?>>>, g3.t.def.GS<g3.t.def.GM<java.lang.Object,java.lang.Exception,java.lang.RuntimeException>>) [gen_www] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.qualified_name(g3.t.def.GS<g3.t.def.GM<?,?,?>>, g3.t.def.GS<g3.t.def.GM<g3.t.def.GM<?,?,?>,g3.t.def.GM<?,?,?>,g3.t.def.GM<?,?,?>>>, g3.t.def.GS<g3.t.def.GM<java.lang.Object,java.lang.Exception,java.lang.RuntimeException>>).gen_obj [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.qualified_name(g3.t.def.GS<g3.t.def.GM<?,?,?>>, g3.t.def.GS<g3.t.def.GM<g3.t.def.GM<?,?,?>,g3.t.def.GM<?,?,?>,g3.t.def.GM<?,?,?>>>, g3.t.def.GS<g3.t.def.GM<java.lang.Object,java.lang.Exception,java.lang.RuntimeException>>) [gen_obj] EXACT_MATCH",
		this.resultCollector);
}
public void testElementPatternLocalVariables12() throws CoreException {
	IJavaSearchScope scope = getJavaSearchScope15("g4.v.ref", false);
	ILocalVariable localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R5.java", "gen_wld = new GM", "gen_wld");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R5.java", "gen_www = new GM", "gen_www");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	localVar = getLocalVariable("/JavaSearch15/src/g4/v/ref/R5.java", "gen_obj = new GM", "gen_obj");
	search(localVar, ALL_OCCURRENCES, scope, this.resultCollector);
	assertSearchResults(
		"src/g4/v/ref/R5.java void g4.v.ref.R5.qualified_name().gen_wld [gen_wld] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.qualified_name() [gen_wld] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.qualified_name().gen_www [gen_www] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.qualified_name() [gen_www] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.qualified_name().gen_obj [gen_obj] EXACT_MATCH\n" +
		"src/g4/v/ref/R5.java void g4.v.ref.R5.qualified_name() [gen_obj] EXACT_MATCH",
		this.resultCollector);
}
}
