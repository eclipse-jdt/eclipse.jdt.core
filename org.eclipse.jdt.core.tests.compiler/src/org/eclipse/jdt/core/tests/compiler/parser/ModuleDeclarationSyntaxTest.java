/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
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

import java.io.IOException;

import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class ModuleDeclarationSyntaxTest extends AbstractSyntaxTreeTest {

	public ModuleDeclarationSyntaxTest(String name, String referenceCompiler,
			String referenceCompilerTestsScratchArea) {
		super(name, referenceCompiler, referenceCompilerTestsScratchArea);
	}
	public static Class<?> testClass() {
		return ModuleDeclarationSyntaxTest.class;
	}
	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_9);
	}

	static {
		//		TESTS_NAMES = new String[] { "test0012" };
		//		TESTS_NUMBERS = new int[] { 133, 134, 135 };
	}
	public ModuleDeclarationSyntaxTest(String testName){
		super(testName, null, null);
	}
	public void test0001() throws IOException {
		String source = 
				"module com.greetings {\n" +
				"}\n";
		String expectedUnitToString = 
				"module com.greetings {\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK1_9;
		options.sourceLevel = ClassFileConstants.JDK1_9;
		options.targetJDK = ClassFileConstants.JDK1_9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0002() throws IOException {
		String source = 
				"module com.greetings {\n" +
				    "requires org.astro;" +
				"}\n";
		String expectedUnitToString = 
				"module com.greetings {\n" +
				"  requires org.astro;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK1_9;
		options.sourceLevel = ClassFileConstants.JDK1_9;
		options.targetJDK = ClassFileConstants.JDK1_9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0003() throws IOException {
		String source = 
				"module org.astro {\n" +
				"    exports org.astro;\n" +
				"}\n";
		String expectedUnitToString = 
				"module org.astro {\n" +
				"  exports org.astro;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK1_9;
		options.sourceLevel = ClassFileConstants.JDK1_9;
		options.targetJDK = ClassFileConstants.JDK1_9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0004() throws IOException {
		String source = 
				"module org.astro {\n" +
				"    exports org.astro to com.greetings, com.example1, com.example2;\n" +
				"}\n";
		String expectedUnitToString = 
				"module org.astro {\n" +
				"  exports org.astro to com.greetings, com.example1, com.example2;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK1_9;
		options.sourceLevel = ClassFileConstants.JDK1_9;
		options.targetJDK = ClassFileConstants.JDK1_9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0005() throws IOException {
		String source = 
				"module com.socket {\n" +
				"    exports com.socket;\n" +
				"    exports com.socket.spi;\n" +
				"    uses com.socket.spi.NetworkSocketProvider;\n" +
				"}\n";
		String expectedUnitToString = 
				"module com.socket {\n" +
				"  exports com.socket;\n" +
				"  exports com.socket.spi;\n" +
				"  uses com.socket.spi.NetworkSocketProvider;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK1_9;
		options.sourceLevel = ClassFileConstants.JDK1_9;
		options.targetJDK = ClassFileConstants.JDK1_9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0006() throws IOException {
		String source = 
				"module org.fastsocket {\n" +
				"    requires com.socket;\n" +
				"    provides com.socket.spi.NetworkSocketProvider\n" +
				"      with org.fastsocket.FastNetworkSocketProvider;\n" +
				"}\n";
		String expectedUnitToString = 
				"module org.fastsocket {\n" +
				"  requires com.socket;\n" +
				"  provides com.socket.spi.NetworkSocketProvider\n" +
				"    with org.fastsocket.FastNetworkSocketProvider;\n" +
				"}\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK1_9;
		options.sourceLevel = ClassFileConstants.JDK1_9;
		options.targetJDK = ClassFileConstants.JDK1_9;
		checkParse(CHECK_PARSER, source.toCharArray(), null, "module-info", expectedUnitToString, null, options);
	}
	public void test0007() throws IOException {
		String source = 
				"module org.fastsocket {\n" +
				"    requires com.socket;\n" +
				"    provides com.socket.spi.NetworkSocketProvider;\n" +
				"}\n";
		String expectedErrorString = 
				"----------\n" +
				"1. ERROR in module-info (at line 3)\n" +
				"	provides com.socket.spi.NetworkSocketProvider;\n" +
				"	                        ^^^^^^^^^^^^^^^^^^^^^\n" +
				"Syntax error on token \"NetworkSocketProvider\", WithClause expected after this token\n" +
				"----------\n";
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		options.complianceLevel = ClassFileConstants.JDK1_9;
		options.sourceLevel = ClassFileConstants.JDK1_9;
		options.targetJDK = ClassFileConstants.JDK1_9;
		checkParse(CHECK_PARSER, source.toCharArray(), expectedErrorString, "module-info", null, null, options);
	}
}
