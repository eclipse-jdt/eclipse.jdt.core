/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.TypeDeclarationPattern;

/**
 * Non-regression tests for bugs fixed in Java Search engine.
 */
public class JavaSearchBugsTests extends AbstractJavaSearchTests implements IJavaSearchConstants {
	private final static int UI_DECLARATIONS = DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE;
	
public JavaSearchBugsTests(String name) {
	super(name);
	this.endChar = "";
}
public static Test suite() {
	return buildModelTestSuite(JavaSearchBugsTests.class);
}
static {
//	org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
//	org.eclipse.jdt.internal.codeassist.SelectionEngine.DEBUG = true;
}

class TestCollector extends JavaSearchResultCollector {
	public List matches = new ArrayList();
	public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
		super.acceptSearchMatch(searchMatch);
		matches.add(searchMatch);
	}
}
class TypeReferencesCollector extends JavaSearchResultCollector {

	protected IJavaElement getElement(SearchMatch searchMatch) {
		IJavaElement element = super.getElement(searchMatch);
		IJavaElement localElement = null;
		TypeReferenceMatch typeRefMatch = (TypeReferenceMatch) match;
		localElement = typeRefMatch.getLocalElement();
		if (localElement != null) {
			return localElement;
		}
		return element;
	}
	protected void writeLine() throws CoreException {
		super.writeLine();
		TypeReferenceMatch typeRefMatch = (TypeReferenceMatch) this.match;
		IJavaElement[] others = typeRefMatch.getOtherElements();
		int length = others==null ? 0 : others.length;
		if (length > 0) {
			line.append("+[");
			for (int i=0; i<length; i++) {
				IJavaElement other = others[i];
				if (i>0) line.append(',');
				line.append(other.getElementName());
			}
			line.append(']');
		}
	}
}

IJavaSearchScope getJavaSearchScopeBugs() {
	return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearchBugs")});
}
IJavaSearchScope getJavaSearchScopeBugs(String packageName, boolean addSubpackages) throws JavaModelException {
	if (packageName == null) return getJavaSearchScopeBugs();
	return getJavaSearchPackageScope("JavaSearchBugs", packageName, addSubpackages);
}
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	if (this.wcOwner == null) {
		this.wcOwner = new WorkingCopyOwner() {};
	}
	return getWorkingCopy(path, source, this.wcOwner, null/*don't compute problems*/);
}
protected void search(IJavaElement element, int limitTo) throws CoreException {
	search(element, limitTo, EXACT_RULE, getJavaSearchScopeBugs(), resultCollector);
}
protected void search(IJavaElement element, int limitTo, int matchRule) throws CoreException {
	search(element, limitTo, matchRule, getJavaSearchScopeBugs(), resultCollector);
}
protected void search(String patternString, int searchFor, int limitTo) throws CoreException {
	search(patternString, searchFor, limitTo, EXACT_RULE, getJavaSearchScopeBugs(), resultCollector);
}
protected void search(String patternString, int searchFor, int limitTo, int matchRule) throws CoreException {
	search(patternString, searchFor, limitTo, matchRule, getJavaSearchScopeBugs(), resultCollector);
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
}
public void tearDownSuite() throws Exception {
	deleteProject("JavaSearchBugs");
	super.tearDownSuite();
}
protected void setUp () throws Exception {
	super.setUp();
	this.resultCollector = new TestCollector();
	resultCollector.showInsideDoc = false;
	resultCollector.showAccuracy = true;
}

/**
 * Bug 41018: Method reference not found
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=41018"
 */
public void testBug41018() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b41018/A.java",
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
	IType type = workingCopies[0].getType("A");
	IMethod method = type.getMethod("methodA", new String[] { "QClassB.InnerInterface;" });
	search(method, REFERENCES);
	assertSearchResults(
		"src/b41018/A.java void b41018.A.anotherMethod() [methodA(null)] EXACT_MATCH"
	);
}

/**
 * Bug 70827: [Search] wrong reference match to private method of supertype
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=70827"
 */
public void testBug70827() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b70827/A.java",
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
	IType type = workingCopies[0].getType("A");
	IMethod method = type.getMethod("privateMethod", new String[] {});
	search(method, REFERENCES);
	assertSearchResults(
		""
	);
}

/**
 * Bug 71279: [Search] NPE in TypeReferenceLocator when moving CU with unresolved type reference
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=71279"
 */
public void testBug71279() throws CoreException {
	JavaSearchResultCollector result = new JavaSearchResultCollector() {
	    public void beginReporting() {
	        results.append("Starting search...");
        }
	    public void endReporting() {
	        results.append("\nDone searching.");
        }
	};
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b71279/AA.java",
		"package b71279;\n" + 
		"public class AA {\n" + 
		"	Unknown ref;\n" + 
		"}\n"
		);
	new SearchEngine(workingCopies).searchDeclarationsOfReferencedTypes(workingCopies[0], result, null);
	assertSearchResults(
		"Starting search...\n" + 
		"Done searching.",
		result);
}

/**
 * Bug 72866: [search] references to endVisit(MethodInvocation) reports refs to endVisit(SuperMethodInvocation)
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=72866"
 */
public void testBug72866() throws CoreException {
	workingCopies = new ICompilationUnit[4];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b72866/A.java",
		"package b72866;\n" + 
		"public abstract class A {\n" + 
		"	public abstract void foo(V v);\n" + 
		"}\n",
		owner,
		true
		);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b72866/SX.java",
		"package b72866;\n" + 
		"public class SX extends A {\n" + 
		"	public void foo(V v) {\n" + 
		"	    v.bar(this);\n" + 
		"	}\n" + 
		"}\n"	,
		owner,
		true);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b72866/V.java",
		"package b72866;\n" + 
		"public class V {\n" + 
		"	void bar(A a) {}\n" + 
		"	void bar(X x) {}\n" + 
		"	void bar(SX s) {}\n" + 
		"}\n"	,
		owner,
		true);
	workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b72866/X.java",
		"package b72866;\n" + 
		"public class X extends A {\n" + 
		"	public void foo(V v) {\n" + 
		"	    v.bar(this);\n" + 
		"	}\n" + 
		"}\n"	,
		owner,
		true	);
	IType type = workingCopies[2].getType("V");
	IMethod method = type.getMethod("bar", new String[] {"QX;"});
	search(method, REFERENCES);
	assertSearchResults(
		"src/b72866/X.java void b72866.X.foo(V) [bar(this)] EXACT_MATCH"
	);
}

/**
 * Bug 73112: [Search] SearchEngine doesn't find all fields multiple field declarations
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=73112"
 */
public void testBug73112a() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73112/A.java",
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
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = super.getWorkingCopy("/JavaSearchBugs/src/b73112/B.java",
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
 * Bug 73336: [1.5][search] Search Engine does not find type references of actual generic type parameters
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=73336"
 */
public void testBug73336() throws CoreException {
	workingCopies = new ICompilationUnit[6];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73336/A.java",
		"package b73336;\n" + 
		"public class A {}\n",
		owner,
		true);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b73336/AA.java",
		"package b73336;\n" + 
		"public class AA extends A {}\n",
		owner,
		true);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b73336/B.java",
		"package b73336;\n" + 
		"public class B extends X<A, A> {\n" + 
		"	<T> void foo(T t) {}\n" + 
		"}\n",
		owner,
		true);
	workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b73336/C.java",
		"package b73336;\n" + 
		"public class C implements I<A> {\n" + 
		"	public void foo() {\n" + 
		"		B b = new B();\n" + 
		"		b.<A>foo(new A());\n" + 
		"	}\n" + 
		"}\n",
		owner,
		true	);
	workingCopies[4] = getWorkingCopy("/JavaSearchBugs/src/b73336/I.java",
		"package b73336;\n" + 
		"public interface I<T>  {\n" + 
		"	public void foo();\n" + 
		"}\n",
		owner,
		true	);
	workingCopies[5] = getWorkingCopy("/JavaSearchBugs/src/b73336/X.java",
		"package b73336;\n" + 
		"public class X<T, U> {\n" + 
		"	<V> void foo(V v) {}\n" + 
		"	class Member<T> {\n" + 
		"		void foo() {}\n" + 
		"	}\n" + 
		"}\n",
		owner,
		true	);
	// search for first and second method should both return 2 inaccurate matches
	IType type = workingCopies[0].getType("A");
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
	workingCopies = new ICompilationUnit[4];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73336b/A.java",
		"package b73336b;\n" + 
		"public class A {}\n",
		owner,
		true);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b73336b/B.java",
		"package b73336b;\n" + 
		"public class B extends X<A, A> {\n" + 
		"}\n",
		owner,
		true);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b73336b/C.java",
		"package b73336b;\n" + 
		"public class C extends X<A, A>.Member<A> {\n" + 
		"	public C() {\n" + 
		"		new X<A, A>().super();\n" + 
		"	}\n" + 
		"}\n",
		owner,
		true);
	workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b73336b/X.java",
		"package b73336b;\n" + 
		"public class X<T, U> {\n" + 
		"	<V> void foo(V v) {}\n" + 
		"	class Member<T> {\n" + 
		"		void foo() {}\n" + 
		"	}\n" + 
		"}\n",
		owner,
		true	);
	// search for first and second method should both return 2 inaccurate matches
	IType type = workingCopies[0].getType("A");
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
	workingCopies = new ICompilationUnit[4];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73336c/A.java",
		"package b73336c;\n" + 
		"public class A {}\n",
		owner,
		true);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b73336c/B.java",
		"package b73336c;\n" + 
		"public class B extends X<A, A> {\n" + 
		"}\n",
		owner,
		true);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b73336c/C.java",
		"package b73336c;\n" + 
		"public class C implements X<A, A>.Interface<A>  {\n" + 
		"	void bar() {}\n" + 
		"}\n",
		owner,
		true);
	workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b73336c/X.java",
		"package b73336c;\n" + 
		"public class X<T, U> {\n" + 
		"	interface Interface<V> {\n" + 
		"		void bar();\n" + 
		"	}\n" + 
		"}\n",
		owner,
		true	);
	// search for first and second method should both return 2 inaccurate matches
	IType type = workingCopies[0].getType("A");
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
 * Bug 73696: searching only works for IJavaSearchConstants.TYPE, but not CLASS or INTERFACE
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=73696"
 */
public void testBug73696() throws CoreException {
	workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73696/C.java",
		"package b73696;\n" + 
		"public class C implements  I {\n" + 
		"}",
		owner,
		true);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b73696/I.java",
		"package b73696;\n" + 
		"public interface I {}\n",
		owner,
		true);
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(workingCopies);
	
	// Interface declaration
	TypeDeclarationPattern pattern = new TypeDeclarationPattern(
		null,
		null,
		null,
		IIndexConstants.INTERFACE_SUFFIX,
		SearchPattern.R_PATTERN_MATCH
	);
	new SearchEngine(new ICompilationUnit[] {workingCopies[1]}).search(
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
	new SearchEngine(new ICompilationUnit[] {workingCopies[0]}).search(
		pattern,
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		scope,
		resultCollector,
		null);
	assertSearchResults(
		"src/b73696/I.java b73696.I [I] EXACT_MATCH\n" + 
		"src/b73696/C.java b73696.C [C] EXACT_MATCH"
	);
}

/**
 * Bug 74776: [Search] Wrong search results for almost identical method
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=74776"
 */
public void testBug74776() throws CoreException {
	workingCopies = new ICompilationUnit[3];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b74776/A.java",
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
		owner,
		true);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b74776/IRegion.java",
		"package b74776;\n" + 
		"public interface IRegion {\n" + 
		"}\n",
		owner,
		true);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b74776/Region.java",
		"package b74776;\n" + 
		"public class Region implements IRegion {\n" + 
		"\n" + 
		"}\n",
		owner,
		true);
	// search method references
	IType type = workingCopies[0].getType("A");
	IMethod method = type.getMethod("foo", new String[] { "QRegion;" });
	search(method, REFERENCES);
	assertSearchResults("");
}

/**
 * Bug 75816: [search] correct results are missing in java search
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
 * Bug 77093: [search] No references found to method with member type argument
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=77093"
 */
private void setUpBug77093() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b77093/X.java",
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
	IType type = workingCopies[0].getType("X");
	IMethod method = type.getMethod("X", new String[] {"[[QZ;"});
	// Search for constructor declarations and references
	search(method, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b77093/X.java b77093.X() [this(new Z[10][])] EXACT_MATCH\n"+
		"src/b77093/X.java b77093.X(Z[][]) [X] EXACT_MATCH"
	);
}
public void testBug77093field() throws CoreException {
	setUpBug77093();
	IType type = workingCopies[0].getType("X");
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
	IType type = workingCopies[0].getType("X");
	IMethod method = type.getMethod("foo", new String[] {"[QZ;"});
	search(method, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b77093/X.java void b77093.X.foo(Z[]) [foo] EXACT_MATCH\n" +
		"src/b77093/X.java void b77093.X.bar() [foo(z_arrays[i])] EXACT_MATCH"
	);
}

/**
 * Bug 77388: [compiler] Reference to constructor includes space after closing parenthesis
 */
public void testBug77388() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b77388/Test.java",
		"package b77388;\n" + 
		"class Test {\n" + 
		"	Test(int a, int b) {	}\n" + 
		"	void take(Test mc) { }\n" + 
		"	void run() {\n" + 
		"		take( new Test(1, 2) ); // space in \") )\" is in match\n" + 
		"	}\n" + 
		"}");
	IType type = workingCopies[0].getType("Test");
	IMethod method = type.getMethod("Test", new String[] {"I", "I"});
	// Search for constructor references
	search(method, REFERENCES);
	assertSearchResults(
		"src/b77388/Test.java void b77388.Test.run() [new Test(1, 2)] EXACT_MATCH"
	);
}

/**
 * Bug 78082: [1.5][search] FieldReferenceMatch in static import should not include qualifier
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=78082"
 */
public void testBug78082() throws CoreException {
	workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b78082/M.java",
		"package b78082;\n" + 
		"public class M {\n" + 
		"	static int VAL=78082;\n" + 
		"}\n",
		owner,
		true);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b78082/XY.java",
		"package b78082;\n" + 
		"import static b78082.M.VAL;\n" + 
		"public class XY {\n" + 
		"	double val = VAL;\n" + 
		"	double val2= b78082.M.VAL;\n" + 
		"}\n",
		owner,
		true);
	// search field references
	IType type = workingCopies[0].getType("M");
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
 * Bug 79267: [search] Refactoring of static generic member fails partially
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=79267"
 */
public void testBug79267() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79267/Test.java",
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
	IType type = workingCopies[0].getType("Test");
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
 * Bug 79378: [search] IOOBE when inlining a method
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=79378"
 */
public void testBug79378() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79378/A.java",
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
	IMethod[] methods = workingCopies[0].getType("Test").getMethods();
	assertEquals("Invalid number of methods", 3, methods.length);
	search(methods[0], REFERENCES);
	assertSearchResults(
		"src/b79378/A.java void b79378.Test.call() [foo79378(s, exceptions)] POTENTIAL_MATCH"
	);
}
public void testBug79378b() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79378/A.java",
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
	IMethod[] methods = workingCopies[0].getType("Test").getMethods();
	assertEquals("Invalid number of methods", 3, methods.length);
	search(methods[1], REFERENCES);
	assertSearchResults("");
}

/**
 * Bug 79803: [1.5][search] Search for references to type A reports match for type variable A
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=79803"
 */
public void testBug79803() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79803/A.java",
		"package b79803;\n" + 
		"class A<A> {\n" + 
		"    A a;\n" + 
		"    b79803.A pa= new b79803.A();\n" + 
		"}\n"	
	);
	IType type = workingCopies[0].getType("A");
	search(type, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"src/b79803/A.java b79803.A.pa [b79803.A] EXACT_MATCH\n" + 
		"src/b79803/A.java b79803.A.pa [b79803.A] EXACT_MATCH"
	);
}
public void testBug79803string() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79803/A.java",
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
 * Bug 79860: [1.5][search] Search doesn't find type reference in type parameter bound
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=79860"
 */
