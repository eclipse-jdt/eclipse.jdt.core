/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;


import java.util.Map;

import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;



public class AnnotationCompletionParserTest extends AbstractCompletionTest {
public AnnotationCompletionParserTest(String testName) {
	super(testName);
}

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
		"@<CompleteOnType:MyAnn>\n" + 
		"class X {\n" + 
		"  X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"@<CompleteOnType:MyAnn>\n" + 
		"interface X {\n" + 
		"}\n";

	checkDietParse(
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
		"@<CompleteOnType:MyAnn>\n" + 
		"enum X {\n" + 
		"  X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"@<CompleteOnType:MyAnn>\n" + 
		"@interface X {\n" + 
		"}\n";

	checkDietParse(
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
		"@<CompleteOnType:MyAnn>\n" + 
		"class X {\n" + 
		"  X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"@<CompleteOnType:MyAnn>\n" + 
		"interface X {\n" + 
		"}\n";

	checkDietParse(
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
		"@<CompleteOnType:MyAnn>\n" + 
		"enum X {\n" + 
		"  X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"@<CompleteOnType:MyAnn>\n" + 
		"@interface X {\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  public @MyAnn class Y {\n" +
		"  }\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @<CompleteOnType:MyAnn>\n" + 
		"  class Y {\n" + 
		"    Y() {\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" + 
		"  @<CompleteOnType:MyAnn>\n" + 
		"  class Y {\n" + 
		"    Y() {\n" + 
		"    }\n" + 
		"  }\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  public @MyAnn\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @<CompleteOnType:MyAnn>\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  public void foo() {\n" +
		"    @MyAnn class Y {\n" +
		"    }\n" +
		"  }\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "MyAnn";
	expectedReplacedSource = "MyAnn";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    @<CompleteOnType:MyAnn>\n" + 
		"  }\n" + 
		"}\n";

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
		"@<CompleteOnType:MyAnn>\n" + 
		"class X {\n" + 
		"  X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"@<CompleteOnType:MyAnn>\n" + 
		"class X {\n" + 
		"  X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"@<CompleteOnType:MyAnn>\n" + 
		"class X {\n" + 
		"  X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"@<CompleteOnType:MyAnn>\n" + 
		"class X {\n" + 
		"  X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  @MyAnn void foo() {}\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @<CompleteOnType:MyAnn>\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  @MyAnn int var;\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @<CompleteOnType:MyAnn>\n" + 
		"  int var;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  void foo(@MyAnn int i) {}\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "MyAnn";
	String expectedReplacedSource = "MyAnn";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "MyAnn";
	expectedReplacedSource = "MyAnn";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    @<CompleteOnType:MyAnn>\n" + 
		"  }\n" + 
		"}\n";

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
public void test0024(){
	String str =
		"public class X {\n" +
		"  void foo() {@MyAnn int i}\n" +
		"}";


	String completeBehind = "MyAnn";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "@<CompleteOnType:MyAnn>";
	expectedParentNodeToString = "<NONE>";
	completionIdentifier = "MyAnn";
	expectedReplacedSource = "MyAnn";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void foo() {\n" + 
		"    @<CompleteOnType:MyAnn>\n" + 
		"  }\n" + 
		"}\n";

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
		"@Annot(foo)\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(<CompleteOnAttributeName:foo>)\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  @Annot(foo)\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  @Annot(foo)\n" +
		"  int var;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  int var;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(foo)\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	completionIdentifier = "foo";
	expectedReplacedSource = "foo";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"    @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  }\n" + 
		"}\n";

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
public void test0029(){
	String str =
		"public class X {\n" +
		"  void bar(int var1, @Annot(foo) int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar(int var1) {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	completionIdentifier = "foo";
	expectedReplacedSource = "foo";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar(int var1) {\n" + 
		"    @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  }\n" + 
		"}\n";

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
		"public class X {\n" +
		"  @Annot(foo)\n" +
		"  X() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"@Annot(foo\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(<CompleteOnAttributeName:foo>)\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  @Annot(foo\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  @Annot(foo\n" +
		"  int var;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  int var;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0034(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(foo\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	completionIdentifier = "foo";
	expectedReplacedSource = "foo";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"    @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  }\n" + 
		"}\n";

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
public void test0035(){
	String str =
		"public class X {\n" +
		"  void bar(int var1, @Annot(foo int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar(int var1) {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	completionIdentifier = "foo";
	expectedReplacedSource = "foo";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar(int var1) {\n" + 
		"    @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  }\n" + 
		"}\n";

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
		"public class X {\n" +
		"  @Annot(foo\n" +
		"  X() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"@Annot(foo=zzz)\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(<CompleteOnAttributeName:foo>)\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  @Annot(foo=zzz)\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  @Annot(foo=zzz)\n" +
		"  int var;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  int var;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0040(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(foo=zzz)\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	completionIdentifier = "foo";
	expectedReplacedSource = "foo";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"    @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  }\n" + 
		"}\n";

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
public void test0041(){
	String str =
		"public class X {\n" +
		"  void bar(int var1, @Annot(foo=zzz) int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar(int var1) {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	completionIdentifier = "foo";
	expectedReplacedSource = "foo";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar(int var1) {\n" + 
		"    @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  }\n" + 
		"}\n";

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
		"public class X {\n" +
		"  @Annot(foo=zzz)\n" +
		"  X() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"@Annot(foo=zzz\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(<CompleteOnAttributeName:foo>)\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  @Annot(foo=zzz\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  @Annot(foo=zzz\n" +
		"  int var;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  int var;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0046(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(foo=zzz\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	completionIdentifier = "foo";
	expectedReplacedSource = "foo";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"    @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  }\n" + 
		"}\n";

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
public void test0047(){
	String str =
		"public class X {\n" +
		"  void bar(int var1, @Annot(foo=zzz int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar(int var1) {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	completionIdentifier = "foo";
	expectedReplacedSource = "foo";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar(int var1) {\n" + 
		"    @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  }\n" + 
		"}\n";

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
		"public class X {\n" +
		"  @Annot(foo=zzz\n" +
		"  X() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(<CompleteOnAttributeName:foo>)\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  zzz X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"@Annot(yyy=zzz,foo)\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo)\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo)\n" +
		"  int var;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  int var;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0052(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(yyy=zzz,foo)\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	completionIdentifier = "foo";
	expectedReplacedSource = "foo";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  }\n" + 
		"}\n";

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
public void test0053(){
	String str =
		"public class X {\n" +
		"  void bar(int var1, @Annot(yyy=zzz,foo) int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar(int var1) {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	completionIdentifier = "foo";
	expectedReplacedSource = "foo";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar(int var1) {\n" + 
		"    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  }\n" + 
		"}\n";

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
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo)\n" +
		"  X() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"@Annot(yyy=zzz,foo\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo\n" +
		"  int var;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  int var;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
}
public void test0058(){
	String str =
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(yyy=zzz,foo\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	completionIdentifier = "foo";
	expectedReplacedSource = "foo";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  }\n" + 
		"}\n";

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
		"public class X {\n" +
		"  void bar(int var1, @Annot(yyy=zzz,foo int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar(int var1) {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	completionIdentifier = "foo";
	expectedReplacedSource = "foo";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar(int var1) {\n" + 
		"    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  }\n" + 
		"}\n";

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
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo\n" +
		"  X() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"@Annot(yyy=zzz,foo=zzz)\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo=zzz)\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo=zzz)\n" +
		"  int var;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  int var;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(yyy=zzz,foo=zzz)\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	completionIdentifier = "foo";
	expectedReplacedSource = "foo";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  }\n" + 
		"}\n";

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
public void test0065(){
	String str =
		"public class X {\n" +
		"  void bar(int var1, @Annot(yyy=zzz,foo=zzz) int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar(int var1) {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	completionIdentifier = "foo";
	expectedReplacedSource = "foo";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar(int var1) {\n" + 
		"    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  }\n" + 
		"}\n";

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
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo=zzz)\n" +
		"  X() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"@Annot(yyy=zzz,foo=zzz\n" +
		"public class X {\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo=zzz\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo=zzz\n" +
		"  int var;\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  int var;\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
		"public class X {\n" +
		"  void bar() {\n" +
		"    @Annot(yyy=zzz,foo=zzz\n" +
		"    int var;\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<NONE>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "<NONE>";
	String expectedReplacedSource = "<NONE>";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	completionIdentifier = "foo";
	expectedReplacedSource = "foo";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar() {\n" + 
		"    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  }\n" + 
		"}\n";

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
public void test0071(){
	String str =
		"public class X {\n" +
		"  void bar(int var1, @Annot(yyy=zzz,foo=zzz int var2) {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar(int var1) {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
			str.toCharArray(),
			cursorLocation,
			expectedCompletionNodeToString,
			expectedParentNodeToString,
			expectedUnitDisplayString,
			completionIdentifier,
			expectedReplacedSource,
	"diet ast");
	
	expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	completionIdentifier = "foo";
	expectedReplacedSource = "foo";
	expectedUnitDisplayString =
		"public class X {\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  void bar(int var1) {\n" + 
		"    @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  }\n" + 
		"}\n";

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
		"public class X {\n" +
		"  @Annot(yyy=zzz,foo=zzz\n" +
		"  X() {\n" +
		"  }\n" +
		"}";


	String completeBehind = "foo";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnAttributeName:foo>";
	String expectedParentNodeToString = "@Annot(yyy = zzz,<CompleteOnAttributeName:foo>)";
	String completionIdentifier = "foo";
	String expectedReplacedSource = "foo";
	String expectedUnitDisplayString =
		"public class X {\n" + 
		"  @Annot(yyy = zzz,<CompleteOnAttributeName:foo>)\n" + 
		"  public X() {\n" + 
		"  }\n" + 
		"  zzz X() {\n" + 
		"  }\n" + 
		"}\n";

	checkDietParse(
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
