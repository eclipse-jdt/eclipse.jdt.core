/*******************************************************************************
 * Copyright (c) 2020 Julian Honnen.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Julian Honnen - initial version
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Hashtable;
import java.util.function.Predicate;
import junit.framework.Test;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

public class SubwordCompletionTests extends AbstractJavaModelCompletionTests {

private Hashtable<String, String> defaultOptions;

public static Test suite() {
	return buildModelTestSuite(SubwordCompletionTests.class, BYTECODE_DECLARATION_ORDER);
}
public SubwordCompletionTests(String name) {
	super(name);
}
public void setUpSuite() throws Exception {
	if (COMPLETION_PROJECT == null)  {
		COMPLETION_PROJECT = setUpJavaProject("Completion", "1.8", true);
	} else {
		setUpProjectCompliance(COMPLETION_PROJECT, "1.8", true);
	}
	super.setUpSuite();
	Hashtable<String, String> options = new Hashtable<>(this.oldOptions);
	options.put(JavaCore.CODEASSIST_SUBWORD_MATCH, JavaCore.ENABLED);
	this.defaultOptions = options;
	JavaCore.setOptions(options);
}

@Override
protected Hashtable<String, String> getDefaultJavaCoreOptions() {
	return this.defaultOptions;
}

private CompletionTestsRequestor2 createFilteredRequestor() {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	Predicate<CompletionProposal> javaTypeRef = p -> p.getKind() == CompletionProposal.TYPE_REF && new String(p.getSignature()).startsWith("Ljava.");
	requestor.setProposalFilter(javaTypeRef.negate());
	return requestor;
}
public void testQualifiedNonStaticMethod() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  public void addListener() {}\n" +
		"  public void addXListener() {}\n" +
		"  public void addYListener() {}\n" +
		"  void foo() {\n" +
		"    this.addlistener\n" +
		"  }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "this.addlistener";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"addXListener[METHOD_REF]{addXListener(), Ltest.Test;, ()V, addXListener, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC + R_NON_RESTRICTED + R_SUBWORD) + "}\n" +
			"addYListener[METHOD_REF]{addYListener(), Ltest.Test;, ()V, addYListener, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC + R_NON_RESTRICTED + R_SUBWORD) + "}\n" +
			"addListener[METHOD_REF]{addListener(), Ltest.Test;, ()V, addListener, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC + R_NON_RESTRICTED + R_EXACT_NAME) + "}",
			requestor.getResults());
}

public void testUnqualifiedNonStaticMethod() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  public void addListener() {}\n" +
		"  public void addXListener() {}\n" +
		"  public void addYListener() {}\n" +
		"  void foo() {\n" +
		"    addlistener\n" +
		"  }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "addlistener";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"addXListener[METHOD_REF]{addXListener(), Ltest.Test;, ()V, addXListener, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}\n" +
			"addYListener[METHOD_REF]{addYListener(), Ltest.Test;, ()V, addYListener, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}\n" +
			"addListener[METHOD_REF]{addListener(), Ltest.Test;, ()V, addListener, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_NAME) + "}",
			requestor.getResults());
}
public void testQualifiedStaticMethod() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  public static void addListener() {}\n" +
		"  public void addXListener() {}\n" +
		"  public static void addYListener() {}\n" +
		"  void foo() {\n" +
		"    Test.addlistener\n" +
		"  }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "Test.addlistener";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"addYListener[METHOD_REF]{addYListener(), Ltest.Test;, ()V, addYListener, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING +  R_NON_RESTRICTED +  R_NON_INHERITED +R_SUBWORD) + "}\n" +
			"addListener[METHOD_REF]{addListener(), Ltest.Test;, ()V, addListener, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING +  R_NON_RESTRICTED +  R_NON_INHERITED +R_EXACT_NAME) + "}",
			requestor.getResults());
}
public void testUnqualifiedStaticMethod() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  public static void addListener() {}\n" +
		"  public void addXListener() {}\n" +
		"  public static void addYListener() {}\n" +
		"  void foo() {\n" +
		"    addlistener\n" +
		"  }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "addlistener";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"addXListener[METHOD_REF]{addXListener(), Ltest.Test;, ()V, addXListener, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}\n" +
			"addYListener[METHOD_REF]{addYListener(), Ltest.Test;, ()V, addYListener, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED +  R_NON_RESTRICTED +  R_SUBWORD) + "}\n" +
			"addListener[METHOD_REF]{addListener(), Ltest.Test;, ()V, addListener, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED +  R_NON_RESTRICTED +  R_EXACT_NAME) + "}",
			requestor.getResults());
}
public void testQualifiedNonStaticField() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"  int fItems;\n" +
		"  int fOtherItems;\n" +
		"  long fItemsCount;\n" +
		"  void foo() {\n" +
		"    this.fitem\n" +
		"  }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "this.fitem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"fOtherItems[FIELD_REF]{fOtherItems, Ltest.Test;, I, fOtherItems, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC + R_NON_RESTRICTED + R_SUBWORD) + "}\n" +
			"fItems[FIELD_REF]{fItems, Ltest.Test;, I, fItems, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC + R_NON_RESTRICTED) + "}\n" +
			"fItemsCount[FIELD_REF]{fItemsCount, Ltest.Test;, J, fItemsCount, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}

public void testUnqualifiedNonStaticField() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  int fItems;\n" +
			"  int fOtherItems;\n" +
			"  long fItemsCount;\n" +
			"  void foo() {\n" +
			"    fitem\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "fitem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"fOtherItems[FIELD_REF]{fOtherItems, Ltest.Test;, I, fOtherItems, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}\n" +
			"fItems[FIELD_REF]{fItems, Ltest.Test;, I, fItems, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"fItemsCount[FIELD_REF]{fItemsCount, Ltest.Test;, J, fItemsCount, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void testQualifiedStaticField() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static int sItems;\n" +
			"  static int sOtherItems;\n" +
			"  long fSomeItemsCount;\n" +
			"  void foo() {\n" +
			"    Test.sitem\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "Test.sitem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"sOtherItems[FIELD_REF]{sOtherItems, Ltest.Test;, I, sOtherItems, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_NON_INHERITED + R_SUBWORD) + "}\n" +
			"sItems[FIELD_REF]{sItems, Ltest.Test;, I, sItems, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_NON_INHERITED) + "}",
			requestor.getResults());
}
public void testUnqualifiedStaticField() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static int sItems;\n" +
			"  static int sOtherItems;\n" +
			"  long fSomeItemsCount;\n" +
			"  void foo() {\n" +
			"    sitem\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "sitem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"fSomeItemsCount[FIELD_REF]{fSomeItemsCount, Ltest.Test;, J, fSomeItemsCount, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}\n" +
			"sOtherItems[FIELD_REF]{sOtherItems, Ltest.Test;, I, sOtherItems, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}\n" +
			"sItems[FIELD_REF]{sItems, Ltest.Test;, I, sItems, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void testLocalVariable() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static int items;\n" +
			"  int otherTemporaryItems;\n" +
			"  static long itemsCount;\n" +
			"  void foo() {\n" +
			"    int temporaryItem = 0;\n" +
			"    tempitem\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "tempitem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"otherTemporaryItems[FIELD_REF]{otherTemporaryItems, Ltest.Test;, I, otherTemporaryItems, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}\n" +
			"temporaryItem[LOCAL_VARIABLE_REF]{temporaryItem, null, I, temporaryItem, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testMethodParamVariable() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static int items;\n" +
			"  int otherTemporaryItems;\n" +
			"  static long itemsCount;\n" +
			"  void foo(int temporaryItems) {\n" +
			"    tempitems\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "item";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"otherTemporaryItems[FIELD_REF]{otherTemporaryItems, Ltest.Test;, I, otherTemporaryItems, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}\n" +
			"temporaryItems[LOCAL_VARIABLE_REF]{temporaryItems, null, I, temporaryItems, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testClassTypeInstantiation() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/test/Foobar.java",
			"package test;\n"+
			"interface Map {}\n" +
			"class LinkedHashMap implements Map {}\n" +
			"class SpecificLinkedHashMap extends LinkedHashMap {}\n" +
			"interface Foobaz {}\n" +
			"class SpecificFooBaz implements Foobaz {}\n");
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  {\n" +
			"    Map f = new linkedmap\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new linkedmap";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"LinkedHashMap[TYPE_REF]{LinkedHashMap, test, Ltest.LinkedHashMap;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}\n" +
			"SpecificLinkedHashMap[TYPE_REF]{SpecificLinkedHashMap, test, Ltest.SpecificLinkedHashMap;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testClassTypeFieldDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/test/Foobar.java",
			"package test;\n"+
			"interface Map {}\n" +
			"class LinkedHashMap implements Map {}\n" +
			"class SpecificLinkedHashMap extends LinkedHashMap {}\n" +
			"interface Foobaz {}\n" +
			"class SpecificFooBaz implements Foobaz {}\n");
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  public linkedmap\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "public linkedmap";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"LinkedHashMap[TYPE_REF]{LinkedHashMap, test, Ltest.LinkedHashMap;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}\n" +
			"SpecificLinkedHashMap[TYPE_REF]{SpecificLinkedHashMap, test, Ltest.SpecificLinkedHashMap;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testClassTypeParamDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/test/Foobar.java",
			"package test;\n"+
			"interface Map {}\n" +
			"class LinkedHashMap implements Map {}\n" +
			"class SpecificLinkedHashMap extends LinkedHashMap {}\n" +
			"interface Foobaz {}\n" +
			"class SpecificFooBaz implements Foobaz {}\n");
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  void setFoo(linkedmap\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "void setFoo(linkedmap";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"LinkedHashMap[TYPE_REF]{LinkedHashMap, test, Ltest.LinkedHashMap;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}\n" +
			"SpecificLinkedHashMap[TYPE_REF]{SpecificLinkedHashMap, test, Ltest.SpecificLinkedHashMap;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testClassTypeLocalVarDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/test/Foobar.java",
			"package test;\n"+
			"interface Map {}\n" +
			"class LinkedHashMap implements Map {}\n" +
			"class SpecificLinkedHashMap extends LinkedHashMap {}\n" +
			"interface Foobaz {}\n" +
			"class SpecificFooBaz implements Foobaz {}\n");
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  void foo() {\n" +
			"    final linkedmap" +
			"  }" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "final linkedmap";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"LinkedHashMap[TYPE_REF]{LinkedHashMap, test, Ltest.LinkedHashMap;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}\n" +
			"SpecificLinkedHashMap[TYPE_REF]{SpecificLinkedHashMap, test, Ltest.SpecificLinkedHashMap;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testClassTypeThrowsDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/test/Foobar.java",
			"package test;\n"+
			"interface Foobar {}\n" +
			"class SpecificFooBar implements Foobar extends Exception {}\n" +
			"class EvenMoreSpecificFooBar extends SpecificFooBar {}\n" +
			"interface Foobaz {}\n" +
			"class SpecificFooBaz implements Foobaz extends Exception {}\n");
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  void foo() throws fbar {\n" +
			"  }" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "void foo() throws fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"EvenMoreSpecificFooBar[TYPE_REF]{EvenMoreSpecificFooBar, test, Ltest.EvenMoreSpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}\n" +
			"SpecificFooBar[TYPE_REF]{SpecificFooBar, test, Ltest.SpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testClassTypeExtends() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/test/Foobar.java",
			"package test;\n"+
			"interface Foobar {}\n" +
			"class SpecificFooBar implements Foobar {}\n" +
			"class EvenMoreSpecificFooBar extends SpecificFooBar {}\n" +
			"interface Foobaz {}\n" +
			"class SpecificFooBaz implements Foobaz {}\n");
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test extends fbar {\n" +
			"  }" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "public class Test extends fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"EvenMoreSpecificFooBar[TYPE_REF]{EvenMoreSpecificFooBar, test, Ltest.EvenMoreSpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_CLASS + R_SUBWORD) + "}\n" +
			"SpecificFooBar[TYPE_REF]{SpecificFooBar, test, Ltest.SpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_CLASS + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testClassTypeImplements() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/test/Foobar.java",
			"package test;\n"+
			"interface FooBar {}\n" +
			"interface FooBarExtension extends FooBar {}\n" +
			"class SpecificFooBar implements FooBar {}\n" +
			"class EvenMoreSpecificFooBar extends SpecificFooBar {}\n" +
			"interface Foobaz {}\n" +
			"class SpecificFooBaz implements Foobaz {}\n");
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test implements fbar {\n" +
			"  }" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "public class Test implements fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"FooBar[TYPE_REF]{FooBar, test, Ltest.FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_INTERFACE + R_SUBWORD) + "}\n" +
			"FooBarExtension[TYPE_REF]{FooBarExtension, test, Ltest.FooBarExtension;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_INTERFACE + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testInnerClassTypeInstantiation() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  class FooBar {}\n" +
			"  {\n" +
			"    Test t = new Test();\n" +
			"    Object f = t.new fbar\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "t.new fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testInnerClassTypeFieldDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  class FooBar {}\n" +
			"  public fbar\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "public fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testInnerClassTypeParamDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  class FooBar {}\n" +
			"  void foo(fbar\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "void foo(fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testInnerClassTypeLocalVarDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  class FooBar {}\n" +
			"  {\n" +
			"    final fbar\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "final fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testInnerClassTypeThrowsDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  class FooBar extends Exception {}\n" +
			"  void foo() throws fbar" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "void foo() throws fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testInnerClassTypeExtends() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  class FooBar {}\n" +
			"  class SpecificFooBar extends fbar\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "SpecificFooBar extends fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testInnerClassTypeImplements() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  interface FooBar {}\n" +
			"  class SpecificFooBar implements fbar\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "class SpecificFooBar implements fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeInstantiation() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static class FooBar {}\n" +
			"  {\n" +
			"    Object f = new fbar\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeFieldDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static class FooBar {}\n" +
			"  public fbar\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "public fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeParamDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static class FooBar {}\n" +
			"  void foo(fbar\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "void foo(fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeLocalVarDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static class FooBar {}\n" +
			"  {\n" +
			"    final fbar\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "final fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeThrowsDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static class FooBar extends Exception {}\n" +
			"  void foo() throws fbar" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "void foo() throws fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeExtends() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static class FooBar {}\n" +
			"  class SpecificFooBar extends fbar\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "SpecificFooBar extends fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeImplements() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  static interface FooBar {}\n" +
			"  class SpecificFooBar implements fbar\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "class SpecificFooBar implements fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testLocalClassTypeInstantiation() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  void foo() {\n" +
			"    class FooBar {}\n" +
			"    Object f = new fbar\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"FooBar[TYPE_REF]{FooBar, test, LFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testLocalClassTypeLocalVarDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  void foo() {\n" +
			"    class FooBar {}\n" +
			"    final fbar\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "final fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"FooBar[TYPE_REF]{FooBar, test, LFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testLocalClassTypeExtends() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  void foo() {\n" +
			"    class FooBar {}\n" +
			"    class SpecificFooBar extends fbar\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "class SpecificFooBar extends fbar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"FooBar[TYPE_REF]{FooBar, test, LFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBWORD) + "}",
			requestor.getResults());
}
public void testDontPreventInsertionOfExactMatch() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"package test;"+
			"public class Test {\n" +
			"  void foo(java.util.Map<String, String> map) {\n" +
			"    map.put(\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "map.put(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED ) +  "}\n" +
			"put[METHOD_REF]{, Ljava.util.Map<Ljava.lang.String;Ljava.lang.String;>;, (Ljava.lang.String;Ljava.lang.String;)Ljava.lang.String;, put, (key, value), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_NAME + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED ) + "}",
			requestor.getResults());
}
}
