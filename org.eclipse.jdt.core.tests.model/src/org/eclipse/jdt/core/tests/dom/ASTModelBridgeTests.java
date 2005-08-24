/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;

import junit.framework.Test;

/*
 * Test the bridge between the DOM AST and the Java model.
 */
public class ASTModelBridgeTests extends AbstractASTTests {
	
	ICompilationUnit workingCopy;

	public ASTModelBridgeTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildTestSuite(ASTModelBridgeTests.class);
	}
	
	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_PREFIX =  "testBug86380";
//		TESTS_NAMES = new String[] { "testLocalType2" };
//		TESTS_NUMBERS = new int[] { 83230 };
//		TESTS_RANGE = new int[] { 83304, -1 };
		}

	/*
	 * Removes the marker comments "*start*" and "*end*" from the given contents,
	 * builds an AST from the resulting source, and returns the AST node that was delimited
	 * by "*start*" and "*end*".
	 */
	private ASTNode buildAST(String contents) throws JavaModelException {
		return buildAST(contents, this.workingCopy);
	}
	
	private IBinding[] createBindings(String contents, IJavaElement element) throws JavaModelException {
		this.workingCopy.getBuffer().setContents(contents);
		this.workingCopy.makeConsistent(null);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setProject(getJavaProject("P"));
		IJavaElement[] elements = new IJavaElement[] {element};
		return parser.createBindings(elements, null);
	}
	
	private IBinding[] createBinaryBindings(String contents, IJavaElement element) throws CoreException {
		createClassFile("/P/lib", "A.class", contents);
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setProject(getJavaProject("P"));
		IJavaElement[] elements = new IJavaElement[] {element};
		return parser.createBindings(elements, null);
	}
	
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		IJavaProject project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB,JCL15_SRC", "/P/lib"}, "bin", "1.5");
		project.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.IGNORE);
		project.setOption(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.IGNORE);
		project.setOption(JavaCore.COMPILER_PB_FIELD_HIDING, JavaCore.IGNORE);
		project.setOption(JavaCore.COMPILER_PB_LOCAL_VARIABLE_HIDING, JavaCore.IGNORE);
		project.setOption(JavaCore.COMPILER_PB_TYPE_PARAMETER_HIDING, JavaCore.IGNORE);
		addLibrary(
			project, 
			"lib.jar",
			"libsrc.zip", 
			new String[] {
				"p/Y.java",
				"package p;\n" +
				"public class Y<T> {\n" +
				"  public Y(T t) {\n" +
				"  }\n" +
				"}"
			}, 
			"1.5");
		this.workingCopy = getCompilationUnit("/P/src/X.java").getWorkingCopy(
			new WorkingCopyOwner() {}, 
			new IProblemRequestor() {
				public void acceptProblem(IProblem problem) {
					System.err.println(problem.getMessage());
				}
				public void beginReporting() {}
				public void endReporting() {}
				public boolean isActive() {
					return true;
				}
			}, 
			null);
	}
	
	public void tearDownSuite() throws Exception {
		if (this.workingCopy != null)
			this.workingCopy.discardWorkingCopy();
		deleteProject("P");
		super.tearDownSuite();
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing an anonymous type is correct.
	 */
	public void testAnonymousType() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  Object foo() {\n" +
			"    return new Object() /*start*/{\n" +
			"    }/*end*/;\n" +
			"  }\n" +
			"}"
		);
		IBinding binding = ((AnonymousClassDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"<anonymous #1> [in foo() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	public void testAnonymousType2() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" + 
			"	public void foo() {\n" + 
			"		new Y(0/*c*/) /*start*/{\n" + 
			"			Object field;\n" + 
			"		}/*end*/;\n" + 
			"	}\n" + 
			"}\n" + 
			"class Y {\n" + 
			"	Y(int i) {\n" + 
			"	}\n" + 
			"}"
		);
		IBinding binding = ((AnonymousClassDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"<anonymous #1> [in foo() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing an array type is correct.
	 */
	public void testArrayType1() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  /*start*/Object[]/*end*/ field;\n" +
			"}"
		);
		IBinding binding = ((ArrayType) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in P]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing an array type of base type null.
	 * (regression test for bug 100142
	  	CCE when calling ITypeBinding#getJavaElement() on char[][]
	 */
	public void testArrayType2() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  /*start*/char[][]/*end*/ field;\n" +
			"}"
		);
		IBinding binding = ((ArrayType) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"<null>",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method coming from a class file is correct.
	 * (regression test for bug 91445 IMethodBinding.getJavaElement() returns an "unopen" IMethod)
	 */
	public void testBinaryMethod() throws JavaModelException {
		IClassFile classFile = getClassFile("P", getExternalJCLPathString("1.5"), "java.lang", "Enum.class");
		String source = classFile.getSource();
		MarkerInfo markerInfo = new MarkerInfo(source);
		markerInfo.astStarts = new int[] {source.indexOf("protected Enum")};
		markerInfo.astEnds = new int[] {source.indexOf('}', markerInfo.astStarts[0]) + 1};
		ASTNode node = buildAST(markerInfo, classFile);
		IBinding binding = ((MethodDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"Enum(java.lang.String, int) [in Enum [in Enum.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in P]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}
	
	/*
	 * Ensures that the IJavaElement of an IBinding representing a type coming from a class file is correct.
	 */
	public void testBinaryType() throws JavaModelException {
		IClassFile classFile = getClassFile("P", getExternalJCLPathString("1.5"), "java.lang", "String.class");
		String source = classFile.getSource();
		MarkerInfo markerInfo = new MarkerInfo(source);
		markerInfo.astStarts = new int[] {source.indexOf("public")};
		markerInfo.astEnds = new int[] {source.lastIndexOf('}') + 1};
		ASTNode node = buildAST(markerInfo, classFile);
		IBinding binding = ((TypeDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"String [in String.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in P]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}
	
	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (test several kinds of elements)
	 */
	public void testCreateBindings01() throws JavaModelException {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);
		parser.setProject(getJavaProject("P"));
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/X.java", 
			"public class X {\n" +
			"  public void foo(int i, String s) {\n" +
			"  }\n" +
			"}",
			owner, false);
		this.workingCopies[1] = getWorkingCopy(
			"/P/src/Y.java", 
			"public class Y extends X {\n" +
			"  void bar() {\n" +
			"    new Y() {};\n" +
			"  }\n" +
			"}",
			owner, false);
		this.workingCopies[2] = getWorkingCopy(
			"/P/src/I.java", 
			"public interface I {\n" +
			"  int BAR;\n" +
			"}",
			owner, false);
		IType typeX = this.workingCopies[0].getType("X");
		IJavaElement[] elements = new IJavaElement[] {
			typeX, 
			getClassFile("P", getExternalJCLPathString("1.5"), "java.lang", "Object.class").getType(),
			typeX.getMethod("foo", new String[] {"I", "QString;"}),
			this.workingCopies[2].getType("I").getField("BAR"),
			this.workingCopies[1].getType("Y").getMethod("bar", new String[0]).getType("", 1)
		};
		IBinding[] bindings = parser.createBindings(elements, null);
		assertBindingsEqual(
			"LX;\n" + 
			"Ljava/lang/Object;\n" + 
			"LX;.foo(ILjava/lang/String;)V\n" + 
			"LI;.BAR)I\n" + 
			"LY$50;",
			bindings);
	}
	
	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (top level type)
	 */
	public void testCreateBindings02() throws JavaModelException {
		IBinding[] bindings = createBindings(
			"public class X {\n" +
			"}",
			this.workingCopy.getType("X")
		);
		assertBindingsEqual(
			"LX;",
			bindings);
	}
	
	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (member type)
	 */
	public void testCreateBindings03() throws JavaModelException {
		IBinding[] bindings = createBindings(
			"public class X {\n" +
			"  public class Member {\n" +
			"  }\n" +
			"}",
			this.workingCopy.getType("X").getType("Member")
		);
		assertBindingsEqual(
			"LX$Member;",
			bindings);
	}
	
	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (anonymous type)
	 */
	public void testCreateBindings04() throws JavaModelException {
		IBinding[] bindings = createBindings(
			"public class X {\n" +
			"  void foo() {\n" +
			"    new X() {\n" +
			"    };\n" +
			"  }\n" +
			"}",
			this.workingCopy.getType("X").getMethod("foo", new String[0]).getType("", 1)
		);
		assertBindingsEqual(
			"LX$40;",
			bindings);
	}
	
	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (local type)
	 */
	public void testCreateBindings05() throws JavaModelException {
		IBinding[] bindings = createBindings(
			"public class X {\n" +
			"  void foo() {\n" +
			"    class Y {\n" +
			"    }\n" +
			"  }\n" +
			"}",
			this.workingCopy.getType("X").getMethod("foo", new String[0]).getType("Y", 1)
		);
		assertBindingsEqual(
			"LX$42;",
			bindings);
	}
	
	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (field)
	 */
	public void testCreateBindings06() throws JavaModelException {
		IBinding[] bindings = createBindings(
			"public class X {\n" +
			"  int field;\n" +
			"}",
			this.workingCopy.getType("X").getField("field")
		);
		assertBindingsEqual(
			"LX;.field)I",
			bindings);
	}
	
	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (method)
	 */
	public void testCreateBindings07() throws JavaModelException {
		IBinding[] bindings = createBindings(
			"public class X {\n" +
			"  void foo() {}\n" +
			"}",
			this.workingCopy.getType("X").getMethod("foo", new String[0])
		);
		assertBindingsEqual(
			"LX;.foo()V",
			bindings);
	}
	
	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (annotation declaration)
	 */
	public void testCreateBindings08() throws JavaModelException {
		IBinding[] bindings = createBindings(
			"@interface X {\n" +
			"}",
			this.workingCopy.getType("X")
		);
		assertBindingsEqual(
			"LX;",
			bindings);
	}
	
	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (enum declaration)
	 */
	public void testCreateBindings09() throws JavaModelException {
		IBinding[] bindings = createBindings(
			"public enum X {\n" +
			"}",
			this.workingCopy.getType("X")
		);
		assertBindingsEqual(
			"LX;",
			bindings);
	}
	
	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (annotation member declaration)
	 */
	public void testCreateBindings10() throws JavaModelException {
		IBinding[] bindings = createBindings(
			"@interface X {\n" +
			"  int foo();\n" +
			"}",
			this.workingCopy.getType("X").getMethod("foo", new String[0])
		);
		assertBindingsEqual(
			"LX;.foo()I",
			bindings);
	}
	
	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (enum constant)
	 */
	public void testCreateBindings11() throws JavaModelException {
		IBinding[] bindings = createBindings(
			"public enum X {\n" +
			"  FOO;\n" +
			"}",
			this.workingCopy.getType("X").getField("FOO")
		);
		assertBindingsEqual(
			"LX;.FOO)LX;",
			bindings);
	}
	
	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (import)
	 */
	public void testCreateBindings12() throws JavaModelException {
		IBinding[] bindings = createBindings(
			"import java.io.*;\n" +
			"public class X implements Serializable {\n" +
			"  static final long serialVersionUID = 0;\n" +
			"}",
			this.workingCopy.getImport("java.io.*")
		);
		assertBindingsEqual(
			"java/io",
			bindings);
	}
	
	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (import)
	 */
	public void testCreateBindings13() throws JavaModelException {
		IBinding[] bindings = createBindings(
			"import java.io.Serializable;\n" +
			"public class X implements Serializable {\n" +
			"  static final long serialVersionUID = 0;\n" +
			"}",
			this.workingCopy.getImport("java.io.Serializable")
		);
		assertBindingsEqual(
			"Ljava/io/Serializable;",
			bindings);
	}
	
	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (type parameter)
	 */
	public void testCreateBindings14() throws JavaModelException {
		IBinding[] bindings = createBindings(
			"public class X<T> {\n" +
			"}",
			this.workingCopy.getType("X").getTypeParameter("T")
		);
		assertBindingsEqual(
			"LX;:TT;",
			bindings);
	}
	
	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (binary type)
	 */
	public void testCreateBindings15() throws CoreException {
		IBinding[] bindings = createBinaryBindings(
			"public class A {\n" +
			"}",
			getClassFile("/P/lib/A.class").getType()
		);
		assertBindingsEqual(
			"LA;",
			bindings);
	}
	
	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (binary field)
	 */
	public void testCreateBindings16() throws CoreException {
		IBinding[] bindings = createBinaryBindings(
			"public class A {\n" +
			"  int field;\n" +
			"}",
			getClassFile("/P/lib/A.class").getType().getField("field")
		);
		assertBindingsEqual(
			"LA;.field)I",
			bindings);
	}
	
	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (binary method)
	 */
	public void testCreateBindings17() throws CoreException {
		IBinding[] bindings = createBinaryBindings(
			"public class A {\n" +
			"  int foo(String s, boolean b) {\n" +
			"    return -1;\n" +
			"  }\n" +
			"}",
			getClassFile("/P/lib/A.class").getType().getMethod("foo", new String[] {"Ljava.lang.String;", "Z"})
		);
		assertBindingsEqual(
			"LA;.foo(Ljava/lang/String;Z)I",
			bindings);
	}
	
	/*
	 * Ensures that the IJavaElement of an IBinding representing a field is correct.
	 */
	public void testField1() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  Object /*start*/field/*end*/;\n" +
			"}"
		);
		IBinding binding = ((VariableDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"field [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a field is correct.
	 */
	public void testField2() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  Object foo() {\n" +
			"    return new Object() {\n" +
			"      Object /*start*/field/*end*/;\n" +
			"    };\n" +
			"  }\n" +
			"}"
		);
		IBinding binding = ((VariableDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"field [in <anonymous #1> [in foo() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a local type is correct.
	 */
	public void testLocalType() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  void foo() {\n" +
			"    /*start*/class Y {\n" +
			"    }/*end*/\n" +
			"  }\n" +
			"}"
		);
		IBinding binding = ((TypeDeclarationStatement) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"Y [in foo() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a local type
	 * and coming from a binding key resolution is correct.
	 */
	public void testLocalType2() throws CoreException {
		String filePath = "/P/src/Z.java";
		try {
			String contents = 
				"public class Z {\n" +
				"  void foo() {\n" +
				"    /*start*/class Local {\n" +
				"    }/*end*/\n" +
				"  }\n" +
				"}";
			createFile(filePath, contents);

			// Get the binding key
			ASTNode node = buildAST(contents, getCompilationUnit(filePath));
			IBinding binding = ((TypeDeclarationStatement) node).resolveBinding();
			String bindingKey = binding.getKey();
			
			// Resolve the binding key
			BindingRequestor requestor = new BindingRequestor();
			String[] bindingKeys = new String[] {bindingKey};
			resolveASTs(
				new ICompilationUnit[] {}, 
				bindingKeys,
				requestor,
				getJavaProject("P"),
				workingCopy.getOwner()
			);
			IBinding[] bindings = requestor.getBindings(bindingKeys);
			
			// Ensure the Java element is correct
			IJavaElement element = bindings[0].getJavaElement();
			assertElementEquals(
				"Unexpected Java element",
				"Local [in foo() [in Z [in Z.java [in <default> [in src [in P]]]]]]",
				element
			);
			assertTrue("Element should exist", element.exists());
		} finally {
			deleteFile(filePath);
		}
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a local variable is correct.
	 * (regression test for bug 79610 IVariableBinding#getJavaElement() returns null for local variables)
	 */
	public void testLocalVariable1() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  void foo() {\n" +
			"    int /*start*/local/*end*/;\n" +
			"  }\n" +
			"}"
		);
		IBinding binding = ((VariableDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		IJavaElement expected = getLocalVariable(this.workingCopy, "local", "local");
		assertEquals(
			"Unexpected Java element",
			expected,
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a local variable is correct.
	 * (regression test for bug 79610 IVariableBinding#getJavaElement() returns null for local variables)
	 */
	public void testLocalVariable2() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  void foo() {\n" +
			"    Object first, /*start*/second/*end*/, third;\n" +
			"  }\n" +
			"}"
		);
		IBinding binding = ((VariableDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		IJavaElement expected = getLocalVariable(this.workingCopy, "second", "second");
		assertEquals(
			"Unexpected Java element",
			expected,
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a local variable is correct.
	 * (regression test for bug 80021 [1.5] CCE in VariableBinding.getJavaElement())
	 */
	public void testLocalVariable3() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  void foo(/*start*/int arg/*end*/) {\n" +
			"  }\n" +
			"}"
		);
		IBinding binding = ((VariableDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		IJavaElement expected = getLocalVariable(this.workingCopy, "arg", "arg");
		assertEquals(
			"Unexpected Java element",
			expected,
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a member type is correct.
	 */
	public void testMemberType() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  /*start*/class Y {\n" +
			"  }/*end*/\n" +
			"}"
		);
		IBinding binding = ((TypeDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"Y [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method is correct.
	 */
	public void testMethod1() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X<K, V> {\n" +
			"  /*start*/void foo(int i, Object o, java.lang.String s, Class[] c, X<K, V> x) {\n" +
			"  }/*end*/\n" +
			"}"
		);
		IBinding binding = ((MethodDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"foo(int, Object, java.lang.String, Class[], X<K,V>) [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method is correct.
	 */
	public void testMethod2() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X<K, V> {\n" +
			"  /*start*/void foo() {\n" +
			"  }/*end*/\n" +
			"}"
		);
		IBinding binding = ((MethodDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"foo() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method is correct.
	 * (regression test for bug 78757 MethodBinding.getJavaElement() returns null)
	 */
	public void testMethod3() throws JavaModelException {
		ICompilationUnit otherWorkingCopy = null;
		try {
			otherWorkingCopy = getWorkingCopy(
				"/P/src/Y.java",
				"public class Y {\n" +
				"  void foo(int i, String[] args, java.lang.Class clazz) {}\n" +
				"}",
				this.workingCopy.getOwner(), 
				null
			);
			ASTNode node = buildAST(
				"public class X {\n" +
				"  void bar() {\n" +
				"    Y y = new Y();\n" +
				"    /*start*/y.foo(1, new String[0], getClass())/*end*/;\n" +
				"  }\n" +
				"}"
			);
			IBinding binding = ((MethodInvocation) node).resolveMethodBinding();
			assertNotNull("No binding", binding);
			IJavaElement element = binding.getJavaElement();
			assertElementEquals(
				"Unexpected Java element",
				"foo(int, String[], java.lang.Class) [in Y [in [Working copy] Y.java [in <default> [in src [in P]]]]]",
				element
			);
			assertTrue("Element should exist", element.exists());
		} finally {
			if (otherWorkingCopy != null)
				otherWorkingCopy.discardWorkingCopy();
		}
	}
	
	/*
	 * Ensures that the IJavaElement of an IBinding representing a method is correct.
	 * (regression test for bug 81258 IMethodBinding#getJavaElement() is null with inferred method parameterization)
	 */
	public void testMethod4() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" + 
			"	void foo() {\n" + 
			"		/*start*/bar(new B<Object>())/*end*/;\n" + 
			"	}\n" + 
			"	<T extends Object> void bar(A<? extends T> arg) {\n" + 
			"	}\n" + 
			"}\n" + 
			"class A<T> {\n" + 
			"}\n" + 
			"class B<T> extends A<T> {	\n" + 
			"}"
		);
		IBinding binding = ((MethodInvocation) node).resolveMethodBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"bar(A<? extends T>) [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a parameterized method is correct.
	 * (regression test for bug 82382 IMethodBinding#getJavaElement() for method m(T t) in parameterized type Gen<T> is null)
	 */
	public void testMethod5() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X<T> {\n" + 
			"    void m(T t) { }\n" + 
			"}\n" + 
			"\n" + 
			"class Y {\n" + 
			"    {\n" + 
			"        /*start*/new X<String>().m(\"s\")/*end*/;\n" + 
			"    }\n" + 
			"}"
		);
		IBinding binding = ((MethodInvocation) node).resolveMethodBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"m(T) [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method inside an annotation is correct.
	 * (regression test for bug 83300 [1.5] ClassCastException in #getJavaElement() on binding of annotation element)
	 */
	public void testMethod6() throws JavaModelException {
		ASTNode node = buildAST(
			"@X(/*start*/value/*end*/=\"Hello\", count=-1)\n" + 
			"@interface X {\n" + 
			"    String value();\n" + 
			"    int count();\n" + 
			"}"
		);
		IBinding binding = ((SimpleName) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"value() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}
	
	/*
	 * Ensures that the IJavaElement of an IBinding representing a method with array parameters is correct.
	 * (regression test for bug 88769 IMethodBinding#getJavaElement() drops extra array dimensions and varargs
	 */
	public void testMethod7() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  /*start*/public int[] bar(int a[]) {\n" +
			"    return a;\n" +
			"  }/*end*/\n" +
			"}"
		);
		IBinding binding = ((MethodDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"bar(int[]) [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method with array parameters is correct.
	 * (regression test for bug 88769 IMethodBinding#getJavaElement() drops extra array dimensions and varargs
	 */
	public void testMethod8() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  /*start*/public Object[] bar2(Object[] o[][]) [][] {\n" +
			"    return o;\n" +
			"  }/*end*/\n" +
			"}"
		);
		IBinding binding = ((MethodDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"bar2(Object[][][]) [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method with varargs parameters is correct.
	 * (regression test for bug 88769 IMethodBinding#getJavaElement() drops extra array dimensions and varargs
	 */
	public void testMethod9() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  /*start*/public void bar3(Object... objs) {\n" +
			"  }/*end*/\n" +
			"}"
		);
		IBinding binding = ((MethodDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"bar3(Object[]) [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a package is correct.
	 */
	public void testPackage1() throws CoreException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  /*start*/java.lang/*end*/.String field;\n" +
			"}"
		);
		IBinding binding = ((QualifiedName) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"java.lang [in "+ getExternalJCLPathString("1.5") + " [in P]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a package is correct
	 * (case of default package)
	 */
	public void testPackage2() throws CoreException {
		ASTNode node = buildAST(
			"/*start*/public class X {\n" +
			"}/*end*/"
		);
		ITypeBinding typeBinding = ((TypeDeclaration) node).resolveBinding();
		assertNotNull("No binding", typeBinding);
		IPackageBinding binding = typeBinding.getPackage();
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"<default> [in src [in P]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}
	
	/*
	 * Ensures that the IJavaElement of an IBinding representing a parameterized binary type is correct.
	 * (regression test for bug 78087 [dom] TypeBinding#getJavaElement() throws IllegalArgumentException for parameterized or raw reference to binary type)
	 */
	public void testParameterizedBinaryType() throws CoreException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  /*start*/Comparable<String>/*end*/ field;\n" +
			"}"
		);
		IBinding binding = ((Type) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"Comparable [in Comparable.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in P]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a parameterized binary method is correct.
	 * (regression test for bug 88892 [1.5] IMethodBinding#getJavaElement() returns nonexistent IMethods (wrong parameter types))
	 */
	public void testParameterizedBinaryMethod() throws CoreException {
		ASTNode node = buildAST(
			"public class X extends p.Y<String> {\n" +
			"  public X(String s) {\n" +
			"    /*start*/super(s);/*end*/\n" +
			"  }\n" +
			"}"		
		);
		IBinding binding = ((SuperConstructorInvocation) node).resolveConstructorBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"Y(T) [in Y [in Y.class [in p [in lib.jar [in P]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a raw binary type is correct.
	 * (regression test for bug 78087 [dom] TypeBinding#getJavaElement() throws IllegalArgumentException for parameterized or raw reference to binary type)
	 */
	public void testRawBinaryType() throws CoreException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  /*start*/Comparable/*end*/ field;\n" +
			"}"
		);
		IBinding binding = ((Type) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"Comparable [in Comparable.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in P]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}
	
	/*
	 * Ensures that the IJavaElement of an IBinding representing a top level type is correct.
	 */
	public void testTopLevelType1() throws JavaModelException {
		ASTNode node = buildAST(
			"/*start*/public class X {\n" +
			"}/*end*/"
		);
		IBinding binding = ((TypeDeclaration) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"X [in [Working copy] X.java [in <default> [in src [in P]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a top level type is correct
	 * (the top level type being in another compilation unit)
	 */
	public void testTopLevelType2() throws CoreException {
		try {
			createFile(
				"/P/src/Y.java",
				"public class Y {\n" +
				"}"
			);
			ASTNode node = buildAST(
				"public class X extends /*start*/Y/*end*/ {\n" +
				"}"
			);
			IBinding binding = ((Type) node).resolveBinding();
			assertNotNull("No binding", binding);
			IJavaElement element = binding.getJavaElement();
			assertElementEquals(
				"Unexpected Java element",
				"Y [in Y.java [in <default> [in src [in P]]]]",
				element
			);
			assertTrue("Element should exist", element.exists());
		} finally {
			deleteFile("/P/src/Y.java");
		}
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a top level type is correct
	 * (the top level type being in a jar)
	 */
	public void testTopLevelType3() throws CoreException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  /*start*/String/*end*/ field;\n" +
			"}"
		);
		IBinding binding = ((Type) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"String [in String.class [in java.lang [in "+ getExternalJCLPathString("1.5") + " [in P]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a parameter type is correct.
	 * (regression test for bug 78930 ITypeBinding#getJavaElement() throws NPE for type variable)
	 */
	public void testTypeParameter() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X</*start*/T/*end*/> {\n" +
			"}"
		);
		IBinding binding = ((TypeParameter) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"<T> [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
		assertTrue("Element should exist", element.exists());
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a wild card is correct.
	 * (regression test for bug 81417 [dom] getJavaElement() throws a NPE for WildcardBinding)
	 */
	public void testWildCard() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X<T> {\n" + 
			"	X</*start*/? extends Exception/*end*/> field;\n" + 
			"}"
		);
		IBinding binding = ((WildcardType) node).resolveBinding();
		assertNotNull("No binding", binding);
		IJavaElement element = binding.getJavaElement();
		assertElementEquals(
			"Unexpected Java element",
			"<null>",
			element
		);
	}


}
