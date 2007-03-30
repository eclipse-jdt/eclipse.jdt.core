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
import java.util.Hashtable;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelStatus;

/**
 * Tests the Java search engine where results are JavaElements and source positions.
 */
public class JavaSearchTests extends AbstractJavaModelTests implements IJavaSearchConstants {
	
	protected IJavaProject javaProject;

/**
 * Collects results as a string.
 */
public static class JavaSearchResultCollector extends SearchRequestor {
	public StringBuffer results = new StringBuffer();
	public boolean showAccuracy;
	public boolean showProject;
	public boolean showContext;
	public boolean showInsideDoc;
	public void acceptSearchMatch(SearchMatch match) throws CoreException {
		try {
			if (results.length() > 0) results.append("\n");
			IResource resource = match.getResource();
			IJavaElement element = (IJavaElement) match.getElement();
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
							results.append(pkgFragmentRoot.getPath().toOSString());
						} else {
							results.append(pkgFragmentRoot.getPath());
						}
					}
				} else {
					results.append(path);
				}
			} else {
				results.append(element.getPath());
			}
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
				char[] contents = null;
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
				int start = match.getOffset();
				int end = start + match.getLength();
				if (start == -1 || contents != null) { // retrieving attached source not implemented here
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
			results.append(((JavaElement)type).occurrenceCount);
		}
	}
	public String toString() {
		return results.toString();
	}
}
public JavaSearchTests(String name) {
	super(name);
}
IJavaSearchScope getJavaSearchScope() {
	return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearch")});
}
public void setUpSuite() throws Exception {
	super.setUpSuite();

	this.javaProject = setUpJavaProject("JavaSearch");
}
public void tearDownSuite() throws Exception {
	deleteProject("JavaSearch");

	super.tearDownSuite();
}
public static Test suite() {
	TestSuite suite = new Suite(JavaSearchTests.class.getName());
	
	if (false) {
		System.err.println("WARNING: Only subset of test cases are running for "+JavaSearchTests.class.getName());
		suite.addTest(new JavaSearchTests("testPatternMatchPackageReference2"));
		return suite;
	}
	
	// package declaration
	suite.addTest(new JavaSearchTests("testSimplePackageDeclaration"));
	suite.addTest(new JavaSearchTests("testVariousPackageDeclarations"));
	suite.addTest(new JavaSearchTests("testPackageDeclaration"));
	suite.addTest(new JavaSearchTests("testPackageDeclarationBug73551"));
	
	// package reference
	suite.addTest(new JavaSearchTests("testSimplePackageReference"));
	suite.addTest(new JavaSearchTests("testPackageReference1"));
	suite.addTest(new JavaSearchTests("testPackageReference2"));
	suite.addTest(new JavaSearchTests("testPackageReference3"));
	suite.addTest(new JavaSearchTests("testVariousPackageReference"));
	suite.addTest(new JavaSearchTests("testAccuratePackageReference"));
	suite.addTest(new JavaSearchTests("testPatternMatchPackageReference"));
	suite.addTest(new JavaSearchTests("testPatternMatchPackageReference2"));

	// type declaration
	suite.addTest(new JavaSearchTests("testSimpleTypeDeclaration"));
	suite.addTest(new JavaSearchTests("testTypeDeclaration"));
	suite.addTest(new JavaSearchTests("testTypeDeclarationInJar"));
	suite.addTest(new JavaSearchTests("testTypeDeclarationInJar2"));
	suite.addTest(new JavaSearchTests("testTypeDeclarationInPackageScope"));
	suite.addTest(new JavaSearchTests("testTypeDeclarationInPackageScope2"));
	suite.addTest(new JavaSearchTests("testMemberTypeDeclaration"));
	suite.addTest(new JavaSearchTests("testPatternMatchTypeDeclaration"));
	suite.addTest(new JavaSearchTests("testLongDeclaration"));
	suite.addTest(new JavaSearchTests("testLocalTypeDeclaration1"));
	suite.addTest(new JavaSearchTests("testLocalTypeDeclaration2"));
	
	// type reference
	suite.addTest(new JavaSearchTests("testSimpleTypeReference"));
	suite.addTest(new JavaSearchTests("testTypeReference1"));
	suite.addTest(new JavaSearchTests("testTypeReference2"));
	suite.addTest(new JavaSearchTests("testTypeReference3"));
	suite.addTest(new JavaSearchTests("testTypeReference4"));
	suite.addTest(new JavaSearchTests("testTypeReference5"));
	suite.addTest(new JavaSearchTests("testTypeReferenceInInitializer"));
	suite.addTest(new JavaSearchTests("testTypeReferenceAsSingleNameReference"));
	suite.addTest(new JavaSearchTests("testMemberTypeReference"));
	suite.addTest(new JavaSearchTests("testMemberTypeReference2"));
	suite.addTest(new JavaSearchTests("testObjectMemberTypeReference"));
	suite.addTest(new JavaSearchTests("testTypeReferenceInQualifiedNameReference"));
	suite.addTest(new JavaSearchTests("testTypeReferenceInQualifiedNameReference2"));
	suite.addTest(new JavaSearchTests("testTypeReferenceInQualifiedNameReference3"));
	suite.addTest(new JavaSearchTests("testTypeReferenceInQualifiedNameReference4"));
	suite.addTest(new JavaSearchTests("testTypeReferenceNotInClasspath"));
	suite.addTest(new JavaSearchTests("testVariousTypeReferences"));
	suite.addTest(new JavaSearchTests("testTypeReferenceInImport"));
	suite.addTest(new JavaSearchTests("testTypeReferenceInImport2"));
	suite.addTest(new JavaSearchTests("testTypeReferenceInArray"));
	suite.addTest(new JavaSearchTests("testTypeReferenceInArray2"));
	suite.addTest(new JavaSearchTests("testNegativeTypeReference"));
	suite.addTest(new JavaSearchTests("testTypeReferenceInThrows"));
	suite.addTest(new JavaSearchTests("testInnacurateTypeReference1"));
	suite.addTest(new JavaSearchTests("testInnacurateTypeReference2"));
	suite.addTest(new JavaSearchTests("testInnacurateTypeReference3"));
	suite.addTest(new JavaSearchTests("testTypeReferenceInCast"));
	suite.addTest(new JavaSearchTests("testPatternMatchTypeReference"));
	suite.addTest(new JavaSearchTests("testTypeReferenceNotCaseSensitive"));
	suite.addTest(new JavaSearchTests("testAccurateTypeReference"));
	suite.addTest(new JavaSearchTests("testTypeReferenceInHierarchy"));
	suite.addTest(new JavaSearchTests("testTypeReferenceWithRecovery"));
	suite.addTest(new JavaSearchTests("testTypeReferenceWithProblem"));
	suite.addTest(new JavaSearchTests("testTypeReferenceWithCorruptJar"));
	suite.addTest(new JavaSearchTests("testLocalTypeReference1"));
	suite.addTest(new JavaSearchTests("testLocalTypeReference2"));
	suite.addTest(new JavaSearchTests("testTypeReferenceInOutDocComment"));
	
	// type occurences
	suite.addTest(new JavaSearchTests("testTypeOccurence"));
	suite.addTest(new JavaSearchTests("testTypeOccurence2"));
	suite.addTest(new JavaSearchTests("testTypeOccurence3"));
	suite.addTest(new JavaSearchTests("testTypeOccurenceWithDollar"));
	
	// interface implementor
	suite.addTest(new JavaSearchTests("testInterfaceImplementors"));
	suite.addTest(new JavaSearchTests("testInterfaceImplementors2"));
	
	// method declaration
	suite.addTest(new JavaSearchTests("testSimpleMethodDeclaration"));
	suite.addTest(new JavaSearchTests("testSimpleConstructorDeclaration"));
	suite.addTest(new JavaSearchTests("testMethodDeclaration"));
	suite.addTest(new JavaSearchTests("testInnerMethodDeclaration"));
	suite.addTest(new JavaSearchTests("testMethodDeclarationInHierarchyScope1"));
	suite.addTest(new JavaSearchTests("testMethodDeclarationInHierarchyScope2"));
	suite.addTest(new JavaSearchTests("testMethodDeclarationInHierarchyScope3"));
	suite.addTest(new JavaSearchTests("testMethodDeclarationInPackageScope"));
	suite.addTest(new JavaSearchTests("testMethodDeclarationInJar"));
	suite.addTest(new JavaSearchTests("testConstructorDeclarationInJar"));
	suite.addTest(new JavaSearchTests("testMethodDeclarationInInitializer"));
	suite.addTest(new JavaSearchTests("testMethodDeclarationNoReturnType"));
	
	// method reference
	suite.addTest(new JavaSearchTests("testSimpleMethodReference"));
	suite.addTest(new JavaSearchTests("testStaticMethodReference1"));
	suite.addTest(new JavaSearchTests("testStaticMethodReference2"));
	suite.addTest(new JavaSearchTests("testInnerMethodReference"));
	suite.addTest(new JavaSearchTests("testMethodReferenceThroughSuper"));
	suite.addTest(new JavaSearchTests("testMethodReferenceInInnerClass"));
	suite.addTest(new JavaSearchTests("testMethodReferenceInAnonymousClass"));
	suite.addTest(new JavaSearchTests("testMethodReferenceThroughArray"));
	suite.addTest(new JavaSearchTests("testMethodReference1"));
	suite.addTest(new JavaSearchTests("testMethodReference2"));
	suite.addTest(new JavaSearchTests("testMethodReference3"));
	suite.addTest(new JavaSearchTests("testMethodReference4"));
	suite.addTest(new JavaSearchTests("testMethodReference5"));
	suite.addTest(new JavaSearchTests("testMethodReference6"));
	suite.addTest(new JavaSearchTests("testMethodReferenceInOutDocComment"));
	
	// constructor reference
	suite.addTest(new JavaSearchTests("testSimpleConstructorReference1"));
	suite.addTest(new JavaSearchTests("testSimpleConstructorReference2"));
	suite.addTest(new JavaSearchTests("testConstructorReferenceExplicitConstructorCall1"));
	suite.addTest(new JavaSearchTests("testConstructorReferenceExplicitConstructorCall2"));
	suite.addTest(new JavaSearchTests("testConstructorReferenceImplicitConstructorCall1"));
	suite.addTest(new JavaSearchTests("testConstructorReferenceImplicitConstructorCall2"));
	suite.addTest(new JavaSearchTests("testConstructorReferenceInFieldInitializer"));
	suite.addTest(new JavaSearchTests("testConstructorReferenceDefaultConstructorOfMemberClass"));
	
	// field declaration
	suite.addTest(new JavaSearchTests("testSimpleFieldDeclaration"));
	suite.addTest(new JavaSearchTests("testFieldDeclarationInJar"));
	suite.addTest(new JavaSearchTests("testFieldDeclarationArrayType"));
	suite.addTest(new JavaSearchTests("testFieldDeclarationWithWildCard"));

	// field reference
	suite.addTest(new JavaSearchTests("testSimpleFieldReference"));
	suite.addTest(new JavaSearchTests("testSimpleReadFieldReference"));
	suite.addTest(new JavaSearchTests("testSimpleWriteFieldReference"));
	suite.addTest(new JavaSearchTests("testMultipleFieldReference"));
	suite.addTest(new JavaSearchTests("testStaticFieldReference"));
	suite.addTest(new JavaSearchTests("testFieldReference"));
	suite.addTest(new JavaSearchTests("testFieldReference2"));
	suite.addTest(new JavaSearchTests("testFieldReference3"));
	suite.addTest(new JavaSearchTests("testFieldReference4"));
	suite.addTest(new JavaSearchTests("testFieldReference5"));
	suite.addTest(new JavaSearchTests("testFieldReference6"));
	suite.addTest(new JavaSearchTests("testFieldReference7"));
	suite.addTest(new JavaSearchTests("testFieldReferenceBug73112a"));
	suite.addTest(new JavaSearchTests("testFieldReferenceBug73112b"));
	suite.addTest(new JavaSearchTests("testFieldReferenceInInnerClass"));
	suite.addTest(new JavaSearchTests("testFieldReferenceInAnonymousClass"));
	suite.addTest(new JavaSearchTests("testFieldReferenceThroughSubclass"));
	suite.addTest(new JavaSearchTests("testReadWriteFieldReferenceInCompoundExpression"));
	suite.addTest(new JavaSearchTests("testReadWriteAccessInQualifiedNameReference"));
	suite.addTest(new JavaSearchTests("testFieldReferenceInBrackets"));
	suite.addTest(new JavaSearchTests("testAccurateFieldReference1"));
	suite.addTest(new JavaSearchTests("testFieldReferenceInOutDocComment"));
	
	// local variable declaration
	suite.addTest(new JavaSearchTests("testLocalVariableDeclaration1"));
	suite.addTest(new JavaSearchTests("testLocalVariableDeclaration2"));
	
	// local variable reference
	suite.addTest(new JavaSearchTests("testLocalVariableReference1"));
	suite.addTest(new JavaSearchTests("testLocalVariableReference2"));
	suite.addTest(new JavaSearchTests("testLocalVariableReference3"));
	
	// local variable occurrences
	suite.addTest(new JavaSearchTests("testLocalVariableOccurrences1"));
	suite.addTest(new JavaSearchTests("testLocalVariableOccurrences2"));
	
	// or pattern
	suite.addTest(new JavaSearchTests("testOrPattern"));
	
	// declarations of accessed fields
	suite.addTest(new JavaSearchTests("testDeclarationOfAccessedFields1"));
	suite.addTest(new JavaSearchTests("testDeclarationOfAccessedFields2"));
	suite.addTest(new JavaSearchTests("testDeclarationOfAccessedFields3"));

	// declarations of referenced types
	suite.addTest(new JavaSearchTests("testDeclarationOfReferencedTypes1"));
	suite.addTest(new JavaSearchTests("testDeclarationOfReferencedTypes2"));
	suite.addTest(new JavaSearchTests("testDeclarationOfReferencedTypes3"));
	suite.addTest(new JavaSearchTests("testDeclarationOfReferencedTypes4"));
	suite.addTest(new JavaSearchTests("testDeclarationOfReferencedTypes5"));
	suite.addTest(new JavaSearchTests("testDeclarationOfReferencedTypes6"));
	suite.addTest(new JavaSearchTests("testDeclarationOfReferencedTypes7"));
	suite.addTest(new JavaSearchTests("testDeclarationOfReferencedTypes8"));
	
	// declarations of sent messages
	suite.addTest(new JavaSearchTests("testSimpleDeclarationsOfSentMessages"));
	suite.addTest(new JavaSearchTests("testDeclarationOfSentMessages"));
	
	// potential match in binary
	suite.addTest(new JavaSearchTests("testPotentialMatchInBinary1"));
	suite.addTest(new JavaSearchTests("testPotentialMatchInBinary2"));
	suite.addTest(new JavaSearchTests("testPotentialMatchInBinary3"));

	// core exception
	suite.addTest(new JavaSearchTests("testCoreException"));
	
	// search scopes
	suite.addTest(new JavaSearchTests("testHierarchyScope"));
	suite.addTest(new JavaSearchTests("testSubCUSearchScope1"));
	suite.addTest(new JavaSearchTests("testSubCUSearchScope2"));
	suite.addTest(new JavaSearchTests("testSubCUSearchScope3"));
	suite.addTest(new JavaSearchTests("testExternalJarScope"));
	
	return suite;
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

/**
 * Type reference test.
 */
public void testAccurateFieldReference1() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"d6.X.CONSTANT", 
		FIELD, 
		REFERENCES,
		SearchEngine.createJavaSearchScope(new IJavaElement[] {
			getPackageFragment("JavaSearch", "src", "d6")
		}), 
		resultCollector);
	assertSearchResults(
		"src/d6/Y.java d6.Y.T [CONSTANT]",
		resultCollector);
}
/**
 * Regression test for 1GBK7B2: ITPJCORE:WINNT - package references: could be more precise
 */
