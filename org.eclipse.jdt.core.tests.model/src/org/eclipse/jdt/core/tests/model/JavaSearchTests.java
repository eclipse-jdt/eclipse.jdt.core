/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.SourceRefElement;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.search.matching.TypeDeclarationPattern;

/**
 * Tests the Java search engine where results are JavaElements and source positions.
 */
public class JavaSearchTests extends AbstractJavaModelTests implements IJavaSearchConstants {

	public static List testSuites = null;
	protected static IJavaProject javaProject;

/**
 * Collects results as a string.
 */
public static class JavaSearchResultCollector extends SearchRequestor {
	public StringBuffer results = new StringBuffer();
	public boolean showAccuracy;
	public boolean showContext;
	public boolean showInsideDoc;
	public boolean showProject;
	public boolean showSynthetic;
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
		try {
			if (results.length() > 0) results.append("\n");
			IResource resource = match.getResource();
			IJavaElement element = (IJavaElement) match.getElement();
			results.append(getPathString(resource, element));
			if (this.showProject) {
				IProject project = element.getJavaProject().getProject();
				results.append(" [in ");
				results.append(project.getName());
				results.append("]");
			}
			ICompilationUnit unit = null;
			if (element instanceof IMethod) {
				results.append(" ");
				IMethod method = (IMethod)element;
				append(method);
				unit = method.getCompilationUnit();
			} else if (element instanceof IType) {
				results.append(" ");
				IType type = (IType)element;
				append(type);
				unit = type.getCompilationUnit();
			} else if (element instanceof IField) {
				results.append(" ");
				IField field = (IField)element;
				append(field);
				unit = field.getCompilationUnit();
			} else if (element instanceof IInitializer) {
				results.append(" ");
				IInitializer initializer = (IInitializer)element;
				append(initializer);
				unit = initializer.getCompilationUnit();
			} else if (element instanceof IPackageFragment) {
				results.append(" ");
				append((IPackageFragment)element);
			} else if (element instanceof ILocalVariable) {
				results.append(" ");
				ILocalVariable localVar = (ILocalVariable)element;
				IJavaElement parent = localVar.getParent();
				if (parent instanceof IInitializer) {
					IInitializer initializer = (IInitializer)parent;
					append(initializer);
				} else { // IMethod
					IMethod method = (IMethod)parent;
					append(method);
				}
				results.append(".");
				results.append(localVar.getElementName());
				unit = (ICompilationUnit)localVar.getAncestor(IJavaElement.COMPILATION_UNIT);
			}
			if (resource instanceof IFile) {
				char[] contents = getSource(resource, element, unit);
				int start = match.getOffset();
				int end = start + match.getLength();
				if (start == -1 || (contents != null && contents.length > 0)) { // retrieving attached source not implemented here
					results.append(" [");
					if (start > -1) {
						if (this.showContext) {
							int lineStart1 = CharOperation.lastIndexOf('\n', contents, 0, start);
							int lineStart2 = CharOperation.lastIndexOf('\r', contents, 0, start);
							int lineStart = Math.max(lineStart1, lineStart2) + 1;
							results.append(CharOperation.subarray(contents, lineStart, start));
							results.append("<");
						}
						results.append(CharOperation.subarray(contents, start, end));
						if (this.showContext) {
							results.append(">");
							int lineEnd1 = CharOperation.indexOf('\n', contents, end);
							int lineEnd2 = CharOperation.indexOf('\r', contents, end);
							int lineEnd = lineEnd1 > 0 && lineEnd2 > 0 ? Math.min(lineEnd1, lineEnd2) : Math.max(lineEnd1, lineEnd2);
							if (lineEnd == -1) lineEnd = contents.length;
							results.append(CharOperation.subarray(contents, end, lineEnd));
						}
					} else {
						results.append("No source");
					}
					results.append("]");
				}
			}
			if (this.showAccuracy) {
				results.append(" ");
				switch (match.getAccuracy()) {
					case SearchMatch.A_ACCURATE:
						results.append("EXACT_MATCH");
						break;
					case SearchMatch.A_INACCURATE:
						results.append("POTENTIAL_MATCH");
						break;
				}
			}
			if (this.showInsideDoc) {
				results.append(" ");
				if (match.isInsideDocComment()) {
					results.append("INSIDE_JAVADOC");
				} else {
					results.append("OUTSIDE_JAVADOC");
				}
			}
			if (this.showSynthetic) {
				if (match instanceof MethodReferenceMatch) {
					MethodReferenceMatch methRef = (MethodReferenceMatch) match;
					if (methRef.isSynthetic()) {
						results.append(" SYNTHETIC");
					}
				}
			}
		} catch (JavaModelException e) {
			results.append("\n");
			results.append(e.toString());
		}
	}
	private void append(IField field) throws JavaModelException {
		append(field.getDeclaringType());
		results.append(".");
		results.append(field.getElementName());
	}
	private void append(IInitializer initializer) throws JavaModelException {
		append(initializer.getDeclaringType());
		results.append(".");
		if (Flags.isStatic(initializer.getFlags())) {
			results.append("static ");
		}
		results.append("{}");
	}
	private void append(IMethod method) throws JavaModelException {
		if (!method.isConstructor()) {
			results.append(Signature.toString(method.getReturnType()));
			results.append(" ");
		}
		append(method.getDeclaringType());
		if (!method.isConstructor()) {
			results.append(".");
			results.append(method.getElementName());
		}
		results.append("(");
		String[] parameters = method.getParameterTypes();			
		for (int i = 0; i < parameters.length; i++) {
			results.append(Signature.toString(parameters[i]));
			if (i < parameters.length-1) {
				results.append(", ");
			}
		}
		results.append(")");
	}
	private void append(IPackageFragment pkg) {
		results.append(pkg.getElementName());
	}
	private void append(IType type) throws JavaModelException {
		IJavaElement parent = type.getParent();
		boolean isLocal = false;
		switch (parent.getElementType()) {
			case IJavaElement.COMPILATION_UNIT:
				IPackageFragment pkg = type.getPackageFragment();
				append(pkg);
				if (!pkg.getElementName().equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
					results.append(".");
				}
				break;
			case IJavaElement.CLASS_FILE:
				IType declaringType = type.getDeclaringType();
				if (declaringType != null) {
					append(type.getDeclaringType());
					results.append("$");
				} else {
					pkg = type.getPackageFragment();
					append(pkg);
					if (!pkg.getElementName().equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
						results.append(".");
					}
				}
				break;
			case IJavaElement.TYPE:
				append((IType)parent);
				results.append("$");
				break;
			case IJavaElement.FIELD:
				append((IField)parent);
				isLocal = true;
				break;
			case IJavaElement.INITIALIZER:
				append((IInitializer)parent);
				isLocal = true;
				break;
			case IJavaElement.METHOD:
				append((IMethod)parent);
				isLocal = true;
				break;
		}
		if (isLocal) {
			results.append(":");
		}
		String typeName = type.getElementName();
		if (typeName.length() == 0) {
			results.append("<anonymous>");
		} else {
			results.append(typeName);
		}
		if (isLocal) {
			results.append("#");
			results.append(((SourceRefElement)type).occurrenceCount);
		}
	}
	protected String getPathString(IResource resource, IJavaElement element) {
		String pathString;
		if (resource != null) {
			IPath path = resource.getProjectRelativePath();
			if (path.segmentCount() == 0) {
				IJavaElement root = element;
				while (root != null && !(root instanceof IPackageFragmentRoot)) {
					root = root.getParent();
				}
				if (root != null) {
					IPackageFragmentRoot pkgFragmentRoot = (IPackageFragmentRoot)root;
					if (pkgFragmentRoot.isExternal()) {
						pathString = pkgFragmentRoot.getPath().toOSString();
					} else {
						pathString = pkgFragmentRoot.getPath().toString();
					}
				} else {
					pathString = "";
				}
			} else {
				pathString = path.toString();
			}
		} else {
			pathString = element.getPath().toString();
		}
		return pathString;
	}
	protected char[] getSource(IResource resource, IJavaElement element, ICompilationUnit unit) throws CoreException {
		char[] contents = CharOperation.NO_CHAR;
		if ("java".equals(resource.getFileExtension())) {
			ICompilationUnit cu = (ICompilationUnit)element.getAncestor(IJavaElement.COMPILATION_UNIT);
			if (cu != null && cu.isWorkingCopy()) {
				// working copy
				contents = unit.getBuffer().getCharacters();
			} else {
				IFile file = ((IFile) resource);
				contents = new org.eclipse.jdt.internal.compiler.batch.CompilationUnit(
					null, 
					file.getLocation().toFile().getPath(),
					file.getCharset()).getContents();
			}
		}
		return contents;
	}
	public String toString() {
		return results.toString();
	}
}
	
//	protected IJavaProject javaProject;
	protected JavaSearchResultCollector resultCollector;
	
public JavaSearchTests(String name) {
	super(name);
}
public static Test suite() {
	return buildTestSuite(JavaSearchTests.class);
//	return buildTestSuite(JavaSearchTests.class, "testMethodReference", null);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
	// Prefix for tests names to be run
//	testsPrefix =  "testEnum";
	// Names of tests to run: can be "testBugXXXX" or "BugXXXX")
//	testsNames = new String[] { "testPackageReference9" };
	// Numbers of tests to run: "test<number>" will be run for each number of this array
//	testsNumbers = new int[] { 1, 2, 3, 9, 11, 16 };
	// Range numbers of tests to run: all tests between "test<first>" and "test<last>" will be run for { first, last }
//	testsRange = new int[] { 16, -1 };
	}
IJavaSearchScope getJavaSearchScope() {
	return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearch")});
}
IJavaSearchScope getJavaSearchScope15() {
	return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearch15")});
}
IJavaSearchScope getJavaSearchScopeBugs() {
	return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearchBugs")});
}
IJavaSearchScope getJavaSearchPackageScope(String packageName) throws JavaModelException {
	return getJavaSearchPackageScope(packageName, false);
}
IJavaSearchScope getJavaSearchPackageScope(String packageName, boolean addSubpackages) throws JavaModelException {
	String projectName = "JavaSearch15";
	IPackageFragment fragment = getPackageFragment(projectName, "src", packageName);
	if (fragment == null) return null;
	IJavaElement[] searchPackages = null;
	if (addSubpackages) {
		// Create list of package with first found one
		List packages = new ArrayList();
		packages.add(fragment);
		// Add all possible subpackages
		IJavaElement[] children= ((IPackageFragmentRoot)fragment.getParent()).getChildren();
		String[] names = ((PackageFragment)fragment).names;
		int namesLength = names.length;
		nextPackage: for (int i= 0, length = children.length; i < length; i++) {
			PackageFragment currentPackage = (PackageFragment) children[i];
			String[] otherNames = currentPackage.names;
			if (otherNames.length <= namesLength) continue nextPackage;
			for (int j = 0; j < namesLength; j++) {
				if (!names[j].equals(otherNames[j]))
					continue nextPackage;
			}
			packages.add(currentPackage);
		}
		searchPackages = new IJavaElement[packages.size()];
		packages.toArray(searchPackages);
	} else {
		searchPackages = new IJavaElement[1];
		searchPackages[0] = fragment;
	}
	return SearchEngine.createJavaSearchScope(searchPackages);
}
protected void search(SearchPattern searchPattern, IJavaSearchScope scope, SearchRequestor requestor) throws CoreException {
	new SearchEngine().search(
		searchPattern, 
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		scope,
		requestor,
		null);
}
protected void searchDeclarationsOfAccessedFields(IJavaElement enclosingElement, SearchRequestor requestor) throws JavaModelException {
	new SearchEngine().searchDeclarationsOfAccessedFields(enclosingElement, requestor, null);
}
protected void searchDeclarationsOfReferencedTypes(IJavaElement enclosingElement, SearchRequestor requestor) throws JavaModelException {
	new SearchEngine().searchDeclarationsOfReferencedTypes(enclosingElement, requestor, null);
}
protected void searchDeclarationsOfSentMessages(IJavaElement enclosingElement, SearchRequestor requestor) throws JavaModelException {
	new SearchEngine().searchDeclarationsOfSentMessages(enclosingElement, requestor, null);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();

	if (javaProject == null) {
		javaProject = setUpJavaProject("JavaSearch");
		setUpJavaProject("JavaSearch15", "1.5");
		setUpJavaProject("JavaSearchBugs");
	}
}
public void tearDownSuite() throws Exception {
	if (testSuites == null) {
		deleteProject("JavaSearch");
		deleteProject("JavaSearch15");
		deleteProject("JavaSearchBugs");
	} else {
		testSuites.remove(getClass());
		if (testSuites.size() == 0) {
			deleteProject("JavaSearch");
			deleteProject("JavaSearch15");
			deleteProject("JavaSearchBugs");
		}
	}

	super.tearDownSuite();
}
protected void setUp () throws Exception {
	this.resultCollector = new JavaSearchResultCollector();
	super.setUp();
}
/**
 * Simple constructor declaration test.
 */
