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
package org.eclipse.jdt.core.tests.dom;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.*;

@SuppressWarnings("rawtypes")
public class ASTConverterBugsTest extends ConverterTestSetup {

@Override
public void setUpSuite() throws Exception {
//	PROJECT_SETUP = true; // do not copy Converter* directories
	super.setUpSuite();
//	setUpJCLClasspathVariables("1.5");
	waitUntilIndexesReady();
}

public ASTConverterBugsTest(String name) {
	super(name);
}

public static Test suite() {
	return buildModelTestSuite(ASTConverterBugsTest.class);
}

protected void checkParameterAnnotations(String message, String expected, IMethodBinding methodBinding) {
	ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
	int size = parameterTypes == null ? 0 : parameterTypes.length;
	StringBuilder buffer = new StringBuilder();
	for (int i=0; i<size; i++) {
		buffer.append("----- param ");
		buffer.append(i+1);
		buffer.append("-----\n");
		IAnnotationBinding[] bindings= methodBinding.getParameterAnnotations(i);
		int length = bindings.length;
		for (int j=0; j<length; j++) {
			buffer.append(bindings[j].getKey());
			buffer.append('\n');
		}
	}
	assertEquals(message, expected, buffer.toString());
}

@Override
public ASTNode runConversion(ICompilationUnit unit, boolean resolveBindings) {
	return runConversion(this.testLevel, unit, resolveBindings);
}

@Override
public ASTNode runConversion(ICompilationUnit unit, int position, boolean resolveBindings) {
	return runConversion(this.testLevel, unit, position, resolveBindings);
}

@Override
public ASTNode runConversion(IClassFile classFile, int position, boolean resolveBindings) {
	return runConversion(this.testLevel, classFile, position, resolveBindings);
}

@Override
public ASTNode runConversion(char[] source, String unitName, IJavaProject project) {
	return runConversion(this.testLevel, source, unitName, project);
}

@Override
public ASTNode runConversion(char[] source, String unitName, IJavaProject project, boolean resolveBindings) {
	return runConversion(this.testLevel, source, unitName, project, resolveBindings);
}

@Override
public ASTNode runConversion(char[] source, String unitName, IJavaProject project, Map<String, String> options, boolean resolveBindings) {
	return runConversion(this.testLevel, source, unitName, project, options, resolveBindings);
}
@Override
public ASTNode runConversion(char[] source, String unitName, IJavaProject project, Map<String, String> options) {
	return runConversion(this.testLevel, source, unitName, project, options);
}

public ASTNode runConversion(
		ICompilationUnit unit,
		boolean resolveBindings,
		boolean statementsRecovery,
		boolean bindingsRecovery) {
	ASTParser parser = createASTParser();
	parser.setSource(unit);
	parser.setResolveBindings(resolveBindings);
	parser.setStatementsRecovery(statementsRecovery);
	parser.setBindingsRecovery(bindingsRecovery);
	parser.setWorkingCopyOwner(this.wcOwner);
	return parser.createAST(null);
}

@Override
protected void resolveASTs(ICompilationUnit[] cus, String[] bindingKeys, ASTRequestor requestor, IJavaProject project, WorkingCopyOwner owner) {
	ASTParser parser = createASTParser();
	parser.setResolveBindings(true);
	parser.setProject(project);
	parser.setWorkingCopyOwner(owner);
	parser.createASTs(cus, bindingKeys,  requestor, null);
}

/**
 * @bug 186410: [dom] StackOverflowError due to endless superclass bindings hierarchy
 * @test Ensures that the superclass of "java.lang.Object" class is null even when it's a recovered binding
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=186410"
 */
public void testBug186410() throws CoreException, IOException {
	try {
		createJavaProject("P", new String[] { "" }, new String[0], "");
			createFile("P/A.java",
				"public class A {\n" +
				"	void method(){}\n" +
				"}"
			);
		ICompilationUnit cuA = getCompilationUnit("P/A.java");
		CompilationUnit unitA = (CompilationUnit) runConversion(cuA, true, false, true);
		AbstractTypeDeclaration typeA = (AbstractTypeDeclaration) unitA.types().get(0);
		ITypeBinding objectType = typeA.resolveBinding().getSuperclass();
		assertEquals("Unexpected superclass", "Object", objectType.getName());
		ITypeBinding objectSuperclass = objectType.getSuperclass();
		assertNull("java.lang.Object should  not have any superclass", objectSuperclass);
	} finally {
		deleteProject("P");
	}
}

public void testBug186410b() throws CoreException, IOException {
	try {
		createJavaProject("P", new String[] { "" }, new String[0], "");
			createFile("P/A.java",
				"public class A {\n" +
				"	Object field;\n" +
				"}"
			);
		ICompilationUnit cuA = getCompilationUnit("P/A.java");
		CompilationUnit unitA = (CompilationUnit) runConversion(cuA, true, false, true);
		AbstractTypeDeclaration type = (AbstractTypeDeclaration) unitA.types().get(0);
		FieldDeclaration field = (FieldDeclaration) type.bodyDeclarations().get(0);
		Type fieldType = field.getType();
		ITypeBinding typeBinding = fieldType.resolveBinding();
		ITypeBinding objectType = typeBinding.createArrayType(2).getElementType();
		assertEquals("Unexpected superclass", "Object", objectType.getName());
		ITypeBinding objectSuperclass = objectType.getSuperclass();
		assertNull("java.lang.Object should  not have any superclass", objectSuperclass);
	} finally {
		deleteProject("P");
	}
}

/**
 * @bug 209150: [dom] Recovered type binding for "java.lang.Object" information are not complete
 * @test Ensures that getPackage() and getQualifiedName() works properly for the "java.lang.Object" recovered binding
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=209150"
 */
public void testBug209150a() throws CoreException, IOException {
	try {
		createJavaProject("P", new String[] { "" }, new String[0], "");
			createFile("P/A.java",
				"public class A {\n" +
				"	void method(){}\n" +
				"}"
			);
		ICompilationUnit cuA = getCompilationUnit("P/A.java");
		CompilationUnit unitA = (CompilationUnit) runConversion(cuA, true, false, true);
		AbstractTypeDeclaration typeA = (AbstractTypeDeclaration) unitA.types().get(0);
		ITypeBinding objectType = typeA.resolveBinding().getSuperclass();
		assertTrue("'java.lang.Object' should be recovered!", objectType.isRecovered());
		assertEquals("Unexpected package for recovered 'java.lang.Object'", "java.lang", objectType.getPackage().getName());
		assertEquals("Unexpected qualified name for recovered 'java.lang.Object'",
		    "java.lang.Object",
		    objectType.getQualifiedName());
	} finally {
		deleteProject("P");
	}
}

public void testBug209150b() throws CoreException, IOException {
	try {
		createJavaProject("P", new String[] { "" }, new String[0], "");
			createFile("P/A.java",
				"public class A {\n" +
				"	Object field;\n" +
				"}"
			);
		ICompilationUnit cuA = getCompilationUnit("P/A.java");
		CompilationUnit unitA = (CompilationUnit) runConversion(cuA, true, false, true);
		AbstractTypeDeclaration type = (AbstractTypeDeclaration) unitA.types().get(0);
		FieldDeclaration field = (FieldDeclaration) type.bodyDeclarations().get(0);
		Type fieldType = field.getType();
		ITypeBinding typeBinding = fieldType.resolveBinding();
		ITypeBinding arrayType = typeBinding.createArrayType(2);
		assertTrue("'java.lang.Object' should be recovered!", arrayType.isRecovered());
		assertNull("Unexpected package for recovered 'array of java.lang.Object'", arrayType.getPackage());
		assertEquals("Unexpected qualified name for recovered 'java.lang.Object'",
		    "java.lang.Object[][]",
		    arrayType.getQualifiedName());
	} finally {
		deleteProject("P");
	}
}

public void testBug209150c() throws CoreException, IOException {
	try {
		createJavaProject("P", new String[] { "" }, new String[0], "");
			createFile("P/A.java",
				"public class A {\n" +
				"	Object[] array;\n" +
				"}"
			);
		ICompilationUnit cuA = getCompilationUnit("P/A.java");
		CompilationUnit unitA = (CompilationUnit) runConversion(cuA, true, false, true);
		AbstractTypeDeclaration type = (AbstractTypeDeclaration) unitA.types().get(0);
		FieldDeclaration field = (FieldDeclaration) type.bodyDeclarations().get(0);
		Type fieldType = field.getType();
		ITypeBinding arrayType = fieldType.resolveBinding();
		assertTrue("'java.lang.Object' should be recovered!", arrayType.isRecovered());
		assertNull("Unexpected package for recovered 'array of java.lang.Object'", arrayType.getPackage());
		assertEquals("Unexpected qualified name for recovered 'java.lang.Object'",
		    "java.lang.Object[]",
		    arrayType.getQualifiedName());
	} finally {
		deleteProject("P");
	}
}

/**
 * @bug 212100: [dom] Can't create binding to inner class
 * @test Verify that the binding is well created for an inner class
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=212100"
 */
public void testBug212100a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
	String contents =
		"public class X {\n" +
		"	public class Y {\n" +
		"      void foo() {}\n" +
		"   }\n" +
		"}";
	this.workingCopies[0].getBuffer().setContents(contents);
	this.workingCopies[0].save(null, true);
	final IBinding[] bindings = new IBinding[4];
	final String key = "Ljava/lang/Object;"; // this will make the test fail
//	final String key = "LX;"; // this would make the test pass

