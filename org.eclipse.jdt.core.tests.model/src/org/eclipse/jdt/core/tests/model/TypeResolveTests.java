/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.LambdaExpression;
import org.eclipse.jdt.internal.core.LambdaMethod;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.NameLookup;
import org.eclipse.jdt.internal.core.NameLookup.Answer;
import org.eclipse.jdt.internal.core.SourceType;

import junit.framework.Test;

@SuppressWarnings({"rawtypes", "unchecked"})
public class TypeResolveTests extends ModifyingResourceTests {
	ICompilationUnit cu;
public TypeResolveTests(String name) {
	super(name);
}
private IType getType(IType[] types, String sourceTypeName) throws JavaModelException {
	for (int i = 0; i < types.length; i++) {
		IType sourceType = types[i];
		if (sourceType.getTypeQualifiedName().equals(sourceTypeName)) {
			return sourceType;
		} else if ((sourceType = getType(sourceType.getTypes(), sourceTypeName)) != null) {
			return sourceType;
		}
	}
	return null;
}
private IType getType(String sourceTypeName) throws JavaModelException {
	return getType(this.cu.getTypes(), sourceTypeName);
}
private String[][] resolveType(String typeName, String sourceTypeName) throws JavaModelException {
	IType sourceType = this.getType(sourceTypeName);
	assertTrue("Type " + sourceTypeName + " was not found", sourceType != null);
	return sourceType.resolveType(typeName);
}
protected void assertTypesEqual(String expected, String[][] types) {
	StringBuilder buffer = new StringBuilder();
	if(types != null) {
		for (int i = 0, length = types.length; i < length; i++) {
			String[] qualifiedName = types[i];
			String packageName = qualifiedName[0];
			if (packageName.length() > 0) {
				buffer.append(packageName);
				buffer.append(".");
			}
			buffer.append(qualifiedName[1]);
			if (i < length-1) {
				buffer.append("\n");
			}
		}
	} else {
		buffer.append("<null>");
	}
	String actual = buffer.toString();
	if (!expected.equals(actual)) {
	 	System.out.print(Util.displayString(actual, 2));
	 	System.out.println(",");
	}
	assertEquals(
		"Unexpected types",
		expected,
		actual);
}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#setUpSuite()
 */
@Override
public void setUpSuite() throws Exception {
	super.setUpSuite();
	setUpJavaProject("TypeResolve");
	this.cu = this.getCompilationUnit("TypeResolve", "src", "p", "TypeResolve.java");
	addLibrary("myLib.jar", "myLibsrc.zip", new String[] {
			"p1/X.java",
			"package p1;\n" +
			"public class X {\n" +
			"}",
			"p2/Y.java",
			"package p2;\n" +
			"import p1.X;\n" +
			"public class Y {\n" +
			"  class Member {\n" +
			"    X field;\n" +
			"  }\n" +
			"  X foo() {\n" +
			"   return new X() {};" +
			"  }\n" +
			"}",
		}, JavaCore.VERSION_1_4);
}
	static {
//		TESTS_NUMBERS = new int[] { 182, 183 };
//		TESTS_NAMES = new String[] { "testBug575503" };
	}
	public static Test suite() {
		return buildModelTestSuite(TypeResolveTests.class);
	}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.SuiteOfTestCases#tearDownSuite()
 */
@Override
public void tearDownSuite() throws Exception {
	deleteProject("TypeResolve");
	super.tearDownSuite();
}
/**
 * Resolve the type "B" within one of the secondary types.
 * (regression test for bug 23829 IType::resolveType incorrectly returns null)
 */
public void testResolveInSecondaryType() throws JavaModelException {
	IType type = getCompilationUnit("/TypeResolve/src/p3/B.java").getType("Test");
	String[][] types = type.resolveType("B");
	assertTypesEqual(
		"p3.B",
		types);
}
/**
 * Resolve the type "B" within one of its inner classes.
 */
public void testResolveMemberTypeInInner() throws JavaModelException {
	String[][] types = resolveType("B", "TypeResolve$A$B$D");
	assertTypesEqual(
		"p.TypeResolve.A.B",
		types);
}
/*
 * Resolve a parameterized type
 * (regression test for bug 94903 Error setting method breakpoint in 1.5 project)
 */
public void testResolveParameterizedType() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
		createFile(
			"/P/src/X.java",
			"public class X<T> {\n" +
			"  X<String> field;\n" +
			"}"
		);
		IType type = getCompilationUnit("/P/src/X.java").getType("X");
		String[][] types = type.resolveType("X<String>");
		assertTypesEqual(
			"X",
			types);
	} finally {
		deleteProject("P");
	}
}
/**
 * Resolve the type "C" within one of its sibling classes.
 */
public void testResolveSiblingTypeInInner() throws JavaModelException {
	String[][] types = resolveType("C", "TypeResolve$A$B");
	assertTypesEqual(
		"p.TypeResolve.A.C",
		types);
}
/*
 * Resolve the type "X" within a top level binary type.
 */
public void testResolveTypeInBinary1() throws JavaModelException {
	IType type = getPackageFragmentRoot("/TypeResolve/myLib.jar").getPackageFragment("p2").getOrdinaryClassFile("Y.class").getType();
	String[][] types = type.resolveType("X");
	assertTypesEqual(
		"p1.X",
		types);
}
/*
 * Resolve the type "X" within a member binary type.
 */
public void testResolveTypeInBinary2() throws JavaModelException {
	IType type = getPackageFragmentRoot("/TypeResolve/myLib.jar").getPackageFragment("p2").getOrdinaryClassFile("Y$Member.class").getType();
	String[][] types = type.resolveType("X");
	assertTypesEqual(
		"p1.X",
		types);
}
/*
 * Resolve the type "X" within an anonymous binary type.
 */
public void testResolveTypeInBinary3() throws JavaModelException {
	IType type = getPackageFragmentRoot("/TypeResolve/myLib.jar").getPackageFragment("p2").getOrdinaryClassFile("Y$1.class").getType();
	String[][] types = type.resolveType("X");
	assertTypesEqual(
		"p1.X",
		types);
}
/*
 * Resolve the type "int" within a member binary type with a constructor.
 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=212224 )
 */
public void testResolveTypeInBinary4() throws Exception {
	try {
		addLibrary("lib212224.jar", "lib212224src.zip", new String[] {
			"X212224.java",
			"public class X212224 {\n" +
			"  public class Member {\n" +
			"    Member(int i) {\n" +
			"    }\n" +
			"  }\n" +
			"}"
		}, "1.4");
		IType type = getPackageFragmentRoot("/TypeResolve/lib212224.jar").getPackageFragment("").getOrdinaryClassFile("X212224$Member.class").getType();
		String[][] types = type.resolveType("int");
		assertTypesEqual(
			"<null>",
			types);
	} finally {
		removeLibrary(this.currentProject, "lib212224.jar", "lib212224src.zip");
	}
}
/**
 * Resolve the type "X" with a type import for it
 * within an inner class
 */
public void testResolveTypeInInner() throws JavaModelException {
	String[][] types = resolveType("X", "TypeResolve$A");
	assertTypesEqual(
		"p1.X",
		types);
}
/**
 * Resolve the type "Object" within a local class.
 * (regression test for bug 48350 IType#resolveType(String) fails on local types)
 */
public void testResolveTypeInInner2() throws JavaModelException {
	IType type = getCompilationUnit("/TypeResolve/src/p5/A.java").getType("A").getMethod("foo", new String[] {}).getType("Local", 1);

	String[][] types = type.resolveType("Object");
	assertTypesEqual(
		"java.lang.Object",
		types);
}
/**
 * Resolve the type "String".
 */
public void testResolveTypeInJavaLang() throws JavaModelException {
	String[][] types = resolveType("String", "TypeResolve");
	assertTypesEqual(
		"java.lang.String",
		types);
}
/**
 * Resolve the type "Vector" with no imports.
 */
