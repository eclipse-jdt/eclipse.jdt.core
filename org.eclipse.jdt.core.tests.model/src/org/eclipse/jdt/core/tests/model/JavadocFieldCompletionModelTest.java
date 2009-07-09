/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.Hashtable;

import junit.framework.Test;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Test class for completion in Javadoc comment of a field declaration.
 */
public class JavadocFieldCompletionModelTest extends AbstractJavadocCompletionModelTest {

public JavadocFieldCompletionModelTest(String name) {
	super(name);
}

static {
//	TESTS_RANGE = new int[] { 22, -1 };
//	TESTS_NUMBERS = new int[] { 16 };
}
public static Test suite() {
	return buildModelTestSuite(JavadocFieldCompletionModelTest.class);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelCompletionTests#setUp()
 */
protected void setUp() throws Exception {
	super.setUp();
	setUpProjectOptions(CompilerOptions.VERSION_1_4); // default compliance
}

/**
 * @category Tests for tag names completion
 */
public void test001() throws JavaModelException {
	String source =
		"package javadoc.fields;\n" +
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion on empty tag name:\n" +
		"	 * 	@\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/fields/Test.java", source, true, "@");
	assertResults(
		"deprecated[JAVADOC_BLOCK_TAG]{@deprecated, null, null, deprecated, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"see[JAVADOC_BLOCK_TAG]{@see, null, null, see, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"category[JAVADOC_BLOCK_TAG]{@category, null, null, category, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"since[JAVADOC_BLOCK_TAG]{@since, null, null, since, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"serial[JAVADOC_BLOCK_TAG]{@serial, null, null, serial, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"serialField[JAVADOC_BLOCK_TAG]{@serialField, null, null, serialField, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"link[JAVADOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"value[JAVADOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test002() throws JavaModelException {
	String source =
		"package javadoc.fields;\n" +
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion on impossible tag name:\n" +
		"	 * 	@thr\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/fields/Test.java", source, true, "@thr");
	assertResults("");
}

public void test003() throws JavaModelException {
	String source =
		"package javadoc.fields;\n" +
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion on one letter:\n" +
		"	 * 	@v\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/fields/Test.java", source, true, "@v");
	assertResults(
		"value[JAVADOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test004() throws JavaModelException {
	String source =
		"package javadoc.fields;\n" +
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion with several letters:\n" +
		"	 * 	@ser\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/fields/Test.java", source, true, "@ser");
	assertResults(
		"serial[JAVADOC_BLOCK_TAG]{@serial, null, null, serial, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"serialField[JAVADOC_BLOCK_TAG]{@serialField, null, null, serialField, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test005() throws JavaModelException {
	String source =
		"package javadoc.fields;\n" +
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion on full tag name:\n" +
		"	 * 	@docRoot\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/fields/Test.java", source, true, "@docRoot");
	assertResults(
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test006() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_3);
	String source =
		"package javadoc.fields;\n" +
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion on empty tag name:\n" +
		"	 * 	@\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/fields/Test.java", source, true, "@");
	assertResults(
		"deprecated[JAVADOC_BLOCK_TAG]{@deprecated, null, null, deprecated, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"see[JAVADOC_BLOCK_TAG]{@see, null, null, see, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"category[JAVADOC_BLOCK_TAG]{@category, null, null, category, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"since[JAVADOC_BLOCK_TAG]{@since, null, null, since, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"serial[JAVADOC_BLOCK_TAG]{@serial, null, null, serial, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"serialField[JAVADOC_BLOCK_TAG]{@serialField, null, null, serialField, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"link[JAVADOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test007() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.fields;\n" +
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion on empty tag name:\n" +
		"	 * 	@\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/fields/Test.java", source, true, "@");
	assertResults(
		"deprecated[JAVADOC_BLOCK_TAG]{@deprecated, null, null, deprecated, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"see[JAVADOC_BLOCK_TAG]{@see, null, null, see, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"category[JAVADOC_BLOCK_TAG]{@category, null, null, category, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"since[JAVADOC_BLOCK_TAG]{@since, null, null, since, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"serial[JAVADOC_BLOCK_TAG]{@serial, null, null, serial, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"serialField[JAVADOC_BLOCK_TAG]{@serialField, null, null, serialField, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"link[JAVADOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"value[JAVADOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"code[JAVADOC_INLINE_TAG]{{@code}, null, null, code, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"literal[JAVADOC_INLINE_TAG]{{@literal}, null, null, literal, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

/**
 * @category Tests for types completion
 */
public void test010() throws JavaModelException {
	String source =
		"package javadoc.fields.tags;\n" +
		"public class BasicTestFields {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see Obj\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/fields/tags/BasicTestFields.java", source, true, "Obj");
	assertResults(
		"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}

public void test011() throws JavaModelException {
	String source =
		"package javadoc.fields.tags;\n" +
		"public class BasicTestFields {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see BasicTest\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/fields/tags/BasicTestFields.java", source, true, "BasicTest", 2);
	assertResults(
		"BasicTestFields[TYPE_REF]{BasicTestFields, javadoc.fields.tags, Ljavadoc.fields.tags.BasicTestFields;, null, null, "+this.positions+R_DRICUNR+"}\n" +
		"BasicTestReferences[TYPE_REF]{org.eclipse.jdt.core.tests.BasicTestReferences, org.eclipse.jdt.core.tests, Lorg.eclipse.jdt.core.tests.BasicTestReferences;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test012() throws JavaModelException {
	String source =
		"package javadoc.fields.tags;\n" +
		"public class BasicTestFields {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see javadoc.fields.tags.BasicTest\n" +
		"	 * 		Note: JDT-UI failed on this one\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/fields/tags/BasicTestFields.java", source, true, "javadoc.fields.tags.BasicTest");
	assertResults(
		"BasicTestFields[TYPE_REF]{BasicTestFields, javadoc.fields.tags, Ljavadoc.fields.tags.BasicTestFields;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test013() throws JavaModelException {
	String source =
		"package javadoc.fields.tags;\n" +
		"public class BasicTestFields {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see java.la\n" +
		"	 * 		Note: JDT-UI fails on this one\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/fields/tags/BasicTestFields.java", source, true, "java.la");
	assertResults(
		"java.lang[PACKAGE_REF]{java.lang, java.lang, null, null, null, "+this.positions+R_DRICQNR+"}"
	);
}

public void test014() throws JavaModelException {
	String source =
		"package javadoc.fields.tags;\n" +
		"public class BasicTestFields {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see pack.Bin\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/fields/tags/BasicTestFields.java", source, true, "pack.Bin");
	assertSortedResults(
		"Bin1[TYPE_REF]{pack.Bin1, pack, Lpack.Bin1;, null, null, "+this.positions+R_DRICQNR+"}\n" +
		"Bin2[TYPE_REF]{pack.Bin2, pack, Lpack.Bin2;, null, null, "+this.positions+R_DRICQNR+"}\n" +
		"Bin3[TYPE_REF]{pack.Bin3, pack, Lpack.Bin3;, null, null, "+this.positions+R_DRICQNR+"}\n" +
		"Bin4[TYPE_REF]{pack.Bin4, pack, Lpack.Bin4;, null, null, "+this.positions+R_DRICQNR+"}\n" +
		"Bin5[TYPE_REF]{pack.Bin5, pack, Lpack.Bin5;, null, null, "+this.positions+R_DRICQNR+"}"
	);
}

public void test015() throws JavaModelException {
	String source =
		"package javadoc.fields.tags;\n" +
		"public class BasicTestFields {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see I\n" +
		"	 * 		Note: completion list shoud not include base types.\n" +
		"	 */\n" +
		"	int field;\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/fields/tags/BasicTestFields.java", source, true, "I");
	assertSortedResults(
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+R_DRICUNR+"}\n" +
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}

/**
 * @category Tests for fields completion
 */
public void test020() throws JavaModelException {
	String source =
		"package javadoc.fields.tags;\n" +
		"public class BasicTestFields {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #fo\n" +
		"	 */\n" +
		"	int foo;\n" +
		"}";
	completeInJavadoc("/Completion/src/javadoc/fields/tags/BasicTestFields.java", source, true, "fo");
	assertResults(
		"foo[FIELD_REF]{foo, Ljavadoc.fields.tags.BasicTestFields;, I, foo, null, "+this.positions+R_DRICNRNS+"}"
	);
}

public void test021() throws JavaModelException {
	String source =
		"package javadoc.fields.tags;\n" +
		"public class BasicTestFields {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see BasicTestFields#fo\n" +
		"	 */\n" +
		"	int foo;\n" +
		"}";
	completeInJavadoc("/Completion/src/javadoc/fields/tags/BasicTestFields.java", source, true, "fo");
	assertResults(
		"foo[FIELD_REF]{foo, Ljavadoc.fields.tags.BasicTestFields;, I, foo, null, "+this.positions+R_DRICNRNS+"}"
	);
}

public void test022() throws JavaModelException {
	String source =
		"package javadoc.fields.tags;\n" +
		"public class BasicTestFields {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see javadoc.fields.tags.BasicTestFields#fo\n" +
		"	 */\n" +
		"	int foo;\n" +
		"}";
	completeInJavadoc("/Completion/src/javadoc/fields/tags/BasicTestFields.java", source, true, "fo");
	assertResults(
		"foo[FIELD_REF]{foo, Ljavadoc.fields.tags.BasicTestFields;, I, foo, null, "+this.positions+R_DRICNRNS+"}"
	);
}

public void test023() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/fields/tags/BasicTestFields.java",
			"package javadoc.fields.tags;\n" +
			"public class BasicTestFields {\n" +
			"	/**\n" +
			"	 * Completion after:\n" +
			"	 * 	@see OtherFields#oth\n" +
			"	 */\n" +
			"	int foo;\n" +
			"}",
		"/Completion/src/javadoc/fields/tags/OtherFields.java",
			"package javadoc.fields.tags;\n" +
			"public class OtherFields {\n" +
			"	int other;\n" +
			"}"
	};
	completeInJavadoc(sources, true, "oth");
	assertResults(
		"other[FIELD_REF]{other, Ljavadoc.fields.tags.OtherFields;, I, other, null, "+this.positions+R_DRICNRNS+"}"
	);
}

public void test024() throws JavaModelException {
	String source =
		"package javadoc.fields.tags;\n" +
		"public class BasicTestFields {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #\n" +
		"	 */\n" +
		"	int foo;\n" +
		"	Object obj;\n" +
		"}";
	completeInJavadoc("/Completion/src/javadoc/fields/tags/BasicTestFields.java", source, true, "#", 0); // completion on empty token
	assertResults(
		"obj[FIELD_REF]{obj, Ljavadoc.fields.tags.BasicTestFields;, Ljava.lang.Object;, obj, null, "+this.positions+R_DRICNRNS+"}\n" +
		"foo[FIELD_REF]{foo, Ljavadoc.fields.tags.BasicTestFields;, I, foo, null, "+this.positions+R_DRICNRNS+"}\n" +
		"wait[METHOD_REF]{wait(long, int), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+this.positions+R_DRICNRNS+"}\n" +
		"wait[METHOD_REF]{wait(long), Ljava.lang.Object;, (J)V, wait, (millis), "+this.positions+R_DRICNRNS+"}\n" +
		"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+this.positions+R_DRICNRNS+"}\n" +
		"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DRICNRNS+"}\n" +
		"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+this.positions+R_DRICNRNS+"}\n" +
		"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+this.positions+R_DRICNRNS+"}\n" +
		"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+this.positions+R_DRICNRNS+"}\n" +
		"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class;, getClass, null, "+this.positions+R_DRICNRNS+"}\n" +
		"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+this.positions+R_DRICNRNS+"}\n" +
		"equals[METHOD_REF]{equals(Object), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+this.positions+R_DRICNRNS+"}\n" +
		"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+this.positions+R_DRICNRNS+"}\n" +
		"BasicTestFields[METHOD_REF<CONSTRUCTOR>]{BasicTestFields(), Ljavadoc.fields.tags.BasicTestFields;, ()V, BasicTestFields, null, "+this.positions+R_DRINR+"}"
	);
}

public void test025() throws JavaModelException {
	String source =
		"package javadoc.fields.tags;\n" +
		"public class BasicTestFields {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see BasicTestFields#\n" +
		"	 */\n" +
		"	int foo;\n" +
		"	Object obj;\n" +
		"}";
	completeInJavadoc("/Completion/src/javadoc/fields/tags/BasicTestFields.java", source, true, "#", 0); // completion on empty token
	assertResults(
		"obj[FIELD_REF]{obj, Ljavadoc.fields.tags.BasicTestFields;, Ljava.lang.Object;, obj, null, "+this.positions+R_DRICNRNS+"}\n" +
		"foo[FIELD_REF]{foo, Ljavadoc.fields.tags.BasicTestFields;, I, foo, null, "+this.positions+R_DRICNRNS+"}\n" +
		"wait[METHOD_REF]{wait(long, int), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+this.positions+R_DRICNRNS+"}\n" +
		"wait[METHOD_REF]{wait(long), Ljava.lang.Object;, (J)V, wait, (millis), "+this.positions+R_DRICNRNS+"}\n" +
		"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+this.positions+R_DRICNRNS+"}\n" +
		"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DRICNRNS+"}\n" +
		"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+this.positions+R_DRICNRNS+"}\n" +
		"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+this.positions+R_DRICNRNS+"}\n" +
		"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+this.positions+R_DRICNRNS+"}\n" +
		"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class;, getClass, null, "+this.positions+R_DRICNRNS+"}\n" +
		"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+this.positions+R_DRICNRNS+"}\n" +
		"equals[METHOD_REF]{equals(Object), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+this.positions+R_DRICNRNS+"}\n" +
		"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+this.positions+R_DRICNRNS+"}\n" +
		"BasicTestFields[METHOD_REF<CONSTRUCTOR>]{BasicTestFields(), Ljavadoc.fields.tags.BasicTestFields;, ()V, BasicTestFields, null, "+this.positions+R_DRINR+"}"
	);
}

