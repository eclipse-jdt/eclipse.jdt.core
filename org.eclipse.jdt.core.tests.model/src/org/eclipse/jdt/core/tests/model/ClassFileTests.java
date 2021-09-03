/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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

import java.io.IOException;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.core.ClasspathEntry;

import junit.framework.Test;

public class ClassFileTests extends ModifyingResourceTests {

	IPackageFragmentRoot jarRoot;
	ICompilationUnit workingCopy;
	IOrdinaryClassFile classFile;

public ClassFileTests(String name) {
	super(name);
}

// Use this static initializer to specify subset for tests
// All specified tests which do not belong to the class are skipped...
static {
//	TESTS_PREFIX = "testGetCategories";
//	TESTS_NAMES = new String[] { "testBug372687"};
//	TESTS_NUMBERS = new int[] { 13 };
//	TESTS_RANGE = new int[] { 16, -1 };
}
public static Test suite() {
	return buildModelTestSuite(ClassFileTests.class);
}

@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();
	IJavaProject javaProject = createJavaProject("P", new String[0], new String[] {"JCL15_LIB", "/P/lib"}, "", JavaCore.VERSION_9);
	String[] pathAndContents = new String[] {
		"nongeneric/A.java",
		"package nongeneric;\n" +
		"public class A {\n" +
		"}",
		"generic/X.java",
		"package generic;\n" +
		"public class X<T> {\n" +
		"  <U extends Exception> X<T> foo(X<T> x) throws RuntimeException, U {\n" +
		"    return null;\n" +
		"  }\n" +
		"  <K, V extends T> V foo(K key, V value) throws Exception {\n" +
		"    return value;\n" +
		"  }\n" +
		"}",
		"generic/Y.java",
		"package generic;\n" +
		"public class Y<K, L> {\n" +
		"}",
		"generic/Z.java",
		"package generic;\n" +
		"public class Z<T extends Object & I<? super T>> {\n" +
		"}",
		"generic/I.java",
		"package generic;\n" +
		"public interface I<T> {\n" +
		"}",
		"generic/W.java",
		"package generic;\n" +
		"public class W<T extends X<T> , U extends T> {\n" +
		"}",
		"generic/V.java",
		"package generic;\n" +
		"public class V extends X<Thread> implements I<String> {\n" +
		"}",
		"generic/GenericField.java",
		"package generic;\n" +
		"import java.util.Collection;\n" +
		"public class GenericField {\n" +
		"	protected Collection<String> myField;\n" +
		"}",
		"annotated/X.java",
		"package annotated;\n" +
		"@MyOtherAnnot\n" +
		"public class X {\n" +
		"  @MyOtherAnnot\n" +
		"  Object field;\n" +
		"  @MyOtherAnnot\n" +
		"  void method() {}\n" +
		"  @MyAnnot(_int=1)\n" +
		"  void foo01() {}\n" +
		"  @MyAnnot(_byte=(byte)2)\n" +
		"  void foo02() {}\n" +
		"  @MyAnnot(_short=(short)3)\n" +
		"  void foo03() {}\n" +
		"  @MyAnnot(_char='a')\n" +
		"  void foo04() {}\n" +
		"  @MyAnnot(_float=1.2f)\n" +
		"  void foo05() {}\n" +
		"  @MyAnnot(_double=3.4)\n" +
		"  void foo06() {}\n" +
		"  @MyAnnot(_boolean=true)\n" +
		"  void foo07() {}\n" +
		"  @MyAnnot(_long=123456789L)\n" +
		"  void foo08() {}\n" +
		"  @MyAnnot(_string=\"abc\")\n" +
		"  void foo09() {}\n" +
		"  @MyAnnot(_annot=@MyOtherAnnot)\n" +
		"  void foo10() {}\n" +
		"  @MyAnnot(_class=String.class)\n" +
		"  void foo11() {}\n" +
		"  @MyAnnot(_enum=MyEnum.SECOND)\n" +
		"  void foo12() {}\n" +
		"  @MyAnnot(_array={1, 2, 3})\n" +
		"  void foo13() {}\n" +
		"  @MyAnnot(_neg_int = -2)\n" +
		"  void foo14() {}\n" +
		"  @MyAnnot(_neg_float=-2.0f)\n" +
		"  void foo15() {}\n" +
		"  @MyAnnot(_neg_double=-2.0)\n" +
		"  void foo16() {}\n" +
		"  @MyAnnot(_neg_long=-2L)\n" +
		"  void foo17() {}\n" +
		"}\n" +
		"@interface MyAnnot {\n" +
		"  int _int() default 0;\n" +
		"  byte _byte() default 0;\n" +
		"  short _short() default 0;\n" +
		"  char _char() default ' ';\n" +
		"  float _float() default 0.0f;\n" +
		"  double _double() default 0.0;\n" +
		"  boolean _boolean() default false;\n" +
		"  long _long() default 0L;\n" +
		"  String _string() default \"   \";\n" +
		"  MyOtherAnnot _annot() default @MyOtherAnnot;\n" +
		"  Class _class() default Object.class;\n" +
		"  MyEnum _enum() default MyEnum.FIRST;\n" +
		"  int[] _array() default {};\n" +
		"  int _neg_int() default -1;\n" +
		"  float _neg_float() default -1.0f;\n" +
		"  double _neg_double() default -1.0;\n" +
		"  long _neg_long() default -1L;\n" +
		"}\n" +
		"@interface MyOtherAnnot {\n" +
		"}\n" +
		"enum MyEnum {\n" +
		"  FIRST, SECOND;\n" +
		"}",
		"annotated/Y.java",
		"package annotated;\n" +
		"import java.lang.annotation.*;\n" +
		"import static java.lang.annotation.ElementType.*;\n" +
		"import static java.lang.annotation.RetentionPolicy.*;\n" +
		"@Deprecated\n" +
		"@Documented\n" +
		"@Inherited\n" +
		"@Retention(SOURCE)\n" +
		"@Target({PACKAGE, TYPE, ANNOTATION_TYPE, METHOD, CONSTRUCTOR, FIELD, LOCAL_VARIABLE, PARAMETER})\n" +
		"public @interface Y {\n" +
		"}",
		"varargs/X.java",
		"package varargs;\n" +
		"public class X {\n" +
		"  void foo(String s, Object ... others) {\n" +
		"  }\n" +
		"}",
		"workingcopy/X.java",
		"package workingcopy;\n" +
		"public class X {\n" +
		"  void foo() {\n" +
		"    System.out.println();\n" +
		"  }\n" +
		"}",
		"workingcopy/Y.java",
		"package workingcopy;\n" +
		"public class Y<W> {\n" +
		"  <T> T foo(T t, String... args) {\n" +
		"    return t;\n" +
		"  }\n" +
		"}",
		"annotated/MyAnnotation.java",
		"package annotated;\n" +
		"import java.lang.annotation.Retention;\n" +
		"import java.lang.annotation.RetentionPolicy;\n" +
		"@Retention(value = RetentionPolicy.RUNTIME)\n" +
		"public @interface MyAnnotation {}",
		"annotated/MyAnnotation2.java",
		"package annotated;\n" +
		"import java.lang.annotation.Retention;\n" +
		"import java.lang.annotation.RetentionPolicy;\n" +
		"@Retention(value = RetentionPolicy.SOURCE)\n" +
		"public @interface MyAnnotation2 {}",
		"annotated/MyAnnotation3.java",
		"package annotated;\n" +
		"import java.lang.annotation.Retention;\n" +
		"import java.lang.annotation.RetentionPolicy;\n" +
		"@Retention(value = RetentionPolicy.CLASS)\n" +
		"public @interface MyAnnotation3 {}",
		"test342757/X.java",
		"package test342757;\n" +
		"public class X {\n" +
		"	class B {\n" +
		"		public B(@Deprecated @Annot String s) {}\n" +
		"		public void foo(@Deprecated @Annot int j) {}\n" +
		"	}\n" +
		"}",
		"test342757/Annot.java",
		"package test342757;\n" +
		"import java.lang.annotation.Retention;\n" +
		"import static java.lang.annotation.RetentionPolicy.*;\n" +
		"@Retention(CLASS)\n" +
		"@interface Annot {}",
	};
	addLibrary(javaProject, "lib.jar", "libsrc.zip", pathAndContents, JavaCore.VERSION_1_5);
	this.jarRoot = javaProject.getPackageFragmentRoot(getFile("/P/lib.jar"));
}

