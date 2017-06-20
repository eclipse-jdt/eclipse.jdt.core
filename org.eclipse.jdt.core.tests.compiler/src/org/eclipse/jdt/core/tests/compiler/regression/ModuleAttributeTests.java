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

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.IClassFileAttribute;
import org.eclipse.jdt.core.util.IClassFileReader;

import junit.framework.Test;

public class ModuleAttributeTests extends AbstractRegressionTest {

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
//		TESTS_NAMES = new String[] { "testBug359495" };
//		TESTS_NUMBERS = new int[] { 53 };
//		TESTS_RANGE = new int[] { 23 -1,};
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_9);
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
}
