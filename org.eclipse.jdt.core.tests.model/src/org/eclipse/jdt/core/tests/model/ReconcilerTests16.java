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
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
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

	/*package*/ static final int JLS_LATEST = AST.JLS16;

/**
 */
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
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"    R r1 = new R( 2, 3); // Wrong error: The constructor MyRecord(int, int) is undefined\n"+
				"    R r2 = new R();      // works\n"+
				"    int total = r1.x()+r2.x()+r1.y()+r2.y();\n"+
				"    System.out.println(\"Hi\"+total);\n"+
				"  }\n"+
				"}");
		createFile("p/src/R.java",
				"public record R(int x, int y) {\n"+
				"    R() {\n"+
				"        this(0, 0);\n"+
				"    }\n"+
				"}");

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
				"public class X {\n"+
				" public static void main(String[] args) {\n"+
				"    R r1 = new R( 2, 3); // Wrong error: The constructor MyRecord(int, int) is undefined\n"+
				"    R r2 = new R();      // works\n"+
				"    int total = r1.x()+r2.x()+r1.y()+r2.y();\n"+
				"    System.out.println(\"Hi\"+total);\n"+
				"  }\n"+
				"}");
		createFile("p/src/R.java",
				"class  R {\n"+
				"   int x, y;\n"+
				"    int x() { return this.x;}\n"+
				"    int y() { return this.y;}\n"+
				"    R(int x, int y) {\n"+
				"        this.x = x; this.y = y;\n"+
				"    }\n"+
				"}");

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
}
