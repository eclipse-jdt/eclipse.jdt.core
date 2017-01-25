/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Test class for completion in Javadoc comment of a method declaration.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class JavadocMethodCompletionModelTest extends AbstractJavadocCompletionModelTest {

public JavadocMethodCompletionModelTest(String name) {
	super(name);
}

static {
//	TESTS_NUMBERS = new int[] { 58 };
//	TESTS_RANGE = new int[] { 58, 69 };
}
public static Test suite() {
	return buildModelTestSuite(JavadocMethodCompletionModelTest.class);
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.AbstractJavadocCompletionModelTest#setUp()
 */
protected void setUp() throws Exception {
	super.setUp();
	setUpProjectOptions(CompilerOptions.VERSION_1_4);
}

/**
 * @tests Tests for tag names completion
 */
public void test001() throws JavaModelException {
	String source =
		"package javadoc.methods;\n" +
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion on empty tag name:\n" +
		"	 * 	@\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/Test.java", source, true, "@");
	assertResults(
		"deprecated[JAVADOC_BLOCK_TAG]{@deprecated, null, null, deprecated, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"exception[JAVADOC_BLOCK_TAG]{@exception, null, null, exception, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"param[JAVADOC_BLOCK_TAG]{@param, null, null, param, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"return[JAVADOC_BLOCK_TAG]{@return, null, null, return, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"see[JAVADOC_BLOCK_TAG]{@see, null, null, see, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"category[JAVADOC_BLOCK_TAG]{@category, null, null, category, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"since[JAVADOC_BLOCK_TAG]{@since, null, null, since, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"serialData[JAVADOC_BLOCK_TAG]{@serialData, null, null, serialData, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"throws[JAVADOC_BLOCK_TAG]{@throws, null, null, throws, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"link[JAVADOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"inheritDoc[JAVADOC_INLINE_TAG]{{@inheritDoc}, null, null, inheritDoc, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"value[JAVADOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test002() throws JavaModelException {
	String source =
		"package javadoc.methods;\n" +
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion on impossible tag name:\n" +
		"	 * 	@aut\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/Test.java", source, true, "@aut");
	assertResults("");
}

public void test003() throws JavaModelException {
	String source =
		"package javadoc.methods;\n" +
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion on one letter:\n" +
		"	 * 	@r\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/Test.java", source, true, "@r");
	assertResults(
		"return[JAVADOC_BLOCK_TAG]{@return, null, null, return, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test004() throws JavaModelException {
	String source =
		"package javadoc.methods;\n" +
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion with several letters:\n" +
		"	 * 	@ser\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/Test.java", source, true, "@ser");
	assertResults(
		"serialData[JAVADOC_BLOCK_TAG]{@serialData, null, null, serialData, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test005() throws JavaModelException {
	String source =
		"package javadoc.methods;\n" +
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion on full tag name:\n" +
		"	 * 	@inheritDoc\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/Test.java", source, true, "@inheritDoc");
	assertResults(
		"inheritDoc[JAVADOC_INLINE_TAG]{{@inheritDoc}, null, null, inheritDoc, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test006() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_3);
	String source =
		"package javadoc.methods;\n" +
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion on empty tag name:\n" +
		"	 * 	@\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/Test.java", source, true, "@");
	assertResults(
		"deprecated[JAVADOC_BLOCK_TAG]{@deprecated, null, null, deprecated, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"exception[JAVADOC_BLOCK_TAG]{@exception, null, null, exception, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"param[JAVADOC_BLOCK_TAG]{@param, null, null, param, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"return[JAVADOC_BLOCK_TAG]{@return, null, null, return, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"see[JAVADOC_BLOCK_TAG]{@see, null, null, see, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"category[JAVADOC_BLOCK_TAG]{@category, null, null, category, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"since[JAVADOC_BLOCK_TAG]{@since, null, null, since, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"serialData[JAVADOC_BLOCK_TAG]{@serialData, null, null, serialData, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"throws[JAVADOC_BLOCK_TAG]{@throws, null, null, throws, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"link[JAVADOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test007() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods;\n" +
		"public class Test {\n" +
		"	/**\n" +
		"	 * Completion on empty tag name:\n" +
		"	 * 	@\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/Test.java", source, true, "@");
	assertResults(
		"deprecated[JAVADOC_BLOCK_TAG]{@deprecated, null, null, deprecated, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"exception[JAVADOC_BLOCK_TAG]{@exception, null, null, exception, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"param[JAVADOC_BLOCK_TAG]{@param, null, null, param, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"return[JAVADOC_BLOCK_TAG]{@return, null, null, return, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"see[JAVADOC_BLOCK_TAG]{@see, null, null, see, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"category[JAVADOC_BLOCK_TAG]{@category, null, null, category, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"since[JAVADOC_BLOCK_TAG]{@since, null, null, since, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"serialData[JAVADOC_BLOCK_TAG]{@serialData, null, null, serialData, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"throws[JAVADOC_BLOCK_TAG]{@throws, null, null, throws, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"link[JAVADOC_INLINE_TAG]{{@link}, null, null, link, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"docRoot[JAVADOC_INLINE_TAG]{{@docRoot}, null, null, docRoot, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"inheritDoc[JAVADOC_INLINE_TAG]{{@inheritDoc}, null, null, inheritDoc, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"linkplain[JAVADOC_INLINE_TAG]{{@linkplain}, null, null, linkplain, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"value[JAVADOC_INLINE_TAG]{{@value}, null, null, value, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"code[JAVADOC_INLINE_TAG]{{@code}, null, null, code, null, "+this.positions+JAVADOC_RELEVANCE+"}\n" +
		"literal[JAVADOC_INLINE_TAG]{{@literal}, null, null, literal, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

/**
 * @tests Tests for types completion
 */
public void test010() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see BasicTestMethodsE\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n" +
		"class BasicTestMethodsException1 extends Exception{}\n" +
		"class BasicTestMethodsException2 extends Exception{}\n" +
		"class BasicTestMethodsExample {\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMethodsE");
	assertSortedResults(
		"BasicTestMethodsExample[TYPE_REF]{BasicTestMethodsExample, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsExample;, null, null, "+this.positions+R_DRICUNR+"}\n" +
		"BasicTestMethodsException1[TYPE_REF]{BasicTestMethodsException1, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsException1;, null, null, "+this.positions+R_DRICUNR+"}\n" +
		"BasicTestMethodsException2[TYPE_REF]{BasicTestMethodsException2, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsException2;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}

public void test011() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see \n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n" +
		"class BasicTestMethodsException1 extends Exception{}\n" +
		"class BasicTestMethodsException2 extends Exception{}\n" +
		"class BasicTestMethodsExample {\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@see ", 0); // completion on empty token
	if(CompletionEngine.NO_TYPE_COMPLETION_ON_EMPTY_TOKEN) {
		assertResults("");
	} else {
		assertResults(
			"BasicTestMethods[TYPE_REF]{BasicTestMethods, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethods;, null, null, "+this.positions+R_DRICUNR+"}\n" +
			"BasicTestMethodsException1[TYPE_REF]{BasicTestMethodsException1, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsException1;, null, null, "+this.positions+R_DRICUNR+"}\n" +
			"BasicTestMethodsException2[TYPE_REF]{BasicTestMethodsException2, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsException2;, null, null, "+this.positions+R_DRICUNR+"}\n" +
			"BasicTestMethodsExample[TYPE_REF]{BasicTestMethodsExample, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsExample;, null, null, "+this.positions+R_DRICUNR+"}");
	}
}

public void test012() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@throws BasicTestMethodsE\n" +
		"	 */\n" +
		"	public void foo() {}\n" +
		"}\n" +
		"class BasicTestMethodsException1 extends Exception{}\n" +
		"class BasicTestMethodsException2 extends Exception{}\n" +
		"class BasicTestMethodsExample {\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMethodsE");
	assertSortedResults(
		"BasicTestMethodsException1[TYPE_REF]{BasicTestMethodsException1, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsException1;, null, null, "+this.positions+R_DRICUNRE+"}\n" +
		"BasicTestMethodsException2[TYPE_REF]{BasicTestMethodsException2, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsException2;, null, null, "+this.positions+R_DRICUNRE+"}\n" +
		"BasicTestMethodsExample[TYPE_REF]{BasicTestMethodsExample, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsExample;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}

public void test013() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@throws BasicTestMethodsE\n" +
		"	 */\n" +
		"	public void foo() throws BasicTestMethodsException2 {}\n" +
		"}\n" +
		"class BasicTestMethodsException1 extends Exception{}\n" +
		"class BasicTestMethodsException2 extends Exception{}\n" +
		"class BasicTestMethodsExample {\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMethodsE");
	assertSortedResults(
		"BasicTestMethodsException2[TYPE_REF]{BasicTestMethodsException2, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsException2;, null, null, "+this.positions+R_DRICUNREEET+"}\n" +
		"BasicTestMethodsException1[TYPE_REF]{BasicTestMethodsException1, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsException1;, null, null, "+this.positions+R_DRICUNRE+"}\n" +
		"BasicTestMethodsExample[TYPE_REF]{BasicTestMethodsExample, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsExample;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}

public void test014() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@throws \n" +
		"	 */\n" +
		"	public void foo() throws BasicTestMethodsException {}\n" +
		"}\n" +
		"class BasicTestMethodsException extends Exception{}\n" +
		"class BasicTestMethodsExample {\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@throws ", 0); // completion on empty token
	if(CompletionEngine.NO_TYPE_COMPLETION_ON_EMPTY_TOKEN) {
		assertResults(
			"BasicTestMethodsException[TYPE_REF]{BasicTestMethodsException, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsException;, null, null, "+this.positions+R_DRICUNREET+"}"
		);
	} else {
		assertResults(
			"BasicTestMethods[TYPE_REF]{BasicTestMethods, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethods;, null, null, "+this.positions+R_DRICUNR+"}\n" +
			"BasicTestMethodsException[TYPE_REF]{BasicTestMethodsException, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsException;, null, null, "+this.positions+R_DRICUNREEET+"}\n" +
			"BasicTestMethodsExample[TYPE_REF]{BasicTestMethodsExample, javadoc.methods.tags, Ljavadoc.methods.tags.BasicTestMethodsExample;, null, null, "+this.positions+R_DRICUNR+"}");
	}
}

public void test015() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@throws I\n" +
		"	 * 		Note: there should be NO base types in proposals." +
		"	 */\n" +
		"	public void foo() {\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "I");
	assertSortedResults(
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+R_DRICUNRE+"}\n" +
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DRICUNRE+"}"
	);
}

public void test016() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@throws java.lang.I\n" +
		"	 */\n" +
		"	public void foo() throws InterruptedException {\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "java.lang.I");
	assertSortedResults(
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DRICNREEET+"}\n" +
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+R_DRICNRE+"}"
	);
}

/**
 * @tests Tests for fields completion
 */
public void test020() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #fo\n" +
		"	 */\n" +
		"	int foo;\n" +
		"	void foo() {}\n" +
		"}";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "fo");
	assertResults(
		"foo[FIELD_REF]{foo, Ljavadoc.methods.tags.BasicTestMethods;, I, foo, null, "+this.positions+R_DRICNRNS+"}\n" +
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, foo, null, "+this.positions+R_DRICNRNS+"}"
	);
}

public void test021() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see BasicTestMethods#fo\n" +
		"	 */\n" +
		"	int foo;\n" +
		"	void foo() {}\n" +
		"}";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "fo");
	assertResults(
		"foo[FIELD_REF]{foo, Ljavadoc.methods.tags.BasicTestMethods;, I, foo, null, "+this.positions+R_DRICNRNS+"}\n" +
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, foo, null, "+this.positions+R_DRICNRNS+"}"
	);
}

public void test022() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see javadoc.methods.tags.BasicTestMethods#fo\n" +
		"	 */\n" +
		"	int foo;\n" +
		"	void foo() {}\n" +
		"}";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "fo");
	assertResults(
		"foo[FIELD_REF]{foo, Ljavadoc.methods.tags.BasicTestMethods;, I, foo, null, "+this.positions+R_DRICNRNS+"}\n" +
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, foo, null, "+this.positions+R_DRICNRNS+"}"
	);
}

public void test023() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/methods/tags/BasicTestMethods.java",
			"package javadoc.methods.tags;\n" +
			"public class BasicTestMethods {\n" +
			"	/**\n" +
			"	 * Completion after:\n" +
			"	 * 	@see OtherFields#fo\n" +
			"	 */\n" +
			"	int foo;\n" +
			"}",
		"/Completion/src/javadoc/methods/tags/OtherFields.java",
			"package javadoc.methods.tags;\n" +
			"public class OtherFields {\n" +
			"	int foo;\n" +
			"	void foo() {}\n" +
			"}"
	};
	completeInJavadoc(sources, true, "fo");
	assertResults(
		"foo[FIELD_REF]{foo, Ljavadoc.methods.tags.OtherFields;, I, foo, null, "+this.positions+R_DRICNRNS+"}\n" +
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.OtherFields;, ()V, foo, null, "+this.positions+R_DRICNRNS+"}"
	);
}

/**
 * @tests Tests for methods completion
 */
public void test030() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see fo\n" +
		"	 */\n" +
		"	void foo() {}\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "fo");
	assertResults("");
}

public void test031() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #fo\n" +
		"	 */\n" +
		"	<T> void foo() {}\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "fo");
	assertResults(
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.BasicTestMethods;, <T:Ljava.lang.Object;>()V, foo, null, "+this.positions+R_DRICNRNS+"}"
	);
}