public void testBug79860() throws CoreException {
	workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79860/X.java",
		"package b79860;\n" + 
		"public class X<T extends A> { }\n" + 
		"class A { }",
		owner,
		true);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b79860/Y.java",
		"package b79860;\n" + 
		"public class Y<T extends B&I1&I2&I3> { }\n" + 
		"class B { }\n" + 
		"interface I1 {}\n" + 
		"interface I2 {}\n" + 
		"interface I3 {}\n",
		owner,
		true);
	IType type = workingCopies[0].getType("A");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b79860/X.java b79860.X [A] EXACT_MATCH"
	);
}
public void testBug79860string() throws CoreException {
	workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79860/X.java",
		"package b79860;\n" + 
		"public class X<T extends A> { }\n" + 
		"class A { }",
		owner,
		true);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b79860/Y.java",
		"package b79860;\n" + 
		"public class Y<T extends B&I1&I2&I3> { }\n" + 
		"class B { }\n" + 
		"interface I1 {}\n" + 
		"interface I2 {}\n" + 
		"interface I3 {}\n",
		owner,
		true);
	search("I?", TYPE, REFERENCES);
	assertSearchResults(
		"src/b79860/Y.java b79860.Y [I1] EXACT_MATCH\n" + 
		"src/b79860/Y.java b79860.Y [I2] EXACT_MATCH\n" + 
		"src/b79860/Y.java b79860.Y [I3] EXACT_MATCH"
	);
}

/**
 * Bug 79990: [1.5][search] Search doesn't find type reference in type parameter bound
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=79990"
 */
private void setUpBug79990() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79990/Test.java",
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
	IMethod method = workingCopies[0].getType("Test").getMethods()[0];
	search(method, DECLARATIONS);
	assertSearchResults(
		"src/b79990/Test.java void b79990.Test.first(Exception) [first] EXACT_MATCH\n" + 
		"src/b79990/Test.java void b79990.Sub.first(Exception) [first] EXACT_MATCH"
	);
}
public void testBug79990b() throws CoreException {
	setUpBug79990();
	IMethod method = workingCopies[0].getType("Test").getMethods()[1];
	search(method, DECLARATIONS);
	assertSearchResults(
		"src/b79990/Test.java void b79990.Test.second(T) [second] EXACT_MATCH\n" + 
		"src/b79990/Test.java void b79990.Sub.second(Exception) [second] EXACT_MATCH"
	);
}
public void testBug79990c() throws CoreException {
	setUpBug79990();
	IMethod method = workingCopies[0].getType("Test").getMethods()[1];
	search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b79990/Test.java void b79990.Test.second(T) [second] EXACT_MATCH\n" + 
		"src/b79990/Test.java void b79990.Sub.second(Exception) [second] EXACT_MATCH"
	);
}
public void testBug79990d() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79990/Test.java",
		"package b79990;\n" + 
		"public class Test<T> {\n" + 
		"	void methodT(T t) {}\n" + 
		"}\n" + 
		"class Sub<X> extends Test<X> {\n" + 
		"	void methodT(X x) {} // overrides Super#methodT(T)\n" + 
		"}\n"
	);
	IMethod method = workingCopies[0].getType("Test").getMethods()[0];
	search(method, DECLARATIONS);
	assertSearchResults(
		"src/b79990/Test.java void b79990.Test.methodT(T) [methodT] EXACT_MATCH\n" + 
		"src/b79990/Test.java void b79990.Sub.methodT(X) [methodT] EXACT_MATCH"
	);
}

/**
 * Bug 80084: [1.5][search]Rename field fails on field based on parameterized type with member type parameter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=80084"
 */
public void testBug80084() throws CoreException, JavaModelException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80084/Test.java",
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
	IType type = workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QList<QException;>;" } );
	search(method, REFERENCES);
	assertSearchResults(
		"src/b80084/Test.java void b80084.Test.bar() [foo(le)] EXACT_MATCH"
	);
}

/**
 * Bug 80194: [1.5][search]Rename field fails on field based on parameterized type with member type parameter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=80194"
 */
private void setUpBug80194() throws CoreException, JavaModelException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80194/Test.java",
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
	IType type = workingCopies[0].getType("Test");
	IMethod method = type.getMethod("doSomething", new String[] { "QMap<QString;QObject;>;" } );
	search(method, REFERENCES);
	assertSearchResults(
		"src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(map)] EXACT_MATCH"
	);
}
public void testBug80194b() throws CoreException, JavaModelException {
	setUpBug80194();
	IType type = workingCopies[0].getType("Test");
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
 * Bug 80223: [search] Declaration search doesn't consider visibility to determine overriding methods
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=80223"
 */
public void testBug80223() throws CoreException {
	workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80223/a/A.java",
		"package b80223.a;\n" + 
		"public class A {\n" + 
		"    void m() {}\n" + 
		"}",
		owner,
		true);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b80223/b/B.java",
		"package b80223.b;\n" + 
		"public class B extends b80223.a.A {\n" + 
		"    void m() {}\n" + 
		"}",
		owner,
		true);
	// search for method declaration should find only A match
	IType type = workingCopies[0].getType("A");
	IMethod method = type.getMethod("m", new String[0]);
	search(method, DECLARATIONS);
	assertSearchResults(
		"src/b80223/a/A.java void b80223.a.A.m() [m] EXACT_MATCH"
	);
}

/**
 * Bug 80264: [search] Search for method declarations in workspace, disregarding declaring type
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=80264"
 * 
 * Following tests also verify
 * Bug 87778: [search] doesn't find all declarations of method with covariant return type
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=87778"
 */
// Methods
private void setUpBug80264_Methods() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80264/Methods.java",
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
	IType type = workingCopies[0].getType("Methods");
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
	IType type = workingCopies[0].getType("Methods");
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
	IType type = workingCopies[0].getType("Methods");
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
	IType type = workingCopies[0].getType("Methods");
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
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80264/Classes.java",
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
	IType type = workingCopies[0].getType("Classes").getType("Inner");
	search(type, DECLARATIONS);
	assertSearchResults(
		"src/b80264/Classes.java b80264.Classes$Inner [Inner] EXACT_MATCH"
	);
}
public void testBug80264_ClassesIgnoreDeclaringType() throws CoreException, JavaModelException {
	setUpBug80264_Classes();
	IType type = workingCopies[0].getType("Classes").getType("Inner");
	search(type, DECLARATIONS|IGNORE_DECLARING_TYPE);
	assertSearchResults(
		"src/b80264/Classes.java b80264.Classes$Inner [Inner] EXACT_MATCH\n" + 
		"src/b80264/Classes.java b80264.ClassesSub$Inner [Inner] EXACT_MATCH\n" + 
		"src/b80264/Classes.java b80264.ClassesOther$Inner [Inner] EXACT_MATCH"
	);
}
public void testBug80264_ClassesIgnoreReturnType() throws CoreException, JavaModelException {
	setUpBug80264_Classes();
	IType type = workingCopies[0].getType("Classes").getType("Inner");
	search(type, DECLARATIONS|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b80264/Classes.java b80264.Classes$Inner [Inner] EXACT_MATCH"
	);
}
public void testBug80264_ClassesIgnoreTypes() throws CoreException, JavaModelException {
	setUpBug80264_Classes();
	IType type = workingCopies[0].getType("Classes").getType("Inner");
	search(type, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b80264/Classes.java b80264.Classes$Inner [Inner] EXACT_MATCH\n" + 
		"src/b80264/Classes.java b80264.ClassesSub$Inner [Inner] EXACT_MATCH\n" + 
		"src/b80264/Classes.java b80264.ClassesOther$Inner [Inner] EXACT_MATCH"
	);
}
// Fields
private void setUpBug80264_Fields() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80264/Fields.java",
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
	IType type = workingCopies[0].getType("Fields");
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
	IType type = workingCopies[0].getType("Fields");
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
	IType type = workingCopies[0].getType("Fields");
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
	IType type = workingCopies[0].getType("Fields");
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
 * Bug 80890: [search] Strange search engine behaviour
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=80890"
 */
public void testBug80890() throws CoreException, JavaModelException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80890/A.java",
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
	IType type = workingCopies[0].getType("A");
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
 * Bug 80918: [1.5][search] ClassCastException when searching for references to binary type
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
 * Bug 81084: [1.5][search]Rename field fails on field based on parameterized type with member type parameter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=81084"
 */
public void testBug81084a() throws CoreException, JavaModelException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b81084a/Test.java",
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
	IType type = workingCopies[0].getType("Test").getType("Inner");
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
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b81084a/Test.java",
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
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b81084b/Test.java",
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
	IType type = workingCopies[0].getType("Test").getType("Inner");
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
 * Bug 81556: [search] correct results are missing in java search
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
 * Bug 82088: [search][javadoc] Method parameter types references not found in @see/@link tags
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=82088"
 */
public void testBug82088method() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b82088/m/Test.java",
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
	IType type = workingCopies[0].getType("A");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b82088/m/Test.java b82088.m.Test [A] EXACT_MATCH\n" + 
		"src/b82088/m/Test.java b82088.m.Test.a [A] EXACT_MATCH\n" + 
		"src/b82088/m/Test.java void b82088.m.Test.setA(A) [A] EXACT_MATCH"
	);
}
public void testBug82088constructor() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b82088/c/Test.java",
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
	IType type = workingCopies[0].getType("A");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b82088/c/Test.java b82088.c.Test [A] EXACT_MATCH\n" + 
		"src/b82088/c/Test.java b82088.c.Test.a [A] EXACT_MATCH\n" + 
		"src/b82088/c/Test.java b82088.c.Test(A) [A] EXACT_MATCH"
	);
}

/**
 * Bug 82208: [1.5][search][annot] Search for annotations misses references in default and values constructs
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=82208"
 */
private void setUpBug82208() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b82208/Test.java",
		"package b82208;\n" + 
		"interface B82208_I {}\n" + 
		"enum B82208_E {}\n" + 
		"@interface B82208_A {}\n" + 
		"public class B82208 {}\n"
	);
}
public void testBug82208_TYPE() throws CoreException {
	resultCollector.showRule = true;
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
	resultCollector.showRule = true;
	setUpBug82208();
	search("B82208*", CLASS, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b82208/Test.java b82208.B82208 [B82208] EXACT_MATCH"
	);
}
public void testBug82208_INTERFACE() throws CoreException {
	resultCollector.showRule = true;
	setUpBug82208();
	search("B82208*", INTERFACE, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b82208/Test.java b82208.B82208_I [B82208_I] EXACT_MATCH"
	);
}
public void testBug82208_ENUM() throws CoreException {
	resultCollector.showRule = true;
	setUpBug82208();
	search("B82208*", ENUM, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b82208/Test.java b82208.B82208_E [B82208_E] EXACT_MATCH"
	);
}
public void testBug82208_ANNOTATION_TYPE() throws CoreException {
	resultCollector.showRule = true;
	setUpBug82208();
	search("B82208*", ANNOTATION_TYPE, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b82208/Test.java b82208.B82208_A [B82208_A] EXACT_MATCH"
	);
}
public void testBug82208_CLASS_AND_INTERFACE() throws CoreException {
	resultCollector.showRule = true;
	setUpBug82208();
	search("B82208*", CLASS_AND_INTERFACE, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b82208/Test.java b82208.B82208_I [B82208_I] EXACT_MATCH\n" + 
		"src/b82208/Test.java b82208.B82208 [B82208] EXACT_MATCH"
	);
}
public void testBug82208_CLASS_AND_ENUMERATION() throws CoreException {
	resultCollector.showRule = true;
	setUpBug82208();
	search("B82208*", CLASS_AND_ENUM, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b82208/Test.java b82208.B82208_E [B82208_E] EXACT_MATCH\n" + 
		"src/b82208/Test.java b82208.B82208 [B82208] EXACT_MATCH"
	);
}

/**
 * Bug 82673: [1.5][search][annot] Search for annotations misses references in default and values constructs
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83012"
 */
public void testBug82673() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b82673/Test.java",
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
	IType type = selectType(workingCopies[0], "Test").getMethod("test1", new String[0]).getType("Dummy", 1);
	search(type, REFERENCES);
	assertSearchResults(
		"src/b82673/Test.java void b82673.Test.test1() [Dummy] EXACT_MATCH\n" + 
		"src/b82673/Test.java void b82673.Test.test1() [Dummy] EXACT_MATCH"
	);
}

/**
 * Bug 83012: [1.5][search][annot] Search for annotations misses references in default and values constructs
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83012"
 */
public void testBug83012() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83012/Test.java",
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
	IType type = selectType(workingCopies[0], "A");
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
 * Bug 83230: [1.5][search][annot] search for annotation elements does not seem to be implemented yet
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83230"
 */
private void setUpBug83230_Explicit() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83230/Test.java",
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
	resultCollector.showRule = true;
	setUpBug83230_Explicit();
	IMethod method = selectMethod(workingCopies[0], "authorName");
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
	resultCollector.showRule = true;
	setUpBug83230_Explicit();
	IMethod method = selectMethod(workingCopies[0], "authorName");
	search(method, DECLARATIONS);
	assertSearchResults(
		"src/b83230/Test.java String[] b83230.Author.authorName() [authorName] EXACT_MATCH"
	);
}
public void testBug83230_Explicit02() throws CoreException {
	resultCollector.showRule = true;
	setUpBug83230_Explicit();
	IType type = selectType(workingCopies[0], "Address");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b83230/Test.java String void b83230.Test.foo():Local#1.foo() [Author.Address] EXACT_MATCH\n" + 
		"src/b83230/Test.java String void b83230.Test.foo():Local#1.foo() [Author.Address] EXACT_MATCH"
	);
}
public void testBug83230_Explicit03() throws CoreException {
	resultCollector.showRule = true;
	setUpBug83230_Explicit();
	IMethod method = selectMethod(workingCopies[0], "foo");
	search(method, REFERENCES);
	assertSearchResults(
		"src/b83230/Test.java String void b83230.Test.foo():Local#1.foo() [foo(obj)] EXACT_MATCH"
	);
}
public void testBug83230_Explicit04() throws CoreException {
	resultCollector.showRule = true;
	setUpBug83230_Explicit();
	IField field = selectField(workingCopies[0], "city");
	search(field, REFERENCES);
	assertSearchResults(
		"src/b83230/Test.java String void b83230.Test.foo():Local#1.foo() [city] EXACT_MATCH"
	);
}
public void testBug83230_Explicit05() throws CoreException {
	resultCollector.showRule = true;
	setUpBug83230_Explicit();
	IField field = selectField(workingCopies[0], "ageMin");
	search(field, REFERENCES);
	assertSearchResults(
		"src/b83230/Test.java b83230.Test.min [ageMin] EXACT_MATCH"
	);
}
public void testBug83230_Implicit01() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83230/Test.java",
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
	IType type = selectType(workingCopies[0], "Annot");
	IMethod method = type.getMethod("value", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b83230/Test.java b83230.Test [41] EXACT_MATCH\n" + 
		"src/b83230/Test.java b83230.Test.bar [21] EXACT_MATCH\n" + 
		"src/b83230/Test.java void b83230.Test.foo() [10] EXACT_MATCH"
	);
}
public void testBug83230_Implicit02() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83230/Test.java",
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
	IType type = selectType(workingCopies[0], "A");
	IMethod method = type.getMethod("value", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b83230/Test.java A b83230.Main.first() [\"Void\"] EXACT_MATCH\n" + 
		"src/b83230/Test.java b83230.Test [\"\"] EXACT_MATCH\n" + 
		"src/b83230/Test.java b83230.Test [\"2\"] EXACT_MATCH"
	);
}

