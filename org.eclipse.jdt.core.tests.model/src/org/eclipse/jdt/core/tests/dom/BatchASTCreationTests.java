/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
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
import java.util.ArrayList;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.tests.util.Util;

@SuppressWarnings({"rawtypes", "unchecked"})
public class BatchASTCreationTests extends AbstractASTTests {

	/**
	 * Internal synonym for deprecated constant AST.JSL3
	 * to alleviate deprecation warnings.
	 * @deprecated
	 */
	/*package*/ static final int JLS3_INTERNAL = AST.JLS3;

	public static class TestASTRequestor extends ASTRequestor {
		public ArrayList asts = new ArrayList();
		public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
			this.asts.add(ast);
		}
		public void acceptBinding(String bindingKey, IBinding binding) {
		}
	}

	class BindingResolver extends TestASTRequestor {
		private ArrayList bindingKeys = new ArrayList();
		int index = -1;
		private ArrayList foundKeys = new ArrayList();
		MarkerInfo[] markerInfos;
		public BindingResolver(MarkerInfo[] markerInfos) {
			this.markerInfos = markerInfos;
		}
		@Override
		public void acceptAST(ICompilationUnit source, CompilationUnit cu) {
			super.acceptAST(source, cu);
			ASTNode[] nodes = findNodes(cu, this.markerInfos[++this.index]);
			for (int i = 0, length = nodes.length; i < length; i++) {
				IBinding binding = resolveBinding(nodes[i]);
				String bindingKey = binding == null ? "null" : binding.getKey();

				// case of a capture binding
				if (bindingKey.indexOf('!') != -1 && binding.getKind() == IBinding.METHOD) {
					bindingKey = ((IMethodBinding) binding).getReturnType().getKey();
				}

				this.bindingKeys.add(bindingKey);
			}
		}
		@Override
		public void acceptBinding(String key, IBinding binding) {
			super.acceptBinding(key, binding);
			this.foundKeys.add(binding == null ? "null" : binding.getKey());
		}

		public String[] getBindingKeys() {
			int length = this.bindingKeys.size();
			String[] result = new String[length];
			this.bindingKeys.toArray(result);
			return result;
		}

		public String[] getFoundKeys() {
			int length = this.foundKeys.size();
			String[] result = new String[length];
			this.foundKeys.toArray(result);
			return result;
		}
	}

	public WorkingCopyOwner owner = new WorkingCopyOwner() {};

	public BatchASTCreationTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildModelTestSuite(BatchASTCreationTests.class);
	}

	// Use this static initializer to specify subset for tests
	// All specified tests which do not belong to the class are skipped...
	static {
//		TESTS_PREFIX =  "testBug86380";
//		TESTS_NAMES = new String[] { "test072a" };
//		TESTS_NUMBERS = new int[] { 72 };
//		TESTS_RANGE = new int[] { 83304, -1 };
	}

	@Override
	public void setUpSuite() throws Exception {
		super.setUpSuite();
		createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
	}

	@Override
	public void tearDownSuite() throws Exception {
		deleteProject("P");
		super.tearDownSuite();
	}

	/*
	 * Resolves the given cus and binding key as a batch.
	 * While resolving, for the ASTNode that is marked, ensures that its binding key is the expected one.
	 * Ensures that the returned binding corresponds to the expected key.
	 */
	private void assertRequestedBindingFound(String[] pathAndSources, final String expectedKey) throws JavaModelException {
		assertRequestedBindingsFound(pathAndSources, new String[] {expectedKey});
	}

	/*
	 * Resolves the given cus and binding key as a batch.
	 * While resolving, for the ASTNode that is marked, ensures that its binding key is the expected one.
	 * Ensures that the returned binding corresponds to the expected key.
	 */
	private void assertRequestedBindingsFound(String[] pathAndSources, final String[] expectedKeys) throws JavaModelException {
		BindingResolver resolver = requestBindings(pathAndSources, expectedKeys);

		assertStringsEqual("Unexpected binding for marked node", expectedKeys, resolver.getBindingKeys());

		assertStringsEqual("Unexpected binding found by acceptBinding", expectedKeys, resolver.getFoundKeys());
	}

	/*
	 * Creates working copies from the given path and sources.
	 * Resolves a dummy cu as a batch and on the first accept, create a binding with the expected key using ASTRequestor#createBindings.
	 * Ensures that the returned binding corresponds to the expected key.
	 */
	private void assertBindingCreated(String[] pathAndSources, final String expectedKey) throws JavaModelException {
		assertBindingsCreated(pathAndSources, new String[] {expectedKey});
	}
	/*
	 * Creates working copies from the given path and sources.
	 * Resolves a dummy cu as a batch and on the first accept, create a binding with the expected key using ASTRequestor#createBindings.
	 * Ensures that the returned binding corresponds to the expected key.
	 */
	private void assertBindingsCreated(String[] pathAndSources, final String[] expectedKeys) throws JavaModelException {
		ICompilationUnit[] copies = null;
		try {
			copies = createWorkingCopies(pathAndSources);
			class Requestor extends TestASTRequestor {
				ArrayList createdBindingKeys = new ArrayList();
				@Override
				public void acceptAST(ICompilationUnit source, CompilationUnit cu) {
					super.acceptAST(source, cu);
					IBinding[] bindings = createBindings(expectedKeys);
					if (bindings != null && bindings.length > 0 && bindings[0] != null)
						this.createdBindingKeys.add(bindings[0].getKey());
				}
				public String getCreatedKeys() {
					StringBuilder buffer = new StringBuilder();
					for (int i = 0, length = this.createdBindingKeys.size(); i < length; i++) {
						buffer.append(this.createdBindingKeys.get(i));
						if (i < length - 1)
							buffer.append('\n');
					}
					return buffer.toString();
				}
			}
			Requestor requestor = new Requestor();
			ICompilationUnit[] dummyWorkingCopies = null;
			try {
				dummyWorkingCopies = createWorkingCopies(new String[] {
					"/P/Test.java",
					"public class Test {\n" +
					"}"
				});
				resolveASTs(dummyWorkingCopies, new String[] {}, requestor, getJavaProject("P"), this.owner);
			} finally {
				discardWorkingCopies(dummyWorkingCopies);
			}

			String expectedKey = toString(expectedKeys);
			String actualKey = requestor.getCreatedKeys();
			if (!expectedKey.equals(actualKey)) {
				BindingResolver resolver = requestBindings(pathAndSources, null);
				String[] markedKeys = resolver.getBindingKeys();
				if (markedKeys.length > 0) {
					assertStringsEqual("Inconsistent expected key ", expectedKeys, markedKeys);
				}
			}
			assertEquals("Unexpected created binding", expectedKey, actualKey);
		} finally {
			discardWorkingCopies(copies);
		}
	}

	private void createASTs(ICompilationUnit[] cus, TestASTRequestor requestor) {
		ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		parser.createASTs(cus, new String[] {}, requestor, null);
	}

	protected ICompilationUnit[] createWorkingCopies(String[] pathAndSources) throws JavaModelException {
		return createWorkingCopies(pathAndSources, this.owner);
	}

	protected ICompilationUnit[] createWorkingCopies(String[] pathAndSources, boolean resolve) throws JavaModelException {
		IProblemRequestor problemRequestor = resolve
			? new IProblemRequestor() {
				public void acceptProblem(IProblem problem) {}
				public void beginReporting() {}
				public void endReporting() {}
				public boolean isActive() {
					return true;
				}
			}
			: null;
		MarkerInfo[] markerInfos = createMarkerInfos(pathAndSources);
		this.owner = newWorkingCopyOwner(problemRequestor);
		return createWorkingCopies(markerInfos, this.owner);
	}

	private void resolveASTs(ICompilationUnit[] cus, TestASTRequestor requestor) {
		resolveASTs(cus, new String[0], requestor, getJavaProject("P"), this.owner);
	}

	private BindingResolver requestBindings(String[] pathAndSources, final String[] expectedKeys) throws JavaModelException {
		ICompilationUnit[] copies = null;
		try {
			MarkerInfo[] markerInfos = createMarkerInfos(pathAndSources);
			copies = createWorkingCopies(markerInfos, this.owner);
			BindingResolver resolver = new BindingResolver(markerInfos);
			resolveASTs(copies, expectedKeys == null ? new String[0] : expectedKeys, resolver, copies.length > 0 ? copies[0].getJavaProject() : getJavaProject("P"), this.owner);
			return resolver;
		} finally {
			discardWorkingCopies(copies);
		}
	}


	/*
	 * Tests the batch creation of 2 ASTs without resolving.
	 */
	public void test001() throws CoreException {
		this.workingCopies = createWorkingCopies(new String[] {
			"/P/p1/X.java",
			"package p1;\n" +
			"public class X extends Y {\n" +
			"}",
			"/P/p1/Y.java",
			"package p1;\n" +
			"public class Y {\n" +
			"}",
		});
		TestASTRequestor requestor = new TestASTRequestor();
		createASTs(this.workingCopies, requestor);
		assertASTNodesEqual(
			"package p1;\n" +
			"public class X extends Y {\n" +
			"}\n" +
			"\n" +
			"package p1;\n" +
			"public class Y {\n" +
			"}\n" +
			"\n",
			requestor.asts
		);
	}

	/*
	 * Tests the batch creation of 2 ASTs with resolving.
	 */
	public void test002() throws CoreException {
		MarkerInfo[] markerInfos = createMarkerInfos(new String[] {
			"/P/p1/X.java",
			"package p1;\n" +
			"public class X extends /*start*/Y/*end*/ {\n" +
			"}",
			"/P/p1/Y.java",
			"package p1;\n" +
			"/*start*/public class Y {\n" +
			"}/*end*/",
		});
		this.workingCopies = createWorkingCopies(markerInfos, this.owner);
		TestASTRequestor requestor = new TestASTRequestor();
		resolveASTs(this.workingCopies, requestor);

		assertASTNodesEqual(
			"package p1;\n" +
			"public class X extends Y {\n" +
			"}\n" +
			"\n" +
			"package p1;\n" +
			"public class Y {\n" +
			"}\n" +
			"\n",
			requestor.asts
		);

		// compare the bindings coming from the 2 ASTs
		Type superX = (Type) findNode((CompilationUnit) requestor.asts.get(0), markerInfos[0]);
		TypeDeclaration typeY = (TypeDeclaration) findNode((CompilationUnit) requestor.asts.get(1), markerInfos[1]);
		IBinding superXBinding = superX.resolveBinding();
		IBinding typeYBinding = typeY.resolveBinding();
		assertTrue("Super of X and Y should be the same", superXBinding == typeYBinding);
	}

	/*
	 * Ensures that ASTs that are required by original source but were not asked for are not handled.
	 */
	public void test003() throws CoreException {
		ICompilationUnit[] otherWorkingCopies = null;
		try {
			this.workingCopies = createWorkingCopies(new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X extends Y {\n" +
				"}",
			});
			otherWorkingCopies = createWorkingCopies(new String[] {
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
			});
			TestASTRequestor requestor = new TestASTRequestor();
			resolveASTs(this.workingCopies, requestor);

			assertASTNodesEqual(
				"package p1;\n" +
				"public class X extends Y {\n" +
				"}\n" +
				"\n",
				requestor.asts
			);
		} finally {
			// Note: this.workingCopies are discarded in tearDown
			discardWorkingCopies(otherWorkingCopies);
		}
	}

	/*
	 * Ensures that a package binding can be retrieved using its key.
	 */
	public void test004() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"/*start*/package p1;/*end*/\n" +
				"public class X {\n" +
				"}",
			},
			"p1");
	}

	/*
	 * Ensures that a type binding can be retrieved using its key.
	 */
	public void test005() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"/*start*/public class X extends Y {\n" +
				"}/*end*/",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
			},
			"Lp1/X;");
	}

	/*
	 * Ensures that a type binding can be retrieved using its key.
	 */
	public void test006() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X extends Y {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"/*start*/public class Y {\n" +
				"}/*end*/",
			},
			"Lp1/Y;");
	}

	/*
	 * Ensures that a member type binding can be retrieved using its key.
	 */
	public void test007() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  /*start*/class Member {\n" +
				"  }/*end*/" +
				"}",
			},
			"Lp1/X$Member;");
	}

	/*
	 * Ensures that a member type binding can be retrieved using its key.
	 */
	public void test008() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  class Member1 {\n" +
				"    /*start*/class Member2 {\n" +
				"    }/*end*/" +
				"  }\n" +
				"}",
			},
			"Lp1/X$Member1$Member2;");
	}
	/*
	 * Ensures that an anonymous type binding can be retrieved using its key.
	 */
	public void test009() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    new X() /*start*/{\n" +
				"    }/*end*/;" +
				"  }\n" +
				"}",
			},
			"Lp1/X$52;");
	}
	/*
	 * Ensures that a local type binding can be retrieved using its key.
	 */
	public void test010() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    /*start*/class Y {\n" +
				"    }/*end*/;" +
				"  }\n" +
				"}",
			},
			"Lp1/X$54$Y;");
	}

	/*
	 * Ensures that a package binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test011() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}",
			},
			"p1");
	}

	/*
	 * Ensures that a type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test012() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X extends Y {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
			},
			"Lp1/X;");
	}

	/*
	 * Ensures that a type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test013() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X extends Y {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
			},
			"Lp1/Y;");
	}

	/*
	 * Ensures that a member type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test014() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  class Member {\n" +
				"  }" +
				"}",
			},
			"Lp1/X$Member;");
	}

	/*
	 * Ensures that a member type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test015() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  class Member1 {\n" +
				"    class Member2 {\n" +
				"    }" +
				"  }\n" +
				"}",
			},
			"Lp1/X$Member1$Member2;");
	}

	/*
	 * Ensures that an anonymous type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test016() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    new X() /*start*/{\n" +
				"    }/*end*/;" +
				"  }\n" +
				"}",
			},
			"Lp1/X$52;");
	}

	/*
	 * Ensures that a local type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test017() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    /*start*/class Y {\n" +
				"    }/*end*/;" +
				"  }\n" +
				"}",
			},
			"Lp1/X$54$Y;");
	}

	/*
	 * Ensures that a method binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test018() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X;.foo()V");
	}

	/*
	 * Ensures that a method binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test019() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo(Object o) {\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X;.foo(Ljava/lang/Object;)V");
	}

	/*
	 * Ensures that a method binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test020() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  X(Object o) {\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X;.(Ljava/lang/Object;)V");
	}

	/*
	 * Ensures that a field binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test021() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  int field;\n" +
				"}",
			},
			"Lp1/X;.field)I");
	}

	/*
	 * Ensures that a base type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test022() throws CoreException {
		assertBindingCreated(new String[0],"I");
	}

	/*
	 * Ensures that an array binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test023() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}",
			},
			"[Lp1/X;");
	}

	/*
	 * Ensures that an array binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test024() throws CoreException {
		assertBindingCreated(new String[0],"[[I");
	}

	/*
	 * Ensures that a method binding in an anonymous type with several kind of parameters can be created using its key in ASTRequestor#createBindings
	 */
	public void test025() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  class Y {\n" +
				"  }\n" +
				"  void foo() {\n" +
				"    new X() {\n" +
				"      /*start*/void bar(int i, X x, String[][] s, Y[] args, boolean b, Object o) {\n" +
				"      }/*end*/\n" +
				"    };\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X$68;.bar(ILp1/X;[[Ljava/lang/String;[Lp1/X$Y;ZLjava/lang/Object;)V"
		);
	}

	/*
	 * Ensures that a generic type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test026() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"}",
			},
			"Lp1/X<TT;>;");
	}

	/*
	 * Ensures that a generic type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test027() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T extends Y & I, U extends Y> {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y {\n" +
				"}",
				"/P/p1/I.java",
				"package p1;\n" +
				"public interface I {\n" +
				"}",
			},
			"Lp1/X<TT;TU;>;");
	}

	/*
	 * Ensures that a parameterized type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test028() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  X<String> field;\n" +
				"}",
			},
			"Lp1/X<Ljava/lang/String;>;");
	}

	/*
	 * Ensures that a member parameterized type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test029() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T,U> {\n" +
				"  class Y<V> {\n" +
				"    X<Error,Exception>.Y<String> field;\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X<Ljava/lang/Error;Ljava/lang/Exception;>.Y<Ljava/lang/String;>;");
	}

	/*
	 * Ensures that a raw type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test030() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T,U> {\n" +
				"   X field;\n" +
				"}",
			},
			"Lp1/X<>;");
	}

	/*
	 * Ensures that a member raw type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test031() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T,U> {\n" +
				"  class Y<V> {\n" +
				"    X<Error,Exception>.Y field;\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X<Ljava/lang/Error;Ljava/lang/Exception;>.Y<>;");
	}

	/*
	 * Ensures that a parameterized method binding can be created using its key in ASTRequestor#createBindings
	 */
	public void test032() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  <T> void foo() {\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X;.foo<T:Ljava/lang/Object;>()V");
	}

	/*
	 * Ensures that a local variable binding can be created using its key in ASTRequestor#createBindings
	 */
	public void test033() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    int i;\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X;.foo()V#i");
	}

	/*
	 * Ensures that a local variable binding can be created using its key in ASTRequestor#createBindings
	 */
	public void test034() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    int i = 1;\n" +
				"    if (i == 0) {\n" +
				"      int a;\n" +
				"    } else {\n" +
				"      int b;\n" +
				"    }\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X;.foo()V#1#b");
	}

	/*
	 * Ensures that a parameterized method binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test035() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  void foo(T t) {\n" +
				"  }\n" +
				"  void bar() {\n" +
				"    new X<String>().foo(\"\");\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X<Ljava/lang/String;>;.foo(Ljava/lang/String;)V");
	}

	/*
	 * Ensures that a parameterized generic method binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test036() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  <U> void foo(T t, U u) {\n" +
				"  }\n" +
				"  void bar() {\n" +
				"    /*start*/new X<String>().foo(\"\", this)/*end*/;\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X<Ljava/lang/String;>;.foo<U:Ljava/lang/Object;>(Ljava/lang/String;TU;)V%<Lp1/X;>"
		);
	}

	/*
	 * Ensures that a raw generic method binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test037() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  <U> void foo(T t, U u) {\n" +
				"  }\n" +
				"  void bar() {\n" +
				"    /*start*/new X().foo(\"\", this)/*end*/;\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X;.foo<U:Ljava/lang/Object;>(TT;TU;)V%<>");
	}

	/*
	 * Ensures that a parameterized method binding (where the parameter is an unbound wildcard) can be created using its key in ASTRequestor#createBindings.
	 */
	public void test038() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"  void bar(X<?> x) {\n" +
				"    x.foo();\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X<Lp1/X;{0}*>;.foo()V");
	}

	/*
	 * Ensures that a parameterized method binding (where the parameter is an extends wildcard) can be created using its key in ASTRequestor#createBindings.
	 */
	public void test039() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"  void bar(X<? extends Object> x) {\n" +
				"    x.foo();\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X<Lp1/X;{0}+Ljava/lang/Object;>;.foo()V");
	}

	/*
	 * Ensures that a parameterized method binding (where the parameter is a super wildcard) can be created using its key in ASTRequestor#createBindings.
	 */
	public void test040() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"  void bar(X<? super Error> x) {\n" +
				"    x.foo();\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X<Lp1/X;{0}-Ljava/lang/Error;>;.foo()V");
	}

	/*
	 * Ensures that a parameterized method binding (where the parameters contain wildcards) can be created using its key in ASTRequestor#createBindings.
	 */
	public void test041() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T, U, V, W> {\n" +
				"  void foo() {\n" +
				"  }\n" +
				"  void bar(X<? super Error, ?, String, ? extends Object> x) {\n" +
				"    x.foo();\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X<Lp1/X;{0}-Ljava/lang/Error;Lp1/X;{1}*Ljava/lang/String;Lp1/X;{2}+Ljava/lang/Object;>;.foo()V");
	}

	/*
	 * Ensures that requesting 2 bindings and an AST for the same compilation unit reports the 2 bindings.
	 */
	public void test042() throws CoreException {
		ICompilationUnit workingCopy = null;
		try {
			workingCopy = getWorkingCopy(
				"/P/X.java",
				"public class X {\n" +
				"  int field;\n" +
				"}"
			);
			BindingRequestor requestor = new BindingRequestor();
			String[] bindingKeys =
				new String[] {
					"LX;",
					"LX;.field)I"
				};
			resolveASTs(
				new ICompilationUnit[] {workingCopy},
				bindingKeys,
				requestor,
				getJavaProject("P"),
				workingCopy.getOwner()
			);
			assertBindingsEqual(
				"LX;\n" +
				"LX;.field)I",
				requestor.getBindings(bindingKeys));
		} finally {
			if (workingCopy != null)
				workingCopy.discardWorkingCopy();
		}
	}

	/*
	 * Ensures that a source parameterized type binding (where the parameters contain wildcard with a super bound) can be created using its key in ASTRequestor#createBindings.
	 */
	public void test043() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  X<? super T> field;\n" +
				"}",
			},
			"Lp1/X<Lp1/X;{0}-Lp1/X;:TT;>;");
	}

	/*
	 * Ensures that a binary parameterized type binding (where the parameters contain wildcard with a super bound) can be created using its key in ASTRequestor#createBindings.
	 * (regression test for 83499 ClassCastException when restoring ITypeBinding from key)
	 */
	public void test044() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<E> {\n" +
				"  Class<? extends E> field;\n" +
				"}",
			},
			"Ljava/lang/Class<Lp1/X;{0}+Lp1/X;:TE;>;");
	}

	/*
	 * Ensures that restoring a second key that references a type in a first key doesn't throw a NPE
	 * (regression test for bug 83499 NPE when restoring ITypeBinding from key)
	 */
	public void test045() throws CoreException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y<E> {\n" +
				"}"
			},
			new String[] {
				"Lp1/X;",
				"Lp1/Y<Lp1/Y;{0}+Lp1/X;>;"
			}
		);
		assertBindingsEqual(
			"Lp1/X;\n" +
			"Lp1/Y<Lp1/Y;{0}+Lp1/X;>;",
			bindings);
	}

	/*
	 * Ensures that a binary array parameterized type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test046() throws CoreException {
		assertBindingCreated(
			new String[] {},
			"[Ljava/lang/Class<Ljava/lang/Object;>;");
	}

	/*
	 * Ensures that the null type binding can be created using its key in batch creation.
	 */
	public void test047() throws CoreException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {"N"});
		assertBindingsEqual(
				"N",
				bindings);
	}

	/*
	 * Ensures that a binary array type binding can be created using its key in batch creation.
	 */
	public void test048() throws CoreException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {"[Ljava/lang/Object;"});
		assertBindingsEqual(
				"[Ljava/lang/Object;",
				bindings);
	}

	/*
	 * Ensures that a type variable binding can be created using its key in batch creation.
	 */
	public void test049() throws CoreException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {"Ljava/lang/Class<TT;>;:TT;"});
		assertBindingsEqual(
				"Ljava/lang/Class;:TT;",
				bindings);
	}

	/*
	 * Ensures that a parameterized type binding with a wildcard that extends an array the can be created using its key in batch creation.
	 */
	public void test050() throws CoreException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {"Ljava/lang/Class<Ljava/lang/Class<TT;>;{0}+[Ljava/lang/Object;>;"});
		assertBindingsEqual(
				"Ljava/lang/Class<Ljava/lang/Class;{0}+[Ljava/lang/Object;>;",
				bindings);
	}

	/*
	 * Ensures that attempting to create a top level type that doesn't exist using its key i in batch creation.
	 * returns null.
	 */
	public void test051() throws CoreException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {"Lp1/DoesNotExist;"});
		assertBindingsEqual(
				"<null>",
				bindings);
	}

	/*
	 * Ensures that a secondary type binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test052() throws CoreException {
		try {
			createFolder("/P/p1");
			createFile(
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}\n" +
				"class Y {\n" +
				"}"
			);
			assertBindingCreated(new String[] {}, "Lp1/X~Y;");
		} finally {
			deleteFolder("/P/p1");
		}
	}

	/*
	 * Ensures that an anonymous type binding coming from secondary type can be created using its key in ASTRequestor#createBindings.
	 */
	public void test053() throws CoreException {
		try {
			createFolder("/P/p1");
			createFile(
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"}\n" +
				"class Y {\n" +
				"  void foo() {\n" +
				"    new Y() {};\n" +
				"  }\n" +
				"}"
			);
			assertBindingCreated(new String[] {}, "Lp1/X~Y$64;");
		} finally {
			deleteFolder("/P/p1");
		}
	}

	/*
	 * Ensures that an anonymous type binding inside a local type can be retrieved using its key.
	 */
	public void test054() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    class Y {\n" +
				"      void bar() {\n" +
				"        new X() /*start*/{\n" +
				"        }/*end*/;" +
				"      }\n" +
				"    }\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X$89;"
		);
	}

	/*
	 * Ensures that a parameterized generic method binding can be created using its key in ASTRequestor#createBindings.
	 */
	public void test055() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"  <U> void foo(U u) {\n" +
				"  }\n" +
				"  void bar() {\n" +
				"    /*start*/new X<String>().foo(new X() {})/*end*/;\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X<Ljava/lang/String;>;.foo<U:Ljava/lang/Object;>(TU;)V%<Lp1/X$101;>"
		);
	}

	/*
	 * Ensures that creating a binary member type binding returns the correct binding
	 * (regression test for bug 86967 [1.5][dom] NPE in BindingKeyResolver for multi-level parameterized type binding)
	 */
	public void test056() throws CoreException, IOException {
		try {
			IJavaProject project = createJavaProject("BinaryProject", new String[0], new String[] {"JCL15_LIB"}, "", "1.5");
			addLibrary(project, "lib.jar", "src.zip", new String[] {
				"/BinaryProject/p/X.java",
				"package p;\n" +
				"public class X<K, V> {\n" +
				"  public class Y<K1, V1> {\n" +
				"  }\n" +
				"}"
			}, "1.5");
			ITypeBinding[] bindings = createTypeBindings(new String[0], new String[] {
				"Lp/X<>.Y<Lp/X;:TK;Lp/X;:TV;>;"
			}, project);
			assertBindingsEqual(
				"Lp/X<>.Y<Lp/X;:TK;Lp/X;:TV;>;",
				bindings);
		} finally {
			deleteProject("BinaryProject");
		}
	}

	/*
	 * Ensures that creating a missing binary member type binding doesn't throw a NPE
	 * (regression test for bug 86967 [1.5][dom] NPE in BindingKeyResolver for multi-level parameterized type binding)
	 */
	public void test057() throws CoreException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {},
			new String[] {"Lp/Missing$Member;"});
		assertBindingsEqual(
			"<null>",
			bindings);
	}

	/*
	 * Ensures that a type parameter binding can be created using its key in batch creation.
	 * (regression test for bug 87050 ASTParser#createASTs(..) cannot resolve method type parameter binding from key)
	 */
	public void test058() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  </*start*/T/*end*/> void foo(T t) {\n" +
				"  }" +
				"}",
			},
			"Lp1/X;.foo<T:Ljava/lang/Object;>(TT;)V:TT;");
	}

	/*
	 * Ensures that a capture binding can be created using its key in batch creation.
	 */
	public void test059() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<T> {\n" +
				"    Object foo(X<?> list) {\n" +
				"       return /*start*/list.get()/*end*/;\n" +
				"    }\n" +
				"    T get() {\n" +
				"    	return null;\n" +
				"    }\n" +
				"}",
			},
			"Lp1/X;&!Lp1/X;{0}*77;"
		);
	}

	/*
	 * Ensures that a capture binding can be created using its key in batch creation.
	 */
	public void test060() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/xy/Cap.java",
				"package xy;\n" +
				"import java.util.Vector;\n" +
				"public class Cap {\n" +
				"	{\n" +
				"		Vector<?> v= null;\n" +
				"		/*start*/v.get(0)/*end*/;\n" +
				"	}\n" +
				"}",
				"/P/java/util/Vector.java",
				"package java.util;\n" +
				"public class Vector<T> {\n" +
				"  public T get(int i) {\n" +
				"    return null;\n" +
				"  }\n" +
				"}"
			},
			"Lxy/Cap;&!Ljava/util/Vector;{0}*82;"
		);
	}

	/*
	 * Ensures that a generic constructor binding can be created using its key in batch creation.
	 */
	public void test061() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"    /*start*/<T> X() {\n" +
				"    }/*end*/\n" +
				"}",
			},
			"Lp1/X;.<T:Ljava/lang/Object;>()V"
		);
	}

	/*
	 * Ensures that an array binding whose leaf type is a type variable binding can be created using its key in batch creation.
	 * (regression test for bug 94206 CCE in BindingKeyResolver when restoring array type of method type parameter)
	 */
	public void test062() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  <T> /*start*/T[]/*end*/ foo(T[] a) {\n" +
				"    return null;\n" +
				"  }\n" +
				"}",
			},
			"[Lp1/X;.foo<T:Ljava/lang/Object;>([TT;)[TT;:TT;"
		);
	}

	/*
	 * Ensures that a raw method binding can be created using its key in batch creation.
	 * (regression test for bug 87749 different IMethodBindings of generic method have equal getKey())
	 */
	public void test063() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"	public static <T extends Y<? super T>> void foo(Z<T> z) {\n" +
				"    }\n" +
				"    /**\n" +
				"     * @see #foo(Z)\n" +
				"     */\n" +
				"    void bar() {\n" +
				"        /*start*/foo(new W())/*end*/;\n" +
				"    }\n" +
				"}\n" +
				"class Y<T> {\n" +
				"}\n" +
				"class Z<T> {\n" +
				"}\n" +
				"class W<T> extends Z<T> {\n" +
				"}",
			},
			"Lp1/X;.foo<T:Lp1/Y<-TT;>;>(Lp1/Z<TT;>;)V%<Lp1/X~Y<Lp1/X~Y;{0}-Lp1/X~Y<Lp1/X~Y;{0}-Lp1/X;:2TT;>;>;>"
		);
	}

	/*
	 * Ensures that a raw method binding can be created using its key in batch creation.
	 * (regression test for bug 87749 different IMethodBindings of generic method have equal getKey())
	 */
	public void test063a() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<U extends X<T>> {\n" +
				"	public void foo(Z<U> z) {\n" +
				"    }\n" +
				"    /**\n" +
				"     * @see #foo(Z)\n" +
				"     */\n" +
				"    static void bar(X x) {\n" +
				"        /*start*/x.foo(new W())/*end*/;\n" +
				"    }\n" +
				"}\n" +
				"class Y<T> {\n" +
				"}\n" +
				"class Z<T> {\n" +
				"}\n" +
				"class W<T> extends Z<T> {\n" +
				"}",
			},
			"Lp1/X<>;.foo(Lp1/Z;)V"
		);
	}

	/*
	 * Ensures that a parameterized type binding with a capture binding in its arguments can be created using its key in batch creation.
	 * (regression test for bug 94092 ASTParser#createASTs(..) restores wrong bindings from capture keys)
	 */
	public void test064() throws CoreException {
		assertRequestedBindingsFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"/*start1*/public class X {\n" +
				"	Object o= null;\n" +
				"	Y<?> field;\n" +
				"	void foo() {\n" +
				"		/*start2*/o = field/*end2*/;\n" +
				"	}\n" +
				"}/*end1*/\n",
				"/P/p1/Y.java",
				"package p1;\n" +
				"public class Y<T> {\n" +
				"}",
			},
			new String[] {
				"Lp1/X;",
				"Lp1/X;&Lp1/Y<!Lp1/Y;{0}*83;>;",
			}
		);
	}

	/*
	 * Ensures that a parameterized type binding with a type variable of the current's method in its arguments can be created using its key in batch creation.
	 * (regression test for bug 97902 NPE on Open Declaration on reference to generic type)
	 */
	public void test065() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  <T> void foo(/*start*/Y<T>/*end*/ y) {\n" +
				"  }\n" +
				"}\n" +
				"class Y<E> {\n" +
				"}",
			},
			"Lp1/X~Y<Lp1/X;:1TT;>;"
		);
	}

	/*
	 * Ensures that the compilation with a owner is used instead of a primary working copy when looking up a type.
	 * (regression test for bug 97542 ASTParser#createASTs does not correctly resolve bindings in working copies)
	 */
	public void test066() throws CoreException {
		ICompilationUnit primaryWorkingCopy = null;
		ICompilationUnit ownedWorkingcopy = null;
		try {
			// primary working copy with no method foo()
			primaryWorkingCopy = getCompilationUnit("/P/p1/X.java");
			primaryWorkingCopy.becomeWorkingCopy(null/*no progress*/);
			primaryWorkingCopy.getBuffer().setContents(
				"package p1;\n" +
				"public class X {\n" +
				"}"
			);
			primaryWorkingCopy.reconcile(ICompilationUnit.NO_AST, false, null, null);

			// working copy for the test's owner with a method foo()
			ownedWorkingcopy = getWorkingCopy(
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {}\n" +
				"}",
				this.owner
			);

			// create bindings
			assertRequestedBindingFound(
				new String[] {
					"/P/p1/Y.java",
					"package p1;\n" +
					"public class Y {\n" +
					"  void bar() {\n" +
					"    /*start*/new X().foo()/*end*/;\n" +
					"}",
				},
				"Lp1/X;.foo()V"
			);
		} finally {
			if (primaryWorkingCopy != null)
				primaryWorkingCopy.discardWorkingCopy();
			if (ownedWorkingcopy != null)
				ownedWorkingcopy.discardWorkingCopy();
		}
	}

	/*
	 * Ensures that the declaring class of a member parameterized type binding with a raw enclosing type is correct
	 */
	public void test067() throws CoreException {
		ITypeBinding[] bindings = createTypeBindings(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X<K, V> {\n" +
				"  public class Y<K1, V1> {\n" +
				"  }\n" +
				"  /*start*/Y<K, V>/*end*/ field;\n" +
				"}"
			},
			new String[] {"Lp1/X$Y<Lp1/X;:TK;Lp1/X;:TV;>;"}
		);
		assertBindingEquals(
			"Lp1/X<>;",
			bindings.length == 0 ? null : bindings[0].getDeclaringClass()
		);
	}

	/*
	 * Ensures that a raw member type can be created using its key in batch creation.
	 */
	public void test068() throws CoreException, IOException {
		try {
			IJavaProject project = createJavaProject("P1", new String[] {""}, new String[] {"JCL15_LIB"}, "", "1.5");
			addLibrary(project, "lib.jar", "src.zip", new String[] {
				"/P1/p/X.java",
				"package p;\n" +
				"public class X<K, V> {\n" +
				"  public static class Member<K1, V1> {\n" +
				"  }\n" +
				"}",
				"/P1/p/Y.java",
				"package p;\n" +
				"public class Y {\n" +
				"  void foo(X.Member x) {\n" +
				"  }\n" +
				"}",
			}, "1.5");
			assertRequestedBindingFound(
				new String[] {
					"/P1/p1/Z.java",
					"package p1;\n" +
					"public class Z extends p.Y {\n" +
					"  /*start*/p.X.Member/*end*/ field;\n" +
					"}"
				},
				"Lp/X$Member<>;"
			);
		} finally {
			deleteProject("P1");
		}
	}

	/*
	 * Ensures that requesting a CU needing a constant in a previously processed CU doesn't throw an NPE
	 * (regression test for bug 111822 DOMParser.createASTs() NPE at FieldReference.getConstantFor(FieldReference.java:408))
	 */
	public void test069() throws CoreException {
		this.workingCopies = createWorkingCopies(new String[] {
			"/P/pkg/RefAnnoAndClassWithAnno.java",
			"package pkg;\n" +
			"public class RefMyAnnoAndClassWithAnno {\n" +
			"	final Class anno = MyAnno.class;\n" +
			"	final Class withAnno = ClassWithAnnotation.class;\n" +
			"}",
			"/P/pkg/MyAnno.java",
			"package pkg;\n" +
			"public @interface MyAnno {\n" +
			"	public enum EnumColor{\n" +
			"		BLUE, RED, WHITE;\n" +
			"	}\n" +
			"	EnumColor aEnum();\n" +
			"}",
			"/P/pkg/ClassWithAnnotation.java",
			"package pkg;\n" +
			"import pkg.MyAnno.EnumColor;\n" +
			"@MyAnno(aEnum = EnumColor.BLUE)\n" +
			"public class ClassWithAnnotation {}"
		});
		String key = BindingKey.createTypeBindingKey("pkg.RefMyAnnoAndClassWithAnno");
		BindingResolver resolver = new BindingResolver(new MarkerInfo[0]);
		resolveASTs(new ICompilationUnit[0],  new String[] {key}, resolver, getJavaProject("P"), this.owner);
		assertStringsEqual(
			"Unexpected bindings",
			"Lpkg/RefAnnoAndClassWithAnno~RefMyAnnoAndClassWithAnno;\n",
			resolver.getFoundKeys());
	}

	/*
	 * Ensures that unrequested compilation units are not resolved
	 * (regression test for bug 114935 ASTParser.createASTs parses more CUs then required)
	 */
	public void test070() throws CoreException {
		MarkerInfo[] markerInfos = createMarkerInfos(new String[] {
			"/P/p1/X.java",
			"package p1;\n" +
			"public class X extends /*start*/Y/*end*/ {\n" +
			"}",
			"/P/p1/Y.java",
			"package p1;\n" +
			"public class Y {\n" +
			"  static final int CONST = 2 + 3;\n" +
			"}",
		});
		this.workingCopies = createWorkingCopies(markerInfos, this.owner);
		TestASTRequestor requestor = new TestASTRequestor();
		resolveASTs(new ICompilationUnit[] {this.workingCopies[0]}, requestor);

		// get the binding for Y
		Type y = (Type) findNode((CompilationUnit) requestor.asts.get(0), markerInfos[0]);
		ITypeBinding yBinding = y.resolveBinding();

		// ensure that the fields for Y are not resolved
		assertBindingsEqual("", yBinding.getDeclaredFields());
	}

	/*
	 * Ensures that unrequested compilation units are not resolved
	 * (regression test for bug 117018 IVariableBinding#getConstantValue() could be lazy resolved)
	 */
	public void test071() throws CoreException {
		final MarkerInfo[] markerInfos = createMarkerInfos(new String[] {
			"/P/p1/X.java",
			"package p1;\n" +
			"public class X extends /*start*/Y/*end*/ {\n" +
			"}",
			"/P/p1/Y.java",
			"package p1;\n" +
			"public class Y {\n" +
			"  static final int CONST = 2 + 3;\n" +
			"}",
		});
		this.workingCopies = createWorkingCopies(markerInfos, this.owner);
		class Requestor extends TestASTRequestor {
			Object constantValue = null;
			@Override
			public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
				super.acceptAST(source, ast);
				Type y = (Type) findNode(ast, markerInfos[0]);
				ITypeBinding typeBinding = y.resolveBinding();
				IVariableBinding fieldBinding = typeBinding.getDeclaredFields()[0];
				this.constantValue = fieldBinding.getConstantValue();
			}
		}
		Requestor requestor = new Requestor();
		resolveASTs(new ICompilationUnit[] {this.workingCopies[0]}, requestor);

		assertEquals("Unexpected constant value", 5, requestor.constantValue);
	}

	/*
	 * Ensures that the declaring method of a local variable binding retrieved using its key
	 * is not null
	 * (regression test for bug 129804 Local variable bindings from ASTParser#createASTs(.., String[], .., ..) have no declaring method)
	 */
	public void test072() throws CoreException {
		IVariableBinding[] bindings = createVariableBindings(
			new String[] {
				"/P/X.java",
				"public class X {\n" +
				"    void m() {\n" +
				"        Object o;\n" +
				"    }\n" +
				"}"
			},
			new String[] {
				"LX;.m()V#o"
			}
		);
		assertBindingEquals(
			"LX;.m()V",
			bindings[0].getDeclaringMethod());
	}

