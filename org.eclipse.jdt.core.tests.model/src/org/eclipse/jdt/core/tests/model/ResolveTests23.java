/*******************************************************************************
 * Copyright (c) 2024 GK Software SE and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan herrmann - initial implementation
 *******************************************************************************/

package org.eclipse.jdt.core.tests.model;

import java.io.IOException;
import junit.framework.Test;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;

public class ResolveTests23 extends AbstractJavaModelTests {
	ICompilationUnit wc = null;

static {
//	 TESTS_NAMES = new String[] { "testModuleImport3_firstSegment" };
	// TESTS_NUMBERS = new int[] { 124 };
	// TESTS_RANGE = new int[] { 16, -1 };
}
public static Test suite() {
	return buildModelTestSuite(ResolveTests23.class);
}
public ResolveTests23(String name) {
	super(name);
}
@Override
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	return super.getWorkingCopy(path, source, this.wcOwner);
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();
	this.currentProject = setUpJavaProject("Resolve", "23", false);
	this.currentProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	waitUntilIndexesReady();
}
@Override
protected void setUp() throws Exception {
	super.setUp();
	this.wcOwner = new WorkingCopyOwner(){};
}
@Override
public void tearDownSuite() throws Exception {
	deleteProject("Resolve");
	super.tearDownSuite();
}

@Override
protected void tearDown() throws Exception {
	if (this.wc != null) {
		this.wc.discardWorkingCopy();
	}
	super.tearDown();
}

public void testModuleImport1() throws IOException, CoreException {
	String jarName = "mod.one.jar";
	addModularLibrary(this.currentProject, jarName, "mod.one.zip",
			new String[] {
				"module-info.java",
				"module mod.one {}"
			},
			"23");
	try {
		this.wc = getWorkingCopy("/Resolve/src/p/X.java",
				"""
				package p;
				import module mod.one;
				class X{}
				""");
		String str = this.wc.getSource();
		String selection = "one";
		int start = str.indexOf(selection);
		int length = selection.length();
		IJavaElement[] elements = this.wc.codeSelect(start, length);
		assertElementsEqual(
			"Unexpected elements",
			"mod.one [in module-info.class [in <default> [in mod.one.jar [in Resolve]]]]",
			elements
		);
	} finally {
		removeLibrary(this.currentProject, jarName, null);
	}
}
public void testModuleImport2_prefix() throws IOException, CoreException {
	String jarName = "mod.one.jar";
	addModularLibrary(this.currentProject, jarName, "mod.one.zip",
			new String[] {
				"module-info.java",
				"module mod.one {}"
			},
			"23");
	try {
		this.wc = getWorkingCopy("/Resolve/src/p/X.java",
				"""
				package p;
				import module mod.o;
				class X{}
				""");
		String str = this.wc.getSource();
		String selection = "mod.o";
		int start = str.indexOf(selection);
		int length = selection.length();
		IJavaElement[] elements = this.wc.codeSelect(start, length);
		assertElementsEqual(
			"Unexpected elements",
			"mod.one [in module-info.class [in <default> [in mod.one.jar [in Resolve]]]]",
			elements
		);
	} finally {
		removeLibrary(this.currentProject, jarName, null);
	}
}
public void testModuleImport3_firstSegment() throws IOException, CoreException {
	// assert that selecting only the first segment of a module name still uses the full name for module lookup
	String jarName1 = "mod.one.jar";
	addModularLibrary(this.currentProject, jarName1, "mod.one.zip",
			new String[] {
				"module-info.java",
				"module mod.one {}"
			},
			"23");
	// second module for potential ambiguity of name part "mod":
	String jarName2 = "mod.two.jar";
	addModularLibrary(this.currentProject, jarName2, "mod.two.zip",
			new String[] {
				"module-info.java",
				"module mod.two {}"
			},
			"23");
	try {
		this.wc = getWorkingCopy("/Resolve/src/p/X.java",
				"""
				package p;
				import module mod.one;
				class X{}
				""");
		String str = this.wc.getSource();
		String selection = "mod.one";
		int start = str.indexOf(selection);
		int length = "mod".length(); // <<== only first segment
		IJavaElement[] elements = this.wc.codeSelect(start, length);
		assertElementsEqual(
			"Unexpected elements",
			"mod.one [in module-info.class [in <default> [in mod.one.jar [in Resolve]]]]",
			elements
		);
	} finally {
		removeLibrary(this.currentProject, jarName1, null);
		removeLibrary(this.currentProject, jarName2, null);
	}
}
}