public void testConstructorDeclaration01() throws CoreException { // was testSimpleConstructorDeclaration
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IMethod constructor = type.getMethod("A", new String[] {"QX;"});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		constructor, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults("src/p/A.java p.A(X) [A]", resultCollector);
}
/**
 * Constructor declaration in jar file test.
 */
public void testConstructorDeclaration02() throws CoreException { // was testConstructorDeclarationInJar
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p1", "A.class").getType();
	IMethod method = type.getMethod("A", new String[] {"Ljava.lang.String;"});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"MyJar.jar p1.A(java.lang.String) [No source]", 
		this.resultCollector);
}
/**
 * Simple constructor reference test.
 */
public void testConstructorReference01() throws CoreException { // was testSimpleConstructorReference1
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IMethod constructor = type.getMethod("A", new String[] {"QX;"});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		constructor, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [new p.A(y)]",
		this.resultCollector);
}
/**
 * Simple constructor reference test.
 */
public void testConstructorReference02() throws CoreException { // was testSimpleConstructorReference2
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"p.A(X)", 
		CONSTRUCTOR,
		REFERENCES,
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [new p.A(y)]",
		this.resultCollector);
}
/**
 * Constructor reference using an explicit constructor call.
 */
public void testConstructorReference03() throws CoreException { // was testConstructorReferenceExplicitConstructorCall1
	IType type = getCompilationUnit("JavaSearch", "src", "p", "Y.java").getType("Y");
	IMethod method = type.getMethod("Y", new String[] {"I"});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/p/Z.java p.Z(int) [super(i)]", 
		this.resultCollector);
}
/**
 * Constructor reference using an explicit constructor call.
 */
public void testConstructorReference04() throws CoreException { // was testConstructorReferenceExplicitConstructorCall2
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("X", new String[] {"I"});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/p/Y.java p.Y(int) [super(i)]\n" +
		"src/p/Y.java p.Y(boolean) [super(1)]", 
		this.resultCollector);
}
/**
 * Constructor reference using an implicit constructor call.
 * (regression test for bug 23112 search: need a way to search for references to the implicit non-arg constructor)
 */
public void testConstructorReference05() throws CoreException { // was testConstructorReferenceImplicitConstructorCall1
	IType type = getCompilationUnit("JavaSearch", "src", "c7", "X.java").getType("X");
	IMethod method = type.getMethod("X", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/c7/Y.java c7.Y() [Y]", 
		this.resultCollector);
}
/**
 * Constructor reference using an implicit constructor call.
 * (regression test for bug 23112 search: need a way to search for references to the implicit non-arg constructor)
 */
public void testConstructorReference06() throws CoreException { // was testConstructorReferenceImplicitConstructorCall2
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"c8.X()", 
		CONSTRUCTOR, 
		REFERENCES,
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/c8/Y.java c8.Y [Y]", 
		this.resultCollector);
}
/**
 * Constructor reference in a field initializer.
 * (regression test for PR 1GKZ8VZ: ITPJCORE:WINNT - Search - did not find references to member constructor)
 */
public void testConstructorReference07() throws CoreException { // was testConstructorReferenceInFieldInitializer
	IType type = getCompilationUnit("JavaSearch", "src", "", "A.java").getType("A").getType("Inner");
	IMethod method = type.getMethod("Inner", new String[] {"I"});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/A.java A.field [new Inner(1)]\n" +
		"src/A.java A.field [new Inner(2)]", 
		this.resultCollector);
}
/**
 * Constructor reference in case of default constructor of member type
 * (regression test for bug 43276)
 */
public void testConstructorReference08() throws CoreException { // was testConstructorReferenceDefaultConstructorOfMemberClass
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"c10.X.Inner()", 
		CONSTRUCTOR, 
		REFERENCES,
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/c10/X.java c10.B() [new X().super()]", 
		this.resultCollector);
}
/*
 * Generic constructor reference
 */
public void testConstructorReference09() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15/src/p2/X.java").getType("X");
	IMethod method = type.getMethod("X", new String[] {"QE;"});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope15(), 
		this.resultCollector);
	assertSearchResults(
		"src/p2/Y.java Object p2.Y.foo() [new X<Object>(this)]",
		this.resultCollector);
}
/**
 * Constructor reference using an implicit constructor call.
 * (regression test for bug 23112 search: need a way to search for references to the implicit non-arg constructor)
 */
public void testConstructorReference10() throws CoreException { // was testConstructorReferenceImplicitConstructorCall2
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showSynthetic = true;
	search(
		"c11.A()", 
		CONSTRUCTOR, 
		REFERENCES,
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/c11/A.java c11.A1 [A1] SYNTHETIC\n" + 
		"src/c11/A.java c11.A2() [A2] SYNTHETIC\n" + 
		"src/c11/A.java c11.A3() [super()]",
		this.resultCollector);
}
/**
 * CoreException thrown during accept.
 * (regression test for PR #1G3UI7A)
 */
public void testCoreException() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	SearchRequestor result = new SearchRequestor() {
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			throw new CoreException(new JavaModelStatus(-1, "test"));
		}
	};
	try {
		search(
			type, 
			DECLARATIONS, 
			getJavaSearchScope(), 
			result);
	} catch (CoreException e) {
		assertEquals("Unexpected CoreException has been thrown", "test", e.getStatus().getMessage());
		return;
	}
	assertTrue("CoreException should have been thrown", false);
}
/**
 * Declaration of accessed fields test.
 * (regression test for bug 6538 searchDeclarationsOf* incorrect)
 */
public void testDeclarationOfAccessedFields1() throws CoreException {
	IMethod method = 
		getCompilationUnit("JavaSearch", "src", "a5", "B.java").
			getType("C").getMethod("i", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfAccessedFields(
		method, 
		resultCollector
	);
	assertSearchResults(
		"", 
		this.resultCollector);
}
/**
 * Declaration of accessed fields test.
 * (regression test for bug 6538 searchDeclarationsOf* incorrect)
 */
public void testDeclarationOfAccessedFields2() throws CoreException {
	IMethod method = 
		getCompilationUnit("JavaSearch", "src", "a6", "A.java").
			getType("B").getMethod("m", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfAccessedFields(
		method, 
		resultCollector
	);
	assertSearchResults(
		"src/a6/A.java a6.B.f [f]", 
		this.resultCollector);
}
/**
 * Declaration of accessed fields test.
 * (regression test for bug 10386 NPE in MatchLocator.lookupType)
 */
public void testDeclarationOfAccessedFields3() throws CoreException {
	IMethod method = 
		getCompilationUnit("JavaSearch", "src", "b6", "A.java").
			getType("A").getMethod("foo", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfAccessedFields(
		method, 
		resultCollector
	);
	assertSearchResults(
		"src/b6/A.java b6.A.field [field]", 
		this.resultCollector);
}
/**
 * Declaration of referenced types test.
 */
public void testDeclarationOfReferencedTypes01() throws CoreException {
	IMethod method = 
		getCompilationUnit("JavaSearch", "src", "a3", "References.java").
			getType("References").getMethod("foo", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfReferencedTypes(
		method, 
		resultCollector
	);
	assertSearchResults(
		"src/a3/X.java a3.X [X]\n" +
		"src/a3/Z.java a3.Z [Z]\n" +
		"src/a3/b/A.java a3.b.A [A]\n" +
		"src/a3/b/A.java a3.b.A$B$C [C]\n" +
		"src/a3/b/A.java a3.b.A$B [B]\n" +
		getExternalJCLPathString() + " java.lang.Object\n" +
		"src/a3/Y.java a3.Y [Y]\n" +
		"src/a3/b/B.java a3.b.B [B]", 
		this.resultCollector);
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 6779 searchDeclarationsOfReferencedTyped - missing exception types)
 */
public void testDeclarationOfReferencedTypes02() throws CoreException {
	IMethod method = 
		getCompilationUnit("JavaSearch", "src", "a7", "X.java").
			getType("X").getMethod("foo", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfReferencedTypes(
		method, 
		resultCollector
	);
	assertSearchResults(
		"src/a7/X.java a7.MyException [MyException]", 
		this.resultCollector);
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 12649 Missing import after move  )
 */
public void testDeclarationOfReferencedTypes03() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch", "src", "c1", "A.java");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfReferencedTypes(
		unit, 
		resultCollector
	);
	assertSearchResults(
		"src/c1/I.java c1.I [I]", 
		this.resultCollector);
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 12649 Missing import after move  )
 */
public void testDeclarationOfReferencedTypes04() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch", "src", "c1", "B.java");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfReferencedTypes(
		unit, 
		resultCollector
	);
	assertSearchResults(
		"src/c1/I.java c1.I [I]", 
		this.resultCollector);
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 18418 search: searchDeclarationsOfReferencedTypes reports import declarations)
 */
public void testDeclarationOfReferencedTypes05() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch", "src", "c2", "A.java");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfReferencedTypes(
		unit, 
		resultCollector
	);
	assertSearchResults(
		"src/c3/C.java c3.C [C]", 
		this.resultCollector);
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 24934 Move top level doesn't optimize the imports[refactoring])
 */
public void testDeclarationOfReferencedTypes06() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch", "src", "d1", "X.java");
	IType innerType = unit.getType("X").getType("Inner");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfReferencedTypes(
		innerType, 
		resultCollector
	);
	assertSearchResults(
		"src/d2/Y.java d2.Y [Y]", 
		this.resultCollector);
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 37438 searchenging NPE in searchDeclarationsOfReferencedTypes 
)
 */
public void testDeclarationOfReferencedTypes07() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "r7");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfReferencedTypes(
		pkg, 
		resultCollector
	);
	assertSearchResults(
		"", 
		this.resultCollector);
}

/**
 * Declaration of referenced types test.
 * (Regression test for bug 47787 IJavaSearchResultCollector.aboutToStart() and done() not called)
 */
public void testDeclarationOfReferencedTypes08() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "r7");
	JavaSearchResultCollector result = new JavaSearchResultCollector() {
	    public void beginReporting() {
	        results.append("Starting search...");
        }
	    public void endReporting() {
	        results.append("\nDone searching.");
        }
	};
	searchDeclarationsOfReferencedTypes(
		pkg, 
		result
	);
	assertSearchResults(
		"Starting search...\n"+
		"Done searching.", 
		result);
}

/**
 * Declaration of referenced types test.
 * (Regression test for bug 68862 [1.5] ClassCastException when moving a a java file 
)
 */
public void testDeclarationOfReferencedTypes09() throws CoreException {
	ICompilationUnit cu = getCompilationUnit("JavaSearch15/src/p3/X.java");
	JavaSearchResultCollector result = new JavaSearchResultCollector() {
	    public void beginReporting() {
	        results.append("Starting search...");
        }
	    public void endReporting() {
	        results.append("\nDone searching.");
        }
	};
	searchDeclarationsOfReferencedTypes(
		cu, 
		result
	);
	assertSearchResults(
		"Starting search...\n" + 
		getExternalJCLPathString("1.5") + " java.lang.Object\n" + 
		"Done searching.",
		result);
}

/**
 * Declaration of referenced types test.
 * (Regression test for bug 71279: [Search] NPE in TypeReferenceLocator when moving CU with unresolved type reference
)
 */
public void testDeclarationOfReferencedTypes10() throws CoreException {
	ICompilationUnit cu = getCompilationUnit("JavaSearchBugs/src/b71279/AA.java");
	JavaSearchResultCollector result = new JavaSearchResultCollector() {
	    public void beginReporting() {
	        results.append("Starting search...");
        }
	    public void endReporting() {
	        results.append("\nDone searching.");
        }
	};
	searchDeclarationsOfReferencedTypes(
		cu, 
		result
	);
	assertSearchResults(
		"Starting search...\n" + 
		"Done searching.",
		result);
}
/**
 * Simple declarations of sent messages test.
 */
public void testDeclarationsOfSentMessages01() throws CoreException { // was testSimpleDeclarationsOfSentMessages
	ICompilationUnit cu = getCompilationUnit("JavaSearch", "src", "", "Test.java");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfSentMessages(
		cu, 
		this.resultCollector);
	assertSearchResults(
		"src/p/X.java void p.X.foo(int, String, X) [foo(int i, String s, X x)]\n" + 
		"src/p/Y.java void p.Y.bar() [bar()]\n" + 
		"src/p/Z.java void p.Z.foo(int, String, X) [foo(int i, String s, X x)]\n" + 
		"src/p/A.java void p.A.foo(int, String, X) [foo()]",
		this.resultCollector);
}

/**
 * Declaration of sent messages test.
 * (regression test for bug 6538 searchDeclarationsOf* incorrect)
 */
