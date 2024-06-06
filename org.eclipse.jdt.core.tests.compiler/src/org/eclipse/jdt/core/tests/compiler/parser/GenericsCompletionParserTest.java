/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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

import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class GenericsCompletionParserTest extends AbstractCompletionTest {
public GenericsCompletionParserTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(GenericsCompletionParserTest.class);
}
@Override
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
	return options;
}
public void test0001(){
	String str =
		"public class X  <T extends Z<Y>. {\n" +
		"}";


	String completeBehind = "Z<Y>.";
	int cursorLocation = str.indexOf("Z<Y>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z<Y>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Z<Y>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Z<Y>.";
	String expectedUnitDisplayString =
		"""
		public class X<T extends <CompleteOnType:Z<Y>.>> {
		  public X() {
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
	"diet ast");
}
public void test0002(){
	String str =
		"public class X  <T extends Z<Y>.W {\n" +
		"}";


	String completeBehind = "Z<Y>.W";
	int cursorLocation = str.indexOf("Z<Y>.W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z<Y>.W>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Z<Y>.W>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "Z<Y>.W";
	String expectedUnitDisplayString =
		"""
		public class X<T extends <CompleteOnType:Z<Y>.W>> {
		  public X() {
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
	"diet ast");
}
public void test0003(){
	String str =
		"public class Test<T extends test0001.X<Y>.Z> {\n" +
		"}";


	String completeBehind = "X<Y>.Z";
	int cursorLocation = str.indexOf("X<Y>.Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:test0001.X<Y>.Z>";
	String expectedParentNodeToString = "T extends <CompleteOnType:test0001.X<Y>.Z>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "test0001.X<Y>.Z";
	String expectedUnitDisplayString =
		"""
		public class Test<T extends <CompleteOnType:test0001.X<Y>.Z>> {
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
	"diet ast");
}
public void test0004(){
	String str =
		"""
		public class X {
		  public Y<Z>.
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:Y<Z>.>;
		  public X() {
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
	"diet ast");
}
public void test0005(){
	String str =
		"""
		public class X {
		  public Y<Z>. foo
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:Y<Z>.>;
		  public X() {
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
	"diet ast");
}
public void test0006(){
	String str =
		"""
		public class X {
		  public Y<Z>. foo;
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:Y<Z>.>;
		  public X() {
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
	"diet ast");
}
public void test0007(){
	String str =
		"""
		public class X {
		  public Y<Z>. foo()
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public <CompleteOnType:Y<Z>.> foo() {
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
	"diet ast");
}
public void test0008(){
	String str =
		"""
		public class X {
		  public Y<Z>. foo(){}
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public <CompleteOnType:Y<Z>.> foo() {
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
	"diet ast");
}
public void test0009(){
	String str =
		"""
		public class X {
		  public Y<Z>.V<W>.
		}""";


	String completeBehind = "Y<Z>.V<W>.";
	int cursorLocation = str.indexOf("Y<Z>.V<W>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.V<W>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.V<W>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:Y<Z>.V<W>.>;
		  public X() {
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
	"diet ast");
}
public void test0010(){
	String str =
		"""
		public class X {
		  public Y<Z>.V<W>. foo
		}""";


	String completeBehind = "Y<Z>.V<W>.";
	int cursorLocation = str.indexOf("Y<Z>.V<W>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.V<W>.>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.V<W>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:Y<Z>.V<W>.>;
		  public X() {
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
	"diet ast");
}
public void test0011(){
	String str =
		"""
		public class X {
		  public Y<Z>.V<W>. foo;
		}""";


	String completeBehind = "Y<Z>.V<W>.";
	int cursorLocation = str.indexOf("Y<Z>.V<W>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.V<W>.>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.V<W>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:Y<Z>.V<W>.>;
		  public X() {
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
	"diet ast");
}
public void test0012(){
	String str =
		"""
		public class X {
		  public Y<Z>.V<W>. foo()
		}""";


	String completeBehind = "Y<Z>.V<W>.";
	int cursorLocation = str.indexOf("Y<Z>.V<W>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.V<W>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.V<W>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public <CompleteOnType:Y<Z>.V<W>.> foo() {
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
	"diet ast");
}
public void test0013(){
	String str =
		"""
		public class X {
		  public Y<Z>.V<W>. foo(){}
		}""";


	String completeBehind = "Y<Z>.V<W>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.V<W>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.V<W>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public <CompleteOnType:Y<Z>.V<W>.> foo() {
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
	"diet ast");
}
public void test0014(){
	String str =
		"""
		public class X extends  Y<Z>. {
		 \s
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnClass:Y<Z>.>";
	String expectedParentNodeToString =
		"""
		public class X extends <CompleteOnClass:Y<Z>.> {
		  public X() {
		  }
		}""";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X extends <CompleteOnClass:Y<Z>.> {
		  public X() {
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
	"diet ast");
}
public void test0015(){
	String str =
		"""
		public class X implements I1, Y<Z>. {
		 \s
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnInterface:Y<Z>.>";
	String expectedParentNodeToString =
		"""
		public class X implements I1, <CompleteOnInterface:Y<Z>.> {
		  public X() {
		  }
		}""";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X implements I1, <CompleteOnInterface:Y<Z>.> {
		  public X() {
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
	"diet ast");
}
public void test0016(){
	String str =
		"""
		public class X {
		  void foo(Y<Z>.){
		 \s
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:Y<Z>.>;
		  {
		  }
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0017(){
	String str =
		"""
		public class X {
		  void foo(Y<Z>. bar){
		 \s
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo(<CompleteOnType:Y<Z>.> bar) {
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
	"diet ast");
}
public void test0018(){
	String str =
		"""
		public class X {
		  Y<Z>. foo(){
		 \s
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <CompleteOnType:Y<Z>.> foo() {
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
	"diet ast");
}
public void test0019(){
	String str =
		"""
		public class X  {
		  void foo() throws Y<Z>. {
		 \s
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() throws <CompleteOnException:Y<Z>.> {
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
	"diet ast");
}
public void test0020(){
	String str =
		"""
		public class X {
		  <T extends Y<Z>.> void foo(){
		 \s
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <T extends <CompleteOnType:Y<Z>.>>void foo() {
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
	"diet ast");
}
public void test0021(){
	String str =
		"""
		public class X {
		  <T extends Y<Z>.> void foo(
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <T extends <CompleteOnType:Y<Z>.>>void foo() {
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
	"diet ast");
}
public void test0022(){
	String str =
		"""
		public class X {
		  <T extends Y<Z>.> int foo
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <T extends <CompleteOnType:Y<Z>.>>
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
	"diet ast");
}
public void test0023(){
	String str =
		"""
		public class X {
		  <T extends Y<Z>.> X
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <T extends <CompleteOnType:Y<Z>.>>
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
	"diet ast");
}
public void test0024(){
	String str =
		"""
		public class X {
		  <T extends Y<Z>.>
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <T extends <CompleteOnType:Y<Z>.>>
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
	"diet ast");
}
public void test0025(){
	String str =
		"""
		public class X {
		  <T extends Y<Z>. void foo(){
		 \s
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <T extends <CompleteOnType:Y<Z>.>>
		  void foo() {
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
	"diet ast");
}
public void test0026(){
	String str =
		"""
		public class X {
		  <T extends Y<Z>. void foo(
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <T extends <CompleteOnType:Y<Z>.>>
		  void foo() {
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
	"diet ast");
}
public void test0027(){
	String str =
		"""
		public class X {
		  <T extends Y<Z>. int foo
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  int foo;
		  public X() {
		  }
		  <T extends <CompleteOnType:Y<Z>.>>
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
	"diet ast");
}
public void test0028(){
	String str =
		"""
		public class X {
		  <T extends Y<Z>. X
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <T extends <CompleteOnType:Y<Z>.>>
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
	"diet ast");
}
public void test0029(){
	String str =
		"""
		public class X {
		  <T extends Y<Z>.
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Y<Z>.>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <T extends <CompleteOnType:Y<Z>.>>
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
	"diet ast");
}
public void test0030_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Y<Z>.
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0030_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Y<Z>.
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnType:Y<Z>.>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0031_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Y<Z>. var
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0031_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Y<Z>. var
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnType:Y<Z>.>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0032_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Y<Z>.W
		  }
		}""";


	String completeBehind = "Y<Z>.W";
	int cursorLocation = str.indexOf("Y<Z>.W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0032_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Y<Z>.W
		  }
		}""";


	String completeBehind = "Y<Z>.W";
	int cursorLocation = str.indexOf("Y<Z>.W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.W>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "Y<Z>.W";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnType:Y<Z>.W>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0033_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Y<Z>.W var
		  }
		}""";


	String completeBehind = "Y<Z>.W";
	int cursorLocation = str.indexOf("Y<Z>.W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0033_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Y<Z>.W var
		  }
		}""";


	String completeBehind = "Y<Z>.W";
	int cursorLocation = str.indexOf("Y<Z>.W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.W>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "Y<Z>.W";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnType:Y<Z>.W>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0034_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z>.>bar();
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0034_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z>.>bar();
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "this.<Y, <CompleteOnType:Y<Z>.>>bar()";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    this.<Y, <CompleteOnType:Y<Z>.>>bar();
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0035_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z>.>bar(
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0035_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z>.>bar(
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnType:Y<Z>.>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0036_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z>.>bar
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0036_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z>.>bar
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnType:Y<Z>.>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0037_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z>.>
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0037_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z>.>
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnType:Y<Z>.>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0038_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z>.
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0038_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z>.
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnType:Y<Z>.>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0039_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    new <Y, Y<Z>.>X();
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0039_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    new <Y, Y<Z>.>X();
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnType:Y<Z>.>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0040_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    aaa.new <Y, Y<Z>.>X();
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0040_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    aaa.new <Y, Y<Z>.>X();
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "aaa.new <Y, <CompleteOnType:Y<Z>.>>X()";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    aaa.new <Y, <CompleteOnType:Y<Z>.>>X();
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0041_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    new V().new <Y, Y<Z>.>X();
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0041_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    new V().new <Y, Y<Z>.>X();
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "new V().new <Y, <CompleteOnType:Y<Z>.>>X()";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    new V().new <Y, <CompleteOnType:Y<Z>.>>X();
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0042_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    for(Y<Z>. var;;){}
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0042_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    for(Y<Z>. var;;){}
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnType:Y<Z>.>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0043_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    for(Y<Z>.
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0043_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    for(Y<Z>.
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnType:Y<Z>.>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0044_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    try {
		    } catch(Y<Z>. e) {
		   }
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0044_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    try {
		    } catch(Y<Z>. e) {
		   }
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:Y<Z>.>";
	String expectedParentNodeToString =
			"""
		try
		  {
		  }
		catch (<CompleteOnException:Y<Z>.>  )
		  {
		  }""";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    try
		      {
		      }
		    catch (<CompleteOnException:Y<Z>.>  )
		      {
		      }
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0045_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    try {
		    } catch(Y<Z>. e
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0045_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    try {
		    } catch(Y<Z>. e
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:Y<Z>.>";
	String expectedParentNodeToString =
			"""
		try
		  {
		  }
		catch (<CompleteOnException:Y<Z>.>  )
		  {
		  }""";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    try
		      {
		      }
		    catch (<CompleteOnException:Y<Z>.>  )
		      {
		      }
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0046_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    catch(Y<Z>. e
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0046_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    catch(Y<Z>. e
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnException:Y<Z>.>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0047_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object a = (Y<Z>.) e;
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0047_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object a = (Y<Z>.) e;
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "Object a = <CompleteOnType:Y<Z>.>;";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object a = <CompleteOnType:Y<Z>.>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0048_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object a = (Y<Z>.) e;
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0048_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object a = (Y<Z>.) e;
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "Object a = <CompleteOnType:Y<Z>.>;";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object a = <CompleteOnType:Y<Z>.>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0049_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    (Y<Z>.) e;
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0049_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    (Y<Z>.) e;
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnType:Y<Z>.>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0050_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object[] o = new Y<Z>.[0];
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0050_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object[] o = new Y<Z>.[0];
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0051_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object[] o = new Y<Z>.
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0051_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object[] o = new Y<Z>.
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0052_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    new Y<Z>.
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0052_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    new Y<Z>.
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0053_Diet(){
	String str =
		"""
		public class X {
		  public X() {
		    <Y<Z>.>super();
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
	"diet ast");
}
public void test0053_Method(){
	String str =
		"""
		public class X {
		  public X() {
		    <Y<Z>.>super();
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		    super();
		    <CompleteOnType:Y<Z>.>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0054_Diet(){
	String str =
		"""
		public class X {
		  public X() {
		    aaa.<Y<Z>.>super();
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
	"diet ast");
}
public void test0054_Method(){
	String str =
		"""
		public class X {
		  public X() {
		    aaa.<Y<Z>.>super();
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		    super();
		    <CompleteOnType:Y<Z>.>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0055_Diet(){
	String str =
		"""
		public class X {
		  public X() {
		    A.this.<Y<Z>.>super();
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
	"diet ast");
}
public void test0055_Method(){
	String str =
		"""
		public class X {
		  public X() {
		    A.this.<Y<Z>.>super();
		  }
		}""";


	String completeBehind = "Y<Z>.";
	int cursorLocation = str.indexOf("Y<Z>.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y<Z>.>";
	String expectedParentNodeToString = "A.this.<<CompleteOnType:Y<Z>.>>super();";
	String completionIdentifier = "";
	String expectedReplacedSource = "Y<Z>.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		    A.this.<<CompleteOnType:Y<Z>.>>super();
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}

public void test0056_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Y<Z
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0056_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Y<Z
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0057_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Y<V,Z
		  }
		}""";


	String completeBehind = "Y<V,Z";
	int cursorLocation = str.indexOf("Y<V,Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0057_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Y<V,Z
		  }
		}""";


	String completeBehind = "Y<V,Z";
	int cursorLocation = str.indexOf("Y<V,Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<V, <CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<V, <CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0058_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    W<U>.Y<V,Z
		  }
		}""";


	String completeBehind = "Y<V,Z";
	int cursorLocation = str.indexOf("Y<V,Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0058_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    W<U>.Y<V,Z
		  }
		}""";


	String completeBehind = "Y<V,Z";
	int cursorLocation = str.indexOf("Y<V,Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "W<U>.Y<V, <CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    W<U>.Y<V, <CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0059(){
	String str =
		"public class X  <T extends Z<Y {\n" +
		"}";


	String completeBehind = "Z<Y";
	int cursorLocation = str.indexOf("Z<Y") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Y>";
	String expectedParentNodeToString = "Z<<CompleteOnType:Y>>";
	String completionIdentifier = "Y";
	String expectedReplacedSource = "Y";
	String expectedUnitDisplayString =
		"""
		public class X<T> {
		  Z<<CompleteOnType:Y>>;
		  public X() {
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
	"diet ast");
}
public void test0060(){
	String str =
		"""
		public class X {
		  public Y<Z
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<<CompleteOnType:Z>>;
		  public X() {
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
	"diet ast");
}
public void test0061(){
	String str =
		"""
		public class X {
		  public Y<Z>
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<<CompleteOnType:Z>>;
		  public X() {
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
	"diet ast");
}
public void test0062(){
	String str =
		"""
		public class X {
		  public Y<Z> var
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public Y<<CompleteOnType:Z>> var;
		  public X() {
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
	"diet ast");
}
public void test0063(){
	String str =
		"""
		public class X {
		  public Y<Z> var;
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public Y<<CompleteOnType:Z>> var;
		  public X() {
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
	"diet ast");
}
public void test0064(){
	String str =
		"""
		public class X {
		  public Y<Z foo()
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<<CompleteOnType:Z>>;
		  public X() {
		  }
		  foo() {
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
	"diet ast");
}
public void test0065(){
	String str =
		"""
		public class X {
		  public Y<Z> foo()
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public Y<<CompleteOnType:Z>> foo() {
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
	"diet ast");
}
public void test0066(){
	String str =
		"""
		public class X {
		  public Y<Z foo(){}
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<<CompleteOnType:Z>>;
		  public X() {
		  }
		  foo() {
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
	"diet ast");
}
public void test0067(){
	String str =
		"""
		public class X {
		  public Y<Z> foo(){}
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public Y<<CompleteOnType:Z>> foo() {
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
	"diet ast");
}
public void test0068(){
	String str =
		"""
		public class X {
		  public Y<Z>.V<W
		}""";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z>.V<W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<Z>.V<<CompleteOnType:W>>;
		  public X() {
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
	"diet ast");
}
public void test0069(){
	String str =
		"""
		public class X {
		  public Y<Z>.V<W>
		}""";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z>.V<W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<Z>.V<<CompleteOnType:W>>;
		  public X() {
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
	"diet ast");
}
public void test0070(){
	String str =
		"""
		public class X {
		  public Y<Z>.V<W> var
		}""";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z>.V<W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public Y<Z>.V<<CompleteOnType:W>> var;
		  public X() {
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
	"diet ast");
}
public void test0071(){
	String str =
		"""
		public class X {
		  public Y<Z>.V<W> var;
		}""";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z>.V<W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public Y<Z>.V<<CompleteOnType:W>> var;
		  public X() {
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
	"diet ast");
}
public void test0072(){
	String str =
		"""
		public class X {
		  public Y<Z>.V<W foo()
		}""";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z>.V<W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<Z>.V<<CompleteOnType:W>>;
		  public X() {
		  }
		  foo() {
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
	"diet ast");
}
public void test0073(){
	String str =
		"""
		public class X {
		  public Y<Z>.V<W> foo()
		}""";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z>.V<W") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public Y<Z>.V<<CompleteOnType:W>> foo() {
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
	"diet ast");
}
public void test0074(){
	String str =
		"""
		public class X {
		  public Y<Z>.V<W foo(){}
		}""";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<Z>.V<<CompleteOnType:W>>;
		  public X() {
		  }
		  foo() {
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
	"diet ast");
}
public void test0075(){
	String str =
		"""
		public class X {
		  public Y<Z>.V<W> foo(){}
		}""";


	String completeBehind = "Y<Z>.V<W";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:W>";
	String expectedParentNodeToString = "Y<Z>.V<<CompleteOnType:W>>";
	String completionIdentifier = "W";
	String expectedReplacedSource = "W";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public Y<Z>.V<<CompleteOnType:W>> foo() {
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
	"diet ast");
}
public void test0076(){
	String str =
		"""
		public class X extends  Y<Z {
		 \s
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<<CompleteOnType:Z>>;
		  public X() {
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
	"diet ast");
}
public void test0077(){
	String str =
		"""
		public class X extends  Y<Z> {
		 \s
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X extends Y<<CompleteOnType:Z>> {
		  public X() {
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
	"diet ast");
}
public void test0078(){
	String str =
		"""
		public class X implements I1, Y<Z {
		 \s
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X implements I1 {
		  Y<<CompleteOnType:Z>>;
		  public X() {
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
	"diet ast");
}
public void test0079(){
	String str =
		"""
		public class X implements I1, Y<Z> {
		 \s
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X implements I1, Y<<CompleteOnType:Z>> {
		  public X() {
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
	"diet ast");
}
public void test0080(){
	String str =
		"""
		public class X {
		  void foo(Y<Z){
		 \s
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
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
	"diet ast");
}
public void test0081(){
	String str =
		"""
		public class X {
		  void foo(Y<Z>){
		 \s
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
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
	"diet ast");
}
public void test0082(){
	String str =
		"""
		public class X {
		  void foo(Y<Z> var){
		 \s
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo(Y<<CompleteOnType:Z>> var) {
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
	"diet ast");
}
public void test0083(){
	String str =
		"""
		public class X {
		  Y<Z foo(){
		 \s
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<<CompleteOnType:Z>>;
		  public X() {
		  }
		  foo() {
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
	"diet ast");
}
public void test0084(){
	String str =
		"""
		public class X {
		  Y<Z> foo(){
		 \s
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  Y<<CompleteOnType:Z>> foo() {
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
	"diet ast");
}
public void test0085(){
	String str =
		"""
		public class X  {
		  void foo() throws Y<Z {
		 \s
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
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
	"diet ast");
}
public void test0086(){
	String str =
		"""
		public class X  {
		  void foo() throws Y<Z> {
		 \s
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() throws Y<<CompleteOnType:Z>> {
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
	"diet ast");
}
public void test0087(){
	String str =
		"""
		public class X {
		  <T extends Y<Z void foo(){
		 \s
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<<CompleteOnType:Z>>;
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0088(){
	String str =
		"""
		public class X {
		  <T extends Y<Z> void foo(){
		 \s
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <T extends Y<<CompleteOnType:Z>>>
		  void foo() {
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
	"diet ast");
}
public void test0089(){
	String str =
		"""
		public class X {
		  <T extends Y<Z>> void foo(){
		 \s
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <T extends Y<<CompleteOnType:Z>>>void foo() {
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
	"diet ast");
}
public void test0090(){
	String str =
		"""
		public class X {
		  <T extends Y<Z int foo
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<<CompleteOnType:Z>>;
		  int foo;
		  public X() {
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
	"diet ast");
}
public void test0091(){
	String str =
		"""
		public class X {
		  <T extends Y<Z> int foo
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  int foo;
		  public X() {
		  }
		  <T extends Y<<CompleteOnType:Z>>>
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
	"diet ast");
}
public void test0092(){
	String str =
		"""
		public class X {
		  <T extends Y<Z>> int foo
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <T extends Y<<CompleteOnType:Z>>>
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
	"diet ast");
}
public void test0093(){
	String str =
		"""
		public class X {
		  <T extends Y<Z X
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<<CompleteOnType:Z>>;
		  public X() {
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
	"diet ast");
}
public void test0094(){
	String str =
		"""
		public class X {
		  <T extends Y<Z> X
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <T extends Y<<CompleteOnType:Z>>>
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
	"diet ast");
}
public void test0095(){
	String str =
		"""
		public class X {
		  <T extends Y<Z>> X
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <T extends Y<<CompleteOnType:Z>>>
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
	"diet ast");
}
public void test0096(){
	String str =
		"""
		public class X {
		  <T extends Y<Z
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<<CompleteOnType:Z>>;
		  public X() {
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
	"diet ast");
}
public void test0097(){
	String str =
		"""
		public class X {
		  <T extends Y<Z>
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <T extends Y<<CompleteOnType:Z>>>
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
	"diet ast");
}
public void test0098(){
	String str =
		"""
		public class X {
		  <T extends Y<Z>>
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <T extends Y<<CompleteOnType:Z>>>
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
	"diet ast");
}
public void test0099_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z bar();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0099_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z bar();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0100_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z> bar();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0100_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z> bar();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0101_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z>> bar();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0101_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z>> bar();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    this.<Y, Y<<CompleteOnType:Z>>>bar();
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0102_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z bar
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0102_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z bar
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0103_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z> bar
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0103_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z> bar
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0104_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z>> bar
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0104_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z>> bar
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0105_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0105_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0106_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z>
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0106_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z>
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0107_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z>>
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0107_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    this.<Y, Y<Z>>
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0108_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    new <Y, Y<Z X();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0108_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    new <Y, Y<Z X();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0109_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    new <Y, Y<Z> X();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0109_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    new <Y, Y<Z> X();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0110_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    new <Y, Y<Z>> X();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0110_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    new <Y, Y<Z>> X();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0111_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    aaa.new <Y, Y<Z X();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0111_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    aaa.new <Y, Y<Z X();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0112_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    aaa.new <Y, Y<Z> X();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0112_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    aaa.new <Y, Y<Z> X();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0113_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    aaa.new <Y, Y<Z>> X();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0113_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    aaa.new <Y, Y<Z>> X();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    aaa.new <Y, Y<<CompleteOnType:Z>>>X();
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0114_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    new V().new <Y, Y<Z X();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0114_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    new V().new <Y, Y<Z X();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0115_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    new V().new <Y, Y<Z> X();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0115_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    new V().new <Y, Y<Z> X();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0116_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    new V().new <Y, Y<Z>> X();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0116_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    new V().new <Y, Y<Z>> X();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    new V().new <Y, Y<<CompleteOnType:Z>>>X();
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0117_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    for(Y<Z var;;){}
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0117_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    for(Y<Z var;;){}
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0118_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    for(Y<Z> var;;){}
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0118_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    for(Y<Z> var;;){}
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0119_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    for(Y<Z
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0119_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    for(Y<Z
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0120_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    for(Y<Z>
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0120_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    for(Y<Z>
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0121_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    try {
		    } catch(Y<Z e) {
		   }
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0121_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    try {
		    } catch(Y<Z e) {
		   }
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0122_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    try {
		    } catch(Y<Z> e) {
		   }
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0122_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    try {
		    } catch(Y<Z> e) {
		   }
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0123_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    try {
		    } catch(Y<Z e
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0123_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    try {
		    } catch(Y<Z e
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0124_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    try {
		    } catch(Y<Z> e
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0124_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    try {
		    } catch(Y<Z> e
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0125_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    catch(Y<Z e
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0125_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    catch(Y<Z e
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0126_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    catch(Y<Z> e
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0126_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    catch(Y<Z> e
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0127_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object a = (Y<Z ) e;
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0127_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object a = (Y<Z ) e;
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:Z>";
	String expectedParentNodeToString = "(Y < <CompleteOnName:Z>)";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object a = (Y < <CompleteOnName:Z>);
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0128_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object a = (Y<Z> ) e;
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0128_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object a = (Y<Z> ) e;
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:Z>";
	String expectedParentNodeToString = "(Y < <CompleteOnName:Z>)";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object a = (Y < <CompleteOnName:Z>);
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0129_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    (Y<Z) e;
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0129_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    (Y<Z) e;
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:Z>";
	String expectedParentNodeToString = "(Y < <CompleteOnName:Z>)";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    (Y < <CompleteOnName:Z>);
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0130_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    (Y<Z>) e;
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0130_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    (Y<Z>) e;
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:Z>";
	String expectedParentNodeToString = "(Y < <CompleteOnName:Z>)";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    (Y < <CompleteOnName:Z>);
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0131_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object[] o = new Y<Z[0];
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0131_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object[] o = new Y<Z[0];
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object[] o = new Y<<CompleteOnType:Z>>();
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0132_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object[] o = new Y<Z>[0];
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0132_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object[] o = new Y<Z>[0];
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object[] o = new Y<<CompleteOnType:Z>>();
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0133_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object[] o = new Y<Z
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0133_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object[] o = new Y<Z
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object[] o = new Y<<CompleteOnType:Z>>();
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0134_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object[] o = new Y<Z>
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0134_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object[] o = new Y<Z>
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object[] o = new Y<<CompleteOnType:Z>>();
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0135_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    new Y<Z
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0135_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    new Y<Z
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0136_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    new Y<Z>
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0136_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    new Y<Z>
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0137_Diet(){
	String str =
		"""
		public class X {
		  public X() {
		    <Y<Z super(0);
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
	"diet ast");
}
public void test0137_Method(){
	String str =
		"""
		public class X {
		  public X() {
		    <Y<Z super(0);
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		    super();
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0138_Diet(){
	String str =
		"""
		public class X {
		  public X() {
		    <Y<Z> super(0);
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
	"diet ast");
}
public void test0138_Method(){
	String str =
		"""
		public class X {
		  public X() {
		    <Y<Z> super(0);
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		    super();
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0139_Diet(){
	String str =
		"""
		public class X {
		  public X() {
		    <Y<Z>> super(0);
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
	"diet ast");
}
public void test0139_Method(){
	String str =
		"""
		public class X {
		  public X() {
		    <Y<Z>> super(0);
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		    super();
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0140_Diet(){
	String str =
		"""
		public class X {
		  public X() {
		    aaa.<Y<Z super(0);
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
	"diet ast");
}
public void test0140_Method(){
	String str =
		"""
		public class X {
		  public X() {
		    aaa.<Y<Z super(0);
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		    super();
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0141_Diet(){
	String str =
		"""
		public class X {
		  public X() {
		    aaa.<Y<Z> super(0);
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
	"diet ast");
}
public void test0141_Method(){
	String str =
		"""
		public class X {
		  public X() {
		    aaa.<Y<Z> super(0);
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		    super();
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0142_Diet(){
	String str =
		"""
		public class X {
		  public X() {
		    aaa.<Y<Z>> super(0);
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
	"diet ast");
}
public void test0142_Method(){
	String str =
		"""
		public class X {
		  public X() {
		    aaa.<Y<Z>> super(0);
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		    super();
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0143_Diet(){
	String str =
		"""
		public class X {
		  public X() {
		    A.this.<Y<Z super(0);
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
	"diet ast");
}
public void test0143_Method(){
	String str =
		"""
		public class X {
		  public X() {
		    A.this.<Y<Z super(0);
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		    super();
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0144_Diet(){
	String str =
		"""
		public class X {
		  public X() {
		    A.this.<Y<Z> super(0);
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
	"diet ast");
}
public void test0144_Method(){
	String str =
		"""
		public class X {
		  public X() {
		    A.this.<Y<Z> super(0);
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		    Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0145_Diet(){
	String str =
		"""
		public class X {
		  public X() {
		    A.this.<Y<Z>> super(0);
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
	"diet ast");
}
public void test0145_Method(){
	String str =
		"""
		public class X {
		  public X() {
		    A.this.<Y<Z>> super(0);
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		    A.this.<Y<<CompleteOnType:Z>>>super(0);
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0146(){
	String str =
		"""
		public class X {
		  W<Y<Z
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<<CompleteOnType:Z>>;
		  public X() {
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
	"diet ast");
}
public void test0147(){
	String str =
		"""
		public class X {
		  W<Y<Z>
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<<CompleteOnType:Z>>;
		  public X() {
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
	"diet ast");
}
public void test0148(){
	String str =
		"""
		public class X {
		  W<Y<Z>>
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<<CompleteOnType:Z>>;
		  public X() {
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
	"diet ast");
}
public void test0149(){
	String str =
		"""
		public class X {
		  W<Y<Z>> var
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  W<Y<<CompleteOnType:Z>>> var;
		  public X() {
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
	"diet ast");
}
public void test0150(){
	String str =
		"""
		public class X {
		  W<Y<Z>> var;
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  W<Y<<CompleteOnType:Z>>> var;
		  public X() {
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
	"diet ast");
}
public void test0151(){
	String str =
		"""
		public class X {
		  W<A,B,C
		}""";


	String completeBehind = "A,B";
	int cursorLocation = str.indexOf("A,B") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:B>";
	String expectedParentNodeToString = "W<A, <CompleteOnType:B>, C>";
	String completionIdentifier = "B";
	String expectedReplacedSource = "B";
	String expectedUnitDisplayString =
		"""
		public class X {
		  W<A, <CompleteOnType:B>, C>;
		  public X() {
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
	"diet ast");
}
public void test0152(){
	String str =
		"""
		public class X {
		  W<A,B,C>
		}""";


	String completeBehind = "A,B";
	int cursorLocation = str.indexOf("A,B") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:B>";
	String expectedParentNodeToString = "W<A, <CompleteOnType:B>, C>";
	String completionIdentifier = "B";
	String expectedReplacedSource = "B";
	String expectedUnitDisplayString =
		"""
		public class X {
		  W<A, <CompleteOnType:B>, C>;
		  public X() {
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
	"diet ast");
}
public void test0153(){
	String str =
		"""
		public class X {
		  W<A,B,C> var
		}""";


	String completeBehind = "A,B";
	int cursorLocation = str.indexOf("A,B") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:B>";
	String expectedParentNodeToString = "W<A, <CompleteOnType:B>, C>";
	String completionIdentifier = "B";
	String expectedReplacedSource = "B";
	String expectedUnitDisplayString =
		"""
		public class X {
		  W<A, <CompleteOnType:B>, C> var;
		  public X() {
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
	"diet ast");
}
public void test0154(){
	String str =
		"""
		public class X {
		  W<A,B,C> var;
		}""";


	String completeBehind = "A,B";
	int cursorLocation = str.indexOf("A,B") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:B>";
	String expectedParentNodeToString = "W<A, <CompleteOnType:B>, C>";
	String completionIdentifier = "B";
	String expectedReplacedSource = "B";
	String expectedUnitDisplayString =
		"""
		public class X {
		  W<A, <CompleteOnType:B>, C> var;
		  public X() {
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
	"diet ast");
}
public void test0155(){
	String str =
		"""
		public class X {
		  Y<Z>.V<W> var;
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>.V<W>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<<CompleteOnType:Z>>.V<W> var;
		  public X() {
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
	"diet ast");
}
public void test0156(){
	String str =
		"""
		public class X {
		  Y<Z>.V<W> var
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>.V<W>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<<CompleteOnType:Z>>.V<W> var;
		  public X() {
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
	"diet ast");
}
public void test0157(){
	String str =
		"""
		public class X {
		  Y<Z>.V<W>
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>.V<W>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<<CompleteOnType:Z>>.V<W>;
		  public X() {
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
	"diet ast");
}
public void test0158(){
	String str =
		"""
		public class X {
		  Y<Z>.V<W
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>.V<W>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Y<<CompleteOnType:Z>>.V<W>;
		  public X() {
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
	"diet ast");
}
public void test0159_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object a = (W<Y<Z> ) e;
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0159_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object a = (W<Y<Z> ) e;
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object a = Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0160_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    ((Y<Z>) e).foo();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0160_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    ((Y<Z>) e).foo();
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:Z>";
	String expectedParentNodeToString = "(Y < <CompleteOnName:Z>)";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    (Y < <CompleteOnName:Z>);
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0161(){
	String str =
		"public class X  <T extends Z<Y>> {\n" +
		"}";


	String completeBehind = "Z";
	int cursorLocation = str.indexOf("Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "T extends <CompleteOnType:Z>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X<T extends <CompleteOnType:Z>> {
		  public X() {
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
	"diet ast");
}
public void test0162(){
	String str =
		"public class X  <T extends X.Z<Y>> {\n" +
		"}";


	String completeBehind = "Z";
	int cursorLocation = str.indexOf("Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:X.Z>";
	String expectedParentNodeToString = "T extends <CompleteOnType:X.Z>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "X.Z";
	String expectedUnitDisplayString =
		"""
		public class X<T extends <CompleteOnType:X.Z>> {
		  public X() {
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
	"diet ast");
}
public void test0163(){
	String str =
		"public class X  <T extends X<W>.Z<Y>> {\n" +
		"}";


	String completeBehind = "Z";
	int cursorLocation = str.indexOf("Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:X<W>.Z>";
	String expectedParentNodeToString = "T extends <CompleteOnType:X<W>.Z>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "X<W>.Z";
	String expectedUnitDisplayString =
		"""
		public class X<T extends <CompleteOnType:X<W>.Z>> {
		  public X() {
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
	"diet ast");
}
public void test0164(){
	String str =
		"""
		public class X {
		  <T extends X<W>.Z> foo() {}
		}""";


	String completeBehind = "Z";
	int cursorLocation = str.indexOf("Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:X<W>.Z>";
	String expectedParentNodeToString = "T extends <CompleteOnType:X<W>.Z>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "X<W>.Z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  <T extends <CompleteOnType:X<W>.Z>>foo() {
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
	"diet ast");
}
public void test0165_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object a = (W.Y<Z>) e;
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0165_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object a = (W.Y<Z>) e;
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:Z>";
	String expectedParentNodeToString = "(W.Y < <CompleteOnName:Z>)";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object a = (W.Y < <CompleteOnName:Z>);
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0166_Diet(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object a = (W<U>.Y<Z>) e;
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void test0166_Method(){
	String str =
		"""
		public class X {
		  void foo() {
		    Object a = (W<U>.Y<Z>) e;
		  }
		}""";


	String completeBehind = "Y<Z";
	int cursorLocation = str.indexOf("Y<Z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Z>";
	String expectedParentNodeToString = "Y<<CompleteOnType:Z>>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    Object a = W<U>.Y<<CompleteOnType:Z>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
* https://bugs.eclipse.org/bugs/show_bug.cgi?id=69598
*/
public void test0167_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    List<Integer> cont=new ArrayList<Integer>();
		    for (Integer i:cont){
		      i.
		    }
		  }
		}""";


	String completeBehind = "i.";
	int cursorLocation = str.indexOf("i.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
/*
* https://bugs.eclipse.org/bugs/show_bug.cgi?id=69598
*/
public void test0167_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    List<Integer> cont=new ArrayList<Integer>();
		    for (Integer i:cont){
		      i.
		    }
		  }
		}""";


	String completeBehind = "i.";
	int cursorLocation = str.indexOf("i.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:i.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "i.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    List<Integer> cont;
		    for (Integer i : cont)\s
		      {
		        <CompleteOnName:i.>;
		      }
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
* https://bugs.eclipse.org/bugs/show_bug.cgi?id=69598
*/
public void test0168_Diet(){
	String str =
		"""
		public class X {
		  void foo(){
		    List<Integer> cont=new ArrayList<Integer>();
		    for (Integer i:cont){
		    }
		    i.
		  }
		}""";


	String completeBehind = "i.";
	int cursorLocation = str.indexOf("i.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
/*
* https://bugs.eclipse.org/bugs/show_bug.cgi?id=69598
*/
public void test0168_Method(){
	String str =
		"""
		public class X {
		  void foo(){
		    List<Integer> cont=new ArrayList<Integer>();
		    for (Integer i:cont){
		    }
		    i.
		  }
		}""";


	String completeBehind = "i.";
	int cursorLocation = str.indexOf("i.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:i.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "i.";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    List<Integer> cont;
		    <CompleteOnName:i.>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
* https://bugs.eclipse.org/bugs/show_bug.cgi?id=71705
*/
public void test0169(){
	String str =
		"""
		public class X {
		  Object o;
		  void foo(int[] a, int[] b){
		    if(a.lenth < b.length)
		      System.out.println();
		  }
		}""";


	String completeBehind = "Object";
	int cursorLocation = str.indexOf("Object") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Object>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Object";
	String expectedReplacedSource = "Object";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:Object>;
		  public X() {
		  }
		  void foo(int[] a, int[] b) {
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
	"diet ast");
}
/*
* https://bugs.eclipse.org/bugs/show_bug.cgi?id=71705
*/
public void test0170(){
	String str =
		"""
		public class X {
		  bar
		  void foo(){
		    A<B
		  }
		}""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:bar>;
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
public void _testXXX2(){
	String str =
		"public class X extends Y. {\n" +
		"}";


	String completeBehind = "Y";
	int cursorLocation = str.indexOf("Y") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnClass:Y>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Z";
	String expectedReplacedSource = "Z";
	String expectedUnitDisplayString =
		"""
		public class X extends <CompleteOnClass:Y> {
		  public X() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0171_Diet(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(A<B>) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0171_Method(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(A<B>) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		    Object o;
		    <CompleteOnName:bar>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0172_Diet(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(A<B>[]) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0172_Method(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(A<B>[]) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		    Object o;
		    <CompleteOnName:bar>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0173_Diet(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(A<B>.C) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0173_Method(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(A<B>.C) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		    Object o;
		    <CompleteOnName:bar>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0174_Diet(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(A<B>.C[]) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0174_Method(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(A<B>.C[]) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		    Object o;
		    <CompleteOnName:bar>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0175_Diet(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(A<B>.C<D>) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0175_Method(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(A<B>.C<D>) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		    Object o;
		    <CompleteOnName:bar>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0176_Diet(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(A<B>.C<D>[]) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public void foo() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=71702
 */
public void test0176_Method(){
	String str =
		"""
		public class X{
		  public void foo() {
		    Object o =(A<B>.C<D>[]) tmp;
		    bar
		  }
		}
		
		""";


	String completeBehind = "bar";
	int cursorLocation = str.indexOf("bar") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:bar>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "bar";
	String expectedReplacedSource = "bar";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		    Object o;
		    <CompleteOnName:bar>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=68594
 */
public void test0177(){
	String str =
		"""
		public class X{
		  Stack<List<Object>> o = null;
		}
		
		""";


	String completeBehind = "Stack";
	int cursorLocation = str.indexOf("Stack") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Stack>;";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Stack";
	String expectedReplacedSource = "Stack";
	String expectedUnitDisplayString =
		"""
		public class X {
		  <CompleteOnType:Stack>;
		  public X() {
		  }
		}
		"""
;

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72238
 */
public void test0178(){
	String str =
		"""
		public class X <T>{
		  X<ZZZ<
		}
		
		""";

	String completeBehind = "ZZZ";
	int cursorLocation = str.indexOf("ZZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	String expectedParentNodeToString = "X<<CompleteOnType:ZZZ>>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
		"""
		public class X<T> {
		  X<<CompleteOnType:ZZZ>>;
		  public X() {
		  }
		}
		"""
;

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72238
 */
public void test0179(){
	String str =
		"""
		public class X <T>{
		  X<ZZZ.
		}
		
		""";

	String completeBehind = "ZZZ";
	int cursorLocation = str.indexOf("ZZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	String expectedParentNodeToString = "X<<CompleteOnType:ZZZ>>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
		"""
		public class X<T> {
		  X<<CompleteOnType:ZZZ>>;
		  public X() {
		  }
		}
		"""
;

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72238
 */
public void test0180(){
	String str =
		"""
		public class X <T>{
		  X<ZZZ
		}
		
		""";

	String completeBehind = "ZZZ";
	int cursorLocation = str.indexOf("ZZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	String expectedParentNodeToString = "X<<CompleteOnType:ZZZ>>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
		"""
		public class X<T> {
		  X<<CompleteOnType:ZZZ>>;
		  public X() {
		  }
		}
		"""
;

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72238
 */
public void test0181(){
	String str =
		"""
		public class X <T>{
		  X<ZZZ>
		}
		
		""";

	String completeBehind = "ZZZ";
	int cursorLocation = str.indexOf("ZZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	String expectedParentNodeToString = "X<<CompleteOnType:ZZZ>>";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
		"""
		public class X<T> {
		  X<<CompleteOnType:ZZZ>>;
		  public X() {
		  }
		}
		"""
;

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=73573
 */
public void test0182(){
	String str =
		"""
		public class X <T>{
		  X<
		}
		
		""";

	String completeBehind = "X";
	int cursorLocation = str.indexOf("X<") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:X>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "X";
	String expectedReplacedSource = "X";
	String expectedUnitDisplayString =
		"""
		public class X<T> {
		  <CompleteOnType:X>;
		  public X() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=73573
 */
public void test0183(){
	String str =
		"""
		public class X <T>{
		  X<Object
		}
		
		""";

	String completeBehind = "X";
	int cursorLocation = str.indexOf("X<") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:X>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "X";
	String expectedReplacedSource = "X";
	String expectedUnitDisplayString =
		"""
		public class X<T> {
		  <CompleteOnType:X>;
		  public X() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=75649
 */
public void test0184_Diet(){
	String str =
		"""
		public class X <T>{
		  void foo() {
		    X<? extends String> s;
		  }
		}
		
		""";

	String completeBehind = "Strin";
	int cursorLocation = str.indexOf("Strin") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X<T> {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=75649
 */
public void test0184_Method(){
	String str =
		"""
		public class X <T>{
		  void foo() {
		    X<? extends String> s;
		  }
		}
		
		""";

	String completeBehind = "Strin";
	int cursorLocation = str.indexOf("Strin") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Strin>";
	String expectedParentNodeToString = "? extends <CompleteOnType:Strin>";
	String completionIdentifier = "Strin";
	String expectedReplacedSource = "String";
	String expectedUnitDisplayString =
			"""
		public class X<T> {
		  public X() {
		  }
		  void foo() {
		    X<? extends <CompleteOnType:Strin>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83236
 */
public void test0185(){
	String str =
		"""
		public class Test {
		  Boolean
		   * some text <b>bold<i>both</i></b>
		   */
		  public void foo(String s) {
		  }
		}
		""";

	String completeBehind = "Boolean";
	int cursorLocation = str.indexOf("Boolean") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Boolean>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Boolean";
	String expectedReplacedSource = "Boolean";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  <CompleteOnType:Boolean>;
		  some text;
		  bold<i> both;
		  public Test() {
		  }
		  public void foo(String s) {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=93119
 */
public void test0186(){
	String str =
		"""
		public class Test {
		  List<? ext
		}
		""";

	String completeBehind = "ext";
	int cursorLocation = str.indexOf("ext") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  List<? extends <CompleteOnKeyword:ext>>;
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=93119
 */
public void test0187_Diet(){
	String str =
		"""
		public class Test {
		  void foo() {
		    List<? ext
		  }
		}
		""";

	String completeBehind = "ext";
	int cursorLocation = str.indexOf("ext") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=93119
 */
public void test0187_Method(){
	String str =
		"""
		public class Test {
		  void foo() {
		    List<? ext
		  }
		}
		""";

	String completeBehind = "ext";
	int cursorLocation = str.indexOf("ext") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
			"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
		    List<? extends <CompleteOnKeyword:ext>>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=80432
 */
public void test0188_Diet(){
	String str =
		"""
		public class Test {
		  void foo() {
		    for(;;) {
		      bar(toto.
		    }
		  }
		}
		""";

	String completeBehind = "toto.";
	int cursorLocation = str.indexOf("toto.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=80432
 */
public void test0188_Method(){
	String str =
		"""
		public class Test {
		  void foo() {
		    for(;;) {
		      bar(toto.
		    }
		  }
		}
		""";

	String completeBehind = "toto.";
	int cursorLocation = str.indexOf("toto.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:toto.>";
	String expectedParentNodeToString = "bar(<CompleteOnName:toto.>)";
	String completionIdentifier = "";
	String expectedReplacedSource = "toto.";
	String expectedUnitDisplayString =
			"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
		    {
		      bar(<CompleteOnName:toto.>);
		    }
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82560
 */
public void test0189_Diet(){
	String str =
		"""
		public class Test {
		  void bar() {
		    zzz.<String>foo(new Runtime());
		  }
		}
		""";

	String completeBehind = "Runtime";
	int cursorLocation = str.indexOf("Runtime") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void bar() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82560
 */
public void test0189_Method(){
	String str =
		"""
		public class Test {
		  void bar() {
		    zzz.<String>foo(new Runtime());
		  }
		}
		""";

	String completeBehind = "Runtime";
	int cursorLocation = str.indexOf("Runtime") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Runtime>";
	String expectedParentNodeToString = "zzz.foo(new <CompleteOnType:Runtime>())";
	String completionIdentifier = "Runtime";
	String expectedReplacedSource = "Runtime";
	String expectedUnitDisplayString =
			"""
		public class Test {
		  public Test() {
		  }
		  void bar() {
		    zzz.foo(new <CompleteOnType:Runtime>());
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82560
 */
public void test0190_Diet(){
	String str =
		"""
		public class Test {
		  void bar() {
		    zzz.<String>foo(var);
		  }
		}
		""";

	String completeBehind = "var";
	int cursorLocation = str.indexOf("var") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void bar() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82560
 */
public void test0190_Method(){
	String str =
		"""
		public class Test {
		  void bar() {
		    zzz.<String>foo(var);
		  }
		}
		""";

	String completeBehind = "var";
	int cursorLocation = str.indexOf("var") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:var>";
	String expectedParentNodeToString = "zzz.foo(<CompleteOnName:var>)";
	String completionIdentifier = "var";
	String expectedReplacedSource = "var";
	String expectedUnitDisplayString =
			"""
		public class Test {
		  public Test() {
		  }
		  void bar() {
		    zzz.foo(<CompleteOnName:var>);
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82560
 */
public void test0191_Diet(){
	String str =
		"""
		public class Test {
		  void bar() {
		    zzz.<String>foo();
		  }
		}
		""";

	String completeBehind = "foo(";
	int cursorLocation = str.indexOf("foo(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void bar() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82560
 */
public void test0191_Method(){
	String str =
		"""
		public class Test {
		  void bar() {
		    zzz.<String>foo();
		  }
		}
		""";

	String completeBehind = "foo(";
	int cursorLocation = str.indexOf("foo(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnMessageSend:zzz.foo()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "foo(";
	String expectedUnitDisplayString =
			"""
		public class Test {
		  public Test() {
		  }
		  void bar() {
		    <CompleteOnMessageSend:zzz.foo()>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82560
 */
public void test0192_Diet(){
	String str =
		"""
		public class Test {
		  void bar() {
		    zzz.<String>foo();
		  }
		}
		""";

	String completeBehind = "fo";
	int cursorLocation = str.indexOf("fo") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void bar() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=82560
 */
public void test0192_Method(){
	String str =
		"""
		public class Test {
		  void bar() {
		    zzz.<String>foo();
		  }
		}
		""";

	String completeBehind = "fo";
	int cursorLocation = str.indexOf("fo") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnMessageSendName:zzz.<String>fo()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "fo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class Test {
		  public Test() {
		  }
		  void bar() {
		    <CompleteOnMessageSendName:zzz.<String>fo()>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94641
 */
public void test0193_Diet(){
	String str =
		"""
		public class Test {
		  void bar() {
		    new Foo<X>();
		  }
		}
		""";

	String completeBehind = ">(";
	int cursorLocation = str.indexOf(">(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void bar() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94641
 */
public void test0193_Method(){
	String str =
		"""
		public class Test {
		  void bar() {
		    new Foo<X>();
		  }
		}
		""";

	String completeBehind = ">(";
	int cursorLocation = str.indexOf(">(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new Foo<X>()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"""
		public class Test {
		  public Test() {
		  }
		  void bar() {
		    <CompleteOnAllocationExpression:new Foo<X>()>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94641
 */
public void test0194_Diet(){
	String str =
		"""
		public class Test {
		  void bar() {
		    new Foo<X<X>>();
		  }
		}
		""";

	String completeBehind = ">(";
	int cursorLocation = str.indexOf(">(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void bar() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94641
 */
public void test0194_Method(){
	String str =
		"""
		public class Test {
		  void bar() {
		    new Foo<X<X>>();
		  }
		}
		""";

	String completeBehind = ">(";
	int cursorLocation = str.indexOf(">(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new Foo<X<X>>()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"""
		public class Test {
		  public Test() {
		  }
		  void bar() {
		    <CompleteOnAllocationExpression:new Foo<X<X>>()>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94641
 */
public void test0195_Diet(){
	String str =
		"""
		public class Test {
		  void bar() {
		    new Foo<X<X<X>>>();
		  }
		}
		""";

	String completeBehind = ">(";
	int cursorLocation = str.indexOf(">(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void bar() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94641
 */
public void test0195_Method(){
	String str =
		"""
		public class Test {
		  void bar() {
		    new Foo<X<X<X>>>();
		  }
		}
		""";

	String completeBehind = ">(";
	int cursorLocation = str.indexOf(">(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAllocationExpression:new Foo<X<X<X>>>()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"""
		public class Test {
		  public Test() {
		  }
		  void bar() {
		    <CompleteOnAllocationExpression:new Foo<X<X<X>>>()>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94907
 */
public void test0196(){
	String str =
		"""
		public class Test<T> ext{
		  void bar() {
		  }
		}
		""";

	String completeBehind = "ext";
	int cursorLocation = str.indexOf("ext") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"""
		public class Test<T> extends <CompleteOnKeyword:ext> {
		  {
		  }
		  public Test() {
		  }
		  void bar() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94907
 */
public void test0197(){
	String str =
		"""
		public class Test<T> imp{
		  void bar() {
		  }
		}
		""";

	String completeBehind = "imp";
	int cursorLocation = str.indexOf("imp") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnKeyword:imp>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "imp";
	String expectedReplacedSource = "imp";
	String expectedUnitDisplayString =
		"""
		public class Test<T> extends <CompleteOnKeyword:imp> {
		  {
		  }
		  public Test() {
		  }
		  void bar() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94907
 */
public void test0198(){
	String str =
		"""
		public class Test<T> extends X ext {
		  void bar() {
		  }
		}
		""";

	String completeBehind = "X ext";
	int cursorLocation = str.indexOf("X ext") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"""
		public class Test<T> extends <CompleteOnKeyword:ext> {
		  {
		  }
		  public Test() {
		  }
		  void bar() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94907
 */
public void test0199(){
	String str =
		"""
		public class Test<T> extends X imp {
		  void bar() {
		  }
		}
		""";

	String completeBehind = "X imp";
	int cursorLocation = str.indexOf("X imp") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnKeyword:imp>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "imp";
	String expectedReplacedSource = "imp";
	String expectedUnitDisplayString =
		"""
		public class Test<T> extends <CompleteOnKeyword:imp> {
		  {
		  }
		  public Test() {
		  }
		  void bar() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94907
 */
public void test0200(){
	String str =
		"""
		public interface Test<T> ext{
		  void bar() {
		  }
		}
		""";

	String completeBehind = "ext";
	int cursorLocation = str.indexOf("ext") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnKeyword:ext>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "ext";
	String expectedReplacedSource = "ext";
	String expectedUnitDisplayString =
		"""
		public interface Test<T> extends <CompleteOnKeyword:ext> {
		  {
		  }
		  <clinit>() {
		  }
		  void bar() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94907
 */
public void test0201(){
	String str =
		"""
		public interface Test<T> imp{
		  void bar() {
		  }
		}
		""";

	String completeBehind = "imp";
	int cursorLocation = str.indexOf("imp") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnKeyword:imp>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "imp";
	String expectedReplacedSource = "imp";
	String expectedUnitDisplayString =
		"""
		public interface Test<T> extends <CompleteOnKeyword:imp> {
		  {
		  }
		  <clinit>() {
		  }
		  void bar() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=100302
 */
public void test0202_Diet(){
	String str =
		"""
		public class Test {
		  void bar() {
		    for (Entry entry : (Set<Entry>) var) {
		      entry.
		    }
		  }
		}
		""";

	String completeBehind = "entry.";
	int cursorLocation = str.indexOf("entry.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void bar() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=100302
 */
public void test0202_Method(){
	String str =
		"""
		public class Test {
		  void bar() {
		    for (Entry entry : (Set<Entry>) var) {
		      entry.
		    }
		  }
		}
		""";

	String completeBehind = "entry.";
	int cursorLocation = str.indexOf("entry.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:entry.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "entry.";
	String expectedUnitDisplayString =
			"""
		public class Test {
		  public Test() {
		  }
		  void bar() {
		    for (Entry entry : (Set<Entry>) var)\s
		      {
		        <CompleteOnName:entry.>;
		      }
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=100302
 */
public void test0203_Diet(){
	String str =
		"""
		public class Test {
		  void bar() {
		    for (Entry entry : (ZZZ<YYY>.Set<Entry>) var) {
		      entry.
		    }
		  }
		}
		""";

	String completeBehind = "entry.";
	int cursorLocation = str.indexOf("entry.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void bar() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=100302
 */
public void test0203_Method(){
	String str =
		"""
		public class Test {
		  void bar() {
		    for (Entry entry : (ZZZ<YYY>.Set<Entry>) var) {
		      entry.
		    }
		  }
		}
		""";

	String completeBehind = "entry.";
	int cursorLocation = str.indexOf("entry.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:entry.>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "entry.";
	String expectedUnitDisplayString =
			"""
		public class Test {
		  public Test() {
		  }
		  void bar() {
		    for (Entry entry : (ZZZ<YYY>.Set<Entry>) var)\s
		      {
		        <CompleteOnName:entry.>;
		      }
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=103148
 */
public void test0204_Diet(){
	String str =
		"""
		public class Test {
			public enum MyEnum { A };
			public static void foo() {
				EnumSet.<MyEnum>of(MyEnum.A);
				zzz
			}
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public enum MyEnum {
		    A(),
		    <clinit>() {
		    }
		    public MyEnum() {
		    }
		  }
		  public Test() {
		  }
		  public static void foo() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=103148
 */
public void test0204_Method(){
	String str =
		"""
		public class Test {
			public enum MyEnum { A };
			public static void foo() {
				EnumSet.<MyEnum>of(MyEnum.A);
				zzz
			}
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		public class Test {
		  public enum MyEnum {
		    A(),
		    <clinit>() {
		    }
		    public MyEnum() {
		    }
		  }
		  public Test() {
		  }
		  public static void foo() {
		    <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=123514
public void test0205(){
	String str =
		"""
		public class X {
		  <T> HashMap<K, V>
		}""";


	String completeBehind = "HashMap<";
	int cursorLocation = str.indexOf("HashMap<") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "HashMap<<CompleteOnType:>, V>";
	String completionIdentifier = "";
	String expectedReplacedSource = "K";
	String expectedUnitDisplayString =
		"""
		public class X {
		  HashMap<<CompleteOnType:>, V>;
		  public X() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0206_Diet(){
	String str =
		"""
		public class Test {
			void foo() {
			  Collections.<B>zzz
			}
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0206_Method(){
	String str =
		"""
		public class Test {
			void foo() {
			  Collections.<B>zzz
			}
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnMessageSendName:Collections.<B>zzz()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
		    <CompleteOnMessageSendName:Collections.<B>zzz()>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0207_Diet(){
	String str =
		"""
		public class Test {
			void foo() {
			  bar().<B>zzz
			}
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0207_Method(){
	String str =
		"""
		public class Test {
			void foo() {
			  bar().<B>zzz
			}
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnMessageSendName:bar().<B>zzz()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
		    <CompleteOnMessageSendName:bar().<B>zzz()>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0208_Diet(){
	String str =
		"""
		public class Test {
			void foo() {
			  int.<B>zzz
			}
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0208_Method(){
	String str =
		"""
		public class Test {
			void foo() {
			  int.<B>zzz
			}
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:zzz>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
		    <CompleteOnName:zzz>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0209_Diet(){
	String str =
		"""
		public class Test {
			void foo() {
			  this.<B>zzz
			}
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0209_Method(){
	String str =
		"""
		public class Test {
			void foo() {
			  this.<B>zzz
			}
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnMessageSendName:this.<B>zzz()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
		    <CompleteOnMessageSendName:this.<B>zzz()>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0210_Diet(){
	String str =
		"""
		public class Test {
			void foo() {
			  super.<B>zzz
			}
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
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
	"diet ast");
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
 */
public void test0210_Method(){
	String str =
		"""
		public class Test {
			void foo() {
			  super.<B>zzz
			}
		}
		""";

	String completeBehind = "zzz";
	int cursorLocation = str.indexOf("zzz") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnMessageSendName:super.<B>zzz()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "zzz";
	String expectedReplacedSource = "zzz";
	String expectedUnitDisplayString =
			"""
		public class Test {
		  public Test() {
		  }
		  void foo() {
		    <CompleteOnMessageSendName:super.<B>zzz()>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83685
public void test0211(){
	String str =
		"""
		public class Test{
		  Test.
		}
		""";

	String completeBehind = "Test";
	int cursorLocation = str.indexOf("Test.") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Test>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "Test";
	String expectedReplacedSource = "Test";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  <CompleteOnType:Test>;
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
	"diet ast");
}
public void test0212(){
	String str =
		"""
		public class Test {
		  List<? extends Obj>
		}
		""";

	String completeBehind = "Obj";
	int cursorLocation = str.indexOf("Obj") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:Obj>";
	String expectedParentNodeToString = "? extends <CompleteOnType:Obj>";
	String completionIdentifier = "Obj";
	String expectedReplacedSource = "Obj";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  List<? extends <CompleteOnType:Obj>>;
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
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0213_Diet() {

	String str =
		"""
		public class X {
			public boolean foo() {
		      try {
		         throwing();
		      }
		      catch (IllegalAccessException e) {
		         bar();
		      }
		      catch (Top<Object>.IZZ) {
		      }
		   }\
		}
		""";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  public boolean foo() {
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
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0213_Method() {

	String str =
		"""
		public class X {
			public boolean foo() {
		      try {
		         throwing();
		      }
		      catch (IllegalAccessException e) {
		         bar();
		      }
		      catch (Top<Object>.IZZ) {
		      }
		   }\
		}
		""";

	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf("IZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnException:Top<Object>.IZZ>";
	String expectedParentNodeToString =
			"""
		try
		  {
		    throwing();
		  }
		catch (IllegalAccessException e)
		  {
		  }
		catch (<CompleteOnException:Top<Object>.IZZ>  )
		  {
		  }""";
	String completionIdentifier = "IZZ";
	String expectedReplacedSource = "Top<Object>.IZZ";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public boolean foo() {
		    try
		      {
		        throwing();
		      }
		    catch (IllegalAccessException e)
		      {
		      }
		    catch (<CompleteOnException:Top<Object>.IZZ>  )
		      {
		      }
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=197400
public void test0214_Diet() {

	String str =
		"""
		public class X {
			static {
		      <>
		   }\
		}
		""";

	String completeBehind = "<";
	int cursorLocation = str.lastIndexOf("<") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  static {
		  }
		  <clinit>() {
		  }
		  public X() {
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
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=197400
public void test0214_Method() {

	String str =
		"""
		public class X {
			static {
		      <>
		   }\
		}
		""";

	String completeBehind = "<";
	int cursorLocation = str.lastIndexOf("<") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	// we are not in a constructor then the completion node isn't attached to the ast
	String expectedUnitDisplayString =
			"""
		public class X {
		  static {
		  }
		  <clinit>() {
		  }
		  public X() {
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=210273
public void test0215_Diet() {

	String str =
		"""
		public class X {
			void foo() {
		      this.<X>bar();
		   }\
		}
		""";

	String completeBehind = "bar(";
	int cursorLocation = str.lastIndexOf("bar(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void foo() {
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
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=210273
public void test0215_Method() {

	String str =
		"""
		public class X {
			void foo() {
		      this.<X>bar();
		   }\
		}
		""";

	String completeBehind = "bar(";
	int cursorLocation = str.lastIndexOf("bar(") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnMessageSend:this.<X>bar()>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "bar()";
	// we are not in a constructor then the completion node isn't attached to the ast
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    <CompleteOnMessageSend:this.<X>bar()>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void test0216_Diet() {

	String str =
		"""
		public class X {
			Object field = new Object(){
				void foo(List<String> ss) {
					for(String s: ss){
						s.z
					}
				}
			};
		}
		""";

	String completeBehind = "s.z";
	int cursorLocation = str.lastIndexOf("s.z") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:s.z>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "z";
	String expectedReplacedSource = "s.z";
	String expectedUnitDisplayString =
		"""
		public class X {
		  Object field = new Object() {
		    void foo(List<String> ss) {
		      String s;
		      {
		        <CompleteOnName:s.z>;
		      }
		    }
		  };
		  public X() {
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
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=207631
public void test0217_Method() {

	String str =
		"""
		public class X {
			void foo() {
		      int y = (x >> (1));
		      foo
		   }\
		}
		""";

	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf("foo") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	// we are not in a constructor then the completion node isn't attached to the ast
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    int y;
		    <CompleteOnName:foo>;
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=227546
public void test0218_Diet() {

	String str =
		"""
		public enum X {
			SUCCESS { ZZZ }
		}
		""";

	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf("ZZZ") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:ZZZ>";
	String expectedParentNodeToString =
			"""
		() {
		  <CompleteOnType:ZZZ>;
		}""";
	String completionIdentifier = "ZZZ";
	String expectedReplacedSource = "ZZZ";
	String expectedUnitDisplayString =
		"""
		public enum X {
		  SUCCESS() {
		    <CompleteOnType:ZZZ>;
		  },
		  public X() {
		  }
		  <clinit>() {
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
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=274557
public void test0219_Diet() {

	String str =
		"""
		public class X {
			@Annot(value="")
			int field;
		}
		""";

	String completeBehind = "value=\"";
	int cursorLocation = str.lastIndexOf("value=\"") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompletionOnString:\"\">";
	String expectedParentNodeToString = "@Annot(value = <CompletionOnString:\"\">)";
	String completionIdentifier = "";
	String expectedReplacedSource = "\"\"";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(value = <CompletionOnString:"">)
		  int field;
		  public X() {
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
	"diet ast");
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=274557
public void test0220_Diet() {

	String str =
		"""
		public class X {
			@Annot("")
			int field;
		}
		""";

	String completeBehind = "@Annot(\"";
	int cursorLocation = str.lastIndexOf("@Annot(\"") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompletionOnString:\"\">";
	String expectedParentNodeToString = "@Annot(value = <CompletionOnString:\"\">)";
	String completionIdentifier = "";
	String expectedReplacedSource = "\"\"";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(value = <CompletionOnString:"">)
		  int field;
		  public X() {
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
	"diet ast");
}
public void testBug351426(){
	String str =
		"""
		public class X<T> {
		  void foo() {
		    X<String> x = new X<>();
		  }
		}""";


	String completeBehind = "new X<";
	int cursorLocation = str.indexOf("new X<") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "X<<CompleteOnType:>>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"""
		public class X<T> {
		  public X() {
		  }
		  void foo() {
		    X<String> x = new X<<CompleteOnType:>>();
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void testBug351426b(){
	String str =
		"""
		public class X<T> {
			static class X1<E>{}
		  void foo() {
		    X1<String> x = new X.X1<>();
		  }
		}""";


	String completeBehind = "new X.X1<";
	int cursorLocation = str.indexOf("new X.X1<") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "X.X1<<CompleteOnType:>>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"""
		public class X<T> {
		  static class X1<E> {
		    X1() {
		    }
		  }
		  public X() {
		  }
		  void foo() {
		    X1<String> x = new X.X1<<CompleteOnType:>>();
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
public void testBug351426c(){
	String str =
		"""
		public class X<T> {
		  public X<String> foo() {
		   return new X<>();
		  }
		}""";


	String completeBehind = "new X<";
	int cursorLocation = str.indexOf("new X<") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "X<<CompleteOnType:>>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
			"""
		public class X<T> {
		  public X() {
		  }
		  public X<String> foo() {
		    return new X<<CompleteOnType:>>();
		  }
		}
		""";

	checkMethodParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
			"full ast");
}
}