public void testAccuratePackageReference() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "p3.p2");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		pkg, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/PackageReference/K.java [p3.p2]", 
		resultCollector);
}
/**
 * Type reference test.
 */
public void testAccurateTypeReference() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"d5.X", 
		TYPE, 
		REFERENCES,
		SearchEngine.createJavaSearchScope(new IJavaElement[] {
			getPackageFragment("JavaSearch", "src", "d5")
		}), 
		resultCollector);
	assertSearchResults(
		"src/d5/Y.java d5.Y.T [d5.X]\n" +
		"src/d5/Y.java d5.Y.c [d5.X]\n" +
		"src/d5/Y.java d5.Y.o [d5.X]",
		resultCollector);
}
/**
 * Constructor declaration in jar file test.
 */
public void testConstructorDeclarationInJar() throws CoreException {
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p1", "A.class").getType();
	IMethod method = type.getMethod("A", new String[] {"Ljava.lang.String;"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"MyJar.jar p1.A(java.lang.String) [No source]", 
		resultCollector);
}
/**
 * Constructor reference in case of default constructor of member type
 * (regression test for bug 43276)
 */
public void testConstructorReferenceDefaultConstructorOfMemberClass() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"c10.X.Inner()", 
		CONSTRUCTOR, 
		REFERENCES,
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/c10/X.java c10.B() [new X().super()]", 
		resultCollector);
}/**
 * Constructor reference using an explicit constructor call.
 */
public void testConstructorReferenceExplicitConstructorCall1() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "Y.java").getType("Y");
	IMethod method = type.getMethod("Y", new String[] {"I"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/p/Z.java p.Z(int) [super(i)]", 
		resultCollector);
}
/**
 * Constructor reference using an explicit constructor call.
 */
public void testConstructorReferenceExplicitConstructorCall2() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("X", new String[] {"I"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/p/Y.java p.Y(int) [super(i)]\n" +
		"src/p/Y.java p.Y(boolean) [super(1)]", 
		resultCollector);
}
/**
 * Constructor reference using an implicit constructor call.
 * (regression test for bug 23112 search: need a way to search for references to the implicit non-arg constructor)
 */
public void testConstructorReferenceImplicitConstructorCall1() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "c7", "X.java").getType("X");
	IMethod method = type.getMethod("X", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/c7/Y.java c7.Y() [Y]", 
		resultCollector);
}
/**
 * Constructor reference using an implicit constructor call.
 * (regression test for bug 23112 search: need a way to search for references to the implicit non-arg constructor)
 */
public void testConstructorReferenceImplicitConstructorCall2() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"c8.X()", 
		CONSTRUCTOR, 
		REFERENCES,
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/c8/Y.java c8.Y [Y]", 
		resultCollector);
}
/**
 * Constructor reference in a field initializer.
 * (regression test for PR 1GKZ8VZ: ITPJCORE:WINNT - Search - did not find references to member constructor)
 */
