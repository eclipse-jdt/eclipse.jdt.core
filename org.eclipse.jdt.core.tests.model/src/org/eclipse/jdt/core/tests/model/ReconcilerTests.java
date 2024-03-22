/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * Contributors:
 *		IBM Corporation - initial API and implementation
 *		Stephan Herrmann - Contribution for
 *								bug 401035 - [1.8] A few tests have started failing recently
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.CompilationUnit;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.JavaModelCache;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.osgi.framework.Bundle;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ReconcilerTests extends ModifyingResourceTests {

	protected ICompilationUnit workingCopy;
	protected ProblemRequestor problemRequestor;

	/* A problem requestor that auto-cancels on first problem */
	static class CancelingProblemRequestor extends ProblemRequestor {
		IProgressMonitor progressMonitor = new IProgressMonitor() {
			boolean isCanceled = false;
			public void beginTask(String name, int totalWork) {}
			public void done() {}
			public void internalWorked(double work) {}
			public boolean isCanceled() {
				return this.isCanceled;
			}
			public void setCanceled(boolean value) {
				this.isCanceled = value;
			}
			public void setTaskName(String name) {}
			public void subTask(String name) {}
			public void worked(int work) {}
		};

		boolean isCanceling = false;
		@Override
		public void acceptProblem(IProblem problem) {
			if (this.isCanceling) this.progressMonitor.setCanceled(true); // auto-cancel on first problem
			super.acceptProblem(problem);
		}
	}

	/*package*/ static final int JLS_LATEST = AST_INTERNAL_LATEST;

	static class ReconcileParticipant extends CompilationParticipant {
		IJavaElementDelta delta;
		org.eclipse.jdt.core.dom.CompilationUnit ast;
		ReconcileParticipant() {
			TestCompilationParticipant.PARTICIPANT = this;
		}
		public boolean isActive(IJavaProject project) {
			return true;
		}
		/**
		 * @deprecated
		 */
		public void reconcile(ReconcileContext context) {
			this.delta = context.getDelta();
			try {
				this.ast = context.getAST(JLS_LATEST);
			} catch (JavaModelException e) {
				assertNull("Unexpected exception", e);
			}
		}
	}

	static class ReconcileParticipant2 extends CompilationParticipant {
		IJavaElementDelta delta;
		org.eclipse.jdt.core.dom.CompilationUnit ast;
		ReconcileParticipant2() {
			TestCompilationParticipant.PARTICIPANT = this;
		}
		public boolean isActive(IJavaProject project) {
			return true;
		}
		/**
		 * @deprecated
		 */
		public void reconcile(ReconcileContext context) {
			this.delta = context.getDelta();
			try {
				this.ast = context.getAST(JLS_LATEST);
				assertTrue("Context should have statement recovery enabled", (context.getReconcileFlags() & ICompilationUnit.ENABLE_STATEMENTS_RECOVERY) != 0);
				assertTrue("Context should have ignore method body enabled", (context.getReconcileFlags() & ICompilationUnit.IGNORE_METHOD_BODIES) != 0);
			} catch (JavaModelException e) {
				assertNull("Unexpected exception", e);
			}
		}
	}

	static class ReconcileParticipant3 extends CompilationParticipant {
		IJavaElementDelta delta;
		org.eclipse.jdt.core.dom.CompilationUnit ast;
		ReconcileParticipant3() {
			TestCompilationParticipant.PARTICIPANT = this;
		}
		public boolean isActive(IJavaProject project) {
			return true;
		}
		/**
		 * @deprecated
		 */
		public void reconcile(ReconcileContext context) {
			this.delta = context.getDelta();
			try {
				this.ast = context.getAST(JLS_LATEST);
				assertFalse("Context should have statement recovery enabled", (context.getReconcileFlags() & ICompilationUnit.ENABLE_STATEMENTS_RECOVERY) != 0);
				assertTrue("Context should have ignore method body enabled", (context.getReconcileFlags() & ICompilationUnit.IGNORE_METHOD_BODIES) != 0);
			} catch (JavaModelException e) {
				assertNull("Unexpected exception", e);
			}
		}
	}
public ReconcilerTests(String name) {
	super(name);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
//	JavaModelManager.VERBOSE = true;
//	org.eclipse.jdt.internal.core.search.BasicSearchEngine.VERBOSE = true;
//	TESTS_PREFIX = "testIgnoreIfBetterNonAccessibleRule";
//	TESTS_NAMES = new String[] { "testBug374176" };
//	TESTS_NUMBERS = new int[] { 118823 };
//	TESTS_RANGE = new int[] { 16, -1 };
}
public static Test suite() {
	return buildModelTestSuite(ReconcilerTests.class);
}
protected void assertProblems(String message, String expected) {
	assertProblems(message, expected, this.problemRequestor);
}
protected void assertProblemsInclude(String message, String expected) {
	String actual = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(this.problemRequestor.problems.toString());
	String independantExpectedString = org.eclipse.jdt.core.tests.util.Util.convertToIndependantLineDelimiter(expected);
	if (actual.indexOf(independantExpectedString) == -1){
	 	System.out.println(org.eclipse.jdt.core.tests.util.Util.displayString(actual, this.tabs));
		assertEquals(
			message,
			independantExpectedString,
			actual);
	}
}
// Expect no error as soon as indexing is finished
protected void assertNoProblem(char[] source, ICompilationUnit unit) throws InterruptedException, JavaModelException {
	IndexManager indexManager = JavaModelManager.getIndexManager();
	if (this.problemRequestor.problemCount > 0) {
		// If errors then wait for indexes to finish
		while (indexManager.awaitingJobsCount() > 0) {
			waitUntilIndexesReady();
		}
		// Reconcile again to see if error goes away
		this.problemRequestor.initialize(source);
		unit.getBuffer().setContents(source); // need to set contents again to be sure that following reconcile will be really done
		unit.reconcile(JLS_LATEST,
			true, // force problem detection to see errors if any
			null,	// do not use working copy owner to not use working copies in name lookup
			null);
		if (this.problemRequestor.problemCount > 0) {
			assertEquals("Working copy should NOT have any problem!", "", this.problemRequestor.problems.toString());
		}
	}
}
protected void addClasspathEntries(IClasspathEntry[] entries, boolean enableForbiddenReferences) throws JavaModelException {
	IJavaProject project = getJavaProject("Reconciler");
	IClasspathEntry[] oldClasspath = project.getRawClasspath();
	int oldLength = oldClasspath.length;
	int length = entries.length;
	IClasspathEntry[] newClasspath = new IClasspathEntry[oldLength+length];
	System.arraycopy(oldClasspath, 0, newClasspath, 0, oldLength);
	System.arraycopy(entries, 0, newClasspath, oldLength, length);
	project.setRawClasspath(newClasspath, null);

	if (enableForbiddenReferences) {
		project.setOption(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);
	}
}
protected void removeClasspathEntries(IClasspathEntry[] entries) throws JavaModelException {
	IJavaProject project = getJavaProject("Reconciler");
	IClasspathEntry[] oldClasspath = project.getRawClasspath();
	int oldLength = oldClasspath.length;
	int length = entries.length;
	IClasspathEntry[] newClasspath = new IClasspathEntry[oldLength-length];
	System.arraycopy(oldClasspath, 0, newClasspath, 0, oldLength-length);
	project.setRawClasspath(newClasspath, null);
}
/**
 * Setup for the next test.
 */
@Override
public void setUp() throws Exception {
	super.setUp();
	this.problemRequestor =  new ProblemRequestor();
	this.wcOwner = new WorkingCopyOwner() {
		public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
			return ReconcilerTests.this.problemRequestor;
		}
	};
	this.workingCopy = getCompilationUnit("Reconciler/src/p1/X.java").getWorkingCopy(this.wcOwner, null);
	this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
	startDeltas();
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();

	// Create project with 1.4 compliance
	IJavaProject project14 = createJavaProject("Reconciler", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
	createFolder("/Reconciler/src/p1");
	createFolder("/Reconciler/src/p2");
	createFile(
		"/Reconciler/src/p1/X.java",
		"""
			package p1;
			import p2.*;
			public class X {
			  public void foo() {
			  }
			}"""
	);
	project14.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
	project14.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.IGNORE);
	project14.setOption(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.WARNING);

	// Create project with 1.5 compliance
	IJavaProject project15 = createJavaProject("Reconciler15", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
	addLibrary(
		project15,
		"lib15.jar",
		"lib15src.zip",
		new String[] {
			"java/util/List.java",
			"""
				package java.util;
				public class List<T> {
				}""",
			"java/util/Stack.java",
			"""
				package java.util;
				public class Stack<T> {
				}""",
			"java/util/Map.java",
			"""
				package java.util;
				public interface Map<K,V> {
				}""",
			"java/lang/annotation/Annotation.java",
			"""
				package java.lang.annotation;
				public interface Annotation {
				}""",
			"java/lang/Deprecated.java",
			"""
				package java.lang;
				public @interface Deprecated {
				}""",
			"java/lang/SuppressWarnings.java",
			"""
				package java.lang;
				public @interface SuppressWarnings {
				   String[] value();
				}"""
		},
		JavaCore.VERSION_1_5
	);
	project15.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.IGNORE);
	project15.setOption(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.IGNORE);
}
private void setUp15WorkingCopy() throws JavaModelException {
	setUp15WorkingCopy("Reconciler15/src/p1/X.java", this.wcOwner);
}
private void setUp15WorkingCopy(String path, WorkingCopyOwner owner) throws JavaModelException {
	String contents = this.workingCopy.getSource();
	setUpWorkingCopy(path, contents, owner);
}
protected void setUpWorkingCopy(String path, String contents) throws JavaModelException {
	setUpWorkingCopy(path, contents, this.wcOwner);
}
private void setUpWorkingCopy(String path, String contents, WorkingCopyOwner owner) throws JavaModelException {
	this.workingCopy.discardWorkingCopy();
	this.workingCopy = getCompilationUnit(path).getWorkingCopy(owner, null);
	assertEquals("Invalid problem requestor!", this.problemRequestor, this.wcOwner.getProblemRequestor(this.workingCopy));
	setWorkingCopyContents(contents);
	this.workingCopy.makeConsistent(null);
}
void setWorkingCopyContents(String contents) throws JavaModelException {
	this.workingCopy.getBuffer().setContents(contents);
	this.problemRequestor.initialize(contents.toCharArray());
}
/**
 * Cleanup after the previous test.
 */
@Override
public void tearDown() throws Exception {
	TestCompilationParticipant.PARTICIPANT = null;
	if (this.workingCopy != null) {
		this.workingCopy.discardWorkingCopy();
	}
	stopDeltas();
	super.tearDown();
}
@Override
public void tearDownSuite() throws Exception {
	deleteProject("Reconciler");
	deleteProject("Reconciler15");
	super.tearDownSuite();
}
/*
 * Ensures that no problem is created for a reference to a type that is included in a prereq project.
 */
public void testAccessRestriction() throws CoreException {
	try {
		createJavaProject("P1", new String[] {"src"}, new String[] {"JCL_LIB"}, null, null, new String[0], null, null, new boolean[0], "bin", null, new String[][] {{"**/X.java"}}, null, "1.4");
		createFolder("/P1/src/p");
		createFile("/P1/src/p/X.java", "package p; public class X {}");

		createJavaProject("P2", new String[] {"src"}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "bin");
		setUpWorkingCopy("/P2/src/Y.java", "public class Y extends p.X {}");
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Ensures that no problem is created for a reference to a binary type that is included in a prereq project.
 * (regression test for bug 82542 Internal error during AST creation)
 */
public void testAccessRestriction2() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P1");
		addLibrary(
			project,
			"lib.jar",
			"libsrc.zip",
			new String[] {
				"p/X.java",
				"""
					package p;
					public class X {
					}""",
			},
			null/*no non-Java resources*/,
			new String[] {
				"**/*"
			},
			null,
			"1.4",
			null
		);
		createJavaProject("P2", new String[] {"src"}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "bin");
		setUpWorkingCopy("/P2/src/Y.java", "public class Y extends p.X {}");
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/*
 * Ensures that no problem is created for a reference to a type that is included and not exported in a prereq project
 * but with combineAccessRestriction flag set to false.
 */
public void testAccessRestriction3() throws CoreException {
	try {
		createJavaProject("P1");
		createFolder("/P1/p");
		createFile("/P1/p/X.java", "package p; public class X {}");

		createJavaProject("P2", new String[] {}, new String[] {}, null, null, new String[] {"/P1"}, null, null, new boolean[] {true}, "", null, null, null, "1.4");

		createJavaProject("P3", new String[] {"src"}, new String[] {"JCL_LIB"}, null, null, new String[] {"/P2"}, null, new String[][] {new String[] {"**/X"}}, false/*don't combine access restrictions*/, new boolean[] {true}, "bin", null, null, null, "1.4", false/*don't import*/);
		setUpWorkingCopy("/P3/src/Y.java", "public class Y extends p.X {}");
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		deleteProjects(new String[] {"P1", "P2", "P3" });
	}
}
/*
 * Ensures that a problem is created for a reference to a type that is included and not exported in a prereq project
 * but with combineAccessRestriction flag set to true.
 */
public void testAccessRestriction4() throws CoreException {
	try {
		createJavaProject("P1");
		createFolder("/P1/p");
		createFile("/P1/p/X.java", "package p; public class X {}");

		createJavaProject("P2", new String[] {}, new String[] {}, null, null, new String[] {"/P1"}, null, null, new boolean[] {true}, "", null, null, null, "1.4");

		createJavaProject("P3", new String[] {"src"}, new String[] {"JCL_LIB"}, null, null, new String[] {"/P2"}, null, new String[][] {new String[] {"**/X"}}, true/*combine access restrictions*/, new boolean[] {true}, "bin", null, null, null, "1.4", false/*don't import*/);
		setUpWorkingCopy("/P3/src/Y.java", "public class Y extends p.X {}");
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. ERROR in /P3/src/Y.java (at line 1)
					public class Y extends p.X {}
					                       ^^^
				Access restriction: The type \'X\' is not API (restriction on required project \'P1\')
				----------
				"""
		);
	} finally {
		deleteProjects(new String[] {"P1", "P2", "P3" });
	}
}
/*
 * Ensures that a problem is created for a reference to a type that is no longer accessible in a prereq project.
 * (regression test for bug 91498 Reconcile still sees old access rules)
 */
public void testAccessRestriction5() throws CoreException {
	try {
		createJavaProject("P1");
		createFolder("/P1/p");
		createFile("/P1/p/X.java", "package p; public class X {}");
		IJavaProject p2 = createJavaProject("P2", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
		IClasspathEntry[] classpath = p2.getRawClasspath();
		int length = classpath.length;
		System.arraycopy(classpath, 0, classpath = new IClasspathEntry[length+1], 0, length);
		classpath[length] = createSourceEntry("P2", "/P1", "+**/p/|-**/*");
		p2.setRawClasspath(classpath, null);
		setUpWorkingCopy("/P2/src/Y.java", "public class Y extends p.X {}");
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);

		// remove accessible rule
		System.arraycopy(classpath, 0, classpath = new IClasspathEntry[length+1], 0, length);
		classpath[length] = createSourceEntry("P2", "/P1", "-**/*");
		p2.setRawClasspath(classpath, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true/*force problem detection*/, null, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. ERROR in /P2/src/Y.java (at line 1)
					public class Y extends p.X {}
					                       ^^^
				Access restriction: The type \'X\' is not API (restriction on required project \'P1\')
				----------
				"""
		);

	} finally {
		deleteProjects(new String[] {"P1", "P2"});
	}
}
/**
 * Ensures that the reconciler handles duplicate members correctly.
 */
public void testAddDuplicateMember() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public void foo() {
			  }
			  public void foo() {
			  }
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()#2[+]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a field and a constructor.
 */
public void testAddFieldAndConstructor() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  int i;
			  X(int i) {
			    this.i = i;
			  }
			  public void foo() {
			  }
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"""
			X[*]: {CHILDREN | FINE GRAINED}
				X(int)[+]: {}
				i[+]: {}"""
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a field and a constructor.
 */
public void testAddImports() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			import java.lang.reflect.*;
			import java.util.Vector;
			public class X {
			  public void foo() {
			  }
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"""
			<import container>[*]: {CHILDREN | FINE GRAINED}
				import java.lang.reflect.*[+]: {}
				import java.util.Vector[+]: {}"""
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a method.
 */
public void testAddMethod1() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public void foo() {
			  }
			  public void bar() {
			  }
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	bar()[+]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a portion of a new method.
 */
public void testAddPartialMethod1() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public void some()
			  public void foo() {
			  }
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	some()[+]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a portion of a new method.  Ensures that when a
 * second part is added to the new method no structural changes are recognized.
 */
public void testAddPartialMethod1and2() throws JavaModelException {
	// Add partial method before foo
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public void some()
			  public void foo() {
			  }
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

	// Add { on partial method
	clearDeltas();
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public void some() {
			  public void foo() {
			  }
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"[Working copy] X.java[*]: {CONTENT | FINE GRAINED}"
	);
}
/*
 * Ensures that the delta is correct when adding an annotation
 */
public void testAnnotations1() throws JavaModelException {
	setUp15WorkingCopy();
	clearDeltas();
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  @MyAnnot
			  public void foo() {
			  }
			}"""
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"""
			X[*]: {CHILDREN | FINE GRAINED}
				foo()[*]: {ANNOTATIONS}
					@MyAnnot[+]: {}"""
	);
}
/*
 * Ensures that the delta is correct when removing an annotation
 */
public void testAnnotations2() throws JavaModelException {
	setUp15WorkingCopy();
	clearDeltas();
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  @MyAnnot
			  public void foo() {
			  }
			}"""
	);
	this.workingCopy.makeConsistent(null);

	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public void foo() {
			  }
			}"""
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"""
			X[*]: {CHILDREN | FINE GRAINED}
				foo()[*]: {ANNOTATIONS}
					@MyAnnot[-]: {}"""
	);
}
/*
 * Ensures that the delta is correct when changing an annotation
 */
public void testAnnotations3() throws JavaModelException {
	setUp15WorkingCopy();
	clearDeltas();
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  @MyAnnot(x=1)
			  public void foo() {
			  }
			}"""
	);
	this.workingCopy.makeConsistent(null);

	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  @MyAnnot(y=1)
			  public void foo() {
			  }
			}"""
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"""
			X[*]: {CHILDREN | FINE GRAINED}
				foo()[*]: {ANNOTATIONS}
					@MyAnnot[*]: {CONTENT}"""
	);
}
/*
 * Ensures that the delta is correct when changing an annotation
 */
public void testAnnotations4() throws JavaModelException {
	setUp15WorkingCopy();
	clearDeltas();
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  @MyAnnot(x=1)
			  public void foo() {
			  }
			}"""
	);
	this.workingCopy.makeConsistent(null);

	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  @MyAnnot(x=2)
			  public void foo() {
			  }
			}"""
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"""
			X[*]: {CHILDREN | FINE GRAINED}
				foo()[*]: {ANNOTATIONS}
					@MyAnnot[*]: {CONTENT}"""
	);
}
/*
 * Ensures that no error is reported if an annotation type's cu starts with a slash
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=214450 )
 */
public void testAnnotations5() throws JavaModelException {
	ICompilationUnit annotation = null;
	try {
		setUp15WorkingCopy();
		annotation = getWorkingCopy(
			"Reconciler15/src/p1/MyAnnot2.java",
			"""
				/* test */
				package p1;
				public @interface MyAnnot2 {
				  String bar();
				}""",
			this.wcOwner);
		setWorkingCopyContents(
			"""
				package p1;
				public class X {
				  @MyAnnot2(bar="a")
				  public void foo() {
				  }
				}"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		if (annotation != null)
			annotation.discardWorkingCopy();
	}
}
/*
 * Ensures that the AST broadcasted during a reconcile operation is correct.
 * (case of a working copy being reconciled with changes, creating AST and no problem detection)
 */
