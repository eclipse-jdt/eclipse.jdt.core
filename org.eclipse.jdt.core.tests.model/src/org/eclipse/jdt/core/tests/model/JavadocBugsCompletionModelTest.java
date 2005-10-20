/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Test class for completion in Javadoc comment of a field declaration.
 */
public class JavadocBugsCompletionModelTest extends AbstractJavadocCompletionModelTest {

public JavadocBugsCompletionModelTest(String name) {
	super(name);
	this.tabs = 2;
}

static {
//	TESTS_NAMES = new String[] { "testBug68757" };
}
public static Test suite() {
	return buildTestSuite(JavadocBugsCompletionModelTest.class);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.AbstractJavadocCompletionModelTest#setUp()
 */
protected void setUp() throws Exception {
	super.setUp();
	setUpProjectOptions(CompilerOptions.VERSION_1_4);
}

/**
 * Bug 3270: [javadoc][assist] Javadoc content assist should show classes after @throws tag (1GDWWV9)
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=3270"
 */
public void testBug3270() throws JavaModelException {
	String source =
		"package javadoc.bugs;\n" + 
		"public class BasicTestBugs {\n" + 
		"	/**\n" + 
		"	 * @throws \n" + 
		"	 */\n" + 
		"	void foo() throws InterruptedException {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/bugs/BasicTestBugs.java", source, true, "@throws ", 0); // empty token
	assertSortedResults(
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+"51}"
	);
}
public void testBug3270a() throws JavaModelException {
	String source =
		"package javadoc.bugs;\n" + 
		"public class BasicTestBugs {\n" + 
		"	/**\n" + 
		"	 * @throws I\n" + 
		"	 */\n" + 
		"	void foo() throws InterruptedException {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/bugs/BasicTestBugs.java", source, true, "I");
	assertSortedResults(
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+(40+R_INLINE_TAG)+"}\n" + 
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+"41}"
	);
}
public void testBug3270b() throws JavaModelException {
	String source =
		"package javadoc.bugs;\n" + 
		"public class BasicTestBugs {\n" + 
		"	/**\n" + 
		"	 * @throws Cl\n" + 
		"	 */\n" + 
		"	void foo() throws InterruptedException {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/bugs/BasicTestBugs.java", source, true, "Cl");
	assertSortedResults(
		"CloneNotSupportedException[TYPE_REF]{CloneNotSupportedException, java.lang, Ljava.lang.CloneNotSupportedException;, null, null, "+this.positions+"41}\n" + 
		"Class[TYPE_REF]{Class, java.lang, Ljava.lang.Class;, null, null, "+this.positions+"21}"
	);
}

/**
 * Bug 22043: [javadoc][assist] Code Completion in Javadoc @see/@link doesn't work on partially entered argument types
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=22043"
 */
public void testBug22043() throws JavaModelException {
	String source =
		"package javadoc.bugs;\n" + 
		"/**\n" + 
		" * Complete after (S:\n" + 
		" * 	@see #thisIsAMethod(S\n" + 
		" */\n" + 
		"public class BasicTestBugs {\n" + 
		"	public void thisIsAMethod(String param) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/bugs/BasicTestBugs.java", source, true, "S", 2); // 2nd occurence
	assertSortedResults(
		"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+this.positions+"21}\n" +
		"Serializable[TYPE_REF]{java.io.Serializable, java.io, Ljava.io.Serializable;, null, null, "+this.positions+"18}\n" + 
		"short[KEYWORD]{short, null, null, short, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void testBug22043a() throws JavaModelException {
	String source =
		"package javadoc.bugs;\n" + 
		"/**\n" + 
		" * Complete after thisIsAMethod:\n" + 
		" * 	@see #thisIsAMethod(S\n" + 
		" */\n" + 
		"public class BasicTestBugs {\n" + 
		"	public void thisIsAMethod(String param) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/bugs/BasicTestBugs.java", source, true, "thisIsAMethod", 2); // 2nd occurence
	assertResults(
		"thisIsAMethod[METHOD_REF]{thisIsAMethod(String), Ljavadoc.bugs.BasicTestBugs;, (Ljava.lang.String;)V, thisIsAMethod, (param), "+this.positions+"25}"
	);
}