public void test032() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #ba\n" +
		"	 * \n" +
		"	 * Note that argument names are put in proposals although there are not while completing\n" +
		"	 * in javadoc text {@link javadoc.text.BasicTestMethods }. This is due to the fact that while\n" +
		"	 * completing in javadoc tags, it\'s JDT-UI which compute arguments, not JDT-CORE.\n" +
		"	 */\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "ba");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+R_DRICNRNS+"}"
	);
}

public void test033() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #ba\n" +
		"	 * \n" +
		"	 * Note that argument names are put in proposals although there are not while completing\n" +
		"	 * in javadoc text {@link javadoc.text.BasicTestMethods }. This is due to the fact that while\n" +
		"	 * completing in javadoc tags, it\'s JDT-UI which compute arguments, not JDT-CORE.\n" +
		"	 */\n" +
		"	<T, U> void bar(String str, Class<T> clt, Class<U> clu) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "ba");
	assertResults(
		"bar[METHOD_REF]{bar(String, Class, Class), Ljavadoc.methods.tags.BasicTestMethods;, <T:Ljava.lang.Object;U:Ljava.lang.Object;>(Ljava.lang.String;Ljava.lang.Class<TT;>;Ljava.lang.Class<TU;>;)V, bar, (str, clt, clu), "+this.positions+R_DRICNRNS+"}"
	);
}

