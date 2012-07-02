/*******************************************************************************
 * Copyright (c) 2009, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;

import org.eclipse.jdt.core.tests.util.CompilerTestSetup;

public class LambdaExpressionSyntaxTest extends AbstractSyntaxTreeTest {

	private static String  jsr335TestScratchArea = "c:\\Jsr335TestScratchArea";
	private static String referenceCompiler = "C:\\jdk-7-ea-bin-b75-windows-i586-30_oct_2009\\jdk7\\bin\\javac.exe";

	public static Class testClass() {
		return LambdaExpressionSyntaxTest.class;
	}
	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_8);
	}

	public LambdaExpressionSyntaxTest(String testName){
		super(testName, referenceCompiler, jsr335TestScratchArea);
		if (referenceCompiler != null) {
			File f = new File(jsr335TestScratchArea);
			if (!f.exists()) {
				f.mkdir();
			}
			CHECK_ALL |= CHECK_JAVAC_PARSER;
		}
	}

	static {
		//		TESTS_NAMES = new String[] { "test0038", "test0039", "test0040a" };
		//		TESTS_NUMBERS = new int[] { 133, 134, 135 };
		if (!(new File(referenceCompiler).exists())) {
			referenceCompiler = null;
			jsr335TestScratchArea = null;
		}
	}

	public void test0001() throws IOException {
		String source = 
				"interface I {\n" +
				"    int square(int x);\n" +
				"}\n" +
				"public class X {\n" +
				"    public static void main(String [] args) {\n" +
				"        System.out.println(((I) x -> x * x).square(10));\n" +
				"    }\n" +
				"}\n";

		String expectedUnitToString = 
				"interface I {\n" + 
				"  int square(int x);\n" + 
				"}\n" + 
				"public class X {\n" + 
				"  public X() {\n" + 
				"    super();\n" + 
				"  }\n" + 
				"  public static void main(String[] args) {\n" + 
				"    System.out.println((I) (<no type> x) -> (x * x).square(10));\n" + 
				"  }\n" + 
				"}\n";
		checkParse(CHECK_ALL & ~CHECK_JAVAC_PARSER , source.toCharArray(), null, "test0001", expectedUnitToString);
	}
		
}
