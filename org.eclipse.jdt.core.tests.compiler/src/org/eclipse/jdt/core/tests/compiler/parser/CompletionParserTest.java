/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
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

import junit.framework.Test;

import org.eclipse.jdt.internal.codeassist.complete.InvalidCursorLocation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class CompletionParserTest extends AbstractCompletionTest {
public CompletionParserTest(String testName) {
	super(testName);
}
static {
//	TESTS_NAMES = new String[] { "testBug292087" };
}
public static Test suite() {
	return buildAllCompliancesTestSuite(CompletionParserTest.class);
}
public void testA() {
	String str =
		"""
		package p;\s
		public class A {
			public void foo(
				java.util.Locale,\s
				java.util.Vector) {
				int i;
				if (i instanceof O) {
				}
				String s = "hello";
				s.}
		}
		""";

	String testName = "<complete on methods/fields>";
	String completeBehind = "s.";
	String expectedCompletionNodeToString = "<CompleteOnName:s.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "s.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class A {
		  public A() {
		  }
		  public void foo() {
		    {
		      int i;
		      String s;
		      <CompleteOnName:s.>;
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testAA_1() {
	String str =
			"""
		package p;\s
		import something;\s
		import p2.;\s
		public class AA {
			void foo() {
				int maxUnits = 0;
				for (int i = 0;\s
					i < maxUnits;\s
					i++) {
					CompilationUnitResult unitResult =\s
						new CompilationUnitResult(
							null,\s
							i,\s
							maxUnits);\s
				}
			}
		}
		""";

	String completeBehind = "n";
	String expectedCompletionNodeToString = "<CompleteOnName:n>";
	String completionIdentifier = "n";
	String expectedUnitDisplayString =
		"""
		package p;
		import something;
		public class AA {
		  public AA() {
		  }
		  void foo() {
		    int maxUnits;
		    int i;
		    {
		      CompilationUnitResult unitResult = <CompleteOnName:n>;
		    }
		  }
		}
		""";
	String expectedReplacedSource = "new";
	String testName = "<complete on initializer (new)>";

	int cursorLocation = str.indexOf("new CompilationUnitResult(") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testAA_2() {
	String str =
			"""
		package p;\s
		import something;\s
		import p2.;\s
		public class AA {
			void foo() {
				int maxUnits = 0;
				for (int i = 0;\s
					i < maxUnits;\s
					i++) {
					CompilationUnitResult unitResult =\s
						new CompilationUnitResult(
							null,\s
							i,\s
							maxUnits);\s
				}
			}
		}
		""";

	String testName = "<complete on method call argument>";
	String completeBehind = "n";
	String expectedCompletionNodeToString = "<CompleteOnName:n>";
	String completionIdentifier = "n";
	String expectedReplacedSource = "null";
	int cursorLocation = str.indexOf("null, ") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import something;
		public class AA {
		  public AA() {
		  }
		  void foo() {
		    int maxUnits;
		    for (int i;; (i < maxUnits); i ++)\s
		      {
		        CompilationUnitResult unitResult = new CompilationUnitResult(<CompleteOnName:n>, i, maxUnits);
		      }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testAA_3() {
	String str =
			"""
		package p;\s
		import something;\s
		import p2.;\s
		public class AA {
			void foo() {
				int maxUnits = 0;
				for (int i = 0;\s
					i < maxUnits;\s
					i++) {
					CompilationUnitResult unitResult =\s
						new CompilationUnitResult(
							null,\s
							i,\s
							maxUnits);\s
				}
			}
		}
		""";

	String testName = "<complete on call to constructor argument>";
	String completeBehind = "i";
	String expectedCompletionNodeToString = "<CompleteOnName:i>";
	String completionIdentifier = "i";
	String expectedReplacedSource = "i";
	int cursorLocation = str.indexOf("i, ") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import something;
		public class AA {
		  public AA() {
		  }
		  void foo() {
		    int maxUnits;
		    for (int i;; (i < maxUnits); i ++)\s
		      {
		        CompilationUnitResult unitResult = new CompilationUnitResult(null, <CompleteOnName:i>, maxUnits);
		      }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testAA_4() {
	String str =
			"""
		package p;\s
		import something;\s
		import p2.;\s
		public class AA {
			void foo() {
				int maxUnits = 0;
				for (int i = 0;\s
					i < maxUnits;\s
					i++) {
					CompilationUnitResult unitResult =\s
						new CompilationUnitResult(
							null,\s
							i,\s
							maxUnits);\s
				}
			}
		}
		""";

	String testName = "<complete on constructor call argument>";
	String completeBehind = "max";
	String expectedCompletionNodeToString = "<CompleteOnName:max>";
	String completionIdentifier = "max";
	String expectedReplacedSource = "maxUnits";
	int cursorLocation = str.indexOf("maxUnits); ") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import something;
		public class AA {
		  public AA() {
		  }
		  void foo() {
		    int maxUnits;
		    for (int i;; (i < maxUnits); i ++)\s
		      {
		        CompilationUnitResult unitResult = new CompilationUnitResult(null, i, <CompleteOnName:max>);
		      }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testAB_1FHU9LU() {
	String str =
			"""
		package p;\s
		/**
		 * 1FHU9LU
		 */
		class SuperClass {
			static void eFooStatic() {
			}
			void eFoo() {
			}
		}
		public class AB
			extends SuperClass {
			void eBar() {
				super.eFoo();
			}
		}
		""";

	String testName = "<complete on methods/fields from super class>";
	String completeBehind = "super.";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:super.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "super.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class SuperClass {
		  SuperClass() {
		  }
		  static void eFooStatic() {
		  }
		  void eFoo() {
		  }
		}
		public class AB extends SuperClass {
		  public AB() {
		  }
		  void eBar() {
		    <CompleteOnMemberAccess:super.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testAC_1FJ8D9Z_1() {
	String str =
			"""
		package p;\s
		/**
		 * 1FJ8D9Z
		 */
		import java.io.*;
		public class AC {
			AC() {
			}
			AC(int i) {
			}
			AC(int i, String s) {
			}
			void foo() {
				new AC(new File(
					new java
					.util
					.Vector(}
		}
		""";

	String testName = "<complete on constructor argument>";
	String completeBehind = "new AC(";
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new AC()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import java.io.*;
		public class AC {
		  AC() {
		  }
		  AC(int i) {
		  }
		  AC(int i, String s) {
		  }
		  void foo() {
		    <CompleteOnAllocationExpression:new AC()>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testAC_1FJ8D9Z_2() {
	String str =
			"""
		package p;\s
		/**
		 * 1FJ8D9Z
		 */
		import java.io.*;
		public class AC {
			AC() {
			}
			AC(int i) {
			}
			AC(int i, String s) {
			}
			void foo() {
				new AC(new File(
					new java
					.util
					.Vector(}
		}
		""";

	String testName = "<complete on constructor argument>";
	String completeBehind = "new File(";
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new File()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import java.io.*;
		public class AC {
		  AC() {
		  }
		  AC(int i) {
		  }
		  AC(int i, String s) {
		  }
		  void foo() {
		    <CompleteOnAllocationExpression:new File()>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testAC_1FJ8D9Z_3() {
	String str =
			"""
		package p;\s
		/**
		 * 1FJ8D9Z
		 */
		import java.io.*;
		public class AC {
			AC() {
			}
			AC(int i) {
			}
			AC(int i, String s) {
			}
			void foo() {
				new AC(new File(
					new java.util.Vector(}
		}
		""";

	String testName = "<complete on constructor argument>";
	String completeBehind = "new java.util.Vector(";
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new java.util.Vector()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import java.io.*;
		public class AC {
		  AC() {
		  }
		  AC(int i) {
		  }
		  AC(int i, String s) {
		  }
		  void foo() {
		    <CompleteOnAllocationExpression:new java.util.Vector()>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testB() {
	String str =
		"""
		package p;\s
		public class B {
			Object o = new Object }
		""";

	String testName = "<complete on type into type creation>";
	String completeBehind = "new Object";
	String expectedCompletionNodeToString = "<CompleteOnType:Object>";
	String completionIdentifier = "Object";
	String expectedReplacedSource = "Object";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class B {
		  Object o = new <CompleteOnType:Object>();
		  public B() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testBA_1() {
	String str =
			"""
		package p;\s
		public class BA {
			void foo() {
				java.util.Vector v2;
				java.util.Vector v1;
			}
		}
		""";

	String testName = "<complete on package name>";
	String completeBehind = "java.";
	String expectedCompletionNodeToString = "<CompleteOnName:java.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "java.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class BA {
		  public BA() {
		  }
		  void foo() {
		    <CompleteOnName:java.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testBA_2() {
	String str =
			"""
		package p;\s
		public class BA {
			void foo() {
				java.util.Vector v2;
				java.util.Vector v1;
			}
		}
		""";

	String testName = "<complete on package contents>";
	String completeBehind = "java.util.";
	String expectedCompletionNodeToString = "<CompleteOnName:java.util.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "java.util.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class BA {
		  public BA() {
		  }
		  void foo() {
		    <CompleteOnName:java.util.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testBB_1FHJ8H9() {
	String str =
			"""
		package p;\s
		/**
		 * 1FHJ8H9
		 */
		public class BB {
			void bar() {
				f }
		}
		""";

	String testName = "<complete on method/field from implicit method call>";
	String completeBehind = "f";
	String expectedCompletionNodeToString = "<CompleteOnName:f>";
	String completionIdentifier = "f";
	String expectedReplacedSource = "f";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class BB {
		  public BB() {
		  }
		  void bar() {
		    <CompleteOnName:f>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testBC_1FJ4GSG_1() {
	String str =
			"""
		package p;\s
		/**
		 * 1FJ4GSG
		 */
		import java.util.Vector;
		public class BC {
			int Value1 = 0;
			interface Constants {
				int OK = 1;
				int CANCEL = 2;
			}
			void foo() {
				Vector v =\s
					new Vector(
						Value1,\s
						BC.Constants.OK
							| BC.Constants.CANCEL);\s
				Object ans = v.elementAt(1);
			}
		}
		""";

	String testName = "<complete on member type>";
	String completeBehind = "BC.";
	String expectedCompletionNodeToString = "<CompleteOnName:BC.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "BC.Constants.OK";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import java.util.Vector;
		public class BC {
		  interface Constants {
		    int OK;
		    int CANCEL;
		    <clinit>() {
		    }
		  }
		  int Value1;
		  public BC() {
		  }
		  void foo() {
		    Vector v = new Vector(Value1, (<CompleteOnName:BC.> | BC.Constants.CANCEL));
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testBC_1FJ4GSG_2() {
	String str =
			"""
		package p;\s
		/**
		 * 1FJ4GSG
		 */
		import java.util.Vector;
		public class BC {
			int Value1 = 0;
			interface Constants {
				int OK = 1;
				int CANCEL = 2;
			}
			void foo() {
				Vector v =\s
					new Vector(
						Value1,\s
						BC.Constants.OK
							| BC.Constants.CANCEL);\s
				Object ans = v.elementAt(1);
			}
		}
		""";

	String testName = "<complete on member type method/field>";
	String completeBehind = "| BC.Constants.";
	String expectedCompletionNodeToString = "<CompleteOnName:BC.Constants.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "BC.Constants.CANCEL";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import java.util.Vector;
		public class BC {
		  interface Constants {
		    int OK;
		    int CANCEL;
		    <clinit>() {
		    }
		  }
		  int Value1;
		  public BC() {
		  }
		  void foo() {
		    Vector v = new Vector(Value1, (BC.Constants.OK | <CompleteOnName:BC.Constants.>));
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testBC_1FJ4GSG_3() {
	String str =
			"""
		package p;\s
		/**
		 * 1FJ4GSG
		 */
		import java.util.Vector;
		public class BC {
			int Value1 = 0;
			interface Constants {
				int OK = 1;
				int CANCEL = 2;
			}
			void foo() {
				Vector v =\s
					new Vector(
						Value1,\s
						BC.Constants.OK
							| BC.Constants.CANCEL);\s
				Object ans = v.elementAt(1);
			}
		}
		""";

	String testName = "<complete on method/field>";
	String completeBehind = "v.";
	String expectedCompletionNodeToString = "<CompleteOnName:v.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "v.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import java.util.Vector;
		public class BC {
		  interface Constants {
		    int OK;
		    int CANCEL;
		    <clinit>() {
		    }
		  }
		  int Value1;
		  public BC() {
		  }
		  void foo() {
		    Vector v;
		    Object ans = <CompleteOnName:v.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testC() {
	String str =
		"""
		package p;\s
		public class C {
			void foo() {
				String string = n;
			}
		}
		""";

	String completeBehind = "= n";
	String expectedCompletionNodeToString = "<CompleteOnName:n>";
	String completionIdentifier = "n";
	String expectedUnitDisplayString =
		"""
		package p;
		public class C {
		  public C() {
		  }
		  void foo() {
		    String string = <CompleteOnName:n>;
		  }
		}
		""";
	String expectedReplacedSource = "n";
	String testName = "<complete on local variable initializer>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testCA_1FGPJQZ() {
	String str =
			"""
		package p;\s
		import p2.X;\s
		/**
		 * 1FGPJQZ
		 */
		public class CA {
			void moo() {
				unknownField.}
		}
		""";

	String testName = "<complete on method/field>";
	String completeBehind = "unknownField.";
	String expectedCompletionNodeToString = "<CompleteOnName:unknownField.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "unknownField.";
	String expectedUnitDisplayString =
		"""
		package p;
		import p2.X;
		public class CA {
		  public CA() {
		  }
		  void moo() {
		    <CompleteOnName:unknownField.>;
		  }
		}
		""";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testCB_1FHSKQ9_1() {
	String str =
			"""
		package p;\s
		/**
		 * 1FHSKQ9
		 */
		public class CB {
			void foo() {
				int i = 0;
				int[] tab1 = new int[10];
				int j = tab1[i];
				System.out.println(
					" " + (i + 1));\s
			}
		}
		""";

	String testName = "<complete on method call argument>";
	String completeBehind = "+ (i";
	String expectedCompletionNodeToString = "<CompleteOnName:i>";
	String completionIdentifier = "i";
	String expectedReplacedSource = "i";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class CB {
		  public CB() {
		  }
		  void foo() {
		    int i;
		    int[] tab1;
		    int j;
		    System.out.println((" " + (<CompleteOnName:i> + 1)));
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testCB_1FHSKQ9_2() {
	String str =
			"""
		package p;\s
		/**
		 * 1FHSKQ9
		 */
		public class CB {
			void foo() {
				int i = 0;
				int[] tab1 = new int[10];
				int j = tab1[i];
				System.out.println(
					" " + (i + 1));\s
			}
		}
		""";

	String completeBehind = "i + 1";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "";
	String expectedUnitDisplayString = null;
	String expectedReplacedSource = NONE;
	String testName = "<complete on digit into method call argument>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;

	try {
		checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
		assertTrue("failed to detect invalid cursor location", false);
	} catch(InvalidCursorLocation e){
		assertEquals("invalid cursor location: ", e.irritant, InvalidCursorLocation.NO_COMPLETION_INSIDE_NUMBER);
	}
}
public void testCC_1FJ64I9() {
	String str =
			"""
		package p;\s
		/**
		 * 1FJ64I9
		 */
		class CCHelper {
			class Member1 {
			}
			class Member2 {
			}
			void foo() {
			}
		}
		public class CC {
			void foo() {
				new CCHelper()
					.new CCHelper()
					.new M }
		}
		""";

	String testName = "<complete on qualified member type>";
	String completeBehind = ".new M";
	String expectedCompletionNodeToString = "<CompleteOnType:M>";
	String completionIdentifier = "M";
	String expectedReplacedSource = "M";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class CCHelper {
		  class Member1 {
		    Member1() {
		    }
		  }
		  class Member2 {
		    Member2() {
		    }
		  }
		  CCHelper() {
		  }
		  void foo() {
		  }
		}
		public class CC {
		  public CC() {
		  }
		  void foo() {
		    new CCHelper().new CCHelper().new <CompleteOnType:M>();
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testD_1() {
	String str =
		"""
		package p;\s
		import java.util.*;
		public class D {
			static int i;
			static {
				i = 5;
			}
			public int j;
			Vector a = new Vector();
			void foo(String s) {
				String string = null;
				int soso;
				float f;
				string.regionMatches(
					0,\s
					"",\s
					0,\s
					0);\s
			}
		}
		""";

	String testName = "<complete on variable into type initializer>";
	String completeBehind = "i";
	String expectedCompletionNodeToString = "<CompleteOnName:i>";
	String completionIdentifier = "i";
	String expectedReplacedSource = "i";
	int cursorLocation = str.indexOf("i = 5;") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import java.util.*;
		public class D {
		  static int i;
		  static {
		    <CompleteOnName:i>;
		  }
		  public int j;
		  Vector a;
		  <clinit>() {
		  }
		  public D() {
		  }
		  void foo(String s) {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testD_2() {
	String str =
		"""
		package p;\s
		import java.util.*;
		public class D {
			static int i;
			static {
				i = 5;
			}
			public int j;
			Vector a = new Vector();
			void foo(String s) {
				String string = null;
				int soso;
				float f;
				string.regionMatches(
					0,\s
					"",\s
					0,\s
					0);\s
			}
		}
		""";

	String testName = "<complete on method/field>";
	String completeBehind = "string.";
	String expectedCompletionNodeToString = "<CompleteOnName:string.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "string.";
	String expectedUnitDisplayString =
		"""
		package p;
		import java.util.*;
		public class D {
		  static int i;
		  static {
		  }
		  public int j;
		  Vector a;
		  <clinit>() {
		  }
		  public D() {
		  }
		  void foo(String s) {
		    String string;
		    int soso;
		    float f;
		    <CompleteOnName:string.>;
		  }
		}
		""";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testDA_1() {
	String str =
			"""
		package p;\s
		public class DA {
			void foo() {
				new TestCase("error") {
					protected void runTest() {
						Vector v11111 = new Vector();
						v }
				};
			}
		}
		""";

	String testName = "<complete on method/field into anonymous declaration>";
	String completeBehind = "v";
	String expectedCompletionNodeToString = "<CompleteOnName:v>";
	String completionIdentifier = "v";
	String expectedReplacedSource = "v";
	int cursorLocation = str.indexOf("v }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class DA {
		  public DA() {
		  }
		  void foo() {
		    new TestCase("error") {
		      protected void runTest() {
		        Vector v11111;
		        <CompleteOnName:v>;
		      }
		    };
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testDA_2() {
	String str =
			"""
		package p;\s
		public class DA {
			void foo() {
				new TestCase("error") {
					protected void runTest() {
						Vector v11111 = new Vector();
						v }
				};
			}
		}
		""";

	String completeBehind = "protected v";
	String expectedCompletionNodeToString = "<CompleteOnType:v>";
	String completionIdentifier = "v";
	String expectedUnitDisplayString =
		"""
		package p;
		public class DA {
		  public DA() {
		  }
		  void foo() {
		    new TestCase("error") {
		      <CompleteOnType:v>;
		      runTest() {
		      }
		    };
		  }
		}
		""";
	String expectedReplacedSource = "void";
	String testName = "<complete on return type into anonymous declaration>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testDA_3() {
	String str =
			"""
		package p;\s
		public class DA {
			void foo() {
				new TestCase("error") {
					protected void runTest() {
						Vector v11111 = new Vector();
						v }
				};
			}
		}
		""";

	String testName = "<complete on method selector into anonymous declaration>";
	String completeBehind = "r";
	String expectedCompletionNodeToString = "<CompletionOnMethodName:protected void r()>";
	String completionIdentifier = "r";
	String expectedReplacedSource = "runTest()";
	int cursorLocation = str.indexOf("runTest") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class DA {
		  public DA() {
		  }
		  void foo() {
		    new TestCase("error") {
		      <CompletionOnMethodName:protected void r()>
		    };
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testDA_4() {
	String str =
			"""
		package p;\s
		public class DA {
			void foo() {
				new TestCase("error") {
					protected void runTest() {
						Vector v11111 = new Vector();
						v }
				};
			}
		}
		""";

	String testName = "<complete on local variable type into anonymous declaration>";
	String completeBehind = "V";
	String expectedCompletionNodeToString = "<CompleteOnType:V>";
	String completionIdentifier = "V";
	String expectedReplacedSource = "Vector";
	int cursorLocation = str.indexOf("Vector v11111") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class DA {
		  public DA() {
		  }
		  void foo() {
		    new TestCase("error") {
		      protected void runTest() {
		        <CompleteOnType:V> v11111;
		      }
		    };
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testDA_5() {
	String str =
			"""
		package p;\s
		public class DA {
			void foo() {
				new TestCase("error") {
					protected void runTest() {
						Vector v11111 = new Vector();
						v }
				};
			}
		}
		""";

	String testName = "<complete on local type into anonymous declaration>";
	String completeBehind = "v";
	String expectedCompletionNodeToString = "<CompleteOnName:v>";
	String completionIdentifier = "v";
	String expectedReplacedSource = "v";
	int cursorLocation = str.indexOf("v }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class DA {
		  public DA() {
		  }
		  void foo() {
		    new TestCase("error") {
		      protected void runTest() {
		        Vector v11111;
		        <CompleteOnName:v>;
		      }
		    };
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testDB_1FHSLDR() {
	String str =
			"""
		package p;\s
		/**
		 * 1FHSLDR
		 */
		public class DB {
			void foo() {
				try {
					System.out.println("");
				}
				fi }
		}
		""";

	String testName = "<complete on finally keyword>";
	String completeBehind = "fi";
	String expectedCompletionNodeToString = "<CompleteOnKeyword:fi>";
	String completionIdentifier = "fi";
	String expectedReplacedSource = "fi";
	int cursorLocation = str.indexOf("fi }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class DB {
		  public DB() {
		  }
		  void foo() {
		    <CompleteOnKeyword:fi>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testDC_1FJJ0JR_1() {
	String str =
			"""
		package p;\s
		/**
		 * 1FJJ0JR
		 */
		public class DC
			extends ModelChangeOperation {
			ISec public SetSecondarySourceOperation(
				ISecondarySourceContainer element,\s
				VersionID id) {
			}
			protected abstract void doExecute(IProgressMonitor monitor)
				throws OperationFailedException {
			}
		}
		""";

	String testName = "<complete on method return type>";
	String completeBehind = "ISec";
	String expectedCompletionNodeToString = "<CompleteOnType:ISec>";
	String completionIdentifier = "ISec";
	String expectedReplacedSource = "ISec";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class DC extends ModelChangeOperation {
		  <CompleteOnType:ISec>;
		  public DC() {
		  }
		  public SetSecondarySourceOperation(ISecondarySourceContainer element, VersionID id) {
		  }
		  protected abstract void doExecute(IProgressMonitor monitor) throws OperationFailedException;
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testE_1FG1YDS_1() {
	String str =
		"""
		package p;\s
		/**
		 * 1FG1YDS
		 */
		public class E {
			{
				new Y()
			 }
			{
				new Y().}
			class Y
				extends java.util.Vector {
				void foo() {
				}
			}
		}
		""";

	String testName = "<complete on type into type creation>";
	String completeBehind = "Y";
	String expectedCompletionNodeToString = "<CompleteOnType:Y>";
	String completionIdentifier = "Y";
	String expectedReplacedSource = "Y";
	String expectedUnitDisplayString =
		"""
		package p;
		public class E {
		  class Y extends java.util.Vector {
		    Y() {
		    }
		    void foo() {
		    }
		  }
		  {
		    new <CompleteOnType:Y>();
		  }
		  {
		  }
		  public E() {
		  }
		}
		""";

	int cursorLocation = str.indexOf("Y()\n") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testE_1FG1YDS_2() {
	String str =
		"""
		package p;\s
		/**
		 * 1FG1YDS
		 */
		public class E {
			{
				new Y()
			 }
			{
				new Y().}
			class Y
				extends java.util.Vector {
				void foo() {
				}
			}
		}
		""";

	String testName = "<complete on implicit method call into intializer>";
	String completeBehind = "new Y().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new Y().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
		"""
		package p;
		public class E {
		  class Y extends java.util.Vector {
		    Y() {
		    }
		    void foo() {
		    }
		  }
		  {
		  }
		  {
		    <CompleteOnMemberAccess:new Y().>;
		  }
		  public E() {
		  }
		}
		""";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testE_1FG1YDS_3() {
	String str =
		"""
		package p;\s
		/**
		 * 1FG1YDS
		 */
		public class E {
			{
				new Y()
			 }
			{
				new Y().}
			class Y
				extends java.util.Vector {
				void foo() {
				}
			}
		}
		""";

	String testName = "<complete on extend type>";
	String completeBehind = "java.util.";
	String expectedCompletionNodeToString = "<CompleteOnClass:java.util.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "java.util.Vector";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class E {
		  class Y extends <CompleteOnClass:java.util.> {
		    Y() {
		    }
		    void foo() {
		    }
		  }
		  {
		  }
		  {
		  }
		  public E() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testEA_1() {
	String str =
			"""
		package p;\s
		public class EA {
			void foo() {
				try {
					throw new Error();
				} catch (Exception eeee) {
					eeee.}
			}
		}
		""";

	String testName = "<complete on catch block exception type declaration>";
	String completeBehind = "E";
	String expectedCompletionNodeToString = "<CompleteOnException:E>";
	String completionIdentifier = "E";
	String expectedReplacedSource = "Exception";
	String expectedUnitDisplayString =
		"""
		package p;
		public class EA {
		  public EA() {
		  }
		  void foo() {
		    try
		      {
		        throw new Error();
		      }
		    catch (<CompleteOnException:E>  )
		      {
		      }
		  }
		}
		""";

	int cursorLocation = str.indexOf("Exception eeee") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testEA_2() {
	String str =
			"""
		package p;\s
		public class EA {
			void foo() {
				try {
					throw new Error();
				} catch (Exception eeee) {
					eeee.}
			}
		}
		""";

	String testName = "<complete on method/field of thrown exception into catch block>";
	String completeBehind = "eeee.";
	String expectedCompletionNodeToString = "<CompleteOnName:eeee.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "eeee.";
	String expectedUnitDisplayString =
		"""
		package p;
		public class EA {
		  public EA() {
		  }
		  void foo() {
		    {
		      Exception eeee;
		      <CompleteOnName:eeee.>;
		    }
		  }
		}
		""";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testEB_1FI74S3() {
	String str =
			"""
		package p;\s
		/**
		 * 1FI74S3
		 */
		public class EB {
			int[] table;
			void foo() {
				int x = table.}
		}
		""";

	String testName = "<complete on method/field of array>";
	String completeBehind = "table.";
	String expectedCompletionNodeToString = "<CompleteOnName:table.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "table.";
	String expectedUnitDisplayString =
		"""
		package p;
		public class EB {
		  int[] table;
		  public EB() {
		  }
		  void foo() {
		    int x = <CompleteOnName:table.>;
		  }
		}
		""";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testEC_1FSBZ2Y() {
	String str =
			"""
		package p;\s
		/**
		 * 1FSBZ2Y
		 */
		class EC {
			void foo() {
				EC
			}
		}
		class ECOtherTopLevel {
		}
		""";

	String testName = "<complete on local variable decaration type>";
	String completeBehind = "EC";
	String expectedCompletionNodeToString = "<CompleteOnName:EC>";
	String completionIdentifier = "EC";
	String expectedReplacedSource = "EC";
	String expectedUnitDisplayString =
		"""
		package p;
		class EC {
		  EC() {
		  }
		  void foo() {
		    <CompleteOnName:EC>;
		  }
		}
		class ECOtherTopLevel {
		  ECOtherTopLevel() {
		  }
		}
		""";

	int cursorLocation = str.indexOf("EC\n") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testF() {
	String str =
		"""
		package p;\s
		public class F {
			void bar() {
			}
			class Y {
				void foo() {
					ba }
			}
		}
		""";

	String testName = "<complete on method/field explicit access>";
	String completeBehind = "ba";
	String expectedCompletionNodeToString = "<CompleteOnName:ba>";
	String completionIdentifier = "ba";
	String expectedReplacedSource = "ba";
	String expectedUnitDisplayString =
		"""
		package p;
		public class F {
		  class Y {
		    Y() {
		    }
		    void foo() {
		      <CompleteOnName:ba>;
		    }
		  }
		  public F() {
		  }
		  void bar() {
		  }
		}
		""";

	int cursorLocation = str.indexOf("ba }") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testFA_1() {
	String str =
			"""
		package p;\s
		public class FA {
			byte value;
			public float foo() {
				return (float) value;
			}
		}
		""";

	String testName = "<complete on cast expression type>";
	String completeBehind = "f";
	String expectedCompletionNodeToString = "<CompleteOnName:f>";
	String completionIdentifier = "f";
	String expectedReplacedSource = "float";
	String expectedUnitDisplayString =
		"""
		package p;
		public class FA {
		  byte value;
		  public FA() {
		  }
		  public float foo() {
		    <CompleteOnName:f>;
		  }
		}
		""";

	int cursorLocation = str.indexOf("float)") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testFA_2() {
	String str =
			"""
		package p;\s
		public class FA {
			byte value;
			public float foo() {
				return (float) value;\s
			}
		}
		""";

	String testName = "<complete on returned value>";
	String completeBehind = "v";
	String expectedCompletionNodeToString = "<CompleteOnName:v>";
	String completionIdentifier = "v";
	String expectedReplacedSource = "value";
	String expectedUnitDisplayString =
		"""
		package p;
		public class FA {
		  byte value;
		  public FA() {
		  }
		  public float foo() {
		    (float) <CompleteOnName:v>;
		  }
		}
		""";

	int cursorLocation = str.indexOf("value; \n") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testFB_1FI74S3() {
	String str =
			"""
		package p;\s
		/**
		 * 1FI74S3
		 */
		public class FB {
			int[] table;
			void foo() {
				int x = table[1].}
		}
		""";

	String testName = "<complete on method/field of array element>";
	String completeBehind = "table[1].";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:table[1].>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
		"""
		package p;
		public class FB {
		  int[] table;
		  public FB() {
		  }
		  void foo() {
		    int x = <CompleteOnMemberAccess:table[1].>;
		  }
		}
		""";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testFC_1FSBZ9B() {
	String str =
			"""
		package p;\s
		/**
		 * 1FSBZ9B
		 */
		class FC {
			UNKOWNTYPE field;
			void foo() {
				f
			}
		}
		""";

	String testName = "<complete on method/field implicit access>";
	String completeBehind = "f";
	String expectedCompletionNodeToString = "<CompleteOnName:f>";
	String completionIdentifier = "f";
	String expectedReplacedSource = "f";
	int cursorLocation = str.indexOf("f\n") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class FC {
		  UNKOWNTYPE field;
		  FC() {
		  }
		  void foo() {
		    <CompleteOnName:f>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testG() {
	String str =
		"""
		package p;\s
		public class G {
			int bar() {
			}
			class Y {
				void foo(int b) {
					return b }
			}
		}
		""";

	String testName = "<complete on return value>";
	String completeBehind = "b";
	String expectedCompletionNodeToString = "<CompleteOnName:b>";
	String completionIdentifier = "b";
	String expectedReplacedSource = "b";
	String expectedUnitDisplayString =
		"""
		package p;
		public class G {
		  class Y {
		    Y() {
		    }
		    void foo(int b) {
		      return <CompleteOnName:b>;
		    }
		  }
		  public G() {
		  }
		  int bar() {
		  }
		}
		""";

	int cursorLocation = str.indexOf("b }") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testGA() {
	String str =
			"""
		package p;\s
		public class GA {
			void foo(String s) {
				String string = s;
			}
		}
		""";

	String testName = "<complete on local variable initializer>";
	String completeBehind = "s";
	String expectedCompletionNodeToString = "<CompleteOnName:s>";
	String completionIdentifier = "s";
	String expectedReplacedSource = "s";
	int cursorLocation = str.indexOf("s;") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class GA {
		  public GA() {
		  }
		  void foo(String s) {
		    String string = <CompleteOnName:s>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testGB_1FI74S3() {
	String str =
			"""
		package p;\s
		/**
		 * 1FI74S3
		 */
		public class GB {
			String[] table;
			void foo() {
				int x = table[1].}
		}
		""";

	String testName = "<complete on method/field of array element>";
	String completeBehind = "table[1].";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:table[1].>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class GB {
		  String[] table;
		  public GB() {
		  }
		  void foo() {
		    int x = <CompleteOnMemberAccess:table[1].>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testGC_1FSHLHV_1() {
	String str =
			"""
		package p;\s
		/**
		 * 1FSHLHV
		 */
		public class GC {
		public static void main(String[] args) {
			Object l = new Object() {
				public void handleEvent(String[] event) {
					String s = new String();
					s.
					try {
						event.;
					}
					catch (Exception e) {
						e.
					}
				}
			}
		}
		}
		""";

	String testName = "<complete on anonymous declaration type>";
	String completeBehind = "O";
	String expectedCompletionNodeToString = "<CompleteOnType:O>";
	String completionIdentifier = "O";
	String expectedReplacedSource = "Object";
	int cursorLocation = str.indexOf("Object()") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class GC {
		  public GC() {
		  }
		  public static void main(String[] args) {
		    Object l = new <CompleteOnType:O>();
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testGC_1FSHLHV_2() {
	String str =
			"""
		package p;\s
		/**
		 * 1FSHLHV
		 */
		public class GC {
		public static void main(String[] args) {
			Object l = new Object() {
				public void handleEvent(String[] event) {
					String s = new String();
					s.
					try {
						event.;
					}
					catch (Exception e) {
						e.
					}
				}
			}
		}
		}
		""";

	String testName = "<complete on method/field of local variable into anonymous declaration>";
	String completeBehind = "s.";
	String expectedCompletionNodeToString = "<CompleteOnName:s.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "s.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class GC {
		  public GC() {
		  }
		  public static void main(String[] args) {
		    Object l;
		    new Object() {
		      public void handleEvent(String[] event) {
		        String s;
		        <CompleteOnName:s.>;
		      }
		    };
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testGC_1FSHLHV_3() {
	String str =
			"""
		package p;\s
		/**
		 * 1FSHLHV
		 */
		public class GC {
		public static void main(String[] args) {
			Object l = new Object() {
				public void handleEvent(String[] event) {
					String s = new String();
					s.
					try {
						event.;
					}
					catch (Exception e) {
						e.
					}
				}
			}
		}
		}
		""";

	String testName = "<complete on method/field of array>";
	String completeBehind = "event.";
	String expectedCompletionNodeToString = "<CompleteOnName:event.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "event.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class GC {
		  public GC() {
		  }
		  public static void main(String[] args) {
		    Object l;
		    new Object() {
		      public void handleEvent(String[] event) {
		        String s;
		        {
		          <CompleteOnName:event.>;
		        }
		      }
		    };
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testGC_1FSHLHV_4() {
	String str =
			"""
		package p;\s
		/**
		 * 1FSHLHV
		 */
		public class GC {
		public static void main(String[] args) {
			Object l = new Object() {
				public void handleEvent(String[] event) {
					String s = new String();
					s.
					try {
						event.;
					}
					catch (Exception e) {
						e.
					}
				}
			}
		}
		}
		""";

	String testName = "<complete on method/field of thrown exception into catch block into anonymous declaration>";
	String completeBehind = "e.";
	String expectedCompletionNodeToString = "<CompleteOnName:e.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "e.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class GC {
		  public GC() {
		  }
		  public static void main(String[] args) {
		    Object l;
		    new Object() {
		      public void handleEvent(String[] event) {
		        String s;
		        {
		          Exception e;
		          <CompleteOnName:e.>;
		        }
		      }
		    };
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testH() {
	String str =
		"""
		package p;\s
		public class H {
			void foo(boolean bbbb) {
				while (Xbm }
			void bar() {
			}
		}
		""";

	String testName = "<complete on while keyword argument>";
	String completeBehind = "Xbm";
	String expectedCompletionNodeToString = "<CompleteOnName:Xbm>";
	String completionIdentifier = "Xbm";
	String expectedReplacedSource = "Xbm";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class H {
		  public H() {
		  }
		  void foo(boolean bbbb) {
		    while (<CompleteOnName:Xbm>)      ;
		  }
		  void bar() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testHA_1() {
	String str =
			"""
		package p;\s
		public class HA {
			void foo() {
				x.y.Z[] field1;\s
				field1[1].}
		}
		""";

	String testName = "<complete on package member type>";
	String completeBehind = "x.y.";
	String expectedCompletionNodeToString = "<CompleteOnName:x.y.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "x.y.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class HA {
		  public HA() {
		  }
		  void foo() {
		    <CompleteOnName:x.y.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testHA_2() {
	String str =
			"""
		package p;\s
		public class HA {
			void foo() {
				x.y.Z[] field1;\s
				field1[1].}
		}
		""";

	String testName = "<complete on method/field of array element>";
	String completeBehind = "field1[1].";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:field1[1].>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class HA {
		  public HA() {
		  }
		  void foo() {
		    x.y.Z[] field1;
		    <CompleteOnMemberAccess:field1[1].>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testHB_1FHSLDR() {
	String str =
			"""
		package p;\s
		/**
		 * 1FHSLDR
		 */
		public class HB {
			void foo() {
				for (; i < totalUnits; i++) {
					unit = unitsToProcess[i];
					try {
						if (options.verbose) {
							System.out.println(
								"process "
									+ (i + 1)
									+ "/"
									+ totalUnits
									+ " : "
									+ unitsToProcess[i]
										.sourceFileName());\s
						}
						process(unit, i);
					}
					fi }
			}
		}
		""";

	String testName = "<complete on finally keyword>";
	String completeBehind = "fi";
	String expectedCompletionNodeToString = "<CompleteOnKeyword:fi>";
	String completionIdentifier = "fi";
	String expectedReplacedSource = "fi";
	int cursorLocation = str.indexOf("fi }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class HB {
		  public HB() {
		  }
		  void foo() {
		    {
		      <CompleteOnKeyword:fi>;
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testHC_1FMPYO3_1() {
	String str =
			"""
		package p;\s
		/**
		 * 1FMPYO3
		 */
		class HC {
			HC(Object o){}
			void foo(){
				HC a = new HC(new Object()).
			}
		}
		""";

	String testName = "<complete on method/field of object creation>";
	String completeBehind = "new HC(new Object()).";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new HC(new Object()).>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class HC {
		  HC(Object o) {
		  }
		  void foo() {
		    HC a = <CompleteOnMemberAccess:new HC(new Object()).>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testHC_1FMPYO3_2() {
	String str =
			"""
		package p;\s
		/**
		 * 1FMPYO3
		 */
		class HC {
			HC(Object o){}
			void foo(){
				A a = new A(new Object()).
			}
		}
		""";

	String testName = "<complete on object of nested object creation declaration>";
	String completeBehind = "O";
	String expectedCompletionNodeToString = "<CompleteOnType:O>";
	String completionIdentifier = "O";
	String expectedReplacedSource = "Object";
	int cursorLocation = str.indexOf("Object()") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class HC {
		  HC(Object o) {
		  }
		  void foo() {
		    A a = new A(new <CompleteOnType:O>());
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testI() {
	String str =
		"""
		package p;\s
		public class I {
			Component }
		""";

	String testName = "<complete on incomplete field declaration type>";
	String completeBehind = "C";
	String expectedCompletionNodeToString = "<CompleteOnType:C>";
	String completionIdentifier = "C";
	String expectedReplacedSource = "Component";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class I {
		  <CompleteOnType:C>;
		  public I() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testIA_1FGNBPR_1() {
	String str =
			"""
		package p;\s
		/**
		 * 1FGNBPR
		 */
		public class IA {
			void foo1() {
				label1 : while (true) {
					class A {
						void foo2() {
							label2 : while (true) {
								break la }
						}
					}
					A a = new A();
					break la }
			}
		}
		""";

	String testName = "<complete on label name>";
	String completeBehind = "la";
	String expectedCompletionNodeToString = "break <CompleteOnLabel:la>;";
	String completionIdentifier = "la";
	String expectedReplacedSource = "la";
	int cursorLocation = str.indexOf("la }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class IA {
		  public IA() {
		  }
		  void foo1() {
		    {
		      class A {
		        A() {
		        }
		        void foo2() {
		          break <CompleteOnLabel:la>;
		        }
		      }
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testIA_1FGNBPR_2() {
	String str =
			"""
		package p;\s
		/**
		 * 1FGNBPR
		 */
		public class IA {
			void foo1() {
				label1 : while (true) {
					class A {
						void foo2() {
							label2 : while (true) {
								break la }
						}
					}
					A a = new A();
					break la }
			}
		}
		""";

	String testName = "<complete on label name>";
	String completeBehind = "la";
	String expectedCompletionNodeToString = "break <CompleteOnLabel:la>;";
	String completionIdentifier = "la";
	String expectedReplacedSource = "la";
	int cursorLocation = str.indexOf("la }", str.indexOf("la }") + 1) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class IA {
		  public IA() {
		  }
		  void foo1() {
		    {
		      class A {
		        A() {
		          super();
		        }
		        void foo2() {
		        }
		      }
		      A a;
		      break <CompleteOnLabel:la>;
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testIB() {
	String str =
			"""
		package p;\s
		public class IB {
			UnknownFieldTYPE field;
			void foo() {
				field.}
		}
		""";

	String testName = "<complete on method/field of field of unkown type>";
	String completeBehind = "field.";
	String expectedCompletionNodeToString = "<CompleteOnName:field.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "field.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class IB {
		  UnknownFieldTYPE field;
		  public IB() {
		  }
		  void foo() {
		    <CompleteOnName:field.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testIC_1FMGUPR() {
	String str =
			"""
		package p;\s
		/**
		 * 1FMGUPR
		 */
		public class IC {
			void foo(){
				new String().toString().
			}
		}
		""";

	String testName = "<complete on multiple method/field call>";
	String completeBehind = "new String().toString().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new String().toString().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class IC {
		  public IC() {
		  }
		  void foo() {
		    <CompleteOnMemberAccess:new String().toString().>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testJ() {
	String str =
		"""
		package p;\s
		public class J {
			int foo1()[void foo2() int i;
			void foo3() {
				f }
		""";

	String testName = "<complete on method/field access into corrupted method declaration>";
	String completeBehind = "f";
	String expectedCompletionNodeToString = "<CompleteOnName:f>";
	String completionIdentifier = "f";
	String expectedReplacedSource = "f";
	int cursorLocation = str.indexOf("f }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class J {
		  public J() {
		  }
		  int foo1() {
		  }
		  void foo2() {
		  }
		  void foo3() {
		    <CompleteOnName:f>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testJA_1FGQVW2_1() {
	String str =
			"""
		package p;\s
		/**
		 * 1FGQVW2
		 */
		public class JA {
			void foo() {
				"abc.txt". 'a'.}
		}
		""";

	String testName = "<complete on string literal>";
	String completeBehind = "\"abc.txt\".";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:\"abc.txt\".>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class JA {
		  public JA() {
		  }
		  void foo() {
		    <CompleteOnMemberAccess:"abc.txt".>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testJA_1FGQVW2_2() {
	String str =
			"""
		package p;\s
		/**
		 * 1FGQVW2
		 */
		public class JA {
			void foo() {
				"abc.txt". 'a'.}
		}
		""";

	String testName = "<complete on char literal>";
	String completeBehind = "'a'.";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:'a'.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class JA {
		  public JA() {
		  }
		  void foo() {
		    <CompleteOnMemberAccess:'a'.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testJB() {
	String str =
			"""
		package p;\s
		public class JB
			extends UnknownSUPERCLASS
			implements UnknownSUPERINTERFACE {
			void foo() {
				f }
		}
		""";

	String testName = "<complete into method declared into corrupted class declaration>";
	String completeBehind = "f";
	String expectedCompletionNodeToString = "<CompleteOnName:f>";
	String completionIdentifier = "f";
	String expectedReplacedSource = "f";
	int cursorLocation = str.indexOf("f }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class JB extends UnknownSUPERCLASS implements UnknownSUPERINTERFACE {
		  public JB() {
		  }
		  void foo() {
		    <CompleteOnName:f>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testJC_1FLG1ZC() {
	String str =
			"""
		package p;\s
		/**
		 * 1FLG1ZC
		 */
		public class JC {
			void foo() {
				new String ().
			}
		}
		""";

	String testName = "<complete on method/field of object creation with dummy spaces>";
	String completeBehind = "new String ().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new String().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class JC {
		  public JC() {
		  }
		  void foo() {
		    <CompleteOnMemberAccess:new String().>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testK_1() {
	String str =
		"""
		package p;\s
		class Other {
			void foo() {
			}
		}
		public class K {
			public static void main(
				java.lang.String[] args) {
				java.io.File bbbb =\s
					new File("c:\\abc.txt");\s
				O bb bbbb.}
		}
		""";

//	str =
//		"public class K {\n" +
//		"	void foo() {\n" +
//		"		new X(\"c:abc.txt\"); \n" +
//		"		O" +
//		"   }\n" +
//		"}\n";

	String testName = "<complete on corrupted local variable declaration>";
	String completeBehind = "		O";
	String expectedCompletionNodeToString = "<CompleteOnName:O>";
	String completionIdentifier = "O";
	String expectedReplacedSource = "O";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class Other {
		  Other() {
		  }
		  void foo() {
		  }
		}
		public class K {
		  public K() {
		  }
		  public static void main(java.lang.String[] args) {
		    java.io.File bbbb;
		    <CompleteOnName:O>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testK_2() {
	String str =
		"""
		package p;\s
		class Other {
			void foo() {
			}
		}
		public class K {
			public static void main(
				java.lang.String[] args) {
				java.io.File bbbb =\s
					new File("c:\\abc.txt");\s
				O bb bbbb.}
		}
		""";

	String testName = "<complete on corrupted local variable declaration name>";
	String completeBehind = "bb";
	String expectedCompletionNodeToString = "<CompleteOnLocalName:O bb>;";
	String completionIdentifier = "bb";
	String expectedReplacedSource = "bb";
	int cursorLocation = str.indexOf("bb bbbb.") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class Other {
		  Other() {
		  }
		  void foo() {
		  }
		}
		public class K {
		  public K() {
		  }
		  public static void main(java.lang.String[] args) {
		    java.io.File bbbb;
		    <CompleteOnLocalName:O bb>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testK_3() {
	String str =
		"""
		package p;\s
		class Other {
			void foo() {
			}
		}
		public class K {
			public static void main(
				java.lang.String[] args) {
				java.io.File bbbb =\s
					new File("c:\\abc.txt");\s
				O bb bbbb.}
		}
		""";

	String testName = "<complete on corrupted local variable declaration>";
	String completeBehind = "bbbb";
	String expectedCompletionNodeToString = "<CompleteOnName:bbbb>";
	String completionIdentifier = "bbbb";
	String expectedReplacedSource = "bbbb";
	int cursorLocation = str.indexOf("bbbb.}") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class Other {
		  Other() {
		  }
		  void foo() {
		  }
		}
		public class K {
		  public K() {
		  }
		  public static void main(java.lang.String[] args) {
		    java.io.File bbbb;
		    O bb;
		    <CompleteOnName:bbbb>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testK_4() {
	String str =
		"""
		package p;\s
		class Other {
			void foo() {
			}
		}
		public class K {
			public static void main(
				java.lang.String[] args) {
				java.io.File bbbb =\s
					new File("c:\\abc.txt");\s
				O bb bbbb.}
		}
		""";

	String testName = "<complete on method/field of local variable with corrupted declaration>";
	String completeBehind = "bbbb.";
	String expectedCompletionNodeToString = "<CompleteOnName:bbbb.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "bbbb.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class Other {
		  Other() {
		  }
		  void foo() {
		  }
		}
		public class K {
		  public K() {
		  }
		  public static void main(java.lang.String[] args) {
		    java.io.File bbbb;
		    O bb;
		    <CompleteOnName:bbbb.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testKA_1FH5SU5() {
	String str =
			"""
		package p;\s
		/**
		 * 1FH5SU5
		 */
		class KAHelper
			extends java.util.Vector {
		}
		public class KA {
			public int hashCode() {
				return 10;
			}
			public static void main(String[] args) {
				KA a = new KA;
				a.has }
		}
		""";

	String testName = "<complete on method/field>";
	String completeBehind = "a.has";
	String expectedCompletionNodeToString = "<CompleteOnName:a.has>";
	String completionIdentifier = "has";
	String expectedReplacedSource = "a.has";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class KAHelper extends java.util.Vector {
		  KAHelper() {
		  }
		}
		public class KA {
		  public KA() {
		  }
		  public int hashCode() {
		  }
		  public static void main(String[] args) {
		    KA a;
		    <CompleteOnName:a.has>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testKB() {
	String str =
			"""
		package p;\s
		public class KB {
			void foo()[i }
		}
		""";

	String testName = "<complete on corrupted method header>";
	String completeBehind = "void foo()[i";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "i";
	String expectedReplacedSource = "i";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class KB {
		  public KB() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testKC_1FLG1ZC() {
	String str =
			"""
		package p;\s
		/**
		 * 1FLG1ZC
		 */
		import java.io.*;
		public class KC {
		private static char[] read(String fileName){
			try {
				File file = new File(fileName);
				FileReader reader =
					new FileReader(file);
				int length;
				char[] contents =
					new char[
						length =
						(int) file.length()];
				int len = 0;
				int readSize = 0;
				while ((readSize != -1)
					&& (len != length)) {
					readSize = reader.read(
						contents,
						len,
						length - len);
					len += readSize;
				}
				reader. t
		""";

	String testName = "<complete on method/field with dummy spaces>";
	String completeBehind = "reader. t";
	String expectedCompletionNodeToString = "<CompleteOnName:reader.t>";
	String completionIdentifier = "t";
	String expectedReplacedSource = "reader. t";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import java.io.*;
		public class KC {
		  public KC() {
		  }
		  private static char[] read(String fileName) {
		    {
		      File file;
		      FileReader reader;
		      int length;
		      char[] contents;
		      int len;
		      int readSize;
		      <CompleteOnName:reader.t>;
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testL_1() {
	String str =
		"""
		package p;\s
		public class L {
			void foo() {
				x.y.Z[] field1,\s
					field2;\s
				field1.if (int[].class }
		}
		""";

	String testName = "<complete on method/field>";
	String completeBehind = "field1.";
	String expectedCompletionNodeToString = "<CompleteOnName:field1.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "field1.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class L {
		  public L() {
		  }
		  void foo() {
		    x.y.Z[] field1;
		    x.y.Z[] field2;
		    <CompleteOnName:field1.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testL_2() {
	String str =
		"""
		package p;\s
		public class L {
			void foo() {
				x.y.Z[] field1,\s
					field2;\s
				field1.if (int[].class }
		}
		""";

	String testName = "<complete on method/field of array>";
	String completeBehind = "int[].";
	String expectedCompletionNodeToString = "<CompleteOnClassLiteralAccess:int[].>";
	String completionIdentifier = "";
	String expectedReplacedSource = "int[].";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class L {
		  public L() {
		  }
		  void foo() {
		    x.y.Z[] field1;
		    x.y.Z[] field2;
		    <CompleteOnClassLiteralAccess:int[].>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testL_3() {
	String str =
		"""
		package p;\s
		public class L {
			void foo() {
				x.y.Z[] field1,\s
					field2;\s
				field1.if (int[].class }
		}
		""";

	String testName = "<complete on argument of corrupted if statement>";
	String completeBehind = "int";
	String expectedCompletionNodeToString = "<CompleteOnName:int>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class L {
		  public L() {
		  }
		  void foo() {
		    x.y.Z[] field1;
		    x.y.Z[] field2;
		    if (<CompleteOnName:int>)
		        ;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testLA_1FGLMOF() {
	String str =
			"""
		package p;\s
		/**
		 * 1FGLMOF
		 */
		public class LA {
			void[] foo() {
			}
			void bar() {
				f }
		}
		""";

	String testName = "<complete on method/field with corrupted method header>";
	String completeBehind = "f";
	String expectedCompletionNodeToString = "<CompleteOnName:f>";
	String completionIdentifier = "f";
	String expectedReplacedSource = "f";
	int cursorLocation = str.indexOf("f }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class LA {
		  public LA() {
		  }
		  void[] foo() {
		  }
		  void bar() {
		    <CompleteOnName:f>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testLB() {
	String str =
			"""
		package p;\s
		public class LB {
			void foo() {
			}
			void foo() {
			}
			void bar() {
				i }
		}
		""";

	String testName = "<complete on method/field with duplicate method declaration>";
	String completeBehind = "i";
	String expectedCompletionNodeToString = "<CompleteOnName:i>";
	String completionIdentifier = "i";
	String expectedReplacedSource = "i";
	int cursorLocation = str.indexOf("i }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class LB {
		  public LB() {
		  }
		  void foo() {
		  }
		  void foo() {
		  }
		  void bar() {
		    <CompleteOnName:i>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testLC_1FLG1E2() {
	String str =
			"""
		package p;\s
		/**
		 * 1FLG1E2
		 */
		public class LC {
			void foo() {
				Object[] x = new Object[10];
				x [1].
			}
		}
		""";

	String testName = "<complete on method/field of array element with dummy spaces>";
	String completeBehind = "x [1].";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:x[1].>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class LC {
		  public LC() {
		  }
		  void foo() {
		    Object[] x;
		    <CompleteOnMemberAccess:x[1].>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testM_1FGGLMT() {
	String str =
		"""
		package p;\s
		/**
		 * 1FGGLMT
		 */
		public class M {
			class Member {
				void fooMember() {
				}
			}
			void foo() {
				new Member().}
		}
		class MemberOfCU {
		}
		""";

	String testName = "<complete on method/field of explicit object creation>";
	String completeBehind = "new Member().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new Member().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class M {
		  class Member {
		    Member() {
		    }
		    void fooMember() {
		    }
		  }
		  public M() {
		  }
		  void foo() {
		    <CompleteOnMemberAccess:new Member().>;
		  }
		}
		class MemberOfCU {
		  MemberOfCU() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testMA_1() {
	String str =
			"""
		package p;\s
		public class MA {
			class Member
				extends java.util.Vector {
				static void fooStaticMember() {
				}
				void fooMember() {
				}
				class MemberMember {
					void fooMemberMember() {
						MemberOfCUMA m =\s
							new MemberOfCUMA();\s
					}
				}
				class MemberMember2 {
				}
			}
			void foo() {
				Membe }
			void foobar() {
				new Member().}
			class Member2 {
			}
		}
		class MemberOfCUMA {
		}
		""";

	String testName = "<complete on local variable declaration type>";
	String completeBehind = "Membe";
	String expectedCompletionNodeToString = "<CompleteOnName:Membe>";
	String completionIdentifier = "Membe";
	String expectedReplacedSource = "Membe";
	int cursorLocation = str.indexOf("Membe }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class MA {
		  class Member extends java.util.Vector {
		    class MemberMember {
		      MemberMember() {
		      }
		      void fooMemberMember() {
		      }
		    }
		    class MemberMember2 {
		      MemberMember2() {
		      }
		    }
		    Member() {
		    }
		    static void fooStaticMember() {
		    }
		    void fooMember() {
		    }
		  }
		  class Member2 {
		    Member2() {
		    }
		  }
		  public MA() {
		  }
		  void foo() {
		    <CompleteOnName:Membe>;
		  }
		  void foobar() {
		  }
		}
		class MemberOfCUMA {
		  MemberOfCUMA() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testMA_2() {
	String str =
			"""
		package p;\s
		public class MA {
			class Member
				extends java.util.Vector {
				static void fooStaticMember() {
				}
				void fooMember() {
				}
				class MemberMember {
					void fooMemberMember() {
						MemberOfCUMA m =\s
							new MemberOfCUMA();\s
					}
				}
				class MemberMember2 {
				}
			}
			void foo() {
				Membe }
			void foobar() {
				new Member().}
			class Member2 {
			}
		}
		class MemberOfCUMA {
		}
		""";

	String testName = "<complete on object creation type>";
	String completeBehind = "MemberOfCU";
	String expectedCompletionNodeToString = "<CompleteOnType:MemberOfCU>";
	String completionIdentifier = "MemberOfCU";
	String expectedReplacedSource = "MemberOfCUMA";
	int cursorLocation = str.indexOf("MemberOfCUMA();") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class MA {
		  class Member extends java.util.Vector {
		    class MemberMember {
		      MemberMember() {
		      }
		      void fooMemberMember() {
		        MemberOfCUMA m = new <CompleteOnType:MemberOfCU>();
		      }
		    }
		    class MemberMember2 {
		      MemberMember2() {
		      }
		    }
		    Member() {
		    }
		    static void fooStaticMember() {
		    }
		    void fooMember() {
		    }
		  }
		  class Member2 {
		    Member2() {
		    }
		  }
		  public MA() {
		  }
		  void foo() {
		  }
		  void foobar() {
		  }
		}
		class MemberOfCUMA {
		  MemberOfCUMA() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testMA_3() {
	String str =
			"""
		package p;\s
		public class MA {
			class Member
				extends java.util.Vector {
				static void fooStaticMember() {
				}
				void fooMember() {
				}
				class MemberMember {
					void fooMemberMember() {
						MemberOfCUMA m =\s
							new MemberOfCUMA();\s
					}
				}
				class MemberMember2 {
				}
			}
			void foo() {
				Membe }
			void foobar() {
				new Member().}
			class Member2 {
			}
		}
		class MemberOfCUMA {
		}
		""";

	String testName = "<complete on method/field of object creation>";
	String completeBehind = "new Member().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new Member().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class MA {
		  class Member extends java.util.Vector {
		    class MemberMember {
		      MemberMember() {
		      }
		      void fooMemberMember() {
		      }
		    }
		    class MemberMember2 {
		      MemberMember2() {
		      }
		    }
		    Member() {
		    }
		    static void fooStaticMember() {
		    }
		    void fooMember() {
		    }
		  }
		  class Member2 {
		    Member2() {
		    }
		  }
		  public MA() {
		  }
		  void foo() {
		  }
		  void foobar() {
		    <CompleteOnMemberAccess:new Member().>;
		  }
		}
		class MemberOfCUMA {
		  MemberOfCUMA() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testMB_1FHSLMQ_1() {
	String str =
			"""
		package p;\s
		/**
		 * 1FHSLMQ
		 */
		public class MB {
			void foo() {
				try {
					System.out.println("");
				} catch (Exception eFirst) {
					e } catch (Exception eSecond) {
					e }
			}
		}
		""";

	String testName = "<complete on local variable name into catch block>";
	String completeBehind = "e";
	String expectedCompletionNodeToString = "<CompleteOnName:e>";
	String completionIdentifier = "e";
	String expectedReplacedSource = "e";
	int cursorLocation = str.indexOf("e }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class MB {
		  public MB() {
		  }
		  void foo() {
		    {
		      Exception eFirst;
		      <CompleteOnName:e>;
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testMB_1FHSLMQ_2() {
	String str =
			"""
		package p;\s
		/**
		 * 1FHSLMQ
		 */
		public class MB {
			void foo() {
				try {
					System.out.println("");
				} catch (Exeption eFirst) {
					e } catch (Exception eSecond) {
					e }
			}
		}
		""";

	String testName = "<complete on local variable name into catch block>";
	String completeBehind = "e";
	String expectedCompletionNodeToString = "<CompleteOnName:e>";
	String completionIdentifier = "e";
	String expectedReplacedSource = "e";
	int cursorLocation = str.indexOf("e }\n") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class MB {
		  public MB() {
		  }
		  void foo() {
		    {
		      Exception eSecond;
		      <CompleteOnName:e>;
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testMC_1FJ8D9Z() {
	String str =
			"""
		package p;\s
		/**
		 * 1FJ8D9Z
		 */
		public class MC {
			p2.X someField;
			public void foo() {
				new p2.X(
			}
		}
		""";

	String testName = "<complete on object creation argument>";
	String completeBehind = "new p2.X(";
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new p2.X()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class MC {
		  p2.X someField;
		  public MC() {
		  }
		  public void foo() {
		    <CompleteOnAllocationExpression:new p2.X()>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testN() {
	String str =
		"""
		package p;\s
		public class N extends p.M {
			void foo() {
				class MLocal
					extends Schmurz {
					void foo() {
					}
					int field1;
					class MLocalMember
						extends myInnerC {
						void foo() {
						}
						void bar() {
							new M }
					}
					class MLocalMember2 {
						void fooMyInnerC() {
						}
					}
				}
			}
		}
		""";

	String testName = "<complete on object creation type>";
	String completeBehind = "new M";
	String expectedCompletionNodeToString = "<CompleteOnType:M>";
	String completionIdentifier = "M";
	String expectedReplacedSource = "M";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class N extends p.M {
		  public N() {
		  }
		  void foo() {
		    class MLocal extends Schmurz {
		      class MLocalMember extends myInnerC {
		        MLocalMember() {
		        }
		        void foo() {
		        }
		        void bar() {
		          new <CompleteOnType:M>();
		        }
		      }
		      class MLocalMember2 {
		        MLocalMember2() {
		        }
		        void fooMyInnerC() {
		        }
		      }
		      int field1;
		      MLocal() {
		      }
		      void foo() {
		      }
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testNA_1() {
	String str =
			"""
		package p;\s
		class NException2
			extends NoClassDefFoundError {
		}
		interface NInterface {
			void foo();
		}
		class DAB {
			public DA foo() {
			}
			public int foufou;
		}
		class DANA {
			public int f;
			N fieldC;
		}
		public class NA
			extends NException2
			implements N {
			DA fieldB;
			class freak {
			}
			void dede() {
				DA local;
				local.fieldC.foo();
			}
		}
		interface NCool {
		}
		""";

	String testName = "<complete on local variable name>";
	String completeBehind = "l";
	String expectedCompletionNodeToString = "<CompleteOnName:l>";
	String completionIdentifier = "l";
	String expectedReplacedSource = "local";
	int cursorLocation = str.indexOf("local.") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class NException2 extends NoClassDefFoundError {
		  NException2() {
		  }
		}
		interface NInterface {
		  void foo();
		}
		class DAB {
		  public int foufou;
		  DAB() {
		  }
		  public DA foo() {
		  }
		}
		class DANA {
		  public int f;
		  N fieldC;
		  DANA() {
		  }
		}
		public class NA extends NException2 implements N {
		  class freak {
		    freak() {
		    }
		  }
		  DA fieldB;
		  public NA() {
		  }
		  void dede() {
		    DA local;
		    <CompleteOnName:l>;
		  }
		}
		interface NCool {
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testNA_2() {
	String str =
			"""
		package p;\s
		class NException2
			extends NoClassDefFoundError {
		}
		interface NInterface {
			void foo();
		}
		class DAB {
			public DA foo() {
			}
			public int foufou;
		}
		class DANA {
			public int f;
			N fieldC;
		}
		public class NA
			extends NException2
			implements N {
			DA fieldB;
			class freak {
			}
			void dede() {
				DA local;
				local.fieldC.foo();
			}
		}
		interface NCool {
		}
		""";

	String testName = "<complete on method/field of local variable>";
	String completeBehind = "local.f";
	String expectedCompletionNodeToString = "<CompleteOnName:local.f>";
	String completionIdentifier = "f";
	String expectedReplacedSource = "local.fieldC";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class NException2 extends NoClassDefFoundError {
		  NException2() {
		  }
		}
		interface NInterface {
		  void foo();
		}
		class DAB {
		  public int foufou;
		  DAB() {
		  }
		  public DA foo() {
		  }
		}
		class DANA {
		  public int f;
		  N fieldC;
		  DANA() {
		  }
		}
		public class NA extends NException2 implements N {
		  class freak {
		    freak() {
		    }
		  }
		  DA fieldB;
		  public NA() {
		  }
		  void dede() {
		    DA local;
		    <CompleteOnName:local.f>;
		  }
		}
		interface NCool {
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testNA_3() {
	String str =
			"""
		package p;\s
		class NException2
			extends NoClassDefFoundError {
		}
		interface NInterface {
			void foo();
		}
		class DAB {
			public DA foo() {
			}
			public int foufou;
		}
		class DANA {
			public int f;
			N fieldC;
		}
		public class NA
			extends NException2
			implements N {
			DA fieldB;
			class freak {
			}
			void dede() {
				DA local;
				local.fieldC.foo();
			}
		}
		interface NCool {
		}
		""";

	String testName = "<complete on method/field of local variable>";
	String completeBehind = "local.fieldC.";
	String expectedCompletionNodeToString = "<CompleteOnName:local.fieldC.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "local.fieldC.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class NException2 extends NoClassDefFoundError {
		  NException2() {
		  }
		}
		interface NInterface {
		  void foo();
		}
		class DAB {
		  public int foufou;
		  DAB() {
		  }
		  public DA foo() {
		  }
		}
		class DANA {
		  public int f;
		  N fieldC;
		  DANA() {
		  }
		}
		public class NA extends NException2 implements N {
		  class freak {
		    freak() {
		    }
		  }
		  DA fieldB;
		  public NA() {
		  }
		  void dede() {
		    DA local;
		    <CompleteOnName:local.fieldC.>;
		  }
		}
		interface NCool {
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testNB() {
	String str =
			"""
		package p;\s
		public class NB {
			void foo() {
				int iOutside;
				if (i != 0) {
					for (int i = 10; --i >= 0;)
						unit[i].parseMethod(
							parser,\s
							unit);\s
				}
			}
		}
		""";

	String testName = "<complete on variable name into for statement>";
	String completeBehind = "i";
	String expectedCompletionNodeToString = "<CompleteOnName:i>";
	String completionIdentifier = "i";
	String expectedReplacedSource = "i";
	int cursorLocation = str.indexOf("i >=") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class NB {
		  public NB() {
		  }
		  void foo() {
		    int iOutside;
		    if ((i != 0))
		        {
		          for (int i;; ((-- <CompleteOnName:i>) >= 0); )\s
		            unit[i].parseMethod(parser, unit);
		        }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testNC_1FJ8D9Z() {
	String str =
			"""
		package p;\s
		/**
		 * 1FJ8D9Z
		 */
		public class NC {
			String s = new String(
		""";

	String testName = "<complete on field intializer into corrupted class declaration>";
	String completeBehind = "new String(";
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new String()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class NC {
		  String s = <CompleteOnAllocationExpression:new String()>;
		  public NC() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testO_1FG1YU0() {
	String str =
		"""
		package p;\s
		/**
		 * 1FG1YU0
		 */
		public class O
			extends java.util.Vector {
			void bar(boolean bbbb) {
				this.}
		}
		""";

	String testName = "<complete on method/field of explicit this>";
	String completeBehind = "this.";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:this.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "this.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class O extends java.util.Vector {
		  public O() {
		  }
		  void bar(boolean bbbb) {
		    <CompleteOnMemberAccess:this.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testOA_1() {
	String str =
			"""
		package p;\s
		public class OA {
			void proc() {
				int[] a = new int[10];
				Object b = a;
				Class c = a.getClass();
				String s = a.toString();
				boolean l = a.equals(b);
				int h = a.hashCode();
				try {
					a.wait();
					a.wait(3);
					a.wait(4, 5);
				} catch (Exception e) {
				}
				a.notify();
				a.notifyAll();
			}
		}
		""";

	String testName = "<complete on method/field of array>";
	String completeBehind = "a.n";
	String expectedCompletionNodeToString = "<CompleteOnName:a.n>";
	String completionIdentifier = "n";
	String expectedReplacedSource = "a.notify";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class OA {
		  public OA() {
		  }
		  void proc() {
		    int[] a;
		    Object b;
		    Class c;
		    String s;
		    boolean l;
		    int h;
		    <CompleteOnName:a.n>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testOA_2() {
	String str =
			"""
		package p;\s
		public class OA {
			void proc() {
				int[] a = new int[10];
				Object b = a;
				Class c = a.getClass();
				String s = a.toString();
				boolean l = a.equals(b);
				int h = a.hashCode();
				try {
					a.wait();
					a.wait(3);
					a.wait(4, 5);
				} catch (Exception e) {
				}
				a.notify();
				a.notifyAll();
			}
		}
		""";

	String testName = "<complete on method/field of array>";
	String completeBehind = "a.w";
	String expectedCompletionNodeToString = "<CompleteOnName:a.w>";
	String completionIdentifier = "w";
	String expectedReplacedSource = "a.wait";
	int cursorLocation = str.indexOf("a.wait(4, 5)") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class OA {
		  public OA() {
		  }
		  void proc() {
		    int[] a;
		    Object b;
		    Class c;
		    String s;
		    boolean l;
		    int h;
		    {
		      <CompleteOnName:a.w>;
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testOB_1() {
	String str =
			"""
		package p;\s
		public class OB {
			void foo() {
				label : while (true) {
					System.out.println("");
					break label;
				}
			}
		}
		""";

	String testName = "<complete on keyword>";
	String completeBehind = "b";
	String expectedCompletionNodeToString = "<CompleteOnType:b>";
	String completionIdentifier = "b";
	String expectedReplacedSource = "break";
	int cursorLocation = str.indexOf("break") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class OB {
		  public OB() {
		  }
		  void foo() {
		    label: while (true)  {
		    System.out.println("");
		    <CompleteOnType:b> label;
		  }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testOB_2() {
	String str =
			"""
		package p;\s
		public class OB {
			void foo() {
				label : while (true) {
					System.out.println("");
					break label;
				}
			}
		}
		""";

	String testName = "<complete on label name>";
	String completeBehind = "l";
	String expectedCompletionNodeToString = "<CompleteOnName:l>";
	String completionIdentifier = "l";
	String expectedReplacedSource = "label";
	int cursorLocation = str.indexOf("label") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class OB {
		  public OB() {
		  }
		  void foo() {
		    <CompleteOnName:l>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testOC_1FM7J7F() {
	String str =
			"""
		package p;\s
		/**
		 * 1FM7J7F
		 */
		class OC {
			String s = new String(
		}
		""";

	String testName = "<complete on field initializer>";
	String completeBehind = "new String(";
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new String()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class OC {
		  String s = <CompleteOnAllocationExpression:new String()>;
		  OC() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testP_1FG1YU0() {
	String str =
		"""
		package p;\s
		/**
		 * 1FG1YU0
		 */
		public class P {
			{
				void bar() {
					f }
				void foo() {
				}
			}
		""";

	String testName = "<complete on method/field>";
	String completeBehind = "f";
	String expectedCompletionNodeToString = "<CompleteOnName:f>";
	String completionIdentifier = "f";
	String expectedReplacedSource = "f";
	int cursorLocation = str.indexOf("f }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class P {
		  {
		  }
		  public P() {
		  }
		  void bar() {
		    <CompleteOnName:f>;
		  }
		  void foo() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName); }
public void testPA_1() {
	String str =
			"""
		package p;\s
		class PAHelper {
			public int fieldPublic;
			protected int fieldProtected;
			private int fieldPrivate;
			int fieldDefault;
			static void staticFoo() {
			}
			static int i = 1;
			int neuneu1() {
				return 0;
			}
			void neuneu2() {
			}
		}
		public class PA
			extends PAHelper {
			void foo() {
				B[] b =\s
					new java.lang.Number[];\s
				java.lang.Short s;
				// b[1].;
			}
		}
		""";

	String testName = "<complete on comment>";
	String completeBehind = "b[1].";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "";
	String expectedReplacedSource = NONE;
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString = null;

	try {
		checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
		assertTrue("failed to detect invalid cursor location", false);
	} catch(InvalidCursorLocation e){
		assertEquals("invalid cursor location: ", e.irritant, InvalidCursorLocation.NO_COMPLETION_INSIDE_COMMENT);
	}
}
public void testPA_2() {
	String str =
			"""
		package p;\s
		class PAHelper {
			public int fieldPublic;
			protected int fieldProtected;
			private int fieldPrivate;
			int fieldDefault;
			static void staticFoo() {
			}
			static int i = 1;
			int neuneu1() {
				return 0;
			}
			void neuneu2() {
			}
		}
		public class PA
			extends PAHelper {
			void foo() {
				B[] b =\s
					new java.lang.Number[];\s
				java.lang.Short s;
				// b[1].;
			}
		}
		""";

	String testName = "<complete on new keyword>";
	String completeBehind = "n";
	String expectedCompletionNodeToString = "<CompleteOnName:n>";
	String completionIdentifier = "n";
	String expectedReplacedSource = "new";
	int cursorLocation = str.indexOf("new ") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class PAHelper {
		  public int fieldPublic;
		  protected int fieldProtected;
		  private int fieldPrivate;
		  int fieldDefault;
		  static int i;
		  <clinit>() {
		  }
		  PAHelper() {
		  }
		  static void staticFoo() {
		  }
		  int neuneu1() {
		  }
		  void neuneu2() {
		  }
		}
		public class PA extends PAHelper {
		  public PA() {
		  }
		  void foo() {
		    B[] b = <CompleteOnName:n>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testPB() {
	String str =
			"""
		package p;\s
		public class PB {
			void foo() {
				class Local {
					void foo() {
					}
					class LocalMember1 {
						void foo() {
							class LocalMemberLocal {
								void foo() {
									f
								}
							}
						}
					}
					class LocalMember2 {
						void foo() {
						}
					}
				}
			}
		}
		""";

	String testName = "<complete on method/field into nested local type>";
	String completeBehind = "f";
	String expectedCompletionNodeToString = "<CompleteOnName:f>";
	String completionIdentifier = "f";
	String expectedReplacedSource = "f";
	int cursorLocation = str.indexOf("f\n") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class PB {
		  public PB() {
		  }
		  void foo() {
		    class Local {
		      class LocalMember1 {
		        LocalMember1() {
		        }
		        void foo() {
		          class LocalMemberLocal {
		            LocalMemberLocal() {
		            }
		            void foo() {
		              <CompleteOnName:f>;
		            }
		          }
		        }
		      }
		      class LocalMember2 {
		        LocalMember2() {
		        }
		        void foo() {
		        }
		      }
		      Local() {
		      }
		      void foo() {
		      }
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testPC_1FSU4EF() {
	String str =
			"""
		package p;\s
		/**
		 * 1FSU4EF
		 */
		import java.util.Vector;
		public class PC {
			void foo() {
				class Inner {
					Vector v = new Vector();
					void foo() {
						Vector v = new Vector();
						v.addElement();
					}
				}
			}
		}
		""";

	String testName = "<complete on method/field into local type>";
	String completeBehind = "v.a";
	String expectedCompletionNodeToString = "<CompleteOnName:v.a>";
	String completionIdentifier = "a";
	String expectedReplacedSource = "v.addElement";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import java.util.Vector;
		public class PC {
		  public PC() {
		  }
		  void foo() {
		    class Inner {
		      Vector v;
		      Inner() {
		      }
		      void foo() {
		        Vector v;
		        <CompleteOnName:v.a>;
		      }
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testQ_1FG1YU0() {
	String str =
		"""
		package p;\s
		/**
		 * 1FG1YU0
		 */
		public class Q {
			void bar(boolean bbbb) {
				this.}
		}
		""";

	String testName = "<complete on method/field>";
	String completeBehind = "this.";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:this.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "this.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class Q {
		  public Q() {
		  }
		  void bar(boolean bbbb) {
		    <CompleteOnMemberAccess:this.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testQA_1() {
	String str =
			"""
		package p;\s
		class QAHelper {
			int i = 10;
			void f() {
				Chk.chkIntVal(
					"err_0",\s
					"i",\s
					this.i,\s
					i);\s
			}
			static class Y
				extends QAHelper {
				public void f() {
					super.f();
					int j = super.i;
				}
				public static void main(String a[]) {
					Y oy = new Y();
					oy.f();
				}
			}
		}
		public class QA {
			static String s[] =\s
				{"Dolby", "Thx",};\s
			void check() {
				new QAHelper().new Y().main(
					s);\s
			}
			static public void main(String args[]) {
				new QA().check();
				Chk.endTest("ciner111");
			}
		}
		""";

	String testName = "<complete on method/field>";
	String completeBehind = "new QAHelper().new Y().m";
	String expectedCompletionNodeToString = "<CompleteOnMessageSendName:new QAHelper().new Y().m()>";
	String completionIdentifier = "m";
	String expectedReplacedSource = "main";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class QAHelper {
		  static class Y extends QAHelper {
		    Y() {
		    }
		    public void f() {
		    }
		    public static void main(String[] a) {
		    }
		  }
		  int i;
		  QAHelper() {
		  }
		  void f() {
		  }
		}
		public class QA {
		  static String[] s;
		  <clinit>() {
		  }
		  public QA() {
		  }
		  void check() {
		    <CompleteOnMessageSendName:new QAHelper().new Y().m()>;
		  }
		  public static void main(String[] args) {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testQA_2() {
	String str =
			"""
		package p;\s
		class QAHelper {
			int i = 10;
			void f() {
				Chk.chkIntVal(
					"err_0",\s
					"i",\s
					this.i,\s
					i);\s
			}
			static class Y
				extends QAHelper {
				public void f() {
					super.f();
					int j = super.i;
				}
				public static void main(String a[]) {
					Y oy = new Y();
					oy.f();
				}
			}
		}
		public class QA {
			static String s[] =\s
				{"Dolby", "Thx",};\s
			void check() {
				new QAHelper().new Y().main(
					s);\s
			}
			static public void main(String args[]) {
				new QA().check();
				Chk.endTest("ciner111");
			}
		}
		""";

	String testName = "<complete on method/field of object creation>";
	String completeBehind = "new QAHelper().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new QAHelper().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class QAHelper {
		  static class Y extends QAHelper {
		    Y() {
		    }
		    public void f() {
		    }
		    public static void main(String[] a) {
		    }
		  }
		  int i;
		  QAHelper() {
		  }
		  void f() {
		  }
		}
		public class QA {
		  static String[] s;
		  <clinit>() {
		  }
		  public QA() {
		  }
		  void check() {
		    <CompleteOnMemberAccess:new QAHelper().>;
		  }
		  public static void main(String[] args) {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testQA_3() {
	String str =
			"""
		package p;\s
		class QAHelper {
			int i = 10;
			void f() {
				Chk.chkIntVal(
					"err_0",\s
					"i",\s
					this.i,\s
					i);\s
			}
			static class Y
				extends QAHelper {
				public void f() {
					super.f();
					int j = super.i;
				}
				public static void main(String a[]) {
					Y oy = new Y();
					oy.f();
				}
			}
		}
		public class QA {
			static String s[] =\s
				{"Dolby", "Thx",};\s
			void check() {
				new QAHelper().new Y().main(
					s);\s
			}
			static public void main(String args[]) {
				new QA().check();
				Chk.endTest("ciner111");
			}
		}
		""";

	String testName = "<complete on method/field of object creation>";
	String completeBehind = "new QAHelper().new Y().";
	String expectedCompletionNodeToString = "<CompleteOnMessageSendName:new QAHelper().new Y().()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "main";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class QAHelper {
		  static class Y extends QAHelper {
		    Y() {
		    }
		    public void f() {
		    }
		    public static void main(String[] a) {
		    }
		  }
		  int i;
		  QAHelper() {
		  }
		  void f() {
		  }
		}
		public class QA {
		  static String[] s;
		  <clinit>() {
		  }
		  public QA() {
		  }
		  void check() {
		    <CompleteOnMessageSendName:new QAHelper().new Y().()>;
		  }
		  public static void main(String[] args) {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testQA_4() {
	String str =
			"""
		package p;\s
		class QAHelper {
			int i = 10;
			void f() {
				Chk.chkIntVal(
					"err_0",\s
					"i",\s
					this.i,\s
					i);\s
			}
			static class Y
				extends QAHelper {
				public void f() {
					super.f();
					int j = super.i;
				}
				public static void main(String a[]) {
					Y oy = new Y();
					oy.f();
				}
			}
		}
		public class QA {
			static String s[] =\s
				{"Dolby", "Thx",};\s
			void check() {
				new QAHelper().new Y().main(
					s);\s
			}
			static public void main(String args[]) {
				new QA().check();
				Chk.endTest("ciner111");
			}
		}
		""";

	String testName = "<complete on method/field of object creation>";
	String completeBehind = "new QA().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new QA().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class QAHelper {
		  static class Y extends QAHelper {
		    Y() {
		    }
		    public void f() {
		    }
		    public static void main(String[] a) {
		    }
		  }
		  int i;
		  QAHelper() {
		  }
		  void f() {
		  }
		}
		public class QA {
		  static String[] s;
		  <clinit>() {
		  }
		  public QA() {
		  }
		  void check() {
		  }
		  public static void main(String[] args) {
		    <CompleteOnMemberAccess:new QA().>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testQB_1FIK820() {
	String str =
			"""
		package p;\s
		/**
		 * 1FIK820
		 */
		public class QB {
			void foo() {
				{
				}
				.}
		}
		""";

	String testName = "<complete on block (no answers wanted)>";
	String completeBehind = ".";
	String expectedCompletionNodeToString = "<CompleteOnName:>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(".}") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class QB {
		  public QB() {
		  }
		  void foo() {
		    <CompleteOnName:>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testR_1FGD31E() {
	String str =
		"""
		package p;\s
		/**
		 * 1FGD31E
		 */
		public class R {
			void moo() {
				b }
			void bar() {
			}
		}
		""";

	String testName = "<complete on method/field>";
	String completeBehind = "b";
	String expectedCompletionNodeToString = "<CompleteOnName:b>";
	String completionIdentifier = "b";
	String expectedReplacedSource = "b";
	int cursorLocation = str.indexOf("b }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class R {
		  public R() {
		  }
		  void moo() {
		    <CompleteOnName:b>;
		  }
		  void bar() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testRA_1() {
	String str =
			"""
		package p;\s
		public class RA extends A {
			private int f = 5;
			int i(int k) {
			}
			class B extends I {
				void foo();
				class C extends Z {
				}
				final int fo;
			}
			final void foo(k j) {
			}
			o o() throws Exc, Exc {
			}
			static {
				this.ff = 5;
			}
		}
		""";

	String testName = "<complete on incorrect this call>";
	String completeBehind = "t";
	String expectedCompletionNodeToString = "<CompleteOnName:t>";
	String completionIdentifier = "t";
	String expectedReplacedSource = "this";
	int cursorLocation = str.indexOf("this.ff") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class RA extends A {
		  class B extends I {
		    class C extends Z {
		      C() {
		      }
		    }
		    final int fo;
		    B() {
		    }
		    void foo();
		  }
		  private int f;
		  static {
		    <CompleteOnName:t>;
		  }
		  <clinit>() {
		  }
		  public RA() {
		  }
		  int i(int k) {
		  }
		  final void foo(k j) {
		  }
		  o o() throws Exc, Exc {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testRA_2() {
	String str =
			"""
		package p;\s
		public class RA extends A {
			private int f = 5;
			int i(int k) {
			}
			class B extends I {
				void foo();
				class C extends Z {
				}
				final int fo;
			}
			final void foo(k j) {
			}
			o o() throws Exc, Exc {
			}
			static {
				this.ff = 5;
			}
		}
		""";

	String testName = "<complete on t>";
	String completeBehind = "t";
	String expectedCompletionNodeToString = "<CompleteOnName:t>";
	String completionIdentifier = "t";
	String expectedReplacedSource = "this";
	int cursorLocation = str.indexOf("this") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class RA extends A {
		  class B extends I {
		    class C extends Z {
		      C() {
		      }
		    }
		    final int fo;
		    B() {
		    }
		    void foo();
		  }
		  private int f;
		  static {
		    <CompleteOnName:t>;
		  }
		  <clinit>() {
		  }
		  public RA() {
		  }
		  int i(int k) {
		  }
		  final void foo(k j) {
		  }
		  o o() throws Exc, Exc {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testRA_3() {
	String str =
			"""
		package p;\s
		public class RA extends A {
			private int f = 5;
			int i(int k) {
			}
			class B extends I {
				void foo();
				class C extends Z {
				}
				final int fo;
			}
			final void foo(k j) {
			}
			o o() throws Exc, Exc {
			}
			static {
				this.ff = 5;
			}
		}
		""";

	String testName = "<complete on exception type>";
	String completeBehind = "Exc";
	String expectedCompletionNodeToString = "<CompleteOnException:Exc>";
	String completionIdentifier = "Exc";
	String expectedReplacedSource = "Exc";
	int cursorLocation = str.indexOf("Exc {") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class RA extends A {
		  class B extends I {
		    class C extends Z {
		      C() {
		      }
		    }
		    final int fo;
		    B() {
		    }
		    void foo();
		  }
		  private int f;
		  static {
		  }
		  <clinit>() {
		  }
		  public RA() {
		  }
		  int i(int k) {
		  }
		  final void foo(k j) {
		  }
		  o o() throws Exc, <CompleteOnException:Exc> {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testRB_1FI74S3() {
	String str =
			"""
		package p;\s
		/**
		 * 1FI74S3
		 */
		public class RB {
			int[] table;
			void foo() {
				int x = table.}
		}
		""";

	String testName = "<complete on method/field of arry>";
	String completeBehind = "table.";
	String expectedCompletionNodeToString = "<CompleteOnName:table.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "table.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class RB {
		  int[] table;
		  public RB() {
		  }
		  void foo() {
		    int x = <CompleteOnName:table.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testS_1FGF64P_1() {
	String str =
		"""
		package p;\s
		/**
		 * 1FGF64P
		 */
		public class S {
			{
				new Y()..}
			class Y {
				void foo() {
				}
			}
		}
		""";

	String testName = "<complete on incorrect call>";
	String completeBehind = "new Y()..";
	String expectedCompletionNodeToString = "<CompleteOnName:>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class S {
		  class Y {
		    Y() {
		    }
		    void foo() {
		    }
		  }
		  {
		    {
		      <CompleteOnName:>;
		    }
		  }
		  public S() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testS_1FGF64P_2() {
	String str =
		"""
		package p;\s
		/**
		 * 1FGF64P
		 */
		public class S {
			{
				new Y()..}
			class Y {
				void foo() {
				}
			}
		}
		""";

	String testName = "<complete on method/field of object creation>";
	String completeBehind = "new Y().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new Y().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class S {
		  class Y {
		    Y() {
		    }
		    void foo() {
		    }
		  }
		  {
		    <CompleteOnMemberAccess:new Y().>;
		  }
		  public S() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testS_1FGF64P_3() {
	String str =
		"""
		package p;\s
		/**
		 * 1FGF64P
		 */
		public class S {
			{
				new Y()..}
			class Y {
				void foo() {
				}
			}
		}
		""";

	String testName = "<complete on incorrect call>";
	String completeBehind = "new Y()..";
	String expectedCompletionNodeToString = "<CompleteOnName:>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class S {
		  class Y {
		    Y() {
		    }
		    void foo() {
		    }
		  }
		  {
		    {
		      <CompleteOnName:>;
		    }
		  }
		  public S() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testSA() {
	String str =
			"""
		package p;\s
		public class SA {
			public sy void foo() {
			}
		}
		""";

	String testName = "<complete on method modifier>";
	String completeBehind = "sy";
	String expectedCompletionNodeToString = "<CompleteOnType:sy>";
	String completionIdentifier = "sy";
	String expectedReplacedSource = "sy";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class SA {
		  <CompleteOnType:sy>;
		  public SA() {
		  }
		  void foo() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testSB_1FILFDG() {
	String str =
			"""
		package p;\s
		/**
		 * 1FILFDG
		 */
		public class SB {
			public void foo() {
				String s = "hello
				int}
		}
		""";

	String testName = "<complete on field declaration type>";
	String completeBehind = "int";
	String expectedCompletionNodeToString = "<CompleteOnName:int>";
	String completionIdentifier = "int";
	String expectedReplacedSource = "int";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class SB {
		  public SB() {
		  }
		  public void foo() {
		    String s;
		    <CompleteOnName:int>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testT_1FGF64P() {
	String str =
		"""
		package p;\s
		/**
		 * 1FGF64P
		 */
		public class T {
			{
				new Y().}
			class Y {
				void foo() {
				}
			}
		}
		""";

	String testName = "<complete on object creation>";
	String completeBehind = "new Y().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new Y().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class T {
		  class Y {
		    Y() {
		    }
		    void foo() {
		    }
		  }
		  {
		    <CompleteOnMemberAccess:new Y().>;
		  }
		  public T() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testTA_1FHISJJ_1() {
	String str =
			"""
		package p;\s
		/**
		 * 1FHISJJ
		 */
		public class TA {
			void foo() {
				Object[] items =\s
					{
						"Mark unublishable",\s
						null,\s
						"Properties..."}
				.;
				items.}
		}
		""";

	String testName = "<complete on array intializer value>";
	String completeBehind = "n";
	String expectedCompletionNodeToString = "<CompleteOnName:n>";
	String completionIdentifier = "n";
	String expectedReplacedSource = "null";
	int cursorLocation = str.indexOf("null, ") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class TA {
		  public TA() {
		  }
		  void foo() {
		    Object[] items = {"Mark unublishable", <CompleteOnName:n>, "Properties..."};
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testTA_1FHISJJ_2() {
	String str =
			"""
		package p;\s
		/**
		 * 1FHISJJ
		 */
		public class TA {
			void foo() {
				Object[] items =\s
					{
						"Mark unublishable",\s
						null,\s
						"Properties..."}
				.;
				items.}
		}
		""";

	String testName = "<complete on method/field of array intializer>";
	String completeBehind =
			"""
					{
						"Mark unublishable",\s
						null,\s
						"Properties..."}
				.\
		""";
	String expectedCompletionNodeToString = "<CompleteOnName:>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class TA {
		  public TA() {
		  }
		  void foo() {
		    Object[] items;
		    <CompleteOnName:>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testTA_1FHISJJ_3() {
	String str =
			"""
		package p;\s
		/**
		 * 1FHISJJ
		 */
		public class TA {
			void foo() {
				Object[] items =\s
					{
						"Mark unublishable",\s
						null,\s
						"Properties..."}
				.;
				items.}
		}
		""";

	String testName = "<complete on method/field of array>";
	String completeBehind = "items.";
	String expectedCompletionNodeToString = "<CompleteOnName:items.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "items.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class TA {
		  public TA() {
		  }
		  void foo() {
		    Object[] items;
		    <CompleteOnName:items.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testTB_1FHSLMQ() {
	String str =
			"""
		package p;\s
		/**
		 * 1FHSLMQ
		 */
		public class TB {
			void foo() {
				if (true)
					System.out.println("");
				e }
		}
		""";

	String testName = "<complete on else keyword>";
	String completeBehind = "e";
	String expectedCompletionNodeToString = "<CompleteOnName:e>";
	String completionIdentifier = "e";
	String expectedReplacedSource = "e";
	int cursorLocation = str.indexOf("e }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class TB {
		  public TB() {
		  }
		  void foo() {
		    <CompleteOnName:e>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testU_1FGGUME() {
	String str =
		"""
		package p;\s
		/**
		 * 1FGGUME
		 */
		public class U {
			public static final int Source =\s
				5;\s
		}
		""";

	String testName = "<complete on digit>";
	String completeBehind = "5";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "";
	String expectedReplacedSource = NONE;
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString = null;

	try {
		checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
		assertTrue("failed to detect invalid cursor location", false);
	} catch(InvalidCursorLocation e){
		assertEquals("invalid cursor location: ", e.irritant, InvalidCursorLocation.NO_COMPLETION_INSIDE_NUMBER);
	}
}
public void testUA_1FHISJJ_1() {
	String str =
			"""
		package p;\s
		/**
		 * 1FHISJJ
		 */
		public class UA {
			void foo() {
				Object[] items =\s
					new String[] {
						"Mark unublishable",\s
						null,\s
						"Properties..."}
				.;
				items.}
		}
		""";

	String testName = "<complete on array initializer>";
	String completeBehind =
			"""
		new String[] {
						"Mark unublishable",\s
						null,\s
						"Properties..."}
				.""";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new String[]{\"Mark unublishable\", null, \"Properties...\"}.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class UA {
		  public UA() {
		  }
		  void foo() {
		    Object[] items = <CompleteOnMemberAccess:new String[]{"Mark unublishable", null, "Properties..."}.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testUA_1FHISJJ_2() {
	String str =
			"""
		package p;\s
		/**
		 * 1FHISJJ
		 */
		public class UA {
			void foo() {
				Object[] items =\s
					new String[] {
						"Mark unublishable",\s
						null,\s
						"Properties..."}
				.;
				items.}
		}
		""";

	String testName = "<complete on method/field of array>";
	String completeBehind = "items.";
	String expectedCompletionNodeToString = "<CompleteOnName:items.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "items.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class UA {
		  public UA() {
		  }
		  void foo() {
		    Object[] items;
		    <CompleteOnName:items.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testUB_1FSBZ02() {
	String str =
			"""
		package p;\s
		/**
		 * 1FSBZ02
		 */
		class UB {
			void bar() {
			}
			class UBMember {
				void bar2() {
				}
				void foo() {
					b
				}
			}
		}
		""";

	String testName = "<complete on keyword>";
	String completeBehind = "b";
	String expectedCompletionNodeToString = "<CompleteOnName:b>";
	String completionIdentifier = "b";
	String expectedReplacedSource = "b";
	int cursorLocation = str.indexOf("b\n") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class UB {
		  class UBMember {
		    UBMember() {
		    }
		    void bar2() {
		    }
		    void foo() {
		      <CompleteOnName:b>;
		    }
		  }
		  UB() {
		  }
		  void bar() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testV_1FGGUOO_1() {
	String str =
		"""
		package p;\s
		/**
		 * 1FGGUOO
		 */
		public class V i java
			.io
			.Serializable {
		}
		""";

	String testName = "<complete on implements keyword>";
	String completeBehind = "i";
	String expectedCompletionNodeToString = "<CompleteOnKeyword:i>";
	String completionIdentifier = "i";
	String expectedReplacedSource = "i";
	int cursorLocation = str.indexOf("i java") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class V extends <CompleteOnKeyword:i> {
		  {
		  }
		  public V() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testV_1FGGUOO_2() {
	String str =
		"""
		package x.y.z;\s
		/**
		 * 1FGGUOO
		 */
		public class V implements java.io.Serializable {
		}
		""";

	String testName = "<complete on package>";
	String completeBehind = "y";
	String expectedCompletionNodeToString = "<CompleteOnPackage:x.y>";
	String completionIdentifier = "y";
	String expectedReplacedSource =
		"x.y.z";
	int cursorLocation = str.indexOf("y") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package <CompleteOnPackage:x.y>;
		public class V implements java.io.Serializable {
		  public V() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testVA_1FHISJJ_1() {
	String str =
			"""
		package p;\s
		/**
		 * 1FHISJJ
		 */
		public class VA {
			void foo() {
				Object item = new String() {
					public boolean equals() {
						return false;
					}
				}
				.;
				item.}
		}
		""";

	String testName = "<complete on anonymous type declaration>";
	String completeBehind =
			"""
		new String() {
					public boolean equals() {
						return false;
					}
				}
				.""";
	String expectedCompletionNodeToString = """
		<CompleteOnMemberAccess:new String() {
		  public boolean equals() {
		    return false;
		  }
		}.>""";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class VA {
		  public VA() {
		  }
		  void foo() {
		    Object item = <CompleteOnMemberAccess:new String() {
		  public boolean equals() {
		    return false;
		  }
		}.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testVA_1FHISJJ_2() {
	String str =
			"""
		package p;\s
		/**
		 * 1FHISJJ
		 */
		public class VA {
			void foo() {
				Object item = new String() {
					public boolean equals() {
						return false;
					}
				}
				.;
				item.}
		}
		""";

	String testName = "<complete on local variable>";
	String completeBehind = "item.";
	String expectedCompletionNodeToString = "<CompleteOnName:item.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "item.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class VA {
		  public VA() {
		  }
		  void foo() {
		    Object item;
		    <CompleteOnName:item.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testVB_1() {
	String str =
		"""
		package p;\s
		public class VB {
			void foo() {
				new java.io.File("error") {
					protected void runTest() {
						Vector v11111 = new Vector();
						v }
				}
				.;
			}
		}
		""";

	String testName = "<complete on local variable name into anonymous declaration>";
	String completeBehind = "v";
	String expectedCompletionNodeToString = "<CompleteOnName:v>";
	String completionIdentifier = "v";
	String expectedReplacedSource = "v";
	int cursorLocation = str.indexOf("v }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class VB {
		  public VB() {
		  }
		  void foo() {
		    new java.io.File("error") {
		      protected void runTest() {
		        Vector v11111;
		        <CompleteOnName:v>;
		      }
		    };
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
// TODO excluded test (completion on field access on anonymous inner class with syntax error)
public void _testVB_2() {
	String str =
		"""
		package p;\s
		public class VB {
			void foo() {
				new java.io.File("error") {
					protected void runTest() {
						Vector v11111 = new Vector();
						v }
				}.
			}
		}
		""";

	String testName = "<complete on anonymous type declaration>";
	String completeBehind =
		"""
		new java.io.File("error") {
					protected void runTest() {
						Vector v11111 = new Vector();
						v }
				}.""";
	String expectedCompletionNodeToString =
		"""
		<CompleteOnMemberAccess:new java.io.File("error") {
		  protected void runTest() {
		  }
		}.>""";
	String completionIdentifier = "";
	String expectedReplacedSource =
		"""
		new java.io.File("error") {
					protected void runTest() {
						Vector v11111 = new Vector();
						v }
				}.""";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class VB {
		  public VB() {
		  }
		  void foo() {
		    <CompleteOnMemberAccess:new java.io.File("error") {
		  protected void runTest() {
		  }
		}.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testVB_3() {
	String str =
		"""
		package p;\s
		public class VB {
			void foo() {
				new java.io.File("error") {
					protected void runTest() {
						Vector v11111 = new Vector();
						v }
				}
				.;
			}
		}
		""";

	String testName = "<complete on constructor>";
	String completeBehind = "new java.io.File(";
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new java.io.File()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class VB {
		  public VB() {
		  }
		  void foo() {
		    <CompleteOnAllocationExpression:new java.io.File()>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
// TODO excluded test (completion on field access on anonymous inner class with syntax error)
public void _testVB_4() {
	String str =
		"""
		package p;\s
		public class VB {
			void foo() {
				new java.io.File("error") {
					protected void runTest() {
						Vector v11111 = new Vector();
						v }
				}
				.;
			}
		}
		""";

	String testName = "<complete on anonymous type declaration with dummy spaces>";
	String completeBehind =
		"""
		new java.io.File("error") {
					protected void runTest() {
						Vector v11111 = new Vector();
						v }
				}
				.""";
	String expectedCompletionNodeToString =
		"""
		<CompleteOnName:new java.io.File("error") {
		  protected void runTest() {
		  }
		}.>""";
	String completionIdentifier = "";
	String expectedReplacedSource =
		"""
		new java.io.File("error") {
					protected void runTest() {
						Vector v11111 = new Vector();
						v }
				}
				.""";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class VB {
		  public VB() {
		  }
		  void foo() {
		    <CompleteOnMemberAccess:new java.io.File("error") {
		  protected void runTest() {
		  }
		}.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
// TODO excluded test (completion on field access on anonymous inner class with syntax error)
public void _testVB_5() {
	String str =
		"""
		package p;\s
		public class VB {
			void foo() {
				new java.io.File("error") {
					protected void runTest() {
						Vector v11111 = new Vector();
						v }
				}.;
			}
		}
		""";

	String testName = "<complete on anonymous type declaration with trailing semi-colon>";
	String completeBehind =
		"""
		new java.io.File("error") {
					protected void runTest() {
						Vector v11111 = new Vector();
						v }
				}.""";
	String expectedCompletionNodeToString =
		"""
		<CompleteOnMemberAccess:new java.io.File("error") {
		  protected void runTest() {
		  }
		}.>""";
	String completionIdentifier = "";
	String expectedReplacedSource =
		"""
		new java.io.File("error") {
					protected void runTest() {
						Vector v11111 = new Vector();
						v }
				}.""";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class VB {
		  public VB() {
		  }
		  void foo() {
		    <CompleteOnMemberAccess:new java.io.File("error") {
		  protected void runTest() {
		  }
		}.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testW_1FGGUS4() {
	String str =
		"""
		package p;\s
		/**
		 * 1FGGUS4
		 */
		public class W {
			public static final int LA =\s
				1;\s
			public static final int LAB =\s
				2;\s
			public static final int LABO =\s
				4;\s
			public int produceDebugAttributes =\s
				LABO;\s
		}
		""";

	String testName = "<complete on field initializer>";
	String completeBehind = "L";
	String expectedCompletionNodeToString = "<CompleteOnName:L>";
	String completionIdentifier = "L";
	String expectedReplacedSource = "LABO";
	int cursorLocation = str.indexOf("LABO;") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class W {
		  public static final int LA;
		  public static final int LAB;
		  public static final int LABO;
		  public int produceDebugAttributes = <CompleteOnName:L>;
		  public W() {
		  }
		  <clinit>() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testWA_1() {
	String str =
			"""
		package p;\s
		public class WA {
			void foo() {
				int value = 10;
				v int[] tab = new int[value];
			}
		""";

	String testName = "<complete on array size value>";
	String completeBehind = "v";
	String expectedCompletionNodeToString = "<CompleteOnName:v>";
	String completionIdentifier = "v";
	String expectedReplacedSource = "value";
	int cursorLocation = str.indexOf("value];") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class WA {
		  public WA() {
		  }
		  void foo() {
		    int value;
		    int[] tab = new int[<CompleteOnName:v>];
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testWA_2() {
	String str =
			"""
		package p;\s
		public class WA {
			void foo() {
				int value = 10;
				v int[] tab = new int[value];
			}
		""";

	String testName = "<complete on corrupter local variable declaration>";
	String completeBehind = "v";
	String expectedCompletionNodeToString = "<CompleteOnName:v>";
	String completionIdentifier = "v";
	String expectedReplacedSource = "v";
	int cursorLocation = str.indexOf("v int[]") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class WA {
		  public WA() {
		  }
		  void foo() {
		    int value;
		    <CompleteOnName:v>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testWB_1FI313C() {
	String str =
			"""
		package p;\s
		/*
		 * 1FI313C
		 */
		class WBHelper {
			public int fieldPublic;
			protected int fieldProtected;
			private int fieldPrivate;
			int fieldDefault;
			static void staticFoo() {
			}
			static int i = d;
			int neuneu1() {
			}
			void neuneu2() {
			}
		}
		public class WB
			extends WBHelper {
			void foo() {
				BIJOUR[] b =\s
					new java.lang.Number[];\s
				java.lang.Short s;
				b[1].}
			B() {
			}
			B(int) {
			}
		}
		""";

	String testName = "<complete on method/field of array element>";
	String completeBehind = "b[1].";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:b[1].>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		class WBHelper {
		  public int fieldPublic;
		  protected int fieldProtected;
		  private int fieldPrivate;
		  int fieldDefault;
		  static int i;
		  WBHelper() {
		  }
		  <clinit>() {
		  }
		  static void staticFoo() {
		  }
		  int neuneu1() {
		  }
		  void neuneu2() {
		  }
		}
		public class WB extends WBHelper {
		  public WB() {
		  }
		  void foo() {
		    BIJOUR[] b;
		    java.lang.Short s;
		    <CompleteOnMemberAccess:b[1].>;
		  }
		  B() {
		  }
		  B() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testX_1FGGV8C_1() {
	String str =
		"""
		package p;\s
		import p2.Y;\s
		/**
		 * 1FGGV8C and 1FGPE8E
		 */
		public class X {
			public static final float Vars;\s
			public static final float Lines;\s
			public static final float Source;\s
			public static final float UnreachableCode;\s
			public static final float produceDebugAttributes;\s
			void foo() {
				int locale,\s
					errorThreshold,\s
					preserveAllLocalVariables;\s
				return new Y[] {
					new Y(
						"debug.vars",\s
						this,\s
						locale,\s
						(produceDebugAttributes
							& Vars)
							!= 0
							? 0
							: 1),\s
					new Y(
						"debug.lines",\s
						this,\s
						locale,\s
						(produceDebugAttributes
							& Lines)
							!= 0
							? 0
							: 1),\s
					new Y(
						"debug.source",\s
						this,\s
						locale,\s
						(produceDebugAttributes
							& Source)
							!= 0
							? 0
							: 1),\s
					new Y(
						"debug.preserveAllLocals",\s
						this,\s
						locale,\s
						preserveAllLocalVariables
							? 0
							: 1),\s
					new Y(
						"optionalError.unReachableCode",\s
						this,\s
						locale,\s
						(errorThreshold
							& UnreachableCode)
							!= 0
							? 0
							: 1)
						 }
			}
		}
		""";

	String testName = "<complete on argument of anonymous type declaration>";
	String completeBehind = "t";
	String expectedCompletionNodeToString = "<CompleteOnName:t>";
	String completionIdentifier = "t";
	String expectedReplacedSource = "this";
	int cursorLocation = str.indexOf("this, ") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import p2.Y;
		public class X {
		  public static final float Vars;
		  public static final float Lines;
		  public static final float Source;
		  public static final float UnreachableCode;
		  public static final float produceDebugAttributes;
		  <clinit>() {
		  }
		  public X() {
		  }
		  void foo() {
		    int locale;
		    int errorThreshold;
		    int preserveAllLocalVariables;
		    new Y[]{new Y("debug.vars", <CompleteOnName:t>, locale, (((produceDebugAttributes & Vars) != 0) ? 0 : 1)),\
		 new Y("debug.lines", this, locale, (((produceDebugAttributes & Lines) != 0) ? 0 : 1)),\
		 new Y("debug.source", this, locale, (((produceDebugAttributes & Source) != 0) ? 0 : 1)),\
		 new Y("debug.preserveAllLocals", this, locale, (preserveAllLocalVariables ? 0 : 1)),\
		 new Y("optionalError.unReachableCode", this, locale, (((errorThreshold & UnreachableCode) != 0) ? 0 : 1))};
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testX_1FGGV8C_2() {
	String str =
		"""
		package p;\s
		import p2.YZA;\s
		/**
		 * 1FGGV8C and 1FGPE8E
		 */
		public class X {
			public static final float Vars;\s
			public static final float Lines;\s
			public static final float Source;\s
			public static final float UnreachableCode;\s
			public static final float produceDebugAttributes;\s
			void foo() {
				int locale,\s
					errorThreshold,\s
					preserveAllLocalVariables;\s
				return new YZA[] {
					new YZA(
						"debug.vars",\s
						this,\s
						locale,\s
						(produceDebugAttributes
							& Vars)
							!= 0
							? 0
							: 1),\s
					new YZA(
						"debug.lines",\s
						this,\s
						locale,\s
						(produceDebugAttributes
							& Lines)
							!= 0
							? 0
							: 1),\s
					new YZA(
						"debug.source",\s
						this,\s
						locale,\s
						(produceDebugAttributes
							& Source)
							!= 0
							? 0
							: 1),\s
					new YZA(
						"debug.preserveAllLocals",\s
						this,\s
						locale,\s
						preserveAllLocalVariables
							? 0
							: 1),\s
					new YZA(
						"optionalError.unReachableCode",\s
						this,\s
						locale,\s
						(errorThreshold
							& UnreachableCode)
							!= 0
							? 0
							: 1)
						 }
			}
		}
		""";

	String testName = "<complete on anonymous type declaration into a return statement>";
	String completeBehind = "Y";
	String expectedCompletionNodeToString = "<CompleteOnType:Y>";
	String completionIdentifier = "Y";
	String expectedReplacedSource = "YZA";
	int cursorLocation = str.indexOf("YZA[]") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import p2.YZA;
		public class X {
		  public static final float Vars;
		  public static final float Lines;
		  public static final float Source;
		  public static final float UnreachableCode;
		  public static final float produceDebugAttributes;
		  <clinit>() {
		  }
		  public X() {
		  }
		  void foo() {
		    int locale;
		    int errorThreshold;
		    int preserveAllLocalVariables;
		    return new <CompleteOnType:Y>();
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testX_1FGGV8C_3() {
	String str =
		"""
		package p;\s
		import p2.YZA;\s
		/**
		 * 1FGGV8C and 1FGPE8E
		 */
		public class X {
			public static final float Vars;\s
			public static final float Lines;\s
			public static final float Source;\s
			public static final float UnreachableCode;\s
			public static final float produceDebugAttributes;\s
			void foo() {
				int locale,\s
					errorThreshold,\s
					preserveAllLocalVariables;\s
				return new YZA[] {
					new YZA(
						"debug.vars",\s
						this,\s
						locale,\s
						(produceDebugAttributes
							& Vars)
							!= 0
							? 0
							: 1),\s
					new YZA(
						"debug.lines",\s
						this,\s
						locale,\s
						(produceDebugAttributes
							& Lines)
							!= 0
							? 0
							: 1),\s
					new YZA(
						"debug.source",\s
						this,\s
						locale,\s
						(produceDebugAttributes
							& Source)
							!= 0
							? 0
							: 1),\s
					new YZA(
						"debug.preserveAllLocals",\s
						this,\s
						locale,\s
						preserveAllLocalVariables
							? 0
							: 1),\s
					new YZA(
						"optionalError.unReachableCode",\s
						this,\s
						locale,\s
						(errorThreshold
							& UnreachableCode)
							!= 0
							? 0
							: 1)
						 }
			}
		}
		""";

	String testName = "<complete on anonymous type declaration nested into an array initializer>";
	String completeBehind = "Y";
	String expectedCompletionNodeToString = "<CompleteOnType:Y>";
	String completionIdentifier = "Y";
	String expectedReplacedSource = "YZA";
	int cursorLocation = str.indexOf("YZA(") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import p2.YZA;
		public class X {
		  public static final float Vars;
		  public static final float Lines;
		  public static final float Source;
		  public static final float UnreachableCode;
		  public static final float produceDebugAttributes;
		  <clinit>() {
		  }
		  public X() {
		  }
		  void foo() {
		    int locale;
		    int errorThreshold;
		    int preserveAllLocalVariables;
		    new YZA[]{new <CompleteOnType:Y>("debug.vars", this, locale, (((produceDebugAttributes & Vars) != 0) ? 0 : 1)), new YZA("debug.lines", this, locale, (((produceDebugAttributes & Lines) != 0) ? 0 : 1)), new YZA("debug.source", this, locale, (((produceDebugAttributes & Source) != 0) ? 0 : 1)), new YZA("debug.preserveAllLocals", this, locale, (preserveAllLocalVariables ? 0 : 1)), new YZA("optionalError.unReachableCode", this, locale, (((errorThreshold & UnreachableCode) != 0) ? 0 : 1))};
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testX_1FGGV8C_4() {
	String str =
		"""
		package p;\s
		import p2.Y;\s
		/**
		 * 1FGGV8C and 1FGPE8E
		 */
		public class X {
			public static final float Vars;\s
			public static final float Lines;\s
			public static final float Source;\s
			public static final float UnreachableCode;\s
			public static final float produceDebugAttributes;\s
			void foo() {
				int locale,\s
					errorThreshold,\s
					preserveAllLocalVariables;\s
				return new Y[] {
					new Y(
						"debug.vars",\s
						this,\s
						locale,\s
						(produceDebugAttributes
							& Vars)
							!= 0
							? 0
							: 1),\s
					new Y(
						"debug.lines",\s
						this,\s
						locale,\s
						(produceDebugAttributes
							& Lines)
							!= 0
							? 0
							: 1),\s
					new Y(
						"debug.source",\s
						this,\s
						locale,\s
						(produceDebugAttributes
							& Source)
							!= 0
							? 0
							: 1),\s
					new Y(
						"debug.preserveAllLocals",\s
						this,\s
						locale,\s
						preserveAllLocalVariables
							? 0
							: 1),\s
					new Y(
						"optionalError.unReachableCode",\s
						this,\s
						locale,\s
						(errorThreshold
							& UnreachableCode)
							!= 0
							? 0
							: 1)
						 }
			}
		}
		""";

	String testName = "<complete on method/field into array intializer>";
	String completeBehind = "n";
	String expectedCompletionNodeToString = "<CompleteOnName:n>";
	String completionIdentifier = "n";
	String expectedReplacedSource = "new";
	int cursorLocation = str.indexOf("new Y(") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import p2.Y;
		public class X {
		  public static final float Vars;
		  public static final float Lines;
		  public static final float Source;
		  public static final float UnreachableCode;
		  public static final float produceDebugAttributes;
		  <clinit>() {
		  }
		  public X() {
		  }
		  void foo() {
		    int locale;
		    int errorThreshold;
		    int preserveAllLocalVariables;
		    new Y[]{<CompleteOnName:n>};
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testX_1FGPE8E() {
	String str =
		"""
		package p;\s
		import p2.Y;\s
		/**
		 * 1FGGV8C and 1FGPE8E
		 */
		public class X {
			public static final float Vars;\s
			public static final float Lines;\s
			public static final float Source;\s
			public static final float UnreachableCode;\s
			public static final float produceDebugAttributes;\s
			void foo() {
				int locale,\s
					errorThreshold,\s
					preserveAllLocalVariables;\s
				return new Y[] {
					new Y(
						"debug.vars",\s
						this,\s
						locale,\s
						(produceDebugAttributes
							& Vars)
							!= 0
							? 0
							: 1),\s
					new Y(
						"debug.lines",\s
						this,\s
						locale,\s
						(produceDebugAttributes
							& Lines)
							!= 0
							? 0
							: 1),\s
					new Y(
						"debug.source",\s
						this,\s
						locale,\s
						(produceDebugAttributes
							& Source)
							!= 0
							? 0
							: 1),\s
					new Y(
						"debug.preserveAllLocals",\s
						this,\s
						locale,\s
						preserveAllLocalVariables
							? 0
							: 1),\s
					new Y(
						"optionalError.unReachableCode",\s
						this,\s
						locale,\s
						(errorThreshold
							& UnreachableCode)
							!= 0
							? 0
							: 1)
						 }
			}
		}
		""";

	String testName = "<complete on method/field into return statement>";
	String completeBehind = "n";
	String expectedCompletionNodeToString = "<CompleteOnName:n>";
	String completionIdentifier = "n";
	String expectedReplacedSource = "new";
	int cursorLocation = str.indexOf("new Y[]") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import p2.Y;
		public class X {
		  public static final float Vars;
		  public static final float Lines;
		  public static final float Source;
		  public static final float UnreachableCode;
		  public static final float produceDebugAttributes;
		  <clinit>() {
		  }
		  public X() {
		  }
		  void foo() {
		    int locale;
		    int errorThreshold;
		    int preserveAllLocalVariables;
		    return <CompleteOnName:n>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
// Disabled since javadoc completion has been implemented
public void _testXA_1FGGUQF_1FHSL8H_1() {
	String str =
			"""
		// int
		package p;\s
		/**
		 * 1FGGUQF and 1FHSL8H
		 */
		/**
		 * int
		 */
		/*
		 * int
		 */
		// int
		/**
		int.
		 * Internal API used to resolve a compilation unit minimally for code assist engine
		 */
		/**
		 * int
		 */
		public class XA {
			//  int
			/*  int */
			/** int */
			/**
			int.
			 * Internal API used to resolve a compilation unit minimally for code assist engine
			 */
			void /* int */
			foo() {
				//  int
				/*  int */
				/** int */
			}
			/**
			int.
			 * Internal API used to resolve a compilation unit minimally for code assist engine
			 */
			int field /* int */
			;
			/*
			    int
			*/
			static {
				// int
			}
		}
		//  int
		/*  int */
		/** int */
		""";

	String testName = "<complete on comment>";
	String completeBehind = "int.";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "";
	String expectedReplacedSource = NONE;
	int cursorLocation = str.indexOf("int.\n") + completeBehind.length() - 1;
	String expectedUnitDisplayString = null;

	try {
		checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
		assertTrue("failed to detect invalid cursor location", false);
	} catch(InvalidCursorLocation e){
		assertEquals("invalid cursor location: ", e.irritant, InvalidCursorLocation.NO_COMPLETION_INSIDE_COMMENT);
	}
}
public void testXA_1FGGUQF_1FHSL8H_2() {
	String str =
			"""
		// int
		package p;\s
		/**
		 * 1FGGUQF and 1FHSL8H
		 */
		/**
		 * int
		 */
		/*
		 * int
		 */
		// int
		/**
		int.
		 * Internal API used to resolve a compilation unit minimally for code assist engine
		 */
		/**
		 * int
		 */
		public class XA {
			//  int
			/*  int */
			/** int */
			/**
			int.
			 * Internal API used to resolve a compilation unit minimally for code assist engine
			 */
			void /* int */
			foo() {
				//  int
				/*  int */
				/** int */
			}
			/**
			int.
			 * Internal API used to resolve a compilation unit minimally for code assist engine
			 */
			int field /* int */
			;
			/*
			    int
			*/
			static {
				// int
			}
		}
		//  int
		/*  int */
		/** int */
		""";

	String testName = "<complete on comment>";
	String completeBehind = "i";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "";
	String expectedReplacedSource = NONE;
	int cursorLocation = str.indexOf("int\n") + completeBehind.length() - 1;
	String expectedUnitDisplayString = null;

	try {
		checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
		assertTrue("failed to detect invalid cursor location", false);
	} catch(InvalidCursorLocation e){
		assertEquals("invalid cursor location: ", e.irritant, InvalidCursorLocation.NO_COMPLETION_INSIDE_COMMENT);
	}
}
public void testXA_1FGGUQF_1FHSL8H_3() {
	String str =
			"""
		// int
		package p;\s
		/**
		 * 1FGGUQF and 1FHSL8H
		 */
		/**
		 * int
		 */
		/*
		 * int
		 */
		// int
		/**
		int.
		 * Internal API used to resolve a compilation unit minimally for code assist engine
		 */
		/**
		 * int
		 */
		public class XA {
			//  int
			/*  int */
			/** int */
			/**
			int.
			 * Internal API used to resolve a compilation unit minimally for code assist engine
			 */
			void /* int */ foo() {
				//  int
				/*  int */
				/** int */
			}
			/**
			int.
			 * Internal API used to resolve a compilation unit minimally for code assist engine
			 */
			int field /* int */
			;
			/*
			    int
			*/
			static {
				// int
			}
		}
		//  int
		/*  int */
		/** int */
		""";

	String testName = "<complete on comment>";
	String completeBehind = "i";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "";
	String expectedReplacedSource = NONE;
	int cursorLocation = str.indexOf("int */") + completeBehind.length() - 1;
	String expectedUnitDisplayString = null;

	try {
		checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
		assertTrue("failed to detect invalid cursor location", false);
	} catch(InvalidCursorLocation e){
		assertEquals("invalid cursor location: ", e.irritant, InvalidCursorLocation.NO_COMPLETION_INSIDE_COMMENT);
	}
}
public void testXA_1FGGUQF_1FHSL8H_4() {
	String str =
			"""
		// int
		package p;\s
		/**
		 * 1FGGUQF and 1FHSL8H
		 */
		/**
		 * int
		 */
		/*
		 * int
		 */
		// int
		/**
		int.
		 * Internal API used to resolve a compilation unit minimally for code assist engine
		 */
		/**
		 * int
		 */
		public class XA {
			//  int
			/*  int */
			/** int */
			/**
			int.
			 * Internal API used to resolve a compilation unit minimally for code assist engine
			 */
			void /* int */ foo() {
				//  int
				/*  int */
				/** int */
			}
			/**
			int.
			 * Internal API used to resolve a compilation unit minimally for code assist engine
			 */
			int field /* int */
			;
			/*
			    int
			*/
			static {
				// int
			}
		}
		//  int
		/*  int  */
		/** int   */
		""";

	String testName = "<complete on comment>";
	String completeBehind = "i";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "";
	String expectedReplacedSource = NONE;
	int cursorLocation = str.indexOf("int */ foo()") + completeBehind.length() - 1;
	String expectedUnitDisplayString = null;

	try {
		checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
		assertTrue("failed to detect invalid cursor location", false);
	} catch(InvalidCursorLocation e){
		assertEquals("invalid cursor location: ", e.irritant, InvalidCursorLocation.NO_COMPLETION_INSIDE_COMMENT);
	}
}
public void testXB_1FIYM5I_1() {
	String str =
			"""
		package p;\s
		/*
		 * 1FIYM5I
		 */
		public class XB
			extends java.io.File {
			void foo() {
				XB xb = new XB();
				this.separator.;
				this.bar().;
			}
			String bar() {
			}
		}
		""";

	String testName = "<complete on method/field of explicit this access>";
	String completeBehind = "this.s";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:this.s>";
	String completionIdentifier = "s";
	String expectedReplacedSource = "this.separator";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class XB extends java.io.File {
		  public XB() {
		  }
		  void foo() {
		    XB xb;
		    <CompleteOnMemberAccess:this.s>;
		  }
		  String bar() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testXB_1FIYM5I_2() {
	String str =
			"""
		package p;\s
		/*
		 * 1FIYM5I
		 */
		public class XB
			extends java.io.File {
			void foo() {
				XB xb = new XB();
				this.separator.;
				this.bar().;
			}
			String bar() {
			}
		}
		""";

	String testName = "<complete on method/field of explicitly accessed field>";
	String completeBehind = "this.separator.";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:this.separator.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class XB extends java.io.File {
		  public XB() {
		  }
		  void foo() {
		    XB xb;
		    <CompleteOnMemberAccess:this.separator.>;
		  }
		  String bar() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testXB_1FIYM5I_3() {
	String str =
			"""
		package p;\s
		/*
		 * 1FIYM5I
		 */
		public class XB
			extends java.io.File {
			void foo() {
				XB xb = new XB();
				this.separator.;
				this.bar().;
			}
			String bar() {
			}
		}
		""";

	String testName = "<complete on method/field of explicit this access>";
	String completeBehind = "this.b";
	String expectedCompletionNodeToString = "<CompleteOnMessageSendName:this.b()>";
	String completionIdentifier = "b";
	String expectedReplacedSource = "bar";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class XB extends java.io.File {
		  public XB() {
		  }
		  void foo() {
		    XB xb;
		    <CompleteOnMessageSendName:this.b()>;
		  }
		  String bar() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testXB_1FIYM5I_4() {
	String str =
			"""
		package p;\s
		/*
		 * 1FIYM5I
		 */
		public class XB
			extends java.io.File {
			void foo() {
				XB xb = new XB();
				this.separator.;
				this.bar().;
			}
			String bar() {
			}
		}
		""";

	String testName = "<complete on method/field of explicitly accessed method>";
	String completeBehind = "this.bar().";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:this.bar().>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class XB extends java.io.File {
		  public XB() {
		  }
		  void foo() {
		    XB xb;
		    <CompleteOnMemberAccess:this.bar().>;
		  }
		  String bar() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testY_1FGPESI() {
	String str =
		"""
		package p;\s
		import p2.;\s
		/**
		 * 1FGPESI
		 */
		public class Y {
		}
		""";

	String testName = "<complete on imports>";
	String completeBehind = "p2.";
	String expectedCompletionNodeToString = "<CompleteOnImport:p2.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "p2.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import <CompleteOnImport:p2.>;
		public class Y {
		  public Y() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testYA_1FGRIUH() {
	String str =
			"""
		package p;\s
		/**
		 * 1FGRIUH
		 */
		public class YA
			extends YASecondTopLevel {
			void eFoo() {
			}
			class YAMember {
				void eFoo() {
				}
				void eBar() {
					e }
			}
		}
		class YASecondTopLevel {
			public boolean equals(YA yaya) {
				return true;
			}
			public eFoo() {
			}
			public void eFooBar() {
			}
		}
		""";

	String testName = "<complete on method/field>";
	String completeBehind = "e";
	String expectedCompletionNodeToString = "<CompleteOnName:e>";
	String completionIdentifier = "e";
	String expectedReplacedSource = "e";
	int cursorLocation = str.indexOf("e }") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class YA extends YASecondTopLevel {
		  class YAMember {
		    YAMember() {
		    }
		    void eFoo() {
		    }
		    void eBar() {
		      <CompleteOnName:e>;
		    }
		  }
		  public YA() {
		  }
		  void eFoo() {
		  }
		}
		class YASecondTopLevel {
		  YASecondTopLevel() {
		  }
		  public boolean equals(YA yaya) {
		  }
		  public eFoo() {
		  }
		  public void eFooBar() {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testYB_1FJ4D46_1() {
	String str =
			"""
		package p;\s
		/**
		 * 1FJ4D46
		 */
		public class YB {
			void foo() {
				new String("asdf".getBytes()).}
		}
		""";

	String testName = "<complete on method/field of object creation>";
	String completeBehind = "new String(\"asdf\".getBytes()).";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:new String(\"asdf\".getBytes()).>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class YB {
		  public YB() {
		  }
		  void foo() {
		    <CompleteOnMemberAccess:new String("asdf".getBytes()).>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testZ_1FGPF3D_1() {
	String str =
		"""
		package p;\s
		/**
		 * 1FGPF3D
		 */
		public class Z imp Pro.Sev,\s
			Bla.Blo {
		}
		""";

	String testName = "<complete on implements keyword>";
	String completeBehind = "imp";
	String expectedCompletionNodeToString = "<CompleteOnKeyword:imp>";
	String completionIdentifier = "imp";
	String expectedReplacedSource = "imp";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class Z extends <CompleteOnKeyword:imp> {
		  {
		  }
		  public Z() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testZ_1FGPF3D_2() {
	String str =
		"""
		package p;\s
		/**
		 * 1FGPF3D
		 */
		public class Z implements Pro.Sev,\s
			Bla.Blo {
		}
		""";

	String testName = "<complete on implented interface>";
	String completeBehind = "P";
	String expectedCompletionNodeToString = "<CompleteOnInterface:P>";
	String completionIdentifier = "P";
	String expectedReplacedSource = "Pro";
	int cursorLocation = str.indexOf("Pro.Sev") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class Z implements <CompleteOnInterface:P>, Bla.Blo {
		  public Z() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testZA_1() {
	String str =
			"package p; \n" +
			"import java.util.Vector;\n";

	String testName = "<complete on import keyword>";
	String completeBehind = "i";
	String expectedCompletionNodeToString = "<CompleteOnKeyword:i>";
	String completionIdentifier = "i";
	String expectedReplacedSource = "import";
	int cursorLocation = str.indexOf("import") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import <CompleteOnKeyword:i>;\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testZA_2() {
	String str =
			"package p; \n" +
			"import java.util.Vector;\n";

	String testName = "<complete on imported package>";
	String completeBehind = "jav";
	String expectedCompletionNodeToString = "<CompleteOnImport:jav>";
	String completionIdentifier = "jav";
	String expectedReplacedSource = "java.util.Vector";
	int cursorLocation = str.indexOf("java.util.Vector") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import <CompleteOnImport:jav>;\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testZA_3() {
	String str =
			"package p; \n" +
			"import java.util.Vector;\n";

	String testName = "<complete on imported type>";
	String completeBehind = "java.util.V";
	String expectedCompletionNodeToString = "<CompleteOnImport:java.util.V>";
	String completionIdentifier = "V";
	String expectedReplacedSource = "java.util.Vector";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"package p;\n" +
		"import <CompleteOnImport:java.util.V>;\n";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testZB_1FJ4D46_1() {
	String str =
			"""
		package p;\s
		/**
		 * 1FJ4D46
		 */
		import java.util.zip.CRC32;
		import java.io.*;
		public class ZB {
			public static void main(
				java.lang.String[] args) {
				File file =\s
					new File("d:\\\\314");\s
				CRC32 crc = new CRC32();
				file.}
		}
		""";

	String testName = "<complete on method/field of local variable>";
	String completeBehind = "file.";
	String expectedCompletionNodeToString = "<CompleteOnName:file.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "file.";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import java.util.zip.CRC32;
		import java.io.*;
		public class ZB {
		  public ZB() {
		  }
		  public static void main(java.lang.String[] args) {
		    File file;
		    CRC32 crc;
		    <CompleteOnName:file.>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testZB_1FJ4D46_2() {
	String str =
			"""
		package p;\s
		/**
		 * 1FJ4D46
		 */
		import java.util.zip.CRC32;
		import java.io.*;
		public class ZB {
			public static void main(
				java.lang.String[] args) {
				File file =\s
					new File("d:\\\\314");\s
				CRC32 crc = new CRC32();
				file.}
		}
		""";

	String testName = "<complete on local variable type>";
	String completeBehind = "CRC";
	String expectedCompletionNodeToString = "<CompleteOnName:CRC>";
	String completionIdentifier = "CRC";
	String expectedReplacedSource = "CRC32";
	int cursorLocation = str.indexOf("CRC32 crc") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		import java.util.zip.CRC32;
		import java.io.*;
		public class ZB {
		  public ZB() {
		  }
		  public static void main(java.lang.String[] args) {
		    File file;
		    <CompleteOnName:CRC>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Complete in initializer
 */
public void test001(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int v1;
		    {
		      Obj
		    }
		  }
		}
		""";

	String testName = "<complete in initializer>";
	String completeBehind = "Obj";
	String expectedCompletionNodeToString = "<CompleteOnName:Obj>";
	String completionIdentifier = "Obj";
	String expectedReplacedSource = "Obj";
	int cursorLocation = str.indexOf("Obj") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    int v1;
		    {
		      <CompleteOnName:Obj>;
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Complete after initializer
 */
public void test002(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int v1;
		    {
		      int v2
		    }
		    Obj\
		  }
		}
		""";

	String testName = "<complete after initializer>";
	String completeBehind = "Obj";
	String expectedCompletionNodeToString = "<CompleteOnName:Obj>";
	String completionIdentifier = "Obj";
	String expectedReplacedSource = "Obj";
	int cursorLocation = str.indexOf("Obj") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    int v1;
		    <CompleteOnName:Obj>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Complete in initializer
 */
public void test003(){
	String str =
		"""
		package p;
		public class X {
		  void foo(){
		    int v1;
		    {
		      this.
		    }
		  }
		}
		""";

	String testName = "<complete in initializer>";
	String completeBehind = "this.";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:this.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "this.";
	int cursorLocation = str.indexOf("this.") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		package p;
		public class X {
		  public X() {
		  }
		  void foo() {
		    int v1;
		    {
		      <CompleteOnMemberAccess:this.>;
		    }
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

/**
 * Complete in switch
 */
public void test004(){
	String str =
		"""
		public class X {
		  final static int ZZZ = 1;
		  void foo(){
		    switch(2)
		      case 0 + ZZZ :
		      case 1 + ZZZ :
		          bar(ZZZ)
		  }
		  void bar(int y) {}
		}
		""";

	String testName = "<complete in switch>";
	String completeBehind = "ZZZ";
	String expectedCompletionNodeToString = "<CompleteOnName:ZZZ>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	int cursorLocation = str.lastIndexOf("ZZZ") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		public class X {
		  static final int ZZZ;
		  <clinit>() {
		  }
		  public X() {
		  }
		  void foo() {
		    bar(<CompleteOnName:ZZZ>);
		  }
		  void bar(int y) {
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Complete in method type.
 */
public void test005(){
	String str =
		"""
		public class X {
		  clon foo(){
		  }
		}
		""";

	String testName = "<complete in method type>";
	String completeBehind = "clon";
	String expectedCompletionNodeToString = "<CompleteOnType:clon>";
	String completionIdentifier = "clon";
	String expectedReplacedSource = "clon";
	int cursorLocation = str.lastIndexOf("clon") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <CompleteOnType:clon>
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Complete in method type.
 */
public void test006(){
	String str =
		"""
		public class X {
		  clon
		  foo();
		}
		""";

	String testName = "<complete in method type>";
	String completeBehind = "clon";
	String expectedCompletionNodeToString = "<CompleteOnType:clon>;";
	String completionIdentifier = "clon";
	String expectedReplacedSource = "clon";
	int cursorLocation = str.lastIndexOf("clon") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:clon>;
		  public X() {
		  }
		  foo();
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Complete in field type.
 */
public void test007(){
	String str =
		"""
		public class X {
		  clon  x;
		}
		""";

	String testName = "<complete in field type>";
	String completeBehind = "clon";
	String expectedCompletionNodeToString = "<CompleteOnType:clon>;";
	String completionIdentifier = "clon";
	String expectedReplacedSource = "clon";
	int cursorLocation = str.lastIndexOf("clon") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:clon>;
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Complete in field type.
 */
public void test008(){
	String str =
		"""
		public class X {
		  clon
		  x;
		}
		""";

	String testName = "<complete in field type>";
	String completeBehind = "clon";
	String expectedCompletionNodeToString = "<CompleteOnType:clon>;";
	String completionIdentifier = "clon";
	String expectedReplacedSource = "clon";
	int cursorLocation = str.lastIndexOf("clon") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:clon>;
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Complete in field type.
 */
public void test009(){
	String str =
		"""
		public class X {
		  clon
		  x y;
		}
		""";

	String testName = "<complete in field tpye>";
	String completeBehind = "clon";
	String expectedCompletionNodeToString = "<CompleteOnType:clon>;";
	String completionIdentifier = "clon";
	String expectedReplacedSource = "clon";
	int cursorLocation = str.lastIndexOf("clon") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:clon>;
		  x y;
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * Complete in method type.
 */
public void test010(){
	String str =
		"""
		public class X {
		  clon
		  x y(){}
		}
		""";

	String testName = "<complete in method type>";
	String completeBehind = "clon";
	String expectedCompletionNodeToString = "<CompleteOnType:clon>;";
	String completionIdentifier = "clon";
	String expectedReplacedSource = "clon";
	int cursorLocation = str.lastIndexOf("clon") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:clon>;
		  public X() {
		  }
		  x y() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=25233
 */
public void test011(){
	String str =
		"""
		public class X {
		  void foo() {
		    new Object[]{
		      bar(zzz)
		    };
		  }
		}
		""";

	String testName = "<bug 25233>";
	String completeBehind = "zzz";
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	int cursorLocation = str.lastIndexOf("zzz") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    new Object[]{bar(<CompleteOnName:zzz>)};
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=27370
 */
public void test012(){
	String str =
		"""
		public class X {
		  public X() {
		    super();
		  }
		  Object o = new ZZZ
		}
		""";

	String testName = "<bug 27370>";
	String completeBehind = "ZZZ";
	String expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	int cursorLocation = str.lastIndexOf("ZZZ") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		public class X {
		  Object o = new <CompleteOnType:ZZZ>();
		  public X() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=27735
 */
public void test013(){
	String str =
		"""
		public class Bar {
		  #
		  Bar foo1 = new Bar(){};
		  {int i;}
		  synchronized void foo3() {}
		  zzz
		}
		""";

	String testName = "<bug 27735>";
	String completeBehind = "zzz";
	String expectedCompletionNodeToString = "<CompleteOnType:zzz>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	int cursorLocation = str.lastIndexOf("zzz") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		public class Bar {
		  Bar foo1;
		  {
		  }
		  <CompleteOnType:zzz>;
		  public Bar() {
		  }
		  synchronized void foo3() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=27941
 */
public void test014(){
	String str =
		"""
		public class Bar {
		  void foo() {
		    String s = "a" + "b";
		    zzz
		  }
		}
		""";

	String testName = "<bug 27941>";
	String completeBehind = "zzz";
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	int cursorLocation = str.lastIndexOf("zzz") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		public class Bar {
		  public Bar() {
		  }
		  void foo() {
		    String s;
		    <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=39502
 */
public void test015(){
	String str =
		"""
		public class Bar {
		  void foo() {
		    Object o = new Object[]{};
		    foo();
		  }
		}
		""";

	String testName = "<bug 39502>";
	String completeBehind = "foo(";
	String expectedCompletionNodeToString = "<CompleteOnMessageSend:foo()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "foo(";
	int cursorLocation = str.lastIndexOf("foo(") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		public class Bar {
		  public Bar() {
		  }
		  void foo() {
		    Object o;
		    <CompleteOnMessageSend:foo()>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/**
 * http://dev.eclipse.org/bugs/show_bug.cgi?id=39502
 */
public void test016(){
	String str =
		"""
		public class Bar {
		  void foo() {
		    Object o = new Object[0];
		    foo();
		  }
		}
		""";

	String testName = "<bug 39502>";
	String completeBehind = "foo(";
	String expectedCompletionNodeToString = "<CompleteOnMessageSend:foo()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "foo(";
	int cursorLocation = str.lastIndexOf("foo(") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		public class Bar {
		  public Bar() {
		  }
		  void foo() {
		    Object o;
		    <CompleteOnMessageSend:foo()>;
		  }
		}
		""";

	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

public void test017(){
	String str =
		"""
		public class Bar {
		  String s;
		  /**/
		}
		""";

	String testName = "";
	String completeBehind = "/**/";
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.lastIndexOf("/**/") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		public class Bar {
		  String s;
		  <CompleteOnType:>;
		  public Bar() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=310423
// To verify that assist node parent is set to the the type declaration
// when completion is requested after implements in a type declaration.
public void testBug310423(){
	String str =
		"""
		import java.lang.annotation.Annotation;
		interface In {}
		interface Inn {
			interface Inn2 {}
			@interface InAnnot {}
		}
		@interface InnAnnot {}
		public class Test implements In{
		}
		""";

	String testName = "";
	String completeBehind = "In";
	String expectedCompletionNodeToString = "<CompleteOnInterface:In>";
	String expectedParentNodeToString =
		"""
		public class Test implements <CompleteOnInterface:In> {
		  public Test() {
		  }
		}""";
	String completionIdentifier = "In";
	String expectedReplacedSource = "In";
	int cursorLocation = str.lastIndexOf("In") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
		"""
		import java.lang.annotation.Annotation;
		interface In {
		}
		interface Inn {
		  interface Inn2 {
		  }
		  @interface InAnnot {
		  }
		}
		@interface InnAnnot {
		}
		public class Test implements <CompleteOnInterface:In> {
		  public Test() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338789
public void testBug338789(){
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	String str =
		"""
		public class Test {
			public void throwing() throws IZZBException, IZZException {}
			public void foo() {
		      try {
		         throwing();
		      }
		      catch (IZZException | IZZ) {
		         bar();
		      }
		   }\
		}
		class IZZAException extends Exception {
		}
		class IZZBException extends Exception {
		}
		class IZZException extends Exception {
		}
		""";

	String testName = "<complete on multi-catch block exception type declaration>";
	String completeBehind = "IZZException | IZZ";
	String expectedCompletionNodeToString = "<CompleteOnException:IZZ>";
	String completionIdentifier = "IZZ";
	String expectedReplacedSource = "IZZ";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  public void throwing() throws IZZBException, IZZException {
		  }
		  public void foo() {
		    try
		      {
		        throwing();
		      }
		    catch (IZZException | <CompleteOnException:IZZ>  )
		      {
		      }
		  }
		}
		class IZZAException extends Exception {
		  IZZAException() {
		  }
		}
		class IZZBException extends Exception {
		  IZZBException() {
		  }
		}
		class IZZException extends Exception {
		  IZZException() {
		  }
		}
		""";

	int cursorLocation = str.indexOf("IZZException | IZZ") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=338789
// Qualified assist type reference
public void testBug338789b(){
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	String str =
		"""
		public class Test {
			public void throwing() throws java.lang.IllegalArgumentException, java.lang.IndexOutOfBoundsException {}
			public void foo() {
		      try {
		         throwing();
		      }
		      catch (java.lang.IllegalArgumentException | java.lang.I) {
		         bar();
		      }
		   }\
		}
		""";

	String testName = "<complete on multi-catch block exception type declaration qualified>";
	String completeBehind = "java.lang.IllegalArgumentException | java.lang.I";
	String expectedCompletionNodeToString = "<CompleteOnException:java.lang.I>";
	String completionIdentifier = "I";
	String expectedReplacedSource = "java.lang.I";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  public void throwing() throws java.lang.IllegalArgumentException, java.lang.IndexOutOfBoundsException {
		  }
		  public void foo() {
		    try
		      {
		        throwing();
		      }
		    catch (java.lang.IllegalArgumentException | <CompleteOnException:java.lang.I>  )
		      {
		      }
		  }
		}
		""";

	int cursorLocation = str.indexOf("java.lang.IllegalArgumentException | java.lang.I") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=343637
// Check that the whole union type ref is part of the completion node parent
public void testBug343637(){
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	String str =
		"""
		public class Test {
			public void throwing() throws java.lang.IllegalArgumentException, java.lang.IndexOutOfBoundsException, java.lang.IOException {}
			public void foo() {
		      try {
		         throwing();
		      }
			   catch (java.lang.IOException e){}
		      catch (java.lang.IllegalArgumentException | java.lang.I) {
		         bar();
		      }
		   }\
		}
		""";

	String testName = "<complete on multi-catch block exception type declaration qualified>";
	String completeBehind = "java.lang.IllegalArgumentException | java.lang.I";
	String expectedCompletionNodeToString = "<CompleteOnException:java.lang.I>";
	String completionIdentifier = "I";
	String expectedReplacedSource = "java.lang.I";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  public void throwing() throws java.lang.IllegalArgumentException, java.lang.IndexOutOfBoundsException, java.lang.IOException {
		  }
		  public void foo() {
		    try
		      {
		        throwing();
		      }
		    catch (java.lang.IOException e)
		      {
		      }
		    catch (java.lang.IllegalArgumentException | <CompleteOnException:java.lang.I>  )
		      {
		      }
		  }
		}
		""";

	int cursorLocation = str.indexOf("java.lang.IllegalArgumentException | java.lang.I") + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346454
public void testBug346454(){
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	String str =
		"""
		public class Test<T> {
			public void foo() {
		      Test<String> t = new Test<>()
		   }\
		}
		""";

	String testName = "<complete after diamond type>";
	String completeBehind = "new Test<>(";
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new Test<>()>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
		"""
		public class Test<T> {
		  public Test() {
		  }
		  public void foo() {
		    Test<String> t = <CompleteOnAllocationExpression:new Test<>()>;
		  }
		}
		""";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346454
public void testBug346454b(){
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	String str =
		"""
		public class Test<T> {
			public class T2<Z>{}
			public void foo() {
		      Test<String>.T2<String> t = new Test<>().new T2<>()
		   }\
		}
		""";

	String testName = "<complete after diamond type>";
	String completeBehind = "new Test<>().new T2<>(";
	String expectedCompletionNodeToString = "<CompleteOnQualifiedAllocationExpression:new Test<>().new T2<>(<CompleteOnName:>)>";
	String completionIdentifier = "";
	String expectedReplacedSource = "new Test<>().new T2<>()";
	String expectedUnitDisplayString =
		"""
		public class Test<T> {
		  public class T2<Z> {
		    public T2() {
		    }
		  }
		  public Test() {
		  }
		  public void foo() {
		    Test<String>.T2<String> t = <CompleteOnQualifiedAllocationExpression:new Test<>().new T2<>(<CompleteOnName:>)>;
		  }
		}
		""";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void testBug346454b2(){
	// TODO: unsuccessful attempt to show that completion would work right after "<>" but for that
	// CompletionParser.checkClassInstanceCreation would need to handle parameterized types, which it never did
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	String str =
		"""
		public class Test<T> {
			public class T2<Z>{}
			public void foo() {
		      Test<String>.T2<String> t = new Test<String>().new T2<>()
		   }\
		}
		""";

	String testName = "<complete after diamond type>";
	String completeBehind = "new Test<String>().new T2<>";
	String expectedCompletionNodeToString = "<NONE>"; // TODO: improve
	String completionIdentifier = "";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test<T> {
		  public class T2<Z> {
		    public T2() {
		    }
		  }
		  public Test() {
		  }
		  public void foo() {
		  }
		}
		""";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=346415
// To make sure that all catch blocks before the one in which we're invoking assist are avaiable in the ast.
public void testBug346415(){
	if (this.complianceLevel < ClassFileConstants.JDK1_7)
		return;
	String str =
		"""
		public class Test {
			public void throwing() throws java.lang.IllegalArgumentException, java.lang.IndexOutOfBoundsException, java.lang.IOException {}
			public void foo() {
		      try {
		         throwing();
		      }
			   catch (java.lang.IOException e){
		      } catch (java.lang.IllegalArgumentException e){
			   } catch (/*propose*/) {
		      }
		   }
		}
		""";

	String testName = "<complete on third catch block>";
	String completeBehind = "catch (/*propose*/";
	String expectedCompletionNodeToString = "<CompleteOnException:>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"""
		public class Test {
		  public Test() {
		  }
		  public void throwing() throws java.lang.IllegalArgumentException, java.lang.IndexOutOfBoundsException, java.lang.IOException {
		  }
		  public void foo() {
		    try
		      {
		        throwing();
		      }
		    catch (java.lang.IOException e)
		      {
		      }
		    catch (java.lang.IllegalArgumentException e)
		      {
		      }
		    catch (<CompleteOnException:>  )
		      {
		      }
		  }
		}
		""";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	checkMethodParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292087
// To verify that the completion node is found inside a field initializer
// that contains an anonymous class.
public void testBug292087a(){
	String str =
			"""
		package test;
		class MyClass{
		}
		public class Try extends Thread{
			public static MyClass MyClassField;\
			public static MyClass MyClassMethod(){
				return null;
			}
			public MyClass member[] = {
				\
				new MyClass (){
					public void abc() {}
				},
				/*Complete here*/
			};
		}
		""";

	String testName = "";
	String completeBehind = "/*Complete here*/";
	String expectedCompletionNodeToString = "<CompleteOnName:>";
	String expectedParentNodeToString =
		"public MyClass[] member = {<CompleteOnName:>};";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.lastIndexOf("/*Complete here*/") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
			"""
		package test;
		class MyClass {
		  MyClass() {
		  }
		}
		public class Try extends Thread {
		  public static MyClass MyClassField;
		  public MyClass[] member = {<CompleteOnName:>};
		  public Try() {
		  }
		  <clinit>() {
		  }
		  public static MyClass MyClassMethod() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=292087
// To verify that anonymous class inside an array initializer of a recovered field
// doesn't end up at a bogus location.
public void testBug292087b(){
	String str =
			"""
		package test;
		class MyClass{
		}
		public class Try extends Thread{
			public static MyClass MyClassField;\
			public static MyClass MyClassMethod(){
				return null;
			}
			public MyClass member[] = {
				/*Complete here*/
				new MyClass (){
					public void abc() {}
				},
				\
			};
		}
		""";

	String testName = "";
	String completeBehind = "/*Complete here*/";
	String expectedCompletionNodeToString = "<CompleteOnName:>";
	String expectedParentNodeToString =
		"public MyClass[] member = {<CompleteOnName:>};";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	int cursorLocation = str.lastIndexOf("/*Complete here*/") + completeBehind.length() - 1;
	String expectedUnitDisplayString =
			"""
		package test;
		class MyClass {
		  MyClass() {
		  }
		}
		public class Try extends Thread {
		  public static MyClass MyClassField;
		  public MyClass[] member = {<CompleteOnName:>};
		  public Try() {
		  }
		  <clinit>() {
		  }
		  public static MyClass MyClassMethod() {
		  }
		}
		""";

	checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedParentNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
}
