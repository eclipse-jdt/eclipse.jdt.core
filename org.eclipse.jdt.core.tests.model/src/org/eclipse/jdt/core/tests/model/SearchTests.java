/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Vector;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.ITypeNameRequestor;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.matching.SearchPattern;
import org.eclipse.jdt.internal.core.search.processing.IJob;

import junit.framework.Test;

/*
 * Test indexing support.
 */
public class SearchTests extends ModifyingResourceTests implements IJavaSearchConstants {
	class TypeNameRequestor implements ITypeNameRequestor {
		Vector results = new Vector();
		public void acceptClass(char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path){
			acceptType(packageName, simpleTypeName, enclosingTypeNames);
		}
		public void acceptInterface(char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path){
			acceptType(packageName, simpleTypeName, enclosingTypeNames);
		}
		private void acceptType(char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames) {
			char[] typeName = 
				CharOperation.concat(
					CharOperation.concatWith(enclosingTypeNames, '$'), 
					simpleTypeName,
					'$');
			results.addElement(new String(CharOperation.concat(packageName, typeName, '.')));
		}
		public String toString(){
			int length = results.size();
			String[] strings = new String[length];
			results.toArray(strings);
			org.eclipse.jdt.internal.core.Util.sort(strings);
			StringBuffer buffer = new StringBuffer(100);
			for (int i = 0; i < length; i++){
				buffer.append(strings[i]);
				if (i != length-1) {
					buffer.append('\n');
				}
			}
			return buffer.toString();
		}
	}
	class WaitingJob implements IJob {
		boolean isResumed;
		boolean isRunning;
		public boolean belongsTo(String jobFamily) {
			return false;
		}
		public void cancel() {
		}
		public boolean execute(IProgressMonitor progress) {
			startJob();
			suspend();
			return true;
		}
		public boolean isReadyToRun() {
			return true;
		}
		public synchronized void resume() {
			this.isResumed = true;
			notifyAll();
		}
		public synchronized void startJob() {
			this.isRunning = true;
			notifyAll();
		}
		public synchronized void suspend() {
			if (this.isResumed) return;
			try {
				wait(60000); // wait 1 minute max
			} catch (InterruptedException e) {
			}
		}
		public synchronized void waitForJobToStart() {
			if (this.isRunning) return;
			try {
				wait(60000); // wait 1 minute max
			} catch (InterruptedException e) {
			}
		}
	}
public static Test suite() {
	return new Suite(SearchTests.class);
}


public SearchTests(String name) {
	super(name);
}
protected void assertAllTypes(int waitingPolicy, IProgressMonitor progressMonitor, String expected) throws JavaModelException {
	assertAllTypes("Unexpected all types", null/* no specific project*/, waitingPolicy, progressMonitor, expected);
}
protected void assertAllTypes(String expected) throws JavaModelException {
	assertAllTypes(WAIT_UNTIL_READY_TO_SEARCH, null/* no progress monitor*/, expected);
}
protected void assertAllTypes(String message, IJavaProject project, String expected) throws JavaModelException {
	assertAllTypes(message, project, WAIT_UNTIL_READY_TO_SEARCH, null/* no progress monitor*/, expected);
}
protected void assertAllTypes(String message, IJavaProject project, int waitingPolicy, IProgressMonitor progressMonitor, String expected) throws JavaModelException {
	IJavaSearchScope scope =
		project == null ?
			SearchEngine.createWorkspaceScope() :
			SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
	SearchEngine searchEngine = new SearchEngine();
	TypeNameRequestor requestor = new TypeNameRequestor();
	searchEngine.searchAllTypeNames(
		ResourcesPlugin.getWorkspace(),
		null,
		null,
		PATTERN_MATCH,
		CASE_INSENSITIVE,
		TYPE,
		scope, 
		requestor,
		waitingPolicy,
		progressMonitor);
	String actual = requestor.toString();
	if (!expected.equals(actual)){
	 	System.out.println(Util.displayString(actual, 3));
	}
	assertEquals(
		message,
		expected,
		actual);
}
protected void assertPattern(String expected, ISearchPattern actualPattern) {
	String actual = actualPattern == null ? null : actualPattern.toString();
	if (!expected.equals(actual)) {
		System.out.println(actual == null ? "null" : Util.displayString(actual));
	}
	assertEquals(
		"Unexpected search pattern",
		expected,
		actual);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	createJavaProject("P");
	createFolder("/P/x/y/z");
	createFile(
		"/P/x/y/z/Foo.java",
		"package x.y,z;\n" +
		"import x.y.*;\n" +
		"import java.util.Vector;\n" +
		"public class Foo {\n" +
		"  int field;\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}"
	);
	createFile(
		"/P/x/y/z/I.java",
		"package x.y,z;\n" +
		"public interface I {\n" +
		"}"
	);
}
public void tearDownSuite() throws Exception {
	deleteProject("P");
	super.tearDownSuite();
}
/*
 * Ensure that changing the classpath in the middle of reindexing
 * a project causes another request to reindex.
 */
public void testChangeClasspath() throws CoreException {
	IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
	WaitingJob job = new WaitingJob();
	try {
		// setup: suspend indexing and create a project (prj=src) with one cu
		indexManager.disable();
		JavaCore.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				createJavaProject("P1");
				createFile(
					"/P1/X.java",
					"public class X {\n" +
					"}"
				);
			}
		}, null);
		
		// add waiting job and wait for it to be executed
		indexManager.request(job);
		indexManager.enable();
		job.waitForJobToStart(); // job is suspended here
		
		// remove source folder from classpath
		IJavaProject project = getJavaProject("P1");
		project.setRawClasspath(
			new IClasspathEntry[0], 
			null);
			
		// resume waiting job
		job.resume();
		
		assertAllTypes(
			"Unexpected all types after removing source folder",
			project,
			""
		);
	} finally {
		job.resume();
		deleteProject("P1");
		indexManager.enable();
	}
}
/*
 * Ensures that passing a null progress monitor with a CANCEL_IF_NOT_READY_TO_SEARCH
 * waiting policy doesn't throw a NullPointerException but an OperationCanceledException.
 * (regression test for bug 33571 SearchEngine.searchAllTypeNames: NPE when passing null as progress monitor)
 */
 public void testNullProgressMonitor() throws CoreException {
	IndexManager indexManager = JavaModelManager.getJavaModelManager().getIndexManager();
	WaitingJob job = new WaitingJob();
 	try {
 		// add waiting job and wait for it to be executed
		indexManager.disable();
		indexManager.request(job);
		indexManager.enable();
		job.waitForJobToStart(); // job is suspended here
		
		// query all type names with a null progress monitor
		boolean operationCanceled = false;
		try {
			assertAllTypes(
				CANCEL_IF_NOT_READY_TO_SEARCH,
				null, // null progress monitor
				"Should not get any type"
			);
		} catch (OperationCanceledException e) {
			operationCanceled = true;
		}
		assertTrue("Should throw an OperationCanceledException", operationCanceled);
 	} finally {
 		job.resume();
 	}
 }
