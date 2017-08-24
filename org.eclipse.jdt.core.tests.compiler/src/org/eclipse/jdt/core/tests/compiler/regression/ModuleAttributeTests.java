/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation.
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

package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.Arrays;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IClassFileReader;
import org.eclipse.jdt.core.util.IModuleAttribute;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;

public class ModuleAttributeTests extends AbstractRegressionTest9 {

	public ModuleAttributeTests(String name) {
		super(name);
	}

	public static Class<?> testClass() {
		return ModuleAttributeTests.class;
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which does not belong to the class are skipped...
	static {
//		TESTS_PREFIX = "testBug95521";
//		TESTS_NAMES = new String[] { "testBug508889_003" };
//		TESTS_NUMBERS = new int[] { 53 };
//		TESTS_RANGE = new int[] { 23 -1,};
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
	}

	private IModuleAttribute getModuleAttribute(String[] contents) {
		this.runConformTest(contents);
		IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "module-info.class", IClassFileReader.CLASSFILE_ATTRIBUTES);
		assertNotNull("Error reading module-info.class", cfr);
		IClassFileAttribute attr = Arrays.stream(cfr.getAttributes())
			.filter(e -> new String(e.getAttributeName()).equals("Module"))
			.findFirst()
			.orElse(null);
		assertNotNull("Module attribute not found", attr);
		assertTrue("Not a module attribute", attr instanceof IModuleAttribute);
		return (IModuleAttribute) attr;
	}

	// basic test to check for presence of module attribute in module-info.class
	public void test001() throws Exception {
		this.runConformTest(
			new String[] {
				"module-info.java",
				"module test {\n" +
				"}\n",
				});
		IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "module-info.class", IClassFileReader.CLASSFILE_ATTRIBUTES);
		assertNotNull("Error reading module-info.class", cfr);
		IClassFileAttribute moduleAttribute = null;
		IClassFileAttribute[] attrs = cfr.getAttributes();
		for (int i=0,max=attrs.length;i<max;i++) {
			if (new String(attrs[i].getAttributeName()).equals("Module")) {
				moduleAttribute = attrs[i];
			}
		}
		assertNotNull("Module attribute not found", moduleAttribute);
	}
	// Test that there is at most one Module attribute in the attributes table of a ClassFile structure- JVMS Sec 4.7.25
	public void testBug508889_002() throws Exception {
		this.runConformTest(
			new String[] {
				"module-info.java",
				"module first {\n" +
				"}\n",
				});
		IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "module-info.class", IClassFileReader.CLASSFILE_ATTRIBUTES);
		assertNotNull("Error reading module-info.class", cfr);
		long count = Arrays.stream(cfr.getAttributes())
			.filter(e -> new String(e.getAttributeName()).equals("Module"))
			.count();
		assertEquals("Unexpected number of module attributes", 1,  count);
	}
	// Module Atrribute sanity
	public void _testBug508889_003() throws Exception {
		String[] contents = {
			"module-info.java",
			"module first {\n" +
				"exports pack1;\n" +
				"exports pack2 to zero;\n" +
			"}\n",
			"pack1/X11.java",
			"package pack1;\n" +
			"public class X11 {}\n",
			"pack2/X21.java",
			"package pack2;\n" +
			"public class X21 {}\n",	
		};
		IModuleAttribute module = getModuleAttribute(contents);
		assertEquals("Wrong Module Name", "first", new String(module.getModuleName()));
		assertTrue("Unexpected attribute length", module.getAttributeLength() > 0);
		//int flags = module.getModuleFlags();
	}
	public void testBug521521() throws Exception {
		this.runConformTest(
			new String[] {
				"module-info.java",
				"module test {\n" +
				"}\n",
				});
		IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "module-info.class", IClassFileReader.CLASSFILE_ATTRIBUTES);
		assertNotNull("Error reading module-info.class", cfr);
		int flags = cfr.getAccessFlags();
		assertTrue("Invalid access flags", (flags & ~ClassFileConstants.AccModule) == 0);
	}
	public void testBug521521a() throws Exception {
		this.runConformTest(
			new String[] {
				"module-info.java",
				"open module test {\n" +
				"}\n",
				});
		IClassFileReader cfr = ToolFactory.createDefaultClassFileReader(OUTPUT_DIR + File.separator + "module-info.class", IClassFileReader.CLASSFILE_ATTRIBUTES);
		assertNotNull("Error reading module-info.class", cfr);
		int flags = cfr.getAccessFlags();
		assertTrue("Invalid access flags", (flags & ~ClassFileConstants.AccModule) == 0);
	}

	public void testModuleCompile() throws Exception {
		String pack1_x11java = "pack1/X11.java";
		String pack2_x21java = "pack2/X21.java";
		associateToModule("first", pack1_x11java, pack2_x21java);
		String[] contents = {
			"module-info.java",
			"module first {\n" +
				"exports pack1;\n" +
				"exports pack2 to zero;\n" +
			"}\n",
			pack1_x11java,
			"package pack1;\n" +
			"public class X11 {}\n",
			pack2_x21java,
			"package pack2;\n" +
			"public class X21 {}\n",
		};
		this.runConformTest(contents);
	}
}
