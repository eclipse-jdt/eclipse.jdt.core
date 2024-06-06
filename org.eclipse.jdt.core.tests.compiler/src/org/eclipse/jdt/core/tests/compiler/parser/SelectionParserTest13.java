/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
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

public class SelectionParserTest13 extends AbstractSelectionTest {
static {
//		TESTS_NUMBERS = new int[] { 1 };
//		TESTS_NAMES = new String[] { "test005" };
}
public static Test suite() {
	return buildMinimalComplianceTestSuite(SelectionParserTest13.class, F_13);
}

public SelectionParserTest13(String testName) {
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
public void test018() throws JavaModelException {
	String string =  """
		import org.eclipse.jdt.annotation.*;
		import java.util.function.*;
		interface IN0 {}\s
		interface IN1 extends IN0 {}\s
		interface IN2 extends IN0 {}
		public class X {
			 IN1 n_1() { return new IN1() {}; }\s
			IN2 n_2() { return null; }\s
			<M> void m( Supplier< M> m2) { }\s
			void testSw(int i) {\s
				m(switch(i) {\s
					case 1 -> this::n_1;\s
					default -> this::n_2; });\s
			}
		}""";

	String selection = "n_1";
	String selectKey = "<SelectionOnReferenceExpressionName:this::";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "n_1";
	String expectedUnitDisplayString =
			"""
		import org.eclipse.jdt.annotation.*;
		import java.util.function.*;
		interface IN0 {
		}
		interface IN1 extends IN0 {
		}
		interface IN2 extends IN0 {
		}
		public class X {
		  public X() {
		  }
		  IN1 n_1() {
		  }
		  IN2 n_2() {
		  }
		  <M>void m(Supplier<M> m2) {
		  }
		  void testSw(int i) {
		    m(switch (i) {
		case 1 ->
		    <SelectionOnReferenceExpressionName:this::n_1>;
		default ->
		    this::n_2;
		});
		  }
		}
		""";
	String expectedReplacedSource = "this::n_1";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
public void test019() throws JavaModelException {
	String string =  """
		import org.eclipse.jdt.annotation.*;
		import java.util.function.*;
		interface IN0 {}\s
		interface IN1 extends IN0 {}\s
		interface IN2 extends IN0 {}
		public class X {
			 IN1 n_1() { return new IN1() {}; }\s
			IN2 n_2() { return null; }\s
			<M> void m( Supplier< M> m2) { }\s
			void testSw(int i) {\s
				m(switch(i) {\s
					case 2 -> () -> n_1();\s
					default -> this::n_2; });\s
			}
		}""";

	String selection = "n_1";
	String selectKey = "<SelectOnMessageSend:";
	String expectedSelection = selectKey + selection + "()>";

	String selectionIdentifier = "n_1";
	String expectedUnitDisplayString =
			"""
		import org.eclipse.jdt.annotation.*;
		import java.util.function.*;
		interface IN0 {
		}
		interface IN1 extends IN0 {
		}
		interface IN2 extends IN0 {
		}
		public class X {
		  public X() {
		  }
		  IN1 n_1() {
		  }
		  IN2 n_2() {
		  }
		  <M>void m(Supplier<M> m2) {
		  }
		  void testSw(int i) {
		    m(switch (i) {
		case 2 ->
		    () -> <SelectOnMessageSend:n_1()>;
		default ->
		    this::n_2;
		});
		  }
		}
		""";
	String expectedReplacedSource = "n_1()";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
public void test020() throws JavaModelException {
	String string =  """
		import org.eclipse.jdt.annotation.*;
		import java.util.function.*;
		interface IN0 {}\s
		interface IN1 extends IN0 {}\s
		interface IN2 extends IN0 {}
		public class X {
			 IN1 n_1() { return new IN1() {}; }\s
			IN2 n_2() { return null; }\s
			<M> void m( Supplier< M> m2) { }\s
			void testSw(int i) {\s
				m(switch(i) {\s
					default -> this::n_2; });\s
			}
		}""";

	String selection = "n_2";
	String selectKey = "<SelectionOnReferenceExpressionName:this::";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "n_2";
	String expectedUnitDisplayString =
			"""
		import org.eclipse.jdt.annotation.*;
		import java.util.function.*;
		interface IN0 {
		}
		interface IN1 extends IN0 {
		}
		interface IN2 extends IN0 {
		}
		public class X {
		  public X() {
		  }
		  IN1 n_1() {
		  }
		  IN2 n_2() {
		  }
		  <M>void m(Supplier<M> m2) {
		  }
		  void testSw(int i) {
		    m(switch (i) {
		default ->
		    <SelectionOnReferenceExpressionName:this::n_2>;
		});
		  }
		}
		""";
	String expectedReplacedSource = "this::n_2";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
public void test021() throws JavaModelException {
	String string =  """
		import org.eclipse.jdt.annotation.*;
		import java.util.function.*;
		interface IN0 {}\s
		interface IN1 extends IN0 {}\s
		interface IN2 extends IN0 {}
		public class X {
			 IN1 n_1(int ijk) { return new IN1() {}; }\s
			IN2 n_2() { return null; }\s
			<M> void m( Supplier< M> m2) { }\s
			void testSw(int ijk) {\s
				m(switch(ijk) {\s
					default -> () -> n_1(ijk); });\s
			}
		}""";

	String selection = "n_1";
	String selectKey = "<SelectOnMessageSend:";
	String expectedSelection = selectKey + selection + "(ijk)>";

	String selectionIdentifier = "n_1";
	String expectedUnitDisplayString =
			"""
		import org.eclipse.jdt.annotation.*;
		import java.util.function.*;
		interface IN0 {
		}
		interface IN1 extends IN0 {
		}
		interface IN2 extends IN0 {
		}
		public class X {
		  public X() {
		  }
		  IN1 n_1(int ijk) {
		  }
		  IN2 n_2() {
		  }
		  <M>void m(Supplier<M> m2) {
		  }
		  void testSw(int ijk) {
		    m(switch (ijk) {
		default ->
		    () -> <SelectOnMessageSend:n_1(ijk)>;
		});
		  }
		}
		""";
	String expectedReplacedSource = "n_1(ijk)";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
public void test022() throws JavaModelException {
	String string =  """
		import org.eclipse.jdt.annotation.*;
		import java.util.function.*;
		interface IN0 {}\s
		interface IN1 extends IN0 {}\s
		interface IN2 extends IN0 {}
		public class X {
			 IN1 n_1(int ijk) { return new IN1() {}; }\s
			IN2 n_2() { return null; }\s
			<M> void m( Supplier< M> m2) { }\s
			void testSw(int ijk) {\s
				m(switch(ijk) {\s
					default -> () -> n_1(ijk); });\s
			}
		}""";

	String selection = "ijk";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "ijk";
	String expectedUnitDisplayString =
			"""
		import org.eclipse.jdt.annotation.*;
		import java.util.function.*;
		interface IN0 {
		}
		interface IN1 extends IN0 {
		}
		interface IN2 extends IN0 {
		}
		public class X {
		  public X() {
		  }
		  IN1 n_1(int ijk) {
		  }
		  IN2 n_2() {
		  }
		  <M>void m(Supplier<M> m2) {
		  }
		  void testSw(int ijk) {
		    m(switch (ijk) {
		default ->
		    () -> n_1(<SelectOnName:ijk>);
		});
		  }
		}
		""";
	String expectedReplacedSource = "ijk";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
public void testIssue708_1() throws JavaModelException {
	String string =  """
		public class X {
			public void test(Type type, String string) {
				switch (type) {
				case openDeclarationFails -> {
					switch (string) {
						case "Test" -> method(Type.openDeclarationFails);
					}
				}
				}
			}
			private void method(Type relay) {}
			static public enum Type {
				openDeclarationFails, anotherValue;
			}
		}""";

	String selection = "openDeclarationFails";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + selection + ">";

	String selectionIdentifier = "openDeclarationFails";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public static enum Type {
		    openDeclarationFails(),
		    anotherValue(),
		    <clinit>() {
		    }
		    public Type() {
		    }
		  }
		  public X() {
		  }
		  public void test(Type type, String string) {
		    {
		      switch (type) {
		      case <SelectOnName:openDeclarationFails> :
		      }
		    }
		  }
		  private void method(Type relay) {
		  }
		}
		""";
	String expectedReplacedSource = "openDeclarationFails";
	String testName = "X.java";

	int selectionStart = string.indexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
public void testIssue708_2() throws JavaModelException {
	String string =  """
		public class X {
			static public enum Type {
				openDeclarationFails, anotherValue;
			}
			public void test(Type type, String string) {
				switch (type) {
				case openDeclarationFails -> {
					switch (string) {
					case "Test" -> method(Type.openDeclarationFails);
					}
				}
				case anotherValue -> {
					switch (string) {
					case "Test" -> method(Type.anotherValue);
					}
				}
				}
			}
			private void method(Type relay) {}
		}""";

	String selection = "anotherValue";
	String selectKey = "<SelectOnName:";
	String expectedSelection = selectKey + "Type." + selection + ">";

	String selectionIdentifier = "anotherValue";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public static enum Type {
		    openDeclarationFails(),
		    anotherValue(),
		    <clinit>() {
		    }
		    public Type() {
		    }
		  }
		  public X() {
		  }
		  public void test(Type type, String string) {
		    {
		      {
		        {
		          switch (type) {
		          case openDeclarationFails ->
		              {
		                switch (string) {
		                case "Test" ->
		                    method(Type.openDeclarationFails);
		                }
		              }
		          case anotherValue ->
		              switch (string) {
		              case "Test" ->
		                  <SelectOnName:Type.anotherValue>;
		              }
		          }
		        }
		      }
		    }
		  }
		  private void method(Type relay) {
		  }
		}
		""";
	String expectedReplacedSource = "Type.anotherValue";
	String testName = "X.java";

	int selectionStart = string.lastIndexOf(selection);
	int selectionEnd = selectionStart + selection.length() - 1;

	checkMethodParse(string.toCharArray(), selectionStart, selectionEnd, expectedSelection, expectedUnitDisplayString,
			selectionIdentifier, expectedReplacedSource, testName);
}
}