	resolveASTs(this.workingCopies,
			new String[] { key },
			new ASTRequestor() {
				public void acceptBinding(String bindingKey, IBinding binding) {
					if (key.equals(bindingKey)) {
						bindings[0] = binding;
						IBinding[] temp = createBindings(new String[] {"LX;", "LX$Y;", "[LX$Y;"});
						for (int i = 0; i < temp.length; ++i) {
							bindings[i + 1] = temp[i];
						}
					}
				}
			},
			getJavaProject("Converter15"),
			null);
	assertNotNull("Binding for java.lang.Object should not be null", bindings[0]);
	assertNotNull("Binding for X should not be null", bindings[1]);
	assertNotNull("Binding for X.Y should not be null", bindings[2]);
	assertNotNull("Binding for X.Y[] should not be null", bindings[3]);
}
public void testBug212100b() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
	String contents =
		"public class X {\n" +
		"	public class Y {\n" +
		"      void foo() {}\n" +
		"   }\n" +
		"}";
	this.workingCopies[0].getBuffer().setContents(contents);
	this.workingCopies[0].save(null, true);

	this.workingCopies[1] = getWorkingCopy("/Converter15/src/Z.java", true/*resolve*/);
	String contentsZ =
		"public class Z {\n" +
		"	public class W {\n" +
		"      void bar() {}\n" +
		"   }\n" +
		"}";
	this.workingCopies[1].getBuffer().setContents(contentsZ);
	this.workingCopies[1].save(null, true);

	final String keyX = "LX;";
	final String keyXY = "LX$Y;";
	final String keyZ = "LZ;";
	final String keyZW = "LZ$W;";

	resolveASTs(this.workingCopies,
			new String[] { keyX, keyZ },
			new ASTRequestor() {
				public void acceptBinding(String bindingKey, IBinding binding) {
					IBinding[] bindings = createBindings(new String[] {
							keyX, keyXY, keyZ, keyZW
					});
					assertNotNull("When accepting " + bindingKey + ", Binding for X should not be null", bindings[0]);
					assertNotNull("When accepting " + bindingKey + ", Binding for X.Y should not be null", bindings[1]);
					assertNotNull("When accepting " + bindingKey + ", Binding for Z should not be null", bindings[2]);
					assertNotNull("When accepting " + bindingKey + ", Binding for Z.W should not be null", bindings[3]);
				}
			},
			getJavaProject("Converter15"),
			null);
}