// https://bugs.eclipse.org/bugs/show_bug.cgi?id=159631
public void test073() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P072", new String[] {}, Util.getJavaClassLibs(), "", "1.5");
		ICompilationUnit compilationUnits[] = new ICompilationUnit[3];
		compilationUnits[0] = getWorkingCopy(
			"P072/X.java",
			"public class X {\n" +
			"  @Override" +
			"  public boolean equals(Object o) {\n" +
			"    return true;\n" +
			"  }\n" +
			"}");
		compilationUnits[1] = getWorkingCopy(
			"P072/Y.java",
			"public class Y extends X {\n" +
			"}");
		compilationUnits[2] = getWorkingCopy(
			"P072/Z.java",
			"public class Z {\n" +
			"  Y m;\n" +
			"  boolean foo(Object p) {\n" +
			"    return this.m.equals(p);\n" +
			"  }\n" +
			"}");
		ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		parser.setResolveBindings(true);
		parser.setProject(project);
		class Requestor extends ASTRequestor {
		}
		parser.createASTs(compilationUnits, new String[0], new Requestor(), null);
		// will throw an unexpected NPE, until the bug is fixed
	} finally {
		deleteProject("P072");
	}
}

/**
 * @bug 155003: [model] Missing exception types / wrong signature?
 * @test Ensure that thrown exceptions are added in method unique key (not in signature)
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=155003"
 */