public void testBroadcastAST1() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			}""");
	this.workingCopy.reconcile(JLS_LATEST, false/*don't force problem detection*/, null/*primary owner*/, null/*no progress*/);
	assertASTNodeEquals(
		"Unexpected ast",
		"""
			package p1;
			import p2.*;
			public class X {
			}
			""",
		this.deltaListener.getCompilationUnitAST(this.workingCopy));
}
/*
 * Ensures that the AST broadcasted during a reconcile operation is correct.
 * (case of a working copy being reconciled with NO changes, creating AST and forcing problem detection)
 */
public void testBroadcastAST2() throws JavaModelException {
	this.workingCopy.reconcile(JLS_LATEST, true/*force problem detection*/, null/*primary owner*/, null/*no progress*/);
	assertASTNodeEquals(
		"Unexpected ast",
		"""
			package p1;
			import p2.*;
			public class X {
			  public void foo(){
			  }
			}
			""",
		this.deltaListener.getCompilationUnitAST(this.workingCopy));
}
/*
 * Ensures that no AST is broadcasted during a reconcile operation if the working copy being reconciled
 * has NO changes and NO problem detection is requested)
 */
public void testBroadcastAST3() throws JavaModelException {
	this.workingCopy.reconcile(JLS_LATEST, false/*don't force problem detection*/, null/*primary owner*/, null/*no progress*/);
	assertASTNodeEquals(
		"Unexpected ast",
		"null",
		this.deltaListener.getCompilationUnitAST(this.workingCopy));
}
/*
 * Ensures that the AST broadcasted during a reconcile operation is correct.
 * (case of a working copy being reconciled twice in a batch operation)
 */
public void testBroadcastAST4() throws CoreException {
	JavaCore.run(
		new IWorkspaceRunnable() {
			public void run(IProgressMonitor monitor) throws CoreException {
				ReconcilerTests.this.workingCopy.reconcile(JLS_LATEST, true/*force problem detection*/, null/*primary owner*/, monitor);
				setWorkingCopyContents(
					"""
						package p1;
						import p2.*;
						public class X {
						}""");
				ReconcilerTests.this.workingCopy.reconcile(JLS_LATEST, false/*don't force problem detection*/, null/*primary owner*/, monitor);
			}
		},
		null/*no progress*/);
	assertASTNodeEquals(
		"Unexpected ast",
		"""
			package p1;
			import p2.*;
			public class X {
			}
			""",
		this.deltaListener.getCompilationUnitAST(this.workingCopy));
}
/*
 * Ensures that the AST broadcasted doesn't have a type root that is caching its contents
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=222213 )
 */
public void testBroadcastAST5() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			}""");
	this.workingCopy.reconcile(JLS_LATEST, false/*don't force problem detection*/, null/*primary owner*/, null/*no progress*/);
	org.eclipse.jdt.core.dom.CompilationUnit compilationUnit = this.deltaListener.getCompilationUnitAST(this.workingCopy);
	String newContents =
		"""
		package p1;
		public class X {
		}""";
	setWorkingCopyContents(newContents);
	org.eclipse.jdt.internal.compiler.env.ICompilationUnit compilerCU = (org.eclipse.jdt.internal.compiler.env.ICompilationUnit) compilationUnit.getTypeRoot();
	assertSourceEquals("Unexpected contents", newContents, new String(compilerCU.getContents()));
}
/*
 * Ensures that reconciling a subclass doesn't close the buffer while resolving its superclass.
 * (regression test for bug 62854 refactoring does not trigger reconcile)
 */
public void testBufferOpenAfterReconcile() throws CoreException {
 	try {
		createFile(
			"/Reconciler/src/p1/Super.java",
			"""
				package p1;
				public class Super {
				}"""
		);
		setWorkingCopyContents(
			"""
				package p1;
				import p2.*;
				public class X extends Super {
				  public void foo() {
				  }
				}""");
		IBuffer buffer = this.workingCopy.getBuffer();
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true, null, null);
		assertTrue("Buffer should still be open", !buffer.isClosed());
	} finally {
		deleteFile("/Reconciler/src/p1/Super.java");
	}
}
/*
 * Ensures that reconciling with a closed buffer reports an error
 * (regression test for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=138882 )
 */
public void testBufferClosed1() throws CoreException {
	this.wcOwner = new WorkingCopyOwner() {
		public IBuffer createBuffer(ICompilationUnit copy) {
			return new TestBuffer(copy);
		}
		public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
			return ReconcilerTests.this.problemRequestor;
		}
	};
	setUpWorkingCopy(
		"Reconciler/src/p1/X.java",
		"""
			package p1;
			public class X {
			  void foo(String s) {
			  }
			}"""
	);

	// simulate buffer being closed
	((TestBuffer) this.workingCopy.getBuffer()).contents = null;

	this.workingCopy.reconcile(ICompilationUnit.NO_AST, true, null, null);
	assertProblemsInclude(
		"Unexpected problems",
		"""
			----------
			1. ERROR in /Reconciler/src/p1/X.java (at line 1)
				package p1;
				^
			Cannot read the source from /Reconciler/src/p1/X.java due to internal exception java.io.IOException:Buffer is closed
			----------
			"""
	);
}
/*
 * Ensures that reconciling with a closed buffer reports an error
 * (regression test for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=138882 )
 */
public void testBufferClosed2() throws CoreException {
	this.wcOwner = new WorkingCopyOwner() {
		public IBuffer createBuffer(ICompilationUnit copy) {
			return new TestBuffer(copy);
		}
		public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
			return ReconcilerTests.this.problemRequestor;
		}
	};
	setUpWorkingCopy(
		"Reconciler/src/p1/X.java",
		"""
			package p1;
			public class X {
			  void foo(String s) {
			  }
			}"""
	);
	// make the working copy not consistent
	setWorkingCopyContents(
		"""
			package p1;
			public class X {
			}"""
	);

	// simulate buffer being closed
	((TestBuffer) this.workingCopy.getBuffer()).contents = null;

	this.workingCopy.reconcile(ICompilationUnit.NO_AST, true, null, null);
	assertProblemsInclude(
		"Unexpected problems",
		"""
			----------
			1. ERROR in /Reconciler/src/p1/X.java (at line 1)
				package p1;
				^
			Cannot read the source from /Reconciler/src/p1/X.java due to internal exception java.io.IOException:Buffer is closed
			----------
			"""
	);
}
/**
 * Ensure an OperationCanceledException is correctly thrown when progress monitor is canceled
 * @deprecated using deprecated code
 */
public void testCancel() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			public class X {
			  void foo(String s) {
			  }
			}"""
	);
	this.workingCopy.makeConsistent(null);

	// count the number of time isCanceled() is called when converting this source unit
	CancelCounter counter = new CancelCounter();
	this.workingCopy.reconcile(AST.JLS2, true, null, counter);

	// throw an OperatonCanceledException at each point isCanceled() is called
	for (int i = 0; i < counter.count; i++) {
		boolean gotException = false;
		try {
			this.workingCopy.reconcile(AST.JLS2, true, null, new Canceler(i));
		} catch (OperationCanceledException e) {
			gotException = true;
		}
		assertTrue("Should get an OperationCanceledException (" + i + ")", gotException);
	}

	// last should not throw an OperationCanceledException
	this.workingCopy.reconcile(AST.JLS2, true, null, new Canceler(counter.count));
}
/**
 * Ensures that the delta is correct when adding a category
 */
public void testCategories1() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  /**
			   * @category cat1
			   */
			  public void foo() {
			  }
			}"""
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[*]: {CATEGORIES}"
	);
}
/**
 * Ensures that the delta is correct when removing a category
 */
public void testCategories2() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  /**
			   * @category cat1
			   */
			  public void foo() {
			  }
			}"""
	);
	this.workingCopy.makeConsistent(null);

	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public void foo() {
			  }
			}"""
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[*]: {CATEGORIES}"
	);
}
/**
 * Ensures that the delta is correct when changing a category
 */
public void testCategories3() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  /**
			   * @category cat1
			   */
			  public void foo() {
			  }
			}"""
	);
	this.workingCopy.makeConsistent(null);

	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  /**
			   * @category cat2
			   */
			  public void foo() {
			  }
			}"""
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[*]: {CATEGORIES}"
	);
}
/*
 * Ensures that the delta is correct when adding a category to a second field
 * (regression test for bug 125675 @category not reflected in outliner in live fashion)
 */
public void testCategories4() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  /**
			   * @category cat1
			   */
			  int f1;
			  int f2;
			}"""
	);
	this.workingCopy.makeConsistent(null);

	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  /**
			   * @category cat1
			   */
			  int f1;
			  /**
			   * @category cat2
			   */
			  int f2;
			}"""
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	f2[*]: {CATEGORIES}"
	);
}
/*
 * Ensures that changing the source level to make a type valid doesn't report an error any longer
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=233568 )
 */
public void testChangeSourceLevel1() throws Exception {
	try {
		IJavaProject p = createJavaProject("P1", new String[] {"src"}, new String[] {"JCL15_LIB", "/P1/lib.jar"}, "bin", "1.5");
		Util.createJar(new String[] {
				"p/enum/X.java",
				"""
					package p.enum;
					public class X{
					}"""
			},
			p.getProject().getLocation().append("lib.jar").toOSString(),
			"1.3");
		refresh(p);
		setUpWorkingCopy(
			"/P1/src/p1/X.java",
			"""
				package p1;
				public class X {
				  p.enum.X field;
				}""");
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		/* At this point, the following error is reported:
		"----------\n" +
		"1. ERROR in /P1/src/p1/X.java (at line 3)\n" +
		"	p.enum.X field;\n" +
		"	  ^^^^\n" +
		"Syntax error on token \"enum\", Identifier expected\n" +
		"----------\n"
		*/

		this.problemRequestor.reset();
		p.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true/*force problem detection*/, null, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. WARNING in /P1/src/p1/X.java (at line 3)
					p.enum.X field;
					  ^^^^
				\'enum\' should not be used as an identifier, since it is a reserved keyword from source level 1.5 on
				----------
				"""
		);
	} finally {
		deleteProject("P1");
	}
}
/*
 * Ensures that changing the source level to make a type valid doesn't report an error any longer
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=233568 )
 */
public void testChangeSourceLevel2() throws Exception {
	Hashtable defaultOptions = null;
	try {
		defaultOptions = JavaCore.getOptions();
		Hashtable newOptions = new Hashtable();
		newOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		JavaCore.setOptions(newOptions);
		IJavaProject p = createJavaProject("P1", new String[] {"src"}, new String[] {"JCL15_LIB", "/P1/lib.jar"}, "bin");
		Util.createJar(new String[] {
				"p/enum/X.java",
				"""
					package p.enum;
					public class X{
					}"""
			},
			p.getProject().getLocation().append("lib.jar").toOSString(),
			"1.3");
		refresh(p);
		setUpWorkingCopy(
			"/P1/src/p1/X.java",
			"""
				package p1;
				public class X {
				  p.enum.X field;
				}""");
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		/* At this point, the following error is reported:
		"----------\n" +
		"1. ERROR in /P1/src/p1/X.java (at line 3)\n" +
		"	p.enum.X field;\n" +
		"	  ^^^^\n" +
		"Syntax error on token \"enum\", Identifier expected\n" +
		"----------\n"
		*/

		this.problemRequestor.reset();
		newOptions.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
		JavaCore.setOptions(newOptions);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true/*force problem detection*/, null, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. WARNING in /P1/src/p1/X.java (at line 3)
					p.enum.X field;
					  ^^^^
				\'enum\' should not be used as an identifier, since it is a reserved keyword from source level 1.5 on
				----------
				"""
		);
	} finally {
		deleteProject("P1");
		if (defaultOptions != null)
			JavaCore.setOptions(defaultOptions);
	}
}
/*
 * Ensures that changing a binary folder used as class folder in 2 projects doesn't cause the old binary to be seen
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=210746 )
 */
public void testChangeClassFolder() throws CoreException {
	try {
		createJavaProject("P1", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
		createFolder("/P1/src/p");
		createFile(
			"/P1/src/p/X.java",
			"""
				package p;
				public class X {
				}"""
		);
		getProject("P1").build(IncrementalProjectBuilder.FULL_BUILD, null);
		createJavaProject("P2", new String[0], new String[] {"/P1/bin"}, "bin");
		createJavaProject("P3", new String[] {"src"}, new String[] {"JCL_LIB", "/P1/bin"}, "bin");
		setUpWorkingCopy(
			"/P3/src/q/Y.java",
			"""
				package q;
				import p.X;
				public class Y {
				  void foo(X x) {
				  }
				}"""
		);
		editFile(
			"/P1/src/p/X.java",
			"""
				package p;
				public class X {
				  public void bar() {
				  }
				}"""
		);
		getProject("P1").build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
		setWorkingCopyContents(
			"""
				package q;
				import p.X;
				public class Y {
				  void foo(X x) {
				    x.bar();
				  }
				}"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		deleteProjects(new String[] {"P1", "P2", "P3"});
	}
}
/*
 * Ensures that changing and external jar and refreshing takes the change into account
 * (regression test for bug 134110 [regression] Does not pick-up interface changes from classes in the build path)
 */
public void testChangeExternalJar() throws CoreException, IOException {
	IJavaProject project = getJavaProject("Reconciler");
	String jarPath = getExternalPath() + "lib.jar";
	try {
		createJar(new String[] {
			"p/Y.java",
			"""
				package p;
				public class Y {
				  public void foo() {
				  }
				}"""
		}, jarPath);
		addLibraryEntry(project, jarPath, false);

		// force Y.class file to be cached during resolution
		setWorkingCopyContents(
			"""
				package p1;
				public class X extends p.Y {
				  public void bar() {
				    foo();
				  }
				}""");
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

		// change jar and refresh
		createJar(new String[] {
			"p/Y.java",
			"""
				package p;
				public class Y {
				  public void foo(String s) {
				  }
				}"""
		}, jarPath);
		getJavaModel().refreshExternalArchives(null,null);

		setWorkingCopyContents(
			"""
				package p1;
				public class X extends p.Y {
				  public void bar() {
				    foo("a");
				  }
				}""");
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		removeClasspathEntry(project, new Path(jarPath));
		deleteResource(new File(jarPath));
	}
}
/**
 * bug 162621: [model][delta] Validation errors do not clear after replacing jar file
 * test Ensures that changing an internal jar and refreshing takes the change into account
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=162621"
 */
public void testChangeInternalJar() throws CoreException, IOException {
	IJavaProject project = getJavaProject("Reconciler");
	String jarName = "b162621.jar";
	try {
		String[] pathAndContents = new String[] {
			"test/before/Foo.java",
			"""
				package test.before;
				public class Foo {
				}
				"""
		};
		addLibrary(project, jarName, "b162621_src.zip", pathAndContents, JavaCore.VERSION_1_4);

		// Wait a little bit to be sure file system is aware of zip file creation
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException ie) {
			// skip
		}

		// Set working copy content with no error
		setUpWorkingCopy("/Reconciler/src/test/Test.java",
			"""
				package test;
				import test.before.Foo;
				public class Test {
					Foo f;
				}
				"""
		);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);

		// Update working copy with Jar expected changes
		String contents = """
			package test;
			import test.after.Foo;
			public class Test {
				Foo f;
			}
			""";
		setWorkingCopyContents(contents);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true, null, null);
		assertProblems(
			"Wrong expected problems",
			"""
				----------
				1. ERROR in /Reconciler/src/test/Test.java (at line 2)
					import test.after.Foo;
					       ^^^^^^^^^^
				The import test.after cannot be resolved
				----------
				2. ERROR in /Reconciler/src/test/Test.java (at line 4)
					Foo f;
					^^^
				Foo cannot be resolved to a type
				----------
				"""
		);

		// change jar and refresh
		String projectLocation = project.getProject().getLocation().toOSString();
		String jarPath = projectLocation + File.separator + jarName;
		createJar(new String[] {
			"test/after/Foo.java",
			"""
				package test.after;
				public class Foo {
				}
				"""
		}, jarPath);
		project.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);

		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException ie) {
			// skip
		}

		// Verify that error is gone
		this.problemRequestor.initialize(contents.toCharArray());
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		removeClasspathEntry(project, new Path(jarName));
		deleteResource(new File(jarName));
	}
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of a method's type parameter change.
 */
public void testChangeMethodTypeParameters() throws JavaModelException {
	setUp15WorkingCopy();
	clearDeltas();
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public <T> void foo() {
			  }
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[*]: {CONTENT}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of a type's type parameter change.
 */
public void testChangeTypeTypeParameters() throws JavaModelException {
	setUp15WorkingCopy();
	clearDeltas();
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X <T> {
			  public void foo() {
			  }
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"X[*]: {CONTENT}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of a method visibility change.
 */
public void testChangeMethodVisibility() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  private void foo() {
			  }
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[*]: {MODIFIERS CHANGED}"
	);
}
/**
 * Ensures that the correct delta is reported when closing the working copy and modifying its buffer.
 */
public void testCloseWorkingCopy() throws JavaModelException {
	IBuffer buffer = this.workingCopy.getBuffer();
	this.workingCopy.close();
	buffer.setContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public void foo() {
			  }
			  public void bar() {
			  }
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	bar()[+]: {}"
	);
}

/**
 * Ensures that a reference to a constant with type mismatch doesn't show an error.
 * (regression test for bug 17104 Compiler does not complain but "Quick Fix" ??? complains)
 */
public void testConstantReference() throws CoreException {
	try {
		createFile(
			"/Reconciler/src/p1/OS.java",
			"""
				package p1;
				public class OS {
					public static final int CONST = 23 * 1024;
				}""");
		setWorkingCopyContents(
			"""
				package p1;
				public class X {
					public short c;
					public static void main(String[] arguments) {
						short c = 1;
						switch (c) {
							case OS.CONST: return;
						}
					}
				}""");
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		deleteFile("/Reconciler/src/p1/OS.java");
	}
}
/*
 * Ensures that the source type converter doesn't throw an OutOfMemoryError if converting a generic type with a primitive type array as argument
 * (regression test for bug 135296 opening a special java file results in an "out of memory" message)
 */
public void testConvertPrimitiveTypeArrayTypeArgument() throws CoreException {
	ICompilationUnit otherCopy = null;
	try {
		otherCopy = getWorkingCopy(
			"Reconciler15/src/Y.java",
			"""
				public class Y {
				  void foo(Z<int[]> z) {}
				}
				class Z<E> {
				}""",
			this.wcOwner
		);
		setUp15WorkingCopy("/Reconciler15/src/X.java", this.wcOwner);
		setWorkingCopyContents(
			"""
				public class X {
				  void bar(Y y) {
				    y.foo(new Z<int[]>());
				  }
				}"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		if (otherCopy != null)
			otherCopy.discardWorkingCopy();
	}
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of a method being deleted.
 */
public void testDeleteMethod1() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[-]: {}"
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of two methods being deleted.
 */
public void testDeleteTwoMethods() throws JavaModelException {
	// create 2 methods
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public void foo() {
			  }
			  public void bar() {
			  }
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

	// delete the 2 methods
	clearDeltas();
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"""
			X[*]: {CHILDREN | FINE GRAINED}
				bar()[-]: {}
				foo()[-]: {}"""
	);
}
/*
 * Ensures that excluded part of prereq project are not visible
 */
public void testExcludePartOfAnotherProject1() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P", "-**/internal/"});
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P");
		createFolder("/P/p/internal");
		createFile(
			"/P/p/internal/Y.java",
			"""
				package p.internal;
				public class Y {
				}"""
		);
		setWorkingCopyContents(
			"""
				package p1;
				public class X extends p.internal.Y {
				}"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. ERROR in /Reconciler/src/p1/X.java (at line 2)
					public class X extends p.internal.Y {
					                       ^^^^^^^^^^^^
				Access restriction: The type \'Y\' is not API (restriction on required project \'P\')
				----------
				"""
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProject("P");
	}
}
/*
 * Ensures that packages that are not in excluded part of prereq project are visible
 */
public void testExcludePartOfAnotherProject2() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P", "-**/internal/"});
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P");
		createFolder("/P/p/api");
		createFile(
			"/P/p/api/Y.java",
			"""
				package p.api;
				public class Y {
				}"""
		);
		setWorkingCopyContents(
			"""
				package p1;
				public class X extends p.api.Y {
				}"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProject("P");
	}
}
/*
 * Ensures that an external working copy can be reconciled with no error.
 */
public void testExternal1() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	this.problemRequestor =  new ProblemRequestor();
	IClasspathEntry[] classpath = new IClasspathEntry[] {JavaCore.newLibraryEntry(getExternalJCLPath(), null, null)};
	this.workingCopy = newExternalWorkingCopy("External.java", classpath, this.problemRequestor,
		"""
			public class External {
				String foo(){
					return "";
				}
			}
			"""
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null/*no owner*/, null);

	assertProblems(
		"Unexpected problems",
		"----------\n" +
		"----------\n"
	);
}

/*
 * Ensures that an external working copy with a container classpath entry can be reconciled with no exception.
 * (regression test for bug 148970 Exceptions opening external Java file)
 */
public void testExternal2() throws CoreException {
	class LogListener implements ILogListener {
    	IStatus loggedStatus;
        public void logging(IStatus status, String plugin) {
            this.loggedStatus = status;
        }
	}
	LogListener listener = new LogListener();
	try {
		Platform.addLogListener(listener);
		this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
		this.workingCopy = null;
		this.problemRequestor =  new ProblemRequestor();
		ContainerInitializer.setInitializer(new DefaultContainerInitializer(new String[] {" ", getExternalJCLPathString()}));
		IClasspathEntry[] classpath = new IClasspathEntry[] {
			JavaCore.newContainerEntry(new Path("org.eclipse.jdt.core.tests.model.TEST_CONTAINER"))
		};
		this.workingCopy = newExternalWorkingCopy("External.java", classpath, this.problemRequestor,
			"""
				public class External {
					String foo(){
						return "";
					}
				}
				"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null/*no owner*/, null);
		assertEquals("Should not get any exception in log", null, listener.loggedStatus);
	} finally {
		Platform.removeLogListener(listener);
	}
}

