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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.IWorkingCopy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
public class WorkingCopySearchTests extends JavaSearchTests {
	ICompilationUnit workingCopy;
	
public WorkingCopySearchTests(String name) {
	super(name);
}

public static Test suite() {
	TestSuite suite = new Suite(WorkingCopySearchTests.class.getName());
	
	suite.addTest(new WorkingCopySearchTests("testAddNewType"));
	suite.addTest(new WorkingCopySearchTests("testRemoveType"));
	suite.addTest(new WorkingCopySearchTests("testMoveType"));
	suite.addTest(new WorkingCopySearchTests("testHierarchyScopeOnWorkingCopy"));
	suite.addTest(new WorkingCopySearchTests("testDeclarationOfReferencedTypes"));

	return suite;
}
/**
 * Get a new working copy.
 */
protected void setUp() {
	try {
		this.workingCopy = (ICompilationUnit)this.getCompilationUnit("JavaSearch", "src", "wc", "X.java").getWorkingCopy();
	} catch (JavaModelException e) {
		e.printStackTrace();
	}
}
/**
 * Destroy the working copy.
 */
protected void tearDown() throws Exception {
	this.workingCopy.destroy();
	this.workingCopy = null;
}


/**
 * Hierarchy scope on a working copy test.
 */
public void testHierarchyScopeOnWorkingCopy() throws JavaModelException, CoreException {
	ICompilationUnit unit = this. getCompilationUnit("JavaSearch", "src", "a9", "A.java");
	ICompilationUnit workingCopy = (ICompilationUnit)unit.getWorkingCopy();
	try {
		IType type = workingCopy.getType("A");
		IJavaSearchScope scope = SearchEngine.createHierarchyScope(type);
		assertTrue("a9.A should be included in hierarchy scope", scope.encloses(type));
		assertTrue("a9.C should be included in hierarchy scope", scope.encloses(workingCopy.getType("C")));
		assertTrue("a9.B should be included in hierarchy scope", scope.encloses(workingCopy.getType("B")));
		IPath path = unit.getUnderlyingResource().getFullPath();
		assertTrue("a9/A.java should not be included in hierarchy scope", !scope.encloses(path.toString()));
	} finally {
		workingCopy.destroy();
	}
}


/**
 * Type declaration in a working copy test.
 * A new type is added in the working copy only.
 */
public void testAddNewType() throws JavaModelException, CoreException {
	this.workingCopy.createType(
		"class NewType {\n" +
		"}",
		null,
		false,
		null);
	
	IJavaSearchScope scope = 
		SearchEngine.createJavaSearchScope(
			new IJavaElement[] {this.workingCopy.getParent()});
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine(new IWorkingCopy[] {this.workingCopy}).search(
		getWorkspace(), 
		"NewType",
		TYPE,
		DECLARATIONS, 
		scope, 
		resultCollector);
	assertEquals(
		"src/wc/X.java wc.NewType [NewType]", 
		resultCollector.toString());
}
/**
 * Declaration of referenced types test.
 * (Regression test for bug 5355 search: NPE in searchDeclarationsOfReferencedTypes  )
 */
public void testDeclarationOfReferencedTypes() throws JavaModelException, CoreException {
	IMethod method = this.workingCopy.getType("X").createMethod(
		"public void foo() {\n" +
		"  X x = new X();\n" +
		"}",
		null,
		true,
		null);
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine().searchDeclarationsOfReferencedTypes(
		getWorkspace(), 
		method,
		resultCollector
	);
	assertEquals(
		"src/wc/X.java wc.X [X]", 
		resultCollector.toString());
}


/**
 * Type declaration in a working copy test.
 * A type is removed from the working copy only.
 */
public void testRemoveType() throws JavaModelException, CoreException {
	this.workingCopy.getType("X").delete(true, null);
	
	IJavaSearchScope scope = 
		SearchEngine.createJavaSearchScope(
			new IJavaElement[] {this.workingCopy.getParent()});
	
	// type X should not be visible when working copy hides it
	JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
	new SearchEngine(new IWorkingCopy[] {this.workingCopy}).search(
		getWorkspace(), 
		"X",
		TYPE,
		DECLARATIONS, 
		scope, 
		resultCollector);
	assertEquals(
		"", 
		resultCollector.toString());
		
	// ensure the type is still present in the compilation unit
	resultCollector = new JavaSearchResultCollector();
	new SearchEngine().search(
		getWorkspace(), 
		"X",
		TYPE,
		DECLARATIONS, 
		scope, 
		resultCollector);
	assertEquals(
		"src/wc/X.java wc.X [X]", 
		resultCollector.toString());

}
/**
 * Type declaration in a working copy test.
 * A type is moved from one working copy to another.
 */
public void testMoveType() throws JavaModelException, CoreException {
	
	// move type X from working copy in one package to a working copy in another package
	IJavaElement element1 = getCompilationUnit("JavaSearch", "src", "wc1", "X.java").getWorkingCopy();
	ICompilationUnit workingCopy1 = (ICompilationUnit)element1;
	IJavaElement element2 = getCompilationUnit("JavaSearch", "src", "wc2", "Y.java").getWorkingCopy();
	ICompilationUnit workingCopy2 = (ICompilationUnit)element2;
	
	try {
		workingCopy1.getType("X").move(workingCopy2, null, null, true, null);
		
		SearchEngine searchEngine = new SearchEngine(new IWorkingCopy[] {workingCopy1, workingCopy2});
		
		// type X should not be visible in old package
		IJavaSearchScope scope1 = SearchEngine.createJavaSearchScope(new IJavaElement[] {workingCopy1.getParent()});
		JavaSearchResultCollector resultCollector = new JavaSearchResultCollector();
		searchEngine.search(
			getWorkspace(), 
			"X",
			TYPE,
			DECLARATIONS, 
			scope1, 
			resultCollector);
		assertEquals(
			"", 
			resultCollector.toString());
		
		// type X should be visible in new package
		IJavaSearchScope scope2 = SearchEngine.createJavaSearchScope(new IJavaElement[] {workingCopy2.getParent()});
		resultCollector = new JavaSearchResultCollector();
		searchEngine.search(
			getWorkspace(), 
			"X",
			TYPE,
			DECLARATIONS, 
			scope2, 
			resultCollector);
		assertEquals(
			"src/wc2/Y.java wc2.X [X]", 
			resultCollector.toString());
	} finally {
		workingCopy1.destroy();
		workingCopy2.destroy();
	}
}


}