@Override
public void tearDownSuite() throws Exception {
	super.tearDownSuite();
	deleteProject("P");
}

@Override
protected void tearDown() throws Exception {
	if (this.workingCopy != null)
		this.workingCopy.discardWorkingCopy();
	if (this.classFile != null) {
		removeLibrary(getJavaProject("P"), "lib2.jar", "src2.zip");
		this.classFile = null;
	}
	super.tearDown();
}

private IOrdinaryClassFile createClassFile(String contents) throws CoreException, IOException {
	IJavaProject project = getJavaProject("P");
	addLibrary(project, "lib2.jar", "src2.zip", new String[] {"p/X.java", contents}, "1.5");
	this.classFile =  project.getPackageFragmentRoot(getFile("/P/lib2.jar")).getPackageFragment("p").getOrdinaryClassFile("X.class");
	return this.classFile;
}

/*
 * Ensures that the annotations of a binary type are correct
 */
public void testAnnotations01() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	assertAnnotationsEqual(
		"@annotated.MyOtherAnnot\n",
		type.getAnnotations());
}

/*
 * Ensures that the annotations of a binary method are correct
 */
public void testAnnotations02() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("method", new String[0]);
	assertAnnotationsEqual(
		"@annotated.MyOtherAnnot\n",
		method.getAnnotations());
}

/*
 * Ensures that the annotations of a binary field are correct
 */
public void testAnnotations03() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IField field = type.getField("field");
	assertAnnotationsEqual(
		"@annotated.MyOtherAnnot\n",
		field.getAnnotations());
}

/*
 * Ensures that an annotation with an int value is correct
 */
public void testAnnotations04() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo01", new String[0]);
	assertAnnotationsEqual(
		"@annotated.MyAnnot(_int=(int)1)\n",
		method.getAnnotations());
}

/*
 * Ensures that an annotation with a byte value is correct
 */
public void testAnnotations05() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo02", new String[0]);
	assertAnnotationsEqual(
		"@annotated.MyAnnot(_byte=(byte)2)\n",
		method.getAnnotations());
}

/*
 * Ensures that an annotation with a short value is correct
 */
public void testAnnotations06() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo03", new String[0]);
	assertAnnotationsEqual(
		"@annotated.MyAnnot(_short=(short)3)\n",
		method.getAnnotations());
}

/*
 * Ensures that an annotation with a char value is correct
 */
public void testAnnotations07() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo04", new String[0]);
	assertAnnotationsEqual(
		"@annotated.MyAnnot(_char='a')\n",
		method.getAnnotations());
}

/*
 * Ensures that an annotation with a float value is correct
 */
public void testAnnotations08() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo05", new String[0]);
	assertAnnotationsEqual(
		"@annotated.MyAnnot(_float=1.2f)\n",
		method.getAnnotations());
}

/*
 * Ensures that an annotation with a double value is correct
 */
public void testAnnotations09() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo06", new String[0]);
	assertAnnotationsEqual(
		"@annotated.MyAnnot(_double=(double)3.4)\n",
		method.getAnnotations());
}

/*
 * Ensures that an annotation with a boolean value is correct
 */
public void testAnnotations10() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo07", new String[0]);
	assertAnnotationsEqual(
		"@annotated.MyAnnot(_boolean=true)\n",
		method.getAnnotations());
}

/*
 * Ensures that an annotation with a long value is correct
 */
public void testAnnotations11() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo08", new String[0]);
	assertAnnotationsEqual(
		"@annotated.MyAnnot(_long=123456789L)\n",
		method.getAnnotations());
}

/*
 * Ensures that an annotation with a String value is correct
 */
public void testAnnotations12() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo09", new String[0]);
	assertAnnotationsEqual(
		"@annotated.MyAnnot(_string=\"abc\")\n",
		method.getAnnotations());
}

/*
 * Ensures that an annotation with an annotation value is correct
 */
public void testAnnotations13() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo10", new String[0]);
	assertAnnotationsEqual(
		"@annotated.MyAnnot(_annot=@annotated.MyOtherAnnot)\n",
		method.getAnnotations());
}

/*
 * Ensures that an annotation with a Class value is correct
 */
public void testAnnotations14() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo11", new String[0]);
	assertAnnotationsEqual(
		"@annotated.MyAnnot(_class=java.lang.String.class)\n",
		method.getAnnotations());
}

/*
 * Ensures that an annotation with an enumeration value is correct
 */
public void testAnnotations15() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo12", new String[0]);
	assertAnnotationsEqual(
		"@annotated.MyAnnot(_enum=annotated.MyEnum.SECOND)\n",
		method.getAnnotations());
}

/*
 * Ensures that an annotation with an array value is correct
 */
public void testAnnotations16() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo13", new String[0]);
	assertAnnotationsEqual(
		"@annotated.MyAnnot(_array={(int)1, (int)2, (int)3})\n",
		method.getAnnotations());
}

/*
 * Ensures that the standard annotations of a binary type are correct
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=248309 )
 */
public void testAnnotations17() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("Y.class").getType();
	assertAnnotationsEqual(
		"@java.lang.annotation.Target({java.lang.annotation.ElementType.PACKAGE, java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.ANNOTATION_TYPE, java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.CONSTRUCTOR, java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.LOCAL_VARIABLE, java.lang.annotation.ElementType.PARAMETER})\n" +
		"@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.SOURCE)\n" +
		"@java.lang.Deprecated\n" +
		"@java.lang.annotation.Documented\n" +
		"@java.lang.annotation.Inherited\n",
		type.getAnnotations());
}

