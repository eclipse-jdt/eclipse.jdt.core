/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

import org.eclipse.jdt.internal.core.JavaModelStatus;
import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.compiler.CharOperation;

import junit.framework.*;

/**
 * Tests the Java search engine where results are JavaElements and source positions.
 */
public class JavaSearchTests extends AbstractJavaModelTests implements IJavaSearchConstants {

/**
 * Collects results as a string.
 */
public static class JavaSearchResultCollector implements IJavaSearchResultCollector {
	public StringBuffer results = new StringBuffer();
	public boolean showAccuracy = false;
	public boolean showProject = false;
	public void aboutToStart() {
	}
	public void accept(IResource resource, int start, int end, IJavaElement element, int accuracy) {
		try {
			if (results.length() > 0) results.append("\n");
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
			if (showProject) {
				IProject project = element.getJavaProject().getProject();
				results.append(" [in ");
				results.append(project.getName());
				results.append("]");
			}
			ICompilationUnit unit = null;
			if (element instanceof IMethod) {
				results.append(" ");
				IMethod method = (IMethod)element;
				results.append(method.getDeclaringType().getFullyQualifiedName());
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
				if (!method.isConstructor()) {
					results.append(" -> ");
					results.append(Signature.toString(method.getReturnType()));
				}
				unit = method.getCompilationUnit();
			} else if (element instanceof IType) {
				results.append(" ");
				IType type = (IType)element;
				results.append(type.getFullyQualifiedName());
				unit = type.getCompilationUnit();
			} else if (element instanceof IField) {
				results.append(" ");
				IField field = (IField)element;
				results.append(field.getDeclaringType().getFullyQualifiedName());
				results.append(".");
				results.append(field.getElementName());
				unit = field.getCompilationUnit();
			} else if (element instanceof IInitializer) {
				results.append(" ");
				IInitializer initializer = (IInitializer)element;
				results.append(initializer.getDeclaringType().getFullyQualifiedName());
				results.append(".");
				if (Flags.isStatic(initializer.getFlags())) {
					results.append("static ");
				}
				results.append("{}");
				unit = initializer.getCompilationUnit();
			} else if (element instanceof IPackageFragment) {
				results.append(" ");
				results.append(element.getElementName());
			}
			if (resource instanceof IFile) {
				char[] contents = null;
				if ("java".equals(resource.getFileExtension())) {
					if (!resource.equals(element.getUnderlyingResource())) {
						// working copy
						contents = unit.getBuffer().getCharacters();
					} else {
						contents = new org.eclipse.jdt.internal.compiler.batch.CompilationUnit(
							null, 
							((IFile) resource).getLocation().toFile().getPath(),
							null).getContents();
					}
				}
				if (start == -1 || contents != null) { // retrieving attached source not implemented here
					results.append(" [");
					if (start > -1) {
						results.append(CharOperation.subarray(contents, start, end));
					} else {
						results.append("No source");
					}
					results.append("]");
				}
			}
			if (showAccuracy) {
				results.append(" ");
				switch (accuracy) {
					case EXACT_MATCH:
						results.append("EXACT_MATCH");
						break;
					case POTENTIAL_MATCH:
						results.append("POTENTIAL_MATCH");
						break;
				}
			}
		} catch (JavaModelException e) {
			results.append("\n");
			results.append(e.toString());
		}
	}
	public void done() {
	}
	public IProgressMonitor getProgressMonitor() {
		return null;
	}
	public String toString() {
		return results.toString();
	}
}
public JavaSearchTests(String name) {
	super(name);
}
private IJavaSearchScope getJavaSearchScope() {
	return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearch")});
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	
	setUpJavaProject("JavaSearch");
}
public void tearDownSuite() throws Exception {
	deleteProject("JavaSearch");
	
	super.tearDownSuite();
}


public static Test suite() {
	TestSuite suite = new Suite(JavaSearchTests.class.getName());
	
	// package declaration
	suite.addTest(new JavaSearchTests("testSimplePackageDeclaration"));
	suite.addTest(new JavaSearchTests("testVariousPackageDeclarations"));
	
	// package reference
	suite.addTest(new JavaSearchTests("testSimplePackageReference"));
	suite.addTest(new JavaSearchTests("testPackageReference1"));
	suite.addTest(new JavaSearchTests("testPackageReference2"));
	suite.addTest(new JavaSearchTests("testVariousPackageReference"));
	suite.addTest(new JavaSearchTests("testAccuratePackageReference"));
	suite.addTest(new JavaSearchTests("testPatternMatchPackageReference"));

	// type declaration
	suite.addTest(new JavaSearchTests("testSimpleTypeDeclaration"));
	suite.addTest(new JavaSearchTests("testTypeDeclaration"));
	suite.addTest(new JavaSearchTests("testTypeDeclarationInJar"));
	suite.addTest(new JavaSearchTests("testTypeDeclarationInJar2"));
	suite.addTest(new JavaSearchTests("testTypeDeclarationInJar3"));
	suite.addTest(new JavaSearchTests("testTypeDeclarationInPackageScope"));
	suite.addTest(new JavaSearchTests("testTypeDeclarationInPackageScope2"));
	suite.addTest(new JavaSearchTests("testMemberTypeDeclaration"));
	suite.addTest(new JavaSearchTests("testPatternMatchTypeDeclaration"));
	suite.addTest(new JavaSearchTests("testLongDeclaration"));
	
	// type reference
	suite.addTest(new JavaSearchTests("testSimpleTypeReference"));
	suite.addTest(new JavaSearchTests("testTypeReference1"));
	suite.addTest(new JavaSearchTests("testTypeReference2"));
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
	
	// type occurences
	suite.addTest(new JavaSearchTests("testTypeOccurence"));
	suite.addTest(new JavaSearchTests("testTypeOccurenceWithDollar"));
	
	// interface implementor
	suite.addTest(new JavaSearchTests("testInterfaceImplementors"));
	suite.addTest(new JavaSearchTests("testInterfaceImplementors2"));
	
	// method declaration
	suite.addTest(new JavaSearchTests("testSimpleMethodDeclaration"));
	suite.addTest(new JavaSearchTests("testSimpleConstructorDeclaration"));
	suite.addTest(new JavaSearchTests("testInnerMethodDeclaration"));
	suite.addTest(new JavaSearchTests("testMethodDeclarationInHierarchyScope1"));
	suite.addTest(new JavaSearchTests("testMethodDeclarationInHierarchyScope2"));
	suite.addTest(new JavaSearchTests("testMethodDeclarationInHierarchyScope3"));
	suite.addTest(new JavaSearchTests("testMethodDeclarationInPackageScope"));
	suite.addTest(new JavaSearchTests("testMethodDeclarationInJar"));
	suite.addTest(new JavaSearchTests("testConstructorDeclarationInJar"));
	suite.addTest(new JavaSearchTests("testMethodDeclarationInInitializer"));
	
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
	
	// constructor reference
	suite.addTest(new JavaSearchTests("testSimpleConstructorReference1"));
	suite.addTest(new JavaSearchTests("testSimpleConstructorReference2"));
	suite.addTest(new JavaSearchTests("testConstructorReferenceExplicitConstructorCall1"));
	suite.addTest(new JavaSearchTests("testConstructorReferenceExplicitConstructorCall2"));
	suite.addTest(new JavaSearchTests("testConstructorReferenceImplicitConstructorCall1"));
	suite.addTest(new JavaSearchTests("testConstructorReferenceImplicitConstructorCall2"));
	suite.addTest(new JavaSearchTests("testConstructorReferenceInFieldInitializer"));

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
	suite.addTest(new JavaSearchTests("testFieldReferenceInInnerClass"));
	suite.addTest(new JavaSearchTests("testFieldReferenceInAnonymousClass"));
	suite.addTest(new JavaSearchTests("testFieldReferenceThroughSubclass"));
	suite.addTest(new JavaSearchTests("testReadWriteFieldReferenceInCompoundExpression"));
	suite.addTest(new JavaSearchTests("testReadWriteAccessInQualifiedNameReference"));
	suite.addTest(new JavaSearchTests("testFieldReferenceInBrackets"));
	suite.addTest(new JavaSearchTests("testAccurateFieldReference1"));
	
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
	
	// declarations of sent messages
	suite.addTest(new JavaSearchTests("testSimpleDeclarationsOfSentMessages"));
	suite.addTest(new JavaSearchTests("testDeclarationOfSentMessages"));
	
	// potential match in binary
	suite.addTest(new JavaSearchTests("testPotentialMatchInBinary"));

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
/**
 * Type reference test.
 */
public void testAccurateFieldReference1() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"d6.X.CONSTANT", 
		FIELD,
		REFERENCES, 
		SearchEngine.createJavaSearchScope(new IJavaElement[] {
			getPackageFragment("JavaSearch", "src", "d6")
		}), 
		resultCollector);
	assertEquals(
		"src/d6/Y.java d6.Y.T [CONSTANT]",
		resultCollector.toString());
}
/**
 * Regression test for 1GBK7B2: ITPJCORE:WINNT - package references: could be more precise
 */
