/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *		IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;


import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;

import junit.framework.Test;

public class ReconcilerTests16 extends ModifyingResourceTests {

	protected ICompilationUnit workingCopy;
	protected ProblemRequestor problemRequestor;

	/*package*/ @SuppressWarnings("deprecation")
	static final int JLS_LATEST = AST.JLS16;

public ReconcilerTests16(String name) {
	super(name);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
//	JavaModelManager.VERBOSE = true;
//	TESTS_PREFIX = "testAnnotations";
//	TESTS_NAMES = new String[] { "testBug564289_001" };
//	TESTS_NUMBERS = new int[] { 118823 };
//	TESTS_RANGE = new int[] { 16, -1 };
}
public static Test suite() {
	return buildModelTestSuite(ReconcilerTests16.class);
}
protected void assertProblems(String message, String expected) {
	assertProblems(message, expected, this.problemRequestor);
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
			return ReconcilerTests16.this.problemRequestor;
		}
	};
	this.workingCopy = getCompilationUnit("Reconciler16/src/module-info.java").getWorkingCopy(this.wcOwner, null);
	this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
	startDeltas();
}
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();

	// Create project with 16 compliance
	IJavaProject project16 = createJava9Project("Reconciler16");
	project16.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_16);
	project16.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.IGNORE);
	project16.setOption(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.WARNING);


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
	deleteProject("Reconciler16");
	super.tearDownSuite();
}
public void testBug570399_001() throws Exception {
	if (!isJRE16)
		return;
	IJavaProject p = createJava16Project("p");
	try {
		createFile("p/src/X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					    R r1 = new R( 2, 3); // Wrong error: The constructor MyRecord(int, int) is undefined
					    R r2 = new R();      // works
					    int total = r1.x()+r2.x()+r1.y()+r2.y();
					    System.out.println("Hi"+total);
					  }
					}""");
		createFile("p/src/R.java",
				"""
					public record R(int x, int y) {
					    R() {
					        this(0, 0);
					    }
					}""");

		p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("markers in p",
				"",
				markers);

		this.workingCopy = getCompilationUnit("p/src/X.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
		assertProblems("Expecting no problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);
		this.workingCopy.discardWorkingCopy();
	} finally {
		deleteProject(p);
	}
}
public void _testBug570399_002() throws Exception {
	if (!isJRE16)
		return;
	IJavaProject p = createJava16Project("p");
	try {
		createFile("p/src/X.java",
				"""
					public class X {
					 public static void main(String[] args) {
					    R r1 = new R( 2, 3); // Wrong error: The constructor MyRecord(int, int) is undefined
					    R r2 = new R();      // works
					    int total = r1.x()+r2.x()+r1.y()+r2.y();
					    System.out.println("Hi"+total);
					  }
					}""");
		createFile("p/src/R.java",
				"""
					class  R {
					   int x, y;
					    int x() { return this.x;}
					    int y() { return this.y;}
					    R(int x, int y) {
					        this.x = x; this.y = y;
					    }
					}""");

		p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("markers in p",
				"",
				markers);

		this.workingCopy = getCompilationUnit("p/src/X.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
		assertProblems("Expecting no problems",
						"",
						this.problemRequestor);
		this.workingCopy.discardWorkingCopy();
	} finally {
		deleteProject(p);
	}
}
public void testBug576448_001() throws Exception {
	if (!isJRE16)
		return;
	IJavaProject p = createJava16Project("p");
	createFolder("/p/src/a");
	try {
		createFile("p/src/a/X.java",
				"""
					package a;
					import a.Interface.NestedInterface;
					import a.Interface.NestedInterface2;
					
					public record X(String someString, NestedInterface someInterface) implements NestedInterface2 {
					 public X(NestedInterface someInterface) {
					   this(null, someInterface); // <- error here
					 }
					 public X(String someString, NestedInterface someInterface) {
					   this.someString = someString;
					   this.someInterface = someInterface;
					 }
					 public static void main(String[] args) {
					   System.out.println("hello");
					 }
					}""");
		createFile("p/src/a/Interface.java",
				"""
					package a;
					public interface Interface {
					 interface NestedInterface {
					 }
					 interface NestedInterface2 {
					   String someString();
					   NestedInterface someInterface();
					   static NestedInterface2 create(String s, NestedInterface n) {
					     return new X(s, n);
					   }
					   static NestedInterface2 create(NestedInterface n) {
					     return new X(n);
					   }
					 }
					}""");

		p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("markers in p",
				"",
				markers);

		this.workingCopy = getCompilationUnit("p/src/X.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
		assertProblems("Expecting no problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);
		this.workingCopy.discardWorkingCopy();
	} finally {
		deleteProject(p);
	}
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/342
public void testissue342_001() throws Exception {
	if (!isJRE16)
		return;
	IJavaProject p = createJava16Project("p");
	createFolder("/p/src/a");
	try {
		createFile("p/src/a/Sneaker.java",
				"""
					package a;
					
					public record Sneaker(String brand, float price, int ... sizes) {
					 public void test () {
					   Sneaker sn = new Sneaker("Eclipse", 100, 9, 10, 11);
					   System.out.println(sn.sizes().length);
					 }
					}""");
		String mainSource =
				"""
			package a;
			
			public class Main {
			  public static void main(String[] args) {
			   Sneaker sn = new Sneaker("Eclipse", 100, 1, 2, 3);
			   System.out.println(sn.sizes().length);
			  }
			}""";
		createFile("p/src/a/Main.java", mainSource);

		p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("markers in p",
				"",
				markers);
		int reconcileFlags= ICompilationUnit.FORCE_PROBLEM_DETECTION;
		reconcileFlags|= ICompilationUnit.ENABLE_STATEMENTS_RECOVERY;
		reconcileFlags|= ICompilationUnit.ENABLE_BINDINGS_RECOVERY;
		this.workingCopy = getCompilationUnit("p/src/a/Main.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(mainSource.toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, reconcileFlags, this.wcOwner, null);
		assertProblems("Expecting no problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);
		this.workingCopy.discardWorkingCopy();
	} finally {
		deleteProject(p);
	}
}
public void testGH612_001() throws Exception {
	if (!isJRE16)
		return;
	IJavaProject p = createJava16Project("p");
	createFolder("/p/src/a");
	try {
		String source =
				"""
			package a;
			public class X {
			    public static void main(String[] args) {
			        Sub sub = new Sub();
			        sub.method1();
			    }
			
			    static class Outer<T> {\s
			        class Inner {
			            void sayHi() {
			                System.out.println("hello world!");
			            }
			        }
			    }
			
			    static class Sub extends Outer<Sub.Inner> {
			        void method1() {
			            Inner inner = new Inner();
			            inner.sayHi();
			        }
			    }
			}
			""";
		createFile("p/src/a/X.java",
				source);
		p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("markers in p",
				"",
				markers);
	} finally {
		deleteProject(p);
	}
}
public void testGH612_002() throws Exception {
	if (!isJRE16)
		return;
	IJavaProject p = createJava16Project("p");
	createFolder("/p/src/a");
	try {
		String source =
				"""
			package a;
			public class X {
			    public static void main(String[] args) {
			        Sub sub = new Sub();
			        sub.method1();
			    }
			
			    static interface Outer<T> {\s
			        class Inner {
			            void sayHi() {
			                System.out.println("hello world!");
			            }
			        }
			    }
			
			    static class Sub implements Outer<Sub.Inner> {
			        void method1() {
			            Inner inner = new Inner();
			            inner.sayHi();
			        }
			    }
			}
			""";
		createFile("p/src/a/X.java",
				source);
		p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("markers in p",
				"",
				markers);
	} finally {
		deleteProject(p);
	}
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1085
// Canonical constructor of generic record not found if other constructor is present
public void testGHIssue1085() throws Exception {
	if (!isJRE16)
		return;
	IJavaProject p = createJava16Project("p");
	try {
		createFile("p/src/GenericRecord.java",
				"""
					public record GenericRecord<A>(int parameter) {
					    public GenericRecord() {
					        this(0);
					    }
					}
					""");

		createFile("p/src/Test.java",
				"""
					public class Test {
					    public void test() {
					        new GenericRecord<String>(0);
					    }
					}
					""");

		p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("markers in p",
				"",
				markers);

		this.workingCopy = getCompilationUnit("p/src/Test.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
		assertProblems("Expecting no problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);
		this.workingCopy.discardWorkingCopy();
	} finally {
		deleteProject(p);
	}
}

// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1085
// Canonical constructor of generic record not found if other constructor is present
public void testGHIssue1085_2() throws Exception {
	if (!isJRE16)
		return;
	IJavaProject p = createJava16Project("p");
	try {
		createFile("p/src/GenericRecord.java",
				"""
					public record GenericRecord<A>(int parameter) {
					    public GenericRecord() {
					        this(0);
					    }
					    public GenericRecord(int parameter) {
					        this.parameter = parameter;
					    }
					}
					""");

		createFile("p/src/Test.java",
				"""
					public class Test {
					    public void test() {
					        new GenericRecord<String>(0);
					    }
					}
					""");

		p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("markers in p",
				"",
				markers);

		this.workingCopy = getCompilationUnit("p/src/Test.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
		assertProblems("Expecting no problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);
		this.workingCopy.discardWorkingCopy();
	} finally {
		deleteProject(p);
	}
}
// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/1085
// Canonical constructor of generic record not found if other constructor is present
public void testGHIssue1085_3() throws Exception {
	if (!isJRE16)
		return;
	IJavaProject p = createJava16Project("p");
	try {
		createFile("p/src/GenericRecord.java",
				"""
					public record GenericRecord<A>(int parameter) {
					    public GenericRecord() {
					        this(0);
					    }
					    public GenericRecord {
					    }
					}
					""");

		createFile("p/src/Test.java",
				"""
					public class Test {
					    public void test() {
					        new GenericRecord<String>(0);
					    }
					}
					""");

		p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("markers in p",
				"",
				markers);

		this.workingCopy = getCompilationUnit("p/src/Test.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
		assertProblems("Expecting no problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);
		this.workingCopy.discardWorkingCopy();
	} finally {
		deleteProject(p);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=578080
// incorrect compiler error: Type inference not working on records with constructor
public void test578080() throws Exception {
	if (!isJRE16)
		return;
	IJavaProject p = createJava16Project("p");
	try {
		createFile("p/src/Diamond.java",
				"""
					public record Diamond<T> (T value) {
						public Diamond {\t
						}
					}
					""");

		createFile("p/src/DiamondTest.java",
				"""
					public class DiamondTest {
						public void testDiamond(){
							assertEquals(new Diamond<>("y"), new Diamond<>("y"));
							final Diamond<String> hi = new Diamond<>("hello");
							assertNotNull(hi);
						}
					
						private void assertNotNull(Diamond<String> hi) {
						}
					
						private void assertEquals(Object o1, Object o2) {
						}\s
					}
					""");

		p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("markers in p",
				"",
				markers);

		this.workingCopy = getCompilationUnit("p/src/DiamondTest.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
		assertProblems("Expecting no problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);
		this.workingCopy.discardWorkingCopy();
	} finally {
		deleteProject(p);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=577351
// inference with diamond error in records in two files
public void test577351() throws Exception {
	if (!isJRE16)
		return;
	IJavaProject p = createJava16Project("p");
	try {

		createFile("p/src/TimeSeries.java",
				"""
					import java.util.Objects;
					
					public final class TimeSeries<T> {
					  public record Data<T>(long timestamp, T element) {
					    public Data {
					      Objects.requireNonNull(element);
					    }
					    @Override
					    public String toString() {
					      return timestamp + " | " + element;
					    }
					  }
					}
					""");

		createFile("p/src/TimeSeriesTest.java",
				"""
					public class TimeSeriesTest {
						public interface Executable {
							void execute() throws Throwable;
						}
					
						public void test() {
							assertThrows(NullPointerException.class, () -> new TimeSeries.Data<>(0, null));
						}
					
						private void assertThrows(Class<NullPointerException> class1, Executable ex) {
						}
					}
					"""
				);

		p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
		IMarker[] markers = p.getProject().findMarkers(null, true, IResource.DEPTH_INFINITE);
		assertMarkers("markers in p",
				"",
				markers);

		this.workingCopy = getCompilationUnit("p/src/TimeSeriesTest.java").getWorkingCopy(this.wcOwner, null);
		this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
		this.workingCopy.reconcile(JLS_LATEST, true, this.wcOwner, null);
		assertProblems("Expecting no problems",
				"----------\n" +
				"----------\n",
				this.problemRequestor);
		this.workingCopy.discardWorkingCopy();
	} finally {
		deleteProject(p);
	}
}
}