/*
 * Ensures that an error is detected after refreshing external archives used by
 * an external working copy.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=216772 )
 */
public void testExternal3() throws Exception {
	try {
		this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
		this.workingCopy = null;
		this.problemRequestor =  new ProblemRequestor();
		createJar(new String[] {
			"p/Lib.java",
			"""
				package p;
				public class Lib {
				}"""
		}, getExternalResourcePath("lib.jar"));
		IClasspathEntry[] classpath = new IClasspathEntry[] {
			JavaCore.newLibraryEntry(getExternalJCLPath(), null, null),
			JavaCore.newLibraryEntry(new Path(getExternalResourcePath("lib.jar")), null, null)
		};
		this.workingCopy = newExternalWorkingCopy("External.java", classpath, this.problemRequestor,
			"""
				public class External {
					p.Lib field;
				}
				"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null/*no owner*/, null);

		createJar(new String[] {
			"p/Lib2.java",
			"""
				package p;
				public class Lib2 {
				}"""
		}, getExternalResourcePath("lib.jar"));
		getJavaModel().refreshExternalArchives(null, null);
		this.problemRequestor.reset();
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true/*force problem detection*/, null/*no owner*/, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. ERROR in / /External.java
				p.Lib cannot be resolved to a type
				----------
				"""
		);
	} finally {
		deleteExternalResource("lib.jar");
	}
}

/*
 * Ensures that included part of prereq project are visible
 */
