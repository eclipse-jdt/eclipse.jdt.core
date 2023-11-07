/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
 *     Stephan Herrmann - contribution for Bug 300576 - NPE Computing type hierarchy when compliance doesn't match libraries
 *     Jesper S Moller - contributions for bug 393192 - Incomplete type hierarchy with > 10 annotations
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import static org.junit.Assert.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IOrdinaryClassFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.model.SearchTests.WaitingJob;
import org.eclipse.jdt.core.tests.model.Semaphore.TimeOutException;
import org.eclipse.jdt.core.tests.util.Util;

@SuppressWarnings("rawtypes")
public class TypeHierarchyTests extends ModifyingResourceTests {
	/**
	 * A placeholder for a type hierarchy used in some test cases.
	 */
	ITypeHierarchy typeHierarchy;

static {
//	TESTS_NAMES= new String[] { "testBug573450" };
}
public static Test suite() {
	return buildModelTestSuite(TypeHierarchyTests.class, BYTECODE_DECLARATION_ORDER);
}
public TypeHierarchyTests(String name) {
	super(name);
	this.displayName = true;
}

@Override
protected void setUp() throws Exception {
	this.indexDisabledForTest = false;
	super.setUp();
}

@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();

	setUpJavaProject("TypeHierarchy");
	addLibrary("myLib.jar", "myLibsrc.zip", new String[] {
		"my/pkg/X.java",
		"package my.pkg;\n" +
		"public class X {\n" +
		"}",
		"my/pkg/Y.java",
		"package my.pkg;\n" +
		"public class Y {\n" +
		"  void foo() {\n" +
		"    new X() {};" +
		"  }\n" +
		"}",
	}, JavaCore.VERSION_1_4);

	IPackageFragmentRoot root = this.currentProject.getPackageFragmentRoot(this.currentProject.getProject().getFile("lib.jar"));
	IRegion region = JavaCore.newRegion();
	region.add(root);
	this.typeHierarchy = this.currentProject.newTypeHierarchy(region, null);

	IJavaProject project15 = createJavaProject("TypeHierarchy15", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
	addLibrary(project15, "lib15.jar", "lib15src.zip", new String[] {
		"util/AbstractList.java",
		"package util;\n" +
		"public class AbstractList<E> {\n" +
		"}",
		"util/ArrayList.java",
		"package util;\n" +
		"public class ArrayList<E> extends AbstractList<E> implements List<E> {\n" +
		"}",
		"util/List.java",
		"package util;\n" +
		"public interface List<E> {\n" +
		"}",
		"util/Map.java",
		"package util;\n" +
		"public class Map<K,V> extends AbstractList<V> {\n" +
		"}",
	}, JavaCore.VERSION_1_5);
	createFile(
		"/TypeHierarchy15/src/X.java",
		"import util.*;\n" +
		"public class X<E> extends ArrayList<E> implements List<E> {\n" +
		"}"
	);
	createFile(
		"/TypeHierarchy15/src/Y.java",
		"import util.*;\n" +
		"public class Y extends ArrayList implements List {\n" +
		"}"
	);
	createFile(
		"/TypeHierarchy15/src/I.java",
		"public interface I<E> {\n" +
		"}"
	);
	createFile(
		"/TypeHierarchy15/src/A.java",
		"public class A<E> implements I<E> {\n" +
		"}"
	);
	createFile(
		"/TypeHierarchy15/src/X99606.java",
		"public class X99606 extends Y99606<X99606.Color> {\n" +
		"	static class Color {}\n" +
		"}"
	);
	createFile(
		"/TypeHierarchy15/src/Y99606.java",
		"public class Y99606<T> {\n" +
		"}"
	);
	createFile(
		"/TypeHierarchy15/src/A108740.java",
		"class A108740<T> {}"
	);
	createFile(
		"/TypeHierarchy15/src/B108740.java",
		"class B108740<T> extends A108740<C108740> {}"
	);
	createFile(
		"/TypeHierarchy15/src/C108740.java",
		"class C108740 extends B108740<C108740> {}"
	);
	createFile(
		"/TypeHierarchy15/src/D108740.java",
		"class D108740 extends B108740<D108740> {}"
	);
	createFile(
		"/TypeHierarchy15/src/CycleParent.java",
		"class CycleParent extends CycleBase<CycleChild> {}"
	);
	createFile(
		"/TypeHierarchy15/src/CycleBase.java",
		"class CycleBase<T extends CycleBase> {}"
	);
	createFile(
		"/TypeHierarchy15/src/CycleChild.java",
		"class CycleChild extends CycleParent implements Comparable<CycleChild> {\n" +
		"	public int compareTo(CycleChild o) { return 0; }\n" +
		"}"
	);
	createFile(
		"/TypeHierarchy15/src/Try.java",
		"public enum Try {\n" +
		"    THIS,\n" +
		"    THAT(),\n" +
		"    ANONYMOUS() {}\n" +
		"}"
	);

	IJavaProject project_16 = createJava16Project("TypeHierarchy_16", new String[] {"src"});
	project_16.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	createFolder("/TypeHierarchy_16/src/pkg");
	createFile(
			"/TypeHierarchy_16/src/pkg/Rec1.java",
			"public record Rec1 (int one) {\n" +
			"   " +
			"}"
		);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.SuiteOfTestCases#tearDownSuite()
 */
@Override
public void tearDownSuite() throws Exception {
	this.typeHierarchy = null;
	deleteProject("TypeHierarchy");
	deleteProject("TypeHierarchy15");
	deleteProject("TypeHierarchy_16");

	super.tearDownSuite();
}
/*
 * Ensures that a hierarchy on an anonymous type in an initializer is correct.
 */
public void testAnonymousType01() throws JavaModelException {
	IType typeA = getCompilationUnit("TypeHierarchy", "src", "p7", "A.java").getType("A");
	IType type = typeA.getInitializer(1).getType("", 1);
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: <anonymous #1> [in <initializer #1> [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"Super types:\n" +
		"  X [in X.java [in p7 [in src [in TypeHierarchy]]]]\n" +
		"    Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n",
		hierarchy);
}
/*
 * Ensures that a hierarchy on an anonymous type in a second initializer is correct.
 */
public void testAnonymousType02() throws JavaModelException {
	IType typeA = getCompilationUnit("TypeHierarchy", "src", "p7", "A.java").getType("A");
	IType type = typeA.getInitializer(2).getType("", 1);
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: <anonymous #1> [in <initializer #2> [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"Super types:\n" +
		"  X [in X.java [in p7 [in src [in TypeHierarchy]]]]\n" +
		"    Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n",
		hierarchy);
}
/*
 * Ensures that a hierarchy on an anonymous type in a field declaration is correct.
 */
public void testAnonymousType03() throws JavaModelException {
	IType typeA = getCompilationUnit("TypeHierarchy", "src", "p7", "A.java").getType("A");
	IType type = typeA.getField("field1").getType("", 1);
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: <anonymous #1> [in field1 [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"Super types:\n" +
		"  X [in X.java [in p7 [in src [in TypeHierarchy]]]]\n" +
		"    Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n",
		hierarchy);
}
/*
 * Ensures that a hierarchy on an anonymous type in a field declaration is correct.
 */
public void testAnonymousType04() throws JavaModelException {
	IType typeA = getCompilationUnit("TypeHierarchy", "src", "p7", "A.java").getType("A");
	IType type = typeA.getField("field2").getType("", 1);
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: <anonymous #1> [in field2 [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"Super types:\n" +
		"  X [in X.java [in p7 [in src [in TypeHierarchy]]]]\n" +
		"    Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n",
		hierarchy);
	type = typeA.getField("field2").getType("", 2);
	hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: <anonymous #2> [in field2 [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"Super types:\n" +
		"  X [in X.java [in p7 [in src [in TypeHierarchy]]]]\n" +
		"    Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n",
		hierarchy);
}
/*
 * Ensures that a hierarchy on an anonymous type in a method declaration is correct.
 */
public void testAnonymousType05() throws JavaModelException {
	IType typeA = getCompilationUnit("TypeHierarchy", "src", "p7", "A.java").getType("A");
	IType type = typeA.getMethod("foo", new String[] {}).getType("", 1);
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: <anonymous #1> [in foo() [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"Super types:\n" +
		"  X [in X.java [in p7 [in src [in TypeHierarchy]]]]\n" +
		"    Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n",
		hierarchy);
}
/*
 * Ensures that a hierarchy on an anonymous type that uses a non-default constructor is correct.
 * (regression test for bug 44506 Type hierarchy is missing anonymous type)
 */
public void testAnonymousType06() throws JavaModelException {
	IType typeA = getCompilationUnit("TypeHierarchy", "src", "p8", "X.java").getType("X");
	IType type = typeA.getMethod("foo", new String[] {}).getType("", 1);
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: <anonymous #1> [in foo() [in X [in X.java [in p8 [in src [in TypeHierarchy]]]]]]\n" +
		"Super types:\n" +
		"  X [in X.java [in p8 [in src [in TypeHierarchy]]]]\n" +
		"    Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n",
		hierarchy);
}
/*
 * Ensure that the key of an anonymous binary type in a hierarchy is correct.
 * (regression test for bug 93826 ArrayIndexOutOfBoundsException when opening type hierarchy)
 */
public void testAnonymousType07() throws CoreException {
	IType type = getClassFile("TypeHierarchy","myLib.jar", "my.pkg", "X.class").getType();
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	IType[] subtypes = hierarchy.getSubtypes(type);
	assertEquals("Unexpected key", "Lmy/pkg/Y$1;", subtypes.length < 1 ? null : subtypes[0].getKey());
}
/*
 * Ensure that hierarchy on an enum also include the anonymous of its enum contants
 * (regression test for bug 120667 [hierarchy] Type hierarchy for enum type does not include anonymous subtypes)
 */
public void testAnonymousType08() throws CoreException {
	IType type = getCompilationUnit("TypeHierarchy15/src/Try.java").getType("Try");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: Try [in Try.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
		"Super types:\n" +
		"  Enum [in Enum.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
		"    Comparable [in Comparable.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
		"    Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
		"    Serializable [in Serializable.class [in java.io [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
		"Sub types:\n" +
		"  <anonymous #1> [in ANONYMOUS [in Try [in Try.java [in <default> [in src [in TypeHierarchy15]]]]]]\n",
		hierarchy);
}
/*
 * Ensure that hierarchy on the anonymous type of an enum constant is correct
 * (regression test for bug 120667 [hierarchy] Type hierarchy for enum type does not include anonymous subtypes)
 */
public void testAnonymousType09() throws CoreException {
	IType type = getCompilationUnit("TypeHierarchy15/src/Try.java").getType("Try").getField("ANONYMOUS").getType("", 1);
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: <anonymous #1> [in ANONYMOUS [in Try [in Try.java [in <default> [in src [in TypeHierarchy15]]]]]]\n" +
		"Super types:\n" +
		"  Try [in Try.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
		"    Enum [in Enum.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
		"      Comparable [in Comparable.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
		"      Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
		"      Serializable [in Serializable.class [in java.io [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
		"Sub types:\n",
		hierarchy);
}
/*
 * Ensure that hierarchy on the anonymous type of a member type that is opened is correct
 * (regression test for bug 122444 [hierarchy] Type hierarchy of inner member type misses anonymous subtypes)
 */
public void testAnonymousType10() throws CoreException {
	ICompilationUnit cu =  getCompilationUnit("TypeHierarchy/src/q7/X.java");
	cu.open(null);
	IType type = cu.getType("X").getType("Member");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: Member [in X [in X.java [in q7 [in src [in TypeHierarchy]]]]]\n" +
		"Super types:\n" +
		"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n" +
		"  <anonymous #1> [in foo(X) [in Y [in X.java [in q7 [in src [in TypeHierarchy]]]]]]\n",
		hierarchy);
}
/*
 * Ensure that hierarchy contains an anonymous type as a subclass of the focus type,
 * if the anonymous type is created with a message send to a third type as an argument to
 * the constructor.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=210070)
 */
public void testAnonymousType11() throws CoreException {
	IType type = getCompilationUnit("TypeHierarchy/src/q8/Y210070.java").getType("Y210070");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: Y210070 [in Y210070.java [in q8 [in src [in TypeHierarchy]]]]\n" +
		"Super types:\n" +
		"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n" +
		"  <anonymous #1> [in foo(X210070) [in Z210070 [in Z210070.java [in q8 [in src [in TypeHierarchy]]]]]]\n",
		hierarchy);
}
/*
 * Ensure that hierarchy contains an anonymous type as a subclass of the focus type,
 * if the anonymous type is created with a problem in its constructor call.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=210070)
 */
public void testAnonymousType12() throws CoreException {
	IType type = getCompilationUnit("TypeHierarchy/src/q8/A210070.java").getType("A210070");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: A210070 [in A210070.java [in q8 [in src [in TypeHierarchy]]]]\n" +
		"Super types:\n" +
		"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n" +
		"  <anonymous #1> [in foo() [in A210070 [in A210070.java [in q8 [in src [in TypeHierarchy]]]]]]\n",
		hierarchy);
}/**
 * Ensures that the superclass can be retrieved for a binary inner type.
 */
public void testBinaryInnerTypeGetSuperclass() throws JavaModelException {
	IOrdinaryClassFile cf = getClassFile("TypeHierarchy", "lib.jar", "binary", "Y$Inner.class");
	IType type = cf.getType();
	ITypeHierarchy h = type.newSupertypeHierarchy(null);
	IType superclass = h.getSuperclass(type);
	assertTrue("Superclass not found for Y$Inner", superclass != null);
	assertEquals("Unexpected super class", "Z", superclass.getElementName());
}
/**
 * Ensures that the superinterfaces can be retrieved for a binary inner type.
 */
public void testBinaryInnerTypeGetSuperInterfaces() throws JavaModelException {
	IOrdinaryClassFile cf = getClassFile("TypeHierarchy", "lib.jar", "binary", "Y$Inner.class");
	IType type = cf.getType();
	ITypeHierarchy h = type.newSupertypeHierarchy(null);
	assertTypesEqual(
		"Unexpected super interfaces",
		"binary.I\n",
		h.getSuperInterfaces(type));
}
/*
 * Ensures that the hierarchy lookup mechanism get the right binary if it is missplaced.
 * (regression test for bug 139279 Fup of bug 134110, got CCE changing an external jar contents and refreshing the project)
 */
public void testBinaryInWrongPackage() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL_LIB", "lib"}, "bin");
		createFolder("/P/src/p");
		createFile(
			"/P/src/p/X.java",
			"pakage p;\n" +
			"public class X {\n" +
			"}"
		);
		getProject("P").build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForAutoBuild();
		getFile("/P/bin/p/X.class").copy(new Path("/P/lib/X.class"), false, null);
		ITypeHierarchy hierarchy = getClassFile("P", "/P/lib", "", "X.class").getType().newSupertypeHierarchy(null);
		assertHierarchyEquals(
			"Focus: X [in X.class [in <default> [in lib [in P]]]]\n" +
			"Super types:\n" +
			"Sub types:\n",
			hierarchy);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a hierarchy with a binary subclass that is also referenced can be computed
 * (regression test for bug 48459 NPE in Type hierarchy)
 */
public  void testBinarySubclass() throws JavaModelException {
	IType type = getCompilationUnit("TypeHierarchy/src/p48459/p1/X48459.java").getType("X48459");
	ITypeHierarchy h = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: X48459 [in X48459.java [in p48459.p1 [in src [in TypeHierarchy]]]]\n" +
		"Super types:\n" +
		"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n" +
		"  <anonymous #1> [in foo [in Z48459 [in Z48459.java [in p48459.p1 [in src [in TypeHierarchy]]]]]]\n" +
		"  Y48459 [in Y48459.class [in p48459.p2 [in lib48459 [in TypeHierarchy]]]]\n",
		h);
}
/**
 * Ensures that the superclass can be retrieved for a binary type's superclass.
 */
public void testBinaryTypeGetSuperclass() throws JavaModelException {
	IOrdinaryClassFile cf = getClassFile("TypeHierarchy", "lib.jar", "binary", "Y.class");
	IType type = cf.getType();
	ITypeHierarchy h= type.newSupertypeHierarchy(null);
	IType superclass= h.getSuperclass(type);
	assertTrue("Superclass not found forY", superclass != null);
	assertEquals("Unexpected superclass of Y", "X", superclass.getElementName());
}
/**
 * Ensures that the superclass can be retrieved for a binary type's superclass.
 * This is a relatively deep type hierarchy.
 */
public void testBinaryTypeGetSuperclass2() throws JavaModelException {
	IOrdinaryClassFile cf = getClassFile("TypeHierarchy", "lib.jar", "binary", "Deep.class");
	IType type = cf.getType();
	ITypeHierarchy h= type.newSupertypeHierarchy(null);
	IType superclass= h.getSuperclass(type);
	assertTrue("Superclass not found for Deep", superclass != null);
	assertEquals("Unexpected superclass of Deep", "Z", superclass.getElementName());
}
/**
 * Ensures that the superinterfaces can be retrieved for a binary type's superinterfaces.
 */
