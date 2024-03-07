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
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

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
//	TESTS_NUMBERS = new int[] { 118092 };
}
public static Test suite() {
	return buildModelTestSuite(JavadocBugsCompletionModelTest.class);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.AbstractJavadocCompletionModelTest#setUp()
 */
@Override
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
	if (CompletionEngine.NO_TYPE_COMPLETION_ON_EMPTY_TOKEN) {
		assertSortedResults(
			"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DRICUNREETE+"}"
		);
	} else {
		assertSortedResults(
			"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DRICUNREETE+"}\n" +
			"BasicTestBugs[TYPE_REF]{BasicTestBugs, javadoc.bugs, Ljavadoc.bugs.BasicTestBugs;, null, null, "+this.positions+R_DRICUNR+"}"
		);
	}
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
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DRICUNREEET+"}\n" +
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+R_DRICUNRE+"}"
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
		"CloneNotSupportedException[TYPE_REF]{CloneNotSupportedException, java.lang, Ljava.lang.CloneNotSupportedException;, null, null, "+this.positions+R_DRICUNRE+"}\n" +
		"Class[TYPE_REF]{Class, java.lang, Ljava.lang.Class;, null, null, "+this.positions+R_DRICUNR+"}"
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
		"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+this.positions+R_DRICUNR+"}\n" +
		"Serializable[TYPE_REF]{java.io.Serializable, java.io, Ljava.io.Serializable;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"short[KEYWORD]{short, null, null, short, null, "+this.positions+R_DRINR+"}"
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
		"thisIsAMethod[METHOD_REF]{thisIsAMethod(String), Ljavadoc.bugs.BasicTestBugs;, (Ljava.lang.String;)V, thisIsAMethod, (param), "+this.positions+R_DRICENNRNS+"}"
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
		"thisIsAMethod[METHOD_REF]{thisIsAMethod(String), Ljavadoc.bugs.BasicTestBugs;, (Ljava.lang.String;)V, thisIsAMethod, (param), "+this.positions+R_DRICENUNR+"}"
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
		"thisIsAMethod[METHOD_REF]{thisIsAMethod(Object), Ljavadoc.bugs.BasicTestBugs;, (Ljava.lang.Object;)V, thisIsAMethod, (str), "+this.positions+R_DRICENUNR+"}\n" +
		"thisIsAMethod[METHOD_REF]{thisIsAMethod(String), Ljavadoc.bugs.BasicTestBugs;, (Ljava.lang.String;)V, thisIsAMethod, (param), "+this.positions+R_DRICENUNR+"}"
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
		"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DRICNRNS+"}"
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
		"OtherType[TYPE_REF]{OtherType, javadoc.tests, Ljavadoc.tests.OtherType;, null, null, "+this.positions+R_DRICUNR+"}"
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
		"OtherType[TYPE_REF]{javadoc.tests.OtherType, javadoc.tests, Ljavadoc.tests.OtherType;, null, null, "+this.positions+R_DRICNR+"}"
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
		"OtherType[TYPE_REF]{javadoc.tests.OtherType, javadoc.tests, Ljavadoc.tests.OtherType;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

