/*******************************************************************************
* Copyright (c) 2026 Advantest Europe GmbH and others.
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
*     Srikanth Sankaran - initial implementation
*******************************************************************************/

package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class CompletionTests28 extends AbstractJavaModelCompletionTests {

	private IJavaProject completion28Project;

	public CompletionTests28(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(CompletionTests28.class);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();

		assertEquals(
				"Run this suite using JDK 28, since it uses the running JDK's library",
				"28",
				System.getProperty("java.specification.version"));

		// Creates the project rather than copying workspace/Completion28.
		this.completion28Project = createJava9Project("Completion28", "28");

		this.completion28Project.setOption(
				JavaCore.COMPILER_SOURCE, "28");
		this.completion28Project.setOption(
				JavaCore.COMPILER_COMPLIANCE, "28");
		this.completion28Project.setOption(
				JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, "28");
		this.completion28Project.setOption(
				JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		this.completion28Project.setOption(
				JavaCore.COMPILER_PB_REPORT_PREVIEW_FEATURES, JavaCore.IGNORE);

		waitUntilIndexesReady();
	}

	@Override
	public void tearDownSuite() throws Exception {
		try {
			if (this.completion28Project != null) {
				deleteProject(this.completion28Project);
				this.completion28Project = null;
			}
		} finally {
			super.tearDownSuite();
		}
	}

	public void test001_valueObjectThisCompletion() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion28/src/p/X.java", """
								package p;
								public value class X {
									final int f = 1;
									public void m() {}
									void test() {
										this.
									}
								}
								""");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();

		String contents = this.workingCopies[0].getSource();
		int start = contents.lastIndexOf("this.");
		assertTrue("Completion marker not found: " + "this.", start >= 0);

		this.workingCopies[0].codeComplete(
				start + "this.".length(), requestor, this.wcOwner);
		String results = requestor.getResults();

		assertTrue("Expected field f in:\n" + results, results.contains("f[FIELD_REF]"));
		assertTrue("Expected method " + "m" + " in:\n" + results, results.contains("m" + "[METHOD_REF]"));
		assertTrue("Expected method " + "equals" + " in:\n" + results, results.contains("equals" + "[METHOD_REF]"));
		assertTrue("Expected method " + "hashCode" + " in:\n" + results, results.contains("hashCode" + "[METHOD_REF]"));
		assertTrue("Expected method " + "toString" + " in:\n" + results, results.contains("toString" + "[METHOD_REF]"));
		assertTrue("Expected method " + "getClass" + " in:\n" + results, results.contains("getClass" + "[METHOD_REF]"));

		assertFalse("Unexpected method " + "wait" + " in:\n" + results, results.contains("wait" + "[METHOD_REF]"));
		assertFalse("Unexpected method " + "notify" + " in:\n" + results, results.contains("notify" + "[METHOD_REF]"));
		assertFalse("Unexpected method " + "notifyAll" + " in:\n" + results, results.contains("notifyAll" + "[METHOD_REF]"));
	}

	public void test002_valueObjectReceiverCompletion()
			throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
				this.workingCopies[0] = getWorkingCopy(
						"/Completion28/src/p/X.java", """
										package p;
										value class V {
											public final int f = 1;
											public void m() {}
										}
										public class X {
											void test(V valueObject) {
												valueObject.
											}
										}
										""");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();

		String contents = this.workingCopies[0].getSource();
		int start = contents.lastIndexOf("valueObject.");
		assertTrue("Completion marker not found: " + "valueObject.", start >= 0);

		this.workingCopies[0].codeComplete(start + "valueObject.".length(), requestor, this.wcOwner);
		String results = requestor.getResults();

		assertTrue("Expected field f in:\n" + results, results.contains("f[FIELD_REF]"));
		assertTrue("Expected method " + "m" + " in:\n" + results, results.contains("m" + "[METHOD_REF]"));
		assertTrue("Expected method " + "equals" + " in:\n" + results, results.contains("equals" + "[METHOD_REF]"));
		assertTrue("Expected method " + "hashCode" + " in:\n" + results, results.contains("hashCode" + "[METHOD_REF]"));
		assertTrue("Expected method " + "toString" + " in:\n" + results, results.contains("toString" + "[METHOD_REF]"));
		assertTrue("Expected method " + "getClass" + " in:\n" + results, results.contains("getClass" + "[METHOD_REF]"));

		assertFalse("Unexpected method " + "wait" + " in:\n" + results, results.contains("wait" + "[METHOD_REF]"));
		assertFalse("Unexpected method " + "notify" + " in:\n" + results, results.contains("notify" + "[METHOD_REF]"));
		assertFalse("Unexpected method " + "notifyAll" + " in:\n" + results, results.contains("notifyAll" + "[METHOD_REF]"));
	}

	public void test003_identityObjectReceiverCompletion()
			throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
				this.workingCopies[0] = getWorkingCopy(
						"/Completion28/src/p/X.java", """
										package p;
										public class X {
											void test(X identityObject) {
												identityObject.
											}
										}
										""");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();

		String contents = this.workingCopies[0].getSource();
		int start = contents.lastIndexOf("identityObject.");
		assertTrue("Completion marker not found: " + "identityObject.", start >= 0);

		this.workingCopies[0].codeComplete(start + "identityObject.".length(), requestor, this.wcOwner);
		String results = requestor.getResults();

		assertTrue("Expected method " + "wait" + " in:\n" + results, results.contains("wait" + "[METHOD_REF]"));
		assertTrue("Expected method " + "notify" + " in:\n" + results, results.contains("notify" + "[METHOD_REF]"));
		assertTrue("Expected method " + "notifyAll" + " in:\n" + results, results.contains("notifyAll" + "[METHOD_REF]"));
		assertTrue("Expected method " + "equals" + " in:\n" + results, results.contains("equals" + "[METHOD_REF]"));
		assertTrue("Expected method " + "hashCode" + " in:\n" + results, results.contains("hashCode" + "[METHOD_REF]"));
		assertTrue("Expected method " + "toString" + " in:\n" + results, results.contains("toString" + "[METHOD_REF]"));
		assertTrue("Expected method " + "getClass" + " in:\n" + results, results.contains("getClass" + "[METHOD_REF]"));
	}
}