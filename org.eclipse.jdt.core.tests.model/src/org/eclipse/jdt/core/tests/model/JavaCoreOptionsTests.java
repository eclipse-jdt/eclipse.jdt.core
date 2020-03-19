/*******************************************************************************
 * Copyright (c) 2010, 2016 IBM Corporation and others.
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

import java.util.Hashtable;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CharOperation;
/**
 * Tests JavaCore options
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class JavaCoreOptionsTests extends ModifyingResourceTests {

public static Test suite() {
	return buildModelTestSuite(JavaCoreOptionsTests.class);
}

// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_NUMBERS = new int[] { 4 };
}

public JavaCoreOptionsTests(String name) {
	super(name);
}
public void test1() {
	Hashtable options = JavaCore.getOptions();
	try {
		Hashtable currentOptions = new Hashtable(options);
		currentOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "HIGH,HIGH");
		currentOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO");
		JavaCore.setOptions(currentOptions);

		Hashtable options2 = JavaCore.getOptions();
		String taskTagsValue = (String) options2.get(JavaCore.COMPILER_TASK_TAGS);
		String taskPrioritiesValue = (String) options2.get(JavaCore.COMPILER_TASK_PRIORITIES);
		char[][] taskPriorities = CharOperation.splitAndTrimOn(',', taskPrioritiesValue.toCharArray());
		char[][] taskTags = CharOperation.splitAndTrimOn(',', taskTagsValue.toCharArray());
		assertEquals("wrong size", 1, taskPriorities.length);
		assertEquals("wrong size", 1, taskTags.length);
	} finally {
		JavaCore.setOptions(options);
	}
}
public void test2() {
	Hashtable options = JavaCore.getOptions();
	try {
		Hashtable currentOptions = new Hashtable(options);
		currentOptions.remove(JavaCore.COMPILER_TASK_PRIORITIES);
		currentOptions.remove(JavaCore.COMPILER_TASK_TAGS);
		JavaCore.setOptions(currentOptions);

		Hashtable options2 = JavaCore.getOptions();
		String taskTagsValue = (String) options2.get(JavaCore.COMPILER_TASK_TAGS);
		String taskPrioritiesValue = (String) options2.get(JavaCore.COMPILER_TASK_PRIORITIES);
		assertNull("wrong value", taskTagsValue);
		assertNull("wrong value", taskPrioritiesValue);
	} finally {
		JavaCore.setOptions(options);
	}
}
public void test3() {
	Hashtable options = JavaCore.getOptions();
	try {
		Hashtable currentOptions = new Hashtable(options);
		currentOptions.put(JavaCore.COMPILER_TASK_PRIORITIES, "HIGH,HIGH");
		currentOptions.remove(JavaCore.COMPILER_TASK_TAGS);
		JavaCore.setOptions(currentOptions);

		Hashtable options2 = JavaCore.getOptions();
		String taskTagsValue = (String) options2.get(JavaCore.COMPILER_TASK_TAGS);
		String taskPrioritiesValue = (String) options2.get(JavaCore.COMPILER_TASK_PRIORITIES);
		assertNull("wrong value", taskTagsValue);
		assertNull("wrong value", taskPrioritiesValue);
	} finally {
		JavaCore.setOptions(options);
	}
}
public void test4() {
	Hashtable options = JavaCore.getOptions();
	try {
		Hashtable currentOptions = new Hashtable(options);
		currentOptions.remove(JavaCore.COMPILER_TASK_PRIORITIES);
		currentOptions.put(JavaCore.COMPILER_TASK_TAGS, "TODO");
		JavaCore.setOptions(currentOptions);

		Hashtable options2 = JavaCore.getOptions();
		String taskTagsValue = (String) options2.get(JavaCore.COMPILER_TASK_TAGS);
		String taskPrioritiesValue = (String) options2.get(JavaCore.COMPILER_TASK_PRIORITIES);
		assertNull("wrong value", taskTagsValue);
		assertNull("wrong value", taskPrioritiesValue);
	} finally {
		JavaCore.setOptions(options);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=464845
public void test5() {
	assertTrue(JavaCore.compareJavaVersions("1.1", "1.3") < 0);
	assertTrue(JavaCore.compareJavaVersions("1.4", "1.1") > 0);
	assertTrue(JavaCore.compareJavaVersions("1.8", "cldc1.1") > 0);
	assertTrue(JavaCore.compareJavaVersions("cldc1.1", "1.1") > 0);
	assertTrue(JavaCore.compareJavaVersions("1.1", "cldc1.1") < 0);
	assertTrue(JavaCore.compareJavaVersions("1.8", "1.8") == 0);
	assertTrue(JavaCore.compareJavaVersions("1.8", "9") < 0);
	assertTrue(JavaCore.compareJavaVersions("9", "1.8") > 0);
	assertTrue(JavaCore.compareJavaVersions("9.0.1", "9.1.2") == 0);
	assertTrue(JavaCore.compareJavaVersions("9", "9.1.2") == 0);
	assertTrue(JavaCore.compareJavaVersions("12", "11") > 0);
	assertTrue(JavaCore.compareJavaVersions("12", "1.5") > 0);
	String latest = JavaCore.latestSupportedJavaVersion();
	String latestPlus = "" + (Integer.parseInt(latest) + 1);
	assertTrue(JavaCore.compareJavaVersions(latest, latestPlus) == 0);
}
}