public void testDeclarationsOfSentMessages02() throws CoreException { // was testDeclarationOfSentMessages
	IMethod method = 
		getCompilationUnit("JavaSearch", "src", "a5", "B.java").
			getType("C").getMethod("i", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfSentMessages(
		method, 
		resultCollector
	);
	assertSearchResults(
		"", 
		this.resultCollector);
}
/**
 * Simple field declaration test.
 */
public void testFieldDeclaration01() throws CoreException { // was testSimpleFieldDeclaration
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("x");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/p/A.java p.A.x [x]", 
		this.resultCollector);
}
/**
 * Field declaration in jar file test.
 */
public void testFieldDeclaration02() throws CoreException { // was testFieldDeclarationInJar
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p1", "A.class").getType();
	IField field = type.getField("field");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"MyJar.jar p1.A.field [No source]", 
		this.resultCollector);
}


/**
 * Field declaration with array type test.
 * (regression test for PR 1GKEG73: ITPJCORE:WIN2000 - search (136): missing field declaration)
 */
public void testFieldDeclaration03() throws CoreException { // was testFieldDeclarationArrayType
	IType type = getCompilationUnit("JavaSearch", "src", "", "B.java").getType("B");
	IField field = type.getField("open");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/B.java B.open [open]", 
		this.resultCollector);
}
/**
 * Field declaration with wild card test.
 * (regression test for bug 21763 Problem in Java search [search]  )
 */
public void testFieldDeclaration04() throws CoreException { // was testFieldDeclarationWithWildCard
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"class*path", 
		FIELD,
		DECLARATIONS,
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/c5/Test.java c5.Test.class_path [class_path]", 
		this.resultCollector);
}
/**
 * Field reference test.
 * (regression test for bug #3433 search: missing field occurrecnces (1GKZ8J6))
 */
public void testFieldReference01() throws CoreException { // was testFieldReference
	IType type = getCompilationUnit("JavaSearch", "src", "p8", "A.java").getType("A");
	IField field = type.getField("g");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/p8/A.java void p8.A.m() [g]\n" + 
		"src/p8/A.java void p8.B.m() [g]",
		this.resultCollector);
}
/**
 * Field reference test.
 * (regression test for PR 1GK8TXE: ITPJCORE:WIN2000 - search: missing field reference)
 */
public void testFieldReference02() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p9", "X.java").getType("X");
	IField field = type.getField("f");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/p9/X.java void p9.X.m() [f]",
		this.resultCollector);
}
/**
 * Field reference test.
 * (regression test for bug 5821 Refactor > Rename renames local variable instead of member in case of name clash  )
 */
public void testFieldReference03() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "q8", "EclipseTest.java").getType("EclipseTest");
	IField field = type.getField("test");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/q8/EclipseTest.java void q8.EclipseTest.main(String[]) [test]",
		this.resultCollector);
}
/**
 * Field reference test.
 * (regression test for bug 5923 Search for "length" field refs finds [].length)
 */
public void testFieldReference04() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "a2", "X.java").getType("X");
	IField field = type.getField("length");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/a2/X.java void a2.X.foo() [length]",
		this.resultCollector);
}
/**
 * Field reference test.
 * (regression test for bug 7987 Field reference search should do lookup in 1.4 mode)
 */
public void testFieldReference05() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "b1", "A.java").getType("A");
	IField field = type.getField("x");
	
	// Set 1.4 compliance level (no constant yet)
	Hashtable options = JavaCore.getOptions();
	String currentOption = (String)options.get("org.eclipse.jdt.core.compiler.compliance");
	options.put("org.eclipse.jdt.core.compiler.compliance", "1.4");
	JavaCore.setOptions(options);
	
	try {
	//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
			field, 
			REFERENCES, 
			getJavaSearchScope(), 
			this.resultCollector);
		assertSearchResults(
			"src/b1/B.java void b1.B.foo() [x]",
			this.resultCollector);
	} finally {
		// Restore compliance level
		options.put("org.eclipse.jdt.core.compiler.compliance", currentOption);
		JavaCore.setOptions(options);
	}
}
/**
 * Field reference test.
 * (regression test for bug 20693 Finding references to variables does not find all occurances)
 */
public void testFieldReference06() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "c4", "X.java").getType("X");
	IField field = type.getField("x");
	
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/c4/X.java int c4.X.foo() [x]",
		this.resultCollector);
}
/**
 * Field reference test.
 * (regression test for bug 61017 Refactoring - test case that results in uncompilable source)
 */
public void testFieldReference07() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "s5", "A.java").getType("A");
	IField field = type.getField("b");
	
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/s5/A.java void s5.A.method() [b]",
		this.resultCollector);
}
/**
 * Simple field reference test.
 */
public void testFieldReference08() throws CoreException { // was testSimpleFieldReference
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("x");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [x]\n" + 
		"src/p/A.java p.A(X) [x]",
		this.resultCollector);
}
/**
 * Simple field read access reference test.
 */
public void testFieldReference09() throws CoreException { // was testSimpleReadFieldReference
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("x");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		READ_ACCESSES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [x]",
		this.resultCollector);
}
/**
 * Simple write field access reference test.
 */
public void testFieldReference10() throws CoreException { // was testSimpleWriteFieldReference
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("x");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		WRITE_ACCESSES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/p/A.java p.A(X) [x]", 
		this.resultCollector);
}
/**
 * Multiple field references in one ast test.
 * (regression test for PR 1GD79XM: ITPJCORE:WINNT - Search - search for field references - not all found)
 */
public void testFieldReference11() throws CoreException { // was testMultipleFieldReference
	IType type = getCompilationUnit("JavaSearch", "src", "p5", "A.java").getType("A");
	IField field = type.getField("x");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/p5/A.java void p5.A.k() [x]\n" + 
		"src/p5/A.java void p5.A.k() [x]\n" + 
		"src/p5/A.java void p5.A.k() [x]",
		this.resultCollector);
}
/**
 * Static field reference test.
 * (regression test for PR #1G2P5EP)
 */
public void testFieldReference12() throws CoreException { // was testStaticFieldReference
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("DEBUG");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/p/A.java void p.A.foo() [DEBUG]",
		this.resultCollector);
}
/**
 * Field reference in inner class test.
 * (regression test for PR 1GL11J6: ITPJCORE:WIN2000 - search: missing field references (nested types))
 */
public void testFieldReference13() throws CoreException { // was testFieldReferenceInInnerClass
	IType type = getCompilationUnit("JavaSearch", "src", "", "O.java").getType("O");
	IField field = type.getField("y");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/O.java void O$I.y() [y]",
		this.resultCollector);
}
/**
 * Field reference in anonymous class test.
 * (regression test for PR 1GL12XE: ITPJCORE:WIN2000 - search: missing field references in inner class)
 */
public void testFieldReference14() throws CoreException { // was testFieldReferenceInAnonymousClass
	IType type = getCompilationUnit("JavaSearch", "src", "", "D.java").getType("D");
	IField field = type.getField("h");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/D.java void void D.g():<anonymous>#1.run() [h]",
		this.resultCollector);
}
/**
 * Field reference through subclass test.
 * (regression test for PR 1GKB9YH: ITPJCORE:WIN2000 - search for field refs - incorrect results)
 */
public void testFieldReference15() throws CoreException { // was testFieldReferenceThroughSubclass
	IType type = getCompilationUnit("JavaSearch", "src", "p6", "A.java").getType("A");
	IField field = type.getField("f");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/p6/A.java void p6.A.m() [f]\n" + 
		"src/p6/A.java void p6.B.m() [f]\n" + 
		"src/p6/A.java void p6.B.m() [f]",
		this.resultCollector);
		
	type = getCompilationUnit("JavaSearch", "src", "p6", "A.java").getType("AA");
	field = type.getField("f");
	resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/p6/A.java void p6.B.m() [f]",
		this.resultCollector);
		
}
/**
 * Read and write access reference in compound expression test.
 * (regression test for bug 6158 Search - Prefix and postfix expression not found as write reference)
 */
public void testFieldReference16() throws CoreException { // was testReadWriteFieldReferenceInCompoundExpression
	IType type = getCompilationUnit("JavaSearch", "src", "a4", "X.java").getType("X");
	IField field = type.getField("field");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	
	// Read reference
	search(
		field, 
		READ_ACCESSES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/a4/X.java void a4.X.foo() [field]",
		this.resultCollector);
		
	// Write reference
	resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		WRITE_ACCESSES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/a4/X.java void a4.X.foo() [field]",
		this.resultCollector);
		
}
/**
 * Write access reference in a qualified name reference test.
 * (regression test for bug 7344 Search - write acces give wrong result)
 */
public void testFieldReference17() throws CoreException { // was testReadWriteAccessInQualifiedNameReference
	IType type = getCompilationUnit("JavaSearch", "src", "a8", "A.java").getType("A");
	IField field = type.getField("a");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	
	resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		WRITE_ACCESSES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"", 
		this.resultCollector);
		
}
/**
 * Field reference in brackets test.
 * (regression test for bug 23329 search: incorrect range for type references in brackets)
 */
public void testFieldReference18() throws CoreException { // was testFieldReferenceInBrackets
	IType type = getCompilationUnit("JavaSearch", "src", "s3", "A.java").getType("A");
	IField field = type.getField("field");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/s3/A.java int s3.A.bar() [field]",
		this.resultCollector);
}

/**
 * Accurate field reference test.
 */
public void testFieldReference19() throws CoreException { // was testAccurateFieldReference1
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"d6.X.CONSTANT", 
		FIELD, 
		REFERENCES,
		SearchEngine.createJavaSearchScope(new IJavaElement[] {
			getPackageFragment("JavaSearch", "src", "d6")
		}), 
		this.resultCollector);
	assertSearchResults(
		"src/d6/Y.java d6.Y.T [CONSTANT]",
		this.resultCollector);
}
/**
 * Field reference inside/outside doc comment.
 */
public void testFieldReference20() throws CoreException { // was testFieldReferenceInOutDocComment
	IType type = getCompilationUnit("JavaSearch", "src", "s4", "X.java").getType("X");
	IField field = type.getField("x");
	
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showInsideDoc = true;
	search(field, REFERENCES, getJavaSearchScope(), resultCollector);
	assertSearchResults(
		"src/s4/X.java int s4.X.foo() [x] OUTSIDE_JAVADOC\n" + 
		"src/s4/X.java void s4.X.bar() [x] INSIDE_JAVADOC",
		this.resultCollector);
}
/**
 * Field reference in static import
 */
public void testFieldReference21() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "s1.j.l", "S.java").getType("S");
	IField field = type.getField("in");
	search(field, REFERENCES, getJavaSearchScope15(), resultCollector);
	assertSearchResults(
		"src/s1/A.java [s1.j.l.S.in]\n" + 
		"src/s1/B.java void s1.B.foo() [in]\n" + 
		"src/s1/D.java [s1.j.l.S.in]\n" + 
		"src/s1/D.java void s1.D.foo() [in]",
		this.resultCollector);
}
/**
 * Field reference test.
 * (regression test for bug 73112: [Search] SearchEngine doesn't find all fields multiple field declarations
 */
public void testFieldReferenceBug73112a() throws CoreException {
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"fieldA73112*",
		FIELD,
		ALL_OCCURRENCES,
		getJavaSearchScopeBugs(), 
		this.resultCollector);
	assertSearchResults(
		"src/b73112/A.java b73112.A.fieldA73112a [fieldA73112a]\n" + 
		"src/b73112/A.java b73112.A.fieldA73112b [fieldA73112b]\n" + 
		"src/b73112/A.java b73112.A.fieldA73112c [fieldA73112c]\n" + 
		"src/b73112/A.java b73112.A.fieldA73112c [fieldA73112a]\n" + 
		"src/b73112/A.java b73112.A.fieldA73112c [fieldA73112b]\n" + 
		"src/b73112/A.java b73112.A.fieldA73112d [fieldA73112d]",
		this.resultCollector);
}
public void testFieldReferenceBug73112b() throws CoreException {
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"fieldB73112*",
		FIELD,
		ALL_OCCURRENCES,
		getJavaSearchScopeBugs(), 
		this.resultCollector);
	assertSearchResults(
		"src/b73112/B.java b73112.B.fieldB73112a [fieldB73112a]\n" + 
		"src/b73112/B.java b73112.B.fieldB73112b [fieldB73112b]\n" + 
		"src/b73112/B.java b73112.B.fieldB73112c [fieldB73112c]\n" + 
		"src/b73112/B.java b73112.B.fieldB73112c [fieldB73112a]\n" + 
		"src/b73112/B.java b73112.B.fieldB73112c [fieldB73112b]\n" + 
		"src/b73112/B.java b73112.B.fieldB73112d [fieldB73112d]\n" + 
		"src/b73112/B.java b73112.B.fieldB73112d [fieldB73112c]\n" + 
		"src/b73112/B.java b73112.B.fieldB73112d [fieldB73112a]\n" + 
		"src/b73112/B.java b73112.B.fieldB73112e [fieldB73112e]",
		this.resultCollector);
}
/**
 * Interface implementors test.
 */
