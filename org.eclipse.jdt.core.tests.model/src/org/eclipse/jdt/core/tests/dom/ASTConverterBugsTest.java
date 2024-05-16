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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import junit.framework.Test;

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
 * bug 186410: [dom] StackOverflowError due to endless superclass bindings hierarchy
 * test Ensures that the superclass of "java.lang.Object" class is null even when it's a recovered binding
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=186410"
 */
public void testBug186410() throws CoreException, IOException {
	try {
		createJavaProject("P", new String[] { "" }, new String[0], "");
			createFile("P/A.java",
				"""
					public class A {
						void method(){}
					}"""
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
				"""
					public class A {
						Object field;
					}"""
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
 * bug 209150: [dom] Recovered type binding for "java.lang.Object" information are not complete
 * test Ensures that getPackage() and getQualifiedName() works properly for the "java.lang.Object" recovered binding
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=209150"
 */
public void testBug209150a() throws CoreException, IOException {
	try {
		createJavaProject("P", new String[] { "" }, new String[0], "");
			createFile("P/A.java",
				"""
					public class A {
						void method(){}
					}"""
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
				"""
					public class A {
						Object field;
					}"""
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
				"""
					public class A {
						Object[] array;
					}"""
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
 * bug 212100: [dom] Can't create binding to inner class
 * test Verify that the binding is well created for an inner class
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=212100"
 */
public void testBug212100a() throws JavaModelException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/X.java", true/*resolve*/);
	String contents =
		"""
		public class X {
			public class Y {
		      void foo() {}
		   }
		}""";
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
		"""
		public class X {
			public class Y {
		      void foo() {}
		   }
		}""";
	this.workingCopies[0].getBuffer().setContents(contents);
	this.workingCopies[0].save(null, true);

	this.workingCopies[1] = getWorkingCopy("/Converter15/src/Z.java", true/*resolve*/);
	String contentsZ =
		"""
		public class Z {
			public class W {
		      void bar() {}
		   }
		}""";
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
 * bug 212834: [dom] IMethodBinding.getParameterAnnotations does not return annotations
 * test Ensures that the method binding get the parameter annotations even on method invocation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=212834"
 */
public void testBug212834() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[3];
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/Baz.java",
		"public @interface Baz {\n" +
		"}\n"
	);
	this.workingCopies[1] = getWorkingCopy("/Converter15/src/C.java",
		"""
			public class C {
			public C(D d) {
				foo(5);
				d.bar(7);
			}
			@Baz
			public void foo(@Baz int x) { }
			
			}"""
	);
	this.workingCopies[2] = getWorkingCopy("/Converter15/src/D.java",
		"""
			public class D {
			@Baz
			public void bar(@Baz int y) { }
			
			}"""
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
 * bug 212857: [dom] AST has wrong source range after parameter with array-valued annotation
 * test Ensures that the method body has the right source range even when there's braces in its header
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=212857"
 */
public void testBug212857() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	String source = """
		package xy;
		public class C {
			void m(@SuppressWarnings({"unused", "bla"}) int arg) {
				int local;
			}
		}
		""";
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/xy/C.java", source);
	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkSourceRange(methodDeclaration.getBody(),
		"""
			{
					int local;
				}""",
		source
	);
}
public void testBug212857a() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	String source = """
		package xy;
		public class C {
			@SuppressWarnings({"unused", "bla"}) void m() {
				int local;
			}
		}
		""";
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/xy/C.java", source);
	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkSourceRange(methodDeclaration.getBody(),
		"""
			{
					int local;
				}""",
		source
	);
}
// tests with recovery
public void testBug212857b() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	String source = """
		package test;
		public class X {
			void m()\s
				if (arg == 0) {}
			}
		}
		""";
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/test/X.java", source);
	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkSourceRange(methodDeclaration.getBody(),
		"""
			\s
					if (arg == 0) {}
				}""",
		source
	);
}
public void testBug212857c() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	String source = """
		package test;
		public class X {
			void m()\s
			}
		}
		""";
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
	String source = """
		package test;
		public class X {
			void m(String str)\s
				if (arg == 0) {}
			}
		}
		""";
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/test/X.java", source);
	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkSourceRange(methodDeclaration.getBody(),
		"""
			\s
					if (arg == 0) {}
				}""",
		source
	);
}
public void testBug212857e() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	String source = """
		package test;
		public class X {
			void m(Object obj, int x)\s
			}
		}
		""";
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
 * bug 213509: [dom] IMethodBinding.getParameterAnnotations returns annotations for wrong parameter
 * test Ensures that all parameter annotations of a the method binding are correctly  returned
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=213509"
 */
public void testBug213509() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/Test.java",
		"""
			public class Test {
				void m(@Foo @Bar @Annot String str, @Bar @Foo Object obj, @Annot int x) {}
			}
			@interface Foo {}
			@interface Bar {}
			@interface Annot {}
			"""
	);

	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkParameterAnnotations(methodDeclaration+" has invalid parameter annotations!",
		"""
			----- param 1-----
			@LTest~Foo;
			@LTest~Bar;
			@LTest~Annot;
			----- param 2-----
			@LTest~Bar;
			@LTest~Foo;
			----- param 3-----
			@LTest~Annot;
			""",
		methodDeclaration.resolveBinding()
	);
}
public void testBug213509_invocation() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/Test.java",
		"""
			public class Test {
				void m(@Foo @Bar @Annot String str, @Bar @Foo Object obj, @Annot int x) {}
			}
			@interface Foo {}
			@interface Bar {}
			@interface Annot {}
			"""
	);
	this.workingCopies[1] = getWorkingCopy("/Converter15/src/X.java",
		"""
			public class X {
			public X(Test test) {
				test.m("", null, 7);
			}
			}"""
	);

	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[1], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	MethodInvocation methodInvocation = (MethodInvocation) ((ExpressionStatement) methodDeclaration.getBody().statements().get(0)).getExpression();
	checkParameterAnnotations(methodInvocation+" has invalid parameter annotations!",
		"""
			----- param 1-----
			@LTest~Foo;
			@LTest~Bar;
			@LTest~Annot;
			----- param 2-----
			@LTest~Bar;
			@LTest~Foo;
			----- param 3-----
			@LTest~Annot;
			""",
		methodInvocation.resolveMethodBinding()
	);
}