/**
 * @bug 212834: [dom] IMethodBinding.getParameterAnnotations does not return annotations
 * @test Ensures that the method binding get the parameter annotations even on method invocation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=212834"
 */
public void testBug212834() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/Baz.java",
		"public @interface Baz {\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/Converter15/src/C.java",
		"public class C {\n" +
		"public C(D d) {\n" +
		"	foo(5);\n" +
		"	d.bar(7);\n" +
		"}\n" +
		"@Baz\n" +
		"public void foo(@Baz int x) { }\n" +
		"\n" +
		"}"
	);
	this.workingCopies[2] = getWorkingCopy("/Converter15/src/D.java",
		"public class D {\n" +
		"@Baz\n" +
		"public void bar(@Baz int y) { }\n" +
		"\n" +
		"}"
	);

	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[1], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	MethodInvocation methodInvocation = (MethodInvocation) ((ExpressionStatement) methodDeclaration.getBody().statements().get(1)).getExpression();
	IMethodBinding methodBinding = methodInvocation.resolveMethodBinding();
	assertBindingsEqual(methodInvocation+" has invalid parameter annotations!",
		"@LBaz;",
		methodBinding.getParameterAnnotations(0)
	);
}

/**
 * @bug 212857: [dom] AST has wrong source range after parameter with array-valued annotation
 * @test Ensures that the method body has the right source range even when there's braces in its header
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=212857"
 */
public void testBug212857() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	String source = "package xy;\n" +
		"public class C {\n" +
		"	void m(@SuppressWarnings({\"unused\", \"bla\"}) int arg) {\n" +
		"		int local;\n" +
		"	}\n" +
		"}\n";
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/xy/C.java", source);
	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkSourceRange(methodDeclaration.getBody(),
		"{\n" +
		"		int local;\n" +
		"	}",
		source
	);
}
public void testBug212857a() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	String source = "package xy;\n" +
	"public class C {\n" +
	"	@SuppressWarnings({\"unused\", \"bla\"}) void m() {\n" +
	"		int local;\n" +
	"	}\n" +
	"}\n";
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/xy/C.java", source);
	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkSourceRange(methodDeclaration.getBody(),
		"{\n" +
		"		int local;\n" +
		"	}",
		source
	);
}
// tests with recovery
public void testBug212857b() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	String source = "package test;\n" +
	"public class X {\n" +
	"	void m() \n" +
	"		if (arg == 0) {}\n" +
	"	}\n" +
	"}\n";
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/test/X.java", source);
	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkSourceRange(methodDeclaration.getBody(),
		" \n" +
		"		if (arg == 0) {}\n" +
		"	}",
		source
	);
}
public void testBug212857c() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	String source = "package test;\n" +
	"public class X {\n" +
	"	void m() \n" +
	"	}\n" +
	"}\n";
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/test/X.java", source);
	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkSourceRange(methodDeclaration.getBody(),
		" \n" +
		"	}",
		source
	);
}
public void testBug212857d() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	String source = "package test;\n" +
	"public class X {\n" +
	"	void m(String str) \n" +
	"		if (arg == 0) {}\n" +
	"	}\n" +
	"}\n";
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/test/X.java", source);
	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkSourceRange(methodDeclaration.getBody(),
		" \n" +
		"		if (arg == 0) {}\n" +
		"	}",
		source
	);
}
public void testBug212857e() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	String source = "package test;\n" +
	"public class X {\n" +
	"	void m(Object obj, int x) \n" +
	"	}\n" +
	"}\n";
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/test/X.java", source);
	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkSourceRange(methodDeclaration.getBody(),
		" \n" +
		"	}",
		source
	);
}

/**
 * @bug 213509: [dom] IMethodBinding.getParameterAnnotations returns annotations for wrong parameter
 * @test Ensures that all parameter annotations of a the method binding are correctly  returned
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=213509"
 */
