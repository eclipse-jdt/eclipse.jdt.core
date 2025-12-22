/*******************************************************************************
 * Copyright (c) 2026 Advantest and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Advantest - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;

public class TypeHierarchyLambdaTests extends ModifyingResourceTests {

public static Test suite() {
	return buildModelTestSuite(TypeHierarchyLambdaTests.class, BYTECODE_DECLARATION_ORDER);
}
public TypeHierarchyLambdaTests(String name) {
	super(name);
	this.displayName = true;
}

@Override
protected void setUp() throws Exception {
	this.indexDisabledForTest = false;
	super.setUp();
}

public void testAnonymousTypesWithoutWorkingCopy() throws CoreException {
	String a =
			"""
			package test1;
			public abstract class A {
				public A() {}
				public void m() {}
			}
			""";
	String b =
			"""
			package test1;
			public class B {
				interface R {
					public void r();
				}
				public void t() {
					R r = () -> new A() {};
				}
			}
			""";
	String projectName = "WithSubtypeInLambda";
	try {
		createJavaProject(projectName, new String[] { "src" }, new String[] { "JCL_21_LIB" }, "bin", "21");
		IPackageFragmentRoot sourceFolder = getPackageFragmentRoot(projectName, "src");
		IPackageFragment pack1 = sourceFolder.createPackageFragment("test1", false, null);
		ICompilationUnit cuA = pack1.getCompilationUnit("A.java");
		ICompilationUnit cuB = pack1.getCompilationUnit("B.java");
		IType type = cuA.createType(a, null, true, null);
		cuB.createType(b, null, true, null);
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
		IType[] allTypes= hierarchy.getAllTypes();
		List<IType> anonymousTypes = new ArrayList<>();
		for (IType hierarchyType : allTypes) {
			assertTrue("Expected hierarchy type to exist: " + hierarchyType, hierarchyType.exists());
			if (hierarchyType.isAnonymous()) {
				anonymousTypes.add(hierarchyType);
			}
		}
		Set<String> anonymousTypeNames = anonymousTypes.stream().map(IType::getFullyQualifiedName).collect(Collectors.toSet());
		Set<String> expected = Set.of("test1.B$1$1");
		assertEquals("Unexpected anonymous types in hierarchy", expected, anonymousTypeNames);
		for (IType hierarchyType : anonymousTypes) {
			ITypeHierarchy supertypeHierarchy = hierarchyType.newSupertypeHierarchy(null);
			IType[] superTypes = supertypeHierarchy.getAllTypes();
			Set<String> typeNames = Stream.of(superTypes).map(IType::getFullyQualifiedName).collect(Collectors.toSet());
			assertTrue("Super types must contain 'test1.A': " + typeNames, typeNames.contains("test1.A"));
		}
	} finally {
		deleteProject(projectName);
	}
}

public void testAnonymousTypesWithWorkingCopy() throws CoreException {
	String a =
			"""
			package test1;
			public abstract class A {
				public A() {}
				public void m() {}
			}
			""";
	String b =
			"""
			package test1;
			public class B {
				interface R {
					public void r();
				}
				public void t() {
					R r = () -> new A() {};
				}
			}
			""";
	String projectName = "WithSubtypeInLambda";
	ICompilationUnit cuB = null;
	try {
		createJavaProject(projectName, new String[] { "src" }, new String[] { "JCL_21_LIB" }, "bin", "21");
		IPackageFragmentRoot sourceFolder = getPackageFragmentRoot(projectName, "src");
		IPackageFragment pack1 = sourceFolder.createPackageFragment("test1", false, null);
		ICompilationUnit cuA = pack1.getCompilationUnit("A.java");
		cuB = pack1.getCompilationUnit("B.java");
		IType type = cuA.createType(a, null, true, null);
		cuB.createType(b, null, true, null);
		cuB.becomeWorkingCopy(null);
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
		IType[] allTypes= hierarchy.getAllTypes();
		List<IType> anonymousTypes = new ArrayList<>();
		for (IType hierarchyType : allTypes) {
			assertTrue("Expected hierarchy type to exist: " + hierarchyType, hierarchyType.exists());
			if (hierarchyType.isAnonymous()) {
				anonymousTypes.add(hierarchyType);
			}
		}
		Set<String> anonymousTypeNames = anonymousTypes.stream().map(IType::getFullyQualifiedName).collect(Collectors.toSet());
		Set<String> expected = Set.of("test1.B$1$1");
		assertEquals("Unexpected anonymous types in hierarchy", expected, anonymousTypeNames);
		for (IType hierarchyType : anonymousTypes) {
			ITypeHierarchy supertypeHierarchy = hierarchyType.newSupertypeHierarchy(null);
			IType[] superTypes = supertypeHierarchy.getAllTypes();
			Set<String> typeNames = Stream.of(superTypes).map(IType::getFullyQualifiedName).collect(Collectors.toSet());
			assertTrue("Super types must contain 'test1.A': " + typeNames, typeNames.contains("test1.A"));
		}
	} finally {
		if (cuB != null) {
			cuB.discardWorkingCopy();
		}
		deleteProject(projectName);
	}
}

public void testAnonymousTypesBaseInSameClass() throws CoreException {
	String b =
			"""
			package test1;
			public class B {
				interface V {
					public void v();
				}
				interface R {
					public void r();
				}
				public void t() {
					R r = () -> new A() {};
				}
			}
			""";
	String projectName = "WithSubtypeInLambda";
	try {
		IJavaProject project = createJavaProject(projectName, new String[] { "src" }, new String[] { "JCL_21_LIB" }, "bin", "21");
		IPackageFragmentRoot sourceFolder = getPackageFragmentRoot(projectName, "src");
		IPackageFragment pack1 = sourceFolder.createPackageFragment("test1", false, null);
		ICompilationUnit cuB = pack1.getCompilationUnit("B.java");
		cuB.createType(b, null, true, null);
		IType type = project.findType("test1.B$V");
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
		IType[] allTypes= hierarchy.getAllTypes();
		Set<String> typeNames = Stream.of(allTypes).map(IType::getFullyQualifiedName).collect(Collectors.toSet());
		Set<String> expected = Set.of(Object.class.getName(), "test1.B$V", "test1.B$1$1");
		assertEquals("Unexpected types in hierarchy", expected, typeNames);
	} finally {
		deleteProject(projectName);
	}
}

public void testTemporary() throws CoreException {
	String a =
			"""
			package test1;
			public abstract class A {
				public A() {}
				public void m() {}
			}
			""";
	String b =
			"""
			package test1;
			public class B {
				interface R {
					public void r();
				}
				public void t() {
					R r = () -> new A() {};
				}
			}
			""";
	String projectName = "WithSubtypeInLambda";
	try {
		createJavaProject(projectName, new String[] { "src" }, new String[] { "JCL_21_LIB" }, "bin", "21");
		IPackageFragmentRoot sourceFolder = getPackageFragmentRoot(projectName, "src");
		IPackageFragment pack1 = sourceFolder.createPackageFragment("test1", false, null);
		ICompilationUnit cuA = pack1.getCompilationUnit("A.java");
		ICompilationUnit cuB = pack1.getCompilationUnit("B.java");
		IType type = cuA.createType(a, null, true, null);
		IType B = cuB.createType(b, null, true, null);
		ITypeHierarchy hierarchy = type.newTypeHierarchy(null);
		IType[] allTypes= hierarchy.getAllTypes();
		IType anonymousTypeFromHierarchy = null;
		for (IType hierarchyType : allTypes) {
			if (hierarchyType.isAnonymous()) {
				anonymousTypeFromHierarchy = hierarchyType;
				break;
			}
		}
		System.out.println("From hierarchy, parent: " + anonymousTypeFromHierarchy.getParent());
		IJavaElement anonymousTypeFromType = null;
		Deque<IJavaElement> q = new LinkedList<>();
		q.add(B);
		while (!q.isEmpty()) {
			IJavaElement e = q.removeFirst();
			if (e instanceof IType t && t.isAnonymous()) {
				anonymousTypeFromType = e;
				break;
			}
			if (e instanceof IParent p) {
				q.addAll(Arrays.asList(p.getChildren()));
			}
		}
		System.out.println("From type, parent: " + anonymousTypeFromType.getParent());
	} finally {
		deleteProject(projectName);
	}
}
}