public void testBinaryTypeGetSuperInterfaces() throws JavaModelException {
	IOrdinaryClassFile cf = getClassFile("TypeHierarchy", "lib.jar", "binary", "X.class");
	IType type = cf.getType();
	ITypeHierarchy h = type.newSupertypeHierarchy(null);
	IType[] superInterfaces = h.getSuperInterfaces(type);
	assertTypesEqual(
		"Unexpected super interfaces of X",
		"binary.I\n",
		superInterfaces);
}
/**
 * Ensures that the superinterfaces can be retrieved for a binary type's superinterfaces.
 * Test with type that has a "rich" super hierarchy
 */
public void testBinaryTypeGetSuperInterfaces2() throws JavaModelException {
	IOrdinaryClassFile cf = getClassFile("TypeHierarchy", "lib.jar", "rich", "C.class");
	IType type = cf.getType();
	ITypeHierarchy h = type.newSupertypeHierarchy(null);
	IType[] superInterfaces = h.getSuperInterfaces(type);
	assertTypesEqual(
		"Unexpected super interfaces of C",
		"rich.I\n" +
		"rich.I3\n",
		superInterfaces);
}
/*
 * Ensures that a hierarchy can be constructed on a binary type in a jar that is hidden by another jar with the same type.
 * (regression test for bug
 */
public void testBinaryTypeHiddenByOtherJar() throws CoreException, IOException {
	String externalJar1 = null;
	String externalJar2 = null;
	try {
		externalJar1 = Util.getOutputDirectory() + File.separator + "test1.jar";
		Util.createJar(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"}" ,
				"p/Y.java",
				"package p;\n" +
				"public class Y extends X {\n" +
				"}"
			},
			new HashMap(),
			externalJar1
		);
		externalJar2 = Util.getOutputDirectory() + File.separator + "test2.jar";
		Util.createJar(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"}" ,
				"p/Y.java",
				"package p;\n" +
				"public class Y extends X {\n" +
				"}"
			},
			new HashMap(),
			externalJar2
		);
		IJavaProject project = createJavaProject("P", new String[] {}, new String[] {"JCL_LIB", externalJar1, externalJar2}, "");
		IType focus = project.getPackageFragmentRoot(externalJar2).getPackageFragment("p").getOrdinaryClassFile("Y.class").getType();
		assertHierarchyEquals(
			"Focus: Y [in Y.class [in p [in " + externalJar2 + "]]]\n" +
			"Super types:\n" +
			"  X [in X.class [in p [in " + externalJar1 + "]]]\n" +
			"    Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"Sub types:\n",
			focus.newTypeHierarchy(null)
		);
	} finally {
		if (externalJar1 != null)
			Util.delete(externalJar1);
		if (externalJar2 != null)
			Util.delete(externalJar2);
		deleteProject("P");
	}
}
/*
 * Ensures that a hierarchy can be constructed on a binary type in a jar that has '.class' in its name.
 * (regression test for bug 157035 "Open Type Hierarchy" fails if subtype is anonymous or local class and location for this subtype contains ".class")
 */
public void testBinaryTypeInDotClassJar() throws CoreException, IOException {
	String externalJar = null;
	try {
		externalJar = Util.getOutputDirectory() + File.separator + "test.classic.jar";
		Util.createJar(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"}" ,
				"p/Y.java",
				"package p;\n" +
				"public class Y {\n" +
				"  void foo() {\n" +
				"    new X() {\n" +
				"    };\n" +
				" }\n" +
				"}"
			},
			new HashMap(),
			externalJar
		);
		IJavaProject project = createJavaProject("P", new String[] {}, new String[] {"JCL_LIB", externalJar}, "");
		IType focus = project.getPackageFragmentRoot(externalJar).getPackageFragment("p").getOrdinaryClassFile("X.class").getType();
		assertHierarchyEquals(
			"Focus: X [in X.class [in p [in " + externalJar + "]]]\n" +
			"Super types:\n" +
			"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"Sub types:\n" +
			"  <anonymous> [in Y$1.class [in p [in " + externalJar + "]]]\n",
			focus.newTypeHierarchy(null)
		);
	} finally {
		if (externalJar != null)
			Util.delete(externalJar);
		deleteProject("P");
	}
}
/**
 * Ensures that the creation of a type hierarchy can be cancelled.
 */
public void testCancel() throws JavaModelException {
	boolean isCanceled = false;
	IType type = getCompilationUnit("TypeHierarchy", "src", "p1", "X.java").getType("X");
	IRegion region = JavaCore.newRegion();
	region.add(getPackageFragmentRoot("TypeHierarchy", "src"));
	try {
		TestProgressMonitor monitor = TestProgressMonitor.getInstance();
		monitor.setCancelledCounter(1);
		type.getJavaProject().newTypeHierarchy(type, region, monitor);
	} catch (OperationCanceledException e) {
		isCanceled = true;
	}
	assertTrue("Operation should have thrown an operation canceled exception", isCanceled);
}
/**
 * Ensures that contains(...) returns true for a type that is part of the
 * hierarchy and false otherwise.
 */
public void testContains1() throws JavaModelException {
	// regular class
	IType type = getClassFile("TypeHierarchy", "lib.jar", "binary", "X.class").getType();
	assertTrue("X must be included", this.typeHierarchy.contains(type));
}
public void testContains2() throws JavaModelException {
	// root class
	IType type = getClassFile("TypeHierarchy", getExternalJCLPathString(), "java.lang", "Object.class").getType();
	assertTrue("Object must be included", this.typeHierarchy.contains(type));
}
public void testContains3() throws JavaModelException {
	// interface
	IType type = getClassFile("TypeHierarchy", "lib.jar", "binary", "I.class").getType();
	assertTrue("I must be included", this.typeHierarchy.contains(type));
}
public void testCycle() throws JavaModelException {
	IType type = getCompilationUnit("/TypeHierarchy/src/cycle/X.java").getType("X");
	ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: X [in X.java [in cycle [in src [in TypeHierarchy]]]]\n" +
		"Super types:\n" +
		"  Y [in Y.java [in cycle [in src [in TypeHierarchy]]]]\n" +
		"Sub types:\n",
		hierarchy
	);
}
public void testCycle2() throws JavaModelException {
	IType type = getCompilationUnit("/TypeHierarchy15/src/CycleParent.java").getType("CycleParent");
	ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: CycleParent [in CycleParent.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
		"Super types:\n" +
		"  CycleBase [in CycleBase.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
		"    Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
		"Sub types:\n",
		hierarchy
	);
}
/*
 * Ensures that creating a type hierarchy accross multiple project checks for cancellation regularly.
 */
public void testEfficiencyMultipleProjects() throws CoreException {
	try {
		createJavaProject("P1", new String[] {""}, new String[] {"JCL_LIB"}, "");
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "");
		createJavaProject("P3", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "");
		createFile("/P1/X.java", "public class X {}");
		createFile("/P3/Y.java", "public class Y extends X {}");
		createFile("/P3/Z.java", "public class Z extends X {}");
		createFile("/P2/W.java", "public class W extends X {}");
		waitUntilIndexesReady();
		IType type = getCompilationUnit("/P1/X.java").getType("X");
		class ProgressCounter extends TestProgressMonitor {
			int count = 0;
			@Override
			public boolean isCanceled() {
				this.count++;
				return false;
			}
		}
		ProgressCounter counter = new ProgressCounter();
		type.newTypeHierarchy(counter);
		assertTrue("Not enough cancellation checks", counter.count >= 85);
	} finally {
		deleteProjects(new String[] {"P1", "P2", "P3"});
	}
}
/*
 * Ensures that a hierarchy can be created with a potential subtype in an empty primary working copy
 * (regression test for bug 65677 Creating hierarchy failed. See log for details. 0)
 */
public void testEmptyWorkingCopyPotentialSubtype() throws JavaModelException {
    ICompilationUnit workingCopy = null;
    try {
        workingCopy = getCompilationUnit("/TypeHierarchy/src/q4/Y.java");
        workingCopy.becomeWorkingCopy(null);
        workingCopy.getBuffer().setContents("");
        workingCopy.makeConsistent(null);

        IType type = getCompilationUnit("/TypeHierarchy/src/q4/X.java").getType("X");
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
		assertHierarchyEquals(
			"Focus: X [in X.java [in q4 [in src [in TypeHierarchy]]]]\n" +
			"Super types:\n" +
			"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"Sub types:\n",
			hierarchy);
    } finally {
        if (workingCopy != null)
            workingCopy.discardWorkingCopy();
    }
}
/*
 * Ensures that subtypes are found in an external library folder
 */
public void testExternalFolder() throws CoreException, IOException {
	try {
		createExternalFolder("externalLib");
		Util.compile(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"}",
				"p/Y.java",
				"package p;\n" +
				"public class Y extends X {\n" +
				"}",
			},
			new HashMap(),
			getExternalResourcePath("externalLib"));
		createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		IOrdinaryClassFile classFile = getClassFile("P", getExternalResourcePath("externalLib"), "p", "X.class");
		ITypeHierarchy hierarchy = classFile.getType().newTypeHierarchy(null);
		assertHierarchyEquals(
			"Focus: X [in X.class [in p [in "+ getExternalPath() + "externalLib]]]\n" +
			"Super types:\n" +
			"Sub types:\n" +
			"  Y [in Y.class [in p [in "+ getExternalPath() + "externalLib]]]\n",
			hierarchy);
	} finally {
		deleteProject("P");
		deleteExternalResource("externalLib");
	}
}
/*
 * Ensures that subtypes are found in an external ZIP archive
 */
public void testZIPArchive() throws CoreException, IOException {
	try {
		createJar(
			new String[] {
				"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"}",
				"p/Y.java",
				"package p;\n" +
				"public class Y extends X {\n" +
				"}",
			},
			getExternalResourcePath("externalLib.abc"));
		IJavaProject p = createJavaProject("P", new String[0], new String[] {getExternalResourcePath("externalLib.abc")}, "");
		refreshExternalArchives(p);

		IOrdinaryClassFile classFile = getClassFile("P", getExternalResourcePath("externalLib.abc"), "p", "X.class");
		ITypeHierarchy hierarchy = classFile.getType().newTypeHierarchy(null);
		assertHierarchyEquals(
			"Focus: X [in X.class [in p [in "+ getExternalPath() + "externalLib.abc]]]\n" +
			"Super types:\n" +
			"Sub types:\n" +
			"  Y [in Y.class [in p [in "+ getExternalPath() + "externalLib.abc]]]\n",
			hierarchy);
	} finally {
		deleteExternalResource("externalLib.abc");
		deleteProject("P");
	}
}
/*
 * Ensures that a call to IJavaProject.findType("java.lang.Object") doesn't cause the hierarchy
 * computation to throw a StackOverFlow
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=209222)
 */
public void testFindObject() throws CoreException {
	// ensure Object.class is closed
	this.currentProject.getPackageFragmentRoot(getExternalJCLPathString()).getPackageFragment("java.lang").getOrdinaryClassFile("Object.class").close();
	// find Object to fill internal jar type cache
	IType type = this.currentProject.findType("java.lang.Object");
	// create hierarchy
	type.newTypeHierarchy(null); // should not throw a StackOverFlow
}
/*
 * Ensures that a hierarchy on a type with local and anonymous types is correct.
 */
public void testFocusWithLocalAndAnonymousTypes() throws JavaModelException {
	IType type = getCompilationUnit("TypeHierarchy", "src", "p7", "X.java").getType("X");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: X [in X.java [in p7 [in src [in TypeHierarchy]]]]\n" +
		"Super types:\n" +
		"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n" +
		"  <anonymous #1> [in <initializer #2> [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"  <anonymous #1> [in field1 [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"  <anonymous #1> [in field2 [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"  <anonymous #1> [in foo() [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"  <anonymous #1> [in <initializer #1> [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"  <anonymous #2> [in field2 [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"  Y1 [in foo() [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"    Y2 [in foo() [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"  Y1 [in <initializer #1> [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"    Y2 [in <initializer #1> [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n",
		hierarchy);
}
/*
 * Ensures that a hierarchy on a generic type can be opened
 */
public void testGeneric01() throws JavaModelException {
	IType type = getCompilationUnit("/TypeHierarchy15/src/X.java").getType("X");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: X [in X.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
		"Super types:\n" +
		"  ArrayList [in ArrayList.class [in util [in lib15.jar [in TypeHierarchy15]]]]\n" +
		"    AbstractList [in AbstractList.class [in util [in lib15.jar [in TypeHierarchy15]]]]\n" +
		"      Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
		"    List [in List.class [in util [in lib15.jar [in TypeHierarchy15]]]]\n" +
		"  List [in List.class [in util [in lib15.jar [in TypeHierarchy15]]]]\n" +
		"Sub types:\n",
		hierarchy
	);
}
/*
 * Ensures that a hierarchy on a generic type can be opened
 */
public void testGeneric02() throws JavaModelException {
	IType type = getPackageFragmentRoot("/TypeHierarchy15/lib15.jar").getPackageFragment("util").getOrdinaryClassFile("ArrayList.class").getType();
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: ArrayList [in ArrayList.class [in util [in lib15.jar [in TypeHierarchy15]]]]\n" +
		"Super types:\n" +
		"  AbstractList [in AbstractList.class [in util [in lib15.jar [in TypeHierarchy15]]]]\n" +
		"    Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
		"  List [in List.class [in util [in lib15.jar [in TypeHierarchy15]]]]\n" +
		"Sub types:\n" +
		"  X [in X.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
		"  Y [in Y.java [in <default> [in src [in TypeHierarchy15]]]]\n",
		hierarchy
	);
}
/*
 * Ensures that a hierarchy on a generic type can be opened
 */
public void testGeneric03() throws JavaModelException {
	IType type = getCompilationUnit("/TypeHierarchy15/src/Y.java").getType("Y");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: Y [in Y.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
		"Super types:\n" +
		"  ArrayList [in ArrayList.class [in util [in lib15.jar [in TypeHierarchy15]]]]\n" +
		"    AbstractList [in AbstractList.class [in util [in lib15.jar [in TypeHierarchy15]]]]\n" +
		"      Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
		"    List [in List.class [in util [in lib15.jar [in TypeHierarchy15]]]]\n" +
		"  List [in List.class [in util [in lib15.jar [in TypeHierarchy15]]]]\n" +
		"Sub types:\n",
		hierarchy
	);
}
/*
 * Ensures that a super type hierarchy on a generic type can be opened
 * (regression test for bug 72348 [1.5][Type Hierarchy] Super type hierarchy of class extending generic type is empty)
 */
public void testGeneric04() throws JavaModelException {
	IType type = getCompilationUnit("/TypeHierarchy15/src/X.java").getType("X");
	ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: X [in X.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
		"Super types:\n" +
		"  ArrayList [in ArrayList.class [in util [in lib15.jar [in TypeHierarchy15]]]]\n" +
		"    AbstractList [in AbstractList.class [in util [in lib15.jar [in TypeHierarchy15]]]]\n" +
		"      Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
		"    List [in List.class [in util [in lib15.jar [in TypeHierarchy15]]]]\n" +
		"  List [in List.class [in util [in lib15.jar [in TypeHierarchy15]]]]\n" +
		"Sub types:\n",
		hierarchy
	);
}
/*
 * Ensures that a hierarchy on a generic interface can be opened
 * (regression test for bug 82004 [model][5.0] 3.1M4 type hierarchy for generic interface)
 */
public void testGeneric05() throws JavaModelException {
	IType type = getCompilationUnit("/TypeHierarchy15/src/I.java").getType("I");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: I [in I.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
		"Super types:\n" +
		"Sub types:\n" +
		"  A [in A.java [in <default> [in src [in TypeHierarchy15]]]]\n",
		hierarchy
	);
}
/*
 * Ensure that the key of a binary type in a hierarchy is correct when this type is not part of the Java model cache.
 * (regression test for bug 93854 IAE in Util.scanTypeSignature when scanning a signature retrieved from a binding key)
 */
public void testGeneric06() throws CoreException {
	getJavaProject("TypeHierarcht15").close();
	IType type = getClassFile("TypeHierarchy15","lib15.jar", "util", "AbstractList.class").getType();
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	IType[] subtypes = hierarchy.getSubtypes(type);
	assertEquals("Unexpected key", "Lutil/Map<TK;TV;>;", subtypes.length < 2 ? null : subtypes[1].getKey());
}
/*
 * Ensures that a hierarchy on a generic type that is extended using a member as a type parameter can be opened
 * (regression test for bug 99606 Subtype not found if parameterized on inner class)
 */
