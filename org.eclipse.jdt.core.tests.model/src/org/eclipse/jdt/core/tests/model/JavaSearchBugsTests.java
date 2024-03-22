/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import static org.eclipse.jdt.core.search.IJavaSearchScope.APPLICATION_LIBRARIES;
import static org.eclipse.jdt.core.search.IJavaSearchScope.REFERENCED_PROJECTS;
import static org.eclipse.jdt.core.search.IJavaSearchScope.SOURCES;
import static org.eclipse.jdt.core.search.IJavaSearchScope.SYSTEM_LIBRARIES;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOrdinaryClassFile;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.MethodNameMatch;
import org.eclipse.jdt.core.search.MethodNameMatchRequestor;
import org.eclipse.jdt.core.search.MethodReferenceMatch;
import org.eclipse.jdt.core.search.ReferenceMatch;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchMatch;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.SearchRequestor;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.core.search.TypeReferenceMatch;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.SearchableEnvironment;
import org.eclipse.jdt.internal.core.SourceMethod;
import org.eclipse.jdt.internal.core.TypeParameter;
import org.eclipse.jdt.internal.core.index.DiskIndex;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.search.AbstractSearchScope;
import org.eclipse.jdt.internal.core.search.BasicSearchEngine;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexRequest;
import org.eclipse.jdt.internal.core.search.matching.AndPattern;
import org.eclipse.jdt.internal.core.search.matching.MatchLocator;
import org.eclipse.jdt.internal.core.search.matching.MethodPattern;
import org.eclipse.jdt.internal.core.search.matching.PatternLocator;
import org.eclipse.jdt.internal.core.search.matching.TypeDeclarationPattern;
import org.eclipse.jdt.internal.core.search.matching.TypeReferencePattern;

import junit.framework.Test;

/**
 * Non-regression tests for bugs fixed in Java Search engine.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class JavaSearchBugsTests extends AbstractJavaSearchTests {
	private final static int UI_DECLARATIONS = DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE;

// Debug
static {
//	 org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
//	TESTS_NAMES = new String[] {"testBug324189d"};
}

public JavaSearchBugsTests(String name) {
	super(name);
	this.endChar = "";
}
public static Test suite() {
	return buildModelTestSuite(JavaSearchBugsTests.class, BYTECODE_DECLARATION_ORDER);
}
class TestCollector extends JavaSearchResultCollector {
	public List matches = new ArrayList();
	@Override
	public void acceptSearchMatch(SearchMatch searchMatch) throws CoreException {
		super.acceptSearchMatch(searchMatch);
		this.matches.add(searchMatch);
	}
}
class ReferenceCollector extends JavaSearchResultCollector {
	@Override
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
	@Override
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

@Override
IJavaSearchScope getJavaSearchScope() {
	return SearchEngine.createJavaSearchScope(new IJavaProject[] {getJavaProject("JavaSearchBugs")});
}
IJavaSearchScope getJavaSearchScopeBugs(String packageName, boolean addSubpackages) throws JavaModelException {
	if (packageName == null) return getJavaSearchScope();
	return getJavaSearchPackageScope("JavaSearchBugs", packageName, addSubpackages);
}
@Override
public ICompilationUnit getWorkingCopy(String path, String source) throws JavaModelException {
	if (this.wcOwner == null) {
		this.wcOwner = new WorkingCopyOwner() {};
	}
	return getWorkingCopy(path, source, this.wcOwner);
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.SuiteOfTestCases#setUpSuite()
 */
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();
	JAVA_PROJECT = setUpJavaProject("JavaSearchBugs", "1.5");
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b95152.jar", false);
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b123679.jar", false);
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b140156.jar", false);
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b164791.jar", false);
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b166348.jar", false);

	removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/record_reference_in_nonsource_jar.jar"));
	removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/record_reference_in_source_jar.jar"));
	removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/permit_reference_in_nonsource_jar.jar"));
	removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/permit_reference_in_source_jar.jar"));
	removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/annotation_in_record_jar.jar"));
	removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/annotation_in_record_source_jar.jar"));
}
@Override
public void tearDownSuite() throws Exception {
	deleteProject("JavaSearchBugs");
	super.tearDownSuite();
}
@Override
protected void setUp () throws Exception {
	super.setUp();
	this.resultCollector = new TestCollector();
	this.resultCollector.showAccuracy(true);
}

/**
 * bug 41018: Method reference not found
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=41018"
 */
public void testBug41018() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b41018/A.java",
		"""
			package b41018;
			public class A {
				protected void anotherMethod() {
					methodA(null);
				}
				private Object methodA(ClassB.InnerInterface arg3) {
					return null;
				}
			}
			class ClassB implements InterfaceB {
			}
			interface InterfaceB {
				interface InnerInterface {
				}
			}
			"""
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
		"""
			package p6930;
			public class AllConstructorDeclarations01 {
			  public AllConstructorDeclarations01() {}
			  public AllConstructorDeclarations01(Object o) {}
			  public AllConstructorDeclarations01(Object o, String s) {}
			}
			"""
	);

	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p6930/AllConstructorDeclarations01b.java",
		"""
			package p6930;
			public class AllConstructorDeclarations01b {
			}
			"""
	);

	ConstructorDeclarationsCollector requestor = new ConstructorDeclarationsCollector();
	searchAllConstructorDeclarations("AllConstructorDeclarations", SearchPattern.R_PREFIX_MATCH, requestor);
	assertSearchResults(
		"""
			p6930.AllConstructorDeclarations01#AllConstructorDeclarations01()
			p6930.AllConstructorDeclarations01#AllConstructorDeclarations01(Object o)
			p6930.AllConstructorDeclarations01#AllConstructorDeclarations01(Object o,String s)
			p6930.AllConstructorDeclarations01b#AllConstructorDeclarations01b()*""",
		requestor
	);
}

public void testBug6930_AllConstructorDeclarations02() throws Exception {
	try {
		IJavaProject p = createJavaProject("P", new String[] {}, new String[] {"/P/lib6930.jar"}, "");

		createJar(new String[] {
			"p6930/AllConstructorDeclarations02.java",
			"""
				package p6930;
				public class AllConstructorDeclarations02 {
				  public AllConstructorDeclarations02() {}
				  public AllConstructorDeclarations02(Object o) {}
				  public AllConstructorDeclarations02(Object o, String s) {}
				}""",
			"p6930/AllConstructorDeclarations02b.java",
			"""
				package p6930;
				public class AllConstructorDeclarations02b {
				}"""
		}, p.getProject().getLocation().append("lib6930.jar").toOSString());
		refresh(p);

		ConstructorDeclarationsCollector requestor = new ConstructorDeclarationsCollector();
		searchAllConstructorDeclarations("AllConstructorDeclarations", SearchPattern.R_PREFIX_MATCH, requestor);
		assertSearchResults(
			"""
				p6930.AllConstructorDeclarations02#AllConstructorDeclarations02()
				p6930.AllConstructorDeclarations02#AllConstructorDeclarations02(java.lang.Object o)
				p6930.AllConstructorDeclarations02#AllConstructorDeclarations02(java.lang.Object o,java.lang.String s)
				p6930.AllConstructorDeclarations02b#AllConstructorDeclarations02b()""",
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
				"""
					package p6930;
					public class AllConstructorDeclarations03 {
					  public AllConstructorDeclarations03() {}
					  public AllConstructorDeclarations03(Object o) {}
					  public AllConstructorDeclarations03(Object o, String s) {}
					}""");

		createFile(
				"/P/src/p6930/AllConstructorDeclarations03b.java",
				"""
					package p6930;
					public class AllConstructorDeclarations03b {
					}""");
		refresh(p);

		ConstructorDeclarationsCollector requestor = new ConstructorDeclarationsCollector();
		searchAllConstructorDeclarations("AllConstructorDeclarations", SearchPattern.R_PREFIX_MATCH, requestor);
		assertSearchResults(
			"""
				p6930.AllConstructorDeclarations03#AllConstructorDeclarations03()
				p6930.AllConstructorDeclarations03#AllConstructorDeclarations03(Object o)
				p6930.AllConstructorDeclarations03#AllConstructorDeclarations03(Object o,String s)
				p6930.AllConstructorDeclarations03b#AllConstructorDeclarations03b()*""",
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
				"""
					package p6930;
					public class AllConstructorDeclarations04 {
					  public AllConstructorDeclarations04(java.util.Collection<Object> c) {}
					}"""
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
			"""
				package p6930;
				public class AllConstructorDeclarations05 {
				  public class AllConstructorDeclarations05b {
				    public AllConstructorDeclarations05b(Object o) {}
				  }
				}"""
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
 * bug 70827: [Search] wrong reference match to private method of supertype
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=70827"
 */
public void testBug70827() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b70827/A.java",
		"""
			package b70827;
			class A {
				private void privateMethod() {
				}
			}
			class Second extends A {
				void call() {
					int i= privateMethod();
				}
				int privateMethod() {
					return 1;
				}
			}
			"""
		);
	IType type = this.workingCopies[0].getType("A");
	IMethod method = type.getMethod("privateMethod", new String[] {});
	search(method, REFERENCES);
	assertSearchResults(
		""
	);
}

/**
 * bug 71279: [Search] NPE in TypeReferenceLocator when moving CU with unresolved type reference
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
		"""
			package b71279;
			public class AA {
				Unknown ref;
			}
			"""
		);
	new SearchEngine(this.workingCopies).searchDeclarationsOfReferencedTypes(this.workingCopies[0], result, null);
	assertSearchResults(
		"Starting search...\n" +
		"Done searching.",
		result);
}

/**
 * bug 72866: [search] references to endVisit(MethodInvocation) reports refs to endVisit(SuperMethodInvocation)
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=72866"
 */
public void testBug72866() throws CoreException {
	this.workingCopies = new ICompilationUnit[4];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b72866/A.java",
		"""
			package b72866;
			public abstract class A {
				public abstract void foo(V v);
			}
			""",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b72866/SX.java",
		"""
			package b72866;
			public class SX extends A {
				public void foo(V v) {
				    v.bar(this);
				}
			}
			"""	,
		owner
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b72866/V.java",
		"""
			package b72866;
			public class V {
				void bar(A a) {}
				void bar(X x) {}
				void bar(SX s) {}
			}
			"""	,
		owner
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b72866/X.java",
		"""
			package b72866;
			public class X extends A {
				public void foo(V v) {
				    v.bar(this);
				}
			}
			"""	,
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
 * bug 73112: [Search] SearchEngine doesn't find all fields multiple field declarations
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=73112"
 */
public void testBug73112a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73112/A.java",
		"""
			package b73112;
			public class A {
			    int fieldA73112a = 1, fieldA73112b = new Integer(2).intValue(), fieldA73112c = fieldA73112a + fieldA73112b;
			    int fieldA73112d;
			   \s
			    public void method(){}
			}
			""");
	// search field references to first multiple field
	search("fieldA73112*", FIELD, ALL_OCCURRENCES);
	assertSearchResults(
		"""
			src/b73112/A.java b73112.A.fieldA73112a [fieldA73112a] EXACT_MATCH
			src/b73112/A.java b73112.A.fieldA73112b [fieldA73112b] EXACT_MATCH
			src/b73112/A.java b73112.A.fieldA73112c [fieldA73112c] EXACT_MATCH
			src/b73112/A.java b73112.A.fieldA73112c [fieldA73112a] EXACT_MATCH
			src/b73112/A.java b73112.A.fieldA73112c [fieldA73112b] EXACT_MATCH
			src/b73112/A.java b73112.A.fieldA73112d [fieldA73112d] EXACT_MATCH"""
	);
}
public void testBug73112b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = super.getWorkingCopy("/JavaSearchBugs/src/b73112/B.java",
		"""
			package b73112;
			public class B {
			    int fieldB73112a, fieldB73112b = 10;
			    int fieldB73112c = fieldB73112a + fieldB73112b, fieldB73112d = fieldB73112c + fieldB73112a, fieldB73112e;
			   \s
			    public void method(){}
			}
			""");
	// search field references to first multiple field
	search("fieldB73112*", FIELD, ALL_OCCURRENCES);
	assertSearchResults(
		"""
			src/b73112/B.java b73112.B.fieldB73112a [fieldB73112a] EXACT_MATCH
			src/b73112/B.java b73112.B.fieldB73112b [fieldB73112b] EXACT_MATCH
			src/b73112/B.java b73112.B.fieldB73112c [fieldB73112c] EXACT_MATCH
			src/b73112/B.java b73112.B.fieldB73112c [fieldB73112a] EXACT_MATCH
			src/b73112/B.java b73112.B.fieldB73112c [fieldB73112b] EXACT_MATCH
			src/b73112/B.java b73112.B.fieldB73112d [fieldB73112d] EXACT_MATCH
			src/b73112/B.java b73112.B.fieldB73112d [fieldB73112c] EXACT_MATCH
			src/b73112/B.java b73112.B.fieldB73112d [fieldB73112a] EXACT_MATCH
			src/b73112/B.java b73112.B.fieldB73112e [fieldB73112e] EXACT_MATCH"""
	);
}

/**
 * bug 73336: [1.5][search] Search Engine does not find type references of actual generic type parameters
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
		"""
			package b73336;
			public class B extends X<A, A> {
				<T> void foo(T t) {}
			}
			""",
		owner
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b73336/C.java",
		"""
			package b73336;
			public class C implements I<A> {
				public void foo() {
					B b = new B();
					b.<A>foo(new A());
				}
			}
			""",
		owner
	);
	this.workingCopies[4] = getWorkingCopy("/JavaSearchBugs/src/b73336/I.java",
		"""
			package b73336;
			public interface I<T>  {
				public void foo();
			}
			""",
		owner
	);
	this.workingCopies[5] = getWorkingCopy("/JavaSearchBugs/src/b73336/X.java",
		"""
			package b73336;
			public class X<T, U> {
				<V> void foo(V v) {}
				class Member<T> {
					void foo() {}
				}
			}
			""",
		owner
	);
	// search for first and second method should both return 2 inaccurate matches
	IType type = this.workingCopies[0].getType("A");
	search(type, REFERENCES); //, getJavaSearchScopeBugs("b73336", false));
	assertSearchResults(
		"""
			src/b73336/AA.java b73336.AA [A] EXACT_MATCH
			src/b73336/B.java b73336.B [A] EXACT_MATCH
			src/b73336/B.java b73336.B [A] EXACT_MATCH
			src/b73336/C.java b73336.C [A] EXACT_MATCH
			src/b73336/C.java void b73336.C.foo() [A] EXACT_MATCH
			src/b73336/C.java void b73336.C.foo() [A] EXACT_MATCH"""
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
		"""
			package b73336b;
			public class B extends X<A, A> {
			}
			""",
		owner
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b73336b/C.java",
		"""
			package b73336b;
			public class C extends X<A, A>.Member<A> {
				public C() {
					new X<A, A>().super();
				}
			}
			""",
		owner
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b73336b/X.java",
		"""
			package b73336b;
			public class X<T, U> {
				<V> void foo(V v) {}
				class Member<T> {
					void foo() {}
				}
			}
			""",
		owner
	);
	// search for first and second method should both return 2 inaccurate matches
	IType type = this.workingCopies[0].getType("A");
//	search(type, REFERENCES, getJavaSearchScopeBugs("b73336b", false));
	search(type, REFERENCES); //, getJavaSearchScopeBugs("b73336", false));
	assertSearchResults(
		"""
			src/b73336b/B.java b73336b.B [A] EXACT_MATCH
			src/b73336b/B.java b73336b.B [A] EXACT_MATCH
			src/b73336b/C.java b73336b.C [A] EXACT_MATCH
			src/b73336b/C.java b73336b.C [A] EXACT_MATCH
			src/b73336b/C.java b73336b.C [A] EXACT_MATCH
			src/b73336b/C.java b73336b.C() [A] EXACT_MATCH
			src/b73336b/C.java b73336b.C() [A] EXACT_MATCH"""
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
		"""
			package b73336c;
			public class B extends X<A, A> {
			}
			""",
		owner
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b73336c/C.java",
		"""
			package b73336c;
			public class C implements X<A, A>.Interface<A>  {
				void bar() {}
			}
			""",
		owner
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b73336c/X.java",
		"""
			package b73336c;
			public class X<T, U> {
				interface Interface<V> {
					void bar();
				}
			}
			""",
		owner
	);
	// search for first and second method should both return 2 inaccurate matches
	IType type = this.workingCopies[0].getType("A");
//	search(type, REFERENCES, getJavaSearchScopeBugs("b73336c", false));
	search(type, REFERENCES); //, getJavaSearchScopeBugs("b73336", false));
	assertSearchResults(
		"""
			src/b73336c/B.java b73336c.B [A] EXACT_MATCH
			src/b73336c/B.java b73336c.B [A] EXACT_MATCH
			src/b73336c/C.java b73336c.C [A] EXACT_MATCH
			src/b73336c/C.java b73336c.C [A] EXACT_MATCH
			src/b73336c/C.java b73336c.C [A] EXACT_MATCH"""
	);
}

/**
 * bug 73696: searching only works for IJavaSearchConstants.TYPE, but not CLASS or INTERFACE
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=73696"
 */
public void testBug73696() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b73696/C.java",
		"""
			package b73696;
			public class C implements  I {
			}""",
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
 * bug 74776: [Search] Wrong search results for almost identical method
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=74776"
 */
public void testBug74776() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b74776/A.java",
		"""
			package b74776;
			public class A {
				/**
				 * @deprecated Use {@link #foo(IRegion)} instead
				 * @param r
				 */
				void foo(Region r) {
					foo((IRegion)r);
				}
				void foo(IRegion r) {
				}
			}
			""",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b74776/IRegion.java",
		"""
			package b74776;
			public interface IRegion {
			}
			""",
		owner
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b74776/Region.java",
		"""
			package b74776;
			public class Region implements IRegion {
			
			}
			""",
		owner
	);
	// search method references
	IType type = this.workingCopies[0].getType("A");
	IMethod method = type.getMethod("foo", new String[] { "QRegion;" });
	search(method, REFERENCES);
	assertSearchResults("");
}

/**
 * bug 75816: [search] correct results are missing in java search
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
 * bug 77093: [search] No references found to method with member type argument
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=77093"
 */
private void setUpBug77093() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b77093/X.java",
		"""
			package b77093;
			public class X {
				class Z {
				}
				Z[][] z_arrays;
				X() {
					this(new Z[10][]);
				}
				X(Z[][] arrays) {
					z_arrays = arrays;
				}
				private void foo(Z[] args) {
				}
				void bar() {
					for (int i=0; i<z_arrays.length; i++)
						foo(z_arrays[i]);
				}
			}"""
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
		"""
			src/b77093/X.java b77093.X.z_arrays [z_arrays] EXACT_MATCH
			src/b77093/X.java b77093.X(Z[][]) [z_arrays] EXACT_MATCH
			src/b77093/X.java void b77093.X.bar() [z_arrays] EXACT_MATCH
			src/b77093/X.java void b77093.X.bar() [z_arrays] EXACT_MATCH"""
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
 * bug 77388: [compiler] Reference to constructor includes space after closing parenthesis
 */
public void testBug77388() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b77388/Test.java",
		"""
			package b77388;
			class Test {
				Test(int a, int b) {	}
				void take(Test mc) { }
				void run() {
					take( new Test(1, 2) ); // space in ") )" is in match
				}
			}""");
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("Test", new String[] {"I", "I"});
	// Search for constructor references
	search(method, REFERENCES);
	assertSearchResults(
		"src/b77388/Test.java void b77388.Test.run() [new Test(1, 2)] EXACT_MATCH"
	);
}

/**
 * bug 78082: [1.5][search] FieldReferenceMatch in static import should not include qualifier
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=78082"
 */
public void testBug78082() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b78082/M.java",
		"""
			package b78082;
			public class M {
				static int VAL=78082;
			}
			""",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b78082/XY.java",
		"""
			package b78082;
			import static b78082.M.VAL;
			public class XY {
				double val = VAL;
				double val2= b78082.M.VAL;
			}
			""",
		owner
	);
	// search field references
	IType type = this.workingCopies[0].getType("M");
	IField field = type.getField("VAL");
	search(field, ALL_OCCURRENCES);
	assertSearchResults(
		"""
			src/b78082/M.java b78082.M.VAL [VAL] EXACT_MATCH
			src/b78082/XY.java [VAL] EXACT_MATCH
			src/b78082/XY.java b78082.XY.val [VAL] EXACT_MATCH
			src/b78082/XY.java b78082.XY.val2 [VAL] EXACT_MATCH"""
	);
}

/**
 * bug 79267: [search] Refactoring of static generic member fails partially
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=79267"
 */
public void testBug79267() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79267/Test.java",
		"""
			package b79267;
			public class Test {
				private static final X<String, String> BEFORE	= new X<String, String>(4);
			
				static {
					BEFORE.put("key1","value1");
					BEFORE.put("key2","value2");
				}
			\t
				private static final X<Y, Object>	objectToPrimitiveMap	= new X<Y, Object>(8);
			
				static {
					objectToPrimitiveMap.put(new Y<Object>(new Object()), new Object());
				}
			}
			
			class X<T, U> {
				X(int x) {}
				void put(T t, U u) {}
			}
			
			class Y<T> {
				Y(T t) {}
			}
			""");
	// search field references
	IType type = this.workingCopies[0].getType("Test");
	IField field = type.getField("BEFORE");
	search(field, REFERENCES);
	field = type.getField("objectToPrimitiveMap");
	search(field, REFERENCES);
	assertSearchResults(
		"""
			src/b79267/Test.java b79267.Test.static {} [BEFORE] EXACT_MATCH
			src/b79267/Test.java b79267.Test.static {} [BEFORE] EXACT_MATCH
			src/b79267/Test.java b79267.Test.static {} [objectToPrimitiveMap] EXACT_MATCH"""
	);
}

/**
 * bug 79378: [search] IOOBE when inlining a method
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=79378"
 */
public void testBug79378() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79378/A.java",
		"""
			package b79378;
			public class Test {
				void foo79378(String s, RuntimeException[] exceptions) {}
				void foo79378(RuntimeException[] exceptions) {}
				void call() {
					String s= null;\s
					Exception[] exceptions= null;
					foo79378(s, exceptions);
				}
			}
			"""
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
		"""
			package b79378;
			public class Test {
				void foo79378(String s, RuntimeException[] exceptions) {}
				void foo79378(RuntimeException[] exceptions) {}
				void call() {
					String s= null;\s
					Exception[] exceptions= null;
					foo79378(s, exceptions);
				}
			}
			"""
	);
	IMethod[] methods = this.workingCopies[0].getType("Test").getMethods();
	assertEquals("Invalid number of methods", 3, methods.length);
	search(methods[1], REFERENCES);
	assertSearchResults("");
}

/**
 * bug 79803: [1.5][search] Search for references to type A reports match for type variable A
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=79803"
 */
public void testBug79803() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79803/A.java",
		"""
			package b79803;
			class A<A> {
			    A a;
			    b79803.A pa= new b79803.A();
			}
			"""
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
		"""
			package b79803;
			class A<A> {
			    A a;
			    b79803.A pa= new b79803.A();
			}
			"""
	);
	search("A", TYPE, REFERENCES);
	assertSearchResults(
		"""
			src/b79803/A.java b79803.A.a [A] EXACT_MATCH
			src/b79803/A.java b79803.A.pa [A] EXACT_MATCH
			src/b79803/A.java b79803.A.pa [A] EXACT_MATCH"""
	);
}

/**
 * bug 79860: [1.5][search] Search doesn't find type reference in type parameter bound
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=79860"
 */
public void testBug79860() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79860/X.java",
		"""
			package b79860;
			public class X<T extends A> { }
			class A { }""",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b79860/Y.java",
		"""
			package b79860;
			public class Y<T extends B&I1&I2&I3> { }
			class B { }
			interface I1 {}
			interface I2 {}
			interface I3 {}
			""",
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
		"""
			package b79860;
			public class X<T extends A> { }
			class A { }""",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b79860/Y.java",
		"""
			package b79860;
			public class Y<T extends B&I1&I2&I3> { }
			class B { }
			interface I1 {}
			interface I2 {}
			interface I3 {}
			""",
		owner
	);
	search("I?", TYPE, REFERENCES);
	assertSearchResults(
		"""
			src/b79860/Y.java b79860.Y [I1] EXACT_MATCH
			src/b79860/Y.java b79860.Y [I2] EXACT_MATCH
			src/b79860/Y.java b79860.Y [I3] EXACT_MATCH"""
	);
}

/**
 * bug 79990: [1.5][search] Search doesn't find type reference in type parameter bound
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=79990"
 */
private void setUpBug79990() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b79990/Test.java",
		"""
			package b79990;
			class Test<T> {
			    public void first(Exception num) {}
			    public void second(T t) {}
			}
			class Sub extends Test<Exception> {
			    public void first(Exception num) {}
			    public void second(Exception t) {}
			}
			"""
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
		"""
			package b79990;
			public class Test<T> {
				void methodT(T t) {}
			}
			class Sub<X> extends Test<X> {
				void methodT(X x) {} // overrides Super#methodT(T)
			}
			"""
	);
	IMethod method = this.workingCopies[0].getType("Test").getMethods()[0];
	search(method, DECLARATIONS);
	assertSearchResults(
		"src/b79990/Test.java void b79990.Test.methodT(T) [methodT] EXACT_MATCH\n" +
		"src/b79990/Test.java void b79990.Sub.methodT(X) [methodT] EXACT_MATCH"
	);
}

/**
 * bug 80084: [1.5][search]Rename field fails on field based on parameterized type with member type parameter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=80084"
 */
public void testBug80084() throws CoreException, JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80084/Test.java",
		"""
			package b80084;
			class List<T> {}
			public class Test {
			  void foo(List<Exception> le) {}
			  void bar() {
			    List<Exception> le = new List<Exception>();
			    foo(le);
			  }
			}
			"""
		);
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QList<QException;>;" } );
	search(method, REFERENCES);
	assertSearchResults(
		"src/b80084/Test.java void b80084.Test.bar() [foo(le)] EXACT_MATCH"
	);
}

/**
 * bug 80194: [1.5][search]Rename field fails on field based on parameterized type with member type parameter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=80194"
 */
private void setUpBug80194() throws CoreException, JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80194/Test.java",
		"""
			package b80194;
			interface Map<K, V> {}
			class HashMap<K, V> implements Map {}
			public class Test {
				void callDoSomething() {
					final Map<String, Object> map = new HashMap<String, Object>();
					doSomething(map);
					doSomething(map, true);
					doSomething(true);
				}
				void doSomething(final Map<String, Object> map) {}
				void doSomething(final Map<String, Object> map, final boolean flag) {}
				void doSomething(final boolean flag) {}
			}
			"""
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
		"""
			src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(map)] EXACT_MATCH
			src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(true)] EXACT_MATCH
			src/b80194/Test.java void b80194.Test.doSomething(boolean) [doSomething] EXACT_MATCH"""
	);
}
public void testBug80194string2() throws CoreException, JavaModelException {
	setUpBug80194();
	search("doSomething(Map<String,Object>)", METHOD, ALL_OCCURRENCES);
	assertSearchResults(
		"""
			src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(map)] EXACT_MATCH
			src/b80194/Test.java void b80194.Test.callDoSomething() [doSomething(true)] EXACT_MATCH
			src/b80194/Test.java void b80194.Test.doSomething(Map<String,Object>) [doSomething] EXACT_MATCH"""
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
 * bug 80223: [search] Declaration search doesn't consider visibility to determine overriding methods
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=80223"
 */
public void testBug80223() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80223/a/A.java",
		"""
			package b80223.a;
			public class A {
			    void m() {}
			}""",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b80223/b/B.java",
		"""
			package b80223.b;
			public class B extends b80223.a.A {
			    void m() {}
			}""",
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
 * bug 80264: [search] Search for method declarations in workspace, disregarding declaring type
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=80264"
 *
 * Following tests also verify
 * bug 87778: [search] doesn't find all declarations of method with covariant return type
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=87778"
 */
// Methods
private void setUpBug80264_Methods() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80264/Methods.java",
		"""
			package b80264;
			class Methods {
			    Methods stable() { return null; }
			    Methods covariant() { return null; }
			}
			class MethodsSub extends Methods {
			    Methods stable() { return null; }
			    MethodsSub covariant() { return null; }
			}
			class MethodsOther {
			    Methods stable() { return null; }
			    Methods covariant() { return null; }
			}
			"""
	);
}
public void testBug80264_Methods() throws CoreException {
	setUpBug80264_Methods();
	IType type = this.workingCopies[0].getType("Methods");
	IMethod[] methods = type.getMethods();
	search(methods[0], DECLARATIONS);
	search(methods[1], DECLARATIONS);
	assertSearchResults(
		"""
			src/b80264/Methods.java Methods b80264.Methods.stable() [stable] EXACT_MATCH
			src/b80264/Methods.java Methods b80264.MethodsSub.stable() [stable] EXACT_MATCH
			src/b80264/Methods.java Methods b80264.Methods.covariant() [covariant] EXACT_MATCH
			src/b80264/Methods.java MethodsSub b80264.MethodsSub.covariant() [covariant] EXACT_MATCH"""
	);
}
public void testBug80264_MethodsIgnoreDeclaringType() throws CoreException, JavaModelException {
	setUpBug80264_Methods();
	IType type = this.workingCopies[0].getType("Methods");
	IMethod[] methods = type.getMethods();
	search(methods[0], DECLARATIONS|IGNORE_DECLARING_TYPE);
	search(methods[1], DECLARATIONS|IGNORE_DECLARING_TYPE);
	assertSearchResults(
		"""
			src/b80264/Methods.java Methods b80264.Methods.stable() [stable] EXACT_MATCH
			src/b80264/Methods.java Methods b80264.MethodsSub.stable() [stable] EXACT_MATCH
			src/b80264/Methods.java Methods b80264.MethodsOther.stable() [stable] EXACT_MATCH
			src/b80264/Methods.java Methods b80264.Methods.covariant() [covariant] EXACT_MATCH
			src/b80264/Methods.java Methods b80264.MethodsOther.covariant() [covariant] EXACT_MATCH"""
	);
}
public void testBug80264_MethodsIgnoreReturnType() throws CoreException, JavaModelException {
	setUpBug80264_Methods();
	IType type = this.workingCopies[0].getType("Methods");
	IMethod[] methods = type.getMethods();
	search(methods[0], DECLARATIONS|IGNORE_RETURN_TYPE);
	search(methods[1], DECLARATIONS|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"""
			src/b80264/Methods.java Methods b80264.Methods.stable() [stable] EXACT_MATCH
			src/b80264/Methods.java Methods b80264.MethodsSub.stable() [stable] EXACT_MATCH
			src/b80264/Methods.java Methods b80264.Methods.covariant() [covariant] EXACT_MATCH
			src/b80264/Methods.java MethodsSub b80264.MethodsSub.covariant() [covariant] EXACT_MATCH"""
	);
}
public void testBug80264_MethodsIgnoreBothTypes() throws CoreException, JavaModelException {
	setUpBug80264_Methods();
	IType type = this.workingCopies[0].getType("Methods");
	IMethod[] methods = type.getMethods();
	search(methods[0], DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE);
	search(methods[1], DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"""
			src/b80264/Methods.java Methods b80264.Methods.stable() [stable] EXACT_MATCH
			src/b80264/Methods.java Methods b80264.MethodsSub.stable() [stable] EXACT_MATCH
			src/b80264/Methods.java Methods b80264.MethodsOther.stable() [stable] EXACT_MATCH
			src/b80264/Methods.java Methods b80264.Methods.covariant() [covariant] EXACT_MATCH
			src/b80264/Methods.java MethodsSub b80264.MethodsSub.covariant() [covariant] EXACT_MATCH
			src/b80264/Methods.java Methods b80264.MethodsOther.covariant() [covariant] EXACT_MATCH"""
	);
}
// Classes
private void setUpBug80264_Classes() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80264/Classes.java",
		"""
			package b80264;
			class Classes {
			    class Inner {}
			}
			class ClassesSub extends Classes {
			    class Inner {}
			}
			class ClassesOther {
			    class Inner {}
			}
			"""
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
		"""
			src/b80264/Classes.java b80264.Classes$Inner [Inner] EXACT_MATCH
			src/b80264/Classes.java b80264.ClassesSub$Inner [Inner] EXACT_MATCH
			src/b80264/Classes.java b80264.ClassesOther$Inner [Inner] EXACT_MATCH"""
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
		"""
			src/b80264/Classes.java b80264.Classes$Inner [Inner] EXACT_MATCH
			src/b80264/Classes.java b80264.ClassesSub$Inner [Inner] EXACT_MATCH
			src/b80264/Classes.java b80264.ClassesOther$Inner [Inner] EXACT_MATCH"""
	);
}
// Fields
private void setUpBug80264_Fields() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80264/Fields.java",
		"""
			package b80264;
			class Fields {
			    Fields field1;
			    Fields field2;
			}
			class FieldsSub extends Fields {
			    Fields field1;
			    FieldsSub field2;
			}
			class FieldsOther {
			    Fields field1;
			    Fields field2;
			}
			"""
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
		"""
			src/b80264/Fields.java b80264.Fields.field1 [field1] EXACT_MATCH
			src/b80264/Fields.java b80264.FieldsSub.field1 [field1] EXACT_MATCH
			src/b80264/Fields.java b80264.FieldsOther.field1 [field1] EXACT_MATCH
			src/b80264/Fields.java b80264.Fields.field2 [field2] EXACT_MATCH
			src/b80264/Fields.java b80264.FieldsOther.field2 [field2] EXACT_MATCH"""
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
		"""
			src/b80264/Fields.java b80264.Fields.field1 [field1] EXACT_MATCH
			src/b80264/Fields.java b80264.FieldsSub.field1 [field1] EXACT_MATCH
			src/b80264/Fields.java b80264.FieldsOther.field1 [field1] EXACT_MATCH
			src/b80264/Fields.java b80264.Fields.field2 [field2] EXACT_MATCH
			src/b80264/Fields.java b80264.FieldsSub.field2 [field2] EXACT_MATCH
			src/b80264/Fields.java b80264.FieldsOther.field2 [field2] EXACT_MATCH"""
	);
}

/**
 * bug 80890: [search] Strange search engine behaviour
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=80890"
 */
public void testBug80890() throws CoreException, JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b80890/A.java",
		"""
			package b80890;
			public class A {
				protected void foo(Exception e) {}
				protected void foo(String s) {}
			}
			class B1 extends A {
				public void bar1() {
					foo(null);
				}
			}
			class B2 extends A {
				public void bar2() {
					foo(null);
				}
			}
			"""
		);
	// search for first and second method should both return 2 inaccurate matches
	IType type = this.workingCopies[0].getType("A");
	IMethod method = type.getMethods()[0];
	search(method, REFERENCES);
	method = type.getMethods()[1];
	search(method, REFERENCES);
	assertSearchResults(
		"""
			src/b80890/A.java void b80890.B1.bar1() [foo(null)] POTENTIAL_MATCH
			src/b80890/A.java void b80890.B2.bar2() [foo(null)] POTENTIAL_MATCH
			src/b80890/A.java void b80890.B1.bar1() [foo(null)] POTENTIAL_MATCH
			src/b80890/A.java void b80890.B2.bar2() [foo(null)] POTENTIAL_MATCH"""
	);
}

/**
 * bug 80918: [1.5][search] ClassCastException when searching for references to binary type
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
 * bug 81084: [1.5][search]Rename field fails on field based on parameterized type with member type parameter
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=81084"
 */
public void testBug81084a() throws CoreException, JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b81084a/Test.java",
		"""
			package b81084a;
			class List<E> {}
			public class Test {
				class Element{}
				static class Inner {
					private final List<Element> fList1;
					private final List<Test.Element> fList2;
					public Inner(List<Element> list) {
						fList1 = list;
						fList2 = list;
					}
				}
			}
			"""
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
		"""
			package b81084a;
			class List<E> {}
			public class Test {
				class Element{}
				static class Inner {
					private final List<Element> fList1;
					private final List<Test.Element> fList2;
					public Inner(List<Element> list) {
						fList1 = list;
						fList2 = list;
					}
				}
			}
			"""
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
		"""
			package b81084b;
			class List<E> {}
			public class Test {
				class Element{}
				static class Inner {
					private final List<? extends Element> fListb1;
					private final List<? extends Test.Element> fListb2;
					public Inner(List<Element> list) {
						fListb1 = list;
						fListb2 = list;
					}
				}
			}
			"""
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
 * bug 81556: [search] correct results are missing in java search
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
 * bug 82088: [search][javadoc] Method parameter types references not found in @see/@link tags
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=82088"
 */
public void testBug82088method() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b82088/m/Test.java",
		"""
			package b82088.m;
			/**
			 * @see #setA(A)
			 */
			public class Test {
				A a;
				public void setA(A a) {
					this.a = a;
				}
			}
			class A {}
			"""
		);
	IType type = this.workingCopies[0].getType("A");
	search(type, REFERENCES);
	assertSearchResults(
		"""
			src/b82088/m/Test.java b82088.m.Test [A] EXACT_MATCH
			src/b82088/m/Test.java b82088.m.Test.a [A] EXACT_MATCH
			src/b82088/m/Test.java void b82088.m.Test.setA(A) [A] EXACT_MATCH"""
	);
}
public void testBug82088constructor() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b82088/c/Test.java",
		"""
			package b82088.c;
			/**
			 * @see #Test(A)
			 */
			public class Test {
				A a;
				Test(A a) {
					this.a = a;
				}
			}
			class A {}
			"""
		);
	IType type = this.workingCopies[0].getType("A");
	search(type, REFERENCES);
	assertSearchResults(
		"""
			src/b82088/c/Test.java b82088.c.Test [A] EXACT_MATCH
			src/b82088/c/Test.java b82088.c.Test.a [A] EXACT_MATCH
			src/b82088/c/Test.java b82088.c.Test(A) [A] EXACT_MATCH"""
	);
}

/**
 * bug 82208: [1.5][search][annot] Search for annotations misses references in default and values constructs
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=82208"
 */
private void setUpBug82208() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b82208/Test.java",
		"""
			package b82208;
			interface B82208_I {}
			enum B82208_E {}
			@interface B82208_A {}
			public class B82208 {}
			"""
	);
}
public void testBug82208_TYPE() throws CoreException {
	this.resultCollector.showRule();
	setUpBug82208();
	search("B82208*", TYPE, ALL_OCCURRENCES);
	assertSearchResults(
		"""
			src/b82208/Test.java b82208.B82208_I [B82208_I] EXACT_MATCH
			src/b82208/Test.java b82208.B82208_E [B82208_E] EXACT_MATCH
			src/b82208/Test.java b82208.B82208_A [B82208_A] EXACT_MATCH
			src/b82208/Test.java b82208.B82208 [B82208] EXACT_MATCH"""
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
 * bug 82673: [1.5][search][annot] Search for annotations misses references in default and values constructs
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83012"
 */
public void testBug82673() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b82673/Test.java",
		"""
			package b82673;
			public class Test {
			    void test1() {
			        class Dummy {};
			        Dummy d = new Dummy();
					new X();
			    }
			   \s
			    void test2() {
			        class Dummy {};
			        Dummy d = new Dummy();
					new Y();
			    }
			}
			class X {}
			class Y {}
			"""
	);
	IType type = selectType(this.workingCopies[0], "Test").getMethod("test1", new String[0]).getType("Dummy", 1);
	search(type, REFERENCES);
	assertSearchResults(
		"src/b82673/Test.java void b82673.Test.test1() [Dummy] EXACT_MATCH\n" +
		"src/b82673/Test.java void b82673.Test.test1() [Dummy] EXACT_MATCH"
	);
}

/**
 * bug 83012: [1.5][search][annot] Search for annotations misses references in default and values constructs
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83012"
 */
public void testBug83012() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83012/Test.java",
		"""
			package b83012;
			@interface A {
			    String value() default "";
			}
			@interface Main {
				A first() default @A("Void");
				A second();
			}
			
			@Main(first=@A(""), second=@A("2"))
			public class Test {
			}
			"""
	);
	IType type = selectType(this.workingCopies[0], "A");
	search(type, REFERENCES);
	assertSearchResults(
		"""
			src/b83012/Test.java A b83012.Main.first() [A] EXACT_MATCH
			src/b83012/Test.java A b83012.Main.first() [A] EXACT_MATCH
			src/b83012/Test.java A b83012.Main.second() [A] EXACT_MATCH
			src/b83012/Test.java b83012.Test [A] EXACT_MATCH
			src/b83012/Test.java b83012.Test [A] EXACT_MATCH"""
	);
}

/**
 * bug 83230: [1.5][search][annot] search for annotation elements does not seem to be implemented yet
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83230"
 */
private void setUpBug83230_Explicit() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83230/Test.java",
		"""
			package b83230;
			@interface Author {
				String[] authorName() default "FREDERIC";
				int[] age();
				int ageMin = 20;
				@interface Surname {}
				class Address {
					String city;
					public void foo(Object obj) {}
				}
			}
			
			@Author(authorName="FREDERIC", age=41)
			public class Test {
				@Author(authorName={"FREDERIC", "JEROME"}, age={41, 35} )
				Test() {}
				@Author(authorName="PHILIPPE", age=37)
				void foo() {
					@Author(authorName="FREDERIC", age=41)
					final Object obj = new Object() {};
					@Author(authorName="FREDERIC", age=41)
					class Local {
						@Author(authorName="FREDERIC", age=41)
						String foo() {
							Author.Address address = new Author.Address();
							address.foo(obj);
							return address.city;
						}
					}
				}
				@Author(authorName="DAVID", age=28)
				int min = Author.ageMin;
			}
			"""
	);
}
public void testBug83230_Explicit() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83230_Explicit();
	IMethod method = selectMethod(this.workingCopies[0], "authorName");
	search(method, REFERENCES);
	assertSearchResults(
		"""
			src/b83230/Test.java b83230.Test [authorName] EXACT_MATCH
			src/b83230/Test.java b83230.Test.min [authorName] EXACT_MATCH
			src/b83230/Test.java b83230.Test() [authorName] EXACT_MATCH
			src/b83230/Test.java void b83230.Test.foo():Local#1 [authorName] EXACT_MATCH
			src/b83230/Test.java String void b83230.Test.foo():Local#1.foo() [authorName] EXACT_MATCH
			src/b83230/Test.java void b83230.Test.foo() [authorName] EXACT_MATCH
			src/b83230/Test.java void b83230.Test.foo() [authorName] EXACT_MATCH"""
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
		"""
			package b83230;
			@interface Annot {
				int value();
			}
			@Annot(41)
			public class Test {
				@Annot(10)
				public void foo() {}
				@Annot(21)
				int bar;
			}
			"""
	);
	IType type = selectType(this.workingCopies[0], "Annot");
	IMethod method = type.getMethod("value", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"""
			src/b83230/Test.java b83230.Test [41] EXACT_MATCH
			src/b83230/Test.java b83230.Test.bar [21] EXACT_MATCH
			src/b83230/Test.java void b83230.Test.foo() [10] EXACT_MATCH"""
	);
}
public void testBug83230_Implicit02() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83230/Test.java",
		"""
			package b83230;
			@interface A {
			    String value() default "";
			}
			@interface Main {
				A first() default @A("Void");
				A second();
			}
			
			@Main(first=@A(""), second=@A("2"))
			public class Test {
			}
			"""
	);
	IType type = selectType(this.workingCopies[0], "A");
	IMethod method = type.getMethod("value", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"""
			src/b83230/Test.java A b83230.Main.first() ["Void"] EXACT_MATCH
			src/b83230/Test.java b83230.Test [""] EXACT_MATCH
			src/b83230/Test.java b83230.Test ["2"] EXACT_MATCH"""
	);
}

/**
 * bug 83304: [search] correct results are missing in java search
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83304"
 */
public void testBug83304() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83304/Test.java",
		"""
			package b83304;
			public class Test {
				void foo() {
					Class<? extends Throwable> l1= null;
					Class<Exception> l2= null;
				\t
					Class<String> string_Class;
				}
			}
			"""
		);
	IType type = selectType(this.workingCopies[0], "Class", 3);
	search(type, REFERENCES, ERASURE_RULE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"""
			src/b83304/Test.java void b83304.Test.foo() [Class] ERASURE_MATCH
			src/b83304/Test.java void b83304.Test.foo() [Class] ERASURE_MATCH
			src/b83304/Test.java void b83304.Test.foo() [Class] EXACT_MATCH"""
	);
}
private void setUpBug83304_TypeParameterizedElementPattern() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83304/Types.java",
		"""
			package b83304;
			import g1.t.s.def.Generic;
			public class Types {
				public Generic gen;
				public Generic<Object> gen_obj;
				public Generic<Exception> gen_exc;
				public Generic<?> gen_wld;
				public Generic<? extends Throwable> gen_thr;
				public Generic<? super RuntimeException> gen_run;
			}
			"""
	);
}
public void testBug83304_TypeParameterizedElementPattern() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83304_TypeParameterizedElementPattern();
	IType type = selectType(this.workingCopies[0], "Generic", 4);
	search(type, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"""
			src/b83304/Types.java [g1.t.s.def.Generic] EQUIVALENT_RAW_MATCH
			src/b83304/Types.java b83304.Types.gen [Generic] EQUIVALENT_RAW_MATCH
			src/b83304/Types.java b83304.Types.gen_obj [Generic] ERASURE_MATCH
			src/b83304/Types.java b83304.Types.gen_exc [Generic] EXACT_MATCH
			src/b83304/Types.java b83304.Types.gen_wld [Generic] EQUIVALENT_MATCH
			src/b83304/Types.java b83304.Types.gen_thr [Generic] EQUIVALENT_MATCH
			src/b83304/Types.java b83304.Types.gen_run [Generic] EQUIVALENT_MATCH
			lib/JavaSearch15.jar g1.t.s.def.Generic<T> g1.t.s.def.Generic.foo() ERASURE_MATCH"""
	);
}
public void testBug83304_TypeGenericElementPattern() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83304_TypeParameterizedElementPattern();
	IType type = getClassFile("JavaSearchBugs", "lib/JavaSearch15.jar", "g1.t.s.def", "Generic.class").getType();
	search(type, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"""
			src/b83304/Types.java [g1.t.s.def.Generic] EQUIVALENT_RAW_MATCH
			src/b83304/Types.java b83304.Types.gen [Generic] ERASURE_RAW_MATCH
			src/b83304/Types.java b83304.Types.gen_obj [Generic] ERASURE_MATCH
			src/b83304/Types.java b83304.Types.gen_exc [Generic] ERASURE_MATCH
			src/b83304/Types.java b83304.Types.gen_wld [Generic] ERASURE_MATCH
			src/b83304/Types.java b83304.Types.gen_thr [Generic] ERASURE_MATCH
			src/b83304/Types.java b83304.Types.gen_run [Generic] ERASURE_MATCH
			lib/JavaSearch15.jar g1.t.s.def.Generic<T> g1.t.s.def.Generic.foo() EXACT_MATCH"""
	);
}
public void testBug83304_TypeStringPattern() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83304_TypeParameterizedElementPattern();
	search("Generic<? super Exception>", TYPE, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"""
			src/b83304/Types.java [Generic] EQUIVALENT_RAW_MATCH
			src/b83304/Types.java b83304.Types.gen [Generic] EQUIVALENT_RAW_MATCH
			src/b83304/Types.java b83304.Types.gen_obj [Generic] EQUIVALENT_MATCH
			src/b83304/Types.java b83304.Types.gen_exc [Generic] EQUIVALENT_MATCH
			src/b83304/Types.java b83304.Types.gen_wld [Generic] EQUIVALENT_MATCH
			src/b83304/Types.java b83304.Types.gen_thr [Generic] ERASURE_MATCH
			src/b83304/Types.java b83304.Types.gen_run [Generic] ERASURE_MATCH
			lib/JavaSearch15.jar g1.t.s.def.Generic<T> g1.t.s.def.Generic.foo() ERASURE_MATCH"""
	);
}
private void setUpBug83304_MethodParameterizedElementPattern() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83304/Methods.java",
		"""
			package b83304;
			import g5.m.def.Single;
			public class Methods {
				void test() {
					Single<Exception> gs = new Single<Exception>();
					Exception exc = new Exception();
					gs.<Throwable>generic(exc);
					gs.<Exception>generic(exc);
					gs.<String>generic("");
				}
			}
			"""
	);
}
public void testBug83304_MethodParameterizedElementPattern() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83304_MethodParameterizedElementPattern();
	IMethod method = selectMethod(this.workingCopies[0], "generic", 2);
	search(method, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"""
			src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] ERASURE_MATCH
			src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] EXACT_MATCH
			src/b83304/Methods.java void b83304.Methods.test() [generic("")] ERASURE_MATCH"""
	);
}
public void testBug83304_MethodGenericElementPattern() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83304_MethodParameterizedElementPattern();
	IType type = getClassFile("JavaSearchBugs", "lib/JavaSearch15.jar", "g5.m.def", "Single.class").getType();
	IMethod method = type.getMethod("generic", new String[] { "TU;" });
	search(method, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"""
			src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] ERASURE_MATCH
			src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] ERASURE_MATCH
			src/b83304/Methods.java void b83304.Methods.test() [generic("")] ERASURE_MATCH"""
	);
}
public void testBug83304_MethodStringPattern() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83304_MethodParameterizedElementPattern();
	search("<Exception>generic", METHOD, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"""
			src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] ERASURE_MATCH
			src/b83304/Methods.java void b83304.Methods.test() [generic(exc)] EXACT_MATCH
			src/b83304/Methods.java void b83304.Methods.test() [generic("")] ERASURE_MATCH"""
	);
}
private void setUpBug83304_ConstructorGenericElementPattern() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83304/Constructors.java",
		"""
			package b83304;
			import g5.c.def.Single;
			public class Constructors {
				void test() {
					Exception exc= new Exception();
					new <Throwable>Single<String>("", exc);
					new <Exception>Single<String>("", exc);
					new <String>Single<String>("", "");
				}
			}
			"""
	);
}
public void testBug83304_ConstructorGenericElementPattern() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83304_ConstructorGenericElementPattern();
	IMethod method = selectMethod(this.workingCopies[0], "Single", 3);
	search(method, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"""
			src/b83304/Constructors.java void b83304.Constructors.test() [new <Throwable>Single<String>("", exc)] ERASURE_MATCH
			src/b83304/Constructors.java void b83304.Constructors.test() [new <Exception>Single<String>("", exc)] EXACT_MATCH
			src/b83304/Constructors.java void b83304.Constructors.test() [new <String>Single<String>("", "")] ERASURE_MATCH"""
	);
}
public void testBug83304_ConstructorParameterizedElementPattern() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83304_ConstructorGenericElementPattern();
	IType type = getClassFile("JavaSearchBugs", "lib/JavaSearch15.jar", "g5.c.def", "Single.class").getType();
	IMethod method = type.getMethod("Single", new String[] { "TT;", "TU;" });
	search(method, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"""
			src/b83304/Constructors.java void b83304.Constructors.test() [new <Throwable>Single<String>("", exc)] ERASURE_MATCH
			src/b83304/Constructors.java void b83304.Constructors.test() [new <Exception>Single<String>("", exc)] ERASURE_MATCH
			src/b83304/Constructors.java void b83304.Constructors.test() [new <String>Single<String>("", "")] ERASURE_MATCH"""
	);
}
public void testBug83304_ConstructorStringPattern() throws CoreException {
	this.resultCollector.showRule();
	setUpBug83304_ConstructorGenericElementPattern();
	search("<Exception>Single", CONSTRUCTOR, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"""
			src/b83304/Constructors.java void b83304.Constructors.test() [new <Throwable>Single<String>("", exc)] ERASURE_MATCH
			src/b83304/Constructors.java void b83304.Constructors.test() [new <Exception>Single<String>("", exc)] EXACT_MATCH
			src/b83304/Constructors.java void b83304.Constructors.test() [new <String>Single<String>("", "")] ERASURE_MATCH
			lib/JavaSearch15.jar g5.m.def.Single<T> g5.m.def.Single.returnParamType() ERASURE_MATCH
			lib/JavaSearch15.jar g5.m.def.Single<T> g5.m.def.Single.complete(U, g5.m.def.Single<T>) ERASURE_MATCH"""
	);
}

/**
 * bug 83804: [1.5][javadoc] Missing Javadoc node for package declaration
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83804"
 */
private void setUpBug83804_Type() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83804/package-info.java",
		"""
			/**
			 * Valid javadoc.
			 * @see Test
			 * @see Unknown
			 * @see Test#foo()
			 * @see Test#unknown()
			 * @see Test#field
			 * @see Test#unknown
			 * @param unexpected
			 * @throws unexpected
			 * @return unexpected\s
			 */
			package b83804;
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b83804/Test.java",
		"""
			/**
			 * Invalid javadoc
			 */
			package b83804;
			public class Test {
				public int field;
				public void foo() {}
			}
			"""
	);
}
public void testBug83804_Type() throws CoreException {
	this.resultCollector.showInsideDoc();
	setUpBug83804_Type();
	IType type = this.workingCopies[1].getType("Test");
	search(type, REFERENCES);
	assertSearchResults(
		"""
			src/b83804/package-info.java [Test] EXACT_MATCH INSIDE_JAVADOC
			src/b83804/package-info.java [Test] EXACT_MATCH INSIDE_JAVADOC
			src/b83804/package-info.java [Test] EXACT_MATCH INSIDE_JAVADOC
			src/b83804/package-info.java [Test] EXACT_MATCH INSIDE_JAVADOC
			src/b83804/package-info.java [Test] EXACT_MATCH INSIDE_JAVADOC"""
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
 * bug 83388: [1.5][search] Search for varargs method not finding match
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83388"
 */
public void testBug83388() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83388/R.java",
		"""
			package b83388;
			import b83388.*;
			public class R {}
			"""
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
		"""
			package b83388;
			import b83388.*;
			public class R {}
			"""
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
 * bug 83693: [search][javadoc] References to methods/constructors: range does not include parameter lists
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83693"
 */
public void testBug83693() throws CoreException {
	this.resultCollector.showRule();
	this.resultCollector.showInsideDoc();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83693/A.java",
		"""
			package b83693;
			import static b83693.A.m;
			/**
			 * @see A#m(int)
			 */
			class A {
			    static void m(int i) {
			        b83693.A.m(i);
			    }
			}"""
	);
	IMethod[] methods = this.workingCopies[0].getType("A").getMethods();
	assertEquals("Invalid number of methods", 1, methods.length);
	search(methods[0], REFERENCES);
	assertSearchResults(
		"""
			src/b83693/A.java [b83693.A.m] EXACT_MATCH OUTSIDE_JAVADOC
			src/b83693/A.java b83693.A [m(int)] EXACT_MATCH INSIDE_JAVADOC
			src/b83693/A.java void b83693.A.m(int) [m(i)] EXACT_MATCH OUTSIDE_JAVADOC"""
	);
}

/**
 * bug 83716: [search] refs to 2-arg constructor on Action found unexpected matches
 *
 * Note that this test does verify that bug is really fixed, but only that it has no impact
 * on existing behavior. It was not really possible to put a test in this suite to verify that
 * bug is effectively fixed as it appears only to potential match found in plugin dependencies...
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=83716"
 */
public void testBug83716() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b83716/X.java",
		"""
			package b83716;
			public class X {
				X() {}
				X(int x) {}
			}
			class Y extends X {
				Y(int y) {
				}
			}"""
	);
	search("X", CONSTRUCTOR, REFERENCES);
	assertSearchResults(
		"src/b83716/X.java b83716.Y(int) [Y] EXACT_MATCH"
	);
}

/**
 * bug 84100: [1.5][search] Search for varargs method not finding match
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=84100"
 */
private void setUpBug84100() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b84100/X.java",
		"""
			package b84100;
			public class X {
				void foo() {}
				void foo(String s) {}
				void foo(String... xs) {}
				void foo(int x, String... xs) {}
				void foo(String s, int x, String... xs) {}
			}
			"""
		);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b84100/Z.java",
		"""
			package b84100;
			public class Z {
				X x;
				void foo() {
					x.foo();
					x.foo("");
					x.foo("", "");
				 	x.foo("", "", null);
					x.foo(3, "", "");
					x.foo("", 3, "", "");
				}
			}
			"""
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
 * bug 84121: [1.5][search][varargs] reference to type reported as inaccurate in vararg
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=84121"
 */
public void testBug84121() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b84121/Test.java",
		"""
			package b84121;
			public class Test {
				void foo(Test... t) {}
				void foo(int x, Test... t) {}
				void foo(Test[] t1, Test... t2) {}
			}
			"""
	);
	IType type = this.workingCopies[0].getType("Test");
	search(type, REFERENCES);
	assertSearchResults(
		"""
			src/b84121/Test.java void b84121.Test.foo(Test ...) [Test] EXACT_MATCH
			src/b84121/Test.java void b84121.Test.foo(int, Test ...) [Test] EXACT_MATCH
			src/b84121/Test.java void b84121.Test.foo(Test[], Test ...) [Test] EXACT_MATCH
			src/b84121/Test.java void b84121.Test.foo(Test[], Test ...) [Test] EXACT_MATCH"""
	);
}

/**
 * bug 84724: [1.5][search] Search for varargs method not finding match
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=84724"
 */
private void setUpBug84724() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b84724/X.java",
		"""
			package b84724;
			public class X {
				X(String s) {}
				X(String... v) {}
				X(int x, String... v) {}
				X(String s, int x, String... v) {}
			}
			"""
		);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b84724/Z.java",
		"""
			package b84724;
			public class Z {
				void foo() {
					new X();
					new X("");
					new X("", "");
					new X("", "", null);
					new X(3, "", "");
					new X("", 3, "", "");
				}
			}
			"""
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
		"""
			src/b84724/Z.java void b84724.Z.foo() [new X()] EXACT_MATCH
			src/b84724/Z.java void b84724.Z.foo() [new X("", "")] EXACT_MATCH
			src/b84724/Z.java void b84724.Z.foo() [new X("", "", null)] EXACT_MATCH"""
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
 * bug 84727: [1.5][search] String pattern search does not work with multiply nested types
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=84727"
 */
private void setUpBug84727() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b84727/A.java",
		"""
			package b84727;
			public interface A {
				Set<Set<Exception>> getXYZ(List<Set<Exception>> arg);
				void getXYZ(String s);
			}
			"""
		);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b84727/X.java",
		"""
			package b84727;
			public class X {
				A a;
				void foo() {
					a.getXYZ(new ArrayList());
					a.getXYZ("");
				}
			}
			"""
		);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b84727/List.java",
		"""
			package b84727;
			public interface List<E> {}
			interface Set<E> {}
			class ArrayList<E> implements List<E> {}"""
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
 * bug 85810: [1.5][search] Missed type parameter reference in implements clause
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=85810"
 */
public void testBug85810() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b85810/Test.java",
		"""
			package b85810;
			public class Test<E> implements In<Test<E>> {
				E e;
			}
			interface In<T> {}
			"""
		);
	ITypeParameter param = selectTypeParameter(this.workingCopies[0], "E");
	search(param, REFERENCES);
	assertSearchResults(
		"src/b85810/Test.java b85810.Test [E] EXACT_MATCH\n" +
		"src/b85810/Test.java b85810.Test.e [E] EXACT_MATCH"
	);
}

/**
 * bug 86596: [search] Search for type finds segments in import
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
		"""
			package b86596.cc;
			import b86596.aa.link.A;
			import b86596.bb.Link;
			public class X {
				A a;
				Link l;
			}
			"""
	);
	search("Link", TYPE, REFERENCES, SearchPattern.R_EXACT_MATCH);
	assertSearchResults(
		"src/b86596/cc/X.java [Link] EXACT_RAW_MATCH\n" +
		"src/b86596/cc/X.java b86596.cc.X.l [Link] EXACT_MATCH"
	);
}

/**
 * bug 86642: [search] no match found of package-visible supertypes in subtypes
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=86642"
 */
public void testBug86642() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b86642/A.java",
		"""
			package b86642;
			class A {
				public void m() {}
				protected void f(A a){}
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b86642/B.java",
		"""
			package b86642;
			public class B extends A{
				protected void f(A a){
					a.m();
				}
			}
			"""
	);
	IType type = this.workingCopies[0].getType("A");
	search(type, REFERENCES);
	assertSearchResults(
		"""
			src/b86642/A.java void b86642.A.f(A) [A] EXACT_MATCH
			src/b86642/B.java b86642.B [A] EXACT_MATCH
			src/b86642/B.java void b86642.B.f(A) [A] EXACT_MATCH"""
	);
}
/**
 * bug 86293: [search] Search for method declaration with pattern "run()" reports match in binary field instead of anonymous class
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
 * bug 86380: [1.5][search][annot] Add support to find references inside annotations on a package declaration
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=86380"
 */
private void setUpBug86380() throws CoreException {
	this.resultCollector.showInsideDoc();
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b86380/package-info.java",
		"""
			/**
			 * @see Annot#field
			 */
			@Annot(value=11)
			package b86380;
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b86380/Test.java",
		"""
			package b86380;
			@Annot(12) public class Test {
				public int field = Annot.field;
				public void foo() {}
			}
			"""
	);
}
public void testBug86380_Type() throws CoreException {
	this.resultCollector.showInsideDoc();
	setUpBug86380();
	ICompilationUnit unit = getCompilationUnit("JavaSearchBugs", "src", "b86380", "Annot.java");
	IType type = unit.getType("Annot");
	search(type, REFERENCES);
	assertSearchResults(
		"""
			src/b86380/Test.java b86380.Test [Annot] EXACT_MATCH OUTSIDE_JAVADOC
			src/b86380/Test.java b86380.Test.field [Annot] EXACT_MATCH OUTSIDE_JAVADOC
			src/b86380/package-info.java [Annot] EXACT_MATCH INSIDE_JAVADOC
			src/b86380/package-info.java [Annot] EXACT_MATCH OUTSIDE_JAVADOC"""
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
 * bug 88174: [1.5][search][annot] Search for annotations misses references in default and values constructs
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=88174"
 */
public void testBug88174() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b88174/Test.java",
		"""
			package b88174;
			public enum Test {
			  a {
			    int getTheValue() { // not found
			      return 0;
			    }
			  };
			  abstract int getTheValue();
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b88174/X.java",
		"""
			package b88174;
			public class X {
			  X x = new X() {
			    int getTheValue() {
			      return 0;
			    }
			  };
			  int getTheValue() { return 0; }
			}
			"""
	);
	IMethod method = this.workingCopies[0].getType("Test").getMethod("getTheValue", new String[0]);
	search(method, DECLARATIONS | IGNORE_DECLARING_TYPE);
	assertSearchResults(
		"""
			src/b88174/Test.java int b88174.Test.a:<anonymous>#1.getTheValue() [getTheValue] EXACT_MATCH
			src/b88174/Test.java int b88174.Test.getTheValue() [getTheValue] EXACT_MATCH
			src/b88174/X.java int b88174.X.x:<anonymous>#1.getTheValue() [getTheValue] EXACT_MATCH
			src/b88174/X.java int b88174.X.getTheValue() [getTheValue] EXACT_MATCH"""
	);
}

/**
 * bug 87627: [search] correct results are missing in java search
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
 * bug 88300: [search] Reference search result is changed by placement of private method
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=88300"
 */
public void testBug88300() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b88300/SubClass.java",
		"""
			package b88300;
			public class SubClass extends SuperClass {
				private void aMethod(String x) {
				}
				public void aMethod(Object x) {
				}
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b88300/SuperClass.java",
		"""
			package b88300;
			public class SuperClass {
			    public void aMethod(Object x) {
			    }
			}
			"""
		);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b88300/User.java",
		"""
			package b88300;
			public class User {
			    public void methodUsingSubClassMethod() {
			        SuperClass user = new SubClass();
			        user.aMethod(new Object());
			    }
			}
			"""
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
		"""
			package b88300;
			public class SubClass extends SuperClass {
				public void aMethod(Object x) {
				}
				private void aMethod(String x) {
				}
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b88300/SuperClass.java",
		"""
			package b88300;
			public class SuperClass {
			    public void aMethod(Object x) {
			    }
			}
			"""
		);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b88300/User.java",
		"""
			package b88300;
			public class User {
			    public void methodUsingSubClassMethod() {
			        SuperClass user = new SubClass();
			        user.aMethod(new Object());
			    }
			}
			"""
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
		"""
			package b88300.not.fixed;
			public class ConditionalFlowInfo extends FlowInfo {
				public FlowInfo info;
				ConditionalFlowInfo(FlowInfo info){
					this.info = info;
				}
				public void markAsDefinitelyNull(FieldBinding field) {
					info.markAsDefinitelyNull(field);
				}
				public void markAsDefinitelyNull(LocalVariableBinding local) {
					info.markAsDefinitelyNull(local);
				}
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b88300/not/fixed/FlowInfo.java",
		"""
			package b88300.not.fixed;
			
			class FieldBinding {
				int id;
			}
			class LocalVariableBinding extends FieldBinding {}
			
			public abstract class FlowInfo {
				abstract public void markAsDefinitelyNull(LocalVariableBinding local);
				abstract public void markAsDefinitelyNull(FieldBinding field);
			}
			"""
		);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b88300/not/fixed/UnconditionalFlowInfo.java",
		"""
			package b88300.not.fixed;
			public class UnconditionalFlowInfo extends FlowInfo {
				final private void markAsDefinitelyNull(int position) {}
				public void markAsDefinitelyNull(FieldBinding field) {
					markAsDefinitelyNull(field.id);
				}
				public void markAsDefinitelyNull(LocalVariableBinding local) {
					markAsDefinitelyNull(local.id + 1);
				}
			}
			"""
		);
	IType type = this.workingCopies[2].getType("UnconditionalFlowInfo");
	search(type.getMethods()[2], REFERENCES);
	assertSearchResults(
		"src/b88300/not/fixed/ConditionalFlowInfo.java void b88300.not.fixed.ConditionalFlowInfo.markAsDefinitelyNull(LocalVariableBinding) [markAsDefinitelyNull(local)] EXACT_MATCH"
	);
}

/**
 * bug 89686: [1.5][search] JavaModelException on ResolvedSourceMethod during refactoring
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=89686"
 */
public void testBug89686() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b89686/A.java",
		"""
			package b89686;
			public enum Color {
			    RED, GREEN(), BLUE(17), PINK((1+(1+1))) {/*anon*/};
			    Color() {}
			    Color(int i) {}
			}"""
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
		"""
			package b89686;
			public enum Color {
			    RED, GREEN(), BLUE(17), PINK((1+(1+1))) {/*anon*/};
			    Color() {}
			    Color(int i) {}
			}"""
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
 * bug 89848: [search] does not find method references in anonymous class of imported jarred plugin
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
 * bug 90779: [search] Constructor Declaration search with ignoring declaring and return type also ignores type name
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=90779"
 */
public void testBug90779() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b90779/A.java",
		"""
			package b90779;
			public class A {
				public A() {}
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b90779/B.java",
		"""
			package b90779;
			public class B {
				public B() {}
			}
			"""
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b90779/C.java",
		"""
			package b90779;
			public class C {
				public C() {}
			}
			"""
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
 * bug 90915: [1.5][search] NPE in PatternLocator
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=90915"
 */
public void testBug90915() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b90915/X.java",
		"""
			package b90915;
			import g5.m.def.Single;
			public class X<T> {
				void foo() {
					Single<String> single = new Single<String>() {
						public <U> String generic(U u) { return ""; }
						public void paramTypesArgs(Single<String> gs) {}
					};
					single.paramTypesArgs(null);
				}
			}
			"""
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
 * bug 91542: [1.5][search] JavaModelException on ResolvedSourceMethod during refactoring
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=91542"
 */
public void testBug91542() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b91542/A.java",
		"""
			package b91542;
			
			class A<T> {
				void a(A<T> a){}
			}
			
			class B<T> extends A<T> {
				protected void foo() {\s
					a(this);
				}
			}"""
	);
	IType type = this.workingCopies[0].getType("B");
	IMethod method = type.getMethod("foo", new String[0]);
	searchDeclarationsOfSentMessages(method, this.resultCollector);
	assertSearchResults(
		"src/b91542/A.java void b91542.A.a(A<T>) [a(A<T> a)] EXACT_MATCH"
	);
}

/**
 * bug 91078: [search] Java search for package reference wrongly identifies inner class as package
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=91078"
 */
public void testBug91078() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b91078/test/Example.java",
		"""
			package b91078.test;
			import b91078.util.HashMap;
			import b91078.util.Map;
			public class Example {
			    public Map.Entry logAll(Object obj) {
			    	if (obj instanceof b91078.util.HashMap) {
			    		HashMap map = (HashMap) obj;
			            return map.entry;
			    	}
			    	if (obj instanceof b91078.util.HashMap.Entry) {
			            Map.Entry entry = (Map.Entry) obj;
			            return entry;
			    	}
			    	return null;
			    }
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b91078/util/HashMap.java",
		"""
			package b91078.util;
			public class HashMap implements Map {
				public Entry entry;
			}
			"""
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b91078/util/Map.java",
		"""
			package b91078.util;
			public interface Map {
				class Entry{}
			}
			"""
	);
	search("*", PACKAGE, REFERENCES, getJavaSearchWorkingCopiesScope(this.workingCopies[0]));
	assertSearchResults(
		"""
			src/b91078/test/Example.java [b91078.util] EXACT_MATCH
			src/b91078/test/Example.java [b91078.util] EXACT_MATCH
			src/b91078/test/Example.java Map.Entry b91078.test.Example.logAll(Object) [b91078.util] EXACT_MATCH
			src/b91078/test/Example.java Map.Entry b91078.test.Example.logAll(Object) [b91078.util] EXACT_MATCH"""
	);
}

/**
 * bug 92264: [search] all types names should support patterns for package/enclosing type name
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
		"""
			java.lang.CharSequence
			java.lang.Class
			java.lang.CloneNotSupportedException
			java.lang.Comparable
			java.lang.Deprecated
			java.lang.Enum
			java.lang.Error
			java.lang.Exception
			java.lang.IllegalMonitorStateException
			java.lang.InterruptedException
			java.lang.Object
			java.lang.RuntimeException
			java.lang.String
			java.lang.Throwable""",
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
		"""
			java.lang.CloneNotSupportedException
			java.lang.Exception
			java.lang.IllegalMonitorStateException
			java.lang.InterruptedException
			java.lang.RuntimeException
			java.lang.annotation.Annotation
			java.lang.annotation.Retention""",
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
		"""
			Test$Inner
			b123679.test.Test
			b123679.test.Test$StaticClass
			b123679.test.Test$StaticClass$Member
			b124645.test.A_124645
			b124645.test.X_124645
			b127628.Test127628$Member127628
			b95794.Test$Color
			pack.age.Test$Member
			test.Test$StaticClass
			test.Test$StaticClass$Member""",
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
 * bug 92944: [1.5][search] SearchEngine#searchAllTypeNames doesn't honor enum or annotation element kind
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=92944"
 */
private void setUpBug92944() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b92944/Test.java",
		"""
			package b92944;
			interface B92944_I {}
			enum B92944_E {}
			@interface B92944_A {}
			public class B92944 {}
			"""
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
		"""
			b92944.B92944
			b92944.B92944_A
			b92944.B92944_E
			b92944.B92944_I""",
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
 * bug 93392: [1.5][search][annot] search for annotation elements does not seem to be implemented yet
 *
 * Note that this test valid also:
 * bug 94062: [1.5][search][annot] search for annotation elements incorrect match range
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=93392"
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=94062"
 */
public void testBug93392() throws CoreException {
	TestCollector collector = new TestCollector();
	collector.showAccuracy(true);
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b93392/Test.java",
		"""
			package b93392;
			@interface Annot {
				int value();
			}
			@Annot(41)
			public class Test {
				@Annot(21)
				int bar;
				@Annot(10)
				public void foo() {}
			}
			"""
	);
	IType type = selectType(this.workingCopies[0], "Annot");
	IMethod method = type.getMethod("value", new String[0]);
	search(method, REFERENCES, getJavaSearchScope(), collector);
	assertSearchResults(
		"""
			src/b93392/Test.java b93392.Test [41] EXACT_MATCH
			src/b93392/Test.java b93392.Test.bar [21] EXACT_MATCH
			src/b93392/Test.java void b93392.Test.foo() [10] EXACT_MATCH""",
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
 * bug 94160: [1.5][search] Generic method in superclass does not exist
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=94160"
 */
public void testBug94160() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b94160/Test.java",
		"""
			package b94160;
			public class Test {
				<T> List<T> generateList(Class<T> clazz) {
					return new ArrayList<T>();
				}
			}
			class CTest extends Test {
				private List<String> myList = generateList(String.class);
				CTest() {
					myList = new ArrayList<String>();
				}
			}
			interface List<E> {}
			class ArrayList<E> implements List<E> {}"""
	);
	IType type = this.workingCopies[0].getType("CTest");
	IField field = type.getField("myList");
	new SearchEngine(this.workingCopies).searchDeclarationsOfSentMessages(field, this.resultCollector, null);
	assertSearchResults(
		"src/b94160/Test.java List<T> b94160.Test.generateList(Class<T>) [generateList(Class<T> clazz)] EXACT_MATCH"
	);
}

/**
 * bug 94389: [search] InvocationTargetException on Rename
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=94389"
 */
public void testBug94389() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b94389/Test.java",
		"""
			package b94389;
			public class Test {
				public void foo() {}
				public void foo() {}
				public void foo() {}
				public void foo() {}
			}
			"""
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
			"""
				src/b94389/Test.java void b94389.Test.foo() [foo]
				src/b94389/Test.java void b94389.Test.foo() [foo]
				src/b94389/Test.java void b94389.Test.foo() [foo]
				src/b94389/Test.java void b94389.Test.foo() [foo]""",
			collector
		);

		// Verify that all matches have correct occurence count
		int size = collector.matches.size();
		for (int i=0; i<size; i++) {
			assertEquals("Invalid foo method occurence count (m="+(m+1)+")", i+1, ((SourceMethod) methods[i]).getOccurrenceCount());
		}
	}
}

/**
 * bug 94718: [1.5][search][annot] Find references in workspace breaks on an annotation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=94718"
 */
public void testBug94718() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b94718/SetUp.java",
		"""
			package b94718;
			public @interface SetUp {
				String value() {}
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b94718/Test.java",
		"""
			package b94718;
			@SetUp("howdy")
			public class Test {
			}
			"""
	);
	IType type = this.workingCopies[1].getType("SetUp");
	search(type, REFERENCES, SearchEngine.createWorkspaceScope());
	assertSearchResults(
		"src/b94718/Test.java b94718.Test [SetUp] EXACT_MATCH"
	);
}

/**
 * bug 95152: [search] Field references not found when type is a qualified member type [regression]
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=95152"
 */
public void testBug95152_jar01() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b95152.jar", "b95152").getOrdinaryClassFile("T1$T12.class").getType();
	// search constructor first level member
	search(type.getMethods()[0], REFERENCES);
	type = getPackageFragment("JavaSearchBugs", "lib/b95152.jar", "b95152").getOrdinaryClassFile("T1$T12$T13.class").getType();
	// search constructor second level member
	search(type.getMethods()[0], REFERENCES);
	assertSearchResults(
		"lib/b95152.jar b95152.T1() EXACT_MATCH\n" +
		"lib/b95152.jar b95152.T1() EXACT_MATCH"
	);
}
public void testBug95152_jar02() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b95152.jar", "b95152").getOrdinaryClassFile("T2$T22.class").getType();
	// search constructor first level member
	search(type.getMethods()[0], REFERENCES);
	type = getPackageFragment("JavaSearchBugs", "lib/b95152.jar", "b95152").getOrdinaryClassFile("T2$T22$T23.class").getType();
	// search constructor second level member
	search(type.getMethods()[0], REFERENCES);
	assertSearchResults(
		"lib/b95152.jar b95152.T2(int) EXACT_MATCH\n" +
		"lib/b95152.jar b95152.T2(int) EXACT_MATCH"
	);
}
public void testBug95152_jar03() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b95152.jar", "b95152").getOrdinaryClassFile("T3$T32.class").getType();
	// search constructor first level member
	search(type.getMethods()[0], REFERENCES);
	type = getPackageFragment("JavaSearchBugs", "lib/b95152.jar", "b95152").getOrdinaryClassFile("T3$T32$T33.class").getType();
	// search constructor second level member
	search(type.getMethods()[0], REFERENCES);
	assertSearchResults(
		"lib/b95152.jar b95152.T3(b95152.T3) EXACT_MATCH\n" +
		"lib/b95152.jar b95152.T3(b95152.T3) EXACT_MATCH"
	);
}
public void testBug95152_jar04() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b95152.jar", "b95152").getOrdinaryClassFile("T4$T42.class").getType();
	// search constructor first level member
	search(type.getMethods()[0], REFERENCES);
	type = getPackageFragment("JavaSearchBugs", "lib/b95152.jar", "b95152").getOrdinaryClassFile("T4$T42$T43.class").getType();
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
		"""
			package b95152;
			public class T {
				T2 c2;
				T2.T3 c3;
				T() {
					c2 = new T2();
					c3 = c2.new T3();
				}
				class T2 {
					T2() {}
					class T3 {
						T3() {}
					}
				}
			}
			"""
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
		"""
			package b95152;
			public class T {
				T2 c2;
				T2.T3 c3;
				T(int c) {
					c2 = new T2(c);
					c3 = c2.new T3(c);
				}
				class T2 {
					T2(int x) {}
					class T3 {
						T3(int x) {}
					}
				}
			}
			"""
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
		"""
			package b95152;
			public class T {
				T2 c2;
				T2.T3 c3;
				T(T c) {
					c2 = new T2(c);
					c3 = c2.new T3(c2);
				}
				class T2 {
					T2(T c) {}
					class T3 {
						T3(T2 c) {}
					}
				}
			}
			"""
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
		"""
			package b95152;
			public class T {
				T2 c2;
				T2.T3 c3;
				T(T c, String str) {
					c2 = new T2(c, str);
					c3 = c2.new T3(c2, str);
				}
				class T2 {
					T2(T c, String str) {}
					class T3 {
						T3(T2 c, String str) {}
					}
				}
			}
			"""
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
 * bug 95794: [1.5][search][annot] Find references in workspace breaks on an annotation
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
		"""
			src/b95794/Test.java [b95794.Test] EXACT_MATCH
			src/b95794/Test.java [b95794.Test] EXACT_MATCH
			src/b95794/Test.java b95794.Test [Test] EXACT_MATCH
			src/b95794/Test.java b95794.Test.there [Test] EXACT_MATCH""",
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
		"""
			src/b95794/Test.java [b95794.Test.Color] EXACT_MATCH
			src/b95794/Test.java [b95794.Test.Color] EXACT_MATCH
			src/b95794/Test.java void b95794.Test.main(String[]) [Color] EXACT_MATCH
			src/b95794/Test.java b95794.Test$Color [Color] EXACT_MATCH""",
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
		"""
			src/b95794/Test.java [WHITE] EXACT_MATCH
			src/b95794/Test.java void b95794.Test.main(String[]) [WHITE] EXACT_MATCH
			src/b95794/Test.java b95794.Test$Color.WHITE [WHITE] EXACT_MATCH""",
		occurencesCollector
	);

	// Verify with references matches
	TestCollector referencesCollector = new TestCollector();
	search(field, REFERENCES, getJavaSearchScope(), referencesCollector);
	assertEquals("Problem with occurences or references number of matches: ", occurencesCollector.matches.size()-1, referencesCollector.matches.size());
}

/**
 * bug 96761: [1.5][search] Search for declarations of generic method finds non-overriding method
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=96761"
 */
public void testBug96761() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b96761/Generic.java",
		"""
			package b96761;
			public class Generic<G> {
				void take(G g) {
				}
			}
			class Impl extends Generic<RuntimeException> {
				void take(InterruptedException g) {
				}
				void take(RuntimeException g) {
				}
			}"""
	);
	IType type = this.workingCopies[0].getType("Generic");
	IMethod method= type.getMethods()[0];
	search(method, REFERENCES);
	assertSearchResults(""); // Expect no result
}

/**
 * bug 96763: [1.5][search] Search for method declarations does not find overridden method with different signature
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=96763"
 */
public void testBug96763() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b96763/Test.java",
		"""
			package b96763;
			class Test<T> {
			    public void first(Exception num) {}
			    public void second(T t) {}
			}
			class Sub extends Test<Exception> {
			    public void first(Exception num) {}
			    public void second(Exception t) {}
			}
			"""
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
		"""
			package b96763;
			class Test<T> {
			    public void first(Exception num) {}
			    public void second(T t) {}
			}
			class Sub extends Test<Exception> {
			    public void first(Exception num) {}
			    public void second(Exception t) {}
			}
			"""
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
		"""
			package b96763;
			public class Test<T> {
				void methodT(T t) {}
			}
			class Sub<X> extends Test<X> {
				void methodT(X x) {} // overrides Super#methodT(T)
			}
			"""
	);
	IMethod method = this.workingCopies[0].getType("Sub").getMethods()[0];
	search(method, DECLARATIONS|IGNORE_DECLARING_TYPE|IGNORE_RETURN_TYPE);
	assertSearchResults(
		"src/b96763/Test.java void b96763.Test.methodT(T) [methodT] EXACT_MATCH\n" +
		"src/b96763/Test.java void b96763.Sub.methodT(X) [methodT] EXACT_MATCH"
	);
}

/**
 * bug 97087: [1.5][search] Can't find reference of generic class's constructor.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=97087"
 */
public void testBug97087() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.resultCollector.showRule();
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b97087/Bug.java",
		"""
			package b97087;
			public class Bug<Type> {
			    Bug(){}
			}
			class Foo extends Bug<String>{
			    Foo(){}
			}
			class Bar extends Bug<Exception>{
			    Bar(){super();}
			}"""
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
 * bug 97120:
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
 * bug 97322: [search] Search for method references sometimes reports potential match with differing argument count
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=97322"
 */
public void testBug97322() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b97322/Test.java",
		"""
			package b97322;
			class Test {
				static void myMethod(int a, String b) {}
				void call() {
					myMethod(12);
				}
			}"""
	);
	IType type = this.workingCopies[0].getType("Test");
	IMethod method= type.getMethods()[0];
	search(method, REFERENCES);
	assertSearchResults(""); // Expect no result
}

/**
 * bug 97547: [search] Package search does not find references in member types import clause
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=97547"
 */
public void testBug97547() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b97547/IX.java",
		"""
			package b97547;
			public interface IX {
				public interface IX1 {}
			}"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b97547/X.java",
		"""
			package b97547;
			import b97547.IX.*;
			class X {
				IX x;
			}"""
	);
	IPackageDeclaration[] packages = this.workingCopies[0].getPackageDeclarations();
	assertTrue("Invalid number of packages declaration!", packages!=null && packages.length==1);
	search(packages[0], REFERENCES);
	assertSearchResults(
		"src/b97547/X.java [b97547] EXACT_MATCH"
	);
}

/**
 * bug 97606: [1.5][search] Raw type reference is reported as exact match for qualified names
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=97606"
 */
public void testBug97606() throws CoreException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/def/L.java",
		"package b97606.pack.def;\n" +
		"public interface L<E> {}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/def/LL.java",
		"""
			package b97606.pack.def;
			public class LL<E> implements L<E> {
				public Object clone() {
					return null;
				}
			}
			"""
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/ref/K.java",
		"package b97606.pack.ref;\n" +
		"public interface K {}\n"
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/ref/X.java",
		"""
			package b97606.pack.ref;
			public class X implements K {
				private b97606.pack.def.LL sg;
				protected synchronized b97606.pack.def.L<K> getSG() {
					return (sg != null)\s
						? (b97606.pack.def.L) sg.clone()
						: null;
				}
			}
			"""
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
			"""
				src/b97606/pack/def/LL.java b97606.pack.def.LL [L] ERASURE_MATCH
				src/b97606/pack/ref/X.java b97606.pack.def.L<K> b97606.pack.ref.X.getSG() [b97606.pack.def.L] ERASURE_MATCH
				src/b97606/pack/ref/X.java b97606.pack.def.L<K> b97606.pack.ref.X.getSG() [b97606.pack.def.L] ERASURE_RAW_MATCH"""
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
		"""
			package b97606.pack.def;
			public class LL<E> implements L<E> {
				public Object clone() {
					return null;
				}
			}
			"""
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/ref/K.java",
		"package b97606.pack.ref;\n" +
		"public interface K {}\n"
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b97606/pack/ref/X.java",
		"""
			package b97606.pack.ref;
			import b97606.pack.def.*;
			public class X implements K {
				private LL sg;
				protected synchronized L<K> getSG() {
					return (sg != null)\s
						? (L) sg.clone()
						: null;
				}
			}
			"""
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
			"""
				src/b97606/pack/def/LL.java b97606.pack.def.LL [L] ERASURE_MATCH
				src/b97606/pack/ref/X.java L<K> b97606.pack.ref.X.getSG() [L] ERASURE_MATCH
				src/b97606/pack/ref/X.java L<K> b97606.pack.ref.X.getSG() [L] ERASURE_RAW_MATCH"""
		);
	}
	finally {
		deleteFolder(rootPath);
	}
}

/**
 * bug 97614: [1.5][search] Refactoring: renaming of field of a (complex) parametrized type does not replace all occurrences
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=97614"
 */
public void testBug97614() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b97614/W.java",
		"""
			package b97614;
			public class W {
				private final Map<String, Y<?, ? extends b97614.X.XX<?, ?>, ? >> m1 = null;     // (a)
				public void getStore(final Object o) {
					m1.get(o);     // (b)
				}
			}
			interface Map<K, V> {
				V get(Object k);
			}"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b97614/X.java",
		"""
			package b97614;
			import java.io.Serializable;
			public interface X<T extends X<T, U, V>,\s
							   U extends X.XX<T, V>,\s
							   V extends X.XY> {
				public interface XX<TT extends X<TT, ?, UU>,\s
				                   UU extends X.XY>\s
						extends	Serializable {
				}
				public interface XY extends Serializable {
				}
			}"""
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b97614/Y.java",
		"""
			package b97614;
			public class Y<T extends X<T, U, V>, U extends X.XX<T, V>, V extends X.XY> {
			}
			"""
	);
	IField field = this.workingCopies[0].getType("W").getField("m1");
	search(field, REFERENCES);
	assertSearchResults(
		"src/b97614/W.java void b97614.W.getStore(Object) [m1] EXACT_MATCH"
	);
}

/**
 * bug 98378: [search] does not find method references in anonymous class of imported jarred plugin
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=98378"
 */
public void testBug98378() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b98378/X.java",
		"""
			package b98378;
			public class  X implements java.lang.CharSequence {
				public int length() {
					return 1;
				}
			}"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b98378/Y.java",
		"""
			package b98378;
			public class Y {
				public int length() {
					return -1;
				}
			}
			"""
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
		"""
			package b98378;
			public class  X implements java.lang.CharSequence {
				public int length() {
					return 1;
				}
			}"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b98378/Y.java",
		"""
			package b98378;
			public class Y {
				public int length() {
					return -1;
				}
			}
			"""
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
 * bug 99600: [search] Java model exception on "Move to new file" on inner type with inner type
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=99600"
 */
public void testBug99600() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b99600/Test.java",
		"""
			package b99600;
			public class Test {
				public class C1 {}
				public class C2 {
					class C3 {
						int foo(C1 c) { return 0; }
					}
					public void foo(C1 c, int i) {
						new C3().foo(c);
					}
				}
			}
			"""
	);
	IType type = this.workingCopies[0].getType("Test").getType("C2");
	new SearchEngine(this.workingCopies).searchDeclarationsOfSentMessages(type, this.resultCollector, null);
	assertSearchResults(
		"src/b99600/Test.java int b99600.Test$C2$C3.foo(C1) [foo(C1 c)] EXACT_MATCH"
	);
}

/**
 * bug 99903: [1.5][search] range wrong for package-info
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=99903"
 */
public void testBug99903_annotation() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b99903/package-info.java",
		"""
			/**
			 * @see Test
			 */
			@Annot
			package b99903;
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b99903/Test.java",
		"""
			package b99903;
			public class Test {
			}
			"""
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b99903/Annot.java",
		"""
			package b99903;
			public @interface Annot {
			}
			"""
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
		"""
			/**
			 * @see Test
			 */
			@Annot
			package b99903;
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b99903/Test.java",
		"""
			package b99903;
			public class Test {
			}
			"""
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b99903/Annot.java",
		"""
			package b99903;
			public @interface Annot {
			}
			"""
	);
	this.resultCollector.showInsideDoc();
	IType type = this.workingCopies[1].getType("Test");
	search(type, REFERENCES);
	assertSearchResults(
		"src/b99903/package-info.java [Test] EXACT_MATCH INSIDE_JAVADOC"
	);
}

/**
 * bug 100695: [1.5][search] Renaming a field of generic array type has no effect
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=100695"
 */
public void testBug100695() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100695/Test.java",
		"""
			package b100695;
			public class Test {
				Class<Class>[] foo;
				Class<Class>[] bar = foo;
				Test() {
					foo = null;
				}
			}
			"""
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
		"""
			package b100695;
			public class Test {
				Class<Class>[] foo;
				Class<Class>[] bar = foo;
				Test() {
					foo = null;
				}
			}
			"""
	);
	IField field = this.workingCopies[0].getType("Test").getField("foo");
	search(field, ALL_OCCURRENCES);
	assertSearchResults(
		"""
			src/b100695/Test.java b100695.Test.foo [foo] EXACT_MATCH
			src/b100695/Test.java b100695.Test.bar [foo] EXACT_MATCH
			src/b100695/Test.java b100695.Test() [foo] EXACT_MATCH"""
	);
}
public void testBug100695b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100695/Test.java",
		"""
			package b100695;
			public class Test {
				Class<Class> foo;
				Class<Class> bar = foo;
				Test() {
					foo = null;
				}
			}
			"""
	);
	IField field = this.workingCopies[0].getType("Test").getField("foo");
	search(field, ALL_OCCURRENCES);
	assertSearchResults(
		"""
			src/b100695/Test.java b100695.Test.foo [foo] EXACT_MATCH
			src/b100695/Test.java b100695.Test.bar [foo] EXACT_MATCH
			src/b100695/Test.java b100695.Test() [foo] EXACT_MATCH"""
	);
}
public void testBug100695c() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100695/Test.java",
		"""
			package b100695;
			public class Test {
				Class[] foo;
				Class[] bar = foo;
				Test() {
					foo = null;
				}
			}
			"""
	);
	IField field = this.workingCopies[0].getType("Test").getField("foo");
	search(field, ALL_OCCURRENCES);
	assertSearchResults(
		"""
			src/b100695/Test.java b100695.Test.foo [foo] EXACT_MATCH
			src/b100695/Test.java b100695.Test.bar [foo] EXACT_MATCH
			src/b100695/Test.java b100695.Test() [foo] EXACT_MATCH"""
	);
}
public void testBug100695d() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100695/Test.java",
		"""
			package b100695;
			public class Test {
				  public Class<Class>[] foo(Class<Class>[] a) {
					  return a;
				  }
				  void bar() {
					  foo(new Class[0]);
				  }
			}
			"""
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
		"""
			package b100695;
			public class Test {
				  public Class<Class> foo(Class<Class> a) {
					  return a;
				  }
				  void bar() {
					  foo(null);
				  }
			}
			"""
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
		"""
			package b100695;
			public class Test {
				  public Class[] foo(Class[] a) {
					  return a;
				  }
				  void bar() {
					  foo(new Class[0]);
				  }
			}
			"""
	);
	IMethod method = this.workingCopies[0].getType("Test").getMethods()[0];
	search(method, ALL_OCCURRENCES);
	assertSearchResults(
		"src/b100695/Test.java Class[] b100695.Test.foo(Class[]) [foo] EXACT_MATCH\n" +
		"src/b100695/Test.java void b100695.Test.bar() [foo(new Class[0])] EXACT_MATCH"
	);
}

/**
 * bug 100772: [1.5][search] Search for declarations in hierarchy reports to many matches
 * @see "http://bugs.eclipse.org/bugs/show_bug.cgi?id=100772"
 */
private void setUpBug100772_HierarchyScope_ClassAndSubclass() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100772/Test.java",
		"""
			package b100772;
			class Test<T> {
			    public void foo(T t) {}
			    public void foo(Class c) {}
			}
			class Sub extends Test<String> {
			    public void foo(String str) {}
			    public void foo(Exception e) {}
			}
			"""
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
		"""
			package b100772;
			interface Test<T> {
			    public void foo(T t);
			    public void foo(Class c);
			}
			interface Sub extends Test<String> {
			    public void foo(String str);
			    public void foo(Exception e);
			}
			class X implements Test<String> {
			    public void foo(String str) {}
			    public void foo(Class c) {}
			    public void foo(Exception e) {}
			}
			"""
	);
}
public void testBug100772_HierarchyScope_InterfacesAndClass01() throws CoreException {
	setUpBug100772_HierarchyScope_InterfacesAndClass();
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"""
			src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH
			src/b100772/Test.java void b100772.Sub.foo(String) [foo] EXACT_MATCH
			src/b100772/Test.java void b100772.X.foo(String) [foo] EXACT_MATCH"""
	);
}
public void testBug100772_HierarchyScope_InterfacesAndClass02() throws CoreException {
	setUpBug100772_HierarchyScope_InterfacesAndClass();
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"""
			src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH
			src/b100772/Test.java void b100772.Sub.foo(String) [foo] EXACT_MATCH
			src/b100772/Test.java void b100772.X.foo(String) [foo] EXACT_MATCH"""
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
		"""
			package b100772;
			public class X<T> implements IX<T> {
				public void foo(T t) {}
			}
			class XX extends X<String> {
				public void foo(String s) {}
				public void foo(Exception e) {}
			}
			interface IX<U> {
				public void foo(U u);
			}
			class Y implements IX<String> {
				public void foo(String s) {}
				public void foo(Exception e) {}
			}
			interface IXX<V extends Exception> {
				public void foo(V v);
			}
			class Z extends Y implements IXX<Exception> {
				public void foo(String s) {}
				public void foo(Exception e) {}
			}
			"""
	);
}
public void testBug100772_HierarchyScope_Complex01() throws CoreException {
	setUpBug100772_HierarchyScope_Complex();
	IType type = this.workingCopies[0].getType("IX");
	IMethod method = type.getMethod("foo", new String[] { "QU;" });
	search(method, DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"""
			src/b100772/Test.java void b100772.X.foo(T) [foo] EXACT_MATCH
			src/b100772/Test.java void b100772.XX.foo(String) [foo] EXACT_MATCH
			src/b100772/Test.java void b100772.IX.foo(U) [foo] EXACT_MATCH
			src/b100772/Test.java void b100772.Y.foo(String) [foo] EXACT_MATCH
			src/b100772/Test.java void b100772.Z.foo(String) [foo] EXACT_MATCH"""
	);
}
public void testBug100772_HierarchyScope_Complex02() throws CoreException {
	setUpBug100772_HierarchyScope_Complex();
	IType type = this.workingCopies[0].getType("Z");
	IMethod method = type.getMethod("foo", new String[] { "QString;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"""
			src/b100772/Test.java void b100772.IX.foo(U) [foo] EXACT_MATCH
			src/b100772/Test.java void b100772.Y.foo(String) [foo] EXACT_MATCH
			src/b100772/Test.java void b100772.Z.foo(String) [foo] EXACT_MATCH"""
	);
}
public void testBug100772_HierarchyScope_Complex03() throws CoreException {
	setUpBug100772_HierarchyScope_Complex();
	IType type = this.workingCopies[0].getType("Z");
	IMethod method = type.getMethod("foo", new String[] { "QException;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"""
			src/b100772/Test.java void b100772.Y.foo(Exception) [foo] EXACT_MATCH
			src/b100772/Test.java void b100772.IXX.foo(V) [foo] EXACT_MATCH
			src/b100772/Test.java void b100772.Z.foo(Exception) [foo] EXACT_MATCH"""
	);
}
public void testBug100772_HierarchyScope_Complex04() throws CoreException {
	setUpBug100772_HierarchyScope_Complex();
	IType type = this.workingCopies[0].getType("X");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, UI_DECLARATIONS, SearchEngine.createHierarchyScope(type));
	assertSearchResults(
		"""
			src/b100772/Test.java void b100772.X.foo(T) [foo] EXACT_MATCH
			src/b100772/Test.java void b100772.XX.foo(String) [foo] EXACT_MATCH
			src/b100772/Test.java void b100772.IX.foo(U) [foo] EXACT_MATCH"""
	);
}
private void setUpBug100772_ProjectScope_ClassAndSubclass() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b100772/Test.java",
		"""
			package b100772;
			class Test<T> {
			    public void foo(T t) {}
			    public void foo(Class c) {}
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b100772/Sub.java",
		"""
			package b100772;
			class Sub extends Test<String> {
			    public void foo(String str) {}
			    public void foo(Exception e) {}
			}
			"""
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
		"""
			package b100772;
			interface Test<T> {
			    public void foo(T t);
			    public void foo(Class c);
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b100772/Sub.java",
		"""
			package b100772;
			interface Sub extends Test<String> {
			    public void foo(String str);
			    public void foo(Exception e);
			}
			"""
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b100772/X.java",
		"""
			package b100772;
			class X implements Test<String> {
			    public void foo(String str) {}
			    public void foo(Class c) {}
			    public void foo(Exception e) {}
			}
			"""
	);
}
public void testBug100772_ProjectScope_InterfacesAndClass01() throws CoreException {
	setUpBug100772_ProjectScope_InterfacesAndClass();
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"""
			src/b100772/Sub.java void b100772.Sub.foo(String) [foo] EXACT_MATCH
			src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH
			src/b100772/X.java void b100772.X.foo(String) [foo] EXACT_MATCH"""
	);
}
public void testBug100772_ProjectScope_InterfacesAndClass02() throws CoreException {
	setUpBug100772_ProjectScope_InterfacesAndClass();
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, DECLARATIONS);
	assertSearchResults(
		"""
			src/b100772/Sub.java void b100772.Sub.foo(String) [foo] EXACT_MATCH
			src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH
			src/b100772/X.java void b100772.X.foo(String) [foo] EXACT_MATCH"""
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
		"""
			src/b100772/Sub.java void b100772.Sub.foo(String) [foo] EXACT_MATCH
			src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH
			src/b100772/X.java void b100772.X.foo(String) [foo] EXACT_MATCH"""
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
		"""
			src/b100772/Sub.java void b100772.Sub.foo(String) [foo] EXACT_MATCH
			src/b100772/Test.java void b100772.Test.foo(T) [foo] EXACT_MATCH
			src/b100772/X.java void b100772.X.foo(String) [foo] EXACT_MATCH"""
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
		"""
			package b100772;
			public class X<T> implements IX<T> {
				public void foo(T t) {}
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b100772/XX.java",
		"""
			package b100772;
			class XX extends X<String> {
				public void foo(String s) {}
				public void foo(Exception e) {}
			}
			"""
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b100772/IX.java",
		"""
			package b100772;
			interface IX<U> {
				public void foo(U u);
			}
			"""
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b100772/Y.java",
		"""
			package b100772;
			class Y implements IX<String> {
				public void foo(String s) {}
				public void foo(Exception e) {}
			}
			"""
	);
	this.workingCopies[4] = getWorkingCopy("/JavaSearchBugs/src/b100772/IXX.java",
		"""
			package b100772;
			interface IXX<V extends Exception> {
				public void foo(V v);
			}
			"""
	);
	this.workingCopies[5] = getWorkingCopy("/JavaSearchBugs/src/b100772/Z.java",
		"""
			package b100772;
			class Z extends Y implements IXX<Exception> {
				public void foo(String s) {}
				public void foo(Exception e) {}
			}
			"""
	);
}
public void testBug100772_ProjectScope_Complex01() throws CoreException {
	setUpBug100772_ProjectScope_Complex();
	IType type = this.workingCopies[2].getType("IX");
	IMethod method = type.getMethod("foo", new String[] { "QU;" });
	search(method, DECLARATIONS);
	assertSearchResults(
		"""
			src/b100772/IX.java void b100772.IX.foo(U) [foo] EXACT_MATCH
			src/b100772/X.java void b100772.X.foo(T) [foo] EXACT_MATCH
			src/b100772/XX.java void b100772.XX.foo(String) [foo] EXACT_MATCH
			src/b100772/Y.java void b100772.Y.foo(String) [foo] EXACT_MATCH
			src/b100772/Z.java void b100772.Z.foo(String) [foo] EXACT_MATCH"""
	);
}
public void testBug100772_ProjectScope_Complex02() throws CoreException {
	setUpBug100772_ProjectScope_Complex();
	IType type = this.workingCopies[5].getType("Z");
	IMethod method = type.getMethod("foo", new String[] { "QString;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"""
			src/b100772/IX.java void b100772.IX.foo(U) [foo] EXACT_MATCH
			src/b100772/XX.java void b100772.XX.foo(String) [foo] EXACT_MATCH
			src/b100772/Y.java void b100772.Y.foo(String) [foo] EXACT_MATCH
			src/b100772/Z.java void b100772.Z.foo(String) [foo] EXACT_MATCH"""
	);
}
public void testBug100772_ProjectScope_Complex03() throws CoreException {
	setUpBug100772_ProjectScope_Complex();
	IType type = this.workingCopies[5].getType("Z");
	IMethod method = type.getMethod("foo", new String[] { "QException;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"""
			src/b100772/IXX.java void b100772.IXX.foo(V) [foo] EXACT_MATCH
			src/b100772/XX.java void b100772.XX.foo(Exception) [foo] EXACT_MATCH
			src/b100772/Y.java void b100772.Y.foo(Exception) [foo] EXACT_MATCH
			src/b100772/Z.java void b100772.Z.foo(Exception) [foo] EXACT_MATCH"""
	);
}
public void testBug100772_ProjectScope_Complex04() throws CoreException {
	setUpBug100772_ProjectScope_Complex();
	IType type = this.workingCopies[0].getType("X");
	IMethod method = type.getMethod("foo", new String[] { "QT;" });
	search(method, UI_DECLARATIONS);
	assertSearchResults(
		"""
			src/b100772/X.java void b100772.X.foo(T) [foo] EXACT_MATCH
			src/b100772/IX.java void b100772.IX.foo(U) [foo] EXACT_MATCH
			src/b100772/XX.java void b100772.XX.foo(String) [foo] EXACT_MATCH"""
	);
}

/**
 * bug 108088: [search] Inaccurate search match for method invocations with literal arguments
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
 * bug 109695: [search] Numbers should be treated as upper-case letters in CamelCase matching
 * test Ensure that camel case pattern including numbers return correct set of types
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=109695"
 */
public void testBug109695() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/IDocumentExtension.java",
		"""
			public interface IDocumentExtension {}
			interface IDocumentExtension2 {}
			interface IDocumentExtension3 {}
			interface IDocumentExtension135 {}
			interface IDocumentExtension315 {}
			"""
	);
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	int validatedRule = SearchPattern.validateMatchRule("IDE3", SearchPattern.R_CAMELCASE_MATCH);
	searchAllTypeNames("IDE3", validatedRule, requestor);
	assertSearchResults(
		"""
			IDocumentExtension135
			IDocumentExtension3
			IDocumentExtension315""",
		requestor
	);
}
public void testBug109695b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/IDocumentProviderExtension.java",
		"""
			public interface IDocumentProviderExtension {}
			interface IDocumentProviderExtension2 {}
			interface IDocumentProviderExtension3 {}
			interface IDocumentProviderExtension4 {}
			interface IDocumentProviderExtension5 {}
			interface IDocumentProviderExtension12345 {}
			interface IDocumentProviderExtension54321 {}
			"""
	);
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	int validatedRule = SearchPattern.validateMatchRule("IDPE3", SearchPattern.R_CAMELCASE_MATCH);
	searchAllTypeNames("IDPE3", validatedRule, requestor);
	assertSearchResults(
		"""
			IDocumentProviderExtension12345
			IDocumentProviderExtension3
			IDocumentProviderExtension54321""",
		requestor
	);
}
public void testBug109695c() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/IPerspectiveListener.java",
		"""
			public interface IPerspectiveListener {}
			interface IPerspectiveListener2 {}
			interface IPerspectiveListener3 {}
			"""
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
		"""
			public interface IWorkbenchWindowPulldownDelegate {}
			interface IWorkbenchWindowPulldownDelegate1 {}
			interface IWorkbenchWindowPulldownDelegate2 {}
			interface IWorkbenchWindowPulldownDelegate3 {}
			interface IWorkbenchWindowPulldownDelegate4 {}
			"""
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
		"""
			public class UTF16DocumentScannerSupport {}
			class UTF1DocScannerSupport {}
			class UTF6DocScannerSupport {}
			class UTFDocScannerSupport {}
			"""
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
		"""
			public class UTF16DocumentScannerSupport {}
			class UTF1DocScannerSupport {}
			class UTF6DocScannerSupport {}
			class UTFDocScannerSupport {}
			"""
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
		"""
			public class UTF16DocumentScannerSupport {}
			class UTF1DocScannerSupport {}
			class UTF6DocScannerSupport {}
			class UTFDocScannerSupport {}
			"""
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
		"""
			public class UTF16DocumentScannerSupport {}
			class UTF1DocScannerSupport {}
			class UTF6DocScannerSupport {}
			class UTFDocScannerSupport {}
			"""
	);
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	int validatedRule = SearchPattern.validateMatchRule("UTFDSS", SearchPattern.R_CAMELCASE_MATCH);
	searchAllTypeNames("UTFDSS", validatedRule, requestor);
	assertSearchResults(
		"""
			UTF16DocumentScannerSupport
			UTF1DocScannerSupport
			UTF6DocScannerSupport
			UTFDocScannerSupport""",
		requestor
	);
}

/**
 * To get these tests search matches in a workspace, do NOT forget to modify files
 * to set them as working copies.
 *
 * test Bug 110060: [plan][search] Add support for Camel Case search pattern
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=110060"
 */
// Types search
private void setUpBug110060_TypePattern() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110060/Test.java",
		"""
			package b110060;
			public class Test {
				Aaa a1;
				AAa a2;
				AaAaAa a3;
				AAxx a4;
				AxA a5;
				AxxAyy a6;
			}
			class AAa {}
			class Aaa {}
			class AaAaAa {}
			class AAxx {}
			class AxA {}
			class AxxAyy {}
			"""
	);
}
public void testBug110060_TypePattern01() throws CoreException {
	setUpBug110060_TypePattern();
	search("AA", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"""
			src/b110060/Test.java b110060.Test.a1 [Aaa] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a2 [AAa] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a3 [AaAaAa] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a4 [AAxx] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a5 [AxA] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a6 [AxxAyy] EXACT_MATCH"""
	);
}
public void testBug110060_TypePattern01_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	search("AA", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"""
			src/b110060/Test.java b110060.Test.a2 [AAa] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a4 [AAxx] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a5 [AxA] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a6 [AxxAyy] EXACT_MATCH"""
	);
}
public void testBug110060_TypePattern02() throws CoreException {
	setUpBug110060_TypePattern();
	search("AA", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"""
			src/b110060/Test.java b110060.Test.a1 [Aaa] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a2 [AAa] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a3 [AaAaAa] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a4 [AAxx] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a5 [AxA] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a6 [AxxAyy] EXACT_MATCH
			src/b110060/Test.java b110060.AAa [AAa] EXACT_MATCH
			src/b110060/Test.java b110060.Aaa [Aaa] EXACT_MATCH
			src/b110060/Test.java b110060.AaAaAa [AaAaAa] EXACT_MATCH
			src/b110060/Test.java b110060.AAxx [AAxx] EXACT_MATCH
			src/b110060/Test.java b110060.AxA [AxA] EXACT_MATCH
			src/b110060/Test.java b110060.AxxAyy [AxxAyy] EXACT_MATCH"""
	);
}
public void testBug110060_TypePattern02_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	search("AA", TYPE, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"""
			src/b110060/Test.java b110060.Test.a2 [AAa] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a4 [AAxx] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a5 [AxA] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a6 [AxxAyy] EXACT_MATCH
			src/b110060/Test.java b110060.AAa [AAa] EXACT_MATCH
			src/b110060/Test.java b110060.AAxx [AAxx] EXACT_MATCH
			src/b110060/Test.java b110060.AxA [AxA] EXACT_MATCH
			src/b110060/Test.java b110060.AxxAyy [AxxAyy] EXACT_MATCH"""
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
		"""
			src/b110060/Test.java b110060.Test.a1 [Aaa] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a2 [AAa] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a3 [AaAaAa] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a4 [AAxx] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a5 [AxA] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a6 [AxxAyy] EXACT_MATCH"""
	);
}
public void testBug110060_TypePattern06_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	search("A*A*", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	// Invalid camel case pattern => replace the camel case flag with pattern match one (case insensitive)
	assertSearchResults(
		"""
			src/b110060/Test.java b110060.Test.a1 [Aaa] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a2 [AAa] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a3 [AaAaAa] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a4 [AAxx] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a5 [AxA] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a6 [AxxAyy] EXACT_MATCH"""
	);
}
public void testBug110060_TypePattern07() throws CoreException {
	setUpBug110060_TypePattern();
	search("aaa", TYPE, REFERENCES, SearchPattern.R_CAMELCASE_MATCH);
	// Invalid camel case pattern => replace the camel case flag by prefix match one (case insensitive)
	assertSearchResults(
		"""
			src/b110060/Test.java b110060.Test.a1 [Aaa] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a2 [AAa] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a3 [AaAaAa] EXACT_MATCH"""
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
		"""
			src/b110060/Test.java b110060.Test.a1 [Aaa] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a2 [AAa] EXACT_MATCH
			src/b110060/Test.java b110060.Test.a3 [AaAaAa] EXACT_MATCH"""
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
		"""
			b110060.AAa
			b110060.AAxx
			b110060.AaAaAa
			b110060.Aaa
			b110060.AxA
			b110060.AxxAyy""",
		requestor);
}
public void testBug110060_AllTypeNames01_SamePartCount() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("AA", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, requestor);
	assertSearchResults("Unexpected all type names",
		"""
			b110060.AAa
			b110060.AAxx
			b110060.AxA
			b110060.AxxAyy""",
		requestor);
}
public void testBug110060_AllTypeNames02() throws CoreException {
	setUpBug110060_TypePattern();
	TypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
	searchAllTypeNames("aaa", SearchPattern.R_CAMELCASE_MATCH, requestor);
	// Invalid camel case pattern => replace the camel case flag with prefix match one (case insensitive)
	assertSearchResults("Unexpected all type names",
		"""
			b110060.AAa
			b110060.AaAaAa
			b110060.Aaa""",
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
		"""
			b110060.AAa
			b110060.AAxx
			b110060.AaAaAa
			b110060.Aaa""",
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
		"""
			b110060.AAa
			b110060.AaAaAa
			b110060.Aaa""",
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
		"""
			b110060.AAa
			b110060.AAxx
			b110060.AaAaAa
			b110060.Aaa""",
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
		"""
			package b110060;
			public class AAAA {
				AAAA() {}
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b110060/AAxx.java",
		"""
			package b110060;
			public class AAxx {
				AAxx() {}
			}
			"""
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b110060/AxxAyy.java",
		"""
			package b110060;
			public class AxxAyy {
				AxxAyy() {}
			}
			"""
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b110060/AxAyAz.java",
		"""
			package b110060;
			public class AxAyAz {
				AxAyAz() {}
			}
			"""
	);
	this.workingCopies[4] = getWorkingCopy("/JavaSearchBugs/src/b110060/Test.java",
		"""
			package b110060;
			public class Test {
				AAAA aaaa = new AAAA();
				AAxx aaxx = new AAxx();
				AxAyAz axayaz = new AxAyAz();
				AxxAyy axxayy = new AxxAyy();
			}
			"""
	);
}
public void testBug110060_ConstructorPattern01() throws CoreException {
	setUpBug110060_ConstructorPattern();
	search("AA", CONSTRUCTOR, REFERENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"""
			src/b110060/Test.java b110060.Test.aaaa [new AAAA()] EXACT_MATCH
			src/b110060/Test.java b110060.Test.aaxx [new AAxx()] EXACT_MATCH
			src/b110060/Test.java b110060.Test.axayaz [new AxAyAz()] EXACT_MATCH
			src/b110060/Test.java b110060.Test.axxayy [new AxxAyy()] EXACT_MATCH"""
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
		"""
			src/b110060/AAAA.java b110060.AAAA() [AAAA] EXACT_MATCH
			src/b110060/AAxx.java b110060.AAxx() [AAxx] EXACT_MATCH
			src/b110060/AxAyAz.java b110060.AxAyAz() [AxAyAz] EXACT_MATCH
			src/b110060/AxxAyy.java b110060.AxxAyy() [AxxAyy] EXACT_MATCH
			src/b110060/Test.java b110060.Test.aaaa [new AAAA()] EXACT_MATCH
			src/b110060/Test.java b110060.Test.aaxx [new AAxx()] EXACT_MATCH
			src/b110060/Test.java b110060.Test.axayaz [new AxAyAz()] EXACT_MATCH
			src/b110060/Test.java b110060.Test.axxayy [new AxxAyy()] EXACT_MATCH"""
	);
}
public void testBug110060_ConstructorPattern02_SamePartCount() throws CoreException {
	setUpBug110060_ConstructorPattern();
	search("AA", CONSTRUCTOR, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"""
			src/b110060/AAxx.java b110060.AAxx() [AAxx] EXACT_MATCH
			src/b110060/AxxAyy.java b110060.AxxAyy() [AxxAyy] EXACT_MATCH
			src/b110060/Test.java b110060.Test.aaxx [new AAxx()] EXACT_MATCH
			src/b110060/Test.java b110060.Test.axxayy [new AxxAyy()] EXACT_MATCH"""
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
		"""
			src/b110060/AxAyAz.java b110060.AxAyAz() [AxAyAz] EXACT_MATCH
			src/b110060/AxxAyy.java b110060.AxxAyy() [AxxAyy] EXACT_MATCH
			src/b110060/Test.java b110060.Test.axayaz [new AxAyAz()] EXACT_MATCH
			src/b110060/Test.java b110060.Test.axxayy [new AxxAyy()] EXACT_MATCH"""
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
		"""
			src/b110060/AAAA.java b110060.AAAA() [AAAA] EXACT_MATCH
			src/b110060/AAxx.java b110060.AAxx() [AAxx] EXACT_MATCH
			src/b110060/AxAyAz.java b110060.AxAyAz() [AxAyAz] EXACT_MATCH
			src/b110060/AxxAyy.java b110060.AxxAyy() [AxxAyy] EXACT_MATCH
			src/b110060/Test.java b110060.Test.aaaa [new AAAA()] EXACT_MATCH
			src/b110060/Test.java b110060.Test.aaxx [new AAxx()] EXACT_MATCH
			src/b110060/Test.java b110060.Test.axayaz [new AxAyAz()] EXACT_MATCH
			src/b110060/Test.java b110060.Test.axxayy [new AxxAyy()] EXACT_MATCH"""
	);
}
public void testBug110060_ConstructorPattern05_SamePartCount() throws CoreException {
	setUpBug110060_ConstructorPattern();
	search("A*A*", CONSTRUCTOR, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	// Invalid camel case pattern => replace the camel case flag with pattern match one (case insensitive)
	assertSearchResults(
		"""
			src/b110060/AAAA.java b110060.AAAA() [AAAA] EXACT_MATCH
			src/b110060/AAxx.java b110060.AAxx() [AAxx] EXACT_MATCH
			src/b110060/AxAyAz.java b110060.AxAyAz() [AxAyAz] EXACT_MATCH
			src/b110060/AxxAyy.java b110060.AxxAyy() [AxxAyy] EXACT_MATCH
			src/b110060/Test.java b110060.Test.aaaa [new AAAA()] EXACT_MATCH
			src/b110060/Test.java b110060.Test.aaxx [new AAxx()] EXACT_MATCH
			src/b110060/Test.java b110060.Test.axayaz [new AxAyAz()] EXACT_MATCH
			src/b110060/Test.java b110060.Test.axxayy [new AxxAyy()] EXACT_MATCH"""
	);
}

// Methods search
private void setUpBug110060_MethodPattern() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110060/Test.java",
		"""
			package b110060;
			public class Test {
				void aMethodWithNothingSpecial() {}
				void aMethodWith1Digit() {}
				void aMethodWith1DigitAnd_AnUnderscore() {}
				void aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores() {}
				void otherMethodWhichStartsWithAnotherLetter() {}
				void testReferences() {
					aMethodWith1Digit();
					aMethodWith1DigitAnd_AnUnderscore();
					aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores();
					aMethodWithNothingSpecial();
					otherMethodWhichStartsWithAnotherLetter();
				}
			}
			"""
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
		"""
			src/b110060/Test.java void b110060.Test.aMethodWith1Digit() [aMethodWith1Digit] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.aMethodWith1DigitAnd_AnUnderscore() [aMethodWith1DigitAnd_AnUnderscore] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Digit()] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1DigitAnd_AnUnderscore()] EXACT_MATCH"""
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
		"""
			src/b110060/Test.java void b110060.Test.aMethodWithNothingSpecial() [aMethodWithNothingSpecial] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.aMethodWith1Digit() [aMethodWith1Digit] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.aMethodWith1DigitAnd_AnUnderscore() [aMethodWith1DigitAnd_AnUnderscore] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Digit()] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1DigitAnd_AnUnderscore()] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores()] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWithNothingSpecial()] EXACT_MATCH"""
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
		"""
			src/b110060/Test.java void b110060.Test.aMethodWithNothingSpecial() [aMethodWithNothingSpecial] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.aMethodWith1Digit() [aMethodWith1Digit] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.aMethodWith1DigitAnd_AnUnderscore() [aMethodWith1DigitAnd_AnUnderscore] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Digit()] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1DigitAnd_AnUnderscore()] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores()] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWithNothingSpecial()] EXACT_MATCH"""
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
		"""
			src/b110060/Test.java void b110060.Test.aMethodWith1Digit() [aMethodWith1Digit] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.aMethodWith1DigitAnd_AnUnderscore() [aMethodWith1DigitAnd_AnUnderscore] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Digit()] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1DigitAnd_AnUnderscore()] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores()] EXACT_MATCH"""
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
		"""
			src/b110060/Test.java void b110060.Test.aMethodWithNothingSpecial() [aMethodWithNothingSpecial] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.aMethodWith1DigitAnd_AnUnderscore() [aMethodWith1DigitAnd_AnUnderscore] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.otherMethodWhichStartsWithAnotherLetter() [otherMethodWhichStartsWithAnotherLetter] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1DigitAnd_AnUnderscore()] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores()] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWithNothingSpecial()] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [otherMethodWhichStartsWithAnotherLetter()] EXACT_MATCH"""
	);
}
public void testBug110060_MethodPattern07_SamePartCount() throws CoreException {
	setUpBug110060_MethodPattern();
	search("*Method*With*A*", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	// Invalid camel case pattern => replace the camel case flag with pattern match one (case insensitive)
	assertSearchResults(
		"""
			src/b110060/Test.java void b110060.Test.aMethodWithNothingSpecial() [aMethodWithNothingSpecial] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.aMethodWith1DigitAnd_AnUnderscore() [aMethodWith1DigitAnd_AnUnderscore] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.otherMethodWhichStartsWithAnotherLetter() [otherMethodWhichStartsWithAnotherLetter] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1DigitAnd_AnUnderscore()] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores()] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWithNothingSpecial()] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [otherMethodWhichStartsWithAnotherLetter()] EXACT_MATCH"""
	);
}
public void testBug110060_MethodPattern08() throws CoreException {
	setUpBug110060_MethodPattern();
	search("aMW1D", METHOD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"""
			src/b110060/Test.java void b110060.Test.aMethodWith1Digit() [aMethodWith1Digit] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.aMethodWith1DigitAnd_AnUnderscore() [aMethodWith1DigitAnd_AnUnderscore] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1Digit()] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aMethodWith1DigitAnd_AnUnderscore()] EXACT_MATCH"""
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
		"""
			package b110060;
			public class Test {
				Object aFieldWithNothingSpecial;
				Object aFieldWithS$Dollar;
				Object aFieldWith$Several$DollarslAnd1DigitAnd_1Underscore;
				Object aStrangeFieldWith$$$$$$$$$$$$$$$SeveraContiguousDollars;
				Object otherFieldWhichStartsWithAnotherLetter;
				void testReferences() {
					Object o0 = aFieldWithNothingSpecial;
					Object o1 = aFieldWithS$Dollar;
					Object o2 = aFieldWith$Several$DollarslAnd1DigitAnd_1Underscore;
					Object o3 = aStrangeFieldWith$$$$$$$$$$$$$$$SeveraContiguousDollars;
				}
				Object oF = otherFieldWhichStartsWithAnotherLetter;
			}
			"""
	);
}
public void testBug110060_FieldPattern01() throws CoreException {
	setUpBug110060_FieldPattern();
	search("aFWSD", FIELD, ALL_OCCURRENCES, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"""
			src/b110060/Test.java b110060.Test.aFieldWithS$Dollar [aFieldWithS$Dollar] EXACT_MATCH
			src/b110060/Test.java b110060.Test.aFieldWith$Several$DollarslAnd1DigitAnd_1Underscore [aFieldWith$Several$DollarslAnd1DigitAnd_1Underscore] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aFieldWithS$Dollar] EXACT_MATCH
			src/b110060/Test.java void b110060.Test.testReferences() [aFieldWith$Several$DollarslAnd1DigitAnd_1Underscore] EXACT_MATCH"""
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
		"""
			src/b110060/Test.java b110060.Test.otherFieldWhichStartsWithAnotherLetter [otherFieldWhichStartsWithAnotherLetter] EXACT_MATCH
			src/b110060/Test.java b110060.Test.oF [oF] EXACT_MATCH
			src/b110060/Test.java b110060.Test.oF [otherFieldWhichStartsWithAnotherLetter] EXACT_MATCH"""
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
 * test Bug 110291: [search] BasicSearchEngine return constructor declarations that doesn't exist in source
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=110291"
 */
public void testBug110291() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110291/Test110291XX.java",
		"""
			package b110291;
			public class Test110291XX {
				class Test110291YY {}\
			}
			"""
	);
	search("Test110291", CONSTRUCTOR, DECLARATIONS, SearchPattern.R_PREFIX_MATCH);
	assertSearchResults(
		"src/b110291/Test110291XX.java b110291.Test110291XX$Test110291YY [Test110291YY] EXACT_MATCH"
	);
}

/**
 * test Bug 110336: [plan][search] Should optionally return the local variable for type reference
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=110336"
 */
public void testBug110336a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
		"""
			package b110336;
			public class Test {
				<TP extends Test> void method(Class<Test> clazz) {
					Test localVar1 = new Test();
					Class<Test> localVar2 = new Class<Test>();
					localVar1.method(localVar2);
				}
			}
			"""
	);
	IType type = this.workingCopies[0].getType("Test");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"""
			src/b110336/Test.java void b110336.Test.method(Class<Test>) [Test]+[TP]
			src/b110336/Test.java void b110336.Test.method(Class<Test>) [Test]+[clazz]
			src/b110336/Test.java void b110336.Test.method(Class<Test>) [Test]+[localVar1]
			src/b110336/Test.java void b110336.Test.method(Class<Test>) [Test]+[localVar1]
			src/b110336/Test.java void b110336.Test.method(Class<Test>) [Test]+[localVar2]
			src/b110336/Test.java void b110336.Test.method(Class<Test>) [Test]+[localVar2]""",
		collector
	);
}
public void testBug110336b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
		"""
			package b110336;
			public class Test {
				void method1(Test methodParam) {
					Test localVar1 = new Test(){
						Class c = Test.class;
						<TP extends Test> void foo(){
							Test o = (Test) null;
						}
					};
				}\t
			}
			"""
	);
	IType type = this.workingCopies[0].getType("Test");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"""
			src/b110336/Test.java void b110336.Test.method1(Test):<anonymous>#1 [Test]
			src/b110336/Test.java void b110336.Test.method1(Test):<anonymous>#1.c [Test]
			src/b110336/Test.java void void b110336.Test.method1(Test):<anonymous>#1.foo() [Test]+[TP]
			src/b110336/Test.java void void b110336.Test.method1(Test):<anonymous>#1.foo() [Test]+[o]
			src/b110336/Test.java void void b110336.Test.method1(Test):<anonymous>#1.foo() [Test]+[o]
			src/b110336/Test.java void b110336.Test.method1(Test) [Test]+[methodParam]
			src/b110336/Test.java void b110336.Test.method1(Test) [Test]+[localVar1]""",
		collector
	);
}
public void testBug110336c() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
		"""
			package b110336;
			public class Test<TP extends X> {
				X x;
			
			}
			class X {}
			"""
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
		"""
			package b110336;
			public class Test {
				Test a1Test = null, b1Test = new Test(), c1Test;
				Test a2Test = new Test(), b2Test, c2Test = null;
				Test a3Test, b3Test = null, c3Test = new Test();
			}
			"""
	);
	IType type = this.workingCopies[0].getType("Test");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"""
			src/b110336/Test.java b110336.Test.a1Test [Test]+[b1Test,c1Test]
			src/b110336/Test.java b110336.Test.b1Test [Test]
			src/b110336/Test.java b110336.Test.a2Test [Test]+[b2Test,c2Test]
			src/b110336/Test.java b110336.Test.a2Test [Test]
			src/b110336/Test.java b110336.Test.a3Test [Test]+[b3Test,c3Test]
			src/b110336/Test.java b110336.Test.c3Test [Test]""",
		collector
	);
}
public void testBug110336e() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
		"""
			package b110336;
			public class Test {
				void foo() {
					Test lv1 = null, lv2 = new Test(), lv3;
					Test lv4 = new Test(), lv5, lv6 = null;
					Test lv7, lv8 = null, lv9 = new Test();
				}
			}
			"""
	);
	IType type = this.workingCopies[0].getType("Test");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"""
			src/b110336/Test.java void b110336.Test.foo() [Test]+[lv1]+[lv2,lv3]
			src/b110336/Test.java void b110336.Test.foo() [Test]+[lv2]
			src/b110336/Test.java void b110336.Test.foo() [Test]+[lv4]+[lv5,lv6]
			src/b110336/Test.java void b110336.Test.foo() [Test]+[lv4]
			src/b110336/Test.java void b110336.Test.foo() [Test]+[lv7]+[lv8,lv9]
			src/b110336/Test.java void b110336.Test.foo() [Test]+[lv9]""",
		collector
	);
}
public void testBug110336f() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
		"""
			package b110336;
			public class Test extends Exception {
			        void foo(Test test1) { // <- no local element
			                Test test2; // <- local element
			                try {
			                        throw new Test();
			                }
			                catch (Test test4) { // <- no local element
			                }
			                for(Test test3;;) {} // <- local element
			        }
			
			}
			"""
	);
	IType type = this.workingCopies[0].getType("Test");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"""
			src/b110336/Test.java void b110336.Test.foo(Test) [Test]+[test1]
			src/b110336/Test.java void b110336.Test.foo(Test) [Test]+[test2]
			src/b110336/Test.java void b110336.Test.foo(Test) [Test]
			src/b110336/Test.java void b110336.Test.foo(Test) [Test]+[test4]
			src/b110336/Test.java void b110336.Test.foo(Test) [Test]+[test3]""",
		collector
	);
}
public void testBug110336g() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
		"""
			package b110336;
			public class Test {
				{
					Test lv1 = null, lv2 = new Test(), lv3;
					Test lv4 = new Test(), lv5, lv6 = null;
					Test lv7, lv8 = null, lv9 = new Test();
				}
			}
			"""
	);
	IType type = this.workingCopies[0].getType("Test");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"""
			src/b110336/Test.java b110336.Test.{} [Test]+[lv1]+[lv2,lv3]
			src/b110336/Test.java b110336.Test.{} [Test]+[lv2]
			src/b110336/Test.java b110336.Test.{} [Test]+[lv4]+[lv5,lv6]
			src/b110336/Test.java b110336.Test.{} [Test]+[lv4]
			src/b110336/Test.java b110336.Test.{} [Test]+[lv7]+[lv8,lv9]
			src/b110336/Test.java b110336.Test.{} [Test]+[lv9]""",
		collector
	);
}
public void testBug110336h() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b110336/Test.java",
		"""
			package b110336;
			public class Test {
				static {
					Test lv1 = null, lv2 = new Test(), lv3;
					Test lv4 = new Test(), lv5, lv6 = null;
					Test lv7, lv8 = null, lv9 = new Test();
				}
			}
			"""
	);
	IType type = this.workingCopies[0].getType("Test");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"""
			src/b110336/Test.java b110336.Test.static {} [Test]+[lv1]+[lv2,lv3]
			src/b110336/Test.java b110336.Test.static {} [Test]+[lv2]
			src/b110336/Test.java b110336.Test.static {} [Test]+[lv4]+[lv5,lv6]
			src/b110336/Test.java b110336.Test.static {} [Test]+[lv4]
			src/b110336/Test.java b110336.Test.static {} [Test]+[lv7]+[lv8,lv9]
			src/b110336/Test.java b110336.Test.static {} [Test]+[lv9]""",
		collector
	);
}

/**
 * test Bug 110422: [search] BasicSearchEngine doesn't find all type declarations
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
 * bug 113671: [search] AIOOBE in SearchEngine#searchAllTypeNames
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
		"""
			java.lang.CharSequence
			java.lang.Class
			java.lang.CloneNotSupportedException
			java.lang.Comparable
			java.lang.Deprecated
			java.lang.Enum
			java.lang.Error
			java.lang.Exception
			java.lang.IllegalMonitorStateException
			java.lang.InterruptedException
			java.lang.Object
			java.lang.RuntimeException
			java.lang.String
			java.lang.Throwable""",
		requestor);
}

/**
 * test Bug 114539: [search] Internal error when refactoring code with errors
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=114539"
 */
public void testBug114539() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b114539/Foo.java",
		"""
			package b114539;
			public class Foo {
				int bar=Bar.FOO;
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b114539/Bar.java",
		"""
			package b114539;
			public class Bar {
				private static final int FOO=0;
			}
			"""
	);
	IField field = this.workingCopies[1].getType("Bar").getField("FOO");
	search(field, REFERENCES);
	assertSearchResults(
		"src/b114539/Foo.java b114539.Foo.bar [FOO] POTENTIAL_MATCH"
	);
}

/**
 * bug 116459: [search] correct results are missing in java search
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=116459"
 */
public void testBug116459() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p1/X.java",
		"""
			package p1;
			class X<T> {
				X<T> gen;
				X<String> param;
				X raw;
			}"""
	);
	IType type = this.workingCopies[0].getType("X");
	this.resultCollector.showRule();
	search(type, REFERENCES, ERASURE_RULE);
	assertSearchResults(
		"""
			src/p1/X.java p1.X.gen [X] EXACT_MATCH
			src/p1/X.java p1.X.param [X] ERASURE_MATCH
			src/p1/X.java p1.X.raw [X] ERASURE_RAW_MATCH"""
	);
}

/**
 * test Bug 119545: [search] Binary java method model elements returned by SearchEngine have unresolved parameter types
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=119545"
 */
public void testBug119545() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b119545/Test.java",
		"""
			package b119545;
			class Test {
				void foo(Object o1, Object o2){
					if (o1.equals(o2)) {}
				}
			}
			"""
	);
	IType type = this.workingCopies[0].getType("Test");
	IMethod method = type.getMethods()[0];
	searchDeclarationsOfSentMessages(method, this.resultCollector);
	assertSearchResults(
		""+ getExternalJCLPathString("1.5") + " boolean java.lang.Object.equals(java.lang.Object) EXACT_MATCH"
	);
}

/**
 * test Bug 120816: [search] NullPointerException at ...jdt.internal.compiler.lookup.SourceTypeBinding.getMethods
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=120816"
 */
public void testBug120816a() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b120816/Test.java",
		"""
			package b120816;
			public class Test<E> {
				String foo(E e) { return ""; }
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b120816/Sub.java",
		"""
			package b120816;
			public class Sub extends Test<Exception> {
				String foo(RuntimeException e) { return ""; }
				String foo(Exception e) {
					return super.foo(e);
				}
			}
			"""
	);
	search("* String (Exception)", METHOD, DECLARATIONS);
	assertSearchResults(
		"src/b120816/Sub.java String b120816.Sub.foo(Exception) [foo] EXACT_MATCH"
	);
}
public void testBug120816b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b120816/Test.java",
		"""
			package b120816;
			public class Test<E> {
				String foo(E e) { return ""; }
			}
			class Sub extends Test<Exception> {
				String foo(RuntimeException e) { return ""; }
				String foo(Exception e) {
					return super.foo(e);
				}
			}
			"""
	);
	search("* String (Exception)", METHOD, DECLARATIONS);
	assertSearchResults(
		"src/b120816/Test.java String b120816.Sub.foo(Exception) [foo] EXACT_MATCH"
	);
}

/**
 * test Bug 122442: [search] API inconsistency with IJavaSearchConstants.IMPLEMENTORS and SearchPattern
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
		"""
			class Klass {}
			interface Interface {}
			class User {
			    void m() {
			        new Klass() {};
			        new Interface() {};
			    }
			}
			class Sub extends Klass {}"""
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
 * bug 123679: [search] Field references not found when type is a qualified member type [regression]
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=123679"
 */
public void testBug123679() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b123679.jar", "pack").getOrdinaryClassFile("I123679.class").getType();
	search(type, REFERENCES);
	assertSearchResults(
		"""
			lib/b123679.jar test.<anonymous> EXACT_MATCH
			lib/b123679.jar test.Test$StaticClass$Member.parent EXACT_MATCH
			lib/b123679.jar test.<anonymous> EXACT_MATCH
			lib/b123679.jar test.Test$StaticClass$Member(test.Test.StaticClass, java.lang.Object) EXACT_MATCH
			lib/b123679.jar test.Test$StaticClass$Member(test.Test.StaticClass, java.lang.Object) EXACT_MATCH
			lib/b123679.jar pack.I123679 test.Test$StaticClass$Member.getParent() EXACT_MATCH"""
	);
}
public void testBug123679_cu() throws CoreException {
	ICompilationUnit unit = getCompilationUnit("JavaSearchBugs", "src", "b123679.pack", "I123679.java");
	IType type = unit.getType("I123679");
	search(type, REFERENCES);
	assertSearchResults(
		"""
			src/b123679/test/Test.java [b123679.pack.I123679] EXACT_MATCH
			src/b123679/test/Test.java b123679.test.Test$StaticClass$Member.parent [I123679] EXACT_MATCH
			src/b123679/test/Test.java b123679.test.Test$StaticClass$Member(Object):<anonymous>#1 [I123679] EXACT_MATCH
			src/b123679/test/Test.java b123679.test.Test$StaticClass$Member(Object) [I123679] EXACT_MATCH
			src/b123679/test/Test.java b123679.test.Test$StaticClass$Member(Object) [I123679] EXACT_MATCH
			src/b123679/test/Test.java I123679 b123679.test.Test$StaticClass$Member.getParent() [I123679] EXACT_MATCH"""
	);
}
public void testBug123679_wc() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/I122679.java",
		"""
			package pack;
			public interface I123679 {
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
		"""
			package test;
			import pack.I123679;
			public class Test {
				static class StaticClass {
					class Member {
						private I123679 parent;
						Member(Object obj) {
							if (obj instanceof I123679) {
								parent = (I123679) obj;
							} else {
								parent = new I123679() {};
							}
						}
						I123679 getParent() {
							return parent;
						}
					}
				}
			}
			"""
	);
	IType type = this.workingCopies[0].getType("I123679");
	search(type, REFERENCES);
	assertSearchResults(
		"""
			src/test/Test.java [pack.I123679] EXACT_MATCH
			src/test/Test.java test.Test$StaticClass$Member.parent [I123679] EXACT_MATCH
			src/test/Test.java test.Test$StaticClass$Member(Object):<anonymous>#1 [I123679] EXACT_MATCH
			src/test/Test.java test.Test$StaticClass$Member(Object) [I123679] EXACT_MATCH
			src/test/Test.java test.Test$StaticClass$Member(Object) [I123679] EXACT_MATCH
			src/test/Test.java I123679 test.Test$StaticClass$Member.getParent() [I123679] EXACT_MATCH"""
	);
}

/**
 * bug 124469: [search] AIOOBE in PatternLocator when searching for dependency extent from manifest
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=124469"
 */
public void testBug124469a() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "E.class").getType();
	search(type, REFERENCES);
	assertSearchResults(
		"""
			lib/b124469.jar pack.E pack.A1.value() EXACT_MATCH
			lib/b124469.jar pack.E[] pack.A1.list() EXACT_MATCH
			lib/b124469.jar pack.E pack.A2.value() EXACT_MATCH
			lib/b124469.jar pack.E[] pack.A2.list() EXACT_MATCH
			lib/b124469.jar pack.E pack.A3.value() EXACT_MATCH
			lib/b124469.jar pack.E[] pack.A3.list() EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH"""
	);
}
public void testBug124469b() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "A1.class").getType();
	search(type, REFERENCES);
	assertSearchResults(
		"""
			lib/b124469.jar pack.A1 pack.A2.annot() EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH"""
	);
}
public void testBug124469c() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "A2.class").getType();
	search(type, REFERENCES);
	assertSearchResults(
		"""
			lib/b124469.jar pack.A2 pack.A3.annot() EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH"""
	);
}
public void testBug124469d() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "A3.class").getType();
	search(type, REFERENCES);
	assertSearchResults(
		"""
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH"""
	);
}
public void testBug124469e() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "E.class").getType();
	IField field = type.getField("CC");
	search(field, REFERENCES);
	assertSearchResults(
		"""
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH"""
	);
}
public void testBug124469f() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "E.class").getType();
	IField field = type.getField("CF");
	search(field, REFERENCES);
	assertSearchResults(
		"""
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH"""
	);
}
public void testBug124469g() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "E.class").getType();
	IField field = type.getField("CM");
	search(field, REFERENCES);
	assertSearchResults(
		"""
			lib/b124469.jar void test.M.foo() EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH"""
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
		"""
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH
			lib/b124469.jar test.C EXACT_MATCH"""
	);
}
public void testBug124469k() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "E.class").getType();
	IField field = type.getField("CAF");
	search(field, REFERENCES);
	assertSearchResults(
		"""
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH
			lib/b124469.jar test.F.field EXACT_MATCH"""
	);
}
public void testBug124469l() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b124469.jar", "pack", "E.class").getType();
	IField field = type.getField("CAM");
	search(field, REFERENCES);
	assertSearchResults(
		"""
			lib/b124469.jar void test.M.foo() EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH
			lib/b124469.jar void test.M.foo() EXACT_MATCH"""
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
 * bug 124489: [search] correct results are missing in java search
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=124489"
 */
public void testBug124489() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Foo.java",
		"public class Foo<T> {}"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/Bar.java",
		"""
			public class Bar {
			    Foo<String> f = new Foo<String>();
			    Foo f2 = new Foo();
			}"""
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
		"""
			src/Bar.java Bar.f [Foo] ERASURE_MATCH
			src/Bar.java Bar.f [Foo] ERASURE_MATCH
			src/Bar.java Bar.f2 [Foo] ERASURE_RAW_MATCH
			src/Bar.java Bar.f2 [Foo] ERASURE_RAW_MATCH"""
	);
}

/**
 * bug 124624: [search] Camel case matching routines should support end character
 * test Ensure that camel case pattern may use end character
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=124624"
 */
private void setupBug124624() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"""
			class HashMap {}
			class HtmlMapper {}
			class HashMapEntry {}
			class HaxMapxxxx {}
			"""
	);
}
public void testBug124624_HM_CamelCase() throws CoreException {
	setupBug124624();
	search("HM", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"""
			src/Test.java HashMap [HashMap] EXACT_MATCH
			src/Test.java HtmlMapper [HtmlMapper] EXACT_MATCH
			src/Test.java HashMapEntry [HashMapEntry] EXACT_MATCH
			src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"""
	);
}
public void testBug124624_HM_CamelCaseSamePartCount() throws CoreException {
	setupBug124624();
	search("HM", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"""
			src/Test.java HashMap [HashMap] EXACT_MATCH
			src/Test.java HtmlMapper [HtmlMapper] EXACT_MATCH
			src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"""
	);
}
public void testBug124624_HaM_CamelCase() throws CoreException {
	setupBug124624();
	search("HaM", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"""
			src/Test.java HashMap [HashMap] EXACT_MATCH
			src/Test.java HashMapEntry [HashMapEntry] EXACT_MATCH
			src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"""
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
		"""
			src/Test.java HashMap [HashMap] EXACT_MATCH
			src/Test.java HtmlMapper [HtmlMapper] EXACT_MATCH
			src/Test.java HashMapEntry [HashMapEntry] EXACT_MATCH
			src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"""
	);
}
public void testBug124624_HMa_CamelCaseSamePartCount() throws CoreException {
	setupBug124624();
	search("HMa", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"""
			src/Test.java HashMap [HashMap] EXACT_MATCH
			src/Test.java HtmlMapper [HtmlMapper] EXACT_MATCH
			src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"""
	);
}
public void testBug124624_HaMa_CamelCase() throws CoreException {
	setupBug124624();
	search("HaMa", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"""
			src/Test.java HashMap [HashMap] EXACT_MATCH
			src/Test.java HashMapEntry [HashMapEntry] EXACT_MATCH
			src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"""
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
		"""
			src/Test.java HashMap [HashMap] EXACT_MATCH
			src/Test.java HtmlMapper [HtmlMapper] EXACT_MATCH
			src/Test.java HashMapEntry [HashMapEntry] EXACT_MATCH
			src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"""
	);
}
public void testBug124624_HMap_CamelCaseSamePartCount() throws CoreException {
	setupBug124624();
	search("HMap", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH);
	assertSearchResults(
		"""
			src/Test.java HashMap [HashMap] EXACT_MATCH
			src/Test.java HtmlMapper [HtmlMapper] EXACT_MATCH
			src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"""
	);
}
public void testBug124624_HaMap_CamelCase() throws CoreException {
	setupBug124624();
	search("HaMap", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, SearchPattern.R_CAMELCASE_MATCH);
	assertSearchResults(
		"""
			src/Test.java HashMap [HashMap] EXACT_MATCH
			src/Test.java HashMapEntry [HashMapEntry] EXACT_MATCH
			src/Test.java HaxMapxxxx [HaxMapxxxx] EXACT_MATCH"""
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
 * test Bug 124645: [search] for implementors does not find subclasses of binary classes
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=124645"
 */
public void testBug124645a() throws CoreException {
	IOrdinaryClassFile classFile = getClassFile("JavaSearchBugs", "lib/b124645.jar", "xy", "BE_124645.class");
	IType type = classFile.getType();
	search(type, IMPLEMENTORS);
	assertSearchResults(
		"""
			src/b124645/xy/X_124645.java b124645.xy.X_124645$Y [BE_124645] EXACT_MATCH
			src/b124645/xy/Y_124645.java b124645.xy.Y_124645 [BE_124645] EXACT_MATCH
			lib/b124645.jar xy.BX_124645$Y EXACT_MATCH
			lib/b124645.jar xy.BY_124645 EXACT_MATCH"""
	);
}
public void testBug124645b() throws CoreException {
	IOrdinaryClassFile classFile = getClassFile("JavaSearchBugs", "lib/b124645.jar", "test", "BE_124645.class");
	IType type = classFile.getType();
	search(type, IMPLEMENTORS);
	assertSearchResults(
		"""
			src/b124645/test/A_124645.java b124645.test.A_124645 [BE_124645] EXACT_MATCH
			src/b124645/test/A_124645.java void b124645.test.A_124645.m():<anonymous>#1 [BE_124645] EXACT_MATCH
			src/b124645/test/X_124645.java b124645.test.X_124645 [BE_124645] EXACT_MATCH
			src/b124645/test/X_124645.java void b124645.test.X_124645.m():Y_124645#1 [BE_124645] EXACT_MATCH
			lib/b124645.jar test.BA_124645 EXACT_MATCH
			lib/b124645.jar test.<anonymous> EXACT_MATCH
			lib/b124645.jar test.BX_124645 EXACT_MATCH
			lib/b124645.jar test.Y EXACT_MATCH"""
	);
}
public void testBug124645c() throws CoreException {
	IOrdinaryClassFile classFile = getClassFile("JavaSearchBugs", "lib/b124645.jar", "", "BC_124645.class");
	IType type = classFile.getType();
	search(type, IMPLEMENTORS);
	assertSearchResults(
		"lib/b124645.jar <anonymous> EXACT_MATCH"
	);
}
public void testBug124645d() throws CoreException {
	IOrdinaryClassFile classFile = getClassFile("JavaSearchBugs", "lib/b124645.jar", "", "BI_124645.class");
	IType type = classFile.getType();
	search(type, IMPLEMENTORS);
	assertSearchResults(
		"lib/b124645.jar <anonymous> EXACT_MATCH"
	);
}

/**
 * bug 125178: [search] AIOOBE in PatternLocator when searching for dependency extent from manifest
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=125178"
 */
public void testBug125178() throws CoreException {
	// Need a working copy as anonymous are not indexed...
	ProblemRequestor problemRequestor = new ProblemRequestor();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b125178/X.java",
		"""
			package b125178;
			import pack.age.Test;
			public class X {
				public static void main(String[] args) {
					new Test().foo(100);
				}
			}
			""",
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
 * bug 126330: Type reference not found in jar file if sources was not already opened
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=126330"
 */
public void testBug126330() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b126330.jar", "").getOrdinaryClassFile("A126330.class").getType();
	search(type, REFERENCES);
	assertSearchResults(
		"lib/b126330.jar B126330.a EXACT_MATCH"
	);
}

/**
 * bug 127628: [index] CodeAssist doesn't filter deprecated types
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=127628"
 */
public void testBug127628() throws CoreException {
	class DeprecatedTypesRequestor extends SearchTests.SearchTypeNameRequestor {
		@Override
		public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path) {
			if ((modifiers & ClassFileConstants.AccDeprecated) != 0) {
				char[] typeName =
					CharOperation.concat(
						CharOperation.concatWith(enclosingTypeNames, '$'),
						simpleTypeName,
						'$');
				this.results.add(new String(CharOperation.concat(packageName, typeName, '.')));
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
 * bug 128877: [search] reports inexistent IMethod for binary constructor of inner class
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=128877"
 */
public void testBug128877a() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b128877.jar", "pack").getOrdinaryClassFile("Test.class").getType();
	IMethod method = type.getMethod("Test", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"lib/b128877.jar pack.X$Sub(pack.X) EXACT_MATCH"
	);
}
public void testBug128877b() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b128877.jar", "pack").getOrdinaryClassFile("Test.class").getType();
	IMethod method = type.getMethod("Test", new String[] { "Ljava.lang.String;" });
	search(method, REFERENCES);
	assertSearchResults(
		"lib/b128877.jar pack.X$Sub(pack.X, java.lang.String) EXACT_MATCH"
	);
}
public void testBug128877c() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b128877.jar", "pack").getOrdinaryClassFile("Test.class").getType();
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
 * test Bug 130390: CamelCase algorithm cleanup and improvement
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=130390"
 */
private void setUpBug130390() throws CoreException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b130390/TZ.java",
		"""
			package b130390;
			public class TZ {
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b130390/TimeZone.java",
		"""
			package b130390;
			public class TimeZone{
			}
			"""
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b130390/Npe.java",
		"""
			package b130390;
			public class Npe {
			}
			"""
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/b130390/NullPointerException.java",
		"""
			package b130390;
			public class NullPointerException {
			}
			"""
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
 * bug 137087: Open Type - missing matches when using mixed case pattern
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
 * bug 137984: [search] Field references not found when type is a qualified member type [regression]
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=137984"
 */
public void testBug137984_jar() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b137984.jar", "").getOrdinaryClassFile("CJ.class").getType();
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
		"""
			public class CW {
				CW2 c2;
				CW2.CW3 c3;
				CW(int c) {
					c2 = new CW2(c);
					c3 = c2.new CW3(c);
				}
				class CW2 {
					CW2(int x) {}
					class CW3 {
						CW3(int x) {}
					}
				}
			}
			"""
	);
	IField field = this.workingCopies[0].getType("CW").getField("c3");
	search(field, REFERENCES);
	assertSearchResults(
		"src/CW.java CW(int) [c3] EXACT_MATCH"
	);
}

/**
 * bug 140156: [1.5][search] Invalid method handle with parameterized parameters when no source is attached
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=140156"
 */
public void testBug140156() throws CoreException {
	IType type = getPackageFragment("JavaSearchBugs", "lib/b140156.jar", "").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethods()[1];
	assertEquals("Search wrong method!!!", "foo", method.getElementName());
	search(method, DECLARATIONS);
	assertSearchResults(
		"lib/b140156.jar void X.foo(List<T>) [No source] EXACT_MATCH"
	);
}

/**
 * bug 142044: [search] "And" Pattern fails with NullPointerException
 * test Verify that no NPE occurs while using a {@link AndPattern}
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
		"""
			src/b142044/X142044.java b142044.X142044$XX1 [I142044_A] EXACT_MATCH
			src/b142044/X142044.java b142044.X142044$XX12 [I142044_A] EXACT_MATCH
			src/b142044/X142044_A.java b142044.X142044_A [I142044_A] EXACT_MATCH
			src/b142044/X142044_AB.java b142044.X142044_AB [I142044_A] EXACT_MATCH"""
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
		"""
			src/b142044/X142044.java b142044.X142044$XX1 [I142044_A] EXACT_MATCH
			src/b142044/X142044.java b142044.X142044$XX12 [I142044_A] EXACT_MATCH
			src/b142044/X142044_A.java b142044.X142044_A [I142044_A] EXACT_MATCH
			src/b142044/X142044_AB.java b142044.X142044_AB [I142044_A] EXACT_MATCH"""
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
		"""
			src/b142044/X142044.java b142044.X142044$XX1 [I142044_A] EXACT_MATCH
			src/b142044/X142044.java b142044.X142044$XX2 [I142044_B] EXACT_MATCH
			src/b142044/X142044.java b142044.X142044$XX12 [I142044_A] EXACT_MATCH
			src/b142044/X142044.java b142044.X142044$XX12 [I142044_B] EXACT_MATCH
			src/b142044/X142044_A.java b142044.X142044_A [I142044_A] EXACT_MATCH
			src/b142044/X142044_AB.java b142044.X142044_AB [I142044_A] EXACT_MATCH
			src/b142044/X142044_AB.java b142044.X142044_AB [I142044_B] EXACT_MATCH
			src/b142044/X142044_B.java b142044.X142044_B [I142044_B] EXACT_MATCH"""
	);
}

/**
 * bug 144044: [search] NPE when trying to find references to field variable
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=144044"
 */
public void testBug144044() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test1/p/Test.java",
		"""
			package test1.p;
			import test1.q.X;
			public class Test {
				String foo(X val) {
					return val.str;
				}
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test1/q/X.java",
		"""
			package test1.q;
			public class X {
				String str;
			}
			"""
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
		"""
			package test2.p;
			import test2.q.X;
			public class Test {
				X foo() {
					return X.y_field.z_field.x_field.y_field.z_field.x_field;
				}
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test2/q/X.java",
		"""
			package test2.q;
			public class X {
				public static Y y_field;
			}
			"""
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/test2/q/Y.java",
		"""
			package test2.q;
			public class Y {
				public static Z z_field;
			}
			"""
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/test2/q/Z.java",
		"""
			package test2.q;
			public class Z {
				static X x_field;
			}
			"""
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
 * bug 148215: [search] correct results are missing in java search
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
 * bug 148380: [search] get IType from TypeNameRequestor result
 * test new SearchEngine.searchAllTypeName API method using {@link TypeNameMatchRequestor}
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
	String expected = """
		class Sub [in [Working copy] Sub.java [in b148380 [in src [in JavaSearchBugs]]]]
		class X [in [Working copy] X.java [in b148380 [in src [in JavaSearchBugs]]]]
		class Y [in [Working copy] Y.java [in b148380 [in src [in JavaSearchBugs]]]]
		interface I [in [Working copy] I.java [in b148380 [in src [in JavaSearchBugs]]]]""";
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
 * bug 153765: [search] Reference to package is not found in qualified annotation
 * test Ensure that references to package are also found in qualified annotation
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
			"""
				package test;
				public class SomeClass {
				        @b153765.Unimportant public void foo() {}
				}"""
		);
		waitUntilIndexesReady();
		IPackageFragment packageFragment = getPackage("/JavaSearchBugs/src/b153765");
		this.resultCollector.showSelection();
		search(packageFragment, REFERENCES);
		assertSearchResults(
			"src/b153765/test/SomeClass.java void b153765.test.SomeClass.foo() [        @!|b153765|!.Unimportant public void foo() {}] EXACT_MATCH"
		);
	}
	finally {
		deleteFolder("/JavaSearchBugs/src/b153765");
	}
}

/**
 * bug 156340: [search] searchAllTypeNames return nothing for empty prefix
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
		"""
			java.lang.CharSequence
			java.lang.Class
			java.lang.CloneNotSupportedException
			java.lang.Comparable
			java.lang.Deprecated
			java.lang.Enum
			java.lang.Error
			java.lang.Exception
			java.lang.IllegalMonitorStateException
			java.lang.InterruptedException
			java.lang.Object
			java.lang.RuntimeException
			java.lang.String
			java.lang.Throwable""",
		requestor);
}

/**
 * bug 156177: [1.5][search] interfaces and annotations could be found with only one requets of searchAllTypeName
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=156177"
 */
public void testBug156177() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b156177/Test.java",
		"""
			package b156177;
			interface B156177_I {}
			enum B156177_E {}
			@interface B156177_A {}
			public class B156177 {}
			"""
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
 * bug 156491: [1.5][search] interfaces and annotations could be found with only one requets of searchAllTypeName
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=156491"
 */
public void testBug156491() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test.java",
		"""
			package pack;
			public class Test {
				void noMatch(Y y) {
					y.toString();
					toString();
				}
				void validMatches(X x) {
					x.toString();
				}
				void superInvocationMatches(Object o) {
					o.toString();
				}
				void interfaceMatches(I i) {
					i.toString();
				}
				void subtypeMatches(Sub s) {
					s.toString();
				}
			}
			interface I {}
			class X {
				public String toString() {
					return "X";
				}
			}
			class Sub extends X {}
			class Y {
				public String toString() {
					return "Y";
				}
			}
			"""
	);
	IMethod method = this.workingCopies[0].getType("X").getMethod("toString", new String[0]);
	this.resultCollector.showFlavors = PatternLocator.SUPER_INVOCATION_FLAVOR;
	search(method, REFERENCES);
	assertSearchResults(
		"""
			src/pack/Test.java void pack.Test.validMatches(X) [toString()] EXACT_MATCH
			src/pack/Test.java void pack.Test.superInvocationMatches(Object) [toString()] EXACT_MATCH SUPER INVOCATION
			src/pack/Test.java void pack.Test.subtypeMatches(Sub) [toString()] EXACT_MATCH
			lib/b125178.jar java.lang.String pack.age.Test.foo(int) EXACT_MATCH SUPER INVOCATION"""
	);
}
private void setUpBug156491() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/other/Test.java",
		"""
			package other;
			public class Test {
				void testInterface(I i) {
					i.test();
				}
				void testSuperInvocation(L1 l) {
					l.test();
				}
				void testInvocation(L2 l) {
					l.test();
				}
			}
			class L1 implements I {
				public void test() {}
			}
			interface I {
				void test();
			}
			class L2 extends L1 {
				public void test() {}
			}"""
	);
}
public void testBug156491a() throws CoreException {
	this.resultCollector.showRule();
	setUpBug156491();
	IMethod method = this.workingCopies[0].getType("L2").getMethod("test", new String[0]);
	this.resultCollector.showFlavors = PatternLocator.SUPER_INVOCATION_FLAVOR;
	search(method, REFERENCES);
	assertSearchResults(
		"""
			src/other/Test.java void other.Test.testInterface(I) [test()] EXACT_MATCH SUPER INVOCATION
			src/other/Test.java void other.Test.testSuperInvocation(L1) [test()] EXACT_MATCH SUPER INVOCATION
			src/other/Test.java void other.Test.testInvocation(L2) [test()] EXACT_MATCH"""
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
 * bug 160301: [search] too many matches found for method references
 * test Ensure that correct number of method references are found
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=160301"
 */
public void testBug160301() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"""
			public class Test {
				class A {
					void foo() {}
					void bar() {
						foo();
					}
				}
				class B extends A {
					void foo() {}
					void bar() {
						foo();
					}
				}
				class C extends B {
					void method() {
						foo();
					}
				}
			}"""
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
		"""
			package test;
			public class Test {
				class A {
					void foo() {}
				}
				class B extends A {}
				class C extends B {
					void foo() {}
				}
				class D extends C {}
				void a() {
					new A().foo();
				}
				void b() {
					new B().foo();
				}
				void c() {
					new C().foo();
				}
				void d() {
					new D().foo();
				}
			\t
			}"""
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
		"""
			public class Test {
				interface I {
					void foo();
				}
				class A1 implements I {
					public void foo() {}
					void a1() {
						foo();
					}
				}
				class B1 extends A1 {
					void b1() {
						foo();
					}
				}
				class C1 extends B1 {
					public void foo() {}
					void c1() {
						foo();
					}
				}
				abstract class A2 implements I {
					void a2() {
						foo();
					}
				}
				class B2 extends A2 {
					public void foo() {}
					void b2() {
						foo();
					}
				}
				class A3 implements I {
					public void foo() {}
					void a3() {
						foo();
					}
				}
			}"""
	);
	IMethod method = this.workingCopies[0].getType("Test").getType("I").getMethod("foo", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"""
			src/Test.java void Test$A1.a1() [foo()] EXACT_MATCH
			src/Test.java void Test$B1.b1() [foo()] EXACT_MATCH
			src/Test.java void Test$A2.a2() [foo()] EXACT_MATCH
			src/Test.java void Test$B2.b2() [foo()] EXACT_MATCH
			src/Test.java void Test$A3.a3() [foo()] EXACT_MATCH"""
	);
}
public void testBug160301_Abstract() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"""
			public class Test {
				abstract class Abstract {
					abstract void foo();
				}
				class A1 extends Abstract {
					public void foo() {}
					void a1() {
						foo(); // valid match as A1.foo() is the first override in sub-class
					}
				}
				class B1 extends A1 {
					void b1() {
						foo(); // valid match as B1 does not override A.foo()
					}
				}
				class C1 extends B1 {
					public void foo() {}
					void c1() {
						foo(); // invalid match as C1 does override A.foo()
					}
				}
				abstract class A2 extends Abstract {
					void a2() {
						foo(); // valid match as A2 does not override Abstract.foo()
					}
				}
				class B2 extends A2 {
					public void foo() {}
					void b2() {
						foo(); // valid match as B2.foo() is the first override in sub-class
					}
				}
			}"""
	);
	IMethod method = this.workingCopies[0].getType("Test").getType("Abstract").getMethod("foo", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"""
			src/Test.java void Test$A1.a1() [foo()] EXACT_MATCH
			src/Test.java void Test$B1.b1() [foo()] EXACT_MATCH
			src/Test.java void Test$A2.a2() [foo()] EXACT_MATCH
			src/Test.java void Test$B2.b2() [foo()] EXACT_MATCH"""
	);
}
public void testBug160301_Abstract2() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"""
			public class Test {
				abstract class Abstract {
					public abstract void foo();
				}
				abstract class A extends Abstract {
					public abstract void foo();
					void a() {
						foo(); // valid match as A is abstract => does not override Abstract.foo()
					}
				}
				class B extends A {
					public void foo() {}
					void b() {
						foo(); // valid match as B.foo() is the first override in sub-class
					}
				}
				class C extends B {
					public void foo() {}
					void c() {
						foo(); // invalid match as C.foo() overrides Abstract.foo()\s
					}
				}
			}"""
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
		"""
			public class Test {
				abstract class Abstract {
					public abstract void foo();
				}
				class A extends Abstract {
					public void foo() {}
					void a() {
						foo(); // valid match as A.foo() is the first override in sub-class
					}
				}
				abstract class B extends A {
					public abstract void foo();
					void b() {
						foo(); // invalid match as B.foo() is hidden by the override A.foo()
					}
				}
				class C extends B {
					public void foo() {}
					void c() {
						foo(); // invalid match as C.foo() overrides A.foo()
					}
				}
			}"""
	);
	IMethod method = this.workingCopies[0].getType("Test").getType("Abstract").getMethod("foo", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"src/Test.java void Test$A.a() [foo()] EXACT_MATCH"
	);
}

/**
 * bug 160323: [search] TypeNameMatch: support hashCode/equals
 * test Ensure that match equals and hashCode methods return same values than those of stored {@link IType}.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=160323"
 */
public void testBug160323() throws CoreException {
	// Search all type names with TypeNameMatchRequestor
	TypeNameMatchCollector collector = new TypeNameMatchCollector() {
		@Override
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
 * bug 160324: [search] SearchEngine.searchAllTypeNames(char[][], char[][], TypeNameMatchRequestor
 * test Ensure that types found using {@link SearchEngine#searchAllTypeNames(char[][], char[][], IJavaSearchScope, TypeNameMatchRequestor, int, org.eclipse.core.runtime.IProgressMonitor) new API method}
 * 	are the same than with already existing API method using {@link TypeNameRequestor}...
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=160324"
 */
public void testBug160324a() throws CoreException {
	boolean debug = false;
	// Search all type names with new API
	TypeNameMatchCollector collector = new TypeNameMatchCollector() {
		@Override
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
		@Override
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
 * bug 160494: [search] searchAllTypeNames(char[][], char[][],...) fails to find types in default package
 * test Ensure that types of default packge are found when empty package is specified in package lists
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
		@Override
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
 * bug 160854: [search] No type is found using seachAllTypeNames(char[][],char[][],...) methods when no type names is specified
 * test Ensure that types are found when typeNames parameter is null...
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=160854"
 */
public void testBug160854() throws CoreException {
	char[][] packagesList = new char[][] {
			"java.lang".toCharArray()
	};
	// Search all type names with new API
	TypeNameMatchCollector collector = new TypeNameMatchCollector() {
		@Override
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
		"""
			java.lang.CharSequence
			java.lang.Class
			java.lang.CloneNotSupportedException
			java.lang.Comparable
			java.lang.Deprecated
			java.lang.Enum
			java.lang.Error
			java.lang.Exception
			java.lang.IllegalMonitorStateException
			java.lang.InterruptedException
			java.lang.Object
			java.lang.RuntimeException
			java.lang.String
			java.lang.Throwable""",
		requestor
	);
	assertEquals("Found types sounds not to be correct", requestor.toString(), collector.toString());
}

/**
 * bug 161028: [search] NPE on organize imports in TypeNameMatch.equals
 * test Ensure that no NPE may happen calling <code>equals(Object)</code>,
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
 * bug 161190: [search] All type search doesn't find all types
 * test Ensure that access rules does not change searchAllTypeNames results.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=161190"
 */
public void testBug161190() throws CoreException {
	char[][] packagesList = new char[][] {
			"xy".toCharArray()
	};
	// Search all type names with new API
	TypeNameMatchCollector collector = new TypeNameMatchCollector() {
		@Override
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
		"""
			xy.BE_124645
			xy.BX_124645
			xy.BY_124645""",
		requestor
	);
	// Should have same types with these 2 searches
	assertEquals("Found types sounds not to be correct", requestor.toString(), collector.toString());
}

/**
 * bug 163984: [search] no results from SearchEngine.searchAllTypeNames with types in scope
 * test Ensure that types are found even when scope is made of elements
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=163984"
 */
public void testBug163984() throws CoreException {
	// Search all type names with TypeNameMatchRequestor
	TypeNameMatchCollector collector = new TypeNameMatchCollector() {
		@Override
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
 * bug 164121: [search] Misses declarations of method parameters
 * test Ensure that param declaration are correctly found by search engine
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=164121"
 */
private void setUpBug164121() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/A.java",
		"""
			class A {
			     int x(int param) {
			         param = 2 + 2;
			         int x = param + 2;
			         return param - x;
			     }
			}
			"""
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
		"""
			src/A.java int A.x(int).param [param] EXACT_MATCH
			src/A.java int A.x(int) [param] EXACT_MATCH
			src/A.java int A.x(int) [param] EXACT_MATCH
			src/A.java int A.x(int) [param] EXACT_MATCH"""
	);
}

/**
 * bug 164791: [search] Type reference reports anonymous type in invalid class file
 * test Ensure that match on anonymous type in local type is well reported
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=164791"
 */
public void testBug164791() throws CoreException {
	IType type = getClassFile("JavaSearchBugs", "lib/b164791.jar", "pack", "ELPM.class").getType();
	JavaSearchResultCollector collector = new JavaSearchResultCollector() {
		@Override
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
 * bug 166348: [search] Stack trace console resolves wrong source
 * test Ensure that only type with same qualification is found in class files
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
 * bug 167190: [search] TypeNameMatchRequestorWrapper causing ClassCastException
 * test Ensure that types are found even when scope is not a {@link org.eclipse.jdt.internal.core.search.JavaSearchScope}
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=167190"
 */
public void testBug167190_Parallel() throws CoreException, JavaModelException {
	bug167190(true);
}

public void testBug167190() throws CoreException, JavaModelException {
	bug167190(false);
}

private void bug167190(boolean parallel) throws JavaModelException {

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
		@Override
		public boolean isParallelSearchSupported() {
			return parallel;
		}
	};
	// Search all type names with TypeNameMatchRequestor
	TypeNameMatchCollector collector = new TypeNameMatchCollector() {
		@Override
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
	assertEquals(String.format("Found types sounds not to be correct [Parallel=%s]", parallel), requestor.toString(), collector.toString());
}

/**
 * bug 178596: [search] Search for method references does not find references to interface method
 * test Ensure that searching method reference finds the interface method reference
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=178596"
 */
public void testBug178596() throws CoreException {
	this.workingCopies = new ICompilationUnit[5];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/ClassA.java",
		"""
			public class ClassA implements InterfaceA {
			    public void setValue(int aValue) {
			    }
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/ClassB.java",
		"public class ClassB extends ClassA implements InterfaceB {}\n"
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/InterfaceA.java",
		"""
			public interface InterfaceA {
			    public void setValue(int aValue);
			}
			"""
	);
	this.workingCopies[3] = getWorkingCopy("/JavaSearchBugs/src/InterfaceB.java",
		"public interface InterfaceB extends InterfaceA {}\n"
	);
	this.workingCopies[4] = getWorkingCopy("/JavaSearchBugs/src/Main.java",
		"""
			public class Main {
			    public static void main(String[] args) {
			        new Main().run();
			    }
			    private void run() {
			        InterfaceB anB = new ClassB();
			        anB.setValue(123);
			    }
			}
			"""
	);
	JavaSearchResultCollector testCollector = new JavaSearchResultCollector() {
		@Override
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
 * bug 178847 [search] Potential matches found when searching references to IJavaElement#getResource()
 * test Ensure that accurate matches are found
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=178847"
 */
public void testBug178847() throws CoreException {
	try {
		createJavaProject("P1", new String[] {"src" }, "bin");
		createFolder("/P1/src/p");
		createFile(
			"/P1/src/p/X.java",
			"""
				package p;
				public class X{
				}"""
		);
		createFile(
			"/P1/src/p/Y.java",
			"""
				package p;
				public class Y extends X {
				  public static void foo() {}
				}"""
		);
		getProject("P1").build(IncrementalProjectBuilder.FULL_BUILD, null);
		deleteFile("/P1/bin/p/X.class");
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB", "/P1/bin"}, "");
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy("/P2/Test1.java",
			"""
				public class Test1 {
				  void bar() {
				    p.Y.foo();
				    new p.X();
				  }
				}"""
		);
		this.workingCopies[1] = getWorkingCopy("/P2/Test2.java",
			"""
				public class Test2 {
				  void foo() {}
				  void bar() {
				    foo();
				  }
				}"""
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
 * bug 181488 [index] Lots of unbuffered sequential reads in DiskIndex
 * test Ensure that indexing does not happen while reopening workspace (see bug 195091)
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=181488"
 */
public void testBug181488a() throws CoreException {
	waitUntilIndexesReady();
	IndexManager manager = JavaModelManager.getIndexManager();
	Index index = manager.getIndex(JAVA_PROJECT.getPath(), true, false);
	File indexFile = index.getIndexFile();
	long lastModified = 0;
	simulateExit();
	try {
		lastModified = indexFile.lastModified();
	} finally {
		simulateRestart();
	}
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
 * bug 185452 [search] for all packages seems hung
 * test Ensure that all package declarations are found only once
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
		"lib/b317264 b317264\n" +
		"lib/b327654 b327654\n" +
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
		"src/b573388 b573388\n" +
		"src/b81556 b81556\n" +
		"src/b81556/a b81556.a\n" +
		"src/b86380 b86380\n" +
		"src/b95794 b95794",
		packageCollector);
}



/**
 * bug 194185 [search] for package declarations finds also sub-packages
 * test Ensure that exact package is found when no
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
 * bug 195489: [search] References not found while using SearchEngine.searchDeclarationsOfReferencedTypes
 * test Verify that the type declaration match is always outside the javadoc and that the workaround described in bug 108053 works well
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=108053"
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=195489"
 */
public void testBug195489a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b195489/Test.java",
		"""
			package b195489;
			/**
			 * @see Ref
			 */
			public class Test {
				public Ref ref;
				public Ref getRef() {
					return this.ref;
				}
			}
			class Ref {}"""
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
		"""
			package b195489;
			public class Test {
				public Ref ref;
				/**
				 * @see Ref
				 */
				public Ref getRef() {
					return this.ref;
				}
			}
			class Ref {}"""
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
		"""
			package b195489;
			/**
			 * @see Ref
			 */
			public class Test {
			}
			class Ref {}"""
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
		"""
			package b195489;
			/**
			 * @see Ref
			 */
			public class Test {
			}
			class Ref {}"""
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
 * bug 196339: [search] SearchEngine not returning correct result
 * test 1) That potential match are now well found while searching for implementors
 * 			2) That there's a workaround for this problem
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=196339"
 */
public void testBug196339() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b196339/x/y/z/Test.java",
		"""
			package b196339.xy.y.z;
			import a.b.c.Foo196339;
			public class Test implements Foo196339 {
			}
			"""
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
		"""
			package b196339.xy.y.z;
			import a.b.c.Foo196339;
			public class Test1 implements Foo196339 {
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b196339/x/y/z/Test2.java",
		"""
			package b196339.xy.y.z;
			import a.b.c.*;
			public class Test2 implements Foo196339 {
			}
			"""
	);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/b196339/x/y/z/Test3.java",
		"""
			package b196339.xy.y.z;
			public class Test3 implements a.b.c.Foo196339 {
			}
			"""
	);

	final String qualifiedType = "a.b.c.Foo196339";
	JavaSearchResultCollector collector = new JavaSearchResultCollector() {
		@Override
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
		"""
			src/b196339/x/y/z/Test1.java b196339.x.y.z.Test1 [Foo196339]
			src/b196339/x/y/z/Test2.java b196339.x.y.z.Test2 [Foo196339]
			src/b196339/x/y/z/Test3.java b196339.x.y.z.Test3 [a.b.c.Foo196339]""",
		collector
	);
}

/**
 * bug 199004: [search] Java Search in 'JRE libraries' finds matches in Application Libraries
 * test 1) That only match in system libraries are returned when SYSTEM_LIBRARIES is used in scope
 * 			2) That only match outside system libraries are returned when SYSTEM_LIBRARIES is NOT used in scope
 * 			3) That match in system libraries are returned when no mask is used in scope
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=199004"
 */
public void testBug199004_SystemLibraries() throws CoreException {
	DefaultContainerInitializer intializer = new DefaultContainerInitializer(new String[] {"JavaSearchBugs", "/JavaSearchBugs/lib/b199004.jar"}) {
		@Override
		protected DefaultContainer newContainer(char[][] libPaths) {
			return new DefaultContainer(libPaths) {
				@Override
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
		@Override
		protected DefaultContainer newContainer(char[][] libPaths) {
			return new DefaultContainer(libPaths) {
				@Override
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
		@Override
		protected DefaultContainer newContainer(char[][] libPaths) {
			return new DefaultContainer(libPaths) {
				@Override
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
		@Override
		protected DefaultContainer newContainer(char[][] libPaths) {
			return new DefaultContainer(libPaths) {
				@Override
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
 * bug 200064: [search] ResourceException while searching for method reference
 * test Ensure that indexing still works properly after close/restart
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
 * bug 201064: [search] SearchEngine.searchAllTypeNames(..) does not find CamelCase match
 * test Ensure that indexing still works properly after close/restart
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=201064"
 */
public void testBug201064a_CamelCase() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CCase", SearchPattern.R_CAMELCASE_MATCH, collector);
	assertSearchResults(
		"""
			CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CamelCaseEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CamelCasexxEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CxxCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CxxxxCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]""",
		collector
	);
}
public void testBug201064b_CamelCase() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CaCase", SearchPattern.R_CAMELCASE_MATCH, collector);
	assertSearchResults(
		"""
			CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CamelCaseEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CamelCasexxEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]""",
		collector
	);
}
public void testBug201064c_CamelCase() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CamelCase", SearchPattern.R_CAMELCASE_MATCH, collector);
	assertSearchResults(
		"""
			CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CamelCaseEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CamelCasexxEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]""",
		collector
	);
}
public void testBug201064d_CamelCase() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CC", SearchPattern.R_CAMELCASE_MATCH, collector);
	assertSearchResults(
		"""
			CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CamelCaseEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CamelCasexxEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CxxCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CxxxxCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]""",
		collector
	);
}
public void testBug201064e_CamelCase() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CaC", SearchPattern.R_CAMELCASE_MATCH, collector);
	assertSearchResults(
		"""
			CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CamelCaseEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CamelCasexxEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]""",
		collector
	);
}
public void testBug201064f_CamelCase() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CamelC", SearchPattern.R_CAMELCASE_MATCH, collector);
	assertSearchResults(
		"""
			CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CamelCaseEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CamelCasexxEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]""",
		collector
	);
}
public void testBug201064g_CamelCase() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CCa", SearchPattern.R_CAMELCASE_MATCH, collector);
	assertSearchResults(
		"""
			CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CamelCaseEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CamelCasexxEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CxxCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CxxxxCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]""",
		collector
	);
}
public void testBug201064h_CamelCase() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CaCa", SearchPattern.R_CAMELCASE_MATCH, collector);
	assertSearchResults(
		"""
			CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CamelCaseEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CamelCasexxEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]""",
		collector
	);
}
public void testBug201064i_CamelCase() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CamelCa", SearchPattern.R_CAMELCASE_MATCH, collector);
	assertSearchResults(
		"""
			CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CamelCaseEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CamelCasexxEntry (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]""",
		collector
	);
}
// Same tests using SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH
public void testBug201064a_CamelCaseSamePartCount() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CCase", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, collector);
	assertSearchResults(
		"""
			CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CxxCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CxxxxCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]""",
		collector
	);
}
public void testBug201064b_CamelCaseSamePartCount() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CaCase", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, collector);
	assertSearchResults(
		"""
			CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]""",
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
		"""
			CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CxxCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CxxxxCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]""",
		collector
	);
}
public void testBug201064e_CamelCaseSamePartCount() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CaC", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, collector);
	assertSearchResults(
		"""
			CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]""",
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
		"""
			CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CxxCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CxxxxCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]""",
		collector
	);
}
public void testBug201064h_CamelCaseSamePartCount() throws CoreException {
	TypeNameMatchCollector collector = new TypeNameMatchCollector();
	searchAllTypeNames("CaCa", SearchPattern.R_CAMELCASE_SAME_PART_COUNT_MATCH, collector);
	assertSearchResults(
		"""
			CamelCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCase (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]
			CatCasexx (not open) [in CamelCase.java [in b201064 [in src [in JavaSearchBugs]]]]""",
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
 * bug 204652 "Open Type": ClassCastException in conjunction with a class folder
 * test Ensure that no ClassCastException is thrown for a library folder with a jar like name
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
 * bug 207657: [search] Exception when refactoring member type to top-level.
 * test Ensure that searching method reference does not find wrong interface call
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=207657"
 */
public void testBug207657() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Relationship.java",
		"""
			package test;
			public class Relationship {
			    static public class End extends ConnectionEnd<Visitor> {
			        public void accept(Visitor visitor) {
			            visitor.visitRelationshipEnd(this);
			        }
			    }
			}
			interface Visitor {
			    boolean visitRelationshipEnd(Relationship.End end);
			    boolean visitAssociationEnd(Association.End end);
			}
			abstract class ConnectionEnd<V extends Visitor> {
			    public abstract void accept( V visitor );
			}
			class Association extends Relationship {
			    static public class RelEnd extends Relationship.End {
			        public void accept(Visitor visitor) {
			            visitor.visitAssociationEnd(this);
			        }
			    }
			}
			"""
	);
	IType type = this.workingCopies[0].getType("Relationship").getType("End");
	searchDeclarationsOfSentMessages(type, this.resultCollector);
	assertSearchResults(
		"src/test/Relationship.java boolean test.Visitor.visitRelationshipEnd(Relationship.End) [visitRelationshipEnd(Relationship.End end)] EXACT_MATCH"
	);
}

/**
 * bug 209054: [search] for references to method finds wrong interface call
 * test Ensure that searching method reference does not find wrong interface call
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=209054"
 */
public void testBug209054() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/xy/Try.java",
		"""
			package xy;
			public class Try implements IReferenceUpdating {
			        IMovePolicy fInter;
			        boolean canDo() { // find references
			                return fInter.canDo(); // not a reference
			        }
			}
			interface IMovePolicy extends IReferenceUpdating {
			        boolean canDo();
			}
			interface IReferenceUpdating {}"""
	);
	IMethod method = this.workingCopies[0].getType("Try").getMethod("canDo", new String[0]);
	search(method, REFERENCES);
	assertSearchResults("");
}

/**
 * bug 209778: [search] TypeReferenceMatch#getOtherElements() fails for match in annotation
 * test Ensure that the local element is no longer a local variable
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=209778"
 */
public void testBug209778() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/xy/Try.java",
		"""
			package xy;
			
			public class Try {
			        void tryB(int tryKind) {
			                @Constants(Try.class) int tryCopy, tryCopy2= tryKind;
			        }
			        @Constants(value= Try.class) Object fTryA, fTryB;
			}
			
			@interface Constants {
			        Class<?> value();
			}"""
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
 * bug 209996: [search] Add a way to access the most local enclosing annotation for reference search matches
 * test Verify the behavior of the new Search API {@link ReferenceMatch#getLocalElement()}
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=209996"
 */
public void testBug209996a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
		"""
			package test;
			public class Test {
			    void method() {
			        @Annot(clazz=Test.class) int x;
			    }
			}
			@interface Annot {
			    Class clazz();
			}
			"""
	);
	IType type = this.workingCopies[0].getType("Test");
	ReferenceCollector collector = new ReferenceCollector();
	collector.showSelection();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/test/Test.java void test.Test.method() [        @Annot(clazz=!|Test|!.class) int x;]+[@Annot on x]",
		collector
	);
}
public void testBug209996b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
		"""
			package test;
			public class Test {
			        @Deprecated foo() {}
			}
			"""
	);
	ReferenceCollector collector = new ReferenceCollector();
	collector.showSelection();
	search("Deprecated", TYPE, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/test/Test.java void test.Test.foo() [        @!|Deprecated|! foo() {}]+[@Deprecated on foo]",
		collector
	);
}
public void testBug209996_c5() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/comment5/Ref.java",
		"""
			package comment5;
			public class Ref {
			    void doA(Ref ref) {}
			    void doB(List<Ref> ref) {}
			    void doC(@Tag(Ref.class) Ref ref) {}
			    void dontD(@Tag(Ref.class) Object ref) {}
			}
			
			@interface Tag {
			    Class value();
			}
			class List<T> {
			}
			"""
	);
	IType type = this.workingCopies[0].getType("Ref");
	ReferenceCollector collector = new ReferenceCollector();
	collector.showSelection();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"""
			src/comment5/Ref.java void comment5.Ref.doA(Ref) [    void doA(!|Ref|! ref) {}]+[ref]
			src/comment5/Ref.java void comment5.Ref.doB(List<Ref>) [    void doB(List<!|Ref|!> ref) {}]+[ref]
			src/comment5/Ref.java void comment5.Ref.doC(Ref) [    void doC(@Tag(!|Ref|!.class) Ref ref) {}]+[@Tag on ref]
			src/comment5/Ref.java void comment5.Ref.doC(Ref) [    void doC(@Tag(Ref.class) !|Ref|! ref) {}]+[ref]
			src/comment5/Ref.java void comment5.Ref.dontD(Object) [    void dontD(@Tag(!|Ref|!.class) Object ref) {}]+[@Tag on ref]""",
		collector
	);
}
public void testBug209996_c10() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/comment10/Ref.java",
		"""
			package comment10;
			@Num(number= Num.CONST)
			@interface Num {
			    public static final int CONST= 42;
			    int number();
			}
			"""
	);
	IField field = this.workingCopies[0].getType("Num").getField("CONST");
	ReferenceCollector collector = new ReferenceCollector();
	collector.showSelection();
	search(field, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/comment10/Ref.java comment10.Num [@Num(number= Num.!|CONST|!)]+[@Num on Num]",
		collector
	);
}
public void testBug209996_c22_3() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/comment22/Test.java",
		"""
			package comment22;
			public class Test {
			    @Tag Test test1, test2, test3;
			    void method() {
			        @Tag Test local= null;
			        @Tag Test local1, local2, local3;
			    }
			}
			@interface Tag {}
			"""
	);
	IType type = this.workingCopies[0].getType("Tag");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	collector.showSelection();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"""
			src/comment22/Test.java comment22.Test.test1 [    @!|Tag|! Test test1, test2, test3;]+[@Tag on test1]+[@Tag on test2,@Tag on test3]
			src/comment22/Test.java void comment22.Test.method() [        @!|Tag|! Test local= null;]+[@Tag on local]
			src/comment22/Test.java void comment22.Test.method() [        @!|Tag|! Test local1, local2, local3;]+[@Tag on local1]+[@Tag on local2,@Tag on local3]""",
		collector
	);
}
public void testBug209996_c22_4() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/Test.java",
		"""
			package test;
			public class TestMethodReference {
			    @Annot(clazz = test.Test.class) int x, y;
			}
			@interface Annot {
			    Class clazz();
			}
			"""
	);
	IType type = this.workingCopies[0].getType("Test");
	TypeReferenceCollector collector = new TypeReferenceCollector();
	collector.showSelection();
	search(type, REFERENCES, EXACT_RULE, getJavaSearchScope(), collector);
	assertSearchResults(
		"src/test/Test.java test.TestMethodReference.x [    @Annot(clazz = !|test.Test|!.class) int x, y;]+[@Annot on x]+[@Annot on y]",
		collector
	);
}

/**
 * bug 210689: [search] Type references are not found in import declarations when JUnit tests only use working copies
 * test Ensure that import references are found when searching on working copies not written on disk
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=210689"
 */
public void testBug210689() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test210689.java",
		"package pack;\n" +
		"public class Test210689 {}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/X.java",
		"""
			package test;
			import pack.Test210689;
			public class X extends Test210689 {}
			"""
	);
	search(this.workingCopies[0].getType("Test210689"), REFERENCES);
	assertSearchResults(
		"src/test/X.java [pack.Test210689] EXACT_MATCH\n" +
		"src/test/X.java test.X [Test210689] EXACT_MATCH"
	);
}

/**
 * bug 210567: [1.5][search] Parameterized type reference not found when used in type parameter bounds
 * test Ensure that all type references are found when used in type variables
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=210567"
 */
public void testBug210567() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/generics/Generic.java",
		"""
			package generics;
			import java.io.Serializable;
			import type.def.Types;
			public class Generic<T extends Types, U extends Types & Comparable<Types> & Serializable, V extends A<? super Types>> {
				Generic<? extends Types, ?, ?> field;
				Comparable<String> comp;
				Class<? extends Exception> clazz;
			}
			
			class A<R> {}"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/type/def/Types.java",
		"""
			package type.def;
			public class Types {
			}
			"""
	);
	this.resultCollector.showSelection();
	search("*", TYPE, REFERENCES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"""
			src/generics/Generic.java [import !|java.io.Serializable|!;] EXACT_MATCH
			src/generics/Generic.java [import !|type.def.Types|!;] EXACT_MATCH
			src/generics/Generic.java generics.Generic [public class Generic<T extends !|Types|!, U extends Types & Comparable<Types> & Serializable, V extends A<? super Types>> {] EXACT_MATCH
			src/generics/Generic.java generics.Generic [public class Generic<T extends Types, U extends !|Types|! & Comparable<Types> & Serializable, V extends A<? super Types>> {] EXACT_MATCH
			src/generics/Generic.java generics.Generic [public class Generic<T extends Types, U extends Types & !|Comparable|!<Types> & Serializable, V extends A<? super Types>> {] EXACT_MATCH
			src/generics/Generic.java generics.Generic [public class Generic<T extends Types, U extends Types & Comparable<!|Types|!> & Serializable, V extends A<? super Types>> {] EXACT_MATCH
			src/generics/Generic.java generics.Generic [public class Generic<T extends Types, U extends Types & Comparable<Types> & !|Serializable|!, V extends A<? super Types>> {] EXACT_MATCH
			src/generics/Generic.java generics.Generic [public class Generic<T extends Types, U extends Types & Comparable<Types> & Serializable, V extends !|A|!<? super Types>> {] EXACT_MATCH
			src/generics/Generic.java generics.Generic [public class Generic<T extends Types, U extends Types & Comparable<Types> & Serializable, V extends A<? super !|Types|!>> {] EXACT_MATCH
			src/generics/Generic.java generics.Generic.field [	!|Generic|!<? extends Types, ?, ?> field;] EXACT_MATCH
			src/generics/Generic.java generics.Generic.field [	Generic<? extends !|Types|!, ?, ?> field;] EXACT_MATCH
			src/generics/Generic.java generics.Generic.comp [	!|Comparable|!<String> comp;] EXACT_MATCH
			src/generics/Generic.java generics.Generic.comp [	Comparable<!|String|!> comp;] EXACT_MATCH
			src/generics/Generic.java generics.Generic.clazz [	!|Class|!<? extends Exception> clazz;] EXACT_MATCH
			src/generics/Generic.java generics.Generic.clazz [	Class<? extends !|Exception|!> clazz;] EXACT_MATCH"""
	);
}

/**
 * bug 210691: [search] Type references position invalid in import references when using "*" pattern
 * test Ensure that all qualified type reference in import references is selected when using "*" pattern
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=210691"
 */
public void testBug210691() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test.java",
		"package pack;\n" +
		"public class Test {}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/Ref.java",
		"""
			package test;
			import pack.Test;
			public class Ref {
				Test test;
			}
			"""
	);
	this.resultCollector.showSelection();
	search("*", TYPE, REFERENCES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/test/Ref.java [import !|pack.Test|!;] EXACT_MATCH\n" +
		"src/test/Ref.java test.Ref.test [	!|Test|! test;] EXACT_MATCH"
	);
}

/**
 * bug 211366: [search] does not return references to types in binary classes
 * test Ensure that annotations references are found in a class file without source
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=211366"
 */
public void testBug211366() throws CoreException {
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b211366.jar", false);
	try {
		IType type = getClassFile("JavaSearchBugs", "lib/b211366.jar", "test", "Bug.class").getType();
		this.resultCollector.showMatchKind();
		search(type, REFERENCES);
		assertSearchResults(
			"""
				TypeReferenceMatch: lib/b211366.jar pack.Test [No source] EXACT_MATCH
				TypeReferenceMatch: lib/b211366.jar pack.TestInner$Member [No source] EXACT_MATCH
				TypeReferenceMatch: lib/b211366.jar void pack.TestMembers.method(java.lang.Object, java.lang.String) [No source] EXACT_MATCH
				TypeReferenceMatch: lib/b211366.jar pack.TestMembers.field [No source] EXACT_MATCH"""
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
			"""
				TypeReferenceMatch: lib/b211366.jar pack.Test [No source] EXACT_MATCH
				TypeReferenceMatch: lib/b211366.jar pack.TestInner$Member [No source] EXACT_MATCH
				TypeReferenceMatch: lib/b211366.jar void pack.TestMembers.method(java.lang.Object, java.lang.String) [No source] EXACT_MATCH
				TypeReferenceMatch: lib/b211366.jar pack.TestMembers.field [No source] EXACT_MATCH
				TypeDeclarationMatch: lib/b211366.jar test.Bug [No source] EXACT_MATCH"""
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
			"""
				FieldDeclarationMatch: lib/b211366.jar pack.TestMembers.field [No source] EXACT_MATCH
				MethodDeclarationMatch: lib/b211366.jar void pack.TestMembers.method(java.lang.Object, java.lang.String) [No source] EXACT_MATCH
				TypeDeclarationMatch: lib/b211366.jar pack.TestInner$Member [No source] EXACT_MATCH
				TypeReferenceMatch: lib/b211366.jar pack.Test [No source] EXACT_MATCH
				TypeReferenceMatch: lib/b211366.jar pack.Test [No source] EXACT_MATCH
				TypeReferenceMatch: lib/b211366.jar pack.TestInner$Member [No source] EXACT_MATCH
				TypeReferenceMatch: lib/b211366.jar pack.TestInner$Member [No source] EXACT_MATCH
				TypeReferenceMatch: lib/b211366.jar pack.TestMembers.field [No source] EXACT_MATCH
				TypeReferenceMatch: lib/b211366.jar pack.TestMembers.field [No source] EXACT_MATCH
				TypeReferenceMatch: lib/b211366.jar void pack.TestMembers.method(java.lang.Object, java.lang.String) [No source] EXACT_MATCH
				TypeReferenceMatch: lib/b211366.jar void pack.TestMembers.method(java.lang.Object, java.lang.String) [No source] EXACT_MATCH"""
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/b211366.jar"));
	}
}

/**
 * bug 211857: [search] Standard annotations references not found on binary fields and methods when no source is attached
 * test Ensure that annotations references on fields and methods are found in a class file without source
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=211857"
 */
public void testBug211857() throws CoreException {
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b211857.jar", false);
	try {
		IType type = getClassFile("JavaSearchBugs", getExternalJCLPathString("1.5"), "java.lang", "Deprecated.class").getType();
		search(type, REFERENCES);
		assertSearchResults(
			"""
				lib/b211857.jar pack.Test [No source] EXACT_MATCH
				lib/b211857.jar void pack.TestMembers.method(java.lang.Object, java.lang.String) [No source] EXACT_MATCH
				lib/b211857.jar pack.TestMembers.field [No source] EXACT_MATCH"""
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/b211857.jar"));
	}
}

/**
 * bug 211872: [search] References to annotations not found in class file without source
 * test Ensure that annotations references are found in the specific class file without source
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=211872"
 */
public void testBug211872_ns() throws CoreException, IOException {
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b211872_ns.jar", false);
	try {
		IType type = getClassFile("JavaSearchBugs", "lib/b211872_ns.jar", "test", "Bug.class").getType();
		search(type, REFERENCES);
		assertSearchResults(
			"""
				lib/b211872_ns.jar pack.Test [No source] EXACT_MATCH
				lib/b211872_ns.jar pack.TestMembers$Member [No source] EXACT_MATCH
				lib/b211872_ns.jar void pack.TestMembers.method(java.lang.Object, java.lang.String) [No source] EXACT_MATCH
				lib/b211872_ns.jar pack.TestMembers.field [No source] EXACT_MATCH"""
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
			"""
				lib/b211872_ws.jar pack.Test EXACT_MATCH
				lib/b211872_ws.jar pack.Test EXACT_MATCH
				lib/b211872_ws.jar pack.TestMembers.field EXACT_MATCH
				lib/b211872_ws.jar void pack.TestMembers.method(java.lang.Object, java.lang.String) EXACT_MATCH
				lib/b211872_ws.jar pack.TestMembers$Member EXACT_MATCH"""
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/b211872_ws.jar"));
	}
}

/**
 * bug 181981: [model] Linked Source Folders with Parallel package structure do not work with occurrences
 * test Ensure that source folder nesting doesn't cause non existing elements to be returned
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=181981"
 */
public void testBug181981() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P", new String[] { "", "src"}, new String[] {"JCL_LIB"}, null, null, "bin", null, null, new String[][] {new String[] {"src/"}, new String[0]}, "1.4");
		createFolder("/P/p1");
		createFile(
			"/P/p1/X.java",
			"""
				package p1;
				public class X {
				  public void foo() {}
				}"""
		);
		createFile(
			"/P/p1/Y.java",
			"""
				package p1;
				public class Y {
				  public void bar(X x) {
				    x.foo();
				  }
				}"""
		);
		createFolder("/P/src/p2");
		createFile(
			"/P/src/p2/Z.java",
			"""
				package p2;
				public class Z {
				  public void bar(p1.X x) {
				    x.foo();
				  }
				}"""
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
 * bug 216875: [search] Field- and LocalVariableReferenceMatch confuse read/write for field access on LHS
 * test Ensure that read access is found inside qualified name reference
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=216875"
 */
public void testBug216875() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"""
			public class Test {
			    int fField;
			    void m() {
			        Test test; // refs to test
			        test = new Test();
			        test.fField = 42; // match for t is writeAccess, should be readAccess
			    }
			
			}
			"""
	);
	this.resultCollector.showSelection();
	ILocalVariable variable = selectLocalVariable(this.workingCopies[0], "test");
	search(variable, READ_ACCESSES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.m() [        !|test|!.fField = 42; // match for t is writeAccess, should be readAccess] EXACT_MATCH"
	);
}
public void testBug216875b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"""
			public class Test {
			    S fWrapped; // refs to fWrapped
			    void wrapper() {
			        fWrapped.fField = 12; // match for fWrapped is writeAccess
			    }
			}
			class S {
				int fField;\
			}"""
	);
	this.resultCollector.showSelection();
	IField field = this.workingCopies[0].getType("Test").getField("fWrapped");
	search(field, READ_ACCESSES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.wrapper() [        !|fWrapped|!.fField = 12; // match for fWrapped is writeAccess] EXACT_MATCH"
	);
}
public void testBug216875c() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test.java",
		"""
			package pack;
			public class Test {
				public int field;
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/X.java",
		"""
			package test;
			import pack.Test;
			public class X {
				void foo(Test t1, Test t2) {
					t1 = t2;
					t1.field = t1.field;
					t2.field = t1.field;
				}
			}
			"""
	);
	this.resultCollector.showSelection();
	ILocalVariable variable = selectLocalVariable(this.workingCopies[1], "t1");
	search(variable, READ_ACCESSES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"""
			src/test/X.java void test.X.foo(Test, Test) [		!|t1|!.field = t1.field;] EXACT_MATCH
			src/test/X.java void test.X.foo(Test, Test) [		t1.field = !|t1|!.field;] EXACT_MATCH
			src/test/X.java void test.X.foo(Test, Test) [		t2.field = !|t1|!.field;] EXACT_MATCH"""
	);
}
public void testBug216875d() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test.java",
		"""
			package pack;
			public class Test {
				public int field;
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/X.java",
		"""
			package test;
			import pack.Test;
			public class X {
				Test t1, t2;
				void foo() {
					t1 = t2;
					t1.field = t1.field;
					t2.field = t1.field;
				}
			}
			"""
	);
	this.resultCollector.showSelection();
	IField field = this.workingCopies[1].getType("X").getField("t1");
	search(field, READ_ACCESSES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"""
			src/test/X.java void test.X.foo() [		!|t1|!.field = t1.field;] EXACT_MATCH
			src/test/X.java void test.X.foo() [		t1.field = !|t1|!.field;] EXACT_MATCH
			src/test/X.java void test.X.foo() [		t2.field = !|t1|!.field;] EXACT_MATCH"""
	);
}
public void testBug216875e() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test.java",
		"""
			package pack;
			public class Test {
				public int field;
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/X.java",
		"""
			package test;
			import pack.Test;
			public class X {
				void foo(Test t1, Test t2) {
					t1 = t2;
					t1.field = t1.field;
					t2.field = t1.field;
				}
			}
			"""
	);
	this.resultCollector.showSelection();
	ILocalVariable variable = selectLocalVariable(this.workingCopies[1], "t1");
	search(variable, WRITE_ACCESSES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/test/X.java void test.X.foo(Test, Test) [		!|t1|! = t2;] EXACT_MATCH"
	);
}
public void testBug216875f() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test.java",
		"""
			package pack;
			public class Test {
				public int field;
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/X.java",
		"""
			package test;
			import pack.Test;
			public class X {
				Test t1, t2;
				void foo() {
					t1 = t2;
					t1.field = t1.field;
					t2.field = t1.field;
				}
			}
			"""
	);
	this.resultCollector.showSelection();
	IField field = this.workingCopies[1].getType("X").getField("t1");
	search(field, WRITE_ACCESSES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/test/X.java void test.X.foo() [		!|t1|! = t2;] EXACT_MATCH"
	);
}
public void testBug216875g() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test.java",
		"""
			package pack;
			public class Test {
				public int field;
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/X.java",
		"""
			package test;
			import pack.Test;
			public class X {
				void foo(Test t1, Test t2) {
					t1 = t2;
					t1.field = t1.field;
					t2.field = t1.field;
				}
			}
			"""
	);
	this.resultCollector.showSelection();
	this.resultCollector.showAccess();
	this.resultCollector.showAccuracy(false);
	ILocalVariable variable = selectLocalVariable(this.workingCopies[1], "t1");
	search(variable, REFERENCES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"""
			src/test/X.java void test.X.foo(Test, Test) [		!|t1|! = t2;] WRITE ACCESS
			src/test/X.java void test.X.foo(Test, Test) [		!|t1|!.field = t1.field;] READ ACCESS
			src/test/X.java void test.X.foo(Test, Test) [		t1.field = !|t1|!.field;] READ ACCESS
			src/test/X.java void test.X.foo(Test, Test) [		t2.field = !|t1|!.field;] READ ACCESS"""
	);
}
public void testBug216875h() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test.java",
		"""
			package pack;
			public class Test {
				public int field;
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/X.java",
		"""
			package test;
			import pack.Test;
			public class X {
				Test t1, t2;
				void foo() {
					t1 = t2;
					t1.field = t1.field;
					t2.field = t1.field;
				}
			}
			"""
	);
	this.resultCollector.showSelection();
	this.resultCollector.showAccess();
	this.resultCollector.showAccuracy(false);
	IField field = this.workingCopies[1].getType("X").getField("t1");
	search(field, REFERENCES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"""
			src/test/X.java void test.X.foo() [		!|t1|! = t2;] WRITE ACCESS
			src/test/X.java void test.X.foo() [		!|t1|!.field = t1.field;] READ ACCESS
			src/test/X.java void test.X.foo() [		t1.field = !|t1|!.field;] READ ACCESS
			src/test/X.java void test.X.foo() [		t2.field = !|t1|!.field;] READ ACCESS"""
	);
}

/**
 * bug 218397: [search] Can't find references of generic local class.
 * test Ensure that the generic local class reference is well found (no CCE)
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=218397"
 */
public void testBug218397() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Bug.java",
		"""
			class Bug{
				{
					class Inner<Type extends Number> {
						Row field;//LINE 3
						class Row{}
					}
				}
			}"""
	);
	this.resultCollector.showSelection();
	IType type = selectType(this.workingCopies[0], "Row");
	search(type, REFERENCES, getJavaSearchWorkingCopiesScope(), this.resultCollector);
	assertSearchResults(
		"src/Bug.java Bug.{}:Inner#1.field [			!|Row|! field;//LINE 3] EXACT_MATCH"
	);
}

/**
 * bug 221081: [search] Java Search should default to widest scope
 * test Ensure that user can search to type/method/field references/declarations in only
 * 	one call to SearchEngine
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=221081"
 */
public void testBug221081() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"""
			public class Test {
				Test test;
				void test(Test test) {
					if (test == this.test) {
						//
					}
				}
			}
			"""
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
		"""
			src/Test.java Test [public class !|Test|! {] EXACT_MATCH
			src/Test.java Test.test [	Test !|test|!;] EXACT_MATCH
			src/Test.java Test.test [	!|Test|! test;] EXACT_MATCH
			src/Test.java void Test.test(Test) [	void !|test|!(Test test) {] EXACT_MATCH
			src/Test.java void Test.test(Test) [	void test(!|Test|! test) {] EXACT_MATCH
			src/Test.java void Test.test(Test) [		if (test == this.!|test|!) {] EXACT_MATCH"""
	);
}

/**
 * bug 221110: [search] NPE at org.eclipse.jdt.internal.compiler.util.SimpleLookupTable.removeKey
 * test Ensure that no NPE occurs while searching for reference to generic class
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
		"src/Y.java Y [public class Y<T extends !|X|!<?>> {] ERASURE_MATCH"
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
		"src/Z.java Z [public class Z<T extends X<?> & !|I|!<?>> {] ERASURE_MATCH"
	);
}

/**
 * bug 221065: [search] Search still finds overridden method
 * test Ensure that correct number of method references are found
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=221065"
 */
public void testBug221065() throws CoreException {
	this.resultCollector.showRule();
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"""
			public class Test {
				abstract class A {
					void foo() {}
					void bar() {
						foo();
					}
				}
				class B extends A {
					void foo() {}
					void bar() {
						foo();
					}
				}
				class C extends B {
					void method() {
						foo();
					}
				}
			}"""
	);
	IMethod method = this.workingCopies[0].getType("Test").getType("A").getMethod("foo", new String[0]);
	search(method, REFERENCES);
	assertSearchResults(
		"src/Test.java void Test$A.bar() [foo()] EXACT_MATCH"
	);
}

/**
 * bug 222284: [search] ZipException while searching if linked jar doesn't exist any longer
 * test Ensure that no exception is raised while searching for a type of the missing jar file
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
			"""
				package pack;
				public class Ref {
				}""",
			};
		createJar(pathsAndContents, jarPath);
		jarFile.createLink(new Path(jarPath), IResource.NONE, null);
		addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib222284.jar", null);

		// Create file and wait for indexes
		createFile("/JavaSearchBugs/src/Test.java",
			"""
				import pack.Ref;\
				public class Test {
					Ref ref;\
				}
				"""
		);
		waitUntilIndexesReady();

		// Exit, delete jar and restart
		simulateExit();
		try {
			deleteExternalResource(jarName);
		} finally {
			simulateRestart();
		}

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
 * bug 228464: Annotation.getMemberValuePairs() empty for single attribute with empty value
 * test Ensure that annotation are correctly recovered
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=228464"
 */
public void testBug228464() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"""
			public class Test {
			    void m() {
			        @TestAnnotation(name=) Test iii;
			    }
			
			}
			"""
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
 * bug 228852: classes opened via Open Type not found
 * test Ensure that types found in an internal jar exist when using a workspace scope
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=228852"
 */
public void testBug228852a() throws Exception {
	try {
		IJavaProject p = createJavaProject("P", new String[] {}, new String[] {"/P/lib228852.jar"}, "");
		createJar(new String[] {
			"p228852/X228852.java",
			"""
				package p228852;
				public class X228852 {
				}"""
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
 * bug 228852: classes opened via Open Type not found
 * test Ensure that types found in an internal jar exist when using a Java search scope
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=228852"
 */
public void testBug228852b() throws Exception {
	try {
		IJavaProject p = createJavaProject("P", new String[] {}, new String[] {"/P/lib228852.jar"}, "");
		createJar(new String[] {
			"p228852/X228852.java",
			"""
				package p228852;
				public class X228852 {
				}"""
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
 * bug 231622: Some classes from Missing classes from Cntrl-Shift-T
 * test Ensure that types added through a container are found using a workspace scope
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=231622"
 */
public void testBug231622() throws Exception {
	try {
		IJavaProject p = createJavaProject("P", new String[] {}, new String[] {"org.eclipse.jdt.core.tests.model.TEST_CONTAINER"}, "");
		createJar(new String[] {
			"p231622/X231622.java",
			"""
				package p231622;
				public class X231622 {
				}"""
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
 * bug 236520: [search] AIOOBE in PatternLocator.updateMatch
 * test Ensure that no exception occurs even when code has compiler errors
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=236520"
 */
public void testBug236520() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack1/I.java",
		"""
			package pack1;
			public interface I<V> {
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/pack2/X.java",
		"""
			package pack2;
			public class X {
			    public I<A> foo() {}
			}
			"""
	);
	this.resultCollector.showRule();
	IType type = this.workingCopies[0].getType("I");
	search(type, REFERENCES, ERASURE_RULE, getJavaSearchWorkingCopiesScope());
	assertSearchResults(
		"src/pack2/X.java I<A> pack2.X.foo() [I] ERASURE_MATCH"
	);
}

/**
 * bug 250083: Search indexes are not correctly updated
 * test Ensure that a library that is no longer referenced, modified, and referenced again is re-indexed
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
 * bug 251827: [search] Search for type reference with wildcards finds references in package
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=251827"
 */
public void testBug251827a() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b251827/B251827.java",
		"""
			package b251827;
			public class B251827 {
				static int VAL=251827;
				static void foo() {};
			}
			""",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b251827/X.java",
		"""
			package b251827;
			import static b251827.B251827.VAL;
			import static b251827.B251827.foo;
			public class X {
				double val = VAL;
				void bar() { foo(); };
			}
			""",
		owner
	);
	this.resultCollector.showSelection();
	search("B251827*", TYPE, REFERENCES);
	assertSearchResults(
		"src/b251827/X.java [import static b251827.!|B251827|!.VAL;] EXACT_MATCH\n" +
		"src/b251827/X.java [import static b251827.!|B251827|!.foo;] EXACT_MATCH"
	);
}
public void testBug251827b() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b251827/B251827.java",
		"""
			package b251827;
			public class B251827 {
				static int VAL=251827;
				static void foo() {};
			}
			""",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b251827/X.java",
		"""
			package b251827;
			import static b251827.B251827.*;
			public class X {
				double val = VAL;
				void bar() { foo(); };
			}
			""",
		owner
	);
	this.resultCollector.showSelection();
	search("B251827*", TYPE, REFERENCES);
	assertSearchResults(
		"src/b251827/X.java [import static b251827.!|B251827|!.*;] EXACT_MATCH"
	);
}
public void testBug251827c() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b251827/B251827.java",
		"""
			package b251827;
			public class B251827 {
				int VAL=251827;
				void foo() {};
			}
			""",
		owner
	);
	this.resultCollector.showSelection();
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b251827/X.java",
		"""
			package b251827;
			import static b251827.*;
			public class X {
				void bar(B251827 m) {;
					double val = m.VAL;
					m.foo();
				};
			}
			""",
		owner
	);
	search("B251827*", TYPE, REFERENCES);
	assertSearchResults(
		"src/b251827/X.java void b251827.X.bar(B251827) [	void bar(!|B251827|! m) {;] EXACT_MATCH"
	);
}

/**
 * bug 261722: [search] NPE after removing a project
 * test Ensure that no NPE occurs when project is deleted before the end of the search request
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=261722"
 */
public void testBug261722() throws Exception {
	String libPath = getExternalResourcePath("lib261722.jar");
	waitUntilIndexesReady();
	IPath projectPath = null;
	try {
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
		Util.createJar(
			pathsAndContents,
			new HashMap(),
			libPath);

		IJavaProject javaProject = createJavaProject("P", new String[0], new String[] {libPath}, "");
		projectPath = javaProject.getProject().getLocation();
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
			@Override
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
 * bug 265065: [search] java.lang.ClassCastException while running "Refactor...Extract Class"
 * test Ensure that no CCE occurs while using an OrPattern made of VariablePattern
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=265065"
 */
public void testBug265065() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/MyClass.java",
		"""
			public class MyClass {
			    class MyPrivateClass {
			            public String type = TYPE_A;
			            public Object value = null;
			            public MyPrivateClass(String type, Object value) {
			                    this.type = type;
			                    this.value = value;
			            }
			    }
			
			    private static final String TYPE_A = "A";
			    private static final String TYPE_B = "B";
			    private static final String TYPE_C = "C";
			
			    void foo (Object value) {
					MyPrivateClass mpc = new MyPrivateClass(TYPE_B, value);
					if (mpc.value == null) {
						mpc.type = TYPE_C;
					}
				}
			}
			"""
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
		"""
			src/MyClass.java void MyClass.foo(Object) [value] EXACT_MATCH READ ACCESS
			src/MyClass.java void MyClass.foo(Object) [type] EXACT_MATCH READ ACCESS
			src/MyClass.java MyClass$MyPrivateClass.type [type] EXACT_MATCH
			src/MyClass.java MyClass$MyPrivateClass.value [value] EXACT_MATCH
			src/MyClass.java MyClass$MyPrivateClass(String, Object) [type] EXACT_MATCH WRITE ACCESS
			src/MyClass.java MyClass$MyPrivateClass(String, Object) [value] EXACT_MATCH WRITE ACCESS"""
	);
}
public void testBug265065b() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack/Test.java",
		"""
			package pack;
			public class Test {
				public int field;
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/X.java",
		"""
			package test;
			import pack.Test;
			public class X {
				void foo(Test t1, Test t2) {
					t1 = t2;
					t1.field = t1.field;
					t2.field = t1.field;
				}
			}
			"""
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
		"""
			src/test/X.java void test.X.foo(Test, Test) [t1] EXACT_MATCH WRITE ACCESS
			src/test/X.java void test.X.foo(Test, Test) [t2] EXACT_MATCH READ ACCESS
			src/test/X.java void test.X.foo(Test, Test) [t1] EXACT_MATCH READ ACCESS
			src/test/X.java void test.X.foo(Test, Test) [t1] EXACT_MATCH READ ACCESS
			src/test/X.java void test.X.foo(Test, Test) [t2] EXACT_MATCH READ ACCESS
			src/test/X.java void test.X.foo(Test, Test) [t1] EXACT_MATCH READ ACCESS"""
	);
}

/**
 * bug 266582: [search] NPE finding references
 * test Ensure that no NPE occurs when searching for type references
 * 	in a project which has the same jar twice on its classpath
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=266582"
 */
public void testBug266582() throws Exception {
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b266582a.jar", false);
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/b266582b.jar", false);
	try {
		createFile("/JavaSearchBugs/src/A.java",
			"""
				import foo.JohnsonException;
				class A {
					void foo() throws JohnsonException {}
				}"""
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
 * bug 266837: SourceField.getConstant does not supply a value if type is fully qualified
 * test Ensure that source field constant is not null when fully qualified type String is used
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=266837"
 */
public void testBug266837() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"""
			public class Test {
				private static final java.lang.String f266837 = "myString";
			}
			"""
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
 * bug 286379: [search] Problem while searching class
 * test Ensure that no IAE occurs when there is a change in JavaLikeNames.
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
			"""
				package p;
				public class Xtorem {
				}"""
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
			"""
				package p;
				public class Xtorem {
				}"""
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
		boolean toRemPresent = false;
		public void resourceChanged(IResourceChangeEvent event) {
			this.toRemPresent = validate(event.getDelta());
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
			"""
				package p;
				public class Xtorem {
				}"""
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
		while (!changeListener.toRemPresent) {
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException ie) {
				// skip
			}
			assertTrue("We should have got a resource event within a 10s delay!", counter++ < 100);
		}

		// Wait to be sure that indexes are ready after the new resource was added
		waitUntilIndexesReady();

		// Restart to let the indexes to be refreshed
		simulateExit();
		simulateRestart();
		waitUntilIndexesReady();

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
				collector,
				false /*only assume*/);

		// Delete the file specification
		changeListener.toRemPresent = true;
		javaContentType.removeFileSpec("torem", IContentType.FILE_EXTENSION_SPEC);
		counter = 0;
		while (changeListener.toRemPresent) {
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException ie) {
				// skip
			}
			assertTrue("We should have got a resource event within a 10s delay!", counter++ < 100);
		}
		waitUntilIndexesReady();

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

/**
 * bug 295894: Search shows focus type implementation for nested types even though the scope is restricted to subtypes.
 * test using the hierarchy with the old API includes the focus type.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=295894"
 */
public void testBug295894() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"public class Test {\n" +
		"    void test() {\n" +
		"        Test t = new Test();\n" +
		"        t.foo();\n" +
		"    }\n" +
		"    public void foo() {\n" +
		"    }\n" +
		"    public class Sub extends Test {\n" +
		"        public void foo() {}\n" +
		"    }\n" +
		"}\n" +
		""
	);
	search(
		"foo",
		METHOD,
		DECLARATIONS,
		SearchEngine.createHierarchyScope(this.workingCopies[0].findPrimaryType()),
		this.resultCollector);
	assertSearchResults(
		"src/Test.java void Test.foo() [foo] EXACT_MATCH\n" +
		"src/Test.java void Test$Sub.foo() [foo] EXACT_MATCH"
	);
}
/**
 * bug 295894: Search shows focus type implementation for nested types even though the scope is restricted to subtypes.
 * test explicitly excluding the focus type
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=295894"
 */
public void testBug295894a() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"public class Test {\n" +
		"    void test() {\n" +
		"        Test t = new Test();\n" +
		"        t.foo();\n" +
		"    }\n" +
		"    public void foo() {\n" +
		"    }\n" +
		"    public class Sub extends Test {\n" +
		"        public void foo() {}\n" +
		"    }\n" +
		"}\n" +
		""
	);
	search(
		"foo",
		METHOD,
		DECLARATIONS,
		SearchEngine.createStrictHierarchyScope(null, this.workingCopies[0].findPrimaryType(), true, false, null),
		this.resultCollector);
	// Test$Sub is a true sub type, not affected by filtering member types
	assertSearchResults(
		"src/Test.java void Test$Sub.foo() [foo] EXACT_MATCH"
	);
}
/**
 * bug 295894: Search shows focus type implementation for nested types even though the scope is restricted to subtypes.
 * test explicitly including the focus type
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=295894"
 */
public void testBug295894b() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Test.java",
		"public class Test {\n" +
		"    void test() {\n" +
		"        Test t = new Test();\n" +
		"        t.foo();\n" +
		"    }\n" +
		"    public void foo() {\n" +
		"    }\n" +
		"    public class Sub extends Test {\n" +
		"        public void foo() {}\n" +
		"    }\n" +
		"}\n" +
		""
	);
	search(
		"foo",
		METHOD,
		DECLARATIONS,
		SearchEngine.createStrictHierarchyScope(null, this.workingCopies[0].findPrimaryType(), false, true, null),
		this.resultCollector);
	// Same results as with the old API
	assertSearchResults(
		"src/Test.java void Test.foo() [foo] EXACT_MATCH\n" +
		"src/Test.java void Test$Sub.foo() [foo] EXACT_MATCH"
	);
}

/**
 * bug 295894: Search shows focus type implementation for nested types even though the scope is restricted to subtypes.
 * test explicitly including the focus type, which has no subtypes.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=295894"
 */
public void testBug295894c() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/A.java",
		"public class A {\n" +
		"    void test() {\n" +
		"        A a= new A();\n" +
		"        a.toString();\n" +
		"    }\n" +
		"    @Override\n" +
		"    public String toString() {\n" +
		"        return \"\";\n" +
		"    }\n" +
		"}\n" +
		""
	);
	search(
		"toString",
		METHOD,
		DECLARATIONS,
		SearchEngine.createStrictHierarchyScope(null, this.workingCopies[0].findPrimaryType(), true, true, null),
		this.resultCollector);
	assertSearchResults(
		"src/A.java String A.toString() [toString] EXACT_MATCH"
	);
}
//Failing test while working with a compilation unit instead of working copy
public void testBug295894c2() throws Exception {
	try {
		createJavaProject("P");
		createFile(
			"/P/A.java",
			"""
				public class A {
				    void test() {
				        A a= new A();
				        a.toString();
				    }
				    @Override
				    public String toString() {
				        return "";
				    }
				}
				"""
		);
		final ICompilationUnit cu = getCompilationUnit("/P/A.java");
		IMethod method = selectMethod(cu, "toString");
		search(method,
			DECLARATIONS|IGNORE_RETURN_TYPE,
			SearchEngine.createStrictHierarchyScope(null, cu.findPrimaryType(), true, true, null),
			this.resultCollector);
		assertSearchResults(
			"A.java String A.toString() [toString] EXACT_MATCH"
		);
	}
	finally {
		deleteProject("P");
	}
}
// Similar test passing when the focus type has a subclass
public void testBug295894c3() throws Exception {
	try {
		createJavaProject("P");
		createFile(
			"/P/A.java",
			"""
				public class A {
				    void test() {
				        A a= new A();
				        a.toString();
				    }
				    @Override
				    public String toString() {
				        return "";
				    }
				}
				class B extends A {
				}
				"""
		);
		final ICompilationUnit cu = getCompilationUnit("/P/A.java");
		IMethod method = selectMethod(cu, "toString");
		search(method,
			DECLARATIONS|IGNORE_RETURN_TYPE,
			SearchEngine.createStrictHierarchyScope(null, cu.findPrimaryType(), true, true, null),
			this.resultCollector);
		assertSearchResults(
			"A.java String A.toString() [toString] EXACT_MATCH"
		);
	}
	finally {
		deleteProject("P");
	}
}
// similar test using older API in SearchEngine:
public void testBug295894c4() throws Exception {
	try {
		createJavaProject("P");
		createFile(
			"/P/A.java",
			"""
				public class A {
				    void test() {
				        A a= new A();
				        a.toString();
				    }
				    @Override
				    public String toString() {
				        return "";
				    }
				}
				"""
		);
		final ICompilationUnit cu = getCompilationUnit("/P/A.java");
		IMethod method = selectMethod(cu, "toString");
		search(method,
			DECLARATIONS|IGNORE_RETURN_TYPE,
			SearchEngine.createHierarchyScope(cu.findPrimaryType(), null),
			this.resultCollector);
		assertSearchResults(
			"A.java String A.toString() [toString] EXACT_MATCH"
		);
	}
	finally {
		deleteProject("P");
	}
}
//Similar test to testBug295894c3 but using separate files for types
public void testBug295894c5() throws Exception {
	try {
		createJavaProject("P");
		createFile(
			"/P/A.java",
			"""
				public class A {
				    void test() {
				        A a= new A();
				        a.toString();
				    }
				    @Override
				    public String toString() {
				        return "";
				    }
				}"""
		);
		createFile(
			"/P/B.java",
			"class B extends A {\n" +
			"}\n"
		);
		final ICompilationUnit cu = getCompilationUnit("/P/A.java");
		IMethod method = selectMethod(cu, "toString");
		search(method,
			DECLARATIONS|IGNORE_RETURN_TYPE,
			SearchEngine.createStrictHierarchyScope(null, cu.findPrimaryType(), true, true, null),
			this.resultCollector);
		assertSearchResults(
			"A.java String A.toString() [toString] EXACT_MATCH"
		);
	}
	finally {
		deleteProject("P");
	}
}
/**
 * bug 288174: NullPointerException when searching for type references
 * test Ensure that no NPE occurs when searching for type references
 * 		when a binary type has matches in several member or anonymous types
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=288174"
 */
public void testBug288174() throws Exception {
	final String libPath = "/JavaSearchBugs/lib/b288174.jar";
	addLibraryEntry(JAVA_PROJECT, libPath, false);
	try {
		IPackageFragmentRoot root = getPackageFragmentRoot(libPath);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { root });
		search("*", TYPE, REFERENCES, scope);
		assertSearchResults(
			"""
				lib/b288174.jar pack.<anonymous> EXACT_MATCH
				lib/b288174.jar E[] pack.<anonymous>.bar1(java.lang.Class<E>) EXACT_MATCH
				lib/b288174.jar E[] pack.<anonymous>.bar1(java.lang.Class<E>) EXACT_MATCH
				lib/b288174.jar E[] pack.<anonymous>.bar1(java.lang.Class<E>) EXACT_MATCH
				lib/b288174.jar E[] pack.<anonymous>.bar1(java.lang.Class<E>) EXACT_MATCH
				lib/b288174.jar E[] pack.<anonymous>.bar1(java.lang.Class<E>) EXACT_MATCH
				lib/b288174.jar void pack.Test.foo1() EXACT_MATCH
				lib/b288174.jar pack.<anonymous> EXACT_MATCH
				lib/b288174.jar F[] pack.<anonymous>.bar2(java.lang.Class<F>) EXACT_MATCH
				lib/b288174.jar F[] pack.<anonymous>.bar2(java.lang.Class<F>) EXACT_MATCH
				lib/b288174.jar F[] pack.<anonymous>.bar2(java.lang.Class<F>) EXACT_MATCH
				lib/b288174.jar F[] pack.<anonymous>.bar2(java.lang.Class<F>) EXACT_MATCH
				lib/b288174.jar F[] pack.<anonymous>.bar2(java.lang.Class<F>) EXACT_MATCH
				lib/b288174.jar void pack.Test.foo2() EXACT_MATCH"""
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, new Path(libPath));
	}
}
/**
 * bug 293861: Problem with refactoring when existing jar with invalid package names
 * test Ensure that the search doesn't return classes with invalid package names
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=293861"
 */
public void testBug293861a() throws CoreException {
	try
	{
		IJavaProject project = createJavaProject("P");
		addClasspathEntry(project, JavaCore.newLibraryEntry(new Path("/JavaSearchBugs/lib/b293861.jar"), null, null));
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES | IJavaSearchScope.REFERENCED_PROJECTS;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);

		search("b293861TestFunc", IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, scope);
		assertSearchResults("No search results expected", "", this.resultCollector);
	} finally {
		deleteProject("P");
	}
}

/*
 * SearchEngine#searchAllTypeNames should also not return classes with invalid package names
 */
public void testBug293861b() throws CoreException {
	try
	{
		IJavaProject project = createJavaProject("P");
		addClasspathEntry(project, JavaCore.newLibraryEntry(new Path("/JavaSearchBugs/lib/b293861.jar"), null, null));
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES | IJavaSearchScope.REFERENCED_PROJECTS;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);

		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
				null,
				new char[][] {"b293861Test".toCharArray()},
				scope,
				collector,
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
		assertSearchResults("No search results expected", "", collector);
	} finally {
		deleteProject("P");
	}
}

/*
 * enum is a valid package name in Java1.4 and those classes should be returned by search
 */
public void testBug293861c() throws CoreException {
	try
	{
		IJavaProject project = createJavaProject("P");
		addClasspathEntry(project, JavaCore.newLibraryEntry(new Path("/JavaSearchBugs/lib/b293861.jar"), null, null));
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES | IJavaSearchScope.REFERENCED_PROJECTS;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);

		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
				null,
				new char[][] {"InEnumPackage".toCharArray()},
				scope,
				collector,
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
		assertSearchResults("Unexpected search results!", "InEnumPackage (not open) [in InEnumPackage.class [in enum [in /JavaSearchBugs/lib/b293861.jar [in P]]]]", collector);
	} finally {
		deleteProject("P");
	}
}

/**
 * bug 296343: OOM error caused by java indexing referencing classloader from threadLocal
 * test Ensure that indexing thread context class loader is not the application class loader
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=296343"
 */
public void testBug296343() throws Exception {
	simulateExit();
	class TestClassLoader extends ClassLoader {
		TestClassLoader(ClassLoader parent) {
			super(parent);
		}
	}
	TestClassLoader tcl = new TestClassLoader(this.getClass().getClassLoader());
	ClassLoader cl = Thread.currentThread().getContextClassLoader();
	try {
		// set the thread context class loader
		Thread.currentThread().setContextClassLoader(tcl);
		simulateRestart();

		// get the indexing thread
		class TestIndexRequest extends IndexRequest {
			public Thread indexingThread = null;
			public boolean executed = false;
			public boolean execute(IProgressMonitor progressMonitor) {
				this.indexingThread = Thread.currentThread();
				this.executed = true;
				return true;
			}
			TestIndexRequest(Path containerPath, IndexManager indexManager) {
				super(containerPath, indexManager);
			}
		}
		IndexManager indexManager = JavaModelManager.getIndexManager();
		TestIndexRequest tir = new TestIndexRequest(new Path(""), indexManager );
		indexManager.request(tir);
		int counter = 0;
		// wait until the Index request gets executed
		while (!tir.executed) {
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException ie) {
				// skip
			}
			assertTrue("Index request should have got executed within a 10s delay!", counter++ < 100);
		}
		assertFalse(tir.indexingThread.getContextClassLoader() == tcl);
	} finally {
		Thread.currentThread().setContextClassLoader(cl);
	}
}

/**
 * bug 304841: [search] NPE in IndexSelector.initializeIndexLocations
 * test Ensure that no NPE occurs when searching for a reference in a CU without primary type
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=304841"
 */
public void testBug304841() throws Exception {
	boolean autoBuild = getWorkspace().isAutoBuilding();
	IWorkspaceDescription preferences = getWorkspace().getDescription();
	try {
		// ensure that the workspace auto-build is ON
		preferences.setAutoBuilding(true);
		getWorkspace().setDescription(preferences);

		// create test case
		IJavaProject project = createJavaProject("P");
		createFolder("/P/p");
		createFile(
			"/P/p/Hello.java",
			"""
				package p;
				class One {
				}
				class Two {
				}
				"""
		);
		createFile(
			"/P/p/Ref.java",
			"""
				package p;
				class Three {
					Two two;
				}
				"""
		);
		waitUntilIndexesReady();

		// perform search
		final ICompilationUnit cu = getCompilationUnit("/P/p/Hello.java");
		IType type = cu.getType("Two");
		SearchPattern pattern = SearchPattern.createPattern(type, REFERENCES);
		MatchLocator.setFocus(pattern, type);
		new SearchEngine().search(
			pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			SearchEngine.createJavaSearchScope(new IJavaElement[] { project }),
			this.resultCollector,
			null
		);
		assertSearchResults(
			"p/Ref.java p.Three.two [Two] EXACT_MATCH"
		);
	} finally {
		preferences.setAutoBuilding(autoBuild);
		getWorkspace().setDescription(preferences);
		deleteProject("P");
	}
}
public void testBug304841b() throws Exception {
	boolean autoBuild = getWorkspace().isAutoBuilding();
	IWorkspaceDescription preferences = getWorkspace().getDescription();
	try {
		// ensure that the workspace auto-build is ON
		preferences.setAutoBuilding(true);
		getWorkspace().setDescription(preferences);

		// perform search
		IType type = getClassFile("/JavaSearchBugs/lib/Bug148380.class").getType();
		SearchPattern pattern = SearchPattern.createPattern(type, REFERENCES);
		MatchLocator.setFocus(pattern, type);
		new SearchEngine().search(
			pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchScope(),
			this.resultCollector,
			null
		);
		assertSearchResults(""); // No expected results, only verify that no CCE occurs
	} finally {
		preferences.setAutoBuilding(autoBuild);
		getWorkspace().setDescription(preferences);
	}
}

/**
 * bug 306196: [search] NPE while searching for annotation references in
 *      rt.jar of JRE 6.0
 * test Ensure that no NPE occurs when searching for both ANNOTATION_TYPE
 *       and TYPE references from an inner enum declared in a binary type.
 *       This same test also ensures that there is no NPE even if the source
 *       has a method that does not exist in the class file.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=306196"
 */
public void testBug306196() throws Exception {
	final String libPath = "/JavaSearchBugs/lib/b306196.jar";
	addLibraryEntry(JAVA_PROJECT, libPath, false);
	try {
		IPackageFragmentRoot root = getPackageFragmentRoot(libPath);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { root });
		SearchPattern leftPattern = createPattern("*", ANNOTATION_TYPE, REFERENCES, false);
		SearchPattern rightPattern = createPattern("*", TYPE, REFERENCES, false);
		new SearchEngine().search(SearchPattern.createOrPattern( leftPattern, rightPattern),
				new SearchParticipant[] { SearchEngine.getDefaultSearchParticipant() }, scope,
				this.resultCollector, null);
		assertSearchResults("""
			lib/b306196.jar java.lang.String pkg.<anonymous>.aFunc(java.lang.Object) EXACT_MATCH
			lib/b306196.jar java.lang.String pkg.<anonymous>.aFunc(java.lang.Object) EXACT_MATCH
			lib/b306196.jar java.lang.String pkg.<anonymous>.aFunc(java.lang.Object) EXACT_MATCH
			lib/b306196.jar java.lang.String pkg.<anonymous>.aFunc(java.lang.Object) EXACT_MATCH
			lib/b306196.jar java.lang.String pkg.<anonymous>.aFunc(java.lang.Object) EXACT_MATCH
			lib/b306196.jar java.lang.String pkg.<anonymous>.aFunc(java.lang.Object) EXACT_MATCH
			lib/b306196.jar java.lang.String pkg.B306196$anEnum.aFunc(java.lang.Object) EXACT_MATCH
			lib/b306196.jar java.lang.String pkg.B306196$anEnum.aFunc(java.lang.Object) EXACT_MATCH""");
	} finally {
		removeClasspathEntry(JAVA_PROJECT, new Path(libPath));
	}
}

/**
 * bug 306223:  [search] Searching for annotation references report all type references
 * test Ensures the following -
 * 		 1. Search for annotation references does not report type references
 * 		 2. Search for annotation references even report a non-annotation references to an annotation type
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=306223"
 */
public void testBug306223a() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b306223/Test.java",
			"""
				import b306223.MyAnnot;
				@MyAnnot
				public class TestAnnot {
				MyAnnot annon;
				String test;
				void foo(String str) {
				this.test = str;
				}
				}
				"""
		);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b306223/MyAnnot.java",
			"@interface MyAnnot {}\n");
	SearchPattern pattern = SearchPattern.createPattern(
			"*",
			ANNOTATION_TYPE,
			REFERENCES,
			EXACT_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
	new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
	getJavaSearchWorkingCopiesScope(),
	this.resultCollector,
	null);
	assertSearchResults(
			"""
				src/b306223/Test.java [b306223.MyAnnot] EXACT_MATCH
				src/b306223/Test.java b306223.TestAnnot [MyAnnot] EXACT_MATCH
				src/b306223/Test.java b306223.TestAnnot.annon [MyAnnot] EXACT_MATCH"""
	);
}
/**
 * This ensures that using ANNOTATION_TYPE_REFERENCE as fine grain constant reports only
 * annotations and not any other references to an annotation type
 */
public void testBug306223b() throws CoreException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b306223/Test.java",
			"""
				import b306223.MyAnnot;
				@MyAnnot
				public class TestAnnot {
				MyAnnot annon;
				String test;
				void foo(String str) {
				this.test = str;
				}
				}
				"""
		);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/b306223/MyAnnot.java",
			"@interface MyAnnot {}\n");
	SearchPattern pattern = SearchPattern.createPattern(
			"*",
			ANNOTATION_TYPE,
			REFERENCES|ANNOTATION_TYPE_REFERENCE,
			EXACT_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
	new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
	getJavaSearchWorkingCopiesScope(),
	this.resultCollector,
	null);
	assertSearchResults(
			"src/b306223/Test.java b306223.TestAnnot [MyAnnot] EXACT_MATCH"
	);
}

/**
 * This test ensures that search for enum references does not report type references.
 */
public void testBug306223c() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b306223/Test.java",
			"""
				public class TestEnum {
					String foo(MyEnum e) {
						switch (e) {
							case ONE:
								return "1";
							case TWO:
								return "2";
							default:
								return "-1";
							}
						}
					}
				enum MyEnum {
					ONE, TWO
				}
				"""
	);

	SearchPattern pattern = SearchPattern.createPattern(
			"*",
			ENUM,
			REFERENCES,
			EXACT_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
	new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
	getJavaSearchWorkingCopiesScope(),
	this.resultCollector,
	null);
	assertSearchResults(
			"src/b306223/Test.java String b306223.TestEnum.foo(MyEnum) [MyEnum] EXACT_MATCH"
	);
}
/**
 * This test ensures that a reference search of ANNOTATION_TYPE should report POTENTIAL_MATCH
 * for unknown references types.
 */
public void testBug306223d() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b306223/Test.java",
			"""
				public class TestAnnot {
				Zork annon;
				}
				"""
		);
	SearchPattern pattern = SearchPattern.createPattern(
			"*",
			ANNOTATION_TYPE,
			REFERENCES,
			EXACT_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
	new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
	getJavaSearchWorkingCopiesScope(),
	this.resultCollector,
	null);
	assertSearchResults(
			"src/b306223/Test.java b306223.TestAnnot.annon [Zork] POTENTIAL_MATCH"
	);
}
/**
 * This test ensures that an ANNOTATION_TYPE reference search for a non-existing
 * type does not report any other references
 */
public void testBug306223e() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b306223/Test.java",
			"""
				public class Test {
					Zork x;
				}
				"""
		);
	SearchPattern pattern = SearchPattern.createPattern(
			"abc",
			ANNOTATION_TYPE,
			REFERENCES,
			EXACT_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		getJavaSearchWorkingCopiesScope(),
		this.resultCollector,
		null);
	assertSearchResults("");
}
/**
 * This test ensures that a TYPE reference search reports EXACT_MATCH
 * for missing types
 */
public void testBug306223f() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b306223/Test.java",
			"""
				public class Test {
					Zork x;
				}
				"""
		);
	SearchPattern pattern = SearchPattern.createPattern(
			"*",
			TYPE,
			REFERENCES,
			EXACT_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		getJavaSearchWorkingCopiesScope(),
		this.resultCollector,
		null);
	assertSearchResults(
		"src/b306223/Test.java b306223.Test.x [Zork] EXACT_MATCH"
	);
}
/**
 * This test ensures that a TYPE reference search for a non-existing
 * type does not report any other references
 */
public void testBug306223g() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b306223/Test.java",
			"""
				public class Test {
					Zork x;
				}
				"""
		);
	SearchPattern pattern = SearchPattern.createPattern(
			"abc",
			TYPE,
			REFERENCES,
			EXACT_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
		new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
		getJavaSearchWorkingCopiesScope(),
		this.resultCollector,
		null);
	assertSearchResults("");
}

/**
 * bug 310213: [search] Reference to package is not found in qualified annotation
 * test Ensure that references to package are also found in qualified annotation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=310213"
 */
public void testBug310213() throws CoreException {
	boolean autoBuild = getWorkspace().isAutoBuilding();
	IWorkspaceDescription preferences = getWorkspace().getDescription();
	try {
		// ensure that the workspace auto-build is ON
		preferences.setAutoBuilding(true);
		getWorkspace().setDescription(preferences);

		// create files
		createFolder("/JavaSearchBugs/src/java/lang");
		createFile("/JavaSearchBugs/src/java/lang/Throwable.java",
			"package java.lang;\n" +
			"public class Throwable{}\n"
		);
		createFolder("/JavaSearchBugs/src/b310213/test");
		createFile("/JavaSearchBugs/src/b310213/test/Test.java",
			"""
				package b310213.test;
				public class Test extends Throwable {
				}"""
		);
		waitUntilIndexesReady();

		// search
		IType type = getCompilationUnit("/JavaSearchBugs/src/java/lang/Throwable.java").getType("Throwable");
		search(type, REFERENCES);
		assertSearchResults(
			"src/b310213/test/Test.java b310213.test.Test [Throwable] EXACT_MATCH\n" +
			""+ getExternalJCLPathString("1.5") + " java.lang.Error EXACT_MATCH\n" +
			""+ getExternalJCLPathString("1.5") + " java.lang.Exception EXACT_MATCH\n" +
			""+ getExternalJCLPathString("1.5") + " void java.lang.Object.finalize() EXACT_MATCH"
		);
	}
	finally {
		// put back initial setup
		preferences.setAutoBuilding(autoBuild);
		getWorkspace().setDescription(preferences);

		// delete files
		deleteFolder("/JavaSearchBugs/src/b310213");
		deleteFolder("/JavaSearchBugs/src/java");
	}
}

/**
 * bug 313668: [search] Call hierarchy doesn't show all calls of the method in workspace
 * test Search for references to method should even return hierarchy sibling's reference.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=313668"
 */
public void testBug313668() throws CoreException {
	boolean autoBuild = getWorkspace().isAutoBuilding();
	IWorkspaceDescription preferences = getWorkspace().getDescription();
	IJavaProject commonProject = null, clientProject = null, serverProject = null;
	try {
		// ensure that the workspace auto-build is ON
		preferences.setAutoBuilding(true);
		getWorkspace().setDescription(preferences);

		// create the common project and create an interface
		commonProject = createJavaProject("common");
		createFolder("/common/com/db");
		createFile("/common/com/db/Repo.java",
				"""
					package com.db;
					public interface Repo {
					public void find();
					}""");

		// create the client project, create the class and the reference
		clientProject = createJavaProject("client");
		IClasspathEntry entry =  JavaCore.newProjectEntry(new Path("/common"));
		addClasspathEntry(clientProject, entry);
		createFolder("/client/com/db");
		createFile("/client/com/db/ClientRepo.java",
				"""
					package com.db;
					public class ClientRepo implements Repo {
					public void find(){};
					}""");
		createFile("/client/com/db/CallerClient.java",
				"""
					package com.db;
					public class CallerClient{
					public static void main(String[] args) {
					Repo r = null;
					r.find();}}
					""");

		// create the server project, create the class and the reference
		serverProject = createJavaProject("server");
		entry =  JavaCore.newProjectEntry(new Path("/common"));
		addClasspathEntry(serverProject, entry);
		createFolder("/server/com/db");
		createFile("/server/com/db/ServerRepo.java",
				"""
					package com.db;
					public class ServerRepo implements Repo{
					public void find(){};
					""");
		createFile("/server/com/db/CallerServer.java",
				"""
					package com.db;
					public class CallerServer {
					public static void main(String[] args) {
					Repo r = null;
					r.find();}}
					""");

		waitUntilIndexesReady();

		// search
		IType type = getCompilationUnit("/server/com/db/ServerRepo.java").getType("ServerRepo");
		IMethod method = type.getMethod("find", new String[]{});
		search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
		assertSearchResults(
				"com/db/CallerClient.java void com.db.CallerClient.main(String[]) [find()] EXACT_MATCH\n"+
				"com/db/CallerServer.java void com.db.CallerServer.main(String[]) [find()] EXACT_MATCH");
	}
	finally {
		// put back initial setup
		preferences.setAutoBuilding(autoBuild);
		getWorkspace().setDescription(preferences);

		// delete projects
		deleteProject(commonProject);
		deleteProject(clientProject);
		deleteProject(serverProject);
	}
}
/**
 * bug 317264: Refactoring is impossible with commons.lang added to project
 * test types in enum package of org.apache.commons.lang.jar should not be reported for 1.5 projects
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=317264"
 */
public void testBug317264a() throws CoreException {
	IJavaProject project = null;
	try
	{
		project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
		addClasspathEntry(project, JavaCore.newLibraryEntry(new Path("/JavaSearchBugs/lib/b317264/org.apache.commons.lang_2.modified.jar"), null, null));
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES | IJavaSearchScope.REFERENCED_PROJECTS;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);

		waitUntilIndexesReady();
		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
				"org.apache.commons.lang.enum".toCharArray(),
				SearchPattern.R_EXACT_MATCH,
				"".toCharArray(),
				SearchPattern.R_PREFIX_MATCH,
				IJavaSearchConstants.TYPE,
				scope,
				collector,
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
		assertSearchResults("Unexpected search results!", "", collector);
	} finally {
		deleteProject(project);
	}
}
// types in enum package of org.apache.commons.lang.jar should be reported for 1.4 projects
public void testBug317264b() throws CoreException {
	IJavaProject project = null;
	try
	{
		project = createJavaProject("P", new String[] {""}, new String[] {"JCL_LIB"}, "", "1.4");
		addClasspathEntry(project, JavaCore.newLibraryEntry(new Path("/JavaSearchBugs/lib/b317264/org.apache.commons.lang_2.modified.jar"), null, null));
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES | IJavaSearchScope.REFERENCED_PROJECTS;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);

		waitUntilIndexesReady();
		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
				"org.apache.commons.lang.enum".toCharArray(),
				SearchPattern.R_EXACT_MATCH,
				"".toCharArray(),
				SearchPattern.R_PREFIX_MATCH,
				IJavaSearchConstants.TYPE,
				scope,
				collector,
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
		assertSearchResults("Unexpected search results!",
				"Enum (not open) [in Enum.class [in org.apache.commons.lang.enum [in /JavaSearchBugs/lib/b317264/org.apache.commons.lang_2.modified.jar [in P]]]]",
				collector);
	} finally {
		deleteProject(project);
	}
}

// types in enum package of org.apache.commons.lang.jar should not be reported for 1.5 projects
public void testBug317264c() throws CoreException {
	try
	{
		IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
		addClasspathEntry(project, JavaCore.newLibraryEntry(new Path("/JavaSearchBugs/lib/b317264/org.apache.commons.lang_2.modified.jar"), null, null));
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);
		search("org.apache.commons.lang.enum*", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, scope, this.resultCollector);
		assertSearchResults("Unexpected search results!", "", this.resultCollector);
	} finally {
		deleteProject("P");
	}
}

// types in enum package of org.apache.commons.lang.jar should be reported for 1.4 projects
public void testBug317264d() throws CoreException {
	try
	{
		IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL_LIB"}, "", "1.4");
		addClasspathEntry(project, JavaCore.newLibraryEntry(new Path("/JavaSearchBugs/lib/b317264/org.apache.commons.lang_2.modified.jar"), null, null));
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);
		search("org.apache.commons.lang.enum.*", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, scope, this.resultCollector);
		assertSearchResults("Unexpected search results!",
				"lib/b317264/org.apache.commons.lang_2.modified.jar org.apache.commons.lang.enum.Enum EXACT_MATCH",
				this.resultCollector);
	} finally {
		deleteProject("P");
	}
}

// enum package of org.apache.commons.lang.jar should not be reported for 1.5 projects
public void testBug317264e() throws CoreException {
	try
	{
		IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
		addClasspathEntry(project, JavaCore.newLibraryEntry(new Path("/JavaSearchBugs/lib/b317264/org.apache.commons.lang_2.modified.jar"), null, null));
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);
		search("org.apache.commons.lang.enum*", IJavaSearchConstants.PACKAGE, IJavaSearchConstants.DECLARATIONS, scope, this.resultCollector);
		assertSearchResults("Unexpected search results!",  "", this.resultCollector);
	} finally {
		deleteProject("P");
	}
}

//enum package of org.apache.commons.lang.jar should be reported for 1.4 projects
public void testBug317264f() throws CoreException {
	try
	{
		IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL_LIB"}, "", "1.4");
		addClasspathEntry(project, JavaCore.newLibraryEntry(new Path("/JavaSearchBugs/lib/b317264/org.apache.commons.lang_2.modified.jar"), null, null));
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);
		search("org.apache.commons.lang.enum*", IJavaSearchConstants.PACKAGE, IJavaSearchConstants.DECLARATIONS, scope, this.resultCollector);
		assertSearchResults("Unexpected search results!",
				"lib/b317264/org.apache.commons.lang_2.modified.jar org.apache.commons.lang.enum [No source] EXACT_MATCH",
				this.resultCollector);
	} finally {
		deleteProject("P");
	}
}

/**
 * bug 322979: [search] use of IJavaSearchConstants.IMPLEMENTORS yields surprising results
 * test search of implementors does no longer report matches in type arguments
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=322979"
 */
public void testBug322979a() throws CoreException {
	try
	{
		IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
		createFile("/P/Test.java",
			"""
				public class Test extends Object implements Comparable<Object>{
				public int compareTo(Object o) {
				return 0;
				}
				}
				""");
		waitUntilIndexesReady();
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);
		this.resultCollector.showAccuracy(true);
		this.resultCollector.showSelection();
		search("Object", TYPE, IMPLEMENTORS, scope);
		assertSearchResults(
			"Test.java Test [public class Test extends !|Object|! implements Comparable<Object>{] EXACT_MATCH"
		);
	} finally {
		deleteProject("P");
	}
}

public void testBug322979b() throws CoreException {
	try
	{
		IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
		createFile("/P/Test.java",
			"""
				public class Test extends java.lang.Object implements Comparable<Object>{
				public int compareTo(Object o) {
				return 0;
				}
				}
				""");
		waitUntilIndexesReady();
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);
		IType type = getClassFile("P", getExternalJCLPathString("1.5"), "java.lang", "Object.class").getType();
		this.resultCollector.showAccuracy(true);
		this.resultCollector.showSelection();
		search(type, IMPLEMENTORS, scope);
		assertSearchResults(
			"Test.java Test [public class Test extends !|java.lang.Object|! implements Comparable<Object>{] EXACT_MATCH"
		);
	} finally {
		deleteProject("P");
	}
}

public void testBug322979c() throws CoreException {
	try
	{
		IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
		createFile("/P/Test.java",
			"""
				public class Test extends Object implements I01a<Object>, I01b<String>, I01c<Object> {
				}
				interface I01a<T> {}
				interface I01b<T> {}
				interface I01c<T> {}
				"""
		);
		waitUntilIndexesReady();
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);
		this.resultCollector.showSelection();
		search("java.lang.Object", TYPE, IMPLEMENTORS, scope);
		assertSearchResults(
			"Test.java Test [public class Test extends !|Object|! implements I01a<Object>, I01b<String>, I01c<Object> {] EXACT_MATCH"
		);
	} finally {
		deleteProject("P");
	}
}

public void testBug322979d() throws CoreException {
	try
	{
		IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
		createFile("/P/Test.java",
			"""
				public class Test extends Object implements I01<
					I02<
						I03<Object,
							I02<Object, I01<Object>>,
							I03<Object, I01<Object>, I02<Object, I01<Object>>>
							>,
						I01<Object>>> {
				}
				interface I01<T> {}
				interface I02<T, U> {}
				interface I03<T, U, V> {}
				"""
		);
		waitUntilIndexesReady();
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);
		this.resultCollector.showSelection();
		search("Object", TYPE, IMPLEMENTORS, scope);
		assertSearchResults(
			"Test.java Test [public class Test extends !|Object|! implements I01<] EXACT_MATCH"
		);
	} finally {
		deleteProject("P");
	}
}

public void testBug322979e() throws CoreException {
	try
	{
		IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
		createFile("/P/Test.java",
			"""
				public class Test extends Object implements I01<
					I02<
						I03<Object,
							I02<Object, I01<Object>>,
							I03<Object, I01<Object>, I02<Object, I01<Object>>>
							>,
						I01<Object>>> {
				}
				interface I01<T> {}
				interface I02<T, U> {}
				interface I03<T, U, V> {}
				"""
		);
		waitUntilIndexesReady();
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);
		this.resultCollector.showSelection();
		search("Object", TYPE, REFERENCES, scope);
		assertSearchResults(
			"Test.java Test [public class Test extends !|Object|! implements I01<] EXACT_MATCH\n" +
			"Test.java Test [		I03<!|Object|!,] EXACT_MATCH\n" +
			"Test.java Test [			I02<!|Object|!, I01<Object>>,] EXACT_MATCH\n" +
			"Test.java Test [			I02<Object, I01<!|Object|!>>,] EXACT_MATCH\n" +
			"Test.java Test [			I03<!|Object|!, I01<Object>, I02<Object, I01<Object>>>] EXACT_MATCH\n" +
			"Test.java Test [			I03<Object, I01<!|Object|!>, I02<Object, I01<Object>>>] EXACT_MATCH\n" +
			"Test.java Test [			I03<Object, I01<Object>, I02<!|Object|!, I01<Object>>>] EXACT_MATCH\n" +
			"Test.java Test [			I03<Object, I01<Object>, I02<Object, I01<!|Object|!>>>] EXACT_MATCH\n" +
			"Test.java Test [		I01<!|Object|!>>> {] EXACT_MATCH\n" +
			""+ getExternalJCLPathString("1.5") + " java.lang.Object java.lang.Object.clone() EXACT_MATCH\n" +
			""+ getExternalJCLPathString("1.5") + " boolean java.lang.Object.equals(java.lang.Object) EXACT_MATCH\n" +
			""+ getExternalJCLPathString("1.5") + " java.lang.Class<? extends java.lang.Object> java.lang.Object.getClass() EXACT_MATCH"
		);
	} finally {
		deleteProject("P");
	}
}

public void testBug322979f() throws CoreException {
	try
	{
		IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
		createFile("/P/Test.java",
			"""
				public class Test extends Object implements I01<
					I02<
						I03<Object,
							I02<Object, I01<Object>>,
							I03<Object, I01<Object>, I02<Object, I01<Object>>>
							>,
						I01<Object>>> {
				}
				interface I01<T> {}
				interface I02<T, U> {}
				interface I03<T, U, V> {}
				"""
		);
		waitUntilIndexesReady();
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);
		this.resultCollector.showSelection();
		search("Object", TYPE, REFERENCES | SUPERTYPE_TYPE_REFERENCE, scope);
		assertSearchResults(
			"Test.java Test [public class Test extends !|Object|! implements I01<] EXACT_MATCH"
		);
	} finally {
		deleteProject("P");
	}
}

public void testBug322979g() throws CoreException {
	try
	{
		IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
		createFile("/P/Test.java",
			"""
				public class Test extends Object implements I<A<Object>.B<I<Object>>.C<I<A<Object>.B<Object>.C<Object>>>> {
				}
				interface I<T> {
				}
				class A<T> {
					class B<U> {
						class C<V> {}
					}
				}
				"""
		);
		waitUntilIndexesReady();
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);
		this.resultCollector.showSelection();
		search("Object", TYPE, IMPLEMENTORS, scope);
		assertSearchResults(
			"Test.java Test [public class Test extends !|Object|! implements I<A<Object>.B<I<Object>>.C<I<A<Object>.B<Object>.C<Object>>>> {] EXACT_MATCH"
		);
	} finally {
		deleteProject("P");
	}
}
public void testBug322979h() throws CoreException {
	try
	{
		IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
		createFile("/P/Test.java",
			"""
				public class Test extends Object implements I1<String>, I2<Object>{
				}
				Interface I1<T> {}
				Interface I2<T> {}
				""");
		waitUntilIndexesReady();
		int mask = IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);
		this.resultCollector.showAccuracy(true);
		this.resultCollector.showSelection();
		SearchPattern leftPattern = SearchPattern.createPattern(
				"Object",
				TYPE,
				IMPLEMENTORS,
				EXACT_RULE);
		SearchPattern rightPattern = SearchPattern.createPattern(
				"String",
				TYPE,
				REFERENCES,
				EXACT_RULE);
		search(SearchPattern.createOrPattern(leftPattern, rightPattern), scope, this.resultCollector);
		assertSearchResults(
			"Test.java Test [public class Test extends !|Object|! implements I1<String>, I2<Object>{] EXACT_MATCH\n" +
			"Test.java Test [public class Test extends Object implements I1<!|String|!>, I2<Object>{] EXACT_MATCH"
		);
	} finally {
		deleteProject("P");
	}
}

/**
 * bug 323514: Search indexes are not correctly updated
 * test [indexing] The Java Indexer is taking longer to run in eclipse 3.6 when opening projects
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=323514"
 */
public void testBug323514() throws Exception {
	String libPath = getExternalResourcePath("lib323514.jar");
	waitUntilIndexesReady();
	try {
		// Create project and external jar file
		Util.createJar(
			new String[] {
				"p323514/Y323514.java",
				"package p323514;\n" +
				"public class Y323514 {}"
			},
			new HashMap(),
			libPath);
		IJavaProject javaProject = createJavaProject("P", new String[0], new String[] {libPath}, "");
		waitUntilIndexesReady();

		// Close the project
		IProject project = javaProject.getProject();
		project.close(null);
		assertNotNull("External jar file index should not have been removed!!!", JavaModelManager.getIndexManager().getIndex(new Path(libPath), false, false));

		// Reopen the project
		project.open(null);
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		waitUntilIndexesReady();
		// Search
		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
			null,
			null,
			BasicSearchEngine.createJavaSearchScope(new IJavaElement[] { javaProject }),
			collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
		assertSearchResults(
			"Y323514 (not open) [in Y323514.class [in p323514 [in "+ getExternalPath() + "lib323514.jar]]]",
			collector);
	} finally {
		deleteExternalFile(libPath);
		deleteProject("P");
	}
}
public void testBug323514a() throws Exception {
	String libPath = getExternalResourcePath("lib323514.jar");
	waitUntilIndexesReady();
	try {
		// Create project and external jar file
		Util.createJar(
			new String[] {
				"p323514/Y323514.java",
				"package p323514;\n" +
				"public class Y323514 {}"
			},
			new HashMap(),
			libPath);
		IJavaProject javaProject = createJavaProject("P", new String[0], new String[] {libPath}, "");
		waitUntilIndexesReady();

		// Close project and delete external jar file
		IProject project = javaProject.getProject();
		waitUntilIndexesReady();
		project.close(null);
		deleteExternalFile(libPath);
		Thread.sleep(1000);	// necessary for filesystems with timestamps only upto seconds (eg. Mac)
		// Open project and recreate external jar file
		Util.createJar(
			new String[] {
				"p323514/X323514.java",
				"package p323514;\n" +
				"public class X323514 {}"
			},
			new HashMap(),
			libPath);
		project.open(null);
		// A refresh external archives seems to be necessary when the external
		// archive has been modified while the project was closed... like a refresh
		// in the workspace to see external files changes.
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		waitUntilIndexesReady();

		// Search
		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
			null,
			null,
			BasicSearchEngine.createJavaSearchScope(new IJavaElement[] { javaProject }),
			collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
		assertSearchResults(
			"X323514 (not open) [in X323514.class [in p323514 [in "+ getExternalPath() + "lib323514.jar]]]",
			collector);
	} finally {
		deleteExternalFile(libPath);
		deleteProject("P");
	}
}
public void testBug323514b() throws Exception {
	String libPath = getExternalResourcePath("lib323514.jar");
	waitUntilIndexesReady();
//	boolean isDebugging = false; // turn to true to verify using the trace that the external jar file is not re-indexed while opening the project
//	org.eclipse.jdt.internal.core.search.processing.JobManager.VERBOSE = isDebugging;
	try {
		// Create project and external jar file
		Util.createJar(
			new String[] {
				"p323514/Y323514.java",
				"package p323514;\n" +
				"public class Y323514 {}"
			},
			new HashMap(),
			libPath);
		IJavaProject javaProject = createJavaProject("P", new String[0], new String[] {libPath}, "");
		waitUntilIndexesReady();

		// Close project
		IProject project = javaProject.getProject();
		project.close(null);
		waitUntilIndexesReady();
		Thread.sleep(1000);	// necessary for filesystems with timestamps only upto seconds (eg. Mac)
		// Open project and modify the external jar file content
		Util.createJar(
			new String[] {
				"p323514/X323514.java",
				"package p323514;\n" +
				"public class X323514 {}"
			},
			new HashMap(),
			libPath);
		project.open(null);
		// A refresh external archives seems to be necessary when the external
		// archive has been modified while the project was closed... like a refresh
		// in the workspace to see external files changes.
		project.refreshLocal(IResource.DEPTH_INFINITE, null);
		waitUntilIndexesReady();

		// Search
		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
			null,
			null,
			BasicSearchEngine.createJavaSearchScope(new IJavaElement[] { javaProject }),
			collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
			null);
		assertSearchResults(
			"X323514 (not open) [in X323514.class [in p323514 [in "+ getExternalPath() + "lib323514.jar]]]",
			collector);
	} finally {
		deleteExternalFile(libPath);
		deleteProject("P");
	}
}

/**
 * bug 324109: [search] Java search shows incorrect results as accurate matches
 * test search of method declaration off missing types should report potential matches and not accurate.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=324109"
 */
public void testBug324109() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b324109/X.java",
		"""
			package b324109;
			public class X extends A {
			 public void run() {}
			}"""
	);
	search("Worker.run()", METHOD, DECLARATIONS);
	assertSearchResults(
		"src/b324109/X.java void b324109.X.run() [run] POTENTIAL_MATCH"
	);
}
/**
 * bug 329727 Invalid check in the isConstructor() method of the IMethod implementation.
 * test check that in a binary type, method's name doesn't contain the enclosing type name and
 * that IMethod#isContructor returns correct value
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=329727"
 */
public void testBug329727() throws CoreException, IOException {
	IJavaProject project = getJavaProject("JavaSearchBugs");
	IClasspathEntry[] originalCP = project.getRawClasspath();
		try {
			int cpLength = originalCP.length;
			IClasspathEntry[] newCP = new IClasspathEntry[cpLength + 1];
			System.arraycopy(originalCP, 0, newCP, 0, cpLength);
			createLibrary(project, "bug329727.jar", null, new String[] {
					"p/OuterClass.java",
					"""
						package p;
						public class OuterClass {
							public OuterClass(){}
							class InnerClass {
								public InnerClass(){}
							}
						}
						""" },
					new String[0], JavaCore.VERSION_1_4);
			newCP[cpLength] = JavaCore.newLibraryEntry(
						new Path("/JavaSearchBugs/bug329727.jar"), null, null);
			project.setRawClasspath(newCP, null);

			final String txtPattern = "InnerClas*";
			SearchPattern pattern = SearchPattern.createPattern(txtPattern,
					IJavaSearchConstants.CONSTRUCTOR,
					IJavaSearchConstants.DECLARATIONS,
					SearchPattern.R_CASE_SENSITIVE
							| SearchPattern.R_PATTERN_MATCH);

			SearchParticipant[] participants = new SearchParticipant[1];
			participants[0] = SearchEngine.getDefaultSearchParticipant();

			SearchRequestor requestor = new SearchRequestor() {
				public void acceptSearchMatch(SearchMatch match)
						throws CoreException {
					assertTrue("Incorrect Element", match.getElement() instanceof IMethod);
					assertTrue("Must be a constructor", ((IMethod) match.getElement()).isConstructor());
					assertEquals("Incorrect Constructor name", "InnerClass", ((IMethod)match.getElement()).getElementName());
				}
			};

			SearchEngine engine = new SearchEngine();
			IJavaSearchScope scope = SearchEngine.createWorkspaceScope();
			engine.search(pattern, participants, scope, requestor, null);
    }
    finally{
		project.setRawClasspath(originalCP, null);
    	deleteFile("/JavaSearchBugs/bug329727.jar");
    }
}

/**
 * bug 327654: FUP of bug 317264: Refactoring is impossible with commons-lang.jar is in the path
 * test types in enum package should not be reported for 1.5 projects
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=327654"
 */
public void testBug327654() throws CoreException {
	IJavaProject project = null;
	try
	{
		project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
		addClasspathEntry(project, JavaCore.newLibraryEntry(new Path("/JavaSearchBugs/lib/b327654/commons-lang.jar"), null, null));
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES | IJavaSearchScope.REFERENCED_PROJECTS;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);

		waitUntilIndexesReady();
		TypeNameMatchCollector collector = new TypeNameMatchCollector();
		new SearchEngine().searchAllTypeNames(
				"org.apache.commons.lang.enum".toCharArray(),
				SearchPattern.R_EXACT_MATCH,
				"".toCharArray(),
				SearchPattern.R_PREFIX_MATCH,
				IJavaSearchConstants.TYPE,
				scope,
				collector,
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null);
		assertSearchResults("Unexpected search results!", "", collector);
	} finally {
		deleteProject(project);
	}
}
/**
 * bug 325418: [search] Search for method declarations returns spurious potential matches for anonymous classes
 * test search of method declarations of binary anonymous classes using
 * 		 enclosing method's type variables should yield correct results.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=325418"
 */
public void testBug325418a() throws Exception {
	try
	{
		IJavaProject p = createJavaProject("P", new String[] {}, new String[] {"/P/lib325418.jar","JCL15_LIB"}, "","1.5");
		org.eclipse.jdt.core.tests.util.Util.createJar(new String[] {
				"p325418/Test.java",
				"""
					package p325418;
					public class Test{
						public <T> T foo() {
							return new Inner<T>() {T  run() {  return null;  }}.run();
						}
					}
					abstract class Inner <T> {
						 abstract T run();
					}
					"""
			}, p.getProject().getLocation().append("lib325418.jar").toOSString(), "1.5");
			refresh(p);
		//addClasspathEntry(project, JavaCore.newLibraryEntry(new Path("/JavaSearchBugs/lib/b325418.jar"), null, null));
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p }, mask);
		search("Inner.run()", IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, scope, this.resultCollector);
		assertSearchResults("Unexpected search results!",
				"lib325418.jar T p325418.Inner.run() [No source] EXACT_MATCH\n" +
				"lib325418.jar T p325418.<anonymous>.run() [No source] EXACT_MATCH",
				this.resultCollector);
	} finally {
		deleteProject("P");
	}
}
// local named class instead of anonymous class
public void testBug325418b() throws Exception {
	try
	{
		IJavaProject p = createJavaProject("P", new String[] {}, new String[] {"/P/lib325418.jar","JCL15_LIB"}, "","1.5");
		org.eclipse.jdt.core.tests.util.Util.createJar(new String[] {
				"p325418/Test.java",
				"""
					package p325418;
					public class Test {
						public <T> T foo() {
							class ExtendsInner extends Inner<T> {
								T run() { return null; }\s
							}\s
							return null;\s
						}\s
					}\s
					abstract class Inner <T> {
						 abstract T run();
					}"""
			}, p.getProject().getLocation().append("lib325418.jar").toOSString(), "1.5");
			refresh(p);
		//addClasspathEntry(project, JavaCore.newLibraryEntry(new Path("/JavaSearchBugs/lib/b325418.jar"), null, null));
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p }, mask);
		search("Inner.run", IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, scope, this.resultCollector);
		assertSearchResults("Unexpected search results!",
				"lib325418.jar T p325418.Inner.run() [No source] EXACT_MATCH\n" +
				"lib325418.jar T p325418.ExtendsInner.run() [No source] EXACT_MATCH",
				this.resultCollector);
	} finally {
		deleteProject("P");
	}
}
// should work good even if both the inner type and the enclosing methods have type variables
public void testBug325418c() throws Exception {
	try
	{
		IJavaProject p = createJavaProject("P", new String[] {}, new String[] {"/P/lib325418.jar","JCL15_LIB"}, "","1.5");
		org.eclipse.jdt.core.tests.util.Util.createJar(new String[] {
				"p325418/Test.java",
				"""
					package p325418;
					public class Test {
						public <T> T foo() {
							class ExtendsInner<U> extends Inner<T, U> {
								T run() { return null; }\s
								T run(U obj) { return null; }\s
							}\s
							return null;\s
						}\s
					}\s
					abstract class Inner <T, U> {
						 abstract T run();
						 abstract T run(U obj);
					}"""
			}, p.getProject().getLocation().append("lib325418.jar").toOSString(), "1.5");
			refresh(p);
		//addClasspathEntry(project, JavaCore.newLibraryEntry(new Path("/JavaSearchBugs/lib/b325418.jar"), null, null));
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p }, mask);
		search("Inner.run", IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, scope, this.resultCollector);
		assertSearchResults("Unexpected search results!",
				"""
					lib325418.jar T p325418.Inner.run() [No source] EXACT_MATCH
					lib325418.jar T p325418.Inner.run(U) [No source] EXACT_MATCH
					lib325418.jar T p325418.ExtendsInner.run() [No source] EXACT_MATCH
					lib325418.jar T p325418.ExtendsInner.run(U) [No source] EXACT_MATCH""",
				this.resultCollector);
	} finally {
		deleteProject("P");
	}
}
// should work good even if the enclosing method having type variables is more than one level
public void testBug325418d() throws Exception {
	try
	{
		IJavaProject p = createJavaProject("P", new String[] {}, new String[] {"/P/lib325418.jar","JCL15_LIB"}, "","1.5");
		org.eclipse.jdt.core.tests.util.Util.createJar(new String[] {
				"p325418/Test.java",
				"""
					package p325418;
					public class Test {
						public <T> T foo() {
							class Inner {
								T run() {
									return new TwoLevelInner<T>() {T  run() {  return null;  }}.run();
								}
							}
							return null;
						}
					}
					abstract class TwoLevelInner <T> {
						 abstract T run();
					}
					"""
			}, p.getProject().getLocation().append("lib325418.jar").toOSString(), "1.5");
			refresh(p);
		//addClasspathEntry(project, JavaCore.newLibraryEntry(new Path("/JavaSearchBugs/lib/b325418.jar"), null, null));
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { p }, mask);
		search("TwoLevelInner.run", IJavaSearchConstants.METHOD, IJavaSearchConstants.DECLARATIONS, scope, this.resultCollector);
		assertSearchResults("Unexpected search results!",
				"lib325418.jar T p325418.<anonymous>.run() [No source] EXACT_MATCH\n" +
				"lib325418.jar T p325418.TwoLevelInner.run() [No source] EXACT_MATCH",
				this.resultCollector);
	} finally {
		deleteProject("P");
	}
}

/**
 * bug 324189: [search] Method Search returns false results
 * test Search for Worker.run() should not return results like TestWorker
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=324189"
 */
public void testBug324189a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b324189/X.java",
		"""
			package b324189;
			public class TestWorker{
			 public void run() {}
			class AWorker {
			 public void run() {}
			}
			}
			"""
	);
	search("Worker.run()", METHOD, DECLARATIONS);
	assertSearchResults("");
}

// Worker in the default package should be in the result
public void testBug324189b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/Worker.java",
		"""
			public class Worker{
			 public void run() {}
			}
			"""
	);
	search("Worker.run()", METHOD, DECLARATIONS);
	assertSearchResults("src/Worker.java void Worker.run() [run] EXACT_MATCH");
}
// bWorker in the package name should also not be in the search result
public void testBug324189c() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/bWorker/X.java",
		"""
			package bWorker;
			public class X{
			 public void run() {}
			}"""
	);
	search("Worker.X.run()", METHOD, DECLARATIONS);
	assertSearchResults("");
}
// TestWorker in a class file also should not be in the search result
public void testBug324189d() throws CoreException, IOException {
	String libPath = getExternalResourcePath("lib324189.jar");
	try {
		// Create project and external jar file
		Util.createJar(
			new String[] {
				"b324189/TestWorker.java",
				"""
					package b324189;
					public class TestWorker{
					 public void run() {}
					class Worker{
					 public void run() {}
					}
					}""",
				"b324189/Worker.java",
				"""
					package b324189;
					public class Worker{
					 public void run() {}
					}"""
			},
			new HashMap(),
			libPath);
		IJavaProject javaProject = createJavaProject("P", new String[0], new String[] {libPath, "JCL_LIB"}, "");
		waitUntilIndexesReady();
		int mask = IJavaSearchScope.APPLICATION_LIBRARIES | IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { javaProject }, mask);
		this.resultCollector.showSelection();
		search("Worker.run()", METHOD, DECLARATIONS, scope);
		assertSearchResults(
				getExternalPath() + "lib324189.jar void b324189.TestWorker$Worker.run() EXACT_MATCH\n" +
				getExternalPath() + "lib324189.jar void b324189.Worker.run() EXACT_MATCH"
		);
	} finally {
		deleteExternalFile(libPath);
		deleteProject("P");
	}
}
// Test the special case in comment 20 of bug 324189
public void testBug324189e() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b324189/A.java",
		"""
			package b324189;
			public class A{
			 public void run() {}
			}
			class AnotherA {\
			 public void run() {}\s
			 }
			"""
	);
	search("A.run()", METHOD, DECLARATIONS);
	assertSearchResults("src/b324189/A.java void b324189.A.run() [run] EXACT_MATCH");
}
/**
 * bug 336322: [1.7][search]CCE while searching for a type reference in multiple catch parameters
 * test Search for type references in a multiple catch parameters
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=336322"
 */
public void testBug336322a() throws CoreException{
	try
	{
		IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.7");
		createFile("/P/Test.java",
				"""
					public class Test {
					public void foo(Object o) {
					  try {
					   }
					 catch(Exception|RuntimeException exc) {
					   }
					}
					}
					""");
		int mask = IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);
		search("RuntimeException", IJavaSearchConstants.TYPE, IJavaSearchConstants.REFERENCES, scope, this.resultCollector);
		assertSearchResults("Unexpected search results!", "Test.java void Test.foo(Object) [RuntimeException] EXACT_MATCH", this.resultCollector);
	} finally {
		deleteProject("P");
	}
}
// search for type in multiple catch parameters in catch clauses
public void testBug336322b() throws CoreException{
	try
	{
		IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.7");
		createFile("/P/Test.java",
				"""
					public class Test {
					public void foo(Object o) {
					  try {
					   }
					 catch(Exception|RuntimeException exc) {
					   }
					}
					}
					""");
		int mask = IJavaSearchScope.SOURCES ;
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);
		search("RuntimeException", IJavaSearchConstants.TYPE, CATCH_TYPE_REFERENCE, scope, this.resultCollector);
		assertSearchResults("Unexpected search results!", "Test.java void Test.foo(Object) [RuntimeException] EXACT_MATCH", this.resultCollector);
	} finally {
		deleteProject("P");
	}
}
// search for the multi-catch variable should return the variable
public void testBug336322c() throws CoreException{
	try
	{
		IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.7");
		createFile("/P/Test.java",
				"""
					public class Test {
					public void foo(Object o) {
					  try {
					   }
					 catch(Exception|RuntimeException exc) {
					       exc.printStackTrace();
					   }
					}
					}
					""");
		int mask = IJavaSearchScope.SOURCES ;
		IType type = project.findType("Test");
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, mask);
		ILocalVariable variable = selectLocalVariable(type.getCompilationUnit(), "exc");
		search(variable, READ_ACCESSES, scope, this.resultCollector);
		assertSearchResults("Unexpected search results!", "Test.java void Test.foo(Object) [exc] EXACT_MATCH", this.resultCollector);
	} finally {
		deleteProject("P");
	}
}
/**
 * bug 339891: NPE when searching for method (with '*' wildcard character)
 * test Search for Worker.run() should not return results like TestWorker
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=339891"
 */
public void testBug339891() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		createFile("/P/Ref.java",
			"""
				public class Ref{
				 public void foo() {}
				}
				}
				""");
		createFile("/P/Test.java",
			"""
				public class Test{
				 public void foo(Ref ref) {\
				   ref.foo();
				}
				}
				""");
		waitUntilIndexesReady();
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[]{project}, IJavaSearchScope.SOURCES);
		search("Ref.*", METHOD, REFERENCES, EXACT_RULE, scope, this.resultCollector);
		assertSearchResults("Test.java void Test.foo(Ref) [foo()] EXACT_MATCH");
	} finally {
		deleteProject("P");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=341462
public void testBug341462() throws CoreException {
	try
	{
		IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.7");
		createFile("/P/X.java",
				"""
					public class X<T> {
					        T field1;
					        public X(T param) {
					            field1 = param;
					        }
					        public static void testFunction(String param){
					            System.out.println(1);
					        }
					        public static void testFunction(Object Param) {
					            System.out.println(2);
					        }
					        public T getField() {
					            return field1;
					        }
					        public static void main(String[] args) {
					            X.testFunction(new X<>("hello").getField());
					...         X.testFunction(new X<>(new Object()).getField());
					        }
					}
					""");
		waitUntilIndexesReady();
		IType type = project.findType("X");
		IMethod method = type.getMethod("testFunction", new String[] { "QString;" });
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[]{project}, IJavaSearchScope.SOURCES);
		search(method, REFERENCES, ERASURE_RULE, scope, this.resultCollector);
		assertSearchResults("Unexpected search results!", "X.java void X.main(String[]) [testFunction(new X<>(\"hello\").getField())] EXACT_MATCH", this.resultCollector);
	} finally {
		deleteProject("P");
	}
}

public void testBug350885() throws CoreException {
	boolean autoBuild = getWorkspace().isAutoBuilding();
	IWorkspaceDescription preferences = getWorkspace().getDescription();
	try {
		// ensure that the workspace auto-build is ON
		preferences.setAutoBuilding(true);
		getWorkspace().setDescription(preferences);

		IJavaProject project = createJavaProject("P");
		createFile("/P/X.java",
			"""
				class Parent {\
				 public void foo() {}\s
				}
				class Child extends Parent{
				 public void foo() {}
				}
				}
				""");
		waitUntilIndexesReady();

		// search
		IType type = getCompilationUnit("/P/X.java").getType("Child");
		IMethod method = type.getMethods()[0];
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[]{project}, IJavaSearchScope.SOURCES);
		search(method, DECLARATIONS, EXACT_RULE, scope, this.resultCollector);
		assertSearchResults("X.java void Child.foo() [foo] EXACT_MATCH");
	}
	finally {
		// put back initial setup
		preferences.setAutoBuilding(autoBuild);
		getWorkspace().setDescription(preferences);

		// delete the created project
		deleteProject("P");
	}
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=349683
public void testBug349683() throws CoreException {
	try
	{
		IJavaProject project = createJavaProject("P", new String[] {""}, new String[] {"JCL17_LIB"}, "", "1.7");
		createFile("/P/X.java",
				"""
					import java.lang.invoke.MethodHandle;
					import java.lang.invoke.MethodHandles;
					import java.lang.invoke.MethodType;
					
					public class X {
						public static void main(String[] args) throws Throwable {
							Object x;
							String s;
							int i;
							MethodType mt;
							MethodHandle mh;
							MethodHandles.Lookup lookup = MethodHandles.lookup();
							// mt is (char,char)String
							mt = MethodType.methodType(String.class, char.class, char.class);
							mh = lookup.findVirtual(String.class, "replace", mt);
							s = (String) mh.invokeExact("daddy", 'd', 'n');
					     }
					}
					""");
		waitUntilIndexesReady();
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[]{project}, IJavaSearchScope.SOURCES | IJavaSearchScope.SYSTEM_LIBRARIES | IJavaSearchScope.APPLICATION_LIBRARIES);
		search("invokeExact", METHOD, DECLARATIONS, EXACT_RULE, scope, this.resultCollector);
		IMethod method = (IMethod)this.resultCollector.match.getElement();
		this.resultCollector = new TestCollector();
		this.resultCollector.showAccuracy(true);
		search(method, REFERENCES, ERASURE_RULE, scope, this.resultCollector);
		assertSearchResults("Unexpected search results!", "X.java void X.main(String[]) [invokeExact(\"daddy\", \'d\', \'n\')] EXACT_MATCH", this.resultCollector);
	} finally {
		deleteProject("P");
	}
}

// Ensure that results for jar are reported after the source projects
public void testBug345807() throws CoreException {
	try {
		// Create a project depending on the jar
		IJavaProject p1 = createJavaProject("P1", new String[] {}, new String[] {"/P1/01b345807.jar"}, "");
		createJar(new String[] {
			"inlib/P345807Test.java",
			"""
				package inlib;
				public class P345807Test {
				}"""
		}, p1.getProject().getLocation().append("01b345807.jar").toOSString());
		refresh(p1);

		// Create another project with the same class name
		createJavaProject("Project2", new String[] {""}, new String[] {}, "");
		createFile("/Project2/P345807Test.java",
				"public class P345807Test {\n" +
				"}\n");

		waitUntilIndexesReady();
		SearchTests.SearchTypeNameRequestor requestor =  new SearchTests.SearchTypeNameRequestor();
		new SearchEngine().searchAllTypeNames(
				null,
				SearchPattern.R_EXACT_MATCH, // case insensitive
				"P345807Test".toCharArray(),
				SearchPattern.R_EXACT_MATCH, // case insensitive
				TYPE,
				SearchEngine.createWorkspaceScope(),
				requestor,
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				null
			);
		assertEquals("Results not in proper order", "P345807Test\ninlib.P345807Test", requestor.unsortedString());
	} catch (IOException e) {
		assertTrue(false); // report a failure
	} finally {
		deleteProject("P1");
		deleteProject("Project2");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=355605
public void testBug355605() throws CoreException {
	try {
	createJavaProject("P");

		String fileContent =
			"""
			public class X {\s
			class R {
			   class S {
			   	void setInfo(String x) {
			   	}
			   }
			   class T {
			   }
				T t = new T()  {
					S s = new S() {
			           void myMethod() {
			               setInfo("a");
			           }
			      };// S ends
			   };
			}
			}
			""" ;
		createFile("/P/X.java", fileContent);

		waitUntilIndexesReady();
		this.resultCollector = new TestCollector();
		this.resultCollector.showAccuracy(true);
		ICompilationUnit unit = getCompilationUnit("/P/X.java");
		IMethod method = selectMethod(unit, "myMethod", 1);
		IJavaSearchScope hierarchyScope = SearchEngine.createHierarchyScope((IType)method.getParent());
		search(method, IMPLEMENTORS, EXACT_RULE, hierarchyScope, this.resultCollector);
		assertSearchResults("Unexpected search results!", "X.java void X$R.t:<anonymous>#1.s:<anonymous>#1.myMethod() [myMethod] EXACT_MATCH", this.resultCollector);

	} finally {
		deleteProject("P");
	}
}
/**
 * bug 241834: [search] ClassCastException during move class refactoring
 * test that search for declarations of referenced types doesn't cause CCE
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=241834"
 */
public void testBug241834() throws CoreException {
	try {
		IJavaProject project = createJavaProject("P");
		project.setOption(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		createFolder("/P/pkg");
		createFile("/P/pkg/Foo.java",
				"""
					package pkg;
					/**
					  * {@link missing.Foo}
					  */
					public class Foo {
					}
					""");
		waitUntilIndexesReady();
		IType type = getCompilationUnit("/P/pkg/Foo.java").getType("Foo");
		searchDeclarationsOfReferencedTypes(type, this.resultCollector);
		assertSearchResults("");
	} finally {
		deleteProject("P");

	}
}
	/**
	 * bug402902:  [1.8][search] Search engine fails to annotation matches in extends/implements clauses
	 * test Ensures that the search for type use annotation finds matches
	 * in extends and implements clauses.
	 *
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=402902"
	 */
public void testBug400902a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400902/X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				import java.io.Serializable;
				@Marker1 @Marker public class X extends @Marker Object implements @Marker Serializable {
					private static final long serialVersionUID = 1L;
					int x = (@Marker int) 0;
				 	@Marker public class Y {}
				}
				@Target(ElementType.TYPE_USE)
				@interface Marker {}
				@Target(ElementType.TYPE)
				@interface Marker1 {}"""
		);
	SearchPattern pattern = SearchPattern.createPattern(
			"Marker",
			ANNOTATION_TYPE,
			REFERENCES,
			EXACT_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
	new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
	getJavaSearchWorkingCopiesScope(),
	this.resultCollector,
	null);
	assertSearchResults(
			"""
				src/b400902/X.java b400902.X [Marker] EXACT_MATCH
				src/b400902/X.java b400902.X [Marker] EXACT_MATCH
				src/b400902/X.java b400902.X [Marker] EXACT_MATCH
				src/b400902/X.java b400902.X.x [Marker] EXACT_MATCH
				src/b400902/X.java b400902.X$Y [Marker] EXACT_MATCH"""
	);
}

/**
 * bug 400919:  [1.8][search] Search engine fails to annotation matches in type variable bounds
 * test Ensures that the search for type use annotation finds matches in type variable bounds
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400919"
 */
public void testBug400919a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400919/X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				import java.util.Collection;
				
				interface I {
					I doit();
				}
				
				@Marker public class X {
				   @SuppressWarnings("unused")
					@Marker <@Existing T>  int x(@Existing T t) { return 10; };
					/**
					 * @param <F> \s
					 */
					class Folder<@Existing  F extends @Existing XYZ> {  }
					Collection<@Existing ? super @Existing XYZ> s;
					/**
					 * @param <T> \s
					 */
					class Test <T extends Outer.@Existing Inner> {}
				}
				
				class Y extends  Object  {
					int x = ( int) 0;
				}
				
				/**
				 * @param <T> \s
				 */
				class XY<@Existing T> {}
				class XYZ {}
				
				class Outer {
					class Inner {
					\t
					}
				}
				/**
				 * @param <T>\s
				 * @param <Q> \s
				 */
				class X2 <@Marker T extends @Marker Y2<@Marker ? extends @Marker X>, @Marker Q extends @Marker Object> {
				}
				/**
				 * @param <T> \s
				 */
				class Y2<T> {}
				@Target(ElementType.TYPE_USE)
				@interface Existing {
				\t
				}
				@Target (ElementType.TYPE_USE)
				@interface Marker {}
				"""
		);
	SearchPattern pattern = SearchPattern.createPattern(
			"Existing",
			ANNOTATION_TYPE,
			REFERENCES,
			EXACT_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
	new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
	getJavaSearchWorkingCopiesScope(),
	this.resultCollector,
	null);
	assertSearchResults(
			"""
				src/b400919/X.java b400919.X.s [Existing] EXACT_MATCH
				src/b400919/X.java b400919.X.s [Existing] EXACT_MATCH
				src/b400919/X.java int b400919.X.x(T) [Existing] EXACT_MATCH
				src/b400919/X.java int b400919.X.x(T) [Existing] EXACT_MATCH
				src/b400919/X.java b400919.X$Folder [Existing] EXACT_MATCH
				src/b400919/X.java b400919.X$Folder [Existing] EXACT_MATCH
				src/b400919/X.java b400919.X$Test [Existing] EXACT_MATCH
				src/b400919/X.java b400919.XY [Existing] EXACT_MATCH"""
	);
}
/**
 * bug 400919:  [1.8][search] Search engine fails to annotation matches in type variable bounds
 * test Ensures that the search for type use annotation finds matches in type variable bounds with TYPE
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400919"
 */
public void testBug400919b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400919/X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				import java.util.Collection;
				
				interface I {
					I doit();
				}
				
				@Marker public class X {
				   @SuppressWarnings("unused")
					@Marker <@Existing T>  int x(@Existing T t) { return 10; };
					/**
					 * @param <F> \s
					 */
					class Folder<@Existing  F extends @Existing XYZ> {  }
					Collection<@Existing ? super @Existing XYZ> s;
					/**
					 * @param <T> \s
					 */
					class Test <T extends Outer.@Existing Inner> {}
				}
				
				class Y extends  Object  {
					int x = ( int) 0;
				}
				
				/**
				 * @param <T> \s
				 */
				class XY<@Existing T> {}
				class XYZ {}
				
				class Outer {
					class Inner {
					\t
					}
				}
				/**
				 * @param <T>\s
				 * @param <Q> \s
				 */
				class X2 <@Marker T extends @Marker Y2<@Marker ? extends @Marker X>, @Marker Q extends @Marker Object> {
				}
				/**
				 * @param <T> \s
				 */
				class Y2<T> {}
				@Target(ElementType.TYPE_USE)
				@interface Existing {
				\t
				}
				@Target (ElementType.TYPE_USE)
				@interface Marker {}
				"""
		);
	SearchPattern pattern = SearchPattern.createPattern(
			"Existing",
			TYPE,
			REFERENCES,
			EXACT_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
	new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
	getJavaSearchWorkingCopiesScope(),
	this.resultCollector,
	null);
	assertSearchResults(
			"""
				src/b400919/X.java b400919.X.s [Existing] EXACT_MATCH
				src/b400919/X.java b400919.X.s [Existing] EXACT_MATCH
				src/b400919/X.java int b400919.X.x(T) [Existing] EXACT_MATCH
				src/b400919/X.java int b400919.X.x(T) [Existing] EXACT_MATCH
				src/b400919/X.java b400919.X$Folder [Existing] EXACT_MATCH
				src/b400919/X.java b400919.X$Folder [Existing] EXACT_MATCH
				src/b400919/X.java b400919.X$Test [Existing] EXACT_MATCH
				src/b400919/X.java b400919.XY [Existing] EXACT_MATCH"""
	);
}
/**
 * bug 400919:  [1.8][search] Search engine fails to annotation matches in type variable bounds
 * test Ensures that the search for type use annotation finds matches in type variable bounds
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=400919"
 */
public void testBug400919c() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/b400919/X.java",
			"""
				import java.lang.annotation.ElementType;
				import java.lang.annotation.Target;
				import java.util.Collection;
				
				interface I {
					I doit();
				}
				
				@Marker public class X {
				   @SuppressWarnings("unused")
					@Marker <T>  int x(T t) { return 10; };
					/**
					 * @param <F> \s
					 */
					class Folder<@Existing  F extends @Existing XYZ> {  }
					Collection<? super @Existing XYZ> s;
					/**
					 * @param <T> \s
					 */
					class Test <T extends Outer.@Existing Inner> {}
				}
				
				class Y extends  Object  {
					int x = ( int) 0;
				}
				
				/**
				 * @param <T> \s
				 */
				class XY<@Existing T> {}
				class XYZ {}
				
				class Outer {
					class Inner {
					\t
					}
				}
				/**
				 * @param <T>\s
				 * @param <Q> \s
				 */
				class X2 <@Marker T extends @Marker Y2<@Marker ? extends @Marker X>, @Marker Q extends @Marker Object> {
				}
				/**
				 * @param <T> \s
				 */
				class Y2<T> {}
				@Target(ElementType.TYPE_USE)
				@interface Existing {
				\t
				}
				@Target (ElementType.TYPE_USE)
				@interface Marker {}
				"""
		);
	SearchPattern pattern = SearchPattern.createPattern(
			"Marker",
			ANNOTATION_TYPE,
			REFERENCES,
			EXACT_RULE);
	new SearchEngine(this.workingCopies).search(pattern,
	new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
	getJavaSearchWorkingCopiesScope(),
	this.resultCollector,
	null);
	assertSearchResults(
			"""
				src/b400919/X.java b400919.X [Marker] EXACT_MATCH
				src/b400919/X.java int b400919.X.x(T) [Marker] EXACT_MATCH
				src/b400919/X.java b400919.X2 [Marker] EXACT_MATCH
				src/b400919/X.java b400919.X2 [Marker] EXACT_MATCH
				src/b400919/X.java b400919.X2 [Marker] EXACT_MATCH
				src/b400919/X.java b400919.X2 [Marker] EXACT_MATCH
				src/b400919/X.java b400919.X2 [Marker] EXACT_MATCH
				src/b400919/X.java b400919.X2 [Marker] EXACT_MATCH"""
	);
}
/** bug 431357
 * [search] Search API got wrong result, when searching for method references, where the parameter is a member type of another type.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=431357"
 */
public void testBug431357_001() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"""
				interface I {\s
				    public void query(Foo.InnerKey key);// Search result of method query(Foo.InnerKey) returns the method query(Bar.InnerKey) too\s
				    public void query(Bar.InnerKey key);
				}
				
				class Foo {\s
				    static class InnerKey  {}
				}
				class Bar {
				    static class InnerKey {}
				}
				
				class X {
					public static void foo(I i, Foo.InnerKey key) {
						i.query(key);
					}
					public static void bar(I i, Bar.InnerKey key) {
						i.query(key);
					}
					public static I getInstance() {
						return null;
					}
				}
				"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "query";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	MethodPattern pattern = (MethodPattern) SearchPattern.createPattern(elements[0], REFERENCES, EXACT_RULE | ERASURE_RULE);

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(
			"src/X.java void X.foo(I, Foo.InnerKey) [query(key)] EXACT_MATCH"
	);
}
/** bug 431357
 * [search] Search API got wrong result, when searching for method references, where the parameter is a member type of another type.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=431357"
 */
public void testBug431357_002() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"""
				interface I {\s
				    public void query(Foo.InnerKey key);// Search result of method query(Foo.InnerKey) returns the method query(Bar.InnerKey) too\s
				    public void query(Bar.InnerKey key);
				}
				
				class Foo {\s
				    static class InnerKey  {}
				}
				class Bar {
				    static class InnerKey {}
				}
				
				class X {
					public static void foo(I i, Foo.InnerKey key) {
						i.query(key);
					}
					public static void bar(I i, Bar.InnerKey key) {
						i.query(key);
					}
					public static I getInstance() {
						return null;
					}
				}
				"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "query";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	MethodPattern pattern = (MethodPattern) SearchPattern.createPattern(elements[0], ALL_OCCURRENCES, EXACT_RULE  | ERASURE_RULE);

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults( "src/X.java void I.query(Foo.InnerKey) [query] EXACT_MATCH\n" +
			"src/X.java void X.foo(I, Foo.InnerKey) [query(key)] EXACT_MATCH"
	);
}
/** bug 431357
 * [search] Search API got wrong result, when searching for method references, where the parameter is a member type of another type.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=431357"
 */
public void testBug431357_003() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"""
				interface I {\s
				    public void query(Foo.InnerKey key);// Search result of method query(Foo.InnerKey) returns the method query(Bar.InnerKey) too\s
				    public void query/*here*/(Bar.InnerKey key);
				}
				
				class Foo {\s
				    static class InnerKey  {}
				}
				class Bar {
				    static class InnerKey {}
				}
				
				class X {
					public static void foo(I i, Foo.InnerKey key) {
						i.query(key);
					}
					public static void bar(I i, Bar.InnerKey key) {
						i.query(key);
					}
					public static I getInstance() {
						return null;
					}
				}
				"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "query/*here*/";
	int start = str.indexOf(selection);
	int length = "query".length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	MethodPattern pattern = (MethodPattern) SearchPattern.createPattern(elements[0], ALL_OCCURRENCES, EXACT_RULE | ERASURE_RULE);

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults("src/X.java void I.query(Bar.InnerKey) [query] EXACT_MATCH\n" +
			"src/X.java void X.bar(I, Bar.InnerKey) [query(key)] EXACT_MATCH"
	);
}
/** bug 431357
 * [search] Search API got wrong result, when searching for method references, where the parameter is a member type of another type.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=431357"
 */
public void testBug431357_004() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"""
				// --
				interface I {\s
				    public void query/*here*/(Foo.Key key);// Search result of method query(Foo.Key) returns the method query(Bar.Key) too\s
				    public void query(Key key);
				}
				
				class Foo {\s
					static class Key  {\t
					}
					public static void foo(I i, Key key) {
						i.query(key);
					}
				\t
				}
				
				class Key {
				\t
				}
				class Bar {
				   \s
				    public static void bar(I i, Key key) {
						i.query(key);
				    }
				}
				
				public class X {
					public static I getInstance() {
						return null;
					}
				}
				"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "query/*here*/";
	int start = str.indexOf(selection);
	int length = "query".length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	MethodPattern pattern = (MethodPattern) SearchPattern.createPattern(elements[0], ALL_OCCURRENCES, EXACT_RULE | ERASURE_RULE);

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(
			"src/X.java void I.query(Foo.Key) [query] EXACT_MATCH\n" +
			"src/X.java void Foo.foo(I, Key) [query(key)] EXACT_MATCH"
	);
}
/** bug 431357
 * [search] Search API got wrong result, when searching for method references, where the parameter is a member type of another type.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=431357"
 */
public void testBug431357_005() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"""
				// --
				interface I {\s
				    public void query(Foo.Key key);// Search result of method query(Foo.Key) returns the method query(Bar.Key) too\s
				    public void query/*here*/(Key key);
				}
				
				class Foo {\s
					static class Key  {\t
					}
					public static void foo(I i, Key key) {
						i.query(key);
					}
				\t
				}
				
				class Key {
				\t
				}
				class Bar {
				   \s
				    public static void bar(I i, Key key) {
						i.query(key);
				    }
				}
				
				public class X {
					public static I getInstance() {
						return null;
					}
				}
				"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "query/*here*/";
	int start = str.indexOf(selection);
	int length = "query".length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	MethodPattern pattern = (MethodPattern) SearchPattern.createPattern(elements[0], ALL_OCCURRENCES, EXACT_RULE | ERASURE_RULE);

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(
			"src/X.java void I.query(Key) [query] EXACT_MATCH\n" +
			"src/X.java void Bar.bar(I, Key) [query(key)] EXACT_MATCH"
	);
}
/** bug 431357
 * [search] Search API got wrong result, when searching for method references, where the parameter is a member type of another type.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=431357"
 */
public void testBug431357_006() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"""
				// --
				interface I {\s
				    public void query/*here*/(Foo.Key key);// Search result of method query(Foo.Key) returns the method query(Bar.Key) too\s
				    public void query(Key key);
				    public void query(Bar.Key key);
				}
				
				class Foo {\s
					static class Key  {\t
					}
					public static void foo(I i, Key key) {
						i.query(key);
					}
				\t
				}
				
				class Key {
				\t
				}
				class Bar {
					static class Key {
					\t
					}   \s
				    public static void bar(I i, Key key) {
						i.query(key);
				    }
				}
				
				public class X {
					public static I getInstance() {
						return null;
					}
				    public static void bar(I i, Key key) {
						i.query(key);
				    }
				}
				"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "query/*here*/";
	int start = str.indexOf(selection);
	int length = "query".length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	MethodPattern pattern = (MethodPattern) SearchPattern.createPattern(elements[0], ALL_OCCURRENCES, EXACT_RULE | ERASURE_RULE);

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(
			"src/X.java void I.query(Foo.Key) [query] EXACT_MATCH\n" +
			"src/X.java void Foo.foo(I, Key) [query(key)] EXACT_MATCH"
	);
}
/** bug 431357
 * [search] Search API got wrong result, when searching for method references, where the parameter is a member type of another type.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=431357"
 */
public void testBug431357_007() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"""
				// --
				interface I {\s
				    public void query(Foo.Key key);// Search result of method query(Foo.Key) returns the method query(Bar.Key) too\s
				    public void query/*here*/(Key key);
				    public void query(Bar.Key key);
				}
				
				class Foo {\s
					static class Key  {\t
					}
					public static void foo(I i, Key key) {
						i.query(key);
					}
				\t
				}
				
				class Key {
				\t
				}
				class Bar {
					static class Key {
					\t
					}   \s
				    public static void bar(I i, Key key) {
						i.query(key);
				    }
				}
				
				public class X {
					public static I getInstance() {
						return null;
					}
				    public static void bar(I i, Key key) {
						i.query(key);
				    }
				}
				"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "query/*here*/";
	int start = str.indexOf(selection);
	int length = "query".length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	MethodPattern pattern = (MethodPattern) SearchPattern.createPattern(elements[0], ALL_OCCURRENCES, EXACT_RULE | ERASURE_RULE);

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(
			"src/X.java void I.query(Key) [query] EXACT_MATCH\n" +
			"src/X.java void X.bar(I, Key) [query(key)] EXACT_MATCH"
	);
}

/** bug 431357
 * [search] Search API got wrong result, when searching for method references, where the parameter is a member type of another type.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=431357"
 */
public void testBug431357_008() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"""
				// --
				interface I {\s
				    public void query(Foo.Key key);// Search result of method query(Foo.Key) returns the method query(Bar.Key) too\s
				    public void query(Key key);
				    public void query/*here*/(Bar.Key key);
				}
				
				class Foo {\s
					static class Key  {\t
					}
					public static void foo(I i, Key key) {
						i.query(key);
					}
				\t
				}
				
				class Key {
				\t
				}
				class Bar {
					static class Key {
					\t
					}   \s
				    public static void bar(I i, Key key) {
						i.query(key);
				    }
				}
				
				public class X {
					public static I getInstance() {
						return null;
					}
				    public static void bar(I i, Key key) {
						i.query(key);
				    }
				}
				"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "query/*here*/";
	int start = str.indexOf(selection);
	int length = "query".length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	MethodPattern pattern = (MethodPattern) SearchPattern.createPattern(elements[0], ALL_OCCURRENCES, EXACT_RULE | ERASURE_RULE);

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(
			"src/X.java void I.query(Bar.Key) [query] EXACT_MATCH\n" +
			"src/X.java void Bar.bar(I, Key) [query(key)] EXACT_MATCH"
	);
}

/** bug 431357
 * [search] Search API got wrong result, when searching for method references, where the parameter is a member type of another type.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=431357"
 */
public void testBug431357_009() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
		"""
			interface MyIF {\s
			    public void query(Foo.InnerKey fk, Bar.InnerKey bk, String s);\s
			    public void query/*here*/(Bar.InnerKey fk, Bar.InnerKey bk, String s);
			}
			
			class Foo {\s
			    static class InnerKey  {   \s
			    }
			
			}
			
			class Bar {
			    static class InnerKey {
			    }
			    public static void bar(MyIF i, Foo.InnerKey fk, Bar.InnerKey bk) {
			        i.query(fk, bk, "");
			    }
			}
			public class X {}
			"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "query/*here*/";
	int start = str.indexOf(selection);
	int length = "query".length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	MethodPattern pattern = (MethodPattern) SearchPattern.createPattern(elements[0], REFERENCES, EXACT_RULE | ERASURE_RULE);

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(""
	);
}

/** bug 431357
 * [search] Search API got wrong result, when searching for method references, where the parameter is a member type of another type.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=431357"
 */
public void testBug431357_010() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
		"""
			interface MyIF {\s
			    public void query(Foo.InnerKey fk,  String s);\s
			    public void query/*here*/(Bar.InnerKey fk,  String s);
			}
			
			class Foo {\s
			    static class InnerKey  {}
			}
			
			class Bar {
			    static class InnerKey {}
			    public static void bar(MyIF i, Foo.InnerKey fk) {
			        i.query(fk, "");
			    }
			}
			public class X {}
			"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "query/*here*/";
	int start = str.indexOf(selection);
	int length = "query".length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	MethodPattern pattern = (MethodPattern) SearchPattern.createPattern(elements[0], REFERENCES, EXACT_RULE | ERASURE_RULE);

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(""
	);
}

/** bug 431357
 * [search] Search API got wrong result, when searching for method references, where the parameter is a member type of another type.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=431357"
 */
public void testBug431357_011() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
		"""
			interface MyIF {\s
			   public void query(String s, Foo.InnerKey fk);\s
			}
			
			class Foo {\s
				static class InnerKey  {}
			}
			
			class Bar {
				static class InnerKey {}
				public static void bar(MyIF i, Foo.InnerKey fk) {
					i.query("", fk);
			    }
			}
			public class X {}
			"""
	);

	String nonExistentPattern = "MyIF.query(String, Bar.InnerKey)";
	MethodPattern pattern = (MethodPattern) SearchPattern.createPattern(nonExistentPattern, IJavaSearchConstants.METHOD, REFERENCES, EXACT_RULE | ERASURE_RULE);

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(""
	);
}

/** bug 431357
 * [search] Search API got wrong result, when searching for method references, where the parameter is a member type of another type.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=431357"
 */
public void testBug431357_012() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
		"""
			interface MyIF {\s
			    public void query/*here*/(Foo.InnerKey fk, Bar.InnerKey bk, String s);\s
			    public void query(Bar.InnerKey fk, Bar.InnerKey bk, String s);
			}
			
			class Foo {\s
				static class InnerKey  {\t
				}
			\t
			}
			
			class Bar {
				static class InnerKey extends Foo.InnerKey {
				}
				public static void bar(MyIF i, Foo.InnerKey fk, Bar.InnerKey bk) {
					i.query(fk, bk, "");
			    }
			}
			public class X {}
			"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "query/*here*/";
	int start = str.indexOf(selection);
	int length = "query".length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	MethodPattern pattern = (MethodPattern) SearchPattern.createPattern(elements[0], ALL_OCCURRENCES, EXACT_RULE | ERASURE_RULE);

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults("src/X.java void MyIF.query(Foo.InnerKey, Bar.InnerKey, String) [query] EXACT_MATCH\n" +
			"src/X.java void Bar.bar(MyIF, Foo.InnerKey, Bar.InnerKey) [query(fk, bk, \"\")] EXACT_MATCH"
	);
}

/** bug 431357
 * [search] Search API got wrong result, when searching for method references, where the parameter is a member type of another type.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=431357"
 */
public void testBug431357_013() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
		"""
			interface MyIF {\s
			    public void query/*here*/(Foo.InnerKey key);\s
			    public void query(Bar.InnerKey key);
			}
			
			class Foo {\s
				static class InnerKey  {\t
				}
			\t
			}
			
			class Bar {
				static class InnerKey{}
			}
			public class X {}
			"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "query/*here*/";
	int start = str.indexOf(selection);
	int length = "query".length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	MethodPattern pattern = (MethodPattern) SearchPattern.createPattern(elements[0], ALL_OCCURRENCES, EXACT_RULE | ERASURE_RULE);

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults("src/X.java void MyIF.query(Foo.InnerKey) [query] EXACT_MATCH"
	);
}

/** bug 431357
 * [search] Search API got wrong result, when searching for method references, where the parameter is a member type of another type.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=431357"
 */
public void testBug431357_014() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
		"""
			interface MyIF {\s
			    public void query/*here*/(Foo.InnerKey key);\s
			    public void query(Bar.InnerKey key);
			}
			
			class Foo {\s
				static class InnerKey  {\t
				}
			\t
			}
			
			class Bar {
				static class InnerKey{}
			}
			public class X {}
			"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "query/*here*/";
	int start = str.indexOf(selection);
	int length = "query".length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	MethodPattern pattern = (MethodPattern) SearchPattern.createPattern(elements[0], DECLARATIONS, EXACT_RULE | ERASURE_RULE);

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults("src/X.java void MyIF.query(Foo.InnerKey) [query] EXACT_MATCH");
}

/** bug 431357
 * [search] Search API got wrong result, when searching for method references, where the parameter is a member type of another type.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=431357"
 */
public void testBug431357_015() throws CoreException {
	String folder = "/JavaSearchBugs/src/testBug431357_015";
	String filename = folder + "/" + "X.java";
	try {
		String contents =
		"""
			package testBug431357_015;
			interface MyIF {\s
			    public void query/*here*/(Foo.InnerKey key);\s
			    public void query(Bar.InnerKey key);
			}
			
			class Foo {\s
				static class InnerKey  {\t
				}
			\t
			}
			
			class Bar {
				static class InnerKey{}
			}
			public class X {}
			""";
		// create files
		createFolder(folder);
		createFile(filename, contents);
		waitUntilIndexesReady();

		// search
		IType[] types = getCompilationUnit(filename).getTypes();
		IMethod method = types[0].getMethods()[0];
		search(method, DECLARATIONS | IJavaSearchConstants.IGNORE_DECLARING_TYPE | IJavaSearchConstants.IGNORE_RETURN_TYPE, ERASURE_RULE);
		assertSearchResults("src/testBug431357_015/X.java void testBug431357_015.MyIF.query(Foo.InnerKey) [query] EXACT_MATCH");
	}
	finally {
		// delete files
		deleteFolder(folder);
	}
}

/** bug 431357
 * [search] Search API got wrong result, when searching for method references, where the parameter is a member type of another type.
 * enable this once 88997 is fixed
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=431357"
 */
public void _testBug431357_016() throws CoreException {
	String folder = "/JavaSearchBugs/src/testBug431357_016";
	String filename = folder + "/" + "X.java";
	try {
		String contents =
			"""
			package testBug431357_016;
			interface I {\s
			 \
			    public void query(Foo.Key key);
			    public void query/*here*/(Key key);
			 \
			}
			 \
			
			 \
			class Foo {\s
			 \
				static class Key  {\t
			 \
				}
			 \
				public static void foo(I i, Key key) {
			 \
					i.query(key);
			 \
				}
			 \
			\t
			 \
			}
			 \
			
			 \
			class Key {
			 \
			\t
			 \
			}
			 \
			class Bar {
			 \
			   \s
			 \
			    public static void bar(I i, Key key) {
			 \
					i.query(key);
			 \
			    }
			 \
			}
			 \
			
			 \
			public class X {
			 \
				public static I getInstance() {
			 \
					return null;
			 \
				}
			 \
			}
			 """;
		// create files
		createFolder(folder);
		createFile(filename, contents);
		waitUntilIndexesReady();

		// search
		IType[] types = getCompilationUnit(filename).getTypes();
		IMethod method = types[0].getMethods()[1];
		search(method, DECLARATIONS | IJavaSearchConstants.IGNORE_DECLARING_TYPE | IJavaSearchConstants.IGNORE_RETURN_TYPE, ERASURE_RULE);
		assertSearchResults("src/testBug431357_016/X.java void testBug431357_016.I.query(Key) [query] EXACT_MATCH");
	}
	finally {
		// delete files
		deleteFolder(folder);
	}
}
public void testBug460465_since_5() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/test/TestE.java",
		"""
			package test;
			public enum TestE {
				TEST1,
				TEST2;
			}
			""" );
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/test/ClassWithoutStaticImports.java",
			"""
				package test;
				public class ClassWithoutStaticImports {
					public ClassWithoutStaticImports() {
						System.out.println(TestE.TEST1);
						System.out.println(TestE.TEST2);
					}
				}
				""" );
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/test/ClassWithStaticImports.java",
			"""
				package test;
				
				import static test.TestE.TEST1;
				import static test.TestE.TEST2;
				
				public class ClassWithStaticImports {
				
					public ClassWithStaticImports() {
				
						System.out.println(TEST1);
						System.out.println(TEST2);
					}
				}
				""");


	IType type = this.workingCopies[0].getTypes()[0];
	TypeReferencePattern pattern = (TypeReferencePattern) SearchPattern.createPattern(type, REFERENCES, EXACT_RULE | ERASURE_RULE);

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(
			"""
				src/test/ClassWithStaticImports.java [test.TestE] EXACT_MATCH
				src/test/ClassWithStaticImports.java [test.TestE] EXACT_MATCH
				src/test/ClassWithoutStaticImports.java test.ClassWithoutStaticImports() [TestE] EXACT_MATCH
				src/test/ClassWithoutStaticImports.java test.ClassWithoutStaticImports() [TestE] EXACT_MATCH""");
}
public void testBug469320_0001() throws CoreException {
	IJavaProject ProjectA = null;
	IJavaProject ProjectB = null;
	try	{
		ProjectA = createJavaProject("ProjectA", new String[] {""}, new String[] {"JCL15_LIB"}, "","1.5");
		IFile f = getFile("/JavaSearchBugs/lib/common.jar");
		this.createFile("/ProjectA/common.jar", f.getContents());
		this.addLibraryEntry(ProjectA, "/ProjectA/common.jar", false);
		createFolder("/ProjectA/test");
		createFile("/ProjectA/test/Validation.java",
				"""
					package test;
					public final class Validation {
					    public static boolean validate(String traceTypeName, String fileName) {
					        ValidationHelper helper = new ValidationHelper();
					        helper.validate(""); //$NON-NLS1$
					        return true;
					    }
					}
					""");
		createFile("/ProjectA/test/ValidationHelper.java",
				"""
					package test;
					public class ValidationHelper {
						public String validate(String path) {
							return null;
						}
					}
					""");
		ProjectB = createJavaProject("ProjectB", new String[] {""}, new String[] {"JCL15_LIB"}, "","1.5");
		//this.createFile("/ProjectB/common.jar", f.getContents());
		this.addLibraryEntry(ProjectB, "/ProjectA/common.jar", false);
		createFolder("/ProjectB/testReferences");
		createFile("/ProjectB/testReferences/Main.java",
				"""
					package testReferences;
					import validator.*;
					public class Main {
					
						public static void main(String[] args) {
					        Validator validator = new Validator();
					        validator.validate(new StreamSource());
						}
					
					}
					""");

		waitUntilIndexesReady();
		// search
		IOrdinaryClassFile classFile = getClassFile("ProjectA", "common.jar", "validator", "Validator.class");
		IType type = classFile.getType();
		IMethod method = type.getMethods()[1];
		search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
		assertSearchResults("testReferences/Main.java void testReferences.Main.main(String[]) [validate(new StreamSource())] EXACT_MATCH");
	} finally {
		if (ProjectB != null) deleteProject(ProjectB);
		if (ProjectA != null) deleteProject(ProjectA);
	}
}
/** bug 476738
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=476738"
 */
public void testBug476738_001() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"""
				interface I {\s
				    public void query(Foo.InnerKey key);// Search result of method query(Foo.InnerKey) returns the method query(Bar.InnerKey) too\s
				    public void query(Bar.InnerKey key);
				}
				
				class Foo {\s
				    static class InnerKey  {}
				}
				class Bar {
				    static class InnerKey {}
				}
				
				class X {
					public static void foo(I i, Foo.InnerKey key) {
						i.query(key);
					}
					public static void bar(I i, Bar.InnerKey key) {
						i.query(key);
					}
					public static I getInstance() {
						return null;
					}
				}
				"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "query";
	int start = str.indexOf(selection);
	int length = selection.length();

	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	MethodPattern leftPattern = (MethodPattern) SearchPattern.createPattern(elements[0], REFERENCES, EXACT_RULE | ERASURE_RULE);
	MethodPattern rightPattern = (MethodPattern) SearchPattern.createPattern(elements[0], REFERENCES, EXACT_RULE | ERASURE_RULE);
	SearchPattern pattern = SearchPattern.createOrPattern(leftPattern, rightPattern);
	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(
			"src/X.java void X.foo(I, Foo.InnerKey) [query(key)] EXACT_MATCH"
	);
}
/** bug 476738
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=476738"
 */
public void testBug476738_002() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/X.java",
			"""
				interface I {\s
				    public void query/* one */(Foo.InnerKey key);// Search result of method query(Foo.InnerKey) returns the method query(Bar.InnerKey) too\s
				    public void query/* two */(Bar.InnerKey key);
				}
				
				class Foo {\s
				    static class InnerKey  {}
				}
				class Bar {
				    static class InnerKey {}
				}
				
				class X {
					public static void foo(I i, Foo.InnerKey key) {
						i.query(key);
					}
					public static void bar(I i, Bar.InnerKey key) {
						i.query(key);
					}
					public static I getInstance() {
						return null;
					}
				}
				"""
	);

	String str = this.workingCopies[0].getSource();
	String selection = "query";
	String selection1 = "query/* one */";
	String selection2 = "query/* two */";
	int start = str.indexOf(selection1);
	int length = selection.length();
	IJavaElement[] elements = this.workingCopies[0].codeSelect(start, length);
	MethodPattern leftPattern = (MethodPattern) SearchPattern.createPattern(elements[0], REFERENCES, EXACT_RULE | ERASURE_RULE);

	start = str.indexOf(selection2);
	length = selection.length();
	elements = this.workingCopies[0].codeSelect(start, length);
	MethodPattern rightPattern = (MethodPattern) SearchPattern.createPattern(elements[0], REFERENCES, EXACT_RULE | ERASURE_RULE);

	SearchPattern pattern = SearchPattern.createOrPattern(leftPattern, rightPattern);
	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(
			"src/X.java void X.foo(I, Foo.InnerKey) [query(key)] EXACT_MATCH\n" +
			"src/X.java void X.bar(I, Bar.InnerKey) [query(key)] EXACT_MATCH"
	);
}
public void testBug478042_wScope_0001() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java",
		"""
			package p478042;
			public class AllMethodDeclarations01 {
			  public void foo01() {}
			  public int foo02(Object o) {return null;}
			  public char foo03(Object o, String s) {return null;}
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java",
		"""
			package p478042;
			public class AllMethodDeclarations01b {
			  public Integer fooInt() {return null;}
			}
			"""
	);
	MethodDeclarationsCollector requestor = new MethodDeclarationsCollector();
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	searchAllMethodNames("foo", SearchPattern.R_PREFIX_MATCH, scope, requestor);
	assertSearchResults(
			"""
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.foo03(Object o,String s)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.foo02(Object o)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.foo01()
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooInt()""",
			requestor
	);
}
public void testBug478042_wScope_0002() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java",
		"""
			package p478042;
			public class AllMethodDeclarations01 {
			  public void foo01() {}
			  public int foo02(Object o) {return null;}
			  public char foo03(Object o, String s) {return null;}
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java",
		"""
			package p478042;
			public class AllMethodDeclarations01b {
			  public Integer fooInt() {return null;}
			}
			"""
	);
	MethodDeclarationsCollector requestor = new MethodDeclarationsCollector();
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	searchAllMethodNames("foo", SearchPattern.R_PREFIX_MATCH, scope, requestor);
	assertSearchResults(
			"""
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.foo03(Object o,String s)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.foo02(Object o)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.foo01()
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooInt()""",
			requestor
	);
}
public void testBug478042_wScope_003() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java",
		"""
			package p478042;
			public class AllMethodDeclarations01 {
			  public void foo01() {}
			  public int foo02(Object o) {return null;}
			  public char foo03(Object o, String s) {return null;}
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java",
		"""
			package p478042;
			public class AllMethodDeclarations01b {
			  public Integer fooInt() {return null;}
			}
			"""
	);
	MethodDeclarationsCollector requestor = new MethodDeclarationsCollector();
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	searchAllMethodNames("AllMethod", SearchPattern.R_PREFIX_MATCH, "foo", SearchPattern.R_PREFIX_MATCH, scope, requestor);
	assertSearchResults(
			"""
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.foo03(Object o,String s)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.foo02(Object o)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.foo01()
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooInt()""",
			requestor
	);
}
public void testBug478042_wScope_004() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java",
		"""
			package p478042;
			public class AllMethodDeclarations01 {
			  public void foo01() {}
			  public int foo02(Object o) {return null;}
			  public char foo03(Object o, String s) {return null;}
			}
			"""
	);

	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java",
		"""
			package p478042;
			public class AllMethodDeclarations01b {
			  public Integer fooInt() {return null;}
			}
			"""
	);
	MethodDeclarationsCollector requestor = new MethodDeclarationsCollector();
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	searchAllMethodNames(
			"p478042", SearchPattern.R_EXACT_MATCH, //package
			null, SearchPattern.R_EXACT_MATCH,  // declaring Qualification
			"AllMethod", SearchPattern.R_PREFIX_MATCH, // declaring SimpleType
			"foo", SearchPattern.R_PREFIX_MATCH,
			scope, requestor);
	assertSearchResults(
			"""
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.foo03(Object o,String s)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.foo02(Object o)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.foo01()
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooInt()""",
			requestor
	);
}
public void testBug478042_wScope_005() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java",
		"""
			package p478042;
			public class AllMethodDeclarations01 {
			  public void foo01() {}
			  public int foo02(Object o) {return null;}
			  public char foo03(Object o, String s) {return null;}
			}
			"""
	);

	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java",
		"""
			package p478042;
			public class AllMethodDeclarations01b {
			  public Integer fooInt() {return null;}
			}
			"""
	);
	MethodNameMatchCollector collector = new MethodNameMatchCollector() {
		@Override
		public String toString() {
			return toFullyQualifiedNamesString();
		}
	};
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	searchAllMethodNames(
			"p478042", SearchPattern.R_EXACT_MATCH, //package
			null, SearchPattern.R_EXACT_MATCH,  // declaring Qualification
			"AllMethod", SearchPattern.R_PREFIX_MATCH, // declaring SimpleType
			"foo", SearchPattern.R_PREFIX_MATCH,
			scope, collector);
	assertSearchResults(
			"""
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.foo01()
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.foo02(Object o)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.foo03(Object o,String s)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooInt()""",
			collector
	);
}
public void testBug478042_wScope_006() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java",
		"""
			package p478042;
			public class AllMethodDeclarations01 {
			  public void fooCamelCase01() {}
			  public int fooCamelCase02(Object o) {return null;}
			  public char fooCamel03(Object o, String s) {return null;}
			}
			"""
	);

	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java",
		"""
			package p478042;
			public class AllMethodDeclarations01b {
			  public Integer fooCamelCaseInt() {return null;}
			}
			"""
	);
	MethodNameMatchCollector collector = new MethodNameMatchCollector() {
		@Override
		public String toString() {
			return toFullyQualifiedNamesString();
		}
	};
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	searchAllMethodNames(
			"p478042", SearchPattern.R_EXACT_MATCH, //package
			null, SearchPattern.R_EXACT_MATCH,  // declaring Qualification
			"AllMethod", SearchPattern.R_PREFIX_MATCH, // declaring SimpleType
			"fooCC", SearchPattern.R_CAMELCASE_MATCH,
			scope, collector);
	assertSearchResults(
			"""
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.fooCamelCase01()
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.fooCamelCase02(Object o)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooCamelCaseInt()""",
			collector
	);
}
public void testBug478042_wScope_007() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java",
		"""
			package p478042;
			class Y<T> {}
			class X<T> {}
			public class AllMethodDeclarations01 {
			  public Y<X> fooCamelCase01(Y<X> t) {}
			  public int fooCamelCase02(Object o) {return null;}
			  public char fooCamel03(Object o, String s) {return null;}
			}
			"""
	);

	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java",
		"""
			package p478042;
			public class AllMethodDeclarations01b {
			  public Integer fooCamelCaseInt() {return null;}
			}
			"""
	);
	MethodNameMatchCollector collector = new MethodNameMatchCollector() {
		@Override
		public String toString() {
			return toFullyQualifiedNamesString();
		}
	};
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	searchAllMethodNames(
			"p478042", SearchPattern.R_EXACT_MATCH, //package
			null, SearchPattern.R_EXACT_MATCH,  // declaring Qualification
			"AllMethod", SearchPattern.R_PREFIX_MATCH, // declaring SimpleType
			"fooCC", SearchPattern.R_CAMELCASE_MATCH,
			scope, collector);
	assertSearchResults(
			"""
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java Y p478042.AllMethodDeclarations01.fooCamelCase01(Y t)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.fooCamelCase02(Object o)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooCamelCaseInt()""",
			collector
	);
}
public void testBug478042_wScope_008() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java",
			"""
				package p478042;
				public class AllMethodDeclarations01 {
				public class Nested {
				public class Inner {
				  public void foo01() {}
				  public int foo02(Object o) {return 0;}
				  public char foo03(Object o, String s) {return '0';}
				}
				}
				}
				"""
	);

	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java",
		"""
			package p478042;
			public class AllMethodDeclarations01b {
			  public Integer fooInt() {return null;}
			}
			"""
	);
	MethodNameMatchCollector collector = new MethodNameMatchCollector() {
		@Override
		public String toString() {
			return toFullyQualifiedNamesString();
		}
	};
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	searchAllMethodNames(
			"p478042", SearchPattern.R_EXACT_MATCH, //package
			"AllMethod", SearchPattern.R_PREFIX_MATCH,  // declaring Qualification
			"Inn", SearchPattern.R_PREFIX_MATCH, // declaring SimpleType
			"foo", SearchPattern.R_PREFIX_MATCH,
			scope, collector);
	assertSearchResults(
			"""
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.Nested.Inner.foo01()
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.Nested.Inner.foo02(Object o)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.Nested.Inner.foo03(Object o,String s)""",
			collector
	);
}
public void testBug483303_001() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/com/test/C1.java",
			"""
				package com.test;
				public class C1 {
				  void m1(int i) {
				  }
				}
				"""
	);

	MethodNameMatchCollector collector = new MethodNameMatchCollector() {
		@Override
		public String toString() {
			return toFullyQualifiedNamesString();
		}
	};
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	searchAllMethodNames(
			null, SearchPattern.R_PREFIX_MATCH, //package
			null, SearchPattern.R_PREFIX_MATCH,  // declaring Qualification
			null, SearchPattern.R_PREFIX_MATCH, // declaring SimpleType
			"m1", SearchPattern.R_PREFIX_MATCH,
			scope, collector);
	assertSearchResults(
			"/JavaSearchBugs/src/com/test/C1.java void com.test.C1.m1(int i)",
			collector
	);
}
public void testBug483303_002() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/com/test/C1.java",
			"""
				package com.test;
				public class C1 {
				  void m1(int i) {
				  }
				}
				"""
	);

	class Collector extends MethodNameMatchRequestor {
		List<MethodNameMatch> matches = new ArrayList<>();
		@Override
		public void acceptMethodNameMatch(MethodNameMatch match) {
			this.matches.add(match);
		}
	}
	Collector collector = new Collector();
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	new SearchEngine(this.workingCopies).searchAllMethodNames(
			null, SearchPattern.R_PREFIX_MATCH, //package
			null, SearchPattern.R_PREFIX_MATCH,  // declaring Qualification
			null, SearchPattern.R_PREFIX_MATCH, // declaring SimpleType
			"m1".toCharArray(), SearchPattern.R_PREFIX_MATCH,
			scope, collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
	assertTrue(collector.matches.size() == 1);
	IMethod method = collector.matches.get(0).getMethod();
	String name = method.toString();
	String expectedName = "void m1(int) [in C1 [in [Working copy] C1.java [in com.test [in src [in JavaSearchBugs]]]]]";
	assertTrue("Unexpected Method Name", expectedName.equals(name));
	assertTrue("IJavaElement Does not exist", method.exists());
}

public void testBug483650_wScope_0001() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java",
		"""
			package p478042;
			public class AllMethodDeclarations01 {
			  public void foo01() {}
			  public int foo02(Object o) {return null;}
			  public char foo03(Object o, String s) {return null;}
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java",
		"""
			package p478042;
			public class AllMethodDeclarations01b {
			  public Integer fooInt() {return null;}
			}
			"""
	);
	MethodNameMatchCollector collector = new MethodNameMatchCollector() {
		@Override
		public String toString() {
			return toFullyQualifiedNamesString();
		}
	};
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	searchAllMethodNames("p478042*.AllMethodDeclarations0*", SearchPattern.R_PATTERN_MATCH, "foo", SearchPattern.R_PREFIX_MATCH, scope, collector);
	assertSearchResults(
			"""
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.foo01()
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.foo02(Object o)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.foo03(Object o,String s)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooInt()""",
			collector
	);
}
public void testBug483650_wScope_0002() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java",
		"""
			package p478042;
			public class AllMethodDeclarations01 {
			  public void foo01() {}
			  public int foo02(Object o) {return null;}
			  public char foo03(Object o, String s) {return null;}
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java",
		"""
			package p478042;
			public class AllMethodDeclarations01b {
			  public Integer fooInt() {return null;}
			}
			"""
	);
	MethodNameMatchCollector collector = new MethodNameMatchCollector() {
		@Override
		public String toString() {
			return toFullyQualifiedNamesString();
		}
	};
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	searchAllMethodNames("p478042*.AllMethodDeclarations0*", SearchPattern.R_PATTERN_MATCH, "foo", SearchPattern.R_PREFIX_MATCH, scope, collector);
	assertSearchResults(
			"""
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.foo01()
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.foo02(Object o)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.foo03(Object o,String s)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooInt()""",
			collector
	);
}
public void testBug483650_wScope_003() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java",
		"""
			package p478042;
			public class AllMethodDeclarations01 {
			  public void foo01() {}
			  public int foo02(Object o) {return null;}
			  public char foo03(Object o, String s) {return null;}
			}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java",
		"""
			package p478042;
			public class AllMethodDeclarations01b {
			  public Integer fooInt() {return null;}
			}
			"""
	);
	MethodNameMatchCollector collector = new MethodNameMatchCollector() {
		@Override
		public String toString() {
			return toFullyQualifiedNamesString();
		}
	};
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	searchAllMethodNames("*AllMethod*", SearchPattern.R_PATTERN_MATCH, "foo", SearchPattern.R_PREFIX_MATCH, scope, collector);
	assertSearchResults(
			"""
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.foo01()
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.foo02(Object o)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.foo03(Object o,String s)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooInt()""",
			collector
	);
}
public void testBug483650_wScope_004() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java",
		"""
			package p478042;
			public class AllMethodDeclarations01 {
			  public void foo01() {}
			  public int foo02(Object o) {return null;}
			  public char foo03(Object o, String s) {return null;}
			}
			"""
	);

	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java",
		"""
			package p478042;
			public class AllMethodDeclarations01b {
			  public Integer fooInt() {return null;}
			}
			"""
	);
	MethodNameMatchCollector collector = new MethodNameMatchCollector() {
		@Override
		public String toString() {
			return toFullyQualifiedNamesString();
		}
	};
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	searchAllMethodNames(
			"p478042.AllMethod*", SearchPattern.R_PATTERN_MATCH, //qualifier
			"foo", SearchPattern.R_PREFIX_MATCH,
			scope, collector);
	assertSearchResults(
			"""
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.foo01()
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.foo02(Object o)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.foo03(Object o,String s)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooInt()""",
			collector
	);
}
public void testBug483650_wScope_005() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java",
		"""
			package p478042;
			public class AllMethodDeclarations01 {
			  public void foo01() {}
			  public int foo02(Object o) {return null;}
			  public char foo03(Object o, String s) {return null;}
			}
			"""
	);

	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java",
		"""
			package p478042;
			public class AllMethodDeclarations01b {
			  public Integer fooInt() {return null;}
			}
			"""
	);
	MethodNameMatchCollector collector = new MethodNameMatchCollector() {
		@Override
		public String toString() {
			return toFullyQualifiedNamesString();
		}
	};
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	searchAllMethodNames(
			"p478042.AllMethod*", SearchPattern.R_PATTERN_MATCH,
			"foo", SearchPattern.R_PREFIX_MATCH,
			scope, collector);
	assertSearchResults(
			"""
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.foo01()
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.foo02(Object o)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.foo03(Object o,String s)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooInt()""",
			collector
	);
}
public void testBug483650_wScope_006() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java",
		"""
			package p478042;
			public class AllMethodDeclarations01 {
			  public void fooCamelCase01() {}
			  public int fooCamelCase02(Object o) {return null;}
			  public char fooCamel03(Object o, String s) {return null;}
			}
			"""
	);

	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java",
		"""
			package p478042;
			public class AllMethodDeclarations01b {
			  public Integer fooCamelCaseInt() {return null;}
			}
			"""
	);
	MethodNameMatchCollector collector = new MethodNameMatchCollector() {
		@Override
		public String toString() {
			return toFullyQualifiedNamesString();
		}
	};
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	searchAllMethodNames(
			"p478042.AllMethod*", SearchPattern.R_PATTERN_MATCH,
			"fooCC", SearchPattern.R_CAMELCASE_MATCH,
			scope, collector);
	assertSearchResults(
			"""
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.fooCamelCase01()
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.fooCamelCase02(Object o)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooCamelCaseInt()""",
			collector
	);
}
public void testBug483650_wScope_007() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java",
		"""
			package p478042;
			class Y<T> {}
			class X<T> {}
			public class AllMethodDeclarations01 {
			  public Y<X> fooCamelCase01(Y<X> t) {}
			  public int fooCamelCase02(Object o) {return null;}
			  public char fooCamel03(Object o, String s) {return null;}
			}
			"""
	);

	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java",
		"""
			package p478042;
			public class AllMethodDeclarations01b {
			  public Integer fooCamelCaseInt() {return null;}
			}
			"""
	);
	MethodNameMatchCollector collector = new MethodNameMatchCollector() {
		@Override
		public String toString() {
			return toFullyQualifiedNamesString();
		}
	};
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	searchAllMethodNames(
			"p478042.AllMethod*", SearchPattern.R_PATTERN_MATCH,
			"fooCC", SearchPattern.R_CAMELCASE_MATCH,
			scope, collector);
	assertSearchResults(
			"""
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java Y p478042.AllMethodDeclarations01.fooCamelCase01(Y t)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.fooCamelCase02(Object o)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java Integer p478042.AllMethodDeclarations01b.fooCamelCaseInt()""",
			collector
	);
}
public void testBug483650_wScope_008() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java",
			"""
				package p478042;
				public class AllMethodDeclarations01 {
				public class Nested {
				public class Inner {
				  public void foo01() {}
				  public int foo02(Object o) {return 0;}
				  public char foo03(Object o, String s) {return '0';}
				}
				}
				}
				"""
	);

	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/p478042/AllMethodDeclarations01b.java",
		"""
			package p478042;
			public class AllMethodDeclarations01b {
			  public Integer fooInt() {return null;}
			}
			"""
	);
	MethodNameMatchCollector collector = new MethodNameMatchCollector() {
		@Override
		public String toString() {
			return toFullyQualifiedNamesString();
		}
	};
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	searchAllMethodNames(
			"p478042.AllMethod*.Inn*", SearchPattern.R_PATTERN_MATCH,
			"foo", SearchPattern.R_PREFIX_MATCH,
			scope, collector);
	assertSearchResults(
			"""
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java void p478042.AllMethodDeclarations01.Nested.Inner.foo01()
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java int p478042.AllMethodDeclarations01.Nested.Inner.foo02(Object o)
				/JavaSearchBugs/src/p478042/AllMethodDeclarations01.java char p478042.AllMethodDeclarations01.Nested.Inner.foo03(Object o,String s)""",
			collector
	);
}
public void testBug483650_009() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/com/test/C1.java",
			"""
				package com.test;
				public class C1 {
				  void m1(int i) {
				  }
				}
				"""
	);

	MethodNameMatchCollector collector = new MethodNameMatchCollector() {
		@Override
		public String toString() {
			return toFullyQualifiedNamesString();
		}
	};
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	searchAllMethodNames(
			null, SearchPattern.R_PREFIX_MATCH,
			"m1", SearchPattern.R_PREFIX_MATCH,
			scope, collector);
	assertSearchResults(
			"/JavaSearchBugs/src/com/test/C1.java void com.test.C1.m1(int i)",
			collector
	);
}
public void testBug483650_010() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/com/test/C1.java",
			"""
				package com.test;
				public class C1 {
				  void m1(int i) {
				  }
				}
				"""
	);

	class Collector extends MethodNameMatchRequestor {
		List<MethodNameMatch> matches = new ArrayList<>();
		@Override
		public void acceptMethodNameMatch(MethodNameMatch match) {
			this.matches.add(match);
		}
	}
	Collector collector = new Collector();
	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(this.workingCopies);
	new SearchEngine(this.workingCopies).searchAllMethodNames(
			null, SearchPattern.R_PREFIX_MATCH,
			"m1".toCharArray(), SearchPattern.R_PREFIX_MATCH,
			scope, collector,
			IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
	assertTrue(collector.matches.size() == 1);
	IMethod method = collector.matches.get(0).getMethod();
	String name = method.toString();
	String expectedName = "void m1(int) [in C1 [in [Working copy] C1.java [in com.test [in src [in JavaSearchBugs]]]]]";
	assertTrue("Unexpected Method Name", expectedName.equals(name));
	assertTrue("IJavaElement Does not exist", method.exists());
}
public void testBug521240_001() throws CoreException {
	this.workingCopies = new ICompilationUnit[3];
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopies[0] = getWorkingCopy("/JavaSearchBugs/src/pack1/X.java",
		"""
			package pack1;
			public class X {
			    void foo(Y s) {}
			    void foo(pack2.Y s) {}
			}
			""",
		owner
	);
	this.workingCopies[1] = getWorkingCopy("/JavaSearchBugs/src/pack1/Y.java",
			"package pack1;\n" +
			"public class Y{}\n",
			owner
		);
	this.workingCopies[2] = getWorkingCopy("/JavaSearchBugs/src/pack2/Y.java",
		"package pack2;\n" +
		"public class Y{}\n",
		owner
	);
	SearchPattern pattern = SearchPattern.createPattern("pack1.X.foo(pack1.Y)",METHOD, DECLARATIONS,
			SearchPattern.R_ERASURE_MATCH | SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE);

	new SearchEngine(this.workingCopies).search(pattern,
			new SearchParticipant[] {SearchEngine.getDefaultSearchParticipant()},
			getJavaSearchWorkingCopiesScope(),
			this.resultCollector,
			null);
	assertSearchResults(
			"src/pack1/X.java void pack1.X.foo(Y) [foo] EXACT_MATCH"
	);
}
public void testBug547051_nonModular() throws Exception {
	try {
		IJavaProject project = createJavaProject("P");
		setUpProjectCompliance(project, "1.8", true);
		IType type = project.findType("java.util.Collection");
        IJavaSearchScope scope = SearchEngine.createStrictHierarchyScope(project, type, Boolean.TRUE, Boolean.TRUE, null);
        BasicSearchEngine engine = new BasicSearchEngine();
        char[] packageName = null;
        char[] typeName = null;
        AtomicBoolean r = new AtomicBoolean(false);
        engine.searchAllTypeNames(packageName, SearchPattern.R_PATTERN_MATCH,
        		typeName, SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CAMELCASE_MATCH,
        		TYPE, scope,
        		(modifiers, packageName1, simpleTypeName, enclosingTypeNames, path, access) -> r.set(true),
        		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, new NullProgressMonitor());

        assertTrue("Type search has no matches for subtypes of " + type, r.get());
	}
	finally {
		deleteProject("P");
	}
}

public void testBug547051_nonModular2() throws Exception {
	try {
		IJavaProject project = createJavaProject("P");
		setUpProjectCompliance(project, "1.8", true);
		IType type = project.findType("java.util.Collection");
        IJavaSearchScope scope = SearchEngine.createStrictHierarchyScope(project, type, Boolean.TRUE, Boolean.TRUE, null);
        BasicSearchEngine engine = new BasicSearchEngine();
        char[] packageName = null;
        char[] typeName = "HashSe".toCharArray();
        AtomicBoolean r = new AtomicBoolean(false);
        engine.searchAllTypeNames(packageName, SearchPattern.R_PATTERN_MATCH,
        		typeName, SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CAMELCASE_MATCH,
        		TYPE, scope,
        		(modifiers, packageName1, simpleTypeName, enclosingTypeNames, path, access) -> r.set(true),
        		IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, new NullProgressMonitor());

        assertTrue("Type search has no matches for subtypes of " + type, r.get());
	}
	finally {
		deleteProject("P");
	}
}
public void testBug547051_nonModular3() throws Exception {
	try {
		IJavaProject project = createJavaProject("P");
		setUpProjectCompliance(project, "1.8", true);
		IType type = project.findType("java.util.Collection");
		IJavaSearchScope scope = SearchEngine.createStrictHierarchyScope(project, type, Boolean.TRUE, Boolean.TRUE, null);
		BasicSearchEngine engine = new BasicSearchEngine();
		char[] packageName = "java.util".toCharArray();
		char[] typeName = "HashSet".toCharArray();
		AtomicBoolean r = new AtomicBoolean(false);
		engine.searchAllTypeNames(packageName, SearchPattern.R_PATTERN_MATCH,
				typeName, SearchPattern.R_PREFIX_MATCH | SearchPattern.R_CAMELCASE_MATCH,
				TYPE, scope,
				(modifiers, packageName1, simpleTypeName, enclosingTypeNames, path, access) -> r.set(true),
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, new NullProgressMonitor());
		assertTrue("Type search has no matches for subtypes of " + type, r.get());
	}
	finally {
		deleteProject("P");
	}
}


public void testBug547095_local_variables_search_non_modular() throws Exception {
	try {
		IJavaProject project = createJavaProject("P");
		setUpProjectCompliance(project, "1.8", true);
		IType type = project.findType("java.util.Collection");
		IMethod method = type.getMethod("equals", new String[] {"Ljava.lang.Object;" });
		LocalVariable lv = new LocalVariable(((JavaElement)method), "o", 0, 0, 0, 0, "QObject;", null, 0, true);
		SearchPattern pattern = SearchPattern.createPattern(lv, REFERENCES, SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, SYSTEM_LIBRARIES | APPLICATION_LIBRARIES | REFERENCED_PROJECTS | SOURCES);
		search(pattern, scope, this.resultCollector);
		// should not throw an error
	}
	finally {
		deleteProject("P");
	}
}

public void testBug547095_type_pattern_search_non_modular() throws Exception {
	try {
		IJavaProject project = createJavaProject("P");
		setUpProjectCompliance(project, "1.8", true);
		IType type = project.findType("java.util.Collection");
		TypeParameter tp = new TypeParameter(((JavaElement)type), "E");
		SearchPattern pattern = SearchPattern.createPattern(tp, REFERENCES, SearchPattern.R_EXACT_MATCH | SearchPattern.R_CASE_SENSITIVE | SearchPattern.R_ERASURE_MATCH);
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] { project }, SYSTEM_LIBRARIES | APPLICATION_LIBRARIES | REFERENCED_PROJECTS | SOURCES);
		search(pattern, scope, this.resultCollector);
		// should not throw an error
	}
	finally {
		deleteProject("P");
	}
}

public void testBug573486_showReferences_inMethodsAndFields_whenNoSource() throws CoreException, IOException {
	addLibraryEntry(JAVA_PROJECT, "/JavaSearchBugs/lib/search_lib_no_source.jar", false);
	try {
		IType type = getClassFile("JavaSearchBugs", "lib/search_lib_no_source.jar", "java.util", "Observable.class").getType();
		search(type, REFERENCES);
		assertSearchResults(
			"""
				lib/search_lib_no_source.jar java.util.List<java.util.Observable> search.ReferenceSubject.methodRef() [No source] POTENTIAL_MATCH
				lib/search_lib_no_source.jar void search.ReferenceSubject.methodRefParam1(java.util.Observable) [No source] POTENTIAL_MATCH
				lib/search_lib_no_source.jar void search.ReferenceSubject.methodRefParam2(java.util.Observable, java.util.Observable) [No source] POTENTIAL_MATCH
				lib/search_lib_no_source.jar T search.ReferenceSubject.methodRefTP() [No source] POTENTIAL_MATCH
				lib/search_lib_no_source.jar search.ReferenceSubject.fieldRef [No source] POTENTIAL_MATCH"""
		);
	}
	finally {
		removeClasspathEntry(JAVA_PROJECT, new Path("/JavaSearchBugs/lib/b211872_ws.jar"));
	}
}

/*
 * Test that using a classpath filter doesn't result in
 * not knowing a package is in the unnamed module.
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/485
 */
public void testClasspathFilterUnnamedModuleBugGh485() throws Exception {
	String testProject1Name = "gh485ClasspathFilterUnnamedModuleBugProject1";
	String testProject2Name = "gh485ClasspathFilterUnnamedModuleBugProject2";
	try {
		JavaProject project1 = (JavaProject) setUpJavaProject(testProject1Name, "11", false);
		String packageFolder1 = "/" + testProject1Name + "/src/t/t/t1/";
		createFolder(packageFolder1);
		String snippet1 = """
			package t.t.t1;
			import com.g.f.t.f.FWC;
			public class F {
			  public static final FWC EMPTY = null;
			}""";
		createFile(packageFolder1 + "/F.java", snippet1);
		setUpJavaProject(testProject2Name, "11", false);
		String packageFolder2 =  "/" + testProject2Name + "/src/com/g/f/t/f";
		createFolder(packageFolder2);
		String snippet2 = """
			package com.g.f.t.f;
			import com.g.f.t.f.FWC;
			public class FWC {
			  public static void main(String[] args) { }
			}""";
		createFile(packageFolder2 + "/FWC.java", snippet2);
		waitForAutoBuild();
		waitUntilIndexesReady();

		IType type = project1.findType("t.t.t1.F");
		ICompilationUnit[] cus = { type.getCompilationUnit() };

		boolean excludeTests = false;
		SearchableEnvironment search = new SearchableEnvironment(project1, cus, excludeTests);

		char[][] packageName = { new char[] { 'c', 'o', 'm' } };
		char[][] modules = search.getModulesDeclaringPackage(packageName, ModuleBinding.UNNAMED);
		assertNotNull("Expected to find module for package: " + toString(packageName), modules);
		assertEquals("Expected unnamed module for package: " + toString(packageName), "", toString(modules));
	} finally {
		JavaCore.setOptions(getDefaultJavaCoreOptions());
		deleteProject(testProject1Name);
		deleteProject(testProject2Name);
	}
}

/*
 * After fixing an compile error about a method reference to a not-existing method,
 * no call hierarchy was found for the then-existing method.
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/438
 */
public void testMethodReferenceAfterCompileErrorBugGh438() throws Exception {
	String testProjectName = "gh438MethodReferenceAfterCompileErrorProject";
	try {
		IJavaProject project = createJava11Project(testProjectName, new String[] {"src"});
		setUpProjectCompliance(project, "11", true);
		String packageFolder = "/" + testProjectName + "/src/test";
		createFolder(packageFolder);
		String testSource = """
			package test;
			public class Test {
			  public void testMethod() {
			    MyClass myClass = new MyClass();
			    java.util.stream.Stream.of("hello").forEach(myClass::doSomething);
			  }
			}""";
		createFile(packageFolder + "/Test.java", testSource);
		buildAndExpectProblems(project, "MyClass cannot be resolved to a type\nMyClass cannot be resolved to a type");

		String myClassSource = """
			package test;
			public class MyClass {
			  void doSomething(String s) {
			    System.out.println(s);
			  }
			}""";
		createFile(packageFolder + "/MyClass.java", myClassSource);
		buildAndExpectNoProblems(project);

		IType type = project.findType("test.MyClass");
		IMethod method = type.getMethod("doSomething", new String[] { "QString;" });
		search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
		assertSearchResults(
				"src/test/Test.java void test.Test.testMethod() [doSomething] EXACT_MATCH");
	} finally {
		deleteProject(testProjectName);
	}
}

/*
 * Test that having a module conflict in libraries that are on the compile classpath
 * (and not the compile module path) doesn't affect searching for types in those projects.
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/675
 */
public void testModuleConflictForClasspathProjectsBugGh675() throws Exception {
	String projectName = "gh675ModuleConflictForClasspathProjectsBugProject";
	try {
		IJavaProject project = createJavaProject(projectName, new String[] {"src"}, new String[] {"JCL11_LIB"}, "bin", "11");
		String packageFolder = "/" + projectName + "/src/test/";
		createFolder(packageFolder);
		String snippet =
				"""
			package test;
			import testpackage.TestClass;
			public class Test {
			  public TestClass testField = null;
			  public void testMethod() {
			      testField = null;
			  }
			}""";
		createFile(packageFolder + "/Test.java", snippet);

		String ambiguousTypeDefinition =
				"""
			package testpackage;
			public class TestClass {
			}""";

		addLibrary(project,
				"libGh675_1.jar",
				"libGh675_1.src.zip",
				new String[] {
						"testpackage/TestClass.java",
						ambiguousTypeDefinition },
				JavaCore.VERSION_11);

		addLibrary(project,
				"libGh675_2.jar",
				"libGh675_2.src.zip",
				new String[]  {
						"module-info.java",
						"""
							module testmodule {
							  exports testpackage;
							}""",
						"testpackage/TestClass.java",
						ambiguousTypeDefinition },
				JavaCore.VERSION_11);

		buildAndExpectNoProblems(project);
		waitUntilIndexesReady();

		IType type = project.findType("test.Test");

		IField field = type.getField("testField");
		search(field, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
		assertSearchResults(
				"src/test/Test.java void test.Test.testMethod() [testField] EXACT_MATCH");
	} finally {
		deleteProject(projectName);
	}
}

/**
 * A modular project refers to a modular library defining a type {@code TestClass},
 * this project also references a non-modular project on its classpath.
 * The non-modular project uses a non-modular library that defines the same type {@code TestClass}.
 * Since neither project exports the library, there is no compile error and no module conflict for the type.
 * This test ensures the search doesn't run into a module conflict due to not considering whether the libraries are exported.
 *
 * Originally the problem was observed by using a Java 8 project and a Java 11 project,
 * with {@code java.util.Locale} as the type that causes the search to run into a module conflict.
 *
 * https://github.com/eclipse-jdt/eclipse.jdt.core/issues/723
 */
public void testModuleConflictGh723() throws Exception {
	String projectName = "gh723Project";
	String modularProjectName = "gh723ProjectModular";
	try {
		IJavaProject project = createJavaProject(projectName, new String[] {"src"}, new String[] {"JCL11_LIB"}, "bin", "11");
		IJavaProject modularProject = createJavaProject(modularProjectName, new String[] {"src"}, new String[] {"JCL11_LIB"}, "bin", "11");

		createFolder("/" + projectName + "/src/test/");
		createFile("/" + projectName + "/src/test/Test.java",
				"""
					package test;
					public class Test {
					  public static void test(testpackage.TestClass t) {
					  }
					}""");

		createFolder("/" + modularProjectName + "/src/testmodular/");
		createFile("/" + modularProjectName + "/src/testmodular/TestModular.java",
				"""
					package testmodular;
					public class TestModular {
					  public void testModular() {
					      test.Test.test(null);
					  }
					}""");

		String ambiguousTypeDefinition =
				"""
			package testpackage;
			public class TestClass {
			}""";

		addLibrary(project,
				"libGh723.jar",
				"libGh723.src.zip",
				new String[] {
						"testpackage/TestClass.java",
						ambiguousTypeDefinition },
				JavaCore.VERSION_1_8,
				false);

		addModularLibrary(modularProject,
				"libGh723_modular.jar",
				"libGh723_modular.src.zip",
				new String[] {
						"module-info.java",
						"""
							module testmodule {
							  exports testpackage;
							}""",
						"testpackage/TestClass.java",
						ambiguousTypeDefinition },
				JavaCore.VERSION_11);

		addClasspathEntry(modularProject, JavaCore.newProjectEntry(project.getPath()));
		buildAndExpectNoProblems(project, modularProject);

		IType type = project.findType("test.Test");
		IMethod method = type.getMethod("test", new String [] {"Qtestpackage.TestClass;"});
		search(method, REFERENCES, EXACT_RULE, SearchEngine.createWorkspaceScope(), this.resultCollector);
		assertSearchResults(
				"src/testmodular/TestModular.java void testmodular.TestModular.testModular() [test(null)] EXACT_MATCH");
	} finally {
		deleteProject(projectName);
		deleteProject(modularProjectName);
	}
}

private static String toString(char[][] modules) {
	StringBuilder sb = new StringBuilder();
	for (char[] m : modules) {
		sb.append(m);
	}
	String s = sb.toString();
	return s;
}
}