public void testAccuratePackageReference() throws JavaModelException, CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "p3.p2");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		pkg, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/PackageReference/K.java [p3.p2]", 
		resultCollector.toString());
}
/**
 * Type reference test.
 */
public void testAccurateTypeReference() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"d5.X", 
		TYPE,
		REFERENCES, 
		SearchEngine.createJavaSearchScope(new IJavaElement[] {
			getPackageFragment("JavaSearch", "src", "d5")
		}), 
		resultCollector);
	assertEquals(
		"src/d5/Y.java d5.Y.T [d5.X]\n" +
		"src/d5/Y.java d5.Y.c [d5.X]\n" +
		"src/d5/Y.java d5.Y.o [d5.X]",
		resultCollector.toString());
}
/**
 * Constructor declaration in jar file test.
 */
public void testConstructorDeclarationInJar() throws JavaModelException, CoreException {
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p1", "A.class").getType();
	IMethod method = type.getMethod("A", new String[] {"Ljava.lang.String;"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"MyJar.jar p1.A(java.lang.String) [No source]", 
		resultCollector.toString());
}
/**
 * Constructor reference using an explicit constructor call.
 */
public void testConstructorReferenceExplicitConstructorCall1() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "Y.java").getType("Y");
	IMethod method = type.getMethod("Y", new String[] {"I"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/p/Z.java p.Z(int) [super(i)]", 
		resultCollector.toString());
}
/**
 * Constructor reference using an explicit constructor call.
 */
public void testConstructorReferenceExplicitConstructorCall2() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("X", new String[] {"I"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/p/Y.java p.Y(int) [super(i)]\n" +
		"src/p/Y.java p.Y(boolean) [super(1)]", 
		resultCollector.toString());
}
/**
 * Constructor reference using an implicit constructor call.
 * (regression test for bug 23112 search: need a way to search for references to the implicit non-arg constructor)
 */
public void testConstructorReferenceImplicitConstructorCall1() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "c7", "X.java").getType("X");
	IMethod method = type.getMethod("X", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/c7/Y.java c7.Y() [Y]", 
		resultCollector.toString());
}
/**
 * Constructor reference using an implicit constructor call.
 * (regression test for bug 23112 search: need a way to search for references to the implicit non-arg constructor)
 */
public void testConstructorReferenceImplicitConstructorCall2() throws JavaModelException, CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"c8.X()", 
		CONSTRUCTOR,
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/c8/Y.java c8.Y [Y]", 
		resultCollector.toString());
}
/**
 * Constructor reference in a field initializer.
 * (regression test for PR 1GKZ8VZ: ITPJCORE:WINNT - Search - did not find references to member constructor)
 */
public void testConstructorReferenceInFieldInitializer() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "A.java").getType("A").getType("Inner");
	IMethod method = type.getMethod("Inner", new String[] {"I"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/A.java A.field [new Inner(1)]\n" +
		"src/A.java A.field [new Inner(2)]", 
		resultCollector.toString());
}
/**
 * CoreException thrown during accept.
 * (regression test for PR #1G3UI7A)
 */
public void testCoreException() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IJavaSearchResultCollector resultCollector = new IJavaSearchResultCollector() {
		public void accept(IResource resource, int start, int end, IJavaElement enclosingElement, int accuracy) throws CoreException {
			throw new CoreException(new JavaModelStatus(-1, "test"));
		}
		public void aboutToStart() {}
		public void done() {}
		public IProgressMonitor getProgressMonitor() {
			return null;
		}
	};
	try {
		new SearchEngine().search(
			getWorkspace(), 
			type, 
			DECLARATIONS, 
			getJavaSearchScope(), 
			resultCollector);
	} catch (JavaModelException e) {
		Throwable wrappedException = e.getException();
		assertTrue("Unexpected wrapped exception", wrappedException instanceof CoreException);
		assertEquals("Unexpected CoreException has been thrown", "test", ((CoreException)wrappedException).getStatus().getMessage());
		return;
	}
	assertTrue("CoreException should have been thrown", false);
}
/**
 * Declaration of accessed fields test.
 * (regression test for bug 6538 searchDeclarationsOf* incorrect)
 */
public void testDeclarationOfAccessedFields1() throws JavaModelException, CoreException {
	IMethod method = 
		getCompilationUnit("JavaSearch", "src", "a5", "B.java").
			getType("C").getMethod("i", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().searchDeclarationsOfAccessedFields(
		getWorkspace(), 
		method,
		resultCollector
	);
	assertEquals(
		"", 
		resultCollector.toString());
}
/**
 * Declaration of accessed fields test.
 * (regression test for bug 6538 searchDeclarationsOf* incorrect)
 */
public void testDeclarationOfAccessedFields2() throws JavaModelException, CoreException {
	IMethod method = 
		getCompilationUnit("JavaSearch", "src", "a6", "A.java").
			getType("B").getMethod("m", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().searchDeclarationsOfAccessedFields(
		getWorkspace(), 
		method,
		resultCollector
	);
	assertEquals(
		"src/a6/A.java a6.B.f [f]", 
		resultCollector.toString());
}
/**
 * Declaration of accessed fields test.
 * (regression test for bug 10386 NPE in MatchLocator.lookupType)
 */
public void testDeclarationOfAccessedFields3() throws JavaModelException, CoreException {
	IMethod method = 
		getCompilationUnit("JavaSearch", "src", "b6", "A.java").
			getType("A").getMethod("foo", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().searchDeclarationsOfAccessedFields(
		getWorkspace(), 
		method,
		resultCollector
	);
	assertEquals(
		"src/b6/A.java b6.A.field [field]", 
		resultCollector.toString());
}
/**
 * Declaration of referenced types test.
 */
public void testDeclarationOfReferencedTypes1() throws JavaModelException, CoreException {
	IMethod method = 
		getCompilationUnit("JavaSearch", "src", "a3", "References.java").
			getType("References").getMethod("foo", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().searchDeclarationsOfReferencedTypes(
		getWorkspace(), 
		method,
		resultCollector
	);
	assertEquals(
		"src/a3/X.java a3.X [X]\n" +
		"src/a3/Z.java a3.Z [Z]\n" +
		"src/a3/b/A.java a3.b.A [A]\n" +
		"src/a3/b/A.java a3.b.A$B$C [C]\n" +
		"src/a3/b/A.java a3.b.A$B [B]\n" +
		getExternalJCLPath() + " java.lang.Object\n" +
		"src/a3/Y.java a3.Y [Y]\n" +
		"src/a3/b/B.java a3.b.B [B]", 
		resultCollector.toString());
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
	new SearchEngine().searchDeclarationsOfReferencedTypes(
		getWorkspace(), 
		method,
		resultCollector
	);
	assertEquals(
		"src/a7/X.java a7.MyException [MyException]", 
		resultCollector.toString());
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 12649 Missing import after move  )
 */
public void testDeclarationOfReferencedTypes3() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch", "src", "c1", "A.java");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().searchDeclarationsOfReferencedTypes(
		getWorkspace(), 
		unit,
		resultCollector
	);
	assertEquals(
		"src/c1/I.java c1.I [I]", 
		resultCollector.toString());
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 12649 Missing import after move  )
 */
public void testDeclarationOfReferencedTypes4() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch", "src", "c1", "B.java");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().searchDeclarationsOfReferencedTypes(
		getWorkspace(), 
		unit,
		resultCollector
	);
	assertEquals(
		"src/c1/I.java c1.I [I]", 
		resultCollector.toString());
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 18418 search: searchDeclarationsOfReferencedTypes reports import declarations)
 */
