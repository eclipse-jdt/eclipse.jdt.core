/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.hierarchy.RegionBasedTypeHierarchy;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;

import junit.framework.Test;

public class TypeHierarchySerializationTests extends AbstractJavaModelTests {
	private static final String PROJECTNAME = "TypeHierarchySerialization";

	IJavaProject project;

public TypeHierarchySerializationTests(String name) {
	super(name);
}
public static Test suite() {
	return buildModelTestSuite(TypeHierarchySerializationTests.class);
}
private static void compare(String focus, ITypeHierarchy stored, ITypeHierarchy loaded){
	if(stored instanceof RegionBasedTypeHierarchy) {
		assertTrue("["+focus+"] hierarchies are not the same", loaded instanceof RegionBasedTypeHierarchy);
		compareRegionBasedTypeHierarchy(focus, (RegionBasedTypeHierarchy)stored,(RegionBasedTypeHierarchy)loaded);
	} else if(stored instanceof TypeHierarchy) {
		assertTrue("["+focus+"] hierarchies are not the same", loaded instanceof TypeHierarchy);
		compareTypeHierarchy(focus, (TypeHierarchy)stored,(TypeHierarchy)loaded);
	}
}
private static void compareRegionBasedTypeHierarchy(String focus, RegionBasedTypeHierarchy stored, RegionBasedTypeHierarchy loaded){
	compareTypeHierarchy(focus, stored, loaded);
}
private static void compareTypeHierarchy(String focus, TypeHierarchy stored, TypeHierarchy loaded){
	//System.out.println(stored.toString());

	IType type1 = stored.getType();
	IType type2 = loaded.getType();
	assertEquals("["+focus+"] focus are not the same", type1, type2);

	IType[] allTypes1 = stored.getAllTypes();
	IType[] allTypes2 = loaded.getAllTypes();
	compare("["+focus+"] all types are not the same", allTypes1, allTypes2);

	IType[] allClasses1 = stored.getAllClasses();
	IType[] allClasses2 = loaded.getAllClasses();
	compare("["+focus+"] all classes are not the same", allClasses1, allClasses2);

	IType[] allInterfaces1 = stored.getAllInterfaces();
	IType[] allInterfaces2 = loaded.getAllInterfaces();
	compare("["+focus+"] all interfaces are not the same", allInterfaces1, allInterfaces2);

	IType[] rootClasses1 = stored.getRootClasses();
	IType[] rootClasses2 = loaded.getRootClasses();
	compare("["+focus+"] all roots are not the same", rootClasses1, rootClasses2);

	IType[] rootInterfaces1 = stored.getRootInterfaces();
	IType[] rootInterfaces2 = loaded.getRootInterfaces();
	compare("["+focus+"] all roots are not the same", rootInterfaces1, rootInterfaces2);

	Object[] missingTypes1 = stored.missingTypes.toArray();
	Object[] missingTypes2 = loaded.missingTypes.toArray();
	compare("["+focus+"] all missing types are not the same", missingTypes1, missingTypes2);

	for (int i = 0; i < allTypes1.length; i++) {
		IType aType = allTypes1[i];

		int cachedFlags1 = stored.getCachedFlags(aType);
		int cachedFlags2 = loaded.getCachedFlags(aType);
		assertEquals("["+focus+"] flags are not the same for "+aType.getFullyQualifiedName(), cachedFlags1, cachedFlags2);

		IType superclass1 = stored.getSuperclass(aType);
		IType superclass2 = loaded.getSuperclass(aType);
		assertEquals("["+focus+"] superclass are not the same for "+aType.getFullyQualifiedName(), superclass1, superclass2);

		IType[] superInterfaces1 = stored.getSuperInterfaces(aType);
		IType[] superInterfaces2 = loaded.getSuperInterfaces(aType);
		compare("["+focus+"] all super interfaces are not the same for "+aType.getFullyQualifiedName(), superInterfaces1, superInterfaces2);

		IType[] superTypes1 = stored.getSupertypes(aType);
		IType[] superTypes2 = loaded.getSupertypes(aType);
		compare("["+focus+"] all super types are not the same for "+aType.getFullyQualifiedName(), superTypes1, superTypes2);

		IType[] subclasses1 = stored.getSubclasses(aType);
		IType[] subclasses2 = loaded.getSubclasses(aType);
		compare("["+focus+"] all subclasses are not the same for "+aType.getFullyQualifiedName(), subclasses1, subclasses2);

		IType[] subtypes1 = stored.getSubtypes(aType);
		IType[] subtypes2 = loaded.getSubtypes(aType);
		compare("["+focus+"] all subtypes are not the same for "+aType.getFullyQualifiedName(), subtypes1, subtypes2);

		IType[] extendingInterfaces1 = stored.getExtendingInterfaces(aType);
		IType[] extendingInterfaces2 = loaded.getExtendingInterfaces(aType);
		compare("["+focus+"] all extending interfaces are not the same for "+aType.getFullyQualifiedName(), extendingInterfaces1, extendingInterfaces2);

		IType[] implementingClasses1 = stored.getImplementingClasses(aType);
		IType[] implementingClasses2 = loaded.getImplementingClasses(aType);
		compare("["+focus+"] all implemeting classes are not the same for "+aType.getFullyQualifiedName(), implementingClasses1, implementingClasses2);

	}
}
private static void compare(String msg, Object[] types1, Object[] types2) {
	if(types1 == null) {
		assertTrue(msg, types2 == null);
	} else {
		assertTrue(msg, types2 != null);
		assertTrue(msg, types1.length == types2.length);
		for (int i = 0; i < types1.length; i++) {
			boolean found = false;
			for (int j = 0; j < types2.length; j++) {
				if(types1[i] == null && types1[j] == null) {
					found = true;
				} else if(types1[i] != null && types1[i].equals(types2[j])) {
					found = true;
				}
			}
			assertTrue(msg, found);
		}
	}
}
public void setUpSuite() throws Exception {
	super.setUpSuite();

	this.project = setUpJavaProject(PROJECTNAME);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.SuiteOfTestCases#tearDownSuite()
 */
public void tearDownSuite() throws Exception {
	deleteProject(PROJECTNAME);

	super.tearDownSuite();
}
private static void testFocusHierarchy(IType type, IJavaProject project) throws JavaModelException{
	ITypeHierarchy h1 = type.newTypeHierarchy(project, null);

	ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	h1.store(outputStream, null);

	byte[] bytes = outputStream.toByteArray();
	ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
	ITypeHierarchy h2 = type.loadTypeHierachy(inputStream, null);

	compare(type.getFullyQualifiedName(), h1, h2);

	h2.refresh(null);
	compare(type.getFullyQualifiedName(), h1, h2);
}
public void test001() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit(PROJECTNAME, "src", "p1", "X.java");
	IType type = cu.getType("X");
	testFocusHierarchy(type, this.project);
}
public void test002() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit(PROJECTNAME, "src", "p1", "Y.java");
	IType type = cu.getType("Y");
	testFocusHierarchy(type, this.project);
}
public void test003() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit(PROJECTNAME, "src", "p1", "Z.java");
	IType type = cu.getType("Z");
	testFocusHierarchy(type, this.project);
}
public void test004() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit(PROJECTNAME, "src", "p1", "I1.java");
	IType type = cu.getType("I1");
	testFocusHierarchy(type, this.project);
}
public void test005() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit(PROJECTNAME, "src", "p1", "I2.java");
	IType type = cu.getType("I2");
	testFocusHierarchy(type, this.project);
}
public void test006() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit(PROJECTNAME, "src", "p1", "I3.java");
	IType type = cu.getType("I3");
	testFocusHierarchy(type, this.project);
}
public void test007() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit(PROJECTNAME, "src", "p1", "I4.java");
	IType type = cu.getType("I4");
	testFocusHierarchy(type, this.project);
}
public void test008() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit(PROJECTNAME, "src", "p1", "I5.java");
	IType type = cu.getType("I5");
	testFocusHierarchy(type, this.project);
}
public void test009() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit(PROJECTNAME, "src", "p1", "I6.java");
	IType type = cu.getType("I6");
	testFocusHierarchy(type, this.project);
}
}
