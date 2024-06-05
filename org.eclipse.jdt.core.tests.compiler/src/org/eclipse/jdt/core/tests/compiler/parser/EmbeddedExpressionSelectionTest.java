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

package org.eclipse.jdt.core.tests.compiler.parser;

import org.eclipse.jdt.core.JavaModelException;
import junit.framework.Test;

public class EmbeddedExpressionSelectionTest extends AbstractSelectionTest {
	static {
		//		TESTS_NUMBERS = new int[] { 1 };
		//		TESTS_NAMES = new String[] { "test003" };
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(EmbeddedExpressionSelectionTest.class, F_21);
	}

	public EmbeddedExpressionSelectionTest(String testName) {
		super(testName);
	}
	public void test001() throws JavaModelException {
		String string =
				"""
			public class X {
			    static int field = 1234;
				 public static void main(String [] args) {
			        String s = STR."Field = \\{field}";
			    }
			}
			""";

		String selection = "field";
		String selectKey = "<SelectOnName:";
		String expectedSelection = selectKey + selection + ">";

		String selectionIdentifier = "field";
		String expectedUnitDisplayString =
				"""
			public class X {
			  static int field;
			  <clinit>() {
			  }
			  public X() {
			  }
			  public static void main(String[] args) {
			    String s = STR."Field = \\{<SelectOnName:field>}";
			  }
			}
			""";
		String expectedReplacedSource = "field";
		String testName = "X.java";

		int selectionStart = string.lastIndexOf(selection);
		int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

		checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
				selectionIdentifier, expectedReplacedSource, testName);
	}
	public void test002() throws JavaModelException {
		String string =
				"""
			public class X {
			  public static void main(String[] args) {
			    String[] fruit = { "apples", "oranges", "peaches" };
			    String s = STR."\\{fruit[0]}, \\{STR."\\{/*here*/fruit[1]}, \\{fruit[2]}"}\\u002e";
			    System.out.println(s);
			  }
			}""";

		String selection = "/*here*/fruit";
		String expectedSelection = """
			<SelectOnName:\
			fruit\
			>""";

		String selectionIdentifier = "fruit";
		String expectedUnitDisplayString =
				"""
			public class X {
			  public X() {
			  }
			  public static void main(String[] args) {
			    String[] fruit;
			    String s = STR."\\{fruit[0]}, \\{STR."\\{<SelectOnName:fruit>[1]}, \\{fruit[2]}"}.";
			  }
			}
			""";
		String expectedReplacedSource = "fruit";
		String testName = "X.java";

		int selectionStart = string.lastIndexOf(selection) + "/*here*/".length();
		int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

		checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
				selectionIdentifier, expectedReplacedSource, testName);
	}
	public void test003() throws JavaModelException {
		String string =
				"""
				public class X<R> {
					@SuppressWarnings("nls")
					public static void main(String[] args) {
						String name    = "Joan Smith";
						String phone   = "555-123-4567";
						String address = "1 Maple Drive, Anytown";
						String doc = STR.\"""
								{
								    "name":    "\\{STR."\\{name}"}",
								    "phone":   "\\{phone}",
								    "address": "\\{address}"
								};\""";
						System.out.println(doc);
					}
				}""";

		String selection = "name";
		String expectedSelection = """
			<SelectOnName:\
			name\
			>""";

		String selectionIdentifier = "name";
		String expectedUnitDisplayString =
                """
                public class X<R> {
                  public X() {
                  }
                  public static @SuppressWarnings(\"nls\") void main(String[] args) {
                    String name;
                    String phone;
                    String address;
                    String doc = STR.\"""\n{\\n    \\\"name\\\":    \\\"\\{STR.\"\\{<SelectOnName:name>}\"}\\\",\\n    \\\"phone\\\":   \\\"\\{phone}\\\",\\n    \\\"address\\\": \\\"\\{address}\\\"\\n};\""";
                  }
                }
                """;
		String expectedReplacedSource = "name";
		String testName = "X.java";

		int selectionStart = string.lastIndexOf(selection);
		int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

		checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
				selectionIdentifier, expectedReplacedSource, testName);
	}
	public void test004() throws JavaModelException {
		String str =
				"""
				public class X<R> {
					public static void main(String[] args) {
						System.out.println(\"""
							Hello\""");
					}
				}""";

		String selectionStartBehind = "System.out.";
		String selectionEndBehind = "println";

		String expectedCompletionNodeToString = "<SelectOnMessageSend:System.out.println(\"\"\"\nHello\"\"\")>";
		String completionIdentifier = "println";
		String expectedUnitDisplayString =
			"""
			public class X<R> {
			  public X() {
			  }
			  public static void main(String[] args) {
			    <SelectOnMessageSend:System.out.println(\"""
			Hello\""")>;
			  }
			}
			""";
		String expectedReplacedSource = "System.out.println(\"\"\"\n\t\t\tHello\"\"\")";
		String testName = "<select message send>";

		int selectionStart = str.indexOf(selectionStartBehind) + selectionStartBehind.length();
		int selectionEnd = str.indexOf(selectionEndBehind) + selectionEndBehind.length() - 1;

		this.checkMethodParse(
			str.toCharArray(),
			selectionStart,
			selectionEnd,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
	}
	// test selection after template
	public void test005() throws JavaModelException {
		String string =
				"""
			public class X {
			  public static void main(String[] args) {
			    String[] fruit = { "apples", "oranges", "peaches" };
			    String s = STR."\\{fruit[0]}, \\{STR."\\{/*here*/fruit[1]}, \\{fruit[2]}"}\\u002e";
			    System.out.println(s);
			    System.out.println(s.hashCode());
			  }
			}""";

		String selection = "hashCode";
		String expectedSelection = "<SelectOnMessageSend:s.hashCode()>";

		String selectionIdentifier = "hashCode";
		String expectedUnitDisplayString =
				"""
			public class X {
			  public X() {
			  }
			  public static void main(String[] args) {
			    String[] fruit;
			    String s;
			    System.out.println(<SelectOnMessageSend:s.hashCode()>);
			  }
			}
			""";
		String expectedReplacedSource = "s.hashCode()";
		String testName = "X.java";

		int selectionStart = string.lastIndexOf(selection);
		int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

		checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
				selectionIdentifier, expectedReplacedSource, testName);
	}
}