public void testDeclarationOfReferencedTypes5() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch", "src", "c2", "A.java");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().searchDeclarationsOfReferencedTypes(
		getWorkspace(), 
		unit,
		resultCollector
	);
	assertEquals(
		"src/c3/C.java c3.C [C]", 
		resultCollector.toString());
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 24934 Move top level doesn't optimize the imports[refactoring])
 */
public void testDeclarationOfReferencedTypes6() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearch", "src", "d1", "X.java");
	IType innerType = unit.getType("X").getType("Inner");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().searchDeclarationsOfReferencedTypes(
		getWorkspace(), 
		innerType,
		resultCollector
	);
	assertEquals(
		"src/d2/Y.java d2.Y [Y]", 
		resultCollector.toString());
}

/**
 * Declaration of sent messages test.
 * (regression test for bug 6538 searchDeclarationsOf* incorrect)
 */
public void testDeclarationOfSentMessages() throws JavaModelException, CoreException {
	IMethod method = 
		getCompilationUnit("JavaSearch", "src", "a5", "B.java").
			getType("C").getMethod("i", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().searchDeclarationsOfSentMessages(
		getWorkspace(), 
		method,
		resultCollector
	);
	assertEquals(
		"", 
		resultCollector.toString());
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
		new SearchEngine().search(
			workspace, 
			"X",
			TYPE,
			DECLARATIONS,
			scope,
			resultCollector);
		assertEquals(
			externalJar.getCanonicalPath()+ " p0.X",
			resultCollector.toString());
			
		IClassFile classFile = pkg.getClassFile("X.class");
		scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {classFile});
		resultCollector = new JavaSearchResultCollector();
		new SearchEngine().search(
			workspace, 
			classFile.getType(),
			DECLARATIONS,
			scope,
			resultCollector);
		assertEquals(
			externalJar.getCanonicalPath()+ " p0.X",
			resultCollector.toString());
		
	} finally {
		externalJar.delete();
		project.setRawClasspath(classpath, null);
	}
	
}


/**
 * Field declaration with array type test.
 * (regression test for PR 1GKEG73: ITPJCORE:WIN2000 - search (136): missing field declaration)
 */
public void testFieldDeclarationArrayType() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "B.java").getType("B");
	IField field = type.getField("open");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/B.java B.open [open]", 
		resultCollector.toString());
}
/**
 * Field declaration in jar file test.
 */
public void testFieldDeclarationInJar() throws JavaModelException, CoreException {
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p1", "A.class").getType();
	IField field = type.getField("field");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"MyJar.jar p1.A.field [No source]", 
		resultCollector.toString());
}
/**
 * Field declaration with wild card test.
 * (regression test for bug 21763 Problem in Java search [search]  )
 */
public void testFieldDeclarationWithWildCard() throws JavaModelException, CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"class*path",
		FIELD,
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/c5/Test.java c5.Test.class_path [class_path]", 
		resultCollector.toString());
}
/**
 * Multiple field references in one ast test.
 * (regression test for PR 1GD79XM: ITPJCORE:WINNT - Search - search for field references - not all found)
 */
public void testMultipleFieldReference() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p5", "A.java").getType("A");
	IField field = type.getField("x");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/p5/A.java p5.A.k() -> void [x]\n" +
		"src/p5/A.java p5.A.k() -> void [x]\n" +
		"src/p5/A.java p5.A.k() -> void [x]", 
		resultCollector.toString());
}
/**
 * Field reference in inner class test.
 * (regression test for PR 1GL11J6: ITPJCORE:WIN2000 - search: missing field references (nested types))
 */
public void testFieldReferenceInInnerClass() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "O.java").getType("O");
	IField field = type.getField("y");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/O.java O$I.y() -> void [y]", 
		resultCollector.toString());
}
/**
 * Field reference test.
 * (regression test for bug #3433 search: missing field occurrecnces (1GKZ8J6))
 */
public void testFieldReference() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p8", "A.java").getType("A");
	IField field = type.getField("g");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/p8/A.java p8.A.m() -> void [g]\n" +
		"src/p8/A.java p8.B.m() -> void [g]", 
		resultCollector.toString());
}
/**
 * Field reference test.
 * (regression test for PR 1GK8TXE: ITPJCORE:WIN2000 - search: missing field reference)
 */
public void testFieldReference2() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p9", "X.java").getType("X");
	IField field = type.getField("f");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/p9/X.java p9.X.m() -> void [f]", 
		resultCollector.toString());
}
/**
 * Field reference test.
 * (regression test for bug 5821 Refactor > Rename renames local variable instead of member in case of name clash  )
 */
public void testFieldReference3() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "q8", "EclipseTest.java").getType("EclipseTest");
	IField field = type.getField("test");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/q8/EclipseTest.java q8.EclipseTest.main(String[]) -> void [test]", 
		resultCollector.toString());
}
/**
 * Field reference test.
 * (regression test for bug 5923 Search for "length" field refs finds [].length)
 */
public void testFieldReference4() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "a2", "X.java").getType("X");
	IField field = type.getField("length");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/a2/X.java a2.X.foo() -> void [length]", 
		resultCollector.toString());
}
/**
 * Field reference test.
 * (regression test for bug 7987 Field reference search should do lookup in 1.4 mode)
 */
public void testFieldReference5() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "b1", "A.java").getType("A");
	IField field = type.getField("x");
	
	// Set 1.4 compliance level (no constant yet)
	Hashtable options = JavaCore.getOptions();
	String currentOption = (String)options.get("org.eclipse.jdt.core.compiler.compliance");
	options.put("org.eclipse.jdt.core.compiler.compliance", "1.4");
	JavaCore.setOptions(options);
	
	try {
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		new SearchEngine().search(
			getWorkspace(), 
			field, 
			REFERENCES, 
			getJavaSearchScope(), 
			resultCollector);
		assertEquals(
			"src/b1/B.java b1.B.foo() -> void [x]", 
			resultCollector.toString());
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
public void testFieldReference6() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "c4", "X.java").getType("X");
	IField field = type.getField("x");
	
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/c4/X.java c4.X.foo() -> int [x]", 
		resultCollector.toString());
}
/**
 * Field reference in anonymous class test.
 * (regression test for PR 1GL12XE: ITPJCORE:WIN2000 - search: missing field references in inner class)
 */
public void testFieldReferenceInAnonymousClass() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "D.java").getType("D");
	IField field = type.getField("h");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/D.java D.g() -> void [h]", 
		resultCollector.toString());
}
/**
 * Field reference in brackets test.
 * (regression test for bug 23329 search: incorrect range for type references in brackets)
 */
public void testFieldReferenceInBrackets() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "s3", "A.java").getType("A");
	IField field = type.getField("field");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/s3/A.java s3.A.bar() -> int [field]",
		resultCollector.toString());
}
/**
 * Field reference through subclass test.
 * (regression test for PR 1GKB9YH: ITPJCORE:WIN2000 - search for field refs - incorrect results)
 */
public void testFieldReferenceThroughSubclass() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p6", "A.java").getType("A");
	IField field = type.getField("f");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/p6/A.java p6.A.m() -> void [f]\n" +
		"src/p6/A.java p6.B.m() -> void [f]\n" +
		"src/p6/A.java p6.B.m() -> void [f]", 
		resultCollector.toString());
		
	type = getCompilationUnit("JavaSearch", "src", "p6", "A.java").getType("AA");
	field = type.getField("f");
	resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/p6/A.java p6.B.m() -> void [f]", 
		resultCollector.toString());
		
}
/**
 * Hierarchy scope test.
 * (regression test for bug 3445 search: type hierarchy scope incorrect (1GLC8VS))
 */
