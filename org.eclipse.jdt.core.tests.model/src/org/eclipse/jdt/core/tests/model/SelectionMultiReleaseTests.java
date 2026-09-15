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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;

/**
 * Tests that code selection (Hover / Open Declaration) in a multi-release modular
 * project resolves types as seen from the release specific source folder the selected
 * unit lives in.
 *
 * See https://github.com/eclipse-jdt/eclipse.jdt.core/pull/4534#issuecomment-4743290623
 * where selecting a type that is only accessible through a release specific
 * {@code module-info.java} produced multiple (duplicate) results because selection
 * always resolved against the base {@code module-info.java}.
 */
public class SelectionMultiReleaseTests extends AbstractJavaModelTests {

	static {
//		TESTS_NAMES = new String[] { "testSelectElementInRelease21" };
	}

	public SelectionMultiReleaseTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(SelectionMultiReleaseTests.class);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		IJavaProject project = createJava9ProjectWithJREAttributes("SelectionMR",
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

		createFolder("/SelectionMR/src/p");
		createFolder("/SelectionMR/src17/p");
		createFolder("/SelectionMR/src21/p");

		// base module requires nothing
		createFile("/SelectionMR/src/module-info.java", """
				module MRmodular {
				}
				""");
		createFile("/SelectionMR/src/p/Test.java", """
				package p;
				public class Test {
					java.awt.Window w;
					org.w3c.dom.Element element;
				}
				""");

		// release 17 requires java.desktop (which transitively reads java.xml)
		createFile("/SelectionMR/src17/module-info.java", """
				module MRmodular {
					requires java.desktop;
				}
				""");
		createFile("/SelectionMR/src17/p/Test.java", """
				package p;
				public class Test {
					java.awt.Window w;
					org.w3c.dom.Element element;
				}
				""");

		// release 21 requires java.xml
		createFile("/SelectionMR/src21/module-info.java", """
				module MRmodular {
					requires java.xml;
				}
				""");
		createFile("/SelectionMR/src21/p/Test.java", """
				package p;
				public class Test {
					java.awt.Window w;
					org.w3c.dom.Element element;
				}
				""");
	}

	@Override
	public void tearDownSuite() throws Exception {
		deleteProject("SelectionMR");
		super.tearDownSuite();
	}

	private IType assertSingleType(String unitPath, String reference, String selection, String expectedFqn)
			throws Exception {
		ICompilationUnit unit = getCompilationUnit(unitPath);
		String source = unit.getSource();
		int referenceStart = source.indexOf(reference);
		assertTrue("reference '" + reference + "' not found in " + unitPath, referenceStart >= 0);
		int start = source.indexOf(selection, referenceStart);
		IJavaElement[] elements = unit.codeSelect(start, selection.length());
		StringBuilder details = new StringBuilder();
		for (IJavaElement element : elements) {
			details.append('\n').append(element);
		}
		assertEquals("Expected exactly one selection result but got: " + details, 1, elements.length);
		assertTrue("Selection result is not a type: " + elements[0], elements[0] instanceof IType);
		IType type = (IType) elements[0];
		assertEquals("Unexpected resolved type", expectedFqn, type.getFullyQualifiedName());
		return type;
	}

	// org.w3c.dom.Element is reachable in src21 via 'requires java.xml': selecting it must
	// resolve to exactly that single type and not offer all the JDK types named 'Element'.
	public void testSelectElementInRelease21() throws Exception {
		assertSingleType("/SelectionMR/src21/p/Test.java", "org.w3c.dom.Element", "Element",
				"org.w3c.dom.Element");
	}

	// java.awt.Window is reachable in src17 via 'requires java.desktop'.
	public void testSelectWindowInRelease17() throws Exception {
		assertSingleType("/SelectionMR/src17/p/Test.java", "java.awt.Window", "Window",
				"java.awt.Window");
	}

	// java.desktop reads java.xml transitively, so org.w3c.dom.Element is reachable in src17 too.
	public void testSelectElementInRelease17() throws Exception {
		assertSingleType("/SelectionMR/src17/p/Test.java", "org.w3c.dom.Element", "Element",
				"org.w3c.dom.Element");
	}
}
