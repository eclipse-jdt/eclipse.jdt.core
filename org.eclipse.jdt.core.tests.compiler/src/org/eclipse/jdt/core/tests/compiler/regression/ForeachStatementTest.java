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
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class ForeachStatementTest extends AbstractRegressionTest {
public ForeachStatementTest(String name) {
	super(name);
}

/*
 * Toggle compiler in mode -1.5
 */
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);	
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);	
	return options;
}
public static Test suite() {
	return setupSuite(testClass());
}
public void test001() { 
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public static void main(String[] args) {\n" + 
			"        \n" + 
			"        for (char c : \"SUCCESS\".toCharArray()) {\n" + 
			"            System.out.print(c);\n" + 
			"        }\n" + 
			"        System.out.println();\n" + 
			"    }\n" + 
			"}\n"
		},
		"SUCCESS");
}

public static Class testClass() {
	return ForeachStatementTest.class;
}
}