public void testHierarchyScope() throws JavaModelException, CoreException {
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
public void testInnacurateTypeReference1() throws JavaModelException, CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"Zork", 
		TYPE,
		REFERENCES, 
		SearchEngine.createJavaSearchScope(new IJavaElement[] {
			getPackageFragment("JavaSearch", "src", "b5")
		}), 
		resultCollector);
	assertEquals(
		"src/b5/A.java [Zork]\n" +
		"src/b5/A.java b5.A.{} [Zork]\n" +
		"src/b5/A.java b5.A.{} [Zork]\n" +
		"src/b5/A.java b5.A.{} [Zork]",
		resultCollector.toString());
}
/**
 * Type reference test.
 * (Regression test for bug 9642 Search - missing inaccurate type matches)
 */
public void testInnacurateTypeReference2() throws JavaModelException, CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"p.Zork", 
		TYPE,
		REFERENCES, 
		SearchEngine.createJavaSearchScope(new IJavaElement[] {
			getPackageFragment("JavaSearch", "src", "b5")
		}), 
		resultCollector);
	assertEquals(
		"src/b5/A.java b5.A.{} [Zork]\n" +
		"src/b5/A.java b5.A.{} [Zork]\n" +
		"src/b5/A.java b5.A.{} [Zork]",
		resultCollector.toString());
}
/**
 * Type reference test.
 * (Regression test for bug 21485  NPE when doing a reference search to a package)
 */
public void testInnacurateTypeReference3() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "r3", "A21485.java").getType("A21485");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/r4/B21485.java [A21485] POTENTIAL_MATCH\n" +
		"src/r4/B21485.java r4.B21485 [A21485] POTENTIAL_MATCH",
		resultCollector.toString());
}


/**
 * Inner method declaration test.
 */
public void testInnerMethodDeclaration() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X").getType("Inner");
	IMethod method = type.getMethod("foo", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		DECLARATIONS, 
		getJavaSearchScope(),  
		resultCollector);
	assertEquals(
		"src/p/X.java p.X$Inner.foo() -> String [foo]", 
		resultCollector.toString());
}
/**
 * Inner method reference test.
 */
public void testInnerMethodReference() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X").getType("Inner");
	IMethod method = type.getMethod("foo", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		REFERENCES, 
		getJavaSearchScope(),  
		resultCollector);
	assertEquals(
		"src/p/A.java p.A.foo(int, String, X) -> void [foo()]", 
		resultCollector.toString());
}
/**
 * Interface implementors test.
 */
public void testInterfaceImplementors() throws JavaModelException, CoreException {
	// implementors of an interface
	IType type = getCompilationUnit("JavaSearch", "src", "p", "I.java").getType("I");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		IMPLEMENTORS, 
		getJavaSearchScope(),  
		resultCollector);
	assertEquals(
		"src/InterfaceImplementors.java InterfaceImplementors [p.I]\n" +
		"src/p/X.java p.X [I]", 
		resultCollector.toString());

	// implementors of a class should give no match
	// (regression test for 1G5HBQA: ITPJUI:WINNT - Search - search for implementors of a class finds subclasses)
	type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		IMPLEMENTORS, 
		getJavaSearchScope(),  
		resultCollector);
	assertEquals(
		"", 
		resultCollector.toString());	
}
/**
 * Interface implementors test.
 * (regression test for bug 22102 Not all implementors found for IPartListener)
 */
public void testInterfaceImplementors2() throws JavaModelException, CoreException {
	// implementors of an interface
	IType type = getCompilationUnit("JavaSearch", "src", "r2", "I.java").getType("I");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		IMPLEMENTORS, 
		getJavaSearchScope(),  
		resultCollector);
	assertEquals(
		"src/r2/X.java r2.X.field [I]", 
		resultCollector.toString());
}
/**
 * Long declaration (>255) test.
 * (regression test for bug 25859 Error doing Java Search)
 */
public void testLongDeclaration() throws JavaModelException, CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"AbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyz", 
		TYPE,
		DECLARATIONS, 
		getJavaSearchScope(),  
		resultCollector);
	assertEquals(
		"src/c9/X.java c9.AbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyz [AbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyz]", 
		resultCollector.toString());
}
/**
 * Memeber type declaration test.
 * (regression test for bug 9992 Member class declaration not found)
 */
public void testMemberTypeDeclaration() throws JavaModelException, CoreException {
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[]{
		this.getPackageFragment("JavaSearch", "src", "b4")
	});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"*.A.B",
		TYPE,
		DECLARATIONS, 
		scope, 
		resultCollector);
	assertEquals(
		"src/b4/A.java b4.A$B [B]", 
		resultCollector.toString());
}
/**
 * Member type reference test.
 */
public void testMemberTypeReference() throws JavaModelException, CoreException {
	// references to second level member type
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	new SearchEngine().search(
		getWorkspace(), 
		"BMember",
		TYPE,
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"References to type BMember",
		"src/MemberTypeReference/Azz.java MemberTypeReference.Azz$AzzMember [BMember] EXACT_MATCH\n" +
		"src/MemberTypeReference/Azz.java MemberTypeReference.Azz.poo() -> void [B.BMember] EXACT_MATCH\n" +
		"src/MemberTypeReference/Azz.java MemberTypeReference.X.val [Azz.AzzMember.BMember] EXACT_MATCH\n" +
		"src/MemberTypeReference/B.java MemberTypeReference.B.foo() -> void [Azz.AzzMember.BMember] EXACT_MATCH",
		resultCollector.toString());

	// references to first level member type
	resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	new SearchEngine().search(
		getWorkspace(), 
		"AzzMember",
		TYPE,
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"References to type AzzMember",
		"src/MemberTypeReference/Azz.java MemberTypeReference.X.val [Azz.AzzMember] EXACT_MATCH\n" +
		"src/MemberTypeReference/B.java MemberTypeReference.B.foo() -> void [Azz.AzzMember] EXACT_MATCH",
		resultCollector.toString());

	// no reference to a field with same name as member type
	resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	new SearchEngine().search(
		getWorkspace(), 
		"BMember",
		FIELD,
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"References to field BMember",
		"",
		resultCollector.toString());
}
/**
 * Member type reference test.
 * (regression test for PR 1GL0MN9: ITPJCORE:WIN2000 - search: not consistent results for nested types)
 */
public void testMemberTypeReference2() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "a", "A.java").getType("A").getType("X");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/a/A.java a.B.ax [A.X]\n" +
		"src/a/A.java a.B.sx [S.X]", 
		resultCollector.toString());
}
/**
 * Method declaration in hierarchy test.
 */
public void testMethodDeclarationInHierarchyScope1() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"foo",
		METHOD,
		DECLARATIONS, 
		SearchEngine.createHierarchyScope(type), 
		resultCollector);
	assertEquals(
		"src/p/X.java p.X.foo(int, String, X) -> void [foo]\n" +
		"src/p/Z.java p.Z.foo(int, String, X) -> void [foo]", 
		resultCollector.toString());
}
/**
 * Method declaration in hierarchy that contains elements in external jar.
 * (regression test for PR #1G2E4F1)
 */
public void testMethodDeclarationInHierarchyScope2() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[] {"I", "QString;", "QX;"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method,
		DECLARATIONS, 
		SearchEngine.createHierarchyScope(type), // java.lang.Object is in external jcl
		resultCollector);
	assertEquals(
		"src/p/X.java p.X.foo(int, String, X) -> void [foo]\n" +
		"src/p/Z.java p.Z.foo(int, String, X) -> void [foo]", 
		resultCollector.toString());
}
/**
 * Method declaration in hierarchy on a secondary type.
 */
