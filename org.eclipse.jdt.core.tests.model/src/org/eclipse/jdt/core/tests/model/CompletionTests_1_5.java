/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
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

import java.util.Hashtable;

import junit.framework.Test;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.codeassist.RelevanceConstants;

@SuppressWarnings({"rawtypes", "unchecked"})
public class CompletionTests_1_5 extends AbstractJavaModelCompletionTests {
	static {
//		TESTS_NAMES = new String[]{"test0040"};
	}
public CompletionTests_1_5(String name) {
	super(name);
}
@Override
public void setUpSuite() throws Exception {
	if (COMPLETION_PROJECT == null)  {
		COMPLETION_PROJECT = setUpJavaProject("Completion", "1.5");
	} else {
		setUpProjectCompliance(COMPLETION_PROJECT, "1.5");
	}
	super.setUpSuite();
}
@Override
protected void setUp() throws Exception {
	this.indexDisabledForTest = false;
	super.setUp();
}
public static Test suite() {
	return buildModelTestSuite(CompletionTests_1_5.class);
}
private ICompilationUnit[] getExternalQQTypes() throws JavaModelException {
	ICompilationUnit[] units = new ICompilationUnit[6];

	units[0] = getWorkingCopy(
		"/Completion/src3/pkgstaticimport/QQType1.java",
		"package pkgstaticimport;\n"+
		"\n"+
		"public class QQType1 {\n"+
		"	public class Inner1 {}\n"+
		"	public static class Inner2 {}\n"+
		"	protected class Inner3 {}\n"+
		"	protected static class Inner4 {}\n"+
		"	private class Inner5 {}\n"+
		"	private static class Inner6 {}\n"+
		"	class Inner7 {}\n"+
		"	static class Inner8 {}\n"+
		"}");

	units[1] = getWorkingCopy(
		"/Completion/src3/pkgstaticimport/QQType3.java",
		"package pkgstaticimport;\n"+
		"\n"+
		"public class QQType3 extends QQType1 {\n"+
		"	\n"+
		"}");

	units[2] = getWorkingCopy(
		"/Completion/src3/pkgstaticimport/QQType4.java",
		"package pkgstaticimport;\n"+
		"\n"+
		"public class QQType4 {\n"+
		"	public int zzvarzz1;\n"+
		"	public static int zzvarzz2;\n"+
		"	protected int zzvarzz3;\n"+
		"	protected static int zzvarzz4;\n"+
		"	private int zzvarzz5;\n"+
		"	private static int zzvarzz6;\n"+
		"	int zzvarzz7;\n"+
		"	static int zzvarzz8;\n"+
		"}");

	units[3] = getWorkingCopy(
		"/Completion/src3/pkgstaticimport/QQType6.java",
		"package pkgstaticimport;\n"+
		"\n"+
		"public class QQType6 extends QQType4 {\n"+
		"	\n"+
		"}");

	units[4] = getWorkingCopy(
		"/Completion/src3/pkgstaticimport/QQType7.java",
		"package pkgstaticimport;\n"+
		"\n"+
		"public class QQType7 {\n"+
		"	public void zzfoozz1(){};\n"+
		"	public static void zzfoozz2(){};\n"+
		"	protected void zzfoozz3(){};\n"+
		"	protected static void zzfoozz4(){};\n"+
		"	private void zzfoozz5(){};\n"+
		"	private static void zzfoozz6(){};\n"+
		"	void zzfoozz7(){};\n"+
		"	static void zzfoozz8(){};\n"+
		"}");

	units[5] = getWorkingCopy(
		"/Completion/src3/pkgstaticimport/QQType9.java",
		"package pkgstaticimport;\n"+
		"\n"+
		"public class QQType9 extends QQType7 {\n"+
		"	\n"+
		"}");

	return units;
}
public void test0001() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0001", "Test.java");

	String str = cu.getSource();
	String completeBehind = "X<St";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("should have one class",
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0002() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0002", "Test.java");

	String str = cu.getSource();
	String completeBehind = "X<Ob";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("should have one class",
		"element:Object    completion:Object    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0003() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0003", "Test.java");

	String str = cu.getSource();
	String completeBehind = "X<St";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("should have one class",
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0004() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0004", "Test.java");

	String str = cu.getSource();
	String completeBehind = "X<XZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("should have one class",
		"element:XZX    completion:XZX    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:XZXSuper    completion:XZXSuper    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0005() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0005/Test.java",
            "package test0005;\n" +
            "\n" +
            "public class Test {\n" +
            "	void foo() {\n" +
            "		X<Object>.Y<St\n" +
            "	}\n" +
            "}\n" +
            "\n" +
            "class X<T> {\n" +
            "	public class Y<U> {\n" +
            "	}\n" +
            "}",
            "Y<St");

    assertResults(
            "expectedTypesSignatures={Ljava.lang.Object;}\n" +
            "expectedTypesKeys={Ljava/lang/Object;}",
            result.context);

    assertResults(
            "String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
            result.proposals);
}
public void test0006() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0006/Test.java",
            "package test0006;\n" +
            "\n" +
            "public class Test {\n" +
            "	void foo() {\n" +
            "		X<String>.Y<Ob\n" +
            "	}\n" +
            "}\n" +
            "\n" +
            "class X<T> {\n" +
            "	public class Y<U> {\n" +
            "	}\n" +
            "}",
            "Y<Ob");

    assertResults(
            "expectedTypesSignatures={Ljava.lang.Object;}\n" +
            "expectedTypesKeys={Ljava/lang/Object;}",
            result.context);

    assertResults(
            "Object[TYPE_REF]{Object, java.lang, Ljava.lang.Object;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) +"}",
            result.proposals);
}
public void test0007() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0007/Test.java",
            "package test0007;\n" +
            "\n" +
            "public class Test {\n" +
            "	void foo() {\n" +
            "		X<Object>.Y<St\n" +
            "	}\n" +
            "}\n" +
            "\n" +
            "class X<T> {\n" +
            "	public class Y<U extends String> {\n" +
            "	}\n" +
            "}",
            "Y<St");

    assertResults(
            "expectedTypesSignatures={Ljava.lang.String;}\n" +
            "expectedTypesKeys={Ljava/lang/String;}",
            result.context);

    assertResults(
            "String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED) +"}",
            result.proposals);
}
public void test0008() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0008/Test.java",
            "package test0008;\n" +
            "\n" +
            "public class Test {\n" +
            "	void foo() {\n" +
            "		X<Object>.Y<XY\n" +
            "	}\n" +
            "}\n" +
            "\n" +
            "class X<T> {\n" +
            "	public class Y<U extends XYXSuper> {\n" +
            "	}\n" +
            "}\n" +
            "class XYX {\n" +
            "	\n" +
            "}\n" +
            "class XYXSuper {\n" +
            "	\n" +
            "}",
            "Y<XY");

    assertResults(
            "expectedTypesSignatures={Ltest0008.XYXSuper;}\n" +
            "expectedTypesKeys={Ltest0008/Test~XYXSuper;}",
            result.context);

    assertResults(
            "XYX[TYPE_REF]{XYX, test0008, Ltest0008.XYX;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}\n"+
			"XYXSuper[TYPE_REF]{XYXSuper, test0008, Ltest0008.XYXSuper;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED)+"}",
			result.proposals);
}
public void test0009() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0009", "Test.java");

	String str = cu.getSource();
	String completeBehind = "/**/T_";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("should have one class",
		"element:T_1    completion:T_1    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:T_2    completion:T_2    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0010() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0010", "Test.java");

	String str = cu.getSource();
	String completeBehind = "/**/T_";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("should have one class",
		"element:T_1    completion:T_1    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:T_2    completion:T_2    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:T_3    completion:T_3    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+"\n"+
		"element:T_4    completion:T_4    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0011() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0011/Test.java",
            "package test0011;\n"+
            "\n"+
            "public class Test <T extends Z0011<Object>.Y001> {\n"+
            "\n"+
            "}\n"+
            "class Z0011<T0011> {\n"+
            "	public class Y0011 {\n"+
            "	}\n"+
            "}",
            ".Y001");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "Z0011<java.lang.Object>.Y0011[TYPE_REF]{Y0011, test0011, Ltest0011.Z0011<Ljava.lang.Object;>.Y0011;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
            result.proposals);
}
public void test0012() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0012/Test.java",
            "package test0012;\n"+
            "\n"+
            "public class Test {\n"+
            "	public Z0012<Object>.Y001\n"+
            "}\n"+
            "class Z0012<T0012> {\n"+
            "	public class Y0012 {\n"+
            "	}\n"+
            "}",
            ".Y001");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "Z0012<java.lang.Object>.Y0012[TYPE_REF]{Y0012, test0012, Ltest0012.Z0012<Ljava.lang.Object;>.Y0012;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}",
            result.proposals);
}
public void test0013() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0013/Test.java",
            "package test0013;\n"+
            "\n"+
            "public class Test {\n"+
            "	public Z0013<Object>.Y001 foo() {}\n"+
            "}\n"+
            "class Z0013<T0013> {\n"+
            "	public class Y0013 {\n"+
            "	}\n"+
            "}",
            ".Y001");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "Z0013<java.lang.Object>.Y0013[TYPE_REF]{Y0013, test0013, Ltest0013.Z0013<Ljava.lang.Object;>.Y0013;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}",
            result.proposals);
}
public void test0014() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0014/Test.java",
            "package test0014;\n" +
            "\n" +
            "public class Test extends Z0014<Object>.Y001 {\n" +
            "}\n" +
            "class Z0014<T0014> {\n" +
            "	public class Y0014 {\n" +
            "	}\n" +
            "}",
            ".Y001");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "Z0014<java.lang.Object>.Y0014[TYPE_REF]{Y0014, test0014, Ltest0014.Z0014<Ljava.lang.Object;>.Y0014;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_CLASS + R_NON_RESTRICTED) +"}",
            result.proposals);
}
public void test0015() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0015/Test.java",
            "package test0015;\n" +
            "\n" +
            "public class Test implements Z0015<Object>.Y001 {\n" +
            "}\n" +
            "class Z0015<T0015> {\n" +
            "	public class Y0015 {\n" +
            "	}\n" +
            "	public interface Y0015I {\n" +
            "	}\n" +
            "}",
            ".Y001");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "Z0015<java.lang.Object>.Y0015[TYPE_REF]{Y0015, test0015, Ltest0015.Z0015<Ljava.lang.Object;>.Y0015;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
            result.proposals);
}
public void test0016() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0016/Test.java",
            "package test0016;\n" +
            "\n" +
            "public class Test implements  {\n" +
            "	void foo(Z0016<Object>.Y001) {\n" +
            "		\n" +
            "	}\n" +
            "}\n" +
            "class Z0016<T0016> {\n" +
            "	public class Y0016 {\n" +
            "	}\n" +
            "}",
            ".Y001");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "Z0016<java.lang.Object>.Y0016[TYPE_REF]{Y0016, test0016, Ltest0016.Z0016<Ljava.lang.Object;>.Y0016;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}",
            result.proposals);
}
public void test0017() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0017/Test.java",
            "package test0017;\n" +
            "\n" +
            "public class Test implements  {\n" +
            "	void foo() throws Z0017<Object>.Y001{\n" +
            "		\n" +
            "	}\n" +
            "}\n" +
            "class Z0017<T0017> {\n" +
            "	public class Y0017 {\n" +
            "	}\n" +
            "}",
            ".Y001");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "Z0017<java.lang.Object>.Y0017[TYPE_REF]{Y0017, test0017, Ltest0017.Z0017<Ljava.lang.Object;>.Y0017;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}",
            result.proposals);
}
public void test0018() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0018/Test.java",
            "package test0018;\n" +
            "\n" +
            "public class Test {\n" +
            "	<T extends Z0018<Object>.Y001> void foo() {\n" +
            "		\n" +
            "	}\n" +
            "}\n" +
            "class Z0018<T0018> {\n" +
            "	public class Y0018 {\n" +
            "	}\n" +
            "}",
            ".Y001");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "Z0018<java.lang.Object>.Y0018[TYPE_REF]{Y0018, test0018, Ltest0018.Z0018<Ljava.lang.Object;>.Y0018;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}",
            result.proposals);
}
public void test0019() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0019/Test.java",
            "package test0019;\n" +
            "\n" +
            "public class Test {\n" +
            "	<T extends Z0019<Object>.Y001\n" +
            "}\n" +
            "class Z0019<T0019> {\n" +
            "	public class Y0019 {\n" +
            "	}\n" +
            "}",
            ".Y001");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "Z0019<java.lang.Object>.Y0019[TYPE_REF]{Y0019, test0019, Ltest0019.Z0019<Ljava.lang.Object;>.Y0019;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}",
            result.proposals);
}
public void test0020() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0020/Test.java",
            "package test0020;\n"+
            "\n"+
            "public class Test {\n"+
            "	void foo() {\n"+
            "		Z0020<Object>.Y002\n"+
            "	}\n"+
            "}\n"+
            "class Z0020<T0020> {\n"+
            "	public class Y0020 {\n"+
            "	}\n"+
            "}",
            ".Y002");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "Z0020<java.lang.Object>.Y0020[TYPE_REF]{Y0020, test0020, Ltest0020.Z0020<Ljava.lang.Object;>.Y0020;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}",
            result.proposals);
}
public void test0021() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0021/Test.java",
		"package test0021;\n" +
		"\n" +
		"public class Test {\n" +
		"	<T extends Z0021Z> void foo() {\n" +
		"		this.<Z0021>foo();\n" +
		"	}\n" +
		"}\n" +
		"class Z0021Z {\n" +
		"	\n" +
		"}\n" +
		"class Z0021ZZ {\n" +
		"	\n" +
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "<Z0021";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Z0021Z[TYPE_REF]{Z0021Z, test0021, Ltest0021.Z0021Z;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"Z0021ZZ[TYPE_REF]{Z0021ZZ, test0021, Ltest0021.Z0021ZZ;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0022() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0022/Test.java",
		"package test0022;\n" +
		"\n" +
		"public class Test {\n" +
		"	void foo() {\n" +
		"		new Z0022<Z0022Z>foo();\n" +
		"	}\n" +
		"}\n" +
		"class Z0022<T extends Z0022ZZ> {\n" +
		"	\n" +
		"}\n" +
		"class Z0022ZZ {\n" +
		"	\n" +
		"}\n" +
		"class Z0022ZZZ {\n" +
		"	\n" +
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "<Z0022Z";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Z0022ZZZ[TYPE_REF]{Z0022ZZZ, test0022, Ltest0022.Z0022ZZZ;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"Z0022ZZ[TYPE_REF]{Z0022ZZ, test0022, Ltest0022.Z0022ZZ;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0023() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0023", "Test.java");

	String str = cu.getSource();
	String completeBehind = "<St";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("should have one class",
		"",
		requestor.getResults());
}
public void test0024() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0024", "Test.java");

	String str = cu.getSource();
	String completeBehind = "<St";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("should have one class",
		"",
		requestor.getResults());
}
public void test0025() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0025", "Test.java");

	String str = cu.getSource();
	String completeBehind = "<St";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("should have one class",
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0026() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0026/Test.java",
		"package test0026;\n" +
		"\n" +
		"public class Test {\n" +
		"	Z0026<String, String>.Z0026Z.Z0026ZZ<St, String> var;\n" +
		"}\n" +
		"class Z0026 <T1 extends String, T2 extends String>{\n" +
		"	public class Z0026Z {\n" +
		"		public class Z0026ZZ <T3, T4 extends String>{\n" +
		"			\n" +
		"		}\n" +
		"	} \n" +
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "Z<St";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0027() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src3/test0026/Test.java",
		"package test0027;\n" +
		"\n" +
		"public class Test {\n" +
		"	Z0027<St, String>.Z0027Z.Z0027ZZ<String, String> var;\n" +
		"}\n" +
		"class Z0027 <T1, T2 extends String>{\n" +
		"	public class Z0027Z {\n" +
		"		public class Z0027ZZ <T3 extends String, T4 extends String>{\n" +
		"			\n" +
		"		}\n" +
		"	} \n" +
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "7<St";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());


}
public void test0028() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0028", "Test.java");

	String str = cu.getSource();
	String completeBehind = "<St";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("should have one class",
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED),
		requestor.getResults());
}
public void test0029() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0029/Test.java",
            "package test0029;\n"+
            "\n"+
            "public class Test {\n"+
            "	public class Inner {\n"+
            "		/**/Inner2<Inner2<Object>> stack= new Inner2<Inner2<Object>>();\n"+
            "	}\n"+
            "	class Inner2<T>{\n"+
            "	}\n"+
            "}",
            "/**/Inner2");

    assertResults(
            "Inner2[POTENTIAL_METHOD_DECLARATION]{Inner2, Ltest0029.Test$Inner;, ()V, Inner2, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED)+"}\n"+
            "Test.Inner2<T>[TYPE_REF]{Inner2, test0029, Ltest0029.Test$Inner2<TT;>;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
            result.proposals);
}
public void test0030() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0030", "Test.java");

	String str = cu.getSource();
	String completeBehind = "ZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("unexpected result",
		"element:ZZX    completion:ZZX    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED)+ "\n" +
		"element:ZZY    completion:ZZY    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=72501
 */
public void test0031() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0031/Test.java",
            "package test0031;\n" +
            "\n" +
            "public class Test <T> {\n" +
            "	class Y {}\n" +
            "		void foo(){\n" +
            "			Test<T>.Y<Stri\n" +
            "		}\n" +
            "	}\n" +
            "}",
            "Stri");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "",
            result.proposals);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0032() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0032", "Test.java");

	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("unexpected result",
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0033() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0033", "Test.java");

	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("unexpected result",
		"element:String    completion:String    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0034() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0034", "Test.java");

	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0035() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0035", "Test.java");

	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0036() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0036", "Test.java");

	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0037() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0037", "Test.java");

	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0038() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0038", "Test.java");

	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0039() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0039", "Test.java");

	String str = cu.getSource();
	String completeBehind = "Stri";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("unexpected result",
		"",
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0040() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0040/Test.java",
            "package test0040;\n" +
            "\n" +
            "public class Test <T> {\n" +
            "	public class Y {\n" +
            "		public class Z <U>{\n" +
            "			\n" +
            "		}\n" +
            "	}\n" +
            "	Test<Object>.Y.Z<Stri\n" +
            "}",
            "Stri");

    assertResults(
            "expectedTypesSignatures={Ljava.lang.Object;}\n" +
            "expectedTypesKeys={Ljava/lang/Object;}",
            result.context);

    assertResults(
            "String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE  + R_UNQUALIFIED + R_NON_RESTRICTED) +"}",
            result.proposals);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0041() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0041/Test.java",
            "package test0041;\n" +
            "\n" +
            "public class Test <T> {\n" +
            "	public class Y {\n" +
            "		public class Z <U> {\n" +
            "			\n" +
            "		}\n" +
            "	}\n" +
            "	void foo() {\n" +
            "		Test<Object>.Y.Z<Stri\n" +
            "	}\n" +
            "}",
            "Stri");

    assertResults(
            "expectedTypesSignatures={Ljava.lang.Object;}\n" +
            "expectedTypesKeys={Ljava/lang/Object;}",
            result.context);

    assertResults(
            "String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED  + R_NON_RESTRICTED) +"}",
            result.proposals);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0042() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0042/Test.java",
            "package test0042;\n" +
            "\n" +
            "public class Test <T> {\n" +
            "	public class Y {\n" +
            "		public class Z {\n" +
            "			\n" +
            "		}\n" +
            "	}\n" +
            "	Test<Object>.Y.Z<Stri\n" +
            "}",
            "Stri");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "",
            result.proposals);

}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0043() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0043/Test.java",
            "package test0043;\n" +
            "\n" +
            "public class Test <T> {\n" +
            "	public class Y {\n" +
            "		public class Z {\n" +
            "			\n" +
            "		}\n" +
            "	}\n" +
            "	void foo() {\n" +
            "		Test<Object>.Y.Z<Stri\n" +
            "	}\n" +
            "}",
            "Stri");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "",
            result.proposals);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0044() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0044/Test.java",
            "package test0044;\n" +
            "\n" +
            "public class Test <T> {\n" +
            "	public class Y {\n" +
            "		public class Z <U>{\n" +
            "			\n" +
            "		}\n" +
            "	}\n" +
            "	Test<Object>.Y.Z<Object, Stri\n" +
            "}",
            "Stri");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "",
            result.proposals);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0045() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0045/Test.java",
            "package test0045;\n" +
            "\n" +
            "public class Test <T> {\n" +
            "	public class Y {\n" +
            "		public class Z <U>{\n" +
            "			\n" +
            "		}\n" +
            "	}\n" +
            "	void foo() {\n" +
            "		Test<Object>.Y.Z<Object, Stri\n" +
            "	}\n" +
            "}",
            "Stri");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "",
            result.proposals);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0046() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0046/Test.java",
            "package test0046;\n" +
            "\n" +
            "public class Test <T> {\n" +
            "	public class Y {\n" +
            "		public class Z <U>{\n" +
            "			\n" +
            "		}\n" +
            "	}\n" +
            "	Test<Object>.Y.Z<Object, Stri, Object> x;\n" +
            "}",
            "Stri");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "",
            result.proposals);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=59082
 */
public void test0047() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0047/Test.java",
            "package test0047;\n" +
            "\n" +
            "public class Test <T> {\n" +
            "	public class Y {\n" +
            "		public class Z <U>{\n" +
            "			\n" +
            "		}\n" +
            "	}\n" +
            "	void foo() {\n" +
            "		Test<Object>.Y.Z<Object, Stri, Object> x;\n" +
            "	}\n" +
            "}",
            "Stri");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "",
            result.proposals);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=75455
 */
public void test0048() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0048", "Test.java");

	String str = cu.getSource();
	String completeBehind = "l.ba";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("unexpected result",
		"element:bar    completion:bar()    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED),
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=75455
 */
public void test0049() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0049", "Test.java");

	String str = cu.getSource();
	String completeBehind = "l.ba";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("unexpected result",
		"element:bar    completion:bar()    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED),
		requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74753
 */
public void test0050() throws JavaModelException {
	CompletionTestsRequestor requestor = new CompletionTestsRequestor();
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "test0050", "Test.java");

	String str = cu.getSource();
	String completeBehind = "Test<T_0050";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertEquals("unexpected result",
		"element:T_0050    completion:T_0050    relevance:"+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED),
		requestor.getResults());
}