public void testInterfaceImplementors1() throws CoreException { // was testInterfaceImplementors
	// implementors of an interface
	IType type = getCompilationUnit("JavaSearch", "src", "p", "I.java").getType("I");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		IMPLEMENTORS, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/InterfaceImplementors.java InterfaceImplementors [p.I]\n" +
		"src/p/X.java p.X [I]", 
		this.resultCollector);

	// implementors of a class should give no match
	// (regression test for 1G5HBQA: ITPJUI:WINNT - Search - search for implementors of a class finds subclasses)
	type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		IMPLEMENTORS, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"", 
		this.resultCollector);	
}
/**
 * Interface implementors test.
 * (regression test for bug 22102 Not all implementors found for IPartListener)
 */
public void testInterfaceImplementors2() throws CoreException {
	// implementors of an interface
	IType type = getCompilationUnit("JavaSearch", "src", "r2", "I.java").getType("I");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		IMPLEMENTORS, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/r2/X.java r2.X.field:<anonymous>#1 [I]",
		this.resultCollector);
}
/*
 * Local variable declaration test.
 * (SingleNameReference)
 */
public void testLocalVariableDeclaration1() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var1 = 1;", "var1");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		localVar, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/f1/X.java void f1.X.foo1().var1 [var1]",
		this.resultCollector);
}
/*
 * Local variable declaration test.
 * (QualifiedNameReference)
 */
public void testLocalVariableDeclaration2() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var2 = new X();", "var2");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		localVar, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/f1/X.java void f1.X.foo2().var2 [var2]",
		this.resultCollector);
}
/*
 * Local variable occurrences test.
 * (SingleNameReference)
 */
public void testLocalVariableOccurrences1() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var1 = 1;", "var1");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		localVar, 
		ALL_OCCURRENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/f1/X.java void f1.X.foo1().var1 [var1]\n" + 
		"src/f1/X.java void f1.X.foo1() [var1]",
		this.resultCollector);
}
/*
 * Local variable occurences test.
 * (QualifiedNameReference)
 */
public void testLocalVariableOccurrences2() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var2 = new X();", "var2");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		localVar, 
		ALL_OCCURRENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/f1/X.java void f1.X.foo2().var2 [var2]\n" + 
		"src/f1/X.java void f1.X.foo2() [var2]",
		this.resultCollector);
}
/*
 * Local variable reference test.
 * (SingleNameReference)
 */
public void testLocalVariableReference1() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var1 = 1;", "var1");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		localVar, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/f1/X.java void f1.X.foo1() [var1]",
		this.resultCollector);
}
/*
 * Local variable reference test.
 * (QualifiedNameReference)
 */
public void testLocalVariableReference2() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var2 = new X();", "var2");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		localVar, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/f1/X.java void f1.X.foo2() [var2]",
		this.resultCollector);
}
/*
 * Local variable reference test.
 * (regression test for bug 48725 Cannot search for local vars in jars.)
 */
public void testLocalVariableReference3() throws CoreException {
    IClassFile classFile = getClassFile("JavaSearch", "test48725.jar", "p", "X.class");
	ILocalVariable localVar = (ILocalVariable) codeSelect(classFile, "local = 1;", "local")[0];
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		localVar, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"test48725.jar void p.X.foo()",
		this.resultCollector);
}
/**
 * Simple method declaration test.
 */
public void testMethodDeclaration01() throws CoreException { // was testSimpleMethodDeclaration
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[] {"I", "QString;", "QX;"});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/p/X.java void p.X.foo(int, String, X) [foo]\n" + 
		"src/p/Z.java void p.Z.foo(int, String, X) [foo]",
		this.resultCollector);
}
/**
 * Method declaration test.
 * (regression test for bug 38568 Search for method declarations fooled by array types)
 */
public void testMethodDeclaration02() throws CoreException { // was testMethodDeclaration
	IType type = getCompilationUnit("JavaSearch", "src", "e2", "X.java").getType("X");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"foo(String, String)", 
		METHOD,
		DECLARATIONS,
		SearchEngine.createJavaSearchScope(new IJavaElement[] {type}), 
		this.resultCollector);
	assertSearchResults(
		"src/e2/X.java void e2.X.foo(String, String) [foo]",
		this.resultCollector);
}


/**
 * Inner method declaration test.
 */
public void testMethodDeclaration03() throws CoreException { // was testInnerMethodDeclaration
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X").getType("Inner");
	IMethod method = type.getMethod("foo", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/p/X.java String p.X$Inner.foo() [foo]",
		this.resultCollector);
}
/**
 * Method declaration in hierarchy test.
 */
public void testMethodDeclaration04() throws CoreException { // was testMethodDeclarationInHierarchyScope1
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"foo", 
		METHOD,
		DECLARATIONS,
		SearchEngine.createHierarchyScope(type), 
		this.resultCollector);
	assertSearchResults(
		"src/p/X.java void p.X.foo(int, String, X) [foo]\n" + 
		"src/p/Z.java void p.Z.foo(int, String, X) [foo]",
		this.resultCollector);
}
/**
 * Method declaration in hierarchy that contains elements in external jar.
 * (regression test for PR #1G2E4F1)
 */
public void testMethodDeclaration05() throws CoreException { // was testMethodDeclarationInHierarchyScope2
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[] {"I", "QString;", "QX;"});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		DECLARATIONS,
		SearchEngine.createHierarchyScope(type), 
		this.resultCollector);
	assertSearchResults(
		"src/p/X.java void p.X.foo(int, String, X) [foo]\n" + 
		"src/p/Z.java void p.Z.foo(int, String, X) [foo]",
		this.resultCollector);
}
/**
 * Method declaration in hierarchy on a secondary type.
 */
public void testMethodDeclaration06() throws CoreException { // was testMethodDeclarationInHierarchyScope3
	IType type = getCompilationUnit("JavaSearch", "src", "d3", "A.java").getType("B");
	IMethod method = type.getMethod("foo", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		DECLARATIONS,
		SearchEngine.createHierarchyScope(type), 
		this.resultCollector);
	assertSearchResults(
		"src/d3/A.java void d3.B.foo() [foo]",
		this.resultCollector);
}
/**
 * Method declaration in package test.
 * (regression tets for PR #1G2KA97)
 */
public void testMethodDeclaration07() throws CoreException { // was testMethodDeclarationInPackageScope
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment()});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"main(String[])", 
		METHOD,
		DECLARATIONS,
		scope, 
		this.resultCollector);
	assertSearchResults(
		"src/p/A.java void p.A.main(String[]) [main]",
		this.resultCollector);
}
/**
 * Method declaration in jar file test.
 */
public void testMethodDeclaration08() throws CoreException { // was testMethodDeclarationInJar
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p1", "A.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"Ljava.lang.String;"});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"MyJar.jar boolean p1.A.foo(java.lang.String) [No source]",
		this.resultCollector);
}
/**
 * Method declaration in field initialzer.
 * (regression test for bug 24346 Method declaration not found in field initializer  )
 */
public void testMethodDeclaration09() throws CoreException { // was testMethodDeclarationInInitializer
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"foo24346", 
		METHOD,
		DECLARATIONS,
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/c6/X.java void c6.X.x:<anonymous>#1.foo24346() [foo24346]",
		this.resultCollector);
}
/*
 * Method declaration with a missing return type.
 * (regression test for bug 43080 NPE when searching in CU with incomplete method declaration)
 */
public void testMethodDeclaration10() throws CoreException { // was testMethodDeclarationNoReturnType
	IType type = getCompilationUnit("JavaSearch", "src", "e8", "A.java").getType("A");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment()});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"m() int", 
		METHOD,
		DECLARATIONS,
		scope, 
		this.resultCollector);
	assertSearchResults(
		"src/e8/A.java void e8.A.m() [m]",
		this.resultCollector);
}
/**
 * Method reference test.
 * (regression test for bug 5068 search: missing method reference)
 */
public void testMethodReference01() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "q5", "AQ.java").getType("I");
	IMethod method = type.getMethod("k", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/q5/AQ.java void q5.T.m() [k()]",
		this.resultCollector);
}
/**
 * Method reference test.
 * (regression test for bug 5069 search: method reference in super missing)
 */
public void testMethodReference02() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "q6", "CD.java").getType("AQ");
	IMethod method = type.getMethod("k", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/q6/CD.java void q6.AQE.k() [k()]",
		this.resultCollector);
}
/**
 * Method reference test.
 * (regression test for bug 5070 search: missing interface method reference  )
 */
public void testMethodReference03() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "q7", "AQ.java").getType("I");
	IMethod method = type.getMethod("k", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/q7/AQ.java void q7.D.h() [k()]",
		this.resultCollector);
}
/**
 * Method reference test.
 * (regression test for bug 8928 Unable to find references or declarations of methods that use static inner classes in the signature)
 */
public void testMethodReference04() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "b2", "Y.java").getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"QX.Inner;"});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/b2/Z.java void b2.Z.bar() [foo(inner)]",
		this.resultCollector);
}
/**
 * Method reference test.
 * (regression test for bug 49120 search doesn't find references to anonymous inner methods)
 */
public void testMethodReference05() throws CoreException {
	IType type = getCompilationUnit("JavaSearch/src/e9/A.java").getType("A").getMethod("foo", new String[] {}).getType("", 1);
	IMethod method = type.getMethod("bar", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/e9/A.java void e9.A.foo() [bar()]",
		this.resultCollector);
}
/*
 * Method reference in second anonymous and second local type of a method test.
 */
public void testMethodReference06() throws CoreException {
	IMethod method= getCompilationUnit("JavaSearch/src/f3/X.java").getType("X").getMethod("bar", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/f3/X.java void void f3.X.foo():<anonymous>#2.foobar() [bar()]\n" + 
		"src/f3/X.java void void f3.X.foo():Y#2.foobar() [bar()]",
		this.resultCollector);
}
/**
 * Simple method reference test.
 */
public void testMethodReference07() throws CoreException { // was testSimpleMethodReference
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[] {"I", "QString;", "QX;"});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [foo(1, \"a\", y)]\n" + 
		"src/Test.java void Test.main(String[]) [foo(1, \"a\", z)]\n" + 
		"src/p/Z.java void p.Z.foo(int, String, X) [foo(i, s, new Y(true))]",
		this.resultCollector);
}
/**
 * Static method reference test.
 */
public void testMethodReference08() throws CoreException { // was testStaticMethodReference1
	IType type = getCompilationUnit("JavaSearch", "src", "p", "Y.java").getType("Y");
	IMethod method = type.getMethod("bar", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [bar()]",
		this.resultCollector);
}
/**
 * Static method reference test.
 */
public void testMethodReference09() throws CoreException { // was testStaticMethodReference2
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("bar", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"", 
		this.resultCollector);
}
/**
 * Inner method reference test.
 */
public void testMethodReference10() throws CoreException { // was testInnerMethodReference
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X").getType("Inner");
	IMethod method = type.getMethod("foo", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/p/A.java void p.A.foo(int, String, X) [foo()]",
		this.resultCollector);
}
/**
 * Method reference through super test.
 */
public void testMethodReference11() throws CoreException { // was testMethodReferenceThroughSuper
	IType type = getCompilationUnit("JavaSearch", "src", "sd", "AQ.java").getType("AQ");
	IMethod method = type.getMethod("k", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/sd/AQ.java void sd.AQE.k() [k()]",
		this.resultCollector);
}
/**
 * Method reference in inner class test.
 */
public void testMethodReference12() throws CoreException { // was testMethodReferenceInInnerClass
	IType type = getCompilationUnit("JavaSearch", "src", "", "CA.java").getType("CA");
	IMethod method = type.getMethod("m", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/CA.java void CA$CB.f() [m()]\n" + 
		"src/CA.java void CA$CB$CC.f() [m()]",
		this.resultCollector);
}
/**
 * Method reference in anonymous class test.
 * (regression test for PR 1GGNOTF: ITPJCORE:WINNT - Search doesn't find method referenced in anonymous inner class)
 */
public void testMethodReference13() throws CoreException { // was testMethodReferenceInAnonymousClass
	IType type = getCompilationUnit("JavaSearch", "src", "", "PR_1GGNOTF.java").getType("PR_1GGNOTF");
	IMethod method = type.getMethod("method", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/PR_1GGNOTF.java void void PR_1GGNOTF.method2():<anonymous>#1.run() [method()]",
		this.resultCollector);
}
/**
 * Method reference through array test.
 * (regression test for 1GHDA2V: ITPJCORE:WINNT - ClassCastException when doing a search)
 */