public void testResolveTypeWithNoImports() throws JavaModelException {
	String[][] types = resolveType("Vector", "TypeResolve");
	assertTypesEqual(
		"<null>",
		types);
}
/**
 * Resolve the type "Y" with an on-demand import.
 */
public void testResolveTypeWithOnDemandImport() throws JavaModelException {
	String[][] types = resolveType("Y", "TypeResolve");
	assertTypesEqual(
		"p2.Y",
		types);
}
/**
 * Resolve the type "X" with a type import for it.
 */
public void testResolveTypeWithTypeImport() throws JavaModelException {
	String[][] types = resolveType("X", "TypeResolve");
	assertTypesEqual(
		"p1.X",
		types);
}
/**
 * Resolve the type "String".
 */
public void test_ResolveString() throws JavaModelException {
	String[][] types = resolveType("String", "TypeResolve");
	assertTypesEqual(
		"java.lang.String",
		types);
}
/**
 * Resolve the type "A.Inner".
 */
public void testResolveInnerType1() throws JavaModelException {
	IType type = getCompilationUnit("/TypeResolve/src/p4/B.java").getType("B");
	String[][] types = type.resolveType("A.Inner");
	assertTypesEqual(
		"p4.A.Inner",
		types);
}
/**
 * Resolve the type "p4.A.Inner".
 */
public void testResolveInnerType2() throws JavaModelException {
	IType type = getCompilationUnit("/TypeResolve/src/p4/B.java").getType("B");
	String[][] types = type.resolveType("p4.A.Inner");
	assertTypesEqual(
		"p4.A.Inner",
		types);
}
/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=334783"
 */
public void testParamAnnotations() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
		String source = "package p;\n" +
				"public class X<T> {\n" +
				"	X<String> field;\n" +
				"	@Inject\n" +
				"	public void Test(@Default String processor) {}\n" +
				"}" +
				"@interface Inject{\n" +
				"}" +
				"@interface Default{\n" +
				"}";
		createFolder("/P/src/p");
		createFile(
			"/P/src/p/X.java",
			source
		);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		IJavaElement[] variable = ((ICodeAssist) unit).codeSelect(source.indexOf("processor"), "processor".length());

		assertEquals(1, variable.length);
		String annotationString = "@Default [in processor [in Test(String) [in X [in X.java [in p [in src [in P]]]]]]]";
		assertEquals(annotationString, ((LocalVariable)variable[0]).getAnnotations()[0].toString());
		IType type = unit.getType("X");

		IMethod method = type.getMethods()[0];
		assertEquals(annotationString, method.getParameters()[0].getAnnotations()[0].toString());
	} finally {
		deleteProject("P");
	}
}
/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=334783"
 */
public void testParamAnnotations2() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
		String[] pathAndContents = new String[]{"p/X.java",
				"package p;\n" +
				"public class X<T> {\n" +
				"	X<String> field;\n" +
				"	@Inject\n" +
				"	public void Test(@Default String processor) {}\n" +
				"}" +
				"@interface Inject{\n" +
				"}" +
				"@interface Default{\n" +
				"}"};
		addLibrary(project, "lib334783.jar", "libsrc.zip", pathAndContents, JavaCore.VERSION_1_5);

		waitForAutoBuild();
		IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/P/lib334783.jar"));
		IType type = root.getPackageFragment("p").getOrdinaryClassFile("X.class").getType();
		String annotationString = "@p.Default [in processor [in Test(java.lang.String) [in X [in X.class [in p [in lib334783.jar [in P]]]]]]]";

		IMethod method = type.getMethods()[1];
		assertEquals(annotationString, method.getParameters()[0].getAnnotations()[0].toString());
	} finally {
		deleteProject("P");
	}
}
/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=334783"
 */
public void testParamAnnotations3() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
		String source = "package p;\n" +
				"public class X<T> {\n" +
				"	X<String> field;\n" +
				"	@Inject\n" +
				"	public void Test(int i, @Default @Marker(id=1) String processor, int k) {}\n" +
				"}\n" +
				"@interface Inject{\n" +
				"}\n" +
				"@interface Marker {\n" +
				"	int id() default 0;\n" +
				"}\n" +
				"@interface Default{\n" +
				"}";
		createFolder("/P/src/p");
		createFile(
			"/P/src/p/X.java",
			source
		);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		IJavaElement[] variable = ((ICodeAssist) unit).codeSelect(source.indexOf("processor"), "processor".length());

		assertEquals(1, variable.length);
		String annotationString1 = "@Default [in processor [in Test(int, String, int) [in X [in X.java [in p [in src [in P]]]]]]]";
		String annotationString2 = "@Marker [in processor [in Test(int, String, int) [in X [in X.java [in p [in src [in P]]]]]]]";
		assertEquals(annotationString1, ((LocalVariable)variable[0]).getAnnotations()[0].toString());
		IType type = unit.getType("X");

		IMethod method = type.getMethods()[0];
		IAnnotation[] parameterAnnotations = method.getParameters()[1].getAnnotations();
		assertEquals("Wrong length", 2, parameterAnnotations.length);
		assertEquals(annotationString1, parameterAnnotations[0].toString());
		IAnnotation iAnnotation = parameterAnnotations[1];
		assertEquals(annotationString2, iAnnotation.toString());
		IMemberValuePair[] memberValuePairs = iAnnotation.getMemberValuePairs();
		assertEquals("Wrong number of pairs", 1, memberValuePairs.length);
		StringBuffer output = new StringBuffer();
		output.append(memberValuePairs[0].getMemberName());
		output.append(' ');
		output.append(memberValuePairs[0].getValue());
		assertEquals("Wrong value", "id 1", String.valueOf(output));
		assertEquals("Wrong length", 0, method.getParameters()[0].getAnnotations().length);
		assertEquals("Wrong length", 0, method.getParameters()[2].getAnnotations().length);
	} finally {
		deleteProject("P");
	}
}
/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=334783"
 */