/**
 * Bug 83304: [search] correct results are missing in java search
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83304"
 */
public void testBug83304() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83304/Test.java",
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
	IType type = selectType(workingCopies[0], "Class", 3);
	search(type, REFERENCES, ERASURE_RULE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/b83304/Test.java void b83304.Test.foo() [Class] ERASURE_MATCH\n" + 
		"src/b83304/Test.java void b83304.Test.foo() [Class] ERASURE_MATCH\n" + 
		"src/b83304/Test.java void b83304.Test.foo() [Class] EXACT_MATCH"
	);
}
private void setUpBug83304_TypeParameterizedElementPattern() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83304/Types.java",
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
	resultCollector.showRule = true;
	setUpBug83304_TypeParameterizedElementPattern();
	IType type = selectType(workingCopies[0], "Generic", 4);
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
	resultCollector.showRule = true;
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
	resultCollector.showRule = true;
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
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83304/Methods.java",
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
	resultCollector.showRule = true;
	setUpBug83304_MethodParameterizedElementPattern();
	IMethod method = selectMethod(workingCopies[0], "generic", 2);
	search(method, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] ERASURE_MATCH\n" + 
		"src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] EXACT_MATCH\n" + 
		"src/b83304/Methods.java void b83304.Methods.test() [generic(\"\")] ERASURE_MATCH"
	);
}
public void testBug83304_MethodGenericElementPattern() throws CoreException {
	resultCollector.showRule = true;
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
	resultCollector.showRule = true;
	setUpBug83304_MethodParameterizedElementPattern();
	search("<Exception>generic", METHOD, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] ERASURE_MATCH\n" + 
		"src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] EXACT_MATCH\n" + 
		"src/b83304/Methods.java void b83304.Methods.test() [generic(\"\")] ERASURE_MATCH"
	);
}
private void setUpBug83304_ConstructorGenericElementPattern() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83304/Constructors.java",
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
	resultCollector.showRule = true;
	setUpBug83304_ConstructorGenericElementPattern();
	IMethod method = selectMethod(workingCopies[0], "Single", 3);
	search(method, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"src/b83304/Constructors.java void b83304.Constructors.test() [new <Throwable>Single<String>(\"\", exc)] ERASURE_MATCH\n" + 
		"src/b83304/Constructors.java void b83304.Constructors.test() [new <Exception>Single<String>(\"\", exc)] EXACT_MATCH\n" + 
		"src/b83304/Constructors.java void b83304.Constructors.test() [new <String>Single<String>(\"\", \"\")] ERASURE_MATCH"
	);
}
public void testBug83304_ConstructorParameterizedElementPattern() throws CoreException {
	resultCollector.showRule = true;
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
	resultCollector.showRule = true;
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
 * Bug 83804: [1.5][javadoc] Missing Javadoc node for package declaration
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83804"
 */
private void setUpBug83804_Type() throws CoreException {
	workingCopies = new ICompilationUnit[2];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83804/package-info.java",
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
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b83804/Test.java",
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
	resultCollector.showInsideDoc = true;
	setUpBug83804_Type();
	IType type = workingCopies[1].getType("Test");
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
	resultCollector.showInsideDoc = true;
	setUpBug83804_Type();
	IMethod[] methods = workingCopies[1].getType("Test").getMethods();
	assertEquals("Invalid number of methods", 1, methods.length);
	search(methods[0], REFERENCES);
	assertSearchResults(
		"src/b83804/package-info.java [foo()] EXACT_MATCH INSIDE_JAVADOC"
	);
}
public void testBug83804_Field() throws CoreException {
	resultCollector.showInsideDoc = true;
	setUpBug83804_Type();
	IField[] fields = workingCopies[1].getType("Test").getFields();
	assertEquals("Invalid number of fields", 1, fields.length);
	search(fields[0], REFERENCES);
	assertSearchResults(
		"src/b83804/package-info.java [field] EXACT_MATCH INSIDE_JAVADOC"
	);
}

/**
 * Bug 83388: [1.5][search] Search for varargs method not finding match
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83388"
 */
public void testBug83388() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83388/R.java",
		"package b83388;\n" + 
		"import b83388.*;\n" + 
		"public class R {}\n"
	);
	IImportDeclaration importDeclaration = workingCopies[0].getImport("pack");
	assertNotNull("Cannot find \"pack\" import declaration for "+workingCopies[0].getElementName(), importDeclaration);
	SearchPattern pattern = SearchPattern.createPattern(
		"pack", 
		PACKAGE,
		DECLARATIONS, 
		EXACT_RULE);
	assertNotNull("Pattern should not be null", pattern);
	MatchLocator.setFocus(pattern, importDeclaration);
	new SearchEngine(workingCopies).search(
		pattern,
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		getJavaSearchScopeBugs(),
		resultCollector,
		null
	);
	assertSearchResults(
		"src/b83388/R.java b83388 [No source] EXACT_MATCH"
	);
}
public void testBug83388b() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83388/R.java",
		"package b83388;\n" + 
		"import b83388.*;\n" + 
		"public class R {}\n"
	);
	IPackageDeclaration packageDeclaration = workingCopies[0].getPackageDeclaration("pack");
	assertNotNull("Cannot find \"pack\" import declaration for "+workingCopies[0].getElementName(), packageDeclaration);
	SearchPattern pattern = SearchPattern.createPattern(
		"pack", 
		PACKAGE,
		DECLARATIONS, 
		EXACT_RULE);
	assertNotNull("Pattern should not be null", pattern);
	MatchLocator.setFocus(pattern, packageDeclaration);
	new SearchEngine(workingCopies).search(
		pattern,
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		getJavaSearchScopeBugs(),
		resultCollector,
		null
	);
	assertSearchResults(
		"src/b83388/R.java b83388 [No source] EXACT_MATCH"
	);
}

/**
 * Bug 83693: [search][javadoc] References to methods/constructors: range does not include parameter lists
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83693"
 */
public void testBug83693() throws CoreException {
	resultCollector.showRule = true;
	resultCollector.showInsideDoc = true;
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83693/A.java",
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
	IMethod[] methods = workingCopies[0].getType("A").getMethods();
	assertEquals("Invalid number of methods", 1, methods.length);
	search(methods[0], REFERENCES);
	assertSearchResults(
		"src/b83693/A.java [b83693.A.m] EXACT_MATCH OUTSIDE_JAVADOC\n" + 
		"src/b83693/A.java b83693.A [m(int)] EXACT_MATCH INSIDE_JAVADOC\n" + 
		"src/b83693/A.java void b83693.A.m(int) [m(i)] EXACT_MATCH OUTSIDE_JAVADOC"
	);
}

/**
 * Bug 83716: [search] refs to 2-arg constructor on Action found unexpected matches
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83716"
 * 
 * Note that this test does verify that bug is really fixed, but only that it has no impact
 * on existing behavior. It was not really possible to put a test in this suite to verify that
 * bug is effectively fixed as it appears only to potential match found in plugin dependencies...
 */
public void testBug83716() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83716/X.java",
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
 * Bug 84100: [1.5][search] Search for varargs method not finding match
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=84100"
 */
private void setUpBug84100() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[2];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b84100/X.java",
		"package b84100;\n" + 
		"public class X {\n" + 
		"	void foo() {}\n" + 
		"	void foo(String s) {}\n" + 
		"	void foo(String... xs) {}\n" + 
		"	void foo(int x, String... xs) {}\n" + 
		"	void foo(String s, int x, String... xs) {}\n" + 
		"}\n"
		);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b84100/Z.java",
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
	resultCollector.showRule = true;
	setUpBug84100();
	IMethod method = selectMethod(workingCopies[0], "foo", 1);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b84100/Z.java void b84100.Z.foo() [foo()] EXACT_MATCH"
	);
}
public void testBug84100b() throws CoreException {
	resultCollector.showRule = true;
	setUpBug84100();
	IMethod method = selectMethod(workingCopies[0], "foo", 2);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b84100/Z.java void b84100.Z.foo() [foo(\"\")] EXACT_MATCH"
	);
}
public void testBug84100c() throws CoreException {
	resultCollector.showRule = true;
	setUpBug84100();
	IMethod method = selectMethod(workingCopies[0], "foo", 3);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b84100/Z.java void b84100.Z.foo() [foo(\"\", \"\")] EXACT_MATCH\n" + 
		"src/b84100/Z.java void b84100.Z.foo() [foo(\"\", \"\", null)] EXACT_MATCH"
	);
}
public void testBug84100d() throws CoreException {
	resultCollector.showRule = true;
	setUpBug84100();
	IMethod method = selectMethod(workingCopies[0], "foo", 4);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b84100/Z.java void b84100.Z.foo() [foo(3, \"\", \"\")] EXACT_MATCH"
	);
}
public void testBug84100e() throws CoreException {
	resultCollector.showRule = true;
	setUpBug84100();
	IMethod method = selectMethod(workingCopies[0], "foo", 5);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b84100/Z.java void b84100.Z.foo() [foo(\"\", 3, \"\", \"\")] EXACT_MATCH"
	);
}

/**
 * Bug 84121: [1.5][search][varargs] reference to type reported as inaccurate in vararg
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=84121"
 */
public void testBug84121() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b84121/Test.java",
		"package b84121;\n" + 
		"public class Test {\n" + 
		"	void foo(Test... t) {}\n" + 
		"	void foo(int x, Test... t) {}\n" + 
		"	void foo(Test[] t1, Test... t2) {}\n" + 
		"}\n"
	);
	IType type = workingCopies[0].getType("Test");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b84121/Test.java void b84121.Test.foo(Test ...) [Test] EXACT_MATCH\n" + 
		"src/b84121/Test.java void b84121.Test.foo(int, Test ...) [Test] EXACT_MATCH\n" + 
		"src/b84121/Test.java void b84121.Test.foo(Test[], Test ...) [Test] EXACT_MATCH\n" + 
		"src/b84121/Test.java void b84121.Test.foo(Test[], Test ...) [Test] EXACT_MATCH"
	);
}

/**
 * Bug 84724: [1.5][search] Search for varargs method not finding match
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=84724"
 */
private void setUpBug84724() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[2];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b84724/X.java",
		"package b84724;\n" + 
		"public class X {\n" + 
		"	X(String s) {}\n" + 
		"	X(String... v) {}\n" + 
		"	X(int x, String... v) {}\n" + 
		"	X(String s, int x, String... v) {}\n" + 
		"}\n"
		);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b84724/Z.java",
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
	resultCollector.showRule = true;
	setUpBug84724();
	IMethod method = selectMethod(workingCopies[0], "X", 2);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b84724/Z.java void b84724.Z.foo() [new X(\"\")] EXACT_MATCH"
	);
}
public void testBug84724b() throws CoreException {
	resultCollector.showRule = true;
	setUpBug84724();
	IMethod method = selectMethod(workingCopies[0], "X", 3);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b84724/Z.java void b84724.Z.foo() [new X()] EXACT_MATCH\n" + 
		"src/b84724/Z.java void b84724.Z.foo() [new X(\"\", \"\")] EXACT_MATCH\n" + 
		"src/b84724/Z.java void b84724.Z.foo() [new X(\"\", \"\", null)] EXACT_MATCH"
	);
}
public void testBug84724c() throws CoreException {
	resultCollector.showRule = true;
	setUpBug84724();
	IMethod method = selectMethod(workingCopies[0], "X", 4);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b84724/Z.java void b84724.Z.foo() [new X(3, \"\", \"\")] EXACT_MATCH"
	);
}
public void testBug84724d() throws CoreException {
	resultCollector.showRule = true;
	setUpBug84724();
	IMethod method = selectMethod(workingCopies[0], "X", 5);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b84724/Z.java void b84724.Z.foo() [new X(\"\", 3, \"\", \"\")] EXACT_MATCH"
	);
}

/**
 * Bug 84727: [1.5][search] String pattern search does not work with multiply nested types
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=84727"
 */
private void setUpBug84727() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[3];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b84727/A.java",
		"package b84727;\n" + 
		"public interface A {\n" + 
		"	Set<Set<Exception>> getXYZ(List<Set<Exception>> arg);\n" + 
		"	void getXYZ(String s);\n" + 
		"}\n"
		);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b84727/X.java",
		"package b84727;\n" + 
		"public class X {\n" + 
		"	A a;\n" + 
		"	void foo() {\n" + 
		"		a.getXYZ(new ArrayList());\n" + 
		"		a.getXYZ(\"\");\n" + 
		"	}\n" + 
		"}\n"
		);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b84727/List.java",
		"package b84727;\n" + 
		"public interface List<E> {}\n" + 
		"interface Set<E> {}\n" + 
		"class ArrayList<E> implements List<E> {}"
	);
}
public void testBug84727() throws CoreException {
	resultCollector.showRule = true;
	setUpBug84727();
	IMethod[] methods = workingCopies[0].getType("A").getMethods();
	assertEquals("Invalid number of methods", 2, methods.length);
	search(methods[0], REFERENCES);
	assertSearchResults(
		"src/b84727/X.java void b84727.X.foo() [getXYZ(new ArrayList())] EXACT_MATCH"
	);
}
public void testBug84727b() throws CoreException {
	resultCollector.showRule = true;
	setUpBug84727();
	IMethod[] methods = workingCopies[0].getType("A").getMethods();
	assertEquals("Invalid number of methods", 2, methods.length);
	search(methods[1], REFERENCES);
	assertSearchResults(
		"src/b84727/X.java void b84727.X.foo() [getXYZ(\"\")] EXACT_MATCH"
	);
}

/**
 * Bug 85810: [1.5][search] Missed type parameter reference in implements clause
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=85810"
 */
public void testBug85810() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b85810/Test.java",
		"package b85810;\n" + 
		"public class Test<E> implements In<Test<E>> {\n" + 
		"	E e;\n" + 
		"}\n" +
		"interface In<T> {}\n"
		);
	ITypeParameter param = selectTypeParameter(workingCopies[0], "E");
	search(param, REFERENCES);
	assertSearchResults(
		"src/b85810/Test.java b85810.Test [E] EXACT_MATCH\n" + 
		"src/b85810/Test.java b85810.Test.e [E] EXACT_MATCH"
	);
}

/**
 * Bug 86596: [search] Search for type finds segments in import
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=86596"
 */
public void testBug86596() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[3];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b86596/aa/link/A.java",
		"package b86596.aa.link;\n" + 
		"public interface A {}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b86596/bb/Link.java",
		"package b86596.bb;\n" + 
		"public class Link{}\n"
	);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b86596/cc/X.java",
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
 * Bug 86642: [search] no match found of package-visible supertypes in subtypes
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=86642"
 */
public void testBug86642() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[2];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b86642/A.java",
		"package b86642;\n" + 
		"class A {\n" + 
		"	public void m() {}\n" + 
		"	protected void f(A a){}\n" + 
		"}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b86642/B.java",
		"package b86642;\n" + 
		"public class B extends A{\n" + 
		"	protected void f(A a){\n" + 
		"		a.m();\n" + 
		"	}\n" + 
		"}\n"
	);
	IType type = workingCopies[0].getType("A");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b86642/A.java void b86642.A.f(A) [A] EXACT_MATCH\n" + 
		"src/b86642/B.java b86642.B [A] EXACT_MATCH\n" + 
		"src/b86642/B.java void b86642.B.f(A) [A] EXACT_MATCH"
	);
}
/**
 * Bug 86293: [search] Search for method declaration with pattern "run()" reports match in binary field instead of anonymous class
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
 * Bug 86380: [1.5][search][annot] Add support to find references inside annotations on a package declaration
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=86380"
 */