public void testMethodDeclarationInHierarchyScope3() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "d3", "A.java").getType("B");
	IMethod method = type.getMethod("foo", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method,
		DECLARATIONS, 
		SearchEngine.createHierarchyScope(type),
		resultCollector);
	assertEquals(
		"src/d3/A.java d3.B.foo() -> void [foo]", 
		resultCollector.toString());
}
/**
 * Method declaration in field initialzer.
 * (regression test for bug 24346 Method declaration not found in field initializer  )
 */
public void testMethodDeclarationInInitializer() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"foo24346",
		METHOD,
		DECLARATIONS, 
		getJavaSearchScope(),
		resultCollector);
	assertEquals(
		"src/c6/X.java c6.X.x [foo24346]", 
		resultCollector.toString());
}
/**
 * Method declaration in jar file test.
 */
public void testMethodDeclarationInJar() throws JavaModelException, CoreException {
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p1", "A.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"Ljava.lang.String;"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	// System.out.println(displayString(resultCollector.toString(), 2));
	assertEquals(
		"MyJar.jar p1.A.foo(java.lang.String) -> boolean [No source]", 
		resultCollector.toString());
}
/**
 * Method declaration in package test.
 * (regression tets for PR #1G2KA97)
 */
public void testMethodDeclarationInPackageScope() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment()});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"main(String[])",
		METHOD,
		DECLARATIONS, 
		scope, 
		resultCollector);
	assertEquals(
		"src/p/A.java p.A.main(String[]) -> void [main]", 
		resultCollector.toString());
}
/**
 * Type declaration using a package scope test.
 * (check that subpackages are not included)
 */
public void testTypeDeclarationInPackageScope() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p3", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment()});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"X",
		TYPE,
		DECLARATIONS, 
		scope, 
		resultCollector);
	assertEquals(
		"src/p3/X.java p3.X [X]", 
		resultCollector.toString());
}
/**
 * Type declaration using a binary package scope test.
 * (check that subpackages are not included)
 */
public void testTypeDeclarationInPackageScope2() throws JavaModelException, CoreException {
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p0", "X.class").getType();
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getPackageFragment()});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getJavaProject("JavaSearch").getProject().getWorkspace(), 
		"X",
		TYPE,
		DECLARATIONS, 
		scope, 
		resultCollector);
	assertEquals(
		"MyJar.jar p0.X [No source]", 
		resultCollector.toString());
}
/**
 * Method reference through super test.
 */
public void testMethodReferenceThroughSuper() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "sd", "AQ.java").getType("AQ");
	IMethod method = type.getMethod("k", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/sd/AQ.java sd.AQE.k() -> void [k()]", 
		resultCollector.toString());
}
/**
 * Method reference test.
 * (regression test for bug 5068 search: missing method reference)
 */
public void testMethodReference1() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "q5", "AQ.java").getType("I");
	IMethod method = type.getMethod("k", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/q5/AQ.java q5.T.m() -> void [k()]", 
		resultCollector.toString());
}
/**
 * Method reference test.
 * (regression test for bug 5069 search: method reference in super missing)
 */
public void testMethodReference2() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "q6", "CD.java").getType("AQ");
	IMethod method = type.getMethod("k", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/q6/CD.java q6.AQE.k() -> void [k()]", 
		resultCollector.toString());
}
/**
 * Method reference test.
 * (regression test for bug 5070 search: missing interface method reference  )
 */
public void testMethodReference3() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "q7", "AQ.java").getType("I");
	IMethod method = type.getMethod("k", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/q7/AQ.java q7.D.h() -> void [k()]", 
		resultCollector.toString());
}
/**
 * Method reference test.
 * (regression test for bug 8928 Unable to find references or declarations of methods that use static inner classes in the signature)
 */
public void testMethodReference4() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "b2", "Y.java").getType("Y");
	IMethod method = type.getMethod("foo", new String[] {"QX.Inner;"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/b2/Z.java b2.Z.bar() -> void [foo(inner)]", 
		resultCollector.toString());
}
/**
 * Method reference through array test.
 * (regression test for 1GHDA2V: ITPJCORE:WINNT - ClassCastException when doing a search)
 */
public void testMethodReferenceThroughArray() throws JavaModelException, CoreException {
	IType type = getClassFile("JavaSearch", getExternalJCLPath(), "java.lang", "Object.class").getType();
	IMethod method = type.getMethod("clone", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/E.java E.foo() -> Object [clone()]", 
		resultCollector.toString());
}
/**
 * Method reference in inner class test.
 */
public void testMethodReferenceInInnerClass() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "CA.java").getType("CA");
	IMethod method = type.getMethod("m", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/CA.java CA$CB.f() -> void [m()]\n" +
		"src/CA.java CA$CB$CC.f() -> void [m()]", 
		resultCollector.toString());
}
/**
 * Method reference in anonymous class test.
 * (regression test for PR 1GGNOTF: ITPJCORE:WINNT - Search doesn't find method referenced in anonymous inner class)
 */
public void testMethodReferenceInAnonymousClass() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "PR_1GGNOTF.java").getType("PR_1GGNOTF");
	IMethod method = type.getMethod("method", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/PR_1GGNOTF.java PR_1GGNOTF.method2() -> void [method()]", 
		resultCollector.toString());
}
/**
 * Member type named "Object" reference test.
 * (regression test for 1G4GHPS: ITPJUI:WINNT - Strange error message in search)
 */
public void testObjectMemberTypeReference() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "ObjectMemberTypeReference", "A.java")
		.getType("A")
		.getType("Object");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/ObjectMemberTypeReference/A.java ObjectMemberTypeReference.A.foo() -> void [Object] EXACT_MATCH",
		resultCollector.toString());
}
/**
 * OrPattern test.
 * (regression test for bug 5862 search : too many matches on search with OrPattern)
 */
public void testOrPattern() throws CoreException {
	IMethod leftMethod = getCompilationUnit("JavaSearch", "src", "q9", "I.java")
		.getType("I").getMethod("m", new String[] {});
	ISearchPattern leftPattern = SearchEngine.createSearchPattern(leftMethod, ALL_OCCURRENCES);
	IMethod rightMethod = getCompilationUnit("JavaSearch", "src", "q9", "I.java")
		.getType("A1").getMethod("m", new String[] {});
	ISearchPattern rightPattern = SearchEngine.createSearchPattern(rightMethod, ALL_OCCURRENCES);
	ISearchPattern orPattern = SearchEngine.createOrSearchPattern(leftPattern, rightPattern);
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	new SearchEngine().search(
		getWorkspace(), 
		orPattern,
		getJavaSearchScope(),
		resultCollector);
	assertEquals(
		"src/q9/I.java q9.I.m() -> void [m] EXACT_MATCH\n" +
		"src/q9/I.java q9.A1.m() -> void [m] EXACT_MATCH",
		resultCollector.toString());
}
/**
 * Package reference test.
 * (regression test for PR 1GK90H4: ITPJCORE:WIN2000 - search: missing package reference)
 */
public void testPackageReference1() throws JavaModelException, CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "q2");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		pkg, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/q1/B.java q1.B.m(AA) -> void [q2]", 
		resultCollector.toString());
}
/**
 * Package reference test.
 * (regression test for bug 17906 Rename package fails when inner classes are imported)
 */
public void testPackageReference2() throws JavaModelException, CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "b8");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		pkg, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/b9/Foo.java [b8]", 
		resultCollector.toString());
}

/**
 * Test pattern match package references
 */
public void testPatternMatchPackageReference() throws JavaModelException, CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"*p2.*",
		PACKAGE,
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/PackageReference/A.java [p3.p2.p]\n" +
		"src/PackageReference/B.java [p3.p2.p]\n" +
		"src/PackageReference/C.java PackageReference.C [p3.p2.p]\n" +
		"src/PackageReference/D.java PackageReference.D.x [p3.p2.p]\n" +
		"src/PackageReference/E.java PackageReference.E.x [p3.p2.p]\n" +
		"src/PackageReference/F.java PackageReference.F.foo() -> p3.p2.p.X [p3.p2.p]\n" +
		"src/PackageReference/G.java PackageReference.G.foo(p3.p2.p.X) -> void [p3.p2.p]\n" +
		"src/PackageReference/H.java PackageReference.H.foo() -> void [p3.p2.p]\n" +
		"src/PackageReference/I.java PackageReference.I.foo() -> void [p3.p2.p]\n" +
		"src/PackageReference/J.java PackageReference.J.foo() -> void [p3.p2.p]", 
		resultCollector.toString());
}
/**
 * Test pattern match type declaration
 * (regression test for bug 17210 No match found when query contains '?')
 */