public void test034() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see BasicTestMethods#ba\n" +
		"	 * \n" +
		"	 * Note that argument names are put in proposals although there are not while completing\n" +
		"	 * in javadoc text {@link javadoc.text.BasicTestMethods }. This is due to the fact that while\n" +
		"	 * completing in javadoc tags, it\'s JDT-UI which compute arguments, not JDT-CORE.\n" +
		"	 */\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "ba");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+R_DRICNRNS+"}"
	);
}

public void test035() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see javadoc.methods.tags.BasicTestMethods#ba\n" +
		"	 * \n" +
		"	 * Note that argument names are put in proposals although there are not while completing\n" +
		"	 * in javadoc text {@link javadoc.text.BasicTestMethods }. This is due to the fact that while\n" +
		"	 * completing in javadoc tags, it\'s JDT-UI which compute arguments, not JDT-CORE.\n" +
		"	 */\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "ba");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+R_DRICNRNS+"}"
	);
}

public void test036() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/methods/tags/BasicTestMethods.java",
			"package javadoc.methods.tags;\n" +
			"public class BasicTestMethods {\n" +
			"	/**\n" +
			"	 * Completion after:\n" +
			"	 * 	@see OtherTypes#fo\n" +
			"	 */\n" +
			"	void foo() {};\n" +
			"}",
		"/Completion/src/javadoc/methods/tags/OtherTypes.java",
			"package javadoc.methods.tags;\n" +
			"public class OtherTypes {\n" +
			"	void foo() {};\n" +
			"}"
	};
	completeInJavadoc(sources, true, "fo");
	assertResults(
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.OtherTypes;, ()V, foo, null, "+this.positions+R_DRICNRNS+"}"
	);
}

public void test037() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #\n" +
		"	 */\n" +
		"	void foo() {}\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "#", 0); // completion on empty token
	assertResults(
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, foo, null, "+this.positions+R_DRICNRNS+"}\n" +
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+R_DRICNRNS+"}\n" +
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
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+R_DRINR+"}"
	);
}

public void test038() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #\n" +
		"	 */\n" +
		"	<T> void foo() {}\n" +
		"	<TParam1, TParam2> void bar(TParam1 tp1, TParam2 tp2) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "#", 0); // completion on empty token
	assertResults(
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.BasicTestMethods;, <T:Ljava.lang.Object;>()V, foo, null, "+this.positions+R_DRICNRNS+"}\n" +
		"bar[METHOD_REF]{bar(Object, Object), Ljavadoc.methods.tags.BasicTestMethods;, <TParam1:Ljava.lang.Object;TParam2:Ljava.lang.Object;>(TTParam1;TTParam2;)V, bar, (tp1, tp2), "+this.positions+R_DRICNRNS+"}\n" +
		"wait[METHOD_REF]{wait(long, int), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+this.positions+R_DRICNRNS+"}\n" +
		"wait[METHOD_REF]{wait(long), Ljava.lang.Object;, (J)V, wait, (millis), "+this.positions+R_DRICNRNS+"}\n" +
		"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+this.positions+R_DRICNRNS+"}\n" +
		"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DRICNRNS+"}\n" +
		"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+this.positions+R_DRICNRNS+"}\n" +
		"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+this.positions+R_DRICNRNS+"}\n" +
		"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+this.positions+R_DRICNRNS+"}\n" +
		"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, "+this.positions+R_DRICNRNS+"}\n" +
		"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+this.positions+R_DRICNRNS+"}\n" +
		"equals[METHOD_REF]{equals(Object), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+this.positions+R_DRICNRNS+"}\n" +
		"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+this.positions+R_DRICNRNS+"}\n" +
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+R_DRINR+"}"
	);
}

public void test039() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see BasicTestMethods#\n" +
		"	 */\n" +
		"	void foo() {}\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "#", 0); // completion on empty token
	assertResults(
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, foo, null, "+this.positions+R_DRICNRNS+"}\n" +
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+R_DRICNRNS+"}\n" +
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
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+R_DRINR+"}"
	);
}

public void test040() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see javadoc.methods.tags.BasicTestMethods#\n" +
		"	 */\n" +
		"	void foo() {}\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "#", 0); // completion on empty token
	assertResults(
		"foo[METHOD_REF]{foo(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, foo, null, "+this.positions+R_DRICNRNS+"}\n" +
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+R_DRICNRNS+"}\n" +
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
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+R_DRINR+"}"
	);
}

