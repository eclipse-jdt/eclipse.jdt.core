/*******************************************************************************
 * Copyright (c) 2018 Jesper Steen Møller and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jesper Steen Møller - initial API and implementation
 *                           bug 529556 - [18.3] Add content assist support for 'var' as a type
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.stream.Stream;
import junit.framework.Test;
import org.eclipse.jdt.core.JavaModelException;

public class CompletionTests10 extends AbstractJavaModelCompletionTests {
	static {
//		TESTS_NAMES = new String[]{"test0001_block_scope"};
	}
public CompletionTests10(String name) {
	super(name);
}
@Override
public void setUpSuite() throws Exception {
	if (COMPLETION_PROJECT == null)  {
		COMPLETION_PROJECT = setUpJavaProject("Completion", "10");
	} else {
		setUpProjectCompliance(COMPLETION_PROJECT, "10");
	}
	super.setUpSuite();
}
public static Test suite() {
	return buildModelTestSuite(CompletionTests10.class);
}
public void test0001_block_scope() throws JavaModelException {
	CompletionResult result = complete(
        "/Completion/src3/test0001/Test.java",
        "package test0001;\n" +
        "\n" +
        "public class Test {\n" +
        "	void x() {\n" +
        "		va\n" +
        "	}\n" +
        "}",
    	"va");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"var[KEYWORD]{var, null, null, var, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				result.proposals);
}
public void test0001a_block_scope_switch() throws JavaModelException {
	CompletionResult result = complete(
        "/Completion/src3/test0001/Test.java",
        "package test0001;\n" +
        "\n" +
        "public class Test {\n" +
        "	void x(int a) {\n" +
        "		switch(a) {\n" +
        "		case 1: 	va\n" +
        "		}\n" +
        "	}\n" +
        "}",
    	"va");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"var[KEYWORD]{var, null, null, var, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				result.proposals);
}
public void test0002_block_scope_final() throws JavaModelException {
	CompletionResult result = complete(
        "/Completion/src3/test0002/Test.java",
        "package test0002;\n" +
        "\n" +
        "public class Test {\n" +
        "	void x() {\n" +
        "		final va\n" +
        "	}\n" +
        "}",
    	"va");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"var[KEYWORD]{var, null, null, var, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				result.proposals);
}
public void test0003_inside_for() throws JavaModelException {
	CompletionResult result = complete(
        "/Completion/src3/test0003/Test.java",
        "package test0003;\n" +
        "\n" +
        "public class Test {\n" +
        "	void x() {\n" +
        "		for(va\n" +
        "	}\n" +
        "}",
    	"va");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"var[KEYWORD]{var, null, null, var, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				result.proposals);
}
public void test0004_inside_for_final() throws JavaModelException {
	CompletionResult result = complete(
        "/Completion/src3/test0004/Test.java",
        "package test0004;\n" +
        "\n" +
        "public class Test {\n" +
        "	void x() {\n" +
        "		for(final va\n" +
        "	}\n" +
        "}",
    	"va");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"var[KEYWORD]{var, null, null, var, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				result.proposals);
}
public void test0005_inside_try() throws JavaModelException {
	CompletionResult result = complete(
        "/Completion/src3/test0005/Test.java",
        "package test0005;\n" +
        "\n" +
        "public class Test {\n" +
        "	void x() {\n" +
        "		try(va\n" +
        "	}\n" +
        "}",
    	"va");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"var[KEYWORD]{var, null, null, var, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				result.proposals);
}
public void test0006_inside_try_final() throws JavaModelException {
	CompletionResult result = complete(
        "/Completion/src3/test0006/Test.java",
        "package test0006;\n" +
        "\n" +
        "public class Test {\n" +
        "	void x() {\n" +
        "		try(final va\n" +
        "	}\n" +
        "}",
    	"va");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"var[KEYWORD]{var, null, null, var, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				result.proposals);
}

public void test0007_not_inside_expression() throws JavaModelException {
	CompletionResult result = complete(
        "/Completion/src3/test0007/Test.java",
        "package test0007;\n" +
        "\n" +
        "public class Test {\n" +
        "	void x() {\n" +
        "		int a = 2 + \n" +
        "	}\n" +
        "}",
    	"+ ");

    assertResults(
            "expectedTypesSignatures={S,I,J,F,D,C,B,Ljava.lang.String;}\n" +
            "expectedTypesKeys={S,I,J,F,D,C,B,Ljava/lang/String;}",
            result.context);

	assertProposalCount("var[KEYWORD]", 0, 14, result);
}

public void test0008_not_in_class_scope() throws JavaModelException {
	CompletionResult result = complete(
        "/Completion/src3/test0008/Test.java",
        "package test0008;\n" +
        "\n" +
        "public class Test { \n" +

        "}",
    	"{");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertProposalCount("var[KEYWORD]", 0, 21, result);
}
	public void test0009_in_formal_param_lists() throws JavaModelException {
		CompletionResult result = complete(
	        "/Completion/src3/test0009/Test.java",
	        "package test0009;\n" +
	        "\n" +
	        "public class Test {\n" +
	        "	void x( ) {\n" +
	        "	}\n" +
	        "}",
	    	"x(");

	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);
		assertProposalCount("var[KEYWORD]", 0, 15, result);
	}

public void testbug_529556_missing_type_info_on_vars() throws JavaModelException {
	CompletionResult result = complete(
	        "/Completion/src3/test0001/Test.java",
	        "package test0001;\n" +
	        "\n" +
	        "public class Test {\n" +
	        "   private class Dummy {\n" +
	        "		public void a_method() {/n"+
	        "	}\n" +
	        "	void x() {\n" +
	        "		var x = new Dummy();\n" +
	        "		x.a\n" +
	        "	}\n" +
	        "}",
	    	"x.a");
		assertResults(
			"a_method[METHOD_REF]{a_method(), Ltest0001.Test$Dummy;, ()V, a_method, null, " + (R_DEFAULT + 30) + "}",
			result.proposals);
}
public void testBug532476a() throws JavaModelException {
	CompletionResult result = complete(
	        "/Completion/src3/p/X.java",
	        "package p;\n" +
	        "public class X {\n" +
	        "   public static void main(String[] args) {\n" +
	        "		var i_jk = 0;\n" +
	        "		System.out.println(i_);/n"+
	        "	}\n" +
	        "}",
	    	"i_");
		assertResults(
			"i_jk[LOCAL_VARIABLE_REF]{i_jk, null, I, i_jk, null, " + (R_DEFAULT + 22) + "}",
			result.proposals);
}
public void testBug532476b() throws JavaModelException {
	CompletionResult result = complete(
	        "/Completion/src3/p/X.java",
	        "package p;\n" +
	        "public class X {\n" +
	        "   public static void main(String[] args) {\n" +
	        "		for (var i_jkl : args) {\n" +
	        "			System.out.println(i_);/n"+
	        "		}\n" +
	        "	}\n" +
	        "}",
	    	"i_");
		assertResults(
			"i_jkl[LOCAL_VARIABLE_REF]{i_jkl, null, Ljava.lang.String;, i_jkl, null, "  + (R_DEFAULT + 22) + "}",
			result.proposals);
}
public void testBug532476c() throws JavaModelException {
	CompletionResult result = complete(
	        "/Completion/src3/p/X.java",
	        "package p;\n" +
	        "public class X {\n" +
	        "   public static void main(String[] args) {\n" +
	        "		for (var i_jkl = 0; i_ " +
	        "	}\n" +
	        "}",
	    	"i_");
		assertResults(
				"i_jkl[LOCAL_VARIABLE_REF]{i_jkl, null, I, i_jkl, null, " + (R_DEFAULT + 22) + "}",
			result.proposals);
}
public void testBug532476d() throws JavaModelException {
	CompletionResult result = complete(
	        "/Completion/src3/p/X.java",
	        "package p;\n" +
	        "public class X {\n" +
	        "   public static void main(String[] args) {\n" +
	        "		for (var i_jkl = 0; ; i_ " +
	        "	}\n" +
	        "}",
	    	"i_");
		assertResults(
				"i_jkl[LOCAL_VARIABLE_REF]{i_jkl, null, I, i_jkl, null, " + (R_DEFAULT + 22) + "}",
			result.proposals);
}
public void testBug532476e() throws JavaModelException {
	CompletionResult result = complete(
	        "/Completion/src3/p/X.java",
	        "package p;\n" +
	        "public class X {\n" +
	        "   public static void main(String[] args) {\n" +
	        "		for (var i_jkl : args) {\n" +
	        "			System.out.println(i_jkl.fin);/n"+
	        "		}\n" +
	        "	}\n" +
	        "}",
	    	"i_jkl.fin");
		assertResults(
			"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, finalize, null, " + (R_DEFAULT + 30) + "}",
			result.proposals);
}
private void assertProposalCount(String proposal, int expectedCount, int expectedOtherCount, CompletionResult result) {
	String[] proposals = result.proposals.split("\n");
	long proposalsCount = Stream.of(proposals).filter(s -> s.startsWith(proposal)).count();
	assertEquals(
			"Unexpected occurrences of " + proposal + " - result was " + result.proposals,
			expectedCount, proposalsCount);

	long otherProposalsCount = proposals.length - proposalsCount;
	assertEquals(
			"Unexpected occurrences that were not " + proposal + " - result was " + result.proposals,
			expectedOtherCount, otherProposalsCount);
}

}