public void testBug22043b() throws JavaModelException {
	String source =
		"package javadoc.bugs;\n" + 
		"/**\n" + 
		" * Complete after thisIsAMethod(:\n" + 
		" * 	@see #thisIsAMethod(S\n" + 
		" */\n" + 
		"public class BasicTestBugs {\n" + 
		"	public void thisIsAMethod(String param) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/bugs/BasicTestBugs.java", source, true, "thisIsAMethod(", 2); // 2nd occurence
	assertResults(
		"thisIsAMethod[METHOD_REF]{thisIsAMethod(String), Ljavadoc.bugs.BasicTestBugs;, (Ljava.lang.String;)V, thisIsAMethod, (param), "+this.positions+"25}"
	);
}

public void testBug22043c() throws JavaModelException {
	String source =
		"package javadoc.bugs;\n" + 
		"/**\n" + 
		" * Complete after thisIsAMethod(:\n" + 
		" * 	@see #thisIsAMethod(Object\n" + 
		" */\n" + 
		"public class BasicTestBugs {\n" + 
		"	public void thisIsAMethod(String param) {}\n" + 
		"	public void thisIsAMethod(Object str) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/bugs/BasicTestBugs.java", source, true, "thisIsAMethod(", 2); // 2nd occurence
	assertResults(
		"thisIsAMethod[METHOD_REF]{thisIsAMethod(Object), Ljavadoc.bugs.BasicTestBugs;, (Ljava.lang.Object;)V, thisIsAMethod, (str), "+this.positions+"25}\n" + 
		"thisIsAMethod[METHOD_REF]{thisIsAMethod(String), Ljavadoc.bugs.BasicTestBugs;, (Ljava.lang.String;)V, thisIsAMethod, (param), "+this.positions+"25}"
	);
}

/**
 * Bug 67732: [javadoc][assist] Content assist doesn't work in Javadoc "line breaks"
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=67732"
 */
public void testBug67732() throws JavaModelException {
	String source =
		"package javadoc.bugs;\n" + 
		"/**\n" + 
		" * This line approaches the print margin {@link \n" + 
		" * Object#to\n" + 
		" */\n" + 
		"public class T67732 {\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/bugs/BasicTestBugs.java", source, true, "to");
	assertSortedResults(
		"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+"29}"
	);
}

/**
 * Bug 68757: [javadoc][assist] inconsistent type qualification
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=68757"
 */
public void testBug68757() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/bugs/BasicTestBugs.java", 
			"package javadoc.bugs;\n" + 
			"import javadoc.tests.OtherType;\n" + 
			"public class BasicTestBugs {\n" + 
			"	/**\n" + 
			"	 * @see Other\n" + 
			"	 */\n" + 
			"	public void foo(OtherType type) {}\n" + 
			"}\n",
		"/Completion/src/javadoc/tests/OtherType.java", 
			"package javadoc.tests;\n" + 
			"public class OtherType {\n" + 
			"}\n"
	};
	completeInJavadoc(sources, true, "Other", 2);	// 2nd occurrence
	assertSortedResults(
		"OtherType[TYPE_REF]{OtherType, javadoc.tests, Ljavadoc.tests.OtherType;, null, null, "+this.positions+"21}"
	);
}
public void testBug68757a() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/bugs/BasicTestBugs.java", 
			"package javadoc.bugs;\n" + 
			"public class BasicTestBugs {\n" + 
			"	/**\n" + 
			"	 * @see Other\n" + 
			"	 */\n" + 
			"	public void foo(javadoc.tests.OtherType type) {}\n" + 
			"}\n",
		"/Completion/src/javadoc/tests/OtherType.java", 
			"package javadoc.tests;\n" + 
			"public class OtherType {\n" + 
			"}\n"
	};
	completeInJavadoc(sources, true, "Other");
	assertSortedResults(
		"OtherType[TYPE_REF]{javadoc.tests.OtherType, javadoc.tests, Ljavadoc.tests.OtherType;, null, null, "+this.positions+"18}"
	);
}
public void testBug68757b() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/bugs/BasicTestBugs.java", 
			"package javadoc.bugs;\n" + 
			"public class BasicTestBugs {\n" + 
			"	/**\n" + 
			"	 * @see Other\n" + 
			"	 */\n" + 
			"	public void foo() {}\n" + 
			"}\n",
		"/Completion/src/javadoc/tests/OtherType.java", 
			"package javadoc.tests;\n" + 
			"public class OtherType {\n" + 
			"}\n"
	};
	completeInJavadoc(sources, true, "Other");
	assertSortedResults(
		"OtherType[TYPE_REF]{javadoc.tests.OtherType, javadoc.tests, Ljavadoc.tests.OtherType;, null, null, "+this.positions+"18}"
	);
}

