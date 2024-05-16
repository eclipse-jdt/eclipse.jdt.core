/*******************************************************************************
 * Copyright (c) 2019, 2023 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.parser;

import org.eclipse.jdt.core.JavaModelException;

import junit.framework.Test;

public class SelectionParserTest12 extends AbstractSelectionTest {
static {
//		TESTS_NUMBERS = new int[] { 1 };
//		TESTS_NAMES = new String[] { "test005" };
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(SelectionParserTest12.class, F_12);
}

public SelectionParserTest12(String testName) {
	super(testName);
}
/*
 * Multi constant case statement with ':', selection node is the string constant
 */
public void test001() throws JavaModelException {
	String string =  """
		public class X {
		static final String ONE="One", TWO = "Two", THREE="Three";
		  public static void foo(String num) {
		 	 switch (num) {
			   case ONE, TWO, THREE:
				 System.out.println(num);
				 break;
		    }\
		  }
		}""";

	String selection = "ONE";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "ONE";
	String expectedUnitDisplayString =
			"""
		public class X {
		  static final String ONE;
		  static final String TWO;
		  static final String THREE;
		  <clinit>() {
		  }
		  public X() {
		  }
		  public static void foo(String num) {
		    {
		      switch (num) {
		      case <SelectOnName:ONE> :
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "ONE";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with ':', selection node is the first enum constant
 */
public void test002() throws JavaModelException {
	String string =  """
		public class X {
		  public static void foo(Num num) {
		 	 switch (num) {
			   case ONE, TWO, THREE:
				 System.out.println(num);
				 break;
		    }\
		  }
			enum Num { ONE, TWO, THREE;}
		}""";

	String selection = "ONE";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "ONE";
	String expectedUnitDisplayString =
			"""
		public class X {
		  enum Num {
		    ONE(),
		    TWO(),
		    THREE(),
		    <clinit>() {
		    }
		    Num() {
		    }
		  }
		  public X() {
		  }
		  public static void foo(Num num) {
		    {
		      switch (num) {
		      case <SelectOnName:ONE> :
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "ONE";
	String testName = "X.java";

	int selectionStart = string.indexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with ':', selection node is the second string constant
 */
public void test003() throws JavaModelException {
	String string =  """
		public class X {
		static final String ONE="One", TWO = "Two", THREE="Three";
		  public static void foo(String num) {
		 	 switch (num) {
			   case ONE, TWO, THREE:
				 System.out.println(num);
				 break;
		    }\
		  }
		}""";

	String selection = "TWO";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "TWO";
	String expectedUnitDisplayString =
			"""
		public class X {
		  static final String ONE;
		  static final String TWO;
		  static final String THREE;
		  <clinit>() {
		  }
		  public X() {
		  }
		  public static void foo(String num) {
		    {
		      switch (num) {
		      case <SelectOnName:TWO> :
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "TWO";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with ':', selection node is the second enum constant
 */
public void test004() throws JavaModelException {
	String string =  """
		public class X {
		  public static void foo(Num num) {
		 	 switch (num) {
			   case ONE, TWO, THREE:
				 System.out.println(num);
				 break;
		    }\
		  }
			enum Num { ONE, TWO, THREE;}
		}""";

	String selection = "TWO";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "TWO";
	String expectedUnitDisplayString =
			"""
		public class X {
		  enum Num {
		    ONE(),
		    TWO(),
		    THREE(),
		    <clinit>() {
		    }
		    Num() {
		    }
		  }
		  public X() {
		  }
		  public static void foo(Num num) {
		    {
		      switch (num) {
		      case <SelectOnName:TWO> :
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "TWO";
	String testName = "X.java";

	int selectionStart = string.indexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection node is the string constant
 */
public void test005() throws JavaModelException {
	String string =  """
		public class X {
		static final String ONE="One", TWO = "Two", THREE="Three";
		  public static void foo(String num) {
		 	 switch (num) {
			   case ONE, TWO, THREE ->
				 System.out.println(num);
		    }\
		  }
		}""";
	/*
	 * Note: The completion parser ignores the -> that follows and we end up creating
	 * the CaseStatement without maring it as an Expression, hence the ':' instead of the '->'
	 */
	String selection = "ONE";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";
	String selectionIdentifier = "ONE";
	String expectedUnitDisplayString =
			"""
		public class X {
		  static final String ONE;
		  static final String TWO;
		  static final String THREE;
		  <clinit>() {
		  }
		  public X() {
		  }
		  public static void foo(String num) {
		    {
		      switch (num) {
		      case <SelectOnName:ONE> :
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "ONE";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection node is the first enum constant
 */
public void test006() throws JavaModelException {
	String string =  """
		public class X {
		  public static void foo(Num num) {
		 	 switch (num) {
			   case ONE, TWO, THREE ->
				 System.out.println(num);
				 break; // illegal, but should be ignored and shouldn't matter
		    }\
		  }
			enum Num { ONE, TWO, THREE;}
		}""";

	String selection = "ONE";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "ONE";
	String expectedUnitDisplayString =
			"""
		public class X {
		  enum Num {
		    ONE(),
		    TWO(),
		    THREE(),
		    <clinit>() {
		    }
		    Num() {
		    }
		  }
		  public X() {
		  }
		  public static void foo(Num num) {
		    {
		      switch (num) {
		      case <SelectOnName:ONE> :
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "ONE";
	String testName = "X.java";

	int selectionStart = string.indexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection node is the second string constant
 */
public void test007() throws JavaModelException {
	String string =  """
		public class X {
		static final String ONE="One", TWO = "Two", THREE="Three";
		  public static void foo(String num) {
		 	 switch (num) {
			   case ONE, TWO, THREE ->
				 System.out.println(num);
				 break;
		    }\
		  }
		}""";

	String selection = "TWO";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "TWO";
	String expectedUnitDisplayString =
			"""
		public class X {
		  static final String ONE;
		  static final String TWO;
		  static final String THREE;
		  <clinit>() {
		  }
		  public X() {
		  }
		  public static void foo(String num) {
		    {
		      switch (num) {
		      case <SelectOnName:TWO> :
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "TWO";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = string.lastIndexOf(selection) + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection node is the second enum constant
 */
public void test008() throws JavaModelException {
	String string =  """
		public class X {
		  public static void foo(Num num) {
		 	 switch (num) {
			   case ONE, TWO, THREE ->
				 System.out.println(num);
				 break;
		    }\
		  }
			enum Num { ONE, TWO, THREE;}
		}""";

	String selection = "TWO";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "TWO";
	String expectedUnitDisplayString =
			"""
		public class X {
		  enum Num {
		    ONE(),
		    TWO(),
		    THREE(),
		    <clinit>() {
		    }
		    Num() {
		    }
		  }
		  public X() {
		  }
		  public static void foo(Num num) {
		    {
		      switch (num) {
		      case <SelectOnName:TWO> :
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "TWO";
	String testName = "X.java";

	int selectionStart = string.indexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection is a reference in the case block
 * which same as the switch's expression
 */
public void test009() throws JavaModelException {
	String string =  """
		public class X {
		  public static void foo(Num num_) {
		 	 switch (num_) {
			   case ONE, TWO, THREE ->
				 System.out.println(num_);
				 break;
		    }\
		  }
			enum Num { ONE, TWO, THREE;}
		}""";

	String selection = "num_";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "num_";
	String expectedUnitDisplayString =
			"""
		public class X {
		  enum Num {
		    ONE(),
		    TWO(),
		    THREE(),
		    <clinit>() {
		    }
		    Num() {
		    }
		  }
		  public X() {
		  }
		  public static void foo(Num num_) {
		    {
		      switch (num_) {
		      case THREE ->
		          <SelectOnName:num_>;
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "num_";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection is a reference in the case block
 * which is referencing a local variable defined in the case block
 */
public void test010() throws JavaModelException {
	String string =  """
		public class X {
		  public static void foo(Num num_) {
		 	 switch (num_) {
			   case ONE, TWO, THREE -> {
				 int i_j = 0;\
				 System.out.println(i_j);
				 break;\
				 }
		    }\
		  }
			enum Num { ONE, TWO, THREE;}
		}""";

	String selection = "i_j";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "i_j";
	String expectedUnitDisplayString =
			"""
		public class X {
		  enum Num {
		    ONE(),
		    TWO(),
		    THREE(),
		    <clinit>() {
		    }
		    Num() {
		    }
		  }
		  public X() {
		  }
		  public static void foo(Num num_) {
		    {
		      {
		        switch (num_) {
		        case THREE ->
		            int i_j;
		            <SelectOnName:i_j>;
		        }
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "i_j";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection is a referenced name of type enum in switch expression
 */
public void test011() throws JavaModelException {
	String string =  """
		public class X {
		  public static void foo(Num num_) {
		 	 switch (num_) {
			   case ONE, TWO, THREE -> {
				 break;\
				 }
		    }\
		  }
			enum Num { ONE, TWO, THREE;}
		}""";

	String selection = "num_";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "num_";
	String expectedUnitDisplayString =
			"""
		public class X {
		  enum Num {
		    ONE(),
		    TWO(),
		    THREE(),
		    <clinit>() {
		    }
		    Num() {
		    }
		  }
		  public X() {
		  }
		  public static void foo(Num num_) {
		    <SelectOnName:num_>;
		  }
		}
		""";
	String expectedReplacedSource = "num_";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection is a referenced name of type int in switch expression
 */
public void test012() throws JavaModelException {
	String string =  """
		public class X {
		  public static void foo(int num_) {
		 	 switch (num_ + 1) {
			   case 1, 2, 3 -> {
				 break;\
				 }
		    }\
		  }
		}""";

	String selection = "num_";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "num_";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public static void foo(int num_) {
		    <SelectOnName:num_>;
		  }
		}
		""";
	String expectedReplacedSource = "num_";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection is a referenced name of type int in switch expression
 */
public void test013() throws JavaModelException {
	String string =  """
		public class X {
		  public static void foo(int num_) {
		 	 int i = switch (num_) {
			   case 1, 2, 3 -> (num_ + 1);
		      default -> 0;
		    }\
		  }
		}""";

	String selection = "num_";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "num_";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public static void foo(int num_) {
		    int i;
		    {
		      switch (num_) {
		      case 3 ->
		          <SelectOnName:num_>;
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "num_";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection is a referenced name of type int in switch expression
 */
public void test014() throws JavaModelException {
	String string =  """
		public class X {
		  public static void foo(int num_) {
		 	 int i = switch (num_) {
			   case 1, 2, 3 -> 0;
		      default -> (num_ + 1);
		    }\
		  }
		}""";

	String selection = "num_";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "num_";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public static void foo(int num_) {
		    int i;
		    {
		      switch (num_) {
		      case 3 ->
		          0;
		      default ->
		          <SelectOnName:num_>;
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "num_";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection is a referenced name of type int in switch expression
 */
public void test015() throws JavaModelException {
	String string =  """
		public class X {
		  public static void foo(int num_) {
		 	 int i = switch (num_) {
			   case 1, 2, 3 -> 0;
		      default -> (num_ + 1);
		    }\
		  }
		}""";

	String selection = "num_";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "num_";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public static void foo(int num_) {
		    int i;
		    {
		      switch (num_) {
		      case 3 ->
		          0;
		      default ->
		          <SelectOnName:num_>;
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "num_";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
/*
 * Multi constant case statement with '->', selection is a referenced name of type int in switch expression
 */
public void test016() throws JavaModelException {
	String string =  """
		public class X {
			public void bar(int s) {
				int i_j = switch (s) {
					case 1, 2, 3 -> (s+1);
					default -> i_j;
				};
			}
		}
		""";

	String selection = "i_j";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "i_j";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public void bar(int s) {
		    int i_j;
		    {
		      switch (s) {
		      case 3 ->
		          (s + 1);
		      default ->
		          <SelectOnName:i_j>;
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "i_j";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
public void test017() throws JavaModelException {
	String string =  """
		public class X {
			public void bar(int s) {
				int i_j = switch (s) {
					case 1, 2, 3 -> (s+1);
					default -> (1+i_j);
				};
			}
		}
		""";

	String selection = "i_j";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "i_j";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public void bar(int s) {
		    int i_j;
		    {
		      switch (s) {
		      case 3 ->
		          (s + 1);
		      default ->
		          <SelectOnName:i_j>;
		      }
		    }
		  }
		}
		""";
	String expectedReplacedSource = "i_j";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
}
