/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
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
 *     Stephan Herrmann - Contributions for
 *								Bug 463330 - [dom] DOMFinder doesn't find the VariableBinding corresponding to a method argument
 *								Bug 464463 - [dom] DOMFinder doesn't find an ITypeParameter
 *								Bug 464615 - [dom] ASTParser.createBindings() ignores parameterization of a method invocation
 *								Bug 466279 - [hovering] IAE on hover when annotation-based null analysis is enabled
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.tests.model.AbstractJavaSearchTests;
import org.eclipse.jdt.core.tests.util.Util;

import junit.framework.Test;

/*
 * Test the bridge between the DOM AST and the Java model.
 */
public class ASTModelBridgeTests extends AbstractASTTests {

	ICompilationUnit workingCopy;

	/**
	 * Internal synonym for deprecated constant AST.JSL3
	 * to alleviate deprecation warnings.
	 * @deprecated
	 */
	/*package*/ static final int JLS3_INTERNAL = AST.JLS3;

	protected void checkSourceRange(int start, int length, String expectedContents, String source) {
		assertTrue("length == 0", length != 0); //$NON-NLS-1$ //$NON-NLS-2$
		assertTrue("start == -1", start != -1); //$NON-NLS-1$
		String actualContentsString = source.substring(start, start + length);
		assertSourceEquals("Unexpected source", Util.convertToIndependantLineDelimiter(expectedContents), Util.convertToIndependantLineDelimiter(actualContentsString));
	}