private void setUpBug86380() throws CoreException {
	resultCollector.showInsideDoc = true;
	workingCopies = new ICompilationUnit[2];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b86380/package-info.java",
		"/**\n" + 
		" * @see Annot#field\n" + 
		" */\n" + 
		"@Annot(value=11)\n" + 
		"package b86380;\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b86380/Test.java",
		"package b86380;\n" + 
		"@Annot(12) public class Test {\n" + 
		"	public int field = Annot.field;\n" + 
		"	public void foo() {}\n" + 
		"}\n"
	);
}
public void testBug86380_Type() throws CoreException {
	resultCollector.showInsideDoc = true;
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
	resultCollector.showInsideDoc = true;
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
	resultCollector.showInsideDoc = true;
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
 * Bug 88174: [1.5][search][annot] Search for annotations misses references in default and values constructs
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=88174"
 */
public void testBug88174() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[2];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b88174/Test.java",
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
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b88174/X.java",
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
	IMethod method = workingCopies[0].getType("Test").getMethod("getTheValue", new String[0]);
	search(method, DECLARATIONS | IGNORE_DECLARING_TYPE);
	assertSearchResults(
		"src/b88174/Test.java int b88174.Test.a:<anonymous>#1.getTheValue() [getTheValue] EXACT_MATCH\n" + 
		"src/b88174/Test.java int b88174.Test.getTheValue() [getTheValue] EXACT_MATCH\n" + 
		"src/b88174/X.java int b88174.X.x:<anonymous>#1.getTheValue() [getTheValue] EXACT_MATCH\n" + 
		"src/b88174/X.java int b88174.X.getTheValue() [getTheValue] EXACT_MATCH"
	);
}

/**
 * Bug 87627: [search] correct results are missing in java search
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
 * Bug 88300: [search] Reference search result is changed by placement of private method
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=88300"
 */
public void testBug88300() throws CoreException {
	workingCopies = new ICompilationUnit[3];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b88300/SubClass.java",
		"package b88300;\n" + 
		"public class SubClass extends SuperClass {\n" + 
		"	private void aMethod(String x) {\n" + 
		"	}\n" + 
		"	public void aMethod(Object x) {\n" + 
		"	}\n" + 
		"}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b88300/SuperClass.java",
		"package b88300;\n" + 
		"public class SuperClass {\n" + 
		"    public void aMethod(Object x) {\n" + 
		"    }\n" + 
		"}\n"
		);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b88300/User.java",
		"package b88300;\n" + 
		"public class User {\n" + 
		"    public void methodUsingSubClassMethod() {\n" + 
		"        SuperClass user = new SubClass();\n" + 
		"        user.aMethod(new Object());\n" + 
		"    }\n" + 
		"}\n"
	);
	IType type = workingCopies[0].getType("SubClass");
	search(type.getMethods()[1], REFERENCES);
	assertSearchResults(
		"src/b88300/User.java void b88300.User.methodUsingSubClassMethod() [aMethod(new Object())] EXACT_MATCH"
	);
}
public void testBug88300b() throws CoreException {
	workingCopies = new ICompilationUnit[3];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b88300/SubClass.java",
		"package b88300;\n" + 
		"public class SubClass extends SuperClass {\n" + 
		"	public void aMethod(Object x) {\n" + 
		"	}\n" + 
		"	private void aMethod(String x) {\n" + 
		"	}\n" + 
		"}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b88300/SuperClass.java",
		"package b88300;\n" + 
		"public class SuperClass {\n" + 
		"    public void aMethod(Object x) {\n" + 
		"    }\n" + 
		"}\n"
		);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b88300/User.java",
		"package b88300;\n" + 
		"public class User {\n" + 
		"    public void methodUsingSubClassMethod() {\n" + 
		"        SuperClass user = new SubClass();\n" + 
		"        user.aMethod(new Object());\n" + 
		"    }\n" + 
		"}\n"
	);
	IType type = workingCopies[0].getType("SubClass");
	search(type.getMethods()[0], REFERENCES);
	assertSearchResults(
		"src/b88300/User.java void b88300.User.methodUsingSubClassMethod() [aMethod(new Object())] EXACT_MATCH"
	);
}
public void testBug88300c() throws CoreException {
	workingCopies = new ICompilationUnit[3];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b88300/not/fixed/ConditionalFlowInfo.java",
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
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b88300/not/fixed/FlowInfo.java",
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
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b88300/not/fixed/UnconditionalFlowInfo.java",
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
	IType type = workingCopies[2].getType("UnconditionalFlowInfo");
	search(type.getMethods()[2], REFERENCES);
	assertSearchResults(
		"src/b88300/not/fixed/ConditionalFlowInfo.java void b88300.not.fixed.ConditionalFlowInfo.markAsDefinitelyNull(LocalVariableBinding) [markAsDefinitelyNull(local)] EXACT_MATCH"
	);
}

/**
 * Bug 89686: [1.5][search] JavaModelException on ResolvedSourceMethod during refactoring
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=89686"
 */
public void testBug89686() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b89686/A.java",
		"package b89686;\n" + 
		"public enum Color {\n" + 
		"    RED, GREEN(), BLUE(17), PINK((1+(1+1))) {/*anon*/};\n" + 
		"    Color() {}\n" + 
		"    Color(int i) {}\n" + 
		"}"
	);
	IType type = workingCopies[0].getType("Color");
	IMethod method = type.getMethod("Color", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"src/b89686/A.java b89686.Color.RED [RED] EXACT_MATCH\n" + 
		"src/b89686/A.java b89686.Color.GREEN [GREEN()] EXACT_MATCH"
	);
}
public void testBug89686b() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b89686/A.java",
		"package b89686;\n" + 
		"public enum Color {\n" + 
		"    RED, GREEN(), BLUE(17), PINK((1+(1+1))) {/*anon*/};\n" + 
		"    Color() {}\n" + 
		"    Color(int i) {}\n" + 
		"}"
	);
	IType type = workingCopies[0].getType("Color");
	IMethod method = type.getMethod("Color", new String[] { "I"} );
	search(method, REFERENCES);
	assertSearchResults(
		"src/b89686/A.java b89686.Color.BLUE [BLUE(17)] EXACT_MATCH\n" + 
		"src/b89686/A.java b89686.Color.PINK [PINK((1+(1+1)))] EXACT_MATCH"
	);
}

/**
 * Bug 89848: [search] does not find method references in anonymous class of imported jarred plugin
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
 * Bug 90779: [search] Constructor Declaration search with ignoring declaring and return type also ignores type name
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=90779"
 */
public void testBug90779() throws CoreException {
	workingCopies = new ICompilationUnit[3];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b90779/A.java",
		"package b90779;\n" +
		"public class A {\n" + 
		"	public A() {}\n" + 
		"}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b90779/B.java",
		"package b90779;\n" +
		"public class B {\n" + 
		"	public B() {}\n" + 
		"}\n"
	);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b90779/C.java",
		"package b90779;\n" +
		"public class C {\n" + 
		"	public C() {}\n" + 
		"}\n"
	);
	IType type = workingCopies[0].getType("A");
	IMethod[] methods = type.getMethods();
	assertEquals("Wrong number of methods", 1, methods.length);
	search(methods[0], DECLARATIONS | IGNORE_DECLARING_TYPE | IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b90779/A.java b90779.A() [A] EXACT_MATCH"
	);
}

/**
 * Bug 90915: [1.5][search] NPE in PatternLocator
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=90915"
 */
public void testBug90915() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b90915/X.java",
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
	IType type = workingCopies[0].getType("X");
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
 * Bug 91542: [1.5][search] JavaModelException on ResolvedSourceMethod during refactoring
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=91542"
 */
public void testBug91542() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b91542/A.java",
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
	IType type = workingCopies[0].getType("B");
	IMethod method = type.getMethod("foo", new String[0]);
	searchDeclarationsOfSentMessages(method, this.resultCollector);
	assertSearchResults(
		"src/b91542/A.java void b91542.A.a(A<T>) [a(A<T> a)] EXACT_MATCH"
	);
}

/**
 * Bug 91078: [search] Java search for package reference wrongly identifies inner class as package
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=91078"
 */
public void testBug91078() throws CoreException {
	workingCopies = new ICompilationUnit[3];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b91078/test/Example.java",
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
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b91078/util/HashMap.java",
		"package b91078.util;\n" + 
		"public class HashMap implements Map {\n" + 
		"	public Entry entry;\n" + 
		"}\n"
	);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b91078/util/Map.java",
		"package b91078.util;\n" + 
		"public interface Map {\n" + 
		"	class Entry{}\n" + 
		"}\n"
	);
	search("*", PACKAGE, REFERENCES, getJavaSearchWorkingCopiesScope(workingCopies[0]));
	assertSearchResults(
		"src/b91078/test/Example.java [b91078.util] EXACT_MATCH\n" + 
		"src/b91078/test/Example.java [b91078.util] EXACT_MATCH\n" + 
		"src/b91078/test/Example.java Map.Entry b91078.test.Example.logAll(Object) [b91078.util] EXACT_MATCH\n" + 
		"src/b91078/test/Example.java Map.Entry b91078.test.Example.logAll(Object) [b91078.util] EXACT_MATCH"
	);
}

/**
 * Bug 92264: [search] all types names should support patterns for package/enclosing type name
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
		getJavaSearchScopeBugs(),
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
		getJavaSearchScopeBugs(),
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
		"java.lang.annotation.Annotation",
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
		getJavaSearchScopeBugs(),
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
		getJavaSearchScopeBugs(),
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
 * Bug 92944: [1.5][search] SearchEngine#searchAllTypeNames doesn't honor enum or annotation element kind
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=92944"
 */
private void setUpBug92944() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b92944/Test.java",
		"package b92944;\n" + 
		"interface B92944_I {}\n" + 
		"enum B92944_E {}\n" + 
		"@interface B92944_A {}\n" + 
		"public class B92944 {}\n"
	);
}
public void testBug92944_TYPE() throws CoreException {
	resultCollector.showRule = true;
	setUpBug92944();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
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
	resultCollector.showRule = true;
	setUpBug92944();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
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
	resultCollector.showRule = true;
	setUpBug92944();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
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
	resultCollector.showRule = true;
	setUpBug92944();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
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
	resultCollector.showRule = true;
	setUpBug92944();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
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
	resultCollector.showRule = true;
	setUpBug92944();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
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
	resultCollector.showRule = true;
	setUpBug92944();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
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
 * Bug 93392: [1.5][search][annot] search for annotation elements does not seem to be implemented yet
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=93392"
 * 
 * Note that this test valid also:
 * Bug 94062: [1.5][search][annot] search for annotation elements incorrect match range
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=94062"
 */
public void testBug93392() throws CoreException {
	TestCollector collector = new TestCollector();
	collector.showAccuracy = true;
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b93392/Test.java",
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
	IType type = selectType(workingCopies[0], "Annot");
	IMethod method = type.getMethod("value", new String[0]);
	search(method, REFERENCES, getJavaSearchScopeBugs(), collector);
	assertSearchResults(
		"src/b93392/Test.java b93392.Test [41] EXACT_MATCH\n" + 
		"src/b93392/Test.java b93392.Test.bar [21] EXACT_MATCH\n" + 
		"src/b93392/Test.java void b93392.Test.foo() [10] EXACT_MATCH",
		collector
	);
	// Verify matches positions
	String source = workingCopies[0].getSource();
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
 * Bug 94160: [1.5][search] Generic method in superclass does not exist
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=94160"
 */
public void testBug94160() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b94160/Test.java",
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
	IType type = workingCopies[0].getType("CTest");
	IField field = type.getField("myList");
	new SearchEngine(this.workingCopies).searchDeclarationsOfSentMessages(field, resultCollector, null);
	assertSearchResults(
		"src/b94160/Test.java List<T> b94160.Test.generateList(Class<T>) [generateList(Class<T> clazz)] EXACT_MATCH"
	);
}

/**
 * Bug 94389: [search] InvocationTargetException on Rename
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=94389"
 */
public void testBug94389() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b94389/Test.java",
		"package b94389;\n" + 
		"public class Test {\n" + 
		"	public void foo() {}\n" + 
		"	public void foo() {}\n" + 
		"	public void foo() {}\n" + 
		"	public void foo() {}\n" + 
		"}\n"
	);
	IType type = workingCopies[0].getType("Test");
	IMethod[] methods = type.getMethods();
	int methodsLength = methods.length;

	// Perform search on each duplicate method
	IJavaSearchScope scope = getJavaSearchScopeBugs();
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
 * Bug 94718: [1.5][search][annot] Find references in workspace breaks on an annotation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=94718"
 */
public void testBug94718() throws CoreException {
	workingCopies = new ICompilationUnit[2];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b94718/SetUp.java",
		"package b94718;\n" + 
		"public @interface SetUp {\n" + 
		"	String value() {}\n" + 
		"}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b94718/Test.java",
		"package b94718;\n" + 
		"@SetUp(\"howdy\")\n" + 
		"public class Test {\n" + 
		"}\n"
	);
	IType type = workingCopies[1].getType("SetUp");
	search(type, REFERENCES, SearchEngine.createWorkspaceScope());
	assertSearchResults(
		"src/b94718/Test.java b94718.Test [SetUp] EXACT_MATCH"
	);
}

/**
 * Bug 95152: [search] Field references not found when type is a qualified member type [regression]
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
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b95152/T.java",
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
	IType type = workingCopies[0].getType("T").getType("T2");
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
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b95152/T.java",
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
	IType type = workingCopies[0].getType("T").getType("T2");
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
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b95152/T.java",
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
	IType type = workingCopies[0].getType("T").getType("T2");
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
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b95152/T.java",
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
	IType type = workingCopies[0].getType("T").getType("T2");
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
 * Bug 95794: [1.5][search][annot] Find references in workspace breaks on an annotation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=95794"
 */
public void testBug95794() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearchBugs", "src", "b95794", "Test.java");
	IType type = unit.getType("Test");
	
	// Verify matches
	TestCollector occurencesCollector = new TestCollector();
	occurencesCollector.showAccuracy = true;
	search(type, ALL_OCCURRENCES, getJavaSearchScopeBugs(), occurencesCollector);
	assertSearchResults(
		"src/b95794/Test.java [b95794.Test] EXACT_MATCH\n" + 
		"src/b95794/Test.java [b95794.Test] EXACT_MATCH\n" + 
		"src/b95794/Test.java b95794.Test [Test] EXACT_MATCH\n" + 
		"src/b95794/Test.java b95794.Test.there [Test] EXACT_MATCH",
		occurencesCollector
	);
	
	// Verify with references matches
	TestCollector referencesCollector = new TestCollector();
	search(type, REFERENCES, getJavaSearchScopeBugs(), referencesCollector);
	assertEquals("Problem with occurences or references number of matches: ", occurencesCollector.matches.size()-1, referencesCollector.matches.size());
}
public void testBug95794b() throws CoreException {
	resultCollector.showRule = true;
	ICompilationUnit unit = getCompilationUnit("JavaSearchBugs", "src", "b95794", "Test.java");
	IType type = unit.getType("Test").getType("Color");
	
	// Verify matches
	TestCollector occurencesCollector = new TestCollector();
	occurencesCollector.showAccuracy = true;
	search(type, ALL_OCCURRENCES, getJavaSearchScopeBugs(), occurencesCollector);
	assertSearchResults(
		"src/b95794/Test.java [b95794.Test.Color] EXACT_MATCH\n" + 
		"src/b95794/Test.java [b95794.Test.Color] EXACT_MATCH\n" + 
		"src/b95794/Test.java void b95794.Test.main(String[]) [Color] EXACT_MATCH\n" + 
		"src/b95794/Test.java b95794.Test$Color [Color] EXACT_MATCH",
		occurencesCollector
	);
	
	// Verify with references matches
	TestCollector referencesCollector = new TestCollector();
	search(type, REFERENCES, getJavaSearchScopeBugs(), referencesCollector);
	assertEquals("Problem with occurences or references number of matches: ", occurencesCollector.matches.size()-1, referencesCollector.matches.size());
}
public void testBug95794c() throws CoreException {
	resultCollector.showRule = true;
	ICompilationUnit unit = getCompilationUnit("JavaSearchBugs", "src", "b95794", "Test.java");
	IField field = unit.getType("Test").getType("Color").getField("WHITE");
	
	// Verify matches
	TestCollector occurencesCollector = new TestCollector();
	occurencesCollector.showAccuracy = true;
	search(field, ALL_OCCURRENCES, getJavaSearchScopeBugs(), occurencesCollector);
	assertSearchResults(
		"src/b95794/Test.java [WHITE] EXACT_MATCH\n" + 
		"src/b95794/Test.java void b95794.Test.main(String[]) [WHITE] EXACT_MATCH\n" + 
		"src/b95794/Test.java b95794.Test$Color.WHITE [WHITE] EXACT_MATCH",
		occurencesCollector
	);
	
	// Verify with references matches
	TestCollector referencesCollector = new TestCollector();
	search(field, REFERENCES, getJavaSearchScopeBugs(), referencesCollector);
	assertEquals("Problem with occurences or references number of matches: ", occurencesCollector.matches.size()-1, referencesCollector.matches.size());
}