public void testParamAnnotations4() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
		String sourceX =
				"package p;\n" +
				"public class X<T> {\n" +
				"	X<String> field;\n" +
				"	@Inject @Marker(id=3)\n" +
				"	public void Test(final int i, @Default final @Marker(id=1) String processor, int k) {}\n" +
				"}";
		String[] pathAndContents = new String[]{"p/X.java",
				sourceX,
				"p/Inject.java",
				"package p;\n"+
				"public @interface Inject{\n" +
				"}",
				"p/Marker.java",
				"package p;\n" +
				"public @interface Marker {\n" +
				"	int id() default 0;\n" +
				"}",
				"p/Default.java",
				"package p;\n" +
				"public @interface Default{\n" +
				"}"};
		addLibrary(project, "lib334783_2.jar", "lib334783_2src.zip", pathAndContents, JavaCore.VERSION_1_5);

		waitForAutoBuild();
		IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/P/lib334783_2.jar"));
		IType type = root.getPackageFragment("p").getOrdinaryClassFile("X.class").getType();
		String annotationString1 = "@p.Default [in processor [in Test(int, java.lang.String, int) [in X [in X.class [in p [in lib334783_2.jar [in P]]]]]]]";
		String annotationString2 = "@p.Marker [in processor [in Test(int, java.lang.String, int) [in X [in X.class [in p [in lib334783_2.jar [in P]]]]]]]";
		IMethod method = type.getMethods()[1];
		IAnnotation[] annotations = method.getAnnotations();
		assertEquals("Wrong length", 2, annotations.length);
		assertEquals("@p.Inject [in Test(int, java.lang.String, int) [in X [in X.class [in p [in lib334783_2.jar [in P]]]]]]", annotations[0].toString());
		IAnnotation annotation = annotations[1];
		assertEquals("@p.Marker [in Test(int, java.lang.String, int) [in X [in X.class [in p [in lib334783_2.jar [in P]]]]]]", annotation.toString());
		IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
		assertEquals("Wrong number of pairs", 1, memberValuePairs.length);
		StringBuffer output = new StringBuffer();
		output.append(memberValuePairs[0].getMemberName());
		output.append(' ');
		output.append(memberValuePairs[0].getValue());
		assertEquals("Wrong value", "id 3", String.valueOf(output));
		ILocalVariable localVariable = method.getParameters()[1];
		ISourceRange sourceRange = localVariable.getSourceRange();
		String localSource = sourceX.substring(
				sourceRange.getOffset(),
				sourceRange.getOffset() + sourceRange.getLength());
		assertEquals("Wrong source", "@Default final @Marker(id=1) String processor", localSource);
		assertTrue("Wrong modifiers", Flags.isFinal(localVariable.getFlags()));
		IAnnotation[] parameterAnnotations = localVariable.getAnnotations();
		assertEquals("Wrong length", 2, parameterAnnotations.length);
		assertEquals(annotationString1, parameterAnnotations[0].toString());
		annotation = parameterAnnotations[1];
		assertEquals(annotationString2, annotation.toString());
		memberValuePairs = annotation.getMemberValuePairs();
		assertEquals("Wrong number of pairs", 1, memberValuePairs.length);
		output = new StringBuffer();
		output.append(memberValuePairs[0].getMemberName());
		output.append(' ');
		output.append(memberValuePairs[0].getValue());
		assertEquals("Wrong value", "id 1", String.valueOf(output));
		localVariable = method.getParameters()[0];
		assertEquals("Wrong length", 0, localVariable.getAnnotations().length);
		assertTrue("Wrong modifiers", Flags.isFinal(localVariable.getFlags()));
		assertEquals("Wrong length", 0, method.getParameters()[2].getAnnotations().length);
	} finally {
		deleteProject("P");
	}
}
/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=334783"
 */
public void testParamAnnotations5() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
		String[] pathAndContents = new String[]{"p/X.java",
				"package p;\n" +
				"public class X<T> {\n" +
				"	X<String> field;\n" +
				"	@Inject @Marker(id=3)\n" +
				"	public void Test(int i, @Default @Marker(id=1) String processor, int k) {}\n" +
				"}",
				"p/Inject.java",
				"package p;\n"+
				"public @interface Inject{\n" +
				"}",
				"p/Marker.java",
				"package p;\n" +
				"public @interface Marker {\n" +
				"	int id() default 0;\n" +
				"}",
				"p/Default.java",
				"package p;\n" +
				"public @interface Default{\n" +
				"}"};
		Map options = new HashMap();
		options.put(JavaCore.COMPILER_LOCAL_VARIABLE_ATTR, JavaCore.DO_NOT_GENERATE);
		addLibrary(project, "lib334783_3.jar", "lib334783_3src.zip", pathAndContents, JavaCore.VERSION_1_5, options);

		waitForAutoBuild();
		IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/P/lib334783_3.jar"));
		IType type = root.getPackageFragment("p").getOrdinaryClassFile("X.class").getType();
		String annotationString1 = "@p.Default [in arg1 [in Test(int, java.lang.String, int) [in X [in X.class [in p [in lib334783_3.jar [in P]]]]]]]";
		String annotationString2 = "@p.Marker [in arg1 [in Test(int, java.lang.String, int) [in X [in X.class [in p [in lib334783_3.jar [in P]]]]]]]";
		IMethod method = type.getMethods()[1];
		IAnnotation[] annotations = method.getAnnotations();
		assertEquals("Wrong length", 2, annotations.length);
		assertEquals("@p.Inject [in Test(int, java.lang.String, int) [in X [in X.class [in p [in lib334783_3.jar [in P]]]]]]", annotations[0].toString());
		IAnnotation annotation = annotations[1];
		assertEquals("@p.Marker [in Test(int, java.lang.String, int) [in X [in X.class [in p [in lib334783_3.jar [in P]]]]]]", annotation.toString());
		IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
		assertEquals("Wrong number of pairs", 1, memberValuePairs.length);
		StringBuffer output = new StringBuffer();
		output.append(memberValuePairs[0].getMemberName());
		output.append(' ');
		output.append(memberValuePairs[0].getValue());
		assertEquals("Wrong value", "id 3", String.valueOf(output));
		IAnnotation[] parameterAnnotations = method.getParameters()[1].getAnnotations();
		assertEquals("Wrong length", 2, parameterAnnotations.length);
		assertEquals(annotationString1, parameterAnnotations[0].toString());
		annotation = parameterAnnotations[1];
		assertEquals(annotationString2, annotation.toString());
		memberValuePairs = annotation.getMemberValuePairs();
		assertEquals("Wrong number of pairs", 1, memberValuePairs.length);
		output = new StringBuffer();
		output.append(memberValuePairs[0].getMemberName());
		output.append(' ');
		output.append(memberValuePairs[0].getValue());
		assertEquals("Wrong value", "id 1", String.valueOf(output));
		assertEquals("Wrong length", 0, method.getParameters()[0].getAnnotations().length);
		assertEquals("Wrong length", 0, method.getParameters()[2].getAnnotations().length);
	} finally {
		deleteProject("P");
	}
}
/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=334783"
 */
public void testParamAnnotations6() throws CoreException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
		String source = "package p;\n" +
				"public class X<T> {\n" +
				"	X<String> field;\n" +
				"	public void Test() {}\n" +
				"}";
		createFolder("/P/src/p");
		createFile(
			"/P/src/p/X.java",
			source
		);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		IType type = unit.getType("X");
		IMethod method = type.getMethods()[0];
		ILocalVariable[] localVariables = method.getParameters();
		assertNotNull(localVariables);
		assertEquals("Wrong length", 0, localVariables.length);
	} finally {
		deleteProject("P");
	}
}
/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=334783"
 */
public void testParamAnnotations7() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
		String[] pathAndContents = new String[]{"p/X.java",
				"package p;\n" +
				"public class X<T> {\n" +
				"	X<String> field;\n" +
				"	public void Test() {}\n" +
				"}"
		};
		addLibrary(project, "lib334783.jar", "libsrc.zip", pathAndContents, JavaCore.VERSION_1_5);

		waitForAutoBuild();
		IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/P/lib334783.jar"));
		IType type = root.getPackageFragment("p").getOrdinaryClassFile("X.class").getType();

		IMethod method = type.getMethods()[1];
		ILocalVariable[] localVariables = method.getParameters();
		assertNotNull(localVariables);
		assertEquals("Wrong length", 0, localVariables.length);
	} finally {
		deleteProject("P");
	}
}
/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=334783"
 */