	public ASTModelBridgeTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(ASTModelBridgeTests.class);
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_PREFIX =  "testBug86380";
//		TESTS_NAMES = new String[] { "testLocalVariable7" };
//		TESTS_NUMBERS = new int[] { 83230 };
//		TESTS_RANGE = new int[] { 83304, -1 };
		}

	private void assertFindElement(String key, String expectedElement) throws JavaModelException {
		IJavaElement element = getJavaProject("P").findElement(key, this.workingCopy.getOwner());
		assertElementEquals(
			"Unexpected found element",
			expectedElement,
			element
		);
	}

	/**
	 * @deprecated
	 */
	static int getJLS8() {
		return AST.JLS8;
	}

	/*
	 * Removes the marker comments "*start*" and "*end*" from the given contents,
	 * builds an AST from the resulting source, and returns the AST node that was delimited
	 * by "*start*" and "*end*".
	 */
	private ASTNode buildAST(String contents) throws JavaModelException {
		return buildAST(contents, this.workingCopy);
	}

	/*
	 * Removes the marker comments "*start*" and "*end*" from the given contents,
	 * builds an AST from the resulting source, gets the binding from the AST node that was delimited
	 * by "*start*" and "*end*", and returns the binding key.
	 */
	private String buildBindingKey(String contents) throws JavaModelException {
		ASTNode node = buildAST(contents);
		if (node == null) return null;
		IBinding binding = resolveBinding(node);
		if (binding == null) return null;
		return binding.getKey();
	}

	private IBinding[] createBindings(String contents, IJavaElement element) throws JavaModelException {
		this.workingCopy.getBuffer().setContents(contents);
		this.workingCopy.makeConsistent(null);
		ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		parser.setProject(getJavaProject("P"));
		IJavaElement[] elements = new IJavaElement[] {element};
		return parser.createBindings(elements, null);
	}

	private IBinding[] createBinaryBindings(String contents, IJavaElement element) throws CoreException {
		createClassFile("/P/lib", "A.class", contents);
		ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		parser.setProject(getJavaProject("P"));
		IJavaElement[] elements = new IJavaElement[] {element};
		return parser.createBindings(elements, null);
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		setUpJavaProject();
	}

	private void setUpJavaProject() throws CoreException, IOException, JavaModelException {
		IJavaProject project = createJavaProject("P", new String[] {"src"}, new String[] {"JCL15_LIB,JCL15_SRC", "/P/lib"}, "bin", "1.5");
		project.setOption(JavaCore.COMPILER_PB_UNUSED_LOCAL, JavaCore.IGNORE);
		project.setOption(JavaCore.COMPILER_PB_UNUSED_PRIVATE_MEMBER, JavaCore.IGNORE);
		project.setOption(JavaCore.COMPILER_PB_FIELD_HIDING, JavaCore.IGNORE);
		project.setOption(JavaCore.COMPILER_PB_LOCAL_VARIABLE_HIDING, JavaCore.IGNORE);
		project.setOption(JavaCore.COMPILER_PB_TYPE_PARAMETER_HIDING, JavaCore.IGNORE);
		project.setOption(JavaCore.COMPILER_PB_RAW_TYPE_REFERENCE, JavaCore.IGNORE);
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
				"}",
				"p/Z.java",
				"package p;\n" +
				"public class Z {\n" +
				"  /*start*/class Member {\n" +
				"  }/*end*/\n" +
				"  void foo() {\n" +
				"    new Member() {};\n" +
				"  }\n" +
				"}",
				"p/W.java",
				"package p;\n" +
				"public class W {\n" +
				"  class Member {\n" +
				"    /*start*/Member(String s) {\n" +
				"    }/*end*/\n" +
				"  }\n" +
				"}",
				"p/ABC.java",
				"package p;\n" +
				"public class ABC {\n" +
				"}",
				"Z.java",
				"public class Z {\n" +
				"  /*start*/class Member {\n" +
				"  }/*end*/\n" +
				"  void foo() {\n" +
				"    new Member() {};\n" +
				"  }\n" +
				"}",
				"p/Q.java",
				"package p;\n" +
				"/*start*/@MyAnnot/*end*/\n" +
				"public class Q {\n" +
				"}\n" +
				"@interface MyAnnot {\n" +
				"}",
			},
			"1.5");
		setUpWorkingCopy();
	}

	private void setUpWorkingCopy() throws JavaModelException {
		if (this.workingCopy != null)
			this.workingCopy.discardWorkingCopy();
		IProblemRequestor problemRequestor = new IProblemRequestor() {
			public void acceptProblem(IProblem problem) {}
			public void beginReporting() {}
			public void endReporting() {}
			public boolean isActive() {
				return true;
			}
		};
		this.workingCopy = getCompilationUnit("/P/src/X.java").getWorkingCopy(
			newWorkingCopyOwner(problemRequestor),
			null/*no progress*/);
	}

	@Override
	public void tearDownSuite() throws Exception {
		tearDownJavaProject();
		super.tearDownSuite();
	}

	private void tearDownJavaProject() throws JavaModelException, CoreException {
		if (this.workingCopy != null)
			this.workingCopy.discardWorkingCopy();
		deleteProject("P");
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing an annotation is correct.
	 */
	public void testAnnotation1() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  /*start*/@MyAnnot/*end*/\n" +
			"  void foo() {\n" +
			"  }\n" +
			"}\n" +
			"@interface MyAnnot {\n" +
			"}"
		);
		IBinding binding = ((Annotation) node).resolveAnnotationBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"@MyAnnot [in foo() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing an annotation is correct.
	 */
	public void testAnnotation2() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  /*start*/@MyAnnot/*end*/\n" +
			"  int field;\n" +
			"}\n" +
			"@interface MyAnnot {\n" +
			"}"
		);
		IBinding binding = ((Annotation) node).resolveAnnotationBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"@MyAnnot [in field [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing an annotation is correct.
	 */
	public void testAnnotation3() throws JavaModelException {
		ASTNode node = buildAST(
			"/*start*/@MyAnnot/*end*/\n" +
			"public class X {\n" +
			"}\n" +
			"@interface MyAnnot {\n" +
			"}"
		);
		IBinding binding = ((Annotation) node).resolveAnnotationBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"@MyAnnot [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing an annotation is correct.
	 */
	public void testAnnotation4() throws JavaModelException {
		ICompilationUnit myAnnot = null;
		ICompilationUnit packageInfo = null;
		try {
			WorkingCopyOwner owner = this.workingCopy.getOwner();
			myAnnot = getCompilationUnit("/P/src/pkg/MyAnnot.java").getWorkingCopy(owner, null);
			myAnnot.getBuffer().setContents(
				"package pkg;\n" +
				"public @interface MyAnnot {\n" +
				"}"
			);
			myAnnot.makeConsistent(null);
			packageInfo = getCompilationUnit("/P/src/pkg/package-info.java").getWorkingCopy(owner, null);
			ASTNode node = buildAST(
				"/*start*/@MyAnnot/*end*/\n" +
				"package pkg;",
				packageInfo
			);
			IBinding binding = ((Annotation) node).resolveAnnotationBinding();
			IJavaElement element = binding.getJavaElement();
			assertElementExists(
				"Unexpected Java element",
				"@MyAnnot [in package pkg [in [Working copy] package-info.java [in pkg [in src [in P]]]]]",
				element
			);
		} finally {
			if (myAnnot != null)
				myAnnot.discardWorkingCopy();
			if (packageInfo != null)
				packageInfo.discardWorkingCopy();
		}
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing an annotation is correct.
	 */
	public void testAnnotation5() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  void foo() {\n" +
			"    /*start*/@MyAnnot/*end*/\n" +
			"    int var1 = 2;\n" +
			"  }\n" +
			"}\n" +
			"@interface MyAnnot {\n" +
			"}"
		);
		IBinding binding = ((Annotation) node).resolveAnnotationBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"@MyAnnot [in var1 [in foo() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing an annotation on an annotation type is correct.
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=249844 )
	 */
	public void testAnnotation6() throws JavaModelException {
		ASTNode node = buildAST(
			"/*start*/@MyAnnot/*end*/\n" +
			"public @interface X {\n" +
			"}\n" +
			"@interface MyAnnot {\n" +
			"}"
		);
		IBinding binding = ((Annotation) node).resolveAnnotationBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"@MyAnnot [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing an annotation on an enum type is correct.
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=249844 )
	 */
	public void testAnnotation7() throws JavaModelException {
		ASTNode node = buildAST(
			"/*start*/@MyAnnot/*end*/\n" +
			"public enum X {\n" +
			"}\n" +
			"@interface MyAnnot {\n" +
			"}"
		);
		IBinding binding = ((Annotation) node).resolveAnnotationBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"@MyAnnot [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing an annotation of a binary member type is correct.
	 */
	public void testAnnotation8() throws Exception {
		IOrdinaryClassFile classFile = getClassFile("P", "/P/lib.jar", "p", "Q.class");
		ASTNode node = buildAST(classFile);
		IBinding binding = ((Annotation) node).resolveAnnotationBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"@p.MyAnnot [in Q [in Q.class [in p [in lib.jar [in P]]]]]",
			element
		);
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328969
	public void testAnnotation9() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"/*start*/@MyAnnot/*end*/ void foo() throws MissingException {}\n" +
			"}\n" +
			"@interface MyAnnot {\n" +
			"}",
			this.workingCopy,
			false,
			true,
			false
		);
		IBinding binding = ((Annotation) node).resolveAnnotationBinding();
		assertNull("Got a java element", binding.getJavaElement());
	}

	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328969
	public void testAnnotation10() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"/*start*/@MyAnnot/*end*/ MissingType foo;\n" +
			"}\n" +
			"@interface MyAnnot {\n" +
			"}",
			this.workingCopy,
			false,
			true,
			false
		);
		IBinding binding = ((Annotation) node).resolveAnnotationBinding();
		assertNull("Got a java element", binding.getJavaElement());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328969
	public void testAnnotation11() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"/*start*/@MyAnnot/*end*/ void foo() throws MissingException {}\n" +
			"}\n" +
			"@interface MyAnnot {\n" +
			"}",
			this.workingCopy,
			false,
			true,
			true
		);
		IBinding binding = ((Annotation) node).resolveAnnotationBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
				"Unexpected Java element",
				"@MyAnnot [in foo() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]]",
				element
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328969
	public void testAnnotation12() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"/*start*/@MyAnnot/*end*/ MissingType foo;\n" +
			"}\n" +
			"@interface MyAnnot {\n" +
			"}",
			this.workingCopy,
			false,
			true,
			true
		);
		IBinding binding = ((Annotation) node).resolveAnnotationBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
				"Unexpected Java element",
				"@MyAnnot [in foo [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]]",
				element
		);
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328969
	public void testAnnotation13() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"	void bar() {\n" +
			"		/*start*/@MyAnnot/*end*/ MissingType foo;\n" +
			"	}\n" +
			"}\n" +
			"@interface MyAnnot {\n" +
			"}",
			this.workingCopy,
			false,
			true,
			false
		);
		IBinding binding = ((Annotation) node).resolveAnnotationBinding();
		assertNull("Got a java element", binding.getJavaElement());
	}
	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=328969
	public void testAnnotation14() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"	void bar() {\n" +
			"		/*start*/@MyAnnot/*end*/ MissingType foo;\n" +
			"	}\n" +
			"}\n" +
			"@interface MyAnnot {\n" +
			"}",
			this.workingCopy,
			false,
			true,
			true
		);
		IBinding binding = ((Annotation) node).resolveAnnotationBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
				"Unexpected Java element",
				"@MyAnnot [in foo [in bar() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]]]",
				element
		);
	}

	/*
	 * Ensures that the correct IBindings can be retrieved from an AST
	 * (parameter annotation)
	 */
	public void testAnnotation15() throws Exception {
		createFolder("/P/src/lib");
		createFile("/P/src/lib/NonNull.java",
				"package lib;\n" +
				"import java.lang.annotation.*;\n" +
				"@Target(ElementType.PARAMETER)\n" +
				"public @interface NonNull{}\n");
		createFile("/P/src/lib/Foo.java",
				"package lib;\n" +
				"public class Foo {\n" +
				"	public <T> void bug1(@NonNull T x) { return; }\n" +
				"	public static <T> void bug2(@NonNull String x) { return; }\n" +
				"}\n");

		String barSource =
				"import lib.Foo;\n" +
				"public class Bar {\n" +
				"	void m() { new Foo().bug1(\"x\"); Foo.bug2(\"x\"); }\n" +
				"}\n";
		this.workingCopies = new ICompilationUnit[2];
		this.workingCopies[1] = getWorkingCopy("/P/src/Bar.java", barSource, this.wcOwner);

		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setProject(getJavaProject("P"));
		parser.setSource(this.workingCopies[1]);
		parser.setResolveBindings(true);
		ASTNode send = NodeFinder.perform(parser.createAST(null), barSource.indexOf("bug1"), 0).getParent();
		IBinding[] bindings = new IBinding[] { ((MethodInvocation) send).resolveMethodBinding() };
		assertBindingsEqual(
			"Llib/Foo;.bug1<T:Ljava/lang/Object;>(TT;)V%<Ljava/lang/String;>",
			bindings);
		IMethodBinding method = (IMethodBinding) bindings[0];
		assertBindingsEqual(
			"@Llib/NonNull;",
			method.getParameterAnnotations(0));
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
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"<anonymous #1> [in foo() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]]",
			element
		);
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
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"<anonymous #1> [in foo() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]]",
			element
		);
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
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"Object [in Object.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]",
			element
		);
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
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
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
		IOrdinaryClassFile classFile = getClassFile("P", getExternalJCLPathString("1.5"), "java.lang", "Enum.class");
		String source = classFile.getSource();
		MarkerInfo markerInfo = new MarkerInfo(source);
		markerInfo.astStarts = new int[] {source.indexOf("protected Enum")};
		markerInfo.astEnds = new int[] {source.indexOf('}', markerInfo.astStarts[0]) + 1};
		ASTNode node = buildAST(markerInfo, classFile);
		IBinding binding = ((MethodDeclaration) node).resolveBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"Enum(java.lang.String, int) [in Enum [in Enum.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a constructor of a binary member type is correct.
	 * (regression test for bug 119249 codeResolve, search, etc. don't work on constructor of binary inner class)
	 */
	public void testBinaryMemberTypeConstructor() throws JavaModelException {
		IOrdinaryClassFile classFile = getClassFile("P", "/P/lib.jar", "p", "W$Member.class");
		ASTNode node = buildAST(classFile);
		IBinding binding = ((MethodDeclaration) node).resolveBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"Member(p.W, java.lang.String) [in Member [in W$Member.class [in p [in lib.jar [in P]]]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a type coming from a class file is correct.
	 */
	public void testBinaryType() throws JavaModelException {
		IOrdinaryClassFile classFile = getClassFile("P", getExternalJCLPathString("1.5"), "java.lang", "String.class");
		String source = classFile.getSource();
		MarkerInfo markerInfo = new MarkerInfo(source);
		markerInfo.astStarts = new int[] {source.indexOf("public")};
		markerInfo.astEnds = new int[] {source.lastIndexOf('}') + 1};
		ASTNode node = buildAST(markerInfo, classFile);
		IBinding binding = ((TypeDeclaration) node).resolveBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"String [in String.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a type coming from a class file is correct
	 * after searching for references to this type.
	 * (regression test for bug 136016 [refactoring] CCE during Use Supertype refactoring)
	 */
	public void testBinaryType2() throws CoreException {
		IOrdinaryClassFile classFile = getClassFile("P", "lib.jar", "p", "ABC.class"); // class with no references

		// ensure classfile is open
		classFile.open(null);

		//search for references to p.ABC after adding references in exactly 1 file
		try {
			createFile(
				"/P/src/Test.java",
				"import p.ABC;\n" +
				"public class Test extends ABC {\n" +
				"}"
				);
			IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {getPackageFragmentRoot("/P/src")});
			search(classFile.getType(), IJavaSearchConstants.REFERENCES, scope, new AbstractJavaSearchTests.JavaSearchResultCollector());
		} finally {
			deleteFile("/P/src/Test.java");
		}

		String source = classFile.getSource();
		MarkerInfo markerInfo = new MarkerInfo(source);
		markerInfo.astStarts = new int[] {source.indexOf("public")};
		markerInfo.astEnds = new int[] {source.lastIndexOf('}') + 1};
		ASTNode node = buildAST(markerInfo, classFile);
		IBinding binding = ((TypeDeclaration) node).resolveBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"ABC [in ABC.class [in p [in lib.jar [in P]]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a type in a jar is correct after deleting the first project
	 * referencing it.
	 */
	public void testBinaryType3() throws CoreException, IOException {
		// force String to be put in the jar cache
		buildAST(
			"public class X {\n" +
			"    /*start*/String/*end*/ field;\n" +
			"}"
		);
		try {
			tearDownJavaProject();

			createJavaProject("P1", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
			createFile(
				"/P1/X.java",
				"public class X {\n" +
				"    /*start*/String/*end*/ field;\n" +
				"}"
			);
			ASTNode node = buildAST(getCompilationUnit("/P1/X.java"));
			IBinding binding = ((Type) node).resolveBinding();
			IJavaElement element = binding.getJavaElement();
			assertElementExists(
				"Unexpected Java element",
				"String [in String.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]",
				element
			);
		} finally {
			deleteProject("P1");
			setUpJavaProject();
		}
	}

	/*
	 * Ensures that the IJavaElement for a binary member type coming from an anoumous class file is correct.
	 * (regression test for bug 100636 [model] Can't find overriden methods of protected nonstatic inner class.)
	 */
	public void testBinaryMemberTypeFromAnonymousClassFile1() throws JavaModelException {
		IOrdinaryClassFile classFile = getClassFile("P", "/P/lib.jar", "p", "Z$1.class");
		ASTNode node = buildAST(classFile);
		IBinding binding = ((TypeDeclaration) node).resolveBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"Member [in Z$Member.class [in p [in lib.jar [in P]]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement for a binary member type coming from an anoumous class file is correct.
	 * (regression test for bug 100636 [model] Can't find overriden methods of protected nonstatic inner class.)
	 */
	public void testBinaryMemberTypeFromAnonymousClassFile2() throws JavaModelException {
		IOrdinaryClassFile classFile = getClassFile("P", "/P/lib.jar", "", "Z$1.class");
		ASTNode node = buildAST(classFile);
		IBinding binding = ((TypeDeclaration) node).resolveBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"Member [in Z$Member.class [in <default> [in lib.jar [in P]]]]",
			element
		);
	}

	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (test several kinds of elements)
	 */
	public void testCreateBindings01() throws JavaModelException {
		ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
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
			owner
		);
		this.workingCopies[1] = getWorkingCopy(
			"/P/src/Y.java",
			"public class Y extends X {\n" +
			"  void bar() {\n" +
			"    new Y() {};\n" +
			"  }\n" +
			"}",
			owner
		);
		this.workingCopies[2] = getWorkingCopy(
			"/P/src/I.java",
			"public interface I {\n" +
			"  int BAR;\n" +
			"}",
			owner
		);
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
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=297757
	 */
	public void testCreateBindings23() throws JavaModelException {
		ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		parser.setProject(getJavaProject("P"));
		WorkingCopyOwner owner = new WorkingCopyOwner() {};
		this.workingCopies = new ICompilationUnit[3];
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/p/IScriptRunnable.java",
			"package p;\n" +
			"public interface IScriptRunnable<V, E extends Exception> {\n" +
			"	public V run(Object cx, Object scope) throws E;\n" +
			"}",
			owner
		);
		this.workingCopies[1] = getWorkingCopy(
			"/P/src/p/Environment.java",
			"package p;\n" +
			"public interface Environment {\n" +
			"	public <V, E extends Exception> V execute(IScriptRunnable<V, E> code) throws E;\n" +
			"}",
			owner
		);
		this.workingCopies[2] = getWorkingCopy(
			"/P/src/X.java",
			"import p.*;\n" +
			"public class X {\n" +
			"	p.Environment env;\n" +
			"	private void test() {\n" +
			"		env.execute(new IScriptRunnable<Object, RuntimeException>() {\n" +
			"			public Object run(Object cx, Object scope) throws RuntimeException {\n" +
			"				return null;\n" +
			"			}\n" +
			"		});\n" +
			"	}\n" +
			"}",
			owner
		);
		IJavaElement[] elements = new IJavaElement[] {
			this.workingCopies[0].getType("IScriptRunnable"),
			this.workingCopies[1].getType("Environment"),
			this.workingCopies[2].getType("X").getMethod("test", new String[0]).getType("", 1)
		};
		IBinding[] bindings = parser.createBindings(elements, null);
		assertBindingsEqual(
			"Lp/IScriptRunnable<TV;TE;>;\n" +
			"Lp/Environment;\n" +
			"LX$90;",
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
			"LX$42$Y;",
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
	 * (type parameter with bound)
	 */
	public void testCreateBindings14a() throws JavaModelException {
		IBinding[] bindings = createBindings(
			"public class X<T extends java.lang.Number> {\n" +
			"}",
			this.workingCopy.getType("X").getTypeParameter("T")
		);
		assertBindingsEqual(
			"LX;:TT;",
			bindings);
	}

	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (type parameter with parameterized bound)
	 */
	public void testCreateBindings14b() throws JavaModelException {
		IBinding[] bindings = createBindings(
			"public class X<T extends java.util.List<String>> {\n" +
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
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (binary method)
	 * (regression test for bug 122650 ASTParser.createBindings(IJavaElement[]) returns wrong element)
	 */
	public void testCreateBindings18() throws CoreException {
		IBinding[] bindings = createBinaryBindings(
			"public class A {\n" +
			"  <E> void foo(E e) {\n" +
			"  }\n" +
			"}",
			getClassFile("/P/lib/A.class").getType().getMethod("foo", new String[] {"TE;"})
		);
		assertBindingsEqual(
			"LA;.foo<E:Ljava/lang/Object;>(TE;)V",
			bindings);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=160637
	 */
	public void testCreateBindings19() throws CoreException {
		IBinding[] bindings = createBinaryBindings(
			"public class A {\n" +
			"  String foo(String s) {\n" +
			"		return null;\n" +
			"  }\n" +
			"}",
			getClassFile("/P/lib/A.class").getType().getMethod("foo", new String[] {"Ljava.lang.String;"})
		);
		assertBindingsEqual(
			"LA;.foo(Ljava/lang/String;)Ljava/lang/String;",
			bindings);
	}

	/*
	 * Ensures that the correct IBinding is created for an IField starting with a 'L'
	 * (regression test for 205860 ASTParser.createBindings() returns [null])
	 */
	public void testCreateBindings20() throws CoreException {
		IField field = getClassFile("/P/lib/A.class").getType().getField("LINE");
		IBinding[] bindings = createBinaryBindings(
			"public class A {\n" +
			"  static int LINE = 0;\n" +
			"}",
			field
		);
		assertBindingsEqual(
			"LA;.LINE)I",
			bindings);
	}

	/*
	 * Ensures that the correct IBinding is created for an IField starting with a 'T'
	 * (regression test for 205860 ASTParser.createBindings() returns [null])
	 */
	public void testCreateBindings21() throws CoreException {
		IField field = getClassFile("/P/lib/A.class").getType().getField("THREE");
		IBinding[] bindings = createBinaryBindings(
			"public class A {\n" +
			"  static int THREE = 0;\n" +
			"}",
			field
		);
		assertBindingsEqual(
			"LA;.THREE)I",
			bindings);
	}

	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (annotation)
	 */
	public void testCreateBindings22() throws JavaModelException {
		IBinding[] bindings = createBindings(
			"@MyAnnot\n" +
			"public class X {\n" +
			"}\n" +
			"@interface MyAnnot {\n" +
			"}",
			this.workingCopy.getType("X").getAnnotation("MyAnnot")
		);
		assertBindingsEqual(
			"LX;@LX~MyAnnot;",
			bindings);
	}

	/*
	 * Ensures that the correct IBinding is created for package-info.class's IType
	 */
	public void testCreateBindings24() throws CoreException {
		createClassFile(
			"/P/lib",
			"pack/package-info.class",
			"@Deprecated\n" +
			"package pack;");
		IJavaProject javaProject = getJavaProject("P");
		IPackageFragment pack = javaProject.findPackageFragment(new Path("/P/lib/pack"));
		IType type = pack.getOrdinaryClassFile("package-info.class").getType();
		ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		parser.setProject(javaProject);
		IJavaElement[] elements = new IJavaElement[] {type};
		IBinding[] bindings = parser.createBindings(elements, null);
		assertBindingsEqual(
			"Lpack/package-info;",
			bindings);
		IAnnotationBinding[] annotations = ((ITypeBinding) bindings[0]).getAnnotations();
		assertBindingsEqual(
			"@Ljava/lang/Deprecated;",
			annotations);
	}

	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (method arguments)
	 */
	public void testCreateBindings25() throws JavaModelException {
		this.workingCopy.getBuffer().setContents(
				"public class X {\n" +
				"  void foo(String str, int i) {}\n" +
				"}");
		this.workingCopy.makeConsistent(null);
		IMethod method = this.workingCopy.getType("X").getMethod("foo", new String[]{"QString;", "I"});
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setProject(getJavaProject("P"));
		IBinding[] bindings = parser.createBindings(method.getParameters(), null);
		assertBindingsEqual(
			"LX;.foo(Ljava/lang/String;I)V#str#0#0\n" + // occurrence 0, rank 0
			"LX;.foo(Ljava/lang/String;I)V#i#0#1", // occurrence 0, rank 1
			bindings);
	}

	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (binary method arguments)
	 */
	public void testCreateBindings26() throws CoreException {
		createClassFile("/P/lib", "A.class",
				"public class A {\n" +
				"  void foo(String str, int i) {}\n" +
				"}");
		IMethod method = getClassFile("/P/lib/A.class").getType().getMethod("foo", new String[] {"Ljava.lang.String;", "I"});
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setProject(getJavaProject("P"));
		IBinding[] bindings = parser.createBindings(method.getParameters(), null);
		assertBindingsEqual(
			"LA;.foo(Ljava/lang/String;I)V#str#0#0\n" + // occurrence 0, rank 0
			"LA;.foo(Ljava/lang/String;I)V#i#0#1", // occurrence 0, rank 1
			bindings);
	}

	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (invocation of a generic method - binary)
	 */
	public void testCreateBinding27() throws Exception {
		createClassFile("/P/lib", "p/A.class",
				"package p;\n" +
				"public class A {\n" +
				"  public static <T> T foo(T[] arg) { return arg[0]; }\n" +
				"}");
		this.workingCopies = new ICompilationUnit[1];
		String xSource = "public class X {\n" +
						"  public String test(String[] args) {\n" +
						"    return p.A.foo(args);\n" +
						"  }\n" +
						"}";
		this.workingCopies[0] = getWorkingCopy(
			"/P/src/X.java",
			xSource,
			this.wcOwner
		);

		IJavaElement elem= this.workingCopies[0].codeSelect(xSource.indexOf("foo"), 0)[0];
		ASTParser parser = ASTParser.newParser(getJLS8());
		parser.setProject(getJavaProject("P"));
		IBinding[] bindings = parser.createBindings(new IJavaElement[]{ elem }, null);
		assertBindingsEqual(
			"Lp/A;.foo<T:Ljava/lang/Object;>([TT;)TT;%<Ljava/lang/String;>",
			bindings);
		IMethodBinding method = (IMethodBinding) bindings[0];
		assertBindingsEqual(
			"[Ljava/lang/String;",
			method.getParameterTypes());
			assertBindingsEqual(
				"Ljava/lang/String;",
				new IBinding[] {method.getReturnType()});
	}

	/*
	 * Ensures that the correct IBindings are created for a given set of IJavaElement
	 * (method parameter - binary)
	 */
	public void testCreateBinding28() throws Exception {
		IJavaProject javaProject = getJavaProject("P");
		String codeGenOption = javaProject.getOption(JavaCore.COMPILER_LOCAL_VARIABLE_ATTR, true);
		try {
			javaProject.setOption(JavaCore.COMPILER_LOCAL_VARIABLE_ATTR, JavaCore.DISABLED);
			createClassFile("/P/lib", "p/A.class",
					"package p;\n" +
					"public class A {\n" +
					"  public static <T> T foo(int i, boolean f) { return null; }\n" +
					"}");
			IType typeA = javaProject.findType("p.A");

			IJavaElement[] elems= typeA.getMethod("foo", new String[]{"I", "Z"}).getParameters();
			ASTParser parser = ASTParser.newParser(getJLS8());
			parser.setProject(javaProject);
			IBinding[] bindings = parser.createBindings(elems, null);
			assertBindingsEqual(
				"Lp/A;.foo<T:Ljava/lang/Object;>(IZ)TT;#arg0#0#0\n" +
				"Lp/A;.foo<T:Ljava/lang/Object;>(IZ)TT;#arg1#0#1",
				bindings);
			IVariableBinding param1 = (IVariableBinding) bindings[0];
			IVariableBinding param2 = (IVariableBinding) bindings[1];
			assertBindingsEqual(
				"I\n" +
				"Z",
				new IBinding[] {
					param1.getType(),
					param2.getType()});
		} finally {
			javaProject.setOption(JavaCore.COMPILER_LOCAL_VARIABLE_ATTR, codeGenOption);
		}
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
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"field [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
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
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"field [in <anonymous #1> [in foo() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]]]",
			element
		);
	}

	/*
	 * Ensures that an IType can be found using its binding key.
	 */
	public void testFindElement01() throws JavaModelException {
		String bindingKey = buildBindingKey(
			"/*start*/public class X {\n" +
			"}/*end*/"
		);
		assertFindElement(
			bindingKey,
			"X [in [Working copy] X.java [in <default> [in src [in P]]]]"
		);
	}

	/*
	 * Ensures that an IMethod can be found using its binding key.
	 */
	public void testFindElement02() throws JavaModelException {
		String bindingKey = buildBindingKey(
			"public class X {\n" +
			"  /*start*/void foo() {\n" +
			"  }/*end*/\n" +
			"}"
		);
		assertFindElement(
			bindingKey,
			"foo() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]"
		);
	}

	/*
	 * Ensures that an IField can be found using its binding key.
	 */
	public void testFindElement03() throws JavaModelException {
		String bindingKey = buildBindingKey(
			"public class X {\n" +
			"  int /*start*/field/*end*/;\n" +
			"}"
		);
		assertFindElement(
			bindingKey,
			"field [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]"
		);
	}

	/*
	 * Ensures that a member IType can be found using its binding key.
	 */
	public void testFindElement04() throws JavaModelException {
		String bindingKey = buildBindingKey(
			"public class X {\n" +
			"  /*start*/class Member {\n" +
			"  }/*end*/\n" +
			"}"
		);
		assertFindElement(
			bindingKey,
			"Member [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]"
		);
	}

	/*
	 * Ensures that a local IType can be found using its binding key.
	 */
	public void testFindElement05() throws JavaModelException {
		String bindingKey = buildBindingKey(
			"public class X {\n" +
			"  void foo() {\n" +
			"    /*start*/class Local {\n" +
			"    }/*end*/\n" +
			"  }\n" +
			"}"
		);
		assertFindElement(
			bindingKey,
			"Local [in foo() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]]"
		);
	}

	/*
	 * Ensures that an anonymous IType can be found using its binding key.
	 */
	public void testFindElement06() throws JavaModelException {
		String bindingKey = buildBindingKey(
			"public class X {\n" +
			"  void foo() {\n" +
			"    new X() /*start*/{\n" +
			"    }/*end*/;\n" +
			"  }\n" +
			"}"
		);
		assertFindElement(
			bindingKey,
			"<anonymous #1> [in foo() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]]"
		);
	}

	/*
	 * Ensures that a secondary IType can be found using its binding key.
	 */
	public void testFindElement07() throws JavaModelException {
		String bindingKey = buildBindingKey(
			"public class X {\n" +
			"}\n" +
			"/*start*/class Secondary {\n" +
			"}/*end*/"
		);
		assertFindElement(
			bindingKey,
			"Secondary [in [Working copy] X.java [in <default> [in src [in P]]]]"
		);
	}

	/*
	 * Ensures that an IAnnotation can be found using its binding key.
	 */
	public void testFindElement08() throws JavaModelException {
		String bindingKey = buildBindingKey(
			"/*start*/@MyAnnot/*end*/\n" +
			"public class X {\n" +
			"}\n" +
			"@interface MyAnnot {\n" +
			"}"
		);
		assertFindElement(
			bindingKey,
			"@MyAnnot [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]"
		);
	}

	/*
	 * Ensures that an IPackageFragment can be found using its binding key.
	 */
	public void testFindElement09() throws JavaModelException {
		String bindingKey = buildBindingKey(
			"public class X {\n" +
			"  /*start*/java.lang/*end*/.String field;\n" +
			"}"
		);
		assertFindElement(
			bindingKey,
			"java.lang [in "+ getExternalJCLPathString("1.5") + "]"
		);
	}

	/*
	 * Ensures that an ITypeParameter can be found using its binding key.
	 */
	public void testFindElement10() throws JavaModelException {
		String bindingKey = buildBindingKey(
			"public class X</*start*/T/*end*/> {\n" +
			"}"
		);
		assertFindElement(
			bindingKey,
			"<T> [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]"
		);
	}

	/*
	 * Ensures that a binary top level IType can be found using its binding key.
	 */
	public void testFindElement11() throws JavaModelException {
		String bindingKey = getClassFile("/P/lib.jar/p/Y.class").getType().getKey();
		assertFindElement(
			bindingKey,
			"Y [in Y.class [in p [in lib.jar [in P]]]]"
		);
	}

	/*
	 * Ensures that a binary member IType can be found using its binding key.
	 */
	public void testFindElement12() throws JavaModelException {
		String bindingKey = getClassFile("/P/lib.jar/p/Z$Member.class").getType().getKey();
		assertFindElement(
			bindingKey,
			"Member [in Z$Member.class [in p [in lib.jar [in P]]]]"
		);
	}

	/*
	 * Ensures that a binary anonymous IType can be found using its binding key.
	 */
	public void testFindElement13() throws JavaModelException {
		String bindingKey = getClassFile("/P/lib.jar/p/Z$1.class").getType().getKey();
		assertFindElement(
			bindingKey,
			"Z$1.class [in p [in lib.jar [in P]]]"
		);
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
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"Y [in foo() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]]",
			element
		);
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
				this.workingCopy.getOwner()
			);
			IBinding[] bindings = requestor.getBindings(bindingKeys);

			// Ensure the Java element is correct
			IJavaElement element = bindings[0].getJavaElement();
			assertElementExists(
				"Unexpected Java element",
				"Local [in foo() [in Z [in Z.java [in <default> [in src [in P]]]]]]",
				element
			);
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
		IJavaElement element = binding.getJavaElement();
		IJavaElement expected = getLocalVariable(this.workingCopy, "arg", "arg");
		assertEquals(
			"Unexpected Java element",
			expected,
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a local variable in an anonymous type
	 * in a discarded working copy is null.
	 * (regression test for bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=212096 )
	 */
	public void testLocalVariable4() throws JavaModelException {
		try {
			ASTNode node = buildAST(
				"public class X {\n" +
				"  void foo() {\n" +
				"    new Object() {\n" +
				"      void bar() {\n" +
				"        int /*start*/local/*end*/;\n" +
				"      }\n" +
				"    };\n" +
				"  }\n" +
				"}"
			);
			IBinding binding = ((VariableDeclaration) node).resolveBinding();
			this.workingCopy.discardWorkingCopy();
			IJavaElement element = binding.getJavaElement();
			assertEquals(
				"Unexpected Java element",
				null,
				element
			);
		} finally {
			setUpWorkingCopy();
		}
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a local variable in an initializer is correct.
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=217287 )
	 */
	public void testLocalVariable5() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  {\n" +
			"    int /*start*/local/*end*/;\n" +
			"  }\n" +
			"}"
		);
		IBinding binding = ((VariableDeclaration) node).resolveBinding();
		IJavaElement element = binding.getJavaElement();
		IJavaElement expected = getLocalVariable(this.workingCopy, "local", "local");
		assertEquals(
			"Unexpected Java element",
			expected,
			element
		);
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=48420
	 */
	public void testLocalVariable6() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"	{\n" +
			"		int local;\n" +
			"	}\n" +
			"	public void foo() {\n" +
			"		int i = 0;\n" +
			"	}\n" +
			"	public static void foo(final int n) {\n" +
			"		int i;\n" +
			"	}\n" +
			"	public X(final int j) {\n" +
			"		int i;\n" +
			"	}\n" +
			"}"
		);
		node.accept(new ASTVisitor() {
			public boolean visit(VariableDeclarationFragment fragment) {
				final IVariableBinding binding = fragment.resolveBinding();
				final IJavaElement javaElement = binding.getJavaElement();
				assertNotNull("No java element", javaElement);
				final int type = javaElement.getElementType();
				assertEquals("Wrong type", IJavaElement.LOCAL_VARIABLE, type);
				ILocalVariable variable = (ILocalVariable) javaElement;
				final ITypeRoot typeRoot = variable.getTypeRoot();
				assertNotNull("Not type root", typeRoot);
				assertTrue("Invalid", typeRoot.exists());
				assertNotNull("No declaring element", variable.getDeclaringMember());
				int flags = variable.getFlags();
				assertFalse("wrong modifier for " + variable.getElementName(), Flags.isFinal(flags));
				assertFalse("wrong value for isParameter" + variable.getElementName(), variable.isParameter());
				return true;
			}
			public boolean visit(SingleVariableDeclaration variableDeclaration) {
				final IVariableBinding binding = variableDeclaration.resolveBinding();
				final IJavaElement javaElement = binding.getJavaElement();
				assertNotNull("No java element", javaElement);
				final int type = javaElement.getElementType();
				assertEquals("Wrong type", IJavaElement.LOCAL_VARIABLE, type);
				ILocalVariable variable = (ILocalVariable) javaElement;
				final ITypeRoot typeRoot = variable.getTypeRoot();
				assertNotNull("Not type root", typeRoot);
				assertTrue("Invalid", typeRoot.exists());
				assertNotNull("No declaring element", variable.getDeclaringMember());
				int flags = variable.getFlags();
				assertTrue("wrong modifier for " + variable.getElementName(), Flags.isFinal(flags));
				assertTrue("wrong value for isParameter" + variable.getElementName(), variable.isParameter());
				return true;
			}
		});
	}

	/*
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=368646
	 */
	public void testLocalVariable7() throws JavaModelException {
		final String source = "public class X {\n" +
				"	public void m(String strX) {\n" +
				"		String strB = strX;\n" +
				"	}\n" +
				"}";
		ASTNode node = buildAST(source);
		final boolean[] checked = new boolean[1];
		node.accept(new ASTVisitor() {
			public boolean visit(VariableDeclarationFragment fragment) {
				final IVariableBinding binding = fragment.resolveBinding();
				final IJavaElement javaElement = binding.getJavaElement();
				assertNotNull("No java element", javaElement);
				final int type = javaElement.getElementType();
				assertEquals("Wrong type", IJavaElement.LOCAL_VARIABLE, type);
				ILocalVariable variable = (ILocalVariable) javaElement;
				ISourceRange range = variable.getNameRange();
				checkSourceRange(range.getOffset(), range.getLength(), "strB", source);
				try {
					range = variable.getSourceRange();
					checkSourceRange(range.getOffset(), range.getLength(), "String strB = strX;", source);
				} catch(JavaModelException e) {
					assertTrue("failed to retrieve the source range", false);
				}
				checked[0] = true;
				return true;
			}
		});
		assertTrue("Not checked", checked[0]);
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
	public void testMethod01() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X<K, V> {\n" +
			"  /*start*/void foo(int i, Object o, java.lang.String s, Class[] c, X<K, V> x) {\n" +
			"  }/*end*/\n" +
			"}"
		);
		IBinding binding = ((MethodDeclaration) node).resolveBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"foo(int, Object, java.lang.String, Class[], X<K,V>) [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method is correct.
	 */
	public void testMethod02() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X<K, V> {\n" +
			"  /*start*/void foo() {\n" +
			"  }/*end*/\n" +
			"}"
		);
		IBinding binding = ((MethodDeclaration) node).resolveBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"foo() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method is correct.
	 * (regression test for bug 78757 MethodBinding.getJavaElement() returns null)
	 */
	public void testMethod03() throws JavaModelException {
		ICompilationUnit otherWorkingCopy = null;
		try {
			otherWorkingCopy = getWorkingCopy(
				"/P/src/Y.java",
				"public class Y {\n" +
				"  void foo(int i, String[] args, java.lang.Class clazz) {}\n" +
				"}",
				this.workingCopy.getOwner()
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
			IJavaElement element = binding.getJavaElement();
			assertElementExists(
				"Unexpected Java element",
				"foo(int, String[], java.lang.Class) [in Y [in [Working copy] Y.java [in <default> [in src [in P]]]]]",
				element
			);
		} finally {
			if (otherWorkingCopy != null)
				otherWorkingCopy.discardWorkingCopy();
		}
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method is correct.
	 * (regression test for bug 81258 IMethodBinding#getJavaElement() is null with inferred method parameterization)
	 */
	public void testMethod04() throws JavaModelException {
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
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"bar(A<? extends T>) [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a parameterized method is correct.
	 * (regression test for bug 82382 IMethodBinding#getJavaElement() for method m(T t) in parameterized type Gen<T> is null)
	 */
	public void testMethod05() throws JavaModelException {
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
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"m(T) [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method inside an annotation is correct.
	 * (regression test for bug 83300 [1.5] ClassCastException in #getJavaElement() on binding of annotation element)
	 */
	public void testMethod06() throws JavaModelException {
		ASTNode node = buildAST(
			"@X(/*start*/value/*end*/=\"Hello\", count=-1)\n" +
			"@interface X {\n" +
			"    String value();\n" +
			"    int count();\n" +
			"}"
		);
		IBinding binding = ((SimpleName) node).resolveBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"value() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method with array parameters is correct.
	 * (regression test for bug 88769 IMethodBinding#getJavaElement() drops extra array dimensions and varargs
	 */
	public void testMethod07() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  /*start*/public int[] bar(int a[]) {\n" +
			"    return a;\n" +
			"  }/*end*/\n" +
			"}"
		);
		IBinding binding = ((MethodDeclaration) node).resolveBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"bar(int[]) [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method with array parameters is correct.
	 * (regression test for bug 88769 IMethodBinding#getJavaElement() drops extra array dimensions and varargs
	 */
	public void testMethod08() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  /*start*/public Object[] bar2(Object[] o[][]) [][] {\n" +
			"    return o;\n" +
			"  }/*end*/\n" +
			"}"
		);
		IBinding binding = ((MethodDeclaration) node).resolveBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"bar2(Object[][][]) [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method with varargs parameters is correct.
	 * (regression test for bug 88769 IMethodBinding#getJavaElement() drops extra array dimensions and varargs
	 */
	public void testMethod09() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  /*start*/public void bar3(Object... objs) {\n" +
			"  }/*end*/\n" +
			"}"
		);
		IBinding binding = ((MethodDeclaration) node).resolveBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"bar3(Object[]) [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
	}

	/*
	 * Ensures that getting the IJavaElement of an IBinding representing a method in an anonymous type
	 * doesn't throw a ClassCastException if there is a syntax error.
	 * (regression test for bug 149853 CCE in IMethodBinding#getJavaElement() for recovered anonymous type)
	 */
	public void testMethod10() throws CoreException {
		try {
			// use a compilation unit instead of a working copy to use the ASTParser instead of reconcile
			createFile(
				"/P/src/Test.java",
				"public class X {\n" +
				"        void test() {\n" +
				"                new Object() {\n" +
				"                        /*start*/public void yes() {\n" +
				"                                System.out.println(\"hello world\");\n" +
				"                        }/*end*/\n" +
				"                } // missing semicolon;\n" +
				"        }\n" +
				"}"
			);
			ICompilationUnit cu = getCompilationUnit("/P/src/Test.java");

			ASTNode node = buildAST(null/*use existing contents*/, cu, false/*don't report errors*/, true/*statement recovery*/, false);
			IBinding binding = ((MethodDeclaration) node).resolveBinding();
			IJavaElement element = binding.getJavaElement();
			assertElementExists(
				"Unexpected Java element",
				"yes() [in <anonymous #1> [in test() [in X [in Test.java [in <default> [in src [in P]]]]]]]",
				element
			);
		} finally {
			deleteFile("/P/src/Test.java");
		}
	}

	/*
	 * Ensures that no ClassCastException is thrown if the method is in an anonymous
	 * which is inside the initializer of another anonymous.
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=208013)
	 */
	public void testMethod11() throws JavaModelException {
		ASTNode node = buildAST(
			"public class X {\n" +
			"  public void foo() {\n" +
			"    new Object() {\n" +
			"      Object o;\n" +
			"      {\n"+
			"        new Object() {\n" +
			"          /*start*/void bar() {\n" +
			"          }/*end*/\n" +
			"		  };\n" +
			"      }\n" +
			"    };\n"+
			"  }\n" +
			"}"
		);
		IBinding binding = ((MethodDeclaration) node).resolveBinding();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"bar() [in <anonymous #1> [in <initializer #1> [in <anonymous #1> [in foo() [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]]]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method is correct.
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=249567 )
	 */
	public void testMethod12() throws Exception {
		try {
			createFolder("/P/src/p1");
			createFile(
				"/P/src/p1/X249567.java",
				"package p1;\n" +
				"public class X249567 {}"
			);
			createFolder("/P/src/p2");
			createFile(
				"/P/src/p2/X249567.java",
				"package p2;\n" +
				"public class X249567 {}"
			);
			ASTNode node = buildAST(
				"public class X {\n" +
				"  void foo(p1.X249567 x) {\n" +
				"  }\n" +
				"  /*start*/void foo(p2.X249567 x) {\n" +
				"  }/*end*/\n" +
				"}"
			);
			IBinding binding = ((MethodDeclaration) node).resolveBinding();
			IJavaElement element = binding.getJavaElement();
			assertElementExists(
				"Unexpected Java element",
				"foo(p2.X249567) [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
				element
			);
		} finally {
			deleteFolder("/P/src/p1");
			deleteFolder("/P/src/p2");
		}
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method is correct.
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=249567 )
	 */
	public void testMethod13() throws Exception {
		try {
			createFolder("/P/src/p1");
			createFile(
				"/P/src/p1/X249567.java",
				"package p1;\n" +
				"public class X249567 {\n" +
				"  public class Member {}\n" +
				"}"
			);
			ASTNode node = buildAST(
				"public class X {\n" +
				"  /*start*/void foo(p1.X249567.Member x) {\n" +
				"  }/*end*/\n" +
				"}"
			);
			IBinding binding = ((MethodDeclaration) node).resolveBinding();
			IJavaElement element = binding.getJavaElement();
			assertElementExists(
				"Unexpected Java element",
				"foo(p1.X249567.Member) [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
				element
			);
		} finally {
			deleteFolder("/P/src/p1");
		}
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a method is correct.
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=249567 )
	 */
	public void testMethod14() throws Exception {
		try {
			createFolder("/P/src/p1");
			createFile(
				"/P/src/p1/X249567.java",
				"package p1;\n" +
				"public class X249567 {\n" +
				"  public class Member<T> {}\n" +
				"}"
			);
			ASTNode node = buildAST(
				"public class X {\n" +
				"  /*start*/void foo(p1.X249567.Member<java.lang.String> x) {\n" +
				"  }/*end*/\n" +
				"}"
			);
			IBinding binding = ((MethodDeclaration) node).resolveBinding();
			IJavaElement element = binding.getJavaElement();
			assertElementExists(
				"Unexpected Java element",
				"foo(p1.X249567.Member<java.lang.String>) [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
				element
			);
		} finally {
			deleteFolder("/P/src/p1");
		}
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
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"java.lang [in "+ getExternalJCLPathString("1.5") + "]",
			element
		);
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
		IPackageBinding binding = typeBinding.getPackage();
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"<default> [in src [in P]]",
			element
		);
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
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"Comparable [in Comparable.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]",
			element
		);
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
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"Y(T) [in Y [in Y.class [in p [in lib.jar [in P]]]]]",
			element
		);
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
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"Comparable [in Comparable.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]",
			element
		);
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a recovered type is correct.
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=232037 )
	 */
	public void testRecoveredTypeBinding1() throws Exception {
		try {
			IJavaProject p = createJavaProject("P15", new String[] {""}, new String[] {"JCL15_LIB", "/P15/util.jar"}, "", "1.5");
			org.eclipse.jdt.core.tests.util.Util.createJar(new String[] {
				"java/util/List.java",
				"package java.util;\n" +
				"public class List<T> {\n" +
				"}"
			}, p.getProject().getLocation() + File.separator + "util.jar",
			"1.5");
			p.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
			createFile("/P15/X.java", "");
			ASTNode node = buildAST(
				"public class X {\n" +
				"  void m() {\n" +
				"    /*start*/new java.util.List<URL>()/*end*/;\n" +
				"  }\n" +
				"}",
				getCompilationUnit("/P15/X.java"),
				false/*don't report errors*/,
				true/*statement recovery*/,
				true/*binding recovery*/
			);
			IBinding binding = ((ClassInstanceCreation) node).resolveTypeBinding();
			IJavaElement element = binding.getJavaElement();
			assertEquals(element, element); // equals should not throw an NPE
		} finally {
			deleteProject("P15");
		}
	}

	/*
	 * Ensures that the IJavaElement of an IBinding representing a recovered type is correct.
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=232037 )
	 */
	public void testRecoveredTypeBinding2() throws Exception {
		try {
			createJavaProject("P15", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
			createFile("/P15/X.java", "");
			ASTNode node = buildAST(
				"public class X {\n" +
				"  void m() {\n" +
				"    new /*start*/java.util.List<URL>/*end*/();\n" +
				"  }\n" +
				"}",
				getCompilationUnit("/P15/X.java"),
				false/*don't report errors*/,
				true/*statement recovery*/,
				true/*binding recovery*/
			);
			IBinding binding = ((ParameterizedType) node).resolveBinding();
			IJavaElement element = binding.getJavaElement();
			assertEquals(element, element); // equals should not throw an NPE
		} finally {
			deleteProject("P15");
		}
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
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"X [in [Working copy] X.java [in <default> [in src [in P]]]]",
			element
		);
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
			IJavaElement element = binding.getJavaElement();
			assertElementExists(
				"Unexpected Java element",
				"Y [in Y.java [in <default> [in src [in P]]]]",
				element
			);
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
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"String [in String.class [in java.lang [in "+ getExternalJCLPathString("1.5") + "]]]",
			element
		);
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
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"<T> [in X [in [Working copy] X.java [in <default> [in src [in P]]]]]",
			element
		);
		assertEquals("Wrong type", IJavaElement.TYPE_PARAMETER, element.getElementType());
		ITypeParameter typeParameter = (ITypeParameter) element;
		final ITypeRoot typeRoot = typeParameter.getTypeRoot();
		assertNotNull("Not type root", typeRoot);
		assertTrue("Invalid", typeRoot.exists());
	}

	/*
	 * Ensures that we can create a binding for an IJavaElement representing a type parameter (source type)
	 * Bug 466279 - [hovering] IAE on hover when annotation-based null analysis is enabled
	 */
	public void testTypeParameter2() throws CoreException {
		ICompilationUnit cu = null;
		try {
			ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
			parser.setResolveBindings(true);
			parser.setProject(getJavaProject("P"));
			createFolder("/P/src/p");
			createFile("/P/src/p/X.java",
				"package p;\n" +
				"public interface X<T> {\n" +
				"  public T foo(int i, String s);\n" +
				"}");
			cu = (ICompilationUnit) getJavaProject("P").findElement(new Path("p/X.java"));
			cu.becomeWorkingCopy(null);
			IJavaElement[] elements = new IJavaElement[] {
					cu.getType("X"),
					cu.getType("X").getTypeParameters()[0],
				};
			IBinding[] bindings = parser.createBindings(elements, null);
			assertBindingsEqual(
				"Lp/X<TT;>;\n" +
				"Lp/X;:TT;",
				bindings);

			IJavaElement element = bindings[1].getJavaElement();
			assertElementExists(
				"Unexpected Java element",
				"<T> [in X [in [Working copy] X.java [in p [in src [in P]]]]]",
				element
			);
			assertEquals("Wrong type", IJavaElement.TYPE_PARAMETER, element.getElementType());
			ITypeParameter typeParameter = (ITypeParameter) element;
			ITypeRoot typeRoot = typeParameter.getTypeRoot();
			assertNotNull("Not type root", typeRoot);
			assertTrue("Invalid", typeRoot.exists());
		} finally {
			if (cu != null)
				cu.discardWorkingCopy();
			deleteFile("/P/src/p/X.java");
			deleteFolder("/P/src/p");
		}
	}

	/*
	 * Ensures that we can create a binding for an IJavaElement representing a type parameter (binary type)
	 * Bug 466279 - [hovering] IAE on hover when annotation-based null analysis is enabled
	 */
	public void testTypeParameter3() throws CoreException {
		try {
			createClassFile("/P/lib", "A.class",
				"package lib;\n" +
				"public interface A<T,Z> {\n" +
				"  public T foo(int i, String s);\n" +
				"}");
			IJavaProject javaProject = getJavaProject("P");
			IJavaElement[] elements = new IJavaElement[] {
					javaProject.findType("lib.A"),
					javaProject.findType("lib.A").getTypeParameters()[0],
					javaProject.findType("lib.A").getTypeParameters()[1]
				};
			ASTParser parser = ASTParser.newParser(getJLS8());
			parser.setProject(javaProject);
			IBinding[] bindings = parser.createBindings(elements, null);
			assertBindingsEqual(
				"Llib/A<TT;TZ;>;\n" +
				"Llib/A;:TT;\n" +
				"Llib/A;:TZ;",
				bindings);
		} finally {
			deleteFile("/P/lib/A.class");
			deleteFolder("/P/lib");
		}
	}

	/*
	 * Ensures that we can create a binding for an IJavaElement representing a type parameter (binary method)
	 * Bug 466279 - [hovering] IAE on hover when annotation-based null analysis is enabled
	 */
	public void testTypeParameter4() throws CoreException {
		try {
			createFolder("P/lib");
			createClassFile("/P/lib", "A.class",
				"package lib;\n" +
				"public interface A {\n" +
				"  public <T,Z> T foo(int i, Z s);\n" +
				"}");
			IJavaProject javaProject = getJavaProject("P");
			IMethod method = javaProject.findType("lib.A").getMethod("foo", new String[] { "I", "TZ;" });
			IJavaElement[] elements = new IJavaElement[] {
					method,
					method.getTypeParameters()[0],
					method.getTypeParameters()[1]
				};
			ASTParser parser = ASTParser.newParser(getJLS8());
			parser.setProject(javaProject);
			IBinding[] bindings = parser.createBindings(elements, null);
			assertBindingsEqual(
				"Llib/A;.foo<T:Ljava/lang/Object;Z:Ljava/lang/Object;>(ITZ;)TT;\n" +
				"Llib/A;.foo<T:Ljava/lang/Object;Z:Ljava/lang/Object;>(ITZ;)TT;:TT;\n" +
				"Llib/A;.foo<T:Ljava/lang/Object;Z:Ljava/lang/Object;>(ITZ;)TT;:TZ;",
				bindings);
		} finally {
			deleteFile("/P/lib/A.class");
			deleteFolder("/P/lib");
		}
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
		IJavaElement element = binding.getJavaElement();
		assertElementExists(
			"Unexpected Java element",
			"<null>",
			element
		);
	}
	/**
	 * Test behavior when the binding key denotes a non existent type.
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=157847"
	 */
	public void test157847a() throws CoreException {
		String filePath = "/P/src/Bug157847A.java";
		try {
			String contents =
				"public class Bug157847A<T> {\n" +
				"	void add(Y<? extends T> l) {}\n" +
				"}\n"+
                "interface Y<T> {}\n";
			createFile(filePath, contents);

			BindingRequestor requestor = new BindingRequestor();
			String[] bindingKeys = new String[] {"LBug157847A~ThisTypeDoesNotExist;"};
			resolveASTs(
				new ICompilationUnit[] {},
				bindingKeys,
				requestor,
				getJavaProject("P"),
				this.workingCopy.getOwner()
			);
			IBinding[] bindings = requestor.getBindings(bindingKeys);
			assertTrue("Constructed non existing type", bindings.length == 0);
		} finally {
			deleteFile(filePath);
		}
	}
	/**
	 * Ensures that we don't create internally inconsistent wildcard
	 * bindings of the form '? extends <null>' or '? super <null>'
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=157847"
	 */
	public void test157847b() throws CoreException {
		String filePath = "/P/src/Bug157847B.java";
		try {
			String contents =
				"public class Bug157847B<T> {\n" +
				"	void add(Y<? super T> l) {}\n" +
				"}\n"+
                "interface Y<T> {}\n";
			createFile(filePath, contents);

			BindingRequestor requestor = new BindingRequestor();
			String[] bindingKeys = new String[] {"LBug157847B~Y<LBug157847B~Y;{0}-!LBug157847B;{0}*54;>;"};
			resolveASTs(
				new ICompilationUnit[] {},
				bindingKeys,
				requestor,
				getJavaProject("P"),
				this.workingCopy.getOwner()
			);
			IBinding[] bindings = requestor.getBindings(bindingKeys);
			assertTrue("Constructed bogus wildcard", bindings.length == 0);
		} finally {
			deleteFile(filePath);
		}
	}
	/**
	 * Ensures that we don't create internally inconsistent wildcard
	 * bindings of the form '? extends <null>' or '? super <null>'
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=157847"
	 */
	public void test157847c() throws CoreException {
		String filePath = "/P/src/Bug157847C.java";
		try {
			String contents =
				"public class Bug157847C<T> {\n" +
				"	void add(Y<? extends T> l) {}\n" +
				"}\n"+
                "interface Y<T> {}\n";
			createFile(filePath, contents);

			BindingRequestor requestor = new BindingRequestor();
			String[] bindingKeys = new String[] {"LBug157847C~Y<LBug157847C~Y;{0}+!LBug157847C;{0}*54;>;"};
			resolveASTs(
				new ICompilationUnit[] {},
				bindingKeys,
				requestor,
				getJavaProject("P"),
				this.workingCopy.getOwner()
			);
			IBinding[] bindings = requestor.getBindings(bindingKeys);
			assertTrue("Constructed bogus wildcard", bindings.length == 0);
		} finally {
			deleteFile(filePath);
		}
	}
	public void test320802() throws CoreException {
		String filePath = "/P/src/X.java";
		String filePathY = "/P/src/p/Y.java";
		try {
			String contents =
				"import p.Y;\n" +
				"public class X {\n" +
				"	Y<MissingType1, MissingType2> y;\n" +
				"	public X() {\n" +
				"		this.y = new Y<MissingType1, MissingType2>();\n" +
				"	}\n" +
				"}";
			createFile(filePath, contents);
			ICompilationUnit compilationUnit = getCompilationUnit("P", "src", "", "X.java");
			assertTrue(compilationUnit.exists());

			String contents2 =
				"package p;\n" +
				"public class Y<T, U> {}";
			createFolder("/P/src/p");
			createFile(filePathY, contents2);
			ICompilationUnit compilationUnit2 = getCompilationUnit("P", "src", "p", "Y.java");
			assertTrue(compilationUnit2.exists());

			final CompilationUnit[] asts = new CompilationUnit[1];
			BindingRequestor requestor = new BindingRequestor() {
				public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
					asts[0] = ast;
				}
			};
			resolveASTs(
				new ICompilationUnit[] {compilationUnit},
				new String[0],
				requestor,
				getJavaProject("P"),
				this.workingCopy.getOwner()
			);
			assertNotNull("No ast", asts[0]);
			final IProblem[] problems = asts[0].getProblems();
			String expectedProblems =
				"1. ERROR in /P/src/X.java (at line 3)\n" +
				"	Y<MissingType1, MissingType2> y;\n" +
				"	  ^^^^^^^^^^^^\n" +
				"MissingType1 cannot be resolved to a type\n" +
				"----------\n" +
				"2. ERROR in /P/src/X.java (at line 3)\n" +
				"	Y<MissingType1, MissingType2> y;\n" +
				"	                ^^^^^^^^^^^^\n" +
				"MissingType2 cannot be resolved to a type\n" +
				"----------\n" +
				"3. ERROR in /P/src/X.java (at line 5)\n" +
				"	this.y = new Y<MissingType1, MissingType2>();\n" +
				"	^^^^^^\n" +
				"MissingType1 cannot be resolved to a type\n" +
				"----------\n" +
				"4. ERROR in /P/src/X.java (at line 5)\n" +
				"	this.y = new Y<MissingType1, MissingType2>();\n" +
				"	^^^^^^\n" +
				"MissingType2 cannot be resolved to a type\n" +
				"----------\n" +
				"5. ERROR in /P/src/X.java (at line 5)\n" +
				"	this.y = new Y<MissingType1, MissingType2>();\n" +
				"	               ^^^^^^^^^^^^\n" +
				"MissingType1 cannot be resolved to a type\n" +
				"----------\n" +
				"6. ERROR in /P/src/X.java (at line 5)\n" +
				"	this.y = new Y<MissingType1, MissingType2>();\n" +
				"	                             ^^^^^^^^^^^^\n" +
				"MissingType2 cannot be resolved to a type\n" +
				"----------\n";
			assertProblems("Wrong problems", expectedProblems, problems, contents.toCharArray());
		} finally {
			deleteFile(filePath);
			deleteFile(filePathY);
			deleteFolder("/P/src/p");
		}
	}
}