/**
 * Bug 96761: [1.5][search] Search for declarations of generic method finds non-overriding method
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=96761"
 */
public void testBug96761() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b96761/Generic.java",
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
	IType type = workingCopies[0].getType("Generic");
	IMethod method= type.getMethods()[0];
	search(method, REFERENCES);
	assertSearchResults(""); // Expect no result
}

/**
 * Bug 96763: [1.5][search] Search for method declarations does not find overridden method with different signature
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=96763"
 */
public void testBug96763() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b96763/Test.java",
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
	IMethod method = workingCopies[0].getType("Sub").getMethods()[0];
	search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b96763/Test.java void b96763.Test.first(Exception) [first] EXACT_MATCH\n" + 
		"src/b96763/Test.java void b96763.Sub.first(Exception) [first] EXACT_MATCH"
	);
}
public void testBug96763b() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b96763/Test.java",
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
	IMethod method = workingCopies[0].getType("Sub").getMethods()[1];
	search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b96763/Test.java void b96763.Test.second(T) [second] EXACT_MATCH\n" + 
		"src/b96763/Test.java void b96763.Sub.second(Exception) [second] EXACT_MATCH"
	);
}
public void testBug96763c() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b96763/Test.java",
		"package b96763;\n" + 
		"public class Test<T> {\n" + 
		"	void methodT(T t) {}\n" + 
		"}\n" + 
		"class Sub<X> extends Test<X> {\n" + 
		"	void methodT(X x) {} // overrides Super#methodT(T)\n" + 
		"}\n"
	);
	IMethod method = workingCopies[0].getType("Sub").getMethods()[0];
	search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b96763/Test.java void b96763.Test.methodT(T) [methodT] EXACT_MATCH\n" + 
		"src/b96763/Test.java void b96763.Sub.methodT(X) [methodT] EXACT_MATCH"
	);
}

/**
 * Bug 97087: [1.5][search] Can't find reference of generic class's constructor.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=97087"
 */
public void testBug97087() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	this.resultCollector.showRule = true;
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b97087/Bug.java",
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
	IType type = workingCopies[0].getType("Bug");
	IMethod method= type.getMethods()[0];
	search(method, REFERENCES, SearchPattern.R_ERASURE_MATCH);
	assertSearchResults(
		"src/b97087/Bug.java b97087.Foo() [Foo] EXACT_MATCH\n" + 
		"src/b97087/Bug.java b97087.Bar() [super()] ERASURE_MATCH"
	);
}

/**
 * Bug 97120: 
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=97120"
 */
public void testBug97120() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", getExternalJCLPathString("1.5"), "java.lang", "Throwable.class").getType();
	IJavaSearchScope scope = SearchEngine.createHierarchyScope(type);
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine().searchAllTypeNames(
		null,
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
 * Bug 97322: [search] Search for method references sometimes reports potential match with differing argument count
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=97322"
 */
public void testBug97322() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b97322/Test.java",
		"package b97322;\n" + 
		"class Test {\n" + 
		"	static void myMethod(int a, String b) {}\n" + 
		"	void call() {\n" + 
		"		myMethod(12);\n" + 
		"	}\n" + 
		"}"
	);
	IType type = workingCopies[0].getType("Test");
	IMethod method= type.getMethods()[0];
	search(method, REFERENCES);
	assertSearchResults(""); // Expect no result
}

/**
 * Bug 97547: [search] Package search does not find references in member types import clause
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=97547"
 */
public void testBug97547() throws CoreException {
	workingCopies = new ICompilationUnit[2];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b97547/IX.java",
		"package b97547;\n" + 
		"public interface IX {\n" + 
		"	public interface IX1 {}\n" + 
		"}"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b97547/X.java",
		"package b97547;\n" + 
		"import b97547.IX.*;\n" + 
		"class X {\n" + 
		"	IX x;\n" + 
		"}"
	);
	IPackageDeclaration[] packages = workingCopies[0].getPackageDeclarations();
	assertTrue("Invalid number of packages declaration!", packages!=null && packages.length==1);
	search(packages[0], REFERENCES);
	assertSearchResults(
		"src/b97547/X.java [b97547] EXACT_MATCH"
	);
}

/**
 * Bug 97606: [1.5][search] Raw type reference is reported as exact match for qualified names
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=97606"
 */
public void testBug97606() throws CoreException {
	workingCopies = new ICompilationUnit[4];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/def/L.java",
		"package b97606.pack.def;\n" + 
		"public interface L<E> {}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/def/LL.java",
		"package b97606.pack.def;\n" + 
		"public class LL<E> implements L<E> {\n" + 
		"	public Object clone() {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n"
	);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/ref/K.java",
		"package b97606.pack.ref;\n" + 
		"public interface K {}\n"
	);
	workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/ref/X.java",
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
	IPath pathDef = new Path("/JavaSearchBugs/src/b97606/pack/def");
	IPath pathRef = new Path("/JavaSearchBugs/src/b97606/pack/ref");
	try {
		createFolder(pathDef);
		createFolder(pathRef);
		workingCopies[0].commitWorkingCopy(true, null);
		workingCopies[1].commitWorkingCopy(true, null);
		workingCopies[2].commitWorkingCopy(true, null);
		workingCopies[3].commitWorkingCopy(true, null);
		this.resultCollector.showRule = true;
		IType type = workingCopies[0].getType("L");
		search(type, REFERENCES, SearchPattern.R_ERASURE_MATCH);
		assertSearchResults(
			"src/b97606/pack/def/LL.java b97606.pack.def.LL [L] ERASURE_MATCH\n" + 
			"src/b97606/pack/ref/X.java b97606.pack.def.L<K> b97606.pack.ref.X.getSG() [b97606.pack.def.L] ERASURE_MATCH\n" + 
			"src/b97606/pack/ref/X.java b97606.pack.def.L<K> b97606.pack.ref.X.getSG() [b97606.pack.def.L] ERASURE_RAW_MATCH"
		);
	}
	finally {
		deleteFolder(pathDef);
		deleteFolder(pathRef);
	}
}
public void testBug97606b() throws CoreException {
	workingCopies = new ICompilationUnit[4];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/def/L.java",
		"package b97606.pack.def;\n" + 
		"public interface L<E> {}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/def/LL.java",
		"package b97606.pack.def;\n" + 
		"public class LL<E> implements L<E> {\n" + 
		"	public Object clone() {\n" + 
		"		return null;\n" + 
		"	}\n" + 
		"}\n"
	);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/ref/K.java",
		"package b97606.pack.ref;\n" + 
		"public interface K {}\n"
	);
	workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/ref/X.java",
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
	IPath pathDef = new Path("/JavaSearchBugs/src/b97606/pack/def");
	IPath pathRef = new Path("/JavaSearchBugs/src/b97606/pack/ref");
	try {
		createFolder(pathDef);
		createFolder(pathRef);
		workingCopies[0].commitWorkingCopy(true, null);
		workingCopies[1].commitWorkingCopy(true, null);
		workingCopies[2].commitWorkingCopy(true, null);
		workingCopies[3].commitWorkingCopy(true, null);
		this.resultCollector.showRule = true;
		IType type = workingCopies[0].getType("L");
		search(type, REFERENCES, SearchPattern.R_ERASURE_MATCH);
		assertSearchResults(
			"src/b97606/pack/def/LL.java b97606.pack.def.LL [L] ERASURE_MATCH\n" + 
			"src/b97606/pack/ref/X.java L<K> b97606.pack.ref.X.getSG() [L] ERASURE_MATCH\n" + 
			"src/b97606/pack/ref/X.java L<K> b97606.pack.ref.X.getSG() [L] ERASURE_RAW_MATCH"
		);
	}
	finally {
		deleteFolder(pathDef);
		deleteFolder(pathRef);
	}
}

/**
 * Bug 97614: [1.5][search] Refactoring: renaming of field of a (complex) parametrized type does not replace all occurrences
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=97614"
 */
public void testBug97614() throws CoreException {
	workingCopies = new ICompilationUnit[3];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b97614/W.java",
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
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b97614/X.java",
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
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b97614/Y.java",
		"package b97614;\n" + 
		"public class Y<T extends X<T, U, V>, U extends X.XX<T, V>, V extends X.XY> {\n" + 
		"}\n"
	);
	IField field = workingCopies[0].getType("W").getField("m1");
	search(field, REFERENCES);
	assertSearchResults(
		"src/b97614/W.java void b97614.W.getStore(Object) [m1] EXACT_MATCH"
	);
}

/**
 * Bug 98378: [search] does not find method references in anonymous class of imported jarred plugin
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=98378"
 */
public void testBug98378() throws CoreException {
	workingCopies = new ICompilationUnit[2];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b98378/X.java",
		"package b98378;\n" + 
		"public class  X implements java.lang.CharSequence {\n" + 
		"	public int length() {\n" + 
		"		return 1;\n" + 
		"	}\n" + 
		"}"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b98378/Y.java",
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
	workingCopies = new ICompilationUnit[2];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b98378/X.java",
		"package b98378;\n" + 
		"public class  X implements java.lang.CharSequence {\n" + 
		"	public int length() {\n" + 
		"		return 1;\n" + 
		"	}\n" + 
		"}"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b98378/Y.java",
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
 * Bug 99600: [search] Java model exception on "Move to new file" on inner type with inner type
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=99600"
 */
public void testBug99600() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b99600/Test.java",
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
	IType type = workingCopies[0].getType("Test").getType("C2");
	new SearchEngine(this.workingCopies).searchDeclarationsOfSentMessages(type, resultCollector, null);
	assertSearchResults(
		"src/b99600/Test.java int b99600.Test$C2$C3.foo(C1) [foo(C1 c)] EXACT_MATCH"
	);
}

/**
 * Bug 99903: [1.5][search] range wrong for package-info
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=99903"
 */
public void testBug99903_annotation() throws CoreException {
	workingCopies = new ICompilationUnit[3];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b99903/package-info.java",
		"/**\n" + 
		" * @see Test\n" + 
		" */\n" + 
		"@Annot\n" + 
		"package b99903;\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b99903/Test.java",
		"package b99903;\n" + 
		"public class Test {\n" + 
		"}\n"
	);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b99903/Annot.java",
		"package b99903;\n" + 
		"public @interface Annot {\n" + 
		"}\n"
	);
	IType type = workingCopies[2].getType("Annot");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b99903/package-info.java [Annot] EXACT_MATCH"
	);
}
public void testBug99903_javadoc() throws CoreException {
	workingCopies = new ICompilationUnit[3];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b99903/package-info.java",
		"/**\n" + 
		" * @see Test\n" + 
		" */\n" + 
		"@Annot\n" + 
		"package b99903;\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b99903/Test.java",
		"package b99903;\n" + 
		"public class Test {\n" + 
		"}\n"
	);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b99903/Annot.java",
		"package b99903;\n" + 
		"public @interface Annot {\n" + 
		"}\n"
	);
	resultCollector.showInsideDoc = true;
	IType type = workingCopies[1].getType("Test");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b99903/package-info.java [Test] EXACT_MATCH INSIDE_JAVADOC"
	);
}

/**
 * Bug 100695: [1.5][search] Renaming a field of generic array type has no effect
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=100695"
 */
public void testBug100695() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100695/Test.java",
		"package b100695;\n" + 
		"public class Test {\n" + 
		"	Class<Class>[] foo;\n" + 
		"	Class<Class>[] bar = foo;\n" + 
		"	Test() {\n" + 
		"		foo = null;\n" + 
		"	}\n" + 
		"}\n"
	);
	IField field = workingCopies[0].getType("Test").getField("foo");
	search(field, REFERENCES);
	assertSearchResults(
		"src/b100695/Test.java b100695.Test.bar [foo] EXACT_MATCH\n" + 
		"src/b100695/Test.java b100695.Test() [foo] EXACT_MATCH"
	);
}
public void testBug100695a() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100695/Test.java",
		"package b100695;\n" + 
		"public class Test {\n" + 
		"	Class<Class>[] foo;\n" + 
		"	Class<Class>[] bar = foo;\n" + 
		"	Test() {\n" + 
		"		foo = null;\n" + 
		"	}\n" + 
		"}\n"
	);
	IField field = workingCopies[0].getType("Test").getField("foo");
	search(field, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b100695/Test.java b100695.Test.foo [foo] EXACT_MATCH\n" + 
		"src/b100695/Test.java b100695.Test.bar [foo] EXACT_MATCH\n" + 
		"src/b100695/Test.java b100695.Test() [foo] EXACT_MATCH"
	);
}
public void testBug100695b() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100695/Test.java",
		"package b100695;\n" + 
		"public class Test {\n" + 
		"	Class<Class> foo;\n" + 
		"	Class<Class> bar = foo;\n" + 
		"	Test() {\n" + 
		"		foo = null;\n" + 
		"	}\n" + 
		"}\n"
	);
	IField field = workingCopies[0].getType("Test").getField("foo");
	search(field, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b100695/Test.java b100695.Test.foo [foo] EXACT_MATCH\n" + 
		"src/b100695/Test.java b100695.Test.bar [foo] EXACT_MATCH\n" + 
		"src/b100695/Test.java b100695.Test() [foo] EXACT_MATCH"
	);
}
public void testBug100695c() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100695/Test.java",
		"package b100695;\n" + 
		"public class Test {\n" + 
		"	Class[] foo;\n" + 
		"	Class[] bar = foo;\n" + 
		"	Test() {\n" + 
		"		foo = null;\n" + 
		"	}\n" + 
		"}\n"
	);
	IField field = workingCopies[0].getType("Test").getField("foo");
	search(field, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b100695/Test.java b100695.Test.foo [foo] EXACT_MATCH\n" + 
		"src/b100695/Test.java b100695.Test.bar [foo] EXACT_MATCH\n" + 
		"src/b100695/Test.java b100695.Test() [foo] EXACT_MATCH"
	);
}
public void testBug100695d() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100695/Test.java",
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
	IMethod method = workingCopies[0].getType("Test").getMethods()[0];
	search(method, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b100695/Test.java Class<Class>[] b100695.Test.foo(Class<Class>[]) [foo] EXACT_MATCH\n" + 
		"src/b100695/Test.java void b100695.Test.bar() [foo(new Class[0])] EXACT_MATCH"
	);
}
public void testBug100695e() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100695/Test.java",
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
	IMethod method = workingCopies[0].getType("Test").getMethods()[0];
	search(method, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b100695/Test.java Class<Class> b100695.Test.foo(Class<Class>) [foo] EXACT_MATCH\n" + 
		"src/b100695/Test.java void b100695.Test.bar() [foo(null)] EXACT_MATCH"
	);
}
public void testBug100695f() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100695/Test.java",
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
	IMethod method = workingCopies[0].getType("Test").getMethods()[0];
	search(method, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b100695/Test.java Class[] b100695.Test.foo(Class[]) [foo] EXACT_MATCH\n" + 
		"src/b100695/Test.java void b100695.Test.bar() [foo(new Class[0])] EXACT_MATCH"
	);
}

