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



public class EnumCompletionParserTest extends AbstractCompletionTest {
public EnumCompletionParserTest(String testName) {
	super(testName);
}

protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);	
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);	
	return options;
}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=83321
 */
public void test0001(){
	String str =
		"public class Completion {\n" + 
		"	/*here*/\n" + 
		"}\n" + 
		"enum Natural {\n" + 
		"	ONE;\n" + 
		"}\n";

	String completeBehind = "/*here*/";
	int cursorLocation = str.indexOf("/*here*/") + completeBehind.length() - 1;
	String expectedCompletionNodeToString = "<CompleteOnType:>";
	String expectedParentNodeToString = "<NONE>";
	String completionIdentifier = "";
	String expectedReplacedSource = "";
	String expectedUnitDisplayString =
		"public class Completion {\n" + 
		"  <CompleteOnType:>;\n" + 
		"  public Completion() {\n" + 
		"  }\n" + 
		"}\n" + 
		"enum Natural {\n" + 
		"  ONE(),\n" + 
		"  Natural() {\n" + 
		"  }\n" + 
		"  <clinit>() {\n" + 
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
