/*******************************************************************************
 * Copyright (c) 2020, 2021 IBM Corporation and others.
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

import junit.framework.Test;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

//java 15 scenarios run with java 16 compliance
public class CompletionTests16_1 extends AbstractJavaModelCompletionTests {

	static {
		// TESTS_NAMES = new String[]{"test034"};
	}

	public CompletionTests16_1(String name) {
		super(name);
	}

	public void setUpSuite() throws Exception {
		if (COMPLETION_PROJECT == null) {
			COMPLETION_PROJECT = setUpJavaProject("Completion", "17");
		} else {
			setUpProjectCompliance(COMPLETION_PROJECT, "17");
		}
		COMPLETION_PROJECT.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.DISABLED);
		super.setUpSuite();
	}

	public static Test suite() {
		return buildModelTestSuite(CompletionTests16_1.class);
	}

	// completion for sealed
	public void test001() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/X.java",
				"public seal class X permits Y{\n" + " public static void main(String[] args){\n"
						+ "    System.out.println(100);\n}\n}\n" + "	final class Y extends X {}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "seal";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("sealed[RESTRICTED_IDENTIFIER]{sealed, null, null, sealed, null, 49}", requestor.getResults());

	}

	// completion for non-sealed
	public void test002() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/X.java",
				"public non class X {\n" + " public static void main(String[] args){\n"
						+ "    System.out.println(100);\n}\n}\n" + "	final class Y extends X {}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "non";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("non-sealed[RESTRICTED_IDENTIFIER]{non-sealed, null, null, non-sealed, null, 49}",
				requestor.getResults());

	}

	// completion for sealed if sealed already present
	public void test003() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/X.java",
				"public sealed /*here*/seal class X permits Y{\n" + " public static void main(String[] args){\n"
						+ "    System.out.println(100);\n}\n}\n" + "	final class Y extends X {}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = " /*here*/seal";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("", requestor.getResults());

	}

	// completion for non-sealed if non-sealed already present
	public void test004() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/X.java",
				"public non-sealed /*here*/non class X {\n" + " public static void main(String[] args){\n"
						+ "    System.out.println(100);\n}\n}\n" + "	final class Y extends X {}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/non";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("", requestor.getResults());

	}

	// completion for sealed if nonsealed already present
	public void test005() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/X.java",
				"public non-sealed /*here*/seal class X permits Y{\n" + " public static void main(String[] args){\n"
						+ "    System.out.println(100);\n}\n}\n" + "	final class Y extends X {}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = " /*here*/seal";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("", requestor.getResults());

	}

	// completion for non-sealed if sealed already present
	public void test006() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/X.java",
				"public sealed /*here*/non class X {\n" + " public static void main(String[] args){\n"
						+ "    System.out.println(100);\n}\n}\n" + "	final class Y extends X {}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/non";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("", requestor.getResults());

	}

	// check if local variable sealed shows up, note that non-sealed is not a valid
	// variable name
	public void test007() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/Point.java", "public class Point {\n"
				+ "private void method(){\n" + "int sealed;\n" + "{\n" + " /*here*/sea\n" + "}\n" + "}\n" +

				"}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/sea";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("sealed[LOCAL_VARIABLE_REF]{sealed, null, I, sealed, null, 52}", requestor.getResults());

	}

	// check if permit content assist works
	public void test008() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/Point.java", "public sealed  class Point per{\n" +

				"}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "per";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("permits[RESTRICTED_IDENTIFIER]{permits, null, null, permits, null, 49}", requestor.getResults());

	}

	// check if permit content doesnt work if no sealed
	public void test009() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/Point.java", "public   class Point per{\n" +

				"}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "per";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("", requestor.getResults());

	}

	// check if permit content if the class has extends
	public void test010() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/X.java",
				"public sealed  class X extends Object per Y{\n" + " public static void main(String[] args){\n"
						+ "    System.out.println(100);\n}\n}\n" + "	final class Y extends X {}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "per";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("permits[RESTRICTED_IDENTIFIER]{permits, null, null, permits, null, 49}", requestor.getResults());

	}

}