/**
 * Bug 100772: [1.5][search] Search for declarations in hierarchy reports to many matches
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=100772"
 */
private void setUpBug100772_HierarchyScope_ClassAndSubclass() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100772/Test.java",
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
	IType type = workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH\n" + 
		"src/b100772/Test.java void b100772.Sub.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_ClassAndSubclass02() throws CoreException {
	setUpBug100772_HierarchyScope_ClassAndSubclass();
	IType type = workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH\n" + 
		"src/b100772/Test.java void b100772.Sub.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_ClassAndSubclass03() throws CoreException {
	setUpBug100772_HierarchyScope_ClassAndSubclass();
	IType type = workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QClass;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(Class) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_ClassAndSubclass04() throws CoreException {
	setUpBug100772_HierarchyScope_ClassAndSubclass();
	IType type = workingCopies[0].getType("Sub");
	IMethod method = type.getMethod("foo", new String[] { "QString;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH\n" + 
		"src/b100772/Test.java void b100772.Sub.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_ClassAndSubclass05() throws CoreException {
	setUpBug100772_HierarchyScope_ClassAndSubclass();
	IType type = workingCopies[0].getType("Sub");
	IMethod method = type.getMethod("foo", new String[] { "QException;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Sub.foo(Exception) [foo] EXACT_MATCH"
	);
}
private void setUpBug100772_HierarchyScope_InterfacesAndClass() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100772/Test.java",
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
	IType type = workingCopies[0].getType("Test");
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
	IType type = workingCopies[0].getType("Test");
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
	IType type = workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QClass;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(Class) [foo] EXACT_MATCH\n" + 
		"src/b100772/Test.java void b100772.X.foo(Class) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_InterfacesAndClass04() throws CoreException {
	setUpBug100772_HierarchyScope_InterfacesAndClass();
	IType type = workingCopies[0].getType("Sub");
	IMethod method = type.getMethod("foo", new String[] { "QString;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH\n" + 
		"src/b100772/Test.java void b100772.Sub.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_InterfacesAndClass05() throws CoreException {
	setUpBug100772_HierarchyScope_InterfacesAndClass();
	IType type = workingCopies[0].getType("Sub");
	IMethod method = type.getMethod("foo", new String[] { "QException;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Sub.foo(Exception) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_InterfacesAndClass06() throws CoreException {
	setUpBug100772_HierarchyScope_InterfacesAndClass();
	IType type = workingCopies[0].getType("X");
	IMethod method = type.getMethod("foo", new String[] { "QString;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH\n" + 
		"src/b100772/Test.java void b100772.X.foo(String) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_InterfacesAndClass07() throws CoreException {
	setUpBug100772_HierarchyScope_InterfacesAndClass();
	IType type = workingCopies[0].getType("X");
	IMethod method = type.getMethod("foo", new String[] { "QClass;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(Class) [foo] EXACT_MATCH\n" + 
		"src/b100772/Test.java void b100772.X.foo(Class) [foo] EXACT_MATCH"
	);
}
public void testBug100772_HierarchyScope_InterfacesAndClass08() throws CoreException {
	setUpBug100772_HierarchyScope_InterfacesAndClass();
	IType type = workingCopies[0].getType("X");
	IMethod method = type.getMethod("foo", new String[] { "QException;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.X.foo(Exception) [foo] EXACT_MATCH"
	);
}
private void setUpBug100772_HierarchyScope_Complex() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100772/Test.java",
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
	IType type = workingCopies[0].getType("IX");
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
	IType type = workingCopies[0].getType("Z");
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
	IType type = workingCopies[0].getType("Z");
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
	IType type = workingCopies[0].getType("X");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"src/b100772/Test.java void b100772.X.foo(T) [foo] EXACT_MATCH\n" + 
		"src/b100772/Test.java void b100772.XX.foo(String) [foo] EXACT_MATCH\n" + 
		"src/b100772/Test.java void b100772.IX.foo(U) [foo] EXACT_MATCH"
	);
}
private void setUpBug100772_ProjectScope_ClassAndSubclass() throws CoreException {
	workingCopies = new ICompilationUnit[2];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100772/Test.java",
		"package b100772;\n" + 
		"class Test<T> {\n" + 
		"    public void foo(T t) {}\n" + 
		"    public void foo(Class c) {}\n" + 
		"}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b100772/Sub.java",
		"package b100772;\n" + 
		"class Sub extends Test<String> {\n" + 
		"    public void foo(String str) {}\n" + 
		"    public void foo(Exception e) {}\n" + 
		"}\n"
	);
}
public void testBug100772_ProjectScope_ClassAndSubclass01() throws CoreException {
	setUpBug100772_ProjectScope_ClassAndSubclass();
	IType type = workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Sub.java void b100772.Sub.foo(String) [foo] EXACT_MATCH\n" + 
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_ClassAndSubclass02() throws CoreException {
	setUpBug100772_ProjectScope_ClassAndSubclass();
	IType type = workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, DECLARATIONS);
	assertSearchResults(
		"src/b100772/Sub.java void b100772.Sub.foo(String) [foo] EXACT_MATCH\n" + 
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_ClassAndSubclass03() throws CoreException {
	setUpBug100772_ProjectScope_ClassAndSubclass();
	IType type = workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QClass;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(Class) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_ClassAndSubclass04() throws CoreException {
	setUpBug100772_ProjectScope_ClassAndSubclass();
	IType type = workingCopies[1].getType("Sub");
	IMethod method = type.getMethod("foo", new String[] { "QString;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Sub.java void b100772.Sub.foo(String) [foo] EXACT_MATCH\n" + 
		"src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_ClassAndSubclass05() throws CoreException {
	setUpBug100772_ProjectScope_ClassAndSubclass();
	IType type = workingCopies[1].getType("Sub");
	IMethod method = type.getMethod("foo", new String[] { "QException;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Sub.java void b100772.Sub.foo(Exception) [foo] EXACT_MATCH"
	);
}
private void setUpBug100772_ProjectScope_InterfacesAndClass() throws CoreException {
	workingCopies = new ICompilationUnit[3];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100772/Test.java",
		"package b100772;\n" + 
		"interface Test<T> {\n" + 
		"    public void foo(T t);\n" + 
		"    public void foo(Class c);\n" + 
		"}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b100772/Sub.java",
		"package b100772;\n" + 
		"interface Sub extends Test<String> {\n" + 
		"    public void foo(String str);\n" + 
		"    public void foo(Exception e);\n" + 
		"}\n"
	);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b100772/X.java",
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
	IType type = workingCopies[0].getType("Test");
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
	IType type = workingCopies[0].getType("Test");
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
	IType type = workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QClass;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(Class) [foo] EXACT_MATCH\n" + 
		"src/b100772/X.java void b100772.X.foo(Class) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_InterfacesAndClass04() throws CoreException {
	setUpBug100772_ProjectScope_InterfacesAndClass();
	IType type = workingCopies[1].getType("Sub");
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
	IType type = workingCopies[1].getType("Sub");
	IMethod method = type.getMethod("foo", new String[] { "QException;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Sub.java void b100772.Sub.foo(Exception) [foo] EXACT_MATCH\n" + 
		"src/b100772/X.java void b100772.X.foo(Exception) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_InterfacesAndClass06() throws CoreException {
	setUpBug100772_ProjectScope_InterfacesAndClass();
	IType type = workingCopies[2].getType("X");
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
	IType type = workingCopies[2].getType("X");
	IMethod method = type.getMethod("foo", new String[] { "QClass;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Test.java void b100772.Test.foo(Class) [foo] EXACT_MATCH\n" + 
		"src/b100772/X.java void b100772.X.foo(Class) [foo] EXACT_MATCH"
	);
}
public void testBug100772_ProjectScope_InterfacesAndClass08() throws CoreException {
	setUpBug100772_ProjectScope_InterfacesAndClass();
	IType type = workingCopies[2].getType("X");
	IMethod method = type.getMethod("foo", new String[] { "QException;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/Sub.java void b100772.Sub.foo(Exception) [foo] EXACT_MATCH\n" + 
		"src/b100772/X.java void b100772.X.foo(Exception) [foo] EXACT_MATCH"
	);
}
private void setUpBug100772_ProjectScope_Complex() throws CoreException {
	workingCopies = new ICompilationUnit[6];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100772/X.java",
		"package b100772;\n" + 
		"public class X<T> implements IX<T> {\n" + 
		"	public void foo(T t) {}\n" + 
		"}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b100772/XX.java",
		"package b100772;\n" + 
		"class XX extends X<String> {\n" + 
		"	public void foo(String s) {}\n" + 
		"	public void foo(Exception e) {}\n" + 
		"}\n"
	);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b100772/IX.java",
		"package b100772;\n" + 
		"interface IX<U> {\n" + 
		"	public void foo(U u);\n" + 
		"}\n"
	);
	workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b100772/Y.java",
		"package b100772;\n" + 
		"class Y implements IX<String> {\n" + 
		"	public void foo(String s) {}\n" + 
		"	public void foo(Exception e) {}\n" + 
		"}\n"
	);
	workingCopies[4] = getWorkingCopy("/JavaSearchBugs/src/b100772/IXX.java",
		"package b100772;\n" + 
		"interface IXX<V extends Exception> {\n" + 
		"	public void foo(V v);\n" + 
		"}\n"
	);
	workingCopies[5] = getWorkingCopy("/JavaSearchBugs/src/b100772/Z.java",
		"package b100772;\n" + 
		"class Z extends Y implements IXX<Exception> {\n" + 
		"	public void foo(String s) {}\n" + 
		"	public void foo(Exception e) {}\n" + 
		"}\n"
	);
}
public void testBug100772_ProjectScope_Complex01() throws CoreException {
	setUpBug100772_ProjectScope_Complex();
	IType type = workingCopies[2].getType("IX");
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
	IType type = workingCopies[5].getType("Z");
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
	IType type = workingCopies[5].getType("Z");
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
	IType type = workingCopies[0].getType("X");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"src/b100772/X.java void b100772.X.foo(T) [foo] EXACT_MATCH\n" + 
		"src/b100772/IX.java void b100772.IX.foo(U) [foo] EXACT_MATCH\n" + 
		"src/b100772/XX.java void b100772.XX.foo(String) [foo] EXACT_MATCH"
	);
}

/**
 * Bug 108088: [search] Inaccurate search match for method invocations with literal arguments
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=108088"
 */
public void testBug108088() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearchBugs", "src", "b108088", "Test108088.java");
	IType type = unit.getType("A108088");
	IMethod method = type.getMethod("subroutine", new String[] { "F" });
	SearchPattern pattern = SearchPattern.createPattern(method, REFERENCES, EXACT_RULE);
	assertNotNull("Pattern should not be null", pattern);
	search(pattern, getJavaSearchScopeBugs(), resultCollector);
	assertSearchResults(
		"src/b108088/B108088.java void b108088.B108088.doit(A108088, String) [subroutine(1.2f)] EXACT_MATCH"
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
	workingCopies = new ICompilationUnit[5];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110060/AA.java",
		"package b110060;\n" + 
		"public class AA {\n" +
		"}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b110060/AAxx.java",
		"package b110060;\n" + 
		"public class AAxx {\n" +
		"}\n"
	);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b110060/AxxAyy.java",
		"package b110060;\n" + 
		"public class AxxAyy {\n" +
		"}\n"
	);
	workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b110060/AxA.java",
		"package b110060;\n" + 
		"public class AxA {\n" +
		"}\n"
	);
	workingCopies[4] = getWorkingCopy("/JavaSearchBugs/src/b110060/Test.java",
		"package b110060;\n" + 
		"public class Test {\n" +
		"	AA aa;\n" + 
		"	AAxx aaxx;\n" + 
		"	AxA axa;\n" + 
		"	AxxAyy axxayy;\n" + 
		"}\n"
	);
}
public void testBug110060_TypePattern01() throws CoreException {
	setUpBug110060_TypePattern();
	search("AA", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.aa [AA] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.aaxx [AAxx] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axa [AxA] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axxayy [AxxAyy] EXACT_MATCH"
	);
}

public void testBug110060_TypePattern02() throws CoreException {
	setUpBug110060_TypePattern();
	search("AA", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/AA.java b110060.AA [AA] EXACT_MATCH\n" + 
		"src/b110060/AAxx.java b110060.AAxx [AAxx] EXACT_MATCH\n" + 
		"src/b110060/AxA.java b110060.AxA [AxA] EXACT_MATCH\n" + 
		"src/b110060/AxxAyy.java b110060.AxxAyy [AxxAyy] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.aa [AA] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.aaxx [AAxx] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axa [AxA] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axxayy [AxxAyy] EXACT_MATCH"
	);
}

public void testBug110060_TypePattern03() throws CoreException {
	setUpBug110060_TypePattern();
	search("AAx", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/AAxx.java b110060.AAxx [AAxx] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.aaxx [AAxx] EXACT_MATCH"
	);
}

public void testBug110060_TypePattern04() throws CoreException {
	setUpBug110060_TypePattern();
	search("Axx", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/AxxAyy.java b110060.AxxAyy [AxxAyy] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axxayy [AxxAyy] EXACT_MATCH"
	);
}

public void testBug110060_TypePattern05() throws CoreException {
	setUpBug110060_TypePattern();
	search("Ax", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/AxA.java b110060.AxA [AxA] EXACT_MATCH\n" + 
		"src/b110060/AxxAyy.java b110060.AxxAyy [AxxAyy] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axa [AxA] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axxayy [AxxAyy] EXACT_MATCH"
	);
}

public void testBug110060_TypePattern06() throws CoreException {
	setUpBug110060_TypePattern();
	search("A*A*", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/AA.java b110060.AA [AA] EXACT_MATCH\n" + 
		"src/b110060/AAxx.java b110060.AAxx [AAxx] EXACT_MATCH\n" + 
		"src/b110060/AxA.java b110060.AxA [AxA] EXACT_MATCH\n" + 
		"src/b110060/AxxAyy.java b110060.AxxAyy [AxxAyy] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.aa [AA] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.aaxx [AAxx] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axa [AxA] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axxayy [AxxAyy] EXACT_MATCH\n" + 
		""+ getExternalJCLPathString("1.5") + " java.lang.annotation.Annotation EXACT_MATCH"
	);
}

public void testBug110060_TypePattern07() throws CoreException {
	setUpBug110060_TypePattern();
	search("aa", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults("");
}

public void testBug110060_TypePattern08() throws CoreException {
	setUpBug110060_TypePattern();
	search("aa", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_PREFIX_MATCH);
	assertSearchResults(
		"src/b110060/AA.java b110060.AA [AA] EXACT_MATCH\n" + 
		"src/b110060/AAxx.java b110060.AAxx [AAxx] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.aa [AA] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.aaxx [AAxx] EXACT_MATCH"
	);
}

public void testBug110060_TypePattern09() throws CoreException {
	setUpBug110060_TypePattern();
	search("AA", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_CASE_SENSITIVE);
	assertSearchResults(
		"src/b110060/AA.java b110060.AA [AA] EXACT_MATCH\n" + 
		"src/b110060/AAxx.java b110060.AAxx [AAxx] EXACT_MATCH\n" + 
		"src/b110060/AxA.java b110060.AxA [AxA] EXACT_MATCH\n" + 
		"src/b110060/AxxAyy.java b110060.AxxAyy [AxxAyy] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.aa [AA] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.aaxx [AAxx] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axa [AxA] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axxayy [AxxAyy] EXACT_MATCH"
	);
}

public void testBug110060_TypePattern10() throws CoreException {
	setUpBug110060_TypePattern();
	search("AxAx", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults("");
}

public void testBug110060_TypePattern11() throws CoreException {
	setUpBug110060_TypePattern();
	search("AxxA", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/AxxAyy.java b110060.AxxAyy [AxxAyy] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axxayy [AxxAyy] EXACT_MATCH"
	);
}

public void testBug110060_TypePattern12() throws CoreException {
	setUpBug110060_TypePattern();
	search("AxXA", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/AxxAyy.java b110060.AxxAyy [AxxAyy] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axxayy [AxxAyy] EXACT_MATCH"
	);
}

public void testBug110060_AllTypeNames01() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		"AA".toCharArray(),
		SearchPattern.R_CAMELCASE_MATCH,
		TYPE,
		getJavaSearchScopeBugs(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"b110060.AA\n" + 
		"b110060.AAxx\n" + 
		"b110060.AxA\n" + 
		"b110060.AxxAyy",
		requestor);
}

public void testBug110060_AllTypeNames02() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		"AA".toCharArray(),
		SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_PREFIX_MATCH,
		TYPE,
		getJavaSearchScopeBugs(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"b110060.AA\n" + 
		"b110060.AAxx\n" + 
		"b110060.AxA\n" + 
		"b110060.AxxAyy",
		requestor);
}

public void testBug110060_AllTypeNames03() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		"AA".toCharArray(),
		SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_CASE_SENSITIVE,
		TYPE,
		getJavaSearchScopeBugs(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"b110060.AA\n" + 
		"b110060.AAxx\n" + 
		"b110060.AxA\n" + 
		"b110060.AxxAyy",
		requestor);
}

public void testBug110060_AllTypeNames04() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		"AA".toCharArray(),
		SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CASE_SENSITIVE,
		TYPE,
		getJavaSearchScopeBugs(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"b110060.AA\n" + 
		"b110060.AAxx\n" + 
		"b110060.AxA\n" + 
		"b110060.AxxAyy",
		requestor);
}

public void testBug110060_AllTypeNames05() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		"AA".toCharArray(),
		SearchPattern.R_PREFIX_MATCH,
		TYPE,
		getJavaSearchScopeBugs(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"b110060.AA\n" + 
		"b110060.AAxx",
		requestor);
}

public void testBug110060_AllTypeNames06() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		"AA".toCharArray(),
		SearchPattern.R_CASE_SENSITIVE,
		TYPE,
		getJavaSearchScopeBugs(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"b110060.AA",
		requestor);
}

public void testBug110060_AllTypeNames07() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		"AA".toCharArray(),
		SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CASE_SENSITIVE,
		TYPE,
		getJavaSearchScopeBugs(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"b110060.AA\n" + 
		"b110060.AAxx",
		requestor);
}

public void testBug110060_AllTypeNames08() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		"aa".toCharArray(),
		SearchPattern.R_CAMELCASE_MATCH,
		TYPE,
		getJavaSearchScopeBugs(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"b110060.AA\n" + 
		"b110060.AAxx",
		requestor);
}

public void testBug110060_AllTypeNames09() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		"aa".toCharArray(),
		SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_PREFIX_MATCH,
		TYPE,
		getJavaSearchScopeBugs(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"b110060.AA\n" + 
		"b110060.AAxx",
		requestor);
}

public void testBug110060_AllTypeNames10() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		"aa".toCharArray(),
		SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_CASE_SENSITIVE,
		TYPE,
		getJavaSearchScopeBugs(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"",
		requestor);
}

public void testBug110060_AllTypeNames11() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		"aa".toCharArray(),
		SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CASE_SENSITIVE,
		TYPE,
		getJavaSearchScopeBugs(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"",
		requestor);
}

