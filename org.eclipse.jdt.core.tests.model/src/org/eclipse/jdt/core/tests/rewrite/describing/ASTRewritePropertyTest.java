/*******************************************************************************
 * Copyright (c) 2013, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.rewrite.describing;


import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;

public class ASTRewritePropertyTest extends ASTRewritingTest {

	public ASTRewritePropertyTest(String name) {
		super(name);
	}
	public ASTRewritePropertyTest(String name, int apiLevel) {
		super(name, apiLevel);
	}

	public static Test suite() {
		return createSuite(ASTRewritePropertyTest.class);
	}

	public void testProperties() throws Exception {
		IPackageFragment pack1= this.sourceFolder.createPackageFragment("test1", false, null);
		StringBuilder buf= new StringBuilder();
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
		rewrite.setProperty(propertyName2, 1);
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
		assertTrue("Wrong value", Integer.valueOf(1).equals(value2));

		rewrite.setProperty(propertyName1, null);
		value1 = rewrite.getProperty(propertyName1);
		assertNull("Not null", value1);

		rewrite.setProperty(propertyName2, null);
		value2 = rewrite.getProperty(propertyName2);
		assertNull("Not null", value2);
	}
}
