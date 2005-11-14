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
//	TESTS_NAMES = new String[] { "testBug22043a" };
//	TESTS_NUMBERS = new int[] { 114341 };
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
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DICUNREET+"}"
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
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DICUNREEET+"}\n" + 
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+R_DICUNRE+"}"
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
		"CloneNotSupportedException[TYPE_REF]{CloneNotSupportedException, java.lang, Ljava.lang.CloneNotSupportedException;, null, null, "+this.positions+R_DICUNRE+"}\n" + 
		"Class[TYPE_REF]{Class, java.lang, Ljava.lang.Class;, null, null, "+this.positions+R_DICUNR+"}"
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
		"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+this.positions+R_DICUNR+"}\n" +
		"Serializable[TYPE_REF]{java.io.Serializable, java.io, Ljava.io.Serializable;, null, null, "+this.positions+R_DICNR+"}\n" + 
		"short[KEYWORD]{short, null, null, short, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void testBug22043a() throws JavaModelException {
	String source =
		"package javadoc.bugs;\n" + 
		"/**\n" + 
		" * Complete after 'thisIsAMethod':\n" + 
		" * 	@see #thisIsAMethod(S\n" + 
		" */\n" + 
		"public class BasicTestBugs {\n" + 
		"	public void thisIsAMethod(String param) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/bugs/BasicTestBugs.java", source, true, "thisIsAMethod", 2); // 2nd occurence
	assertResults(
		"thisIsAMethod[METHOD_REF]{thisIsAMethod(String), Ljavadoc.bugs.BasicTestBugs;, (Ljava.lang.String;)V, thisIsAMethod, (param), "+this.positions+R_DICENNRNS+"}"
	);
}

public void testBug22043b() throws JavaModelException {
	String source =
		"package javadoc.bugs;\n" + 
		"/**\n" + 
		" * Complete after 'thisIsAMethod(':\n" + 
		" * 	@see #thisIsAMethod(S\n" + 
		" */\n" + 
		"public class BasicTestBugs {\n" + 
		"	public void thisIsAMethod(String param) {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/bugs/BasicTestBugs.java", source, true, "thisIsAMethod(", 2); // 2nd occurence
	assertResults(
		"thisIsAMethod[METHOD_REF]{thisIsAMethod(String), Ljavadoc.bugs.BasicTestBugs;, (Ljava.lang.String;)V, thisIsAMethod, (param), "+this.positions+R_DICENUNR+"}"
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
		"thisIsAMethod[METHOD_REF]{thisIsAMethod(Object), Ljavadoc.bugs.BasicTestBugs;, (Ljava.lang.Object;)V, thisIsAMethod, (str), "+this.positions+R_DICENUNR+"}\n" + 
		"thisIsAMethod[METHOD_REF]{thisIsAMethod(String), Ljavadoc.bugs.BasicTestBugs;, (Ljava.lang.String;)V, thisIsAMethod, (param), "+this.positions+R_DICENUNR+"}"
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
		"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DICNRNS+"}"
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
		"OtherType[TYPE_REF]{OtherType, javadoc.tests, Ljavadoc.tests.OtherType;, null, null, "+this.positions+R_DICUNR+"}"
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
		"OtherType[TYPE_REF]{javadoc.tests.OtherType, javadoc.tests, Ljavadoc.tests.OtherType;, null, null, "+this.positions+R_DICNR+"}"
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
		"OtherType[TYPE_REF]{javadoc.tests.OtherType, javadoc.tests, Ljavadoc.tests.OtherType;, null, null, "+this.positions+R_DICNR+"}"
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
		"SuperClass.InnerClass[TYPE_REF]{SuperClass.InnerClass, javadoc.bugs, Ljavadoc.bugs.SuperClass$InnerClass;, null, null, "+this.positions+R_DICUNR+"}"
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
		"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+this.positions+R_DICNRNS+"}\n" + 
		"equals[METHOD_REF]{equals(Object), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+this.positions+R_DICNRNS+"}\n" + 
		"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+this.positions+R_DICNRNS+"}\n" + 
		"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class;, getClass, null, "+this.positions+R_DICNRNS+"}\n" + 
		"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+this.positions+R_DICNRNS+"}\n" + 
		"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+this.positions+R_DICNRNS+"}\n" + 
		"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+this.positions+R_DICNRNS+"}\n" + 
		"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DICNRNS+"}\n" + 
		"wait[METHOD_REF]{wait(long, int), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+this.positions+R_DICNRNS+"}\n" + 
		"wait[METHOD_REF]{wait(long), Ljava.lang.Object;, (J)V, wait, (millis), "+this.positions+R_DICNRNS+"}\n" + 
		"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+this.positions+R_DICNRNS+"}\n" + 
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
		"method[METHOD_REF]{method(Object), Ljavadoc.bugs.BasicTestBugs<TS;>;, (TS;)V, method, (s), "+this.positions+R_DICNRNS+"}"
	);
}