public void testGeneric07() throws JavaModelException {
	IType type = getCompilationUnit("/TypeHierarchy15/src/Y99606.java").getType("Y99606");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: Y99606 [in Y99606.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
		"Super types:\n" +
		"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
		"Sub types:\n" +
		"  X99606 [in X99606.java [in <default> [in src [in TypeHierarchy15]]]]\n",
		hierarchy
	);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=108740
public void testGeneric08() throws JavaModelException {
	IType type = getCompilationUnit("/TypeHierarchy15/src/D108740.java").getType("D108740");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: D108740 [in D108740.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
		"Super types:\n" +
		"  B108740 [in B108740.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
		"    A108740 [in A108740.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
		"      Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
		"Sub types:\n",
		hierarchy
	);
}
/*
 * Ensures that a hierarchy is where a type inherits conflicting paratemerized types is still correctly reported
 * (regression test for bug 136095 Type Hierarchy incomplete with illegally parameterized superinterfaces)
 */
public void testGeneric09() throws CoreException {
	try {
		createFile(
			"/TypeHierarchy15/src/I1_136095.java",
			"public interface I1_136095<E> {\n" +
			"}"
		);
		createFile(
			"/TypeHierarchy15/src/I2_136095.java",
			"public interface I2_136095 extends I1_136095<String>{\n" +
			"}"
		);
		createFile(
			"/TypeHierarchy15/src/X_136095.java",
			"public abstract class X_136095 implements I1_136095<Integer>, I2_136095 {\n" +
			"}"
		);
		IType type = getCompilationUnit("/TypeHierarchy15/src/X_136095.java").getType("X_136095");
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
		assertHierarchyEquals(
			"Focus: X_136095 [in X_136095.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
			"Super types:\n" +
			"  I1_136095 [in I1_136095.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
			"  I2_136095 [in I2_136095.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
			"    I1_136095 [in I1_136095.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
			"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
			"Sub types:\n",
			hierarchy
		);
	} finally {
		deleteFile("/TypeHierarchy15/src/I1_136095.java");
		deleteFile("/TypeHierarchy15/src/I2_136095.java");
		deleteFile("/TypeHierarchy15/src/X_136095.java");
	}
}
/*
 * Ensures that a super type hierarchy is where the focus type implements a generic type with a qualified type parameter is correct
 * (regression test for bug 140340 [5.0][templates] foreach template does not work when an Iterable over a static inner class exists)
 */
public void testGeneric10() throws CoreException {
	try {
		createFile(
			"/TypeHierarchy15/src/Y_140340.java",
			"public class Y_140340 {\n" +
			"  public static class Z {\n" +
			"  }\n" +
			"}"
		);
		createFile(
			"/TypeHierarchy15/src/X_140340.java",
			"public class X_140340 implements I1_140340<Y_140340.Z> {\n" +
			"}\n" +
			"interface I1_140340<T> {\n" +
			"}"
		);
		IType type = getCompilationUnit("/TypeHierarchy15/src/X_140340.java").getType("X_140340");
		ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
		assertHierarchyEquals(
			"Focus: X_140340 [in X_140340.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
			"Super types:\n" +
			"  I1_140340 [in X_140340.java [in <default> [in src [in TypeHierarchy15]]]]\n" +
			"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]\n" +
			"Sub types:\n",
			hierarchy
		);
	} finally {
		deleteFile("/TypeHierarchy15/src/Y_140340.java");
		deleteFile("/TypeHierarchy15/src/X_140340.java");
	}
}
/*
 * Ensures that no cycle is created in a hierarchy with 2 types with same simple names and errors
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=215681 )
 */
public void testGeneric11() throws CoreException {
	try {
		createFolder("/TypeHierarchy15/src/p215681");
		createFile(
			"/TypeHierarchy15/src/p215681/A_215681.java",
			"package p215681;\r\n" +
			"public class A_215681<E> {\n" +
			"}"
		);
		createFolder("/TypeHierarchy15/src/q215681");
		createFile(
			"/TypeHierarchy15/src/q215681/A_215681.java",
			"package q215681;\n" +
			"import p215681.A_215681;\n" +
			"public class A_215681 extends A_215681<Object> {\n" +
			"}"
		);
		IType type = getCompilationUnit("/TypeHierarchy15/src/q215681/A_215681.java").getType("A_215681");
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
		assertHierarchyEquals(
			"Focus: A_215681 [in A_215681.java [in q215681 [in src [in TypeHierarchy15]]]]\n" +
			"Super types:\n" +
			"Sub types:\n",
			hierarchy
		);
	} finally {
		deleteFolder("/TypeHierarchy15/src/p215681");
		deleteFolder("/TypeHierarchy15/src/q215681");
	}
}
/*
 * Ensures that no cycle is created in a hierarchy with 2 types with same simple names and errors
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=215681 )
 */
public void testGeneric12() throws CoreException {
	try {
		createFolder("/TypeHierarchy15/src/p215681");
		createFile(
			"/TypeHierarchy15/src/p215681/A_215681.java",
			"package p215681;\r\n" +
			"public interface A_215681<E> {\n" +
			"}"
		);
		createFolder("/TypeHierarchy15/src/q215681");
		createFile(
			"/TypeHierarchy15/src/q215681/A_215681.java",
			"package q215681;\n" +
			"import p215681.A_215681;\n" +
			"public interface A_215681 extends A_215681<Object> {\n" +
			"}"
		);
		IType type = getCompilationUnit("/TypeHierarchy15/src/q215681/A_215681.java").getType("A_215681");
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
		assertHierarchyEquals(
			"Focus: A_215681 [in A_215681.java [in q215681 [in src [in TypeHierarchy15]]]]\n" +
			"Super types:\n" +
			"Sub types:\n",
			hierarchy
		);
	} finally {
		deleteFolder("/TypeHierarchy15/src/p215681");
		deleteFolder("/TypeHierarchy15/src/q215681");
	}
}
/**
 * Ensures the correctness of all classes in a type hierarchy based on a region.
 */
public void testGetAllClassesInRegion() {
	IType[] types = this.typeHierarchy.getAllClasses();
	assertTypesEqual(
		"Unexpected all classes in hierarchy",
		"binary.Deep\n" +
		"binary.X\n" +
		"binary.Y\n" +
		"binary.Y$Inner\n" +
		"binary.Z\n" +
		"java.lang.Object\n" +
		"rich.A\n" +
		"rich.B\n" +
		"rich.C\n",
		types);
}
/**
 * Ensures the correctness of all interfaces in a type hierarchy based on a region.
 */
public void testGetAllInterfacesInRegion() {
	IType[] types = this.typeHierarchy.getAllInterfaces();
	assertTypesEqual(
		"Unexpected all interfaces in hierarchy",
		"binary.I\n" +
		"rich.I\n" +
		"rich.I2\n" +
		"rich.I3\n",
		types);
}
/**
 * Ensures that the correct subtypes of a type exist in the type
 * hierarchy.
 */
public void testGetAllSubtypes() throws JavaModelException {
	IType type = getCompilationUnit("TypeHierarchy", "src", "p1", "X.java").getType("X");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	IType[] types = hierarchy.getAllSubtypes(type);
	this.assertTypesEqual(
		"Unexpected sub types of X",
		"p1.Deep\n" +
		"p1.Y\n" +
		"p1.Z\n",
		types
	);
}
/**
 * Ensures that the correct subtypes of a binary type
 * exit in the type hierarchy created on a region.
 */
public void testGetAllSubtypesFromBinary() throws JavaModelException {
	IType type = getClassFile("TypeHierarchy", "lib.jar", "binary", "X.class").getType();
	IRegion region = JavaCore.newRegion();
	region.add(type.getPackageFragment());
	ITypeHierarchy hierarchy = type.getJavaProject().newTypeHierarchy(type, region, null);
	IType[] types = hierarchy.getAllSubtypes(type);
	assertTypesEqual(
		"Unexpected all subtypes of binary.X",
		"binary.Deep\n" +
		"binary.Y\n" +
		"binary.Y$Inner\n" +
		"binary.Z\n",
		types);
}

/**
 * Ensures that the correct superclasses of a type exist in the type
 * hierarchy.
 */
public void testGetAllSuperclasses() throws JavaModelException {
	IType type = getCompilationUnit("TypeHierarchy", "src", "p1", "Z.java").getType("Z");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	IType[] types = hierarchy.getAllSuperclasses(type);
	assertTypesEqual(
		"Unexpected all super classes of Z",
		"java.lang.Object\n" +
		"p1.X\n" +
		"p1.Y\n",
		types);
}

/**
 * Ensures that the correct superclasses of a binary type exist in the type  hierarchy.
 * (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=53095)
 */
public void testGetAllSuperclassesFromBinary() throws JavaModelException {
	String fileName = "TypeHierarchy/lib53095/p53095/X53095.class";	//$NON-NLS-1$
	IJavaElement javaElement = JavaCore.create(getFile(fileName));
	assertNotNull("Problem to get class file \""+fileName+"\"", javaElement);
	assertTrue("Invalid type for class file \""+fileName+"\"", javaElement instanceof IClassFile);
	IType type = ((IOrdinaryClassFile) javaElement).getType();
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
 * (see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=54043)
 */
public void testGetAllSuperclassesFromBinary2() throws JavaModelException {
	IOrdinaryClassFile cf = getClassFile("TypeHierarchy", "test54043.jar", "p54043", "X54043.class");
	IType type = cf.getType();
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	IType[] types = hierarchy.getAllSupertypes(type);
	assertTypesEqual(
		"Unexpected all super classes of X54043",
		"java.lang.RuntimeException\n" +
		"java.lang.Exception\n" +
		"java.lang.Throwable\n" +
		"java.lang.Object\n",
		types,
		false);
}
/**
 * Ensures that the correct superinterfaces of a type exist in the type
 * hierarchy.
 */
public void testGetAllSuperInterfaces() throws JavaModelException {
	IType type = getCompilationUnit("TypeHierarchy", "src", "p1", "Z.java").getType("Z");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	IType[] types = hierarchy.getAllSuperInterfaces(type);
	assertTypesEqual(
		"Unexpected super interfaces of Z",
		"p1.I1\n" +
		"p1.I2\n",
		types);
}
/**
 * Ensures that the correct supertypes of a type exist in the type
 * hierarchy.
 */
public void testGetAllSupertypes() throws JavaModelException {
	IType type = getCompilationUnit("TypeHierarchy", "src", "p1", "Z.java").getType("Z");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	IType[] types = hierarchy.getAllSupertypes(type);
	assertTypesEqual(
		"Unexpected all super types of Z",
		"java.lang.Object\n" +
		"p1.I1\n" +
		"p1.I2\n" +
		"p1.X\n" +
		"p1.Y\n",
		types);
}
/**
 * Ensures that the correct supertypes of a type exist in the type
 * hierarchy.
 * (regression test for bug 23644 hierarchy: getAllSuperTypes does not include all superinterfaces?)
 */
public void testGetAllSupertypes2() throws JavaModelException {
	IType type = getCompilationUnit("TypeHierarchy", "src", "p3", "B.java").getType("B");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	IType[] types = hierarchy.getAllSupertypes(type);
	assertTypesEqual(
		"Unexpected all super types of B",
		"java.lang.Object\n" +
		"p3.A\n" +
		"p3.I\n" +
		"p3.I1\n",
		types);
}
/**
 * Ensures that the correct supertypes of a type exist in the type
 * hierarchy.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=239096 )
 */
public void testGetAllSupertypes3() throws JavaModelException {
	IType type = getCompilationUnit("TypeHierarchy", "src", "p1", "A.java").getType("B");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	IType[] types = hierarchy.getAllSupertypes(type);
	assertTypesEqual(
		"Unexpected all super types of B",
		"java.lang.Object\n",
		types);
}
/**
 * Ensures that the correct supertypes of a type exist in the type
 * hierarchy.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=239096 )
 */
public void testGetAllSupertypes4() throws JavaModelException {
	IType type = getCompilationUnit("TypeHierarchy", "src", "p1", "A.java").getType("B");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	IType[] types = hierarchy.getAllSuperInterfaces(type);
	assertTypesEqual(
		"Unexpected all super interfaces of B",
		"",
		types);
}
/**
 * Ensures that the correct types exist in the type
 * hierarchy.
 */
public void testGetAllTypes() throws JavaModelException {
	IType type = getCompilationUnit("TypeHierarchy", "src", "p1", "Y.java").getType("Y");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	this.assertTypesEqual(
		"Unexpected types in hierarchy of Y",
		"java.lang.Object\n" +
		"p1.Deep\n" +
		"p1.I1\n" +
		"p1.I2\n" +
		"p1.X\n" +
		"p1.Y\n" +
		"p1.Z\n",
		hierarchy.getAllTypes()
	);
}
/**
 * Ensures that the flags for an interface hierarchy are correctly cached
 * (regression test for bug 60365 hierarchy view shows some interfaces as classes [type hierarchy])
 */
public void testGetCachedFlags() throws JavaModelException {
	IType type1 = getClassFile("TypeHierarchy", "test60365.jar", "q4", "I1.class").getType();
	ITypeHierarchy hierarchy = type1.newTypeHierarchy(null);
	IType type2 = getClassFile("TypeHierarchy", "test60365.jar", "q4", "I2.class").getType();
	int flags = hierarchy.getCachedFlags(type2);
	assertTrue("Cached flags for I2 should indicate interface", Flags.isInterface(flags));
}
/**
 * Ensures that the correct extending interfaces exist in the type
 * hierarchy.
 */
public void testGetExtendingInterfaces() throws JavaModelException {
	IType type = getCompilationUnit("TypeHierarchy", "src", "p2", "I.java").getType("I");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	IType[] types = hierarchy.getExtendingInterfaces(type);
	this.assertTypesEqual(
		"Unexpected extending interfaces of I",
		"p2.I1\n" +
		"p2.I2\n",
		types
	);

	type = getCompilationUnit("TypeHierarchy", "src", "p2", "X.java").getType("X");
	hierarchy = type.newTypeHierarchy(null);
	types = hierarchy.getExtendingInterfaces(type);
	this.assertTypesEqual(
		"Unexpected extending interfaces of X",
		"", // interfaces cannot extend a class
		types
	);
}
/**
 * Ensures that the correct implementing interfaces exist in the type
 * hierarchy.
 */
public void testGetImplementingClasses() throws JavaModelException {
	IType type = getCompilationUnit("TypeHierarchy", "src", "p2", "I.java").getType("I");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	IType[] types = hierarchy.getImplementingClasses(type);
	this.assertTypesEqual(
		"Unexpected implementing classes of I",
		"p2.X\n",
		types
	);

	type = getCompilationUnit("TypeHierarchy", "src", "p2", "X.java").getType("X");
	hierarchy = type.newTypeHierarchy(null);
	types = hierarchy.getImplementingClasses(type);
	this.assertTypesEqual(
		"Unexpected implementing classes of X",
		"", // classes cannot implement a class
		types
	);
}
/**
 * Ensures that the correct root classes exist in the type
 * hierarchy.
 */
public void testGetRootClasses() {
	IType[] types = this.typeHierarchy.getRootClasses();
	assertTypesEqual(
		"Unexpected root classes",
		"java.lang.Object\n",
		types);
}
/**
 * Ensures that the correct root interfaces exist in the type
 * hierarchy.
 */
public void testGetRootInterfaces() throws JavaModelException {
	IType type = getCompilationUnit("TypeHierarchy", "src", "p2", "Y.java").getType("Y");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	IType[] types = hierarchy.getRootInterfaces();
	assertTypesEqual(
		"Unexpected root classes",
		"p2.I\n",
		types);
}
/**
 * Ensures that getRootInterfaces() works on a IRegion.
 */
public void testGetRootInterfacesFromRegion() {
	IType[] types = this.typeHierarchy.getRootInterfaces();
	assertTypesEqual(
		"Unexpected root classes",
		"binary.I\n" +
		"rich.I\n" +
		"rich.I3\n",
		types);
}
/**
 * Ensures that the correct number of subclasses exist in the type
 * hierarchy created on a region.
 */
public void testGetSubclasses() throws JavaModelException {
	IType type = getClassFile("TypeHierarchy", "lib.jar", "binary", "X.class").getType();
	IType[] types = this.typeHierarchy.getSubclasses(type);
	this.assertTypesEqual(
		"Unexpected subclasses of binary.X",
		"binary.Y\n",
		types
	);

	type = getClassFile("TypeHierarchy", "lib.jar", "binary", "I.class").getType();
	types = this.typeHierarchy.getSubclasses(type);
	this.assertTypesEqual(
		"Unexpected subclasses of binary.I",
		"", // interfaces cannot have a subclass
		types
	);
}
/**
 * Ensures that the correct number of subtypes exist in the type
 * hierarchy created on a region.
 */
public void testGetSubtypes() throws JavaModelException {
	IType type = getClassFile("TypeHierarchy", "lib.jar", "binary", "X.class").getType();
	IType[] types = this.typeHierarchy.getSubtypes(type);
	this.assertTypesEqual(
		"Unexpected subtypes of binary.X",
		"binary.Y\n",
		types
	);

	type = getClassFile("TypeHierarchy", "lib.jar", "binary", "I.class").getType();
	types = this.typeHierarchy.getSubtypes(type);
	this.assertTypesEqual(
		"Unexpected subtypes of binary.I",
		"binary.X\n" +
		"binary.Y$Inner\n",
		types
	);
}

/**
 * Ensures that the superclass is correct in the type
 * hierarchy a type created on a region containing a package.
 */
public void testGetSuperclassInRegion() throws JavaModelException {
	IRegion r = JavaCore.newRegion();
	IPackageFragment p = getPackageFragment("TypeHierarchy", "src", "p1");
	r.add(p);
	ITypeHierarchy hierarchy = p.getJavaProject().newTypeHierarchy(r, null);

	IType type = getCompilationUnit("TypeHierarchy", "src", "p1", "Y.java").getType("Y");
	IType superclass= hierarchy.getSuperclass(type);
	assertEquals("Unexpected super class of Y", "X", superclass.getElementName());
}
/**
 * Ensures that the correct supertypes exist in the type
 * hierarchy created on a region.
 */
public void testGetSupertypesInRegion() throws JavaModelException {
	IType type = getClassFile("TypeHierarchy", "lib.jar", "binary", "Y.class").getType();
	IType[] superTypes = this.typeHierarchy.getSupertypes(type);
	assertTypesEqual(
		"Unexpected super types of Y",
		"binary.X\n",
		superTypes);
}
/**
 * Ensures that the correct supertypes exist in the type
 * hierarchy created on a region containing a project.
 */
public void testGetSupertypesWithProjectRegion() throws JavaModelException {
	IJavaProject project = getJavaProject("TypeHierarchy");
	IRegion region= JavaCore.newRegion();
	region.add(project);
	IType type = getClassFile("TypeHierarchy", "lib.jar", "binary", "Y.class").getType();
	ITypeHierarchy hierarchy = project.newTypeHierarchy(type, region, null);
	IType[] superTypes = hierarchy.getSupertypes(type);
	assertTypesEqual(
		"Unexpected super types of Y",
		"binary.X\n",
		superTypes);
}
/**
 * Ensures that getType() returns the type the hierarchy was created for.
 */
public void testGetType() throws JavaModelException {
	// hierarchy created on a type
	IOrdinaryClassFile cf = getClassFile("TypeHierarchy", "lib.jar", "binary", "Y.class");
	IType type = cf.getType();
	ITypeHierarchy hierarchy = null;
	try {
		hierarchy = type.newSupertypeHierarchy(null);
	} catch (IllegalArgumentException iae) {
		assertTrue("IllegalArgumentException", false);
	}
	assertEquals("Unexpected focus type", type, hierarchy.getType());

	// hierarchy created on a region
	assertTrue("Unexpected focus type for hierarchy on region", this.typeHierarchy.getType() == null);
}
/*
 * Ensures that a hierarchy on an type that implements a binary inner interface is correct.
 * (regression test for bug 58440 type hierarchy incomplete when implementing fully qualified interface)
 */
public void testImplementBinaryInnerInterface() throws JavaModelException {
	IOrdinaryClassFile cf = getClassFile("TypeHierarchy", "test58440.jar", "p58440", "Y.class");
	IType type = cf.getType();
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: Y [in Y.class [in p58440 [in test58440.jar [in TypeHierarchy]]]]\n" +
		"Super types:\n" +
		"  Inner [in X$Inner.class [in p58440 [in test58440.jar [in TypeHierarchy]]]]\n" +
		"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n",
		hierarchy);
}
/**
 * Ensures that a hierarchy on an inner type is correctly rooted.
 */
public void testInnerType1() throws JavaModelException {
	IType type = getCompilationUnit("TypeHierarchy", "src", "p5", "X.java").getType("X").getType("Inner");
	ITypeHierarchy hierarchy = null;
	try {
		hierarchy = type.newTypeHierarchy(null);
	} catch (IllegalArgumentException iae) {
		assertTrue("IllegalArgumentException", false);
	}
	assertHierarchyEquals(
		"Focus: Inner [in X [in X.java [in p5 [in src [in TypeHierarchy]]]]]\n" +
		"Super types:\n" +
		"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n",
		hierarchy);
}


/**
 * Ensures that a hierarchy on an inner type has the correct subtype.
 * (regression test for bug 43274 Type hierarchy broken)
 */
public void testInnerType2() throws JavaModelException {
	IType type = getCompilationUnit("TypeHierarchy", "src", "p6", "A.java").getType("A").getType("Inner");
	ITypeHierarchy hierarchy = null;
	try {
		hierarchy = type.newTypeHierarchy(null);
	} catch (IllegalArgumentException iae) {
		assertTrue("IllegalArgumentException", false);
	}
	assertHierarchyEquals(
		"Focus: Inner [in A [in A.java [in p6 [in src [in TypeHierarchy]]]]]\n" +
		"Super types:\n" +
		"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n" +
		"  B [in A.java [in p6 [in src [in TypeHierarchy]]]]\n",
		hierarchy);
}
/*
 * Ensures that a hierarchy on a local type in an initializer is correct.
 */
public void testLocalType1() throws JavaModelException {
	IType typeA = getCompilationUnit("TypeHierarchy", "src", "p7", "A.java").getType("A");
	IType type = typeA.getInitializer(1).getType("Y1", 1);
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: Y1 [in <initializer #1> [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"Super types:\n" +
		"  X [in X.java [in p7 [in src [in TypeHierarchy]]]]\n" +
		"    Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n" +
		"  Y2 [in <initializer #1> [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n",
		hierarchy);
}
/*
 * Ensures that a hierarchy on a local type in a second initializer is correct.
 */
public void testLocalType2() throws JavaModelException {
	IType typeA = getCompilationUnit("TypeHierarchy", "src", "p7", "A.java").getType("A");
	IType type = typeA.getInitializer(2).getType("Y3", 1);
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: Y3 [in <initializer #2> [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"Super types:\n" +
		"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n",
		hierarchy);
}
/*
 * Ensures that a hierarchy on a local type in a method declaration is correct.
 */
public void testLocalType3() throws JavaModelException {
	IType typeA = getCompilationUnit("TypeHierarchy", "src", "p7", "A.java").getType("A");
	IType type = typeA.getMethod("foo", new String[] {}).getType("Y2", 1);
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: Y2 [in foo() [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"Super types:\n" +
		"  Y1 [in foo() [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"    X [in X.java [in p7 [in src [in TypeHierarchy]]]]\n" +
		"      Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n",
		hierarchy);
}
/*
 * Ensures that a super type hierarchy on a local type in a method declaration is correct.
 * (regression test for bug 44073 Override methods action does not work for local types [code manipulation])
 */
public void testLocalType4() throws JavaModelException {
	IType typeA = getCompilationUnit("TypeHierarchy", "src", "p7", "A.java").getType("A");
	IType type = typeA.getMethod("foo", new String[] {}).getType("Y1", 1);
	ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: Y1 [in foo() [in A [in A.java [in p7 [in src [in TypeHierarchy]]]]]]\n" +
		"Super types:\n" +
		"  X [in X.java [in p7 [in src [in TypeHierarchy]]]]\n" +
		"    Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n",
		hierarchy);
}
/*
 * Ensures that a super type hierarchy on a local type in a method declaration with an error is correct.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=210498 )
 */
public void testLocalType5() throws JavaModelException {
	IType typeX = getCompilationUnit("TypeHierarchy", "src", "q9", "X.java").getType("X");
	IType type = typeX.getMethod("foo", new String[] {}).getType("Local", 1);
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertTypesEqual(
		"Unexpected types in hierarchy",
		"java.lang.Object\n" +
		"q9.X\n" +
		"q9.X$Local\n",
		hierarchy.getAllTypes());
}
/*
 * Ensures that a type hierarchy on a member type with subtypes in another project is correct
 * (regression test for bug 101019 RC3: Type Hierarchy does not find implementers/extenders of inner class/interface in other project)
 */
public void testMemberTypeSubtypeDifferentProject() throws CoreException {
	try {
		createJavaProject("P1");
		createFile(
			"/P1/X.java",
			"public class X {\n" +
			"  public class Member {\n" +
			"  }\n" +
			"}"
			);
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "");
		createFile(
			"/P2/Y.java",
			"public class Y extends X.Member {\n" +
			"}"
		);
		IType focus = getCompilationUnit("/P1/X.java").getType("X").getType("Member");
		ITypeHierarchy hierarchy = focus.newTypeHierarchy(null/*no progress*/);
		assertHierarchyEquals(
			"Focus: Member [in X [in X.java [in <default> [in <project root> [in P1]]]]]\n" +
			"Super types:\n" +
			"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"Sub types:\n" +
			"  Y [in Y.java [in <default> [in <project root> [in P2]]]]\n",
			hierarchy);
	} finally {
		deleteProjects(new String[] {"P1", "P2"});
	}
}
/**
 * Ensures that a hierarchy on a type that implements a missing interface is correctly rooted.
 * (regression test for bug 24691 Missing interface makes hierarchy incomplete)
 */