/**
 * Bug 75551: [javadoc][assist] javadoc completion for links to method with inner classes as argument is not correct
 * TODO (frederic) fix while fixing bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=96237
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=75551"
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
		"SuperClass.InnerClass[TYPE_REF]{SuperClass.InnerClass, javadoc.bugs, Ljavadoc.bugs.SuperClass$InnerClass;, null, null, "+this.positions+R_DRICUNR+"}"
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
		"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+this.positions+R_DRICNRNS+"}\n" +
		"equals[METHOD_REF]{equals(Object), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+this.positions+R_DRICNRNS+"}\n" +
		"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+this.positions+R_DRICNRNS+"}\n" +
		"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class;, getClass, null, "+this.positions+R_DRICNRNS+"}\n" +
		"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+this.positions+R_DRICNRNS+"}\n" +
		"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+this.positions+R_DRICNRNS+"}\n" +
		"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+this.positions+R_DRICNRNS+"}\n" +
		"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DRICNRNS+"}\n" +
		"wait[METHOD_REF]{wait(long, int), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+this.positions+R_DRICNRNS+"}\n" +
		"wait[METHOD_REF]{wait(long), Ljava.lang.Object;, (J)V, wait, (millis), "+this.positions+R_DRICNRNS+"}\n" +
		"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+this.positions+R_DRICNRNS+"}\n" +
		"Terminator[METHOD_REF<CONSTRUCTOR>]{Terminator(), Ljavadoc.bugs.Terminator;, ()V, Terminator, null, "+this.positions+R_DRINR+"}"
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
		"method[METHOD_REF]{method(Object), Ljavadoc.bugs.BasicTestBugs;, (Ljava.lang.Object;)V, method, (s), "+this.positions+R_DRICNRNS+"}"
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
		"Collection[TYPE_REF]{javadoc.util.Collection, javadoc.util, Ljavadoc.util.Collection;, null, null, "+this.positions+R_DRICENQNR+"}"
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
		"Secondary.Member[JAVADOC_TYPE_REF]{{@link Member}, javadoc.text, Ljavadoc.text.Secondary$Member;, null, null, "+this.positions+R_DRICNRIT+"}\n" +
		"Secondary.Member[TYPE_REF]{Member, javadoc.text, Ljavadoc.text.Secondary$Member;, null, null, "+this.positions+R_DRICNR+"}"
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
		"Secondary.Member[TYPE_REF]{Member, javadoc.text, Ljavadoc.text.Secondary$Member;, null, null, "+this.positions+R_DRICNR+"}"
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
		"Secondary.Member[TYPE_REF]{Member, javadoc.text, Ljavadoc.text.Secondary$Member;, null, null, "+this.positions+R_DRICNR+"}"
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
		"Secondary.Member[JAVADOC_TYPE_REF]{{@link Member}, javadoc.text, Ljavadoc.text.Secondary$Member;, null, null, "+this.positions+R_DRICNRIT+"}\n" +
		"Secondary.Member[TYPE_REF]{Member, javadoc.text, Ljavadoc.text.Secondary$Member;, null, null, "+this.positions+R_DRICNR+"}"
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
		"Secondary.Member[TYPE_REF]{Member, javadoc.text, Ljavadoc.text.Secondary$Member;, null, null, "+this.positions+R_DRICNR+"}"
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
		"Secondary.Member[TYPE_REF]{Member, javadoc.text, Ljavadoc.text.Secondary$Member;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

/**
 * Bug 115662: [javadoc][assist] link completion in types
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
		"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DRICNRNS+"}\n" +
		"toto[FIELD_REF]{toto, Lbugs.b115662.Tests;, I, toto, null, "+this.positions+R_DRICNRNS+"}"
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
		"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DRICNRNS+"}\n" +
		"toto[FIELD_REF]{toto, Lbugs.b115662.Tests;, I, toto, null, "+this.positions+R_DRICNRNS+"}"
	);
}

public void testBug115662c() throws JavaModelException {
	String source =
		"package bugs.b115662;\n" +
		"/**\n" +
		" * {@link #toString()\n" +
		" */\n" +
		"public class Test {\n" +
		"	int toto;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b115662/Test.java", source, true, "toString");
	assertSortedResults(
		"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DRICENNRNS+"}"
	);
}

/**
 * Bug 117183: [javadoc][assist] No completion in text when cursor location is followed by a '.'
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=117183"
 */