/*
 * Ensures that the annotation of a binary type exists
 */
public void testAnnotations18() throws JavaModelException {
	IAnnotation annotation = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType().getAnnotation("annotated.MyOtherAnnot");
	assertTrue("Annotation should exist", annotation.exists());
}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=286407
 */
public void testAnnotations19() throws JavaModelException {
	IPackageFragment packageFragment = this.jarRoot.getPackageFragment("annotated");
	IOrdinaryClassFile classFile2 = packageFragment.getOrdinaryClassFile("MyAnnotation.class");
	IType type = classFile2.getType();
	assertAnnotationsEqual(
		"@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)\n",
		type.getAnnotations());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=286407
 */
public void testAnnotations20() throws JavaModelException {
	IPackageFragment packageFragment = this.jarRoot.getPackageFragment("annotated");
	IOrdinaryClassFile classFile2 = packageFragment.getOrdinaryClassFile("MyAnnotation2.class");
	IType type = classFile2.getType();
	assertAnnotationsEqual(
		"@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.SOURCE)\n",
		type.getAnnotations());
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=286407
 */
public void testAnnotations21() throws JavaModelException {
	IPackageFragment packageFragment = this.jarRoot.getPackageFragment("annotated");
	IOrdinaryClassFile classFile2 = packageFragment.getOrdinaryClassFile("MyAnnotation3.class");
	IType type = classFile2.getType();
	assertAnnotationsEqual(
		"@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.CLASS)\n",
		type.getAnnotations());
}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=248312
 * Ensures that an annotation with a negative int value is correct
 */
public void testAnnotations22() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo14", new String[0]);
	assertAnnotationsEqual(
		"@annotated.MyAnnot(_neg_int=(int)-2)\n",
		method.getAnnotations());
}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=248312
 * Ensures that an annotation with a negative float value is correct
 */
public void testAnnotations23() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo15", new String[0]);
	assertAnnotationsEqual(
		"@annotated.MyAnnot(_neg_float=-2.0f)\n",
		method.getAnnotations());
}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=248312
 * Ensures that an annotation with a negative double value is correct
 */
public void testAnnotations24() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo16", new String[0]);
	assertAnnotationsEqual(
		"@annotated.MyAnnot(_neg_double=(double)-2.0)\n",
		method.getAnnotations());
}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=248312
 * Ensures that an annotation with a negative long value is correct
 */
public void testAnnotations25() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo17", new String[0]);
	assertAnnotationsEqual(
		"@annotated.MyAnnot(_neg_long=-2L)\n",
		method.getAnnotations());
}

/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=342757
 */
public void testAnnotations26() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("test342757").getOrdinaryClassFile("X$B.class").getType();
	IMethod[] methods = type.getMethods();
	String expected =
			"@test342757.Annot\n" +
			"@java.lang.Deprecated\n" +
			"@test342757.Annot\n" +
			"@java.lang.Deprecated\n";
	StringBuffer buffer = new StringBuffer();
	for (int i = 0, max = methods.length; i < max; i++) {
		ILocalVariable[] parameters = methods[i].getParameters();
		for (int j = 0, max2 = parameters.length; j < max2; j++) {
			IAnnotation[] annotations = parameters[j].getAnnotations();
			for (int n = 0; n < annotations.length; n++) {
				IAnnotation annotation = annotations[n];
				appendAnnotation(buffer, annotation);
				buffer.append("\n");
			}
		}
	}
	String actual = buffer.toString();
	if (!expected.equals(actual)) {
		System.out.println(displayString(actual, 2) + this.endChar);
	}
	assertEquals("Unexpected annotations", expected, actual);
}

/*
 * Ensures that no exception is thrown for a .class file name with a dot
 * (regression test for bug 114140 assertion failed when opening a class file not not the classpath)
 */
public void testDotName() throws JavaModelException {
	IType type = getClassFile("/P/X.Y.class").getType();
	assertEquals("X.Y", type.getElementName());
}

/*
 * Ensure that the exception types of a binary method are correct.
 */
public void testExceptionTypes1() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"TK;", "TV;"});
	assertStringsEqual(
		"Unexpected return type",
		"Ljava.lang.Exception;\n",
		method.getExceptionTypes());
}

/*
 * Ensure that the exception types of a binary method is correct.
 */
public void testExceptionTypes2() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"Lgeneric.X<TT;>;"});
	assertStringsEqual(
		"Unexpected return type",
		"Ljava.lang.RuntimeException;\n" +
		"TU;\n",
		method.getExceptionTypes());
}

/*
 * Ensure that the categories for a class are correct.
 */
public void testGetCategories01() throws CoreException, IOException {
	createClassFile(
		"package p;\n" +
		"/**\n" +
		" * @category test\n" +
		" */\n" +
		"public class X {\n" +
		"}"
	);
	String[] categories = this.classFile.getType().getCategories();
	assertStringsEqual(
		"Unexpected categories",
		"test\n",
		categories);
}
public void testGetCategories02() throws CoreException, IOException {
	createClassFile(
		"package p;\n" +
		"/**\n" +
		" * @category test1 test2 test3 test4 test5 test6 test7 test8 test9 test10\n" +
		" */\n" +
		"public class X {\n" +
		"}"
	);
	String[] categories = this.classFile.getType().getCategories();
	assertStringsEqual(
		"Unexpected categories",
		"test1\ntest2\ntest3\ntest4\ntest5\ntest6\ntest7\ntest8\ntest9\ntest10\n",
		categories);
}

/*
 * Ensure that the categories for a field are correct.
 */
public void testGetCategories03() throws CoreException, IOException {
	createClassFile(
		"package p;\n" +
		"public class X {\n" +
		"  /**\n" +
		"   * @category test\n" +
		"   */\n" +
		"  int field;\n" +
		"}"
	);
	String[] categories = this.classFile.getType().getField("field").getCategories();
	assertStringsEqual(
		"Unexpected categories",
		"test\n",
		categories);
}
public void testGetCategories04() throws CoreException, IOException {
	createClassFile(
		"package p;\n" +
		"public class X {\n" +
		"  /**\n" +
		"   * @category test1 test2\n" +
		"   */\n" +
		"  int field;\n" +
		"}"
	);
	String[] categories = this.classFile.getType().getField("field").getCategories();
	assertStringsEqual(
		"Unexpected categories",
		"test1\ntest2\n",
		categories);
}

/*
 * Ensure that the categories for a method are correct.
 */