public void testPatternMatchTypeDeclaration() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"X?Z",
		TYPE,
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals("src/r5/XYZ.java r5.XYZ [XYZ]", resultCollector.toString());
}
/**
 * Test pattern match type reference in binary
 * (regression test for bug 24741 Search does not find patterned type reference in binary project  )
 */
public void testPatternMatchTypeReference() throws CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"p24741.*",
		TYPE,
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"test24741.jar q24741.B", 
		resultCollector.toString());
}
/**
 * Test that we find potential matches in binaries even if we can't resolve the entire
 * class file.
 * (Regression test for 1G4IN3E: ITPJCORE:WINNT - AbortCompilation using J9 to search for class declaration) 
 */
public void testPotentialMatchInBinary() throws JavaModelException, CoreException {
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
		new SearchEngine().search(
			getWorkspace(),
			"MissingFieldType.*",
			FIELD, 
			DECLARATIONS, 
			getJavaSearchScope(), 
			resultCollector);
		assertEquals(
			"AbortCompilation.jar AbortCompilation.MissingFieldType.field [No source] POTENTIAL_MATCH\n" +
			"AbortCompilation.jar AbortCompilation.MissingFieldType.missing [No source] POTENTIAL_MATCH\n" +
			"AbortCompilation.jar AbortCompilation.MissingFieldType.otherField [No source] POTENTIAL_MATCH", 
			resultCollector.toString());
	
		// potential match for a method declaration
		resultCollector = new JavaSearchResultCollector();
		resultCollector.showAccuracy = true;
		new SearchEngine().search(
			getWorkspace(),
			"MissingArgumentType.foo*",
			METHOD, 
			DECLARATIONS, 
			getJavaSearchScope(), 
			resultCollector);
		assertEquals(
			"AbortCompilation.jar AbortCompilation.MissingArgumentType.foo() -> void [No source] POTENTIAL_MATCH\n" +
			"AbortCompilation.jar AbortCompilation.MissingArgumentType.foo(java.util.EventListener) -> void [No source] POTENTIAL_MATCH\n" +
			"AbortCompilation.jar AbortCompilation.MissingArgumentType.foo2() -> void [No source] POTENTIAL_MATCH", 
			resultCollector.toString());
	
		// potential match for a type declaration
		resultCollector = new JavaSearchResultCollector();
		resultCollector.showAccuracy = true;
		new SearchEngine().search(
			getWorkspace(),
			"Missing*",
			TYPE, 
			DECLARATIONS, 
			getJavaSearchScope(), 
			resultCollector);
		assertEquals(
			"AbortCompilation.jar AbortCompilation.EnclosingType$MissingEnclosingType [No source] EXACT_MATCH\n" +
			"AbortCompilation.jar AbortCompilation.MissingArgumentType [No source] EXACT_MATCH\n" +
			"AbortCompilation.jar AbortCompilation.MissingFieldType [No source] EXACT_MATCH", 
			resultCollector.toString());
	} finally {
		// reset classpath
		project.setRawClasspath(classpath, null);
	}
}
/**
 * Read and write access reference in compound expression test.
 * (regression test for bug 6158 Search - Prefix and postfix expression not found as write reference)
 */
public void testReadWriteFieldReferenceInCompoundExpression() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "a4", "X.java").getType("X");
	IField field = type.getField("field");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	
	// Read reference
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		READ_ACCESSES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/a4/X.java a4.X.foo() -> void [field]", 
		resultCollector.toString());
		
	// Write reference
	resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		WRITE_ACCESSES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/a4/X.java a4.X.foo() -> void [field]", 
		resultCollector.toString());
		
}
/**
 * Simple constructor declaration test.
 */
public void testSimpleConstructorDeclaration() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IMethod constructor = type.getMethod("A", new String[] {"QX;"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		constructor, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals("src/p/A.java p.A(X) [A]", resultCollector.toString());
}
/**
 * Simple constructor reference test.
 */
public void testSimpleConstructorReference1() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IMethod constructor = type.getMethod("A", new String[] {"QX;"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		constructor, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals("src/Test.java Test.main(String[]) -> void [new p.A(y)]", resultCollector.toString());
}
/**
 * Simple constructor reference test.
 */
public void testSimpleConstructorReference2() throws JavaModelException, CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"p.A(X)",
		CONSTRUCTOR,
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals("src/Test.java Test.main(String[]) -> void [new p.A(y)]", resultCollector.toString());
}
/**
 * Simple declarations of sent messages test.
 */
public void testSimpleDeclarationsOfSentMessages() throws JavaModelException, CoreException {
	ICompilationUnit cu = getCompilationUnit("JavaSearch", "src", "", "Test.java");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().searchDeclarationsOfSentMessages(
		getWorkspace(), 
		cu, 
		resultCollector);
	assertEquals(
		"src/p/X.java p.X.foo(int, String, X) -> void [foo(int i, String s, X x)]\n" +
		"src/p/Y.java p.Y.bar() -> void [bar()]\n" +
		"src/p/Z.java p.Z.foo(int, String, X) -> void [foo(int i, String s, X x)]\n" +
		"src/p/A.java p.A.foo(int, String, X) -> void [foo()]", 
		resultCollector.toString());
}
/**
 * Simple field declaration test.
 */
public void testSimpleFieldDeclaration() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("x");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/p/A.java p.A.x [x]", 
		resultCollector.toString());
}
/**
 * Simple field reference test.
 */
public void testSimpleFieldReference() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("x");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/Test.java Test.main(String[]) -> void [x]\n" +
		"src/p/A.java p.A(X) [x]", 
		resultCollector.toString());
}
/**
 * Simple write field access reference test.
 */
public void testSimpleWriteFieldReference() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("x");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		WRITE_ACCESSES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/p/A.java p.A(X) [x]", 
		resultCollector.toString());
}
/**
 * Sub-cu java search scope test.
 * (regression test for bug 9041 search: cannot create a sub-cu scope)
 */
public void testSubCUSearchScope1() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "b3", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type,
		REFERENCES, 
		scope, 
		resultCollector);
	assertEquals(
		"src/b3/X.java b3.X.field [X]\n" +
		"src/b3/X.java b3.X.foo() -> Object [X]\n" +
		"src/b3/X.java b3.X$Y.field2 [X]\n" +
		"src/b3/X.java b3.X$Y.foo2() -> Object [X]", 
		resultCollector.toString());
}
/**
 * Sub-cu java search scope test.
 * (regression test for bug 9041 search: cannot create a sub-cu scope)
 */
public void testSubCUSearchScope2() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "b3", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getField("field")});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type,
		REFERENCES, 
		scope, 
		resultCollector);
	assertEquals(
		"src/b3/X.java b3.X.field [X]", 
		resultCollector.toString());
}
/**
 * Sub-cu java search scope test.
 * (regression test for bug 9041 search: cannot create a sub-cu scope)
 */
public void testSubCUSearchScope3() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "b3", "X.java").getType("X");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {type.getType("Y")});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type,
		REFERENCES, 
		scope, 
		resultCollector);
	assertEquals(
		"src/b3/X.java b3.X$Y.field2 [X]\n" +
		"src/b3/X.java b3.X$Y.foo2() -> Object [X]", 
		resultCollector.toString());
}
/**
 * Type declaration test.
 * (regression test for bug 29524 Search for declaration via patterns adds '"*")
 */
