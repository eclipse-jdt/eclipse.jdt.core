/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *		IBM Corporation - initial API and implementation
 *		Stephan Herrmann - Copy-adjusted structure from ReconcilerTests, filled with new content
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;


import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;

import junit.framework.Test;

public class ReconcilerTests9 extends ModifyingResourceTests {

	protected ICompilationUnit workingCopy;
	protected ProblemRequestor problemRequestor;
	
	/*package*/ static final int JLS_LATEST = AST.JLS9;

/**
 */
public ReconcilerTests9(String name) {
	super(name);
}
// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
//	JavaModelManager.VERBOSE = true;
//	TESTS_PREFIX = "testAnnotations";
//	TESTS_NAMES = new String[] { "testAnnotations2" };
//	TESTS_NUMBERS = new int[] { 118823 };
//	TESTS_RANGE = new int[] { 16, -1 };
}
public static Test suite() {
	return buildModelTestSuite(ReconcilerTests9.class);
}
protected void assertProblems(String message, String expected) {
	assertProblems(message, expected, this.problemRequestor);
}
/**
 * Setup for the next test.
 */
public void setUp() throws Exception {
	super.setUp();
	this.problemRequestor =  new ProblemRequestor();
	this.wcOwner = new WorkingCopyOwner() {
		public IProblemRequestor getProblemRequestor(ICompilationUnit unit) {
			return ReconcilerTests9.this.problemRequestor;
		}
	};
	this.workingCopy = getCompilationUnit("Reconciler9/src/module-info.java").getWorkingCopy(this.wcOwner, null);
	this.problemRequestor.initialize(this.workingCopy.getSource().toCharArray());
	startDeltas();
}
public void setUpSuite() throws Exception {
	super.setUpSuite();

	// Create project with 9 compliance
	IJavaProject project9 = createJavaProject("Reconciler9", new String[] {"src"}, new String[] {"JCL9_LIB"}, "bin");
	createFile(
		"/Reconciler9/src/module-info.java",
		"/**\n" +
		"  * @category before" +
		"  */\n" +
		"@Deprecated\n" +
		"module mod.one {\n" +
		"}"
	);
	project9.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_9);
	project9.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.IGNORE);
	project9.setOption(JavaCore.COMPILER_PB_INVALID_JAVADOC, JavaCore.WARNING);


}
void setWorkingCopyContents(String contents) throws JavaModelException {
	this.workingCopy.getBuffer().setContents(contents);
	this.problemRequestor.initialize(contents.toCharArray());
}
/**
 * Cleanup after the previous test.
 */
public void tearDown() throws Exception {
	TestCompilationParticipant.PARTICIPANT = null;
	if (this.workingCopy != null) {
		this.workingCopy.discardWorkingCopy();
	}
	stopDeltas();
	super.tearDown();
}
public void tearDownSuite() throws Exception {
	deleteProject("Reconciler9");
	super.tearDownSuite();
}
/*
 * Ensures that the delta is correct when adding an annotation
 */
public void testAnnotations1() throws JavaModelException {
	setWorkingCopyContents(
		"/**\n" +
		"  * @category before\n" +
		"  * @category after\n" +
		"  */\n" +
		"@Deprecated\n" +
		"@MyAnnot\n" +
		"module mod.one {\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"mod.one[*]: {CATEGORIES | ANNOTATIONS}\n" +
		"	@MyAnnot[+]: {}"
	);
}
/*
 * Ensures that the delta is correct when removing an annotation
 */
public void testAnnotations2deprecated() throws JavaModelException {
	setWorkingCopyContents(
		"/**\n" +
		"  * @category before\n" +
		"  */\n" +
		"module mod.one {\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"mod.one[*]: {MODIFIERS CHANGED | ANNOTATIONS}"
	);
}
/*
 * Ensures that the delta is correct when removing an annotation
 */
public void testAnnotations2() throws JavaModelException {
	setWorkingCopyContents(
		"/**\n" +
		"  * @category before\n" +
		"  */\n" +
		"  @MyAnnot(x=1)\n" +
		"module mod.one {\n" +
		"}"
	);
	this.workingCopy.makeConsistent(null);	setWorkingCopyContents(
		"/**\n" +
		"  * @category before\n" +
		"  */\n" +
		"module mod.one {\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"mod.one[*]: {ANNOTATIONS}\n" +
		"	@MyAnnot[-]: {}"
	);
}
/*
 * Ensures that the delta is correct when changing an annotation
 */
public void testAnnotations3() throws JavaModelException {
	setWorkingCopyContents(
		"/**\n" +
		"  * @category before\n" +
		"  */\n" +
		"  @MyAnnot(x=1)\n" +
		"module mod.one {\n" +
		"}"
	);
	this.workingCopy.makeConsistent(null);

	setWorkingCopyContents(
		"/**\n" +
		"  * @category before\n" +
		"  * @category after\n" +
		"  */\n" +
		"  @MyAnnot(y=1)\n" +
		"module mod.one {\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"mod.one[*]: {CATEGORIES | ANNOTATIONS}\n" +
		"	@MyAnnot[*]: {CONTENT}"
	);
}
/**
 * Ensures that the delta is correct when adding a category
 */
public void testCategories1() throws JavaModelException {
	setWorkingCopyContents(
		"/**\n" +
		"  * @category before\n" +
		"  * @category after\n" +
		"  */\n" +
		"@Deprecated\n" +
		"module mod.one {\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"mod.one[*]: {CATEGORIES}"
	);
}
/**
 * Ensures that the delta is correct when removing a category
 */
public void testCategories2() throws JavaModelException {
	setWorkingCopyContents(
		"/**\n" +
		"  */\n" +
		"@Deprecated\n" +
		"module mod.one {\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"mod.one[*]: {CATEGORIES}"
	);
}
/**
 * Ensures that the delta is correct when changing a category
 */
public void testCategories3() throws JavaModelException {
	setWorkingCopyContents(
		"/**\n" +
		"  * @category never\n" +
		"  */\n" +
		"@Deprecated\n" +
		"module mod.one {\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);
	assertWorkingCopyDeltas(
		"Unexpected delta",
		"mod.one[*]: {CATEGORIES}"
	);
}
}