public void testMethodReference14() throws CoreException { // was testMethodReferenceThroughArray
	IType type = getClassFile("JavaSearch", getExternalJCLPathString(), "java.lang", "Object.class").getType();
	IMethod method = type.getMethod("clone", new String[] {});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/E.java Object E.foo() [clone()]",
		this.resultCollector);
}
/**
 * Method reference inside/outside doc comment.
 */
public void testMethodReference15() throws CoreException { // was testMethodReferenceInOutDocComment
	IType type = getCompilationUnit("JavaSearch", "src", "s4", "X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[] {});
	
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showInsideDoc = true;
	search(method, REFERENCES, getJavaSearchScope(), resultCollector);
	assertSearchResults(
		"src/s4/X.java void s4.X.bar() [foo] INSIDE_JAVADOC\n" + 
		"src/s4/X.java void s4.X.fred() [foo()] OUTSIDE_JAVADOC",
		this.resultCollector);
}
/*
 * Generic method reference.
 */
public void testMethodReference16() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15/src/p2/X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[] {"QE;"});
	
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(method, REFERENCES, getJavaSearchScope15(), resultCollector);
	assertSearchResults(
		"src/p2/Y.java void p2.Y.bar() [foo(this)]",
		this.resultCollector);
}
/**
 * Regression test for bug 41018: Method reference not found
 * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=41018)
 */
public void testMethodReferenceBug41018() throws CoreException {
	IType type = getCompilationUnit("JavaSearchBugs", "src", "b41018", "A.java").getType("A");
	IMethod method = type.getMethod("methodA", new String[] { "QClassB.InnerInterface;" });
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScopeBugs(), 
		this.resultCollector);
	assertSearchResults(
		"src/b41018/A.java void b41018.A.anotherMethod() [methodA(null)]",
		this.resultCollector);
}
/**
 * Regression test for bug 70827: [Search] wrong reference match to private method of supertype
 * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=70827)
 */
public void testMethodReferenceBug70827() throws CoreException {
	IType type = getCompilationUnit("JavaSearchBugs", "src", "b70827", "A.java").getType("A");
	IMethod method = type.getMethod("privateMethod", new String[] {});
	search(method, REFERENCES, getJavaSearchScopeBugs(), resultCollector);
	assertSearchResults("", resultCollector);
}
/**
 * Regression test for bug 74776: [Search] Wrong search results for almost identical method
 * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=74776)
 */
public void testMethodReferenceBug74776() throws CoreException {
	IType type = getCompilationUnit("JavaSearchBugs", "src", "b74776", "A.java").getType("A");
	IMethod method = type.getMethod("foo", new String[] { "QRegion;" });
	search(method, REFERENCES, getJavaSearchScopeBugs(), resultCollector);
	assertSearchResults("", resultCollector);
}
/**
 * OrPattern test.
 * (regression test for bug 5862 search : too many matches on search with OrPattern)
 */
public void testOrPattern() throws CoreException {
	IMethod leftMethod = getCompilationUnit("JavaSearch", "src", "q9", "I.java")
		.getType("I").getMethod("m", new String[] {});
	SearchPattern leftPattern = createPattern(leftMethod, ALL_OCCURRENCES);
	IMethod rightMethod = getCompilationUnit("JavaSearch", "src", "q9", "I.java")
		.getType("A1").getMethod("m", new String[] {});
	SearchPattern rightPattern = createPattern(rightMethod, ALL_OCCURRENCES);
	SearchPattern orPattern = SearchPattern.createOrPattern(leftPattern, rightPattern);
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		orPattern, 
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/e8/A.java void e8.A.m() [m] POTENTIAL_MATCH\n" + 
		"src/q9/I.java void q9.I.m() [m] EXACT_MATCH\n" + 
		"src/q9/I.java void q9.A1.m() [m] EXACT_MATCH",
		this.resultCollector);
}
/**
 * Simple package declaration test.
 */
public void testPackageDeclaration1() throws CoreException { // was testSimplePackageDeclaration
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "p");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		pkg, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/p p", 
		this.resultCollector);
}

/**
 * Various package declarations test.
 */
public void testPackageDeclaration2() throws CoreException { // was testVariousPackageDeclarations
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"p3*", 
		PACKAGE,
		DECLARATIONS, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/p3 p3\n" +
		"src/p3/p2/p p3.p2.p", 
		this.resultCollector);
}
/**
 * Package declaration test.
 * (regression test for bug 62698 NPE while searching for declaration of binary package)
 */
public void testPackageDeclaration3() throws CoreException { // was testPackageDeclaration
	IPackageFragment pkg = getPackageFragment("JavaSearch", getExternalJCLPathString(), "java.lang");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		pkg, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		getExternalJCLPath() + " java.lang",
		this.resultCollector);
}
/**
 * Package declaration with corrupt jar on the classpath test.
 * (regression test for bug 75561 Rename package results in I/O exception)
 */
public void testPackageDeclaration4() throws CoreException {
	IJavaProject project = getJavaProject("JavaSearch");
	IClasspathEntry[] originalCP = project.getRawClasspath();
	try {
		// add corrupt.jar to classpath
		int cpLength = originalCP.length;
		IClasspathEntry[] newCP = new IClasspathEntry[cpLength+1];
		System.arraycopy(originalCP, 0, newCP, 0, cpLength);
		newCP[cpLength] = JavaCore.newLibraryEntry(new Path("/JavaSearch/corrupt.jar"), null, null);
		project.setRawClasspath(newCP, null);
		
	//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
			"r9",
			PACKAGE,
			DECLARATIONS, 
			getJavaSearchScope(), 
			this.resultCollector);
		assertSearchResults(
			"src/r9 r9", 
			this.resultCollector);
	} finally {
		project.setRawClasspath(originalCP, null);
	}
}
/**
 * Test fix for bug 73551: NPE while searching package declaration
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=73551">73551</a>
 * @throws CoreException
 */
public void testPackageDeclarationBug73551() throws CoreException {
	JavaSearchResultCollector result = new JavaSearchResultCollector();
	result.showAccuracy = true;
	IPackageDeclaration packDecl = getCompilationUnit("JavaSearch", "src", "p71267", "Test.java").getPackageDeclaration("p71267");
	search(packDecl, DECLARATIONS, getJavaSearchScope(),  result);
	assertSearchResults(
		"src/p71267/Test.java [No source] EXACT_MATCH",
		result);
}
/**
 * Package reference test.
 * (regression test for PR 1GK90H4: ITPJCORE:WIN2000 - search: missing package reference)
 */
public void testPackageReference1() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "q2");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		pkg, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/q1/B.java void q1.B.m(AA) [q2]",
		this.resultCollector);
}
/**
 * Package reference test.
 * (regression test for bug 17906 Rename package fails when inner classes are imported)
 */
public void testPackageReference2() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "b8");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		pkg, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/b9/Foo.java [b8]", 
		this.resultCollector);
}
/**
 * Package reference in jar test.
 * (regression test for bug 47989 Exception when searching for IPackageFragment "java.util.zip")
 */
public void testPackageReference3() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "test47989.jar", "p1");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg.getParent()});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		pkg, 
		REFERENCES, 
		scope, 
		this.resultCollector);
	assertSearchResults(
		"test47989.jar java.lang.Object p2.Y.foo()",
		this.resultCollector);
}
/**
 * Simple package reference test.
 */
public void testPackageReference4() throws CoreException { // was testSimplePackageReference
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "p");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		pkg, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/InterfaceImplementors.java InterfaceImplementors [p]\n" + 
		"src/Test.java void Test.main(String[]) [p]\n" + 
		"src/Test.java void Test.main(String[]) [p]\n" + 
		"src/Test.java void Test.main(String[]) [p]\n" + 
		"src/Test.java void Test.main(String[]) [p]\n" + 
		"src/Test.java void Test.main(String[]) [p]\n" + 
		"src/Test.java void Test.main(String[]) [p]\n" + 
		"src/Test.java void Test.main(String[]) [p]\n" + 
		"src/TypeReferenceInImport/X.java [p]",
		this.resultCollector);
}
/**
 * Various package reference test.
 */
public void testPackageReference5() throws CoreException { // was testVariousPackageReference
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "p3.p2.p");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		pkg, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/PackageReference/A.java [p3.p2.p]\n" + 
		"src/PackageReference/B.java [p3.p2.p]\n" + 
		"src/PackageReference/C.java PackageReference.C [p3.p2.p]\n" + 
		"src/PackageReference/D.java PackageReference.D.x [p3.p2.p]\n" + 
		"src/PackageReference/E.java PackageReference.E.x [p3.p2.p]\n" + 
		"src/PackageReference/F.java p3.p2.p.X PackageReference.F.foo() [p3.p2.p]\n" + 
		"src/PackageReference/G.java void PackageReference.G.foo(p3.p2.p.X) [p3.p2.p]\n" + 
		"src/PackageReference/H.java void PackageReference.H.foo() [p3.p2.p]\n" + 
		"src/PackageReference/I.java void PackageReference.I.foo() [p3.p2.p]\n" + 
		"src/PackageReference/J.java void PackageReference.J.foo() [p3.p2.p]",
		this.resultCollector);
}
/**
 * Regression test for 1GBK7B2: ITPJCORE:WINNT - package references: could be more precise
 */
public void testPackageReference6() throws CoreException { // was testAccuratePackageReference
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "p3.p2");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		pkg, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/PackageReference/K.java [p3.p2]", 
		this.resultCollector);
}
/**
 * Test pattern match package references
 */
public void testPackageReference7() throws CoreException { // was testPatternMatchPackageReference
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"*p2.*", 
		PACKAGE,
		REFERENCES,
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/PackageReference/A.java [p3.p2.p]\n" + 
		"src/PackageReference/B.java [p3.p2.p]\n" + 
		"src/PackageReference/C.java PackageReference.C [p3.p2.p]\n" + 
		"src/PackageReference/D.java PackageReference.D.x [p3.p2.p]\n" + 
		"src/PackageReference/E.java PackageReference.E.x [p3.p2.p]\n" + 
		"src/PackageReference/F.java p3.p2.p.X PackageReference.F.foo() [p3.p2.p]\n" + 
		"src/PackageReference/G.java void PackageReference.G.foo(p3.p2.p.X) [p3.p2.p]\n" + 
		"src/PackageReference/H.java void PackageReference.H.foo() [p3.p2.p]\n" + 
		"src/PackageReference/I.java void PackageReference.I.foo() [p3.p2.p]\n" + 
		"src/PackageReference/J.java void PackageReference.J.foo() [p3.p2.p]",
		this.resultCollector);
}
/**
 * Test pattern match package references
 * Just verify that there's no ArrayOutOfBoundException to validate fix for
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=64421
 */
public void testPackageReference8() throws CoreException { // was testPatternMatchPackageReference2
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"*", 
		PACKAGE,
		REFERENCES,
		getJavaSearchScope(), 
		this.resultCollector);
	resultCollector.toString();
}
/**
 * Package reference in static import
 */
public void testPackageReference9() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch15", "src", "s1.j.l");
	search(pkg, REFERENCES, getJavaSearchScope15(), resultCollector);
	assertSearchResults(
		"src/s1/A.java [s1.j.l]\n" + 
		"src/s1/A.java [s1.j.l]\n" + 
		"src/s1/B.java [s1.j.l]\n" + 
		"src/s1/D.java [s1.j.l]\n" + 
		"src/s1/D.java [s1.j.l]\n" + 
		"src/s1/E.java [s1.j.l]",
		this.resultCollector);
}
/**
 * Test that we find potential matches in binaries even if we can't resolve the entire
 * class file.
 * (Regression test for 1G4IN3E: ITPJCORE:WINNT - AbortCompilation using J9 to search for class declaration) 
 */
public void testPotentialMatchInBinary1() throws CoreException {
	IJavaProject project = this.getJavaProject("JavaSearch");
	IClasspathEntry[] classpath = project.getRawClasspath();
	try {
		// add AbortCompilation.jar to classpath
		int length = classpath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[length+1];
		System.arraycopy(classpath, 0, newClasspath, 0, length);
		newClasspath[length] = JavaCore.newLibraryEntry(new Path("/JavaSearch/AbortCompilation.jar"), null, null);
		project.setRawClasspath(newClasspath, null);
		
		// potential match for a field declaration
	//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showAccuracy = true;
		search(
			"MissingFieldType.*",
			FIELD,
			DECLARATIONS, 
			getJavaSearchScope(), 
			this.resultCollector);
		assertSearchResults(
			"AbortCompilation.jar AbortCompilation.MissingFieldType.field [No source] POTENTIAL_MATCH\n" + 
			"AbortCompilation.jar AbortCompilation.MissingFieldType.missing [No source] POTENTIAL_MATCH\n" + 
			"AbortCompilation.jar AbortCompilation.MissingFieldType.otherField [No source] POTENTIAL_MATCH",
			this.resultCollector);
	} finally {
		// reset classpath
		project.setRawClasspath(classpath, null);
	}
}	
/**
 * Test that we find potential matches in binaries even if we can't resolve the entire
 * class file.
 * (Regression test for 1G4IN3E: ITPJCORE:WINNT - AbortCompilation using J9 to search for class declaration) 
 */