public void testConstructorReferenceInFieldInitializer() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "A.java").getType("A").getType("Inner");
	IMethod method = type.getMethod("Inner", new String[] {"I"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/A.java A.field [new Inner(1)]\n" +
		"src/A.java A.field [new Inner(2)]", 
		resultCollector);
}
/**
 * CoreException thrown during accept.
 * (regression test for PR #1G3UI7A)
 */
public void testCoreException() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	SearchRequestor resultCollector = new SearchRequestor() {
		public void acceptSearchMatch(SearchMatch match) throws CoreException {
			throw new CoreException(new JavaModelStatus(-1, "test"));
		}
	};
	try {
		search(
			type, 
			DECLARATIONS, 
			getJavaSearchScope(), 
			resultCollector);
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
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfAccessedFields(
		method, 
		resultCollector
	);
	assertSearchResults(
		"", 
		resultCollector);
}
/**
 * Declaration of accessed fields test.
 * (regression test for bug 6538 searchDeclarationsOf* incorrect)
 */
public void testDeclarationOfAccessedFields2() throws CoreException {
	IMethod method = 
		getCompilationUnit("JavaSearch", "src", "a6", "A.java").
			getType("B").getMethod("m", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfAccessedFields(
		method, 
		resultCollector
	);
	assertSearchResults(
		"src/a6/A.java a6.B.f [f]", 
		resultCollector);
}
/**
 * Declaration of accessed fields test.
 * (regression test for bug 10386 NPE in MatchLocator.lookupType)
 */
public void testDeclarationOfAccessedFields3() throws CoreException {
	IMethod method = 
		getCompilationUnit("JavaSearch", "src", "b6", "A.java").
			getType("A").getMethod("foo", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfAccessedFields(
		method, 
		resultCollector
	);
	assertSearchResults(
		"src/b6/A.java b6.A.field [field]", 
		resultCollector);
}
/**
 * Declaration of referenced types test.
 */
public void testDeclarationOfReferencedTypes1() throws CoreException {
	IMethod method = 
		getCompilationUnit("JavaSearch", "src", "a3", "References.java").
			getType("References").getMethod("foo", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
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
		resultCollector);
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 6779 searchDeclarationsOfReferencedTyped - missing exception types)
 */
public void testDeclarationOfReferencedTypes2() throws CoreException {
	IMethod method = 
		getCompilationUnit("JavaSearch", "src", "a7", "X.java").
			getType("X").getMethod("foo", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfReferencedTypes(
		method, 
		resultCollector
	);
	assertSearchResults(
		"src/a7/X.java a7.MyException [MyException]", 
		resultCollector);
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 12649 Missing import after move  )
 */
public void testDeclarationOfReferencedTypes3() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch", "src", "c1", "A.java");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfReferencedTypes(
		unit, 
		resultCollector
	);
	assertSearchResults(
		"src/c1/I.java c1.I [I]", 
		resultCollector);
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 12649 Missing import after move  )
 */
public void testDeclarationOfReferencedTypes4() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch", "src", "c1", "B.java");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfReferencedTypes(
		unit, 
		resultCollector
	);
	assertSearchResults(
		"src/c1/I.java c1.I [I]", 
		resultCollector);
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 18418 search: searchDeclarationsOfReferencedTypes reports import declarations)
 */
public void testDeclarationOfReferencedTypes5() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch", "src", "c2", "A.java");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfReferencedTypes(
		unit, 
		resultCollector
	);
	assertSearchResults(
		"src/c3/C.java c3.C [C]", 
		resultCollector);
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 24934 Move top level doesn't optimize the imports[refactoring])
 */
public void testDeclarationOfReferencedTypes6() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch", "src", "d1", "X.java");
	IType innerType = unit.getType("X").getType("Inner");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfReferencedTypes(
		innerType, 
		resultCollector
	);
	assertSearchResults(
		"src/d2/Y.java d2.Y [Y]", 
		resultCollector);
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 37438 searchenging NPE in searchDeclarationsOfReferencedTypes 
)
 */
public void testDeclarationOfReferencedTypes7() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "r7");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfReferencedTypes(
		pkg, 
		resultCollector
	);
	assertSearchResults(
		"", 
		resultCollector);
}

/**
 * Declaration of referenced types test.
 * (Regression test for bug 47787 IJavaSearchResultCollector.aboutToStart() and done() not called)
 */
public void testDeclarationOfReferencedTypes8() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "r7");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector() {
	    public void beginReporting() {
	        results.append("Starting search...\n");
        }
	    public void endReporting() {
	        results.append("Done searching.");
        }
	};
	searchDeclarationsOfReferencedTypes(
		pkg, 
		resultCollector
	);
	assertSearchResults(
		"Starting search...\n"+
		"Done searching.", 
		resultCollector);
}

/**
 * Declaration of sent messages test.
 * (regression test for bug 6538 searchDeclarationsOf* incorrect)
 */
public void testDeclarationOfSentMessages() throws CoreException {
	IMethod method = 
		getCompilationUnit("JavaSearch", "src", "a5", "B.java").
			getType("C").getMethod("i", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfSentMessages(
		method, 
		resultCollector
	);
	assertSearchResults(
		"", 
		resultCollector);
}
/**
 * Java search scope on java element in external jar test.
 */
public void testExternalJarScope() throws CoreException, IOException {
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
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
			"X", 
			TYPE,
			DECLARATIONS,
			scope,
			resultCollector);
		assertSearchResults(
			externalJar.getCanonicalPath()+ " p0.X",
			resultCollector);
			
		IClassFile classFile = pkg.getClassFile("X.class");
		scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {classFile});
		resultCollector = new JavaSearchResultCollector();
		search(
			classFile.getType(), 
			DECLARATIONS,
			scope,
			resultCollector);
		assertSearchResults(
			externalJar.getCanonicalPath()+ " p0.X",
			resultCollector);
		
	} finally {
		Util.delete(externalJar);
		project.setRawClasspath(classpath, null);
	}
	
}


/**
 * Field declaration with array type test.
 * (regression test for PR 1GKEG73: ITPJCORE:WIN2000 - search (136): missing field declaration)
 */
public void testFieldDeclarationArrayType() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "B.java").getType("B");
	IField field = type.getField("open");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/B.java B.open [open]", 
		resultCollector);
}
/**
 * Field declaration in jar file test.
 */
public void testFieldDeclarationInJar() throws CoreException {
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p1", "A.class").getType();
	IField field = type.getField("field");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"MyJar.jar p1.A.field [No source]", 
		resultCollector);
}
/**
 * Field declaration with wild card test.
 * (regression test for bug 21763 Problem in Java search [search]  )
 */
public void testFieldDeclarationWithWildCard() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"class*path", 
		FIELD,
		DECLARATIONS,
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/c5/Test.java c5.Test.class_path [class_path]", 
		resultCollector);
}
/**
 * Multiple field references in one ast test.
 * (regression test for PR 1GD79XM: ITPJCORE:WINNT - Search - search for field references - not all found)
 */
public void testMultipleFieldReference() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p5", "A.java").getType("A");
	IField field = type.getField("x");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/p5/A.java void p5.A.k() [x]\n" + 
		"src/p5/A.java void p5.A.k() [x]\n" + 
		"src/p5/A.java void p5.A.k() [x]",
		resultCollector);
}
/**
 * Field reference in inner class test.
 * (regression test for PR 1GL11J6: ITPJCORE:WIN2000 - search: missing field references (nested types))
 */
public void testFieldReferenceInInnerClass() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "O.java").getType("O");
	IField field = type.getField("y");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/O.java void O$I.y() [y]",
		resultCollector);
}
/**
 * Field reference test.
 * (regression test for bug #3433 search: missing field occurrecnces (1GKZ8J6))
 */
public void testFieldReference() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p8", "A.java").getType("A");
	IField field = type.getField("g");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/p8/A.java void p8.A.m() [g]\n" + 
		"src/p8/A.java void p8.B.m() [g]",
		resultCollector);
}
/**
 * Field reference test.
 * (regression test for PR 1GK8TXE: ITPJCORE:WIN2000 - search: missing field reference)
 */
public void testFieldReference2() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p9", "X.java").getType("X");
	IField field = type.getField("f");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/p9/X.java void p9.X.m() [f]",
		resultCollector);
}
/**
 * Field reference test.
 * (regression test for bug 5821 Refactor > Rename renames local variable instead of member in case of name clash  )
 */
public void testFieldReference3() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "q8", "EclipseTest.java").getType("EclipseTest");
	IField field = type.getField("test");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/q8/EclipseTest.java void q8.EclipseTest.main(String[]) [test]",
		resultCollector);
}
/**
 * Field reference test.
 * (regression test for bug 5923 Search for "length" field refs finds [].length)
 */
public void testFieldReference4() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "a2", "X.java").getType("X");
	IField field = type.getField("length");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/a2/X.java void a2.X.foo() [length]",
		resultCollector);
}
/**
 * Field reference test.
 * (regression test for bug 7987 Field reference search should do lookup in 1.4 mode)
 */
public void testFieldReference5() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "b1", "A.java").getType("A");
	IField field = type.getField("x");
	
	// Set 1.4 compliance level (no constant yet)
	Hashtable options = JavaCore.getOptions();
	String currentOption = (String)options.get("org.eclipse.jdt.core.compiler.compliance");
	options.put("org.eclipse.jdt.core.compiler.compliance", "1.4");
	JavaCore.setOptions(options);
	
	try {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		search(
			field, 
			REFERENCES, 
			getJavaSearchScope(), 
			resultCollector);
		assertSearchResults(
			"src/b1/B.java void b1.B.foo() [x]",
			resultCollector);
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
public void testFieldReference6() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "c4", "X.java").getType("X");
	IField field = type.getField("x");
	
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/c4/X.java int c4.X.foo() [x]",
		resultCollector);
}
/**
 * Field reference test.
 * (regression test for bug 61017 Refactoring - test case that results in uncompilable source)
 */
