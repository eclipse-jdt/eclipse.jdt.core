/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class ASTRewritePropertyTest extends ASTRewritingTest {

	private static final Class THIS= ASTRewritePropertyTest.class;

	public ASTRewritePropertyTest(String name) {
		super(name);
	}
	public static Test allTests() {
		return new Suite(THIS);
	}

	public static Test setUpTest(Test someTest) {
		TestSuite suite= new Suite("one test");
		suite.addTest(someTest);
		return suite;
	}

	public static Test suite() {
		return buildModelTestSuite(THIS);
	}

	public void testProperties() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuffer buf= new StringBuffer();
		buf.append("package test1;\n");
		buf.append("public class C {}");
		ICompilationUnit cu= pack1.createCompilationUnit("C.java", buf.toString(), false, null);

		CompilationUnit astRoot= createAST(cu);
		ASTRewrite rewrite= ASTRewrite.create(astRoot.getAST());
		final String propertyName1 = "test.propertyName1";
		final String propertyName2 = "test.propertyName2";
		assertNull(rewrite.getProperty(propertyName1));
		try {
			rewrite.getProperty(null);
			assertTrue("Should not be reached", false);
		} catch(IllegalArgumentException e) {
			// ignore
		}
		rewrite.setProperty(propertyName1, "value");
		rewrite.setProperty(propertyName2, new Integer(1));
		try {
			rewrite.setProperty(null, "");
			assertTrue("Should not be reached", false);
		} catch(IllegalArgumentException e) {
			// ignore
		}
		Object value1 = rewrite.getProperty(propertyName1);
		assertTrue("Not a String", value1 instanceof String);
		assertTrue("Wrong value", "value".equals(value1));
		
		Object value2 = rewrite.getProperty(propertyName2);
		assertTrue("Not an Integer", value2 instanceof Integer);
		assertTrue("Wrong value", new Integer(1).equals(value2));

		rewrite.setProperty(propertyName1, null);
		value1 = rewrite.getProperty(propertyName1);
		assertNull("Not null", value1);

		rewrite.setProperty(propertyName2, null);
		value2 = rewrite.getProperty(propertyName2);
		assertNull("Not null", value2);
	}
}
