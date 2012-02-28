/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICodeAssist;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ILocalVariable;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.SourceType;

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
	StringBuffer buffer = new StringBuffer();
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
//		TESTS_NAMES = new String[] { "testParamAnnotations4" };
	}
	public static Test suite() {
		return buildModelTestSuite(TypeResolveTests.class);
	}
/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.SuiteOfTestCases#tearDownSuite()
 */
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
	IType type = getPackageFragmentRoot("/TypeResolve/myLib.jar").getPackageFragment("p2").getClassFile("Y.class").getType();
	String[][] types = type.resolveType("X");
	assertTypesEqual(
		"p1.X",
		types);
}
/*
 * Resolve the type "X" within a member binary type.
 */
public void testResolveTypeInBinary2() throws JavaModelException {
	IType type = getPackageFragmentRoot("/TypeResolve/myLib.jar").getPackageFragment("p2").getClassFile("Y$Member.class").getType();
	String[][] types = type.resolveType("X");
	assertTypesEqual(
		"p1.X",
		types);
}
/*
 * Resolve the type "X" within an anonymous binary type.
 */
public void testResolveTypeInBinary3() throws JavaModelException {
	IType type = getPackageFragmentRoot("/TypeResolve/myLib.jar").getPackageFragment("p2").getClassFile("Y$1.class").getType();
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
		IType type = getPackageFragmentRoot("/TypeResolve/lib212224.jar").getPackageFragment("").getClassFile("X212224$Member.class").getType();
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
public void testResolveString() throws JavaModelException {
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
		IType type = root.getPackageFragment("p").getClassFile("X.class").getType();
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
		IType type = root.getPackageFragment("p").getClassFile("X.class").getType();
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
		IType type = root.getPackageFragment("p").getClassFile("X.class").getType();
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
		IType type = root.getPackageFragment("p").getClassFile("X.class").getType();
		
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
		IType type = root.getPackageFragment("p").getClassFile("X.class").getType();
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
}