public void testFieldReference7() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "s5", "A.java").getType("A");
	IField field = type.getField("b");
	
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/s5/A.java void s5.A.method() [b]",
		resultCollector);
}
/**
 * Field reference test.
 * (regression test for bug 73112: [Search] SearchEngine doesn't find all fields multiple field declarations
 */
public void testFieldReferenceBug73112a() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"fieldA73112*",
		FIELD,
		ALL_OCCURRENCES,
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/bug73112/A.java bug73112.A.fieldA73112a [fieldA73112a]\n" + 
		"src/bug73112/A.java bug73112.A.fieldA73112b [fieldA73112b]\n" + 
		"src/bug73112/A.java bug73112.A.fieldA73112c [fieldA73112c]\n" + 
		"src/bug73112/A.java bug73112.A.fieldA73112c [fieldA73112a]\n" + 
		"src/bug73112/A.java bug73112.A.fieldA73112c [fieldA73112b]\n" + 
		"src/bug73112/A.java bug73112.A.fieldA73112d [fieldA73112d]",
		resultCollector);
}
public void testFieldReferenceBug73112b() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"fieldB73112*",
		FIELD,
		ALL_OCCURRENCES,
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/bug73112/B.java bug73112.B.fieldB73112a [fieldB73112a]\n" + 
		"src/bug73112/B.java bug73112.B.fieldB73112b [fieldB73112b]\n" + 
		"src/bug73112/B.java bug73112.B.fieldB73112c [fieldB73112c]\n" + 
		"src/bug73112/B.java bug73112.B.fieldB73112c [fieldB73112a]\n" + 
		"src/bug73112/B.java bug73112.B.fieldB73112c [fieldB73112b]\n" + 
		"src/bug73112/B.java bug73112.B.fieldB73112d [fieldB73112d]\n" + 
		"src/bug73112/B.java bug73112.B.fieldB73112d [fieldB73112c]\n" + 
		"src/bug73112/B.java bug73112.B.fieldB73112d [fieldB73112a]\n" + 
		"src/bug73112/B.java bug73112.B.fieldB73112e [fieldB73112e]",
		resultCollector);
}
/**
 * Field reference in anonymous class test.
 * (regression test for PR 1GL12XE: ITPJCORE:WIN2000 - search: missing field references in inner class)
 */
public void testFieldReferenceInAnonymousClass() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "D.java").getType("D");
	IField field = type.getField("h");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/D.java void void D.g():<anonymous>#1.run() [h]",
		resultCollector);
}
/**
 * Field reference in brackets test.
 * (regression test for bug 23329 search: incorrect range for type references in brackets)
 */
public void testFieldReferenceInBrackets() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "s3", "A.java").getType("A");
	IField field = type.getField("field");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/s3/A.java int s3.A.bar() [field]",
		resultCollector);
}
/**
 * Field reference inside/outside doc comment.
 */
public void testFieldReferenceInOutDocComment() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "s4", "X.java").getType("X");
	IField field = type.getField("x");
	
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showInsideDoc = true;
	search(field, REFERENCES, getJavaSearchScope(), resultCollector);
	assertSearchResults(
		"src/s4/X.java int s4.X.foo() [x] OUTSIDE_JAVADOC\n" + 
		"src/s4/X.java void s4.X.bar() [x] INSIDE_JAVADOC",
		resultCollector);
}
/**
 * Field reference through subclass test.
 * (regression test for PR 1GKB9YH: ITPJCORE:WIN2000 - search for field refs - incorrect results)
 */
public void testFieldReferenceThroughSubclass() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p6", "A.java").getType("A");
	IField field = type.getField("f");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/p6/A.java void p6.A.m() [f]\n" + 
		"src/p6/A.java void p6.B.m() [f]\n" + 
		"src/p6/A.java void p6.B.m() [f]",
		resultCollector);
		
	type = getCompilationUnit("JavaSearch", "src", "p6", "A.java").getType("AA");
	field = type.getField("f");
	resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/p6/A.java void p6.B.m() [f]",
		resultCollector);
		
}
/**
 * Hierarchy scope test.
 * (regression test for bug 3445 search: type hierarchy scope incorrect (1GLC8VS))
 */
public void testHierarchyScope() throws CoreException {
	ICompilationUnit cu = this. getCompilationUnit("JavaSearch", "src", "a9", "A.java");
	IType type = cu.getType("C");
	IJavaSearchScope scope = SearchEngine.createHierarchyScope(type);
	assertTrue("a9.C should be included in hierarchy scope", scope.encloses(type));
	assertTrue("a9.A should be included in hierarchy scope", scope.encloses(cu.getType("A")));
	assertTrue("a9.B should be included in hierarchy scope", scope.encloses(cu.getType("B")));
	assertTrue("a9/A.java should be included in hierarchy scope", scope.encloses(cu.getUnderlyingResource().getFullPath().toString()));
}
/**
 * Type reference test.
 * (Regression test for bug 9642 Search - missing inaccurate type matches)
 */
public void testInnacurateTypeReference1() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"Zork", 
		TYPE, 
		REFERENCES,
		SearchEngine.createJavaSearchScope(new IJavaElement[] {
			getPackageFragment("JavaSearch", "src", "b5")
		}), 
		resultCollector);
	assertSearchResults(
		"src/b5/A.java [Zork]\n" +
		"src/b5/A.java b5.A.{} [Zork]\n" +
		"src/b5/A.java b5.A.{} [Zork]\n" +
		"src/b5/A.java b5.A.{} [Zork]",
		resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 9642 Search - missing inaccurate type matches)
 */
public void testInnacurateTypeReference2() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"p.Zork", 
		TYPE, 
		REFERENCES,
		SearchEngine.createJavaSearchScope(new IJavaElement[] {
			getPackageFragment("JavaSearch", "src", "b5")
		}), 
		resultCollector);
	assertSearchResults(
		"src/b5/A.java b5.A.{} [Zork]\n" +
		"src/b5/A.java b5.A.{} [Zork]\n" +
		"src/b5/A.java b5.A.{} [Zork]",
		resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 21485  NPE when doing a reference search to a package)
 */
public void testInnacurateTypeReference3() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "r3", "A21485.java").getType("A21485");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/r4/B21485.java [r3.A21485] EXACT_MATCH\n" +
		"src/r4/B21485.java r4.B21485 [A21485] POTENTIAL_MATCH",
		resultCollector);
}


/**
 * Inner method declaration test.
 */
public void testInnerMethodDeclaration() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X").getType("Inner");
	IMethod method = type.getMethod("foo", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/p/X.java String p.X$Inner.foo() [foo]",
		resultCollector);
}
/**
 * Inner method reference test.
 */
public void testInnerMethodReference() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X").getType("Inner");
	IMethod method = type.getMethod("foo", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/p/A.java void p.A.foo(int, String, X) [foo()]",
		resultCollector);
}
/**
 * Interface implementors test.
 */
public void testInterfaceImplementors() throws CoreException {
	// implementors of an interface
	IType type = getCompilationUnit("JavaSearch", "src", "p", "I.java").getType("I");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		IMPLEMENTORS, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/InterfaceImplementors.java InterfaceImplementors [p.I]\n" +
		"src/p/X.java p.X [I]", 
		resultCollector);

	// implementors of a class should give no match
	// (regression test for 1G5HBQA: ITPJUI:WINNT - Search - search for implementors of a class finds subclasses)
	type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		IMPLEMENTORS, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"", 
		resultCollector);	
}
/**
 * Interface implementors test.
 * (regression test for bug 22102 Not all implementors found for IPartListener)
 */
public void testInterfaceImplementors2() throws CoreException {
	// implementors of an interface
	IType type = getCompilationUnit("JavaSearch", "src", "r2", "I.java").getType("I");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		IMPLEMENTORS, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/r2/X.java r2.X.field:<anonymous>#1 [I]",
		resultCollector);
}
/*
 * Local type declaration test.
 */
public void testLocalTypeDeclaration1() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "f2");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"Y", 
		TYPE,
		DECLARATIONS,
		scope, 
		resultCollector);
	assertSearchResults(
		"src/f2/X.java Object f2.X.foo1():Y#1 [Y]",
		resultCollector);
}
/*
 * Local type declaration test.
 */
public void testLocalTypeDeclaration2() throws CoreException {
	IType type = getCompilationUnit("JavaSearch/src/f2/X.java").getType("X").getMethod("foo", new String[0]).getType("Y", 1);
	
	IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		DECLARATIONS,
		scope, 
		resultCollector);
	assertSearchResults(
		"src/f2/X.java Object f2.X.foo1():Y#1 [Y]",
		resultCollector);
}
/*
 * Local type reference test.
 */
public void testLocalTypeReference1() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "f2");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showContext = true;
	search(
		"Y", 
		TYPE,
		REFERENCES,
		scope, 
		resultCollector);
	assertSearchResults(
		"src/f2/X.java Object f2.X.foo1() [		return new <Y>();]",
		resultCollector);
}
/*
 * Local type reference test.
 */