public void testBug117183a() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/bugs/BasicTestBugs.java",
			"package javadoc.bugs;\n" +
			"/**\n" +
			" * Completion inside reference Reference#A_STATIC_FIELD.\n" +
			" * Try to complete wherever inside A_STATIC_FIELD gives no proposal!\n" +
			" */\n" +
			"public class BasicTestBugs {\n" +
			"}\n",
		"/Completion/src/javadoc/bugs/Reference.java",
			"package javadoc.bugs;\n" +
			"public class Reference {\n" +
			"	public static int A_STATIC_FIELD = 0;\n" +
			"}\n"
	};
	completeInJavadoc(sources, true, "Reference#A_");
	assertSortedResults(
		"A_STATIC_FIELD[JAVADOC_FIELD_REF]{{@link Reference#A_STATIC_FIELD}, Ljavadoc.bugs.Reference;, I, A_STATIC_FIELD, null, "+this.positions+R_DRICNRIT+"}\n" +
		"A_STATIC_FIELD[JAVADOC_VALUE_REF]{{@value Reference#A_STATIC_FIELD}, Ljavadoc.bugs.Reference;, I, A_STATIC_FIELD, null, "+this.positions+R_DRICNRIT+"}"
	);
}
public void testBug117183b() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/bugs/BasicTestBugs.java",
			"package javadoc.bugs;\n" +
			"/**\n" +
			" * Completion inside reference Reference#A_STATIC_FIELD.\n" +
			" * Try to complete wherever inside A_STATIC_FIELD gives no proposal!\n" +
			" */\n" +
			"public class BasicTestBugs {\n" +
			"}\n",
		"/Completion/src/javadoc/bugs/Reference.java",
			"package javadoc.bugs;\n" +
			"public class Reference {\n" +
			"	public static int A_STATIC_FIELD = 0;\n" +
			"}\n"
	};
	completeInJavadoc(sources, true, "Reference#A_STATIC_FIELD");
	assertSortedResults(
		"A_STATIC_FIELD[JAVADOC_FIELD_REF]{{@link Reference#A_STATIC_FIELD}, Ljavadoc.bugs.Reference;, I, A_STATIC_FIELD, null, "+this.positions+R_DRICENNRIT+"}\n" +
		"A_STATIC_FIELD[JAVADOC_VALUE_REF]{{@value Reference#A_STATIC_FIELD}, Ljavadoc.bugs.Reference;, I, A_STATIC_FIELD, null, "+this.positions+R_DRICENNRIT+"}"
	);
}
public void testBug117183c() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/bugs/BasicTestBugs.java",
			"package javadoc.bugs;\n" +
			"/**\n" +
			" * Completion after Obj|\n" +
			" */\n" +
			"class BasicTestBugs {\n" +
			"}\n"
	};
	completeInJavadoc(sources, true, "Obj");
	assertSortedResults(
		"Object[JAVADOC_TYPE_REF]{{@link Object}, java.lang, Ljava.lang.Object;, null, null, "+this.positions+R_DRICUNRIT+"}\n" +
		"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}
public void testBug117183d() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/bugs/BasicTestBugs.java",
			"package javadoc.bugs;\n" +
			"/**\n" +
			" * Completion after Str.\n" +
			" */\n" +
			"class BasicTestBugs {\n" +
			"}\n"
	};
	completeInJavadoc(sources, true, "Str");
	assertSortedResults(
		"String[JAVADOC_TYPE_REF]{{@link String}, java.lang, Ljava.lang.String;, null, null, "+this.positions+R_DRICUNRIT+"}\n" +
		"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}

/**
 * Bug 118105: [javadoc][assist] Hang with 100% CPU during code assist on comment
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=118105"
 */
public void testBug118105() throws JavaModelException {
	String source =
		"package bugs.b118105;\n" +
		"/**\n" +
		" * Some words here {@link Str.\n" +
		" */\n" +
		"public class BasicTestBugs {\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b118105/BasicTestBugs.java", source, true, "Str");
	assertSortedResults(
		"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}

/**
 * Bug 118092: [javadoc][assist] Eclipse hangs on code assist when writing {@code \u00B8<c}
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=118092"
 */
public void testBug118092() throws JavaModelException {
	String source =
		"package bugs.b118092;\n" +
		"public class BasicTestBugs {\n" +
		"   /**\n" +
		"    * \u00B8<c\n" +
		"    */\n" +
		"   public void method() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b118092/BasicTestBugs.java", source, true, "<c");
	// expect no result, just not hang...
	assertSortedResults("");
}

/**
 * Bug 118311: [javadoc][assist] type \@ in javadoc comment and code assist == hang
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=118311"
 */
public void testBug118311() throws JavaModelException {
	String source =
		"package bugs.b118311;\n" +
		"/**\n" +
		" * Text \\@\n" +
		" */\n" +
		"public class BasicTestBugs {\n" +
		"\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b118311/BasicTestBugs.java", source, true, "@");
	assertSortedResults(
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"link[JAVADOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"value[JAVADOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

/**
 * Bug 118397: [javadoc][assist] type \@ in javadoc comment and code assist == hang
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=118397"
 */
public void testBug118397a() throws JavaModelException {
	String source =
		"package bugs.b118397;\n" +
		"/**\n" +
		" * @see bugs.b118.BasicTestBugs\n" +
		" */\n" +
		"public class BasicTestBugs {\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b118397/BasicTestBugs.java", source, true, "bugs.b118", 2); // 2nd occurence
	assertSortedResults(
		"bugs.b118397[PACKAGE_REF]{bugs.b118397, bugs.b118397, null, null, null, "+this.positions+R_DRICQNR+"}"
	);
}
public void testBug118397b() throws JavaModelException {
	String source =
		"package bugs.b118397;\n" +
		"/**\n" +
		" * @see Basic.Inner\n" +
		" */\n" +
		"public class BasicTestBugs {\n" +
		"	class Inner {\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b118397/BasicTestBugs.java", source, true, "Basic");
	assertSortedResults(
		"BasicTestBugs[TYPE_REF]{BasicTestBugs, bugs.b118397, Lbugs.b118397.BasicTestBugs;, null, null, "+this.positions+R_DRICUNR+"}\n" +
		"BasicTestReferences[TYPE_REF]{org.eclipse.jdt.core.tests.BasicTestReferences, org.eclipse.jdt.core.tests, Lorg.eclipse.jdt.core.tests.BasicTestReferences;, null, null, "+this.positions+R_DRICNR+"}"
	);
}
public void testBug118397c() throws JavaModelException {
	String source =
		"package bugs.b118397;\n" +
		"/**\n" +
		" * @see BasicTestBugs.In.Level2\n" +
		" */\n" +
		"public class BasicTestBugs {\n" +
		"	class Inner {\n" +
		"		class Level2 {\n" +
		"			class Level3 {\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b118397/BasicTestBugs.java", source, true, "In");
	assertSortedResults(
		"BasicTestBugs.Inner[TYPE_REF]{Inner, bugs.b118397, Lbugs.b118397.BasicTestBugs$Inner;, null, null, "+this.positions+R_DRICNR+"}"
	);
}
public void testBug118397d() throws JavaModelException {
	String source =
		"package bugs.b118397;\n" +
		"/**\n" +
		" * @see BasicTestBugs.Inner.Lev.Level3\n" +
		" */\n" +
		"public class BasicTestBugs {\n" +
		"	class Inner {\n" +
		"		class Level2 {\n" +
		"			class Level3 {\n" +
		"			}\n" +
		"		}\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b118397/BasicTestBugs.java", source, true, "Lev");
	assertSortedResults(
		"BasicTestBugs.Inner.Level2[TYPE_REF]{Level2, bugs.b118397, Lbugs.b118397.BasicTestBugs$Inner$Level2;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

/**
 * bug 139621: [javadoc][assist] No Javadoc completions if there's no member below
 * test Ensure that completion happens in an orphan javadoc (ie. a javadoc comment not attached to a declaration
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=139621"
 */
public void testBug139621a() throws JavaModelException {
	String source =
		"package bugs.b139621;\n" +
		"public class BasicTestBugs {\n" +
		"	/**\n" +
		"	 * Test with only an orphan comment in type declaration\n" +
		"	 * @see Obj\n" +
		"	 */\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b139621/BasicTestBugs.java", source, true, "Obj");
	assertSortedResults(
		"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}
public void testBug139621b() throws JavaModelException {
	String source =
		"package bugs.b139621;\n" +
		"/**\n" +
		" * Test with an existing javadoc type declaration\n" +
		" * @see Test\n" +
		" */\n" +
		"public class BasicTestBugs {\n" +
		"	/**\n" +
		"	 * Test with only an orphan comment in type declaration\n" +
		"	 * @see Obj\n" +
		"	 */\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b139621/BasicTestBugs.java", source, true, "Obj");
	assertSortedResults(
		"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}
public void testBug139621c() throws JavaModelException {
	String source =
		"package bugs.b139621;\n" +
		"public class BasicTestBugs {\n" +
		"	/**\n" +
		"	 * Test with only an orphan comment in type declaration\n" +
		"	 * (completion for tags)\n" +
		"	 * @\n" +
		"	 */\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b139621/BasicTestBugs.java", source, true, "@");
	assertSortedResults(
		"author[JAVADOC_BLOCK_TAG]{@author, null, null, author, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"category[JAVADOC_BLOCK_TAG]{@category, null, null, category, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"deprecated[JAVADOC_BLOCK_TAG]{@deprecated, null, null, deprecated, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"exception[JAVADOC_BLOCK_TAG]{@exception, null, null, exception, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"inheritDoc[JAVADOC_INLINE_TAG]{{@inheritDoc}, null, null, inheritDoc, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"link[JAVADOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"param[JAVADOC_BLOCK_TAG]{@param, null, null, param, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"return[JAVADOC_BLOCK_TAG]{@return, null, null, return, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"see[JAVADOC_BLOCK_TAG]{@see, null, null, see, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"serial[JAVADOC_BLOCK_TAG]{@serial, null, null, serial, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"serialData[JAVADOC_BLOCK_TAG]{@serialData, null, null, serialData, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"serialField[JAVADOC_BLOCK_TAG]{@serialField, null, null, serialField, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"since[JAVADOC_BLOCK_TAG]{@since, null, null, since, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"throws[JAVADOC_BLOCK_TAG]{@throws, null, null, throws, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"value[JAVADOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"version[JAVADOC_BLOCK_TAG]{@version, null, null, version, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}
public void testBug139621d() throws JavaModelException {
	String source =
		"package bugs.b139621;\n" +
		"public class BasicTestBugs {\n" +
		"	/**\n" +
		"	 * Test with only an orphan comment in type declaration\n" +
		"	 * (variation of completion for tags but considered in text as it's in fact tag start character is after a reference )\n" +
		"	 * @see Object" + // missing \n
		"	 * @\n" +
		"	 */\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b139621/BasicTestBugs.java", source, true, "@", 2);
	assertSortedResults(
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"inheritDoc[JAVADOC_INLINE_TAG]{{@inheritDoc}, null, null, inheritDoc, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"link[JAVADOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"value[JAVADOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}
public void testBug139621e() throws JavaModelException {
	String source =
		"package bugs.b139621;\n" +
		"/**\n" +
		" * Test with orphan comment after a method declaration\n" +
		" * @see Test\n" +
		" */\n" +
		"public class BasicTestBugs {\n" +
		"	public void foo() {}\n" +
		"	/**\n" +
		"	 * This method returns an object\n" +
		"	 * @see Obj\n" +
		"	 */\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b139621/BasicTestBugs.java", source, true, "Obj");
	assertSortedResults(
		"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}
public void testBug139621f() throws JavaModelException {
	String source =
		"package bugs.b139621;\n" +
		"/**\n" +
		" * Test with orphan comment after a field declaration\n" +
		" * @see Test\n" +
		" */\n" +
		"public class BasicTestBugs {\n" +
		"	public int x;\n" +
		"	/**\n" +
		"	 * This method returns an object\n" +
		"	 * @see Obj\n" +
		"	 */\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b139621/BasicTestBugs.java", source, true, "Obj");
	assertSortedResults(
		"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}
public void testBug139621g() throws JavaModelException {
	String source =
		"package bugs.b139621;\n" +
		"/**\n" +
		" * Compilation unit without any type\n" +
		" * @see Obj\n" +
		" */\n";
	completeInJavadoc("/Completion/src/bugs/b139621/BasicTestBugs.java", source, true, "Obj");
	assertSortedResults(
		"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}
public void testBug139621h() throws JavaModelException {
	String source =
		"package bugs.b139621;\n" +
		"/**\n" +
		" * Compilation unit without any type\n" +
		" * (completion for tags)\n" +
		" * @\n" +
		" */\n";
	completeInJavadoc("/Completion/src/bugs/b139621/BasicTestBugs.java", source, true, "@");
	assertSortedResults(
		"author[JAVADOC_BLOCK_TAG]{@author, null, null, author, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"category[JAVADOC_BLOCK_TAG]{@category, null, null, category, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"deprecated[JAVADOC_BLOCK_TAG]{@deprecated, null, null, deprecated, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"link[JAVADOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"see[JAVADOC_BLOCK_TAG]{@see, null, null, see, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"serial[JAVADOC_BLOCK_TAG]{@serial, null, null, serial, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"since[JAVADOC_BLOCK_TAG]{@since, null, null, since, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"value[JAVADOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"version[JAVADOC_BLOCK_TAG]{@version, null, null, version, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}
public void testBug139621i() throws JavaModelException {
	String source =
		"package bugs.b139621;\n" +
		"/**\n" +
		" * Compilation unit without any type\n" +
		" * (variation of completion for tags but considered in text as it's in fact tag start character is after a reference )\n" +
		" * @see Object The root class" +  // missing \n
		" * @\n" +
		" */\n";
	completeInJavadoc("/Completion/src/bugs/b139621/BasicTestBugs.java", source, true, "@", 2);
	assertSortedResults(
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"link[JAVADOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"value[JAVADOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

/**
 * Bug 144866: [assist][javadoc] Wrong completion inside @value tag
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=144866"
 */
public void testBug144866() throws JavaModelException {
	String source =
		"package bugs.b144866;\n" +
		"public class BasicTestBugs {\n" +
		"	public static int EXAMPLE = 0;\n" +
		"	/**\n" +
		"	 * This method returns an object\n" +
		"	 * @see Object\n" +
		"	 * 	This method will use {@value #EX } constant value...\n" +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b144866/BasicTestBugs.java", source, true, "EX", 2); // 2nd occurence
	assertSortedResults(
		"EXAMPLE[FIELD_REF]{EXAMPLE, Lbugs.b144866.BasicTestBugs;, I, EXAMPLE, null, "+this.positions+R_DRICNR+"}"
	);
}

/**
 * bug 171016: [javadoc][assist] No completion for tag when uppercase is used
 * test Ensure that tag is proposed when prefix match a tag case insensitive
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=171016"
 */
public void testBug171016() throws JavaModelException {
	String source =
		"package bugs.b171016;\n" +
		"public class BasicTestBugs {\n" +
		"	public void foo() {}" +
		"}\n" +
		"class X extends BasicTestBugs {\n" +
		"	/**\n" +
		"	 * {@In\n" +
		"	 */\n" +
		"	public void foo() {}" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b171016/BasicTestBugs.java", source, true, "{@In", 1);
	assertSortedResults(
		"inheritDoc[JAVADOC_INLINE_TAG]{{@inheritDoc}, null, null, inheritDoc, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}
public void testBug171016b() throws JavaModelException {
	String source =
		"package bugs.b171016;\n" +
		"public class BasicTestBugs {\n" +
		"	public void foo() {}" +
		"}\n" +
		"class X extends BasicTestBugs {\n" +
		"	/**\n" +
		"	 * @In\n" +
		"	 */\n" +
		"	public void foo() {}" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b171016/BasicTestBugs.java", source, true, "@In", 1);
	assertSortedResults(
		"inheritDoc[JAVADOC_INLINE_TAG]{{@inheritDoc}, null, null, inheritDoc, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

/**
 * bug 171031: [javadoc][assist] 'inheritDoc' tag is proposed while completing even if the method does not override any
 * test Ensure that no 'inheritDoc' tag is proposed when method does not override any
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=171031"
 */
// TODO (eric) enable when bug will be fixed
public void _testBug171031() throws JavaModelException {
	String source =
		"package bugs.b171031;\n" +
		"public class BasicTestBugs {\n" +
		"	/**\n" +
		"	 * @In+\n" +
		"	 */\n" +
		"	public void foo() {}" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b171031/BasicTestBugs.java", source, true, "@In", 1);
	assertSortedResults(""); // should not have any proposal as
}

/**
 * bug 185576: [javadoc][assist] Type parameters should not be proposed while completing in @link or @see reference
 * test Do not include type params in Javadoc content assist proposals
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=185576"
 */
public void testBug185576a() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package bugs.b185576;\n" +
		"public class BasicTestBugs {\n" +
		"	/**\n" +
		"	 * The return type is {@link } and that is all.  \n" +
		"	 * \n" +
		"	 * @param <X>\n" +
		"	 * @param t\n" +
		"	 * @return something.\n" +
		"	 */\n" +
		"	public <X> X foooo(X t) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b185576/BasicTestBugs.java", source, true, "@link ", 0);
	String emptyPositions = "["+this.completionStart+", "+this.completionStart+"], ";
	assertSortedResults(
		"BasicTestBugs[TYPE_REF]{BasicTestBugs, bugs.b185576, Lbugs.b185576.BasicTestBugs;, null, null, "+emptyPositions+R_DRICUNR+"}"
	);
}
public void testBug185576b() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package bugs.b185576;\n" +
		"public class BasicTestBugs {\n" +
		"	/**\n" +
		"	 * The return type is {@link } and that is all.  \n" +
		"	 * \n" +
		"	 * @param <X>\n" +
		"	 * @param t\n" +
		"	 * @return something.\n" +
		"	 */\n" +
		"	public <X> X foooo(X t) {\n" +
		"		return null;\n" +
		"	}\n" +
		"  class X {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b185576/BasicTestBugs.java", source, true, "@link ", 0);
	String emptyPositions = "["+this.completionStart+", "+this.completionStart+"], ";
	assertSortedResults(
		"BasicTestBugs[TYPE_REF]{BasicTestBugs, bugs.b185576, Lbugs.b185576.BasicTestBugs;, null, null, "+emptyPositions+R_DRICUNR+"}\n" +
		"BasicTestBugs.X[TYPE_REF]{X, bugs.b185576, Lbugs.b185576.BasicTestBugs$X;, null, null, "+emptyPositions+R_DRICUNR+"}"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=249785
public void testBug249785a() throws JavaModelException {
	String source =
		"package bugs.b171016;\n" +
		"public class BasicTestBugs {\n" +
		"	/**\n" +
		"	 * @deprecated\n" +
		"	 */\n" +
		"	public int field\n" +
		"	/**\n" +
		"	 * @see #fie\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/bugs/b171016/BasicTestBugs.java", source, true, "fie", -1);
	assertSortedResults(
		"field[FIELD_REF]{field, Lbugs.b171016.BasicTestBugs;, I, field, null, "+this.positions+R_DRICNRNS+"}"
	);
}
/**
 * bug 255752 [javadoc][assist] Inappropriate completion proposals for javadoc at compilation unit level
 * test that there are no tag completions offered at the compilation unit level for a non package-info.java
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=255752"
 */
public void testBug255752() throws JavaModelException {
	String source =
		"/**\n" +
		" *\n" +
		" * @\n" +
		" */" +
		"package javadoc.bugs;\n" +
		"public class BasicTestBugs {}\n";
	completeInJavadoc("/Completion/src/javadoc/bugs/BasicTestBugs.java", source, true, "@", -1);
	assertSortedResults("");
}
/**
 * Additional tests for bug 255752
 * test whether an orphan Javadoc comment gets all the possible tags applicable to the class level.
 */
public void testBug255752a() throws JavaModelException {
	String source =
		"/**\n" +
		" *\n" +
		" * @\n" +
		" */" +
		"\n";
	completeInJavadoc("/Completion/src/javadoc/bugs/BasicTestBugs.java", source, true, "@", -1);
	assertResults(
			"author[JAVADOC_BLOCK_TAG]{@author, null, null, author, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
			"deprecated[JAVADOC_BLOCK_TAG]{@deprecated, null, null, deprecated, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
			"see[JAVADOC_BLOCK_TAG]{@see, null, null, see, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
			"version[JAVADOC_BLOCK_TAG]{@version, null, null, version, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
			"category[JAVADOC_BLOCK_TAG]{@category, null, null, category, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
			"since[JAVADOC_BLOCK_TAG]{@since, null, null, since, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
			"serial[JAVADOC_BLOCK_TAG]{@serial, null, null, serial, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
			"link[JAVADOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
			"docRoot[JAVADOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
			"linkplain[JAVADOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
			"value[JAVADOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}"
		);
}
}
