/*******************************************************************************
 * Copyright (c) 2015, 2020 Gábor Kövesdán and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gábor Kövesdán - initial version
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.function.Predicate;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;

import junit.framework.Test;

public class SubstringCompletionTests extends AbstractJavaModelCompletionTests {

public static Test suite() {
	return buildModelTestSuite(SubstringCompletionTests.class, BYTECODE_DECLARATION_ORDER);
}
public SubstringCompletionTests(String name) {
	super(name);
}
@Override
public void setUpSuite() throws Exception {
	if (COMPLETION_PROJECT == null)  {
		COMPLETION_PROJECT = setUpJavaProject("Completion", "1.8", true);
	} else {
		setUpProjectCompliance(COMPLETION_PROJECT, "1.8", true);
	}
	super.setUpSuite();
	System.setProperty(AssistOptions.PROPERTY_SubstringMatch, "true");
}
@Override
public void tearDownSuite() throws Exception {
	if (COMPLETION_SUITES == null) {
		deleteProject("Completion");
	} else {
		COMPLETION_SUITES.remove(getClass());
		if (COMPLETION_SUITES.size() == 0) {
			deleteProject("Completion");
			COMPLETION_SUITES = null;
		}
	}
	if (COMPLETION_SUITES == null) {
		COMPLETION_PROJECT = null;
	}
	super.tearDownSuite();
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
		"""
			package test;\
			public class Test {
			  public Object bar1() {}
			  public Zork Bar2() {}
			  public void removeBar() {}
			  void foo() {
			    this.bar
			  }
			}
			""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "this.bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"removeBar[METHOD_REF]{removeBar(), Ltest.Test;, ()V, removeBar, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"Bar2[METHOD_REF]{Bar2(), Ltest.Test;, ()LZork;, Bar2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC + R_NON_RESTRICTED) + "}\n" +
			"bar1[METHOD_REF]{bar1(), Ltest.Test;, ()Ljava.lang.Object;, bar1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}

public void testUnqualifiedNonStaticMethod() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"""
			package test;\
			public class Test {
			  public Object bar1() {}
			  public Zork Bar2() {}
			  public void removeBar() {}
			  void foo() {
			    bar
			  }
			}
			""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"removeBar[METHOD_REF]{removeBar(), Ltest.Test;, ()V, removeBar, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"Bar2[METHOD_REF]{Bar2(), Ltest.Test;, ()LZork;, Bar2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"bar1[METHOD_REF]{bar1(), Ltest.Test;, ()Ljava.lang.Object;, bar1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void testQualifiedStaticMethod() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"""
			package test;\
			public class Test {
			  public static Object bar1() {}
			  public Zork Bar2() {}
			  public static void removeBar() {}
			  void foo() {
			    Test.bar
			  }
			}
			""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "Test.bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"removeBar[METHOD_REF]{removeBar(), Ltest.Test;, ()V, removeBar, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_NON_INHERITED + R_SUBSTRING) + "}\n" +
			"bar1[METHOD_REF]{bar1(), Ltest.Test;, ()Ljava.lang.Object;, bar1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_NON_INHERITED) + "}",
			requestor.getResults());
}
public void testUnqualifiedStaticMethod() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"""
			package test;\
			public class Test {
			  public static Object bar1() {}
			  public Zork Bar2() {}
			  public static void removeBar() {}
			  void foo() {
			    Bar
			  }
			}
			""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "Bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"removeBar[METHOD_REF]{removeBar(), Ltest.Test;, ()V, removeBar, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"bar1[METHOD_REF]{bar1(), Ltest.Test;, ()Ljava.lang.Object;, bar1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"Bar2[METHOD_REF]{Bar2(), Ltest.Test;, ()LZork;, Bar2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void testQualifiedNonStaticField() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"""
			package test;\
			public class Test {
			  int items;
			  int otherItems;
			  long itemsCount;
			  void foo() {
			    this.item
			  }
			}
			""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "this.item";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"otherItems[FIELD_REF]{otherItems, Ltest.Test;, I, otherItems, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"items[FIELD_REF]{items, Ltest.Test;, I, items, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}\n" +
			"itemsCount[FIELD_REF]{itemsCount, Ltest.Test;, J, itemsCount, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}

public void testUnqualifiedNonStaticField() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  int items;
				  int otherItems;
				  long itemsCount;
				  void foo() {
				    item
				  }
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "item";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"otherItems[FIELD_REF]{otherItems, Ltest.Test;, I, otherItems, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"items[FIELD_REF]{items, Ltest.Test;, I, items, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"itemsCount[FIELD_REF]{itemsCount, Ltest.Test;, J, itemsCount, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void testQualifiedStaticField() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  static int items;
				  int otherItems;
				  static long itemsCount;
				  void foo() {
				    Test.item
				  }
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "Test.item";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"items[FIELD_REF]{items, Ltest.Test;, I, items, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_NON_INHERITED) + "}\n" +
			"itemsCount[FIELD_REF]{itemsCount, Ltest.Test;, J, itemsCount, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_NON_INHERITED) + "}",
			requestor.getResults());
}
public void testUnqualifiedStaticField() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  static int items;
				  int otherItems;
				  static long itemsCount;
				  void foo() {
				    item
				  }
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "item";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"otherItems[FIELD_REF]{otherItems, Ltest.Test;, I, otherItems, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"items[FIELD_REF]{items, Ltest.Test;, I, items, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"itemsCount[FIELD_REF]{itemsCount, Ltest.Test;, J, itemsCount, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void testLocalVariable() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  static int items;
				  int otherItems;
				  static long itemsCount;
				  void foo() {
				    int temporaryItem = 0;
				    item
				  }
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "item";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"otherItems[FIELD_REF]{otherItems, Ltest.Test;, I, otherItems, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"temporaryItem[LOCAL_VARIABLE_REF]{temporaryItem, null, I, temporaryItem, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"items[FIELD_REF]{items, Ltest.Test;, I, items, null, " + (R_DEFAULT + R_RESOLVED + R_CASE + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"itemsCount[FIELD_REF]{itemsCount, Ltest.Test;, J, itemsCount, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void testMethodParamVariable() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  static int items;
				  int otherItems;
				  static long itemsCount;
				  void foo(int initItems) {
				    item
				  }
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "item";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"initItems[LOCAL_VARIABLE_REF]{initItems, null, I, initItems, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"otherItems[FIELD_REF]{otherItems, Ltest.Test;, I, otherItems, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"items[FIELD_REF]{items, Ltest.Test;, I, items, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"itemsCount[FIELD_REF]{itemsCount, Ltest.Test;, J, itemsCount, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void testClassTypeInstantiation() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/test/Foobar.java",
			"""
				package test;
				interface Foobar {}
				class SpecificFooBar implements Foobar {}
				class EvenMoreSpecificFooBar extends SpecificFooBar {}
				interface Foobaz {}
				class SpecificFooBaz implements Foobaz {}
				""");
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  {
				    Foobar f = new bar
				  }
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"EvenMoreSpecificFooBar[TYPE_REF]{EvenMoreSpecificFooBar, test, Ltest.EvenMoreSpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"SpecificFooBar[TYPE_REF]{SpecificFooBar, test, Ltest.SpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"Foobar[TYPE_REF]{Foobar, test, Ltest.Foobar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testClassTypeFieldDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/test/Foobar.java",
			"""
				package test;
				interface Foobar {}
				class SpecificFooBar implements Foobar {}
				class EvenMoreSpecificFooBar extends SpecificFooBar {}
				interface Foobaz {}
				class SpecificFooBaz implements Foobaz {}
				""");
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  public bar
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "public bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"EvenMoreSpecificFooBar[TYPE_REF]{EvenMoreSpecificFooBar, test, Ltest.EvenMoreSpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"Foobar[TYPE_REF]{Foobar, test, Ltest.Foobar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"SpecificFooBar[TYPE_REF]{SpecificFooBar, test, Ltest.SpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testClassTypeParamDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/test/Foobar.java",
			"""
				package test;
				interface Foobar {}
				class SpecificFooBar implements Foobar {}
				class EvenMoreSpecificFooBar extends SpecificFooBar {}
				interface Foobaz {}
				class SpecificFooBaz implements Foobaz {}
				""");
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  void setFoo(bar
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "void setFoo(bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"EvenMoreSpecificFooBar[TYPE_REF]{EvenMoreSpecificFooBar, test, Ltest.EvenMoreSpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"Foobar[TYPE_REF]{Foobar, test, Ltest.Foobar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"SpecificFooBar[TYPE_REF]{SpecificFooBar, test, Ltest.SpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testClassTypeLocalVarDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/test/Foobar.java",
			"""
				package test;
				interface Foobar {}
				class SpecificFooBar implements Foobar {}
				class EvenMoreSpecificFooBar extends SpecificFooBar {}
				interface Foobaz {}
				class SpecificFooBaz implements Foobaz {}
				""");
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  void foo() {
				    final bar\
				  }\
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "final bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"EvenMoreSpecificFooBar[TYPE_REF]{EvenMoreSpecificFooBar, test, Ltest.EvenMoreSpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"Foobar[TYPE_REF]{Foobar, test, Ltest.Foobar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"SpecificFooBar[TYPE_REF]{SpecificFooBar, test, Ltest.SpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testClassTypeThrowsDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/test/Foobar.java",
			"""
				package test;
				interface Foobar {}
				class SpecificFooBar implements Foobar {}
				class EvenMoreSpecificFooBar extends SpecificFooBar {}
				interface Foobaz {}
				class SpecificFooBaz implements Foobaz {}
				""");
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  void foo() throws bar {
				  }\
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "void foo() throws bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"EvenMoreSpecificFooBar[TYPE_REF]{EvenMoreSpecificFooBar, test, Ltest.EvenMoreSpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}\n" +
			"SpecificFooBar[TYPE_REF]{SpecificFooBar, test, Ltest.SpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testClassTypeExtends() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/test/Foobar.java",
			"""
				package test;
				interface Foobar {}
				class SpecificFooBar implements Foobar {}
				class EvenMoreSpecificFooBar extends SpecificFooBar {}
				interface Foobaz {}
				class SpecificFooBaz implements Foobaz {}
				""");
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test extends bar {
				  }\
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "public class Test extends bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"EvenMoreSpecificFooBar[TYPE_REF]{EvenMoreSpecificFooBar, test, Ltest.EvenMoreSpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_CLASS + R_SUBSTRING) + "}\n" +
			"SpecificFooBar[TYPE_REF]{SpecificFooBar, test, Ltest.SpecificFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_CLASS + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testClassTypeImplements() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/test/Foobar.java",
			"""
				package test;
				interface Foobar {}
				interface FoobarExtension extends Foobar {}
				class SpecificFooBar implements Foobar {}
				class EvenMoreSpecificFooBar extends SpecificFooBar {}
				interface Foobaz {}
				class SpecificFooBaz implements Foobaz {}
				""");
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test implements bar {
				  }\
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "public class Test implements bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Foobar[TYPE_REF]{Foobar, test, Ltest.Foobar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_INTERFACE + R_SUBSTRING) + "}\n" +
			"FoobarExtension[TYPE_REF]{FoobarExtension, test, Ltest.FoobarExtension;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED + R_INTERFACE + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testInnerClassTypeInstantiation() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  class FooBar {}
				  {
				    Test t = new Test();
				    Object f = t.new bar
				  }
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "t.new bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testInnerClassTypeFieldDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  class FooBar {}
				  public bar
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "public bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testInnerClassTypeParamDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  class FooBar {}
				  void foo(bar
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "void foo(bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testInnerClassTypeLocalVarDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  class FooBar {}
				  {
				    final bar
				  }
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "final bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testInnerClassTypeThrowsDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  class FooBar extends Exception {}
				  void foo() throws bar\
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "void foo() throws bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testInnerClassTypeExtends() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  class FooBar {}
				  class SpecificFooBar extends bar
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "SpecificFooBar extends bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testInnerClassTypeImplements() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  interface FooBar {}
				  class SpecificFooBar implements bar
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "class SpecificFooBar implements bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeInstantiation() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  static class FooBar {}
				  {
				    Object f = new bar
				  }
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeFieldDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  static class FooBar {}
				  public bar
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "public bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeParamDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  static class FooBar {}
				  void foo(bar
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "void foo(bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeLocalVarDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  static class FooBar {}
				  {
				    final bar
				  }
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "final bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeThrowsDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  static class FooBar extends Exception {}
				  void foo() throws bar\
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "void foo() throws bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeExtends() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  static class FooBar {}
				  class SpecificFooBar extends bar
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "SpecificFooBar extends bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testStaticNestedClassTypeImplements() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  static interface FooBar {}
				  class SpecificFooBar implements bar
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "class SpecificFooBar implements bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.FooBar[TYPE_REF]{FooBar, test, Ltest.Test$FooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testLocalClassTypeInstantiation() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  void foo() {
				    class FooBar {}
				    Object f = new bar
				  }
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"FooBar[TYPE_REF]{FooBar, test, LFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testLocalClassTypeLocalVarDeclaration() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  void foo() {
				    class FooBar {}
				    final bar
				  }
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "final bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"FooBar[TYPE_REF]{FooBar, test, LFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testLocalClassTypeExtends() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				package test;\
				public class Test {
				  void foo() {
				    class FooBar {}
				    class SpecificFooBar extends bar
				  }
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "class SpecificFooBar extends bar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"FooBar[TYPE_REF]{FooBar, test, LFooBar;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_NON_RESTRICTED + R_UNQUALIFIED + R_SUBSTRING) + "}",
			requestor.getResults());
}
public void testBug488441_1() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	String content = """
		public class Try18 {
			public void main(String[] args) {
				"s".st
			}
		}
		}
		""";
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			content);
	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = ".st";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC + R_NON_RESTRICTED;
	assertResults(
			"lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (I)I, lastIndexOf, (arg0), "+ (relevance + R_SUBSTRING) +"}\n" +
			"lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (II)I, lastIndexOf, (arg0, arg1), "+ (relevance + R_SUBSTRING) +"}\n" +
			"lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (Ljava.lang.String;)I, lastIndexOf, (arg0), "+ (relevance + R_SUBSTRING) +"}\n" +
			"lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (Ljava.lang.String;I)I, lastIndexOf, (arg0, arg1), "+ (relevance + R_SUBSTRING) +"}\n" +
			"replaceFirst[METHOD_REF]{replaceFirst(), Ljava.lang.String;, (Ljava.lang.String;Ljava.lang.String;)Ljava.lang.String;, replaceFirst, (arg0, arg1), "+ (relevance + R_SUBSTRING) +"}\n" +
			"substring[METHOD_REF]{substring(), Ljava.lang.String;, (I)Ljava.lang.String;, substring, (arg0), "+ (relevance + R_SUBSTRING) +"}\n" +
			"substring[METHOD_REF]{substring(), Ljava.lang.String;, (II)Ljava.lang.String;, substring, (arg0, arg1), "+ (relevance + R_SUBSTRING) +"}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.String;, ()Ljava.lang.String;, toString, null, "+ (relevance + R_SUBSTRING) +"}\n" +
			"startsWith[METHOD_REF]{startsWith(), Ljava.lang.String;, (Ljava.lang.String;)Z, startsWith, (arg0), "+ (relevance + R_CASE) +"}\n" +
			"startsWith[METHOD_REF]{startsWith(), Ljava.lang.String;, (Ljava.lang.String;I)Z, startsWith, (arg0, arg1), "+ (relevance + R_CASE) +"}",
			requestor.getResults());
}
public void testBug488441_2() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	String content = """
		public class Try18 {
			public void main(String[] args) {
				int i = "s".st
			}
		}
		""";
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			content);

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = ".st";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC + R_NON_RESTRICTED;
	assertResults(
			"replaceFirst[METHOD_REF]{replaceFirst(), Ljava.lang.String;, (Ljava.lang.String;Ljava.lang.String;)Ljava.lang.String;, replaceFirst, (arg0, arg1), "+ (relevance + R_SUBSTRING) +"}\n" +
			"substring[METHOD_REF]{substring(), Ljava.lang.String;, (I)Ljava.lang.String;, substring, (arg0), "+ (relevance + R_SUBSTRING) +"}\n" +
			"substring[METHOD_REF]{substring(), Ljava.lang.String;, (II)Ljava.lang.String;, substring, (arg0, arg1), "+ (relevance + R_SUBSTRING) +"}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.String;, ()Ljava.lang.String;, toString, null, "+ (relevance + R_SUBSTRING) +"}\n" +
			"lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (I)I, lastIndexOf, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (II)I, lastIndexOf, (arg0, arg1), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (Ljava.lang.String;)I, lastIndexOf, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (Ljava.lang.String;I)I, lastIndexOf, (arg0, arg1), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"startsWith[METHOD_REF]{startsWith(), Ljava.lang.String;, (Ljava.lang.String;)Z, startsWith, (arg0), "+ (relevance + R_CASE) +"}\n" +
			"startsWith[METHOD_REF]{startsWith(), Ljava.lang.String;, (Ljava.lang.String;I)Z, startsWith, (arg0, arg1), "+ (relevance + R_CASE) +"}",
			requestor.getResults());
}
public void testBug488441_3() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	String content = """
		public class Try18 {
			public void main(String[] args) {
				String s = "s".st
			}
		}
		""";
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			content);

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = ".st";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC + R_NON_RESTRICTED;
	assertResults(
			"lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (I)I, lastIndexOf, (arg0), "+ (relevance + R_SUBSTRING) +"}\n" +
			"lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (II)I, lastIndexOf, (arg0, arg1), "+ (relevance + R_SUBSTRING) +"}\n" +
			"lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (Ljava.lang.String;)I, lastIndexOf, (arg0), "+ (relevance + R_SUBSTRING) +"}\n" +
			"lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (Ljava.lang.String;I)I, lastIndexOf, (arg0, arg1), "+ (relevance + R_SUBSTRING) +"}\n" +
			"replaceFirst[METHOD_REF]{replaceFirst(), Ljava.lang.String;, (Ljava.lang.String;Ljava.lang.String;)Ljava.lang.String;, replaceFirst, (arg0, arg1), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"substring[METHOD_REF]{substring(), Ljava.lang.String;, (I)Ljava.lang.String;, substring, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"substring[METHOD_REF]{substring(), Ljava.lang.String;, (II)Ljava.lang.String;, substring, (arg0, arg1), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.String;, ()Ljava.lang.String;, toString, null, "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE)  +"}\n" +
			"startsWith[METHOD_REF]{startsWith(), Ljava.lang.String;, (Ljava.lang.String;)Z, startsWith, (arg0), "+ (relevance + R_CASE) +"}\n" +
			"startsWith[METHOD_REF]{startsWith(), Ljava.lang.String;, (Ljava.lang.String;I)Z, startsWith, (arg0, arg1), "+ (relevance + R_CASE) +"}",
			requestor.getResults());
}
public void testBug488441_4() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	String content = """
		public class Try18 {
			public void main(String[] args) {
				boolean s = "s".st
			}
		}
		""";
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			content);

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = ".st";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_STATIC + R_NON_RESTRICTED;
	assertResults(
			"lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (I)I, lastIndexOf, (arg0), " + (relevance + R_SUBSTRING) + "}\n" +
			"lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (II)I, lastIndexOf, (arg0, arg1), " + (relevance + R_SUBSTRING) + "}\n" +
			"lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (Ljava.lang.String;)I, lastIndexOf, (arg0), " + (relevance + R_SUBSTRING) + "}\n" +
			"lastIndexOf[METHOD_REF]{lastIndexOf(), Ljava.lang.String;, (Ljava.lang.String;I)I, lastIndexOf, (arg0, arg1), " + (relevance + R_SUBSTRING) + "}\n" +
			"replaceFirst[METHOD_REF]{replaceFirst(), Ljava.lang.String;, (Ljava.lang.String;Ljava.lang.String;)Ljava.lang.String;, replaceFirst, (arg0, arg1), " + (relevance + R_SUBSTRING) + "}\n" +
			"substring[METHOD_REF]{substring(), Ljava.lang.String;, (I)Ljava.lang.String;, substring, (arg0), " + (relevance + R_SUBSTRING) + "}\n" +
			"substring[METHOD_REF]{substring(), Ljava.lang.String;, (II)Ljava.lang.String;, substring, (arg0, arg1), " + (relevance + R_SUBSTRING) + "}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.String;, ()Ljava.lang.String;, toString, null, " + (relevance + R_SUBSTRING) + "}\n" +
			"startsWith[METHOD_REF]{startsWith(), Ljava.lang.String;, (Ljava.lang.String;)Z, startsWith, (arg0), " + (relevance + R_EXACT_EXPECTED_TYPE + R_CASE) + "}\n" +
			"startsWith[METHOD_REF]{startsWith(), Ljava.lang.String;, (Ljava.lang.String;I)Z, startsWith, (arg0, arg1), " + (relevance + R_EXACT_EXPECTED_TYPE + R_CASE) + "}",
			requestor.getResults());
}
public void testBug488441_5() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				import java.util.Arrays;
				public class Try18 {
					public void main(String[] args) {
					String msg="";
					String[] parameters = {"a"};
					System.out.println(msg + Arrays.as);
					}
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = ".as";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_NON_INHERITED;
	assertResults(
			"deepHashCode[METHOD_REF]{deepHashCode(), Ljava.util.Arrays;, ([Ljava.lang.Object;)I, deepHashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([B)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([C)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([D)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([F)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([I)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([J)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([Ljava.lang.Object;)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([S)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([Z)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE)+"}\n" +
			"asList[METHOD_REF]{asList(), Ljava.util.Arrays;, <T:Ljava.lang.Object;>([TT;)Ljava.util.List<TT;>;, asList, (arg0), "+ (relevance + R_CASE)  +"}",
			requestor.getResults());
}
public void testBug488441_6() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				import java.util.Arrays;
				public class Try18 {
					public void main(String[] args) {
					String msg="";
					String[] parameters = {"a"};
					System.out.println(msg + Arrays.aS);
					}
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = ".aS";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_NON_INHERITED;
	assertResults(
			"asList[METHOD_REF]{asList(), Ljava.util.Arrays;, <T:Ljava.lang.Object;>([TT;)Ljava.util.List<TT;>;, asList, (arg0), "+ (relevance) +"}\n" +
			"deepHashCode[METHOD_REF]{deepHashCode(), Ljava.util.Arrays;, ([Ljava.lang.Object;)I, deepHashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([B)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([C)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([D)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([F)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([I)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([J)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([Ljava.lang.Object;)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([S)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([Z)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}",
			requestor.getResults());
}
public void testBug488441_7() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				import java.util.Arrays;
				public class Try18 {
					public void main(String[] args) {
					String msg="";
					String[] parameters = {"a"};
					System.out.println(Arrays.as);
					}
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = ".as";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_NON_INHERITED;
	assertResults(
			"deepHashCode[METHOD_REF]{deepHashCode(), Ljava.util.Arrays;, ([Ljava.lang.Object;)I, deepHashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([B)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([C)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([D)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([F)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([I)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([J)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([Ljava.lang.Object;)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([S)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([Z)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"asList[METHOD_REF]{asList(), Ljava.util.Arrays;, <T:Ljava.lang.Object;>([TT;)Ljava.util.List<TT;>;, asList, (arg0), "+ (relevance + R_EXPECTED_TYPE + R_CASE) +"}",
			requestor.getResults());
}
public void testBug488441_8() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test.java",
			"""
				import java.util.Arrays;
				public class Try18 {
					public void main(String[] args) {
					String msg="";
					String[] parameters = {"a"};
					int i = Arrays.as;
					}
				}
				""");

	CompletionTestsRequestor2 requestor = createFilteredRequestor();
	String str = this.workingCopies[0].getSource();
	String completeBehind = ".as";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_NON_INHERITED;
	assertResults(
			"deepHashCode[METHOD_REF]{deepHashCode(), Ljava.util.Arrays;, ([Ljava.lang.Object;)I, deepHashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([B)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([C)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([D)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([F)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([I)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([J)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([Ljava.lang.Object;)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([S)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE) +"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.util.Arrays;, ([Z)I, hashCode, (arg0), "+ (relevance + R_SUBSTRING + R_EXACT_EXPECTED_TYPE)+"}\n" +
			"asList[METHOD_REF]{asList(), Ljava.util.Arrays;, <T:Ljava.lang.Object;>([TT;)Ljava.util.List<TT;>;, asList, (arg0), "+ (relevance + R_CASE)  +"}",
			requestor.getResults());
}
}