public void testLocalTypeReference2() throws CoreException {
	IType type = getCompilationUnit("JavaSearch/src/f2/X.java").getType("X").getMethod("foo", new String[0]).getType("Y", 1);
	
	IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showContext = true;
	search(
		type, 
		REFERENCES,
		scope, 
		resultCollector);
	assertSearchResults(
		"src/f2/X.java Object f2.X.foo1() [		return new <Y>();]",
		resultCollector);
}
/*
 * Local variable declaration test.
 * (SingleNameReference)
 */
public void testLocalVariableDeclaration1() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var1 = 1;", "var1");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		localVar, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/f1/X.java void f1.X.foo1().var1 [var1]",
		resultCollector);
}
/*
 * Local variable declaration test.
 * (QualifiedNameReference)
 */
public void testLocalVariableDeclaration2() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var2 = new X();", "var2");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		localVar, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/f1/X.java void f1.X.foo2().var2 [var2]",
		resultCollector);
}
/*
 * Local variable occurrences test.
 * (SingleNameReference)
 */
public void testLocalVariableOccurrences1() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var1 = 1;", "var1");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		localVar, 
		ALL_OCCURRENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/f1/X.java void f1.X.foo1().var1 [var1]\n" + 
		"src/f1/X.java void f1.X.foo1() [var1]",
		resultCollector);
}
/*
 * Local variable occurences test.
 * (QualifiedNameReference)
 */
public void testLocalVariableOccurrences2() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var2 = new X();", "var2");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		localVar, 
		ALL_OCCURRENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/f1/X.java void f1.X.foo2().var2 [var2]\n" + 
		"src/f1/X.java void f1.X.foo2() [var2]",
		resultCollector);
}
/*
 * Local variable reference test.
 * (SingleNameReference)
 */
public void testLocalVariableReference1() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var1 = 1;", "var1");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		localVar, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/f1/X.java void f1.X.foo1() [var1]",
		resultCollector);
}
/*
 * Local variable reference test.
 * (QualifiedNameReference)
 */
public void testLocalVariableReference2() throws CoreException {
	ILocalVariable localVar = getLocalVariable("/JavaSearch/src/f1/X.java", "var2 = new X();", "var2");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		localVar, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/f1/X.java void f1.X.foo2() [var2]",
		resultCollector);
}
/*
 * Local variable reference test.
 * (regression test for bug 48725 Cannot search for local vars in jars.)
 */
public void testLocalVariableReference3() throws CoreException {
    IClassFile classFile = getClassFile("JavaSearch", "test48725.jar", "p", "X.class");
	ILocalVariable localVar = (ILocalVariable) codeSelect(classFile, "local = 1;", "local")[0];
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		localVar, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"test48725.jar void p.X.foo()",
		resultCollector);
}
/**
 * Long declaration (>255) test.
 * (regression test for bug 25859 Error doing Java Search)
 */
public void testLongDeclaration() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"AbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyz", 
		TYPE, 
		DECLARATIONS,
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/c9/X.java c9.AbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyz [AbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyz]", 
		resultCollector);
}
/**
 * Memeber type declaration test.
 * (regression test for bug 9992 Member class declaration not found)
 */
public void testMemberTypeDeclaration() throws CoreException {
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[]{
		this.getPackageFragment("JavaSearch", "src", "b4")
	});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"*.A.B", 
		TYPE,
		DECLARATIONS,
		scope, 
		resultCollector);
	assertSearchResults(
		"src/b4/A.java b4.A$B [B]", 
		resultCollector);
}
/**
 * Member type reference test.
 */
public void testMemberTypeReference() throws CoreException {
	// references to second level member type
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		"BMember", 
		TYPE,
		REFERENCES,
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"References to type BMember",
		"src/MemberTypeReference/Azz.java void MemberTypeReference.Azz.poo() [BMember] EXACT_MATCH\n" + 
		"src/MemberTypeReference/Azz.java MemberTypeReference.Azz$AzzMember [BMember] EXACT_MATCH\n" + 
		"src/MemberTypeReference/Azz.java MemberTypeReference.X.val [BMember] EXACT_MATCH\n" + 
		"src/MemberTypeReference/B.java void MemberTypeReference.B.foo() [BMember] EXACT_MATCH",
		resultCollector);

	// references to first level member type
	resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		"AzzMember", 
		TYPE,
		REFERENCES,
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"References to type AzzMember",
		"src/MemberTypeReference/Azz.java MemberTypeReference.X.val [AzzMember] EXACT_MATCH\n" + 
		"src/MemberTypeReference/B.java void MemberTypeReference.B.foo() [AzzMember] EXACT_MATCH",
		resultCollector);

	// no reference to a field with same name as member type
	resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		"BMember", 
		FIELD,
		REFERENCES,
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"References to field BMember",
		"",
		resultCollector);
}
/**
 * Member type reference test.
 * (regression test for PR 1GL0MN9: ITPJCORE:WIN2000 - search: not consistent results for nested types)
 */
public void testMemberTypeReference2() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "a", "A.java").getType("A").getType("X");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/a/A.java a.B.ax [A.X]\n" +
		"src/a/A.java a.B.sx [S.X]", 
		resultCollector);
}
/**
 * Method declaration test.
 * (regression test for bug 38568 Search for method declarations fooled by array types)
 */
public void testMethodDeclaration() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "e2", "X.java").getType("X");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"foo(String, String)", 
		METHOD,
		DECLARATIONS,
		SearchEngine.createJavaSearchScope(new IJavaElement[] {type}), 
		resultCollector);
	assertSearchResults(
		"src/e2/X.java void e2.X.foo(String, String) [foo]",
		resultCollector);
}
/**
 * Method declaration in hierarchy test.
 */
public void testMethodDeclarationInHierarchyScope1() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"foo", 
		METHOD,
		DECLARATIONS,
		SearchEngine.createHierarchyScope(type), 
		resultCollector);
	assertSearchResults(
		"src/p/X.java void p.X.foo(int, String, X) [foo]\n" + 
		"src/p/Z.java void p.Z.foo(int, String, X) [foo]",
		resultCollector);
}
/**
 * Method declaration in hierarchy that contains elements in external jar.
 * (regression test for PR #1G2E4F1)
 */
public void testMethodDeclarationInHierarchyScope2() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[] {"I", "QString;", "QX;"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		DECLARATIONS,
		SearchEngine.createHierarchyScope(type), 
		resultCollector);
	assertSearchResults(
		"src/p/X.java void p.X.foo(int, String, X) [foo]\n" + 
		"src/p/Z.java void p.Z.foo(int, String, X) [foo]",
		resultCollector);
}
/**
 * Method declaration in hierarchy on a secondary type.
 */
public void testMethodDeclarationInHierarchyScope3() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "d3", "A.java").getType("B");
	IMethod method = type.getMethod("foo", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		DECLARATIONS,
		SearchEngine.createHierarchyScope(type), 
		resultCollector);
	assertSearchResults(
		"src/d3/A.java void d3.B.foo() [foo]",
		resultCollector);
}
/**
 * Method declaration in field initialzer.
 * (regression test for bug 24346 Method declaration not found in field initializer  )
 */
public void testMethodDeclarationInInitializer() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"foo24346", 
		METHOD,
		DECLARATIONS,
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/c6/X.java void c6.X.x:<anonymous>#1.foo24346() [foo24346]",
		resultCollector);
}
/**
 * Method declaration in jar file test.
 */
public void testMethodDeclarationInJar() throws CoreException {
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p1", "A.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"Ljava.lang.String;"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	// System.out.println(displayString(resultCollector, 2));
	assertSearchResults(
		"MyJar.jar boolean p1.A.foo(java.lang.String) [No source]",
		resultCollector);
}
/**
 * Method declaration in package test.
 * (regression tets for PR #1G2KA97)
 */
public void testMethodDeclarationInPackageScope() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment()});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"main(String[])", 
		METHOD,
		DECLARATIONS,
		scope, 
		resultCollector);
	assertSearchResults(
		"src/p/A.java void p.A.main(String[]) [main]",
		resultCollector);
}
/*
 * Method declaration with a missing return type.
 * (regression test for bug 43080 NPE when searching in CU with incomplete method declaration)
 */
public void testMethodDeclarationNoReturnType() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "e8", "A.java").getType("A");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment()});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"m() int", 
		METHOD,
		DECLARATIONS,
		scope, 
		resultCollector);
	assertSearchResults(
		"src/e8/A.java void e8.A.m() [m]",
		resultCollector);
}
/**
 * Type declaration using a package scope test.
 * (check that subpackages are not included)
 */
public void testTypeDeclarationInPackageScope() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p3", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment()});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"X", 
		TYPE,
		DECLARATIONS,
		scope, 
		resultCollector);
	assertSearchResults(
		"src/p3/X.java p3.X [X]", 
		resultCollector);
}
/**
 * Type declaration using a binary package scope test.
 * (check that subpackages are not included)
 */
public void testTypeDeclarationInPackageScope2() throws CoreException {
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p0", "X.class").getType();
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment()});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"X", 
		TYPE,
		DECLARATIONS,
		scope, 
		resultCollector);
	assertSearchResults(
		"MyJar.jar p0.X [No source]", 
		resultCollector);
}
/**
 * Method reference through super test.
 */
public void testMethodReferenceThroughSuper() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "sd", "AQ.java").getType("AQ");
	IMethod method = type.getMethod("k", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/sd/AQ.java void sd.AQE.k() [k()]",
		resultCollector);
}
/**
 * Method reference test.
 * (regression test for bug 5068 search: missing method reference)
 */
public void testMethodReference1() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "q5", "AQ.java").getType("I");
	IMethod method = type.getMethod("k", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/q5/AQ.java void q5.T.m() [k()]",
		resultCollector);
}
/**
 * Method reference test.
 * (regression test for bug 5069 search: method reference in super missing)
 */