/**
 * Bug 113374: [javadoc][assist] do not propose anything if the prefix is preceded by a special character
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=113374"
 */
public void testBug113374a() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.bugs;\n" + 
		"/** \n" + 
		" * <co\n" + 
		" */\n" + 
		"public class BasicTestBugs {\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/bugs/BasicTestBugs.java", source, true, "co");
	assertSortedResults("");
}
public void testBug113374b() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.bugs;\n" + 
		"/** \n" + 
		" * &un\n" + 
		" */\n" + 
		"public class BasicTestBugs {\n" + 
		"}";
	completeInJavadoc("/Completion/src/javadoc/bugs/BasicTestBugs.java", source, true, "un");
	assertSortedResults("");
}

/**
 * Bug 113376: [javadoc][assist] wrong overwrite range on completion followed by a tag
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=113376"
 */
public void testBug113376a() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String[] sources = {
		"/Completion/src/javadoc/bugs/BasicTestFields.java",
			"package javadoc.bugs;\n" + 
			"/**\n" + 
			" * @see javadoc.util.Collection\n" + 
			" * @see javadoc.util.List#add(Object)\n" + 
			" */public class BasicTestBugs<A> {\n" + 
			"}",
		"/Completion/src/javadoc/util/Collection.java",
			"package javadoc.util;\n" + 
			"public interface Collection<E> {}\n" + 
			"public interface List<E> {}\n" + 
			"	public void add(E e);\n" + 
			"}"
	};
	completeInJavadoc(sources, true, "javadoc.util.Collection");
	assertSortedResults(
		"Collection[TYPE_REF]{javadoc.util.Collection, javadoc.util, Ljavadoc.util.Collection;, null, null, "+this.positions+R_DICENQNR+"}"
	);
}
public void testBug113376b() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String[] sources = {
		"/Completion/src/javadoc/bugs/BasicTestFields.java",
			"package javadoc.bugs;\n" + 
			"/**\n" + 
			" * {@link String.}\n" + 
			" * \n" + 
			" * @see javadoc.util.Collection\n" + 
			" * @see javadoc.util.List#add(Object)\n" + 
			" */public class BasicTestBugs<A> {\n" + 
			"}",
		"/Completion/src/javadoc/util/Collection.java",
			"package javadoc.util;\n" + 
			"public interface Collection<E> {}\n" + 
			"public interface List<E> {}\n" + 
			"	public void add(E e);\n" + 
			"}"
	};
	completeInJavadoc(sources, true, "String.", 0); // empty token
	assertSortedResults("");
}

/**
 * Bug 114341: [javadoc][assist] range of the qualified type completion in javadoc text isn't corect
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=114341"
 */
public void testBug114341a() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"  /**\n" + 
		"   * Secondary.Mem\n" + 
		"   */\n" + 
		"  void foo() {}\n" + 
		"}\n" + 
		"class Secondary {\n" + 
		"  class Member {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "Secondary.Mem");
	assertSortedResults(
		"Secondary.Member[JAVADOC_TYPE_REF]{{@link Member }, javadoc.text, Ljavadoc.text.Secondary$Member;, null, null, "+this.positions+R_DICNRIT+"}\n" + 
		"Secondary.Member[TYPE_REF]{Member, javadoc.text, Ljavadoc.text.Secondary$Member;, null, null, "+this.positions+R_DICNR+"}"
	);
}