public void test041() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #bar(\n" +
		"	 */\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+R_DRICENUNR+"}"
	);
}

public void test042() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #bar(Str\n" +
		"	 */\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "Str");
	assertResults(
		"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}

public void test043() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #bar(java.lang.\n" +
		"	 */\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "java.lang.");
	assertSortedResults(
		"Class[TYPE_REF]{Class, java.lang, Ljava.lang.Class;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"CloneNotSupportedException[TYPE_REF]{CloneNotSupportedException, java.lang, Ljava.lang.CloneNotSupportedException;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Error[TYPE_REF]{Error, java.lang, Ljava.lang.Error;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Exception[TYPE_REF]{Exception, java.lang, Ljava.lang.Exception;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"RuntimeException[TYPE_REF]{RuntimeException, java.lang, Ljava.lang.RuntimeException;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Throwable[TYPE_REF]{Throwable, java.lang, Ljava.lang.Throwable;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test044() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #bar(java.lang.St\n" +
		"	 */\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "java.lang.St");
	assertResults(
		"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test045() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #bar(String s\n" +
		"	 */\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(String s");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+R_DRICENUNR+"}"
	);
}

public void test046() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #bar(String str, \n" +
		"	 */\n" +
		"	// Note: Completion takes place just after trailoing comma (there's a space after)\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(String str,");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+R_DRICENUNR+"}"
	);
}

public void test047() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #bar(String str, \n" +
		"	 */\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(String str, ");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+R_DRICENUNR+"}"
	);
}

public void test048() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #bar(String,\n" +
		"	 */\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(String,");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+R_DRICENUNR+"}"
	);
}

public void test049() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #bar(String str, bool\n" +
		"	 */\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bool");
	assertResults(
		"boolean[KEYWORD]{boolean, null, null, boolean, null, "+this.positions+R_DRICNR+"}"
	);
}

/*
 * Specific case where we can complete but we don't want to as the prefix is not syntaxically correct
 */
public void test050() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #bar(String str, boolean,\n" +
		"	 */\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(String str, boolean,");
	assertResults("");
}

public void test051() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #bar(String str, boolean flag,\n" +
		"	 */\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(String str, boolean flag,");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+R_DRICENUNR+"}"
	);
}

public void test052() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #bar(String,boolean,\n" +
		"	 */\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(String,boolean,");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+R_DRICENUNR+"}"
	);
}

public void test053() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #bar(String,boolean,Object\n" +
		"	 */\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "Object");
	assertResults(
		"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+this.positions+R_DRICENUNR+"}"
	);
}

/*
 * Specific case where we can complete but we don't want to as the prefix is not syntaxically correct
 */
public void test054() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #bar(String, boolean, Object o\n" +
		"	 */\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(String, boolean, Object o");
	assertResults("");
}

public void test055() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #bar(String str, boolean flag, Object o\n" +
		"	 */\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "bar(String str, boolean flag, Object o");
	assertResults(
		"bar[METHOD_REF]{bar(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, bar, (str, flag, obj), "+this.positions+R_DRICENUNR+"}"
	);
}

public void test056() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/methods/tags/BasicTestMethods.java",
			"package javadoc.methods.tags;\n" +
			"public class BasicTestMethods {\n" +
			"	/**\n" +
			"	 * Completion after:\n" +
			"	 * 	@see OtherTypes#foo(\n" +
			"	 */\n" +
			"	void foo() {};\n" +
			"}",
		"/Completion/src/javadoc/methods/tags/OtherTypes.java",
			"package javadoc.methods.tags;\n" +
			"public class OtherTypes {\n" +
			"	void foo(String str) {};\n" +
			"}"
	};
	completeInJavadoc(sources, true, "foo(");
	assertResults(
		"foo[METHOD_REF]{foo(String), Ljavadoc.methods.tags.OtherTypes;, (Ljava.lang.String;)V, foo, (str), "+this.positions+R_DRICENUNR+"}"
	);
}

public void test057() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/methods/tags/BasicTestMethods.java",
			"package javadoc.methods.tags;\n" +
			"public class BasicTestMethods {\n" +
			"	/**\n" +
			"	 * Completion after:\n" +
			"	 * 	@see javadoc.methods.tags.OtherTypes#foo(\n" +
			"	 */\n" +
			"	void foo() {};\n" +
			"}",
		"/Completion/src/javadoc/methods/tags/OtherTypes.java",
			"package javadoc.methods.tags;\n" +
			"public class OtherTypes {\n" +
			"	void foo(String str) {};\n" +
			"}"
	};
	completeInJavadoc(sources, true, "foo(");
	assertResults(
		"foo[METHOD_REF]{foo(String), Ljavadoc.methods.tags.OtherTypes;, (Ljava.lang.String;)V, foo, (str), "+this.positions+R_DRICENUNR+"}"
	);
}

public void test058() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see BasicTestMethods#method()\n" +
		"	 */\n" +
		"	void method() {}\n" +
		"	void bar(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "meth", 2); // 2nd occurrence
	assertResults(
		"method[METHOD_REF]{method(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, method, null, "+this.positions+R_DRICNRNS+"}"
	);
}

public void test059() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see BasicTestMethods#method()\n" +
		"	 */\n" +
		"	void method() {}\n" +
		"	void method(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "meth", 2); // 2nd occurrence
	assertResults(
		"method[METHOD_REF]{method(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, method, (str, flag, obj), "+this.positions+R_DRICNRNS+"}\n" +
		"method[METHOD_REF]{method(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, method, null, "+this.positions+R_DRICNRNS+"}"
	);
}

public void test060() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see BasicTestMethods#method(String)\n" +
		"	 */\n" +
		"	void method() {}\n" +
		"	void method(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "meth", 2); // 2nd occurrence
	assertResults(
		"method[METHOD_REF]{method(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, method, (str, flag, obj), "+this.positions+R_DRICNRNS+"}\n" +
		"method[METHOD_REF]{method(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, method, null, "+this.positions+R_DRICNRNS+"}"
	);
}

public void test061() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see BasicTestMethods#method(String,boolean,Object)\n" +
		"	 */\n" +
		"	void method() {}\n" +
		"	void method(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "meth", 2); // 2nd occurrence
	assertResults(
		"method[METHOD_REF]{method(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, method, (str, flag, obj), [116, 145], "+R_DRICNRNS+"}\n" +
		"method[METHOD_REF]{method(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, method, null, [116, 145], "+R_DRICNRNS+"}"
	);
}

