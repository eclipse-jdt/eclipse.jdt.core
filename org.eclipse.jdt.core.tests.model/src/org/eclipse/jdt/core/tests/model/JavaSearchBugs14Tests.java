/*******************************************************************************
 * Copyright (c) 2019, 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.ReferenceMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.TypeReferenceMatch;
import org.eclipse.jdt.internal.core.LocalVariable;

import junit.framework.Test;

public class JavaSearchBugs14Tests extends AbstractJavaSearchTests {

	static {
		//	 org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
		//		TESTS_NUMBERS = new int[] { 19 };
		//		TESTS_RANGE = new int[] { 1, -1 };
		//		TESTS_NAMES = new String[] {"testBug542559_001"};
	}

	public JavaSearchBugs14Tests(String name) {
		super(name);
		this.endChar = "";
	}

	public static Test suite() {
		return buildModelTestSuite(JavaSearchBugs14Tests.class, BYTECODE_DECLARATION_ORDER);
	}

	class TestCollector extends JavaSearchResultCollector {
		public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
			super.acceptSearchMatch(searchMatch);
		}
	}

	class ReferenceCollector extends JavaSearchResultCollector {
		protected void writeLine() throws CoreException {
			super.writeLine();
			ReferenceMatch refMatch = (ReferenceMatch) this.match;
			IJavaElement localElement = refMatch.getLocalElement();
			if (localElement != null) {
				this.line.append("+[");
				if (localElement.getElementType() == IJavaElement.ANNOTATION) {
					this.line.append('@');
					this.line.append(localElement.getElementName());
					this.line.append(" on ");
					this.line.append(localElement.getParent().getElementName());
				} else {
					this.line.append(localElement.getElementName());
				}
				this.line.append(']');
			}
		}
	}

	class TypeReferenceCollector extends ReferenceCollector {
		protected void writeLine() throws CoreException {
			super.writeLine();
			TypeReferenceMatch typeRefMatch = (TypeReferenceMatch) this.match;
			IJavaElement[] others = typeRefMatch.getOtherElements();
			int length = others==null ? 0 : others.length;
			if (length > 0) {
				this.line.append("+[");
				for (int i=0; i<length; i++) {
					IJavaElement other = others[i];
					if (i>0) this.line.append(',');
					if (other.getElementType() == IJavaElement.ANNOTATION) {
						this.line.append('@');
						this.line.append(other.getElementName());
						this.line.append(" on ");
						this.line.append(other.getParent().getElementName());
					} else {
						this.line.append(other.getElementName());
					}
				}
				this.line.append(']');
			}
		}
	}

	protected IJavaProject setUpJavaProject(final String projectName, String compliance, boolean useFullJCL) throws CoreException, IOException {
		// copy files in project from source workspace to target workspace
		IJavaProject setUpJavaProject = super.setUpJavaProject(projectName, compliance, useFullJCL);
		setUpJavaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		return setUpJavaProject;
	}

	IJavaSearchScope getJavaSearchScope() {
		return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearchBugs")});
	}

	IJavaSearchScope getJavaSearchScopeBugs(String packageName, boolean addSubpackages) throws JavaModelException {
		if (packageName == null) return getJavaSearchScope();
		return getJavaSearchPackageScope("JavaSearchBugs", packageName, addSubpackages);
	}

	public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
		if (this.wcOwner == null) {
			this.wcOwner = new WorkingCopyOwner() {};
		}
		return getWorkingCopy(path, source, this.wcOwner);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "14");
	}

	public void tearDownSuite() throws Exception {
		deleteProject("JavaSearchBugs");
		super.tearDownSuite();
	}

	protected void setUp () throws Exception {
		super.setUp();
		this.resultCollector = new TestCollector();
		this.resultCollector.showAccuracy(true);
	}



	//Bug 561048 code selection
	public void testBug561048_029() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class cl {\n"+
						"public cl() {\n"+
						"	method1();\n"+
						"}\n"+
						"private void method1() {\n"+
						"	String y= this.toString();\n"+
						"	if (y instanceof String /*here*/yz) {\n"+
						"	      System.out.println(yz.toLowerCase());\n"+
						"	      System.out.println(yz.charAt(0));\n"+
						"	}\n"+
						"}\n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection = "/*here*/yz";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue((elements[0] instanceof LocalVariable));
	}

	public void testBug561048_030() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class cl {\n"+
						"public cl() {\n"+
						"	method1();\n"+
						"}\n"+
						"private void method1() {\n"+
						"	String y= this.toString();\n"+
						"	if (y instanceof String /*here*/yz) {\n"+
						"	      System.out.println(yz.toLowerCase());\n"+
						"	}\n"+
						"}\n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection = "/*here*/yz";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue((elements[0] instanceof LocalVariable));
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults(
				"src/X.java void cl.method1() [yz] EXACT_MATCH");
	}

	public void testBug561048_031() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class cl {\n"+
						"public cl() {\n"+
						"	method1();\n"+
						"}\n"+
						"private void method1() {\n"+
						"	String y= this.toString();\n"+
						"	if (y instanceof String /*here*/yz) {\n"+
						"	      System.out.println(yz.toLowerCase());\n"+
						"	      System.out.println(yz.charAt(0));\n"+
						"	}\n"+
						"}\n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection = "/*here*/yz";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue((elements[0] instanceof LocalVariable));
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults(
				"src/X.java void cl.method1() [yz] EXACT_MATCH\n" +
				"src/X.java void cl.method1() [yz] EXACT_MATCH");
	}

	// mix Instance of pattern variable  and record
	public void testBug561048_032() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record cl() {\n"+
						"public cl{\n"+
						"	method1();\n"+
						"}\n"+
						"private void method1() {\n"+
						"	String y= this.toString();\n"+
						"	if (y instanceof String /*here*/yz) {\n"+
						"	      System.out.println(yz.toLowerCase());\n"+
						"	      System.out.println(yz.charAt(0));\n"+
						"	}\n"+
						"}\n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection = "/*here*/yz";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue((elements[0] instanceof LocalVariable));
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults(
				"src/X.java void cl.method1() [yz] EXACT_MATCH\n" +
				"src/X.java void cl.method1() [yz] EXACT_MATCH");
	}
	public void testBug561132_033() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public class cl {\n"+
						"public cl() {\n"+
						"	method1();\n"+
						"}\n"+
						"private void method1() {\n"+
						"	String y= this.toString();\n"+
						"	if (y instanceof String yz) {\n"+
						"	      System.out.println(/*here*/yz.toLowerCase());\n"+
						"	}\n"+
						"}\n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection = "/*here*/yz";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue((elements[0] instanceof LocalVariable));

	}
}