public void testBug213509() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/Test.java",
		"public class Test {\n" +
		"	void m(@Foo @Bar @Annot String str, @Bar @Foo Object obj, @Annot int x) {}\n" +
		"}\n" +
		"@interface Foo {}\n" +
		"@interface Bar {}\n" +
		"@interface Annot {}\n"
	);

	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkParameterAnnotations(methodDeclaration+" has invalid parameter annotations!",
		"----- param 1-----\n" +
		"@LTest~Foo;\n" +
		"@LTest~Bar;\n" +
		"@LTest~Annot;\n" +
		"----- param 2-----\n" +
		"@LTest~Bar;\n" +
		"@LTest~Foo;\n" +
		"----- param 3-----\n" +
		"@LTest~Annot;\n",
		methodDeclaration.resolveBinding()
	);
}
public void testBug213509_invocation() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/Test.java",
		"public class Test {\n" +
		"	void m(@Foo @Bar @Annot String str, @Bar @Foo Object obj, @Annot int x) {}\n" +
		"}\n" +
		"@interface Foo {}\n" +
		"@interface Bar {}\n" +
		"@interface Annot {}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/Converter15/src/X.java",
		"public class X {\n" +
		"public X(Test test) {\n" +
		"	test.m(\"\", null, 7);\n" +
		"}\n" +
		"}"
	);

	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[1], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	MethodInvocation methodInvocation = (MethodInvocation) ((ExpressionStatement) methodDeclaration.getBody().statements().get(0)).getExpression();
	checkParameterAnnotations(methodInvocation+" has invalid parameter annotations!",
		"----- param 1-----\n" +
		"@LTest~Foo;\n" +
		"@LTest~Bar;\n" +
		"@LTest~Annot;\n" +
		"----- param 2-----\n" +
		"@LTest~Bar;\n" +
		"@LTest~Foo;\n" +
		"----- param 3-----\n" +
		"@LTest~Annot;\n",
		methodInvocation.resolveMethodBinding()
	);
}

/**
 * @bug 214002: [dom] NPE in MethodBinding.getParameterAnnotations()
 * @test Ensures that no NPE occurs when not all method parameters have annotations
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=214002"
 */
public void testBug214002() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/Test.java",
		"public class Test {\n" +
		"	void m(String str, @Bar @Foo Object obj, @Annot int x) {}\n" +
		"}\n" +
		"@interface Foo {}\n" +
		"@interface Bar {}\n" +
		"@interface Annot {}\n"
	);

	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkParameterAnnotations(methodDeclaration+" has invalid parameter annotations!",
		"----- param 1-----\n" +
		"----- param 2-----\n" +
		"@LTest~Bar;\n" +
		"@LTest~Foo;\n" +
		"----- param 3-----\n" +
		"@LTest~Annot;\n",
		methodDeclaration.resolveBinding()
	);
}
public void testBug214002b() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/Test.java",
		"public class Test {\n" +
		"	void m(@Annot String str, Object obj, @Bar @Foo int x) {}\n" +
		"}\n" +
		"@interface Foo {}\n" +
		"@interface Bar {}\n" +
		"@interface Annot {}\n"
	);

	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkParameterAnnotations(methodDeclaration+" has invalid parameter annotations!",
		"----- param 1-----\n" +
		"@LTest~Annot;\n" +
		"----- param 2-----\n" +
		"----- param 3-----\n" +
		"@LTest~Bar;\n" +
		"@LTest~Foo;\n",
		methodDeclaration.resolveBinding()
	);
}
	/**
	 * @bug 212434: [dom] IllegalArgumentException during AST Creation
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=212434"
	 */
	public void testBug212434a() throws CoreException, IOException {
		try {
			createJavaProject("P", new String[] {""}, new String[0], "");
			createFile("P/A.java",
					"public class A {\n"+
					"\n"+
					"	public void foo() {\n"+
					"		new Object() { \n"+
					"			public void bar() { \n"+
					"				if (true) {\n"+
					"					final\n"+
					"				}\n"+
					"			}\n"+
					"\n"+
					"		};  \n"+
					"\n"+
					"		if (false) {\n"+
					"			Object var = new Object() {\n"+
					"				void toto() {\n"+
					"					\n"+
					"				}\n"+
					"			};\n"+
					"		}\n"+
					"	}\n"+
					"}"
			);
			ICompilationUnit cuA = getCompilationUnit("P/A.java");
			try {
				runConversion(getJLS3(), cuA, true, true, true);
			} catch(IllegalArgumentException e) {
				assertTrue("Unexpected IllegalArgumentException", false);
			}
		} finally {
			deleteProject("P");
		}
	}
/**
 * @bug 214647: [dom] NPE in MethodBinding.getParameterAnnotations(..)
 * @test Ensures that no NPE occurs when parameters have no annotation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=214647"
 */
public void testBug214647() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/Test.java",
		"public class Test {\n" +
		"	void m(String str) {}\n" +
		"}\n"
	);

	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkParameterAnnotations(methodDeclaration+" has invalid parameter annotations!",
		"----- param 1-----\n",
		methodDeclaration.resolveBinding()
	);
}
public void testBug214647b() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/Test.java",
		"public class Test {\n" +
		"	void m(String str, Object o, int x) {}\n" +
		"}\n"
	);

	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkParameterAnnotations(methodDeclaration+" has invalid parameter annotations!",
		"----- param 1-----\n" +
		"----- param 2-----\n" +
		"----- param 3-----\n",
		methodDeclaration.resolveBinding()
	);
}

/**
 * @bug 215759: DOM AST regression tests should be improved
 * @test these tests test the new DOM AST test framework
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=215759"
 */