/**
 * bug 214002: [dom] NPE in MethodBinding.getParameterAnnotations()
 * test Ensures that no NPE occurs when not all method parameters have annotations
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=214002"
 */
public void testBug214002() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/Test.java",
		"""
			public class Test {
				void m(String str, @Bar @Foo Object obj, @Annot int x) {}
			}
			@interface Foo {}
			@interface Bar {}
			@interface Annot {}
			"""
	);

	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkParameterAnnotations(methodDeclaration+" has invalid parameter annotations!",
		"""
			----- param 1-----
			----- param 2-----
			@LTest~Bar;
			@LTest~Foo;
			----- param 3-----
			@LTest~Annot;
			""",
		methodDeclaration.resolveBinding()
	);
}
public void testBug214002b() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/Test.java",
		"""
			public class Test {
				void m(@Annot String str, Object obj, @Bar @Foo int x) {}
			}
			@interface Foo {}
			@interface Bar {}
			@interface Annot {}
			"""
	);

	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkParameterAnnotations(methodDeclaration+" has invalid parameter annotations!",
		"""
			----- param 1-----
			@LTest~Annot;
			----- param 2-----
			----- param 3-----
			@LTest~Bar;
			@LTest~Foo;
			""",
		methodDeclaration.resolveBinding()
	);
}
	/**
	 * bug212434: [dom] IllegalArgumentException during AST Creation
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=212434"
	 */
	public void testBug212434a() throws CoreException, IOException {
		try {
			createJavaProject("P", new String[] {""}, new String[0], "");
			createFile("P/A.java",
					"""
						public class A {
						
							public void foo() {
								new Object() {\s
									public void bar() {\s
										if (true) {
											final
										}
									}
						
								}; \s
						
								if (false) {
									Object var = new Object() {
										void toto() {
										\t
										}
									};
								}
							}
						}"""
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
 * bug 214647: [dom] NPE in MethodBinding.getParameterAnnotations(..)
 * test Ensures that no NPE occurs when parameters have no annotation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=214647"
 */
public void testBug214647() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[1];
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/Test.java",
		"""
			public class Test {
				void m(String str) {}
			}
			"""
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
		"""
			public class Test {
				void m(String str, Object o, int x) {}
			}
			"""
	);

	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[0], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	checkParameterAnnotations(methodDeclaration+" has invalid parameter annotations!",
		"""
			----- param 1-----
			----- param 2-----
			----- param 3-----
			""",
		methodDeclaration.resolveBinding()
	);
}

/**
 * bug 215759: DOM AST regression tests should be improved
 * test these tests test the new DOM AST test framework
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=215759"
 */
public void testBug215759a() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/p/Y.java",
			"""
				package p;
				public class  Y {
				}""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter/src/p/X.java",
			"""
				package p;
				public class X extends Y {
					/**
					 * blabla1
					 * @param [*1*]string[*1*] blabla2
					 */
					protected [*2*]String[*2*] foo(String string) {
						return [*3*]("" + string + "")[*3*] + ("");
					}
					/*comment*/[*4*]protected void bar() {}[*4*]
					#
				}""");

	assertASTResult(
			"""
				===== AST =====
				package p;
				public class X extends Y {
				  /**\s
				 * blabla1
				 * @param [*1*]string[*1*] blabla2
				 */
				  protected [*2*]String[*2*] foo(  String string){
				    return [*3*]("" + string + "")[*3*] + ("");
				  }
				  [*4*]protected void bar(){
				  }[*4*]
				}
				
				===== Details =====
				1:SIMPLE_NAME,[66,6],,,[VARIABLE,Lp/X;.foo(Ljava/lang/String;)Ljava/lang/String;#string#0#0,]
				2:SIMPLE_TYPE,[97,6],,,[TYPE,Ljava/lang/String;,]
				2:SIMPLE_NAME,[97,6],,,[TYPE,Ljava/lang/String;,]
				3:PARENTHESIZED_EXPRESSION,[134,18],,,[N/A]
				4:METHOD_DECLARATION,[176,23],,,[METHOD,Lp/X;.bar()V,]
				===== Problems =====
				1. ERROR in /Converter/src/p/X.java (at line 11)
					#
					^
				Syntax error on token "Invalid Character", delete this token
				""",
			result);
}

public void testBug215759b() throws CoreException {
	this.workingCopies = new ICompilationUnit[1];

	this.workingCopies[0] = getWorkingCopy(
			"/Converter/src/p/Y.java",
			"""
				package p;
				public class  Y {
				}""",
			true/*resolve*/);

	ASTResult result = this.buildMarkedAST(
			"/Converter/src/p/X.java",
			"""
				package p;
				public class X extends Y {
					/**
					 * blabla1
					 * @param [*1*]string[*1*] blabla2
					 */
					protected [*2*]String[*2*] foo(String string) {
						return [*3*]("" + string + "")[*3*] + ("");
					}
					/*comment*/[*4*]protected void bar() {}[*4*]
					[*5*]/**@deprecated*/protected void bar2() {}[*5*]
				}""");

	assertASTResult(
			"""
				===== AST =====
				package p;
				public class X extends Y {
				  /**\s
				 * blabla1
				 * @param [*1*]string[*1*] blabla2
				 */
				  protected [*2*]String[*2*] foo(  String string){
				    return [*3*]("" + string + "")[*3*] + ("");
				  }
				  [*4*]protected void bar(){
				  }[*4*]
				  [*5*]/**\s
				 * @deprecated
				 */
				  protected void bar2(){
				  }[*5*]
				}
				
				===== Details =====
				1:SIMPLE_NAME,[66,6],,,[VARIABLE,Lp/X;.foo(Ljava/lang/String;)Ljava/lang/String;#string#0#0,]
				2:SIMPLE_TYPE,[97,6],,,[TYPE,Ljava/lang/String;,]
				2:SIMPLE_NAME,[97,6],,,[TYPE,Ljava/lang/String;,]
				3:PARENTHESIZED_EXPRESSION,[134,18],,,[N/A]
				4:METHOD_DECLARATION,[176,23],[165,34],,[METHOD,Lp/X;.bar()V,]
				5:METHOD_DECLARATION,[201,40],,,[METHOD,Lp/X;.bar2()V,DEPRECATED]
				===== Problems =====
				No problem""",
			result);
}
/**
 * bug 218824: [DOM/AST] incorrect code leads to IllegalArgumentException during AST creation
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=218824"
 */
public void testBug218824a() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				public class X {
				        public void takeParam(int i) {
				                // do something
				        }
				
				        void test() {
				                char c = 'a';
				                   public void takeParam(int i) {
				                             // do something
				                           }
				
				                           void test() {
				                             char c = 'a';
				                            takeParam((int) c);
				                           }[*1*]takeParam([*1*](int) c);
				        }
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				public class X {
				  public void takeParam(  int i){
				  }
				  void test(){
				    char c='a';
				    public void takeParam;
				    int i;
				    new test(){
				      char c='a';
				{
				        takeParam((int)c);
				      }
				      [*1*]void takeParam(){
				      }[*1*]
				    }
				;
				  }
				}
				
				===== Details =====
				1:METHOD_DECLARATION,[447,10],,MALFORMED,[null]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 9)
					public void takeParam(int i) {
					            ^^^^^^^^^
				void is an invalid type for the variable takeParam
				2. ERROR in /Converter15/src/a/X.java (at line 9)
					public void takeParam(int i) {
					                     ^
				Syntax error on token "(", ; expected
				3. ERROR in /Converter15/src/a/X.java (at line 9)
					public void takeParam(int i) {
					                           ^
				Syntax error on token ")", ; expected
				4. ERROR in /Converter15/src/a/X.java (at line 13)
					void test() {
					^^^^
				Syntax error on token "void", new expected
				5. ERROR in /Converter15/src/a/X.java (at line 13)
					void test() {
					     ^^^^
				test cannot be resolved to a type
				6. ERROR in /Converter15/src/a/X.java (at line 14)
					char c = 'a';
					            ^
				Syntax error on token ";", { expected after this token
				7. ERROR in /Converter15/src/a/X.java (at line 16)
					}takeParam((int) c);
					^
				Syntax error, insert "}" to complete ClassBody
				8. ERROR in /Converter15/src/a/X.java (at line 16)
					}takeParam((int) c);
					^
				Syntax error, insert ";" to complete Statement
				9. ERROR in /Converter15/src/a/X.java (at line 16)
					}takeParam((int) c);
					 ^^^^^^^^^^
				Return type for the method is missing
				""",
			result);
}
/**
 * bug 215137: [AST]Some malformed MethodDeclaration, their Block is null, but they actually have Block
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=215137"
 */
public void testBug215137a() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				public class X {
				        public void foo() [*1*]{
				                System.out.println("hello);
				        }[*1*]
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				public class X {
				  public void foo()[*1*]{
				  }[*1*]
				}
				
				===== Details =====
				1:BLOCK,[54,55],,RECOVERED,[N/A]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 4)
					System.out.println("hello);
					                   ^^^^^^^^
				String literal is not properly closed by a double-quote
				""",
			result);
}
public void testBug215137b() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				public class X {
				        public void foo() [*1*]{
				                System.out.println('a);
				        }[*1*]
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				public class X {
				  public void foo()[*1*]{
				  }[*1*]
				}
				
				===== Details =====
				1:BLOCK,[54,51],,RECOVERED,[N/A]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 4)
					System.out.println('a);
					                   ^^
				Invalid character constant
				""",
			result);
}
public void testBug215137c() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				public class X {
				        public void foo() [*1*]{
				                System.out.println(''a);
				        }[*1*]
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				public class X {
				  public void foo()[*1*]{
				  }[*1*]
				}
				
				===== Details =====
				1:BLOCK,[54,52],,RECOVERED,[N/A]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 4)
					System.out.println(''a);
					                   ^^
				Invalid character constant
				""",
			result);
}
public void testBug215137d() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				public class X {
				        public void foo() [*1*]{
				                7eSystem.out.println();
				        }[*1*]
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				public class X {
				  public void foo()[*1*]{
				  }[*1*]
				}
				
				===== Details =====
				1:BLOCK,[54,51],,RECOVERED,[N/A]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 4)
					7eSystem.out.println();
					^^^
				Invalid float literal number
				""",
			result);
}
/**
 * bug 223838: [dom] AnnotationBinding.isRecovered() always return false
 * test That the annotation binding is well flagged as recovered when the annotation is an unknown type
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=223838"
 */