public void testPotentialMatchInBinary2() throws CoreException {
	IJavaProject project = this.getJavaProject("JavaSearch");
	IClasspathEntry[] classpath = project.getRawClasspath();
	try {
		// add AbortCompilation.jar to classpath
		int length = classpath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[length+1];
		System.arraycopy(classpath, 0, newClasspath, 0, length);
		newClasspath[length] = JavaCore.newLibraryEntry(new Path("/JavaSearch/AbortCompilation.jar"), null, null);
		project.setRawClasspath(newClasspath, null);
		
		// potential match for a method declaration
	//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showAccuracy = true;
		search(
			"MissingArgumentType.foo*",
			METHOD,
			DECLARATIONS, 
			getJavaSearchScope(), 
			this.resultCollector);
		assertSearchResults(
			"AbortCompilation.jar void AbortCompilation.MissingArgumentType.foo() [No source] POTENTIAL_MATCH\n" + 
			"AbortCompilation.jar void AbortCompilation.MissingArgumentType.foo(java.util.EventListener) [No source] POTENTIAL_MATCH\n" + 
			"AbortCompilation.jar void AbortCompilation.MissingArgumentType.foo2() [No source] POTENTIAL_MATCH",
			this.resultCollector);
	} finally {
		// reset classpath
		project.setRawClasspath(classpath, null);
	}
}	
/**
 * Test that we find potential matches in binaries even if we can't resolve the entire
 * class file.
 * (Regression test for 1G4IN3E: ITPJCORE:WINNT - AbortCompilation using J9 to search for class declaration) 
 */
public void testPotentialMatchInBinary3() throws CoreException {
	IJavaProject project = this.getJavaProject("JavaSearch");
	IClasspathEntry[] classpath = project.getRawClasspath();
	try {
		// add AbortCompilation.jar to classpath
		int length = classpath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[length+1];
		System.arraycopy(classpath, 0, newClasspath, 0, length);
		newClasspath[length] = JavaCore.newLibraryEntry(new Path("/JavaSearch/AbortCompilation.jar"), null, null);
		project.setRawClasspath(newClasspath, null);
		
		// potential match for a type declaration
	//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showAccuracy = true;
		search(
			"Missing*",
			TYPE,
			DECLARATIONS, 
			getJavaSearchScope(), 
			this.resultCollector);
		assertSearchResults(
			"AbortCompilation.jar AbortCompilation.EnclosingType$MissingEnclosingType [No source] EXACT_MATCH\n" + 
			"AbortCompilation.jar AbortCompilation.MissingArgumentType [No source] EXACT_MATCH\n" + 
			"AbortCompilation.jar AbortCompilation.MissingFieldType [No source] EXACT_MATCH",
			this.resultCollector);
	} finally {
		// reset classpath
		project.setRawClasspath(classpath, null);
	}
}
/**
 * Hierarchy scope test.
 * (regression test for bug 3445 search: type hierarchy scope incorrect (1GLC8VS))
 */
public void testSearchScope01() throws CoreException { // was testHierarchyScope
	ICompilationUnit cu = this. getCompilationUnit("JavaSearch", "src", "a9", "A.java");
	IType type = cu.getType("C");
	IJavaSearchScope scope = SearchEngine.createHierarchyScope(type);
	assertTrue("a9.C should be included in hierarchy scope", scope.encloses(type));
	assertTrue("a9.A should be included in hierarchy scope", scope.encloses(cu.getType("A")));
	assertTrue("a9.B should be included in hierarchy scope", scope.encloses(cu.getType("B")));
	assertTrue("a9/A.java should be included in hierarchy scope", scope.encloses(cu.getUnderlyingResource().getFullPath().toString()));
}
/**
 * Sub-cu java search scope test.
 * (regression test for bug 9041 search: cannot create a sub-cu scope)
 */
public void testSearchScope02() throws CoreException { // was testSubCUSearchScope1
	IType type = getCompilationUnit("JavaSearch", "src", "b3", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES,
		scope, 
		this.resultCollector);
	assertSearchResults(
		"src/b3/X.java b3.X.field [X]\n" + 
		"src/b3/X.java Object b3.X.foo() [X]\n" + 
		"src/b3/X.java b3.X$Y.field2 [X]\n" + 
		"src/b3/X.java Object b3.X$Y.foo2() [X]",
		this.resultCollector);
}
/**
 * Sub-cu java search scope test.
 * (regression test for bug 9041 search: cannot create a sub-cu scope)
 */
public void testSearchScope03() throws CoreException { // was testSubCUSearchScope2
	IType type = getCompilationUnit("JavaSearch", "src", "b3", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getField("field")});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES,
		scope, 
		this.resultCollector);
	assertSearchResults(
		"src/b3/X.java b3.X.field [X]", 
		this.resultCollector);
}
/**
 * Sub-cu java search scope test.
 * (regression test for bug 9041 search: cannot create a sub-cu scope)
 */
public void testSearchScope04() throws CoreException { // was testSubCUSearchScope3
	IType type = getCompilationUnit("JavaSearch", "src", "b3", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getType("Y")});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES,
		scope, 
		this.resultCollector);
	assertSearchResults(
		"src/b3/X.java b3.X$Y.field2 [X]\n" + 
		"src/b3/X.java Object b3.X$Y.foo2() [X]",
		this.resultCollector);
}
/**
 * Java search scope on java element in external jar test.
 */
public void testSearchScope05() throws CoreException, IOException { // was testExternalJarScope
	IWorkspace workspace = ResourcesPlugin.getWorkspace();
	File workspaceLocation = new File(workspace.getRoot().getLocation().toOSString());
	File minimalJar = new File(workspaceLocation, "JavaSearch/MyJar.jar");
	File externalJar = new File(workspaceLocation.getParentFile(), "MyJar.jar");
	IJavaProject project = this.getJavaProject("JavaSearch");
	IClasspathEntry[] classpath = project.getRawClasspath();
	try {
		copy(minimalJar, externalJar);
		int length = classpath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[length];
		System.arraycopy(classpath, 0, newClasspath, 0, length-1);
		String externalPath = externalJar.getAbsolutePath();
		newClasspath[length-1] = JavaCore.newLibraryEntry(new Path(externalPath), new Path(externalPath), null, false);
		project.setRawClasspath(newClasspath, null);
		
		IPackageFragment pkg = this.getPackageFragment("JavaSearch", externalPath, "p0");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});
	//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
			"X", 
			TYPE,
			DECLARATIONS,
			scope,
			this.resultCollector);
		assertSearchResults(
			externalJar.getCanonicalPath()+ " p0.X",
			this.resultCollector);
			
		IClassFile classFile = pkg.getClassFile("X.class");
		scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {classFile});
		resultCollector = new JavaSearchResultCollector();
		search(
			classFile.getType(), 
			DECLARATIONS,
			scope,
			this.resultCollector);
		assertSearchResults(
			externalJar.getCanonicalPath()+ " p0.X",
			this.resultCollector);
		
	} finally {
		externalJar.delete();
		project.setRawClasspath(classpath, null);
	}
	
}
/**
 * Simple type declaration test.
 */
public void testTypeDeclaration01() throws CoreException { // was testSimpleTypeDeclaration
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		DECLARATIONS,
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults("src/p/X.java p.X [X]", resultCollector);
}
/**
 * Type declaration test.
 * (generic type)
 */
public void testTypeDeclaration02() throws CoreException {
	IPackageFragment pkg = this.getPackageFragment("JavaSearch15", "src", "p1");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"Y", 
		TYPE,
		DECLARATIONS,
		scope, 
		this.resultCollector);
	assertSearchResults(
		"src/p1/Y.java p1.Y [Y]",
		this.resultCollector);
}
/**
 * Type declaration test.
 * (regression test for bug 29524 Search for declaration via patterns adds '"*")
 */
public void testTypeDeclaration03() throws CoreException { // was testTypeDeclaration
	IPackageFragment pkg = this.getPackageFragment("JavaSearch", "src", "d8");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"A", 
		TYPE,
		DECLARATIONS,
		scope, 
		this.resultCollector);
	assertSearchResults("src/d8/A.java d8.A [A]", resultCollector);
}
/**
 * Type declaration in jar file test.
 */
public void testTypeDeclaration04() throws CoreException { // was testTypeDeclarationInJar
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p1", "A.class").getType();
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"MyJar.jar p1.A [No source]", 
		this.resultCollector);
}
/**
 * Type declaration in jar file and in anonymous class test.
 * (regression test for 20631 Declaration of local binary type not found)
 */
public void testTypeDeclaration05() throws CoreException { // was testTypeDeclarationInJar2
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaSearch", "test20631.jar");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {root});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"Y", 
		TYPE,
		DECLARATIONS, 
		scope, 
		this.resultCollector);
	assertSearchResults(
		"test20631.jar void X.foo()",
		this.resultCollector);
}
/**
 * Type declaration using a package scope test.
 * (check that subpackages are not included)
 */
public void testTypeDeclaration06() throws CoreException { // was testTypeDeclarationInPackageScope
	IType type = getCompilationUnit("JavaSearch", "src", "p3", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment()});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"X", 
		TYPE,
		DECLARATIONS,
		scope, 
		this.resultCollector);
	assertSearchResults(
		"src/p3/X.java p3.X [X]", 
		this.resultCollector);
}
/**
 * Type declaration using a binary package scope test.
 * (check that subpackages are not included)
 */
public void testTypeDeclaration07() throws CoreException { // was testTypeDeclarationInPackageScope2
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p0", "X.class").getType();
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment()});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"X", 
		TYPE,
		DECLARATIONS,
		scope, 
		this.resultCollector);
	assertSearchResults(
		"MyJar.jar p0.X [No source]", 
		this.resultCollector);
}
/**
 * Memeber type declaration test.
 * (regression test for bug 9992 Member class declaration not found)
 */
public void testTypeDeclaration08() throws CoreException { // was testMemberTypeDeclaration
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[]{
		this.getPackageFragment("JavaSearch", "src", "b4")
	});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"*.A.B", 
		TYPE,
		DECLARATIONS,
		scope, 
		this.resultCollector);
	assertSearchResults(
		"src/b4/A.java b4.A$B [B]", 
		this.resultCollector);
}
/**
 * Test pattern match type declaration
 * (regression test for bug 17210 No match found when query contains '?')
 */
public void testTypeDeclaration09() throws CoreException {
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"X?Z", 
		TYPE,
		DECLARATIONS,
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults("src/r5/XYZ.java r5.XYZ [XYZ]", resultCollector);
}
/**
 * Long declaration (>255) test.
 * (regression test for bug 25859 Error doing Java Search)
 */
public void testTypeDeclaration10() throws CoreException { // was testLongDeclaration
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"AbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyz", 
		TYPE, 
		DECLARATIONS,
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/c9/X.java c9.AbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyz [AbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyz]", 
		this.resultCollector);
}
/*
 * Local type declaration test.
 */
public void testTypeDeclaration11() throws CoreException { // was testLocalTypeDeclaration1
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "f2");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"Y", 
		TYPE,
		DECLARATIONS,
		scope, 
		this.resultCollector);
	assertSearchResults(
		"src/f2/X.java Object f2.X.foo1():Y#1 [Y]",
		this.resultCollector);
}
/*
 * Local type declaration test.
 */
public void testTypeDeclaration12() throws CoreException { // was testLocalTypeDeclaration2
	IType type = getCompilationUnit("JavaSearch/src/f2/X.java").getType("X").getMethod("foo", new String[0]).getType("Y", 1);
	
	IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		DECLARATIONS,
		scope, 
		this.resultCollector);
	assertSearchResults(
		"src/f2/X.java Object f2.X.foo1():Y#1 [Y]",
		this.resultCollector);
}
/**
 * Type declaration test.
 * Test fix for bug 73696: searching only works for IJavaSearchConstants.TYPE, but not CLASS or INTERFACE
 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=73696">73696</a>
 */