public void testBug215759a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/p/Y.java",
			"package p;\n" +
			"public class  Y {\n" +
			"}",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter/src/p/X.java",
			"package p;\n" +
			"public class X extends Y {\n" +
			"	/**\n" +
			"	 * blabla1\n" +
			"	 * @param [*1*]string[*1*] blabla2\n" +
			"	 */\n" +
			"	protected [*2*]String[*2*] foo(String string) {\n" +
			"		return [*3*](\"\" + string + \"\")[*3*] + (\"\");\n" +
			"	}\n" +
			"	/*comment*/[*4*]protected void bar() {}[*4*]\n" +
			"	#\n" +
			"}");

	assertASTResult(
			"===== AST =====\n" +
			"package p;\n" +
			"public class X extends Y {\n" +
			"  /** \n" +
			" * blabla1\n" +
			" * @param [*1*]string[*1*] blabla2\n" +
			" */\n" +
			"  protected [*2*]String[*2*] foo(  String string){\n" +
			"    return [*3*](\"\" + string + \"\")[*3*] + (\"\");\n" +
			"  }\n" +
			"  [*4*]protected void bar(){\n" +
			"  }[*4*]\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:SIMPLE_NAME,[66,6],,,[VARIABLE,Lp/X;.foo(Ljava/lang/String;)Ljava/lang/String;#string#0#0,]\n" +
			"2:SIMPLE_TYPE,[97,6],,,[TYPE,Ljava/lang/String;,]\n" +
			"2:SIMPLE_NAME,[97,6],,,[TYPE,Ljava/lang/String;,]\n" +
			"3:PARENTHESIZED_EXPRESSION,[134,18],,,[N/A]\n" +
			"4:METHOD_DECLARATION,[176,23],,,[METHOD,Lp/X;.bar()V,]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter/src/p/X.java (at line 11)\n" +
			"	#\n" +
			"	^\n" +
			"Syntax error on token \"Invalid Character\", delete this token\n",
			result);
}

public void testBug215759b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/p/Y.java",
			"package p;\n" +
			"public class  Y {\n" +
			"}",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter/src/p/X.java",
			"package p;\n" +
			"public class X extends Y {\n" +
			"	/**\n" +
			"	 * blabla1\n" +
			"	 * @param [*1*]string[*1*] blabla2\n" +
			"	 */\n" +
			"	protected [*2*]String[*2*] foo(String string) {\n" +
			"		return [*3*](\"\" + string + \"\")[*3*] + (\"\");\n" +
			"	}\n" +
			"	/*comment*/[*4*]protected void bar() {}[*4*]\n" +
			"	[*5*]/**@deprecated*/protected void bar2() {}[*5*]\n" +
			"}");

	assertASTResult(
			"===== AST =====\n" +
			"package p;\n" +
			"public class X extends Y {\n" +
			"  /** \n" +
			" * blabla1\n" +
			" * @param [*1*]string[*1*] blabla2\n" +
			" */\n" +
			"  protected [*2*]String[*2*] foo(  String string){\n" +
			"    return [*3*](\"\" + string + \"\")[*3*] + (\"\");\n" +
			"  }\n" +
			"  [*4*]protected void bar(){\n" +
			"  }[*4*]\n" +
			"  [*5*]/** \n" +
			" * @deprecated\n" +
			" */\n" +
			"  protected void bar2(){\n" +
			"  }[*5*]\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:SIMPLE_NAME,[66,6],,,[VARIABLE,Lp/X;.foo(Ljava/lang/String;)Ljava/lang/String;#string#0#0,]\n" +
			"2:SIMPLE_TYPE,[97,6],,,[TYPE,Ljava/lang/String;,]\n" +
			"2:SIMPLE_NAME,[97,6],,,[TYPE,Ljava/lang/String;,]\n" +
			"3:PARENTHESIZED_EXPRESSION,[134,18],,,[N/A]\n" +
			"4:METHOD_DECLARATION,[176,23],[165,34],,[METHOD,Lp/X;.bar()V,]\n" +
			"5:METHOD_DECLARATION,[201,40],,,[METHOD,Lp/X;.bar2()V,DEPRECATED]\n" +
			"===== Problems =====\n" +
			"No problem",
			result);
}
/**
 * @bug 218824: [DOM/AST] incorrect code leads to IllegalArgumentException during AST creation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=218824"
 */
