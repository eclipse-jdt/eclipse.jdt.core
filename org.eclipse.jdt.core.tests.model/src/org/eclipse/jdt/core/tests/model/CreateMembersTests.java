/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.ArrayList;
import java.util.StringTokenizer;

import junit.framework.Test;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;

public class CreateMembersTests extends AbstractJavaModelTests {

	IJavaProject project;

	public CreateMembersTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new Suite(CreateMembersTests.class);
	}
	public void setUpSuite() throws Exception {
		super.setUpSuite();

		setUpJavaProject("CreateMembers");
	}
	public void tearDownSuite() throws Exception {
		deleteProject("CreateMembers");

		super.tearDownSuite();
	}

	private void assertArraysEquals(String actualContents, String expectedContents) {
		String[] actualContentsArray = createArrayOfString(actualContents);
		String[] expectedContentsArray = createArrayOfString(expectedContents);
		assertTrue("Different size", actualContentsArray.length == expectedContentsArray.length); //$NON-NLS-1$
		for (int i = 0, max = expectedContentsArray.length; i < max; i++) {
			if (!expectedContentsArray[i].equals(actualContentsArray[i])) {
				assertTrue("Found: \n" + actualContents + "\nExpecting: \n" + expectedContents, false);
			}
		}
	}
	
	private String[] createArrayOfString(String s) {
		StringTokenizer tokenizer = new StringTokenizer(s, "\r\n"); //$NON-NLS-1$
		ArrayList arrayList = new ArrayList();
		while (tokenizer.hasMoreElements()) {
			String nextToken = tokenizer.nextToken();
			if (nextToken.length() != 0) {
				arrayList.add(nextToken);
			}
		}
		return (String[]) arrayList.toArray(new String[arrayList.size()]);
	}
	
	public void test001() throws JavaModelException {
		ICompilationUnit compilationUnit = getCompilationUnit("CreateMembers", "src", "", "A.java");
		assertNotNull("No compilation unit", compilationUnit);
		IType[] types = compilationUnit.getTypes();
		assertNotNull("No types", types);
		assertEquals("Wrong size", 1, types.length);
		IType type = types[0];
		type.createMethod("\tpublic void foo() {\n\t\tSystem.out.println(\"Hello World\");\n\t}\n", null, true, new NullProgressMonitor());
		String expectedSource = "public class A\n" +
			"{	public void foo() {\n" +
			"		System.out.println(\"Hello World\");\n" +
			"	}\n" +
			"}";
		assertArraysEquals(type.getSource(), expectedSource);
	}
}