public void testMissingInterface() throws JavaModelException {
	IType type = getCompilationUnit("TypeHierarchy", "src", "p4", "X.java").getType("X");
	ITypeHierarchy hierarchy = null;
	try {
		hierarchy = type.newTypeHierarchy(null);
	} catch (IllegalArgumentException iae) {
		assertTrue("IllegalArgumentException", false);
	}
	assertHierarchyEquals(
		"Focus: X [in X.java [in p4 [in src [in TypeHierarchy]]]]\n" +
		"Super types:\n" +
		"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n",
		hierarchy);
}
/*
 * Ensures that a hierarchy on a binary type that extends a missing class with only binary types in the project
 * is correct.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=213249 )
 */
public void testMissingBinarySuperclass1() throws Exception {
	try {
		IJavaProject project = createJavaProject("P", new String[0], "bin");
		addClassFolder(project, "lib", new String[] {
			"p/X213249.java",
			"package p;\n" +
			"public class X213249 {\n" +
			"}",
			"p/Y213249.java",
			"package p;\n" +
			"public class Y213249 extends X213249 {\n" +
			"}",
			"p/Z213249.java",
			"package p;\n" +
			"public class Z213249 extends Y213249 {\n" +
			"}",
		}, "1.4");
		deleteFile("/P/lib/p/X213249.class");
		IType type = getClassFile("/P/lib/p/Z213249.class").getType();
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
		assertHierarchyEquals(
			"Focus: Z213249 [in Z213249.class [in p [in lib [in P]]]]\n" +
			"Super types:\n" +
			"  Y213249 [in Y213249.class [in p [in lib [in P]]]]\n" +
			"Sub types:\n",
			hierarchy);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a hierarchy on a binary type that extends a missing class with only binary types in the project
 * is correct.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=213249 )
 */
public void testMissingBinarySuperclass2() throws Exception {
	try {
		IJavaProject project = createJavaProject("P", new String[0], "bin");
		addClassFolder(project, "lib", new String[] {
			"p/X213249.java",
			"package p;\n" +
			"public class X213249 {\n" +
			"}",
			"p/Y213249.java",
			"package p;\n" +
			"public class Y213249 extends X213249 {\n" +
			"}",
			"p/Z213249.java",
			"package p;\n" +
			"public class Z213249 extends Y213249 {\n" +
			"}",
		}, "1.4");
		deleteFile("/P/lib/p/X213249.class");
		IType type = getClassFile("/P/lib/p/Z213249.class").getType();
		ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
		assertHierarchyEquals(
			"Focus: Z213249 [in Z213249.class [in p [in lib [in P]]]]\n" +
			"Super types:\n" +
			"  Y213249 [in Y213249.class [in p [in lib [in P]]]]\n" +
			"Sub types:\n",
			hierarchy);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that a potential subtype in a dependent project doesn't appear in the hierarchy
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=169678 )
 */
public void testPotentialSubtypeInDependentProject() throws Exception {
	try {
		createJavaProject("P1", new String[] {""}, new String[] {"JCL_LIB"}, "");
		createFolder("/P1/p1");
		createFile(
			"/P1/p1/X169678.java",
			"package p1;\n" +
			"public class X169678 {\n" +
			"  public static class Y169678 {\n" +
			"  }\n" +
			"}"
		);
		createFile(
			"/P1/p1/Z169678.java",
			"package p1;\n" +
			"public class Z169678 extends X169678.Y169678 {\n" +
			"}"
		);
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "");
		createFolder("/P2/p2");
		createFile(
			"/P2/p2/Y169678.java",
			"package p2;\n" +
			"public class Y169678 {\n" +
			"}\n" +
			"class Z169678 extends Y169678 {\n" +
			"}"
		);
		IType type = getCompilationUnit("/P1/p1/X169678.java").getType("X169678").getType("Y169678");
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
		IType[] allTypes = hierarchy.getAllTypes();
		assertSortedElementsEqual(
			"Unexpected types in hierarchy",
			"Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"Y169678 [in X169678 [in X169678.java [in p1 [in <project root> [in P1]]]]]\n" +
			"Z169678 [in Z169678.java [in p1 [in <project root> [in P1]]]]",
			allTypes);
	} finally {
		deleteProjects(new String[] {"P1", "P2"});
	}
}
/**
 * Ensures that a potential subtype that is not in the classpth is handle correctly.
 * (Regression test for PR #1G4GL9R)
 */
public void testPotentialSubtypeNotInClasspath() throws JavaModelException {
	IJavaProject project = getJavaProject("TypeHierarchy");
	ICompilationUnit cu = getCompilationUnit("TypeHierarchy", "src", "p1", "X.java");
	IType type = cu.getType("X");
	ITypeHierarchy h = type.newTypeHierarchy(project, null);
	IType[] types = h.getSubtypes(type);
	this.assertTypesEqual(
		"Unexpected sub types of X",
		"p1.Y\n",
		types
	);
}
/*
 * Ensures that progress is reported while waiting for indexing to finish
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=210094 )
 */
public void testProgressWhileIndexing() throws CoreException, TimeOutException {
	boolean indexState = isIndexDisabledForTest();
	final WaitingJob job = new WaitingJob();
	try {
		createJavaProject("P");
		this.indexDisabledForTest = true;
		createFile("/P/X210094.java", "public class X210094 {}");
		job.suspend();
		createFile("/P/Y210094.java", "public class Y210094 {}");
		IType type = getCompilationUnit("/P/X210094.java").getType("X210094");
		class ProgressCounter extends TestProgressMonitor {
			int count = 0;
			@Override
			public void subTask(String name) {
				if (this.count++ == 0)
					job.resume();
			}
			@Override
			public boolean isCanceled() {
				return false;
			}
		}
		ProgressCounter counter = new ProgressCounter();
		type.newTypeHierarchy(counter);
		assertTrue("subTask() should be notified", counter.count > 0);
	} finally {
		job.resume();
		deleteProject("P");
		this.indexDisabledForTest = indexState;
	}
}
/*
 * Ensures that a type hierarchy on a region contains all subtypes
 * (regression test for bug 47743 Open type hiearchy problems [type hierarchy])
 */
public void testRegion1() throws JavaModelException {
	IPackageFragment pkg = getPackageFragment("TypeHierarchy", "src", "q1");
	IRegion region = JavaCore.newRegion();
	region.add(pkg);
	ITypeHierarchy h = pkg.getJavaProject().newTypeHierarchy(region, null);
	assertTypesEqual(
		"Unexpected types in hierarchy",
		"java.lang.Object\n" +
		"q1.X\n" +
		"q1.Z\n" +
		"q2.Y\n",
		h.getAllTypes()
	);
}
/*
 * Ensures that a type hierarchy on a region contains all subtypes
 * (regression test for bug 47743 Open type hiearchy problems [type hierarchy])
 */
public void testRegion2() throws JavaModelException {
	IPackageFragment pkg = getPackageFragment("TypeHierarchy", "src", "q2");
	IRegion region = JavaCore.newRegion();
	region.add(pkg);
	ITypeHierarchy h = pkg.getJavaProject().newTypeHierarchy(region, null);
	assertTypesEqual(
		"Unexpected types in hierarchy",
		"java.lang.Object\n" +
		"q1.X\n" +
		"q2.Y\n",
		h.getAllTypes()
	);
}
/*
 * Ensures that a type hierarchy on a region contains anonymous/local types in this region
 * (regression test for bug 48395 Hierarchy on region misses local classes)
 */
public void testRegion3() throws JavaModelException {
	IPackageFragment pkg = getPackageFragment("TypeHierarchy", "src", "p9");
	IRegion region = JavaCore.newRegion();
	region.add(pkg);
	ITypeHierarchy h = pkg.getJavaProject().newTypeHierarchy(region, null);
	assertTypesEqual(
		"Unexpected types in hierarchy",
		"java.lang.Object\n" +
		"p9.X\n" +
		"p9.X$1\n" +
		"p9.X$Y\n",
		h.getAllTypes()
	);
}
public void _testRegion4() throws CoreException {
	try {
		IJavaProject p1 = createJavaProject("P1");
		IJavaProject p2 = createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "");
		IJavaProject p3 = createJavaProject("P3", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "");
		createFile(
			"/P1/X.java",
			"public class X {\n" +
			"}"
		);
		createFile(
			"/P2/Y.java",
			"public class Y extends X {\n" +
			"}"
		);
		createFile(
			"/P3/Z.java",
			"public class Z extends X {\n" +
			"}"
		);
		IRegion region = JavaCore.newRegion();
		region.add(p1);
		region.add(p2);
		region.add(p3);
		ITypeHierarchy hierarchy = JavaCore.newTypeHierarchy(region, null, null);
		assertHierarchyEquals(
			"Focus: <NONE>\n" +
			"Super types of root classes:\n" +
			"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"Sub types of root classes:\n" +
			"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"    Class [in Class.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"    String [in String.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"    Throwable [in Throwable.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"      Error [in Error.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"      Exception [in Exception.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"        CloneNotSupportedException [in CloneNotSupportedException.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"        InterruptedException [in InterruptedException.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"        RuntimeException [in RuntimeException.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"          IllegalMonitorStateException [in IllegalMonitorStateException.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"    X [in X.java [in <default> [in <project root> [in P1]]]]\n" +
			"      Y [in Y.java [in <default> [in <project root> [in P2]]]]\n" +
			"      Z [in Z.java [in <default> [in <project root> [in P3]]]]\n",
			hierarchy);
	} finally {
		deleteProjects(new String[] {"P1", "P2", "P3"});
	}
}
/*
 * Ensures that a type hierarchy on a region that contains a type with a missing super class is correct
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=154865 )
 */
public void testRegion5() throws Exception {
	try {
		createJavaProject("P", new String[] {""}, new String[] {"JCL_LIB"}, "");
		createFolder("/P/p");
		createFile(
			"/P/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"}\n" +
			"class Y extends X {\n" +
			"}"
		);
		createFile(
			"/P/p/Z.java",
			"package p;\n" +
			"public class Z extends Unknown {\n" +
			"}"
		);
		IPackageFragment pkg = getPackage("/P/p");
		IRegion region = JavaCore.newRegion();
		region.add(pkg);
		ITypeHierarchy h = pkg.getJavaProject().newTypeHierarchy(region, null);
		assertHierarchyEquals(
			"Focus: <NONE>\n" +
			"Super types of root classes:\n" +
			"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"  Z [in Z.java [in p [in <project root> [in P]]]]\n" +
			"Sub types of root classes:\n" +
			"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"    X [in X.java [in p [in <project root> [in P]]]]\n" +
			"      Y [in X.java [in p [in <project root> [in P]]]]\n" +
			"  Z [in Z.java [in p [in <project root> [in P]]]]\n",
			h);
	} finally {
		deleteProject("P");
	}
}
/**
 * @bug 150289: [hierarchy] NPE in hierarchy builder when region is empy
 * @test Ensure that no NPE is thrown when IRegion has no associated project
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=150289"
 */
public void testRegion_Bug150289() throws JavaModelException {
	ITypeHierarchy h = this.currentProject.newTypeHierarchy(JavaCore.newRegion(), null);
	assertEquals("Unexpected number of types in hierarchy", 0, h.getAllTypes().length);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=144976
public void testResilienceToMissingBinaries() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL_LIB", "/TypeHierarchy/test144976.jar"}, "bin");
		createFolder("/P/src/tools/");
		createFile(
			"/P/src/tools/DisplayTestResult2.java",
			"pakage tools;\n" +
			"import servlet.*;\n" +
			"public class DisplayTestResult2 extends TmrServlet2 {\n" +
			"}"
		);
		createFolder("/P/src/servlet/");
		createFile(
				"/P/src/servlet/TmrServlet2.java",
				"pakage servlet;\n" +
				"public class TmrServlet2 extends TmrServlet {\n" +
				"}"
			);
		createFile(
				"/P/src/servlet/TmrServlet.java",
				"pakage servlet;\n" +
				"import gk.*;\n" +
				"public class TmrServlet extends GKServlet {\n" +
				"}"
			);
		IType type = getCompilationUnit("P", "src", "tools", "DisplayTestResult2.java").getType("DisplayTestResult2");
		ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
		assertNotNull(hierarchy.getSupertypes(type));
		assertHierarchyEquals(
				"Focus: DisplayTestResult2 [in DisplayTestResult2.java [in tools [in src [in P]]]]\n" +
				"Super types:\n" +
				"  TmrServlet2 [in TmrServlet2.java [in servlet [in src [in P]]]]\n" +
				"    TmrServlet [in TmrServlet.java [in servlet [in src [in P]]]]\n" +
				"      GKServlet [in GKServlet.class [in gk [in /TypeHierarchy/test144976.jar [in P]]]]\n" +
				"Sub types:\n",
			hierarchy);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensures that the focus type is put as a non-resolved type
 * (regression test for bug 92357 ITypeHierarchy#getType() should return an unresolved handle)
 */
public void testResolvedTypeAsFocus() throws CoreException {
	try {
		createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
		String source =
			"public class X {\n" +
			"  Y<String> field;\n" +
			"}\n" +
			"class Y<E> {\n" +
			"}";
		createFile("/P/X.java", source);
		int start = source.indexOf("Y");
		int end = source.indexOf("<String>");
		IJavaElement[] elements = getCompilationUnit("/P/X.java").codeSelect(start, end-start);
		IType focus = (IType) elements[0];
		ITypeHierarchy hierarchy = focus.newTypeHierarchy(null);
		assertElementsEqual(
			"Unexpected focus type in hierarchy",
			"Y [in X.java [in <default> [in <project root> [in P]]]]",
			new IJavaElement[] {hierarchy.getType()},
			true/*show resolved info*/);
	} finally {
		deleteProject("P");
	}
}
/*
 * Ensure that the order of roots is taken into account when a type is present in multiple roots.
 * (regression test for bug 139555 [hierarchy] Opening a class from Type hierarchy will give the wrong one if source and compiled are in defined in project)
 */
public void testRootOrder() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"abc"}, new String[] {"JCL_LIB"}, "bin");
		createFolder("/P/abc/p");
		createFile(
			"/P/abc/p/X.java",
			"package p;\n"+
			"public class X {}"
		);
		createFile(
			"/P/abc/p/Y.java",
			"package p;\n"+
			"public class Y extends X {}"
		);
		addLibrary(project, "lib.jar", "libsrc.zip", new String[] {
			"p/X.java",
			"package p;\n"+
			"public class X {}",
			"p/Y.java",
			"package p;\n"+
			"public class Y extends X {}"
		}, "1.4");
		IType type = getCompilationUnit("/P/abc/p/X.java").getType("X");
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
		assertHierarchyEquals(
			"Focus: X [in X.java [in p [in abc [in P]]]]\n" +
			"Super types:\n" +
			"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"Sub types:\n" +
			"  Y [in Y.java [in p [in abc [in P]]]]\n",
			hierarchy);
	} finally {
		deleteProject("P");
	}
}
/**
 * Ensures that the superclass can be retrieved for a source type's unqualified superclass.
 */
public void testSourceTypeGetSuperclass() throws JavaModelException {
	//unqualified superclass in a source type
	ICompilationUnit cu = getCompilationUnit("TypeHierarchy", "src", "p1", "Y.java");
	IType type = cu.getType("Y");
	ITypeHierarchy h = type.newSupertypeHierarchy(null);
	IType superclass = h.getSuperclass(type);
	assertTrue("Superclass not found for Y", superclass != null);
	assertEquals("Unexpected super class for Y", "X", superclass.getElementName());
}
/**
 * Ensures that the superclass can be retrieved for a source type's superclass when no superclass is specified
 * in the source type.
 */
public void testSourceTypeGetSuperclass2() throws JavaModelException {
	//no superclass specified for a source type
	ICompilationUnit cu = getCompilationUnit("TypeHierarchy", "src", "p1", "X.java");
	IType type = cu.getType("X");
	ITypeHierarchy h = type.newSupertypeHierarchy(null);
	IType superclass = h.getSuperclass(type);
	assertTrue("Superclass not found for X", superclass != null);
	assertEquals("Unexpected super class for X", "Object", superclass.getElementName());
}
/**
 * Ensures that the superclass can be retrieved for a source type's superclass.
 * This type hierarchy is relatively deep.
 */
public void testSourceTypeGetSuperclass3() throws JavaModelException {
	//no superclass specified for a source type
	ICompilationUnit cu = getCompilationUnit("TypeHierarchy", "src", "p1", "Deep.java");
	IType type = cu.getType("Deep");
	ITypeHierarchy h = type.newSupertypeHierarchy(null);
	IType superclass = h.getSuperclass(type);
	assertTrue("Superclass not found for Deep", superclass != null);
	assertEquals("Unexpected super class for Deep", "Z", superclass.getElementName());
}
/**
 * Ensures that the superclass can be retrieved when it is defined
 * in the same compilation unit.
 */
public void testSourceTypeGetSuperclass4() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("TypeHierarchy", "src", "p1", "A.java");
	IType type = cu.getType("A");
	ITypeHierarchy h = type.newSupertypeHierarchy(null);
	IType superclass = h.getSuperclass(type);
	assertTrue("Superclass not found for A", superclass != null);
	assertEquals("Unexpected super class for A", "B", superclass.getElementName());
}
/**
 * Ensures that the superclass can be retrieved when it is defined
 * in the same compilation unit.
 */
public void testSourceRecordTypeGetSuperclass() throws JavaModelException {
	if (!isJRE16) {
		System.err.println("Test "+getName()+" requires a JRE 15");
		return;
	}
	IType type = getCompilationUnit("/TypeHierarchy_16/src/pkg/Rec1.java").getType("Rec1");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	hierarchy.refresh(null);
	IType superclass = hierarchy.getSuperclass(type);
	assertTrue("Superclass not found for Rec1", superclass != null);
	assertEquals("Unexpected super class for Rec1", "Record", superclass.getElementName());
	assertHierarchyEquals(
			"Focus: Rec1 [in Rec1.java [in pkg [in src [in TypeHierarchy_16]]]]\n" +
			"Super types:\n" +
			"  Record [in Record.class [in java.lang [in <module:java.base>]]]\n" +
			"    Object [in Object.class [in java.lang [in <module:java.base>]]]\n" +
			"Sub types:\n",
			hierarchy);
}
/**
 * Ensures that the superinterfaces can be retrieved for a source type's superinterfaces.
 */
public void testSourceTypeGetSuperInterfaces() throws JavaModelException {
	ICompilationUnit cu = getCompilationUnit("TypeHierarchy", "src", "p1", "Y.java");
	IType type = cu.getType("Y");
	ITypeHierarchy h = type.newSupertypeHierarchy(null);
	IType[] superInterfaces = h.getSuperInterfaces(type);
	assertTypesEqual("Unexpected super interfaces for Y",
		"p1.I1\n" +
		"p1.I2\n",
		superInterfaces);
}

/**
 * Ensures that no subclasses exist in a super type hierarchy for the focus type.
 */
public void testSupertypeHierarchyGetSubclasses() throws JavaModelException {
	IType type = getClassFile("TypeHierarchy", getExternalJCLPathString(), "java.lang", "Object.class").getType();
	ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
	IType[] types = hierarchy.getSubclasses(type);
	assertTypesEqual(
		"Unexpected subclasses of Object",
		"",
		types);

	ICompilationUnit cu = getCompilationUnit("TypeHierarchy", "src", "p1", "Y.java");
	type = cu.getType("Y");
	hierarchy = type.newSupertypeHierarchy(null);
	types = hierarchy.getSubclasses(type);
	assertTypesEqual(
		"Unexpected subclasses of Y",
		"",
		types);
}
/**
 * Ensures that no subtypes exist in a super type hierarchy for the focus type.
 */
public void testSupertypeHierarchyGetSubtypes() throws JavaModelException {
	IType type = getClassFile("TypeHierarchy", getExternalJCLPathString(), "java.lang", "Object.class").getType();
	ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
	IType[] types = hierarchy.getSubtypes(type);
	assertTypesEqual(
		"Unexpected subtypes of Object",
		"",
		types);

	ICompilationUnit cu = getCompilationUnit("TypeHierarchy", "src", "p1", "Y.java");
	type = cu.getType("Y");
	hierarchy = type.newSupertypeHierarchy(null);
	types = hierarchy.getSubtypes(type);
	assertTypesEqual(
		"Unexpected subtypes of Y",
		"",
		types);
}
/**
 * Ensures that a super type hierarchy can be created on a working copy.
 * (regression test for bug 3446 type hierarchy: incorrect behavior wrt working copies (1GLDHOA))
 */
public void testSupertypeHierarchyOnWorkingCopy() throws JavaModelException {
	ICompilationUnit cu = this.getCompilationUnit("TypeHierarchy", "src", "wc", "X.java");
	ICompilationUnit workingCopy = null;
	try {
		workingCopy = cu.getWorkingCopy(null);
		workingCopy.createType(
			"class B{\n" +
			"	void m(){\n" +
			"	}\n" +
			"	void f(){\n" +
			"		m();\n" +
			"	}\n" +
			"}\n",
			null,
			true,
			null);
		workingCopy.createType(
			"class A extends B{\n" +
			"	void m(){\n" +
			"	}\n" +
			"}",
			null,
			true,
			null);
		IType typeA = workingCopy.getType("A");
		ITypeHierarchy hierarchy = typeA.newSupertypeHierarchy(null);
		IType typeB = workingCopy.getType("B");
		assertTrue("hierarchy should contain B", hierarchy.contains(typeB));
	} finally {
		if (workingCopy != null) {
			workingCopy.discardWorkingCopy();
		}
	}
}
/*
 * Ensures that creating a hierarchy on a project with classpath problem doesn't throw a NPE
 * (regression test for bug 49809  NPE from MethodVerifier)
 */
public void testSuperTypeHierarchyWithMissingBinary() throws JavaModelException {
	IJavaProject project = getJavaProject("TypeHierarchy");
	IClasspathEntry[] originalClasspath = project.getRawClasspath();
	try {
		int length = originalClasspath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[length+1];
		System.arraycopy(originalClasspath, 0, newClasspath, 0, length);
		newClasspath[length] = JavaCore.newLibraryEntry(new Path("/TypeHierarchy/test49809.jar"), null, null);
		project.setRawClasspath(newClasspath, null);
		ICompilationUnit cu = getCompilationUnit("/TypeHierarchy/src/q3/Z.java");
		IType type = cu.getType("Z");
		ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
		assertHierarchyEquals(
				"Focus: Z [in Z.java [in q3 [in src [in TypeHierarchy]]]]\n" +
				"Super types:\n" +
				"  Y49809 [in Y49809.class [in p49809 [in test49809.jar [in TypeHierarchy]]]]\n" +
				"Sub types:\n",
			hierarchy
		);
	} finally {
		project.setRawClasspath(originalClasspath, null);
	}
}
/*
 * Ensures that a hierarchy where the super type is not visible can still be constructed.
 */
public void testVisibility1() throws JavaModelException {
	IType type = getCompilationUnit("/TypeHierarchy/src/q6/Y.java").getType("Y");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: Y [in Y.java [in q6 [in src [in TypeHierarchy]]]]\n" +
		"Super types:\n" +
		"  NonVisibleClass [in X.java [in q5 [in src [in TypeHierarchy]]]]\n" +
		"    Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n",
		hierarchy
	);
}
/*
 * Ensures that a hierarchy where the super interface is not visible can still be constructed.
 */
