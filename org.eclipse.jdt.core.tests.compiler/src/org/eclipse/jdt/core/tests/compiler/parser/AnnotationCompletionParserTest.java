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
public class AnnotationCompletionParserTest extends AbstractCompletionTest {
static {
	//TESTS_NAMES= new String[]{"test0087"};
}

public AnnotationCompletionParserTest(String testName) {
	super(testName);
}
public static Test suite() {
	return buildAllCompliancesTestSuite(AnnotationCompletionParserTest.class);
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
		"public @MyAnn class X {\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"""
		@<CompleteOnType:MyAnn>
		class X {
		  X() {
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
		"public @MyAnn interface X {\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"""
		@<CompleteOnType:MyAnn>
		interface X {
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
		"public @MyAnn enum X {\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"""
		@<CompleteOnType:MyAnn>
		enum X {
		  X() {
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
public void test0004(){
	String str =
		"public @MyAnn @interface X {\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"""
		@<CompleteOnType:MyAnn>
		@interface X {
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
		"public @MyAnn class X\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"""
		@<CompleteOnType:MyAnn>
		class X {
		  X() {
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
		"public @MyAnn interface X\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"""
		@<CompleteOnType:MyAnn>
		interface X {
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
		"public @MyAnn enum X\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"""
		@<CompleteOnType:MyAnn>
		enum X {
		  X() {
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
public void test0008(){
	String str =
		"public @MyAnn @interface X\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"""
		@<CompleteOnType:MyAnn>
		@interface X {
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
		"public @MyAnn\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n";

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
		  public @MyAnn class Y {
		  }
		}""";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @<CompleteOnType:MyAnn>
		  class Y {
		    Y() {
		    }
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
public void test0011(){
	String str =
		"public class X {\n" +
		"  public @MyAnn class Y\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @<CompleteOnType:MyAnn>
		  class Y {
		    Y() {
		    }
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
public void test0012(){
	String str =
		"""
		public class X {
		  public @MyAnn
		}""";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @<CompleteOnType:MyAnn>
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
public void test0013_Diet(){
	String str =
		"""
		public class X {
		  public void foo() {
		    @MyAnn class Y {
		    }
		  }
		}""";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
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
public void test0013_Method(){
	String str =
		"""
		public class X {
		  public void foo() {
		    @MyAnn class Y {
		    }
		  }
		}""";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  public void foo() {
		    @<CompleteOnType:MyAnn>
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
public void test0014(){
	String str =
		"public @MyAnn(ZORK) class X {\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"""
		@<CompleteOnType:MyAnn>
		class X {
		  X() {
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
		"public @MyAnn(ZORK) class X\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"""
		@<CompleteOnType:MyAnn>
		class X {
		  X() {
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
		"public @MyAnn(ZORK)\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n";

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
		"public @MyAnn(v1=\"\", v2=\"\") class X {\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"""
		@<CompleteOnType:MyAnn>
		class X {
		  X() {
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
		"public @MyAnn(v1=\"\", v2=\"\")) class X\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"""
		@<CompleteOnType:MyAnn>
		class X {
		  X() {
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
		"public @MyAnn(v1=\"\", v2=\"\")\n" +
		"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n";

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
		"public @MyAnn(v1=\"\"";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"@<CompleteOnType:MyAnn>\n";

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
		  @MyAnn void foo() {}
		}""";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @<CompleteOnType:MyAnn>
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
public void test0022(){
	String str =
		"""
		public class X {
		  @MyAnn int var;
		}""";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @<CompleteOnType:MyAnn>
		  int var;
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
public void test0023(){
	String str =
		"""
		public class X {
		  void foo(@MyAnn int i) {}
		}""";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @<CompleteOnType:MyAnn>
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
public void test0024_Diet(){
	String str =
		"""
		public class X {
		  void foo() {@MyAnn int i}
		}""";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
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
public void test0024_Method(){
	String str =
		"""
		public class X {
		  void foo() {@MyAnn int i}
		}""";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void foo() {
		    @<CompleteOnType:MyAnn>
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
public void test0025(){
	String str =
		"""
		@Annot(foo)
		public class X {
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		@Annot(<CompleteOnAttributeName:foo>)
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
public void test0026(){
	String str =
		"""
		public class X {
		  @Annot(foo)
		  void bar() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(<CompleteOnAttributeName:foo>)
		  public X() {
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
public void test0027(){
	String str =
		"""
		public class X {
		  @Annot(foo)
		  int var;
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(<CompleteOnAttributeName:foo>)
		  int var;
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
public void test0028_Diet(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(foo)
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0028_Method(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(foo)
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(<CompleteOnAttributeName:foo>)
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
public void test0029_Diet(){
	String str =
		"""
		public class X {
		  void bar(int var1, @Annot(foo) int var2) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void bar(int var1) {
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
public void test0029_Method(){
	String str =
		"""
		public class X {
		  void bar(int var1, @Annot(foo) int var2) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar(int var1) {
		    @Annot(<CompleteOnAttributeName:foo>)
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
public void test0030(){
	String str =
		"""
		public class X {
		  @Annot(foo)
		  X() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(<CompleteOnAttributeName:foo>)
		  X() {
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
public void test0031(){
	String str =
		"""
		@Annot(foo
		public class X {
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		@Annot(<CompleteOnAttributeName:foo>)
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
public void test0032(){
	String str =
		"""
		public class X {
		  @Annot(foo
		  void bar() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(<CompleteOnAttributeName:foo>)
		  public X() {
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
public void test0033(){
	String str =
		"""
		public class X {
		  @Annot(foo
		  int var;
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(<CompleteOnAttributeName:foo>)
		  int var;
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
public void test0034_Diet(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(foo
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0034_Method(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(foo
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(<CompleteOnAttributeName:foo>)
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
		  void bar(int var1, @Annot(foo int var2) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void bar(int var1) {
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
		  void bar(int var1, @Annot(foo int var2) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar(int var1) {
		    @Annot(<CompleteOnAttributeName:foo>)
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
public void test0036(){
	String str =
		"""
		public class X {
		  @Annot(foo
		  X() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(<CompleteOnAttributeName:foo>)
		  X() {
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
public void test0037(){
	String str =
		"""
		@Annot(foo=zzz)
		public class X {
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		@Annot(<CompleteOnAttributeName:foo>)
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
public void test0038(){
	String str =
		"""
		public class X {
		  @Annot(foo=zzz)
		  void bar() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(<CompleteOnAttributeName:foo>)
		  public X() {
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
public void test0039(){
	String str =
		"""
		public class X {
		  @Annot(foo=zzz)
		  int var;
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(<CompleteOnAttributeName:foo>)
		  int var;
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
public void test0040_Diet(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(foo=zzz)
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0040_Method(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(foo=zzz)
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(<CompleteOnAttributeName:foo>)
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
		  void bar(int var1, @Annot(foo=zzz) int var2) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void bar(int var1) {
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
		  void bar(int var1, @Annot(foo=zzz) int var2) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar(int var1) {
		    @Annot(<CompleteOnAttributeName:foo>)
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
public void test0042(){
	String str =
		"""
		public class X {
		  @Annot(foo=zzz)
		  X() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(<CompleteOnAttributeName:foo>)
		  X() {
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
public void test0043(){
	String str =
		"""
		@Annot(foo=zzz
		public class X {
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		@Annot(<CompleteOnAttributeName:foo>)
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
public void test0044(){
	String str =
		"""
		public class X {
		  @Annot(foo=zzz
		  void bar() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(<CompleteOnAttributeName:foo>)
		  public X() {
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
public void test0045(){
	String str =
		"""
		public class X {
		  @Annot(foo=zzz
		  int var;
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(<CompleteOnAttributeName:foo>)
		  int var;
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
public void test0046_Diet(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(foo=zzz
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0046_Method(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(foo=zzz
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(<CompleteOnAttributeName:foo>)
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
		  void bar(int var1, @Annot(foo=zzz int var2) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void bar(int var1) {
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
		  void bar(int var1, @Annot(foo=zzz int var2) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar(int var1) {
		    @Annot(<CompleteOnAttributeName:foo>)
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
public void test0048(){
	String str =
		"""
		public class X {
		  @Annot(foo=zzz
		  X() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(<CompleteOnAttributeName:foo>)
		  public X() {
		  }
		  zzz X() {
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
public void test0049(){
	String str =
		"""
		@Annot(yyy=zzz,foo)
		public class X {
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
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
public void test0050(){
	String str =
		"""
		public class X {
		  @Annot(yyy=zzz,foo)
		  void bar() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
		  public X() {
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
public void test0051(){
	String str =
		"""
		public class X {
		  @Annot(yyy=zzz,foo)
		  int var;
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
		  int var;
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
public void test0052_Diet(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(yyy=zzz,foo)
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0052_Method(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(yyy=zzz,foo)
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
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
		  void bar(int var1, @Annot(yyy=zzz,foo) int var2) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void bar(int var1) {
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
		  void bar(int var1, @Annot(yyy=zzz,foo) int var2) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar(int var1) {
		    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
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
public void test0054(){
	String str =
		"""
		public class X {
		  @Annot(yyy=zzz,foo)
		  X() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
		  X() {
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
public void test0055(){
	String str =
		"""
		@Annot(yyy=zzz,foo
		public class X {
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
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
public void test0056(){
	String str =
		"""
		public class X {
		  @Annot(yyy=zzz,foo
		  void bar() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
		  public X() {
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
public void test0057(){
	String str =
		"""
		public class X {
		  @Annot(yyy=zzz,foo
		  int var;
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
		  int var;
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
public void test0058_Diet(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(yyy=zzz,foo
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0058_Method(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(yyy=zzz,foo
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
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
public void test0059_Diet(){
	String str =
		"""
		public class X {
		  void bar(int var1, @Annot(yyy=zzz,foo int var2) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void bar(int var1) {
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
public void test0059_Method(){
	String str =
		"""
		public class X {
		  void bar(int var1, @Annot(yyy=zzz,foo int var2) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar(int var1) {
		    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
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
public void test0060(){
	String str =
		"""
		public class X {
		  @Annot(yyy=zzz,foo
		  X() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
		  X() {
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
		@Annot(yyy=zzz,foo=zzz)
		public class X {
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
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
public void test0062(){
	String str =
		"""
		public class X {
		  @Annot(yyy=zzz,foo=zzz)
		  void bar() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
		  public X() {
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
public void test0063(){
	String str =
		"""
		public class X {
		  @Annot(yyy=zzz,foo=zzz)
		  int var;
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
		  int var;
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
public void test0064_Diet(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(yyy=zzz,foo=zzz)
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0064_Method(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(yyy=zzz,foo=zzz)
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
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
public void test0065_Diet(){
	String str =
		"""
		public class X {
		  void bar(int var1, @Annot(yyy=zzz,foo=zzz) int var2) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void bar(int var1) {
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
public void test0065_Method(){
	String str =
		"""
		public class X {
		  void bar(int var1, @Annot(yyy=zzz,foo=zzz) int var2) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar(int var1) {
		    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
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
public void test0066(){
	String str =
		"""
		public class X {
		  @Annot(yyy=zzz,foo=zzz)
		  X() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
		  X() {
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
		@Annot(yyy=zzz,foo=zzz
		public class X {
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
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
public void test0068(){
	String str =
		"""
		public class X {
		  @Annot(yyy=zzz,foo=zzz
		  void bar() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
		  public X() {
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
public void test0069(){
	String str =
		"""
		public class X {
		  @Annot(yyy=zzz,foo=zzz
		  int var;
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
		  int var;
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
public void test0070_Diet(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(yyy=zzz,foo=zzz
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0070_Method(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(yyy=zzz,foo=zzz
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
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
public void test0071_Diet(){
	String str =
		"""
		public class X {
		  void bar(int var1, @Annot(yyy=zzz,foo=zzz int var2) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void bar(int var1) {
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
public void test0071_Method(){
	String str =
		"""
		public class X {
		  void bar(int var1, @Annot(yyy=zzz,foo=zzz int var2) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar(int var1) {
		    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
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
public void test0072(){
	String str =
		"""
		public class X {
		  @Annot(yyy=zzz,foo=zzz
		  X() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)
		  public X() {
		  }
		  zzz X() {
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
		@Annot(zzz=yyy,f)
		public class X {
		}""";


	String completeBehind = "f";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:f>";
	String expectedParentNodeToString = "@Annot(zzz = yyy,<CompleteOnAttributeName:f>)";
	String completionIdentifier = "f";
	String expectedReplacedSource = "f";
	String expectedUnitDisplayString =
		"""
		@Annot(zzz = yyy,<CompleteOnAttributeName:f>)
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
public void test0074(){
	String str =
		"""
		@Annot(zzz=foo)
		public class X {
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public @Annot(zzz = <CompleteOnName:foo>) class X {
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
public void test0075(){
	String str =
		"""
		@Annot(zzz= a && foo)
		public class X {
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public @Annot(zzz = (a && <CompleteOnName:foo>)) class X {
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
public void test0076(){
	String str =
		"""
		@Annot(zzz= {foo})
		public class X {
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		@Annot(zzz = {<CompleteOnName:foo>})
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
public void test0078(){
	String str =
		"""
		@Annot(zzz= {yyy, foo})
		public class X {
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		@Annot(zzz = {<CompleteOnName:foo>})
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
public void test0079(){
	String str =
		"""
		@Annot(zzz=foo
		public class X {
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		@Annot(zzz = <CompleteOnName:foo>)
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
public void test0080(){
	String str =
		"""
		@Annot(zzz= a && foo
		public class X {
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		@Annot(zzz = (a && <CompleteOnName:foo>))
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
public void test0081(){
	String str =
		"""
		@Annot(zzz= {yyy, foo}
		public class X {
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		@Annot(zzz = {<CompleteOnName:foo>})
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
public void test0082(){
	String str =
		"""
		@Annot(zzz= {yyy, foo
		public class X {
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		@Annot(zzz = {<CompleteOnName:foo>})
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
public void test0083(){
	String str =
		"""
		@Annot(zzz= a && (b || (foo && c)))
		public class X {
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(<CompleteOnName:foo> && c)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public @Annot(zzz = (a && (b || (<CompleteOnName:foo> && c)))) class X {
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
public void test0084(){
	String str =
		"""
		@Annot(zzz= a && (b || (foo
		public class X {
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "@Annot(zzz = <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		@Annot(zzz = <CompleteOnName:foo>)
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
public void test0085(){
	String str =
		"""
		public class X {
		  @Annot(zzz=foo)
		  void bar() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  @Annot(zzz = <CompleteOnName:foo>) void bar() {
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
		public class X {
		  @Annot(zzz= a && foo)
		  void bar() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  @Annot(zzz = (a && <CompleteOnName:foo>)) void bar() {
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
		  @Annot(zzz= {foo})
		  void bar() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(zzz = {<CompleteOnName:foo>})
		  public X() {
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
public void test0088(){
	String str =
		"""
		public class X {
		  @Annot(zzz= {yyy, foo})
		  void bar() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(zzz = {<CompleteOnName:foo>})
		  public X() {
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
public void test0089(){
	String str =
		"""
		public class X {
		  @Annot(zzz=foo
		  void bar() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(zzz = <CompleteOnName:foo>)
		  public X() {
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
public void test0090(){
	String str =
		"""
		public class X {
		  @Annot(zzz= a && foo
		  void bar() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(zzz = (a && <CompleteOnName:foo>))
		  public X() {
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
public void test0091(){
	String str =
		"""
		public class X {
		  @Annot(zzz= {yyy, foo}
		  void bar() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(zzz = {<CompleteOnName:foo>})
		  public X() {
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
public void test0092(){
	String str =
		"""
		public class X {
		  @Annot(zzz= {yyy, foo
		  void bar() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(zzz = {<CompleteOnName:foo>})
		  public X() {
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
public void test0093(){
	String str =
		"""
		public class X {
		  @Annot(zzz= a && (b || (foo && c)))
		  void bar() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(<CompleteOnName:foo> && c)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  @Annot(zzz = (a && (b || (<CompleteOnName:foo> && c)))) void bar() {
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
		  @Annot(zzz= a && (b || (foo
		  void bar() {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "@Annot(zzz = <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(zzz = <CompleteOnName:foo>)
		  public X() {
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
public void test0095(){
	String str =
		"""
		public class X {
		  @Annot(zzz=foo)
		  int bar;
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(zzz = <CompleteOnName:foo>) int bar;
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
public void test0096(){
	String str =
		"""
		public class X {
		  @Annot(zzz= a && foo)
		  int bar;
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(zzz = (a && <CompleteOnName:foo>)) int bar;
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
		  @Annot(zzz= {foo})
		  int bar;
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(zzz = {<CompleteOnName:foo>})
		  int bar;
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
public void test0098(){
	String str =
		"""
		public class X {
		  @Annot(zzz= {yyy, foo})
		  int bar;
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(zzz = {<CompleteOnName:foo>})
		  int bar;
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
public void test0099(){
	String str =
		"""
		public class X {
		  @Annot(zzz=foo
		  int bar;
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(zzz = <CompleteOnName:foo>)
		  int bar;
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
public void test0100(){
	String str =
		"""
		public class X {
		  @Annot(zzz= a && foo
		  int bar;
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(zzz = (a && <CompleteOnName:foo>))
		  int bar;
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
public void test0101(){
	String str =
		"""
		public class X {
		  @Annot(zzz= {yyy, foo}
		  int bar;
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(zzz = {<CompleteOnName:foo>})
		  int bar;
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
public void test0102(){
	String str =
		"""
		public class X {
		  @Annot(zzz= {yyy, foo
		  int bar;
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(zzz = {<CompleteOnName:foo>})
		  int bar;
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
public void test0103(){
	String str =
		"""
		public class X {
		  @Annot(zzz= a && (b || (foo && c)))
		  int bar;
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(<CompleteOnName:foo> && c)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(zzz = (a && (b || (<CompleteOnName:foo> && c)))) int bar;
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
public void test0104(){
	String str =
		"""
		public class X {
		  @Annot(zzz= a && (b || (foo
		  int bar;
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "@Annot(zzz = <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  @Annot(zzz = <CompleteOnName:foo>)
		  int bar;
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
public void test0105_Diet(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(zzz=foo)
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0105_Method(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(zzz=foo)
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(zzz = <CompleteOnName:foo>)
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
		  void bar() {
		    @Annot(zzz= a && foo)
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0106_Method(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(zzz= a && foo)
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(zzz = (a && <CompleteOnName:foo>))
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
		  void bar() {
		    @Annot(zzz= {foo})
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0107_Method(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(zzz= {foo})
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(zzz = {<CompleteOnName:foo>})
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
		  void bar() {
		    @Annot(zzz= {yyy, foo})
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0108_Method(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(zzz= {yyy, foo})
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(zzz = {<CompleteOnName:foo>})
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
		  void bar() {
		    @Annot(zzz=foo
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0109_Method(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(zzz=foo
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(zzz = <CompleteOnName:foo>)
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
		  void bar() {
		    int var;
		    @Annot(zzz= a && foo
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0110_Method(){
	String str =
		"""
		public class X {
		  void bar() {
		    int var;
		    @Annot(zzz= a && foo
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    int var;
		    @Annot(zzz = (a && <CompleteOnName:foo>))
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
		  void bar() {
		    @Annot(zzz= {yyy, foo}
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0111_Method(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(zzz= {yyy, foo}
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(zzz = {<CompleteOnName:foo>})
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
		  void bar() {
		    @Annot(zzz= {yyy, foo
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0112_Method(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(zzz= {yyy, foo
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(zzz = {<CompleteOnName:foo>})
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
		  void bar() {
		    @Annot(zzz= a && (b || (foo && c)))
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0113_Method(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(zzz= a && (b || (foo && c)))
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "@Annot(zzz = <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(zzz = <CompleteOnName:foo>)
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
		  void bar() {
		    @Annot(zzz= a && (b || (foo
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0114_Method(){
	String str =
		"""
		public class X {
		  void bar() {
		    @Annot(zzz= a && (b || (foo
		    int var;
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "@Annot(zzz = <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(zzz = <CompleteOnName:foo>)
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
public void test0115(){
	String str =
		"""
		public class X {
		  void bar(@Annot(zzz=foo) int var) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void bar(@Annot(zzz = <CompleteOnName:foo>) int var) {
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
public void test0116(){
	String str =
		"""
		public class X {
		  void bar(@Annot(zzz= a && foo) int var) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void bar(@Annot(zzz = (a && <CompleteOnName:foo>)) int var) {
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
public void test0117(){
	String str =
		"""
		public class X {
		  void bar(@Annot(zzz= {foo}) int var) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0118(){
	String str =
		"""
		public class X {
		  void bar(@Annot(zzz= {yyy, foo}) int var) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0119_Diet(){
	String str =
		"""
		public class X {
		  void bar(@Annot(zzz=foo int var) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0119_Method(){
	String str =
		"""
		public class X {
		  void bar(@Annot(zzz=foo int var) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "zzz = <CompleteOnName:foo>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(zzz = <CompleteOnName:foo>)
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
		  void bar(@Annot(zzz= a && foo int var) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0120_Method(){
	String str =
		"""
		public class X {
		  void bar(@Annot(zzz= a && foo int var) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(a && <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
			"""
		public class X {
		  public X() {
		  }
		  void bar() {
		    @Annot(zzz = (a && <CompleteOnName:foo>))
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
public void test0121(){
	String str =
		"""
		public class X {
		  void bar(@Annot(zzz= {yyy, foo} int var) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0122(){
	String str =
		"""
		public class X {
		  void bar(@Annot(zzz= {yyy, foo int var) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@Annot(zzz)>";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
public void test0123(){
	String str =
		"""
		public class X {
		  void bar(@Annot(zzz= a && (b || (foo && c))) int var) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "(<CompleteOnName:foo> && c)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
		  }
		  void bar(@Annot(zzz = (a && (b || (<CompleteOnName:foo> && c)))) int var) {
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
public void test0124(){
	String str =
		"""
		public class X {
		  void bar(@Annot(zzz= a && (b || (foo int var) {
		  }
		}""";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:foo>";
	String expectedParentNodeToString = "@Annot(zzz = <CompleteOnName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"""
		public class X {
		  public X() {
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
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=148742
public void test0125(){
	String str =
		"""
		public interface X {
		  public void test(@TestAnnotation int testParam);
		}""";


	String completeBehind = "@TestAnnotation";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:TestAnnotation>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "TestAnnotation";
	String expectedReplacedSource = "TestAnnotation";
	String expectedUnitDisplayString =
		"""
		public interface X {
		  @<CompleteOnType:TestAnnotation>
		  public void test() {
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
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=148742
public void test0126(){
	String str =
		"""
		public abstract class X {
		  public abstract void test(@TestAnnotation int testParam);
		}""";


	String completeBehind = "@TestAnnotation";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:TestAnnotation>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "TestAnnotation";
	String expectedReplacedSource = "TestAnnotation";
	String expectedUnitDisplayString =
		"""
		public abstract class X {
		  @<CompleteOnType:TestAnnotation>
		  public X() {
		  }
		  public abstract void test();
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
public void test0127(){
	String str =
		"""
		public class Test {
		  public static final int zzint = 0;
		  @ZZAnnotation({ZZ})
		  void bar() {
		  }
		}""";


	String completeBehind = "{ZZ";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:ZZ>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@ZZAnnotation(value)>";
	String completionIdentifier = "ZZ";
	String expectedReplacedSource = "ZZ";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  @ZZAnnotation(value = {<CompleteOnName:ZZ>})
		  public static final int zzint;
		  public Test() {
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
public void test0128(){
	String str =
		"""
		public class Test {
		  public static final int zzint = 0;
		  @ZZAnnotation(value={ZZ})
		  void bar() {
		  }
		}""";


	String completeBehind = "{ZZ";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:ZZ>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@ZZAnnotation(value)>";
	String completionIdentifier = "ZZ";
	String expectedReplacedSource = "ZZ";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  @ZZAnnotation(value = {<CompleteOnName:ZZ>})
		  public static final int zzint;
		  public Test() {
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
public void test0129(){
	String str =
		"""
		public class Test {
		  public static final int zzint = 0;
		  @ZZAnnotation({ZZ
		}""";


	String completeBehind = "{ZZ";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnName:ZZ>";
	String expectedParentNodeToString = "<AssistNodeParentAnnotationArrayInitializer:@ZZAnnotation(value)>";
	String completionIdentifier = "ZZ";
	String expectedReplacedSource = "ZZ";
	String expectedUnitDisplayString =
		"""
		public class Test {
		  @ZZAnnotation(value = {<CompleteOnName:ZZ>})
		  public static final int zzint;
		  public Test() {
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
}