public void testBug218824a() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"public class X {\n"+
			"        public void takeParam(int i) {\n"+
			"                // do something\n"+
			"        }\n"+
			"\n"+
			"        void test() {\n"+
			"                char c = 'a';\n"+
			"                   public void takeParam(int i) {\n"+
			"                             // do something\n"+
			"                           }\n"+
			"\n"+
			"                           void test() {\n"+
			"                             char c = 'a';\n"+
			"                            takeParam((int) c);\n"+
			"                           }[*1*]takeParam([*1*](int) c);\n"+
			"        }\n"+
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"public class X {\n" +
			"  public void takeParam(  int i){\n" +
			"  }\n" +
			"  void test(){\n" +
			"    char c=\'a\';\n" +
			"    public void takeParam;\n" +
			"    int i;\n" +
			"    new test(){\n" +
			"      char c=\'a\';\n" +
			"{\n" +
			"        takeParam((int)c);\n" +
			"      }\n" +
			"      [*1*]void takeParam(){\n" +
			"      }[*1*]\n" +
			"    }\n" +
			";\n" +
			"  }\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:METHOD_DECLARATION,[447,10],,MALFORMED,[null]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 9)\n" +
			"	public void takeParam(int i) {\n" +
			"	            ^^^^^^^^^\n" +
			"void is an invalid type for the variable takeParam\n" +
			"2. ERROR in /Converter15/src/a/X.java (at line 9)\n" +
			"	public void takeParam(int i) {\n" +
			"	                     ^\n" +
			"Syntax error on token \"(\", ; expected\n" +
			"3. ERROR in /Converter15/src/a/X.java (at line 9)\n" +
			"	public void takeParam(int i) {\n" +
			"	                           ^\n" +
			"Syntax error on token \")\", ; expected\n" +
			"4. ERROR in /Converter15/src/a/X.java (at line 13)\n" +
			"	void test() {\n" +
			"	^^^^\n" +
			"Syntax error on token \"void\", new expected\n" +
			"5. ERROR in /Converter15/src/a/X.java (at line 13)\n" +
			"	void test() {\n" +
			"	     ^^^^\n" +
			"test cannot be resolved to a type\n" +
			"6. ERROR in /Converter15/src/a/X.java (at line 14)\n" +
			"	char c = \'a\';\n" +
			"	            ^\n" +
			"Syntax error on token \";\", { expected after this token\n" +
			"7. ERROR in /Converter15/src/a/X.java (at line 16)\n" +
			"	}takeParam((int) c);\n" +
			"	^\n" +
			"Syntax error, insert \"}\" to complete ClassBody\n" +
			"8. ERROR in /Converter15/src/a/X.java (at line 16)\n" +
			"	}takeParam((int) c);\n" +
			"	^\n" +
			"Syntax error, insert \";\" to complete Statement\n" +
			"9. ERROR in /Converter15/src/a/X.java (at line 16)\n" +
			"	}takeParam((int) c);\n" +
			"	 ^^^^^^^^^^\n" +
			"Return type for the method is missing\n",
			result);
}
/**
 * @bug 215137: [AST]Some malformed MethodDeclaration, their Block is null, but they actually have Block
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=215137"
 */
public void testBug215137a() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"public class X {\n"+
			"        public void foo() [*1*]{\n"+
			"                System.out.println(\"hello);\n"+
			"        }[*1*]\n"+
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"public class X {\n" +
			"  public void foo()[*1*]{\n" +
			"  }[*1*]\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:BLOCK,[54,55],,RECOVERED,[N/A]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 4)\n" +
			"	System.out.println(\"hello);\n" +
			"	                   ^^^^^^^^\n" +
			"String literal is not properly closed by a double-quote\n",
			result);
}
public void testBug215137b() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"public class X {\n"+
			"        public void foo() [*1*]{\n"+
			"                System.out.println('a);\n"+
			"        }[*1*]\n"+
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"public class X {\n" +
			"  public void foo()[*1*]{\n" +
			"  }[*1*]\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:BLOCK,[54,51],,RECOVERED,[N/A]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 4)\n" +
			"	System.out.println(\'a);\n" +
			"	                   ^^\n" +
			"Invalid character constant\n",
			result);
}
public void testBug215137c() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"public class X {\n"+
			"        public void foo() [*1*]{\n"+
			"                System.out.println(''a);\n"+
			"        }[*1*]\n"+
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"public class X {\n" +
			"  public void foo()[*1*]{\n" +
			"  }[*1*]\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:BLOCK,[54,52],,RECOVERED,[N/A]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 4)\n" +
			"	System.out.println(\'\'a);\n" +
			"	                   ^^\n" +
			"Invalid character constant\n",
			result);
}
public void testBug215137d() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"public class X {\n"+
			"        public void foo() [*1*]{\n"+
			"                7eSystem.out.println();\n"+
			"        }[*1*]\n"+
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"public class X {\n" +
			"  public void foo()[*1*]{\n" +
			"  }[*1*]\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:BLOCK,[54,51],,RECOVERED,[N/A]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 4)\n" +
			"	7eSystem.out.println();\n" +
			"	^^^\n" +
			"Invalid float literal number\n",
			result);
}
/**
 * @bug 223838: [dom] AnnotationBinding.isRecovered() always return false
 * @test That the annotation binding is well flagged as recovered when the annotation is an unknown type
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=223838"
 */
public void testBug223838() throws JavaModelException {
	String contents =
		"package b223838;\n" +
		"@Deprecated\n" +
		"@Invalid\n" +
		"public class Test {\n" +
		"}\n";
	ICompilationUnit workingCopy = getWorkingCopy(
			"/Converter15/src/b223838/Test.java",
			contents,
			true/*resolve*/
		);
	ASTNode node = buildAST(contents, workingCopy, false, false, true);
	CompilationUnit unit = (CompilationUnit) node;
	List types = unit.types();
	TypeDeclaration type = (TypeDeclaration) types.get(0);
	ITypeBinding typeBinding = type.resolveBinding();
	IAnnotationBinding[] annotations = typeBinding.getAnnotations();
	assertTrue("Expected recovered annotation binding!", annotations[1].isRecovered());
}
/**
 * @bug 223838: [dom] AnnotationBinding.isRecovered() always return false
 * @test That the annotation binding is not reported when the recovery is off
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=223838"
 */
public void testBug223838a() throws JavaModelException {
	String contents =
		"package b223838;\n" +
		"@Deprecated\n" +
		"@Invalid\n" +
		"public class Test {\n" +
		"}\n";
	ICompilationUnit workingCopy = getWorkingCopy(
			"/Converter15/src/b223838/Test.java",
			contents,
			true/*resolve*/
		);
	ASTNode node = buildAST(contents, workingCopy, false, false, false);
	CompilationUnit unit = (CompilationUnit) node;
	List types = unit.types();
	TypeDeclaration type = (TypeDeclaration) types.get(0);
	ITypeBinding typeBinding = type.resolveBinding();
	IAnnotationBinding[] annotations = typeBinding.getAnnotations();
	assertEquals("Got more than one annotation binding", 1, annotations.length);
}

