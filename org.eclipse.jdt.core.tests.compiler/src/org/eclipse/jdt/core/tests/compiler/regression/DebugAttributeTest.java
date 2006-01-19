/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;
import java.io.File;
import java.io.IOException;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;

public class DebugAttributeTest extends AbstractRegressionTest {

	public DebugAttributeTest(String name) {
		super(name);
	}

	public static Test suite() {
		return setupSuite(testClass());
	}
	
	public static Class testClass() {
		return DebugAttributeTest.class;
	}

/**
 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=124212
 */
public void test001() {
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X {\n" + 
			"    public static void main(String[] args) {\n" + 
			"        String s;\n" + 
			"        if(args.length == 0) {\n" + 
			"          s = \"SUCCESS\";\n" + 
			"        } else {\n" + 
			"          return;\n" + 
			"        }\n" + 
			"        System.out.println(s);\n" + 
			"    }\n" + 
			"}",
		},
		"SUCCESS");
		
	ClassFileBytesDisassembler disassembler = ToolFactory.createDefaultClassFileBytesDisassembler();
	String actualOutput = null;
	try {
		byte[] classFileBytes = org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(new File(OUTPUT_DIR + File.separator  +"X.class"));
		actualOutput =
			disassembler.disassemble(
				classFileBytes,
				"\n",
				ClassFileBytesDisassembler.DETAILED); 
	} catch (org.eclipse.jdt.core.util.ClassFormatException e) {
		assertTrue("ClassFormatException", false);
	} catch (IOException e) {
		assertTrue("IOException", false);
	}
	
	String expectedOutput = 
		"      Local variable table:\n" + 
		"        [pc: 0, pc: 20] local: args index: 0 type: java.lang.String[]\n" + 
		"        [pc: 8, pc: 11] local: s index: 1 type: java.lang.String\n" + 
		"        [pc: 12, pc: 20] local: s index: 1 type: java.lang.String\n";

	int index = actualOutput.indexOf(expectedOutput);
	if (index == -1 || expectedOutput.length() == 0) {
		System.out.println(Util.displayString(actualOutput, 3));
	}
	if (index == -1) {
		assertEquals("unexpected bytecode sequence", expectedOutput, actualOutput);
	}
}
}