// TODO (frederic) See with David what to do on this case...
public void _test062() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see BasicTestMethods#method(String str,boolean,Object)\n" +
		"	 */\n" +
		"	void method() {}\n" +
		"	void method(String str, boolean flag, Object obj) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "meth", 2); // 2nd occurrence
	assertResults(
		"method[METHOD_REF]{method(String, boolean, Object), Ljavadoc.methods.tags.BasicTestMethods;, (Ljava.lang.String;ZLjava.lang.Object;)V, method, (str, flag, obj), "+this.positions+R_DRICUNR+"}\n" +
		"method[METHOD_REF]{method(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, method, null, "+this.positions+R_DRICUNR+"}"
	);
}

/**
 * @tests Tests for method parameters completion
 */
public void test070() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param \n" +
		"	 */\n" +
		"	public String foo(String str) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@param ", 0); // empty token
	assertResults(
		"str[JAVADOC_PARAM_REF]{str, null, null, str, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test071() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param x\n" +
		"	 */\n" +
		"	public String foo(String xstr) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "x");
	assertResults(
		"xstr[JAVADOC_PARAM_REF]{xstr, null, null, xstr, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test072() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param xstr\n" +
		"	 */\n" +
		"	public String foo(String xstr) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr");
	assertResults(
		"xstr[JAVADOC_PARAM_REF]{xstr, null, null, xstr, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test073() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param xstr\n" +
		"	 */\n" +
		"	public String foo(String xstr) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "x");
	assertResults(
		"xstr[JAVADOC_PARAM_REF]{xstr, null, null, xstr, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test074() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param xx\n" +
		"	 */\n" +
		"	public String foo(String xstr) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xx");
	assertResults("");
}

public void test075() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param xstr\n" +
		"	 ** 	@param \n" +
		"	 */\n" +
		"	public String foo(String xstr) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "** 	@param ", 0); // empty token
	assertResults(	"");
}

public void test076() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param xstr\n" +
		"	 * 	@param xstr\n" +
		"	 */\n" +
		"	public String foo(String xstr) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr");
	assertResults("");
}

public void test077() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param xstr\n" +
		"	 * 	@param xstr\n" +
		"	 */\n" +
		"	public String foo(String xstr) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr", 2);
	assertResults("");
}

public void test078() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param xstr\n" +
		"	 * 	@param xstr\n" +
		"	 */\n" +
		"	public String foo(String xstr, String xstr2) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr");
	assertResults(
		"xstr2[JAVADOC_PARAM_REF]{xstr2, null, null, xstr2, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test079() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param xstr\n" +
		"	 * 	@param xstr\n" +
		"	 */\n" +
		"	public String foo(String xstr, String xstr2) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr", 2); // 2nd occurence
	assertResults(
		"xstr2[JAVADOC_PARAM_REF]{xstr2, null, null, xstr2, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test080() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param xstr\n" +
		"	 * 	@param xstr2\n" +
		"	 */\n" +
		"	public String foo(String xstr, String xstr2) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr");
	assertResults(
		"xstr[JAVADOC_PARAM_REF]{xstr, null, null, xstr, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test081() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param xstr\n" +
		"	 * 	@param xstr2\n" +
		"	 */\n" +
		"	public String foo(String xstr, String xstr2) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr", 2); // 2nd occurence
	assertResults(
		"xstr2[JAVADOC_PARAM_REF]{xstr2, null, null, xstr2, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test082() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param xstr\n" +
		"	 * 	@param xstr\n" +
		"	 * 	@param xstr2\n" +
		"	 */\n" +
		"	public String foo(String xstr, String xstr2) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr");
	assertResults("");
}

public void test083() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param xstr\n" +
		"	 * 	@param xstr\n" +
		"	 * 	@param xstr2\n" +
		"	 */\n" +
		"	public String foo(String xstr, String xstr2) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr", 2); // 2nd occurence
	assertResults("");
}

public void test084() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param xstr\n" +
		"	 * 	@param xstr\n" +
		"	 * 	@param xstr2\n" +
		"	 */\n" +
		"	public String foo(String xstr, String xstr2) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "xstr", 3); // 3rd position
	assertResults(
		"xstr2[JAVADOC_PARAM_REF]{xstr2, null, null, xstr2, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test085() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param \n" +
		"	 */\n" +
		"	public String foo(String xstr, boolean flag, Object obj) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@param ", 0); // empty token
	assertResults(
		"xstr[JAVADOC_PARAM_REF]{xstr, null, null, xstr, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING+2)+"}\n" +
		"flag[JAVADOC_PARAM_REF]{flag, null, null, flag, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING+1)+"}\n" +
		"obj[JAVADOC_PARAM_REF]{obj, null, null, obj, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test086() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param xstr\n" +
		"	 ** 	@param \n" +
		"	 */\n" +
		"	public String methodMultipleParam2(String xstr, boolean flag, Object obj) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "** 	@param ", 0); // empty token
	assertResults(
		"flag[JAVADOC_PARAM_REF]{flag, null, null, flag, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING+1)+"}\n" +
		"obj[JAVADOC_PARAM_REF]{obj, null, null, obj, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test087() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param \n" +
		"	 * 	@param flag\n" +
		"	 */\n" +
		"	public String methodMultipleParam3(String xstr, boolean flag, Object obj) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@param ", 0); // empty token
	assertResults(
		"xstr[JAVADOC_PARAM_REF]{xstr, null, null, xstr, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING+1)+"}\n" +
		"obj[JAVADOC_PARAM_REF]{obj, null, null, obj, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test088() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param obj\n" +
		"	 * 	@param xstr\n" +
		"	 ** 	@param \n" +
		"	 */\n" +
		"	public String methodMultipleParam4(String xstr, boolean flag, Object obj) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "** 	@param ", 0); // empty token
	assertResults(
		"flag[JAVADOC_PARAM_REF]{flag, null, null, flag, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test089() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param \n" +
		"	 * 	@param obj\n" +
		"	 * 	@param xstr\n" +
		"	 * 	@param flag\n" +
		"	 */\n" +
		"	public String methodMultipleParam5(String xstr, boolean flag, Object obj) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@param ", 0); // empty token
	assertResults("");
}