public void test0051() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	ICompilationUnit[] qqTypes = null;
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		qqTypes = getExternalQQTypes();

		this.wc = getWorkingCopy(
				"/Completion/src3/test0051/Test.java",
				"package test0051;\n"+
				"import static pkgstaticimport.QQType1.*;\n"+
				"public class Test {\n"+
				"	void foo() {\n"+
				"		Inner\n"+
				"	}\n"+
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		String str = this.wc.getSource();
		String completeBehind = "Inner";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"QQType1.Inner1[TYPE_REF]{pkgstaticimport.QQType1.Inner1, pkgstaticimport, Lpkgstaticimport.QQType1$Inner1;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"QQType1.Inner2[TYPE_REF]{Inner2, pkgstaticimport, Lpkgstaticimport.QQType1$Inner2;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		discardWorkingCopies(qqTypes);

		JavaCore.setOptions(this.oldOptions);
	}
}
public void test0052() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	ICompilationUnit[] qqTypes = null;
	ICompilationUnit qqType2 = null;
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		qqTypes = getExternalQQTypes();

		qqType2 = getWorkingCopy(
				"/Completion/src3/test0052/QQType2.java",
				"package test0052;\n"+
				"public class QQType2 {\n"+
				"	public class Inner1 {}\n"+
				"	public static class Inner2 {}\n"+
				"	protected class Inner3 {}\n"+
				"	protected static class Inner4 {}\n"+
				"	private class Inner5 {}\n"+
				"	private static class Inner6 {}\n"+
				"	class Inner7 {}\n"+
				"	static class Inner8 {}\n"+
				"}");

		this.wc = getWorkingCopy(
				"/Completion/src3/test0052/Test.java",
				"package test0052;\n"+
				"import static test0052.QQType2.*;\n"+
				"public class Test {\n"+
				"	void foo() {\n"+
				"		Inner\n"+
				"	}\n"+
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		String str = this.wc.getSource();
		String completeBehind = "Inner";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"QQType1.Inner1[TYPE_REF]{pkgstaticimport.QQType1.Inner1, pkgstaticimport, Lpkgstaticimport.QQType1$Inner1;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"QQType1.Inner2[TYPE_REF]{pkgstaticimport.QQType1.Inner2, pkgstaticimport, Lpkgstaticimport.QQType1$Inner2;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"QQType2.Inner1[TYPE_REF]{test0052.QQType2.Inner1, test0052, Ltest0052.QQType2$Inner1;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"QQType2.Inner3[TYPE_REF]{test0052.QQType2.Inner3, test0052, Ltest0052.QQType2$Inner3;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"QQType2.Inner7[TYPE_REF]{test0052.QQType2.Inner7, test0052, Ltest0052.QQType2$Inner7;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"QQType2.Inner2[TYPE_REF]{Inner2, test0052, Ltest0052.QQType2$Inner2;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"QQType2.Inner4[TYPE_REF]{Inner4, test0052, Ltest0052.QQType2$Inner4;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"QQType2.Inner8[TYPE_REF]{Inner8, test0052, Ltest0052.QQType2$Inner8;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		discardWorkingCopies(qqTypes);
		if(qqType2 != null) {
			qqType2.discardWorkingCopy();
		}

		JavaCore.setOptions(this.oldOptions);
	}
}
public void test0053() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	ICompilationUnit[] qqTypes = null;
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		qqTypes = getExternalQQTypes();

		this.wc = getWorkingCopy(
				"/Completion/src3/test0053/Test.java",
				"package test0053;\n"+
				"import static pkgstaticimport.QQType1.*;\n"+
				"public class Test extends pkgstaticimport.QQType1 {\n"+
				"	void foo() {\n"+
				"		Inner\n"+
				"	}\n"+
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		String str = this.wc.getSource();
		String completeBehind = "Inner";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"QQType1.Inner1[TYPE_REF]{Inner1, pkgstaticimport, Lpkgstaticimport.QQType1$Inner1;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"QQType1.Inner2[TYPE_REF]{Inner2, pkgstaticimport, Lpkgstaticimport.QQType1$Inner2;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"QQType1.Inner3[TYPE_REF]{Inner3, pkgstaticimport, Lpkgstaticimport.QQType1$Inner3;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"QQType1.Inner4[TYPE_REF]{Inner4, pkgstaticimport, Lpkgstaticimport.QQType1$Inner4;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		discardWorkingCopies(qqTypes);

		JavaCore.setOptions(this.oldOptions);
	}
}
public void test0054() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	ICompilationUnit[] qqTypes = null;
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		qqTypes = getExternalQQTypes();

		this.wc = getWorkingCopy(
				"/Completion/src3/test0054/Test.java",
				"package test0054;\n"+
				"import static pkgstaticimport.QQType1.Inner2;\n"+
				"public class Test {\n"+
				"	void foo() {\n"+
				"		Inner\n"+
				"	}\n"+
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		String str = this.wc.getSource();
		String completeBehind = "Inner";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"QQType1.Inner1[TYPE_REF]{pkgstaticimport.QQType1.Inner1, pkgstaticimport, Lpkgstaticimport.QQType1$Inner1;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"QQType1.Inner2[TYPE_REF]{Inner2, pkgstaticimport, Lpkgstaticimport.QQType1$Inner2;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		discardWorkingCopies(qqTypes);

		JavaCore.setOptions(this.oldOptions);
	}
}
public void test0055() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	ICompilationUnit[] qqTypes = null;
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		qqTypes = getExternalQQTypes();

		this.wc = getWorkingCopy(
				"/Completion/src3/test0055/Test.java",
				"package test0055;\n"+
				"import static pkgstaticimport.QQType1.*;\n"+
				"import static pkgstaticimport.QQType1.Inner2;\n"+
				"public class Test {\n"+
				"	void foo() {\n"+
				"		Inner\n"+
				"	}\n"+
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		String str = this.wc.getSource();
		String completeBehind = "Inner";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"QQType1.Inner1[TYPE_REF]{pkgstaticimport.QQType1.Inner1, pkgstaticimport, Lpkgstaticimport.QQType1$Inner1;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"QQType1.Inner2[TYPE_REF]{Inner2, pkgstaticimport, Lpkgstaticimport.QQType1$Inner2;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		discardWorkingCopies(qqTypes);

		JavaCore.setOptions(this.oldOptions);
	}
}
public void test0056() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	ICompilationUnit[] qqTypes = null;
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		qqTypes = getExternalQQTypes();

		this.wc = getWorkingCopy(
				"/Completion/src3/test0056/Test.java",
				"package test0056;\n"+
				"import static pkgstaticimport.QQType1.Inner2;\n"+
				"import static pkgstaticimport.QQType1.*;\n"+
				"public class Test {\n"+
				"	void foo() {\n"+
				"		Inner\n"+
				"	}\n"+
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		String str = this.wc.getSource();
		String completeBehind = "Inner";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"QQType1.Inner1[TYPE_REF]{pkgstaticimport.QQType1.Inner1, pkgstaticimport, Lpkgstaticimport.QQType1$Inner1;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"QQType1.Inner2[TYPE_REF]{Inner2, pkgstaticimport, Lpkgstaticimport.QQType1$Inner2;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		discardWorkingCopies(qqTypes);

		JavaCore.setOptions(this.oldOptions);
	}
}
public void test0057() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	ICompilationUnit[] qqTypes = null;
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		qqTypes = getExternalQQTypes();

		this.wc = getWorkingCopy(
				"/Completion/src3/test0056/Test.java",
				"package test0057;\n"+
				"import static pkgstaticimport.QQType3.*;\n"+
				"public class Test {\n"+
				"	void foo() {\n"+
				"		Inner\n"+
				"	}\n"+
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();

		String str = this.wc.getSource();
		String completeBehind = "Inner";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"QQType1.Inner1[TYPE_REF]{pkgstaticimport.QQType1.Inner1, pkgstaticimport, Lpkgstaticimport.QQType1$Inner1;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"QQType1.Inner2[TYPE_REF]{Inner2, pkgstaticimport, Lpkgstaticimport.QQType1$Inner2;, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		discardWorkingCopies(qqTypes);

		JavaCore.setOptions(this.oldOptions);
	}
}
public void test0058() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	ICompilationUnit[] qqTypes = null;
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		qqTypes = getExternalQQTypes();

		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test0058/Test.java",
			"package test0058;\n"+
			"import static pkgstaticimport.QQType4.*;\n"+
			"public class Test {\n"+
			"	void foo() {\n"+
			"		zzvarzz\n"+
			"	}\n"+
			"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "zzvarzz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"zzvarzz2[FIELD_REF]{zzvarzz2, Lpkgstaticimport.QQType4;, I, zzvarzz2, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());

	} finally {
		discardWorkingCopies(qqTypes);

		JavaCore.setOptions(this.oldOptions);
	}
}
public void test0059() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	ICompilationUnit[] qqTypes = null;
	ICompilationUnit qqType5 = null;
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		qqTypes = getExternalQQTypes();

		qqType5 = getWorkingCopy(
				"/Completion/src3/test0059/QQType5.java",
				"package test0059;\n"+
				"\n"+
				"public class QQType5 {\n"+
				"	public int zzvarzz1;\n"+
				"	public static int zzvarzz2;\n"+
				"	protected int zzvarzz3;\n"+
				"	protected static int zzvarzz4;\n"+
				"	private int zzvarzz5;\n"+
				"	private static int zzvarzz6;\n"+
				"	int zzvarzz7;\n"+
				"	static int zzvarzz8;\n"+
				"}");

		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test0059/Test.java",
			"package test0059;\n"+
			"import static test0059.QQType5.*;\n"+
			"public class Test {\n"+
			"	void foo() {\n"+
			"		zzvarzz\n"+
			"	}\n"+
			"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "zzvarzz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"zzvarzz2[FIELD_REF]{zzvarzz2, Ltest0059.QQType5;, I, zzvarzz2, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzvarzz4[FIELD_REF]{zzvarzz4, Ltest0059.QQType5;, I, zzvarzz4, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzvarzz8[FIELD_REF]{zzvarzz8, Ltest0059.QQType5;, I, zzvarzz8, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		discardWorkingCopies(qqTypes);
		if(qqType5 != null) {
			qqType5.discardWorkingCopy();
		}
		JavaCore.setOptions(this.oldOptions);
	}
}
public void test0060() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	ICompilationUnit[] qqTypes = null;
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		qqTypes = getExternalQQTypes();

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0060", "Test.java");

		String str = cu.getSource();
		String completeBehind = "zzvarzz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"zzvarzz1[FIELD_REF]{zzvarzz1, Lpkgstaticimport.QQType4;, I, zzvarzz1, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzvarzz2[FIELD_REF]{zzvarzz2, Lpkgstaticimport.QQType4;, I, zzvarzz2, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzvarzz3[FIELD_REF]{zzvarzz3, Lpkgstaticimport.QQType4;, I, zzvarzz3, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzvarzz4[FIELD_REF]{zzvarzz4, Lpkgstaticimport.QQType4;, I, zzvarzz4, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		discardWorkingCopies(qqTypes);

		JavaCore.setOptions(this.oldOptions);
	}
}
public void test0061() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	ICompilationUnit[] qqTypes = null;
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		qqTypes = getExternalQQTypes();

		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test0061/Test.java",
			"package test0061;\n" +
			"import static pkgstaticimport.QQType4.zzvarzz2;\n" +
			"public class Test {\n" +
			"	void foo() {\n" +
			"		zzvarzz\n" +
			"	}\n" +
			"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "zzvarzz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"zzvarzz2[FIELD_REF]{zzvarzz2, Lpkgstaticimport.QQType4;, I, zzvarzz2, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		discardWorkingCopies(qqTypes);

		JavaCore.setOptions(this.oldOptions);
	}
}
public void test0062() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	ICompilationUnit[] qqTypes = null;
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		qqTypes = getExternalQQTypes();

		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test0062/Test.java",
			"package test0062;\n" +
			"import static pkgstaticimport.QQType4.*;\n" +
			"import static pkgstaticimport.QQType4.zzvarzz2;\n" +
			"public class Test {\n" +
			"	void foo() {\n" +
			"		zzvarzz\n" +
			"	}\n" +
			"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "zzvarzz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"zzvarzz2[FIELD_REF]{zzvarzz2, Lpkgstaticimport.QQType4;, I, zzvarzz2, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		discardWorkingCopies(qqTypes);

		JavaCore.setOptions(this.oldOptions);
	}
}
public void test0063() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	ICompilationUnit[] qqTypes = null;
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		qqTypes = getExternalQQTypes();

		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test0063/Test.java",
			"package test0063;\n" +
			"import static pkgstaticimport.QQType4.zzvarzz2;\n" +
			"import static pkgstaticimport.QQType4.*;\n" +
			"public class Test {\n" +
			"	void foo() {\n" +
			"		zzvarzz\n" +
			"	}\n" +
			"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "zzvarzz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"zzvarzz2[FIELD_REF]{zzvarzz2, Lpkgstaticimport.QQType4;, I, zzvarzz2, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		discardWorkingCopies(qqTypes);

		JavaCore.setOptions(this.oldOptions);
	}
}
public void test0064() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	ICompilationUnit[] qqTypes = null;
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		qqTypes = getExternalQQTypes();

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0064", "Test.java");

		String str = cu.getSource();
		String completeBehind = "zzvarzz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"zzvarzz2[FIELD_REF]{zzvarzz2, Lpkgstaticimport.QQType4;, I, zzvarzz2, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		discardWorkingCopies(qqTypes);

		JavaCore.setOptions(this.oldOptions);
	}
}
public void test0065() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	ICompilationUnit[] qqTypes = null;
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		qqTypes = getExternalQQTypes();

		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test0065/Test.java",
			"package test0065;\n" +
			"import static pkgstaticimport.QQType7.*;\n" +
			"public class Test {\n" +
			"	void foo() {\n" +
			"		zzfoozz\n" +
			"	}\n" +
			"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "zzfoozz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"zzfoozz2[METHOD_REF]{zzfoozz2(), Lpkgstaticimport.QQType7;, ()V, zzfoozz2, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		discardWorkingCopies(qqTypes);

		JavaCore.setOptions(this.oldOptions);
	}
}
public void test0066() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	ICompilationUnit[] qqTypes = null;
	ICompilationUnit qqType8 = null;
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		qqTypes = getExternalQQTypes();

		qqType8 = getWorkingCopy(
				"/Completion/src3/test0066/QQType8.java",
				"package test0066;\n"+
				"\n"+
				"public class QQType8 {\n"+
				"	public void zzfoozz1(){};\n"+
				"	public static void zzfoozz2(){};\n"+
				"	protected void zzfoozz3(){};\n"+
				"	protected static void zzfoozz4(){};\n"+
				"	private void zzfoozz5(){};\n"+
				"	private static void zzfoozz6(){};\n"+
				"	void zzfoozz7(){};\n"+
				"	static void zzfoozz8(){};\n"+
				"}");

		this.workingCopies = new ICompilationUnit[1];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test0066/Test.java",
			"package test0066;\n" +
			"import static test0066.QQType8.*;\n" +
			"public class Test {\n" +
			"	void foo() {\n" +
			"		zzfoozz\n" +
			"	}\n" +
			"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		String str = this.workingCopies[0].getSource();
		String completeBehind = "zzfoozz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"zzfoozz2[METHOD_REF]{zzfoozz2(), Ltest0066.QQType8;, ()V, zzfoozz2, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzfoozz4[METHOD_REF]{zzfoozz4(), Ltest0066.QQType8;, ()V, zzfoozz4, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzfoozz8[METHOD_REF]{zzfoozz8(), Ltest0066.QQType8;, ()V, zzfoozz8, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		discardWorkingCopies(qqTypes);
		if(qqType8 != null) {
			qqType8.discardWorkingCopy();
		}
		JavaCore.setOptions(this.oldOptions);
	}
}
public void test0067() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	ICompilationUnit[] qqTypes = null;
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		qqTypes = getExternalQQTypes();

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0067", "Test.java");

		String str = cu.getSource();
		String completeBehind = "zzfoozz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"zzfoozz1[METHOD_REF]{zzfoozz1(), Lpkgstaticimport.QQType7;, ()V, zzfoozz1, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzfoozz2[METHOD_REF]{zzfoozz2(), Lpkgstaticimport.QQType7;, ()V, zzfoozz2, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzfoozz3[METHOD_REF]{zzfoozz3(), Lpkgstaticimport.QQType7;, ()V, zzfoozz3, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzfoozz4[METHOD_REF]{zzfoozz4(), Lpkgstaticimport.QQType7;, ()V, zzfoozz4, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		discardWorkingCopies(qqTypes);

		JavaCore.setOptions(this.oldOptions);
	}
}
public void test0068() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	ICompilationUnit[] qqTypes = null;
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		qqTypes = getExternalQQTypes();

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0068", "Test.java");

		String str = cu.getSource();
		String completeBehind = "zzfoozz";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"zzfoozz2[METHOD_REF]{zzfoozz2(), Lpkgstaticimport.QQType7;, ()V, zzfoozz2, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		discardWorkingCopies(qqTypes);

		JavaCore.setOptions(this.oldOptions);
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=74295
 */
public void test0069() throws JavaModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0069", "Test.java");

	String str = cu.getSource();
	String completeBehind = "icell.p";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertResults(
			"putValue[METHOD_REF]{putValue(), Ltest0069.Test<Ljava.lang.String;>;, (Ljava.lang.String;)V, putValue, (value), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77573
 */
public void test0070() throws JavaModelException {
	ICompilationUnit importedClass = null;
	try {
		importedClass = getWorkingCopy(
				"/Completion/src3/test0070/p/ImportedClass.java",
				"package test0070.p;\n"+
				"\n"+
				"public class ImportedClass {\n"+
				"	\n"+
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0070", "Test.java");

		String str = cu.getSource();
		String completeBehind = "test0070";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"test0070.p[PACKAGE_REF]{test0070.p., test0070.p, null, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"test0070[PACKAGE_REF]{test0070., test0070, null, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		if(importedClass != null) {
			importedClass.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77573
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94303
 */
public void test0071() throws JavaModelException {
	ICompilationUnit importedClass = null;
	try {
		importedClass = getWorkingCopy(
				"/Completion/src3/test0071/p/ImportedClass.java",
				"package test0071.p;\n"+
				"\n"+
				"public class ImportedClass {\n"+
				"	\n"+
				"}");

		CompletionResult result = complete(
	            "/Completion/src3/test0071/Test.java",
	            "package test0071;\n" +
	            "\n" +
	            "import static test0071.p.Im\n" +
	            "\n" +
	            "public class Test {\n" +
	            "	\n" +
	            "}",
            	"test0071.p.Im");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

		assertResults(
				"ImportedClass[TYPE_REF]{ImportedClass., test0071.p, Ltest0071.p.ImportedClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(importedClass != null) {
			importedClass.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77573
 */
public void test0072() throws JavaModelException {
	ICompilationUnit importedClass = null;
	try {
		importedClass = getWorkingCopy(
				"/Completion/src3/test0072/p/ImportedClass.java",
				"package test0072.p;\n"+
				"\n"+
				"public class ImportedClass {\n"+
				"	public static int ZZZ1;\n"+
				"	public static void ZZZ2() {}\n"+
				"	public static void ZZZ2(int i) {}\n"+
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true);
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0072", "Test.java");

		String str = cu.getSource();
		String completeBehind = "test0072.p.ImportedClass.ZZ";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor, this.wcOwner);

		int end = cursorLocation;
		int start = end - "ZZ".length();

		assertResults(
				"ZZZ1[FIELD_REF]{ZZZ1;, Ltest0072.p.ImportedClass;, I, ZZZ1, null, ["+start+", "+end+"], " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"ZZZ2[METHOD_NAME_REFERENCE]{ZZZ2;, Ltest0072.p.ImportedClass;, ()V, ZZZ2, null, ["+start+", "+end+"], " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"ZZZ2[METHOD_NAME_REFERENCE]{ZZZ2;, Ltest0072.p.ImportedClass;, (I)V, ZZZ2, (i), ["+start+", "+end+"], " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		if(importedClass != null) {
			importedClass.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77573
 */
public void test0073() throws JavaModelException {
	ICompilationUnit importedClass = null;
	try {
		importedClass = getWorkingCopy(
				"/Completion/src3/test0073/p/ImportedClass.java",
				"package test0073.p;\n"+
				"\n"+
				"public class ImportedClass {\n"+
				"	public static class Inner {\n"+
				"		public static int ZZZ1;\n"+
				"		public static void ZZZ2() {}\n"+
				"		public static void ZZZ2(int i) {}\n"+
				"	}\n"+
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true);
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0073", "Test.java");

		String str = cu.getSource();
		String completeBehind = "test0073.p.ImportedClass.Inner.ZZ";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor, this.wcOwner);

		int end = cursorLocation;
		int start = end - "ZZ".length();

		assertResults(
				"ZZZ1[FIELD_REF]{ZZZ1;, Ltest0073.p.ImportedClass$Inner;, I, ZZZ1, null, ["+start+", "+end+"], " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"ZZZ2[METHOD_NAME_REFERENCE]{ZZZ2;, Ltest0073.p.ImportedClass$Inner;, ()V, ZZZ2, null, ["+start+", "+end+"], " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"ZZZ2[METHOD_NAME_REFERENCE]{ZZZ2;, Ltest0073.p.ImportedClass$Inner;, (I)V, ZZZ2, (i), ["+start+", "+end+"], " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		if(importedClass != null) {
			importedClass.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=77573
 */
public void test0074() throws JavaModelException {
	ICompilationUnit importedClass = null;
	try {
		importedClass = getWorkingCopy(
				"/Completion/src3/test0074/p/ImportedClass.java",
				"package test0074.p;\n"+
				"\n"+
				"public class ImportedClass {\n"+
				"	public class Inner {\n"+
				"		public static int ZZZ1;\n"+
				"		public static void ZZZ2() {}\n"+
				"		public static void ZZZ2(int i) {}\n"+
				"	}\n"+
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true);
		ICompilationUnit cu= getCompilationUnit("Completion", "src3", "test0074", "Test.java");

		String str = cu.getSource();
		String completeBehind = "test0074.p.ImportedClass.Inner.ZZ";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		cu.codeComplete(cursorLocation, requestor, this.wcOwner);

		int end = cursorLocation;
		int start = end - "ZZ".length();

		assertResults(
				"ZZZ1[FIELD_REF]{ZZZ1;, Ltest0074.p.ImportedClass$Inner;, I, ZZZ1, null, ["+start+", "+end+"], " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"ZZZ2[METHOD_NAME_REFERENCE]{ZZZ2;, Ltest0074.p.ImportedClass$Inner;, ()V, ZZZ2, null, ["+start+", "+end+"], " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"ZZZ2[METHOD_NAME_REFERENCE]{ZZZ2;, Ltest0074.p.ImportedClass$Inner;, (I)V, ZZZ2, (i), ["+start+", "+end+"], " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		if(importedClass != null) {
			importedClass.discardWorkingCopy();
		}
	}
}
public void test0075() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test0075/Test.java",
			"package test0075;\n" +
			"public @QQAnnot class Test {\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/pkgannotations/QQAnnotation.java",
		"package pkgannotations;"+
		"public @interface QQAnnotation {\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@QQAnnot";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0076() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test0076/Test.java",
			"package test0076;\n" +
			"public @QQAnnot class Test\n" +
			"");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/pkgannotations/QQAnnotation.java",
		"package pkgannotations;"+
		"public @interface QQAnnotation {\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@QQAnnot";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0077() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test0077/Test.java",
			"package test0077;\n" +
			"public @QQAnnot\n" +
			"");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/pkgannotations/QQAnnotation.java",
		"package pkgannotations;"+
		"public @interface QQAnnotation {\n"+
		"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@QQAnnot";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0078() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test0078/Test.java",
			"package test0078;\n" +
			"public class Test {\n" +
			"  public @QQAnnot void foo() {\n" +
			"  }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/pkgannotations/QQAnnotation.java",
		"package pkgannotations;"+
		"public @interface QQAnnotation {\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@QQAnnot";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0079() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test0078/Test.java",
			"package test0078;\n" +
			"public class Test {\n" +
			"  public @QQAnnot void foo(\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/pkgannotations/QQAnnotation.java",
		"package pkgannotations;"+
		"public @interface QQAnnotation {\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@QQAnnot";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0080() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test0078/Test.java",
			"package test0078;\n" +
			"public class Test {\n" +
			"  public @QQAnnot int var;\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/pkgannotations/QQAnnotation.java",
		"package pkgannotations;"+
		"public @interface QQAnnotation {\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@QQAnnot";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0081() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test0078/Test.java",
			"package test0078;\n" +
			"public class Test {\n" +
			"  public @QQAnnot int var\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/pkgannotations/QQAnnotation.java",
		"package pkgannotations;"+
		"public @interface QQAnnotation {\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@QQAnnot";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0082() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test0078/Test.java",
			"package test0078;\n" +
			"public class Test {\n" +
			"  void foo(@QQAnnot int i) {}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/pkgannotations/QQAnnotation.java",
		"package pkgannotations;"+
		"public @interface QQAnnotation {\n"+
		"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@QQAnnot";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0083() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test0078/Test.java",
			"package test0078;\n" +
			"public class Test {\n" +
			"  void foo() {@QQAnnot int i}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/pkgannotations/QQAnnotation.java",
		"package pkgannotations;"+
		"public @interface QQAnnotation {\n"+
		"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@QQAnnot";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0084() throws JavaModelException {
	ICompilationUnit imported = null;
	try {
		imported = getWorkingCopy(
				"/Completion/src3/pkgstaticimport/MyClass0084.java",
				"package pkgstaticimport;\n" +
				"public class MyClass0084 {\n" +
				"   public static int foo() {return 0;}\n" +
				"   public static int foo(int i) {return 0;}\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0084/Test.java",
				"package test0084;\n" +
				"import static pkgstaticimport.MyClass0084.foo;\n" +
				"public class Test {\n" +
				"  void bar() {\n" +
				"    int i = foo\n" +
				"  }\n" +
				"}",
				"foo");

		assertResults(
				"foo[METHOD_REF]{foo(), Lpkgstaticimport.MyClass0084;, ()I, foo, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"foo[METHOD_REF]{foo(), Lpkgstaticimport.MyClass0084;, (I)I, foo, (i), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(imported != null) {
			imported.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=85290
public void test0085() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0085/TestAnnotation.java",
			"package test0085;\n" +
			"public @interface TestAnnotation {\n" +
			"}\n" +
			"@TestAnnotati\n" +
			"class Test2 {\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "@TestAnnotati";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"TestAnnotation[TYPE_REF]{TestAnnotation, test0085, Ltest0085.TestAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=85290
public void test0086() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/TestAnnotation.java",
			"public @interface TestAnnotation {\n" +
			"}\n" +
			"@TestAnnotati\n" +
			"class Test2 {\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "@TestAnnotati";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"TestAnnotation[TYPE_REF]{TestAnnotation, , LTestAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=85402
public void test0087() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0087/TestAnnotation.java",
			"package test0087;\n" +
			"public @interface TestAnnotation {\n" +
			"}\n" +
			"@\n" +
			"class Test2 {\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "@";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	if(CompletionEngine.NO_TYPE_COMPLETION_ON_EMPTY_TOKEN) {
		assertResults(
				"",
				requestor.getResults());
	} else {
		assertResults(
				"interface[KEYWORD]{interface, null, null, interface, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
				"YAAnnot[TYPE_REF]{testxxx.YAAnnot, testxxx, Ltestxxx.YAAnnot;, null, null, " + (R_NAME_PREFIX + R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
				"_ConfigurationData[TYPE_REF]{test325481._ConfigurationData, test325481, Ltest325481._ConfigurationData;, null, null, " + (R_NAME_PREFIX + R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
				"_Path[TYPE_REF]{test325481._Path, test325481, Ltest325481._Path;, null, null, " + (R_NAME_PREFIX + R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
				"Deprecated[TYPE_REF]{Deprecated, java.lang, Ljava.lang.Deprecated;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED + R_UNQUALIFIED) + "}\n" +
				"TestAnnotation[TYPE_REF]{TestAnnotation, test0087, Ltest0087.TestAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
				requestor.getResults());
	}
}
public void test0088() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0088/TestAnnotation.java",
			"package test0088;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"@TestAnnotation(foo)\n" +
			"class Test2 {\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0088.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0089() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0089/TestAnnotation.java",
			"package test0089;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo)\n" +
			"  void bar(){}\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0089.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0090() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0090/TestAnnotation.java",
			"package test0090;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo)\n" +
			"  int var;\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0090.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0091() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0091/TestAnnotation.java",
			"package test0091;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(){\n" +
			"    @TestAnnotation(foo)\n" +
			"    int var;\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0091.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0092() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0092/TestAnnotation.java",
			"package test0092;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(int var1, @TestAnnotation(foo) int var2){\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0092.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0093() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0093/TestAnnotation.java",
			"package test0093;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo)\n" +
			"  Test2(){\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0093.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0094() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0094/TestAnnotation.java",
			"package test0094;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"@TestAnnotation(foo\n" +
			"class Test2 {\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0094.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0095() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0095/TestAnnotation.java",
			"package test0095;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo\n" +
			"  void bar(){}\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0095.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0096() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0096/TestAnnotation.java",
			"package test0096;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo\n" +
			"  int var;\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0096.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0097() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0097/TestAnnotation.java",
			"package test0097;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(){\n" +
			"    @TestAnnotation(foo\n" +
			"    int var;\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0097.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0098() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0098/TestAnnotation.java",
			"package test0098;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(int var1, @TestAnnotation(foo int var2){\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0098.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0099() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0099/TestAnnotation.java",
			"package test0099;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo\n" +
			"  Test2(){\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0099.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0100() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0100/TestAnnotation.java",
			"package test0100;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"@TestAnnotation(foo=\"\")\n" +
			"class Test2 {\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0100.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0101() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0101/TestAnnotation.java",
			"package test00101;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo=\"\")\n" +
			"  void bar(){}\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0101.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0102() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0102/TestAnnotation.java",
			"package test0102;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo=\"\")\n" +
			"  int var;\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0102.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0103() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0103/TestAnnotation.java",
			"package test00103;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(){\n" +
			"    @TestAnnotation(foo=\"\")\n" +
			"    int var;\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0103.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0104() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0104/TestAnnotation.java",
			"package test0104;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(int var1, @TestAnnotation(foo=\"\") int var2){\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0104.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0105() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0105/TestAnnotation.java",
			"package test0105;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo=\"\")\n" +
			"  Test2(){\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0105.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0106() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0106/TestAnnotation.java",
			"package test0106;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"@TestAnnotation(foo=\"\"\n" +
			"class Test2 {\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0106.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0107() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0107/TestAnnotation.java",
			"package test0107;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo=\"\"\n" +
			"  void bar(){}\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0107.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0108() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0108/TestAnnotation.java",
			"package test0108;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo=\"\"\n" +
			"  int var;\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0108.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0109() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0109/TestAnnotation.java",
			"package test0109;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(){\n" +
			"    @TestAnnotation(foo=\"\"\n" +
			"    int var;\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0109.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0110() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0110/TestAnnotation.java",
			"package test0110;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(int var1, @TestAnnotation(foo=\"\" int var2){\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0110.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0111() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0111/TestAnnotation.java",
			"package test0111;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo=\"\"\n" +
			"  Test2(){\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0111.TestAnnotation;, Ljava.lang.String;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0112() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0112/TestAnnotation.java",
			"package test0112;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"@TestAnnotation(foo1=\"\", foo)\n" +
			"class Test2 {\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0112.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0113() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0113/TestAnnotation.java",
			"package test0113;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo)\n" +
			"  void bar(){}\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0113.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0114() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0114/TestAnnotation.java",
			"package test0114;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo)\n" +
			"  int var;\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0114.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0115() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0115/TestAnnotation.java",
			"package test0115;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(){\n" +
			"    @TestAnnotation(foo1=\"\", foo)\n" +
			"    int var;\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0115.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0116() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0116/TestAnnotation.java",
			"package test0116;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(int var1, @TestAnnotation(foo1=\"\", foo) int var2){\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0116.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0117() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0117/TestAnnotation.java",
			"package test0117;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo)\n" +
			"  Test2(){\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0117.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0118() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0118/TestAnnotation.java",
			"package test0118;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"@TestAnnotation(foo1=\"\", foo\n" +
			"class Test2 {\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0118.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0119() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0119/TestAnnotation.java",
			"package test0119;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo\n" +
			"  void bar(){}\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0119.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0120() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0120/TestAnnotation.java",
			"package test0120;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo\n" +
			"  int var;\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0120.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0121() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0121/TestAnnotation.java",
			"package test0121;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(){\n" +
			"    @TestAnnotation(foo1=\"\", foo\n" +
			"    int var;\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0121.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0122() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0122/TestAnnotation.java",
			"package test0122;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(int var1, @TestAnnotation(foo1=\"\", foo int var2){\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0122.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0123() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0123/TestAnnotation.java",
			"package test0123;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo\n" +
			"  Test2(){\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0123.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0124() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0124/TestAnnotation.java",
			"package test0124;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"@TestAnnotation(foo1=\"\", foo=\"\")\n" +
			"class Test2 {\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0124.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0125() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0125/TestAnnotation.java",
			"package test0125;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo=\"\")\n" +
			"  void bar(){}\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0125.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0126() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0126/TestAnnotation.java",
			"package test0126;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo=\"\")\n" +
			"  int var;\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0126.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0127() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0127/TestAnnotation.java",
			"package test0127;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(){\n" +
			"    @TestAnnotation(foo1=\"\", foo=\"\")\n" +
			"    int var;\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0127.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0128() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0128/TestAnnotation.java",
			"package test0128;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(int var1, @TestAnnotation(foo1=\"\", foo=\"\") int var2){\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0128.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0129() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0129/TestAnnotation.java",
			"package test0129;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo=\"\")\n" +
			"  Test2(){\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0129.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0130() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0130/TestAnnotation.java",
			"package test0130;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"@TestAnnotation(foo1=\"\", foo=\"\"\n" +
			"class Test2 {\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0130.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0131() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0131/TestAnnotation.java",
			"package test0131;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo=\"\"\n" +
			"  void bar(){}\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0131.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0132() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0132/TestAnnotation.java",
			"package test0132;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo=\"\"\n" +
			"  int var;\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0132.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0133() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0133/TestAnnotation.java",
			"package test0133;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(){\n" +
			"    @TestAnnotation(foo1=\"\", foo=\"\"\n" +
			"    int var;\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0133.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0134() throws JavaModelException {
	this.wc = getWorkingCopy(
			"/Completion/src3/test0134/TestAnnotation.java",
			"package test0134;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  void bar(int var1, @TestAnnotation(foo1=\"\", foo=\"\" int var2){\n" +
			"  }\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.wc.getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.wc.codeComplete(cursorLocation, requestor);

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0134.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
public void test0135() throws JavaModelException {
	CompletionResult result = complete(
			"/Completion/src3/test0135/TestAnnotation.java",
			"package test0135;\n" +
			"public @interface TestAnnotation {\n" +
			"  String foo1();\n" +
			"  String foo2();\n" +
			"}\n" +
			"class Test2 {\n" +
			"  @TestAnnotation(foo1=\"\", foo=\"\"\n" +
			"  Test2(){\n" +
			"  }\n" +
			"}",
			"foo");

	assertResults(
			"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0135.TestAnnotation;, Ljava.lang.String;, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0136() throws JavaModelException {
	ICompilationUnit enumeration = null;
	try {
		enumeration = getWorkingCopy(
				"/Completion/src3/test0136/Colors.java",
				"package test0136;\n" +
				"public enum Colors {\n" +
				"  RED, BLUE, WHITE;\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0136/Test.java",
				"package test0136;\n" +
				"public class Test {\n" +
				"  void bar(Colors c) {\n" +
				"    switch(c) {\n" +
				"      case RED :\n" +
				"        break;\n" +
				"    }\n" +
				"  }\n" +
				"}",
				"RED");

		assertResults(
				"expectedTypesSignatures={Ltest0136.Colors;}\n" +
				"expectedTypesKeys={Ltest0136/Colors;}",
				result.context);

		assertResults(
				"RED[FIELD_REF]{RED, Ltest0136.Colors;, Ltest0136.Colors;, RED, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED + R_ENUM + R_ENUM_CONSTANT + R_RESOLVED) + "}",
				result.proposals);
	} finally {
		if(enumeration != null) {
			enumeration.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0137() throws JavaModelException {
	ICompilationUnit enumeration = null;
	try {
		enumeration = getWorkingCopy(
				"/Completion/src3/test0137/Colors.java",
				"package test0137;\n" +
				"public enum Colors {\n" +
				"  RED, BLUE, WHITE;\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0137/Test.java",
				"package test0137;\n" +
				"public class Test {\n" +
				"  void bar(Colors c) {\n" +
				"    switch(c) {\n" +
				"      case BLUE :\n" +
				"      case RED :\n" +
				"        break;\n" +
				"    }\n" +
				"  }\n" +
				"}",
				"RED");

		assertResults(
				"expectedTypesSignatures={Ltest0137.Colors;}\n" +
				"expectedTypesKeys={Ltest0137/Colors;}",
				result.context);

		assertResults(
				"RED[FIELD_REF]{RED, Ltest0137.Colors;, Ltest0137.Colors;, RED, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED + R_ENUM + R_ENUM_CONSTANT + R_RESOLVED) + "}",
				result.proposals);
	} finally {
		if(enumeration != null) {
			enumeration.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0138() throws JavaModelException {
	ICompilationUnit enumeration = null;
	try {
		enumeration = getWorkingCopy(
				"/Completion/src3/test0138/Colors.java",
				"package test0138;\n" +
				"public enum Colors {\n" +
				"  RED, BLUE, WHITE;\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0138/Test.java",
				"package test0138;\n" +
				"public class Test {\n" +
				"  void bar(Colors c) {\n" +
				"    switch(c) {\n" +
				"      case BLUE :\n" +
				"        break;\n" +
				"      case RED :\n" +
				"        break;\n" +
				"    }\n" +
				"  }\n" +
				"}",
				"RED");

		assertResults(
				"expectedTypesSignatures={Ltest0138.Colors;}\n" +
				"expectedTypesKeys={Ltest0138/Colors;}",
				result.context);

		assertResults(
				"RED[FIELD_REF]{RED, Ltest0138.Colors;, Ltest0138.Colors;, RED, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED + R_ENUM + R_ENUM_CONSTANT + R_RESOLVED) + "}",
				result.proposals);
	} finally {
		if(enumeration != null) {
			enumeration.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0139() throws JavaModelException {
	ICompilationUnit enumeration = null;
	try {
		enumeration = getWorkingCopy(
				"/Completion/src3/test0139/Colors.java",
				"package test0139;\n" +
				"public enum Colors {\n" +
				"  RED, BLUE, WHITE;\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0139/Test.java",
				"package test0139;\n" +
				"public class Test {\n" +
				"  void bar(Colors c) {\n" +
				"    switch(c) {\n" +
				"      case BLUE :\n" +
				"        break;\n" +
				"      case RED :\n" +
				"    }\n" +
				"  }\n" +
				"}",
				"RED");

		assertResults(
				"expectedTypesSignatures={Ltest0139.Colors;}\n" +
				"expectedTypesKeys={Ltest0139/Colors;}",
				result.context);

		assertResults(
				"RED[FIELD_REF]{RED, Ltest0139.Colors;, Ltest0139.Colors;, RED, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED + R_ENUM + R_ENUM_CONSTANT + R_RESOLVED) + "}",
				result.proposals);
	} finally {
		if(enumeration != null) {
			enumeration.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0140() throws JavaModelException {
	ICompilationUnit enumeration = null;
	try {
		enumeration = getWorkingCopy(
				"/Completion/src3/test0140/Colors.java",
				"package test0140;\n" +
				"public enum Colors {\n" +
				"  RED, BLUE, WHITE;\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0140/Test.java",
				"package test0140;\n" +
				"public class Test {\n" +
				"  void bar(Colors c) {\n" +
				"    switch(c) {\n" +
				"      case BLUE :\n" +
				"        break;\n" +
				"      case RED\n" +
				"    }\n" +
				"  }\n" +
				"}",
				"RED");

		assertResults(
				"expectedTypesSignatures={Ltest0140.Colors;}\n" +
				"expectedTypesKeys={Ltest0140/Colors;}",
				result.context);

		assertResults(
				"RED[FIELD_REF]{RED, Ltest0140.Colors;, Ltest0140.Colors;, RED, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED + R_ENUM + R_ENUM_CONSTANT + R_RESOLVED) + "}",
				result.proposals);
	} finally {
		if(enumeration != null) {
			enumeration.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
public void test0141() throws JavaModelException {
	ICompilationUnit enumeration = null;
	try {
		enumeration = getWorkingCopy(
				"/Completion/src3/test0141/Colors.java",
				"package test0141;\n" +
				"public class Colors {\n" +
				"  public final static int RED = 0, BLUE = 1, WHITE = 3;\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0141/Test.java",
				"package test0141;\n" +
				"public class Test {\n" +
				"  void bar(Colors c) {\n" +
				"    switch(c) {\n" +
				"      case BLUE :\n" +
				"        break;\n" +
				"      case RED :\n" +
				"        break;\n" +
				"    }\n" +
				"  }\n" +
				"}",
				"RED");

		assertResults(
				"expectedTypesSignatures={Ltest0141.Colors;}\n" +
				"expectedTypesKeys={Ltest0141/Colors;}",
				result.context);

		assertResults(
				"",
				result.proposals);
	} finally {
		if(enumeration != null) {
			enumeration.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=84554
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=88295
public void test0142() throws JavaModelException {
	ICompilationUnit enumeration = null;
	try {
		enumeration = getWorkingCopy(
				"/Completion/src3/test0142/Colors.java",
				"package test0142;\n" +
				"public enum Colors {\n" +
				"  RED, BLUE, WHITE;\n" +
				"  public static final int RED2 = 0;\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0142/Test.java",
				"package test0142;\n" +
				"public class Test {\n" +
				"  void bar(Colors REDc) {\n" +
				"    switch(REDc) {\n" +
				"      case BLUE :\n" +
				"        break;\n" +
				"      case RED:\n" +
				"        break;\n" +
				"    }\n" +
				"  }\n" +
				"}",
				"RED");

		assertResults(
				"expectedTypesSignatures={Ltest0142.Colors;}\n" +
				"expectedTypesKeys={Ltest0142/Colors;}",
				result.context);

		assertResults(
				"RED[FIELD_REF]{RED, Ltest0142.Colors;, Ltest0142.Colors;, RED, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_NAME + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED + R_ENUM + R_ENUM_CONSTANT + R_RESOLVED) + "}",
				result.proposals);
	} finally {
		if(enumeration != null) {
			enumeration.discardWorkingCopy();
		}
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=88756
public void test0143() throws JavaModelException {
	Hashtable oldCurrentOptions = JavaCore.getOptions();
	ICompilationUnit enumeration = null;
	try {
		Hashtable options = new Hashtable(oldCurrentOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.DISABLED);
		JavaCore.setOptions(options);

		enumeration = getWorkingCopy(
				"/Completion/src3/test0143/Colors.java",
				"package test0143;\n" +
				"public enum Colors {\n" +
				"  RED, BLUE, WHITE;\n" +
				"  private Colors(){};\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0143/Test.java",
				"package test0143;\n" +
				"public class Test {\n" +
				"  void bar() {\n" +
				"    new Colors(\n" +
				"  }\n" +
				"}",
				"Colors(");

		assertResults(
				"expectedTypesSignatures=null\n" +
				"expectedTypesKeys=null",
				result.context);

		assertResults(
				"",
				result.proposals);
	} finally {
		if(enumeration != null) {
			enumeration.discardWorkingCopy();
		}
		JavaCore.setOptions(oldCurrentOptions);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=88845
public void test0144() throws JavaModelException {
	ICompilationUnit aClass = null;
	Hashtable oldCurrentOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(oldCurrentOptions);
		options.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		aClass = getWorkingCopy(
				"/Completion/src3/test0144/X.java",
				"package test0144;\n" +
				"public class X {\n" +
				"  public class Y {}\n" +
				"  private class Y2 {}\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0144/Test.java",
				"package test0144;\n" +
				"public class Test extends X.\n" +
				"{}",
				"X.");

		assertResults(
				"expectedTypesSignatures=null\n" +
				"expectedTypesKeys=null",
				result.context);

		assertResults(
				"X.Y[TYPE_REF]{Y, test0144, Ltest0144.X$Y;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_CLASS + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
		JavaCore.setOptions(oldCurrentOptions);
	}
}
// complete annotation attribute value
public void test0145() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0145/ZZAnnotation.java",
				"package test0145;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0145/ZZClass.java",
				"package test0145;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0145/Test.java",
				"package test0145;\n" +
				"@ZZAnnotation(foo1=ZZ)\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0145, Ltest0145.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0145, Ltest0145.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0146() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0146/ZZAnnotation.java",
				"package test0146;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0146/ZZClass.java",
				"package test0146;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0146/Test.java",
				"package test0146;\n" +
				"@ZZAnnotation(foo1= 0 + ZZ)\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={S,I,J,F,D,C,B,Ljava.lang.String;}\n" +
				"expectedTypesKeys={S,I,J,F,D,C,B,Ljava/lang/String;}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0146, Ltest0146.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0146, Ltest0146.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0147() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0147/ZZAnnotation.java",
				"package test0147;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int[] foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0147/ZZClass.java",
				"package test0147;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0147/Test.java",
				"package test0147;\n" +
				"@ZZAnnotation(foo1= {ZZ})\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0147, Ltest0147.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0147, Ltest0147.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0148() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0148/ZZAnnotation.java",
				"package test0148;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0148/ZZClass.java",
				"package test0148;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0148/Test.java",
				"package test0148;\n" +
				"@ZZAnnotation(foo1=ZZ\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0148, Ltest0148.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0148, Ltest0148.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0149() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0149/ZZAnnotation.java",
				"package test0149;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0149/ZZClass.java",
				"package test0149;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0149/Test.java",
				"package test0149;\n" +
				"@ZZAnnotation(foo1= 0 + ZZ\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={S,I,J,F,D,C,B,Ljava.lang.String;}\n" +
				"expectedTypesKeys={S,I,J,F,D,C,B,Ljava/lang/String;}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0149, Ltest0149.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0149, Ltest0149.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0150() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0150/ZZAnnotation.java",
				"package test0150;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int[] foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0150/ZZClass.java",
				"package test0150;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0150/Test.java",
				"package test0150;\n" +
				"@ZZAnnotation(foo1= {ZZ}\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0150, Ltest0150.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0150, Ltest0150.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0151() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0151/ZZAnnotation.java",
				"package test0151;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int[] foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0151/ZZClass.java",
				"package test0151;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0151/Test.java",
				"package test0151;\n" +
				"@ZZAnnotation(foo1= {ZZ\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0151, Ltest0151.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0151, Ltest0151.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0152() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0152/ZZAnnotation.java",
				"package test0152;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0152/ZZClass.java",
				"package test0152;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0152/Test.java",
				"package test0152;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation(foo1=ZZ)\n" +
				"  void bar(){}\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0152, Ltest0152.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0152, Ltest0152.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0152.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0153() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0153/ZZAnnotation.java",
				"package test0153;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0153/ZZClass.java",
				"package test0153;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0153/Test.java",
				"package test0153;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation(foo1= 0 + ZZ)\n" +
				"  void bar(){}\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={S,I,J,F,D,C,B,Ljava.lang.String;}\n" +
				"expectedTypesKeys={S,I,J,F,D,C,B,Ljava/lang/String;}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0153, Ltest0153.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0153, Ltest0153.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0153.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0154() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0154/ZZAnnotation.java",
				"package test0154;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int[] foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0154/ZZClass.java",
				"package test0154;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0154/Test.java",
				"package test0154;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation(foo1= {ZZ})\n" +
				"  void bar(){}\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0154, Ltest0154.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0154, Ltest0154.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0154.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0155() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0155/ZZAnnotation.java",
				"package test0155;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0155/ZZClass.java",
				"package test0155;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0155/Test.java",
				"package test0155;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation(foo1=ZZ\n" +
				"  void bar(){}\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0155, Ltest0155.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0155, Ltest0155.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0155.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0156() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0156/ZZAnnotation.java",
				"package test0156;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0156/ZZClass.java",
				"package test0156;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0156/Test.java",
				"package test0156;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation(foo1= 0 + ZZ\n" +
				"  void bar(){}\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={S,I,J,F,D,C,B,Ljava.lang.String;}\n" +
				"expectedTypesKeys={S,I,J,F,D,C,B,Ljava/lang/String;}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0156, Ltest0156.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0156, Ltest0156.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0156.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0157() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0157/ZZAnnotation.java",
				"package test0157;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int[] foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0157/ZZClass.java",
				"package test0157;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0157/Test.java",
				"package test0157;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation(foo1= {ZZ}\n" +
				"  void bar(){}\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0157, Ltest0157.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0157, Ltest0157.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0157.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE +  R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0158() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0158/ZZAnnotation.java",
				"package test0158;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int[] foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0158/ZZClass.java",
				"package test0158;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0158/Test.java",
				"package test0158;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation(foo1= {ZZ\n" +
				"  void bar(){}\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0158, Ltest0158.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0158, Ltest0158.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0158.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0159() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0159/ZZAnnotation.java",
				"package test0159;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0159/ZZClass.java",
				"package test0159;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0159/Test.java",
				"package test0159;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation(foo1=ZZ)\n" +
				"  int bar;\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0159, Ltest0159.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0159, Ltest0159.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0159.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0160() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0160/ZZAnnotation.java",
				"package test0160;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0160/ZZClass.java",
				"package test0160;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0160/Test.java",
				"package test0160;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation(foo1= 0 + ZZ)\n" +
				"  int bar;\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={S,I,J,F,D,C,B,Ljava.lang.String;}\n" +
				"expectedTypesKeys={S,I,J,F,D,C,B,Ljava/lang/String;}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0160, Ltest0160.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0160, Ltest0160.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0160.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0161() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0161/ZZAnnotation.java",
				"package test0161;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int[] foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0161/ZZClass.java",
				"package test0161;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0161/Test.java",
				"package test0161;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation(foo1= {ZZ})\n" +
				"  int bar;\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0161, Ltest0161.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0161, Ltest0161.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED)  + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0161.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE +  R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0162() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0162/ZZAnnotation.java",
				"package test0162;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0162/ZZClass.java",
				"package test0162;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0162/Test.java",
				"package test0162;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation(foo1=ZZ\n" +
				"  int bar;\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0162, Ltest0162.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0162, Ltest0162.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0162.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0163() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0163/ZZAnnotation.java",
				"package test0163;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0163/ZZClass.java",
				"package test0163;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0163/Test.java",
				"package test0163;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation(foo1= 0 + ZZ\n" +
				"  int bar;\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={S,I,J,F,D,C,B,Ljava.lang.String;}\n" +
				"expectedTypesKeys={S,I,J,F,D,C,B,Ljava/lang/String;}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0163, Ltest0163.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0163, Ltest0163.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0163.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0164() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0164/ZZAnnotation.java",
				"package test0164;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int[] foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0164/ZZClass.java",
				"package test0164;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0164/Test.java",
				"package test0164;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation(foo1= {ZZ}\n" +
				"  int bar;\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0164, Ltest0164.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0164, Ltest0164.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0164.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE +  R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0165() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0165/ZZAnnotation.java",
				"package test0165;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int[] foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0165/ZZClass.java",
				"package test0165;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0165/Test.java",
				"package test0165;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation(foo1= {ZZ\n" +
				"  int bar;\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0165, Ltest0165.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0165, Ltest0165.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0165.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0166() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0166/ZZAnnotation.java",
				"package test0166;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0166/ZZClass.java",
				"package test0166;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0166/Test.java",
				"package test0166;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  void baz() {\n" +
				"    @ZZAnnotation(foo1=ZZ)\n" +
				"    int bar;\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0166, Ltest0166.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0166, Ltest0166.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0166.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0167() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0167/ZZAnnotation.java",
				"package test0167;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0167/ZZClass.java",
				"package test0167;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0167/Test.java",
				"package test0167;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  void baz() {\n" +
				"    @ZZAnnotation(foo1= 0 + ZZ)\n" +
				"    int bar;\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={S,I,J,F,D,C,B,Ljava.lang.String;}\n" +
				"expectedTypesKeys={S,I,J,F,D,C,B,Ljava/lang/String;}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0167, Ltest0167.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0167, Ltest0167.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0167.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0168() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0168/ZZAnnotation.java",
				"package test0168;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int[] foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0168/ZZClass.java",
				"package test0168;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0168/Test.java",
				"package test0168;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  void baz() {\n" +
				"    @ZZAnnotation(foo1= {ZZ})\n" +
				"    int bar;\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0168, Ltest0168.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0168, Ltest0168.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0168.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0169() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0169/ZZAnnotation.java",
				"package test0169;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0169/ZZClass.java",
				"package test0169;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0169/Test.java",
				"package test0169;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  void baz() {\n" +
				"    @ZZAnnotation(foo1=ZZ\n" +
				"    int bar;\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0169, Ltest0169.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0169, Ltest0169.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0169.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0170() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0170/ZZAnnotation.java",
				"package test0170;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0170/ZZClass.java",
				"package test0170;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0170/Test.java",
				"package test0170;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  void baz() {\n" +
				"    @ZZAnnotation(foo1= 0 + ZZ\n" +
				"    int bar;\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={S,I,J,F,D,C,B,Ljava.lang.String;}\n" +
				"expectedTypesKeys={S,I,J,F,D,C,B,Ljava/lang/String;}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0170, Ltest0170.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0170, Ltest0170.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0170.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0171() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0171/ZZAnnotation.java",
				"package test0171;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int[] foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0171/ZZClass.java",
				"package test0171;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0171/Test.java",
				"package test0171;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  void baz() {\n" +
				"    @ZZAnnotation(foo1= {ZZ}\n" +
				"    int bar;\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0171, Ltest0171.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0171, Ltest0171.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0171.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0172() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0172/ZZAnnotation.java",
				"package test0172;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int[] foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0172/ZZClass.java",
				"package test0172;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0172/Test.java",
				"package test0172;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  void baz() {\n" +
				"    @ZZAnnotation(foo1= {ZZ\n" +
				"    int bar;\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0172, Ltest0172.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0172, Ltest0172.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0172.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0173() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0173/ZZAnnotation.java",
				"package test0173;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0173/ZZClass.java",
				"package test0173;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0173/Test.java",
				"package test0173;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  void baz(@ZZAnnotation(foo1=ZZ) int bar) {\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0173, Ltest0173.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0173, Ltest0173.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0173.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0174() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0174/ZZAnnotation.java",
				"package test0174;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0174/ZZClass.java",
				"package test0174;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0174/Test.java",
				"package test0174;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  void baz(@ZZAnnotation(foo1= 0 + ZZ) int bar) {\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={S,I,J,F,D,C,B,Ljava.lang.String;}\n" +
				"expectedTypesKeys={S,I,J,F,D,C,B,Ljava/lang/String;}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0174, Ltest0174.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0174, Ltest0174.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0174.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0175() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0175/ZZAnnotation.java",
				"package test0175;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int[] foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0175/ZZClass.java",
				"package test0175;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0175/Test.java",
				"package test0175;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  void baz(@ZZAnnotation(foo1= {ZZ}) int bar) {\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0175, Ltest0175.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0175, Ltest0175.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0175.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0176() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0176/ZZAnnotation.java",
				"package test0176;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0176/ZZClass.java",
				"package test0176;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0176/Test.java",
				"package test0176;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  void baz(@ZZAnnotation(foo1=ZZ int bar) {\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0176, Ltest0176.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0176, Ltest0176.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0176.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0177() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0177/ZZAnnotation.java",
				"package test0177;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0177/ZZClass.java",
				"package test0177;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0177/Test.java",
				"package test0177;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  void baz(@ZZAnnotation(foo1= 0 + ZZ int bar) {\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={S,I,J,F,D,C,B,Ljava.lang.String;}\n" +
				"expectedTypesKeys={S,I,J,F,D,C,B,Ljava/lang/String;}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0177, Ltest0177.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0177, Ltest0177.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0177.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0178() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0178/ZZAnnotation.java",
				"package test0178;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int[] foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0178/ZZClass.java",
				"package test0178;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0178/Test.java",
				"package test0178;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  void baz(@ZZAnnotation(foo1= {ZZ} int bar) {\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0178, Ltest0178.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0178, Ltest0178.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0178.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE +  R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0179() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0179/ZZAnnotation.java",
				"package test0179;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int[] foo1();\n" +
				"  int foo2();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0179/ZZClass.java",
				"package test0179;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0179/Test.java",
				"package test0179;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  void baz(@ZZAnnotation(foo1= {ZZ int bar) {\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0179, Ltest0179.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0179, Ltest0179.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0179.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0180() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0180/ZZAnnotation.java",
				"package test0180;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int value();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0180/ZZClass.java",
				"package test0180;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0180/Test.java",
				"package test0180;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation(ZZ)\n" +
				"  void bar() {\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0180, Ltest0180.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0180, Ltest0180.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0180.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0181() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0181/ZZAnnotation.java",
				"package test0181;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int value();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0181/ZZClass.java",
				"package test0181;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0181/Test.java",
				"package test0181;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation(0 + ZZ)\n" +
				"  void bar() {\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={S,I,J,F,D,C,B,Ljava.lang.String;}\n" +
				"expectedTypesKeys={S,I,J,F,D,C,B,Ljava/lang/String;}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0181, Ltest0181.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0181, Ltest0181.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0181.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0182() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0182/ZZAnnotation.java",
				"package test0182;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int[] value();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0182/ZZClass.java",
				"package test0182;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0182/Test.java",
				"package test0182;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation({ZZ})\n" +
				"  void bar() {\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0182, Ltest0182.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0182, Ltest0182.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0182.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0183() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0183/ZZAnnotation.java",
				"package test0183;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int value();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0183/ZZClass.java",
				"package test0183;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0183/Test.java",
				"package test0183;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation(ZZ\n" +
				"  void bar() {\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0183, Ltest0183.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0183, Ltest0183.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0183.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0184() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0184/ZZAnnotation.java",
				"package test0184;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int value();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0184/ZZClass.java",
				"package test0184;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0184/Test.java",
				"package test0184;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation(0 + ZZ\n" +
				"  void bar() {\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures=null\n" +
				"expectedTypesKeys=null",
				result.context);

		assertResults(
				"zzint[FIELD_REF]{zzint, Ltest0184.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0184, Ltest0184.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0184, Ltest0184.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0185() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0185/ZZAnnotation.java",
				"package test0185;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int[] value();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0185/ZZClass.java",
				"package test0185;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0185/Test.java",
				"package test0185;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation({ZZ}\n" +
				"  void bar() {\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0185, Ltest0185.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0185, Ltest0185.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0185.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// complete annotation attribute value
public void test0186() throws JavaModelException {
	ICompilationUnit anAnnotation = null;
	ICompilationUnit aClass = null;
	try {
		anAnnotation = getWorkingCopy(
				"/Completion/src3/test0186/ZZAnnotation.java",
				"package test0186;\n" +
				"public @interface ZZAnnotation {\n" +
				"  int[] value();\n" +
				"}");

		aClass = getWorkingCopy(
				"/Completion/src3/test0186/ZZClass.java",
				"package test0186;\n" +
				"public class ZZClass {\n" +
				"}");

		CompletionResult result = complete(
				"/Completion/src3/test0186/Test.java",
				"package test0186;\n" +
				"public class Test {\n" +
				"  public static final int zzint = 0;\n" +
				"  @ZZAnnotation({ZZ\n" +
				"  void bar() {\n" +
				"  }\n" +
				"}",
				"ZZ");

		assertResults(
				"expectedTypesSignatures={I}\n" +
				"expectedTypesKeys={I}",
				result.context);

		assertResults(
				"ZZAnnotation[TYPE_REF]{ZZAnnotation, test0186, Ltest0186.ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"ZZClass[TYPE_REF]{ZZClass, test0186, Ltest0186.ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"zzint[FIELD_REF]{zzint, Ltest0186.Test;, I, zzint, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(anAnnotation != null) {
			anAnnotation.discardWorkingCopy();
		}
		if(aClass != null) {
			aClass.discardWorkingCopy();
		}
	}
}
// completion test with capture
public void test0187() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0187/Test.java",
            "package test0187;\n" +
            "public class Test<U> {\n" +
            "  void bar(ZZClass1<? extends U> var) {\n" +
            "    var.zzz\n" +
            "  }\n" +
            "}\n" +
            "abstract class ZZClass1<V> {\n" +
            "  ZZClass2<V>[] zzz1;\n"+
            "  abstract ZZClass2<V>[] zzz2();\n" +
            "}\n" +
            "abstract class ZZClass2<T> {\n" +
            "}",
            "var.zzz");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "zzz1[FIELD_REF]{zzz1, Ltest0187.ZZClass1<Ljava.lang.Object;>;, [Ltest0187.ZZClass2<Ljava.lang.Object;>;, zzz1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}\n" +
            "zzz2[METHOD_REF]{zzz2(), Ltest0187.ZZClass1<Ljava.lang.Object;>;, ()[Ltest0187.ZZClass2<Ljava.lang.Object;>;, zzz2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_NON_STATIC + R_NON_RESTRICTED) + "}",
            result.proposals);
}
// completion test with capture
public void test0188() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0188/Test.java",
            "package test0188;\n" +
            "public class Test<U> {\n" +
            "  ZZClass1<? extends U> var1;\n" +
            "  void bar(ZZClass1<? extends U> var2) {\n" +
            "    var\n" +
            "  }\n" +
            "}\n" +
            "abstract class ZZClass1<V> {\n" +
            "  ZZClass2<V>[] zzz1;\n"+
            "  abstract ZZClass2<V>[] zzz2();\n" +
            "}\n" +
            "abstract class ZZClass2<T> {\n" +
            "}",
            "var");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "var1[FIELD_REF]{var1, Ltest0188.Test<TU;>;, Ltest0188.ZZClass1<+TU;>;, var1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED+ R_NON_RESTRICTED) + "}\n" +
            "var2[LOCAL_VARIABLE_REF]{var2, null, Ltest0188.ZZClass1<+TU;>;, var2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
            result.proposals);
}
// completion test with capture
public void test0189() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0189/Test.java",
            "package test0189;\n" +
            "public class Test<U> {\n" +
            "  void bar(ZZClass3 var) {\n" +
            "    var.zzz\n" +
            "  }\n" +
            "}\n" +
            "abstract class ZZClass2<T> {\n" +
            "}\n" +
            "class ZZClass3 {\n" +
             "  ZZClass2<? extends Object> zzz1;\n"+
            "}",
            "var.zzz");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "zzz1[FIELD_REF]{zzz1, Ltest0189.ZZClass3;, Ltest0189.ZZClass2<+Ljava.lang.Object;>;, zzz1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_NON_STATIC + R_NON_RESTRICTED) + "}",
            result.proposals);
}
// completion test with capture
public void test0190() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0190/Test.java",
            "package test0190;\n" +
            "public class Test<U> {\n" +
            "  ZZClass1<? extends U> var1\n" +
            "  void bar(ZZClass3<Object> var2) {\n" +
            "    var2.toto().zzz\n" +
            "  }\n" +
            "}\n" +
            "abstract class ZZClass1<V> {\n" +
            "  ZZClass2<V>[] zzz1;\n"+
            "  abstract ZZClass2<V>[] zzz2();\n" +
            "}\n" +
            "abstract class ZZClass2<T> {\n" +
            "}\n" +
            "abstract class ZZClass3<T> {\n" +
            "  ZZClass1<? extends T> toto() {\n" +
            "    return null;\n" +
            "  }\n" +
            "}",
            "toto().zzz");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "zzz1[FIELD_REF]{zzz1, Ltest0190.ZZClass1<Ljava.lang.Object;>;, [Ltest0190.ZZClass2<Ljava.lang.Object;>;, zzz1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC+ R_NON_RESTRICTED) + "}\n" +
            "zzz2[METHOD_REF]{zzz2(), Ltest0190.ZZClass1<Ljava.lang.Object;>;, ()[Ltest0190.ZZClass2<Ljava.lang.Object;>;, zzz2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_NON_STATIC + R_NON_RESTRICTED) + "}",
            result.proposals);
}
// completion test with capture
public void test0191() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0191/Test.java",
            "package test0191;\n" +
            "public class Test<U> {\n" +
            "  ZZClass1<? extends U> var1;\n" +
            "  void bar(ZZClass1<? extends U> zzzvar, ZZClass1<? extends U> var2) {\n" +
            "    zzzvar = var\n" +
            "  }\n" +
            "}\n" +
            "abstract class ZZClass1<V> {\n" +
            "  ZZClass2<V>[] zzz1;\n"+
            "  abstract ZZClass2<V>[] zzz2();\n" +
            "}\n" +
            "abstract class ZZClass2<T> {\n" +
            "}",
            "var");

    assertResults(
            "expectedTypesSignatures={Ltest0191.ZZClass1<+TU;>;}\n" +
    		"expectedTypesKeys={Ltest0191/Test~ZZClass1<Ltest0191/Test~ZZClass1;{0}+Ltest0191/Test;:TU;>;}",
            result.context);

    assertResults(
            "var1[FIELD_REF]{var1, Ltest0191.Test<TU;>;, Ltest0191.ZZClass1<+TU;>;, var1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED) + "}\n" +
            "var2[LOCAL_VARIABLE_REF]{var2, null, Ltest0191.ZZClass1<+TU;>;, var2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED) + "}",
            result.proposals);
}
public void test0192() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0192/Test.java",
            "package test0192;\n" +
            "class ZZClass1<X,Y> {\n" +
            "}\n" +
            "public class Test {\n" +
            "  ZZClass1<\n" +
            "}",
            "ZZClass1<");

    assertResults(
            "expectedTypesSignatures={Ljava.lang.Object;}\n" +
            "expectedTypesKeys={Ljava/lang/Object;}",
            result.context);

    assertResults(
            "ZZClass1<X,Y>[TYPE_REF]{, test0192, Ltest0192.ZZClass1<TX;TY;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_EXACT_NAME+ R_UNQUALIFIED + + R_NON_RESTRICTED) + "}",
            result.proposals);
}
public void test0193() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0193/Test.java",
            "package test0193;\n" +
            "class ZZClass1<X,Y> {\n" +
            "}\n" +
            "public class Test {\n" +
            "  void foo(){\n" +
            "    ZZClass1<\n" +
            "  }\n" +
            "}",
            "ZZClass1<");

    assertResults(
            "expectedTypesSignatures={Ljava.lang.Object;}\n" +
            "expectedTypesKeys={Ljava/lang/Object;}",
            result.context);

    assertResults(
            "ZZClass1<X,Y>[TYPE_REF]{, test0193, Ltest0193.ZZClass1<TX;TY;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_EXACT_NAME + R_UNQUALIFIED + + R_NON_RESTRICTED) + "}",
            result.proposals);
}
public void test0194() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0194/Test.java",
            "package test0194;\n" +
            "class ZZClass1<X,Y> {\n" +
            "}\n" +
            "public class Test {\n" +
            "  ZZClass1<Object,\n" +
            "}",
            "ZZClass1<Object,");

    assertResults(
            "expectedTypesSignatures={Ljava.lang.Object;}\n" +
            "expectedTypesKeys={Ljava/lang/Object;}",
            result.context);

    assertResults(
            "ZZClass1<X,Y>[TYPE_REF]{, test0194, Ltest0194.ZZClass1<TX;TY;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_EXACT_NAME+ R_UNQUALIFIED + + R_NON_RESTRICTED) + "}",
            result.proposals);
}
public void test0195() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0195/Test.java",
            "package test0195;\n" +
            "class ZZClass1<X,Y> {\n" +
            "}\n" +
            "public class Test {\n" +
            "  void foo(){\n" +
            "    ZZClass1<Object,\n" +
            "  }\n" +
            "}",
            "ZZClass1<Object,");

    assertResults(
            "expectedTypesSignatures={Ljava.lang.Object;}\n" +
            "expectedTypesKeys={Ljava/lang/Object;}",
            result.context);

    assertResults(
            "ZZClass1<X,Y>[TYPE_REF]{, test0195, Ltest0195.ZZClass1<TX;TY;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_EXACT_NAME + R_UNQUALIFIED + + R_NON_RESTRICTED) + "}",
            result.proposals);
}
public void test0196() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0196/Test.java",
            "package test0196;\n" +
            "class ZZAnnot {\n" +
            "  int foo1();\n" +
            "  int foo2();\n" +
            "}\n" +
            "@ZZAnnot(\n" +
            "public class Test {\n" +
            "}",
            "@ZZAnnot(");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "",
            result.proposals);
}
public void test0196b() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0196/Test.java",
            "package test0196;\n" +
            "@interface ZZAnnot {\n" +
            "  int foo1();\n" +
            "  int foo2();\n" +
            "}\n" +
            "@ZZAnnot(\n" +
            "public class Test {\n" +
            "}",
            "@ZZAnnot(");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
    		"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0196.ZZAnnot;, I, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
            "foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0196.ZZAnnot;, I, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
            result.proposals);
}
public void test0197() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0197/Test.java",
            "package test0197;\n" +
            "class ZZAnnot {\n" +
            "  int foo1();\n" +
            "  int foo2();\n" +
            "}\n" +
            "public class Test {\n" +
            "  @ZZAnnot(\n" +
            "  void foo(){}\n" +
            "}",
            "@ZZAnnot(");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "",
            result.proposals);
}
public void test0197b() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0197/Test.java",
            "package test0197;\n" +
            "@interface ZZAnnot {\n" +
            "  int foo1();\n" +
            "  int foo2();\n" +
            "}\n" +
            "public class Test {\n" +
            "  @ZZAnnot(\n" +
            "  void foo(){}\n" +
            "}",
            "@ZZAnnot(");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
    		"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0197.ZZAnnot;, I, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
            "foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0197.ZZAnnot;, I, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
            result.proposals);
}
public void test0198() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0198/Test.java",
            "package test0198;\n" +
            "class ZZAnnot {\n" +
            "  int foo1();\n" +
            "  int foo2();\n" +
            "}\n" +

            "public class Test {\n" +
            "  @ZZAnnot(\n" +
            "}",
            "@ZZAnnot(");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "",
            result.proposals);
}
public void test0198b() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0198/Test.java",
            "package test0198;\n" +
            "@interface ZZAnnot {\n" +
            "  int foo1();\n" +
            "  int foo2();\n" +
            "}\n" +

            "public class Test {\n" +
            "  @ZZAnnot(\n" +
            "}",
            "@ZZAnnot(");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
    		"foo1[ANNOTATION_ATTRIBUTE_REF]{foo1 = , Ltest0198.ZZAnnot;, I, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
            "foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0198.ZZAnnot;, I, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
            result.proposals);
}
public void test0199() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0199/Test.java",
            "package test0199;\n" +
            "class ZZAnnot {\n" +
            "  int foo1();\n" +
            "  int foo2();\n" +
            "}\n" +
            "@ZZAnnot(foo1=0,\n" +
            "public class Test {\n" +
            "}",
            "@ZZAnnot(foo1=0,");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "",
            result.proposals);
}
public void test0199b() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0199/Test.java",
            "package test0199;\n" +
            "@interface ZZAnnot {\n" +
            "  int foo1();\n" +
            "  int foo2();\n" +
            "}\n" +
            "@ZZAnnot(foo1=0,\n" +
            "public class Test {\n" +
            "}",
            "@ZZAnnot(foo1=0,");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
    		"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0199.ZZAnnot;, I, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
            result.proposals);
}
public void test0200() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0200/Test.java",
            "package test0200;\n" +
            "class ZZAnnot {\n" +
            "  int foo1();\n" +
            "  int foo2();\n" +
            "}\n" +
            "public class Test {\n" +
            "  @ZZAnnot(foo1=0,\n" +
            "  void foo(){}\n" +
            "}",
            "@ZZAnnot(foo1=0,");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "",
            result.proposals);
}
public void test0200b() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0200/Test.java",
            "package test0200;\n" +
            "@interface ZZAnnot {\n" +
            "  int foo1();\n" +
            "  int foo2();\n" +
            "}\n" +
            "public class Test {\n" +
            "  @ZZAnnot(foo1=0,\n" +
            "  void foo(){}\n" +
            "}",
            "@ZZAnnot(foo1=0,");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
    		"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0200.ZZAnnot;, I, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
            result.proposals);
}
public void test0201() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0201/Test.java",
            "package test0201;\n" +
            "class ZZAnnot {\n" +
            "  int foo1();\n" +
            "  int foo2();\n" +
            "}\n" +

            "public class Test {\n" +
            "  @ZZAnnot(foo1=0,\n" +
            "}",
            "@ZZAnnot(foo1=0,");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
            "",
            result.proposals);
}
public void test0201b() throws JavaModelException {
    CompletionResult result = complete(
            "/Completion/src3/test0201/Test.java",
            "package test0201;\n" +
            "@interface ZZAnnot {\n" +
            "  int foo1();\n" +
            "  int foo2();\n" +
            "}\n" +

            "public class Test {\n" +
            "  @ZZAnnot(foo1=0,\n" +
            "}",
            "@ZZAnnot(foo1=0,");

    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

    assertResults(
    		"foo2[ANNOTATION_ATTRIBUTE_REF]{foo2 = , Ltest0201.ZZAnnot;, I, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
            result.proposals);
}
public void test0202() throws JavaModelException {
	ICompilationUnit aType = null;
	try {
		aType = getWorkingCopy(
	            "/Completion/src3/p/ZZType.java",
	            "package p;\n" +
	            "public class ZZType {\n" +
	            "  public class ZZClass {" +
	            "  }" +
	            "  public interface ZZInterface {" +
	            "  }" +
	            "  public enum ZZEnum {" +
	            "  }" +
	            "  public @interface ZZAnnotation {" +
	            "  }" +
	            "}");

	    CompletionResult result = complete(
	            "/Completion/src3/test0202/Test.java",
	            "package test0202;\n" +
	            "public class Test {\n" +
	            "  public void foo() {" +
	            "    ZZ" +
	            "  }" +
	            "}",
            	"ZZ");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

	    assertResults(
	            "ZZType[TYPE_REF]{p.ZZType, p, Lp.ZZType;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"ZZType.ZZAnnotation[TYPE_REF]{p.ZZType.ZZAnnotation, p, Lp.ZZType$ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"ZZType.ZZClass[TYPE_REF]{p.ZZType.ZZClass, p, Lp.ZZType$ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"ZZType.ZZEnum[TYPE_REF]{p.ZZType.ZZEnum, p, Lp.ZZType$ZZEnum;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"ZZType.ZZInterface[TYPE_REF]{p.ZZType.ZZInterface, p, Lp.ZZType$ZZInterface;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
	            result.proposals);
	} finally {
		if(aType != null) {
			aType.discardWorkingCopy();
		}
	}
}
public void test0203() throws JavaModelException {
	ICompilationUnit aType = null;
	try {
		aType = getWorkingCopy(
	            "/Completion/src3/p/ZZType.java",
	            "package p;\n" +
	            "public class ZZType {\n" +
	            "  public class ZZClass {" +
	            "  }" +
	            "  public interface ZZInterface {" +
	            "  }" +
	            "  public enum ZZEnum {" +
	            "  }" +
	            "  public @interface ZZAnnotation {" +
	            "  }" +
	            "}");

	    CompletionResult result = complete(
	            "/Completion/src3/test0203/Test.java",
	            "package test0203;\n" +
	            "public class Test extends ZZ{\n" +
	            "}",
            	"ZZ");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

	    assertResults(
	            "ZZType[TYPE_REF]{p.ZZType, p, Lp.ZZType;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_CLASS + R_NON_RESTRICTED) + "}\n" +
				"ZZType.ZZClass[TYPE_REF]{p.ZZType.ZZClass, p, Lp.ZZType$ZZClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_CLASS + R_NON_RESTRICTED) + "}",
	            result.proposals);
	} finally {
		if(aType != null) {
			aType.discardWorkingCopy();
		}
	}
}
public void test0204() throws JavaModelException {
	ICompilationUnit aType = null;
	try {
		aType = getWorkingCopy(
	            "/Completion/src3/p/ZZType.java",
	            "package p;\n" +
	            "public class ZZType {\n" +
	            "  public class ZZClass {" +
	            "  }" +
	            "  public interface ZZInterface {" +
	            "  }" +
	            "  public enum ZZEnum {" +
	            "  }" +
	            "  public @interface ZZAnnotation {" +
	            "  }" +
	            "}");

	    CompletionResult result = complete(
	            "/Completion/src3/test0204/Test.java",
	            "package test0204;\n" +
	            "public interface Test extends ZZ{\n" +
	            "}",
            	"ZZ");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

	    assertResults(
	            "ZZType.ZZAnnotation[TYPE_REF]{p.ZZType.ZZAnnotation, p, Lp.ZZType$ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_INTERFACE + R_NON_RESTRICTED) + "}\n" +
				"ZZType.ZZInterface[TYPE_REF]{p.ZZType.ZZInterface, p, Lp.ZZType$ZZInterface;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_INTERFACE + R_NON_RESTRICTED) + "}",
	            result.proposals);
	} finally {
		if(aType != null) {
			aType.discardWorkingCopy();
		}
	}
}
public void test0205() throws JavaModelException {
	ICompilationUnit aType = null;
	try {
		aType = getWorkingCopy(
	            "/Completion/src3/p/ZZType.java",
	            "package p;\n" +
	            "public class ZZType {\n" +
	            "  public class ZZClass {" +
	            "  }" +
	            "  public interface ZZInterface {" +
	            "  }" +
	            "  public enum ZZEnum {" +
	            "  }" +
	            "  public @interface ZZAnnotation {" +
	            "  }" +
	            "}");

	    CompletionResult result = complete(
	            "/Completion/src3/test0205/Test.java",
	            "package test0205;\n" +
	            "public class Test implements ZZ {\n" +
	            "}",
            	"ZZ");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

	    assertResults(
				"ZZType.ZZInterface[TYPE_REF]{p.ZZType.ZZInterface, p, Lp.ZZType$ZZInterface;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_INTERFACE + R_NON_RESTRICTED) + "}",
	            result.proposals);
	} finally {
		if(aType != null) {
			aType.discardWorkingCopy();
		}
	}
}
public void test0206() throws JavaModelException {
	ICompilationUnit aType = null;
	try {
		aType = getWorkingCopy(
	            "/Completion/src3/p/ZZType.java",
	            "package p;\n" +
	            "public class ZZType {\n" +
	            "  public class ZZClass {" +
	            "  }" +
	            "  public interface ZZInterface {" +
	            "  }" +
	            "  public enum ZZEnum {" +
	            "  }" +
	            "  public @interface ZZAnnotation {" +
	            "  }" +
	            "}");

	    CompletionResult result = complete(
	            "/Completion/src3/test0206/Test.java",
	            "package test0206;\n" +
	            "@ZZ\n" +
	            "public class Test {\n" +
	            "}",
            	"ZZ");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

	    assertResults(
	            "ZZType.ZZAnnotation[TYPE_REF]{p.ZZType.ZZAnnotation, p, Lp.ZZType$ZZAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
	            result.proposals);
	} finally {
		if(aType != null) {
			aType.discardWorkingCopy();
		}
	}
}
// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=93254
public void test0207() throws JavaModelException {
	ICompilationUnit aType = null;
	try {
		aType = getWorkingCopy(
	            "/Completion/src3/p/Annot.java",
	            "package p;\n" +
	            "public @interface Annot {\n" +
	            "}");

	    CompletionResult result = complete(
	            "/Completion/src3/test0207/Test.java",
	            "package test0206;\n" +
	            "@p.Annot\n",
            	"@p.Annot");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

	    assertResults(
	            "Annot[TYPE_REF]{p.Annot, p, Lp.Annot;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_ANNOTATION + R_QUALIFIED + R_NON_RESTRICTED) + "}",
	            result.proposals);
	} finally {
		if(aType != null) {
			aType.discardWorkingCopy();
		}
	}
}
public void test0208() throws JavaModelException {
	ICompilationUnit aType = null;
	try {
		aType = getWorkingCopy(
	            "/Completion/src3/p/Colors.java",
	            "package p;\n" +
	            "public enum Colors { BLACK, BLUE, WHITE, RED }\n");

	    CompletionResult result = complete(
	            "/Completion/src3/test0208/Test.java",
	            "package test0208;\n" +
	            "public class Test {\n" +
	            "  static final String BLANK = \"    \";\n" +
	            "  void foo(p.Colors color) {\n" +
	            "    switch (color) {\n" +
	            "      case BLUE:\n" +
	            "      case RED:\n" +
	            "        break;\n" +
	            "      case \n" +
	            "    }\n" +
	            "  }\n" +
	            "}",
            	"case ");


	    assertResults(
	            "expectedTypesSignatures={Lp.Colors;}\n" +
	            "expectedTypesKeys={Lp/Colors;}",
	            result.context);

	    assertResults(
	            "BLACK[FIELD_REF]{BLACK, Lp.Colors;, Lp.Colors;, BLACK, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_ENUM + R_ENUM_CONSTANT + R_UNQUALIFIED + R_NON_RESTRICTED + R_RESOLVED) + "}\n" +
				"WHITE[FIELD_REF]{WHITE, Lp.Colors;, Lp.Colors;, WHITE, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_ENUM + R_ENUM_CONSTANT + R_UNQUALIFIED + R_NON_RESTRICTED + R_RESOLVED) + "}",
	            result.proposals);
	} finally {
		if(aType != null) {
			aType.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94303
 */
public void test0209() throws JavaModelException {
	ICompilationUnit importedClass = null;
	try {
		importedClass = getWorkingCopy(
				"/Completion/src3/test0209/p/ImportedClass.java",
				"package test0209.p;\n"+
				"\n"+
				"public class ImportedClass {\n"+
				"	public static class ImportedMember {\n"+
				"	}\n"+
				"}");

		CompletionResult result = complete(
	            "/Completion/src3/test0209/Test.java",
	            "package test0209;\n" +
	            "\n" +
	            "import static Imported\n" +
	            "\n" +
	            "public class Test {\n" +
	            "	\n" +
	            "}",
            	"Imported");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

		assertResults(
				"ImportedClass[TYPE_REF]{test0209.p.ImportedClass., test0209.p, Ltest0209.p.ImportedClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"ImportedClass.ImportedMember[TYPE_REF]{test0209.p.ImportedClass.ImportedMember;, test0209.p, Ltest0209.p.ImportedClass$ImportedMember;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(importedClass != null) {
			importedClass.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94303
 */
public void test0210() throws JavaModelException {
	ICompilationUnit importedClass = null;
	try {
		importedClass = getWorkingCopy(
				"/Completion/src3/test0210/p/ImportedClass.java",
				"package test0210.p;\n"+
				"\n"+
				"public class ImportedClass {\n"+
				"	public class ImportedMember {\n"+
				"	}\n"+
				"}");

		CompletionResult result = complete(
	            "/Completion/src3/test0210/Test.java",
	            "package test0210;\n" +
	            "\n" +
	            "import static test0210.p.ImportedClass.Im\n" +
	            "\n" +
	            "public class Test {\n" +
	            "	\n" +
	            "}",
            	"test0210.p.ImportedClass.Im");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

		assertResults(
				"",
				result.proposals);
	} finally {
		if(importedClass != null) {
			importedClass.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94303
 */
public void test0211() throws JavaModelException {
	ICompilationUnit importedClass = null;
	try {
		importedClass = getWorkingCopy(
				"/Completion/src3/test0211/p/ImportedClass.java",
				"package test0211.p;\n"+
				"\n"+
				"public class ImportedClass {\n"+
				"	public static class ImportedMember {\n"+
				"	}\n"+
				"}");

		CompletionResult result = complete(
	            "/Completion/src3/test0211/Test.java",
	            "package test0211;\n" +
	            "\n" +
	            "import static test0211.p.ImportedClass.Im\n" +
	            "\n" +
	            "public class Test {\n" +
	            "	\n" +
	            "}",
            	"test0211.p.ImportedClass.Im");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

		assertResults(
				"ImportedClass.ImportedMember[TYPE_REF]{ImportedMember;, test0211.p, Ltest0211.p.ImportedClass$ImportedMember;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(importedClass != null) {
			importedClass.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94303
 */
public void test0212() throws JavaModelException {
	ICompilationUnit importedClass = null;
	try {
		importedClass = getWorkingCopy(
				"/Completion/src3/test0212/p/ImportedClass.java",
				"package test0212.p;\n"+
				"\n"+
				"public class ImportedClass {\n"+
				"	public static class ImportedMember {\n"+
				"	}\n"+
				"}");

		CompletionResult result = complete(
	            "/Completion/src3/test0212/Test.java",
	            "package test0212;\n" +
	            "\n" +
	            "import test0212.p.Im\n" +
	            "\n" +
	            "public class Test {\n" +
	            "	\n" +
	            "}",
            	"test0212.p.Im");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

		assertResults(
				"ImportedClass[TYPE_REF]{ImportedClass;, test0212.p, Ltest0212.p.ImportedClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(importedClass != null) {
			importedClass.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=94303
 */
public void test0213() throws JavaModelException {
	ICompilationUnit importedClass = null;
	try {
		importedClass = getWorkingCopy(
				"/Completion/src3/test0213/p/ImportedClass.java",
				"package test0213.p;\n"+
				"\n"+
				"public class ImportedClass {\n"+
				"}");

		CompletionResult result = complete(
	            "/Completion/src3/test0213/Test.java",
	            "package test0213;\n" +
	            "\n" +
	            "import test0213.p.Im\n" +
	            "\n" +
	            "public class Test {\n" +
	            "	\n" +
	            "}",
            	"test0213.p.Im");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

		assertResults(
				"ImportedClass[TYPE_REF]{ImportedClass;, test0213.p, Ltest0213.p.ImportedClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(importedClass != null) {
			importedClass.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=93249
 */
public void test0214() throws JavaModelException {
	ICompilationUnit paramClass1 = null;
	ICompilationUnit paramClass2 = null;
	ICompilationUnit superClass = null;
	try {
		paramClass1 = getWorkingCopy(
				"/Completion/src3/test0214/AClass1.java",
				"package test0214;\n"+
				"\n"+
				"public class AClass1 {\n"+
				"}");

		paramClass2 = getWorkingCopy(
				"/Completion/src3/test0214/AClass2.java",
				"package test0214;\n"+
				"\n"+
				"public class AClass2 {\n"+
				"}");

		superClass = getWorkingCopy(
				"/Completion/src3/test0214/SuperClass.java",
				"package test0214;\n"+
				"\n"+
				"public class SuperClass<T> {\n"+
				"  public <M extends AClass1> void foo(M p1) {\n" +
				"  }\n" +
				"  public <M extends AClass2> void foo(M p2) {\n" +
				"  }\n" +
				"}");

		CompletionResult result = complete(
	            "/Completion/src3/test0214/Test.java",
	            "package test0214;\n" +
	            "\n" +
	            "public class Test<Z> extends SuperClass<Z>{\n" +
	            "	foo\n" +
	            "}",
            	"foo");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

		assertResults(
				"foo[POTENTIAL_METHOD_DECLARATION]{foo, Ltest0214.Test<TZ;>;, ()V, foo, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
				"foo[METHOD_DECLARATION]{public <M extends AClass1> void foo(M p1), Ltest0214.SuperClass<TZ;>;, <M:Ltest0214.AClass1;>(TM;)V, foo, (p1), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_METHOD_OVERIDE + R_NON_RESTRICTED) + "}\n" +
				"foo[METHOD_DECLARATION]{public <M extends AClass2> void foo(M p2), Ltest0214.SuperClass<TZ;>;, <M:Ltest0214.AClass2;>(TM;)V, foo, (p2), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_METHOD_OVERIDE + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(paramClass1 != null) {
			paramClass1.discardWorkingCopy();
		}
		if(paramClass2 != null) {
			paramClass2.discardWorkingCopy();
		}
		if(superClass != null) {
			superClass.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=93249
 */
public void test0215() throws JavaModelException {
	ICompilationUnit paramClass = null;
	ICompilationUnit superClass = null;
	try {
		paramClass = getWorkingCopy(
				"/Completion/src3/test0215/p/ParamClass.java",
				"package test0215.p;\n"+
				"\n"+
				"public class ParamClass {\n"+
				"  public class MemberParamClass<P2> {\n" +
				"  }\n" +
				"}");

		superClass = getWorkingCopy(
				"/Completion/src3/test0215/SuperClass.java",
				"package test0215;\n"+
				"\n"+
				"public class SuperClass<T> {\n"+
				"  public <M extends SuperClass<T>> SuperClass<?> foo(test0215.p.ParamClass.MemberParamClass<? super T> p1, int p2) throws Exception {\n" +
				"    return null;\n" +
				"  }\n" +
				"}");

		CompletionResult result = complete(
	            "/Completion/src3/test0215/Test.java",
	            "package test0215;\n" +
	            "\n" +
	            "public class Test<Z> extends SuperClass<Z>{\n" +
	            "	foo\n" +
	            "}",
            	"foo");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

		assertResults(
				"foo[POTENTIAL_METHOD_DECLARATION]{foo, Ltest0215.Test<TZ;>;, ()V, foo, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
				"foo[METHOD_DECLARATION]{public <M extends test0215.SuperClass<Z>> test0215.SuperClass<?> foo(test0215.p.ParamClass.MemberParamClass<? super Z> p1, int p2) throws Exception, Ltest0215.SuperClass<TZ;>;, <M:Ltest0215.SuperClass<TZ;>;>(Ltest0215.p.ParamClass$MemberParamClass<-TZ;>;I)Ltest0215.SuperClass<*>;, foo, (p1, p2), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_METHOD_OVERIDE + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(paramClass != null) {
			paramClass.discardWorkingCopy();
		}
		if(superClass != null) {
			superClass.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=93249
 */
public void test0216() throws JavaModelException {
	ICompilationUnit paramClass1 = null;
	ICompilationUnit paramClass2 = null;
	ICompilationUnit superClass = null;
	try {
		paramClass1 = getWorkingCopy(
				"/Completion/src3/test0216/p/ParamClass.java",
				"package test0216.p;\n"+
				"\n"+
				"public class ParamClass {\n"+
				"}");

		paramClass2 = getWorkingCopy(
				"/Completion/src3/test0216/q/ParamClass.java",
				"package test0216.q;\n"+
				"\n"+
				"public class ParamClass {\n"+
				"}");

		superClass = getWorkingCopy(
				"/Completion/src3/test0216/SuperClass.java",
				"package test0216;\n"+
				"\n"+
				"public class SuperClass<T> {\n"+
				"  public void foo(test0216.p.ParamClass p1) {\n" +
				"  }\n" +
				"  public void foo(test0216.q.ParamClass p2) {\n" +
				"  }\n" +
				"}");

		CompletionResult result = complete(
	            "/Completion/src3/test0216/Test.java",
	            "package test0216;\n" +
	            "\n" +
	            "public class Test<Z> extends SuperClass<Z>{\n" +
	            "	foo\n" +
	            "}",
            	"foo");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

		assertResults(
				"foo[POTENTIAL_METHOD_DECLARATION]{foo, Ltest0216.Test<TZ;>;, ()V, foo, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
				"foo[METHOD_DECLARATION]{public void foo(test0216.p.ParamClass p1), Ltest0216.SuperClass<TZ;>;, (Ltest0216.p.ParamClass;)V, foo, (p1), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_METHOD_OVERIDE + R_NON_RESTRICTED) + "}\n" +
				"foo[METHOD_DECLARATION]{public void foo(test0216.q.ParamClass p2), Ltest0216.SuperClass<TZ;>;, (Ltest0216.q.ParamClass;)V, foo, (p2), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_METHOD_OVERIDE + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(paramClass1 != null) {
			paramClass1.discardWorkingCopy();
		}
		if(paramClass2 != null) {
			paramClass2.discardWorkingCopy();
		}
		if(superClass != null) {
			superClass.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=93119
 */
public void test0217() throws JavaModelException {
	ICompilationUnit paramClass1 = null;
	try {
		paramClass1 = getWorkingCopy(
				"/Completion/src3/test0217/AType.java",
				"package test0217;\n"+
				"\n"+
				"public class AType<T> {\n"+
				"}");



		CompletionResult result = complete(
	            "/Completion/src3/test0217/Test.java",
	            "package test0217;\n" +
	            "\n" +
	            "public class Test {\n" +
	            "	AType<? ext\n" +
	            "}",
            	"ext");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

		assertResults(
				"extends[KEYWORD]{extends, null, null, extends, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(paramClass1 != null) {
			paramClass1.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=93119
 */
public void test0218() throws JavaModelException {
	ICompilationUnit paramClass1 = null;
	try {
		paramClass1 = getWorkingCopy(
				"/Completion/src3/test0218/AType.java",
				"package test0218;\n"+
				"\n"+
				"public class AType<T> {\n"+
				"}");



		CompletionResult result = complete(
	            "/Completion/src3/test0218/Test.java",
	            "package test0218;\n" +
	            "\n" +
	            "public class Test {\n" +
	            "	AType<? sup\n" +
	            "}",
            	"sup");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

		assertResults(
				"super[KEYWORD]{super, null, null, super, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(paramClass1 != null) {
			paramClass1.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=93119
 */
public void test0219() throws JavaModelException {
	ICompilationUnit paramClass1 = null;
	try {
		paramClass1 = getWorkingCopy(
				"/Completion/src3/test0219/AType.java",
				"package test0219;\n"+
				"\n"+
				"public class AType<T> {\n"+
				"}");



		CompletionResult result = complete(
	            "/Completion/src3/test0219/Test.java",
	            "package test0219;\n" +
	            "\n" +
	            "public class Test {\n" +
	            "	void foo() {\n" +
	            "	  AType<? ext\n" +
	            "	}\n" +
	            "}",
            	"ext");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

		assertResults(
				"extends[KEYWORD]{extends, null, null, extends, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(paramClass1 != null) {
			paramClass1.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=93119
 */
public void test0220() throws JavaModelException {
	ICompilationUnit paramClass1 = null;
	try {
		paramClass1 = getWorkingCopy(
				"/Completion/src3/test0220/AType.java",
				"package test0220;\n"+
				"\n"+
				"public class AType<T> {\n"+
				"}");



		CompletionResult result = complete(
	            "/Completion/src3/test0220/Test.java",
	            "package test0220;\n" +
	            "\n" +
	            "public class Test {\n" +
	            "	void foo() {\n" +
	            "	  AType<? sup\n" +
	            "	}\n" +
	            "}",
            	"sup");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

		assertResults(
				"super[KEYWORD]{super, null, null, super, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(paramClass1 != null) {
			paramClass1.discardWorkingCopy();
		}
	}
}
public void test0221() throws JavaModelException {
	ICompilationUnit paramClass1 = null;
	try {
		paramClass1 = getWorkingCopy(
				"/Completion/src3/test0221/AType.java",
				"package test0221;\n"+
				"\n"+
				"public class AType<T> {\n"+
				"}");



		CompletionResult result = complete(
	            "/Completion/src3/test0221/Test.java",
	            "package test0221;\n" +
	            "\n" +
	            "public class Test {\n" +
	            "  AType<? extends ATy\n"+
	            "}",
            	"ATy");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

		assertResults(
				"AType[TYPE_REF]{AType, test0221, Ltest0221.AType;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(paramClass1 != null) {
			paramClass1.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=96918
 */
public void test0222() throws JavaModelException {
	ICompilationUnit paramClass1 = null;
	try {
		paramClass1 = getWorkingCopy(
				"/Completion/src3/test0222/AType.java",
				"package test0222;\n"+
				"\n"+
				"public class AType<T> {\n"+
				"}");



		CompletionResult result = complete(
	            "/Completion/src3/test0222/Test.java",
	            "package test0222;\n" +
	            "\n" +
	            "public class Test {\n" +
	            "	void foo() {\n" +
	            "	  AType<? \n" +
	            "	}\n" +
	            "}",
            	"? ");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

		assertResults(
				"extends[KEYWORD]{extends, null, null, extends, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
				"super[KEYWORD]{super, null, null, super, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(paramClass1 != null) {
			paramClass1.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=97307
 */
public void test0223() throws JavaModelException {
	ICompilationUnit paramClass1 = null;
	try {
		paramClass1 = getWorkingCopy(
				"/Completion/src3/test0223/AType.java",
				"package test0223;\n"+
				"\n"+
				"public class AType {\n"+
				"  public static final int VAR = 0;\n"+
				"}");



		CompletionResult result = complete(
	            "/Completion/src3/test0223/Test.java",
	            "package test0223;\n" +
	            "\n" +
	            "import static test0223.AType.va\n" +
	            "\n" +
	            "public class Test {\n" +
	            "}",
	            true, // show positions
            	"AType.va");


	    assertResults(
	            "expectedTypesSignatures=null\n" +
	            "expectedTypesKeys=null",
	            result.context);

	    int end = result.cursorLocation;
		int start = end - "va".length();

		assertResults(
				"VAR[FIELD_REF]{VAR;, Ltest0223.AType;, I, VAR, null, ["+start+", "+end+"], " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}",
				result.proposals);
	} finally {
		if(paramClass1 != null) {
			paramClass1.discardWorkingCopy();
		}
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=85384
 */
public void test0224() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0224/Test.java",
            "package test0224;\n" +
            "\n" +
            "public class Test<T ext> {\n" +
            "}",
        	"ext");


    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"extends[KEYWORD]{extends, null, null, extends, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			result.proposals);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=85384
 */
public void test0225() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0225/Test.java",
            "package test0225;\n" +
            "\n" +
            "public class Test<T ext\n" +
            "",
        	"ext");


    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"extends[KEYWORD]{extends, null, null, extends, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			result.proposals);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=85384
 */
public void test0226() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0226/Test.java",
            "package test0226;\n" +
            "\n" +
            "public class Test {\n" +
            "  public <T ext> void foo() {}\n" +
            "}",
        	"ext");


    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"extends[KEYWORD]{extends, null, null, extends, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			result.proposals);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=85384
 */
public void test0227() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0227/Test.java",
            "package test0227;\n" +
            "\n" +
            "public class Test {\n" +
            "  public <T ext\n" +
            "}",
        	"ext");


    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"extends[KEYWORD]{extends, null, null, extends, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			result.proposals);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=97801
 */
public void test0228() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0228/Test.java",
            "package test0228;\n" +
            "\n" +
            "public class Test {\n" +
            "	void foo() {\n" +
            "	  Test.clas \n" +
            "	}\n" +
            "}",
        	".clas");


    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"class[FIELD_REF]{class, null, Ljava.lang.Class<Ltest0228.Test;>;, class, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_INHERITED + R_NON_RESTRICTED) + "}",
			result.proposals);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=97801
 */
public void test0229() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0229/Test.java",
            "package test0229;\n" +
            "\n" +
            "public class Test<T> {\n" +
            "	void foo() {\n" +
            "	  Test.clas \n" +
            "	}\n" +
            "}",
        	".clas");


    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"class[FIELD_REF]{class, null, Ljava.lang.Class<Ltest0229.Test;>;, class, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_INHERITED + R_NON_RESTRICTED) + "}",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=96944
public void test0230() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0230/Test.java",
            "package test0230;\n" +
            "\n" +
            "public class Test<ZT> {\n" +
            "  void foo() {\n"+
            "    new ZT\n"+
            "  }\n"+
            "}",
        	"ZT");


    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=96944
public void test0231() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0231/Test.java",
            "package test0231;\n" +
            "\n" +
            "public class Test<ZT> {\n" +
            "  void foo() {\n"+
            "    ZT var = new ZT\n"+
            "  }\n"+
            "}",
        	"ZT");


    assertResults(
            "expectedTypesSignatures={TZT;}\n" +
            "expectedTypesKeys={Ltest0231/Test;:TZT;}",
            result.context);

	assertResults(
			"",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=96944
public void test0232() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0232/Test.java",
            "package test0232;\n" +
            "\n" +
            "public class Test<ZT> {\n" +
            "  void foo() {\n"+
            "    ZT var = new \n"+
            "  }\n"+
            "}",
        	"new ");


    assertResults(
            "expectedTypesSignatures={TZT;}\n" +
            "expectedTypesKeys={Ltest0232/Test;:TZT;}",
            result.context);

    if(CompletionEngine.NO_TYPE_COMPLETION_ON_EMPTY_TOKEN) {
		assertResults(
				"",
				result.proposals);
    } else {
    	assertResults(
				"Test<ZT>[TYPE_REF]{Test, test0232, Ltest0232.Test<TZT;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_PACKAGE_EXPECTED_TYPE) + "}",
				result.proposals);
    }
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=82560
public void test0233() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0233/Test0233Z.java",
            "package test0233;\n" +
            "\n" +
            "public class Test0233Z<ZT> {\n" +
            "  void bar() {\n"+
            "    zzz.<String>foo(new Test0233Z());\n"+
            "  }\n"+
            "  <T> void foo(Object o) {\n"+
            "  }\n"+
            "}",
        	"Test0233Z");


    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"Test0233Z<ZT>[TYPE_REF]{Test0233Z, test0233, Ltest0233.Test0233Z<TZT;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME+ R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97860
public void test0234() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0234/Test.java",
            "package test0234;\n" +
            "\n" +
            "public class Test<ZT> {\n" +
            "  void foo() {\n"+
            "    ZT.c\n"+
            "  }\n"+
            "}",
        	"ZT.c");


    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97860
public void test0235() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0235/Test.java",
            "package test0235;\n" +
            "\n" +
            "public class Test<ZT> {\n" +
            "  void foo() throws ZT.c {\n"+
            "  }\n"+
            "}",
        	"ZT.c");


    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94641
public void test0236() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0236/Test.java",
            "package test0236;\n" +
            "\n" +
            "public class Test<ZT> {\n" +
            "  void foo() {\n"+
            "    new Test<String>();\n"+
            "  }\n"+
            "}",
        	">(");


    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"Test[METHOD_REF<CONSTRUCTOR>]{, Ltest0236.Test<Ljava.lang.String;>;, ()V, Test, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
			"Test<java.lang.String>[ANONYMOUS_CLASS_DECLARATION]{, Ltest0236.Test<Ljava.lang.String;>;, ()V, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94907
public void test0237() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0237/Test.java",
            "package test0237;\n" +
            "\n" +
            "public class Test<ZT> ext {\n" +
            "}",
        	"ext");


    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"extends[KEYWORD]{extends, null, null, extends, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94907
public void test0238() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0238/Test.java",
            "package test0238;\n" +
            "\n" +
            "public class Test<ZT> imp {\n" +
            "}",
        	"imp");


    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"implements[KEYWORD]{implements, null, null, implements, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94907
public void test0239() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0239/Test.java",
            "package test0239;\n" +
            "\n" +
            "public class Test<ZT> extends Object ext {\n" +
            "}",
        	"ext");


    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94907
public void test0240() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0204/Test.java",
            "package test0240;\n" +
            "\n" +
            "public class Test<ZT> extends Object imp {\n" +
            "}",
        	"imp");


    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"implements[KEYWORD]{implements, null, null, implements, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94907
public void test0241() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0241/Test.java",
            "package test0241;\n" +
            "\n" +
            "public interface Test<ZT> ext {\n" +
            "}",
        	"ext");


    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"extends[KEYWORD]{extends, null, null, extends, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=94907
public void test0242() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/test0242/Test.java",
            "package test0242;\n" +
            "\n" +
            "public interface Test<ZT> imp {\n" +
            "}",
        	"imp");


    assertResults(
            "expectedTypesSignatures=null\n" +
            "expectedTypesKeys=null",
            result.context);

	assertResults(
			"",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=99686
	public void test0243() throws JavaModelException {
		CompletionResult result = complete(
			"/Completion/src3/test0243/X.java",
			"package test0243;\n" +
			"public class X {\n" +
			"	void test() {\n" +
			"		foo(new Object() {}).b\n" +
			"	}\n" +
			"	<T> Y<T> foo(T t) {\n" +
			"		return null;\n" +
			"	}\n" +
			"}\n" +
			"class Y<T> {\n" +
			"	T bar() {\n" +
			"		return null;\n" +
			"	}\n" +
			"}",
			"foo(new Object() {}).b");

		assertResults(
			"bar[METHOD_REF]{bar(), Ltest0243.Y<LObject;>;, ()LObject;, bar, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=100009
public void test0244() throws JavaModelException {
		CompletionResult result = complete(
			"/Completion/src3/test0244/X.java",
			"package test0244;\n" +
			"import generics.*;\n" +
			"public class X extends ZAGenericType {\n" +
			"	foo\n" +
			"}",
			"foo");

		assertResults(
			"foo[POTENTIAL_METHOD_DECLARATION]{foo, Ltest0244.X;, ()V, foo, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
			"foo[METHOD_DECLARATION]{public Object foo(Object t), Lgenerics.ZAGenericType;, (Ljava.lang.Object;)Ljava.lang.Object;, foo, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_METHOD_OVERIDE + R_NON_RESTRICTED) + "}\n" +
			"foo[METHOD_DECLARATION]{public Object foo(ZAGenericType var), Lgenerics.ZAGenericType;, (Lgenerics.ZAGenericType;)Ljava.lang.Object;, foo, (var), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_METHOD_OVERIDE + R_NON_RESTRICTED) + "}",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=101456
public void test0245() throws JavaModelException {
    this.wc = getWorkingCopy(
            "/Completion/src/test/SnapshotImpl.java",
            "class SnapshotImpl extends AbstractSnapshot<SnapshotImpl, ProviderImpl> {}");
    getWorkingCopy(
            "/Completion/src/test/Snapshot.java",
            "public interface Snapshot<S extends Snapshot> {}");
    getWorkingCopy(
            "/Completion/src/test/SnapshotProvider.java",
            "interface SnapshotProvider<S extends Snapshot> {}");
    getWorkingCopy(
            "/Completion/src/test/AbstractSnapshot.java",
            "abstract class AbstractSnapshot<S extends Snapshot, P extends SnapshotProvider<S>> implements Snapshot<S> {}");
    getWorkingCopy(
            "/Completion/src/test/ProviderImpl.java",
            "class ProviderImpl implements SnapshotProvider<SnapshotImpl> {}");

    CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
    String str = this.wc.getSource();
    String completeBehind = "ProviderImp";
    int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
    this.wc.codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults("", requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=83005
public void test0246() throws JavaModelException {
		CompletionResult result = complete(
			"/Completion/src3/test0245/X.java",
			"package test0245;\n" +
			"public @interface X {\n" +
			"	ann\n" +
			"}",
			"ann");

		assertResults(
			"Annotation[TYPE_REF]{java.lang.annotation.Annotation, java.lang.annotation, Ljava.lang.annotation.Annotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102284
public void test0247() throws JavaModelException {
		CompletionResult result = complete(
			"/Completion/src3/test0245/X.java",
			"package test0245;\n" +
			"public class X {\n" +
			"  void test() {\n" +
			"    class Type<S, T> {\n" +
			"      Type<String, String> t= new Type<String, String> ()\n" +
			"    }\n" +
			"  }\n" +
			"}",
			"Type<String, String> (");

		assertResults(
			"Type[METHOD_REF<CONSTRUCTOR>]{, LType<Ljava.lang.String;Ljava.lang.String;>;, ()V, Type, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
			"Type<java.lang.String,java.lang.String>[ANONYMOUS_CLASS_DECLARATION]{, LType<Ljava.lang.String;Ljava.lang.String;>;, ()V, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}",
			result.proposals);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102572
public void test0248() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_CAMEL_CASE_MATCH, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/camelcase/Test.java",
			"package camelcase;"+
			"import static camelcase.ImportedType.*;"+
			"public class Test {\n"+
			"  void foo() {\n"+
			"    oTT\n"+
			"  }\n"+
			"}");

		this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/camelcase/ImportedType.java",
			"package camelcase;"+
			"public class ImportedType {\n"+
			"  public static void oneTwoThree(){}\n"+
			"  public static void oTTMethod(){}\n"+
			"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "oTT";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"oneTwoThree[METHOD_REF]{oneTwoThree(), Lcamelcase.ImportedType;, ()V, oneTwoThree, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CAMEL_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"oTTMethod[METHOD_REF]{oTTMethod(), Lcamelcase.ImportedType;, ()V, oTTMethod, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102572
public void test0249() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_CAMEL_CASE_MATCH, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/camelcase/Test.java",
			"package camelcase;"+
			"import static camelcase.ImportedType.*;"+
			"public class Test {\n"+
			"  void foo() {\n"+
			"    oTT\n"+
			"  }\n"+
			"}");

		this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/camelcase/ImportedType.java",
			"package camelcase;"+
			"public class ImportedType {\n"+
			"  public static int oneTwoThree;\n"+
			"  public static int oTTField;\n"+
			"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "oTT";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"oneTwoThree[FIELD_REF]{oneTwoThree, Lcamelcase.ImportedType;, I, oneTwoThree, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CAMEL_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"oTTField[FIELD_REF]{oTTField, Lcamelcase.ImportedType;, I, oTTField, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102572
public void test0250() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_CAMEL_CASE_MATCH, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/camelcase/Test.java",
			"package camelcase;"+
			"import static camelcase.ImportedType.oTT;"+
			"public class Test {\n"+
			"}");

		this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/camelcase/ImportedType.java",
			"package camelcase;"+
			"public class ImportedType {\n"+
			"  public static void oneTwoThree(){}\n"+
			"  public static void oTTMethod(){}\n"+
			"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "oTT";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"oneTwoThree[METHOD_NAME_REFERENCE]{oneTwoThree;, Lcamelcase.ImportedType;, ()V, oneTwoThree, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CAMEL_CASE + R_NON_RESTRICTED) + "}\n" +
				"oTTMethod[METHOD_NAME_REFERENCE]{oTTMethod;, Lcamelcase.ImportedType;, ()V, oTTMethod, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102572
public void test0260() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_CAMEL_CASE_MATCH, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/camelcase/Test.java",
			"package camelcase;"+
			"@Annot(oTT)"+
			"public class Test {\n"+
			"}");

		this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/camelcase/Annot.java",
			"package camelcase;"+
			"public @interface Annot {\n"+
			"  String oneTwoThree() default \"\";\n"+
			"  String oTTAttribute() default \"\";\n"+
			"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
		String str = this.workingCopies[0].getSource();
		String completeBehind = "oTT";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		assertResults(
				"oneTwoThree[ANNOTATION_ATTRIBUTE_REF]{oneTwoThree = , Lcamelcase.Annot;, Ljava.lang.String;, oneTwoThree, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CAMEL_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
				"oTTAttribute[ANNOTATION_ATTRIBUTE_REF]{oTTAttribute = , Lcamelcase.Annot;, Ljava.lang.String;, oTTAttribute, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=113945
public void test0261() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test<T extends SuperClass> {\n"+
		"  T foo() {\n"+
		"    foo().zz\n"+
		"  }\n"+
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/SuperClass.java",
		"package test;"+
		"public class SuperClass {\n"+
		"  public int zzfield;\n"+
		"  public void zzmethod(){}\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo().zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"zzfield[FIELD_REF]{zzfield, Ltest.SuperClass;, I, zzfield, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}\n" +
			"zzmethod[METHOD_REF]{zzmethod(), Ltest.SuperClass;, ()V, zzmethod, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=113945
public void test0262() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test<T extends SuperInterface> {\n"+
		"  T foo() {\n"+
		"    foo().zz\n"+
		"  }\n"+
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/SuperInterface.java",
		"package test;"+
		"public interface SuperInterface {\n"+
		"  public static int zzfield;\n"+
		"  public void zzmethod();\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo().zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"zzfield[FIELD_REF]{zzfield, Ltest.SuperInterface;, I, zzfield, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"zzmethod[METHOD_REF]{zzmethod(), Ltest.SuperInterface;, ()V, zzmethod, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=113945
public void test0263() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test<T extends SuperClass & SuperInterface> {\n"+
		"  T foo() {\n"+
		"    foo().zz\n"+
		"  }\n"+
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/SuperClass.java",
		"package test;"+
		"public class SuperClass {\n"+
		"  public int zzfield;\n"+
		"  public void zzmethod();\n"+
		"}");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src/test/SuperInterface.java",
		"package test;"+
		"public interface SuperInterface {\n"+
		"  public static int zzfield2;\n"+
		"  public void zzmethod2();\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo().zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"zzfield2[FIELD_REF]{zzfield2, Ltest.SuperInterface;, I, zzfield2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"zzfield[FIELD_REF]{zzfield, Ltest.SuperClass;, I, zzfield, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}\n" +
			"zzmethod[METHOD_REF]{zzmethod(), Ltest.SuperClass;, ()V, zzmethod, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}\n" +
			"zzmethod2[METHOD_REF]{zzmethod2(), Ltest.SuperInterface;, ()V, zzmethod2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=120522
public void test0264() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"@MyAnnot(MyEnum\n"+
		"public class Test {\n"+
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/MyEnum.java",
		"package test;"+
		"public enum MyEnum {\n"+
		"  AAA\n"+
		"}");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src/test/MyAnnot.java",
		"package test;"+
		"public @interface MyAnnot {\n"+
		"  MyEnum[] value();\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.setIgnored(CompletionProposal.ANNOTATION_ATTRIBUTE_REF, true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "MyEnum";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"MyEnum[TYPE_REF]{MyEnum, test, Ltest.MyEnum;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_ENUM + R_EXACT_EXPECTED_TYPE + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=127323
public void test0265() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/enumbug/EnumBug.java",
		"package enumbug;\n"+
		"public class EnumBug {\n"+
		"  public static enum Foo {foo, bar, baz}\n"+
		"  public void bar(Foo f) {\n"+
		"    switch(f) {\n"+
		"      case Foo.baz:\n"+
		"      case  // <-- invoke context assist here!\n"+
		"    }\n"+
		"  }\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "case ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"bar[FIELD_REF]{bar, Lenumbug.EnumBug$Foo;, Lenumbug.EnumBug$Foo;, bar, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_ENUM + R_ENUM_CONSTANT + R_NON_RESTRICTED + R_RESOLVED) + "}\n" +
			"baz[FIELD_REF]{baz, Lenumbug.EnumBug$Foo;, Lenumbug.EnumBug$Foo;, baz, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_ENUM + R_ENUM_CONSTANT + R_NON_RESTRICTED + R_RESOLVED) + "}\n" +
			"foo[FIELD_REF]{foo, Lenumbug.EnumBug$Foo;, Lenumbug.EnumBug$Foo;, foo, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_ENUM + R_ENUM_CONSTANT + R_NON_RESTRICTED + R_RESOLVED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=128169
public void test0266() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test<T, U, TU> extends SuperTest<T> {\n"+
		"  foo\n"+
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/SuperTest.java",
		"package test;\n"+
		"public class SuperTest<E> {\n"+
		"  public <T, U, TU> T foo(SuperTest<T> t, SuperTest<U> u, SuperTest<TU> tu, SuperTest<E> e) {return null;}\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"foo[POTENTIAL_METHOD_DECLARATION]{foo, Ltest.Test<TT;TU;TTU;>;, ()V, foo, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
			"foo[METHOD_DECLARATION]{public <V, W, TU2> V foo(test.SuperTest<V> t, test.SuperTest<W> u, test.SuperTest<TU2> tu, test.SuperTest<T> e), " +
				"Ltest.SuperTest<TT;>;, <V:Ljava.lang.Object;W:Ljava.lang.Object;TU2:Ljava.lang.Object;>(Ltest.SuperTest<TV;>;Ltest.SuperTest<TW;>;" +
				"Ltest.SuperTest<TTU2;>;Ltest.SuperTest<TT;>;)TV;, foo, (t, u, tu, e), " +
				(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_METHOD_OVERIDE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=128169
public void test0267() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test<T, U, TU> extends SuperTest {\n"+
		"  foo\n"+
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/SuperTest.java",
		"package test;\n"+
		"public class SuperTest<E> {\n"+
		"  public <T, U, TU> T foo(SuperTest<T> t, SuperTest<U> u, SuperTest<TU> tu, SuperTest<E> e) {return null;}\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"foo[POTENTIAL_METHOD_DECLARATION]{foo, Ltest.Test<TT;TU;TTU;>;, ()V, foo, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
			"foo[METHOD_DECLARATION]{public Object foo(SuperTest t, SuperTest u, SuperTest tu, SuperTest e), Ltest.SuperTest;, (Ltest.SuperTest;" +
				"Ltest.SuperTest;Ltest.SuperTest;Ltest.SuperTest;)Ljava.lang.Object;, foo, (t, u, tu, e), " +
				(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_METHOD_OVERIDE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=128169
public void test0268() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test<T, U, TU> extends SuperTest {\n"+
		"  foo\n"+
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/SuperTest.java",
		"package test;\n"+
		"public class SuperTest {\n"+
		"  public <T, U, TU> T foo(T t, U u, TU tu) {return null;}\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"foo[POTENTIAL_METHOD_DECLARATION]{foo, Ltest.Test<TT;TU;TTU;>;, ()V, foo, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
			"foo[METHOD_DECLARATION]{public <V, W, TU2> V foo(V t, W u, TU2 tu), Ltest.SuperTest;, <V:Ljava.lang.Object;W:Ljava.lang.Object;TU2:Ljava.lang.Object;>(TV;TW;TTU2;)TV;, foo, (t, u, tu), " +
				(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_METHOD_OVERIDE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=131681
public void test0269() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test extends SuperTest {\n"+
		"  foo\n"+
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/SuperTest.java",
		"package test;\n"+
		"public class SuperTest {\n"+
		"  public <T> void foo() {}\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"foo[POTENTIAL_METHOD_DECLARATION]{foo, Ltest.Test;, ()V, foo, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
			"foo[METHOD_DECLARATION]{public <T> void foo(), Ltest.SuperTest;, <T:Ljava.lang.Object;>()V, foo, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_METHOD_OVERIDE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
public void test0270() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test270_2.java",
		"package test;\n"+
		"public class Test270_2 extends SuperTest<Test270> {\n"+
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/SuperTest.java",
		"package test;\n"+
		"public class SuperTest<T> {\n"+
		"}");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src/test/Test270.java",
		"package test;\n"+
		"public class Test270 {\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "Test270";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test270_2[TYPE_REF]{Test270_2, test, Ltest.Test270_2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"Test270[TYPE_REF]{Test270, test, Ltest.Test270;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED + R_EXACT_NAME + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
public void test0271() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"	void foo() {\n"+
		"	  TestCollections.<Object>zzz\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/TestCollections.java",
		"package test;\n"+
		"public class TestCollections {\n"+
		"  public <T> void zzz1(T t) {}\n"+
		"  public static <T> void zzz2(T t) {}\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "zzz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"zzz2[METHOD_REF]{zzz2(), Ltest.TestCollections;, (Ljava.lang.Object;)V, zzz2, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_INHERITED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
public void test0272() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"	void foo(TestCollections t) {\n"+
		"	  t.<Object>zzz\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/TestCollections.java",
		"package test;\n"+
		"public class TestCollections {\n"+
		"  public <T> void zzz1(T t) {}\n"+
		"  public static <T> void zzz2(T t) {}\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "zzz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"zzz2[METHOD_REF]{zzz2(), Ltest.TestCollections;, (Ljava.lang.Object;)V, zzz2, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"zzz1[METHOD_REF]{zzz1(), Ltest.TestCollections;, (Ljava.lang.Object;)V, zzz1, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
public void test0273() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"	TestCollections bar() {\n"+
		"	  return null;\n"+
		"	}\n"+
		"	void foo() {\n"+
		"	  bar().<Object>zzz\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/TestCollections.java",
		"package test;\n"+
		"public class TestCollections {\n"+
		"  public <T> void zzz1(T t) {}\n"+
		"  public static <T> void zzz2(T t) {}\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "zzz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"zzz2[METHOD_REF]{zzz2(), Ltest.TestCollections;, (Ljava.lang.Object;)V, zzz2, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"zzz1[METHOD_REF]{zzz1(), Ltest.TestCollections;, (Ljava.lang.Object;)V, zzz1, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
public void test0274() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"	void foo() {\n"+
		"	  int.<Object>zzz\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/TestCollections.java",
		"package test;\n"+
		"public class TestCollections {\n"+
		"  public <T> void zzz1(T t) {}\n"+
		"  public static <T> void zzz2(T t) {}\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "zzz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
public void test0275() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"	void foo(int t) {\n"+
		"	  t.<Object>zzz\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/TestCollections.java",
		"package test;\n"+
		"public class TestCollections {\n"+
		"  public <T> void zzz1(T t) {}\n"+
		"  public static <T> void zzz2(T t) {}\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "zzz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
public void test0276() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"	int bar() {\n"+
		"	  return 0;\n"+
		"	}\n"+
		"	void foo() {\n"+
		"	  bar().<Object>zzz\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/TestCollections.java",
		"package test;\n"+
		"public class TestCollections {\n"+
		"  public <T> void zzz1(T t) {}\n"+
		"  public static <T> void zzz2(T t) {}\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "zzz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
public void test0277() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"	void foo(TestCollections[] o) {\n"+
		"	  o.<Object>zzz\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/TestCollections.java",
		"package test;\n"+
		"public class TestCollections {\n"+
		"  public <T> void zzz1(T t) {}\n"+
		"  public static <T> void zzz2(T t) {}\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "zzz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
public void test0278() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"  public <T> void zzz1(T t) {}\n"+
		"  public static <T> void zzz2(T t) {}\n"+
		"  void foo(TestCollections[] o) {\n"+
		"    this.<Object>zzz\n"+
		"  }\n"+
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "zzz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"zzz2[METHOD_REF]{zzz2(), Ltest.Test;, (Ljava.lang.Object;)V, zzz2, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"zzz1[METHOD_REF]{zzz1(), Ltest.Test;, (Ljava.lang.Object;)V, zzz1, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
public void test0279() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test extends TestCollections {\n"+
		"	void foo() {\n"+
		"	  super.<Object>zzz\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/TestCollections.java",
		"package test;\n"+
		"public class TestCollections {\n"+
		"  public <T> void zzz1(T t) {}\n"+
		"  public static <T> void zzz2(T t) {}\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "zzz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"zzz2[METHOD_REF]{zzz2(), Ltest.TestCollections;, (Ljava.lang.Object;)V, zzz2, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"zzz1[METHOD_REF]{zzz1(), Ltest.TestCollections;, (Ljava.lang.Object;)V, zzz1, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
public void test0280() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"	void foo() {\n"+
		"	  TestCollections.<Object, Object>zzz\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/TestCollections.java",
		"package test;\n"+
		"public class TestCollections {\n"+
		"  public <T> void zzz1(T t) {}\n"+
		"  public static <T> void zzz2(T t) {}\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "zzz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
public void test0281() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"	void foo() {\n"+
		"	  TestCollections.zzz\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/TestCollections.java",
		"package test;\n"+
		"public class TestCollections {\n"+
		"  public <T> void zzz1(T t) {}\n"+
		"  public static <T> void zzz2(T t) {}\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "zzz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"zzz2[METHOD_REF]{zzz2(), Ltest.TestCollections;, <T:Ljava.lang.Object;>(TT;)V, zzz2, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_INHERITED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
public void test0282() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"  public <T> void zzz1(T t) {}\n"+
		"  public static <T> void zzz2(T t) {}\n"+
		"  void foo() {\n"+
		"    this.<Unknown>zzz\n"+
		"  }\n"+
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "zzz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
public void test0283() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"  public <T, U> void zzz1(T t) {}\n"+
		"  public static <T> void zzz2(T t) {}\n"+
		"  void foo() {\n"+
		"    this.<Unknown, Object>zzz\n"+
		"  }\n"+
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "zzz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=106450
public void test0284() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"  public <T extends Test2> void zzz1(T t) {}\n"+
		"  public static <T extends Test2> void zzz2(T t) {}\n"+
		"  void foo() {\n"+
		"    this.<Object>zzz\n"+
		"  }\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/Test2.java",
		"package test;\n"+
		"public class Test2 {\n"+
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "zzz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"zzz2[METHOD_REF]{zzz2(), Ltest.Test;, (Ljava.lang.Object;)V, zzz2, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"zzz1[METHOD_REF]{zzz1(), Ltest.Test;, (Ljava.lang.Object;)V, zzz1, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133491
public void test0285() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/other/Test.java",
		"package other;\n"+
		"import pack.*;\n"+
		"public class Test {\n"+
		"  @MyAnnotation(ZZZN\n"+
		"  public void hello() {\n"+
		"  }\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/pack/ZZZNeedsImportEnum.java",
		"package pack;\n"+
		"public enum ZZZNeedsImportEnum {\n"+
		"  HELLO;\n"+
		"}\n");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src/pack/MyAnnotation.java",
		"package pack;\n"+
		"public @interface MyAnnotation {\n"+
		"  ZZZNeedsImportEnum value();\n"+
		"  boolean value2() default false;\n"+
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZN";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZNeedsImportEnum[TYPE_REF]{ZZZNeedsImportEnum, pack, Lpack.ZZZNeedsImportEnum;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ENUM + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=95829
public void test0286() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"  void bar(Test2<Object> t) {\n"+
		"    t.fo\n"+
		"  }\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/Test1.java",
		"package test;\n"+
		"public interface Test1<U> {\n"+
		"  <T> T[] foo(T[] t);\n"+
		"}\n");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src/test/Test2.java",
		"package test;\n"+
		"public interface Test2<U> extends Test1<U> {\n"+
		"  <T> T[] foo(T[] t);\n"+
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "t.fo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.Test2<Ljava.lang.Object;>;, <T:Ljava.lang.Object;>([TT;)[TT;, foo, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=95829
public void test0287() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test implements Test2<Object>{\n"+
		"  fo\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/Test1.java",
		"package test;\n"+
		"public interface Test1<U> {\n"+
		"  <T> T[] foo(T[] t);\n"+
		"}\n");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src/test/Test2.java",
		"package test;\n"+
		"public interface Test2<U> extends Test1<U> {\n"+
		"  <T> T[] foo(T[] t);\n"+
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "fo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"fo[POTENTIAL_METHOD_DECLARATION]{fo, Ltest.Test;, ()V, fo, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
			"foo[METHOD_DECLARATION]{public <T> T[] foo(T[] t), Ltest.Test2<Ljava.lang.Object;>;, <T:Ljava.lang.Object;>([TT;)[TT;, foo, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_METHOD_OVERIDE + R_ABSTRACT_METHOD + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97085
public void test0288() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"import test0.tes"+
		"public class Test {\n"+
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test0/test1/X.java",
		"package test0/test1;"+
		"public class X {\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "test0.tes";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"test0.test1[PACKAGE_REF]{test0.test1.*;, test0.test1, null, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=97085
public void test0289() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"import static test0.tes"+
		"public class Test {\n"+
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test0/test1/X.java",
		"package test0/test1;"+
		"public class X {\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "test0.tes";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"test0.test1[PACKAGE_REF]{test0.test1., test0.test1, null, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=129983
public void test0290() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test0290/Test.java",
			"package test0290;\n" +
			"@\n" +
			"public class Test {\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/pkgannotations/QQAnnotation.java",
		"package pkgannotations;"+
		"public @interface QQAnnotation {\n"+
		"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"interface[KEYWORD]{interface, null, null, interface, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"QQAnnotation[TYPE_REF]{pkgannotations.QQAnnotation, pkgannotations, Lpkgannotations.QQAnnotation;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"YAAnnot[TYPE_REF]{testxxx.YAAnnot, testxxx, Ltestxxx.YAAnnot;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"_ConfigurationData[TYPE_REF]{test325481._ConfigurationData, test325481, Ltest325481._ConfigurationData;, null, null, " + (R_NAME_PREFIX + R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"_Path[TYPE_REF]{test325481._Path, test325481, Ltest325481._Path;, null, null, " + (R_NAME_PREFIX + R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"Deprecated[TYPE_REF]{Deprecated, java.lang, Ljava.lang.Deprecated;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED + R_UNQUALIFIED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=123225
public void test0291() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[5];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"  public void foo(){\n" +
			"    new Test2<Test4>().foo\n" +
			"  }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/Test1.java",
			"package test;\n" +
			"public class Test1<TTest1> {\n" +
			"  public void foo(TTest1 t){}\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/test/Test2.java",
			"package test;\n" +
			"public class Test2<TTest2 extends Test3> extends Test1<TTest2> {\n" +
			"  public void foo(Test3 t){}\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/test/Test3.java",
			"package test;\n" +
			"public class Test3 {\n" +
			"}");

	this.workingCopies[4] = getWorkingCopy(
			"/Completion/src3/test/Test4.java",
			"package test;\n" +
			"public class Test4 extends Test3 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = ".foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.Test2<Ltest.Test4;>;, (Ltest.Test3;)V, foo, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=123225
public void test0292() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[6];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"  public void foo(){\n" +
			"    new Test5().foo\n" +
			"  }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/Test1.java",
			"package test;\n" +
			"public class Test1<TTest1> {\n" +
			"  public void foo(TTest1 t){}\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/test/Test2.java",
			"package test;\n" +
			"public class Test2<TTest2 extends Test3> extends Test1<TTest2> {\n" +
			"  public void foo(Test3 t){}\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/test/Test3.java",
			"package test;\n" +
			"public class Test3 {\n" +
			"}");

	this.workingCopies[4] = getWorkingCopy(
			"/Completion/src3/test/Test4.java",
			"package test;\n" +
			"public class Test4 extends Test3 {\n" +
			"}");

	this.workingCopies[5] = getWorkingCopy(
			"/Completion/src3/test/Test5.java",
			"package test;\n" +
			"public class Test5 extends Test2<Test4> {\n" +
			"  public void foo(Test4 t){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = ".foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.Test2<Ltest.Test4;>;, (Ltest.Test3;)V, foo, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_STATIC + R_NON_RESTRICTED) + "}\n" +
			"foo[METHOD_REF]{foo(), Ltest.Test5;, (Ltest.Test4;)V, foo, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=123225
public void test0293() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[5];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test extends Test2<Test4> {\n" +
			"  public void foo(Test4 t){}\n" +
			"  public void bar(){\n" +
			"    foo\n" +
			"  }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/Test1.java",
			"package test;\n" +
			"public class Test1<TTest1> {\n" +
			"  public void foo(TTest1 t){}\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/test/Test2.java",
			"package test;\n" +
			"public class Test2<TTest2 extends Test3> extends Test1<TTest2> {\n" +
			"  public void foo(Test3 t){}\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/test/Test3.java",
			"package test;\n" +
			"public class Test3 {\n" +
			"}");

	this.workingCopies[4] = getWorkingCopy(
			"/Completion/src3/test/Test4.java",
			"package test;\n" +
			"public class Test4 extends Test3 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.Test2<Ltest.Test4;>;, (Ltest.Test3;)V, foo, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"foo[METHOD_REF]{foo(), Ltest.Test;, (Ltest.Test4;)V, foo, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=161557
public void test0294() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"  Test1<Test2> var[];\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/Test1.java",
			"package test;\n" +
			"public class Test1<TTest1> {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/test/Test2.java",
			"package test;\n" +
			"public class Test2 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "Test2";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test2[TYPE_REF]{Test2, test, Ltest.Test2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=99928
public void test0295() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[5];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    void test(StringTest s, IntegerTest i) {\n" +
			"        combine(s, i).compareTo(null);\n" +
			"    }\n" +
			"    \n" +
			"    <T> T combine(T t1, T t2) {\n" +
			"        return null;\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/StringTest.java",
			"package test;\n" +
			"public class StringTest implements ComparableTest<StringTest>, SerializableTest {\n" +
			"    public int compareTo(StringTest s) {\n" +
			"        return 0;\n" +
			"    }\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/test/IntegerTest.java",
			"package test;\n" +
			"public class IntegerTest implements ComparableTest<IntegerTest>, SerializableTest {\n" +
			"    public int compareTo(IntegerTest i) {\n" +
			"        return 0;\n" +
			"    }\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/test/ComparableTest.java",
			"package test;\n" +
			"public interface ComparableTest<T> {\n" +
			"    public int compareTo(T t) ;\n" +
			"}");

	this.workingCopies[4] = getWorkingCopy(
			"/Completion/src3/test/SerializableTest.java",
			"package test;\n" +
			"public interface SerializableTest {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "compare";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"compareTo[METHOD_REF]{compareTo(), Ltest.ComparableTest<Ljava.lang.Object;>;, (Ljava.lang.Object;)I, compareTo, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=99928
public void test0296() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[5];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"        public static void main(String[] args) {\n" +
			"                IntegerTest foo = null;\n" +
			"                StringTest bar = null;\n" +
			"                System.out.println((foo != null ? foo : bar).compare\n" +
			"        }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/StringTest.java",
			"package test;\n" +
			"public class StringTest implements ComparableTest<StringTest>, SerializableTest {\n" +
			"    public int compareTo(StringTest s) {\n" +
			"        return 0;\n" +
			"    }\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/test/IntegerTest.java",
			"package test;\n" +
			"public class IntegerTest implements ComparableTest<IntegerTest>, SerializableTest {\n" +
			"    public int compareTo(IntegerTest i) {\n" +
			"        return 0;\n" +
			"    }\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/test/ComparableTest.java",
			"package test;\n" +
			"public interface ComparableTest<T> {\n" +
			"    public int compareTo(T t) ;\n" +
			"}");

	this.workingCopies[4] = getWorkingCopy(
			"/Completion/src3/test/SerializableTest.java",
			"package test;\n" +
			"public interface SerializableTest {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "compare";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"compareTo[METHOD_REF]{compareTo(), Ltest.ComparableTest<Ljava.lang.Object;>;, (Ljava.lang.Object;)I, compareTo, (t), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=154993
public void test0297() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    String description = \"Some description\";\n" +
			"    @Description(this.description)\n" +
			"    public void method() {\n" +
			"    }");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/Description.java",
			"package test;\n" +
			"public @interface Description {\n" +
			"    String value();\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "this.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=164792
public void test0298() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void method(ZZZ[] z) {\n" +
			"        ZZZ[] z2 = z.clon\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/ZZZ.java",
			"package test;\n" +
			"public class ZZZ {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "clon";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"clone[METHOD_REF]{clone(), [Ltest.ZZZ;, ()[Ltest.ZZZ;, clone, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=164792
public void test0299() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void method(ZZZ z) {\n" +
			"        ZZZ z2 = z.clon\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/ZZZ.java",
			"package test;\n" +
			"public class ZZZ {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "clon";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, clone, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=157584
public void test0300() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test {\n" +
		"	public void throwing() throws IZZAException, Top<Object>.IZZException {}\n" +
		"	public void foo() {\n" +
		"      try {\n" +
		"         throwing();\n" +
		"      }\n" +
		"      catch (IZZAException e) {\n" +
		"         bar();\n" +
		"      }\n" +
		"      catch (IZZ) {\n" +
		"      }\n" +
		"   }" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/test/IZZAException.java",
			"package test;"+
			"public class IZZAException extends Exception {\n" +
			"}\n");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src/test/IZZException.java",
			"package test;"+
			"public class Top<T> {\n" +
			"  public class IZZException extends Exception {\n" +
			"  }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "IZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Top<java.lang.Object>.IZZException[TYPE_REF]{test.Top.IZZException, test, Ltest.Top<Ljava.lang.Object;>.IZZException;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXCEPTION + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0301() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @ZZZ\n" +
			"    public void method() {\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0302() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @ZZZ\n" +
			"    public void method() {\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0303() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @ZZZ\n" +
			"    public void method() {\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/TOPZZZ1.java",
			"package p;\n" +
			"public class TOPZZZ1 {\n" +
			"  @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})\n" +
			"  public @interface ZZZ1 {\n" +
			"  }\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/TOPZZZ2.java",
			"package p;\n" +
			"public class TOPZZZ2 {\n" +
			"  public @interface ZZZ2 {\n" +
			"  }\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"TOPZZZ1.ZZZ1[TYPE_REF]{p.TOPZZZ1.ZZZ1, p, Lp.TOPZZZ1$ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"TOPZZZ2.ZZZ2[TYPE_REF]{p.TOPZZZ2.ZZZ2, p, Lp.TOPZZZ2$ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0304() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})\n" +
			"@interface ZZZ1 {\n" +
			"}\n" +
			"@interface ZZZ2 {\n" +
			"}\n" +
			"public class Test {\n" +
			"    @ZZZ\n" +
			"    public void method() {\n" +
			"    }\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{ZZZ1, test, Ltest.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{ZZZ2, test, Ltest.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0305() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"class TOPZZZ1 {\n" +
			"  @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})\n" +
			"  public @interface ZZZ1 {\n" +
			"  }\n" +
			"}\n" +
			"class TOPZZZ2 {\n" +
			"  public @interface ZZZ2 {\n" +
			"  }\n" +
			"}\n" +
			"public class Test {\n" +
			"    @ZZZ\n" +
			"    public void method() {\n" +
			"    }\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"TOPZZZ1.ZZZ1[TYPE_REF]{test.TOPZZZ1.ZZZ1, test, Ltest.TOPZZZ1$ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"TOPZZZ2.ZZZ2[TYPE_REF]{test.TOPZZZ2.ZZZ2, test, Ltest.TOPZZZ2$ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0306() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"  public static class TOPZZZ1 {\n" +
			"    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})\n" +
			"    public @interface ZZZ1 {\n" +
			"    }\n" +
			"  }\n" +
			"  public static class TOPZZZ2 {\n" +
			"    public @interface ZZZ2 {\n" +
			"    }\n" +
			"  }\n" +
			"  public class TOPZZZ3 {\n" +
			"    public @interface ZZZ3 {\n" +
			"    }\n" +
			"  }\n" +
			"  @ZZZ\n" +
			"  public void method() {\n" +
			"  }\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.TOPZZZ1.ZZZ1[TYPE_REF]{test.Test.TOPZZZ1.ZZZ1, test, Ltest.Test$TOPZZZ1$ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"Test.TOPZZZ2.ZZZ2[TYPE_REF]{test.Test.TOPZZZ2.ZZZ2, test, Ltest.Test$TOPZZZ2$ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0307() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})\n" +
			"    public @interface ZZZ1 {\n" +
			"    }\n" +
			"    public @interface ZZZ2 {\n" +
			"    }\n" +
			"    @ZZZ\n" +
			"    public void method() {\n" +
			"    }\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.ZZZ1[TYPE_REF]{ZZZ1, test, Ltest.Test$ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"Test.ZZZ2[TYPE_REF]{ZZZ2, test, Ltest.Test$ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0308() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @ZZZ\n" +
			"    public void method() {\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/p/ZZZ3.java",
			"package p;\n" +
			"public @interface ZZZ3 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"ZZZ3[TYPE_REF]{p.ZZZ3, p, Lp.ZZZ3;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0309() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @ZZZ\n" +
			"    public class TestInner {\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.FIELD})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/p/ZZZ3.java",
			"package p;\n" +
			"public @interface ZZZ3 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"ZZZ3[TYPE_REF]{p.ZZZ3, p, Lp.ZZZ3;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0310() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @ZZZ\n" +
			"    public int field;\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.FIELD})\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/p/ZZZ3.java",
			"package p;\n" +
			"public @interface ZZZ3 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"ZZZ3[TYPE_REF]{p.ZZZ3, p, Lp.ZZZ3;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0311() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void foo(@ZZZ int param){}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.PARAMETER})\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/p/ZZZ3.java",
			"package p;\n" +
			"public @interface ZZZ3 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"ZZZ3[TYPE_REF]{p.ZZZ3, p, Lp.ZZZ3;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0312() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @ZZZ\n" +
			"    public Test(){}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.CONSTRUCTOR})\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/p/ZZZ3.java",
			"package p;\n" +
			"public @interface ZZZ3 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"ZZZ3[TYPE_REF]{p.ZZZ3, p, Lp.ZZZ3;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
// When the completion is inside a method body the annotation cannot be accuratly attached to the correct node by completino recovery.
// So relevance based on annotation target are ignored.
public void test0313() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    {\n" +
			"        @ZZZ\n" +
			"        int var = 0;\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.LOCAL_VARIABLE})\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/p/ZZZ3.java",
			"package p;\n" +
			"public @interface ZZZ3 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ3[TYPE_REF]{p.ZZZ3, p, Lp.ZZZ3;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0314() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @ZZZ\n" +
			"    public @interface TestInner {}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.ANNOTATION_TYPE})\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/p/ZZZ3.java",
			"package p;\n" +
			"public @interface ZZZ3 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"ZZZ3[TYPE_REF]{p.ZZZ3, p, Lp.ZZZ3;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0315() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"@ZZZ package test;\n" +
			"public class Test {\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.PACKAGE})\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/p/ZZZ3.java",
			"package p;\n" +
			"public @interface ZZZ3 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"ZZZ3[TYPE_REF]{p.ZZZ3, p, Lp.ZZZ3;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0316() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @ZZZ\n" +
			"    public Test(){}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.CONSTRUCTOR})\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/p/ZZZ3.java",
			"package p;\n" +
			"public @interface ZZZ3 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"ZZZ3[TYPE_REF]{p.ZZZ3, p, Lp.ZZZ3;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0317() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @ZZZ\n" +
			"    public @interface TestInner {}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.ANNOTATION_TYPE})\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/p/ZZZ3.java",
			"package p;\n" +
			"public @interface ZZZ3 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"ZZZ3[TYPE_REF]{p.ZZZ3, p, Lp.ZZZ3;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0318() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"@ZZZ\n" +
			"import test.*;\n" +
			"public class Test {\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0319() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @ZZZ\n" +
			"    {}\n" +
			"    public void foo() {}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0320() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[5];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @ZZZ\n" +
			"    {\n" +
			"        int var = 0;\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.FIELD})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.PARAMETER})\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/p/ZZZ3.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.LOCAL_VARIABLE})\n" +
			"public @interface ZZZ3 {\n" +
			"}");

	this.workingCopies[4] = getWorkingCopy(
			"/Completion/src3/p/ZZZ4.java",
			"package p;\n" +
			"public @interface ZZZ4 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ3[TYPE_REF]{p.ZZZ3, p, Lp.ZZZ3;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ4[TYPE_REF]{p.ZZZ4, p, Lp.ZZZ4;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0321() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @ZZZ({})\n" +
			"    int var = 0;\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.FIELD})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0321b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @ZZZ(value={})\n" +
			"    int var = 0;\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.FIELD})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0322() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @ZZZ @Annot\n" +
			"    int var = 0;\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.FIELD})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0323() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @ZZZ @Annot({})\n" +
			"    int var = 0;\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.FIELD})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0324() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void foo(@ZZZ int param){}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.FIELD})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.PARAMETER})\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/p/ZZZ3.java",
			"package p;\n" +
			"public @interface ZZZ3 {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"ZZZ3[TYPE_REF]{p.ZZZ3, p, Lp.ZZZ3;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0325() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @p.Annot(@ZZZ)\n" +
			"    public void foo(){}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/p/Annot.java",
			"package p;\n" +
			"public @interface Annot {\n" +
			"    ZZZ2 value();\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0326() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @p.Annot(@ZZZ(value=0))\n" +
			"    public void foo(){}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/p/Annot.java",
			"package p;\n" +
			"public @interface Annot {\n" +
			"    ZZZ2 value();\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0327() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @p.Annot(@ZZZ(value=0\n" +
			"    public void foo(){}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/p/Annot.java",
			"package p;\n" +
			"public @interface Annot {\n" +
			"    ZZZ2 value();\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0328() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    @p.Annot(@ZZZ(value=\n" +
			"    public void foo(){}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/p/Annot.java",
			"package p;\n" +
			"public @interface Annot {\n" +
			"    ZZZ2 value();\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}\n" +
			"ZZZ2[TYPE_REF]{p.ZZZ2, p, Lp.ZZZ2;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=158985
public void test0329() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"@ZZZ\n" +
			"public class Test {\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/p/ZZZ1.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})\n" +
			"public @interface ZZZ1 {\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/p/ZZZ2.java",
			"package p;\n" +
			"@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})\n" +
			"public @interface ZZZ2 {\n" +
			"}");

	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src3/p/ZZZ3.java",
			"package p;\n" +
			"public @interface ZZZ3 {\n" +
			"}");


	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "ZZZ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZZZ1[TYPE_REF]{p.ZZZ1, p, Lp.ZZZ1;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}\n" +
			"ZZZ3[TYPE_REF]{p.ZZZ3, p, Lp.ZZZ3;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185318
public void test0330() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import static test0330.q.Y.foo;\n" +
			"public class Test {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"foo1[METHOD_NAME_REFERENCE]{foo1;, Ltest0330.q.Y;, ()Ltest0330.p.X;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"foo2[METHOD_NAME_REFERENCE]{foo2;, Ltest0330.q.Y;, ()V, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185318
public void test0331() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import static test0331.q.Y.foo;\n" +
			"public class Test {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"foo2[METHOD_NAME_REFERENCE]{foo2;, Ltest0331.q.Y;, ()V, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185318
public void test0332() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import static test0332.q.Y.foo;\n" +
			"public class Test {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"foo1[FIELD_REF]{foo1;, Ltest0332.q.Y;, Ltest0332.p.X;, foo1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"foo2[FIELD_REF]{foo2;, Ltest0332.q.Y;, I, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=185318
public void test0333() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import static test0333.q.Y.foo;\n" +
			"public class Test {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"foo2[FIELD_REF]{foo2;, Ltest0333.q.Y;, I, foo2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=151967
public void test0334() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/ClassC.java",
			"package test;\n" +
			"public class ClassC {\n" +
			"	public ClassC() {\n" +
			"		EnumB.B1.a();\n" +
			"		EnumB.B1.b();\n" +
			"	}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/InterfaceA.java",
			"package test;\n" +
			"public interface InterfaceA {\n" +
			"	void a();\n" +
			"	void b();\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/test/EnumB.java",
			"package test;\n" +
			"public enum EnumB implements InterfaceA {\n" +
			"	B1 {\n" +
			"		public void b() {\n" +
			"			// do something for B1\n" +
			"		}\n" +
			"	},\n" +
			"	B2 {\n" +
			"		public void b() {\n" +
			"			// do something for B2\n" +
			"		}\n" +
			"	};\n" +
			"	public void a() {\n" +
			"		// do something in common\n" +
			"	}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "EnumB.B1.a";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"a[METHOD_REF]{a, Ltest.EnumB;, ()V, a, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=151967
public void test0335() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/ClassC.java",
			"package test;\n" +
			"public class ClassC {\n" +
			"	public ClassC() {\n" +
			"		EnumB.B1.a();\n" +
			"		EnumB.B1.b();\n" +
			"	}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/InterfaceA.java",
			"package test;\n" +
			"public interface InterfaceA {\n" +
			"	void a();\n" +
			"	void b();\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/test/EnumB.java",
			"package test;\n" +
			"public enum EnumB implements InterfaceA {\n" +
			"	B1 {\n" +
			"		public void b() {\n" +
			"			// do something for B1\n" +
			"		}\n" +
			"	},\n" +
			"	B2 {\n" +
			"		public void b() {\n" +
			"			// do something for B2\n" +
			"		}\n" +
			"	};\n" +
			"	public void a() {\n" +
			"		// do something in common\n" +
			"	}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "EnumB.B1.b";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"B1[FIELD_REF]{B1, Ltest.EnumB;, Ltest.EnumB;, B1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
			"B2[FIELD_REF]{B2, Ltest.EnumB;, Ltest.EnumB;, B2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
			"b[METHOD_REF]{b, Ltest.InterfaceA;, ()V, b, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=99631
public void test0336() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"@boole"+
		"public class Test {\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/booleanClass.java",
		"package test;"+
		"public @interface booleanClass {\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "boole";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"booleanClass[TYPE_REF]{booleanClass, test, Ltest.booleanClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_UNQUALIFIED + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=99631
public void test0337() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"@voi"+
		"public class Test {\n" +
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/voidClass.java",
		"package test;"+
		"public @interface voidClass {\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "voi";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"voidClass[TYPE_REF]{voidClass, test, Ltest.voidClass;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_UNQUALIFIED + R_TARGET + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=212153
public void test0338() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Xxx {\n"+
		"	static {\n"+
		"		new Object() {\n"+
		"			public boolean f(Object o) {\n"+
		"				if (o instanceof Yyy) {\n"+
		"					((Yyy<?, ?>)o).getZzz().\n"+
		"				}\n"+
		"			}\n"+
		"		};\n"+
		"	}\n"+
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "getZzz().";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=215331
public void test0339() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import test2.ClassA;\n" +
			"public class Test {\n" +
			"	void foo() {\n" +
			"		ClassA.Mem\n" +
			"	}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test2/ClassA.java",
			"package test2;\n" +
			"public class ClassA<T> {\n" +
			"	public class Member<U> {}\n" +
			"	public static class MemberStatic<V> {}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, false, true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "ClassA.Mem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ClassA.Member[TYPE_REF]{Member, test2, Ltest2.ClassA$Member;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_QUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"ClassA.MemberStatic<V>[TYPE_REF]{MemberStatic, test2, Ltest2.ClassA$MemberStatic<TV;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_QUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=215331
public void test0340() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"	void foo() {\n" +
			"		ClassA.Mem\n" +
			"	}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test2/ClassA.java",
			"package test2;\n" +
			"public class ClassA<T> {\n" +
			"	public class Member<U> {}\n" +
			"	public static class MemberStatic<V> {}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, false, true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "ClassA.Mem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ClassA.Member[TYPE_REF]{Member, test2, Ltest2.ClassA$Member;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_QUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"   ClassA[TYPE_REF]{test2.ClassA, test2, Ltest2.ClassA;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_QUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"ClassA.MemberStatic<V>[TYPE_REF]{MemberStatic, test2, Ltest2.ClassA$MemberStatic<TV;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_QUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"   ClassA[TYPE_REF]{test2.ClassA, test2, Ltest2.ClassA;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_QUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=215331
public void test0341() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import test2.ClassA;\n" +
			"public class Test {\n" +
			"	void foo() {\n" +
			"		ClassA<Object>.Mem\n" +
			"	}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test2/ClassA.java",
			"package test2;\n" +
			"public class ClassA<T> {\n" +
			"	public class Member<U> {}\n" +
			"	public static class MemberStatic<V> {}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, false, true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "ClassA<Object>.Mem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ClassA<java.lang.Object>.Member<U>[TYPE_REF]{Member, test2, Ltest2.ClassA<Ljava.lang.Object;>.Member<TU;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=215331
public void test0342() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"	void foo() {\n" +
			"		ClassA<Object>.Mem\n" +
			"	}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test2/ClassA.java",
			"package test2;\n" +
			"public class ClassA<T> {\n" +
			"	public class Member<U> {}\n" +
			"	public static class MemberStatic<V> {}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, false, true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "ClassA<Object>.Mem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ClassA<java.lang.Object>.Member<U>[TYPE_REF]{Member, test2, Ltest2.ClassA<Ljava.lang.Object;>.Member<TU;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_NON_RESTRICTED + R_NO_PROBLEMS) + "}\n" +
			"   ClassA[TYPE_REF]{test2.ClassA, test2, Ltest2.ClassA;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_NON_RESTRICTED + R_NO_PROBLEMS) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=215331
public void test0343() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import test2.ClassA;\n" +
			"public class Test {\n" +
			"	void foo() {\n" +
			"		ClassA<ClassB>.Mem\n" +
			"	}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test2/ClassA.java",
			"package test2;\n" +
			"public class ClassA<T> {\n" +
			"	public class Member<U> {}\n" +
			"	public static class MemberStatic<V> {}\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/test2/ClassB.java",
			"package test2;\n" +
			"public class ClassB {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, false, true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "ClassA<ClassB>.Mem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ClassA<test2.ClassB>.Member<U>[TYPE_REF]{Member, test2, Ltest2.ClassA<Ltest2.ClassB;>.Member<TU;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_NON_RESTRICTED + R_NO_PROBLEMS) + "}\n" +
			"   ClassB[TYPE_REF]{test2.ClassB, test2, Ltest2.ClassB;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_NON_RESTRICTED + R_NO_PROBLEMS) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=215331
public void test0344() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import test2.ClassA;\n" +
			"public class Test {\n" +
			"	ClassA.Mem\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test2/ClassA.java",
			"package test2;\n" +
			"public class ClassA<T> {\n" +
			"	public class Member<U> {}\n" +
			"	public static class MemberStatic<V> {}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, false, true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "ClassA.Mem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ClassA.Member[TYPE_REF]{Member, test2, Ltest2.ClassA$Member;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"ClassA.MemberStatic<V>[TYPE_REF]{MemberStatic, test2, Ltest2.ClassA$MemberStatic<TV;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=215331
public void test0345() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"	ClassA.Mem\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test2/ClassA.java",
			"package test2;\n" +
			"public class ClassA<T> {\n" +
			"	public class Member<U> {}\n" +
			"	public static class MemberStatic<V> {}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, false, true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "ClassA.Mem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ClassA.Member[TYPE_REF]{Member, test2, Ltest2.ClassA$Member;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"   ClassA[TYPE_REF]{test2.ClassA, test2, Ltest2.ClassA;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"ClassA.MemberStatic<V>[TYPE_REF]{MemberStatic, test2, Ltest2.ClassA$MemberStatic<TV;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"   ClassA[TYPE_REF]{test2.ClassA, test2, Ltest2.ClassA;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=215331
public void test0346() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import test2.ClassA;\n" +
			"public class Test {\n" +
			"	ClassA<Object>.Mem\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test2/ClassA.java",
			"package test2;\n" +
			"public class ClassA<T> {\n" +
			"	public class Member<U> {}\n" +
			"	public static class MemberStatic<V> {}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, false, true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "ClassA<Object>.Mem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ClassA<java.lang.Object>.Member<U>[TYPE_REF]{Member, test2, Ltest2.ClassA<Ljava.lang.Object;>.Member<TU;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=215331
public void test0347() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"		ClassA<Object>.Mem\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test2/ClassA.java",
			"package test2;\n" +
			"public class ClassA<T> {\n" +
			"	public class Member<U> {}\n" +
			"	public static class MemberStatic<V> {}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, false, true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "ClassA<Object>.Mem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ClassA<java.lang.Object>.Member<U>[TYPE_REF]{Member, test2, Ltest2.ClassA<Ljava.lang.Object;>.Member<TU;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_NON_RESTRICTED + R_NO_PROBLEMS) + "}\n" +
			"   ClassA[TYPE_REF]{test2.ClassA, test2, Ltest2.ClassA;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_NON_RESTRICTED + R_NO_PROBLEMS) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=215331
public void test0348() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import test2.ClassA;\n" +
			"public class Test {\n" +
			"	ClassA<ClassB>.Mem\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test2/ClassA.java",
			"package test2;\n" +
			"public class ClassA<T> {\n" +
			"	public class Member<U> {}\n" +
			"	public static class MemberStatic<V> {}\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/test2/ClassB.java",
			"package test2;\n" +
			"public class ClassB {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, false, true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "ClassA<ClassB>.Mem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ClassA<test2.ClassB>.Member<U>[TYPE_REF]{Member, test2, Ltest2.ClassA<Ltest2.ClassB;>.Member<TU;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_NON_RESTRICTED + R_NO_PROBLEMS) + "}\n" +
			"   ClassB[TYPE_REF]{test2.ClassB, test2, Ltest2.ClassB;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_NON_RESTRICTED + R_NO_PROBLEMS) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=215331
public void test0349() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import test2.ClassA;\n" +
			"public class Test<T> {\n" +
			"	public class Member<U> {}\n" +
			"	public static class MemberStatic<V> {}\n" +
			"	void foo() {\n" +
			"		Mem\n" +
			"	}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, false, true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "Mem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Test.MemberStatic<V>[TYPE_REF]{MemberStatic, test, Ltest.Test$MemberStatic<TV;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"Test<T>.Member<U>[TYPE_REF]{Member, test, Ltest.Test<TT;>.Member<TU;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=215331
public void test0350() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import test2.ClassA;\n" +
			"public class Test<T> {\n" +
			"	public class Member<U> {}\n" +
			"	public static class MemberStatic<V> {}\n" +
			"	Mem\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, false, false, true);
	requestor.allowAllRequiredProposals();

	String str = this.workingCopies[0].getSource();
	String completeBehind = "Mem";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Mem[POTENTIAL_METHOD_DECLARATION]{Mem, Ltest.Test<TT;>;, ()V, Mem, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
			"Test.MemberStatic<V>[TYPE_REF]{MemberStatic, test, Ltest.Test$MemberStatic<TV;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"Test<T>.Member<U>[TYPE_REF]{Member, test, Ltest.Test<TT;>.Member<TU;>;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING  + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=96604
public void test0351() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"import test.util.List;\n"+
		"public class X<U, V extends List<U>> {\n"+
		"	V v;\n"+
		"	void foo(X<String, ?> x1, X<Object, ?> x2) {\n"+
		"		x1.v.get\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/util/Test.java",
		"package test.util;\n"+
		"public interface List<T> {\n"+
		"	public T get(int i);\n"+
		"}n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "x1.v.get";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}\n" +
			"get[METHOD_REF]{get(), Ltest.util.List<Ljava.lang.String;>;, (I)Ljava.lang.String;, get, (i), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=96604
public void test0352() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"import test.util.List;\n"+
		"public class X<U, V extends List<U>> {\n"+
		"	V v;\n"+
		"	void foo(X<String, ?> x1, X<Object, ?> x2) {\n"+
		"		x1.v.get\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/util/Test.java",
		"package test.util;\n"+
		"public class List<T> {\n"+
		"	public T get(int i) {return null;}\n"+
		"}n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "x1.v.get";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}\n" +
			"get[METHOD_REF]{get(), Ltest.util.List<Ljava.lang.String;>;, (I)Ljava.lang.String;, get, (i), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=96604
public void test0353() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"import test.util.List;\n"+
		"public class X<U, V extends List<U>> {\n"+
		"	X<?,?> foo(X<String, ?> xxxxx) {\n"+
		"		xxxxx // the type should not be captured\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/util/Test.java",
		"package test.util;\n"+
		"public interface List<T> {\n"+
		"	public T get(int i);\n"+
		"}n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "xxxxx";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"xxxxx[LOCAL_VARIABLE_REF]{xxxxx, null, Ltest.X<Ljava.lang.String;*>;, xxxxx, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=96604
public void test0354() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"import test.util.List;\n"+
		"public class X<U, V extends List<U>> {\n"+
		"	X<?,?> foo(X<String, ?> xxxxx) {\n"+
		"		Object o = (List<String>) xxxxx // the type should not be captured\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/util/Test.java",
		"package test.util;\n"+
		"public interface List<T> {\n"+
		"	public T get(int i);\n"+
		"}n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "xxxxx";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"xxxxx[LOCAL_VARIABLE_REF]{xxxxx, null, Ltest.X<Ljava.lang.String;*>;, xxxxx, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=96604
public void test0355() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"import test.util.List;\n"+
		"public class X<U, V extends List<U>> {\n"+
		"	X<?,?> foo(X<String, ?> xxxxx) {\n"+
		"		return xxxxx;// the type should not be captured\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/util/Test.java",
		"package test.util;\n"+
		"public interface List<T> {\n"+
		"	public T get(int i);\n"+
		"}n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "xxxxx";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"xxxxx[LOCAL_VARIABLE_REF]{xxxxx, null, Ltest.X<Ljava.lang.String;*>;, xxxxx, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=96604
public void test0356() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"import test.util.List;\n"+
		"public class X<T, U extends List<U>> {\n"+
		"	U get() { return null; }\n"+
		"	void foo(X<String, ?> x) {\n"+
		"		x.get().get // should show capture\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/util/Test.java",
		"package test.util;\n"+
		"public interface List<T> {\n"+
		"	public T get(int i);\n"+
		"}n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "get().get";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}\n" +
			"get[METHOD_REF]{get(), Ltest.util.List<Ltest.util.List<TT;>;>;, (I)Ltest.util.List<TT;>;, get, (i), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=96604
public void test0357() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"import test.util.List;\n"+
		"public class X<U extends X<U>> {\n"+
		"	U get() { return null; }\n"+
		"	X<?> foo(X<?> x) {\n"+
		"		x.get().get // should show capture\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/util/Test.java",
		"package test.util;\n"+
		"public interface List<T> {\n"+
		"	public T get(int i);\n"+
		"}n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "get().get";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}\n" +
			"get[METHOD_REF]{get(), Ltest.X<Ltest.X<Ljava.lang.Object;>;>;, ()Ltest.X<Ltest.X<Ljava.lang.Object;>;>;, get, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=96604
public void test0358() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"import test.util.List;\n"+
		"public class X<U, V extends List<U>> {\n"+
		"	V v() {return null;}\n"+
		"	void foo(X<String, ?> x1, X<Object, ?> x2) {\n"+
		"		x1.v().get\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/util/Test.java",
		"package test.util;\n"+
		"public interface List<T> {\n"+
		"	public T get(int i);\n"+
		"}n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "x1.v().get";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, getClass, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_STATIC + R_NON_RESTRICTED) + "}\n" +
			"get[METHOD_REF]{get(), Ltest.util.List<Ljava.lang.String;>;, (I)Ljava.lang.String;, get, (i), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_STATIC + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=96604
public void test0359() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"import test.util.List;\n"+
		"public class X<U, V extends List<U>> {\n"+
		"	V v;\n"+
		"	void foo(X<String, ?> x1, X<Object, ?> x2) {\n"+
		"		x1.v.new Inner(\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/util/Test.java",
		"package test.util;\n"+
		"public class List<T> {\n"+
		"	public class Inner { }\n"+
		"}n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "Inner(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Inner[METHOD_REF<CONSTRUCTOR>]{, Ltest.util.List<Ljava.lang.String;>.Inner;, ()V, Inner, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
			"List<java.lang.String>.Inner[ANONYMOUS_CLASS_DECLARATION]{, Ltest.util.List<Ljava.lang.String;>.Inner;, ()V, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=96604
public void test0360() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"import test.util.List;\n"+
		"public class X<U, V extends List<U>> {\n"+
		"	V v;\n"+
		"	void foo(X<String, ?> x1, X<Object, ?> x2) {\n"+
		"		x1.v.get(\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/util/Test.java",
		"package test.util;\n"+
		"public interface List<T> {\n"+
		"	public T get(int i);\n"+
		"}n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "x1.v.get(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"get[METHOD_REF]{, Ltest.util.List<Ljava.lang.String;>;, (I)Ljava.lang.String;, get, (i), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=97310
public void test0361() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[4];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/ZTest.java",
		"package test;\n"+
		"public class ZTest <ZTest0, A extends ZTest1 & ZTes >{\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/ZTest1.java",
		"package test;\n"+
		"public class ZTest1 {\n"+
		"}\n");

	this.workingCopies[2] = getWorkingCopy(
		"/Completion/src/test/ZTest2.java",
		"package test;\n"+
		"public class ZTest2 {\n"+
		"}\n");

	this.workingCopies[3] = getWorkingCopy(
		"/Completion/src/test/ZTest3.java",
		"package test;\n"+
		"public interface ZTest3 {\n"+
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "& ZTes";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ZTest3[TYPE_REF]{ZTest3, test, Ltest.ZTest3;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_INTERFACE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=82712
public void test0362() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"import static test.util.Math.*;\n"+
		"public class Test {\n"+
		"	void foo() {\n"+
		"	  abs();\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/util/Math.java",
		"package test.util;\n"+
		"public class Math {\n"+
		"	public static int abs(int i) {return null;}\n"+
		"}n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "abs(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"abs[METHOD_REF]{, Ltest.util.Math;, (I)I, abs, (i), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=82712
public void test0363() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"import static test.util.Math.abs;\n"+
		"public class Test {\n"+
		"	void foo() {\n"+
		"	  abs();\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/util/Math.java",
		"package test.util;\n"+
		"public class Math {\n"+
		"	public static int abs(int i) {return null;}\n"+
		"}n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "abs(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, hashCode, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"abs[METHOD_REF]{, Ltest.util.Math;, (I)I, abs, (i), " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=161030
public void test0364() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"import test.p.MyEnum;\n"+
		"public class Test {\n"+
		"	void foo(MyEnum myEnumVar) {\n"+
		"	  foo(MyEnu\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/p/Math.java",
		"package test.p;\n"+
		"public enum MyEnum {\n"+
		"	MyEnum1, MyEnum2;\n"+
		"}n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "MyEnu";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"myEnumVar[LOCAL_VARIABLE_REF]{myEnumVar, null, Ltest.p.MyEnum;, myEnumVar, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_ENUM + R_ENUM_CONSTANT + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"MyEnum[TYPE_REF]{MyEnum, test.p, Ltest.p.MyEnum;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ENUM + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"MyEnum1[FIELD_REF]{MyEnum.MyEnum1, Ltest.p.MyEnum;, Ltest.p.MyEnum;, MyEnum1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ENUM + R_ENUM_CONSTANT + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED) + "}\n" +
			"MyEnum2[FIELD_REF]{MyEnum.MyEnum2, Ltest.p.MyEnum;, Ltest.p.MyEnum;, MyEnum2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ENUM + R_ENUM_CONSTANT + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=161030
public void test0365() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"import test.p.MyEnum;\n"+
		"public class Test {\n"+
		"	void foo(MyEnum myEnumVar) {\n"+
		"	  foo(myEnu\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/p/Math.java",
		"package test.p;\n"+
		"public enum MyEnum {\n"+
		"	MyEnum1, MyEnum2;\n"+
		"}n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "myEnu";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"MyEnum[TYPE_REF]{MyEnum, test.p, Ltest.p.MyEnum;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_ENUM + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"MyEnum1[FIELD_REF]{MyEnum.MyEnum1, Ltest.p.MyEnum;, Ltest.p.MyEnum;, MyEnum1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_ENUM + R_ENUM_CONSTANT + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED) + "}\n" +
			"MyEnum2[FIELD_REF]{MyEnum.MyEnum2, Ltest.p.MyEnum;, Ltest.p.MyEnum;, MyEnum2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_ENUM + R_ENUM_CONSTANT + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED) + "}\n" +
			"myEnumVar[LOCAL_VARIABLE_REF]{myEnumVar, null, Ltest.p.MyEnum;, myEnumVar, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ENUM + R_ENUM_CONSTANT + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=161030
public void test0366() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"import test.p.MyEnum;\n"+
		"public class Test {\n"+
		"	void foo(MyEnum MyEnumVar) {\n"+
		"	  foo(MyEnu\n"+
		"	}\n"+
		"}\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/p/Math.java",
		"package test.p;\n"+
		"public enum MyEnum {\n"+
		"	MyEnum1, MyEnum2;\n"+
		"}n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "MyEnu";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"MyEnum[TYPE_REF]{MyEnum, test.p, Ltest.p.MyEnum;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ENUM + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"MyEnum1[FIELD_REF]{MyEnum.MyEnum1, Ltest.p.MyEnum;, Ltest.p.MyEnum;, MyEnum1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ENUM + R_ENUM_CONSTANT + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED) + "}\n" +
			"MyEnum2[FIELD_REF]{MyEnum.MyEnum2, Ltest.p.MyEnum;, Ltest.p.MyEnum;, MyEnum2, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ENUM + R_ENUM_CONSTANT + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED) + "}\n" +
			"MyEnumVar[LOCAL_VARIABLE_REF]{MyEnumVar, null, Ltest.p.MyEnum;, MyEnumVar, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ENUM + R_ENUM_CONSTANT + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=222326
public void test0367() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[7];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"public interface X<T1  extends Y<?, ?, ?>, T2 extends Zz<?, ?, ?>> extends  O<T1> , U<T2> {}");
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/Y.java",
			"public interface Y<T1 extends Y<?, ?, ?>, T2 extends Zz<?, ?, ?>, T3 extends X<?, ?>> extends X<T1, T2>, N<T3> {}");
	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src/Zz.java",
			"public interface Zz<T1 extends Y<?, ?, ?>, T2 extends Zz<?, ?, ?>, T3 extends X<?, ?>>extends X<T1, T2>, C<T3> {}");
	this.workingCopies[3] = getWorkingCopy(
			"/Completion/src/C.java",
			"public interface C<T extends U<? extends C<?>>> {}");
	this.workingCopies[4] = getWorkingCopy(
			"/Completion/src/N.java",
			"public interface N<T extends O<? extends N<?>>> {}");
	this.workingCopies[5] = getWorkingCopy(
			"/Completion/src/O.java",
			"public interface O<T extends N<? extends O<?>>> {}");
	this.workingCopies[6] = getWorkingCopy(
			"/Completion/src/U.java",
			"public interface U<T extends C<? extends U<?>>> {}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "extends Zz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
		"Zz[TYPE_REF]{Zz, , LZz;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED + R_CASE + R_EXACT_NAME + R_UNQUALIFIED) + "}",
		requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=209643
public void test0368() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public enu\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "enu";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"enum[KEYWORD]{enum, null, null, enum, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=209643
public void test0369() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n" +
		"	enu\n"+
		"\n");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/enumFoo.java",
		"package test;\n"+
		"public class enumFoo {\n" +
		"\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "enu";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"enu[POTENTIAL_METHOD_DECLARATION]{enu, Ltest.Test;, ()V, enu, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
			"Enum[TYPE_REF]{Enum, java.lang, Ljava.lang.Enum;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"enum[KEYWORD]{enum, null, null, enum, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"enumFoo[TYPE_REF]{enumFoo, test, Ltest.enumFoo;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=209643
public void test0370() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public @int\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@int";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"interface[KEYWORD]{interface, null, null, interface, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=209643
public void test0371() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"  public @int\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@int";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"interface[KEYWORD]{interface, null, null, interface, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_ANNOTATION + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=209643
public void test0372() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"  public void foo() {\n"+
		"    @int\n" +
		"  }\n"+
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@int";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=209643
public void test0373() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"  public void foo() {\n"+
		"    int var0;\n" +
		"    if (true) {\n" +
		"      int var1;\n" +
		"      @int\n" +
		"    }\n" +
		"  }\n"+
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@int";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=209643
public void test0374() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"  public void foo(@int float var) {\n"+
		"    \n" +
		"  }\n"+
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@int";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=106821
public void test0375() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];

	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test\n"+
		"{\n"+
		"        /**\n"+
		"         * @deprecated\n"+
		"         */\n"+
		"        public void foo1() {\n"+
		"        }\n"+
		"        @Deprecated\n"+
		"        public void foo2() {\n"+
		"        }\n"+
		"        {\n"+
		"               foo\n"+
		"               Thread.\n"+
		"        }\n"+
		"}");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(false, true, false, false, true, false, false, true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"foo1[METHOD_REF]{foo1(), Ltest.Test;, ()V, foo1, null, public deprecated, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"foo2[METHOD_REF]{foo2(), Ltest.Test;, ()V, foo2, null, public deprecated, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114116
public void test0376() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"        MyCollection<String> \n"+
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/MyCollection.java",
		"package test;\n"+
		"public abstract class MyCollection<T> implements java.util.Collection<T> {\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "MyCollection<String> ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"collection[VARIABLE_DECLARATION]{collection, null, Ltest.MyCollection<Ljava.lang.String;>;, collection, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"myCollection[VARIABLE_DECLARATION]{myCollection, null, Ltest.MyCollection<Ljava.lang.String;>;, myCollection, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"strings[VARIABLE_DECLARATION]{strings, null, Ltest.MyCollection<Ljava.lang.String;>;, strings, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114116
public void test0377() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"        MyCollection \n"+
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/MyCollection.java",
		"package test;\n"+
		"public abstract class MyCollection<T> implements java.util.Collection<T> {\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "MyCollection ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"collection[VARIABLE_DECLARATION]{collection, null, Ltest.MyCollection;, collection, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"myCollection[VARIABLE_DECLARATION]{myCollection, null, Ltest.MyCollection;, myCollection, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=114116
public void test0378() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];

	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;\n"+
		"public class Test {\n"+
		"        MyCollection<String, String> \n"+
		"}");

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/test/MyCollection.java",
		"package test;\n"+
		"public abstract class MyCollection<T, U> implements java.util.Collection<T> {\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "MyCollection<String, String> ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"collection[VARIABLE_DECLARATION]{collection, null, Ltest.MyCollection<Ljava.lang.String;Ljava.lang.String;>;, collection, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}\n" +
			"myCollection[VARIABLE_DECLARATION]{myCollection, null, Ltest.MyCollection<Ljava.lang.String;Ljava.lang.String;>;, myCollection, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=153130
public void testEC001() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/Test.java",
		"package test;"+
		"public class Test<T> {\n"+
		"}");

	String start = "new test.Test<";
	IJavaProject javaProject = getJavaProject("Completion");
	IEvaluationContext context = javaProject.newEvaluationContext();

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false);
	context.codeComplete(start, start.length(), requestor, this.wcOwner);

	int startOffset = start.length();
	int endOffset = startOffset;

	assertResults(
			"completion offset="+startOffset+"\n"+
			"completion range=["+startOffset+", "+(endOffset-1)+"]\n"+
			"completion token=\"\"\n"+
			"completion token kind=TOKEN_KIND_NAME\n"+
			"expectedTypesSignatures={Ljava.lang.Object;}\n"+
			"expectedTypesKeys={Ljava/lang/Object;}\n"+
			"completion token location=UNKNOWN",
            requestor.getContext());

	assertResults(
			"Test<T>[TYPE_REF]{, test, Ltest.Test<TT;>;, null, null, ["+startOffset+", "+endOffset+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports001() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo;\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.*"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED;
	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	int start2 = str.lastIndexOf("public class");
	int end2 = start2 + "".length();
	assertResults(
			"foo[FIELD_REF]{foo, Ltest.p.ZZZ;, I, foo, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
			"   foo[FIELD_IMPORT]{import static test.p.ZZZ.foo;\n, Ltest.p.ZZZ;, I, foo, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports002() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.*"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED;
	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	int start2 = str.lastIndexOf("public class");
	int end2 = start2 + "".length();
	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, ()I, foo, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
			"   foo[METHOD_IMPORT]{import static test.p.ZZZ.foo;\n, Ltest.p.ZZZ;, ()I, foo, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports003() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo;\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports004() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports005() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo;\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.foo"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED;
	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	int start2 = str.lastIndexOf("public class");
	int end2 = start2 + "".length();
	assertResults(
			"foo[FIELD_REF]{foo, Ltest.p.ZZZ;, I, foo, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
			"   foo[FIELD_IMPORT]{import static test.p.ZZZ.foo;\n, Ltest.p.ZZZ;, I, foo, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports006() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.foo"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED;
	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	int start2 = str.lastIndexOf("public class");
	int end2 = start2 + "".length();
	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, ()I, foo, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
			"   foo[METHOD_IMPORT]{import static test.p.ZZZ.foo;\n, Ltest.p.ZZZ;, ()I, foo, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports007() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import test.p.ZZZ.*;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.*"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED;
	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	int start2 = str.lastIndexOf("public class");
	int end2 = start2 + "".length();
	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, ()I, foo, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
			"   foo[METHOD_IMPORT]{import static test.p.ZZZ.foo;\n, Ltest.p.ZZZ;, ()I, foo, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports008() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import static test.p.ZZZ.*;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.*"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, ()I, foo, null, ["+start1+", "+end1+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED +R_NON_RESTRICTED)+"}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports009() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import test.p.ZZZ.*;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.foo"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED;
	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	int start2 = str.lastIndexOf("public class");
	int end2 = start2 + "".length();
	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, ()I, foo, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
			"   foo[METHOD_IMPORT]{import static test.p.ZZZ.foo;\n, Ltest.p.ZZZ;, ()I, foo, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports010() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import static test.p.ZZZ.*;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.foo"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, ()I, foo, null, ["+start1+", "+end1+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED +R_NON_RESTRICTED)+"}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports011() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import test.p.ZZZ.foo;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.*"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED;
	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	int start2 = str.lastIndexOf("public class");
	int end2 = start2 + "".length();
	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, ()I, foo, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
			"   foo[METHOD_IMPORT]{import static test.p.ZZZ.foo;\n, Ltest.p.ZZZ;, ()I, foo, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports012() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import static test.p.ZZZ.foo;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.*"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, ()I, foo, null, ["+start1+", "+end1+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED +R_NON_RESTRICTED)+"}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports013() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import test.p.ZZZ.foo;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.foo"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED;
	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	int start2 = str.lastIndexOf("public class");
	int end2 = start2 + "".length();
	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, ()I, foo, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
			"   foo[METHOD_IMPORT]{import static test.p.ZZZ.foo;\n, Ltest.p.ZZZ;, ()I, foo, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports014() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import static test.p.ZZZ.foo;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.foo"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, ()I, foo, null, ["+start1+", "+end1+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED +R_NON_RESTRICTED)+"}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports015() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import static test.p.ZZZ.foo;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){}\n" +
			"}");

	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/test/q/ZZZ2.java",
			"package test.q;\n" +
			"public class ZZZ2 {\n" +
			"    public static int foo(){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.q.ZZZ2.foo"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, ()I, foo, null, ["+start1+", "+end1+"], " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports016() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public class foo {\n" +
			"        public void method() {\n" +
			"            foo\n" +
			"        }\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.*"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED;
	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	int start2 = str.lastIndexOf("public class Test");
	int end2 = start2 + "".length();
	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, ()I, foo, null, ["+start1+", "+end1+"], " + (relevance1) + "}\n" +
			"   foo[METHOD_IMPORT]{import static test.p.ZZZ.foo;\n, Ltest.p.ZZZ;, ()I, foo, null, ["+start2+", "+end2+"], " + (relevance1) + "}\n" +
			"Test.foo[TYPE_REF]{foo, test, Ltest.Test$foo;, null, null, ["+start1+", "+end1+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports017() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void foo() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.*"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.Test;, ()V, foo, null, ["+start1+", "+end1+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports018() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public int foo;\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.*"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED;
	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	int start2 = str.lastIndexOf("public class");
	int end2 = start2 + "".length();
	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, ()I, foo, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
			"   foo[METHOD_IMPORT]{import static test.p.ZZZ.foo;\n, Ltest.p.ZZZ;, ()I, foo, null, ["+start2+", "+end2+"], " + (relevance1) + "}\n"+
			"foo[FIELD_REF]{foo, Ltest.Test;, I, foo, null, ["+start1+", "+end1+"], " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports019() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        int foo = 0;\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.*"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED;
	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	int start2 = str.lastIndexOf("public class");
	int end2 = start2 + "".length();
	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, ()I, foo, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
			"   foo[METHOD_IMPORT]{import static test.p.ZZZ.foo;\n, Ltest.p.ZZZ;, ()I, foo, null, ["+start2+", "+end2+"], " + (relevance1) + "}\n"+
			"foo[LOCAL_VARIABLE_REF]{foo, null, I, foo, null, ["+start1+", "+end1+"], "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_UNQUALIFIED + R_NON_RESTRICTED)+"}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports020() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){}\n" +
			"    public static int foo(int i){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.foo"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED;
	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	int start2 = str.lastIndexOf("public class");
	int end2 = start2 + "".length();
	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, ()I, foo, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
			"   foo[METHOD_IMPORT]{import static test.p.ZZZ.foo;\n, Ltest.p.ZZZ;, ()I, foo, null, ["+start2+", "+end2+"], " + (relevance1) + "}\n"+
			"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, (I)I, foo, (i), ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
			"   foo[METHOD_IMPORT]{import static test.p.ZZZ.foo;\n, Ltest.p.ZZZ;, (I)I, foo, (i), ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports021() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        <Object>foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static <T> int foo(){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.foo"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED;
	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	int start2 = str.lastIndexOf("public class");
	int end2 = start2 + "".length();
	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, <T:Ljava.lang.Object;>()I, foo, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
			"   foo[METHOD_IMPORT]{import static test.p.ZZZ.foo;\n, Ltest.p.ZZZ;, <T:Ljava.lang.Object;>()I, foo, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports022() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo();\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){}\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.foo"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports023() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"/** */\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo;\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.*"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED;
	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	int start2 = str.lastIndexOf("/** */");
	int end2 = start2 + "".length();
	assertResults(
			"foo[FIELD_REF]{foo, Ltest.p.ZZZ;, I, foo, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
			"   foo[FIELD_IMPORT]{import static test.p.ZZZ.foo;\n, Ltest.p.ZZZ;, I, foo, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports024() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public int foo;\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.*"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports025() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public int foo;\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.foo"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports026() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public int foo(){return 0;};\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.*"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports027() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public int foo(){return 0;};\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.foo"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports029() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"import test.p.ZZZ;\n" +
			"public class Test {\n" +
			"    public void method() {\n" +
			"        foo\n" +
			"    }\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/p/ZZZ.java",
			"package test.p;\n" +
			"public class ZZZ {\n" +
			"    public static int foo(){return 0;};\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
	requestor.allowAllRequiredProposals();
	requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.foo"});

	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED;
	int start1 = str.lastIndexOf("foo") + "".length();
	int end1 = start1 + "foo".length();
	int start2 = str.lastIndexOf("public class");
	int end2 = start2 + "".length();
	assertResults(
			"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, ()I, foo, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
			"   foo[METHOD_IMPORT]{import static test.p.ZZZ.foo;\n, Ltest.p.ZZZ;, ()I, foo, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports030() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_SUGGEST_STATIC_IMPORTS, JavaCore.DISABLED);
		JavaCore.setOptions(options);

		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src3/test/Test.java",
				"package test;\n" +
				"public class Test {\n" +
				"    public void method() {\n" +
				"        foo\n" +
				"    }\n" +
				"}");

		this.workingCopies[1] = getWorkingCopy(
				"/Completion/src3/test/p/ZZZ.java",
				"package test.p;\n" +
				"public class ZZZ {\n" +
				"    public static int foo(){}\n" +
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
		requestor.allowAllRequiredProposals();
		requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.*"});

		String str = this.workingCopies[0].getSource();
		String completeBehind = "foo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED;
		int start1 = str.lastIndexOf("foo") + "".length();
		int end1 = start1 + "foo".length();
		int start2 = str.lastIndexOf("public class");
		int end2 = start2 + "".length();
		assertResults(
				"foo[METHOD_REF]{ZZZ.foo(), Ltest.p.ZZZ;, ()I, foo, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
				"   ZZZ[TYPE_IMPORT]{import test.p.ZZZ;\n, test.p, Ltest.p.ZZZ;, null, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152123
public void testFavoriteImports031() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_SUGGEST_STATIC_IMPORTS, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src3/test/Test.java",
				"package test;\n" +
				"public class Test {\n" +
				"    public void method() {\n" +
				"        foo\n" +
				"    }\n" +
				"}");

		this.workingCopies[1] = getWorkingCopy(
				"/Completion/src3/test/p/ZZZ.java",
				"package test.p;\n" +
				"public class ZZZ {\n" +
				"    public static int foo(){}\n" +
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
		requestor.allowAllRequiredProposals();
		requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.*"});

		String str = this.workingCopies[0].getSource();
		String completeBehind = "foo";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_NAME + R_NON_RESTRICTED;
		int start1 = str.lastIndexOf("foo") + "".length();
		int end1 = start1 + "foo".length();
		int start2 = str.lastIndexOf("public class");
		int end2 = start2 + "".length();
		assertResults(
				"foo[METHOD_REF]{foo(), Ltest.p.ZZZ;, ()I, foo, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
				"   foo[METHOD_IMPORT]{import static test.p.ZZZ.foo;\n, Ltest.p.ZZZ;, ()I, foo, null, ["+start2+", "+end2+"], " + (relevance1) + "}",
				requestor.getResults());
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=178982
public void testFavoriteImports032() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();

	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_SUGGEST_STATIC_IMPORTS, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src3/test/Test.java",
				"package test;\n" +
				"public class Test extends Test2 {\n" +
				"    public void method() {\n" +
				"        int zelement1;\n" +
				"        float zelement2;\n" +
				"        double zelement3;\n" +
				"        foo(0, zelement);\n" +
				"    }\n" +
				"}");

		this.workingCopies[1] = getWorkingCopy(
				"/Completion/src3/test/Test2.java",
				"package test;\n" +
				"public class Test2 {\n" +
				"    public void foo(double i, double j) {}\n" +
				"    public void foo(float i, float j) {}\n" +
				"    public void foo(int i, int j) {}\n" +
				"}");

		this.workingCopies[2] = getWorkingCopy(
				"/Completion/src3/test/p/ZZZ.java",
				"package test.p;\n" +
				"public class ZZZ {\n" +
				"    public static int zelement4(){}\n" +
				"    public static float zelement5(){}\n" +
				"    public static double zelement6(){}\n" +
				"}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, false, true);
		requestor.allowAllRequiredProposals();
		requestor.setFavoriteReferences(new String[]{"test.p.ZZZ.*"});

		String str = this.workingCopies[0].getSource();
		String completeBehind = "zelement";
		int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
		this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

		int relevance1 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED;
		int relevance2 = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXACT_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED;
		int start1 = str.lastIndexOf("zelement") + "".length();
		int end1 = start1 + "zelement".length();
		int start2 = str.lastIndexOf("public class");
		int end2 = start2 + "".length();
		assertResults(
			"zelement4[METHOD_REF]{zelement4(), Ltest.p.ZZZ;, ()I, zelement4, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
			"   zelement4[METHOD_IMPORT]{import static test.p.ZZZ.zelement4;\n, Ltest.p.ZZZ;, ()I, zelement4, null, ["+start2+", "+end2+"], " + (relevance1) + "}\n"+
			"zelement5[METHOD_REF]{zelement5(), Ltest.p.ZZZ;, ()F, zelement5, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
			"   zelement5[METHOD_IMPORT]{import static test.p.ZZZ.zelement5;\n, Ltest.p.ZZZ;, ()F, zelement5, null, ["+start2+", "+end2+"], " + (relevance1) + "}\n"+
			"zelement6[METHOD_REF]{zelement6(), Ltest.p.ZZZ;, ()D, zelement6, null, ["+start1+", "+end1+"], "+(relevance1)+"}\n" +
			"   zelement6[METHOD_IMPORT]{import static test.p.ZZZ.zelement6;\n, Ltest.p.ZZZ;, ()D, zelement6, null, ["+start2+", "+end2+"], " + (relevance1) + "}\n"+
			"zelement1[LOCAL_VARIABLE_REF]{zelement1, null, I, zelement1, null, ["+start1+", "+end1+"], "+(relevance2)+"}\n" +
			"zelement2[LOCAL_VARIABLE_REF]{zelement2, null, F, zelement2, null, ["+start1+", "+end1+"], "+(relevance2)+"}\n" +
			"zelement3[LOCAL_VARIABLE_REF]{zelement3, null, D, zelement3, null, ["+start1+", "+end1+"], "+(relevance2)+"}",
			requestor.getResults());
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=162865
public void testNameWithUnresolvedReferences001() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"   void foo() {\n" +
 			"      {\n" +
 			"         int varzz1 = 0;\n" +
			"         varzz1 = varzz2;\n" +
			"         {\n" +
			"            int varzz3 = 0;\n" +
			"            varzz3 = varzz4;\n" +
			"            int varzz5 = 0;\n" +
			"         }\n" +
			"         {\n" +
			"            varzz1 = varzz5;\n" +
			"         }\n" +
			"         int varzz6 = varzz7;\n" +
			"         @MyAnnot1(/**/varzz\n" +
 			"      }\n" +

			"   }\n" +
			"}\n");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/MyAnnot1.java",
			"package test;\n" +
			"public @interface MyAnnot1 {\n" +
			"   String value();\n" +
			"   }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "/**/varzz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"varzz2[LOCAL_VARIABLE_REF]{varzz2, null, Ljava.lang.Object;, varzz2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"varzz4[LOCAL_VARIABLE_REF]{varzz4, null, Ljava.lang.Object;, varzz4, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"varzz5[LOCAL_VARIABLE_REF]{varzz5, null, Ljava.lang.Object;, varzz5, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"varzz7[LOCAL_VARIABLE_REF]{varzz7, null, Ljava.lang.Object;, varzz7, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"varzz1[LOCAL_VARIABLE_REF]{varzz1, null, I, varzz1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"varzz6[LOCAL_VARIABLE_REF]{varzz6, null, I, varzz6, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=162865
public void testNameWithUnresolvedReferences002() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"   void foo() {\n" +
 			"      {\n" +
 			"         int varzz1 = 0;\n" +
			"         varzz1 = varzz2;\n" +
			"         {\n" +
			"            int varzz3 = 0;\n" +
			"            varzz3 = varzz4;\n" +
			"            int varzz5 = 0;\n" +
			"         }\n" +
			"         {\n" +
			"            varzz1 = varzz5;\n" +
			"         }\n" +
			"         int varzz6 = varzz7;\n" +
			"         @MyAnnot1(value=/**/varzz\n" +
 			"      }\n" +

			"   }\n" +
			"}\n");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/MyAnnot1.java",
			"package test;\n" +
			"public @interface MyAnnot1 {\n" +
			"   String value();\n" +
			"   }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "/**/varzz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"varzz2[LOCAL_VARIABLE_REF]{varzz2, null, Ljava.lang.Object;, varzz2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"varzz4[LOCAL_VARIABLE_REF]{varzz4, null, Ljava.lang.Object;, varzz4, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"varzz5[LOCAL_VARIABLE_REF]{varzz5, null, Ljava.lang.Object;, varzz5, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"varzz7[LOCAL_VARIABLE_REF]{varzz7, null, Ljava.lang.Object;, varzz7, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"varzz1[LOCAL_VARIABLE_REF]{varzz1, null, I, varzz1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"varzz6[LOCAL_VARIABLE_REF]{varzz6, null, I, varzz6, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=162865
public void testNameWithUnresolvedReferences003() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class Test {\n" +
			"   void foo() {\n" +
 			"      {\n" +
 			"         int varzz1 = 0;\n" +
			"         varzz1 = varzz2;\n" +
			"         {\n" +
			"            int varzz3 = 0;\n" +
			"            varzz3 = varzz4;\n" +
			"            int varzz5 = 0;\n" +
			"         }\n" +
			"         {\n" +
			"            varzz1 = varzz5;\n" +
			"         }\n" +
			"         new Object() {\n" +
			"            int varzz6 = varzz7;\n" +
			"            @MyAnnot1(/**/varzz\n" +
			"         };\n" +
 			"      }\n" +

			"   }\n" +
			"}\n");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/test/MyAnnot1.java",
			"package test;\n" +
			"public @interface MyAnnot1 {\n" +
			"   String value();\n" +
			"   }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "/**/varzz";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"varzz2[LOCAL_VARIABLE_REF]{varzz2, null, Ljava.lang.Object;, varzz2, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"varzz4[LOCAL_VARIABLE_REF]{varzz4, null, Ljava.lang.Object;, varzz4, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"varzz5[LOCAL_VARIABLE_REF]{varzz5, null, Ljava.lang.Object;, varzz5, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"varzz7[LOCAL_VARIABLE_REF]{varzz7, null, Ljava.lang.Object;, varzz7, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"varzz1[LOCAL_VARIABLE_REF]{varzz1, null, I, varzz1, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=99399 test to verify that we continue to propose final
// types in extends contexts but where they are not directly extended.
public void testCompletionOnExtends() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"class X<T> {};\n" +
			"final class ThisClassIsFinal {}\n" +
			"class ThisClassIsNotFinal {}\n" +
			"public class Test extends X<ThisClassI> {}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "ThisClassI";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ThisClassIsFinal[TYPE_REF]{ThisClassIsFinal, test, Ltest.ThisClassIsFinal;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"ThisClassIsNotFinal[TYPE_REF]{ThisClassIsNotFinal, test, Ltest.ThisClassIsNotFinal;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=99399 test to verify that we don't propose final
//types in extends contexts where we should not.
public void testCompletionOnExtends2() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"final class ThisClassIsFinal {}\n" +
			"class ThisClassIsNotFinal {}\n" +
			"public class Test <T extends ThisClassI> {}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "ThisClassI";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ThisClassIsNotFinal[TYPE_REF]{ThisClassIsNotFinal, test, Ltest.ThisClassIsNotFinal;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=99399 test to verify that we don't propose final
//types in extends contexts where we should not.
public void testCompletionOnExtends3() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"final class ThisClassIsFinal {}\n" +
			"class ThisClassIsNotFinal {}\n" +
			"public class Test {\n" +
			"    Test(Bag<? extends ThisClassI> p) {}" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "ThisClassI";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ThisClassIsNotFinal[TYPE_REF]{ThisClassIsNotFinal, test, Ltest.ThisClassIsNotFinal;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}

//https://bugs.eclipse.org/bugs/show_bug.cgi?id=99399 test to verify that we do propose final
//types in super contexts where we should.
public void testCompletionOnExtends4() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"final class ThisClassIsFinal {}\n" +
			"class ThisClassIsNotFinal {}\n" +
			"public class Test {\n" +
			"    void boo() {\n" +
			"        Bag<? super ThisClassI> local;\n" +
			"    }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "ThisClassI";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"ThisClassIsFinal[TYPE_REF]{ThisClassIsFinal, test, Ltest.ThisClassIsFinal;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"ThisClassIsNotFinal[TYPE_REF]{ThisClassIsNotFinal, test, Ltest.ThisClassIsNotFinal;, null, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=246832
 * To test whether camel case completion works for imported static methods
 */
public void testCamelCaseStaticMethodImport() throws JavaModelException {
	this.oldOptions = JavaCore.getOptions();
	this.workingCopies = new ICompilationUnit[2];
	try {
		Hashtable options = new Hashtable(this.oldOptions);
		options.put(JavaCore.CODEASSIST_CAMEL_CASE_MATCH, JavaCore.ENABLED);
		JavaCore.setOptions(options);

		this.workingCopies[0] = getWorkingCopy(
				"/Completion/src/a/A.java",
				"package a;\n" +
				"public class A{\n" +
				"public static void testMethodWithLongName(){}\n" +
				"public static void testMethodWithLongName2(){}\n" +
				"}}");

		this.workingCopies[1] = getWorkingCopy(
				"/Completion/src/b/B.java",
				"import static a.A.testMethodWithLongName;\n" +
				"public class B {\n" +
				"public void b() {\n" +
				"tMWLN \n" +
				"}}");

		CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(
				true);
		String str = this.workingCopies[1].getSource();
		String completeBehind = "tMWLN";
		int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
		this.workingCopies[1].codeComplete(cursorLocation, requestor, this.wcOwner);

			assertResults(
					"testMethodWithLongName[METHOD_REF]{testMethodWithLongName(), La.A;, ()V, testMethodWithLongName, null, " +
					(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CAMEL_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
					requestor.getResults());
	} finally {
		JavaCore.setOptions(this.oldOptions);
	}
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=84720
 * to test that the methods with Boxed/unboxed return types get higher relevance than the ones that return void
 */
public void testCompletionWithUnboxing() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class C {\n" +
			"public void myMethod1(){}\n" +
			"public void myMethod2(){}\n" +
			"public int myMethod3(){return 0;}\n" +
			"public Integer myMethod4(){return 0;}\n" +
			"public void foo() {\n" +
			"	int i = myMeth \n" +
			"}\n" +
			"}");
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/java/lang/Test.java",
			"package java.lang;\n" +
			"public class Integer {\n" +
			"}");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "= myMeth";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"myMethod1[METHOD_REF]{myMethod1(), Ltest.C;, ()V, myMethod1, null, " +
				(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_VOID + R_NON_RESTRICTED) + "}\n" +
			"myMethod2[METHOD_REF]{myMethod2(), Ltest.C;, ()V, myMethod2, null, " +
				(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_VOID + R_NON_RESTRICTED) + "}\n" +
			"myMethod4[METHOD_REF]{myMethod4(), Ltest.C;, ()Ljava.lang.Integer;, myMethod4, null, " +
				(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"myMethod3[METHOD_REF]{myMethod3(), Ltest.C;, ()I, myMethod3, null, " +
				(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=84720
 * Additional tests for bug 84720
 *
 * @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=273991 also
 */
public void testCompletionWithUnboxing_1() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class C {\n" +
			"public void myMethod1(){}\n" +
			"public long myMethod2(){return 0;}\n" +
			"public Long myMethod3(){return 0;}\n" +
			"public float myMethod4(){return 0;}\n" +
			"public Float myMethod5(){return 0;}\n" +
			"public void foo() {\n" +
			"	Long l = myMeth \n" +
			"}\n" +
			"}");
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/java/lang/Long.java",
			"package java.lang;\n" +
			"public class Long {\n" +
			"}");
	this.workingCopies[2] = getWorkingCopy(
			"/Completion/src3/java/lang/Float.java",
			"package java.lang;\n" +
			"public class Float {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "= myMeth";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"myMethod1[METHOD_REF]{myMethod1(), Ltest.C;, ()V, myMethod1, null, " +
				(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_VOID + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"myMethod4[METHOD_REF]{myMethod4(), Ltest.C;, ()F, myMethod4, null, " +
				(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"myMethod5[METHOD_REF]{myMethod5(), Ltest.C;, ()Ljava.lang.Float;, myMethod5, null, " +
			(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"myMethod2[METHOD_REF]{myMethod2(), Ltest.C;, ()J, myMethod2, null, " +
				(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"myMethod3[METHOD_REF]{myMethod3(), Ltest.C;, ()Ljava.lang.Long;, myMethod3, null, " +
				(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}" ,
			requestor.getResults());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=84720
 * Additional tests for bug 84720
 */
public void testCompletionWithUnboxing_2() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src3/test/Test.java",
			"package test;\n" +
			"public class C {\n" +
			"int myVariable1 = 0;\n" +
			"long myVariable2 = 0;\n" +
			"boolean myVariable3 = false;\n" +
			"Boolean myVariable4 = false;\n" +
			"public void foo() {\n" +
			"	if(myVar \n" +
			"}\n" +
			"}");

	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/java/lang/Test.java",
			"package java.lang;\n" +
			"public class Boolean {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);

	String str = this.workingCopies[0].getSource();
	String completeBehind = "if(myVar";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"myVariable1[FIELD_REF]{myVariable1, Ltest.C;, I, myVariable1, null, " +
				(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"myVariable2[FIELD_REF]{myVariable2, Ltest.C;, J, myVariable2, null, " +
				(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"myVariable4[FIELD_REF]{myVariable4, Ltest.C;, Ljava.lang.Boolean;, myVariable4, null, " +
				(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXPECTED_TYPE + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"myVariable3[FIELD_REF]{myVariable3, Ltest.C;, Z, myVariable3, null, " +
				(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_EXPECTED_TYPE + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=274466
//Check for boolean methods with higher relevance in assert statement's conditional part
public void test274466() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Test274466.java",
			"package test;" +
			"public class Test274466 {\n" +
			"	boolean methodReturningBoolean() { return true; }\n" +
			"	Boolean methodReturningBooleanB() { return true; }\n" +
			"   void methodReturningBlah() { return; }\n" +
			"	int foo(int p) {\n" +
			"     assert methodR : \"Exception Message\";" +
			"	}\n" +
			"}\n");
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src3/java/lang/Test.java",
			"package java.lang;\n" +
			"public class Boolean {\n" +
			"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "methodR";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();

	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"methodReturningBlah[METHOD_REF]{methodReturningBlah(), Ltest.Test274466;, ()V, methodReturningBlah, " +
					(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_VOID + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
			"methodReturningBooleanB[METHOD_REF]{methodReturningBooleanB(), Ltest.Test274466;, ()Ljava.lang.Boolean;, methodReturningBooleanB, " +
					(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXPECTED_TYPE + R_NON_RESTRICTED) + "}\n" +
			"methodReturningBoolean[METHOD_REF]{methodReturningBoolean(), Ltest.Test274466;, ()Z, methodReturningBoolean, " +
					(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_EXACT_EXPECTED_TYPE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=307486
public void testLabel() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/label/Test.java",
		"package label;"+
		"public class Test {\n" +
		"  void foo() {\n" +
		"    \ud842\udf9fabc :\n" +
		"    while (true) {\n" +
		"        break \ud842\udf9fabc;\n" +
		"    }\n" +
		"  }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "break";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length() + 1;
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"\ud842\udf9fabc[LABEL_REF]{\ud842\udf9fabc, null, null, \ud842\udf9fabc, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) + "}",
			requestor.getResults());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=310423
// Annotation types are not proposed after 'implements' in a Single type ref
public void testBug310423a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/label/Test.java",
		"import java.lang.annotation.Annotation;\n" +
		"interface In {}\n" +
		"interface Inn {\n" +
		"	interface Inn2 {}\n" +
		"	@interface IAnnot {}\n" +
		"}\n" +
		"@interface InnAnnot {}\n"+
		"public class Test implements {\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "implements";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length() + 1;
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Inn.Inn2[TYPE_REF]{label.Inn.Inn2, label, Llabel.Inn$Inn2;, null, null, " + (R_DEFAULT + 39) + "}\n" +
			"In[TYPE_REF]{In, label, Llabel.In;, null, null, " + (R_DEFAULT + 42) + "}\n" +
			"Inn[TYPE_REF]{Inn, label, Llabel.Inn;, null, null, " + (R_DEFAULT + 42) + "}",
			requestor.getResults());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=310423
// Annotation types are not proposed after 'implements' in a Qualified type ref
public void testBug310423b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/label/Test.java",
		"interface In{}\n" +
		"interface Inn{\n" +
		"	interface Inn2{}\n" +
		"	interface Inn3{}\n" +
		"	@interface IAnnot {}\n" +
		"}"+
		"public class Test implements Inn. {\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "Inn.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length() + 1;
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Inn.Inn2[TYPE_REF]{Inn2, label, Llabel.Inn$Inn2;, null, null, " + (R_DEFAULT + 39) + "}\n" +
			"Inn.Inn3[TYPE_REF]{Inn3, label, Llabel.Inn$Inn3;, null, null, " + (R_DEFAULT + 39) + "}",
			requestor.getResults());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=343865
// Verify that no NPE is thrown and we get correct proposals
public void testBug343865a() throws JavaModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "testxxx", "TestType.java");

	String str = cu.getSource();
	String completeBehind = "@YAAnnot(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertResults(
		"name[ANNOTATION_ATTRIBUTE_REF]{name = , Ltestxxx.YAAnnot;, Ljava.lang.String;, name, null, " +
						(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
		"val[ANNOTATION_ATTRIBUTE_REF]{val = , Ltestxxx.YAAnnot;, I, val, null, " +
						(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}",
		requestor.getResults());
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=343865
// Verify that the correct expected type is computed
public void testBug343865b() throws JavaModelException {
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	ICompilationUnit cu = getCompilationUnit("Completion", "src3", "testxxx", "TestType2.java");

	String str = cu.getSource();
	String completeBehind = "String xxyy2 = xxy";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	cu.codeComplete(cursorLocation, requestor);

	assertResults(
			"expectedTypesSignatures={Ljava.lang.String;}\n" +
			"expectedTypesKeys={Ljava/lang/String;}",
			requestor.getContext());

	assertResults(
		"xxyy[FIELD_REF]{xxyy, Ltestxxx.TestType2;, I, xxyy, null, " +
				(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED) + "}\n" +
		"xxyy1[FIELD_REF]{xxyy1, Ltestxxx.TestType2;, Ljava.lang.String;, xxyy1, null, " +
				(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE) + "}",
		requestor.getResults());
}
public void testBug351426() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/X.java",
		"package test;\n" +
		"public class X<T> {\n" +
		"	public void foo() {\n" +
		"		X<String> x = new X<>();" +
		"   }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new X<";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);


	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_NAME + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE;

	assertResults(
			"expectedTypesSignatures={Ltest.X<Ljava.lang.String;>;}\n" +
			"expectedTypesKeys={Ltest/X<Ljava/lang/String;>;}",
			requestor.getContext());
	assertResults(
			"X<T>[TYPE_REF]{, test, Ltest.X<TT;>;, null, null, replace[77, 77], token[77, 77], " + relevance + "}",
			requestor.getResults());
}
public void testBug351426b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/X.java",
		"package test;\n" +
		"public class X<T> {\n" +
		"	X(T t){}\n" +
		"	public void foo() {\n" +
		"		X<String> x = new X<>(\"\");" +
		"   }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new X<";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_NAME + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE;

	assertResults(
			"expectedTypesSignatures={Ltest.X<Ljava.lang.String;>;}\n" +
			"expectedTypesKeys={Ltest/X<Ljava/lang/String;>;}",
			requestor.getContext());
	assertResults(
			"X<T>[TYPE_REF]{, test, Ltest.X<TT;>;, null, null, replace[87, 87], token[87, 87], " + relevance + "}",
			requestor.getResults());
}
// qualified allocation
public void testBug351426c() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/X.java",
		"package test;\n" +
		"public class X<T> {\n" +
		"	X(T t){}\n" +
		"	class X1<E> {}\n" +
		"	public void foo() {\n" +
		"		X<String>.X1<String> x = new X<String>(\"\").new X1<>();" +
		"   }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new X1<";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_NAME + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE;

	assertResults(
			"expectedTypesSignatures={Ltest.X<Ljava.lang.String;>.X1<Ljava.lang.String;>;}\n" +
			"expectedTypesKeys={Ltest/X<Ljava/lang/String;>.X1<Ljava/lang/String;>;}",
			requestor.getContext());
	assertResults(
			"X<java.lang.String>.X1[TYPE_REF]{, test, Ltest.X<Ljava.lang.String;>.X1;, null, null, replace[133, 133], token[133, 133], " + relevance + "}",
			requestor.getResults());
}
// qualified allocation
public void testBug351426d() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/X.java",
		"package test;\n" +
		"public class X<T> {\n" +
		"	X(T t){}\n" +
		"	class X1<E> {\n" +
		"		class X11<F>{}\n" +
		"	}\n" +
		"	public void foo() {\n" +
		"		X<String>.X1<Object>.X11<String> x = new X<String>(\"\").new X1<Object>().new X11<>();" +
		"   }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new X11<";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_NAME + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE;

	assertResults(
			"expectedTypesSignatures={Ltest.X<Ljava.lang.String;>.X1<Ljava.lang.Object;>.X11<Ljava.lang.String;>;}\n" +
			"expectedTypesKeys={Ltest/X<Ljava/lang/String;>.X1<Ljava/lang/Object;>.X11<Ljava/lang/String;>;}",
			requestor.getContext());
	assertResults(
			"X<java.lang.String>.X1<java.lang.Object>.X11[TYPE_REF]{, test, Ltest.X<Ljava.lang.String;>.X1<Ljava.lang.Object;>.X11;, null, null, replace[182, 182], token[182, 182], " + relevance + "}",
			requestor.getResults());
}
// qualified allocation
public void testBug351426e() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/X.java",
		"package test;\n" +
		"public class X<T> {\n" +
		"	X(T t){}\n" +
		"	static class X1<E> {\n" +
		"	}\n" +
		"	public static void foo() {\n" +
		"		X1<String> x = new X.X1<>();" +
		"   }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new X.X1<";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_NAME + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE;

	assertResults(
			"expectedTypesSignatures={Ltest.X$X1<Ljava.lang.String;>;}\n" +
			"expectedTypesKeys={Ltest/X$X1<Ljava/lang/String;>;}",
			requestor.getContext());
	assertResults(
			"X.X1<E>[TYPE_REF]{, test, Ltest.X$X1<TE;>;, null, null, replace[123, 123], token[123, 123], " + relevance + "}",
			requestor.getResults());
}
// returning allocated object
public void testBug351426f() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/X.java",
		"package test;\n" +
		"public class X<T> {\n" +
		"	X(T t){}\n" +
		"	public X<String> foo() {\n" +
		"		return new X<>(\"\");" +
		"   }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new X<";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_NAME + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE;

	assertResults(
			"expectedTypesSignatures={Ltest.X<Ljava.lang.String;>;}\n" +
			"expectedTypesKeys={Ltest/X<Ljava/lang/String;>;}",
			requestor.getContext());
	assertResults(
			"X<T>[TYPE_REF]{, test, Ltest.X<TT;>;, null, null, replace[85, 85], token[85, 85], " + relevance + "}",
			requestor.getResults());
}
// returning allocated object, qualified case
public void testBug351426g() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/X.java",
		"package test;\n" +
		"public class X<T> {\n" +
		"	X(T t){}\n" +
		"	class X1<E>{}\n" +
		"	public X1<String> foo() {\n" +
		"		return new X<String>(\"\").new X1<>();" +
		"   }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new X1<";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_NAME + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE;

	assertResults(
			"expectedTypesSignatures={Ltest.X<TT;>.X1<Ljava.lang.String;>;}\n" +
			"expectedTypesKeys={Ltest/X<Ltest/X;:TT;>.X1<Ljava/lang/String;>;}",
			requestor.getContext());
	assertResults(
			"X<java.lang.String>.X1[TYPE_REF]{, test, Ltest.X<Ljava.lang.String;>.X1;, null, null, replace[120, 120], token[120, 120], " + relevance + "}",
			requestor.getResults());
}
// returning allocated object, qualified case
public void testBug351426h() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/X.java",
		"package test;\n" +
		"public class X<T> {\n" +
		"	X(T t){}\n" +
		"	static class X1<E>{}\n" +
		"	public X.X1<String> foo() {\n" +
		"		return new X.X1<>();" +
		"   }\n" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new X.X1<";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_NAME + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE;

	assertResults(
			"expectedTypesSignatures={Ltest.X$X1<Ljava.lang.String;>;}\n" +
			"expectedTypesKeys={Ltest/X$X1<Ljava/lang/String;>;}",
			requestor.getContext());
	assertResults(
			"X.X1<E>[TYPE_REF]{, test, Ltest.X$X1<TE;>;, null, null, replace[113, 113], token[113, 113], " + relevance + "}",
			requestor.getResults());
}
// fields
public void testBug351426i() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/test/X.java",
		"package test;\n" +
		"public class X<T> {\n" +
		"	X<String> x = new X<>();" +
		"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new X<";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);


	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_NAME + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE;

	assertResults(
			"expectedTypesSignatures={Ltest.X<Ljava.lang.String;>;}\n" +
			"expectedTypesKeys={Ltest/X<Ljava/lang/String;>;}",
			requestor.getContext());
	assertResults(
			"X<T>[TYPE_REF]{, test, Ltest.X<TT;>;, null, null, replace[55, 55], token[55, 55], " + relevance + "}",
			requestor.getResults());
}
// fields, qualified allocation
public void testBug351426j() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/X.java",
			"package test;\n" +
			"public class X<T> {\n" +
			"	X(T t){}\n" +
			"	static class X1<E> {\n" +
			"	}\n" +
			"	public static void foo() {\n" +
			"		X1<String> x = new X.X1<>();" +
			"   }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new X.X1<";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_NAME + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE;

	assertResults(
			"expectedTypesSignatures={Ltest.X$X1<Ljava.lang.String;>;}\n" +
			"expectedTypesKeys={Ltest/X$X1<Ljava/lang/String;>;}",
			requestor.getContext());
	assertResults(
			"X.X1<E>[TYPE_REF]{, test, Ltest.X$X1<TE;>;, null, null, replace[123, 123], token[123, 123], " + relevance + "}",
			requestor.getResults());
}
// more than one type arg, completing on second arg
public void testBug351426k() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/X.java",
			"package test;\n" +
			"public class X<T,U> {\n" +
			"	X(T t){}\n" +
			"	public void foo() {\n" +
			"		X<String, String> x = new X<String, >(\"\");" +
			"   }\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new X<String, ";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_NAME + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE;

	assertResults(
			"expectedTypesSignatures={Ltest.X<Ljava.lang.String;Ljava.lang.String;>;}\n" +
			"expectedTypesKeys={Ltest/X<Ljava/lang/String;Ljava/lang/String;>;}",
			requestor.getContext());
	assertResults(
			"X<T,U>[TYPE_REF]{, test, Ltest.X<TT;TU;>;, null, null, replace[105, 105], token[105, 105], " + relevance + "}",
			requestor.getResults());
}
// different CU's
public void testBug351426l() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/X.java",
			"package test;\n" +
			"public class X {\n" +
			"	public void foo() {\n" +
			"		X1<String> x1 = new X1<>(\"\");" +
			"   }\n" +
			"}\n");
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/test/X1.java",
			"package test;\n" +
			"public class X1<T> {\n" +
			"	X1(T t){}\n" +
			"}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new X1<";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	int relevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_NAME + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED + R_EXACT_EXPECTED_TYPE;

	assertResults(
			"expectedTypesSignatures={Ltest.X1<Ljava.lang.String;>;}\n" +
			"expectedTypesKeys={Ltest/X1<Ljava/lang/String;>;}",
			requestor.getContext());
	assertResults(
			"X1<T>[TYPE_REF]{, test, Ltest.X1<TT;>;, null, null, replace[77, 77], token[77, 77], " + relevance + "}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=361963
public void test361963() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/X.java",
			"public class X<T> {\n" +
			"    void g() {\n" +
			"        return new X() {\n" +
			"            void g() {\n" +
			"                Object o = new X<\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, false, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new X<";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"X<T>[TYPE_REF]{, , LX<TT;>;, null, null, replace[116, 116], token[116, 116], " +
								(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_EXACT_NAME + R_CASE + R_EXPECTED_TYPE + R_UNQUALIFIED + R_NON_RESTRICTED)+ "}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=326610
public void testBug326610() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/package-info.java",
			"package test;\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "package test;";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"import[KEYWORD]{import, null, null, import, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=326610
public void testBug326610a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Try.java",
			"package test;\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "package test;";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"abstract[KEYWORD]{abstract, null, null, abstract, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}\n" +
			"class[KEYWORD]{class, null, null, class, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}\n" +
			"enum[KEYWORD]{enum, null, null, enum, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}\n" +
			"final[KEYWORD]{final, null, null, final, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}\n" +
			"import[KEYWORD]{import, null, null, import, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}\n" +
			"interface[KEYWORD]{interface, null, null, interface, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}\n" +
			"public[KEYWORD]{public, null, null, public, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=326610
public void testBug326610b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/package-info.java",
			"/*Complete here*/\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/*Complete here*/";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"package[KEYWORD]{package, null, null, package, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=326610
public void testBug326610c() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Try.java",
			"/*Complete here*/\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/*Complete here*/";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"abstract[KEYWORD]{abstract, null, null, abstract, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}\n" +
			"class[KEYWORD]{class, null, null, class, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}\n" +
			"enum[KEYWORD]{enum, null, null, enum, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}\n" +
			"final[KEYWORD]{final, null, null, final, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}\n" +
			"import[KEYWORD]{import, null, null, import, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}\n" +
			"interface[KEYWORD]{interface, null, null, interface, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}\n" +
			"package[KEYWORD]{package, null, null, package, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}\n" +
			"public[KEYWORD]{public, null, null, public, null, " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED) +"}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=326610
public void testBug326610d() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/package-info.java",
			"@Non\n" +
			"package test");
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/j/NonNull.java",
			"package j;\n" +
			"import static java.lang.annotation.ElementType.PACKAGE;\n" +
			"import java.lang.annotation.Target;\n" +
			"@Target({PACKAGE})\n" +
			"public @interface NonNull{}\n");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@Non";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"NonNull[TYPE_REF]{j.NonNull, j, Lj.NonNull;, null, null, " + (R_DEFAULT + R_INTERESTING + R_CASE + R_QUALIFIED + R_EXACT_NAME + R_NON_RESTRICTED + R_ANNOTATION) + "}",
			requestor.getResults());
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=326610
public void testBug326610e() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/package-info.java",
			"/*Complete here*/\n" +
			"package test;");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "/*Complete here*/";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults(
			"",
			requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=482775
public void test482775() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/MyEnum.java",
			"public enum MyEnum {\n" +
			"	/**\n" +
			"	 * @see #B\n" +
			"	 */\n" +
			"	ALPHA,\n" +
			"	/**\n" +
			"	 * @see #\n" +
			"	 */\n" +
			"	BETA,\n" +
			"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, false);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@see #B";
	int cursorLocation = str.indexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevanceEnum = RelevanceConstants.R_DEFAULT + RelevanceConstants.R_RESOLVED + RelevanceConstants.R_INTERESTING +
			RelevanceConstants.R_NON_RESTRICTED + RelevanceConstants.R_CASE;

	int relevanceObject = RelevanceConstants.R_DEFAULT + RelevanceConstants.R_RESOLVED + RelevanceConstants.R_INTERESTING +
				RelevanceConstants.R_NON_STATIC + RelevanceConstants.R_NON_RESTRICTED + RelevanceConstants.R_CASE;

	assertResults("BETA[FIELD_REF]{BETA, LMyEnum;, LMyEnum;, null, null, BETA, null, [36, 37], " +
				relevanceEnum + "}", requestor.getResults());
	requestor = new CompletionTestsRequestor2(true, true, true, false);
	completeBehind = "@see #";
	cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertResults("MyEnum[METHOD_REF<CONSTRUCTOR>]{MyEnum(), LMyEnum;, ()V, null, null, MyEnum, null, [66, 66], " + (R_DEFAULT + R_RESOLVED + R_INTERESTING + R_NON_RESTRICTED) + "}\n" +
					"ALPHA[FIELD_REF]{ALPHA, LMyEnum;, LMyEnum;, null, null, ALPHA, null, [66, 66], " + relevanceEnum +  "}\n" +
					"BETA[FIELD_REF]{BETA, LMyEnum;, LMyEnum;, null, null, BETA, null, [66, 66], " + relevanceEnum + "}\n" +
					"valueOf[METHOD_REF]{valueOf(String), LMyEnum;, (Ljava.lang.String;)LMyEnum;, null, null, valueOf, (arg0), [66, 66], " + relevanceEnum + "}\n" +
					"values[METHOD_REF]{values(), LMyEnum;, ()[LMyEnum;, null, null, values, null, [66, 66], " + relevanceEnum + "}\n" +
					"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, null, null, clone, null, [66, 66], " + relevanceObject + "}\n" +
					"equals[METHOD_REF]{equals(Object), Ljava.lang.Object;, (Ljava.lang.Object;)Z, null, null, equals, (obj), [66, 66], " + relevanceObject + "}\n" +
					"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, null, null, finalize, null, [66, 66], " + relevanceObject + "}\n" +
					"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, null, null, getClass, null, [66, 66], " + relevanceObject + "}\n" +
					"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, null, null, hashCode, null, [66, 66], " + relevanceObject + "}\n" +
					"name[METHOD_REF]{name(), Ljava.lang.Enum<LMyEnum;>;, ()Ljava.lang.String;, null, null, name, null, [66, 66], " + relevanceObject + "}\n" +
					"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, null, null, notify, null, [66, 66], " + relevanceObject + "}\n" +
					"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, null, null, notifyAll, null, [66, 66], " + relevanceObject + "}\n" +
					"ordinal[METHOD_REF]{ordinal(), Ljava.lang.Enum<LMyEnum;>;, ()I, null, null, ordinal, null, [66, 66], " + relevanceObject + "}\n" +
					"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, [66, 66], " + relevanceObject + "}\n" +
					"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, null, null, wait, null, [66, 66], " + relevanceObject + "}\n" +
					"wait[METHOD_REF]{wait(long), Ljava.lang.Object;, (J)V, null, null, wait, (millis), [66, 66], " + relevanceObject + "}\n" +
					"wait[METHOD_REF]{wait(long, int), Ljava.lang.Object;, (JI)V, null, null, wait, (millis, nanos), [66, 66], " + relevanceObject + "}", requestor.getResults());
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=525421
public void testBug525421() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
		"/Completion/src/Complete.java",
		"class List<T> {\n" +
		"}\n" +
		"\n" +
		"enum Flag {\n" +
		"	YES, NO;\n" +
		"}\n" +
		"\n" +
		"public class Complete {\n" +
		"	private <T> List<T> emptyList() {\n" +
		"		return null;\n" +
		"	}\n" +
		"\n" +
		"	static boolean f(List<String> generic, Flag e) {\n" +
		"		return generic != null && e == Flag.YES;\n" +
		"	}\n" +
		"\n" +
		"	void fails() {\n" +
		"		f(emptyList(), Y);\n" +
		"	}\n" +
		"}\n" +
		"");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	String str = this.workingCopies[0].getSource();
	String completeBehind = "emptyList(), Y";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"YES[FIELD_REF]{Flag.YES, LFlag;, LFlag;, YES, null, 104}",
			requestor.getResults());
}


public void testBug526590() throws JavaModelException {
	// test for abstract method in abstract class
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/testbug526590/Bug526590.java",
			"package testbug526590;\n" +
			"public abstract class Bug526590 {\n" +
			"  public abstract void foo(@QQAnnotation() String param);\n" +
			"}");
	//SuppressWarnings

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/testbug526590/QQAnnotation.java",
		"package testbug526590;"+
		"public @interface QQAnnotation {\n"+
		"String[] value();\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@QQAnnotation(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Bug526590[TYPE_REF]{Bug526590, testbug526590, Ltestbug526590.Bug526590;, null, null, 52}\n" +
			"value[ANNOTATION_ATTRIBUTE_REF]{value = , Ltestbug526590.QQAnnotation;, [Ljava.lang.String;, value, null, 52}\n" +
			"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, 82}",
			requestor.getResults());
}

public void testBug526590b() throws JavaModelException {
	// test for abstract method in interface
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/testbug526590/Bug526590.java",
			"package testbug526590;\n" +
			"public interface Bug526590 {\n" +
			"  void foo(@QQAnnotation() String param);\n" +
			"}");
	//SuppressWarnings

	this.workingCopies[1] = getWorkingCopy(
		"/Completion/src/testbug526590/QQAnnotation.java",
		"package testbug526590;"+
		"public @interface QQAnnotation {\n"+
		"String[] value();\n"+
		"}");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "@QQAnnotation(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);

	assertResults(
			"Bug526590[TYPE_REF]{Bug526590, testbug526590, Ltestbug526590.Bug526590;, null, null, 52}\n" +
			"value[ANNOTATION_ATTRIBUTE_REF]{value = , Ltestbug526590.QQAnnotation;, [Ljava.lang.String;, value, null, 52}\n" +
			"String[TYPE_REF]{String, java.lang, Ljava.lang.String;, null, null, 82}",
			requestor.getResults());
}

public void test536983() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/SingleLineForEach.java",
			"package test;\n" +
			 		"public class SingleLineForEach {\n" +
			 		"	private void meth() {	\n" +
			 		"	Object[] f= {new Object(), new Object() };\n" +
			 		"	for (Object abc : f) abc.\n" +
			 		"}\n" +
			 		"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "abc.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertTrue(!requestor.getResults().equals(""));
	assertTrue(requestor.getResults().contains("toString("));
}
public void testBug15589() throws JavaModelException {
	CompletionResult result = complete(
            "/Completion/src3/bug15589/Test.java",
            "package bug15589;\n" +
            "import java.util.Coll;\n" +
            "public class Test {\n" +
            "}\n",
            "import java.util.Coll");

    assertResults(
            "Collection[TYPE_REF]{Collection, java.util, Ljava.util.Collection;, null, null, "+(R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_NON_RESTRICTED)+"}",
            result.proposals);
}
public void test565386() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/MyAnno.java",
			"package test;\n" +
			 		"public @interface MyAnno {\n" +
			 		"int age();"+
			 		"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "age";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertTrue(requestor.getResults().equals(""));

}
public void testBug532366() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/test/Color.java",
			"package test;\n" +
			"public enum Color {\n" +
			"	GREEN, BLUE;\n" +
			"}"
			);
	this.workingCopies[1] = getWorkingCopy(
			"/Completion/src/test/Foo.java",
			"package test;\n" +
	 		"public enum Foo {\n" +
	 		"	Bar(Color.G /* Ctrl+space and nothing happens here */);\n" +
	 		"	Foo(Color color) {}\n" +
	 		"}\n");
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[1].getSource();
	String completeBehind = "Color.G";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[1].codeComplete(cursorLocation, requestor, this.wcOwner);
	assertEquals("GREEN[FIELD_REF]{GREEN, Ltest.Color;, Ltest.Color;, null, null, GREEN, null, replace[43, 44], token[43, 44], 81}",
			requestor.getResults());
}
public void testBug573279() throws Exception {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy(
			"/Completion/src/App.java",
			"class MyArrayList<T> {\n" +
			"	boolean add(T t) {}\n" +
			"	boolean remove(Object o) {}\n" +
			"	int size() { return 0; }\n" +
			"}\n" +
			"\n" +
			"public class App {\n" +
			"  MyArrayList<String> list = new MyArrayList<String>();\n" +
			"  public static void main(String[] args) {}\n" +
			"\n" +
			"  private void foo() {\n" +
			"    String template = \"temp\";\n" +
			"    this.list.add(template.concat(\"late\"));\n" +
			"  }\n" +
			"}"
			);
	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "this.list.";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_INTERESTING + R_RESOLVED + R_CASE + R_NON_STATIC + R_NON_RESTRICTED;
	assertEquals("add[METHOD_REF]{add(), LMyArrayList<Ljava.lang.String;>;, (Ljava.lang.String;)Z, null, null, add, (t), replace[289, 318], token[289, 292], "+relevance+"}\n" +
			"clone[METHOD_REF]{clone(), Ljava.lang.Object;, ()Ljava.lang.Object;, null, null, clone, null, replace[289, 318], token[289, 292], "+relevance+"}\n" +
			"equals[METHOD_REF]{equals(), Ljava.lang.Object;, (Ljava.lang.Object;)Z, null, null, equals, (obj), replace[289, 318], token[289, 292], "+relevance+"}\n" +
			"finalize[METHOD_REF]{finalize(), Ljava.lang.Object;, ()V, null, null, finalize, null, replace[289, 318], token[289, 292], "+relevance+"}\n" +
			"getClass[METHOD_REF]{getClass(), Ljava.lang.Object;, ()Ljava.lang.Class<+Ljava.lang.Object;>;, null, null, getClass, null, replace[289, 318], token[289, 292], "+relevance+"}\n" +
			"hashCode[METHOD_REF]{hashCode(), Ljava.lang.Object;, ()I, null, null, hashCode, null, replace[289, 318], token[289, 292], "+relevance+"}\n" +
			"notify[METHOD_REF]{notify(), Ljava.lang.Object;, ()V, null, null, notify, null, replace[289, 318], token[289, 292], "+relevance+"}\n" +
			"notifyAll[METHOD_REF]{notifyAll(), Ljava.lang.Object;, ()V, null, null, notifyAll, null, replace[289, 318], token[289, 292], "+relevance+"}\n" +
			"remove[METHOD_REF]{remove(), LMyArrayList<Ljava.lang.String;>;, (Ljava.lang.Object;)Z, null, null, remove, (o), replace[289, 318], token[289, 292], "+relevance+"}\n" +
			"size[METHOD_REF]{size(), LMyArrayList<Ljava.lang.String;>;, ()I, null, null, size, null, replace[289, 318], token[289, 292], "+relevance+"}\n" +
			"toString[METHOD_REF]{toString(), Ljava.lang.Object;, ()Ljava.lang.String;, null, null, toString, null, replace[289, 318], token[289, 292], "+relevance+"}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, ()V, null, null, wait, null, replace[289, 318], token[289, 292], "+relevance+"}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (J)V, null, null, wait, (millis), replace[289, 318], token[289, 292], "+relevance+"}\n" +
			"wait[METHOD_REF]{wait(), Ljava.lang.Object;, (JI)V, null, null, wait, (millis, nanos), replace[289, 318], token[289, 292], "+relevance+"}",
			requestor.getResults());
}
public void testGH969_completeOnFirstArgumentPosition_noToken() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/Completion/src/GH969.java", """
			public class GH969 {
				public static void main(String[] args) {
					foo("1", new PersonDetails("1", GH969List.empty(), 0));
				}
				private static void foo(String name, PersonDetails per) {}

				public static class PersonDetails {
					public PersonDetails(String id, GH969List<String> address, int age){}
				}
			}
			""");
	this.workingCopies[1] = getWorkingCopy("/Completion/src/GH969List.java", """
			public class GH969List<T> {
				public static <T> GH969List<T> empty() {
					return new GH969List<>();
				}
			}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "foo(\"1\", new PersonDetails(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_INTERESTING + R_RESOLVED + R_NON_RESTRICTED;
	assertEquals(
			"GH969.PersonDetails[ANONYMOUS_CLASS_DECLARATION]{, LGH969$PersonDetails;, (Ljava.lang.String;LGH969List<Ljava.lang.String;>;I)V, LGH969$PersonDetails;, LGH969$PersonDetails;.(Ljava/lang/String;LGH969List<Ljava/lang/String;>;I)V, null, (id, address, age), replace[118, 118], token[118, 118], "
					+ relevance + "}\n"
					+ "PersonDetails[METHOD_REF<CONSTRUCTOR>]{, LGH969$PersonDetails;, (Ljava.lang.String;LGH969List<Ljava.lang.String;>;I)V, null, null, PersonDetails, (id, address, age), replace[118, 118], token[74, 118], "
					+ relevance + "}",
			requestor.getResults());
}
public void testGH969_completeOnFirstArgumentPosition_WithToken() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/Completion/src/GH969.java", """
			public class GH969 {
				public static void main(String[] args) {
					foo("1", new PersonDetails(first, GH969List.empty(), 0));
				}
				private static void foo(String name, PersonDetails per) {}
				private static String firstName() { return ""; }
				public static class PersonDetails {
					public PersonDetails(String id, GH969List<String> address, int age){}
				}
			}
			""");
	this.workingCopies[1] = getWorkingCopy("/Completion/src/GH969List.java", """
			public class GH969List<T> {
				public static <T> GH969List<T> empty() {
					return new GH969List<>();
				}
			}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "PersonDetails(first";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_INTERESTING + R_RESOLVED + R_NON_RESTRICTED + R_CASE + R_UNQUALIFIED;
	assertEquals(
			"firstName[METHOD_REF]{firstName(), LGH969;, ()Ljava.lang.String;, null, null, firstName, null, replace[92, 97], token[92, 97], "
					+ relevance + "}",
			requestor.getResults());
}
public void testGH969_completeOnArgumentPosition_WithToken() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/Completion/src/GH969.java", """
			public class GH969 {
				public static void main(String[] args) {
					foo("1", new PersonDetails("1", empty, 0));
				}
				private static void foo(String name, PersonDetails per) {}
				private static <T> GH969List emptyList(){ retutn null; }
				public static class PersonDetails {
					public PersonDetails(String id, GH969List<String> address, int age){}
				}
			}
			""");
	this.workingCopies[1] = getWorkingCopy("/Completion/src/GH969List.java", """
			public class GH969List<T> {
				public static <T> GH969List<T> empty() {
					return new GH969List<>();
				}
			}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = ", empty";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_INTERESTING + R_RESOLVED + R_NON_RESTRICTED + R_CASE + R_UNQUALIFIED;
	assertEquals(
			"emptyList[METHOD_REF]{emptyList(), LGH969;, <T:Ljava.lang.Object;>()LGH969List;, null, null, emptyList, null, replace[97, 102], token[97, 102], "
					+ relevance + "}",
			requestor.getResults());
}

public void testGH969_completeOnArgumentPosition_onMethodWithReceiver() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/Completion/src/GH969.java", """
			public class GH969 {
				public static void main(String[] args) {
					instance().foo(new PersonDetails(GH969.emptyList(), 0));
				}
				public static GH969 instance() {
					return new GH969();
				}
				public void foo(PersonDetails per) {}
				private static <T> GH969List<T> emptyList(){ return null; }
				public static class PersonDetails {
					public PersonDetails(GH969List<String> address, int age){}
				}
			}
			""");
	this.workingCopies[1] = getWorkingCopy("/Completion/src/GH969List.java", """
			public class GH969List<T> {
				public static <T> GH969List<T> empty() {
					return new GH969List<>();
				}
			}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new PersonDetails(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_INTERESTING + R_RESOLVED + R_NON_RESTRICTED;
	int symbolRelevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED;
	assertEquals(
			"GH969.PersonDetails[ANONYMOUS_CLASS_DECLARATION]{, LGH969$PersonDetails;, (LGH969List<Ljava.lang.String;>;I)V, LGH969$PersonDetails;, LGH969$PersonDetails;.(LGH969List<Ljava/lang/String;>;I)V, null, (address, age), replace[119, 119], token[119, 119], "
					+ relevance + "}\n"
					+ "PersonDetails[METHOD_REF<CONSTRUCTOR>]{, LGH969$PersonDetails;, (LGH969List<Ljava.lang.String;>;I)V, null, null, PersonDetails, (address, age), replace[119, 119], token[80, 119], "
					+ relevance + "}\n"
					+ "emptyList[METHOD_REF]{emptyList(), LGH969;, <T:Ljava.lang.Object;>()LGH969List<TT;>;, null, null, emptyList, null, replace[98, 98], token[98, 98], " + symbolRelevance + "}",
			requestor.getResults());
}
public void testGH969_completeOnArgumentPosition_onMethodInvocation() throws Exception {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/Completion/src/GH969.java", """
			public class GH969 {
				public static void main(String[] args) {
					instance().foo(new PersonDetails(emptyList(), 0));
				}
				public static GH969 instance() {
					return new GH969();
				}
				public void foo(PersonDetails per) {}
				private static <T> GH969List<T> emptyList(){ return null; }
				public static class PersonDetails {
					public PersonDetails(GH969List<String> address, int age){}
				}
			}
			""");
	this.workingCopies[1] = getWorkingCopy("/Completion/src/GH969List.java", """
			public class GH969List<T> {
				public static <T> GH969List<T> empty() {
					return new GH969List<>();
				}
			}
			""");

	CompletionTestsRequestor2 requestor = new CompletionTestsRequestor2(true, true, true, true, true, true);
	requestor.allowAllRequiredProposals();
	String str = this.workingCopies[0].getSource();
	String completeBehind = "new PersonDetails(";
	int cursorLocation = str.lastIndexOf(completeBehind) + completeBehind.length();
	this.workingCopies[0].codeComplete(cursorLocation, requestor, this.wcOwner);
	int relevance = R_DEFAULT + R_INTERESTING + R_RESOLVED + R_NON_RESTRICTED;
	int symbolRelevance = R_DEFAULT + R_RESOLVED + R_INTERESTING + R_CASE + R_UNQUALIFIED + R_NON_RESTRICTED;
	assertEquals(
			"GH969.PersonDetails[ANONYMOUS_CLASS_DECLARATION]{, LGH969$PersonDetails;, (LGH969List<Ljava.lang.String;>;I)V, LGH969$PersonDetails;, LGH969$PersonDetails;.(LGH969List<Ljava/lang/String;>;I)V, null, (address, age), replace[113, 113], token[113, 113], "
					+ relevance + "}\n"
					+ "PersonDetails[METHOD_REF<CONSTRUCTOR>]{, LGH969$PersonDetails;, (LGH969List<Ljava.lang.String;>;I)V, null, null, PersonDetails, (address, age), replace[113, 113], token[80, 113], "
					+ relevance + "}\n"
					+ "emptyList[METHOD_REF]{emptyList(), LGH969;, <T:Ljava.lang.Object;>()LGH969List<TT;>;, null, null, emptyList, null, replace[98, 98], token[98, 98], " + symbolRelevance + "}",
			requestor.getResults());
}
}