public void testParamAnnotations8() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
		String[] pathAndContents = new String[]{"p/X.java",
				"package p;\n" +
				"public class X<T> {\n" +
				"	X<String> field;\n" +
				"	@Inject @Marker(id=3)\n" +
				"	public X(int i, @Default @Marker(id=1) String processor, int k) {}\n" +
				"}",
				"p/Inject.java",
				"package p;\n"+
				"public @interface Inject{\n" +
				"}",
				"p/Marker.java",
				"package p;\n" +
				"public @interface Marker {\n" +
				"	int id() default 0;\n" +
				"}",
				"p/Default.java",
				"package p;\n" +
				"public @interface Default{\n" +
				"}"};
		Map options = new HashMap();
		options.put(JavaCore.COMPILER_LOCAL_VARIABLE_ATTR, JavaCore.DO_NOT_GENERATE);
		addLibrary(project, "lib334783_3.jar", "lib334783_3src.zip", pathAndContents, JavaCore.VERSION_1_5, options);

		waitForAutoBuild();
		IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/P/lib334783_3.jar"));
		IType type = root.getPackageFragment("p").getOrdinaryClassFile("X.class").getType();
		String annotationString1 = "@p.Default [in arg1 [in X(int, java.lang.String, int) [in X [in X.class [in p [in lib334783_3.jar [in P]]]]]]]";
		String annotationString2 = "@p.Marker [in arg1 [in X(int, java.lang.String, int) [in X [in X.class [in p [in lib334783_3.jar [in P]]]]]]]";
		IMethod method = type.getMethods()[0];
		IAnnotation[] annotations = method.getAnnotations();
		assertEquals("Wrong length", 2, annotations.length);
		assertEquals("@p.Inject [in X(int, java.lang.String, int) [in X [in X.class [in p [in lib334783_3.jar [in P]]]]]]", annotations[0].toString());
		IAnnotation annotation = annotations[1];
		assertEquals("@p.Marker [in X(int, java.lang.String, int) [in X [in X.class [in p [in lib334783_3.jar [in P]]]]]]", annotation.toString());
		IMemberValuePair[] memberValuePairs = annotation.getMemberValuePairs();
		assertEquals("Wrong number of pairs", 1, memberValuePairs.length);
		StringBuffer output = new StringBuffer();
		output.append(memberValuePairs[0].getMemberName());
		output.append(' ');
		output.append(memberValuePairs[0].getValue());
		assertEquals("Wrong value", "id 3", String.valueOf(output));
		IAnnotation[] parameterAnnotations = method.getParameters()[1].getAnnotations();
		assertEquals("Wrong length", 2, parameterAnnotations.length);
		assertEquals(annotationString1, parameterAnnotations[0].toString());
		annotation = parameterAnnotations[1];
		assertEquals(annotationString2, annotation.toString());
		memberValuePairs = annotation.getMemberValuePairs();
		assertEquals("Wrong number of pairs", 1, memberValuePairs.length);
		output = new StringBuffer();
		output.append(memberValuePairs[0].getMemberName());
		output.append(' ');
		output.append(memberValuePairs[0].getValue());
		assertEquals("Wrong value", "id 1", String.valueOf(output));
		assertEquals("Wrong length", 0, method.getParameters()[0].getAnnotations().length);
		assertEquals("Wrong length", 0, method.getParameters()[2].getAnnotations().length);
	} finally {
		deleteProject("P");
	}
}
/**
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=375568"
 */
public void testParamAnnotations9() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
		String[] pathAndContents = new String[]{"p/X.java",
				"package p;\n" +
				"public class X {\n" +
				"	X field;\n" +
				"	@Deprecated\n" +
				"	public void Test(@Default String processor) {}\n" +
				"}" +
				"@interface Default{\n" +
				"}"};
		addLibrary(project, "lib334783.jar", "libsrc.zip", pathAndContents, JavaCore.VERSION_1_5);

		waitForAutoBuild();
		IPackageFragmentRoot root = project.getPackageFragmentRoot(getFile("/P/lib334783.jar"));
		IType type = root.getPackageFragment("p").getOrdinaryClassFile("X.class").getType();
		String annotationString = "@p.Default [in processor [in Test(java.lang.String) [in X [in X.class [in p [in lib334783.jar [in P]]]]]]]";

		IMethod method = type.getMethods()[1];
		assertEquals(1, method.getParameters()[0].getAnnotations().length);
		assertEquals(annotationString, method.getParameters()[0].getAnnotations()[0].toString());
	} finally {
		deleteProject("P");
	}
}
/**
 * @bug 342393: Anonymous class' occurrence count is incorrect when two methods in a class have the same name.
 *
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=342393"
 */
public void testBug342393() throws Exception {
	try {
		IJavaProject project = createJavaProject("Test342393", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
		project.open(null);
			String fileContent =  "package p;\n"
					 + "public class Test {\n"
					 + "Test() {\n"
					 + "    class A {\n"
					 + "    	// one \n"
					 + "        public void foo() {\n"
					 + "            Throwable a1 = new Throwable(){ // two \n"
					 + "            };\n"
					 + "            Throwable b1 = new Throwable(){ // three \n"
					 + "            };\n"
					 + "        }\n"
					 + "        public void bar() {\n"
					 + "            Throwable b2 = new Throwable(){ // four\n"
					 + "            	Throwable bi2 = new Throwable() { // five\n"
					 + "            	};\n"
					 + "            };\n"
					 + "        }\n"
					 + "        class B {\n"
					 + "        	Throwable t1 = new Throwable() { // six\n"
					 + "        	};\n"
					 + "        	Throwable t2 = new Throwable() { // seven\n"
					 + "        	};\n"
					 + "        }\n"
					 + "    };\n"
					 + "    {\n"
					 + "        Throwable a3 = new Throwable(){ // eight\n"
					 + "        	Throwable ai3 = new Throwable() { // nine\n"
					 + "        	};\n"
					 + "        };\n"
					 + "    }\n"
					 + "}\n"
					 + "public static void main(String[] args) throws Exception {\n"
					 + "	Throwable c1 = new Throwable() { // ten\n"
					 + "	};\n"
					 + "	Throwable c2 = new Throwable() { // eleven\n"
					 + "	};\n"
					 + "}\n"
					 + "}\n";
			createFolder("/Test342393/src/p");
			createFile(	"/Test342393/src/p/Test.java",	fileContent);

			ICompilationUnit unit = getCompilationUnit("/Test342393/src/p/Test.java");
			int index = fileContent.indexOf("// one");
			IJavaElement element = unit.getElementAt(index);
			assertEquals("Incorrect Type selected", "p.Test$A", ((SourceType)element.getParent()).getFullyQualifiedName());
			index = fileContent.indexOf("// two");
			element = unit.getElementAt(index);
			assertEquals("Incorrect Type selected", "p.Test$A$1", ((SourceType)element).getFullyQualifiedName());
			index = fileContent.indexOf("// three");
			element = unit.getElementAt(index);
			assertEquals("Incorrect Type selected", "p.Test$A$2", ((SourceType)element).getFullyQualifiedName());
			index = fileContent.indexOf("// four");
			element = unit.getElementAt(index);
			assertEquals("Incorrect Type selected", "p.Test$A$3", ((SourceType)element).getFullyQualifiedName());
			index = fileContent.indexOf("// five");
			element = unit.getElementAt(index);
			assertEquals("Incorrect Type selected", "p.Test$A$3$1", ((SourceType)element).getFullyQualifiedName());
			index = fileContent.indexOf("// six");
			element = unit.getElementAt(index);
			assertEquals("Incorrect Type selected", "p.Test$A$B$1", ((SourceType)element).getFullyQualifiedName());
			index = fileContent.indexOf("// seven");
			element = unit.getElementAt(index);
			assertEquals("Incorrect Type selected", "p.Test$A$B$2", ((SourceType)element).getFullyQualifiedName());

			String handleId = ((SourceType)element).getHandleMemento();
			IJavaElement newElement = JavaCore.create(handleId);
			assertEquals("Incorrect Element", element, newElement);

			index = fileContent.indexOf("// eight");
			element = unit.getElementAt(index);
			assertEquals("Incorrect Type selected", "p.Test$1", ((SourceType)element).getFullyQualifiedName());
			index = fileContent.indexOf("// nine");
			element = unit.getElementAt(index);
			assertEquals("Incorrect Type selected", "p.Test$1$1", ((SourceType)element).getFullyQualifiedName());

			handleId = ((SourceType)element).getHandleMemento();
			newElement = JavaCore.create(handleId);
			assertEquals("Incorrect Element", element, newElement);

			index = fileContent.indexOf("// ten");
			element = unit.getElementAt(index);
			assertEquals("Incorrect Type selected", "p.Test$2", ((SourceType)element).getFullyQualifiedName());
			index = fileContent.indexOf("// eleven");
			element = unit.getElementAt(index);
			assertEquals("Incorrect Type selected", "p.Test$3", ((SourceType)element).getFullyQualifiedName());

			handleId = ((SourceType)element).getHandleMemento();
			newElement = JavaCore.create(handleId);
			assertEquals("Incorrect Element", element, newElement);
	}
	finally {
		deleteProject("Test342393");
	}
}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=377710
public void test377710() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
		String source = "public class Foo {}\n";
		createFile("/P/src/Foo.java", source);
		waitForAutoBuild();
		IType itype = project.findType(".Foo");
		assertNull(itype);
	} finally {
		deleteProject("P");
	}
}