public void testVisibility2() throws JavaModelException {
	IType type = getCompilationUnit("/TypeHierarchy/src/q6/Z.java").getType("Z");
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
	assertHierarchyEquals(
		"Focus: Z [in Z.java [in q6 [in src [in TypeHierarchy]]]]\n" +
		"Super types:\n" +
		"  NonVisibleInterface [in X.java [in q5 [in src [in TypeHierarchy]]]]\n" +
		"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n",
		hierarchy
	);
}

/**
 * @bug 186781: StackOverflowError while computing launch button tooltip
 * @test Verify that StackOverflowException does no longer occur with the given test case
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=186781"
 */
public void testBug186781() throws JavaModelException {
	IType type = getCompilationUnit("/TypeHierarchy/src/q186871/X.java").getType("X");
	assertTrue("Type should exist!", type.exists());
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null); // when bug occurred a stack overflow happened here...
	assertHierarchyEquals(
		"Focus: X [in X.java [in q186871 [in src [in TypeHierarchy]]]]\n" +
		"Super types:\n" +
		"  Super [in X.java [in q186871 [in src [in TypeHierarchy]]]]\n" +
		"    Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n",
		hierarchy);
}

/**
 * @bug 215841: [search] Opening Type Hierarchy extremely slow
 * @test Ensure that the non-existing library referenced through a linked resource
 * 	is not indexed on each search request while building the hierarchy
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=215841"
 */
public void testBug215841() throws JavaModelException, CoreException, InterruptedException {
	final String linkedPath = "/TypeHierarchy/linked.jar";
	class LocalProgressMonitor extends TestProgressMonitor {
		int count = 0;
		@Override
		public boolean isCanceled() {
	        return false;
        }
		@Override
		public void subTask(String name) {
			if (name.indexOf("files to index") > 0 && name.indexOf(linkedPath) > 0) {
	        	this.count++;
			}
        }
	}
	IJavaProject project = getJavaProject("TypeHierarchy");
	IClasspathEntry[] originalClasspath = project.getRawClasspath();
	try {
		// Add linked resource to an unknown jar file on the project classpath
		int length = originalClasspath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[length+1];
		System.arraycopy(originalClasspath, 0, newClasspath, 0, length);
		IFile file = getFile(linkedPath);
		file.createLink(new Path(getExternalPath()).append("unknown.jar"), IResource.ALLOW_MISSING_LOCAL, null);
		newClasspath[length] = JavaCore.newLibraryEntry(new Path(linkedPath), null, null);
		project.setRawClasspath(newClasspath, null);
		waitUntilIndexesReady();

		// Build hierarchy of Throwable
		IType type = getClassFile("TypeHierarchy", getExternalJCLPathString(), "java.lang", "Throwable.class").getType();
		LocalProgressMonitor monitor = new LocalProgressMonitor();
		type.newTypeHierarchy(monitor);
		assertEquals("Unexpected indexing of non-existent external jar file while building hierarchy!", 1, monitor.count);
	} finally {
		project.setRawClasspath(originalClasspath, null);
	}
}
/**
 * @bug 254738: NPE in HierarchyResolver.setFocusType
 * @test that a nested method/anonymous sub type is included in the hierarchy when the number of annotations > 10
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=254738"
 */