public void testGetCategories05() throws CoreException, IOException {
	createClassFile(
		"package p;\n" +
		"public class X {\n" +
		"  /**\n" +
		" * @category test\n" +
		"   */\n" +
		"  void foo() {}\n" +
		"}"
	);
	String[] categories = this.classFile.getType().getMethod("foo", new String[0]).getCategories();
	assertStringsEqual(
		"Unexpected categories",
		"test\n",
		categories);
}
public void testGetCategories06() throws CoreException, IOException {
	createClassFile(
		"package p;\n" +
		"public class X {\n" +
		"  /**\n" +
		" * @category test1 test2 test3 test4 test5\n" +
		"   */\n" +
		"  void foo() {}\n" +
		"}"
	);
	String[] categories = this.classFile.getType().getMethod("foo", new String[0]).getCategories();
	assertStringsEqual(
		"Unexpected categories",
		"test1\ntest2\ntest3\ntest4\ntest5\n",
		categories);
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=125676
public void testGetCategories07() throws CoreException, IOException {
	createClassFile(
		"package p;\n" +
		"public class X {\n" +
		"  /**\n" +
		"   * @category " +
		"	 *		test\n" +
		"   */\n" +
		"  void foo() {}\n" +
		"}"
	);
	String[] categories = this.classFile.getType().getMethod("foo", new String[0]).getCategories();
	assertStringsEqual(
		"Unexpected categories",
		"",
		categories);
}
public void testGetCategories08() throws CoreException, IOException {
	createClassFile(
		"package p;\n" +
		"public class X {\n" +
		"  /**\n" +
		"   * @category" +
		"	 *		test\n" +
		"   */\n" +
		"  void foo() {}\n" +
		"}"
	);
	String[] categories = this.classFile.getType().getMethod("foo", new String[0]).getCategories();
	assertStringsEqual(
		"Unexpected categories",
		"",
		categories);
}
public void testGetCategories09() throws CoreException, IOException {
	createClassFile(
		"package p;\n" +
		"public class X {\n" +
		"  /**\n" +
		"   * @category test1" +
		"	 *		test2\n" +
		"   */\n" +
		"  void foo() {}\n" +
		"}"
	);
	String[] categories = this.classFile.getType().getMethod("foo", new String[0]).getCategories();
	assertStringsEqual(
		"Unexpected categories",
		"test1\n",
		categories);
}

/*
 * Ensure that the categories for a member that has no categories when another member defines some are correct.
 */
public void testGetCategories10() throws CoreException, IOException {
	createClassFile(
		"package p;\n" +
		"public class X {\n" +
		"  int field1;\n" +
		"  /**\n" +
		"   * @category test\n" +
		"   */\n" +
		"  int field2;\n" +
		"}"
	);
	String[] categories = this.classFile.getType().getField("field1").getCategories();
	assertStringsEqual(
		"Unexpected categories",
		"",
		categories);
}

/*
 * Ensures that the children of a type for a given category are correct.
 */
public void testGetChildrenForCategory01() throws CoreException, IOException {
	createClassFile(
		"package p;\n" +
		"public class X {\n" +
		"  /**\n" +
		"   * @category test\n" +
		"   */\n" +
		"  int field;\n" +
		"  /**\n" +
		"   * @category test\n" +
		"   */\n" +
		"  void foo1() {}\n" +
		"  /**\n" +
		"   * @category test\n" +
		"   */\n" +
		"  void foo2() {}\n" +
		"  /**\n" +
		"   * @category other\n" +
		"   */\n" +
		"  void foo3() {}\n" +
		"}"
	);
	IJavaElement[] children = this.classFile.getType().getChildrenForCategory("test");
	assertElementsEqual(
		"Unexpected children",
		"field [in X [in X.class [in p [in lib2.jar [in P]]]]]\n" +
		"foo1() [in X [in X.class [in p [in lib2.jar [in P]]]]]\n" +
		"foo2() [in X [in X.class [in p [in lib2.jar [in P]]]]]",
		children);
}
public void testGetChildrenForCategory02() throws CoreException, IOException {
	createClassFile(
		"package p;\n" +
		"public class X {\n" +
		"  /**\n" +
		"   * @category fields test all\n" +
		"   */\n" +
		"  int field;\n" +
		"  /**\n" +
		"   * @category methods test all\n" +
		"   */\n" +
		"  void foo1() {}\n" +
		"  /**\n" +
		"   * @category methods test all\n" +
		"   */\n" +
		"  void foo2() {}\n" +
		"  /**\n" +
		"   * @category methods other all\n" +
		"   */\n" +
		"  void foo3() {}\n" +
		"}"
	);
	IJavaElement[] tests  = this.classFile.getType().getChildrenForCategory("test");
	assertElementsEqual(
		"Unexpected children",
		"field [in X [in X.class [in p [in lib2.jar [in P]]]]]\n" +
		"foo1() [in X [in X.class [in p [in lib2.jar [in P]]]]]\n" +
		"foo2() [in X [in X.class [in p [in lib2.jar [in P]]]]]",
		tests);
	IJavaElement[] methods = this.classFile.getType().getChildrenForCategory("methods");
	assertElementsEqual(
		"Unexpected children",
		"foo1() [in X [in X.class [in p [in lib2.jar [in P]]]]]\n" +
		"foo2() [in X [in X.class [in p [in lib2.jar [in P]]]]]\n" +
		"foo3() [in X [in X.class [in p [in lib2.jar [in P]]]]]",
		methods);
	IJavaElement[] others = this.classFile.getType().getChildrenForCategory("other");
	assertElementsEqual(
		"Unexpected children",
		"foo3() [in X [in X.class [in p [in lib2.jar [in P]]]]]",
		others);
	IJavaElement[] all = this.classFile.getType().getChildrenForCategory("all");
	assertElementsEqual(
		"Unexpected children",
		"field [in X [in X.class [in p [in lib2.jar [in P]]]]]\n" +
		"foo1() [in X [in X.class [in p [in lib2.jar [in P]]]]]\n" +
		"foo2() [in X [in X.class [in p [in lib2.jar [in P]]]]]\n" +
		"foo3() [in X [in X.class [in p [in lib2.jar [in P]]]]]",
		all);
}

/*
 * Ensures that the default value for an annotation method is correct.
 */
public void testDefaultValue1() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("MyAnnot.class").getType();
	IMethod method = type.getMethod("_int", new String[0]);
	assertMemberValuePairEquals(
		"_int=(int)0",
		method.getDefaultValue());
}

/*
 * Ensures that the default value for an annotation method is correct.
 */
public void testDefaultValue2() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("MyAnnot.class").getType();
	IMethod method = type.getMethod("_annot", new String[0]);
	assertMemberValuePairEquals(
		"_annot=@annotated.MyOtherAnnot",
		method.getDefaultValue());
}

/*
 * Ensures that the default value for an annotation method is correct.
 */
public void testDefaultValue3() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("annotated").getOrdinaryClassFile("MyAnnot.class").getType();
	IMethod method = type.getMethod("_array", new String[0]);
	assertMemberValuePairEquals(
		"_array=[unknown]{}",
		method.getDefaultValue());
}


/*
 * Ensures that the default value for an regular method is correct.
 */
public void testDefaultValue4() throws JavaModelException {
	IType type = getPackageFragmentRoot("P", getExternalJCLPathString(JavaCore.VERSION_1_5)).getPackageFragment("java.lang").getOrdinaryClassFile("Object.class").getType();
	IMethod method = type.getMethod("toString", new String[0]);
	assertMemberValuePairEquals(
		"<null>",
		method.getDefaultValue());
}

