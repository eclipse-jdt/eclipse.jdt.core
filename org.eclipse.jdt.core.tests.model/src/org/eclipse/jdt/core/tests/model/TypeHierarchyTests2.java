/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class TypeHierarchyTests2 extends AbstractJavaModelTests {
	/**
	 * A placeholder for a type hierarchy used in some test cases.
	 */
	ITypeHierarchy typeHierarchy;

public TypeHierarchyTests2(String name) {
	super(name);
}
public static Test suite() {
	if (false) {
		Suite suite = new Suite(TypeHierarchyTests2.class.getName());
//		suite.addTest(new TypeHierarchyTests("testGetAllSuperclassesFromBinary"));
		return suite;
	}
	return new Suite(TypeHierarchyTests2.class);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#setUpSuite()
 */
public void setUpSuite() throws Exception {
	super.setUpSuite();

	setUpJavaProject("TypeHierarchy2");
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.SuiteOfTestCases#tearDownSuite()
 */
public void tearDownSuite() throws Exception {
	this.typeHierarchy = null;
	deleteProject("TypeHierarchy2");
	
	super.tearDownSuite();
}
/**
 * Ensures that the correct superclasses of a binary type exist in the type  hierarchy.
 * (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=53095)
 */
public void testGetAllSuperclassesFromBinary() throws JavaModelException {
	String fileName = "TypeHierarchy2/lib53095/p53095/X53095.class";	//$NON-NLS-1$
	IJavaElement javaElement = JavaCore.create(getFile(fileName));
	assertNotNull("Problem to get class file \""+fileName+"\"", javaElement);
	assertTrue("Invalid type for class file \""+fileName+"\"", javaElement instanceof IClassFile);
	IType type = ((IClassFile) javaElement).getType();
	ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null); // it works when we use newTypeHierarchy(null)
	IType[] types = hierarchy.getAllSupertypes(type);
	assertTypesEqual(
		"Unexpected all super classes of X53095", 
		"java.lang.RuntimeException\n" +
		"java.lang.Exception\n" +
		"java.lang.Throwable\n" +
		"java.lang.Object\n",
		types,
		false);
}
/**
 * Ensures that the correct superclasses of a binary type exist in the type  hierarchy.
 * (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=53095)
 */
public void testGetAllSuperclassesFromBinary2() throws JavaModelException {
	IClassFile cf = getClassFile("TypeHierarchy2", "test54043.jar", "p54043", "X54043.class");
	IType type = cf.getType();
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	IType[] types = hierarchy.getAllSupertypes(type);
	assertTypesEqual(
		"Unexpected all super classes of X53095", 
		"java.lang.RuntimeException\n" +
		"java.lang.Exception\n" +
		"java.lang.Throwable\n" +
		"java.lang.Object\n",
		types,
		false);
}
}