public void testBug254738() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {}, "bin", "1.5");
		createFolder("/P/src/abc");
		createFile(
			"/P/src/abc/Parent.java",
			"package abc;\n" +
			"public class Parent {\n" +
			"	public void parentmethod() {\n" +
			"   	new Object() {\n" +
			"			void nestedonemethod() {\n" +
			"           	new Object(){\n" +
			"               	public int hashCode() {\n" +
			"                   	return 0; \n" +
			"                   } \n" +
			"				}; \n" +
			"         	}\n" +
			"   	};\n" +
			"	}\n" +
			"}\n" +
			"@Deprecated\n" +
			"class Dep {\n" +
			"        @Deprecated void a() {}\n" +
			"        @Deprecated void b() {}\n" +
			"        @Deprecated void c() {}\n" +
			"        @Deprecated void d() {}\n" +
			"        @Deprecated void e() {}\n" +
			"        @Deprecated void f() {}\n" +
			"        @Deprecated void g() {}\n" +
			"        @Deprecated void h() {}\n" +
			"        @Deprecated void i() {}\n" +
			"        @Deprecated void j() {}\n" +
			"        @Deprecated void k() {}\n" +
			"}"
			);
		IType focus  = getCompilationUnit("/P/src/abc/Parent.java").getType("Parent");
		focus =	focus.getMethod("parentmethod", new String[]{}).getType("", 1).getMethod("nestedonemethod", new String[]{}).
												getType("", 1);
		ITypeHierarchy hierarchy = focus.newTypeHierarchy(null);
		assertHierarchyEquals(
				"Focus: <anonymous #1> [in nestedonemethod() [in <anonymous #1> [in parentmethod() [in Parent [in Parent.java [in abc [in src [in P]]]]]]]]\n" +
				"Super types:\n" +
				"Sub types:\n",
			hierarchy);
	} finally {
		deleteProjects(new String[] {"P"});
	}
}
/**
 * @bug 288698: Can't create type hierarchy for abstract types when they have inline descendants and *.class* in project name
 * @test Ensure that ".class" as a substring of a path name is not interpreted as the ".class" suffix.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=288698"
 */
public void testBug288698() throws JavaModelException {
	IType type = getCompilationUnit("/TypeHierarchy/src288698.classbug/p288698/AbstractBugTest.java").getType("AbstractBugTest");
	assertTrue("Type should exist!", type.exists());
	ITypeHierarchy hierarchy = type.newTypeHierarchy(null); // when bug occurred a StringIndexOutOfBoundsException was thrown here
	assertHierarchyEquals(
		"Focus: AbstractBugTest [in AbstractBugTest.java [in p288698 [in src288698.classbug [in TypeHierarchy]]]]\n" +
		"Super types:\n" +
		"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
		"Sub types:\n" +
		"  <anonymous #1> [in testIt() [in BugTest2Buggy [in BugTest2Buggy.java [in p288698 [in src288698.classbug [in TypeHierarchy]]]]]]\n",
		hierarchy);
}
/**
 * @bug  329663:[type hierarchy] Interfaces duplicated in type hierarchy on two packages from multiple projects
 * @test that when two selected regions contains the same interface, it's not reported twice in the hierarchy.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=329663"
 */
public void _testBug329663() throws JavaModelException, CoreException {
	try {
		IJavaProject p1 = createJavaProject("P1", new String[] {""}, new String[] {"JCL_LIB"}, new String[0], "");
		IJavaProject p2 = createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "");
		createFolder("/P1/p");
		createFile(
				"/P1/p/I.java",
				"package p;\n" +
				"public interface I{}");
		createFile(
				"/P1/p/X.java",
				"package p;\n" +
				"public class X implements I{\n" +
				"}"
		);
		createFolder("/P2/q");
		createFile(
				"/P2/q/Y.java",
				"package q;\n" +
				"import p.*;\n" +
				"public class Y implements I {\n" +
					"}"
		);
		IRegion region = JavaCore.newRegion();
		region.add(p1);
		region.add(p2);
		ITypeHierarchy hierarchy = JavaCore.newTypeHierarchy(region, null, null);
		IType[] types = hierarchy.getRootInterfaces();
		assertTypesEqual("Unexpected super interfaces",
				"java.io.Serializable\n" +
				"p.I\n",
				types);
	}
	finally{
		deleteProject("P1");
		deleteProject("P2");
	}
}
/**
 * @bug  329663:[type hierarchy] Interfaces duplicated in type hierarchy on two packages from multiple projects
 * @test that when two selected regions contains interfaces with same name but different, they are reported individually
 * in the hierarchy.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=329663"
 */