public void testMethodReference2() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "q6", "CD.java").getType("AQ");
	IMethod method = type.getMethod("k", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/q6/CD.java void q6.AQE.k() [k()]",
		resultCollector);
}
/**
 * Method reference test.
 * (regression test for bug 5070 search: missing interface method reference  )
 */
public void testMethodReference3() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "q7", "AQ.java").getType("I");
	IMethod method = type.getMethod("k", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/q7/AQ.java void q7.D.h() [k()]",
		resultCollector);
}
/**
 * Method reference test.
 * (regression test for bug 8928 Unable to find references or declarations of methods that use static inner classes in the signature)
 */
public void testMethodReference4() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "b2", "Y.java").getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"QX.Inner;"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/b2/Z.java void b2.Z.bar() [foo(inner)]",
		resultCollector);
}
/**
 * Method reference test.
 * (regression test for bug 49120 search doesn't find references to anonymous inner methods)
 */
public void testMethodReference5() throws CoreException {
	IType type = getCompilationUnit("JavaSearch/src/e9/A.java").getType("A").getMethod("foo", new String[] {}).getType("", 1);
	IMethod method = type.getMethod("bar", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/e9/A.java void e9.A.foo() [bar()]",
		resultCollector);
}
/*
 * Method reference in second anonymous and second local type of a method test.
 */
public void testMethodReference6() throws CoreException {
	IMethod method= getCompilationUnit("JavaSearch/src/f3/X.java").getType("X").getMethod("bar", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/f3/X.java void void f3.X.foo():<anonymous>#2.foobar() [bar()]\n" + 
		"src/f3/X.java void void f3.X.foo():Y#2.foobar() [bar()]",
		resultCollector);
}
/**
 * Method reference through array test.
 * (regression test for 1GHDA2V: ITPJCORE:WINNT - ClassCastException when doing a search)
 */
public void testMethodReferenceThroughArray() throws CoreException {
	IType type = getClassFile("JavaSearch", getExternalJCLPathString(), "java.lang", "Object.class").getType();
	IMethod method = type.getMethod("clone", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/E.java Object E.foo() [clone()]",
		resultCollector);
}
/**
 * Method reference in inner class test.
 */
public void testMethodReferenceInInnerClass() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "CA.java").getType("CA");
	IMethod method = type.getMethod("m", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/CA.java void CA$CB.f() [m()]\n" + 
		"src/CA.java void CA$CB$CC.f() [m()]",
		resultCollector);
}
/**
 * Method reference inside/outside doc comment.
 */
public void testMethodReferenceInOutDocComment() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "s4", "X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[] {});
	
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showInsideDoc = true;
	search(method, REFERENCES, getJavaSearchScope(), resultCollector);
	assertSearchResults(
		"src/s4/X.java void s4.X.bar() [foo] INSIDE_JAVADOC\n" + 
		"src/s4/X.java void s4.X.fred() [foo()] OUTSIDE_JAVADOC",
		resultCollector);
}
/**
 * Method reference in anonymous class test.
 * (regression test for PR 1GGNOTF: ITPJCORE:WINNT - Search doesn't find method referenced in anonymous inner class)
 */
public void testMethodReferenceInAnonymousClass() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "PR_1GGNOTF.java").getType("PR_1GGNOTF");
	IMethod method = type.getMethod("method", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/PR_1GGNOTF.java void void PR_1GGNOTF.method2():<anonymous>#1.run() [method()]",
		resultCollector);
}
/**
 * Member type named "Object" reference test.
 * (regression test for 1G4GHPS: ITPJUI:WINNT - Strange error message in search)
 */
public void testObjectMemberTypeReference() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "ObjectMemberTypeReference", "A.java")
		.getType("A")
		.getType("Object");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/ObjectMemberTypeReference/A.java void ObjectMemberTypeReference.A.foo() [Object] EXACT_MATCH",
		resultCollector);
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
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		orPattern, 
		getJavaSearchScope(),
		resultCollector);
	assertSearchResults(
		"src/e8/A.java void e8.A.m() [m] POTENTIAL_MATCH\n" + 
		"src/q9/I.java void q9.I.m() [m] EXACT_MATCH\n" + 
		"src/q9/I.java void q9.A1.m() [m] EXACT_MATCH",
		resultCollector);
}
/**
 * Package declaration test.
 * (regression test for bug 62698 NPE while searching for declaration of binary package)
 */
public void testPackageDeclaration() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", getExternalJCLPathString(), "java.lang");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		pkg, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		getExternalJCLPath() + " java.lang",
		resultCollector);
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
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		pkg, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/q1/B.java void q1.B.m(AA) [q2]",
		resultCollector);
}
/**
 * Package reference test.
 * (regression test for bug 17906 Rename package fails when inner classes are imported)
 */
public void testPackageReference2() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "b8");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		pkg, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/b9/Foo.java [b8]", 
		resultCollector);
}
/**
 * Package reference in jar test.
 * (regression test for bug 47989 Exception when searching for IPackageFragment "java.util.zip")
 */
public void testPackageReference3() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "test47989.jar", "p1");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg.getParent()});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		pkg, 
		REFERENCES, 
		scope, 
		resultCollector);
	assertSearchResults(
		"test47989.jar java.lang.Object p2.Y.foo()",
		resultCollector);
}
/**
 * Test pattern match package references
 */
public void testPatternMatchPackageReference() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"*p2.*", 
		PACKAGE,
		REFERENCES,
		getJavaSearchScope(), 
		resultCollector);
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
		resultCollector);
}
/**
 * Test pattern match package references
 * Just verify that there's no ArrayOutOfBoundException to validate fix for
 * bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=64421
 */
public void testPatternMatchPackageReference2() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"*", 
		PACKAGE,
		REFERENCES,
		getJavaSearchScope(), 
		resultCollector);
	resultCollector.toString();
}
/**
 * Test pattern match type declaration
 * (regression test for bug 17210 No match found when query contains '?')
 */
public void testPatternMatchTypeDeclaration() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"X?Z", 
		TYPE,
		DECLARATIONS,
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults("src/r5/XYZ.java r5.XYZ [XYZ]", resultCollector);
}
/**
 * Test pattern match type reference in binary
 * (regression test for bug 24741 Search does not find patterned type reference in binary project  )
 */
public void testPatternMatchTypeReference() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"p24741.*", 
		TYPE,
		REFERENCES,
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"test24741.jar q24741.B", 
		resultCollector);
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
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showAccuracy = true;
		search(
			"MissingFieldType.*",
			FIELD,
			DECLARATIONS, 
			getJavaSearchScope(), 
			resultCollector);
		assertSearchResults(
			"AbortCompilation.jar AbortCompilation.MissingFieldType.field [No source] POTENTIAL_MATCH\n" + 
			"AbortCompilation.jar AbortCompilation.MissingFieldType.missing [No source] POTENTIAL_MATCH\n" + 
			"AbortCompilation.jar AbortCompilation.MissingFieldType.otherField [No source] POTENTIAL_MATCH",
			resultCollector);
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
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showAccuracy = true;
		search(
			"MissingArgumentType.foo*",
			METHOD,
			DECLARATIONS, 
			getJavaSearchScope(), 
			resultCollector);
		assertSearchResults(
			"AbortCompilation.jar void AbortCompilation.MissingArgumentType.foo() [No source] POTENTIAL_MATCH\n" + 
			"AbortCompilation.jar void AbortCompilation.MissingArgumentType.foo(java.util.EventListener) [No source] POTENTIAL_MATCH\n" + 
			"AbortCompilation.jar void AbortCompilation.MissingArgumentType.foo2() [No source] POTENTIAL_MATCH",
			resultCollector);
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
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showAccuracy = true;
		search(
			"Missing*",
			TYPE,
			DECLARATIONS, 
			getJavaSearchScope(), 
			resultCollector);
		assertSearchResults(
			"AbortCompilation.jar AbortCompilation.EnclosingType$MissingEnclosingType [No source] EXACT_MATCH\n" + 
			"AbortCompilation.jar AbortCompilation.MissingArgumentType [No source] EXACT_MATCH\n" + 
			"AbortCompilation.jar AbortCompilation.MissingFieldType [No source] EXACT_MATCH",
			resultCollector);
	} finally {
		// reset classpath
		project.setRawClasspath(classpath, null);
	}
}
/**
 * Read and write access reference in compound expression test.
 * (regression test for bug 6158 Search - Prefix and postfix expression not found as write reference)
 */
public void testReadWriteFieldReferenceInCompoundExpression() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "a4", "X.java").getType("X");
	IField field = type.getField("field");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	
	// Read reference
	search(
		field, 
		READ_ACCESSES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/a4/X.java void a4.X.foo() [field]",
		resultCollector);
		
	// Write reference
	resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		WRITE_ACCESSES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/a4/X.java void a4.X.foo() [field]",
		resultCollector);
		
}
/**
 * Simple constructor declaration test.
 */
public void testSimpleConstructorDeclaration() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IMethod constructor = type.getMethod("A", new String[] {"QX;"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		constructor, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults("src/p/A.java p.A(X) [A]", resultCollector);
}
/**
 * Simple constructor reference test.
 */
public void testSimpleConstructorReference1() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IMethod constructor = type.getMethod("A", new String[] {"QX;"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		constructor, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [new p.A(y)]",
		resultCollector);
}
/**
 * Simple constructor reference test.
 */
public void testSimpleConstructorReference2() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"p.A(X)", 
		CONSTRUCTOR,
		REFERENCES,
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [new p.A(y)]",
		resultCollector);
}
/**
 * Simple declarations of sent messages test.
 */
