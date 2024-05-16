/*******************************************************************************
 * Copyright (c) 2010, 2020 IBM Corporation and others.
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
 *     Stephan Herrmann - Contribution for
 *								Bug 461250 - ArrayIndexOutOfBoundsException in SourceTypeBinding.fields
 *     Carmi Grushko - Bug 465048 - Binding is null for class literals in synchronized blocks
 *******************************************************************************/
package org.eclipse.jdt.core.tests.dom;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.FileASTRequestor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.ModuleDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SwitchExpression;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.YieldStatement;
import org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

@SuppressWarnings({ "rawtypes" })
public class StandAloneASTParserTest extends AbstractRegressionTest {
	public StandAloneASTParserTest(String name) {
		super(name);
	}

	private static final int AST_JLS_LATEST = AST.getJLSLatest();

	public ASTNode runConversion(
			int astLevel,
			String source,
			boolean resolveBindings,
			boolean statementsRecovery,
			boolean bindingsRecovery,
			String unitName) {

		ASTParser parser = ASTParser.newParser(astLevel);
		parser.setSource(source.toCharArray());
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(resolveBindings);
		parser.setStatementsRecovery(statementsRecovery);
		parser.setBindingsRecovery(bindingsRecovery);
		parser.setCompilerOptions(getCompilerOptions());
		parser.setUnitName(unitName);
		return parser.createAST(null);
	}
	protected File createFile(File dir, String fileName, String contents) throws IOException {
		File file = new File(dir, fileName);
		try (Writer writer = new BufferedWriter(new FileWriter(file))) {
			writer.write(contents);
		}
		return file;
	}
	public void testBug529654_001() {
		String contents =
				"module m {\n" +
				"}";
		ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
		parser.setSource(contents.toCharArray());
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setUnitName("module-info.java");
		Map<String, String> options = getCompilerOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_9);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_9);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_9);
		parser.setCompilerOptions(options);

		ASTNode node = parser.createAST(null);
		assertTrue("Should be a compilation unit", node instanceof CompilationUnit);
		CompilationUnit unit = (CompilationUnit) node;
		ModuleDeclaration module = unit.getModule();
		assertTrue("Incorrect Module Name", module.getName().getFullyQualifiedName().equals("m"));
	}
	public void test1() {
		String contents =
				"""
			package p;
			public class X {
				public int i;
				public static void main(String[] args) {
					int length = args.length;
					System.out.println(length);
				}
			}""";
		ASTNode node = runConversion(AST_JLS_LATEST, contents, true, true, true, "p/X.java");
		assertTrue("Should be a compilation unit", node instanceof CompilationUnit);
		CompilationUnit unit = (CompilationUnit) node;
		List types = unit.types();
		TypeDeclaration typeDeclaration  = (TypeDeclaration) types.get(0);
		ITypeBinding binding = typeDeclaration.resolveBinding();
		assertNotNull("No binding", binding);
		assertNull("Got a java element", binding.getJavaElement());
		assertEquals("Wrong name", "p.X", binding.getQualifiedName());
		MethodDeclaration methodDeclaration = (MethodDeclaration) typeDeclaration.bodyDeclarations().get(1);
		IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		assertNotNull("No binding", methodBinding);
		assertNull("Got a java element", methodBinding.getJavaElement());
		Block body = methodDeclaration.getBody();
		VariableDeclarationStatement statement = (VariableDeclarationStatement) body.statements().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) statement.fragments().get(0);
		IVariableBinding variableBinding = fragment.resolveBinding();
		assertNotNull("No binding", variableBinding);
		assertNull("Got a java element", variableBinding.getJavaElement());
		ExpressionStatement statement2 = (ExpressionStatement) body.statements().get(1);
		Expression expression = statement2.getExpression();
		MethodInvocation invocation = (MethodInvocation) expression;
		Expression expression2 = invocation.getExpression();
		assertNotNull("No binding", expression2.resolveTypeBinding());

		FieldDeclaration fieldDeclaration = (FieldDeclaration) typeDeclaration.bodyDeclarations().get(0);
		VariableDeclarationFragment fragment2 = (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
		IVariableBinding variableBinding2 = fragment2.resolveBinding();
		assertNotNull("No binding", variableBinding2);
		assertNull("Got a java element", variableBinding2.getJavaElement());
	}

	public void test2() {
		ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setCompilerOptions(getCompilerOptions());

		final String key = "Ljava/lang/String;";
		final IBinding[] bindings = new IBinding[1];

		FileASTRequestor requestor = new FileASTRequestor() {
			public void acceptBinding(String bindingKey, IBinding binding) {
				if (key.equals(bindingKey)) {
					bindings[0] = binding;
				}
			}
		};

		parser.createASTs(new String[] {}, null, new String[] {key}, requestor, null);

		assertNotNull("No binding", bindings[0]);
		assertEquals("Wrong type of binding", IBinding.TYPE, bindings[0].getKind());
		ITypeBinding typeBinding = (ITypeBinding) bindings[0];
		assertEquals("Wrong binding", "java.lang.String", typeBinding.getQualifiedName());
		assertNull("No java element", typeBinding.getJavaElement());
	}

	public void test3() throws IOException {
		File rootDir = new File(System.getProperty("java.io.tmpdir"));
		ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setCompilerOptions(getCompilerOptions());

		final String key = "Lp/X;";
		final IBinding[] bindings = new IBinding[1];

		String contents =
			"""
			package p;
			public class X extends Y {
				public int i;
				public static void main(String[] args) {
					int length = args.length;
					System.out.println(length);
				}
			}""";

		File packageDir = new File(rootDir, "p");
		packageDir.mkdir();
		File file = new File(packageDir, "X.java");
		Writer writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(contents);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}

		String contents2 =
			"package p;\n" +
			"public class Y {}";
		File fileY = new File(packageDir, "Y.java");
		Writer writer2 = null;
		try {
			writer2 = new BufferedWriter(new FileWriter(fileY));
			writer2.write(contents2);
		} finally {
			if (writer2 != null) {
				try {
					writer2.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}

		try {
			final String canonicalPath = file.getCanonicalPath();
			final CompilationUnit[] units = new CompilationUnit[1];

			FileASTRequestor requestor = new FileASTRequestor() {
				public void acceptBinding(String bindingKey, IBinding binding) {
					if (key.equals(bindingKey)) {
						bindings[0] = binding;
					}
				}
				public void acceptAST(String sourceFilePath, CompilationUnit ast) {
					if (canonicalPath.equals(sourceFilePath)) {
						units[0] = ast;
					}
				}
			};

			parser.setEnvironment(null, new String[] { rootDir.getCanonicalPath() }, null, true);

			parser.createASTs(new String[] {canonicalPath}, null, new String[] {key}, requestor, null);

			assertNotNull("No binding", bindings[0]);
			assertEquals("Wrong type of binding", IBinding.TYPE, bindings[0].getKind());
			ITypeBinding typeBinding = (ITypeBinding) bindings[0];
			assertEquals("Wrong binding", "p.X", typeBinding.getQualifiedName());
			assertNull("No java element", typeBinding.getJavaElement());
			assertNotNull("No ast", units[0]);
			assertEquals("No problem", 0, units[0].getProblems().length);
		} finally {
			file.delete();
			fileY.delete();
		}
	}

	public void test4() {
		ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
		try {
			parser.setEnvironment(null, null, new String[] {"UTF-8"}, true);
			assertTrue("Should have failed", false);
		} catch(IllegalArgumentException e) {
			// ignore
		}
	}

	public void test5() {
		ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
		try {
			parser.setEnvironment(null, new String[] {}, new String[] {"UTF-8"}, true);
			assertTrue("Should have failed", false);
		} catch(IllegalArgumentException e) {
			// ignore
		}
	}

	public void test6() throws IOException {
		File rootDir = new File(System.getProperty("java.io.tmpdir"));
		ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setCompilerOptions(getCompilerOptions());

		final String key = "Lp/X;";
		final IBinding[] bindings = new IBinding[2];

		String contents =
			"""
			package p;
			public class X extends Y {
				public int i;
				public static void main(String[] args) {
					int length = args.length;
					System.out.println(length);
				}
			}""";

		File packageDir = new File(rootDir, "p");
		packageDir.mkdir();
		File file = new File(packageDir, "X.java");
		Writer writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(contents);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}

		String contents2 =
			"package p;\n" +
			"public class Y {}";
		File fileY = new File(packageDir, "Y.java");
		Writer writer2 = null;
		try {
			writer2 = new BufferedWriter(new FileWriter(fileY));
			writer2.write(contents2);
		} finally {
			if (writer2 != null) {
				try {
					writer2.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}

		try {
			final String canonicalPath = file.getCanonicalPath();
			final CompilationUnit[] units = new CompilationUnit[1];

			FileASTRequestor requestor = new FileASTRequestor() {
				public void acceptBinding(String bindingKey, IBinding binding) {
					if (key.equals(bindingKey)) {
						bindings[0] = binding;
						IBinding[] temp = createBindings(new String[] {"Ljava/lang/Object;"});
						for (int i = 0; i < temp.length; ++i) {
							bindings[i + 1] = temp[i];
						}
					}
				}
				public void acceptAST(String sourceFilePath, CompilationUnit ast) {
					if (canonicalPath.equals(sourceFilePath)) {
						units[0] = ast;
					}
				}
			};

			parser.setEnvironment(null, new String[] { rootDir.getCanonicalPath() }, null, true);

			parser.createASTs(new String[] {canonicalPath}, null, new String[] {key}, requestor, null);

			assertNotNull("No binding", bindings[0]);
			assertEquals("Wrong type of binding", IBinding.TYPE, bindings[0].getKind());
			ITypeBinding typeBinding = (ITypeBinding) bindings[0];
			assertEquals("Wrong binding", "p.X", typeBinding.getQualifiedName());
			assertNull("No java element", typeBinding.getJavaElement());
			IPackageBinding packageBinding = typeBinding.getPackage();
			assertNull("No java element", packageBinding.getJavaElement());
			assertNotNull("No ast", units[0]);
			assertEquals("No problem", 0, units[0].getProblems().length);
			assertNotNull("No binding", bindings[1]);
			assertEquals("Wrong type of binding", IBinding.TYPE, bindings[1].getKind());
			typeBinding = (ITypeBinding) bindings[1];
			assertEquals("Wrong binding", "java.lang.Object", typeBinding.getQualifiedName());
		} finally {
			file.delete();
			fileY.delete();
		}
	}

	/**
	 * @deprecated
	 */
	public void testBug415066_001() throws IOException {
		File rootDir = new File(System.getProperty("java.io.tmpdir"));
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setCompilerOptions(getCompilerOptions());

		final String key = "Lp/C;";
		final IBinding[] bindings = new IBinding[2];

		String contents =
			"""
			package p;
			public class A{}
			class B{}""";

		File packageDir = new File(rootDir, "p");
		packageDir.mkdir();
		File file = new File(packageDir, "A.java");
		Writer writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(contents);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}

		String contents2 =
			"package p;\n" +
			"public class C extends B {}";
		File fileY = new File(packageDir, "C.java");
		Writer writer2 = null;
		try {
			writer2 = new BufferedWriter(new FileWriter(fileY));
			writer2.write(contents2);
		} finally {
			if (writer2 != null) {
				try {
					writer2.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}

		try {
			final String canonicalPath = fileY.getCanonicalPath();
			final CompilationUnit[] units = new CompilationUnit[1];

			FileASTRequestor requestor = new FileASTRequestor() {
				public void acceptBinding(String bindingKey, IBinding binding) {
					if (key.equals(bindingKey)) {
						bindings[0] = binding;
						IBinding[] temp = createBindings(new String[] {"Lp/C;"});
						for (int i = 0; i < temp.length; ++i) {
							bindings[i + 1] = temp[i];
						}
					}
				}
				public void acceptAST(String sourceFilePath, CompilationUnit ast) {
					if (canonicalPath.equals(sourceFilePath)) {
						units[0] = ast;
					}
				}
			};

			parser.setEnvironment(null, new String[] { rootDir.getCanonicalPath() }, null, true);
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = 0;
			parser.createASTs(new String[] {canonicalPath}, null, new String[] {key}, requestor, null);
			assertNotNull("No ast", units[0]);
			assertEquals("No problem", 0, units[0].getProblems().length);
		} finally {
			file.delete();
			fileY.delete();
		}
	}

	/**
	 * Negative test case
	 * @deprecated
	 */
	public void testBug415066_002() throws IOException {
		File rootDir = new File(System.getProperty("java.io.tmpdir"));
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setCompilerOptions(getCompilerOptions());

		final String key = "Lp/C;";
		final IBinding[] bindings = new IBinding[2];

		String contents =
			"""
			package p;
			public class A{}
			class B{}""";

		File packageDir = new File(rootDir, "p");
		packageDir.mkdir();
		File file = new File(packageDir, "A.java");
		Writer writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(contents);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}

		String contents2 =
			"""
			package q;
			import p.*;
			public class C extends B {}""";
		File fileY = new File(packageDir, "C.java");
		Writer writer2 = null;
		try {
			writer2 = new BufferedWriter(new FileWriter(fileY));
			writer2.write(contents2);
		} finally {
			if (writer2 != null) {
				try {
					writer2.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}

		try {
			final String canonicalPath = fileY.getCanonicalPath();
			final CompilationUnit[] units = new CompilationUnit[1];

			FileASTRequestor requestor = new FileASTRequestor() {
				public void acceptBinding(String bindingKey, IBinding binding) {
					if (key.equals(bindingKey)) {
						bindings[0] = binding;
						IBinding[] temp = createBindings(new String[] {"Lq/C;"});
						for (int i = 0; i < temp.length; ++i) {
							bindings[i + 1] = temp[i];
						}
					}
				}
				public void acceptAST(String sourceFilePath, CompilationUnit ast) {
					if (canonicalPath.equals(sourceFilePath)) {
						units[0] = ast;
					}
				}
			};

			parser.setEnvironment(null, new String[] { rootDir.getCanonicalPath() }, null, true);
			parser.createASTs(new String[] {canonicalPath}, null, new String[] {key}, requestor, null);
			assertNotNull("No ast", units[0]);
			IProblem[] problems = units[0].getProblems();
			assertEquals("No problem", 1, problems.length);
			assertEquals("Pb(3) The type B is not visible", problems[0].toString());
		} finally {
			file.delete();
			fileY.delete();
		}
	}

	public void test7() throws IOException {
		File rootDir = new File(System.getProperty("java.io.tmpdir"));

		String contents =
			"""
			enum X {
			              /** */
			    FOO
			}""";

		File file = new File(rootDir, "X.java");
		Writer writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(contents);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}

		String contents2 =
			"package p;\n" +
			"class Y {}";
		File packageDir = new File(rootDir, "p");
		packageDir.mkdir();
		File fileY = new File(packageDir, "Y.java");
		Writer writer2 = null;
		try {
			writer2 = new BufferedWriter(new FileWriter(fileY));
			writer2.write(contents2);
		} finally {
			if (writer2 != null) {
				try {
					writer2.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}

		try {
			ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setCompilerOptions(JavaCore.getOptions());
			parser.createASTs(
					new String[] { file.getCanonicalPath(), fileY.getCanonicalPath() },
					null,
					new String[] {},
					new FileASTRequestor() {},
					null);
		} finally {
			file.delete();
			fileY.delete();
		}
	}

	public void testBug461250() {
		String source =
				"""
			class QH<T> implements QR.Q {
			  QR.Q q;
			  @V(v = A, d = "") Map p;
			}
			""";
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(source.toCharArray());
		parser.setResolveBindings(true);
		String[] emptyStringArray = new String[0];
		parser.setEnvironment(emptyStringArray, emptyStringArray, emptyStringArray, true /* includeRunningVMBootclasspath */);
		parser.setUnitName("dontCare");
		ASTNode ast = parser.createAST(null);
		assertTrue("should have parsed a CUD", ast instanceof CompilationUnit);
	}

	@Deprecated
	public void testBug465048() {
		String source =
				"class A {\n" +
				"  void f(OtherClass otherClass) {\n" +
				"    synchronized (otherClass) {\n" +
				"      Class c = InnerClass.class;\n" +  // Line = 4
				"    }\n" +
				"  }\n" +
				"  class InnerClass { }\n" +
				"}\n";
		Map<String, String> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		ASTParser parser = ASTParser.newParser(AST.JLS9);
		parser.setCompilerOptions(options);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(source.toCharArray());
		parser.setResolveBindings(true);
		String[] emptyStringArray = new String[0];
		parser.setEnvironment(emptyStringArray, emptyStringArray, emptyStringArray,
				true /* includeRunningVMBootclasspath */);
		parser.setUnitName("dontCare");

		CompilationUnit cu = (CompilationUnit) parser.createAST(null);
		SimpleName innerClassLiteral = (SimpleName) NodeFinder.perform(cu, cu.getPosition(4, 16), 1 /* length */);
		ITypeBinding innerClassBinding = (ITypeBinding) innerClassLiteral.resolveBinding();

		assertEquals("InnerClass", innerClassBinding.getName());
	}

	/**
	 * Verifies that ASTParser doesn't throw an IllegalArgumentException when given
	 * this valid input.
	 * @deprecated
	 */
	public void testBug480545() {
	    String input = "class Test2 { void f(Test2... xs) {} }";
	    ASTParser parser = ASTParser.newParser(AST.JLS9);
	    parser.setSource(input.toCharArray());
	    Map<String, String> options = JavaCore.getOptions();
	    JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
	    parser.setCompilerOptions(options);
	    assertNotNull(parser.createAST(null));
	}
	@Deprecated
	public void testBug493336_001() {
	    String input = """
			public class X implements á¼³ {
			  public static final class if {
			    public static final if ËŠ = new if(null, null, null, null);
			  }
			}""";
	    ASTParser parser = ASTParser.newParser(AST.JLS9);
	    parser.setSource(input.toCharArray());
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setEnvironment(null, new String[] {null}, null, true);

		Hashtable<String, String> options1 = JavaCore.getDefaultOptions();
		options1.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_8);
	    options1.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_8);
	    options1.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_8);
	    parser.setCompilerOptions(options1);
	    assertNotNull(parser.createAST(null));
	}
	@Deprecated
	public void testBug526996_001() {
		File rootDir = new File(System.getProperty("java.io.tmpdir"));
		String contents =
				"""
			public class X {
			    public X() {
			        this.f16132b =
			/*
			        at jadx.api.JavaClass.decompile(JavaClass.java:62)
			*/
			
			            /* JADX WARNING: inconsistent code. */
			            /* Code decompiled incorrectly, please refer to instructions dump. */
			            public final C1984r m22030a() {
			            }
			        }
			
			""";

		File file = new File(rootDir, "X.java");
		Writer writer = null;
		try {
			try {
				writer = new BufferedWriter(new FileWriter(file));
				writer.write(contents);
			} catch (IOException e1) {
				// ignore
			}
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}
		String contents2 =
				"""
			public class Y {
			
			    /* JADX WARNING: inconsistent code. */
			    protected void finalize() {
			        for (i =
			/*
			        at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
			*/
			        public void close() { }
			    }
			""" ;

		File fileY = new File(rootDir, "Y.java");
		Writer writer2 = null;
		try {
			try {
				writer2 = new BufferedWriter(new FileWriter(fileY));
				writer2.write(contents2);
			} catch (IOException e) {
				// ignore
			}
		} finally {
			try {
				if (writer2 != null) writer2.close();
			} catch(IOException e) {
				// ignore
			}
		}
		try {
			final FileASTRequestor astRequestor = new FileASTRequestor() {
				@Override
				public void acceptAST(String sourceFilePath, CompilationUnit ast) {
					super.acceptAST(sourceFilePath, ast);
				}
			};
			ASTParser parser = ASTParser.newParser(AST.JLS9);
			parser.setResolveBindings(true);
			parser.setStatementsRecovery(true);
			parser.setBindingsRecovery(true);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setEnvironment(new String[0], new String[] { rootDir.getAbsolutePath() }, null, true);
		    String[] files = null;
			try {
				files = new String[] {file.getCanonicalPath(), fileY.getCanonicalPath()};
				parser.createASTs(files,
						null,
						new String[0],
						astRequestor,
						null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} finally {
			file.delete();
			fileY.delete();
		}
	}
	public void testBug526996_002() {
		File rootDir = new File(System.getProperty("java.io.tmpdir"));
		String contents =
						"""
			public class zzei {
			    private final Context mContext;
			    private final String zzAg;
			    private zzb<zzbb> zzAh;
			    private zzb<zzbb> zzAi;
			    private zze zzAj;
			    private int zzAk;
			    private final VersionInfoParcel zzpI;
			    private final Object zzpK;
			
			    public interface zzb<T> {
			        void zzc(T t);
			    }
			
			    class zza {
			        static int zzAu = 60000;
			        static int zzAv = 10000;
			    }
			
			    public class zzc<T> implements zzb<T> {
			        public void zzc(T t) {
			        }
			    }
			
			    public class zzd extends zzjh<zzbe> {
			        private final zze zzAw;
			        private boolean zzAx;
			        private final Object zzpK = new Object();
			
			        public zzd(zze com_google_android_gms_internal_zzei_zze) {
			            this.zzAw = com_google_android_gms_internal_zzei_zze;
			        }
			
			        public void release() {
			            synchronized (this.zzpK) {
			                if (this.zzAx) {
			                    return;
			                }
			                this.zzAx = true;
			                zza(new com.google.android.gms.internal.zzjg.zzc<zzbe>(this) {
			                    final /* synthetic */ zzd zzAy;
			
			                    {
			                        this.zzAy = r1;
			                    }
			
			                    public void zzb(zzbe com_google_android_gms_internal_zzbe) {
			                        com.google.android.gms.ads.internal.util.client.zzb.v("Ending javascript session.");
			                        ((zzbf) com_google_android_gms_internal_zzbe).zzcs();
			                    }
			
			                    public /* synthetic */ void zzc(Object obj) {
			                        zzb((zzbe) obj);
			                    }
			                }, new com.google.android.gms.internal.zzjg.zzb());
			                zza(new com.google.android.gms.internal.zzjg.zzc<zzbe>(this) {
			                    final /* synthetic */ zzd zzAy;
			
			                    {
			                        this.zzAy = r1;
			                    }
			
			                    public void zzb(zzbe com_google_android_gms_internal_zzbe) {
			                        com.google.android.gms.ads.internal.util.client.zzb.v("Releasing engine reference.");
			                        this.zzAy.zzAw.zzek();
			                    }
			
			                    public /* synthetic */ void zzc(Object obj) {
			                        zzb((zzbe) obj);
			                    }
			                }, new com.google.android.gms.internal.zzjg.zza(this) {
			                    final /* synthetic */ zzd zzAy;
			
			                    {
			                        this.zzAy = r1;
			                    }
			
			                    public void run() {
			                        this.zzAy.zzAw.zzek();
			                    }
			                });
			            }
			        }
			    }
			
			    public class zze extends zzjh<zzbb> {
			        private int zzAA;
			        private zzb<zzbb> zzAi;
			        private boolean zzAz;
			        private final Object zzpK = new Object();
			
			        public zze(zzb<zzbb> com_google_android_gms_internal_zzei_zzb_com_google_android_gms_internal_zzbb) {
			            this.zzAi = com_google_android_gms_internal_zzei_zzb_com_google_android_gms_internal_zzbb;
			            this.zzAz = false;
			            this.zzAA = 0;
			        }
			
			        public zzd zzej() {
			            final zzd com_google_android_gms_internal_zzei_zzd = new zzd(this);
			            synchronized (this.zzpK) {
			                zza(new com.google.android.gms.internal.zzjg.zzc<zzbb>(this) {
			                    final /* synthetic */ zze zzAC;
			
			                    public void zza(zzbb com_google_android_gms_internal_zzbb) {
			                        com.google.android.gms.ads.internal.util.client.zzb.v("Getting a new session for JS Engine.");
			                        com_google_android_gms_internal_zzei_zzd.zzg(com_google_android_gms_internal_zzbb.zzcq());
			                    }
			
			                    public /* synthetic */ void zzc(Object obj) {
			                        zza((zzbb) obj);
			                    }
			                }, new com.google.android.gms.internal.zzjg.zza(this) {
			                    final /* synthetic */ zze zzAC;
			
			                    public void run() {
			                        com.google.android.gms.ads.internal.util.client.zzb.v("Rejecting reference for JS Engine.");
			                        com_google_android_gms_internal_zzei_zzd.reject();
			                    }
			                });
			                zzx.zzaa(this.zzAA >= 0);
			                this.zzAA++;
			            }
			            return com_google_android_gms_internal_zzei_zzd;
			        }
			
			        protected void zzek() {
			            boolean z = true;
			            synchronized (this.zzpK) {
			                if (this.zzAA < 1) {
			                    z = false;
			                }
			                zzx.zzaa(z);
			                com.google.android.gms.ads.internal.util.client.zzb.v("Releasing 1 reference for JS Engine");
			                this.zzAA--;
			                zzem();
			            }
			        }
			
			        public void zzel() {
			            boolean z = true;
			            synchronized (this.zzpK) {
			                if (this.zzAA < 0) {
			                    z = false;
			                }
			                zzx.zzaa(z);
			                com.google.android.gms.ads.internal.util.client.zzb.v("Releasing root reference. JS Engine will be destroyed once other references are released.");
			                this.zzAz = true;
			                zzem();
			            }
			        }
			
			        protected void zzem() {
			            synchronized (this.zzpK) {
			                zzx.zzaa(this.zzAA >= 0);
			                if (this.zzAz && this.zzAA == 0) {
			                    com.google.android.gms.ads.internal.util.client.zzb.v("No reference is left (including root). Cleaning up engine.");
			                    zza(new com.google.android.gms.internal.zzjg.zzc<zzbb>(this) {
			                        final /* synthetic */ zze zzAC;
			
			                        {
			                            this.zzAC = r1;
			                        }
			
			                        public void zza(final zzbb com_google_android_gms_internal_zzbb) {
			                            zzip.runOnUiThread(new Runnable(this) {
			                                final /* synthetic */ AnonymousClass3 zzAD;
			
			                                public void run() {
			                                    this.zzAD.zzAC.zzAi.zzc(com_google_android_gms_internal_zzbb);
			                                    com_google_android_gms_internal_zzbb.destroy();
			                                }
			                            });
			                        }
			
			                        public /* synthetic */ void zzc(Object obj) {
			                            zza((zzbb) obj);
			                        }
			                    }, new com.google.android.gms.internal.zzjg.zzb());
			                } else {
			                    com.google.android.gms.ads.internal.util.client.zzb.v("There are still references to the engine. Not destroying.");
			                }
			            }
			        }
			    }
			
			    public zzei(Context context, VersionInfoParcel versionInfoParcel, String str) {
			        this.zzpK = new Object();
			        this.zzAk = 1;
			        this.zzAg = str;
			        this.mContext = context.getApplicationContext();
			        this.zzpI = versionInfoParcel;
			        this.zzAh = new zzc();
			        this.zzAi = new zzc();
			    }
			
			    public zzei(Context context, VersionInfoParcel versionInfoParcel, String str, zzb<zzbb> com_google_android_gms_internal_zzei_zzb_com_google_android_gms_internal_zzbb, zzb<zzbb> com_google_android_gms_internal_zzei_zzb_com_google_android_gms_internal_zzbb2) {
			        this(context, versionInfoParcel, str);
			        this.zzAh = com_google_android_gms_internal_zzei_zzb_com_google_android_gms_internal_zzbb;
			        this.zzAi = com_google_android_gms_internal_zzei_zzb_com_google_android_gms_internal_zzbb2;
			    }
			
			    private zze zzeg() {
			        final zze com_google_android_gms_internal_zzei_zze = new zze(this.zzAi);
			        zzip.runOnUiThread(new Runnable(this) {
			            final /* synthetic */ zzei zzAm;
			
			            public void run() {
			                final zzbb zza = this.zzAm.zza(this.zzAm.mContext, this.zzAm.zzpI);
			                zza.zza(new com.google.android.gms.internal.zzbb.zza(this) {
			                    final /* synthetic */ AnonymousClass1 zzAo;
			
			                    public void zzcr() {
			                        zzip.zzKO.postDelayed(new Runnable(this) {
			                            final /* synthetic */ AnonymousClass1 zzAp;
			
			                            {
			                                this.zzAp = r1;
			                            }
			
			                            /* JADX WARNING: inconsistent code. */
			                            /* Code decompiled incorrectly, please refer to instructions dump. */
			                            public void run() {
			                                /*
			                                r3 = this;
			                                r0 = r3.zzAp;
			                                r0 = r0.zzAo;
			                                r0 = r0.zzAm;
			                                r1 = r0.zzpK;
			                                monitor-enter(r1);
			                                r0 = r3.zzAp;	 Catch:{ all -> 0x003f }
			                                r0 = r0.zzAo;	 Catch:{ all -> 0x003f }
			                                r0 = r0;	 Catch:{ all -> 0x003f }
			                                r0 = r0.getStatus();	 Catch:{ all -> 0x003f }
			                                r2 = -1;
			                                if (r0 == r2) throw GOTO_REPLACEMENT_1_L_0x0025;
			                            L_0x0018:
			                                r0 = r3.zzAp;	 Catch:{ all -> 0x003f }
			                                r0 = r0.zzAo;	 Catch:{ all -> 0x003f }
			                                r0 = r0;	 Catch:{ all -> 0x003f }
			                                r0 = r0.getStatus();	 Catch:{ all -> 0x003f }
			                                r2 = 1;
			                                if (r0 != r2) throw GOTO_REPLACEMENT_2_L_0x0027;
			                            L_0x0025:
			                                monitor-exit(r1);	 Catch:{ all -> 0x003f }
			                            L_0x0026:
			                                return;
			                            L_0x0027:
			                                r0 = r3.zzAp;	 Catch:{ all -> 0x003f }
			                                r0 = r0.zzAo;	 Catch:{ all -> 0x003f }
			                                r0 = r0;	 Catch:{ all -> 0x003f }
			                                r0.reject();	 Catch:{ all -> 0x003f }
			                                r0 = new com.google.android.gms.internal.zzei$1$1$1$1;	 Catch:{ all -> 0x003f }
			                                r0.<init>(r3);	 Catch:{ all -> 0x003f }
			                                com.google.android.gms.internal.zzip.runOnUiThread(r0);	 Catch:{ all -> 0x003f }
			                                r0 = "Could not receive loaded message in a timely manner. Rejecting.";
			                                com.google.android.gms.ads.internal.util.client.zzb.v(r0);	 Catch:{ all -> 0x003f }
			                                monitor-exit(r1);	 Catch:{ all -> 0x003f }
			                                throw GOTO_REPLACEMENT_3_L_0x0026;
			                            L_0x003f:
			                                r0 = move-exception;
			                                monitor-exit(r1);	 Catch:{ all -> 0x003f }
			                                throw r0;
			                                */
			                                throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzei.1.1.1.run():void");
			                            }
			                        }, (long) zza.zzAv);
			                    }
			                });
			                zza.zza("/jsLoaded", new zzdl(this) {
			                    final /* synthetic */ AnonymousClass1 zzAo;
			
			                    /* JADX WARNING: inconsistent code. */
			                    /* Code decompiled incorrectly, please refer to instructions dump. */
			                    public void zza(com.google.android.gms.internal.zzjn r4, java.util.Map<java.lang.String, java.lang.String> r5) {
			                        /*
			                        r3 = this;
			                        r0 = r3.zzAo;
			                        r0 = r0.zzAm;
			                        r1 = r0.zzpK;
			                        monitor-enter(r1);
			                        r0 = r3.zzAo;	 Catch:{ all -> 0x0051 }
			                        r0 = r0;	 Catch:{ all -> 0x0051 }
			                        r0 = r0.getStatus();	 Catch:{ all -> 0x0051 }
			                        r2 = -1;
			                        if (r0 == r2) throw GOTO_REPLACEMENT_4_L_0x001f;
			                    L_0x0014:
			                        r0 = r3.zzAo;	 Catch:{ all -> 0x0051 }
			                        r0 = r0;	 Catch:{ all -> 0x0051 }
			                        r0 = r0.getStatus();	 Catch:{ all -> 0x0051 }
			                        r2 = 1;
			                        if (r0 != r2) throw GOTO_REPLACEMENT_5_L_0x0021;
			                    L_0x001f:
			                        monitor-exit(r1);	 Catch:{ all -> 0x0051 }
			                    L_0x0020:
			                        return;
			                    L_0x0021:
			                        r0 = r3.zzAo;	 Catch:{ all -> 0x0051 }
			                        r0 = r0.zzAm;	 Catch:{ all -> 0x0051 }
			                        r2 = 0;
			                        r0.zzAk = r2;	 Catch:{ all -> 0x0051 }
			                        r0 = r3.zzAo;	 Catch:{ all -> 0x0051 }
			                        r0 = r0.zzAm;	 Catch:{ all -> 0x0051 }
			                        r0 = r0.zzAh;	 Catch:{ all -> 0x0051 }
			                        r2 = r0;	 Catch:{ all -> 0x0051 }
			                        r0.zzc(r2);	 Catch:{ all -> 0x0051 }
			                        r0 = r3.zzAo;	 Catch:{ all -> 0x0051 }
			                        r0 = r0;	 Catch:{ all -> 0x0051 }
			                        r2 = r0;	 Catch:{ all -> 0x0051 }
			                        r0.zzg(r2);	 Catch:{ all -> 0x0051 }
			                        r0 = r3.zzAo;	 Catch:{ all -> 0x0051 }
			                        r0 = r0.zzAm;	 Catch:{ all -> 0x0051 }
			                        r2 = r3.zzAo;	 Catch:{ all -> 0x0051 }
			                        r2 = r0;	 Catch:{ all -> 0x0051 }
			                        r0.zzAj = r2;	 Catch:{ all -> 0x0051 }
			                        r0 = "Successfully loaded JS Engine.";
			                        com.google.android.gms.ads.internal.util.client.zzb.v(r0);	 Catch:{ all -> 0x0051 }
			                        monitor-exit(r1);	 Catch:{ all -> 0x0051 }
			                        throw GOTO_REPLACEMENT_6_L_0x0020;
			                    L_0x0051:
			                        r0 = move-exception;
			                        monitor-exit(r1);	 Catch:{ all -> 0x0051 }
			                        throw r0;
			                        */
			                        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzei.1.2.zza(com.google.android.gms.internal.zzjn, java.util.Map):void");
			                    }
			                });
			                final zziy com_google_android_gms_internal_zziy = new zziy();
			                zzdl anonymousClass3 = new zzdl(this) {
			                    final /* synthetic */ AnonymousClass1 zzAo;
			
			                    public void zza(zzjn com_google_android_gms_internal_zzjn, Map<String, String> map) {
			                        synchronized (this.zzAo.zzAm.zzpK) {
			                            com.google.android.gms.ads.internal.util.client.zzb.zzaG("JS Engine is requesting an update");
			                            if (this.zzAo.zzAm.zzAk == 0) {
			                                com.google.android.gms.ads.internal.util.client.zzb.zzaG("Starting reload.");
			                                this.zzAo.zzAm.zzAk = 2;
			                                this.zzAo.zzAm.zzeh();
			                            }
			                            zza.zzb("/requestReload", (zzdl) com_google_android_gms_internal_zziy.get());
			                        }
			                    }
			                };
			                com_google_android_gms_internal_zziy.set(anonymousClass3);
			                zza.zza("/requestReload", anonymousClass3);
			                if (this.zzAm.zzAg.endsWith(".js")) {
			                    zza.zzs(this.zzAm.zzAg);
			                } else if (this.zzAm.zzAg.startsWith("<html>")) {
			                    zza.zzu(this.zzAm.zzAg);
			                } else {
			                    zza.zzt(this.zzAm.zzAg);
			                }
			                zzip.zzKO.postDelayed(new Runnable(this) {
			                    final /* synthetic */ AnonymousClass1 zzAo;
			
			                    /* JADX WARNING: inconsistent code. */
			                    /* Code decompiled incorrectly, please refer to instructions dump. */
			                    public void run() {
			                        /*
			                        r3 = this;
			                        r0 = r3.zzAo;
			                        r0 = r0.zzAm;
			                        r1 = r0.zzpK;
			                        monitor-enter(r1);
			                        r0 = r3.zzAo;	 Catch:{ all -> 0x0037 }
			                        r0 = r0;	 Catch:{ all -> 0x0037 }
			                        r0 = r0.getStatus();	 Catch:{ all -> 0x0037 }
			                        r2 = -1;
			                        if (r0 == r2) throw GOTO_REPLACEMENT_7_L_0x001f;
			                    L_0x0014:
			                        r0 = r3.zzAo;	 Catch:{ all -> 0x0037 }
			                        r0 = r0;	 Catch:{ all -> 0x0037 }
			                        r0 = r0.getStatus();	 Catch:{ all -> 0x0037 }
			                        r2 = 1;
			                        if (r0 != r2) throw GOTO_REPLACEMENT_8_L_0x0021;
			                    L_0x001f:
			                        monitor-exit(r1);	 Catch:{ all -> 0x0037 }
			                    L_0x0020:
			                        return;
			                    L_0x0021:
			                        r0 = r3.zzAo;	 Catch:{ all -> 0x0037 }
			                        r0 = r0;	 Catch:{ all -> 0x0037 }
			                        r0.reject();	 Catch:{ all -> 0x0037 }
			                        r0 = new com.google.android.gms.internal.zzei$1$4$1;	 Catch:{ all -> 0x0037 }
			                        r0.<init>(r3);	 Catch:{ all -> 0x0037 }
			                        com.google.android.gms.internal.zzip.runOnUiThread(r0);	 Catch:{ all -> 0x0037 }
			                        r0 = "Could not receive loaded message in a timely manner. Rejecting.";
			                        com.google.android.gms.ads.internal.util.client.zzb.v(r0);	 Catch:{ all -> 0x0037 }
			                        monitor-exit(r1);	 Catch:{ all -> 0x0037 }
			                        throw GOTO_REPLACEMENT_9_L_0x0020;
			                    L_0x0037:
			                        r0 = move-exception;
			                        monitor-exit(r1);	 Catch:{ all -> 0x0037 }
			                        throw r0;
			                        */
			                        throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzei.1.4.run():void");
			                    }
			                }, (long) zza.zzAu);
			            }
			        });
			        return com_google_android_gms_internal_zzei_zze;
			    }
			}
			""";

		File file = new File(rootDir, "zzei.java");
		Writer writer = null;
		try {
			try {
				writer = new BufferedWriter(new FileWriter(file));
				writer.write(contents);
			} catch (IOException e1) {
				// ignore
			}
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}
		String contents2 =
						"""
			public final class dm {
			    private final byte[] a;
			    private final boolean b;
			    private int c;
			    private int d;
			    private int e;
			    private final InputStream f;
			    private int g;
			    private boolean h;
			    private int i;
			    private int j;
			    private int k;
			    private int l;
			    private int m;
			    private a n;
			
			    public static dm a(byte[] bArr, int i) {
			        dm dmVar = new dm(bArr, i);
			        try {
			            dmVar.b(i);
			            return dmVar;
			        } catch (Throwable e) {
			            throw new IllegalArgumentException(e);
			        }
			    }
			
			    public final int a() {
			        int i = 1;
			        if (this.e != this.c || d(1)) {
			            i = 0;
			        }
			        if (i != 0) {
			            this.g = 0;
			            return 0;
			        }
			        this.g = e();
			        if (ed.b(this.g) != 0) {
			            return this.g;
			        }
			        throw dr.d();
			    }
			
			    public final void a(int i) {
			        if (this.g != i) {
			            throw dr.e();
			        }
			    }
			
			    public final dv a(dx dxVar, do doVar) {
			        int e = e();
			        if (this.k >= this.l) {
			            throw dr.g();
			        }
			        int b = b(e);
			        this.k++;
			        dv dvVar = (dv) dxVar.a(this, doVar);
			        a(0);
			        this.k--;
			        this.j = b;
			        i();
			        return dvVar;
			    }
			
			    /* JADX WARNING: inconsistent code. */
			    /* Code decompiled incorrectly, please refer to instructions dump. */
			    public final int e() {
			        /*
			        r8 = this;
			        r6 = 0;
			        r0 = r8.e;
			        r1 = r8.c;
			        if (r1 == r0) throw GOTO_REPLACEMENT_1_L_0x0081;
			    L_0x0008:
			        r3 = r8.a;
			        r2 = r0 + 1;
			        r0 = r3[r0];
			        if (r0 < 0) throw GOTO_REPLACEMENT_2_L_0x0013;
			    L_0x0010:
			        r8.e = r2;
			    L_0x0012:
			        return r0;
			    L_0x0013:
			        r1 = r8.c;
			        r1 = r1 - r2;
			        r4 = 9;
			        if (r1 < r4) throw GOTO_REPLACEMENT_3_L_0x0081;
			    L_0x001a:
			        r1 = r2 + 1;
			        r2 = r3[r2];
			        r2 = r2 << 7;
			        r0 = r0 ^ r2;
			        r4 = (long) r0;
			        r2 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
			        if (r2 >= 0) throw GOTO_REPLACEMENT_4_L_0x002e;
			    L_0x0026:
			        r2 = (long) r0;
			        r4 = -128; // 0xffffffffffffff80 float:NaN double:NaN;
			        r2 = r2 ^ r4;
			        r0 = (int) r2;
			    L_0x002b:
			        r8.e = r1;
			        throw GOTO_REPLACEMENT_5_L_0x0012;
			    L_0x002e:
			        r2 = r1 + 1;
			        r1 = r3[r1];
			        r1 = r1 << 14;
			        r0 = r0 ^ r1;
			        r4 = (long) r0;
			        r1 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
			        if (r1 < 0) throw GOTO_REPLACEMENT_6_L_0x0041;
			    L_0x003a:
			        r0 = (long) r0;
			        r4 = 16256; // 0x3f80 float:2.278E-41 double:8.0315E-320;
			        r0 = r0 ^ r4;
			        r0 = (int) r0;
			        r1 = r2;
			        throw GOTO_REPLACEMENT_7_L_0x002b;
			    L_0x0041:
			        r1 = r2 + 1;
			        r2 = r3[r2];
			        r2 = r2 << 21;
			        r0 = r0 ^ r2;
			        r4 = (long) r0;
			        r2 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1));
			        if (r2 >= 0) throw GOTO_REPLACEMENT_8_L_0x0054;
			    L_0x004d:
			        r2 = (long) r0;
			        r4 = -2080896; // 0xffffffffffe03f80 float:NaN double:NaN;
			        r2 = r2 ^ r4;
			        r0 = (int) r2;
			        throw GOTO_REPLACEMENT_9_L_0x002b;
			    L_0x0054:
			        r2 = r1 + 1;
			        r1 = r3[r1];
			        r4 = r1 << 28;
			        r0 = r0 ^ r4;
			        r4 = (long) r0;
			        r6 = 266354560; // 0xfe03f80 float:2.2112565E-29 double:1.315966377E-315;
			        r4 = r4 ^ r6;
			        r0 = (int) r4;
			        if (r1 >= 0) throw GOTO_REPLACEMENT_10_L_0x0087;
			    L_0x0063:
			        r1 = r2 + 1;
			        r2 = r3[r2];
			        if (r2 >= 0) throw GOTO_REPLACEMENT_11_L_0x002b;
			    L_0x0069:
			        r2 = r1 + 1;
			        r1 = r3[r1];
			        if (r1 >= 0) throw GOTO_REPLACEMENT_12_L_0x0087;
			    L_0x006f:
			        r1 = r2 + 1;
			        r2 = r3[r2];
			        if (r2 >= 0) throw GOTO_REPLACEMENT_13_L_0x002b;
			    L_0x0075:
			        r2 = r1 + 1;
			        r1 = r3[r1];
			        if (r1 >= 0) throw GOTO_REPLACEMENT_14_L_0x0087;
			    L_0x007b:
			        r1 = r2 + 1;
			        r2 = r3[r2];
			        if (r2 >= 0) throw GOTO_REPLACEMENT_15_L_0x002b;
			    L_0x0081:
			        r0 = r8.h();
			        r0 = (int) r0;
			        throw GOTO_REPLACEMENT_16_L_0x0012;
			    L_0x0087:
			        r1 = r2;
			        throw GOTO_REPLACEMENT_17_L_0x002b;
			        */
			        throw new UnsupportedOperationException("Method not decompiled: com.tapjoy.internal.dm.e():int");
			    }
			
			    /* JADX WARNING: inconsistent code. */
			    /* Code decompiled incorrectly, please refer to instructions dump. */
			    public final long f() {
			        /*
			        r10 = this;
			        r8 = 0;
			        r0 = r10.e;
			        r1 = r10.c;
			        if (r1 == r0) throw GOTO_REPLACEMENT_18_L_0x00bb;
			    L_0x0008:
			        r4 = r10.a;
			        r1 = r0 + 1;
			        r0 = r4[r0];
			        if (r0 < 0) throw GOTO_REPLACEMENT_19_L_0x0014;
			    L_0x0010:
			        r10.e = r1;
			        r0 = (long) r0;
			    L_0x0013:
			        return r0;
			    L_0x0014:
			        r2 = r10.c;
			        r2 = r2 - r1;
			        r3 = 9;
			        if (r2 < r3) throw GOTO_REPLACEMENT_20_L_0x00bb;
			    L_0x001b:
			        r2 = r1 + 1;
			        r1 = r4[r1];
			        r1 = r1 << 7;
			        r0 = r0 ^ r1;
			        r0 = (long) r0;
			        r3 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));
			        if (r3 >= 0) throw GOTO_REPLACEMENT_21_L_0x002d;
			    L_0x0027:
			        r4 = -128; // 0xffffffffffffff80 float:NaN double:NaN;
			        r0 = r0 ^ r4;
			    L_0x002a:
			        r10.e = r2;
			        throw GOTO_REPLACEMENT_22_L_0x0013;
			    L_0x002d:
			        r3 = r2 + 1;
			        r2 = r4[r2];
			        r2 = r2 << 14;
			        r6 = (long) r2;
			        r0 = r0 ^ r6;
			        r2 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));
			        if (r2 < 0) throw GOTO_REPLACEMENT_23_L_0x003e;
			    L_0x0039:
			        r4 = 16256; // 0x3f80 float:2.278E-41 double:8.0315E-320;
			        r0 = r0 ^ r4;
			        r2 = r3;
			        throw GOTO_REPLACEMENT_24_L_0x002a;
			    L_0x003e:
			        r2 = r3 + 1;
			        r3 = r4[r3];
			        r3 = r3 << 21;
			        r6 = (long) r3;
			        r0 = r0 ^ r6;
			        r3 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));
			        if (r3 >= 0) throw GOTO_REPLACEMENT_25_L_0x004f;
			    L_0x004a:
			        r4 = -2080896; // 0xffffffffffe03f80 float:NaN double:NaN;
			        r0 = r0 ^ r4;
			        throw GOTO_REPLACEMENT_26_L_0x002a;
			    L_0x004f:
			        r3 = r2 + 1;
			        r2 = r4[r2];
			        r6 = (long) r2;
			        r2 = 28;
			        r6 = r6 << r2;
			        r0 = r0 ^ r6;
			        r2 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));
			        if (r2 < 0) throw GOTO_REPLACEMENT_27_L_0x0062;
			    L_0x005c:
			        r4 = 266354560; // 0xfe03f80 float:2.2112565E-29 double:1.315966377E-315;
			        r0 = r0 ^ r4;
			        r2 = r3;
			        throw GOTO_REPLACEMENT_28_L_0x002a;
			    L_0x0062:
			        r2 = r3 + 1;
			        r3 = r4[r3];
			        r6 = (long) r3;
			        r3 = 35;
			        r6 = r6 << r3;
			        r0 = r0 ^ r6;
			        r3 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));
			        if (r3 >= 0) throw GOTO_REPLACEMENT_29_L_0x0076;
			    L_0x006f:
			        r4 = -34093383808; // 0xfffffff80fe03f80 float:2.2112565E-29 double:NaN;
			        r0 = r0 ^ r4;
			        throw GOTO_REPLACEMENT_30_L_0x002a;
			    L_0x0076:
			        r3 = r2 + 1;
			        r2 = r4[r2];
			        r6 = (long) r2;
			        r2 = 42;
			        r6 = r6 << r2;
			        r0 = r0 ^ r6;
			        r2 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));
			        if (r2 < 0) throw GOTO_REPLACEMENT_31_L_0x008b;
			    L_0x0083:
			        r4 = 4363953127296; // 0x3f80fe03f80 float:2.2112565E-29 double:2.1560793202584E-311;
			        r0 = r0 ^ r4;
			        r2 = r3;
			        throw GOTO_REPLACEMENT_32_L_0x002a;
			    L_0x008b:
			        r2 = r3 + 1;
			        r3 = r4[r3];
			        r6 = (long) r3;
			        r3 = 49;
			        r6 = r6 << r3;
			        r0 = r0 ^ r6;
			        r3 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));
			        if (r3 >= 0) throw GOTO_REPLACEMENT_33_L_0x009f;
			    L_0x0098:
			        r4 = -558586000294016; // 0xfffe03f80fe03f80 float:2.2112565E-29 double:NaN;
			        r0 = r0 ^ r4;
			        throw GOTO_REPLACEMENT_34_L_0x002a;
			    L_0x009f:
			        r3 = r2 + 1;
			        r2 = r4[r2];
			        r6 = (long) r2;
			        r2 = 56;
			        r6 = r6 << r2;
			        r0 = r0 ^ r6;
			        r6 = 71499008037633920; // 0xfe03f80fe03f80 float:2.2112565E-29 double:6.838959413692434E-304;
			        r0 = r0 ^ r6;
			        r2 = (r0 > r8 ? 1 : (r0 == r8 ? 0 : -1));
			        if (r2 >= 0) throw GOTO_REPLACEMENT_35_L_0x00c1;
			    L_0x00b2:
			        r2 = r3 + 1;
			        r3 = r4[r3];
			        r4 = (long) r3;
			        r3 = (r4 > r8 ? 1 : (r4 == r8 ? 0 : -1));
			        if (r3 >= 0) throw GOTO_REPLACEMENT_36_L_0x002a;
			    L_0x00bb:
			        r0 = r10.h();
			        throw GOTO_REPLACEMENT_37_L_0x0013;
			    L_0x00c1:
			        r2 = r3;
			        throw GOTO_REPLACEMENT_38_L_0x002a;
			        */
			        throw new UnsupportedOperationException("Method not decompiled: com.tapjoy.internal.dm.f():long");
			    }
			
			    private long h() {
			        long j = 0;
			        for (int i = 0; i < 64; i += 7) {
			            if (this.e == this.c) {
			                c(1);
			            }
			            byte[] bArr = this.a;
			            int i2 = this.e;
			            this.e = i2 + 1;
			            byte b = bArr[i2];
			            j |= ((long) (b & 127)) << i;
			            if ((b & 128) == 0) {
			                return j;
			            }
			        }
			        throw dr.c();
			    }
			}
			""";

		File fileY = new File(rootDir, "dm.java");
		Writer writer2 = null;
		try {
			try {
				writer2 = new BufferedWriter(new FileWriter(fileY));
				writer2.write(contents2);
			} catch (IOException e) {
				// ignore
			}
		} finally {
			try {
				if (writer2 != null) writer2.close();
			} catch(IOException e) {
				// ignore
			}
		}
		try {
			final FileASTRequestor astRequestor = new FileASTRequestor() {
				@Override
				public void acceptAST(String sourceFilePath, CompilationUnit ast) {
					super.acceptAST(sourceFilePath, ast);
				}
			};
			ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
			parser.setResolveBindings(true);
			parser.setStatementsRecovery(true);
			parser.setBindingsRecovery(true);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setEnvironment(new String[0], new String[] { rootDir.getAbsolutePath() }, null, true);
		    String[] files = null;
			try {
				files = new String[] {file.getCanonicalPath(), fileY.getCanonicalPath()};
				parser.createASTs(files,
						null,
						new String[0],
						astRequestor,
						null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} finally {
			file.delete();
			fileY.delete();
		}
	}
	public void testBug530299_001() {
		String contents =
				"""
			public class X {
				public static void main(String[] args) {
					var x = new X();
			       for (var i = 0; i < 10; ++i) {}
				}
			}""";
	    ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
	    parser.setSource(contents.toCharArray());
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setEnvironment(null, new String[] {null}, null, true);
		parser.setResolveBindings(true);
		Map<String, String> options = getCompilerOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_10);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_10);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_10);
		parser.setCompilerOptions(options);
		ASTNode node = parser.createAST(null);
		assertEquals("Not a compilation unit", ASTNode.COMPILATION_UNIT, node.getNodeType());
		CompilationUnit cu = (CompilationUnit) node;
		assertTrue("Problems in compilation", cu.getProblems().length == 0);
		TypeDeclaration typeDeclaration = (TypeDeclaration) cu.types().get(0);
		MethodDeclaration[] methods = typeDeclaration.getMethods();
		MethodDeclaration methodDeclaration = methods[0];
		VariableDeclarationStatement vStmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
		Type type = vStmt.getType();
		assertNotNull(type);
		assertTrue("not a var", type.isVar());
	}
	public void testBug482254() throws IOException {
		File rootDir = new File(System.getProperty("java.io.tmpdir"));

		String contents =
			"""
			enum X {
			              /** */
			    FOO
			}""";

		File file = new File(rootDir, "X.java");
		Writer writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(contents);
		} finally {
			if (writer != null) {
				try {
					writer.close();
				} catch(IOException e) {
					// ignore
				}
			}
		}

		File packageDir = new File(rootDir, "p");
		packageDir.mkdir();
		File fileY = new File(packageDir, "Y.java");
		String canonicalPath = fileY.getCanonicalPath();

		packageDir = new File(rootDir, "p");
		packageDir.mkdir();
		fileY = new File(packageDir, "Z.java");
		String canonicalPath2 = fileY.getCanonicalPath();

		contents =
				"""
					enum X {
					              /** */
					    FOO
					}""";

			File file2 = new File(rootDir, "X.java");
			writer = null;
			try {
				writer = new BufferedWriter(new FileWriter(file2));
				writer.write(contents);
			} finally {
				if (writer != null) {
					try {
						writer.close();
					} catch(IOException e) {
						// ignore
					}
				}
			}

		try {
			ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setEnvironment(null, null, null, true);
			parser.setResolveBindings(true);
			parser.setCompilerOptions(JavaCore.getOptions());
			parser.createASTs(
					new String[] { file.getCanonicalPath(), canonicalPath, canonicalPath2, file2.getCanonicalPath() },
					null,
					new String[] {},
					new FileASTRequestor() {},
					null);
		} finally {
			file.delete();
			fileY.delete();
		}
	}

	/*
	 * To test isVar returning false for ast level 10 and compliance 9
	 */
	public void testBug533210_0001() throws JavaModelException {
		String contents =
				"""
			public class X {
				public static void main(String[] args) {
					var s = new Y();
				}
			}
			class Y {}""";

			ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
			parser.setSource(contents.toCharArray());
			parser.setEnvironment(null, null, null, true);
			parser.setResolveBindings(true);
			parser.setStatementsRecovery(true);
			parser.setBindingsRecovery(true);
			parser.setUnitName("module-info.java");
			Map<String, String> options = getCompilerOptions();
			options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_9);
			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_9);
			options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_9);
			parser.setCompilerOptions(options);

			ASTNode node = parser.createAST(null);
			assertTrue("Should be a compilation unit", node instanceof CompilationUnit);
			CompilationUnit cu = (CompilationUnit) node;
			TypeDeclaration typeDeclaration = (TypeDeclaration) cu.types().get(0);
			MethodDeclaration[] methods = typeDeclaration.getMethods();
			MethodDeclaration methodDeclaration = methods[0];
			VariableDeclarationStatement vStmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
			Type type = vStmt.getType();
			SimpleType simpleType = (SimpleType) type;
			assertFalse("A var", simpleType.isVar());
			Name name = simpleType.getName();
			SimpleName simpleName = (SimpleName) name;
			assertFalse("A var", simpleName.isVar());
	}
	// no longer a preview feature, test is not relevant
	@Deprecated
	public void _testBug545383_01() throws JavaModelException {
		String contents =
				"""
			class X {
				public static int foo(int i) {
					int result = switch (i) {
					case 1 -> {break 5;}
					default -> 0;
					};
					return result;
				}
			}
			""";

		ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
		parser.setSource(contents.toCharArray());
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(false);
		Map<String, String> options = getCompilerOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_12);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_12);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_12);
		options.put(CompilerOptions.OPTION_EnablePreviews, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportPreviewFeatures, CompilerOptions.IGNORE);
		parser.setCompilerOptions(options);

		ASTNode node = parser.createAST(null);
		assertTrue("Should be a compilation unit", node instanceof CompilationUnit);
		CompilationUnit cu = (CompilationUnit) node;
		IProblem[] problems = cu.getProblems();
		assertTrue(problems.length > 0);
		assertTrue(problems[0].toString().contains("preview"));
	}
	public void testBug547900_01() throws JavaModelException {
		String contents =
				"""
			class X {
				public static int foo(int i) {
					int result = switch (i) {
					case 1 -> {yield 5;}
					default -> 0;
					};
					return result;
				}
			}
			""";

		ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
		parser.setSource(contents.toCharArray());
		parser.setEnvironment(null, null, null, true);
		parser.setResolveBindings(false);
		Map<String, String> options = getCompilerOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_14);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_14);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_14);
		parser.setCompilerOptions(options);

		ASTNode node = parser.createAST(null);
		assertTrue("Should be a compilation unit", node instanceof CompilationUnit);
		CompilationUnit cu = (CompilationUnit) node;
		TypeDeclaration typeDeclaration = (TypeDeclaration) cu.types().get(0);
		MethodDeclaration[] methods = typeDeclaration.getMethods();
		MethodDeclaration methodDeclaration = methods[0];
		VariableDeclarationStatement stmt = (VariableDeclarationStatement) methodDeclaration.getBody().statements().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) stmt.fragments().get(0);
		SwitchExpression se = (SwitchExpression) fragment.getInitializer();
		YieldStatement yieldStatement = (YieldStatement) ((Block)se.statements().get(1)).statements().get(0);
		assertNotNull("Expression null", yieldStatement.getExpression());
	}
	public void testBug558517() throws IOException {
		File f1 = null, f2 = null, packDir = null;
		try {
			File rootDir = new File(System.getProperty("java.io.tmpdir"));
			packDir = new File(rootDir, "P/src/x");
			packDir.mkdirs();

			String fileName1 = "EnsureImpl$1.java";
			String fileName2 = "C9947f.java";
			f1 = createFile(
					packDir, fileName1,
					"""
						package x;
						
						class EnsureImpl$1 {
						}
						""");
			f2 = createFile(
					packDir, fileName2,
					"""
						package x;
						public final class C9947f {
						    public C9947f() {
						        try {
						            new x.EnsureImpl$1();
						        } catch (Throwable unused) {
						        }
						    }
						}
						""");
			ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
			parser.setResolveBindings(true);
			Map<String, String> options = new HashMap<>();
			JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
			parser.setCompilerOptions(options );
			parser.setEnvironment(null,
					new String[] { rootDir + "/P/src" },
					null,
					true);
			parser.createASTs(new String[] { packDir + "/" + fileName1, packDir + "/" + fileName2 },
					null,
					new String[] { "Lx/C9947f;" },
					new FileASTRequestor() {
					},
					null);
			// just ensure the above doesn't throw NPE
		} finally {
			f1.delete();
			f2.delete();
			packDir.delete();
		}
	}

	public void testGitHub316() throws JavaModelException {
		String contents =
				"public class X {\n" +
						"void m() {"
						+ " (1+2) = 3;"// was IllegalArgumentException in InfixExpression#setOperator
						+ " }"+
				"}";

			ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
			parser.setSource(contents.toCharArray());
			parser.setEnvironment(null, null, null, true);
			parser.setResolveBindings(true);
			parser.setUnitName("X.java");

			ASTNode node = parser.createAST(null);
			assertTrue("Should be a compilation unit", node instanceof CompilationUnit);
			CompilationUnit cu = (CompilationUnit) node;
			assertEquals("Problems in compilation", 1,cu.getProblems().length);
			assertEquals("The left-hand side of an assignment must be a variable",cu.getProblems()[0].getMessage());
	}
	public void testGitHub1122() throws JavaModelException {
		String contents =
				"public class X {\n" +
						"void m() {"
						+ ".a() >0);"// was IllegalArgumentException in InfixExpression#setOperator
						+ " }"+
				"}";

			ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
			parser.setSource(contents.toCharArray());
			parser.setEnvironment(null, null, null, true);
			parser.setResolveBindings(true);
			parser.setUnitName("X.java");
			parser.setStatementsRecovery(true);
			parser.setBindingsRecovery(true);

			ASTNode node = parser.createAST(null);
			assertTrue("Should be a compilation unit", node instanceof CompilationUnit);
			CompilationUnit cu = (CompilationUnit) node;
			assertEquals("Problems in compilation", 3,cu.getProblems().length);
			assertEquals("Syntax error on token \".\", invalid (",cu.getProblems()[0].getMessage());
			// XXX BatchCompiler instead reports 'Syntax error, insert "AssignmentOperator Expression" to complete Expression':
			assertEquals("The left-hand side of an assignment must be a variable",cu.getProblems()[1].getMessage());
	}
	public void testBug568629() throws JavaModelException {
		String contents =
				"public class X {\n" +
						"void m() {"
						+ "A.a-5a-a = true;"// was IllegalArgumentException in InfixExpression#setOperator
						+ " for (int j = 0; j < 1; j++) {"
						+ " \" \" + j );"// was IllegalArgumentException in InfixExpression#setOperator
						+ " }"
						+ " }"+
				"}";

			ASTParser parser = ASTParser.newParser(AST_JLS_LATEST);
			parser.setSource(contents.toCharArray());
			parser.setEnvironment(null, null, null, true);
			parser.setResolveBindings(true);
			parser.setUnitName("X.java");
			parser.setStatementsRecovery(true);
			parser.setBindingsRecovery(true);

			ASTNode node = parser.createAST(null);
			assertTrue("Should be a compilation unit", node instanceof CompilationUnit);
			CompilationUnit cu = (CompilationUnit) node;
			assertEquals("Problems in compilation", 7,cu.getProblems().length);
			assertEquals("Syntax error on tokens, ( expected instead",cu.getProblems()[0].getMessage());
			assertEquals("The left-hand side of an assignment must be a variable",cu.getProblems()[1].getMessage());
			assertEquals("Syntax error on token \"a\", delete this token",cu.getProblems()[2].getMessage());
			assertEquals("Syntax error, insert \")\" to complete Expression",cu.getProblems()[3].getMessage());
			assertEquals("Syntax error on token \"{\", ( expected after this token",cu.getProblems()[4].getMessage());
			assertEquals("The left-hand side of an assignment must be a variable",cu.getProblems()[5].getMessage());
			assertEquals("Syntax error, insert \"AssignmentOperator Expression\" to complete Expression",cu.getProblems()[6].getMessage());
	}


}