public void testBug110060_AllTypeNames12() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		"aa".toCharArray(),
		SearchPattern.R_PREFIX_MATCH,
		TYPE,
		getJavaSearchScopeBugs(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"b110060.AA\n" + 
		"b110060.AAxx",
		requestor);
}

public void testBug110060_AllTypeNames13() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		"aa".toCharArray(),
		SearchPattern.R_CASE_SENSITIVE,
		TYPE,
		getJavaSearchScopeBugs(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"",
		requestor);
}

public void testBug110060_AllTypeNames14() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
		"aa".toCharArray(),
		SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CASE_SENSITIVE,
		TYPE,
		getJavaSearchScopeBugs(),
		requestor,
		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
		null
	);
	assertSearchResults(
		"Unexpected all type names",
		"",
		requestor);
}

// Constructor search
private void setUpBug110060_ConstructorPattern() throws CoreException {
	workingCopies = new ICompilationUnit[5];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110060/AA.java",
		"package b110060;\n" + 
		"public class AA {\n" +
		"	AA() {}\n" +
		"}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b110060/AAxx.java",
		"package b110060;\n" + 
		"public class AAxx {\n" +
		"	AAxx() {}\n" +
		"}\n"
	);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b110060/AxxAyy.java",
		"package b110060;\n" + 
		"public class AxxAyy {\n" +
		"	AxxAyy() {}\n" +
		"}\n"
	);
	workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b110060/AxA.java",
		"package b110060;\n" + 
		"public class AxA {\n" +
		"	AxA() {}\n" +
		"}\n"
	);
	workingCopies[4] = getWorkingCopy("/JavaSearchBugs/src/b110060/Test.java",
		"package b110060;\n" + 
		"public class Test {\n" +
		"	AA aa = new AA();\n" + 
		"	AAxx aaxx = new AAxx();\n" + 
		"	AxA axa = new AxA();\n" + 
		"	AxxAyy axxayy = new AxxAyy();\n" + 
		"}\n"
	);
}
public void testBug110060_ConstructorPattern01() throws CoreException {
	setUpBug110060_ConstructorPattern();
	search("AA", CONSTRUCTOR, REFERENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java b110060.Test.aa [new AA()] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.aaxx [new AAxx()] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axa [new AxA()] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axxayy [new AxxAyy()] EXACT_MATCH"
	);
}

public void testBug110060_ConstructorPattern02() throws CoreException {
	setUpBug110060_ConstructorPattern();
	search("AA", CONSTRUCTOR, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/AA.java b110060.AA() [AA] EXACT_MATCH\n" + 
		"src/b110060/AAxx.java b110060.AAxx() [AAxx] EXACT_MATCH\n" + 
		"src/b110060/AxA.java b110060.AxA() [AxA] EXACT_MATCH\n" + 
		"src/b110060/AxxAyy.java b110060.AxxAyy() [AxxAyy] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.aa [new AA()] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.aaxx [new AAxx()] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axa [new AxA()] EXACT_MATCH\n" + 
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

public void testBug110060_ConstructorPattern04() throws CoreException {
	setUpBug110060_ConstructorPattern();
	search("Axx", CONSTRUCTOR, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/AxxAyy.java b110060.AxxAyy() [AxxAyy] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axxayy [new AxxAyy()] EXACT_MATCH"
	);
}

public void testBug110060_ConstructorPattern05() throws CoreException {
	setUpBug110060_ConstructorPattern();
	search("Ax", CONSTRUCTOR, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/AxA.java b110060.AxA() [AxA] EXACT_MATCH\n" + 
		"src/b110060/AxxAyy.java b110060.AxxAyy() [AxxAyy] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axa [new AxA()] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axxayy [new AxxAyy()] EXACT_MATCH"
	);
}

public void testBug110060_ConstructorPattern06() throws CoreException {
	setUpBug110060_ConstructorPattern();
	search("A*A*", CONSTRUCTOR, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/AA.java b110060.AA() [AA] EXACT_MATCH\n" + 
		"src/b110060/AAxx.java b110060.AAxx() [AAxx] EXACT_MATCH\n" + 
		"src/b110060/AxA.java b110060.AxA() [AxA] EXACT_MATCH\n" + 
		"src/b110060/AxxAyy.java b110060.AxxAyy() [AxxAyy] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.aa [new AA()] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.aaxx [new AAxx()] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axa [new AxA()] EXACT_MATCH\n" + 
		"src/b110060/Test.java b110060.Test.axxayy [new AxxAyy()] EXACT_MATCH"
	);
}

// Methods search
private void setUpBug110060_MethodPattern() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110060/Test.java",
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

public void testBug110060_MethodPattern02() throws CoreException {
	setUpBug110060_MethodPattern();
	search("AMWD", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
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

public void testBug110060_MethodPattern07() throws CoreException {
	setUpBug110060_MethodPattern();
	search("*Method*With*A*", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
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
	assertSearchResults("");
}

public void testBug110060_MethodPattern09() throws CoreException {
	setUpBug110060_MethodPattern();
	search("aMWOOODASU", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b110060/Test.java void b110060.Test.aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores] EXACT_MATCH\n" + 
		"src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores()] EXACT_MATCH"
	);
}

// Fields search
private void setUpBug110060_FieldPattern() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110060/Test.java",
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

public void testBug110060_FieldPattern02() throws CoreException {
	setUpBug110060_FieldPattern();
	search("afwsd", FIELD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
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

public void testBug110060_FieldPattern04() throws CoreException {
	setUpBug110060_FieldPattern();
	search("aSFWSCD", FIELD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
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

/**
 * @test Bug 110291: [search] BasicSearchEngine return constructor declarations that doesn't exist in source
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=110291"
 */
public void testBug110291() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110291/Test110291XX.java",
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
 * @test Bug 110336: [plan][search] Should optionaly return the local variable for type reference
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=110336"
 */
public void testBug110336a() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
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
	TypeReferencesCollector collector = new TypeReferencesCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScopeBugs(), collector);
	assertSearchResults(
		"src/b110336/Test.java void b110336.Test.method(Class<Test>).TP [Test]\n" + 
		"src/b110336/Test.java void b110336.Test.method(Class<Test>).clazz [Test]\n" + 
		"src/b110336/Test.java void b110336.Test.method(Class<Test>).localVar1 [Test]\n" + 
		"src/b110336/Test.java void b110336.Test.method(Class<Test>).localVar1 [Test]\n" + 
		"src/b110336/Test.java void b110336.Test.method(Class<Test>).localVar2 [Test]\n" + 
		"src/b110336/Test.java void b110336.Test.method(Class<Test>).localVar2 [Test]",
		collector
	);
}
public void testBug110336b() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
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
	TypeReferencesCollector collector = new TypeReferencesCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScopeBugs(), collector);
	assertSearchResults(
		"src/b110336/Test.java void b110336.Test.method1(Test):<anonymous>#1 [Test]\n" + 
		"src/b110336/Test.java void b110336.Test.method1(Test):<anonymous>#1.c [Test]\n" + 
		"src/b110336/Test.java void void b110336.Test.method1(Test):<anonymous>#1.foo().TP [Test]\n" + 
		"src/b110336/Test.java void void b110336.Test.method1(Test):<anonymous>#1.foo().o [Test]\n" + 
		"src/b110336/Test.java void void b110336.Test.method1(Test):<anonymous>#1.foo().o [Test]\n" + 
		"src/b110336/Test.java void b110336.Test.method1(Test).methodParam [Test]\n" + 
		"src/b110336/Test.java void b110336.Test.method1(Test).localVar1 [Test]",
		collector
	);
}
public void testBug110336c() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
		"package b110336;\n" + 
		"public class Test<TP extends X> {\n" + 
		"	X x;\n" + 
		"\n" + 
		"}\n" + 
		"class X {}\n"
	);
	IType type = this.workingCopies[0].getType("X");
	TypeReferencesCollector collector = new TypeReferencesCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScopeBugs(), collector);
	assertSearchResults(
		"src/b110336/Test.java b110336.Test.TP [X]\n" + 
		"src/b110336/Test.java b110336.Test.x [X]",
		collector
	);
}
public void testBug110336d() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
		"package b110336;\n" + 
		"public class Test {\n" + 
		"	Test a1Test = null, b1Test = new Test(), c1Test;\n" + 
		"	Test a2Test = new Test(), b2Test, c2Test = null;\n" + 
		"	Test a3Test, b3Test = null, c3Test = new Test();\n" + 
		"}\n"
	);
	IType type = this.workingCopies[0].getType("Test");
	TypeReferencesCollector collector = new TypeReferencesCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScopeBugs(), collector);
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
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
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
	TypeReferencesCollector collector = new TypeReferencesCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScopeBugs(), collector);
	assertSearchResults(
		"src/b110336/Test.java void b110336.Test.foo().lv1 [Test]+[lv2,lv3]\n" + 
		"src/b110336/Test.java void b110336.Test.foo().lv2 [Test]\n" + 
		"src/b110336/Test.java void b110336.Test.foo().lv4 [Test]+[lv5,lv6]\n" + 
		"src/b110336/Test.java void b110336.Test.foo().lv4 [Test]\n" + 
		"src/b110336/Test.java void b110336.Test.foo().lv7 [Test]+[lv8,lv9]\n" + 
		"src/b110336/Test.java void b110336.Test.foo().lv9 [Test]",
		collector
	);
}
public void testBug110336f() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
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
	TypeReferencesCollector collector = new TypeReferencesCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScopeBugs(), collector);
	assertSearchResults(
		"src/b110336/Test.java void b110336.Test.foo(Test).test1 [Test]\n" + 
		"src/b110336/Test.java void b110336.Test.foo(Test).test2 [Test]\n" + 
		"src/b110336/Test.java void b110336.Test.foo(Test) [Test]\n" + 
		"src/b110336/Test.java void b110336.Test.foo(Test).test4 [Test]\n" + 
		"src/b110336/Test.java void b110336.Test.foo(Test).test3 [Test]",
		collector
	);
}
public void testBug110336g() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
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
	TypeReferencesCollector collector = new TypeReferencesCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScopeBugs(), collector);
	assertSearchResults(
		"src/b110336/Test.java b110336.Test.{}.lv1 [Test]+[lv2,lv3]\n" + 
		"src/b110336/Test.java b110336.Test.{}.lv2 [Test]\n" + 
		"src/b110336/Test.java b110336.Test.{}.lv4 [Test]+[lv5,lv6]\n" + 
		"src/b110336/Test.java b110336.Test.{}.lv4 [Test]\n" + 
		"src/b110336/Test.java b110336.Test.{}.lv7 [Test]+[lv8,lv9]\n" + 
		"src/b110336/Test.java b110336.Test.{}.lv9 [Test]",
		collector
	);
}
public void testBug110336h() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
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
	TypeReferencesCollector collector = new TypeReferencesCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScopeBugs(), collector);
	assertSearchResults(
		"src/b110336/Test.java b110336.Test.static {}.lv1 [Test]+[lv2,lv3]\n" + 
		"src/b110336/Test.java b110336.Test.static {}.lv2 [Test]\n" + 
		"src/b110336/Test.java b110336.Test.static {}.lv4 [Test]+[lv5,lv6]\n" + 
		"src/b110336/Test.java b110336.Test.static {}.lv4 [Test]\n" + 
		"src/b110336/Test.java b110336.Test.static {}.lv7 [Test]+[lv8,lv9]\n" + 
		"src/b110336/Test.java b110336.Test.static {}.lv9 [Test]",
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
		"lib/b110422.jar b110422.TestPrefix [No source] EXACT_MATCH"
	);
}
public void testBug110422b() throws CoreException {
	search("TESTP", TYPE, DECLARATIONS, SearchPattern.R_PREFIX_MATCH);
	assertSearchResults(
		"lib/b110422.jar b110422.TestPrefix [No source] EXACT_MATCH"
	);
}