public void testSimpleDeclarationsOfSentMessages() throws CoreException {
	ICompilationUnit cu = getCompilationUnit("JavaSearch", "src", "", "Test.java");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	searchDeclarationsOfSentMessages(
		cu, 
		resultCollector);
	assertSearchResults(
		"src/p/X.java void p.X.foo(int, String, X) [foo(int i, String s, X x)]\n" + 
		"src/p/Y.java void p.Y.bar() [bar()]\n" + 
		"src/p/Z.java void p.Z.foo(int, String, X) [foo(int i, String s, X x)]\n" + 
		"src/p/A.java void p.A.foo(int, String, X) [foo()]",
		resultCollector);
}
/**
 * Simple field declaration test.
 */
public void testSimpleFieldDeclaration() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("x");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/p/A.java p.A.x [x]", 
		resultCollector);
}
/**
 * Simple field reference test.
 */
public void testSimpleFieldReference() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("x");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [x]\n" + 
		"src/p/A.java p.A(X) [x]",
		resultCollector);
}
/**
 * Simple write field access reference test.
 */
public void testSimpleWriteFieldReference() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("x");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		WRITE_ACCESSES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/p/A.java p.A(X) [x]", 
		resultCollector);
}
/**
 * Sub-cu java search scope test.
 * (regression test for bug 9041 search: cannot create a sub-cu scope)
 */
public void testSubCUSearchScope1() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "b3", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES,
		scope, 
		resultCollector);
	assertSearchResults(
		"src/b3/X.java b3.X.field [X]\n" + 
		"src/b3/X.java Object b3.X.foo() [X]\n" + 
		"src/b3/X.java b3.X$Y.field2 [X]\n" + 
		"src/b3/X.java Object b3.X$Y.foo2() [X]",
		resultCollector);
}
/**
 * Sub-cu java search scope test.
 * (regression test for bug 9041 search: cannot create a sub-cu scope)
 */
public void testSubCUSearchScope2() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "b3", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getField("field")});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES,
		scope, 
		resultCollector);
	assertSearchResults(
		"src/b3/X.java b3.X.field [X]", 
		resultCollector);
}
/**
 * Sub-cu java search scope test.
 * (regression test for bug 9041 search: cannot create a sub-cu scope)
 */
public void testSubCUSearchScope3() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "b3", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getType("Y")});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES,
		scope, 
		resultCollector);
	assertSearchResults(
		"src/b3/X.java b3.X$Y.field2 [X]\n" + 
		"src/b3/X.java Object b3.X$Y.foo2() [X]",
		resultCollector);
}
/**
 * Type declaration test.
 * (regression test for bug 29524 Search for declaration via patterns adds '"*")
 */
public void testTypeDeclaration() throws CoreException {
	IPackageFragment pkg = this.getPackageFragment("JavaSearch", "src", "d8");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"A", 
		TYPE,
		DECLARATIONS,
		scope, 
		resultCollector);
	assertSearchResults("src/d8/A.java d8.A [A]", resultCollector);
}
/**
 * Simple method declaration test.
 */
public void testSimpleMethodDeclaration() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[] {"I", "QString;", "QX;"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/p/X.java void p.X.foo(int, String, X) [foo]\n" + 
		"src/p/Z.java void p.Z.foo(int, String, X) [foo]",
		resultCollector);
}
/**
 * Simple method reference test.
 */
public void testSimpleMethodReference() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[] {"I", "QString;", "QX;"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [foo(1, \"a\", y)]\n" + 
		"src/Test.java void Test.main(String[]) [foo(1, \"a\", z)]\n" + 
		"src/p/Z.java void p.Z.foo(int, String, X) [foo(i, s, new Y(true))]",
		resultCollector);
}
/**
 * Simple package declaration test.
 */
public void testSimplePackageDeclaration() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "p");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		pkg, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/p p", 
		resultCollector);
}
/**
 * Simple package reference test.
 */
public void testSimplePackageReference() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "p");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		pkg, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
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
		resultCollector);
}
/**
 * Simple field read access reference test.
 */
public void testSimpleReadFieldReference() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("x");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		READ_ACCESSES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [x]",
		resultCollector);
}
/**
 * Simple type declaration test.
 */
public void testSimpleTypeDeclaration() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		DECLARATIONS,
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults("src/p/X.java p.X [X]", resultCollector);
}
/**
 * Simple type reference test.
 */
public void testSimpleTypeReference() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/p/A.java p.A.x [X]\n" + 
		"src/p/A.java p.A(X) [X]\n" + 
		"src/p/A.java void p.A.foo(int, String, X) [X]\n" + 
		"src/p/X.java p.X() [X]\n" + 
		"src/p/X.java void p.X.foo(int, String, X) [X]\n" + 
		"src/p/Y.java p.Y [X]\n" + 
		"src/p/Z.java void p.Z.foo(int, String, X) [X]",
		resultCollector);
}
/**
 * Static field reference test.
 * (regression test for PR #1G2P5EP)
 */
public void testStaticFieldReference() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("DEBUG");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/p/A.java void p.A.foo() [DEBUG]",
		resultCollector);
}
/**
 * Static method reference test.
 */
public void testStaticMethodReference1() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "Y.java").getType("Y");
	IMethod method = type.getMethod("bar", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [bar()]",
		resultCollector);
}
/**
 * Static method reference test.
 */
public void testStaticMethodReference2() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("bar", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"", 
		resultCollector);
}
/**
 * Type declaration in jar file test.
 */
public void testTypeDeclarationInJar() throws CoreException {
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p1", "A.class").getType();
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"MyJar.jar p1.A [No source]", 
		resultCollector);
}
/**
 * Type declaration in jar file and in anonymous class test.
 * (regression test for 20631 Declaration of local binary type not found)
 */
public void testTypeDeclarationInJar2() throws CoreException {
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaSearch", "test20631.jar");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {root});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"Y", 
		TYPE,
		DECLARATIONS, 
		scope, 
		resultCollector);
	assertSearchResults(
		"test20631.jar void X.foo()",
		resultCollector);
}
/**
 * Type ocurrence test.
 * (regression test for PR 1GKAQJS: ITPJCORE:WIN2000 - search: incorrect results for nested types)
 */
public void testTypeOccurence() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "r", "A.java").getType("A").getType("X");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		ALL_OCCURRENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/r/A.java A r.A.m() [X]\n" + 
		"src/r/A.java r.A$X [X]\n" + 
		"src/r/A.java r.A$X(X) [X]\n" + 
		"src/r/A.java r.A$X(X) [X]\n" + 
		"src/r/A.java r.B.ax [A.X]\n" + 
		"src/r/A.java r.B.ax [X]",
		resultCollector);
}
/**
 * Type ocuurence in unresolvable import test.
 * (regression test for bug 37166 NPE in SearchEngine when matching type against ProblemReferenceBinding )
 */
public void testTypeOccurence2() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "r8", "B.java").getType("B");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment()});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		ALL_OCCURRENCES, 
		scope, 
		resultCollector);
	assertSearchResults(
		"src/r8/A.java [B]",
		resultCollector);
}
/**
 * Type occurences test.
 * Ensures that correct positions are reported for an inner type reference using a ALL_OCCURENCES pattern
 */
public void testTypeOccurence3() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "e4", "A.java").getType("A").getType("Inner");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		ALL_OCCURRENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/e4/A.java e4.A$Inner [Inner]\n" +
		"src/e5/A1.java [e4.A.Inner]\n" +
		"src/e5/A1.java e5.A1.a [e4.A.Inner]\n" +
		"src/e5/A1.java e5.A1.a1 [e4.A.Inner]\n" +
		"src/e5/A1.java e5.A1.a2 [Inner]\n" +
		"src/e5/A1.java e5.A1.a3 [Inner]",
		resultCollector);
}
/**
 * Type name with $ ocurrence test.
 * (regression test for bug 3310 Smoke 124: Compile errors introduced with rename refactoring (1GFBK2G))
 */
public void testTypeOccurenceWithDollar() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "q3", "A$B.java").getType("A$B");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		ALL_OCCURRENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/q3/A$B.java q3.A$B [A$B]\n" + 
		"src/q4/C.java Object q4.C.foo() [q3.A$B]",
		resultCollector);
}
/**
 * Type reference test.
 * (Regression test for PR 1GK7K17: ITPJCORE:WIN2000 - search: missing type reference)
 */
public void testTypeReference1() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "X.java").getType("X");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/X.java AA() [X]",
		resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 29516 SearchEngine regressions in 20030114)
 */
public void testTypeReference2() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "d7", "A.java").getType("A");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/d7/A.java d7.A.A [A]\n" + 
		"src/d7/A.java A d7.A.A(A) [A]\n" + 
		"src/d7/A.java A d7.A.A(A) [A]",
		resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 31985 NPE searching non-qualified and case insensitive type ref)
 */
public void testTypeReference3() throws CoreException {
	SearchPattern pattern = createPattern("x31985", TYPE, REFERENCES, false);
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		pattern, 
		getJavaSearchScope(),
		resultCollector);
	assertSearchResults(
		"src/e3/X31985.java e3.X31985.CONSTANT [X31985] EXACT_MATCH\n" + 
		"src/e3/Y31985.java Object e3.Y31985.foo() [X31985] EXACT_MATCH",
		resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 31997 Refactoring d.n. work for projects with brackets in name.)
 */