public void test090() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param obj\n" +
		"	 * 	@param xstr\n" +
		"	 * 	@param flag\n" +
		"	 */\n" +
		"	public String methodMultipleParam5(String xstr, boolean flag, Object obj) {\n" +
		"		return null;\n" +
		"	}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "ob");
	assertResults(
		"obj[JAVADOC_PARAM_REF]{obj, null, null, obj, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

/**
 * @tests Tests for type parameters completion
 */
public void test100() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param \n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@param ", 0); // empty token
	assertSortedResults(
		"xtm[JAVADOC_PARAM_REF]{xtm, null, null, xtm, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING+1)+"}\n" +
		"xtc[JAVADOC_PARAM_REF]{xtc, null, null, xtc, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}\n" +
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test101() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param <TM>\n" +
		"	 ** 	@param \n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "** 	@param ", 0); // empty token
	assertSortedResults(
		"xtm[JAVADOC_PARAM_REF]{xtm, null, null, xtm, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING+1)+"}\n" +
		"xtc[JAVADOC_PARAM_REF]{xtc, null, null, xtc, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test102() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param xtc\n" +
		"	 * 	@param <TM>\n" +
		"	 ** 	@param \n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true,"** 	@param ", 0); // empty token
	assertSortedResults(
		"xtm[JAVADOC_PARAM_REF]{xtm, null, null, xtm, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test103() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param xtc\n" +
		"	 ** 	@param \n" +
		"	 * 	@param xtc\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true,"** 	@param ", 0); // empty token
	assertSortedResults(
		"xtm[JAVADOC_PARAM_REF]{xtm, null, null, xtm, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}\n" +
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test104() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 ** 	@param \n" +
		"	 * 	@param xtc\n" +
		"	 * 	@param xtm\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true,"** 	@param ", 0); // empty token
	assertSortedResults(
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test105() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param xtc\n" +
		"	 * 	@param xtm\n" +
		"	 * 	@param <TM>\n" +
		"	 ** 	@param \n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true,"** 	@param ", 0); // empty token
	assertSortedResults("");
}

public void test106() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param <\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "<", 2); // 2nd occurence
	assertSortedResults(
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test107() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param <T\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "<T", 2); // 2nd occurence
	assertSortedResults(
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test108() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param <TC\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "<TC", 2); // 2nd occurence
	assertSortedResults("");
}

public void test109() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param <TM>\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "<TM");
	assertSortedResults(
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test110() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param <TM>\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "<TM>");
	assertSortedResults(
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test111() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param <TM>\n" +
		"	 * 	@param <TM>\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "<TM");
	assertSortedResults("");
}

public void test112() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param <TM>\n" +
		"	 * 	@param <TM>\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "<TM>", 2); // 2nd occurence
	assertSortedResults("");
}

public void test113() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param ab\n" +
		"	 */\n" +
		"	void foo(Object ab1, Object ab2) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@param ", 0);
	assertSortedResults(
		"ab1[JAVADOC_PARAM_REF]{ab1, null, null, ab1, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING+1)+"}\n" +
		"ab2[JAVADOC_PARAM_REF]{ab2, null, null, ab2, null, "+this.positions+(JAVADOC_RELEVANCE+R_INTERESTING)+"}"
	);
}

public void test114() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param <ZZZ>\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@param ", 0); // empty token
	assertSortedResults(
		"xtm[JAVADOC_PARAM_REF]{xtm, null, null, xtm, null, "+this.positions+(R_DEFAULT + 14)+"}\n" +
		"xtc[JAVADOC_PARAM_REF]{xtc, null, null, xtc, null, "+this.positions+(R_DEFAULT + 13)+"}\n" +
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test115() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param <ZZZ>\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "<", 2); // 2nd occurrence
	assertSortedResults(
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test116() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param <ZZZ>\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "Z");
	assertSortedResults("");
}

public void test117() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param <ZZZ>\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "ZZZ");
	assertSortedResults("");
}

public void test118() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param ZZZ>\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@param ", 0); // empty token
	assertSortedResults(
		"xtm[JAVADOC_PARAM_REF]{xtm, null, null, xtm, null, [105, 108], " + (R_DEFAULT + 14) +"}\n" +
		"xtc[JAVADOC_PARAM_REF]{xtc, null, null, xtc, null, [105, 108], " + (R_DEFAULT + 13) +"}\n" +
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, [105, 108], "+JAVADOC_RELEVANCE+"}"
	);
}

public void test119() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param ZZZ>\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "Z");
	assertSortedResults("");
}

public void test120() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param ZZZ>\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "ZZZ");
	assertSortedResults("");
}

public void test121() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param ZZZ.\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@param ", 0); // empty token
	assertSortedResults(
		"xtm[JAVADOC_PARAM_REF]{xtm, null, null, xtm, null, "+this.positions+(R_DEFAULT + 14)+"}\n" +
		"xtc[JAVADOC_PARAM_REF]{xtc, null, null, xtc, null, "+this.positions+(R_DEFAULT + 13)+"}\n" +
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test122() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param ZZZ#\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@param ", 0); // empty token
	assertSortedResults(
		"xtm[JAVADOC_PARAM_REF]{xtm, null, null, xtm, null, "+this.positions+(R_DEFAULT + 14)+"}\n" +
		"xtc[JAVADOC_PARAM_REF]{xtc, null, null, xtc, null, "+this.positions+(R_DEFAULT + 13)+"}\n" +
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

public void test123() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods<TC> {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@param ZZZ?\n" +
		"	 */\n" +
		"	<TM> void foo(Class<TM> xtm, Class<TC> xtc) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "@param ", 0); // empty token
	assertSortedResults(
		"xtm[JAVADOC_PARAM_REF]{xtm, null, null, xtm, null, "+this.positions+(R_DEFAULT + 14)+"}\n" +
		"xtc[JAVADOC_PARAM_REF]{xtc, null, null, xtc, null, "+this.positions+(R_DEFAULT + 13)+"}\n" +
		"TM[JAVADOC_PARAM_REF]{<TM>, null, null, TM, null, "+this.positions+JAVADOC_RELEVANCE+"}"
	);
}

/**
 * @tests Tests for constructors completion
 */
public void test130() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTest\n" +
		"	 */\n" +
		"	BasicTestMethods() {}\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTest", 2); // 2nd occurence
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+R_DRINR+"}\n" +
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+R_DRINR+"}"
	);
}

public void test131() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTest\n" +
		"	 * @since 3.2\n" +
		"	 */\n" +
		"	BasicTestMethods() {}\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTest", 2); // 2nd occurence
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+R_DRINR+"}\n" +
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+R_DRINR+"}"
	);
}

