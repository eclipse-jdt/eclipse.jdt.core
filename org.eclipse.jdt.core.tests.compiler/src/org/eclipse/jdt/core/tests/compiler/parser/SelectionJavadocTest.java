/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.compiler.parser;

import java.util.Locale;
import java.util.Map;

import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.codeassist.select.SelectionJavadoc;
import org.eclipse.jdt.internal.codeassist.select.SelectionParser;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

import junit.framework.Test;

/**
 * Class to test selection in Javadoc comments.
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=54968"
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SelectionJavadocTest extends AbstractSelectionTest {

	String source;
	ICompilationUnit unit;
	StringBuilder result;

	public SelectionJavadocTest(String testName) {
		super(testName);
	}

	static {
//		TESTS_NUMBERS = new int[] { 9, 10 };
//		TESTS_RANGE = new int[] { 26, -1 };
	}

	public static Test suite() {
		return buildAllCompliancesTestSuite(SelectionJavadocTest.class);
	}

	class JavadocSelectionVisitor extends ASTVisitor {

		public boolean visit(ConstructorDeclaration constructor, ClassScope scope) {
			if (constructor.javadoc != null) {
				assertTrue("Invalid type for Javadoc on " + constructor, constructor.javadoc instanceof SelectionJavadoc);
				SelectionJavadocTest.this.result.append(constructor.javadoc.toString());
			}
			return super.visit(constructor, scope);
		}

		public boolean visit(FieldDeclaration field, MethodScope scope) {
			if (field.javadoc != null) {
				assertTrue("Invalid type for Javadoc on " + field, field.javadoc instanceof SelectionJavadoc);
				SelectionJavadocTest.this.result.append(field.javadoc.toString());
			}
			return super.visit(field, scope);
		}

		public boolean visit(MethodDeclaration method, ClassScope scope) {
			if (method.javadoc != null) {
				assertTrue("Invalid type for Javadoc on " + method, method.javadoc instanceof SelectionJavadoc);
				SelectionJavadocTest.this.result.append(method.javadoc.toString());
			}
			return super.visit(method, scope);
		}

		public boolean visit(TypeDeclaration type, BlockScope scope) {
			if (type.javadoc != null) {
				assertTrue("Invalid type for Javadoc on " + type, type.javadoc instanceof SelectionJavadoc);
				SelectionJavadocTest.this.result.append(type.javadoc.toString());
			}
			return super.visit(type, scope);
		}

		public boolean visit(TypeDeclaration type, ClassScope scope) {
			if (type.javadoc != null) {
				assertTrue("Invalid type for Javadoc on " + type, type.javadoc instanceof SelectionJavadoc);
				SelectionJavadocTest.this.result.append(type.javadoc.toString());
			}
			return super.visit(type, scope);
		}

		public boolean visit(TypeDeclaration type, CompilationUnitScope scope) {
			if (type.javadoc != null) {
				assertTrue("Invalid type for Javadoc on " + type, type.javadoc instanceof SelectionJavadoc);
				SelectionJavadocTest.this.result.append(type.javadoc.toString());
			}
			return super.visit(type, scope);
		}
	}

	protected void assertValid(String expected) {
		String actual = this.result.toString();
		if (!actual.equals(expected)) {
			System.out.println("Expected result for test "+testName()+":");
			System.out.println(Util.displayString(actual, 3));
			System.out.println("	source: [");
			System.out.print(Util.indentString(this.source, 2));
			System.out.println("]\n");
			assertEquals("Invalid selection node", expected, actual);
		}
	}
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.unit = null;
	}

	void setUnit(String name, String source) {
		this.source = source;
		this.unit = new CompilationUnit(source.toCharArray(), name, null);
		this.result = new StringBuilder();
	}

	/*
	 * Parse a method with selectionNode check
	 */
	protected CompilationResult  findJavadoc(String selection) {
		return findJavadoc(selection, 1);
	}

	protected CompilationResult findJavadoc(String selection, int occurences) {

		// Verify unit
		assertNotNull("Missing compilation unit!", this.unit);

		// Get selection start and end
		int selectionStart = this.source.indexOf(selection);
		int length = selection.length();
		int selectionEnd = selectionStart + length - 1;
		for (int i = 1; i < occurences; i++) {
			selectionStart = this.source.indexOf(selection, selectionEnd);
			selectionEnd = selectionStart + length - 1;
		}

		// Parse unit
		CompilerOptions options = new CompilerOptions(getCompilerOptions());
		SelectionParser parser = new SelectionParser(new ProblemReporter(DefaultErrorHandlingPolicies.proceedWithAllProblems(),
			options,
			new DefaultProblemFactory(Locale.getDefault())));
		CompilationUnitDeclaration unitDecl = parser.dietParse(this.unit, new CompilationResult(this.unit, 0, 0, 0), selectionStart, selectionEnd);
		parser.getMethodBodies(unitDecl);

		// Visit compilation unit declaration to find javadoc
		unitDecl.traverse(new JavadocSelectionVisitor(), unitDecl.scope);

		// Return the unit declaration result
		return unitDecl.compilationResult();
	}

	@Override
	protected Map getCompilerOptions() {
	    Map optionsMap = super.getCompilerOptions();
		optionsMap.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		optionsMap.put(CompilerOptions.OPTION_ReportInvalidJavadoc, CompilerOptions.WARNING);
		optionsMap.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, CompilerOptions.ENABLED);
	    return optionsMap;
    }

	public void test01() {
		setUnit("Test.java",
			"""
				public class Test {
					/** @see #foo() */
					void bar() {
						foo();
					}
					void foo() {}
				}
				"""
		);
		findJavadoc("foo");
		assertValid("/**<SelectOnMethod:#foo()>*/\n");
	}

	public void test02() {
		setUnit("Test.java",
			"""
				public class Test {
					/** {@link #foo() foo} */
					void bar() {
						foo();
					}
					void foo() {}
				}
				"""
		);
		findJavadoc("foo");
		assertValid("/**<SelectOnMethod:#foo()>*/\n");
	}

	public void test03() {
		setUnit("Test.java",
			"""
				public class Test {
					/** @see Test */
					void foo() {}
				}
				"""
		);
		findJavadoc("Test", 2);
		assertValid("/**<SelectOnType:Test>*/\n");
	}

	public void test04() {
		setUnit("Test.java",
			"""
				public class Test {
					/** Javadoc {@link Test} */
					void foo() {}
				}
				"""
		);
		findJavadoc("Test", 2);
		assertValid("/**<SelectOnType:Test>*/\n");
	}

	public void test05() {
		setUnit("Test.java",
			"""
				public class Test {
					int field;
					/** @see #field */
					void foo() {}
				}
				"""
		);
		findJavadoc("field", 2);
		assertValid("/**<SelectOnField:#field>*/\n");
	}

	public void test06() {
		setUnit("Test.java",
			"""
				public class Test {
					int field;
					/**{@link #field}*/
					void foo() {}
				}
				"""
		);
		findJavadoc("field", 2);
		assertValid("/**<SelectOnField:#field>*/\n");
	}

	public void test07() {
		setUnit("Test.java",
			"""
				public class Test {
					/**
					 * @see Test#field
					 * @see #foo(int, String)
					 * @see Test#foo(int, String)
					 */
					void bar() {
						foo(0, "");
					}
					int field;
					void foo(int x, String s) {}
				}
				"""
		);
		findJavadoc("foo");
		findJavadoc("String");
		findJavadoc("Test", 2);
		findJavadoc("foo", 2);
		findJavadoc("String", 2);
		findJavadoc("Test", 3);
		findJavadoc("field");
		assertValid(
			"""
				/**<SelectOnMethod:#foo(int , String )>*/
				/**<SelectOnType:String>*/
				/**<SelectOnType:Test>*/
				/**<SelectOnMethod:Test#foo(int , String )>*/
				/**<SelectOnType:String>*/
				/**<SelectOnType:Test>*/
				/**<SelectOnField:Test#field>*/
				"""
		);
	}

	public void test08() {
		setUnit("Test.java",
			"""
				public class Test {
					/**
					 * First {@link #foo(int, String)}
					 * Second {@link Test#foo(int, String) method foo}
					 * Third {@link Test#field field}
					 */
					void bar() {
						foo(0, "");
					}
					int field;
					void foo(int x, String s) {}
				}
				"""
		);
		findJavadoc("foo");
		findJavadoc("String");
		findJavadoc("Test", 2);
		findJavadoc("foo", 2);
		findJavadoc("String", 2);
		findJavadoc("Test", 3);
		findJavadoc("field");
		assertValid(
			"""
				/**<SelectOnMethod:#foo(int , String )>*/
				/**<SelectOnType:String>*/
				/**<SelectOnType:Test>*/
				/**<SelectOnMethod:Test#foo(int , String )>*/
				/**<SelectOnType:String>*/
				/**<SelectOnType:Test>*/
				/**<SelectOnField:Test#field>*/
				"""
		);
	}

	public void test09() {
		setUnit("test/junit/Test.java",
			"""
				package test.junit;
				public class Test {
					/**
					 * @see test.junit.Test
					 * @see test.junit.Test#field
					 * @see test.junit.Test#foo(Object[] array)
					 */
					void bar() {
						foo(null);
					}
					int field;
					void foo(Object[] array) {}
				}
				"""
		);
		findJavadoc("test", 2);
		findJavadoc("junit", 2);
		findJavadoc("Test", 2);
		findJavadoc("test", 3);
		findJavadoc("junit", 3);
		findJavadoc("Test", 3);
		findJavadoc("field");
		findJavadoc("test", 4);
		findJavadoc("junit", 4);
		findJavadoc("Test", 4);
		findJavadoc("foo");
		findJavadoc("Object");
		findJavadoc("array");
		assertValid(
			"""
				/**<SelectOnType:test>*/
				/**<SelectOnType:test.junit>*/
				/**<SelectOnType:test.junit.Test>*/
				/**<SelectOnType:test>*/
				/**<SelectOnType:test.junit>*/
				/**<SelectOnType:test.junit.Test>*/
				/**<SelectOnField:test.junit.Test#field>*/
				/**<SelectOnType:test>*/
				/**<SelectOnType:test.junit>*/
				/**<SelectOnType:test.junit.Test>*/
				/**<SelectOnMethod:test.junit.Test#foo(Object[] array)>*/
				/**<SelectOnType:Object>*/
				/**
				 */
				"""
		);
	}

	public void test10() {
		setUnit("test/junit/Test.java",
			"""
				package test.junit;
				public class Test {
					/** Javadoc {@linkplain test.junit.Test}
					 * {@linkplain test.junit.Test#field field}
					 * last line {@linkplain test.junit.Test#foo(Object[] array) foo(Object[])}
					 */
					void bar() {
						foo(null);
					}
					int field;
					void foo(Object[] array) {}
				}
				"""
		);
		findJavadoc("test", 2);
		findJavadoc("junit", 2);
		findJavadoc("Test", 2);
		findJavadoc("test", 3);
		findJavadoc("junit", 3);
		findJavadoc("Test", 3);
		findJavadoc("field");
		findJavadoc("test", 4);
		findJavadoc("junit", 4);
		findJavadoc("Test", 4);
		findJavadoc("foo");
		findJavadoc("Object");
		findJavadoc("array");
		assertValid(
			"""
				/**<SelectOnType:test>*/
				/**<SelectOnType:test.junit>*/
				/**<SelectOnType:test.junit.Test>*/
				/**<SelectOnType:test>*/
				/**<SelectOnType:test.junit>*/
				/**<SelectOnType:test.junit.Test>*/
				/**<SelectOnField:test.junit.Test#field>*/
				/**<SelectOnType:test>*/
				/**<SelectOnType:test.junit>*/
				/**<SelectOnType:test.junit.Test>*/
				/**<SelectOnMethod:test.junit.Test#foo(Object[] array)>*/
				/**<SelectOnType:Object>*/
				/**
				 */
				"""
		);
	}

	public void test11() {
		setUnit("Test.java",
			"""
				public class Test {
					/**
					 * @throws RuntimeException runtime exception
					 * @throws InterruptedException interrupted exception
					 */
					void foo() {}
				}
				"""
		);
		findJavadoc("RuntimeException");
		findJavadoc("InterruptedException");
		assertValid(
			"/**<SelectOnType:RuntimeException>*/\n" +
			"/**<SelectOnType:InterruptedException>*/\n"
		);
	}

	public void test12() {
		setUnit("Test.java",
			"""
				public class Test {
					/**
					 * @exception RuntimeException runtime exception
					 * @exception InterruptedException interrupted exception
					 */
					void foo() {}
				}
				"""
		);
		findJavadoc("RuntimeException");
		findJavadoc("InterruptedException");
		assertValid(
			"/**<SelectOnType:RuntimeException>*/\n" +
			"/**<SelectOnType:InterruptedException>*/\n"
		);
	}

	public void test13() {
		setUnit("Test.java",
			"""
				public class Test {
					/**
					 * @param xxx integer param
					 * @param str string param
					 */
					void foo(int xxx, String str) {}
				}
				"""
		);
		findJavadoc("xxx");
		findJavadoc("str");
		assertValid(
			"/**<SelectOnLocalVariable:xxx>*/\n" +
			"/**<SelectOnLocalVariable:str>*/\n"
		);
	}

	public void test14() {
		setUnit("Test.java",
			"""
				/**
				 * Javadoc of {@link Test}
				 * @see Field#foo
				 */
				public class Test {}
				/**
				 * Javadoc on {@link Field} to test selection in javadoc field references
				 * @see #foo
				 */
				class Field {
					/**
					 * Javadoc on {@link #foo} to test selection in javadoc field references
					 * @see #foo
					 * @see Field#foo
					 */
					int foo;
				}
				"""
		);
		findJavadoc("Field");
		findJavadoc("foo");
		findJavadoc("Field", 2);
		findJavadoc("foo", 2);
		findJavadoc("foo", 3);
		findJavadoc("foo", 4);
		findJavadoc("Field", 4);
		findJavadoc("foo", 5);
		assertValid(
			"""
				/**<SelectOnType:Field>*/
				/**<SelectOnField:Field#foo>*/
				/**<SelectOnType:Field>*/
				/**<SelectOnField:#foo>*/
				/**<SelectOnField:#foo>*/
				/**<SelectOnField:#foo>*/
				/**<SelectOnType:Field>*/
				/**<SelectOnField:Field#foo>*/
				"""
		);
	}

	public void test15() {
		setUnit("Test.java",
			"""
				/**
				 * Javadoc of {@link Test}
				 * @see Method#foo(int, String)
				 */
				public class Test {}
				/**
				 * Javadoc on {@link Method} to test selection in javadoc method references
				 * @see #foo(int, String)
				 */
				class Method {
					/**
					 * Javadoc on {@link #foo(int,String)} to test selection in javadoc method references
					 * @see #foo(int, String)
					 * @see Method#foo(int, String)
					 */
					void bar() {}
					/**
					 * Method with parameter and throws clause to test selection in javadoc
					 * @param xxx TODO
					 * @param str TODO
					 * @throws RuntimeException blabla
					 * @throws InterruptedException bloblo
					 */
					void foo(int xxx, String str) throws RuntimeException, InterruptedException {}
				}
				"""
		);
		findJavadoc("Method");
		findJavadoc("foo");
		findJavadoc("Method", 2);
		findJavadoc("foo", 2);
		findJavadoc("foo", 3);
		findJavadoc("foo", 4);
		findJavadoc("Method", 4);
		findJavadoc("foo", 5);
		findJavadoc("xxx");
		findJavadoc("str");
		findJavadoc("RuntimeException");
		findJavadoc("InterruptedException");
		assertValid(
			"""
				/**<SelectOnType:Method>*/
				/**<SelectOnMethod:Method#foo(int , String )>*/
				/**<SelectOnType:Method>*/
				/**<SelectOnMethod:#foo(int , String )>*/
				/**<SelectOnMethod:#foo(int , String )>*/
				/**<SelectOnMethod:#foo(int , String )>*/
				/**<SelectOnType:Method>*/
				/**<SelectOnMethod:Method#foo(int , String )>*/
				/**<SelectOnLocalVariable:xxx>*/
				/**<SelectOnLocalVariable:str>*/
				/**<SelectOnType:RuntimeException>*/
				/**<SelectOnType:InterruptedException>*/
				"""
		);
	}

	public void test16() {
		setUnit("Test.java",
			"""
				/**
				 * Javadoc of {@link Test}
				 * @see Other
				 */
				public class Test {}
				/**
				 * Javadoc of {@link Other}
				 * @see Test
				 */
				class Other {}
				"""
		);
		findJavadoc("Test");
		findJavadoc("Other");
		findJavadoc("Test", 3);
		findJavadoc("Other", 2);
		assertValid(
			"""
				/**<SelectOnType:Test>*/
				/**<SelectOnType:Other>*/
				/**<SelectOnType:Test>*/
				/**<SelectOnType:Other>*/
				"""
		);
	}

	public void test17() {
		setUnit("Test.java",
			"""
				/**
				 * @see Test.Field#foo
				 */
				public class Test {
					/**
					 * @see Field#foo
					 */
					class Field {
						/**
						 * @see #foo
						 * @see Field#foo
						 * @see Test.Field#foo
						 */
						int foo;
					}
				}
				"""
		);
		findJavadoc("Test");
		findJavadoc("Field");
		findJavadoc("foo");
		findJavadoc("Field", 2);
		findJavadoc("foo", 2);
		findJavadoc("foo", 3);
		findJavadoc("Field", 4);
		findJavadoc("foo", 4);
		findJavadoc("Test", 3);
		findJavadoc("Field", 5);
		findJavadoc("foo", 5);
		assertValid(
			"""
				/**<SelectOnType:Test>*/
				/**<SelectOnType:Test.Field>*/
				/**<SelectOnField:Test.Field#foo>*/
				/**<SelectOnType:Field>*/
				/**<SelectOnField:Field#foo>*/
				/**<SelectOnField:#foo>*/
				/**<SelectOnType:Field>*/
				/**<SelectOnField:Field#foo>*/
				/**<SelectOnType:Test>*/
				/**<SelectOnType:Test.Field>*/
				/**<SelectOnField:Test.Field#foo>*/
				"""
		);
	}

	public void test18() {
		setUnit("Test.java",
			"""
				/**
				 * @see Test.Method#foo()
				 */
				public class Test {
					/**
					 * @see Method#foo()
					 */
					class Method {
						/**
						 * @see #foo()
						 * @see Method#foo()
						 * @see Test.Method#foo()
						 */
						void foo() {}
					}
				}"""
		);
		findJavadoc("Test");
		findJavadoc("Method");
		findJavadoc("foo");
		findJavadoc("Method", 2);
		findJavadoc("foo", 2);
		findJavadoc("foo", 3);
		findJavadoc("Method", 4);
		findJavadoc("foo", 4);
		findJavadoc("Test", 3);
		findJavadoc("Method", 5);
		findJavadoc("foo", 5);
		assertValid(
			"""
				/**<SelectOnType:Test>*/
				/**<SelectOnType:Test.Method>*/
				/**<SelectOnMethod:Test.Method#foo()>*/
				/**<SelectOnType:Method>*/
				/**<SelectOnMethod:Method#foo()>*/
				/**<SelectOnMethod:#foo()>*/
				/**<SelectOnType:Method>*/
				/**<SelectOnMethod:Method#foo()>*/
				/**<SelectOnType:Test>*/
				/**<SelectOnType:Test.Method>*/
				/**<SelectOnMethod:Test.Method#foo()>*/
				"""
		);
	}

	public void test19() {
		setUnit("Test.java",
			"""
				/**
				 * @see Test.Other
				 */
				public class Test {
					/**
					 * @see Test
					 * @see Other
					 * @see Test.Other
					 */
					class Other {}
				}"""
		);
		findJavadoc("Test");
		findJavadoc("Other");
		findJavadoc("Test", 3);
		findJavadoc("Other", 2);
		findJavadoc("Test", 4);
		findJavadoc("Other", 3);
		assertValid(
			"""
				/**<SelectOnType:Test>*/
				/**<SelectOnType:Test.Other>*/
				/**<SelectOnType:Test>*/
				/**<SelectOnType:Other>*/
				/**<SelectOnType:Test>*/
				/**<SelectOnType:Test.Other>*/
				"""
		);
	}

	public void test20() {
		setUnit("Test.java",
			"""
				public class Test {
					void bar() {
						/**
						 * @see Field#foo
						 */
						class Field {
							/**
							 * @see #foo
							 * @see Field#foo
							 */
							int foo;
						}
					}
				}
				"""
		);
		findJavadoc("Field");
		findJavadoc("foo");
		findJavadoc("foo", 2);
		findJavadoc("Field", 3);
		findJavadoc("foo", 3);
		assertValid(
			"""
				/**<SelectOnType:Field>*/
				/**<SelectOnField:Field#foo>*/
				/**<SelectOnField:#foo>*/
				/**<SelectOnType:Field>*/
				/**<SelectOnField:Field#foo>*/
				"""
		);
	}

	public void test21() {
		setUnit("Test.java",
			"""
				public class Test {
					void bar() {
						/**
						 * @see Method#foo()
						 */
						class Method {
							/**
							 * @see #foo()
							 * @see Method#foo()
							 */
							void foo() {}
						}
					}
				}"""
		);
		findJavadoc("Method");
		findJavadoc("foo");
		findJavadoc("foo", 2);
		findJavadoc("Method", 3);
		findJavadoc("foo", 3);
		assertValid(
			"""
				/**<SelectOnType:Method>*/
				/**<SelectOnMethod:Method#foo()>*/
				/**<SelectOnMethod:#foo()>*/
				/**<SelectOnType:Method>*/
				/**<SelectOnMethod:Method#foo()>*/
				"""
		);
	}

	public void test22() {
		setUnit("Test.java",
			"""
				public class Test {
					void bar() {
						/**
						 * @see Test
						 * @see Other
						 */
						class Other {}
					}
				}"""
		);
		findJavadoc("Test", 2);
		findJavadoc("Other");
		assertValid(
			"/**<SelectOnType:Test>*/\n" +
			"/**<SelectOnType:Other>*/\n"
		);
	}

	public void test23() {
		setUnit("Test.java",
			"""
				public class Test {
					void bar() {
						new Object() {
							/**
							 * @see Field#foo
							 */
							class Field {
								/**
								 * @see #foo
								 * @see Field#foo
								 */
								int foo;
							}
						};
					}
				}
				"""
		);
		findJavadoc("Field");
		findJavadoc("foo");
		findJavadoc("foo", 2);
		findJavadoc("Field", 3);
		findJavadoc("foo", 3);
		assertValid(
			"""
				/**<SelectOnType:Field>*/
				/**<SelectOnField:Field#foo>*/
				/**<SelectOnField:#foo>*/
				/**<SelectOnType:Field>*/
				/**<SelectOnField:Field#foo>*/
				"""
		);
	}

	public void test24() {
		setUnit("Test.java",
			"""
				public class Test {
					void bar() {
						new Object() {
							/**
							 * @see Method#foo()
							 */
							class Method {
								/**
								 * @see #foo()
								 * @see Method#foo()
								 */
								void foo() {}
							}
						};
					}
				}"""
		);
		findJavadoc("Method");
		findJavadoc("foo");
		findJavadoc("foo", 2);
		findJavadoc("Method", 3);
		findJavadoc("foo", 3);
		assertValid(
			"""
				/**<SelectOnType:Method>*/
				/**<SelectOnMethod:Method#foo()>*/
				/**<SelectOnMethod:#foo()>*/
				/**<SelectOnType:Method>*/
				/**<SelectOnMethod:Method#foo()>*/
				"""
		);
	}

	public void test25() {
		setUnit("Test.java",
			"""
				public class Test {
					void bar() {
						new Object() {
							/**
							 * @see Test
							 * @see Other
							 */
							class Other {}
						};
					}
				}"""
		);
		findJavadoc("Test", 2);
		findJavadoc("Other");
		assertValid(
			"/**<SelectOnType:Test>*/\n" +
			"/**<SelectOnType:Other>*/\n"
		);
	}

	/**
	 * bug 192449: [javadoc][assist] SelectionJavadocParser should not report problems
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=192449"
	 */
	public void test26() {
		setUnit("Test.java",
			"""
				/**
				 * @see\s
				 * @throws noException
				 * @see Test
				 * @see Other
				 */
				public class Test {
					/**
					 * @see
					 * @param noParam
					 * @throws noException
					 */
					void bar() {}
				}"""
		);

		// parse and check results
		CompilationResult compilationResult = findJavadoc("Other");
		assertEquals("SelectionJavadocParser should not report errors", "", Util.getProblemLog(compilationResult, false, false));
	}
}