/**
 * @bug 226357: NPE in MethodBinding.getParameterAnnotations() if some, but not all parameters are annotated
 * @test Verify that NPE does no longer occur on the given test case
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=226357"
 */
public void testBug226357() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/ParameterSubsetAnnotated.java",
		"public class ParameterSubsetAnnotated {\n" +
		"        public @interface NonZero { }\n" +
		"        public static int safeDiv(int a, @NonZero int b) {\n" +
		"                return a / b;\n" +
		"        }\n" +
		"}"
	);
	this.workingCopies[1] = getWorkingCopy("/Converter15/src/ParameterSubsetClient.java",
		"public class ParameterSubsetClient {\n" +
		"\n" +
		"        public void client() {\n" +
		"                ParameterSubsetAnnotated.safeDiv(5, 0);\n" +
		"        }\n" +
		"\n" +
		"}\n"
	);

	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[1], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	ExpressionStatement statement = (ExpressionStatement) methodDeclaration.getBody().statements().get(0);
	MethodInvocation methodInvocation = (MethodInvocation) statement.getExpression();
	checkParameterAnnotations(methodDeclaration+" has invalid parameter annotations!",
		"----- param 1-----\n" +
		"----- param 2-----\n" +
		"@LParameterSubsetAnnotated$NonZero;\n",
		methodInvocation.resolveMethodBinding()
	);
}

public void testBug274898a() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"public class X {\n"+
			"        public void foo() {\n"+
			"                [*1*]Object o = new [*1*][*2*]new Object(){}[*2*]; \n"+
			"        }\n"+
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"public class X {\n" +
			"  public void foo(){\n" +
			"    [*1*]Object o;[*1*]\n" +
			"    [*2*]new Object(){\n" +
			"    }\n" +
			";[*2*]\n" +
			"  }\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:VARIABLE_DECLARATION_STATEMENT,[72,15],,,[N/A]\n" +
			"2:EXPRESSION_STATEMENT,[87,14],,,[N/A]\n" +
			"2:CLASS_INSTANCE_CREATION,[87,14],,RECOVERED,[N/A]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 4)\n" +
			"	Object o = new new Object(){}; \n" +
			"	               ^^^\n" +
			"Syntax error on token \"new\", delete this token\n",
			result);
}
public void testBug274898b() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"public class X {\n"+
			"        public void foo() {\n"+
			"                [*1*]Object o = new # [*1*][*2*]new Object(){}[*2*]; \n"+
			"        }\n"+
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"public class X {\n" +
			"  public void foo(){\n" +
			"    [*1*]Object o;[*1*]\n" +
			"    [*2*]new Object(){\n" +
			"    }\n" +
			";[*2*]\n" +
			"  }\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:VARIABLE_DECLARATION_STATEMENT,[72,17],,,[N/A]\n" +
			"2:EXPRESSION_STATEMENT,[89,14],,,[N/A]\n" +
			"2:CLASS_INSTANCE_CREATION,[89,14],,,[N/A]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 4)\n" +
			"	Object o = new # new Object(){}; \n" +
			"	               ^^^^^\n" +
			"Syntax error on tokens, delete these tokens\n",
			result);
}