public void test132() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTest\n" +
		"	 */\n" +
		"	BasicTestMethods() {}\n" +
		"	<T> BasicTestMethods(int xxx, float real, Class<T> clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTest", 2); // 2nd occurence
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, <T:Ljava.lang.Object;>(IFLjava.lang.Class<TT;>;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+R_DRINR+"}\n" +
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+R_DRINR+"}"
	);
}

public void test133() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see BasicTestMethods#BasicTest\n" +
		"	 */\n" +
		"	BasicTestMethods() {}\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTest", 3); // 3rd occurence
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+R_DRINR+"}\n" +
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+R_DRINR+"}"
	);
}

public void test134() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see javadoc.methods.tags.BasicTestMethods#BasicTest\n" +
		"	 */\n" +
		"	BasicTestMethods() {}\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTest", 3); // 3rd occurence
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+R_DRINR+"}\n" +
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+R_DRINR+"}"
	);
}

public void test135() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/methods/tags/BasicTestMethods.java",
			"package javadoc.methods.tags;\n" +
			"public class BasicTestMethods {\n" +
			"	/**\n" +
			"	 * Completion after:\n" +
			"	 * 	@see OtherTypes#O\n" +
			"	 */\n" +
			"	void foo() {};\n" +
			"}",
		"/Completion/src/javadoc/methods/tags/OtherTypes.java",
			"package javadoc.methods.tags;\n" +
			"public class OtherTypes {\n" +
			"	OtherTypes() {};\n" +
			"}"
	};
	completeInJavadoc(sources, true, "O", 2); // 2nd occurence
	assertResults(
		"OtherTypes[METHOD_REF<CONSTRUCTOR>]{OtherTypes(), Ljavadoc.methods.tags.OtherTypes;, ()V, OtherTypes, null, "+this.positions+R_DRINR+"}"
	);
}

public void test136() throws JavaModelException {
	String[] sources = {
		"/Completion/src/javadoc/methods/tags/BasicTestMethods.java",
			"package javadoc.methods.tags;\n" +
			"public class BasicTestMethods {\n" +
			"	/**\n" +
			"	 * Completion after:\n" +
			"	 * 	@see OtherTypes#O implicit default constructor\n" +
			"	 */\n" +
			"	void foo() {};\n" +
			"}",
		"/Completion/src/javadoc/methods/tags/OtherTypes.java",
			"package javadoc.methods.tags;\n" +
			"public class OtherTypes {\n" +
			"}"
	};
	completeInJavadoc(sources, true, "O", 2); // 2nd occurence
	assertResults(
		"OtherTypes[METHOD_REF<CONSTRUCTOR>]{OtherTypes(), Ljavadoc.methods.tags.OtherTypes;, ()V, OtherTypes, null, "+this.positions+R_DRINR+"}"
	);
}

public void test137() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #\n" +
		"	 */\n" +
		"	BasicTestMethods() {}\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "#", 0); // empty token
	assertResults(
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
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+R_DRINR+"}\n" +
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+R_DRINR+"}"
	);
}

public void test138() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #\n" +
		"	 * @since 3.2\n" +
		"	 */\n" +
		"	BasicTestMethods() {}\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "#", 0); // empty token
	assertResults(
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
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+R_DRINR+"}\n" +
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+R_DRINR+"}"
	);
}

public void test139() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #\n" +
		"	 */\n" +
		"	<T> BasicTestMethods() {}\n" +
		"	<T, U> BasicTestMethods(int xxx, Class<T> cl1, Class<U> cl2) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "#", 0); // empty token
	assertResults(
		"wait[METHOD_REF]{wait(long, int), Ljava.lang.Object;, (JI)V, wait, (millis, nanos), "+this.positions+R_DRICNRNS+"}\n" +
		"wait[METHOD_REF]{wait(long), Ljava.lang.Object;, (J)V, wait, (millis), "+this.positions+R_DRICNRNS+"}\n" +
		"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, wait, null, "+this.positions+R_DRICNRNS+"}\n" +
		"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, toString, null, "+this.positions+R_DRICNRNS+"}\n" +
		"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, notifyAll, null, "+this.positions+R_DRICNRNS+"}\n" +
		"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, notify, null, "+this.positions+R_DRICNRNS+"}\n" +
		"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, "+this.positions+R_DRICNRNS+"}\n" +
		"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, "+this.positions+R_DRICNRNS+"}\n" +
		"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, "+this.positions+R_DRICNRNS+"}\n" +
		"equals[METHOD_REF]{equals(Object), Ljava.lang.Object;, (Ljava.lang.Object;)Z, equals, (obj), "+this.positions+R_DRICNRNS+"}\n" +
		"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, "+this.positions+R_DRICNRNS+"}\n" +
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, Class, Class), Ljavadoc.methods.tags.BasicTestMethods;, <T:Ljava.lang.Object;U:Ljava.lang.Object;>(ILjava.lang.Class<TT;>;Ljava.lang.Class<TU;>;)V, BasicTestMethods, (xxx, cl1, cl2), "+this.positions+R_DRINR+"}\n" +
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, <T:Ljava.lang.Object;>()V, BasicTestMethods, null, "+this.positions+R_DRINR+"}"
	);
}

public void test140() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"/**\n" +
		" * Completion after:\n" +
		" * 	@see #\n" +
		" */\n" +
		"public class BasicTestMethods {\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "#", 0); // empty token
	assertResults(
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
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+R_DRINR+"}"
	);
}

public void test141() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTestMethods(\n" +
		"	 */\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMethods(");
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+R_DRINR+"}"
	);
}

public void test142() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTestMethods(\n" +
		"	 * @since 3.2\n" +
		"	 */\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMethods(");
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+R_DRINR+"}"
	);
}

public void test143() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTestMethods( trailing text\n" +
		"	 */\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMethods(");
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+R_DRINR+"}"
	);
}

public void test144() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTestMethods(   ...\n" +
		"	 */\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMethods(");
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+R_DRINR+"}"
	);
}

public void test145() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTestMethods(        \n" +
		"	 */\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMethods(");
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+R_DRINR+"}"
	);
}

public void test146() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTestMethods(     ????\n" +
		"	 */\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMethods(");
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+R_DRINR+"}"
	);
}

public void test147() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTestMethods(  ,,\n" +
		"	 */\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMethods(");
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+R_DRINR+"}"
	);
}