public void testTypeDeclarationBug73696() throws CoreException {
	IPackageFragment pkg = this.getPackageFragment("JavaSearchBugs", "src", "b73696");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	
	// Interface declaration
	TypeDeclarationPattern pattern = new TypeDeclarationPattern(
		null,
		null,
		null,
		IIndexConstants.INTERFACE_SUFFIX,
		SearchPattern.R_PATTERN_MATCH
	);
	new SearchEngine().search(
		pattern,
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		scope,
		resultCollector,
		null);
	// Class declaration
	pattern = new TypeDeclarationPattern(
		null,
		null,
		null,
		IIndexConstants.CLASS_SUFFIX,
		SearchPattern.R_PATTERN_MATCH
	);
	new SearchEngine().search(
		pattern,
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		scope,
		resultCollector,
		null);
	assertSearchResults(
		"src/b73696/I.java b73696.I [I]\n" + 
		"src/b73696/C.java b73696.C [C]",
		this.resultCollector);
}
/**
 * Type ocurrence test.
 * (regression test for PR 1GKAQJS: ITPJCORE:WIN2000 - search: incorrect results for nested types)
 */
public void testTypeOccurence1() throws CoreException { // was testTypeOccurence
	IType type = getCompilationUnit("JavaSearch", "src", "r", "A.java").getType("A").getType("X");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		ALL_OCCURRENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/r/A.java A r.A.m() [X]\n" + 
		"src/r/A.java r.A$X [X]\n" + 
		"src/r/A.java r.A$X(X) [X]\n" + 
		"src/r/A.java r.A$X(X) [X]\n" + 
		"src/r/A.java r.B.ax [A.X]\n" + 
		"src/r/A.java r.B.ax [X]",
		this.resultCollector);
}
/**
 * Type ocuurence in unresolvable import test.
 * (regression test for bug 37166 NPE in SearchEngine when matching type against ProblemReferenceBinding )
 */
public void testTypeOccurence2() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "r8", "B.java").getType("B");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment()});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		ALL_OCCURRENCES, 
		scope, 
		this.resultCollector);
	assertSearchResults(
		"src/r8/A.java [B]",
		this.resultCollector);
}
/**
 * Type occurences test.
 * Ensures that correct positions are reported for an inner type reference using a ALL_OCCURENCES pattern
 */
public void testTypeOccurence3() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "e4", "A.java").getType("A").getType("Inner");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		ALL_OCCURRENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/e4/A.java e4.A$Inner [Inner]\n" +
		"src/e5/A1.java [e4.A.Inner]\n" +
		"src/e5/A1.java e5.A1.a [e4.A.Inner]\n" +
		"src/e5/A1.java e5.A1.a1 [e4.A.Inner]\n" +
		"src/e5/A1.java e5.A1.a2 [Inner]\n" +
		"src/e5/A1.java e5.A1.a3 [Inner]",
		this.resultCollector);
}
/**
 * Type name with $ ocurrence test.
 * (regression test for bug 3310 Smoke 124: Compile errors introduced with rename refactoring (1GFBK2G))
 */
public void testTypeOccurence4() throws CoreException { // was testTypeOccurenceWithDollar
	IType type = getCompilationUnit("JavaSearch", "src", "q3", "A$B.java").getType("A$B");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		ALL_OCCURRENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/q3/A$B.java q3.A$B [A$B]\n" + 
		"src/q4/C.java Object q4.C.foo() [q3.A$B]",
		this.resultCollector);
}
/**
 * Type reference test.
 * (Regression test for PR 1GK7K17: ITPJCORE:WIN2000 - search: missing type reference)
 */
public void testTypeReference01() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "X.java").getType("X");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/X.java AA() [X]",
		this.resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 29516 SearchEngine regressions in 20030114)
 */
public void testTypeReference02() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "d7", "A.java").getType("A");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/d7/A.java d7.A.A [A]\n" + 
		"src/d7/A.java A d7.A.A(A) [A]\n" + 
		"src/d7/A.java A d7.A.A(A) [A]",
		this.resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 31985 NPE searching non-qualified and case insensitive type ref)
 */
public void testTypeReference03() throws CoreException {
	SearchPattern pattern = createPattern("x31985", TYPE, REFERENCES, false);
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		pattern, 
		getJavaSearchScope(),
		this.resultCollector);
	assertSearchResults(
		"src/e3/X31985.java e3.X31985.CONSTANT [X31985] EXACT_MATCH\n" + 
		"src/e3/Y31985.java Object e3.Y31985.foo() [X31985] EXACT_MATCH",
		this.resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 31997 Refactoring d.n. work for projects with brackets in name.)
 */
public void testTypeReference04() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "otherSrc()", "", "X31997.java").getType("X31997");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"otherSrc()/Y31997.java Y31997 [X31997]",
		this.resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 48261 Search does not show results)
 */
public void testTypeReference05() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "test48261.jar", "p", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment().getParent()});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		scope, 
		this.resultCollector);
	assertSearchResults(
		"test48261.jar p.X$Y(java.lang.String)",
		this.resultCollector);
}
/**
 * Type reference test
 * (in a generic type)
 */
public void testTypeReference06() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15/src/p1/X.java").getType("X");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchPackageScope("p1", true), 
		this.resultCollector);
	assertSearchResults(
		"src/p1/Y.java Object p1.Y.foo() [X]",
		this.resultCollector);
}
/**
 * Simple type reference test.
 */
public void testTypeReference07() throws CoreException { // was testTypeDeclaration01
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/p/A.java p.A.x [X]\n" + 
		"src/p/A.java p.A(X) [X]\n" + 
		"src/p/A.java void p.A.foo(int, String, X) [X]\n" + 
		"src/p/X.java p.X() [X]\n" + 
		"src/p/X.java void p.X.foo(int, String, X) [X]\n" + 
		"src/p/Y.java p.Y [X]\n" + 
		"src/p/Z.java void p.Z.foo(int, String, X) [X]",
		this.resultCollector);
}
/**
 * Type reference in initializer test.
 * (regression test for PR #1G4GO4O)
 */
public void testTypeReference08() throws CoreException { // was testTypeReferenceInInitializer
	IType type = getCompilationUnit("JavaSearch", "src", "", "Test.java").getType("Test");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/Test.java Test.static {} [Test]\n" +
		"src/Test.java Test.static {} [Test]\n" +
		"src/Test.java Test.{} [Test]",
		this.resultCollector);
}
/**
 * Type reference as a single name reference test.
 */
public void testTypeReference09() throws CoreException { // was testTypeReferenceAsSingleNameReference
	IType type = getCompilationUnit("JavaSearch", "src", "", "TypeReferenceAsSingleNameReference.java").getType("TypeReferenceAsSingleNameReference");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/TypeReferenceAsSingleNameReference.java void TypeReferenceAsSingleNameReference.hasReference() [TypeReferenceAsSingleNameReference]\n" + 
		"src/TypeReferenceAsSingleNameReference.java void TypeReferenceAsSingleNameReference.hasReference() [TypeReferenceAsSingleNameReference]",
		this.resultCollector);
}
/**
 * Member type reference test.
 */
public void testTypeReference10() throws CoreException { // was testMemberTypeReference
	// references to second level member type
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		"BMember", 
		TYPE,
		REFERENCES,
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"References to type BMember",
		"src/MemberTypeReference/Azz.java void MemberTypeReference.Azz.poo() [BMember] EXACT_MATCH\n" + 
		"src/MemberTypeReference/Azz.java MemberTypeReference.Azz$AzzMember [BMember] EXACT_MATCH\n" + 
		"src/MemberTypeReference/Azz.java MemberTypeReference.X.val [BMember] EXACT_MATCH\n" + 
		"src/MemberTypeReference/B.java void MemberTypeReference.B.foo() [BMember] EXACT_MATCH",
		this.resultCollector);

	// references to first level member type
	resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		"AzzMember", 
		TYPE,
		REFERENCES,
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"References to type AzzMember",
		"src/MemberTypeReference/Azz.java MemberTypeReference.X.val [AzzMember] EXACT_MATCH\n" + 
		"src/MemberTypeReference/B.java void MemberTypeReference.B.foo() [AzzMember] EXACT_MATCH",
		this.resultCollector);

	// no reference to a field with same name as member type
	resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		"BMember", 
		FIELD,
		REFERENCES,
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"References to field BMember",
		"",
		this.resultCollector);
}
/**
 * Member type reference test.
 * (regression test for PR 1GL0MN9: ITPJCORE:WIN2000 - search: not consistent results for nested types)
 */
public void testTypeReference11() throws CoreException { // was testMemberTypeReference2
	IType type = getCompilationUnit("JavaSearch", "src", "a", "A.java").getType("A").getType("X");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/a/A.java a.B.ax [A.X]\n" +
		"src/a/A.java a.B.sx [S.X]", 
		this.resultCollector);
}
/**
 * Member type named "Object" reference test.
 * (regression test for 1G4GHPS: ITPJUI:WINNT - Strange error message in search)
 */
public void testTypeReference12() throws CoreException { // was testObjectMemberTypeReference
	IType type = getCompilationUnit("JavaSearch", "src", "ObjectMemberTypeReference", "A.java")
		.getType("A")
		.getType("Object");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/ObjectMemberTypeReference/A.java void ObjectMemberTypeReference.A.foo() [Object] EXACT_MATCH",
		this.resultCollector);
}
/**
 * Type reference inside a qualified name reference test.
 * (Regression test for PR #1G4TSC0)
 */
public void testTypeReference13() throws CoreException { // was testTypeReferenceInQualifiedNameReference
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [p.A]\n" + 
		"src/Test.java void Test.main(String[]) [p.A]\n" + 
		"src/p/A.java void p.A.foo() [A]",
		this.resultCollector);
}
/**
 * Type reference inside a qualified name reference test.
 * (Regression test for PR #1GLBP65)
 */
public void testTypeReference14() throws CoreException { // was testTypeReferenceInQualifiedNameReference2
	IType type = getCompilationUnit("JavaSearch", "src", "p4", "A.java").getType("A");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/p4/A.java p4.A.A [A]\n" + 
		"src/p4/A.java p4.X [p4.A]\n" + 
		"src/p4/A.java void p4.X.x() [p4.A]",
		this.resultCollector);
}
/**
 * Type reference inside a qualified name reference test.
 * (Regression test for PR 1GL9UMH: ITPJCORE:WIN2000 - search: missing type occurrences)
 */
public void testTypeReference15() throws CoreException { // was testTypeReferenceInQualifiedNameReference3
	IType type = getCompilationUnit("JavaSearch", "src", "", "W.java").getType("W");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/W.java int W.m() [W]",
		this.resultCollector);
}
/**
 * Type reference inside a qualified name reference test.
 * (Regression test for bug 16751 Renaming a class doesn't update all references  )
 */
public void testTypeReference16() throws CoreException { // was testTypeReferenceInQualifiedNameReference4
	IType type = getCompilationUnit("JavaSearch", "src", "b7", "X.java").getType("SubClass");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/b7/X.java void b7.Test.main(String[]) [SubClass]",
		this.resultCollector);
}
/**
 * Type reference in a folder that is not in the classpath.
 * (regression test for PR #1G5N8KS)
 */
public void testTypeReference17() throws CoreException { // was testTypeReferenceNotInClasspath
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/p/A.java p.A.x [X]\n" + 
		"src/p/A.java p.A(X) [X]\n" + 
		"src/p/A.java void p.A.foo(int, String, X) [X]\n" + 
		"src/p/X.java p.X() [X]\n" + 
		"src/p/X.java void p.X.foo(int, String, X) [X]\n" + 
		"src/p/Y.java p.Y [X]\n" + 
		"src/p/Z.java void p.Z.foo(int, String, X) [X]",
		this.resultCollector);
}
/**
 * Type reference inside an argument, a return type or a field type.
 * (Regression test for PR #1GA7QA1)
 */
public void testTypeReference18() throws CoreException { // was testVariousTypeReferences
	IType type = getCompilationUnit("JavaSearch", "src", "NoReference", "A.java").getType("A");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"", // no reference should be found
		this.resultCollector);
}
/**
 * Type reference in import test.
 * (regression test for PR #1GA7PAS)
 */
public void testTypeReference19() throws CoreException { // was testTypeReferenceInImport
	IType type = getCompilationUnit("JavaSearch", "src", "p2", "Z.java").getType("Z");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/TypeReferenceInImport/X.java [p2.Z]",
		this.resultCollector);
}
/**
 * Type reference in import test.
 * (regression test for bug 23077 search: does not find type references in some imports)
 */
public void testTypeReference20() throws CoreException { // was testTypeReferenceInImport2
	IType type = getCompilationUnit("JavaSearch", "src", "r6", "A.java").getType("A");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]",
		this.resultCollector);
}
/**
 * Type reference in array test.
 * (regression test for PR #1GAL424)
 */
public void testTypeReference21() throws CoreException { // was testTypeReferenceInArray
	IType type = getCompilationUnit("JavaSearch", "src", "TypeReferenceInArray", "A.java").getType("A");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/TypeReferenceInArray/A.java TypeReferenceInArray.A.a [A]\n" +
		"src/TypeReferenceInArray/A.java TypeReferenceInArray.A.b [TypeReferenceInArray.A]",
		this.resultCollector);
}
/**
 * Type reference in array test.
 * (regression test for bug 3230 Search - Too many type references for query ending with * (1GAZVGI)  )
 */
