/*******************************************************************************
 * Copyright (c) 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gayan Perera - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import junit.framework.Test;

public class CompletionTests21 extends AbstractJavaModelCompletionTests {

	// private static int expected_Rel = R_DEFAULT+R_RESOLVED+ R_CASE+
	// R_INTERESTING+R_EXACT_EXPECTED_TYPE+R_NON_STATIC+R_NON_RESTRICTED;
	// private static int void_Rel = R_DEFAULT+R_RESOLVED+ R_CASE+ R_INTERESTING+
	// R_VOID +R_NON_STATIC+R_NON_RESTRICTED;
	// private static int nonVoid_Rel = R_DEFAULT+R_RESOLVED+ R_CASE+ R_INTERESTING
	// +R_NON_STATIC+R_NON_RESTRICTED;
	private static int unqualified_Rel = R_DEFAULT + R_RESOLVED + R_CASE + R_INTERESTING + R_UNQUALIFIED
			+ R_NON_RESTRICTED;
	// private static int unqualifiedExact_Rel =
	// R_DEFAULT+R_RESOLVED+R_EXACT_EXPECTED_TYPE+ R_CASE+ R_INTERESTING
	// +R_UNQUALIFIED+R_NON_RESTRICTED;
	// private static int keyword_Rel= R_DEFAULT + R_RESOLVED + R_INTERESTING +
	// R_CASE + R_NON_RESTRICTED;
	static {
		// TESTS_NAMES = new String[] { "testGH2299" };
	}

	public CompletionTests21(String name) {
		super(name);
	}

	public void setUpSuite() throws Exception {
		if (COMPLETION_PROJECT == null) {
			COMPLETION_PROJECT = setUpJavaProject("Completion", "21");
		} else {
			setUpProjectCompliance(COMPLETION_PROJECT, "21");
		}
		super.setUpSuite();
		COMPLETION_PROJECT.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
	}

	public static Test suite() {
		return buildModelTestSuite(CompletionTests21.class);
	}

	public void testGH2299() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/SwitchRecordPattern.java", """
				public class SwitchRecordPattern {
					public void foo(java.io.Serializable o) {
						switch(o) {
							case Person(var name, var age) -> {
								/*here*/nam
							}
						}
					}
				}\
				""");
		this.workingCopies[1] = getWorkingCopy("/Completion/src/Person.java", """
				public record Person(String name, int age) implements java.io.Serializable  {}\
				""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/nam";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("name[LOCAL_VARIABLE_REF]{name, null, Ljava.lang.String;, name, null, " + unqualified_Rel + "}",
				requestor.getResults());
	}
}