public void testBug277204a() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"public class X {\n" +
			"	{\n" +
			"        class Local {\n" +
			"                [*1*]Object [*2*]x[*2*], [*3*]Local[*3*];[*1*]  \n" +
			"        }\n" +
			"	}\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"public class X {\n" +
			"{\n" +
			"class Local {\n" +
			"      [*1*]Object [*2*]x[*2*], [*3*]Local[*3*];[*1*]\n" +
			"    }\n" +
			"  }\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:FIELD_DECLARATION,[69,16],,,[N/A]\n" +
			"2:VARIABLE_DECLARATION_FRAGMENT,[76,1],,,[VARIABLE,La/X$45$Local;.x)Ljava/lang/Object;,]\n" +
			"2:SIMPLE_NAME,[76,1],,,[VARIABLE,La/X$45$Local;.x)Ljava/lang/Object;,]\n" +
			"3:VARIABLE_DECLARATION_FRAGMENT,[79,5],,,[VARIABLE,La/X$45$Local;.Local)Ljava/lang/Object;,]\n" +
			"3:SIMPLE_NAME,[79,5],,,[VARIABLE,La/X$45$Local;.Local)Ljava/lang/Object;,]\n" +
			"===== Problems =====\n" +
			"No problem",
			result);
}
public void testBug277204b() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"public class X {\n" +
			"	{\n" +
			"        class Local {\n" +
			"                [*1*]Object [*2*]x[*2*], [*3*]Local[*3*]  \n" +
			"        [*1*]}\n" +
			"	}\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"public class X {\n" +
			"{\n" +
			"class Local {\n" +
			"      [*1*]Object [*2*]x[*2*], [*3*]Local[*3*];[*1*]\n" +
			"    }\n" +
			"  }\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:FIELD_DECLARATION,[69,26],,MALFORMED,[N/A]\n" +
			"2:VARIABLE_DECLARATION_FRAGMENT,[76,1],,,[VARIABLE,La/X$45$Local;.x)Ljava/lang/Object;,]\n" +
			"2:SIMPLE_NAME,[76,1],,,[VARIABLE,La/X$45$Local;.x)Ljava/lang/Object;,]\n" +
			"3:VARIABLE_DECLARATION_FRAGMENT,[79,5],,,[VARIABLE,La/X$45$Local;.Local)Ljava/lang/Object;,]\n" +
			"3:SIMPLE_NAME,[79,5],,,[VARIABLE,La/X$45$Local;.Local)Ljava/lang/Object;,]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 5)\n" +
			"	Object x, Local  \n" +
			"	          ^^^^^\n" +
			"Syntax error, insert \";\" to complete ClassBodyDeclarations\n",
			result);
}
public void testBug277204c() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"public class X {\n" +
			"    [*1*]Object [*2*]x[*2*], [*3*]Local[*3*];[*1*]  \n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"public class X {\n" +
			"  [*1*]Object [*2*]x[*2*], [*3*]Local[*3*];[*1*]\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:FIELD_DECLARATION,[32,16],,,[N/A]\n" +
			"2:VARIABLE_DECLARATION_FRAGMENT,[39,1],,,[VARIABLE,La/X;.x)Ljava/lang/Object;,]\n" +
			"2:SIMPLE_NAME,[39,1],,,[VARIABLE,La/X;.x)Ljava/lang/Object;,]\n" +
			"3:VARIABLE_DECLARATION_FRAGMENT,[42,5],,,[VARIABLE,La/X;.Local)Ljava/lang/Object;,]\n" +
			"3:SIMPLE_NAME,[42,5],,,[VARIABLE,La/X;.Local)Ljava/lang/Object;,]\n" +
			"===== Problems =====\n" +
			"No problem",
			result);
}
public void testBug277204d() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"public class X {\n" +
			"    [*1*]Object [*2*]x[*2*], [*3*]Local[*3*][*1*]  \n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"public class X {\n" +
			"  [*1*]Object [*2*]x[*2*], [*3*]Local[*3*];[*1*]\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:FIELD_DECLARATION,[32,15],,MALFORMED|RECOVERED,[N/A]\n" +
			"2:VARIABLE_DECLARATION_FRAGMENT,[39,1],,,[VARIABLE,La/X;.x)Ljava/lang/Object;,]\n" +
			"2:SIMPLE_NAME,[39,1],,,[VARIABLE,La/X;.x)Ljava/lang/Object;,]\n" +
			"3:VARIABLE_DECLARATION_FRAGMENT,[42,5],,,[VARIABLE,La/X;.Local)Ljava/lang/Object;,]\n" +
			"3:SIMPLE_NAME,[42,5],,,[VARIABLE,La/X;.Local)Ljava/lang/Object;,]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 3)\n" +
			"	Object x, Local  \n" +
			"	          ^^^^^\n" +
			"Syntax error, insert \";\" to complete ClassBodyDeclarations\n",
			result);
}
public void testBug277204e() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"package a;\n" +
			"public class X {\n" +
			"	{\n" +
			"        class Local {\n" +
			"                [*1*]Object [*2*]x[*2*],\n" +
			"                [*3*]Local[*3*]  \n" +
			"        [*1*]}\n" +
			"	}\n" +
			"}\n");

	assertASTResult(
			"===== AST =====\n" +
			"package a;\n" +
			"public class X {\n" +
			"{\n" +
			"class Local {\n" +
			"      [*1*]Object [*2*]x[*2*], [*3*]Local[*3*];[*1*]\n" +
			"    }\n" +
			"  }\n" +
			"}\n" +
			"\n" +
			"===== Details =====\n" +
			"1:FIELD_DECLARATION,[69,42],,MALFORMED,[N/A]\n" +
			"2:VARIABLE_DECLARATION_FRAGMENT,[76,1],,,[VARIABLE,La/X$45$Local;.x)Ljava/lang/Object;,]\n" +
			"2:SIMPLE_NAME,[76,1],,,[VARIABLE,La/X$45$Local;.x)Ljava/lang/Object;,]\n" +
			"3:VARIABLE_DECLARATION_FRAGMENT,[95,5],,,[VARIABLE,La/X$45$Local;.Local)Ljava/lang/Object;,]\n" +
			"3:SIMPLE_NAME,[95,5],,,[VARIABLE,La/X$45$Local;.Local)Ljava/lang/Object;,]\n" +
			"===== Problems =====\n" +
			"1. ERROR in /Converter15/src/a/X.java (at line 6)\n" +
			"	Local  \n" +
			"	^^^^^\n" +
			"Syntax error, insert \";\" to complete ClassBodyDeclarations\n",
			result);
}

// Verify that the binding for a constructor is a method binding
@SuppressWarnings("deprecation")
public void testBug381503() throws CoreException, IOException {
	try {
		IJavaProject javaProject = createJavaProject("P", new String[] { "src" }, new String[] { "CONVERTER_JCL_LIB" }, "bin");
		IType type = javaProject.findType("java.lang.IllegalMonitorStateException");
		IMethod javaElement = type.getMethod("IllegalMonitorStateException", new String[]{});
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setProject(javaProject);
		IBinding[] bindings = parser.createBindings(new IJavaElement[] { javaElement }, null);
		assertEquals("Wrong number of bindings", 1, bindings.length);
		assertTrue("Wrong binding kind: "+bindings[0].getClass().getName(), bindings[0] instanceof IMethodBinding);
	} finally {
		deleteProject("P");
	}
}
}