public void testBug114341b() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"  /**\n" + 
		"   * @see Secondary.Mem\n" + 
		"   */\n" + 
		"  void foo() {}\n" + 
		"}\n" + 
		"class Secondary {\n" + 
		"  class Member {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "Mem");
	assertSortedResults(
		"Secondary.Member[TYPE_REF]{Member, javadoc.text, Ljavadoc.text.Secondary$Member;, null, null, "+this.positions+R_DICNR+"}"
	);
}

public void testBug114341c() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"  /**\n" + 
		"   * {@link Secondary.Mem }\n" + 
		"   */\n" + 
		"  void foo() {}\n" + 
		"}\n" + 
		"class Secondary {\n" + 
		"  class Member {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "Mem");
	assertSortedResults(
		"Secondary.Member[TYPE_REF]{Member, javadoc.text, Ljavadoc.text.Secondary$Member;, null, null, "+this.positions+R_DICNR+"}"
	);
}
public void testBug114341d() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"  /**\n" + 
		"   * javadoc.text.Secondary.Mem\n" + 
		"   */\n" + 
		"  void foo() {}\n" + 
		"}\n" + 
		"class Secondary {\n" + 
		"  class Member {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "javadoc.text.Secondary.Mem");
	assertSortedResults(
		"Secondary.Member[JAVADOC_TYPE_REF]{{@link Member }, javadoc.text, Ljavadoc.text.Secondary$Member;, null, null, "+this.positions+R_DICNRIT+"}\n" + 
		"Secondary.Member[TYPE_REF]{Member, javadoc.text, Ljavadoc.text.Secondary$Member;, null, null, "+this.positions+R_DICNR+"}"
	);
}

public void testBug114341e() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"  /**\n" + 
		"   * @see javadoc.text.Secondary.Mem\n" + 
		"   */\n" + 
		"  void foo() {}\n" + 
		"}\n" + 
		"class Secondary {\n" + 
		"  class Member {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "Mem");
	assertSortedResults(
		"Secondary.Member[TYPE_REF]{Member, javadoc.text, Ljavadoc.text.Secondary$Member;, null, null, "+this.positions+R_DICNR+"}"
	);
}

public void testBug114341f() throws JavaModelException {
	String source =
		"package javadoc.text;\n" + 
		"public class BasicTestTextIns {\n" + 
		"  /**\n" + 
		"   * {@link javadoc.text.Secondary.Mem }\n" + 
		"   */\n" + 
		"  void foo() {}\n" + 
		"}\n" + 
		"class Secondary {\n" + 
		"  class Member {}\n" + 
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/text/BasicTestTextIns.java", source, true, "Mem");
	assertSortedResults(
		"Secondary.Member[TYPE_REF]{Member, javadoc.text, Ljavadoc.text.Secondary$Member;, null, null, "+this.positions+R_DICNR+"}"
	);
}

/**
 * Bug 115662: [javadoc][assist] range of the qualified type completion in javadoc text isn't corect
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=115662"
 */
public void testBug115662a() throws JavaModelException {
	String source =
		"package bugs.b115662;\n" + 
		"/**\n" + 
		" * {@link #to\n" + 
		" */\n" + 
		"public class Tests {\n" + 
		"	int toto;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b115662/Test.java", source, true, "to");
	assertSortedResults(
		"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DICNRNS+"}\n" + 
		"toto[FIELD_REF]{toto, Lbugs.b115662.Tests;, I, toto, null, "+this.positions+R_DICNRNS+"}"
	);
}

public void testBug115662b() throws JavaModelException {
	String source =
		"package bugs.b115662;\n" + 
		"/**\n" + 
		" * {@link #toString()\n" + 
		" */\n" + 
		"public class Tests {\n" + 
		"	int toto;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b115662/Test.java", source, true, "to");
	assertSortedResults(
		"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DICNRNS+"}\n" + 
		"toto[FIELD_REF]{toto, Lbugs.b115662.Tests;, I, toto, null, "+this.positions+R_DICNRNS+"}"
	);
}

public void testBug115662c() throws JavaModelException {
	String source =
		"package bugs.b115662;\n" + 
		"/**\n" + 
		" * {@link #toString()\n" + 
		" */\n" + 
		"public class Tests {\n" + 
		"	int toto;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b115662/Test.java", source, true, "toString");
	assertSortedResults(
		"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DICENNRNS+"}"
	);
}
}