public void test074_Bug155003() throws CoreException {
	assertBindingCreated(
		new String[] {
			"/P/X.java",
			"public class X {\n" +
			"    public void foo() throws InterruptedException, IllegalMonitorStateException {\n" +
			"    }\n" +
			"    void test() throws InterruptedException, IllegalMonitorStateException {\n" +
			"    	/*start*/foo()/*end*/;\n" +
			"    }\n" +
			"}",
		},
		"LX;.foo()V|Ljava/lang/InterruptedException;|Ljava/lang/IllegalMonitorStateException;"
	);
	String content = "public class X {\n" +
			"    public void foo() throws InterruptedException, IllegalMonitorStateException {\n" +
			"    }\n" +
			"    void test() throws InterruptedException, IllegalMonitorStateException {\n" +
			"    	/*start*/foo()/*end*/;\n" +
			"    }\n" +
			"}";
	this.workingCopies = createWorkingCopies(new String[] { "/P/X.java", content }, true /*resolve*/);
	ASTNode node = buildAST(content, this.workingCopies[0]);
	assertEquals("Invalid node type!", ASTNode.METHOD_INVOCATION, node.getNodeType());
	IBinding binding = resolveBinding(node);
	BindingKey bindingKey = new BindingKey(binding.getKey());
	assertStringsEqual("Unexpected thrown exceptions",
		"Ljava.lang.InterruptedException;\n" +
		"Ljava.lang.IllegalMonitorStateException;\n",
		bindingKey.getThrownExceptions()
	);
}
public void test075_Bug155003() throws CoreException {
	String content = "public class X<T> {\n" +
		"	<U extends Exception> X<T> foo(X<T> x) throws RuntimeException, U {\n" +
		"		return null;\n" +
		"	}\n" +
		"	void test() throws Exception {\n" +
		"		/*start*/foo(this)/*end*/;\n" +
		"	}\n" +
		"}";
	this.workingCopies = createWorkingCopies(new String[] { "/P/X.java", content }, true /*resolve*/);
	ASTNode node = buildAST(content, this.workingCopies[0]);
	assertEquals("Invalid node type!", ASTNode.METHOD_INVOCATION, node.getNodeType());
	IBinding binding = resolveBinding(node);
	BindingKey bindingKey = new BindingKey(binding.getKey());
	assertStringsEqual("Unexpected thrown exceptions",
		"Ljava.lang.RuntimeException;\n" +
		"TU;\n",
		bindingKey.getThrownExceptions()
	);
}
public void test076_Bug155003() throws CoreException {
	String content = "public class X<T> {\n" +
		"	<K, V> V bar(K key, V value) throws Exception {\n" +
		"		return value;\n" +
		"	}\n" +
		"	void test() throws Exception {\n" +
		"		/*start*/bar(\"\", \"\")/*end*/;\n" +
		"	}\n" +
		"}";
	this.workingCopies = createWorkingCopies(new String[] { "/P/X.java", content }, true /*resolve*/);
	ASTNode node = buildAST(content, this.workingCopies[0]);
	assertEquals("Invalid node type!", ASTNode.METHOD_INVOCATION, node.getNodeType());
	IBinding binding = resolveBinding(node);
	BindingKey bindingKey = new BindingKey(binding.getKey());
	assertStringsEqual("Unexpected thrown exceptions",
		"Ljava.lang.Exception;\n",
		bindingKey.getThrownExceptions()
	);
}

