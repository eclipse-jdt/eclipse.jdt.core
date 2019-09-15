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
package org.eclipse.jdt.core.tests.model;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.IJavaProject;

@SuppressWarnings({"rawtypes", "unchecked"})
public class NamingConventionTests extends AbstractJavaModelTests {

IJavaProject project;
Hashtable oldOptions;

public NamingConventionTests(String name) {
	super(name);
}

public static Test suite() {
	return buildModelTestSuite(NamingConventionTests.class);
}
/**
 * Setup for the next test.
 */
@Override
public void setUp() throws Exception {
	super.setUp();
	this.project = createJavaProject("P", new String[]{"src"}, "bin"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	this.oldOptions = JavaCore.getOptions();
//	this.abortOnFailure = false; // some tests have failing one time on macos boxes => do not abort on failures
}
/**
 * Cleanup after the previous test.
 */
@Override
public void tearDown() throws Exception {
	JavaCore.setOptions(this.oldOptions);

	this.deleteProject("P"); //$NON-NLS-1$

	super.tearDown();
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testGetBaseName001() {
	String baseName = NamingConventions.getBaseName(
			NamingConventions.VK_INSTANCE_FIELD,
			"OneName", //$NON-NLS-1$
			this.project);

	assertEquals(
			"oneName", //$NON-NLS-1$
			baseName);
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testGetBaseName002() {
	String baseName = NamingConventions.getBaseName(
			NamingConventions.VK_STATIC_FINAL_FIELD,
			"ONE_NAME", //$NON-NLS-1$
			this.project);

	assertEquals(
			"oneName", //$NON-NLS-1$
			baseName);
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testGetBaseName003() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	String baseName = NamingConventions.getBaseName(
			NamingConventions.VK_INSTANCE_FIELD,
			"preOneNamesuf", //$NON-NLS-1$
			this.project);

	assertEquals(
			"oneName", //$NON-NLS-1$
			baseName);
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testGetBaseName004() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_STATIC_FINAL_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_STATIC_FINAL_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	String baseName = NamingConventions.getBaseName(
			NamingConventions.VK_STATIC_FINAL_FIELD,
			"preONE_NAMEsuf", //$NON-NLS-1$
			this.project);

	assertEquals(
			"oneName", //$NON-NLS-1$
			baseName);
}
public void testSuggestFieldName001() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"OneName", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"oneName\n" + //$NON-NLS-1$
		"name", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName002() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"OneClass", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"oneClass\n" + //$NON-NLS-1$
		"class1", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName003() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"f"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"OneName", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"fOneName\n" + //$NON-NLS-1$
		"fName\n" + //$NON-NLS-1$
		"oneName\n" + //$NON-NLS-1$
		"name", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName004() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"_"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"OneName", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"_oneName\n" + //$NON-NLS-1$
		"_name\n" + //$NON-NLS-1$
		"oneName\n" + //$NON-NLS-1$
		"name", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName005() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"f"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES,"fg"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_STATIC_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"OneName", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"fgOneName\n" + //$NON-NLS-1$
		"fgName\n" + //$NON-NLS-1$
		"oneName\n" + //$NON-NLS-1$
		"name", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName006() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"OneName", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"preOneNamesuf\n" + //$NON-NLS-1$
		"preNamesuf\n" + //$NON-NLS-1$
		"preOneName\n" + //$NON-NLS-1$
		"preName\n" + //$NON-NLS-1$
		"oneNamesuf\n" + //$NON-NLS-1$
		"namesuf\n" + //$NON-NLS-1$
		"oneName\n" + //$NON-NLS-1$
		"name", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName007() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"int", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"preIsuf\n" + //$NON-NLS-1$
		"preI\n" + //$NON-NLS-1$
		"isuf\n" + //$NON-NLS-1$
		"i", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName008() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"OneName", //$NON-NLS-1$
			this.project,
			0,
			new String[]{"name"}, //$NON-NLS-1$
			true);

	assumeEquals(
		"oneName\n" + //$NON-NLS-1$
		"name2", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName009() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"OneName", //$NON-NLS-1$
			this.project,
			0,
			new String[]{"preNamesuf"}, //$NON-NLS-1$
			true);

	assumeEquals(
		"preOneNamesuf\n" + //$NON-NLS-1$
		"preName2suf\n" + //$NON-NLS-1$
		"preOneName\n" + //$NON-NLS-1$
		"preName\n" + //$NON-NLS-1$
		"oneNamesuf\n" + //$NON-NLS-1$
		"namesuf\n" + //$NON-NLS-1$
		"oneName\n" + //$NON-NLS-1$
		"name", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName010() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"OneName", //$NON-NLS-1$
			this.project,
			1,
			new String[]{"preNamesuf"}, //$NON-NLS-1$
			true);

	assumeEquals(
		"preOneNamessuf\n" + //$NON-NLS-1$
		"preNamessuf\n" + //$NON-NLS-1$
		"preOneNames\n" + //$NON-NLS-1$
		"preNames\n" + //$NON-NLS-1$
		"oneNamessuf\n" + //$NON-NLS-1$
		"namessuf\n" + //$NON-NLS-1$
		"oneNames\n" + //$NON-NLS-1$
		"names", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName011() {

	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"Factory", //$NON-NLS-1$
			this.project,
			1,
			new String[]{},
			true);

	assumeEquals(
		"factories", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName012() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"FooBar", //$NON-NLS-1$
			this.project,
			0,
			new String[]{"bar"}, //$NON-NLS-1$
			true);

	assumeEquals(
		"fooBar\n" + //$NON-NLS-1$
		"bar2", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName013() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"Class", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"class1",//$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName014() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"Class", //$NON-NLS-1$
			this.project,
			0,
			new String[]{"class1"}, //$NON-NLS-1$
			true);

	assumeEquals(
		"class2",//$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName015() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"#", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"name",//$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName016() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"#", //$NON-NLS-1$
			this.project,
			0,
			new String[]{"name"}, //$NON-NLS-1$
			true);

	assumeEquals(
		"name2",//$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=35356
 */
public void testSuggestFieldName017() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"names", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"names",//$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=35356
 */
public void testSuggestFieldName018() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"names", //$NON-NLS-1$
			this.project,
			1,
			new String[]{},
			true);

	assumeEquals(
		"names",//$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=35356
 */
public void testSuggestFieldName019() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"MyClass", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"myClass\n" + //$NON-NLS-1$
		"class1", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=35356
 */
public void testSuggestFieldName020() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"MyClass", //$NON-NLS-1$
			this.project,
			1,
			new String[]{},
			true);

	assumeEquals(
		"myClasses\n" + //$NON-NLS-1$
		"classes", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestFieldName021() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_STATIC_FINAL_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"MyType", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"MY_TYPE\n" + //$NON-NLS-1$
		"TYPE", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestFieldName022() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_STATIC_FINAL_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_STATIC_FINAL_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_STATIC_FINAL_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"MyType", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"preMY_TYPEsuf\n" + //$NON-NLS-1$
		"preTYPEsuf\n" + //$NON-NLS-1$
		"preMY_TYPE\n" + //$NON-NLS-1$
		"preTYPE\n" + //$NON-NLS-1$
		"MY_TYPEsuf\n" + //$NON-NLS-1$
		"TYPEsuf\n" + //$NON-NLS-1$
		"MY_TYPE\n" + //$NON-NLS-1$
		"TYPE", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestFieldName023() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_NAME,
			"oneName", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"oneName", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestFieldName024() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_NAME,
			"oneName", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"preOneNamesuf\n" + //$NON-NLS-1$
		"preOneName\n" + //$NON-NLS-1$
		"oneNamesuf\n" + //$NON-NLS-1$
		"oneName", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestFieldName025() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_STATIC_FINAL_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"My_Type", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"MY_TYPE\n" + //$NON-NLS-1$
		"TYPE", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestFieldName026() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_STATIC_FINAL_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"_MyType", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"MY_TYPE\n" + //$NON-NLS-1$
		"TYPE", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestFieldName027() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_STATIC_FINAL_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"MyType_", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"MY_TYPE\n" + //$NON-NLS-1$
		"TYPE", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestFieldName028() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_STATIC_FINAL_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"MyTyp_e", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"MY_TYP_E\n" + //$NON-NLS-1$
		"TYP_E\n" + //$NON-NLS-1$
		"E", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestFieldName029() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"My1Type", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"my1Type\n" + //$NON-NLS-1$
		"type", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestFieldName030() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"M1yType", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"m1yType\n" + //$NON-NLS-1$
		"type", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestFieldName031() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"MY1Type", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"my1Type\n" + //$NON-NLS-1$
		"type", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestFieldName032() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"M1YType", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"m1yType\n" + //$NON-NLS-1$
		"type", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestFieldName033() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"My_First_Type", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
			"my_First_Type\n" +
			"first_Type\n" +
			"type", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestFieldName034() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"MY_FIRST_Type", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
			"my_FIRST_Type\n" +
			"first_Type\n" +
			"type", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestFieldName035() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"my_first_Type", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
			"my_first_Type\n" +
			"first_Type\n" +
			"type", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestFieldName036() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"MyFirst_9_Type", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
			"myFirst_9_Type\n" +
			"first_9_Type\n" +
			"type", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestFieldName037() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"AType", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"aType\n" + //$NON-NLS-1$
		"type", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestFieldName038() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"aType", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"aType\n" + //$NON-NLS-1$
		"type", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=255345
 */
public void testSuggestFieldName039() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_STATIC_FINAL_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"A", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"A", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=255345
 */
public void testSuggestFieldName040() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_STATIC_FINAL_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"int", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"INT", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=260840
 */
public void testSuggestFieldName041() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"Key", //$NON-NLS-1$
			this.project,
			1,
			new String[]{},
			true);

	assumeEquals(
		"keys", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=260840
 */
public void testSuggestFieldName042() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_STATIC_FINAL_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"Key", //$NON-NLS-1$
			this.project,
			1,
			new String[]{},
			true);

	assumeEquals(
		"KEYS", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=263786
 */
public void testSuggestFieldName043() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"TheURI", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"theURI\n" + //$NON-NLS-1$
		"uri", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=263786
 */
public void testSuggestFieldName044() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"URI", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"uri", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=263786
 */
public void testSuggestFieldName045() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_TYPE_NAME,
			"URIZork", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"uriZork\n" +  //$NON-NLS-1$
		"zork", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=263786
 */
public void testSuggestFieldName046() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_NAME,
			"TheURI", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"theURI", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=263786
 */
public void testSuggestFieldName047() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_NAME,
			"URI", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"uri", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=263786
 */
public void testSuggestFieldName048() {
	String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_INSTANCE_FIELD,
			NamingConventions.BK_NAME,
			"URIZork", //$NON-NLS-1$
			this.project,
			0,
			new String[]{},
			true);

	assumeEquals(
		"uriZork", //$NON-NLS-1$
		toString(suggestions));
}
/** @deprecated */
public void testRemovePrefixAndSuffixForFieldName001() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	char[] name = NamingConventions.removePrefixAndSuffixForFieldName(
		this.project,
		"preOneNamesuf".toCharArray(), //$NON-NLS-1$
		0);

	assumeEquals(
		"oneName", //$NON-NLS-1$
		new String(name));
}
/** @deprecated */
public void testRemovePrefixAndSuffixForFieldName002() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pr, pre"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"uf, suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	char[] name = NamingConventions.removePrefixAndSuffixForFieldName(
		this.project,
		"preOneNamesuf".toCharArray(), //$NON-NLS-1$
		Flags.AccStatic);

	assumeEquals(
		"preOneNamesuf", //$NON-NLS-1$
		new String(name));
}
/** @deprecated */
public void testRemovePrefixAndSuffixForFieldName003() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pr, pre"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"uf, suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	char[] name = NamingConventions.removePrefixAndSuffixForFieldName(
		this.project,
		"preOneNamesuf".toCharArray(), //$NON-NLS-1$
		0);

	assumeEquals(
		"oneName", //$NON-NLS-1$
		new String(name));
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=114086
/** @deprecated */
public void testRemovePrefixAndSuffixForFieldName004() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pre,"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	char[] name = NamingConventions.removePrefixAndSuffixForFieldName(
		this.project,
		"preOneName".toCharArray(), //$NON-NLS-1$
		0);

