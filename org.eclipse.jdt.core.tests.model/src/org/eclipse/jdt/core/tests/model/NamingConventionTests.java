/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Hashtable;

import junit.framework.Test;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.NamingConventions;
import org.eclipse.jdt.core.compiler.CharOperation;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;

public class NamingConventionTests extends AbstractJavaModelTests {

IJavaProject project;

public NamingConventionTests(String name) {
	super(name);
}

public static Test suite() {
	return new Suite(NamingConventionTests.class);
}
/**
 * Setup for the next test.
 */
public void setUp() throws Exception {
	super.setUp();
	
	project = createJavaProject("P", new String[]{"src"}, "bin"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
}
/**
 * Cleanup after the previous test.
 */
public void tearDown() throws Exception {
	this.deleteProject("P"); //$NON-NLS-1$
	
	super.tearDown();
}
private String toString(char[][] suggestions) {
	if(suggestions == null) {
		return ""; //$NON-NLS-1$
	}
	
	StringBuffer buffer = new StringBuffer();
	for (int i = 0; i < suggestions.length; i++) {
		if(i != 0) {
			buffer.append('\n');
		}
		buffer.append(suggestions[i]);
	}
	return buffer.toString();
}
public void testSuggestFieldName001() throws CoreException {
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"OneName".toCharArray(), //$NON-NLS-1$
		0,
		0,
		CharOperation.NO_CHAR_CHAR);
	
	assertEquals(
		"name\n" + //$NON-NLS-1$
		"oneName", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName002() throws CoreException {
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"OneClass".toCharArray(), //$NON-NLS-1$
		0,
		0,
		CharOperation.NO_CHAR_CHAR);
	
	assertEquals(
		"oneClass", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName003() throws CoreException {
	Hashtable options = JavaCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"f"); //$NON-NLS-1$
	JavaCore.setOptions(options);
	
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"OneName".toCharArray(), //$NON-NLS-1$
		0,
		0,
		CharOperation.NO_CHAR_CHAR);
	
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	JavaCore.setOptions(options);
	assertEquals(
		"fName\n" + //$NON-NLS-1$
		"fOneName", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName004() throws CoreException {
	Hashtable options = JavaCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"_"); //$NON-NLS-1$
	JavaCore.setOptions(options);
	
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"OneName".toCharArray(), //$NON-NLS-1$
		0,
		0,
		CharOperation.NO_CHAR_CHAR);
	
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	JavaCore.setOptions(options);
	assertEquals(
		"_name\n" + //$NON-NLS-1$
		"_oneName", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName005() throws CoreException {
	Hashtable options = JavaCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"f"); //$NON-NLS-1$
	Object staticFieldPrefixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES,"fg"); //$NON-NLS-1$
	JavaCore.setOptions(options);
	
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"OneName".toCharArray(), //$NON-NLS-1$
		0,
		Flags.AccStatic,
		CharOperation.NO_CHAR_CHAR);
	
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES,staticFieldPrefixPreviousValue);
	JavaCore.setOptions(options);
	assertEquals(
		"fgName\n" + //$NON-NLS-1$
		"fgOneName", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName006() throws CoreException {
	Hashtable options = JavaCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);
	
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"OneName".toCharArray(), //$NON-NLS-1$
		0,
		0,
		CharOperation.NO_CHAR_CHAR);
	
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaCore.setOptions(options);
	assertEquals(
		"preNamesuf\n" + //$NON-NLS-1$
		"preOneNamesuf", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName007() throws CoreException {
	Hashtable options = JavaCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);
	
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"int".toCharArray(), //$NON-NLS-1$
		0,
		0,
		CharOperation.NO_CHAR_CHAR);
	
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaCore.setOptions(options);
	assertEquals(
		"preIsuf", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName008() throws CoreException {
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"OneName".toCharArray(), //$NON-NLS-1$
		0,
		0,
		new char[][]{"name".toCharArray()}); //$NON-NLS-1$
	
	assertEquals(
		"name2\n" + //$NON-NLS-1$
		"oneName", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName009() throws CoreException {
	Hashtable options = JavaCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);
	
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"OneName".toCharArray(), //$NON-NLS-1$
		0,
		0,
		new char[][]{"preNamesuf".toCharArray()}); //$NON-NLS-1$
	
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaCore.setOptions(options);
	assertEquals(
		"preName2suf\n" + //$NON-NLS-1$
		"preOneNamesuf", //$NON-NLS-1$
		toString(suggestions));
}
public void testSuggestFieldName010() throws CoreException {
	Hashtable options = JavaCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);
	
