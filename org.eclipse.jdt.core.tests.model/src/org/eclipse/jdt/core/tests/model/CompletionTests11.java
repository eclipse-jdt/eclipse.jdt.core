/*******************************************************************************
 * Copyright (c) 2018 IBM and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

import junit.framework.Test;

public class CompletionTests11 extends AbstractJavaModelCompletionTests {

public CompletionTests11(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	if (COMPLETION_PROJECT == null)  {
		COMPLETION_PROJECT = setUpJavaProject("Completion", "11");
	} else {
		setUpProjectCompliance(COMPLETION_PROJECT, "11");
	}
	super.setUpSuite();
}
public static Test suite() {
	return buildModelTestSuite(CompletionTests11.class);
}
public void test_var_in_parameter_in_lambda() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/X.java",
			"package test;\n" +
			 		"public class X {\n" +
			 		"	public static void main(String[] args) {	\n" +
			 		"		I lambda = (va ) -> {}; \n" +
			 		"		lambda.apply(10); \n" +
			 		"		}\n" +
			 		"	}\n" +
			 		"interface I {\n" +
			 		"void apply(Integer a); \n" +
			 		"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "(va";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
		"var[KEYWORD]{var, null, null, var, null, 49}",
		requestor.getResults());
}

}