/**
 * Bug 113671: [search] AIOOBE in SearchEngine#searchAllTypeNames
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
		getJavaSearchScopeBugs(),
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
	workingCopies = new ICompilationUnit[2];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b114539/Foo.java",
		"package b114539;\n" + 
		"public class Foo {\n" + 
		"	int bar=Bar.FOO;\n" + 
		"}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b114539/Bar.java",
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
 * Bug 116459: [search] correct results are missing in java search
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=116459"
 */
public void testBug116459() throws CoreException {
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p1/X.java",
		"package p1;\n" + 
		"class X<T> {\n" + 
		"	X<T> gen;\n" + 
		"	X<String> param;\n" + 
		"	X raw;\n" + 
		"}"
	);
	IType type = workingCopies[0].getType("X");
	this.resultCollector.showRule = true;
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
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b119545/Test.java",
		"package b119545;\n" + 
		"class Test {\n" + 
		"	void foo(Object o1, Object o2){\n" + 
		"		if (o1.equals(o2)) {}\n" + 
		"	}\n" + 
		"}\n"
	);
	IType type = workingCopies[0].getType("Test");
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
	workingCopies = new ICompilationUnit[2];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b120816/Test.java",
		"package b120816;\n" + 
		"public class Test<E> {\n" + 
		"	String foo(E e) { return \"\"; }\n" + 
		"}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b120816/Sub.java",
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
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b120816/Test.java",
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
	workingCopies = new ICompilationUnit[3];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b122442/I.java",
		"package b122442;\n" + 
		"public interface I {}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b122442/II.java",
		"package b122442;\n" + 
		"public interface II extends I {}\n"
	);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b122442/X.java",
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
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b122442/User.java",
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
 * Bug 123679: [search] Field references not found when type is a qualified member type [regression]
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
	workingCopies = new ICompilationUnit[2];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/I122679.java",
		"package pack;\n" + 
		"public interface I123679 {\n" + 
		"}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
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
	IType type = workingCopies[0].getType("I123679");
	search(type, REFERENCES);
	assertSearchResults(
		// import reference is not found because package fragment and CU do not exist on disk
		// So, PackageReferenceLocator.isDeclaringPackageFragment(...) returns false and S.E. misses this match
		"src/test/Test.java test.Test$StaticClass$Member.parent [I123679] EXACT_MATCH\n" + 
		"src/test/Test.java test.Test$StaticClass$Member(Object):<anonymous>#1 [I123679] EXACT_MATCH\n" + 
		"src/test/Test.java test.Test$StaticClass$Member(Object) [I123679] EXACT_MATCH\n" + 
		"src/test/Test.java test.Test$StaticClass$Member(Object) [I123679] EXACT_MATCH\n" + 
		"src/test/Test.java I123679 test.Test$StaticClass$Member.getParent() [I123679] EXACT_MATCH"
	);
}

/**
 * Bug 124469: [search] AIOOBE in PatternLocator when searching for dependency extent from manifest
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
 * Bug 124489: [search] correct results are missing in java search
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=124489"
 */
public void testBug124489() throws CoreException {
	workingCopies = new ICompilationUnit[2];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Foo.java",
		"public class Foo<T> {}"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/Bar.java",
		"public class Bar {\n" + 
		"    Foo<String> f = new Foo<String>();\n" + 
		"    Foo f2 = new Foo();\n" + 
		"}"
	);
	IType type = workingCopies[0].getType("Foo");
	this.resultCollector.showRule = true;
	new SearchEngine(workingCopies).search(
		SearchPattern.createPattern(type, REFERENCES),
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		getJavaSearchScopeBugs(),
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
 * Bug 125178: [search] AIOOBE in PatternLocator when searching for dependency extent from manifest
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=125178"
 */
public void testBug125178() throws CoreException {
	// Need a working copy as anonymous are not indexed...
	ProblemRequestor problemRequestor = new ProblemRequestor();
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b125178/X.java",
		"package b125178;\n" + 
		"import pack.age.Test;\n" + 
		"public class X {\n" + 
		"	public static void main(String[] args) {\n" + 
		"		new Test().foo(100);\n" + 
		"	}\n" + 
		"}\n",
		new WorkingCopyOwner() {},
		problemRequestor
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
 * Bug 126330: Type reference not found in jar file if sources was not already opened
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
 * Bug 127628: [index] CodeAssist doesn't filter deprecated types
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
				results.addElement(new String(CharOperation.concat(packageName, typeName, '.')));
			}
		}
	}
	TypeNameRequestor requestor =  new DeprecatedTypesRequestor();
	new SearchEngine().searchAllTypeNames(
		null,
		null,
		SearchPattern.R_PATTERN_MATCH, // case insensitive
		TYPE,
		getJavaSearchScopeBugs(),
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
 * Bug 128877: [search] reports inexistent IMethod for binary constructor of inner class
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
	workingCopies = new ICompilationUnit[4];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b130390/TZ.java",
		"package b130390;\n" + 
		"public class TZ {\n" +
		"}\n"
	);
	workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b130390/TimeZone.java",
		"package b130390;\n" + 
		"public class TimeZone{\n" +
		"}\n"
	);
	workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b130390/Npe.java",
		"package b130390;\n" + 
		"public class Npe {\n" +
		"}\n"
	);
	workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b130390/NullPointerException.java",
		"package b130390;\n" + 
		"public class NullPointerException {\n" +
		"}\n"
	);
}
public void testBug130390() throws CoreException {
	setUpBug130390();
	search("NuPoEx", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b130390/NullPointerException.java b130390.NullPointerException [NullPointerException] EXACT_MATCH"
	);
}
public void testBug130390b() throws CoreException {
	setUpBug130390();
	search("NPE", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b130390/Npe.java b130390.Npe [Npe] EXACT_MATCH\n" + 
		"src/b130390/NullPointerException.java b130390.NullPointerException [NullPointerException] EXACT_MATCH"
	);
}
public void testBug130390c() throws CoreException {
	setUpBug130390();
	search("NPE", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_CASE_SENSITIVE);
	assertSearchResults(
		"src/b130390/NullPointerException.java b130390.NullPointerException [NullPointerException] EXACT_MATCH"
	);
}
public void testBug130390d() throws CoreException {
	setUpBug130390();
	search("Npe", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b130390/Npe.java b130390.Npe [Npe] EXACT_MATCH"
	);
}
public void testBug130390e() throws CoreException {
	setUpBug130390();
	search("Npe", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_CASE_SENSITIVE);
	assertSearchResults(
		"src/b130390/Npe.java b130390.Npe [Npe] EXACT_MATCH"
	);
}
public void testBug130390f() throws CoreException {
	setUpBug130390();
	search("NullPE", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"src/b130390/NullPointerException.java b130390.NullPointerException [NullPointerException] EXACT_MATCH"
	);
}
public void testBug130390g() throws CoreException {
	setUpBug130390();
	search("TZ", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_CASE_SENSITIVE);
	assertSearchResults(
		"src/b130390/TZ.java b130390.TZ [TZ] EXACT_MATCH\n" + 
		"src/b130390/TimeZone.java b130390.TimeZone [TimeZone] EXACT_MATCH"
	);
}
public void testBug130390h() throws CoreException {
	setUpBug130390();
	search("TiZo", TYPE, DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH | SearchPattern.R_CASE_SENSITIVE);
	assertSearchResults(
		"src/b130390/TimeZone.java b130390.TimeZone [TimeZone] EXACT_MATCH"
	);
}
/**
 * To get these tests search matches in a workspace, do NOT forget to modify files
 * to set them as working copies.
 *
 * @test Bug 137087: Open Type - missing matches when using mixed case pattern
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=137087"
 */
public void testBug137087() throws CoreException {
	// Search CamelCase
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "runtimeEx";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.RuntimeException EXACT_MATCH"
	);
	// Search like UI does (ie. replace it with prefix if it's not a valid CamelCase)
	int validatedRule = SearchPattern.validateMatchRule(pattern, matchRule);
	if (validatedRule != matchRule) {
		matchRule = SearchPattern.R_PREFIX_MATCH;
	}
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.RuntimeException EXACT_MATCH\n" +
		""+ getExternalJCLPathString("1.5") + " java.lang.RuntimeException EXACT_MATCH"
	);
}
public void testBug137087b() throws CoreException {
	// Search CamelCase
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "Runtimeex";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.RuntimeException EXACT_MATCH"
	);
	// Search like UI does (ie. replace it with prefix if it's not a valid CamelCase)
	int validatedRule = SearchPattern.validateMatchRule(pattern, matchRule);
	if (validatedRule != matchRule) {
		matchRule = SearchPattern.R_PREFIX_MATCH;
	}
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.RuntimeException EXACT_MATCH\n" +
		""+ getExternalJCLPathString("1.5") + " java.lang.RuntimeException EXACT_MATCH"
	);
}
public void testBug137087c() throws CoreException {
	// Search CamelCase
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "runtimeexception";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		"" // no match expected as this is not a valid camel case
	);
	// Search like UI does (ie. replace it with prefix if it's not a valid CamelCase)
	int validatedRule = SearchPattern.validateMatchRule(pattern, matchRule);
	if (validatedRule != matchRule) {
		matchRule = SearchPattern.R_PREFIX_MATCH;
	}
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.RuntimeException EXACT_MATCH"
	);
}
public void testBug137087d() throws CoreException {
	// Search CamelCase
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "Runtimexception";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		"" // no match expected as pattern is missing a 'e'
	);
	// Search like UI does (ie. replace it with prefix if it's not a valid CamelCase)
	int validatedRule = SearchPattern.validateMatchRule(pattern, matchRule);
	if (validatedRule != matchRule) {
		matchRule = SearchPattern.R_PREFIX_MATCH;
	}
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		"" // no match expected as pattern is missing a 'e'
	);
}
public void testBug137087e() throws CoreException {
	// Search CamelCase
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "IllegalMSException";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.IllegalMonitorStateException EXACT_MATCH"
	);
	// Search like UI does (ie. replace it with prefix if it's not a valid CamelCase)
	int validatedRule = SearchPattern.validateMatchRule(pattern, matchRule);
	if (validatedRule != matchRule) {
		matchRule = SearchPattern.R_PREFIX_MATCH;
	}
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.IllegalMonitorStateException EXACT_MATCH\n" +
		""+ getExternalJCLPathString("1.5") + " java.lang.IllegalMonitorStateException EXACT_MATCH"
	);
}
public void testBug137087f() throws CoreException {
	// Search CamelCase
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "illegalMsExceptionSException";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		"" // expected no result as uppercase characters in pattern do not match any camelcase ones in existing types
	);
	// Search like UI does (ie. replace it with prefix if it's not a valid CamelCase)
	int validatedRule = SearchPattern.validateMatchRule(pattern, matchRule);
	if (validatedRule != matchRule) {
		matchRule = SearchPattern.R_PREFIX_MATCH;
	}
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		"" // expected no result as uppercase characters in pattern do not match any camelcase ones in existing types
	);
}
public void testBug137087g() throws CoreException {
	// Search CamelCase
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "clonenotsupportedex";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		"" // no match expected as this is not a valid camel case
	);
	// Search like UI does (ie. replace it with prefix if it's not a valid CamelCase)
	int validatedRule = SearchPattern.validateMatchRule(pattern, matchRule);
	if (validatedRule != matchRule) {
		matchRule = SearchPattern.R_PREFIX_MATCH;
	}
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
}
public void testBug137087h() throws CoreException {
	// Search CamelCase
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "CloneNotSupportedEx";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
	// Search like UI does (ie. replace it with prefix if it's not a valid CamelCase)
	int validatedRule = SearchPattern.validateMatchRule(pattern, matchRule);
	if (validatedRule != matchRule) {
		matchRule = SearchPattern.R_PREFIX_MATCH;
	}
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH\n" +
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
}
public void testBug137087i() throws CoreException {
	// Search CamelCase
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "cloneNotsupportedEx";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
	// Search like UI does (ie. replace it with prefix if it's not a valid CamelCase)
	int validatedRule = SearchPattern.validateMatchRule(pattern, matchRule);
	if (validatedRule != matchRule) {
		matchRule = SearchPattern.R_PREFIX_MATCH;
	}
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH\n" +
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
}
public void testBug137087j() throws CoreException {
	// Search CamelCase
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "ClonenotSupportedexc";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
	// Search like UI does (ie. replace it with prefix if it's not a valid CamelCase)
	int validatedRule = SearchPattern.validateMatchRule(pattern, matchRule);
	if (validatedRule != matchRule) {
		matchRule = SearchPattern.R_PREFIX_MATCH;
	}
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH\n" +
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
}
public void testBug137087k() throws CoreException {
	// Search CamelCase
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "cloneNotSupportedExcep";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
	// Search like UI does (ie. replace it with prefix if it's not a valid CamelCase)
	int validatedRule = SearchPattern.validateMatchRule(pattern, matchRule);
	if (validatedRule != matchRule) {
		matchRule = SearchPattern.R_PREFIX_MATCH;
	}
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH\n" +
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
}
public void testBug137087l() throws CoreException {
	// Search CamelCase
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "Clonenotsupportedexception";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
	// Search like UI does (ie. replace it with prefix if it's not a valid CamelCase)
	int validatedRule = SearchPattern.validateMatchRule(pattern, matchRule);
	if (validatedRule != matchRule) {
		matchRule = SearchPattern.R_PREFIX_MATCH;
	}
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH\n" +
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
}
public void testBug137087m() throws CoreException {
	// Search CamelCase
	int matchRule = SearchPattern.R_CAMELCASE_MATCH;
	String pattern = "CloneNotSupportedException";
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
	// Search like UI does (ie. replace it with prefix if it's not a valid CamelCase)
	int validatedRule = SearchPattern.validateMatchRule(pattern, matchRule);
	if (validatedRule != matchRule) {
		matchRule = SearchPattern.R_PREFIX_MATCH;
	}
	search(pattern, TYPE, DECLARATIONS, matchRule);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH\n" +
		""+ getExternalJCLPathString("1.5") + " java.lang.CloneNotSupportedException EXACT_MATCH"
	);
}
/**
 * Bug 137984: [search] Field references not found when type is a qualified member type [regression]
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
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/CW.java",
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
	IField field = workingCopies[0].getType("CW").getField("c3");
	search(field, REFERENCES);
	assertSearchResults(
		"src/CW.java CW(int) [c3] EXACT_MATCH"
	);
}

/**
 * Bug 140156: [1.5][search] Invalid method handle with parameterized parameters when no source is attached
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
 * Bug 148215: [search] correct results are missing in java search
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
		removeLibraryEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/b148215.jar"));
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
		removeLibraryEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/b148215.jar"));
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
		removeLibraryEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/b148215.jar"));
	}
}

/**
 * Bug 156340: [search] searchAllTypeNames return nothing for empty prefix
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=156340"
 */
public void testBug156340() throws CoreException {
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	IPackageFragment fragment = getPackageFragment("JavaSearchBugs", getExternalJCLPathString(), "java.lang");
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
		"java.lang.Class\n" + 
		"java.lang.CloneNotSupportedException\n" + 
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
 * Bug 156177: [1.5][search] interfaces and annotations could be found with only one requets of searchAllTypeName
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=156177"
 */
public void testBug156177() throws CoreException {
	resultCollector.showRule = true;
	workingCopies = new ICompilationUnit[1];
	workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b156177/Test.java",
		"package b156177;\n" + 
		"interface B156177_I {}\n" + 
		"enum B156177_E {}\n" + 
		"@interface B156177_A {}\n" + 
		"public class B156177 {}\n"
	);
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	new SearchEngine(this.workingCopies).searchAllTypeNames(
		null,
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
}