/*
 * Ensures that getFullyQualifiedName() behaves correctly for a top level binary type
 */
public void testGetFullyQualifiedName1() {
	IType type = getClassFile("/P/lib/p/X.class").getType();
	assertEquals("p.X", type.getFullyQualifiedName());
}

/*
 * Ensures that getFullyQualifiedName() behaves correctly for a top level binary type
 */
public void testGetFullyQualifiedName2() {
	IType type = getClassFile("/P/lib/X.class").getType();
	assertEquals("X", type.getFullyQualifiedName());
}

/*
 * Ensures that getFullyQualifiedName() behaves correctly for a member type
 */
public void testGetFullyQualifiedName3() {
	IType type = getClassFile("/P/lib/p/X$Member.class").getType();
	assertEquals("p.X$Member", type.getFullyQualifiedName());
}

/*
 * Ensures that getFullyQualifiedName() behaves correctly for a local type
 */
public void testGetFullyQualifiedName4() {
	IType type = getClassFile("/P/lib/p/X$Local.class").getType();
	assertEquals("p.X$Local", type.getFullyQualifiedName());
}

/*
 * Ensures that getFullyQualifiedName('.') behaves correctly for a top level binary type
 */
public void testGetFullyQualifiedName5() {
	IType type = getClassFile("/P/lib/p/X.class").getType();
	assertEquals("p.X", type.getFullyQualifiedName('.'));
}

/*
 * Ensures that getFullyQualifiedName('.') behaves correctly for a top level binary type
 */
public void testGetFullyQualifiedName6() {
	IType type = getClassFile("/P/lib/X.class").getType();
	assertEquals("X", type.getFullyQualifiedName('.'));
}

/*
 * Ensures that getFullyQualifiedName() behaves correctly for a member type
 */
public void testGetFullyQualifiedName7() {
	IType type = getClassFile("/P/lib/p/X$Member.class").getType();
	assertEquals("p.X.Member", type.getFullyQualifiedName('.'));
}

/*
 * Ensures that getFullyQualifiedName() behaves correctly for a local type
 */
public void testGetFullyQualifiedName8() {
	IType type = getClassFile("/P/lib/p/X$Local.class").getType();
	assertEquals("p.X.Local", type.getFullyQualifiedName('.'));
}

/*
 * Ensures that the resource of a .class file in an external folder is null
 */
public void testGetResource() throws Exception {
	try {
		createExternalFolder("externalLib/p");
		createExternalFile("externalLib/p/X.class", "");
		createJavaProject("P1", new String[0], new String[] {getExternalResourcePath("externalLib")}, "");
		IOrdinaryClassFile classFile1 = getClassFile("P1", getExternalResourcePath("externalLib"), "p", "X.class");
		assertResourceEquals(
			"Unexpected resource",
			"<null>",
			classFile1.getResource());
	} finally {
		deleteExternalResource("externalLib");
		deleteProject("P1");
	}
}

/*
 * Ensures that IType#getSuperclassTypeSignature() is correct for a binary type.
 * (regression test for bug 78520 [model] IType#getSuperInterfaceTypeSignatures() doesn't include type arguments)
 */
public void testGetSuperclassTypeSignature() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile("V.class").getType();
	assertEquals(
		"Unexpected signature",
		"Lgeneric.X<Ljava.lang.Thread;>;",
		type.getSuperclassTypeSignature());
}

/*
 * Ensures that IType#getSuperInterfaceTypeSignatures() is correct for a binary type.
 * (regression test for bug 78520 [model] IType#getSuperInterfaceTypeSignatures() doesn't include type arguments)
 */
public void testGetSuperInterfaceTypeSignatures() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile("V.class").getType();
	assertStringsEqual(
		"Unexpected signatures",
		"Lgeneric.I<Ljava.lang.String;>;\n",
		type.getSuperInterfaceTypeSignatures());
}

/*
 * Ensures that if a root folder (that has a jar like name) is opened, and then a Jar package fragment root created on this root folder,
 * then attempting to open a class file in this folder doesn't throw a ClassCastException
 * (regression test for bug 204652 "Open Type": ClassCastException in conjunction with a class folder)
 */
public void testJarLikeRootFolder() throws CoreException {
	try {
		IJavaProject p = createJavaProject("P1", new String[0], new String[] {"/P1/classFolder.jar"}, "");
		IFolder folder = createFolder("/P1/classFolder.jar/p");
		createFile("/P1/classFolder.jar/X.class", "p");

		// populate cache with a valid package fragment root and a valid package fragment
		IPackageFragment validPkg = p.getPackageFragmentRoot(folder.getParent()).getPackageFragment("p");
		validPkg.open(null);

		// create an invalid package fragment root and an invalid package fragment
		IPackageFragment invalidPkg = p.getPackageFragmentRoot("/P1/classFolder.jar").getPackageFragment("p");

		// ensure that the class file cannot be opened with a valid exception
		IOrdinaryClassFile openable = invalidPkg.getOrdinaryClassFile("X.class");
		JavaModelException expected = null;
		try {
			openable.open(null);
		} catch (JavaModelException e) {
			expected = e;
		}
		assertExceptionEquals("Unexpected exception", new Path("/P1/classFolder.jar").toOSString() + " does not exist", expected);
	} finally {
		deleteProject("P1");
	}
}

/*
 * Ensures that the parameter names of a binary method with source attached are correct.
 */
public void testParameterNames01() throws CoreException {
	IMethod method = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile("X.class").getType().getMethod("foo", new String[] {"TK;", "TV;"});
	String[] parameterNames = method.getParameterNames();
	assertStringsEqual(
		"Unexpected parameter names",
		"key\n" +
		"value\n",
		parameterNames);
}

/*
 * Ensures that the parameter names of a binary method without source attached are correct.
 */
public void testParameterNames02() throws CoreException {
	IPath sourceAttachmentPath = this.jarRoot.getSourceAttachmentPath();
	try {
		attachSource(this.jarRoot, null, null);
		IMethod method = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile("X.class").getType().getMethod("foo", new String[] {"TK;", "TV;"});
		String[] parameterNames = method.getParameterNames();
		assertStringsEqual(
			"Unexpected parameter names",
			"key\n" +
			"value\n",
			parameterNames);
	} finally {
		attachSource(this.jarRoot, sourceAttachmentPath.toString(), null);
	}
}

/**
 * Ensure that the type parameter signatures of a binary type are correct.
 */
public void testParameterTypeSignatures1() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile("X.class").getType();
	assertStringsEqual(
		"Unexpected type parameters",
		"T:Ljava.lang.Object;\n",
		type.getTypeParameterSignatures());
}

/**
 * Ensure that the type parameter signatures of a binary type are correct.
 */
public void testParameterTypeSignatures2() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("nongeneric").getOrdinaryClassFile("A.class").getType();
	assertStringsEqual(
		"Unexpected type parameters",
		"",
		type.getTypeParameterSignatures());
}