public void testIncludePartOfAnotherProject1() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P", "+**/api/"});
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P");
		createFolder("/P/p/api");
		createFile(
			"/P/p/api/Y.java",
			"""
				package p.api;
				public class Y {
				}"""
		);
		setWorkingCopyContents(
			"""
				package p1;
				public class X extends p.api.Y {
				}"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProject("P");
	}
}
/*
 * Ensures that packages that are not in included part of prereq project are not visible
 */
public void testIncludePartOfAnotherProject2() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P", "+**/api/|-**"});
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P");
		createFolder("/P/p/internal");
		createFile(
			"/P/p/internal/Y.java",
			"""
				package p.internal;
				public class Y {
				}"""
		);
		setWorkingCopyContents(
			"""
				package p1;
				public class X extends p.internal.Y {
				}"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. ERROR in /Reconciler/src/p1/X.java (at line 2)
					public class X extends p.internal.Y {
					                       ^^^^^^^^^^^^
				Access restriction: The type \'Y\' is not API (restriction on required project \'P\')
				----------
				"""
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProject("P");
	}
}
/**
 * Start with no imports, add an import, and then append to the import name.
 */
public void testGrowImports() throws JavaModelException {
	// no imports
	setWorkingCopyContents(
		"""
			package p1;
			public class X {
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

	// add an import
	clearDeltas();
	setWorkingCopyContents(
		"""
			package p1;
			import p
			public class X {
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"<import container>[+]: {}"
	);

	// append to import name
	clearDeltas();
	setWorkingCopyContents(
		"""
			package p1;
			import p2
			public class X {
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"""
			<import container>[*]: {CHILDREN | FINE GRAINED}
				import p2[+]: {}
				import p[-]: {}"""
	);
}
/*
 * Ensures that a type matching a ignore-if-better non-accessible rule is further found when accessible
 * on another classpath entry.
 * (regression test for bug 98127 Access restrictions started showing up after switching to bundle)
 */
public void testIgnoreIfBetterNonAccessibleRule1() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P1", "?**/internal/", "/P2", "+**/internal/Y"});
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P1");
		createFolder("/P1/p/internal");
		createFile(
			"/P1/p/internal/Y.java",
			"""
				package p.internal;
				public class Y {
				}"""
		);
		createJavaProject("P2");
		createFolder("/P2/p/internal");
		createFile(
			"/P2/p/internal/Y.java",
			"""
				package p.internal;
				public class Y {
				}"""
		);
		setWorkingCopyContents(
			"""
				package p1;
				public class X extends p.internal.Y {
				}"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProjects(new String[] {"P1", "P2"});
	}
}
/*
 * Ensures that a type matching a ignore-if-better non-accessible rule is further found when accessible
 * on another classpath entry.
 * (regression test for bug 98127 Access restrictions started showing up after switching to bundle)
 */
public void testIgnoreIfBetterNonAccessibleRule2() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P1", "?**/internal/", "/P2", "~**/internal/Y"});
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P1");
		createFolder("/P1/p/internal");
		createFile(
			"/P1/p/internal/Y.java",
			"""
				package p.internal;
				public class Y {
				}"""
		);
		createJavaProject("P2");
		createFolder("/P2/p/internal");
		createFile(
			"/P2/p/internal/Y.java",
			"""
				package p.internal;
				public class Y {
				}"""
		);
		setWorkingCopyContents(
			"""
				package p1;
				public class X extends p.internal.Y {
				}"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. WARNING in /Reconciler/src/p1/X.java (at line 2)
					public class X extends p.internal.Y {
					                       ^^^^^^^^^^^^
				Discouraged access: The type \'Y\' is not API (restriction on required project \'P2\')
				----------
				"""
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProjects(new String[] {"P1", "P2"});
	}
}
/*
 * Ensures that a type matching a ignore-if-better non-accessible rule is further found non-accessible
 * on another classpath entry.
 * (regression test for bug 98127 Access restrictions started showing up after switching to bundle)
 */
public void testIgnoreIfBetterNonAccessibleRule3() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P1", "?**/internal/", "/P2", "-**/internal/Y"});
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P1");
		createFolder("/P1/p/internal");
		createFile(
			"/P1/p/internal/Y.java",
			"""
				package p.internal;
				public class Y {
				}"""
		);
		createJavaProject("P2");
		createFolder("/P2/p/internal");
		createFile(
			"/P2/p/internal/Y.java",
			"""
				package p.internal;
				public class Y {
				}"""
		);
		setWorkingCopyContents(
			"""
				package p1;
				public class X extends p.internal.Y {
				}"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. ERROR in /Reconciler/src/p1/X.java (at line 2)
					public class X extends p.internal.Y {
					                       ^^^^^^^^^^^^
				Access restriction: The type \'Y\' is not API (restriction on required project \'P1\')
				----------
				"""
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProjects(new String[] {"P1", "P2"});
	}
}
/*
 * Ensures that a type matching a ignore-if-better non-accessible rule is found non-accessible
 * if no other classpath entry matches it.
 * (regression test for bug 98127 Access restrictions started showing up after switching to bundle)
 */
public void testIgnoreIfBetterNonAccessibleRule4() throws CoreException {
	IClasspathEntry[] newEntries = createClasspath("Reconciler", new String[] {"/P1", "?**/internal/"});
	try {
		addClasspathEntries(newEntries, true);
		createJavaProject("P1");
		createFolder("/P1/p/internal");
		createFile(
			"/P1/p/internal/Y.java",
			"""
				package p.internal;
				public class Y {
				}"""
		);
		setWorkingCopyContents(
			"""
				package p1;
				public class X extends p.internal.Y {
				}"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. ERROR in /Reconciler/src/p1/X.java (at line 2)
					public class X extends p.internal.Y {
					                       ^^^^^^^^^^^^
				Access restriction: The type \'Y\' is not API (restriction on required project \'P1\')
				----------
				"""
		);
	} finally {
		removeClasspathEntries(newEntries);
		deleteProjects(new String[] {"P1"});
	}
}
/**
 * Introduces a syntax error in the modifiers of a method.
 */
public void testMethodWithError01() throws CoreException {
	// Introduce syntax error
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public.void foo() {
			  }
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta after syntax error",
		"[Working copy] X.java[*]: {CONTENT | FINE GRAINED}"
	);
	assertProblems(
		"Unexpected problems",
		"""
			----------
			1. ERROR in /Reconciler/src/p1/X.java (at line 4)
				public.void foo() {
				      ^
			Syntax error on token ".", delete this token
			----------
			"""
	);

	// Fix the syntax error
	clearDeltas();
	String contents =
		"""
		package p1;
		import p2.*;
		public class X {
		  public void foo() {
		  }
		}""";
	setWorkingCopyContents(contents);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta after fixing syntax error",
		"[Working copy] X.java[*]: {CONTENT | FINE GRAINED}"
	);
	assertProblems(
		"Unexpected problems",
		"""
			----------
			1. WARNING in /Reconciler/src/p1/X.java (at line 2)
				import p2.*;
				       ^^
			The import p2 is never used
			----------
			"""
	);
}
/**
 * Introduces a syntax error in the modifiers of a method.
 * Variant to force the expected modifier change.
 */
public void testMethodWithError01a() throws CoreException {
	// Introduce syntax error
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public_ void foo() {
			  }
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta after syntax error",
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[*]: {MODIFIERS CHANGED}"
	);
	assertProblems(
		"Unexpected problems",
		"""
			----------
			1. ERROR in /Reconciler/src/p1/X.java (at line 4)
				public_ void foo() {
				^^^^^^^
			Syntax error on token "public_", public expected
			----------
			"""
	);

	// Fix the syntax error
	clearDeltas();
	String contents =
		"""
		package p1;
		import p2.*;
		public class X {
		  public void foo() {
		  }
		}""";
	setWorkingCopyContents(contents);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta after fixing syntax error",
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[*]: {MODIFIERS CHANGED}"
	);
	assertProblems(
		"Unexpected problems",
		"""
			----------
			1. WARNING in /Reconciler/src/p1/X.java (at line 2)
				import p2.*;
				       ^^
			The import p2 is never used
			----------
			"""
	);
}
/**
 * Test reconcile force flag
 */
public void testMethodWithError02() throws CoreException {
	String contents =
		"""
		package p1;
		import p2.*;
		public class X {
		  public.void foo() {
		  }
		}""";
	setWorkingCopyContents(contents);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

	// use force flag to refresh problems
	this.problemRequestor.initialize(contents.toCharArray());
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, true, null, null);
	assertProblems(
		"Unexpected problems",
		"""
			----------
			1. ERROR in /Reconciler/src/p1/X.java (at line 4)
				public.void foo() {
				      ^
			Syntax error on token ".", delete this token
			----------
			"""
	);
}

/**
 * Test reconcile force flag off
 */
public void testMethodWithError03() throws CoreException {
	String contents =
		"""
		package p1;
		import p2.*;
		public class X {
		  public.void foo() {
		  }
		}""";
	setWorkingCopyContents(contents);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

	// reconcile with force flag turned off
	this.problemRequestor.initialize(contents.toCharArray());
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertProblems(
		"Unexpected problems",
		""
	);
}
/**
 * Test reconcile force flag + cancel
 */
public void testMethodWithError04() throws CoreException {

	CancelingProblemRequestor myPbRequestor = new CancelingProblemRequestor();

	this.workingCopy.discardWorkingCopy();
	ICompilationUnit x = getCompilationUnit("Reconciler", "src", "p1", "X.java");
	this.problemRequestor = myPbRequestor;
	this.workingCopy = x.getWorkingCopy(this.wcOwner, null);

	String contents =
		"""
		package p1;
		public class X {
			Zork f;\t
			void foo(Zork z){
			}
		}\t
		""";
	setWorkingCopyContents(contents);

	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

	// use force flag to refresh problems
	myPbRequestor.isCanceling = true;
	myPbRequestor.initialize(contents.toCharArray());
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, true, null, myPbRequestor.progressMonitor);
	assertProblems(
		"Unexpected problems",
		"""
			----------
			1. ERROR in /Reconciler/src/p1/X.java (at line 3)
				Zork f;\t
				^^^^
			Zork cannot be resolved to a type
			----------
			"""
	);
}

/**
 * Test reconcile force flag off
 */
public void testMethodWithError05() throws CoreException {
	try {
		createFolder("/Reconciler/src/tests");
		String contents =
			"""
			package tests;\t
			abstract class AbstractSearchableSource extends AbstractSource implements SearchableSource {\t
				abstract int indexOfImpl(long value);\t
				public final int indexOf(long value) {\t
					return indexOfImpl(value);\t
				}\t
			}\t
			""";
		createFile(
			"/Reconciler/src/tests/AbstractSearchableSource.java",
			contents);

		createFile(
			"/Reconciler/src/tests/Source.java",
			"""
				package tests;\t
				interface Source {\t
					long getValue(int index);\t
					int size();\t
				}\t
				""");

		createFile(
			"/Reconciler/src/tests/AbstractSource.java",
			"""
				package tests;\t
				abstract class AbstractSource implements Source {\t
					AbstractSource() {\t
					}\t
					void invalidate() {\t
					}\t
					abstract long getValueImpl(int index);\t
					abstract int sizeImpl();\t
					public final long getValue(int index) {\t
						return 0;\t
					}\t
					public final int size() {\t
						return 0;\t
					}\t
				}\t
				""");

		createFile(
			"/Reconciler/src/tests/SearchableSource.java",
			"""
				package tests;\t
				interface SearchableSource extends Source {\t
					int indexOf(long value);\t
				}\t
				""");

		ICompilationUnit compilationUnit = getCompilationUnit("Reconciler", "src", "tests", "AbstractSearchableSource.java");
		ProblemRequestor pbReq =  new ProblemRequestor();
		WorkingCopyOwner owner = newWorkingCopyOwner(pbReq);
		ICompilationUnit wc = compilationUnit.getWorkingCopy(owner, null);
		pbReq.initialize(contents.toCharArray());
		startDeltas();
		wc.reconcile(ICompilationUnit.NO_AST, true, null, null);
		String actual = pbReq.problems.toString();
		String expected =
			"----------\n" +
			"----------\n";
		if (!expected.equals(actual)){
		 	System.out.println(Util.displayString(actual, 2));
		}
		assertEquals(
			"unexpected errors",
			expected,
			actual);
	} finally {
		deleteFile("/Reconciler/src/tests/AbstractSearchableSource.java");
		deleteFile("/Reconciler/src/tests/SearchableSource.java");
		deleteFile("/Reconciler/src/tests/Source.java");
		deleteFile("/Reconciler/src/tests/AbstractSource.java");
		deleteFolder("/Reconciler/src/tests");
	}
}
/*
 * Test that the creation of a working copy detects errors
 * (regression test for bug 33757 Problem not detected when opening a working copy)
 */
public void testMethodWithError06() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	try {
		String contents =
			"""
			package p1;
			public class Y {
			  public.void foo() {
			  }
			}""";
		createFile(
			"/Reconciler/src/p1/Y.java",
			contents
		);
		this.problemRequestor =  new ProblemRequestor();
		this.problemRequestor.initialize(contents.toCharArray());
		this.workingCopy = getCompilationUnit("Reconciler/src/p1/Y.java").getWorkingCopy(this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. ERROR in /Reconciler/src/p1/Y.java (at line 3)
					public.void foo() {
					      ^
				Syntax error on token ".", delete this token
				----------
				"""
		);
	} finally {
		deleteFile("/Reconciler/src/p1/Y.java");
	}
}
/*
 * Test that the opening of a working copy detects errors
 * (regression test for bug 33757 Problem not detected when opening a working copy)
 */
public void testMethodWithError07() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	try {
		String contents =
			"""
			package p1;
			public class Y {
			  public.void foo() {
			  }
			}""";
		createFile(
			"/Reconciler/src/p1/Y.java",
			contents
		);
		this.problemRequestor =  new ProblemRequestor();
		this.problemRequestor.initialize(contents.toCharArray());
		this.workingCopy = getCompilationUnit("Reconciler/src/p1/Y.java").getWorkingCopy(this.wcOwner, null);

		// Close working copy
		JavaModelManager.getJavaModelManager().removeInfoAndChildren((CompilationUnit)this.workingCopy); // use a back door as working copies cannot be closed

		// Reopen should detect syntax error
		this.problemRequestor.initialize(contents.toCharArray());
		this.workingCopy.open(null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. ERROR in /Reconciler/src/p1/Y.java (at line 3)
					public.void foo() {
					      ^
				Syntax error on token ".", delete this token
				----------
				"""
		);
	} finally {
		deleteFile("/Reconciler/src/p1/Y.java");
	}
}

/** tests that after closing a workingcopy all elements are removed from JavaModelCache**/
public void testCloseMethodArgumentChildren() throws Exception {
	this.workingCopy.discardWorkingCopy();
	this.workingCopy = null;
	try {
		String contents = """
			package p1;
			public class Y {
			  void foo(int i) {
			  }
			}""";
		createFile("/Reconciler/src/p1/Y.java", contents);
		this.problemRequestor = new ProblemRequestor();
		this.problemRequestor.initialize(contents.toCharArray());
		WorkingCopyOwner owner = new WorkingCopyOwner(){};

		long owningCount0 = getCachedElementCount(owner);
		// Create working copy
		this.workingCopy = getCompilationUnit("Reconciler/src/p1/Y.java").getWorkingCopy(owner, null);
		// Close working copy
		long owningCount1 = getCachedElementCount(owner);
		assertTrue(owningCount1 - owningCount0 > 0);
		JavaModelManager.getJavaModelManager().removeInfoAndChildren((CompilationUnit) this.workingCopy);
		long owningCount2 = getCachedElementCount(owner);
		assertEquals(0, owningCount2 - owningCount0); // did fail with 1 element (the method argument)
		assertEquals(0, owningCount0);
		assertEquals(0, owningCount2);
	} finally {
		deleteFile("/Reconciler/src/p1/Y.java");
	}
}
private static long getCachedElementCount(WorkingCopyOwner owner) throws Exception {
	JavaModelManager javaModelManager = JavaModelManager.getJavaModelManager();
	// private JavaModelCache javaModelCache=javaModelManager.cache:
	Field cacheField = javaModelManager.getClass().getDeclaredField("cache");
	cacheField.setAccessible(true);
	JavaModelCache javaModelCache = (JavaModelCache) cacheField.get(javaModelManager);
	// private Map<IJavaElement, Object> childrenCache=javaModelCache.childrenCache:
	Field childrenCacheField = javaModelCache.getClass().getDeclaredField("childrenCache");
	childrenCacheField.setAccessible(true);
	Map<IJavaElement, Object> childrenCache = (Map<IJavaElement, Object>) childrenCacheField.get(javaModelCache);
	return childrenCache.keySet().stream()
			.filter(je -> ((((JavaElement) je).getCompilationUnit()==null)?null:((JavaElement) je).getCompilationUnit().getOwner()) == owner).count();
}

/*
 * Test that the units with similar names aren't presenting each other errors
 * (regression test for bug 39475)
 */
public void testMethodWithError08() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	try {
		createFile(
			"/Reconciler/src/p1/X01.java",
			"""
				package p1;
				public abstract class X01 {
					public abstract void bar();\t
				  public abstract void foo(Zork z);\s
				}"""
		);
		String contents =
			"""
			package p2;
			public class X01 extends p1.X01 {
				public void bar(){}\t
			}""";
		createFile(
			"/Reconciler/src/p2/X01.java",
			contents
		);
		this.problemRequestor =  new ProblemRequestor();
		this.problemRequestor.initialize(contents.toCharArray());
		this.workingCopy = getCompilationUnit("Reconciler/src/p2/X01.java").getWorkingCopy(this.wcOwner, null);

		// Close working copy
		JavaModelManager.getJavaModelManager().removeInfoAndChildren((CompilationUnit)this.workingCopy); // use a back door as working copies cannot be closed

		// Reopen should detect syntax error
		this.problemRequestor.initialize(contents.toCharArray());
		this.workingCopy.open(null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. ERROR in /Reconciler/src/p2/X01.java (at line 2)
					public class X01 extends p1.X01 {
					             ^^^
				The type X01 must implement the inherited abstract method X01.foo(Zork)
				----------
				"""
		);
	} finally {
		deleteFile("/Reconciler/src/p1/X01.java");
		deleteFile("/Reconciler/src/p2/X01.java");
	}
}
/*
 * Scenario of reconciling using a working copy owner
 */
public void testMethodWithError09() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	ICompilationUnit workingCopy1 = null;
	try {
		workingCopy1 = getCompilationUnit("/Reconciler/src/p1/X1.java").getWorkingCopy(this.wcOwner, null);
		workingCopy1.getBuffer().setContents(
			"""
				package p1;
				public abstract class X1 {
					public abstract void bar();\t
				}"""
		);
		workingCopy1.makeConsistent(null);

		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler/src/p/X.java").getWorkingCopy(this.wcOwner, null);
		setWorkingCopyContents(
			"""
				package p;
				public class X extends p1.X1 {
					public void bar(){}\t
				}"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, this.wcOwner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n" // shouldn't report problem against p.X
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
	}
}
/*
 * Scenario of reconciling using a working copy owner  (68557)
 */
public void testMethodWithError10() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	ICompilationUnit workingCopy1 = null;
	try {
		createFolder("/Reconciler15/src/test/cheetah");
		workingCopy1 = getCompilationUnit("/Reconciler15/src/test/cheetah/NestedGenerics.java").getWorkingCopy(this.wcOwner, null);
		workingCopy1.getBuffer().setContents(
			"""
				package test.cheetah;
				import java.util.List;
				import java.util.Stack;
				public class NestedGenerics {
				    Stack< List<Object>> stack = new Stack< List<Object> >();
				}
				"""
		);
		workingCopy1.makeConsistent(null);

		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler15/src/test/cheetah/NestedGenericsTest.java").getWorkingCopy(this.wcOwner, null);
		setWorkingCopyContents(
			"""
				package test.cheetah;
				import java.util.Stack;
				public class NestedGenericsTest {
				    void test() { \s
				        Stack s = new NestedGenerics().stack; \s
				    }
				}
				"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, this.wcOwner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
		deleteFolder("/Reconciler15/src/test");
	}
}
/*
 * Scenario of reconciling using a working copy owner (68557)
 */
public void testMethodWithError11() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	ICompilationUnit workingCopy1 = null;
	try {
		createFolder("/Reconciler15/src/test/cheetah");
		workingCopy1 = getCompilationUnit("/Reconciler15/src/test/cheetah/NestedGenerics.java").getWorkingCopy(this.wcOwner, null);
		workingCopy1.getBuffer().setContents(
			"""
				package test.cheetah;
				import java.util.*;
				public class NestedGenerics {
				    Map<List<Object>,String> map = null;
				    Stack<List<Object>> stack2 = null;
				    Map<List<Object>,List<Object>> map3 = null;
				}
				"""
		);
		workingCopy1.makeConsistent(null);

		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler15/src/test/cheetah/NestedGenericsTest.java").getWorkingCopy(this.wcOwner, null);
		setWorkingCopyContents(
			"""
				package test.cheetah;
				import java.util.*;
				public class NestedGenericsTest {
				    void test() { \s
				        Map m = new NestedGenerics().map; \s
						 Stack s2 = new NestedGenerics().stack2;   \s
				        Map m3 = new NestedGenerics().map3;   \s
				    }
				}
				"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, this.wcOwner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
		deleteFolder("/Reconciler15/src/test");
	}
}
/*
 * Scenario of reconciling using a working copy owner (68557 variation with wildcards)
 */
public void testMethodWithError12() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	ICompilationUnit workingCopy1 = null;
	try {
		createFolder("/Reconciler15/src/test/cheetah");
		workingCopy1 = getCompilationUnit("/Reconciler15/src/test/cheetah/NestedGenerics.java").getWorkingCopy(this.wcOwner, null);
		workingCopy1.getBuffer().setContents(
			"""
				package test.cheetah;
				import java.util.*;
				public class NestedGenerics {
				    Map<List<?>,? super String> map = null;
				    Stack<List<? extends Object>> stack2 = null;
				    Map<List<Object[]>,List<Object>[]> map3 = null;
				}
				"""
		);
		workingCopy1.makeConsistent(null);

		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler15/src/test/cheetah/NestedGenericsTest.java").getWorkingCopy(this.wcOwner, null);
		setWorkingCopyContents(
			"""
				package test.cheetah;
				import java.util.*;
				public class NestedGenericsTest {
				    void test() { \s
				        Map m = new NestedGenerics().map; \s
						 Stack s2 = new NestedGenerics().stack2;   \s
				        Map m3 = new NestedGenerics().map3;   \s
				    }
				}
				"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, this.wcOwner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
		deleteFolder("/Reconciler15/src/test");
	}
}
/*
 * Scenario of reconciling using a working copy owner (68730)
 */
public void testMethodWithError13() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	ICompilationUnit workingCopy1 = null;
	this.problemRequestor =  null;
	try {
		workingCopy1 = getCompilationUnit("/Reconciler15/src/test/X.java").getWorkingCopy(this.wcOwner, null);
		createFolder("/Reconciler15/src/test");
		workingCopy1.getBuffer().setContents(
			"""
				package test;
				public class X <T extends String, U> {
					<Y1> void bar(Y1[] y) {}
					void bar2(Y<E3[]>[] ye[]) {}
				    void foo(java.util.Map<Object[],String>.MapEntry<p.K<T>[],? super q.r.V8> m){}
				    Class<? extends Object> getClass0() {}
				    <E extends String> void pair (X<? extends E, U> e, T t){}
				}
				"""
		);
		workingCopy1.makeConsistent(null);

		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler15/src/test/Y.java").getWorkingCopy(this.wcOwner, null);
		setWorkingCopyContents(
			"""
				package test;
				public class Y {
					void foo(){
						X someX = new X();
						someX.bar(null);
					}
				}
				"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, this.wcOwner, null);

		assertProblems(
			"Unexpected problems",
		"""
			----------
			1. WARNING in /Reconciler15/src/test/Y.java (at line 5)
				someX.bar(null);
				^^^^^^^^^^^^^^^
			Type safety: The method bar(Object[]) belongs to the raw type X. References to generic type X<T,U> should be parameterized
			----------
			"""
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
		deleteFolder("/Reconciler15/src/test");
	}
}
/*
 * Scenario of reconciling using a working copy owner (66424)
 */
public void testMethodWithError14() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	ICompilationUnit workingCopy1 = null;
	try {
		workingCopy1 = getCompilationUnit("/Reconciler15/src/test/X.java").getWorkingCopy(this.wcOwner, null);
		createFolder("/Reconciler15/src/test");
		workingCopy1.getBuffer().setContents(
			"""
				package test;
				public class X <T> {
					<U> void bar(U u) {}
				}
				"""
		);
		workingCopy1.makeConsistent(null);

		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler15/src/test/Y.java").getWorkingCopy(this.wcOwner, null);
		setWorkingCopyContents(
			"""
				package test;
				public class Y {
					void foo(){
						X someX = new X();
						someX.bar();
					}
				}
				"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, this.wcOwner, null);

		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. ERROR in /Reconciler15/src/test/Y.java (at line 5)
					someX.bar();
					      ^^^
				The method bar(Object) in the type X is not applicable for the arguments ()
				----------
				"""
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
		deleteFolder("/Reconciler15/src/test");
	}
}
/**
 * Ensures that the reconciler handles member move correctly.
 */
public void testMoveMember() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public void foo() {
			  }
			  public void bar() {
			  }
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	clearDeltas();

	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public void bar() {
			  }
			  public void foo() {
			  }
			}""");
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"""
			X[*]: {CHILDREN | FINE GRAINED}
				bar()[*]: {REORDERED}
				foo()[*]: {REORDERED}"""
	);
}
/**
 * Ensures that the reconciler does nothing when the source
 * to reconcile with is the same as the current contents.
 */
public void testNoChanges1() throws JavaModelException {
	setWorkingCopyContents(this.workingCopy.getSource());
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"[Working copy] X.java[*]: {CONTENT | FINE GRAINED}"
	);
}
/**
 * Ensures that the reconciler does nothing when the source
 * to reconcile with has the same structure as the current contents.
 */
public void testNoChanges2() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public void foo() {
			    System.out.println()
			  }
			}"""
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"[Working copy] X.java[*]: {CONTENT | FINE GRAINED}"
	);
}
/*
 * Ensures that the problem requestor is not called when the source
 * to reconcile is the same as the current contents,
 * no ast is requested, no problem is requested and problem requestor is not active.
 * (regression test for bug 179258 simple reconcile starts problem finder - main thread waiting)
 */
public void testNoChanges3() throws JavaModelException {
	setWorkingCopyContents(this.workingCopy.getSource());
	this.problemRequestor.isActive = false;
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertProblems("Unexpected problems", "");
}
/*
 * Ensures that using a non-generic method with no parameter and with a raw receiver type doesn't create a type safety warning
 * (regression test for bug 105756 [1.5][model] Incorrect warning on using raw types)
 */
public void testRawUsage() throws CoreException {
	ICompilationUnit otherCopy = null;
	try {
		otherCopy = getWorkingCopy(
			"Reconciler15/src/Generic105756.java",
			"""
				public class Generic105756<T> {
				  void foo() {}
				}""",
			this.wcOwner
		);
		setUp15WorkingCopy("/Reconciler15/src/X.java", this.wcOwner);
		setWorkingCopyContents(
			"""
				public class X {
				  void bar(Generic105756 g) {
				    g.foo();
				  }
				}"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		if (otherCopy != null)
			otherCopy.discardWorkingCopy();
	}
}
/*
 * Ensures that a reconcile participant is notified when a working copy is reconciled.
 */
public void testReconcileParticipant01() throws CoreException {
	ReconcileParticipant participant = new ReconcileParticipant();
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public void bar() {
			    System.out.println()
			  }
			}"""
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected participant delta",
		"""
			[Working copy] X.java[*]: {CHILDREN | FINE GRAINED}
				X[*]: {CHILDREN | FINE GRAINED}
					bar()[+]: {}
					foo()[-]: {}""",
		participant.delta
	);
}
/*
 * Ensures that a reconcile participant is not notified if not participating.
 */
public void testReconcileParticipant02() throws CoreException {
	ReconcileParticipant participant = new ReconcileParticipant(){
		@Override
		public boolean isActive(IJavaProject project) {
			return false;
		}
	};
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public void bar() {
			    System.out.println()
			  }
			}"""
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertDeltas(
		"Unexpected participant delta",
		"<null>",
		participant.delta
	);
}
/*
 * Ensures that a reconcile participant is notified with the correct AST.
 */
public void testReconcileParticipant03() throws CoreException {
	ReconcileParticipant participant = new ReconcileParticipant();
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public void bar() {
			    System.out.println()
			  }
			}"""
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertASTNodeEquals(
		"Unexpected participant ast",
		"""
			package p1;
			import p2.*;
			public class X {
			  public void bar(){
			  }
			}
			""",
		participant.ast
	);
}
/*
 * Ensures that the same AST as the one a reconcile participant requested is reported.
 */
public void testReconcileParticipant04() throws CoreException {
	ReconcileParticipant participant = new ReconcileParticipant();
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public void bar() {
			    System.out.println()
			  }
			}"""
	);
	org.eclipse.jdt.core.dom.CompilationUnit ast = this.workingCopy.reconcile(JLS_LATEST, false, null, null);
	assertSame(
		"Unexpected participant ast",
		participant.ast,
		ast
	);
}
/*
 * Ensures that a participant can fix an error during reconcile.
 */