public void testBug223838() throws JavaModelException {
	String contents =
		"""
		package b223838;
		@Deprecated
		@Invalid
		public class Test {
		}
		""";
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
 * bug 223838: [dom] AnnotationBinding.isRecovered() always return false
 * test That the annotation binding is not reported when the recovery is off
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=223838"
 */
public void testBug223838a() throws JavaModelException {
	String contents =
		"""
		package b223838;
		@Deprecated
		@Invalid
		public class Test {
		}
		""";
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
 * bug 226357: NPE in MethodBinding.getParameterAnnotations() if some, but not all parameters are annotated
 * test Verify that NPE does no longer occur on the given test case
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=226357"
 */
public void testBug226357() throws CoreException, IOException {
	this.workingCopies = new ICompilationUnit[2];
	this.workingCopies[0] = getWorkingCopy("/Converter15/src/ParameterSubsetAnnotated.java",
		"""
			public class ParameterSubsetAnnotated {
			        public @interface NonZero { }
			        public static int safeDiv(int a, @NonZero int b) {
			                return a / b;
			        }
			}"""
	);
	this.workingCopies[1] = getWorkingCopy("/Converter15/src/ParameterSubsetClient.java",
		"""
			public class ParameterSubsetClient {
			
			        public void client() {
			                ParameterSubsetAnnotated.safeDiv(5, 0);
			        }
			
			}
			"""
	);

	CompilationUnit unit = (CompilationUnit) runConversion(this.workingCopies[1], true/*bindings*/, false/*no statement recovery*/, true/*bindings recovery*/);
	MethodDeclaration methodDeclaration = (MethodDeclaration) getASTNode(unit, 0, 0);
	ExpressionStatement statement = (ExpressionStatement) methodDeclaration.getBody().statements().get(0);
	MethodInvocation methodInvocation = (MethodInvocation) statement.getExpression();
	checkParameterAnnotations(methodDeclaration+" has invalid parameter annotations!",
		"""
			----- param 1-----
			----- param 2-----
			@LParameterSubsetAnnotated$NonZero;
			""",
		methodInvocation.resolveMethodBinding()
	);
}

public void testBug274898a() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				public class X {
				        public void foo() {
				                [*1*]Object o = new [*1*][*2*]new Object(){}[*2*];\s
				        }
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				public class X {
				  public void foo(){
				    [*1*]Object o;[*1*]
				    [*2*]new Object(){
				    }
				;[*2*]
				  }
				}
				
				===== Details =====
				1:VARIABLE_DECLARATION_STATEMENT,[72,15],,,[N/A]
				2:EXPRESSION_STATEMENT,[87,14],,,[N/A]
				2:CLASS_INSTANCE_CREATION,[87,14],,RECOVERED,[N/A]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 4)
					Object o = new new Object(){};\s
					               ^^^
				Syntax error on token "new", delete this token
				""",
			result);
}
public void testBug274898b() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				public class X {
				        public void foo() {
				                [*1*]Object o = new # [*1*][*2*]new Object(){}[*2*];\s
				        }
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				public class X {
				  public void foo(){
				    [*1*]Object o;[*1*]
				    [*2*]new Object(){
				    }
				;[*2*]
				  }
				}
				
				===== Details =====
				1:VARIABLE_DECLARATION_STATEMENT,[72,17],,,[N/A]
				2:EXPRESSION_STATEMENT,[89,14],,,[N/A]
				2:CLASS_INSTANCE_CREATION,[89,14],,,[N/A]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 4)
					Object o = new # new Object(){};\s
					               ^^^^^
				Syntax error on tokens, delete these tokens
				""",
			result);
}

