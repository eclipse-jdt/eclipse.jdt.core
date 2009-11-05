/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.tests.util.Util;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.index.DiskIndex;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.search.AbstractSearchScope;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.search.matching.AndPattern;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.PatternLocator;
import org.eclipse.jdt.internal.core.search.matching.TypeDeclarationPattern;

/**
 * Non-regression tests for bugs fixed in Java Search engine.
 */
public class JavaSearchBugsTests extends AbstractJavaSearchTests {
	private final static int UI_DECLARATIONS = DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE;

// Debug
static {
//	 org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
}

public JavaSearchBugsTests(String name) {
	super(name);
	this.endChar = "";
}
public static Test suite() {
	return buildModelTestSuite(JavaSearchBugsTests.class);
}
class TestCollector extends JavaSearchResultCollector {
	public List matches = new ArrayList();
	public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
		super.acceptSearchMatch(searchMatch);
		this.matches.add(searchMatch);
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
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.SuiteOfTestCases#setUpSuite()
 */
public void setUpSuite() throws Exception {
	super.setUpSuite();
	JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "1.5");
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b95152.jar", false);
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b123679.jar", false);
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b140156.jar", false);
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b164791.jar", false);
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b166348.jar", false);
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

/**
 * @bug 41018: Method reference not found
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=41018"
 */
public void testBug41018() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b41018/A.java",
		"package b41018;\n" +
		"public class A {\n" +
		"	protected void anotherMethod() {\n" +
		"		methodA(null);\n" +
		"	}\n" +
		"	private Object methodA(ClassB.InnerInterface arg3) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n" +
		"class ClassB implements InterfaceB {\n" +
		"}\n" +
		"interface InterfaceB {\n" +
		"	interface InnerInterface {\n" +
		"	}\n" +
		"}\n"
		);
	IType type = this.workingCopies[0].getType("A");
	IMethod method = type.getMethod("methodA", new String[] { "QClassB.InnerInterface;" });
	search(method, REFERENCES);
	assertSearchResults(
		"src/b41018/A.java void b41018.A.anotherMethod() [methodA(null)] EXACT_MATCH"
	);
}

public void testBug6930_AllConstructorDeclarations01() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p6930/AllConstructorDeclarations01.java",
		"package p6930;\n" +
		"public class AllConstructorDeclarations01 {\n" +
		"  public AllConstructorDeclarations01() {}\n" +
		"  public AllConstructorDeclarations01(Object o) {}\n" +
		"  public AllConstructorDeclarations01(Object o, String s) {}\n" +
		"}\n"
	);
	
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p6930/AllConstructorDeclarations01b.java",
		"package p6930;\n" +
		"public class AllConstructorDeclarations01b {\n" +
		"}\n"
	);
	
	ConstructorDeclarationsCollector requestor = new ConstructorDeclarationsCollector();
	searchAllConstructorDeclarations("AllConstructorDeclarations", SearchPattern.R_PREFIX_MATCH, requestor);
	assertSearchResults(
		"p6930.AllConstructorDeclarations01#AllConstructorDeclarations01()\n" + 
		"p6930.AllConstructorDeclarations01#AllConstructorDeclarations01(Object o)\n" + 
		"p6930.AllConstructorDeclarations01#AllConstructorDeclarations01(Object o,String s)\n" + 
		"p6930.AllConstructorDeclarations01b#AllConstructorDeclarations01b()*",
		requestor
	);
}

public void testBug6930_AllConstructorDeclarations02() throws Exception {
	try {
		IJavaProject p = createJavaProject("P", new String[] {}, new String[] {"/P/lib6930.jar"}, "");
		
		createJar(new String[] {
			"p6930/AllConstructorDeclarations02.java",
			"package p6930;\n" +
			"public class AllConstructorDeclarations02 {\n" +
			"  public AllConstructorDeclarations02() {}\n" +
			"  public AllConstructorDeclarations02(Object o) {}\n" +
			"  public AllConstructorDeclarations02(Object o, String s) {}\n" +
			"}",
			"p6930/AllConstructorDeclarations02b.java",
			"package p6930;\n" +
			"public class AllConstructorDeclarations02b {\n" +
			"}"
		}, p.getProject().getLocation().append("lib6930.jar").toOSString());
		refresh(p);
		
		ConstructorDeclarationsCollector requestor = new ConstructorDeclarationsCollector();
		searchAllConstructorDeclarations("AllConstructorDeclarations", SearchPattern.R_PREFIX_MATCH, requestor);
		assertSearchResults(
			"p6930.AllConstructorDeclarations02#AllConstructorDeclarations02()\n" + 
			"p6930.AllConstructorDeclarations02#AllConstructorDeclarations02(java.lang.Object o)\n" + 
			"p6930.AllConstructorDeclarations02#AllConstructorDeclarations02(java.lang.Object o,java.lang.String s)\n" + 
			"p6930.AllConstructorDeclarations02b#AllConstructorDeclarations02b()",
			requestor
		);
	} finally {
		deleteProject("P");
	}
}

public void testBug6930_AllConstructorDeclarations03() throws Exception {
	try {
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[] {}, "bin");
		
		createFolder("/P/src/p6930");
		
		createFile(
				"/P/src/p6930/AllConstructorDeclarations03.java",
				"package p6930;\n" +
				"public class AllConstructorDeclarations03 {\n" +
				"  public AllConstructorDeclarations03() {}\n" +
				"  public AllConstructorDeclarations03(Object o) {}\n" +
				"  public AllConstructorDeclarations03(Object o, String s) {}\n" +
				"}");
		
		createFile(
				"/P/src/p6930/AllConstructorDeclarations03b.java",
				"package p6930;\n" +
				"public class AllConstructorDeclarations03b {\n" +
				"}");
		refresh(p);
		
		ConstructorDeclarationsCollector requestor = new ConstructorDeclarationsCollector();
		searchAllConstructorDeclarations("AllConstructorDeclarations", SearchPattern.R_PREFIX_MATCH, requestor);
		assertSearchResults(
			"p6930.AllConstructorDeclarations03#AllConstructorDeclarations03()\n" + 
			"p6930.AllConstructorDeclarations03#AllConstructorDeclarations03(Object o)\n" + 
			"p6930.AllConstructorDeclarations03#AllConstructorDeclarations03(Object o,String s)\n" + 
			"p6930.AllConstructorDeclarations03b#AllConstructorDeclarations03b()*",
			requestor
		);
	} finally {
		deleteProject("P");
	}
}

public void testBug6930_AllConstructorDeclarations04() throws Exception {
	try {
		IJavaProject p = createJavaProject("P", new String[] {}, new String[] {"/P/lib6930.jar"}, "","1.5");
		
		createJar(
			new String[] {
				"p6930/AllConstructorDeclarations04.java",
				"package p6930;\n" +
				"public class AllConstructorDeclarations04 {\n" +
				"  public AllConstructorDeclarations04(java.util.Collection<Object> c) {}\n" +
				"}"
			},
			p.getProject().getLocation().append("lib6930.jar").toOSString(),
			new String[]{getExternalJCLPathString("1.5")},
			"1.5");
		refresh(p);
		
		ConstructorDeclarationsCollector requestor = new ConstructorDeclarationsCollector();
		searchAllConstructorDeclarations("AllConstructorDeclarations", SearchPattern.R_PREFIX_MATCH, requestor);
		assertSearchResults(
			"p6930.AllConstructorDeclarations04#AllConstructorDeclarations04(java.util.Collection<java.lang.Object> c)",
			requestor
		);
	} finally {
		deleteProject("P");
	}
}

public void testBug6930_AllConstructorDeclarations05() throws Exception {
	try {
		IJavaProject p = createJavaProject("P", new String[] {}, new String[] {"/P/lib6930.jar"}, "");
		
		createJar(new String[] {
			"p6930/AllConstructorDeclarations05.java",
			"package p6930;\n" +
			"public class AllConstructorDeclarations05 {\n" +
			"  public class AllConstructorDeclarations05b {\n" +
			"    public AllConstructorDeclarations05b(Object o) {}\n" +
			"  }\n" +
			"}"
		}, p.getProject().getLocation().append("lib6930.jar").toOSString());
		refresh(p);
		
		ConstructorDeclarationsCollector requestor = new ConstructorDeclarationsCollector();
		searchAllConstructorDeclarations("AllConstructorDeclarations", SearchPattern.R_PREFIX_MATCH, requestor);
		assertSearchResults(
			"p6930.AllConstructorDeclarations05#AllConstructorDeclarations05()",
			requestor
		);
	} finally {
		deleteProject("P");
	}
}

/**
 * @bug 70827: [Search] wrong reference match to private method of supertype
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=70827"
 */
public void testBug70827() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b70827/A.java",
		"package b70827;\n" +
		"class A {\n" +
		"	private void privateMethod() {\n" +
		"	}\n" +
		"}\n" +
		"class Second extends A {\n" +
		"	void call() {\n" +
		"		int i= privateMethod();\n" +
		"	}\n" +
		"	int privateMethod() {\n" +
		"		return 1;\n" +
		"	}\n" +
		"}\n"
		);
	IType type = this.workingCopies[0].getType("A");
	IMethod method = type.getMethod("privateMethod", new String[] {});
	search(method, REFERENCES);
	assertSearchResults(
		""
	);
}

/**
 * @bug 71279: [Search] NPE in TypeReferenceLocator when moving CU with unresolved type reference
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=71279"
 */
public void testBug71279() throws CoreException {
	JavaSearchResultCollector result = new JavaSearchResultCollector() {
	    public void beginReporting() {
	    	addLine("Starting search...");
        }
	    public void endReporting() {
	        addLine("Done searching.");
        }
	};
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b71279/AA.java",
		"package b71279;\n" +
		"public class AA {\n" +
		"	Unknown ref;\n" +
		"}\n"
		);
	new SearchEngine(this.workingCopies).searchDeclarationsOfReferencedTypes(this.workingCopies[0], result, null);
	assertSearchResults(
		"Starting search...\n" +
		"Done searching.",
		result);
}

/**
 * @bug 72866: [search] references to endVisit(MethodInvocation) reports refs to endVisit(SuperMethodInvocation)
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=72866"
 */
public void testBug72866() throws CoreException {
	this.workingCopies = new ICompilationUnit[4];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b72866/A.java",
		"package b72866;\n" +
		"public abstract class A {\n" +
		"	public abstract void foo(V v);\n" +
		"}\n",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b72866/SX.java",
		"package b72866;\n" +
		"public class SX extends A {\n" +
		"	public void foo(V v) {\n" +
		"	    v.bar(this);\n" +
		"	}\n" +
		"}\n"	,
		owner
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b72866/V.java",
		"package b72866;\n" +
		"public class V {\n" +
		"	void bar(A a) {}\n" +
		"	void bar(X x) {}\n" +
		"	void bar(SX s) {}\n" +
		"}\n"	,
		owner
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b72866/X.java",
		"package b72866;\n" +
		"public class X extends A {\n" +
		"	public void foo(V v) {\n" +
		"	    v.bar(this);\n" +
		"	}\n" +
		"}\n"	,
		owner
	);
	IType type = this.workingCopies[2].getType("V");
	IMethod method = type.getMethod("bar", new String[] {"QX;"});
	search(method, REFERENCES);
	assertSearchResults(
		"src/b72866/X.java void b72866.X.foo(V) [bar(this)] EXACT_MATCH"
	);
}

/**
 * @bug 73112: [Search] SearchEngine doesn't find all fields multiple field declarations
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=73112"
 */
public void testBug73112a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73112/A.java",
		"package b73112;\n" +
		"public class A {\n" +
		"    int fieldA73112a = 1, fieldA73112b = new Integer(2).intValue(), fieldA73112c = fieldA73112a + fieldA73112b;\n" +
		"    int fieldA73112d;\n" +
		"    \n" +
		"    public void method(){}\n" +
		"}\n");
	// search field references to first multiple field
	search("fieldA73112*", FIELD, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b73112/A.java b73112.A.fieldA73112a [fieldA73112a] EXACT_MATCH\n" +
		"src/b73112/A.java b73112.A.fieldA73112b [fieldA73112b] EXACT_MATCH\n" +
		"src/b73112/A.java b73112.A.fieldA73112c [fieldA73112c] EXACT_MATCH\n" +
		"src/b73112/A.java b73112.A.fieldA73112c [fieldA73112a] EXACT_MATCH\n" +
		"src/b73112/A.java b73112.A.fieldA73112c [fieldA73112b] EXACT_MATCH\n" +
		"src/b73112/A.java b73112.A.fieldA73112d [fieldA73112d] EXACT_MATCH"
	);
}
public void testBug73112b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = super.getWorkingCopy("/JavaSearchBugs/src/b73112/B.java",
		"package b73112;\n" +
		"public class B {\n" +
		"    int fieldB73112a, fieldB73112b = 10;\n" +
		"    int fieldB73112c = fieldB73112a + fieldB73112b, fieldB73112d = fieldB73112c + fieldB73112a, fieldB73112e;\n" +
		"    \n" +
		"    public void method(){}\n" +
		"}\n");
	// search field references to first multiple field
	search("fieldB73112*", FIELD, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b73112/B.java b73112.B.fieldB73112a [fieldB73112a] EXACT_MATCH\n" +
		"src/b73112/B.java b73112.B.fieldB73112b [fieldB73112b] EXACT_MATCH\n" +
		"src/b73112/B.java b73112.B.fieldB73112c [fieldB73112c] EXACT_MATCH\n" +
		"src/b73112/B.java b73112.B.fieldB73112c [fieldB73112a] EXACT_MATCH\n" +
		"src/b73112/B.java b73112.B.fieldB73112c [fieldB73112b] EXACT_MATCH\n" +
		"src/b73112/B.java b73112.B.fieldB73112d [fieldB73112d] EXACT_MATCH\n" +
		"src/b73112/B.java b73112.B.fieldB73112d [fieldB73112c] EXACT_MATCH\n" +
		"src/b73112/B.java b73112.B.fieldB73112d [fieldB73112a] EXACT_MATCH\n" +
		"src/b73112/B.java b73112.B.fieldB73112e [fieldB73112e] EXACT_MATCH"
	);
}

/**
 * @bug 73336: [1.5][search] Search Engine does not find type references of actual generic type parameters
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=73336"
 */
public void testBug73336() throws CoreException {
	this.workingCopies = new ICompilationUnit[6];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73336/A.java",
		"package b73336;\n" +
		"public class A {}\n",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b73336/AA.java",
		"package b73336;\n" +
		"public class AA extends A {}\n",
		owner
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b73336/B.java",
		"package b73336;\n" +
		"public class B extends X<A, A> {\n" +
		"	<T> void foo(T t) {}\n" +
		"}\n",
		owner
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b73336/C.java",
		"package b73336;\n" +
		"public class C implements I<A> {\n" +
		"	public void foo() {\n" +
		"		B b = new B();\n" +
		"		b.<A>foo(new A());\n" +
		"	}\n" +
		"}\n",
		owner
	);
	this.workingCopies[4] = getWorkingCopy("/JavaSearchBugs/src/b73336/I.java",
		"package b73336;\n" +
		"public interface I<T>  {\n" +
		"	public void foo();\n" +
		"}\n",
		owner
	);
	this.workingCopies[5] = getWorkingCopy("/JavaSearchBugs/src/b73336/X.java",
		"package b73336;\n" +
		"public class X<T, U> {\n" +
		"	<V> void foo(V v) {}\n" +
		"	class Member<T> {\n" +
		"		void foo() {}\n" +
		"	}\n" +
		"}\n",
		owner
	);
	// search for first and second method should both return 2 inaccurate matches
	IType type = this.workingCopies[0].getType("A");
	search(type, REFERENCES); //, getJavaSearchScopeBugs("b73336", false));
	assertSearchResults(
		"src/b73336/AA.java b73336.AA [A] EXACT_MATCH\n" +
		"src/b73336/B.java b73336.B [A] EXACT_MATCH\n" +
		"src/b73336/B.java b73336.B [A] EXACT_MATCH\n" +
		"src/b73336/C.java b73336.C [A] EXACT_MATCH\n" +
		"src/b73336/C.java void b73336.C.foo() [A] EXACT_MATCH\n" +
		"src/b73336/C.java void b73336.C.foo() [A] EXACT_MATCH"
	);
}
public void testBug73336b() throws CoreException {
	this.workingCopies = new ICompilationUnit[4];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73336b/A.java",
		"package b73336b;\n" +
		"public class A {}\n",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b73336b/B.java",
		"package b73336b;\n" +
		"public class B extends X<A, A> {\n" +
		"}\n",
		owner
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b73336b/C.java",
		"package b73336b;\n" +
		"public class C extends X<A, A>.Member<A> {\n" +
		"	public C() {\n" +
		"		new X<A, A>().super();\n" +
		"	}\n" +
		"}\n",
		owner
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b73336b/X.java",
		"package b73336b;\n" +
		"public class X<T, U> {\n" +
		"	<V> void foo(V v) {}\n" +
		"	class Member<T> {\n" +
		"		void foo() {}\n" +
		"	}\n" +
		"}\n",
		owner
	);
	// search for first and second method should both return 2 inaccurate matches
	IType type = this.workingCopies[0].getType("A");
//	search(type, REFERENCES, getJavaSearchScopeBugs("b73336b", false));
	search(type, REFERENCES); //, getJavaSearchScopeBugs("b73336", false));
	assertSearchResults(
		"src/b73336b/B.java b73336b.B [A] EXACT_MATCH\n" +
		"src/b73336b/B.java b73336b.B [A] EXACT_MATCH\n" +
		"src/b73336b/C.java b73336b.C [A] EXACT_MATCH\n" +
		"src/b73336b/C.java b73336b.C [A] EXACT_MATCH\n" +
		"src/b73336b/C.java b73336b.C [A] EXACT_MATCH\n" +
		"src/b73336b/C.java b73336b.C() [A] EXACT_MATCH\n" +
		"src/b73336b/C.java b73336b.C() [A] EXACT_MATCH"
	);
}
// Verify that no NPE was raised on following case (which produces compiler error)
public void testBug73336c() throws CoreException {
	this.workingCopies = new ICompilationUnit[4];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73336c/A.java",
		"package b73336c;\n" +
		"public class A {}\n",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b73336c/B.java",
		"package b73336c;\n" +
		"public class B extends X<A, A> {\n" +
		"}\n",
		owner
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b73336c/C.java",
		"package b73336c;\n" +
		"public class C implements X<A, A>.Interface<A>  {\n" +
		"	void bar() {}\n" +
		"}\n",
		owner
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b73336c/X.java",
		"package b73336c;\n" +
		"public class X<T, U> {\n" +
		"	interface Interface<V> {\n" +
		"		void bar();\n" +
		"	}\n" +
		"}\n",
		owner
	);
	// search for first and second method should both return 2 inaccurate matches
	IType type = this.workingCopies[0].getType("A");
//	search(type, REFERENCES, getJavaSearchScopeBugs("b73336c", false));
	search(type, REFERENCES); //, getJavaSearchScopeBugs("b73336", false));
	assertSearchResults(
		"src/b73336c/B.java b73336c.B [A] EXACT_MATCH\n" +
		"src/b73336c/B.java b73336c.B [A] EXACT_MATCH\n" +
		"src/b73336c/C.java b73336c.C [A] EXACT_MATCH\n" +
		"src/b73336c/C.java b73336c.C [A] EXACT_MATCH\n" +
		"src/b73336c/C.java b73336c.C [A] EXACT_MATCH"
	);
}

/**
 * @bug 73696: searching only works for IJavaSearchConstants.TYPE, but not CLASS or INTERFACE
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=73696"
 */
public void testBug73696() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73696/C.java",
		"package b73696;\n" +
		"public class C implements  I {\n" +
		"}",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b73696/I.java",
		"package b73696;\n" +
		"public interface I {}\n",
		owner
	);
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);

	// Interface declaration
	TypeDeclarationPattern pattern = new TypeDeclarationPattern(
		null,
		null,
		null,
		IIndexConstants.INTERFACE_SUFFIX,
		SearchPattern.R_PATTERN_MATCH
	);
	new SearchEngine(new ICompilationUnit[] {this.workingCopies[1]}).search(
		pattern,
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		scope,
		this.resultCollector,
		null);
	// Class declaration
	pattern = new TypeDeclarationPattern(
		null,
		null,
		null,
		IIndexConstants.CLASS_SUFFIX,
		SearchPattern.R_PATTERN_MATCH
	);
	new SearchEngine(new ICompilationUnit[] {this.workingCopies[0]}).search(
		pattern,
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		scope,
		this.resultCollector,
		null);
	assertSearchResults(
		"src/b73696/I.java b73696.I [I] EXACT_MATCH\n" +
		"src/b73696/C.java b73696.C [C] EXACT_MATCH"
	);
}

/**
 * @bug 74776: [Search] Wrong search results for almost identical method
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=74776"
 */
public void testBug74776() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b74776/A.java",
		"package b74776;\n" +
		"public class A {\n" +
		"	/**\n" +
		"	 * @deprecated Use {@link #foo(IRegion)} instead\n" +
		"	 * @param r\n" +
		"	 */\n" +
		"	void foo(Region r) {\n" +
		"		foo((IRegion)r);\n" +
		"	}\n" +
		"	void foo(IRegion r) {\n" +
		"	}\n" +
		"}\n",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b74776/IRegion.java",
		"package b74776;\n" +
		"public interface IRegion {\n" +
		"}\n",
		owner
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b74776/Region.java",
		"package b74776;\n" +
		"public class Region implements IRegion {\n" +
		"\n" +
		"}\n",
		owner
	);
	// search method references
	IType type = this.workingCopies[0].getType("A");
	IMethod method = type.getMethod("foo", new String[] { "QRegion;" });
	search(method, REFERENCES);
	assertSearchResults("");
}

/**
 * @bug 75816: [search] correct results are missing in java search
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=75816"
 */
public void testBug75816() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/test75816.jar", "", "Test.class").getType();
	IType innerType = type.getType("Inner");
	IMethod[] methods = innerType.getMethods();
	assertEquals("Wrong number of method.", 1, methods.length);
	search(methods[0], REFERENCES);
	assertSearchResults(
		"lib/test75816.jar Test.Inner Test.newInner(java.lang.Object) EXACT_MATCH"
	);
}

/**
 * @bug 77093: [search] No references found to method with member type argument
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=77093"
 */
private void setUpBug77093() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b77093/X.java",
		"package b77093;\n" +
		"public class X {\n" +
		"	class Z {\n" +
		"	}\n" +
		"	Z[][] z_arrays;\n" +
		"	X() {\n" +
		"		this(new Z[10][]);\n" +
		"	}\n" +
		"	X(Z[][] arrays) {\n" +
		"		z_arrays = arrays;\n" +
		"	}\n" +
		"	private void foo(Z[] args) {\n" +
		"	}\n" +
		"	void bar() {\n" +
		"		for (int i=0; i<z_arrays.length; i++)\n" +
		"			foo(z_arrays[i]);\n" +
		"	}\n" +
		"}"
	);
}
public void testBug77093constructor() throws CoreException {
	setUpBug77093();
	IType type = this.workingCopies[0].getType("X");
	IMethod method = type.getMethod("X", new String[] {"[[QZ;"});
	// Search for constructor declarations and references
	search(method, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b77093/X.java b77093.X() [this(new Z[10][]);] EXACT_MATCH\n"+
		"src/b77093/X.java b77093.X(Z[][]) [X] EXACT_MATCH"
	);
}
public void testBug77093field() throws CoreException {
	setUpBug77093();
	IType type = this.workingCopies[0].getType("X");
	IField field = type.getField("z_arrays");
	// Search for field declarations and references
	search(field, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b77093/X.java b77093.X.z_arrays [z_arrays] EXACT_MATCH\n" +
		"src/b77093/X.java b77093.X(Z[][]) [z_arrays] EXACT_MATCH\n" +
		"src/b77093/X.java void b77093.X.bar() [z_arrays] EXACT_MATCH\n" +
		"src/b77093/X.java void b77093.X.bar() [z_arrays] EXACT_MATCH"
	);
}
public void testBug77093method() throws CoreException {
	setUpBug77093();
	IType type = this.workingCopies[0].getType("X");
	IMethod method = type.getMethod("foo", new String[] {"[QZ;"});
	search(method, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b77093/X.java void b77093.X.foo(Z[]) [foo] EXACT_MATCH\n" +
		"src/b77093/X.java void b77093.X.bar() [foo(z_arrays[i])] EXACT_MATCH"
	);
}

/**
 * @bug 77388: [compiler] Reference to constructor includes space after closing parenthesis
 */
public void testBug77388() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b77388/Test.java",
		"package b77388;\n" +
		"class Test {\n" +
		"	Test(int a, int b) {	}\n" +
		"	void take(Test mc) { }\n" +
		"	void run() {\n" +
		"		take( new Test(1, 2) ); // space in \") )\" is in match\n" +
		"	}\n" +
		"}");
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("Test", new String[] {"I", "I"});
	// Search for constructor references
	search(method, REFERENCES);
	assertSearchResults(
		"src/b77388/Test.java void b77388.Test.run() [new Test(1, 2)] EXACT_MATCH"
	);
}

/**
 * @bug 78082: [1.5][search] FieldReferenceMatch in static import should not include qualifier
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=78082"
 */
public void testBug78082() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b78082/M.java",
		"package b78082;\n" +
		"public class M {\n" +
		"	static int VAL=78082;\n" +
		"}\n",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b78082/XY.java",
		"package b78082;\n" +
		"import static b78082.M.VAL;\n" +
		"public class XY {\n" +
		"	double val = VAL;\n" +
		"	double val2= b78082.M.VAL;\n" +
		"}\n",
		owner
	);
	// search field references
	IType type = this.workingCopies[0].getType("M");
	IField field = type.getField("VAL");
	search(field, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b78082/M.java b78082.M.VAL [VAL] EXACT_MATCH\n" +
		"src/b78082/XY.java [VAL] EXACT_MATCH\n" +
		"src/b78082/XY.java b78082.XY.val [VAL] EXACT_MATCH\n" +
		"src/b78082/XY.java b78082.XY.val2 [VAL] EXACT_MATCH"
	);
}

/**
 * @bug 79267: [search] Refactoring of static generic member fails partially
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=79267"
 */
public void testBug79267() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79267/Test.java",
		"package b79267;\n" +
		"public class Test {\n" +
		"	private static final X<String, String> BEFORE	= new X<String, String>(4);\n" +
		"\n" +
		"	static {\n" +
		"		BEFORE.put(\"key1\",\"value1\");\n" +
		"		BEFORE.put(\"key2\",\"value2\");\n" +
		"	}\n" +
		"	\n" +
		"	private static final X<Y, Object>	objectToPrimitiveMap	= new X<Y, Object>(8);\n" +
		"\n" +
		"	static {\n" +
		"		objectToPrimitiveMap.put(new Y<Object>(new Object()), new Object());\n" +
		"	}\n" +
		"}\n" +
		"\n" +
		"class X<T, U> {\n" +
		"	X(int x) {}\n" +
		"	void put(T t, U u) {}\n" +
		"}\n" +
		"\n" +
		"class Y<T> {\n" +
		"	Y(T t) {}\n" +
		"}\n");
	// search field references
	IType type = this.workingCopies[0].getType("Test");
	IField field = type.getField("BEFORE");
	search(field, REFERENCES);
	field = type.getField("objectToPrimitiveMap");
	search(field, REFERENCES);
	assertSearchResults(
		"src/b79267/Test.java b79267.Test.static {} [BEFORE] EXACT_MATCH\n" +
		"src/b79267/Test.java b79267.Test.static {} [BEFORE] EXACT_MATCH\n" +
		"src/b79267/Test.java b79267.Test.static {} [objectToPrimitiveMap] EXACT_MATCH"
	);
}

/**
 * @bug 79378: [search] IOOBE when inlining a method
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=79378"
 */
public void testBug79378() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79378/A.java",
		"package b79378;\n" +
		"public class Test {\n" +
		"	void foo79378(String s, RuntimeException[] exceptions) {}\n" +
		"	void foo79378(RuntimeException[] exceptions) {}\n" +
		"	void call() {\n" +
		"		String s= null; \n" +
		"		Exception[] exceptions= null;\n" +
		"		foo79378(s, exceptions);\n" +
		"	}\n" +
		"}\n"
	);
	IMethod[] methods = this.workingCopies[0].getType("Test").getMethods();
	assertEquals("Invalid number of methods", 3, methods.length);
	search(methods[0], REFERENCES);
	assertSearchResults(
		"src/b79378/A.java void b79378.Test.call() [foo79378(s, exceptions)] POTENTIAL_MATCH"
	);
}
public void testBug79378b() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79378/A.java",
		"package b79378;\n" +
		"public class Test {\n" +
		"	void foo79378(String s, RuntimeException[] exceptions) {}\n" +
		"	void foo79378(RuntimeException[] exceptions) {}\n" +
		"	void call() {\n" +
		"		String s= null; \n" +
		"		Exception[] exceptions= null;\n" +
		"		foo79378(s, exceptions);\n" +
		"	}\n" +
		"}\n"
	);
	IMethod[] methods = this.workingCopies[0].getType("Test").getMethods();
	assertEquals("Invalid number of methods", 3, methods.length);
	search(methods[1], REFERENCES);
	assertSearchResults("");
}

/**
 * @bug 79803: [1.5][search] Search for references to type A reports match for type variable A
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=79803"
 */
public void testBug79803() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79803/A.java",
		"package b79803;\n" +
		"class A<A> {\n" +
		"    A a;\n" +
		"    b79803.A pa= new b79803.A();\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("A");
	search(type, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"src/b79803/A.java b79803.A.pa [b79803.A] EXACT_MATCH\n" +
		"src/b79803/A.java b79803.A.pa [b79803.A] EXACT_MATCH"
	);
}
public void testBug79803string() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79803/A.java",
		"package b79803;\n" +
		"class A<A> {\n" +
		"    A a;\n" +
		"    b79803.A pa= new b79803.A();\n" +
		"}\n"
	);
	search("A", TYPE, REFERENCES);
	assertSearchResults(
		"src/b79803/A.java b79803.A.a [A] EXACT_MATCH\n" +
		"src/b79803/A.java b79803.A.pa [A] EXACT_MATCH\n" +
		"src/b79803/A.java b79803.A.pa [A] EXACT_MATCH"
	);
}

/**
 * @bug 79860: [1.5][search] Search doesn't find type reference in type parameter bound
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=79860"
 */
public void testBug79860() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79860/X.java",
		"package b79860;\n" +
		"public class X<T extends A> { }\n" +
		"class A { }",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b79860/Y.java",
		"package b79860;\n" +
		"public class Y<T extends B&I1&I2&I3> { }\n" +
		"class B { }\n" +
		"interface I1 {}\n" +
		"interface I2 {}\n" +
		"interface I3 {}\n",
		owner
	);
	IType type = this.workingCopies[0].getType("A");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b79860/X.java b79860.X [A] EXACT_MATCH"
	);
}
public void testBug79860string() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79860/X.java",
		"package b79860;\n" +
		"public class X<T extends A> { }\n" +
		"class A { }",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b79860/Y.java",
		"package b79860;\n" +
		"public class Y<T extends B&I1&I2&I3> { }\n" +
		"class B { }\n" +
		"interface I1 {}\n" +
		"interface I2 {}\n" +
		"interface I3 {}\n",
		owner
	);
	search("I?", TYPE, REFERENCES);
	assertSearchResults(
		"src/b79860/Y.java b79860.Y [I1] EXACT_MATCH\n" +
		"src/b79860/Y.java b79860.Y [I2] EXACT_MATCH\n" +
		"src/b79860/Y.java b79860.Y [I3] EXACT_MATCH"
	);
}

/**
 * @bug 79990: [1.5][search] Search doesn't find type reference in type parameter bound
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=79990"
 */
private void setUpBug79990() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79990/Test.java",
		"package b79990;\n" +
		"class Test<T> {\n" +
		"    public void first(Exception num) {}\n" +
		"    public void second(T t) {}\n" +
		"}\n" +
		"class Sub extends Test<Exception> {\n" +
		"    public void first(Exception num) {}\n" +
		"    public void second(Exception t) {}\n" +
		"}\n"
	);}
public void testBug79990() throws CoreException {
	setUpBug79990();
	IMethod method = this.workingCopies[0].getType("Test").getMethods()[0];
	search(method, DECLARATIONS);
	assertSearchResults(
		"src/b79990/Test.java void b79990.Test.first(Exception) [first] EXACT_MATCH\n" +
		"src/b79990/Test.java void b79990.Sub.first(Exception) [first] EXACT_MATCH"
	);
}
public void testBug79990b() throws CoreException {
	setUpBug79990();
	IMethod method = this.workingCopies[0].getType("Test").getMethods()[1];
	search(method, DECLARATIONS);
	assertSearchResults(
		"src/b79990/Test.java void b79990.Test.second(T) [second] EXACT_MATCH\n" +
		"src/b79990/Test.java void b79990.Sub.second(Exception) [second] EXACT_MATCH"
	);
}
public void testBug79990c() throws CoreException {
	setUpBug79990();
	IMethod method = this.workingCopies[0].getType("Test").getMethods()[1];
	search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b79990/Test.java void b79990.Test.second(T) [second] EXACT_MATCH\n" +
		"src/b79990/Test.java void b79990.Sub.second(Exception) [second] EXACT_MATCH"
	);
}
public void testBug79990d() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79990/Test.java",
		"package b79990;\n" +
		"public class Test<T> {\n" +
		"	void methodT(T t) {}\n" +
		"}\n" +
		"class Sub<X> extends Test<X> {\n" +
		"	void methodT(X x) {} // overrides Super#methodT(T)\n" +
		"}\n"
	);
	IMethod method = this.workingCopies[0].getType("Test").getMethods()[0];
	search(method, DECLARATIONS);
	assertSearchResults(
		"src/b79990/Test.java void b79990.Test.methodT(T) [methodT] EXACT_MATCH\n" +
		"src/b79990/Test.java void b79990.Sub.methodT(X) [methodT] EXACT_MATCH"
	);
}

/**
 * @bug 80084: [1.5][search]Rename field fails on field based on parameterized type with member type parameter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=80084"
 */
public void testBug80084() throws CoreException, JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80084/Test.java",
		"package b80084;\n" +
		"class List<T> {}\n" +
		"public class Test {\n" +
		"  void foo(List<Exception> le) {}\n" +
		"  void bar() {\n" +
		"    List<Exception> le = new List<Exception>();\n" +
		"    foo(le);\n" +
		"  }\n" +
		"}\n"
		);
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QList<QException;>;" } );
	search(method, REFERENCES);
	assertSearchResults(
		"src/b80084/Test.java void b80084.Test.bar() [foo(le)] EXACT_MATCH"
	);
}

/**
 * @bug 80194: [1.5][search]Rename field fails on field based on parameterized type with member type parameter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=80194"
 */
private void setUpBug80194() throws CoreException, JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80194/Test.java",
		"package b80194;\n" +
		"interface Map<K, V> {}\n" +
		"class HashMap<K, V> implements Map {}\n" +
		"public class Test {\n" +
		"	void callDoSomething() {\n" +
		"		final Map<String, Object> map = new HashMap<String, Object>();\n" +
		"		doSomething(map);\n" +
		"		doSomething(map, true);\n" +
		"		doSomething(true);\n" +
		"	}\n" +
		"	void doSomething(final Map<String, Object> map) {}\n" +
		"	void doSomething(final Map<String, Object> map, final boolean flag) {}\n" +
		"	void doSomething(final boolean flag) {}\n" +
		"}\n"
	);
}
public void testBug80194() throws CoreException, JavaModelException {
	setUpBug80194();
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("doSomething", new String[] { "QMap<QString;QObject;>;" } );
	search(method, REFERENCES);
	assertSearchResults(
		"src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(map)] EXACT_MATCH"
	);
}
public void testBug80194b() throws CoreException, JavaModelException {
	setUpBug80194();
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("doSomething", new String[] { "QMap<QString;QObject;>;", "Z" } );
	search(method, REFERENCES);
	assertSearchResults(
		"src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(map, true)] EXACT_MATCH"
	);
}
public void testBug80194string1() throws CoreException, JavaModelException {
	setUpBug80194();
	search("doSomething(boolean)", METHOD, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(map)] EXACT_MATCH\n" +
		"src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(true)] EXACT_MATCH\n" +
		"src/b80194/Test.java void b80194.Test.doSomething(boolean) [doSomething] EXACT_MATCH"
	);
}
public void testBug80194string2() throws CoreException, JavaModelException {
	setUpBug80194();
	search("doSomething(Map<String,Object>)", METHOD, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(map)] EXACT_MATCH\n" +
		"src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(true)] EXACT_MATCH\n" +
		"src/b80194/Test.java void b80194.Test.doSomething(Map<String,Object>) [doSomething] EXACT_MATCH"
	);
}
public void testBug80194string3() throws CoreException, JavaModelException {
	setUpBug80194();
	search("doSomething(Map<String,Object>,boolean)", METHOD, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(map, true)] EXACT_MATCH\n" +
		"src/b80194/Test.java void b80194.Test.doSomething(Map<String,Object>, boolean) [doSomething] EXACT_MATCH"
	);
}

/**
 * @bug 80223: [search] Declaration search doesn't consider visibility to determine overriding methods
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=80223"
 */
public void testBug80223() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80223/a/A.java",
		"package b80223.a;\n" +
		"public class A {\n" +
		"    void m() {}\n" +
		"}",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b80223/b/B.java",
		"package b80223.b;\n" +
		"public class B extends b80223.a.A {\n" +
		"    void m() {}\n" +
		"}",
		owner
	);
	// search for method declaration should find only A match
	IType type = this.workingCopies[0].getType("A");
	IMethod method = type.getMethod("m", new String[0]);
	search(method, DECLARATIONS);
	assertSearchResults(
		"src/b80223/a/A.java void b80223.a.A.m() [m] EXACT_MATCH"
	);
}

/**
 * @bug 80264: [search] Search for method declarations in workspace, disregarding declaring type
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=80264"
 *
 * Following tests also verify
 * @bug 87778: [search] doesn't find all declarations of method with covariant return type
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=87778"
 */
// Methods
private void setUpBug80264_Methods() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80264/Methods.java",
		"package b80264;\n" +
		"class Methods {\n" +
		"    Methods stable() { return null; }\n" +
		"    Methods covariant() { return null; }\n" +
		"}\n" +
		"class MethodsSub extends Methods {\n" +
		"    Methods stable() { return null; }\n" +
		"    MethodsSub covariant() { return null; }\n" +
		"}\n" +
		"class MethodsOther {\n" +
		"    Methods stable() { return null; }\n" +
		"    Methods covariant() { return null; }\n" +
		"}\n"
	);
}
public void testBug80264_Methods() throws CoreException {
	setUpBug80264_Methods();
	IType type = this.workingCopies[0].getType("Methods");
	IMethod[] methods = type.getMethods();
	search(methods[0], DECLARATIONS);
	search(methods[1], DECLARATIONS);
	assertSearchResults(
		"src/b80264/Methods.java Methods b80264.Methods.stable() [stable] EXACT_MATCH\n" +
		"src/b80264/Methods.java Methods b80264.MethodsSub.stable() [stable] EXACT_MATCH\n" +
		"src/b80264/Methods.java Methods b80264.Methods.covariant() [covariant] EXACT_MATCH\n" +
		"src/b80264/Methods.java MethodsSub b80264.MethodsSub.covariant() [covariant] EXACT_MATCH"
	);
}
public void testBug80264_MethodsIgnoreDeclaringType() throws CoreException, JavaModelException {
	setUpBug80264_Methods();
	IType type = this.workingCopies[0].getType("Methods");
	IMethod[] methods = type.getMethods();
	search(methods[0], DECLARATIONS|IGNORE_DECLARING_TYPE);
	search(methods[1], DECLARATIONS|IGNORE_DECLARING_TYPE);
	assertSearchResults(
		"src/b80264/Methods.java Methods b80264.Methods.stable() [stable] EXACT_MATCH\n" +
		"src/b80264/Methods.java Methods b80264.MethodsSub.stable() [stable] EXACT_MATCH\n" +
		"src/b80264/Methods.java Methods b80264.MethodsOther.stable() [stable] EXACT_MATCH\n" +
		"src/b80264/Methods.java Methods b80264.Methods.covariant() [covariant] EXACT_MATCH\n" +
		"src/b80264/Methods.java Methods b80264.MethodsOther.covariant() [covariant] EXACT_MATCH"
	);
}
public void testBug80264_MethodsIgnoreReturnType() throws CoreException, JavaModelException {
	setUpBug80264_Methods();
	IType type = this.workingCopies[0].getType("Methods");
	IMethod[] methods = type.getMethods();
	search(methods[0], DECLARATIONS|IGNORE_RETURN_TYPE);
	search(methods[1], DECLARATIONS|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b80264/Methods.java Methods b80264.Methods.stable() [stable] EXACT_MATCH\n" +
		"src/b80264/Methods.java Methods b80264.MethodsSub.stable() [stable] EXACT_MATCH\n" +
		"src/b80264/Methods.java Methods b80264.Methods.covariant() [covariant] EXACT_MATCH\n" +
		"src/b80264/Methods.java MethodsSub b80264.MethodsSub.covariant() [covariant] EXACT_MATCH"
	);
}
public void testBug80264_MethodsIgnoreBothTypes() throws CoreException, JavaModelException {
	setUpBug80264_Methods();
	IType type = this.workingCopies[0].getType("Methods");
	IMethod[] methods = type.getMethods();
	search(methods[0], DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE);
	search(methods[1], DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b80264/Methods.java Methods b80264.Methods.stable() [stable] EXACT_MATCH\n" +
		"src/b80264/Methods.java Methods b80264.MethodsSub.stable() [stable] EXACT_MATCH\n" +
		"src/b80264/Methods.java Methods b80264.MethodsOther.stable() [stable] EXACT_MATCH\n" +
		"src/b80264/Methods.java Methods b80264.Methods.covariant() [covariant] EXACT_MATCH\n" +
		"src/b80264/Methods.java MethodsSub b80264.MethodsSub.covariant() [covariant] EXACT_MATCH\n" +
		"src/b80264/Methods.java Methods b80264.MethodsOther.covariant() [covariant] EXACT_MATCH"
	);
}
// Classes
private void setUpBug80264_Classes() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80264/Classes.java",
		"package b80264;\n" +
		"class Classes {\n" +
		"    class Inner {}\n" +
		"}\n" +
		"class ClassesSub extends Classes {\n" +
		"    class Inner {}\n" +
		"}\n" +
		"class ClassesOther {\n" +
		"    class Inner {}\n" +
		"}\n"
	);
}
public void testBug80264_Classes() throws CoreException {
	setUpBug80264_Classes();
	IType type = this.workingCopies[0].getType("Classes").getType("Inner");
	search(type, DECLARATIONS);
	assertSearchResults(
		"src/b80264/Classes.java b80264.Classes$Inner [Inner] EXACT_MATCH"
	);
}
public void testBug80264_ClassesIgnoreDeclaringType() throws CoreException, JavaModelException {
	setUpBug80264_Classes();
	IType type = this.workingCopies[0].getType("Classes").getType("Inner");
	search(type, DECLARATIONS|IGNORE_DECLARING_TYPE);
	assertSearchResults(
		"src/b80264/Classes.java b80264.Classes$Inner [Inner] EXACT_MATCH\n" +
		"src/b80264/Classes.java b80264.ClassesSub$Inner [Inner] EXACT_MATCH\n" +
		"src/b80264/Classes.java b80264.ClassesOther$Inner [Inner] EXACT_MATCH"
	);
}
public void testBug80264_ClassesIgnoreReturnType() throws CoreException, JavaModelException {
	setUpBug80264_Classes();
	IType type = this.workingCopies[0].getType("Classes").getType("Inner");
	search(type, DECLARATIONS|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b80264/Classes.java b80264.Classes$Inner [Inner] EXACT_MATCH"
	);
}
public void testBug80264_ClassesIgnoreTypes() throws CoreException, JavaModelException {
	setUpBug80264_Classes();
	IType type = this.workingCopies[0].getType("Classes").getType("Inner");
	search(type, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b80264/Classes.java b80264.Classes$Inner [Inner] EXACT_MATCH\n" +
		"src/b80264/Classes.java b80264.ClassesSub$Inner [Inner] EXACT_MATCH\n" +
		"src/b80264/Classes.java b80264.ClassesOther$Inner [Inner] EXACT_MATCH"
	);
}
// Fields
private void setUpBug80264_Fields() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80264/Fields.java",
		"package b80264;\n" +
		"class Fields {\n" +
		"    Fields field1;\n" +
		"    Fields field2;\n" +
		"}\n" +
		"class FieldsSub extends Fields {\n" +
		"    Fields field1;\n" +
		"    FieldsSub field2;\n" +
		"}\n" +
		"class FieldsOther {\n" +
		"    Fields field1;\n" +
		"    Fields field2;\n" +
		"}\n"
	);
}
public void testBug80264_Fields() throws CoreException {
	setUpBug80264_Fields();
	IType type = this.workingCopies[0].getType("Fields");
	IField[] fields = type.getFields();
	search(fields[0], DECLARATIONS);
	search(fields[1], DECLARATIONS);
	assertSearchResults(
		"src/b80264/Fields.java b80264.Fields.field1 [field1] EXACT_MATCH\n" +
		"src/b80264/Fields.java b80264.Fields.field2 [field2] EXACT_MATCH"
	);
}
public void testBug80264_FieldsIgnoreDeclaringType() throws CoreException, JavaModelException {
	setUpBug80264_Fields();
	IType type = this.workingCopies[0].getType("Fields");
	IField[] fields = type.getFields();
	search(fields[0], DECLARATIONS|IGNORE_DECLARING_TYPE);
	search(fields[1], DECLARATIONS|IGNORE_DECLARING_TYPE);
	assertSearchResults(
		"src/b80264/Fields.java b80264.Fields.field1 [field1] EXACT_MATCH\n" +
		"src/b80264/Fields.java b80264.FieldsSub.field1 [field1] EXACT_MATCH\n" +
		"src/b80264/Fields.java b80264.FieldsOther.field1 [field1] EXACT_MATCH\n" +
		"src/b80264/Fields.java b80264.Fields.field2 [field2] EXACT_MATCH\n" +
		"src/b80264/Fields.java b80264.FieldsOther.field2 [field2] EXACT_MATCH"
	);
}
public void testBug80264_FieldsIgnoreReturnType() throws CoreException, JavaModelException {
	setUpBug80264_Fields();
	IType type = this.workingCopies[0].getType("Fields");
	IField[] fields = type.getFields();
	search(fields[0], DECLARATIONS|IGNORE_RETURN_TYPE);
	search(fields[1], DECLARATIONS|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b80264/Fields.java b80264.Fields.field1 [field1] EXACT_MATCH\n" +
		"src/b80264/Fields.java b80264.Fields.field2 [field2] EXACT_MATCH"
	);
}
public void testBug80264_FieldsIgnoreBothTypes() throws CoreException, JavaModelException {
	setUpBug80264_Fields();
	IType type = this.workingCopies[0].getType("Fields");
	IField[] fields = type.getFields();
	search(fields[0], DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE);
	search(fields[1], DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b80264/Fields.java b80264.Fields.field1 [field1] EXACT_MATCH\n" +
		"src/b80264/Fields.java b80264.FieldsSub.field1 [field1] EXACT_MATCH\n" +
		"src/b80264/Fields.java b80264.FieldsOther.field1 [field1] EXACT_MATCH\n" +
		"src/b80264/Fields.java b80264.Fields.field2 [field2] EXACT_MATCH\n" +
		"src/b80264/Fields.java b80264.FieldsSub.field2 [field2] EXACT_MATCH\n" +
		"src/b80264/Fields.java b80264.FieldsOther.field2 [field2] EXACT_MATCH"
	);
}

/**
 * @bug 80890: [search] Strange search engine behaviour
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=80890"
 */
public void testBug80890() throws CoreException, JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80890/A.java",
		"package b80890;\n" +
		"public class A {\n" +
		"	protected void foo(Exception e) {}\n" +
		"	protected void foo(String s) {}\n" +
		"}\n" +
		"class B1 extends A {\n" +
		"	public void bar1() {\n" +
		"		foo(null);\n" +
		"	}\n" +
		"}\n" +
		"class B2 extends A {\n" +
		"	public void bar2() {\n" +
		"		foo(null);\n" +
		"	}\n" +
		"}\n"
		);
	// search for first and second method should both return 2 inaccurate matches
	IType type = this.workingCopies[0].getType("A");
	IMethod method = type.getMethods()[0];
	search(method, REFERENCES);
	method = type.getMethods()[1];
	search(method, REFERENCES);
	assertSearchResults(
		"src/b80890/A.java void b80890.B1.bar1() [foo(null)] POTENTIAL_MATCH\n" +
		"src/b80890/A.java void b80890.B2.bar2() [foo(null)] POTENTIAL_MATCH\n" +
		"src/b80890/A.java void b80890.B1.bar1() [foo(null)] POTENTIAL_MATCH\n" +
		"src/b80890/A.java void b80890.B2.bar2() [foo(null)] POTENTIAL_MATCH"
	);
}

/**
 * @bug 80918: [1.5][search] ClassCastException when searching for references to binary type
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=80918"
 */
public void testBug80918() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", getExternalJCLPathString("1.5"), "java.lang", "Exception.class").getType();
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearchBugs")}, IJavaSearchScope.SOURCES);
	search(type, REFERENCES, SearchPattern.R_CASE_SENSITIVE|SearchPattern.R_ERASURE_MATCH, scope);
	assertSearchResults(
		"" // do not expect to find anything, just verify that no CCE happens
	);
}

/**
 * @bug 81084: [1.5][search]Rename field fails on field based on parameterized type with member type parameter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=81084"
 */
public void testBug81084a() throws CoreException, JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b81084a/Test.java",
		"package b81084a;\n" +
		"class List<E> {}\n" +
		"public class Test {\n" +
		"	class Element{}\n" +
		"	static class Inner {\n" +
		"		private final List<Element> fList1;\n" +
		"		private final List<Test.Element> fList2;\n" +
		"		public Inner(List<Element> list) {\n" +
		"			fList1 = list;\n" +
		"			fList2 = list;\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("Test").getType("Inner");
	IField field1 = type.getField("fList1");
	search(field1, REFERENCES);
	IField field2 = type.getField("fList2");
	search(field2, REFERENCES);
	assertSearchResults(
		"src/b81084a/Test.java b81084a.Test$Inner(List<Element>) [fList1] EXACT_MATCH\n" +
		"src/b81084a/Test.java b81084a.Test$Inner(List<Element>) [fList2] EXACT_MATCH"
	);
}
public void testBug81084string() throws CoreException, JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b81084a/Test.java",
		"package b81084a;\n" +
		"class List<E> {}\n" +
		"public class Test {\n" +
		"	class Element{}\n" +
		"	static class Inner {\n" +
		"		private final List<Element> fList1;\n" +
		"		private final List<Test.Element> fList2;\n" +
		"		public Inner(List<Element> list) {\n" +
		"			fList1 = list;\n" +
		"			fList2 = list;\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
	search("fList1", FIELD, REFERENCES);
	search("fList2", FIELD, REFERENCES);
	assertSearchResults(
		"src/b81084a/Test.java b81084a.Test$Inner(List<Element>) [fList1] EXACT_MATCH\n" +
		"src/b81084a/Test.java b81084a.Test$Inner(List<Element>) [fList2] EXACT_MATCH"
	);
}
public void testBug81084b() throws CoreException, JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b81084b/Test.java",
		"package b81084b;\n" +
		"class List<E> {}\n" +
		"public class Test {\n" +
		"	class Element{}\n" +
		"	static class Inner {\n" +
		"		private final List<? extends Element> fListb1;\n" +
		"		private final List<? extends Test.Element> fListb2;\n" +
		"		public Inner(List<Element> list) {\n" +
		"			fListb1 = list;\n" +
		"			fListb2 = list;\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
		);
	// search element patterns
	IType type = this.workingCopies[0].getType("Test").getType("Inner");
	IField field1 = type.getField("fListb1");
	search(field1, REFERENCES);
	IField field2 = type.getField("fListb2");
	search(field2, REFERENCES);
	assertSearchResults(
		"src/b81084b/Test.java b81084b.Test$Inner(List<Element>) [fListb1] EXACT_MATCH\n" +
		"src/b81084b/Test.java b81084b.Test$Inner(List<Element>) [fListb2] EXACT_MATCH"
	);
}

/**
 * @bug 81556: [search] correct results are missing in java search
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=81556"
 */
public void testBug81556() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearchBugs", "src", "b81556.a", "X81556.java");
	IType type = unit.getType("X81556");
	IMethod method = type.getMethod("foo", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b81556/a/A81556.java void b81556.a.A81556.bar(XX81556) [foo()] EXACT_MATCH"
	);
}

/**
 * @bug 82088: [search][javadoc] Method parameter types references not found in @see/@link tags
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=82088"
 */
public void testBug82088method() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b82088/m/Test.java",
		"package b82088.m;\n" +
		"/**\n" +
		" * @see #setA(A)\n" +
		" */\n" +
		"public class Test {\n" +
		"	A a;\n" +
		"	public void setA(A a) {\n" +
		"		this.a = a;\n" +
		"	}\n" +
		"}\n" +
		"class A {}\n"
		);
	IType type = this.workingCopies[0].getType("A");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b82088/m/Test.java b82088.m.Test [A] EXACT_MATCH\n" +
		"src/b82088/m/Test.java b82088.m.Test.a [A] EXACT_MATCH\n" +
		"src/b82088/m/Test.java void b82088.m.Test.setA(A) [A] EXACT_MATCH"
	);
}
public void testBug82088constructor() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b82088/c/Test.java",
		"package b82088.c;\n" +
		"/**\n" +
		" * @see #Test(A)\n" +
		" */\n" +
		"public class Test {\n" +
		"	A a;\n" +
		"	Test(A a) {\n" +
		"		this.a = a;\n" +
		"	}\n" +
		"}\n" +
		"class A {}\n"
		);
	IType type = this.workingCopies[0].getType("A");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b82088/c/Test.java b82088.c.Test [A] EXACT_MATCH\n" +
		"src/b82088/c/Test.java b82088.c.Test.a [A] EXACT_MATCH\n" +
		"src/b82088/c/Test.java b82088.c.Test(A) [A] EXACT_MATCH"
	);
}

/**
 * @bug 82208: [1.5][search][annot] Search for annotations misses references in default and values constructs
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=82208"
 */
private void setUpBug82208() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b82208/Test.java",
		"package b82208;\n" +
		"interface B82208_I {}\n" +
		"enum B82208_E {}\n" +
		"@interface B82208_A {}\n" +
		"public class B82208 {}\n"
	);
}
public void testBug82208_TYPE() throws CoreException {
	this.resultCollector.showRule();
	setUpBug82208();
	search("B82208*", TYPE, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b82208/Test.java b82208.B82208_I [B82208_I] EXACT_MATCH\n" +
		"src/b82208/Test.java b82208.B82208_E [B82208_E] EXACT_MATCH\n" +
		"src/b82208/Test.java b82208.B82208_A [B82208_A] EXACT_MATCH\n" +
		"src/b82208/Test.java b82208.B82208 [B82208] EXACT_MATCH"
	);
}
public void testBug82208_CLASS() throws CoreException {
	this.resultCollector.showRule();
	setUpBug82208();
	search("B82208*", CLASS, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b82208/Test.java b82208.B82208 [B82208] EXACT_MATCH"
	);
}
public void testBug82208_INTERFACE() throws CoreException {
	this.resultCollector.showRule();
	setUpBug82208();
	search("B82208*", INTERFACE, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b82208/Test.java b82208.B82208_I [B82208_I] EXACT_MATCH"
	);
}
public void testBug82208_ENUM() throws CoreException {
	this.resultCollector.showRule();
	setUpBug82208();
	search("B82208*", ENUM, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b82208/Test.java b82208.B82208_E [B82208_E] EXACT_MATCH"
	);
}
public void testBug82208_ANNOTATION_TYPE() throws CoreException {
	this.resultCollector.showRule();
	setUpBug82208();
	search("B82208*", ANNOTATION_TYPE, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b82208/Test.java b82208.B82208_A [B82208_A] EXACT_MATCH"
	);
}
public void testBug82208_CLASS_AND_INTERFACE() throws CoreException {
	this.resultCollector.showRule();
	setUpBug82208();
	search("B82208*", CLASS_AND_INTERFACE, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b82208/Test.java b82208.B82208_I [B82208_I] EXACT_MATCH\n" +
		"src/b82208/Test.java b82208.B82208 [B82208] EXACT_MATCH"
	);
}
public void testBug82208_CLASS_AND_ENUMERATION() throws CoreException {
	this.resultCollector.showRule();
	setUpBug82208();
	search("B82208*", CLASS_AND_ENUM, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b82208/Test.java b82208.B82208_E [B82208_E] EXACT_MATCH\n" +
		"src/b82208/Test.java b82208.B82208 [B82208] EXACT_MATCH"
	);
}

/**
 * @bug 82673: [1.5][search][annot] Search for annotations misses references in default and values constructs
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83012"
 */
public void testBug82673() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b82673/Test.java",
		"package b82673;\n" +
		"public class Test {\n" +
		"    void test1() {\n" +
		"        class Dummy {};\n" +
		"        Dummy d = new Dummy();\n" +
		"		new X();\n" +
		"    }\n" +
		"    \n" +
		"    void test2() {\n" +
		"        class Dummy {};\n" +
		"        Dummy d = new Dummy();\n" +
		"		new Y();\n" +
		"    }\n" +
		"}\n" +
		"class X {}\n" +
		"class Y {}\n"
	);
	IType type = selectType(this.workingCopies[0], "Test").getMethod("test1", new String[0]).getType("Dummy", 1);
	search(type, REFERENCES);
	assertSearchResults(
		"src/b82673/Test.java void b82673.Test.test1() [Dummy] EXACT_MATCH\n" +
		"src/b82673/Test.java void b82673.Test.test1() [Dummy] EXACT_MATCH"
	);
}

/**
 * @bug 83012: [1.5][search][annot] Search for annotations misses references in default and values constructs
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83012"
 */
public void testBug83012() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83012/Test.java",
		"package b83012;\n" +
		"@interface A {\n" +
		"    String value() default \"\";\n" +
		"}\n" +
		"@interface Main {\n" +
		"	A first() default @A(\"Void\");\n" +
		"	A second();\n" +
		"}\n" +
		"\n" +
		"@Main(first=@A(\"\"), second=@A(\"2\"))\n" +
		"public class Test {\n" +
		"}\n"
	);
	IType type = selectType(this.workingCopies[0], "A");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b83012/Test.java A b83012.Main.first() [A] EXACT_MATCH\n" +
		"src/b83012/Test.java A b83012.Main.first() [A] EXACT_MATCH\n" +
		"src/b83012/Test.java A b83012.Main.second() [A] EXACT_MATCH\n" +
		"src/b83012/Test.java b83012.Test [A] EXACT_MATCH\n" +
		"src/b83012/Test.java b83012.Test [A] EXACT_MATCH"
	);
}

/**
 * @bug 83230: [1.5][search][annot] search for annotation elements does not seem to be implemented yet
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83230"
 */
private void setUpBug83230_Explicit() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83230/Test.java",
		"package b83230;\n" +
		"@interface Author {\n" +
		"	String[] authorName() default \"FREDERIC\";\n" +
		"	int[] age();\n" +
		"	int ageMin = 20;\n" +
		"	@interface Surname {}\n" +
		"	class Address {\n" +
		"		String city;\n" +
		"		public void foo(Object obj) {}\n" +
		"	}\n" +
		"}\n" +
		"\n" +
		"@Author(authorName=\"FREDERIC\", age=41)\n" +
		"public class Test {\n" +
		"	@Author(authorName={\"FREDERIC\", \"JEROME\"}, age={41, 35} )\n" +
		"	Test() {}\n" +
		"	@Author(authorName=\"PHILIPPE\", age=37)\n" +
		"	void foo() {\n" +
		"		@Author(authorName=\"FREDERIC\", age=41)\n" +
		"		final Object obj = new Object() {};\n" +
		"		@Author(authorName=\"FREDERIC\", age=41)\n" +
		"		class Local {\n" +
		"			@Author(authorName=\"FREDERIC\", age=41)\n" +
		"			String foo() {\n" +
		"				Author.Address address = new Author.Address();\n" +
		"				address.foo(obj);\n" +
		"				return address.city;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"	@Author(authorName=\"DAVID\", age=28)\n" +
		"	int min = Author.ageMin;\n" +
		"}\n"
	);
}
public void testBug83230_Explicit() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83230_Explicit();
	IMethod method = selectMethod(this.workingCopies[0], "authorName");
	search(method, REFERENCES);
	assertSearchResults(
		"src/b83230/Test.java b83230.Test [authorName] EXACT_MATCH\n" +
		"src/b83230/Test.java b83230.Test.min [authorName] EXACT_MATCH\n" +
		"src/b83230/Test.java b83230.Test() [authorName] EXACT_MATCH\n" +
		"src/b83230/Test.java void b83230.Test.foo():Local#1 [authorName] EXACT_MATCH\n" +
		"src/b83230/Test.java String void b83230.Test.foo():Local#1.foo() [authorName] EXACT_MATCH\n" +
		"src/b83230/Test.java void b83230.Test.foo() [authorName] EXACT_MATCH\n" +
		"src/b83230/Test.java void b83230.Test.foo() [authorName] EXACT_MATCH"
	);
}
public void testBug83230_Explicit01() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83230_Explicit();
	IMethod method = selectMethod(this.workingCopies[0], "authorName");
	search(method, DECLARATIONS);
	assertSearchResults(
		"src/b83230/Test.java String[] b83230.Author.authorName() [authorName] EXACT_MATCH"
	);
}
public void testBug83230_Explicit02() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83230_Explicit();
	IType type = selectType(this.workingCopies[0], "Address");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b83230/Test.java String void b83230.Test.foo():Local#1.foo() [Author.Address] EXACT_MATCH\n" +
		"src/b83230/Test.java String void b83230.Test.foo():Local#1.foo() [Author.Address] EXACT_MATCH"
	);
}
public void testBug83230_Explicit03() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83230_Explicit();
	IMethod method = selectMethod(this.workingCopies[0], "foo");
	search(method, REFERENCES);
	assertSearchResults(
		"src/b83230/Test.java String void b83230.Test.foo():Local#1.foo() [foo(obj)] EXACT_MATCH"
	);
}
public void testBug83230_Explicit04() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83230_Explicit();
	IField field = selectField(this.workingCopies[0], "city");
	search(field, REFERENCES);
	assertSearchResults(
		"src/b83230/Test.java String void b83230.Test.foo():Local#1.foo() [city] EXACT_MATCH"
	);
}
public void testBug83230_Explicit05() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83230_Explicit();
	IField field = selectField(this.workingCopies[0], "ageMin");
	search(field, REFERENCES);
	assertSearchResults(
		"src/b83230/Test.java b83230.Test.min [ageMin] EXACT_MATCH"
	);
}
public void testBug83230_Implicit01() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83230/Test.java",
		"package b83230;\n" +
		"@interface Annot {\n" +
		"	int value();\n" +
		"}\n" +
		"@Annot(41)\n" +
		"public class Test {\n" +
		"	@Annot(10)\n" +
		"	public void foo() {}\n" +
		"	@Annot(21)\n" +
		"	int bar;\n" +
		"}\n"
	);
	IType type = selectType(this.workingCopies[0], "Annot");
	IMethod method = type.getMethod("value", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b83230/Test.java b83230.Test [41] EXACT_MATCH\n" +
		"src/b83230/Test.java b83230.Test.bar [21] EXACT_MATCH\n" +
		"src/b83230/Test.java void b83230.Test.foo() [10] EXACT_MATCH"
	);
}
public void testBug83230_Implicit02() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83230/Test.java",
		"package b83230;\n" +
		"@interface A {\n" +
		"    String value() default \"\";\n" +
		"}\n" +
		"@interface Main {\n" +
		"	A first() default @A(\"Void\");\n" +
		"	A second();\n" +
		"}\n" +
		"\n" +
		"@Main(first=@A(\"\"), second=@A(\"2\"))\n" +
		"public class Test {\n" +
		"}\n"
	);
	IType type = selectType(this.workingCopies[0], "A");
	IMethod method = type.getMethod("value", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b83230/Test.java A b83230.Main.first() [\"Void\"] EXACT_MATCH\n" +
		"src/b83230/Test.java b83230.Test [\"\"] EXACT_MATCH\n" +
		"src/b83230/Test.java b83230.Test [\"2\"] EXACT_MATCH"
	);
}

/**
 * @bug 83304: [search] correct results are missing in java search
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83304"
 */
public void testBug83304() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83304/Test.java",
		"package b83304;\n" +
		"public class Test {\n" +
		"	void foo() {\n" +
		"		Class<? extends Throwable> l1= null;\n" +
		"		Class<Exception> l2= null;\n" +
		"		\n" +
		"		Class<String> string_Class;\n" +
		"	}\n" +
		"}\n"
		);
	IType type = selectType(this.workingCopies[0], "Class", 3);
	search(type, REFERENCES, ERASURE_RULE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/b83304/Test.java void b83304.Test.foo() [Class] ERASURE_MATCH\n" +
		"src/b83304/Test.java void b83304.Test.foo() [Class] ERASURE_MATCH\n" +
		"src/b83304/Test.java void b83304.Test.foo() [Class] EXACT_MATCH"
	);
}
private void setUpBug83304_TypeParameterizedElementPattern() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83304/Types.java",
		"package b83304;\n" +
		"import g1.t.s.def.Generic;\n" +
		"public class Types {\n" +
		"	public Generic gen;\n" +
		"	public Generic<Object> gen_obj;\n" +
		"	public Generic<Exception> gen_exc;\n" +
		"	public Generic<?> gen_wld;\n" +
		"	public Generic<? extends Throwable> gen_thr;\n" +
		"	public Generic<? super RuntimeException> gen_run;\n" +
		"}\n"
	);
}
public void testBug83304_TypeParameterizedElementPattern() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83304_TypeParameterizedElementPattern();
	IType type = selectType(this.workingCopies[0], "Generic", 4);
	search(type, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"src/b83304/Types.java [g1.t.s.def.Generic] EQUIVALENT_RAW_MATCH\n" +
		"src/b83304/Types.java b83304.Types.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
		"src/b83304/Types.java b83304.Types.gen_obj [Generic] ERASURE_MATCH\n" +
		"src/b83304/Types.java b83304.Types.gen_exc [Generic] EXACT_MATCH\n" +
		"src/b83304/Types.java b83304.Types.gen_wld [Generic] EQUIVALENT_MATCH\n" +
		"src/b83304/Types.java b83304.Types.gen_thr [Generic] EQUIVALENT_MATCH\n" +
		"src/b83304/Types.java b83304.Types.gen_run [Generic] EQUIVALENT_MATCH\n" +
		"lib/JavaSearch15.jar g1.t.s.def.Generic<T> g1.t.s.def.Generic.foo() ERASURE_MATCH"
	);
}
public void testBug83304_TypeGenericElementPattern() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83304_TypeParameterizedElementPattern();
	IType type = getClassFile("JavaSearchBugs", "lib/JavaSearch15.jar", "g1.t.s.def", "Generic.class").getType();
	search(type, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"src/b83304/Types.java [g1.t.s.def.Generic] EQUIVALENT_RAW_MATCH\n" +
		"src/b83304/Types.java b83304.Types.gen [Generic] ERASURE_RAW_MATCH\n" +
		"src/b83304/Types.java b83304.Types.gen_obj [Generic] ERASURE_MATCH\n" +
		"src/b83304/Types.java b83304.Types.gen_exc [Generic] ERASURE_MATCH\n" +
		"src/b83304/Types.java b83304.Types.gen_wld [Generic] ERASURE_MATCH\n" +
		"src/b83304/Types.java b83304.Types.gen_thr [Generic] ERASURE_MATCH\n" +
		"src/b83304/Types.java b83304.Types.gen_run [Generic] ERASURE_MATCH\n" +
		"lib/JavaSearch15.jar g1.t.s.def.Generic<T> g1.t.s.def.Generic.foo() EXACT_MATCH"
	);
}
public void testBug83304_TypeStringPattern() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83304_TypeParameterizedElementPattern();
	search("Generic<? super Exception>", TYPE, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"src/b83304/Types.java [Generic] EQUIVALENT_RAW_MATCH\n" +
		"src/b83304/Types.java b83304.Types.gen [Generic] EQUIVALENT_RAW_MATCH\n" +
		"src/b83304/Types.java b83304.Types.gen_obj [Generic] EQUIVALENT_MATCH\n" +
		"src/b83304/Types.java b83304.Types.gen_exc [Generic] EQUIVALENT_MATCH\n" +
		"src/b83304/Types.java b83304.Types.gen_wld [Generic] EQUIVALENT_MATCH\n" +
		"src/b83304/Types.java b83304.Types.gen_thr [Generic] ERASURE_MATCH\n" +
		"src/b83304/Types.java b83304.Types.gen_run [Generic] ERASURE_MATCH\n" +
		"lib/JavaSearch15.jar g1.t.s.def.Generic<T> g1.t.s.def.Generic.foo() ERASURE_MATCH"
	);
}
private void setUpBug83304_MethodParameterizedElementPattern() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83304/Methods.java",
		"package b83304;\n" +
		"import g5.m.def.Single;\n" +
		"public class Methods {\n" +
		"	void test() {\n" +
		"		Single<Exception> gs = new Single<Exception>();\n" +
		"		Exception exc = new Exception();\n" +
		"		gs.<Throwable>generic(exc);\n" +
		"		gs.<Exception>generic(exc);\n" +
		"		gs.<String>generic(\"\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug83304_MethodParameterizedElementPattern() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83304_MethodParameterizedElementPattern();
	IMethod method = selectMethod(this.workingCopies[0], "generic", 2);
	search(method, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] ERASURE_MATCH\n" +
		"src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] EXACT_MATCH\n" +
		"src/b83304/Methods.java void b83304.Methods.test() [generic(\"\")] ERASURE_MATCH"
	);
}
public void testBug83304_MethodGenericElementPattern() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83304_MethodParameterizedElementPattern();
	IType type = getClassFile("JavaSearchBugs", "lib/JavaSearch15.jar", "g5.m.def", "Single.class").getType();
	IMethod method = type.getMethod("generic", new String[] { "TU;" });
	search(method, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] ERASURE_MATCH\n" +
		"src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] ERASURE_MATCH\n" +
		"src/b83304/Methods.java void b83304.Methods.test() [generic(\"\")] ERASURE_MATCH"
	);
}
public void testBug83304_MethodStringPattern() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83304_MethodParameterizedElementPattern();
	search("<Exception>generic", METHOD, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] ERASURE_MATCH\n" +
		"src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] EXACT_MATCH\n" +
		"src/b83304/Methods.java void b83304.Methods.test() [generic(\"\")] ERASURE_MATCH"
	);
}
private void setUpBug83304_ConstructorGenericElementPattern() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83304/Constructors.java",
		"package b83304;\n" +
		"import g5.c.def.Single;\n" +
		"public class Constructors {\n" +
		"	void test() {\n" +
		"		Exception exc= new Exception();\n" +
		"		new <Throwable>Single<String>(\"\", exc);\n" +
		"		new <Exception>Single<String>(\"\", exc);\n" +
		"		new <String>Single<String>(\"\", \"\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug83304_ConstructorGenericElementPattern() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83304_ConstructorGenericElementPattern();
	IMethod method = selectMethod(this.workingCopies[0], "Single", 3);
	search(method, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"src/b83304/Constructors.java void b83304.Constructors.test() [new <Throwable>Single<String>(\"\", exc)] ERASURE_MATCH\n" +
		"src/b83304/Constructors.java void b83304.Constructors.test() [new <Exception>Single<String>(\"\", exc)] EXACT_MATCH\n" +
		"src/b83304/Constructors.java void b83304.Constructors.test() [new <String>Single<String>(\"\", \"\")] ERASURE_MATCH"
	);
}
public void testBug83304_ConstructorParameterizedElementPattern() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83304_ConstructorGenericElementPattern();
	IType type = getClassFile("JavaSearchBugs", "lib/JavaSearch15.jar", "g5.c.def", "Single.class").getType();
	IMethod method = type.getMethod("Single", new String[] { "TT;", "TU;" });
	search(method, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"src/b83304/Constructors.java void b83304.Constructors.test() [new <Throwable>Single<String>(\"\", exc)] ERASURE_MATCH\n" +
		"src/b83304/Constructors.java void b83304.Constructors.test() [new <Exception>Single<String>(\"\", exc)] ERASURE_MATCH\n" +
		"src/b83304/Constructors.java void b83304.Constructors.test() [new <String>Single<String>(\"\", \"\")] ERASURE_MATCH"
	);
}
public void testBug83304_ConstructorStringPattern() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83304_ConstructorGenericElementPattern();
	search("<Exception>Single", CONSTRUCTOR, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"src/b83304/Constructors.java void b83304.Constructors.test() [new <Throwable>Single<String>(\"\", exc)] ERASURE_MATCH\n" +
		"src/b83304/Constructors.java void b83304.Constructors.test() [new <Exception>Single<String>(\"\", exc)] EXACT_MATCH\n" +
		"src/b83304/Constructors.java void b83304.Constructors.test() [new <String>Single<String>(\"\", \"\")] ERASURE_MATCH\n" +
		"lib/JavaSearch15.jar g5.m.def.Single<T> g5.m.def.Single.returnParamType() ERASURE_MATCH\n" +
		"lib/JavaSearch15.jar g5.m.def.Single<T> g5.m.def.Single.complete(U, g5.m.def.Single<T>) ERASURE_MATCH"
	);
}

/**
 * @bug 83804: [1.5][javadoc] Missing Javadoc node for package declaration
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83804"
 */
private void setUpBug83804_Type() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83804/package-info.java",
		"/**\n" +
		" * Valid javadoc.\n" +
		" * @see Test\n" +
		" * @see Unknown\n" +
		" * @see Test#foo()\n" +
		" * @see Test#unknown()\n" +
		" * @see Test#field\n" +
		" * @see Test#unknown\n" +
		" * @param unexpected\n" +
		" * @throws unexpected\n" +
		" * @return unexpected \n" +
		" */\n" +
		"package b83804;\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b83804/Test.java",
		"/**\n" +
		" * Invalid javadoc\n" +
		" */\n" +
		"package b83804;\n" +
		"public class Test {\n" +
		"	public int field;\n" +
		"	public void foo() {}\n" +
		"}\n"
	);
}
public void testBug83804_Type() throws CoreException {
	this.resultCollector.showInsideDoc();
	setUpBug83804_Type();
	IType type = this.workingCopies[1].getType("Test");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b83804/package-info.java [Test] EXACT_MATCH INSIDE_JAVADOC\n" +
		"src/b83804/package-info.java [Test] EXACT_MATCH INSIDE_JAVADOC\n" +
		"src/b83804/package-info.java [Test] EXACT_MATCH INSIDE_JAVADOC\n" +
		"src/b83804/package-info.java [Test] EXACT_MATCH INSIDE_JAVADOC\n" +
		"src/b83804/package-info.java [Test] EXACT_MATCH INSIDE_JAVADOC"
	);
}
public void testBug83804_Method() throws CoreException {
	this.resultCollector.showInsideDoc();
	setUpBug83804_Type();
	IMethod[] methods = this.workingCopies[1].getType("Test").getMethods();
	assertEquals("Invalid number of methods", 1, methods.length);
	search(methods[0], REFERENCES);
	assertSearchResults(
		"src/b83804/package-info.java [foo()] EXACT_MATCH INSIDE_JAVADOC"
	);
}
public void testBug83804_Field() throws CoreException {
	this.resultCollector.showInsideDoc();
	setUpBug83804_Type();
	IField[] fields = this.workingCopies[1].getType("Test").getFields();
	assertEquals("Invalid number of fields", 1, fields.length);
	search(fields[0], REFERENCES);
	assertSearchResults(
		"src/b83804/package-info.java [field] EXACT_MATCH INSIDE_JAVADOC"
	);
}

/**
 * @bug 83388: [1.5][search] Search for varargs method not finding match
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83388"
 */
public void testBug83388() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83388/R.java",
		"package b83388;\n" +
		"import b83388.*;\n" +
		"public class R {}\n"
	);
	IImportDeclaration importDeclaration = this.workingCopies[0].getImport("pack");
	assertNotNull("Cannot find \"pack\" import declaration for "+this.workingCopies[0].getElementName(), importDeclaration);
	SearchPattern pattern = SearchPattern.createPattern(
		"pack",
		PACKAGE,
		DECLARATIONS,
		EXACT_RULE);
	assertNotNull("Pattern should not be null", pattern);
	MatchLocator.setFocus(pattern, importDeclaration);
	new SearchEngine(this.workingCopies).search(
		pattern,
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		getJavaSearchScope(),
		this.resultCollector,
		null
	);
	assertSearchResults(
		"src/b83388/R.java b83388 [No source] EXACT_MATCH"
	);
}
public void testBug83388b() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83388/R.java",
		"package b83388;\n" +
		"import b83388.*;\n" +
		"public class R {}\n"
	);
	IPackageDeclaration packageDeclaration = this.workingCopies[0].getPackageDeclaration("pack");
	assertNotNull("Cannot find \"pack\" import declaration for "+this.workingCopies[0].getElementName(), packageDeclaration);
	SearchPattern pattern = SearchPattern.createPattern(
		"pack",
		PACKAGE,
		DECLARATIONS,
		EXACT_RULE);
	assertNotNull("Pattern should not be null", pattern);
	MatchLocator.setFocus(pattern, packageDeclaration);
	new SearchEngine(this.workingCopies).search(
		pattern,
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		getJavaSearchScope(),
		this.resultCollector,
		null
	);
	assertSearchResults(
		"src/b83388/R.java b83388 [No source] EXACT_MATCH"
	);
}

/**
 * @bug 83693: [search][javadoc] References to methods/constructors: range does not include parameter lists
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83693"
 */
public void testBug83693() throws CoreException {
	this.resultCollector.showRule();
	this.resultCollector.showInsideDoc();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83693/A.java",
		"package b83693;\n" +
		"import static b83693.A.m;\n" +
		"/**\n" +
		" * @see A#m(int)\n" +
		" */\n" +
		"class A {\n" +
		"    static void m(int i) {\n" +
		"        b83693.A.m(i);\n" +
		"    }\n" +
		"}"
	);
	IMethod[] methods = this.workingCopies[0].getType("A").getMethods();
	assertEquals("Invalid number of methods", 1, methods.length);
	search(methods[0], REFERENCES);
	assertSearchResults(
		"src/b83693/A.java [b83693.A.m] EXACT_MATCH OUTSIDE_JAVADOC\n" +
		"src/b83693/A.java b83693.A [m(int)] EXACT_MATCH INSIDE_JAVADOC\n" +
		"src/b83693/A.java void b83693.A.m(int) [m(i)] EXACT_MATCH OUTSIDE_JAVADOC"
	);
}

/**
 * @bug 83716: [search] refs to 2-arg constructor on Action found unexpected matches
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83716"
 *
 * Note that this test does verify that bug is really fixed, but only that it has no impact
 * on existing behavior. It was not really possible to put a test in this suite to verify that
 * bug is effectively fixed as it appears only to potential match found in plugin dependencies...
 */
public void testBug83716() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83716/X.java",
		"package b83716;\n" +
		"public class X {\n" +
		"	X() {}\n" +
		"	X(int x) {}\n" +
		"}\n" +
		"class Y extends X {\n" +
		"	Y(int y) {\n" +
		"	}\n" +
		"}"
	);
	search("X", CONSTRUCTOR, REFERENCES);
	assertSearchResults(
		"src/b83716/X.java b83716.Y(int) [Y] EXACT_MATCH"
	);
}

/**
 * @bug 84100: [1.5][search] Search for varargs method not finding match
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=84100"
 */
private void setUpBug84100() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b84100/X.java",
		"package b84100;\n" +
		"public class X {\n" +
		"	void foo() {}\n" +
		"	void foo(String s) {}\n" +
		"	void foo(String... xs) {}\n" +
		"	void foo(int x, String... xs) {}\n" +
		"	void foo(String s, int x, String... xs) {}\n" +
		"}\n"
		);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b84100/Z.java",
		"package b84100;\n" +
		"public class Z {\n" +
		"	X x;\n" +
		"	void foo() {\n" +
		"		x.foo();\n" +
		"		x.foo(\"\");\n" +
		"		x.foo(\"\", \"\");\n" +
		"	 	x.foo(\"\", \"\", null);\n" +
		"		x.foo(3, \"\", \"\");\n" +
		"		x.foo(\"\", 3, \"\", \"\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug84100() throws CoreException {
	this.resultCollector.showRule();
	setUpBug84100();
	IMethod method = selectMethod(this.workingCopies[0], "foo", 1);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b84100/Z.java void b84100.Z.foo() [foo()] EXACT_MATCH"
	);
}
public void testBug84100b() throws CoreException {
	this.resultCollector.showRule();
	setUpBug84100();
	IMethod method = selectMethod(this.workingCopies[0], "foo", 2);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b84100/Z.java void b84100.Z.foo() [foo(\"\")] EXACT_MATCH"
	);
}
public void testBug84100c() throws CoreException {
	this.resultCollector.showRule();
	setUpBug84100();
	IMethod method = selectMethod(this.workingCopies[0], "foo", 3);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b84100/Z.java void b84100.Z.foo() [foo(\"\", \"\")] EXACT_MATCH\n" +
		"src/b84100/Z.java void b84100.Z.foo() [foo(\"\", \"\", null)] EXACT_MATCH"
	);
}
public void testBug84100d() throws CoreException {
	this.resultCollector.showRule();
	setUpBug84100();
	IMethod method = selectMethod(this.workingCopies[0], "foo", 4);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b84100/Z.java void b84100.Z.foo() [foo(3, \"\", \"\")] EXACT_MATCH"
	);
}
public void testBug84100e() throws CoreException {
	this.resultCollector.showRule();
	setUpBug84100();
	IMethod method = selectMethod(this.workingCopies[0], "foo", 5);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b84100/Z.java void b84100.Z.foo() [foo(\"\", 3, \"\", \"\")] EXACT_MATCH"
	);
}

/**
 * @bug 84121: [1.5][search][varargs] reference to type reported as inaccurate in vararg
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=84121"
 */
public void testBug84121() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b84121/Test.java",
		"package b84121;\n" +
		"public class Test {\n" +
		"	void foo(Test... t) {}\n" +
		"	void foo(int x, Test... t) {}\n" +
		"	void foo(Test[] t1, Test... t2) {}\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("Test");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b84121/Test.java void b84121.Test.foo(Test ...) [Test] EXACT_MATCH\n" +
		"src/b84121/Test.java void b84121.Test.foo(int, Test ...) [Test] EXACT_MATCH\n" +
		"src/b84121/Test.java void b84121.Test.foo(Test[], Test ...) [Test] EXACT_MATCH\n" +
		"src/b84121/Test.java void b84121.Test.foo(Test[], Test ...) [Test] EXACT_MATCH"
	);
}

/**
 * @bug 84724: [1.5][search] Search for varargs method not finding match
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=84724"
 */
private void setUpBug84724() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b84724/X.java",
		"package b84724;\n" +
		"public class X {\n" +
		"	X(String s) {}\n" +
		"	X(String... v) {}\n" +
		"	X(int x, String... v) {}\n" +
		"	X(String s, int x, String... v) {}\n" +
		"}\n"
		);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b84724/Z.java",
		"package b84724;\n" +
		"public class Z {\n" +
		"	void foo() {\n" +
		"		new X();\n" +
		"		new X(\"\");\n" +
		"		new X(\"\", \"\");\n" +
		"		new X(\"\", \"\", null);\n" +
		"		new X(3, \"\", \"\");\n" +
		"		new X(\"\", 3, \"\", \"\");\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug84724() throws CoreException {
	this.resultCollector.showRule();
	setUpBug84724();
	IMethod method = selectMethod(this.workingCopies[0], "X", 2);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b84724/Z.java void b84724.Z.foo() [new X(\"\")] EXACT_MATCH"
	);
}
public void testBug84724b() throws CoreException {
	this.resultCollector.showRule();
	setUpBug84724();
	IMethod method = selectMethod(this.workingCopies[0], "X", 3);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b84724/Z.java void b84724.Z.foo() [new X()] EXACT_MATCH\n" +
		"src/b84724/Z.java void b84724.Z.foo() [new X(\"\", \"\")] EXACT_MATCH\n" +
		"src/b84724/Z.java void b84724.Z.foo() [new X(\"\", \"\", null)] EXACT_MATCH"
	);
}
public void testBug84724c() throws CoreException {
	this.resultCollector.showRule();
	setUpBug84724();
	IMethod method = selectMethod(this.workingCopies[0], "X", 4);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b84724/Z.java void b84724.Z.foo() [new X(3, \"\", \"\")] EXACT_MATCH"
	);
}
public void testBug84724d() throws CoreException {
	this.resultCollector.showRule();
	setUpBug84724();
	IMethod method = selectMethod(this.workingCopies[0], "X", 5);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b84724/Z.java void b84724.Z.foo() [new X(\"\", 3, \"\", \"\")] EXACT_MATCH"
	);
}

/**
 * @bug 84727: [1.5][search] String pattern search does not work with multiply nested types
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=84727"
 */
private void setUpBug84727() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b84727/A.java",
		"package b84727;\n" +
		"public interface A {\n" +
		"	Set<Set<Exception>> getXYZ(List<Set<Exception>> arg);\n" +
		"	void getXYZ(String s);\n" +
		"}\n"
		);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b84727/X.java",
		"package b84727;\n" +
		"public class X {\n" +
		"	A a;\n" +
		"	void foo() {\n" +
		"		a.getXYZ(new ArrayList());\n" +
		"		a.getXYZ(\"\");\n" +
		"	}\n" +
		"}\n"
		);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b84727/List.java",
		"package b84727;\n" +
		"public interface List<E> {}\n" +
		"interface Set<E> {}\n" +
		"class ArrayList<E> implements List<E> {}"
	);
}
public void testBug84727() throws CoreException {
	this.resultCollector.showRule();
	setUpBug84727();
	IMethod[] methods = this.workingCopies[0].getType("A").getMethods();
	assertEquals("Invalid number of methods", 2, methods.length);
	search(methods[0], REFERENCES);
	assertSearchResults(
		"src/b84727/X.java void b84727.X.foo() [getXYZ(new ArrayList())] EXACT_MATCH"
	);
}
public void testBug84727b() throws CoreException {
	this.resultCollector.showRule();
	setUpBug84727();
	IMethod[] methods = this.workingCopies[0].getType("A").getMethods();
	assertEquals("Invalid number of methods", 2, methods.length);
	search(methods[1], REFERENCES);
	assertSearchResults(
		"src/b84727/X.java void b84727.X.foo() [getXYZ(\"\")] EXACT_MATCH"
	);
}

/**
 * @bug 85810: [1.5][search] Missed type parameter reference in implements clause
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=85810"
 */
public void testBug85810() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b85810/Test.java",
		"package b85810;\n" +
		"public class Test<E> implements In<Test<E>> {\n" +
		"	E e;\n" +
		"}\n" +
		"interface In<T> {}\n"
		);
	ITypeParameter param = selectTypeParameter(this.workingCopies[0], "E");
	search(param, REFERENCES);
	assertSearchResults(
		"src/b85810/Test.java b85810.Test [E] EXACT_MATCH\n" +
		"src/b85810/Test.java b85810.Test.e [E] EXACT_MATCH"
	);
}

/**
 * @bug 86596: [search] Search for type finds segments in import
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=86596"
 */
public void testBug86596() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b86596/aa/link/A.java",
		"package b86596.aa.link;\n" +
		"public interface A {}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b86596/bb/Link.java",
		"package b86596.bb;\n" +
		"public class Link{}\n"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b86596/cc/X.java",
		"package b86596.cc;\n" +
		"import b86596.aa.link.A;\n" +
		"import b86596.bb.Link;\n" +
		"public class X {\n" +
		"	A a;\n" +
		"	Link l;\n" +
		"}\n"
	);
	search("Link", TYPE, REFERENCES, SearchPattern.R_EXACT_MATCH);
	assertSearchResults(
		"src/b86596/cc/X.java [Link] EXACT_RAW_MATCH\n" +
		"src/b86596/cc/X.java b86596.cc.X.l [Link] EXACT_MATCH"
	);
}

/**
 * @bug 86642: [search] no match found of package-visible supertypes in subtypes
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=86642"
 */
public void testBug86642() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b86642/A.java",
		"package b86642;\n" +
		"class A {\n" +
		"	public void m() {}\n" +
		"	protected void f(A a){}\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b86642/B.java",
		"package b86642;\n" +
		"public class B extends A{\n" +
		"	protected void f(A a){\n" +
		"		a.m();\n" +
		"	}\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("A");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b86642/A.java void b86642.A.f(A) [A] EXACT_MATCH\n" +
		"src/b86642/B.java b86642.B [A] EXACT_MATCH\n" +
		"src/b86642/B.java void b86642.B.f(A) [A] EXACT_MATCH"
	);
}
/**
 * @bug 86293: [search] Search for method declaration with pattern "run()" reports match in binary field instead of anonymous class
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=86293"
 */
public void testBug86293() throws CoreException {
    IType type = getClassFile("JavaSearchBugs", "lib/b86293.jar", "", "I86293.class").getType();
	IMethod method = type.getMethod("m86293", new String[0]);
	search(method, DECLARATIONS);
	assertSearchResults(
		"lib/b86293.jar void <anonymous>.m86293() EXACT_MATCH\n" +
		"lib/b86293.jar void I86293.m86293() EXACT_MATCH"
	);
}

/**
 * @bug 86380: [1.5][search][annot] Add support to find references inside annotations on a package declaration
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=86380"
 */
private void setUpBug86380() throws CoreException {
	this.resultCollector.showInsideDoc();
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b86380/package-info.java",
		"/**\n" +
		" * @see Annot#field\n" +
		" */\n" +
		"@Annot(value=11)\n" +
		"package b86380;\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b86380/Test.java",
		"package b86380;\n" +
		"@Annot(12) public class Test {\n" +
		"	public int field = Annot.field;\n" +
		"	public void foo() {}\n" +
		"}\n"
	);
}
public void testBug86380_Type() throws CoreException {
	this.resultCollector.showInsideDoc();
	setUpBug86380();
	ICompilationUnit unit = getCompilationUnit("JavaSearchBugs", "src", "b86380", "Annot.java");
	IType type = unit.getType("Annot");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b86380/Test.java b86380.Test [Annot] EXACT_MATCH OUTSIDE_JAVADOC\n" +
		"src/b86380/Test.java b86380.Test.field [Annot] EXACT_MATCH OUTSIDE_JAVADOC\n" +
		"src/b86380/package-info.java [Annot] EXACT_MATCH INSIDE_JAVADOC\n" +
		"src/b86380/package-info.java [Annot] EXACT_MATCH OUTSIDE_JAVADOC"
	);
}
public void testBug86380_Method() throws CoreException {
	this.resultCollector.showInsideDoc();
	setUpBug86380();
	ICompilationUnit unit = getCompilationUnit("JavaSearchBugs", "src", "b86380", "Annot.java");
	IMethod[] methods = unit.getType("Annot").getMethods();
	assertEquals("Invalid number of methods", 1, methods.length);
	search(methods[0], REFERENCES);
	assertSearchResults(
		"src/b86380/Test.java b86380.Test [12] EXACT_MATCH OUTSIDE_JAVADOC\n" +
		"src/b86380/package-info.java [value] EXACT_MATCH OUTSIDE_JAVADOC"
	);
}
public void testBug86380_Field() throws CoreException {
	this.resultCollector.showInsideDoc();
	setUpBug86380();
	ICompilationUnit unit = getCompilationUnit("JavaSearchBugs", "src", "b86380", "Annot.java");
	IField[] fields = unit.getType("Annot").getFields();
	assertEquals("Invalid number of fields", 1, fields.length);
	search(fields[0], REFERENCES);
	assertSearchResults(
		"src/b86380/Test.java b86380.Test.field [field] EXACT_MATCH OUTSIDE_JAVADOC\n" +
		"src/b86380/package-info.java [field] EXACT_MATCH INSIDE_JAVADOC"
	);
}

/**
 * @bug 88174: [1.5][search][annot] Search for annotations misses references in default and values constructs
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=88174"
 */
public void testBug88174() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b88174/Test.java",
		"package b88174;\n" +
		"public enum Test {\n" +
		"  a {\n" +
		"    int getTheValue() { // not found\n" +
		"      return 0;\n" +
		"    }\n" +
		"  };\n" +
		"  abstract int getTheValue();\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b88174/X.java",
		"package b88174;\n" +
		"public class X {\n" +
		"  X x = new X() {\n" +
		"    int getTheValue() {\n" +
		"      return 0;\n" +
		"    }\n" +
		"  };\n" +
		"  int getTheValue() { return 0; }\n" +
		"}\n"
	);
	IMethod method = this.workingCopies[0].getType("Test").getMethod("getTheValue", new String[0]);
	search(method, DECLARATIONS | IGNORE_DECLARING_TYPE);
	assertSearchResults(
		"src/b88174/Test.java int b88174.Test.a:<anonymous>#1.getTheValue() [getTheValue] EXACT_MATCH\n" +
		"src/b88174/Test.java int b88174.Test.getTheValue() [getTheValue] EXACT_MATCH\n" +
		"src/b88174/X.java int b88174.X.x:<anonymous>#1.getTheValue() [getTheValue] EXACT_MATCH\n" +
		"src/b88174/X.java int b88174.X.getTheValue() [getTheValue] EXACT_MATCH"
	);
}

/**
 * @bug 87627: [search] correct results are missing in java search
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=87627"
 */
public void testBug87627() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b87627.jar", "b87627", "List.class").getType();
	ITypeParameter[] parameters = type.getTypeParameters();
	assertNotNull(type.getFullyQualifiedName()+" should have parameters", parameters);
	assertEquals("Wrong number of parameters", 1, parameters.length);
	search(parameters[0], REFERENCES);
	assertSearchResults(
		"lib/b87627.jar b87627.List EXACT_MATCH\n" +
		"lib/b87627.jar boolean b87627.List.addAll(b87627.Collection<? extends E>) EXACT_MATCH"
	);
}

/**
 * @bug 88300: [search] Reference search result is changed by placement of private method
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=88300"
 */
public void testBug88300() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b88300/SubClass.java",
		"package b88300;\n" +
		"public class SubClass extends SuperClass {\n" +
		"	private void aMethod(String x) {\n" +
		"	}\n" +
		"	public void aMethod(Object x) {\n" +
		"	}\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b88300/SuperClass.java",
		"package b88300;\n" +
		"public class SuperClass {\n" +
		"    public void aMethod(Object x) {\n" +
		"    }\n" +
		"}\n"
		);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b88300/User.java",
		"package b88300;\n" +
		"public class User {\n" +
		"    public void methodUsingSubClassMethod() {\n" +
		"        SuperClass user = new SubClass();\n" +
		"        user.aMethod(new Object());\n" +
		"    }\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("SubClass");
	search(type.getMethods()[1], REFERENCES);
	assertSearchResults(
		"src/b88300/User.java void b88300.User.methodUsingSubClassMethod() [aMethod(new Object())] EXACT_MATCH"
	);
}
public void testBug88300b() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b88300/SubClass.java",
		"package b88300;\n" +
		"public class SubClass extends SuperClass {\n" +
		"	public void aMethod(Object x) {\n" +
		"	}\n" +
		"	private void aMethod(String x) {\n" +
		"	}\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b88300/SuperClass.java",
		"package b88300;\n" +
		"public class SuperClass {\n" +
		"    public void aMethod(Object x) {\n" +
		"    }\n" +
		"}\n"
		);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b88300/User.java",
		"package b88300;\n" +
		"public class User {\n" +
		"    public void methodUsingSubClassMethod() {\n" +
		"        SuperClass user = new SubClass();\n" +
		"        user.aMethod(new Object());\n" +
		"    }\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("SubClass");
	search(type.getMethods()[0], REFERENCES);
	assertSearchResults(
		"src/b88300/User.java void b88300.User.methodUsingSubClassMethod() [aMethod(new Object())] EXACT_MATCH"
	);
}
public void testBug88300c() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b88300/not/fixed/ConditionalFlowInfo.java",
		"package b88300.not.fixed;\n" +
		"public class ConditionalFlowInfo extends FlowInfo {\n" +
		"	public FlowInfo info;\n" +
		"	ConditionalFlowInfo(FlowInfo info){\n" +
		"		this.info = info;\n" +
		"	}\n" +
		"	public void markAsDefinitelyNull(FieldBinding field) {\n" +
		"		info.markAsDefinitelyNull(field);\n" +
		"	}\n" +
		"	public void markAsDefinitelyNull(LocalVariableBinding local) {\n" +
		"		info.markAsDefinitelyNull(local);\n" +
		"	}\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b88300/not/fixed/FlowInfo.java",
		"package b88300.not.fixed;\n" +
		"\n" +
		"class FieldBinding {\n" +
		"	int id;\n" +
		"}\n" +
		"class LocalVariableBinding extends FieldBinding {}\n" +
		"\n" +
		"public abstract class FlowInfo {\n" +
		"	abstract public void markAsDefinitelyNull(LocalVariableBinding local);\n" +
		"	abstract public void markAsDefinitelyNull(FieldBinding field);\n" +
		"}\n"
		);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b88300/not/fixed/UnconditionalFlowInfo.java",
		"package b88300.not.fixed;\n" +
		"public class UnconditionalFlowInfo extends FlowInfo {\n" +
		"	final private void markAsDefinitelyNull(int position) {}\n" +
		"	public void markAsDefinitelyNull(FieldBinding field) {\n" +
		"		markAsDefinitelyNull(field.id);\n" +
		"	}\n" +
		"	public void markAsDefinitelyNull(LocalVariableBinding local) {\n" +
		"		markAsDefinitelyNull(local.id + 1);\n" +
		"	}\n" +
		"}\n"
		);
	IType type = this.workingCopies[2].getType("UnconditionalFlowInfo");
	search(type.getMethods()[2], REFERENCES);
	assertSearchResults(
		"src/b88300/not/fixed/ConditionalFlowInfo.java void b88300.not.fixed.ConditionalFlowInfo.markAsDefinitelyNull(LocalVariableBinding) [markAsDefinitelyNull(local)] EXACT_MATCH"
	);
}

/**
 * @bug 89686: [1.5][search] JavaModelException on ResolvedSourceMethod during refactoring
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=89686"
 */
public void testBug89686() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b89686/A.java",
		"package b89686;\n" +
		"public enum Color {\n" +
		"    RED, GREEN(), BLUE(17), PINK((1+(1+1))) {/*anon*/};\n" +
		"    Color() {}\n" +
		"    Color(int i) {}\n" +
		"}"
	);
	IType type = this.workingCopies[0].getType("Color");
	IMethod method = type.getMethod("Color", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b89686/A.java b89686.Color.RED [RED] EXACT_MATCH\n" +
		"src/b89686/A.java b89686.Color.GREEN [GREEN()] EXACT_MATCH"
	);
}
public void testBug89686b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b89686/A.java",
		"package b89686;\n" +
		"public enum Color {\n" +
		"    RED, GREEN(), BLUE(17), PINK((1+(1+1))) {/*anon*/};\n" +
		"    Color() {}\n" +
		"    Color(int i) {}\n" +
		"}"
	);
	IType type = this.workingCopies[0].getType("Color");
	IMethod method = type.getMethod("Color", new String[] { "I"} );
	search(method, REFERENCES);
	assertSearchResults(
		"src/b89686/A.java b89686.Color.BLUE [BLUE(17)] EXACT_MATCH\n" +
		"src/b89686/A.java b89686.Color.PINK [PINK((1+(1+1)))] EXACT_MATCH"
	);
}

/**
 * @bug 89848: [search] does not find method references in anonymous class of imported jarred plugin
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=89848"
 */
public void testBug89848() throws CoreException {
	IType classFile = getClassFile("JavaSearchBugs", "lib", "b89848", "X.class").getType();
	IMethod method = classFile.getMethod("foo", new String[0]);
	search(method, ALL_OCCURRENCES);
	assertSearchResults(
		"lib/b89848/Test.class void b89848.Test.foo() EXACT_MATCH\n" +
		"lib/b89848/X.class void b89848.X.foo() EXACT_MATCH"
	);
}

/**
 * @bug 90779: [search] Constructor Declaration search with ignoring declaring and return type also ignores type name
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=90779"
 */
public void testBug90779() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b90779/A.java",
		"package b90779;\n" +
		"public class A {\n" +
		"	public A() {}\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b90779/B.java",
		"package b90779;\n" +
		"public class B {\n" +
		"	public B() {}\n" +
		"}\n"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b90779/C.java",
		"package b90779;\n" +
		"public class C {\n" +
		"	public C() {}\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("A");
	IMethod[] methods = type.getMethods();
	assertEquals("Wrong number of methods", 1, methods.length);
	search(methods[0], DECLARATIONS | IGNORE_DECLARING_TYPE | IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b90779/A.java b90779.A() [A] EXACT_MATCH"
	);
}

/**
 * @bug 90915: [1.5][search] NPE in PatternLocator
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=90915"
 */
public void testBug90915() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b90915/X.java",
		"package b90915;\n" +
		"import g5.m.def.Single;\n" +
		"public class X<T> {\n" +
		"	void foo() {\n" +
		"		Single<String> single = new Single<String>() {\n" +
		"			public <U> String generic(U u) { return \"\"; }\n" +
		"			public void paramTypesArgs(Single<String> gs) {}\n" +
		"		};\n" +
		"		single.paramTypesArgs(null);\n" +
		"	}\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("X");
	IMethod[] methods = type.getMethods();
	assertEquals("Wrong number of methods", 1, methods.length);
	IType anonymous = methods[0].getType("", 1);
	assertNotNull("Cannot find anonymous in method foo()", anonymous);
	methods = anonymous.getMethods();
	assertEquals("Wrong number of methods", 2, methods.length);
	search(methods[1], REFERENCES);
	assertSearchResults(
		"src/b90915/X.java void b90915.X.foo() [paramTypesArgs(null)] EXACT_MATCH"
	);
}

/**
 * @bug 91542: [1.5][search] JavaModelException on ResolvedSourceMethod during refactoring
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=91542"
 */
public void testBug91542() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b91542/A.java",
		"package b91542;\n" +
		"\n" +
		"class A<T> {\n" +
		"	void a(A<T> a){}\n" +
		"}\n" +
		"\n" +
		"class B<T> extends A<T> {\n" +
		"	protected void foo() { \n" +
		"		a(this);\n" +
		"	}\n" +
		"}"
	);
	IType type = this.workingCopies[0].getType("B");
	IMethod method = type.getMethod("foo", new String[0]);
	searchDeclarationsOfSentMessages(method, this.resultCollector);
	assertSearchResults(
		"src/b91542/A.java void b91542.A.a(A<T>) [a(A<T> a)] EXACT_MATCH"
	);
}

/**
 * @bug 91078: [search] Java search for package reference wrongly identifies inner class as package
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=91078"
 */
public void testBug91078() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b91078/test/Example.java",
		"package b91078.test;\n" +
		"import b91078.util.HashMap;\n" +
		"import b91078.util.Map;\n" +
		"public class Example {\n" +
		"    public Map.Entry logAll(Object obj) {\n" +
		"    	if (obj instanceof b91078.util.HashMap) {\n" +
		"    		HashMap map = (HashMap) obj;\n" +
		"            return map.entry;\n" +
		"    	}\n" +
		"    	if (obj instanceof b91078.util.HashMap.Entry) {\n" +
		"            Map.Entry entry = (Map.Entry) obj;\n" +
		"            return entry;\n" +
		"    	}\n" +
		"    	return null;\n" +
		"    }\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b91078/util/HashMap.java",
		"package b91078.util;\n" +
		"public class HashMap implements Map {\n" +
		"	public Entry entry;\n" +
		"}\n"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b91078/util/Map.java",
		"package b91078.util;\n" +
		"public interface Map {\n" +
		"	class Entry{}\n" +
		"}\n"
	);
	search("*", PACKAGE, REFERENCES, getJavaSearchWorkingCopiesScope(this.workingCopies[0]));
	assertSearchResults(
		"src/b91078/test/Example.java [b91078.util] EXACT_MATCH\n" +
		"src/b91078/test/Example.java [b91078.util] EXACT_MATCH\n" +
		"src/b91078/test/Example.java Map.Entry b91078.test.Example.logAll(Object) [b91078.util] EXACT_MATCH\n" +
		"src/b91078/test/Example.java Map.Entry b91078.test.Example.logAll(Object) [b91078.util] EXACT_MATCH"
	);
}

/**
 * @bug 92264: [search] all types names should support patterns for package/enclosing type name
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=92264"
 */
public void testBug92264a() throws CoreException {
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine().searchAllTypeNames(
		"*.lang".toCharArray(),
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		IIndexConstants.ONE_STAR,
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		TYPE,
		getJavaSearchScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"java.lang.CharSequence\n" +
		"java.lang.Class\n" +
		"java.lang.CloneNotSupportedException\n" +
		"java.lang.Comparable\n" +
		"java.lang.Deprecated\n" +
		"java.lang.Enum\n" +
		"java.lang.Error\n" +
		"java.lang.Exception\n" +
		"java.lang.IllegalMonitorStateException\n" +
		"java.lang.InterruptedException\n" +
		"java.lang.Object\n" +
		"java.lang.RuntimeException\n" +
		"java.lang.String\n" +
		"java.lang.Throwable",
		requestor);
}
public void testBug92264b() throws CoreException {
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine().searchAllTypeNames(
		"*.lang*".toCharArray(),
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		"*tion".toCharArray(),
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		TYPE,
		getJavaSearchScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"java.lang.CloneNotSupportedException\n" +
		"java.lang.Exception\n" +
		"java.lang.IllegalMonitorStateException\n" +
		"java.lang.InterruptedException\n" +
		"java.lang.RuntimeException\n" +
		"java.lang.annotation.Annotation\n" +
		"java.lang.annotation.Retention",
		requestor);
}
public void testBug92264c() throws CoreException {
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine().searchAllTypeNames(
		"*.test*".toCharArray(),
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		IIndexConstants.ONE_STAR,
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		TYPE,
		getJavaSearchScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"Test$Inner\n" +
		"b123679.test.Test\n" +
		"b123679.test.Test$StaticClass\n" +
		"b123679.test.Test$StaticClass$Member\n" +
		"b124645.test.A_124645\n" +
		"b124645.test.X_124645\n" +
		"b127628.Test127628$Member127628\n" +
		"b95794.Test$Color\n" +
		"pack.age.Test$Member\n" +
		"test.Test$StaticClass\n" +
		"test.Test$StaticClass$Member",
		requestor);
}
public void testBug92264d() throws CoreException {
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine().searchAllTypeNames(
		"b12*".toCharArray(),
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		new char[] { 'X' },
		SearchPattern.R_PREFIX_MATCH, // case insensitive
		TYPE,
		getJavaSearchScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"b124645.test.X_124645\n" +
		"b124645.xy.X_124645",
		requestor);
}

/**
 * @bug 92944: [1.5][search] SearchEngine#searchAllTypeNames doesn't honor enum or annotation element kind
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=92944"
 */
private void setUpBug92944() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b92944/Test.java",
		"package b92944;\n" +
		"interface B92944_I {}\n" +
		"enum B92944_E {}\n" +
		"@interface B92944_A {}\n" +
		"public class B92944 {}\n"
	);
}
public void testBug92944_TYPE() throws CoreException {
	this.resultCollector.showRule();
	setUpBug92944();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		null,
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		TYPE,
		getJavaSearchScopeBugs("b92944", true),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"b92944.B92944\n" +
		"b92944.B92944_A\n" +
		"b92944.B92944_E\n" +
		"b92944.B92944_I",
		requestor);
}
public void testBug92944_CLASS() throws CoreException {
	this.resultCollector.showRule();
	setUpBug92944();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		IIndexConstants.ONE_STAR,
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		CLASS,
		getJavaSearchWorkingCopiesScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	// Remove interface, enum and annotation
	assertSearchResults(
		"Unexpected all type names",
		"b92944.B92944",
		requestor);
}
public void testBug92944_CLASS_AND_INTERFACE() throws CoreException {
	this.resultCollector.showRule();
	setUpBug92944();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		null,
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		CLASS_AND_INTERFACE,
		getJavaSearchWorkingCopiesScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	// Remove enum and annotation
	assertSearchResults(
		"Unexpected all type names",
		"b92944.B92944\n" +
		"b92944.B92944_I",  // Annotation is an interface in java.lang
		requestor);
}
public void testBug92944_CLASS_AND_ENUM() throws CoreException {
	this.resultCollector.showRule();
	setUpBug92944();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		null,
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		CLASS_AND_ENUM,
		getJavaSearchWorkingCopiesScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	// Remove interface and annotation
	assertSearchResults(
		"Unexpected all type names",
		"b92944.B92944\n" +
		"b92944.B92944_E",
		requestor);
}
public void testBug92944_INTERFACE() throws CoreException {
	this.resultCollector.showRule();
	setUpBug92944();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		null,
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		INTERFACE,
		getJavaSearchWorkingCopiesScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"b92944.B92944_I",
		requestor);
}
public void testBug92944_ENUM() throws CoreException {
	this.resultCollector.showRule();
	setUpBug92944();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		null,
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		ENUM,
		getJavaSearchWorkingCopiesScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"b92944.B92944_E",
		requestor);
}
public void testBug92944_ANNOTATION_TYPE() throws CoreException {
	this.resultCollector.showRule();
	setUpBug92944();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		null,
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		ANNOTATION_TYPE,
		getJavaSearchWorkingCopiesScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"b92944.B92944_A",
		requestor);
}

/**
 * @bug 93392: [1.5][search][annot] search for annotation elements does not seem to be implemented yet
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=93392"
 *
 * Note that this test valid also:
 * @bug 94062: [1.5][search][annot] search for annotation elements incorrect match range
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=94062"
 */
public void testBug93392() throws CoreException {
	TestCollector collector = new TestCollector();
	collector.showAccuracy(true);
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b93392/Test.java",
		"package b93392;\n" +
		"@interface Annot {\n" +
		"	int value();\n" +
		"}\n" +
		"@Annot(41)\n" +
		"public class Test {\n" +
		"	@Annot(21)\n" +
		"	int bar;\n" +
		"	@Annot(10)\n" +
		"	public void foo() {}\n" +
		"}\n"
	);
	IType type = selectType(this.workingCopies[0], "Annot");
	IMethod method = type.getMethod("value", new String[0]);
	search(method, REFERENCES, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/b93392/Test.java b93392.Test [41] EXACT_MATCH\n" +
		"src/b93392/Test.java b93392.Test.bar [21] EXACT_MATCH\n" +
		"src/b93392/Test.java void b93392.Test.foo() [10] EXACT_MATCH",
		collector
	);
	// Verify matches positions
	String source = this.workingCopies[0].getSource();
	String str = "@Annot(";
	// First match
	assertEquals("Invalid number of matches", 3, collector.matches.size());
	int index= source.indexOf(str)+str.length();
	assertEquals("Invalid offset for first match", index, ((SearchMatch)collector.matches.get(0)).getOffset());
	assertEquals("Invalid length for first match", 2, ((SearchMatch)collector.matches.get(0)).getLength());
	// Second match
	index= source.indexOf(str, index)+str.length();
	assertEquals("Invalid offset for second match", index, ((SearchMatch)collector.matches.get(1)).getOffset());
	assertEquals("Invalid length for second match", 2, ((SearchMatch)collector.matches.get(1)).getLength());
	// Last match
	index= source.indexOf(str, index)+str.length();
	assertEquals("Invalid offset for last match", index, ((SearchMatch)collector.matches.get(2)).getOffset());
	assertEquals("Invalid length for last match", 2, ((SearchMatch)collector.matches.get(2)).getLength());
}

/**
 * @bug 94160: [1.5][search] Generic method in superclass does not exist
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=94160"
 */
public void testBug94160() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b94160/Test.java",
		"package b94160;\n" +
		"public class Test {\n" +
		"	<T> List<T> generateList(Class<T> clazz) {\n" +
		"		return new ArrayList<T>();\n" +
		"	}\n" +
		"}\n" +
		"class CTest extends Test {\n" +
		"	private List<String> myList = generateList(String.class);\n" +
		"	CTest() {\n" +
		"		myList = new ArrayList<String>();\n" +
		"	}\n" +
		"}\n" +
		"interface List<E> {}\n" +
		"class ArrayList<E> implements List<E> {}"
	);
	IType type = this.workingCopies[0].getType("CTest");
	IField field = type.getField("myList");
	new SearchEngine(this.workingCopies).searchDeclarationsOfSentMessages(field, this.resultCollector, null);
	assertSearchResults(
		"src/b94160/Test.java List<T> b94160.Test.generateList(Class<T>) [generateList(Class<T> clazz)] EXACT_MATCH"
	);
}

/**
 * @bug 94389: [search] InvocationTargetException on Rename
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=94389"
 */
public void testBug94389() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b94389/Test.java",
		"package b94389;\n" +
		"public class Test {\n" +
		"	public void foo() {}\n" +
		"	public void foo() {}\n" +
		"	public void foo() {}\n" +
		"	public void foo() {}\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("Test");
	IMethod[] methods = type.getMethods();
	int methodsLength = methods.length;

	// Perform search on each duplicate method
	IJavaSearchScope scope = getJavaSearchScope();
	for (int m=0; m<methodsLength; m++) {

		// Search method declaration
		TestCollector collector = new TestCollector();
		search(methods[m], DECLARATIONS, scope, collector);
		assertSearchResults(
			"src/b94389/Test.java void b94389.Test.foo() [foo]\n" +
			"src/b94389/Test.java void b94389.Test.foo() [foo]\n" +
			"src/b94389/Test.java void b94389.Test.foo() [foo]\n" +
			"src/b94389/Test.java void b94389.Test.foo() [foo]",
			collector
		);

		// Verify that all matches have correct occurence count
		int size = collector.matches.size();
		for (int i=0; i<size; i++) {
			assertEquals("Invalid foo method occurence count (m="+(m+1)+")", i+1, ((SourceMethod) methods[i]).occurrenceCount);
		}
	}
}

/**
 * @bug 94718: [1.5][search][annot] Find references in workspace breaks on an annotation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=94718"
 */
public void testBug94718() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b94718/SetUp.java",
		"package b94718;\n" +
		"public @interface SetUp {\n" +
		"	String value() {}\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b94718/Test.java",
		"package b94718;\n" +
		"@SetUp(\"howdy\")\n" +
		"public class Test {\n" +
		"}\n"
	);
	IType type = this.workingCopies[1].getType("SetUp");
	search(type, REFERENCES, SearchEngine.createWorkspaceScope());
	assertSearchResults(
		"src/b94718/Test.java b94718.Test [SetUp] EXACT_MATCH"
	);
}

/**
 * @bug 95152: [search] Field references not found when type is a qualified member type [regression]
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=95152"
 */
public void testBug95152_jar01() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b95152.jar", "b95152").getClassFile("T1$T12.class").getType();
	// search constructor first level member
	search(type.getMethods()[0], REFERENCES);
	type = getPackageFragment("JavaSearchBugs", "lib/b95152.jar", "b95152").getClassFile("T1$T12$T13.class").getType();
	// search constructor second level member
	search(type.getMethods()[0], REFERENCES);
	assertSearchResults(
		"lib/b95152.jar b95152.T1() EXACT_MATCH\n" +
		"lib/b95152.jar b95152.T1() EXACT_MATCH"
	);
}
public void testBug95152_jar02() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b95152.jar", "b95152").getClassFile("T2$T22.class").getType();
	// search constructor first level member
	search(type.getMethods()[0], REFERENCES);
	type = getPackageFragment("JavaSearchBugs", "lib/b95152.jar", "b95152").getClassFile("T2$T22$T23.class").getType();
	// search constructor second level member
	search(type.getMethods()[0], REFERENCES);
	assertSearchResults(
		"lib/b95152.jar b95152.T2(int) EXACT_MATCH\n" +
		"lib/b95152.jar b95152.T2(int) EXACT_MATCH"
	);
}
public void testBug95152_jar03() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b95152.jar", "b95152").getClassFile("T3$T32.class").getType();
	// search constructor first level member
	search(type.getMethods()[0], REFERENCES);
	type = getPackageFragment("JavaSearchBugs", "lib/b95152.jar", "b95152").getClassFile("T3$T32$T33.class").getType();
	// search constructor second level member
	search(type.getMethods()[0], REFERENCES);
	assertSearchResults(
		"lib/b95152.jar b95152.T3(b95152.T3) EXACT_MATCH\n" +
		"lib/b95152.jar b95152.T3(b95152.T3) EXACT_MATCH"
	);
}
public void testBug95152_jar04() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b95152.jar", "b95152").getClassFile("T4$T42.class").getType();
	// search constructor first level member
	search(type.getMethods()[0], REFERENCES);
	type = getPackageFragment("JavaSearchBugs", "lib/b95152.jar", "b95152").getClassFile("T4$T42$T43.class").getType();
	// search constructor second level member
	search(type.getMethods()[0], REFERENCES);
	assertSearchResults(
		"lib/b95152.jar b95152.T4(b95152.T4, java.lang.String) EXACT_MATCH\n" +
		"lib/b95152.jar b95152.T4(b95152.T4, java.lang.String) EXACT_MATCH"
	);
}
public void testBug95152_wc01() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b95152/T.java",
		"package b95152;\n" +
		"public class T {\n" +
		"	T2 c2;\n" +
		"	T2.T3 c3;\n" +
		"	T() {\n" +
		"		c2 = new T2();\n" +
		"		c3 = c2.new T3();\n" +
		"	}\n" +
		"	class T2 {\n" +
		"		T2() {}\n" +
		"		class T3 {\n" +
		"			T3() {}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
	// search constructor first level member
	IType type = this.workingCopies[0].getType("T").getType("T2");
	search(type.getMethods()[0], REFERENCES);
	// search constructor second level member
	type = type.getType("T3");
	search(type.getMethods()[0], REFERENCES);
	// verify searches results
	assertSearchResults(
		"src/b95152/T.java b95152.T() [new T2()] EXACT_MATCH\n" +
		"src/b95152/T.java b95152.T() [c2.new T3()] EXACT_MATCH"
	);
}
public void testBug95152_wc02() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b95152/T.java",
		"package b95152;\n" +
		"public class T {\n" +
		"	T2 c2;\n" +
		"	T2.T3 c3;\n" +
		"	T(int c) {\n" +
		"		c2 = new T2(c);\n" +
		"		c3 = c2.new T3(c);\n" +
		"	}\n" +
		"	class T2 {\n" +
		"		T2(int x) {}\n" +
		"		class T3 {\n" +
		"			T3(int x) {}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
	// search constructor first level member
	IType type = this.workingCopies[0].getType("T").getType("T2");
	search(type.getMethods()[0], REFERENCES);
	// search constructor second level member
	type = type.getType("T3");
	search(type.getMethods()[0], REFERENCES);
	// verify searches results
	assertSearchResults(
		"src/b95152/T.java b95152.T(int) [new T2(c)] EXACT_MATCH\n" +
		"src/b95152/T.java b95152.T(int) [c2.new T3(c)] EXACT_MATCH"
	);
}
public void testBug95152_wc03() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b95152/T.java",
		"package b95152;\n" +
		"public class T {\n" +
		"	T2 c2;\n" +
		"	T2.T3 c3;\n" +
		"	T(T c) {\n" +
		"		c2 = new T2(c);\n" +
		"		c3 = c2.new T3(c2);\n" +
		"	}\n" +
		"	class T2 {\n" +
		"		T2(T c) {}\n" +
		"		class T3 {\n" +
		"			T3(T2 c) {}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
	// search constructor first level member
	IType type = this.workingCopies[0].getType("T").getType("T2");
	search(type.getMethods()[0], REFERENCES);
	// search constructor second level member
	type = type.getType("T3");
	search(type.getMethods()[0], REFERENCES);
	// verify searches results
	assertSearchResults(
		"src/b95152/T.java b95152.T(T) [new T2(c)] EXACT_MATCH\n" +
		"src/b95152/T.java b95152.T(T) [c2.new T3(c2)] EXACT_MATCH"
	);
}
public void testBug95152_wc04() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b95152/T.java",
		"package b95152;\n" +
		"public class T {\n" +
		"	T2 c2;\n" +
		"	T2.T3 c3;\n" +
		"	T(T c, String str) {\n" +
		"		c2 = new T2(c, str);\n" +
		"		c3 = c2.new T3(c2, str);\n" +
		"	}\n" +
		"	class T2 {\n" +
		"		T2(T c, String str) {}\n" +
		"		class T3 {\n" +
		"			T3(T2 c, String str) {}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
	// search constructor first level member
	IType type = this.workingCopies[0].getType("T").getType("T2");
	search(type.getMethods()[0], REFERENCES);
	// search constructor second level member
	type = type.getType("T3");
	search(type.getMethods()[0], REFERENCES);
	// verify searches results
	assertSearchResults(
		"src/b95152/T.java b95152.T(T, String) [new T2(c, str)] EXACT_MATCH\n" +
		"src/b95152/T.java b95152.T(T, String) [c2.new T3(c2, str)] EXACT_MATCH"
	);
}

/**
 * @bug 95794: [1.5][search][annot] Find references in workspace breaks on an annotation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=95794"
 */
public void testBug95794() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearchBugs", "src", "b95794", "Test.java");
	IType type = unit.getType("Test");

	// Verify matches
	TestCollector occurencesCollector = new TestCollector();
	occurencesCollector.showAccuracy(true);
	search(type, ALL_OCCURRENCES, getJavaSearchScope(), occurencesCollector);
	assertSearchResults(
		"src/b95794/Test.java [b95794.Test] EXACT_MATCH\n" +
		"src/b95794/Test.java [b95794.Test] EXACT_MATCH\n" +
		"src/b95794/Test.java b95794.Test [Test] EXACT_MATCH\n" +
		"src/b95794/Test.java b95794.Test.there [Test] EXACT_MATCH",
		occurencesCollector
	);

	// Verify with references matches
	TestCollector referencesCollector = new TestCollector();
	search(type, REFERENCES, getJavaSearchScope(), referencesCollector);
	assertEquals("Problem with occurences or references number of matches: ", occurencesCollector.matches.size()-1, referencesCollector.matches.size());
}
public void testBug95794b() throws CoreException {
	this.resultCollector.showRule();
	ICompilationUnit unit = getCompilationUnit("JavaSearchBugs", "src", "b95794", "Test.java");
	IType type = unit.getType("Test").getType("Color");

	// Verify matches
	TestCollector occurencesCollector = new TestCollector();
	occurencesCollector.showAccuracy(true);
	search(type, ALL_OCCURRENCES, getJavaSearchScope(), occurencesCollector);
	assertSearchResults(
		"src/b95794/Test.java [b95794.Test.Color] EXACT_MATCH\n" +
		"src/b95794/Test.java [b95794.Test.Color] EXACT_MATCH\n" +
		"src/b95794/Test.java void b95794.Test.main(String[]) [Color] EXACT_MATCH\n" +
		"src/b95794/Test.java b95794.Test$Color [Color] EXACT_MATCH",
		occurencesCollector
	);

	// Verify with references matches
	TestCollector referencesCollector = new TestCollector();
	search(type, REFERENCES, getJavaSearchScope(), referencesCollector);
	assertEquals("Problem with occurences or references number of matches: ", occurencesCollector.matches.size()-1, referencesCollector.matches.size());
}
public void testBug95794c() throws CoreException {
	this.resultCollector.showRule();
	ICompilationUnit unit = getCompilationUnit("JavaSearchBugs", "src", "b95794", "Test.java");
	IField field = unit.getType("Test").getType("Color").getField("WHITE");

	// Verify matches
	TestCollector occurencesCollector = new TestCollector();
	occurencesCollector.showAccuracy(true);
	search(field, ALL_OCCURRENCES, getJavaSearchScope(), occurencesCollector);
	assertSearchResults(
		"src/b95794/Test.java [WHITE] EXACT_MATCH\n" +
		"src/b95794/Test.java void b95794.Test.main(String[]) [WHITE] EXACT_MATCH\n" +
		"src/b95794/Test.java b95794.Test$Color.WHITE [WHITE] EXACT_MATCH",
		occurencesCollector
	);

	// Verify with references matches
	TestCollector referencesCollector = new TestCollector();
	search(field, REFERENCES, getJavaSearchScope(), referencesCollector);
	assertEquals("Problem with occurences or references number of matches: ", occurencesCollector.matches.size()-1, referencesCollector.matches.size());
}

/**
 * @bug 96761: [1.5][search] Search for declarations of generic method finds non-overriding method
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=96761"
 */
public void testBug96761() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b96761/Generic.java",
		"package b96761;\n" +
		"public class Generic<G> {\n" +
		"	void take(G g) {\n" +
		"	}\n" +
		"}\n" +
		"class Impl extends Generic<RuntimeException> {\n" +
		"	void take(InterruptedException g) {\n" +
		"	}\n" +
		"	void take(RuntimeException g) {\n" +
		"	}\n" +
		"}"
	);
	IType type = this.workingCopies[0].getType("Generic");
	IMethod method= type.getMethods()[0];
	search(method, REFERENCES);
	assertSearchResults(""); // Expect no result
}

/**
 * @bug 96763: [1.5][search] Search for method declarations does not find overridden method with different signature
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=96763"
 */
public void testBug96763() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b96763/Test.java",
		"package b96763;\n" +
		"class Test<T> {\n" +
		"    public void first(Exception num) {}\n" +
		"    public void second(T t) {}\n" +
		"}\n" +
		"class Sub extends Test<Exception> {\n" +
		"    public void first(Exception num) {}\n" +
		"    public void second(Exception t) {}\n" +
		"}\n"
	);
	IMethod method = this.workingCopies[0].getType("Sub").getMethods()[0];
	search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b96763/Test.java void b96763.Test.first(Exception) [first] EXACT_MATCH\n" +
		"src/b96763/Test.java void b96763.Sub.first(Exception) [first] EXACT_MATCH"
	);
}
public void testBug96763b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b96763/Test.java",
		"package b96763;\n" +
		"class Test<T> {\n" +
		"    public void first(Exception num) {}\n" +
		"    public void second(T t) {}\n" +
		"}\n" +
		"class Sub extends Test<Exception> {\n" +
		"    public void first(Exception num) {}\n" +
		"    public void second(Exception t) {}\n" +
		"}\n"
	);
	IMethod method = this.workingCopies[0].getType("Sub").getMethods()[1];
	search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b96763/Test.java void b96763.Test.second(T) [second] EXACT_MATCH\n" +
		"src/b96763/Test.java void b96763.Sub.second(Exception) [second] EXACT_MATCH"
	);
}
public void testBug96763c() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b96763/Test.java",
		"package b96763;\n" +
		"public class Test<T> {\n" +
		"	void methodT(T t) {}\n" +
		"}\n" +
		"class Sub<X> extends Test<X> {\n" +
		"	void methodT(X x) {} // overrides Super#methodT(T)\n" +
		"}\n"
	);
	IMethod method = this.workingCopies[0].getType("Sub").getMethods()[0];
	search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b96763/Test.java void b96763.Test.methodT(T) [methodT] EXACT_MATCH\n" +
		"src/b96763/Test.java void b96763.Sub.methodT(X) [methodT] EXACT_MATCH"
	);
}

/**
 * @bug 97087: [1.5][search] Can't find reference of generic class's constructor.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=97087"
 */
public void testBug97087() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.resultCollector.showRule();
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b97087/Bug.java",
		"package b97087;\n" +
		"public class Bug<Type> {\n" +
		"    Bug(){}\n" +
		"}\n" +
		"class Foo extends Bug<String>{\n" +
		"    Foo(){}\n" +
		"}\n" +
		"class Bar extends Bug<Exception>{\n" +
		"    Bar(){super();}\n" +
		"}"
	);
	IType type = this.workingCopies[0].getType("Bug");
	IMethod method= type.getMethods()[0];
	search(method, REFERENCES, SearchPattern.R_ERASURE_MATCH);
	assertSearchResults(
		"src/b97087/Bug.java b97087.Foo() [Foo] EXACT_MATCH\n" +
		"src/b97087/Bug.java b97087.Bar() [super();] ERASURE_MATCH"
	);
}

/**
 * @bug 97120:
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=97120"
 */
public void testBug97120() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", getExternalJCLPathString("1.5"), "java.lang", "Throwable.class").getType();
	IJavaSearchScope scope = SearchEngine.createHierarchyScope(type);
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine().searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		"E*".toCharArray(),
		SearchPattern.R_PATTERN_MATCH,
		TYPE,
		scope,
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"java.lang.Error\n" +
		"java.lang.Exception",
		requestor
	);
}

/**
 * @bug 97322: [search] Search for method references sometimes reports potential match with differing argument count
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=97322"
 */
public void testBug97322() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b97322/Test.java",
		"package b97322;\n" +
		"class Test {\n" +
		"	static void myMethod(int a, String b) {}\n" +
		"	void call() {\n" +
		"		myMethod(12);\n" +
		"	}\n" +
		"}"
	);
	IType type = this.workingCopies[0].getType("Test");
	IMethod method= type.getMethods()[0];
	search(method, REFERENCES);
	assertSearchResults(""); // Expect no result
}

/**
 * @bug 97547: [search] Package search does not find references in member types import clause
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=97547"
 */
public void testBug97547() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b97547/IX.java",
		"package b97547;\n" +
		"public interface IX {\n" +
		"	public interface IX1 {}\n" +
		"}"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b97547/X.java",
		"package b97547;\n" +
		"import b97547.IX.*;\n" +
		"class X {\n" +
		"	IX x;\n" +
		"}"
	);
	IPackageDeclaration[] packages = this.workingCopies[0].getPackageDeclarations();
	assertTrue("Invalid number of packages declaration!", packages!=null && packages.length==1);
	search(packages[0], REFERENCES);
	assertSearchResults(
		"src/b97547/X.java [b97547] EXACT_MATCH"
	);
}

/**
 * @bug 97606: [1.5][search] Raw type reference is reported as exact match for qualified names
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=97606"
 */
public void testBug97606() throws CoreException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/def/L.java",
		"package b97606.pack.def;\n" +
		"public interface L<E> {}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/def/LL.java",
		"package b97606.pack.def;\n" +
		"public class LL<E> implements L<E> {\n" +
		"	public Object clone() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/ref/K.java",
		"package b97606.pack.ref;\n" +
		"public interface K {}\n"
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/ref/X.java",
		"package b97606.pack.ref;\n" +
		"public class X implements K {\n" +
		"	private b97606.pack.def.LL sg;\n" +
		"	protected synchronized b97606.pack.def.L<K> getSG() {\n" +
		"		return (sg != null) \n" +
		"			? (b97606.pack.def.L) sg.clone()\n" +
		"			: null;\n" +
		"	}\n" +
		"}\n"
	);
	IPath rootPath = new Path("/JavaSearchBugs/src/b97606");
	IPath pathDef = rootPath.append("pack").append("def");
	IPath pathRef = rootPath.append("pack").append("ref");
	try {
		createFolder(pathDef);
		createFolder(pathRef);
		this.workingCopies[0].commitWorkingCopy(true, null);
		this.workingCopies[1].commitWorkingCopy(true, null);
		this.workingCopies[2].commitWorkingCopy(true, null);
		this.workingCopies[3].commitWorkingCopy(true, null);
		this.resultCollector.showRule();
		IType type = this.workingCopies[0].getType("L");
		search(type, REFERENCES, SearchPattern.R_ERASURE_MATCH);
		assertSearchResults(
			"src/b97606/pack/def/LL.java b97606.pack.def.LL [L] ERASURE_MATCH\n" +
			"src/b97606/pack/ref/X.java b97606.pack.def.L<K> b97606.pack.ref.X.getSG() [b97606.pack.def.L] ERASURE_MATCH\n" +
			"src/b97606/pack/ref/X.java b97606.pack.def.L<K> b97606.pack.ref.X.getSG() [b97606.pack.def.L] ERASURE_RAW_MATCH"
		);
	}
	finally {
		deleteFolder(rootPath);
	}
}
public void testBug97606b() throws CoreException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/def/L.java",
		"package b97606.pack.def;\n" +
		"public interface L<E> {}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/def/LL.java",
		"package b97606.pack.def;\n" +
		"public class LL<E> implements L<E> {\n" +
		"	public Object clone() {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/ref/K.java",
		"package b97606.pack.ref;\n" +
		"public interface K {}\n"
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/ref/X.java",
		"package b97606.pack.ref;\n" +
		"import b97606.pack.def.*;\n" +
		"public class X implements K {\n" +
		"	private LL sg;\n" +
		"	protected synchronized L<K> getSG() {\n" +
		"		return (sg != null) \n" +
		"			? (L) sg.clone()\n" +
		"			: null;\n" +
		"	}\n" +
		"}\n"
	);
	IPath rootPath = new Path("/JavaSearchBugs/src/b97606");
	IPath pathDef = rootPath.append("pack").append("def");
	IPath pathRef = rootPath.append("pack").append("ref");
	try {
		createFolder(pathDef);
		createFolder(pathRef);
		this.workingCopies[0].commitWorkingCopy(true, null);
		this.workingCopies[1].commitWorkingCopy(true, null);
		this.workingCopies[2].commitWorkingCopy(true, null);
		this.workingCopies[3].commitWorkingCopy(true, null);
		this.resultCollector.showRule();
		IType type = this.workingCopies[0].getType("L");
		search(type, REFERENCES, SearchPattern.R_ERASURE_MATCH);
		assertSearchResults(
			"src/b97606/pack/def/LL.java b97606.pack.def.LL [L] ERASURE_MATCH\n" +
			"src/b97606/pack/ref/X.java L<K> b97606.pack.ref.X.getSG() [L] ERASURE_MATCH\n" +
			"src/b97606/pack/ref/X.java L<K> b97606.pack.ref.X.getSG() [L] ERASURE_RAW_MATCH"
		);
	}
	finally {
		deleteFolder(rootPath);
	}
}

/**
 * @bug 97614: [1.5][search] Refactoring: renaming of field of a (complex) parametrized type does not replace all occurrences
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=97614"
 */
public void testBug97614() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b97614/W.java",
		"package b97614;\n" +
		"public class W {\n" +
		"	private final Map<String, Y<?, ? extends b97614.X.XX<?, ?>, ? >> m1 = null;     // (a)\n" +
		"	public void getStore(final Object o) {\n" +
		"		m1.get(o);     // (b)\n" +
		"	}\n" +
		"}\n" +
		"interface Map<K, V> {\n" +
		"	V get(Object k);\n" +
		"}"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b97614/X.java",
		"package b97614;\n" +
		"import java.io.Serializable;\n" +
		"public interface X<T extends X<T, U, V>, \n" +
		"				   U extends X.XX<T, V>, \n" +
		"				   V extends X.XY> {\n" +
		"	public interface XX<TT extends X<TT, ?, UU>, \n" +
		"	                   UU extends X.XY> \n" +
		"			extends	Serializable {\n" +
		"	}\n" +
		"	public interface XY extends Serializable {\n" +
		"	}\n" +
		"}"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b97614/Y.java",
		"package b97614;\n" +
		"public class Y<T extends X<T, U, V>, U extends X.XX<T, V>, V extends X.XY> {\n" +
		"}\n"
	);
	IField field = this.workingCopies[0].getType("W").getField("m1");
	search(field, REFERENCES);
	assertSearchResults(
		"src/b97614/W.java void b97614.W.getStore(Object) [m1] EXACT_MATCH"
	);
}

/**
 * @bug 98378: [search] does not find method references in anonymous class of imported jarred plugin
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=98378"
 */
public void testBug98378() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b98378/X.java",
		"package b98378;\n" +
		"public class  X implements java.lang.CharSequence {\n" +
		"	public int length() {\n" +
		"		return 1;\n" +
		"	}\n" +
		"}"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b98378/Y.java",
		"package b98378;\n" +
		"public class Y {\n" +
		"	public int length() {\n" +
		"		return -1;\n" +
		"	}\n" +
		"}\n"
	);
	String jclPath = getExternalJCLPathString("1.5");
	IType type = getClassFile("JavaSearchBugs", jclPath, "java.lang", "CharSequence.class").getType();
	IMethod method = type.getMethod("length", new String[] {});
	search(method, DECLARATIONS, SearchEngine.createHierarchyScope(type, this.wcOwner));
	assertSearchResults(
		jclPath + " int java.lang.CharSequence.length() EXACT_MATCH\n" +
		jclPath + " int java.lang.String.length() EXACT_MATCH"
	);
}
public void testBug98378b() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b98378/X.java",
		"package b98378;\n" +
		"public class  X implements java.lang.CharSequence {\n" +
		"	public int length() {\n" +
		"		return 1;\n" +
		"	}\n" +
		"}"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b98378/Y.java",
		"package b98378;\n" +
		"public class Y {\n" +
		"	public int length() {\n" +
		"		return -1;\n" +
		"	}\n" +
		"}\n"
	);
	String jclPath = getExternalJCLPathString("1.5");
	IType type = getClassFile("JavaSearchBugs", jclPath, "java.lang", "CharSequence.class").getType();
	IMethod method = type.getMethod("length", new String[] {});
	search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE, SearchEngine.createHierarchyScope(type, this.wcOwner));
	assertSearchResults(
		"src/b98378/X.java int b98378.X.length() [length] EXACT_MATCH\n" +
		jclPath + " int java.lang.CharSequence.length() EXACT_MATCH\n" +
		jclPath + " int java.lang.String.length() EXACT_MATCH"
	);
}

/**
 * @bug 99600: [search] Java model exception on "Move to new file" on inner type with inner type
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=99600"
 */
public void testBug99600() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b99600/Test.java",
		"package b99600;\n" +
		"public class Test {\n" +
		"	public class C1 {}\n" +
		"	public class C2 {\n" +
		"		class C3 {\n" +
		"			int foo(C1 c) { return 0; }\n" +
		"		}\n" +
		"		public void foo(C1 c, int i) {\n" +
		"			new C3().foo(c);\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("Test").getType("C2");
	new SearchEngine(this.workingCopies).searchDeclarationsOfSentMessages(type, this.resultCollector, null);
	assertSearchResults(
		"src/b99600/Test.java int b99600.Test$C2$C3.foo(C1) [foo(C1 c)] EXACT_MATCH"
	);
}

/**
 * @bug 99903: [1.5][search] range wrong for package-info
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=99903"
 */
public void testBug99903_annotation() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b99903/package-info.java",
		"/**\n" +
		" * @see Test\n" +
		" */\n" +
		"@Annot\n" +
		"package b99903;\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b99903/Test.java",
		"package b99903;\n" +
		"public class Test {\n" +
		"}\n"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b99903/Annot.java",
		"package b99903;\n" +
		"public @interface Annot {\n" +
		"}\n"
	);
	IType type = this.workingCopies[2].getType("Annot");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b99903/package-info.java [Annot] EXACT_MATCH"
	);
}
public void testBug99903_javadoc() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b99903/package-info.java",
		"/**\n" +
		" * @see Test\n" +
		" */\n" +
		"@Annot\n" +
		"package b99903;\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b99903/Test.java",
		"package b99903;\n" +
		"public class Test {\n" +
		"}\n"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b99903/Annot.java",
		"package b99903;\n" +
		"public @interface Annot {\n" +
		"}\n"
	);
	this.resultCollector.showInsideDoc();
	IType type = this.workingCopies[1].getType("Test");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b99903/package-info.java [Test] EXACT_MATCH INSIDE_JAVADOC"
	);
}

/**
 * @bug 100695: [1.5][search] Renaming a field of generic array type has no effect
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=100695"
 */
public void testBug100695() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100695/Test.java",
		"package b100695;\n" +
		"public class Test {\n" +
		"	Class<Class>[] foo;\n" +
		"	Class<Class>[] bar = foo;\n" +
		"	Test() {\n" +
		"		foo = null;\n" +
		"	}\n" +
		"}\n"
	);
	IField field = this.workingCopies[0].getType("Test").getField("foo");
	search(field, REFERENCES);
	assertSearchResults(
		"src/b100695/Test.java b100695.Test.bar [foo] EXACT_MATCH\n" +
		"src/b100695/Test.java b100695.Test() [foo] EXACT_MATCH"
	);
}
public void testBug100695a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100695/Test.java",
		"package b100695;\n" +
		"public class Test {\n" +
		"	Class<Class>[] foo;\n" +
		"	Class<Class>[] bar = foo;\n" +
		"	Test() {\n" +
		"		foo = null;\n" +
		"	}\n" +
		"}\n"
	);
	IField field = this.workingCopies[0].getType("Test").getField("foo");
	search(field, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b100695/Test.java b100695.Test.foo [foo] EXACT_MATCH\n" +
		"src/b100695/Test.java b100695.Test.bar [foo] EXACT_MATCH\n" +
		"src/b100695/Test.java b100695.Test() [foo] EXACT_MATCH"
	);
}
public void testBug100695b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100695/Test.java",
		"package b100695;\n" +
		"public class Test {\n" +
		"	Class<Class> foo;\n" +
		"	Class<Class> bar = foo;\n" +
		"	Test() {\n" +
		"		foo = null;\n" +
		"	}\n" +
		"}\n"
	);
	IField field = this.workingCopies[0].getType("Test").getField("foo");
	search(field, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b100695/Test.java b100695.Test.foo [foo] EXACT_MATCH\n" +
		"src/b100695/Test.java b100695.Test.bar [foo] EXACT_MATCH\n" +
		"src/b100695/Test.java b100695.Test() [foo] EXACT_MATCH"
	);
}
public void testBug100695c() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100695/Test.java",
		"package b100695;\n" +
		"public class Test {\n" +
		"	Class[] foo;\n" +
		"	Class[] bar = foo;\n" +
		"	Test() {\n" +
		"		foo = null;\n" +
		"	}\n" +
		"}\n"
	);
	IField field = this.workingCopies[0].getType("Test").getField("foo");
	search(field, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b100695/Test.java b100695.Test.foo [foo] EXACT_MATCH\n" +
		"src/b100695/Test.java b100695.Test.bar [foo] EXACT_MATCH\n" +
		"src/b100695/Test.java b100695.Test() [foo] EXACT_MATCH"
	);
}
public void testBug100695d() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100695/Test.java",
		"package b100695;\n" +
		"public class Test {\n" +
		"	  public Class<Class>[] foo(Class<Class>[] a) {\n" +
		"		  return a;\n" +
		"	  }\n" +
		"	  void bar() {\n" +
		"		  foo(new Class[0]);\n" +
		"	  }\n" +
		"}\n"
	);
	IMethod method = this.workingCopies[0].getType("Test").getMethods()[0];
	search(method, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b100695/Test.java Class<Class>[] b100695.Test.foo(Class<Class>[]) [foo] EXACT_MATCH\n" +
		"src/b100695/Test.java void b100695.Test.bar() [foo(new Class[0])] EXACT_MATCH"
	);
}
public void testBug100695e() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100695/Test.java",
		"package b100695;\n" +
		"public class Test {\n" +
		"	  public Class<Class> foo(Class<Class> a) {\n" +
		"		  return a;\n" +
		"	  }\n" +
		"	  void bar() {\n" +
		"		  foo(null);\n" +
		"	  }\n" +
		"}\n"
	);
	IMethod method = this.workingCopies[0].getType("Test").getMethods()[0];
	search(method, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b100695/Test.java Class<Class> b100695.Test.foo(Class<Class>) [foo] EXACT_MATCH\n" +
		"src/b100695/Test.java void b100695.Test.bar() [foo(null)] EXACT_MATCH"
	);
}
public void testBug100695f() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100695/Test.java",
		"package b100695;\n" +
		"public class Test {\n" +
		"	  public Class[] foo(Class[] a) {\n" +
		"		  return a;\n" +
		"	  }\n" +
		"	  void bar() {\n" +
		"		  foo(new Class[0]);\n" +
		"	  }\n" +
		"}\n"
	);
	IMethod method = this.workingCopies[0].getType("Test").getMethods()[0];
	search(method, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b100695/Test.java Class[] b100695.Test.foo(Class[]) [foo] EXACT_MATCH\n" +
		"src/b100695/Test.java void b100695.Test.bar() [foo(new Class[0])] EXACT_MATCH"
	);
}

/**
 * @bug 100772: [1.5][search] Search for declarations in hierarchy reports to many matches
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=100772"
 */
private void setUpBug100772_HierarchyScope_ClassAndSubclass() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100772/Test.java",
		"package b100772;\n" +
		"class Test<T> {\n" +
		"    public void foo(T t) {}\n" +
		"    public void foo(Class c) {}\n" +
		"}\n" +
		"class Sub extends Test<String> {\n" +
		"    public void foo(String str) {}\n" +
		"    public void foo(Exception e) {}\n" +
		"}\n"
	);
}
public void testBug100772_HierarchyScope_ClassAndSubclass01() throws CoreException {
	setUpBug100772_HierarchyScope_ClassAndSubclass();
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.Sub.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_ClassAndSubclass02() throws CoreException {
	setUpBug100772_HierarchyScope_ClassAndSubclass();
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.Sub.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_ClassAndSubclass03() throws CoreException {
	setUpBug100772_HierarchyScope_ClassAndSubclass();
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QClass;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(Class) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_ClassAndSubclass04() throws CoreException {
	setUpBug100772_HierarchyScope_ClassAndSubclass();
	IType type = this.workingCopies[0].getType("Sub");
	IMethod method = type.getMethod("foo", new String[] { "QString;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.Sub.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_ClassAndSubclass05() throws CoreException {
	setUpBug100772_HierarchyScope_ClassAndSubclass();
	IType type = this.workingCopies[0].getType("Sub");
	IMethod method = type.getMethod("foo", new String[] { "QException;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Sub.foo(Exception) [foo] EXACT_MATCH"
	);
}
private void setUpBug100772_HierarchyScope_InterfacesAndClass() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100772/Test.java",
		"package b100772;\n" +
		"interface Test<T> {\n" +
		"    public void foo(T t);\n" +
		"    public void foo(Class c);\n" +
		"}\n" +
		"interface Sub extends Test<String> {\n" +
		"    public void foo(String str);\n" +
		"    public void foo(Exception e);\n" +
		"}\n" +
		"class X implements Test<String> {\n" +
		"    public void foo(String str) {}\n" +
		"    public void foo(Class c) {}\n" +
		"    public void foo(Exception e) {}\n" +
		"}\n"
	);
}
public void testBug100772_HierarchyScope_InterfacesAndClass01() throws CoreException {
	setUpBug100772_HierarchyScope_InterfacesAndClass();
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.Sub.foo(String) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.X.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_InterfacesAndClass02() throws CoreException {
	setUpBug100772_HierarchyScope_InterfacesAndClass();
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.Sub.foo(String) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.X.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_InterfacesAndClass03() throws CoreException {
	setUpBug100772_HierarchyScope_InterfacesAndClass();
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QClass;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(Class) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.X.foo(Class) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_InterfacesAndClass04() throws CoreException {
	setUpBug100772_HierarchyScope_InterfacesAndClass();
	IType type = this.workingCopies[0].getType("Sub");
	IMethod method = type.getMethod("foo", new String[] { "QString;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.Sub.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_InterfacesAndClass05() throws CoreException {
	setUpBug100772_HierarchyScope_InterfacesAndClass();
	IType type = this.workingCopies[0].getType("Sub");
	IMethod method = type.getMethod("foo", new String[] { "QException;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Sub.foo(Exception) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_InterfacesAndClass06() throws CoreException {
	setUpBug100772_HierarchyScope_InterfacesAndClass();
	IType type = this.workingCopies[0].getType("X");
	IMethod method = type.getMethod("foo", new String[] { "QString;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.X.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_InterfacesAndClass07() throws CoreException {
	setUpBug100772_HierarchyScope_InterfacesAndClass();
	IType type = this.workingCopies[0].getType("X");
	IMethod method = type.getMethod("foo", new String[] { "QClass;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(Class) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.X.foo(Class) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_InterfacesAndClass08() throws CoreException {
	setUpBug100772_HierarchyScope_InterfacesAndClass();
	IType type = this.workingCopies[0].getType("X");
	IMethod method = type.getMethod("foo", new String[] { "QException;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.X.foo(Exception) [foo] EXACT_MATCH"
	);
}
private void setUpBug100772_HierarchyScope_Complex() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100772/Test.java",
		"package b100772;\n" +
		"public class X<T> implements IX<T> {\n" +
		"	public void foo(T t) {}\n" +
		"}\n" +
		"class XX extends X<String> {\n" +
		"	public void foo(String s) {}\n" +
		"	public void foo(Exception e) {}\n" +
		"}\n" +
		"interface IX<U> {\n" +
		"	public void foo(U u);\n" +
		"}\n" +
		"class Y implements IX<String> {\n" +
		"	public void foo(String s) {}\n" +
		"	public void foo(Exception e) {}\n" +
		"}\n" +
		"interface IXX<V extends Exception> {\n" +
		"	public void foo(V v);\n" +
		"}\n" +
		"class Z extends Y implements IXX<Exception> {\n" +
		"	public void foo(String s) {}\n" +
		"	public void foo(Exception e) {}\n" +
		"}\n"
	);
}
public void testBug100772_HierarchyScope_Complex01() throws CoreException {
	setUpBug100772_HierarchyScope_Complex();
	IType type = this.workingCopies[0].getType("IX");
	IMethod method = type.getMethod("foo", new String[] { "QU;" });
	search(method, DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.X.foo(T) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.XX.foo(String) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.IX.foo(U) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.Y.foo(String) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.Z.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_Complex02() throws CoreException {
	setUpBug100772_HierarchyScope_Complex();
	IType type = this.workingCopies[0].getType("Z");
	IMethod method = type.getMethod("foo", new String[] { "QString;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.IX.foo(U) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.Y.foo(String) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.Z.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_Complex03() throws CoreException {
	setUpBug100772_HierarchyScope_Complex();
	IType type = this.workingCopies[0].getType("Z");
	IMethod method = type.getMethod("foo", new String[] { "QException;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Y.foo(Exception) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.IXX.foo(V) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.Z.foo(Exception) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_Complex04() throws CoreException {
	setUpBug100772_HierarchyScope_Complex();
	IType type = this.workingCopies[0].getType("X");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.X.foo(T) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.XX.foo(String) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.IX.foo(U) [foo] EXACT_MATCH"
	);
}
private void setUpBug100772_ProjectScope_ClassAndSubclass() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100772/Test.java",
		"package b100772;\n" +
		"class Test<T> {\n" +
		"    public void foo(T t) {}\n" +
		"    public void foo(Class c) {}\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b100772/Sub.java",
		"package b100772;\n" +
		"class Sub extends Test<String> {\n" +
		"    public void foo(String str) {}\n" +
		"    public void foo(Exception e) {}\n" +
		"}\n"
	);
}
public void testBug100772_ProjectScope_ClassAndSubclass01() throws CoreException {
	setUpBug100772_ProjectScope_ClassAndSubclass();
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Sub.java void b100772.Sub.foo(String) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_ClassAndSubclass02() throws CoreException {
	setUpBug100772_ProjectScope_ClassAndSubclass();
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, DECLARATIONS);
	assertSearchResults(
		"src/b100772/Sub.java void b100772.Sub.foo(String) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_ClassAndSubclass03() throws CoreException {
	setUpBug100772_ProjectScope_ClassAndSubclass();
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QClass;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(Class) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_ClassAndSubclass04() throws CoreException {
	setUpBug100772_ProjectScope_ClassAndSubclass();
	IType type = this.workingCopies[1].getType("Sub");
	IMethod method = type.getMethod("foo", new String[] { "QString;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Sub.java void b100772.Sub.foo(String) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_ClassAndSubclass05() throws CoreException {
	setUpBug100772_ProjectScope_ClassAndSubclass();
	IType type = this.workingCopies[1].getType("Sub");
	IMethod method = type.getMethod("foo", new String[] { "QException;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Sub.java void b100772.Sub.foo(Exception) [foo] EXACT_MATCH"
	);
}
private void setUpBug100772_ProjectScope_InterfacesAndClass() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100772/Test.java",
		"package b100772;\n" +
		"interface Test<T> {\n" +
		"    public void foo(T t);\n" +
		"    public void foo(Class c);\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b100772/Sub.java",
		"package b100772;\n" +
		"interface Sub extends Test<String> {\n" +
		"    public void foo(String str);\n" +
		"    public void foo(Exception e);\n" +
		"}\n"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b100772/X.java",
		"package b100772;\n" +
		"class X implements Test<String> {\n" +
		"    public void foo(String str) {}\n" +
		"    public void foo(Class c) {}\n" +
		"    public void foo(Exception e) {}\n" +
		"}\n"
	);
}
public void testBug100772_ProjectScope_InterfacesAndClass01() throws CoreException {
	setUpBug100772_ProjectScope_InterfacesAndClass();
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Sub.java void b100772.Sub.foo(String) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH\n" +
		"src/b100772/X.java void b100772.X.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_InterfacesAndClass02() throws CoreException {
	setUpBug100772_ProjectScope_InterfacesAndClass();
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, DECLARATIONS);
	assertSearchResults(
		"src/b100772/Sub.java void b100772.Sub.foo(String) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH\n" +
		"src/b100772/X.java void b100772.X.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_InterfacesAndClass03() throws CoreException {
	setUpBug100772_ProjectScope_InterfacesAndClass();
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QClass;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(Class) [foo] EXACT_MATCH\n" +
		"src/b100772/X.java void b100772.X.foo(Class) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_InterfacesAndClass04() throws CoreException {
	setUpBug100772_ProjectScope_InterfacesAndClass();
	IType type = this.workingCopies[1].getType("Sub");
	IMethod method = type.getMethod("foo", new String[] { "QString;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Sub.java void b100772.Sub.foo(String) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH\n" +
		"src/b100772/X.java void b100772.X.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_InterfacesAndClass05() throws CoreException {
	setUpBug100772_ProjectScope_InterfacesAndClass();
	IType type = this.workingCopies[1].getType("Sub");
	IMethod method = type.getMethod("foo", new String[] { "QException;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Sub.java void b100772.Sub.foo(Exception) [foo] EXACT_MATCH\n" +
		"src/b100772/X.java void b100772.X.foo(Exception) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_InterfacesAndClass06() throws CoreException {
	setUpBug100772_ProjectScope_InterfacesAndClass();
	IType type = this.workingCopies[2].getType("X");
	IMethod method = type.getMethod("foo", new String[] { "QString;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Sub.java void b100772.Sub.foo(String) [foo] EXACT_MATCH\n" +
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH\n" +
		"src/b100772/X.java void b100772.X.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_InterfacesAndClass07() throws CoreException {
	setUpBug100772_ProjectScope_InterfacesAndClass();
	IType type = this.workingCopies[2].getType("X");
	IMethod method = type.getMethod("foo", new String[] { "QClass;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(Class) [foo] EXACT_MATCH\n" +
		"src/b100772/X.java void b100772.X.foo(Class) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_InterfacesAndClass08() throws CoreException {
	setUpBug100772_ProjectScope_InterfacesAndClass();
	IType type = this.workingCopies[2].getType("X");
	IMethod method = type.getMethod("foo", new String[] { "QException;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Sub.java void b100772.Sub.foo(Exception) [foo] EXACT_MATCH\n" +
		"src/b100772/X.java void b100772.X.foo(Exception) [foo] EXACT_MATCH"
	);
}
private void setUpBug100772_ProjectScope_Complex() throws CoreException {
	this.workingCopies = new ICompilationUnit[6];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100772/X.java",
		"package b100772;\n" +
		"public class X<T> implements IX<T> {\n" +
		"	public void foo(T t) {}\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b100772/XX.java",
		"package b100772;\n" +
		"class XX extends X<String> {\n" +
		"	public void foo(String s) {}\n" +
		"	public void foo(Exception e) {}\n" +
		"}\n"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b100772/IX.java",
		"package b100772;\n" +
		"interface IX<U> {\n" +
		"	public void foo(U u);\n" +
		"}\n"
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b100772/Y.java",
		"package b100772;\n" +
		"class Y implements IX<String> {\n" +
		"	public void foo(String s) {}\n" +
		"	public void foo(Exception e) {}\n" +
		"}\n"
	);
	this.workingCopies[4] = getWorkingCopy("/JavaSearchBugs/src/b100772/IXX.java",
		"package b100772;\n" +
		"interface IXX<V extends Exception> {\n" +
		"	public void foo(V v);\n" +
		"}\n"
	);
	this.workingCopies[5] = getWorkingCopy("/JavaSearchBugs/src/b100772/Z.java",
		"package b100772;\n" +
		"class Z extends Y implements IXX<Exception> {\n" +
		"	public void foo(String s) {}\n" +
		"	public void foo(Exception e) {}\n" +
		"}\n"
	);
}
public void testBug100772_ProjectScope_Complex01() throws CoreException {
	setUpBug100772_ProjectScope_Complex();
	IType type = this.workingCopies[2].getType("IX");
	IMethod method = type.getMethod("foo", new String[] { "QU;" });
	search(method, DECLARATIONS);
	assertSearchResults(
		"src/b100772/IX.java void b100772.IX.foo(U) [foo] EXACT_MATCH\n" +
		"src/b100772/X.java void b100772.X.foo(T) [foo] EXACT_MATCH\n" +
		"src/b100772/XX.java void b100772.XX.foo(String) [foo] EXACT_MATCH\n" +
		"src/b100772/Y.java void b100772.Y.foo(String) [foo] EXACT_MATCH\n" +
		"src/b100772/Z.java void b100772.Z.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_Complex02() throws CoreException {
	setUpBug100772_ProjectScope_Complex();
	IType type = this.workingCopies[5].getType("Z");
	IMethod method = type.getMethod("foo", new String[] { "QString;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/IX.java void b100772.IX.foo(U) [foo] EXACT_MATCH\n" +
		"src/b100772/XX.java void b100772.XX.foo(String) [foo] EXACT_MATCH\n" +
		"src/b100772/Y.java void b100772.Y.foo(String) [foo] EXACT_MATCH\n" +
		"src/b100772/Z.java void b100772.Z.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_Complex03() throws CoreException {
	setUpBug100772_ProjectScope_Complex();
	IType type = this.workingCopies[5].getType("Z");
	IMethod method = type.getMethod("foo", new String[] { "QException;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/IXX.java void b100772.IXX.foo(V) [foo] EXACT_MATCH\n" +
		"src/b100772/XX.java void b100772.XX.foo(Exception) [foo] EXACT_MATCH\n" +
		"src/b100772/Y.java void b100772.Y.foo(Exception) [foo] EXACT_MATCH\n" +
		"src/b100772/Z.java void b100772.Z.foo(Exception) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_Complex04() throws CoreException {
	setUpBug100772_ProjectScope_Complex();
	IType type = this.workingCopies[0].getType("X");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/X.java void b100772.X.foo(T) [foo] EXACT_MATCH\n" +
		"src/b100772/IX.java void b100772.IX.foo(U) [foo] EXACT_MATCH\n" +
		"src/b100772/XX.java void b100772.XX.foo(String) [foo] EXACT_MATCH"
	);
}

/**
 * @bug 108088: [search] Inaccurate search match for method invocations with literal arguments
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=108088"
 */
public void testBug108088() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearchBugs", "src", "b108088", "Test108088.java");
	IType type = unit.getType("A108088");
	IMethod method = type.getMethod("subroutine", new String[] { "F" });
	SearchPattern pattern = SearchPattern.createPattern(method, REFERENCES, EXACT_RULE);
	assertNotNull("Pattern should not be null", pattern);
	search(pattern, getJavaSearchScope(), this.resultCollector);
	assertSearchResults(
		"src/b108088/B108088.java void b108088.B108088.doit(A108088, String) [subroutine(1.2f)] EXACT_MATCH"
	);
}

/**
 *	@bug 109695: [search] Numbers should be treated as upper-case letters in CamelCase matching
 *	@test Ensure that camel case pattern including numbers return correct set of types
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=109695"
 */
public void testBug109695() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/IDocumentExtension.java",
		"public interface IDocumentExtension {}\n" +
		"interface IDocumentExtension2 {}\n" +
		"interface IDocumentExtension3 {}\n" +
		"interface IDocumentExtension135 {}\n" +
		"interface IDocumentExtension315 {}\n"
	);
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	int validatedRule = SearchPattern.validateMatchRule("IDE3", SearchPattern.R_CAMELCASE_MATCH);
	searchAllTypeNames("IDE3", validatedRule, requestor);
	assertSearchResults(
		"IDocumentExtension135\n" +
		"IDocumentExtension3\n" +
		"IDocumentExtension315",
		requestor
	);
}
public void testBug109695b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/IDocumentProviderExtension.java",
		"public interface IDocumentProviderExtension {}\n" +
		"interface IDocumentProviderExtension2 {}\n" +
		"interface IDocumentProviderExtension3 {}\n" +
		"interface IDocumentProviderExtension4 {}\n" +
		"interface IDocumentProviderExtension5 {}\n" +
		"interface IDocumentProviderExtension12345 {}\n" +
		"interface IDocumentProviderExtension54321 {}\n"
	);
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	int validatedRule = SearchPattern.validateMatchRule("IDPE3", SearchPattern.R_CAMELCASE_MATCH);
	searchAllTypeNames("IDPE3", validatedRule, requestor);
	assertSearchResults(
		"IDocumentProviderExtension12345\n" +
		"IDocumentProviderExtension3\n" +
		"IDocumentProviderExtension54321",
		requestor
	);
}
public void testBug109695c() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/IPerspectiveListener.java",
		"public interface IPerspectiveListener {}\n" +
		"interface IPerspectiveListener2 {}\n" +
		"interface IPerspectiveListener3 {}\n"
	);
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	int validatedRule = SearchPattern.validateMatchRule("IPL3", SearchPattern.R_CAMELCASE_MATCH);
	searchAllTypeNames("IPL3", validatedRule, requestor);
	assertSearchResults(
		"IPerspectiveListener3",
		requestor
	);
}
public void testBug109695d() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/IPropertySource.java",
		"public interface IPropertySource {}\n" +
		"interface IPropertySource2 {}\n"
	);
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	int validatedRule = SearchPattern.validateMatchRule("IPS2", SearchPattern.R_CAMELCASE_MATCH);
	searchAllTypeNames("IPS2", validatedRule, requestor);
	assertSearchResults(
		"IPropertySource2",
		requestor
	);
}
public void testBug109695e() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/IWorkbenchWindowPulldownDelegate.java",
		"public interface IWorkbenchWindowPulldownDelegate {}\n" +
		"interface IWorkbenchWindowPulldownDelegate1 {}\n" +
		"interface IWorkbenchWindowPulldownDelegate2 {}\n" +
		"interface IWorkbenchWindowPulldownDelegate3 {}\n" +
		"interface IWorkbenchWindowPulldownDelegate4 {}\n"
	);
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	int validatedRule = SearchPattern.validateMatchRule("IWWPD2", SearchPattern.R_CAMELCASE_MATCH);
	searchAllTypeNames("IWWPD2", validatedRule, requestor);
	assertSearchResults(
		"IWorkbenchWindowPulldownDelegate2",
		requestor
	);
}
public void testBug109695f() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/UTF16DocumentScannerSupport.java",
		"public class UTF16DocumentScannerSupport {}\n" +
		"class UTF1DocScannerSupport {}\n" +
		"class UTF6DocScannerSupport {}\n" +
		"class UTFDocScannerSupport {}\n"
	);
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	int validatedRule = SearchPattern.validateMatchRule("UTF16DSS", SearchPattern.R_CAMELCASE_MATCH);
	searchAllTypeNames("UTF16DSS", validatedRule, requestor);
	assertSearchResults(
		"UTF16DocumentScannerSupport",
		requestor
	);
}
public void testBug109695g() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/UTF16DocumentScannerSupport.java",
		"public class UTF16DocumentScannerSupport {}\n" +
		"class UTF1DocScannerSupport {}\n" +
		"class UTF6DocScannerSupport {}\n" +
		"class UTFDocScannerSupport {}\n"
	);
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	int validatedRule = SearchPattern.validateMatchRule("UTF1DSS", SearchPattern.R_CAMELCASE_MATCH);
	searchAllTypeNames("UTF1DSS", validatedRule, requestor);
	assertSearchResults(
		"UTF16DocumentScannerSupport\n" +
		"UTF1DocScannerSupport",
		requestor
	);
}
public void testBug109695h() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/UTF16DocumentScannerSupport.java",
		"public class UTF16DocumentScannerSupport {}\n" +
		"class UTF1DocScannerSupport {}\n" +
		"class UTF6DocScannerSupport {}\n" +
		"class UTFDocScannerSupport {}\n"
	);
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	int validatedRule = SearchPattern.validateMatchRule("UTF6DSS", SearchPattern.R_CAMELCASE_MATCH);
	searchAllTypeNames("UTF6DSS", validatedRule, requestor);
	assertSearchResults(
		"UTF16DocumentScannerSupport\n" +
		"UTF6DocScannerSupport",
		requestor
	);
}
public void testBug109695i() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/UTF16DocumentScannerSupport.java",
		"public class UTF16DocumentScannerSupport {}\n" +
		"class UTF1DocScannerSupport {}\n" +
		"class UTF6DocScannerSupport {}\n" +
		"class UTFDocScannerSupport {}\n"
	);
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	int validatedRule = SearchPattern.validateMatchRule("UTFDSS", SearchPattern.R_CAMELCASE_MATCH);
	searchAllTypeNames("UTFDSS", validatedRule, requestor);
	assertSearchResults(
		"UTF16DocumentScannerSupport\n" +
		"UTF1DocScannerSupport\n" +
		"UTF6DocScannerSupport\n" +
		"UTFDocScannerSupport",
		requestor
	);
}

/**
 * To get these tests search matches in a workspace, do NOT forget to modify files
 * to set them as working copies.
 *
 * @test Bug 110060: [plan][search] Add support for Camel Case search pattern
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=110060"
 */
// Types search
private void setUpBug110060_TypePattern() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110060/Test.java",
		"package b110060;\n" +
		"public class Test {\n" +
		"	Aaa a1;\n" +
		"	AAa a2;\n" +
		"	AaAaAa a3;\n" +
		"	AAxx a4;\n" +
		"	AxA a5;\n" +
		"	AxxAyy a6;\n" +
		"}\n" +
		"class AAa {}\n" +
		"class Aaa {}\n" +
		"class AaAaAa {}\n" +
		"class AAxx {}\n" +
		"class AxA {}\n" +
		"class AxxAyy {}\n"
	);
}
public void testBug110060_TypePattern01() throws CoreException {
	setUpBug110060_TypePattern();
	search("AA", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a1 [Aaa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a2 [AAa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a3 [AaAaAa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a4 [AAxx] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a5 [AxA] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a6 [AxxAyy] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern01_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	search("AA", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a2 [AAa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a4 [AAxx] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a5 [AxA] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a6 [AxxAyy] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern02() throws CoreException {
	setUpBug110060_TypePattern();
	search("AA", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a1 [Aaa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a2 [AAa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a3 [AaAaAa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a4 [AAxx] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a5 [AxA] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a6 [AxxAyy] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.AAa [AAa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Aaa [Aaa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.AaAaAa [AaAaAa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.AAxx [AAxx] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.AxA [AxA] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.AxxAyy [AxxAyy] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern02_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	search("AA", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a2 [AAa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a4 [AAxx] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a5 [AxA] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a6 [AxxAyy] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.AAa [AAa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.AAxx [AAxx] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.AxA [AxA] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.AxxAyy [AxxAyy] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern03() throws CoreException {
	setUpBug110060_TypePattern();
	search("AAx", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a4 [AAxx] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern03_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	search("AAx", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a4 [AAxx] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern04() throws CoreException {
	setUpBug110060_TypePattern();
	search("Axx", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a6 [AxxAyy] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern04_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	search("Axx", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults("");
}
public void testBug110060_TypePattern05() throws CoreException {
	setUpBug110060_TypePattern();
	search("Ax", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a5 [AxA] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a6 [AxxAyy] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern05_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	search("Ax", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults("");
}
public void testBug110060_TypePattern06() throws CoreException {
	setUpBug110060_TypePattern();
	search("A*A*", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_MATCH);
	// Invalid camel case pattern => replace the camel case flag with pattern match one (case insensitive)
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a1 [Aaa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a2 [AAa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a3 [AaAaAa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a4 [AAxx] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a5 [AxA] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a6 [AxxAyy] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern06_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	search("A*A*", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	// Invalid camel case pattern => replace the camel case flag with pattern match one (case insensitive)
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a1 [Aaa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a2 [AAa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a3 [AaAaAa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a4 [AAxx] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a5 [AxA] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a6 [AxxAyy] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern07() throws CoreException {
	setUpBug110060_TypePattern();
	search("aaa", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_MATCH);
	// Invalid camel case pattern => replace the camel case flag by prefix match one (case insensitive)
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a1 [Aaa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a2 [AAa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a3 [AaAaAa] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern07_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	search("aaa", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	// Invalid camel case pattern => replace the camel case flag by exact match one (case insensitive)
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a1 [Aaa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a2 [AAa] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern08() throws CoreException {
	setUpBug110060_TypePattern();
	search("Aaa", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_MATCH);
	// Invalid camel case pattern => replace the camel case flag by prefix  match one (case insensitive)
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a1 [Aaa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a2 [AAa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a3 [AaAaAa] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern08_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	search("Aaa", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	// Invalid camel case pattern => replace the camel case flag by exact match one (case insensitive)
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a1 [Aaa] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.a2 [AAa] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern09() throws CoreException {
	setUpBug110060_TypePattern();
	search("Aaa", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_CASE_SENSITIVE);
	// Invalid camel case pattern => replace the camel case flag by prefix  match one keeping case sensitive
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a1 [Aaa] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern09_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	search("Aaa", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_CASE_SENSITIVE);
	// Invalid camel case pattern => replace the camel case flag by exact match one keeping case sensitive
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a1 [Aaa] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern10() throws CoreException {
	setUpBug110060_TypePattern();
	search("AxAx", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults("");
}
public void testBug110060_TypePattern10_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	search("AxAx", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults("");
}
public void testBug110060_TypePattern11() throws CoreException {
	setUpBug110060_TypePattern();
	search("AxxA", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a6 [AxxAyy] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern11_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	search("AxxA", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a6 [AxxAyy] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern12() throws CoreException {
	setUpBug110060_TypePattern();
	search("AxXA", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.a6 [AxxAyy] EXACT_MATCH"
	);
}
public void testBug110060_TypePattern12_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	search("AxXA", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults("");
}

// Search all type names requests
public void testBug110060_AllTypeNames01() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("AA", SearchPattern.R_CAMELCASE_MATCH, requestor);
	assertSearchResults("Unexpected all type names",
		"b110060.AAa\n" +
		"b110060.AAxx\n" +
		"b110060.AaAaAa\n" +
		"b110060.Aaa\n" +
		"b110060.AxA\n" +
		"b110060.AxxAyy",
		requestor);
}
public void testBug110060_AllTypeNames01_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("AA", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, requestor);
	assertSearchResults("Unexpected all type names",
		"b110060.AAa\n" +
		"b110060.AAxx\n" +
		"b110060.AxA\n" +
		"b110060.AxxAyy",
		requestor);
}
public void testBug110060_AllTypeNames02() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("aaa", SearchPattern.R_CAMELCASE_MATCH, requestor);
	// Invalid camel case pattern => replace the camel case flag with prefix match one (case insensitive)
	assertSearchResults("Unexpected all type names",
		"b110060.AAa\n" +
		"b110060.AaAaAa\n" +
		"b110060.Aaa",
		requestor);
}
public void testBug110060_AllTypeNames02_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("aaa", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, requestor);
	// Invalid camel case pattern => replace the camel case flag with exact match one (case insensitive)
	assertSearchResults("Unexpected all type names",
		"b110060.AAa\n" +
		"b110060.Aaa",
		requestor);
}
public void testBug110060_AllTypeNames03() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("AAa", SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_CASE_SENSITIVE, requestor);
	assertSearchResults("Unexpected all type names",
		"b110060.AAa\n" +
		"b110060.AaAaAa",
		requestor);
}
public void testBug110060_AllTypeNames03_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("AAa", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_CASE_SENSITIVE, requestor);
	assertSearchResults("Unexpected all type names",
		"b110060.AAa",
		requestor);
}
public void testBug110060_AllTypeNames04() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("AA", SearchPattern.R_PREFIX_MATCH, requestor);
	assertSearchResults(
		"Unexpected all type names",
		"b110060.AAa\n" +
		"b110060.AAxx\n" +
		"b110060.AaAaAa\n" +
		"b110060.Aaa",
		requestor);
}
public void testBug110060_AllTypeNames05() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("AAA", SearchPattern.R_CASE_SENSITIVE, requestor);
	assertSearchResults(
		"Unexpected all type names",
		"",
		requestor);
}
public void testBug110060_AllTypeNames06() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("AA", SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CASE_SENSITIVE, requestor);
	assertSearchResults(
		"Unexpected all type names",
		"b110060.AAa\n" +
		"b110060.AAxx",
		requestor);
}
public void testBug110060_AllTypeNames07() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("aaa", SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_PREFIX_MATCH, requestor);
	// Invalid camel case pattern => replace the camel case flag with prefix match one (case insensitive)
	assertSearchResults(
		"Unexpected all type names",
		"b110060.AAa\n" +
		"b110060.AaAaAa\n" +
		"b110060.Aaa",
		requestor);
}
public void testBug110060_AllTypeNames07_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("aaa", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_PREFIX_MATCH, requestor);
	// Invalid camel case pattern => replace the camel case flag with exact match one (case insensitive)
	assertSearchResults(
		"Unexpected all type names",
		"b110060.AAa\n" +
		"b110060.Aaa",
		requestor);
}
public void testBug110060_AllTypeNames08() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("aaa", SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_CASE_SENSITIVE, requestor);
	// Invalid camel case pattern => replace the camel case flag with prefix match one keeping case sensitive
	assertSearchResults(
		"Unexpected all type names",
		"",
		requestor);
}
public void testBug110060_AllTypeNames08_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("aaa", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_CASE_SENSITIVE, requestor);
	// Invalid camel case pattern => replace the camel case flag with exact match one keeping case sensitive
	assertSearchResults(
		"Unexpected all type names",
		"",
		requestor);
}
public void testBug110060_AllTypeNames09() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("aaa", SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CASE_SENSITIVE, requestor);
	// Invalid camel case pattern => reset the camel case flag keeping prefix match and case sensitive ones
	assertSearchResults(
		"Unexpected all type names",
		"",
		requestor);
}
public void testBug110060_AllTypeNames09_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("aaa", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CASE_SENSITIVE, requestor);
	// Invalid camel case pattern => reset the camel case flag exact match keeping case sensitive
	assertSearchResults(
		"Unexpected all type names",
		"",
		requestor);
}
public void testBug110060_AllTypeNames12() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("aa", SearchPattern.R_PREFIX_MATCH, requestor);
	assertSearchResults(
		"Unexpected all type names",
		"b110060.AAa\n" +
		"b110060.AAxx\n" +
		"b110060.AaAaAa\n" +
		"b110060.Aaa",
		requestor);
}
public void testBug110060_AllTypeNames13() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("aa", SearchPattern.R_CASE_SENSITIVE, requestor);
	assertSearchResults(
		"Unexpected all type names",
		"",
		requestor);
}
public void testBug110060_AllTypeNames14() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("aa", SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CASE_SENSITIVE, requestor);
	assertSearchResults(
		"Unexpected all type names",
		"",
		requestor);
}

// Constructor search
private void setUpBug110060_ConstructorPattern() throws CoreException {
	this.workingCopies = new ICompilationUnit[5];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110060/AAAA.java",
		"package b110060;\n" +
		"public class AAAA {\n" +
		"	AAAA() {}\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b110060/AAxx.java",
		"package b110060;\n" +
		"public class AAxx {\n" +
		"	AAxx() {}\n" +
		"}\n"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b110060/AxxAyy.java",
		"package b110060;\n" +
		"public class AxxAyy {\n" +
		"	AxxAyy() {}\n" +
		"}\n"
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b110060/AxAyAz.java",
		"package b110060;\n" +
		"public class AxAyAz {\n" +
		"	AxAyAz() {}\n" +
		"}\n"
	);
	this.workingCopies[4] = getWorkingCopy("/JavaSearchBugs/src/b110060/Test.java",
		"package b110060;\n" +
		"public class Test {\n" +
		"	AAAA aaaa = new AAAA();\n" +
		"	AAxx aaxx = new AAxx();\n" +
		"	AxAyAz axayaz = new AxAyAz();\n" +
		"	AxxAyy axxayy = new AxxAyy();\n" +
		"}\n"
	);
}
public void testBug110060_ConstructorPattern01() throws CoreException {
	setUpBug110060_ConstructorPattern();
	search("AA", CONSTRUCTOR, REFERENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.aaaa [new AAAA()] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.aaxx [new AAxx()] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.axayaz [new AxAyAz()] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.axxayy [new AxxAyy()] EXACT_MATCH"
	);
}
public void testBug110060_ConstructorPattern01_SamePartCount() throws CoreException {
	setUpBug110060_ConstructorPattern();
	search("AA", CONSTRUCTOR, REFERENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.aaxx [new AAxx()] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.axxayy [new AxxAyy()] EXACT_MATCH"
	);
}
public void testBug110060_ConstructorPattern02() throws CoreException {
	setUpBug110060_ConstructorPattern();
	search("AA", CONSTRUCTOR, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/AAAA.java b110060.AAAA() [AAAA] EXACT_MATCH\n" +
		"src/b110060/AAxx.java b110060.AAxx() [AAxx] EXACT_MATCH\n" +
		"src/b110060/AxAyAz.java b110060.AxAyAz() [AxAyAz] EXACT_MATCH\n" +
		"src/b110060/AxxAyy.java b110060.AxxAyy() [AxxAyy] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.aaaa [new AAAA()] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.aaxx [new AAxx()] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.axayaz [new AxAyAz()] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.axxayy [new AxxAyy()] EXACT_MATCH"
	);
}
public void testBug110060_ConstructorPattern02_SamePartCount() throws CoreException {
	setUpBug110060_ConstructorPattern();
	search("AA", CONSTRUCTOR, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/b110060/AAxx.java b110060.AAxx() [AAxx] EXACT_MATCH\n" +
		"src/b110060/AxxAyy.java b110060.AxxAyy() [AxxAyy] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.aaxx [new AAxx()] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.axxayy [new AxxAyy()] EXACT_MATCH"
	);
}
public void testBug110060_ConstructorPattern03() throws CoreException {
	setUpBug110060_ConstructorPattern();
	search("AAx", CONSTRUCTOR, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/AAxx.java b110060.AAxx() [AAxx] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.aaxx [new AAxx()] EXACT_MATCH"
	);
}
public void testBug110060_ConstructorPattern03_SamePartCount() throws CoreException {
	setUpBug110060_ConstructorPattern();
	search("AAx", CONSTRUCTOR, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/b110060/AAxx.java b110060.AAxx() [AAxx] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.aaxx [new AAxx()] EXACT_MATCH"
	);
}
public void testBug110060_ConstructorPattern04() throws CoreException {
	setUpBug110060_ConstructorPattern();
	search("AxA", CONSTRUCTOR, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/AxAyAz.java b110060.AxAyAz() [AxAyAz] EXACT_MATCH\n" +
		"src/b110060/AxxAyy.java b110060.AxxAyy() [AxxAyy] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.axayaz [new AxAyAz()] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.axxayy [new AxxAyy()] EXACT_MATCH"
	);
}
public void testBug110060_ConstructorPattern04_SamePartCount() throws CoreException {
	setUpBug110060_ConstructorPattern();
	search("AxA", CONSTRUCTOR, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/b110060/AxxAyy.java b110060.AxxAyy() [AxxAyy] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.axxayy [new AxxAyy()] EXACT_MATCH"
	);
}
public void testBug110060_ConstructorPattern05() throws CoreException {
	setUpBug110060_ConstructorPattern();
	search("A*A*", CONSTRUCTOR, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	// Invalid camel case pattern => replace the camel case flag with pattern match one (case insensitive)
	assertSearchResults(
		"src/b110060/AAAA.java b110060.AAAA() [AAAA] EXACT_MATCH\n" +
		"src/b110060/AAxx.java b110060.AAxx() [AAxx] EXACT_MATCH\n" +
		"src/b110060/AxAyAz.java b110060.AxAyAz() [AxAyAz] EXACT_MATCH\n" +
		"src/b110060/AxxAyy.java b110060.AxxAyy() [AxxAyy] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.aaaa [new AAAA()] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.aaxx [new AAxx()] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.axayaz [new AxAyAz()] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.axxayy [new AxxAyy()] EXACT_MATCH"
	);
}
public void testBug110060_ConstructorPattern05_SamePartCount() throws CoreException {
	setUpBug110060_ConstructorPattern();
	search("A*A*", CONSTRUCTOR, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	// Invalid camel case pattern => replace the camel case flag with pattern match one (case insensitive)
	assertSearchResults(
		"src/b110060/AAAA.java b110060.AAAA() [AAAA] EXACT_MATCH\n" +
		"src/b110060/AAxx.java b110060.AAxx() [AAxx] EXACT_MATCH\n" +
		"src/b110060/AxAyAz.java b110060.AxAyAz() [AxAyAz] EXACT_MATCH\n" +
		"src/b110060/AxxAyy.java b110060.AxxAyy() [AxxAyy] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.aaaa [new AAAA()] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.aaxx [new AAxx()] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.axayaz [new AxAyAz()] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.axxayy [new AxxAyy()] EXACT_MATCH"
	);
}

// Methods search
private void setUpBug110060_MethodPattern() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110060/Test.java",
		"package b110060;\n" +
		"public class Test {\n" +
		"	void aMethodWithNothingSpecial() {}\n" +
		"	void aMethodWith1Digit() {}\n" +
		"	void aMethodWith1DigitAnd_AnUnderscore() {}\n" +
		"	void aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores() {}\n" +
		"	void otherMethodWhichStartsWithAnotherLetter() {}\n" +
		"	void testReferences() {\n" +
		"		aMethodWith1Digit();\n" +
		"		aMethodWith1DigitAnd_AnUnderscore();\n" +
		"		aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores();\n" +
		"		aMethodWithNothingSpecial();\n" +
		"		otherMethodWhichStartsWithAnotherLetter();\n" +
		"	}\n" +
		"}\n"
	);
}
public void testBug110060_MethodPattern01() throws CoreException {
	setUpBug110060_MethodPattern();
	search("MWD", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults("");
}
public void testBug110060_MethodPattern01_SamePartCount() throws CoreException {
	setUpBug110060_MethodPattern();
	search("MWD", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults("");
}
public void testBug110060_MethodPattern02() throws CoreException {
	setUpBug110060_MethodPattern();
	search("AMWD", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults("");
}
public void testBug110060_MethodPattern02_SamePartCount() throws CoreException {
	setUpBug110060_MethodPattern();
	search("AMWD", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults("");
}
public void testBug110060_MethodPattern03() throws CoreException {
	setUpBug110060_MethodPattern();
	search("aMWD", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java void b110060.Test.aMethodWith1Digit() [aMethodWith1Digit] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.aMethodWith1DigitAnd_AnUnderscore() [aMethodWith1DigitAnd_AnUnderscore] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Digit()] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1DigitAnd_AnUnderscore()] EXACT_MATCH"
	);
}
public void testBug110060_MethodPattern03_SamePartCount() throws CoreException {
	setUpBug110060_MethodPattern();
	search("aMWD", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/b110060/Test.java void b110060.Test.aMethodWith1Digit() [aMethodWith1Digit] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Digit()] EXACT_MATCH"
	);
}
public void testBug110060_MethodPattern04() throws CoreException {
	setUpBug110060_MethodPattern();
	search("aMW", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java void b110060.Test.aMethodWithNothingSpecial() [aMethodWithNothingSpecial] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.aMethodWith1Digit() [aMethodWith1Digit] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.aMethodWith1DigitAnd_AnUnderscore() [aMethodWith1DigitAnd_AnUnderscore] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Digit()] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1DigitAnd_AnUnderscore()] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores()] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWithNothingSpecial()] EXACT_MATCH"
	);
}
public void testBug110060_MethodPattern04_SamePartCount() throws CoreException {
	setUpBug110060_MethodPattern();
	search("aMW", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		""
	);
}
public void testBug110060_MethodPattern05() throws CoreException {
	setUpBug110060_MethodPattern();
	search("aMethod", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java void b110060.Test.aMethodWithNothingSpecial() [aMethodWithNothingSpecial] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.aMethodWith1Digit() [aMethodWith1Digit] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.aMethodWith1DigitAnd_AnUnderscore() [aMethodWith1DigitAnd_AnUnderscore] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Digit()] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1DigitAnd_AnUnderscore()] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores()] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWithNothingSpecial()] EXACT_MATCH"
	);
}
public void testBug110060_MethodPattern05_SamePartCount() throws CoreException {
	setUpBug110060_MethodPattern();
	search("aMethod", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults("");
}
public void testBug110060_MethodPattern06() throws CoreException {
	setUpBug110060_MethodPattern();
	search("aMethodWith1", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java void b110060.Test.aMethodWith1Digit() [aMethodWith1Digit] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.aMethodWith1DigitAnd_AnUnderscore() [aMethodWith1DigitAnd_AnUnderscore] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Digit()] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1DigitAnd_AnUnderscore()] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores()] EXACT_MATCH"
	);
}
public void testBug110060_MethodPattern06_SamePartCount() throws CoreException {
	setUpBug110060_MethodPattern();
	search("aMethodWith1", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults("");
}
public void testBug110060_MethodPattern07() throws CoreException {
	setUpBug110060_MethodPattern();
	search("*Method*With*A*", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	// Invalid camel case pattern => replace the camel case flag with pattern match one (case insensitive)
	assertSearchResults(
		"src/b110060/Test.java void b110060.Test.aMethodWithNothingSpecial() [aMethodWithNothingSpecial] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.aMethodWith1DigitAnd_AnUnderscore() [aMethodWith1DigitAnd_AnUnderscore] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.otherMethodWhichStartsWithAnotherLetter() [otherMethodWhichStartsWithAnotherLetter] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1DigitAnd_AnUnderscore()] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores()] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWithNothingSpecial()] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [otherMethodWhichStartsWithAnotherLetter()] EXACT_MATCH"
	);
}
public void testBug110060_MethodPattern07_SamePartCount() throws CoreException {
	setUpBug110060_MethodPattern();
	search("*Method*With*A*", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	// Invalid camel case pattern => replace the camel case flag with pattern match one (case insensitive)
	assertSearchResults(
		"src/b110060/Test.java void b110060.Test.aMethodWithNothingSpecial() [aMethodWithNothingSpecial] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.aMethodWith1DigitAnd_AnUnderscore() [aMethodWith1DigitAnd_AnUnderscore] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.otherMethodWhichStartsWithAnotherLetter() [otherMethodWhichStartsWithAnotherLetter] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1DigitAnd_AnUnderscore()] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores()] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWithNothingSpecial()] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [otherMethodWhichStartsWithAnotherLetter()] EXACT_MATCH"
	);
}
public void testBug110060_MethodPattern08() throws CoreException {
	setUpBug110060_MethodPattern();
	search("aMW1D", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java void b110060.Test.aMethodWith1Digit() [aMethodWith1Digit] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.aMethodWith1DigitAnd_AnUnderscore() [aMethodWith1DigitAnd_AnUnderscore] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Digit()] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1DigitAnd_AnUnderscore()] EXACT_MATCH"
	);
}
public void testBug110060_MethodPattern08_SamePartCount() throws CoreException {
	setUpBug110060_MethodPattern();
	search("aMW1D", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/b110060/Test.java void b110060.Test.aMethodWith1Digit() [aMethodWith1Digit] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Digit()] EXACT_MATCH"
	);
}
public void testBug110060_MethodPattern09() throws CoreException {
	setUpBug110060_MethodPattern();
	search("aMWOOODASU", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java void b110060.Test.aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores()] EXACT_MATCH"
	);
}
public void testBug110060_MethodPattern09_SamePartCount() throws CoreException {
	setUpBug110060_MethodPattern();
	search("aMWOOODASU", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/b110060/Test.java void b110060.Test.aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores()] EXACT_MATCH"
	);
}

// Fields search
private void setUpBug110060_FieldPattern() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110060/Test.java",
		"package b110060;\n" +
		"public class Test {\n" +
		"	Object aFieldWithNothingSpecial;\n" +
		"	Object aFieldWithS$Dollar;\n" +
		"	Object aFieldWith$Several$DollarslAnd1DigitAnd_1Underscore;\n" +
		"	Object aStrangeFieldWith$$$$$$$$$$$$$$$SeveraContiguousDollars;\n" +
		"	Object otherFieldWhichStartsWithAnotherLetter;\n" +
		"	void testReferences() {\n" +
		"		Object o0 = aFieldWithNothingSpecial;\n" +
		"		Object o1 = aFieldWithS$Dollar;\n" +
		"		Object o2 = aFieldWith$Several$DollarslAnd1DigitAnd_1Underscore;\n" +
		"		Object o3 = aStrangeFieldWith$$$$$$$$$$$$$$$SeveraContiguousDollars;\n" +
		"	}\n" +
		"	Object oF = otherFieldWhichStartsWithAnotherLetter;\n" +
		"}\n"
	);
}
public void testBug110060_FieldPattern01() throws CoreException {
	setUpBug110060_FieldPattern();
	search("aFWSD", FIELD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.aFieldWithS$Dollar [aFieldWithS$Dollar] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.aFieldWith$Several$DollarslAnd1DigitAnd_1Underscore [aFieldWith$Several$DollarslAnd1DigitAnd_1Underscore] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aFieldWithS$Dollar] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aFieldWith$Several$DollarslAnd1DigitAnd_1Underscore] EXACT_MATCH"
	);
}
public void testBug110060_FieldPattern01_SamePartCount() throws CoreException {
	setUpBug110060_FieldPattern();
	search("aFWSD", FIELD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.aFieldWithS$Dollar [aFieldWithS$Dollar] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aFieldWithS$Dollar] EXACT_MATCH"
	);
}
public void testBug110060_FieldPattern02() throws CoreException {
	setUpBug110060_FieldPattern();
	search("afwsd", FIELD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults("");
}
public void testBug110060_FieldPattern02_SamePartCount() throws CoreException {
	setUpBug110060_FieldPattern();
	search("afwsd", FIELD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults("");
}
public void testBug110060_FieldPattern03() throws CoreException {
	setUpBug110060_FieldPattern();
	search("aFWS$", FIELD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.aFieldWithS$Dollar [aFieldWithS$Dollar] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aFieldWithS$Dollar] EXACT_MATCH"
	);
}
public void testBug110060_FieldPattern03_SamePartCount() throws CoreException {
	setUpBug110060_FieldPattern();
	search("aFWS$", FIELD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults("");
}
public void testBug110060_FieldPattern04() throws CoreException {
	setUpBug110060_FieldPattern();
	search("aSFWSCD", FIELD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.aStrangeFieldWith$$$$$$$$$$$$$$$SeveraContiguousDollars [aStrangeFieldWith$$$$$$$$$$$$$$$SeveraContiguousDollars] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aStrangeFieldWith$$$$$$$$$$$$$$$SeveraContiguousDollars] EXACT_MATCH"
	);
}
public void testBug110060_FieldPattern04_SamePartCount() throws CoreException {
	setUpBug110060_FieldPattern();
	search("aSFWSCD", FIELD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.aStrangeFieldWith$$$$$$$$$$$$$$$SeveraContiguousDollars [aStrangeFieldWith$$$$$$$$$$$$$$$SeveraContiguousDollars] EXACT_MATCH\n" +
		"src/b110060/Test.java void b110060.Test.testReferences() [aStrangeFieldWith$$$$$$$$$$$$$$$SeveraContiguousDollars] EXACT_MATCH"
	);
}
public void testBug110060_FieldPattern05() throws CoreException {
	setUpBug110060_FieldPattern();
	search("oF", FIELD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.otherFieldWhichStartsWithAnotherLetter [otherFieldWhichStartsWithAnotherLetter] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.oF [oF] EXACT_MATCH\n" +
		"src/b110060/Test.java b110060.Test.oF [otherFieldWhichStartsWithAnotherLetter] EXACT_MATCH"
	);
}
public void testBug110060_FieldPattern05new() throws CoreException {
	setUpBug110060_FieldPattern();
	search("oF", FIELD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.oF [oF] EXACT_MATCH"
	);
}

/**
 * @test Bug 110291: [search] BasicSearchEngine return constructor declarations that doesn't exist in source
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=110291"
 */
public void testBug110291() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110291/Test110291XX.java",
		"package b110291;\n" +
		"public class Test110291XX {\n" +
		"	class Test110291YY {}" +
		"}\n"
	);
	search("Test110291", CONSTRUCTOR, DECLARATIONS, SearchPattern.R_PREFIX_MATCH);
	assertSearchResults(
		"src/b110291/Test110291XX.java b110291.Test110291XX$Test110291YY [Test110291YY] EXACT_MATCH"
	);
}

/**
 * @test Bug 110336: [plan][search] Should optionally return the local variable for type reference
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=110336"
 */
public void testBug110336a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
		"package b110336;\n" +
		"public class Test {\n" +
		"	<TP extends Test> void method(Class<Test> clazz) {\n" +
		"		Test localVar1 = new Test();\n" +
		"		Class<Test> localVar2 = new Class<Test>();\n" +
		"		localVar1.method(localVar2);\n" +
		"	}\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("Test");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/b110336/Test.java void b110336.Test.method(Class<Test>) [Test]+[TP]\n" + 
		"src/b110336/Test.java void b110336.Test.method(Class<Test>) [Test]+[clazz]\n" + 
		"src/b110336/Test.java void b110336.Test.method(Class<Test>) [Test]+[localVar1]\n" + 
		"src/b110336/Test.java void b110336.Test.method(Class<Test>) [Test]+[localVar1]\n" + 
		"src/b110336/Test.java void b110336.Test.method(Class<Test>) [Test]+[localVar2]\n" + 
		"src/b110336/Test.java void b110336.Test.method(Class<Test>) [Test]+[localVar2]",
		collector
	);
}
public void testBug110336b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
		"package b110336;\n" +
		"public class Test {\n" +
		"	void method1(Test methodParam) {\n" +
		"		Test localVar1 = new Test(){\n" +
		"			Class c = Test.class;\n" +
		"			<TP extends Test> void foo(){\n" +
		"				Test o = (Test) null;\n" +
		"			}\n" +
		"		};\n" +
		"	}	\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("Test");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/b110336/Test.java void b110336.Test.method1(Test):<anonymous>#1 [Test]\n" + 
		"src/b110336/Test.java void b110336.Test.method1(Test):<anonymous>#1.c [Test]\n" + 
		"src/b110336/Test.java void void b110336.Test.method1(Test):<anonymous>#1.foo() [Test]+[TP]\n" + 
		"src/b110336/Test.java void void b110336.Test.method1(Test):<anonymous>#1.foo() [Test]+[o]\n" + 
		"src/b110336/Test.java void void b110336.Test.method1(Test):<anonymous>#1.foo() [Test]+[o]\n" + 
		"src/b110336/Test.java void b110336.Test.method1(Test) [Test]+[methodParam]\n" + 
		"src/b110336/Test.java void b110336.Test.method1(Test) [Test]+[localVar1]",
		collector
	);
}
public void testBug110336c() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
		"package b110336;\n" +
		"public class Test<TP extends X> {\n" +
		"	X x;\n" +
		"\n" +
		"}\n" +
		"class X {}\n"
	);
	IType type = this.workingCopies[0].getType("X");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/b110336/Test.java b110336.Test [X]+[TP]\n" + 
		"src/b110336/Test.java b110336.Test.x [X]",
		collector
	);
}
public void testBug110336d() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
		"package b110336;\n" +
		"public class Test {\n" +
		"	Test a1Test = null, b1Test = new Test(), c1Test;\n" +
		"	Test a2Test = new Test(), b2Test, c2Test = null;\n" +
		"	Test a3Test, b3Test = null, c3Test = new Test();\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("Test");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/b110336/Test.java b110336.Test.a1Test [Test]+[b1Test,c1Test]\n" +
		"src/b110336/Test.java b110336.Test.b1Test [Test]\n" +
		"src/b110336/Test.java b110336.Test.a2Test [Test]+[b2Test,c2Test]\n" +
		"src/b110336/Test.java b110336.Test.a2Test [Test]\n" +
		"src/b110336/Test.java b110336.Test.a3Test [Test]+[b3Test,c3Test]\n" +
		"src/b110336/Test.java b110336.Test.c3Test [Test]",
		collector
	);
}
public void testBug110336e() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
		"package b110336;\n" +
		"public class Test {\n" +
		"	void foo() {\n" +
		"		Test lv1 = null, lv2 = new Test(), lv3;\n" +
		"		Test lv4 = new Test(), lv5, lv6 = null;\n" +
		"		Test lv7, lv8 = null, lv9 = new Test();\n" +
		"	}\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("Test");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/b110336/Test.java void b110336.Test.foo() [Test]+[lv1]+[lv2,lv3]\n" + 
		"src/b110336/Test.java void b110336.Test.foo() [Test]+[lv2]\n" + 
		"src/b110336/Test.java void b110336.Test.foo() [Test]+[lv4]+[lv5,lv6]\n" + 
		"src/b110336/Test.java void b110336.Test.foo() [Test]+[lv4]\n" + 
		"src/b110336/Test.java void b110336.Test.foo() [Test]+[lv7]+[lv8,lv9]\n" + 
		"src/b110336/Test.java void b110336.Test.foo() [Test]+[lv9]",
		collector
	);
}
public void testBug110336f() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
		"package b110336;\n" +
		"public class Test extends Exception {\n" +
		"        void foo(Test test1) { // <- no local element\n" +
		"                Test test2; // <- local element\n" +
		"                try {\n" +
		"                        throw new Test();\n" +
		"                }\n" +
		"                catch (Test test4) { // <- no local element\n" +
		"                }\n" +
		"                for(Test test3;;) {} // <- local element\n" +
		"        }\n" +
		"\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("Test");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/b110336/Test.java void b110336.Test.foo(Test) [Test]+[test1]\n" + 
		"src/b110336/Test.java void b110336.Test.foo(Test) [Test]+[test2]\n" + 
		"src/b110336/Test.java void b110336.Test.foo(Test) [Test]\n" + 
		"src/b110336/Test.java void b110336.Test.foo(Test) [Test]+[test4]\n" + 
		"src/b110336/Test.java void b110336.Test.foo(Test) [Test]+[test3]",
		collector
	);
}
public void testBug110336g() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
		"package b110336;\n" +
		"public class Test {\n" +
		"	{\n" +
		"		Test lv1 = null, lv2 = new Test(), lv3;\n" +
		"		Test lv4 = new Test(), lv5, lv6 = null;\n" +
		"		Test lv7, lv8 = null, lv9 = new Test();\n" +
		"	}\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("Test");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/b110336/Test.java b110336.Test.{} [Test]+[lv1]+[lv2,lv3]\n" + 
		"src/b110336/Test.java b110336.Test.{} [Test]+[lv2]\n" + 
		"src/b110336/Test.java b110336.Test.{} [Test]+[lv4]+[lv5,lv6]\n" + 
		"src/b110336/Test.java b110336.Test.{} [Test]+[lv4]\n" + 
		"src/b110336/Test.java b110336.Test.{} [Test]+[lv7]+[lv8,lv9]\n" + 
		"src/b110336/Test.java b110336.Test.{} [Test]+[lv9]",
		collector
	);
}
public void testBug110336h() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
		"package b110336;\n" +
		"public class Test {\n" +
		"	static {\n" +
		"		Test lv1 = null, lv2 = new Test(), lv3;\n" +
		"		Test lv4 = new Test(), lv5, lv6 = null;\n" +
		"		Test lv7, lv8 = null, lv9 = new Test();\n" +
		"	}\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("Test");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/b110336/Test.java b110336.Test.static {} [Test]+[lv1]+[lv2,lv3]\n" + 
		"src/b110336/Test.java b110336.Test.static {} [Test]+[lv2]\n" + 
		"src/b110336/Test.java b110336.Test.static {} [Test]+[lv4]+[lv5,lv6]\n" + 
		"src/b110336/Test.java b110336.Test.static {} [Test]+[lv4]\n" + 
		"src/b110336/Test.java b110336.Test.static {} [Test]+[lv7]+[lv8,lv9]\n" + 
		"src/b110336/Test.java b110336.Test.static {} [Test]+[lv9]",
		collector
	);
}

/**
 * @test Bug 110422: [search] BasicSearchEngine doesn't find all type declarations
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=110422"
 */
public void testBug110422a() throws CoreException {
	search("TestP", TYPE, DECLARATIONS, SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CASE_SENSITIVE);
	assertSearchResults(
		"lib/b110422.jar b110422.TestPrefix EXACT_MATCH"
	);
}
public void testBug110422b() throws CoreException {
	search("TESTP", TYPE, DECLARATIONS, SearchPattern.R_PREFIX_MATCH);
	assertSearchResults(
		"lib/b110422.jar b110422.TestPrefix EXACT_MATCH"
	);
}

/**
 * @bug 113671: [search] AIOOBE in SearchEngine#searchAllTypeNames
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=113671"
 */
public void testBug113671() throws CoreException {
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine().searchAllTypeNames(
	   "java.lang".toCharArray(),
		SearchPattern.R_EXACT_MATCH,
		CharOperation.NO_CHAR,
		SearchPattern.R_PREFIX_MATCH,
		IJavaSearchConstants.TYPE,
		getJavaSearchScope(),
		requestor,
		WAIT_UNTIL_READY_TO_SEARCH,
		null
   );
	assertSearchResults(
		"Unexpected all type names",
		"java.lang.CharSequence\n" +
		"java.lang.Class\n" +
		"java.lang.CloneNotSupportedException\n" +
		"java.lang.Comparable\n" +
		"java.lang.Deprecated\n" +
		"java.lang.Enum\n" +
		"java.lang.Error\n" +
		"java.lang.Exception\n" +
		"java.lang.IllegalMonitorStateException\n" +
		"java.lang.InterruptedException\n" +
		"java.lang.Object\n" +
		"java.lang.RuntimeException\n" +
		"java.lang.String\n" +
		"java.lang.Throwable",
		requestor);
}

/**
 * @test Bug 114539: [search] Internal error when refactoring code with errors
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=114539"
 */
public void testBug114539() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b114539/Foo.java",
		"package b114539;\n" +
		"public class Foo {\n" +
		"	int bar=Bar.FOO;\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b114539/Bar.java",
		"package b114539;\n" +
		"public class Bar {\n" +
		"	private static final int FOO=0;\n" +
		"}\n"
	);
	IField field = this.workingCopies[1].getType("Bar").getField("FOO");
	search(field, REFERENCES);
	assertSearchResults(
		"src/b114539/Foo.java b114539.Foo.bar [FOO] POTENTIAL_MATCH"
	);
}

/**
 * @bug 116459: [search] correct results are missing in java search
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=116459"
 */
public void testBug116459() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p1/X.java",
		"package p1;\n" +
		"class X<T> {\n" +
		"	X<T> gen;\n" +
		"	X<String> param;\n" +
		"	X raw;\n" +
		"}"
	);
	IType type = this.workingCopies[0].getType("X");
	this.resultCollector.showRule();
	search(type, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"src/p1/X.java p1.X.gen [X] EXACT_MATCH\n" +
		"src/p1/X.java p1.X.param [X] ERASURE_MATCH\n" +
		"src/p1/X.java p1.X.raw [X] ERASURE_RAW_MATCH"
	);
}

/**
 * @test Bug 119545: [search] Binary java method model elements returned by SearchEngine have unresolved parameter types
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=119545"
 */
public void testBug119545() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b119545/Test.java",
		"package b119545;\n" +
		"class Test {\n" +
		"	void foo(Object o1, Object o2){\n" +
		"		if (o1.equals(o2)) {}\n" +
		"	}\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethods()[0];
	searchDeclarationsOfSentMessages(method, this.resultCollector);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " boolean java.lang.Object.equals(java.lang.Object) EXACT_MATCH"
	);
}

/**
 * @test Bug 120816: [search] NullPointerException at ...jdt.internal.compiler.lookup.SourceTypeBinding.getMethods
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=120816"
 */
public void testBug120816a() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b120816/Test.java",
		"package b120816;\n" +
		"public class Test<E> {\n" +
		"	String foo(E e) { return \"\"; }\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b120816/Sub.java",
		"package b120816;\n" +
		"public class Sub extends Test<Exception> {\n" +
		"	String foo(RuntimeException e) { return \"\"; }\n" +
		"	String foo(Exception e) {\n" +
		"		return super.foo(e);\n" +
		"	}\n" +
		"}\n"
	);
	search("* String (Exception)", METHOD, DECLARATIONS);
	assertSearchResults(
		"src/b120816/Sub.java String b120816.Sub.foo(Exception) [foo] EXACT_MATCH"
	);
}
public void testBug120816b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b120816/Test.java",
		"package b120816;\n" +
		"public class Test<E> {\n" +
		"	String foo(E e) { return \"\"; }\n" +
		"}\n" +
		"class Sub extends Test<Exception> {\n" +
		"	String foo(RuntimeException e) { return \"\"; }\n" +
		"	String foo(Exception e) {\n" +
		"		return super.foo(e);\n" +
		"	}\n" +
		"}\n"
	);
	search("* String (Exception)", METHOD, DECLARATIONS);
	assertSearchResults(
		"src/b120816/Test.java String b120816.Sub.foo(Exception) [foo] EXACT_MATCH"
	);
}

/**
 * @test Bug 122442: [search] API inconsistency with IJavaSearchConstants.IMPLEMENTORS and SearchPattern
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=122442"
 */
private void setUpBug122442a() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b122442/I.java",
		"package b122442;\n" +
		"public interface I {}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b122442/II.java",
		"package b122442;\n" +
		"public interface II extends I {}\n"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b122442/X.java",
		"package b122442;\n" +
		"public class X implements I {}\n"
	);
}
public void testBug122442a() throws CoreException {
	setUpBug122442a();
	search("I", TYPE, IMPLEMENTORS);
	assertSearchResults(
		"src/b122442/II.java b122442.II [I] EXACT_MATCH\n" +
		"src/b122442/X.java b122442.X [I] EXACT_MATCH"
	);
}
public void testBug122442b() throws CoreException {
	setUpBug122442a();
	search("I", INTERFACE, IMPLEMENTORS);
	assertSearchResults(
		"src/b122442/II.java b122442.II [I] EXACT_MATCH"
	);
}
public void testBug122442c() throws CoreException {
	setUpBug122442a();
	search("I", CLASS, IMPLEMENTORS);
	assertSearchResults(
		"src/b122442/X.java b122442.X [I] EXACT_MATCH"
	);
}
private void setUpBug122442d() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b122442/User.java",
		"class Klass {}\n" +
		"interface Interface {}\n" +
		"class User {\n" +
		"    void m() {\n" +
		"        new Klass() {};\n" +
		"        new Interface() {};\n" +
		"    }\n" +
		"}\n" +
		"class Sub extends Klass {}"
	);
}
public void testBug122442d() throws CoreException {
	setUpBug122442d();
	search("Interface", TYPE, IMPLEMENTORS);
	assertSearchResults(
		"src/b122442/User.java void b122442.User.m():<anonymous>#2 [Interface] EXACT_MATCH"
	);
}
public void testBug122442e() throws CoreException {
	setUpBug122442d();
	search("Interface", INTERFACE, IMPLEMENTORS);
	assertSearchResults(
		"" // expected no result
	);
}
public void testBug122442f() throws CoreException {
	setUpBug122442d();
	search("Interface", CLASS, IMPLEMENTORS);
	assertSearchResults(
		"src/b122442/User.java void b122442.User.m():<anonymous>#2 [Interface] EXACT_MATCH"
	);
}
public void testBug122442g() throws CoreException {
	setUpBug122442d();
	search("Klass", TYPE, IMPLEMENTORS);
	assertSearchResults(
		"src/b122442/User.java void b122442.User.m():<anonymous>#1 [Klass] EXACT_MATCH\n" +
		"src/b122442/User.java b122442.Sub [Klass] EXACT_MATCH"
	);
}
public void testBug122442h() throws CoreException {
	setUpBug122442d();
	search("Klass", INTERFACE, IMPLEMENTORS);
	assertSearchResults(
		"" // expected no result
	);
}
public void testBug122442i() throws CoreException {
	setUpBug122442d();
	search("Klass", CLASS, IMPLEMENTORS);
	assertSearchResults(
		"src/b122442/User.java void b122442.User.m():<anonymous>#1 [Klass] EXACT_MATCH\n" +
		"src/b122442/User.java b122442.Sub [Klass] EXACT_MATCH"
	);
}

/**
 * @bug 123679: [search] Field references not found when type is a qualified member type [regression]
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=123679"
 */
public void testBug123679() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b123679.jar", "pack").getClassFile("I123679.class").getType();
	search(type, REFERENCES);
	assertSearchResults(
		"lib/b123679.jar test.<anonymous> EXACT_MATCH\n" +
		"lib/b123679.jar test.Test$StaticClass$Member.parent EXACT_MATCH\n" +
		"lib/b123679.jar test.<anonymous> EXACT_MATCH\n" +
		"lib/b123679.jar test.Test$StaticClass$Member(test.Test.StaticClass, java.lang.Object) EXACT_MATCH\n" +
		"lib/b123679.jar test.Test$StaticClass$Member(test.Test.StaticClass, java.lang.Object) EXACT_MATCH\n" +
		"lib/b123679.jar pack.I123679 test.Test$StaticClass$Member.getParent() EXACT_MATCH"
	);
}
public void testBug123679_cu() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearchBugs", "src", "b123679.pack", "I123679.java");
	IType type = unit.getType("I123679");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b123679/test/Test.java [b123679.pack.I123679] EXACT_MATCH\n" +
		"src/b123679/test/Test.java b123679.test.Test$StaticClass$Member.parent [I123679] EXACT_MATCH\n" +
		"src/b123679/test/Test.java b123679.test.Test$StaticClass$Member(Object):<anonymous>#1 [I123679] EXACT_MATCH\n" +
		"src/b123679/test/Test.java b123679.test.Test$StaticClass$Member(Object) [I123679] EXACT_MATCH\n" +
		"src/b123679/test/Test.java b123679.test.Test$StaticClass$Member(Object) [I123679] EXACT_MATCH\n" +
		"src/b123679/test/Test.java I123679 b123679.test.Test$StaticClass$Member.getParent() [I123679] EXACT_MATCH"
	);
}
public void testBug123679_wc() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/I122679.java",
		"package pack;\n" +
		"public interface I123679 {\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
		"package test;\n" +
		"import pack.I123679;\n" +
		"public class Test {\n" +
		"	static class StaticClass {\n" +
		"		class Member {\n" +
		"			private I123679 parent;\n" +
		"			Member(Object obj) {\n" +
		"				if (obj instanceof I123679) {\n" +
		"					parent = (I123679) obj;\n" +
		"				} else {\n" +
		"					parent = new I123679() {};\n" +
		"				}\n" +
		"			}\n" +
		"			I123679 getParent() {\n" +
		"				return parent;\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("I123679");
	search(type, REFERENCES);
	assertSearchResults(
		"src/test/Test.java [pack.I123679] EXACT_MATCH\n" +
		"src/test/Test.java test.Test$StaticClass$Member.parent [I123679] EXACT_MATCH\n" +
		"src/test/Test.java test.Test$StaticClass$Member(Object):<anonymous>#1 [I123679] EXACT_MATCH\n" +
		"src/test/Test.java test.Test$StaticClass$Member(Object) [I123679] EXACT_MATCH\n" +
		"src/test/Test.java test.Test$StaticClass$Member(Object) [I123679] EXACT_MATCH\n" +
		"src/test/Test.java I123679 test.Test$StaticClass$Member.getParent() [I123679] EXACT_MATCH"
	);
}

/**
 * @bug 124469: [search] AIOOBE in PatternLocator when searching for dependency extent from manifest
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=124469"
 */
public void testBug124469a() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "E.class").getType();
	search(type, REFERENCES);
	assertSearchResults(
		"lib/b124469.jar pack.E pack.A1.value() EXACT_MATCH\n" +
		"lib/b124469.jar pack.E[] pack.A1.list() EXACT_MATCH\n" +
		"lib/b124469.jar pack.E pack.A2.value() EXACT_MATCH\n" +
		"lib/b124469.jar pack.E[] pack.A2.list() EXACT_MATCH\n" +
		"lib/b124469.jar pack.E pack.A3.value() EXACT_MATCH\n" +
		"lib/b124469.jar pack.E[] pack.A3.list() EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH"
	);
}
public void testBug124469b() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "A1.class").getType();
	search(type, REFERENCES);
	assertSearchResults(
		"lib/b124469.jar pack.A1 pack.A2.annot() EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH"
	);
}
public void testBug124469c() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "A2.class").getType();
	search(type, REFERENCES);
	assertSearchResults(
		"lib/b124469.jar pack.A2 pack.A3.annot() EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH"
	);
}
public void testBug124469d() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "A3.class").getType();
	search(type, REFERENCES);
	assertSearchResults(
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH"
	);
}
public void testBug124469e() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "E.class").getType();
	IField field = type.getField("CC");
	search(field, REFERENCES);
	assertSearchResults(
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH"
	);
}
public void testBug124469f() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "E.class").getType();
	IField field = type.getField("CF");
	search(field, REFERENCES);
	assertSearchResults(
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH"
	);
}
public void testBug124469g() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "E.class").getType();
	IField field = type.getField("CM");
	search(field, REFERENCES);
	assertSearchResults(
		"lib/b124469.jar void test.M.foo() EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH"
	);
}
public void testBug124469h() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "E.class").getType();
	IField field = type.getField("CP");
	search(field, REFERENCES);
	assertSearchResults(
		"" // expected no result as parameters annotations are not stored in class file
	);
}
public void testBug124469i() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "E.class").getType();
	IField field = type.getField("CV");
	search(field, REFERENCES);
	assertSearchResults(
		"" // expected no result as parameters annotations are not stored in class file
	);
}
public void testBug124469j() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "E.class").getType();
	IField field = type.getField("CAC");
	search(field, REFERENCES);
	assertSearchResults(
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH\n" +
		"lib/b124469.jar test.C EXACT_MATCH"
	);
}
public void testBug124469k() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "E.class").getType();
	IField field = type.getField("CAF");
	search(field, REFERENCES);
	assertSearchResults(
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH\n" +
		"lib/b124469.jar test.F.field EXACT_MATCH"
	);
}
public void testBug124469l() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "E.class").getType();
	IField field = type.getField("CAM");
	search(field, REFERENCES);
	assertSearchResults(
		"lib/b124469.jar void test.M.foo() EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH\n" +
		"lib/b124469.jar void test.M.foo() EXACT_MATCH"
	);
}
public void testBug124469m() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "E.class").getType();
	IField field = type.getField("CAP");
	search(field, REFERENCES);
	assertSearchResults(
		"" // expected no result as parameters annotations are not stored in class file
	);
}
public void testBug124469n() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "E.class").getType();
	IField field = type.getField("CAV");
	search(field, REFERENCES);
	assertSearchResults(
		"" // expected no result as parameters annotations are not stored in class file
	);
}

/**
 * @bug 124489: [search] correct results are missing in java search
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=124489"
 */
public void testBug124489() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Foo.java",
		"public class Foo<T> {}"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/Bar.java",
		"public class Bar {\n" +
		"    Foo<String> f = new Foo<String>();\n" +
		"    Foo f2 = new Foo();\n" +
		"}"
	);
	IType type = this.workingCopies[0].getType("Foo");
	this.resultCollector.showRule();
	new SearchEngine(this.workingCopies).search(
		SearchPattern.createPattern(type, REFERENCES),
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		getJavaSearchScope(),
		this.resultCollector,
		null
	);
	assertSearchResults(
		"src/Bar.java Bar.f [Foo] ERASURE_MATCH\n" +
		"src/Bar.java Bar.f [Foo] ERASURE_MATCH\n" +
		"src/Bar.java Bar.f2 [Foo] ERASURE_RAW_MATCH\n" +
		"src/Bar.java Bar.f2 [Foo] ERASURE_RAW_MATCH"
	);
}

/**
 *	@bug 124624: [search] Camel case matching routines should support end character
 *	@test Ensure that camel case pattern may use end character
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=124624"
 */
private void setupBug124624() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"class HashMap {}\n" +
		"class HtmlMapper {}\n" +
		"class HashMapEntry {}\n" +
		"class HaxMapxxxx {}\n"
	);
}
public void testBug124624_HM_CamelCase() throws CoreException {
	setupBug124624();
	search("HM", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/Test.java HashMap [HashMap] EXACT_MATCH\n" +
		"src/Test.java HtmlMapper [HtmlMapper] EXACT_MATCH\n" +
		"src/Test.java HashMapEntry [HashMapEntry] EXACT_MATCH\n" +
		"src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"
	);
}
public void testBug124624_HM_CamelCaseSamePartCount() throws CoreException {
	setupBug124624();
	search("HM", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/Test.java HashMap [HashMap] EXACT_MATCH\n" +
		"src/Test.java HtmlMapper [HtmlMapper] EXACT_MATCH\n" +
		"src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"
	);
}
public void testBug124624_HaM_CamelCase() throws CoreException {
	setupBug124624();
	search("HaM", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/Test.java HashMap [HashMap] EXACT_MATCH\n" +
		"src/Test.java HashMapEntry [HashMapEntry] EXACT_MATCH\n" +
		"src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"
	);
}
public void testBug124624_HaM_CamelCaseSamePartCount() throws CoreException {
	setupBug124624();
	search("HaM", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/Test.java HashMap [HashMap] EXACT_MATCH\n" +
		"src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"
	);
}
public void testBug124624_HashM_CamelCase() throws CoreException {
	setupBug124624();
	search("HashM", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/Test.java HashMap [HashMap] EXACT_MATCH\n" +
		"src/Test.java HashMapEntry [HashMapEntry] EXACT_MATCH"
	);
}
public void testBug124624_HashM_CamelCaseSamePartCount() throws CoreException {
	setupBug124624();
	search("HashM", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/Test.java HashMap [HashMap] EXACT_MATCH"
	);
}
public void testBug124624_HMa_CamelCase() throws CoreException {
	setupBug124624();
	search("HMa", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/Test.java HashMap [HashMap] EXACT_MATCH\n" +
		"src/Test.java HtmlMapper [HtmlMapper] EXACT_MATCH\n" +
		"src/Test.java HashMapEntry [HashMapEntry] EXACT_MATCH\n" +
		"src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"
	);
}
public void testBug124624_HMa_CamelCaseSamePartCount() throws CoreException {
	setupBug124624();
	search("HMa", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/Test.java HashMap [HashMap] EXACT_MATCH\n" +
		"src/Test.java HtmlMapper [HtmlMapper] EXACT_MATCH\n" +
		"src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"
	);
}
public void testBug124624_HaMa_CamelCase() throws CoreException {
	setupBug124624();
	search("HaMa", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/Test.java HashMap [HashMap] EXACT_MATCH\n" +
		"src/Test.java HashMapEntry [HashMapEntry] EXACT_MATCH\n" +
		"src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"
	);
}
public void testBug124624_HaMa_CamelCaseSamePartCount() throws CoreException {
	setupBug124624();
	search("HaMa", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/Test.java HashMap [HashMap] EXACT_MATCH\n" +
		"src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"
	);
}
public void testBug124624_HashMa_CamelCase() throws CoreException {
	setupBug124624();
	search("HashMa", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/Test.java HashMap [HashMap] EXACT_MATCH\n" +
		"src/Test.java HashMapEntry [HashMapEntry] EXACT_MATCH"
	);
}
public void testBug124624_HashMa_CamelCaseSamePartCount() throws CoreException {
	setupBug124624();
	search("HashMa", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/Test.java HashMap [HashMap] EXACT_MATCH"
	);
}
public void testBug124624_HMap_CamelCase() throws CoreException {
	setupBug124624();
	search("HMap", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/Test.java HashMap [HashMap] EXACT_MATCH\n" +
		"src/Test.java HtmlMapper [HtmlMapper] EXACT_MATCH\n" +
		"src/Test.java HashMapEntry [HashMapEntry] EXACT_MATCH\n" +
		"src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"
	);
}
public void testBug124624_HMap_CamelCaseSamePartCount() throws CoreException {
	setupBug124624();
	search("HMap", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/Test.java HashMap [HashMap] EXACT_MATCH\n" +
		"src/Test.java HtmlMapper [HtmlMapper] EXACT_MATCH\n" +
		"src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"
	);
}
public void testBug124624_HaMap_CamelCase() throws CoreException {
	setupBug124624();
	search("HaMap", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/Test.java HashMap [HashMap] EXACT_MATCH\n" +
		"src/Test.java HashMapEntry [HashMapEntry] EXACT_MATCH\n" +
		"src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"
	);
}
public void testBug124624_HaMap_CamelCaseSamePartCount() throws CoreException {
	setupBug124624();
	search("HaMap", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/Test.java HashMap [HashMap] EXACT_MATCH\n" +
		"src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"
	);
}
public void testBug124624_HashMap_CamelCase() throws CoreException {
	setupBug124624();
	search("HashMap", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/Test.java HashMap [HashMap] EXACT_MATCH\n" +
		"src/Test.java HashMapEntry [HashMapEntry] EXACT_MATCH"
	);
}
public void testBug124624_HashMap_CamelCaseSamePartCount() throws CoreException {
	setupBug124624();
	search("HashMap", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/Test.java HashMap [HashMap] EXACT_MATCH"
	);
}

/**
 * @test Bug 124645: [search] for implementors does not find subclasses of binary classes
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=124645"
 */
public void testBug124645a() throws CoreException {
	IClassFile classFile = getClassFile("JavaSearchBugs", "lib/b124645.jar", "xy", "BE_124645.class");
	IType type = classFile.getType();
	search(type, IMPLEMENTORS);
	assertSearchResults(
		"src/b124645/xy/X_124645.java b124645.xy.X_124645$Y [BE_124645] EXACT_MATCH\n" +
		"src/b124645/xy/Y_124645.java b124645.xy.Y_124645 [BE_124645] EXACT_MATCH\n" +
		"lib/b124645.jar xy.BX_124645$Y EXACT_MATCH\n" +
		"lib/b124645.jar xy.BY_124645 EXACT_MATCH"
	);
}
public void testBug124645b() throws CoreException {
	IClassFile classFile = getClassFile("JavaSearchBugs", "lib/b124645.jar", "test", "BE_124645.class");
	IType type = classFile.getType();
	search(type, IMPLEMENTORS);
	assertSearchResults(
		"src/b124645/test/A_124645.java b124645.test.A_124645 [BE_124645] EXACT_MATCH\n" +
		"src/b124645/test/A_124645.java void b124645.test.A_124645.m():<anonymous>#1 [BE_124645] EXACT_MATCH\n" +
		"src/b124645/test/X_124645.java b124645.test.X_124645 [BE_124645] EXACT_MATCH\n" +
		"src/b124645/test/X_124645.java void b124645.test.X_124645.m():Y_124645#1 [BE_124645] EXACT_MATCH\n" +
		"lib/b124645.jar test.BA_124645 EXACT_MATCH\n" +
		"lib/b124645.jar test.<anonymous> EXACT_MATCH\n" +
		"lib/b124645.jar test.BX_124645 EXACT_MATCH\n" +
		"lib/b124645.jar test.Y EXACT_MATCH"
	);
}
public void testBug124645c() throws CoreException {
	IClassFile classFile = getClassFile("JavaSearchBugs", "lib/b124645.jar", "", "BC_124645.class");
	IType type = classFile.getType();
	search(type, IMPLEMENTORS);
	assertSearchResults(
		"lib/b124645.jar <anonymous> EXACT_MATCH"
	);
}
public void testBug124645d() throws CoreException {
	IClassFile classFile = getClassFile("JavaSearchBugs", "lib/b124645.jar", "", "BI_124645.class");
	IType type = classFile.getType();
	search(type, IMPLEMENTORS);
	assertSearchResults(
		"lib/b124645.jar <anonymous> EXACT_MATCH"
	);
}

/**
 * @bug 125178: [search] AIOOBE in PatternLocator when searching for dependency extent from manifest
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=125178"
 */
public void testBug125178() throws CoreException {
	// Need a working copy as anonymous are not indexed...
	ProblemRequestor problemRequestor = new ProblemRequestor();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b125178/X.java",
		"package b125178;\n" +
		"import pack.age.Test;\n" +
		"public class X {\n" +
		"	public static void main(String[] args) {\n" +
		"		new Test().foo(100);\n" +
		"	}\n" +
		"}\n",
		newWorkingCopyOwner(problemRequestor)
	);
	assertEquals("CU Should not have any problem!",
		"----------\n" +
		"----------\n",
		problemRequestor.problems.toString()
	);

	// Get anonymous from
	IPackageFragment jar = getPackageFragment("JavaSearchBugs", "lib/b125178.jar", "pack.age");
	IJavaElement[] children = jar.getChildren();
	assertNotNull("We should have children for in default package of lib/b125178.jar", children);
	for (int i=0,l=children.length; i<l; i++) {
		assertTrue("Jar should only have class files!", children[i] instanceof ClassFile);
		IType type = ((ClassFile)children[i]).getType();
		if (type.isAnonymous()) {
			search(type, REFERENCES);
		}
	}
	assertSearchResults(
		"" // no result expected
	);
}

/**
 * @bug 126330: Type reference not found in jar file if sources was not already opened
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=126330"
 */
public void testBug126330() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b126330.jar", "").getClassFile("A126330.class").getType();
	search(type, REFERENCES);
	assertSearchResults(
		"lib/b126330.jar B126330.a EXACT_MATCH"
	);
}

/**
 * @bug 127628: [index] CodeAssist doesn't filter deprecated types
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=127628"
 */
public void testBug127628() throws CoreException {
	class DeprecatedTypesRequestor extends SearchTests.SearchTypeNameRequestor {
		public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path) {
			if ((modifiers & ClassFileConstants.AccDeprecated) != 0) {
				char[] typeName =
					CharOperation.concat(
						CharOperation.concatWith(enclosingTypeNames, '$'),
						simpleTypeName,
						'$');
				this.results.addElement(new String(CharOperation.concat(packageName, typeName, '.')));
			}
		}
	}
	TypeNameRequestor requestor =  new DeprecatedTypesRequestor();
	new SearchEngine().searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		null,
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		TYPE,
		getJavaSearchScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"b127628.Test127628",
		requestor);
}

/**
 * @bug 128877: [search] reports inexistent IMethod for binary constructor of inner class
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=128877"
 */
public void testBug128877a() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b128877.jar", "pack").getClassFile("Test.class").getType();
	IMethod method = type.getMethod("Test", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"lib/b128877.jar pack.X$Sub(pack.X) EXACT_MATCH"
	);
}
public void testBug128877b() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b128877.jar", "pack").getClassFile("Test.class").getType();
	IMethod method = type.getMethod("Test", new String[] { "Ljava.lang.String;" });
	search(method, REFERENCES);
	assertSearchResults(
		"lib/b128877.jar pack.X$Sub(pack.X, java.lang.String) EXACT_MATCH"
	);
}
public void testBug128877c() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b128877.jar", "pack").getClassFile("Test.class").getType();
	IMethod method = type.getMethod("foo128877", new String[] { "I" });
	search(method, REFERENCES);
	assertSearchResults(
		"lib/b128877.jar pack.X$Sub(pack.X) EXACT_MATCH"
	);
}

/**
 * To get these tests search matches in a workspace, do NOT forget to modify files
 * to set them as working copies.
 *
 * @test Bug 130390: CamelCase algorithm cleanup and improvement
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=130390"
 */
private void setUpBug130390() throws CoreException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b130390/TZ.java",
		"package b130390;\n" +
		"public class TZ {\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b130390/TimeZone.java",
		"package b130390;\n" +
		"public class TimeZone{\n" +
		"}\n"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b130390/Npe.java",
		"package b130390;\n" +
		"public class Npe {\n" +
		"}\n"
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b130390/NullPointerException.java",
		"package b130390;\n" +
		"public class NullPointerException {\n" +
		"}\n"
	);
}
public void testBug130390_CamelCase() throws CoreException {
	setUpBug130390();
	search("NuPoEx", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b130390/NullPointerException.java b130390.NullPointerException [NullPointerException] EXACT_MATCH"
	);
}
public void testBug130390_CamelCaseSamePartCount() throws CoreException {
	setUpBug130390();
	search("NuPoEx", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/b130390/NullPointerException.java b130390.NullPointerException [NullPointerException] EXACT_MATCH"
	);
}
public void testBug130390b_CamelCase() throws CoreException {
	setUpBug130390();
	search("NPE", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b130390/Npe.java b130390.Npe [Npe] EXACT_MATCH\n" +
		"src/b130390/NullPointerException.java b130390.NullPointerException [NullPointerException] EXACT_MATCH"
	);
}
public void testBug130390b_CamelCaseSamePartCount() throws CoreException {
	setUpBug130390();
	search("NPE", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/b130390/NullPointerException.java b130390.NullPointerException [NullPointerException] EXACT_MATCH"
	);
}
public void testBug130390c_CamelCase() throws CoreException {
	setUpBug130390();
	search("NPE", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_CASE_SENSITIVE);
	assertSearchResults(
		"src/b130390/NullPointerException.java b130390.NullPointerException [NullPointerException] EXACT_MATCH"
	);
}
public void testBug130390c_CamelCaseSamePartCount() throws CoreException {
	setUpBug130390();
	search("NPE", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_CASE_SENSITIVE);
	assertSearchResults(
		"src/b130390/NullPointerException.java b130390.NullPointerException [NullPointerException] EXACT_MATCH"
	);
}
public void testBug130390d_CamelCase() throws CoreException {
	setUpBug130390();
	search("Npe", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b130390/Npe.java b130390.Npe [Npe] EXACT_MATCH"
	);
}
public void testBug130390d_CamelCaseSamePartCount() throws CoreException {
	setUpBug130390();
	search("Npe", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/b130390/Npe.java b130390.Npe [Npe] EXACT_MATCH"
	);
}
public void testBug130390e_CamelCase() throws CoreException {
	setUpBug130390();
	search("Npe", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_CASE_SENSITIVE);
	assertSearchResults(
		"src/b130390/Npe.java b130390.Npe [Npe] EXACT_MATCH"
	);
}
public void testBug130390e_CamelCaseSamePartCount() throws CoreException {
	setUpBug130390();
	search("Npe", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_CASE_SENSITIVE);
	assertSearchResults(
		"src/b130390/Npe.java b130390.Npe [Npe] EXACT_MATCH"
	);
}
public void testBug130390f_CamelCase() throws CoreException {
	setUpBug130390();
	search("NullPE", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b130390/NullPointerException.java b130390.NullPointerException [NullPointerException] EXACT_MATCH"
	);
}
public void testBug130390f_CamelCaseSamePartCount() throws CoreException {
	setUpBug130390();
	search("NullPE", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"src/b130390/NullPointerException.java b130390.NullPointerException [NullPointerException] EXACT_MATCH"
	);
}
public void testBug130390g_CamelCase() throws CoreException {
	setUpBug130390();
	search("TZ", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_CASE_SENSITIVE);
	assertSearchResults(
		"src/b130390/TZ.java b130390.TZ [TZ] EXACT_MATCH\n" +
		"src/b130390/TimeZone.java b130390.TimeZone [TimeZone] EXACT_MATCH"
	);
}
public void testBug130390g_CamelCaseSamePartCount() throws CoreException {
	setUpBug130390();
	search("TZ", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_CASE_SENSITIVE);
	assertSearchResults(
		"src/b130390/TZ.java b130390.TZ [TZ] EXACT_MATCH\n" +
		"src/b130390/TimeZone.java b130390.TimeZone [TimeZone] EXACT_MATCH"
	);
}
public void testBug130390h_CamelCase() throws CoreException {
	setUpBug130390();
	search("TiZo", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_CASE_SENSITIVE);
	assertSearchResults(
		"src/b130390/TimeZone.java b130390.TimeZone [TimeZone] EXACT_MATCH"
	);
}
public void testBug130390h_CamelCaseSamePartCount() throws CoreException {
	setUpBug130390();
	search("TiZo", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH | SearchPattern.R_CASE_SENSITIVE);
	assertSearchResults(
		"src/b130390/TimeZone.java b130390.TimeZone [TimeZone] EXACT_MATCH"
	);
}

/**
 * To get these tests search matches in a workspace, do NOT forget to modify files
 * to set them as working copies.
 *
 * @bug 137087: Open Type - missing matches when using mixed case pattern
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=137087"
 */
public void testBug137087_CamelCase() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "runtimeEx";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.RuntimeException EXACT_MATCH"
	);
}
public void testBug137087b_CamelCase() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "Runtimeex";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.RuntimeException EXACT_MATCH"
	);
}
public void testBug137087c_CamelCase() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "runtimeexception";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.RuntimeException EXACT_MATCH"
	);
}
public void testBug137087d_CamelCase() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "Runtimexception";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		"" // no match expected as pattern is missing a 'e'
	);
}
public void testBug137087e_CamelCase() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "IllegalMSException";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.IllegalMonitorStateException EXACT_MATCH"
	);
}
public void testBug137087f_CamelCase() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "illegalMsExceptionSException";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		"" // expected no result as uppercase characters in pattern do not match any camelcase ones in existing types
	);
}
public void testBug137087g_CamelCase() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "clonenotsupportedex";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
}
public void testBug137087h_CamelCase() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "CloneNotSupportedEx";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
}
public void testBug137087i_CamelCase() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "cloneNotsupportedEx";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
}
public void testBug137087j_CamelCase() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "ClonenotSupportedexc";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
}
public void testBug137087k_CamelCase() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "cloneNotSupportedExcep";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
}
public void testBug137087l_CamelCase() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "Clonenotsupportedexception";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
}
public void testBug137087m_CamelCase() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "CloneNotSupportedException";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
}
// Same tests using SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH
public void testBug137087_CamelCaseSamePartCount() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH;
	String pattern = "runtimeEx";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults("");
}
public void testBug137087b_CamelCaseSamePartCount() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH;
	String pattern = "Runtimeex";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults("");
}
public void testBug137087c_CamelCaseSamePartCount() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH;
	String pattern = "runtimeexception";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.RuntimeException EXACT_MATCH"
	);
}
public void testBug137087d_CamelCaseSamePartCount() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH;
	String pattern = "Runtimexception";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		"" // no match expected as pattern is missing a 'e'
	);
}
public void testBug137087e_CamelCaseSamePartCount() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH;
	String pattern = "IllegalMSException";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.IllegalMonitorStateException EXACT_MATCH"
	);
}
public void testBug137087f_CamelCaseSamePartCount() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH;
	String pattern = "illegalMsExceptionSException";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		"" // expected no result as uppercase characters in pattern do not match any camel case ones in existing types
	);
}
public void testBug137087g_CamelCaseSamePartCount() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH;
	String pattern = "clonenotsupportedex";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	// Invalid camel case match pattern => replaced with exact match one (case insensitive)
	assertSearchResults("");
}
public void testBug137087h_CamelCaseSamePartCount() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH;
	String pattern = "CloneNotSupportedEx";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
}
public void testBug137087i_CamelCaseSamePartCount() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH;
	String pattern = "cloneNotsupportedEx";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults("");
}
public void testBug137087j_CamelCaseSamePartCount() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH;
	String pattern = "ClonenotSupportedexc";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults("");
}
public void testBug137087k_CamelCaseSamePartCount() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH;
	String pattern = "cloneNotSupportedExcep";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults("");
}
public void testBug137087l_CamelCaseSamePartCount() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH;
	String pattern = "Clonenotsupportedexception";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	// Invalid camel case match pattern => replaced with exact match one (case insensitive)
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
}
public void testBug137087m_CamelCaseSamePartCount() throws CoreException {
	int matchRule = SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH;
	String pattern = "CloneNotSupportedException";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
}

/**
 * @bug 137984: [search] Field references not found when type is a qualified member type [regression]
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=137984"
 */
public void testBug137984_jar() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b137984.jar", "").getClassFile("CJ.class").getType();
	IField field = type.getField("c3");
	search(field, REFERENCES);
	assertSearchResults(
		"lib/b137984.jar CJ(int) EXACT_MATCH"
	);
}
public void testBug137984_cu() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearchBugs", "src", "b137984", "C.java");
	IField field = unit.getType("C").getField("c3");
	search(field, REFERENCES);
	assertSearchResults(
		"src/b137984/C.java b137984.C(int) [c3] EXACT_MATCH"
	);
}
public void testBug137984_wc() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/CW.java",
		"public class CW {\n" +
		"	CW2 c2;\n" +
		"	CW2.CW3 c3;\n" +
		"	CW(int c) {\n" +
		"		c2 = new CW2(c);\n" +
		"		c3 = c2.new CW3(c);\n" +
		"	}\n" +
		"	class CW2 {\n" +
		"		CW2(int x) {}\n" +
		"		class CW3 {\n" +
		"			CW3(int x) {}\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
	IField field = this.workingCopies[0].getType("CW").getField("c3");
	search(field, REFERENCES);
	assertSearchResults(
		"src/CW.java CW(int) [c3] EXACT_MATCH"
	);
}

/**
 * @bug 140156: [1.5][search] Invalid method handle with parameterized parameters when no source is attached
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=140156"
 */
public void testBug140156() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b140156.jar", "").getClassFile("X.class").getType();
	IMethod method = type.getMethods()[1];
	assertEquals("Search wrong method!!!", "foo", method.getElementName());
	search(method, DECLARATIONS);
	assertSearchResults(
		"lib/b140156.jar void X.foo(List<T>) [No source] EXACT_MATCH"
	);
}

/**
 * @bug 142044: [search] "And" Pattern fails with NullPointerException
 * @test Verify that no NPE occurs while using a {@link AndPattern}
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=142044"
 * @deprecated As using deprecated method
 */
/* Bug test case:
	Following test does not produce any result due to the fact that each type reference
	on type only match one of the pattern => it always fails while making a AND on both matches...
	However, consider the bug fixed as we do not get the NPE...
*/
public void testBug142044() throws CoreException {
	IType type1 = getCompilationUnit("JavaSearchBugs", "src", "b142044", "I142044_A.java").getType("I142044_A");
	SearchPattern leftPattern = SearchPattern.createPattern(type1, IMPLEMENTORS);
	IType type2 = getCompilationUnit("JavaSearchBugs", "src", "b142044", "I142044_B.java").getType("I142044_B");
	SearchPattern rightPattern = SearchPattern.createPattern(type2, IMPLEMENTORS);
	search(SearchPattern.createAndPattern(leftPattern, rightPattern), getJavaSearchScope(), this.resultCollector);
	assertSearchResults("");
}
/**
 * @deprecated As using deprecated method
 */
public void testBug142044_Identical() throws CoreException {
	IType type1 = getCompilationUnit("JavaSearchBugs", "src", "b142044", "I142044_A.java").getType("I142044_A");
	SearchPattern leftPattern = SearchPattern.createPattern(type1, IMPLEMENTORS);
	IType type2 = getCompilationUnit("JavaSearchBugs", "src", "b142044", "I142044_A.java").getType("I142044_A");
	SearchPattern rightPattern = SearchPattern.createPattern(type2, IMPLEMENTORS);
	search(SearchPattern.createAndPattern(leftPattern, rightPattern), getJavaSearchScope(), this.resultCollector);
	assertSearchResults(
		"src/b142044/X142044.java b142044.X142044$XX1 [I142044_A] EXACT_MATCH\n" +
		"src/b142044/X142044.java b142044.X142044$XX12 [I142044_A] EXACT_MATCH\n" +
		"src/b142044/X142044_A.java b142044.X142044_A [I142044_A] EXACT_MATCH\n" +
		"src/b142044/X142044_AB.java b142044.X142044_AB [I142044_A] EXACT_MATCH"
	);
}
/**
 * @deprecated As using deprecated method
 */
public void testBug142044_And01() throws CoreException {
	SearchPattern leftPattern = createPattern("X*", CLASS, DECLARATIONS, true);
	IType iType = getCompilationUnit("JavaSearchBugs", "src", "b142044", "I142044_A.java").getType("I142044_A");
	SearchPattern rightPattern = SearchPattern.createPattern(iType, IMPLEMENTORS);
	search(SearchPattern.createAndPattern(leftPattern, rightPattern), getJavaSearchScope(), this.resultCollector);
	assertSearchResults(""); // currently no results as only same kind of pattern are ANDoable...
}
/**
 * @deprecated As using deprecated method
 */
public void testBug142044_And02() throws CoreException {
	IType type1 = getCompilationUnit("JavaSearchBugs", "src", "b142044", "I142044_A.java").getType("I142044_A");
	SearchPattern leftPattern = SearchPattern.createPattern(type1, IMPLEMENTORS);
	SearchPattern rightPattern = createPattern("I*", CLASS, IMPLEMENTORS, true);
	search(SearchPattern.createAndPattern(leftPattern, rightPattern), getJavaSearchScope(), this.resultCollector);
	assertSearchResults(
		"src/b142044/X142044.java b142044.X142044$XX1 [I142044_A] EXACT_MATCH\n" +
		"src/b142044/X142044.java b142044.X142044$XX12 [I142044_A] EXACT_MATCH\n" +
		"src/b142044/X142044_A.java b142044.X142044_A [I142044_A] EXACT_MATCH\n" +
		"src/b142044/X142044_AB.java b142044.X142044_AB [I142044_A] EXACT_MATCH"
	);
}
/**
 * @deprecated As using deprecated method
 */
public void testBug142044_Or() throws CoreException {
	IType type1 = getCompilationUnit("JavaSearchBugs", "src", "b142044", "I142044_A.java").getType("I142044_A");
	SearchPattern leftPattern = SearchPattern.createPattern(type1, IMPLEMENTORS);
	IType type2 = getCompilationUnit("JavaSearchBugs", "src", "b142044", "I142044_B.java").getType("I142044_B");
	SearchPattern rightPattern = SearchPattern.createPattern(type2, IMPLEMENTORS);
	search(SearchPattern.createOrPattern(leftPattern, rightPattern), getJavaSearchScope(), this.resultCollector);
	assertSearchResults(
		"src/b142044/X142044.java b142044.X142044$XX1 [I142044_A] EXACT_MATCH\n" +
		"src/b142044/X142044.java b142044.X142044$XX2 [I142044_B] EXACT_MATCH\n" +
		"src/b142044/X142044.java b142044.X142044$XX12 [I142044_A] EXACT_MATCH\n" +
		"src/b142044/X142044.java b142044.X142044$XX12 [I142044_B] EXACT_MATCH\n" +
		"src/b142044/X142044_A.java b142044.X142044_A [I142044_A] EXACT_MATCH\n" +
		"src/b142044/X142044_AB.java b142044.X142044_AB [I142044_A] EXACT_MATCH\n" +
		"src/b142044/X142044_AB.java b142044.X142044_AB [I142044_B] EXACT_MATCH\n" +
		"src/b142044/X142044_B.java b142044.X142044_B [I142044_B] EXACT_MATCH"
	);
}

/**
 * @bug 144044: [search] NPE when trying to find references to field variable
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=144044"
 */
public void testBug144044() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test1/p/Test.java",
		"package test1.p;\n" +
		"import test1.q.X;\n" +
		"public class Test {\n" +
		"	String foo(X val) {\n" +
		"		return val.str;\n" +
		"	}\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test1/q/X.java",
		"package test1.q;\n" +
		"public class X {\n" +
		"	String str;\n" +
		"}\n"
	);
	IType type = this.workingCopies[1].getType("X");
	IField field = type.getField("str");
	search(field, REFERENCES);
	assertSearchResults(
		"src/test1/p/Test.java String test1.p.Test.foo(X) [str] POTENTIAL_MATCH"
	);
}
public void testBug144044b() throws CoreException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test2/p/Test.java",
		"package test2.p;\n" +
		"import test2.q.X;\n" +
		"public class Test {\n" +
		"	X foo() {\n" +
		"		return X.y_field.z_field.x_field.y_field.z_field.x_field;\n" +
		"	}\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test2/q/X.java",
		"package test2.q;\n" +
		"public class X {\n" +
		"	public static Y y_field;\n" +
		"}\n"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/test2/q/Y.java",
		"package test2.q;\n" +
		"public class Y {\n" +
		"	public static Z z_field;\n" +
		"}\n"
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/test2/q/Z.java",
		"package test2.q;\n" +
		"public class Z {\n" +
		"	static X x_field;\n" +
		"}\n"
	);
	IType type = this.workingCopies[3].getType("Z");
	IField field = type.getField("x_field");
	search(field, REFERENCES);
	assertSearchResults(
		"src/test2/p/Test.java X test2.p.Test.foo() [x_field] POTENTIAL_MATCH\n" +
		"src/test2/p/Test.java X test2.p.Test.foo() [x_field] POTENTIAL_MATCH"
	);
}

/**
 * @bug 148215: [search] correct results are missing in java search
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=148215"
 */
public void testBug148215_Types() throws CoreException {
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b148215.jar", false);
	try {
		IType type = getClassFile("JavaSearchBugs", "lib/b148215.jar", "test.pack", "Test.class").getType();
		IMethod method = type.getMethods()[1];
		searchDeclarationsOfReferencedTypes(method, this.resultCollector);
		assertSearchResults(
			""+ getExternalJCLPathString("1.5") + " java.lang.Object EXACT_MATCH\n" +
			""+ getExternalJCLPathString("1.5") + " java.lang.String EXACT_MATCH\n" +
			"lib/b148215.jar test.def.Reference EXACT_MATCH"
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/b148215.jar"));
	}
}
public void testBug148215_Messages() throws CoreException {
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b148215.jar", false);
	try {
		IType type = getClassFile("JavaSearchBugs", "lib/b148215.jar", "test.pack", "Test.class").getType();
		IMethod method = type.getMethods()[1];
		searchDeclarationsOfSentMessages(method, this.resultCollector);
		assertSearchResults(
			"lib/b148215.jar void test.pack.Test.bar(java.lang.String) EXACT_MATCH\n" +
			"lib/b148215.jar void test.pack.Test.bar(test.def.Reference) EXACT_MATCH"
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/b148215.jar"));
	}
}
public void testBug148215_Fields() throws CoreException {
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b148215.jar", false);
	try {
		IType type = getClassFile("JavaSearchBugs", "lib/b148215.jar", "test.pack", "Test.class").getType();
		IMethod method = type.getMethods()[1];
		searchDeclarationsOfAccessedFields(method, this.resultCollector);
		assertSearchResults(
			"lib/b148215.jar test.pack.Test.sField EXACT_MATCH\n" +
			"lib/b148215.jar test.pack.Test.rField EXACT_MATCH"
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/b148215.jar"));
	}
}

/**
 * @bug 148380: [search] get IType from TypeNameRequestor result
 * @test new SearchEngine.searchAllTypeName API method using {@link TypeNameMatchRequestor}
 * 	instead of {@link TypeNameRequestor}
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=148380"
 */
public void testBug148380_SearchAllTypes_wc() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b148380/I.java",
		"package b148380;\n" +
		"public interface I {}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b148380/X.java",
		"package b148380;\n" +
		"public class X {}\n"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b148380/Sub.java",
		"package b148380;\n" +
		"public class Sub extends X {}\n"
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b148380/Y.java",
		"package b148380;\n" +
		"public class Y {}\n"
	);
	IJavaSearchScope scope = getJavaSearchScope();
	TypeNameMatchCollector requestor1 = new TypeNameMatchCollector();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		"b148380".toCharArray(),
		SearchPattern.R_EXACT_MATCH,
		null,
		SearchPattern.R_EXACT_MATCH,
		IJavaSearchConstants.TYPE,
		scope,
		requestor1,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	String expected = "class Sub [in [Working copy] Sub.java [in b148380 [in src [in JavaSearchBugs]]]]\n" +
		"class X [in [Working copy] X.java [in b148380 [in src [in JavaSearchBugs]]]]\n" +
		"class Y [in [Working copy] Y.java [in b148380 [in src [in JavaSearchBugs]]]]\n" +
		"interface I [in [Working copy] I.java [in b148380 [in src [in JavaSearchBugs]]]]";
	assertSearchResults(expected, requestor1);
	//  Expected same result with the wc owner
	TypeNameMatchCollector requestor2 = new TypeNameMatchCollector();
	new SearchEngine(this.wcOwner).searchAllTypeNames(
		"b148380".toCharArray(),
		SearchPattern.R_EXACT_MATCH,
		null,
		SearchPattern.R_EXACT_MATCH,
		IJavaSearchConstants.TYPE,
		scope,
		requestor2,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	assertSearchResults(expected, requestor2);
}
public void testBug148380_SearchAllTypes_cu() throws CoreException, JavaModelException {
	IJavaSearchScope scope = getJavaSearchScope();
	TypeNameMatchCollector requestor = new TypeNameMatchCollector();
	new SearchEngine().searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		"Bug".toCharArray(),
		SearchPattern.R_PREFIX_MATCH,
		IJavaSearchConstants.TYPE,
		scope,
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	assertSearchResults(
		"Bug148380 (not open) [in Bug148380.class [in <default> [in lib [in JavaSearchBugs]]]]",
		requestor
	);
}
public void testBug148380_SearchAllTypes_cu_wksp() throws CoreException, JavaModelException {
	IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
	TypeNameMatchCollector requestor = new TypeNameMatchCollector();
	new SearchEngine().searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		"Bug".toCharArray(),
		SearchPattern.R_PREFIX_MATCH,
		IJavaSearchConstants.TYPE,
		scope,
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	assertSearchResults(
		"Bug148380 (not open) [in Bug148380.class [in <default> [in lib [in JavaSearchBugs]]]]",
		requestor
	);
}

/**
 * @bug 153765: [search] Reference to package is not found in qualified annotation
 * @test Ensure that references to package are also found in qualified annotation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=153765"
 */
public void testBug153765() throws CoreException {
	try {
		createFolder("/JavaSearchBugs/src/b153765");
		createFile("/JavaSearchBugs/src/b153765/Unimportant.java",
			"package b153765;\n" +
			"public @interface Unimportant {}\n"
		);
		createFolder("/JavaSearchBugs/src/b153765/test");
		createFile("/JavaSearchBugs/src/b153765/test/SomeClass.java",
			"package test;\n" +
			"public class SomeClass {\n" +
			"        @b153765.Unimportant public void foo() {}\n" +
			"}"
		);
		waitUntilIndexesReady();
		IPackageFragment packageFragment = getPackage("/JavaSearchBugs/src/b153765");
		this.resultCollector.showSelection();
		search(packageFragment, REFERENCES);
		assertSearchResults(
			"src/b153765/test/SomeClass.java void b153765.test.SomeClass.foo() [        @§|b153765|§.Unimportant public void foo() {}] EXACT_MATCH"
		);
	}
	finally {
		deleteFolder("/JavaSearchBugs/src/b153765");
	}
}

/**
 * @bug 156340: [search] searchAllTypeNames return nothing for empty prefix
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=156340"
 */
public void testBug156340() throws CoreException {
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	IPackageFragment fragment = getPackageFragment("JavaSearchBugs", getExternalJCLPathString("1.5"), "java.lang");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { fragment });
	new SearchEngine().searchAllTypeNames(
	   null,
		SearchPattern.R_EXACT_MATCH,
		CharOperation.NO_CHAR,
		SearchPattern.R_PREFIX_MATCH,
		IJavaSearchConstants.TYPE,
		scope,
		requestor,
		WAIT_UNTIL_READY_TO_SEARCH,
		null
   );
	assertSearchResults(
		"Unexpected all type names",
		"java.lang.CharSequence\n" +
		"java.lang.Class\n" +
		"java.lang.CloneNotSupportedException\n" +
		"java.lang.Comparable\n" +
		"java.lang.Deprecated\n" +
		"java.lang.Enum\n" +
		"java.lang.Error\n" +
		"java.lang.Exception\n" +
		"java.lang.IllegalMonitorStateException\n" +
		"java.lang.InterruptedException\n" +
		"java.lang.Object\n" +
		"java.lang.RuntimeException\n" +
		"java.lang.String\n" +
		"java.lang.Throwable",
		requestor);
}

/**
 * @bug 156177: [1.5][search] interfaces and annotations could be found with only one requets of searchAllTypeName
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=156177"
 */
public void testBug156177() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b156177/Test.java",
		"package b156177;\n" +
		"interface B156177_I {}\n" +
		"enum B156177_E {}\n" +
		"@interface B156177_A {}\n" +
		"public class B156177 {}\n"
	);
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		null,
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		INTERFACE_AND_ANNOTATION,
		getJavaSearchWorkingCopiesScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"b156177.B156177_A\n" +
		"b156177.B156177_I",
		requestor);
}

/**
 * @bug 156491: [1.5][search] interfaces and annotations could be found with only one requets of searchAllTypeName
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=156491"
 */
public void testBug156491() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test.java",
		"package pack;\n" +
		"public class Test {\n" +
		"	void noMatch(Y y) {\n" +
		"		y.toString();\n" +
		"		toString();\n" +
		"	}\n" +
		"	void validMatches(X x) {\n" +
		"		x.toString();\n" +
		"	}\n" +
		"	void superInvocationMatches(Object o) {\n" +
		"		o.toString();\n" +
		"	}\n" +
		"	void interfaceMatches(I i) {\n" +
		"		i.toString();\n" +
		"	}\n" +
		"	void subtypeMatches(Sub s) {\n" +
		"		s.toString();\n" +
		"	}\n" +
		"}\n" +
		"interface I {}\n" +
		"class X {\n" +
		"	public String toString() {\n" +
		"		return \"X\";\n" +
		"	}\n" +
		"}\n" +
		"class Sub extends X {}\n" +
		"class Y {\n" +
		"	public String toString() {\n" +
		"		return \"Y\";\n" +
		"	}\n" +
		"}\n"
	);
	IMethod method = this.workingCopies[0].getType("X").getMethod("toString", new String[0]);
	this.resultCollector.showFlavors = PatternLocator.SUPER_INVOCATION_FLAVOR;
	search(method, REFERENCES);
	assertSearchResults(
		"src/pack/Test.java void pack.Test.validMatches(X) [toString()] EXACT_MATCH\n" +
		"src/pack/Test.java void pack.Test.superInvocationMatches(Object) [toString()] EXACT_MATCH SUPER INVOCATION\n" +
		"src/pack/Test.java void pack.Test.subtypeMatches(Sub) [toString()] EXACT_MATCH\n" + 
		"lib/b125178.jar java.lang.String pack.age.Test.foo(int) EXACT_MATCH SUPER INVOCATION"
	);
}
private void setUpBug156491() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/other/Test.java",
		"package other;\n" +
		"public class Test {\n" +
		"	void testInterface(I i) {\n" +
		"		i.test();\n" +
		"	}\n" +
		"	void testSuperInvocation(L1 l) {\n" +
		"		l.test();\n" +
		"	}\n" +
		"	void testInvocation(L2 l) {\n" +
		"		l.test();\n" +
		"	}\n" +
		"}\n" +
		"class L1 implements I {\n" +
		"	public void test() {}\n" +
		"}\n" +
		"interface I {\n" +
		"	void test();\n" +
		"}\n" +
		"class L2 extends L1 {\n" +
		"	public void test() {}\n" +
		"}"
	);
}
public void testBug156491a() throws CoreException {
	this.resultCollector.showRule();
	setUpBug156491();
	IMethod method = this.workingCopies[0].getType("L2").getMethod("test", new String[0]);
	this.resultCollector.showFlavors = PatternLocator.SUPER_INVOCATION_FLAVOR;
	search(method, REFERENCES);
	assertSearchResults(
		"src/other/Test.java void other.Test.testInterface(I) [test()] EXACT_MATCH SUPER INVOCATION\n" +
		"src/other/Test.java void other.Test.testSuperInvocation(L1) [test()] EXACT_MATCH SUPER INVOCATION\n" +
		"src/other/Test.java void other.Test.testInvocation(L2) [test()] EXACT_MATCH"
	);
}
public void testBug156491b() throws CoreException {
	this.resultCollector.showRule();
	setUpBug156491();
	IMethod method = this.workingCopies[0].getType("L1").getMethod("test", new String[0]);
	this.resultCollector.showFlavors = PatternLocator.SUPER_INVOCATION_FLAVOR;
	search(method, REFERENCES);
	assertSearchResults(
		"src/other/Test.java void other.Test.testInterface(I) [test()] EXACT_MATCH SUPER INVOCATION\n" +
		"src/other/Test.java void other.Test.testSuperInvocation(L1) [test()] EXACT_MATCH"
		// since bug 160301 fix, subclass overridden method calls are not reported
		//"src/other/Test.java void other.Test.testInvocation(L2) [test()] EXACT_MATCH"
	);
}
/**
 * @bug 160301: [search] too many matches found for method references
 * @test Ensure that correct number of method references are found
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=160301"
 */
public void testBug160301() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"public class Test {\n" +
		"	class A {\n" +
		"		void foo() {}\n" +
		"		void bar() {\n" +
		"			foo();\n" +
		"		}\n" +
		"	}\n" +
		"	class B extends A {\n" +
		"		void foo() {}\n" +
		"		void bar() {\n" +
		"			foo();\n" +
		"		}\n" +
		"	}\n" +
		"	class C extends B {\n" +
		"		void method() {\n" +
		"			foo();\n" +
		"		}\n" +
		"	}\n" +
		"}"
	);
	IMethod method = this.workingCopies[0].getType("Test").getType("A").getMethod("foo", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"src/Test.java void Test$A.bar() [foo()] EXACT_MATCH"
	);
}
public void testBug160301b() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
		"package test;\n" +
		"public class Test {\n" +
		"	class A {\n" +
		"		void foo() {}\n" +
		"	}\n" +
		"	class B extends A {}\n" +
		"	class C extends B {\n" +
		"		void foo() {}\n" +
		"	}\n" +
		"	class D extends C {}\n" +
		"	void a() {\n" +
		"		new A().foo();\n" +
		"	}\n" +
		"	void b() {\n" +
		"		new B().foo();\n" +
		"	}\n" +
		"	void c() {\n" +
		"		new C().foo();\n" +
		"	}\n" +
		"	void d() {\n" +
		"		new D().foo();\n" +
		"	}\n" +
		"	\n" +
		"}"
	);
	IMethod method = this.workingCopies[0].getType("Test").getType("A").getMethod("foo", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"src/test/Test.java void test.Test.a() [foo()] EXACT_MATCH\n" +
		"src/test/Test.java void test.Test.b() [foo()] EXACT_MATCH"
	);
}
public void testBug160301_Interface() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"public class Test {\n" +
		"	interface I {\n" +
		"		void foo();\n" +
		"	}\n" +
		"	class A1 implements I {\n" +
		"		public void foo() {}\n" +
		"		void a1() {\n" +
		"			foo();\n" +
		"		}\n" +
		"	}\n" +
		"	class B1 extends A1 {\n" +
		"		void b1() {\n" +
		"			foo();\n" +
		"		}\n" +
		"	}\n" +
		"	class C1 extends B1 {\n" +
		"		public void foo() {}\n" +
		"		void c1() {\n" +
		"			foo();\n" +
		"		}\n" +
		"	}\n" +
		"	abstract class A2 implements I {\n" +
		"		void a2() {\n" +
		"			foo();\n" +
		"		}\n" +
		"	}\n" +
		"	class B2 extends A2 {\n" +
		"		public void foo() {}\n" +
		"		void b2() {\n" +
		"			foo();\n" +
		"		}\n" +
		"	}\n" +
		"	class A3 implements I {\n" +
		"		public void foo() {}\n" +
		"		void a3() {\n" +
		"			foo();\n" +
		"		}\n" +
		"	}\n" +
		"}"
	);
	IMethod method = this.workingCopies[0].getType("Test").getType("I").getMethod("foo", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"src/Test.java void Test$A1.a1() [foo()] EXACT_MATCH\n" +
		"src/Test.java void Test$B1.b1() [foo()] EXACT_MATCH\n" +
		"src/Test.java void Test$A2.a2() [foo()] EXACT_MATCH\n" +
		"src/Test.java void Test$B2.b2() [foo()] EXACT_MATCH\n" +
		"src/Test.java void Test$A3.a3() [foo()] EXACT_MATCH"
	);
}
public void testBug160301_Abstract() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"public class Test {\n" +
		"	abstract class Abstract {\n" +
		"		abstract void foo();\n" +
		"	}\n" +
		"	class A1 extends Abstract {\n" +
		"		public void foo() {}\n" +
		"		void a1() {\n" +
		"			foo(); // valid match as A1.foo() is the first override in sub-class\n" +
		"		}\n" +
		"	}\n" +
		"	class B1 extends A1 {\n" +
		"		void b1() {\n" +
		"			foo(); // valid match as B1 does not override A.foo()\n" +
		"		}\n" +
		"	}\n" +
		"	class C1 extends B1 {\n" +
		"		public void foo() {}\n" +
		"		void c1() {\n" +
		"			foo(); // invalid match as C1 does override A.foo()\n" +
		"		}\n" +
		"	}\n" +
		"	abstract class A2 extends Abstract {\n" +
		"		void a2() {\n" +
		"			foo(); // valid match as A2 does not override Abstract.foo()\n" +
		"		}\n" +
		"	}\n" +
		"	class B2 extends A2 {\n" +
		"		public void foo() {}\n" +
		"		void b2() {\n" +
		"			foo(); // valid match as B2.foo() is the first override in sub-class\n" +
		"		}\n" +
		"	}\n" +
		"}"
	);
	IMethod method = this.workingCopies[0].getType("Test").getType("Abstract").getMethod("foo", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"src/Test.java void Test$A1.a1() [foo()] EXACT_MATCH\n" +
		"src/Test.java void Test$B1.b1() [foo()] EXACT_MATCH\n" +
		"src/Test.java void Test$A2.a2() [foo()] EXACT_MATCH\n" +
		"src/Test.java void Test$B2.b2() [foo()] EXACT_MATCH"
	);
}
public void testBug160301_Abstract2() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"public class Test {\n" +
		"	abstract class Abstract {\n" +
		"		public abstract void foo();\n" +
		"	}\n" +
		"	abstract class A extends Abstract {\n" +
		"		public abstract void foo();\n" +
		"		void a() {\n" +
		"			foo(); // valid match as A is abstract => does not override Abstract.foo()\n" +
		"		}\n" +
		"	}\n" +
		"	class B extends A {\n" +
		"		public void foo() {}\n" +
		"		void b() {\n" +
		"			foo(); // valid match as B.foo() is the first override in sub-class\n" +
		"		}\n" +
		"	}\n" +
		"	class C extends B {\n" +
		"		public void foo() {}\n" +
		"		void c() {\n" +
		"			foo(); // invalid match as C.foo() overrides Abstract.foo() \n" +
		"		}\n" +
		"	}\n" +
		"}"
	);
	IMethod method = this.workingCopies[0].getType("Test").getType("Abstract").getMethod("foo", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"src/Test.java void Test$A.a() [foo()] EXACT_MATCH\n" +
		"src/Test.java void Test$B.b() [foo()] EXACT_MATCH"
	);
}
public void testBug160301_Abstract3() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"public class Test {\n" +
		"	abstract class Abstract {\n" +
		"		public abstract void foo();\n" +
		"	}\n" +
		"	class A extends Abstract {\n" +
		"		public void foo() {}\n" +
		"		void a() {\n" +
		"			foo(); // valid match as A.foo() is the first override in sub-class\n" +
		"		}\n" +
		"	}\n" +
		"	abstract class B extends A {\n" +
		"		public abstract void foo();\n" +
		"		void b() {\n" +
		"			foo(); // invalid match as B.foo() is hidden by the override A.foo()\n" +
		"		}\n" +
		"	}\n" +
		"	class C extends B {\n" +
		"		public void foo() {}\n" +
		"		void c() {\n" +
		"			foo(); // invalid match as C.foo() overrides A.foo()\n" +
		"		}\n" +
		"	}\n" +
		"}"
	);
	IMethod method = this.workingCopies[0].getType("Test").getType("Abstract").getMethod("foo", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"src/Test.java void Test$A.a() [foo()] EXACT_MATCH"
	);
}

/**
 * @bug 160323: [search] TypeNameMatch: support hashCode/equals
 * @test Ensure that match equals and hashCode methods return same values than those of stored {@link IType}.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=160323"
 */
public void testBug160323() throws CoreException {
	// Search all type names with TypeNameMatchRequestor
	TypeNameMatchCollector collector = new TypeNameMatchCollector() {
		public String toString(){
			return toFullyQualifiedNamesString();
		}
	};
	new SearchEngine().searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		null,
		SearchPattern.R_PREFIX_MATCH,
		IJavaSearchConstants.TYPE,
		getJavaSearchScope(),
		collector,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	// Search all type names with TypeNameRequestor
	TypeNameRequestor requestor = new SearchTests.SearchTypeNameRequestor();
	new SearchEngine().searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		null,
		SearchPattern.R_PREFIX_MATCH,
		IJavaSearchConstants.TYPE,
		getJavaSearchScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	// Should have same types with these 2 searches
	assertTrue("We should get some types!", collector.size() > 0);
	assertEquals("Found types sounds not to be correct", requestor.toString(), collector.toString());
}

/**
 * @bug 160324: [search] SearchEngine.searchAllTypeNames(char[][], char[][], TypeNameMatchRequestor
 * @test Ensure that types found using {@link SearchEngine#searchAllTypeNames(char[][], char[][], IJavaSearchScope, TypeNameMatchRequestor, int, org.eclipse.core.runtime.IProgressMonitor) new API method}
 * 	are the same than with already existing API method using {@link TypeNameRequestor}...
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=160324"
 */
public void testBug160324a() throws CoreException {
	boolean debug = false;
	// Search all type names with new API
	TypeNameMatchCollector collector = new TypeNameMatchCollector() {
		public String toString(){
			return toFullyQualifiedNamesString();
		}
	};
	new SearchEngine().searchAllTypeNames(
		null,
		null,
		getJavaSearchScope(),
		collector,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	// Search all type names with old API
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine().searchAllTypeNames(
		null,
		null,
		getJavaSearchScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	if (debug) System.out.println("TypeNameRequestor results: \n"+requestor);
	// Should have same types with these 2 searches
	assertTrue("We should get some types!", collector.size() > 0);
	assertEquals("Found types sounds not to be correct", requestor.toString(), collector.toString());
}
public void testBug160324b() throws CoreException {
	// Search all type names with new API
	TypeNameMatchCollector collector = new TypeNameMatchCollector() {
		public String toString(){
			return toFullyQualifiedNamesString();
		}
	};
	new SearchEngine().searchAllTypeNames(
		null,
		new char[][] { "Test".toCharArray() },
		getJavaSearchScope(),
		collector,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	// Search all type names with old API
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine().searchAllTypeNames(
		null,
		new char[][] { "Test".toCharArray() },
		getJavaSearchScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	// Should have same types with these 2 searches
	assertTrue("We should get some types!", collector.size() > 0);
	assertEquals("Found types sounds not to be correct", requestor.toString(), collector.toString());
}

/**
 * @bug 160494: [search] searchAllTypeNames(char[][], char[][],...) fails to find types in default package
 * @test Ensure that types of default packge are found when empty package is specified in package lists
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=160494"
 */
public void testBug160324c() throws CoreException {
	boolean debug = false;
	char[][] packagesList = new char[][] {
			CharOperation.NO_CHAR,
			"b110422".toCharArray(),
			"b123679.test".toCharArray(),
			"b89848".toCharArray(),
			"b95794".toCharArray(),
			"pack".toCharArray(),
			"pack.age".toCharArray()
	};
	char[][] typesList = new char[][] {
		"Test".toCharArray(),
		"TestPrefix".toCharArray()
	};
	// Search all type names with new API
	TypeNameMatchCollector collector = new TypeNameMatchCollector() {
		public String toString(){
			return toFullyQualifiedNamesString();
		}
	};
	new SearchEngine().searchAllTypeNames(
		packagesList,
		typesList,
		getJavaSearchScope(),
		collector,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	if (debug) System.out.println("TypeNameMatchRequestor results: \n"+collector);
	// Search all type names with old API
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine().searchAllTypeNames(
		packagesList,
		typesList,
		getJavaSearchScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	if (debug) System.out.println("TypeNameRequestor results: \n"+requestor);
	// Should have same types with these 2 searches
	assertEquals("Wrong number of found types!", packagesList.length, collector.size());
	assertEquals("Found types sounds not to be correct", requestor.toString(), collector.toString());
}

/**
 * @bug 160854: [search] No type is found using seachAllTypeNames(char[][],char[][],...) methods when no type names is specified
 * @test Ensure that types are found when typeNames parameter is null...
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=160854"
 */
public void testBug160854() throws CoreException {
	char[][] packagesList = new char[][] {
			"java.lang".toCharArray()
	};
	// Search all type names with new API
	TypeNameMatchCollector collector = new TypeNameMatchCollector() {
		public String toString(){
			return toFullyQualifiedNamesString();
		}
	};
	new SearchEngine().searchAllTypeNames(
		packagesList,
		null,
		getJavaSearchScope(),
		collector,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	// Search all type names with old API
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine().searchAllTypeNames(
		packagesList,
		null,
		getJavaSearchScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	// Should have same types with these 2 searches
	assertSearchResults("Wrong types found!",
		"java.lang.CharSequence\n" +
		"java.lang.Class\n" +
		"java.lang.CloneNotSupportedException\n" +
		"java.lang.Comparable\n" +
		"java.lang.Deprecated\n" +
		"java.lang.Enum\n" +
		"java.lang.Error\n" +
		"java.lang.Exception\n" +
		"java.lang.IllegalMonitorStateException\n" +
		"java.lang.InterruptedException\n" +
		"java.lang.Object\n" +
		"java.lang.RuntimeException\n" +
		"java.lang.String\n" +
		"java.lang.Throwable",
		requestor
	);
	assertEquals("Found types sounds not to be correct", requestor.toString(), collector.toString());
}

/**
 * @bug 161028: [search] NPE on organize imports in TypeNameMatch.equals
 * @test Ensure that no NPE may happen calling <code>equals(Object)</code>,
 * 	<code>hashCode()</code> or <code>toString()</code> methods.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=161028"
 */
public void testBug161028() throws CoreException {
	TypeNameMatch match1 = SearchEngine.createTypeNameMatch(null, 0);
	assertEquals("Should be equals!", match1, match1);
	assertEquals("Wrong toString value!", "org.eclipse.jdt.internal.core.search.JavaSearchTypeNameMatch@0", match1.toString());
	TypeNameMatch match2 = SearchEngine.createTypeNameMatch(null, 0);
	assertFalse("Should NOT be identical!", match1 == match2);
	assertTrue("Should be equals!", match1.equals(match2));
	assertTrue("Should be equals!", match2.equals(match1));
	assertEquals("Wrong toString value!", match1, match2);
	assertEquals("Should have same hashCode!", match1.hashCode(), match2.hashCode());
}

/**
 * @bug 161190: [search] All type search doesn't find all types
 * @test Ensure that access rules does not change searchAllTypeNames results.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=161190"
 */
public void testBug161190() throws CoreException {
	char[][] packagesList = new char[][] {
			"xy".toCharArray()
	};
	// Search all type names with new API
	TypeNameMatchCollector collector = new TypeNameMatchCollector() {
		public String toString(){
			return toFullyQualifiedNamesString();
		}
	};
	new SearchEngine().searchAllTypeNames(
		packagesList,
		null,
		getJavaSearchScope(),
		collector,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	// Search all type names with old API
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine().searchAllTypeNames(
		packagesList,
		null,
		getJavaSearchScope(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	assertSearchResults("Wrong types found!",
		"xy.BE_124645\n" +
		"xy.BX_124645\n" +
		"xy.BY_124645",
		requestor
	);
	// Should have same types with these 2 searches
	assertEquals("Found types sounds not to be correct", requestor.toString(), collector.toString());
}

/**
 * @bug 163984: [search] no results from SearchEngine.searchAllTypeNames with types in scope
 * @test Ensure that types are found even when scope is made of elements
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=163984"
 */
public void testBug163984() throws CoreException {
	// Search all type names with TypeNameMatchRequestor
	TypeNameMatchCollector collector = new TypeNameMatchCollector() {
		public String toString(){
			return toFullyQualifiedNamesString();
		}
	};
	ICompilationUnit[] elements = getCompilationUnits("JavaSearchBugs", "src", "b163984");
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(elements);
	new SearchEngine().searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		new char[] { '*' },
		SearchPattern.R_PATTERN_MATCH,
		IJavaSearchConstants.TYPE,
		scope,
		collector,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	// Search all type names with TypeNameRequestor
	TypeNameRequestor requestor = new SearchTests.SearchTypeNameRequestor();
	new SearchEngine().searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		new char[] { '*' },
		SearchPattern.R_PATTERN_MATCH,
		IJavaSearchConstants.TYPE,
		scope,
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	// Should have same types with these 2 searches
	assertEquals("We should get 3 types!", 3, collector.size());
	assertEquals("Found types sounds not to be correct", requestor.toString(), collector.toString());
}

/**
 * @bug 164121: [search] Misses declarations of method parameters
 * @test Ensure that param declaration are correctly found by search engine
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=164121"
 */
private void setUpBug164121() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/A.java",
		"class A {\n" +
		"     int x(int param) {\n" +
		"         param = 2 + 2;\n" +
		"         int x = param + 2;\n" +
		"         return param - x;\n" +
		"     }\n" +
		"}\n"
	);
}
public void testBug164121a() throws CoreException {
	this.resultCollector.showRule();
	setUpBug164121();
	ILocalVariable param = getLocalVariable(this.workingCopies[0], "param", "param");
	search(param, DECLARATIONS);
	assertSearchResults(
		"src/A.java int A.x(int).param [param] EXACT_MATCH"
	);
}
public void testBug164121b() throws CoreException {
	this.resultCollector.showRule();
	setUpBug164121();
	ILocalVariable param = getLocalVariable(this.workingCopies[0], "param", "param");
	search(param, ALL_OCCURRENCES);
	assertSearchResults(
		"src/A.java int A.x(int).param [param] EXACT_MATCH\n" +
		"src/A.java int A.x(int) [param] EXACT_MATCH\n" +
		"src/A.java int A.x(int) [param] EXACT_MATCH\n" +
		"src/A.java int A.x(int) [param] EXACT_MATCH"
	);
}

/**
 * @bug 164791: [search] Type reference reports anonymous type in invalid class file
 * @test Ensure that match on anonymous type in local type is well reported
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=164791"
 */
public void testBug164791() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b164791.jar", "pack", "ELPM.class").getType();
	JavaSearchResultCollector collector = new JavaSearchResultCollector() {
		public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
			super.acceptSearchMatch(searchMatch);
			IJavaElement element = (IJavaElement) searchMatch.getElement();
			assertTrue("Search match element "+element.getElementName()+" should exist!!!", element.exists());
		}
	};
	collector.showAccuracy(true);
	search(type, REFERENCES, getJavaSearchScope(), collector);
	assertSearchResults(
		"lib/b164791.jar test.<anonymous> EXACT_MATCH\n" +
		"lib/b164791.jar test.<anonymous> EXACT_MATCH",
		collector
	);
}

/**
 * @bug 166348: [search] Stack trace console resolves wrong source
 * @test Ensure that only type with same qualification is found in class files
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=166348"
 */
public void testBug166348() throws CoreException {
	search("Test166348", TYPE, DECLARATIONS);
	assertSearchResults(
		"lib/b166348.jar pack.Test166348 [No source] EXACT_MATCH\n" +
		"lib/b166348.jar test.Test166348 [No source] EXACT_MATCH"
	);
}
public void testBug166348_Qualified() throws CoreException {
	search("test.Test166348", TYPE, DECLARATIONS);
	assertSearchResults(
		"lib/b166348.jar test.Test166348 [No source] EXACT_MATCH"
	);
}

/**
 * @bug 167190: [search] TypeNameMatchRequestorWrapper causing ClassCastException
 * @test Ensure that types are found even when scope is not a {@link org.eclipse.jdt.internal.core.search.JavaSearchScope}
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=167190"
 */
public void testBug167190() throws CoreException, JavaModelException {
	IJavaSearchScope scope = new AbstractSearchScope() {
		IJavaSearchScope jsScope = getJavaSearchScope();
		public void processDelta(IJavaElementDelta delta, int eventType) {
			// we should have no delta on this test case
		}
		public boolean encloses(String resourcePath) {
			return this.jsScope.encloses(resourcePath);
		}
		public boolean encloses(IJavaElement element) {
			return this.jsScope.encloses(element);
		}
		public IPath[] enclosingProjectsAndJars() {
			return this.jsScope.enclosingProjectsAndJars();
		}
	};
	// Search all type names with TypeNameMatchRequestor
	TypeNameMatchCollector collector = new TypeNameMatchCollector() {
		public String toString(){
			return toFullyQualifiedNamesString();
		}
	};
	new SearchEngine().searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		"C".toCharArray(), // need a prefix which returns most of different types (class file, CU, member,...)
		SearchPattern.R_PREFIX_MATCH,
		IJavaSearchConstants.TYPE,
		scope,
		collector,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	// Search all type names with old API
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine().searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		"C".toCharArray(), // need a prefix which returns most of different types (class file, CU, member,...)
		SearchPattern.R_PREFIX_MATCH,
		IJavaSearchConstants.TYPE,
		scope,
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	// Should have same types with these 2 searches
	assertEquals("Found types sounds not to be correct", requestor.toString(), collector.toString());
}

/**
 * @bug 178596: [search] Search for method references does not find references to interface method
 * @test Ensure that searching method reference finds the interface method reference
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=178596"
 */
public void testBug178596() throws CoreException {
	this.workingCopies = new ICompilationUnit[5];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/ClassA.java",
		"public class ClassA implements InterfaceA {\n" +
		"    public void setValue(int aValue) {\n" +
		"    }\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/ClassB.java",
		"public class ClassB extends ClassA implements InterfaceB {}\n"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/InterfaceA.java",
		"public interface InterfaceA {\n" +
		"    public void setValue(int aValue);\n" +
		"}\n"
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/InterfaceB.java",
		"public interface InterfaceB extends InterfaceA {}\n"
	);
	this.workingCopies[4] = getWorkingCopy("/JavaSearchBugs/src/Main.java",
		"public class Main {\n" +
		"    public static void main(String[] args) {\n" +
		"        new Main().run();\n" +
		"    }\n" +
		"    private void run() {\n" +
		"        InterfaceB anB = new ClassB();\n" +
		"        anB.setValue(123);\n" +
		"    }\n" +
		"}\n"
	);
	JavaSearchResultCollector testCollector = new JavaSearchResultCollector() {
		public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
	        super.acceptSearchMatch(searchMatch);
	        assertTrue("Method reference match should be super invocation one!", ((MethodReferenceMatch)searchMatch).isSuperInvocation());
        }

	};
	testCollector.showAccuracy(true);
	IMethod method = this.workingCopies[0].getType("ClassA").getMethod("setValue", new String[] { "I" });
	search(method, REFERENCES, getJavaSearchScope(), testCollector);
	assertSearchResults(
		"src/Main.java void Main.run() [setValue(123)] EXACT_MATCH",
		testCollector
	);
}

/**
 * @bug 178847 [search] Potential matches found when searching references to IJavaElement#getResource()
 * @test Ensure that accurate matches are found
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=178847"
 */
public void testBug178847() throws CoreException {
	try {
		createJavaProject("P1", new String[] {"src" }, "bin");
		createFolder("/P1/src/p");
		createFile(
			"/P1/src/p/X.java",
			"package p;\n" +
			"public class X{\n" +
			"}"
		);
		createFile(
			"/P1/src/p/Y.java",
			"package p;\n" +
			"public class Y extends X {\n" +
			"  public static void foo() {}\n" +
			"}"
		);
		getProject("P1").build(IncrementalProjectBuilder.FULL_BUILD, null);
		deleteFile("/P1/bin/p/X.class");
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB", "/P1/bin"}, "");
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy("/P2/Test1.java",
			"public class Test1 {\n" +
			"  void bar() {\n" +
			"    p.Y.foo();\n" +
			"    new p.X();\n" + // cause AbortCompilation here
			"  }\n" +
			"}"
		);
		this.workingCopies[1] = getWorkingCopy("/P2/Test2.java",
			"public class Test2 {\n" +
			"  void foo() {}\n" +
			"  void bar() {\n" +
			"    foo();\n" +
			"  }\n" +
			"}"
		);
		IMethod method = this.workingCopies[1].getType("Test2").getMethod("foo", new String[0]);
		search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
		assertSearchResults(
			"Test2.java void Test2.bar() [foo()] EXACT_MATCH"
		);
	} finally {
		deleteProjects(new String[] {"P1", "P2" });
	}
}

/**
 * @bug 181488 [index] Lots of unbuffered sequential reads in DiskIndex
 * @test Ensure that indexing does not happen while reopening workspace (see bug 195091)
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=181488"
 */
public void testBug181488a() throws CoreException {
	waitUntilIndexesReady();
	IndexManager manager = JavaModelManager.getIndexManager();
	Index index = manager.getIndex(JAVA_PROJECT.getPath(), true, false);
	File indexFile = index.getIndexFile();
	simulateExit();
	long lastModified = indexFile.lastModified();
	simulateRestart();
	waitUntilIndexesReady();
	Index newIndex = manager.getIndex(JAVA_PROJECT.getPath(), true, false);
	assertEquals("Index file should be unchanged!!!", lastModified, newIndex.getIndexFile().lastModified());
}
public void testBug181488b() throws CoreException {
	IJavaProject project = createJavaProject("Bug181488");
	try {
		waitUntilIndexesReady();
		IndexManager manager = JavaModelManager.getIndexManager();
		Index index = manager.getIndex(project.getPath(), true, false);
		assertEquals("Index file should at least contains the signature!!!", DiskIndex.SIGNATURE.length()+6, index.getIndexFile().length());
	}
	finally {
		deleteProject(project);
	}
}

/**
 * @bug 185452 [search] for all packages seems hung
 * @test Ensure that all package declarations are found only once
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=185452"
 */
public void testBug185452() throws CoreException {
	JavaSearchResultCollector packageCollector = new JavaSearchResultCollector(true);
	search(
		"*",
		PACKAGE,
		DECLARATIONS,
		SearchEngine.createWorkspaceScope(),
		packageCollector);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " \n" +
		""+ getExternalJCLPathString("1.5") + " java\n" +
		""+ getExternalJCLPathString("1.5") + " java.io\n" +
		""+ getExternalJCLPathString("1.5") + " java.lang\n" +
		""+ getExternalJCLPathString("1.5") + " java.lang.annotation\n" +
		""+ getExternalJCLPathString("1.5") + " java.util\n" +
		"lib \n" +
		"lib/JavaSearch15.jar  [No source]\n" +
		"lib/JavaSearch15.jar g1 [No source]\n" +
		"lib/JavaSearch15.jar g1.t [No source]\n" +
		"lib/JavaSearch15.jar g1.t.s [No source]\n" +
		"lib/JavaSearch15.jar g1.t.s.def [No source]\n" +
		"lib/JavaSearch15.jar g5 [No source]\n" +
		"lib/JavaSearch15.jar g5.c [No source]\n" +
		"lib/JavaSearch15.jar g5.c.def [No source]\n" +
		"lib/JavaSearch15.jar g5.m [No source]\n" +
		"lib/JavaSearch15.jar g5.m.def [No source]\n" +
		"lib/b110422.jar  [No source]\n" +
		"lib/b110422.jar b110422 [No source]\n" +
		"lib/b123679.jar  [No source]\n" +
		"lib/b123679.jar pack [No source]\n" +
		"lib/b123679.jar test [No source]\n" +
		"lib/b124469.jar  [No source]\n" +
		"lib/b124469.jar pack [No source]\n" +
		"lib/b124469.jar test [No source]\n" +
		"lib/b124645.jar  [No source]\n" +
		"lib/b124645.jar test [No source]\n" +
		"lib/b124645.jar xy [No source]\n" +
		"lib/b125178.jar  [No source]\n" +
		"lib/b125178.jar pack [No source]\n" +
		"lib/b125178.jar pack.age [No source]\n" +
		"lib/b126330.jar  [No source]\n" +
		"lib/b128877.jar  [No source]\n" +
		"lib/b128877.jar pack [No source]\n" +
		"lib/b137984.jar  [No source]\n" +
		"lib/b140156.jar  [No source]\n" +
		"lib/b164791.jar  [No source]\n" +
		"lib/b164791.jar pack [No source]\n" +
		"lib/b164791.jar test [No source]\n" +
		"lib/b166348.jar  [No source]\n" +
		"lib/b166348.jar pack [No source]\n" +
		"lib/b166348.jar test [No source]\n" +
		"lib/b86293.jar  [No source]\n" +
		"lib/b87627.jar  [No source]\n" +
		"lib/b87627.jar b87627 [No source]\n" +
		"lib/b89848 b89848\n" +
		"lib/b95152.jar  [No source]\n" +
		"lib/b95152.jar b95152 [No source]\n" +
		"lib/test75816.jar  [No source]\n" +
		"lib/test81556.jar  [No source]\n" +
		"lib/test81556.jar b81556 [No source]\n" +
		"lib/test81556.jar b81556.b [No source]\n" +
		"src \n" +
		"src/b108088 b108088\n" +
		"src/b123679 b123679\n" +
		"src/b123679/pack b123679.pack\n" +
		"src/b123679/test b123679.test\n" +
		"src/b124645 b124645\n" +
		"src/b124645/test b124645.test\n" +
		"src/b124645/xy b124645.xy\n" +
		"src/b127628 b127628\n" +
		"src/b137984 b137984\n" +
		"src/b142044 b142044\n" +
		"src/b163984 b163984\n" +
		"src/b201064 b201064\n" +
		"src/b81556 b81556\n" +
		"src/b81556/a b81556.a\n" +
		"src/b86380 b86380\n" +
		"src/b95794 b95794",
		packageCollector);
}

/**
 * @bug 194185 [search] for package declarations finds also sub-packages
 * @test Ensure that exact package is found when no
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=194185"
 */
public void testBug194185() throws CoreException {
	JavaSearchResultCollector packageCollector = new JavaSearchResultCollector(true);
	search(
		"java",
		PACKAGE,
		DECLARATIONS,
		SearchEngine.createWorkspaceScope(),
		packageCollector);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java",
		packageCollector);
}

/**
 * @bug 195489: [search] References not found while using SearchEngine.searchDeclarationsOfReferencedTypes
 * @test Verify that the type declaration match is always outside the javadoc and that the workaround described in bug 108053 works well
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=108053"
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=195489"
 */
public void testBug195489a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b195489/Test.java",
		"package b195489;\n" +
		"/**\n" +
		" * @see Ref\n" +
		" */\n" +
		"public class Test {\n" +
		"	public Ref ref;\n" +
		"	public Ref getRef() {\n" +
		"		return this.ref;\n" +
		"	}\n" +
		"}\n" +
		"class Ref {}"
	);
	this.resultCollector.showInsideDoc();
	new SearchEngine(this.workingCopies).searchDeclarationsOfReferencedTypes(this.workingCopies[0], this.resultCollector, null);
	assertSearchResults(
		"src/b195489/Test.java b195489.Ref [Ref] EXACT_MATCH OUTSIDE_JAVADOC"
	);
}
public void testBug195489b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b195489/Test.java",
		"package b195489;\n" +
		"public class Test {\n" +
		"	public Ref ref;\n" +
		"	/**\n" +
		"	 * @see Ref\n" +
		"	 */\n" +
		"	public Ref getRef() {\n" +
		"		return this.ref;\n" +
		"	}\n" +
		"}\n" +
		"class Ref {}"
	);
	this.resultCollector.showInsideDoc();
	new SearchEngine(this.workingCopies).searchDeclarationsOfReferencedTypes(this.workingCopies[0], this.resultCollector, null);
	assertSearchResults(
		"src/b195489/Test.java b195489.Ref [Ref] EXACT_MATCH OUTSIDE_JAVADOC"
	);
}
public void testBug195489c() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b195489/Test.java",
		"package b195489;\n" +
		"/**\n" +
		" * @see Ref\n" +
		" */\n" +
		"public class Test {\n" +
		"}\n" +
		"class Ref {}"
	);
	this.resultCollector.showInsideDoc();
	new SearchEngine(this.workingCopies).searchDeclarationsOfReferencedTypes(this.workingCopies[0], this.resultCollector, null);
	assertSearchResults(
		"src/b195489/Test.java b195489.Ref [Ref] EXACT_MATCH OUTSIDE_JAVADOC"
	);
}
// test case for bug 108053 workaround
public void testBug195489d() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b195489/Test.java",
		"package b195489;\n" +
		"/**\n" +
		" * @see Ref\n" +
		" */\n" +
		"public class Test {\n" +
		"}\n" +
		"class Ref {}"
	);
	this.resultCollector.showInsideDoc();
	String docCommentSupport = JAVA_PROJECT.getOption(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, true);
	JAVA_PROJECT.setOption(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.DISABLED);
	try {
		new SearchEngine(this.workingCopies).searchDeclarationsOfReferencedTypes(this.workingCopies[0], this.resultCollector, null);
		assertSearchResults("");
	}
	finally {
		JAVA_PROJECT.setOption(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, docCommentSupport);
	}
}

/**
 * @bug 196339: [search] SearchEngine not returning correct result
 * @test 1) That potential match are now well found while searching for implementors
 * 			2) That there's a workaround for this problem
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=196339"
 * @throws CoreException
 */
public void testBug196339() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b196339/x/y/z/Test.java",
		"package b196339.xy.y.z;\n" +
		"import a.b.c.Foo196339;\n" +
		"public class Test implements Foo196339 {\n" +
		"}\n"
	);
	search("a.b.c.Foo196339", IJavaSearchConstants.TYPE, IJavaSearchConstants.IMPLEMENTORS);
	assertSearchResults(
		"src/b196339/x/y/z/Test.java b196339.x.y.z.Test [Foo196339] POTENTIAL_MATCH"
	);
}
// Possible workaround until this bug is fixed
// Following test passed before the fix for bug 196339 was applied
public void testBug196339b() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b196339/x/y/z/Test1.java",
		"package b196339.xy.y.z;\n" +
		"import a.b.c.Foo196339;\n" +
		"public class Test1 implements Foo196339 {\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b196339/x/y/z/Test2.java",
		"package b196339.xy.y.z;\n" +
		"import a.b.c.*;\n" +
		"public class Test2 implements Foo196339 {\n" +
		"}\n"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b196339/x/y/z/Test3.java",
		"package b196339.xy.y.z;\n" +
		"public class Test3 implements a.b.c.Foo196339 {\n" +
		"}\n"
	);

	final String qualifiedType = "a.b.c.Foo196339";
	JavaSearchResultCollector collector = new JavaSearchResultCollector() {
		public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
        Object element = searchMatch.getElement();
        if (element instanceof IType) {
            IType type = (IType) element;
            // Look if super interface names matches the qualified type
            String[] superInterfaces = type.getSuperInterfaceNames();
            int length = superInterfaces == null ? 0 : superInterfaces.length;
            for (int i=0; i<length; i++) {
                if (superInterfaces[i].equals(qualifiedType)) {
                    super.acceptSearchMatch(searchMatch);
                    return;
                }
            }
            // Look if an import declaration matches the qualified type
            IImportDeclaration[] imports = ((ICompilationUnit) type.getAncestor(IJavaElement.COMPILATION_UNIT)).getImports();
            length = imports == null ? 0 : imports.length;
            for (int i=0; i<length; i++) {
                String importName = imports[i].getElementName();
                if (importName.equals(qualifiedType)) {
                    super.acceptSearchMatch(searchMatch);
                    return;
                }
                if (imports[i].isOnDemand()) {
                    int idx = importName.lastIndexOf('.');
                    if (idx > 0 && importName.substring(0, idx).equals(qualifiedType.substring(0, idx))) {
                        super.acceptSearchMatch(searchMatch);
                        return;
                    }
                }
            }
        }
		}

	};
	search("Foo196339", IJavaSearchConstants.TYPE, IJavaSearchConstants.IMPLEMENTORS, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/b196339/x/y/z/Test1.java b196339.x.y.z.Test1 [Foo196339]\n" +
		"src/b196339/x/y/z/Test2.java b196339.x.y.z.Test2 [Foo196339]\n" +
		"src/b196339/x/y/z/Test3.java b196339.x.y.z.Test3 [a.b.c.Foo196339]",
		collector
	);
}

/**
 * @bug 199004: [search] Java Search in 'JRE libraries' finds matches in Application Libraries
 * @test 1) That only match in system libraries are returned when SYSTEM_LIBRARIES is used in scope
 * 			2) That only match outside system libraries are returned when SYSTEM_LIBRARIES is NOT used in scope
 * 			3) That match in system libraries are returned when no mask is used in scope
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=199004"
 * @throws CoreException
 */
public void testBug199004_SystemLibraries() throws CoreException {
	DefaultContainerInitializer intializer = new DefaultContainerInitializer(new String[] {"JavaSearchBugs", "/JavaSearchBugs/lib/b199004.jar"}) {
		protected DefaultContainer newContainer(char[][] libPaths) {
			return new DefaultContainer(libPaths) {
				public int getKind() {
					return IClasspathContainer.K_SYSTEM;
				}
			};
		}
	};
	ContainerInitializer.setInitializer(intializer);
	Path libPath = new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER");
	addClasspathEntry(JAVA_PROJECT, JavaCore.newContainerEntry(libPath));
	try {
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { JAVA_PROJECT }, IJavaSearchScope.SYSTEM_LIBRARIES);
		search("length", IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, scope);
		assertSearchResults(
			"lib/b199004.jar int Test.length() EXACT_MATCH"
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, libPath);
	}
}
public void testBug199004_DefaultSystemLibraries() throws CoreException {
	DefaultContainerInitializer intializer = new DefaultContainerInitializer(new String[] {"JavaSearchBugs", "/JavaSearchBugs/lib/b199004.jar"}) {
		protected DefaultContainer newContainer(char[][] libPaths) {
			return new DefaultContainer(libPaths) {
				public int getKind() {
					return IClasspathContainer.K_DEFAULT_SYSTEM;
				}
			};
		}
	};
	ContainerInitializer.setInitializer(intializer);
	Path libPath = new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER");
	addClasspathEntry(JAVA_PROJECT, JavaCore.newContainerEntry(libPath));
	try {
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { JAVA_PROJECT }, IJavaSearchScope.SYSTEM_LIBRARIES);
		search("length", IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, scope);
		assertSearchResults(
			"lib/b199004.jar int Test.length() EXACT_MATCH"
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, libPath);
	}
}
public void testBug199004_ApplicationLibraries() throws CoreException {
	DefaultContainerInitializer intializer = new DefaultContainerInitializer(new String[] {"JavaSearchBugs", "/JavaSearchBugs/lib/b199004.jar"}) {
		protected DefaultContainer newContainer(char[][] libPaths) {
			return new DefaultContainer(libPaths) {
				public int getKind() {
					return IClasspathContainer.K_SYSTEM;
				}
			};
		}
	};
	ContainerInitializer.setInitializer(intializer);
	Path libPath = new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER");
	addClasspathEntry(JAVA_PROJECT, JavaCore.newContainerEntry(libPath));
	try {
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES | IJavaSearchScope.REFERENCED_PROJECTS;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { JAVA_PROJECT }, mask);
		search("length", IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, scope);
		assertSearchResults(
			""+ getExternalJCLPathString("1.5") + " int java.lang.CharSequence.length() EXACT_MATCH\n" +
			""+ getExternalJCLPathString("1.5") + " int java.lang.String.length() EXACT_MATCH"
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, libPath);
	}
}
public void testBug199004_NoMask() throws CoreException {
	DefaultContainerInitializer intializer = new DefaultContainerInitializer(new String[] {"JavaSearchBugs", "/JavaSearchBugs/lib/b199004.jar"}) {
		protected DefaultContainer newContainer(char[][] libPaths) {
			return new DefaultContainer(libPaths) {
				public int getKind() {
					return IClasspathContainer.K_SYSTEM;
				}
			};
		}
	};
	ContainerInitializer.setInitializer(intializer);
	Path libPath = new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER");
	addClasspathEntry(JAVA_PROJECT, JavaCore.newContainerEntry(libPath));
	try {
		search("length", IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS);
		assertSearchResults(
			""+ getExternalJCLPathString("1.5") + " int java.lang.CharSequence.length() EXACT_MATCH\n" +
			""+ getExternalJCLPathString("1.5") + " int java.lang.String.length() EXACT_MATCH\n" +
			"lib/b199004.jar int Test.length() EXACT_MATCH"
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, libPath);
	}
}

/**
 * @bug 200064: [search] ResourceException while searching for method reference
 * @test Ensure that indexing still works properly after close/restart
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=200064"
 */
public void testBug200064() throws CoreException {
	waitUntilIndexesReady();
	simulateExitRestart();
	waitUntilIndexesReady();
	// Search all type names with TypeNameMatchRequestor
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	new SearchEngine().searchAllTypeNames(
		null,
		SearchPattern.R_EXACT_MATCH,
		"Object".toCharArray(),
		SearchPattern.R_PREFIX_MATCH,
		IJavaSearchConstants.TYPE,
		getJavaSearchScope(),
		collector,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null);
	assertSearchResults(
		"Object (not open) [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]",
		collector
	);
}

/**
 * @bug 201064: [search] SearchEngine.searchAllTypeNames(..) does not find CamelCase match
 * @test Ensure that indexing still works properly after close/restart
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=201064"
 */
public void testBug201064a_CamelCase() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CCase", SearchPattern.R_CAMELCASE_MATCH, collector);
	assertSearchResults(
		"CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CamelCaseEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CamelCasexxEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CxxCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CxxxxCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]",
		collector
	);
}
public void testBug201064b_CamelCase() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CaCase", SearchPattern.R_CAMELCASE_MATCH, collector);
	assertSearchResults(
		"CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CamelCaseEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CamelCasexxEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]",
		collector
	);
}
public void testBug201064c_CamelCase() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CamelCase", SearchPattern.R_CAMELCASE_MATCH, collector);
	assertSearchResults(
		"CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CamelCaseEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CamelCasexxEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]",
		collector
	);
}
public void testBug201064d_CamelCase() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CC", SearchPattern.R_CAMELCASE_MATCH, collector);
	assertSearchResults(
		"CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CamelCaseEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CamelCasexxEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CxxCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CxxxxCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]",
		collector
	);
}
public void testBug201064e_CamelCase() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CaC", SearchPattern.R_CAMELCASE_MATCH, collector);
	assertSearchResults(
		"CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CamelCaseEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CamelCasexxEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]",
		collector
	);
}
public void testBug201064f_CamelCase() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CamelC", SearchPattern.R_CAMELCASE_MATCH, collector);
	assertSearchResults(
		"CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CamelCaseEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CamelCasexxEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]",
		collector
	);
}
public void testBug201064g_CamelCase() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CCa", SearchPattern.R_CAMELCASE_MATCH, collector);
	assertSearchResults(
		"CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CamelCaseEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CamelCasexxEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CxxCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CxxxxCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]",
		collector
	);
}
public void testBug201064h_CamelCase() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CaCa", SearchPattern.R_CAMELCASE_MATCH, collector);
	assertSearchResults(
		"CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CamelCaseEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CamelCasexxEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]",
		collector
	);
}
public void testBug201064i_CamelCase() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CamelCa", SearchPattern.R_CAMELCASE_MATCH, collector);
	assertSearchResults(
		"CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CamelCaseEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CamelCasexxEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]",
		collector
	);
}
// Same tests using SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH
public void testBug201064a_CamelCaseSamePartCount() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CCase", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, collector);
	assertSearchResults(
		"CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CxxCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CxxxxCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]",
		collector
	);
}
public void testBug201064b_CamelCaseSamePartCount() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CaCase", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, collector);
	assertSearchResults(
		"CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]",
		collector
	);
}
public void testBug201064c_CamelCaseSamePartCount() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CamelCase", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, collector);
	assertSearchResults(
		"CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]",
		collector
	);
}
public void testBug201064d_CamelCaseSamePartCount() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CC", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, collector);
	assertSearchResults(
		"CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CxxCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CxxxxCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]",
		collector
	);
}
public void testBug201064e_CamelCaseSamePartCount() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CaC", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, collector);
	assertSearchResults(
		"CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]",
		collector
	);
}
public void testBug201064f_CamelCaseSamePartCount() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CamelC", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, collector);
	assertSearchResults(
		"CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]",
		collector
	);
}
public void testBug201064g_CamelCaseSamePartCount() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CCa", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, collector);
	assertSearchResults(
		"CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CxxCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CxxxxCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]",
		collector
	);
}
public void testBug201064h_CamelCaseSamePartCount() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CaCa", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, collector);
	assertSearchResults(
		"CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]\n" +
		"CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]",
		collector
	);
}
public void testBug201064i_CamelCaseSamePartCount() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CamelCa", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, collector);
	assertSearchResults(
		"CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]",
		collector
	);
}

/**
 * @bug 204652 "Open Type": ClassCastException in conjunction with a class folder
 * @test Ensure that no ClassCastException is thrown for a library folder with a jar like name
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=204652"
 */
public void testBug204652() throws CoreException {
	IJavaProject javaProject = getJavaProject("JavaSearchBugs");
	IClasspathEntry[] originalRawClasspath = javaProject.getRawClasspath();
	try {
		addLibraryEntry(javaProject, new Path("/JavaSearchBugs/b204652.jar"), false/*not exported*/);
		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		searchAllTypeNames("b204652", null, SearchPattern.R_PREFIX_MATCH, collector);
		IPackageFragment pkg = getPackage("/JavaSearchBugs/b204652.jar/b204652");
		pkg.open(null);
		IType result = (IType) collector.matches.get(0);
		assertTrue("Resulting type should exist", result.exists());
	} finally {
		javaProject.setRawClasspath(originalRawClasspath, null);
	}
}

/**
 * @bug 207657: [search] Exception when refactoring member type to top-level.
 * @test Ensure that searching method reference does not find wrong interface call
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=207657"
 */
public void testBug207657() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Relationship.java",
		"package test;\n" + 
		"public class Relationship {\n" + 
		"    static public class End extends ConnectionEnd<Visitor> {\n" +
		"        public void accept(Visitor visitor) {\n" + 
		"            visitor.visitRelationshipEnd(this);\n" + 
		"        }\n" + 
		"    }\n" + 
		"}\n" +
		"interface Visitor {\n" + 
		"    boolean visitRelationshipEnd(Relationship.End end);\n" + 
		"    boolean visitAssociationEnd(Association.End end);\n" + 
		"}\n" +
		"abstract class ConnectionEnd<V extends Visitor> {\n" + 
		"    public abstract void accept( V visitor );\n" + 
		"}\n" +
		"class Association extends Relationship {\n" + 
		"    static public class RelEnd extends Relationship.End {\n" + 
		"        public void accept(Visitor visitor) {\n" + 
		"            visitor.visitAssociationEnd(this);\n" + 
		"        }\n" + 
		"    }\n" + 
		"}\n"
	);
	IType type = this.workingCopies[0].getType("Relationship").getType("End");
	searchDeclarationsOfSentMessages(type, this.resultCollector);
	assertSearchResults(
		"src/test/Relationship.java boolean test.Visitor.visitRelationshipEnd(Relationship.End) [visitRelationshipEnd(Relationship.End end)] EXACT_MATCH"
	);
}

/**
 * @bug 209054: [search] for references to method finds wrong interface call
 * @test Ensure that searching method reference does not find wrong interface call
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=209054"
 */
public void testBug209054() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/xy/Try.java",
		"package xy;\n" +
		"public class Try implements IReferenceUpdating {\n" +
		"        IMovePolicy fInter;\n" +
		"        boolean canDo() { // find references\n" +
		"                return fInter.canDo(); // not a reference\n" +
		"        }\n" +
		"}\n" +
		"interface IMovePolicy extends IReferenceUpdating {\n" +
		"        boolean canDo();\n" +
		"}\n" +
		"interface IReferenceUpdating {}"
	);
	IMethod method = this.workingCopies[0].getType("Try").getMethod("canDo", new String[0]);
	search(method, REFERENCES);
	assertSearchResults("");
}

/**
 * @bug 209778: [search] TypeReferenceMatch#getOtherElements() fails for match in annotation
 * @test Ensure that the local element is no longer a local variable
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=209778"
 */
public void testBug209778() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/xy/Try.java",
		"package xy;\n" +
		"\n" +
		"public class Try {\n" +
		"        void tryB(int tryKind) {\n" +
		"                @Constants(Try.class) int tryCopy, tryCopy2= tryKind;\n" +
		"        }\n" +
		"        @Constants(value= Try.class) Object fTryA, fTryB;\n" +
		"}\n" +
		"\n" +
		"@interface Constants {\n" +
		"        Class<?> value();\n" +
		"}"
	);
	IType type = this.workingCopies[0].getType("Try");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/xy/Try.java xy.Try.fTryA [Try]+[@Constants on fTryA]+[@Constants on fTryB]\n" + 
		"src/xy/Try.java void xy.Try.tryB(int) [Try]+[@Constants on tryCopy]+[@Constants on tryCopy2]",
		collector
	);
}

/**
 * @bug 209996: [search] Add a way to access the most local enclosing annotation for reference search matches
 * @test Verify the behavior of the new Search API {@link ReferenceMatch#getLocalElement()}
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=209996"
 */
public void testBug209996a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
		"package test;\n" +
		"public class Test {\n" +
		"    void method() {\n" +
		"        @Annot(clazz=Test.class) int x;\n" +
		"    }\n" +
		"}\n" +
		"@interface Annot {\n" +
		"    Class clazz();\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("Test");
	ReferenceCollector collector = new ReferenceCollector();
	collector.showSelection();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/test/Test.java void test.Test.method() [        @Annot(clazz=§|Test|§.class) int x;]+[@Annot on x]",
		collector
	);
}
public void testBug209996b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
		"package test;\n" +
		"public class Test {\n" +
	"        @Deprecated foo() {}\n" +
		"}\n"
	);
	ReferenceCollector collector = new ReferenceCollector();
	collector.showSelection();
	search("Deprecated", TYPE, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/test/Test.java void test.Test.foo() [        @§|Deprecated|§ foo() {}]+[@Deprecated on foo]",
		collector
	);
}
public void testBug209996_c5() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/comment5/Ref.java",
		"package comment5;\n" +
		"public class Ref {\n" +
		"    void doA(Ref ref) {}\n" +
		"    void doB(List<Ref> ref) {}\n" +
		"    void doC(@Tag(Ref.class) Ref ref) {}\n" +
		"    void dontD(@Tag(Ref.class) Object ref) {}\n" +
		"}\n" +
		"\n" +
		"@interface Tag {\n" +
		"    Class value();\n" +
		"}\n" +
		"class List<T> {\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("Ref");
	ReferenceCollector collector = new ReferenceCollector();
	collector.showSelection();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/comment5/Ref.java void comment5.Ref.doA(Ref) [    void doA(§|Ref|§ ref) {}]+[ref]\n" + 
		"src/comment5/Ref.java void comment5.Ref.doB(List<Ref>) [    void doB(List<§|Ref|§> ref) {}]+[ref]\n" + 
		"src/comment5/Ref.java void comment5.Ref.doC(Ref) [    void doC(@Tag(§|Ref|§.class) Ref ref) {}]+[@Tag on ref]\n" + 
		"src/comment5/Ref.java void comment5.Ref.doC(Ref) [    void doC(@Tag(Ref.class) §|Ref|§ ref) {}]+[ref]\n" + 
		"src/comment5/Ref.java void comment5.Ref.dontD(Object) [    void dontD(@Tag(§|Ref|§.class) Object ref) {}]+[@Tag on ref]",
		collector
	);
}
public void testBug209996_c10() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/comment10/Ref.java",
		"package comment10;\n" +
		"@Num(number= Num.CONST)\n" +
		"@interface Num {\n" +
		"    public static final int CONST= 42;\n" +
		"    int number();\n" +
		"}\n"
	);
	IField field = this.workingCopies[0].getType("Num").getField("CONST");
	ReferenceCollector collector = new ReferenceCollector();
	collector.showSelection();
	search(field, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/comment10/Ref.java comment10.Num [@Num(number= Num.§|CONST|§)]+[@Num on Num]",
		collector
	);
}
public void testBug209996_c22_3() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/comment22/Test.java",
		"package comment22;\n" +
		"public class Test {\n" +
		"    @Tag Test test1, test2, test3;\n" +
		"    void method() {\n" +
		"        @Tag Test local= null;\n" +
		"        @Tag Test local1, local2, local3;\n" +
		"    }\n" +
		"}\n" +
		"@interface Tag {}\n"
	);
	IType type = this.workingCopies[0].getType("Tag");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	collector.showSelection();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/comment22/Test.java comment22.Test.test1 [    @§|Tag|§ Test test1, test2, test3;]+[@Tag on test1]+[@Tag on test2,@Tag on test3]\n" + 
		"src/comment22/Test.java void comment22.Test.method() [        @§|Tag|§ Test local= null;]+[@Tag on local]\n" + 
		"src/comment22/Test.java void comment22.Test.method() [        @§|Tag|§ Test local1, local2, local3;]+[@Tag on local1]+[@Tag on local2,@Tag on local3]",
		collector
	);
}
public void testBug209996_c22_4() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
		"package test;\n" +
		"public class TestMethodReference {\n" +
		"    @Annot(clazz = test.Test.class) int x, y;\n" +
		"}\n" +
		"@interface Annot {\n" +
		"    Class clazz();\n" +
		"}\n"
	);
	IType type = this.workingCopies[0].getType("Test");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	collector.showSelection();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/test/Test.java test.TestMethodReference.x [    @Annot(clazz = §|test.Test|§.class) int x, y;]+[@Annot on x]+[@Annot on y]",
		collector
	);
}

/**
 * @bug 210689: [search] Type references are not found in import declarations when JUnit tests only use working copies
 * @test Ensure that import references are found when searching on working copies not written on disk
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=210689"
 */
public void testBug210689() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test210689.java",
		"package pack;\n" +
		"public class Test210689 {}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/X.java",
		"package test;\n" +
		"import pack.Test210689;\n" +
		"public class X extends Test210689 {}\n"
	);
	search(this.workingCopies[0].getType("Test210689"), REFERENCES);
	assertSearchResults(
		"src/test/X.java [pack.Test210689] EXACT_MATCH\n" +
		"src/test/X.java test.X [Test210689] EXACT_MATCH"
	);
}

/**
 * @bug 210567: [1.5][search] Parameterized type reference not found when used in type parameter bounds
 * @test Ensure that all type references are found when used in type variables
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=210567"
 */
public void testBug210567() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/generics/Generic.java",
		"package generics;\n" +
		"import java.io.Serializable;\n" +
		"import type.def.Types;\n" +
		"public class Generic<T extends Types, U extends Types & Comparable<Types> & Serializable, V extends A<? super Types>> {\n" +
		"	Generic<? extends Types, ?, ?> field;\n" +
		"	Comparable<String> comp;\n" +
		"	Class<? extends Exception> clazz;\n" +
		"}\n" +
		"\n" +
		"class A<R> {}"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/type/def/Types.java",
		"package type.def;\n" +
		"public class Types {\n" +
		"}\n"
	);
	this.resultCollector.showSelection();
	search("*", TYPE, REFERENCES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/generics/Generic.java [import §|java.io.Serializable|§;] EXACT_MATCH\n" +
		"src/generics/Generic.java [import §|type.def.Types|§;] EXACT_MATCH\n" +
		"src/generics/Generic.java generics.Generic [public class Generic<T extends §|Types|§, U extends Types & Comparable<Types> & Serializable, V extends A<? super Types>> {] EXACT_MATCH\n" +
		"src/generics/Generic.java generics.Generic [public class Generic<T extends Types, U extends §|Types|§ & Comparable<Types> & Serializable, V extends A<? super Types>> {] EXACT_MATCH\n" +
		"src/generics/Generic.java generics.Generic [public class Generic<T extends Types, U extends Types & §|Comparable|§<Types> & Serializable, V extends A<? super Types>> {] EXACT_MATCH\n" +
		"src/generics/Generic.java generics.Generic [public class Generic<T extends Types, U extends Types & Comparable<§|Types|§> & Serializable, V extends A<? super Types>> {] EXACT_MATCH\n" +
		"src/generics/Generic.java generics.Generic [public class Generic<T extends Types, U extends Types & Comparable<Types> & §|Serializable|§, V extends A<? super Types>> {] EXACT_MATCH\n" +
		"src/generics/Generic.java generics.Generic [public class Generic<T extends Types, U extends Types & Comparable<Types> & Serializable, V extends §|A|§<? super Types>> {] EXACT_MATCH\n" +
		"src/generics/Generic.java generics.Generic [public class Generic<T extends Types, U extends Types & Comparable<Types> & Serializable, V extends A<? super §|Types|§>> {] EXACT_MATCH\n" +
		"src/generics/Generic.java generics.Generic.field [	§|Generic|§<? extends Types, ?, ?> field;] EXACT_MATCH\n" +
		"src/generics/Generic.java generics.Generic.field [	Generic<? extends §|Types|§, ?, ?> field;] EXACT_MATCH\n" +
		"src/generics/Generic.java generics.Generic.comp [	§|Comparable|§<String> comp;] EXACT_MATCH\n" +
		"src/generics/Generic.java generics.Generic.comp [	Comparable<§|String|§> comp;] EXACT_MATCH\n" +
		"src/generics/Generic.java generics.Generic.clazz [	§|Class|§<? extends Exception> clazz;] EXACT_MATCH\n" +
		"src/generics/Generic.java generics.Generic.clazz [	Class<? extends §|Exception|§> clazz;] EXACT_MATCH"
	);
}

/**
 * @bug 210691: [search] Type references position invalid in import references when using "*" pattern
 * @test Ensure that all qualified type reference in import references is selected when using "*" pattern
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=210691"
 */
public void testBug210691() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test.java",
		"package pack;\n" +
		"public class Test {}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/Ref.java",
		"package test;\n" +
		"import pack.Test;\n" +
		"public class Ref {\n" +
		"	Test test;\n" +
		"}\n"
	);
	this.resultCollector.showSelection();
	search("*", TYPE, REFERENCES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/test/Ref.java [import §|pack.Test|§;] EXACT_MATCH\n" +
		"src/test/Ref.java test.Ref.test [	§|Test|§ test;] EXACT_MATCH"
	);
}

/**
 * @bug 211366: [search] does not return references to types in binary classes
 * @test Ensure that annotations references are found in a class file without source
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=211366"
 */
public void testBug211366() throws CoreException {
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b211366.jar", false);
	try {
		IType type = getClassFile("JavaSearchBugs", "lib/b211366.jar", "test", "Bug.class").getType();
		this.resultCollector.showMatchKind();
		search(type, REFERENCES);
		assertSearchResults(
			"TypeReferenceMatch: lib/b211366.jar pack.Test [No source] EXACT_MATCH\n" +
			"TypeReferenceMatch: lib/b211366.jar pack.TestInner$Member [No source] EXACT_MATCH\n" +
			"TypeReferenceMatch: lib/b211366.jar void pack.TestMembers.method(java.lang.Object, java.lang.String) [No source] EXACT_MATCH\n" +
			"TypeReferenceMatch: lib/b211366.jar pack.TestMembers.field [No source] EXACT_MATCH"
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/b211366.jar"));
	}
}
public void testBug211366_OrPattern() throws CoreException {
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b211366.jar", false);
	try {
		IType type = getClassFile("JavaSearchBugs", "lib/b211366.jar", "test", "Bug.class").getType();
		SearchPattern rightPattern = SearchPattern.createPattern(type, REFERENCES);
		SearchPattern leftPattern = SearchPattern.createPattern(type, DECLARATIONS);
		SearchPattern pattern = SearchPattern.createOrPattern(leftPattern, rightPattern);
		this.resultCollector.showMatchKind();
		new SearchEngine(this.workingCopies).search(
			pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchScope(),
			this.resultCollector,
			null
		);
		assertSearchResults(
			"TypeReferenceMatch: lib/b211366.jar pack.Test [No source] EXACT_MATCH\n" +
			"TypeReferenceMatch: lib/b211366.jar pack.TestInner$Member [No source] EXACT_MATCH\n" +
			"TypeReferenceMatch: lib/b211366.jar void pack.TestMembers.method(java.lang.Object, java.lang.String) [No source] EXACT_MATCH\n" +
			"TypeReferenceMatch: lib/b211366.jar pack.TestMembers.field [No source] EXACT_MATCH\n" +
			"TypeDeclarationMatch: lib/b211366.jar test.Bug [No source] EXACT_MATCH"
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/b211366.jar"));
	}
}
public void testBug211366_ComplexOrPattern() throws CoreException {
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b211366.jar", false);
	try {
		IType bType = getClassFile("JavaSearchBugs", "lib/b211366.jar", "test", "Bug.class").getType();
		SearchPattern leftPattern = SearchPattern.createOrPattern(
			SearchPattern.createPattern("field", FIELD, DECLARATIONS, SearchPattern.R_CASE_SENSITIVE),
			SearchPattern.createPattern(bType, REFERENCES));
		SearchPattern rightPattern = SearchPattern.createOrPattern(
			SearchPattern.createPattern("Member", TYPE, DECLARATIONS, SearchPattern.R_EXACT_MATCH),
			SearchPattern.createOrPattern(
				SearchPattern.createPattern("method", METHOD, DECLARATIONS, SearchPattern.R_EXACT_MATCH),
				SearchPattern.createPattern("Bug", TYPE, REFERENCES, SearchPattern.R_EXACT_MATCH)));
		IPackageFragmentRoot root = JAVA_PROJECT.getPackageFragmentRoot("/JavaSearchBugs/lib/b211366.jar");
		this.resultCollector.sorted = true;
		this.resultCollector.showMatchKind();
		new SearchEngine(this.workingCopies).search(
			SearchPattern.createOrPattern(leftPattern, rightPattern),
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			SearchEngine.createJavaSearchScope(new IJavaElement[] { root }),
			this.resultCollector,
			null
		);
		assertSearchResults(
			"FieldDeclarationMatch: lib/b211366.jar pack.TestMembers.field [No source] EXACT_MATCH\n" +
			"MethodDeclarationMatch: lib/b211366.jar void pack.TestMembers.method(java.lang.Object, java.lang.String) [No source] EXACT_MATCH\n" +
			"TypeDeclarationMatch: lib/b211366.jar pack.TestInner$Member [No source] EXACT_MATCH\n" +
			"TypeReferenceMatch: lib/b211366.jar pack.Test [No source] EXACT_MATCH\n" +
			"TypeReferenceMatch: lib/b211366.jar pack.Test [No source] EXACT_MATCH\n" +
			"TypeReferenceMatch: lib/b211366.jar pack.TestInner$Member [No source] EXACT_MATCH\n" +
			"TypeReferenceMatch: lib/b211366.jar pack.TestInner$Member [No source] EXACT_MATCH\n" +
			"TypeReferenceMatch: lib/b211366.jar pack.TestMembers.field [No source] EXACT_MATCH\n" +
			"TypeReferenceMatch: lib/b211366.jar pack.TestMembers.field [No source] EXACT_MATCH\n" +
			"TypeReferenceMatch: lib/b211366.jar void pack.TestMembers.method(java.lang.Object, java.lang.String) [No source] EXACT_MATCH\n" +
			"TypeReferenceMatch: lib/b211366.jar void pack.TestMembers.method(java.lang.Object, java.lang.String) [No source] EXACT_MATCH"
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/b211366.jar"));
	}
}

/**
 * @bug 211857: [search] Standard annotations references not found on binary fields and methods when no source is attached
 * @test Ensure that annotations references on fields and methods are found in a class file without source
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=211857"
 */
public void testBug211857() throws CoreException {
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b211857.jar", false);
	try {
		IType type = getClassFile("JavaSearchBugs", getExternalJCLPathString("1.5"), "java.lang", "Deprecated.class").getType();
		search(type, REFERENCES);
		assertSearchResults(
			"lib/b211857.jar pack.Test [No source] EXACT_MATCH\n" +
			"lib/b211857.jar void pack.TestMembers.method(java.lang.Object, java.lang.String) [No source] EXACT_MATCH\n" +
			"lib/b211857.jar pack.TestMembers.field [No source] EXACT_MATCH"
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/b211857.jar"));
	}
}

/**
 * @bug 211872: [search] References to annotations not found in class file without source
 * @test Ensure that annotations references are found in the specific class file without source
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=211872"
 */
public void testBug211872_ns() throws CoreException, IOException {
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b211872_ns.jar", false);
	try {
		IType type = getClassFile("JavaSearchBugs", "lib/b211872_ns.jar", "test", "Bug.class").getType();
		search(type, REFERENCES);
		assertSearchResults(
			"lib/b211872_ns.jar pack.Test [No source] EXACT_MATCH\n" +
			"lib/b211872_ns.jar pack.TestMembers$Member [No source] EXACT_MATCH\n" +
			"lib/b211872_ns.jar void pack.TestMembers.method(java.lang.Object, java.lang.String) [No source] EXACT_MATCH\n" +
			"lib/b211872_ns.jar pack.TestMembers.field [No source] EXACT_MATCH"
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/b211872_ns.jar"));
	}
}
public void testBug211872_ws() throws CoreException, IOException {
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b211872_ws.jar", false);
	try {
		IType type = getClassFile("JavaSearchBugs", "lib/b211872_ws.jar", "test", "Bug.class").getType();
		search(type, REFERENCES);
		assertSearchResults(
			"lib/b211872_ws.jar pack.Test EXACT_MATCH\n" +
			"lib/b211872_ws.jar pack.Test EXACT_MATCH\n" +
			"lib/b211872_ws.jar pack.TestMembers.field EXACT_MATCH\n" +
			"lib/b211872_ws.jar void pack.TestMembers.method(java.lang.Object, java.lang.String) EXACT_MATCH\n" +
			"lib/b211872_ws.jar pack.TestMembers$Member EXACT_MATCH"
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/b211872_ws.jar"));
	}
}

/**
 * @bug 181981: [model] Linked Source Folders with Parallel package structure do not work with occurrences
 * @test Ensure that source folder nesting doesn't cause non existing elements to be returned
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=181981"
 */
public void testBug181981() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.4");
		createFolder("/P/p1");
		createFile(
			"/P/p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"  public void foo() {}\n" +
			"}"
		);
		createFile(
			"/P/p1/Y.java",
			"package p1;\n" +
			"public class Y {\n" +
			"  public void bar(X x) {\n" +
			"    x.foo();\n" +
			"  }\n" +
			"}"
		);
		createFolder("/P/src/p2");
		createFile(
			"/P/src/p2/Z.java",
			"package p2;\n" +
			"public class Z {\n" +
			"  public void bar(p1.X x) {\n" +
			"    x.foo();\n" +
			"  }\n" +
			"}"
		);
		IMethod method = getCompilationUnit("/P/p1/X.java").getType("X").getMethod("foo", new String[0]);
		search(method, REFERENCES, EXACT_RULE, SearchEngine.createJavaSearchScope(new IJavaProject[] {project}), this.resultCollector);
		assertSearchResults(
			"p1/Y.java void p1.Y.bar(X) [foo()] EXACT_MATCH\n" +
			"src/p2/Z.java void p2.Z.bar(p1.X) [foo()] EXACT_MATCH"
		);
	}
	finally {
		deleteProject("P");
	}
}

/**
 * @bug 216875: [search] Field- and LocalVariableReferenceMatch confuse read/write for field access on LHS
 * @test Ensure that read access is found inside qualified name reference
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=216875"
 */
public void testBug216875() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"public class Test {\n" +
		"    int fField;\n" +
		"    void m() {\n" +
		"        Test test; // refs to test\n" +
		"        test = new Test();\n" +
		"        test.fField = 42; // match for t is writeAccess, should be readAccess\n" +
		"    }\n" +
		"\n" +
		"}\n"
	);
	this.resultCollector.showSelection();
	ILocalVariable variable = selectLocalVariable(this.workingCopies[0], "test");
	search(variable, READ_ACCESSES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.m() [        §|test|§.fField = 42; // match for t is writeAccess, should be readAccess] EXACT_MATCH"
	);
}
public void testBug216875b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"public class Test {\n" +
		"    S fWrapped; // refs to fWrapped\n" +
		"    void wrapper() {\n" +
		"        fWrapped.fField = 12; // match for fWrapped is writeAccess\n" +
		"    }\n" +
		"}\n" +
		"class S {\n" +
		"	int fField;" +
		"}"
	);
	this.resultCollector.showSelection();
	IField field = this.workingCopies[0].getType("Test").getField("fWrapped");
	search(field, READ_ACCESSES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.wrapper() [        §|fWrapped|§.fField = 12; // match for fWrapped is writeAccess] EXACT_MATCH"
	);
}
public void testBug216875c() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test.java",
		"package pack;\n" +
		"public class Test {\n" +
		"	public int field;\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/X.java",
		"package test;\n" +
		"import pack.Test;\n" +
		"public class X {\n" +
		"	void foo(Test t1, Test t2) {\n" +
		"		t1 = t2;\n" +
		"		t1.field = t1.field;\n" +
		"		t2.field = t1.field;\n" +
		"	}\n" +
		"}\n"
	);
	this.resultCollector.showSelection();
	ILocalVariable variable = selectLocalVariable(this.workingCopies[1], "t1");
	search(variable, READ_ACCESSES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/test/X.java void test.X.foo(Test, Test) [		§|t1|§.field = t1.field;] EXACT_MATCH\n" +
		"src/test/X.java void test.X.foo(Test, Test) [		t1.field = §|t1|§.field;] EXACT_MATCH\n" +
		"src/test/X.java void test.X.foo(Test, Test) [		t2.field = §|t1|§.field;] EXACT_MATCH"
	);
}
public void testBug216875d() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test.java",
		"package pack;\n" +
		"public class Test {\n" +
		"	public int field;\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/X.java",
		"package test;\n" +
		"import pack.Test;\n" +
		"public class X {\n" +
		"	Test t1, t2;\n" +
		"	void foo() {\n" +
		"		t1 = t2;\n" +
		"		t1.field = t1.field;\n" +
		"		t2.field = t1.field;\n" +
		"	}\n" +
		"}\n"
	);
	this.resultCollector.showSelection();
	IField field = this.workingCopies[1].getType("X").getField("t1");
	search(field, READ_ACCESSES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/test/X.java void test.X.foo() [		§|t1|§.field = t1.field;] EXACT_MATCH\n" +
		"src/test/X.java void test.X.foo() [		t1.field = §|t1|§.field;] EXACT_MATCH\n" +
		"src/test/X.java void test.X.foo() [		t2.field = §|t1|§.field;] EXACT_MATCH"
	);
}
public void testBug216875e() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test.java",
		"package pack;\n" +
		"public class Test {\n" +
		"	public int field;\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/X.java",
		"package test;\n" +
		"import pack.Test;\n" +
		"public class X {\n" +
		"	void foo(Test t1, Test t2) {\n" +
		"		t1 = t2;\n" +
		"		t1.field = t1.field;\n" +
		"		t2.field = t1.field;\n" +
		"	}\n" +
		"}\n"
	);
	this.resultCollector.showSelection();
	ILocalVariable variable = selectLocalVariable(this.workingCopies[1], "t1");
	search(variable, WRITE_ACCESSES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/test/X.java void test.X.foo(Test, Test) [		§|t1|§ = t2;] EXACT_MATCH"
	);
}
public void testBug216875f() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test.java",
		"package pack;\n" +
		"public class Test {\n" +
		"	public int field;\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/X.java",
		"package test;\n" +
		"import pack.Test;\n" +
		"public class X {\n" +
		"	Test t1, t2;\n" +
		"	void foo() {\n" +
		"		t1 = t2;\n" +
		"		t1.field = t1.field;\n" +
		"		t2.field = t1.field;\n" +
		"	}\n" +
		"}\n"
	);
	this.resultCollector.showSelection();
	IField field = this.workingCopies[1].getType("X").getField("t1");
	search(field, WRITE_ACCESSES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/test/X.java void test.X.foo() [		§|t1|§ = t2;] EXACT_MATCH"
	);
}
public void testBug216875g() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test.java",
		"package pack;\n" +
		"public class Test {\n" +
		"	public int field;\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/X.java",
		"package test;\n" +
		"import pack.Test;\n" +
		"public class X {\n" +
		"	void foo(Test t1, Test t2) {\n" +
		"		t1 = t2;\n" +
		"		t1.field = t1.field;\n" +
		"		t2.field = t1.field;\n" +
		"	}\n" +
		"}\n"
	);
	this.resultCollector.showSelection();
	this.resultCollector.showAccess();
	this.resultCollector.showAccuracy(false);
	ILocalVariable variable = selectLocalVariable(this.workingCopies[1], "t1");
	search(variable, REFERENCES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/test/X.java void test.X.foo(Test, Test) [		§|t1|§ = t2;] WRITE ACCESS\n" +
		"src/test/X.java void test.X.foo(Test, Test) [		§|t1|§.field = t1.field;] READ ACCESS\n" +
		"src/test/X.java void test.X.foo(Test, Test) [		t1.field = §|t1|§.field;] READ ACCESS\n" +
		"src/test/X.java void test.X.foo(Test, Test) [		t2.field = §|t1|§.field;] READ ACCESS"
	);
}
public void testBug216875h() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test.java",
		"package pack;\n" +
		"public class Test {\n" +
		"	public int field;\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/X.java",
		"package test;\n" +
		"import pack.Test;\n" +
		"public class X {\n" +
		"	Test t1, t2;\n" +
		"	void foo() {\n" +
		"		t1 = t2;\n" +
		"		t1.field = t1.field;\n" +
		"		t2.field = t1.field;\n" +
		"	}\n" +
		"}\n"
	);
	this.resultCollector.showSelection();
	this.resultCollector.showAccess();
	this.resultCollector.showAccuracy(false);
	IField field = this.workingCopies[1].getType("X").getField("t1");
	search(field, REFERENCES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/test/X.java void test.X.foo() [		§|t1|§ = t2;] WRITE ACCESS\n" +
		"src/test/X.java void test.X.foo() [		§|t1|§.field = t1.field;] READ ACCESS\n" +
		"src/test/X.java void test.X.foo() [		t1.field = §|t1|§.field;] READ ACCESS\n" +
		"src/test/X.java void test.X.foo() [		t2.field = §|t1|§.field;] READ ACCESS"
	);
}

/**
 * @bug 218397: [search] Can't find references of generic local class.
 * @test Ensure that the generic local class reference is well found (no CCE)
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=218397"
 */
public void testBug218397() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Bug.java",
		"class Bug{\n" +
		"	{\n" +
		"		class Inner<Type extends Number> {\n" +
		"			Row field;//LINE 3\n" +
		"			class Row{}\n" +
		"		}\n" +
		"	}\n" +
		"}"
	);
	this.resultCollector.showSelection();
	IType type = selectType(this.workingCopies[0], "Row");
	search(type, REFERENCES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/Bug.java Bug.{}:Inner#1.field [			§|Row|§ field;//LINE 3] EXACT_MATCH"
	);
}

/**
 * @bug 221081: [search] Java Search should default to widest scope
 * @test Ensure that user can search to type/method/field references/declarations in only
 * 	one call to SearchEngine
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=221081"
 */
public void testBug221081() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"public class Test {\n" +
		"	Test test;\n" +
		"	void test(Test test) {\n" +
		"		if (test == this.test) {\n" +
		"			//\n" +
		"		}\n" +
		"	}\n" +
		"}\n"
	);
	this.resultCollector.showSelection();
	this.resultCollector.showRule();
	SearchPattern typePattern = SearchPattern.createPattern("test", TYPE, ALL_OCCURRENCES, SearchPattern.R_EXACT_MATCH);
	SearchPattern methPattern = SearchPattern.createPattern("test", METHOD, ALL_OCCURRENCES, SearchPattern.R_EXACT_MATCH);
	SearchPattern fieldPattern = SearchPattern.createPattern("test", FIELD, ALL_OCCURRENCES, SearchPattern.R_EXACT_MATCH);
	SearchPattern pattern = SearchPattern.createOrPattern(typePattern, methPattern);
	pattern = SearchPattern.createOrPattern(pattern, fieldPattern);
	new SearchEngine(this.workingCopies).search(
		pattern,
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		getJavaSearchWorkingCopiesScope(),
		this.resultCollector,
		null);
	assertSearchResults(
		"src/Test.java Test [public class §|Test|§ {] EXACT_MATCH\n" +
		"src/Test.java Test.test [	Test §|test|§;] EXACT_MATCH\n" +
		"src/Test.java Test.test [	§|Test|§ test;] EXACT_MATCH\n" +
		"src/Test.java void Test.test(Test) [	void §|test|§(Test test) {] EXACT_MATCH\n" +
		"src/Test.java void Test.test(Test) [	void test(§|Test|§ test) {] EXACT_MATCH\n" +
		"src/Test.java void Test.test(Test) [		if (test == this.§|test|§) {] EXACT_MATCH"
	);
}

/**
 * @bug 221110: [search] NPE at org.eclipse.jdt.internal.compiler.util.SimpleLookupTable.removeKey
 * @test Ensure that no NPE occurs while searching for reference to generic class
 * 	when referenced in a unbound wildcard parameterized type
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=221110"
 */
public void testBug221110() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
		"public class X<T> {\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/Y.java",
		"public class Y<T extends X<?>> {\n" +
		"}\n"
	);
	this.resultCollector.showSelection();
	this.resultCollector.showRule();
	IType type = this.workingCopies[0].getType("X");
	search(type, REFERENCES, SearchPattern.R_ERASURE_MATCH, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/Y.java Y [public class Y<T extends §|X|§<?>> {] ERASURE_MATCH"
	);
}
public void testBug221110b() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/I.java",
		"public interface I<T> {\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/X.java",
		"public class X<T> {\n" +
		"}\n"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/Z.java",
		"public class Z<T extends X<?> & I<?>> {\n" +
		"}\n"
	);
	this.resultCollector.showSelection();
	this.resultCollector.showRule();
	IType type = this.workingCopies[0].getType("I");
	search(type, REFERENCES, SearchPattern.R_ERASURE_MATCH, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/Z.java Z [public class Z<T extends X<?> & §|I|§<?>> {] ERASURE_MATCH"
	);
}

/**
 * @bug 221065: [search] Search still finds overridden method
 * @test Ensure that correct number of method references are found
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=221065"
 */
public void testBug221065() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"public class Test {\n" +
		"	abstract class A {\n" +
		"		void foo() {}\n" +
		"		void bar() {\n" +
		"			foo();\n" +
		"		}\n" +
		"	}\n" +
		"	class B extends A {\n" +
		"		void foo() {}\n" +
		"		void bar() {\n" +
		"			foo();\n" +
		"		}\n" +
		"	}\n" +
		"	class C extends B {\n" +
		"		void method() {\n" +
		"			foo();\n" +
		"		}\n" +
		"	}\n" +
		"}"
	);
	IMethod method = this.workingCopies[0].getType("Test").getType("A").getMethod("foo", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"src/Test.java void Test$A.bar() [foo()] EXACT_MATCH"
	);
}

/**
 * @bug 222284: [search] ZipException while searching if linked jar doesn't exist any longer
 * @test Ensure that no exception is raised while searching for a type of the missing jar file
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=222284"
 */
public void testBug222284() throws Exception {
	String jarName = "lib222284.jar";
	String jarPath = getExternalPath()+jarName;
	IFile jarFile = JAVA_PROJECT.getProject().getFile(jarName);
	try {
		// Create jar and add it to JavaSearchBugs project build path
		String[] pathsAndContents = new String[] {
			"pack/Ref.java",
			"package pack;\n" +
			"public class Ref {\n" +
			"}",
			};
		createJar(pathsAndContents, jarPath);
		jarFile.createLink(new Path(jarPath), IResource.NONE, null);
		addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib222284.jar", null);

		// Create file and wait for indexes
		createFile("/JavaSearchBugs/src/Test.java",
			"import pack.Ref;" +
			"public class Test {\n" +
			"	Ref ref;" +
			"}\n"
		);
		waitUntilIndexesReady();

		// Exit, delete jar and restart
		simulateExit();
		deleteExternalResource(jarName);
		simulateRestart();

		// Search for references to a class of deleted jar file, expect no result
		search("pack.Ref", TYPE, REFERENCES);
		assertSearchResults(
			"src/Test.java [Ref] POTENTIAL_MATCH\n" +
			"src/Test.java Test.ref [Ref] POTENTIAL_MATCH"
		);
	} finally {
		deleteResource(jarFile);
		removeClasspathEntry(JAVA_PROJECT, new Path(jarPath));
	}
}

/**
 * @bug 228464: Annotation.getMemberValuePairs() empty for single attribute with empty value
 * @test Ensure that annotation are correctly recovered
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=228464"
 */
public void testBug228464() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"public class Test {\n" +
		"    void m() {\n" +
		"        @TestAnnotation(name=) Test iii;\n" +
				"    }\n" +
		"\n" +
		"}\n"
	);
	this.resultCollector.showSelection();
	IType type = this.workingCopies[0].getType("Test");
	search(type, REFERENCES, getJavaSearchWorkingCopiesScope());

	IAnnotation[] annotations = new IAnnotation[0];
	if (this.resultCollector.match != null &&
			this.resultCollector.match instanceof ReferenceMatch) {
		IJavaElement element = ((ReferenceMatch)this.resultCollector.match).getLocalElement();
		if (element instanceof ILocalVariable) {
			annotations = ((ILocalVariable)element).getAnnotations();
		}
	}

	assertAnnotationsEqual(
		"@TestAnnotation(name=<null>)\n",
		annotations);
}

/**
 * @bug 228852: classes opened via Open Type not found
 * @test Ensure that types found in an internal jar exist when using a workspace scope
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=228852"
 */
public void testBug228852a() throws Exception {
	try {
		IJavaProject p = createJavaProject("P", new String[] {}, new String[] {"/P/lib228852.jar"}, "");
		createJar(new String[] {
			"p228852/X228852.java",
			"package p228852;\n" +
			"public class X228852 {\n" +
			"}"
		}, p.getProject().getLocation().append("lib228852.jar").toOSString());
		refresh(p);

		char[][] packagesList = new char[][] {
				"p228852".toCharArray()
		};
		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
			packagesList,
			null,
			SearchEngine.createWorkspaceScope(),
			collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
		assertTrue("p228852.X228852 should exist", ((IJavaElement) collector.matches.get(0)).exists());
	} finally {
		deleteProject("P");
	}
}

/**
 * @bug 228852: classes opened via Open Type not found
 * @test Ensure that types found in an internal jar exist when using a Java search scope
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=228852"
 */
public void testBug228852b() throws Exception {
	try {
		IJavaProject p = createJavaProject("P", new String[] {}, new String[] {"/P/lib228852.jar"}, "");
		createJar(new String[] {
			"p228852/X228852.java",
			"package p228852;\n" +
			"public class X228852 {\n" +
			"}"
		}, p.getProject().getLocation().append("lib228852.jar").toOSString());
		refresh(p);

		char[][] packagesList = new char[][] {
				"p228852".toCharArray()
		};
		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
			packagesList,
			null,
			SearchEngine.createJavaSearchScope(new IJavaElement[] {p}),
			collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
		assertTrue("p228852.X228852 should exist", ((IJavaElement) collector.matches.get(0)).exists());
	} finally {
		deleteProject("P");
	}
}

/**
 * @bug 231622: Some classes from Missing classes from Cntrl-Shift-T
 * @test Ensure that types added through a container are found using a workspace scope
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=231622"
 */
public void testBug231622() throws Exception {
	try {
		IJavaProject p = createJavaProject("P", new String[] {}, new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, "");
		createJar(new String[] {
			"p231622/X231622.java",
			"package p231622;\n" +
			"public class X231622 {\n" +
			"}"
		}, p.getProject().getLocation().append("lib231622.jar").toOSString());
		refresh(p);
		DefaultContainerInitializer initializer = new DefaultContainerInitializer(new String[] {"P", "/P/lib231622.jar"});
		ContainerInitializer.setInitializer(initializer);
		initializer.initialize(new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER"), p);

		char[][] packagesList = new char[][] {
				"p231622".toCharArray()
		};
		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
			packagesList,
			null,
			SearchEngine.createWorkspaceScope(),
			collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
		assertSearchResults(
			"X231622 (not open) [in X231622.class [in p231622 [in lib231622.jar [in P]]]]",
			collector);
	} finally {
		deleteProject("P");
	}
}

/**
 * @bug 236520: [search] AIOOBE in PatternLocator.updateMatch
 * @test Ensure that no exception occurs even when code has compiler errors
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=236520"
 */
public void testBug236520() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack1/I.java",
		"package pack1;\n" + 
		"public interface I<V> {\n" + 
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/pack2/X.java",
		"package pack2;\n" + 
		"public class X {\n" + 
		"    public I<A> foo() {}\n" + 
		"}\n"
	);
	this.resultCollector.showRule();
	IType type = this.workingCopies[0].getType("I");
	search(type, REFERENCES, ERASURE_RULE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/pack2/X.java I<A> pack2.X.foo() [I] ERASURE_MATCH"
	);
}

/**
 * @bug 250083: Search indexes are not correctly updated
 * @test Ensure that a library that is no longer referenced, modified, and referenced again is re-indexed
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=250083"
 */
public void testBug250083() throws Exception {
	String libPath = getExternalResourcePath("lib250083.jar");
	try {
		Util.createJar(
			new String[] {
				"p250083/Y250083.java",
				"package p250083;\n" +
				"public class Y250083 {}"
			},
			new HashMap(),
			libPath);
		createJavaProject("P", new String[0], new String[] {libPath}, "");
		waitUntilIndexesReady();
		deleteExternalFile(libPath);
		deleteProject("P");
		
		Util.createJar(
			new String[] {
				"p250083/X250083.java",
				"package p250083;\n" +
				"public class X250083 {}"
			},
			new HashMap(),
			libPath);
		createJavaProject("P", new String[0], new String[] {libPath}, "");
		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
			null,
			new char[][] {"X250083".toCharArray()},
			SearchEngine.createWorkspaceScope(),
			collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
		assertSearchResults(
			"X250083 (not open) [in X250083.class [in p250083 [in "+ getExternalPath() + "lib250083.jar]]]",
			collector);
	} finally {
		deleteExternalFile(libPath);
		deleteProject("P");
	}
}

/**
 * @bug 251827: [search] Search for type reference with wildcards finds references in package
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=251827"
 */
public void testBug251827a() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b251827/B251827.java",
		"package b251827;\n" +
		"public class B251827 {\n" +
		"	static int VAL=251827;\n" +
		"	static void foo() {};\n" +
		"}\n",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b251827/X.java",
		"package b251827;\n" +
		"import static b251827.B251827.VAL;\n" +
		"import static b251827.B251827.foo;\n" +
		"public class X {\n" +
		"	double val = VAL;\n" +
		"	void bar() { foo(); };\n" +
		"}\n",
		owner
	);
	this.resultCollector.showSelection();
	search("B251827*", TYPE, REFERENCES);
	assertSearchResults(
		"src/b251827/X.java [import static b251827.§|B251827|§.VAL;] EXACT_MATCH\n" + 
		"src/b251827/X.java [import static b251827.§|B251827|§.foo;] EXACT_MATCH"
	);
}
public void testBug251827b() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b251827/B251827.java",
		"package b251827;\n" +
		"public class B251827 {\n" +
		"	static int VAL=251827;\n" +
		"	static void foo() {};\n" +
		"}\n",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b251827/X.java",
		"package b251827;\n" +
		"import static b251827.B251827.*;\n" +
		"public class X {\n" +
		"	double val = VAL;\n" +
		"	void bar() { foo(); };\n" +
		"}\n",
		owner
	);
	this.resultCollector.showSelection();
	search("B251827*", TYPE, REFERENCES);
	assertSearchResults(
		"src/b251827/X.java [import static b251827.§|B251827|§.*;] EXACT_MATCH"
	);
}
public void testBug251827c() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b251827/B251827.java",
		"package b251827;\n" +
		"public class B251827 {\n" +
		"	int VAL=251827;\n" +
		"	void foo() {};\n" +
		"}\n",
		owner
	);
	this.resultCollector.showSelection();
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b251827/X.java",
		"package b251827;\n" +
		"import static b251827.*;\n" +
		"public class X {\n" +
		"	void bar(B251827 m) {;\n" +
		"		double val = m.VAL;\n" +
		"		m.foo();\n" +
		"	};\n" +
		"}\n",
		owner
	);
	search("B251827*", TYPE, REFERENCES);
	assertSearchResults(
		"src/b251827/X.java void b251827.X.bar(B251827) [	void bar(§|B251827|§ m) {;] EXACT_MATCH"
	);
}

/**
 * @bug 261722: [search] NPE after removing a project
 * @test Ensure that no NPE occurs when project is deleted before the end of the search request
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=261722"
 */
public void testBug261722() throws Exception {
	IPath projectPath = null;
	IJavaProject javaProject = null;
	try {
		// Create jar and project
		final int MAX = 10;
		final String[] pathsAndContents = new String[(1+MAX)*2];
		pathsAndContents[0] = "p261722/X.java";
		pathsAndContents[1] = "package p261722;\n" +
        	"public class X {}";
		for (int i=1; i<=MAX; i++) {
			String className = (i<10) ? "X0"+i : "X"+i;
			pathsAndContents[i*2] = "p261722/"+className+".java";
			pathsAndContents[i*2+1] = "package p261722;\n" +
	        	"public class "+className+" extends X {}";
        }
		javaProject = createJavaProject("P");
		projectPath = javaProject.getProject().getLocation();
		addLibrary(javaProject, "lib261722.jar", "lib261722.zip", pathsAndContents, "1.4");
		waitUntilIndexesReady();

		// Create a specific requestor slowed down to give the main thread
		// a chance to delete the project before the end of the search request
		class TestSearchRequestor extends SearchRequestor {
			int count = 0;
			public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
			    try {
	                Thread.sleep(100);
                } catch (InterruptedException e) {
	                // skip
                }
                this.count++;
		    }
		}

		// Search in separated thread
		final TestSearchRequestor requestor = new TestSearchRequestor();
		final SearchPattern pattern = SearchPattern.createPattern("X*", IJavaSearchConstants.DECLARATIONS, IJavaSearchConstants.TYPE, SearchPattern.R_PATTERN_MATCH);
		final IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { javaProject });
		Runnable search = new Runnable() {
			public void run() {
				try {
					new SearchEngine().search(pattern, new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope, requestor, null);
				} catch (CoreException e) {
					throw new RuntimeException(e);
				}
		    }
		};
		Thread thread = new Thread(search);
		thread.start();

		// Delete project in current thread after being sure that the search
		// request was started
		while (requestor.count < (MAX/3)) {
			Thread.sleep(10);
		}
		deleteProject(javaProject);
		
		// Wait until search thread is finished
		while (thread.isAlive()) {
			Thread.sleep(100);
		}
		
		// Verify search results
		assertEquals("Unexpected matches count", MAX+1, requestor.count);
	} finally {
		if (projectPath != null) {
			deleteFile("/P/lib261722.jar");
			deleteFile("/P/lib261722.zip");
		}
	}
}

/**
 * @bug 265065: [search] java.lang.ClassCastException while running "Refactor...Extract Class"
 * @test Ensure that no CCE occurs while using an OrPattern made of VariablePattern
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=265065"
 */
public void testBug265065() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/MyClass.java",
		"public class MyClass {\n" + 
		"    class MyPrivateClass {\n" + 
		"            public String type = TYPE_A;\n" + 
		"            public Object value = null;\n" + 
		"            public MyPrivateClass(String type, Object value) {\n" + 
		"                    this.type = type;\n" + 
		"                    this.value = value;\n" + 
		"            }\n" + 
		"    }\n" + 
		"\n" + 
		"    private static final String TYPE_A = \"A\";\n" + 
		"    private static final String TYPE_B = \"B\";\n" + 
		"    private static final String TYPE_C = \"C\";\n" + 
		"\n" + 
		"    void foo (Object value) {\n" + 
		"		MyPrivateClass mpc = new MyPrivateClass(TYPE_B, value);\n" + 
		"		if (mpc.value == null) {\n" + 
		"			mpc.type = TYPE_C;\n" + 
		"		}\n" + 
		"	}\n" + 
		"}\n"
	);
	this.resultCollector.showRule();
	this.resultCollector.showAccess();
	SearchPattern leftPattern = SearchPattern.createPattern("MyClass.MyPrivateClass.value Object", FIELD, ALL_OCCURRENCES, SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
	SearchPattern rightPattern = SearchPattern.createPattern("MyClass.MyPrivateClass.type String", FIELD, ALL_OCCURRENCES, SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);
	new SearchEngine(this.workingCopies).search(
		SearchPattern.createOrPattern(leftPattern, rightPattern),
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		getJavaSearchWorkingCopiesScope(),
		this.resultCollector,
		null);
	assertSearchResults(
		"src/MyClass.java void MyClass.foo(Object) [value] EXACT_MATCH READ ACCESS\n" + 
		"src/MyClass.java void MyClass.foo(Object) [type] EXACT_MATCH READ ACCESS\n" + 
		"src/MyClass.java MyClass$MyPrivateClass.type [type] EXACT_MATCH\n" + 
		"src/MyClass.java MyClass$MyPrivateClass.value [value] EXACT_MATCH\n" + 
		"src/MyClass.java MyClass$MyPrivateClass(String, Object) [type] EXACT_MATCH WRITE ACCESS\n" + 
		"src/MyClass.java MyClass$MyPrivateClass(String, Object) [value] EXACT_MATCH WRITE ACCESS"
	);
}
public void testBug265065b() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test.java",
		"package pack;\n" +
		"public class Test {\n" +
		"	public int field;\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/X.java",
		"package test;\n" +
		"import pack.Test;\n" +
		"public class X {\n" +
		"	void foo(Test t1, Test t2) {\n" +
		"		t1 = t2;\n" +
		"		t1.field = t1.field;\n" +
		"		t2.field = t1.field;\n" +
		"	}\n" +
		"}\n"
	);
	this.resultCollector.showAccess();
	ILocalVariable localVar1 = selectLocalVariable(this.workingCopies[1], "t1");
	SearchPattern leftPattern = createPattern(localVar1, IJavaSearchConstants.REFERENCES);
	ILocalVariable localVar2 = selectLocalVariable(this.workingCopies[1], "t2");
	SearchPattern rightPattern = createPattern(localVar2, IJavaSearchConstants.REFERENCES);
	new SearchEngine(this.workingCopies).search(
		SearchPattern.createOrPattern(leftPattern, rightPattern),
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		getJavaSearchWorkingCopiesScope(),
		this.resultCollector,
		null);
	assertSearchResults(
		"src/test/X.java void test.X.foo(Test, Test) [t1] EXACT_MATCH WRITE ACCESS\n" + 
		"src/test/X.java void test.X.foo(Test, Test) [t2] EXACT_MATCH READ ACCESS\n" + 
		"src/test/X.java void test.X.foo(Test, Test) [t1] EXACT_MATCH READ ACCESS\n" + 
		"src/test/X.java void test.X.foo(Test, Test) [t1] EXACT_MATCH READ ACCESS\n" + 
		"src/test/X.java void test.X.foo(Test, Test) [t2] EXACT_MATCH READ ACCESS\n" + 
		"src/test/X.java void test.X.foo(Test, Test) [t1] EXACT_MATCH READ ACCESS"
	);
}

/**
 * @bug 266582: [search] NPE finding references 
 * @test Ensure that no NPE occurs when searching for type references
 * 	in a project which has the same jar twice on its classpath
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=266582"
 */
public void testBug266582() throws Exception {
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b266582a.jar", false);
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b266582b.jar", false);
	try {
		createFile("/JavaSearchBugs/src/A.java",
			"import foo.JohnsonException;\n" + 
			"class A {\n" + 
			"	void foo() throws JohnsonException {}\n" + 
			"}"
		);
		IType type = getClassFile("JavaSearchBugs", "/JavaSearchBugs/lib/b266582a.jar", "foo", "JohnsonException.class").getType();
		search(type, REFERENCES);
		assertSearchResults(
			"src/A.java [foo.JohnsonException] EXACT_MATCH\n" + 
			"src/A.java void A.foo() [JohnsonException] EXACT_MATCH"
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/b266582a.jar"));
		removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/b266582b.jar"));
	}
}

/**
 * @bug 266837: SourceField.getConstant does not supply a value if type is fully qualified
 * @test Ensure that source field constant is not null when fully qualified type String is used
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=266837"
 */
public void testBug266837() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"public class Test {\n" + 
		"	private static final java.lang.String f266837 = \"myString\";\n" + 
		"}\n"
	);
	SearchRequestor requestor = new SearchRequestor() {
		public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
	        IField sourceField = (IField) searchMatch.getElement();
	        assertEquals("Unexpected source field constant!", "\"myString\"", sourceField.getConstant());
        }
	};
	search("f266837", FIELD, DECLARATIONS, requestor);
}

/**
 * @bug 286379: [search] Problem while searching class
 * @test Ensure that no IAE occurs when there is a change in JavaLikeNames.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=286379"
 */
public void testBug286379a() throws CoreException {
	IContentType javaContentType = Platform.getContentTypeManager().getContentType(JavaCore.JAVA_SOURCE_CONTENT_TYPE);
	ICompilationUnit cu = null;
	try {
		assertNotNull("We should have got a Java Source content type!", javaContentType);
		javaContentType.addFileSpec("torem", IContentType.FILE_EXTENSION_SPEC);
		createJavaProject("P");
		createFolder("/P/p");			
		createFile(
			"/P/p/Xtorem.torem",
			"package p;\n" +
			"public class Xtorem {\n" +
			"}"
		);
		waitUntilIndexesReady();
		cu = getCompilationUnit("/P/p/Xtorem.torem");
		cu.becomeWorkingCopy(null);
		IType type = cu.getType("Xtorem");
		
		// Ensure that the Xtorem class is really found
		TestCollector collector = new TestCollector();
		new SearchEngine().search(
				SearchPattern.createPattern(type, IJavaSearchConstants.DECLARATIONS),
				new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
				SearchEngine.createWorkspaceScope(),
				collector,
				null
			);
		assertSearchResults("p/Xtorem.torem p.Xtorem", collector);
		
		// Ensure that removal of the content type doesn't cause any further exception
		// during the search and also ensure that the search doesn't return any result
		javaContentType.removeFileSpec("torem", IContentType.FILE_EXTENSION_SPEC);		
		collector = new TestCollector();
		new SearchEngine().search(
				SearchPattern.createPattern(type, IJavaSearchConstants.DECLARATIONS),
				new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
				SearchEngine.createWorkspaceScope(),
				collector,
				null
			);
		assertSearchResults("No results expected", "", collector);
	} finally {
		if (cu != null)
			cu.discardWorkingCopy();
		if (javaContentType != null)
			javaContentType.removeFileSpec("torem", IContentType.FILE_EXTENSION_SPEC);
		deleteProject("P");
	}
}

/**
 * This is similar to testBug286379a, except that it ensures that IAE doesn't occur 
 * at a different place
 */
public void testBug286379b() throws CoreException {
	IContentType javaContentType = Platform.getContentTypeManager().getContentType(JavaCore.JAVA_SOURCE_CONTENT_TYPE);
	try {
		assertNotNull("We should have got a Java Source a content type!", javaContentType);
		javaContentType.addFileSpec("torem", IContentType.FILE_EXTENSION_SPEC);
		createJavaProject("P");
		createFolder("/P/p");			
		createFile(
			"/P/p/Xtorem.torem",
			"package p;\n" +
			"public class Xtorem {\n" +
			"}"
		);	
		
		// Ensure that the class Xtorem is really found
		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
				null,
				new char[][] {"Xtorem".toCharArray()},
				SearchEngine.createWorkspaceScope(),
				collector,
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
		assertSearchResults("Xtorem (not open) [in Xtorem.torem [in p [in <project root> [in P]]]]", collector);
		
		// Ensure that removal of the content type doesn't cause any further exception
		// during the search and also ensure that the search doesn't return any result
		javaContentType.removeFileSpec("torem", IContentType.FILE_EXTENSION_SPEC);
		collector = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
				null,
				new char[][] {"Xtorem".toCharArray()},
				SearchEngine.createWorkspaceScope(),
				collector,
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
		assertSearchResults("No results expected", "", collector);
	} finally {
		if (javaContentType != null)
			javaContentType.removeFileSpec("torem", IContentType.FILE_EXTENSION_SPEC);
		deleteProject("P");
	}
}

/**
 * If any javaLikeNames are added, this ensures that such files can get searched 
 * at least on the restart of the workspace. 
 * If any javaLikeNames are deleted, this ensures that the index file is regenerated.
 */
public void testBug286379c() throws CoreException {
	class TestResourceChangeListener implements IResourceChangeListener {
		boolean valid = false;
		public void resourceChanged(IResourceChangeEvent event) {
			this.valid = validate(event.getDelta());
		}
		/*
		 * Ensure that the listener receives a delta concerning the resource
		 * with the new extension...
		 */
		private boolean validate(IResourceDelta delta) {
	        IResourceDelta[] children = delta.getAffectedChildren();
	        int length = children.length;
	        if (length == 0) {
	        	IResource resource = delta.getResource();
	        	if (resource.getType() == IResource.FILE &&
	        		resource.getName().equals("Xtorem.torem")) {
	        		return true;
	        	}
	        } else {
		        for (int i=0; i<length; i++) {
		        	if (validate(children[i])) return true;
		        }
	        }
	        return false;
        }
	}

	IContentType javaContentType = Platform.getContentTypeManager().getContentType(JavaCore.JAVA_SOURCE_CONTENT_TYPE);
	TestResourceChangeListener changeListener = new TestResourceChangeListener();
	try {
		// Create resource
		createJavaProject("P");
		createFolder("/P/p");			
		createFile(
			"/P/p/Xtorem.torem",
			"package p;\n" +
			"public class Xtorem {\n" +
			"}"
		);
		
		// Wait to be sure that indexes are ready after the resource creation
		waitUntilIndexesReady();

		// Add the resource listener
		getWorkspace().addResourceChangeListener(changeListener, IResourceChangeEvent.POST_CHANGE);

		// Change the file extension
		assertNotNull("We should have got a Java Source a content type!", javaContentType);
		javaContentType.addFileSpec("torem", IContentType.FILE_EXTENSION_SPEC);
		
		// Wait for all the resource event before continuing
		// Note that if we are not waiting for this event occurring then the search may
		// fail as we don't get any specific event from the platform to refresh the indexes.
		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=118619
		int counter = 0;
		while (!changeListener.valid) {
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException ie) {
				// skip
			}
			assertTrue("We should have got a resource event within a 10s delay!", counter++ < 100);
		}

		// Search for the new type with new extension
		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
				null,
				new char[][] {"Xtorem".toCharArray()},
				SearchEngine.createWorkspaceScope(),
				collector,
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
		assertSearchResults("Unexpected search results!",
			"Xtorem (not open) [in Xtorem.torem [in p [in <project root> [in P]]]]",
			collector);
		
		// Delete the file specification
		javaContentType.removeFileSpec("torem", IContentType.FILE_EXTENSION_SPEC);
		
		// Restarting should update the index file to remove the references of any .torem files
		simulateExit();
		simulateRestart();		
		waitUntilIndexesReady();

		// Search for the new type with new extension
		collector = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
				null,
				new char[][] {"Xtorem".toCharArray()},
				SearchEngine.createWorkspaceScope(),
				collector,
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
		assertSearchResults("No search results expected", "", collector);
	} finally {
		getWorkspace().removeResourceChangeListener(changeListener);
		if (javaContentType != null)
			javaContentType.removeFileSpec("torem", IContentType.FILE_EXTENSION_SPEC);
		deleteProject("P");
	}
}
}