/**
 * Ensure that the type parameter signatures of a binary type are correct.
 */
public void testParameterTypeSignatures3() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile("Y.class").getType();
	assertStringsEqual(
		"Unexpected type parameters",
		"K:Ljava.lang.Object;\n" +
		"L:Ljava.lang.Object;\n",
		type.getTypeParameterSignatures());
}

/**
 * Ensure that the type parameter signatures of a binary type are correct.
 */
public void testParameterTypeSignatures4() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile("Z.class").getType();
	assertStringsEqual(
		"Unexpected type parameters",
		"T:Ljava.lang.Object;:Lgeneric.I<-TT;>;\n",
		type.getTypeParameterSignatures());
}

/**
 * Ensure that the type parameter signatures of a binary type are correct.
 */
public void testParameterTypeSignatures5() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile("W.class").getType();
	assertStringsEqual(
		"Unexpected type parameters",
		"T:Lgeneric.X<TT;>;\n" +
		"U:TT;\n",
		type.getTypeParameterSignatures());
}

/**
 * Ensure that the type parameter signatures of a binary method are correct.
 * @deprecated
 */
public void testParameterTypeSignatures6() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"TK;", "TV;"});
	assertStringsEqual(
		"Unexpected type parameters",
		"K:Ljava.lang.Object;\n" +
		"V:TT;\n",
		method.getTypeParameterSignatures());
}

/*
 * Ensures that the raw parameter names of a binary method with source attached are correct.
 */
public void testRawParameterNames01() throws CoreException {
	IMethod method = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile("X.class").getType().getMethod("foo", new String[] {"TK;", "TV;"});
	String[] parameterNames = method.getRawParameterNames();
	assertStringsEqual(
		"Unexpected parameter names",
		"arg0\n" +
		"arg1\n",
		parameterNames);
}

/*
 * Ensures that the raw parameter names of a binary method without source attached are correct.
 */
public void testRawParameterNames02() throws CoreException {
	IPath sourceAttachmentPath = this.jarRoot.getSourceAttachmentPath();
	try {
		attachSource(this.jarRoot, null, null);
		IMethod method = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile("X.class").getType().getMethod("foo", new String[] {"TK;", "TV;"});
		String[] parameterNames = method.getParameterNames();
		assertStringsEqual(
			"Unexpected parameter names",
			"key\n" +
			"value\n",
			parameterNames);
	} finally {
		attachSource(this.jarRoot, sourceAttachmentPath.toString(), null);
	}
}

/*
 * Ensure that the return type of a binary method is correct.
 */
public void testReturnType1() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"TK;", "TV;"});
	assertEquals(
		"Unexpected return type",
		"TV;",
		method.getReturnType());
}

/*
 * Ensure that the return type of a binary method is correct.
 */
public void testReturnType2() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo", new String[] {"Lgeneric.X<TT;>;"});
	assertEquals(
		"Unexpected return type",
		"Lgeneric.X<TT;>;",
		method.getReturnType());
}

/*
 * Ensures that asking for the source range of a IOrdinaryClassFile in a non-Java project throws a JavaModelException
 * (regression test for bug 132494 JavaModelException opening up class file in non java project)
 */
public void testSourceRange1() throws CoreException { // was testSourceRangeNonJavaProject()
	try {
		createProject("Simple");
		createFile("/Simple/X.class", "");
		IOrdinaryClassFile classX = getClassFile("/Simple/X.class");
		JavaModelException exception = null;
		try {
			classX.getSourceRange();
		} catch (JavaModelException e) {
			exception = e;
		}
		assertExceptionEquals("Unexpected exception", "Simple does not exist", exception);
	} finally {
		deleteProject("Simple");
	}
}

/*
 * Ensures that asking for the source range of a IOrdinaryClassFile not on the classpath of a Java project doesn't throw a JavaModelException
 * (regression test for bug 138507 exception in .class file editor for classes imported via plug-in import)
 */
public void testSourceRange2() throws CoreException { // was testSourceRangeNotOnClasspath()
	try {
		createJavaProject("P2", new String[] {"src"}, "bin");
		createFile("/P2/bin/X.class", "");
		IOrdinaryClassFile classX = getClassFile("/P2/bin/X.class");
		assertNull("Unxepected source range", classX.getSourceRange());
	} finally {
		deleteProject("P2");
	}
}

/*
 * Ensures that asking for the source range of a IOrdinaryClassFile in proj==src case without the corresponding .java file doesn't throw a JavaModelException
 * (regression test for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=221904 )
 */
public void testSourceRange3() throws CoreException {
	try {
		createJavaProject("P2", new String[] {""}, "");
		createFile("/P2/X.class", "");
		IOrdinaryClassFile classX = getClassFile("/P2/X.class");
		assertNull("Unxepected source range", classX.getSourceRange());
	} finally {
		deleteProject("P2");
	}
}

/*
 * Ensure that opening a binary type parameter when its parent has not been open yet
 * doesn't throw a JavaModelException
 * (regression test for bug 101228 JME on code assist)
 */
public void testTypeParameter() throws CoreException {
	IOrdinaryClassFile clazz = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile("X.class");
	ITypeParameter typeParameter = clazz.getType().getTypeParameter("T");
	clazz.close();
	assertStringsEqual(
		"Unexpected bounds",
		"java.lang.Object\n",
		typeParameter.getBounds());
}

/*
 * Ensure that a method with varargs has the AccVarargs flag set.
 */
public void testVarargs() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("varargs").getOrdinaryClassFile("X.class").getType();
	IMethod method = type.getMethod("foo", new String[]{"Ljava.lang.String;", "[Ljava.lang.Object;"});
	assertTrue("Should have the AccVarargs flag set", Flags.isVarargs(method.getFlags()));
}

/*
 * Ensures that a class file can be turned into a working copy and that its children are correct.
 */
public void testWorkingCopy01() throws CoreException {
	IOrdinaryClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getOrdinaryClassFile("X.class");
	this.workingCopy = clazz.getWorkingCopy(null/*primary owner*/, (IProgressMonitor) null/*no progress*/);
	assertElementDescendants(
		"Unexpected children",
		"[Working copy] X.class\n" +
		"  package workingcopy\n" +
		"  class X\n" +
		"    void foo()",
		this.workingCopy);
}

/*
 * Ensures that a class file without source attached can be turned into a working copy and that its children are correct.
 */
public void testWorkingCopy02() throws CoreException {
	IPath sourceAttachmentPath = this.jarRoot.getSourceAttachmentPath();
	try {
		attachSource(this.jarRoot, null, null);
		IOrdinaryClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getOrdinaryClassFile("X.class");
		assertNull("Should not have source attached", clazz.getSource());
		this.workingCopy = clazz.getWorkingCopy(null/*primary owner*/, (IProgressMonitor) null/*no progress*/);
		assertElementDescendants(
			"Unexpected children",
			"[Working copy] X.class\n" +
			"  package workingcopy\n" +
			"  class X\n" +
			"    X()\n" +
			"    void foo()",
			this.workingCopy);
	} finally {
		attachSource(this.jarRoot, sourceAttachmentPath.toString(), null);
	}
}