/*
 * Ensure that removing the outer folder from the classpath doesn't remove cus in inner folder
 * from index
 * (regression test for bug 32607 Removing outer folder removes nested folder's cus from index)
 */
public void testRemoveOuterFolder() throws CoreException {
	try {
		// setup: one cu in a nested source folder
		JavaCore.run(new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				IJavaProject project = createJavaProject("P1");
				project.setRawClasspath(
					createClasspath(new String[] {"/P1/src1", "src2/", "/P1/src1/src2", ""}), 
					new Path("/P1/bin"),
					null);
				createFolder("/P1/src1/src2");
				createFile(
					"/P1/src1/src2/X.java",
					"public class X {\n" +
					"}"
				);
			}
		}, null);
		IJavaProject project = getJavaProject("P1");
		assertAllTypes(
			"Unexpected all types after setup",
			project,
			"X"
		);
		
		// remove outer folder from classpath
		project.setRawClasspath(
			createClasspath(new String[] {"/P1/src1/src2", ""}), 
			null);
		assertAllTypes(
			"Unexpected all types after removing outer folder",
			project,
			"X"
		);
		
	} finally {
		deleteProject("P1");
	}
}
/**
 * Test pattern creation
 */
public void testSearchPatternCreation01() {

	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			"main(*)", 
			IJavaSearchConstants.METHOD,
			IJavaSearchConstants.REFERENCES,
			CASE_SENSITIVE);
	
	assertPattern(
		"MethodReferencePattern: main(*), pattern match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation02() {

	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			"main(*) void", 
			IJavaSearchConstants.METHOD,
			IJavaSearchConstants.REFERENCES,
			CASE_SENSITIVE);
	
	assertPattern(
		"MethodReferencePattern: main(*) --> void, pattern match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation03() {

	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			"main(String*) void", 
			IJavaSearchConstants.METHOD,
			IJavaSearchConstants.REFERENCES,
			CASE_SENSITIVE);
	
	assertPattern(
		"MethodReferencePattern: main(String*) --> void, pattern match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation04() {

	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			"main(*[])", 
			IJavaSearchConstants.METHOD,
			IJavaSearchConstants.REFERENCES,
			CASE_SENSITIVE);
	
	assertPattern(
		"MethodReferencePattern: main(*[]), pattern match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation05() {

	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			"java.lang.*.main ", 
			IJavaSearchConstants.METHOD,
			IJavaSearchConstants.REFERENCES,
			CASE_SENSITIVE);
	
	assertPattern(
		"MethodReferencePattern: java.lang.*.main(...), pattern match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation06() {

	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			"java.lang.* ", 
			IJavaSearchConstants.CONSTRUCTOR,
			IJavaSearchConstants.REFERENCES,
			CASE_SENSITIVE);
	
	assertPattern(
		"ConstructorReferencePattern: java.lang.*(...), pattern match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation07() {

	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			"X(*,*)", 
			IJavaSearchConstants.CONSTRUCTOR,
			IJavaSearchConstants.REFERENCES,
			CASE_SENSITIVE);
	
	assertPattern(
		"ConstructorReferencePattern: X(*, *), pattern match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation08() {

	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			"main(String*,*) void", 
			IJavaSearchConstants.METHOD,
			IJavaSearchConstants.REFERENCES,
			CASE_SENSITIVE);
	
	assertPattern(
		"MethodReferencePattern: main(String*, *) --> void, pattern match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation09() {

	SearchPattern searchPattern = (SearchPattern)SearchEngine.createSearchPattern(
			"foo*(X, int, int, X, int)", 
			IJavaSearchConstants.METHOD,
			IJavaSearchConstants.DECLARATIONS,
			CASE_SENSITIVE);
	
	assertEquals(
		"methodDecl/foo",
		searchPattern == null ? null : new String(searchPattern.indexEntryPrefix()));
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation10() {

	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			"x.y.z.Bar.field Foo", 
			IJavaSearchConstants.FIELD,
			IJavaSearchConstants.DECLARATIONS,
			CASE_SENSITIVE);
	
	assertPattern(
		"FieldDeclarationPattern: x.y.z.Bar.field --> Foo, exact match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation11() {

	SearchPattern searchPattern = (SearchPattern)SearchEngine.createSearchPattern(
			"x.y.z.Bar.field Foo", 
			IJavaSearchConstants.FIELD,
			IJavaSearchConstants.DECLARATIONS,
			CASE_SENSITIVE);
	
	assertEquals(
		"fieldDecl/field",
		searchPattern == null ? null : new String(searchPattern.indexEntryPrefix()));
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation12() throws CoreException {
	IField field = getCompilationUnit("/P/x/y/z/Foo.java").getType("Foo").getField("field");
	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			field, 
			IJavaSearchConstants.REFERENCES);
	
	assertPattern(
		"FieldReferencePattern: x.y.z.Foo.field --> int, exact match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation13() throws CoreException {
	IField field = getCompilationUnit("/P/x/y/z/Foo.java").getType("Foo").getField("field");
	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			field, 
			IJavaSearchConstants.DECLARATIONS);
	
	assertPattern(
		"FieldDeclarationPattern: x.y.z.Foo.field --> int, exact match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation14() throws CoreException {
	IField field = getCompilationUnit("/P/x/y/z/Foo.java").getType("Foo").getField("field");
	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			field, 
			IJavaSearchConstants.ALL_OCCURRENCES);
	
	assertPattern(
		"FieldDeclarationPattern: x.y.z.Foo.field --> int, exact match, case sensitive\n" +
		"| FieldReferencePattern: x.y.z.Foo.field --> int, exact match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation15() throws CoreException {
	IImportDeclaration importDecl = getCompilationUnit("/P/x/y/z/Foo.java").getImport("x.y.*");
	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			importDecl, 
			IJavaSearchConstants.REFERENCES);
	
	assertPattern(
		"PackageReferencePattern: <x.y>, exact match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation16() throws CoreException {
	IMethod method = getCompilationUnit("/P/x/y/z/Foo.java").getType("Foo").getMethod("bar", new String[] {});
	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			method, 
			IJavaSearchConstants.DECLARATIONS);
	
	assertPattern(
		"MethodDeclarationPattern: x.y.z.Foo.bar() --> void, exact match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation17() throws CoreException {
	IMethod method = getCompilationUnit("/P/x/y/z/Foo.java").getType("Foo").getMethod("bar", new String[] {});
	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			method, 
			IJavaSearchConstants.REFERENCES);
	
	assertPattern(
		"MethodReferencePattern: x.y.z.Foo.bar() --> void, exact match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation18() throws CoreException {
	IMethod method = getCompilationUnit("/P/x/y/z/Foo.java").getType("Foo").getMethod("bar", new String[] {});
	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			method, 
			IJavaSearchConstants.ALL_OCCURRENCES);
	
	assertPattern(
		"MethodDeclarationPattern: x.y.z.Foo.bar() --> void, exact match, case sensitive\n" +
		"| MethodReferencePattern: x.y.z.Foo.bar() --> void, exact match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation19() throws CoreException {
	IType type = getCompilationUnit("/P/x/y/z/Foo.java").getType("Foo");
	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			type, 
			IJavaSearchConstants.DECLARATIONS);
	
	assertPattern(
		"TypeDeclarationPattern: pkg<x.y.z>, enclosing<>, type<Foo>, exact match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation20() throws CoreException {
	IType type = getCompilationUnit("/P/x/y/z/Foo.java").getType("Foo");
	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			type, 
			IJavaSearchConstants.REFERENCES);
	
	assertPattern(
		"TypeReferencePattern: pkg<x.y.z>, type<Foo>, exact match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation21() throws CoreException {
	IType type = getCompilationUnit("/P/x/y/z/I.java").getType("I");
	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			type, 
			IJavaSearchConstants.IMPLEMENTORS);
	
	assertPattern(
		"SuperInterfaceReferencePattern: <I>, exact match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation22() throws CoreException {
	IType type = getCompilationUnit("/P/x/y/z/Foo.java").getType("Foo");
	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			type, 
			IJavaSearchConstants.ALL_OCCURRENCES);
	
	assertPattern(
		"TypeDeclarationPattern: pkg<x.y.z>, enclosing<>, type<Foo>, exact match, case sensitive\n" +
		"| TypeReferencePattern: pkg<x.y.z>, type<Foo>, exact match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation23() throws CoreException {
	IPackageDeclaration pkg = getCompilationUnit("/P/x/y/z/Foo.java").getPackageDeclaration("x.y.z");
	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			pkg, 
			IJavaSearchConstants.REFERENCES);
	
	assertPattern(
		"PackageReferencePattern: <x.y.z>, exact match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation24() throws CoreException {
	IPackageFragment pkg = getPackage("/P/x/y/z");
	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			pkg, 
			IJavaSearchConstants.REFERENCES);
	
	assertPattern(
		"PackageReferencePattern: <x.y.z>, exact match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation25() throws CoreException {
	IImportDeclaration importDecl = getCompilationUnit("/P/x/y/z/Foo.java").getImport("java.util.Vector");
	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			importDecl, 
			IJavaSearchConstants.REFERENCES);
	
	assertPattern(
		"TypeReferencePattern: pkg<java.util>, type<Vector>, exact match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation26() throws CoreException {
	IPackageFragment pkg = getPackage("/P/x/y/z");
	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			pkg, 
			IJavaSearchConstants.DECLARATIONS);
	
	assertPattern(
		"PackageDeclarationPattern: <x.y.z>, exact match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation27() throws CoreException {
	IPackageDeclaration pkg = getCompilationUnit("/P/x/y/z/Foo.java").getPackageDeclaration("x.y.z");
	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			pkg, 
			IJavaSearchConstants.DECLARATIONS);
	
	assertPattern(
		"PackageDeclarationPattern: <x.y.z>, exact match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation28() throws CoreException {
	IImportDeclaration importDecl = getCompilationUnit("/P/x/y/z/Foo.java").getImport("x.y.*");
	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			importDecl, 
			IJavaSearchConstants.DECLARATIONS);
	
	assertPattern(
		"PackageDeclarationPattern: <x.y>, exact match, case sensitive",
		searchPattern);
}

/**
 * Test pattern creation
 */
public void testSearchPatternCreation29() throws CoreException {
	IPackageFragment pkg = getPackage("/P/x/y/z");
	ISearchPattern searchPattern = SearchEngine.createSearchPattern(
			pkg, 
			IJavaSearchConstants.ALL_OCCURRENCES);
	
	assertPattern(
		"PackageDeclarationPattern: <x.y.z>, exact match, case sensitive\n" +
		"| PackageReferencePattern: <x.y.z>, exact match, case sensitive",
		searchPattern);
}


}
