/*******************************************************************************
 * Copyright (c) 2026 Christoph Läubrich and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;

/**
 * Tests that computing a type hierarchy (see
 * {@code org.eclipse.jdt.internal.core.hierarchy.HierarchyBuilder}) resolves super types as
 * seen from the release specific source folder the focus type lives in, honoring a release
 * specific {@code module-info.java}.
 *
 * See https://github.com/eclipse-jdt/eclipse.jdt.core/pull/4534#discussion_r3660886418
 */
public class HierarchyMultiReleaseTests extends AbstractJavaModelTests {

	static {
//		TESTS_NAMES = new String[] { "testSupertypeWindowInRelease17" };
	}

	public HierarchyMultiReleaseTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(HierarchyMultiReleaseTests.class);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		IJavaProject project = createJava9ProjectWithJREAttributes("HierarchyMR",
				new String[] { "src", "src17", "src21" }, null, "21");
		IClasspathEntry[] classpath = project.getRawClasspath();
		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry entry = classpath[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				if (entry.getPath().toString().endsWith("src17")) {
					classpath[i] = JavaCore.newSourceEntry(entry.getPath(), null, null, null,
							new IClasspathAttribute[] {
									JavaCore.newClasspathAttribute(IClasspathAttribute.RELEASE, "17") });
				} else if (entry.getPath().toString().endsWith("src21")) {
					classpath[i] = JavaCore.newSourceEntry(entry.getPath(), null, null, null,
							new IClasspathAttribute[] {
									JavaCore.newClasspathAttribute(IClasspathAttribute.RELEASE, "21") });
				}
			}
		}
		project.setRawClasspath(classpath, new NullProgressMonitor());
		project.setOption(JavaCore.COMPILER_RELEASE, JavaCore.ENABLED);

		createFolder("/HierarchyMR/src/p");
		createFolder("/HierarchyMR/src17/p");
		createFolder("/HierarchyMR/src21/p");

		// base module requires nothing, so neither java.desktop nor java.xml types are
		// accessible: Test extends/implements plain java.lang types only.
		createFile("/HierarchyMR/src/module-info.java", """
				module MRhierarchy {
				}
				""");
		createFile("/HierarchyMR/src/p/Test.java", """
				package p;
				public class Test {
				}
				""");

		// release 17 requires java.desktop (which transitively reads java.xml): the focus
		// type extends java.awt.Window, only reachable through the release 17 module-info.
		createFile("/HierarchyMR/src17/module-info.java", """
				module MRhierarchy {
					requires java.desktop;
				}
				""");
		createFile("/HierarchyMR/src17/p/Test.java", """
				package p;
				public class Test extends java.awt.Window {
				}
				""");

		// release 21 requires java.xml: the focus type implements org.w3c.dom.Element,
		// only reachable through the release 21 module-info.
		createFile("/HierarchyMR/src21/module-info.java", """
				module MRhierarchy {
					requires java.xml;
				}
				""");
		createFile("/HierarchyMR/src21/p/Test.java", """
				package p;
				public class Test implements org.w3c.dom.Element {
				}
				""");
	}

	@Override
	public void tearDownSuite() throws Exception {
		deleteProject("HierarchyMR");
		super.tearDownSuite();
	}

	private IType getFocusType(String unitPath) throws Exception {
		ICompilationUnit unit = getCompilationUnit(unitPath);
		IType type = unit.getType("Test");
		assertTrue("Test type does not exist in " + unitPath, type.exists());
		return type;
	}

	// java.awt.Window is reachable in src17 via 'requires java.desktop': the supertype
	// hierarchy of the release 17 focus type must resolve it as the direct superclass.
	public void testSupertypeWindowInRelease17() throws Exception {
		IType type = getFocusType("/HierarchyMR/src17/p/Test.java");
		ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
		IType superclass = hierarchy.getSuperclass(type);
		assertNotNull("Superclass of release 17 Test not resolved", superclass);
		assertEquals("java.awt.Window", superclass.getFullyQualifiedName());
	}

	// org.w3c.dom.Element is reachable in src21 via 'requires java.xml': the supertype
	// hierarchy of the release 21 focus type must resolve it as a super interface.
	public void testSuperinterfaceElementInRelease21() throws Exception {
		IType type = getFocusType("/HierarchyMR/src21/p/Test.java");
		ITypeHierarchy hierarchy = type.newSupertypeHierarchy(new NullProgressMonitor());
		IType[] superInterfaces = hierarchy.getSuperInterfaces(type);
		boolean found = false;
		for (IType superInterface : superInterfaces) {
			if ("org.w3c.dom.Element".equals(superInterface.getFullyQualifiedName())) {
				found = true;
			}
		}
		assertTrue("org.w3c.dom.Element not found among super interfaces of release 21 Test", found);
	}
}