/*
 * Ensures that a class file can be turned into a working copy, modified and that its children are correct.
 */
public void testWorkingCopy03() throws CoreException {
	IOrdinaryClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getOrdinaryClassFile("X.class");
	this.workingCopy = clazz.getWorkingCopy(null/*primary owner*/, (IProgressMonitor) null/*no progress*/);
	this.workingCopy.getBuffer().setContents(
		"package workingcopy;\n" +
		"public class X {\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false/*don't force problems*/, null/*primary owner*/, null/*no progress*/);
	assertElementDescendants(
		"Unexpected children",
		"[Working copy] X.class\n" +
		"  package workingcopy\n" +
		"  class X\n" +
		"    void bar()",
		this.workingCopy);
}

/*
 * Ensures that a class file working copy cannot be commited
 */
public void testWorkingCopy04() throws CoreException {
	IOrdinaryClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getOrdinaryClassFile("X.class");
	this.workingCopy = clazz.getWorkingCopy(null/*primary owner*/, (IProgressMonitor) null/*no progress*/);
	this.workingCopy.getBuffer().setContents(
		"package workingcopy;\n" +
		"public class X {\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}"
	);
	JavaModelException exception = null;
	try {
		this.workingCopy.commitWorkingCopy(false/*don't force*/, null);
	} catch (JavaModelException e) {
		exception = e;
	}
	assertEquals(
		"Unxepected JavaModelException",
		"Java Model Exception: Error in Java Model (code 967): Operation not supported for specified element type(s):[Working copy] X.class [in workingcopy [in lib.jar [in P]]]",
		exception.toString());
}

/*
 * Ensures that a type can be created in class file working copy.
 */
public void testWorkingCopy05() throws CoreException {
	IOrdinaryClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getOrdinaryClassFile("X.class");
	this.workingCopy = clazz.getWorkingCopy(null/*primary owner*/, (IProgressMonitor) null/*no progress*/);
	this.workingCopy.createType(
		"class Y {\n" +
		"}",
		null,
		false/*don't force*/,
		null);
	assertElementDescendants(
		"Unexpected children",
		"[Working copy] X.class\n" +
		"  package workingcopy\n" +
		"  class X\n" +
		"    void foo()\n" +
		"  class Y",
		this.workingCopy);
}

/*
 * Ensures that the primary compilation unit of class file working copy is correct.
 */
public void testWorkingCopy06() throws CoreException {
	IOrdinaryClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getOrdinaryClassFile("X.class");
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopy = clazz.getWorkingCopy(owner, null/*no progress*/);
	ICompilationUnit primary = this.workingCopy.getPrimary();
	assertEquals("Unexpected owner of primary working copy", null, primary.getOwner());
}

/*
 * Ensures that a class file working copy can be restored from the original source.
 */
public void testWorkingCopy07() throws CoreException {
	IOrdinaryClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getOrdinaryClassFile("X.class");
	WorkingCopyOwner owner = new WorkingCopyOwner() {};
	this.workingCopy = clazz.getWorkingCopy(owner, null/*no progress*/);
	this.workingCopy.getBuffer().setContents(
		"package workingcopy;\n" +
		"public class X {\n" +
		"  void bar() {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.reconcile(ICompilationUnit.NO_AST, false/*don't force problems*/, null/*primary owner*/, null/*no progress*/);
	this.workingCopy.restore();
	assertElementDescendants(
		"Unexpected children",
		"[Working copy] X.class\n" +
		"  package workingcopy\n" +
		"  class X\n" +
		"    void foo()",
		this.workingCopy);
}

/*
 * Ensures that a class file working copy can be reconciled against.
 */
public void testWorkingCopy08() throws CoreException {
	IOrdinaryClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getOrdinaryClassFile("X.class");
	ProblemRequestor problemRequestor = new ProblemRequestor();
	WorkingCopyOwner owner = newWorkingCopyOwner(problemRequestor);
	this.workingCopy = clazz.getWorkingCopy(owner, null/*no progress*/);
	this.workingCopy.getBuffer().setContents(
		"package workingcopy;\n" +
		"public class X {\n" +
		"  public void bar() {\n" +
		"  }\n" +
		"}"
	);
	this.workingCopy.makeConsistent(null);

	ICompilationUnit cu = getCompilationUnit("/P/Y.java");
	ICompilationUnit copy = null;
	try {
		copy = cu.getWorkingCopy(owner, null/*no progress*/);
		copy.getBuffer().setContents(
			"public class Y {\n" +
			"  void foo(workingcopy.X x) {\n" +
			"    x.bar();\n" +
			"  }\n" +
			"}"
		);
		problemRequestor.problems = new StringBuffer();
		problemRequestor.problemCount = 0;
		copy.reconcile(ICompilationUnit.NO_AST, false/*don't force problems*/, owner, null/*no progress*/);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"----------\n",
			problemRequestor);
	} finally {
		if (copy != null)
			copy.discardWorkingCopy();
	}
}

/*
 * Ensures that types in a class file are hidden when reconciling against if the class file working copy is empty.
 */
public void testWorkingCopy09() throws CoreException {
	IOrdinaryClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getOrdinaryClassFile("X.class");
	ProblemRequestor problemRequestor = new ProblemRequestor();
	WorkingCopyOwner owner = newWorkingCopyOwner(problemRequestor);
	this.workingCopy = clazz.getWorkingCopy(owner, null/*no progress*/);
	this.workingCopy.getBuffer().setContents(	"");
	this.workingCopy.makeConsistent(null);

	ICompilationUnit cu = getCompilationUnit("/P/Y.java");
	ICompilationUnit copy = null;
	try {
		copy = cu.getWorkingCopy(owner, /*problemRequestor, */null/*no prpgress*/);
		copy.getBuffer().setContents(
			"public class Y {\n" +
			"  workingcopy.X x;\n" +
			"}"
		);
		problemRequestor.problems = new StringBuffer();
		problemRequestor.problemCount = 0;
		copy.reconcile(ICompilationUnit.NO_AST, false/*don't force problems*/, owner, null/*no progress*/);
		assertProblems(
			"Unexpected problems",
			"----------\n" +
			"1. ERROR in /P/Y.java\n" +
			"workingcopy.X cannot be resolved to a type\n" +
			"----------\n",
			problemRequestor);
	} finally {
		if (copy != null)
			copy.discardWorkingCopy();
	}
}

/*
 * Ensures that a 1.5 class file without source attached can be turned into a working copy and that its source is correct.
 */
public void testWorkingCopy10() throws CoreException {
	IPath sourceAttachmentPath = this.jarRoot.getSourceAttachmentPath();
	try {
		attachSource(this.jarRoot, null, null);
		IOrdinaryClassFile clazz = this.jarRoot.getPackageFragment("workingcopy").getOrdinaryClassFile("Y.class");
		assertNull("Should not have source attached", clazz.getSource());
		this.workingCopy = clazz.getWorkingCopy(null/*primary owner*/, (IProgressMonitor) null/*no progress*/);
		assertSourceEquals(
			"Unexpected source",
			"package workingcopy;\n" +
			"public class Y<W> {\n" +
			"  \n" +
			"  public Y() {\n" +
			"  }\n" +
			"  \n" +
			"  <T> T foo(T t, java.lang.String... args) {\n" +
			"    return null;\n" +
			"  }\n" +
			"}",
			this.workingCopy.getSource());
	} finally {
		attachSource(this.jarRoot, sourceAttachmentPath.toString(), null);
	}
}

/*
 * Ensures that types in a class file are not found by a search if the class file working copy is empty.
 */
public void testWorkingCopy11() throws CoreException {
	IPackageFragment pkg = this.jarRoot.getPackageFragment("workingcopy");
	IOrdinaryClassFile clazz = pkg.getOrdinaryClassFile("X.class");
	this.workingCopy = clazz.getWorkingCopy(null/*primary owner*/, (IProgressMonitor) null/*no progress*/);
	this.workingCopy.getBuffer().setContents(	"");
	this.workingCopy.makeConsistent(null);

	IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {pkg});
	AbstractJavaSearchTests.JavaSearchResultCollector requestor = new AbstractJavaSearchTests.JavaSearchResultCollector();
	search("*", IJavaSearchConstants.TYPE, IJavaSearchConstants.DECLARATIONS, scope, requestor);
	assertSearchResults(
		"lib.jar workingcopy.Y",
		requestor);
}
/*
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=150244
 */