/**
 * Bug 75551: [javadoc][assist] javadoc completion for links to method with inner classes as argument is not correct
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=75551"
 * TODO (frederic) fix while fixing bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=96237
 */
public void _testBug75551() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/bugs/BasicTestBugs.java", 
			"package javadoc.bugs;\n" + 
			"public class BasicTestBugs extends SuperClass {\n" + 
			"  /**\n" + 
			"   * {@link #kick(Inner\n" + 
			"   */\n" + 
			"  public BasicTestBugs() {\n" + 
			"  }\n" + 
			"  public void kick(InnerClass innerClass) {}\n" + 
			"}",
			"/Completion/src/javadoc/bugs/SuperClass.java", 
			"package javadoc.bugs;\n" + 
			"public class SuperClass {\n" + 
			"  protected static class InnerClass {\n" + 
			"  }\n" + 
			"}"
	};
	completeInJavadoc(sources, true, "Inner");
	assertSortedResults(
		"SuperClass.InnerClass[TYPE_REF]{SuperClass.InnerClass, javadoc.bugs, Ljavadoc.bugs.SuperClass$InnerClass;, null, null, "+this.positions+"21}"
	);
}

/**
 * Bug 86112: [javadoc][assist] Wrong reference to binary static initializer in javadoc
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=86112"
 */
public void testBug86112() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/bugs/BasicTestBugs.java", 
			"package javadoc.bugs;\n" + 
			"public class BasicTestBugs {\n" + 
			"  /**\n" + 
			"   * @see Terminator#\n" + 
			"   */\n" + 
			"  public BasicTestBugs() {\n" + 
			"  }\n" + 
			"}",
			"/Completion/src/javadoc/bugs/Terminator.java", 
			"package javadoc.bugs;\n" + 
			"public class Terminator {\n" + 
			"  static {\n" + 
			"  }\n" + 
			"}"
	};
	completeInJavadoc(sources, true, "#", 0); // empty token
	assertSortedResults(
		"Terminator[METHOD_REF<CONSTRUCTOR>]{Terminator(), Ljavadoc.bugs.Terminator;, ()V, Terminator, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

/**
 * Bug 87868: [javadoc][assist] Wrong reference to binary static initializer in javadoc
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=87868"
 */
public void testBug87868() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.bugs;\n" + 
		"public class BasicTestBugs<S> {\n" + 
		"	/** \n" + 
		"	 * Calls {@link #meth\n" + 
		"	 */\n" + 
		"	public void method(S s) {}\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/bugs/BasicTestBugs.java", source, true, "meth");
	assertSortedResults(
		"method[METHOD_REF]{method(Object), Ljavadoc.bugs.BasicTestBugs<TS;>;, (TS;)V, method, (s), "+this.positions+"29}"
	);
}
}