public void testTypeDeclaration() throws JavaModelException, CoreException {
	IPackageFragment pkg = this.getPackageFragment("JavaSearch", "src", "d8");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"A",
		TYPE,
		DECLARATIONS, 
		scope, 
		resultCollector);
	assertEquals("src/d8/A.java d8.A [A]", resultCollector.toString());
}
/**
 * Simple method declaration test.
 */
public void testSimpleMethodDeclaration() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[] {"I", "QString;", "QX;"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/p/X.java p.X.foo(int, String, X) -> void [foo]\n" +
		"src/p/Z.java p.Z.foo(int, String, X) -> void [foo]", 
		resultCollector.toString());
}
/**
 * Simple method reference test.
 */
public void testSimpleMethodReference() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("foo", new String[] {"I", "QString;", "QX;"});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/Test.java Test.main(String[]) -> void [foo(1, \"a\", y)]\n" +
		"src/Test.java Test.main(String[]) -> void [foo(1, \"a\", z)]\n" +
		"src/p/Z.java p.Z.foo(int, String, X) -> void [foo(i, s, new Y(true))]", 
		resultCollector.toString());
}
/**
 * Simple package declaration test.
 */
public void testSimplePackageDeclaration() throws JavaModelException, CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "p");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		pkg, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/p p", 
		resultCollector.toString());
}
/**
 * Simple package reference test.
 */
public void testSimplePackageReference() throws JavaModelException, CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "p");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		pkg, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/InterfaceImplementors.java InterfaceImplementors [p]\n" +
		"src/Test.java Test.main(String[]) -> void [p]\n" +
		"src/Test.java Test.main(String[]) -> void [p]\n" +
		"src/Test.java Test.main(String[]) -> void [p]\n" +
		"src/Test.java Test.main(String[]) -> void [p]\n" +
		"src/Test.java Test.main(String[]) -> void [p]\n" +
		"src/Test.java Test.main(String[]) -> void [p]\n" +
		"src/Test.java Test.main(String[]) -> void [p]\n" +
		"src/TypeReferenceInImport/X.java [p]", 
		resultCollector.toString());
}
/**
 * Simple field read access reference test.
 */
public void testSimpleReadFieldReference() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("x");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		READ_ACCESSES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/Test.java Test.main(String[]) -> void [x]", 
		resultCollector.toString());
}
/**
 * Simple type declaration test.
 */
public void testSimpleTypeDeclaration() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type,
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals("src/p/X.java p.X [X]", resultCollector.toString());
}
/**
 * Simple type reference test.
 */
public void testSimpleTypeReference() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/p/A.java p.A.x [X]\n" +
		"src/p/A.java p.A(X) [X]\n" +
		"src/p/A.java p.A.foo(int, String, X) -> void [X]\n" +
		"src/p/X.java p.X() [X]\n" +
		"src/p/X.java p.X.foo(int, String, X) -> void [X]\n" +
		"src/p/Y.java p.Y [X]\n" +
		"src/p/Z.java p.Z.foo(int, String, X) -> void [X]", 
		resultCollector.toString());
}
/**
 * Static field reference test.
 * (regression test for PR #1G2P5EP)
 */
public void testStaticFieldReference() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	IField field = type.getField("DEBUG");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/p/A.java p.A.foo() -> void [DEBUG]", 
		resultCollector.toString());
}
/**
 * Static method reference test.
 */
public void testStaticMethodReference1() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "Y.java").getType("Y");
	IMethod method = type.getMethod("bar", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/Test.java Test.main(String[]) -> void [bar()]", 
		resultCollector.toString());
}
/**
 * Static method reference test.
 */
public void testStaticMethodReference2() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	IMethod method = type.getMethod("bar", new String[] {});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		method, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"", 
		resultCollector.toString());
}
/**
 * Type declaration in jar file test.
 */
public void testTypeDeclarationInJar() throws JavaModelException, CoreException {
	IType type = getClassFile("JavaSearch", "MyJar.jar", "p1", "A.class").getType();
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"MyJar.jar p1.A [No source]", 
		resultCollector.toString());
}
/**
 * Type declaration in jar file and in anonymous class test.
 * (regression test for 20631 Declaration of local binary type not found)
 */
public void testTypeDeclarationInJar2() throws JavaModelException, CoreException {
	IPackageFragmentRoot root = getPackageFragmentRoot("JavaSearch", "test20631.jar");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {root});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"Y",
		TYPE, 
		DECLARATIONS, 
		scope, 
		resultCollector);
	assertEquals(
		"test20631.jar X$1$Y", 
		resultCollector.toString());
}
/**
 * Type declaration in external jar file that is shared by 2 projects.
 * (regression test for bug 27485 SearchEngine returns wrong java element when searching in an archive that is included by two distinct java projects.)
 */
public void testTypeDeclarationInJar3() throws CoreException {
	try {
		IJavaProject p1 = this.createJavaProject("P1", new String[] {}, new String[] {"JCL_LIB"}, "");
		IJavaProject p2 = this.createJavaProject("P2", new String[] {}, new String[] {"JCL_LIB"}, "");
		
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {p1});
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject = true;
		new SearchEngine().search(
			getWorkspace(), 
			"Object",
			TYPE, 
			DECLARATIONS, 
			scope, 
			resultCollector);
		assertEquals(
			"Unexpected result in scope of P1",
			getExternalJCLPath() + " [in P1] java.lang.Object", 
			resultCollector.toString());
			
		scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {p2});
		resultCollector = new JavaSearchResultCollector();
		resultCollector.showProject = true;
		new SearchEngine().search(
			getWorkspace(), 
			"Object",
			TYPE, 
			DECLARATIONS, 
			scope, 
			resultCollector);
		assertEquals(
			"Unexpected result in scope of P2",
			getExternalJCLPath() + " [in P2] java.lang.Object", 
			resultCollector.toString());
		} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
	}
}
/**
 * Type ocurrence test.
 * (regression test for PR 1GKAQJS: ITPJCORE:WIN2000 - search: incorrect results for nested types)
 */
public void testTypeOccurence() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "r", "A.java").getType("A").getType("X");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		ALL_OCCURRENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/r/A.java r.A.m() -> A [X]\n" +
		"src/r/A.java r.A$X [X]\n" +
		"src/r/A.java r.A$X(X) [X]\n" +
		"src/r/A.java r.A$X(X) [X]\n" +
		"src/r/A.java r.B.ax [A.X]\n" +
		"src/r/A.java r.B.ax [X]",
		resultCollector.toString());
}
/**
 * Type name with $ ocurrence test.
 * (regression test for bug 3310 Smoke 124: Compile errors introduced with rename refactoring (1GFBK2G))
 */
public void testTypeOccurenceWithDollar() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "q3", "A$B.java").getType("A$B");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		ALL_OCCURRENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/q3/A$B.java q3.A$B [A$B]\n" +
		"src/q4/C.java q4.C.foo() -> Object [q3.A$B]",
		resultCollector.toString());
}
/**
 * Type reference test.
 * (Regression test for PR 1GK7K17: ITPJCORE:WIN2000 - search: missing type reference)
 */
public void testTypeReference1() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "X.java").getType("X");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/X.java AA() [X]",
		resultCollector.toString());
}
/**
 * Type reference test.
 * (Regression test for bug 29516 SearchEngine regressions in 20030114)
 */
public void testTypeReference2() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "d7", "A.java").getType("A");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/d7/A.java d7.A.A [A]\n" +
		"src/d7/A.java d7.A.A(A) -> A [A]\n" +
		"src/d7/A.java d7.A.A(A) -> A [A]",
		resultCollector.toString());
}
/**
 * Type reference as a single name reference test.
 */
public void testTypeReferenceAsSingleNameReference() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "TypeReferenceAsSingleNameReference.java").getType("TypeReferenceAsSingleNameReference");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/TypeReferenceAsSingleNameReference.java TypeReferenceAsSingleNameReference.hasReference() -> void [TypeReferenceAsSingleNameReference]\n" +
		"src/TypeReferenceAsSingleNameReference.java TypeReferenceAsSingleNameReference.hasReference() -> void [TypeReferenceAsSingleNameReference]",
		resultCollector.toString());
}
/**
 * Type reference in array test.
 * (regression test for PR #1GAL424)
 */
