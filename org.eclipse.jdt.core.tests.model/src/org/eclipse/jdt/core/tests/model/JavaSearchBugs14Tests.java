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
import java.util.Map;

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
import org.eclipse.jdt.internal.core.ResolvedSourceField;
import org.eclipse.jdt.internal.core.ResolvedSourceType;
import org.eclipse.jdt.internal.core.SourceField;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.SourceType;

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

	// 0 reference of the component in compact constructor
	public void testBug558812_001() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			search("comp_", FIELD, REFERENCES);
			assertSearchResults("");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	//1 reference of the component in compact constructor
	public void testBug558812_002() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	  comp_=11;\n" +
						"	}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			search("comp_", FIELD, REFERENCES);
			assertSearchResults("src/X.java Point(int) [comp_] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	//2 reference of the component in compact constructor
	public void testBug558812_003() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	  comp_=11;\n" +
						"	  this.comp_=11;\n" +
						"	}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			search("comp_", FIELD, REFERENCES);
			assertSearchResults("src/X.java Point(int) [comp_] EXACT_MATCH\n" +
					"src/X.java Point(int) [comp_] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	//3 reference of the component in compact constructor
	public void testBug558812_004() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	  comp_=11;\n" +
						"	  this.comp_=comp_;\n" +
						"	}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			search("comp_", FIELD, REFERENCES);
			assertSearchResults("src/X.java Point(int) [comp_] EXACT_MATCH\n" +
					"src/X.java Point(int) [comp_] EXACT_MATCH\n" +
					"src/X.java Point(int) [comp_] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	//0 reference of the component in canonical constructor
	public void testBug558812_005() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record Point(int comp_) { \n" +
						"	public Point (int a) {\n" +
						"	}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			search("comp_", FIELD, REFERENCES);
			assertSearchResults("");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	//1 reference of the component in canonical constructor
	public void testBug558812_006() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record Point(int comp_) { \n" +
						"	public Point (int a) {\n" +
						"	  comp_=11;\n" +
						"	}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			search("comp_", FIELD, REFERENCES);
			assertSearchResults("src/X.java Point(int) [comp_] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	//1 reference of the component in canonical constructor - part2
	public void testBug558812_007() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record Point(int comp_) { \n" +
						"	public Point (int a) {\n" +
						"	 // comp_=11;\n" +
						"	 this.comp_=a;\n" +
						"	}\n" +
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			search("comp_", FIELD, REFERENCES);
			assertSearchResults("src/X.java Point(int) [comp_] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	//1 reference of the component in compact constructor - clashing method parameter
	public void testBug558812_008() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	  comp_=11;\n" +
						"	}\n" +
						"public void method ( int comp_) {	  \n"+
						"} \n"+
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			search("comp_", FIELD, REFERENCES);
			assertSearchResults("src/X.java Point(int) [comp_] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}


	//1 reference of the component in compact constructor - clashing method's local variable
	public void testBug558812_009() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	  comp_=11;\n" +
						"	}\n" +
						"public void method ( ) {	  \n"+
						"	  int comp_=11;\n" +
						"} \n"+
						"}\n"
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			search("comp_", FIELD, REFERENCES);
			assertSearchResults("src/X.java Point(int) [comp_] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	// more test case
	// test case of comp_ selection in compact constructor
	// selection  - select record type
	public void testBug558812_010() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record /* here*/Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	  comp_=11;\n" +
						"	}\n" +
						"public void method ( ) {	  \n"+
						"	  int  compp_=11;\n" +
						"} \n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection = "/* here*/Point";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		//TODO: check if record
	}

	//selection  - select local field in a method in record
	public void testBug558812_011() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record /* here*/Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	  comp_=11;\n" +
						"	}\n" +
						"public void method ( ) {	  \n"+
						"	  int  /* here*/compp_=11;\n" +
						"} \n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection =  "/* here*/compp_";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof LocalVariable);

	}

	//selection  - select local field in a compact constructor in record
	public void testBug558812_012() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record /* here*/Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	  /* here*/comp_=11;\n" +
						"	}\n" +
						"public void method ( ) {	  \n"+
						"	  int compp_=11;\n" +
						"} \n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection =  "/* here*/comp_";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof LocalVariable);

	}

	//selection  - select local field in a compact constructor in record
	public void testBug558812_013() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record /* here*/Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	  /* here*/this.comp_=11;\n" +
						"	}\n" +
						"public void method ( ) {	  \n"+
						"	  int  compp_=11;\n" +
						"} \n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection =  "/* here*/this.comp_";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof ResolvedSourceField);
	}

	//selection  - select  field in a method in record ( using this)
	public void testBug558812_014() throws CoreException {

		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record /* here*/Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	  /* here*/comp_=11;\n" +
						"	}\n" +
						"public void method ( ) {	  \n"+
						"	  int  compp_=/* here*/this.comp_;\n" +
						"} \n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection =  "/* here*/this.comp_";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof ResolvedSourceField);
	}

	//selection  - select  field in a method in record ( without this)
	public void testBug558812_015() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record /* here*/Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	  comp_=11;\n" +
						"	}\n" +
						"public void method ( ) {	  \n"+
						"	  int  compp_=/* here2*/comp_;\n" +
						"} \n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection =  "/* here2*/comp_";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof ResolvedSourceField);
		ResolvedSourceField sm = (ResolvedSourceField)elements[0];
		IJavaElement parent = sm.getParent();
		SourceType st = (SourceType)parent;
		assertTrue(st.isRecord());
	}

	public void testBug558812_016() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	  /*here2*/comp_=11;\n" +
						"	}\n" +
						"public void method ( ) {	  \n"+
						"	  int  compp_=comp_;\n" +
						"	  int  compp2_=comp_;\n" +
						"} \n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection =  "/*here2*/comp_";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof LocalVariable);
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults(
				"src/X.java Point(int) [comp_] EXACT_MATCH\n" +
						"src/X.java void Point.method() [comp_] EXACT_MATCH\n" +
				"src/X.java void Point.method() [comp_] EXACT_MATCH");
	}

	public void testBug558812_017() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	  /*here2*/comp_=11;\n" +
						"	}\n" +
						"public void method ( ) {	  \n"+
						"	  int  compp_=comp_;\n" +
						"	  int  compp2_=comp_;\n" +
						"	  int  compp3_=this.comp_;\n" +
						"} \n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection =  "/*here2*/comp_";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof LocalVariable);
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults(
				"src/X.java Point(int) [comp_] EXACT_MATCH\n" +
						"src/X.java void Point.method() [comp_] EXACT_MATCH\n" +
						"src/X.java void Point.method() [comp_] EXACT_MATCH\n" +
				"src/X.java void Point.method() [comp_] EXACT_MATCH");
	}

	//selection  - select CC type
	public void testBug558812_018() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record Point(int comp_) { \n" +
						"	public /* here*/Point  {\n" +
						"	  comp_=11;\n" +
						"	}\n" +
						"	public Point  (int a, int b){\n" +
						"	  comp_=11;\n" +
						"	}\n" +
						"public void method ( ) {	  \n"+
						"	  int  compp_=11;\n" +
						"} \n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection = "/* here*/Point";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof SourceMethod);
		SourceMethod sm = (SourceMethod)elements[0];
		IJavaElement parent = sm.getParent();
		SourceType st = (SourceType)parent;
		assertTrue(st.isRecord());
	}

	//selection  - select CC type and search
	public void testBug558812_019() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record Point(int comp_) { \n" +
						"	public /* here*/Point  {\n" +
						"	  comp_=11;\n" +
						"	}\n" +
						"	public Point  (int a, int b){\n" +
						"	  comp_=11;\n" +
						"	}\n" +
						"public void method ( ) {	  \n"+
						"	  int  compp_=11;\n" +
						"	  Point p = new Point(1) ;\n" +
						"} \n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection = "/* here*/Point";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof SourceMethod);
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults(
				"src/X.java void Point.method() [new Point(1)] EXACT_MATCH");
	}

	//selection  - select non-CC type and search
	public void testBug558812_020() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	  comp_=11;\n" +
						"	}\n" +
						"	public /* here*/Point  (int a, int b){\n" +
						"	  comp_=11;\n" +
						"	}\n" +
						"public void method ( ) {	  \n"+
						"	  int  compp_=11;\n" +
						"	  Point p = new Point(1) ;\n" +
						"} \n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection = "/* here*/Point";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof SourceMethod);
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults("");
	}

	//selection  - select non-CC type and search- 2
	public void testBug558812_021() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	  comp_=11;\n" +
						"	}\n" +
						"	public /* here*/Point  (int a, int b){\n" +
						"	  comp_=11;\n" +
						"	}\n" +
						"public void method ( ) {	  \n"+
						"	  int  compp_=11;\n" +
						"	  Point p = new Point(1) ;\n" +
						"	  Point p = new Point(1,2) ;\n" +
						"} \n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection = "/* here*/Point";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof SourceMethod);
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults(
				"src/X.java void Point.method() [new Point(1,2)] EXACT_MATCH");
	}

	//selection  - select CC type and search- 2
	public void testBug558812_022() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record Point(int comp_) { \n" +
						"	public /* here*/Point  {\n" +
						"	  comp_=11;\n" +
						"	}\n" +
						"	public Point  (int a, int b){\n" +
						"	  comp_=11;\n" +
						"	}\n" +
						"public void method ( ) {	  \n"+
						"	  int  compp_=11;\n" +
						"	  Point p = new Point(1) ;\n" +
						"	  Point p = new Point(1,2) ;\n" +
						"} \n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection = "/* here*/Point";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof SourceMethod);
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults(
				"src/X.java void Point.method() [new Point(1)] EXACT_MATCH");
	}

	//selection  - select local field in a compact constructor in record and search1
	public void testBug558812_23() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record /* here*/Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	  /* here*/comp_=11;\n" +
						"	}\n" +
						"public void method ( ) {	  \n"+
						"	  int compp_=11;\n" +
						"} \n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection =  "/* here*/comp_";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof LocalVariable);
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults(
				"src/X.java Point(int) [comp_] EXACT_MATCH");
	}

	//selection  - select local field in a compact constructor in record and search2
	public void testBug558812_24() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record /* here*/Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	  /* here*/comp_=11;\n" +
						"	}\n" +
						"public void method ( ) {	  \n"+
						"	   comp_=11;\n" +
						"} \n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection =  "/* here*/comp_";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof LocalVariable);
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults(
				"src/X.java Point(int) [comp_] EXACT_MATCH\n" +
				"src/X.java void Point.method() [comp_] EXACT_MATCH");
	}

	//selection  - select local field in a compact constructor in record and search3
	public void testBug558812_25() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record /* here*/Point(int comp_) { \n" +
						"	public Point  {\n" +
						"	  /* here*/comp_=11;\n" +
						"	}\n" +
						"public void method ( ) {	  \n"+
						"	   comp_=11;\n" +
						"	   int a=this.comp_;\n" +
						"} \n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection =  "/* here*/comp_";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof LocalVariable);
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults(
				"src/X.java Point(int) [comp_] EXACT_MATCH\n" +
						"src/X.java void Point.method() [comp_] EXACT_MATCH\n"+
				"src/X.java void Point.method() [comp_] EXACT_MATCH");
	}

	//selection  - select record in another file
	public void testBug558812_26() throws CoreException {
		IJavaProject project1 = createJavaProject("JavaSearchBugs14", new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "14");
		try {
			Map<String, String> options = project1.getOptions(false);
			options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_14);
			project1.setOptions(options);
			project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			project1.open(null);
			createFolder("/JavaSearchBugs14/src/pack11");
			String fileContent = "package pack11;\n" +
					"public record X11() {\n" +
					"}\n";
			String fileContent2 = "package pack11;\n" +
					"public class X12  {\n" +
					"/*here*/X11 p =null;\n"+
					"}\n";

			createFile("/JavaSearchBugs14/src/pack11/X11.java", fileContent);
			createFile("/JavaSearchBugs14/src/pack11/X12.java",fileContent2);
			ICompilationUnit unit = getCompilationUnit("/JavaSearchBugs14/src/pack11/X12.java");
			String x11 = "/*here*/X11";
			int start = fileContent2.indexOf(x11);
			IJavaElement[] elements = unit.codeSelect(start, x11.length());
			assertTrue(elements.length ==1);
			assertTrue(elements[0] instanceof ResolvedSourceType);
			boolean record = ((ResolvedSourceType)elements[0]).isRecord();
			assertTrue(record);
		} finally {
			deleteProject(project1);
		}
	}

	//selection  - select parameter in normal constructor matching component name
	public void testBug558812_27() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record X(int a) {\n" +
						"public X {  \n"+
						"	this.a = a; \n"+
						"	}\n"+
						"public X(int/*here*/a, int b) { // select the a here\n"+
						"this.a = a;\n"+
						"}\n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection =  "/*here*/a";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof LocalVariable);
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults(
				"src/X.java X(int, int) [a] EXACT_MATCH");
	}

	public void testBug560486_028() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"public record Point(int comp) { \n" +
						"public void method ( ) {	  \n"+
						"/*here*/comp(); \n"+
						"} \n"+
						"}\n"
				);

		String str = this.workingCopies[0].getSource();
		String selection = "/*here*/comp";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(!(elements[0] instanceof SourceType));
		assertTrue((elements[0] instanceof SourceField));
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
}
