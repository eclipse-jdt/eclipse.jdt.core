/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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

public class Compliance_CLDC extends AbstractRegressionTest {

public Compliance_CLDC(String name) {
	super(name);
}

/*
 * Toggle compiler in mode -1.3
 */
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_3);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_CLDC1_1);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_3);
	return options;
}
public static Test suite() {
		return buildUniqueComplianceTestSuite(testClass(), COMPLIANCE_1_3);
}
public static Class testClass() {
	return Compliance_CLDC.class;
}
// Use this static initializer to specify subset for tests
// All specified tests which does not belong to the class are skipped...
static {
//		TESTS_NAMES = new String[] { "Bug58069" };
//		TESTS_NUMBERS = new int[] { 104 };
//		TESTS_RANGE = new int[] { 76, -1 };
}
public void test001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"import java.awt.Image;\n" + 
			"import java.awt.Toolkit;\n" + 
			"import java.awt.image.ImageProducer;\n" + 
			"import java.net.URL;\n" + 
			"\n" + 
			"public class X {\n" + 
			"\n" + 
			"	public Image loadImage(String name) {\n" + 
			"		Toolkit toolkit= Toolkit.getDefaultToolkit();\n" + 
			"		try {\n" + 
			"			URL url= X.class.getResource(name);\n" + 
			"			return toolkit.createImage((ImageProducer) url.getContent());\n" + 
			"		} catch (Exception ex) {\n" + 
			"		}\n" + 
			"		return null;\n" + 
			"	}\n" + 
			"	\n" + 
			"	public static void main(String[] args) {\n" + 
			"			System.out.println(\"OK\");\n" + 
			"	}\n" + 
			"}",
		},
		"OK");
}
}
