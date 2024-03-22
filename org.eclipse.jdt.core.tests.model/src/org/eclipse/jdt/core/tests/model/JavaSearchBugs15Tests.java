/*******************************************************************************
 * Copyright (c)  2020, 2022 IBM Corporation and others.
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
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
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
// JavaSearchBugs14Tests deleted and all test cases moved to JavaSearchBugs15Tests
// since both record and instanceof variable are in 2nd preview in Java15
public class JavaSearchBugs15Tests extends AbstractJavaSearchTests {

	static {
		//	 org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
		//		TESTS_NUMBERS = new int[] { 19 };
		//		TESTS_RANGE = new int[] { 1, -1 };
		//		TESTS_NAMES = new String[] {"testBug542559_001"};
	}

	public JavaSearchBugs15Tests(String name) {
		super(name);
		this.endChar = "";
	}

	public static Test suite() {
		return buildModelTestSuite(JavaSearchBugs15Tests.class, BYTECODE_DECLARATION_ORDER);
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
		JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "16");
		makeJCLModular(JAVA_PROJECT);
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
				"""
					public record Point(int comp_) {\s
						public Point  {
						}
					}
					"""
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
				"""
					public record Point(int comp_) {\s
						public Point  {
						  comp_=11;
						}
					}
					"""
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
				"""
					public record Point(int comp_) {\s
						public Point  {
						  comp_=11;
						  this.comp_=11;
						}
					}
					"""
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
				"""
					public record Point(int comp_) {\s
						public Point  {
						  comp_=11;
						  this.comp_=comp_;
						}
					}
					"""
				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			search("comp_", FIELD, REFERENCES);
			assertSearchResults("""
				src/X.java Point(int) [comp_] EXACT_MATCH
				src/X.java Point(int) [comp_] EXACT_MATCH
				src/X.java Point(int) [comp_] EXACT_MATCH""");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	//0 reference of the component in canonical constructor
	public void testBug558812_005() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"""
					public record Point(int comp_) {\s
						public Point (int a) {
						}
					}
					"""
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
				"""
					public record Point(int comp_) {\s
						public Point (int a) {
						  comp_=11;
						}
					}
					"""
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
				"""
					public record Point(int comp_) {\s
						public Point (int a) {
						 // comp_=11;
						 this.comp_=a;
						}
					}
					"""
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
				"""
					public record Point(int comp_) {\s
						public Point  {
						  comp_=11;
						}
					public void method ( int comp_) {	 \s
					}\s
					}
					"""
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
				"""
					public record Point(int comp_) {\s
						public Point  {
						  comp_=11;
						}
					public void method ( ) {	 \s
						  int comp_=11;
					}\s
					}
					"""
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
				"""
					public record /* here*/Point(int comp_) {\s
						public Point  {
						  comp_=11;
						}
					public void method ( ) {	 \s
						  int  compp_=11;
					}\s
					}
					"""
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
				"""
					public record /* here*/Point(int comp_) {\s
						public Point  {
						  comp_=11;
						}
					public void method ( ) {	 \s
						  int  /* here*/compp_=11;
					}\s
					}
					"""
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
				"""
					public record /* here*/Point(int comp_) {\s
						public Point  {
						  /* here*/comp_=11;
						}
					public void method ( ) {	 \s
						  int compp_=11;
					}\s
					}
					"""
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
				"""
					public record /* here*/Point(int comp_) {\s
						public Point  {
						  /* here*/this.comp_=11;
						}
					public void method ( ) {	 \s
						  int  compp_=11;
					}\s
					}
					"""
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
				"""
					public record /* here*/Point(int comp_) {\s
						public Point  {
						  /* here*/comp_=11;
						}
					public void method ( ) {	 \s
						  int  compp_=/* here*/this.comp_;
					}\s
					}
					"""
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
				"""
					public record /* here*/Point(int comp_) {\s
						public Point  {
						  comp_=11;
						}
					public void method ( ) {	 \s
						  int  compp_=/* here2*/comp_;
					}\s
					}
					"""
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
				"""
					public record Point(int comp_) {\s
						public Point  {
						  /*here2*/comp_=11;
						}
					public void method ( ) {	 \s
						  int  compp_=comp_;
						  int  compp2_=comp_;
					}\s
					}
					"""
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
				"""
					src/X.java Point(int) [comp_] EXACT_MATCH
					src/X.java void Point.method() [comp_] EXACT_MATCH
					src/X.java void Point.method() [comp_] EXACT_MATCH""");
	}
	public void testBug558812_016a() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"""
					public record Point(int comp_) {\s
						public Point  {
						  comp_=11;
						}
					public void method ( ) {	 \s
						  int  compp_=comp_;
						  int  compp2_=comp_;
					}\s
					}
					"""
				);

		String str = this.workingCopies[0].getSource();
		String selection =  "comp_";
		int start = str.lastIndexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof IField);
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults(
				"""
					src/X.java Point(int) [comp_] EXACT_MATCH
					src/X.java void Point.method() [comp_] EXACT_MATCH
					src/X.java void Point.method() [comp_] EXACT_MATCH""");
	}

	public void testBug558812_017() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"""
					public record Point(int comp_) {\s
						public Point  {
						  /*here2*/comp_=11;
						}
					public void method ( ) {	 \s
						  int  compp_=comp_;
						  int  compp2_=comp_;
						  int  compp3_=this.comp_;
					}\s
					}
					"""
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
				"""
					src/X.java Point(int) [comp_] EXACT_MATCH
					src/X.java void Point.method() [comp_] EXACT_MATCH
					src/X.java void Point.method() [comp_] EXACT_MATCH
					src/X.java void Point.method() [comp_] EXACT_MATCH""");
	}
	public void testBug558812_017a() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"""
					public record Point(int comp_) {\s
						public Point  {
						  comp_=11;
						}
					public void method ( ) {	 \s
						  int  compp_=comp_;
						  int  compp2_=comp_;
						  int  compp3_=this.comp_;
					}\s
					}
					"""
				);

		String str = this.workingCopies[0].getSource();
		String selection =  "comp_";
		int start = str.lastIndexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof IField);
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults(
				"""
					src/X.java Point(int) [comp_] EXACT_MATCH
					src/X.java void Point.method() [comp_] EXACT_MATCH
					src/X.java void Point.method() [comp_] EXACT_MATCH
					src/X.java void Point.method() [comp_] EXACT_MATCH""");
	}

	//
	public void testBug572467() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Point.java",
				"""
					public record Point(int comp_) {\s
						public Point  {
						  comp_=11;
						}
					public void method ( ) {	 \s
						  int  var1=comp_;
						  int  var2=comp_;
						  int  var3=this.comp_;
						  int  accMethod=this.comp_();
					}\s
					}
					"""
				);

		String str = this.workingCopies[0].getSource();
		String selection =  "comp_";
		int start = str.lastIndexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue(elements[0] instanceof IField);
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults(
				"""
					src/Point.java Point(int) [comp_] EXACT_MATCH
					src/Point.java void Point.method() [comp_] EXACT_MATCH
					src/Point.java void Point.method() [comp_] EXACT_MATCH
					src/Point.java void Point.method() [comp_] EXACT_MATCH
					src/Point.java void Point.method() [comp_()] EXACT_MATCH""");
	}
	//selection  - select CC type
	public void testBug558812_018() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"""
					public record Point(int comp_) {\s
						public /* here*/Point  {
						  comp_=11;
						}
						public Point  (int a, int b){
						  comp_=11;
						}
					public void method ( ) {	 \s
						  int  compp_=11;
					}\s
					}
					"""
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
				"""
					public record Point(int comp_) {\s
						public /* here*/Point  {
						  comp_=11;
						}
						public Point  (int a, int b){
						  comp_=11;
						}
					public void method ( ) {	 \s
						  int  compp_=11;
						  Point p = new Point(1) ;
					}\s
					}
					"""
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
				"""
					public record Point(int comp_) {\s
						public Point  {
						  comp_=11;
						}
						public /* here*/Point  (int a, int b){
						  comp_=11;
						}
					public void method ( ) {	 \s
						  int  compp_=11;
						  Point p = new Point(1) ;
					}\s
					}
					"""
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
				"""
					public record Point(int comp_) {\s
						public Point  {
						  comp_=11;
						}
						public /* here*/Point  (int a, int b){
						  comp_=11;
						}
					public void method ( ) {	 \s
						  int  compp_=11;
						  Point p = new Point(1) ;
						  Point p = new Point(1,2) ;
					}\s
					}
					"""
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
				"""
					public record Point(int comp_) {\s
						public /* here*/Point  {
						  comp_=11;
						}
						public Point  (int a, int b){
						  comp_=11;
						}
					public void method ( ) {	 \s
						  int  compp_=11;
						  Point p = new Point(1) ;
						  Point p = new Point(1,2) ;
					}\s
					}
					"""
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
				"""
					public record /* here*/Point(int comp_) {\s
						public Point  {
						  /* here*/comp_=11;
						}
					public void method ( ) {	 \s
						  int compp_=11;
					}\s
					}
					"""
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
				"""
					public record /* here*/Point(int comp_) {\s
						public Point  {
						  /* here*/comp_=11;
						}
					public void method ( ) {	 \s
						   comp_=11;
					}\s
					}
					"""
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
				"""
					public record /* here*/Point(int comp_) {\s
						public Point  {
						  /* here*/comp_=11;
						}
					public void method ( ) {	 \s
						   comp_=11;
						   int a=this.comp_;
					}\s
					}
					"""
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
				"""
					src/X.java Point(int) [comp_] EXACT_MATCH
					src/X.java void Point.method() [comp_] EXACT_MATCH
					src/X.java void Point.method() [comp_] EXACT_MATCH""");
	}

	//selection  - select record in another file
	public void testBug558812_26() throws CoreException {
		IJavaProject project1 = createJavaProject("JavaSearchBugs15", new String[] {"src"}, new String[] {"JCL14_LIB"}, "bin", "14");
		try {
			Map<String, String> options = project1.getOptions(false);
			options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_16);
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_16);
			project1.setOptions(options);
			project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			project1.open(null);
			createFolder("/JavaSearchBugs15/src/pack11");
			String fileContent = """
				package pack11;
				public record X11() {
				}
				""";
			String fileContent2 = """
				package pack11;
				public class X12  {
				/*here*/X11 p =null;
				}
				""";

			createFile("/JavaSearchBugs15/src/pack11/X11.java", fileContent);
			createFile("/JavaSearchBugs15/src/pack11/X12.java",fileContent2);
			ICompilationUnit unit = getCompilationUnit("/JavaSearchBugs15/src/pack11/X12.java");
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
				"""
					public record X(int a) {
					public X { \s
						this.a = a;\s
						}
					public X(int/*here*/a, int b) { // select the a here
					this.a = a;
					}
					}
					"""
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
				"""
					public record Point(int comp) {\s
					public void method ( ) {	 \s
					/*here*/comp();\s
					}\s
					}
					"""
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
					"""
						public class cl {
						public cl() {
							method1();
						}
						private void method1() {
							String y= this.toString();
							if (y instanceof String /*here*/yz) {
							      System.out.println(yz.toLowerCase());
							      System.out.println(yz.charAt(0));
							}
						}
						}
						"""
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
					"""
						public class cl {
						public cl() {
							method1();
						}
						private void method1() {
							String y= this.toString();
							if (y instanceof String /*here*/yz) {
							      System.out.println(yz.toLowerCase());
							}
						}
						}
						"""
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
					"""
						public class cl {
						public cl() {
							method1();
						}
						private void method1() {
							String y= this.toString();
							if (y instanceof String /*here*/yz) {
							      System.out.println(yz.toLowerCase());
							      System.out.println(yz.charAt(0));
							}
						}
						}
						"""
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
					"""
						public class cl {
						public cl() {
							method1();
						}
						private void method1() {
							String y= this.toString();
							if (y instanceof String yz) {
							      System.out.println(/*here*/yz.toLowerCase());
							}
						}
						}
						"""
					);

			String str = this.workingCopies[0].getSource();
			String selection = "/*here*/yz";
			int start = str.indexOf(selection);
			int length = selection.length();

			IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
			assertEquals("incorrect size of elements", 1, elements.length);
			assertTrue((elements[0] instanceof LocalVariable));

		}

	// mix Instance of pattern variable  and record
	public void testBug561048_032() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"""
					public record cl() {
					public cl{
						method1();
					}
					private void method1() {
						String y= this.toString();
						if (y instanceof String /*here*/yz) {
						      System.out.println(yz.toLowerCase());
						      System.out.println(yz.charAt(0));
						}
					}
					}
					"""
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


	//check for permit reference
	public void test564049_001() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"""
					public sealed class X permits Y{\s
						}
						final class Y extends X {}
					"""

				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			search("Y", CLASS, REFERENCES);
			assertSearchResults("src/X.java X [Y] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	// select a class and check its permit reference
	public void test564049_002() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"""
					public sealed class X permits Y{\s
						}
						final class /*here*/Y extends X {}
					"""

				);
		String str = this.workingCopies[0].getSource();
		String selection = "/*here*/Y";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue((elements[0] instanceof SourceType));
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults(
				"src/X.java X [Y] EXACT_MATCH");
	}

	// select a class ( at permit location) and check its reference
	public void test564049_003() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"""
					public sealed class X permits /*here*/Y{\s
						}
						final class Y extends X {}
					"""

				);
		String str = this.workingCopies[0].getSource();
		String selection = "/*here*/Y";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue((elements[0] instanceof SourceType));
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults(
				"src/X.java X [Y] EXACT_MATCH");
	}
	//check for permit reference if it is the nth permitted item
	public void test564049_004() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"""
					public sealed class X permits Y,Q{\s
						}
						final class Q extends X {}
						final class Y extends X {}
					"""

				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			search("Q", CLASS, REFERENCES);
			assertSearchResults("src/X.java X [Q] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	// select a class and check its nth permit reference
	public void test564049_005() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"""
					public sealed class X permits Y,Q{\s
						}
						final class /*here*/Q extends X {}
						final class Y extends X {}
					"""

				);
		String str = this.workingCopies[0].getSource();
		String selection = "/*here*/Q";
		int start = str.indexOf(selection);
		int length = selection.length();

		IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
		assertTrue(elements.length ==1);
		assertTrue((elements[0] instanceof SourceType));
		search(elements[0], REFERENCES, EXACT_RULE);
		assertSearchResults(
				"src/X.java X [Q] EXACT_MATCH");
	}

	//check for permit reference with supertype finegrain
	public void test564049_006() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"""
					public sealed class X permits Y{\s
						}
						final class Y extends X {}
					"""

				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			search("Y", CLASS, SUPERTYPE_TYPE_REFERENCE);
			assertSearchResults("");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	//check for permit reference with permittype finegrain
	public void test564049_007() throws CoreException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
				"""
					public sealed class X permits Y{\s
						}
						final class Y extends X {}
					"""

				);
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			search("Y", CLASS, PERMITTYPE_TYPE_REFERENCE);
			assertSearchResults("src/X.java X [Y] EXACT_MATCH");
		} finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}
	//check for permit reference with permittype or supertype finegrain
		public void test564049_008() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
					"""
						public sealed class X permits Y{\s
							}
							final class Y extends X {}
						"""

					);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				search("Y", CLASS, PERMITTYPE_TYPE_REFERENCE | SUPERTYPE_TYPE_REFERENCE);
				assertSearchResults("src/X.java X [Y] EXACT_MATCH");
			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}
		}

		//check for permit reference for qualified type
		public void test564049_009() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p1/X.java",
					"""
						package p1;
						public sealed class X permits A.Y {
							public static void main(String[] args) {}
						}
						class A {
							sealed class Y extends X {
								final class SubInnerY extends Y {}
							}\s
							final class Z extends Y {}
						   final class SubY extends Y {}\
						}"""

					);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				search("A.Y", CLASS, REFERENCES);
				assertSearchResults(
						"""
							src/p1/X.java p1.X [A.Y] EXACT_MATCH
							src/p1/X.java p1.A$Y$SubInnerY [Y] EXACT_MATCH
							src/p1/X.java p1.A$Z [Y] EXACT_MATCH
							src/p1/X.java p1.A$SubY [Y] EXACT_MATCH""");
			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}
		}


		//check for permit reference with permittype finegrain - negative test case
		public void test564049_010() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
					"""
						public sealed class X extends Y{\s
							}
							 class Y {}
						"""

					);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				search("Y", CLASS, PERMITTYPE_TYPE_REFERENCE);
				assertSearchResults("");
			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}
		}
		//permit reference in another file
		public void test564049_011() throws CoreException {
			IJavaProject project1 = createJavaProject("JavaSearchBugs15", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "16");
			try {
				Map<String, String> options = project1.getOptions(false);
				options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_16);
				project1.setOptions(options);
				project1.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				project1.open(null);
				createFolder("/JavaSearchBugs15/src/pack11");
				String fileContent = """
					package pack11;
					public sealed class X11_ permits X12_{
					}
					""";
				String fileContent2 = """
					package pack11;
					final public class /*here*/X12_ extends X11_ {
					}
					""";

				createFile("/JavaSearchBugs15/src/pack11/X11_.java", fileContent);
				createFile("/JavaSearchBugs15/src/pack11/X12_.java",fileContent2);
				ICompilationUnit unit = getCompilationUnit("/JavaSearchBugs15/src/pack11/X12_.java");
				String x11 = "/*here*/X12_";
				int start = fileContent2.indexOf(x11);
				IJavaElement[] elements = unit.codeSelect(start, x11.length());
				assertTrue(elements.length ==1);
				IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
				search(elements[0].getElementName(), TYPE, PERMITTYPE_TYPE_REFERENCE, EXACT_RULE, scope);
				assertSearchResults("src/pack11/X11_.java pack11.X11_ [X12_] EXACT_MATCH");
			} finally {
				deleteProject(project1);
			}
		}
	  public void testRecordReferenceInNonSourceJar() throws CoreException {

		 		IType typeRecord = getClassFile("JavaSearchBugs", "lib/record_reference_in_nonsource_jar.jar", "pack", "rr.class").getType();//record
		 		search(
		 			typeRecord,
		 			ALL_OCCURRENCES,
		 			getJavaSearchScope(),
		 			this.resultCollector);
		 		assertSearchResults(
		 			"lib/record_reference_in_nonsource_jar.jar pack.c1.ob [No source] EXACT_MATCH\n"
		 			+ "lib/record_reference_in_nonsource_jar.jar pack.rr [No source] EXACT_MATCH",
		 			this.resultCollector);
		 		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=565180 ( reason for 1 result instead of 3)
		}

	 	public void testRecordReferenceInSourceJar() throws CoreException {

	 		IType typeRecord = getClassFile("JavaSearchBugs", "lib/record_reference_in_source_jar.jar", "pack", "rr2.class").getType();//record
	 		search(
	 			typeRecord,
	 			ALL_OCCURRENCES,
	 			getJavaSearchScope(),
	 			this.resultCollector);
	 		assertSearchResults(
	 				"""
						lib/record_reference_in_source_jar.jar pack.c1.ob EXACT_MATCH
						lib/record_reference_in_source_jar.jar pack.c1.ob EXACT_MATCH
						lib/record_reference_in_source_jar.jar pack.rr2 EXACT_MATCH""",
	 			this.resultCollector);
	 	}

	 	public void testPermitReferenceInNonSourceJar() throws CoreException {

	 		IType myClass = getClassFile("JavaSearchBugs", "lib/permit_reference_in_nonsource_jar.jar", "pack", "PermitClass.class").getType();
	 		search(
	 			myClass,
	 			ALL_OCCURRENCES,
	 			getJavaSearchScope(),
	 			this.resultCollector);
	 		assertSearchResults(
	 				"lib/permit_reference_in_nonsource_jar.jar pack.PermitClass [No source] EXACT_MATCH",
	 			this.resultCollector);

	 	}

	 	public void testPermitReferenceInSourceJar() throws CoreException {

	 		IType myClass = getClassFile("JavaSearchBugs", "lib/permit_reference_in_source_jar.jar", "pack", "PermitClass2.class").getType();
	 		search(
	 			myClass,
	 			ALL_OCCURRENCES,
	 			getJavaSearchScope(),
	 			this.resultCollector);
	 		assertSearchResults(
	 				"lib/permit_reference_in_source_jar.jar pack.MyClass2 EXACT_MATCH\n" +
	 				"lib/permit_reference_in_source_jar.jar pack.PermitClass2 EXACT_MATCH",
	 			this.resultCollector);

	 	}

	 	public void testAnnotationsInRecords1() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];

			String contents = """
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				record X(@MyAnnot int lo) {
					public int lo() {
						return this.lo;
					}
				
				}
				@Target({ElementType.FIELD})
				@interface MyAnnot {}""";
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",contents);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				search("MyAnnot", ANNOTATION_TYPE, ALL_OCCURRENCES);
				assertSearchResults(
						"src/X.java X.lo [MyAnnot] EXACT_MATCH\n" +
						"src/X.java MyAnnot [MyAnnot] EXACT_MATCH");
			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}

	 	}

	 	public void testAnnotationsInRecords2() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];

			String contents = """
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				record X(@MyAnnot int lo) {
					public int lo() {
						return this.lo;
					}
				
				}
				@Target({ElementType.FIELD})
				@interface MyAnnot {}""";
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",contents);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				search("MyAnnot", ANNOTATION_TYPE, REFERENCES);
				assertSearchResults("src/X.java X.lo [MyAnnot] EXACT_MATCH");
			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}

	 	}
	 	public void testAnnotationsInRecords3() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];

			String contents = """
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				record X(@MyAnnot int lo) {
					public static @MyAnnot int x;
					public int lo() {
						return this.lo;
					}
				
				}
				@Target({ElementType.RECORD_COMPONENT})
				@interface MyAnnot {}""";
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",contents);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				search("MyAnnot", ANNOTATION_TYPE, REFERENCES);
				assertSearchResults(
						"src/X.java X.lo [MyAnnot] EXACT_MATCH\n" +
						"src/X.java X.x [MyAnnot] EXACT_MATCH");
			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}

	 	}
	 	public void testAnnotationsInRecords4() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];

			String contents = "package test1;\n" +
					"import java.lang.annotation.ElementType;\n" +
					"public record X() {\n" +
					"	static String myObject = \"Foo\";\n" +
					"	public void foo() {\n" +
					"		String myString = (@Annot String) myObject;\n" +
					"		String myString1 = (@Annot1 @Annot String) myObject;\n" +
					"	}\n" +
					"}\n" +
					"@java.lang.annotation.Target(value = {ElementType.TYPE_USE})\n" +
					"@interface Annot {}\n" +
					"@java.lang.annotation.Target(value = {ElementType.TYPE_USE})\n" +
					"@interface Annot1 {}\n" +
					"";
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",contents);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				search("Annot", ANNOTATION_TYPE, REFERENCES);

				assertSearchResults(
						"src/X.java void X.foo() [Annot] EXACT_MATCH\n" +
						"src/X.java void X.foo() [Annot] EXACT_MATCH");

			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}

	 	}
	 	public void testAnnotationsInRecords5() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];

			String contents = """
				package test1;
				import java.lang.annotation.ElementType;
				public record X() {
					static String myObject = "Foo";
					public void foo() {
						String myString = (@Annot String) myObject;
						String myString1 = (@Annot1 @Annot String) myObject;
					}
				}
				@java.lang.annotation.Target(value = {ElementType.TYPE_USE})
				@interface Annot {}
				@java.lang.annotation.Target(value = {ElementType.TYPE_USE})
				@interface Annot1 {}""";
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",contents);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				search("Annot", ANNOTATION_TYPE, REFERENCES);
				assertSearchResults(
						"src/X.java void X.foo() [Annot] EXACT_MATCH\n" +
						"src/X.java void X.foo() [Annot] EXACT_MATCH");
			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}

	 	}
	 	public void testAnnotationsInRecords6() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];

			String contents = """
				package test1;
				
				import java.lang.annotation.ElementType;
				
				public record X2() {
					public interface Helper<T> {
					}
					public class Foo1<T> implements @Annot_ Helper<T> {
					}
					public class Foo2<T> implements @Annot_ @Annot1_ Helper<T> {
					}
				}
				
				@java.lang.annotation.Target (ElementType.TYPE_USE)
				@interface Annot_ {}
				
				@java.lang.annotation.Target (ElementType.TYPE_USE)
				@interface Annot1_ {}""";
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",contents);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				search("Annot_", ANNOTATION_TYPE, REFERENCES);
				assertSearchResults(
						"src/X.java X2$Foo1 [Annot_] EXACT_MATCH\n" +
						"src/X.java X2$Foo2 [Annot_] EXACT_MATCH");
			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}

	 	}
		public void testAnnotationsInRecords7() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];

			String contents = "package test1;\n" +
					"import java.lang.annotation.ElementType;\n" +
					"public record X3() {\n" +
					"	public record Helper<T>() {\n" +
					"	}\n" +
					"	public record Base() {\n" +
					"	}\n" +
					"	public static void UnboundedWildcard1 (Helper<@Annot__ ?> x) {\n" +
					"	}\n" +
					"	public static void UnboundedWildcard2 (Helper<@Annot1__ @Annot__ ?> x) {\n" +
					"	}\n" +
					"	public static void BoundedWildcard1 (Helper<@Annot__ ? extends Base> x) {\n" +
					"	}\n" +
					"	public static void BoundedWildcard2 (Helper<@Annot1__ @Annot__ ? extends Base> x) {\n" +
					"	}\n" +
					"}\n" +
					"@java.lang.annotation.Target (ElementType.TYPE_USE)\n" +
					"@interface Annot__ {}\n" +
					"@java.lang.annotation.Target (ElementType.TYPE_USE)\n" +
					"@interface Annot1__ {}\n" +
					"";
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",contents);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				search("Annot__", ANNOTATION_TYPE, REFERENCES);
				assertSearchResults(
						"""
							src/X.java void X3.UnboundedWildcard1(Helper<?>) [Annot__] EXACT_MATCH
							src/X.java void X3.UnboundedWildcard2(Helper<?>) [Annot__] EXACT_MATCH
							src/X.java void X3.BoundedWildcard1(Helper<? extends Base>) [Annot__] EXACT_MATCH
							src/X.java void X3.BoundedWildcard2(Helper<? extends Base>) [Annot__] EXACT_MATCH""");
			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}

	 	}

		public void testAnnotationsInRecords8() throws CoreException {
			this.workingCopies = new ICompilationUnit[1];

			String contents =  """
				package test1;
				import java.lang.annotation.Target;
				public record X() {
					public static void main(String[] args) {
						Outer outer = new Outer();
						Outer.@Marker1 Inner first = outer.new Inner();
						Outer.@Marker2 Inner second = outer.new Inner() ;
						Outer.Inner.@Marker1 Deeper deeper = second.new Deeper();
						Outer.Inner.Deeper deeper2 =  second.new Deeper();
					}
				}
				class Outer {
					public class Inner {
						public class Deeper {
						}
					}
				}
				@Target (java.lang.annotation.ElementType.TYPE_USE)
				@interface Marker {}
				@Target (java.lang.annotation.ElementType.TYPE_USE)
				@interface Marker1 {}
				@Target (java.lang.annotation.ElementType.TYPE_USE)
				@interface Marker2 {}
				""";
			this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",contents);
			IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
			String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
			try {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
				search("Marker1", ANNOTATION_TYPE, REFERENCES);
				assertSearchResults(
						"src/X.java void X.main(String[]) [Marker1] EXACT_MATCH\n" +
						"src/X.java void X.main(String[]) [Marker1] EXACT_MATCH");
			} finally {
				javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
			}

	 	}
		// test all occurrences  of annotation of non-source jar with record
		  public void testAnnnotationInRecordJar() throws CoreException {

		 		IType typeRecord = getClassFile("JavaSearchBugs", "lib/annotation_in_record_jar.jar", "pack", "X99.class").getType();
		 		search(
		 			typeRecord,
		 			ALL_OCCURRENCES,
		 			getJavaSearchScope(),
		 			this.resultCollector);
		 		assertSearchResults(
		 				"lib/annotation_in_record_jar.jar pack.MyRecord.lo [No source] EXACT_MATCH\n" +
		 				"lib/annotation_in_record_jar.jar pack.X99 [No source] EXACT_MATCH",
		 			this.resultCollector);
		}

		// test all occurrences  of annotation of source jar with record
		  public void testAnnnotationInRecordSourceJar() throws CoreException {

		 		IType typeRecord = getClassFile("JavaSearchBugs", "lib/annotation_in_record_source_jar.jar", "pack", "X100.class").getType();
		 		search(
		 			typeRecord,
		 			ALL_OCCURRENCES,
		 			getJavaSearchScope(),
		 			this.resultCollector);
		 		assertSearchResults(
		 				"lib/annotation_in_record_source_jar.jar pack.MyRecord.lo EXACT_MATCH\n" +
		 						"lib/annotation_in_record_source_jar.jar pack.X100 EXACT_MATCH",
		 			this.resultCollector);
		}
		// test all reference  of annotation of non-source jar with record
		  public void testAnnnotationJustReferenceInRecordJar() throws CoreException {

		 		IType typeRecord = getClassFile("JavaSearchBugs", "lib/annotation_in_record_jar.jar", "pack", "X99.class").getType();
		 		search(
		 			typeRecord,
		 			REFERENCES,
		 			getJavaSearchScope(),
		 			this.resultCollector);
		 		assertSearchResults(
		 				"lib/annotation_in_record_jar.jar pack.MyRecord.lo [No source] EXACT_MATCH",
		 			this.resultCollector);
		}
		// test all reference  of annotation of source jar with record
		  public void testAnnnotationJustReferenceInRecordSourceJar() throws CoreException {

		 		IType typeRecord = getClassFile("JavaSearchBugs", "lib/annotation_in_record_source_jar.jar", "pack", "X100.class").getType();
		 		search(
		 			typeRecord,
		 			REFERENCES,
		 			getJavaSearchScope(),
		 			this.resultCollector);
		 		assertSearchResults(
		 				"lib/annotation_in_record_source_jar.jar pack.MyRecord.lo EXACT_MATCH",
		 			this.resultCollector);
		}

			public void test566507_componentSelectAndSearch() throws CoreException {
				this.workingCopies = new ICompilationUnit[1];
				this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
						"""
							public record Point(int /* here*/comp_) {\s
								public Point  {
								  comp_=11;
								}
							public void method ( ) {	 \s
								  int  compp_=11;
							}\s
							}
							"""
						);

				String str = this.workingCopies[0].getSource();
				String selection = "/* here*/comp_";
				int start = str.indexOf(selection);
				int length = selection.length();

				IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
				assertTrue(elements.length ==1);
				assertTrue(elements[0] instanceof SourceField);
				search(elements[0], REFERENCES, EXACT_RULE);
				assertSearchResults(
						"src/X.java Point(int) [comp_] EXACT_MATCH");

			}
			public void test566507_fieldSelectAndSearch() throws CoreException {
				this.workingCopies = new ICompilationUnit[1];
				this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
						"""
							public record Point(int /* here*/comp_) {\s
								public static int staticF =0;
								public Point  {
								  comp_=11;
								  staticF=11;
								}
							public void method ( ) {	 \s
								  int  compp_=11;
							}\s
							}
							"""
						);

				String str = this.workingCopies[0].getSource();
				String selection = "staticF";
				int start = str.indexOf(selection);
				int length = selection.length();

				IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
				assertTrue(elements.length ==1);
				assertTrue(elements[0] instanceof SourceField);
				search(elements[0], ALL_OCCURRENCES, EXACT_RULE);
				assertSearchResults(
						"src/X.java Point.staticF [staticF] EXACT_MATCH\n" +
						"src/X.java Point(int) [staticF] EXACT_MATCH");

			}

			public void test566062_001() throws CoreException {
				this.workingCopies = new ICompilationUnit[2];
				this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/test.java",
						"""
							public class /* here*/test {\s
								/**
								 * @see mod.one/pack.test
								 */
								public void method ( ) {	 \s
									int  compp_=11;
								}\s
								/**
								 * {@link mod.one/pack.test abc}
								 */
								public void apply ( ) {	 \s
									int  compp_=11;
								}\s
								/**
								 * {@linkplain mod.one/pack.test abc}
								 */
								public void evaluate ( ) {	 \s
									int  compp_=11;
								}\s
							}
							"""
					);

				this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/module-info.java",
						"import pack.*;\n" +
						"module mod.one {}");

				String str = this.workingCopies[0].getSource();
				String selection = "test";
				int start = str.indexOf(selection);
				int length = selection.length();

				IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
				assertTrue(elements.length ==1);

				search(elements[0], ALL_OCCURRENCES, EXACT_RULE);
				assertSearchResults(
						"""
							src/pack/test.java pack.test [test] EXACT_MATCH
							src/pack/test.java void pack.test.method() [pack.test] EXACT_MATCH
							src/pack/test.java void pack.test.apply() [pack.test] EXACT_MATCH
							src/pack/test.java void pack.test.evaluate() [pack.test] EXACT_MATCH""");
			}

			public void test566062_002() throws CoreException {
				this.workingCopies = new ICompilationUnit[2];
				this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/test.java",
						"""
							public class test {\s
								public int /* here*/val;\
								/**
								 * @see mod.one/pack.test#val
								 */
								public void method ( ) {	 \s
									int  compp_=11;
								}\s
								/**
								 * {@link mod.one/pack.test#val abc}
								 */
								public void apply ( ) {	 \s
									int  compp_=11;
								}\s
								/**
								 * {@linkplain mod.one/pack.test#val abc}
								 */
								public void evaluate ( ) {	 \s
									int  compp_=11;
								}\s
							}
							"""
					);

				this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/module-info.java",
						"import pack.*;\n" +
						"module mod.one {}");

				String str = this.workingCopies[0].getSource();
				String selection = "val";
				int start = str.indexOf(selection);
				int length = selection.length();

				IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
				assertTrue(elements.length ==1);

				search(elements[0], ALL_OCCURRENCES, EXACT_RULE);
				assertSearchResults(
						"""
							src/pack/test.java pack.test.val [val] EXACT_MATCH
							src/pack/test.java void pack.test.method() [val] EXACT_MATCH
							src/pack/test.java void pack.test.apply() [val] EXACT_MATCH
							src/pack/test.java void pack.test.evaluate() [val] EXACT_MATCH""");
			}

			public void test566062_003() throws CoreException {
				this.workingCopies = new ICompilationUnit[2];
				this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/test.java",
						"""
							public class test {\s
								public void /* here*/setComp ( ) {	 \s
									int  compp_=11;
								}\s
								/**
								 * @see mod.one/pack.test#setComp()
								 */
								public void method ( ) {	 \s
									int  compp_=11;
								}\s
								/**
								 * {@link mod.one/pack.test#setComp() setComp}
								 */
								public void apply ( ) {	 \s
									int  compp_=11;
								}\s
								/**
								 * {@linkplain mod.one/pack.test#setComp() setComp}
								 */
								public void evaluate ( ) {	 \s
									int  compp_=11;
								}\s
							}
							"""
					);

				this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/module-info.java",
						"import pack.*;\n" +
						"module mod.one {}");

				String str = this.workingCopies[0].getSource();
				String selection = "setComp";
				int start = str.indexOf(selection);
				int length = selection.length();

				IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
				assertTrue(elements.length ==1);

				search(elements[0], ALL_OCCURRENCES, EXACT_RULE);
				assertSearchResults(
						"""
							src/pack/test.java void pack.test.setComp() [setComp] EXACT_MATCH
							src/pack/test.java void pack.test.method() [setComp()] EXACT_MATCH
							src/pack/test.java void pack.test.apply() [setComp()] EXACT_MATCH
							src/pack/test.java void pack.test.evaluate() [setComp()] EXACT_MATCH""");
			}

			public void test566062_004() throws CoreException {
				this.workingCopies = new ICompilationUnit[2];
				this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/test.java",
						"""
							public class /* here*/test {\s
								public int /* here*/val;\
								/**
								 * @see mod.one/pack.test#apply()
								 */
								public void method ( ) {	 \s
									int  compp_=11;
								}\s
								/**
								 * {@link mod.one/pack.test abc}
								 */
								public void apply ( ) {	 \s
									int  compp_=11;
								}\s
								/**
								 * {@linkplain mod.one/pack.test#val abc}
								 */
								public void evaluate ( ) {	 \s
									int  compp_=11;
								}\s
							}
							"""
					);

				this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/module-info.java",
						"import pack.*;\n" +
						"module mod.one {}");

				String str = this.workingCopies[0].getSource();
				String selection = "test";
				int start = str.indexOf(selection);
				int length = selection.length();

				IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
				assertTrue(elements.length ==1);

				search(elements[0], ALL_OCCURRENCES, EXACT_RULE);
				assertSearchResults(
						"""
							src/pack/test.java pack.test [test] EXACT_MATCH
							src/pack/test.java void pack.test.method() [pack.test] EXACT_MATCH
							src/pack/test.java void pack.test.apply() [pack.test] EXACT_MATCH
							src/pack/test.java void pack.test.evaluate() [pack.test] EXACT_MATCH""");
			}
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=572975
			// An error is occurring that prevents hover to work
			public void testBug572975() throws JavaModelException {
				this.workingCopies = new ICompilationUnit[1];
				this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/TestInstanceOf.java",
						"""
							public class TestInstanceOf {
								public interface WithValue {
									String value();
								}
							
								public record Key1(String value, int num) implements WithValue {
								}
							
								interface TestIt {
									boolean test(WithValue k1, WithValue k2);
								}
								private static final String AAA = "AAA";
							
								private static final TestIt TESTIT_LAMBDA = (o1, o2) -> {
									if (o1 instanceof Key1 k1 && o2 instanceof Key1 k2) {
										return k1./*here1*/value().equals(AAA);
									} else
										return false;
								};
							
								private static final TestIt TESTIT_METHOD = new TestIt() {
									@Override
									public boolean test(WithValue o1, WithValue o2) {
										if (o1 instanceof Key1 k1 && o2 instanceof Key1 k2) {
											return k1./*here2*/value().equals(AAA);
										} else
											return false;
									}
								};
							
								public static void main(String[] args) {
									System.out.println(AAA);
								}
							}
							""");
				String str = this.workingCopies[0].getSource();
				String selection = "/*here1*/value";
				int start = str.indexOf(selection);
				int length = selection.length();
				IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
				assertElementsEqual(
					"Unexpected elements",
					"value [in Key1 [in TestInstanceOf [in [Working copy] TestInstanceOf.java [in <default> [in src [in JavaSearchBugs]]]]]]",
					elements
				);
				selection = "/*here2*/value";
				start = str.lastIndexOf(selection);
				elements = this.workingCopies[0].codeSelect(start, length);
				assertElementsEqual(
					"Unexpected elements",
					"value [in Key1 [in TestInstanceOf [in [Working copy] TestInstanceOf.java [in <default> [in src [in JavaSearchBugs]]]]]]",
					elements
				);
			}
}