public void testTypeReferenceInArray() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "TypeReferenceInArray", "A.java").getType("A");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/TypeReferenceInArray/A.java TypeReferenceInArray.A.a [A]\n" +
		"src/TypeReferenceInArray/A.java TypeReferenceInArray.A.b [TypeReferenceInArray.A]",
		resultCollector.toString());
}
/**
 * Type reference in array test.
 * (regression test for bug 3230 Search - Too many type references for query ending with * (1GAZVGI)  )
 */
public void testTypeReferenceInArray2() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "s1", "X.java").getType("X");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/s1/Y.java s1.Y.f [X]",
		resultCollector.toString());
}
/**
 * Type reference in cast test.
 * (regression test for bug 23329 search: incorrect range for type references in brackets)
 */
public void testTypeReferenceInCast() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "s3", "A.java").getType("B");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/s3/A.java s3.A.foo() -> Object [B]",
		resultCollector.toString());
}
/**
 * Type reference in hierarchy test.
 * (regression test for bug 28236 Search for refs to class in hierarchy matches class outside hierarchy )
 */
public void testTypeReferenceInHierarchy() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "d9.p1", "A.java").getType("A");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	IJavaSearchScope scope = SearchEngine.createHierarchyScope(type);
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		scope, 
		resultCollector);
	assertEquals(
		"",
		resultCollector.toString());
}
/**
 * Type reference in import test.
 * (regression test for PR #1GA7PAS)
 */
public void testTypeReferenceInImport() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p2", "Z.java").getType("Z");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/TypeReferenceInImport/X.java [p2.Z]",
		resultCollector.toString());
}
/**
 * Type reference in import test.
 * (regression test for bug 23077 search: does not find type references in some imports)
 */
public void testTypeReferenceInImport2() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "r6", "A.java").getType("A");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]\n" +
		"src/r6/B.java [r6.A]",
		resultCollector.toString());
}
/**
 * Type reference in initializer test.
 * (regression test for PR #1G4GO4O)
 */
public void testTypeReferenceInInitializer() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "Test.java").getType("Test");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/Test.java Test.static {} [Test]\n" +
		"src/Test.java Test.static {} [Test]\n" +
		"src/Test.java Test.{} [Test]",
		resultCollector.toString());
}
/**
 * Type reference inside a qualified name reference test.
 * (Regression test for PR #1G4TSC0)
 */
public void testTypeReferenceInQualifiedNameReference() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "A.java").getType("A");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/Test.java Test.main(String[]) -> void [p.A]\n" +
		"src/Test.java Test.main(String[]) -> void [p.A]\n" +
		"src/p/A.java p.A.foo() -> void [A]",
		resultCollector.toString());
}
/**
 * Type reference inside a qualified name reference test.
 * (Regression test for PR #1GLBP65)
 */
public void testTypeReferenceInQualifiedNameReference2() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p4", "A.java").getType("A");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/p4/A.java p4.A.A [A]\n" +
		"src/p4/A.java p4.X.x() -> void [p4.A]\n" +
		"src/p4/A.java p4.X [p4.A]",
		resultCollector.toString());
}
/**
 * Type reference inside a qualified name reference test.
 * (Regression test for PR 1GL9UMH: ITPJCORE:WIN2000 - search: missing type occurrences)
 */
public void testTypeReferenceInQualifiedNameReference3() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "", "W.java").getType("W");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/W.java W.m() -> int [W]",
		resultCollector.toString());
}
/**
 * Type reference inside a qualified name reference test.
 * (Regression test for bug 16751 Renaming a class doesn't update all references  )
 */
public void testTypeReferenceInQualifiedNameReference4() throws CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "b7", "X.java").getType("SubClass");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/b7/X.java b7.Test.main(String[]) -> void [SubClass]",
		resultCollector.toString());
}
/**
 * Type reference in a throw clause test.
 * (Regression test for bug 6779 searchDeclarationsOfReferencedTyped - missing exception types)
 */
public void testTypeReferenceInThrows() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "a7", "X.java").getType("MyException");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	resultCollector.showAccuracy = true;
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/a7/X.java a7.X.foo() -> void [MyException] EXACT_MATCH",
		resultCollector.toString());
}
/**
 * Type reference test (not case sensitive)
 */
public void testTypeReferenceNotCaseSensitive() throws JavaModelException, CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "d4");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});
	ISearchPattern pattern = SearchEngine.createSearchPattern("Y", TYPE, REFERENCES, false);
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		pattern, 
		scope,
		resultCollector);
	assertEquals(
		"src/d4/X.java d4.X.foo() -> Object [Y]",
		resultCollector.toString());
}
/**
 * Type reference in a folder that is not in the classpath.
 * (regression test for PR #1G5N8KS)
 */
public void testTypeReferenceNotInClasspath() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p", "X.java").getType("X");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/p/A.java p.A.x [X]\n" +
		"src/p/A.java p.A(X) [X]\n" +
		"src/p/A.java p.A.foo(int, String, X) -> void [X]\n" +
		"src/p/X.java p.X() [X]\n" +
		"src/p/X.java p.X.foo(int, String, X) -> void [X]\n" +
		"src/p/Y.java p.Y [X]\n" +
		"src/p/Z.java p.Z.foo(int, String, X) -> void [X]",
		resultCollector.toString());
}
/**
 * Negative type reference test.
 * (regression test for 1G52F7P: ITPJCORE:WINNT - Search - finds bogus references to class)
 */
public void testNegativeTypeReference() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "p7", "A.java").getType("A");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"",
		resultCollector.toString());
}

/**
 * Various package declarations test.
 */
public void testVariousPackageDeclarations() throws JavaModelException, CoreException {
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"p3*",
		PACKAGE, 
		DECLARATIONS, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/p3 p3\n" +
		"src/p3/p2/p p3.p2.p", 
		resultCollector.toString());
}
/**
 * Various package reference test.
 */
public void testVariousPackageReference() throws JavaModelException, CoreException {
	IPackageFragment pkg = getPackageFragment("JavaSearch", "src", "p3.p2.p");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		pkg, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"src/PackageReference/A.java [p3.p2.p]\n" +
		"src/PackageReference/B.java [p3.p2.p]\n" +
		"src/PackageReference/C.java PackageReference.C [p3.p2.p]\n" +
		"src/PackageReference/D.java PackageReference.D.x [p3.p2.p]\n" +
		"src/PackageReference/E.java PackageReference.E.x [p3.p2.p]\n" +
		"src/PackageReference/F.java PackageReference.F.foo() -> p3.p2.p.X [p3.p2.p]\n" +
		"src/PackageReference/G.java PackageReference.G.foo(p3.p2.p.X) -> void [p3.p2.p]\n" +
		"src/PackageReference/H.java PackageReference.H.foo() -> void [p3.p2.p]\n" +
		"src/PackageReference/I.java PackageReference.I.foo() -> void [p3.p2.p]\n" +
		"src/PackageReference/J.java PackageReference.J.foo() -> void [p3.p2.p]", 
		resultCollector.toString());
}
/**
 * Type reference inside an argument, a return type or a field type.
 * (Regression test for PR #1GA7QA1)
 */
public void testVariousTypeReferences() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "NoReference", "A.java").getType("A");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		type, 
		REFERENCES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"", // no reference should be found
		resultCollector.toString());
}
/**
 * Write access reference in a qualified name reference test.
 * (regression test for bug 7344 Search - write acces give wrong result)
 */
public void testReadWriteAccessInQualifiedNameReference() throws JavaModelException, CoreException {
	IType type = getCompilationUnit("JavaSearch", "src", "a8", "A.java").getType("A");
	IField field = type.getField("a");
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	
	resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		field, 
		WRITE_ACCESSES, 
		getJavaSearchScope(), 
		resultCollector);
	assertEquals(
		"", 
		resultCollector.toString());
		
}
}