public void testReconcileParticipant05() throws CoreException {
	new ReconcileParticipant() {
		/**
		 * @deprecated
		 */
		@Override
		public void reconcile(ReconcileContext context) {
			try {
				setWorkingCopyContents(
					"""
						package p1;
						public class X {
						  public void bar() {
						  }
						}"""
				);
				context.resetAST();
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
	};
	setWorkingCopyContents(
		"""
			package p1;
			public class X {
			  public void bar() {
			    toString()
			  }
			}"""
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertProblems(
		"Unexpected problems",
		"----------\n" +
		"----------\n"
	);
}
/*
 * Ensures that a participant can introduce an error during reconcile.
 */
public void testReconcileParticipant06() throws CoreException {
	new ReconcileParticipant() {
		/**
		 * @deprecated
		 */
		@Override
		public void reconcile(ReconcileContext context) {
			try {
				setWorkingCopyContents(
					"""
						package p1;
						public class X {
						  public void bar() {
						    toString()
						  }
						}"""
				);
				context.resetAST();
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
		}
	};
	setWorkingCopyContents(
		"""
			package p1;
			public class X {
			  public void bar() {
			  }
			}"""
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertProblems(
		"Unexpected problems",
		"""
			----------
			1. ERROR in /Reconciler/src/p1/X.java (at line 4)
				toString()
				         ^
			Syntax error, insert ";" to complete BlockStatements
			----------
			"""
	);
}
/*
 * Ensures that a reconcile participant is NOT notified when a working copy is reconciled
 * in a project with insufficient source level.
 * (regression test for bug 125291 Enable conditional loading of APT)
 */
public void testReconcileParticipant07() throws CoreException {
	IJavaProject project = this.workingCopy.getJavaProject();
	String originalSourceLevel = project.getOption(JavaCore.COMPILER_SOURCE, true);
	try {
		project.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_1);
		ReconcileParticipant participant = new ReconcileParticipant();
		setWorkingCopyContents(
			"""
				package p1;
				import p2.*;
				public class X {
				  public void bar() {
				    System.out.println()
				  }
				}"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertDeltas(
			"Unexpected participant delta",
			"<null>",
			participant.delta
		);
	} finally {
		project.setOption(JavaCore.COMPILER_SOURCE, originalSourceLevel);
	}
}
/*
 * Ensures that a problem reporting session is not started during reconcile if a participant reports an error
 * and if the working copy is already consistent and the forceProblemDetection flag is false.
 * (regression test for bug 154170 Printing warnings breaks in-editor quick fixes)
 */
public void testReconcileParticipant08() throws CoreException {
	// set working copy contents and ensure it is consistent
	String contents =
		"""
		package p1;
		public class X {
		  public void bar() {
		  }
		}""";
	setWorkingCopyContents(contents);
	this.workingCopy.makeConsistent(null);
	this.problemRequestor.initialize(contents.toCharArray());

	// reconcile with a participant adding a list of problems
	new ReconcileParticipant() {
		/**
		 * @deprecated
		 */
		@Override
		public void reconcile(ReconcileContext context) {
			context.putProblems("test.marker", new CategorizedProblem[] {});
		}
	};
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertProblems(
		"Unexpected problems",
		""
	);
}
/*
 * Ensures that a reconcile participant is not notified when a working copy is reconciled
 * and it was consistent and forcing problem detection is off
 * (regression test for 177319 Annotation Processing (APT) affects eclipse speed)
 */
public void testReconcileParticipant09() throws CoreException {
	this.workingCopy.makeConsistent(null);
	new ReconcileParticipant() {
		/**
		 * @deprecated
		 */
		@Override
		public void reconcile(ReconcileContext context) {
			assertTrue("Participant should not be notified of a reconcile", false);
		}
	};
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false/*don't force problem detection*/, null, null);
}

/*
 * Ensures that a reconcile participant is notified when a working copy is reconciled
 * and it was consistent and forcing problem detection is on
 */
public void testReconcileParticipant10() throws CoreException {
	this.workingCopy.makeConsistent(null);
	final boolean[] participantReconciled = new boolean[1];
	new ReconcileParticipant() {
		/**
		 * @deprecated
		 */
		@Override
		public void reconcile(ReconcileContext context) {
			participantReconciled[0] = true;
		}
	};
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, true/*force problem detection*/, null, null);
	assertTrue(
		"Participant should have been notified",
		participantReconciled[0]
	);
}

/*
 * Ensures that the delta is still correct if a participant resets the ast during reconcile
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=210310)
 */
public void testReconcileParticipant11() throws CoreException {
	new ReconcileParticipant() {
		/**
		 * @deprecated
		 */
		@Override
		public void reconcile(ReconcileContext context) {
			context.resetAST();
		}
	};
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  private void foo() {
			  }
			}"""
	);
	this.workingCopy.reconcile(JLS_LATEST, true/*force problem detection*/, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"X[*]: {CHILDREN | FINE GRAINED}\n" +
		"	foo()[*]: {MODIFIERS CHANGED}"
	);
}

/*
 * Ensures that a misbehaving reconcile participant doesn't interfere with a reconcile operation
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=248680 )
 */
public void testReconcileParticipant12() throws CoreException {
	try {
		TestCompilationParticipant.failToInstantiate = true;
		simulateExitRestart();
		this.workingCopy = getCompilationUnit("Reconciler/src/p1/X.java").getWorkingCopy(this.wcOwner, null);
		setWorkingCopyContents(
			"""
				package p1;
				import p2.*;
				public class X {
				  public void bar() {
				    System.out.println()
				  }
				}"""
		);
		startDeltas();
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertWorkingCopyDeltas(
			"Unexpected participant delta",
			"""
				X[*]: {CHILDREN | FINE GRAINED}
					bar()[+]: {}
					foo()[-]: {}"""
		);
	} finally {
		stopDeltas();
		TestCompilationParticipant.failToInstantiate = false;
	}
}

	/*
 * Ensures that errors are fixed if renaming the .classpath file causes the missing type to be visible.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=207890)
 */
public void testRenameClasspathFile() throws CoreException {
	ICompilationUnit copy = null;
	try {
		createJavaProject("P1");
		deleteFile("/P1/.classpath");
		createFile(
			"/P1/.classpath2",
			"""
				<?xml version="1.0" encoding="UTF-8"?>
				<classpath>
				    <classpathentry kind="src" path="src1"/>
					<classpathentry kind="var" path="JCL_LIB"/>
					<classpathentry kind="output" path="bin"/>
				</classpath>"""
		);
		createFolder("/P1/src1/p1");
		createFile(
			"/P1/src1/p1/X.java",
			"""
				package p1;
				public class X {
				}"""
		);
		createJavaProject("P2", new String[] {"src"}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "bin");
		createFolder("/P2/src/p2");
		copy = getWorkingCopy(
			"/P2/src/p2/Y.java",
			"""
				package p2;
				import p1.X;
				public class Y extends X {
				}""",
			true/*compute problems*/
		);
		moveFile("/P1/.classpath2", "/P1/.classpath");
		this.problemRequestor.reset();
		copy.reconcile(ICompilationUnit.NO_AST, true/*for pb detection*/, null/*default owner*/, null/*no progress*/);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		if (copy != null)
			copy.discardWorkingCopy();
		deleteProject("P1");
		deleteProject("P2");
	}
}

/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents, updating the structure of this reconciler's compilation
 * unit, and fires the Java element deltas for the structural changes
 * of a renaming a method; the original method deleted and the new method added structurally.
 */
public void testRenameMethod1() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public void bar() {
			  }
			}"""
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"""
			X[*]: {CHILDREN | FINE GRAINED}
				bar()[+]: {}
				foo()[-]: {}"""
	);
}
/**
 * Ensures that the reconciler reconciles the new contents with the current
 * contents,updating the structure of this reconciler's compilation
 * unit, and fires the Java element delta for the structural changes
 * of the addition of a portion of a new method.
 */
public void testRenameWithSyntaxError() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;
			public class X {
			  public void bar( {
			  }
			}"""
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"""
			X[*]: {CHILDREN | FINE GRAINED}
				bar()[+]: {}
				foo()[-]: {}"""
	);
	assertProblems(
		"Unexpected problems",
		"""
			----------
			1. ERROR in /Reconciler/src/p1/X.java (at line 4)
				public void bar( {
				               ^
			Syntax error, insert ")" to complete MethodDeclaration
			----------
			"""
	);
}
/*
 * Ensure that warning are suppressed by an @SuppressWarnings annotation.
 */
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=95056
public void testSuppressWarnings1() throws JavaModelException {
	ICompilationUnit otherCopy = null;
	try {
		otherCopy = getWorkingCopy(
			"/Reconciler15/src/X.java",
	        """
				@Deprecated
				public class X {
				   void foo(){}
				}
				""",
			this.wcOwner
		);
		setUp15WorkingCopy("/Reconciler15/src/Y.java", this.wcOwner);
		setWorkingCopyContents(
	        """
				public class Y extends X {
				  @SuppressWarnings("all")
				   void foo(){ super.foo(); }
				   Zork z;
				}
				"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. WARNING in /Reconciler15/src/Y.java (at line 1)
					public class Y extends X {
					                       ^
				The type X is deprecated
				----------
				2. ERROR in /Reconciler15/src/Y.java (at line 4)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
	} finally {
		if (otherCopy != null)
			otherCopy.discardWorkingCopy();
	}
}
/*
 * Ensure that warning are suppressed by an @SuppressWarning annotation.
 */
public void testSuppressWarnings2() throws JavaModelException {
	ICompilationUnit otherCopy = null;
	try {
		otherCopy = getWorkingCopy(
			"/Reconciler15/src/java/util/List.java",
			"""
				package java.util;
				public interface List<E> {
				}
				""",
			this.wcOwner
		);
		setUp15WorkingCopy("/Reconciler15/src/X.java", this.wcOwner);
		setWorkingCopyContents(
            """
				import java.util.List;
				
				public class X {
				    void foo(List list) {
				        List<String> ls1 = list;
				    }
				    @SuppressWarnings("unchecked")
				    void bar(List list) {
				        List<String> ls2 = list;
				    }
				   Zork z;
				}
				"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. WARNING in /Reconciler15/src/X.java (at line 5)
					List<String> ls1 = list;
					                   ^^^^
				Type safety: The expression of type List needs unchecked conversion to conform to List<String>
				----------
				2. ERROR in /Reconciler15/src/X.java (at line 11)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				"""
		);
	} finally {
		if (otherCopy != null)
			otherCopy.discardWorkingCopy();
	}
}
/*
 * Ensure that warning are suppressed by an @SuppressWarning annotation.
 */
public void testSuppressWarnings3() throws JavaModelException {
	ICompilationUnit otherCopy = null;
	try {
		otherCopy = getWorkingCopy(
			"/Reconciler15/src/java/util/HashMap.java",
			"""
				package java.util;
				public class HashMap implements Map {
				}
				""",
			this.wcOwner
		);
		setUp15WorkingCopy("/Reconciler15/src/X.java", this.wcOwner);
		setWorkingCopyContents(
			"""
				import java.util.*;
				@SuppressWarnings("unchecked")
				public class X {
					void foo() {
						Map<String, String>[] map = new HashMap[10];
					}
				   Zork z;
				}
				"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. ERROR in /Reconciler15/src/X.java (at line 7)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				"""
		);
	} finally {
		if (otherCopy != null)
			otherCopy.discardWorkingCopy();
	}
}
/*
 * Ensure that warning are suppressed by an @SuppressWarnings annotation.
 */
public void testSuppressWarnings4() throws JavaModelException {
	ICompilationUnit otherCopy = null;
	try {
		otherCopy = getWorkingCopy(
			"/Reconciler15/src/X.java",
	        """
				/** @deprecated */
				public class X {
				   void foo(){}
				}
				""",
			this.wcOwner
		);
		setUp15WorkingCopy("/Reconciler15/src/Y.java", this.wcOwner);
		setWorkingCopyContents(
	        """
				public class Y extends X {
				  @SuppressWarnings("all")
				   void foo(){ super.foo(); }
				   Zork z;
				}
				"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. WARNING in /Reconciler15/src/Y.java (at line 1)
					public class Y extends X {
					                       ^
				The type X is deprecated
				----------
				2. ERROR in /Reconciler15/src/Y.java (at line 4)
					Zork z;
					^^^^
				Zork cannot be resolved to a type
				----------
				""");
	} finally {
		if (otherCopy != null)
			otherCopy.discardWorkingCopy();
	}
}
/**
 * Ensure that an unhandled exception is detected.
 */
public void testUnhandledException() throws JavaModelException {
	setWorkingCopyContents(
		"""
			package p1;
			public class X {
			  public void foo() {
			    throw new Exception();
			  }
			}"""
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertProblems(
		"Unexpected problems",
		"""
			----------
			1. ERROR in /Reconciler/src/p1/X.java (at line 4)
				throw new Exception();
				^^^^^^^^^^^^^^^^^^^^^^
			Unhandled exception type Exception
			----------
			"""
	);
}
/**
 * Check that forcing a make consistent action is leading the next reconcile to not notice changes.
 */
public void testMakeConsistentFoolingReconciler() throws JavaModelException {
	setWorkingCopyContents("");
	this.workingCopy.makeConsistent(null);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Should have got NO delta",
		""
	);
}
/**
 * Test bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=60689
 * AST on reconcile: AST without Javadoc comments created
 * @deprecated using deprecated code
 */
public void testBug60689() throws JavaModelException {
	setWorkingCopyContents("""
		public class X {
			/**
			 * Returns the length of the string representing the number of\s
			 * indents in the given string <code>line</code>. Returns\s
			 * <code>-1</code> if the line isn't prefixed with an indent of
			 * the given number of indents.\s
			 */
			public static int computeIndentLength(String line, int numberOfIndents, int tabWidth) {
				return 0;
		}"""
	);
	org.eclipse.jdt.core.dom.CompilationUnit testCU = this.workingCopy.reconcile(AST.JLS2, true, null, null);
	assertNotNull("We should have a comment!", testCU.getCommentList());
	assertEquals("We should have 1 comment!", 1, testCU.getCommentList().size());
	testCU = this.workingCopy.reconcile(AST.JLS2, true, null, null);
	assertNotNull("We should have a comment!", testCU.getCommentList());
	assertEquals("We should have one comment!", 1, testCU.getCommentList().size());
}
/*
 * Ensures that a working copy in a 1.4 project that references a 1.5 project can be reconciled without error.
 * (regression test for bug 98434 A non-1.5 project with 1.5 projects in the classpath does not show methods with generics)
 */
public void testTwoProjectsWithDifferentCompliances() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	try {
		createJavaProject("P1", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
		createFolder("/P1/p");
		createFile(
			"/P1/p/X.java",
			"""
				package p;
				public class X {
				  void foo(Class<String> c) {
				  }
				}"""
		);

		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] {"/P1"}, "", "1.4");
		createFolder("/P2/p");
		this.workingCopy = getWorkingCopy("/P2/p/Y.java", "", this.wcOwner);
		setWorkingCopyContents(
			"""
				package p;
				public class Y {
				  void bar(Class c) {
				    new X().foo(c);
				  }
				}"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true/*force pb detection*/, this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		deleteProjects(new String[] {"P1", "P2"});
	}
}
/*
 * Ensures that a method that has a type parameter with bound can be overriden in another working copy.
 * (regression test for bug 76780 [model] return type not recognized correctly on some generic methods)
 */
public void testTypeParameterWithBound() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	ICompilationUnit workingCopy1 = null;
	try {
		workingCopy1 = getWorkingCopy(
			"/Reconciler15/src/test/I.java",
			"""
				package test;
				public interface I {
					<T extends I> void foo(T t);
				}
				""",
			this.wcOwner
		);

		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getWorkingCopy("Reconciler15/src/test/X.java", "", this.wcOwner);
		setWorkingCopyContents(
			"""
				package test;
				public class X implements I {
					public <T extends I> void foo(T t) {
					}
				}
				"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, this.wcOwner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
	}
}
/*
 * Ensures that a method that has a type parameter starting with $ can be reconciled against.
 * (regression test for bug 91709 [1.5][model] Quick Fix Error but no Problem Reported)
 */
public void testTypeParameterStartingWithDollar() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	ICompilationUnit workingCopy1 = null;
	try {
		workingCopy1 = getWorkingCopy(
			"/Reconciler15/src/test/Y.java",
			"""
				package test;
				public class Y<$T> {
					void foo($T t);
				}
				""",
			this.wcOwner
		);

		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getWorkingCopy("Reconciler15/src/test/X.java", "", this.wcOwner);
		setWorkingCopyContents(
			"""
				package test;
				public class X {
					public void bar() {
				    new Y<String>().foo("");
					}
				}
				"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, this.wcOwner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
	}
}
/*
 * Ensures that a working copy with a type with a dollar name can be reconciled without errors.
 * (regression test for bug 117121 Can't create class called A$B in eclipse)
 */
public void testTypeWithDollarName() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	try {
		String contents =
			"""
			package p1;
			public class Y$Z {
			}""";
		createFile(
			"/Reconciler/src/p1/Y$Z.java",
			contents
		);
		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getCompilationUnit("Reconciler/src/p1/Y$Z.java").getWorkingCopy(this.wcOwner, null);

		this.problemRequestor.initialize(contents.toCharArray());
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true, null, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		deleteFile("/Reconciler/src/p1/Y$Z.java");
	}
}
/*
 * Ensures that a working copy with a type with a dollar name can be reconciled against without errors.
 * (regression test for bug 125301 Handling of classes with $ in class name.)
 */
public void testTypeWithDollarName2() throws CoreException {
	ICompilationUnit workingCopy2 = null;
	try {
		WorkingCopyOwner owner = this.workingCopy.getOwner();
		workingCopy2 = getWorkingCopy(
			"/Reconciler/src/p1/Y$Z.java",
			"""
				package p1;
				public class Y$Z {
				}""",
			owner
		);
		setWorkingCopyContents(
			"""
				package p1;
				public class X {
				  Y$Z field;
				}"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, owner, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		if (workingCopy2 != null)
			workingCopy2.discardWorkingCopy();
	}
}
/*
 * Ensures that a working copy with a type with a dollar name can be reconciled against without errors.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=232803 )
 */
public void testTypeWithDollarName3() throws CoreException {
	ICompilationUnit workingCopy2 = null;
	try {
		WorkingCopyOwner owner = this.workingCopy.getOwner();
		workingCopy2 = getWorkingCopy(
			"/Reconciler/src/p1/Cl$ss.java",
			"""
				package p1;
				public interface Cl$ss {
				        public void test(Cl$ss c);
				        public void foo();
				}""",
			owner
		);
		setWorkingCopyContents(
			"""
				package p1;
				public class X {
				        void m(Cl$ss c2) {
				                c2.test(c2);
				                c2.foo();
				        }
				}"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, owner, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		if (workingCopy2 != null)
			workingCopy2.discardWorkingCopy();
	}
}
/*
 * Ensures that a varargs method can be referenced from another working copy.
 */
public void testVarargs() throws CoreException {
	this.workingCopy.discardWorkingCopy(); // don't use the one created in setUp()
	this.workingCopy = null;
	ICompilationUnit workingCopy1 = null;
	try {
		workingCopy1 = getWorkingCopy(
			"/Reconciler15/src/test/X.java",
			"""
				package test;
				public class X {
					void bar(String ... args) {}
				}
				""",
			this.wcOwner
		);

		this.problemRequestor =  new ProblemRequestor();
		this.workingCopy = getWorkingCopy("Reconciler15/src/test/Y.java", "", this.wcOwner);
		setWorkingCopyContents(
			"""
				package test;
				public class Y {
					void foo(){
						X someX = new X();
						someX.bar("a", "b");
					}
				}
				"""
		);
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, this.wcOwner, null);

		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		if (workingCopy1 != null) {
			workingCopy1.discardWorkingCopy();
		}
	}
}

/**
 * Bug 114338:[javadoc] Reconciler reports wrong javadoc warning (missing return type)
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=114338"
 */
public void testBug114338() throws CoreException {
	// Set initial CU content
	setWorkingCopyContents(
		"""
			package p1;
			public class X {
				/**
				 * @return a
				 */
				boolean get() {
					return false;
				}
			}""");
	this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
	assertProblems(
		"Unexpected problems",
		"----------\n" +
		"----------\n"
	);

	// Modify content
	String contents =
		"""
		package p1;
		public class X {
			/**
			 * @return boolean
			 */
			boolean get() {
				return false;
			}
		}""";
	setWorkingCopyContents(contents);
	this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
	assertProblems(
		"Unexpected problems",
		"----------\n" +
		"----------\n"
	);
}

/**
 * Bug 36032:[plan] JavaProject.findType() fails to find second type in source file
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=36032"
 */
public void testBug36032a() throws CoreException, InterruptedException {
	try {
		// Resources creation
		createJavaProject("P", new String[] {""}, new String[] {"JCL_LIB"}, "bin");
		String source =
			"""
			public class Test {
				public static void main(String[] args) {
					new SFoo().foo();
				}
			}
			""";
		this.createFile(
			"/P/Foo.java",
			"class SFoo { void foo() {} }\n"
		);
		this.createFile(
			"/P/Test.java",
			source
		);

		// Get compilation unit and reconcile it
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopy = getCompilationUnit("/P/Test.java").getWorkingCopy(this.wcOwner, null);
		this.workingCopy.getBuffer().setContents(source);
		this.workingCopy.reconcile(JLS_LATEST, true, null, null);
		assertNoProblem(sourceChars, this.workingCopy);

		// Add new secondary type
		this.createFile(
			"/P/Bar.java",
			"class SBar{ void bar() {} }\n"
		);
		source =
			"""
				public class Test {
					public static void main(String[] args) {
						new SFoo().foo();
						new SBar().bar();
					}
				}
				""";

		// Reconcile with modified source
		sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopy.getBuffer().setContents(source);
		this.workingCopy.reconcile(JLS_LATEST, true, null, null);
		assertNoProblem(sourceChars, this.workingCopy);
	} finally {
		deleteProject("P");
	}
}
public void testBug36032b() throws CoreException, InterruptedException {
	try {
		// Resources creation
		createJavaProject("P", new String[] {""}, new String[] {"JCL_LIB"}, "bin");
		String source =
			"""
			public class Test {
				public static void main(String[] args) {
					new SFoo().foo();
					new SBar().bar();
				}
			}
			""";
		createFile(
			"/P/Foo.java",
			"class SFoo { void foo() {} }\n"
		);
		createFile(
			"/P/Test.java",
			source
		);
		createFile(
			"/P/Bar.java",
			"class SBar{ void bar() {} }\n"
		);

		// Get compilation unit and reconcile it
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopy = getCompilationUnit("/P/Test.java").getWorkingCopy(this.wcOwner, null);
		this.workingCopy.getBuffer().setContents(source);
		this.workingCopy.reconcile(JLS_LATEST, true, null, null);
		assertNoProblem(sourceChars, this.workingCopy);

		// Delete secondary type => should get a problem
		waitUntilIndexesReady();
		deleteFile("/P/Bar.java");
		this.problemRequestor.initialize(source.toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, true, null, null);
		assertEquals("Working copy should not find secondary type 'Bar'!", 1, this.problemRequestor.problemCount);
		assertProblems("Working copy should have problem!",
			"""
				----------
				1. ERROR in /P/Test.java (at line 4)
					new SBar().bar();
					    ^^^^
				SBar cannot be resolved to a type
				----------
				"""
		);

		// Fix the problem
		source =
			"""
				public class Test {
					public static void main(String[] args) {
						new SFoo().foo();
					}
				}
				""";
		sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopy.getBuffer().setContents(source);
		this.workingCopy.reconcile(JLS_LATEST, true, null, null);
		assertNoProblem(sourceChars, this.workingCopy);
	} finally {
		deleteProject("P");
	}
}
// Secondary types used through multiple projects
public void testBug36032c() throws CoreException, InterruptedException {
	try {
		// Create first project
		createJavaProject("P1", new String[] {""}, new String[] {"JCL_LIB"}, "bin");
		createFolder("/P1/test");
		createFile(
			"/P1/test/Foo.java",
			"package test;\n" +
			"class Secondary{ void foo() {} }\n"
		);
		createFile(
			"/P1/test/Test1.java",
			"""
				package test;
				public class Test1 {
					public static void main(String[] args) {
						new Secondary().foo();
					}
				}
				"""
		);

		// Create second project
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] { "/P1" }, "bin");
		String source =
			"""
			package test;
			public class Test2 {
				public static void main(String[] args) {
					new Secondary().foo();
				}
			}
			""";
		createFolder("/P2/test");
		createFile(
			"/P2/test/Test2.java",
			source
		);

		// Get compilation unit and reconcile it => expect no error
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopy = getCompilationUnit("/P2/test/Test2.java").getWorkingCopy(this.wcOwner, null);
		this.workingCopy.getBuffer().setContents(source);
		this.workingCopy.reconcile(JLS_LATEST, true, null, null);
		assertNoProblem(sourceChars, this.workingCopy);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
/**
 * Bug 118823: [model] Secondary types cache not reset while removing _all_ secondary types from CU
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=118823"
 */
public void testBug118823() throws CoreException, InterruptedException, IOException {
	try {
		// Resources creation
		createJavaProject("P1", new String[] {""}, new String[] {"JCL_LIB"}, "bin");
		String source = "class Test {}\n";
		createFile(
			"/P1/Test.java",
			source
		);
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] { "/P1" }, "bin");
		String source2 =
			"""
			class A {
				Secondary s;
			}
			""";
		createFile(
			"/P2/A.java",
			source2
		);
		waitUntilIndexesReady();
		this.workingCopies = new ICompilationUnit[2];

		// Get first working copy and verify that there's no error
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/P1/Test.java").getWorkingCopy(this.wcOwner, null);
		assertNoProblem(sourceChars, this.workingCopies[0]);

		// Get second working copy and verify that there's one error (missing secondary type)
		this.problemRequestor.initialize(source2.toCharArray());
		this.workingCopies[1] = getCompilationUnit("/P2/A.java").getWorkingCopy(this.wcOwner, null);
		assertEquals("Working copy should not find secondary type 'Secondary'!", 1, this.problemRequestor.problemCount);
		assertProblems("Working copy should have problem!",
			"""
				----------
				1. ERROR in /P2/A.java (at line 2)
					Secondary s;
					^^^^^^^^^
				Secondary cannot be resolved to a type
				----------
				"""
		);

		// Delete file and recreate it with secondary
		final String source1 =
			"public class Test {}\n" +
			"class Secondary{}\n";
		getWorkspace().run(
			new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					deleteFile("/P1/Test.java");
					createFile(
						"/P1/Test.java",
						source1
					);
				}
			},
			null
		);

		// Get first working copy and verify that there's still no error
		sourceChars = source1.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0].getBuffer().setContents(source1);
		this.workingCopies[0].reconcile(JLS_LATEST,
			true, // force problem detection to see errors if any
			null,	// do not use working copy owner to not use working copies in name lookup
			null);
		assertNoProblem(sourceChars, this.workingCopies[0]);

		// Get second working copy and verify that there's any longer error
		sourceChars = source2.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[1].getBuffer().setContents(source2);
		this.workingCopies[1].reconcile(JLS_LATEST,
			true, // force problem detection to see errors if any
			null,	// do not use working copy owner to not use working copies in name lookup
			null);
		assertNoProblem(sourceChars, this.workingCopies[1]);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
public void testBug118823b() throws CoreException, InterruptedException {
	try {
		// Resources creation
		createJavaProject("P1", new String[] {""}, new String[] {"JCL_LIB"}, "bin");
		String source1 = "class Test {}\n";
		createFile(
			"/P1/Test.java",
			source1
		);
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] { "/P1" }, "bin");
		String source2 =
			"""
			class A {
				Secondary s;
			}
			""";
		createFile(
			"/P2/A.java",
			source2
		);
		waitUntilIndexesReady();
		this.workingCopies = new ICompilationUnit[2];

		// Get first working copy and verify that there's no error
		char[] sourceChars = source1.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/P1/Test.java").getWorkingCopy(this.wcOwner, null);
		assertNoProblem(sourceChars, this.workingCopies[0]);

		// Get second working copy and verify that there's one error (missing secondary type)
		this.problemRequestor.initialize(source2.toCharArray());
		this.workingCopies[1] = getCompilationUnit("/P2/A.java").getWorkingCopy(this.wcOwner, null);
		assertEquals("Working copy should not find secondary type 'Secondary'!", 1, this.problemRequestor.problemCount);
		assertProblems("Working copy should have problem!",
			"""
				----------
				1. ERROR in /P2/A.java (at line 2)
					Secondary s;
					^^^^^^^^^
				Secondary cannot be resolved to a type
				----------
				"""
		);

		// Modify first working copy and verify that there's still no error
		source1 =
			"public class Test {}\n" +
			"class Secondary{}\n";
		sourceChars = source1.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0].getBuffer().setContents(source1);
		this.workingCopies[0].reconcile(JLS_LATEST,
			true, // force problem detection to see errors if any
			null,	// do not use working copy owner to not use working copies in name lookup
			null);
		this.workingCopies[0].commitWorkingCopy(true, null);
		assertNoProblem(sourceChars, this.workingCopies[0]);

		// Get second working copy and verify that there's any longer error
		sourceChars = source2.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[1].getBuffer().setContents(source2);
		this.workingCopies[1].reconcile(JLS_LATEST,
			true, // force problem detection to see errors if any
			null,	// do not use working copy owner to not use working copies in name lookup
			null);
		assertNoProblem(sourceChars, this.workingCopies[1]);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}
public void testBug118823c() throws CoreException, InterruptedException {
	try {
		// Resources creation
		createJavaProject("P1", new String[] {""}, new String[] {"JCL_LIB"}, "bin");
		String source1 = "class Test {}\n";
		createFile(
			"/P1/Test.java",
			source1
		);
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] { "/P1" }, "bin");
		String source2 =
			"""
			class A {
				Secondary s;
			}
			""";
		createFile(
			"/P2/A.java",
			source2
		);
		waitUntilIndexesReady();
		this.workingCopies = new ICompilationUnit[2];

		// Get first working copy and verify that there's no error
		char[] sourceChars = source1.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/P1/Test.java").getWorkingCopy(this.wcOwner, null);
		assertNoProblem(sourceChars, this.workingCopies[0]);

		// Get second working copy and verify that there's one error (missing secondary type)
		this.problemRequestor.initialize(source2.toCharArray());
		this.workingCopies[1] = getCompilationUnit("/P2/A.java").getWorkingCopy(this.wcOwner, null);
		assertEquals("Working copy should not find secondary type 'Secondary'!", 1, this.problemRequestor.problemCount);
		assertProblems("Working copy should have problem!",
			"""
				----------
				1. ERROR in /P2/A.java (at line 2)
					Secondary s;
					^^^^^^^^^
				Secondary cannot be resolved to a type
				----------
				"""
		);

		// Delete file and recreate it with secondary
		deleteFile("/P1/Test.java");
		source1 =
			"public class Test {}\n" +
			"class Secondary{}\n";
		createFile(
			"/P1/Test.java",
			source1
		);

		// Get first working copy and verify that there's still no error
		sourceChars = source1.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0].getBuffer().setContents(source1);
		this.workingCopies[0].reconcile(JLS_LATEST,
			true, // force problem detection to see errors if any
			null,	// do not use working copy owner to not use working copies in name lookup
			null);
		this.workingCopies[0].commitWorkingCopy(true, null);
		assertNoProblem(sourceChars, this.workingCopies[0]);

		// Get second working copy and verify that there's any longer error
		sourceChars = source2.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[1].getBuffer().setContents(source2);
		this.workingCopies[1].reconcile(JLS_LATEST,
			true, // force problem detection to see errors if any
			null,	// do not use working copy owner to not use working copies in name lookup
			null);
		assertNoProblem(sourceChars, this.workingCopies[1]);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=107931
// won't be fixed; this test watches the current behavior in case we change
// our mind
public void test1001() throws CoreException, InterruptedException, IOException {
	try {
		// Resources creation
		String sources[] = new String[3];
		char[] sourcesAsCharArrays[] = new char[3][];
		createJavaProject("P1", new String[] {""}, new String[] {"JCL_LIB"}, "bin");
		sources[0] = "class X {}\n";
		createFile(
			"/P1/X.java",
			sources[0]
		);
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] { "/P1" }, "bin");
		sources[1] =
			"""
				interface I {
				  void foo();
				  void bar(X p);
				}
				""";
		createFile(
			"/P2/I.java",
			sources[1]
		);
		createJavaProject("P3", new String[] {""}, new String[] {"JCL_LIB"}, new String[] { "/P2" }, "bin");
		sources[2] =
			"""
				class Y implements I {
				  // public void foo() { }
				  // public void bar(X p) { }
				}
				""";
		createFile(
			"/P3/Y.java",
			sources[2]
		);
		for (int i = 0 ; i < sources.length ; i++) {
			sourcesAsCharArrays[i] = sources[i].toCharArray();
		}
		waitUntilIndexesReady();
		this.workingCopies = new ICompilationUnit[3];

		// Get first working copy and verify that there's no error
		this.problemRequestor.initialize(sourcesAsCharArrays[0]);
		this.workingCopies[0] = getCompilationUnit("/P1/X.java").getWorkingCopy(this.wcOwner, null);
		assertNoProblem(sourcesAsCharArrays[0], this.workingCopies[0]);

		// Get second working copy and verify that there's no error
		this.problemRequestor.initialize(sourcesAsCharArrays[1]);
		this.workingCopies[1] = getCompilationUnit("/P2/I.java").getWorkingCopy(this.wcOwner, null);
		assertNoProblem(sourcesAsCharArrays[1], this.workingCopies[1]);

		// Get third working copy and verify that all expected errors are here
		this.problemRequestor.initialize(sourcesAsCharArrays[2]);
		this.workingCopies[2] = getCompilationUnit("/P3/Y.java").getWorkingCopy(this.wcOwner, null);
		assertProblems("Working copy should have problems:",
				"""
					----------
					1. ERROR in /P3/Y.java (at line 1)
						class Y implements I {
						      ^
					The type Y must implement the inherited abstract method I.bar(X)
					----------
					2. ERROR in /P3/Y.java (at line 1)
						class Y implements I {
						      ^
					The type Y must implement the inherited abstract method I.foo()
					----------
					""");
	} finally {
		deleteProject("P1");
		deleteProject("P2");
		deleteProject("P3");
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=107931
// variant: having all needed projects on the classpath solves the issue
public void test1002() throws CoreException, InterruptedException, IOException {
	try {
		// Resources creation
		String sources[] = new String[3];
		char[] sourcesAsCharArrays[] = new char[3][];
		createJavaProject("P1", new String[] {""}, new String[] {"JCL_LIB"}, "bin");
		sources[0] = "class X {}\n";
		createFile(
			"/P1/X.java",
			sources[0]
		);
		createJavaProject("P2", new String[] {""}, new String[] {"JCL_LIB"}, new String[] { "/P1" }, "bin");
		sources[1] =
			"""
				interface I {
				  void foo();
				  void bar(X p);
				}
				""";
		createFile(
			"/P2/I.java",
			sources[1]
		);
		createJavaProject("P3", new String[] {""}, new String[] {"JCL_LIB"}, new String[] { "/P1" /* compare with test1001 */, "/P2" }, "bin");
		sources[2] =
			"""
				class Y implements I {
				  // public void foo() { }
				  // public void bar(X p) { }
				}
				""";
		createFile(
			"/P3/Y.java",
			sources[2]
		);
		for (int i = 0 ; i < sources.length ; i++) {
			sourcesAsCharArrays[i] = sources[i].toCharArray();
		}
		waitUntilIndexesReady();
		this.workingCopies = new ICompilationUnit[3];

		// Get first working copy and verify that there's no error
		this.problemRequestor.initialize(sourcesAsCharArrays[0]);
		this.workingCopies[0] = getCompilationUnit("/P1/X.java").getWorkingCopy(this.wcOwner, null);
		assertNoProblem(sourcesAsCharArrays[0], this.workingCopies[0]);

		// Get second working copy and verify that there's no error
		this.problemRequestor.initialize(sourcesAsCharArrays[1]);
		this.workingCopies[1] = getCompilationUnit("/P2/I.java").getWorkingCopy(this.wcOwner, null);
		assertNoProblem(sourcesAsCharArrays[1], this.workingCopies[1]);

		// Get third working copy and verify that all expected errors are here
		this.problemRequestor.initialize(sourcesAsCharArrays[2]);
		this.workingCopies[2] = getCompilationUnit("/P3/Y.java").getWorkingCopy(this.wcOwner, null);
		assertProblems("Working copy should have problems:",
			"""
				----------
				1. ERROR in /P3/Y.java (at line 1)
					class Y implements I {
					      ^
				The type Y must implement the inherited abstract method I.bar(X)
				----------
				2. ERROR in /P3/Y.java (at line 1)
					class Y implements I {
					      ^
				The type Y must implement the inherited abstract method I.foo()
				----------
				"""
		);
	} finally {
		deleteProject("P1");
		deleteProject("P2");
		deleteProject("P3");
	}
}
/*
 * Ensure that fallthrough diagnostics are silenced by $FALL-THROUGH$ comments
 */
public void testFallthroughDiagnosis() throws CoreException, InterruptedException {
	try {
		// Resources creation
		IJavaProject p1 = createJavaProject("P1", new String[] {""}, new String[] {"JCL_LIB"}, "bin");
		p1.setOption(JavaCore.COMPILER_PB_FALLTHROUGH_CASE, JavaCore.ERROR);
		String source =
			"""
			public class X {
				void foo(int i) {
					switch(i) {
					case 0:
						i ++;
						// $FALL-THROUGH$
					case 1:
						i++;
						/* $FALL-THROUGH$ */
					case 2:
						i++;
					case 3:
					}
				}
			}
			""";

		createFile("/P1/X.java", source);
		this.workingCopies = new ICompilationUnit[1];

		// Get first working copy and verify that there's no error
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/P1/X.java").getWorkingCopy(this.wcOwner, null);
		assertProblems("Working copy should have problems:",
				"""
					----------
					1. ERROR in /P1/X.java (at line 12)
						case 3:
						^^^^^^
					Switch case may be entered by falling through previous case. If intended, add a new comment //$FALL-THROUGH$ on the line above
					----------
					"""
		);
	} finally {
		deleteProject("P1");
	}
}
/*
 * Ensure that the option ICompilationUnit.IGNORE_METHOD_BODIES is honored
 */
public void testIgnoreMethodBodies1() throws CoreException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;\
			public class X {
			  public int foo() {
			    int i = 0;
			  }
			}""");
	org.eclipse.jdt.core.dom.CompilationUnit ast = this.workingCopy.reconcile(JLS_LATEST, ICompilationUnit.IGNORE_METHOD_BODIES, null, null);
	// X.foo() not returning any value should not be reported
	assertProblems("Working copy should have problems:",
			"""
				----------
				1. WARNING in /Reconciler/src/p1/X.java (at line 2)
					import p2.*;public class X {
					       ^^
				The import p2 is never used
				----------
				"""
		);
	// statement declaring i should not be in the AST
	assertASTNodeEquals(
			"Unexpected participant ast",
			"""
				package p1;
				import p2.*;
				public class X {
				  public int foo(){
				  }
				}
				""",
			ast
		);
}
public void testIgnoreMethodBodies2() throws CoreException {
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;\
			public class X {
			  public void foo() {
			    int i = 0;
			  }
			  public int bar() {
			    int i = 0;
			    new X() /*start*/{
			    }/*end*/;\
			  }
			}""");
	org.eclipse.jdt.core.dom.CompilationUnit ast = this.workingCopy.reconcile(JLS_LATEST, ICompilationUnit.IGNORE_METHOD_BODIES, null, null);
	// methods with anonymous classes should have their statements intact
	assertASTNodeEquals(
			"Unexpected ast",
			"""
				package p1;
				import p2.*;
				public class X {
				  public void foo(){
				  }
				  public int bar(){
				  }
				}
				""",
			ast
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130000
public void testIgnoreMethodBodies3() throws CoreException {
	new ReconcileParticipant2();
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;\
			public class X {
			  public void foo() {
			    int i = 0;
			  }
			  public int bar() {
			    int i = 0;
			    new X() {
			    };\
			  }
			}""");
	org.eclipse.jdt.core.dom.CompilationUnit ast = this.workingCopy.reconcile(
			JLS_LATEST,
			ICompilationUnit.IGNORE_METHOD_BODIES | ICompilationUnit.ENABLE_STATEMENTS_RECOVERY,
			null,
			null);
	// methods with anonymous classes should have their statements intact
	assertASTNodeEquals(
			"Unexpected ast",
			"""
				package p1;
				import p2.*;
				public class X {
				  public void foo(){
				  }
				  public int bar(){
				  }
				}
				""",
			ast
		);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=130000
public void testIgnoreMethodBodies4() throws CoreException {
	new ReconcileParticipant3();
	setWorkingCopyContents(
		"""
			package p1;
			import p2.*;\
			public class X {
			  public void foo() {
			    int i = 0;
			  }
			  public int bar() {
			    int i = 0;
			    new X() {
			    };\
			  }
			}""");
	org.eclipse.jdt.core.dom.CompilationUnit ast = this.workingCopy.reconcile(
			JLS_LATEST,
			ICompilationUnit.IGNORE_METHOD_BODIES,
			null,
			null);
	// methods with anonymous classes should have their statements intact
	assertASTNodeEquals(
			"Unexpected ast",
			"""
				package p1;
				import p2.*;
				public class X {
				  public void foo(){
				  }
				  public int bar(){
				  }
				}
				""",
			ast
		);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=305259
public void testGenericAPIUsageFromA14Project() throws CoreException {
	IJavaProject project14 = null;
	IJavaProject project15 = null;
	try {
		project15 = createJavaProject("Reconciler15API", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
		createFolder("/Reconciler15API/src/p2");
		createFile(
			"/Reconciler15API/src/p2/BundleContext.java",
			"""
				package p2;
				public class BundleContext {
				  public <S> S getService(S s) {
				      return null;
				  }
				}"""
		);
		project15.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);

		project14 = createJavaProject("Reconciler1415", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
		project14.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);

		IClasspathEntry[] oldClasspath = project14.getRawClasspath();
		int oldLength = oldClasspath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[oldLength+1];
		System.arraycopy(oldClasspath, 0, newClasspath, 0, oldLength);
		newClasspath[oldLength] = JavaCore.newProjectEntry(new Path("/Reconciler15API"));
		project14.setRawClasspath(newClasspath, null);

		createFolder("/Reconciler1415/src/p1");
		String source =
			"""
			package p1;
			import p2.BundleContext;
			public class X {
			  public static void main(BundleContext context, String string) {
			  String s = (String) context.getService(string);\s
			  }
			}""";

		createFile(
			"/Reconciler1415/src/p1/X.java",
			source
		);

		this.workingCopies = new ICompilationUnit[1];
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/Reconciler1415/src/p1/X.java").getWorkingCopy(this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. WARNING in /Reconciler1415/src/p1/X.java (at line 5)
					String s = (String) context.getService(string);\s
					       ^
				The value of the local variable s is not used
				----------
				"""
		);
	} finally {
		if (project14 != null)
			deleteProject(project14);
		if (project15 != null)
			deleteProject(project15);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=305259 (same as above, but with a JSR14 target)
public void testGenericAPIUsageFromA14Project2() throws CoreException {
	IJavaProject project14 = null;
	IJavaProject project15 = null;
	try {
		project15 = createJavaProject("Reconciler15API", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
		createFolder("/Reconciler15API/src/p2");
		createFile(
			"/Reconciler15API/src/p2/BundleContext.java",
			"""
				package p2;
				public class BundleContext {
				  public <S> S getService(S s) {
				      return null;
				  }
				}"""
		);
		project15.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);

		project14 = createJavaProject("Reconciler1415", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
		project14.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);

		IClasspathEntry[] oldClasspath = project14.getRawClasspath();
		int oldLength = oldClasspath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[oldLength+1];
		System.arraycopy(oldClasspath, 0, newClasspath, 0, oldLength);
		newClasspath[oldLength] = JavaCore.newProjectEntry(new Path("/Reconciler15API"));
		project14.setRawClasspath(newClasspath, null);

		createFolder("/Reconciler1415/src/p1");
		String source =
			"""
			package p1;
			import p2.BundleContext;
			public class X {
			  public static void main(BundleContext context, String string) {
			  String s = (String) context.getService(string);\s
			  }
			}""";

		createFile(
			"/Reconciler1415/src/p1/X.java",
			source
		);

		this.workingCopies = new ICompilationUnit[1];
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/Reconciler1415/src/p1/X.java").getWorkingCopy(this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. WARNING in /Reconciler1415/src/p1/X.java (at line 5)
					String s = (String) context.getService(string);\s
					       ^
				The value of the local variable s is not used
				----------
				"""
		);
	} finally {
		if (project14 != null)
			deleteProject(project14);
		if (project15 != null)
			deleteProject(project15);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=323633
public void testGenericAPIUsageFromA14Project3() throws CoreException {
	IJavaProject project14 = null;
	IJavaProject project15 = null;
	try {
		project15 = createJavaProject("Reconciler15API", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin");
		createFolder("/Reconciler15API/src/p2");
		createFile(
			"/Reconciler15API/src/p2/X.java",
			"""
				package p2;
				import java.util.Collection;
				import java.util.Iterator;
				public class X<E> implements Collection<E>{
				   public static X getX() {
				        	return new X();
					}
					public int size() {
						return 0;
					}
					public boolean isEmpty() {
						return false;
					}
					public boolean contains(Object o) {
						return false;
					}
					public Iterator<E> iterator() {
						return null;
					}
					public Object[] toArray() {
						return null;
					}
					public <T> T[] toArray(T[] a) {
						return null;
					}
					public boolean add(E e) {
						return false;
					}
					public boolean remove(Object o) {
						return false;
					}
					public boolean containsAll(Collection<?> c) {
						return false;
					}
					public boolean addAll(Collection<? extends E> c) {
						return false;
					}
					public boolean removeAll(Collection<?> c) {
						return false;
					}
					public boolean retainAll(Collection<?> c) {
						return false;
					}
					public void clear() {
					}
				}
				"""
		);
		project15.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);

		project14 = createJavaProject("Reconciler1415", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin");
		project14.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);

		IClasspathEntry[] oldClasspath = project14.getRawClasspath();
		int oldLength = oldClasspath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[oldLength+1];
		System.arraycopy(oldClasspath, 0, newClasspath, 0, oldLength);
		newClasspath[oldLength] = JavaCore.newProjectEntry(new Path("/Reconciler15API"));
		project14.setRawClasspath(newClasspath, null);

		createFolder("/Reconciler1415/src/p1");
		String source =
			"""
			package p1;
			import java.util.Collection;
			public class X {
			  public static void main(String string) {
			  Collection c = p2.X.getX();\s
			  }
			}""";

		createFile(
			"/Reconciler1415/src/p1/X.java",
			source
		);

		this.workingCopies = new ICompilationUnit[1];
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/Reconciler1415/src/p1/X.java").getWorkingCopy(this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. WARNING in /Reconciler1415/src/p1/X.java (at line 5)
					Collection c = p2.X.getX();\s
					           ^
				The value of the local variable c is not used
				----------
				"""
		);
	} finally {
		if (project14 != null)
			deleteProject(project14);
		if (project15 != null)
			deleteProject(project15);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=323633 (variation: 15 uses 14)
public void testGenericAPIUsageFromA14Project4() throws CoreException {
	IJavaProject project14 = null;
	IJavaProject project15 = null;
	try {
		project14 = createJavaProject("Reconciler1415", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin");
		createFolder("/Reconciler1415/src/p1");
		String source =
			"""
			package p1;
			import java.lang.Comparable;
			public class X implements Comparable {
			  public static X getX() {
			      return new X();
			  }
			}""";

		createFile(
			"/Reconciler1415/src/p1/X.java",
			source
		);

		project14.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);

		project15 = createJavaProject("Reconciler15API", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin");
		createFolder("/Reconciler15API/src/p2");
		String otherSource = """
			package p2;
			public class X {\s
			 private p1.X x = p1.X.getX();
			 Comparable<String> y = null;
			}
			""";

		createFile(
			"/Reconciler15API/src/p2/X.java",
			otherSource
		);
		project15.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);

		IClasspathEntry[] oldClasspath = project15.getRawClasspath();
		int oldLength = oldClasspath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[oldLength+1];
		System.arraycopy(oldClasspath, 0, newClasspath, 0, oldLength);
		newClasspath[oldLength] = JavaCore.newProjectEntry(new Path("/Reconciler1415"));
		project15.setRawClasspath(newClasspath, null);

		this.workingCopies = new ICompilationUnit[1];
		char[] sourceChars = otherSource.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/Reconciler15API/src/p2/X.java").getWorkingCopy(this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. WARNING in /Reconciler15API/src/p2/X.java (at line 3)
					private p1.X x = p1.X.getX();
					             ^
				The value of the field X.x is not used
				----------
				"""
		);
	} finally {
		if (project14 != null)
			deleteProject(project14);
		if (project15 != null)
			deleteProject(project15);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=325633
public void testGenericAPIUsageFromA14Project5() throws CoreException {
	IJavaProject project14 = null;
	IJavaProject project15 = null;
	try {
		project15 = createJavaProject("Reconciler15API", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin");
		createFolder("/Reconciler15API/src/p2");
		createFile(
				"/Reconciler15API/src/p2/List.java",
				"""
					package p2;
					public  class List<T> {}
					  public static List<String> [] getArray() {
					      return null;
					  }
					  public static List<String> [] getBackArray(List<String>[] p) {
					      return p;
					  }
					}"""
			);
		project15.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);

		project14 = createJavaProject("Reconciler1415", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
		project14.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);

		IClasspathEntry[] oldClasspath = project14.getRawClasspath();
		int oldLength = oldClasspath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[oldLength+1];
		System.arraycopy(oldClasspath, 0, newClasspath, 0, oldLength);
		newClasspath[oldLength] = JavaCore.newProjectEntry(new Path("/Reconciler15API"));
		project14.setRawClasspath(newClasspath, null);

		createFolder("/Reconciler1415/src/p1");
		String source =
			"""
			package p1;
			import p2.List;
			public class X {
			  private List [] l = List.getArray();
			  private List [] l2 = List.getBackArray(l);
			}""";

		createFile(
			"/Reconciler1415/src/p1/X.java",
			source
		);

		this.workingCopies = new ICompilationUnit[1];
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/Reconciler1415/src/p1/X.java").getWorkingCopy(this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. WARNING in /Reconciler1415/src/p1/X.java (at line 5)
					private List [] l2 = List.getBackArray(l);
					                ^^
				The value of the field X.l2 is not used
				----------
				"""
		);
	} finally {
		if (project14 != null)
			deleteProject(project14);
		if (project15 != null)
			deleteProject(project15);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850
public void testGenericAPIUsageFromA14Project6() throws CoreException {
	IJavaProject project14 = null;
	IJavaProject project15 = null;
	try {
		project15 = createJavaProject("Reconciler15API", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin");
		createFolder("/Reconciler15API/src/p2");
		createFile(
				"/Reconciler15API/src/p2/Y.java",
				"""
					package p2;
					public abstract class Y implements I<Y> {
					    public final Y foo(Object o, J<Y> j) {
					        return null;
					    }
					    public final void bar(Object o, J<Y> j, Y y) {
					    }
					}
					interface I<S> {
						public S foo(Object o, J<S> j);
						public void bar(Object o, J<S> j, S s);
					}
					interface J<S> {}
					"""
			);
		project15.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);

		project14 = createJavaProject("Reconciler1415", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
		project14.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);

		IClasspathEntry[] oldClasspath = project14.getRawClasspath();
		int oldLength = oldClasspath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[oldLength+1];
		System.arraycopy(oldClasspath, 0, newClasspath, 0, oldLength);
		newClasspath[oldLength] = JavaCore.newProjectEntry(new Path("/Reconciler15API"));
		project14.setRawClasspath(newClasspath, null);

		createFolder("/Reconciler1415/src/p1");
		String source =
			"""
			package p1;
			import p2.Y;
			public class X {
			   private int unused = 0;
				public Object foo() {
					return new Y() {};
				}
			}""";

		createFile(
			"/Reconciler1415/src/p1/X.java",
			source
		);

		this.workingCopies = new ICompilationUnit[1];
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/Reconciler1415/src/p1/X.java").getWorkingCopy(this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. WARNING in /Reconciler1415/src/p1/X.java (at line 4)
					private int unused = 0;
					            ^^^^^^
				The value of the field X.unused is not used
				----------
				"""
		);
	} finally {
		if (project14 != null)
			deleteProject(project14);
		if (project15 != null)
			deleteProject(project15);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850
public void testGenericAPIUsageFromA14Project7() throws CoreException, IOException {
	IJavaProject project14 = null;
	IJavaProject project15 = null;
	try {
		project15 = createJavaProject("Reconciler15API", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin");
		createFolder("/Reconciler15API/src/p2");
		createFile(
				"/Reconciler15API/src/p2/Y.java",
				"""
					package p2;
					import java.util.List;
					public class Y<T> extends List<T> {
					    public static Y<String> getY() {
					        return new Y<String>();
					    }
					}
					"""
			);
		project15.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);

		addLibrary(
				project15,
				"libList15.jar",
				"libList15src.zip",
				new String[] {
					"java/util/List.java",
					"""
						package java.util;
						public class List<T> {
						}"""
				},
				JavaCore.VERSION_1_5
			);

		project14 = createJavaProject("Reconciler1415", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
		project14.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);

		addLibrary(
				project14,
				"libList14.jar",
				"libList14src.zip",
				new String[] {
					"java/util/List.java",
					"""
						package java.util;
						public class List {
						}"""
				},
				JavaCore.VERSION_1_4
			);

		IClasspathEntry[] oldClasspath = project14.getRawClasspath();
		int oldLength = oldClasspath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[oldLength+1];
		System.arraycopy(oldClasspath, 0, newClasspath, 0, oldLength);
		newClasspath[oldLength] = JavaCore.newProjectEntry(new Path("/Reconciler15API"));
		project14.setRawClasspath(newClasspath, null);

		createFolder("/Reconciler1415/src/p1");
		String source =
			"""
			package p1;
			import java.util.List;
			import p2.Y;
			public class X {
				private static List getList(boolean test) {
				    if (test)
				        return new Y();
				    else
					    return Y.getY();
			   }
			}""";

		createFile(
			"/Reconciler1415/src/p1/X.java",
			source
		);

		this.workingCopies = new ICompilationUnit[1];
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/Reconciler1415/src/p1/X.java").getWorkingCopy(this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. WARNING in /Reconciler1415/src/p1/X.java (at line 5)
					private static List getList(boolean test) {
					                    ^^^^^^^^^^^^^^^^^^^^^
				The method getList(boolean) from the type X is never used locally
				----------
				"""
		);
	} finally {
		if (project14 != null)
			deleteProject(project14);
		if (project15 != null)
			deleteProject(project15);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=324850
public void testGenericAPIUsageFromA14Project8() throws CoreException, IOException {
	IJavaProject project14 = null;
	IJavaProject project15 = null;
	try {
		project15 = createJavaProject("Reconciler15API", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin");
		createFolder("/Reconciler15API/src/p2");
		createFile(
				"/Reconciler15API/src/p2/Y.java",
				"""
					package p2;
					public class Y<T> extends java.util.List<T> {
					    public static Y<String> getY() {
					        return new Y<String>();
					    }
					}
					"""
			);
		project15.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);

		addLibrary(
				project15,
				"libList15.jar",
				"libList15src.zip",
				new String[] {
					"java/util/List.java",
					"""
						package java.util;
						public class List<T> {
						}"""
				},
				JavaCore.VERSION_1_5
			);

		project14 = createJavaProject("Reconciler1415", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
		project14.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);

		addLibrary(
				project14,
				"libList14.jar",
				"libList14src.zip",
				new String[] {
					"java/util/List.java",
					"""
						package java.util;
						public class List {
						}"""
				},
				JavaCore.VERSION_1_4
			);

		IClasspathEntry[] oldClasspath = project14.getRawClasspath();
		int oldLength = oldClasspath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[oldLength+1];
		System.arraycopy(oldClasspath, 0, newClasspath, 0, oldLength);
		newClasspath[oldLength] = JavaCore.newProjectEntry(new Path("/Reconciler15API"));
		project14.setRawClasspath(newClasspath, null);

		createFolder("/Reconciler1415/src/p1");
		String source =
			"""
			package p1;
			import p2.Y;
			public class X {
				private static java.util.List getList(boolean test) {
				    if (test)
				        return new Y();
				    else
					    return Y.getY();
			   }
			}""";

		createFile(
			"/Reconciler1415/src/p1/X.java",
			source
		);

		this.workingCopies = new ICompilationUnit[1];
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/Reconciler1415/src/p1/X.java").getWorkingCopy(this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. WARNING in /Reconciler1415/src/p1/X.java (at line 4)
					private static java.util.List getList(boolean test) {
					                              ^^^^^^^^^^^^^^^^^^^^^
				The method getList(boolean) from the type X is never used locally
				----------
				"""
		);
	} finally {
		if (project14 != null)
			deleteProject(project14);
		if (project15 != null)
			deleteProject(project15);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=328775
public void test14ProjectWith15JRE() throws CoreException, IOException {
	IJavaProject project14 = null;
	try {
		project14 = createJavaProject("Reconciler1415", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin");
		project14.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);

		createFolder("/Reconciler1415/src/p1");
		String source =
			"""
			package p1;
			public class X {
				int a;
			   private Class c = a == 1 ? int.class : long.class;
			}
			""";
		createFile(
			"/Reconciler1415/src/p1/X.java",
			source
		);

		this.workingCopies = new ICompilationUnit[1];
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/Reconciler1415/src/p1/X.java").getWorkingCopy(this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. WARNING in /Reconciler1415/src/p1/X.java (at line 4)
					private Class c = a == 1 ? int.class : long.class;
					              ^
				The value of the field X.c is not used
				----------
				"""
		);
	} finally {
		if (project14 != null)
			deleteProject(project14);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=329593
public void testJsr14TargetProjectWith14JRE() throws CoreException, IOException {
	IJavaProject project14 = null;
	try {
		project14 = createJavaProject("Reconciler1415", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
		project14.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		project14.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		project14.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);

		createFolder("/Reconciler1415/src/p1");
		String source =
			"""
			package p1;
			public class X {
			    public void foo() {
			        Class type = null;
			        if (type == byte.class)
			            return;
			    }
			}
			""";
		createFile(
			"/Reconciler1415/src/p1/X.java",
			source
		);

		this.workingCopies = new ICompilationUnit[1];
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/Reconciler1415/src/p1/X.java").getWorkingCopy(this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. WARNING in /Reconciler1415/src/p1/X.java (at line 6)
					return;
					^^^^^^^
				Dead code
				----------
				"""

		);
	} finally {
		if (project14 != null)
			deleteProject(project14);
	}
}
public void testGenericAPIUsageFromA14Project9() throws CoreException {
	IJavaProject project14 = null;
	IJavaProject project15 = null;
	try {
		project15 = createJavaProject("Reconciler15API", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin");
		createFolder("/Reconciler15API/src/p1");
		createFile(
				"/Reconciler15API/src/p1/Y.java",
				"""
					package p1;
					public class Y {
					    static <T> void foo(List<T> expected) {}
					    public static <T> void foo(T expected) {}
					}
					"""
			);
		createFile(
				"/Reconciler15API/src/p1/List.java",
				"package p1;\n" +
				"public class List<T> {}\n"
			);
		project15.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);

		project14 = createJavaProject("Reconciler1415", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin");
		project14.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
		project14.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);

		IClasspathEntry[] oldClasspath = project14.getRawClasspath();
		int oldLength = oldClasspath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[oldLength+1];
		System.arraycopy(oldClasspath, 0, newClasspath, 0, oldLength);
		newClasspath[oldLength] = JavaCore.newProjectEntry(new Path("/Reconciler15API"));
		project14.setRawClasspath(newClasspath, null);

		createFolder("/Reconciler1415/src/p1");
		String source =
			"""
			package p1;
			public class X {
				private int unused = 0;
			    X(List l) {
			        Y.foo(l);
			    }
			}
			""";

		createFile(
			"/Reconciler1415/src/p1/X.java",
			source
		);

		this.workingCopies = new ICompilationUnit[1];
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/Reconciler1415/src/p1/X.java").getWorkingCopy(this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. WARNING in /Reconciler1415/src/p1/X.java (at line 3)
					private int unused = 0;
					            ^^^^^^
				The value of the field X.unused is not used
				----------
				"""
		);
	} finally {
		if (project14 != null)
			deleteProject(project14);
		if (project15 != null)
			deleteProject(project15);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=374176
// verify that a reconcile does not result in errors for a CU whose package does not have
// default null annotations
public void testBug374176() throws CoreException, IOException, InterruptedException {
	IJavaProject project15 = null;
	try {
		project15 = createJavaProject("TestAnnot", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin");
		createFolder("/TestAnnot/src/p1");
		String source = """
			package p1;
			public class Y {
			}
			;""";
		createFile(
				"/TestAnnot/src/p1/Y.java",
				source
			);
		createFolder("/TestAnnot/src/p2");
		createFile(
				"/TestAnnot/src/p2/Y2.java",
				"""
					package p2;
					public class Y2{
					}
					"""
			);
		project15.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_PB_NULL_REFERENCE, JavaCore.ERROR);
		project15.setOption(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		project15.setOption(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
		project15.setOption(JavaCore.COMPILER_PB_INCLUDE_ASSERTS_IN_NULL_ANALYSIS, JavaCore.ENABLED);
		project15.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
		project15.setOption(JavaCore.COMPILER_PB_MISSING_NONNULL_BY_DEFAULT_ANNOTATION, JavaCore.ERROR);

		this.workingCopies = new ICompilationUnit[2];
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/TestAnnot/src/p1/Y.java").getWorkingCopy(this.wcOwner, null);
		this.workingCopies[0].makeConsistent(null);
		this.workingCopies[0].reconcile(ICompilationUnit.NO_AST, false, null, null);

		assertNoProblem(sourceChars, this.workingCopies[0]);
	} finally {
		if (project15 != null)
			deleteProject(project15);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=374176
// verify that a reconcile DOES result in errors for package-info whose package does not have
// default null annotations
public void testBug374176b() throws CoreException, IOException, InterruptedException {
	IJavaProject project15 = null;
	try {
		project15 = createJavaProject("TestAnnot", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin");
		createFolder("/TestAnnot/src/p1");
		String source = "package p1;\n";
		createFile(
				"/TestAnnot/src/p1/package-info.java",
				source
			);
		project15.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_PB_NULL_REFERENCE, JavaCore.ERROR);
		project15.setOption(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
		project15.setOption(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
		project15.setOption(JavaCore.COMPILER_PB_INCLUDE_ASSERTS_IN_NULL_ANALYSIS, JavaCore.ENABLED);
		project15.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
		project15.setOption(JavaCore.COMPILER_PB_MISSING_NONNULL_BY_DEFAULT_ANNOTATION, JavaCore.ERROR);

		this.workingCopies = new ICompilationUnit[1];
		char[] sourceChars = source.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/TestAnnot/src/p1/package-info.java").getWorkingCopy(this.wcOwner, null);
		this.workingCopies[0].makeConsistent(null);
		this.workingCopies[0].reconcile(ICompilationUnit.NO_AST, false, null, null);

		assertProblems("Unexpected problems",
			"""
				----------
				1. ERROR in /TestAnnot/src/p1/package-info.java (at line 1)
					package p1;
					        ^^
				A default nullness annotation has not been specified for the package p1
				----------
				""");
		assertProblems("Unexpected problems",
			"""
				----------
				1. ERROR in /TestAnnot/src/p1/package-info.java (at line 1)
					package p1;
					        ^^
				A default nullness annotation has not been specified for the package p1
				----------
				""",
			this.problemRequestor);
	} finally {
		if (project15 != null)
			deleteProject(project15);
	}
}
public void testSecondaryTypeDeletion() throws CoreException, IOException {

	// Set working copy content with no error
	setUpWorkingCopy("/Reconciler/src/X.java",
			"""
				interface I {
					void foo();
				}
				public class X {
					static void goo(I i) {
					}
				}
				"""
			);
	assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
			);

	String contents =
					"""
		public class X {
			static void goo(I i) {
			}
		}
		""";

	setWorkingCopyContents(contents);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, true, null, null);
	assertProblems(
			"Wrong expected problems",
			"""
				----------
				1. ERROR in /Reconciler/src/X.java (at line 2)
					static void goo(I i) {
					                ^
				I cannot be resolved to a type
				----------
				"""
			);
	}
/**
 * Project's compliance: source: 1.5, compiler: 1.5
 * Jar's compliance: source: 1.3, compiler: 1.3
 * Jar contains a class with "enum" package and is located inside the project.
 * The test verifies that class from the "enum" package is correctly reconciled.
 */
public void testBug410207a() throws Exception {
	try {
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB", "/P/lib.jar"}, "bin", "1.5");
		Util.createJar(new String[] {
				"a/enum/b/NonCompliant.java",
				"""
					package a.enum.b;
					public class NonCompliant {
					}""",
				"lib/External.java",
				"""
					package lib;
					import a.enum.b.NonCompliant;
					public class External {
					   public NonCompliant setNonCompliant(NonCompliant x) {
					      return null;
						}
					}"""
			},
			p.getProject().getLocation().append("lib.jar").toOSString(),
			"1.3");
		refresh(p);
		setUpWorkingCopy(
				"/P/src/p/Main.java",
				"""
					package p;
					import lib.External;
					public class Main {
					   public void m() {
					      External external = new External();
					      external.setNonCompliant(null);
					   };
					}"""
		);
		this.problemRequestor.reset();
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true/*force problem detection*/, null, null);
		assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n"
		);
	} finally {
		deleteProject("P");
	}
}
/**
 * Project's compliance: source: 1.5, compiler: 1.5
 * Jar's compliance: source: 1.4, compiler: 1.6
 * Jar contains a class with "enum" package and is located inside the project.
 * The test verifies that class from the "enum" package is correctly reconciled.
 */
public void testBug410207b() throws Exception {
	try {
		IJavaProject p = createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB", "/P/lib.jar"}, "bin", "1.5");
		Map options = new HashMap();
		options.put(CompilerOptions.OPTION_Source, "1.4");
		Util.createJar(new String[] {
				"a/enum/b/NonCompliant.java",
				"""
					package a.enum.b;
					public class NonCompliant {
					}""",
				"lib/External.java",
				"""
					package lib;
					import a.enum.b.NonCompliant;
					public class External {
					   public NonCompliant setNonCompliant(NonCompliant x) {
					      return null;
						}
					}"""
			},
			null,/*extraPathsAndContents*/
			p.getProject().getLocation().append("lib.jar").toOSString(),
			null,/*classpath*/
			"1.6",
			options);
		refresh(p);
		setUpWorkingCopy(
				"/P/src/p/Main.java",
				"""
					package p;
					import lib.External;
					public class Main {
					   public void m() {
					      External external = new External();
					      external.setNonCompliant(null);
					   };
					}"""
		);
		this.problemRequestor.reset();
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true/*force problem detection*/, null, null);
		assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n"
		);
	} finally {
		deleteProject("P");
	}
}
/**
 * Two projects:
 * 		Lib: source: 1.4, compiler: 1.4
 * 		P: source: 1.5, compiler: 1.5
 * Lib contains a class with "enum" package and is required by P (dependency on the bin folder).
 * The test verifies that class from the "enum" package is correctly reconciled for P.
 */
public void testBug410207c() throws Exception {
	try {
		createJavaProject("Lib", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin", "1.4");
		createFolder("/Lib/src/a/enum/b");
		createFile(
				"/Lib/src/a/enum/b/NonCompliant.java",
				"""
					package a.enum.b;
					public class NonCompliant {
					}"""
		);
		createFolder("/Lib/src/lib");
		createFile(
				"/Lib/src/lib/External.java",
				"""
					package lib;
					import a.enum.b.NonCompliant;
					public class External {
					   public NonCompliant setNonCompliant(NonCompliant x) {
					      return null;
						}
					}"""
		);
		getProject("Lib").build(IncrementalProjectBuilder.FULL_BUILD, null);
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB", "/Lib/bin"}, "bin", "1.5");
		setUpWorkingCopy(
				"/P/src/p/Main.java",
				"""
					package p;
					import lib.External;
					public class Main {
					   public void m() {
					      External external = new External();
					      external.setNonCompliant(null);
					   };
					}"""
		);
		this.problemRequestor.reset();
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true/*force problem detection*/, null, null);
		assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n"
		);
	} finally {
		deleteProjects(new String[] { "Lib", "P" });
	}
}
/**
 * Two projects:
 * 		Lib: source: 1.4, compiler: 1.4
 * 		P: source: 1.5, compiler: 1.5
 * Lib contains a class with "enum" package and is required by P (dependency on the whole project).
 * The test verifies that class from the "enum" package is correctly reconciled for P.
 */
public void testBug410207d() throws Exception {
	try {
		createJavaProject("Lib", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin", "1.4");
		createFolder("/Lib/src/a/enum/b");
		createFile(
				"/Lib/src/a/enum/b/NonCompliant.java",
				"""
					package a.enum.b;
					public class NonCompliant {
					}"""
		);
		createFolder("/Lib/src/lib");
		createFile(
				"/Lib/src/lib/External.java",
				"""
					package lib;
					import a.enum.b.NonCompliant;
					public class External {
					   public NonCompliant setNonCompliant(NonCompliant x) {
					      return null;
						}
					}"""
		);
		getProject("Lib").build(IncrementalProjectBuilder.FULL_BUILD, null);
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB"}, new String[] {"/Lib"}, "bin", "1.5");
		setUpWorkingCopy(
				"/P/src/p/Main.java",
				"""
					package p;
					import lib.External;
					public class Main {
					   public void m() {
					      External external = new External();
					      external.setNonCompliant(null);
					   };
					}"""
		);
		this.problemRequestor.reset();
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true/*force problem detection*/, null, null);
		assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n"
		);
	} finally {
		deleteProjects(new String[] { "Lib", "P" });
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=440592, Cannot easily launch application in case of certain usage of lambda expressions
public void testBug440592() throws Exception {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL18_FULL"}, "bin", "1.8", true);
		createFile(
				"/P/src/BugTest.java",
				"""
					public class BugTest {
						public void baz(InterfaceForBugTest arg) {
						}
						public void bar() {
							baz(InterfaceForBugTest.instance);\s
						}
						public Runnable returningLambda() {\s
							return () -> {
							};
						}
						public static void main(String[] args) {
						}
					}
					"""
		);
		createFile(
				"/P/src/InterfaceForBugTest.java",
				"""
					public interface InterfaceForBugTest {
						public static InterfaceForBugTest creator1(Runnable simpleInstance){
							return null;
						}
						public static void methodWithAnonymousImplementation() {
							new InterfaceForBugTest() {
								@Override
								public void fun1() {
								}
								@Override
								public void fun2() {
								}
								@Override
								public void fun3() {
								}
								@Override
								public void fun4() {
								}
								@Override
								public void fun5() {
								}
							};
						}\s
						public static void methodWithAnonymousImplementation2() {
							new InterfaceForBugTest() {
								@Override
								public void fun1() {
								}
								@Override
								public void fun2() {
								}
								@Override
								public void fun3() {
								}
								@Override
								public void fun4() {
								}
								@Override
								public void fun5() {
								}
							};
						}
						public static InterfaceForBugTest instance = creator1(() -> {
						});
						void fun1();
						void fun2();
						void fun3();
						void fun4();
						void fun5();
					}
					"""

		);
		getProject("P").build(IncrementalProjectBuilder.FULL_BUILD, null);
		setUpWorkingCopy(
				"/P/src/BugTest.java",
				"""
					public class BugTest {
						public void baz(InterfaceForBugTest arg) {
						}
						public void bar() {
							baz(InterfaceForBugTest.instance);\s
						}
						public Runnable returningLambda() {\s
							return () -> {
							};
						}
						public static void main(String[] args) {
						}
					}
					"""
		);
		this.problemRequestor.reset();
		this.workingCopy.reconcile(ICompilationUnit.NO_AST, true/*force problem detection*/, null, null);
		assertProblems(
				"Unexpected problems",
				"----------\n" +
				"----------\n"
		);
	} finally {
		deleteProjects(new String[] { "P" });
	}
}
public void testBug485092() throws CoreException, InterruptedException {

	IJavaProject project15 = null;
	IJavaProject project18 = null;
	try {
		project15 = createJavaProject("Reconciler1518", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin");
		createFolder("/Reconciler1518/src/p1");
		String source =
				"""
			package p1;
			
			public interface PreferenceableOption<T extends Object> {}
			""";

		createFile(
			"/Reconciler1518/src/p1/PreferenceableOption.java",
			source
		);

		project15.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		project15.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);

		project18 = createJavaProject("Reconciler18", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin");
		createFolder("/Reconciler18/src/p2");
		String otherSource =
				"""
			package p2;
			import org.eclipse.jdt.annotation.NonNull;
			import org.eclipse.jdt.annotation.Nullable;
			
			import p1.PreferenceableOption;
			public class PivotEnvironmentFactory
			{
				public @NonNull Object @Nullable [] scopeContexts = null;
			}
			""";

		createFile(
			"/Reconciler18/src/p2/PivotEnvironmentFactory.java",
			otherSource
		);
		project18.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
		project18.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
		project18.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);


		project18.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
		Bundle[] bundles = Platform.getBundles("org.eclipse.jdt.annotation","[2.0.0,3.0.0)");
		File bundleFile = FileLocator.getBundleFileLocation(bundles[0]).get();
		String annotationsLib = bundleFile.isDirectory() ? bundleFile.getPath()+"/bin" : bundleFile.getPath();
		IClasspathEntry nullAnnotationsClassPathEntry = JavaCore.newLibraryEntry(new Path(annotationsLib), null, null);

		IClasspathEntry[] oldClasspath = project18.getRawClasspath();
		int oldLength = oldClasspath.length;
		IClasspathEntry[] newClasspath = new IClasspathEntry[oldLength+2];
		System.arraycopy(oldClasspath, 0, newClasspath, 0, oldLength);
		newClasspath[oldLength] = JavaCore.newProjectEntry(new Path("/Reconciler1518"));
		newClasspath[oldLength + 1] = nullAnnotationsClassPathEntry;
		project18.setRawClasspath(newClasspath, null);

		this.workingCopies = new ICompilationUnit[1];
		char[] sourceChars = otherSource.toCharArray();
		this.problemRequestor.initialize(sourceChars);
		this.workingCopies[0] = getCompilationUnit("/Reconciler18/src/p2/PivotEnvironmentFactory.java").getWorkingCopy(this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"""
				----------
				1. WARNING in /Reconciler18/src/p2/PivotEnvironmentFactory.java (at line 5)
					import p1.PreferenceableOption;
					       ^^^^^^^^^^^^^^^^^^^^^^^
				The import p1.PreferenceableOption is never used
				----------
				"""
		);
	} finally {
		if (project15 != null)
			deleteProject(project15);
		if (project18 != null)
			deleteProject(project18);
	}
}
public void testBug534865() throws CoreException, IOException {
	IJavaProject project18 = null;
	try {
		project18 = createJavaProject("Reconciler18", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin");
		setUpProjectCompliance(project18, "1.8");
		project18.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
		createFolder("/Reconciler18/src/org/eclipse/jdt/annotation");
		createFile(
			"/Reconciler18/src/org/eclipse/jdt/annotation/Nullable.java",
			"""
				package org.eclipse.jdt.annotation;
				import static java.lang.annotation.ElementType.TYPE_USE;
				
				import java.lang.annotation.Documented;
				import java.lang.annotation.Retention;
				import java.lang.annotation.RetentionPolicy;
				import java.lang.annotation.Target;
				@Documented
				@Retention(RetentionPolicy.CLASS)
				@Target({ TYPE_USE })
				public @interface Nullable {
					// marker annotation with no members
				}
				"""
		);
		String source =
				"""
			package org.eclipse.jdt.annotation;
			import static java.lang.annotation.ElementType.TYPE_USE;
			
			import java.lang.annotation.Documented;
			import java.lang.annotation.Retention;
			import java.lang.annotation.RetentionPolicy;
			import java.lang.annotation.Target;
			@Documented
			@Retention(RetentionPolicy.CLASS)
			@Target({ TYPE_USE })
			public @interface NonNull {
				// marker annotation with no members
			}
			""";

		createFile(
			"/Reconciler18/src/org/eclipse/jdt/annotation/NonNull.java",
			source
		);
		this.workingCopies = new ICompilationUnit[1];
		this.problemRequestor.initialize(source.toCharArray());
		this.workingCopies[0] = getCompilationUnit("/Reconciler18/src/org/eclipse/jdt/annotation/NonNull.java").getWorkingCopy(this.wcOwner, null);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n"
		);
	} finally {
		if (project18 != null)
			deleteProject(project18);
	}
}
public void testBug562637() throws CoreException, IOException, InterruptedException {
	if (!isJRE15) return;
	IJavaProject project15 = null;
	try {
		project15 = createJava14Project("Reconciler_15", new String[] {"src"});
		project15.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_16);
		project15.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_16);
		project15.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_16);
		project15.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		project15.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);

		String[] sources = new String[2];
		char[][] sourceAsArray = new char[2][];
		createFolder("/Reconciler_15/src/p");
		createFolder("/Reconciler_15/src/q");
		sources[0] = """
			package p;
			public record X(int a, int b) {
				public X {
					a = a * 2;
				}
				public  void foo() {}
			}
			class Z {
				public void bar() {
					X x = new X(0,1);
					int l = x.a(); // works fine in the same file
					System.out.println(l);
				}
			}""";
		createFile(
			"/Reconciler_15/src/p/X.java",
			sources[0]
		);
		sourceAsArray[0] = sources[0].toCharArray();
		sources[1] =
				"""
					package q;
					import p.X;
					public class Y {
						public X myField;\s
						public static void main(String[] args) {
							X x  = new X(0, 1);
							int l = x.a(); //Incorrect Error: The method a() is undefined for the type X
							System.out.println(l);
						}
					} """;

		createFile(
			"/Reconciler_15/src/q/Y.java",
			sources[1]
		);
		sourceAsArray[1] = sources[1].toCharArray();
		waitUntilIndexesReady();
		this.workingCopies = new ICompilationUnit[2];
			// Get first working copy and verify that there's no error
		this.problemRequestor.initialize(sourceAsArray[0]);
		this.workingCopies[0] = getCompilationUnit("/Reconciler_15/src/p/X.java").getWorkingCopy(this.wcOwner, null);
		assertNoProblem(sourceAsArray[0], this.workingCopies[0]);

		// Get second working copy and verify that there's no error
		this.problemRequestor.initialize(sourceAsArray[1]);
		this.workingCopies[1] = getCompilationUnit("/Reconciler_15/src/q/Y.java").getWorkingCopy(this.wcOwner, null);
		assertNoProblem(sourceAsArray[1], this.workingCopies[1]);

	} finally {
		if (project15 != null)
			deleteProject(project15);
	}
}
public void testBug564613_001() throws CoreException, IOException, InterruptedException {
	if (!isJRE17) return;
	IJavaProject project15 = null;
	try {
		project15 = createJava15Project("Reconciler_15", new String[] {"src"});
		project15.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_17);
		project15.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_17);
		project15.setOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_17);
		project15.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		project15.setOption(JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);

		String[] sources = new String[2];
		char[][] sourceAsArray = new char[2][];
		createFolder("/Reconciler_15/src/X");
		sources[0] =
				"""
					public sealed class X  permits Y Z {}\s
					final class Y extends X{}\s
					final class Z extends X{}""";
		createFile(
			"/Reconciler_15/src/X.java",
			sources[0]
		);
		sourceAsArray[0] = sources[0].toCharArray();
		waitUntilIndexesReady();
			// Get first working copy and verify that there's no error
		this.problemRequestor.initialize(sourceAsArray[0]);
		this.workingCopy = getCompilationUnit("/Reconciler_15/src/X.java").getWorkingCopy(this.wcOwner, null);
		try {
			this.workingCopy.makeConsistent(null);
			assertProblemsInclude("Unexpected Errors",
					"""
						----------
						1. ERROR in /Reconciler_15/src/X.java (at line 1)
							public sealed class X  permits Y Z {}\s
							                       ^^^^^^^
						Syntax error on token "permits", { expected
						----------
						""");
		} catch (ArrayIndexOutOfBoundsException e) {
			assertFalse("Failure: AIOOBE thrown", true);
		}


	} finally {
		if (project15 != null)
			deleteProject(project15);
	}
}
}