//405026 - IJavaProject#findType(String) finds secondary type if editor is open
//Partial Match is not set for the API Calls.
public void test405026a() throws CoreException, IOException {
	try {
		JavaProject project = (JavaProject) createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
		String source = "package p;\n"  +
						"\n" +
						"public interface test13 {\n"  +
						"}\n"  +
						"\n"  +
						"/**\n" +
						" * @noreference\n"  +
						" */\n"  +
						"interface test13outer {}\n"  +
						"class Foo {}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/test13.java", source);
		waitForAutoBuild();
		IType itype = project.findType("p.test13");
		assertNotNull(itype);
		itype = project.findType("p.test13outer");
		assertNull("Should be a null", itype);

		ICompilationUnit[] workingCopy = new ICompilationUnit[1];
		workingCopy[0] = getWorkingCopy("/P/src/p/test13.java", source);
		NameLookup nameLookup = project.newNameLookup(workingCopy);
		itype = nameLookup.findType("p.test13", false, NameLookup.ACCEPT_ALL);
		assertEquals("test13", itype.getElementName());
		Answer answer = nameLookup.findType("p.test13outer", false, /*NameLookup.ACCEPT_ALL*/ NameLookup.ACCEPT_INTERFACES, false, true, false, null);
		assertNull(answer);

		itype = project.findType("p.test13outer", (IProgressMonitor) null);
		assertEquals("test13outer", itype.getElementName());

		itype = project.findType("p", "test13");
		assertEquals("test13", itype.getElementName());

		itype = project.findType("p", "test13outer");
		assertNull(itype);

		itype = project.findType("p.test13", workingCopy[0].getOwner());
		assertEquals("test13", itype.getElementName());

		itype = project.findType("p.test13outer", workingCopy[0].getOwner());
		assertNull(itype);

		itype = project.findType("p", "test13outer", (IProgressMonitor) null);
		assertEquals("test13outer", itype.getElementName());

		itype = project.findType("p", "test13", workingCopy[0].getOwner());
		assertEquals("test13", itype.getElementName());

		itype = project.findType("p", "test13outer", workingCopy[0].getOwner());
		assertNull(itype);

		itype = project.findType("p.test13outer", workingCopy[0].getOwner(), (IProgressMonitor) null);
		assertEquals("test13outer", itype.getElementName());

		itype = project.findType("p", "test13outer", workingCopy[0].getOwner(), (IProgressMonitor) null);
		assertEquals("test13outer", itype.getElementName());

		itype = nameLookup.findType("p.test13outer", false, NameLookup.ACCEPT_ALL);
		assertEquals("test13outer", itype.getElementName());

		answer = nameLookup.findType("p.test13outer", false, NameLookup.ACCEPT_ALL, false);
		assertEquals("test13outer", answer.type.getElementName());

		IPackageFragment[] packageFragments = project.newNameLookup(workingCopy[0].getOwner()).findPackageFragments("p", false);
		itype = nameLookup.findType("test13outer", packageFragments[0], false, NameLookup.ACCEPT_ALL);
		assertNull(itype);

		itype = nameLookup.findType("test13", packageFragments[0], false, NameLookup.ACCEPT_ALL);
		assertEquals("test13", itype.getElementName());

		answer = nameLookup.findType("test13outer", "p", false, NameLookup.ACCEPT_ALL, false);
		assertEquals("test13outer", answer.type.getElementName());

		answer = nameLookup.findType("test13", "p", false, NameLookup.ACCEPT_ALL, false);
		assertEquals("test13", answer.type.getElementName());

		itype = nameLookup.findType("test13outer", packageFragments[0], false, NameLookup.ACCEPT_ALL, false, /* considerSecondaryTypes */ true);
		assertEquals("test13outer", itype.getElementName());

		itype = nameLookup.findType("test13outer", packageFragments[0], false, NameLookup.ACCEPT_ALL, false, /* considerSecondaryTypes */ false);
		assertNull(itype);

		itype = nameLookup.findType("test13", packageFragments[0], false, NameLookup.ACCEPT_ALL, false, /* considerSecondaryTypes */ false);
		assertEquals("test13", itype.getElementName());

		answer = nameLookup.findType("p.test13", false, NameLookup.ACCEPT_ALL, /* considerSecondaryTypes */ false, true, false, null);
		assertEquals("test13", answer.type.getElementName());

		answer = nameLookup.findType("test13", "p", false, NameLookup.ACCEPT_ALL, /* considerSecondaryTypes */ false, true, false, null);
		assertEquals("test13", answer.type.getElementName());

		answer = nameLookup.findType("test13outer", "p", false, NameLookup.ACCEPT_ALL, /* considerSecondaryTypes */ false, true, false, null);
		assertNull(answer);

		answer = nameLookup.findType("test13outer", "p", false, NameLookup.ACCEPT_ALL, /* considerSecondaryTypes */ true, true, false, null);
		assertEquals("test13outer", answer.type.getElementName());
	} finally {
		deleteProject("P");
	}
}

