/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
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
}