	assumeEquals(
		"oneName", //$NON-NLS-1$
		new String(name));
}
/** @deprecated */
public void testRemovePrefixAndSuffixForLocalName001() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_LOCAL_PREFIXES,"pr, pre"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_LOCAL_SUFFIXES,"uf, suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	char[] name = NamingConventions.removePrefixAndSuffixForLocalVariableName(
		this.project,
		"preOneNamesuf".toCharArray() //$NON-NLS-1$
		);

	assumeEquals(
		"oneName", //$NON-NLS-1$
		new String(name));
}
public void testSuggestGetterName001() {
	char[] suggestion = NamingConventions.suggestGetterName(
		this.project,
		"fieldName".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);

	assumeEquals(
		"getFieldName", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestGetterName002() {
	char[] suggestion = NamingConventions.suggestGetterName(
		this.project,
		"FieldName".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);

	assumeEquals(
		"getFieldName", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestGetterName003() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pr, pre"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"uf, suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	char[] suggestion = NamingConventions.suggestGetterName(
		this.project,
		"preFieldName".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);

	assumeEquals(
		"getFieldName", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestGetterName004() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pr, pre"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"uf, suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	char[] suggestion = NamingConventions.suggestGetterName(
		this.project,
		"preFieldNamesuf".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);

	assumeEquals(
		"getFieldName", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestGetterName005() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pr, pre"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"uf, suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	char[] suggestion = NamingConventions.suggestGetterName(
		this.project,
		"preFieldNamesuf".toCharArray(), //$NON-NLS-1$
		0,
		true,
		CharOperation.NO_CHAR_CHAR);

	assumeEquals(
		"isFieldName", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestGetterName006() {
	char[] suggestion = NamingConventions.suggestGetterName(
		this.project,
		"isSomething".toCharArray(), //$NON-NLS-1$
		0,
		true,
		CharOperation.NO_CHAR_CHAR);

	assumeEquals(
		"isSomething", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestGetterName007() {
	char[] suggestion = NamingConventions.suggestGetterName(
		this.project,
		"isSomething".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);

	assumeEquals(
		"getIsSomething", //$NON-NLS-1$
		new String(suggestion));
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=153125
public void testSuggestGetterName008() {
	char[] suggestion = NamingConventions.suggestGetterName(
		this.project,
		"éfield".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);

	assumeEquals(
		"getÉfield", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestGetterName009() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_STATIC_FINAL_FIELD_PREFIXES,"PRE_"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_STATIC_FINAL_FIELD_SUFFIXES,"_SUF"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	char[] suggestion = NamingConventions.suggestGetterName(
		this.project,
		"PRE_FIELD_NAME_SUF".toCharArray(), //$NON-NLS-1$
		Flags.AccStatic | Flags.AccFinal,
		false,
		CharOperation.NO_CHAR_CHAR);

	assumeEquals(
		"getFieldName", //$NON-NLS-1$
		new String(suggestion));
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=154823
public void testSuggestGetterName010() {
	char[] suggestion = NamingConventions.suggestGetterName(
		this.project,
		"eMail".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);

	assumeEquals(
		"geteMail", //$NON-NLS-1$
		new String(suggestion));
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=154823
public void testSuggestGetterName011() {
	char[] suggestion = NamingConventions.suggestGetterName(
		this.project,
		"EMail".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);

	assumeEquals(
		"getEMail", //$NON-NLS-1$
		new String(suggestion));
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=154823
public void testSuggestGetterName012() {
	char[] suggestion = NamingConventions.suggestGetterName(
		this.project,
		"z".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);

	assumeEquals(
		"getZ", //$NON-NLS-1$
		new String(suggestion));
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=154823
public void testSuggestGetterName013() {
	char[] suggestion = NamingConventions.suggestGetterName(
		this.project,
		"Z".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);

	assumeEquals(
		"getZ", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestSetterName001() {
	char[] suggestion = NamingConventions.suggestSetterName(
		this.project,
		"isSomething".toCharArray(), //$NON-NLS-1$
		0,
		true,
		CharOperation.NO_CHAR_CHAR);

	assumeEquals(
		"setSomething", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestSetterName002() {
	char[] suggestion = NamingConventions.suggestSetterName(
		this.project,
		"isSomething".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);

	assumeEquals(
		"setIsSomething", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestSetterName003() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_STATIC_FINAL_FIELD_PREFIXES,"PRE_"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_STATIC_FINAL_FIELD_SUFFIXES,"_SUF"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	char[] suggestion = NamingConventions.suggestSetterName(
		this.project,
		"PRE_FIELD_NAME_SUF".toCharArray(), //$NON-NLS-1$
		Flags.AccStatic | Flags.AccFinal,
		false,
		CharOperation.NO_CHAR_CHAR);

	assumeEquals(
		"setFieldName", //$NON-NLS-1$
		new String(suggestion));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=133562
 */
public void testSuggestLocalName001() {
	Map options = this.project.getOptions(true);
	try {
		Map newOptions = new HashMap(options);
		newOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
		newOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		newOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		this.project.setOptions(newOptions);

		String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_LOCAL,
			NamingConventions.BK_TYPE_NAME,
			"Enum",
			this.project,
			0,
			new String[]{"o"}, //$NON-NLS-1$
			true);

		assumeEquals(
			"enum1", //$NON-NLS-1$
			toString(suggestions));
	} finally {
		this.project.setOptions(options);
	}
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=133562
 */
public void testSuggestLocalName002() {
	Map options = this.project.getOptions(true);
	try {
		Map newOptions = new HashMap(options);
		newOptions.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);
		newOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		newOptions.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		this.project.setOptions(newOptions);

		String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_LOCAL,
			NamingConventions.BK_TYPE_NAME,
			"Enums",
			this.project,
			0,
			new String[]{"o"}, //$NON-NLS-1$
			true);

		assumeEquals(
			"enums", //$NON-NLS-1$
			toString(suggestions));
	} finally {
		this.project.setOptions(options);
	}
}
public void testSuggestConstantFieldName001() {
	String[] suggestions = NamingConventions.suggestVariableNames(
		NamingConventions.VK_STATIC_FINAL_FIELD,
		NamingConventions.BK_NAME,
		"__", //$NON-NLS-1$
		this.project,
		0,
		new String[]{},
		true);

	assumeEquals(
		"__", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=38111
 */
public void testSuggestConstantFieldName002() {
	Hashtable options = JavaCore.getOptions();
	options.put(JavaCore.CODEASSIST_STATIC_FINAL_FIELD_PREFIXES,"PRE"); //$NON-NLS-1$
	options.put(JavaCore.CODEASSIST_STATIC_FINAL_FIELD_SUFFIXES,"SUF"); //$NON-NLS-1$
	JavaCore.setOptions(options);

	String[] suggestions = NamingConventions.suggestVariableNames(
		NamingConventions.VK_STATIC_FINAL_FIELD,
		NamingConventions.BK_NAME,
		"__", //$NON-NLS-1$
		this.project,
		0,
		new String[]{},
		true);

	assumeEquals(
		"PRE__SUF\n" + //$NON-NLS-1$
		"PRE__\n" + //$NON-NLS-1$
		"__SUF\n" + //$NON-NLS-1$
		"__", //$NON-NLS-1$
		toString(suggestions));
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=283539
 * Test that suggestions for parameters include the underscores as supplied
 */
public void testSuggestParamWithUnderscore() {
	Map options = this.project.getOptions(true);
	try {
		Map newOptions = new HashMap(options);
		this.project.setOptions(newOptions);

		String[] suggestions = NamingConventions.suggestVariableNames(
			NamingConventions.VK_PARAMETER,
			NamingConventions.BK_TYPE_NAME,
			"lMin___Trigger__Period_usec",
			this.project,
			0,
			new String[]{}, //$NON-NLS-1$
			true);

		assumeEquals(
				"lMin___Trigger__Period_usec\n" +
				"min___Trigger__Period_usec\n" +
				"trigger__Period_usec\n" +
				"period_usec\n" +
				"usec", //$NON-NLS-1$
			toString(suggestions));
	} finally {
		this.project.setOptions(options);
	}
}
}
