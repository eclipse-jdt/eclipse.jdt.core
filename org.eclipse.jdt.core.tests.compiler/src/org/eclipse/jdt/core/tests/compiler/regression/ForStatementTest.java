/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class ForStatementTest extends AbstractRegressionTest {
	
public ForStatementTest(String name) {
	super(name);
}

protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_PreserveUnusedLocal, CompilerOptions.OPTIMIZE_OUT);
	return options;
}
// Static initializer to specify tests subset using TESTS_* static variables
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_NAMES = new String[] { "test000" };
//	TESTS_NUMBERS = new int[] { 45, 46 };
//	TESTS_RANGE = new int[] { 34, 38 };
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}
public void test001() {
	this.runConformTest(
		new String[] {
				"X.java",
				"public class X {\n" + 
				"	public static Object m(int[] arg) {\n" + 
				"		yyLoop: for (int i = 0;; ++i) {\n" + 
				"			yyInner: for (;;) {\n" + 
				"				switch (arg[i]) {\n" + 
				"					case 0:\n" + 
				"						break;\n" + 
				"					case 1:\n" + 
				"						continue yyInner;\n" + 
				"				}\n" + 
				"				if (i == 32)\n" + 
				"					return arg;\n" + 
				"				if (i == 12)\n" + 
				"					break;\n" + 
				"				continue yyLoop;\n" + 
				"			}\n" + 
				"			if (i == 32)\n" + 
				"				return null;\n" + 
				"			if (i > 7)\n" + 
				"				continue yyLoop;\n" + 
				"		}\n" + 
				"	}\n" + 
				"\n" + 
				"	public static void main(String[] args) {\n" + 
				"		System.out.println(\"SUCCESS\");\n" + 
				"	}\n" + 
				"}\n",
		},
		"SUCCESS");
}
public static Class testClass() {
	return ForStatementTest.class;
}
}