public void test026() throws JavaModelException {
	String source =
		"package javadoc.fields.tags;\n" +
		"public class BasicTestFields {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see javadoc.fields.tags.BasicTestFields#\n" +
		"	 */\n" +
		"	int foo;\n" +
		"	Object obj;\n" +
		"}";
	completeInJavadoc("/Completion/src/javadoc/fields/tags/BasicTestFields.java", source, true, "#", 0); // completion on empty token
	assertResults(
		"obj[FIELD_REF]{obj, Ljavadoc.fields.tags.BasicTestFields;, Ljava.lang.Object;, obj, null, "+this.positions+R_DRICNRNS+"}\n" +
		"foo[FIELD_REF]{foo, Ljavadoc.fields.tags.BasicTestFields;, I, foo, null, "+this.positions+R_DRICNRNS+"}\n" +
		"wait[METHOD_REF]{wait(long, int), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+this.positions+R_DRICNRNS+"}\n" +
		"wait[METHOD_REF]{wait(long), Ljava.lang.Object;, (J)V, wait, (millis), "+this.positions+R_DRICNRNS+"}\n" +
		"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+this.positions+R_DRICNRNS+"}\n" +
		"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DRICNRNS+"}\n" +
		"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+this.positions+R_DRICNRNS+"}\n" +
		"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+this.positions+R_DRICNRNS+"}\n" +
		"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+this.positions+R_DRICNRNS+"}\n" +
		"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class;, getClass, null, "+this.positions+R_DRICNRNS+"}\n" +
		"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+this.positions+R_DRICNRNS+"}\n" +
		"equals[METHOD_REF]{equals(Object), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+this.positions+R_DRICNRNS+"}\n" +
		"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+this.positions+R_DRICNRNS+"}\n" +
		"BasicTestFields[METHOD_REF<CONSTRUCTOR>]{BasicTestFields(), Ljavadoc.fields.tags.BasicTestFields;, ()V, BasicTestFields, null, "+this.positions+R_DRINR+"}"
	);
}