public void testGetBytes() throws CoreException {
	IPackageFragment pkg = this.jarRoot.getPackageFragment("workingcopy");
	IOrdinaryClassFile clazz = pkg.getOrdinaryClassFile("X.class");
	byte[] bytes = clazz.getBytes();
	assertNotNull("No bytes", bytes);
	int length = bytes.length;
	assertTrue("wrong size", length > 5);
	// sanity check: first four bytes are 0xCAFEBABE
	assertEquals("Wrong value", 0xCA, bytes[0] & 0xFF);
	assertEquals("Wrong value", 0xFE, bytes[1] & 0xFF);
	assertEquals("Wrong value", 0xBA, bytes[2] & 0xFF);
	assertEquals("Wrong value", 0xBE, bytes[3] & 0xFF);
}
/*
 * Ensures that the annotations of a binary field are correct
 */
public void testGenericFieldGetTypeSignature() throws JavaModelException {
	IType type = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile("GenericField.class").getType();
	IField field = type.getField("myField");
	assertEquals(
		"Wrong type signature",
		"Ljava.util.Collection<Ljava.lang.String;>;",
		field.getTypeSignature());
}

	public void testBug246594() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile(
				"Z.class").getType();
		ITypeParameter typeParam = type.getTypeParameter("T");
		assertNotNull(typeParam);
		assertStringsEqual("Type parameter bounds signatures",
				"Ljava.lang.Object;\n" +
				"Lgeneric.I<-TT;>;\n",
				typeParam.getBoundsSignatures());
	}

	public void testBug246594a() throws JavaModelException {
		IType type = this.jarRoot.getPackageFragment("generic").getOrdinaryClassFile(
				"X.class").getType();
		IMethod method = type.getMethod("foo", new String[] { "TK;", "TV;" });
		ITypeParameter typeParam = method.getTypeParameter("V");
		assertStringsEqual("Type parameter bounds signatures",
							"TT;\n", typeParam.getBoundsSignatures());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=316937
	public void testBug316937() throws Exception {
		try {
			IJavaProject project = getJavaProject("P");
			String[] pathAndContents = new String[] {
					"bug316937/Foo.java",
					"package bug316937;\n" + "public class Foo {\n"
							+ "	class Bar {\n"
							+ "		public Bar(int a, int b) {}\n" + "	}\n"
							+ "}\n" };
			addLibrary(project, "lib316937.jar", "src316937.zip",
					pathAndContents, JavaCore.VERSION_1_5);
			IPackageFragmentRoot packageFragRoot = project
					.getPackageFragmentRoot(getFile("/P/lib316937.jar"));

			IType type = packageFragRoot.getPackageFragment("bug316937")
					.getOrdinaryClassFile("Foo.class").getType();
			IType subType = type.getType("Bar");
			IMethod[] methods = subType.getMethods();
			assertEquals("Constructros", 1, methods.length);
			IMethod method = methods[0];
			String[] paramNames = method.getParameterNames();
			assertStringsEqual("Type parameter names", "a\n" + "b\n",
					paramNames);

			// Remove the source attachment
			IClasspathEntry[] rawClasspath = project.getRawClasspath();
			for (int index = 0; index < rawClasspath.length; index++) {
				IClasspathEntry entry = rawClasspath[index];
				if (entry.getPath().toString().endsWith("lib316937.jar")) {
					((ClasspathEntry) entry).sourceAttachmentPath = null;
				}
			}
			project.setRawClasspath(rawClasspath, null);

			packageFragRoot = project
					.getPackageFragmentRoot(getFile("/P/lib316937.jar"));
			type = packageFragRoot.getPackageFragment("bug316937")
					.getOrdinaryClassFile("Foo.class").getType();
			subType = type.getType("Bar");
			methods = subType.getMethods();
			assertEquals("Constructros", 1, methods.length);
			method = methods[0];
			paramNames = method.getParameterNames();
			assertStringsEqual("Type parameter names", "a\n" + "b\n",
					paramNames);
		} finally {
			removeLibrary(getJavaProject("P"), "lib316937.jar", "src316937.zip");
		}
	}
	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=372687
	 * Ensures that if more than one thread try to open a class file at the same time, the children are correct.
	 */
	public void testBug372687() throws CoreException {
		String expected = "X.class\n" +
						  "  class X\n" +
						  "    X()\n" +
						  "    void foo()";
		class GetClassThread extends Thread {
			public String childString;
			@Override
			public void run(){
				IOrdinaryClassFile clazz = ClassFileTests.this.jarRoot.getPackageFragment("workingcopy").getOrdinaryClassFile("X.class");
				try {
					this.childString = expandAll(clazz);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
		for (int i = 0; i < 10; i++) {
			GetClassThread th1 = new GetClassThread();
			GetClassThread th2 = new GetClassThread();
			GetClassThread th3 = new GetClassThread();
			th1.start();
			th2.start();
			th3.start();
			try {
				th1.join();
				th2.join();
				th3.join();
			} catch (InterruptedException e) {
				// ignore
			}
			assertEquals("Unexpected children", expected, th1.childString);
			assertEquals("Unexpected children", expected, th2.childString);
			assertEquals("Unexpected children", expected, th3.childString);
			IOrdinaryClassFile clazz = ClassFileTests.this.jarRoot.getPackageFragment("workingcopy").getOrdinaryClassFile("X.class");
			clazz.close();
		}
	}

}
