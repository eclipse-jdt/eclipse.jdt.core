/*******************************************************************************
* Copyright (c) 2023 Advantest Europe GmbH and others.
*
* This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
*
https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import junit.framework.Test;

public class EmbeddedExpressionCompletionTests extends AbstractJavaModelCompletionTests {

	static {
		// TESTS_NAMES = new String[]{"test034"};
	}

	public EmbeddedExpressionCompletionTests(String name) {
		super(name);
	}

	public void setUpSuite() throws Exception {
		if (COMPLETION_PROJECT == null) {
			COMPLETION_PROJECT = setUpJavaProject("Completion", "21");
		} else {
			setUpProjectCompliance(COMPLETION_PROJECT, "21");
		}
		super.setUpSuite();
		COMPLETION_PROJECT.setOption(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES, JavaCore.ENABLED);
	}

	public static Test suite() {
		return buildModelTestSuite(EmbeddedExpressionCompletionTests.class);
	}

	public void test001() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n" +
				"		static String name = \"Jay\";\n" +
				"		public static void main(String[] args) {\n" +
				"			String s = STR.\"Hello \\{/*here*/na}\";\n" +
				"			System.out.println(s);\n" +
				"		}\n" +
				"}\n"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "/*here*/na";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("name[FIELD_REF]{name, LX;, Ljava.lang.String;, name, null, 52}",
				requestor.getResults());

	}
	public void test002() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n"
				+ "  public static void main(String[] args) {\n"
				+ "    String[] fruit = { \"apples\", \"oranges\", \"peaches\" };\n"
				+ "    String s = STR.\"\\{fruit[0]}, \\{STR.\"\\{/*here*/fruit[1].has}, \\{fruit[2]}\"}\\u002e\";\n"
				+ "    System.out.println(s);\n"
				+ "  }\n"
				+ "}"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "has";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 60}",
				requestor.getResults());

	}
	// test completion after template
	public void test003() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n"
				+ "  public static void main(String[] args) {\n"
				+ "    String[] fruit = { \"apples\", \"oranges\", \"peaches\" };\n"
				+ "    String s = STR.\"\\{fruit[0]}, \\{STR.\"\\{fruit[1]}, \\{fruit[2]}\"}\\u002e\";\n"
				+ "    System.out.println(s);\n"
				+ "    System.out.println(s.has);\n"
				+ "  }\n"
				+ "}"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "has";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 90}",
				requestor.getResults());

	}
	// test completion before template
	public void test004() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"public class X {\n"
				+ "  public static void main(String[] args) {\n"
				+ "    System.out.println(args[0].has);\n"
				+ "    String[] fruit = { \"apples\", \"oranges\", \"peaches\" };\n"
				+ "    String s = STR.\"\\{fruit[0]}, \\{STR.\"\\{/*here*/fruit[1].has}, \\{fruit[2]}\"}\\u002e\";\n"
				+ "    System.out.println(s);\n"
				+ "  }\n"
				+ "}"
				);
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "has";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, 90}",
				requestor.getResults());

	}
	// completion before text block template
	public void test005() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
				public class X {
					@SuppressWarnings("preview")
					public static void main(String[] args) {
						String firstName = "Bill", lastName = "Duck"; //$NON-NLS-1$ //$NON-NLS-2$
						System.out.println(STR."\\{firstName} \\{lastName}");
						System.out.println(STR."\\{firstN} \\{lastName}");
						String titleStr = "My Web Page"; //$NON-NLS-1$
						String html = STR.\"""
						     <title>\\{titleStr}</title>
				      	\""";
						System.out.println(html);
					}
				}""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "firstN";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("firstName[LOCAL_VARIABLE_REF]{firstName, null, Ljava.lang.String;, firstName, null, 52}",
				requestor.getResults());
	}
	// completion after text block template
	public void test006() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
				public class X {
					@SuppressWarnings("preview")
					public static void main(String[] args) {
						String firstName = "Bill", lastName = "Duck"; //$NON-NLS-1$ //$NON-NLS-2$
						System.out.println(STR."\\{firstName} \\{lastName}");
						String titleStr = "My Web Page"; //$NON-NLS-1$
						String html = STR.\"""
						      <title>\\{titleS}</title>
				      	\""";
						System.out.println(html);
						System.out.println(STR."\\{firstN} \\{lastName}");
					}
				}""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "firstN";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("firstName[LOCAL_VARIABLE_REF]{firstName, null, Ljava.lang.String;, firstName, null, 52}",
				requestor.getResults());
	}
	// completion before inside a text block template on the first expression
	public void test007() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
				public class X {
					@SuppressWarnings("preview")
					public static void main(String[] args) {
						String firstName = "Bill", lastName = "Duck"; //$NON-NLS-1$ //$NON-NLS-2$
						System.out.println(STR."\\{firstName} \\{lastName}");
						String greeting = "Hello";
						String title = "Mr";
						String html = STR.\"""
						      <title>\\{greet} \\{title}. \\{lastName}!</title>
				      	\""";
						System.out.println(html);
					}
				}""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "greet";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("greeting[LOCAL_VARIABLE_REF]{greeting, null, Ljava.lang.String;, greeting, null, 52}",
				requestor.getResults());
	}
	// completion before inside a text block template on the last expression
	public void test008() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
				public class X {
					@SuppressWarnings("preview")
					public static void main(String[] args) {
						String firstName = "Bill", lastName = "Duck"; //$NON-NLS-1$ //$NON-NLS-2$
						System.out.println(STR."\\{firstName} \\{lastName}");
						String greeting = "Hello";
						String title = "Mr";
						String html = STR.\"""
						      <title>\\{greeting} \\{title}. \\{lastN}!</title>
				      	\""";
						System.out.println(html);
					}
				}""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "lastN";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("lastName[LOCAL_VARIABLE_REF]{lastName, null, Ljava.lang.String;, lastName, null, 52}",
				requestor.getResults());
	}
	// completion before inside a text block template on the middle expression
	public void test009() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
				public class X {
					@SuppressWarnings("preview")
					public static void main(String[] args) {
						String firstName = "Bill", lastName = "Duck"; //$NON-NLS-1$ //$NON-NLS-2$
						System.out.println(STR."\\{firstName} \\{lastName}");
						String greeting = "Hello";
						String nameTitle = "Mr";
						String html = STR.\"""
						      <title>\\{greeting} \\{nameT}. \\{lastName}!</title>
				      	\""";
						System.out.println(html);
					}
				}""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "nameT";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("nameTitle[LOCAL_VARIABLE_REF]{nameTitle, null, Ljava.lang.String;, nameTitle, null, 52}",
				requestor.getResults());
	}
	// completion on a string template expression
	public void _test010() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
				import java.lang.StringTemplate.STR;
				public class X {
					@SuppressWarnings("preview")
					public static void main(String[] args) {
						String firstName = "Bill", lastName = "Duck"; //$NON-NLS-1$ //$NON-NLS-2$
						System.out.println(STR."\\{firstName} \\{lastName}".toStr);
					}
				}""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "toStr";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 90}",
				requestor.getResults());
	}
	// completion on a textblock template expression
	public void _test011() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
				public class X {
					@SuppressWarnings("preview")
					public static void main(String[] args) {
						String firstName = "Bill", lastName = "Duck"; //$NON-NLS-1$ //$NON-NLS-2$
						System.out.println(STR."\\{firstName} \\{lastName}");
						String greeting = "Hello";
						String nameTitle = "Mr";
						String html = STR.\"""
						      <title>\\{greeting} \\{nameT}. \\{lastName}!</title>
				      	\""".toStr;
						System.out.println(html);
					}
				}""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "toStr";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 90}",
				requestor.getResults());
	}
	// completion inside a textblock template expression
	public void test012() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
				public class X {
					@SuppressWarnings("preview")
					public static void main(String[] args) {
						String firstName = "Bill", lastName = "Duck"; //$NON-NLS-1$ //$NON-NLS-2$
						System.out.println(STR."\\{firstName} \\{lastName}");
						String greeting = "Hello";
						String nameTitle = "Mr";
						String html = STR.\"""
						      <title>\\{greeting} \\{nameTitle.toStr}. \\{lastName}!</title>
				      	\""";
						System.out.println(html);
					}
				}""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "toStr";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, 60}",
				requestor.getResults());
	}
	public void test013() throws JavaModelException {
		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/X.java",
				"""
				public class X<R> {
					@SuppressWarnings("nls")
					public static void main(String[] args) {
						String name = "Joan Smith";
						String phone = "555-123-4567";
						String address = "1 Maple Drive, Anytown";
						String doc = STR.\"""
				    {
				        "name":    "\\{
				          STR.\"""
				          \\{name}\"""}",
				        "phone":   "\\{phone}",
				        "address": "\\{addre}"
				    };\""";
						System.out.println(doc);
					}
				}""");
		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		requestor.allowAllRequiredProposals();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "addre";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
		assertResults("address[LOCAL_VARIABLE_REF]{address, null, Ljava.lang.String;, address, null, 52}",
				requestor.getResults());
	}
}
