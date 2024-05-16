/*******************************************************************************
 * Copyright (c) 2023, 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.IOException;
import java.util.Map;

import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.core.util.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

import junit.framework.Test;

public class UnnamedPatternsAndVariablesTest extends AbstractBatchCompilerTest {

	private static String[] JAVAC_OPTIONS = new String[] { "--enable-preview" };

	public static Test suite() {
		return buildMinimalComplianceTestSuite(UnnamedPatternsAndVariablesTest.class, F_22);
	}

	static {
		//	TESTS_NAMES = new String [] { "testInstanceOfPatternMatchingWithMixedPatterns" };
	}

	public UnnamedPatternsAndVariablesTest(String name) {
		super(name);
	}

	@Override
	protected Map<String, String> getCompilerOptions() {
		CompilerOptions compilerOptions = new CompilerOptions(super.getCompilerOptions());
		if (compilerOptions.sourceLevel == ClassFileConstants.JDK22) {
			compilerOptions.enablePreviewFeatures = true;
		}
		return compilerOptions.getMap();
	}

	public void runConformTest(String[] files, String expectedOutput) {
		if(!isJRE22Plus)
			return;
		super.runConformTest(files, expectedOutput, null, JAVAC_OPTIONS);
	}

	public void testAllSnippetsFromUnnamedVariablesAndPatternsProposal() {
		runConformTest(new String[] { "X.java", """
				import java.util.Queue;
				import java.util.LinkedList;
				import java.util.stream.Collectors;
				import java.util.stream.Stream;

				record Point(int x, int y) { }
				enum Color { RED, GREEN, BLUE }
				record ColoredPoint(Point p, Color c) { }
				record Box<T extends Ball>(T content) { }

				sealed abstract class Ball permits RedBall, BlueBall, GreenBall { }
				final  class RedBall   extends Ball { }
				final  class BlueBall  extends Ball { }
				final  class GreenBall extends Ball { }

				public class X {
					public static void main(String[] args) throws Exception {
						ColoredPoint r = new ColoredPoint(new Point(3,4), Color.GREEN);
						if (r instanceof ColoredPoint(Point(int x, int y), Color _)) {
						}
						if (r instanceof ColoredPoint(Point(int x, int y), var _)) {
						}
						for (int i = 0, _ = sideEffect(); i < 10; i++) {
						}
						Queue<Integer> q = new LinkedList<>();
						q.offer(1); q.offer(1); q.offer(1);
						while (q.size() >= 3) {
							var x = q.remove();
							var _ = q.remove();       // Unnamed variable
							var _ = q.remove();       // Unnamed variable
						}
						String s = "";
						try {
							int i = Integer.parseInt(s);
						} catch (NumberFormatException _) {        // Unnamed variable
							System.out.println("Bad number: " + s);
						} catch (NullPointerException _) {
						}
						class ScopedContext implements AutoCloseable {
							public static ScopedContext acquire() {
								return null;
							}
							@Override
							public void close() throws Exception {
							}
						}
						try (var _ = ScopedContext.acquire()) {    // Unnamed variable
						}
						Stream<String> stream = new LinkedList<String>().stream();
						stream.collect(Collectors.toMap(String::toUpperCase, _ -> "NODATA")) ;
						Ball ball = new GreenBall();
						switch (ball) {
							case RedBall   _   -> process(ball);
							case BlueBall  _  -> process(ball);
							case GreenBall _ -> stopProcessing();
						}
						Box<? extends Ball> box = new Box<>(new GreenBall());
						switch (box) {
							case Box(RedBall   red)     -> processBox(box);
							case Box(BlueBall  blue)    -> processBox(box);
							case Box(GreenBall green)   -> stopProcessing();
							case Box(var       itsNull) -> pickAnotherBox();
							default -> throw new IllegalArgumentException("Unexpected value: " + box);
						}
						switch (box) {
							case Box(RedBall _)   -> processBox(box);   // Unnamed pattern variable
							case Box(BlueBall _)  -> processBox(box);   // Unnamed pattern variable
							case Box(GreenBall _) -> stopProcessing();  // Unnamed pattern variable
							case Box(var _)       -> pickAnotherBox();  // Unnamed pattern variable
							default -> throw new IllegalArgumentException("Unexpected value: " + box);
						}
					}

					private static Object pickAnotherBox() {
						// TODO Auto-generated method stub
						return null;
					}

					private static Object processBox(Box<? extends Ball> box) {
						// TODO Auto-generated method stub
						return null;
					}

					private static Object stopProcessing() {
						return null;
					}

					private static Object process(Ball ball) {
						return null;
					}

					static int sideEffect() {
						return 0;
					}

					class Order {}

					static int count(Iterable<Order> orders) {
						int total = 0;
						for (Order _ : orders)    // Unnamed variable
							total++;
						return total;
					}
				}
				"""}, "Bad number:");
	}

	public void testCatchStatementWithUnnamedVars() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String... args) {
						try {
							throw new Exception();
						} catch( Exception _) {
							int i = 12;
							System.out.println(Integer.toString(i));
						}
					}
				}
				"""}, "12");
	}

	public void testTryWithResourcesWithUnnamedVars() {
		runConformTest(new String[] { "A.java", """
				import java.io.File;
				import java.io.FileInputStream;
				import java.io.InputStream;
				public class A {
					public static void main(String... args) {
						File f = null;
						try (final InputStream _ = new FileInputStream(f)){
							System.out.println("unexpected success");
						} catch( Exception e) {
							System.out.println("expected failure");
						}
					}
				}
				"""}, "expected failure");
	}

	public void testLambdaUnnamedParameter() {
		runConformTest(new String[] { "A.java", """
				public class A {
					interface FuncInterface {
						void abstractFun(int x, String y);
					}
					public static void main(String args[]) {
						FuncInterface fobj = (int x, String _) -> System.out.println(2 * x);
						fobj.abstractFun(5, "blah");
					}
				}
				"""}, "10");
	}

	public void testLambdaBracketedUnnamedParameter() {
		runConformTest(new String[] { "A.java", """
				import java.util.function.Function;
				public class A {
					public static void main(String... args) {
						Function<Integer, String> myFunc =  (Integer _) -> "Hello";
						System.out.println(myFunc.apply(1));
					}
				}
				"""}, "Hello");
	}

	public void testLambdaNoTypeBracketedUnnamedParameter() {
		runConformTest(new String[] { "A.java", """
				import java.util.function.Function;
				public class A {
					public static void main(String... args) {
						Function<Integer, String> myFunc =  (_) -> "Hello";
						System.out.println(myFunc.apply(1));
					}
				}
				"""}, "Hello");
	}

	public void testLambdaNoTypeNoBracketsUnnamedParameter() {
		runConformTest(new String[] { "A.java", """
				import java.util.function.Function;
				public class A {
					public static void main(String... args) {
						Function<Integer, String> myFunc =  _ -> "Hello";
						System.out.println(myFunc.apply(1));
					}
				}
				"""}, "Hello");
	}

	public void testLambdaTypeWithNoParens() {
		runNegativeTest(new String[] { "A.java", """
				import java.util.function.Function;
				public class A {
					public static void main(String... args) {
						Function<Integer, String> myFunc = Integer _ -> "Hello";
						System.out.println(myFunc.apply(1));
					}
				}
				"""},
				"""
				----------
				1. ERROR in A.java (at line 4)
					Function<Integer, String> myFunc = Integer _ -> "Hello";
					                                   ^^^^^^^
				Syntax error, insert ":: IdentifierOrNew" to complete ReferenceExpression
				----------
				2. ERROR in A.java (at line 4)
					Function<Integer, String> myFunc = Integer _ -> "Hello";
					                                   ^^^^^^^
				Syntax error, insert ";" to complete BlockStatements
				----------
				3. ERROR in A.java (at line 4)
					Function<Integer, String> myFunc = Integer _ -> "Hello";
					                                                ^^^^^^^
				Syntax error, insert "AssignmentOperator Expression" to complete Expression
				----------
				""");
	}

	public void testLambdaBiFunctionBracketedWithOneNamedParam() {
		runConformTest(new String[] { "A.java", """
				import java.util.function.BiFunction;
				public class A {
					public static void main(String... args) {
						BiFunction<Integer, Integer, String> myFunc =  (_,b) -> "Hello, " + b;
						System.out.println(myFunc.apply(2, 3));
					}
				}
				"""}, "Hello, 3");
	}

	public void testLambdaBiFunctionBracketedWithNoNamedParam() {
		runConformTest(new String[] { "A.java", """
				import java.util.function.BiFunction;
				public class A {
					public static void main(String... args) {
						BiFunction<Integer, Integer, String> myFunc =  (_,_) -> "Hello";
						System.out.println(myFunc.apply(2, 3));
					}
				}
				"""}, "Hello");
	}

	public void testLambdaBiFunctionUnbracketedWithNoNamedParam() {
		runNegativeTest(new String[] { "A.java", """
				import java.util.function.BiFunction;
				public class A {
					public static void main(String... args) {
						BiFunction<Integer, Integer, String> myFunc =  _,_ -> "Hello";
						System.out.println(myFunc.apply(2, 3));
					}
				}
				"""},
				"""
				----------
				1. ERROR in A.java (at line 4)
					BiFunction<Integer, Integer, String> myFunc =  _,_ -> "Hello";
					                                                ^
				Syntax error on token ",", -> expected
				----------
				""");
	}

	public void testInstanceOfPatternMatchingWithUnnamedPatterns() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						var namedPoint = new NamedPoint("salutations", new Point(1, 2));
						if (namedPoint instanceof NamedPoint(_, Point(_, _))) {
							System.out.println("matched point");
						}
					}
				}
				record NamedPoint(String name, Point point) {}
				record Point(int x, int y) {}
				"""}, "matched point");
	}

	public void testInstanceOfPatternMatchingWithMixedPatterns() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						var namedPoint = new NamedPoint("salutations", new Point(1, 2));
						if (namedPoint instanceof NamedPoint(_, Point(_, int y))) {
							System.out.println("matched point! y: " + y);
						}
					}
				}
				record NamedPoint(String name, Point point) {}
				record Point(int x, int y) {}
				"""}, "matched point! y: 2");
	}

	public void testInstanceOfPatternMatchingWithMixedPatterns2() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						var namedPoint = new NamedPoint("salutations", new Point(1, 2));
						if (namedPoint instanceof NamedPoint(_, Point(int x, _))) {
							System.out.println("matched point! x: " + x);
						}
					}
				}
				record NamedPoint(String name, Point point) {}
				record Point(int x, int y) {}
				"""}, "matched point! x: 1");
	}

	public void testInstanceOfPatternMatchingWithUnnamedVariables() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						var namedPoint = new NamedPoint("salutations", new Point(1, 2));
						if (namedPoint instanceof NamedPoint(String _, Point(int _, int _))) {
							System.out.println("matched point");
						}
					}
				}
				record NamedPoint(String name, Point point) {}
				record Point(int x, int y) {}
				"""}, "matched point");
	}

	public void testSwitchPatternMatchingWithUnnamedPatterns() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						var namedPoint = new NamedPoint("name", new Point(1, 2));
						switch (namedPoint) {
							case NamedPoint(_, Point(_, _)) -> System.out.println("I am utilizing pattern matching");
							default -> System.out.println("oh no");
						}
					}
				}
				record NamedPoint(String name, Point point) {}
				record Point(int x, int y) {}
				"""}, "I am utilizing pattern matching");
	}

	public void testSwitchPatternMatchingWithMixedPatterns() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						var namedPoint = new NamedPoint("name", new Point(1, 2));
						switch (namedPoint) {
							case NamedPoint(_, Point(int x, _)) -> System.out.println(x);
							default -> System.out.println("oh no");
						}
					}
				}
				record NamedPoint(String name, Point point) {}
				record Point(int x, int y) {}
				"""}, "1");
	}

	public void testSwitchPatternMatchingWithUnnamedVariables() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						var namedPoint = new NamedPoint("name", new Point(1, 2));
						switch (namedPoint) {
							case NamedPoint(String _, Point(int _, int y)) -> System.out.println(y);
							default -> System.out.println("oh no");
						}
					}
				}
				record NamedPoint(String name, Point point) {}
				record Point(int x, int y) {}
				"""}, "2");
	}

	public void testSwitchPatternMatchingWithUnnamedVariablesVar() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						var namedPoint = new NamedPoint("name", new Point(1, 2));
						switch (namedPoint) {
							case NamedPoint(String _, Point(var _, int y)) -> System.out.println(y);
							default -> System.out.println("oh no");
						}
					}
				}
				record NamedPoint(String name, Point point) {}
				record Point(int x, int y) {}
				"""}, "2");
	}

	public void testSwitchPatternMatchingWithUnnamedVariablesUnicodeEscape() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						var namedPoint = new NamedPoint("name", new Point(1, 2));
						switch (namedPoint) {
							case NamedPoint(String _, Point(var \\u005F, int y)) -> System.out.println(y);
							default -> System.out.println("oh no");
						}
					}
				}
				record NamedPoint(String name, Point point) {}
				record Point(int x, int y) {}
				"""}, "2");
	}

	public void testEnhancedForLoopVariableWithModifier() {
		runConformTest(new String[] { "A.java", """
				import java.util.List;
				public class A {
					public static void main(String... args) {
						List<String> myList = List.of("hi", "hello", "salu", "bonjour");
						int accumulator = 0;
						for (final String _ : myList) {
							accumulator++;
						}
						System.out.println(accumulator);
						accumulator = 0;
						for (final int _ : new int[0]) {
						}
						System.out.println(accumulator);
					}
				}
				"""}, "4\n0");
	}



	public void testInstanceofUnnamedPatternMatching() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						Object r = null;
						if (r instanceof ColoredPoint(Point(int x, _), _)) {
							System.out.println("Hello, World!" + x);
						}
					}
				}
				record Point(int x, int y) { }
				enum Color { RED, GREEN, BLUE }
				record ColoredPoint(Point p, Color c) { }
				"""}, "");
	}

	public void testReuseLocalUnnamedVariable() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						int _ = 1;
						int _ = 2;
						int _ = 3;
					}
				}
				record Point(int x, int y) { }
				enum Color { RED, GREEN, BLUE }
				record ColoredPoint(Point p, Color c) { }
				"""}, "");
	}

	public void testReuseLocalUnnamedVariableUnicodeEscape() {
		runConformTest(new String[] { "A.java", """
				public class A {
					public static void main(String[] args) {
						int _ = 1;
						int \\u005F = 2;
						int \\u005F = 3;
					}
				}
				record Point(int x, int y) { }
				enum Color { RED, GREEN, BLUE }
				record ColoredPoint(Point p, Color c) { }
				"""}, "");
	}

	public void testUnnamedVariableInEnhancedFor() {
		runConformTest(new String[] { "A.java", """
				import java.util.List;
				public class A {
					public static void main(String[] args) {
						List<Order> orders = List.of(new Order(), new Order());
						int total = 0;
						for (Order _ : orders)
							total++;
						System.out.println(total);
					}
				}
				class Order {}
				"""}, "2");
	}

	public void testUnnamedVariableAsLambdaParameter() {
		runConformTest(new String[] { "A.java", """
				import java.util.function.Function;
				public class A {
					public static void main(String[] args) {
						Function<Integer, Integer> myFunc = _ -> 1;
						System.out.println(myFunc.apply(0));
					}
				}
				"""}, "1");
	}

	public void testUnnamedVariableWithoutInitializer() {
		runNegativeTest(new String[] { "A.java", """
				import java.io.BufferedReader;
				import java.io.IOException;
				import java.util.List;
				public class A {
					void foo(int x, int y) {
						int _;
						int _;
						try (BufferedReader _ = null) {
						} catch (IOException _) {
						}
						for (String _ : List.of("hello")) {
						}
						int i = 12;
						for (int _; i < 14; i++) {
						}
					}
				}
				"""},
				"""
				----------
				1. ERROR in A.java (at line 6)
					int _;
					    ^
				Unnamed variables must have an initializer
				----------
				2. ERROR in A.java (at line 7)
					int _;
					    ^
				Unnamed variables must have an initializer
				----------
				3. ERROR in A.java (at line 14)
					for (int _; i < 14; i++) {
					         ^
				Unnamed variables must have an initializer
				----------
				""");
	}

	public void test001() {
		runConformTest(new String[] {
				"X.java",
				"""
					public class X {
					 public static int foo() {
					   int _ = 1;
					   return 0;
					 }
					 public static void main(String[] args) {
					   System.out.println(X.foo());
					 }
					}"""
			},
			"0");
	}

	// Test that pattern variables are allowed for the nested patterns (not just the outermost record pattern)
	public void test002() {
		runConformTest(new String[] {
				"X.java",
				"""
					@SuppressWarnings("preview")
					public class X {
					 public static int foo() {
					   int _ = bar();
					   return 0;
					 }
					 public static int bar() {
					   return 0;
					 }
					 public static void main(String[] args) {
					   System.out.println(X.foo());
					 }
					}"""
			},
			"0");
	}

	public void test003() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n" +
				"public class X {\n" +
				" public static int foo() {\n"+
				"   int _;\n"+ // Error should be thrown - uninitialized
				"   return 0;\n"+
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(X.foo());\n"+
				" }\n"+
				"}"
				},
				"""
					----------
					1. ERROR in X.java (at line 4)
						int _;
						    ^
					Unnamed variables must have an initializer
					----------
					""");
	}

	public void test004() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n" +
				"public class X {\n" +
				" public static int foo() {\n"+
				"   int _ = 0;\n"+
				"   return _;\n"+  // Error should be thrown - uninitialized
				" }\n"+
				" public static void main(String[] args) {\n"+
				"   System.out.println(X.foo());\n"+
				" }\n"+
				"}"
				},
				"""
				----------
				1. ERROR in X.java (at line 5)
					return _;
					       ^
				Syntax error, insert "-> LambdaBody" to complete Expression
				----------
				""");
	}

	public void test005() {
		runNegativeTest(new String[] {
				"X.java",
				"@SuppressWarnings(\"preview\")\n" +
				"public class X {\n" +
				"   public int _;\n"+ // Error should be thrown - Field not allowed
				" public static void main(String[] args) {\n"+
				"   System.out.println(0);\n"+
				" }\n"+
				"}"
				},
				"""
				----------
				1. ERROR in X.java (at line 3)
					public int _;
					           ^
				As of release 22, '_' is only allowed to declare unnamed patterns, local variables, exception parameters or lambda parameters
				----------
				""");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2024
	// [Patterns][Unnamed] VerifyError with unnamed pattern variable in instanceof
	public void testIssue2024() {
		runConformTest(new String[] {
				"X.java",
				"""
				record A(){}
				record R<T>(T t) {}
				public class X {
				    private static boolean foo(R<A> r) {
				        return r instanceof R(var _);
				    }
				    public static void main(String argv[]) {
				        System.out.println(foo(new R<A>(new A())) ? "Pass" : "Fail");
				    }
				}
				"""
			},
			"Pass");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2024
	// [Patterns][Unnamed] VerifyError with unnamed pattern variable in instanceof
	public void testIssue2024_2() {
		runConformTest(new String[] {
				"X.java",
				"""
				record A(){}
				record R<T>(T t) {}
				public class X {
				    private static boolean foo(R<? extends A> r) {
				        return r instanceof R(var _);
				    }
				    public static void main(String argv[]) {
				        System.out.println(foo(new R<A>(new A())) ? "Pass" : "Fail");
				    }
				}
				"""
			},
			"Pass");
	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2024
	// [Patterns][Unnamed] VerifyError with unnamed pattern variable in instanceof
	public void testIssue2024_3() {
		runConformTest(new String[] {
				"X.java",
				"""
				interface I {}
				record A() implements I {}
				record R<T>(T t) {}

				public class X {
				    private static boolean foo(R<? extends I> r) {
				        return r instanceof R(var _);
				    }
				    public static void main(String argv[]) {
				        System.out.println(foo(new R<A>(new A())) ? "Pass" : "Fail");
				    }
				}
				"""
			},
			"Pass");
	}

	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2020
	// [Patterns][Unnamed] Wasteful allocation and assignment into unnamed pattern variables.
    public void testIssue2020() throws ClassFormatException, IOException {
    	String source =
    			"""
    			public class X {
				  record Point3D (int x, int y, int z) {

				  }

				  public static void main(String[] args) {
					Object o = new Point3D(1,2,3);
					if (o instanceof Point3D(_, int y, _)) {
						System.out.println(y);
					}
				  }
				}
    			""";
    	String expectedOutput =
    			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 5, Locals: 3
			  public static void main(String[] args);
			     0  new X$Point3D [16]
			     3  dup
			     4  iconst_1
			     5  iconst_2
			     6  iconst_3
			     7  invokespecial X$Point3D(int, int, int) [18]
			    10  astore_1 [o]
			    11  aload_1 [o]
			    12  instanceof X$Point3D [16]
			    15  ifeq 43
			    18  aload_1 [o]
			    19  checkcast X$Point3D [16]
			    22  dup
			    23  invokevirtual X$Point3D.x() : int [21]
			    26  pop
			    27  dup
			    28  invokevirtual X$Point3D.y() : int [25]
			    31  istore_2 [y]
			    32  invokevirtual X$Point3D.z() : int [28]
			    35  pop
			    36  getstatic System.out : PrintStream [31]
			    39  iload_2 [y]
			    40  invokevirtual PrintStream.println(int) : void [37]
			    43  return
			    44  new MatchException [43]
			    47  dup_x1
			    48  swap
			    49  dup
			    50  invokevirtual Throwable.toString() : String [45]
			    53  swap
			    54  invokespecial MatchException(String, Throwable) [51]
			    57  athrow
			      Exception Table:
			        [pc: 23, pc: 26] -> 44 when : Throwable
			        [pc: 28, pc: 31] -> 44 when : Throwable
			        [pc: 32, pc: 35] -> 44 when : Throwable
			      Line numbers:
			        [pc: 0, line: 7]
			        [pc: 11, line: 8]
			        [pc: 36, line: 9]
			        [pc: 43, line: 11]
			      Local variable table:
			        [pc: 0, pc: 58] local: args index: 0 type: String[]
			        [pc: 11, pc: 44] local: o index: 1 type: Object
			        [pc: 32, pc: 43] local: y index: 2 type: int
			      Stack map table: number of frames 2
			        [pc: 43, append: {Object}]
			        [pc: 44, full, stack: {Throwable}, locals: {String[]}]
			""";


    	boolean savedPreview = this.enablePreview;
    	try {
	    	this.enablePreview = true;
	    	checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    	} finally {
    		this.enablePreview = savedPreview;
    	}
    	runConformTest(
                new String[] {
                        "X.java",
                        source,
                },
                "2");
    }
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2020
	// [Patterns][Unnamed] Wasteful allocation and assignment into unnamed pattern variables.
    public void testIssue2020_2() throws ClassFormatException, IOException {
    	String source =
    			"""
    			public class X {
				  record Point3D (long x, long y, long z) {

				  }

				  public static void main(String[] args) {
					Object o = new Point3D(1,2,3);
					if (o instanceof Point3D(_, long y, _)) {
						System.out.println(y);
					}
				  }
				}
    			""";
    	String expectedOutput =
    			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 8, Locals: 4
			  public static void main(String[] args);
			     0  new X$Point3D [16]
			     3  dup
			     4  lconst_1
			     5  ldc2_w <Long 2> [18]
			     8  ldc2_w <Long 3> [20]
			    11  invokespecial X$Point3D(long, long, long) [22]
			    14  astore_1 [o]
			    15  aload_1 [o]
			    16  instanceof X$Point3D [16]
			    19  ifeq 47
			    22  aload_1 [o]
			    23  checkcast X$Point3D [16]
			    26  dup
			    27  invokevirtual X$Point3D.x() : long [25]
			    30  pop2
			    31  dup
			    32  invokevirtual X$Point3D.y() : long [29]
			    35  lstore_2 [y]
			    36  invokevirtual X$Point3D.z() : long [32]
			    39  pop2
			    40  getstatic System.out : PrintStream [35]
			    43  lload_2 [y]
			    44  invokevirtual PrintStream.println(long) : void [41]
			    47  return
			    48  new MatchException [47]
			    51  dup_x1
			    52  swap
			    53  dup
			    54  invokevirtual Throwable.toString() : String [49]
			    57  swap
			    58  invokespecial MatchException(String, Throwable) [55]
			    61  athrow
			      Exception Table:
			        [pc: 27, pc: 30] -> 48 when : Throwable
			        [pc: 32, pc: 35] -> 48 when : Throwable
			        [pc: 36, pc: 39] -> 48 when : Throwable
			      Line numbers:
			        [pc: 0, line: 7]
			        [pc: 15, line: 8]
			        [pc: 40, line: 9]
			        [pc: 47, line: 11]
			      Local variable table:
			        [pc: 0, pc: 62] local: args index: 0 type: String[]
			        [pc: 15, pc: 48] local: o index: 1 type: Object
			        [pc: 36, pc: 47] local: y index: 2 type: long
			      Stack map table: number of frames 2
			        [pc: 47, append: {Object}]
			        [pc: 48, full, stack: {Throwable}, locals: {String[]}]
			""";


    	boolean savedPreview = this.enablePreview;
    	try {
	    	this.enablePreview = true;
    		checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    	} finally {
    		this.enablePreview = savedPreview;
    	}
    	runConformTest(
                new String[] {
                        "X.java",
                        source,
                },
                "2");
    }
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2020
	// [Patterns][Unnamed] Wasteful allocation and assignment into unnamed pattern variables.
    public void testIssue2020_3() throws ClassFormatException, IOException {
    	String source =
    			"""
    			public class X {
				  record Point3D (String x, String y, String z) {

				  }

				  public static void main(String[] args) {
					Object o = new Point3D("1","2","3");
					if (o instanceof Point3D(_, String y, _)) {
						System.out.println(y);
					}
				  }
				}
    			""";
    	String expectedOutput =
    			"""
			  // Method descriptor #15 ([Ljava/lang/String;)V
			  // Stack: 5, Locals: 3
			  public static void main(String[] args);
			     0  new X$Point3D [16]
			     3  dup
			     4  ldc <String "1"> [18]
			     6  ldc <String "2"> [20]
			     8  ldc <String "3"> [22]
			    10  invokespecial X$Point3D(String, String, String) [24]
			    13  astore_1 [o]
			    14  aload_1 [o]
			    15  instanceof X$Point3D [16]
			    18  ifeq 46
			    21  aload_1 [o]
			    22  checkcast X$Point3D [16]
			    25  dup
			    26  invokevirtual X$Point3D.x() : String [27]
			    29  pop
			    30  dup
			    31  invokevirtual X$Point3D.y() : String [31]
			    34  astore_2 [y]
			    35  invokevirtual X$Point3D.z() : String [34]
			    38  pop
			    39  getstatic System.out : PrintStream [37]
			    42  aload_2 [y]
			    43  invokevirtual PrintStream.println(String) : void [43]
			    46  return
			    47  new MatchException [49]
			    50  dup_x1
			    51  swap
			    52  dup
			    53  invokevirtual Throwable.toString() : String [51]
			    56  swap
			    57  invokespecial MatchException(String, Throwable) [56]
			    60  athrow
			      Exception Table:
			        [pc: 26, pc: 29] -> 47 when : Throwable
			        [pc: 31, pc: 34] -> 47 when : Throwable
			        [pc: 35, pc: 38] -> 47 when : Throwable
			      Line numbers:
			        [pc: 0, line: 7]
			        [pc: 14, line: 8]
			        [pc: 39, line: 9]
			        [pc: 46, line: 11]
			      Local variable table:
			        [pc: 0, pc: 61] local: args index: 0 type: String[]
			        [pc: 14, pc: 47] local: o index: 1 type: Object
			        [pc: 35, pc: 46] local: y index: 2 type: String
			      Stack map table: number of frames 2
			        [pc: 46, append: {Object}]
			        [pc: 47, full, stack: {Throwable}, locals: {String[]}]
			""";

    	boolean savedPreview = this.enablePreview;
    	try {
	    	this.enablePreview = true;
    		checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    	} finally {
    		this.enablePreview = savedPreview;
    	}
    	runConformTest(
                new String[] {
                        "X.java",
                        source,
                },
                "2");
    }
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2020
	// [Patterns][Unnamed] Wasteful allocation and assignment into unnamed pattern variables.
    public void testIssue2020_4() throws ClassFormatException, IOException {
    	String source =
    			"""
    			interface I {}
    			public class X implements I {

				  record Point3D (I x, I y, I z) {

				  }

				  public static void main(String[] args) {
					Object o = new Point3D(null, new X(), null);
					if (o instanceof Point3D(_, X y, _)) {
						System.out.println("Y is of class: " + y.getClass());
					}
				  }
				}
    			""";
    	String expectedOutput =
    			"""
			  // Method descriptor #17 ([Ljava/lang/String;)V
			  // Stack: 5, Locals: 3
			  public static void main(String[] args);
			     0  new X$Point3D [18]
			     3  dup
			     4  aconst_null
			     5  new X [1]
			     8  dup
			     9  invokespecial X() [20]
			    12  aconst_null
			    13  invokespecial X$Point3D(I, I, I) [21]
			    16  astore_1 [o]
			    17  aload_1 [o]
			    18  instanceof X$Point3D [18]
			    21  ifeq 74
			    24  aload_1 [o]
			    25  checkcast X$Point3D [18]
			    28  dup
			    29  invokevirtual X$Point3D.x() : I [24]
			    32  pop
			    33  dup
			    34  invokevirtual X$Point3D.y() : I [28]
			    37  dup
			    38  instanceof X [1]
			    41  ifne 48
			    44  pop2
			    45  goto 74
			    48  checkcast X [1]
			    51  astore_2 [y]
			    52  invokevirtual X$Point3D.z() : I [31]
			    55  pop
			    56  getstatic System.out : PrintStream [34]
			    59  aload_2 [y]
			    60  invokevirtual Object.getClass() : Class [40]
			    63  invokestatic String.valueOf(Object) : String [44]
			    66  invokedynamic 0 makeConcatWithConstants(String) : String [50]
			    71  invokevirtual PrintStream.println(String) : void [54]
			    74  return
			    75  new MatchException [60]
			    78  dup_x1
			    79  swap
			    80  dup
			    81  invokevirtual Throwable.toString() : String [62]
			    84  swap
			    85  invokespecial MatchException(String, Throwable) [68]
			    88  athrow
			      Exception Table:
			        [pc: 29, pc: 32] -> 75 when : Throwable
			        [pc: 34, pc: 37] -> 75 when : Throwable
			        [pc: 52, pc: 55] -> 75 when : Throwable
			      Line numbers:
			        [pc: 0, line: 9]
			        [pc: 17, line: 10]
			        [pc: 56, line: 11]
			        [pc: 74, line: 13]
			      Local variable table:
			        [pc: 0, pc: 89] local: args index: 0 type: String[]
			        [pc: 17, pc: 75] local: o index: 1 type: Object
			        [pc: 52, pc: 74] local: y index: 2 type: X
			      Stack map table: number of frames 3
			        [pc: 48, full, stack: {X$Point3D, I}, locals: {String[], Object}]
			        [pc: 74, same]
			        [pc: 75, full, stack: {Throwable}, locals: {String[]}]
			""";

    	boolean savedPreview = this.enablePreview;
    	try {
	    	this.enablePreview = true;
    		checkClassFile("X", source, expectedOutput, ClassFileBytesDisassembler.DETAILED | ClassFileBytesDisassembler.COMPACT);
    	} finally {
    		this.enablePreview = savedPreview;
    	}
    	runConformTest(
                new String[] {
                        "X.java",
                        source,
                },
                "Y is of class: class X");
    }
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2020
	// [Patterns][Unnamed] Wasteful allocation and assignment into unnamed pattern variables.
    public void testIssue2020_5() throws ClassFormatException, IOException {
    	String source =
    			"""
    			interface I {}
    			public class X implements I {

				  record Point3D (I x, I y, I z) {

				  }

				  public static void main(String[] args) {
					Object o = new Point3D(null, null, null);
					if (o instanceof Point3D(_, X y, _)) {
						System.out.println("Y is of class: " + y.getClass());
					} else {
						System.out.println("No match");
					}
				  }
				}
    			""";

    	runConformTest(
                new String[] {
                        "X.java",
                        source,
                },
                "No match");
    }
    // https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2020
 	// [Patterns][Unnamed] Wasteful allocation and assignment into unnamed pattern variables.
     public void testIssue2020_6() throws ClassFormatException, IOException {
     	String source =
     			"""
     			interface I {}
     			public class X implements I {

 				  record Point3D (I x, I y, I z) {

 				  }

 				  public static void main(String[] args) {
 					Object o = new Point3D(null, null, new X());
 					if (o instanceof Point3D(_, _, X y)) {
 						System.out.println("Y is of class: " + y.getClass());
 					}
 				  }
 				}
     			""";
     	runConformTest(
                 new String[] {
                         "X.java",
                         source,
                 },
                 "Y is of class: class X");
     }
 	// Test for regression caused by fix for https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2020
 	public void testIssue1889_2() {
 		runConformTest(
 				new String[] {
 						"X.java",
 						"""
 						public class X {
 							public Object a0 = new Boolean(false);

 							public boolean testMethod() {
 								Object ax = a0;
 								boolean res = false;

 								if (ax instanceof Boolean _) {
 									res = true;
 								}
 								return res;
 							}

 							public static void main(String argv[]) {
 								X c = new X();
 								System.out.println(c.testMethod());
 							}
 						}
 						""",
 				},
 				"true");
 	}
	// https://github.com/eclipse-jdt/eclipse.jdt.core/issues/2007
	// [Patterns][Unnamed] VerifyError with unnamed pattern variable in instanceof
	public void testIssue2007() {
		runConformTest(new String[] {
				"X.java",
				"""
				record I<J> (int x) {}
				record O<T> (I<T> i) {}
				public class X<T> {
				    public static void main(String argv[]) {
				    	Object o = null;
				    	if (o instanceof O(_)) {
				    		System.out.println("Fail");
				    	} else {
					    	System.out.println("Pass");
				    	}
				    }
				}
				"""
			},
			"Pass");
	}
}
