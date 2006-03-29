/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

public class NumericTest extends AbstractRegressionTest {
	
public NumericTest(String name) {
	super(name);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(testClass());
}

public void test001() {
	this.runConformTest(new String[] {
		"p/B.java",
		"package p;\n" + 
		"public class B {\n" + 
		"  public static void main(String[] args) {\n" + 
		"    int offset = -8;\n" + 
		"    int temp = 0 - offset;\n" + 
		"    offset = 0 - offset;  // This is the problem line\n" + 
		"    System.out.println(\"offset: \" + offset);\n" + 
		"    System.out.println(\"temp: \" + temp);\n" + 
		"    if (offset != temp ) {\n" + 
		"      System.err.println(\"offset (\" + offset + \") should be equal to temp (\" + temp + \").\");\n" + 
		"      System.exit(-1);\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}

public void test002() {
	this.runConformTest(new String[] {
		"p/Y.java",
		"package p;\n" + 
		"public class Y {\n" + 
		"  public static void main(String[] args) {\n" + 
		"    int clockend = 0;\n" + 
		"    clockend += 128;\n" + 
		"    if(clockend < 0) {\n" + 
		"      System.out.println(clockend);\n" + 
		"      System.exit(-1);\n" + 
		"    }\n" + 
		"  }\n" + 
		"}\n",
	});
}
public static Class testClass() {
	return NumericTest.class;
}
}