public void test027() throws JavaModelException {
	String source =
		"package javadoc.fields.tags;\n" +
		"public class BasicTestFields {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see BasicTestReferences#FIE\n" +
		"	 */\n" +
		"	int foo;\n" +
		"	Object obj;\n" +
		"}";
	completeInJavadoc("/Completion/src/javadoc/fields/tags/BasicTestFields.java", source, true, "FIE");
	assertResults("");
}

public void test028() throws JavaModelException {
	String source =
		"package javadoc.fields.tags;\n" +
		"public class BasicTestFields {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see org.eclipse.jdt.core.tests.BasicTestReferences#FIE\n" +
		"	 */\n" +
		"	int foo;\n" +
		"	Object obj;\n" +
		"}";
	completeInJavadoc("/Completion/src/javadoc/fields/tags/BasicTestFields.java", source, true, "FIE");
	assertResults(
		"FIELD[FIELD_REF]{FIELD, Lorg.eclipse.jdt.core.tests.BasicTestReferences;, I, FIELD, null, "+this.positions+R_DRICNR+"}"
	);
}
/**
 * @tests Tests for camel case completion
 */
public void test030() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_CAMEL_CASE_MATCH, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		String source =
			"package javadoc.fields.tags;\n" +
			"public class BasicTestFields {\n" +
			"	Object oneTwoThree;\n" +
			"	/**\n" +
			"	 * Completion after:\n" +
			"	 * 	@see #oTT\n" +
			"	 */\n" +
			"	int foo;\n" +

			"}";
		completeInJavadoc("/Completion/src/javadoc/fields/tags/BasicTestFields.java", source, true, "oTT");
		assertResults(
			"oneTwoThree[FIELD_REF]{oneTwoThree, Ljavadoc.fields.tags.BasicTestFields;, Ljava.lang.Object;, oneTwoThree, null, "+this.positions+"30}"
		);
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
}