//405026 - IJavaProject#findType(String) finds secondary type if editor is open
//Partial Match is set for the API's.
public void test405026b() throws CoreException, IOException {
	try {
		JavaProject project = (JavaProject) createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
		String source = "package p;\n"  +
						"\n" +
						"public interface test13 {\n"  +
						"}\n"  +
						"\n"  +
						"/**\n" +
						" * @noreference\n"  +
						" */\n"  +
						"interface test13outer {}\n"  +
						"class Foo {}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/test13.java", source);
		waitForAutoBuild();

		ICompilationUnit[] workingCopy = new ICompilationUnit[1];
		workingCopy[0] = getWorkingCopy("/P/src/p/test13.java", source);
		NameLookup nameLookup = project.newNameLookup(workingCopy);

		IType itype = nameLookup.findType("p.test13", true, NameLookup.ACCEPT_ALL);
		assertNotNull(itype);
		Answer answer = nameLookup.findType("p.test13outer", true, /*NameLookup.ACCEPT_ALL*/ NameLookup.ACCEPT_INTERFACES, false, true, false, null);
		itype = (answer == null) ? null : answer.type;
		assertNull("Should be a null", itype);
		itype = project.findType("p.test13");
		assertNotNull(itype);
		itype = project.findType("p.test13outer");
		assertNull("Should be a null", itype);

		IPackageFragment[] packageFragments = project.newNameLookup(workingCopy[0].getOwner()).findPackageFragments("p", false);
		itype = nameLookup.findType("test13outer", packageFragments[0], false, NameLookup.ACCEPT_ALL);
		assertNull(itype);

		itype = nameLookup.findType("test13o", packageFragments[0], true, NameLookup.ACCEPT_ALL, false, true);
		assertEquals("test13outer", itype.getElementName());

		itype = nameLookup.findType("test13outer", packageFragments[0], true, NameLookup.ACCEPT_ALL, false, false);
		assertNull(itype);

		itype = nameLookup.findType("test1", packageFragments[0], true, NameLookup.ACCEPT_ALL, false, false);
		assertEquals("test13", itype.getElementName());

		answer = nameLookup.findType("test13out", "p", true, NameLookup.ACCEPT_ALL, /* considerSecondaryTypes */ true, true, false, null);
		assertEquals("test13outer", answer.type.getElementName());
	} finally {
		deleteProject("P");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=433404
public void test433404() throws CoreException, IOException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB"}, "bin", "1.5");
		String source = "package p;\n"  +
						"public class X {\n"  +
						" 	FI fi = (i_) -> { return 0;};\n" +
						"}\n" +
						"interface FI {\n" +
						"	public int foo(int i);\n" +
						"}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String lambdaString = "i_";
		IJavaElement[] elements = unit.codeSelect(source.indexOf(lambdaString), lambdaString.length());
		assertEquals("Array size should be 1", 1, elements.length);
		ILocalVariable variable = (ILocalVariable) elements[0];
		elements = unit.findElements(variable);
		assertNull("Should be null", elements);
		LambdaMethod method = (LambdaMethod) variable.getParent();
		LambdaExpression lambda = (LambdaExpression) method.getParent();
		assertTrue("Should be a lambda", lambda.isLambda());
		elements = unit.findElements(lambda);
		assertNull("Should be null", elements);
	} finally {
		deleteProject("P");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=458613
public void testBug458613() throws CoreException, IOException {
	IJavaProject prj = null;
	try {
		prj = createJavaProject("Bug458613", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin", "1.8");
		createFolder("/Bug458613/src/p");
		String source = "package p;\n" +
				"interface I9<T> {\n" +
				"  void foo(T x);\n" +
				"}\n" +
				"public class X {\n" +
				"  public static void main(String[] args) {\n" +
				"    A.sort(new String[2], x_ -> { // not shown in Ctrl+T\n" +
				"    });\n" +
				"  }\n" +
				"}\n" +
				"class A {\n" +
				"  static <T> void sort(T[] t, I9<? super T> i9b) {}\n" +
				"}\n";
		createFile("/Bug458613/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/Bug458613/src/p/X.java");
		String lambdaString = "x_";
		IJavaElement[] elements = unit.codeSelect(source.indexOf(lambdaString), lambdaString.length());
		assertEquals("Array size should be 1", 1, elements.length);
		ILocalVariable variable = (ILocalVariable) elements[0];
		LambdaMethod method = (LambdaMethod) variable.getParent();
		LambdaExpression lambda = (LambdaExpression) method.getParent();
			assertEquals(
					"Incorrect handle",
					"=Bug458613/src<p{X.java[X~main~\\[QString;=)=\"Lp.I9\\<Ljava.lang.String;>;!134!169!138=&foo!1=\"Ljava.lang.String;=\"x_=\"V=\"Lp\\/X\\~I9\\"
					+ "<Ljava\\/lang\\/String;>;.foo\\(Ljava\\/lang\\/String;)V@x_!134!135!134!135!Ljava\\/lang\\/String;!0!true=)",
					lambda.getHandleMemento());
	} finally {
		if (prj != null)
			deleteProject(prj);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=458613
public void testBug458613b() throws CoreException, IOException {
	IJavaProject prj = null;
	try {
		prj = createJavaProject("Bug458613", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin", "1.8");
		createFolder("/Bug458613/src/p");
		String source = "package p;\n" +
				"interface I9<U, T> {\n" +
				"  void foo(T x);\n" +
				"}\n" +
				"interface I10<T> extends I9<Number,T> {}\n" +
				"public class X {\n" +
				"	I9<Number,String> i9a = x -> {};\n" +
				"  public static void main(String[] args) {\n" +
				"    B.sort(new String[2], x_ -> {\n" +
				"    });\n" +
				"  }\n" +
				"}\n" +
				"class B {\n" +
				"  static <T> void sort(T[] t, I10<? super T> i10) {} \n" +
				"}\n";
		createFile("/Bug458613/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/Bug458613/src/p/X.java");
		String lambdaString = "x_";
		IJavaElement[] elements = unit.codeSelect(source.indexOf(lambdaString), lambdaString.length());
		assertEquals("Array size should be 1", 1, elements.length);
		ILocalVariable variable = (ILocalVariable) elements[0];
		LambdaMethod method = (LambdaMethod) variable.getParent();
		LambdaExpression lambda = (LambdaExpression) method.getParent();
			assertEquals(
					"Incorrect handle",
					"=Bug458613/src<p{X.java[X~main~\\[QString;=)=\"Lp.I10\\<Ljava.lang.String;>;!212!224!216=&foo!1=\"Ljava.lang.String;=\"x_=\"V=\"Lp\\/X\\"
					+ "~I9\\<LNumber;Ljava\\/lang\\/String;>;.foo\\(Ljava\\/lang\\/String;)V@x_!212!213!212!213!Ljava\\/lang\\/String;!0!true=)",
					lambda.getHandleMemento());
	} finally {
		if (prj != null)
			deleteProject(prj);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=479963
public void test479963() throws CoreException, IOException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin", "1.8");
		String source = "package p;\n" +
						"public class X {\n" +
						"	public void foo() {\n" +
						"		bar(item -> System.out.println(item));\n" +
						"	}\n" +
						"	public void bar(FI fi) { /* Do nothing */ }\n" +
						"}\n" +
						"interface FI {\n" +
						"	public void foobar(String param);\n" +
						"}";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String lambdaString = "item";
		IJavaElement[] elements = unit.codeSelect(source.indexOf(lambdaString), lambdaString.length());
		assertEquals("Array size should be 1", 1, elements.length);
		ILocalVariable variable = (ILocalVariable) elements[0];
		elements = unit.findElements(variable);
		assertNull("Should be null", elements);
		LambdaMethod method = (LambdaMethod) variable.getParent();
		LambdaExpression lambda = (LambdaExpression) method.getParent();
		assertTrue("Should be a lambda", lambda.isLambda());
		String[][] types = lambda.resolveType("String");
		assertTypesEqual("java.lang.String", types);
	} finally {
		deleteProject("P");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=479963
public void test479963a() throws CoreException, IOException {
	try {
		createJavaProject("P", new String[] {"src"}, new String[] {"JCL_LIB"}, "bin", "1.8");
		String source = "package p;\n" +
						"public class X {\n" +
						"	public void foo() {\n" +
						"		new FI1() {\n" +
						"			public void foobar(String param) {\n" +
						"				bar(() -> item -> System.out.println(item));\n" +
						"			}\n" +
						"		};\n" +
						"	}\n" +
						"	public void bar(FI2 fi) { /* Do nothing */ }\n" +
						"}\n" +
						"interface FI1 {\n" +
						"	public void foobar(Object param);\n" +
						"}\n" +
						"interface FI2 {\n" +
						"	public FI1 get();\n" +
						"}";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String lambdaString = "item";
		IJavaElement[] elements = unit.codeSelect(source.indexOf(lambdaString), lambdaString.length());
		assertEquals("Array size should be 1", 1, elements.length);
		ILocalVariable variable = (ILocalVariable) elements[0];
		elements = unit.findElements(variable);
		assertNull("Should be null", elements);
		LambdaMethod method = (LambdaMethod) variable.getParent();
		LambdaExpression lambda = (LambdaExpression) method.getParent();
		assertTrue("Should be a lambda", lambda.isLambda());
		String[][] types = lambda.resolveType("Object");
		assertTypesEqual("java.lang.Object", types);
	} finally {
		deleteProject("P");
	}
}
public void test528818a() throws CoreException, IOException {
	if (!AbstractJavaModelTests.isJRE9) {
		return;
	}
	try {
		IJavaProject project = createJava9Project("P");
		waitForAutoBuild();
		IType type = project.findType("java.lang.annotation.Target");
		assertNotNull("Type should not be null", type);
		String[][] resolveType = type.resolveType("java.lang.Object");
		assertNotNull("Type should not be null", resolveType);
	} finally {
		deleteProject("P");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=479963
public void test531046a() throws CoreException, IOException {
	if (!isJRE9) return;
	try {
		createJava10Project("P", new String[] {"src"});
		String source =   "package p;\n"
				+ "public class X {\n"
				+ "  public static void main(java.lang.String[] args) {\n"
				+ "    var s1 = args[0];\n"
				+ "    System.out.println(s1);\n"
				+ "  }\n"
				+ "}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String select = "s1";
		IJavaElement[] elements = unit.codeSelect(source.indexOf(select), select.length());
		ILocalVariable variable = (ILocalVariable) elements[0];
		elements = unit.findElements(variable);
		assertNotNull("Should not be null", elements);
		assertEquals("incorrect type", "Ljava.lang.String;", variable.getTypeSignature());
	} finally {
		deleteProject("P");
	}
}
public void test531046b() throws CoreException, IOException {
	if (!isJRE9) return;
	try {
		createJava10Project("P", new String[] {"src"});
		String source =   "package p;\n"
				+ "public class X {\n"
				+ "  public static void main(java.lang.String[] args) {\n"
				+ "    var s1 = args[0];\n"
				+ "    System.out.println(s1);\n"
				+ "  }\n"
				+ "}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String select = "s1";
		IJavaElement[] elements = unit.codeSelect(source.lastIndexOf(select), select.length());
		ILocalVariable variable = (ILocalVariable) elements[0];
		elements = unit.findElements(variable);
		assertNotNull("Should not be null", elements);
		assertEquals("incorrect type", "Ljava.lang.String;", variable.getTypeSignature());
	} finally {
		deleteProject("P");
	}
}
public void test531046c() throws CoreException, IOException {
	if (!isJRE9) return;
	try {
		createJava10Project("P", new String[] {"src"});
		String source =   "package p;\n"
				+ "public class X {\n"
				+ "  public static void main(java.lang.String[] args) {\n"
				+ "    var s1 = args;\n"
				+ "    System.out.println(s1);\n"
				+ "  }\n"
				+ "}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String select = "s1";
		IJavaElement[] elements = unit.codeSelect(source.lastIndexOf(select), select.length());
		ILocalVariable variable = (ILocalVariable) elements[0];
		elements = unit.findElements(variable);
		assertNotNull("Should not be null", elements);
		assertEquals("incorrect type", "[Ljava.lang.String;", variable.getTypeSignature());
	} finally {
		deleteProject("P");
	}
}
public void test531046d() throws CoreException, IOException {
	if (!isJRE9) return;
	try {
		createJava10Project("P", new String[] {"src"});
		String source =   "package p;\n"
				+ "public class X {\n"
				+ "  public static void main(java.lang.String[] args) {\n"
				+ "    var s1 = new java.util.HashMap<String, Object>();\n"
				+ "  }\n"
				+ "}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String select = "s1";
		IJavaElement[] elements = unit.codeSelect(source.lastIndexOf(select), select.length());
		ILocalVariable variable = (ILocalVariable) elements[0];
		elements = unit.findElements(variable);
		assertNotNull("Should not be null", elements);
		assertEquals("incorrect type", "Ljava.util.HashMap<Ljava.lang.String;Ljava.lang.Object;>;", variable.getTypeSignature());
	} finally {
		deleteProject("P");
	}
}
public void test531046e() throws CoreException, IOException {
	if (!isJRE9) return;
	try {
		createJava10Project("P", new String[] {"src"});
		String source =   "package p;\n"
				+ "public class X {\n"
				+ "  public static void main(java.lang.String[] args) {\n"
				+ "    var s1 = new java.util.HashMap<String, Object>();\n"
				+ "  }\n"
				+ "}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String select = "var";
		IJavaElement[] elements = unit.codeSelect(source.lastIndexOf(select), select.length());
		assertEquals("should not be empty", 1, elements.length);
		IType type = (IType) elements[0];
		assertEquals("incorrect type", "java.util.HashMap<java.lang.String,java.lang.Object>", type.getFullyQualifiedParameterizedName());
	}  finally {
		deleteProject("P");
	}
}
public void test531046f() throws CoreException, IOException {
	if (!isJRE9) return;
	try {
		createJava10Project("P", new String[] {"src"});
		String source =   "package p;\n"
				+ "public class X {\n"
				+ "  public static void main(java.lang.String[] args) {\n"
				+ "    var e = (CharSequence & Comparable<String>) \"x\";\n"
				+ "  }\n"
				+ "}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String select = "var";
		IJavaElement[] elements = unit.codeSelect(source.lastIndexOf(select), select.length());
		assertEquals("should not be empty", 2, elements.length);
		IType type = (IType) elements[0];
		assertEquals("incorrect type", "java.lang.CharSequence", type.getFullyQualifiedParameterizedName());
		type = (IType) elements[1];
		assertEquals("incorrect type", "java.lang.Comparable<java.lang.String>", type.getFullyQualifiedParameterizedName());
	} finally {
		deleteProject("P");
	}
}
public void test531046g() throws CoreException, IOException {
	if (!isJRE9) return;
	try {
		createJava10Project("P", new String[] {"src"});
		String source =   "package p;\n"
				+ "public class X {\n"
				+ "  public static void main(java.lang.String[] args) {\n"
				+ "    var v_v = (CharSequence & Comparable<String>) \"x\";\n"
				+ "  }\n"
				+ "}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String select = "v_v";
		IJavaElement[] elements = unit.codeSelect(source.lastIndexOf(select), select.length());
		assertEquals("should not be empty", 1, elements.length);
		ILocalVariable variable = (ILocalVariable) elements[0];
		assertEquals("incorrect type", "&QCharSequence;:QComparable<QString;>;", variable.getTypeSignature());
	} finally {
		deleteProject("P");
	}
}
public void test531046h() throws CoreException, IOException {
	if (!isJRE9) return;
	try {
		createJava10Project("P", new String[] {"src"});
		String source =   "package p;\n"
				+ "public class X {\n"
				+ "  public static void main(java.lang.String[] args) {\n"
				+ "    var v_v = (CharSequence & Comparable<String>) \"x\";\n"
				+ "		System.out.println(v_v);\n"
				+ "  }\n"
				+ "}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String select = "v_v";
		IJavaElement[] elements = unit.codeSelect(source.lastIndexOf(select), select.length());
		assertEquals("should not be empty", 1, elements.length);
		ILocalVariable variable = (ILocalVariable) elements[0];
		assertEquals("incorrect type", "&QCharSequence;:QComparable<QString;>;", variable.getTypeSignature());
	} finally {
		deleteProject("P");
	}
}
public void testBug533884a() throws Exception {
	if (!isJRE9) return;
	try {
		createJava10Project("P", new String[] {"src"});
		String source =   "package p;\n" +
				"public class X {\n" +
				"	void bar() {\n" +
				"		String[] x = {\"a\", \"b\"};\n" +
				"		for (var y : x) { \n" +  // <= select this occurrence of 'y'
				"			System.err.println(y.toUpperCase());\n" +
				"		}\n" +
				"	}\n" +
				"\n"
				+ "}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String select = "y";
		IJavaElement[] elements = unit.codeSelect(source.indexOf(select), select.length());
		assertEquals("should not be empty", 1, elements.length);
		ILocalVariable variable = (ILocalVariable) elements[0];
		assertEquals("incorrect type", "Ljava.lang.String;", variable.getTypeSignature());
	} finally {
		deleteProject("P");
	}
}
public void testBug533884b() throws Exception {
	if (!isJRE9) return;
	try {
		createJava10Project("P", new String[] {"src"});
		String source =   "package p;\n" +
				"public class X {\n" +
				"	void bar() {\n" +
				"		String[] x = {\"a\", \"b\"};\n" +
				"		for (var y : x) { \n" +
				"			System.err.println(y.toUpperCase());\n" + // <= select this occurrence of 'y'
				"		}\n" +
				"	}\n" +
				"\n"
				+ "}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String select = "y";
		IJavaElement[] elements = unit.codeSelect(source.lastIndexOf(select), select.length());
		assertEquals("should not be empty", 1, elements.length);
		ILocalVariable variable = (ILocalVariable) elements[0];
		assertEquals("incorrect type", "Ljava.lang.String;", variable.getTypeSignature());
	} finally {
		deleteProject("P");
	}
}
public void testBug533884b_blockless() throws Exception {
	if (!isJRE9) return;
	try {
		createJava10Project("P", new String[] {"src"});
		String source =   "package p;\n" +
				"public class X {\n" +
				"	void bar() {\n" +
				"		String[] x = {\"a\", \"b\"};\n" +
				"		for (var y : x) \n" +
				"			System.err.println(y.toUpperCase());\n" + // <= select this occurrence of 'y'
				"	}\n" +
				"\n"
				+ "}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String select = "y";
		IJavaElement[] elements = unit.codeSelect(source.lastIndexOf(select), select.length());
		assertEquals("should not be empty", 1, elements.length);
		ILocalVariable variable = (ILocalVariable) elements[0];
		assertEquals("incorrect type", "Ljava.lang.String;", variable.getTypeSignature());
	} finally {
		deleteProject("P");
	}
}
public void testBug533884c() throws Exception {
	try {
		createJava10Project("P", new String[] {"src"});
		String source =   "package p;\n" +
				"import java.io.*;\n" +
				"public class X {\n" +
				"	void bar(File file) {\n" +
				"		try (var rc = new FileInputStream(file)) { \n" +
				"			System.err.println(rc.read());\n" + // <= select this occurrence of 'rc'
				"		}\n" +
				"	}\n" +
				"\n"
				+ "}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String select = "rc";
		IJavaElement[] elements = unit.codeSelect(source.lastIndexOf(select), select.length());
		assertEquals("should not be empty", 1, elements.length);
		ILocalVariable variable = (ILocalVariable) elements[0];
		if (isJRE9)
			assertEquals("incorrect type", "Ljava.io.FileInputStream;", variable.getTypeSignature());
		else
			assertEquals("incorrect type", "LFileInputStream;", variable.getTypeSignature()); // unresolved because JRT lib not available
	} finally {
		deleteProject("P");
	}
}
public void testBug533884c_blockless() throws Exception {
	try {
		createJava10Project("P", new String[] {"src"});
		String source =   "package p;\n" +
				"import java.io.*;\n" +
				"public class X {\n" +
				"	void bar(File file) {\n" +
				"		try (var rc = new FileInputStream(file))\n" +
				"			System.err.println(rc.read());\n" + // <= select this occurrence of 'rc'
				"	}\n" +
				"\n"
				+ "}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String select = "rc";
		IJavaElement[] elements = unit.codeSelect(source.lastIndexOf(select), select.length());
		assertEquals("should not be empty", 1, elements.length);
		ILocalVariable variable = (ILocalVariable) elements[0];
		if (isJRE9)
			assertEquals("incorrect type", "Ljava.io.FileInputStream;", variable.getTypeSignature());
		else
			assertEquals("incorrect type", "LFileInputStream;", variable.getTypeSignature()); // unresolved because JRT lib not available
	} finally {
		deleteProject("P");
	}
}
public void testBug536387() throws Exception {
	try {
		createJava11Project("P", new String[] {"src"});
		String source =   "package p;\n\n" +
				"public class X {\n" +
				"	public class NewType {\n" +
				"		public int indexOf(int two) {\n" +
				"       	return 0;\n" +
				"       }\n" +
				"	}\n\n"+
				"	public interface Finder {\n" +
				"		public int find(NewType one, int two);\n" +
				"	}\n\n"+
				" 	public static void main(String[] args) {\n" +
				" 		final Finder finder = (var s1,var s2) -> s1.indexOf(s2);\n"+
				"	}\n" +
				"\n"
				+ "}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String select = "var";
		IJavaElement[] elements = unit.codeSelect(source.indexOf(select), select.length());
		assertEquals("should not be empty", 1, elements.length);
		IType type = (IType) elements[0];
		String signature= Signature.createTypeSignature(type.getFullyQualifiedName(), true);
		assertEquals("incorrect type", "Lp.X$NewType;", signature);
	} finally {
		deleteProject("P");
	}
}
public void testBug570314() throws Exception{
	try {
		createJava16Project("P", new String[] {"src"});
		String source =   "package p;\n\n" +
				"public class X {\n" +

				" 	public static void main(String[] args) {\n" +
				"		enum Y1 {\n" +
				"				\n" +
				"				BLEU,\n" +
				"				BLANC,\n" +
				"				ROUGE;\n" +
				"				\n" +
				"				public static void printValues() {\r\n" +
				"					for(Y1 y: Y1.values()) {\r\n" +
				"						System.out.print(y);\r\n" +
				"					}\r\n" +
				"				}\r\n" +
				"				\r\n" +
				"		}\r\n" +
				"		Y1.printValues();"	+
				"	}\n" +
				"\n" +
				"}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String select = "main";
		IJavaElement[] elements = unit.codeSelect(source.indexOf(select), select.length());
		assertEquals("should not be empty", 1, elements.length);
		IMethod method= (IMethod) elements[0];
		IJavaElement[] children = method.getChildren();
		assertEquals("children should not be empty", 1, children.length);
		IType type = (IType) children[0];
		String signature= Signature.createTypeSignature(type.getFullyQualifiedName(), true);
		assertEquals("incorrect type", "Lp.X$Y1;", signature);
	} finally {
		deleteProject("P");
	}
}
public void testBug575503() throws Exception{
	try {
		createJava16Project("P", new String[] {"src"});
		String source =   "package p;\n\n" +
				"public class Ssss {\n" +
				"	public static void main(String[] args) {\n" +
				"		new Ssss.Entry(false, new int[0]);\n" +
    			"	}\n" +
    			"	record Entry(boolean isHidden, int... indexes) {\n" +
    			"		Entry(int... indexes) {\n" +
    			"			this(false, indexes);\n" +
        		"		}\n" +
    			"	}\n" +
				"}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/Ssss.java", source);
		waitForAutoBuild();

		ICompilationUnit unit = getCompilationUnit("/P/src/p/Ssss.java");
		String select = "Ssss.Entry";
		IJavaElement[] elements = unit.codeSelect(source.indexOf(select), select.length());
		assertEquals("should not be empty", 1, elements.length);
		IType type = (IType) elements[0];
		String signature= Signature.createTypeSignature(type.getFullyQualifiedName(), true);
		assertEquals("incorrect type", "Lp.Ssss$Entry;", signature);
	} finally {
		deleteProject("P");
	}
}
// https://bugs.eclipse.org/bugs/show_bug.cgi?id=576778
public void testBug576778() throws Exception {
	try {
		createJava11Project("P", new String[] {"src"});
		String source =    "package p;\n\n"
				+"public class X {\n"
				+ "  public static void main(String[] args) {\n"
				+ "   var runnable = new Runnable() {\n"
				+ "     public void run() {}\n"
				+ "   };\n"
				+ "   runnable.run();\n"
				+ "  }\n"
				+ "}\n";
		createFolder("/P/src/p");
		createFile("/P/src/p/X.java", source);
		waitForAutoBuild();
		ICompilationUnit unit = getCompilationUnit("/P/src/p/X.java");
		String select = "runnable";
		IJavaElement[] elements = unit.codeSelect(source.indexOf(select), select.length());
		assertEquals("should not be empty", 1, elements.length);
		ILocalVariable variable = (ILocalVariable) elements[0];
		String signature= variable.getTypeSignature();
		assertEquals("incorrect type", "Qvar;", signature);
	} finally {
		deleteProject("P");
	}
}
}