public void testTypeReference22() throws CoreException { // was testTypeReferenceInArray2
	IType type = getCompilationUnit("JavaSearch", "src", "s1", "X.java").getType("X");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/s1/Y.java s1.Y.f [X]",
		this.resultCollector);
}
/**
 * Negative type reference test.
 * (regression test for 1G52F7P: ITPJCORE:WINNT - Search - finds bogus references to class)
 */
public void testTypeReference23() throws CoreException { // testNegativeTypeReference
	IType type = getCompilationUnit("JavaSearch", "src", "p7", "A.java").getType("A");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"",
		this.resultCollector);
}
/**
 * Type reference in a throw clause test.
 * (Regression test for bug 6779 searchDeclarationsOfReferencedTyped - missing exception types)
 */
public void testTypeReference24() throws CoreException { // was testTypeReferenceInThrows
	IType type = getCompilationUnit("JavaSearch", "src", "a7", "X.java").getType("MyException");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/a7/X.java void a7.X.foo() [MyException] EXACT_MATCH",
		this.resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 9642 Search - missing inaccurate type matches)
 */
public void testTypeReference25() throws CoreException { // was testInnacurateTypeReference1
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"Zork", 
		TYPE, 
		REFERENCES,
		SearchEngine.createJavaSearchScope(new IJavaElement[] {
			getPackageFragment("JavaSearch", "src", "b5")
		}), 
		this.resultCollector);
	assertSearchResults(
		"src/b5/A.java [Zork]\n" +
		"src/b5/A.java b5.A.{} [Zork]\n" +
		"src/b5/A.java b5.A.{} [Zork]\n" +
		"src/b5/A.java b5.A.{} [Zork]",
		this.resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 9642 Search - missing inaccurate type matches)
 */
public void testTypeReference26() throws CoreException { // was testInnacurateTypeReference2
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"p.Zork", 
		TYPE, 
		REFERENCES,
		SearchEngine.createJavaSearchScope(new IJavaElement[] {
			getPackageFragment("JavaSearch", "src", "b5")
		}), 
		this.resultCollector);
	assertSearchResults(
		"src/b5/A.java b5.A.{} [Zork]\n" +
		"src/b5/A.java b5.A.{} [Zork]\n" +
		"src/b5/A.java b5.A.{} [Zork]",
		this.resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 21485  NPE when doing a reference search to a package)
 */
public void testTypeReference27() throws CoreException { // was testInnacurateTypeReference3
	IType type = getCompilationUnit("JavaSearch", "src", "r3", "A21485.java").getType("A21485");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/r4/B21485.java [r3.A21485] EXACT_MATCH\n" +
		"src/r4/B21485.java r4.B21485 [A21485] POTENTIAL_MATCH",
		this.resultCollector);
}
/**
 * Type reference in cast test.
 * (regression test for bug 23329 search: incorrect range for type references in brackets)
 */
public void testTypeReference28() throws CoreException { // was testTypeReferenceInCast
	IType type = getCompilationUnit("JavaSearch", "src", "s3", "A.java").getType("B");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/s3/A.java Object s3.A.foo() [B]",
		this.resultCollector);
}
/**
 * Test pattern match type reference in binary
 * (regression test for bug 24741 Search does not find patterned type reference in binary project  )
 */
public void testTypeReference29() throws CoreException { // was testPatternMatchTypeReference
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"p24741.*", 
		TYPE,
		REFERENCES,
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"test24741.jar q24741.B", 
		this.resultCollector);
}
/**
 * Type reference test (not case sensitive)
 */
public void testTypeReference30() throws CoreException { // was testTypeReferenceNotCaseSensitive
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "d4");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});
	SearchPattern pattern = createPattern("Y", TYPE, REFERENCES, false);
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		pattern, 
		scope, 
		this.resultCollector);
	assertSearchResults(
		"src/d4/X.java Object d4.X.foo() [Y]",
		this.resultCollector);
}
/**
 * Type reference test.
 */
public void testTypeReference31() throws CoreException { // was testAccurateTypeReference
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"d5.X", 
		TYPE, 
		REFERENCES,
		SearchEngine.createJavaSearchScope(new IJavaElement[] {
			getPackageFragment("JavaSearch", "src", "d5")
		}), 
		this.resultCollector);
	assertSearchResults(
		"src/d5/Y.java d5.Y.T [d5.X]\n" +
		"src/d5/Y.java d5.Y.c [d5.X]\n" +
		"src/d5/Y.java d5.Y.o [d5.X]",
		this.resultCollector);
}
/**
 * Type reference in hierarchy test.
 * (regression test for bug 28236 Search for refs to class in hierarchy matches class outside hierarchy )
 */
public void testTypeReference32() throws CoreException { // was testTypeReferenceInHierarchy
	IType type = getCompilationUnit("JavaSearch", "src", "d9.p1", "A.java").getType("A");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	IJavaSearchScope scope = SearchEngine.createHierarchyScope(type);
	search(
		type, 
		REFERENCES, 
		scope, 
		this.resultCollector);
	assertSearchResults(
		"",
		this.resultCollector);
}
/**
 * Type reference with recovery test.
 * (Regression test for bug 29366 Search reporting invalid inaccurate match )
 */
public void testTypeReference33() throws CoreException { // was testTypeReferenceWithRecovery
	IType type = getCompilationUnit("JavaSearch", "src", "e1", "A29366.java").getType("A29366");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		this.resultCollector);
	assertSearchResults(
		"src/e1/A29366.java void e1.A29366.foo() [A29366] EXACT_MATCH",
		this.resultCollector);
}
/**
 * Type reference with problem test.
 * (Regression test for bug 36479 Rename operation during refactoring fails)
 */
public void testTypeReference34() throws CoreException { // was testTypeReferenceWithProblem
	IType type = getCompilationUnit("JavaSearch", "src", "e6", "A.java").getType("A");
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		"B36479", 
		TYPE,
		REFERENCES, 
		SearchEngine.createJavaSearchScope(new IJavaElement[] {type}), 
		this.resultCollector);
	assertSearchResults(
		"src/e6/A.java Object e6.A.foo() [B36479] POTENTIAL_MATCH",
		this.resultCollector);
}
/**
 * Type reference with corrupt jar on the classpath test.
 * (Regression test for bug 39831 Search finds only "inexact" matches)
 */
public void testTypeReference35() throws CoreException { // was testTypeReferenceWithCorruptJar
	IJavaProject project = getJavaProject("JavaSearch");
	IClasspathEntry[] originalCP = project.getRawClasspath();
	try {
		// add corrupt.jar to classpath
		int cpLength = originalCP.length;
		IClasspathEntry[] newCP = new IClasspathEntry[cpLength+1];
		System.arraycopy(originalCP, 0, newCP, 0, cpLength);
		newCP[cpLength] = JavaCore.newLibraryEntry(new Path("/JavaSearch/corrupt.jar"), null, null);
		project.setRawClasspath(newCP, null);
		
		IType type = getCompilationUnit("JavaSearch", "src", "e7", "A.java").getType("A");
	//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showAccuracy = true;
		search(
			type, 
			REFERENCES,
			SearchEngine.createJavaSearchScope(new IJavaElement[] {project}), 
			this.resultCollector);
		assertSearchResults(
			"src/e7/A.java e7.A.a [A] EXACT_MATCH",
			this.resultCollector);
	} finally {
		project.setRawClasspath(originalCP, null);
	}
}
/*
 * Local type reference test.
 */
public void testTypeReference36() throws CoreException { // was testLocalTypeReference1
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "f2");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showContext = true;
	search(
		"Y", 
		TYPE,
		REFERENCES,
		scope, 
		this.resultCollector);
	assertSearchResults(
		"src/f2/X.java Object f2.X.foo1() [		return new <Y>();]",
		this.resultCollector);
}
/*
 * Local type reference test.
 */
public void testTypeReference37() throws CoreException { // was testLocalTypeReference2
	IType type = getCompilationUnit("JavaSearch/src/f2/X.java").getType("X").getMethod("foo", new String[0]).getType("Y", 1);
	
	IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showContext = true;
	search(
		type, 
		REFERENCES,
		scope, 
		this.resultCollector);
	assertSearchResults(
		"src/f2/X.java Object f2.X.foo1() [		return new <Y>();]",
		this.resultCollector);
}
/**
 * Type reference inside/outside doc comment.
 */
public void testTypeReference38() throws CoreException { // was testTypeReferenceInOutDocComment
	IType type = getCompilationUnit("JavaSearch", "src", "s4", "X.java").getType("X");
	
//	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showInsideDoc = true;
	search(type, REFERENCES, getJavaSearchScope(), resultCollector);
	assertSearchResults(
		"src/s4/X.java void s4.X.bar() [X] INSIDE_JAVADOC\n" + 
		"src/s4/X.java void s4.X.bar() [X] INSIDE_JAVADOC\n" + 
		"src/s4/X.java void s4.X.bar() [X] INSIDE_JAVADOC\n" + 
		"src/s4/X.java void s4.X.fred() [X] OUTSIDE_JAVADOC",
		this.resultCollector);
}
/**
 * Type reference in static import
 */
public void testTypeReference39() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "s1.j.l", "S.java").getType("S");
	search(type, REFERENCES, getJavaSearchScope15(), resultCollector);
	assertSearchResults(
		"src/s1/A.java [s1.j.l.S]\n" + 
		"src/s1/A.java [s1.j.l.S]\n" + 
		"src/s1/B.java [s1.j.l.S]\n" + 
		"src/s1/D.java [s1.j.l.S]\n" + 
		"src/s1/D.java [s1.j.l.S]\n" + 
		"src/s1/E.java [s1.j.l.S]",
		this.resultCollector);
}
/**
 * Member type reference in static import
 */
public void testTypeReference40() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "s1.j.l", "S.java").getType("S");
	IType member = type.getType("Member");
	search(member, REFERENCES, getJavaSearchScope15(), resultCollector);
	assertSearchResults(
		"src/s1/E.java [s1.j.l.S.Member]\n" + 
		"src/s1/E.java s1.E.m [Member]",
		this.resultCollector);
}
/**
 * Search in enumeration
 */
public void testEnum01() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "e1", "T.java").getType("T");
	search(type, REFERENCES, getJavaSearchScope15(), resultCollector);
	assertSearchResults(
		"src/e1/X.java [e1.T]\n" + 
		"src/e1/X.java void e1.X.main(String[]) [T]\n" + 
		"src/e1/X.java void e1.X.main(String[]) [T]\n" + 
		"src/e1/X.java Location e1.X.location(T) [T]",
		this.resultCollector);
}
public void testEnum02() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "e1", "T.java").getType("T");
	IMethod method = type.getMethod("T", new String[0]);
	search(method, REFERENCES, getJavaSearchScope15(), resultCollector);
	assertSearchResults(
		"src/e1/T.java e1.T.FREDERIC [FREDERIC]",
		this.resultCollector);
}
public void testEnum03() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "e1", "T.java").getType("T");
	IMethod method = type.getMethod("T", new String[] { "I" });
	search(method, REFERENCES, getJavaSearchScope15(), resultCollector);
	assertSearchResults(
		"src/e1/T.java e1.T.PHILIPPE [PHILIPPE]\n" + 
		"src/e1/T.java e1.T.DAVID [DAVID]\n" + 
		"src/e1/T.java e1.T.JEROME [JEROME]\n" + 
		"src/e1/T.java e1.T.OLIVIER [OLIVIER]\n" + 
		"src/e1/T.java e1.T.KENT [KENT]",
		this.resultCollector);
}
public void testEnum04() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "e1", "T.java").getType("T");
	IMethod method = type.getMethod("age", new String[0]);
	search(method, REFERENCES, getJavaSearchScope15(), resultCollector);
	assertSearchResults(
		"src/e1/X.java void e1.X.main(String[]) [age()]",
		this.resultCollector);
}
public void testEnum05() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "e1", "T.java").getType("T");
	IMethod method = type.getMethod("isManager", new String[0]);
	search(method, ALL_OCCURRENCES, getJavaSearchScope15(), resultCollector);
	assertSearchResults(
		"src/e1/T.java e1.T.PHILIPPE [isManager()]\n" + 
		"src/e1/T.java boolean e1.T.isManager() [isManager]\n" + 
		"src/e1/X.java void e1.X.main(String[]) [isManager()]",
		this.resultCollector);
}
public void testEnum06() throws CoreException {
	IType type = getCompilationUnit("JavaSearch15", "src", "e1", "T.java").getType("T");
	IMethod method = type.getMethod("setRole", new String[] { "Z" });
	search(method, REFERENCES, getJavaSearchScope15(), resultCollector);
	assertSearchResults(
		"src/e1/X.java void e1.X.main(String[]) [setRole(t.isManager())]",
		this.resultCollector);
}
}