public void test148() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see BasicTestMethods#BasicTestMethods(\n" +
		"	 */\n" +
		"	BasicTestMethods() {}\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMethods(");
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+R_DRINR+"}\n" +
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+R_DRINR+"}"
	);
}

public void test149() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see javadoc.methods.tags.BasicTestMethods#BasicTestMethods(\n" +
		"	 */\n" +
		"	void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMethods(");
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(), Ljavadoc.methods.tags.BasicTestMethods;, ()V, BasicTestMethods, null, "+this.positions+R_DRINR+"}"
	);
}

// TODO (frederic) Reduce proposal as there's only a single valid proposal: int
public void test150() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTestMethods(in\n" +
		"	 */\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "in");
	assertResults(
		"int[KEYWORD]{int, null, null, int, null, "+this.positions+R_DRICNR+"}\n" +
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DRIUNR+"}"
	);
}

// TODO (frederic) Reduce proposal as there's only a single valid proposal: int
public void test151() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTestMethods(int\n" +
		"	 */\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "int");
	assertResults(
		"int[KEYWORD]{int, null, null, int, null, "+this.positions+R_DRICENNR+"}\n" +
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DRIUNR+"}"
	);
}

public void test152() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTestMethods(int aaa, fl\n" +
		"	 */\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "fl");
	assertResults(
		"float[KEYWORD]{float, null, null, float, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test153() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTestMethods(int aaa, float\n" +
		"	 */\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "float");
	assertResults(
		"float[KEYWORD]{float, null, null, float, null, "+this.positions+R_DRICENNR+"}"
	);
}

public void test154() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTestMethods(int, float, Cla\n" +
		"	 */\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "Cla");
	assertResults(
		"Class[TYPE_REF]{Class, java.lang, Ljava.lang.Class;, null, null, "+this.positions+R_DRICUNR+"}"
	);
}

// TODO (frederic) Reduce proposal as there's only a single valid proposal: Class
public void test155() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTestMethods(int, float, java.lang.\n" +
		"	 */\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "java.lang.");
	assertSortedResults(
		"Class[TYPE_REF]{Class, java.lang, Ljava.lang.Class;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"CloneNotSupportedException[TYPE_REF]{CloneNotSupportedException, java.lang, Ljava.lang.CloneNotSupportedException;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Error[TYPE_REF]{Error, java.lang, Ljava.lang.Error;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Exception[TYPE_REF]{Exception, java.lang, Ljava.lang.Exception;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"IllegalMonitorStateException[TYPE_REF]{IllegalMonitorStateException, java.lang, Ljava.lang.IllegalMonitorStateException;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"InterruptedException[TYPE_REF]{InterruptedException, java.lang, Ljava.lang.InterruptedException;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"RuntimeException[TYPE_REF]{RuntimeException, java.lang, Ljava.lang.RuntimeException;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+this.positions+R_DRICNR+"}\n" +
		"Throwable[TYPE_REF]{Throwable, java.lang, Ljava.lang.Throwable;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test156() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTestMethods(int, float, java.lang.Cla\n" +
		"	 */\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "java.lang.Cla");
	assertResults(
		"Class[TYPE_REF]{Class, java.lang, Ljava.lang.Class;, null, null, "+this.positions+R_DRICNR+"}"
	);
}

public void test157() throws JavaModelException {
	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	void foo() {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTestMethods(int, float, Class\n" +
		"	 * \n" +
		"	 */\n" +
		"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "Class");
	assertResults(
		"Class[TYPE_REF]{Class, java.lang, Ljava.lang.Class;, null, null, "+this.positions+R_DRICENUNR+"}"
	);
}
/**
 * @tests Tests for camel case completion
 */
public void test160() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_CAMEL_CASE_MATCH, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		String source =
			"package javadoc.methods.tags;\n" +
			"public class BasicTestMethods {\n" +
			"	void foo() {}\n" +
			"	/**\n" +
			"	 * Completion after:\n" +
			"	 * 	@see #BTM\n" +
			"	 * \n" +
			"	 */\n" +
			"	BasicTestMethods(int xxx, float real, Class clazz) {}\n" +
			"}\n";
		completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BTM");
		assertResults(
			"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(int, float, Class), Ljavadoc.methods.tags.BasicTestMethods;, (IFLjava.lang.Class;)V, BasicTestMethods, (xxx, real, clazz), "+this.positions+R_DRINR+"}"
		);
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
public void test161() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_CAMEL_CASE_MATCH, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		String source =
			"package javadoc.methods.tags;\n" +
			"public class BasicTestMethods {\n" +
			"	void oneTwoThree(int i) {}\n" +
			"	/**\n" +
			"	 * Completion after:\n" +
			"	 * 	@see #oTT\n" +
			"	 * \n" +
			"	 */\n" +
			"	BasicTestMethods() {}\n" +
			"}\n";
		completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "oTT");
		assertResults(
			"oneTwoThree[METHOD_REF]{oneTwoThree(int), Ljavadoc.methods.tags.BasicTestMethods;, (I)V, oneTwoThree, (i), "+this.positions+(R_DEFAULT + 25)+"}"
		);
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155824
public void test162() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);

	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	public void oneTwoThree(Object... o) {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #oneTwoT\n" +
		"	 * \n" +
		"	 */\n" +
		"	BasicTestMethods() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "oneTwoT", 2);
	assertResults(
		"oneTwoThree[METHOD_REF]{oneTwoThree(Object...), Ljavadoc.methods.tags.BasicTestMethods;, ([Ljava.lang.Object;)V, oneTwoThree, (o), "+this.positions+R_DRICNRNS+"}"
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=155824
public void test163() throws JavaModelException {
	setUpProjectOptions(CompilerOptions.VERSION_1_5);

	String source =
		"package javadoc.methods.tags;\n" +
		"public class BasicTestMethods {\n" +
		"	public BasicTestMethods(Object... o) {}\n" +
		"	/**\n" +
		"	 * Completion after:\n" +
		"	 * 	@see #BasicTestMeth\n" +
		"	 * \n" +
		"	 */\n" +
		"	void foo() {}\n" +
		"}\n";
	completeInJavadoc("/Completion/src/javadoc/methods/tags/BasicTestMethods.java", source, true, "BasicTestMeth", 3);
	assertResults(
		"BasicTestMethods[METHOD_REF<CONSTRUCTOR>]{BasicTestMethods(Object...), Ljavadoc.methods.tags.BasicTestMethods;, ([Ljava.lang.Object;)V, BasicTestMethods, (o), "+this.positions+R_DRINR+"}"
	);
}
}
