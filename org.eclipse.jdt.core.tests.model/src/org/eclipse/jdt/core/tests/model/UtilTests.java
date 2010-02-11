/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.internal.core.util.Util;

import junit.framework.Test;

public class UtilTests extends AbstractJavaModelTests {

	static {
//		TESTS_PREFIX = "testInvalidCompilerOptions";
//		TESTS_NAMES = new String[] { "test028"};
	}

	public UtilTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(UtilTests.class);
	}
	public void test001() {
		String[] arguments = Util.getProblemArgumentsFromMarker("1:foo");
		assertStringsEqual("Wrong arguments", new String[] {"foo"}, arguments);
	}
	public void test002() {
		String[] arguments = Util.getProblemArgumentsFromMarker("2:foo#bar");
		assertStringsEqual("Wrong arguments", new String[] {"foo", "bar"}, arguments);
	}
	public void test003() {
		String[] arguments = Util.getProblemArgumentsFromMarker("1:   ");
		assertStringsEqual("Wrong arguments", new String[] {""}, arguments);
	}
	public void test004() {
		String[] arguments = Util.getProblemArgumentsFromMarker("0:");
		assertStringsEqual("Wrong arguments", new String[0], arguments);
	}
	public void test005() {
		String[] arguments = Util.getProblemArgumentsFromMarker("3:Task<capture##1-of ?>#getTaskListeners#   ");
		assertStringsEqual("Wrong arguments", new String[] {"Task<capture#1-of ?>", "getTaskListeners", ""}, arguments);
	}
	public void test006() {
		String[] arguments = new String[] {"Task<capture#1-of ?>", "getTaskListeners", ""};
		String[] result = Util.getProblemArgumentsFromMarker(Util.getProblemArgumentsForMarker(arguments));
		assertStringsEqual("Wrong arguments", arguments, result);
	}
	public void test007() {
		assertNull("Not null", Util.getProblemArgumentsFromMarker("tt:Task<capture##1-of ?>#getTaskListeners#   "));
	}
	public void test008() {
		assertNull("Not null", Util.getProblemArgumentsFromMarker("3Task<capture##1-of ?>#getTaskListeners#   "));
	}
	public void test009() {
		assertNull("Not null", Util.getProblemArgumentsFromMarker(null));
	}
	public void test010() {
		assertNull("Not null", Util.getProblemArgumentsFromMarker("0:Task"));
	}
	public void test011() {
		String[] arguments = new String[] {"", "", ""};
		String[] result = Util.getProblemArgumentsFromMarker(Util.getProblemArgumentsForMarker(arguments));
		assertStringsEqual("Wrong arguments", arguments, result);
	}
	public void test012() {
		String[] arguments = new String[] {"foo#test", "bar"};
		String[] result = Util.getProblemArgumentsFromMarker(Util.getProblemArgumentsForMarker(arguments));
		assertStringsEqual("Wrong arguments", arguments, result);
	}
}