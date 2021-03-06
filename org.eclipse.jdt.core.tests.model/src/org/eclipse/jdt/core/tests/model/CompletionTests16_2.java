/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;

import junit.framework.Test;

//java 16 scenarios run with java 16 compliance
public class CompletionTests16_2 extends AbstractJavaModelCompletionTests {

	static {
		// TESTS_NAMES = new String[]{"test034"};
	}

	public CompletionTests16_2(String name) {
		super(name);
	}

	public void setUpSuite() throws Exception {
		if (COMPLETION_PROJECT == null) {
			COMPLETION_PROJECT = setUpJavaProject("Completion", "16");
		} else {
			setUpProjectCompliance(COMPLETION_PROJECT, "16");
		}
		super.setUpSuite();
	}

	public static Test suite() {
		return buildModelTestSuite(CompletionTests16_2.class);
	}

	// completion for local interface
	public void test001() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/X.java",
				"public seal class X permits Y{\n"
		+ " public static void main(String[] args){\n"
		+ "    interf;\n}\n}\n"
		+ "	");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "interf";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("interface[KEYWORD]{interface, null, null, interface, null, 49}", requestor.getResults());

	}

	// completion for local enum
	public void test002() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/X.java",
				"public seal class X permits Y{\n"
		+ " public static void main(String[] args){\n"
		+ "    enu;\n}\n}\n"
		+ "	");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "enu";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("Enum[TYPE_REF]{Enum, java.lang, Ljava.lang.Enum;, null, null, 42}\n"
				+ "enum[KEYWORD]{enum, null, null, enum, null, 49}",
				requestor.getResults());

	}
	// completion for final keyword in instanceof pattern variable
	public void test003() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public class Point {\n" +
				"private void method(Object o) throws Exception{\n" +
				"if ((o instanceof fina Record xvar )) \n" +
				"{\n" +
				"}\n" +
				"}\n" +
				"}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "fina";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("final[KEYWORD]{final, null, null, final, null, " + (R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_NON_RESTRICTED+R_FINAL+R_EXPECTED_TYPE) + "}",
				requestor.getResults());


	}
	// completion for final keyword in instanceof pattern variable at higher relevance than Fin* classes
		public void test004() throws JavaModelException {
			this.workingCopies = new ICompilationUnit[2];
			this.workingCopies[0] = getWorkingCopy(
					"/Completion/src/Point.java",
					"public class Point {\n" +
					"private void method(Object o) throws Exception{\n" +
					"if ((o instanceof fina Record xvar )) \n" +
					"{\n" +
					"}\n" +
					"}\n" +
					"}");
			this.workingCopies[1] = getWorkingCopy(
					"/Completion/src/Bug460411.java",
					"package abc;" +
					"public class FinalCl {\n"+
					"}\n");
			this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "fina";
			int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("FinalCl[TYPE_REF]{abc.FinalCl, abc, Labc.FinalCl;, null, null, "+(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_NON_RESTRICTED+R_EXPECTED_TYPE)+"}\n"
					+ "final[KEYWORD]{final, null, null, final, null, "+(R_DEFAULT+R_RESOLVED+R_INTERESTING+R_CASE+R_NON_RESTRICTED+R_FINAL+R_EXPECTED_TYPE)+"}",
					requestor.getResults());


		}

}