public void testTypeReference4() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "otherSrc()", "", "X31997.java").getType("X31997");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"otherSrc()/Y31997.java Y31997 [X31997]",
		resultCollector);
}
/**
 * Type reference test.
 * (Regression test for bug 48261 Search does not show results)
 */
public void testTypeReference5() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "test48261.jar", "p", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment().getParent()});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		scope, 
		resultCollector);
	assertSearchResults(
		"test48261.jar p.X$Y(java.lang.String)",
		resultCollector);
}
/**
 * Type reference as a single name reference test.
 */
public void testTypeReferenceAsSingleNameReference() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "TypeReferenceAsSingleNameReference.java").getType("TypeReferenceAsSingleNameReference");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/TypeReferenceAsSingleNameReference.java void TypeReferenceAsSingleNameReference.hasReference() [TypeReferenceAsSingleNameReference]\n" + 
		"src/TypeReferenceAsSingleNameReference.java void TypeReferenceAsSingleNameReference.hasReference() [TypeReferenceAsSingleNameReference]",
		resultCollector);
}
/**
 * Type reference in array test.
 * (regression test for PR #1GAL424)
 */
public void testTypeReferenceInArray() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "TypeReferenceInArray", "A.java").getType("A");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/TypeReferenceInArray/A.java TypeReferenceInArray.A.a [A]\n" +
		"src/TypeReferenceInArray/A.java TypeReferenceInArray.A.b [TypeReferenceInArray.A]",
		resultCollector);
}
/**
 * Type reference in array test.
 * (regression test for bug 3230 Search - Too many type references for query ending with * (1GAZVGI)  )
 */
public void testTypeReferenceInArray2() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "s1", "X.java").getType("X");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/s1/Y.java s1.Y.f [X]",
		resultCollector);
}
/**
 * Type reference in cast test.
 * (regression test for bug 23329 search: incorrect range for type references in brackets)
 */
public void testTypeReferenceInCast() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "s3", "A.java").getType("B");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/s3/A.java Object s3.A.foo() [B]",
		resultCollector);
}
/**
 * Type reference in hierarchy test.
 * (regression test for bug 28236 Search for refs to class in hierarchy matches class outside hierarchy )
 */
public void testTypeReferenceInHierarchy() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "d9.p1", "A.java").getType("A");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	IJavaSearchScope scope = SearchEngine.createHierarchyScope(type);
	search(
		type, 
		REFERENCES, 
		scope, 
		resultCollector);
	assertSearchResults(
		"",
		resultCollector);
}
/**
 * Type reference in import test.
 * (regression test for PR #1GA7PAS)
 */
public void testTypeReferenceInImport() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p2", "Z.java").getType("Z");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/TypeReferenceInImport/X.java [p2.Z]",
		resultCollector);
}
/**
 * Type reference in import test.
 * (regression test for bug 23077 search: does not find type references in some imports)
 */
public void testTypeReferenceInImport2() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "r6", "A.java").getType("A");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]",
		resultCollector);
}
/**
 * Type reference in initializer test.
 * (regression test for PR #1G4GO4O)
 */
public void testTypeReferenceInInitializer() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "Test.java").getType("Test");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/Test.java Test.static {} [Test]\n" +
		"src/Test.java Test.static {} [Test]\n" +
		"src/Test.java Test.{} [Test]",
		resultCollector);
}
/**
 * Type reference inside/outside doc comment.
 */
public void testTypeReferenceInOutDocComment() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "s4", "X.java").getType("X");
	
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showInsideDoc = true;
	search(type, REFERENCES, getJavaSearchScope(), resultCollector);
	assertSearchResults(
		"src/s4/X.java void s4.X.bar() [X] INSIDE_JAVADOC\n" + 
		"src/s4/X.java void s4.X.bar() [X] INSIDE_JAVADOC\n" + 
		"src/s4/X.java void s4.X.bar() [X] INSIDE_JAVADOC\n" + 
		"src/s4/X.java void s4.X.fred() [X] OUTSIDE_JAVADOC",
		resultCollector);
}
/**
 * Type reference inside a qualified name reference test.
 * (Regression test for PR #1G4TSC0)
 */
public void testTypeReferenceInQualifiedNameReference() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/Test.java void Test.main(String[]) [p.A]\n" + 
		"src/Test.java void Test.main(String[]) [p.A]\n" + 
		"src/p/A.java void p.A.foo() [A]",
		resultCollector);
}
/**
 * Type reference inside a qualified name reference test.
 * (Regression test for PR #1GLBP65)
 */
public void testTypeReferenceInQualifiedNameReference2() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p4", "A.java").getType("A");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/p4/A.java p4.A.A [A]\n" + 
		"src/p4/A.java p4.X [p4.A]\n" + 
		"src/p4/A.java void p4.X.x() [p4.A]",
		resultCollector);
}
/**
 * Type reference inside a qualified name reference test.
 * (Regression test for PR 1GL9UMH: ITPJCORE:WIN2000 - search: missing type occurrences)
 */
public void testTypeReferenceInQualifiedNameReference3() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "W.java").getType("W");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/W.java int W.m() [W]",
		resultCollector);
}
/**
 * Type reference inside a qualified name reference test.
 * (Regression test for bug 16751 Renaming a class doesn't update all references  )
 */
public void testTypeReferenceInQualifiedNameReference4() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "b7", "X.java").getType("SubClass");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/b7/X.java void b7.Test.main(String[]) [SubClass]",
		resultCollector);
}
/**
 * Type reference in a throw clause test.
 * (Regression test for bug 6779 searchDeclarationsOfReferencedTyped - missing exception types)
 */
public void testTypeReferenceInThrows() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "a7", "X.java").getType("MyException");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/a7/X.java void a7.X.foo() [MyException] EXACT_MATCH",
		resultCollector);
}
/**
 * Type reference test (not case sensitive)
 */
public void testTypeReferenceNotCaseSensitive() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "d4");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});
	SearchPattern pattern = createPattern("Y", TYPE, REFERENCES, false);
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		pattern, 
		scope, 
		resultCollector);
	assertSearchResults(
		"src/d4/X.java Object d4.X.foo() [Y]",
		resultCollector);
}
/**
 * Type reference in a folder that is not in the classpath.
 * (regression test for PR #1G5N8KS)
 */
public void testTypeReferenceNotInClasspath() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/p/A.java p.A.x [X]\n" + 
		"src/p/A.java p.A(X) [X]\n" + 
		"src/p/A.java void p.A.foo(int, String, X) [X]\n" + 
		"src/p/X.java p.X() [X]\n" + 
		"src/p/X.java void p.X.foo(int, String, X) [X]\n" + 
		"src/p/Y.java p.Y [X]\n" + 
		"src/p/Z.java void p.Z.foo(int, String, X) [X]",
		resultCollector);
}
/**
 * Type reference with corrupt jar on the classpath test.
 * (Regression test for bug 39831 Search finds only "inexact" matches)
 */
public void testTypeReferenceWithCorruptJar() throws CoreException {
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
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showAccuracy = true;
		search(
			type, 
			REFERENCES,
			SearchEngine.createJavaSearchScope(new IJavaElement[] {project}), 
			resultCollector);
		assertSearchResults(
			"src/e7/A.java e7.A.a [A] EXACT_MATCH",
			resultCollector);
	} finally {
		project.setRawClasspath(originalCP, null);
	}
}
/**
 * Type reference with problem test.
 * (Regression test for bug 36479 Rename operation during refactoring fails)
 */
public void testTypeReferenceWithProblem() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "e6", "A.java").getType("A");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		"B36479", 
		TYPE,
		REFERENCES, 
		SearchEngine.createJavaSearchScope(new IJavaElement[] {type}), 
		resultCollector);
	assertSearchResults(
		"src/e6/A.java Object e6.A.foo() [B36479] POTENTIAL_MATCH",
		resultCollector);
}
/**
 * Type reference with recovery test.
 * (Regression test for bug 29366 Search reporting invalid inaccurate match )
 */
public void testTypeReferenceWithRecovery() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "e1", "A29366.java").getType("A29366");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/e1/A29366.java void e1.A29366.foo() [A29366] EXACT_MATCH",
		resultCollector);
}
/**
 * Negative type reference test.
 * (regression test for 1G52F7P: ITPJCORE:WINNT - Search - finds bogus references to class)
 */
public void testNegativeTypeReference() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p7", "A.java").getType("A");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"",
		resultCollector);
}

/**
 * Various package declarations test.
 */
public void testVariousPackageDeclarations() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		"p3*", 
		PACKAGE,
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"src/p3 p3\n" +
		"src/p3/p2/p p3.p2.p", 
		resultCollector);
}
/**
 * Various package reference test.
 */
public void testVariousPackageReference() throws CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "p3.p2.p");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		pkg, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
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
		resultCollector);
}
/**
 * Type reference inside an argument, a return type or a field type.
 * (Regression test for PR #1GA7QA1)
 */
public void testVariousTypeReferences() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "NoReference", "A.java").getType("A");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	search(
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"", // no reference should be found
		resultCollector);
}
/**
 * Write access reference in a qualified name reference test.
 * (regression test for bug 7344 Search - write acces give wrong result)
 */
public void testReadWriteAccessInQualifiedNameReference() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "a8", "A.java").getType("A");
	IField field = type.getField("a");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	
	resultCollector = new JavaSearchResultCollector();
	search(
		field, 
		WRITE_ACCESSES, 
		getJavaSearchScope(), 
		resultCollector);
	assertSearchResults(
		"", 
		resultCollector);
		
}
}
