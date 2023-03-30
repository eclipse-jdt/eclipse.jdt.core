/*******************************************************************************
 * Copyright (c) 2020, 2021 IBM and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import junit.framework.Test;

public class CompletionTests14 extends AbstractJavaModelCompletionTests {

	static {
		//		TESTS_NAMES = new String[]{"test034"};
	}

	public CompletionTests14(String name) {
		super(name);
	}

	public void setUpSuite() throws Exception {
		if (COMPLETION_PROJECT == null)  {
			COMPLETION_PROJECT = setUpJavaProject("Completion", "16");
		} else {
			setUpProjectCompliance(COMPLETION_PROJECT, "16");
		}
		COMPLETION_PROJECT.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
		super.setUpSuite();
	}

	public static Test suite() {
		return buildModelTestSuite(CompletionTests14.class);
	}

	public void test001() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public record Point() imple {\n" +
				"}");
		this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "imple";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"implements[KEYWORD]{implements, null, null, implements, null, 49}",
				requestor.getResults());

	}

	public void test002() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public record Point() exte {\n" +
				"}");
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			String str = this.workingCopies[0].getSource();
			String completeBehind = "exte";
			int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults("", requestor.getResults());
		}finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void test003() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public record Point<U>() imple {\n" +
				"}");
		this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "imple";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"implements[KEYWORD]{implements, null, null, implements, null, 49}",
				requestor.getResults());
	}
	public void test004() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public record Point<U>() exte {\n" +
				"}");
		this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "exte";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("", requestor.getResults());
	}

	public void test005() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public record Point<U extends Thread>() imple {\n" +
				"}");
		this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "imple";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"implements[KEYWORD]{implements, null, null, implements, null, 49}",
				requestor.getResults());

	}
	public void test006() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public record Point<U extends Thread>() /*here*/exte {\n" +
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/exte";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("", requestor.getResults());
	}

	public void test007() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy("/Completion/src/Point.java",
				"public record Point(int comp) imple {\n" + "}");
		this.workingCopies[0].getJavaProject(); // assuming single project for all working copies

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "imple";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("implements[KEYWORD]{implements, null, null, implements, null, 49}", requestor.getResults());

	}
	public void test008() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public record Point(int comp) exte {\n" +
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "exte";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("", requestor.getResults());
	}

	public void test009() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public record Point<U>(int comp) imple {\n" +
				"}");
		this.workingCopies[0].getJavaProject(); //assuming single project for all working copies

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "imple";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"implements[KEYWORD]{implements, null, null, implements, null, 49}",
				requestor.getResults());
	}

	public void test0010() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public record Point<U>(int comp) exte {\n" +
				"}");
		this.workingCopies[0].getJavaProject(); //assuming single project for all working copies

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "exte";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("", requestor.getResults());
	}

	public void test011() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public record Point<U extends Thread>(int comp) imple {\n" +
				"}");
		IJavaProject javaProject = this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		String old = javaProject.getOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
			CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
			requestor.allowAllRequiredProposals();
			String str = this.workingCopies[0].getSource();
			String completeBehind = "imple";
			int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
			this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
			assertResults(
					"implements[KEYWORD]{implements, null, null, implements, null, 49}",
					requestor.getResults());
		}
		finally {
			javaProject.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, old);
		}
	}

	public void test012() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public record Point<U extends Thread>(int comp) /*here*/exte {\n" +
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/exte";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("", requestor.getResults());
	}

	public void test013() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[1] = getWorkingCopy(
				"/Completion/src/mypack1/rrr.java",
				"package mypack1;\n" +
						"public record rrr() {\n" +
				"}");
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/mypack1/MyClass.java",
				"package mypack1;\n" +
						"public class MyClass extends /*here*/rr {\n" +
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/rr";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("", requestor.getResults());
	}

	public void test0014() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"/**\n" +
						" * \n" +
						" * @par \n" +
						" *\n"+
						" */\n" +
						"public record Point()  {\n" +
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "par";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("", requestor.getResults());
	}

	public void _test0015() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"/**\n" +
						" * \n" +
						" * @par \n" +
						" *\n"+
						" */\n" +
						"//public record Point(int a)  {\n" +
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "par";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"param[JAVADOC_BLOCK_TAG]{@param, null, null, param, null, 38}",
				requestor.getResults());
	}

	public void testBug560781() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public record  Point(int comp_) { \n" +
						"}\n"+
						"class MyClass extends /*here*/Poin	\n"
				);

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/Poin";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("", requestor.getResults());
	}
	public void testBug564828_1() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public reco {\n" +
				"}");
		this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "reco";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"record[RESTRICTED_IDENTIFIER]{record, null, null, record, null, 49}",
				requestor.getResults());

	}

	//check if local variable record shows up
	public void testBug564828_2() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Point.java",
				"public class Point {\n" +
				"private void method(){\n" +
				"int record;\n" +
				"{\n" +
				" /*here*/rec\n" +
				"}\n" +
				"}\n" +

				"}");
		this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/rec";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"Record[TYPE_REF]{Record, java.lang, Ljava.lang.Record;, null, null, 42}\n"+
				"record[LOCAL_VARIABLE_REF]{record, null, I, record, null, 52}",
				requestor.getResults());

	}
	// Complete with "." after a text block
	public void testBug553097_1() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
						"long count = \"\"\"\n"
						+ "			aa\n"
						+ "			\n"
						+ "			\"\"\".len\n" +
						"}\n" +

				"}");
		this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = ".len";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"length[METHOD_REF]{length(), Ljava.lang.String;, ()I, length, null, "+
						(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_NON_STATIC + R_NON_RESTRICTED) +"}",
						requestor.getResults());

	}
	// Same as above, but text block inside a method
	public void testBug553097_2() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n"
						+ "  private void method(){\n"
						+ "    long count = \"\"\"\n"
						+ "			aa\n"
						+ "			\n"
						+ "			\"\"\".len\n"
						+ "  }\n"
						+ "}");
		this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = ".len";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"length[METHOD_REF]{length(), Ljava.lang.String;, ()I, length, null, "+
						(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_NON_STATIC + R_NON_RESTRICTED) +"}",
						requestor.getResults());

	}
	public void testBug553097_3() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n"
						+ "  private void method(){\n"
						+ "    String d_ef = \"\"\"\n"
						+ "			def\n"
						+ "			\"\"\";\n"
						+ "    String abc = \"\"\"\n"
						+ "			abc\n"
						+ "			\"\"\" + d_\n"
						+ "  }\n"
						+ "}");
		this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "d_";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"d_ef[LOCAL_VARIABLE_REF]{d_ef, null, Ljava.lang.String;, d_ef, null, "+
						(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) +"}",
						requestor.getResults());

	}
	public void testBug553097_4() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n"
						+ "  private void method(String a_rg){\n"
						+ "    String d_ef = \"\"\"\n"
						+ "			def\n"
						+ "			\"\"\" + a_ +\n"
						+ "     \"\"\"\n"
						+ "			abc\n"
						+ "			\"\"\";\n"
						+ "  }\n"
						+ "}");
		this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "a_";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"a_rg[LOCAL_VARIABLE_REF]{a_rg, null, Ljava.lang.String;, a_rg, null, "+
						(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) +"}",
						requestor.getResults());

	}
	public void testBug553097_5() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n"
						+ "  private void method(String a_rg){\n"
						+ "    var d_ef = \"\"\"\n"
						+ "			def\n"
						+ "			\"\"\";\n"
						+ "     d_\n"
						+ "  }\n"
						+ "}");
		this.workingCopies[0].getJavaProject(); //assuming single project for all working copies
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "d_";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"d_ef[LOCAL_VARIABLE_REF]{d_ef, null, Ljava.lang.String;, d_ef, null, "+
						(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE  + R_UNQUALIFIED + R_NON_RESTRICTED) +"}",
						requestor.getResults());

	}

	public void testGH667_CompletionOnRecordComponent_SimpleTypeName() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Person.java",
				"public record Person(Nam) {\n"
						+ "}\n");
		this.workingCopies[1] = getWorkingCopy(
				"/Completion/src/Name.java",
				"public class Name {\n"
						+ "}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "Nam";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"Name[TYPE_REF]{Name, , LName;, null, null, null, null, [21, 24], "
						+ (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	}

	public void testGH667_CompletionOnRecordComponent_QualifiedTypeName() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Person.java",
				"public record Person(pack2.P) {\n"
						+ "}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "pack2.P";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"PX[TYPE_REF]{pack2.PX, pack2, Lpack2.PX;, null, null, null, null, [21, 28], "
						+ (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_QUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	}

	public void testGH667_CompletionOnRecordComponent_VariableName() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Person.java",
				"public record Person(Name ) {\n"
						+ "}\n");
		this.workingCopies[1] = getWorkingCopy(
				"/Completion/src/Name.java",
				"public class Name {\n"
						+ "}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "Name ";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"name[VARIABLE_DECLARATION]{name, null, LName;, null, null, name, null, [26, 26], "
						+ (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	}

	public void testGH757_CompletionOnTypeNameAboveRecordDeclaration() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Person.java",
				"public class Person {\n"
						+ "private Name name = new Name \n"
						+ "record Age(int value){};"
						+ "}\n");
		this.workingCopies[1] = getWorkingCopy(
				"/Completion/src/Name.java",
				"public class Name {\n"
						+ "}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "new Name";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"Name[TYPE_REF]{Name, , LName;, null, null, null, null, [46, 50], "
						+ (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED
								+ R_EXACT_NAME + R_EXACT_EXPECTED_TYPE)
						+ "}",
				requestor.getResults());
	}

	public void testGH697_CompletionOnSwitchExpressionInsideLambda() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[1] = getWorkingCopy(
				"/Completion/src/State.java",
				"public enum State {\n"
						+ "	BLOCKED, RUNNING;"
						+ "}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/Completion/src/Func.java",
				"public interface Func<I,O> {\n"
						+ "	O apply(I input);"
						+ "}\n");

		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n"
						+ "static String toString(State state) {\n"
						+ " 	return Switch.<State, String>transform(state, st -> {\n"
						+ " 		return switch(st) { \n"
						+ " 			case B\n"
						+ " 		};\n"
						+ " 	});\n"
						+ "}\n"
						+ "static <I, O> O transform(I input, Func<I, O> t) {\n"
						+ "	return null;"
						+ "}\n"
						+ "}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "case B";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"BLOCKED[FIELD_REF]{BLOCKED, LState;, LState;, null, null, BLOCKED, null, [149, 150], "
						+ (R_DEFAULT + R_ENUM + R_ENUM_CONSTANT + R_RESOLVED + R_INTERESTING + R_CASE
								+ R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE)
						+ "}",
				requestor.getResults());
	}

	public void testGH697_CompletionOnCompletedSwitchExpressionInsideLambda() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[1] = getWorkingCopy(
				"/Completion/src/State.java",
				"public enum State {\n"
						+ "	BLOCKED, RUNNING;"
						+ "}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/Completion/src/Func.java",
				"public interface Func<I,O> {\n"
						+ "	O apply(I input);"
						+ "}\n");

		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n"
						+ "static String toString(State state) {\n"
						+ " 	return Switch.<State, String>transform(state, st -> {\n"
						+ " 		return switch(st) { \n"
						+ " 			case BLOCK -> \"blocked\";\n"
						+ " 		};\n"
						+ " 	});\n"
						+ "}\n"
						+ "static <I, O> O transform(I input, Func<I, O> t) {\n"
						+ "	return null;"
						+ "}\n"
						+ "}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "case B";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"BLOCKED[FIELD_REF]{BLOCKED, LState;, LState;, null, null, BLOCKED, null, [149, 154], "
						+ (R_DEFAULT + R_ENUM + R_ENUM_CONSTANT + R_RESOLVED + R_INTERESTING + R_CASE
								+ R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE)
						+ "}",
				requestor.getResults());
	}

	public void testGH697_CompletionOnCompletedSwitchExpressionInitializationInsideLambda() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[1] = getWorkingCopy(
				"/Completion/src/State.java",
				"public enum State {\n"
						+ "	BLOCKED, RUNNING;"
						+ "}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/Completion/src/Func.java",
				"public interface Func<I,O> {\n"
						+ "	O apply(I input);"
						+ "}\n");

		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n"
						+ "static String toString(State state) {\n"
						+ " 	return Switch.<State, String>transform(state, st -> {\n"
						+ " 		String value = switch(st) { \n"
						+ " 			case BLOCK -> \"blocked\";\n"
						+ " 		};\n"
						+ "			return value;\n"
						+ " 	});\n"
						+ "}\n"
						+ "static <I, O> O transform(I input, Func<I, O> t) {\n"
						+ "	return null;"
						+ "}\n"
						+ "}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "case B";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"BLOCKED[FIELD_REF]{BLOCKED, LState;, LState;, null, null, BLOCKED, null, [157, 162], "
						+ (R_DEFAULT + R_ENUM + R_ENUM_CONSTANT + R_RESOLVED + R_INTERESTING + R_CASE
								+ R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE)
						+ "}",
				requestor.getResults());
	}

	public void testGH697_CompletionOnCompletedSwitchExpressionAssignmentInsideLambda() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[1] = getWorkingCopy(
				"/Completion/src/State.java",
				"public enum State {\n"
						+ "	BLOCKED, RUNNING;"
						+ "}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/Completion/src/Func.java",
				"public interface Func<I,O> {\n"
						+ "	O apply(I input);"
						+ "}\n");

		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n"
						+ "static String toString(State state) {\n"
						+ " 	return Switch.<State, String>transform(state, st -> {\n"
						+ " 	String value = null;\n"
						+ "		value = switch(st) { \n"
						+ " 			case BLOCK -> \"blocked\";\n"
						+ " 		};\n"
						+ "			return value;\n"
						+ " 	});\n"
						+ "}\n"
						+ "static <I, O> O transform(I input, Func<I, O> t) {\n"
						+ "	return null;"
						+ "}\n"
						+ "}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "case B";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"BLOCKED[FIELD_REF]{BLOCKED, LState;, LState;, null, null, BLOCKED, null, [172, 177], "
						+ (R_DEFAULT + R_ENUM + R_ENUM_CONSTANT + R_RESOLVED + R_INTERESTING + R_CASE
								+ R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE)
						+ "}",
				requestor.getResults());
	}

	public void testGH697_CompletionOnSwitchExpressionWithoutReturn() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[1] = getWorkingCopy(
				"/Completion/src/State.java",
				"public enum State {\n"
						+ "	BLOCKED, RUNNING;"
						+ "}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/Completion/src/Func.java",
				"public interface Func<I,O> {\n"
						+ "	O apply(I input);"
						+ "}\n");

		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n"
						+ "static String toString(State state) {\n"
						+ " 	return Switch.<State, String>transform(state, \n"
						+ " 		st -> switch(st) { \n"
						+ " 			case B\n"
						+ " 		});\n"
						+ "}\n"
						+ "static <I, O> O transform(I input, Func<I, O> t) {\n"
						+ "	return null;"
						+ "}\n"
						+ "}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "case B";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"BLOCKED[FIELD_REF]{BLOCKED, LState;, LState;, null, null, BLOCKED, null, [141, 142], "
						+ (R_DEFAULT + R_ENUM + R_ENUM_CONSTANT + R_RESOLVED + R_INTERESTING + R_CASE
								+ R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE)
						+ "}",
				requestor.getResults());
	}

	public void testGH697_CompletionOnSwitchExpressionAsFirstParameter() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[1] = getWorkingCopy(
				"/Completion/src/State.java",
				"public enum State {\n"
						+ "	BLOCKED, RUNNING;"
						+ "}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/Completion/src/Func.java",
				"public interface Func<I,O> {\n"
						+ "	O apply(I input);"
						+ "}\n");

		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n"
						+ "static String toString(State state) {\n"
						+ " 	return Switch.<State, String>transform(\n"
						+ " 		st -> switch(st) { \n"
						+ " 			case B\n"
						+ " 		}, state);\n"
						+ "}\n"
						+ "static <I, O> O transform(Func<I, O> t, I input) {\n"
						+ "	return null;"
						+ "}\n"
						+ "}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "case B";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"BLOCKED[FIELD_REF]{BLOCKED, LState;, LState;, null, null, BLOCKED, null, [134, 135], "
						+ (R_DEFAULT + R_ENUM + R_ENUM_CONSTANT + R_RESOLVED + R_INTERESTING + R_CASE
								+ R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE)
						+ "}",
				requestor.getResults());
	}

	public void testGH697_CompletionOnSwitchExpressionAsFirstParameter_OnInitializer() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[1] = getWorkingCopy(
				"/Completion/src/State.java",
				"public enum State {\n"
						+ "	BLOCKED, RUNNING;"
						+ "}\n");
		this.workingCopies[2] = getWorkingCopy(
				"/Completion/src/Func.java",
				"public interface Func<I,O> {\n"
						+ "	O apply(I input);"
						+ "}\n");

		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/Switch.java",
				"public class Switch {\n"
						+ "private static String S = Switch.transform(\n"
						+ " 		st -> switch(st) { \n"
						+ " 			case B\n"
						+ " 		}, State.BLOCKED);\n"
						+ "static <I, O> O transform(Func<I, O> t, I input) {\n"
						+ "	return null;"
						+ "}\n"
						+ "}\n");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "case B";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults(
				"BLOCKED[FIELD_REF]{BLOCKED, LState;, LState;, null, null, BLOCKED, null, [98, 99], "
						+ (R_DEFAULT + R_ENUM + R_ENUM_CONSTANT + R_RESOLVED + R_INTERESTING + R_CASE
								+ R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE)
						+ "}",
				requestor.getResults());
	}

}