public void _testBug329663a() throws JavaModelException, CoreException {
try {
	IJavaProject p1 = createJavaProject("P1", new String[] {""}, new String[] {"JCL_LIB"}, new String[0], "");
	IJavaProject p2 = createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "");
	createFolder("/P1/p");
	createFile(
			"/P1/p/I.java",
			"package p;\n" +
			"public interface I{}");
	createFile(
			"/P1/p/X.java",
			"package p;\n" +
			"public class X implements I{\n" +
				"}"
	);
	createFolder("/P2/q");
	createFile(
			"/P2/q/I.java",
			"package q;\n" +
			"public interface I{}");
	createFile(
			"/P2/q/Y.java",
			"package q;\n" +
			"public class Y implements I {\n" +
				"}"
	);
	IRegion region = JavaCore.newRegion();
	region.add(p1);
	region.add(p2);
	ITypeHierarchy hierarchy = JavaCore.newTypeHierarchy(region, null, null);
	IType[] types = hierarchy.getRootInterfaces();
	assertTypesEqual("Unexpected super interfaces",
			"java.io.Serializable\n" +
			"p.I\n" +
			"q.I\n",
			types);
}
finally{
	deleteProject("P1");
	deleteProject("P2");
}
}
// Bug 300576 - NPE Computing type hierarchy when compliance doesn't match libraries
// test that a missing java.lang.Enum doesn't cause NPE
public void testBug300576() throws CoreException {
	IJavaProject prj = null;
	try {
		prj = createJavaProject("Bug300576", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin", "1.5");
		createFolder("/Bug300576/src/p");
		createFile("/Bug300576/src/p/Outer.java",
				"package p;\n" +
				"class Outer {\n" +
				"    enum A {\n" +
				"        GREEN, DARK_GREEN, BLACK;\n" +
				"        /** Javadoc of getNext() */\n" +
				"        A getNext() {\n" +
				"            switch (this) {\n" +
				"                case GREEN : return DARK_GREEN;\n" +
				"                case DARK_GREEN : return BLACK;\n" +
				"                case BLACK : return GREEN;\n" +
				"                default : return null;\n" +
				"            }\n" +
				"        }\n" +
				"    }\n" +
				"    {\n" +
				"        A a= A.GREEN.getNext();\n" +
				"    }\n" +
				"}\n");
		IType a = getCompilationUnit("Bug300576", "src", "p", "Outer.java").getType("Outer").getType("A");
		IRegion region = JavaCore.newRegion();
		region.add(getPackageFragmentRoot("Bug300576", "src"));
		ITypeHierarchy hierarchy = prj.newTypeHierarchy(a, region, new NullProgressMonitor());
		assertHierarchyEquals(
				"Focus: A [in Outer [in Outer.java [in p [in src [in Bug300576]]]]]\n" +
				"Super types:\n" +
				"Sub types:\n",
				hierarchy);
	} finally {
		if (prj != null)
			deleteProject(prj);
	}
}
// Bug 300576 - NPE Computing type hierarchy when compliance doesn't match libraries
// test that a bogus java.lang.Enum (non-generic) doesn't cause NPE
public void testBug300576b() throws CoreException {
	IJavaProject prj = null;
	try {
		prj = createJavaProject("Bug300576", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin", "1.5");
		createFolder("/Bug300576/src/p");
		createFolder("/Bug300576/src/java/lang");
		createFile("/Bug300576/src/java/lang/Enum.java",
				"package java.lang;\n" +
				"public class Enum {}\n");
		createFile("/Bug300576/src/p/Outer.java",
				"package p;\n" +
				"class Outer {\n" +
				"    enum A {\n" +
				"        GREEN, DARK_GREEN, BLACK;\n" +
				"        /** Javadoc of getNext() */\n" +
				"        A getNext() {\n" +
				"            switch (this) {\n" +
				"                case GREEN : return DARK_GREEN;\n" +
				"                case DARK_GREEN : return BLACK;\n" +
				"                case BLACK : return GREEN;\n" +
				"                default : return null;\n" +
				"            }\n" +
				"        }\n" +
				"    }\n" +
				"    {\n" +
				"        A a= A.GREEN.getNext();\n" +
				"    }\n" +
				"}\n");
		IType a = getCompilationUnit("Bug300576", "src", "p", "Outer.java").getType("Outer").getType("A");
		IRegion region = JavaCore.newRegion();
		region.add(getPackageFragmentRoot("Bug300576", "src"));
		ITypeHierarchy hierarchy = prj.newTypeHierarchy(a, region, new NullProgressMonitor());
		assertHierarchyEquals(
				"Focus: A [in Outer [in Outer.java [in p [in src [in Bug300576]]]]]\n" +
				"Super types:\n" +
				"Sub types:\n",
				hierarchy);
	} finally {
		if (prj != null)
			deleteProject(prj);
	}
}
//Bug 393192 -- Incomplete type hierarchy with > 10 annotations
public void testBug393192() throws CoreException {
	IJavaProject prj = null;
	try {
		prj = createJavaProject("Bug393192", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin", "1.5");
		createFolder("/Bug393192/src/pullup");
		createFile("/Bug393192/src/pullup/A.java",
				"package pullup;\n" +
				"\n" +
				"class A {\n" +
				"    @Deprecated\n" +
				"    void m0() {\n" +
				"    }\n" +
				"\n" +
				"    @Deprecated\n" +
				"    void m1() {\n" +
				"    }\n" +
				"\n" +
				"    @Deprecated\n" +
				"    void m2() {\n" +
				"    }\n" +
				"\n" +
				"    @Deprecated\n" +
				"    void m3() {\n" +
				"    }\n" +
				"\n" +
				"    @Deprecated\n" +
				"    void m4() {\n" +
				"    }\n" +
				"\n" +
				"    @Deprecated\n" +
				"    void m5() {\n" +
				"    }\n" +
				"\n" +
				"    @Deprecated\n" +
				"    void m6() {\n" +
				"    }\n" +
				"\n" +
				"    @Deprecated\n" +
				"    void m7() {\n" +
				"    }\n" +
				"\n" +
				"    @Deprecated\n" +
				"    void m8() {\n" +
				"    }\n" +
				"\n" +
				"    @Deprecated\n" +
				"    void m9() {\n" +
				"    }\n" +
				"\n" +
				"    /**\n" +
				"     * @param\n" +
				"     */\n" +
				"    @Deprecated\n" +
				"    void m10() {\n" +
				"    }\n" +
				"}\n" +
				"\n");
		createFile("/Bug393192/src/pullup/package-info.java",
				"package pullup;\n" +
				"\n");
		createFile("/Bug393192/src/pullup/PullUpBug.java",
				"package pullup;\n" +
				"\n" +
				"class PullUpBug extends A {\n" +
				"\n" +
				"    void mb() {\n" +
				"        pullUp();\n" +
				"    }\n" +
				"\n" +
				"    // INVOKE Pull Up REFACTORING ON \"pullUp\", or type Hierarchy on A\n" +
				"    private void pullUp() {\n" +
				"    }\n" +
				"}\n");
		IType a = getCompilationUnit("Bug393192", "src", "pullup", "A.java").getType("A");
		ITypeHierarchy hierarchy = a.newTypeHierarchy(new NullProgressMonitor());
		assertHierarchyEquals(
				"Focus: A [in A.java [in pullup [in src [in Bug393192]]]]\n" +
				"Super types:\n" +
				"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
				"Sub types:\n" +
				"  PullUpBug [in PullUpBug.java [in pullup [in src [in Bug393192]]]]\n",
				hierarchy);
	} finally {
		if (prj != null)
			deleteProject(prj);
	}
}
public void testBug436155() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
		createFolder("/P/abc/p");
		addLibrary(project, "lib.jar", "libsrc.zip", new String[] {
			"p/I.java",
			"package p;\n" +
			"public abstract class I {}",
			"p/I2.java",
			"package p;\n" +
			"public abstract class I2 extends I {}",
			"p/Text.java",
			"package p;\n"+
			"public class Text extends I2 {}",
		}, "1.4");

		createFolder("/P/src/q");
		String source = "package q;\n" +
				"import p.Text;\n" +
				"class A {\n" +
				"	Text text = null;\n" +
				"}\n";
		createFile("/P/src/q/A.java", source);

		int start = source.lastIndexOf("Text");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("P/src/q/A.java", source);

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, "Text".length());
		IType focus = (IType) elements[0];
		ITypeHierarchy hierarchy = focus.newTypeHierarchy(null);
		assertHierarchyEquals(
				"Focus: Text [in Text.class [in p [in lib.jar [in P]]]]\n" +
				"Super types:\n" +
				"  I2 [in I2.class [in p [in lib.jar [in P]]]]\n" +
				"    I [in I.class [in p [in lib.jar [in P]]]]\n" +
				"      Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
				"Sub types:\n",
			hierarchy);
	} finally {
		deleteProject("P");
	}
}
public void testBug436139() throws CoreException, IOException {
	IJavaProject prj = null;
	try {
		prj = createJavaProject("Bug436139", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin", "1.8");
		createFolder("/Bug436139/src/p1");
		String iSource = "package p1;\n" +
				"public interface I {\n" +
				"    void foo(A<B> x);\n" +
				"}";
		createFile("/Bug436139/src/p1/I.java", iSource);
		createFile("/Bug436139/src/p1/A.java", "package p1;\n" +"public class A<T> {}");
		createFile("/Bug436139/src/p1/B.java", "package p1;\n" +"public class B {}");


		createFolder("/Bug436139/src/p2");
		String source = "package p2;\n" +
				"import p1.*;\n" +
				"\n" +
				"@SuppressWarnings(\"unused\")\n" +
				"public class X {\n" +
				"\n" +
				"	private Object patternChanged(A<B> x1) {\n" +
				"\n" +
				"		I f1 = new I() {\n" +
				"			public void foo(A<B> x) {}\n" +
				"		};\n" +
				"		return null;\n" +
				"	}\n" +
				"\n" +
				"	private void someOtherMethod() {\n" +
				"		I f2 = (x) -> {	patternChanged(x);};\n" +
				"\n" +
				"		I f3 = new I() {\n" +
				"			public void foo(A<B> x) {}\n" +
				"		};\n" +
				"	}\n" +
				"}\n";
		createFile("/Bug436139/src/p2/X.java", source);

		int start = iSource.lastIndexOf("I");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Bug436139/src/p1/I.java", iSource);

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, "I".length());
		IType focus = (IType) elements[0];
		ITypeHierarchy hierarchy = focus.newTypeHierarchy(null);
		assertHierarchyEquals(
				"Focus: I [in [Working copy] I.java [in p1 [in src [in Bug436139]]]]\n" +
				"Super types:\n" +
				"Sub types:\n" +
				"  <anonymous #1> [in patternChanged(A<B>) [in X [in X.java [in p2 [in src [in Bug436139]]]]]]\n" +
				"  <anonymous #1> [in someOtherMethod() [in X [in X.java [in p2 [in src [in Bug436139]]]]]]\n" +
				"  <lambda #1> [in someOtherMethod() [in X [in X.java [in p2 [in src [in Bug436139]]]]]]\n",
				hierarchy);
	} finally {
		if (prj != null)
			deleteProject(prj);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=469668
public void testBug469668() throws CoreException, IOException {
	IJavaProject project = null;
	try {
		project = createJavaProject("Bug469668", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin", "1.8");
		createFolder("/Bug469668/src/hierarchy");
		StringBuilder buffer = new StringBuilder();
		for (int i = 1; i < 21; i++) {
			String unitName = "I" + i;
			String content = "package hierarchy;\n" +
			"public interface " + unitName + " " + buffer.toString() + " {}";
			createFile("/Bug469668/src/hierarchy/" + unitName + ".java", content);
			if (i == 1) {
				buffer.append(" extends ");
			}
			if (i > 1 && i < 20) buffer.append(", ");
			buffer.append(unitName);
		}

		IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
		IRegion region = JavaCore.newRegion();
		for (IPackageFragmentRoot root: roots) {
			if(root.getRawClasspathEntry().getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				region.add(root);
			}
		}

		ITypeHierarchy hierarchy = JavaCore.newTypeHierarchy(region, null, new NullProgressMonitor());
		IType type = project.findType("hierarchy.I15");
		IType[] supertypes = hierarchy.getAllSupertypes(type);
		assertEquals(14, supertypes.length);

		type = project.findType("hierarchy.I5");
		IType[]	subtypes = hierarchy.getAllSubtypes(type);
		assertEquals("Incorrect number of entries for subtype", 15, subtypes.length);

		int i20Count = 0;
		for (IType t: subtypes) {
			if("hierarchy.I20".equals(t.getFullyQualifiedName())) {
				i20Count++;
			}
		}
		assertEquals("Multiple entries of the same type in the hierarchy", 1, i20Count);
		assertEquals(15, subtypes.length);

	} finally {
		if (project != null)
			deleteProject(project);
	}
}
public void testBug462158() throws CoreException, IOException {
	IJavaProject prj = null;
	try {
		prj = createJavaProject("Bug462158", new String[] {"src"}, new String[] {"JCL18_FULL"}, "bin", "1.8", true);
		createFolder("/Bug462158/src/p1");
		String iSource = "package p1;\n" +
				"import java.util.ArrayList;\n" +
				"public class TestEclipseForEachBug {\n" +
				"    static abstract class MyList extends ArrayList<Object> {\n" +
				"        private static final long serialVersionUID = 784633858339367208L;\n" +
				"    }\n" +
				"    @Deprecated\n" +
				"    public void foo1(){}\n" +
				"    @Deprecated\n" +
				"    public void foo2(){}\n" +
				"    @Deprecated\n" +
				"    public void foo3(){}\n" +
				"    @Deprecated\n" +
				"    public void foo4(){}\n" +
				"    @Deprecated\n" +
				"    public void foo5(){}\n" +
				"    @Deprecated\n" +
				"    public void foo6(){}\n" +
				"    @Deprecated\n" +
				"    public void foo7(){}\n" +
				"    @Deprecated\n" +
				"    public void foo8(){}\n" +
				"    @Deprecated\n" +
				"    public void foo9(){}\n" +
				"    @Deprecated\n" +
				"    public void foo10(){}\n" +
				"    @Deprecated\n" +
				"    public void foo11(){}\n" +
				"    @Deprecated\n" +
				"    public void foo12(){}\n" +
				"    void foo(MyList list) {\n" +
				"        forea\n" +
				"    }\n" +
				"}";
		createFile("/Bug462158/src/p1/TestEclipseForEachBug.java", iSource);

		int start = iSource.indexOf("MyList list");
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Bug462158/src/p1/TestEclipseForEachBug.java", iSource);

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, "MyList".length());
		assertEquals("Incorrect elements", 1, elements.length);
		IType focus = (IType) elements[0];
		ITypeHierarchy hierarchy = focus.newTypeHierarchy(this.workingCopies, null);
		IType[] allSupertypes = hierarchy.getAllSuperclasses(focus);
		assertTypesEqual("Incorrect hierarchy",
				"java.util.ArrayList\n" +
				"java.util.AbstractList\n" +
				"java.util.AbstractCollection\n" +
				"java.lang.Object\n",
				allSupertypes,
				false);
	} finally {
		if (prj != null)
			deleteProject(prj);
	}
}
public void testBug507954_0001() throws JavaModelException, CoreException {
	IJavaProject javaProject = null;
	try {
		String projectName = "507954";
		javaProject = createJavaProject(projectName, new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
		String packA = "/" + projectName + "/src/a/";
		createFolder(packA);
		String fileA = 				"package a;\n" +
				"public abstract class A {\n"+
				"  protected abstract void foo2();\n" +
				"  public void baz2() {\n" +
				"    foo2();\n" +
			"}";
		createFile(packA + "A.java", fileA);
		createFile(
				packA + "B.java",
				"package a;\n" +
				"public class B {\n" +
				"  public void a() {\n" +
				"    new A() {\n" +
				"      @Override\n" +
				"      protected void foo2() {}\n" +
				"    }.baz2();\n" +
				"  }\n" +
				"}"
				);
		createFile(
				packA + "C.java",
				"package a;\n" +
				"public abstract class C {\n" +
				"  protected abstract void foo1();\n" +
				"  public void baz1() {\n" +
				"    foo1();\n" +
				"  }\n" +
				"}"
				);
		String packD = "/" + projectName + "/src/b/";
		createFolder(packD);

		String fileD = "package d;\n" +
				"import a.A;\n" +
				"import a.C;\n" +
				"public final class D {\n" +
				"\n" +
				"	protected void c() {\n" +
				"		new C() {\n" +
				"			@Override\n" +
				"			protected void foo1() {\n" +
				"				a();\n" +
				"				b();\n" +
				"			}\n" +
				"			private void a() {\n" +
				"				new A() {\n" +
				"					@Override\n" +
				"					protected void foo2() {\n" +
				"					}\n" +
				"				}.baz2();\n" +
				"			}\n" +
				"			private void b() {\n" +
				"				new A() {\n" +
				"					@Override\n" +
				"					protected void foo2() {\n" +
				"					}\n" +
				"				}.baz2();\n" +
				"			}\n" +
				"		}.baz1();\n" +
				"	}\n" +
				"\n" +
				"}\n";

		createFile(packA + "D.java", fileD);

		String classA = "A";
		int start = fileA.indexOf(classA);
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(packA + "A.java", fileA);

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, classA.length());
		assertEquals("Incorrect elements", 1, elements.length);
		IType focus = (IType) elements[0];
		ITypeHierarchy hierarchy = focus.newTypeHierarchy(this.workingCopies, null);
		IType[] allSubTypes = hierarchy.getAllSubtypes(focus);
		assertTypesEqual("Incorrect hierarchy",
				"a.B$1\n" +
				"a.D$1$1\n" +
				"a.D$1$2\n",
				allSubTypes,
				true);
	}
	finally{
		if (javaProject != null) deleteProject(javaProject);
	}
}

public void testBug533949() throws CoreException {
	if (!isJRE9) return;
	IJavaProject javaProject1 = null;
	IJavaProject javaProject2 = null;
	try {
		javaProject1 = createJava9Project("mod1");
		String packA = "/mod1/src/a/";
		createFolder(packA);
		createFile(packA + "A.java",
				"package a;\n" +
				"public abstract class A {\n"+
				"}\n");
		createFile("/mod1/src/module-info.java",
				"module mod1 {\n" +
				"	exports a;\n"+
				"}\n");

		javaProject2 = createJava9Project("mod2");
		addClasspathEntry(javaProject2, JavaCore.newProjectEntry(javaProject1.getPath()));
		String packB = "/mod2/src/b/";
		createFolder(packB);
		createFile(packB + "B.java",
				"package b;\n" +
				"public class B extends a.A {\n"+
				"}\n");
		createFile("/mod2/src/module-info.java",
				"module mod2 {\n" +
				"	requires mod1;\n"+
				"}\n");

		waitUntilIndexesReady();

		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(packB + "B.java", true);

		IType focus = javaProject2.findType("b.B");
		ITypeHierarchy hierarchy = focus.newTypeHierarchy(this.workingCopies, null);
		IType[] allSuperTypes = hierarchy.getAllSupertypes(focus);
		assertTypesEqual("Incorrect super hierarchy",
				"a.A\n" +
				"java.lang.Object\n",
				allSuperTypes,
				true);

		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(packA + "A.java", true);

		focus = javaProject1.findType("a.A");
		hierarchy = focus.newTypeHierarchy(this.workingCopies, null);
		IType[] allSubTypes = hierarchy.getAllSubtypes(focus);
		assertTypesEqual("Incorrect sub hierarchy",
				"b.B\n",
				allSubTypes,
				true);
	} finally{
		if (javaProject1 != null) deleteProject(javaProject1);
		if (javaProject2 != null) deleteProject(javaProject2);
	}
}

public void testBug541217() throws CoreException {
    if (!isJRE9) return;
    IJavaProject javaProject1 = null;
    try {
        javaProject1 = createJava9Project("mod1");
        String packA = "/mod1/src/a/";
        createFolder(packA);
        createFile(packA + "A.java",
                        "package a;\n" +
                        "public interface A extends java.sql.Driver{\n"+
                        "}\n");
        createFile("/mod1/src/module-info.java",
                        "module mod1 {\n" +
                        "       requires java.sql;\n"+
                        "       exports a;\n"+
                        "}\n");

        waitUntilIndexesReady();

        IType focus = javaProject1.findType("java.sql.Driver");
        ITypeHierarchy hierarchy = focus.newTypeHierarchy(null);
        IType[] allSubTypes = hierarchy.getAllSubtypes(focus);
        assertTypesEqual("Incorrect sub hierarchy",
                        "a.A\n",
                        allSubTypes,
                        true);
    } finally{
        if (javaProject1 != null) deleteProject(javaProject1);
    }
}
public void testBug425111() throws Exception {
	IJavaProject javaProject1 = null;
	try {
		javaProject1 = createJavaProject("P1", new String[] {"src"}, new String[] {"JCL18_FULL", "/P1/lib.jar"},"bin", "1.8");

		createLibrary(javaProject1, "lib.jar", "lib-src.zip",
				new String[] {
						"javax/tools/JavaFileManager.java",
						"package javax.tools;\n" +
						"public interface JavaFileManager extends AutoCloseable {}\n",
						"javax/tools/ForwardingJavaFileManager.java",
						"package javax.tools;\n" +
						"public class ForwardingJavaFileManager<M extends JavaFileManager> implements JavaFileManager {\n" +
						"	public void close() {}\n" +
						"}\n"
				},
				null,
				"1.8");
		createFolder("/P1/src/p1");
		createFile("/P1/src/p1/T.java",
				"package p1;\n" +
				"import javax.tools.*;\n" +
				"public class T {\n" +
				"	Object test() {\n" +
				"		return new ForwardingJavaFileManager<JavaFileManager>(null) {\n" +
				"		}\n" +
				"	}\n" +
				"}\n");
		waitUntilIndexesReady();
        IType focus = javaProject1.findType("java.lang.AutoCloseable");
        ITypeHierarchy hierarchy = focus.newTypeHierarchy(null);
        IType[] allSubTypes = hierarchy.getAllSubtypes(focus);
        assertTypesEqual("Incorrect sub hierarchy",
        		"java.io.BufferedInputStream\n" +
        				"java.io.BufferedOutputStream\n" +
        				"java.io.BufferedReader\n" +
        				"java.io.BufferedWriter\n" +
        				"java.io.ByteArrayInputStream\n" +
        				"java.io.ByteArrayOutputStream\n" +
        				"java.io.CharArrayReader\n" +
        				"java.io.CharArrayWriter\n" +
        				"java.io.Closeable\n" +
        				"java.io.DataInputStream\n" +
        				"java.io.DataOutputStream\n" +
        				"java.io.FileInputStream\n" +
        				"java.io.FileOutputStream\n" +
        				"java.io.FileReader\n" +
        				"java.io.FileWriter\n" +
        				"java.io.FilterInputStream\n" +
        				"java.io.FilterOutputStream\n" +
        				"java.io.FilterReader\n" +
        				"java.io.FilterWriter\n" +
        				"java.io.InputStream\n" +
        				"java.io.InputStreamReader\n" +
        				"java.io.LineNumberInputStream\n" +
        				"java.io.LineNumberReader\n" +
        				"java.io.ObjectInput\n" +
        				"java.io.ObjectInputStream\n" +
        				"java.io.ObjectOutput\n" +
        				"java.io.ObjectOutputStream\n" +
        				"java.io.OutputStream\n" +
        				"java.io.OutputStreamWriter\n" +
        				"java.io.PipedInputStream\n" +
        				"java.io.PipedOutputStream\n" +
        				"java.io.PipedReader\n" +
        				"java.io.PipedWriter\n" +
        				"java.io.PrintStream\n" +
        				"java.io.PrintWriter\n" +
        				"java.io.PushbackInputStream\n" +
        				"java.io.PushbackReader\n" +
        				"java.io.RandomAccessFile\n" +
        				"java.io.Reader\n" +
        				"java.io.SequenceInputStream\n" +
        				"java.io.StringBufferInputStream\n" +
        				"java.io.StringReader\n" +
        				"java.io.StringWriter\n" +
        				"java.io.Writer\n" +
        				"java.net.DatagramSocket\n" +
        				"java.net.FactoryURLClassLoader\n" +
        				"java.net.MulticastSocket\n" +
        				"java.net.ServerSocket\n" +
        				"java.net.Socket\n" +
        				"java.net.SocketInputStream\n" +
        				"java.net.SocketOutputStream\n" +
        				"java.net.URLClassLoader\n" +
        				"java.nio.channels.AsynchronousByteChannel\n" +
        				"java.nio.channels.AsynchronousChannel\n" +
        				"java.nio.channels.AsynchronousFileChannel\n" +
        				"java.nio.channels.AsynchronousServerSocketChannel\n" +
        				"java.nio.channels.AsynchronousSocketChannel\n" +
        				"java.nio.channels.ByteChannel\n" +
        				"java.nio.channels.Channel\n" +
        				"java.nio.channels.DatagramChannel\n" +
        				"java.nio.channels.FileChannel\n" +
        				"java.nio.channels.FileLock\n" +
        				"java.nio.channels.GatheringByteChannel\n" +
        				"java.nio.channels.InterruptibleChannel\n" +
        				"java.nio.channels.MulticastChannel\n" +
        				"java.nio.channels.NetworkChannel\n" +
        				"java.nio.channels.Pipe$SinkChannel\n" +
        				"java.nio.channels.Pipe$SourceChannel\n" +
        				"java.nio.channels.ReadableByteChannel\n" +
        				"java.nio.channels.ScatteringByteChannel\n" +
        				"java.nio.channels.SeekableByteChannel\n" +
        				"java.nio.channels.SelectableChannel\n" +
        				"java.nio.channels.Selector\n" +
        				"java.nio.channels.ServerSocketChannel\n" +
        				"java.nio.channels.SocketChannel\n" +
        				"java.nio.channels.WritableByteChannel\n" +
        				"java.nio.channels.spi.AbstractInterruptibleChannel\n" +
        				"java.nio.channels.spi.AbstractSelectableChannel\n" +
        				"java.nio.channels.spi.AbstractSelector\n" +
        				"java.nio.file.WatchService\n" +
        				"java.security.DigestInputStream\n" +
        				"java.security.DigestOutputStream\n" +
        				"java.sql.CallableStatement\n" +
        				"java.sql.Connection\n" +
        				"java.sql.PreparedStatement\n" +
        				"java.sql.ResultSet\n" +
        				"java.sql.Statement\n" +
        				"java.util.Formatter\n" +
        				"java.util.Scanner\n" +
        				"java.util.jar.JarFile\n" +
        				"java.util.jar.JarInputStream\n" +
        				"java.util.jar.JarOutputStream\n" +
        				"java.util.stream.AbstractPipeline\n" +
        				"java.util.stream.BaseStream\n" +
        				"java.util.stream.DoublePipeline\n" +
        				"java.util.stream.DoubleStream\n" +
        				"java.util.stream.IntPipeline\n" +
        				"java.util.stream.IntStream\n" +
        				"java.util.stream.LongPipeline\n" +
        				"java.util.stream.LongStream\n" +
        				"java.util.stream.ReferencePipeline\n" +
        				"java.util.stream.Stream\n" +
        				"java.util.zip.CheckedInputStream\n" +
        				"java.util.zip.CheckedOutputStream\n" +
        				"java.util.zip.DeflaterInputStream\n" +
        				"java.util.zip.DeflaterOutputStream\n" +
        				"java.util.zip.GZIPInputStream\n" +
        				"java.util.zip.GZIPOutputStream\n" +
        				"java.util.zip.InflaterInputStream\n" +
        				"java.util.zip.InflaterOutputStream\n" +
        				"java.util.zip.ZipFile\n" +
        				"java.util.zip.ZipInputStream\n" +
        				"java.util.zip.ZipOutputStream\n" +
        				"javax.tools.ForwardingJavaFileManager\n" +
        				"javax.tools.JavaFileManager\n" +
        				"p1.T$1\n",
                        allSubTypes,
                        true);
	} finally {
		if (javaProject1 != null) deleteProject(javaProject1);
	}
}
public void testBug559210() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL_LIB", "lib"}, "bin", "1.7");
		createFolder("/P/src/java/io");
		createFile(
				"/P/src/java/io/Closeable.java",
				"pakage java.io;\n" +
				"public interface Closeable { void close(); }\n");
		createFile(
				"/P/src/java/io/InputStream.java",
				"pakage java.io;\n" +
				"public abstract class InputStream implements Closeable { }\n");

		createFolder("/P/src/p");
		createFile(
			"/P/src/p/ReferenceInputStream.java",
			"pakage p;\n" +
			"public class ReferenceInputStream extends java.io.InputStream {\n" +
			"	private final File reference;\n" +
			"\n" +
			"	public ReferenceInputStream(File reference) {\n" +
			"		this.reference = reference;\n" +
			"	}\n" +
			"\n" +
			"	/* This method should not be called.\n" +
			"	 */\n" +
			"	@Override\n" +
			"	public int read() throws IOException {\n" +
			"		throw new IOException();\n" +
			"	}\n" +
			"\n" +
			"	public File getReference() {\n" +
			"		return reference;\n" +
			"	}\n" +
			"}"
		);
		createFile(
			"/P/src/p/Storage.java",
			"pakage p;\n" +
			"import p.ReferenceInputStream;\n" +
			"public class Storage {\n" +
			"\n" +
			"	public ReferenceInputStream stream;\n" +
			"}"
		);
		getProject("P").build(IncrementalProjectBuilder.FULL_BUILD, null);
		waitForAutoBuild();
		ITypeHierarchy hierarchy = getCompilationUnit("/P/src/p/Storage.java").getTypes()[0].newSupertypeHierarchy(null);
		assertHierarchyEquals(
			"Focus: Storage [in Storage.java [in p [in src [in P]]]]\n" +
			"Super types:\n" +
			"  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString() + "]]]\n" +
			"Sub types:\n",
			hierarchy);
	} finally {
		deleteProject("P");
	}
}
/**
 * @bug 457813: StackOverflowError while computing launch button tooltip
 * @test Verify that StackOverflowException does no longer occur with the given test case
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=457813"
 */