/**
 * @bug 163647: [model] Thrown exceptions are not found in method binding key which have a capture as declaring class
 * @test Ensure that thrown exceptions are added in method unique key (not in signature)
 * 			even when declaring class is a capture
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=163647"
 */
public void test077_Bug163647() throws CoreException {
	String content = 	"public class Test {\n" +
		"    public X<? extends Object> getX() { return null; }\n" +
		"    public void bar() {\n" +
		"		try {\n" +
		"			/*start*/getX().foo()/*end*/;\n" +
		"		} catch (Exception e) {\n" +
		"			// skip\n" +
		"		}\n" +
		"    }\n" +
		"}\n" +
		"class X<T> {\n" +
		"    public void foo() throws CloneNotSupportedException, IllegalMonitorStateException, InterruptedException {\n" +
		"    }\n" +
		"}";
	this.workingCopies = createWorkingCopies(new String[] { "/P/Test.java", content }, true /*resolve*/);
	ASTNode node = buildAST(content, this.workingCopies[0]);
	assertEquals("Invalid node type!", ASTNode.METHOD_INVOCATION, node.getNodeType());
	IBinding binding = resolveBinding(node);
	BindingKey bindingKey = new BindingKey(binding.getKey());
	assertStringsEqual("Unexpected thrown exceptions",
		"Ljava.lang.CloneNotSupportedException;\n" +
		"Ljava.lang.IllegalMonitorStateException;\n" +
		"Ljava.lang.InterruptedException;\n",
		bindingKey.getThrownExceptions()
	);
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152060
public void test078() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P078", new String[] {}, Util.getJavaClassLibs(), "", "1.5");
		ICompilationUnit compilationUnits[] = new ICompilationUnit[1];
		compilationUnits[0] = getWorkingCopy(
			"P078/Test.java",
			"import java.util.*;\n" +
			"public class Test {\n" +
			"        public interface ExtraIterator<T> extends Iterator {\n" +
			"                public void extra();\n" +
			"        }\n" +
			"        public class Test2<T> implements ExtraIterator<T> {\n" +
			"            public boolean hasNext() {\n" +
			"                return false;\n" +
			"            }\n" +
			"            public T next() {\n" +
			"                return null;\n" +
			"            }\n" +
			"            public void remove() {\n" +
			"            }\n" +
			"            public void extra() {\n" +
			"            }\n" +
			"        }\n" +
			"}");
		ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		parser.setResolveBindings(true);
		parser.setProject(project);
       	final IBinding[] bindings = new IBinding[1];
		final String key = "LTest$ExtraIterator<>;";
		parser.createASTs(
			compilationUnits,
			new String[] {
				key
			},
			new ASTRequestor() {
                public void acceptAST(ICompilationUnit source, CompilationUnit localAst) {
                	// do nothing
                }
                public void acceptBinding(String bindingKey, IBinding binding) {
                	if (key.equals(bindingKey)) {
                		bindings[0] = binding;
                 	}
                }
			},
			null);
		IBinding binding = bindings[0];
		assertNotNull("Should not be null", binding);
		assertEquals("Not a type binding", IBinding.TYPE, binding.getKind());
		ITypeBinding typeBinding = (ITypeBinding) binding;
		assertEquals("Wrong name", "Test.ExtraIterator", typeBinding.getQualifiedName());
		assertTrue("Not a raw type", typeBinding.isRawType());
	} finally {
		deleteProject("P078");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152060
public void test079() throws CoreException, IOException {
	try {
		IJavaProject project = createJavaProject("P079", new String[] {"src"}, Util.getJavaClassLibs(), "bin", "1.5");
		createFolder("/P079/src/test");
		createFile("/P079/src/test/Test.java",
				"package test;\n" +
				"import java.util.*;\n" +
				"interface ExtraIterator<T> extends Iterator {\n" +
				"        public void extra();\n" +
				"}\n" +
				"public class Test<T> implements ExtraIterator<T> {\n" +
				"    public boolean hasNext() {\n" +
				"        return false;\n" +
				"    }\n" +
				"    public T next() {\n" +
				"        return null;\n" +
				"    }\n" +
				"    public void remove() {\n" +
				"    }\n" +
				"    public void extra() {\n" +
				"    }\n" +
				"}");
		ICompilationUnit compilationUnits[] = new ICompilationUnit[1];
		compilationUnits[0] = getCompilationUnit("P079", "src", "test", "Test.java");
		ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		parser.setResolveBindings(true);
		parser.setProject(project);
		final IBinding[] bindings = new IBinding[1];
		final String key = "Ltest/Test~ExtraIterator<>;";
		parser.createASTs(
				compilationUnits,
				new String[] {
						key
				},
				new ASTRequestor() {
					public void acceptAST(ICompilationUnit source, CompilationUnit localAst) {
						// do nothing
					}
					public void acceptBinding(String bindingKey, IBinding binding) {
						if (key.equals(bindingKey)) {
							bindings[0] = binding;
						}
					}
				},
				null);
		IBinding binding = bindings[0];
		assertNotNull("Should not be null", binding);
		assertEquals("Not a type binding", IBinding.TYPE, binding.getKind());
		ITypeBinding typeBinding = (ITypeBinding) binding;
		assertEquals("Wrong type", "test.ExtraIterator", typeBinding.getQualifiedName());
	} finally {
		deleteProject("P079");
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=152060
public void test080() throws CoreException, IOException {
	final String projectName = "P080";
	try {
		IJavaProject project = createJavaProject(projectName, new String[] {"src"}, Util.getJavaClassLibs(), "bin", "1.5");
		createFolder("/" + projectName + "/src/test");
		createFile("/" + projectName + "/src/test/Test.java",
				"package test;\n" +
				"import java.util.*;\n" +
				"public class Test {\n" +
				"        public interface ExtraIterator<T> extends Iterator {\n" +
				"                public void extra();\n" +
				"        }\n" +
				"        public class Test2<T> implements ExtraIterator<T> {\n" +
				"            public boolean hasNext() {\n" +
				"                return false;\n" +
				"            }\n" +
				"            public T next() {\n" +
				"                return null;\n" +
				"            }\n" +
				"            public void remove() {\n" +
				"            }\n" +
				"            public void extra() {\n" +
				"            }\n" +
				"        }\n" +
				"}");
		ICompilationUnit compilationUnits[] = new ICompilationUnit[1];
		compilationUnits[0] = getCompilationUnit(projectName, "src", "test", "Test.java");
		ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		parser.setResolveBindings(true);
		parser.setProject(project);
       	final IBinding[] bindings = new IBinding[1];
		final String key = "Ltest/Test$ExtraIterator<>;";
		parser.createASTs(
			compilationUnits,
			new String[] {
				key
			},
			new ASTRequestor() {
                public void acceptAST(ICompilationUnit source, CompilationUnit localAst) {
                	// do nothing
                }
                public void acceptBinding(String bindingKey, IBinding binding) {
                	if (key.equals(bindingKey)) {
                		bindings[0] = binding;
                 	}
                }
			},
			null);
		IBinding binding = bindings[0];
		assertNotNull("Should not be null", binding);
		assertEquals("Not a type binding", IBinding.TYPE, binding.getKind());
		ITypeBinding typeBinding = (ITypeBinding) binding;
		assertEquals("Wrong name", "test.Test.ExtraIterator", typeBinding.getQualifiedName());
		assertTrue("Not a raw type", typeBinding.isRawType());
	} finally {
		deleteProject(projectName);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=111529
public void test081() throws CoreException, IOException {
	final String projectName = "P081";
	try {
		IJavaProject javaProject = createJavaProject(projectName, new String[] {"src"}, Util.getJavaClassLibs(), "bin", "1.5");
		String typeName = "java.util.List<java.lang.Integer>";
		class BindingRequestor extends ASTRequestor {
			ITypeBinding _result = null;
			public void acceptBinding(String bindingKey, IBinding binding) {
				if (this._result == null && binding != null && binding.getKind() == IBinding.TYPE)
					this._result = (ITypeBinding) binding;
			}
		}
		String[] keys = new String[] {
			BindingKey.createTypeBindingKey(typeName)
		};
		final BindingRequestor requestor = new BindingRequestor();
		final ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		parser.setResolveBindings(true);
		parser.setProject(javaProject);
		// this doesn't really do a parse; it's a type lookup
		parser.createASTs(new ICompilationUnit[] {}, keys, requestor, null);
		ITypeBinding typeBinding = requestor._result;
		assertNotNull("No binding", typeBinding);
		assertTrue("Not a parameterized type binding", typeBinding.isParameterizedType());
	} finally {
		deleteProject(projectName);
	}
}
//https://bugs.eclipse.org/bugs/show_bug.cgi?id=111529
public void test082() throws CoreException, IOException {
	final String projectName = "P082";
	try {
		IJavaProject javaProject = createJavaProject(projectName, new String[] {"src"}, Util.getJavaClassLibs(), "bin", "1.5");
		String typeName = "java.util.List<Integer>";
		class BindingRequestor extends ASTRequestor {
			ITypeBinding _result = null;
			public void acceptBinding(String bindingKey, IBinding binding) {
				if (this._result == null && binding != null && binding.getKind() == IBinding.TYPE)
					this._result = (ITypeBinding) binding;
			}
		}
		String[] keys = new String[] {
			BindingKey.createTypeBindingKey(typeName)
		};
		final BindingRequestor requestor = new BindingRequestor();
		final ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
		parser.setResolveBindings(true);
		parser.setProject(javaProject);
		// this doesn't really do a parse; it's a type lookup
		parser.createASTs(new ICompilationUnit[] {}, keys, requestor, null);
		ITypeBinding typeBinding = requestor._result;
		assertNull("Got a binding", typeBinding);
	} finally {
		deleteProject(projectName);
	}
}

	/*
	 * Ensures that a secondary type binding can be retrieved using its key, even if the primary type doesn't exist.
	 * (regression test for bug 177115 NullPointerException in BindingKeyResolver.consumeTypeVariable(...))
	 */
	public void test083() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"/*start*/class Y {\n" +
				"}/*end*/",
			},
			"Lp1/X~Y;");
	}

	/*
	 * Ensures that a duplicate local variable binding can be created using its key in ASTRequestor#createBindings
	 * (regression test for bug 149590 [model] bindings for duplicate local variables share same key)
	 */
	public void test084() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  void foo() {\n" +
				"    int i;\n" +
				"    int i;\n" +
				"  }\n" +
				"}",
			},
			"Lp1/X;.foo()V#i#1");
	}

	/*
	 * Ensures that an annotation binding can be retrieved using its key.
	 */
	public void test085() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"/*start*/@MyAnnot/*end*/\n" +
				"public class X {\n" +
				"}",
				"/P/p1/MyAnnot.java",
				"package p1;\n" +
				"public @interface MyAnnot {\n" +
				"}",
			},
			"Lp1/X;@Lp1/MyAnnot;");
	}

	/*
	 * Ensures that an annotation binding can be retrieved using its key.
	 */
	public void test086() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  /*start*/@MyAnnot/*end*/\n" +
				"  int field;\n" +
				"}",
				"/P/p1/MyAnnot.java",
				"package p1;\n" +
				"public @interface MyAnnot {\n" +
				"}",
			},
			"Lp1/X;.field)I@Lp1/MyAnnot;");
	}

	/*
	 * Ensures that an annotation binding can be retrieved using its key.
	 */
	public void test087() throws CoreException {
		assertRequestedBindingFound(
			new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  /*start*/@MyAnnot/*end*/\n" +
				"  void foo() {\n" +
				"  }\n" +
				"}",
				"/P/p1/MyAnnot.java",
				"package p1;\n" +
				"public @interface MyAnnot {\n" +
				"}",
			},
			"Lp1/X;.foo()V@Lp1/MyAnnot;");
	}

	/*
	 * Ensures that a parameterized type with 2 arguments referring to the same cu that contains an anonymous type with a non-default constructor
	 * can be created using its key in ASTRequestor#createBindings
	 * (regression test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=236445 )
	 */
	public void test088() throws CoreException {
		assertBindingCreated(
			new String[] {
				"/P/A.java",
				"public class A {\n" +
				"	public void foo(C c) {\n" +
				"		c.bar(B //|<---Ctrl+Space after B\n" +
				"	}\n" +
				"}\n" +
				"class B<V, E> {\n" +
				"}",
				"/P/C.java",
				"public class C {\n" +
				"	public <V, E> void bar(B<V, E> code) {\n" +
				"		new D(null) {};  \n" +
				"	}\n" +
				"}\n" +
				"class D {\n" +
				"	D(Object o) {}\n" +
				"}"
			},
			"LA~B<LC;:1TV;LC;:1TE;>;");
	}

	public void testIgnoreMethodBodies1() throws CoreException {
		this.workingCopies = createWorkingCopies(new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  public int foo() {\n" +
				"    int i = 0;\n" +
				"  }\n" +
				"  public int bar() {\n" +
				"    int i = 0;\n" +
				"    new X() /*start*/{\n" +
				"    }/*end*/;" +
				"  }\n" +
				"}",
			});
			TestASTRequestor requestor = new TestASTRequestor();
			ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
			parser.setIgnoreMethodBodies(true);
			parser.createASTs(this.workingCopies, new String[] {}, requestor, null);
			// statement declaring i should not be in the AST
			assertASTNodesEqual(
					"package p1;\n" +
					"public class X {\n" +
					"  public int foo(){\n" +
					"  }\n" +
					"  public int bar(){\n" +
					"  }\n" +
					"}\n" +
					"\n",
					requestor.asts
				);
	}
	public void testIgnoreMethodBodies2() throws CoreException {
		this.workingCopies = createWorkingCopies(new String[] {
				"/P/p1/X.java",
				"package p1;\n" +
				"public class X {\n" +
				"  public int foo() {\n" +
				"    int i = 0;\n" +
				"  }\n" +
				"  public int bar() {\n" +
				"    int i = 0;\n" +
				"    new X() /*start*/{\n" +
				"    }/*end*/;" +
				"  }\n" +
				"}",
			});
			TestASTRequestor requestor = new TestASTRequestor();
			ASTParser parser = ASTParser.newParser(JLS3_INTERNAL);
			parser.setIgnoreMethodBodies(true);
			parser.setResolveBindings(true);
			parser.setProject(getJavaProject("P"));
			parser.createASTs(this.workingCopies, new String[] {}, requestor, null);
			// statement declaring i should not be in the AST
			assertASTNodesEqual(
					"package p1;\n" +
					"public class X {\n" +
					"  public int foo(){\n" +
					"  }\n" +
					"  public int bar(){\n" +
					"  }\n" +
					"}\n" +
					"\n",
					requestor.asts
				);
	}
}
