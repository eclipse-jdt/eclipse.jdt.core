/*******************************************************************************
 * Copyright (c) 2020 Julian Honnen.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Julian Honnen - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Hashtable;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import junit.framework.Test;

public class CompletionWithMissingTypesTests_1_8 extends AbstractJavaModelCompletionTests {

public CompletionWithMissingTypesTests_1_8(String name) {
	super(name);
}
@Override
public void setUpSuite() throws Exception {
	if (COMPLETION_PROJECT == null)  {
		COMPLETION_PROJECT = setUpJavaProject("Completion", "1.8");
	} else {
		setUpProjectCompliance(COMPLETION_PROJECT, "1.8");
	}
	super.setUpSuite();
}
static {
//	TESTS_NAMES = new String[] { "testZZZ"};
}
public static Test suite() {
	return buildModelTestSuite(CompletionWithMissingTypesTests_1_8.class);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=563010
public void testStaticInterfaceMethod() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	try {
		Hashtable<String, String> options = new Hashtable<>(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/test/Test.java",
				"package test;"+
				"public class Test {\n" +
				"  void foo() {\n" +
				"    MissingType.foo\n" +
				"  }\n" +
				"}\n");

			this.workingCopies[1] = getWorkingCopy(
				"/Completion/src/missing/MissingType.java",
				"package missing;"+
				"public interface MissingType {\n" +
				"  void foo1()\n" +
				"  static void foo2() {}\n" +
				"}\n");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "MissingType.foo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_INHERITED + R_NON_RESTRICTED
				+ R_NO_PROBLEMS;
		int start1 = str.lastIndexOf("MissingType.") + "MissingType.".length();
		int end1 = start1 + "foo".length();
		int start2 = str.indexOf("MissingType");
		int end2 = start2 + "MissingType".length();
		assertResults(
				"foo2[METHOD_REF]{foo2(), Lmissing.MissingType;, ()V, foo2, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
				"   MissingType[TYPE_REF]{missing.MissingType, missing, Lmissing.MissingType;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}

}