public void testBug457813() throws CoreException {
	try {
		createJavaProject("P", new String[] { "src" }, new String[] { "JCL_LIB", "/TypeHierarchy/test457813.jar" },
				"bin");
		createFolder("/P/src/hierarchy");
		createFile(
				"/P/src/hierarchy/X.java",
				"pakage hierarchy;\n" +
				"public class X extends aspose.b.a.a {\n" +
				"}"
			);
		IType type = getCompilationUnit("P", "src", "hierarchy", "X.java").getType("X");
		assertTrue("Type should exist!", type.exists());
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null); // when bug occurred a stack overflow happened here...
		assertHierarchyEquals(
				"Focus: X [in X.java [in hierarchy [in src [in P]]]]\n" +
				"Super types:\n" +
				"Sub types:\n",
				hierarchy);
	} finally {
		deleteProject("P");
	}
}

public void testBug573450_001() throws CoreException {
	if (!isJRE16) return;
	try {
		IJavaProject proj = createJava16Project("P", new String[] {"src"});
		proj.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		createFolder("/P/src/hierarchy");
		createFile(
				"/P/src/hierarchy/X.java",
				"@SuppressWarnings(\"preview\")\n" +
				"public sealed class X permits X.Y {\n" +
				"	final class Y extends X {}\n" +
				"}"
			);
		IType type = getCompilationUnit("P", "src", "hierarchy", "X.java").getType("X");
		assertTrue("Type should exist!", type.exists());
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null); // when bug occurred a stack overflow happened here...
		assertHierarchyEquals(
				"Focus: X [in X.java [in hierarchy [in src [in P]]]]\n" +
				"Super types:\n" +
				"  Object [in Object.class [in java.lang [in <module:java.base>]]]\n" +
				"Sub types:\n" +
				"  Y [in X [in X.java [in hierarchy [in src [in P]]]]]\n",
				hierarchy);
	} finally {
		deleteProject("P");
	}
}

public void testBug573450_002() throws CoreException {
	if (!isJRE16) return;
	try {
		IJavaProject proj = createJava16Project("P", new String[] {"src"});
		proj.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		createFolder("/P/src/hierarchy");
		createFile(
				"/P/src/hierarchy/Foo.java",
				"@SuppressWarnings(\"preview\")\n" +
				"sealed interface Foo permits Foo.Bar {\n" +
				"	interface Interface {}\n" +
				"	record Bar() implements Foo, Interface {}\n" +
				"}"
			);
		IType type1 = getCompilationUnit("P", "src", "hierarchy", "Foo.java").getType("Foo");
		assertTrue("Type should exist!", type1.exists());
		ITypeHierarchy hierarchy1 = type1.newTypeHierarchy(null);
		assertHierarchyEquals(
				"Focus: Foo [in Foo.java [in hierarchy [in src [in P]]]]\n" +
				"Super types:\n" +
				"Sub types:\n" +
				"  Bar [in Foo [in Foo.java [in hierarchy [in src [in P]]]]]\n",
				hierarchy1);
	} finally {
		deleteProject("P");
	}
}

private void setupQualifierProject() throws Exception {
	IJavaProject projectQ = createJavaProject("TypeHierarchyQ", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
	addClasspathEntry(projectQ, getJRTLibraryEntry());
	addClasspathEntry(projectQ, JavaCore.newProjectEntry(getProject("TypeHierarchy15").getFullPath()));
	addLibraryEntry(projectQ, Paths.get(getSourceWorkspacePath(), "TypeHierarchy", "test57007.jar").toFile().getAbsolutePath(), false);

	// source file for index qualifier tests
	createFile(
			"/TypeHierarchyQ/src/Q1.java",
			"public class Q1<E> extends util.ArrayList<E> {\n" +
			"}"
	);
	createFile(
			"/TypeHierarchyQ/src/Q2.java",
			"public class Q2<E> implements util.List<E> {\n" +
			"}"
	);
	createFile(
			"/TypeHierarchyQ/src/Q3.java",
			"public class Q3 {\n" +
			"	public util.List<String> listOf() {\n" +
			"		return new util.List(){};\n" +
			"	}\n"+
			"	private class Q3List implements util.List {\n" +
			"	}\n"+
			"}"
	);
	createFile(
			"/TypeHierarchyQ/src/Q4.java",
			"public class Q4 {\n" +
			"	public Runnable job() {\n" +
			"		return new Runnable(){\n" +
			"			public void run(){}" +
			"		};\n" +
			"	}\n"+
			"	private class Q4Job implements Runnable {\n" +
			"			public void run(){}" +
			"	}\n"+
			"	public java.util.function.Function<String, String> func() {\n" +
			"		return i -> {\n" +
			"			return \"i\";"+
			"		};\n" +
			"	}\n"+
			"}"
	);

	createFolder("/TypeHierarchyQ/src/p1");
	createFile(
			"/TypeHierarchyQ/src/p1/COuter.java",
			"package p1;\n" +
			"public class COuter {\n" +
			"	protected class Inner {}\n" +
			"}"
	);

	createFolder("/TypeHierarchyQ/src/p2");
	createFile(
			"/TypeHierarchyQ/src/p2/Middle.java",
			"package p2;\n" +
			"import p1.COuter;\n" +
			"public class Middle extends COuter {}\n"
	);

	IJavaProject projectR = createJavaProject("TypeHierarchyR", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
	addClasspathEntry(projectR, JavaCore.newProjectEntry(getProject("TypeHierarchyQ").getFullPath()));
	addClasspathEntry(projectR, getJRTLibraryEntry());
	createFolder("/TypeHierarchyR/src/p3");
	createFile(
			"/TypeHierarchyR/src/p3/Final.java",
			"package p3;" +
			"public class Final extends p2.Middle {\n"+
			"	private class FinalInner extends Inner {}\n"+
			"	private void exec() {\n"+
			"		new Thread(()-> {});\n"+
			"	}\n"+
			"}\n"
	);

}

private void deleteQualifierProject() throws CoreException {
	deleteProject("TypeHierarchyQ");
	deleteProject("TypeHierarchyR");
}

public void testIndexQualificationFQNReferences() throws Exception {
	setupQualifierProject();
	waitUntilIndexesReady();
	try {
		IType type = getPackageFragmentRoot("/TypeHierarchy15/lib15.jar").getPackageFragment("util")
				.getOrdinaryClassFile("List.class").getType();
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
		assertHierarchyEquals("Focus: List [in List.class [in util [in lib15.jar [in TypeHierarchy15]]]]\n"
				+ "Super types:\n"
				+ "Sub types:\n"
				+ "  <anonymous #1> [in listOf() [in Q3 [in Q3.java [in <default> [in src [in TypeHierarchyQ]]]]]]\n"
				+ "  ArrayList [in ArrayList.class [in util [in lib15.jar [in TypeHierarchy15]]]]\n"
				+ "    Q1 [in Q1.java [in <default> [in src [in TypeHierarchyQ]]]]\n"
				+ "    X [in X.java [in <default> [in src [in TypeHierarchy15]]]]\n"
				+ "    Y [in Y.java [in <default> [in src [in TypeHierarchy15]]]]\n"
				+ "  Q2 [in Q2.java [in <default> [in src [in TypeHierarchyQ]]]]\n"
				+ "  Q3List [in Q3 [in Q3.java [in <default> [in src [in TypeHierarchyQ]]]]]\n"
				+ "  X [in X.java [in <default> [in src [in TypeHierarchy15]]]]\n"
				+ "  Y [in Y.java [in <default> [in src [in TypeHierarchy15]]]]\n"
				, hierarchy);
	} finally {
		deleteQualifierProject();
	}
}

public void testIndexQualificationJavaLangReferences() throws Exception {
	setupQualifierProject();
	waitUntilIndexesReady();
	try {
		IType type = getJavaProject("TypeHierarchyQ").findType("java.lang.Runnable");
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null);

		String actual = hierarchy.toString();
		assertTrue("Actual (<anonymous #1> [in job()]): ".concat(actual), actual.contains("<anonymous #1> [in job() [in Q4 [in Q4.java [in <default> [in src [in TypeHierarchyQ]]]]]]"));
		assertTrue("Actual (Q4Job): ".concat(actual), actual.contains("Q4Job [in Q4 [in Q4.java [in <default> [in src [in TypeHierarchyQ]]]]]"));
	} finally {
		deleteQualifierProject();
	}
}

public void testIndexQualificationLambdaReferences_AsReturnTypes() throws Exception {
	setupQualifierProject();
	waitUntilIndexesReady();
	try {
		IType type = getJavaProject("TypeHierarchyQ").findType("java.util.function.Function");
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null);

		String actual = hierarchy.toString();
		assertTrue("Actual (<lambda #1> [in func()]): ".concat(actual), actual.contains("<lambda #1> [in func() [in Q4 [in Q4.java [in <default> [in src [in TypeHierarchyQ]]]]]]"));
	} finally {
		deleteQualifierProject();
	}
}

public void testIndexQualificationLambdaReferences_AsParameters() throws Exception {
	setupQualifierProject();
	waitUntilIndexesReady();
	try {
		IType type = getJavaProject("TypeHierarchyR").findType("java.lang.Runnable");
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null);

		String actual = hierarchy.toString();
		assertTrue("Actual <lambda #1> [in exec()]): ".concat(actual), actual.contains("<lambda #1> [in exec() [in Final [in Final.java [in p3 [in src [in TypeHierarchyR]]]]]]"));
	} finally {
		deleteQualifierProject();
	}
}

public void testIndexQualificationInnerClassInheritence() throws Exception {
	setupQualifierProject();
	waitUntilIndexesReady();
	try {
		IType type = getJavaProject("TypeHierarchyQ").findType("p1.COuter.Inner");
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null);

		assertHierarchyEquals("Focus: Inner [in COuter [in COuter.java [in p1 [in src [in TypeHierarchyQ]]]]]\n"
				+ "Super types:\n"
				+ "  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.8") + "]]]\n"
				+ "Sub types:\n"
				+ "  FinalInner [in Final [in Final.java [in p3 [in src [in TypeHierarchyR]]]]]\n"
				, hierarchy);
	} finally {
		deleteQualifierProject();
	}
}

public void testIndexQualificationBinaryNestedSubTypes() throws Exception {
	setupQualifierProject();
	waitUntilIndexesReady();
	try {
		IType type = getJavaProject("TypeHierarchyQ").findType("meta.Future");
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null);

		String jarPath = Paths.get(getSourceWorkspacePath(), "TypeHierarchy", "test57007.jar").toFile().getAbsolutePath();
		assertHierarchyEquals("Focus: Future [in Future.class [in meta [in " + jarPath + "]]]\n"
				+ "Super types:\n"
				+ "  Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.8") + "]]]\n"
				+ "Sub types:\n"
				+ "  <anonymous> [in Future$1.class [in meta [in " + jarPath + "]]]\n"
				+ "  AsyncFuture [in Future$AsyncFuture.class [in meta [in " + jarPath + "]]]\n"
				+ "    NestedAsyncFuture [in Future$NestedAsyncFuture.class [in meta [in " + jarPath + "]]]\n"
				+ "    NestedNestedAsyncFuture [in Future$NestedAsyncFuture$NestedNestedAsyncFuture.class [in meta [in " + jarPath + "]]]\n"
				+ "      <anonymous> [in Future$NestedAsyncFuture$NestedNestedAsyncFuture$1.class [in meta [in " + jarPath + "]]]\n"
				+ "  AsyncFuture [in FutureX$AsyncFuture.class [in meta [in " + jarPath + "]]]\n"
				+ "    NestedAsyncFuture [in FutureX$NestedAsyncFuture.class [in meta [in " + jarPath + "]]]\n"
				+ "    NestedNestedAsyncFuture [in FutureX$NestedAsyncFuture$NestedNestedAsyncFuture.class [in meta [in " + jarPath + "]]]\n"
				, hierarchy);


	} finally {
		deleteQualifierProject();
	}
}

public void testIndexQualificationBinaryNestedSubTypes_SearchForNestedSuperType() throws Exception {
	setupQualifierProject();
	waitUntilIndexesReady();
	try {
		IType type = getJavaProject("TypeHierarchyQ").findType("meta.Future.AsyncFuture");
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null);

		String jarPath = Paths.get(getSourceWorkspacePath(), "TypeHierarchy", "test57007.jar").toFile().getAbsolutePath();
		assertHierarchyEquals("Focus: AsyncFuture [in Future$AsyncFuture.class [in meta [in " + jarPath + "]]]\n"
				+ "Super types:\n"
				+ "  Future [in Future.class [in meta [in "+ jarPath + "]]]\n"
				+ "    Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.8") + "]]]\n"
				+ "Sub types:\n"
				+ "  NestedAsyncFuture [in Future$NestedAsyncFuture.class [in meta [in " + jarPath + "]]]\n"
				+ "  NestedNestedAsyncFuture [in Future$NestedAsyncFuture$NestedNestedAsyncFuture.class [in meta [in " + jarPath + "]]]\n"
				+ "    <anonymous> [in Future$NestedAsyncFuture$NestedNestedAsyncFuture$1.class [in meta [in " + jarPath + "]]]\n"
				, hierarchy);


	} finally {
		deleteQualifierProject();
	}
}

/**
 * @bug GitHub 269: Wrong type hierarchy computed for types with cyclic static imports.
 * @see "https://github.com/eclipse-jdt/eclipse.jdt.core/issues/269"
 */
public void testBugGh269() throws Exception {
	String testProjectName = "TypeHierarchyBugGh269";
	IJavaProject projectQ = createJavaProject(testProjectName, new String[] {"src"}, new String[] {"JCL11_LIB"}, "bin", "11");
	try {
		addClasspathEntry(projectQ, getJRTLibraryEntry());
		waitUntilIndexesReady();
		createFolder("/TypeHierarchyBugGh269/src/p269");
		createFolder("/TypeHierarchyBugGh269/src/p269/internal");
		createFile(
			"/TypeHierarchyBugGh269/src/p269/Gh269TestInterface.java",
			"package p269;\n" +
			"public interface Gh269TestInterface {\n" +
			"    void foo();\n" +
			"}"
		);
		String subtypeContents =
				"package p269.internal;\n" +
				"import static p269.internal.Gh269SomeClass.*;\n" +
				"import p269.Gh269TestInterface;\n" +
				"public class Gh269TestImplementation implements Gh269TestInterface {\n" +
				"    @Override public void foo() { someStaticMethod(); }\n" +
				"    public static void someOtherStaticMethod() { System.out.println(\"hello world\"); }\n" +
				"}";
		createFile(
				"/TypeHierarchyBugGh269/src/p269/internal/Gh269TestImplementation.java",
				subtypeContents
				);
		createFile(
				"/TypeHierarchyBugGh269/src/p269/internal/Gh269SomeClass.java",
				"package p269.internal;\n" +
				"import static p269.internal.Gh269TestImplementation.*;\n" +
				"public class Gh269SomeClass implements Comparable<Gh269SomeClass> {\n" +
				"    public static void someStaticMethod() { someOtherStaticMethod(); }\n" +
				"    @Override public int compareTo(Gh269SomeClass o) { return 0; }\n" +
				"}"
				);
		waitUntilIndexesReady();

		int indexStart = subtypeContents.indexOf("implements Gh269TestInterface");
		assertNotEquals("Failed to find implements statement", -1, indexStart);
		indexStart += "implements ".length();
		int length = "Gh269TestInterface".length();

		ICompilationUnit compilationUnit = getCompilationUnit("/TypeHierarchyBugGh269/src/p269/internal/Gh269TestImplementation.java");
		IJavaElement[] selectedTypes = compilationUnit.codeSelect(indexStart, length);
		assertEquals("Expected only 1 type to be selected but got: " + Arrays.toString(selectedTypes), 1, selectedTypes.length);
		IType type = (IType) selectedTypes[0];
		assertTrue("Type should exist!", type.exists());
		ITypeHierarchy hierarchy = type.newTypeHierarchy(new NullProgressMonitor());
		assertHierarchyEquals(
			"Focus: Gh269TestInterface [in Gh269TestInterface.java [in p269 [in src [in TypeHierarchyBugGh269]]]]\n" +
			"Super types:\n" +
			"Sub types:\n" +
			"  Gh269TestImplementation [in Gh269TestImplementation.java [in p269.internal [in src [in TypeHierarchyBugGh269]]]]\n",
			hierarchy);
	} finally {
		deleteProject(testProjectName);
	}
}

}
