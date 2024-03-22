/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

public class DietCompletionTest extends AbstractCompletionTest {
public DietCompletionTest(String testName){
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(DietCompletionTest.class);
}
/*
 * Complete on superclass
 */
public void test01() {

	String str =
		"""
		import java.io.*;						\t
												\t
		public class X extends IOException {	\t
		}										\t
		""";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnClass:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends <CompleteOnClass:IOEx> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "IOException";
	String testName = "<complete on superclass>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on superinterface
 */
public void test02() {

	String str =
		"""
		import java.io.*;												\t
																		\t
		public class X extends IOException implements Serializable {	\t
		 int foo(){}\s
		}																\t
		""";

	String completeBehind = "Seria";
	String expectedCompletionNodeToString = "<CompleteOnInterface:Seria>";
	String completionIdentifier = "Seria";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends IOException implements <CompleteOnInterface:Seria> {
		  public X() {
		  }
		  int foo() {
		  }
		}
		""";
	String expectedReplacedSource = "Serializable";
	String testName = "<complete on superinterface>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on qualified superclass
 */
public void test03() {

	String str =
		"""
		import java.io.*;												\t
																		\t
		public class X extends java.io.IOException  {					\t
		}																\t
		""";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnClass:java.io.IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends <CompleteOnClass:java.io.IOEx> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "java.io.IOException";
	String testName = "<complete on qualified superclass>";


	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);

}
/*
 * Complete on qualified superinterface
 */
public void test04() {

	String str =
		"""
		import java.io.*;														\t
																				\t
		public class X extends IOException implements java.io.Serializable {	\t
		}																		\t
		""";

	String completeBehind = "Seria";
	String expectedCompletionNodeToString = "<CompleteOnInterface:java.io.Seria>";
	String completionIdentifier = "Seria";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends IOException implements <CompleteOnInterface:java.io.Seria> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "java.io.Serializable";
	String testName = "<complete on qualified superinterface>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on incomplete superclass
 */
public void test05() {

	String str =
		"""
		import java.io.*;						\t
												\t
		public class X extends IOEx {			\t
		}										\t
		""";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnClass:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends <CompleteOnClass:IOEx> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete on incomplete superclass>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on incomplete superinterface
 */
public void test06() {

	String str =
		"""
		import java.io.*;												\t
																		\t
		public class X extends IOException implements Seria {			\t
		}																\t
		""";

	String completeBehind = "Seria";
	String expectedCompletionNodeToString = "<CompleteOnInterface:Seria>";
	String completionIdentifier = "Seria";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends IOException implements <CompleteOnInterface:Seria> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "Seria";
	String testName = "<complete on incomplete superinterface>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on incomplete qualified superclass
 */
public void test07() {

	String str =
		"""
		import java.io.*;												\t
																		\t
		public class X extends java.io.IOEx  		{					\t
		}																\t
		""";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnClass:java.io.IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString = """
		import java.io.*;
		public class X extends <CompleteOnClass:java.io.IOEx> {
		  public X() {
		  }
		}
		""";
		String expectedReplacedSource = "java.io.IOEx";
	String testName = "<complete on incomplete qualified superclass>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on incomplete qualified superinterface
 */
public void test08() {

	String str =
		"""
		import java.io.*;														\t
																				\t
		public class X extends IOException implements java.io.Seria {			\t
		}																		\t
		""";

	String completeBehind = "Seria";
	String expectedCompletionNodeToString = "<CompleteOnInterface:java.io.Seria>";
	String completionIdentifier = "Seria";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends IOException implements <CompleteOnInterface:java.io.Seria> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "java.io.Seria";
	String testName = "<complete on incomplete qualified superinterface>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete inside qualified superclass
 */
public void test09() {

	String str =
		"""
																		\t
		public class X extends java.io.IOException  		{			\t
		}																\t
		""";

	String completeBehind = ".io";
	String expectedCompletionNodeToString = "<CompleteOnClass:java.io>";
	String completionIdentifier = "io";
	String expectedUnitDisplayString =
		"""
		public class X extends <CompleteOnClass:java.io> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "java.io.IOException";
	String testName = "<complete inside qualified superclass>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete inside qualified superinterface
 */
public void test10() {

	String str =
		"public class X extends IOException implements java.io.Serializable {		\n" +
		"}																			\n";

	String completeBehind = ".io";
	String expectedCompletionNodeToString = "<CompleteOnInterface:java.io>";
	String completionIdentifier = "io";
	String expectedUnitDisplayString =
		"""
		public class X extends IOException implements <CompleteOnInterface:java.io> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "java.io.Serializable";
	String testName = "<complete inside qualified superinterface>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete inside qualified superclass ending with dot
 */
public void test11() {

	String str =
		"""
																		\t
		public class X extends java.io.	{							\t
		}																\t
		""";

	String completeBehind = ".io.";
	String expectedCompletionNodeToString = "<CompleteOnClass:java.io.>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"""
		public class X extends <CompleteOnClass:java.io.> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "java.io.";
	String testName = "<complete inside qualified superclass ending with dot>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete inside qualified superinterface ending with dot
 */
public void test12() {

	String str =
		"public class X extends IOException implements java.io.				 {		\n" +
		"}																			\n";

	String completeBehind = ".io.";
	String expectedCompletionNodeToString = "<CompleteOnInterface:java.io.>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"""
		public class X extends IOException implements <CompleteOnInterface:java.io.> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "java.io.";
	String testName = "<complete inside qualified superinterface ending with dot>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on empty superclass
 */
public void test13() {

	String str =
		"""
		import java.io.*;						\t
												\t
		public class X extends  {				\t
		}										\t
		""";

	String completeBehind = "extends ";
	String expectedCompletionNodeToString = "<CompleteOnClass:>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends <CompleteOnClass:> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "";
	String testName = "<complete on empty superclass>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on empty superinterface
 */
public void test14() {

	String str =
		"""
		import java.io.*;												\t
																		\t
		public class X extends IOException implements  {				\t
		}																\t
		""";

	String completeBehind = "implements ";
	String expectedCompletionNodeToString = "<CompleteOnInterface:>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends IOException implements <CompleteOnInterface:> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "";
	String testName = "<complete on empty superinterface>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on empty superclass followed by identifier
 */
public void test15() {

	String str =
		"public class X extends java.io. IOException  {			\n" +
		"}														\n";

	String completeBehind = "java.io.";
	String expectedCompletionNodeToString = "<CompleteOnClass:java.io.>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"""
		public class X extends <CompleteOnClass:java.io.> {
		  {
		  }
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "java.io.";
	String testName = "<complete on empty superclass followed by identifier>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on keyword extends
 */
public void test16() {

	String str =
		"""
		import java.io.*;						\t
												\t
		public class X extends  {				\t
		}										\t
		""";

	String completeBehind = "extends";
	String expectedCompletionNodeToString = "<CompleteOnKeyword:extends>";
	String completionIdentifier = "extends";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends <CompleteOnKeyword:extends> {
		  {
		  }
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "extends";
	String testName = "<complete on keyword extends>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in keyword extends
 */
public void test17() {

	String str =
		"""
		import java.io.*;						\t
												\t
		public class X ext  {					\t
		}										\t
		""";

	String completeBehind = "ext";
	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String completionIdentifier = "ext";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends <CompleteOnKeyword:ext> {
		  {
		  }
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "ext";
	String testName = "<complete in keyword extends>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in field type
 */
public void test18() {

	String str =
		"""
		class X {								\t
												\t
			IOException x;						\t
		}										\t
		""";

	String completeBehind = "IO";
	String expectedCompletionNodeToString = "<CompleteOnType:IO>;";
	String completionIdentifier = "IO";
	String expectedUnitDisplayString =
		"""
		class X {
		  <CompleteOnType:IO>;
		  X() {
		  }
		}
		""";
	String expectedReplacedSource = "IOException";
	String testName = "<complete in field type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete at beginning of field type
 */
public void test19() {

	String str =
		"""
		class X {								\t
												\t
			final IOException x;				\t
		}										\t
		""";

	String completeBehind = "final ";
	String expectedCompletionNodeToString = "<CompleteOnType:>;";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"""
		class X {
		  <CompleteOnType:>;
		  X() {
		  }
		}
		""";
	String expectedReplacedSource = "IOException";
	String testName = "<complete at beginning of field type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete at beginning of superclass
 */
public void test20() {

	String str =
		"""
		class X extends IOException {			\t
												\t
		}										\t
		""";

	String completeBehind = "extends ";
	String expectedCompletionNodeToString = "<CompleteOnClass:>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"""
		class X extends <CompleteOnClass:> {
		  X() {
		  }
		}
		""";
	String expectedReplacedSource = "IOException";
	String testName = "<complete at beginning of superclass>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in return type
 */
public void test21() {

	String str =
		"""
		class X {								\t
			IOEx								\t
		}										\t
		""";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnType:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"""
		class X {
		  <CompleteOnType:IOEx>;
		  X() {
		  }
		}
		""";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete in return type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in argument type
 */
public void test22() {

	String str =
		"""
		class X {								\t
			int foo(IOEx						\t
		}										\t
		""";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnType:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"""
		class X {
		  <CompleteOnType:IOEx>;
		  X() {
		  }
		  int foo() {
		  }
		}
		""";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete in argument type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in return type
 */
public void test23() {

	String str =
		"""
		class X {								\t
			IOEx								\t
												\t
		""";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnType:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"""
		class X {
		  <CompleteOnType:IOEx>;
		  X() {
		  }
		}
		""";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete in return type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in argument type (no closing brace for type)
 */
public void test24() {

	String str =
		"""
		class X {								\t
			int foo(IOEx						\t
												\t
		""";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnType:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"""
		class X {
		  <CompleteOnType:IOEx>;
		  X() {
		  }
		  int foo() {
		  }
		}
		""";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete in argument type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in return type with modifiers
 */
public void test25() {

	String str =
		"""
		class X {								\t
			public final IOEx					\t
												\t
		""";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnType:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"""
		class X {
		  <CompleteOnType:IOEx>;
		  X() {
		  }
		}
		""";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete in return type with modifiers>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in field initialization
 */
public void test26() {

	String str =
		"""
		class X {								\t
			public final int x = IOEx			\t
												\t
		""";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnName:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"""
		class X {
		  public final int x = <CompleteOnName:IOEx>;
		  X() {
		  }
		}
		""";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete in field initialization>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in nth argument type
 */
public void test27() {

	String str =
		"""
		class X {								\t
			int foo(AA a, BB b, IOEx			\t
												\t
		""";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnType:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"""
		class X {
		  <CompleteOnType:IOEx>;
		  X() {
		  }
		  int foo(AA a, BB b) {
		  }
		}
		""";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete in nth argument type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in nth argument qualified type
 */
public void test28() {

	String str =
		"""
		class X {								\t
			int foo(AA a, BB b, java.io.IOEx	\t
												\t
		""";

	String completeBehind = ".io.";
	String expectedCompletionNodeToString = "<CompleteOnType:java.io.>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"""
		class X {
		  <CompleteOnType:java.io.>;
		  X() {
		  }
		  int foo(AA a, BB b) {
		  }
		}
		""";
	String expectedReplacedSource = "java.io.IOEx";
	String testName = "<complete in nth argument qualified type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in nth thrown exception qualified type
 */
public void test29() {

	String str =
		"""
		class X {											\t
			public int foo(AA a, BB b) throws AA, java.io.IOEx\t
															\t
		""";

	String completeBehind = ".io";
	String expectedCompletionNodeToString = "<CompleteOnException:java.io>";
	String completionIdentifier = "io";
	String expectedUnitDisplayString =
		"""
		class X {
		  X() {
		  }
		  public int foo(AA a, BB b) throws AA, <CompleteOnException:java.io> {
		  }
		}
		""";
	String expectedReplacedSource = "java.io.IOEx";
	String testName = "<complete in nth thrown exception qualified type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in completed argument
 */
public void test30() {

	String str =
		"""
		class X {											\t
			public int foo(AA a, java.io.BB b) 				\t
															\t
		""";

	String completeBehind = "io.";
	String expectedCompletionNodeToString = "<CompleteOnType:java.io.>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"""
		class X {
		  X() {
		  }
		  public int foo(AA a, <CompleteOnType:java.io.> b) {
		  }
		}
		""";
	String expectedReplacedSource = "java.io.BB";
	String testName = "<complete in in completed argument>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Negative test: no diet completion in allocation expression
 */
public void test31() {

	String str =
		"""
		class Bar {							\t
			void foo() {						\t
				new X().zzz();					\t
			}									\t
		}
		""";

	String completeBehind = "new X";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = NONE;
	String expectedUnitDisplayString =
		"""
		class Bar {
		  Bar() {
		  }
		  void foo() {
		  }
		}
		""";
	String expectedReplacedSource = null;
	String testName = "<no diet completion in allocation expression>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Completion in package statement
 */
public void test32() {

	String str =
		"""
		package x.abc			\t
		import x.util.*;			\t
		import x.*;				\t
		class X extends util{\t
		    X(){}			\t
		    X(int a, int b){}\t
		}							\t
		""";

	String completeBehind = "x.ab";
	String expectedCompletionNodeToString = "<CompleteOnPackage:x.ab>";
	String completionIdentifier = "ab";
	String expectedUnitDisplayString =
		"""
		package <CompleteOnPackage:x.ab>;
		import x.util.*;
		import x.*;
		class X extends util {
		  X() {
		  }
		  X(int a, int b) {
		  }
		}
		""";
	String expectedReplacedSource = "x.abc";
	String testName = "<complete in package statement>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Completion in import statement
 */
public void test33() {

	String str =
		"""
		package x.abc;			\t
		import x.util			\t
		import x.*;				\t
		class X extends util{\t
		    X(){}			\t
		    X(int a, int b){}\t
		}							\t
		""";

	String completeBehind = "x.util";
	String expectedCompletionNodeToString = "<CompleteOnImport:x.util>";
	String completionIdentifier = "util";
	String expectedUnitDisplayString =
		"""
		package x.abc;
		import <CompleteOnImport:x.util>;
		import x.*;
		class X extends util {
		  X() {
		  }
		  X(int a, int b) {
		  }
		}
		""";
	String expectedReplacedSource = "x.util";
	String testName = "<complete in import statement>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on superclass behind a unicode
 *
 * -- compute the unicode representation for a given string --
   [ String str = "IOEx";
	StringBuffer buffer = new StringBuffer("\"");
	for (int i = 0; i < str.length(); i++){
		String hex = Integer.toHexString(str.charAt(i));
		buffer.append("\\u0000".substring(0, 6-hex.length()));
		buffer.append(hex);
	}
	buffer.append("\"");
	buffer.toString()
	]
 */
public void test34() {

	String str =
		"""
		import java.io.*;								\t
														\t
		public class X extends IOE\\u0078ception {		\t
		}												\t
		""";

	String completeBehind = "IOE\\u0078";
	String expectedCompletionNodeToString = "<CompleteOnClass:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends <CompleteOnClass:IOEx> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "IOE\\u0078ception";
	String testName = "<complete on superclass behind a unicode>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test34a() {

	String str =
		"""
		import java.io.*;								\t
														\t
		public class X extends IOException {		\t
		}												\t
		""";

	String completeBehind = "IOE";
	String expectedCompletionNodeToString = "<CompleteOnClass:IOE>";
	String completionIdentifier = "IOE";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends <CompleteOnClass:IOE> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "IOException";
	String testName = "<complete on superclass before a unicode>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test34b() {

	String str =
		"""
		import java.io.*;								\t
														\t
		public class X extends IOE\\u0078c\\u0065ption {		\t
		}												\t
		""";

	String completeBehind = "IOE\\u0078c\\u0065p";
	String expectedCompletionNodeToString = "<CompleteOnClass:IOExcep>";
	String completionIdentifier = "IOExcep";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends <CompleteOnClass:IOExcep> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "IOE\\u0078c\\u0065ption";
	String testName = "<complete on superclass behind a unicode>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
public void test34c() {

	String str =
		"""
		import java.io.*;								\t
														\t
		public class X \\u0065xt\\u0065nds IOE\\u0078c\\u0065ption {		\t
		}												\t
		""";

	String completeBehind = "IOE\\u0078c\\u0065p";
	String expectedCompletionNodeToString = "<CompleteOnClass:IOExcep>";
	String completionIdentifier = "IOExcep";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X extends <CompleteOnClass:IOExcep> {
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "IOE\\u0078c\\u0065ption";
	String testName = "<complete on superclass behind a unicode>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Invalid completion inside a unicode
 *
 * -- compute the unicode representation for a given string --
   [ String str = "IOEx";
	StringBuffer buffer = new StringBuffer("\"");
	for (int i = 0; i < str.length(); i++){
		String hex = Integer.toHexString(str.charAt(i));
		buffer.append("\\u0000".substring(0, 6-hex.length()));
		buffer.append(hex);
	}
	buffer.append("\"");
	buffer.toString()
	]
 */
public void test35() {

	String str =
		"""
		import java.io.*;								\t
														\t
		public class X extends IOE\\u0078ception {		\t
		}												\t
		""";

	String completeBehind = "IOE\\u00";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = NONE;
	String expectedUnitDisplayString = "";
	String expectedReplacedSource = NONE;
	String testName = "<complete inside unicode>";

	try {
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
		this.checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			testName);
		assertTrue("failed to detect invalid cursor location", false);
	} catch(InvalidCursorLocation e){
		assertEquals("invalid cursor location: ", e.irritant, InvalidCursorLocation.NO_COMPLETION_INSIDE_UNICODE);
	}
}
/*
 * Invalid completion inside a comment
 */
public void test36() {

	String str =
		"""
		import java.io.*;								\t
														\t
		public class X extends /*IOException*/ {		\t
		}												\t
		""";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = NONE;
	String expectedUnitDisplayString = "";
	String expectedReplacedSource = NONE;
	String testName = "<complete inside comment>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	try {
		this.checkDietParse(
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
/*
 * Invalid completion inside a string literal
 */
public void test37() {

	String str =
		"""
		import java.io.*;								\t
														\t
		public class X {								\t
			String s = "IOException";					\t
		}												\t
		""";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompletionOnString:\"IOEx\">";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X {
		  String s = <CompletionOnString:"IOEx">;
		  public X() {
		  }
		}
		""";
	String expectedReplacedSource = "\"IOException\"";
	String testName = "<complete inside a string literal>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Invalid completion inside a number literal
 */
public void test38() {

	String str =
		"""
		import java.io.*;								\t
														\t
		public class X {								\t
			int s = 12345678;							\t
		}												\t
		""";

	String completeBehind = "1234";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = NONE;
	String expectedUnitDisplayString = "";
	String expectedReplacedSource = NONE;
	String testName = "<complete inside a number literal>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	try{
		this.checkDietParse(
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
/*
 * Completion in import statement at the end of the unit
 */
public void test39() {

	String str =
		"package x.abc;				\n"+
		"import x.util";

	String completeBehind = "x.util";
	String expectedCompletionNodeToString = "<CompleteOnImport:x.util>";
	String completionIdentifier = "util";
	String expectedUnitDisplayString =
		"package x.abc;\n" +
		"import <CompleteOnImport:x.util>;\n";
	String expectedReplacedSource = "x.util";
	String testName = "<complete in import statement at the end of the unit>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Completion in import statement at the end of the unit (qualified empty name)
 */
public void test40() {

	String str =
		"package a.b;			\n"+
		"import java.";

	String completeBehind = "java.";
	String expectedCompletionNodeToString = "<CompleteOnImport:java.>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"package a.b;\n" +
		"import <CompleteOnImport:java.>;\n";
	String expectedReplacedSource = "java.";
	String testName = "<complete in import statement at the end of the unit>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Should not find any diet completion
 */
public void test41() {

	String str =
		"""
		import java.io.*;						\t
												\t
		public class X extends 				\t
			int foo(String str)					\t
				String variable = ;				\t
				{								\t
				 	String variableNotInScope;	\t
				}								\t
				foo(varia						\t
		""";

	String completeBehind = "foo(var";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "var";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X {
		  public X() {
		  }
		  int foo(String str) {
		  }
		}
		""";
	String expectedReplacedSource = "varia";
	String testName = "<should not find diet completion>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on array type with prefix dimensions
 */
public void test42() {

	String str =
		"""
		import java.io.*;						\t
												\t
		public class X {		 				\t
			int[] foo(String str)				\t
		""";

	String completeBehind = "int";
	String expectedCompletionNodeToString = "<CompleteOnType:int>";
	String completionIdentifier = "int";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X {
		  public X() {
		  }
		  <CompleteOnType:int>
		}
		""";
	String expectedReplacedSource = "int";
	String testName = "<completion on array type with prefix dimensions>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on array type with postfix dimensions
 */
public void test43() {

	String str =
		"""
		import java.io.*;						\t
												\t
		public class X {		 				\t
			int foo(String str)	[]				\t
		""";

	String completeBehind = "int";
	String expectedCompletionNodeToString = "<CompleteOnType:int>";
	String completionIdentifier = "int";
	String expectedUnitDisplayString =
		"""
		import java.io.*;
		public class X {
		  public X() {
		  }
		  <CompleteOnType:int>
		}
		""";
	String expectedReplacedSource = "int";
	String testName = "<completion on array type with postfix dimensions>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in return type behind other member
 */
public void test44() {

	String str =
		"""
		class X {								\t
			int i;								\t
			IOEx								\t
		}										\t
		""";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnType:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"""
		class X {
		  int i;
		  <CompleteOnType:IOEx>;
		  X() {
		  }
		}
		""";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete in return type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in return type behind other member
 */
public void test45() {

	String str =
		"""
		class X {								\t
			int i;								\t
			public IOEx							\t
		}										\t
		""";

	String completeBehind = "IOEx";
	String expectedCompletionNodeToString = "<CompleteOnType:IOEx>";
	String completionIdentifier = "IOEx";
	String expectedUnitDisplayString =
		"""
		class X {
		  int i;
		  <CompleteOnType:IOEx>;
		  X() {
		  }
		}
		""";
	String expectedReplacedSource = "IOEx";
	String testName = "<complete in return type>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on name in field initializer
 */
public void test46() {

	String str =
		"""
		class X {								\t
			String s = "hello";				\t
			int f = s.							\t
		}										\t
		""";

	String completeBehind = "= s";
	String expectedCompletionNodeToString = "<CompleteOnName:s>";
	String completionIdentifier = "s";
	String expectedUnitDisplayString =
		"""
		class X {
		  String s;
		  int f = <CompleteOnName:s>;
		  X() {
		  }
		}
		""";
	String expectedReplacedSource = "s";
	String testName = "<complete on name in field initializer>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete in field initializer in innner class
 */
public void test47() {

	String str =
		"""
		class X {								\t
			class Y {							\t
				Object[] f = { this.foo }		\t
				Object foo(){ return this; }	\t
		}										\t
		""";

	String completeBehind = "this.foo";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:this.foo>";
	String completionIdentifier = "foo";
	String expectedUnitDisplayString =
		"""
		class X {
		  class Y {
		    Object[] f = {<CompleteOnMemberAccess:this.foo>};
		    Y() {
		    }
		    Object foo() {
		    }
		  }
		  X() {
		  }
		}
		""";
	String expectedReplacedSource = "this.foo";
	String testName = "<complete in field initializer in inner class>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Should not find fake field of type <CompleteOnType:f>
 */
public void test48() {

	String str =
		"""
		package pack;							\t
		class A  {								\t
												\t
			public static void main(String[] argv)\t
					new Member().f				\t
					;							\t
			}									\t
			class Member {						\t
				int foo()						\t
				}								\t
			}									\t
		};										\t
		""";

	String completeBehind = "new Member().f";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "f";
	String expectedUnitDisplayString =
		"""
		package pack;
		class A {
		  class Member {
		    Member() {
		    }
		    int foo() {
		    }
		  }
		  A() {
		  }
		  public static void main(String[] argv) {
		  }
		}
		""";

	String expectedReplacedSource = "f";
	String testName = "<should not find fake field of type f>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Completion in middle of package import statement
 */
public void test49() {

	String str =
		"""
		import java.lang.reflect.*;\t
		class X {					\t
		}							\t
		""";

	String completeBehind = "java.la";
	String expectedCompletionNodeToString = "<CompleteOnImport:java.la>";
	String completionIdentifier = "la";
	String expectedUnitDisplayString =
		"""
		import <CompleteOnImport:java.la>;
		class X {
		  X() {
		  }
		}
		""";
	String expectedReplacedSource = "java.lang.reflect";
	String testName = "<complete in middle of package import statement>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on instance creation in field initializer.
 */
public void test50() {

	String str =
		"""
		class X {								\t
			String s = "hello";				\t
			Object o = new Xyz();				\t
		}										\t
		""";

	String completeBehind = "new X";
	String expectedCompletionNodeToString = "<CompleteOnType:X>";
	String completionIdentifier = "X";
	String expectedUnitDisplayString =
		"""
		class X {
		  String s;
		  Object o = new <CompleteOnType:X>();
		  X() {
		  }
		}
		""";
	String expectedReplacedSource = "Xyz";
	String testName = "<complete on instance creation in field initializer>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on member access in field initializer.
 */
public void test51() {

	String str =
		"""
		class X {								\t
			String s = "hello";				\t
			Object o = fred().xyz;				\t
		}										\t
		""";

	String completeBehind = "fred().x";
	String expectedCompletionNodeToString = "<CompleteOnMemberAccess:fred().x>";
	String completionIdentifier = "x";
	String expectedUnitDisplayString =
		"""
		class X {
		  String s;
		  Object o = <CompleteOnMemberAccess:fred().x>;
		  X() {
		  }
		}
		""";
	String expectedReplacedSource = "xyz";
	String testName = "<complete on member access in field initializer>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on class literal access in field initializer.
 */
public void test52() {

	String str =
		"""
		class X {								\t
			String s = "hello";				\t
			Class c = int[].class;				\t
		}										\t
		""";

	String completeBehind = "int[].c";
	String expectedCompletionNodeToString = "<CompleteOnClassLiteralAccess:int[].c>";
	String completionIdentifier = "c";
	String expectedUnitDisplayString =
		"""
		class X {
		  String s;
		  Class c = <CompleteOnClassLiteralAccess:int[].c>;
		  X() {
		  }
		}
		""";
	String expectedReplacedSource = "int[].class";
	String testName = "<complete on class literal access in field initializer>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on method invocation in field initializer.
 */
public void test53() {

	String str =
		"""
		class X {								\t
			String s = "hello";				\t
			Object o = s.concat();				\t
		}										\t
		""";

	String completeBehind = "s.concat(";
	String expectedCompletionNodeToString = "<CompleteOnMessageSend:s.concat()>";
	String completionIdentifier = "";
	String expectedUnitDisplayString =
		"""
		class X {
		  String s;
		  Object o = <CompleteOnMessageSend:s.concat()>;
		  X() {
		  }
		}
		""";
	String expectedReplacedSource = "concat(";
	String testName = "<complete on method invocation in field initializer>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Should not find fake field of type <CompleteOnType:f>
 */
public void test54() {

	String str =
		"""
		package pack;							\t
		class A  {								\t
												\t
			public static void main(String[] argv\t
					new Member().f				\t
					;							\t
			}									\t
			class Member {						\t
				int foo()						\t
				}								\t
			}									\t
		};										\t
		""";

	String completeBehind = "new Member().f";
	String expectedCompletionNodeToString = NONE;
	String completionIdentifier = "f";
	String expectedUnitDisplayString =
		"""
		package pack;
		class A {
		  class Member {
		    Member() {
		    }
		    int foo() {
		    }
		  }
		  A() {
		  }
		  public static void main(String[] argv) {
		  }
		}
		""";

	String expectedReplacedSource = "f";
	String testName = "<should not find fake field of type f>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * Complete on anonymous type in field initializer.
 */
public void test55() {

	String str =
		"""
		class X {								\t
			Object o = new Object(){			\t
				void foo(){						\t
					String x = "";			\t
					x.index						\t
				}								\t
												\t
				void bar(){						\t
					String y = "";			\t
				}								\t
			};					 				\t
		}										\t
		""";

	String completeBehind = "x.index";
	String expectedCompletionNodeToString = "<CompleteOnName:x.index>";
	String completionIdentifier = "index";
	String expectedUnitDisplayString =
		"""
		class X {
		  Object o = new Object() {
		    void foo() {
		      String x;
		      <CompleteOnName:x.index>;
		    }
		    void bar() {
		    }
		  };
		  X() {
		  }
		}
		""";
	String expectedReplacedSource = "x.index";
	String testName = "<complete on anonymous type in field initializer>";

	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		str.toCharArray(),
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		completionIdentifier,
		expectedReplacedSource,
		testName);
}
}
