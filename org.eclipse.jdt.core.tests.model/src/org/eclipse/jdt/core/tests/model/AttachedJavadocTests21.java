/*******************************************************************************
 * Copyright (c) 2024 Andrey Loskutov (loskutov@gmx.de) and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov (loskutov@gmx.de) - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IParent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import junit.framework.Test;

/**
 * Test case for the modern (Java 17 - 21) generated external javadoc being
 * properly consumed by JDT in absence of attached sources to external binary
 * classes
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class AttachedJavadocTests21 extends ModifyingResourceTests {

	/**
	 * Project that uses javadoc that was *manually* created from
	 * JavadocProducer21 project. TODO Ideally we should be able to generate
	 * javadoc from JavadocProducer21 for every supported Java version on the
	 * fly.
	 */
	private static final String JAVADOCS_PROJECT = "JavadocConsumer21";

	public static Test suite() {
		return buildModelTestSuite(AttachedJavadocTests21.class, BYTECODE_DECLARATION_ORDER);
	}

	private IJavaProject project;
	private IPackageFragmentRoot root;

	public AttachedJavadocTests21(String name) {
		super(name);
	}

	/**
	 * Imports existing project with external library and attached javadoc
	 */
	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();

		this.project = setUpJavaProject(JAVADOCS_PROJECT, "17");
		Map options = this.project.getOptions(true);
		options.put(JavaCore.TIMEOUT_FOR_PARAMETER_NAME_FROM_ATTACHED_JAVADOC, "2000");
		this.project.setOptions(options);

		IPackageFragmentRoot[] roots = this.project.getAllPackageFragmentRoots();
		for (final IPackageFragmentRoot packageFragmentRoot : roots) {
			switch (packageFragmentRoot.getKind()) {
			case IPackageFragmentRoot.K_BINARY:
				if (!packageFragmentRoot.isExternal()) {
					this.root = packageFragmentRoot;
					break;
				}
			}
		}
		assertNotNull("Should find one external library", this.root);
	}

	public void testJava21javadoc() throws JavaModelException {
		IPackageFragment packageFragment = this.root.getPackageFragment("package1");
		assertNotNull("Should not be null", packageFragment);
		IClassFile[] classFiles = packageFragment.getAllClassFiles();
		assertTrue("Should find at least one class in " + packageFragment, classFiles.length > 0);
		for (IClassFile classFile : classFiles) {
			inspectJavadoc(classFile);
		}
	}

	private void inspectJavadoc(IClassFile classFile) throws JavaModelException {
		IJavaElement[] children = classFile.getChildren();
		assertTrue("Should find at least one element in " + classFile, children.length > 0);
		for (IJavaElement child : children) {
			inspectJavadoc(child);
		}
	}

	private void inspectJavadoc(IJavaElement child) throws JavaModelException {
		assertAttachedjavadoc(child);
		if (child instanceof IParent parent) {
			IJavaElement[] children = parent.getChildren();
			for (IJavaElement elt : children) {
				inspectJavadoc(elt);
			}
		}
	}

	// Set to true for debugging new javadoc versions
	private static final boolean PRINT_INSTEAD_OF_FAIL = false;

	private void assertAttachedjavadoc(IJavaElement elt) throws JavaModelException {
		if (elt instanceof IMember member) {
			int flags = member.getFlags();
			if (Flags.isPackageDefault(flags) || Flags.isPrivate(flags)) {
				return;
			}
		}
		String javadoc = elt.getAttachedJavadoc(new NullProgressMonitor());
		if (javadoc == null || javadoc.isBlank()) {
			if (PRINT_INSTEAD_OF_FAIL) {
				System.err.println("No javadoc found for " + elt + "\n");
			} else {
				fail("Should have javadoc: " + elt);
			}
		} else {
			if (PRINT_INSTEAD_OF_FAIL) {
				System.out.println("Some javadoc found for " + elt);
			}
		}
		String elementName = elt.getElementName();
		String message = "Should match name: '" + elementName + "' but was: " + javadoc + " for " + elt;
		if (javadoc == null || !javadoc.contains(elementName)) {
			if (PRINT_INSTEAD_OF_FAIL) {
				System.err.println("\n" + message + "\n");
			} else {
				fail(message);
			}
		} else {
			if (PRINT_INSTEAD_OF_FAIL) {
				System.out.println("Matching javadoc for " + elementName + " found");
			}
		}
	}

	@Override
	public void tearDownSuite() throws Exception {
		this.deleteProject(JAVADOCS_PROJECT);
		this.root = null;
		this.project = null;
		super.tearDownSuite();
	}
}