public void testBug277204a() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				public class X {
					{
				        class Local {
				                [*1*]Object [*2*]x[*2*], [*3*]Local[*3*];[*1*] \s
				        }
					}
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				public class X {
				{
				class Local {
				      [*1*]Object [*2*]x[*2*], [*3*]Local[*3*];[*1*]
				    }
				  }
				}
				
				===== Details =====
				1:FIELD_DECLARATION,[69,16],,,[N/A]
				2:VARIABLE_DECLARATION_FRAGMENT,[76,1],,,[VARIABLE,La/X$45$Local;.x)Ljava/lang/Object;,]
				2:SIMPLE_NAME,[76,1],,,[VARIABLE,La/X$45$Local;.x)Ljava/lang/Object;,]
				3:VARIABLE_DECLARATION_FRAGMENT,[79,5],,,[VARIABLE,La/X$45$Local;.Local)Ljava/lang/Object;,]
				3:SIMPLE_NAME,[79,5],,,[VARIABLE,La/X$45$Local;.Local)Ljava/lang/Object;,]
				===== Problems =====
				No problem""",
			result);
}
public void testBug277204b() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				public class X {
					{
				        class Local {
				                [*1*]Object [*2*]x[*2*], [*3*]Local[*3*] \s
				        [*1*]}
					}
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				public class X {
				{
				class Local {
				      [*1*]Object [*2*]x[*2*], [*3*]Local[*3*];[*1*]
				    }
				  }
				}
				
				===== Details =====
				1:FIELD_DECLARATION,[69,26],,MALFORMED,[N/A]
				2:VARIABLE_DECLARATION_FRAGMENT,[76,1],,,[VARIABLE,La/X$45$Local;.x)Ljava/lang/Object;,]
				2:SIMPLE_NAME,[76,1],,,[VARIABLE,La/X$45$Local;.x)Ljava/lang/Object;,]
				3:VARIABLE_DECLARATION_FRAGMENT,[79,5],,,[VARIABLE,La/X$45$Local;.Local)Ljava/lang/Object;,]
				3:SIMPLE_NAME,[79,5],,,[VARIABLE,La/X$45$Local;.Local)Ljava/lang/Object;,]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 5)
					Object x, Local \s
					          ^^^^^
				Syntax error, insert ";" to complete ClassBodyDeclarations
				""",
			result);
}
public void testBug277204c() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				public class X {
				    [*1*]Object [*2*]x[*2*], [*3*]Local[*3*];[*1*] \s
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				public class X {
				  [*1*]Object [*2*]x[*2*], [*3*]Local[*3*];[*1*]
				}
				
				===== Details =====
				1:FIELD_DECLARATION,[32,16],,,[N/A]
				2:VARIABLE_DECLARATION_FRAGMENT,[39,1],,,[VARIABLE,La/X;.x)Ljava/lang/Object;,]
				2:SIMPLE_NAME,[39,1],,,[VARIABLE,La/X;.x)Ljava/lang/Object;,]
				3:VARIABLE_DECLARATION_FRAGMENT,[42,5],,,[VARIABLE,La/X;.Local)Ljava/lang/Object;,]
				3:SIMPLE_NAME,[42,5],,,[VARIABLE,La/X;.Local)Ljava/lang/Object;,]
				===== Problems =====
				No problem""",
			result);
}
public void testBug277204d() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				public class X {
				    [*1*]Object [*2*]x[*2*], [*3*]Local[*3*][*1*] \s
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				public class X {
				  [*1*]Object [*2*]x[*2*], [*3*]Local[*3*];[*1*]
				}
				
				===== Details =====
				1:FIELD_DECLARATION,[32,15],,MALFORMED|RECOVERED,[N/A]
				2:VARIABLE_DECLARATION_FRAGMENT,[39,1],,,[VARIABLE,La/X;.x)Ljava/lang/Object;,]
				2:SIMPLE_NAME,[39,1],,,[VARIABLE,La/X;.x)Ljava/lang/Object;,]
				3:VARIABLE_DECLARATION_FRAGMENT,[42,5],,,[VARIABLE,La/X;.Local)Ljava/lang/Object;,]
				3:SIMPLE_NAME,[42,5],,,[VARIABLE,La/X;.Local)Ljava/lang/Object;,]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 3)
					Object x, Local \s
					          ^^^^^
				Syntax error, insert ";" to complete ClassBodyDeclarations
				""",
			result);
}
public void testBug277204e() throws JavaModelException {
	ASTResult result = this.buildMarkedAST(
			"/Converter15/src/a/X.java",
			"""
				package a;
				public class X {
					{
				        class Local {
				                [*1*]Object [*2*]x[*2*],
				                [*3*]Local[*3*] \s
				        [*1*]}
					}
				}
				""");

	assertASTResult(
			"""
				===== AST =====
				package a;
				public class X {
				{
				class Local {
				      [*1*]Object [*2*]x[*2*], [*3*]Local[*3*];[*1*]
				    }
				  }
				}
				
				===== Details =====
				1:FIELD_DECLARATION,[69,42],,MALFORMED,[N/A]
				2:VARIABLE_DECLARATION_FRAGMENT,[76,1],,,[VARIABLE,La/X$45$Local;.x)Ljava/lang/Object;,]
				2:SIMPLE_NAME,[76,1],,,[VARIABLE,La/X$45$Local;.x)Ljava/lang/Object;,]
				3:VARIABLE_DECLARATION_FRAGMENT,[95,5],,,[VARIABLE,La/X$45$Local;.Local)Ljava/lang/Object;,]
				3:SIMPLE_NAME,[95,5],,,[VARIABLE,La/X$45$Local;.Local)Ljava/lang/Object;,]
				===== Problems =====
				1. ERROR in /Converter15/src/a/X.java (at line 6)
					Local \s
					^^^^^
				Syntax error, insert ";" to complete ClassBodyDeclarations
				""",
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