	char[][] suggestions = NamingConventions.suggestFieldNames(
		project,
		"a.b.c".toCharArray(), //$NON-NLS-1$
		"OneName".toCharArray(), //$NON-NLS-1$
		1,
		0,
		new char[][]{"preNamesuf".toCharArray()}); //$NON-NLS-1$
	
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaCore.setOptions(options);
	assertEquals(
		"preNamessuf\n" + //$NON-NLS-1$
		"preOneNamessuf", //$NON-NLS-1$
		toString(suggestions));
}
public void testRemovePrefixAndSuffixForFieldName001() throws CoreException {
	Hashtable options = JavaCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);
	
	char[] name = NamingConventions.removePrefixAndSuffixForFieldName(
		project,
		"preOneNamesuf".toCharArray(), //$NON-NLS-1$
		0);
	
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaCore.setOptions(options);
	assertEquals(
		"oneName", //$NON-NLS-1$
		new String(name));
}
public void testRemovePrefixAndSuffixForFieldName002() throws CoreException {
	Hashtable options = JavaCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pr, pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"uf, suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);
	
	char[] name = NamingConventions.removePrefixAndSuffixForFieldName(
		project,
		"preOneNamesuf".toCharArray(), //$NON-NLS-1$
		Flags.AccStatic);
	
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaCore.setOptions(options);
	assertEquals(
		"preOneNamesuf", //$NON-NLS-1$
		new String(name));
}
public void testRemovePrefixAndSuffixForFieldName003() throws CoreException {
	Hashtable options = JavaCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pr, pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"uf, suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);
	
	char[] name = NamingConventions.removePrefixAndSuffixForFieldName(
		project,
		"preOneNamesuf".toCharArray(), //$NON-NLS-1$
		0);
	
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaCore.setOptions(options);
	assertEquals(
		"oneName", //$NON-NLS-1$
		new String(name));
}
public void testSuggestGetterName001() throws CoreException {
	char[] suggestion = NamingConventions.suggestGetterName(
		project,
		"fieldName".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);
	
	assertEquals(
		"getFieldName", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestGetterName002() throws CoreException {
	char[] suggestion = NamingConventions.suggestGetterName(
		project,
		"FieldName".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);
	
	assertEquals(
		"getFieldName", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestGetterName003() throws CoreException {
	Hashtable options = JavaCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pr, pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"uf, suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);
	
	char[] suggestion = NamingConventions.suggestGetterName(
		project,
		"preFieldName".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);
	
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaCore.setOptions(options);
	assertEquals(
		"getFieldName", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestGetterName004() throws CoreException {
	Hashtable options = JavaCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pr, pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"uf, suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);
	
	char[] suggestion = NamingConventions.suggestGetterName(
		project,
		"preFieldNamesuf".toCharArray(), //$NON-NLS-1$
		0,
		false,
		CharOperation.NO_CHAR_CHAR);
	
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaCore.setOptions(options);
	assertEquals(
		"getFieldName", //$NON-NLS-1$
		new String(suggestion));
}
public void testSuggestGetterName005() throws CoreException {
	Hashtable options = JavaCore.getOptions();
	Object fieldPrefixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_PREFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,"pr, pre"); //$NON-NLS-1$
	Object fieldSuffixPreviousValue = options.get(JavaCore.CODEASSIST_FIELD_SUFFIXES);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,"uf, suf"); //$NON-NLS-1$
	JavaCore.setOptions(options);
	
	char[] suggestion = NamingConventions.suggestGetterName(
		project,
		"preFieldNamesuf".toCharArray(), //$NON-NLS-1$
		0,
		true,
		CharOperation.NO_CHAR_CHAR);
	
	options.put(JavaCore.CODEASSIST_FIELD_PREFIXES,fieldPrefixPreviousValue);
	options.put(JavaCore.CODEASSIST_FIELD_SUFFIXES,fieldSuffixPreviousValue);
	JavaCore.setOptions(options);
	assertEquals(
		"isFieldName", //$NON-NLS-1$
		new String(suggestion));
}
}
