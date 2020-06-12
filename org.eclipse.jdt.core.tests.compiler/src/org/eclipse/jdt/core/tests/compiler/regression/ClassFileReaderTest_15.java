/*******************************************************************************
 * Copyright (c) 2013, 2019 GoPivotal, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *		Andy Clement (GoPivotal, Inc) aclement@gopivotal.com - Contributions for
 *			Bug 407191 - [1.8] Binary access support for type annotations
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

@SuppressWarnings({ "rawtypes" })
public class ClassFileReaderTest_15 extends AbstractRegressionTest {
	static {
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_15);
	}
	public static Class testClass() {
		return ClassFileReaderTest_15.class;
	}

	public ClassFileReaderTest_15(String name) {
		super(name);
	}

	// Needed to run tests individually from JUnit
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.complianceLevel = ClassFileConstants.JDK15;
		this.enablePreview = true;
	}

	public void testBug564227_001() throws Exception {
		String source =
				"sealed class X permits Y, Z{\n" +
				"  public static void main(String[] args){\n"+
				"     System.out.println(0);\n" +
				"  }\n"+
				"}\n" +
				"final class Y extends X{}\n" +
				"final class Z extends X{}\n";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		char[][] permittedSubtypesNames = classFileReader.getPermittedSubtypeNames();

		assertEquals(2, permittedSubtypesNames.length);

		char [][] expected = {"Y".toCharArray(), "Z".toCharArray()};
		assertTrue(CharOperation.equals(permittedSubtypesNames, expected));

	}
}
